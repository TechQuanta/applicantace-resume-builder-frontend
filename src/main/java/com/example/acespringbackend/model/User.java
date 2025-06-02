// com.example.backend.auth.model.User.java
package com.example.applicantace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

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
    private String password;

    private AuthProvider authProvider;

    public enum AuthProvider {
        GOOGLE,
        GITHUB,
        FIREBASE, WEBSITE
    }

    public User(String username, String email, String password, AuthProvider authProvider) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.authProvider = authProvider;
    }
}