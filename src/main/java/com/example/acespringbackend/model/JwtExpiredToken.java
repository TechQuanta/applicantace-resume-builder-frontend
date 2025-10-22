package com.example.acespringbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field; // It's good practice to explicitly map field names if they might differ or for clarity

import java.time.Instant;
import java.util.Objects;

@Document(collection = "jwtExpiredTokens")
public class JwtExpiredToken {

    @Id
    private String id; // This can be the JWT token itself or a hash of it

    // CHANGE THIS LINE: from ExpirationToken to token
    @Field("token") // Explicitly map to "token" in MongoDB for clarity
    private String token; // Store the full token string

    private Instant expirationTime;
    private boolean used;
    private Instant issuedAt;

    public JwtExpiredToken() {
    }

    public JwtExpiredToken(String id, String token, Instant expirationTime, boolean used, Instant issuedAt) {
        this.id = id;
        this.token = token; // Update assignment here too
        this.expirationTime = expirationTime;
        this.used = used;
        this.issuedAt = issuedAt;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    // This getter is now directly for the 'token' field
    public String getToken() {
        return token;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public boolean isUsed() {
        return used;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    // --- Setters ---

    public void setId(String id) {
        this.id = id;
    }

    // This setter is now directly for the 'token' field
    public void setToken(String token) {
        this.token = token;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    // --- toString, equals, hashCode ---

    @Override
    public String toString() {
        return "JwtExpiredToken{" +
               "id='" + id + '\'' +
               ", token='" + "[PROTECTED]" + '\'' + // Mask token for security
               ", expirationTime=" + expirationTime +
               ", used=" + used +
               ", issuedAt=" + issuedAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwtExpiredToken that = (JwtExpiredToken) o;
        return used == that.used &&
               Objects.equals(id, that.id) &&
               Objects.equals(token, that.token) && // Update here as well
               Objects.equals(expirationTime, that.expirationTime) &&
               Objects.equals(issuedAt, that.issuedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token, expirationTime, used, issuedAt); // Update here as well
    }
}