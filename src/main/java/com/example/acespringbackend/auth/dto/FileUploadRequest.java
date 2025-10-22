package com.example.acespringbackend.auth.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for encapsulating a file upload request.
 * This DTO is designed to carry the file content, the user's email,
 * and the target folder ID for the upload operation to Google Drive.
 */
public class FileUploadRequest {

    /**
     * The file content itself, received as a {@link MultipartFile} from the client.
     * This holds the binary data of the file to be uploaded.
     */
    private MultipartFile file;

    /**
     * The email address of the user initiating the file upload.
     * This is crucial for authentication, authorization, and associating the
     * uploaded file with the correct user account on the backend.
     */
    private String userEmail;

    /**
     * The Google Drive folder ID where the file should be uploaded.
     * If this is null or empty, the file might be uploaded to the user's root folder
     * or a default application-specific folder.
     */
    private String folderId;

    /**
     * Default no-argument constructor for {@code FileUploadRequest}.
     * This constructor is essential for frameworks like Spring to automatically
     * bind form data or multipart requests to an instance of this object.
     */
    public FileUploadRequest() {
        // Default constructor
    }

    /**
     * Constructs a new {@code FileUploadRequest} with all necessary parameters
     * for a file upload operation.
     *
     * @param file      The {@link MultipartFile} containing the file's binary content.
     * @param userEmail The email address of the user performing the upload.
     * @param folderId  The Google Drive folder ID where the file should be uploaded (can be {@code null}).
     */
    public FileUploadRequest(MultipartFile file, String userEmail, String folderId) {
        this.file = file;
        this.userEmail = userEmail;
        this.folderId = folderId;
    }

    // --- Getters ---

    /**
     * Retrieves the {@link MultipartFile} containing the uploaded file's content.
     *
     * @return The uploaded file.
     */
    public MultipartFile getFile() {
        return file;
    }

    /**
     * Retrieves the email address of the user initiating the upload.
     *
     * @return The user's email address as a {@link String}.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Retrieves the target Google Drive folder ID for the upload.
     *
     * @return The folder ID as a {@link String}, or {@code null} if not specified.
     */
    public String getFolderId() {
        return folderId;
    }

    // --- Setters ---

    /**
     * Sets the {@link MultipartFile} containing the uploaded file's content.
     *
     * @param file The uploaded file to set.
     */
    public void setFile(MultipartFile file) {
        this.file = file;
    }

    /**
     * Sets the email address of the user.
     *
     * @param userEmail The user's email address to set.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Sets the target Google Drive folder ID for the upload.
     *
     * @param folderId The folder ID to set (can be {@code null}).
     */
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }
}