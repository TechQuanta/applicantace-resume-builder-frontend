package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.service.DriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/drive")
public class DriveController {

    private final DriveService driveService;

    @Autowired
    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    // 1. Create folder for user (called after signup/login)
    @PostMapping("/create-folder")
    public Mono<ResponseEntity<String>> createUserFolder(@RequestParam String email) {
        return driveService.createUserFolderIfNotExists(email)
                .map(folderId -> ResponseEntity.ok("Drive folder created with ID: " + folderId))
                .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body("Error: " + ex.getMessage())));
    }

    // 2. Upload file (limit to 2 files + size check inside service)
    @PostMapping("/upload")
    public Mono<ResponseEntity<String>> uploadFile(
            @RequestParam String email,
            @RequestParam MultipartFile file) {

        return driveService.uploadFile(email, file)
                .map(msg -> ResponseEntity.ok("File uploaded: " + msg))
                .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body("Error: " + ex.getMessage())));
    }

    // 3. List all files
    @GetMapping("/list")
    public Mono<ResponseEntity<List<String>>> listUserFiles(@RequestParam String email) {
        return driveService.listFiles(email)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(List.of())));
    }


}
