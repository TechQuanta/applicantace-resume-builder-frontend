////package com.example.acespringbackend.service;
////
////import com.example.acespringbackend.model.User;
////import com.example.acespringbackend.repository.UserRepository;
////import com.example.acespringbackend.auth.dto.FileUploadResponse;
////import com.example.acespringbackend.model.UserFile;
////import com.example.acespringbackend.repository.UserFileRepository;
////import com.example.acespringbackend.auth.dto.FileDetail;
////import com.example.acespringbackend.auth.dto.DeleteResponse;
////
////import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
////import com.google.api.client.json.jackson2.JacksonFactory;
////import com.google.api.services.drive.Drive;
////import com.google.api.services.drive.DriveScopes;
////import com.google.api.services.drive.model.*;
////import com.google.auth.oauth2.GoogleCredentials;
////import com.google.auth.oauth2.ServiceAccountCredentials;
////
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.context.ApplicationContext;
////import org.springframework.core.io.Resource;
////import org.springframework.stereotype.Service;
////import org.springframework.web.multipart.MultipartFile;
////import reactor.core.publisher.Mono;
////import reactor.core.scheduler.Schedulers;
////
////import com.google.auth.http.HttpCredentialsAdapter;
////import com.google.api.client.http.ByteArrayContent;
////
////import java.io.IOException;
////import java.io.InputStream;
////import java.time.LocalDateTime;
////import java.time.ZoneId;
////import java.time.Instant;
////import java.util.*;
////
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////
////
////@Service
////public class DriveService {
////
////    private static final Logger log = LoggerFactory.getLogger(DriveService.class);
////
////    @Value("${google.drive.credentials.path}")
////    private String credentialsPathString;
////
////    @Value("${google.drive.master.folder.id}")
////    private String masterFolderId;
////
////    @Value("${user.drive.quota.mb:10}")
////    private double userDriveQuotaMb;
////
////    @Value("${user.max.individual.file.size.mb:3}")
////    private double maxIndividualFileSizeMb;
////
////    private long MAX_USER_SPACE_BYTES;
////    private long MAX_INDIVIDUAL_FILE_SIZE_BYTES;
////
////    private final ApplicationContext applicationContext;
////    private final UserRepository userRepository; // Re-enabled
////    private final UserFileRepository userFileRepository; // Re-enabled
////
////    private Drive drive;
////
////    // Re-enabled constructor with UserRepository and UserFileRepository
////    public DriveService(ApplicationContext applicationContext, UserRepository userRepository, UserFileRepository userFileRepository) {
////        this.applicationContext = applicationContext;
////        this.userRepository = userRepository;
////        this.userFileRepository = userFileRepository;
////    }
////
////    @Value("${user.drive.quota.mb:10}")
////    public void setQuotas(@Value("${user.drive.quota.mb:10}") double userDriveQuotaMbParam,
////                          @Value("${user.max.individual.file.size.mb:3}") double maxIndividualFileSizeMbParam) {
////        this.userDriveQuotaMb = userDriveQuotaMbParam;
////        this.MAX_USER_SPACE_BYTES = (long) (userDriveQuotaMbParam * 1024 * 1024);
////        log.info("User overall Drive Quota set to {} MB ({} bytes).", userDriveQuotaMbParam, MAX_USER_SPACE_BYTES);
////
////        this.maxIndividualFileSizeMb = maxIndividualFileSizeMbParam;
////        this.MAX_INDIVIDUAL_FILE_SIZE_BYTES = (long) (maxIndividualFileSizeMbParam * 1024 * 1024);
////        log.info("Max individual file size limit set to {} MB ({} bytes).", maxIndividualFileSizeMbParam, MAX_INDIVIDUAL_FILE_SIZE_BYTES);
////    }
////
////
////    private Mono<Drive> getDriveInstance() {
////        if (drive != null) return Mono.just(drive);
////
////        return Mono.fromCallable(() -> {
////            log.info("Attempting to initialize Google Drive instance using credentials from: {}", credentialsPathString);
////            try {
////                Resource credentialsResource = applicationContext.getResource(credentialsPathString);
////
////                if (!credentialsResource.exists()) {
////                    log.error("Google Drive credentials file DOES NOT EXIST at: {}", credentialsPathString);
////                    throw new IOException("Google Drive credentials file not found: " + credentialsPathString);
////                }
////                if (!credentialsResource.isReadable()) {
////                    log.error("Google Drive credentials file is NOT READABLE: {}", credentialsPathString);
////                    throw new IOException("Google Drive credentials file is not readable: " + credentialsPathString);
////                }
////
////                try (InputStream inputStream = credentialsResource.getInputStream()) {
////                    GoogleCredentials credentials = ServiceAccountCredentials
////                            .fromStream(inputStream)
////                            .createScoped(Collections.singleton(DriveScopes.DRIVE));
////
////                    drive = new Drive.Builder(
////                            GoogleNetHttpTransport.newTrustedTransport(),
////                            JacksonFactory.getDefaultInstance(),
////                            new HttpCredentialsAdapter(credentials)
////                    ).setApplicationName("AceDriveApp").build();
////                    log.info("Google Drive instance initialized successfully.");
////                    return drive;
////                }
////            } catch (Exception e) {
////                log.error("Failed to initialize Google Drive instance: {}", e.getMessage(), e);
////                throw e;
////            }
////        }).subscribeOn(Schedulers.boundedElastic());
////    }
////
////    private String getSubfolderId(Drive driveInstance, String parentFolderId, String subfolderName) throws IOException {
////        String query = String.format(
////                "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and '%s' in parents",
////                subfolderName, parentFolderId
////        );
////
////        FileList subfolders = driveInstance.files().list()
////                .setQ(query)
////                .setSpaces("drive")
////                .setFields("files(id)")
////                .execute();
////
////        if (!subfolders.getFiles().isEmpty()) {
////            return subfolders.getFiles().get(0).getId();
////        }
////        return null;
////    }
////
////    private long getUserFolderCurrentSizeFromDrive(Drive driveInstance, String folderId) throws IOException {
////        if (folderId == null) {
////            return 0;
////        }
////
////        long currentSize = 0;
////        String pageToken = null;
////        do {
////            FileList files = driveInstance.files().list()
////                    .setQ(String.format("'%s' in parents and trashed=false and mimeType!='application/vnd.google-apps.folder'", folderId))
////                    .setFields("nextPageToken, files(size)")
////                    .setSpaces("drive")
////                    .setPageToken(pageToken)
////                    .execute();
////
////            if (files.getFiles() != null) {
////                for (File file : files.getFiles()) {
////                    if (file.getSize() != null) {
////                        currentSize += file.getSize();
////                    }
////                }
////            }
////            pageToken = files.getNextPageToken();
////        } while (pageToken != null);
////
////        return currentSize;
////    }
////
////    private double bytesToMegabytes(long bytes) {
////        return bytes / (1024.0 * 1024.0);
////    }
////
////    public Mono<String> createUserFolderIfNotExists(String email) {
////        return getDriveInstance().flatMap(driveInstance -> Mono.fromCallable(() -> {
////            log.info("Attempting to create/verify Drive folder for user: {}", email);
////            try {
////                String query = String.format(
////                        "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and '%s' in parents",
////                        email, masterFolderId
////                );
////
////                FileList existing = driveInstance.files().list()
////                        .setQ(query)
////                        .setSpaces("drive")
////                        .setFields("files(id)")
////                        .execute();
////
////                String userFolderId;
////
////                if (!existing.getFiles().isEmpty()) {
////                    userFolderId = existing.getFiles().get(0).getId();
////                    log.info("User folder already exists for {}: {}", email, userFolderId);
////                } else {
////                    File metadata = new File();
////                    metadata.setName(email);
////                    metadata.setMimeType("application/vnd.google-apps.folder");
////                    metadata.setParents(List.of(masterFolderId));
////
////                    File folder = driveInstance.files().create(metadata)
////                            .setFields("id")
////                            .execute();
////
////                    userFolderId = folder.getId();
////                    log.info("Created new user folder for {}: {}", email, userFolderId);
////                }
////
////                List<String> subfolders = List.of("docs", "images", "tasks");
////
////                for (String sub : subfolders) {
////                    String subFolderId = getSubfolderId(driveInstance, userFolderId, sub);
////
////                    if (subFolderId == null) {
////                        File subMeta = new File();
////                        subMeta.setName(sub);
////                        subMeta.setMimeType("application/vnd.google-apps.folder");
////                        subMeta.setParents(List.of(userFolderId));
////                        driveInstance.files().create(subMeta).setFields("id").execute();
////                        log.info("Created subfolder '{}' for user: {}", sub, email);
////                    } else {
////                        log.info("Subfolder '{}' already exists for user: {}", sub, email);
////                    }
////                }
////                log.info("All necessary folders for user {} are verified/created.", email);
////                return userFolderId;
////            } catch (Exception e) {
////                log.error("Failed to create/verify Drive folder for user {}: {}", email, e.getMessage(), e);
////                throw e;
////            }
////        }).subscribeOn(Schedulers.boundedElastic()));
////    }
////
////    // >>>>>> THIS METHOD REMAINS AS IS, BUT WILL NOW RECEIVE userEmail from the Controller <<<<<<
////    public Mono<FileUploadResponse> uploadFile(String email, MultipartFile file, String targetFolderId) {
////        return userRepository.findByEmail(email)
////                .flatMap(user -> {
////                    if (user == null) {
////                        log.error("User with email {} not found for file upload.", email);
////                        return Mono.just(new FileUploadResponse(
////                                false, "User not found for file upload operation.", null, null, null, 0.0, 0.0));
////                    }
////
////                    long incomingFileSize = file.getSize();
////                    String originalFileName = file.getOriginalFilename();
////                    String fileMimeType = file.getContentType();
////
////                    if (incomingFileSize <= 0) {
////                        return Mono.just(new FileUploadResponse(
////                                false,
////                                "Cannot upload empty file or file with size 0.",
////                                originalFileName, null, fileMimeType,
////                                bytesToMegabytes(user.getCurrentDriveUsageBytes()),
////                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                        ));
////                    }
////
////                    if (incomingFileSize > MAX_INDIVIDUAL_FILE_SIZE_BYTES) {
////                        log.warn("File '{}' ({} bytes) exceeds individual file limit ({} bytes) for user {}.",
////                                originalFileName, incomingFileSize, MAX_INDIVIDUAL_FILE_SIZE_BYTES, email);
////                        return Mono.just(new FileUploadResponse(
////                                false,
////                                String.format("File '%s' size (%.2fMB) exceeds individual upload limit of %.2fMB.",
////                                        originalFileName, bytesToMegabytes(incomingFileSize), bytesToMegabytes(MAX_INDIVIDUAL_FILE_SIZE_BYTES)),
////                                originalFileName, null, fileMimeType,
////                                bytesToMegabytes(user.getCurrentDriveUsageBytes()),
////                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                        ));
////                    }
////
////                    long currentUsage = user.getCurrentDriveUsageBytes();
////                    log.info("User {} current Drive usage (from DB): {} bytes ({:.2f} MB). Incoming file size: {} bytes ({:.2f} MB).",
////                            email, currentUsage, bytesToMegabytes(currentUsage), incomingFileSize, bytesToMegabytes(incomingFileSize));
////
////                    if (currentUsage + incomingFileSize > MAX_USER_SPACE_BYTES) {
////                        long remainingSpace = MAX_USER_SPACE_BYTES - currentUsage;
////                        log.warn("User {} will exceed overall quota. Current: {} bytes, Incoming: {} bytes, Limit: {} bytes. Remaining: {} bytes.",
////                                email, currentUsage, incomingFileSize, MAX_USER_SPACE_BYTES, remainingSpace);
////                        return Mono.just(new FileUploadResponse(
////                                false,
////                                String.format("User %s overall storage quota exceeded. Current usage: %.2fMB, Limit: %.2fMB. Remaining: %.2fMB.",
////                                        email, bytesToMegabytes(currentUsage), bytesToMegabytes(MAX_USER_SPACE_BYTES), bytesToMegabytes(remainingSpace)),
////                                originalFileName, null, fileMimeType,
////                                bytesToMegabytes(currentUsage),
////                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                        ));
////                    }
////
////                    // For production, you might want to add a check here
////                    // to ensure 'targetFolderId' belongs to the 'user' or is within their permitted hierarchy.
////                    if (targetFolderId == null || targetFolderId.isEmpty()) {
////                        log.error("Target folder ID is null or empty. Cannot upload file.");
////                        return Mono.just(new FileUploadResponse(
////                                false,
////                                "Target folder ID for upload is missing.",
////                                originalFileName, null, fileMimeType,
////                                bytesToMegabytes(user.getCurrentDriveUsageBytes()),
////                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                        ));
////                    }
////
////
////                    return getDriveInstance().flatMap(driveInstance ->
////                            Mono.fromCallable(() -> {
////                                log.info("Attempting to upload file '{}' for user '{}' into PROVIDED FOLDER ID: '{}'", originalFileName, email, targetFolderId);
////
////                                File fileMeta = new File();
////                                fileMeta.setName(originalFileName);
////                                fileMeta.setParents(List.of(targetFolderId)); // Use targetFolderId directly as the parent
////                                fileMeta.setMimeType(fileMimeType);
////
////                                ByteArrayContent mediaContent = new ByteArrayContent(
////                                        fileMimeType, file.getBytes()
////                                );
////
////                                return driveInstance.files().create(fileMeta, mediaContent)
////                                        .setFields("id, webViewLink, thumbnailLink")
////                                        .execute();
////                            }).subscribeOn(Schedulers.boundedElastic())
////                                    .flatMap(uploadedDriveFile -> {
////                                        UserFile userFile = new UserFile();
////                                        userFile.setFilename(originalFileName);
////                                        userFile.setDriveFileId(uploadedDriveFile.getId());
////                                        userFile.setUserId(user.getId());
////                                        userFile.setUploadedAt(LocalDateTime.now());
////                                        userFile.setSize(incomingFileSize);
////                                        userFile.setContentType(fileMimeType);
////                                        userFile.setWebViewLink(uploadedDriveFile.getWebViewLink());
////                                        // userFile.setThumbnailLink(uploadedDriveFile.getThumbnailLink()); // No setter for thumbnailLink in UserFile
////
////                                        user.setCurrentDriveUsageBytes(user.getCurrentDriveUsageBytes() + incomingFileSize);
////
////                                        return Mono.zip(
////                                                userRepository.save(user),
////                                                userFileRepository.save(userFile)
////                                        ).map(tuple -> {
////                                            User updatedUser = tuple.getT1();
////                                            log.info("File '{}' uploaded (Drive ID: {}) and user usage updated for {}. New usage: {} bytes.",
////                                                    originalFileName, uploadedDriveFile.getId(), email, updatedUser.getCurrentDriveUsageBytes());
////                                            return new FileUploadResponse(
////                                                    true,
////                                                    "File uploaded successfully. Storage updated.",
////                                                    originalFileName,
////                                                    uploadedDriveFile.getId(),
////                                                    fileMimeType,
////                                                    bytesToMegabytes(updatedUser.getCurrentDriveUsageBytes()),
////                                                    bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                                            );
////                                        }).onErrorResume(dbError -> {
////                                            log.error("CRITICAL: File uploaded to Drive but failed to save user/file metadata in DB for {}: {}. Drive File ID: {}",
////                                                    email, dbError.getMessage(), uploadedDriveFile.getId(), dbError);
////                                            return Mono.just(new FileUploadResponse(
////                                                    false,
////                                                    "File uploaded, but failed to update database records. Please contact support. Drive File ID: " + uploadedDriveFile.getId(),
////                                                    originalFileName,
////                                                    uploadedDriveFile.getId(),
////                                                    fileMimeType,
////                                                    bytesToMegabytes(user.getCurrentDriveUsageBytes()),
////                                                    bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                                            ));
////                                        });
////                                    })
////                                    .onErrorResume(Exception.class, e -> {
////                                        log.error("Failed to upload file '{}' for user {}: {}", originalFileName, email, e.getMessage(), e);
////                                        return Mono.just(new FileUploadResponse(
////                                                false,
////                                                "Failed to upload file to Google Drive: " + e.getMessage(),
////                                                originalFileName, null, fileMimeType,
////                                                bytesToMegabytes(user.getCurrentDriveUsageBytes()),
////                                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                                        ));
////                                    })
////                    );
////                })
////                .switchIfEmpty(Mono.defer(() -> {
////                    log.error("User with email {} not found for file upload operation (switchIfEmpty).", email);
////                    return Mono.just(new FileUploadResponse(
////                            false, "User not found for file upload operation.", null, null, null, 0.0, 0.0));
////                }));
////    }
////
////    // You still have the listFiles and deleteFile methods which depend on UserRepository.
////    // I've completed the deleteFile method's structure for you below.
////    // If you plan to use these, ensure your security config allows authenticated users to access them.
////    public Mono<List<? extends Object>> listFiles(String email) {
////        return userRepository.findByEmail(email)
////                .flatMap(user -> {
////                    if (user == null || user.getDriveFolderId() == null || user.getDriveFolderId().isEmpty()) {
////                        log.warn("User {} or their Drive folder ID not found. Returning empty list.", email);
////                        return Mono.just(Collections.emptyList());
////                    }
////                    String userFolderId = user.getDriveFolderId();
////                    return getDriveInstance().flatMap(driveInstance ->
////                            Mono.fromCallable(() -> {
////                                log.info("Attempting to list files for user '{}' in folder '{}'", email, userFolderId);
////                                try {
////                                    String docsFolderId = getSubfolderId(driveInstance, userFolderId, "docs");
////
////                                    if (docsFolderId == null) {
////                                        log.warn("Docs folder not found for user {}. Returning empty list.", email);
////                                        return Collections.emptyList();
////                                    }
////
////                                    log.debug("Docs folder ID for user {}: {}", email, docsFolderId);
////
////                                    FileList files = driveInstance.files().list()
////                                            .setQ(String.format("'%s' in parents and trashed=false and mimeType!='application/vnd.google-apps.folder'", docsFolderId))
////                                            .setFields("files(id, name, mimeType, size, createdTime, webViewLink, thumbnailLink)")
////                                            .execute();
////
////                                    List<FileDetail> fileDetails = new ArrayList<>();
////                                    if (files.getFiles() != null) {
////                                        for (File file : files.getFiles()) {
////                                            String uploadedAtString = null;
////                                            if (file.getCreatedTime() != null) {
////                                                long milliseconds = file.getCreatedTime().getValue();
////                                                uploadedAtString = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault()).toString();
////                                            }
////
////                                            fileDetails.add(new FileDetail(
////                                                    null,
////                                                    file.getId(),
////                                                    file.getName(),
////                                                    file.getMimeType(),
////                                                    file.getSize() != null ? file.getSize() : 0L,
////                                                    uploadedAtString,
////                                                    file.getWebViewLink(),
////                                                    file.getThumbnailLink()
////                                            ));
////                                        }
////                                    }
////                                    log.info("Found {} files for user {}.", fileDetails.size(), email);
////                                    return fileDetails;
////                                } catch (Exception e) {
////                                    log.error("Failed to list files for user {}: {}", email, e.getMessage(), e);
////                                    throw e;
////                                }
////                            }).subscribeOn(Schedulers.boundedElastic())
////                    );
////                })
////                .switchIfEmpty(Mono.just(Collections.emptyList()));
////    }
////
////
////    public Mono<DeleteResponse> deleteFile(String email, String fileId) {
////        return userRepository.findByEmail(email)
////                .flatMap(user -> {
////                    if (user == null) {
////                        log.warn("User {} not found for file deletion (ID: {}).", email, fileId);
////                        return Mono.just(new DeleteResponse(
////                                false, "User not found for file deletion.", fileId, 0L, 0.0, 0.0));
////                    }
////
////                    return getDriveInstance().flatMap(driveInstance ->
////                            Mono.fromCallable(() -> {
////                                log.info("Attempting to get file details for deletion: File ID '{}' for user '{}'", fileId, email);
////                                File driveFile = driveInstance.files().get(fileId)
////                                        .setFields("parents,size")
////                                        .execute();
////
////                                if (driveFile == null) {
////                                    log.warn("File with ID '{}' not found on Drive for user {}.", fileId, email);
////                                    throw new IOException("File not found on Google Drive.");
////                                }
////
////                                String userRootFolderId = user.getDriveFolderId();
////                                String docsFolderId = getSubfolderId(driveInstance, userRootFolderId, "docs");
////
////                                // This check assumes files are either in the docs subfolder, the root user folder, or the fileId itself is a parent (which is unusual)
////                                boolean isFileInUserFolder = driveFile.getParents() != null &&
////                                                             (driveFile.getParents().contains(docsFolderId) || driveFile.getParents().contains(userRootFolderId));
////                                // Consider if the 'targetFolderId' used for upload should also be verified here for deletion
////                                // You might need a way to know which folder the file was uploaded into if it's not always 'docs'
////
////                                if (!isFileInUserFolder) {
////                                     log.warn("File ID '{}' is not within user {}'s recognized Drive folders. Deletion aborted.", fileId, email);
////                                     throw new IOException("File is not accessible for deletion by this user or not in their designated space.");
////                                }
////
////                                long deletedFileSize = driveFile.getSize() != null ? driveFile.getSize() : 0L;
////                                driveInstance.files().delete(fileId).execute();
////                                log.info("File ID '{}' deleted from Google Drive.", fileId);
////
////                                return userFileRepository.findByDriveFileId(fileId)
////                                        .flatMap(userFile -> {
////                                            return userFileRepository.delete(userFile)
////                                                    .then(Mono.just(userFile.getSize()));
////                                        })
////                                        .defaultIfEmpty(deletedFileSize)
////                                        .flatMap(actualDeletedSize -> {
////                                            long newUsage = Math.max(0, user.getCurrentDriveUsageBytes() - actualDeletedSize);
////                                            user.setCurrentDriveUsageBytes(newUsage);
////                                            return userRepository.save(user)
////                                                    .map(updatedUser -> {
////                                                        log.info("File ID '{}' deleted from DB and user usage updated for {}. New usage: {} bytes.",
////                                                                fileId, email, updatedUser.getCurrentDriveUsageBytes());
////                                                        return new DeleteResponse(
////                                                                true,
////                                                                "File deleted successfully. Storage updated.",
////                                                                fileId,
////                                                                actualDeletedSize,
////                                                                bytesToMegabytes(updatedUser.getCurrentDriveUsageBytes()),
////                                                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                                                        );
////                                                    });
////                                        });
////                            }).subscribeOn(Schedulers.boundedElastic())
////                                    .flatMap(monoResponse -> monoResponse)
////                                    .onErrorResume(e -> {
////                                        log.error("Failed to delete file ID '{}' for user {}: {}", fileId, email, e.getMessage(), e);
////                                        return Mono.just(new DeleteResponse(
////                                                false,
////                                                "Failed to delete file from Google Drive: " + e.getMessage(),
////                                                fileId, 0L, bytesToMegabytes(user.getCurrentDriveUsageBytes()), bytesToMegabytes(MAX_USER_SPACE_BYTES)
////                                        ));
////                                    })
////                    );
////                })
////                .switchIfEmpty(Mono.defer(() -> {
////                    log.error("User with email {} not found for file delete operation (file ID: {}).", email, fileId);
////                    return Mono.just(new DeleteResponse(
////                            false, "User not found for file deletion operation.", fileId, 0L, 0.0, 0.0));
////                }));
////    }
////}
//package com.example.acespringbackend.service;
//
//import com.example.acespringbackend.model.User;
//import com.example.acespringbackend.repository.UserRepository;
//import com.example.acespringbackend.auth.dto.FileUploadResponse;
//import com.example.acespringbackend.model.UserFile;
//import com.example.acespringbackend.repository.UserFileRepository;
//import com.example.acespringbackend.auth.dto.FileDetail;
//import com.example.acespringbackend.auth.dto.DeleteResponse;
//import com.example.acespringbackend.auth.dto.FileListResponse; // Import FileListResponse
//
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.*;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.auth.oauth2.ServiceAccountCredentials;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//
//import com.google.auth.http.HttpCredentialsAdapter;
//import com.google.api.client.http.ByteArrayContent;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.Instant;
//import java.util.*;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//@Service
//public class DriveService {
//
//    private static final Logger log = LoggerFactory.getLogger(DriveService.class);
//
//    @Value("${google.drive.credentials.path}")
//    private String credentialsPathString;
//
//    @Value("${google.drive.master.folder.id}")
//    private String masterFolderId;
//
//    @Value("${user.drive.quota.mb:10}")
//    private double userDriveQuotaMb;
//
//    @Value("${user.max.individual.file.size.mb:3}")
//    private double maxIndividualFileSizeMb;
//
//    private long MAX_USER_SPACE_BYTES;
//    private long MAX_INDIVIDUAL_FILE_SIZE_BYTES;
//
//    private final ApplicationContext applicationContext;
//    private final UserRepository userRepository;
//    private final UserFileRepository userFileRepository;
//
//    private Drive drive;
//
//    public DriveService(ApplicationContext applicationContext, UserRepository userRepository, UserFileRepository userFileRepository) {
//        this.applicationContext = applicationContext;
//        this.userRepository = userRepository;
//        this.userFileRepository = userFileRepository;
//    }
//
//    @Value("${user.drive.quota.mb:10}")
//    public void setQuotas(@Value("${user.drive.quota.mb:10}") double userDriveQuotaMbParam,
//                          @Value("${user.max.individual.file.size.mb:3}") double maxIndividualFileSizeMbParam) {
//        this.userDriveQuotaMb = userDriveQuotaMbParam;
//        this.MAX_USER_SPACE_BYTES = (long) (userDriveQuotaMbParam * 1024 * 1024);
//        log.info("User overall Drive Quota set to {} MB ({} bytes).", userDriveQuotaMbParam, MAX_USER_SPACE_BYTES);
//
//        this.maxIndividualFileSizeMb = maxIndividualFileSizeMbParam;
//        this.MAX_INDIVIDUAL_FILE_SIZE_BYTES = (long) (maxIndividualFileSizeMbParam * 1024 * 1024);
//        log.info("Max individual file size limit set to {} MB ({} bytes).", maxIndividualFileSizeMbParam, MAX_INDIVIDUAL_FILE_SIZE_BYTES);
//    }
//
//
//    private Mono<Drive> getDriveInstance() {
//        if (drive != null) return Mono.just(drive);
//
//        return Mono.fromCallable(() -> {
//            log.info("Attempting to initialize Google Drive instance using credentials from: {}", credentialsPathString);
//            try {
//                Resource credentialsResource = applicationContext.getResource(credentialsPathString);
//
//                if (!credentialsResource.exists()) {
//                    log.error("Google Drive credentials file DOES NOT EXIST at: {}", credentialsPathString);
//                    throw new IOException("Google Drive credentials file not found: " + credentialsPathString);
//                }
//                if (!credentialsResource.isReadable()) {
//                    log.error("Google Drive credentials file is NOT READABLE: {}", credentialsPathString);
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
//                            JacksonFactory.getDefaultInstance(),
//                            new HttpCredentialsAdapter(credentials)
//                    ).setApplicationName("AceDriveApp").build();
//                    log.info("Google Drive instance initialized successfully.");
//                    return drive;
//                }
//            } catch (Exception e) {
//                log.error("Failed to initialize Google Drive instance: {}", e.getMessage(), e);
//                throw e;
//            }
//        }).subscribeOn(Schedulers.boundedElastic());
//    }
//
//    private String getSubfolderId(Drive driveInstance, String parentFolderId, String subfolderName) throws IOException {
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
//    private long getUserFolderCurrentSizeFromDrive(Drive driveInstance, String folderId) throws IOException {
//        if (folderId == null) {
//            return 0;
//        }
//
//        long currentSize = 0;
//        String pageToken = null;
//        do {
//            FileList files = driveInstance.files().list()
//                    .setQ(String.format("'%s' in parents and trashed=false and mimeType!='application/vnd.google-apps.folder'", folderId))
//                    .setFields("nextPageToken, files(size)")
//                    .setSpaces("drive")
//                    .setPageToken(pageToken)
//                    .execute();
//
//            if (files.getFiles() != null) {
//                for (File file : files.getFiles()) {
//                    if (file.getSize() != null) {
//                        currentSize += file.getSize();
//                    }
//                }
//            }
//            pageToken = files.getNextPageToken();
//        } while (pageToken != null);
//
//        return currentSize;
//    }
//
//    private double bytesToMegabytes(long bytes) {
//        return bytes / (1024.0 * 1024.0);
//    }
//
//    public Mono<String> createUserFolderIfNotExists(String email) {
//        return getDriveInstance().flatMap(driveInstance -> Mono.fromCallable(() -> {
//            log.info("Attempting to create/verify Drive folder for user: {}", email);
//            try {
//                String query = String.format(
//                        "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and '%s' in parents",
//                        email, masterFolderId
//                );
//
//                FileList existing = driveInstance.files().list()
//                        .setQ(query)
//                        .setSpaces("drive")
//                        .setFields("files(id)")
//                        .execute();
//
//                String userFolderId;
//
//                if (!existing.getFiles().isEmpty()) {
//                    userFolderId = existing.getFiles().get(0).getId();
//                    log.info("User folder already exists for {}: {}", email, userFolderId);
//                } else {
//                    File metadata = new File();
//                    metadata.setName(email);
//                    metadata.setMimeType("application/vnd.google-apps.folder");
//                    metadata.setParents(List.of(masterFolderId));
//
//                    File folder = driveInstance.files().create(metadata)
//                            .setFields("id")
//                            .execute();
//
//                    userFolderId = folder.getId();
//                    log.info("Created new user folder for {}: {}", email, userFolderId);
//                }
//
//                List<String> subfolders = List.of("docs", "images", "tasks");
//
//                for (String sub : subfolders) {
//                    String subFolderId = getSubfolderId(driveInstance, userFolderId, sub);
//
//                    if (subFolderId == null) {
//                        File subMeta = new File();
//                        subMeta.setName(sub);
//                        subMeta.setMimeType("application/vnd.google-apps.folder");
//                        subMeta.setParents(List.of(userFolderId));
//                        driveInstance.files().create(subMeta).setFields("id").execute();
//                        log.info("Created subfolder '{}' for user: {}", sub, email);
//                    } else {
//                        log.info("Subfolder '{}' already exists for user: {}", sub, email);
//                    }
//                }
//                log.info("All necessary folders for user {} are verified/created.", email);
//                return userFolderId;
//            } catch (Exception e) {
//                log.error("Failed to create/verify Drive folder for user {}: {}", email, e.getMessage(), e);
//                throw e;
//            }
//        }).subscribeOn(Schedulers.boundedElastic()));
//    }
//
//    // >>>>>> THIS METHOD REMAINS AS IS, BUT WILL NOW RECEIVE userEmail from the Controller <<<<<<
//    public Mono<FileUploadResponse> uploadFile(String email, MultipartFile file, String targetFolderId) {
//        return userRepository.findByEmail(email)
//                .flatMap(user -> {
//                    if (user == null) {
//                        log.error("User with email {} not found for file upload.", email);
//                        return Mono.just(new FileUploadResponse(
//                                false, "User not found for file upload operation.", null, null, null, 0.0, 0.0));
//                    }
//
//                    long incomingFileSize = file.getSize();
//                    String originalFileName = file.getOriginalFilename();
//                    String fileMimeType = file.getContentType();
//
//                    if (incomingFileSize <= 0) {
//                        return Mono.just(new FileUploadResponse(
//                                false,
//                                "Cannot upload empty file or file with size 0.",
//                                originalFileName, null, fileMimeType,
//                                bytesToMegabytes(user.getCurrentDriveUsageBytes()),
//                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                        ));
//                    }
//
//                    if (incomingFileSize > MAX_INDIVIDUAL_FILE_SIZE_BYTES) {
//                        log.warn("File '{}' ({} bytes) exceeds individual file limit ({} bytes) for user {}.",
//                                originalFileName, incomingFileSize, MAX_INDIVIDUAL_FILE_SIZE_BYTES, email);
//                        return Mono.just(new FileUploadResponse(
//                                false,
//                                String.format("File '%s' size (%.2fMB) exceeds individual upload limit of %.2fMB.",
//                                        originalFileName, bytesToMegabytes(incomingFileSize), bytesToMegabytes(MAX_INDIVIDUAL_FILE_SIZE_BYTES)),
//                                originalFileName, null, fileMimeType,
//                                bytesToMegabytes(user.getCurrentDriveUsageBytes()),
//                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                        ));
//                    }
//
//                    long currentUsage = user.getCurrentDriveUsageBytes();
//                    log.info("User {} current Drive usage (from DB): {} bytes ({:.2f} MB). Incoming file size: {} bytes ({:.2f} MB).",
//                            email, currentUsage, bytesToMegabytes(currentUsage), incomingFileSize, bytesToMegabytes(incomingFileSize));
//
//                    if (currentUsage + incomingFileSize > MAX_USER_SPACE_BYTES) {
//                        long remainingSpace = MAX_USER_SPACE_BYTES - currentUsage;
//                        log.warn("User {} will exceed overall quota. Current: {} bytes, Incoming: {} bytes, Limit: {} bytes. Remaining: {} bytes.",
//                                email, currentUsage, incomingFileSize, MAX_USER_SPACE_BYTES, remainingSpace);
//                        return Mono.just(new FileUploadResponse(
//                                false,
//                                String.format("User %s overall storage quota exceeded. Current usage: %.2fMB, Limit: %.2fMB. Remaining: %.2fMB.",
//                                        email, bytesToMegabytes(currentUsage), bytesToMegabytes(MAX_USER_SPACE_BYTES), bytesToMegabytes(remainingSpace)),
//                                originalFileName, null, fileMimeType,
//                                bytesToMegabytes(currentUsage),
//                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                        ));
//                    }
//
//                    // For production, you might want to add a check here
//                    // to ensure 'targetFolderId' belongs to the 'user' or is within their permitted hierarchy.
//                    if (targetFolderId == null || targetFolderId.isEmpty()) {
//                        log.error("Target folder ID is null or empty. Cannot upload file.");
//                        return Mono.just(new FileUploadResponse(
//                                false,
//                                "Target folder ID for upload is missing.",
//                                originalFileName, null, fileMimeType,
//                                bytesToMegabytes(user.getCurrentDriveUsageBytes()),
//                                bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                        ));
//                    }
//
//
//                    return getDriveInstance().flatMap(driveInstance ->
//                                Mono.fromCallable(() -> {
//                                    log.info("Attempting to upload file '{}' for user '{}' into PROVIDED FOLDER ID: '{}'", originalFileName, email, targetFolderId);
//
//                                    File fileMeta = new File();
//                                    fileMeta.setName(originalFileName);
//                                    fileMeta.setParents(List.of(targetFolderId)); // Use targetFolderId directly as the parent
//                                    fileMeta.setMimeType(fileMimeType);
//
//                                    ByteArrayContent mediaContent = new ByteArrayContent(
//                                            fileMimeType, file.getBytes()
//                                    );
//
//                                    return driveInstance.files().create(fileMeta, mediaContent)
//                                            .setFields("id, webViewLink, thumbnailLink")
//                                            .execute();
//                                }).subscribeOn(Schedulers.boundedElastic())
//                                        .flatMap(uploadedDriveFile -> {
//                                            UserFile userFile = new UserFile();
//                                            userFile.setFilename(originalFileName);
//                                            userFile.setDriveFileId(uploadedDriveFile.getId());
//                                            userFile.setUserId(user.getId());
//                                            userFile.setUploadedAt(LocalDateTime.now());
//                                            userFile.setSize(incomingFileSize);
//                                            userFile.setContentType(fileMimeType);
//                                            userFile.setWebViewLink(uploadedDriveFile.getWebViewLink());
//                                            // userFile.setThumbnailLink(uploadedDriveFile.getThumbnailLink()); // No setter for thumbnailLink in UserFile
//
//                                            user.setCurrentDriveUsageBytes(user.getCurrentDriveUsageBytes() + incomingFileSize);
//
//                                            return Mono.zip(
//                                                    userRepository.save(user),
//                                                    userFileRepository.save(userFile)
//                                            ).map(tuple -> {
//                                                User updatedUser = tuple.getT1();
//                                                log.info("File '{}' uploaded (Drive ID: {}) and user usage updated for {}. New usage: {} bytes.",
//                                                        originalFileName, uploadedDriveFile.getId(), email, updatedUser.getCurrentDriveUsageBytes());
//                                                return new FileUploadResponse(
//                                                        true,
//                                                        "File uploaded successfully. Storage updated.",
//                                                        originalFileName,
//                                                        uploadedDriveFile.getId(),
//                                                        fileMimeType,
//                                                        bytesToMegabytes(updatedUser.getCurrentDriveUsageBytes()),
//                                                        bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                                                );
//                                            }).onErrorResume(dbError -> {
//                                                log.error("CRITICAL: File uploaded to Drive but failed to save user/file metadata in DB for {}: {}. Drive File ID: {}",
//                                                        email, dbError.getMessage(), uploadedDriveFile.getId(), dbError);
//                                                return Mono.just(new FileUploadResponse(
//                                                        false,
//                                                        "File uploaded, but failed to update database records. Please contact support. Drive File ID: " + uploadedDriveFile.getId(),
//                                                        originalFileName,
//                                                        uploadedDriveFile.getId(),
//                                                        fileMimeType,
//                                                        bytesToMegabytes(user.getCurrentDriveUsageBytes()),
//                                                        bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                                                ));
//                                            });
//                                        })
//                                        .onErrorResume(Exception.class, e -> {
//                                            log.error("Failed to upload file '{}' for user {}: {}", originalFileName, email, e.getMessage(), e);
//                                            return Mono.just(new FileUploadResponse(
//                                                    false,
//                                                    "Failed to upload file to Google Drive: " + e.getMessage(),
//                                                    originalFileName, null, fileMimeType,
//                                                    bytesToMegabytes(user.getCurrentDriveUsageBytes()),
//                                                    bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                                            ));
//                                        })
//                    );
//                })
//                .switchIfEmpty(Mono.defer(() -> {
//                    log.error("User with email {} not found for file upload operation (switchIfEmpty).", email);
//                    return Mono.just(new FileUploadResponse(
//                            false, "User not found for file upload operation.", null, null, null, 0.0, 0.0));
//                }));
//    }
//
//    /**
//     * Retrieves a list of file details for a specific user and a subfolder (e.g., "docs", "images").
//     *
//     * @param email The email of the user.
//     * @param folderId The Google Drive ID of the specific folder (e.g., user's "docs" folder ID).
//     * @return A Mono emitting a FileListResponse containing the file details or an error message.
//     */
//    public Mono<FileListResponse> getAllFileDetails(String email, String folderId) {
//        return userRepository.findByEmail(email)
//                .flatMap(user -> {
//                    if (user == null || user.getDriveFolderId() == null || user.getDriveFolderId().isEmpty()) {
//                        log.warn("User {} or their root Drive folder ID not found. Cannot list files.", email);
//                        return Mono.just(new FileListResponse(
//                                false, "User or user's Drive folder not found.",
//                                Collections.emptyList(), 0.0, userDriveQuotaMb));
//                    }
//
//                    // Validate that the provided folderId is a subfolder of the user's root folder
//                    // Or, if the requirement is to list all files directly under the user's root,
//                    // you would use user.getDriveFolderId() directly for the query.
//                    // Assuming the 'folderId' parameter passed to this method is already the specific subfolder (like 'docs')
//                    // or the main user folder that the client wants to list.
//
//                    return getDriveInstance().flatMap(driveInstance ->
//                                Mono.fromCallable(() -> {
//                                    log.info("Attempting to list files for user '{}' in provided folder ID '{}'", email, folderId);
//                                    try {
//                                        // Verify if the provided folderId actually exists and belongs to the user
//                                        // (Optional but highly recommended for security)
//                                        // You could fetch the file metadata for 'folderId' and check its parents
//                                        // to ensure it's under masterFolderId and the user's root folder.
//                                        // For simplicity, we'll assume the provided folderId is valid and correct for the user.
//
//                                        FileList files = driveInstance.files().list()
//                                                .setQ(String.format("'%s' in parents and trashed=false and mimeType!='application/vnd.google-apps.folder'", folderId))
//                                                .setFields("files(id, name, mimeType, size, createdTime, webViewLink, thumbnailLink)")
//                                                .setSpaces("drive")
//                                                .execute();
//
//                                        List<FileDetail> fileDetails = new ArrayList<>();
//                                        if (files.getFiles() != null) {
//                                            for (File file : files.getFiles()) {
//                                                String uploadedAtString = null;
//                                                if (file.getCreatedTime() != null) {
//                                                    long milliseconds = file.getCreatedTime().getValue();
//                                                    uploadedAtString = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault()).toString();
//                                                }
//                                                // Assuming mongoFileId is not available from Drive API, set as null or retrieve from UserFileRepository if needed.
//                                                // For this example, we'll retrieve it from the UserFileRepository.
//                                                UserFile userFileRecord = userFileRepository.findByDriveFileId(file.getId()).block(); // Blocking call, consider async if performance is critical for many files
//                                                String mongoId = userFileRecord != null ? userFileRecord.getId() : null;
//
//
//                                                fileDetails.add(new FileDetail(
//                                                        mongoId, // Set MongoDB ID if found
//                                                        file.getId(),
//                                                        file.getName(),
//                                                        file.getMimeType(),
//                                                        file.getSize() != null ? file.getSize() : 0L,
//                                                        uploadedAtString,
//                                                        file.getWebViewLink(),
//                                                        file.getThumbnailLink()
//                                                ));
//                                            }
//                                        }
//                                        log.info("Found {} files for user {} in folder {}.", fileDetails.size(), email, folderId);
//                                        return new FileListResponse(true, "Files retrieved successfully.", fileDetails,
//                                                bytesToMegabytes(user.getCurrentDriveUsageBytes()), bytesToMegabytes(MAX_USER_SPACE_BYTES));
//
//                                    } catch (Exception e) {
//                                        log.error("Failed to list files for user {} in folder {}: {}", email, folderId, e.getMessage(), e);
//                                        throw e; // Re-throw to be caught by onErrorResume
//                                    }
//                                }).subscribeOn(Schedulers.boundedElastic())
//                    )
//                    .onErrorResume(Exception.class, e -> {
//                        return Mono.just(new FileListResponse(false, "Failed to retrieve files: " + e.getMessage(),
//                                Collections.emptyList(), bytesToMegabytes(user.getCurrentDriveUsageBytes()), bytesToMegabytes(MAX_USER_SPACE_BYTES)));
//                    });
//                })
//                .switchIfEmpty(Mono.just(new FileListResponse(
//                        false, "User not found for file listing operation.",
//                        Collections.emptyList(), 0.0, userDriveQuotaMb)));
//    }
//
//
//    public Mono<DeleteResponse> deleteFile(String email, String fileId) {
//        return userRepository.findByEmail(email)
//                .flatMap(user -> {
//                    if (user == null) {
//                        log.warn("User {} not found for file deletion (ID: {}).", email, fileId);
//                        return Mono.just(new DeleteResponse(
//                                false, "User not found for file deletion.", fileId, 0L, 0.0, 0.0));
//                    }
//
//                    return getDriveInstance().flatMap(driveInstance ->
//                                Mono.fromCallable(() -> {
//                                    log.info("Attempting to get file details for deletion: File ID '{}' for user '{}'", fileId, email);
//                                    File driveFile = driveInstance.files().get(fileId)
//                                            .setFields("parents,size")
//                                            .execute();
//
//                                    if (driveFile == null) {
//                                        log.warn("File with ID '{}' not found on Drive for user {}.", fileId, email);
//                                        throw new IOException("File not found on Google Drive.");
//                                    }
//
//                                    String userRootFolderId = user.getDriveFolderId();
//                                    // Retrieve all known subfolders of the user to verify parentage
//                                    List<String> userFolderAndSubfolderIds = new ArrayList<>();
//                                    userFolderAndSubfolderIds.add(userRootFolderId);
//                                    // Add specific subfolder IDs if they are known (e.g., from initial setup)
//                                    // This requires fetching them dynamically or storing them with the user
//                                    String docsFolderId = getSubfolderId(driveInstance, userRootFolderId, "docs");
//                                    if (docsFolderId != null) userFolderAndSubfolderIds.add(docsFolderId);
//                                    String imagesFolderId = getSubfolderId(driveInstance, userRootFolderId, "images");
//                                    if (imagesFolderId != null) userFolderAndSubfolderIds.add(imagesFolderId);
//                                    String tasksFolderId = getSubfolderId(driveInstance, userRootFolderId, "tasks");
//                                    if (tasksFolderId != null) userFolderAndSubfolderIds.add(tasksFolderId);
//
//                                    boolean isFileInUserFolders = driveFile.getParents() != null &&
//                                            driveFile.getParents().stream().anyMatch(userFolderAndSubfolderIds::contains);
//
//
//                                    if (!isFileInUserFolders) {
//                                        log.warn("File ID '{}' is not within user {}'s recognized Drive folders. Deletion aborted.", fileId, email);
//                                        throw new IOException("File is not accessible for deletion by this user or not in their designated space.");
//                                    }
//
//                                    long deletedFileSize = driveFile.getSize() != null ? driveFile.getSize() : 0L;
//                                    driveInstance.files().delete(fileId).execute();
//                                    log.info("File ID '{}' deleted from Google Drive.", fileId);
//
//                                    return userFileRepository.findByDriveFileId(fileId)
//                                            .flatMap(userFile -> {
//                                                return userFileRepository.delete(userFile)
//                                                        .then(Mono.just(userFile.getSize()));
//                                            })
//                                            .defaultIfEmpty(deletedFileSize) // If no DB record, use size from Drive API
//                                            .flatMap(actualDeletedSize -> {
//                                                long newUsage = Math.max(0, user.getCurrentDriveUsageBytes() - actualDeletedSize);
//                                                user.setCurrentDriveUsageBytes(newUsage);
//                                                return userRepository.save(user)
//                                                        .map(updatedUser -> {
//                                                            log.info("File ID '{}' deleted from DB and user usage updated for {}. New usage: {} bytes.",
//                                                                    fileId, email, updatedUser.getCurrentDriveUsageBytes());
//                                                            return new DeleteResponse(
//                                                                    true,
//                                                                    "File deleted successfully. Storage updated.",
//                                                                    fileId,
//                                                                    actualDeletedSize,
//                                                                    bytesToMegabytes(updatedUser.getCurrentDriveUsageBytes()),
//                                                                    bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                                                            );
//                                                        });
//                                            });
//                                }).subscribeOn(Schedulers.boundedElastic())
//                                        .flatMap(monoResponse -> monoResponse) // Flatten the Mono<Mono<DeleteResponse>>
//                                        .onErrorResume(e -> {
//                                            log.error("Failed to delete file ID '{}' for user {}: {}", fileId, email, e.getMessage(), e);
//                                            return Mono.just(new DeleteResponse(
//                                                    false,
//                                                    "Failed to delete file from Google Drive: " + e.getMessage(),
//                                                    fileId, 0L, bytesToMegabytes(user.getCurrentDriveUsageBytes()), bytesToMegabytes(MAX_USER_SPACE_BYTES)
//                                            ));
//                                        })
//                    );
//                })
//                .switchIfEmpty(Mono.defer(() -> {
//                    log.error("User with email {} not found for file delete operation (file ID: {}).", email, fileId);
//                    return Mono.just(new DeleteResponse(
//                            false, "User not found for file deletion operation.", fileId, 0L, 0.0, 0.0));
//                }));
//    }
//}

package com.example.acespringbackend.service;

import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.auth.dto.FileUploadResponse;
import com.example.acespringbackend.model.UserFile;
import com.example.acespringbackend.repository.UserFileRepository;
import com.example.acespringbackend.utility.DriveUtility;
import com.example.acespringbackend.auth.dto.FileDetail;
import com.example.acespringbackend.auth.dto.DeleteResponse;
import com.example.acespringbackend.auth.dto.FileListResponse;

import com.google.api.services.drive.model.File; // Still needed for Google Drive File object type
import com.google.api.services.drive.model.FileList;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * DriveService class handles the business logic related to user file management,
 * enforces quotas, and persists file metadata in the application's database.
 * It delegates all direct Google Drive API interactions to DriveUtility.
 */
@Service
public class DriveService {

    private static final Logger log = LoggerFactory.getLogger(DriveService.class);

    private final UserRepository userRepository;
    private final UserFileRepository userFileRepository;
    private final DriveUtility driveUtility; // Injected DriveUtility
    private final DriveProperties driveProperties; // Injected DriveProperties

    /**
     * Constructor for DriveService, injecting necessary repositories, DriveUtility, and DriveProperties.
     *
     * @param userRepository The repository for User entities.
     * @param userFileRepository The repository for UserFile entities.
     * @param driveUtility The utility class for Google Drive operations.
     * @param driveProperties The configuration properties related to Google Drive.
     */
    public DriveService(UserRepository userRepository, UserFileRepository userFileRepository, DriveUtility driveUtility, DriveProperties driveProperties) {
        this.userRepository = userRepository;
        this.userFileRepository = userFileRepository;
        this.driveUtility = driveUtility;
        this.driveProperties = driveProperties; // Assign injected properties
    }

    /**
     * Creates a user-specific folder and its standard subfolders (docs, images, tasks) in Google Drive
     * if they do not already exist. This method ensures a consistent folder structure for each user.
     *
     * @param email The email of the user for whom the folder structure needs to be created or verified.
     * @return A Mono that emits the Google Drive ID of the user's main root folder.
     */
    public Mono<String> createUserFolderIfNotExists(String email) {
        return driveUtility.getDriveInstance().flatMap(driveInstance -> Mono.fromCallable(() -> {
            log.info("DriveService: Attempting to create/verify Drive folder structure for user: {}", email);
            try {
                String masterFolderId = driveProperties.getMasterFolderId(); // Get from DriveProperties
                String userFolderId;

                // 1. Check for existing user root folder
                String query = String.format(
                        "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and '%s' in parents",
                        email, masterFolderId
                );
                FileList existingUserFolders = driveInstance.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id)")
                        .execute();

                if (!existingUserFolders.getFiles().isEmpty()) {
                    userFolderId = existingUserFolders.getFiles().get(0).getId();
                    log.info("DriveService: User root folder already exists for {}: {}", email, userFolderId);
                } else {
                    // 2. Create user root folder if it doesn't exist
                    File createdFolder = driveUtility.createDriveFolder(driveInstance, email, masterFolderId);
                    userFolderId = createdFolder.getId();
                    log.info("DriveService: Created new user root folder for {}: {}", email, userFolderId);
                }

                // 3. Create default subfolders (docs, images, tasks) if they don't exist
                List<String> subfolderNames = List.of("docs", "images", "tasks");
                for (String subName : subfolderNames) {
                    String subFolderId = driveUtility.getSubfolderId(driveInstance, userFolderId, subName);
                    if (subFolderId == null) {
                        driveUtility.createDriveFolder(driveInstance, subName, userFolderId);
                        log.info("DriveService: Created subfolder '{}' for user: {}", subName, email);
                    } else {
                        log.info("DriveService: Subfolder '{}' already exists for user: {}", subName, email);
                    }
                }
                log.info("DriveService: All necessary folders for user {} are verified/created.", email);
                return userFolderId;
            } catch (Exception e) {
                log.error("DriveService: Failed to create/verify Drive folder for user {}: {}", email, e.getMessage(), e);
                throw e; // Re-throw the exception for downstream error handling
            }
        }).subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * Uploads a multipart file to a specified Google Drive folder for a given user.
     * This method includes robust quota checks (individual file size and total user usage)
     * before initiating the upload and persists the file metadata in the application's database.
     *
     * @param email The email of the user attempting to upload the file.
     * @param file The MultipartFile object containing the file's content and metadata.
     * @param targetFolderId The Google Drive ID of the specific folder where the file should be uploaded.
     * @return A Mono emitting a FileUploadResponse that indicates success or failure,
     * along with relevant file and quota details.
     */
    public Mono<FileUploadResponse> uploadFile(String email, MultipartFile file, String targetFolderId) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (user == null) {
                        log.error("DriveService: User with email {} not found for file upload.", email);
                        return Mono.just(new FileUploadResponse(
                                false, "User not found for file upload operation.", null, null, null, 0.0, 0.0));
                    }

                    long incomingFileSize = file.getSize();
                    String originalFileName = file.getOriginalFilename();
                    String fileMimeType = file.getContentType();

                    // Get quota limits from DriveProperties via DriveUtility
                    long maxIndividualFileSizeBytes = driveProperties.getMaxIndividualFileSizeBytes();
                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes();

                    // --- Quota and Input Validation ---
                    if (incomingFileSize <= 0) {
                        return Mono.just(new FileUploadResponse(
                                false,
                                "Cannot upload empty file or file with size 0.",
                                originalFileName, null, fileMimeType,
                                driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                        ));
                    }

                    if (incomingFileSize > maxIndividualFileSizeBytes) {
                        log.warn("DriveService: File '{}' ({} bytes) exceeds individual file limit ({} bytes) for user {}.",
                                originalFileName, incomingFileSize, maxIndividualFileSizeBytes, email);
                        return Mono.just(new FileUploadResponse(
                                false,
                                String.format("File '%s' size (%.2fMB) exceeds individual upload limit of %.2fMB.",
                                        originalFileName, driveUtility.bytesToMegabytes(incomingFileSize), driveUtility.bytesToMegabytes(maxIndividualFileSizeBytes)),
                                originalFileName, null, fileMimeType,
                                driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                        ));
                    }

                    long currentUsage = user.getCurrentDriveUsageBytes();
                    log.info("DriveService: User {} current Drive usage (from DB): {} bytes ({:.2f} MB). Incoming file size: {} bytes ({:.2f} MB).",
                            email, currentUsage, driveUtility.bytesToMegabytes(currentUsage), incomingFileSize, driveUtility.bytesToMegabytes(incomingFileSize));

                    if (currentUsage + incomingFileSize > maxUserSpaceBytes) {
                        long remainingSpace = maxUserSpaceBytes - currentUsage;
                        log.warn("DriveService: User {} will exceed overall quota. Current: {} bytes, Incoming: {} bytes, Limit: {} bytes. Remaining: {} bytes.",
                                email, currentUsage, incomingFileSize, maxUserSpaceBytes, remainingSpace);
                        return Mono.just(new FileUploadResponse(
                                false,
                                String.format("User %s overall storage quota exceeded. Current usage: %.2fMB, Limit: %.2fMB. Remaining: %.2fMB.",
                                        email, driveUtility.bytesToMegabytes(currentUsage), driveUtility.bytesToMegabytes(maxUserSpaceBytes), driveUtility.bytesToMegabytes(remainingSpace)),
                                originalFileName, null, fileMimeType,
                                driveUtility.bytesToMegabytes(currentUsage),
                                driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                        ));
                    }

                    if (targetFolderId == null || targetFolderId.isEmpty()) {
                        log.error("DriveService: Target folder ID is null or empty. Cannot upload file.");
                        return Mono.just(new FileUploadResponse(
                                false,
                                "Target folder ID for upload is missing.",
                                originalFileName, null, fileMimeType,
                                driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                        ));
                    }

                    // --- Google Drive Upload and DB Update ---
                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> {
                                log.info("DriveService: Initiating upload of file '{}' for user '{}' into folder ID: '{}'", originalFileName, email, targetFolderId);
                                return driveUtility.uploadFileToDrive(driveInstance, originalFileName, fileMimeType, file.getBytes(), targetFolderId);
                            }).subscribeOn(Schedulers.boundedElastic())
                                    .flatMap(uploadedDriveFile -> {
                                        // Save file metadata to application's database
                                        UserFile userFile = new UserFile();
                                        userFile.setFilename(uploadedDriveFile.getName());
                                        userFile.setDriveFileId(uploadedDriveFile.getId());
                                        userFile.setUserId(user.getId());
                                        userFile.setUploadedAt(LocalDateTime.now());
                                        userFile.setSize(uploadedDriveFile.getSize()); // Use size from Drive API response
                                        userFile.setContentType(uploadedDriveFile.getMimeType());
                                        userFile.setWebViewLink(uploadedDriveFile.getWebViewLink());

                                        // Update user's current drive usage
                                        user.setCurrentDriveUsageBytes(user.getCurrentDriveUsageBytes() + uploadedDriveFile.getSize());

                                        return Mono.zip(
                                                userRepository.save(user),
                                                userFileRepository.save(userFile)
                                        ).map(tuple -> {
                                            User updatedUser = tuple.getT1();
                                            log.info("DriveService: File '{}' uploaded (Drive ID: {}) and user usage updated for {}. New usage: {} bytes.",
                                                    originalFileName, uploadedDriveFile.getId(), email, updatedUser.getCurrentDriveUsageBytes());
                                            return new FileUploadResponse(
                                                    true,
                                                    "File uploaded successfully. Storage updated.",
                                                    originalFileName,
                                                    uploadedDriveFile.getId(),
                                                    fileMimeType,
                                                    driveUtility.bytesToMegabytes(updatedUser.getCurrentDriveUsageBytes()),
                                                    driveUtility.bytesToMegabytes(driveProperties.getMaxUserSpaceBytes())
                                            );
                                        }).onErrorResume(dbError -> {
                                            log.error("DriveService: CRITICAL: File uploaded to Drive but failed to save user/file metadata in DB for {}: {}. Drive File ID: {}",
                                                    email, dbError.getMessage(), uploadedDriveFile.getId(), dbError);
                                            return Mono.just(new FileUploadResponse(
                                                    false,
                                                    "File uploaded, but failed to update database records. Please contact support. Drive File ID: " + uploadedDriveFile.getId(),
                                                    originalFileName,
                                                    uploadedDriveFile.getId(),
                                                    fileMimeType,
                                                    driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                                    driveUtility.bytesToMegabytes(driveProperties.getMaxUserSpaceBytes())
                                            ));
                                        });
                                    })
                                    .onErrorResume(Exception.class, e -> {
                                        log.error("DriveService: Failed to upload file '{}' for user {}: {}", originalFileName, email, e.getMessage(), e);
                                        return Mono.just(new FileUploadResponse(
                                                false,
                                                "Failed to upload file to Google Drive: " + e.getMessage(),
                                                originalFileName, null, fileMimeType,
                                                driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                                driveUtility.bytesToMegabytes(driveProperties.getMaxUserSpaceBytes())
                                        ));
                                    })
                    );
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found for file upload operation (switchIfEmpty).", email);
                    return Mono.just(new FileUploadResponse(
                            false, "User not found for file upload operation.", null, null, null, 0.0, 0.0));
                }));
    }

    /**
     * Retrieves a list of file details for a specific user from a given Google Drive folder.
     * This method fetches file metadata from Google Drive and enriches it with application-specific
     * details (like MongoDB ID if available) by querying the local database.
     *
     * @param email The email of the user whose files are to be listed.
     * @param folderId The Google Drive ID of the specific folder (e.g., user's "docs" folder ID).
     * @return A Mono emitting a FileListResponse containing the file details or an error message.
     */
    public Mono<FileListResponse> getAllFileDetails(String email, String folderId) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes(); // Get from DriveProperties

                    if (user == null || user.getDriveFolderId() == null || user.getDriveFolderId().isEmpty()) {
                        log.warn("DriveService: User {} or their root Drive folder ID not found. Cannot list files.", email);
                        return Mono.just(new FileListResponse(
                                false, "User or user's Drive folder not found.",
                                Collections.emptyList(), 0.0, driveUtility.bytesToMegabytes(maxUserSpaceBytes)));
                    }

                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> {
                                log.info("DriveService: Attempting to list files for user '{}' in provided folder ID '{}'", email, folderId);
                                try {
                                    // Fetch files from Google Drive using DriveUtility
                                    List<File> driveFiles = driveUtility.listFilesFromDrive(driveInstance, folderId);

                                    // Fetch all user files from the database to avoid blocking calls inside the loop
                                    List<UserFile> userFileRecords = userFileRepository.findByUserId(user.getId()).collectList().block(); // Still blocking here for simplicity
                                    Map<String, String> driveFileIdToMongoIdMap = userFileRecords.stream()
                                            .collect(Collectors.toMap(UserFile::getDriveFileId, UserFile::getId));


                                    List<FileDetail> fileDetails = new ArrayList<>();
                                    for (File driveFile : driveFiles) {
                                        String uploadedAtString = null;
                                        if (driveFile.getCreatedTime() != null) {
                                            long milliseconds = driveFile.getCreatedTime().getValue();
                                            uploadedAtString = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault()).toString();
                                        }

                                        String mongoId = driveFileIdToMongoIdMap.get(driveFile.getId()); // Get MongoDB ID from pre-fetched map

                                        fileDetails.add(new FileDetail(
                                                mongoId,
                                                driveFile.getId(),
                                                driveFile.getName(),
                                                driveFile.getMimeType(),
                                                driveFile.getSize() != null ? driveFile.getSize() : 0L,
                                                uploadedAtString,
                                                driveFile.getWebViewLink(),
                                                driveFile.getThumbnailLink()
                                        ));
                                    }
                                    log.info("DriveService: Found {} files for user {} in folder {}.", fileDetails.size(), email, folderId);
                                    return new FileListResponse(true, "Files retrieved successfully.", fileDetails,
                                            driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                            driveUtility.bytesToMegabytes(maxUserSpaceBytes));

                                } catch (Exception e) {
                                    log.error("DriveService: Failed to list files for user {} in folder {}: {}", email, folderId, e.getMessage(), e);
                                    throw e;
                                }
                            }).subscribeOn(Schedulers.boundedElastic())
                    )
                    .onErrorResume(Exception.class, e -> {
                        return Mono.just(new FileListResponse(false, "Failed to retrieve files: " + e.getMessage(),
                                Collections.emptyList(), driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                driveUtility.bytesToMegabytes(maxUserSpaceBytes)));
                    });
                })
                .switchIfEmpty(Mono.just(new FileListResponse(
                        false, "User not found for file listing operation.",
                        Collections.emptyList(), 0.0, driveUtility.bytesToMegabytes(driveProperties.getMaxUserSpaceBytes())))); // Get from DriveProperties
    }

    /**
     * Deletes a specific file from Google Drive and updates the user's storage quota
     * in the application's database. It performs checks to ensure the file belongs
     * to the user's recognized Drive space before deletion.
     *
     * @param email The email of the user requesting file deletion.
     * @param fileId The Google Drive ID of the file to be deleted.
     * @return A Mono emitting a DeleteResponse indicating the outcome of the deletion.
     */
    public Mono<DeleteResponse> deleteFile(String email, String fileId) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes(); // Get from DriveProperties
                    if (user == null) {
                        log.warn("DriveService: User {} not found for file deletion (ID: {}).", email, fileId);
                        return Mono.just(new DeleteResponse(
                                false, "User not found for file deletion.", fileId, 0L, 0.0, 0.0));
                    }

                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> {
                                log.info("DriveService: Attempting to get file details for deletion: File ID '{}' for user '{}'", fileId, email);
                                // Fetch file metadata from Drive to get its parents and size
                                File driveFile = driveUtility.getDriveFileMetadata(driveInstance, fileId, "parents,size");

                                if (driveFile == null) {
                                    log.warn("DriveService: File with ID '{}' not found on Drive for user {}.", fileId, email);
                                    throw new IOException("File not found on Google Drive.");
                                }

                                String userRootFolderId = user.getDriveFolderId();
                                // Dynamically retrieve subfolder IDs to verify parentage
                                List<String> userFolderAndSubfolderIds = new ArrayList<>();
                                userFolderAndSubfolderIds.add(userRootFolderId);
                                String docsFolderId = driveUtility.getSubfolderId(driveInstance, userRootFolderId, "docs");
                                if (docsFolderId != null) userFolderAndSubfolderIds.add(docsFolderId);
                                String imagesFolderId = driveUtility.getSubfolderId(driveInstance, userRootFolderId, "images");
                                if (imagesFolderId != null) userFolderAndSubfolderIds.add(imagesFolderId);
                                String tasksFolderId = driveUtility.getSubfolderId(driveInstance, userRootFolderId, "tasks");
                                if (tasksFolderId != null) userFolderAndSubfolderIds.add(tasksFolderId);

                                boolean isFileInUserFolders = driveFile.getParents() != null &&
                                        driveFile.getParents().stream().anyMatch(userFolderAndSubfolderIds::contains);

                                if (!isFileInUserFolders) {
                                    log.warn("DriveService: File ID '{}' is not within user {}'s recognized Drive folders. Deletion aborted.", fileId, email);
                                    throw new IOException("File is not accessible for deletion by this user or not in their designated space.");
                                }

                                long deletedFileSize = driveFile.getSize() != null ? driveFile.getSize() : 0L;

                                // Delete the file from Google Drive using DriveUtility
                                driveUtility.deleteDriveFile(driveInstance, fileId);
                                log.info("DriveService: File ID '{}' deleted from Google Drive.", fileId);

                                // Update DB record for user and user file
                                return userFileRepository.findByDriveFileId(fileId)
                                        .flatMap(userFile -> userFileRepository.delete(userFile).thenReturn(userFile.getSize()))
                                        .defaultIfEmpty(deletedFileSize) // If no DB record, use size from Drive API
                                        .flatMap(actualDeletedSize -> {
                                            long newUsage = Math.max(0, user.getCurrentDriveUsageBytes() - actualDeletedSize);
                                            user.setCurrentDriveUsageBytes(newUsage);
                                            return userRepository.save(user)
                                                    .map(updatedUser -> {
                                                        log.info("DriveService: File ID '{}' deleted from DB and user usage updated for {}. New usage: {} bytes.",
                                                                fileId, email, updatedUser.getCurrentDriveUsageBytes());
                                                        return new DeleteResponse(
                                                                true,
                                                                "File deleted successfully. Storage updated.",
                                                                fileId,
                                                                actualDeletedSize,
                                                                driveUtility.bytesToMegabytes(updatedUser.getCurrentDriveUsageBytes()),
                                                                driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                                                        );
                                                    });
                                        });
                            }).subscribeOn(Schedulers.boundedElastic())
                                    // Flatten the Mono<Mono<DeleteResponse>> to Mono<DeleteResponse>
                                    .flatMap(monoResponse -> monoResponse)
                                    .onErrorResume(e -> {
                                        log.error("DriveService: Failed to delete file ID '{}' for user {}: {}", fileId, email, e.getMessage(), e);
                                        return Mono.just(new DeleteResponse(
                                                false,
                                                "Failed to delete file from Google Drive: " + e.getMessage(),
                                                fileId, 0L, driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                                driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                                        ));
                                    })
                    );
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found for file delete operation (file ID: {}).", email, fileId);
                    return Mono.just(new DeleteResponse(
                            false, "User not found for file deletion operation.", fileId, 0L, 0.0, 0.0));
                }));
    }
}