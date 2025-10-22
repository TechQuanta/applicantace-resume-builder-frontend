package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for conveying the response after a file upload operation to Google Drive.
 * This DTO provides feedback on the success or failure of the upload, details about the
 * newly uploaded file, and updated user storage metrics.
 */
public class FileUploadResponse {

    /**
     * A boolean flag indicating whether the file upload operation was successful.
     * {@code true} if the file was successfully uploaded, {@code false} otherwise.
     */
    private Boolean success;

    /**
     * A descriptive message providing context about the upload operation's outcome,
     * such as success confirmation or a detailed error message.
     */
    private String message;

    /**
     * The original name of the file as provided by the client during the upload.
     */
    private String fileName;

    /**
     * The unique identifier (File ID) assigned to the uploaded file by Google Drive.
     * This ID is essential for subsequent interactions with the file.
     */
    private String driveFileId;

    /**
     * The MIME type (content type) of the uploaded file (e.g., "application/pdf", "image/png").
     */
    private String fileMimeType;

    /**
     * The user's current Google Drive storage usage in megabytes (MB)
     * after the file upload operation. This provides updated storage status.
     */
    private double currentStorageUsageMb;

    /**
     * The maximum Google Drive storage quota available to the user in megabytes (MB).
     * This helps the client application display storage limits.
     */
    private double maxStorageQuotaMb;

    /**
     * Default no-argument constructor for {@code FileUploadResponse}.
     * This constructor is necessary for frameworks like Spring to deserialize JSON
     * or form data into an instance of this object.
     */
    public FileUploadResponse() {
        // Default constructor
    }

    /**
     * Constructs a new {@code FileUploadResponse} with complete information about the upload outcome.
     * This constructor is used to provide a detailed response to the client after a file upload attempt.
     *
     * @param success             A boolean indicating if the upload was successful.
     * @param message             A descriptive message about the operation's outcome.
     * @param fileName            The original name of the uploaded file.
     * @param driveFileId         The Google Drive file ID assigned to the uploaded file.
     * @param fileMimeType        The MIME type of the uploaded file.
     * @param currentStorageUsageMb The user's updated current storage usage in MB.
     * @param maxStorageQuotaMb   The user's maximum storage quota in MB.
     */
    public FileUploadResponse(Boolean success, String message, String fileName, String driveFileId, String fileMimeType, double currentStorageUsageMb, double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.fileName = fileName;
        this.driveFileId = driveFileId;
        this.fileMimeType = fileMimeType;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // --- Getters ---

    /**
     * Retrieves the success status of the upload operation.
     *
     * @return {@code true} if the upload succeeded, {@code false} otherwise.
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Retrieves the message related to the upload operation's outcome.
     *
     * @return A descriptive message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the original name of the uploaded file from the client.
     *
     * @return The original file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Retrieves the Google Drive file ID assigned to the uploaded file.
     *
     * @return The Google Drive file ID.
     */
    public String getDriveFileId() {
        return driveFileId;
    }

    /**
     * Retrieves the MIME type of the uploaded file.
     *
     * @return The file MIME type.
     */
    public String getFileMimeType() {
        return fileMimeType;
    }

    /**
     * Retrieves the user's current storage usage after the upload.
     *
     * @return The current storage usage in megabytes (MB).
     */
    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    /**
     * Retrieves the user's maximum allowed storage quota.
     *
     * @return The maximum storage quota in megabytes (MB).
     */
    public double getMaxStorageQuotaMb() {
        return maxStorageQuotaMb;
    }

    // --- Setters ---

    /**
     * Sets the success status of the upload operation.
     *
     * @param success {@code true} for success, {@code false} for failure.
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * Sets the message related to the upload operation.
     *
     * @param message A descriptive message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the original name of the uploaded file.
     *
     * @param fileName The original file name to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Sets the Google Drive file ID assigned to the uploaded file.
     *
     * @param driveFileId The Google Drive file ID to set.
     */
    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    /**
     * Sets the MIME type of the uploaded file.
     *
     * @param fileMimeType The file MIME type to set.
     */
    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = fileMimeType;
    }

    /**
     * Sets the user's current storage usage after the upload.
     *
     * @param currentStorageUsageMb The current storage usage in megabytes (MB) to set.
     */
    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    /**
     * Sets the user's maximum allowed storage quota.
     *
     * @param maxStorageQuotaMb The maximum storage quota in megabytes (MB) to set.
     */
    public void setMaxStorageQuotaMb(double maxStorageQuotaMb) {
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }
}