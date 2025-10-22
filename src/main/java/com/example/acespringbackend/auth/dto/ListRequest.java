package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for encapsulating a request to list files from Google Drive.
 * This DTO specifies the target folder and the user associated with the request
 * to enable proper file retrieval and authorization.
 */
public class ListRequest {

    /**
     * The unique identifier (Folder ID) of the Google Drive folder from which
     * to list files. If this is null or empty, it might imply listing files
     * from the user's root directory or a default application folder.
     */
    private String folderId;

    /**
     * The email address of the user making the file listing request.
     * This is essential for authentication, authorization checks (to ensure the
     * user has permissions to access the specified folder), and for auditing.
     */
    private String userEmail;

    /**
     * Default no-argument constructor for {@code ListRequest}.
     * This constructor is crucial for deserialization frameworks (like Spring)
     * to automatically map JSON or form data into an instance of this object.
     */
    public ListRequest() {
        // Default constructor
    }

    /**
     * Constructs a new {@code ListRequest} with the specified folder ID and user email.
     * This constructor allows for direct instantiation with all required parameters.
     *
     * @param folderId  The ID of the Google Drive folder from which to list files (can be {@code null}).
     * @param userEmail The email address of the user making the request.
     */
    public ListRequest(String folderId, String userEmail) {
        this.folderId = folderId;
        this.userEmail = userEmail;
    }

    // --- Getters ---

    /**
     * Retrieves the Google Drive folder ID from which to list files.
     *
     * @return The folder ID as a {@link String}, or {@code null} if not specified.
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Retrieves the email address of the user making the request.
     *
     * @return The user's email address as a {@link String}.
     */
    public String getUserEmail() {
        return userEmail;
    }

    // --- Setters ---

    /**
     * Sets the Google Drive folder ID from which to list files.
     *
     * @param folderId The folder ID to set (can be {@code null}).
     */
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    /**
     * Sets the email address of the user making the request.
     *
     * @param userEmail The user's email address to set.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}