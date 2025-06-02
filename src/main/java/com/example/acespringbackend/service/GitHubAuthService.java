package com.example.acespringbackend.service;

import com.example.acespringbackend.config.GitHubOAuthConfig;
import com.example.applicantace.auth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubAuthService {

    private final GitHubOAuthConfig gitHubConfig;

    public UserResponse loginWithGitHub(String accessToken) {
        return gitHubConfig.verifyGitHubAccessTokenAndFetchUser(accessToken);
    }
}
