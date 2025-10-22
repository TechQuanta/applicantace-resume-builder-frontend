package com.example.acespringbackend.auth.dto;

/**
 * Represents the response returned after a file deletion attempt from Google Drive.
 * This DTO provides feedback on the success or failure of the operation, details about
 * the deleted file, and updated user storage metrics.
 */
public class DeleteResponse {

    /**
     * A boolean flag indicating whether the file deletion operation was successful.
     * True if the file was deleted, false otherwise.
     */
    private Boolean success;

    /**
     * A descriptive message providing context about the deletion attempt,
     * such as success confirmation or a detailed error message.
     */
    private String message;

    /**
     * The Google Drive file ID of the file that was attempted to be deleted.
     * This ID is included regardless of success to provide a clear reference.
     */
    private String deletedFileId;

    /**
     * The size of the file that was deleted, in bytes.
     * This helps in confirming the specific file and for logging purposes.
     */
    private Long deletedFileSize;

    /**
     * The user's current Google Drive storage usage in megabytes (MB)
     * after the deletion operation. This provides real-time storage status.
     */
    private double currentStorageUsageMb;

    /**
     * The maximum Google Drive storage quota available to the user in megabytes (MB).
     * This helps the client application display storage limits.
     */
    private double maxStorageQuotaMb;

    /**
     * Default constructor for {@code DeleteResponse}.
     * This constructor is required for frameworks like Spring to deserialize
     * JSON or form data into an instance of this object.
     */
    public DeleteResponse() {
        // Default constructor
    }

    /**
     * Constructs a new {@code DeleteResponse} with complete information about the deletion outcome.
     * This constructor is used to provide a detailed response to the client after a file deletion attempt.
     *
     * @param success A boolean indicating if the deletion was successful.
     * @param message A descriptive message about the operation's outcome.
     * @param deletedFileId The ID of the file that was targeted for deletion.
     * @param deletedFileSize The size of the deleted file in bytes.
     * @param currentStorageUsageMb The user's updated current storage usage in MB.
     * @param maxStorageQuotaMb The user's maximum storage quota in MB.
     */
    public DeleteResponse(Boolean success, String message, String deletedFileId, Long deletedFileSize, double currentStorageUsageMb, double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.deletedFileId = deletedFileId;
        this.deletedFileSize = deletedFileSize;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // --- Getters ---

    /**
     * Retrieves the success status of the delete operation.
     *
     * @return True if the file was successfully deleted, false otherwise.
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Retrieves the message related to the delete operation's outcome.
     *
     * @return A descriptive message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the Google Drive ID of the file that was targeted for deletion.
     *
     * @return The deleted file's ID.
     */
    public String getDeletedFileId() {
        return deletedFileId;
    }

    /**
     * Retrieves the size of the file that was deleted.
     *
     * @return The deleted file's size in bytes.
     */
    public Long getDeletedFileSize() {
        return deletedFileSize;
    }

    /**
     * Retrieves the user's current storage usage after the deletion.
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
     * Sets the success status of the delete operation.
     *
     * @param success True for success, false for failure.
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * Sets the message related to the delete operation.
     *
     * @param message A descriptive message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the Google Drive ID of the file that was targeted for deletion.
     *
     * @param deletedFileId The deleted file's ID to set.
     */
    public void setDeletedFileId(String deletedFileId) {
        this.deletedFileId = deletedFileId;
    }

    /**
     * Sets the size of the file that was deleted.
     *
     * @param deletedFileSize The deleted file's size in bytes to set.
     */
    public void setDeletedFileSize(Long deletedFileSize) {
        this.deletedFileSize = deletedFileSize;
    }

    /**
     * Sets the user's current storage usage after the deletion.
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