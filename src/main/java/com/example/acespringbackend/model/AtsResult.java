// src/main/java/com/example/acespringbackend/model/AtsResult.java
package com.example.acespringbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "atsResults")
public class AtsResult {

    @Id
    private String id;
    private String userId; // Can still be stored, but userEmail will be primary lookup
    private String userEmail; // Key identifier for lookup
    private String fileName;
    private String jobTitle;
    private String jobDescription;
    private String extractedResumeContent; // Raw text from PDF
    private String fullAtsResponse; // Full Markdown from Gemini
    private int atsScore; // Stored as int in DB
    private LocalDateTime checkTimestamp;

    public AtsResult() {
    }

    public AtsResult(String userId, String userEmail, String fileName, String jobTitle, String jobDescription,
                     String extractedResumeContent, String fullAtsResponse, int atsScore, LocalDateTime checkTimestamp) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.fileName = fileName;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.extractedResumeContent = extractedResumeContent;
        this.fullAtsResponse = fullAtsResponse;
        this.atsScore = atsScore;
        this.checkTimestamp = checkTimestamp;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getFileName() { return fileName; }
    public String getJobTitle() { return jobTitle; }
    public String getJobDescription() { return jobDescription; }
    public String getExtractedResumeContent() { return extractedResumeContent; }
    public String getFullAtsResponse() { return fullAtsResponse; }
    public int getAtsScore() { return atsScore; }
    public LocalDateTime getCheckTimestamp() { return checkTimestamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    public void setExtractedResumeContent(String extractedResumeContent) { this.extractedResumeContent = extractedResumeContent; }
    public void setFullAtsResponse(String fullAtsResponse) { this.fullAtsResponse = fullAtsResponse; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }
    public void setCheckTimestamp(LocalDateTime checkTimestamp) { this.checkTimestamp = checkTimestamp; }

    @Override
    public String toString() {
        return "AtsResult{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", fileName='" + fileName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", atsScore=" + atsScore +
                ", checkTimestamp=" + checkTimestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtsResult atsResult = (AtsResult) o;
        return Objects.equals(userEmail, atsResult.userEmail) &&
                Objects.equals(fileName, atsResult.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userEmail, fileName);
    }
}