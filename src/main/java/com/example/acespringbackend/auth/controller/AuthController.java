package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.LoginRequest;
import com.example.acespringbackend.auth.dto.LoginResponse;
import com.example.acespringbackend.auth.dto.SignUpRequest;
import com.example.acespringbackend.auth.dto.SignUpResponse;
import com.example.acespringbackend.service.WebSiteAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ace/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final WebSiteAuth authService;

    public AuthController(WebSiteAuth authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<SignUpResponse>> register(@RequestBody SignUpRequest request) {
        return authService.signup(request)
                .map(response -> {
                    if ("Signup successful".equalsIgnoreCase(response.getMessage())) {
                        logger.info("User registered successfully: {}", request.getEmail());
                        return ResponseEntity.ok(response);
                    } else {
                        logger.warn("Signup failed for user {}: {}", request.getEmail(), response.getMessage());
                        return ResponseEntity.badRequest().body(response);
                    }
                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        return authService.login(request)
                .map(response -> {
                    if (response.getToken() != null) {
                        logger.info("User logged in successfully: {}", request.getEmail());
                        return ResponseEntity.ok(response);
                    } else {
                        logger.warn("Login failed for user {}.", request.getEmail());
                        return ResponseEntity.status(401).body(response);
                    }
                });
    }
}
