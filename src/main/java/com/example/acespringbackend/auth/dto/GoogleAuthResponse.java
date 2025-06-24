package com.example.acespringbackend.auth.dto;

public class GoogleAuthResponse {

    private String token;
    private String email;
    private String username;
    private String imageUrl;
    private String authProvider;
    private double currentStorageUsageMb;
    private String driveFolderId; // ADDED: Field for the user's Google Drive folder ID

    public GoogleAuthResponse() {
    }

    public GoogleAuthResponse(String token, String email, String username, String imageUrl, String authProvider, String driveFolderId, double currentStorageUsageMb) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
        this.authProvider = authProvider;
        this.currentStorageUsageMb = currentStorageUsageMb; // This is receiving the driveFolderId string
        this.driveFolderId = driveFolderId; // This is receiving the double currentStorageUsageMb
    }
    // --- Getters ---
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getImageUrl() { return imageUrl; }
    public String getAuthProvider() { return authProvider; }
    public double getCurrentStorageUsageMb() { return currentStorageUsageMb; }
    public String getDriveFolderId() { return driveFolderId; } // NEW: Getter

    // --- Setters ---
    public void setToken(String token) { this.token = token; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public void setCurrentStorageUsageMb(double currentStorageUsageMb) { this.currentStorageUsageMb = currentStorageUsageMb; }
    public void setDriveFolderId(String driveFolderId) { this.driveFolderId = driveFolderId; } // NEW: Setter

    @Override
    public String toString() {
        return "GoogleAuthResponse{" +
               "token='" + token + '\'' +
               ", email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", imageUrl='" + imageUrl + '\'' +
               ", authProvider='" + authProvider + '\'' +
               ", currentStorageUsageMb=" + currentStorageUsageMb +
               ", driveFolderId='" + driveFolderId + '\'' +
               '}';
    }
}