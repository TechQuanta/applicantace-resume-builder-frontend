package com.example.acespringbackend.auth.dto;

public class ParaphrasingRequest {
    private String input;
    private String tone;
    private String style;

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getWordLimit() {
        return wordLimit;
    }

    public void setWordLimit(String wordLimit) {
        this.wordLimit = wordLimit;
    }

    public boolean isEnableSuggestions() {
        return enableSuggestions;
    }

    public void setEnableSuggestions(boolean enableSuggestions) {
        this.enableSuggestions = enableSuggestions;
    }

    private String jobDescription;
    private String keywords;
    private String wordLimit;
    private boolean enableSuggestions;

    // Getters & Setters
}
