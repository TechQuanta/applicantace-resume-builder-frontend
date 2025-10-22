package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for requests related to a "Forgot Password" operation.
 * This DTO typically carries the user's email address to initiate the password reset process.
 */
public class ForgetPasswordRequest {

    /**
     * The email address associated with the user account for which the password
     * reset is being requested. This is the primary identifier for the user
     * in the forget password flow.
     */
    private String email;

    /**
     * Default no-argument constructor for {@code ForgetPasswordRequest}.
     * This constructor is necessary for frameworks like Spring to properly
     * deserialize JSON or form data into an instance of this object.
     */
    public ForgetPasswordRequest() {
        // Default constructor for deserialization
    }

    /**
     * Constructs a new {@code ForgetPasswordRequest} with the specified email address.
     *
     * @param email The email address for the password reset request.
     */
    public ForgetPasswordRequest(String email) {
        this.email = email;
    }

    // --- Getter ---

    /**
     * Retrieves the email address associated with the forget password request.
     *
     * @return The email address as a {@link String}.
     */
    public String getEmail() {
        return email;
    }

    // --- Setter ---

    /**
     * Sets the email address for the forget password request.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }
}