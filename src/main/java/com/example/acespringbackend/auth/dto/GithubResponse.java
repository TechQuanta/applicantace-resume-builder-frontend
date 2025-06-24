package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class GithubResponse {

    private String jwtToken; // Your application's JWT
    private String email;
    private String username;
    private String imageUrl;
    private String authProvider; // Will be "GITHUB" for this response, now initialized properly
    private String driveFolderId; // User's dedicated Google Drive folder ID
    private double currentStorageUsageMb; // Added field for current storage usage

    // --- Constructors ---



    // All-argument constructor
    public GithubResponse(String jwtToken, String email, String username, String imageUrl, String authProvider, String driveFolderId, Object object, double currentStorageUsageMb) {
        this.jwtToken = jwtToken;
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
        this.authProvider = authProvider;
        this.driveFolderId = driveFolderId;
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    // Private constructor for the Builder pattern
    private GithubResponse(Builder builder) {
        this.jwtToken = builder.jwtToken;
        this.email = builder.email;
        this.username = builder.username;
        this.imageUrl = builder.imageUrl;
        this.authProvider = builder.authProvider;
        this.driveFolderId = builder.driveFolderId;
        this.currentStorageUsageMb = builder.currentStorageUsageMb;
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

    // --- Builder Pattern ---
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jwtToken;
        private String email;
        private String username;
        private String imageUrl;
        private String authProvider;
        private String driveFolderId;
        private double currentStorageUsageMb;

        private Builder() {
        }

        public Builder jwtToken(String jwtToken) {
            this.jwtToken = jwtToken;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder authProvider(String authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        public Builder driveFolderId(String driveFolderId) {
            this.driveFolderId = driveFolderId;
            return this;
        }

        public Builder currentStorageUsageMb(double currentStorageUsageMb) {
            this.currentStorageUsageMb = currentStorageUsageMb;
            return this;
        }

        public GithubResponse build() {
            return new GithubResponse(this);
        }
    }

    @Override
    public String toString() {
        return "GithubResponse{" +
               "jwtToken='" + "[PROTECTED]" + '\'' + // Mask token for security in toString
               ", email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", imageUrl='" + imageUrl + '\'' +
               ", authProvider='" + authProvider + '\'' +
               ", driveFolderId='" + driveFolderId + '\'' +
               ", currentStorageUsageMb=" + currentStorageUsageMb +
               '}';
    }
}
