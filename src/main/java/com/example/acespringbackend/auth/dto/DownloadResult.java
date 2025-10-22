package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for conveying the outcome of a file download operation
 * from Google Drive. It encapsulates the binary content of the file, its metadata,
 * and the status of the download.
 */
public class DownloadResult {

    /**
     * Indicates whether the file download operation was successful.
     * {@code true} if the file was successfully retrieved, {@code false} otherwise.
     */
    private boolean success;

    /**
     * A descriptive message regarding the download operation's outcome.
     * This can convey success messages, warnings, or detailed error information.
     */
    private String message;

    /**
     * The binary content of the downloaded file. This byte array contains the
     * actual data of the file, ready to be streamed or saved.
     */
    private byte[] fileContent;

    /**
     * The original name of the downloaded file, including its extension.
     */
    private String fileName;

    /**
     * The MIME (Multipurpose Internet Mail Extensions) type of the downloaded file.
     * This helps in identifying the file's format (e.g., "application/pdf", "image/png").
     */
    private String mimeType;

    /**
     * Default constructor for {@code DownloadResult}.
     * This is typically used by frameworks for deserialization purposes.
     */
    public DownloadResult() {
    }

    /**
     * Constructs a new {@code DownloadResult} with all relevant details of the download operation.
     * This constructor is used to fully populate the response object after a download attempt.
     *
     * @param success     {@code true} if the download was successful, {@code false} otherwise.
     * @param message     A descriptive message about the download's outcome.
     * @param fileContent The binary content of the downloaded file. Can be {@code null} if download failed.
     * @param fileName    The name of the downloaded file. Can be {@code null} or empty if download failed.
     * @param mimeType    The MIME type of the downloaded file. Can be {@code null} or empty if download failed.
     */
    public DownloadResult(boolean success, String message, byte[] fileContent, String fileName, String mimeType) {
        this.success = success;
        this.message = message;
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.mimeType = mimeType;
    }

    // --- Getters ---

    /**
     * Checks if the file download was successful.
     *
     * @return {@code true} if the download succeeded, {@code false} otherwise.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Retrieves the message describing the download's outcome.
     *
     * @return A {@link String} message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the binary content of the downloaded file.
     *
     * @return A byte array containing the file's content. Returns {@code null} if no content.
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * Retrieves the name of the downloaded file.
     *
     * @return The file name as a {@link String}.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Retrieves the MIME type of the downloaded file.
     *
     * @return The MIME type as a {@link String}.
     */
    public String getMimeType() {
        return mimeType;
    }

    // --- Setters ---

    /**
     * Sets the success status of the file download.
     *
     * @param success The boolean success status to set.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Sets the message describing the download's outcome.
     *
     * @param message The message {@link String} to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the binary content of the downloaded file.
     *
     * @param fileContent The byte array containing the file's content to set.
     */
    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    /**
     * Sets the name of the downloaded file.
     *
     * @param fileName The file name {@link String} to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Sets the MIME type of the downloaded file.
     *
     * @param mimeType The MIME type {@link String} to set.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}