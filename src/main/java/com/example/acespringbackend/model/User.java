package com.example.acespringbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id; // Firebase UID or UUID for website users

    private String username;
    private String email;
    private String password; // for website auth users only
    private Boolean emailVerified;
    private String imageUrl;

    private String accessToken;
    private String firebaseIdToken;

    private AuthProvider authProvider;
    private String signInProvider; // e.g. "google.com", "github.com", "email/password"
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    private String driveFolderId; // Add this to track user's Google Drive folder

    // GitHub-specific fields
    private String githubId;
    private String githubLogin;
    private String githubHtmlUrl;
    private String githubProfileUrl;
    private String githubCompany;
    private String githubLocation;
    private String githubBio;
    private Integer githubPublicRepos;
    private Integer githubFollowers;
    private Integer githubFollowing;

    public enum AuthProvider {
        GOOGLE,
        GITHUB,
        FIREBASE,
        WEBSITE
    }
}
