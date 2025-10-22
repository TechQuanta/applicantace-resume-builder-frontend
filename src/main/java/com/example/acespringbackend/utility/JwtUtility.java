package com.example.acespringbackend.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders; // Make sure this import is present
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.concurrent.TimeUnit; // Import for TimeUnit

@Component
public class JwtUtility {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Token validity periods
    private final long JWT_VALIDITY_7_DAYS = TimeUnit.DAYS.toMillis(7);     // 7 days expiration
    private final long JWT_VALIDITY_30_MINUTES = TimeUnit.MINUTES.toMillis(30); // 30 minutes expiration

    // --- Token Generation Methods ---

    public String generateToken7Days(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email, JWT_VALIDITY_7_DAYS);
    }

    public String generateToken30Minutes(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email, JWT_VALIDITY_30_MINUTES);
    }

    // --- Core Token Creation Logic (Reusable) ---

    private String createToken(Map<String, Object> claims, String subject, long validityMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Set the expiration date based on the provided validity
                .setExpiration(new Date(System.currentTimeMillis() + validityMillis))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- Token Information Extraction Methods ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- Token Validation Methods ---

    private Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // --- Helper Method for Signing Key ---

    private Key getSignKey() {
        // !!! FIX IS HERE: Use Base64URL decoder for the secret key !!!
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey); // <--- CHANGED FROM Decoders.BASE64
        return Keys.hmacShaKeyFor(keyBytes);
    }
}