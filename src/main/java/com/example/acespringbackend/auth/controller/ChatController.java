package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.ParaphrasingRequest;
import com.example.acespringbackend.auth.dto.ParaphrasingResponse;
import com.example.acespringbackend.service.GeminiService;
import com.example.acespringbackend.utility.PromptBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono; // Import Mono for reactive types

// Consider adding @CrossOrigin if your frontend is on a different origin
@RestController
// You can have multiple @RequestMapping annotations if you want different base paths for different methods
// Or, if all methods are under one "chat" umbrella, consolidate.
// For demonstration, I'll keep them separate or use a common base.
// Let's assume the /api/chat is for general chat and /ace for specific tools.
// So, we'll keep the @RequestMapping("/ace") here and handle the /api/chat/gemini as a separate endpoint.
@RequestMapping("/ace") // This applies to the methods within this class that don't have their own @RequestMapping
public class ChatController {

    private final GeminiService geminiService;
    private final PromptBuilder promptBuilder; // Assuming PromptBuilder is correctly injected

    public ChatController(GeminiService geminiService, PromptBuilder promptBuilder) {
        this.geminiService = geminiService;
        this.promptBuilder = promptBuilder;
    }

    /**
     * Handles general chat requests using Gemini.
     * This endpoint should probably be under a more general chat controller,
     * or you might want to consider merging it into the /ace/jot logic
     * if all text interactions go through a similar flow.
     * For now, I'll keep it as a distinct endpoint but within the same controller file.
     */
    @PostMapping("/api/chat/gemini") // Specific mapping for this method
    public Mono<String> chatWithGemini(@RequestBody String prompt) {
        System.out.println("Received chat prompt: " + prompt);
        return geminiService.getGeminiReply(prompt)
                .doOnSuccess(response -> System.out.println("Gemini response received."))
                .doOnError(e -> System.err.println("Error during Gemini chat: " + e.getMessage()));
    }

    /**
     * Handles paraphrasing/jotting requests, including content filtering.
     * This method is now fully reactive.
     */
    @PostMapping("/jot")
    public Mono<ResponseEntity<ParaphrasingResponse>> aceJot(@RequestBody ParaphrasingRequest req) {
        if (isUnwantedContentRequest(req.getInput())) {
            System.out.println("ChatController - Blocking unwanted content request: " + req.getInput());
            return Mono.just(new ResponseEntity<>(
                    new ParaphrasingResponse(
                            null,
                            true,
                            "I cannot fulfill requests that ask for large amounts of code, extensive content generation, or similar off-topic queries. Please provide specific text for paraphrasing or ask concise questions about existing content."
                    ),
                    HttpStatus.BAD_REQUEST
            ));
        }

        // The reactive chain begins here
        return Mono.fromCallable(() -> promptBuilder.buildPrompt(req)) // buildPrompt might throw exceptions, wrap in Mono.fromCallable
                   .onErrorResume(e -> { // Handle errors during prompt building
                       System.err.println("Error building prompt: " + e.getMessage());
                       return Mono.error(new RuntimeException("Error preparing request: " + e.getMessage()));
                   })
                   .flatMap(prompt -> geminiService.getGeminiReply(prompt)) // Call Gemini Service, which returns Mono<String>
                   .map(geminiContent -> new ResponseEntity<>( // Map the String response to ResponseEntity<ParaphrasingResponse>
                       new ParaphrasingResponse(geminiContent, false, null),
                       HttpStatus.OK
                   ))
                   .onErrorResume(Exception.class, e -> { // Catch any errors in the reactive chain (e.g., from GeminiService)
                       System.err.println("An unexpected error occurred during aceJot: " + e.getMessage());
                       e.printStackTrace();
                       return Mono.just(new ResponseEntity<>(
                               new ParaphrasingResponse(null, true, "An unexpected server error occurred: " + e.getMessage()),
                               HttpStatus.INTERNAL_SERVER_ERROR
                       ));
                   });
    }

    private boolean isUnwantedContentRequest(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String lowerCaseInput = input.toLowerCase();

        if (lowerCaseInput.contains("line of code") ||
            lowerCaseInput.contains("lines of code") ||
            lowerCaseInput.contains("write a program") ||
            lowerCaseInput.contains("generate code") ||
            lowerCaseInput.contains("full program")) {

            if (lowerCaseInput.matches(".*\\b(100|200|300|400|500|1000|many|tons)\\b.*")) {
                return true;
            }
        }

        if (lowerCaseInput.contains("write an essay") ||
            lowerCaseInput.contains("write a book") ||
            lowerCaseInput.contains("long article") ||
            lowerCaseInput.contains("extensive report")) {
            return true;
        }

        return false;
    }
}