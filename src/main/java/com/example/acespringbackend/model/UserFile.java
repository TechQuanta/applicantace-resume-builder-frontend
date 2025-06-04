package com.example.acespringbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_files")
public class UserFile {

    @Id
    private String id;

    private String userId;
    private String filename;
    private String driveFileId;  // Optional: if pushed to Google Drive
    private long size;           // in bytes
    private String contentType;
    private LocalDateTime uploadedAt;
}
