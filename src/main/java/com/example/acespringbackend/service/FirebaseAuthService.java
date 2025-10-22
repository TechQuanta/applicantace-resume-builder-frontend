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
import java.time.Instant;
import java.util.Date;

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

    private double bytesToMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
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
                    String name = firebaseToken.getName(); // This is Google's display name
                    String picture = firebaseToken.getPicture();

                    System.out.println("--- Firebase Token Details ---");
                    System.out.println("UID: " + uid);
                    System.out.println("Email: " + email + " (Verified: " + emailVerified + ")");
                    System.out.println("Name (from Firebase): " + name); // Log actual name from Firebase
                    System.out.println("Picture URL: " + picture);
                    System.out.println("Firebase Sign-in Provider (Fixed): google.com");
                    System.out.println("------------------------------");

                    return userRepository.findByEmail(email)
                            .flatMap(existingUser -> {
                                // User already exists, update details
                                existingUser.setFirebaseIdToken(idToken);
                                existingUser.setImageUrl(picture);
                                existingUser.setAuthProvider(User.AuthProvider.GOOGLE);
                                existingUser.setSignInProvider("google.com");
                                existingUser.setEmailVerified(emailVerified);
                                existingUser.setLastLogin(LocalDateTime.now());

                                // --- START: REFINED USERNAME LOGIC (EXISTING USER) ---
                                String newUsernameForExisting;
                                // If name exists AND is not empty AND does NOT contain '@' (i.e., it's not an email itself)
                                if (name != null && !name.isEmpty() && !name.contains("@")) {
                                    newUsernameForExisting = name.toLowerCase().replaceAll("\\s+", "");
                                    System.out.println("DEBUG: Name from Firebase ('" + name + "') is clean. Calculated newUsernameForExisting: " + newUsernameForExisting);
                                } else {
                                    // Otherwise (name is null/empty OR it's an email itself), use the email prefix.
                                    newUsernameForExisting = email.split("@")[0].toLowerCase();
                                    System.out.println("DEBUG: Name from Firebase ('" + name + "') is not clean. Calculated newUsernameForExisting (from email prefix): " + newUsernameForExisting);
                                }

                                System.out.println("DEBUG: Current existingUser.getUsername() (UserDetails) BEFORE update: " + existingUser.getUsername());
                                System.out.println("DEBUG: Current existingUser.getUsernameField() (User model field) BEFORE update: " + existingUser.getUsernameField());

                                // The comparison should be against the actual 'username' field, not the UserDetails 'getUsername()'
                                if (!newUsernameForExisting.equals(existingUser.getUsernameField())) {
                                    existingUser.setUsernameField(newUsernameForExisting);
                                    System.out.println("DEBUG: existingUser.setUsernameField() called with: " + newUsernameForExisting);
                                } else {
                                    System.out.println("DEBUG: newUsernameForExisting is same as current usernameField, no update needed.");
                                }

                                System.out.println("DEBUG: existingUser.getUsername() (UserDetails) AFTER potential setUsernameField: " + existingUser.getUsername());
                                System.out.println("DEBUG: existingUser.getUsernameField() (User model field) AFTER potential setUsernameField: " + existingUser.getUsernameField());
                                // --- END: REFINED USERNAME LOGIC (EXISTING USER) ---

                                // Generate the JWT using the 7-day expiration method
                                String appJwtToken = jwtUtility.generateToken7Days(existingUser.getEmail());
                                existingUser.setAccessToken(appJwtToken);

                                Mono<User> userSaveMono;
                                if (existingUser.getDriveFolderId() == null || existingUser.getDriveFolderId().isEmpty()) {
                                    System.out.println("Existing user " + email + " found without Drive folder. Creating folder...");
                                    userSaveMono = driveService.createUserFolderIfNotExists(existingUser.getEmail())
                                            .flatMap(folderId -> {
                                                existingUser.setDriveFolderId(folderId);
                                                existingUser.setCurrentDriveUsageBytes(0L);
                                                return userRepository.save(existingUser); // Save after drive folder set
                                            })
                                            .onErrorResume(driveEx -> {
                                                System.err.println("CRITICAL: Failed to create Drive folder for existing Google user " + existingUser.getEmail() + ". Proceeding without folder link. Error: " + driveEx.getMessage());
                                                return userRepository.save(existingUser); // Save even if drive folder creation fails
                                            });
                                } else {
                                    System.out.println("Existing user " + email + " found with existing Drive folder. Updating details...");
                                    userSaveMono = userRepository.save(existingUser); // This is where the update is saved
                                }
                                return userSaveMono; // Return the Mono<User> that results from saving
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
                                newUser.setCurrentDriveUsageBytes(0L);

                                // --- START: REFINED USERNAME LOGIC (NEW USER) ---
                                String finalUsername;
                                // If name exists AND is not empty AND does NOT contain '@' (i.e., it's not an email itself)
                                if (name != null && !name.isEmpty() && !name.contains("@")) {
                                    finalUsername = name.toLowerCase().replaceAll("\\s+", "");
                                    System.out.println("DEBUG (NEW USER): Name from Firebase ('" + name + "') is clean. Calculated finalUsername: " + finalUsername);
                                } else {
                                    // Otherwise (name is null/empty OR it's an email itself), use the email prefix.
                                    finalUsername = email.split("@")[0].toLowerCase();
                                    System.out.println("DEBUG (NEW USER): Name from Firebase ('" + name + "') is not clean. Calculated finalUsername (from email prefix): " + finalUsername);
                                }
                                newUser.setUsernameField(finalUsername);
                                System.out.println("DEBUG (NEW USER): newUser.getUsername() (UserDetails) after setUsernameField: " + newUser.getUsername());
                                System.out.println("DEBUG (NEW USER): newUser.getUsernameField() (User model field) after setUsernameField: " + newUser.getUsernameField());
                                // --- END: REFINED USERNAME LOGIC (NEW USER) ---

                                LocalDateTime now = LocalDateTime.now();
                                newUser.setCreatedAt(now);
                                newUser.setLastLogin(now);

                                // Generate the JWT for new user
                                String appJwtToken = jwtUtility.generateToken7Days(newUser.getEmail());
                                newUser.setAccessToken(appJwtToken);

                                return userRepository.save(newUser) // Save the new user
                                        .flatMap(savedUser ->
                                                driveService.createUserFolderIfNotExists(savedUser.getEmail())
                                                        .flatMap(folderId -> {
                                                            savedUser.setDriveFolderId(folderId);
                                                            savedUser.setCurrentDriveUsageBytes(0L);
                                                            return userRepository.save(savedUser); // Save again if drive folder added
                                                        })
                                                        .onErrorResume(driveEx -> {
                                                            System.err.println("CRITICAL: Failed to create Drive folder for new Google user " + savedUser.getEmail() + ". Deleting user from DB. Error: " + driveEx.getMessage());
                                                            return userRepository.delete(savedUser)
                                                                    .then(Mono.error(new RuntimeException("Google Signup failed: Could not create Drive folder for user.", driveEx)));
                                                        })
                                        );
                            }))
                            .map(user -> { // This 'user' object is the result of the last flatMap (a saved User entity)
                                String appJwtToken = user.getAccessToken();
                                Date finalExpirationDate = jwtUtility.extractExpiration(appJwtToken);
                                Instant finalExpirationInstant = (finalExpirationDate != null) ? finalExpirationDate.toInstant() : null;

                                // --- Crucial Debugging Lines for final output ---
                                System.out.println("DEBUG: Username in GoogleAuthResponse before sending to frontend (using getUsernameField()): " + user.getUsernameField());
                                System.out.println("DEBUG: Email in GoogleAuthResponse before sending to frontend (using getEmail()): " + user.getEmail());
                                // --- End Crucial Debugging Lines ---

                                return new GoogleAuthResponse(
                                        appJwtToken,
                                        finalExpirationInstant,
                                        user.getEmail(),
                                        user.getUsernameField(), // <<<--- **THIS IS THE KEY CHANGE!**
                                        user.getImageUrl(),
                                        user.getAuthProvider().name(),
                                        user.getDriveFolderId(),
                                        bytesToMegabytes(user.getCurrentDriveUsageBytes())
                                );
                            });
                })
                .onErrorMap(FirebaseAuthException.class, e -> {
                    System.err.println("Firebase Auth Exception: " + e.getMessage());
                    return new RuntimeException("Firebase token verification failed: " + e.getMessage(), e);
                })
                .onErrorMap(Exception.class, e -> {
                    System.err.println("General Exception during authentication: " + e.getMessage());
                    e.printStackTrace();
                    return new RuntimeException("Authentication failed: " + e.getMessage(), e);
                });
    }
}