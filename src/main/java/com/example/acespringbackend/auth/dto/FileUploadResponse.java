// src/main/java/com/example/acespringbackend/auth/dto/FileUploadResponse.java
package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class FileUploadResponse {
    private boolean success;
    private String message;
    private String fileId;
    private String fileName;
    private long fileSize; // in bytes
    private String fileType;
    private double currentStorageUsageMb;
    private double maxStorageQuotaMb;

    // No-argument constructor
    public FileUploadResponse() {
    }

    // All-argument constructor
    public FileUploadResponse(boolean success, String message, String fileId, String fileName,
                              long fileSize, String fileType, double currentStorageUsageMb,
                              double maxStorageQuotaMb) {
        this.success = success;
        this.message = message;
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.currentStorageUsageMb = currentStorageUsageMb;
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }

    // --- Builder Class (manual implementation for clarity and flexibility) ---
    public static class FileUploadResponseBuilder {
        private boolean success;
        private String message;
        private String fileId;
        private String fileName;
        private long fileSize;
        private String fileType;
        private double currentStorageUsageMb;
        private double maxStorageQuotaMb;

        public FileUploadResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public FileUploadResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public FileUploadResponseBuilder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public FileUploadResponseBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public FileUploadResponseBuilder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public FileUploadResponseBuilder fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public FileUploadResponseBuilder currentStorageUsageMb(double currentStorageUsageMb) {
            this.currentStorageUsageMb = currentStorageUsageMb;
            return this;
        }

        public FileUploadResponseBuilder maxStorageQuotaMb(double maxStorageQuotaMb) {
            this.maxStorageQuotaMb = maxStorageQuotaMb;
            return this;
        }

        public FileUploadResponse build() {
            return new FileUploadResponse(success, message, fileId, fileName,
                    fileSize, fileType, currentStorageUsageMb,
                    maxStorageQuotaMb);
        }
    }

    // Static method to get a new builder instance
    public static FileUploadResponseBuilder builder() {
        return new FileUploadResponseBuilder();
    }


    // --- Getters ---
    // Explicitly defined getSuccess()
    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public double getCurrentStorageUsageMb() {
        return currentStorageUsageMb;
    }

    public double getMaxStorageQuotaMb() {
        return maxStorageQuotaMb;
    }

    // --- Setters ---
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setCurrentStorageUsageMb(double currentStorageUsageMb) {
        this.currentStorageUsageMb = currentStorageUsageMb;
    }

    public void setMaxStorageQuotaMb(double maxStorageQuotaMb) {
        this.maxStorageQuotaMb = maxStorageQuotaMb;
    }
}
