package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for responses after a Google Drive template file replication operation.
 * This DTO provides essential feedback to the client regarding the success or failure of the
 * replication, along with details about the newly created file and updated storage metrics if successful.
 */
public class TemplateReplicationResponse {

    /**
     * A boolean flag indicating whether the template replication operation was successful.
     * {@code true} if the file was successfully copied, {@code false} otherwise.
     */
    private boolean success;

    /**
     * A descriptive message detailing the outcome of the replication operation.
     * This could be a success message or an error message explaining why the operation failed.
     */
    private String message;

    /**
     * The desired new name for the replicated file, as specified in the request.
     * This might differ from {@link #replicatedFileName} if the Drive API modifies it (e.g., adds a suffix).
     */
    private String newFileName;

    /**
     * The unique Google Drive ID of the newly copied file.
     * This ID allows the client to refer to and manage the replicated file directly on Google Drive.
     */
    private String replicatedFileId;

    /**
     * The actual name of the file on Google Drive after replication.
     * This field is important because the Drive API might slightly alter the requested {@code newFileName}.
     */
    private String replicatedFileName;

    /**
     * The direct web view link to the newly replicated file in Google Docs/Drive.
     * This URL allows users to open and interact with the file directly in their browser.
     */
    private String webViewLink;

    /**
     * The provider associated with the template. This might indicate the source of the template
     * (e.g., "Google", "OurApp", "Community").
     */
    private String provider; // Added to match the constructor and reflect data often passed from template info

    /**
     * The user's current storage usage in megabytes (MB) on their associated cloud drive
     * after the file replication. This helps the client update the UI with real-time usage.
     */
    private double currentStorageUsageMb;

    /**
     * The user's maximum allowed storage quota in megabytes (MB) on their associated cloud drive.
     * This provides context for the current storage usage.
     */
    private double maxStorageQuotaMb;


    /**
     * Default no-argument constructor for {@code TemplateReplicationResponse}.
     * This constructor is essential for frameworks like Spring or Jackson for
     * object deserialization from JSON or other data formats.
     */
    public TemplateReplicationResponse() {
    }

    /**
     * All-arguments constructor for {@code TemplateReplicationResponse}.
     * This constructor allows for convenient and complete initialization of the DTO fields.
     *
     * @param success               True if the operation was successful, false otherwise.
     * @param message               A message describing the outcome of the operation.
     * @param newFileName           The desired new name for the replicated file (from the request).
     * @param replicatedFileId      The Google Drive ID of the newly copied file.
     * @param replicatedFileName    The actual name of the file on Drive after replication.
     * @param webViewLink           The direct link to view the file in Google Docs/Drive.
     * @param currentStorageUsageMb The user's current storage usage in MB after the operation.
     * @param maxStorageQuotaMb     The user's maximum allowed storage quota in MB.
     * @param provider              The provider of the template.
     */
    public TemplateReplicationResponse(boolean success, String message, String newFileName, String replicatedFileId,
                                       String replicatedFileName, String webViewLink, double currentStorageUsageMb,
                                       double maxStorageQuotaMb, String provider) {
        this.success = success;
        this.message = message;
        this.newFileName = newFileName;
        this.replicatedFileId = replicatedFileId;
        this.replicatedFileName = replicatedFileName;
        this.webViewLink = webViewLink;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
        this.provider = provider; // Initialize the provider field
    }

    // --- Getters ---

    /**
     * Retrieves the success status of the replication operation.
     *
     * @return {@code true} if the operation was successful, {@code false} otherwise.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Retrieves the message describing the outcome of the operation.
     *
     * @return The message as a {@link String}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the desired new file name that was requested.
     *
     * @return The requested new file name as a {@link String}.
     */
    public String getNewFileName() {
        return newFileName;
    }

    /**
     * Retrieves the Google Drive ID of the newly replicated file.
     *
     * @return The replicated file ID as a {@link String}.
     */
    public String getReplicatedFileId() {
        return replicatedFileId;
    }

    /**
     * Retrieves the actual name of the file on Google Drive after replication.
     *
     * @return The actual replicated file name as a {@link String}.
     */
    public String getReplicatedFileName() {
        return replicatedFileName;
    }

    /**
     * Retrieves the direct web view link to the newly replicated file.
     *
     * @return The web view link as a {@link String}.
     */
    public String getWebViewLink() {
        return webViewLink;
    }

    /**
     * Retrieves the provider of the template.
     *
     * @return The provider as a {@link String}.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Retrieves the user's current storage usage in megabytes after the operation.
     *
     * @return The current storage usage in MB as a {@code double}.
     */
    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    /**
     * Retrieves the user's maximum allowed storage quota in megabytes.
     *
     * @return The maximum storage quota in MB as a {@code double}.
     */
    public double getMaxStorageQuotaMb() {
        return maxStorageQuotaMb;
    }

    // --- Setters ---

    /**
     * Sets the success status of the replication operation.
     *
     * @param success {@code true} for success, {@code false} for failure.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Sets the message describing the outcome of the operation.
     *
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the desired new file name that was requested.
     *
     * @param newFileName The requested new file name to set.
     */
    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    /**
     * Sets the Google Drive ID of the newly replicated file.
     *
     * @param replicatedFileId The replicated file ID to set.
     */
    public void setReplicatedFileId(String replicatedFileId) {
        this.replicatedFileId = replicatedFileId;
    }

    /**
     * Sets the actual name of the file on Google Drive after replication.
     *
     * @param replicatedFileName The actual replicated file name to set.
     */
    public void setReplicatedFileName(String replicatedFileName) {
        this.replicatedFileName = replicatedFileName;
    }

    /**
     * Sets the direct web view link to the newly replicated file.
     *
     * @param webViewLink The web view link to set.
     */
    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    /**
     * Sets the provider of the template.
     *
     * @param provider The provider to set.
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Sets the user's current storage usage in megabytes.
     *
     * @param currentStorageUsageMb The current storage usage in MB to set.
     */
    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    /**
     * Sets the user's maximum allowed storage quota in megabytes.
     *
     * @param maxStorageQuotaMb The maximum storage quota in MB to set.
     */
    public void setMaxStorageQuotaMb(double maxStorageQuotaMb) {
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }
}