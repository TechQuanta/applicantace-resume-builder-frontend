package com.example.acespringbackend.auth.dto;

/**
 * DTO for responses after a Google Drive template file replication operation.
 * Provides information about the success/failure, a message, and details
 * about the newly replicated file if successful.
 */
public class TemplateReplicationResponse {
    private boolean success;
    private String message;
    private String newFileName;
    private String replicatedFileId; // The Google Drive ID of the newly copied file
    private String replicatedFileName; // The actual name of the file on Drive after replication
    private String webViewLink; 
    private String provider;// The direct link to view the file in Google Docs/Drive
    private double currentStorageUsageMb;
    private double maxStorageQuotaMb;

    /**
     * Default constructor.
     */
    public TemplateReplicationResponse() {
    }

    /**
     * All-arguments constructor.
     * @param success True if the operation was successful, false otherwise.
     * @param message A message describing the outcome of the operation.
     * @param newFileName The desired new name for the replicated file.
     * @param replicatedFileId The Google Drive ID of the newly copied file.
     * @param replicatedFileName The actual name of the file on Drive after replication.
     * @param webViewLink The direct link to view the file in Google Docs/Drive.
     * @param currentStorageUsageMb The user's current storage usage in MB.
     * @param maxStorageQuotaMb The user's maximum allowed storage quota in MB.
     */
    public TemplateReplicationResponse(boolean success, String message, String newFileName, String replicatedFileId,
                                       String replicatedFileName, String webViewLink, double currentStorageUsageMb,
                                       double maxStorageQuotaMb,String provider) {
        this.success = success;
        this.message = message;
        this.newFileName = newFileName;
        this.replicatedFileId = replicatedFileId;
        this.replicatedFileName = replicatedFileName;
        this.webViewLink = webViewLink;
        this.provider=provider;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // Getters and Setters

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public String getReplicatedFileId() {
        return replicatedFileId;
    }

    public void setReplicatedFileId(String replicatedFileId) {
        this.replicatedFileId = replicatedFileId;
    }

    public String getReplicatedFileName() {
        return replicatedFileName;
    }

    public void setReplicatedFileName(String replicatedFileName) {
        this.replicatedFileName = replicatedFileName;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    public String getProvider() {
    	return provider;
    }
    public double getMaxStorageQuotaMb() {
        return maxStorageQuotaMb;
    }

    public void setMaxStorageQuotaMb(double maxStorageQuotaMb) {
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }
}
