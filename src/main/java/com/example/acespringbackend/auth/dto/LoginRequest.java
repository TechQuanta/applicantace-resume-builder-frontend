package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class LoginRequest {

    private String email;
    private String password;

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public LoginRequest() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getters --- (replaces @Getter)

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // --- Setters --- (replaces @Setter)

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
               "email='" + email + '\'' +
               ", password='" + "[PROTECTED]" + '\'' + // Mask password for security in toString
               '}';
    }
}
