package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class LoginResponse {

    private String token; // Your application's JWT
    private String username;
    private String message;
    private double currentStorageUsageMb; // Added for login response
    private String driveFolderId; // Added to send the user's root drive folder ID
    private String authProvider; // Added to indicate the authentication provider

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public LoginResponse() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public LoginResponse(String token, String username, String message, double currentStorageUsageMb, String driveFolderId, String authProvider) {
        this.token = token;
        this.username = username;
        this.message = message;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.driveFolderId = driveFolderId;
        this.authProvider = authProvider;
    }

    // Private constructor for the Builder pattern
    private LoginResponse(Builder builder) {
        this.token = builder.token;
        this.username = builder.username;
        this.message = builder.message;
        this.currentStorageUsageMb = builder.currentStorageUsageMb;
        this.driveFolderId = builder.driveFolderId;
        this.authProvider = builder.authProvider;
    }

    // --- Getters --- (replaces @Getter)

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    public String getDriveFolderId() {
        return driveFolderId;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    // --- Setters --- (replaces @Setter)

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    public void setDriveFolderId(String driveFolderId) {
        this.driveFolderId = driveFolderId;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    // --- Builder Pattern --- (replaces @Builder)

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String username;
        private String message;
        private double currentStorageUsageMb;
        private String driveFolderId;
        private String authProvider;

        // Private constructor for the Builder class itself
        private Builder() {
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder currentStorageUsageMb(double currentStorageUsageMb) {
            this.currentStorageUsageMb = currentStorageUsageMb;
            return this;
        }

        public Builder driveFolderId(String driveFolderId) {
            this.driveFolderId = driveFolderId;
            return this;
        }

        public Builder authProvider(String authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(this);
        }
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
               "token='" + "[PROTECTED]" + '\'' + // Mask token for security in toString
               ", username='" + username + '\'' +
               ", message='" + message + '\'' +
               ", currentStorageUsageMb=" + currentStorageUsageMb +
               ", driveFolderId='" + driveFolderId + '\'' +
               ", authProvider='" + authProvider + '\'' +
               '}';
    }
}
