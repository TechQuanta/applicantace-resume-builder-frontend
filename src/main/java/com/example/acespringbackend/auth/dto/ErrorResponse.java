package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for conveying error information in API responses.
 * This class provides a standardized format for communicating error codes and
 * human-readable messages back to the client.
 */
public class ErrorResponse {

    /**
     * A concise error code or identifier, typically representing the type of error
     * (e.g., "INVALID_INPUT", "AUTHENTICATION_FAILED", "RESOURCE_NOT_FOUND").
     */
    private String error;

    /**
     * A human-readable message providing more details about the error.
     * This message can be displayed directly to the user or used for logging.
     */
    private String message;

    /**
     * Constructs a new {@code ErrorResponse} with a specified error code and message.
     *
     * @param error   A string representing the error code.
     * @param message A detailed, human-readable error message.
     */
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    /**
     * Retrieves the error code.
     *
     * @return The error code as a {@link String}.
     */
    public String getError() {
        return error;
    }

    /**
     * Retrieves the error message.
     *
     * @return The error message as a {@link String}.
     */
    public String getMessage() {
        return message;
    }

    // Note: Setters are intentionally omitted for ErrorResponse to promote immutability
    // and ensure that once an error response is created, its content cannot be altered.
    // If mutability is required, add standard setters.
}