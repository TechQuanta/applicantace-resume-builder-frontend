// src/main/java/com/yourcompany/yourapp/dto/TemplateReplicationRequest.java

package com.example.acespringbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class TemplateReplicationRequest {

    @NotBlank(message = "User email is required.")
    private String userEmail;

    @NotBlank(message = "Template name is required.")
    private String name; // Corresponds to template.name from frontend

    private String category; // Corresponds to template.category

    @NotBlank(message = "Drive ID is required.")
    private String drive_id; // Corresponds to template.id from frontend (renamed to drive_id)

    private String image_url; // Corresponds to template.image

    private String description; // Corresponds to template.description

    private String spotlight; // Corresponds to template.spotlights

    private String provider; // Corresponds to template.provider

    @NotBlank(message = "New file name is required.")
    private String newFileName;

    @NotBlank(message = "Target Drive folder ID is required.")
    private String targetDriveFolderId;

    // --- Constructors ---

    /**
     * Default constructor. Required by many frameworks for object deserialization.
     */
    public TemplateReplicationRequest() {
    }

    /**
     * Parameterized constructor for convenience, allowing all fields to be set upon creation.
     */
    public TemplateReplicationRequest(
        String userEmail,
        String name,
        String category,
        String drive_id,
        String image_url,
        String description,
        String spotlight,
        String provider,
        String newFileName,
        String targetDriveFolderId
    ) {
        this.userEmail = userEmail;
        this.name = name;
        this.category = category;
        this.drive_id = drive_id;
        this.image_url = image_url;
        this.description = description;
        this.spotlight = spotlight;
        this.provider = provider;
        this.newFileName = newFileName;
        this.targetDriveFolderId = targetDriveFolderId;
    }

    // --- Getters and Setters ---

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDrive_id() {
        return drive_id;
    }

    public void setDrive_id(String drive_id) {
        this.drive_id = drive_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpotlight() {
        return spotlight;
    }

    public void setSpotlight(String spotlight) {
        this.spotlight = spotlight;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public String getTargetDriveFolderId() {
        return targetDriveFolderId;
    }

    public void setTargetDriveFolderId(String targetDriveFolderId) {
        this.targetDriveFolderId = targetDriveFolderId;
    }

    // --- Optional: toString(), equals(), hashCode() methods for better debugging/object comparison ---
    // You can generate these in most IDEs (e.g., in IntelliJ/Eclipse: Right-click -> Generate -> toString()/equals() and hashCode())

    @Override
    public String toString() {
        return "TemplateReplicationRequest{" +
               "userEmail='" + userEmail + '\'' +
               ", name='" + name + '\'' +
               ", category='" + category + '\'' +
               ", drive_id='" + drive_id + '\'' +
               ", image_url='" + image_url + '\'' +
               ", description='" + description + '\'' +
               ", spotlight='" + spotlight + '\'' +
               ", provider='" + provider + '\'' +
               ", newFileName='" + newFileName + '\'' +
               ", targetDriveFolderId='" + targetDriveFolderId + '\'' +
               '}';
    }

    // You would typically generate equals and hashCode together.
    // For brevity, I'm omitting the full equals/hashCode implementation,
    // but they are crucial for correct object comparison in collections (e.g., Sets, Maps).
}