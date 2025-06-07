// src/main/java/com/example/acespringbackend/service/FirebaseGitHubService.java
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
import java.util.List;
import java.util.Map;

@Service
public class FirebaseGitHubService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseGitHubService.class);

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final DriveService driveService; // Inject DriveService

    public FirebaseGitHubService(FirebaseAuth firebaseAuth, UserRepository userRepository, JwtUtility jwtUtility, DriveService driveService) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
        this.driveService = driveService; // Assign it
    }

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

                                // Ensure Google-specific fields are NOT set or are cleared for GitHub users
                                existingUser.setAccessToken(null); // IMPORTANT: Clear or ensure this is null for GitHub users

                                String usernameFromEmail = (finalEmail != null && finalEmail.contains("@")) ? finalEmail.split("@")[0] : null;
                                if (name != null && !name.isEmpty() && (existingUser.getUsername() == null || existingUser.getUsername().isEmpty() || (usernameFromEmail != null && existingUser.getUsername().equals(usernameFromEmail)))) {
                                    existingUser.setUsername(name);
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

                                Mono<User> userSaveMono;
                                // All users (including GitHub) should have a Drive folder
                                if (existingUser.getDriveFolderId() == null || existingUser.getDriveFolderId().isEmpty()) {
                                    logger.info("Existing GitHub user {} found without Drive folder. Creating folder...", finalEmail);
                                    userSaveMono = driveService.createUserFolderIfNotExists(existingUser.getEmail())
                                            .flatMap(folderId -> {
                                                logger.debug("Drive folder created for existing user: {}", folderId);
                                                existingUser.setDriveFolderId(folderId);
                                                existingUser.setCurrentDriveUsageBytes(0L); // Initialize usage
                                                return userRepository.save(existingUser);
                                            })
                                            .onErrorResume(driveEx -> {
                                                logger.error("Failed to create Drive folder for existing GitHub user {}. Proceeding without folder link. Error: {}", existingUser.getEmail(), driveEx.getMessage(), driveEx);
                                                return userRepository.save(existingUser); // Still save the user even if folder creation fails
                                            });
                                } else {
                                    logger.info("Existing GitHub user {} found with existing Drive folder. Updating details...", finalEmail);
                                    userSaveMono = userRepository.save(existingUser);
                                }
                                return userSaveMono;
                            })
                            .switchIfEmpty(Mono.defer(() -> {
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

                                // Ensure Google-specific fields are NOT set for new GitHub users
                                newUser.setAccessToken(null); // IMPORTANT: Ensure this is null

                                String finalUsername = (name != null && !name.isEmpty())
                                        ? name
                                        : (finalEmail != null && finalEmail.contains("@") ? finalEmail.split("@")[0] : "github_user_" + uid.substring(0, 8));
                                newUser.setUsername(finalUsername);
                                logger.debug("New user username set to: {}", finalUsername);

                                LocalDateTime now = LocalDateTime.now();
                                newUser.setCreatedAt(now);
                                newUser.setLastLogin(now);

                                return userRepository.save(newUser)
                                        .flatMap(savedUser -> {
                                            logger.debug("New user saved to DB: {}", savedUser.getEmail());
                                            return driveService.createUserFolderIfNotExists(savedUser.getEmail())
                                                    .flatMap(folderId -> {
                                                        logger.debug("Drive folder created for new user: {}", folderId);
                                                        savedUser.setDriveFolderId(folderId); // Set the folder ID
                                                        return userRepository.save(savedUser);
                                                    })
                                                    .onErrorResume(driveEx -> {
                                                        logger.error("CRITICAL: Failed to create Drive folder for new GitHub user {}. Error: {}", savedUser.getEmail(), driveEx.getMessage(), driveEx);
//                                                         It's usually better to save the user even if Drive folder creation fails,
//                                                         so they can still use other parts of the app. You might want to flag
//                                                         them for later folder creation or handle this more gracefully.
//                                                         For now, we'll save without the folder ID.
//                                                         If you truly want to prevent signup, re-throw a specific error:
//                                                         return userRepository.delete(savedUser)
//                                                                .then(Mono.error(new RuntimeException("GitHub Signup failed: Could not create Drive folder for user.", driveEx)));
                                                        return userRepository.save(savedUser);
                                                    });
                                        });
                            }))
                            .map(user -> {
                                String appJwtToken = jwtUtility.generateToken(user.getEmail());
                                double usageInMb = user.getCurrentDriveUsageBytes() / (1024.0 * 1024.0);
                                logger.debug("Generated JWT for user: {}", user.getEmail());

                                return new GithubResponse(
                                        appJwtToken,
                                        user.getEmail(),
                                        user.getUsername(),
                                        user.getImageUrl(),
                                        user.getAuthProvider().name(), // Send the authProvider name
                                        user.getDriveFolderId(),     // Send the Drive Folder ID
                                        (long) Math.round(usageInMb)
                                );
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