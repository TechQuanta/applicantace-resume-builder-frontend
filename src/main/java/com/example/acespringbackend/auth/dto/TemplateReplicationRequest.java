package com.example.acespringbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects; // Import for Objects.equals and Objects.hash

/**
 * Data Transfer Object (DTO) for requesting the replication of a Google Drive template file.
 * This DTO carries all necessary information from the frontend to the backend to
 * create a copy of a predefined template into a user's specified Google Drive folder.
 */
public class TemplateReplicationRequest {

    /**
     * The email address of the user who is initiating the template replication.
     * This is used for authentication and authorization to ensure the user
     * has the right to perform this action and to associate the new file with their account.
     */
    @NotBlank(message = "User email is required.")
    private String userEmail;

    /**
     * The name of the template as displayed on the frontend.
     * This helps identify the specific template to be copied.
     */
    @NotBlank(message = "Template name is required.")
    private String name; // Corresponds to template.name from frontend

    /**
     * The category to which the template belongs (e.g., "Resume", "Cover Letter", "Project Plan").
     * This is an optional field for categorization purposes.
     */
    private String category; // Corresponds to template.category

    /**
     * The unique Google Drive ID of the source template file that needs to be replicated.
     * This ID points to the original template file in Google Drive.
     */
    @NotBlank(message = "Drive ID is required.")
    private String drive_id; // Corresponds to template.id from frontend (renamed to drive_id for clarity with Google Drive APIs)

    /**
     * The URL of the image associated with the template, typically used for displaying
     * a preview or thumbnail of the template on the frontend.
     */
    private String image_url; // Corresponds to template.image

    /**
     * A brief description of the template.
     */
    private String description; // Corresponds to template.description

    /**
     * A field for highlighting key features or "spotlights" of the template,
     * often used for marketing or quick information display.
     */
    private String spotlight; // Corresponds to template.spotlights

    /**
     * The source or creator of the template (e.g., "Google", "OurApp", "Community").
     */
    private String provider; // Corresponds to template.provider

    /**
     * The desired name for the new file that will be created from the template.
     * This is the name the user wants for their copy of the template.
     */
    @NotBlank(message = "New file name is required.")
    private String newFileName;

    /**
     * The Google Drive folder ID where the new replicated file should be placed.
     * This ensures the copied template lands in the user's intended destination folder.
     */
    @NotBlank(message = "Target Drive folder ID is required.")
    private String targetDriveFolderId;

    // --- Constructors ---

    /**
     * Default no-argument constructor for {@code TemplateReplicationRequest}.
     * This constructor is required by many frameworks (e.g., Spring, Jackson) for
     * object deserialization when mapping JSON or form data to this DTO.
     */
    public TemplateReplicationRequest() {
    }

    /**
     * Parameterized constructor for convenience, allowing all fields to be initialized
     * upon creation of a {@code TemplateReplicationRequest} object.
     *
     * @param userEmail           The email of the user requesting the replication.
     * @param name                The name of the template.
     * @param category            The category of the template.
     * @param drive_id            The Google Drive ID of the source template file.
     * @param image_url           The URL of the template's preview image.
     * @param description         A description of the template.
     * @param spotlight           Spotlight details for the template.
     * @param provider            The provider/source of the template.
     * @param newFileName         The desired name for the new replicated file.
     * @param targetDriveFolderId The Google Drive folder ID where the new file should be saved.
     */
    public TemplateReplicationRequest(
        String userEmail,
        String name,
        String category,
        String drive_id,
        String image_url,
        String description,
        String spotlight,
        String provider,
        String newFileName,
        String targetDriveFolderId
    ) {
        this.userEmail = userEmail;
        this.name = name;
        this.category = category;
        this.drive_id = drive_id;
        this.image_url = image_url;
        this.description = description;
        this.spotlight = spotlight;
        this.provider = provider;
        this.newFileName = newFileName;
        this.targetDriveFolderId = targetDriveFolderId;
    }

    // --- Getters and Setters ---

    /**
     * Retrieves the email address of the user initiating the request.
     *
     * @return The user's email as a {@link String}.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the email address of the user initiating the request.
     *
     * @param userEmail The user's email to set.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Retrieves the name of the template.
     *
     * @return The template name as a {@link String}.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the template.
     *
     * @param name The template name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the category of the template.
     *
     * @return The template category as a {@link String}, or {@code null} if not set.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the template.
     *
     * @param category The template category to set.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Retrieves the Google Drive ID of the source template file.
     *
     * @return The Google Drive ID as a {@link String}.
     */
    public String getDrive_id() {
        return drive_id;
    }

    /**
     * Sets the Google Drive ID of the source template file.
     *
     * @param drive_id The Google Drive ID to set.
     */
    public void setDrive_id(String drive_id) {
        this.drive_id = drive_id;
    }

    /**
     * Retrieves the URL of the image associated with the template.
     *
     * @return The image URL as a {@link String}, or {@code null} if not set.
     */
    public String getImage_url() {
        return image_url;
    }

    /**
     * Sets the URL of the image associated with the template.
     *
     * @param image_url The image URL to set.
     */
    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    /**
     * Retrieves the description of the template.
     *
     * @return The template description as a {@link String}, or {@code null} if not set.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the template.
     *
     * @param description The template description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieves the spotlight information for the template.
     *
     * @return The spotlight string as a {@link String}, or {@code null} if not set.
     */
    public String getSpotlight() {
        return spotlight;
    }

    /**
     * Sets the spotlight information for the template.
     *
     * @param spotlight The spotlight string to set.
     */
    public void setSpotlight(String spotlight) {
        this.spotlight = spotlight;
    }

    /**
     * Retrieves the provider of the template.
     *
     * @return The template provider as a {@link String}, or {@code null} if not set.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the provider of the template.
     *
     * @param provider The template provider to set.
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Retrieves the desired name for the new replicated file.
     *
     * @return The new file name as a {@link String}.
     */
    public String getNewFileName() {
        return newFileName;
    }

    /**
     * Sets the desired name for the new replicated file.
     *
     * @param newFileName The new file name to set.
     */
    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    /**
     * Retrieves the target Google Drive folder ID where the new file should be saved.
     *
     * @return The target folder ID as a {@link String}.
     */
    public String getTargetDriveFolderId() {
        return targetDriveFolderId;
    }

    /**
     * Sets the target Google Drive folder ID where the new file should be saved.
     *
     * @param targetDriveFolderId The target folder ID to set.
     */
    public void setTargetDriveFolderId(String targetDriveFolderId) {
        this.targetDriveFolderId = targetDriveFolderId;
    }

    // --- Object Overrides (toString, equals, hashCode) ---

    /**
     * Returns a string representation of the {@code TemplateReplicationRequest} object.
     * Useful for logging and debugging purposes.
     *
     * @return A string containing the values of all fields in this DTO.
     */
    @Override
    public String toString() {
        return "TemplateReplicationRequest{" +
               "userEmail='" + userEmail + '\'' +
               ", name='" + name + '\'' +
               ", category='" + category + '\'' +
               ", drive_id='" + drive_id + '\'' +
               ", image_url='" + image_url + '\'' +
               ", description='" + description + '\'' +
               ", spotlight='" + spotlight + '\'' +
               ", provider='" + provider + '\'' +
               ", newFileName='" + newFileName + '\'' +
               ", targetDriveFolderId='" + targetDriveFolderId + '\'' +
               '}';
    }

    /**
     * Compares this {@code TemplateReplicationRequest} object with another object for equality.
     * Two {@code TemplateReplicationRequest} objects are considered equal if all their
     * fields (userEmail, name, category, drive_id, image_url, description, spotlight, provider,
     * newFileName, targetDriveFolderId) are equal.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateReplicationRequest that = (TemplateReplicationRequest) o;
        return Objects.equals(userEmail, that.userEmail) &&
               Objects.equals(name, that.name) &&
               Objects.equals(category, that.category) &&
               Objects.equals(drive_id, that.drive_id) &&
               Objects.equals(image_url, that.image_url) &&
               Objects.equals(description, that.description) &&
               Objects.equals(spotlight, that.spotlight) &&
               Objects.equals(provider, that.provider) &&
               Objects.equals(newFileName, that.newFileName) &&
               Objects.equals(targetDriveFolderId, that.targetDriveFolderId);
    }

    /**
     * Generates a hash code for this {@code TemplateReplicationRequest} object.
     * The hash code is based on all fields to ensure consistency with the {@code equals()} method.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(userEmail, name, category, drive_id, image_url, description, spotlight, provider, newFileName, targetDriveFolderId);
    }
}