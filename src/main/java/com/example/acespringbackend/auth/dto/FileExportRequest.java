package com.example.acespringbackend.auth.dto;

/**
 * DTO for requests to export/convert a file from Google Drive to a different MIME type.
 */
public class FileExportRequest {
    private String userEmail;
    private String fileId;
    private String exportMimeType; // The desired MIME type for the exported file (e.g., "application/pdf")
    private String newFileName;    // Optional: New name for the exported file (without extension)

    public FileExportRequest() {
    }

    public FileExportRequest(String userEmail, String fileId, String exportMimeType, String newFileName) {
        this.userEmail = userEmail;
        this.fileId = fileId;
        this.exportMimeType = exportMimeType;
        this.newFileName = newFileName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getExportMimeType() {
        return exportMimeType;
    }

    public void setExportMimeType(String exportMimeType) {
        this.exportMimeType = exportMimeType;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }
}
