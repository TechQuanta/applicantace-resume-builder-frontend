package com.example.acespringbackend.auth.dto;

/**
 * Represents the response structure for the Applicant Tracking System (ATS) service.
 * This DTO encapsulates the results of resume processing, including AI-generated feedback
 * and extracted content, designed for seamless data transfer within the application.
 */
public class AtsResponse {

    /**
     * The numerical or categorical score assigned to the resume based on ATS analysis.
     */
    private String score;

    /**
     * The complete response from the AI model (e.g., Gemini), often including detailed
     * feedback, suggestions, or analysis formatted in Markdown or plain text.
     */
    private String fullGeminiResponse;

    /**
     * The raw textual content extracted directly from the uploaded resume document
     * (e.g., from a PDF or DOCX file), used for further processing or display.
     */
    private String extractedResumeContent;

    /**
     * A boolean flag indicating whether an error occurred during the resume processing.
     * True if an error was encountered, false otherwise.
     */
    private boolean error;

    /**
     * An optional message providing details about an error, if {@code error} is true.
     * This helps in debugging and providing user-friendly error feedback.
     */
    private String errorMessage;

    /**
     * Constructs a new {@code AtsResponse} with only a score.
     * This constructor is suitable for initial responses where detailed AI feedback
     * or extracted content might not yet be available, or for quick success indicators.
     * Error status is set to false by default.
     *
     * @param score The resume score to be set.
     */
    public AtsResponse(String score) {
        this.score = score;
        this.fullGeminiResponse = "";
        this.extractedResumeContent = "";
        this.error = false;
        this.errorMessage = null;
    }

    /**
     * Constructs a comprehensive {@code AtsResponse} with all possible fields.
     * This is typically used when the resume processing is complete, and all
     * AI feedback, extracted content, and error statuses are finalized.
     *
     * @param score The score assigned to the resume.
     * @param fullGeminiResponse The full response from the Gemini AI.
     * @param extractedResumeContent The raw text content extracted from the resume.
     * @param error A boolean indicating if an error occurred (true for error, false for success).
     * @param errorMessage A descriptive message for any error encountered; null if no error.
     */
    public AtsResponse(String score, String fullGeminiResponse, String extractedResumeContent, boolean error, String errorMessage) {
        this.score = score;
        this.fullGeminiResponse = fullGeminiResponse;
        this.extractedResumeContent = extractedResumeContent;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    /**
     * Retrieves the score assigned to the resume.
     *
     * @return The resume score as a String.
     */
    public String getScore() {
        return score;
    }

    /**
     * Sets the score for the resume.
     *
     * @param score The score to be set.
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     * Retrieves the full AI (Gemini) response.
     *
     * @return The complete AI response, typically in Markdown format.
     */
    public String getFullGeminiResponse() {
        return fullGeminiResponse;
    }

    /**
     * Sets the full AI (Gemini) response.
     *
     * @param fullGeminiResponse The full AI response to be set.
     */
    public void setFullGeminiResponse(String fullGeminiResponse) {
        this.fullGeminiResponse = fullGeminiResponse;
    }

    /**
     * Retrieves the raw text content extracted from the resume.
     *
     * @return The extracted resume content as a String.
     */
    public String getExtractedResumeContent() {
        return extractedResumeContent;
    }

    /**
     * Sets the raw text content extracted from the resume.
     *
     * @param extractedResumeContent The extracted resume content to be set.
     */
    public void setExtractedResumeContent(String extractedResumeContent) {
        this.extractedResumeContent = extractedResumeContent;
    }

    /**
     * Checks if an error occurred during the ATS processing.
     *
     * @return True if an error was encountered, false otherwise.
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets the error status for the response.
     *
     * @param error The boolean error status to be set.
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Retrieves the error message associated with the response.
     *
     * @return The error message as a String, or null if no error occurred.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message for the response.
     *
     * @param errorMessage The error message to be set.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}