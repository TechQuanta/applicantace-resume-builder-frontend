// src/main/java/com/example/acespringbackend/auth/controller/FirebaseController.java
package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.GoogleAuthRequest;
import com.example.acespringbackend.auth.dto.GoogleAuthResponse;
import com.example.acespringbackend.auth.dto.GithubRequest; // Import GithubRequest DTO
import com.example.acespringbackend.auth.dto.GithubResponse; // Import GithubResponse DTO
import com.example.acespringbackend.service.FirebaseAuthService; // Service for Google/Email Firebase authentication
import com.example.acespringbackend.service.FirebaseGitHubService; // Service for GitHub Firebase authentication

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono; // Reactive type for non-blocking operations

/**
 * REST Controller for handling Firebase authentication flows.
 * This controller provides endpoints for Google and GitHub sign-in,
 * delegating the actual authentication logic to respective Firebase services.
 * All endpoints under this controller are prefixed with "/ace/auth".
 */
@RestController
@RequestMapping("/ace/auth") // Base path for all authentication-related API endpoints.
public class FirebaseController {

    private final FirebaseAuthService firebaseAuthService; // Service for standard Firebase authentication (e.g., Google, Email/Password)
    private final FirebaseGitHubService firebaseGithubService; // Service specifically for Firebase GitHub authentication

    /**
     * Constructor for dependency injection. Spring automatically injects the required
     * {@link FirebaseAuthService} and {@link FirebaseGitHubService} instances.
     *
     * @param firebaseAuthService The service handling Google and other non-GitHub Firebase authentications.
     * @param firebaseGithubService The service specifically handling GitHub Firebase authentication.
     */
    public FirebaseController(FirebaseAuthService firebaseAuthService, FirebaseGitHubService firebaseGithubService) {
        this.firebaseAuthService = firebaseAuthService;
        this.firebaseGithubService = firebaseGithubService;
    }

    /**
     * Handles authentication requests initiated via Google Sign-In.
     * It expects a Firebase ID Token in the request body, which is then
     * verified and authenticated by the {@link FirebaseAuthService}.
     *
     * @param request {@link GoogleAuthRequest} containing the Firebase ID Token
     * obtained from the client-side Google Sign-In process.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link GoogleAuthResponse}
     * with user details (token, email, username, etc.) upon successful authentication,
     * or an error response with {@link HttpStatus#UNAUTHORIZED} if authentication fails.
     */
    @PostMapping("/google") // Specific endpoint for Google login: /ace/auth/google
    public Mono<ResponseEntity<GoogleAuthResponse>> loginWithGoogle(@RequestBody GoogleAuthRequest request) {
        // Delegate the authentication logic to the firebaseAuthService.
        return firebaseAuthService.authenticate(request)
                // On successful authentication, wrap the response in a 200 OK ResponseEntity.
                .map(ResponseEntity::ok)
                // If an error occurs anywhere in the reactive chain, handle it.
                .onErrorResume(e -> {
                    // Log the error for server-side debugging.
                    System.err.println("Controller error during Google login: " + e.getMessage());

                    // Return a 401 Unauthorized response with a structured error message.
                    // The authProvider field is repurposed here to carry the error message for simplicity.
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new GoogleAuthResponse(
                                    null, // token (not provided on error)
                                    null, // email
                                    null, // username
                                    null, // imageUrl
                                    null, // driveFolderId
                                    "ERROR: " + e.getMessage(), // authProvider will contain the error message
                                    null, // currentStorageUsageMb
                                    0.0 // userDriveQuotaMb
                            )));
                });
    }

    /**
     * Handles authentication requests initiated via GitHub Sign-In.
     * Similar to Google login, it expects a Firebase ID Token (derived from GitHub authentication)
     * which is then processed by the {@link FirebaseGitHubService}.
     *
     * @param request {@link GithubRequest} containing the Firebase ID Token
     * obtained from the client-side GitHub Sign-In process.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link GithubResponse}
     * with user details upon successful authentication, or an error response
     * with {@link HttpStatus#UNAUTHORIZED} if authentication fails.
     */
    @PostMapping("/github") // Specific endpoint for GitHub login: /ace/auth/github
    public Mono<ResponseEntity<GithubResponse>> loginWithGithub(@RequestBody GithubRequest request) {
        // Delegate the authentication logic to the firebaseGithubService.
        return firebaseGithubService.authenticate(request)
                // On successful authentication, wrap the response in a 200 OK ResponseEntity.
                .map(ResponseEntity::ok)
                // If an error occurs, handle it.
                .onErrorResume(e -> {
                    // Log the error for server-side debugging.
                    System.err.println("Controller error during GitHub login: " + e.getMessage());

                    // Return a 401 Unauthorized response with a structured error message.
                    // The authProvider field is repurposed here to carry the error message.
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new GithubResponse(
                                    null, // token (not provided on error)
                                    null, // email
                                    null, // username
                                    null, // imageUrl
                                    null, // driveFolderId
                                    "ERROR: " + e.getMessage(), // authProvider will contain the error message
                                    null, // currentStorageUsageMb
                                    0L // userDriveQuotaMb (assuming Long for GitHub response for consistency)
                            )));
                });
    }
}