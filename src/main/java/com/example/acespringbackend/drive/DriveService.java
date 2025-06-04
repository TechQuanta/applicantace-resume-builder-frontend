package com.example.acespringbackend.drive;

import com.example.acespringbackend.model.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

// Google Drive imports
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class DriveService {

    private final Drive driveService;

    public DriveService() throws GeneralSecurityException, IOException {
        // Initialize Google Drive client with service account or credentials JSON file
        // NOTE: Implement authentication with Google credentials here

        this.driveService = createDriveService();
    }

    private Drive createDriveService() throws GeneralSecurityException, IOException {
        // TODO: Replace path/to/credentials.json with your service account credentials file path
        GoogleCredential credential = GoogleCredential.fromStream(
                        getClass().getResourceAsStream("/credentials.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive"));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("YourAppName").build();
    }

    public Mono<String> createFolderForUser(User user) {
        return Mono.fromCallable(() -> {
            File fileMetadata = new File();
            fileMetadata.setName("user_" + user.getId());
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File folder = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();

            return folder.getId();
        });
    }
}
