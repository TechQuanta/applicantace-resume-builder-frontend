package com.example.acespringbackend.service;

import com.example.acespringbackend.auth.dto.LoginRequest;
import com.example.acespringbackend.auth.dto.LoginResponse;
import com.example.acespringbackend.auth.dto.SignUpRequest;
import com.example.acespringbackend.auth.dto.SignUpResponse;
import com.example.applicantace.model.User;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.utility.JwtUtility;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class WebSiteAuth {

    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final PasswordEncoder passwordEncoder;

    public WebSiteAuth(UserRepository userRepository, JwtUtility jwtUtility, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<SignUpResponse> signup(SignUpRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existingUser -> Mono.just(new SignUpResponse(null, "Email already registered")))
                .switchIfEmpty(
                        Mono.defer(() -> {
                            String encodedPassword = passwordEncoder.encode(request.getPassword());
                            User newUser = new User(request.getUsername(), request.getEmail(), encodedPassword, User.AuthProvider.WEBSITE);
                            return userRepository.save(newUser)
                                    .map(user -> new SignUpResponse(user.getEmail(), "Signup successful"));
                        })
                );
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        if (user.getAuthProvider() == null || user.getAuthProvider() != User.AuthProvider.WEBSITE) {
                            user.setAuthProvider(User.AuthProvider.WEBSITE);
                            userRepository.save(user).subscribe();
                        }
                        String jwtToken = jwtUtility.generateToken(user.getUsername());
                        return Mono.just(new LoginResponse(jwtToken, user.getUsername()));
                    } else {
                        return Mono.just(new LoginResponse(null, "Invalid email or password"));
                    }
                })
                .switchIfEmpty(Mono.just(new LoginResponse(null, "Invalid email or password")));
    }
}