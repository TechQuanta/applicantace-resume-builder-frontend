package com.example.acespringbackend.auth.dto;

public class GoogleAuthRequest {
    private String idToken;

    public GoogleAuthRequest() {}

    public GoogleAuthRequest(String idToken) { // Updated constructor
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    // Removed getAccessToken and setAccessToken
}