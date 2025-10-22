package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.DeleteResponse;
import com.example.acespringbackend.auth.dto.DeleteRequest;
import com.example.acespringbackend.auth.dto.FileDetail;
import com.example.acespringbackend.auth.dto.FileListResponse;
import com.example.acespringbackend.auth.dto.ListRequest;
import com.example.acespringbackend.auth.dto.FileUploadResponse;
import com.example.acespringbackend.auth.dto.TemplateReplicationRequest;
import com.example.acespringbackend.auth.dto.TemplateReplicationResponse;
import com.example.acespringbackend.auth.dto.DownloadResult;
import com.example.acespringbackend.auth.dto.FileRenameRequest;
import com.example.acespringbackend.auth.dto.FileRenameResponse;
import com.example.acespringbackend.auth.dto.PermissionUpdateRequest;
import com.example.acespringbackend.auth.dto.PermissionUpdateResponse;
import com.example.acespringbackend.auth.dto.FileExportRequest;
import com.example.acespringbackend.auth.dto.FileExportResponse;
import com.example.acespringbackend.service.DriveService; // Service for Google Drive operations

import jakarta.validation.Valid; // For request body validation

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // For injecting properties from application.properties/yml
import org.springframework.core.io.ByteArrayResource; // For serving byte arrays as resources
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders; // For setting HTTP headers
import org.springframework.http.HttpStatus; // HTTP status codes
import org.springframework.http.MediaType; // Content types
import org.springframework.http.ResponseEntity; // Wrapper for HTTP response
import org.springframework.web.bind.annotation.*; // Spring Web annotations
import org.springframework.http.codec.multipart.FilePart; // For handling multipart file uploads in reactive stack
import reactor.core.publisher.Mono; // Reactive programming type

import java.util.Collections; // Utility for immutable collections
import java.util.List;

/**
 * REST Controller for managing Google Drive operations within the ACE application.
 * This controller provides endpoints for file upload, listing, deletion, download,
 * template replication, renaming, permission updates, and file export.
 * It leverages Spring WebFlux for reactive non-blocking operations and delegates
 * core logic to the {@link DriveService}.
 */
@RestController
@RequestMapping("/ace/drive") // Base path for all Google Drive-related API endpoints.
public class DriveController {

    private static final Logger logger = LoggerFactory.getLogger(DriveController.class);

    private final DriveService driveService;

    // Injects the user drive quota from application properties, defaulting to 10MB if not set.
    @Value("${user.drive.quota.mb:10}")
    private double userDriveQuotaMb;

    /**
     * Constructs the DriveController and injects the {@link DriveService}.
     *
     * @param driveService The service responsible for interacting with Google Drive APIs.
     */
    @Autowired
    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    /**
     * Handles the upload of files to a user's Google Drive folder.
     * This endpoint consumes multipart form data, expecting a file, user email, and folder ID.
     *
     * @param filePart The file content as a {@link FilePart}.
     * @param userEmail The email of the user performing the upload.
     * @param folderId The Google Drive ID of the target folder where the file will be uploaded.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link FileUploadResponse}
     * with details of the upload operation, including success status, messages, and file metadata.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<FileUploadResponse>> uploadFile(
            @RequestPart("file") FilePart filePart,
            @RequestPart("userEmail") String userEmail,
            @RequestPart("folderId") String folderId) {

        logger.info("Received file upload request for folderId: {} from user: {}", folderId, userEmail);

        // Input validation for critical parameters.
        if (filePart == null || filePart.filename().isEmpty()) {
            logger.warn("Received empty file for upload for folderId: {} from user: {}", folderId, userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
                    false, "File is empty or not provided. Please select a non-empty file.",
                    null, null, null, 0.0, 0.0)));
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for upload to folderId: {}", folderId);
            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
                    false, "User email is required for file upload.",
                    null, null, null, 0.0, 0.0)));
        }

        if (folderId == null || folderId.trim().isEmpty()) {
            logger.warn("Folder ID is missing for upload from user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
                    false, "Folder ID is required for file upload.",
                    null, null, null, 0.0, 0.0)));
        }

        logger.info("Successfully received file: {}", filePart.filename());

        // Delegate to DriveService and handle the reactive flow.
        return driveService.uploadFile(userEmail, filePart, folderId)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("File upload successful for folderId {} and user {}. File ID: {}", folderId, userEmail, response.getDriveFileId());
                        return ResponseEntity.ok(response); // Return 200 OK on success.
                    } else {
                        logger.error("File upload failed for folderId {} and user {}: {}", folderId, userEmail, response.getMessage());
                        // Map specific error messages to appropriate HTTP status codes for better client-side handling.
                        if (response.getMessage().contains("User not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("exceeds individual upload limit") ||
                                   response.getMessage().contains("overall storage quota exceeded")) {
                            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response); // 413 Payload Too Large
                        } else if (response.getMessage().contains("Drive folder not initialized") || response.getMessage().contains("Target folder ID for upload is missing")) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Generic 500 for other failures.
                    }
                })
                .onErrorResume(e -> {
                    // Catch-all for unexpected exceptions during the reactive stream processing.
                    logger.error("Controller error during file upload for folderId {} and user {}: {}", folderId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileUploadResponse(
                            false, "An unexpected error occurred during file upload: " + e.getMessage(),
                            null, null, null, 0.0, 0.0)));
                });
    }

    /**
     * Retrieves a list of all files within a specific Google Drive folder for a given user.
     *
     * @param request The {@link ListRequest} containing the user's email and the folder ID.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link FileListResponse}
     * with the list of files, current storage usage, and total quota.
     */
    @PostMapping("/files")
    public Mono<ResponseEntity<FileListResponse>> getAllFiles(
            @RequestBody ListRequest request) {

        String userEmail = request.getUserEmail();
        String folderId = request.getFolderId();

        logger.info("Received POST request to get all files for folderId: {} from user: {}", folderId, userEmail);

        // Input validation.
        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for getting all files for folderId: {}", folderId);
            return Mono.just(ResponseEntity.badRequest().body(new FileListResponse(
                    false, "User email is required to list files.",
                    Collections.emptyList(), 0.0, userDriveQuotaMb)));
        }

        if (folderId == null || folderId.trim().isEmpty()) {
            logger.warn("Folder ID is missing for getting all files from user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new FileListResponse(
                    false, "Folder ID is required to list files.",
                    Collections.emptyList(), 0.0, userDriveQuotaMb)));
        }

        // Delegate to DriveService and handle the reactive flow.
        return driveService.getAllFileDetails(userEmail, folderId)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("Successfully retrieved {} files for folderId {} and user {}",
                                 response.getFiles().size(), folderId, userEmail);
                        return ResponseEntity.ok(response); // Return 200 OK on success.
                    } else {
                        logger.error("Failed to retrieve files for folderId {} and user {}: {}",
                                 folderId, userEmail, response.getMessage());
                        // Map specific error messages to appropriate HTTP status codes.
                        if (response.getMessage().contains("User not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("Drive folder not initialized") || response.getMessage().contains("Target folder ID for listing is missing")) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Generic 500.
                    }
                })
                .onErrorResume(e -> {
                    // Catch-all for unexpected exceptions.
                    logger.error("Controller error during getting all files for folderId {} and user {}: {}", folderId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileListResponse(
                            false, "An unexpected error occurred while listing files: " + e.getMessage(),
                            Collections.emptyList(), 0.0, userDriveQuotaMb)));
                });
    }

    /**
     * Handles the deletion of a file from a user's Google Drive.
     *
     * @param request The {@link DeleteRequest} containing the user's email and the file ID to delete.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link DeleteResponse}
     * indicating the success or failure of the deletion, along with updated storage usage.
     */
    @DeleteMapping("/delete")
    public Mono<ResponseEntity<DeleteResponse>> deleteFile(@RequestBody DeleteRequest request) {
        String userEmail = request.getUserEmail();
        String fileId = request.getFileId();

        logger.info("Received request to delete file ID: {} for user: {}", fileId, userEmail);

        // Input validation.
        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for file deletion for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(new DeleteResponse(
                    false, "User email is required for file deletion.", fileId, 0L, 0.0, 0.0)));
        }

        if (fileId == null || fileId.trim().isEmpty()) {
            logger.warn("File ID is missing for deletion from user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new DeleteResponse(
                    false, "File ID is required for deletion.", null, 0L, 0.0, 0.0)));
        }

        // Delegate to DriveService and handle the reactive flow.
        return driveService.deleteFile(userEmail, fileId)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("File ID {} deleted successfully for user {}. New usage: {} MB.",
                                 fileId, userEmail, response.getCurrentStorageUsageMb());
                        return ResponseEntity.ok(response); // Return 200 OK on success.
                    } else {
                        logger.error("File deletion failed for file ID {} and user {}: {}",
                                 fileId, userEmail, response.getMessage());
                        // Map specific error messages to appropriate HTTP status codes.
                        if (response.getMessage().contains("User not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("File not found") || response.getMessage().contains("not accessible for deletion")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Generic 500.
                    }
                })
                .onErrorResume(e -> {
                    // Catch-all for unexpected exceptions.
                    logger.error("Controller error during file deletion for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DeleteResponse(
                            false, "An unexpected error occurred during file deletion: " + e.getMessage(),
                            fileId, 0L, 0.0, 0.0)));
                });
    }

    /**
     * Handles the download of a specific file from a user's Google Drive.
     * The file content is returned as a {@link ByteArrayResource} with appropriate headers.
     *
     * @param userEmail The email of the user requesting the download.
     * @param fileId The Google Drive ID of the file to download.
     * @return A {@link Mono} of {@link ResponseEntity} that either contains the file content
     * as a {@link ByteArrayResource} with download headers or an error status.
     */
    @GetMapping("/download/{fileId}")
    public Mono<ResponseEntity<? extends Object>> downloadFile(
            @RequestParam String userEmail, // User email passed as a request parameter.
            @PathVariable String fileId) { // File ID extracted from the path.

        logger.info("Received request to download file ID: {} for user: {}", fileId, userEmail);

        // Input validation.
        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for file download for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(null)); // Return 400 Bad Request with empty body.
        }

        if (fileId == null || fileId.trim().isEmpty()) {
            logger.warn("File ID is missing for download from user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(null)); // Return 400 Bad Request with empty body.
        }

        // Delegate to DriveService and handle the reactive flow.
        return driveService.downloadFile(userEmail, fileId)
                .map(downloadResult -> {
                    if (downloadResult.getSuccess()) {
                        logger.info("Successfully prepared file for download: {} (MimeType: {})", downloadResult.getFileName(), downloadResult.getMimeType());
                        // Set necessary HTTP headers for file download.
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadResult.getFileName() + "\"");
                        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(downloadResult.getFileContent().length));
                        return ResponseEntity.ok()
                                .headers(headers)
                                .contentLength(downloadResult.getFileContent().length)
                                .contentType(MediaType.parseMediaType(downloadResult.getMimeType()))
                                .body(new ByteArrayResource(downloadResult.getFileContent())); // Return file content as a resource.
                    } else {
                        logger.error("File download failed for file ID {} and user {}: {}", fileId, userEmail, downloadResult.getMessage());
                        // Map specific error messages to appropriate HTTP status codes.
                        if (downloadResult.getMessage().contains("User not found") || downloadResult.getMessage().contains("File not found") || downloadResult.getMessage().contains("not accessible")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404 Not Found.
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Generic 500.
                    }
                })
                .onErrorResume(e -> {
                    // Catch-all for unexpected exceptions.
                    logger.error("Controller error during file download for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)); // Generic 500.
                });
    }

    /**
     * Handles the replication (copying) of a master template file from Google Drive
     * into a user's specific folder, with a new file name.
     *
     * @param request The {@link TemplateReplicationRequest} containing details like
     * user email, master template ID, new file name, and target folder ID.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link TemplateReplicationResponse}
     * with the new file's details or an error message.
     */
    @PostMapping("/replicate-template")
    public Mono<ResponseEntity<TemplateReplicationResponse>> replicateTemplate(
            @Valid @RequestBody TemplateReplicationRequest request) { // @Valid enables DTO validation.

        String userEmail = request.getUserEmail();
        String drive_id = request.getDrive_id(); // Master template file ID.
        String newFileName = request.getNewFileName();

        // Logging detailed request info.
        logger.info("Received request to replicate template ID: {} (Name: {}) for user: {} with new name: {} in folder: {}",
                     drive_id, request.getName(), userEmail, newFileName, request.getTargetDriveFolderId());

        // Input validation.
        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for template replication.");
            return Mono.just(ResponseEntity.badRequest().body(new TemplateReplicationResponse(
                    false, "User email is required for template replication.", null, null, null, null, 0.0, 0.0, null)));
        }

        if (drive_id == null || drive_id.trim().isEmpty()) {
            logger.warn("Master template file ID (drive_id) is missing for template replication for user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new TemplateReplicationResponse(
                    false, "Master template file ID (drive_id) is required for template replication.", null, null, null, null, 0.0, 0.0, null)));
        }

        if (newFileName == null || newFileName.trim().isEmpty()) {
            logger.warn("New file name is missing for template replication for user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new TemplateReplicationResponse(
                    false, "New file name is required for template replication.", null, null, null, null, 0.0, 0.0, null)));
        }
        
        // Delegate to DriveService and handle the reactive flow.
        return driveService.replicateTemplateFile(request)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("Template replication successful for user {} and new file ID {}.", userEmail, response.getReplicatedFileId());
                        return ResponseEntity.ok(response); // Return 200 OK on success.
                    } else {
                        logger.error("Template replication failed for user {} and master template {}: {}",
                                 userEmail, drive_id, response.getMessage());
                        // Map specific error messages to appropriate HTTP status codes.
                        if (response.getMessage().contains("User not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("User's Drive folder not found")) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                        } else if (response.getMessage().contains("Failed to copy template file")) {
                            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response); // 502 Bad Gateway if Google Drive copy fails.
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Generic 500.
                    }
                })
                .onErrorResume(e -> {
                    // Catch-all for unexpected exceptions.
                    logger.error("Controller error during template replication for user {} and master template {}: {}",
                                 userEmail, drive_id, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TemplateReplicationResponse(
                            false, "An unexpected error occurred during template replication: " + e.getMessage(),
                            null, null, null, null, 0.0, 0.0, null)));
                });
    }

    /**
     * Handles the renaming of an existing file in a user's Google Drive.
     *
     * @param request The {@link FileRenameRequest} containing the user's email,
     * the file ID to rename, and the new desired file name.
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link FileRenameResponse}
     * with the updated file details or an error message.
     */
    @PostMapping("/rename")
    public Mono<ResponseEntity<FileRenameResponse>> renameFile(@RequestBody FileRenameRequest request) {
        String userEmail = request.getUserEmail();
        String fileId = request.getFileId();
        String newFileName = request.getNewFileName();

        logger.info("Received request to rename file ID: {} to '{}' for user: {}", fileId, newFileName, userEmail);

        // Input validation.
        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for file rename operation.");
            return Mono.just(ResponseEntity.badRequest().body(new FileRenameResponse(false, "User email is required for file rename.", null, null, null, 0.0, 0.0)));
        }
        if (fileId == null || fileId.trim().isEmpty()) {
            logger.warn("File ID is missing for rename operation for user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new FileRenameResponse(false, "File ID is required for file rename.", null, null, null, 0.0, 0.0)));
        }
        if (newFileName == null || newFileName.trim().isEmpty()) {
            logger.warn("New file name is missing for rename operation for user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new FileRenameResponse(false, "New file name is required for file rename.", null, null, null, 0.0, 0.0)));
        }

        // Delegate to DriveService and handle the reactive flow.
        return driveService.renameFile(userEmail, fileId, newFileName)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("File ID {} renamed successfully to '{}' for user {}.", fileId, newFileName, userEmail);
                        return ResponseEntity.ok(response); // Return 200 OK on success.
                    } else {
                        logger.error("File rename failed for file ID {} and user {}: {}", fileId, userEmail, response.getMessage());
                        // Map specific error messages to appropriate HTTP status codes.
                        if (response.getMessage().contains("User not found") || response.getMessage().contains("File not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("not accessible for rename")) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response); // 403 Forbidden.
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Generic 500.
                    }
                })
                .onErrorResume(e -> {
                    // Catch-all for unexpected exceptions.
                    logger.error("Controller error during file rename for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileRenameResponse(false, "An unexpected error occurred during file rename: " + e.getMessage(), null, null, null, 0.0, 0.0)));
                });
    }

    /**
     * Handles updating permissions for a file in Google Drive, allowing users to
     * add or remove sharing permissions for other users.
     *
     * @param request The {@link PermissionUpdateRequest} containing the user's email (owner/manager),
     * the file ID, the target email for permission change, the role (e.g., "reader", "writer"),
     * and the action ("add" or "remove").
     * @return A {@link Mono} of {@link ResponseEntity} containing a {@link PermissionUpdateResponse}
     * indicating the outcome of the permission update operation.
     */
    @PostMapping("/update-permission")
    public Mono<ResponseEntity<PermissionUpdateResponse>> updateFilePermission(@RequestBody PermissionUpdateRequest request) {
        String userEmail = request.getUserEmail();
        String fileId = request.getFileId();
        String targetEmail = request.getTargetEmail();
        String role = request.getRole();
        String action = request.getAction();

        logger.debug("Received PermissionUpdateRequest - UserEmail: {}, FileId: {}, TargetEmail: {}, Role: {}, Action: {}",
                     userEmail, fileId, targetEmail, role, action);
        logger.info("Received request to {} permission for file ID: {} for user: {} to target: {} with role: {}",
                     action != null ? action : "unknown action", fileId, userEmail, targetEmail, role != null ? role : "N/A");

        // Comprehensive input validation for permission updates.
        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for permission update operation.");
            return Mono.just(ResponseEntity.badRequest().body(new PermissionUpdateResponse(false, "User email is required for permission update.")));
        }
        if (fileId == null || fileId.trim().isEmpty()) {
            logger.warn("File ID is missing for permission update operation for user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new PermissionUpdateResponse(false, "File ID is required for permission update.")));
        }
        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            logger.warn("Target email is missing for permission update operation for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(new PermissionUpdateResponse(false, "Target email is required for permission update.")));
        }
        if (action == null || action.trim().isEmpty()) {
            logger.warn("Action is missing for permission update operation for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(new PermissionUpdateResponse(false, "Permission action (add/remove) is required.")));
        }
        // Specific validation for 'add' action requiring a role.
        if ("add".equalsIgnoreCase(action) && (role == null || role.trim().isEmpty())) {
            logger.warn("Role is missing for 'add' permission operation for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(new PermissionUpdateResponse(false, "Role is required when adding permission.")));
        }
        // Specific validation for 'remove' action to ensure correct role is specified (or 'remove_all').
        if ("remove".equalsIgnoreCase(action) && !"remove_all".equalsIgnoreCase(role)) {
            logger.warn("Invalid role '{}' provided for 'remove' permission operation for file ID: {}. Expected 'remove_all'.", role, fileId);
            return Mono.just(ResponseEntity.badRequest().body(new PermissionUpdateResponse(false, "Invalid role for 'remove' action. Expected 'remove_all'.")));
        }

        // Delegate to DriveService and handle the reactive flow.
        return driveService.updateFilePermission(userEmail, fileId, targetEmail, role, action)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("Permission operation successful for file ID {} to target {} by user {}. Message: {}",
                                 fileId, targetEmail, userEmail, response.getMessage());
                        return ResponseEntity.ok(response); // Return 200 OK on success.
                    } else {
                        logger.error("Permission operation failed for file ID {} by user {}: {}",
                                 fileId, userEmail, response.getMessage());
                        // Map specific error messages to appropriate HTTP status codes.
                        if (response.getMessage().contains("User not found") || response.getMessage().contains("File not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("not accessible") || response.getMessage().contains("Insufficient permissions")) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response); // 403 Forbidden.
                        } else if (response.getMessage().contains("Invalid request")) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 Bad Request.
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Generic 500.
                    }
                })
                .onErrorResume(e -> {
                    // Catch-all for unexpected exceptions.
                    logger.error("Controller error during permission operation for file ID {} by user {}: {}",
                                 fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PermissionUpdateResponse(false, "An unexpected error occurred during permission operation: " + e.getMessage())));
                });
    }

    /**
     * Handles the export of a Google Drive file to a specified MIME type.
     * This is particularly useful for converting Google Workspace files (Docs, Sheets, Slides)
     * into standard formats like PDF, DOCX, XLSX, etc.
     * The exported file content is returned as a byte array resource.
     *
     * @param request The {@link FileExportRequest} containing the user's email,
     * the file ID to export, the target export MIME type, and an optional new file name.
     * @return A {@link Mono} of {@link ResponseEntity} that either contains the exported file content
     * as a {@link ByteArrayResource} or an error status.
     */
    @PostMapping("/export")
    public Mono<ResponseEntity<? extends Object>> exportFile(@RequestBody FileExportRequest request) {
        String userEmail = request.getUserEmail();
        String fileId = request.getFileId();
        String exportMimeType = request.getExportMimeType();
        String newFileName = request.getNewFileName();

        logger.info("Received request to export file ID: {} to mimeType: {} for user: {}", fileId, exportMimeType, userEmail);

        // Input validation.
        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for file export operation.");
            return Mono.just(ResponseEntity.badRequest().body(null));
        }
        if (fileId == null || fileId.trim().isEmpty()) {
            logger.warn("File ID is missing for export operation for user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(null));
        }
        if (exportMimeType == null || exportMimeType.trim().isEmpty()) {
            logger.warn("Export MIME type is missing for export operation for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        // Delegate to DriveService and handle the reactive flow.
        return driveService.exportFile(userEmail, fileId, exportMimeType, newFileName)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("File ID {} exported successfully to {} for user {}.", fileId, exportMimeType, userEmail);
                        // Set necessary HTTP headers for file download.
                        HttpHeaders headers = new HttpHeaders();
                        String filenameToSuggest = (response.getExportedFileName() != null && !response.getExportedFileName().isEmpty()) ? response.getExportedFileName() : "exported_file";
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenameToSuggest + "\"");
                        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(response.getFileContent().length));
                        return ResponseEntity.ok()
                                .headers(headers)
                                .contentLength(response.getFileContent().length)
                                .contentType(MediaType.parseMediaType(response.getExportedFileMimeType()))
                                .body(new ByteArrayResource(response.getFileContent())); // Return exported file content as a resource.
                    } else {
                        logger.error("File export failed for file ID {} and user {}: {}", fileId, userEmail, response.getMessage());
                        // Map specific error messages to appropriate HTTP status codes.
                        if (response.getMessage().contains("User not found") || response.getMessage().contains("File not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404 Not Found.
                        } else if (response.getMessage().contains("not accessible for export") || response.getMessage().contains("Conversion not supported")) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 400 Bad Request.
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Generic 500.
                    }
                })
                .onErrorResume(e -> {
                    // Catch-all for unexpected exceptions.
                    logger.error("Controller error during file export for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)); // Generic 500.
                });
    }
}