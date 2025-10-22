package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for requests to export or convert a file stored
 * in Google Drive to a different MIME type. This DTO carries all necessary
 * parameters for the file export operation.
 */
public class FileExportRequest {

    /**
     * The email address of the user initiating the file export.
     * This is crucial for user authentication and authorization, ensuring the
     * user has permissions to access and export the specified file.
     */
    private String userEmail;

    /**
     * The unique identifier (File ID) of the file in Google Drive that is to be exported.
     * This ID directly points to the source file for the conversion.
     */
    private String fileId;

    /**
     * The desired MIME type for the exported file (e.g., "application/pdf", "text/plain").
     * This specifies the target format for the conversion.
     */
    private String exportMimeType;

    /**
     * An optional new name for the exported file (without the file extension).
     * If provided, the exported file will be saved with this name; otherwise,
     * Google Drive's default naming convention or the original file name might be used.
     */
    private String newFileName;

    /**
     * Default no-argument constructor for {@code FileExportRequest}.
     * This is necessary for frameworks like Spring to properly deserialize JSON or
     * form data into an instance of this object.
     */
    public FileExportRequest() {
        // Default constructor
    }

    /**
     * Constructs a new {@code FileExportRequest} with all parameters.
     * This constructor allows for direct instantiation with all required and optional fields.
     *
     * @param userEmail      The email address of the user making the request.
     * @param fileId         The Google Drive file ID of the file to export.
     * @param exportMimeType The desired MIME type for the exported file.
     * @param newFileName    An optional new name for the exported file (can be {@code null}).
     */
    public FileExportRequest(String userEmail, String fileId, String exportMimeType, String newFileName) {
        this.userEmail = userEmail;
        this.fileId = fileId;
        this.exportMimeType = exportMimeType;
        this.newFileName = newFileName;
    }

    // --- Getters ---

    /**
     * Retrieves the email address of the user.
     *
     * @return The user's email address as a {@link String}.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Retrieves the Google Drive file ID of the file to be exported.
     *
     * @return The file ID as a {@link String}.
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Retrieves the desired MIME type for the exported file.
     *
     * @return The export MIME type as a {@link String}.
     */
    public String getExportMimeType() {
        return exportMimeType;
    }

    /**
     * Retrieves the optional new file name for the exported file.
     *
     * @return The new file name as a {@link String}, or {@code null} if not specified.
     */
    public String getNewFileName() {
        return newFileName;
    }

    // --- Setters ---

    /**
     * Sets the email address of the user.
     *
     * @param userEmail The user's email address to set.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Sets the Google Drive file ID of the file to be exported.
     *
     * @param fileId The file ID to set.
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Sets the desired MIME type for the exported file.
     *
     * @param exportMimeType The export MIME type to set.
     */
    public void setExportMimeType(String exportMimeType) {
        this.exportMimeType = exportMimeType;
    }

    /**
     * Sets the optional new file name for the exported file.
     *
     * @param newFileName The new file name to set (can be {@code null}).
     */
    public  void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }
}