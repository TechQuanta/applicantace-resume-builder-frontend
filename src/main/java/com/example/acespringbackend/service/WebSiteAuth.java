package com.example.acespringbackend.service;

import com.example.acespringbackend.auth.dto.*;
import com.example.acespringbackend.auth.dto.LoginResponse;
import com.example.acespringbackend.auth.dto.OtpVerificationRequest;
import com.example.acespringbackend.auth.dto.SignUpRequest;
import com.example.acespringbackend.auth.dto.SignUpResponse;
import com.example.acespringbackend.model.User;
import com.example.acespringbackend.model.JwtExpiredToken;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.repository.JwtExpiredTokenRepository;
import com.example.acespringbackend.utility.JwtUtility;
import com.example.acespringbackend.service.EmailService;
import com.example.acespringbackend.service.DriveService;
import com.example.acespringbackend.service.OTPStorageService;

import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class WebSiteAuth {

    private static final Logger log = LoggerFactory.getLogger(WebSiteAuth.class);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final DriveService driveService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtility;
    private final JwtExpiredTokenRepository jwtExpiredTokenRepository;
    private final OTPStorageService otpStorageService;

    public WebSiteAuth(EmailService emailService,
                       UserRepository userRepository,
                       DriveService driveService,
                       PasswordEncoder passwordEncoder,
                       JwtUtility jwtUtility,
                       JwtExpiredTokenRepository jwtExpiredTokenRepository,
                       OTPStorageService otpStorageService) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.driveService = driveService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtility = jwtUtility;
        this.jwtExpiredTokenRepository = jwtExpiredTokenRepository;
        this.otpStorageService = otpStorageService;
    }

    private double bytesToMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }


    /**
     * Sends an OTP for user registration.
     * @param request SignUpRequest containing user email.
     * @return Mono of SignUpResponse indicating OTP sent status.
     */
    public Mono<SignUpResponse> sendOtpForSignup(SignUpRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existingUser -> {
                    if (existingUser != null && existingUser.getEmailVerified()) {
                        log.warn("Attempted signup OTP for already verified user: {}", request.getEmail());
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "An account with this email already exists and is verified. Please try logging in."));
                    }
                    return sendOtpAndBuildResponse(request);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    return sendOtpAndBuildResponse(request);
                }));
    }

    private Mono<SignUpResponse> sendOtpAndBuildResponse(SignUpRequest request) {
        String otp = generateOtp();
        otpStorageService.storeOtp(request.getEmail(), otp);
        log.debug("OTP generated and stored for email: {}", request.getEmail());

        try {
            emailService.sendOtpEmail(request.getEmail(), otp);
            log.info("OTP email successfully queued for sending to: {}", request.getEmail());
            return Mono.just(SignUpResponse.builder()
                    .email(request.getEmail()) // Email is already sent here
                    .message("An OTP has been sent to your email. Please check your inbox and spam folder.")
                    .currentStorageUsageMb(0.0)
                    .driveFolderId(null)
                    .authProvider(User.AuthProvider.WEBSITE.name())
                    .build());
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", request.getEmail(), e.getMessage(), e);
            otpStorageService.removeOtp(request.getEmail());
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "We couldn't send the OTP. Please try again."));
        } catch (Exception e) {
            log.error("An unexpected error occurred while sending OTP to {}: {}", request.getEmail(), e.getMessage(), e);
            otpStorageService.removeOtp(request.getEmail());
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again."));
        }
    }


    /**
     * Verifies the OTP provided by the user.
     * @param request OtpVerificationRequest containing email and OTP.
     * @return Mono of SignUpResponse indicating OTP verification status.
     */
    public Mono<SignUpResponse> verifyOtpAndSignup(OtpVerificationRequest request) {
        if (otpStorageService.validateOtp(request.getEmail(), request.getOtp())) {
            otpStorageService.removeOtp(request.getEmail()); // OTP consumed
            log.info("OTP successfully verified for user: {}", request.getEmail());
            return Mono.just(SignUpResponse.builder()
                    .email(request.getEmail()) // Email is already sent here
                    .message("OTP verified successfully. You can now complete your registration.")
                    .currentStorageUsageMb(0.0)
                    .driveFolderId(null)
                    .authProvider(User.AuthProvider.WEBSITE.name())
                    .build());
        } else {
            log.warn("Invalid or expired OTP attempt for user: {}", request.getEmail());
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The OTP is invalid or has expired. Please request a new one."));
        }
    }

    /**
     * Completes the user signup process after OTP verification. This involves
     * saving user details and creating a dedicated Google Drive folder for them.
     *
     * @param request SignUpRequest containing full user details.
     * @return Mono of SignUpResponse with user details, JWT, and drive folder ID.
     */
    public Mono<SignUpResponse> completeSignup(SignUpRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existingUser -> {
                    // Scenario 1: User exists and is already verified
                    if (existingUser.getEmailVerified()) {
                        log.warn("Attempted to complete signup for an already verified user: {}", existingUser.getEmail());
                        return Mono.<SignUpResponse>error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your account is already verified. Please proceed to login."));
                    }
                    // Scenario 2: User exists but not verified (e.g., email initially existed, now completing signup)
                    log.info("Updating unverified user details for signup completion: {}", existingUser.getEmail());
                    return processUserRegistration(request, existingUser);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Scenario 3: Truly new user (email not found at all, proceeds directly after OTP verification)
                    log.info("Creating new user for signup completion: {}", request.getEmail());
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID().toString()); // Generate a unique ID for new user
                    newUser.setEmail(request.getEmail()); // Set email for the new user
                    return processUserRegistration(request, newUser);
                }));
    }

    private Mono<SignUpResponse> processUserRegistration(SignUpRequest request, User userToSave) {
        userToSave.setUsernameField(request.getUsername());
        userToSave.setPassword(passwordEncoder.encode(request.getPassword()));
        userToSave.setEmailVerified(true);
        userToSave.setLastLogin(LocalDateTime.now());
        userToSave.setAuthProvider(User.AuthProvider.WEBSITE);
        userToSave.setCreatedAt(userToSave.getCreatedAt() != null ? userToSave.getCreatedAt() : LocalDateTime.now());

        Optional.ofNullable(request.getLinkedinProfileUrl())
                .filter(url -> !url.isEmpty())
                .ifPresent(userToSave::setLinkedinProfileUrl);

        return userRepository.save(userToSave)
                .flatMap(savedUser ->
                        driveService.createUserFolderIfNotExists(savedUser.getEmail())
                                .flatMap(folderId -> {
                                    savedUser.setDriveFolderId(folderId);
                                    savedUser.setCurrentDriveUsageBytes(0L);
                                    return userRepository.save(savedUser);
                                })
                                .map(finalSavedUser -> {
                                    String token = jwtUtility.generateToken7Days(finalSavedUser.getEmail());
                                    Date expirationDate = jwtUtility.extractExpiration(token);
                                    Instant expirationInstant = (expirationDate != null) ? expirationDate.toInstant() : null;

                                    log.info("Signup completed and drive folder created for: {}", finalSavedUser.getEmail());
                                    return SignUpResponse.builder()
                                            .email(finalSavedUser.getEmail()) // Email is already here
                                            .username(finalSavedUser.getUsernameField())
                                            .linkedinUrl(finalSavedUser.getLinkedinProfileUrl())
                                            .message("Your account has been successfully created!")
                                            .token(token)
                                            .expirationTime(expirationInstant)
                                            .currentStorageUsageMb(bytesToMegabytes(finalSavedUser.getCurrentDriveUsageBytes()))
                                            .driveFolderId(finalSavedUser.getDriveFolderId())
                                            .authProvider(finalSavedUser.getAuthProvider() != null ? finalSavedUser.getAuthProvider().name() : null)
                                            .build();
                                })
                                .onErrorResume(driveEx -> {
                                    log.error("CRITICAL: Failed to create Drive folder for user {} during signup. Deleting user from DB to prevent inconsistencies.", savedUser.getEmail(), driveEx);
                                    return userRepository.delete(savedUser)
                                            .then(Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "We couldn't set up your storage. Please try signing up again.")));
                                })
                )
                .onErrorResume(e -> {
                    log.error("An unexpected error occurred during user data persistence for {}: {}", request.getEmail(), e.getMessage(), e);
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during registration. Please try again."));
                });
    }

    /**
     * Handles user login for website-authenticated users.
     * @param request LoginRequest containing user credentials.
     * @return Mono of LoginResponse with JWT, user details, and drive folder ID.
     */
    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        log.warn("Login attempt failed for user {}: Invalid password.", request.getEmail());
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
                    }
                    if (!user.getEmailVerified()) {
                        log.warn("Login attempt failed for user {}: Email not verified.", request.getEmail());
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Your email is not verified. Please complete the signup process."));
                    }
                    if (user.getAuthProvider() != User.AuthProvider.WEBSITE) {
                        log.warn("Login attempt failed for user {}: Account not website-authenticated.", request.getEmail());
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "This account was not registered with an email and password. Please use your original login method (e.g., Google, LinkedIn)."));
                    }

                    user.setLastLogin(LocalDateTime.now());
                    return userRepository.save(user)
                            .map(savedUser -> {
                                String token = jwtUtility.generateToken7Days(savedUser.getEmail());
                                Date expirationDate = jwtUtility.extractExpiration(token);
                                Instant expirationInstant = (expirationDate != null) ? expirationDate.toInstant() : null;

                                log.info("User logged in successfully: {}", savedUser.getEmail());
                                return LoginResponse.builder()
                                        .email(savedUser.getEmail()) // <--- ADDED THIS LINE!
                                        .token(token)
                                        .expirationTime(expirationInstant)
                                        .username(savedUser.getUsernameField())
                                        .message("Login successful!")
                                        .currentStorageUsageMb(bytesToMegabytes(savedUser.getCurrentDriveUsageBytes()))
                                        .driveFolderId(savedUser.getDriveFolderId())
                                        .authProvider(savedUser.getAuthProvider() != null ? savedUser.getAuthProvider().name() : null)
                                        .build();
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Login attempt failed: User not found for email {}.", request.getEmail());
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
                }))
                .onErrorResume(ResponseStatusException.class, Mono::error)
                .onErrorResume(e -> {
                    log.error("An unexpected error occurred during login for user {}: {}", request.getEmail(), e.getMessage(), e);
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during login. Please try again."));
                });
    }


    /**
     * Initiates the forgot password process by sending a reset link to the user's email.
     * The link contains a time-limited JWT token.
     * This method is specifically for WEBSITE auth users.
     * @param email The email of the user requesting a password reset.
     * @param resetLinkBase The base URL for the password reset page.
     * @return Mono<ResponseEntity<String>> indicating success or failure with appropriate HTTP status.
     */
    public Mono<ResponseEntity<String>> forgotPassword(String email, String resetLinkBase) {
        log.debug("Forgot password service initiated for email: {}", email);
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (user.getAuthProvider() != User.AuthProvider.WEBSITE) {
                        log.warn("Password reset requested for non-WEBSITE user: {}. Preventing email for security.", email);
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("We couldn't find an account associated with this email."));
                    }

                    String resetToken = jwtUtility.generateToken30Minutes(user.getEmail());
                    Date expirationDate = jwtUtility.extractExpiration(resetToken);
                    Instant tokenExpirationTime = (expirationDate != null) ? expirationDate.toInstant() : null;

                    JwtExpiredToken jwtTokenRecord = new JwtExpiredToken();
                    jwtTokenRecord.setToken(resetToken);
                    jwtTokenRecord.setExpirationTime(tokenExpirationTime);
                    jwtTokenRecord.setUsed(false);
                    jwtTokenRecord.setIssuedAt(Instant.now());

                    return jwtExpiredTokenRepository.save(jwtTokenRecord)
                            .flatMap(savedTokenRecord -> {
                                String fullResetLink = resetLinkBase + "/" + savedTokenRecord.getToken();
                                log.info("Generated password reset link for {}.", user.getEmail());
                                try {
                                    emailService.sendPasswordResetEmail(user.getEmail(), fullResetLink);
                                    log.info("Password reset link email sent to {}", user.getEmail());
                                    // *** CHANGE START *** Return JSON for success
                                    return Mono.just(ResponseEntity.ok("{\"status\": \"success\", \"message\": \"A password reset link has been sent to your email. Please check your inbox and spam folder.\"}"));
                                    // *** CHANGE END ***
                                } catch (MessagingException e) {
                                    log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage(), e);
                                    // *** CHANGE START *** Return JSON for error
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"status\": \"error\", \"message\": \"We couldn't send the password reset email. Please try again.\"}"));
                                    // *** CHANGE END ***
                                }
                            })
                            .onErrorResume(dbEx -> {
                                log.error("Failed to save password reset token to DB for {}: {}", user.getEmail(), dbEx.getMessage(), dbEx);
                                // *** CHANGE START *** Return JSON for error
                                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"status\": \"error\", \"message\": \"We encountered an issue initiating your password reset. Please try again.\"}"));
                                // *** CHANGE END ***
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Forgot password requested for non-existent email: {}", email);
                    // *** CHANGE START *** Return JSON for not found
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"status\": \"error\", \"message\": \"We couldn't find an account associated with this email.\"}"));
                    // *** CHANGE END ***
                }))
                .onErrorResume(e -> {
                    log.error("An unexpected error occurred during forgot password for email {}: {}", email, e.getMessage(), e);
                    // *** CHANGE START *** Return JSON for unexpected error
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"status\": \"error\", \"message\": \"An unexpected error occurred. Please try again.\"}"));
                    // *** CHANGE END ***
                });
    }

    /**
     * Resets the user's password using a valid, unused JWT token.
     * This method is specifically for WEBSITE auth users.
     * @param token The JWT token string.
     * @param newPassword The new password provided by the user.
     * @return Mono<ResponseEntity<String>> indicating success or a detailed error message with appropriate HTTP status.
     */
    public Mono<ResponseEntity<String>> resetPassword(String token, String newPassword) {
        log.debug("Reset password service initiated for token: {}", token);

        return jwtExpiredTokenRepository.findByToken(token)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password reset link is invalid or has expired. Please request a new one.")))
                .flatMap(tokenRecord -> {
                    if (tokenRecord.isUsed()) {
                        log.warn("Attempt to use an already consumed password reset token: {}", token);
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "This password reset link has already been used. Please request a new one."));
                    }

                    String userEmail = jwtUtility.extractUsername(token);
                    if (userEmail == null || userEmail.isEmpty()) {
                        log.error("User email could not be extracted from JWT during password reset: {}", token);
                        // IMPORTANT: Do NOT mark as used here. If the token is intrinsically malformed,
                        // it was never valid in the first place, and marking it used would prevent retries if
                        // there was an issue with the link itself. It should only be marked used on a successful password change.
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password reset link is invalid. Please request a new one."));
                    }

                    return userRepository.findByEmail(userEmail)
                            .switchIfEmpty(Mono.defer(() -> {
                                log.warn("User not found for email '{}' extracted from token {}. Not marking token as used.", userEmail, token);
                                // IMPORTANT: Do NOT mark as used here. If the user doesn't exist, the link cannot complete,
                                // but marking it used would imply it was successfully "consumed".
                                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "The account associated with this reset link could not be found."));
                            }))
                            .flatMap(user -> {
                                // Important: JWT validity check should consider the actual JWT expiry too
                                // even if the DB record hasn't caught up.
                                if (!jwtUtility.validateToken(token, user)) {
                                    log.warn("JWT token is intrinsically invalid or expired for user {}: {}", userEmail, token);
                                    // IMPORTANT: Do NOT mark as used here. Intrinsic JWT invalidity means it's not a valid token to begin with.
                                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password reset link has expired or is invalid. Please request a new one."));
                                }

                                if (Instant.now().isAfter(tokenRecord.getExpirationTime())) {
                                    log.warn("Password reset token found in DB but is past its recorded expiration: {}", token);
                                    // IMPORTANT: Do NOT mark as used here. It's truly expired.
                                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password reset link has expired. Please request a new one."));
                                }

                                if (user.getAuthProvider() != User.AuthProvider.WEBSITE) {
                                    log.warn("Password reset attempted for non-WEBSITE user through link: {}", userEmail);
                                    // IMPORTANT: Do NOT mark as used here. This link is not for this auth type.
                                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "This reset link is not for accounts created with email and password. Please use your original login method."));
                                }

                                user.setPassword(passwordEncoder.encode(newPassword));
                                user.setLastLogin(LocalDateTime.now());

                                return userRepository.save(user)
                                        .flatMap(savedUser -> {
                                            // *** CRITICAL CHANGE HERE: Mark token as used ONLY after successful password update ***
                                            tokenRecord.setUsed(true);
                                            log.info("Password successfully reset for user: {}. Marking token as used.", savedUser.getEmail());
                                            return jwtExpiredTokenRepository.save(tokenRecord)
                                                    // *** CRITICAL CHANGE HERE: Return JSON response on success ***
                                                    .thenReturn(ResponseEntity.ok("{\"status\": \"success\", \"message\": \"Your password has been successfully reset!\"}"));
                                        });
                            });
                })
                .onErrorResume(ResponseStatusException.class, e -> {
                    log.error("Password reset failed for token {}: {}", token, e.getReason());
                    // *** CHANGE START *** Ensure all error responses are JSON
                    return Mono.just(ResponseEntity.status(e.getStatusCode()).body("{\"status\": \"error\", \"message\": \"" + e.getReason() + "\"}"));
                    // *** CHANGE END ***
                })
                .onErrorResume(e -> {
                    log.error("An unexpected error occurred during password reset for token {}: {}", token, e.getMessage(), e);
                    // *** CHANGE START *** Ensure all error responses are JSON
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"status\": \"error\", \"message\": \"An unexpected error occurred. Please try again.\"}"));
                    // *** CHANGE END ***
                });
    }

    private String generateOtp() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }
}