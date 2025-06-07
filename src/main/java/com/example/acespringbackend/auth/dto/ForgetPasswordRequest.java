package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class ForgetPasswordRequest {

    private String email;

    // No-argument constructor
    public ForgetPasswordRequest() {
    }

    // All-argument constructor
    public ForgetPasswordRequest(String email) {
        this.email = email;
    }

    // Getter
    public String getEmail() {
        return email;
    }

    // Setter
    public void setEmail(String email) {
        this.email = email;
    }
}
