package com.example.acespringbackend.auth.dto;

import java.util.List;

/**
 * DTO for responding with a list of file details for a user's drive.
 * Includes status, message, the list of files, and storage usage information.
 */
public class FileListResponse {
    private Boolean success;            // Indicates if the operation was successful
    private String message;             // A descriptive message about the operation's outcome
    private List<FileDetail> files;     // The list of detailed file information
    private Double currentStorageUsageMb; // Current storage usage for the user in megabytes
    private Double maxStorageQuotaMb;   // Maximum allowed storage quota for the user in megabytes

    /**
     * No-argument constructor for deserialization frameworks (like Jackson).
     * Necessary for Spring to automatically map JSON to this object.
     */
    public FileListResponse() {
    }

    // Note: The constructor below with `List<? extends Object>` is a bit unusual.
    // It's better to explicitly use `List<FileDetail>` if that's the expected type.
    // If you intend to use this constructor for specific cases where `fileDetailsList`
    // might be a generic list during intermediate processing, keep it.
    // Otherwise, the full constructor below is generally preferred for clarity.
    public FileListResponse(boolean b, String string, List<? extends Object> fileDetailsList, double d, double userDriveQuotaMb) {
        this.success = b;
        this.message = string;
        // This cast might lead to issues if the list truly contains non-FileDetail objects
        this.files = (List<FileDetail>) fileDetailsList;
        this.currentStorageUsageMb = d;
        this.maxStorageQuotaMb = userDriveQuotaMb;
    }

    /**
     * Full constructor for direct instantiation of FileListResponse.
     * Ensures all fields are initialized correctly.
     *
     * @param success               Boolean indicating if the operation was successful.
     * @param message               A descriptive message about the operation's outcome.
     * @param files                 A list of FileDetail objects.
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
    public Boolean getSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    public List<FileDetail> getFiles() {
        return files;
    }
    public Double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }
    public Double getMaxStorageQuotaMb() {
        return maxStorageQuotaMb;
    }

    // --- Setters (Often only needed for deserialization, but included for completeness) ---
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setFiles(List<FileDetail> files) {
        this.files = files;
    }
    public void setCurrentStorageUsageMb(Double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }
    public void setMaxStorageQuotaMb(Double maxStorageQuotaMb) {
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }
}