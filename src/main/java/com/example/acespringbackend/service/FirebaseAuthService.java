package com.example.acespringbackend.service;

import com.example.acespringbackend.auth.dto.GoogleAuthRequest;
import com.example.acespringbackend.auth.dto.GoogleAuthResponse;
import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.utility.JwtUtility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;
    private final JwtUtility jwtUtility;
    private final UserRepository userRepository;

    public FirebaseAuthService(FirebaseAuth firebaseAuth, JwtUtility jwtUtility, UserRepository userRepository) {
        this.firebaseAuth = firebaseAuth;
        this.jwtUtility = jwtUtility;
        this.userRepository = userRepository;
    }

    public Mono<GoogleAuthResponse> authenticate(GoogleAuthRequest request) {
        return Mono.fromCallable(() -> firebaseAuth.verifyIdToken(request.getIdToken()))
                .flatMap(decodedToken -> {
                    String email = decodedToken.getEmail();
                    if (email == null || email.isBlank()) {
                        return Mono.error(new IllegalArgumentException("Email is missing in Firebase token"));
                    }

                    return userRepository.findByEmail(email)
                            .switchIfEmpty(Mono.defer(() -> {
                                String name = decodedToken.getName();
                                String picture = decodedToken.getPicture();

                                User newUser = new User(
                                        name,
                                        email,
                                        picture,
                                        request.getAccessToken(),
                                        request.getIdToken(),
                                        User.AuthProvider.GOOGLE
                                );
                                return userRepository.save(newUser);
                            }))
                            .flatMap(user -> {
                                // update access + firebase token if changed
                                user.setAccessToken(request.getAccessToken());
                                user.setFirebaseIdToken(request.getIdToken());
                                return userRepository.save(user);
                            })
                            .map(user -> new GoogleAuthResponse(
                                    jwtUtility.generateToken(user.getEmail()),
                                    user.getEmail(),
                                    user.getUsername(),
                                    user.getImageUrl(),
                                    user.getAuthProvider().name()
                            ));
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("Authentication failed: " + e.getMessage())));
    }
}
