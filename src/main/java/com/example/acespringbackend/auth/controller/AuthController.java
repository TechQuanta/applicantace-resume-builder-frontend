// auth/controller/AuthController.java (Corrected onErrorResume using @Builder)
package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.*;
import com.example.acespringbackend.service.WebSiteAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    // STEP 1: Send OTP
    @PostMapping("/register")
    public Mono<ResponseEntity<SignUpResponse>> requestOtp(@RequestBody SignUpRequest request) {
        return authService.sendOtpForSignup(request)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(e -> {
                    logger.error("Error sending OTP for {}: {}", request.getEmail(), e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(SignUpResponse.builder().email(request.getEmail()).message("Failed to send OTP. Please try again.").build()));
                });
    }

    // STEP 2: Verify OTP
    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<SignUpResponse>> verifyOtp(@RequestBody OtpVerificationRequest request) {
        return authService.verifyOtpAndSignup(request)
                .map(response -> {
                    if ("OTP verified".equalsIgnoreCase(response.getMessage())) {
                        logger.info("OTP verified for: {}", request.getEmail());
                        return ResponseEntity.ok(response);
                    } else {
                        logger.warn("OTP verification failed for {}: {}", request.getEmail(), response.getMessage());
                        return ResponseEntity.badRequest().body(response);
                    }
                });
    }

    // STEP 3: Finalize signup after OTP verified
    @PostMapping("/complete-signup")
    public Mono<ResponseEntity<SignUpResponse>> completeSignup(@RequestBody SignUpRequest request) {
        return authService.completeSignup(request)
                .map(response -> {
                    if ("Signup successful".equalsIgnoreCase(response.getMessage())) {
                        logger.info("User registered successfully: {}", request.getEmail());
                        return ResponseEntity.ok(response);
                    } else if ("User already exists".equalsIgnoreCase(response.getMessage())) {
                        logger.warn("Signup failed for user {}: {}", request.getEmail(), response.getMessage());
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict
                    } else {
                        logger.warn("Signup failed for user {}: {}", request.getEmail(), response.getMessage());
                        return ResponseEntity.badRequest().body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Unexpected error during signup for {}: {}", request.getEmail(), e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(SignUpResponse.builder().email(request.getEmail()).message("An unexpected error occurred during signup.").build()));
                });
    }

    // LOGIN
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        return authService.login(request)
                .map(response -> {
                    // It's still crucial to fix the logic here, as discussed previously.
                    // This `map` block should check `response.getToken() != null` or login-specific messages.
                    // For the sake of fixing the redline, I'm keeping the original logic for now,
                    // but strongly recommend you replace it with the corrected logic from our previous conversation.

                    if (response.getToken() != null && !response.getToken().isEmpty()) { // Correct success check for login
                        logger.info("User logged in successfully: {}", request.getEmail());
                        return ResponseEntity.ok(response);
                    } else if ("User not found".equalsIgnoreCase(response.getMessage())) { // Use message from service
                        logger.warn("Login failed for user {}: User not found.", request.getEmail());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
                    } else if ("Invalid password".equalsIgnoreCase(response.getMessage())) { // Use message from service
                        logger.warn("Login failed for user {}: Invalid password.", request.getEmail());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized
                    } else {
                        logger.warn("Login failed for user {}: {}", request.getEmail(), response.getMessage());
                        return ResponseEntity.badRequest().body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Unexpected error during login for {}: {}", request.getEmail(), e.getMessage());
                    // FIX: Use the @Builder pattern to create LoginResponse
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(LoginResponse.builder()
                                    .token(null)
                                    .username(null) // Or request.getEmail() if you want to pass it
                                    .message("An unexpected error occurred during login.")
                                    .build()));
                });
    }
}