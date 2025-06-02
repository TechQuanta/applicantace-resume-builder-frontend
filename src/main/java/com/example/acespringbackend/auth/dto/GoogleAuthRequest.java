package com.example.acespringbackend.auth.dto;

public class GoogleAuthRequest {
    private String idToken;
    private String accessToken;

    public GoogleAuthRequest() {}

    public GoogleAuthRequest(String idToken, String accessToken) {
        this.idToken = idToken;
        this.accessToken = accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
