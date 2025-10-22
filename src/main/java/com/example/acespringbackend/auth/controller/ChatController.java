package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.ParaphrasingRequest;
import com.example.acespringbackend.auth.dto.ParaphrasingResponse;
import com.example.acespringbackend.service.GeminiService; // Service for interacting with Gemini AI
import com.example.acespringbackend.utility.PromptBuilder; // Utility for building AI prompts
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Import for specifying content types
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono; // Import Mono for reactive types

// Consider adding @CrossOrigin if your frontend is on a different origin.
// For example: @CrossOrigin(origins = "http://localhost:3000")
// This annotation enables cross-origin resource sharing for this controller.
@RestController
@RequestMapping("/ace") // Base path for all endpoints within this controller, related to ACE functionalities.
public class ChatController {

    private final GeminiService geminiService;
    private final PromptBuilder promptBuilder; // Assumes PromptBuilder is a Spring component and correctly injected.

    /**
     * Constructor for dependency injection. Spring automatically injects the required
     * {@link GeminiService} and {@link PromptBuilder} instances.
     *
     * @param geminiService The service responsible for making calls to the Gemini AI.
     * @param promptBuilder The utility class used to construct formatted prompts for the AI.
     */
    public ChatController(GeminiService geminiService, PromptBuilder promptBuilder) {
        this.geminiService = geminiService;
        this.promptBuilder = promptBuilder;
    }

    /**
     * Handles general chat requests by sending a raw text prompt to the Gemini AI.
     * This endpoint is designed for direct conversational interactions where the AI's
     * raw text response is expected.
     *
     * @param prompt The user's input prompt as a raw {@link String} in the request body.
     * @return A {@link Mono} of {@link String} containing the Gemini AI's reply.
     * Error handling is basic, logging issues and returning a simple error message.
     */
    @PostMapping(value = "/api/chat/gemini", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> chatWithGemini(@RequestBody String prompt) {
        System.out.println("Received chat prompt: " + prompt); // Log the incoming prompt for debugging.
        return geminiService.getGeminiReply(prompt)
                .doOnSuccess(response -> System.out.println("Gemini response received.")) // Log successful response.
                .doOnError(e -> System.err.println("Error during Gemini chat: " + e.getMessage())) // Log errors.
                .onErrorResume(Exception.class, e -> {
                    // Fallback for any error during Gemini interaction, returning a user-friendly message.
                    System.err.println("An unexpected error occurred during chatWithGemini: " + e.getMessage());
                    return Mono.just("An error occurred while processing your chat request. Please try again later.");
                });
    }

    /**
     * Handles paraphrasing and "jotting" requests. This endpoint processes an input text
     * based on specified parameters (tone, style, word limit, etc.) and leverages the
     * Gemini AI for text transformation. It also includes an initial content filter
     * to prevent unwanted or overly broad requests.
     *
     * @param req The {@link ParaphrasingRequest} DTO containing the input text and all
     * parameters for text transformation.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link ParaphrasingResponse}
     * with the processed content or an error message if the request cannot be fulfilled.
     */
    @PostMapping(value = "/jot", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ParaphrasingResponse>> aceJot(@RequestBody ParaphrasingRequest req) {
        // Step 1: Perform initial content filtering based on the input.
        // This acts as a guardrail to block requests asking for large amounts of code or extensive content.
        if (isUnwantedContentRequest(req.getInput())) {
            System.out.println("ChatController - Blocking unwanted content request: " + req.getInput());
            return Mono.just(new ResponseEntity<>(
                    new ParaphrasingResponse(
                            null, // No paraphrased content
                            true, // Indicate an error
                            "I cannot fulfill requests that ask for large amounts of code, extensive content generation, or similar off-topic queries. Please provide specific text for paraphrasing or ask concise questions about existing content."
                    ),
                    HttpStatus.BAD_REQUEST // Return HTTP 400 Bad Request status.
            ));
        }

        // Step 2: Build the AI prompt reactively.
        // Mono.fromCallable is used to wrap a potentially blocking or exception-throwing operation
        // (like promptBuilder.buildPrompt) into a reactive stream.
        return Mono.fromCallable(() -> promptBuilder.buildPrompt(req))
                   .onErrorResume(e -> { // Handle errors that occur during the prompt building phase.
                       System.err.println("Error building prompt: " + e.getMessage());
                       // Return a Mono.error to propagate the error further down the reactive chain,
                       // which will be caught by the final onErrorResume.
                       return Mono.error(new RuntimeException("Error preparing request: " + e.getMessage()));
                   })
                   // Step 3: Call the Gemini AI service with the constructed prompt.
                   // flatMap is used because geminiService.getGeminiReply returns a Mono<String>,
                   // allowing the reactive stream to continue with the result of this async operation.
                   .flatMap(prompt -> geminiService.getGeminiReply(prompt))
                   // Step 4: Map the Gemini AI's string response to the desired ParaphrasingResponse DTO.
                   // This is executed upon successful reception of the AI's content.
                   .map(geminiContent -> new ResponseEntity<>(
                       new ParaphrasingResponse(geminiContent, false, null), // Create a success response.
                       HttpStatus.OK // Return HTTP 200 OK status.
                   ))
                   // Step 5: Centralized error handling for any exceptions occurring in the entire reactive chain.
                   // This catches exceptions from prompt building, Gemini service calls, or any other unexpected issues.
                   .onErrorResume(Exception.class, e -> {
                       System.err.println("An unexpected error occurred during aceJot: " + e.getMessage());
                       e.printStackTrace(); // Print stack trace for detailed error analysis in logs.
                       // Return an INTERNAL_SERVER_ERROR status with a generic error message to the client.
                       return Mono.just(new ResponseEntity<>(
                               new ParaphrasingResponse(null, true, "An unexpected server error occurred: " + e.getMessage()),
                               HttpStatus.INTERNAL_SERVER_ERROR
                       ));
                   });
    }

    /**
     * Helper method to detect and filter out requests that are likely asking for
     * extensive content generation (e.g., long code snippets, full essays, reports)
     * which might be outside the intended scope or resource limits of this service.
     *
     * @param input The raw input string provided by the user.
     * @return {@code true} if the input contains patterns indicating an unwanted,
     * overly extensive content generation request; {@code false} otherwise.
     */
    private boolean isUnwantedContentRequest(String input) {
        // Return false immediately if the input is null or blank, as it's not "unwanted content" in this context.
        if (input == null || input.isBlank()) {
            return false;
        }
        String lowerCaseInput = input.toLowerCase(); // Convert to lowercase for case-insensitive matching.

        // Check for keywords related to code generation.
        if (lowerCaseInput.contains("line of code") ||
            lowerCaseInput.contains("lines of code") ||
            lowerCaseInput.contains("write a program") ||
            lowerCaseInput.contains("generate code") ||
            lowerCaseInput.contains("full program")) {

            // If code-related keywords are found, further check for indicators of large quantities.
            // This regex looks for numbers (100, 200, etc.) or words like "many", "tons" near code terms.
            if (lowerCaseInput.matches(".*\\b(100|200|300|400|500|1000|many|tons)\\b.*")) {
                return true; // Block if it seems to be a request for a large amount of code.
            }
        }

        // Check for keywords related to extensive essay/report writing.
        if (lowerCaseInput.contains("write an essay") ||
            lowerCaseInput.contains("write a book") ||
            lowerCaseInput.contains("long article") ||
            lowerCaseInput.contains("extensive report")) {
            return true; // Block if it seems to be a request for a large amount of text.
        }

        return false; // If no unwanted patterns are detected, allow the request to proceed.
    }
}