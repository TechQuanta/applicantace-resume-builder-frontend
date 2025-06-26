package com.example.acespringbackend.auth.dto;

/**
 * DTO for requests to replicate a Google Drive template file.
 * Contains the user's email, the ID of the master template file,
 * and the desired new name for the replicated file.
 */
public class TemplateReplicationRequest {
    private String userEmail;
    private String masterTemplateFileId;
    private String newFileName;

    /**
     * Default constructor.
     */
    public TemplateReplicationRequest() {
    }

    /**
     * All-arguments constructor.
     * @param userEmail The email of the user.
     * @param masterTemplateFileId The Google Drive ID of the master template file.
     * @param newFileName The desired new name for the replicated file.
     */
    public TemplateReplicationRequest(String userEmail, String masterTemplateFileId, String newFileName) {
        this.userEmail = userEmail;
        this.masterTemplateFileId = masterTemplateFileId;
        this.newFileName = newFileName;
    }

    // Getters and Setters

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMasterTemplateFileId() {
        return masterTemplateFileId;
    }

    public void setMasterTemplateFileId(String masterTemplateFileId) {
        this.masterTemplateFileId = masterTemplateFileId;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }
}
