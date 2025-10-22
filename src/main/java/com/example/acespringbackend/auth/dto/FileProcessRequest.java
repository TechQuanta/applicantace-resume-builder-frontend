package com.example.acespringbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) for requests to process a file using an external
 * file processing service (e.g., Adobe PDF Services). This DTO contains the
 * URL of the input file and specifies the desired output format.
 */
public class FileProcessRequest {

    /**
     * The URL of the file to be processed. This URL should be publicly accessible
     * by the file processing service.
     * <p>
     * This field is mandatory and must not be blank.
     */
    @NotBlank(message = "File URL cannot be empty")
    private String fileUrl;

    /**
     * An optional field specifying the desired output format for the processed file
     * (e.g., "pdf", "docx", "jpeg"). If not specified, the service might use a default
     * or infer the format based on the operation.
     */
    private String outputFormat;

    /**
     * Default no-argument constructor for {@code FileProcessRequest}.
     * This constructor is essential for frameworks like Spring to automatically
     * deserialize JSON or form data into an instance of this object.
     */
    public FileProcessRequest() {
        // Default constructor
    }

    /**
     * Constructs a new {@code FileProcessRequest} with the specified file URL and
     * an optional output format.
     *
     * @param fileUrl      The URL of the file to be processed.
     * @param outputFormat The desired output format for the processed file (can be {@code null}).
     */
    public FileProcessRequest(String fileUrl, String outputFormat) {
        this.fileUrl = fileUrl;
        this.outputFormat = outputFormat;
    }

    // --- Getters ---

    /**
     * Retrieves the URL of the file to be processed.
     *
     * @return The file URL as a {@link String}.
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * Retrieves the desired output format for the processed file.
     *
     * @return The output format as a {@link String}, or {@code null} if not specified.
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    // --- Setters ---

    /**
     * Sets the URL of the file to be processed.
     *
     * @param fileUrl The file URL to set.
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * Sets the desired output format for the processed file.
     *
     * @param outputFormat The output format to set (can be {@code null}).
     */
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Provides a string representation of the {@code FileProcessRequest} object,
     * useful for logging and debugging.
     *
     * @return A string containing the file URL and output format.
     */
    @Override
    public String toString() {
        return "FileProcessRequest{" +
               "fileUrl='" + fileUrl + '\'' +
               ", outputFormat='" + outputFormat + '\'' +
               '}';
    }
}