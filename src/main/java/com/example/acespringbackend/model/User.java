package com.example.acespringbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private Boolean emailVerified;
    private String imageUrl;

    private String accessToken; // Consider if these fields are truly necessary if you're using your own JWTs for primary auth
    private String firebaseIdToken; // These might be for external IdP integration, which is fine

    private AuthProvider authProvider;
    private String signInProvider;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin; // Useful for tracking user activity, not JWT expiry

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

    private long currentDriveUsageBytes;
    private List<String> roles;

    // User account status fields (NOT token status)
    private boolean enabled = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean accountNonExpired = true;

    // --- Constructors ---
    public User() {}

    public User(String id, String username, String email, String password, Boolean emailVerified, String imageUrl,
                String accessToken, String firebaseIdToken, AuthProvider authProvider, String signInProvider,
                LocalDateTime createdAt, LocalDateTime lastLogin, String driveFolderId, String linkedinProfileUrl,
                String githubId, String githubLogin, String githubHtmlUrl, String githubProfileUrl,
                String githubCompany, String githubLocation, String githubBio, Integer githubPublicRepos,
                Integer githubFollowers, Integer githubFollowing, long currentDriveUsageBytes, List<String> roles,
                boolean enabled, boolean accountNonLocked, boolean credentialsNonExpired, boolean accountNonExpired) {
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
        this.roles = roles;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonExpired = accountNonExpired;
    }

    // --- Getters and Setters (omitted for brevity, assume they are present and correct as before) ---
    // Make sure you have getters/setters for all fields, especially 'email' and 'password' for UserDetails.

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsernameField() { return username; } // Renamed to avoid clash with UserDetails.getUsername()
    public void setUsernameField(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @Override
    public String getPassword() { return password; } // UserDetails method
    public void setPassword(String password) { this.password = password; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getFirebaseIdToken() { return firebaseIdToken; }
    public void setFirebaseIdToken(String firebaseIdToken) { this.firebaseIdToken = firebaseIdToken; }
    public AuthProvider getAuthProvider() { return authProvider; }
    public void setAuthProvider(AuthProvider authProvider) { this.authProvider = authProvider; }
    public String getSignInProvider() { return signInProvider; }
    public void setSignInProvider(String signInProvider) { this.signInProvider = signInProvider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public String getDriveFolderId() { return driveFolderId; }
    public void setDriveFolderId(String driveFolderId) { this.driveFolderId = driveFolderId; }
    public String getLinkedinProfileUrl() { return linkedinProfileUrl; }
    public void setLinkedinProfileUrl(String linkedinProfileUrl) { this.linkedinProfileUrl = linkedinProfileUrl; }
    public String getGithubId() { return githubId; }
    public void setGithubId(String githubId) { this.githubId = githubId; }
    public String getGithubLogin() { return githubLogin; }
    public void setGithubLogin(String githubLogin) { this.githubLogin = githubLogin; }
    public String getGithubHtmlUrl() { return githubHtmlUrl; }
    public void setGithubHtmlUrl(String githubHtmlUrl) { this.githubHtmlUrl = githubHtmlUrl; }
    public String getGithubProfileUrl() { return githubProfileUrl; }
    public void setGithubProfileUrl(String githubProfileUrl) { this.githubProfileUrl = githubProfileUrl; }
    public String getGithubCompany() { return githubCompany; }
    public void setGithubCompany(String githubCompany) { this.githubCompany = githubCompany; }
    public String getGithubLocation() { return githubLocation; }
    public void setGithubLocation(String githubLocation) { this.githubLocation = githubLocation; }
    public String getGithubBio() { return githubBio; }
    public void setGithubBio(String githubBio) { this.githubBio = githubBio; }
    public Integer getGithubPublicRepos() { return githubPublicRepos; }
    public void setGithubPublicRepos(Integer githubPublicRepos) { this.githubPublicRepos = githubPublicRepos; }
    public Integer getGithubFollowers() { return githubFollowers; }
    public void setGithubFollowers(Integer githubFollowers) { this.githubFollowers = githubFollowers; }
    public Integer getGithubFollowing() { return githubFollowing; }
    public void setGithubFollowing(Integer githubFollowing) { this.githubFollowing = githubFollowing; }
    public long getCurrentDriveUsageBytes() { return currentDriveUsageBytes; }
    public void setCurrentDriveUsageBytes(long currentDriveUsageBytes) { this.currentDriveUsageBytes = currentDriveUsageBytes; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
    public void setCredentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }
    public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }


    // --- Enum Definition ---
    public enum AuthProvider {
        GOOGLE, GITHUB, FIREBASE, WEBSITE
    }

    // --- UserDetails Interface Implementations ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.email; // Spring Security uses this for the 'username' field
    }

    @Override
    public boolean isAccountNonExpired() { return this.accountNonExpired; }
    @Override
    public boolean isAccountNonLocked() { return this.accountNonLocked; }
    @Override
    public boolean isCredentialsNonExpired() { return this.credentialsNonExpired; }
    @Override
    public boolean isEnabled() { return this.enabled; }

    // --- toString, equals, hashCode (omitted for brevity, but ensure they cover all fields) ---
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + "[PROTECTED]" + '\'' +
                ", emailVerified=" + emailVerified +
                ", imageUrl='" + imageUrl + '\'' +
                ", accessToken='" + "[PROTECTED]" + '\'' +
                ", firebaseIdToken='" + "[PROTECTED]" + '\'' +
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
                ", roles=" + roles +
                ", enabled=" + enabled +
                ", accountNonLocked=" + accountNonLocked +
                ", credentialsNonExpired=" + credentialsNonExpired +
                ", accountNonExpired=" + accountNonExpired +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return currentDriveUsageBytes == user.currentDriveUsageBytes &&
                enabled == user.enabled &&
                accountNonLocked == user.accountNonLocked &&
                credentialsNonExpired == user.credentialsNonExpired &&
                accountNonExpired == user.accountNonExpired &&
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
                Objects.equals(githubFollowing, user.githubFollowing) &&
                Objects.equals(roles, user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, password, emailVerified, imageUrl, accessToken, firebaseIdToken,
                authProvider, signInProvider, createdAt, lastLogin, driveFolderId, linkedinProfileUrl,
                githubId, githubLogin, githubHtmlUrl, githubProfileUrl, githubCompany, githubLocation,
                githubBio, githubPublicRepos, githubFollowers, githubFollowing, currentDriveUsageBytes, roles,
                enabled, accountNonLocked, credentialsNonExpired, accountNonExpired);
    }
}