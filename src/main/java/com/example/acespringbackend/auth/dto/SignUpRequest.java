package com.example.acespringbackend.auth.dto;

public class SignUpRequest {

    private String username;
    private String email;
    private String password;
    private String name;
    private String linkedinProfileUrl;
    private String token;

    public SignUpRequest() {}

    public SignUpRequest(String username, String email, String password, String name,
                         String linkedinProfileUrl, String token) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.linkedinProfileUrl = linkedinProfileUrl;
        this.token = token;
    }

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkedinProfileUrl() {
        return linkedinProfileUrl;
    }

    public void setLinkedinProfileUrl(String linkedinProfileUrl) {
        this.linkedinProfileUrl = linkedinProfileUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
