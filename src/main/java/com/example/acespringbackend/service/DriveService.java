package com.example.acespringbackend.service;

import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.auth.dto.FileUploadResponse;
import com.example.acespringbackend.auth.dto.FileViewResponse;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.http.ByteArrayContent;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class DriveService {

    private static final Logger log = LoggerFactory.getLogger(DriveService.class);

    @Value("${google.drive.credentials.path}")
    private String credentialsPathString;

    @Value("${google.drive.master.folder.id}")
    private String masterFolderId;

    @Value("${user.drive.quota.mb:10}")
    private double userDriveQuotaMb;

    @Value("${user.max.individual.file.size.mb:3}")
    private double maxIndividualFileSizeMb;

    private long MAX_USER_SPACE_BYTES;
    private long MAX_INDIVIDUAL_FILE_SIZE_BYTES;

    private final ApplicationContext applicationContext;
    private final UserRepository userRepository;

    private Drive drive;

    public DriveService(ApplicationContext applicationContext, UserRepository userRepository) {
        this.applicationContext = applicationContext;
        this.userRepository = userRepository;
    }

    @Value("${user.drive.quota.mb:10}")
    public void setQuotas(@Value("${user.drive.quota.mb:10}") double userDriveQuotaMbParam,
                          @Value("${user.max.individual.file.size.mb:3}") double maxIndividualFileSizeMbParam) {
        this.userDriveQuotaMb = userDriveQuotaMbParam;
        this.MAX_USER_SPACE_BYTES = (long) (userDriveQuotaMbParam * 1024 * 1024);
        log.info("User overall Drive Quota set to {} MB ({} bytes).", userDriveQuotaMbParam, MAX_USER_SPACE_BYTES);

        this.maxIndividualFileSizeMb = maxIndividualFileSizeMbParam;
        this.MAX_INDIVIDUAL_FILE_SIZE_BYTES = (long) (maxIndividualFileSizeMbParam * 1024 * 1024);
        log.info("Max individual file size limit set to {} MB ({} bytes).", maxIndividualFileSizeMbParam, MAX_INDIVIDUAL_FILE_SIZE_BYTES);
    }


    private Mono<Drive> getDriveInstance() {
        if (drive != null) return Mono.just(drive);

        return Mono.fromCallable(() -> {
            log.info("Attempting to initialize Google Drive instance using credentials from: {}", credentialsPathString);
            try {
                Resource credentialsResource = applicationContext.getResource(credentialsPathString);

                if (!credentialsResource.exists()) {
                    log.error("Google Drive credentials file DOES NOT EXIST at: {}", credentialsPathString);
                    throw new IOException("Google Drive credentials file not found: " + credentialsPathString);
                }
                if (!credentialsResource.isReadable()) {
                    log.error("Google Drive credentials file is NOT READABLE: {}", credentialsPathString);
                    throw new IOException("Google Drive credentials file is not readable: " + credentialsPathString);
                }

                try (InputStream inputStream = credentialsResource.getInputStream()) {
                    GoogleCredentials credentials = ServiceAccountCredentials
                            .fromStream(inputStream)
                            .createScoped(Collections.singleton(DriveScopes.DRIVE));

                    drive = new Drive.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(),
                            JacksonFactory.getDefaultInstance(),
                            new HttpCredentialsAdapter(credentials)
                    ).setApplicationName("AceDriveApp").build();
                    log.info("Google Drive instance initialized successfully.");
                    return drive;
                }
            } catch (Exception e) {
                log.error("Failed to initialize Google Drive instance: {}", e.getMessage(), e);
                throw e;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String getSubfolderId(Drive driveInstance, String parentFolderId, String subfolderName) throws IOException {
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

    private long getUserFolderCurrentSizeFromDrive(Drive driveInstance, String folderId) throws IOException {
        if (folderId == null) {
            return 0;
        }

        long currentSize = 0;
        String pageToken = null;
        do {
            FileList files = driveInstance.files().list()
                    .setQ(String.format("'%s' in parents and trashed=false and mimeType!='application/vnd.google-apps.folder'", folderId))
                    .setFields("nextPageToken, files(size)")
                    .setSpaces("drive")
                    .setPageToken(pageToken)
                    .execute();

            if (files.getFiles() != null) {
                for (File file : files.getFiles()) {
                    if (file.getSize() != null) {
                        currentSize += file.getSize();
                    }
                }
            }
            pageToken = files.getNextPageToken();
        } while (pageToken != null);

        return currentSize;
    }

    private double bytesToMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    public Mono<String> createUserFolderIfNotExists(String email) {
        return getDriveInstance().flatMap(driveInstance -> Mono.fromCallable(() -> {
            log.info("Attempting to create/verify Drive folder for user: {}", email);
            try {
                String query = String.format(
                        "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and '%s' in parents",
                        email, masterFolderId
                );

                FileList existing = driveInstance.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id)")
                        .execute();

                String userFolderId;

                if (!existing.getFiles().isEmpty()) {
                    userFolderId = existing.getFiles().get(0).getId();
                    log.info("User folder already exists for {}: {}", email, userFolderId);
                } else {
                    File metadata = new File();
                    metadata.setName(email);
                    metadata.setMimeType("application/vnd.google-apps.folder");
                    metadata.setParents(List.of(masterFolderId));

                    File folder = driveInstance.files().create(metadata)
                            .setFields("id")
                            .execute();

                    userFolderId = folder.getId();
                    log.info("Created new user folder for {}: {}", email, userFolderId);
                }

                List<String> subfolders = List.of("docs", "images", "tasks");

                for (String sub : subfolders) {
                    String subFolderId = getSubfolderId(driveInstance, userFolderId, sub);

                    if (subFolderId == null) {
                        File subMeta = new File();
                        subMeta.setName(sub);
                        subMeta.setMimeType("application/vnd.google-apps.folder");
                        subMeta.setParents(List.of(userFolderId));
                        driveInstance.files().create(subMeta).setFields("id").execute();
                        log.info("Created subfolder '{}' for user: {}", sub, email);
                    } else {
                        log.info("Subfolder '{}' already exists for user: {}", sub, email);
                    }
                }
                log.info("All necessary folders for user {} are verified/created.", email);
                return userFolderId;
            } catch (Exception e) {
                log.error("Failed to create/verify Drive folder for user {}: {}", email, e.getMessage(), e);
                throw e;
            }
        }).subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<FileUploadResponse> uploadFile(String email, MultipartFile file) {
        // 1. Fetch user reactively
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    // Handle case where user is not found
                    if (user == null) {
                        log.error("User with email {} not found for file upload.", email);
                        return Mono.just(FileUploadResponse.builder()
                                .success(false)
                                .message("User not found for file upload.")
                                .build());
                    }

                    // 2. Validate file size and user quota early
                    long incomingFileSize = file.getSize();
                    String originalFileName = file.getOriginalFilename();

                    if (incomingFileSize <= 0) {
                        return Mono.just(FileUploadResponse.builder()
                                .success(false)
                                .message("Cannot upload empty file or file with size 0.")
                                .currentStorageUsageMb(bytesToMegabytes(user.getCurrentDriveUsageBytes()))
                                .maxStorageQuotaMb(bytesToMegabytes(MAX_USER_SPACE_BYTES))
                                .build());
                    }

                    if (incomingFileSize > MAX_INDIVIDUAL_FILE_SIZE_BYTES) {
                        log.warn("File '{}' ({} bytes) exceeds individual file limit ({} bytes) for user {}.",
                                originalFileName, incomingFileSize, MAX_INDIVIDUAL_FILE_SIZE_BYTES, email);
                        return Mono.just(FileUploadResponse.builder()
                                .success(false)
                                .message(String.format("File '%s' size (%.2fMB) exceeds individual upload limit of %.2fMB.",
                                        originalFileName, bytesToMegabytes(incomingFileSize), bytesToMegabytes(MAX_INDIVIDUAL_FILE_SIZE_BYTES)))
                                .currentStorageUsageMb(bytesToMegabytes(user.getCurrentDriveUsageBytes()))
                                .maxStorageQuotaMb(bytesToMegabytes(MAX_USER_SPACE_BYTES))
                                .build());
                    }

                    long currentUsage = user.getCurrentDriveUsageBytes();
                    log.info("User {} current Drive usage (from DB): {} bytes ({:.2f} MB). Incoming file size: {} bytes ({:.2f} MB).",
                            email, currentUsage, bytesToMegabytes(currentUsage), incomingFileSize, bytesToMegabytes(incomingFileSize));

                    if (currentUsage + incomingFileSize > MAX_USER_SPACE_BYTES) {
                        long remainingSpace = MAX_USER_SPACE_BYTES - currentUsage;
                        log.warn("User {} will exceed overall quota. Current: {} bytes, Incoming: {} bytes, Limit: {} bytes. Remaining: {} bytes.",
                                email, currentUsage, incomingFileSize, MAX_USER_SPACE_BYTES, remainingSpace);
                        return Mono.just(FileUploadResponse.builder()
                                .success(false)
                                .message(String.format("User %s overall storage quota exceeded. Current usage: %.2fMB, Limit: %.2fMB. Remaining: %.2fMB.",
                                        email, bytesToMegabytes(currentUsage), bytesToMegabytes(MAX_USER_SPACE_BYTES), bytesToMegabytes(remainingSpace)))
                                .currentStorageUsageMb(bytesToMegabytes(currentUsage))
                                .maxStorageQuotaMb(bytesToMegabytes(MAX_USER_SPACE_BYTES))
                                .build());
                    }

                    // 3. Create user folder if not exists
                    return createUserFolderIfNotExists(email)
                            .flatMap(userFolderId ->
                                    // 4. Get Drive instance
                                    getDriveInstance().flatMap(driveInstance ->
                                            // 5. Perform the actual file upload (blocking operation, so use fromCallable)
                                            Mono.fromCallable(() -> {
                                                log.info("Attempting to upload file '{}' for user '{}' into folder '{}'", originalFileName, email, userFolderId);
                                                String docsFolderId = getSubfolderId(driveInstance, userFolderId, "docs");
                                                if (docsFolderId == null) {
                                                    throw new IOException("Docs folder not found for user. Cannot upload file.");
                                                }

                                                File fileMeta = new File();
                                                fileMeta.setName(originalFileName);
                                                fileMeta.setParents(List.of(docsFolderId));

                                                ByteArrayContent mediaContent = new ByteArrayContent(
                                                        file.getContentType(), file.getBytes()
                                                );

                                                return driveInstance.files().create(fileMeta, mediaContent)
                                                        .setFields("id")
                                                        .execute();
                                            }).subscribeOn(Schedulers.boundedElastic()) // Run on a suitable scheduler
                                                    // 6. Update user's usage in DB after successful upload
                                                    .flatMap(uploadedFile -> {
                                                        user.setCurrentDriveUsageBytes(user.getCurrentDriveUsageBytes() + incomingFileSize);
                                                        return userRepository.save(user)
                                                                .map(updatedUser -> FileUploadResponse.builder()
                                                                        .fileId(uploadedFile.getId())
                                                                        .fileName(originalFileName)
                                                                        .currentStorageUsageMb(bytesToMegabytes(updatedUser.getCurrentDriveUsageBytes()))
                                                                        .maxStorageQuotaMb(bytesToMegabytes(MAX_USER_SPACE_BYTES))
                                                                        .message("File uploaded successfully. Storage updated.")
                                                                        .success(true)
                                                                        .build()
                                                                )
                                                                .onErrorResume(dbError -> {
                                                                    log.error("Failed to update user's drive usage in DB after file upload for {}: {}", email, dbError.getMessage());
                                                                    // If DB update fails, return a specific error message
                                                                    // We use the original user's usage here as the update failed.
                                                                    return Mono.just(FileUploadResponse.builder()
                                                                            .fileId(uploadedFile.getId())
                                                                            .fileName(originalFileName)
                                                                            .currentStorageUsageMb(bytesToMegabytes(user.getCurrentDriveUsageBytes()))
                                                                            .maxStorageQuotaMb(bytesToMegabytes(MAX_USER_SPACE_BYTES))
                                                                            .message("File uploaded, but failed to update storage usage in database. Please contact support.")
                                                                            .success(false)
                                                                            .build());
                                                                });
                                                    })
                                                    // 7. Handle any exceptions during the Google Drive upload or subsequent DB save
                                                    .onErrorResume(Exception.class, e -> {
                                                        log.error("Failed to upload file '{}' for user {}: {}", originalFileName, email, e.getMessage(), e);
                                                        return Mono.just(FileUploadResponse.builder()
                                                                .success(false)
                                                                .message("Failed to upload file: " + e.getMessage())
                                                                .currentStorageUsageMb(bytesToMegabytes(user.getCurrentDriveUsageBytes()))
                                                                .maxStorageQuotaMb(bytesToMegabytes(MAX_USER_SPACE_BYTES))
                                                                .build());
                                                    })
                                    )
                            );
                })
                // 8. Handle the case where findByEmail returns an empty Mono (user not found)
                .switchIfEmpty(Mono.defer(() -> { // Use Mono.defer to lazily create the Mono if needed
                    log.error("User with email {} not found for file upload operation (switchIfEmpty).", email);
                    return Mono.just(FileUploadResponse.builder()
                            .success(false)
                            .message("User not found for file upload operation.")
                            .build());
                }));
    }

    // Method to create an empty Google Doc file
    public Mono<FileViewResponse> createGoogleDoc(String email, String docTitle) {
        return createUserFolderIfNotExists(email).flatMap(userFolderId ->
                getDriveInstance().flatMap(driveInstance -> Mono.fromCallable(() -> {
                    log.info("Attempting to create Google Doc '{}' for user '{}'", docTitle, email);
                    try {
                        String docsFolderId = getSubfolderId(driveInstance, userFolderId, "docs");
                        if (docsFolderId == null) {
                            log.error("Docs folder not found for user {}. Cannot create Google Doc.", email);
                            return FileViewResponse.builder() // Return DTO with error message
                                    .success(false)
                                    .message("Docs folder not found for user. Cannot create Google Doc.")
                                    .build();
                        }

                        File fileMetadata = new File();
                        fileMetadata.setName(docTitle);
                        fileMetadata.setMimeType("application/vnd.google-apps.document");
                        fileMetadata.setParents(Collections.singletonList(docsFolderId));

                        File googleDoc = driveInstance.files().create(fileMetadata)
                                .setFields("id, name, webViewLink")
                                .execute();

                        log.info("Google Doc '{}' created successfully for user {}. ID: {}, WebViewLink: {}",
                                docTitle, email, googleDoc.getId(), googleDoc.getWebViewLink());

                        return FileViewResponse.builder()
                                .fileId(googleDoc.getId())
                                .docTitle(googleDoc.getName()) // Use docTitle field
                                .webViewLink(googleDoc.getWebViewLink())
                                .message("Google Doc created successfully.")
                                .success(true) // Set success to true
                                .build();
                    } catch (Exception e) {
                        log.error("Failed to create Google Doc '{}' for user {}: {}", docTitle, email, e.getMessage(), e);
                        return FileViewResponse.builder() // Return DTO with error message
                                .success(false)
                                .message("Error creating Google Doc: " + e.getMessage())
                                .build();
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
        );
    }

    // Method to get webViewLink for an existing file
    public Mono<FileViewResponse> getWebViewLinkForFile(String email, String fileId) {
        return userRepository.findByEmail(email) // Start by fetching the user reactively
                .flatMap(user -> {
                    if (user == null || user.getDriveFolderId() == null) {
                        return Mono.just(FileViewResponse.builder()
                                .success(false)
                                .message("User or user's Drive folder not found.")
                                .build());
                    }
                    return getDriveInstance().flatMap(driveInstance -> Mono.fromCallable(() -> {
                        log.info("Attempting to retrieve webViewLink for file ID '{}' for user '{}'", fileId, email);
                        String docsFolderId = getSubfolderId(driveInstance, user.getDriveFolderId(), "docs");
                        if (docsFolderId == null) {
                            return FileViewResponse.builder()
                                    .success(false)
                                    .message("User's docs folder not found.")
                                    .build();
                        }

                        FileList files = driveInstance.files().list()
                                .setQ(String.format("'%s' in parents and trashed=false and id='%s'", docsFolderId, fileId))
                                .setFields("files(id, name, webViewLink)")
                                .execute();

                        if (files.getFiles().isEmpty()) {
                            return FileViewResponse.builder()
                                    .success(false)
                                    .fileId(fileId)
                                    .message("File not found or not accessible in user's docs folder.")
                                    .build();
                        }

                        File file = files.getFiles().get(0);

                        log.info("Retrieved webViewLink for file '{}' (ID: {}): {}", file.getName(), fileId, file.getWebViewLink());

                        return FileViewResponse.builder()
                                .success(true)
                                .fileId(file.getId())
                                .docTitle(file.getName()) // Using docTitle for file name
                                .webViewLink(file.getWebViewLink())
                                .message("Web view link retrieved successfully.")
                                .build();

                    }).subscribeOn(Schedulers.boundedElastic())
                            .onErrorResume(Exception.class, e -> { // Catch exceptions during the Google Drive API call
                                log.error("Failed to retrieve webViewLink for file ID '{}' for user {}: {}", fileId, email, e.getMessage(), e);
                                return Mono.just(FileViewResponse.builder()
                                        .success(false)
                                        .fileId(fileId)
                                        .message("Error retrieving web view link: " + e.getMessage())
                                        .build());
                            }));
                })
                .switchIfEmpty(Mono.defer(() -> { // Handle case where findByEmail returns empty
                    log.error("User with email {} not found for file view operation (switchIfEmpty).", email);
                    return Mono.just(FileViewResponse.builder()
                            .success(false)
                            .message("User not found for file view operation.")
                            .build());
                }));
    }


    // List user files in "docs" folder
    public Mono<List<String>> listFiles(String email) {
        return createUserFolderIfNotExists(email)
                .flatMap(userFolderId ->
                        getDriveInstance().flatMap(driveInstance ->
                                Mono.fromCallable(() -> {
                                    log.info("Attempting to list files for user '{}' in folder '{}'", email, userFolderId);
                                    try {
                                        String docsFolderId = getSubfolderId(driveInstance, userFolderId, "docs");

                                        if (docsFolderId == null) {
                                            log.warn("Docs folder not found for user {}. Returning empty list.", email);
                                            return List.<String>of();
                                        }

                                        log.debug("Docs folder ID for user {}: {}", email, docsFolderId);

                                        FileList files = driveInstance.files().list()
                                                .setQ(String.format("'%s' in parents and trashed=false and mimeType!='application/vnd.google-apps.folder'", docsFolderId))
                                                .setFields("files(name)")
                                                .execute();

                                        List<String> names = new ArrayList<>();
                                        if (files.getFiles() != null) {
                                            for (File file : files.getFiles()) {
                                                names.add(file.getName());
                                            }
                                        }
                                        log.info("Found {} files for user {}.", names.size(), email);
                                        return names;
                                    } catch (Exception e) {
                                        log.error("Failed to list files for user {}: {}", email, e.getMessage(), e);
                                        throw e; // Re-throw to be caught by onErrorResume in controller
                                    }
                                }).subscribeOn(Schedulers.boundedElastic())
                        )
                );
    }
}
