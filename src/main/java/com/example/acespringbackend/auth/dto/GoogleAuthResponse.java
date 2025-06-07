package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class GoogleAuthResponse {

    private String token; // Your application's JWT
    private String email;
    private String username;
    private String imageUrl;
    private String authProvider; // e.g., "GOOGLE"
    private double currentStorageUsageMb; // Added field for current storage usage

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public GoogleAuthResponse() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public GoogleAuthResponse(String token, String email, String username, String imageUrl, String authProvider, double currentStorageUsageMb) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
        this.authProvider = authProvider;
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    // --- Getters --- (replaces @Getter)

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    // --- Setters --- (replaces @Setter)

    public void setToken(String token) {
        this.token = token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    @Override
    public String toString() {
        return "GoogleAuthResponse{" +
               "token='" + token + '\'' +
               ", email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", imageUrl='" + imageUrl + '\'' +
               ", authProvider='" + authProvider + '\'' +
               ", currentStorageUsageMb=" + currentStorageUsageMb +
               '}';
    }
}
