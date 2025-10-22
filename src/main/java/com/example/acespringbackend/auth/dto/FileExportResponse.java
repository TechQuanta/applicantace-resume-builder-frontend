package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for conveying the response after a file export or
 * conversion operation from Google Drive. This DTO includes the binary content
 * of the exported file along with metadata and updated storage usage information.
 */
public class FileExportResponse {

    /**
     * A boolean flag indicating whether the file export operation was successful.
     * {@code true} if the export was successful, {@code false} otherwise.
     */
    private boolean success;

    /**
     * A descriptive message providing context about the export operation's outcome,
     * such as a success confirmation or a detailed error message.
     */
    private String message;

    /**
     * The name of the file after it has been successfully exported (e.g., "document.pdf").
     * This may differ from the original file name if a new name was specified in the request.
     */
    private String exportedFileName;

    /**
     * The MIME type of the file after it has been exported (e.g., "application/pdf", "text/plain").
     * This confirms the format of the {@code fileContent}.
     */
    private String exportedFileMimeType;

    /**
     * The binary content of the exported file. This byte array contains the actual
     * data of the converted file, ready to be transmitted or saved.
     */
    private byte[] fileContent;

    /**
     * The user's current Google Drive storage usage in megabytes (MB)
     * after the file export operation. This provides updated storage status.
     */
    private double currentStorageUsageMb;

    /**
     * The maximum Google Drive storage quota available to the user in megabytes (MB).
     * This helps the client application display storage limits.
     */
    private double maxStorageQuotaMb;

    /**
     * Default constructor for {@code FileExportResponse}.
     * This constructor is necessary for frameworks like Spring to deserialize JSON
     * or form data into an instance of this object.
     */
    public FileExportResponse() {
        // Default constructor
    }

    /**
     * Constructs a new {@code FileExportResponse} with all relevant details
     * regarding the outcome of a file export operation.
     *
     * @param success             A boolean indicating if the export was successful.
     * @param message             A descriptive message about the operation's outcome.
     * @param exportedFileName    The name of the file after export.
     * @param exportedFileMimeType The MIME type of the exported file.
     * @param fileContent         The binary content of the exported file.
     * @param currentStorageUsageMb The user's updated current storage usage in MB.
     * @param maxStorageQuotaMb   The user's maximum storage quota in MB.
     */
    public FileExportResponse(boolean success, String message, String exportedFileName,
                              String exportedFileMimeType, byte[] fileContent,
                              double currentStorageUsageMb, double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.exportedFileName = exportedFileName;
        this.exportedFileMimeType = exportedFileMimeType;
        this.fileContent = fileContent;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // --- Getters ---

    /**
     * Retrieves the success status of the file export operation.
     *
     * @return {@code true} if the export succeeded, {@code false} otherwise.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Retrieves the message related to the file export operation's outcome.
     *
     * @return A descriptive message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the name of the file after export.
     *
     * @return The exported file's name.
     */
    public String getExportedFileName() {
        return exportedFileName;
    }

    /**
     * Retrieves the MIME type of the file after export.
     *
     * @return The exported file's MIME type.
     */
    public String getExportedFileMimeType() {
        return exportedFileMimeType;
    }

    /**
     * Retrieves the binary content of the exported file.
     *
     * @return A byte array containing the exported file's content. Returns {@code null} if no content.
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * Retrieves the user's current storage usage after the export.
     *
     * @return The current storage usage in megabytes (MB).
     */
    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    /**
     * Retrieves the user's maximum allowed storage quota.
     *
     * @return The maximum storage quota in megabytes (MB).
     */
    public double getMaxStorageQuotaMb() {
        return maxStorageQuotaMb;
    }

    // --- Setters ---

    /**
     * Sets the success status of the file export operation.
     *
     * @param success {@code true} for success, {@code false} for failure.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Sets the message related to the file export operation.
     *
     * @param message A descriptive message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the name of the file after export.
     *
     * @param exportedFileName The exported file's name to set.
     */
    public void setExportedFileName(String exportedFileName) {
        this.exportedFileName = exportedFileName;
    }

    /**
     * Sets the MIME type of the file after export.
     *
     * @param exportedFileMimeType The exported file's MIME type to set.
     */
    public void setExportedFileMimeType(String exportedFileMimeType) {
        this.exportedFileMimeType = exportedFileMimeType;
    }

    /**
     * Sets the binary content of the exported file.
     *
     * @param fileContent The byte array containing the exported file's content to set.
     */
    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    /**
     * Sets the user's current storage usage after the export.
     *
     * @param currentStorageUsageMb The current storage usage in megabytes (MB) to set.
     */
    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    /**
     * Sets the user's maximum allowed storage quota.
     *
     * @param maxStorageQuotaMb The maximum storage quota in megabytes (MB) to set.
     */
    public void setMaxStorageQuotaMb(double maxStorageQuotaMb) {
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }
}