package com.example.acespringbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "user_files")
public class UserFile {

    @Id
    private String id;

    private String userId;
    private String filename;
    private String driveFileId;
    private long size;
    private String contentType; // This was 'mimeType' in my previous UserFile, now matching yours.
    private LocalDateTime uploadedAt;
    private String webViewLink; // Existing field for the Google Drive web view link

    // --- MISSING FIELDS FROM YOUR ERROR REPORT ---
    private String mimeType; // Re-added as it was implicitly expected by DriveService
    private String webContentLink; // Re-added as it was implicitly expected by DriveService
    private LocalDateTime lastModified; // Re-added as it was implicitly expected by DriveService


    // --- NEW FIELDS FOR TEMPLATE METADATA ---
    private String templateName;
    private String templateCategory;
    private String templateImageUrl;
    private String templateDescription;
    private String templateSpotlight;
    private String templateProvider; // e.g., "APPLICANTACE" or "GOOGLE_DRIVE_TEMPLATE"
    private String originalTemplateDriveId; // To reference back to the master template if needed

    // --- Constructors ---

    public UserFile() {
    }

    // Updated constructor to include all new and re-added fields
    public UserFile(String id, String userId, String filename, String driveFileId, long size, String contentType,
                    LocalDateTime uploadedAt, String webViewLink, String mimeType, String webContentLink,
                    LocalDateTime lastModified, String templateName, String templateCategory,
                    String templateImageUrl, String templateDescription, String templateSpotlight,
                    String templateProvider, String originalTemplateDriveId) {
        this.id = id;
        this.userId = userId;
        this.filename = filename;
        this.driveFileId = driveFileId;
        this.size = size;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
        this.webViewLink = webViewLink;
        this.mimeType = mimeType; // Initialize re-added field
        this.webContentLink = webContentLink; // Initialize re-added field
        this.lastModified = lastModified; // Initialize re-added field
        this.templateName = templateName;
        this.templateCategory = templateCategory;
        this.templateImageUrl = templateImageUrl;
        this.templateDescription = templateDescription;
        this.templateSpotlight = templateSpotlight;
        this.templateProvider = templateProvider;
        this.originalTemplateDriveId = originalTemplateDriveId;
    }

    // Updated private constructor for Builder to include all new and re-added fields
    private UserFile(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.filename = builder.filename;
        this.driveFileId = builder.driveFileId;
        this.size = builder.size;
        this.contentType = builder.contentType;
        this.uploadedAt = builder.uploadedAt;
        this.webViewLink = builder.webViewLink;
        this.mimeType = builder.mimeType; // Initialize from builder
        this.webContentLink = builder.webContentLink; // Initialize from builder
        this.lastModified = builder.lastModified; // Initialize from builder
        this.templateName = builder.templateName;
        this.templateCategory = builder.templateCategory;
        this.templateImageUrl = builder.templateImageUrl;
        this.templateDescription = builder.templateDescription;
        this.templateSpotlight = builder.templateSpotlight;
        this.templateProvider = builder.templateProvider;
        this.originalTemplateDriveId = builder.originalTemplateDriveId;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFilename() { return filename; }
    public String getDriveFileId() { return driveFileId; }
    public long getSize() { return size; }
    public String getContentType() { return contentType; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getWebViewLink() { return webViewLink; }

    // --- Getters for RE-ADDED FIELDS ---
    public String getMimeType() { return mimeType; }
    public String getWebContentLink() { return webContentLink; }
    public LocalDateTime getLastModified() { return lastModified; }

    // --- Getters for NEW TEMPLATE FIELDS ---
    public String getTemplateName() { return templateName; }
    public String getTemplateCategory() { return templateCategory; }
    public String getTemplateImageUrl() { return templateImageUrl; }
    public String getTemplateDescription() { return templateDescription; }
    public String getTemplateSpotlight() { return templateSpotlight; }
    public String getTemplateProvider() { return templateProvider; }
    public String getOriginalTemplateDriveId() { return originalTemplateDriveId; }

    // --- Setters ---

    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setDriveFileId(String driveFileId) { this.driveFileId = driveFileId; }
    public void setSize(long size) { this.size = size; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setWebViewLink(String webViewLink) { this.webViewLink = webViewLink; }

    // --- Setters for RE-ADDED FIELDS ---
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public void setWebContentLink(String webContentLink) { this.webContentLink = webContentLink; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    // --- Setters for NEW TEMPLATE FIELDS ---
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public void setTemplateCategory(String templateCategory) { this.templateCategory = templateCategory; }
    public void setTemplateImageUrl(String templateImageUrl) { this.templateImageUrl = templateImageUrl; }
    public void setTemplateDescription(String templateDescription) { this.templateDescription = templateDescription; }
    public void setTemplateSpotlight(String templateSpotlight) { this.templateSpotlight = templateSpotlight; }
    public void setTemplateProvider(String templateProvider) { this.templateProvider = templateProvider; }
    public void setOriginalTemplateDriveId(String originalTemplateDriveId) { this.originalTemplateDriveId = originalTemplateDriveId; }


    // --- Builder Pattern ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private String filename;
        private String driveFileId;
        private long size;
        private String contentType;
        private LocalDateTime uploadedAt;
        private String webViewLink;
        // --- RE-ADDED FIELDS IN BUILDER ---
        private String mimeType;
        private String webContentLink;
        private LocalDateTime lastModified;
        // --- NEW TEMPLATE FIELDS IN BUILDER ---
        private String templateName;
        private String templateCategory;
        private String templateImageUrl;
        private String templateDescription;
        private String templateSpotlight;
        private String templateProvider;
        private String originalTemplateDriveId;


        private Builder() { }

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder filename(String filename) { this.filename = filename; return this; }
        public Builder driveFileId(String driveFileId) { this.driveFileId = driveFileId; return this; }
        public Builder size(long size) { this.size = size; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }
        public Builder webViewLink(String webViewLink) { this.webViewLink = webViewLink; return this; }

        // --- Builder methods for RE-ADDED FIELDS ---
        public Builder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public Builder webContentLink(String webContentLink) { this.webContentLink = webContentLink; return this; }
        public Builder lastModified(LocalDateTime lastModified) { this.lastModified = lastModified; return this; }

        // --- Builder methods for NEW TEMPLATE FIELDS ---
        public Builder templateName(String templateName) { this.templateName = templateName; return this; }
        public Builder templateCategory(String templateCategory) { this.templateCategory = templateCategory; return this; }
        public Builder templateImageUrl(String templateImageUrl) { this.templateImageUrl = templateImageUrl; return this; }
        public Builder templateDescription(String templateDescription) { this.templateDescription = templateDescription; return this; }
        public Builder templateSpotlight(String templateSpotlight) { this.templateSpotlight = templateSpotlight; return this; }
        public Builder templateProvider(String templateProvider) { this.templateProvider = templateProvider; return this; }
        public Builder originalTemplateDriveId(String originalTemplateDriveId) { this.originalTemplateDriveId = originalTemplateDriveId; return this; }


        public UserFile build() { return new UserFile(this); }
    }

    // --- toString, equals, hashCode ---

    @Override
    public String toString() {
        return "UserFile{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", filename='" + filename + '\'' +
                ", driveFileId='" + driveFileId + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", uploadedAt=" + uploadedAt +
                ", webViewLink='" + webViewLink + '\'' +
                ", mimeType='" + mimeType + '\'' + // Included re-added field
                ", webContentLink='" + webContentLink + '\'' + // Included re-added field
                ", lastModified=" + lastModified + // Included re-added field
                ", templateName='" + templateName + '\'' +
                ", templateCategory='" + templateCategory + '\'' +
                ", templateImageUrl='" + templateImageUrl + '\'' +
                ", templateDescription='" + templateDescription + '\'' +
                ", templateSpotlight='" + templateSpotlight + '\'' +
                ", templateProvider='" + templateProvider + '\'' +
                ", originalTemplateDriveId='" + originalTemplateDriveId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFile userFile = (UserFile) o;
        return size == userFile.size &&
                Objects.equals(id, userFile.id) &&
                Objects.equals(userId, userFile.userId) &&
                Objects.equals(filename, userFile.filename) &&
                Objects.equals(driveFileId, userFile.driveFileId) &&
                Objects.equals(contentType, userFile.contentType) &&
                Objects.equals(uploadedAt, userFile.uploadedAt) &&
                Objects.equals(webViewLink, userFile.webViewLink) &&
                Objects.equals(mimeType, userFile.mimeType) && // Included re-added field
                Objects.equals(webContentLink, userFile.webContentLink) && // Included re-added field
                Objects.equals(lastModified, userFile.lastModified) && // Included re-added field
                Objects.equals(templateName, userFile.templateName) &&
                Objects.equals(templateCategory, userFile.templateCategory) &&
                Objects.equals(templateImageUrl, userFile.templateImageUrl) &&
                Objects.equals(templateDescription, userFile.templateDescription) &&
                Objects.equals(templateSpotlight, userFile.templateSpotlight) &&
                Objects.equals(templateProvider, userFile.templateProvider) &&
                Objects.equals(originalTemplateDriveId, userFile.originalTemplateDriveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, filename, driveFileId, size, contentType, uploadedAt, webViewLink,
                mimeType, webContentLink, lastModified, // Included re-added fields
                templateName, templateCategory, templateImageUrl, templateDescription, templateSpotlight,
                templateProvider, originalTemplateDriveId);
    }
}