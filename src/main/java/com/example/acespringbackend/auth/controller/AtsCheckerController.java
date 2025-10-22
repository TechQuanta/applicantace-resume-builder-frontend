package com.example.acespringbackend.auth.controller;

import java.io.IOException;
import java.security.Principal;

import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;

import com.example.acespringbackend.auth.dto.AtsResponse; // Ensure this DTO is correctly defined
import com.example.acespringbackend.service.GeminiService; // Service that interacts with Gemini API
import com.example.acespringbackend.utility.PdfTextExtractor; // Utility for PDF text extraction

/**
 * REST Controller for handling ATS (Applicant Tracking System) related functionalities.
 * This controller provides endpoints for extracting text from PDF resumes and for
 * generating an ATS compatibility score using the Gemini AI service.
 *
 * It uses Spring WebFlux for reactive programming, allowing non-blocking operations,
 * especially useful for file uploads and external API calls.
 */
@RestController
@RequestMapping("/ats/checker") // Base path for all endpoints in this controller
public class AtsCheckerController {

    private final GeminiService geminiService;

    /**
     * Constructor for dependency injection. Spring automatically injects the
     * {@link GeminiService} instance.
     *
     * @param geminiService The service responsible for interacting with the Gemini API.
     */
    public AtsCheckerController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * Endpoint to extract plain text content from an uploaded PDF file.
     * This is useful for pre-processing resumes before sending them to an AI service
     * or for displaying raw text to the user.
     *
     * @param filePart The uploaded PDF file as a {@link FilePart}. This expects a multipart/form-data request.
     * @return A {@link Mono} of {@link ResponseEntity} containing the extracted text as a {@link String}
     * or an error message.
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<ResponseEntity<String>> extractPdfText(@RequestPart("file") FilePart filePart) {
        return DataBufferUtils.join(filePart.content()) // Joins all DataBuffers into a single buffer
                .map(this::toByteArray) // Converts the DataBuffer to a byte array
                .flatMap(bytes -> {
                    try {
                        // Extracts text from the PDF byte array using a utility method.
                        // It includes a verification step to ensure reliable extraction.
                        String extractedText = PdfTextExtractor.extractTextTwiceAndVerify(bytes);
                        if (extractedText.isEmpty()) {
                            // If extraction is unreliable (e.g., blank text), return an error.
                            return Mono.error(new IOException("Resume extraction was unreliable. Please try a different file format or ensure text is selectable."));
                        }
                        // Return the extracted text with an OK status.
                        return Mono.just(ResponseEntity.ok(extractedText));
                    } catch (IOException e) {
                        // Propagate IOException for specific error handling.
                        return Mono.error(e);
                    }
                })
                .onErrorResume(IOException.class, e ->
                        // Handles IOException specifically, returning a BAD_REQUEST status.
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("❌ Error: " + e.getMessage()))
                )
                .onErrorResume(Exception.class, e ->
                        // Handles any other unexpected exceptions, returning an INTERNAL_SERVER_ERROR.
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("❌ Error extracting text: " + e.getMessage()))
                );
    }

    /**
     * Endpoint to get an ATS (Applicant Tracking System) compatibility score for a resume
     * against a job description using the Gemini AI service.
     * This endpoint also handles user authentication and stores relevant information.
     *
     * @param filePart       The uploaded resume file (e.g., PDF) as a {@link FilePart}.
     * @param email          Optional email provided in the request. If authenticated, the authenticated email takes precedence.
     * @param deepCheck      Optional boolean flag to request a more thorough (and potentially more resource-intensive) check.
     * @param jobTitle       Optional job title for context.
     * @param jobDescription Optional job description to compare the resume against.
     * @param principal      The authenticated user's principal, automatically provided by Spring Security.
     * @return A {@link Mono} of {@link ResponseEntity} containing an {@link AtsResponse} DTO
     * with the score and other relevant information, or an error response.
     */
    @PostMapping(value = "/score", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AtsResponse>> getAtsScore(
            @RequestPart("file") FilePart filePart,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "deepCheck", defaultValue = "false") boolean deepCheck,
            @RequestParam(value = "jobTitle", required = false) String jobTitle,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            Principal principal // Used to get the authenticated user's details
    ) {
        String userEmailForDb = email; // Initialize with email from request param
        String userIdForDb = null;     // Initialize user ID

        // Check if a user is authenticated (e.g., via JWT, OAuth2)
        if (principal != null && principal.getName() != null) {
            String authenticatedEmail = principal.getName();
            userEmailForDb = authenticatedEmail; // Prioritize authenticated user's email
            // Generate a mock user ID for database storage. In a real application,
            // this would come from the user management system (e.g., database ID).
            userIdForDb = "mock-user-id-" + authenticatedEmail.hashCode();

            // Log a warning if the email parameter conflicts with the authenticated user's email
            if (email != null && !email.equalsIgnoreCase(authenticatedEmail)) {
                System.out.println("WARNING: Email parameter from request (" + email + ") does not match authenticated user's email (" + authenticatedEmail + "). Using authenticated email for storage.");
            }
        } else {
            // If no user is authenticated, ensure an email is provided in the request
            if (userEmailForDb == null || userEmailForDb.trim().isEmpty()) {
                // Return a BAD_REQUEST if email is missing for unauthenticated users
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AtsResponse("0", "", "", true, "Email is required for ATS checks that need to be saved. For unauthenticated users, providing an email is mandatory.")));
            }
        }

        // Use final variables for use in lambda expressions
        final String finalUserEmailForDb = userEmailForDb;
        final String finalUserIdForDb = userIdForDb;

        // Process the uploaded file and call GeminiService
        return DataBufferUtils.join(filePart.content()) // Join the DataBuffers into a single buffer
                .map(this::toByteArray) // Convert the DataBuffer to a byte array
                .flatMap(fileBytes ->
                    // Call the GeminiService to get the ATS score
                    geminiService.getAtsScore(
                            fileBytes,
                            filePart.filename(), // Pass the original filename to the service
                            deepCheck,
                            jobTitle,
                            jobDescription,
                            finalUserEmailForDb, // User email for database operations
                            finalUserIdForDb      // User ID for database operations
                    )
                )
                .map(responseDTO -> {
                    // Map the AtsResponse DTO to an HTTP ResponseEntity
                    if (responseDTO.isError()) {
                        // If the service indicates an error, return a BAD_REQUEST status
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
                    }
                    // Otherwise, return an OK status with the successful response
                    return ResponseEntity.ok(responseDTO);
                })
                .onErrorResume(IllegalArgumentException.class, e ->
                        // Handle IllegalArgumentException specifically (e.g., bad request parameters)
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new AtsResponse("0", "Error: " + e.getMessage(), "", true, "Invalid request parameters.")))
                )
                .onErrorResume(Exception.class, e ->
                        // Handle any other unexpected exceptions as internal server errors
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new AtsResponse("0", "An unexpected server error occurred: " + e.getMessage(), "", true, "Server error. Type: " + e.getClass().getSimpleName())))
                );
    }

    /**
     * Helper method to convert a {@link DataBuffer} into a byte array.
     * It also ensures the {@link DataBuffer} is released after being read to prevent memory leaks.
     *
     * @param buffer The {@link DataBuffer} to convert.
     * @return A byte array containing the buffer's content.
     */
    private byte[] toByteArray(DataBuffer buffer) {
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer); // IMPORTANT: Release the buffer to prevent memory leaks in reactive streams
        return bytes;
    }
}