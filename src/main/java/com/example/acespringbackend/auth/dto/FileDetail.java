package com.example.acespringbackend.auth.dto;

import java.time.LocalDateTime; // This import is not used if 'uploadedAt' is String

/**
 * Data Transfer Object (DTO) providing detailed information for a single file.
 * This DTO is designed to encapsulate all relevant metadata about a file stored
 * both internally (MongoDB) and externally (Google Drive), making it suitable
 * for display or processing in frontend applications.
 */
public class FileDetail {

    /**
     * The unique identifier (_id) of the file document in MongoDB.
     * This is used for internal application tracking and database operations.
     */
    private String mongoFileId;

    /**
     * The unique identifier (File ID) of the file as assigned by Google Drive.
     * This ID is essential for interacting with the Google Drive API (e.g., downloading, deleting).
     */
    private String driveFileId;

    /**
     * The human-readable name of the file, including its extension (e.g., "my_resume.pdf").
     */
    private String fileName;

    /**
     * The MIME type (content type) of the file (e.g., "application/pdf", "image/jpeg", "text/plain").
     * This helps client applications correctly render or handle the file.
     */
    private String fileMimeType;

    /**
     * The size of the file in bytes.
     */
    private Long fileSizeInBytes;

    /**
     * The timestamp indicating when the file was uploaded, formatted as an ISO 8601 string
     * (e.g., "2023-10-27T10:30:00"). This provides a standardized time representation.
     */
    private String uploadedAt;

    /**
     * A public web link to view the file directly in Google Drive, suitable for opening in a browser.
     */
    private String webViewLink;

    /**
     * An optional link to a thumbnail image of the file, if available from Google Drive.
     * This is useful for displaying previews in file lists.
     */
    private String thumbnailLink;

    /**
     * A link specifically designed for embedding the file content directly into web pages,
     * typically suitable for use within an HTML {@code <iframe>} element (e.g., a Google Docs viewer link).
     */
    private String embedLink;

    /**
     * The source provider of the file or template. This helps identify where the file originated
     * from (e.g., "GoogleDrive", "OneDrive", "LocalUpload").
     */
    private String provider;

    /**
     * Default no-argument constructor.
     * Required by deserialization frameworks (like Jackson) to instantiate the DTO.
     */
    public FileDetail() {
    }

    /**
     * Full constructor for direct instantiation of a {@code FileDetail} object with all its attributes.
     *
     * @param mongoFileId      The MongoDB document ID.
     * @param driveFileId      The Google Drive File ID.
     * @param fileName         The name of the file.
     * @param fileMimeType     The MIME type of the file.
     * @param fileSizeInBytes  The size of the file in bytes.
     * @param uploadedAt       The upload timestamp in ISO 8601 string format.
     * @param webViewLink      The public web view link for the file.
     * @param thumbnailLink    The link to a thumbnail image (can be {@code null}).
     * @param embedLink        The link suitable for embedding (can be {@code null}).
     * @param provider         The source provider of the file.
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
            String provider) {
        this.mongoFileId = mongoFileId;
        this.driveFileId = driveFileId;
        this.fileName = fileName;
        this.fileMimeType = fileMimeType;
        this.fileSizeInBytes = fileSizeInBytes;
        this.uploadedAt = uploadedAt;
        this.webViewLink = webViewLink;
        this.thumbnailLink = thumbnailLink;
        this.embedLink = embedLink;
        this.provider = provider;
    }

    // --- Getters ---

    /**
     * Retrieves the MongoDB document ID for the file record.
     * @return The MongoDB file ID.
     */
    public String getMongoFileId() {
        return mongoFileId;
    }

    /**
     * Retrieves the Google Drive File ID.
     * @return The Google Drive file ID.
     */
    public String getDriveFileId() {
        return driveFileId;
    }

    /**
     * Retrieves the name of the file.
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Retrieves the MIME type of the file.
     * @return The file MIME type.
     */
    public String getFileMimeType() {
        return fileMimeType;
    }

    /**
     * Retrieves the size of the file in bytes.
     * @return The file size in bytes.
     */
    public Long getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    /**
     * Retrieves the upload timestamp of the file in ISO 8601 string format.
     * @return The upload timestamp string.
     */
    public String getUploadedAt() {
        return uploadedAt;
    }

    /**
     * Retrieves the public web view link for the file on Google Drive.
     * @return The web view link.
     */
    public String getWebViewLink() {
        return webViewLink;
    }

    /**
     * Retrieves the link to a thumbnail image of the file.
     * @return The thumbnail link, or {@code null} if not available.
     */
    public String getThumbnailLink() {
        return thumbnailLink;
    }

    /**
     * Retrieves the link specifically designed for embedding the file.
     * @return The embed link, or {@code null} if not available.
     */
    public String getEmbedLink() {
        return embedLink;
    }

    /**
     * Retrieves the source provider of the file.
     * @return The provider name.
     */
    public String getProvider() {
        return provider;
    }

    // --- Setters ---

    /**
     * Sets the MongoDB document ID for the file record.
     * @param mongoFileId The MongoDB file ID to set.
     */
    public void setMongoFileId(String mongoFileId) {
        this.mongoFileId = mongoFileId;
    }

    /**
     * Sets the Google Drive File ID.
     * @param driveFileId The Google Drive file ID to set.
     */
    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    /**
     * Sets the name of the file.
     * @param fileName The file name to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Sets the MIME type of the file.
     * @param fileMimeType The file MIME type to set.
     */
    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = fileMimeType;
    }

    /**
     * Sets the size of the file in bytes.
     * @param fileSizeInBytes The file size in bytes to set.
     */
    public void setFileSizeInBytes(Long fileSizeInBytes) {
        this.fileSizeInBytes = fileSizeInBytes;
    }

    /**
     * Sets the upload timestamp of the file in ISO 8601 string format.
     * @param uploadedAt The upload timestamp string to set.
     */
    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    /**
     * Sets the public web view link for the file on Google Drive.
     * @param webViewLink The web view link to set.
     */
    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    /**
     * Sets the link to a thumbnail image of the file.
     * @param thumbnailLink The thumbnail link to set.
     */
    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    /**
     * Sets the link specifically designed for embedding the file.
     * @param embedLink The embed link to set.
     */
    public void setEmbedLink(String embedLink) {
        this.embedLink = embedLink;
    }

    /**
     * Sets the source provider of the file.
     * @param provider The provider name to set.
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Provides a string representation of the {@code FileDetail} object,
     * useful for logging and debugging.
     * @return A string containing all file details.
     */
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
               ", provider='" + provider + '\'' +
               '}';
    }
}