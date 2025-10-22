package com.example.acespringbackend.utility;

import com.example.acespringbackend.service.DriveProperties;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
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
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    private static final String APPLICATION_NAME = "AceCloudDrive";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.drive.credentials.path}")
    private String credentialsPathString;

    private final ApplicationContext applicationContext;
    private final DriveProperties driveProperties;

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
        if (drive == null) {
            synchronized (this) {
                if (drive == null) {
                    return Mono.fromCallable(() -> {
                        log.info("Attempting to initialize Google Drive client using credentials from: {}", credentialsPathString);
                        try {
                            Resource credentialsResource = applicationContext.getResource(credentialsPathString);

                            if (!credentialsResource.exists()) {
                                log.error("Initialization Failed: Google Drive credentials file not found at [{}]. Please ensure the path is correct and the file exists.", credentialsPathString);
                                throw new IOException("Google Drive credentials file is missing. Please verify the configuration path: " + credentialsPathString);
                            }
                            if (!credentialsResource.isReadable()) {
                                log.error("Initialization Failed: Google Drive credentials file at [{}] is not readable. Please check file permissions.", credentialsPathString);
                                throw new IOException("Google Drive credentials file is not accessible. Please check read permissions: " + credentialsPathString);
                            }

                            try (InputStream inputStream = credentialsResource.getInputStream()) {
                                GoogleCredentials credentials = ServiceAccountCredentials
                                        .fromStream(inputStream)
                                        .createScoped(Collections.singleton(DriveScopes.DRIVE));

                                drive = new Drive.Builder(
                                        GoogleNetHttpTransport.newTrustedTransport(),
                                        JSON_FACTORY,
                                        new HttpCredentialsAdapter(credentials)
                                ).setApplicationName(APPLICATION_NAME).build();
                                log.info("Google Drive client initialized successfully.");
                                return drive;
                            }
                        } catch (Exception e) {
                            log.error("Initialization Critical: Failed to establish connection with Google Drive. Cause: {}", e.getMessage(), e);
                            // Re-throwing the original exception to propagate the detailed cause
                            throw e;
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
            log.debug("Found subfolder '{}' with ID: {} within parent: {}", subfolderName, subfolders.getFiles().get(0).getId(), parentFolderId);
            return subfolders.getFiles().get(0).getId();
        }
        log.debug("Subfolder '{}' not found within parent folder ID: {}. It might not exist.", subfolderName, parentFolderId);
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
        log.info("Folder '{}' created successfully with ID: {} inside parent ID: {}.", folderName, createdFolder.getId(), parentFolderId);
        return createdFolder;
    }

    /**
     * Uploads a file to a specified Google Drive folder.
     *
     * @param driveInstance The authenticated Drive service instance.
     * @param fileName The name of the file to upload.
     * @param mimeType The MIME type of the file.
     * @param fileBytes The byte array of the file content.
     * @param parentFolderId The ID of the parent folder where the file should be uploaded.
     * @return The uploaded Google Drive File object.
     * @throws IOException if an error occurs during the Drive API call.
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
        log.info("File '{}' uploaded successfully with ID: {} to folder ID: {}.", fileName, uploadedFile.getId(), parentFolderId);
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
        log.info("Successfully listed {} files in folder ID '{}'.", resultFiles.size(), folderId);
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
            log.debug("Retrieved metadata for file ID '{}'. Requested fields: {}", fileId, fields);
            return file;
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                log.warn("File Not Found: Attempted to retrieve metadata for file ID '{}', but it does not exist on Google Drive (HTTP 404).", fileId);
                return null;
            }
            log.error("Failed to retrieve metadata for file ID '{}'. HTTP Status: {} - Message: {}", fileId, e.getStatusCode(), e.getStatusMessage());
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
        log.info("File with ID '{}' successfully deleted from Google Drive.", fileId);
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

        File copiedFile = driveInstance.files().copy(sourceFileId, copiedFileMetadata)
                .setFields("id,name,mimeType,size,webViewLink")
                .execute();
        log.info("Successfully copied file '{}' (source ID: {}) to folder '{}' with new ID: {}.",
                newFileName, sourceFileId, targetFolderId, copiedFile.getId());
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
        log.info("Granted '{}' permission to '{}' for file ID '{}'. Notification email sent: {}.", role, email, fileId, sendNotificationEmail);
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
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            driveInstance.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            log.info("Successfully downloaded content for file ID: {}. Size: {} bytes.", fileId, outputStream.size());
            return outputStream.toByteArray();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                log.warn("Download Failed: File with ID '{}' was not found on Google Drive (HTTP 404).", fileId);
                throw new IOException("File not found for download. Please verify the file ID: " + fileId);
            }
            log.error("Download Failed: An HTTP error occurred while downloading file ID '{}'. Status: {} - Message: {}", fileId, e.getStatusCode(), e.getStatusMessage());
            throw e;
        } catch (IOException e) {
            log.error("Download Failed: An unexpected I/O error occurred while downloading file ID '{}'. Details: {}", fileId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Renames a file on Google Drive.
     *
     * @param driveInstance The authenticated Drive service instance.
     * @param fileId The ID of the file to rename.
     * @param newFileName The new name for the file.
     * @return The updated Google Drive File object.
     * @throws IOException if an error occurs during the Drive API call.
     */
    public File renameDriveFile(Drive driveInstance, String fileId, String newFileName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(newFileName);
        File updatedFile = driveInstance.files().update(fileId, fileMetadata)
                .setFields("id,name,webViewLink,mimeType")
                .execute();
        log.info("File ID '{}' successfully renamed to '{}'.", fileId, newFileName);
        return updatedFile;
    }

    /**
     * Finds the permission ID for a given email on a specific file.
     * This is useful before updating a permission to determine if it exists.
     *
     * @param driveInstance The authenticated Drive service instance.
     * @param fileId The ID of the file to check permissions for.
     * @param email The email address to find the permission ID for.
     * @return The permission ID if found, otherwise null.
     * @throws IOException if an error occurs during the Drive API call.
     */
    public String getPermissionIdForEmail(Drive driveInstance, String fileId, String email) throws IOException {
        // Query permissions for the file, filtering by emailAddress
        PermissionList permissions = driveInstance.permissions().list(fileId)
                .setFields("permissions(id,emailAddress,type,role)")
                .execute();

        Optional<Permission> foundPermission = Optional.ofNullable(permissions.getPermissions())
                .orElse(Collections.emptyList()) // Handle null permissions list
                .stream()
                .filter(p -> "user".equals(p.getType()) && email.equals(p.getEmailAddress()))
                .findFirst();

        if (foundPermission.isPresent()) {
            log.debug("Found permission ID '{}' for email '{}' on file ID '{}'.", foundPermission.get().getId(), email, fileId);
            return foundPermission.get().getId();
        }
        log.debug("No permission found for email '{}' on file ID '{}'.", email, fileId);
        return null;
    }

    /**
     * Updates an existing permission for a specific file or folder.
     *
     * @param driveInstance The authenticated Google Drive service instance.
     * @param fileId The ID of the file or folder to update permissions for.
     * @param permissionId The ID of the permission to update (obtained via getPermissionIdForEmail).
     * @param newRole The new role to assign (e.g., "writer", "reader", "commenter").
     * @return The updated Permission object.
     * @throws IOException if an error occurs during the Drive API call.
     */
    public Permission updatePermission(Drive driveInstance, String fileId, String permissionId, String newRole) throws IOException {
        Permission updatedPermission = new Permission().setRole(newRole);
        Permission result = driveInstance.permissions().update(fileId, permissionId, updatedPermission)
                .execute();
        log.info("Permission ID '{}' on file ID '{}' successfully updated to role '{}'.", permissionId, fileId, newRole);
        return result;
    }

    /**
     * Deletes a permission from a specific file or folder.
     *
     * @param driveInstance The authenticated Google Drive service instance.
     * @param fileId The ID of the file or folder from which to delete the permission.
     * @param targetEmail The email address whose permission is to be deleted.
     * @param role The role that was previously granted (e.g., "writer", "reader").
     * @return true if permission was found and deleted, false otherwise.
     * @throws IOException if an error occurs during the Drive API call.
     */
    public boolean deletePermission(Drive driveInstance, String fileId, String targetEmail, String role) throws IOException {
        String permissionId = getPermissionIdForEmail(driveInstance, fileId, targetEmail);
        if (permissionId != null) {
            // It's good practice to also check the role if the permission ID might match multiple roles.
            // For simplicity, here we'll just delete by ID if found.
            // If you want to strictly match role, you'd need to fetch the permission object by ID and check its role.
            driveInstance.permissions().delete(fileId, permissionId).execute();
            log.info("Successfully deleted permission with ID '{}' for email '{}' (role: '{}') from file ID '{}'.", permissionId, targetEmail, role, fileId);
            return true;
        } else {
            log.warn("Permission Not Found: Unable to delete. No permission found for email '{}' with role '{}' on file ID '{}'.", targetEmail, role, fileId);
            return false;
        }
    }


    /**
     * Exports a Google Drive file to a specified MIME type. This is particularly useful
     * for Google Workspace documents (Docs, Sheets, Slides) which don't have direct content
     * to download but can be exported to standard formats (PDF, DOCX, XLSX, etc.).
     *
     * @param driveInstance The authenticated Google Drive service instance.
     * @param fileId The ID of the file to export.
     * @param exportMimeType The MIME type to which the file should be exported (e.g., "application/pdf",
     * "application/vnd.openxmlformats-officedocument.wordprocessingml.document").
     * @return A byte array containing the exported file's content.
     * @throws IOException If an error occurs during the Drive API call or if the file cannot be exported.
     */
    public byte[] exportGoogleDriveFile(Drive driveInstance, String fileId, String exportMimeType) throws IOException {
        log.info("Attempting to export file ID '{}' to MIME type '{}'.", fileId, exportMimeType);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            HttpRequest request = driveInstance.files().export(fileId, exportMimeType)
                    .buildHttpRequest();
            HttpResponse response = request.execute();

            if (response.isSuccessStatusCode()) {
                response.download(outputStream);
                log.info("Successfully exported file ID '{}' to MIME type '{}'. Size: {} bytes.",
                        fileId, exportMimeType, outputStream.size());
                return outputStream.toByteArray();
            } else {
                String errorMessage = String.format("Export Failed: Could not export file ID '%s' to '%s'. Google Drive API responded with status code: %d, Message: %s",
                        fileId, exportMimeType, response.getStatusCode(), response.getStatusMessage());
                log.error(errorMessage);
                throw new IOException(errorMessage);
            }
        } catch (HttpResponseException e) {
            log.error("Export Failed (HTTP Error): An HTTP error occurred while exporting file ID '{}'. Status: {} - Details: {}", fileId, e.getStatusCode(), e.getStatusMessage());
            throw e;
        } catch (IOException e) {
            log.error("Export Failed (I/O Error): An unexpected I/O error occurred during export of file ID '{}'. Details: {}", fileId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Moves a Google Drive folder to the trash. This is a "soft delete".
     *
     * @param driveInstance The authenticated Google Drive service instance.
     * @param fileId The ID of the folder (or file) to trash.
     * @return A Mono that completes when the operation is done.
     * @throws IOException if an error occurs during the Drive API call.
     */
    public Mono<Object> deleteDriveFolder(Drive driveInstance, String fileId) {
        return Mono.fromCallable(() -> {
            log.info("Attempting to trash Google Drive item with ID: {}. (Soft delete)", fileId);
            // Set 'trashed' to true to move the file/folder to trash
            File file = new File().setTrashed(true);
            driveInstance.files().update(fileId, file).execute();
            log.info("Google Drive item with ID '{}' successfully moved to trash.", fileId);
            return null; // Return null as this is a Void method
        }).subscribeOn(Schedulers.boundedElastic());
    }
}