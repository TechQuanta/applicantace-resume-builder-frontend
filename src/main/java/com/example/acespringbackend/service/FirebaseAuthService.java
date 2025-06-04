package com.example.acespringbackend.service;

import com.example.acespringbackend.auth.dto.GoogleAuthRequest;
import com.example.acespringbackend.auth.dto.GoogleAuthResponse;
import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.utility.JwtUtility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;  // Inject JwtUtility for JWT token creation

    public FirebaseAuthService(FirebaseAuth firebaseAuth, UserRepository userRepository, JwtUtility jwtUtility) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
    }

    public Mono<GoogleAuthResponse> authenticate(GoogleAuthRequest request) {
        String idToken = request.getIdToken();
        if (idToken == null || idToken.isEmpty()) {
            return Mono.error(new IllegalArgumentException("ID token is missing."));
        }

        return Mono.fromCallable(() -> firebaseAuth.verifyIdToken(idToken))
                .flatMap(firebaseToken -> {
                    String uid = firebaseToken.getUid();
                    String email = firebaseToken.getEmail();
                    boolean emailVerified = firebaseToken.isEmailVerified();
                    String name = firebaseToken.getName();
                    String picture = firebaseToken.getPicture();

                    System.out.println("--- Firebase Token Details ---");
                    System.out.println("UID: " + uid);
                    System.out.println("Email: " + email + " (Verified: " + emailVerified + ")");
                    System.out.println("Name: " + name);
                    System.out.println("Picture URL: " + picture);
                    System.out.println("Firebase Sign-in Provider (Fixed): google.com");
                    System.out.println("------------------------------");

                    return userRepository.findByEmail(email)
                            .flatMap(existingUser -> {
                                existingUser.setFirebaseIdToken(idToken);
                                existingUser.setImageUrl(picture);
                                existingUser.setAuthProvider(User.AuthProvider.GOOGLE);
                                existingUser.setSignInProvider("google.com");
                                existingUser.setEmailVerified(emailVerified);
                                existingUser.setLastLogin(LocalDateTime.now());

                                if (name != null && !name.isEmpty() && !name.equals(existingUser.getUsername())) {
                                    existingUser.setUsername(name);
                                }

                                return userRepository.save(existingUser);
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                User newUser = new User();
                                newUser.setId(uid);
                                newUser.setEmail(email);
                                newUser.setEmailVerified(emailVerified);
                                newUser.setImageUrl(picture);
                                newUser.setFirebaseIdToken(idToken);
                                newUser.setAuthProvider(User.AuthProvider.GOOGLE);
                                newUser.setSignInProvider("google.com");

                                String finalUsername = (name != null && !name.isEmpty())
                                        ? name.toLowerCase().replaceAll("\\s+", "")
                                        : email.split("@")[0].toLowerCase();
                                newUser.setUsername(finalUsername);

                                LocalDateTime now = LocalDateTime.now();
                                newUser.setCreatedAt(now);
                                newUser.setLastLogin(now);

                                return userRepository.save(newUser);
                            }))
                            .map(user -> {
                                // Generate your own JWT token here using email or username as subject
                                String appJwtToken = jwtUtility.generateToken(user.getEmail());

                                return new GoogleAuthResponse(
                                        appJwtToken,       // <-- Return your app's JWT token here
                                        user.getEmail(),
                                        user.getUsername(),
                                        user.getImageUrl(),
                                        user.getAuthProvider().name()
                                );
                            });
                })
                .onErrorMap(FirebaseAuthException.class, e -> {
                    System.err.println("Firebase Auth Exception: " + e.getMessage());
                    return new RuntimeException("Firebase token verification failed: " + e.getMessage(), e);
                })
                .onErrorMap(Exception.class, e -> {
                    System.err.println("General Exception during authentication: " + e.getMessage());
                    return new RuntimeException("Authentication failed: " + e.getMessage(), e);
                });
    }
}
