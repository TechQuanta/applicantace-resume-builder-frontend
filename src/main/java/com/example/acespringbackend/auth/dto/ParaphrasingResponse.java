package com.example.acespringbackend.auth.dto;

public class ParaphrasingResponse {
    private String content;
    private boolean error;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String errorMessage;

    // Getters & Setters
}

