package com.example.acespringbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for requests to process a file using Adobe PDF Services,
 * providing a URL to the input file.
 */
public class FileProcessRequest { // Renamed from AdobeProcessRequest
    @NotBlank(message = "File URL cannot be empty")
    private String fileUrl;

    // Optional: Define the desired output format if the service supports it
    // For simplicity, we'll assume PDF conversion for this example, but
    // you could extend this to "docx", "jpeg", etc. for export operations.
    private String outputFormat;

    // Default constructor
    public FileProcessRequest() {
    }

    public FileProcessRequest(String fileUrl, String outputFormat) {
        this.fileUrl = fileUrl;
        this.outputFormat = outputFormat;
    }

    // Getters and Setters
    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    @Override
    public String toString() {
        return "FileProcessRequest{" +
               "fileUrl='" + fileUrl + '\'' +
               ", outputFormat='" + outputFormat + '\'' +
               '}';
    }
}