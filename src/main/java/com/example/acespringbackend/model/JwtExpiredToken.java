package com.example.acespringbackend.model;

// No Lombok imports needed anymore
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects; // Used for Objects.hash and Objects.equals in hashCode and equals

@Document(collection = "jwtExpiredTokens")
public class JwtExpiredToken {

    @Id
    private String id; // This can be the JWT token itself or a hash of it
    private String token; // Store the full token string
    private Instant expirationTime; // When the token is set to expire
    private boolean used; // To mark if the token has been used
    private Instant issuedAt; // When the token was issued

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public JwtExpiredToken() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public JwtExpiredToken(String id, String token, Instant expirationTime, boolean used, Instant issuedAt) {
        this.id = id;
        this.token = token;
        this.expirationTime = expirationTime;
        this.used = used;
        this.issuedAt = issuedAt;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public boolean isUsed() { // For boolean fields, Lombok typically generates `is` prefix
        return used;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    // --- Setters ---

    public void setId(String id) {
        this.id = id;
    }

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

    // --- toString, equals, hashCode (replaces @Data functionality) ---

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
               Objects.equals(token, that.token) &&
               Objects.equals(expirationTime, that.expirationTime) &&
               Objects.equals(issuedAt, that.issuedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token, expirationTime, used, issuedAt);
    }
}
