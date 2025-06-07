package com.example.acespringbackend.service;

import com.example.acespringbackend.auth.dto.GoogleAuthRequest;
import com.example.acespringbackend.auth.dto.GoogleAuthResponse;
import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.utility.JwtUtility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final DriveService driveService;

    public FirebaseAuthService(FirebaseAuth firebaseAuth, UserRepository userRepository, JwtUtility jwtUtility, DriveService driveService) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
        this.driveService = driveService;
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
                                // User already exists, update details and potentially create folder if not already linked
                                existingUser.setFirebaseIdToken(idToken);
                                existingUser.setImageUrl(picture);
                                existingUser.setAuthProvider(User.AuthProvider.GOOGLE);
                                existingUser.setSignInProvider("google.com");
                                existingUser.setEmailVerified(emailVerified);
                                existingUser.setLastLogin(LocalDateTime.now());

                                if (name != null && !name.isEmpty() && !name.equals(existingUser.getUsername())) {
                                    existingUser.setUsername(name);
                                }

                                Mono<User> userSaveMono;
                                // Check if the user already has a Drive folder ID
                                if (existingUser.getDriveFolderId() == null || existingUser.getDriveFolderId().isEmpty()) {
                                    // User exists but no Drive folder linked, create one
                                    System.out.println("Existing user " + email + " found without Drive folder. Creating folder...");
                                    userSaveMono = driveService.createUserFolderIfNotExists(existingUser.getEmail())
                                            .flatMap(folderId -> {
                                                existingUser.setDriveFolderId(folderId);
                                                existingUser.setCurrentDriveUsageBytes(0L); // Initialize storage if not already
                                                return userRepository.save(existingUser);
                                            })
                                            .onErrorResume(driveEx -> {
                                                System.err.println("CRITICAL: Failed to create Drive folder for existing Google user " + existingUser.getEmail() + ". Proceeding without folder link. Error: " + driveEx.getMessage());
                                                return userRepository.save(existingUser);
                                            });
                                } else {
                                    // User exists and already has a Drive folder linked, just save updated details
                                    System.out.println("Existing user " + email + " found with existing Drive folder. Updating details...");
                                    userSaveMono = userRepository.save(existingUser);
                                }
                                return userSaveMono;
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                // New user, create user record and then create Drive folder
                                System.out.println("New Google user " + email + " detected. Creating user and Drive folder...");
                                User newUser = new User();
                                newUser.setId(uid);
                                newUser.setEmail(email);
                                newUser.setEmailVerified(emailVerified);
                                newUser.setImageUrl(picture);
                                newUser.setFirebaseIdToken(idToken);
                                newUser.setAuthProvider(User.AuthProvider.GOOGLE);
                                newUser.setSignInProvider("google.com");
                                newUser.setCurrentDriveUsageBytes(0L); // Initialize drive usage for new users

                                String finalUsername = (name != null && !name.isEmpty())
                                        ? name.toLowerCase().replaceAll("\\s+", "")
                                        : email.split("@")[0].toLowerCase();
                                newUser.setUsername(finalUsername);

                                LocalDateTime now = LocalDateTime.now();
                                newUser.setCreatedAt(now);
                                newUser.setLastLogin(now);

                                return userRepository.save(newUser)
                                        .flatMap(savedUser ->
                                                driveService.createUserFolderIfNotExists(savedUser.getEmail()) // Create folder
                                                        .flatMap(folderId -> {
                                                            savedUser.setDriveFolderId(folderId);
                                                            return userRepository.save(savedUser); // Save user with folder ID
                                                        })
                                                        .onErrorResume(driveEx -> {
                                                            System.err.println("CRITICAL: Failed to create Drive folder for new Google user " + savedUser.getEmail() + ". Deleting user from DB. Error: " + driveEx.getMessage());
                                                            return userRepository.delete(savedUser)
                                                                    .then(Mono.error(new RuntimeException("Google Signup failed: Could not create Drive folder for user.", driveEx)));
                                                        })
                                        );
                            }))
                            .map(user -> {
                                String appJwtToken = jwtUtility.generateToken(user.getEmail());

                                return new GoogleAuthResponse(
                                        appJwtToken,
                                        user.getEmail(),
                                        user.getUsername(),
                                        user.getImageUrl(),
                                        user.getAuthProvider().name(),
                                        // CORRECTED: Removed != null check as getCurrentDriveUsageBytes() returns primitive long
                                        user.getCurrentDriveUsageBytes() / (1024.0 * 1024.0)
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