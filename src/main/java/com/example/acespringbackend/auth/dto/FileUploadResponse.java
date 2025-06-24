// --- com.example.acespringbackend.auth.dto.FileUploadResponse.java ---
package com.example.acespringbackend.auth.dto;

public class FileUploadResponse {
    private Boolean success;
    private String message;
    private String fileName;    // original name from client
    private String driveFileId; // Google Drive file ID
    private String fileMimeType; // MIME type
    private double currentStorageUsageMb; // current usage in MB
    private double maxStorageQuotaMb;     // max quota in MB

    public FileUploadResponse() {}

    // Full constructor for direct instantiation
    public FileUploadResponse(Boolean success, String message, String fileName, String driveFileId, String fileMimeType, double currentStorageUsageMb, double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.fileName = fileName;
        this.driveFileId = driveFileId;
        this.fileMimeType = fileMimeType;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // Getters
    public Boolean getSuccess() { return success; }
    public String getMessage() { return message; }
    public String getFileName() { return fileName; }
    public String getDriveFileId() { return driveFileId; }
    public String getFileMimeType() { return fileMimeType; }
    public double getCurrentStorageUsageMb() { return currentStorageUsageMb; }
    public double getMaxStorageQuotaMb() { return maxStorageQuotaMb; }

    // Setters
    public void setSuccess(Boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setDriveFileId(String driveFileId) { this.driveFileId = driveFileId; }
    public void setFileMimeType(String fileMimeType) { this.fileMimeType = fileMimeType; }
    public void setCurrentStorageUsageMb(double currentStorageUsageMb) { this.currentStorageUsageMb = currentStorageUsageMb; }
    public void setMaxStorageQuotaMb(double maxStorageQuotaMb) { this.maxStorageQuotaMb = maxStorageQuotaMb; }
}