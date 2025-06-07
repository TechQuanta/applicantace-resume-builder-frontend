package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class SignUpResponse {

    private String email;
    private String username;
    private String linkedinUrl;
    private String message;
    private String token; // Will only be present in the final step of signup
    private double currentStorageUsageMb; // Initial usage will be 0.0 for new users

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public SignUpResponse() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public SignUpResponse(String email, String username, String linkedinUrl, String message, String token, double currentStorageUsageMb) {
        this.email = email;
        this.username = username;
        this.linkedinUrl = linkedinUrl;
        this.message = message;
        this.token = token;
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    // Private constructor for the Builder pattern
    private SignUpResponse(Builder builder) {
        this.email = builder.email;
        this.username = builder.username;
        this.linkedinUrl = builder.linkedinUrl;
        this.message = builder.message;
        this.token = builder.token;
        this.currentStorageUsageMb = builder.currentStorageUsageMb;
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
               '}';
    }
}
