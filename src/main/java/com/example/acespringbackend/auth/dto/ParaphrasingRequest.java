// src/main/java/com/example/acespringbackend/auth/dto/ParaphrasingRequest.java
package com.example.acespringbackend.auth.dto;

public class ParaphrasingRequest {

    private String input;
    private String tone;
    private String style; // e.g., formal, informal, academic, creative
    private String jobDescription; // Used for researched mode or cover letter generation
    private String keywords;
    private Integer wordLimit;
    private Boolean enableSuggestions; // To ask Gemini to highlight keywords (e.g., bold/italic)

    // New fields directly from ChatSettings.jsx
    private Boolean autoCoverLetterMode; // true if cover letter generation is requested
    private Boolean researchedMode;      // true if "researched" (job description analysis) mode is requested

    public ParaphrasingRequest() {
    }

    // Constructor updated to include new fields
    public ParaphrasingRequest(String input, String tone, String style, String jobDescription,
                               String keywords, Integer wordLimit, Boolean enableSuggestions,
                               Boolean autoCoverLetterMode, Boolean researchedMode) {
        this.input = input;
        this.tone = tone;
        this.style = style;
        this.jobDescription = jobDescription;
        this.keywords = keywords;
        this.wordLimit = wordLimit;
        this.enableSuggestions = enableSuggestions;
        this.autoCoverLetterMode = autoCoverLetterMode;
        this.researchedMode = researchedMode;
    }

    // --- Getters ---
    public String getInput() { return input; }
    public String getTone() { return tone; }
    public String getStyle() { return style; }
    public String getJobDescription() { return jobDescription; }
    public String getKeywords() { return keywords; }
    public Integer getWordLimit() { return wordLimit; }
    public Boolean getEnableSuggestions() { return enableSuggestions; }
    public Boolean getAutoCoverLetterMode() { return autoCoverLetterMode; }
    public Boolean getResearchedMode() { return researchedMode; }


    // --- Setters ---
    public void setInput(String input) { this.input = input; }
    public void setTone(String tone) { this.tone = tone; }
    public void setStyle(String style) { this.style = style; } // Fixed typo here (was style = style = style;)
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public void setWordLimit(Integer wordLimit) { this.wordLimit = wordLimit; }
    public void setEnableSuggestions(Boolean enableSuggestions) { this.enableSuggestions = enableSuggestions; }
    public void setAutoCoverLetterMode(Boolean autoCoverLetterMode) { this.autoCoverLetterMode = autoCoverLetterMode; }
    public void setResearchedMode(Boolean researchedMode) { this.researchedMode = researchedMode; }
}