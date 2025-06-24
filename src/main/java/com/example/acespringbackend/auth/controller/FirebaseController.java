// src/main/java/com/example/acespringbackend/auth/controller/FirebaseController.java
package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.GoogleAuthRequest;
import com.example.acespringbackend.auth.dto.GoogleAuthResponse;
import com.example.acespringbackend.auth.dto.GithubRequest; // Import GithubRequest
import com.example.acespringbackend.auth.dto.GithubResponse; // Import GithubResponse
import com.example.acespringbackend.service.FirebaseAuthService;
import com.example.acespringbackend.service.FirebaseGitHubService; // Import FirebaseGithubService

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ace/auth") // Base path for auth
public class FirebaseController {

    private final FirebaseAuthService firebaseAuthService;
    private final FirebaseGitHubService firebaseGithubService; // Inject the GitHub service

    // Constructor injection for both authentication services
    public FirebaseController(FirebaseAuthService firebaseAuthService, FirebaseGitHubService firebaseGithubService) {
        this.firebaseAuthService = firebaseAuthService;
        this.firebaseGithubService = firebaseGithubService;
    }

    /**
     * Handles authentication requests initiated via Google Sign-In using Firebase ID tokens.
     * @param request GoogleAuthRequest containing the Firebase ID Token.
     * @return Mono of ResponseEntity with GoogleAuthResponse or an error response.
     */
    @PostMapping("/google") // Specific endpoint for Google login
    public Mono<ResponseEntity<GoogleAuthResponse>> loginWithGoogle(@RequestBody GoogleAuthRequest request) {
        return firebaseAuthService.authenticate(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    System.err.println("Controller error during Google login: " + e.getMessage()); // Log the error

                    // Return a 401 Unauthorized or 400 Bad Request with a structured error response
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new GoogleAuthResponse(
                                    null, // token
                                    null, // email
                                    null, // username
                                    null, // imageUrl
                                    null,
                                    "ERROR: " + e.getMessage(), // authProvider will contain the error message
                                    0.0 // currentStorageUsageMb,
                                   
                            )));
                });
    }

    /**
     * Handles authentication requests initiated via GitHub Sign-In using Firebase ID tokens.
     * @param request GithubRequest containing the Firebase ID Token.
     * @return Mono of ResponseEntity with GithubResponse or an error response.
     */
    @PostMapping("/github") // Specific endpoint for GitHub login
    public Mono<ResponseEntity<GithubResponse>> loginWithGithub(@RequestBody GithubRequest request) {
        return firebaseGithubService.authenticate(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    System.err.println("Controller error during GitHub login: " + e.getMessage()); // Log the error

                    // Return a 401 Unauthorized or 400 Bad Request with a structured error response
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new GithubResponse(
                                    null, // token
                                    null, // email
                                    null, // username
                                    null, // imageUrl
                                    null,
                                    "ERROR: " + e.getMessage(), // authProvider will contain the error message
                                    null, 0L // currentStorageUsageMb
                            )));
                });
    }
}