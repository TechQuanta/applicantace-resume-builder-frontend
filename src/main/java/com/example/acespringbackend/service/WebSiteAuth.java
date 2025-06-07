// src/main/java/com/example/acespringbackend/service/WebSiteAuth.java
package com.example.acespringbackend.service;

import com.example.acespringbackend.auth.dto.LoginRequest;
import com.example.acespringbackend.auth.dto.LoginResponse;
import com.example.acespringbackend.auth.dto.OtpVerificationRequest;
import com.example.acespringbackend.auth.dto.SignUpRequest;
import com.example.acespringbackend.auth.dto.SignUpResponse;
import com.example.acespringbackend.model.User;
import com.example.acespringbackend.model.JwtExpiredToken;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.repository.JwtExpiredTokenRepository;
import com.example.acespringbackend.utility.JwtUtility;

import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class WebSiteAuth {

    private static final Logger log = LoggerFactory.getLogger(WebSiteAuth.class);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final DriveService driveService;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final JwtUtility jwtUtility;
    private final JwtExpiredTokenRepository jwtExpiredTokenRepository;

    private static final long RESET_TOKEN_VALIDITY_MINUTES = 20;

    public WebSiteAuth(EmailService emailService,
                       UserRepository userRepository,
                       DriveService driveService,
                       PasswordEncoder passwordEncoder,
                       JwtUtility jwtUtility,
                       JwtExpiredTokenRepository jwtExpiredTokenRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.driveService = driveService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtility = jwtUtility;
        this.jwtExpiredTokenRepository = jwtExpiredTokenRepository;
    }

    private double bytesToMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    public Mono<SignUpResponse> sendOtpForSignup(SignUpRequest request) {
        String otp = generateOtp();
        otpStore.put(request.getEmail(), otp);

        try {
            emailService.sendOtpEmail(request.getEmail(), otp);
            return Mono.just(SignUpResponse.builder()
                    .email(request.getEmail())
                    .message("OTP sent to email")
                    .currentStorageUsageMb(0.0)
                    .build());
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage(), e);
            return Mono.error(new RuntimeException("Failed to send OTP email: " + e.getMessage(), e));
        }
    }

    public Mono<SignUpResponse> verifyOtpAndSignup(OtpVerificationRequest request) {
        String savedOtp = otpStore.get(request.getEmail());
        if (savedOtp != null && savedOtp.equals(request.getOtp())) {
            otpStore.remove(request.getEmail());
            return Mono.just(SignUpResponse.builder()
                    .email(request.getEmail())
                    .message("OTP verified")
                    .currentStorageUsageMb(0.0)
                    .build());
        } else {
            return Mono.just(SignUpResponse.builder()
                    .email(request.getEmail())
                    .message("Invalid or expired OTP")
                    .currentStorageUsageMb(0.0)
                    .build());
        }
    }

    public Mono<SignUpResponse> completeSignup(SignUpRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existingUser -> {
                    if (existingUser != null && existingUser.getEmailVerified()) {
                        log.warn("User already exists and is verified: {}", existingUser.getEmail());
                        return Mono.just(SignUpResponse.builder()
                                .email(existingUser.getEmail())
                                .username(existingUser.getUsername())
                                .linkedinUrl(existingUser.getLinkedinProfileUrl())
                                .message("User already exists")
                                .currentStorageUsageMb(bytesToMegabytes(existingUser.getCurrentDriveUsageBytes()))
                                .build());
                    } else if (existingUser != null && !existingUser.getEmailVerified()) {
                        log.info("Updating unverified user for signup completion: {}", existingUser.getEmail());
                        existingUser.setUsername(request.getUsername());
                        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                        existingUser.setEmailVerified(true);
                        existingUser.setLastLogin(LocalDateTime.now());

                        if (request.getLinkedinProfileUrl() != null && !request.getLinkedinProfileUrl().isEmpty()) {
                            existingUser.setLinkedinProfileUrl(request.getLinkedinProfileUrl());
                        }

                        return userRepository.save(existingUser)
                                .flatMap(savedUser ->
                                        driveService.createUserFolderIfNotExists(savedUser.getEmail())
                                                .flatMap(folderId -> {
                                                    savedUser.setDriveFolderId(folderId);
                                                    savedUser.setCurrentDriveUsageBytes(0L);
                                                    return userRepository.save(savedUser);
                                                })
                                                .map(finalSavedUser -> {
                                                    String token = jwtUtility.generateToken(finalSavedUser.getEmail());
                                                    return SignUpResponse.builder()
                                                            .email(finalSavedUser.getEmail())
                                                            .username(finalSavedUser.getUsername())
                                                            .linkedinUrl(finalSavedUser.getLinkedinProfileUrl())
                                                            .message("Signup successful")
                                                            .token(token)
                                                            .currentStorageUsageMb(bytesToMegabytes(finalSavedUser.getCurrentDriveUsageBytes()))
                                                            .build();
                                                })
                                                .onErrorResume(driveEx -> {
                                                    log.error("CRITICAL: Failed to create Drive folder for existing unverified user {}. Deleting user from DB.", savedUser.getEmail(), driveEx);
                                                    return userRepository.delete(savedUser)
                                                            .then(Mono.error(new RuntimeException("Signup failed: Could not create Drive folder for user.", driveEx)));
                                                })
                                );
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID().toString());
                    newUser.setUsername(request.getUsername());
                    newUser.setEmail(request.getEmail());
                    newUser.setPassword(passwordEncoder.encode(request.getPassword()));
                    newUser.setEmailVerified(true);
                    newUser.setAuthProvider(User.AuthProvider.WEBSITE);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setLastLogin(LocalDateTime.now());
                    newUser.setCurrentDriveUsageBytes(0L);

                    if (request.getLinkedinProfileUrl() != null && !request.getLinkedinProfileUrl().isEmpty()) {
                        newUser.setLinkedinProfileUrl(request.getLinkedinProfileUrl());
                    }

                    return userRepository.save(newUser)
                            .flatMap(savedUser ->
                                    driveService.createUserFolderIfNotExists(savedUser.getEmail())
                                            .flatMap(folderId -> {
                                                savedUser.setDriveFolderId(folderId);
                                                return userRepository.save(savedUser);
                                            })
                                            .map(finalSavedUser -> {
                                                String token = jwtUtility.generateToken(finalSavedUser.getEmail());
                                                return SignUpResponse.builder()
                                                        .email(finalSavedUser.getEmail())
                                                        .username(finalSavedUser.getUsername())
                                                        .linkedinUrl(finalSavedUser.getLinkedinProfileUrl())
                                                        .message("Signup successful")
                                                        .token(token)
                                                        .currentStorageUsageMb(bytesToMegabytes(finalSavedUser.getCurrentDriveUsageBytes()))
                                                        .build();
                                            })
                                            .onErrorResume(driveEx -> {
                                                log.error("CRITICAL: Failed to create Drive folder for new user {}. Deleting user from DB.", savedUser.getEmail(), driveEx);
                                                return userRepository.delete(savedUser)
                                                        .then(Mono.error(new RuntimeException("Signup failed: Could not create Drive folder for user.", driveEx)));
                                            })
                            );
                }));
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(user -> {
                    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        log.warn("Login attempt failed for user {}: Invalid credentials.", request.getEmail());
                        return Mono.just(LoginResponse.builder()
                                .message("Invalid email or password")
                                .build());
                    }
                    if (!user.getEmailVerified()) {
                        log.warn("Login attempt failed for user {}: Email not verified.", request.getEmail());
                        return Mono.just(LoginResponse.builder()
                                .message("Email not verified. Please complete signup process.")
                                .build());
                    }
                    String token = jwtUtility.generateToken(user.getEmail());
                    return Mono.just(LoginResponse.builder()
                            .token(token)
                            .username(user.getUsername())
                            .message("Login successful")
                            .currentStorageUsageMb(bytesToMegabytes(user.getCurrentDriveUsageBytes()))
                            .build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Login attempt failed: User not found for email {}.", request.getEmail());
                    return Mono.just(LoginResponse.builder()
                            .message("User not found")
                            .build());
                }))
                .onErrorResume(e -> {
                    log.error("An unexpected error occurred during login for user {}: {}", request.getEmail(), e.getMessage(), e);
                    return Mono.just(LoginResponse.builder()
                            .message("An unexpected error occurred during login.")
                            .build());
                });
    }

    /**
     * Initiates the forgot password process by sending a reset link to the user's email.
     * The link contains a time-limited JWT token.
     * This method is specifically for WEBSITE auth users.
     * @param email The email of the user requesting a password reset.
     * @param resetLinkBase The base URL for the password reset page.
     * @return Mono<Void> indicating success or failure without a direct response body.
     */
    public Mono<Void> forgotPassword(String email, String resetLinkBase) {
        return userRepository.findByEmail(email)
                // Ensure the user exists AND their authentication provider is WEBSITE
                .filter(user -> user.getAuthProvider() == User.AuthProvider.WEBSITE)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found or not a website user. Password reset is only available for website accounts.")))
                .flatMap(user -> {
                    try {
                        String resetToken = jwtUtility.generateTokenWithExpiration(user.getEmail(), Duration.ofMinutes(RESET_TOKEN_VALIDITY_MINUTES));

                        JwtExpiredToken jwtTokenRecord = new JwtExpiredToken();
                        jwtTokenRecord.setId(resetToken);
                        jwtTokenRecord.setToken(resetToken);
                        jwtTokenRecord.setExpirationTime(jwtUtility.extractExpiration(resetToken).toInstant());
                        jwtTokenRecord.setUsed(false);
                        jwtTokenRecord.setIssuedAt(Instant.now());

                        return jwtExpiredTokenRepository.save(jwtTokenRecord)
                                .flatMap(savedTokenRecord -> {
                                    String resetLink = resetLinkBase + "?token=" + resetToken;
                                    try {
                                        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
                                    } catch (MessagingException e) {
                                        throw new RuntimeException(e);
                                    }
                                    log.info("Password reset link sent to {}", user.getEmail());
                                    return Mono.empty();
                                })
                                .onErrorResume(dbEx -> {
                                    log.error("Failed to save password reset token to DB for {}: {}", user.getEmail(), dbEx.getMessage(), dbEx);
                                    return Mono.error(new RuntimeException("Failed to initiate password reset: Could not save token."));
                                });

                    } catch (Exception e) {
                        log.error("Failed to send password reset email for {}: {}", user.getEmail(), e.getMessage(), e);
                        return Mono.error(new RuntimeException("Failed to send password reset email: " + e.getMessage(), e));
                    }
                }).then();
    }

    /**
     * Resets the user's password using a valid, unused JWT token.
     * This method is specifically for WEBSITE auth users.
     * @param token The JWT token string (for database lookup).
     * @param email The email extracted from the token (already validated by caller).
     * @param newPassword The new password provided by the user.
     * @return Mono<String> indicating success or failure.
     */
    public Mono<String> resetPassword(String token, String email, String newPassword) {
        if (email == null || email.isEmpty()) {
            log.warn("Email parameter is missing for password reset.");
            return Mono.error(new RuntimeException("Invalid password reset request. Email missing."));
        }

        return jwtExpiredTokenRepository.findByTokenAndUsed(token, false)
                .switchIfEmpty(Mono.error(new RuntimeException("Password reset link is invalid, expired, or has already been used.")))
                .flatMap(tokenRecord -> {
                    if (Instant.now().isAfter(tokenRecord.getExpirationTime())) {
                        log.warn("Password reset token found in DB but is past its server-side expiration: {}", token);
                        tokenRecord.setUsed(true);
                        jwtExpiredTokenRepository.save(tokenRecord).subscribe();
                        return Mono.error(new RuntimeException("Password reset link has expired."));
                    }

                    return userRepository.findByEmail(email)
                            .filter(user -> user.getAuthProvider() == User.AuthProvider.WEBSITE)
                            .switchIfEmpty(Mono.error(new RuntimeException("User not found or not a website user for password reset.")))
                            .flatMap(user -> {
                                user.setPassword(passwordEncoder.encode(newPassword));
                                user.setLastLogin(LocalDateTime.now());

                                return userRepository.save(user)
                                        .flatMap(savedUser -> {
                                            tokenRecord.setUsed(true);
                                            return jwtExpiredTokenRepository.save(tokenRecord)
                                                    .map(updatedRecord -> {
                                                        log.info("Password successfully reset for user: {}", savedUser.getEmail());
                                                        return "Password has been reset successfully.";
                                                    });
                                        });
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error during password reset for token: {}. Error: {}", token, e.getMessage());
                    return Mono.error(new RuntimeException("Failed to reset password: " + e.getMessage(), e));
                });
    }

    private String generateOtp() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }
}
