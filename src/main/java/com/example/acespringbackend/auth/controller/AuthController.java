    package com.example.acespringbackend.auth.controller;

    import com.example.acespringbackend.auth.dto.*;
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

        // STEP 1: Send OTP
        @PostMapping("/register")
        public Mono<ResponseEntity<SignUpResponse>> requestOtp(@RequestBody SignUpRequest request) {
            return authService.sendOtpForSignup(request)
                    .map(response -> ResponseEntity.ok(response))
                    .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(new SignUpResponse(request.getEmail(), e.getMessage()))));
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
                        } else {
                            logger.warn("Signup failed for user {}: {}", request.getEmail(), response.getMessage());
                            return ResponseEntity.badRequest().body(response);
                        }
                    });
        }

        // ✅ LOGIN: remains unchanged
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
