package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for encapsulating a request to rename a file on Google Drive.
 * This DTO contains all necessary information to identify the file and its new desired name.
 */
public class FileRenameRequest {

    /**
     * The email address of the user initiating the file rename request.
     * This is essential for authentication, authorization checks (to ensure the user
     * has permissions to rename the file), and for auditing purposes.
     */
    private String userEmail;

    /**
     * The unique identifier (File ID) of the file on Google Drive that is to be renamed.
     * This ID precisely targets the file to be modified.
     */
    private String fileId;

    /**
     * The new desired name for the file, including its extension (e.g., "updated_document.pdf").
     */
    private String newFileName;

    /**
     * Default no-argument constructor for {@code FileRenameRequest}.
     * This constructor is necessary for frameworks like Spring to automatically
     * deserialize JSON or form data into an instance of this object.
     */
    public FileRenameRequest() {
        // Default constructor
    }

    /**
     * Constructs a new {@code FileRenameRequest} with the specified user email, file ID, and new file name.
     * This constructor allows for direct instantiation with all required parameters.
     *
     * @param userEmail   The email address of the user.
     * @param fileId      The Google Drive file ID of the file to rename.
     * @param newFileName The new name for the file.
     */
    public FileRenameRequest(String userEmail, String fileId, String newFileName) {
        this.userEmail = userEmail;
        this.fileId = fileId;
        this.newFileName = newFileName;
    }

    // --- Getters ---

    /**
     * Retrieves the email address of the user initiating the rename request.
     *
     * @return The user's email address as a {@link String}.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Retrieves the Google Drive file ID of the file to be renamed.
     *
     * @return The file ID as a {@link String}.
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Retrieves the new desired name for the file.
     *
     * @return The new file name as a {@link String}.
     */
    public String getNewFileName() {
        return newFileName;
    }

    // --- Setters ---

    /**
     * Sets the email address of the user.
     *
     * @param userEmail The user's email address to set.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Sets the Google Drive file ID of the file to be renamed.
     *
     * @param fileId The file ID to set.
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Sets the new desired name for the file.
     *
     * @param newFileName The new file name to set.
     */
    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }
}