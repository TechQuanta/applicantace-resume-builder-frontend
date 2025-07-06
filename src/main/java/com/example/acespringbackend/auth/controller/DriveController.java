//package com.example.acespringbackend.auth.controller;
//
//import com.example.acespringbackend.auth.dto.FileUploadRequest; // Import the new DTO
//import com.example.acespringbackend.auth.dto.FileUploadResponse;
//import com.example.acespringbackend.service.DriveService;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import reactor.core.publisher.Mono;
//
//@RestController
//@RequestMapping("/ace/drive")
//public class DriveController {
//
//    private static final Logger logger = LoggerFactory.getLogger(DriveController.class);
//
//    private final DriveService driveService;
//
//    @Value("${user.drive.quota.mb:10}")
//    private double userDriveQuotaMb;
//
//    @Autowired
//    public DriveController(DriveService driveService) { // Constructor injection
//        this.driveService = driveService;
//    }
//
//    /**
//     * Handles file uploads to Google Drive, associating the file with a user
//     * identified by their email, and uploading to a specified folder ID.
//     * The file, user email, and folder ID are all sent as part of a single form data request.
//     *
//     * @param file The MultipartFile to be uploaded, part of the form data.
//     * @param request A FileUploadRequest object containing userEmail and folderId, bound from form data.
//     * @return A Mono emitting a ResponseEntity with FileUploadResponse, indicating success or failure.
//     */
//    @PostMapping("/upload")
//    public Mono<ResponseEntity<FileUploadResponse>> uploadFile(
//            @RequestPart("file") MultipartFile file, // File is still a separate part
//            @ModelAttribute FileUploadRequest request) { // Use @ModelAttribute for the DTO containing other form fields
//
//        String userEmail = request.getUserEmail();
//        String folderId = request.getFolderId();
//
//        logger.info("Received file upload request for folderId: {} from user: {}", folderId, userEmail);
//
//        if (file.isEmpty()) {
//            logger.warn("Received empty file for upload for folderId: {} from user: {}", folderId, userEmail);
//            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
//                    false, "File is empty. Please select a non-empty file.",
//                    null, null, null, 0.0, 0.0)));
//        }
//
//        if (userEmail == null || userEmail.trim().isEmpty()) {
//            logger.warn("User email is missing for upload to folderId: {}", folderId);
//            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
//                    false, "User email is required for file upload.",
//                    null, null, null, 0.0, 0.0)));
//        }
//
//        if (folderId == null || folderId.trim().isEmpty()) {
//            logger.warn("Folder ID is missing for upload from user: {}", userEmail);
//            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
//                    false, "Folder ID is required for file upload.",
//                    null, null, null, 0.0, 0.0)));
//        }
//
//        logger.info("Successfully received file: {}", file.getOriginalFilename());
//
//        // Pass the userEmail, file, and folderId to the DriveService
//        return driveService.uploadFile(userEmail, file, folderId)
//                .map(response -> {
//                    if (response.getSuccess()) {
//                        logger.info("File upload successful for folderId {} and user {}. File ID: {}", folderId, userEmail, response.getDriveFileId());
//                        return ResponseEntity.ok(response);
//                    } else {
//                        logger.error("File upload failed for folderId {} and user {}: {}", folderId, userEmail, response.getMessage());
//                        if (response.getMessage().contains("User not found")) {
//                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//                        } else if (response.getMessage().contains("exceeds individual upload limit") ||
//                                   response.getMessage().contains("overall storage quota exceeded")) {
//                            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
//                        } else if (response.getMessage().contains("Drive folder not initialized") || response.getMessage().contains("Target folder ID for upload is missing")) {
//                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//                        }
//                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//                    }
//                })
//                .onErrorResume(e -> {
//                    logger.error("Controller error during file upload for folderId {} and user {}: {}", folderId, userEmail, e.getMessage(), e);
//                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileUploadResponse(
//                            false, "An unexpected error occurred during file upload: " + e.getMessage(),
//                            null, null, null, 0.0, 0.0)));
//                });
//    }
//
//    // Keep other methods if they are still needed for authenticated operations.
//    // If you uncomment them later, remember they might need proper @AuthenticationPrincipal logic.
//}
//package com.example.acespringbackend.auth.controller;
//
//import com.example.acespringbackend.auth.dto.DeleteResponse;
//import com.example.acespringbackend.auth.dto.DeleteRequest;
//import com.example.acespringbackend.auth.dto.FileDetail;
//import com.example.acespringbackend.auth.dto.FileListResponse;
//import com.example.acespringbackend.auth.dto.ListRequest; // ADDED: Import FileListRequest for the POST body
//import com.example.acespringbackend.auth.dto.FileUploadRequest;
//import com.example.acespringbackend.auth.dto.FileUploadResponse;
//// REMOVED: import com.example.acespringbackend.auth.dto.FileNameListResponse; (as it's not used in your provided code)
//import com.example.acespringbackend.service.DriveService;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import reactor.core.publisher.Mono;
//
//import java.util.Collections;
//import java.util.List;

//// REMOVED: import java.util.stream.Collectors; (as it's not used in your provided code after removing getAllFileNames)
//
//
//@RestController
//@RequestMapping("/ace/drive")
//public class DriveController {
//
//    private static final Logger logger = LoggerFactory.getLogger(DriveController.class);
//
//    private final DriveService driveService;
//
//    @Value("${user.drive.quota.mb:10}")
//    private double userDriveQuotaMb;
//
//    @Autowired
//    public DriveController(DriveService driveService) { // Constructor injection
//        this.driveService = driveService;
//    }
//
//    /**
//     * Handles file uploads to Google Drive, associating the file with a user
//     * identified by their email, and uploading to a specified folder ID.
//     * The file, user email, and folder ID are all sent as part of a single form data request.
//     *
//     * @param file The MultipartFile to be uploaded, part of the form data.
//     * @param request A FileUploadRequest object containing userEmail and folderId, bound from form data.
//     * @return A Mono emitting a ResponseEntity with FileUploadResponse, indicating success or failure.
//     */
//    @PostMapping("/upload")
//    public Mono<ResponseEntity<FileUploadResponse>> uploadFile(
//            @RequestPart("file") MultipartFile file, // File is still a separate part
//            @ModelAttribute FileUploadRequest request) { // Use @ModelAttribute for the DTO containing other form fields
//
//        String userEmail = request.getUserEmail();
//        String folderId = request.getFolderId();
//
//        logger.info("Received file upload request for folderId: {} from user: {}", folderId, userEmail);
//
//        if (file.isEmpty()) {
//            logger.warn("Received empty file for upload for folderId: {} from user: {}", folderId, userEmail);
//            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
//                    false, "File is empty. Please select a non-empty file.",
//                    null, null, null, 0.0, 0.0)));
//        }
//
//        if (userEmail == null || userEmail.trim().isEmpty()) {
//            logger.warn("User email is missing for upload to folderId: {}", folderId);
//            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
//                    false, "User email is required for file upload.",
//                    null, null, null, 0.0, 0.0)));
//        }
//
//        if (folderId == null || folderId.trim().isEmpty()) {
//            logger.warn("Folder ID is missing for upload from user: {}", userEmail);
//            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
//                    false, "Folder ID is required for file upload.",
//                    null, null, null, 0.0, 0.0)));
//        }
//
//        logger.info("Successfully received file: {}", file.getOriginalFilename());
//
//        // Pass the userEmail, file, and folderId to the DriveService
//        return driveService.uploadFile(userEmail, file, folderId)
//                .map(response -> {
//                    if (response.getSuccess()) {
//                        logger.info("File upload successful for folderId {} and user {}. File ID: {}", folderId, userEmail, response.getDriveFileId());
//                        return ResponseEntity.ok(response);
//                    } else {
//                        logger.error("File upload failed for folderId {} and user {}: {}", folderId, userEmail, response.getMessage());
//                        if (response.getMessage().contains("User not found")) {
//                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//                        } else if (response.getMessage().contains("exceeds individual upload limit") ||
//                                   response.getMessage().contains("overall storage quota exceeded")) {
//                            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
//                        } else if (response.getMessage().contains("Drive folder not initialized") || response.getMessage().contains("Target folder ID for upload is missing")) {
//                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//                        }
//                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//                    }
//                })
//                .onErrorResume(e -> {
//                    logger.error("Controller error during file upload for folderId {} and user {}: {}", folderId, userEmail, e.getMessage(), e);
//                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileUploadResponse(
//                            false, "An unexpected error occurred during file upload: " + e.getMessage(),
//                            null, null, null, 0.0, 0.0)));
//                });
//    }
//
//    /**
//     * Retrieves all file details for a given user and folder.
//     * NOW ACCEPTS A POST REQUEST WITH A JSON BODY.
//     *
//     * @param request A FileListRequest object containing userEmail and folderId in the JSON body.
//     * @return A Mono emitting a ResponseEntity with FileListResponse, containing a list of file details.
//     */
//    @PostMapping("/files") // CHANGED: From @GetMapping to @PostMapping
//    public Mono<ResponseEntity<FileListResponse>> getAllFiles(
//            @RequestBody ListRequest request) { // CHANGED: From @RequestParam to @RequestBody FileListRequest
//
//        String userEmail = request.getUserEmail(); // CHANGED: Get from request DTO
//        String folderId = request.getFolderId();   // CHANGED: Get from request DTO
//
//        logger.info("Received POST request to get all files for folderId: {} from user: {}", folderId, userEmail);
//
//        if (userEmail == null || userEmail.trim().isEmpty()) {
//            logger.warn("User email is missing for getting all files for folderId: {}", folderId);
//            return Mono.just(ResponseEntity.badRequest().body(new FileListResponse(
//                    false, "User email is required to list files.",
//                    Collections.<FileDetail>emptyList(), 0.0, userDriveQuotaMb))); // FIX for type inference
//        }
//
//        if (folderId == null || folderId.trim().isEmpty()) {
//            logger.warn("Folder ID is missing for getting all files from user: {}", userEmail);
//            return Mono.just(ResponseEntity.badRequest().body(new FileListResponse(
//                    false, "Folder ID is required to list files.",
//                    Collections.<FileDetail>emptyList(), 0.0, userDriveQuotaMb))); // FIX for type inference
//        }
//
//        // Call the service layer to get file details
//        return driveService.getAllFileDetails(userEmail, folderId)
//                .map(response -> {
//                    if (response.getSuccess()) {
//                        logger.info("Successfully retrieved {} files for folderId {} and user {}",
//                                response.getFiles().size(), folderId, userEmail);
//                        return ResponseEntity.ok(response);
//                    } else {
//                        logger.error("Failed to retrieve files for folderId {} and user {}: {}",
//                                folderId, userEmail, response.getMessage());
//                        if (response.getMessage().contains("User not found")) {
//                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//                        } else if (response.getMessage().contains("Drive folder not initialized") || response.getMessage().contains("Target folder ID for listing is missing")) {
//                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//                        }
//                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//                    }
//                })
//                .onErrorResume(e -> {
//                    logger.error("Controller error during getting all files for folderId {} and user {}: {}", folderId, userEmail, e.getMessage(), e);
//                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileListResponse(
//                            false, "An unexpected error occurred while listing files: " + e.getMessage(),
//                            Collections.<FileDetail>emptyList(), 0.0, userDriveQuotaMb))); // FIX for type inference
//                });
//    }
//
//    /**
//     * Deletes a specific file from Google Drive and updates the user's storage usage.
//     *
//     * @param request A FileDeleteRequest object containing userEmail and fileId.
//     * @return A Mono emitting a ResponseEntity with DeleteResponse, indicating success or failure.
//     */
//    @DeleteMapping("/delete")
//    public Mono<ResponseEntity<DeleteResponse>> deleteFile(@RequestBody DeleteRequest request) {
//        String userEmail = request.getUserEmail();
//        String fileId = request.getFileId();
//
//        logger.info("Received request to delete file ID: {} for user: {}", fileId, userEmail);
//
//        if (userEmail == null || userEmail.trim().isEmpty()) {
//            logger.warn("User email is missing for file deletion for file ID: {}", fileId);
//            return Mono.just(ResponseEntity.badRequest().body(new DeleteResponse(
//                    false, "User email is required for file deletion.", fileId, 0L, 0.0, 0.0)));
//        }
//
//        if (fileId == null || fileId.trim().isEmpty()) {
//            logger.warn("File ID is missing for deletion from user: {}", userEmail);
//            return Mono.just(ResponseEntity.badRequest().body(new DeleteResponse(
//                    false, "File ID is required for deletion.", null, 0L, 0.0, 0.0)));
//        }
//
//        return driveService.deleteFile(userEmail, fileId)
//                .map(response -> {
//                    if (response.getSuccess()) {
//                        logger.info("File ID {} deleted successfully for user {}. New usage: {} MB.",
//                                fileId, userEmail, response.getCurrentStorageUsageMb());
//                        return ResponseEntity.ok(response);
//                    } else {
//                        logger.error("File deletion failed for file ID {} and user {}: {}",
//                                fileId, userEmail, response.getMessage());
//                        if (response.getMessage().contains("User not found")) {
//                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//                        } else if (response.getMessage().contains("File not found") || response.getMessage().contains("not accessible for deletion")) {
//                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//                        }
//                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//                    }
//                })
//                .onErrorResume(e -> {
//                    logger.error("Controller error during file deletion for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
//                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DeleteResponse(
//                            false, "An unexpected error occurred during file deletion: " + e.getMessage(),
//                            fileId, 0L, 0.0, 0.0)));
//                });
//    }
//}
package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.DeleteResponse;
import com.example.acespringbackend.auth.dto.DeleteRequest;
import com.example.acespringbackend.auth.dto.FileDetail;
import com.example.acespringbackend.auth.dto.FileListResponse;
import com.example.acespringbackend.auth.dto.ListRequest;
import com.example.acespringbackend.auth.dto.FileUploadRequest;
import com.example.acespringbackend.auth.dto.FileUploadResponse;
import com.example.acespringbackend.auth.dto.TemplateReplicationRequest;
import com.example.acespringbackend.auth.dto.TemplateReplicationResponse;
import com.example.acespringbackend.auth.dto.DownloadResult;
import com.example.acespringbackend.auth.dto.FileRenameRequest;       // NEW
import com.example.acespringbackend.auth.dto.FileRenameResponse;      // NEW
import com.example.acespringbackend.auth.dto.PermissionUpdateRequest; // NEW
import com.example.acespringbackend.auth.dto.PermissionUpdateResponse;// NEW
import com.example.acespringbackend.auth.dto.FileExportRequest;      // NEW
import com.example.acespringbackend.auth.dto.FileExportResponse;     // NEW
import com.example.acespringbackend.service.DriveService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/ace/drive")
public class DriveController {

    private static final Logger logger = LoggerFactory.getLogger(DriveController.class);

    private final DriveService driveService;

    @Value("${user.drive.quota.mb:10}")
    private double userDriveQuotaMb;

    @Autowired
    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    /**
     * Handles file uploads to Google Drive, associating the file with a user
     * identified by their email, and uploading to a specified folder ID.
     * The file, user email, and folder ID are all sent as part of a single form data request.
     *
     * @param file The MultipartFile to be uploaded, part of the form data.
     * @param request A FileUploadRequest object containing userEmail and folderId, bound from form data.
     * @return A Mono emitting a ResponseEntity with FileUploadResponse, indicating success or failure.
     */
    @PostMapping("/upload")
    public Mono<ResponseEntity<FileUploadResponse>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @ModelAttribute FileUploadRequest request) {

        String userEmail = request.getUserEmail();
        String folderId = request.getFolderId();

        logger.info("Received file upload request for folderId: {} from user: {}", folderId, userEmail);

        if (file.isEmpty()) {
            logger.warn("Received empty file for upload for folderId: {} from user: {}", folderId, userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new FileUploadResponse(
                    false, "File is empty. Please select a non-empty file.",
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

        logger.info("Successfully received file: {}", file.getOriginalFilename());

        return driveService.uploadFile(userEmail, file, folderId)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("File upload successful for folderId {} and user {}. File ID: {}", folderId, userEmail, response.getDriveFileId());
                        return ResponseEntity.ok(response);
                    } else {
                        logger.error("File upload failed for folderId {} and user {}: {}", folderId, userEmail, response.getMessage());
                        if (response.getMessage().contains("User not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("exceeds individual upload limit") ||
                                   response.getMessage().contains("overall storage quota exceeded")) {
                            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
                        } else if (response.getMessage().contains("Drive folder not initialized") || response.getMessage().contains("Target folder ID for upload is missing")) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Controller error during file upload for folderId {} and user {}: {}", folderId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileUploadResponse(
                            false, "An unexpected error occurred during file upload: " + e.getMessage(),
                            null, null, null, 0.0, 0.0)));
                });
    }

    /**
     * Retrieves all file details for a given user and folder.
     * NOW ACCEPTS A POST REQUEST WITH A JSON BODY.
     *
     * @param request A FileListRequest object containing userEmail and folderId in the JSON body.
     * @return A Mono emitting a ResponseEntity with FileListResponse, containing a list of file details.
     */
    @PostMapping("/files")
    public Mono<ResponseEntity<FileListResponse>> getAllFiles(
            @RequestBody ListRequest request) {

        String userEmail = request.getUserEmail();
        String folderId = request.getFolderId();

        logger.info("Received POST request to get all files for folderId: {} from user: {}", folderId, userEmail);

        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for getting all files for folderId: {}", folderId);
            return Mono.just(ResponseEntity.badRequest().body(new FileListResponse(
                    false, "User email is required to list files.",
                    Collections.<FileDetail>emptyList(), 0.0, userDriveQuotaMb)));
        }

        if (folderId == null || folderId.trim().isEmpty()) {
            logger.warn("Folder ID is missing for getting all files from user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new FileListResponse(
                    false, "Folder ID is required to list files.",
                    Collections.<FileDetail>emptyList(), 0.0, userDriveQuotaMb)));
        }

        return driveService.getAllFileDetails(userEmail, folderId)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("Successfully retrieved {} files for folderId {} and user {}",
                                response.getFiles().size(), folderId, userEmail);
                        return ResponseEntity.ok(response);
                    } else {
                        logger.error("Failed to retrieve files for folderId {} and user {}: {}",
                                folderId, userEmail, response.getMessage());
                        if (response.getMessage().contains("User not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("Drive folder not initialized") || response.getMessage().contains("Target folder ID for listing is missing")) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Controller error during getting all files for folderId {} and user {}: {}", folderId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileListResponse(
                            false, "An unexpected error occurred while listing files: " + e.getMessage(),
                            Collections.<FileDetail>emptyList(), 0.0, userDriveQuotaMb)));
                });
    }

    /**
     * Deletes a specific file from Google Drive and updates the user's storage usage.
     *
     * @param request A FileDeleteRequest object containing userEmail and fileId.
     * @return A Mono emitting a ResponseEntity with DeleteResponse, indicating success or failure.
     */
    @DeleteMapping("/delete")
    public Mono<ResponseEntity<DeleteResponse>> deleteFile(@RequestBody DeleteRequest request) {
        String userEmail = request.getUserEmail();
        String fileId = request.getFileId();

        logger.info("Received request to delete file ID: {} for user: {}", fileId, userEmail);

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

        return driveService.deleteFile(userEmail, fileId)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("File ID {} deleted successfully for user {}. New usage: {} MB.",
                                fileId, userEmail, response.getCurrentStorageUsageMb());
                        return ResponseEntity.ok(response);
                    } else {
                        logger.error("File deletion failed for file ID {} and user {}: {}",
                                fileId, userEmail, response.getMessage());
                        if (response.getMessage().contains("User not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("File not found") || response.getMessage().contains("not accessible for deletion")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Controller error during file deletion for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DeleteResponse(
                            false, "An unexpected error occurred during file deletion: " + e.getMessage(),
                            fileId, 0L, 0.0, 0.0)));
                });
    }

    /**
     * Downloads a specific file from Google Drive.
     *
     * @param userEmail The email of the user requesting the download.
     * @param fileId The ID of the file to be downloaded.
     * @return A Mono emitting a ResponseEntity with the file content as a ByteArrayResource,
     * and appropriate headers for download, or an error response.
     */
    @GetMapping("/download/{fileId}")
    public Mono<ResponseEntity<? extends Object>> downloadFile(
            @RequestParam String userEmail,
            @PathVariable String fileId) {

        logger.info("Received request to download file ID: {} for user: {}", fileId, userEmail);

        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for file download for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        if (fileId == null || fileId.trim().isEmpty()) {
            logger.warn("File ID is missing for download from user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        return driveService.downloadFile(userEmail, fileId)
                .map(downloadResult -> {
                    if (downloadResult.getSuccess()) {
                        logger.info("Successfully prepared file for download: {} (MimeType: {})", downloadResult.getFileName(), downloadResult.getMimeType());
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadResult.getFileName() + "\"");
                        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(downloadResult.getFileContent().length));
                        return ResponseEntity.ok()
                                .headers(headers)
                                .contentLength(downloadResult.getFileContent().length)
                                .contentType(MediaType.parseMediaType(downloadResult.getMimeType()))
                                .body(new ByteArrayResource(downloadResult.getFileContent()));
                    } else {
                        logger.error("File download failed for file ID {} and user {}: {}", fileId, userEmail, downloadResult.getMessage());
                        if (downloadResult.getMessage().contains("User not found") || downloadResult.getMessage().contains("File not found") || downloadResult.getMessage().contains("not accessible")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Controller error during file download for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                });
    }

    /**
     * Endpoint to replicate a master template file into a user's Google Drive folder.
     *
     * @param request A TemplateReplicationRequest object containing userEmail, masterTemplateFileId, and newFileName.
     * @return A Mono emitting a ResponseEntity with TemplateReplicationResponse, indicating success or failure.
     */
    @PostMapping("/replicate-template")
    public Mono<ResponseEntity<TemplateReplicationResponse>> replicateTemplate(
            @Valid @RequestBody TemplateReplicationRequest request) { // Use @Valid for DTO validation

        String userEmail = request.getUserEmail();
        String drive_id = request.getDrive_id(); // Use the new field name
        String newFileName = request.getNewFileName();

        // Extracting all the new template details from the request
        String templateName = request.getName();
        String category = request.getCategory();
        String imageUrl = request.getImage_url();
        String description = request.getDescription();
        String spotlight = request.getSpotlight();
        String provider = request.getProvider();
        String targetDriveFolderId = request.getTargetDriveFolderId();


        logger.info("Received request to replicate template ID: {} (Name: {}) for user: {} with new name: {} in folder: {}",
                drive_id, templateName, userEmail, newFileName, targetDriveFolderId);

        // Basic validations (can be more comprehensive with @Valid and global error handling)
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
        
        // Pass all relevant details to the service layer
        return driveService.replicateTemplateFile(request) // Pass the entire request DTO
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("Template replication successful for user {} and new file ID {}.", userEmail, response.getReplicatedFileId());
                        return ResponseEntity.ok(response);
                    } else {
                        logger.error("Template replication failed for user {} and master template {}: {}",
                                userEmail, drive_id, response.getMessage());
                        if (response.getMessage().contains("User not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("User's Drive folder not found")) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                        } else if (response.getMessage().contains("Failed to copy template file")) {
                            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Controller error during template replication for user {} and master template {}: {}",
                            userEmail, drive_id, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TemplateReplicationResponse(
                            false, "An unexpected error occurred during template replication: " + e.getMessage(),
                            null, null, null, null, 0.0, 0.0, null)));
                });
    }

    /**
     * Endpoint to rename a file in Google Drive.
     *
     * @param request A FileRenameRequest object containing userEmail, fileId, and newFileName.
     * @return A Mono emitting a ResponseEntity with FileRenameResponse, indicating success or failure.
     */
    @PostMapping("/rename")
    public Mono<ResponseEntity<FileRenameResponse>> renameFile(@RequestBody FileRenameRequest request) {
        String userEmail = request.getUserEmail();
        String fileId = request.getFileId();
        String newFileName = request.getNewFileName();

        logger.info("Received request to rename file ID: {} to '{}' for user: {}", fileId, newFileName, userEmail);

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

        return driveService.renameFile(userEmail, fileId, newFileName)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("File ID {} renamed successfully to '{}' for user {}.", fileId, newFileName, userEmail);
                        return ResponseEntity.ok(response);
                    } else {
                        logger.error("File rename failed for file ID {} and user {}: {}", fileId, userEmail, response.getMessage());
                        if (response.getMessage().contains("User not found") || response.getMessage().contains("File not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("not accessible for rename")) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Controller error during file rename for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileRenameResponse(false, "An unexpected error occurred during file rename: " + e.getMessage(), null, null, null, 0.0, 0.0)));
                });
    }

    /**
     * Endpoint to update permissions for a file in Google Drive.
     *
     * @param request A PermissionUpdateRequest object containing userEmail, fileId, targetEmail, and role.
     * @return A Mono emitting a ResponseEntity with PermissionUpdateResponse, indicating success or failure.
     */
    @PostMapping("/update-permission")
    public Mono<ResponseEntity<PermissionUpdateResponse>> updateFilePermission(@RequestBody PermissionUpdateRequest request) {
        String userEmail = request.getUserEmail();
        String fileId = request.getFileId();
        String targetEmail = request.getTargetEmail();
        String role = request.getRole();
        String action = request.getAction(); // Get the action from the request DTO

        logger.debug("Received PermissionUpdateRequest - UserEmail: {}, FileId: {}, TargetEmail: {}, Role: {}, Action: {}",
                     userEmail, fileId, targetEmail, role, action);


        logger.info("Received request to {} permission for file ID: {} for user: {} to target: {} with role: {}",
                action != null ? action : "unknown action", fileId, userEmail, targetEmail, role != null ? role : "N/A");

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
        // If action is "add", role is required
        if ("add".equalsIgnoreCase(action) && (role == null || role.trim().isEmpty())) {
            logger.warn("Role is missing for 'add' permission operation for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(new PermissionUpdateResponse(false, "Role is required when adding permission.")));
        }
        // If action is "remove", explicitly check if the role is "remove_all"
        if ("remove".equalsIgnoreCase(action) && !"remove_all".equalsIgnoreCase(role)) {
            logger.warn("Invalid role '{}' provided for 'remove' permission operation for file ID: {}. Expected 'remove_all'.", role, fileId);
            return Mono.just(ResponseEntity.badRequest().body(new PermissionUpdateResponse(false, "Invalid role for 'remove' action. Expected 'remove_all'.")));
        }


        // IMPORTANT: Pass both 'role' and 'action' to the service
        return driveService.updateFilePermission(userEmail, fileId, targetEmail, role, action)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("Permission operation successful for file ID {} to target {} by user {}. Message: {}",
                                fileId, targetEmail, userEmail, response.getMessage());
                        return ResponseEntity.ok(response);
                    } else {
                        logger.error("Permission operation failed for file ID {} by user {}: {}",
                                fileId, userEmail, response.getMessage());
                        if (response.getMessage().contains("User not found") || response.getMessage().contains("File not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("not accessible") || response.getMessage().contains("Insufficient permissions")) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                        } else if (response.getMessage().contains("Invalid request")) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Controller error during permission operation for file ID {} by user {}: {}",
                            fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PermissionUpdateResponse(false, "An unexpected error occurred during permission operation: " + e.getMessage())));
                });
    }


    /**
     * Endpoint to export/convert a Google Drive file to a specified MIME type.
     *
     * @param request A FileExportRequest object containing userEmail, fileId, exportMimeType, and optional newFileName.
     * @return A Mono emitting a ResponseEntity with FileExportResponse, containing the exported file's content.
     */
    @PostMapping("/export")
    public Mono<ResponseEntity<FileExportResponse>> exportFile(@RequestBody FileExportRequest request) {
        String userEmail = request.getUserEmail();
        String fileId = request.getFileId();
        String exportMimeType = request.getExportMimeType();
        String newFileName = request.getNewFileName(); // Optional

        logger.info("Received request to export file ID: {} to mimeType: {} for user: {}", fileId, exportMimeType, userEmail);

        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.warn("User email is missing for file export operation.");
            return Mono.just(ResponseEntity.badRequest().body(new FileExportResponse(false, "User email is required for file export.", null, null, null, 0.0, 0.0)));
        }
        if (fileId == null || fileId.trim().isEmpty()) {
            logger.warn("File ID is missing for export operation for user: {}", userEmail);
            return Mono.just(ResponseEntity.badRequest().body(new FileExportResponse(false, "File ID is required for file export.", null, null, null, 0.0, 0.0)));
        }
        if (exportMimeType == null || exportMimeType.trim().isEmpty()) {
            logger.warn("Export MIME type is missing for export operation for file ID: {}", fileId);
            return Mono.just(ResponseEntity.badRequest().body(new FileExportResponse(false, "Export MIME type is required for file export.", null, null, null, 0.0, 0.0)));
        }

        return driveService.exportFile(userEmail, fileId, exportMimeType, newFileName)
                .map(response -> {
                    if (response.getSuccess()) {
                        logger.info("File ID {} exported successfully to {} for user {}.", fileId, exportMimeType, userEmail);
                        HttpHeaders headers = new HttpHeaders();
                        String filenameToSuggest = (response.getExportedFileName() != null && !response.getExportedFileName().isEmpty()) ? response.getExportedFileName() : "exported_file";
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filenameToSuggest + "\"");
                        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(response.getFileContent().length));
                        return ResponseEntity.ok()
                                .headers(headers)
                                .contentLength(response.getFileContent().length)
                                .contentType(MediaType.parseMediaType(response.getExportedFileMimeType()))
                                .body(response); // Send the DTO back, which includes the byte array
                    } else {
                        logger.error("File export failed for file ID {} and user {}: {}", fileId, userEmail, response.getMessage());
                        if (response.getMessage().contains("User not found") || response.getMessage().contains("File not found")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (response.getMessage().contains("not accessible for export") || response.getMessage().contains("Conversion not supported")) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Controller error during file export for file ID {} and user {}: {}", fileId, userEmail, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileExportResponse(false, "An unexpected error occurred during file export: " + e.getMessage(), null, null, null, 0.0, 0.0)));
                });
    }
}
