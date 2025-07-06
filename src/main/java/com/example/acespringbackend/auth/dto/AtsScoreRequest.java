package com.example.acespringbackend.auth.dto;

public class AtsScoreRequest {
    private String email; // New field for user's email
    private boolean deepCheck;
    private String jobTitle;
    private String jobDescription;

    public AtsScoreRequest() {
        // Default constructor
    }

    public AtsScoreRequest(String email, boolean deepCheck, String jobTitle, String jobDescription) {
        this.email = email;
        this.deepCheck = deepCheck;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public boolean isDeepCheck() {
        return deepCheck;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setDeepCheck(boolean deepCheck) {
        this.deepCheck = deepCheck;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}