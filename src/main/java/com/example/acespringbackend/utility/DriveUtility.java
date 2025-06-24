package com.example.acespringbackend.utility;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// Changed from jackson2.JacksonFactory to gson.GsonFactory
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.example.acespringbackend.service.DriveProperties;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.google.api.client.http.ByteArrayContent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * DriveUtility class provides core methods for interacting directly with the Google Drive API.
 * It encapsulates the low-level logic for initializing the Drive client, managing files and folders
 * on Google Drive, and performing common Drive-related calculations.
 *
 * This class now uses GsonFactory for JSON parsing, addressing the deprecation warning.
 */
@Component
public class DriveUtility {

    private static final Logger log = LoggerFactory.getLogger(DriveUtility.class);

    @Value("${google.drive.credentials.path}")
    private String credentialsPathString;

    private final ApplicationContext applicationContext;
    private final DriveProperties driveProperties; // Injected DriveProperties

    private Drive drive;

    /**
     * Constructor for DriveUtility, injecting ApplicationContext and DriveProperties.
     *
     * @param applicationContext The Spring application context to access resources.
     * @param driveProperties The bean containing Google Drive related configuration properties.
     */
    public DriveUtility(ApplicationContext applicationContext, DriveProperties driveProperties) {
        this.applicationContext = applicationContext;
        this.driveProperties = driveProperties;
    }

    /**
     * Lazily initializes and returns a Mono that emits the Google Drive client instance.
     * It ensures the Drive client is initialized only once and handles credentials loading.
     *
     * @return A Mono that emits the Google Drive client instance.
     */
    public Mono<Drive> getDriveInstance() {
        if (drive != null) {
            return Mono.just(drive);
        }

        return Mono.fromCallable(() -> {
            log.info("DriveUtility: Attempting to initialize Google Drive instance using credentials from: {}", credentialsPathString);
            try {
                Resource credentialsResource = applicationContext.getResource(credentialsPathString);

                if (!credentialsResource.exists()) {
                    log.error("DriveUtility: Google Drive credentials file DOES NOT EXIST at: {}", credentialsPathString);
                    throw new IOException("Google Drive credentials file not found: " + credentialsPathString);
                }
                if (!credentialsResource.isReadable()) {
                    log.error("DriveUtility: Google Drive credentials file is NOT READABLE: {}", credentialsPathString);
                    throw new IOException("Google Drive credentials file is not readable: " + credentialsPathString);
                }

                try (InputStream inputStream = credentialsResource.getInputStream()) {
                    GoogleCredentials credentials = ServiceAccountCredentials
                            .fromStream(inputStream)
                            .createScoped(Collections.singleton(DriveScopes.DRIVE));

                    drive = new Drive.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(),
                            GsonFactory.getDefaultInstance(), // Changed from JacksonFactory to GsonFactory
                            new HttpCredentialsAdapter(credentials)
                    ).setApplicationName("AceDriveApp").build();
                    log.info("DriveUtility: Google Drive instance initialized successfully.");
                    return drive;
                }
            } catch (Exception e) {
                log.error("DriveUtility: Failed to initialize Google Drive instance: {}", e.getMessage(), e);
                throw e;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves the ID of a subfolder within a given parent folder.
     *
     * @param driveInstance The Google Drive client instance.
     * @param parentFolderId The ID of the parent folder.
     * @param subfolderName The name of the subfolder to find.
     * @return The ID of the subfolder if found, otherwise null.
     * @throws IOException If an error occurs during the Drive API call.
     */
    public String getSubfolderId(Drive driveInstance, String parentFolderId, String subfolderName) throws IOException {
        String query = String.format(
                "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and '%s' in parents",
                subfolderName, parentFolderId
        );

        FileList subfolders = driveInstance.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id)")
                .execute();

        if (!subfolders.getFiles().isEmpty()) {
            return subfolders.getFiles().get(0).getId();
        }
        return null;
    }

    /**
     * Creates a new folder in Google Drive.
     *
     * @param driveInstance The Google Drive client instance.
     * @param folderName The name of the new folder.
     * @param parentFolderId The ID of the parent folder where the new folder will be created.
     * @return The created Google Drive File object representing the new folder.
     * @throws IOException If an error occurs during the Drive API call.
     */
    public File createDriveFolder(Drive driveInstance, String folderName, String parentFolderId) throws IOException {
        File metadata = new File();
        metadata.setName(folderName);
        metadata.setMimeType("application/vnd.google-apps.folder");
        metadata.setParents(Collections.singletonList(parentFolderId));

        return driveInstance.files().create(metadata)
                .setFields("id, name, mimeType")
                .execute();
    }


    /**
     * Uploads a file to Google Drive.
     *
     * @param driveInstance The Google Drive client instance.
     * @param fileName The name of the file to upload.
     * @param mimeType The MIME type of the file.
     * @param fileBytes The byte array content of the file.
     * @param parentFolderId The ID of the parent folder to upload the file to.
     * @return The created Google Drive File object representing the uploaded file.
     * @throws IOException If an error occurs during the Drive API call.
     */
    public File uploadFileToDrive(Drive driveInstance, String fileName, String mimeType, byte[] fileBytes, String parentFolderId) throws IOException {
        File fileMeta = new File();
        fileMeta.setName(fileName);
        fileMeta.setParents(Collections.singletonList(parentFolderId));
        fileMeta.setMimeType(mimeType);

        ByteArrayContent mediaContent = new ByteArrayContent(mimeType, fileBytes);

        return driveInstance.files().create(fileMeta, mediaContent)
                .setFields("id, name, mimeType, size, createdTime, webViewLink, thumbnailLink")
                .execute();
    }

    /**
     * Lists files from a specific Google Drive folder.
     *
     * @param driveInstance The Google Drive client instance.
     * @param folderId The ID of the folder to list files from.
     * @return A list of Google Drive File objects.
     * @throws IOException If an error occurs during the Drive API call.
     */
    public List<File> listFilesFromDrive(Drive driveInstance, String folderId) throws IOException {
        String query = String.format("'%s' in parents and trashed=false and mimeType!='application/vnd.google-apps.folder'", folderId);
        FileList files = driveInstance.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, size, createdTime, webViewLink, thumbnailLink)")
                .setSpaces("drive")
                .execute();
        return files.getFiles() != null ? files.getFiles() : Collections.emptyList();
    }

    /**
     * Retrieves metadata for a specific file from Google Drive.
     *
     * @param driveInstance The Google Drive client instance.
     * @param fileId The ID of the file to retrieve metadata for.
     * @param fields A comma-separated list of fields to retrieve (e.g., "parents,size").
     * @return The Google Drive File object containing the requested metadata.
     * @throws IOException If an error occurs during the Drive API call or file is not found.
     */
    public File getDriveFileMetadata(Drive driveInstance, String fileId, String fields) throws IOException {
        return driveInstance.files().get(fileId)
                .setFields(fields)
                .execute();
    }

    /**
     * Deletes a file from Google Drive.
     *
     * @param driveInstance The Google Drive client instance.
     * @param fileId The ID of the file to delete.
     * @throws IOException If an error occurs during the Drive API call.
     */
    public void deleteDriveFile(Drive driveInstance, String fileId) throws IOException {
        driveInstance.files().delete(fileId).execute();
    }

    /**
     * Converts a given number of bytes to megabytes.
     *
     * @param bytes The number of bytes to convert.
     * @return The equivalent value in megabytes.
     */
    public double bytesToMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    /**
     * Returns the maximum user space allowed in bytes, obtained from DriveProperties.
     * @return The maximum user space in bytes.
     */
    public long getMaxUserSpaceBytes() {
        return driveProperties.getMaxUserSpaceBytes();
    }

    /**
     * Returns the maximum individual file size allowed in bytes, obtained from DriveProperties.
     * @return The maximum individual file size in bytes.
     */
    public long getMaxIndividualFileSizeBytes() {
        return driveProperties.getMaxIndividualFileSizeBytes();
    }

    /**
     * Returns the master folder ID, obtained from DriveProperties.
     * @return The master folder ID.
     */
    public String getMasterFolderId() {
        return driveProperties.getMasterFolderId();
    }
}