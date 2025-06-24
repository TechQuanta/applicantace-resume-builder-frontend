// --- com.example.acespringbackend.auth.dto.DeleteResponse.java ---
package com.example.acespringbackend.auth.dto;

public class DeleteResponse {
    private Boolean success;
    private String message;
    private String deletedFileId;
    private Long deletedFileSize;
    private double currentStorageUsageMb; // Include updated storage usage
    private double maxStorageQuotaMb;     // Include max quota

    public DeleteResponse() {
    }

    // Full constructor for direct instantiation
    public DeleteResponse(Boolean success, String message, String deletedFileId, Long deletedFileSize, double currentStorageUsageMb, double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.deletedFileId = deletedFileId;
        this.deletedFileSize = deletedFileSize;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // Getters
    public Boolean getSuccess() { return success; }
    public String getMessage() { return message; }
    public String getDeletedFileId() { return deletedFileId; }
    public Long getDeletedFileSize() { return deletedFileSize; }
    public double getCurrentStorageUsageMb() { return currentStorageUsageMb; }
    public double getMaxStorageQuotaMb() { return maxStorageQuotaMb; }


    // Setters
    public void setSuccess(Boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setDeletedFileId(String deletedFileId) { this.deletedFileId = deletedFileId; }
    public void setDeletedFileSize(Long deletedFileSize) { this.deletedFileSize = deletedFileSize; }
    public void setCurrentStorageUsageMb(double currentStorageUsageMb) { this.currentStorageUsageMb = currentStorageUsageMb; }
    public void setMaxStorageQuotaMb(double maxStorageQuotaMb) { this.maxStorageQuotaMb = maxStorageQuotaMb; }
}
