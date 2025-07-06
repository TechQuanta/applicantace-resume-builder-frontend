package com.example.acespringbackend.auth.dto;

import java.time.LocalDateTime;

/**
 * DTO representing detailed information for a single file.
 * This is typically used within a list of files to be sent to the frontend.
 */
public class FileDetail {
    private String mongoFileId;     // MongoDB _id of the UserFile document (internal tracking)
    private String driveFileId;     // Google Drive File ID (external, for Drive API interaction)
    private String fileName;
    private String fileMimeType;    // Content type of the file (e.g., "image/jpeg", "application/pdf")
    private Long fileSizeInBytes;
    private String uploadedAt;      // ISO 8601 string representation of upload time (e.g., "2023-10-27T10:30:00")
    private String webViewLink;     // Link to view the file in Google Drive (often a /view link)
    private String thumbnailLink;   // Optional: Link to a thumbnail image of the file (if available from Drive)
    private String embedLink;       // New: Link suitable for embedding in an <iframe> (e.g., /preview or /pub?embedded=true)
    private String provider;        // <--- ADDED THIS FIELD!

    /**
     * Default no-argument constructor for deserialization frameworks (like Jackson).
     */
    public FileDetail() {
    }

    /**
     * Full constructor for direct instantiation of a FileDetail object.
     * This constructor includes all file attributes, including the new embedLink and provider.
     *
     * @param mongoFileId      The MongoDB document ID for the file record.
     * @param driveFileId      The Google Drive ID for the file.
     * @param fileName         The name of the file.
     * @param fileMimeType     The MIME type of the file.
     * @param fileSizeInBytes  The size of the file in bytes.
     * @param uploadedAt       The timestamp of when the file was uploaded, in ISO 8601 string format.
     * @param webViewLink      The public web view link for the file on Google Drive.
     * @param thumbnailLink    The link to a thumbnail image of the file.
     * @param embedLink        The link specifically designed for embedding the file.
     * @param provider         The source provider of the template (e.g., "GoogleDrive").
     */
    public FileDetail(
            String mongoFileId,
            String driveFileId,
            String fileName,
            String fileMimeType,
            Long fileSizeInBytes,
            String uploadedAt,
            String webViewLink,
            String thumbnailLink,
            String embedLink,
            String provider) { // Correctly includes 'provider' in parameters
        this.mongoFileId = mongoFileId;
        this.driveFileId = driveFileId;
        this.fileName = fileName;
        this.fileMimeType = fileMimeType;
        this.fileSizeInBytes = fileSizeInBytes;
        this.uploadedAt = uploadedAt;
        this.webViewLink = webViewLink;
        this.thumbnailLink = thumbnailLink;
        this.embedLink = embedLink;
        this.provider = provider; // <--- ADDED THIS ASSIGNMENT!
    }

    // --- Getters ---
    public String getMongoFileId() {
        return mongoFileId;
    }

    public String getDriveFileId() {
        return driveFileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public Long getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public String getEmbedLink() {
        return embedLink;
    }

    public String getProvider() { // <--- ADDED THIS GETTER!
        return provider;
    }

    // --- Setters ---
    public void setMongoFileId(String mongoFileId) {
        this.mongoFileId = mongoFileId;
    }

    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = fileMimeType;
    }

    public void setFileSizeInBytes(Long fileSizeInBytes) {
        this.fileSizeInBytes = fileSizeInBytes;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    public void setEmbedLink(String embedLink) {
        this.embedLink = embedLink;
    }

    public void setProvider(String provider) { // <--- ADDED THIS SETTER!
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "FileDetail{" +
               "mongoFileId='" + mongoFileId + '\'' +
               ", driveFileId='" + driveFileId + '\'' +
               ", fileName='" + fileName + '\'' +
               ", fileMimeType='" + fileMimeType + '\'' +
               ", fileSizeInBytes=" + fileSizeInBytes +
               ", uploadedAt='" + uploadedAt + '\'' +
               ", webViewLink='" + webViewLink + '\'' +
               ", thumbnailLink='" + thumbnailLink + '\'' +
               ", embedLink='" + embedLink + '\'' +
               ", provider='" + provider + '\'' + // <--- ADDED TO toString
               '}';
    }
}