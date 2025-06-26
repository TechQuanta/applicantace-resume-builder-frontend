//package com.example.acespringbackend.utility;
//
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//// Changed from jackson2.JacksonFactory to gson.GsonFactory
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.File;
//import com.example.acespringbackend.service.DriveProperties;
//import com.google.api.services.drive.model.FileList;
//import com.google.auth.http.HttpCredentialsAdapter;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.auth.oauth2.ServiceAccountCredentials;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Component;
//
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//
//import com.google.api.client.http.ByteArrayContent;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Collections;
//import java.util.List;
//
///**
// * DriveUtility class provides core methods for interacting directly with the Google Drive API.
// * It encapsulates the low-level logic for initializing the Drive client, managing files and folders
// * on Google Drive, and performing common Drive-related calculations.
// *
// * This class now uses GsonFactory for JSON parsing, addressing the deprecation warning.
// */
//@Component
//public class DriveUtility {
//
//    private static final Logger log = LoggerFactory.getLogger(DriveUtility.class);
//
//    @Value("${google.drive.credentials.path}")
//    private String credentialsPathString;
//
//    private final ApplicationContext applicationContext;
//    private final DriveProperties driveProperties; // Injected DriveProperties
//
//    private Drive drive;
//
//    /**
//     * Constructor for DriveUtility, injecting ApplicationContext and DriveProperties.
//     *
//     * @param applicationContext The Spring application context to access resources.
//     * @param driveProperties The bean containing Google Drive related configuration properties.
//     */
//    public DriveUtility(ApplicationContext applicationContext, DriveProperties driveProperties) {
//        this.applicationContext = applicationContext;
//        this.driveProperties = driveProperties;
//    }
//
//    /**
//     * Lazily initializes and returns a Mono that emits the Google Drive client instance.
//     * It ensures the Drive client is initialized only once and handles credentials loading.
//     *
//     * @return A Mono that emits the Google Drive client instance.
//     */
//    public Mono<Drive> getDriveInstance() {
//        if (drive != null) {
//            return Mono.just(drive);
//        }
//
//        return Mono.fromCallable(() -> {
//            log.info("DriveUtility: Attempting to initialize Google Drive instance using credentials from: {}", credentialsPathString);
//            try {
//                Resource credentialsResource = applicationContext.getResource(credentialsPathString);
//
//                if (!credentialsResource.exists()) {
//                    log.error("DriveUtility: Google Drive credentials file DOES NOT EXIST at: {}", credentialsPathString);
//                    throw new IOException("Google Drive credentials file not found: " + credentialsPathString);
//                }
//                if (!credentialsResource.isReadable()) {
//                    log.error("DriveUtility: Google Drive credentials file is NOT READABLE: {}", credentialsPathString);
//                    throw new IOException("Google Drive credentials file is not readable: " + credentialsPathString);
//                }
//
//                try (InputStream inputStream = credentialsResource.getInputStream()) {
//                    GoogleCredentials credentials = ServiceAccountCredentials
//                            .fromStream(inputStream)
//                            .createScoped(Collections.singleton(DriveScopes.DRIVE));
//
//                    drive = new Drive.Builder(
//                            GoogleNetHttpTransport.newTrustedTransport(),
//                            GsonFactory.getDefaultInstance(), // Changed from JacksonFactory to GsonFactory
//                            new HttpCredentialsAdapter(credentials)
//                    ).setApplicationName("AceDriveApp").build();
//                    log.info("DriveUtility: Google Drive instance initialized successfully.");
//                    return drive;
//                }
//            } catch (Exception e) {
//                log.error("DriveUtility: Failed to initialize Google Drive instance: {}", e.getMessage(), e);
//                throw e;
//            }
//        }).subscribeOn(Schedulers.boundedElastic());
//    }
//
//    /**
//     * Retrieves the ID of a subfolder within a given parent folder.
//     *
//     * @param driveInstance The Google Drive client instance.
//     * @param parentFolderId The ID of the parent folder.
//     * @param subfolderName The name of the subfolder to find.
//     * @return The ID of the subfolder if found, otherwise null.
//     * @throws IOException If an error occurs during the Drive API call.
//     */
//    public String getSubfolderId(Drive driveInstance, String parentFolderId, String subfolderName) throws IOException {
//        String query = String.format(
//                "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and '%s' in parents",
//                subfolderName, parentFolderId
//        );
//
//        FileList subfolders = driveInstance.files().list()
//                .setQ(query)
//                .setSpaces("drive")
//                .setFields("files(id)")
//                .execute();
//
//        if (!subfolders.getFiles().isEmpty()) {
//            return subfolders.getFiles().get(0).getId();
//        }
//        return null;
//    }
//
//    /**
//     * Creates a new folder in Google Drive.
//     *
//     * @param driveInstance The Google Drive client instance.
//     * @param folderName The name of the new folder.
//     * @param parentFolderId The ID of the parent folder where the new folder will be created.
//     * @return The created Google Drive File object representing the new folder.
//     * @throws IOException If an error occurs during the Drive API call.
//     */
//    public File createDriveFolder(Drive driveInstance, String folderName, String parentFolderId) throws IOException {
//        File metadata = new File();
//        metadata.setName(folderName);
//        metadata.setMimeType("application/vnd.google-apps.folder");
//        metadata.setParents(Collections.singletonList(parentFolderId));
//
//        return driveInstance.files().create(metadata)
//                .setFields("id, name, mimeType")
//                .execute();
//    }
//
//
//    /**
//     * Uploads a file to Google Drive.
//     *
//     * @param driveInstance The Google Drive client instance.
//     * @param fileName The name of the file to upload.
//     * @param mimeType The MIME type of the file.
//     * @param fileBytes The byte array content of the file.
//     * @param parentFolderId The ID of the parent folder to upload the file to.
//     * @return The created Google Drive File object representing the uploaded file.
//     * @throws IOException If an error occurs during the Drive API call.
//     */
//    public File uploadFileToDrive(Drive driveInstance, String fileName, String mimeType, byte[] fileBytes, String parentFolderId) throws IOException {
//        File fileMeta = new File();
//        fileMeta.setName(fileName);
//        fileMeta.setParents(Collections.singletonList(parentFolderId));
//        fileMeta.setMimeType(mimeType);
//
//        ByteArrayContent mediaContent = new ByteArrayContent(mimeType, fileBytes);
//
//        return driveInstance.files().create(fileMeta, mediaContent)
//                .setFields("id, name, mimeType, size, createdTime, webViewLink, thumbnailLink")
//                .execute();
//    }
//
//    /**
//     * Lists files from a specific Google Drive folder.
//     *
//     * @param driveInstance The Google Drive client instance.
//     * @param folderId The ID of the folder to list files from.
//     * @return A list of Google Drive File objects.
//     * @throws IOException If an error occurs during the Drive API call.
//     */
//    public List<File> listFilesFromDrive(Drive driveInstance, String folderId) throws IOException {
//        String query = String.format("'%s' in parents and trashed=false and mimeType!='application/vnd.google-apps.folder'", folderId);
//        FileList files = driveInstance.files().list()
//                .setQ(query)
//                .setFields("files(id, name, mimeType, size, createdTime, webViewLink, thumbnailLink)")
//                .setSpaces("drive")
//                .execute();
//        return files.getFiles() != null ? files.getFiles() : Collections.emptyList();
//    }
//
//    /**
//     * Retrieves metadata for a specific file from Google Drive.
//     *
//     * @param driveInstance The Google Drive client instance.
//     * @param fileId The ID of the file to retrieve metadata for.
//     * @param fields A comma-separated list of fields to retrieve (e.g., "parents,size").
//     * @return The Google Drive File object containing the requested metadata.
//     * @throws IOException If an error occurs during the Drive API call or file is not found.
//     */
//    public File getDriveFileMetadata(Drive driveInstance, String fileId, String fields) throws IOException {
//        return driveInstance.files().get(fileId)
//                .setFields(fields)
//                .execute();
//    }
//
//    /**
//     * Deletes a file from Google Drive.
//     *
//     * @param driveInstance The Google Drive client instance.
//     * @param fileId The ID of the file to delete.
//     * @throws IOException If an error occurs during the Drive API call.
//     */
//    public void deleteDriveFile(Drive driveInstance, String fileId) throws IOException {
//        driveInstance.files().delete(fileId).execute();
//    }
//
//    /**
//     * Converts a given number of bytes to megabytes.
//     *
//     * @param bytes The number of bytes to convert.
//     * @return The equivalent value in megabytes.
//     */
//    public double bytesToMegabytes(long bytes) {
//        return bytes / (1024.0 * 1024.0);
//    }
//
//    /**
//     * Returns the maximum user space allowed in bytes, obtained from DriveProperties.
//     * @return The maximum user space in bytes.
//     */
//    public long getMaxUserSpaceBytes() {
//        return driveProperties.getMaxUserSpaceBytes();
//    }
//
//    /**
//     * Returns the maximum individual file size allowed in bytes, obtained from DriveProperties.
//     * @return The maximum individual file size in bytes.
//     */
//    public long getMaxIndividualFileSizeBytes() {
//        return driveProperties.getMaxIndividualFileSizeBytes();
//    }
//
//    /**
//     * Returns the master folder ID, obtained from DriveProperties.
//     * @return The master folder ID.
//     */
//    public String getMasterFolderId() {
//        return driveProperties.getMasterFolderId();
//    }
//}
package com.example.acespringbackend.utility;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// Changed from jackson2.JacksonFactory to gson.GsonFactory
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.example.acespringbackend.service.DriveProperties; // Ensure this import is correct based on your project structure
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission; // Import for Permissions
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
import com.google.api.client.http.HttpResponseException; // For specific error handling

import java.io.ByteArrayOutputStream;
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

    private volatile Drive drive; // Use volatile for thread-safe lazy initialization

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
        // Double-checked locking for thread-safe lazy initialization
        if (drive == null) {
            synchronized (this) {
                if (drive == null) {
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
                                        GsonFactory.getDefaultInstance(),
                                        new HttpCredentialsAdapter(credentials)
                                ).setApplicationName("AceDriveApp").build();
                                log.info("DriveUtility: Google Drive instance initialized successfully.");
                                return drive;
                            }
                        } catch (Exception e) {
                            log.error("DriveUtility: Failed to initialize Google Drive instance: {}", e.getMessage(), e);
                            throw e; // Re-throw for Mono.error
                        }
                    }).subscribeOn(Schedulers.boundedElastic());
                }
            }
        }
        return Mono.just(drive);
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
            log.debug("DriveUtility: Found subfolder '{}' with ID: {} within parent: {}", subfolderName, subfolders.getFiles().get(0).getId(), parentFolderId);
            return subfolders.getFiles().get(0).getId();
        }
        log.debug("DriveUtility: Subfolder '{}' not found within parent: {}", subfolderName, parentFolderId);
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

        File createdFolder = driveInstance.files().create(metadata)
                .setFields("id, name, mimeType")
                .execute();
        log.info("DriveUtility: Folder '{}' created with ID: {}", folderName, createdFolder.getId());
        return createdFolder;
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

        File uploadedFile = driveInstance.files().create(fileMeta, mediaContent)
                .setFields("id, name, mimeType, size, createdTime, webViewLink, thumbnailLink")
                .execute();
        log.info("DriveUtility: File '{}' uploaded with ID: {}", fileName, uploadedFile.getId());
        return uploadedFile;
    }

    /**
     * Lists files from a specific Google Drive folder, excluding folders themselves.
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
        List<File> resultFiles = files.getFiles();
        if (resultFiles == null) {
            resultFiles = Collections.emptyList();
        }
        log.info("DriveUtility: Listed {} files in folder ID '{}'.", resultFiles.size(), folderId);
        return resultFiles;
    }

    /**
     * Retrieves metadata for a specific file from Google Drive.
     *
     * @param driveInstance The Google Drive client instance.
     * @param fileId The ID of the file to retrieve metadata for.
     * @param fields A comma-separated list of fields to retrieve (e.g., "parents,size").
     * @return The Google Drive File object containing the requested metadata, or null if not found.
     * @throws IOException If an error occurs during the Drive API call other than 404.
     */
    public File getDriveFileMetadata(Drive driveInstance, String fileId, String fields) throws IOException {
        try {
            File file = driveInstance.files().get(fileId)
                    .setFields(fields)
                    .execute();
            log.debug("DriveUtility: Retrieved metadata for file ID '{}'.", fileId);
            return file;
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                log.warn("DriveUtility: File with ID '{}' not found on Google Drive (404 error).", fileId);
                return null;
            }
            throw e;
        }
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
        log.info("DriveUtility: File with ID '{}' deleted from Drive.", fileId);
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

    /**
     * Copies a Google Drive file to a specified target folder, optionally renaming it.
     *
     * @param driveInstance The authenticated Google Drive service instance.
     * @param sourceFileId The ID of the file to copy.
     * @param newFileName The new name for the copied file.
     * @param targetFolderId The ID of the target folder where the copy should be placed.
     * @return The Google Drive File object of the newly copied file.
     * @throws IOException if an error occurs during the Drive API call.
     */
    public File copyFileToDrive(Drive driveInstance, String sourceFileId, String newFileName, String targetFolderId) throws IOException {
        File copiedFileMetadata = new File();
        copiedFileMetadata.setName(newFileName);
        copiedFileMetadata.setParents(Collections.singletonList(targetFolderId));

        // Perform the copy operation
        File copiedFile = driveInstance.files().copy(sourceFileId, copiedFileMetadata)
                .setFields("id,name,mimeType,size,webViewLink") // Request relevant fields for the response
                .execute();
        log.info("DriveUtility: Copied file '{}' (ID: {}) to folder '{}' with new ID: {}",
                newFileName, sourceFileId, targetFolderId, copiedFile.getId()); // Corrected log order
        return copiedFile;
    }

    /**
     * Creates a permission for a specific file or folder.
     *
     * @param driveInstance The authenticated Google Drive service instance.
     * @param fileId The ID of the file or folder to set permissions for.
     * @param email The email address of the user to grant permission to.
     * @param role The role to grant (e.g., "writer", "reader", "commenter").
     * @param sendNotificationEmail Whether to send a notification email to the user.
     * @return The created Permission object.
     * @throws IOException if an error occurs during the Drive API call.
     */
    public Permission createPermission(
            Drive driveInstance,
            String fileId,
            String email,
            String role,
            boolean sendNotificationEmail) throws IOException {

        Permission newPermission = new Permission()
                .setEmailAddress(email)
                .setType("user")
                .setRole(role);

        Permission createdPermission = driveInstance.permissions().create(fileId, newPermission)
                .setSendNotificationEmail(sendNotificationEmail)
                .execute();
        log.info("DriveUtility: Granted '{}' permission to '{}' for file ID '{}'.", role, email, fileId);
        return createdPermission;
    }

    /**
     * Downloads the content of a specific file from Google Drive as a byte array.
     *
     * @param driveInstance The authenticated Google Drive service instance.
     * @param fileId The ID of the file to download.
     * @return A byte array containing the file's content.
     * @throws IOException if an error occurs during the Drive API call, or if the file content cannot be read.
     */
    public byte[] downloadFileContent(Drive driveInstance, String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            driveInstance.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            log.info("DriveUtility: Downloaded content for file ID: {}", fileId);
            return outputStream.toByteArray();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                log.warn("DriveUtility: File with ID '{}' not found for download (404 error).", fileId);
                throw new IOException("File not found on Google Drive: " + fileId);
            }
            throw e; // Re-throw other HTTP exceptions
        } catch (IOException e) {
            log.error("DriveUtility: Failed to download content for file ID '{}': {}", fileId, e.getMessage(), e);
            throw e;
        }
    }
}
