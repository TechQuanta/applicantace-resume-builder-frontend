package com.example.acespringbackend.auth.controller;

import java.io.IOException;
import java.security.Principal;
import reactor.core.publisher.Mono; // Import Mono
import reactor.core.scheduler.Schedulers; // Import Schedulers

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import com.example.acespringbackend.auth.dto.AtsResponse;
import com.example.acespringbackend.service.GeminiService;
import com.example.acespringbackend.utility.PdfTextExtractor;

@RestController
@RequestMapping("/ats/checker")
public class AtsCheckerController {

    private final GeminiService geminiService;

    public AtsCheckerController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/extract")
    // Change return type to Mono<ResponseEntity<String>>
    public Mono<ResponseEntity<String>> extractPdfText(@RequestParam("file") MultipartFile file) {
        // Wrap blocking PdfTextExtractor.extractTextTwiceAndVerify in Mono.fromCallable
        // and run it on a separate scheduler to avoid blocking the event loop.
        return Mono.fromCallable(() -> {
            String extractedText = PdfTextExtractor.extractTextTwiceAndVerify(file);
            if (extractedText.isEmpty()) {
                throw new IOException("Resume extraction was unreliable. Please try a different file format or ensure text is selectable.");
            }
            return extractedText;
        })
        .subscribeOn(Schedulers.boundedElastic()) // Use a bounded elastic scheduler for blocking I/O
        .map(ResponseEntity::ok) // On success, wrap in ResponseEntity.ok
        .onErrorResume(IOException.class, e -> { // Catch specific IO errors
            e.printStackTrace();
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Error: " + e.getMessage()));
        })
        .onErrorResume(Exception.class, e -> { // Catch any other exceptions
            e.printStackTrace();
            return Mono.just(ResponseEntity.badRequest().body("❌ Error extracting text: " + e.getMessage()));
        });
    }

    @PostMapping(value = "/score", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // FIX THIS: Change return type to Mono<ResponseEntity<AtsResponse>>
    public Mono<ResponseEntity<AtsResponse>> getAtsScore(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "deepCheck", defaultValue = "false") boolean deepCheck,
            @RequestParam(value = "jobTitle", required = false) String jobTitle,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            Principal principal
    ) {
        String userEmailForDb = email;
        String userIdForDb = null;

        if (principal != null && principal.getName() != null) {
            String authenticatedEmail = principal.getName();
            userEmailForDb = authenticatedEmail;
            userIdForDb = "mock-user-id-" + authenticatedEmail.hashCode();
            System.out.println("Authenticated request. Using email from Principal: " + userEmailForDb + ", (Mock) User ID: " + userIdForDb);

            if (email != null && !email.equalsIgnoreCase(authenticatedEmail)) {
                System.out.println("WARNING: Email parameter from request (" + email + ") does not match authenticated user's email (" + authenticatedEmail + "). Using authenticated email for storage.");
            }
        } else {
            if (userEmailForDb == null || userEmailForDb.trim().isEmpty()) {
                System.out.println("Unauthenticated request without email. ATS score will not be saved.");
                // For immediate synchronous errors, return Mono.just
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new AtsResponse("0", "", "", true, "Email is required for ATS checks that need to be saved. For unauthenticated users, providing an email is mandatory.")));
            }
            System.out.println("Unauthenticated request. Using email from request parameter: " + userEmailForDb + ". Result will be stored under this email.");
        }

        final String finalUserEmailForDb = userEmailForDb;
        final String finalUserIdForDb = userIdForDb;

        System.out.println("Received request for ATS score. Deep check: " + deepCheck);
        System.out.println("Job Title: " + (jobTitle != null ? jobTitle : "null"));
        System.out.println("Job Description Length: " + (jobDescription != null ? jobDescription.length() : "null"));
        System.out.println("Email for storage: " + finalUserEmailForDb);

        // This is the core change: Call the reactive service method and chain reactively
        return geminiService.getAtsScore(
                    file,
                    deepCheck,
                    jobTitle,
                    jobDescription,
                    finalUserEmailForDb,
                    finalUserIdForDb
               )
               .map(responseDTO -> { // When the Mono<AtsResponse> emits, transform it to ResponseEntity
                   if (responseDTO.isError()) {
                       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
                   }
                   return ResponseEntity.ok(responseDTO);
               })
               .onErrorResume(IllegalArgumentException.class, e -> { // Handle specific expected errors
                   System.err.println("Bad Request during ATS score calculation: " + e.getMessage());
                   return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body(new AtsResponse("0", "Error: " + e.getMessage(), "", true, "Invalid request parameters.")));
               })
               .onErrorResume(Exception.class, e -> { // Catch any unexpected errors
                   System.err.println("An unexpected internal server error occurred during ATS score calculation: " + e.getMessage());
                   e.printStackTrace();
                   return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(new AtsResponse("0", "An unexpected server error occurred: " + e.getMessage(), "", true, "Server error. Type: " + e.getClass().getSimpleName())));
               });
    }
}