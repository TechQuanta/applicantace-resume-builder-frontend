package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for encapsulating a password reset request.
 * This DTO is used to convey the unique reset token and the user's new password
 * from the frontend to the backend during the password reset process.
 */
public class PasswordResetRequest {

    /**
     * The unique token issued by the backend for password reset verification.
     * This token is typically sent to the user via email and must be provided
     * by the user to authorize the password change.
     */
    private String token;

    /**
     * The new password that the user wishes to set for their account.
     * This sensitive field should be handled securely (e.g., hashed) on the backend.
     */
    private String newPassword;

    /**
     * Default no-argument constructor for {@code PasswordResetRequest}.
     * This constructor is necessary for frameworks like Spring to properly
     * deserialize JSON or form data into an instance of this object.
     */
    public PasswordResetRequest() {
        // Default constructor for deserialization
    }

    /**
     * Constructs a new {@code PasswordResetRequest} with the specified reset token and new password.
     *
     * @param token       The unique password reset token.
     * @param newPassword The new password to be set.
     */
    public PasswordResetRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    // --- Getters ---

    /**
     * Retrieves the password reset token.
     *
     * @return The reset token as a {@link String}.
     */
    public String getToken() {
        return token;
    }

    /**
     * Retrieves the new password.
     * <p>
     * WARNING: This method directly exposes the new password. Use with extreme caution
     * and only when absolutely necessary (e.g., during the hashing process before storage).
     * Avoid logging or transmitting this directly.
     *
     * @return The new password as a {@link String}.
     */
    public String getNewPassword() {
        return newPassword;
    }

    // --- Setters ---

    /**
     * Sets the password reset token.
     *
     * @param token The reset token to set.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Sets the new password.
     *
     * @param newPassword The new password to set.
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * Provides a string representation of the {@code PasswordResetRequest} object.
     * For security reasons, the {@code newPassword} field is masked to prevent
     * accidental logging of sensitive information.
     *
     * @return A string containing the token and a masked representation of the new password.
     */
    @Override
    public String toString() {
        // For production logging, consider masking the token as well if it's sensitive,
        // or just showing a short prefix. For this DTO, the new password is the most sensitive.
        return "PasswordResetRequest{" +
               "token='" + token + '\'' +
               ", newPassword='" + "[PROTECTED]" + '\'' + // Mask newPassword for security in toString
               '}';
    }
}