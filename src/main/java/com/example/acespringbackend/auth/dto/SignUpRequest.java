package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for encapsulating user registration (sign-up) requests.
 * This DTO carries the necessary information from the frontend to the backend
 * to create a new user account.
 */
public class SignUpRequest {

    /**
     * The desired username for the new account. This is typically a unique identifier
     * chosen by the user, distinct from their email.
     */
    private String username;

    /**
     * The user's email address, which often serves as a primary contact and login identifier.
     */
    private String email;

    /**
     * The password chosen by the user for their new account. This sensitive field
     * should be securely hashed on the backend before storage.
     */
    private String password;

    /**
     * The user's full name, used for display purposes.
     */
    private String name;

    /**
     * An optional URL to the user's LinkedIn profile. This can be used for
     * professional networking or profile enrichment.
     */
    private String linkedinProfileUrl;

    /**
     * Default no-argument constructor for {@code SignUpRequest}.
     * This constructor is essential for frameworks like Spring to properly
     * deserialize JSON or form data into an instance of this object.
     */
    public SignUpRequest() {
        // Default constructor for deserialization
    }

    /**
     * Constructs a new {@code SignUpRequest} with all necessary user registration details.
     *
     * @param username         The desired username.
     * @param email            The user's email address.
     * @param password         The chosen password.
     * @param name             The user's full name.
     * @param linkedinProfileUrl An optional LinkedIn profile URL.
     */
    public SignUpRequest(String username, String email, String password, String name, String linkedinProfileUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.linkedinProfileUrl = linkedinProfileUrl;
    }

    // --- Getters ---

    /**
     * Retrieves the username provided in the sign-up request.
     *
     * @return The username as a {@link String}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the email address provided in the sign-up request.
     *
     * @return The email address as a {@link String}.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retrieves the password provided in the sign-up request.
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

    /**
     * Retrieves the user's full name provided in the sign-up request.
     *
     * @return The full name as a {@link String}.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the LinkedIn profile URL provided in the sign-up request.
     *
     * @return The LinkedIn profile URL as a {@link String}, or {@code null} if not provided.
     */
    public String getLinkedinProfileUrl() {
        return linkedinProfileUrl;
    }

    // --- Setters ---

    /**
     * Sets the username for the sign-up request.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the email address for the sign-up request.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the password for the sign-up request.
     *
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the user's full name for the sign-up request.
     *
     * @param name The full name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the LinkedIn profile URL for the sign-up request.
     *
     * @param linkedinProfileUrl The LinkedIn profile URL to set.
     */
    public void setLinkedinProfileUrl(String linkedinProfileUrl) {
        this.linkedinProfileUrl = linkedinProfileUrl;
    }

    /**
     * Provides a string representation of the {@code SignUpRequest} object.
     * For security reasons, the {@code password} field is masked to prevent
     * accidental logging of sensitive information.
     *
     * @return A string containing the user's sign-up details with a masked password.
     */
    @Override
    public String toString() {
        return "SignUpRequest{" +
               "username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", password='" + "[PROTECTED]" + '\'' + // Mask password for security in toString
               ", name='" + name + '\'' +
               ", linkedinProfileUrl='" + linkedinProfileUrl + '\'' +
               '}';
    }
}