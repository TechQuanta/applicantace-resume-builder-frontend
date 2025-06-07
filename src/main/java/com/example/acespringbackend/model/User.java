package com.example.acespringbackend.model;

// No Lombok imports needed anymore
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects; // Used for Objects.hash and Objects.equals in hashCode and equals

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private Boolean emailVerified;
    private String imageUrl;

    private String accessToken;
    private String firebaseIdToken;

    private AuthProvider authProvider;
    private String signInProvider;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    private String driveFolderId;

    private String linkedinProfileUrl;

    // GitHub-specific fields
    private String githubId;
    private String githubLogin;
    private String githubHtmlUrl;
    private String githubProfileUrl;
    private String githubCompany;
    private String githubLocation;
    private String githubBio;
    private Integer githubPublicRepos;
    private Integer githubFollowers;
    private Integer githubFollowing;

    // NEW: Field to store current drive usage in bytes
    private long currentDriveUsageBytes;

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public User() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public User(String id, String username, String email, String password, Boolean emailVerified, String imageUrl,
                String accessToken, String firebaseIdToken, AuthProvider authProvider, String signInProvider,
                LocalDateTime createdAt, LocalDateTime lastLogin, String driveFolderId, String linkedinProfileUrl,
                String githubId, String githubLogin, String githubHtmlUrl, String githubProfileUrl,
                String githubCompany, String githubLocation, String githubBio, Integer githubPublicRepos,
                Integer githubFollowers, Integer githubFollowing, long currentDriveUsageBytes) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.emailVerified = emailVerified;
        this.imageUrl = imageUrl;
        this.accessToken = accessToken;
        this.firebaseIdToken = firebaseIdToken;
        this.authProvider = authProvider;
        this.signInProvider = signInProvider;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.driveFolderId = driveFolderId;
        this.linkedinProfileUrl = linkedinProfileUrl;
        this.githubId = githubId;
        this.githubLogin = githubLogin;
        this.githubHtmlUrl = githubHtmlUrl;
        this.githubProfileUrl = githubProfileUrl;
        this.githubCompany = githubCompany;
        this.githubLocation = githubLocation;
        this.githubBio = githubBio;
        this.githubPublicRepos = githubPublicRepos;
        this.githubFollowers = githubFollowers;
        this.githubFollowing = githubFollowing;
        this.currentDriveUsageBytes = currentDriveUsageBytes;
    }

    // --- Getters --- (replaces @Getter)

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getFirebaseIdToken() {
        return firebaseIdToken;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public String getSignInProvider() {
        return signInProvider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public String getDriveFolderId() {
        return driveFolderId;
    }

    public String getLinkedinProfileUrl() {
        return linkedinProfileUrl;
    }

    public String getGithubId() {
        return githubId;
    }

    public String getGithubLogin() {
        return githubLogin;
    }

    public String getGithubHtmlUrl() {
        return githubHtmlUrl;
    }

    public String getGithubProfileUrl() {
        return githubProfileUrl;
    }

    public String getGithubCompany() {
        return githubCompany;
    }

    public String getGithubLocation() {
        return githubLocation;
    }

    public String getGithubBio() {
        return githubBio;
    }

    public Integer getGithubPublicRepos() {
        return githubPublicRepos;
    }

    public Integer getGithubFollowers() {
        return githubFollowers;
    }

    public Integer getGithubFollowing() {
        return githubFollowing;
    }

    public long getCurrentDriveUsageBytes() {
        return currentDriveUsageBytes;
    }

    // --- Setters --- (replaces @Setter)

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setFirebaseIdToken(String firebaseIdToken) {
        this.firebaseIdToken = firebaseIdToken;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public void setSignInProvider(String signInProvider) {
        this.signInProvider = signInProvider;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setDriveFolderId(String driveFolderId) {
        this.driveFolderId = driveFolderId;
    }

    public void setLinkedinProfileUrl(String linkedinProfileUrl) {
        this.linkedinProfileUrl = linkedinProfileUrl;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public void setGithubLogin(String githubLogin) {
        this.githubLogin = githubLogin;
    }

    public void setGithubHtmlUrl(String githubHtmlUrl) {
        this.githubHtmlUrl = githubHtmlUrl;
    }

    public void setGithubProfileUrl(String githubProfileUrl) {
        this.githubProfileUrl = githubProfileUrl;
    }

    public void setGithubCompany(String githubCompany) {
        this.githubCompany = githubCompany;
    }

    public void setGithubLocation(String githubLocation) {
        this.githubLocation = githubLocation;
    }

    public void setGithubBio(String githubBio) {
        this.githubBio = githubBio;
    }

    public void setGithubPublicRepos(Integer githubPublicRepos) {
        this.githubPublicRepos = githubPublicRepos;
    }

    public void setGithubFollowers(Integer githubFollowers) {
        this.githubFollowers = githubFollowers;
    }

    public void setGithubFollowing(Integer githubFollowing) {
        this.githubFollowing = githubFollowing;
    }

    public void setCurrentDriveUsageBytes(long currentDriveUsageBytes) {
        this.currentDriveUsageBytes = currentDriveUsageBytes;
    }

    // --- Enum Definition ---
    public enum AuthProvider {
        GOOGLE,
        GITHUB,
        FIREBASE,
        WEBSITE
    }

    // --- toString, equals, hashCode (for completeness, often generated by Lombok) ---

    @Override
    public String toString() {
        return "User{" +
               "id='" + id + '\'' +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", password='" + "[PROTECTED]" + '\'' + // Mask password
               ", emailVerified=" + emailVerified +
               ", imageUrl='" + imageUrl + '\'' +
               ", accessToken='" + "[PROTECTED]" + '\'' + // Mask access token
               ", firebaseIdToken='" + "[PROTECTED]" + '\'' + // Mask Firebase ID token
               ", authProvider=" + authProvider +
               ", signInProvider='" + signInProvider + '\'' +
               ", createdAt=" + createdAt +
               ", lastLogin=" + lastLogin +
               ", driveFolderId='" + driveFolderId + '\'' +
               ", linkedinProfileUrl='" + linkedinProfileUrl + '\'' +
               ", githubId='" + githubId + '\'' +
               ", githubLogin='" + githubLogin + '\'' +
               ", githubHtmlUrl='" + githubHtmlUrl + '\'' +
               ", githubProfileUrl='" + githubProfileUrl + '\'' +
               ", githubCompany='" + githubCompany + '\'' +
               ", githubLocation='" + githubLocation + '\'' +
               ", githubBio='" + githubBio + '\'' +
               ", githubPublicRepos=" + githubPublicRepos +
               ", githubFollowers=" + githubFollowers +
               ", githubFollowing=" + githubFollowing +
               ", currentDriveUsageBytes=" + currentDriveUsageBytes +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return currentDriveUsageBytes == user.currentDriveUsageBytes &&
               Objects.equals(id, user.id) &&
               Objects.equals(username, user.username) &&
               Objects.equals(email, user.email) &&
               Objects.equals(password, user.password) &&
               Objects.equals(emailVerified, user.emailVerified) &&
               Objects.equals(imageUrl, user.imageUrl) &&
               Objects.equals(accessToken, user.accessToken) &&
               Objects.equals(firebaseIdToken, user.firebaseIdToken) &&
               authProvider == user.authProvider &&
               Objects.equals(signInProvider, user.signInProvider) &&
               Objects.equals(createdAt, user.createdAt) &&
               Objects.equals(lastLogin, user.lastLogin) &&
               Objects.equals(driveFolderId, user.driveFolderId) &&
               Objects.equals(linkedinProfileUrl, user.linkedinProfileUrl) &&
               Objects.equals(githubId, user.githubId) &&
               Objects.equals(githubLogin, user.githubLogin) &&
               Objects.equals(githubHtmlUrl, user.githubHtmlUrl) &&
               Objects.equals(githubProfileUrl, user.githubProfileUrl) &&
               Objects.equals(githubCompany, user.githubCompany) &&
               Objects.equals(githubLocation, user.githubLocation) &&
               Objects.equals(githubBio, user.githubBio) &&
               Objects.equals(githubPublicRepos, user.githubPublicRepos) &&
               Objects.equals(githubFollowers, user.githubFollowers) &&
               Objects.equals(githubFollowing, user.githubFollowing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, password, emailVerified, imageUrl, accessToken, firebaseIdToken,
                            authProvider, signInProvider, createdAt, lastLogin, driveFolderId, linkedinProfileUrl,
                            githubId, githubLogin, githubHtmlUrl, githubProfileUrl, githubCompany, githubLocation,
                            githubBio, githubPublicRepos, githubFollowers, githubFollowing, currentDriveUsageBytes);
    }
}
