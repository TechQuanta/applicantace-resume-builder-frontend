package com.example.acespringbackend.auth.dto;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) representing the response after a user sign-up operation.
 * This DTO provides feedback on the registration outcome, user profile details,
 * and, upon final successful registration, authentication token information and
 * initial storage details.
 */
public class SignUpResponse {

    /**
     * The email address of the newly registered or signing-up user.
     */
    private String email;

    /**
     * The username of the newly registered or signing-up user.
     */
    private String username;

    /**
     * The LinkedIn profile URL provided by the user during sign-up, if any.
     */
    private String linkedinUrl;

    /**
     * A descriptive message about the sign-up outcome (e.g., "Registration successful!",
     * "User already exists").
     */
    private String message;

    /**
     * The JWT (JSON Web Token) issued by the backend upon successful completion
     * of the sign-up process. This token enables immediate authenticated access.
     * This field will only be present in the final step of a multi-step signup flow, if applicable.
     */
    private String token;

    /**
     * The {@link Instant} representing the exact time when the issued {@code token} will expire.
     * This allows the frontend to manage token validity and initiate refresh procedures.
     */
    private Instant expirationTime;

    /**
     * The user's initial storage usage in megabytes (MB) on their associated cloud drive.
     * For new users, this will typically be 0.0.
     */
    private double currentStorageUsageMb;

    /**
     * The unique identifier (Folder ID) of the user's dedicated application folder
     * within their cloud drive (e.g., Google Drive). This facilitates organizing user-specific files.
     */
    private String driveFolderId;

    /**
     * The authentication provider used for sign-up (e.g., "email/password", "Google", "GitHub").
     */
    private String authProvider;

    /**
     * Default no-argument constructor for {@code SignUpResponse}.
     * This constructor is essential for deserialization frameworks (like Jackson)
     * to automatically map JSON data to an instance of this object.
     */
    public SignUpResponse() {
    }

    /**
     * Constructs a new {@code SignUpResponse} with all relevant details after a sign-up operation.
     * This constructor is used to fully populate the response object for the client.
     *
     * @param email                 The user's email address.
     * @param username              The user's username.
     * @param linkedinUrl           The user's LinkedIn profile URL.
     * @param message               A message describing the sign-up outcome.
     * @param token                 The JWT token issued (can be {@code null} if not applicable yet).
     * @param expirationTime        The expiration time of the token (can be {@code null} if no token).
     * @param currentStorageUsageMb The user's initial storage usage in MB.
     * @param driveFolderId         The ID of the user's dedicated drive folder.
     * @param authProvider          The authentication provider used.
     */
    public SignUpResponse(String email, String username, String linkedinUrl, String message, String token, Instant expirationTime, double currentStorageUsageMb, String driveFolderId, String authProvider) {
        this.email = email;
        this.username = username;
        this.linkedinUrl = linkedinUrl;
        this.message = message;
        this.token = token;
        this.expirationTime = expirationTime;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.driveFolderId = driveFolderId;
        this.authProvider = authProvider;
    }

    /**
     * Private constructor used by the {@link Builder} to create an immutable {@code SignUpResponse} instance.
     *
     * @param builder The builder instance containing the populated fields.
     */
    private SignUpResponse(Builder builder) {
        this.email = builder.email;
        this.username = builder.username;
        this.linkedinUrl = builder.linkedinUrl;
        this.message = builder.message;
        this.token = builder.token;
        this.expirationTime = builder.expirationTime;
        this.currentStorageUsageMb = builder.currentStorageUsageMb;
        this.driveFolderId = builder.driveFolderId;
        this.authProvider = builder.authProvider;
    }

    // --- Getters ---

    /**
     * Retrieves the email address of the user.
     *
     * @return The email address as a {@link String}.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retrieves the username of the user.
     *
     * @return The username as a {@link String}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the LinkedIn profile URL of the user.
     *
     * @return The LinkedIn URL as a {@link String}, or {@code null} if not provided.
     */
    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    /**
     * Retrieves the message about the sign-up outcome.
     *
     * @return The message as a {@link String}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the JWT token issued upon successful sign-up.
     *
     * @return The token as a {@link String}, or {@code null} if not applicable.
     */
    public String getToken() {
        return token;
    }

    /**
     * Retrieves the expiration time of the token.
     *
     * @return The token expiration time as an {@link Instant}, or {@code null} if no token.
     */
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Retrieves the user's current storage usage in megabytes.
     *
     * @return The current storage usage in MB.
     */
    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    /**
     * Retrieves the ID of the user's dedicated drive folder.
     *
     * @return The drive folder ID as a {@link String}.
     */
    public String getDriveFolderId() {
        return driveFolderId;
    }

    /**
     * Retrieves the authentication provider used for sign-up.
     *
     * @return The authentication provider as a {@link String}.
     */
    public String getAuthProvider() {
        return authProvider;
    }

    // --- Setters ---

    /**
     * Sets the email address of the user.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the LinkedIn profile URL of the user.
     *
     * @param linkedinUrl The LinkedIn URL to set.
     */
    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    /**
     * Sets the message about the sign-up outcome.
     *
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the JWT token issued upon successful sign-up.
     *
     * @param token The token to set.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Sets the expiration time of the token.
     *
     * @param expirationTime The token expiration time to set.
     */
    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Sets the user's current storage usage in megabytes.
     *
     * @param currentStorageUsageMb The current storage usage in MB to set.
     */
    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    /**
     * Sets the ID of the user's dedicated drive folder.
     *
     * @param driveFolderId The drive folder ID to set.
     */
    public void setDriveFolderId(String driveFolderId) {
        this.driveFolderId = driveFolderId;
    }

    /**
     * Sets the authentication provider used for sign-up.
     *
     * @param authProvider The authentication provider to set.
     */
    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    // --- Builder Pattern ---

    /**
     * Returns a new {@link Builder} instance to construct a {@code SignUpResponse} object.
     * This provides a fluent API for creating instances, especially useful when
     * not all fields are available at once or when dealing with many optional fields.
     *
     * @return A new {@link Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Inner static class implementing the Builder pattern for {@code SignUpResponse}.
     * This allows for a more readable and flexible way to construct {@code SignUpResponse} objects.
     */
    public static class Builder {
        private String email;
        private String username;
        private String linkedinUrl;
        private String message;
        private String token;
        private Instant expirationTime;
        private double currentStorageUsageMb;
        private String driveFolderId;
        private String authProvider;

        /**
         * Private constructor for the Builder class itself, ensuring it can only be created via {@code SignUpResponse.builder()}.
         */
        private Builder() {
        }

        /**
         * Sets the email for the builder.
         * @param email The user's email address.
         * @return The builder instance.
         */
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        /**
         * Sets the username for the builder.
         * @param username The username.
         * @return The builder instance.
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets the LinkedIn profile URL for the builder.
         * @param linkedinUrl The LinkedIn URL.
         * @return The builder instance.
         */
        public Builder linkedinUrl(String linkedinUrl) {
            this.linkedinUrl = linkedinUrl;
            return this;
        }

        /**
         * Sets the message for the builder.
         * @param message The message.
         * @return The builder instance.
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the token for the builder.
         * @param token The JWT token.
         * @return The builder instance.
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * Sets the expiration time for the builder.
         * @param expirationTime The token expiration time.
         * @return The builder instance.
         */
        public Builder expirationTime(Instant expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /**
         * Sets the current storage usage in MB for the builder.
         * @param currentStorageUsageMb The current storage usage.
         * @return The builder instance.
         */
        public Builder currentStorageUsageMb(double currentStorageUsageMb) {
            this.currentStorageUsageMb = currentStorageUsageMb;
            return this;
        }

        /**
         * Sets the drive folder ID for the builder.
         * @param driveFolderId The drive folder ID.
         * @return The builder instance.
         */
        public Builder driveFolderId(String driveFolderId) {
            this.driveFolderId = driveFolderId;
            return this;
        }

        /**
         * Sets the authentication provider for the builder.
         * @param authProvider The authentication provider.
         * @return The builder instance.
         */
        public Builder authProvider(String authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        /**
         * Builds and returns a new {@code SignUpResponse} instance based on the
         * values set in this builder.
         *
         * @return A new {@code SignUpResponse} object.
         */
        public SignUpResponse build() {
            return new SignUpResponse(this);
        }
    }

    /**
     * Provides a string representation of the {@code SignUpResponse} object.
     * For security reasons, the {@code token} field is masked to prevent
     * accidental logging of sensitive information in production environments.
     *
     * @return A string containing the sign-up response details with masked token.
     */
    @Override
    public String toString() {
        // Masking sensitive tokens for production logging is a good practice.
        final int LOG_TOKEN_LENGTH = 10; // Number of characters to show from token start
        String maskedToken = (token != null && token.length() > LOG_TOKEN_LENGTH) ?
                             token.substring(0, LOG_TOKEN_LENGTH) + "..." : token;

        return "SignUpResponse{" +
               "email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", linkedinUrl='" + linkedinUrl + '\'' +
               ", message='" + message + '\'' +
               ", token='" + maskedToken + '\'' +
               ", expirationTime=" + expirationTime +
               ", currentStorageUsageMb=" + currentStorageUsageMb +
               ", driveFolderId='" + driveFolderId + '\'' +
               ", authProvider='" + authProvider + '\'' +
               '}';
    }
}