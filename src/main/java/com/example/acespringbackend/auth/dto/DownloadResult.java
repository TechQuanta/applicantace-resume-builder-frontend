package com.example.acespringbackend.auth.dto;

/**
 * DTO for returning the result of a file download operation from Google Drive.
 * Contains the binary content of the file, its name, and MIME type,
 * along with success status and any relevant messages.
 */
public class DownloadResult {
    private boolean success;
    private String message;
    private byte[] fileContent;
    private String fileName;
    private String mimeType;

    /**
     * Default constructor.
     */
    public DownloadResult() {
    }

    /**
     * All-arguments constructor.
     * @param success True if the download was successful, false otherwise.
     * @param message A message describing the outcome of the download operation.
     * @param fileContent The binary content of the downloaded file.
     * @param fileName The name of the downloaded file.
     * @param mimeType The MIME type of the downloaded file.
     */
    public DownloadResult(boolean success, String message, byte[] fileContent, String fileName, String mimeType) {
        this.success = success;
        this.message = message;
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.mimeType = mimeType;
    }

    // Getters and Setters

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
