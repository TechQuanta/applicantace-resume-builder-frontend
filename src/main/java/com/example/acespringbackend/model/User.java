package com.example.acespringbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private String imageUrl;

    private String accessToken;
    private String firebaseIdToken;

    private AuthProvider authProvider;

    public User(String username, String email, String encodedPassword, AuthProvider authProvider) {
    }

    public String getPassword() {
        return email;
    }

    public enum AuthProvider {
        GOOGLE,
        GITHUB,
        FIREBASE,
        WEBSITE
    }

    public User(String username, String email, String imageUrl, String accessToken, String firebaseIdToken, AuthProvider authProvider) {
        this.username = username;
        this.email = email;
        this.imageUrl = imageUrl;
        this.accessToken = accessToken;
        this.firebaseIdToken = firebaseIdToken;
        this.authProvider = authProvider;
    }
}
