// src/main/java/com/ace/auth/dto/GithubAuthRequest.java
package com.example.acespringbackend.auth.dto;

public class GithubRequest {
    // This is the Firebase ID Token obtained from the Firebase SDK on the frontend
    // after successful authentication with GitHub via Firebase.
    private String idToken;

    // Default constructor is important for JSON deserialization
    public GithubRequest() {
    }

    // Constructor with all fields
    public GithubRequest(String idToken) {
        this.idToken = idToken;
    }

    // Getter for idToken
    public String getIdToken() {
        return idToken;
    }

    // Setter for idToken
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    @Override
    public String toString() {
        return "GithubAuthRequest{" +
               "idToken='" + idToken + '\'' +
               '}';
    }
}