package com.example.acespringbackend.auth.dto;

public class ParaphrasingResponse {
    private String paraphrasedContent;
    private boolean error;
    private String errorMessage;

    public ParaphrasingResponse(String paraphrasedContent, boolean error, String errorMessage) {
        this.paraphrasedContent = paraphrasedContent;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public String getParaphrasedContent() { return paraphrasedContent; }
    public void setParaphrasedContent(String paraphrasedContent) { this.paraphrasedContent = paraphrasedContent; }
    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}