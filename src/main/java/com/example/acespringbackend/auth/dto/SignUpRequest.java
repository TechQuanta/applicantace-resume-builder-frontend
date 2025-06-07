package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class SignUpRequest {

    private String username;
    private String email;
    private String password;
    private String name;
    private String linkedinProfileUrl;

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public SignUpRequest() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public SignUpRequest(String username, String email, String password, String name, String linkedinProfileUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.linkedinProfileUrl = linkedinProfileUrl;
    }

    // --- Getters --- (replaces @Getter)

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getLinkedinProfileUrl() {
        return linkedinProfileUrl;
    }

    // --- Setters --- (replaces @Setter)

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLinkedinProfileUrl(String linkedinProfileUrl) {
        this.linkedinProfileUrl = linkedinProfileUrl;
    }

    @Override
    public String toString() {
        return "SignUpRequest{" +
               "username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", password='" + "[PROTECTED]" + '\'' + // Mask password for security in toString
               ", name='" + name + '\'' +
               ", linkedinProfileUrl='" + linkedinProfileUrl + '\'' +
               '}';
    }
}
