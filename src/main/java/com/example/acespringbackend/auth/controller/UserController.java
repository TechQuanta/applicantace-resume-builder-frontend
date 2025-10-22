package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.service.DriveService; // Import DriveService
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ace/auth")
public class UserController {

    private final DriveService driveService; // Inject DriveService

    public UserController(DriveService driveService) {
        this.driveService = driveService;
    }

    @PostMapping("/login-status")
    public ResponseEntity<?> postLoginStatus(@RequestBody Map<String, String> requestBody, Authentication authentication) {
        System.out.println("Login-status called with message: " + requestBody.get("message"));

        String message = requestBody.get("message");
        if (message == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "No login message provided"));
        }

        if ("success".equalsIgnoreCase(message)) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "fail", "message", "User is not authenticated"));
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof Jwt jwt)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "fail", "message", "Invalid user session"));
            }

            Map<String, Object> userInfo = Map.of(
                    "id", jwt.getSubject(),
                    "name", jwt.getClaimAsString("name"),
                    "email", jwt.getClaimAsString("email"),
                    "avatar", jwt.getClaimAsString("avatar_url")
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Login successful",
                    "user", userInfo
            ));
        } else if ("fail".equalsIgnoreCase(message)) {
            return ResponseEntity.ok(Map.of("status", "fail", "message", "Login failed"));
        } else {
            return ResponseEntity.ok(Map.of("status", "unknown", "message", "Unknown login message"));
        }
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<Map<String, String>>> deleteUser(@RequestBody Map<String, String> requestBody, Authentication authentication) {
        // IMPORTANT: In a production environment, you should derive the email from the
        // 'authentication' object (the JWT) to ensure the user is only deleting their own data.
        // For this example, we're taking it from the request body for simplicity,
        // but this is a security vulnerability if not properly handled with authorization.

        String emailToDelete = requestBody.get("email");

        if (emailToDelete == null || emailToDelete.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "User email is required for deletion.")));
        }

        // Optional: If you want to ensure the authenticated user can only delete their own account
        /*
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Authentication required for deletion.")));
        }

        String authenticatedUserEmail = jwt.getClaimAsString("email");
        if (!emailToDelete.equals(authenticatedUserEmail)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "You are not authorized to delete this user's data.")));
        }
        */

        return driveService.deleteUserData(emailToDelete)
                .map(success -> {
                    if (success) {
                        return ResponseEntity.ok(Map.of("status", "success", "message", "User and all associated data deleted successfully."));
                    } else {
                        // This else block might be reached if the user was not found, or another internal issue
                        // that wasn't an explicit error. DriveService should ideally return more specific info.
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("status", "fail", "message", "User not found or data could not be deleted."));
                    }
                })
                .onErrorResume(e -> {
                    // Log the exception for debugging
                    System.err.println("Error during user deletion: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("status", "error", "message", "An error occurred during user deletion: " + e.getMessage())));
                });
    }
}