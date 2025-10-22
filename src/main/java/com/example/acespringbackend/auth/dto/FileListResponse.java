package com.example.acespringbackend.auth.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) for conveying a list of file details from a user's drive.
 * This response object provides information about the operation's success, a descriptive message,
 * the actual list of files, and the user's current storage usage and quota.
 */
public class FileListResponse {

    /**
     * A boolean flag indicating whether the operation to retrieve the file list was successful.
     * {@code true} if the list was successfully fetched, {@code false} otherwise.
     */
    private Boolean success;

    /**
     * A descriptive message providing context about the operation's outcome,
     * such as success confirmation or a detailed error message if issues occurred.
     */
    private String message;

    /**
     * A list of {@link FileDetail} objects, each containing comprehensive metadata
     * about a single file stored in the user's drive.
     */
    private List<FileDetail> files;

    /**
     * The user's current storage usage in megabytes (MB) on the drive.
     * This provides real-time information about consumed storage space.
     */
    private Double currentStorageUsageMb;

    /**
     * The maximum allowed storage quota for the user in megabytes (MB) on the drive.
     * This indicates the total storage capacity available to the user.
     */
    private Double maxStorageQuotaMb;

    /**
     * Default no-argument constructor for {@code FileListResponse}.
     * This constructor is crucial for deserialization frameworks (like Jackson)
     * to automatically map JSON data to an instance of this object.
     */
    public FileListResponse() {
    }

    /**
     * Constructs a new {@code FileListResponse} with comprehensive details about the file list and storage.
     * This is the primary constructor for instantiating a fully populated response object.
     *
     * @param success               A boolean indicating if the operation was successful.
     * @param message               A descriptive message about the operation's outcome.
     * @param files                 A list of {@link FileDetail} objects representing the user's files.
     * @param currentStorageUsageMb The user's current storage usage in megabytes.
     * @param maxStorageQuotaMb     The user's maximum allowed storage quota in megabytes.
     */
    public FileListResponse(Boolean success, String message, List<FileDetail> files, Double currentStorageUsageMb, Double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.files = files;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // --- Getters ---

    /**
     * Retrieves the success status of the operation.
     *
     * @return {@code true} if the operation was successful, {@code false} otherwise.
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Retrieves the descriptive message about the operation's outcome.
     *
     * @return The message as a {@link String}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the list of detailed file information.
     *
     * @return A {@link List} of {@link FileDetail} objects.
     */
    public List<FileDetail> getFiles() {
        return files;
    }

    /**
     * Retrieves the user's current storage usage in megabytes.
     *
     * @return The current storage usage in MB.
     */
    public Double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    /**
     * Retrieves the user's maximum allowed storage quota in megabytes.
     *
     * @return The maximum storage quota in MB.
     */
    public Double getMaxStorageQuotaMb() {
        return maxStorageQuotaMb;
    }

    // --- Setters (Typically used by deserialization frameworks) ---

    /**
     * Sets the success status of the operation.
     *
     * @param success The boolean success status to set.
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * Sets the descriptive message about the operation's outcome.
     *
     * @param message The message {@link String} to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the list of detailed file information.
     *
     * @param files The {@link List} of {@link FileDetail} objects to set.
     */
    public void setFiles(List<FileDetail> files) {
        this.files = files;
    }

    /**
     * Sets the user's current storage usage in megabytes.
     *
     * @param currentStorageUsageMb The current storage usage in MB to set.
     */
    public void setCurrentStorageUsageMb(Double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    /**
     * Sets the user's maximum allowed storage quota in megabytes.
     *
     * @param maxStorageQuotaMb The maximum storage quota in MB to set.
     */
    public void setMaxStorageQuotaMb(Double maxStorageQuotaMb) {
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }
}