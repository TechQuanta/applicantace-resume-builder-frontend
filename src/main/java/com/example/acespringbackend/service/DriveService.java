package com.example.acespringbackend.service;

import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;
import com.example.acespringbackend.auth.dto.FileUploadResponse;
import com.example.acespringbackend.model.UserFile;
import com.example.acespringbackend.repository.UserFileRepository;
import com.example.acespringbackend.utility.DriveUtility;
import com.example.acespringbackend.utility.MimeTypeMap;
import com.example.acespringbackend.auth.dto.FileDetail;
import com.example.acespringbackend.auth.dto.DeleteResponse;
import com.example.acespringbackend.auth.dto.FileListResponse;
import com.example.acespringbackend.auth.dto.TemplateReplicationResponse;
import com.example.acespringbackend.auth.dto.DownloadResult;
import com.example.acespringbackend.auth.dto.FileRenameResponse;
import com.example.acespringbackend.auth.dto.PermissionUpdateResponse;
import com.example.acespringbackend.auth.dto.TemplateReplicationRequest;
import com.example.acespringbackend.auth.dto.FileExportResponse;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import jakarta.mail.MessagingException;

import org.springframework.stereotype.Service;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.core.io.buffer.DataBufferUtils;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
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
    private final DriveUtility driveUtility;
    private final DriveProperties driveProperties;
    private final EmailService emailService; // Assuming EmailService exists

    /**
     * Constructor for DriveService, injecting necessary repositories, DriveUtility, and DriveProperties.
     *
     * @param userRepository The repository for User entities.
     * @param userFileRepository The repository for UserFile entities.
     * @param driveUtility The utility class for Google Drive operations.
     * @param driveProperties The configuration properties related to Google Drive.
     * @param emailService The service for sending emails (e.g., for notifications).
     */
    public DriveService(UserRepository userRepository, UserFileRepository userFileRepository, DriveUtility driveUtility, DriveProperties driveProperties, EmailService emailService) {
        this.userRepository = userRepository;
        this.userFileRepository = userFileRepository;
        this.driveUtility = driveUtility;
        this.driveProperties = driveProperties;
        this.emailService = emailService;
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
                String masterFolderId = driveProperties.getMasterFolderId();
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
                throw e;
            }
        }).subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * Uploads a file to a specified Google Drive folder for a given user.
     * This method includes robust quota checks (individual file size and total user usage)
     * before initiating the upload and persists the file metadata in the application's database.
     *
     * @param email The email of the user attempting to upload the file.
     * @param filePart The FilePart object containing the file's content and metadata.
     * @param targetFolderId The Google Drive ID of the specific folder where the file should be uploaded.
     * @return A Mono emitting a FileUploadResponse that indicates success or failure,
     * along with relevant file and quota details.
     */
    public Mono<FileUploadResponse> uploadFile(String email, FilePart filePart, String targetFolderId) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (user == null) {
                        log.error("DriveService: User with email {} not found for file upload.", email);
                        return Mono.just(new FileUploadResponse(
                                false, "User not found for file upload operation.", null, null, null, 0.0, 0.0));
                    }

                    String originalFileName = filePart.filename();
                    String fileMimeType = filePart.headers().getContentType() != null ? filePart.headers().getContentType().toString() : MimeTypeMap.getDefaultExtensionFromMimeType(originalFileName.substring(originalFileName.lastIndexOf('.') + 1));

                    long maxIndividualFileSizeBytes = driveProperties.getMaxIndividualFileSizeBytes();
                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes();

                    if (originalFileName == null || originalFileName.isEmpty()) {
                        return Mono.just(new FileUploadResponse(
                                false,
                                "Cannot upload file without a filename.",
                                null, null, null,
                                driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
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

                    // Read the content of the FilePart to perform size check and then upload
                    return DataBufferUtils.join(filePart.content())
                            .flatMap(dataBuffer -> {
                                byte[] fileBytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(fileBytes);
                                DataBufferUtils.release(dataBuffer); // Release the data buffer

                                long incomingFileSize = fileBytes.length;

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

                                return driveUtility.getDriveInstance().flatMap(driveInstance ->
                                        Mono.fromCallable(() -> {
                                            log.info("DriveService: Initiating upload of file '{}' for user '{}' into folder ID: '{}'", originalFileName, email, targetFolderId);
                                            return driveUtility.uploadFileToDrive(driveInstance, originalFileName, fileMimeType, fileBytes, targetFolderId);
                                        }).subscribeOn(Schedulers.boundedElastic())
                                                .flatMap(uploadedDriveFile -> {
                                                    UserFile userFile = new UserFile();
                                                    userFile.setFilename(uploadedDriveFile.getName());
                                                    userFile.setDriveFileId(uploadedDriveFile.getId());
                                                    userFile.setUserId(user.getId());
                                                    userFile.setUploadedAt(LocalDateTime.now());
                                                    userFile.setSize(uploadedDriveFile.getSize());
                                                    userFile.setMimeType(uploadedDriveFile.getMimeType());
                                                    userFile.setWebViewLink(uploadedDriveFile.getWebViewLink());

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
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found for file upload operation (switchIfEmpty).", email);
                    return Mono.just(new FileUploadResponse(
                            false, "User not found for file upload operation.", null, null, null, 0.0, 0.0));
                }));
    }

    /**
     * Replicates a template file from a master location into a user's specific Drive folder.
     * Grants the user edit permissions on the newly created replica.
     *
     * @param request The TemplateReplicationRequest containing user email, master template ID,
     * new filename, target Drive folder ID, and template metadata.
     * @return A Mono emitting a TemplateReplicationResponse with details of the replicated file.
     */
    public Mono<TemplateReplicationResponse> replicateTemplateFile(TemplateReplicationRequest request) {
        String userEmail = request.getUserEmail();
        String drive_id = request.getDrive_id(); // Master template ID
        String newFileName = request.getNewFileName();
        String targetDriveFolderId = request.getTargetDriveFolderId(); // User's specific folder ID from DTO

        // New template metadata from the request
        String templateName = request.getName();
        String templateCategory = request.getCategory();
        String templateImageUrl = request.getImage_url();
        String templateDescription = request.getDescription(); // Corrected getter if DTO changed
        String templateSpotlight = request.getSpotlight();
        String requestTemplateProvider = request.getProvider(); // Get provider from request

        // --- Set default template provider if not provided in the request ---
        final String effectiveTemplateProvider = (requestTemplateProvider == null || requestTemplateProvider.isEmpty())
                                                 ? "APPLICANTACE"
                                                 : requestTemplateProvider;
        // --- End of default provider logic ---


        return userRepository.findByEmail(userEmail)
                .flatMap(user -> {
                    if (user == null) {
                        log.error("DriveService: User with email {} not found for template replication.", userEmail);
                        return Mono.just(new TemplateReplicationResponse(
                                false, "User not found for template replication.", null, null, null, null, 0.0, 0.0, null));
                    }

                    String userFolderId = targetDriveFolderId;

                    if (userFolderId == null || userFolderId.isEmpty()) {
                        log.error("DriveService: Target Drive folder ID is missing or invalid for user {}.", userEmail);
                        return Mono.just(new TemplateReplicationResponse(
                                false, "User's Drive folder not found or not provided. Cannot replicate template.", null, null, null, null,
                                driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                driveUtility.bytesToMegabytes(driveProperties.getMaxUserSpaceBytes()), null));
                    }

                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes();

                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> { // Schedulers.boundedElastic() will execute this blocking call
                                log.info("DriveService: Replicating template ID '{}' (Name: {}) for user '{}' into folder '{}' with new name '{}'.",
                                        drive_id, templateName, userEmail, userFolderId, newFileName);
                                log.info("DriveService: Attempting to set templateProvider to: '{}' for file: '{}'.", effectiveTemplateProvider, newFileName);


                                // 1. Copy the template file
                                File copiedFile = driveUtility.copyFileToDrive(driveInstance, drive_id, newFileName, userFolderId);
                                if (copiedFile == null || copiedFile.getId() == null) {
                                    throw new IOException("Failed to copy template file to Google Drive.");
                                }
                                log.info("DriveService: Template '{}' replicated with new ID: {}", newFileName, copiedFile.getId());

                                // 2. Set permissions for the user to be a writer on the new file
                                driveUtility.createPermission(driveInstance, copiedFile.getId(), userEmail, "writer", false);
                                log.info("DriveService: Granted 'writer' permission to user '{}' for replicated file ID '{}'.", userEmail, copiedFile.getId());

                                // 3. Store file metadata to application's database (MongoDB)
                                UserFile userFile = new UserFile();
                                userFile.setUserId(user.getId()); // Link to the user document
                                userFile.setDriveFileId(copiedFile.getId());
                                userFile.setFilename(copiedFile.getName());
                                userFile.setMimeType(copiedFile.getMimeType()); // Assuming getMimeType is available
                                userFile.setSize(copiedFile.getSize() != null ? copiedFile.getSize() : 0L);
                                userFile.setWebViewLink(copiedFile.getWebViewLink());
                                userFile.setWebContentLink(copiedFile.getWebContentLink()); // Assuming Drive API provides this
                                userFile.setUploadedAt(LocalDateTime.now());
                                userFile.setLastModified(LocalDateTime.now()); // Set initial last modified time

                                // Store the new template details
                                userFile.setTemplateName(templateName);
                                userFile.setTemplateCategory(templateCategory);
                                userFile.setTemplateImageUrl(templateImageUrl);
                                userFile.setDescription(templateDescription);
                                userFile.setTemplateSpotlight(templateSpotlight);
                                // --- THIS IS THE KEY CHANGE ---
                                userFile.setTemplateProvider(effectiveTemplateProvider);
                                // --- End of key change ---
                                userFile.setOriginalTemplateDriveId(drive_id); // Store original template's ID

                                // Update user's drive usage
                                user.setCurrentDriveUsageBytes(user.getCurrentDriveUsageBytes() + userFile.getSize());

                                log.info("DEBUG_PROVIDER: UserFile object before saving - filename: '{}', provider: '{}'", userFile.getFilename(), userFile.getTemplateProvider());


                                return Mono.zip(
                                        userRepository.save(user), // Save updated user with new usage
                                        userFileRepository.save(userFile) // Save the new user file metadata
                                ).map(tuple -> {
                                    User updatedUser = tuple.getT1();
                                    UserFile savedUserFile = tuple.getT2(); // Capture the saved UserFile
                                    log.info("DriveService: Replicated file metadata and user usage updated for {}. New usage: {} bytes.",
                                            userEmail, updatedUser.getCurrentDriveUsageBytes());
                                    log.info("DEBUG_PROVIDER: UserFile saved successfully to DB. ID: '{}', DriveFileId: '{}', Provider: '{}'",
                                             savedUserFile.getId(), savedUserFile.getDriveFileId(), savedUserFile.getTemplateProvider());


                                    return new TemplateReplicationResponse(
                                            true,
                                            "Template replicated and permissioned successfully.",
                                            copiedFile.getName(),
                                            copiedFile.getId(),
                                            copiedFile.getMimeType(),
                                            copiedFile.getWebViewLink(),
                                            driveUtility.bytesToMegabytes(updatedUser.getCurrentDriveUsageBytes()),
                                            driveUtility.bytesToMegabytes(maxUserSpaceBytes),
                                            savedUserFile.getTemplateProvider() // Pass the actual provider from the saved object
                                    );
                                }).onErrorResume(dbError -> {
                                    log.error("DriveService: CRITICAL: Template replicated and permissioned in Drive, but failed to save user/file metadata in DB for {}: {}. Drive File ID: {}",
                                            userEmail, dbError.getMessage(), copiedFile.getId(), dbError);
                                    return Mono.just(new TemplateReplicationResponse(
                                            false,
                                            "Template replicated to Drive, but failed to update internal database records. Please contact support. Drive File ID: " + copiedFile.getId(),
                                            copiedFile.getName(),
                                            copiedFile.getId(),
                                            copiedFile.getMimeType(),
                                            copiedFile.getWebViewLink(),
                                            driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                            driveUtility.bytesToMegabytes(maxUserSpaceBytes),
                                            effectiveTemplateProvider // Use the effective provider if DB save failed
                                    ));
                                });
                            }).subscribeOn(Schedulers.boundedElastic())
                                    .flatMap(monoResponse -> monoResponse)
                                    .onErrorResume(Exception.class, e -> {
                                        log.error("DriveService: Failed to replicate template ID '{}' for user {}: {}", drive_id, userEmail, e.getMessage(), e);
                                        return Mono.just(new TemplateReplicationResponse(
                                                false,
                                                "Failed to replicate template from Google Drive: " + e.getMessage(),
                                                newFileName, null, null, null,
                                                driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                                driveUtility.bytesToMegabytes(maxUserSpaceBytes),
                                                effectiveTemplateProvider
                                        ));
                                    })
                    );
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found for template replication (switchIfEmpty).", userEmail);
                    return Mono.just(new TemplateReplicationResponse(
                            false, "User not found for template replication operation.", null, null, null, null, 0.0, 0.0, null));
                }));
    }
    /**
     * Retrieves a list of file details for a specific user from a given Google Drive folder.
     * This method fetches file metadata from Google Drive and enriches it with application-specific
     * details (like MongoDB ID if available) by querying the local database.
     *
     * @param email The email of the user whose files are to be listed.
     * @param folderId The Google Drive ID of the specific folder (e.g., user's "docs" folder ID).
     * NOTE: The current implementation primarily fetches from MongoDB based on user ID
     * and does not filter by `folderId` from Drive. If `folderId` filtering from Drive
     * is needed, additional Drive API calls and logic would be required here.
     * @return A Mono emitting a FileListResponse containing the file details or an error message.
     */
    public Mono<FileListResponse> getAllFileDetails(String email, String folderId) { // folderId is still not used for Drive API filtering
        // Convert the input email to lowercase for consistent querying.
        // This is crucial to avoid case-sensitivity issues in MongoDB queries.
        String lowercaseEmail = email.toLowerCase();
        log.info("DriveService: Method entry - getAllFileDetails for user email: '{}' (normalized: '{}').", email, lowercaseEmail);

        // Step 1: Find the User by email to get quota information.
        // This is done first because the response needs user-specific quota.
        log.debug("DriveService: Attempting to find user by email: '{}' in UserRepository for quota info.", lowercaseEmail);
        return userRepository.findByEmail(lowercaseEmail) // Assuming UserRepository queries by 'email' field in User model
                .flatMap(user -> {
                    log.info("DriveService: User found: '{}'.", user.getEmail());
                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes();
                    log.debug("DriveService: Max user space bytes configured: {} MB ({} bytes).",
                            driveUtility.bytesToMegabytes(maxUserSpaceBytes), maxUserSpaceBytes);

                    // If user is null, it means no user found for the email
                    if (user == null) {
                        log.warn("DriveService: User object is null after findByEmail for '{}'. This should ideally not happen if flatMap is entered.", lowercaseEmail);
                        return Mono.just(new FileListResponse(
                                false, "User not found.",
                                Collections.emptyList(), 0.0, driveUtility.bytesToMegabytes(maxUserSpaceBytes)));
                    }

                    log.info("DriveService: Proceeding to fetch all UserFile records from MongoDB directly by their 'email' field: '{}'.", lowercaseEmail);

                    // Step 2: Retrieve all UserFile records for the user directly by the 'email' field from MongoDB.
                    // findByEmail returns Flux<UserFile>. collectList() turns it into Mono<List<UserFile>>.
                    log.debug("DriveService: Calling userFileRepository.findByEmail({}) and collecting to list.", lowercaseEmail);
                    return userFileRepository.findByEmail(lowercaseEmail) // <--- CRITICAL: Now using findByEmail
                            .collectList()
                            .flatMap(userFiles -> {
                                log.info("DriveService: Successfully retrieved {} UserFile records from MongoDB for user email: '{}'.", userFiles.size(), lowercaseEmail);
                                log.debug("DriveService: Starting transformation of UserFile records to FileDetail DTOs.");
                                List<FileDetail> fileDetails = new ArrayList<>();

                                for (UserFile userFile : userFiles) {
                                    log.debug("DriveService: Processing UserFile - MongoDB ID: '{}', DriveFileId: '{}', Filename: '{}', MimeType: '{}', Size: {} bytes, Provider: '{}'.",
                                            userFile.getId(), userFile.getDriveFileId(), userFile.getFilename(),
                                            userFile.getMimeType(), userFile.getSize(), userFile.getTemplateProvider());

                                    String uploadedAtString = null;
                                    if (userFile.getUploadedAt() != null) {
                                        uploadedAtString = userFile.getUploadedAt().atZone(ZoneId.systemDefault()).toInstant().toString();
                                        log.debug("DriveService: UserFile uploadedAt (LocalDateTime): '{}', Converted to String: '{}'.", userFile.getUploadedAt(), uploadedAtString);
                                    } else {
                                        log.debug("DriveService: UserFile uploadedAt is null for file '{}'.", userFile.getDriveFileId());
                                    }

                                    String embedLink = null;
                                    String mimeType = userFile.getMimeType();
                                    String webViewLink = userFile.getWebViewLink();

                                    log.debug("DriveService: Determining embedLink for file '{}'. WebViewLink: '{}', MimeType: '{}'.", userFile.getFilename(), webViewLink, mimeType);
                                    if (webViewLink != null) {
                                        if (mimeType != null && mimeType.startsWith("application/vnd.google-apps.")) {
                                            embedLink = webViewLink;
                                            log.debug("DriveService: Google Workspace file ({}). Using webViewLink as embedLink: '{}'.", mimeType, embedLink);
                                        } else {
                                            embedLink = webViewLink
                                                    .replace("/view?usp=drivesdk", "/preview")
                                                    .replace("/view", "/preview");
                                            log.debug("DriveService: Non-Google Workspace file ({}). Transformed webViewLink to embedLink: '{}'.", mimeType, embedLink);
                                        }
                                    } else {
                                        log.warn("DriveService: No webViewLink found in MongoDB record for file ID: '{}'. Cannot generate embedLink for '{}'.", userFile.getDriveFileId(), userFile.getFilename());
                                    }

                                    FileDetail detail = new FileDetail(
                                            userFile.getId(),
                                            userFile.getDriveFileId(),
                                            userFile.getFilename(),
                                            userFile.getMimeType(),
                                            Objects.requireNonNullElse(userFile.getSize(), 0L),
                                            uploadedAtString,
                                            userFile.getWebViewLink(),
                                            null, // webContentLink is not currently used in FileDetail
                                            embedLink,
                                            userFile.getTemplateProvider()
                                    );
                                    fileDetails.add(detail);
                                    log.debug("DriveService: Added FileDetail for '{}'. Details: {}", userFile.getFilename(), detail.toString());
                                }

                                log.info("DriveService: Finished transforming files. Total {} FileDetails prepared for response.", fileDetails.size());
                                log.debug("DriveService: Current Drive Usage for user '{}': {} bytes ({} MB). Max Quota: {} MB.",
                                        user.getEmail(), user.getCurrentDriveUsageBytes(),
                                        driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                        driveUtility.bytesToMegabytes(maxUserSpaceBytes));

                                // Constructing the final FileListResponse
                                FileListResponse response = new FileListResponse(
                                        true, "Files retrieved successfully from MongoDB.", fileDetails,
                                        driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                        driveUtility.bytesToMegabytes(maxUserSpaceBytes));
                                log.info("DriveService: Returning successful FileListResponse. Success: {}, Message: '{}', Files Count: {}.",
                                        response.getSuccess(), response.getMessage(), response.getFiles().size());
                                return Mono.just(response);
                            })
                            // Error handling for MongoDB file retrieval
                            .onErrorResume(Exception.class, e -> {
                                log.error("DriveService: CRITICAL ERROR - Failed to retrieve or process user files from MongoDB for user '{}': {}. Error type: {}. Stack Trace: ",
                                        lowercaseEmail, e.getMessage(), e.getClass().getSimpleName(), e);
                                return Mono.just(new FileListResponse(false, "Failed to retrieve files from database: " + e.getMessage(),
                                        Collections.emptyList(),
                                        driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                        driveUtility.bytesToMegabytes(maxUserSpaceBytes)));
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                })
                // Handle the case where userRepository.findByEmail(email) returns an empty Mono (user not found).
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found by userRepository.findByEmail. Cannot provide quota or file list.", lowercaseEmail);
                    return Mono.just(new FileListResponse(
                            false, "User not found for file listing operation. Cannot retrieve files or quota.",
                            Collections.emptyList(), 0.0, driveUtility.bytesToMegabytes(driveProperties.getMaxUserSpaceBytes())));
                }));
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
                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes();
                    if (user == null) {
                        log.warn("DriveService: User {} not found for file deletion (ID: {}).", email, fileId);
                        return Mono.just(new DeleteResponse(
                                false, "User not found for file deletion.", fileId, 0L, 0.0, 0.0));
                    }

                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> {
                                log.info("DriveService: Attempting to get file details for deletion: File ID '{}' for user '{}'", fileId, email);
                                File driveFile = driveUtility.getDriveFileMetadata(driveInstance, fileId, "parents,size");

                                if (driveFile == null) {
                                    log.warn("DriveService: File with ID '{}' not found on Drive for user {}.", fileId, email);
                                    throw new IOException("File not found on Google Drive.");
                                }

                                String userRootFolderId = user.getDriveFolderId();
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

                                driveUtility.deleteDriveFile(driveInstance, fileId);
                                log.info("DriveService: File ID '{}' deleted from Google Drive.", fileId);

                                return userFileRepository.findByDriveFileId(fileId)
                                        .flatMap(userFile -> userFileRepository.delete(userFile).thenReturn(userFile.getSize()))
                                        .defaultIfEmpty(deletedFileSize) // Use deletedFileSize from Drive if not found in DB
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

    /**
     * Downloads a file's content from Google Drive.
     *
     * @param email The email of the user requesting the download.
     * @param fileId The Google Drive ID of the file to download.
     * @return A Mono emitting a DownloadResult with the file content and metadata.
     */
    public Mono<DownloadResult> downloadFile(String email, String fileId) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (user == null) {
                        log.error("DriveService: User with email {} not found for file download.", email);
                        return Mono.just(new DownloadResult(false, "User not found.", null, null, null));
                    }

                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> {
                                log.info("DriveService: Attempting to download file ID '{}' for user '{}'.", fileId, email);
                                File driveFile = driveUtility.getDriveFileMetadata(driveInstance, fileId, "name,mimeType,parents");

                                if (driveFile == null) {
                                    log.warn("DriveService: File with ID '{}' not found on Drive for user {}.", fileId, email);
                                    throw new IOException("File not found on Google Drive.");
                                }

                                String userRootFolderId = user.getDriveFolderId();
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
                                    log.warn("DriveService: File ID '{}' is not within user {}'s recognized Drive folders. Download aborted.", fileId, email);
                                    throw new IOException("File is not accessible for download by this user or not in their designated space.");
                                }

                                byte[] fileContent = driveUtility.downloadFileContent(driveInstance, fileId);

                                log.info("DriveService: File ID '{}' content downloaded successfully for user {}.", fileId, email);
                                return new DownloadResult(true, "File downloaded successfully.",
                                        fileContent, driveFile.getName(), driveFile.getMimeType());
                            }).subscribeOn(Schedulers.boundedElastic())
                    )
                            .onErrorResume(Exception.class, e -> {
                                log.error("DriveService: Failed to download file ID '{}' for user {}: {}", fileId, email, e.getMessage(), e);
                                return Mono.just(new DownloadResult(false, "Failed to download file from Google Drive: " + e.getMessage(), null, null, null));
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found for file download (switchIfEmpty).", email);
                    return Mono.just(new DownloadResult(false, "User not found for file download operation.", null, null, null));
                }));
    }

    /**
     * Renames a file in Google Drive and updates its metadata in the application's database.
     *
     * @param userEmail The email of the user who owns the file.
     * @param fileId The Google Drive ID of the file to rename.
     * @param newFileName The new name for the file.
     * @return A Mono emitting a FileRenameResponse indicating success or failure.
     */
    public Mono<FileRenameResponse> renameFile(String userEmail, String fileId, String newFileName) {
        return userRepository.findByEmail(userEmail)
                .flatMap(user -> {
                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes();
                    if (user == null) {
                        log.warn("DriveService: User {} not found for file rename operation (ID: {}).", userEmail, fileId);
                        return Mono.just(new FileRenameResponse(false, "User not found for file rename.", null, null, null, 0.0, 0.0));
                    }

                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> {
                                log.info("DriveService: Attempting to rename file ID '{}' to '{}' for user '{}'.", fileId, newFileName, userEmail);
                                // Get metadata to verify ownership and existing name
                                File driveFile = driveUtility.getDriveFileMetadata(driveInstance, fileId, "name,parents,webViewLink");

                                if (driveFile == null) {
                                    log.warn("DriveService: File with ID '{}' not found on Drive for user {}.", fileId, userEmail);
                                    throw new IOException("File not found on Google Drive.");
                                }

                                // Verify file belongs to the user's recognized folders
                                String userRootFolderId = user.getDriveFolderId();
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
                                    log.warn("DriveService: File ID '{}' is not within user {}'s recognized Drive folders. Rename aborted.", fileId, userEmail);
                                    throw new IOException("File is not accessible for rename by this user or not in their designated space.");
                                }

                                // Perform rename using DriveUtility
                                File renamedFile = driveUtility.renameDriveFile(driveInstance, fileId, newFileName);

                                // Update DB record for UserFile
                                return userFileRepository.findByDriveFileId(fileId)
                                        .flatMap(userFile -> {
                                            userFile.setFilename(renamedFile.getName());
                                            userFile.setWebViewLink(renamedFile.getWebViewLink()); // Link might change if name is part of it
                                            return userFileRepository.save(userFile);
                                        })
                                        .map(updatedUserFile -> {
                                            log.info("DriveService: File ID '{}' renamed in DB for user {}.", fileId, userEmail);
                                            return new FileRenameResponse(
                                                    true,
                                                    "File renamed successfully.",
                                                    updatedUserFile.getFilename(),
                                                    updatedUserFile.getDriveFileId(),
                                                    updatedUserFile.getWebViewLink(),
                                                    driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                                    driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                                            );
                                        })
                                        .switchIfEmpty(Mono.error(new IOException("File metadata not found in database for update after Drive rename.")));
                            }).subscribeOn(Schedulers.boundedElastic())
                                    .flatMap(monoResponse -> monoResponse) // Flatten Mono<Mono<FileRenameResponse>>
                                    .onErrorResume(Exception.class, e -> {
                                        log.error("DriveService: Failed to rename file ID '{}' for user {}: {}", fileId, userEmail, e.getMessage(), e);
                                        return Mono.just(new FileRenameResponse(
                                                false,
                                                "Failed to rename file: " + e.getMessage(),
                                                null, fileId, null,
                                                driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                                driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                                        ));
                                    })
                    );
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found for file rename operation (switchIfEmpty).", userEmail);
                    return Mono.just(new FileRenameResponse(false, "User not found for file rename operation.", null, null, null, 0.0, 0.0));
                }));
    }

    /**
     * Updates permissions for a specific file in Google Drive.
     *
     * @param userEmail The email of the user performing the action (must have sufficient permissions).
     * @param fileId The Google Drive ID of the file to update permissions for.
     * @param targetEmail The email of the user whose permission is being updated.
     * @param role The new role to assign ("reader", "writer", "commenter").
     * @param action The action to perform: "add" or "remove".
     * @return A Mono emitting a PermissionUpdateResponse indicating success or failure.
     */
    public Mono<PermissionUpdateResponse> updateFilePermission(String userEmail, String fileId, String targetEmail, String role, String action) {
        return userRepository.findByEmail(userEmail)
                .flatMap(user -> {
                    // Check if the acting user exists
                    if (user == null) {
                        log.warn("DriveService: User {} not found for permission update operation.", userEmail);
                        return Mono.just(new PermissionUpdateResponse(false, "User not found for permission update."));
                    }

                    // Get Google Drive instance and perform file operations
                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> { // Use Mono.fromCallable for blocking operations
                                log.info("DriveService: Attempting to {} permission for file ID '{}' for target '{}' with role '{}' by user '{}'.", action, fileId, targetEmail, role, userEmail);

                                // 1. Verify the file exists and is accessible by the acting user
                                File driveFile = driveUtility.getDriveFileMetadata(driveInstance, fileId, "name,parents");
                                if (driveFile == null) {
                                    log.warn("DriveService: File with ID '{}' not found on Drive for user {}.", fileId, userEmail);
                                    throw new IOException("File not found on Google Drive.");
                                }

                                // 2. Verify file belongs to the acting user's recognized folders
                                String userRootFolderId = user.getDriveFolderId();
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
                                    log.warn("DriveService: File ID '{}' is not within user {}'s recognized Drive folders. Permission update aborted.", fileId, userEmail);
                                    throw new IOException("File is not accessible for permission update by this user or not in their designated space.");
                                }

                                if ("add".equalsIgnoreCase(action)) {
                                    driveUtility.createPermission(driveInstance, fileId, targetEmail, role, false);
                                    log.info("DriveService: Granted '{}' permission to user '{}' for file ID '{}'.", role, targetEmail, fileId);
                                    return new PermissionUpdateResponse(true, String.format("Permission '%s' granted to %s for file %s.", role, targetEmail, driveFile.getName()));
                                } else if ("remove".equalsIgnoreCase(action)) {
                                    boolean removed = driveUtility.deletePermission(driveInstance, fileId, targetEmail, role);
                                    if (removed) {
                                        log.info("DriveService: Removed '{}' permission from user '{}' for file ID '{}'.", role, targetEmail, fileId);
                                        return new PermissionUpdateResponse(true, String.format("Permission '%s' removed from %s for file %s.", role, targetEmail, driveFile.getName()));
                                    } else {
                                        log.warn("DriveService: Permission for target '{}' with role '{}' not found or could not be removed from file ID '{}'.", targetEmail, role, fileId);
                                        throw new IOException(String.format("Permission for %s with role %s not found or could not be removed.", targetEmail, role));
                                    }
                                } else {
                                    throw new IllegalArgumentException("Invalid action specified for permission update: " + action + ". Must be 'add' or 'remove'.");
                                }
                            }).subscribeOn(Schedulers.boundedElastic())
                    )
                            .onErrorResume(Exception.class, e -> {
                                log.error("DriveService: Failed to update permission for file ID '{}' for user {}: {}", fileId, userEmail, e.getMessage(), e);
                                return Mono.just(new PermissionUpdateResponse(false, "Failed to update permission: " + e.getMessage()));
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found for permission update operation (switchIfEmpty).", userEmail);
                    return Mono.just(new PermissionUpdateResponse(false, "User not found for permission update operation."));
                }));
    }

    /**
     * Exports/converts a Google Drive native file (Docs, Sheets, Slides) to a specified MIME type.
     * For non-native files, it simply returns the content as is if the MIME type matches or is a general download.
     *
     * @param userEmail The email of the user requesting the export.
     * @param fileId The Google Drive ID of the file to export.
     * @param exportMimeType The target MIME type for conversion (e.g., "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document").
     * @param newFileName An optional desired filename for the exported file.
     * @return A Mono emitting a FileExportResponse with the exported file's content and metadata.
     */
    public Mono<FileExportResponse> exportFile(String userEmail, String fileId, String exportMimeType, String newFileName) {
        return userRepository.findByEmail(userEmail)
                .flatMap(user -> {
                    long maxUserSpaceBytes = driveProperties.getMaxUserSpaceBytes();
                    if (user == null) {
                        log.error("DriveService: User with email {} not found for file export.", userEmail);
                        return Mono.just(new FileExportResponse(false, "User not found.", null, null, null, 0.0, 0.0));
                    }

                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> {
                                log.info("DriveService: Attempting to export file ID '{}' to MIME type '{}' for user '{}'.", fileId, exportMimeType, userEmail);

                                File driveFile = driveUtility.getDriveFileMetadata(driveInstance, fileId, "name,mimeType,parents");
                                if (driveFile == null) {
                                    log.warn("DriveService: File with ID '{}' not found on Drive for user {}.", fileId, userEmail);
                                    throw new IOException("File not found on Google Drive.");
                                }

                                // Verify file belongs to the user's recognized folders
                                String userRootFolderId = user.getDriveFolderId();
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
                                    log.warn("DriveService: File ID '{}' is not within user {}'s recognized Drive folders. Export aborted.", fileId, userEmail);
                                    throw new IOException("File is not accessible for export by this user or not in their designated space.");
                                }

                                byte[] exportedContent;
                                String finalFileName = (newFileName != null && !newFileName.isEmpty()) ? newFileName : driveFile.getName();
                                String finalMimeType = exportMimeType;

                                // Handle native Google Workspace files (Docs, Sheets, Slides) for export
                                if (MimeTypeMap.isGoogleAppsMimeType(driveFile.getMimeType())) {
                                    log.info("DriveService: Exporting Google Workspace native file ID '{}' to '{}'.", fileId, exportMimeType);
                                    exportedContent = driveUtility.exportGoogleDriveFile(driveInstance, fileId, exportMimeType);
                                    // Append correct extension to filename
                                    if (finalFileName.lastIndexOf('.') > 0) {
                                        finalFileName = finalFileName.substring(0, finalFileName.lastIndexOf('.'));
                                    }
                                    finalFileName += "." + MimeTypeMap.getDefaultExtensionFromMimeType(exportMimeType);

                                } else {
                                    // For non-native files, just download them
                                    log.info("DriveService: Downloading non-native file ID '{}'. No conversion needed.", fileId);
                                    exportedContent = driveUtility.downloadFileContent(driveInstance, fileId);
                                    // Ensure the original mimeType is kept if not a conversion
                                    finalMimeType = driveFile.getMimeType();
                                    // Ensure filename has original extension
                                    if (!finalFileName.contains(".") && driveFile.getName().contains(".")) {
                                        finalFileName = driveFile.getName();
                                    }
                                }

                                if (exportedContent == null || exportedContent.length == 0) {
                                    throw new IOException("Exported file content is empty or conversion failed.");
                                }

                                return new FileExportResponse(
                                        true,
                                        "File exported successfully.",
                                        finalFileName,
                                        finalMimeType,
                                        exportedContent,
                                        driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()), // Usage not changed by export
                                        driveUtility.bytesToMegabytes(maxUserSpaceBytes)
                                );
                            }).subscribeOn(Schedulers.boundedElastic())
                    )
                            .onErrorResume(Exception.class, e -> {
                                log.error("DriveService: Failed to export file ID '{}' for user {}: {}", fileId, userEmail, e.getMessage(), e);
                                String errorMessage = "Failed to export file: " + e.getMessage();
                                if (e.getMessage().contains("Conversion not supported")) {
                                    errorMessage = "File conversion to the requested format is not supported or possible.";
                                } else if (e.getMessage().contains("File not found")) {
                                    errorMessage = "File not found or not accessible.";
                                }
                                return Mono.just(new FileExportResponse(false, errorMessage, null, null, null,
                                        driveUtility.bytesToMegabytes(user.getCurrentDriveUsageBytes()),
                                        driveUtility.bytesToMegabytes(maxUserSpaceBytes)));
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("DriveService: User with email {} not found for file export operation (switchIfEmpty).", userEmail);
                    return Mono.just(new FileExportResponse(false, "User not found for file export operation.", null, null, null, 0.0, 0.0));
                }));
    }

    public Mono<Boolean> deleteUserData(String email) {
        log.info("DriveService: Initiating deletion for user: {}", email);
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (user == null) {
                        log.warn("DriveService: User with email {} not found for deletion. No action taken.", email);
                        return Mono.just(false);
                    }

                    String userId = user.getId();
                    String userEmail = user.getEmail(); // Use the email from the found user object

                    // 1. Find the user's root folder in Google Drive
                    return driveUtility.getDriveInstance().flatMap(driveInstance ->
                            Mono.fromCallable(() -> {
                                log.info("DriveService: Searching for user's root folder for email: {}", userEmail);
                                String masterFolderId = driveProperties.getMasterFolderId();
                                String query = String.format(
                                        "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and '%s' in parents",
                                        userEmail, masterFolderId
                                );
                                FileList existingUserFolders = driveInstance.files().list()
                                        .setQ(query)
                                        .setSpaces("drive")
                                        .setFields("files(id)")
                                        .execute();

                                if (!existingUserFolders.getFiles().isEmpty()) {
                                    return existingUserFolders.getFiles().get(0).getId();
                                }
                                return null; // Folder not found
                            }).subscribeOn(Schedulers.boundedElastic())
                                    .flatMap(userFolderId -> {
                                        Mono<Object> driveDeleteMono = Mono.empty(); // Changed to Mono<Void>
                                        if (userFolderId != null) {
                                            log.info("DriveService: Found user folder '{}' with ID '{}'. Trashing...", userEmail, userFolderId);
                                            driveDeleteMono = driveUtility.deleteDriveFolder(driveInstance, userFolderId)
                                                    .doOnSuccess(v -> log.info("DriveService: User folder '{}' (ID: {}) trashed successfully.", userEmail, userFolderId))
                                                    .onErrorResume(e -> {
                                                        log.error("DriveService: Failed to trash user folder '{}' (ID: {}): {}", userEmail, userFolderId, e.getMessage());
                                                        // For now, we'll log and continue to delete DB records even if Drive fails.
                                                        return Mono.empty(); // Continue with database deletion
                                                    });
                                        } else {
                                            log.warn("DriveService: User folder not found for email {}. Skipping Drive deletion.", userEmail);
                                        }

                                        // 2. Delete all UserFile records for this user
                                        Mono<Void> userFilesDeleteMono = userFileRepository.deleteByUserId(userId)
                                                .doOnSuccess(v -> log.info("DriveService: All UserFile records for user ID {} deleted.", userId))
                                                .onErrorResume(e -> {
                                                    log.error("DriveService: Failed to delete UserFile records for user ID {}: {}", userId, e.getMessage());
                                                    return Mono.empty(); // Continue with user deletion
                                                });

                                        // 3. Delete the User record
                                        Mono<Void> userDeleteMono = userRepository.delete(user)
                                                .doOnSuccess(v -> log.info("DriveService: User record for email {} (ID: {}) deleted.", userEmail, userId))
                                                .onErrorResume(e -> {
                                                    log.error("DriveService: Failed to delete User record for email {} (ID: {}): {}", userEmail, userId, e.getMessage());
                                                    return Mono.empty(); // Final operation, if this fails, we report false.
                                                });

                                        // Combine all deletion operations
                                        return Mono.when(driveDeleteMono, userFilesDeleteMono, userDeleteMono)
                                                .thenReturn(true) // If all combined operations complete, return true
                                                .onErrorResume(e -> {
                                                    log.error("DriveService: An error occurred during combined deletion operations for user {}: {}", userEmail, e.getMessage());
                                                    return Mono.just(false); // If any part of the chain fails, return false
                                                });
                                    })
                    );
                })
                .defaultIfEmpty(false); // If findByEmail returns empty (user not found), return false
    }

}