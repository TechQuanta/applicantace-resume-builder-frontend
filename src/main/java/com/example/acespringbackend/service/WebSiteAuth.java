package com.example.acespringbackend.service;

import com.example.acespringbackend.auth.dto.*;
import com.example.acespringbackend.drive.DriveService;
import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;

import com.example.acespringbackend.utility.JwtUtility;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSiteAuth {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final DriveService driveService;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    private final JwtUtility jwtUtility;

    public WebSiteAuth(EmailService emailService,
                       UserRepository userRepository,
                       DriveService driveService,
                       PasswordEncoder passwordEncoder,
                       JwtUtility jwtUtility) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.driveService = driveService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtility = jwtUtility;
    }


    // Step 1: Generate and send OTP email
    public Mono<SignUpResponse> sendOtpForSignup(SignUpRequest request) {
        String otp = generateOtp();
        otpStore.put(request.getEmail(), otp);

        try {
            emailService.sendOtpEmail(request.getEmail(), otp);
            return Mono.just(new SignUpResponse(request.getEmail(), "OTP sent to email"));
        } catch (Exception e) {
            return Mono.just(new SignUpResponse(request.getEmail(), "Failed to send OTP email"));
        }
    }

    // Step 2: Verify OTP correctness
    public Mono<SignUpResponse> verifyOtpAndSignup(OtpVerificationRequest request) {
        String savedOtp = otpStore.get(request.getEmail());
        if (savedOtp != null && savedOtp.equals(request.getOtp())) {
            otpStore.remove(request.getEmail());
            return Mono.just(new SignUpResponse(request.getEmail(), "OTP verified"));
        } else {
            return Mono.just(new SignUpResponse(request.getEmail(), "Invalid or expired OTP"));
        }
    }

    // Step 3: Complete signup and create Drive folder
    public Mono<SignUpResponse> completeSignup(SignUpRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existingUser ->
                        Mono.just(new SignUpResponse(request.getEmail(), "User already exists"))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID().toString());
                    newUser.setUsername(request.getUsername());
                    newUser.setEmail(request.getEmail());
                    newUser.setPassword(passwordEncoder.encode(request.getPassword()));
                    newUser.setEmailVerified(false);
                    newUser.setAuthProvider(User.AuthProvider.WEBSITE);
                    newUser.setSignInProvider("email/password");
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setLastLogin(LocalDateTime.now());

                    return userRepository.save(newUser)
                            .flatMap(savedUser ->
                                    driveService.createFolderForUser(savedUser)
                                            .flatMap(folderId -> {
                                                savedUser.setDriveFolderId(folderId);
                                                return userRepository.save(savedUser);
                                            })
                            )
                            .map(savedUser -> {
                                String token = jwtUtility.generateToken(savedUser.getEmail());
                                return new SignUpResponse(savedUser.getEmail(), "Signup successful", token);
                            });
                }));
    }


    // Login
    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        String token = jwtUtility.generateToken(user.getEmail());
                        return Mono.just(new LoginResponse(token, user.getEmail()));
                    } else {
                        return Mono.just(new LoginResponse(null, "Invalid password"));
                    }
                })
                .switchIfEmpty(Mono.just(new LoginResponse(null, "User not found")));
    }


    private String generateOtp() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }
}
