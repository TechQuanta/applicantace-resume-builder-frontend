// --- com.example.acespringbackend.auth.dto.FileUploadRequest.java ---
package com.example.acespringbackend.auth.dto;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadRequest {

    private MultipartFile file;
    private String userEmail;
    private String folderId; // Added: To pass the target folder ID from frontend

    public FileUploadRequest() {
    }

    public FileUploadRequest(MultipartFile file, String userEmail, String folderId) {
        this.file = file;
        this.userEmail = userEmail;
        this.folderId = folderId;
    }

    // Getters
    public MultipartFile getFile() {
        return file;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getFolderId() { // New getter for folderId
        return folderId;
    }

    // Setters
    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setFolderId(String folderId) { // New setter for folderId
        this.folderId = folderId;
    }
}