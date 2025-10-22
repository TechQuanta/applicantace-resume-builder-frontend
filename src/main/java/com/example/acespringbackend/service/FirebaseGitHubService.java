//package com.example.acespringbackend.service;
//
//import com.example.acespringbackend.auth.dto.GithubResponse;
//import com.example.acespringbackend.auth.dto.GithubRequest;
//import com.example.acespringbackend.utility.JwtUtility;
//import com.example.acespringbackend.model.User;
//import com.example.acespringbackend.repository.UserRepository;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseAuthException;
//import com.google.firebase.auth.FirebaseToken;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class FirebaseGitHubService {
//
//    private static final Logger logger = LoggerFactory.getLogger(FirebaseGitHubService.class);
//
//    private final FirebaseAuth firebaseAuth;
//    private final UserRepository userRepository;
//    private final JwtUtility jwtUtility;
//    private final DriveService driveService; // Inject DriveService
//
//    public FirebaseGitHubService(FirebaseAuth firebaseAuth, UserRepository userRepository, JwtUtility jwtUtility, DriveService driveService) {
//        this.firebaseAuth = firebaseAuth;
//        this.userRepository = userRepository;
//        this.jwtUtility = jwtUtility;
//        this.driveService = driveService; // Assign it
//    }
//
//    /**
//     * Authenticates a user with a Firebase ID token obtained from GitHub Sign-In.
//     * It either updates an existing user's details or creates a new user,
//     * ensuring a dedicated Google Drive folder is created and its ID stored.
//     *
//     * @param request GithubRequest containing the Firebase ID Token.
//     * @return Mono of GithubResponse containing JWT, user details, and drive folder ID.
//     */
//    public Mono<GithubResponse> authenticate(GithubRequest request) {
//        String idToken = request.getIdToken();
//        if (idToken == null || idToken.isEmpty()) {
//            return Mono.error(new IllegalArgumentException("ID token is missing."));
//        }
//
//        return Mono.fromCallable(() -> firebaseAuth.verifyIdToken(idToken))
//                .flatMap(firebaseToken -> {
//                    String uid = firebaseToken.getUid();
//                    String email = firebaseToken.getEmail();
//                    boolean emailVerified = firebaseToken.isEmailVerified();
//                    String name = firebaseToken.getName();
//                    String picture = firebaseToken.getPicture();
//
//                    logger.debug("FirebaseToken received for UID: {}", uid);
//                    logger.debug("Email from FirebaseToken: {}", email);
//                    logger.debug("Name from FirebaseToken: {}", name);
//                    logger.debug("Picture from FirebaseToken: {}", picture);
//
//                    // Ensure auth provider is GITHUB for this service
//                    String firebaseSignInProvider = User.AuthProvider.GITHUB.name();
//
//                    logger.info("Firebase GitHub token verified for UID: {}", uid);
//                    logger.debug("Email: {}, Name: {}, Picture: {}, Provider (Internal): {}", email, name, picture, firebaseSignInProvider);
//
//                    if (email == null || email.isEmpty()) {
//                        email = uid + "@github.firebaseuser.com"; // Fallback email
//                        logger.warn("GitHub user {} did not provide an email. Using derived email: {}", uid, email);
//                    }
//                    final String finalEmail = email; // Make effectively final
//
//                    String githubId = null;
//                    if (firebaseToken.getClaims() != null) {
//                        try {
//                            Map<String, Object> firebaseClaims = (Map<String, Object>) firebaseToken.getClaims().get("firebase");
//                            if (firebaseClaims != null) {
//                                Map<String, Object> identities = (Map<String, Object>) firebaseClaims.get("identities");
//                                if (identities != null && identities.containsKey("github.com")) {
//                                    List<String> githubUids = (List<String>) identities.get("github.com");
//                                    if (githubUids != null && !githubUids.isEmpty()) {
//                                        githubId = githubUids.get(0);
//                                        logger.debug("Extracted GitHub ID from claims: {}", githubId);
//                                    }
//                                }
//                            }
//                        } catch (ClassCastException e) {
//                            logger.warn("Could not cast Firebase claims for GitHub identities: {}", e.getMessage(), e);
//                        }
//                    }
//                    final String finalGithubId = githubId; // Make effectively final
//
//                    // These fields remain null as they are not reliably provided by FirebaseToken by default
//                    // or are not directly provided in the initial GitHub OAuth flow via Firebase
//                    String githubLogin = null;
//                    String githubHtmlUrl = null;
//                    String githubProfileUrl = null;
//                    String githubCompany = null;
//                    String githubLocation = null;
//                    String githubBio = null;
//                    Integer githubPublicRepos = null;
//                    Integer githubFollowers = null;
//                    Integer githubFollowing = null;
//
//
//                    return userRepository.findByEmail(finalEmail)
//                            .flatMap(existingUser -> {
//                                logger.info("Found existing user with email: {}", existingUser.getEmail());
//                                existingUser.setFirebaseIdToken(idToken);
//                                existingUser.setImageUrl(picture);
//                                existingUser.setAuthProvider(User.AuthProvider.GITHUB); // Explicitly GITHUB
//                                existingUser.setSignInProvider(firebaseSignInProvider);
//                                existingUser.setEmailVerified(emailVerified);
//                                existingUser.setLastLogin(LocalDateTime.now());
//
//                                // Ensure Google-specific fields are NOT set or are cleared for GitHub users
//                                existingUser.setAccessToken(null); // IMPORTANT: Clear or ensure this is null for GitHub users
//
//                                String usernameFromEmail = (finalEmail != null && finalEmail.contains("@")) ? finalEmail.split("@")[0] : null;
//                                if (name != null && !name.isEmpty() && (existingUser.getUsername() == null || existingUser.getUsername().isEmpty() || (usernameFromEmail != null && existingUser.getUsername().equals(usernameFromEmail)))) {
//                                    existingUser.setUsername(name);
//                                    logger.debug("Updated existing user username to: {}", name);
//                                }
//
//                                existingUser.setGithubId(finalGithubId);
//                                existingUser.setGithubLogin(githubLogin);
//                                existingUser.setGithubHtmlUrl(githubHtmlUrl);
//                                existingUser.setGithubProfileUrl(githubProfileUrl);
//                                existingUser.setGithubCompany(githubCompany);
//                                existingUser.setGithubLocation(githubLocation);
//                                existingUser.setGithubBio(githubBio);
//                                existingUser.setGithubPublicRepos(githubPublicRepos);
//                                existingUser.setGithubFollowers(githubFollowers);
//                                existingUser.setGithubFollowing(githubFollowing);
//
//                                Mono<User> userSaveMono;
//                                // All users (including GitHub) should have a Drive folder
//                                if (existingUser.getDriveFolderId() == null || existingUser.getDriveFolderId().isEmpty()) {
//                                    logger.info("Existing GitHub user {} found without Drive folder. Creating folder...", finalEmail);
//                                    userSaveMono = driveService.createUserFolderIfNotExists(existingUser.getEmail())
//                                            .flatMap(folderId -> {
//                                                logger.debug("Drive folder created for existing user: {}", folderId);
//                                                existingUser.setDriveFolderId(folderId);
//                                                existingUser.setCurrentDriveUsageBytes(0L); // Initialize usage
//                                                return userRepository.save(existingUser);
//                                            })
//                                            .onErrorResume(driveEx -> {
//                                                logger.error("Failed to create Drive folder for existing GitHub user {}. Proceeding without folder link. Error: {}", existingUser.getEmail(), driveEx.getMessage(), driveEx);
//                                                // If Drive folder creation fails, still save the user so they can log in.
//                                                // You might add logic here to re-attempt folder creation later or notify admin.
//                                                return userRepository.save(existingUser);
//                                            });
//                                } else {
//                                    logger.info("Existing GitHub user {} found with existing Drive folder. Updating details...", finalEmail);
//                                    userSaveMono = userRepository.save(existingUser);
//                                }
//                                return userSaveMono;
//                            })
//                            .switchIfEmpty(Mono.defer(() -> {
//                                logger.info("New GitHub user {} detected. Creating user and Drive folder...", finalEmail);
//                                User newUser = new User();
//                                newUser.setId(uid);
//                                newUser.setEmail(finalEmail);
//                                newUser.setEmailVerified(emailVerified);
//                                newUser.setImageUrl(picture);
//                                newUser.setFirebaseIdToken(idToken);
//                                newUser.setAuthProvider(User.AuthProvider.GITHUB); // Explicitly GITHUB
//                                newUser.setSignInProvider(firebaseSignInProvider);
//                                newUser.setCurrentDriveUsageBytes(0L);
//                                newUser.setGithubId(finalGithubId);
//                                newUser.setGithubLogin(githubLogin);
//                                newUser.setGithubHtmlUrl(githubHtmlUrl);
//                                newUser.setGithubProfileUrl(githubProfileUrl);
//                                newUser.setGithubCompany(githubCompany);
//                                newUser.setGithubLocation(githubLocation);
//                                newUser.setGithubBio(githubBio);
//                                newUser.setGithubPublicRepos(githubPublicRepos);
//                                newUser.setGithubFollowers(githubFollowers);
//                                newUser.setGithubFollowing(githubFollowing);
//
//                                // Ensure Google-specific fields are NOT set for new GitHub users
//                                newUser.setAccessToken(null); // IMPORTANT: Ensure this is null
//
//                                String finalUsername = (name != null && !name.isEmpty())
//                                        ? name
//                                        : (finalEmail != null && finalEmail.contains("@") ? finalEmail.split("@")[0] : "github_user_" + uid.substring(0, 8));
//                                newUser.setUsername(finalUsername);
//                                logger.debug("New user username set to: {}", finalUsername);
//
//                                LocalDateTime now = LocalDateTime.now();
//                                newUser.setCreatedAt(now);
//                                newUser.setLastLogin(now);
//
//                                return userRepository.save(newUser)
//                                        .flatMap(savedUser -> {
//                                            logger.debug("New user saved to DB: {}", savedUser.getEmail());
//                                            return driveService.createUserFolderIfNotExists(savedUser.getEmail())
//                                                    .flatMap(folderId -> {
//                                                        logger.debug("Drive folder created for new user: {}", folderId);
//                                                        savedUser.setDriveFolderId(folderId); // Set the folder ID
//                                                        return userRepository.save(savedUser);
//                                                    })
//                                                    .onErrorResume(driveEx -> {
//                                                        logger.error("CRITICAL: Failed to create Drive folder for new GitHub user {}. Deleting user from DB.", savedUser.getEmail(), driveEx.getMessage(), driveEx);
//                                                        // If folder creation fails, you might want to prevent the user from being fully created
//                                                        return userRepository.delete(savedUser)
//                                                                .then(Mono.error(new RuntimeException("GitHub Signup failed: Could not create Drive folder for user.", driveEx)));
//                                                    });
//                                        });
//                            }))
//                            .map(user -> {
//                                String appJwtToken = jwtUtility.generateToken(user.getEmail());
//                                double usageInMb = user.getCurrentDriveUsageBytes() / (1024.0 * 1024.0);
//                                logger.debug("Generated JWT for user: {}", user.getEmail());
//
//                                return GithubResponse.builder() // Using builder for GithubResponse
//                                        .jwtToken(appJwtToken)
//                                        .email(user.getEmail())
//                                        .username(user.getUsername())
//                                        .imageUrl(user.getImageUrl())
//                                        .authProvider(user.getAuthProvider().name()) // Send the authProvider name
//                                        .driveFolderId(user.getDriveFolderId())    // Send the Drive Folder ID
//                                        .currentStorageUsageMb(usageInMb) // Send usage as double
//                                        .build();
//                            });
//                })
//                .onErrorMap(FirebaseAuthException.class, e -> {
//                    logger.error("Firebase Auth Exception during GitHub authentication: {}", e.getMessage(), e);
//                    return new RuntimeException("Firebase token verification failed for GitHub login: " + e.getMessage(), e);
//                })
//                .onErrorMap(Exception.class, e -> {
//                    logger.error("General Exception during GitHub authentication process: {}", e.getMessage(), e);
//                    return new RuntimeException("GitHub authentication failed: " + e.getMessage(), e);
//                });
//    }
//}

package com.example.acespringbackend.service;

import com.example.acespringbackend.auth.dto.GithubResponse;
import com.example.acespringbackend.auth.dto.GithubRequest;
import com.example.acespringbackend.utility.JwtUtility;
import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.Instant; // ADDED: Import for Instant
import java.util.Date;   // ADDED: Import for Date
import java.util.List;
import java.util.Map;

@Service
public class FirebaseGitHubService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseGitHubService.class);

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final DriveService driveService;

    public FirebaseGitHubService(FirebaseAuth firebaseAuth, UserRepository userRepository, JwtUtility jwtUtility, DriveService driveService) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
        this.driveService = driveService;
    }

    // Helper method to convert bytes to megabytes for the response DTO
    private double bytesToMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    /**
     * Authenticates a user with a Firebase ID token obtained from GitHub Sign-In.
     * It either updates an existing user's details or creates a new user,
     * ensuring a dedicated Google Drive folder is created and its ID stored.
     *
     * @param request GithubRequest containing the Firebase ID Token.
     * @return Mono of GithubResponse containing JWT, user details, and drive folder ID.
     */
    public Mono<GithubResponse> authenticate(GithubRequest request) {
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

                    logger.debug("FirebaseToken received for UID: {}", uid);
                    logger.debug("Email from FirebaseToken: {}", email);
                    logger.debug("Name from FirebaseToken: {}", name);
                    logger.debug("Picture from FirebaseToken: {}", picture);

                    // Ensure auth provider is GITHUB for this service
                    String firebaseSignInProvider = User.AuthProvider.GITHUB.name();

                    logger.info("Firebase GitHub token verified for UID: {}", uid);
                    logger.debug("Email: {}, Name: {}, Picture: {}, Provider (Internal): {}", email, name, picture, firebaseSignInProvider);

                    if (email == null || email.isEmpty()) {
                        email = uid + "@github.firebaseuser.com"; // Fallback email
                        logger.warn("GitHub user {} did not provide an email. Using derived email: {}", uid, email);
                    }
                    final String finalEmail = email; // Make effectively final

                    String githubId = null;
                    if (firebaseToken.getClaims() != null) {
                        try {
                            Map<String, Object> firebaseClaims = (Map<String, Object>) firebaseToken.getClaims().get("firebase");
                            if (firebaseClaims != null) {
                                Map<String, Object> identities = (Map<String, Object>) firebaseClaims.get("identities");
                                if (identities != null && identities.containsKey("github.com")) {
                                    List<String> githubUids = (List<String>) identities.get("github.com");
                                    if (githubUids != null && !githubUids.isEmpty()) {
                                        githubId = githubUids.get(0);
                                        logger.debug("Extracted GitHub ID from claims: {}", githubId);
                                    }
                                }
                            }
                        } catch (ClassCastException e) {
                            logger.warn("Could not cast Firebase claims for GitHub identities: {}", e.getMessage(), e);
                        }
                    }
                    final String finalGithubId = githubId; // Make effectively final

                    // These fields remain null as they are not reliably provided by FirebaseToken by default
                    // or are not directly provided in the initial GitHub OAuth flow via Firebase
                    String githubLogin = null;
                    String githubHtmlUrl = null;
                    String githubProfileUrl = null;
                    String githubCompany = null;
                    String githubLocation = null;
                    String githubBio = null;
                    Integer githubPublicRepos = null;
                    Integer githubFollowers = null;
                    Integer githubFollowing = null;


                    return userRepository.findByEmail(finalEmail)
                            .flatMap(existingUser -> {
                                logger.info("Found existing user with email: {}", existingUser.getEmail());
                                existingUser.setFirebaseIdToken(idToken);
                                existingUser.setImageUrl(picture);
                                existingUser.setAuthProvider(User.AuthProvider.GITHUB); // Explicitly GITHUB
                                existingUser.setSignInProvider(firebaseSignInProvider);
                                existingUser.setEmailVerified(emailVerified);
                                existingUser.setLastLogin(LocalDateTime.now());

                                String usernameFromEmail = (finalEmail != null && finalEmail.contains("@")) ? finalEmail.split("@")[0] : null;
                                if (name != null && !name.isEmpty() && (existingUser.getUsername() == null || existingUser.getUsername().isEmpty() || (usernameFromEmail != null && existingUser.getUsername().equals(usernameFromEmail)))) {
                                    existingUser.setUsernameField(name);
                                    logger.debug("Updated existing user username to: {}", name);
                                }

                                existingUser.setGithubId(finalGithubId);
                                existingUser.setGithubLogin(githubLogin);
                                existingUser.setGithubHtmlUrl(githubHtmlUrl);
                                existingUser.setGithubProfileUrl(githubProfileUrl);
                                existingUser.setGithubCompany(githubCompany);
                                existingUser.setGithubLocation(githubLocation);
                                existingUser.setGithubBio(githubBio);
                                existingUser.setGithubPublicRepos(githubPublicRepos);
                                existingUser.setGithubFollowers(githubFollowers);
                                existingUser.setGithubFollowing(githubFollowing);

                                // GENERATE and SET the application JWT here for existing user (7-day expiration)
                                String appJwtToken = jwtUtility.generateToken7Days(existingUser.getEmail());
                                existingUser.setAccessToken(appJwtToken);
                                logger.debug("Generated and set JWT for existing user: {}", existingUser.getEmail());

                                Mono<User> userSaveMono;
                                // All users (including GitHub) should have a Drive folder
                                if (existingUser.getDriveFolderId() == null || existingUser.getDriveFolderId().isEmpty()) {
                                    logger.info("Existing GitHub user {} found without Drive folder. Creating folder...", finalEmail);
                                    userSaveMono = driveService.createUserFolderIfNotExists(existingUser.getEmail())
                                            .flatMap(folderId -> {
                                                logger.debug("Drive folder created for existing user: {}", folderId);
                                                existingUser.setDriveFolderId(folderId);
                                                existingUser.setCurrentDriveUsageBytes(0L); // Initialize usage
                                                return userRepository.save(existingUser); // Save after setting folder ID
                                            })
                                            .onErrorResume(driveEx -> {
                                                logger.error("Failed to create Drive folder for existing GitHub user {}. Proceeding without folder link. Error: {}", existingUser.getEmail(), driveEx.getMessage(), driveEx);
                                                return userRepository.save(existingUser); // Still save user even if folder creation fails
                                            });
                                } else {
                                    logger.info("Existing GitHub user {} found with existing Drive folder. Updating details...", finalEmail);
                                    userSaveMono = userRepository.save(existingUser); // Save updates, including new JWT
                                }
                                return userSaveMono;
                            })
                            // Crucial for type inference when switching from Mono.empty() to a new user Mono
                            .<User>switchIfEmpty(Mono.defer(() -> {
                                logger.info("New GitHub user {} detected. Creating user and Drive folder...", finalEmail);
                                User newUser = new User();
                                newUser.setId(uid);
                                newUser.setEmail(finalEmail);
                                newUser.setEmailVerified(emailVerified);
                                newUser.setImageUrl(picture);
                                newUser.setFirebaseIdToken(idToken);
                                newUser.setAuthProvider(User.AuthProvider.GITHUB); // Explicitly GITHUB
                                newUser.setSignInProvider(firebaseSignInProvider);
                                newUser.setCurrentDriveUsageBytes(0L);
                                newUser.setGithubId(finalGithubId);
                                newUser.setGithubLogin(githubLogin);
                                newUser.setGithubHtmlUrl(githubHtmlUrl);
                                newUser.setGithubProfileUrl(githubProfileUrl);
                                newUser.setGithubCompany(githubCompany);
                                newUser.setGithubLocation(githubLocation);
                                newUser.setGithubBio(githubBio);
                                newUser.setGithubPublicRepos(githubPublicRepos);
                                newUser.setGithubFollowers(githubFollowers);
                                newUser.setGithubFollowing(githubFollowing);

                                // Use Math.min to prevent IndexOutOfBoundsException if uid is short
                                String finalUsername = (name != null && !name.isEmpty())
                                        ? name
                                        : (finalEmail != null && finalEmail.contains("@") ? finalEmail.split("@")[0] : "github_user_" + uid.substring(0, Math.min(uid.length(), 8)));
                                newUser.setUsernameField(finalUsername);
                                logger.debug("New user username set to: {}", finalUsername);

                                LocalDateTime now = LocalDateTime.now();
                                newUser.setCreatedAt(now);
                                newUser.setLastLogin(now);

                                // GENERATE and SET the application JWT here for new user (7-day expiration)
                                String appJwtToken = jwtUtility.generateToken7Days(newUser.getEmail());
                                newUser.setAccessToken(appJwtToken);
                                logger.debug("Generated and set JWT for new user: {}", newUser.getEmail());

                                return userRepository.save(newUser) // Save the new user with their JWT
                                        .flatMap(savedUser -> {
                                            logger.debug("New user saved to DB: {}", savedUser.getEmail());
                                            return driveService.createUserFolderIfNotExists(savedUser.getEmail())
                                                    .flatMap(folderId -> {
                                                        logger.debug("Drive folder created for new user: {}", folderId);
                                                        savedUser.setDriveFolderId(folderId); // Set the folder ID
                                                        return userRepository.save(savedUser); // Save again with folder ID
                                                    })
                                                    .onErrorResume(driveEx -> {
                                                        logger.error("CRITICAL: Failed to create Drive folder for new GitHub user {}. Deleting user from DB.", savedUser.getEmail(), driveEx.getMessage(), driveEx);
                                                        // Explicitly cast to Mono<User> to help with type inference
                                                        return userRepository.delete(savedUser)
                                                                .then(Mono.<User>error(new RuntimeException("GitHub Signup failed: Could not create Drive folder for user.", driveEx)));
                                                    });
                                        });
                            }))
                            .map(user -> {
                                // Retrieve the JWT from the user object (it's already stored and available)
                                String appJwtToken = user.getAccessToken();

                                // Extract the expiration time from the 7-day token
                                Date finalExpirationDate = jwtUtility.extractExpiration(appJwtToken);
                                // Defensive check, though should not be null for valid generated JWT
                                Instant finalExpirationInstant = (finalExpirationDate != null) ? finalExpirationDate.toInstant() : null;

                                double usageInMb = bytesToMegabytes(user.getCurrentDriveUsageBytes());
                                logger.debug("Returning GithubResponse for user: {}", user.getEmail());

                                return GithubResponse.builder()
                                        .jwtToken(appJwtToken)
                                        .expirationTime(finalExpirationInstant) // ADDED: Pass the extracted Instant expiration
                                        .email(user.getEmail())
                                        .username(user.getUsername())
                                        .imageUrl(user.getImageUrl())
                                        .authProvider(user.getAuthProvider().name())
                                        .driveFolderId(user.getDriveFolderId())
                                        .currentStorageUsageMb(usageInMb)
                                        .build();
                            });
                })
                .onErrorMap(FirebaseAuthException.class, e -> {
                    logger.error("Firebase Auth Exception during GitHub authentication: {}", e.getMessage(), e);
                    return new RuntimeException("Firebase token verification failed for GitHub login: " + e.getMessage(), e);
                })
                .onErrorMap(Exception.class, e -> {
                    logger.error("General Exception during GitHub authentication process: {}", e.getMessage(), e);
                    return new RuntimeException("GitHub authentication failed: " + e.getMessage(), e);
                });
    }
}