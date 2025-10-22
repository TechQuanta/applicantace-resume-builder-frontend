package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.LoginRequest;
import com.example.acespringbackend.auth.dto.LoginResponse;
import com.example.acespringbackend.auth.dto.OtpVerificationRequest;
import com.example.acespringbackend.auth.dto.SignUpRequest;
import com.example.acespringbackend.auth.dto.SignUpResponse;
import com.example.acespringbackend.service.WebSiteAuth; // Service handling authentication logic

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException; // For specific HTTP error responses
import reactor.core.publisher.Mono; // For reactive programming

/**
 * REST Controller for handling user authentication and registration flows.
 * This controller provides endpoints for user sign-up (including OTP verification),
 * and user login, leveraging reactive programming with Spring WebFlux.
 * All authentication logic is delegated to the {@link WebSiteAuth} service.
 */
@RestController
@RequestMapping("/ace/auth") // Base path for all authentication-related endpoints
public class AuthController {

    /**
     * Logger instance for logging messages within this controller.
     * Used for debugging, monitoring, and auditing request flows and errors.
     */
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final WebSiteAuth webSiteAuth; // Service dependency for authentication operations

    /**
     * Constructor for dependency injection. Spring automatically injects the
     * {@link WebSiteAuth} service instance.
     *
     * @param webSiteAuth The service responsible for handling all website authentication logic.
     */
    public AuthController(WebSiteAuth webSiteAuth) {
        this.webSiteAuth = webSiteAuth;
    }

    // --- Signup Flow Endpoints ---

    /**
     * Handles the initial registration request, typically involving sending an OTP (One-Time Password)
     * to the user's provided email for verification.
     *
     * @param request The {@link SignUpRequest} containing user registration details (e.g., email, username).
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link SignUpResponse}
     * indicating the status of the OTP sending process (e.g., success, user already exists).
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<SignUpResponse>> register(@RequestBody SignUpRequest request) {
        log.info("Register request received for email: {}", request.getEmail());

        return webSiteAuth.sendOtpForSignup(request)
                .map(signUpResponse -> {
                    // On successful OTP sending, return an OK response with the signup response DTO.
                    log.info("OTP sent successfully for signup to email: {}", request.getEmail());
                    return ResponseEntity.ok(signUpResponse);
                })
                .onErrorResume(ResponseStatusException.class, e -> {
                    // Handles specific HTTP status exceptions thrown by the WebSiteAuth service.
                    log.warn("Signup initiation failed for {}: {}", request.getEmail(), e.getReason());
                    // Return appropriate HTTP status and a SignUpResponse with the error message.
                    return Mono.just(ResponseEntity.status(e.getStatusCode()).body(
                            SignUpResponse.builder().email(request.getEmail()).message(e.getReason()).build()
                    ));
                })
                .onErrorResume(e -> {
                    // Catches any other unexpected exceptions during the signup initiation process.
                    log.error("An unexpected error occurred during signup initiation for {}: {}", request.getEmail(), e.getMessage(), e);
                    // Return an INTERNAL_SERVER_ERROR status.
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            SignUpResponse.builder().email(request.getEmail()).message("An unexpected server error occurred during signup initiation.").build()
                    ));
                });
    }

    /**
     * Handles the OTP verification step during the sign-up process.
     * The user submits the OTP received via email, and this endpoint verifies its correctness.
     *
     * @param request The {@link OtpVerificationRequest} containing the user's email and the OTP to verify.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link SignUpResponse}.
     * If successful, it indicates OTP verification and potentially the final registration status.
     */
    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<SignUpResponse>> verifyOtp(@RequestBody OtpVerificationRequest request) {
        log.info("OTP verification request received for email: {}", request.getEmail());

        return webSiteAuth.verifyOtpAndSignup(request)
                .map(signUpResponse -> {
                    // On successful OTP verification, return an OK response.
                    log.info("OTP verified successfully for {}. Message: {}", request.getEmail(), signUpResponse.getMessage());
                    return ResponseEntity.ok(signUpResponse);
                })
                .onErrorResume(ResponseStatusException.class, e -> {
                    // Handles specific HTTP status exceptions (e.g., invalid OTP, expired OTP).
                    log.warn("OTP verification failed for {}: {}", request.getEmail(), e.getReason());
                    // Return appropriate HTTP status and a SignUpResponse with the error message.
                    return Mono.just(ResponseEntity.status(e.getStatusCode()).body(
                            SignUpResponse.builder().email(request.getEmail()).message(e.getReason()).build()
                    ));
                })
                .onErrorResume(e -> {
                    // Catches any other unexpected exceptions during OTP verification.
                    log.error("An unexpected error occurred during OTP verification for {}: {}", request.getEmail(), e.getMessage(), e);
                    // Return an INTERNAL_SERVER_ERROR status.
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            SignUpResponse.builder().email(request.getEmail()).message("An unexpected server error occurred during OTP verification.").build()
                    ));
                });
    }

    /**
     * Handles the final step of the sign-up process, typically after OTP verification,
     * where the user's account is fully created and an authentication token is issued.
     *
     * @param request The {@link SignUpRequest} containing the complete user registration details.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link SignUpResponse}
     * with the authentication token and user details upon successful account creation.
     */
    @PostMapping("/complete-signup")
    public Mono<ResponseEntity<SignUpResponse>> completeSignup(@RequestBody SignUpRequest request) {
        log.info("Complete signup request received for email: {}", request.getEmail());

        return webSiteAuth.completeSignup(request)
                .map(signUpResponse -> {
                    // On successful signup completion, return an OK response with the full signup response DTO.
                    log.info("Signup completed successfully for user: {}", signUpResponse.getEmail());
                    return ResponseEntity.ok(signUpResponse);
                })
                .onErrorResume(ResponseStatusException.class, e -> {
                    // Handles specific HTTP status exceptions during signup finalization.
                    log.warn("Signup finalization failed for {}: {}", request.getEmail(), e.getReason());
                    // Return appropriate HTTP status and a SignUpResponse with the error message.
                    return Mono.just(ResponseEntity.status(e.getStatusCode()).body(
                            SignUpResponse.builder().email(request.getEmail()).message(e.getReason()).build()
                    ));
                })
                .onErrorResume(e -> {
                    // Catches any other unexpected exceptions during signup finalization.
                    log.error("An unexpected error occurred during signup finalization for {}: {}", request.getEmail(), e.getMessage(), e);
                    // Return an INTERNAL_SERVER_ERROR status.
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            SignUpResponse.builder().email(request.getEmail()).message("An unexpected server error occurred during signup finalization.").build()
                    ));
                });
    }

    // --- Login Endpoint ---

    /**
     * Handles user login requests, authenticating users based on their provided credentials.
     *
     * @param request The {@link LoginRequest} containing the user's email and password.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link LoginResponse}
     * with the authentication token and user details upon successful login.
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        return webSiteAuth.login(request)
                .map(loginResponse -> {
                    // On successful login, return an OK response with the login response DTO.
                    log.info("User logged in successfully: {}", request.getEmail());
                    return ResponseEntity.ok(loginResponse);
                })
                .onErrorResume(ResponseStatusException.class, e -> {
                    // Handles specific HTTP status exceptions (e.g., bad credentials, user not found).
                    log.warn("Login failed for {}: {}", request.getEmail(), e.getReason());
                    // Return appropriate HTTP status and a LoginResponse with the error message.
                    return Mono.just(ResponseEntity.status(e.getStatusCode()).body(
                            LoginResponse.builder().message(e.getReason()).build()
                    ));
                })
                .onErrorResume(e -> {
                    // Catches any other unexpected exceptions during the login process.
                    log.error("An unexpected error occurred during login for {}: {}", request.getEmail(), e.getMessage(), e);
                    // Return an INTERNAL_SERVER_ERROR status.
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            LoginResponse.builder().message("An unexpected server error occurred during login.").build()
                    ));
                });
    }
}