package com.example.acespringbackend.auth.dto;

public class DeleteRequest {

    private String fileId; // Google Drive file ID to delete
    private String userEmail; // Added: To pass user email from frontend for identification

    // Removed folderId as it's often derived from user or not strictly needed at request level

    public DeleteRequest() {
    }

    public DeleteRequest(String fileId, String userEmail) {
        this.fileId = fileId;
        this.userEmail = userEmail;
    }

    // Getters
    public String getFileId() { return fileId; }
    public String getUserEmail() { return userEmail; }

    // Setters
    public void setFileId(String fileId) { this.fileId = fileId; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}