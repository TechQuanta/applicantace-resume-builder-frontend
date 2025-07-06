package com.example.acespringbackend.auth.dto;

/**
 * DTO for responses after a file export/conversion operation from Google Drive.
 * Includes the binary content of the exported file.
 */
public class FileExportResponse {
    private boolean success;
    private String message;
    private String exportedFileName;    // The name of the file after export (e.g., "report.pdf")
    private String exportedFileMimeType; // The MIME type of the exported file
    private byte[] fileContent;         // The binary content of the exported file
    private double currentStorageUsageMb;
    private double maxStorageQuotaMb;

    public FileExportResponse() {
    }

    public FileExportResponse(boolean success, String message, String exportedFileName, String exportedFileMimeType,
                              byte[] fileContent, double currentStorageUsageMb, double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.exportedFileName = exportedFileName;
        this.exportedFileMimeType = exportedFileMimeType;
        this.fileContent = fileContent;
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

    public String getExportedFileName() {
        return exportedFileName;
    }

    public void setExportedFileName(String exportedFileName) {
        this.exportedFileName = exportedFileName;
    }

    public String getExportedFileMimeType() {
        return exportedFileMimeType;
    }

    public void setExportedFileMimeType(String exportedFileMimeType) {
        this.exportedFileMimeType = exportedFileMimeType;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
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
