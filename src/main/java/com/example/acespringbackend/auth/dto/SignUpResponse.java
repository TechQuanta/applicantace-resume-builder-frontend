package com.example.acespringbackend.auth.dto;

public class SignUpResponse {
    private String email;
    private String message;
    private String token; // ✅ Add JWT token

    public SignUpResponse() {}

    public SignUpResponse(String email, String message) {
        this.email = email;
        this.message = message;
    }

    public SignUpResponse(String email, String message, String token) {
        this.email = email;
        this.message = message;
        this.token = token;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
