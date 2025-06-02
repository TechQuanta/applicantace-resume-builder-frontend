package com.example.acespringbackend.auth.controller;

import com.example.applicantace.auth.dto.GitHubLoginRequest;
import com.example.applicantace.auth.dto.UserResponse;
import com.example.acespringbackend.service.GitHubAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ace/auth")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubAuthService authService;

    @PostMapping("/github-login")
    public ResponseEntity<?> loginWithGitHub(@RequestBody GitHubLoginRequest request) {
        try {
            UserResponse user = authService.loginWithGitHub(request.getCode());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
        }
    }
}
