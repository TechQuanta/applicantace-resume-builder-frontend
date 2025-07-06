package com.example.acespringbackend.auth.dto;

public class AtsResponse {
    private String score;
    private String fullGeminiResponse; // Holds the AI's score and feedback (e.g., in Markdown)
    private String extractedResumeContent; // NEW: Holds the raw text extracted from the PDF
    private boolean error;
    private String errorMessage;

    public AtsResponse(String score) {
        this.score = score;
        this.fullGeminiResponse = "";
        this.extractedResumeContent = ""; // Initialize new field
        this.error = false;
        this.errorMessage = null;
    }

    public AtsResponse(String score, String fullGeminiResponse, String extractedResumeContent, boolean error, String errorMessage) {
        this.score = score;
        this.fullGeminiResponse = fullGeminiResponse;
        this.extractedResumeContent = extractedResumeContent;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getFullGeminiResponse() {
        return fullGeminiResponse;
    }

    public void setFullGeminiResponse(String fullGeminiResponse) {
        this.fullGeminiResponse = fullGeminiResponse;
    }

    public String getExtractedResumeContent() {
        return extractedResumeContent;
    }

    public void setExtractedResumeContent(String extractedResumeContent) {
        this.extractedResumeContent = extractedResumeContent;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String setErrorMessage(String errorMessage) {
        return errorMessage;
    }
}