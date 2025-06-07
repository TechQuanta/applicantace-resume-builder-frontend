package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class PasswordResetRequest {

    private String token;
    private String newPassword;

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public PasswordResetRequest() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public PasswordResetRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    // --- Getters --- (replaces @Getter)

    public String getToken() {
        return token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    // --- Setters --- (replaces @Setter)

    public void setToken(String token) {
        this.token = token;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "PasswordResetRequest{" +
               "token='" + token + '\'' +
               ", newPassword='" + "[PROTECTED]" + '\'' + // Mask newPassword for security in toString
               '}';
    }
}
