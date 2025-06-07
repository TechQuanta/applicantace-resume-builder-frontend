package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.ForgetPasswordRequest;
import com.example.acespringbackend.auth.dto.PasswordResetRequest;
import com.example.acespringbackend.service.WebSiteAuth;
import com.example.acespringbackend.utility.JwtUtility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ace/auth")
public class PasswordResetController {

    private final WebSiteAuth webSiteAuth;
    private final JwtUtility jwtUtility;

    public PasswordResetController(WebSiteAuth webSiteAuth, JwtUtility jwtUtility) {
        this.webSiteAuth = webSiteAuth;
        this.jwtUtility = jwtUtility;
    }

    /**
     * Endpoint for users to request a password reset link.
     * @param request The request body containing the user's email.
     * @return Mono<ResponseEntity<String>> indicating success or failure.
     */
    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<String>> forgotPassword(@RequestBody ForgetPasswordRequest request) {
        String email = request.getEmail();
        if (email == null || email.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("Email is required."));
        }
        String resetLinkBase = "http://localhost:8081/ace/auth/reset-password-page";

        return webSiteAuth.forgotPassword(email, resetLinkBase)
                .thenReturn(ResponseEntity.ok("Password reset link sent to your email."))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    /**
     * Endpoint to serve the HTML page for password reset.
     * This page will contain a form for the user to enter their new password.
     * Now includes token expiration check upon page load.
     * @param token The JWT token from the password reset email link.
     * @return HTML content for the password reset form or an error page.
     */
    @GetMapping(value = "/reset-password-page", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> showResetPasswordPage(@RequestParam("token") String token) {
        if (token == null || token.isEmpty()) {
            return Mono.just("""
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Invalid Link</title>
                        <script src="https://cdn.tailwindcss.com"></script>
                        <style>body { font-family: 'Inter', sans-serif; background-color: #f3f4f6; }</style>
                    </head>
                    <body class="flex items-center justify-center min-h-screen">
                        <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
                            <h2 class="text-2xl font-bold mb-4 text-red-600">Invalid Reset Link</h2>
                            <p class="text-gray-700 mb-6">The password reset link is missing or malformed.</p>
                            <a href="/forgot-password-frontend" class="text-blue-600 hover:underline">Request a new password reset link</a>
                        </div>
                    </body>
                    </html>
                    """);
        }

        if (!jwtUtility.validateToken(token)) {
            return Mono.just("""
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Link Expired</title>
                        <script src="https://cdn.tailwindcss.com"></script>
                        <style>body { font-family: 'Inter', sans-serif; background-color: #f3f4f6; }</style>
                    </head>
                    <body class="flex items-center justify-center min-h-screen">
                        <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
                            <h2 class="text-2xl font-bold mb-4 text-red-600">Password Reset Link Expired or Invalid</h2>
                            <p class="text-gray-700 mb-6">This link has either expired or is no longer valid. Please request a new one.</p>
                            <a href="/forgot-password-frontend" class="text-blue-600 hover:underline">Request a new password reset link</a>
                        </div>
                    </body>
                    </html>
                    """);
        }

        String htmlContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Your Password</title>
                <script src="https://cdn.tailwindcss.com"></script>
                <style>
                    body {
                        font-family: 'Inter', sans-serif;
                        background-color: #f3f4f6;
                    }
                </style>
            </head>
            <body class="flex items-center justify-center min-h-screen">
                <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
                    <h2 class="text-2xl font-bold mb-6 text-center text-gray-800">Reset Your Password</h2>
                    <form id="resetPasswordForm" class="space-y-4">
                        <input type="hidden" id="token" name="token" value="%s">
                        <div>
                            <label for="newPassword" class="block text-sm font-medium text-gray-700">New Password</label>
                            <input type="password" id="newPassword" name="newPassword"
                                   class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                   required minlength="8">
                        </div>
                        <div>
                            <label for="confirmPassword" class="block text-sm font-medium text-gray-700">Confirm Password</label>
                            <input type="password" id="confirmPassword" name="confirmPassword"
                                   class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                   required minlength="8">
                        </div>
                        <button type="submit"
                                class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                            Reset Password
                        </button>
                    </form>
                    <div id="message" class="mt-4 text-center"></div>
                </div>

                <script>
                    document.getElementById('resetPasswordForm').addEventListener('submit', async function(event) {
                        event.preventDefault();

                        const token = document.getElementById('token').value;
                        const newPassword = document.getElementById('newPassword').value;
                        const confirmPassword = document.getElementById('confirmPassword').value;
                        const messageDiv = document.getElementById('message');

                        if (newPassword !== confirmPassword) {
                            messageDiv.innerHTML = '<p class="text-red-500">Passwords do not match!</p>';
                            return;
                        }
                        if (newPassword.length < 8) {
                            messageDiv.innerHTML = '<p class="text-red-500">Password must be at least 8 characters long!</p>';
                            return;
                        }

                        messageDiv.innerHTML = '<p class="text-blue-500">Resetting password...</p>';

                        try {
                            const response = await fetch('/ace/auth/reset-password', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json'
                                },
                                body: JSON.stringify({ token, newPassword })
                            });

                            const text = await response.text();

                            if (response.ok) {
                                messageDiv.innerHTML = '<p class="text-green-500">' + text + '</p>';
                                setTimeout(() => {
                                    window.location.href = '/login-frontend';
                                }, 3000);
                            } else {
                                messageDiv.innerHTML = '<p class="text-red-500">' + text + '</p>';
                                setTimeout(() => {
                                    window.location.href = '/forgot-password-frontend';
                                }, 3000);
                            }
                        } catch (error) {
                            console.error('Error:', error);
                            messageDiv.innerHTML = '<p class="text-red-500">An error occurred while resetting password.</p>';
                        }
                    });
                </script>
            </body>
            </html>
            """;
        return Mono.just(String.format(htmlContent, token));
    }

    /**
     * Endpoint to handle the submission of the new password.
     * This method now performs token validation and email extraction.
     * @param request The request body containing the token and the new password.
     * @return Mono<ResponseEntity<String>> indicating success or failure.
     */
    @PostMapping("/reset-password")
    public Mono<ResponseEntity<String>> resetPassword(@RequestBody PasswordResetRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("Token and new password are required."));
        }
        if (newPassword.length() < 8) {
            return Mono.just(ResponseEntity.badRequest().body("Password must be at least 8 characters long."));
        }

        if (!jwtUtility.validateToken(token)) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid or expired password reset token."));
        }

        String email = jwtUtility.extractUsername(token);
        if (email == null || email.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid password reset token structure (email missing)."));
        }

        return webSiteAuth.resetPassword(token, email, newPassword)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }
}
