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
    private String contentType;
    private LocalDateTime uploadedAt;
    private String webViewLink; // Added: Field for the Google Drive web view link

    // --- Constructors ---

    public UserFile() {
    }

    // Updated constructor to include webViewLink
    public UserFile(String id, String userId, String filename, String driveFileId, long size, String contentType, LocalDateTime uploadedAt, String webViewLink) {
        this.id = id;
        this.userId = userId;
        this.filename = filename;
        this.driveFileId = driveFileId;
        this.size = size;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
        this.webViewLink = webViewLink; // Initialize the new field
    }

    // Updated private constructor for Builder to include webViewLink
    private UserFile(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.filename = builder.filename;
        this.driveFileId = builder.driveFileId;
        this.size = builder.size;
        this.contentType = builder.contentType;
        this.uploadedAt = builder.uploadedAt;
        this.webViewLink = builder.webViewLink; // Initialize webViewLink from builder
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFilename() { return filename; }
    public String getDriveFileId() { return driveFileId; }
    public long getSize() { return size; }
    public String getContentType() { return contentType; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getWebViewLink() { return webViewLink; } // Added: Getter for webViewLink

    // --- Setters ---

    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setDriveFileId(String driveFileId) { this.driveFileId = driveFileId; }
    public void setSize(long size) { this.size = size; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; } // Renamed param for consistency
    public void setWebViewLink(String webViewLink) { this.webViewLink = webViewLink; } // Correctly implemented setter

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
        private String webViewLink; // Added: Field for webViewLink in Builder

        private Builder() { }

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder filename(String filename) { this.filename = filename; return this; }
        public Builder driveFileId(String driveFileId) { this.driveFileId = driveFileId; return this; }
        public Builder size(long size) { this.size = size; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }
        public Builder webViewLink(String webViewLink) { this.webViewLink = webViewLink; return this; } // Added: Builder method for webViewLink

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
                ", webViewLink='" + webViewLink + '\'' + // Included webViewLink in toString
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
                Objects.equals(webViewLink, userFile.webViewLink); // Included webViewLink in equals
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, filename, driveFileId, size, contentType, uploadedAt, webViewLink); // Included webViewLink in hashCode
    }
}