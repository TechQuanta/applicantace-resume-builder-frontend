package com.example.acespringbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "user_files")
public class UserFile {
    @Id
    private String id;
    private String userId; // Keep this if you still want to link to User by ID
    private String email; // ADD THIS FIELD if it's not already there
    private String driveFileId;
    private String filename;
    private String mimeType;
    private Long size;
    private LocalDateTime uploadedAt;
    private LocalDateTime lastModified;
    private String webViewLink;
    private String webContentLink;
    private String thumbnailLink;
    private String templateName;
    private String templateCategory;
    private String templateImageUrl;
    private String templateDescription;
    private String templateSpotlight;
    private String templateProvider;
    private String originalTemplateDriveId;

    // Getters and Setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; } // Getter for the new email field
    public void setEmail(String email) { this.email = email; } // Setter for the new email field
    public String getDriveFileId() { return driveFileId; }
    public void setDriveFileId(String driveFileId) { this.driveFileId = driveFileId; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public String getWebViewLink() { return webViewLink; }
    public void setWebViewLink(String webViewLink) { this.webViewLink = webViewLink; }
    public String getWebContentLink() { return webContentLink; }
    public void setWebContentLink(String webContentLink) { this.webContentLink = webContentLink; }
    public String getThumbnailLink() { return thumbnailLink; }
    public void setThumbnailLink(String thumbnailLink) { this.thumbnailLink = thumbnailLink; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTemplateCategory() { return templateCategory; }
    public void setTemplateCategory(String templateCategory) { this.templateCategory = templateCategory; }
    public String getTemplateImageUrl() { return templateImageUrl; }
    public void setTemplateImageUrl(String templateImageUrl) { this.templateImageUrl = templateImageUrl; }
    public String getTemplateDescription() { return templateDescription; }
    public void setDescription(String templateDescription) { this.templateDescription = templateDescription; }
    public String getTemplateSpotlight() { return templateSpotlight; }
    public void setTemplateSpotlight(String templateSpotlight) { this.templateSpotlight = templateSpotlight; }
    public String getTemplateProvider() { return templateProvider; }
    public void setTemplateProvider(String templateProvider) { this.templateProvider = templateProvider; }
    public String getOriginalTemplateDriveId() { return originalTemplateDriveId; }
    public void setOriginalTemplateDriveId(String originalTemplateDriveId) { this.originalTemplateDriveId = originalTemplateDriveId; }
}