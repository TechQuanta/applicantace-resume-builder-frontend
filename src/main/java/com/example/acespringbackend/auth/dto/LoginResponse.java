package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class LoginResponse {

    private String token; // Your application's JWT
    private String username;
    private String message;
    private double currentStorageUsageMb; // Added for login response

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public LoginResponse() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public LoginResponse(String token, String username, String message, double currentStorageUsageMb) {
        this.token = token;
        this.username = username;
        this.message = message;
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    // Private constructor for the Builder pattern
    private LoginResponse(Builder builder) {
        this.token = builder.token;
        this.username = builder.username;
        this.message = builder.message;
        this.currentStorageUsageMb = builder.currentStorageUsageMb;
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

    // --- Builder Pattern --- (replaces @Builder)

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String username;
        private String message;
        private double currentStorageUsageMb;

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
               '}';
    }
}
