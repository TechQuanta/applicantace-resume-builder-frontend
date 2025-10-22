package com.example.acespringbackend.auth.dto;

import java.time.Instant;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) representing the response after a successful user login.
 * This DTO encapsulates authentication tokens, user profile information, and
 * relevant storage details for the client.
 */
public class LoginResponse {

    /**
     * The JWT (JSON Web Token) issued by the backend upon successful authentication.
     * This token is sent to the frontend and used for subsequent authenticated API requests.
     */
    private String token;

    /**
     * The {@link Instant} representing the exact time when the issued {@code token} will expire.
     * This allows the frontend to manage token validity and initiate refresh procedures proactively.
     */
    private Instant expirationTime;

    /**
     * The username or display name of the authenticated user.
     */
    private String username;

    /**
     * The email address of the authenticated user.
     */
    private String email; // <--- ADDED THIS FIELD

    /**
     * A descriptive message about the login outcome (e.g., "Login successful!").
     */
    private String message;

    /**
     * The user's current storage usage in megabytes (MB) on their associated cloud drive (e.g., Google Drive).
     */
    private double currentStorageUsageMb;

    /**
     * The unique identifier (Folder ID) of the user's dedicated application folder
     * within their cloud drive. This helps in organizing user-specific files.
     */
    private String driveFolderId;

    /**
     * The authentication provider used for login (e.g., "email/password", "Google", "GitHub").
     */
    private String authProvider;

    /**
     * Default no-argument constructor for {@code LoginResponse}.
     * This constructor is essential for deserialization frameworks (like Jackson)
     * to automatically map JSON data to an instance of this object.
     */
    public LoginResponse() {
    }

    /**
     * Constructs a new {@code LoginResponse} with all required parameters.
     * This constructor is typically used when all login and user details are available
     * at the time of response creation.
     *
     * @param token                 The JWT token issued to the user.
     * @param expirationTime        The expiration time of the token.
     * @param username              The username of the logged-in user.
     * @param email                 The email of the logged-in user.  // <--- ADDED THIS PARAM
     * @param message               A message describing the login outcome.
     * @param currentStorageUsageMb The user's current storage usage in MB.
     * @param driveFolderId         The ID of the user's dedicated drive folder.
     * @param authProvider          The authentication provider used.
     */
    public LoginResponse(String token, Instant expirationTime, String username, String email, String message, double currentStorageUsageMb, String driveFolderId, String authProvider) {
        this.token = token;
        this.expirationTime = expirationTime;
        this.username = username;
        this.email = email; // <--- ASSIGNED HERE
        this.message = message;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.driveFolderId = driveFolderId;
        this.authProvider = authProvider;
    }

    /**
     * Private constructor used by the {@link Builder} to create an immutable {@code LoginResponse} instance.
     *
     * @param builder The builder instance containing the populated fields.
     */
    private LoginResponse(Builder builder) {
        this.token = builder.token;
        this.expirationTime = builder.expirationTime;
        this.username = builder.username;
        this.email = builder.email; // <--- ASSIGNED HERE
        this.message = builder.message;
        this.currentStorageUsageMb = builder.currentStorageUsageMb;
        this.driveFolderId = builder.driveFolderId;
        this.authProvider = builder.authProvider;
    }

    // --- Getters ---

    /**
     * Retrieves the JWT token issued to the user.
     *
     * @return The authentication token as a {@link String}.
     */
    public String getToken() {
        return token;
    }

    /**
     * Retrieves the expiration time of the token.
     *
     * @return The token expiration time as an {@link Instant}.
     */
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Retrieves the username of the logged-in user.
     *
     * @return The username as a {@link String}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the email address of the logged-in user.
     *
     * @return The email as a {@link String}.
     */
    public String getEmail() { // <--- ADDED GETTER
        return email;
    }

    /**
     * Retrieves the message about the login outcome.
     *
     * @return The message as a {@link String}.
     */
    public String getMessage() {
        return message;
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
     * Retrieves the authentication provider used for login.
     *
     * @return The authentication provider as a {@link String}.
     */
    public String getAuthProvider() {
        return authProvider;
    }

    // --- Setters ---

    /**
     * Sets the JWT token.
     *
     * @param token The authentication token to set.
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
     * Sets the username of the logged-in user.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the email address of the logged-in user.
     *
     * @param email The email to set.
     */
    public void setEmail(String email) { // <--- ADDED SETTER
        this.email = email;
    }

    /**
     * Sets the message about the login outcome.
     *
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
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
     * Sets the authentication provider used for login.
     *
     * @param authProvider The authentication provider to set.
     */
    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    // --- Builder Pattern ---

    /**
     * Returns a new {@link Builder} instance to construct a {@code LoginResponse} object.
     * This provides a fluent API for creating instances, especially useful when
     * not all fields are available at once or when dealing with many optional fields.
     *
     * @return A new {@link Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Inner static class implementing the Builder pattern for {@code LoginResponse}.
     * This allows for a more readable and flexible way to construct {@code LoginResponse} objects.
     */
    public static class Builder {
        private String token;
        private Instant expirationTime;
        private String username;
        private String email; // <--- ADDED TO BUILDER
        private String message;
        private double currentStorageUsageMb;
        private String driveFolderId;
        private String authProvider;

        /**
         * Private constructor for the Builder, ensuring it can only be created via {@code LoginResponse.builder()}.
         */
        private Builder() {
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
         * Sets the username for the builder.
         * @param username The username.
         * @return The builder instance.
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets the email for the builder.
         * @param email The email.
         * @return The builder instance.
         */
        public Builder email(String email) { // <--- ADDED BUILDER METHOD
            this.email = email;
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
         * Builds and returns a new {@code LoginResponse} instance based on the
         * values set in this builder.
         *
         * @return A new {@code LoginResponse} object.
         */
        public LoginResponse build() {
            return new LoginResponse(this);
        }
    }

    /**
     * Provides a string representation of the {@code LoginResponse} object.
     * For security reasons, the {@code token} field is masked to prevent
     * accidental logging of sensitive information in production environments.
     *
     * @return A string containing the login response details with masked token.
     */
    @Override
    public String toString() {
        // Masking sensitive tokens for production logging is a good practice.
        final int LOG_TOKEN_LENGTH = 10; // Number of characters to show from token start
        String maskedToken = (token != null && token.length() > LOG_TOKEN_LENGTH) ?
                             token.substring(0, LOG_TOKEN_LENGTH) + "..." : token;

        return "LoginResponse{" +
                "token='" + maskedToken + '\'' +
                ", expirationTime=" + expirationTime +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' + // <--- ADDED TO TOSTRING
                ", message='" + message + '\'' +
                ", currentStorageUsageMb=" + currentStorageUsageMb +
                ", driveFolderId='" + driveFolderId + '\'' +
                ", authProvider='" + authProvider + '\'' +
                '}';
    }

    /**
     * Compares this {@code LoginResponse} object with another object for equality.
     * Two {@code LoginResponse} objects are considered equal if all their fields are equal.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginResponse that = (LoginResponse) o;
        return Double.compare(that.currentStorageUsageMb, currentStorageUsageMb) == 0 &&
               Objects.equals(token, that.token) &&
               Objects.equals(expirationTime, that.expirationTime) &&
               Objects.equals(username, that.username) &&
               Objects.equals(email, that.email) && // <--- ADDED TO EQUALS
               Objects.equals(message, that.message) &&
               Objects.equals(driveFolderId, that.driveFolderId) &&
               Objects.equals(authProvider, that.authProvider);
    }

    /**
     * Generates a hash code for this {@code LoginResponse} object.
     * The hash code is based on all fields to ensure consistency with {@code equals()}.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(token, expirationTime, username, email, message, currentStorageUsageMb, driveFolderId, authProvider); // <--- ADDED TO HASHCODE
    }
}