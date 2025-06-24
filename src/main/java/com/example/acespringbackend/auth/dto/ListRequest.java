package com.example.acespringbackend.auth.dto;

/**
 * DTO for requests to list files.
 * Contains the folder ID and the user's email to identify the owner of the files.
 */
public class ListRequest {
    private String folderId;    // The Google Drive folder ID from which to list files
    private String userEmail;   // The email of the user making the request

    /**
     * Default no-argument constructor for deserialization frameworks.
     */
    public ListRequest() {
    }

    /**
     * Constructor for direct instantiation of a ListRequest object.
     *
     * @param folderId  The ID of the Google Drive folder.
     * @param userEmail The email of the user.
     */
    public ListRequest(String folderId, String userEmail) {
        this.folderId = folderId;
        this.userEmail = userEmail;
    }

    // --- Getters ---
    public String getFolderId() {
        return folderId;
    }
    public String getUserEmail() {
        return userEmail;
    }

    // --- Setters ---
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}