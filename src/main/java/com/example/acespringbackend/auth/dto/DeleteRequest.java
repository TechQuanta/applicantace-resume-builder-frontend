package com.example.acespringbackend.auth.dto;

/**
 * Represents a request to delete a file from Google Drive.
 * This DTO includes the necessary identifiers to specify which file to delete
 * and the user associated with the deletion request for authorization or logging purposes.
 */
public class DeleteRequest {

    /**
     * The unique identifier (ID) of the file in Google Drive that is to be deleted.
     * This ID is crucial for targeting the correct file for removal.
     */
    private String fileId;

    /**
     * The email address of the user initiating the delete request.
     * This is used for user identification, authorization checks (e.g., ensuring
     * the user has permission to delete the file), and for logging purposes.
     */
    private String userEmail;

    /**
     * Default constructor for {@code DeleteRequest}.
     * This constructor is required for frameworks like Spring to deserialize
     * JSON or form data into an instance of this object.
     */
    public DeleteRequest() {
        // Default constructor
    }

    /**
     * Constructs a new {@code DeleteRequest} with the specified file ID and user email.
     * This constructor is used when all necessary parameters are available at the
     * time of object instantiation.
     *
     * @param fileId The Google Drive file ID of the file to be deleted.
     * @param userEmail The email address of the user initiating the deletion.
     */
    public DeleteRequest(String fileId, String userEmail) {
        this.fileId = fileId;
        this.userEmail = userEmail;
    }

    // --- Getters ---

    /**
     * Retrieves the Google Drive file ID.
     *
     * @return The file ID as a String.
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Retrieves the email address of the user initiating the delete request.
     *
     * @return The user's email address as a String.
     */
    public String getUserEmail() {
        return userEmail;
    }

    // --- Setters ---

    /**
     * Sets the Google Drive file ID.
     *
     * @param fileId The file ID to be set.
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Sets the email address of the user.
     *
     * @param userEmail The user's email address to be set.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}