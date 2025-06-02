package com.example.acespringbackend.config;

import com.example.applicantace.auth.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Component
public class GitHubOAuthConfig {

    public UserResponse verifyGitHubAccessTokenAndFetchUser(String accessToken) {
        String url = "https://api.github.com/user";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitHubUser> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GitHubUser.class
            );

            GitHubUser user = response.getBody();
            if (user == null || user.getId() == null) {
                throw new RuntimeException("Invalid GitHub user");
            }

            return new UserResponse(user.getId(), user.getLogin(), user.getEmail(), user.getAvatarUrl());

        } catch (HttpClientErrorException ex) {
            log.error("GitHub token verification failed", ex);
            throw new RuntimeException("Invalid GitHub access token");
        }
    }

    private static class GitHubUser {
        private Long id;
        private String login;
        private String email;
        private String avatar_url;

        public Long getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public String getEmail() {
            return email;
        }

        public String getAvatarUrl() {
            return avatar_url;
        }
    }
}
