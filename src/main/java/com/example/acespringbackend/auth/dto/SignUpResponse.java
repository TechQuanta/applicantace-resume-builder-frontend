package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class SignUpResponse {

    private String email;
    private String username;
    private String linkedinUrl;
    private String message;
    private String token; // Will only be present in the final step of signup
    private double currentStorageUsageMb; // Initial usage will be 0.0 for new users
    private String driveFolderId; // Added to send the user's root drive folder ID
    private String authProvider; // NEW: Added to indicate the authentication provider

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public SignUpResponse() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public SignUpResponse(String email, String username, String linkedinUrl, String message, String token, double currentStorageUsageMb, String driveFolderId, String authProvider) {
        this.email = email;
        this.username = username;
        this.linkedinUrl = linkedinUrl;
        this.message = message;
        this.token = token;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.driveFolderId = driveFolderId;
        this.authProvider = authProvider; // Initialize new field
    }

    // Private constructor for the Builder pattern
    private SignUpResponse(Builder builder) {
        this.email = builder.email;
        this.username = builder.username;
        this.linkedinUrl = builder.linkedinUrl;
        this.message = builder.message;
        this.token = builder.token;
        this.currentStorageUsageMb = builder.currentStorageUsageMb;
        this.driveFolderId = builder.driveFolderId;
        this.authProvider = builder.authProvider; // Initialize new field from builder
    }

    // --- Getters --- (replaces @Getter)

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
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
        private String email;
        private String username;
        private String linkedinUrl;
        private String message;
        private String token;
        private double currentStorageUsageMb;
        private String driveFolderId;
        private String authProvider; // NEW: Added to builder

        // Private constructor for the Builder class itself
        private Builder() {
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder linkedinUrl(String linkedinUrl) {
            this.linkedinUrl = linkedinUrl;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
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

        public SignUpResponse build() {
            return new SignUpResponse(this);
        }
    }

    @Override
    public String toString() {
        return "SignUpResponse{" +
               "email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", linkedinUrl='" + linkedinUrl + '\'' +
               ", message='" + message + '\'' +
               ", token='" + (token != null ? "[PROTECTED]" : "null") + '\'' + // Mask token for security
               ", currentStorageUsageMb=" + currentStorageUsageMb +
               ", driveFolderId='" + driveFolderId + '\'' +
               ", authProvider='" + authProvider + '\'' + // Include in toString
               '}';
    }
}
