// src/main/java/com/example/acespringbackend/utility/JwtUtility.java
package com.example.acespringbackend.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // Import Keys for secure key generation
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.time.Duration; // Import Duration for convenient expiration time

@Component
public class JwtUtility {

    // IMPORTANT: In a production application, this secret key MUST be loaded securely
    // from environment variables, a configuration server, or a secrets management system.
    // NEVER hardcode it. It should be at least 256 bits (32 bytes) long.
    // Keys.secretKeyFor(SignatureAlgorithm.HS256) generates a secure random key.
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Default expiration time for general tokens (e.g., login tokens)
    private static final long DEFAULT_EXPIRATION_TIME_MILLIS = 1000 * 60 * 60 * 10; // 10 hours

    /**
     * Generates a standard JWT token with a default expiration time.
     * This is typically used for authentication after login.
     *
     * @param username The subject of the token (e.g., user's email).
     * @return A new JWT string.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Sets the principal subject of the token
                .setIssuedAt(new Date(System.currentTimeMillis())) // Sets the issue date of the token
                .setExpiration(new Date(System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME_MILLIS)) // Sets token expiration
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // Signs the token with the secret key and algorithm
                .compact(); // Builds and compacts the token to a string
    }

    /**
     * Generates a JWT token with a custom, specified expiration duration.
     * This is ideal for time-sensitive operations like password reset links.
     *
     * @param username The subject of the token (e.g., user's email).
     * @param expirationDuration The duration after which the token will expire.
     * @return A new JWT string.
     */
    public String generateTokenWithExpiration(String username, Duration expirationDuration) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Sets token expiration based on the provided Duration
                .setExpiration(new Date(System.currentTimeMillis() + expirationDuration.toMillis()))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token The JWT string.
     * @return The username contained in the token.
     */
    public String extractUsername(String token) {
        return parseToken(token).getBody().getSubject();
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token The JWT string.
     * @return The expiration Date of the token.
     */
    public Date extractExpiration(String token) {
        return parseToken(token).getBody().getExpiration();
    }

    /**
     * Validates a JWT token against a known username.
     * This method is suitable for verifying tokens that belong to an already authenticated user.
     * It first checks general token validity (signature, expiry) then verifies the username.
     *
     * @param token The JWT string to validate.
     * @param username The expected username.
     * @return True if the token is valid and belongs to the specified username, false otherwise.
     */
    public boolean validateToken(String token, String username) {
        try {
            // First, perform general token validation (signature and expiry)
            if (!validateToken(token)) {
                return false;
            }
            // Then, compare the extracted username with the provided one
            String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username);
        } catch (JwtException | IllegalArgumentException e) {
            // Log any issues like malformed tokens, invalid signatures, etc.
            System.err.println("Token validation error for username " + username + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates the general integrity and expiration of a JWT token.
     * This method checks if the token's signature is valid and if it has not expired.
     * It does NOT check against a specific username, making it suitable for
     * password reset tokens where the user identity is derived *from* the token.
     *
     * @param token The JWT string to validate.
     * @return True if the token is valid (signed correctly and not expired), false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            // Attempting to parse the token will automatically validate its signature and structure.
            // If it's malformed or has an invalid signature, parseClaimsJws will throw an exception.
            // We then explicitly check for expiration.
            parseToken(token); // This line validates signature and structure
            return !isTokenExpired(token); // Check if it's expired
        } catch (ExpiredJwtException e) {
            System.err.println("Token is expired: " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // Catches general JWT exceptions (e.g., malformed, bad signature)
            System.err.println("General token validation failed (malformed/invalid signature): " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the given JWT token has expired.
     *
     * @param token The JWT string.
     * @return True if the token's expiration date is before the current date, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        // Use the public extractExpiration method
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    /**
     * Parses the given JWT token and returns its claims.
     * This is a private helper method used by other public methods.
     * It also handles basic validation (signature, structure) internally.
     *
     * @param token The JWT string to parse.
     * @return A Jws object containing the token's claims.
     * @throws JwtException if the token is invalid (e.g., malformed, bad signature).
     */
    private Jws<Claims> parseToken(String token) {
        // Using parserBuilder for newer jjwt versions
        return Jwts.parser()
                .setSigningKey(SECRET_KEY) // Set the key used for signing
                .build()
                .parseClaimsJws(token); // Parse and validate the token's claims
    }
}