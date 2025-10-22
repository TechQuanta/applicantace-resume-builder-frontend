package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for encapsulating user login credentials.
 * This DTO is used to receive the user's email and password from the frontend
 * for authentication purposes.
 */
public class LoginRequest {

    /**
     * The email address provided by the user for login. This acts as the
     * primary identifier for the user account.
     */
    private String email;

    /**
     * The password provided by the user for login. This field should be handled
     * with utmost care and never exposed in logs or insecure channels.
     */
    private String password;

    /**
     * Default no-argument constructor for {@code LoginRequest}.
     * This constructor is necessary for frameworks like Spring to properly
     * deserialize JSON or form data into an instance of this object.
     */
    public LoginRequest() {
        // Default constructor for deserialization
    }

    /**
     * Constructs a new {@code LoginRequest} with the specified email and password.
     *
     * @param email    The email address of the user.
     * @param password The password provided by the user.
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getters ---

    /**
     * Retrieves the email address provided for login.
     *
     * @return The email address as a {@link String}.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retrieves the password provided for login.
     * <p>
     * WARNING: This method directly exposes the password. Use with extreme caution
     * and only when absolutely necessary (e.g., during the hashing process before storage).
     * Avoid logging or transmitting this directly.
     *
     * @return The password as a {@link String}.
     */
    public String getPassword() {
        return password;
    }

    // --- Setters ---

    /**
     * Sets the email address for the login request.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the password for the login request.
     *
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Provides a string representation of the {@code LoginRequest} object.
     * For security reasons, the password field is masked to prevent accidental
     * logging of sensitive information.
     *
     * @return A string containing the email and a masked representation of the password.
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
               "email='" + email + '\'' +
               ", password='" + "[PROTECTED]" + '\'' + // Mask password for security in toString
               '}';
    }
}