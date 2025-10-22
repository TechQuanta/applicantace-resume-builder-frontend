package com.example.acespringbackend.auth.dto;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) for conveying the response after a successful
 * Google authentication. This DTO contains user profile information,
 * authentication token details, and Google Drive related data.
 */
public class GoogleAuthResponse {

    /**
     * The JWT (JSON Web Token) issued by the backend upon successful authentication.
     * This token is used by the frontend for subsequent authenticated API requests.
     */
    private String token;

    /**
     * The {@link Instant} at which the issued {@code token} will expire.
     * This allows the client to manage token refresh proactively.
     */
    private Instant expirationTime;

    /**
     * The user's email address obtained from their Google profile.
     */
    private String email;

    /**
     * The user's display name or username obtained from their Google profile.
     */
    private String username;

    /**
     * A URL to the user's profile picture, if available from Google.
     */
    private String imageUrl;

    /**
     * The authentication provider used (e.g., "Google").
     */
    private String authProvider;

    /**
     * The user's current storage usage in megabytes (MB) on Google Drive.
     * This provides real-time information about consumed storage space.
     */
    private double currentStorageUsageMb;

    /**
     * The unique identifier (Folder ID) of the user's dedicated application
     * folder within Google Drive. This facilitates organizing user-specific files.
     */
    private String driveFolderId;

    /**
     * Default no-argument constructor for {@code GoogleAuthResponse}.
     * This constructor is necessary for frameworks like Spring to properly
     * deserialize JSON into an instance of this object.
     */
    public GoogleAuthResponse() {
    }

    /**
     * Constructs a new {@code GoogleAuthResponse} with all relevant details
     * after a successful Google authentication.
     *
     * @param token                 The JWT token issued by the backend.
     * @param expirationTime        The expiration time of the token.
     * @param email                 The user's email address.
     * @param username              The user's display name.
     * @param imageUrl              The URL to the user's profile picture.
     * @param authProvider          The authentication provider (e.g., "Google").
     * @param driveFolderId         The ID of the user's dedicated Google Drive folder.
     * @param currentStorageUsageMb The user's current Google Drive storage usage in MB.
     */
    public GoogleAuthResponse(String token, Instant expirationTime, String email, String username, String imageUrl, String authProvider, String driveFolderId, double currentStorageUsageMb) {
        this.token = token;
        this.expirationTime = expirationTime;
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
        this.authProvider = authProvider;
        // FIX: Corrected the assignment of these two fields
        this.driveFolderId = driveFolderId;
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    // --- Getters ---

    /**
     * Retrieves the JWT token.
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
     * Retrieves the user's email address.
     *
     * @return The email address as a {@link String}.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retrieves the user's username or display name.
     *
     * @return The username as a {@link String}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the URL to the user's profile picture.
     *
     * @return The image URL as a {@link String}.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Retrieves the authentication provider used.
     *
     * @return The authentication provider as a {@link String}.
     */
    public String getAuthProvider() {
        return authProvider;
    }

    /**
     * Retrieves the user's current storage usage on Google Drive in megabytes.
     *
     * @return The current storage usage in MB.
     */
    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    /**
     * Retrieves the ID of the user's dedicated Google Drive folder.
     *
     * @return The Google Drive folder ID as a {@link String}.
     */
    public String getDriveFolderId() {
        return driveFolderId;
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
     * Sets the user's email address.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the user's username or display name.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the URL to the user's profile picture.
     *
     * @param imageUrl The image URL to set.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Sets the authentication provider used.
     *
     * @param authProvider The authentication provider to set.
     */
    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    /**
     * Sets the user's current storage usage on Google Drive in megabytes.
     *
     * @param currentStorageUsageMb The current storage usage in MB to set.
     */
    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    /**
     * Sets the ID of the user's dedicated Google Drive folder.
     *
     * @param driveFolderId The Google Drive folder ID to set.
     */
    public void setDriveFolderId(String driveFolderId) {
        this.driveFolderId = driveFolderId;
    }

    /**
     * Provides a string representation of the {@code GoogleAuthResponse} object,
     * useful for logging and debugging.
     * <p>
     * Note: For security reasons, consider masking or omitting sensitive
     * information like the full token in production logs.
     *
     * @return A string containing the Google authentication response details.
     */
    @Override
    public String toString() {
        // Masking token for logging in production is a good practice
        final int LOG_TOKEN_LENGTH = 10;
        String maskedToken = (token != null && token.length() > LOG_TOKEN_LENGTH) ?
                             token.substring(0, LOG_TOKEN_LENGTH) + "..." : token;

        return "GoogleAuthResponse{" +
               "token='" + maskedToken + '\'' +
               ", expirationTime=" + expirationTime +
               ", email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", imageUrl='" + imageUrl + '\'' +
               ", authProvider='" + authProvider + '\'' +
               ", currentStorageUsageMb=" + currentStorageUsageMb +
               ", driveFolderId='" + driveFolderId + '\'' +
               '}';
    }
}