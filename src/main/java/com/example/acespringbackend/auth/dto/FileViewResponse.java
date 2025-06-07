package com.example.acespringbackend.auth.dto;

// No Lombok imports needed anymore

public class FileViewResponse {

    private boolean success;
    private String message;
    private String fileId;
    private String webViewLink;
    private String docTitle;

    // --- Constructors ---

    // No-argument constructor (replaces @NoArgsConstructor)
    public FileViewResponse() {
    }

    // All-argument constructor (replaces @AllArgsConstructor)
    public FileViewResponse(boolean success, String message, String fileId, String webViewLink, String docTitle) {
        this.success = success;
        this.message = message;
        this.fileId = fileId;
        this.webViewLink = webViewLink;
        this.docTitle = docTitle;
    }

    // Private constructor for the Builder pattern
    private FileViewResponse(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.fileId = builder.fileId;
        this.webViewLink = builder.webViewLink;
        this.docTitle = builder.docTitle;
    }

    // --- Getters --- (replaces @Getter)

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getFileId() {
        return fileId;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public String getDocTitle() {
        return docTitle;
    }

    // --- Setters --- (replaces @Setter)

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    // --- Builder Pattern --- (replaces @Builder)

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private String fileId;
        private String webViewLink;
        private String docTitle;

        // Private constructor for the Builder class itself
        private Builder() {
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder webViewLink(String webViewLink) {
            this.webViewLink = webViewLink;
            return this;
        }

        public Builder docTitle(String docTitle) {
            this.docTitle = docTitle;
            return this;
        }

        public FileViewResponse build() {
            return new FileViewResponse(this);
        }
    }

    @Override
    public String toString() {
        return "FileViewResponse{" +
               "success=" + success +
               ", message='" + message + '\'' +
               ", fileId='" + fileId + '\'' +
               ", webViewLink='" + webViewLink + '\'' +
               ", docTitle='" + docTitle + '\'' +
               '}';
    }
}
