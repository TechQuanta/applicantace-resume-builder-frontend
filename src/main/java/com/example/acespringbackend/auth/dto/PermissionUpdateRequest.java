package com.example.acespringbackend.auth.dto;

public class PermissionUpdateRequest {
    private String userEmail;
    private String fileId;
    private String targetEmail;
    private String role; // This will now be optional/null if action is 'remove'
    private String action; // NEW: "add" or "remove"

    // Constructors
    public PermissionUpdateRequest() {}

    public PermissionUpdateRequest(String userEmail, String fileId, String targetEmail, String role, String action) {
        this.userEmail = userEmail;
        this.fileId = fileId;
        this.targetEmail = targetEmail;
        this.role = role;
        this.action = action;
    }

    // Getters and Setters
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAction() { return action; } // NEW Getter
    public void setAction(String action) { this.action = action; } // NEW Setter
}