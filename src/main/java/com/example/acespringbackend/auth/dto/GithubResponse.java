package com.example.acespringbackend.auth.dto;

// No Lombok imports needed

public class GithubResponse {

    private String jwtToken; // Your application's JWT
    private String email;
    private String username;
    private String imageUrl;
    private String authProvider; // Will be "GITHUB" for this response
    private String driveFolderId; // User's dedicated Google Drive folder ID
    private double currentStorageUsageMb; // Added field for current storage usage

    // --- Constructors ---

    // No-argument constructor
    public GithubResponse() {
    }

    // All-argument constructor
    public GithubResponse(String jwtToken, String email, String username, String imageUrl, String authProvider, String driveFolderId, double currentStorageUsageMb) {
        this.jwtToken = jwtToken;
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
        this.authProvider = authProvider;
        this.driveFolderId = driveFolderId;
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    // --- Getters ---

    public String getJwtToken() {
        return jwtToken;
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

    public String getDriveFolderId() {
        return driveFolderId;
    }

    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    // --- Setters ---

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
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

    public void setDriveFolderId(String driveFolderId) {
        this.driveFolderId = driveFolderId;
    }

    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    @Override
    public String toString() {
        return "GithubResponse{" +
               "jwtToken='" + jwtToken + '\'' +
               ", email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", imageUrl='" + imageUrl + '\'' +
               ", authProvider='" + authProvider + '\'' +
               ", driveFolderId='" + driveFolderId + '\'' +
               ", currentStorageUsageMb=" + currentStorageUsageMb +
               '}';
    }
}