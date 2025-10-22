package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for requests to update file permissions on Google Drive.
 * This DTO supports both adding and removing permissions for a specific user on a given file.
 */
public class PermissionUpdateRequest {

    /**
     * The email address of the user initiating the permission update request.
     * This is crucial for authenticating the request and verifying that the user
     * has the necessary permissions to modify the file's sharing settings.
     */
    private String userEmail;

    /**
     * The unique identifier (File ID) of the file on Google Drive for which
     * permissions are being updated.
     */
    private String fileId;

    /**
     * The email address of the user whose permissions are being added or removed.
     * This is the recipient or target of the permission change.
     */
    private String targetEmail;

    /**
     * The role to assign to the {@code targetEmail} if the {@code action} is "add".
     * Common roles include "reader", "writer", "commenter", "owner".
     * This field can be {@code null} or empty if the {@code action} is "remove".
     */
    private String role;

    /**
     * Specifies the type of permission action to perform.
     * Valid values are "add" (to grant a new permission) or "remove" (to revoke an existing permission).
     */
    private String action; // e.g., "add", "remove"

    /**
     * Default no-argument constructor for {@code PermissionUpdateRequest}.
     * This constructor is necessary for frameworks like Spring to automatically
     * deserialize JSON or form data into an instance of this object.
     */
    public PermissionUpdateRequest() {
    }

    /**
     * Constructs a new {@code PermissionUpdateRequest} with all necessary parameters.
     *
     * @param userEmail   The email of the user performing the action.
     * @param fileId      The ID of the file whose permissions are being updated.
     * @param targetEmail The email of the user whose permission is being modified.
     * @param role        The role to assign (e.g., "reader", "writer") if action is "add". Can be null for "remove".
     * @param action      The action to perform: "add" or "remove".
     */
    public PermissionUpdateRequest(String userEmail, String fileId, String targetEmail, String role, String action) {
        this.userEmail = userEmail;
        this.fileId = fileId;
        this.targetEmail = targetEmail;
        this.role = role;
        this.action = action;
    }

    // --- Getters ---

    /**
     * Retrieves the email address of the user initiating the permission update.
     *
     * @return The initiator's email address as a {@link String}.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Retrieves the file ID for which permissions are being updated.
     *
     * @return The file ID as a {@link String}.
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Retrieves the email address of the target user whose permissions are being modified.
     *
     * @return The target user's email address as a {@link String}.
     */
    public String getTargetEmail() {
        return targetEmail;
    }

    /**
     * Retrieves the role to be assigned. This will be null if the action is "remove".
     *
     * @return The role as a {@link String}, or {@code null}.
     */
    public String getRole() {
        return role;
    }

    /**
     * Retrieves the action to be performed ("add" or "remove").
     *
     * @return The action as a {@link String}.
     */
    public String getAction() {
        return action;
    }

    // --- Setters ---

    /**
     * Sets the email address of the user initiating the permission update.
     *
     * @param userEmail The initiator's email address to set.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Sets the file ID for which permissions are being updated.
     *
     * @param fileId The file ID to set.
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Sets the email address of the target user whose permissions are being modified.
     *
     * @param targetEmail The target user's email address to set.
     */
    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    /**
     * Sets the role to be assigned.
     *
     * @param role The role to set. Can be {@code null}.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Sets the action to be performed ("add" or "remove").
     *
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }
}