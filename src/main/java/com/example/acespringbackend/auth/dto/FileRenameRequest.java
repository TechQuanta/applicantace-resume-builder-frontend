package com.example.acespringbackend.auth.dto;

/**
 * DTO for requests to rename a file on Google Drive.
 */
public class FileRenameRequest {
    private String userEmail;
    private String fileId;
    private String newFileName;

    public FileRenameRequest() {
    }

    public FileRenameRequest(String userEmail, String fileId, String newFileName) {
        this.userEmail = userEmail;
        this.fileId = fileId;
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

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }
}
