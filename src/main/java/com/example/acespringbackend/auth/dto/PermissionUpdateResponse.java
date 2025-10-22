package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for conveying the response after a file permission update
 * operation on Google Drive. This simplified DTO provides basic feedback on the
 * success or failure of the operation with a descriptive message.
 */
public class PermissionUpdateResponse {

    /**
     * A boolean flag indicating whether the permission update operation was successful.
     * {@code true} if the permission was successfully updated, {@code false} otherwise.
     */
    private boolean success;

    /**
     * A descriptive message providing context about the permission update's outcome,
     * such as success confirmation or a detailed error message if the operation failed.
     */
    private String message;

    /**
     * Default no-argument constructor for {@code PermissionUpdateResponse}.
     * This constructor is necessary for frameworks like Spring to deserialize JSON
     * or form data into an instance of this object.
     */
    public PermissionUpdateResponse() {
        // Default constructor
    }

    /**
     * Constructs a new {@code PermissionUpdateResponse} with the specified success status and message.
     * This constructor is used to provide a direct response to the client after a permission update attempt.
     *
     * @param success A boolean indicating if the permission update was successful.
     * @param message A descriptive message about the operation's outcome.
     */
    public PermissionUpdateResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // --- Getters ---

    /**
     * Retrieves the success status of the permission update operation.
     *
     * @return {@code true} if the update succeeded, {@code false} otherwise.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Retrieves the message related to the permission update operation's outcome.
     *
     * @return A descriptive message.
     */
    public String getMessage() {
        return message;
    }

    // --- Setters ---

    /**
     * Sets the success status of the permission update operation.
     *
     * @param success {@code true} for success, {@code false} for failure.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Sets the message related to the permission update operation.
     *
     * @param message A descriptive message.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}