// src/main/java/com/example/acespringbackend/auth/controller/DriveController.java
package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.FileUploadResponse;
import com.example.acespringbackend.auth.dto.FileViewResponse;
import com.example.acespringbackend.service.DriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/drive")
public class DriveController {

    private final DriveService driveService;

    @Value("${user.drive.quota.mb:10}")
    private double userDriveQuotaMb;

    @Autowired
    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    // 1. Create folder for user
    @PostMapping("/create-folder")
    public Mono<ResponseEntity<String>> createUserFolder(@RequestParam String email) {
        return driveService.createUserFolderIfNotExists(email)
                .map(folderId -> ResponseEntity.ok("Drive folder created with ID: " + folderId))
                .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body("Error creating folder: " + ex.getMessage())));
    }

    // 2. Upload file
    @PostMapping("/upload")
    public Mono<ResponseEntity<FileUploadResponse>> uploadFile(
            @RequestParam String email,
            @RequestParam MultipartFile file) {
        return driveService.uploadFile(email, file)
                .map(fileUploadResponse -> {
                    // Using getSuccess() as per the updated FileUploadResponse DTO
                    if (!fileUploadResponse.getSuccess()) {
                        HttpStatus status;
                        if (fileUploadResponse.getMessage() != null) {
                            if (fileUploadResponse.getMessage().contains("quota exceeded") ||
                                    fileUploadResponse.getMessage().contains("exceeds individual upload limit") ||
                                    fileUploadResponse.getMessage().contains("empty file") ||
                                    fileUploadResponse.getMessage().contains("Docs folder not found") ||
                                    fileUploadResponse.getMessage().contains("User not found for file upload operation")) {
                                status = HttpStatus.BAD_REQUEST;
                            } else {
                                status = HttpStatus.INTERNAL_SERVER_ERROR;
                            }
                        } else {
                            status = HttpStatus.INTERNAL_SERVER_ERROR;
                        }
                        // If it's an error, return the appropriate status and body
                        return ResponseEntity.status(status).body(fileUploadResponse);
                    } else {
                        // If it's a success, return OK
                        return ResponseEntity.ok(fileUploadResponse);
                    }
                })
                .onErrorResume(ex -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FileUploadResponse.builder()
                            .success(false)
                            .message("An unexpected error occurred during file upload: " + ex.getMessage())
                            .maxStorageQuotaMb(userDriveQuotaMb)
                            .build()));
                });
    }

    // 3. List files
    @GetMapping("/list")
    public Mono<ResponseEntity<List<String>>> listUserFiles(@RequestParam String email) {
        return driveService.listFiles(email)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of())));
    }

    // Endpoint to create a Google Doc
    @PostMapping("/create-doc")
    public Mono<ResponseEntity<FileViewResponse>> createGoogleDoc(
            @RequestParam String email,
            @RequestParam String docTitle) {
        return driveService.createGoogleDoc(email, docTitle)
                .map(fileViewResponse -> ResponseEntity.status(HttpStatus.CREATED).body(fileViewResponse))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FileViewResponse.builder()
                        .success(false) // Added success field for error case
                        .message("Error creating Google Doc: " + ex.getMessage())
                        .build())));
    }

    // Endpoint to get webViewLink for a specific file (by fileId)
    @GetMapping("/view-file")
    public Mono<ResponseEntity<FileViewResponse>> getFileWebViewLink(
            @RequestParam String email,
            @RequestParam String fileId) {
        return driveService.getWebViewLinkForFile(email, fileId)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(FileViewResponse.builder()
                        .success(false) // Added success field for error case
                        .fileId(fileId) // Keep fileId in error response for context
                        .message("Error retrieving file view link: " + ex.getMessage())
                        .build())));
    }
}
