package com.example.acespringbackend.auth.dto;

/**
 * Simplified DTO for responses after a permission update operation on Google Drive.
 * Only includes success status and a descriptive message.
 */
public class PermissionUpdateResponse {
    private boolean success;
    private String message;

    public PermissionUpdateResponse() {
    }

    public PermissionUpdateResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
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
}