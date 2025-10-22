package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.ForgetPasswordRequest;
import com.example.acespringbackend.auth.dto.PasswordResetRequest;
import com.example.acespringbackend.repository.JwtExpiredTokenRepository;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.service.WebSiteAuth;
import com.example.acespringbackend.utility.JwtUtility;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/ace/auth")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private final WebSiteAuth webSiteAuth;
    private final JwtUtility jwtUtility;
    private final JwtExpiredTokenRepository jwtExpiredTokenRepository;
    private final UserRepository userRepository;

    public PasswordResetController(WebSiteAuth webSiteAuth,
                                   JwtUtility jwtUtility,
                                   JwtExpiredTokenRepository jwtExpiredTokenRepository,
                                   UserRepository userRepository) {
        this.webSiteAuth = webSiteAuth;
        this.jwtUtility = jwtUtility;
        this.jwtExpiredTokenRepository = jwtExpiredTokenRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<String>> forgotPassword(@RequestBody ForgetPasswordRequest request) {
        String email = request.getEmail();
        if (email == null || email.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("Email is required."));
        }
        String resetLinkBase = "http://api.techquanta.tech/ace/auth/reset-password-verify";

        return webSiteAuth.forgotPassword(email, resetLinkBase);
    }

    @GetMapping(value = "/reset-password-verify/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> verifyResetTokenAndLoadPage(@PathVariable("token") String token) {
        if (token == null || token.isEmpty()) {
            log.warn("verifyResetTokenAndLoadPage: Token is missing or malformed.");
            return Mono.just(createInvalidLinkHtml("missing or malformed"));
        }
        log.debug("verifyResetTokenAndLoadPage: Received token {}", token);
        return Mono.just(createLoadingPageHtml(token));
    }


    @GetMapping(value = "/verify-token-ajax", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> verifyTokenAjax(@RequestParam("token") String token) {
        if (token == null || token.isEmpty()) {
            log.warn("verifyTokenAjax: Token is missing.");
            return Mono.just(ResponseEntity.badRequest().body("{\"status\": \"error\", \"message\": \"Token is missing.\" }"));
        }
        log.debug("verifyTokenAjax: Verifying token {}", token);

        // Find the token record from the database
        return jwtExpiredTokenRepository.findByToken(token)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or unknown password reset link.")))
                .flatMap(tokenRecord -> {
                    // Check if it's explicitly marked as used
                    if (tokenRecord.isUsed()) {
                        log.warn("verifyTokenAjax: Token {} found and already used.", token);
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "This password reset link has already been used. Please request a new one."));
                    }

                    // Check server-side expiration (from the database record)
                    if (Instant.now().isAfter(tokenRecord.getExpirationTime())) {
                        log.warn("verifyTokenAjax: Token {} found in DB but is past its recorded expiration.", token);
                        // Do NOT mark as used here. It's truly expired. The /reset-password endpoint will also catch this.
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset link has expired."));
                    }

                    // Extract email from JWT and validate against user
                    String userEmail = jwtUtility.extractUsername(token);
                    if (userEmail == null || userEmail.isEmpty()) {
                        log.warn("verifyTokenAjax: Cannot extract email from token {}. Invalid JWT.", token);
                        // Do NOT mark as used. It's a malformed token.
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset link (missing user information)."));
                    }

                    return userRepository.findByEmail(userEmail)
                            .switchIfEmpty(Mono.defer(() -> {
                                log.warn("verifyTokenAjax: User not found for email extracted from token {}.", userEmail);
                                // Do NOT mark as used. User simply doesn't exist.
                                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User associated with this link not found or invalid."));
                            }))
                            .flatMap(user -> {
                                // Perform intrinsic JWT validation with user details
                                if (!jwtUtility.validateToken(token, user)) {
                                    log.warn("verifyTokenAjax: JWT intrinsic validation or user mismatch failed for token {} and user {}.", token, userEmail);
                                    // Do NOT mark as used. It's an intrinsically invalid/mismatched JWT.
                                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset link expired or invalid (JWT validation failed)."));
                                }

                                // If all checks pass, the token is valid for use.
                                // IMPORTANT: DO NOT mark the token as 'used' or save it here.
                                // It should only be marked 'used' upon successful password reset.
                                log.info("verifyTokenAjax: Token {} successfully verified with user context (ready for use).", token);
                                return Mono.just(ResponseEntity.ok("{\"status\": \"success\", \"message\": \"Token verified.\" }"));
                            });
                })
                .onErrorResume(ResponseStatusException.class, e -> {
                    log.error("verifyTokenAjax: ResponseStatusException caught for token {}: {}", token, e.getMessage());
                    return Mono.just(ResponseEntity.status(e.getStatusCode()).body("{\"status\": \"error\", \"message\": \"" + e.getReason() + "\" }"));
                })
                .onErrorResume(e -> {
                    log.error("verifyTokenAjax: An unexpected error occurred during token verification for token {}: {}", token, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"status\": \"error\", \"message\": \"An internal error occurred during token verification.\" }"));
                });
    }


    @PostMapping("/reset-password")
    public Mono<ResponseEntity<String>> resetPassword(@RequestBody PasswordResetRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            log.warn("resetPassword: Token or new password missing.");
            return Mono.just(ResponseEntity.badRequest().body("Token and new password are required."));
        }
        if (newPassword.length() < 8) {
            log.warn("resetPassword: New password too short.");
            return Mono.just(ResponseEntity.badRequest().body("Password must be at least 8 characters long."));
        }
        log.debug("resetPassword: Attempting password reset for token {}", token);

        // Delegate to webSiteAuth service where the actual password reset and token marking logic should reside
        return webSiteAuth.resetPassword(token, newPassword);
    }

    private String createLoadingPageHtml(String token) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verifying Link...</title>
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
                <script src="https://cdn.tailwindcss.com"></script>
                <style>
                    body {
                        font-family: 'Inter', sans-serif;
                        background: linear-gradient(135deg, #e0f2fe 0%%, #c5e1fd 100%%); /* Light blue gradient */
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        min-height: 100vh;
                        color: #333;
                        overflow: hidden; /* Prevent scrollbar from animation */
                    }

                    /* Background Circles Animation */
                    .bg-circles {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%%;
                        height: 100%%;
                        overflow: hidden;
                        z-index: -1;
                    }

                    .bg-circles li {
                        position: absolute;
                        display: block;
                        list-style: none;
                        width: 20px;
                        height: 20px;
                        background: rgba(255, 255, 255, 0.2);
                        animation: animate 25s linear infinite;
                        bottom: -150px;
                    }

                    .bg-circles li:nth-child(1) {
                        left: 25%%;
                        width: 80px;
                        height: 80px;
                        animation-delay: 0s;
                    }
                    .bg-circles li:nth-child(2) {
                        left: 10%%;
                        width: 20px;
                        height: 20px;
                        animation-delay: 2s;
                        animation-duration: 12s;
                    }
                    .bg-circles li:nth-child(3) {
                        left: 70%%;
                        width: 20px;
                        height: 20px;
                        animation-delay: 4s;
                    }
                    .bg-circles li:nth-child(4) {
                        left: 40%%;
                        width: 60px;
                        height: 60px;
                        animation-delay: 0s;
                        animation-duration: 18s;
                    }
                    .bg-circles li:nth-child(5) {
                        left: 65%%;
                        width: 20px;
                        height: 20px;
                        animation-delay: 0s;
                    }
                    .bg-circles li:nth-child(6) {
                        left: 75%%;
                        width: 110px;
                        height: 110px;
                        animation-delay: 3s;
                    }
                    .bg-circles li:nth-child(7) {
                        left: 35%%;
                        width: 150px;
                        height: 150px;
                        animation-delay: 7s;
                    }
                    .bg-circles li:nth-child(8) {
                        left: 50%%;
                        width: 25px;
                        height: 25px;
                        animation-delay: 15s;
                        animation-duration: 45s;
                    }
                    .bg-circles li:nth-child(9) {
                        left: 20%%;
                        width: 15px;
                        height: 15px;
                        animation-delay: 2s;
                        animation-duration: 35s;
                    }
                    .bg-circles li:nth-child(10) {
                        left: 85%%;
                        width: 150px;
                        height: 150px;
                        animation-delay: 0s;
                        animation-duration: 11s;
                    }

                    @keyframes animate {
                        0%% {
                            transform: translateY(0) rotate(0deg);
                            opacity: 1;
                            border-radius: 0;
                        }
                        100%% {
                            transform: translateY(-1000px) rotate(720deg);
                            opacity: 0;
                            border-radius: 50%%;
                        }
                    }


                    .card-container {
                        background-color: #ffffff;
                        padding: 2.5rem;
                        border-radius: 0.75rem;
                        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1); /* Softer shadow */
                        width: 100%%;
                        max-width: 480px; /* Slightly wider for better forms */
                        text-align: center;
                        position: relative;
                        z-index: 10;
                        animation: fadeIn 0.8s ease-out forwards;
                    }

                    @keyframes fadeIn {
                        from { opacity: 0; transform: translateY(20px); }
                        to { opacity: 1; transform: translateY(0); }
                    }

                    .loader {
                        border: 8px solid rgba(0, 0, 0, 0.1);
                        border-top: 8px solid #3b82f6; /* Tailwind blue-500 */
                        border-radius: 50%%;
                        width: 70px; /* Slightly larger */
                        height: 70px;
                        animation: spin 1s linear infinite, pulse 1.5s infinite alternate; /* Spin and subtle pulse */
                        margin-bottom: 1.5rem;
                    }
                    @keyframes spin {
                        0%% { transform: rotate(0deg); }
                        100%% { transform: rotate(360deg); }
                    }
                    @keyframes pulse {
                        0%% { transform: scale(1); }
                        100%% { transform: scale(1.05); }
                    }

                    h2.title {
                        font-size: 2.25rem; /* Larger title */
                        font-weight: 700; /* Bold */
                        color: #1a202c; /* Darker text */
                        margin-bottom: 0.75rem;
                    }

                    p.subtitle {
                        font-size: 1.125rem; /* Larger subtitle */
                        color: #4a5568; /* Medium text */
                        margin-bottom: 2rem;
                    }

                    /* Form specific styles */
                    .form-group {
                        margin-bottom: 1rem;
                        text-align: left;
                    }
                    .form-label {
                        display: block;
                        font-size: 0.95rem;
                        font-weight: 600;
                        color: #2d3748;
                        margin-bottom: 0.5rem;
                    }
                    .form-input {
                        width: 100%%;
                        padding: 0.75rem 1rem;
                        border: 1px solid #cbd5e0; /* Light border */
                        border-radius: 0.375rem;
                        font-size: 1rem;
                        transition: all 0.2s ease-in-out;
                        box-shadow: inset 0 1px 2px rgba(0,0,0,0.05);
                    }
                    .form-input:focus {
                        outline: none;
                        border-color: #3b82f6; /* Blue on focus */
                        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25); /* Focus ring */
                    }

                    .submit-button {
                        width: 100%%;
                        padding: 0.8rem 1.5rem;
                        background-image: linear-gradient(to right, #3b82f6 0%%, #2563eb 100%%); /* Blue gradient button */
                        color: white;
                        font-weight: 700;
                        border-radius: 0.375rem;
                        border: none;
                        cursor: pointer;
                        transition: all 0.3s ease;
                        box-shadow: 0 4px 10px rgba(59, 130, 246, 0.3);
                    }
                    .submit-button:hover {
                        background-image: linear-gradient(to right, #2563eb 0%%, #1e40af 100%%);
                        transform: translateY(-2px);
                        box-shadow: 0 6px 15px rgba(59, 130, 246, 0.4);
                    }
                    .submit-button:active {
                        transform: translateY(0);
                        box-shadow: 0 2px 5px rgba(59, 130, 246, 0.2);
                    }

                    .message-div {
                        margin-top: 1rem;
                        font-size: 0.95rem;
                        padding: 0.75rem;
                        border-radius: 0.375rem;
                    }
                    .message-div.text-green-500 {
                        background-color: #d1fae5; /* Light green */
                        color: #065f46; /* Dark green */
                    }
                    .message-div.text-red-500 {
                        background-color: #fee2e2; /* Light red */
                        color: #991b1b; /* Dark red */
                    }
                    .message-div.text-blue-500 {
                        background-color: #e0f2fe; /* Light blue */
                        color: #1e40af; /* Dark blue */
                    }
                </style>
            </head>
            <body>
                <ul class="bg-circles">
                    <li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li>
                </ul>

                <div id="loadingContainer" class="card-container">
                    <div class="loader"></div>
                    <h2 class="title">Verifying Password Reset Link...</h2>
                    <p class="subtitle">Please wait, this may take a moment.</p>
                </div>

                <div id="passwordFormContainer" class="hidden card-container">
                    <h2 class="title">Reset Your Password</h2>
                    <form id="resetPasswordForm">
                        <input type="hidden" id="token" name="token" value="%s">
                        <div class="form-group">
                            <label for="newPassword" class="form-label">New Password</label>
                            <input type="password" id="newPassword" name="newPassword" class="form-input"
                                   required minlength="8" placeholder="Enter your new password">
                        </div>
                        <div class="form-group">
                            <label for="confirmPassword" class="form-label">Confirm Password</label>
                            <input type="password" id="confirmPassword" name="confirmPassword" class="form-input"
                                   required minlength="8" placeholder="Confirm your new password">
                        </div>
                        <button type="submit" class="submit-button">
                            Reset Password
                        </button>
                    </form>
                    <div id="message" class="message-div hidden"></div>
                </div>

                <div id="errorContainer" class="hidden card-container">
                    <h2 id="errorTitle" class="title text-red-600"></h2>
                    <p id="errorMessage" class="subtitle mb-4"></p>
                    <a href="/forgot-password-frontend" class="text-blue-600 hover:underline font-medium">Request a new password reset link</a>
                </div>

                <script>
                    document.addEventListener('DOMContentLoaded', async function() {
                        const token = document.getElementById('token').value;
                        const loadingContainer = document.getElementById('loadingContainer');
                        const passwordFormContainer = document.getElementById('passwordFormContainer');
                        const errorContainer = document.getElementById('errorContainer');
                        const errorTitle = document.getElementById('errorTitle');
                        const errorMessage = document.getElementById('errorMessage');
                        const messageDiv = document.getElementById('message');

                        try {
                            const response = await fetch('/ace/auth/verify-token-ajax?token=' + encodeURIComponent(token));
                            const data = await response.json();

                            loadingContainer.classList.add('hidden'); // Hide loading elements

                            if (data.status === 'success') {
                                passwordFormContainer.classList.remove('hidden'); // Show password form
                            } else {
                                errorContainer.classList.remove('hidden'); // Show error container
                                errorTitle.textContent = 'Password Reset Link Invalid';
                                errorMessage.textContent = data.message;
                            }
                        } catch (error) {
                            console.error('Error during token verification:', error);
                            loadingContainer.classList.add('hidden'); // Hide loading elements

                            errorContainer.classList.remove('hidden'); // Show error container
                            errorTitle.textContent = 'An Error Occurred';
                            errorMessage.textContent = 'Could not verify the password reset link. Please try again or request a new one.';
                        }

                        // Attach event listener for the password reset form AFTER it might be shown
                        document.getElementById('resetPasswordForm').addEventListener('submit', async function(event) {
                            event.preventDefault();

                            const token = document.getElementById('token').value;
                            const newPasswordInput = document.getElementById('newPassword');
                            const confirmPasswordInput = document.getElementById('confirmPassword');
                            const newPassword = newPasswordInput.value;
                            const confirmPassword = confirmPasswordInput.value;

                            messageDiv.classList.remove('hidden', 'text-green-500', 'text-red-500', 'text-blue-500'); // Clean previous states

                            if (newPassword !== confirmPassword) {
                                messageDiv.innerHTML = 'Passwords do not match!';
                                messageDiv.classList.add('text-red-500');
                                newPasswordInput.focus(); // Keep focus for correction
                                return;
                            }
                            if (newPassword.length < 8) {
                                messageDiv.innerHTML = 'Password must be at least 8 characters long!';
                                messageDiv.classList.add('text-red-500');
                                newPasswordInput.focus(); // Keep focus for correction
                                return;
                            }

                            messageDiv.innerHTML = 'Resetting password...';
                            messageDiv.classList.add('text-blue-500');


                            try {
                                const response = await fetch('/ace/auth/reset-password', {
                                    method: 'POST',
                                    headers: {
                                        'Content-Type': 'application/json'
                                    },
                                    body: JSON.stringify({ token: token, newPassword: newPassword })
                                });

                                // Ensure to always parse JSON if the backend consistently returns JSON
                                const data = await response.json(); // This assumes your backend always returns JSON

                                if (response.ok) {
                                    messageDiv.innerHTML = data.message || 'Password reset successfully!';
                                    messageDiv.classList.remove('text-blue-500', 'text-red-500');
                                    messageDiv.classList.add('text-green-500');
                                    document.getElementById('resetPasswordForm').reset(); // Clear form
                                    setTimeout(() => {
                                        window.location.href = 'https://aceresume.techquanta.tech/SignUp'; // Redirect to login
                                    }, 2000);
                                } else {
                                    // If not response.ok, data.message will contain the error message from the service
                                    messageDiv.innerHTML = data.message || 'Failed to reset password.';
                                    messageDiv.classList.remove('text-blue-500', 'text-green-500');
                                    messageDiv.classList.add('text-red-500');
                                }
                            } catch (error) {
                                console.error('Error during password reset:', error);
                                messageDiv.innerHTML = 'An error occurred. Please try again.';
                                messageDiv.classList.remove('text-blue-500', 'text-green-500');
                                messageDiv.classList.add('text-red-500');
                            }
                        });
                    });
                </script>
            </body>
            </html>
            """, token);
    }

    private String createInvalidLinkHtml(String reason) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Invalid Link</title>
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
                <script src="https://cdn.tailwindcss.com"></script>
                <style>
                    body {
                        font-family: 'Inter', sans-serif;
                        background: linear-gradient(135deg, #e0f2fe 0%%, #c5e1fd 100%%);
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        min-height: 100vh;
                        color: #333;
                    }
                    .card-container {
                        background-color: #ffffff;
                        padding: 2.5rem;
                        border-radius: 0.75rem;
                        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
                        width: 100%%;
                        max-width: 480px;
                        text-align: center;
                    }
                    .title {
                        font-size: 2.25rem;
                        font-weight: 700;
                        color: #dc2626; /* Red-600 */
                        margin-bottom: 0.75rem;
                    }
                    .subtitle {
                        font-size: 1.125rem;
                        color: #4a5568;
                        margin-bottom: 2rem;
                    }
                    .link {
                        color: #2563eb; /* Blue-700 */
                        text-decoration: none;
                        font-weight: 500;
                    }
                    .link:hover {
                        text-decoration: underline;
                    }
                </style>
            </head>
            <body>
                <div class="card-container">
                    <h2 class="title">Invalid or Expired Link</h2>
                    <p class="subtitle">The password reset link is %s or has expired. Please request a new one.</p>
                    <a href="/forgot-password-frontend" class="link">Request a new password reset link</a>
                </div>
            </body>
            </html>
        """.replace("%s", reason));
    }
}