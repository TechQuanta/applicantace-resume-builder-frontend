package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for handling Google authentication requests.
 * This DTO primarily carries the ID Token issued by Google (or Firebase after
 * Google authentication) from the client-side to the backend for verification.
 */
public class GoogleAuthRequest {

    /**
     * The ID Token obtained from Google's authentication process on the frontend.
     * This token is used by the backend to verify the user's identity and
     * establish a secure session. It can be a Google ID token directly or
     * a Firebase ID token generated after Google sign-in via Firebase.
     */
    private String idToken;

    // Potentially add the Google access token here if you're getting it from the frontend
    /*
    *//**
     * An optional Google Access Token. If obtained from the frontend, this token
     * can be used to access Google APIs on behalf of the user.
     *//*
    private String googleAccessToken;
    */

    /**
     * Default no-argument constructor for {@code GoogleAuthRequest}.
     * This constructor is essential for frameworks like Spring to automatically
     * deserialize JSON or form data into an instance of this object.
     */
    public GoogleAuthRequest() {
        // Default constructor for deserialization
    }

    /**
     * Constructs a new {@code GoogleAuthRequest} with the specified ID Token.
     *
     * @param idToken The ID Token obtained from the frontend after Google authentication.
     */
    public GoogleAuthRequest(String idToken) {
        this.idToken = idToken;
    }

    /*
    *//**
     * Constructs a new {@code GoogleAuthRequest} with the specified ID Token and Google Access Token.
     *
     * @param idToken The ID Token.
     * @param googleAccessToken The Google Access Token.
     *//*
    public GoogleAuthRequest(String idToken, String googleAccessToken) {
        this.idToken = idToken;
        this.googleAccessToken = googleAccessToken;
    }
    */

    // --- Getter ---

    /**
     * Retrieves the ID Token.
     *
     * @return The ID Token as a {@link String}.
     */
    public String getIdToken() {
        return idToken;
    }

    /*
    *//**
     * Retrieves the Google Access Token.
     *
     * @return The Google Access Token as a {@link String}, or {@code null} if not provided.
     *//*
    public String getGoogleAccessToken() {
        return googleAccessToken;
    }
    */

    // --- Setter ---

    /**
     * Sets the ID Token.
     *
     * @param idToken The ID Token to set.
     */
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    /*
    *//**
     * Sets the Google Access Token.
     *
     * @param googleAccessToken The Google Access Token to set.
     *//*
    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }
    */

    /**
     * Provides a string representation of the {@code GoogleAuthRequest} object,
     * useful for logging and debugging.
     * <p>
     * Note: For security reasons, sensitive tokens like ID tokens should be
     * handled carefully in logs (e.g., masked or truncated).
     *
     * @return A string containing the ID token (and potentially access token) details.
     */
    @Override
    public String toString() {
        // Masking sensitive tokens for production logging is a good practice.
        final int LOG_TOKEN_LENGTH = 10; // Number of characters to show from token start

        return "GoogleAuthRequest{" +
               "idToken='" + (idToken != null && idToken.length() > LOG_TOKEN_LENGTH ?
                               idToken.substring(0, LOG_TOKEN_LENGTH) + "..." : idToken) + '\'' +
               /*
               ", googleAccessToken='" + (googleAccessToken != null && googleAccessToken.length() > LOG_TOKEN_LENGTH ?
                                        googleAccessToken.substring(0, LOG_TOKEN_LENGTH) + "..." : googleAccessToken) + '\'' +
               */
               '}';
    }
}