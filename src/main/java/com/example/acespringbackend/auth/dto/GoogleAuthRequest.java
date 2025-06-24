package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class GoogleAuthRequest {

    private String idToken;
    // Potentially add the Google access token here if you're getting it from the frontend
    // private String googleAccessToken;

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public GoogleAuthRequest() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public GoogleAuthRequest(String idToken) {
        this.idToken = idToken;
    }

    // --- Getter --- (replaces @Getter)

    public String getIdToken() {
        return idToken;
    }

    // --- Setter --- (replaces @Setter)

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    // You can uncomment and add the getter/setter for googleAccessToken if needed
    /*
    public String getGoogleAccessToken() {
        return googleAccessToken;
    }

    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }
    */

    @Override
    public String toString() {
        return "GoogleAuthRequest{" +
               "idToken='" + idToken + '\'' +
               // Include googleAccessToken in toString if uncommented
               // ", googleAccessToken='" + googleAccessToken + '\'' +
               '}';
    }
}