package com.example.acespringbackend.auth.dto;

/**
 * DTO for responses after a file rename operation on Google Drive.
 */
public class FileRenameResponse {
    private boolean success;
    private String message;
    private String newFileName;
    private String fileId;
    private String webViewLink;
    private double currentStorageUsageMb;
    private double maxStorageQuotaMb;

    public FileRenameResponse() {
    }

    public FileRenameResponse(boolean success, String message, String newFileName, String fileId, String webViewLink, double currentStorageUsageMb, double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.newFileName = newFileName;
        this.fileId = fileId;
        this.webViewLink = webViewLink;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

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

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
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

    public double getMaxStorageQuotaMb() {
        return maxStorageQuotaMb;
    }

    public void setMaxStorageQuotaMb(double maxStorageQuotaMb) {
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }
}
