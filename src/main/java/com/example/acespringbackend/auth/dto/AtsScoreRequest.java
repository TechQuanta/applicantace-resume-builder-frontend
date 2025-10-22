package com.example.acespringbackend.auth.dto;

/**
 * Represents the request payload for an ATS (Applicant Tracking System) scoring operation.
 * This DTO encapsulates all the necessary information required to initiate a resume
 * evaluation, including user details and job-specific criteria.
 */
public class AtsScoreRequest {

    /**
     * The email address of the user submitting the request. This can be used for
     * identification, logging, or personalized responses.
     */
    private String email;

    /**
     * A boolean flag indicating whether a "deep check" or more comprehensive analysis
     * should be performed on the resume. True for deep check, false for a standard analysis.
     */
    private boolean deepCheck;

    /**
     * The title of the job for which the resume is being evaluated (e.g., "Software Engineer").
     * This is crucial for relevance scoring against the job description.
     */
    private String jobTitle;

    /**
     * The detailed description of the job opening. This content is used by the ATS
     * to compare against the resume and determine compatibility and a match score.
     */
    private String jobDescription;

    /**
     * Default constructor for {@code AtsScoreRequest}.
     * Required for frameworks like Spring to deserialize JSON/form data into this object.
     */
    public AtsScoreRequest() {
        // Default constructor for deserialization
    }

    /**
     * Constructs a new {@code AtsScoreRequest} with all necessary parameters.
     * This constructor is used when all request details are available at the time of creation.
     *
     * @param email The email address of the user.
     * @param deepCheck A boolean indicating if a deep analysis is requested.
     * @param jobTitle The title of the job.
     * @param jobDescription The full job description.
     */
    public AtsScoreRequest(String email, boolean deepCheck, String jobTitle, String jobDescription) {
        this.email = email;
        this.deepCheck = deepCheck;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
    }

    // --- Getters ---

    /**
     * Retrieves the email address of the user.
     *
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Checks if a deep resume analysis is requested.
     *
     * @return True if a deep check is requested, false otherwise.
     */
    public boolean isDeepCheck() {
        return deepCheck;
    }

    /**
     * Retrieves the job title for which the resume is being evaluated.
     *
     * @return The job title.
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Retrieves the full job description.
     *
     * @return The job description.
     */
    public String getJobDescription() {
        return jobDescription;
    }

    // --- Setters ---

    /**
     * Sets the email address of the user.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets whether a deep resume analysis should be performed.
     *
     * @param deepCheck The boolean value to set for deep check.
     */
    public void setDeepCheck(boolean deepCheck) {
        this.deepCheck = deepCheck;
    }

    /**
     * Sets the job title for the evaluation.
     *
     * @param jobTitle The job title to set.
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * Sets the full job description.
     *
     * @param jobDescription The job description to set.
     */
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}