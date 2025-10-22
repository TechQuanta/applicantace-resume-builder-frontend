package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for requests to verify a One-Time Password (OTP).
 * This DTO encapsulates the user's email address and the OTP they have provided
 * for verification.
 */
public class OtpVerificationRequest {

    /**
     * The email address of the user who is attempting to verify an OTP.
     * This email is used to identify the correct OTP to validate against.
     */
    private String email;

    /**
     * The One-Time Password (OTP) provided by the user for verification.
     * This string will be compared against the stored OTP for the given email.
     */
    private String otp;

    /**
     * Default no-argument constructor for {@code OtpVerificationRequest}.
     * This constructor is essential for frameworks like Spring to automatically
     * deserialize JSON or form data into an instance of this object.
     */
    public OtpVerificationRequest() {
        // Default constructor for deserialization
    }

    /**
     * Constructs a new {@code OtpVerificationRequest} with the specified email and OTP.
     * This constructor is used for direct instantiation when both pieces of information are available.
     *
     * @param email The email address associated with the OTP.
     * @param otp   The OTP string to be verified.
     */
    public OtpVerificationRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    // --- Getters ---

    /**
     * Retrieves the email address from the verification request.
     *
     * @return The email address as a {@link String}.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retrieves the OTP from the verification request.
     *
     * @return The OTP as a {@link String}.
     */
    public String getOtp() {
        return otp;
    }

    // --- Setters ---

    /**
     * Sets the email address for the verification request.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the OTP for the verification request.
     *
     * @param otp The OTP string to set.
     */
    public void setOtp(String otp) {
        this.otp = otp;
    }
}