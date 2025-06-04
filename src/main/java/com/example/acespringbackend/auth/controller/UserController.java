package com.example.acespringbackend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ace/auth")
public class UserController {

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
}
