package com.example.acespringbackend.service;

/**
 * A simple POJO to hold Google Drive related configuration properties.
 * This class encapsulates the master folder ID and user storage quotas.
 */
public class DriveProperties {
    private final String masterFolderId;
    private final long maxUserSpaceBytes;
    private final long maxIndividualFileSizeBytes;

    /**
     * Constructs a new DriveProperties instance.
     *
     * @param masterFolderId The Google Drive ID of the main master folder for the application.
     * @param maxUserSpaceBytes The maximum allowed storage space for a user in bytes.
     * @param maxIndividualFileSizeBytes The maximum allowed size for a single file upload in bytes.
     */
    public DriveProperties(String masterFolderId, long maxUserSpaceBytes, long maxIndividualFileSizeBytes) {
        this.masterFolderId = masterFolderId;
        this.maxUserSpaceBytes = maxUserSpaceBytes;
        this.maxIndividualFileSizeBytes = maxIndividualFileSizeBytes;
    }

    /**
     * Returns the Google Drive ID of the main master folder.
     * @return The master folder ID.
     */
    public String getMasterFolderId() {
        return masterFolderId;
    }

    /**
     * Returns the maximum allowed storage space for a user in bytes.
     * @return The maximum user space in bytes.
     */
    public long getMaxUserSpaceBytes() {
        return maxUserSpaceBytes;
    }

    /**
     * Returns the maximum individual file size in bytes.
     * @return The maximum individual file size in bytes.
     */
    public long getMaxIndividualFileSizeBytes() {
        return maxIndividualFileSizeBytes;
    }
};