package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for conveying the response after a file rename operation on Google Drive.
 * This DTO provides feedback on the success or failure of the operation, the new file name,
 * and updated storage usage information.
 */
public class FileRenameResponse {

    /**
     * A boolean flag indicating whether the file rename operation was successful.
     * {@code true} if the file was successfully renamed, {@code false} otherwise.
     */
    private boolean success;

    /**
     * A descriptive message providing context about the rename operation's outcome,
     * such as success confirmation or a detailed error message.
     */
    private String message;

    /**
     * The new name assigned to the file after a successful rename operation.
     * This confirms the change to the client.
     */
    private String newFileName;

    /**
     * The unique identifier (File ID) of the file that was renamed.
     * This ID remains constant even after a rename operation.
     */
    private String fileId;

    /**
     * The updated public web view link for the renamed file on Google Drive.
     * This link may be the same or updated depending on Google Drive's handling of renames.
     */
    private String webViewLink;

    /**
     * The user's current Google Drive storage usage in megabytes (MB)
     * after the file rename operation. This provides updated storage status.
     */
    private double currentStorageUsageMb;

    /**
     * The maximum Google Drive storage quota available to the user in megabytes (MB).
     * This helps the client application display storage limits.
     */
    private double maxStorageQuotaMb;

    /**
     * Default no-argument constructor for {@code FileRenameResponse}.
     * This constructor is necessary for frameworks like Spring to deserialize JSON
     * or form data into an instance of this object.
     */
    public FileRenameResponse() {
        // Default constructor
    }

    /**
     * Constructs a new {@code FileRenameResponse} with complete information about the rename outcome.
     * This constructor is used to provide a detailed response to the client after a file rename attempt.
     *
     * @param success             A boolean indicating if the rename was successful.
     * @param message             A descriptive message about the operation's outcome.
     * @param newFileName         The new name assigned to the file.
     * @param fileId              The Google Drive ID of the renamed file.
     * @param webViewLink         The updated web view link for the file.
     * @param currentStorageUsageMb The user's updated current storage usage in MB.
     * @param maxStorageQuotaMb   The user's maximum storage quota in MB.
     */
    public FileRenameResponse(boolean success, String message, String newFileName, String fileId, String webViewLink, double currentStorageUsageMb, double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.newFileName = newFileName;
        this.fileId = fileId;
        this.webViewLink = webViewLink;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // --- Getters ---

    /**
     * Retrieves the success status of the rename operation.
     *
     * @return {@code true} if the rename succeeded, {@code false} otherwise.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Retrieves the message related to the rename operation's outcome.
     *
     * @return A descriptive message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the new name of the file after a successful rename.
     *
     * @return The new file name.
     */
    public String getNewFileName() {
        return newFileName;
    }

    /**
     * Retrieves the Google Drive ID of the renamed file.
     *
     * @return The file ID.
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Retrieves the updated public web view link for the renamed file.
     *
     * @return The web view link.
     */
    public String getWebViewLink() {
        return webViewLink;
    }

    /**
     * Retrieves the user's current storage usage after the rename.
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
     * Sets the success status of the rename operation.
     *
     * @param success {@code true} for success, {@code false} for failure.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Sets the message related to the rename operation.
     *
     * @param message A descriptive message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the new name of the file after a successful rename.
     *
     * @param newFileName The new file name to set.
     */
    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    /**
     * Sets the Google Drive ID of the renamed file.
     *
     * @param fileId The file ID to set.
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Sets the updated public web view link for the renamed file.
     *
     * @param webViewLink The web view link to set.
     */
    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    /**
     * Sets the user's current storage usage after the rename.
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