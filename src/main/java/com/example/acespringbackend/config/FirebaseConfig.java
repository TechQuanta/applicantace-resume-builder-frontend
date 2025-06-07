package com.example.acespringbackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    // This value should be set in your application.properties or application.yml
    // e.g., firebase.credentials.path=path/to/your/serviceAccountKey.json
    @Value("${firebase.credentials.path}")
    private String firebaseCredentialsPath;

    @Bean
    public FirebaseAuth firebaseAuth() {
        try {
            // Load the service account key file from the classpath
            ClassPathResource resource = new ClassPathResource(firebaseCredentialsPath);

            try (InputStream serviceAccount = resource.getInputStream()) {
                // Build FirebaseOptions using credentials from the service account file
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                // Initialize FirebaseApp only if it hasn't been initialized yet
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    logger.info("Firebase app initialized successfully");
                } else {
                    logger.info("Firebase app already initialized");
                }
            }

            // Return the FirebaseAuth instance
            return FirebaseAuth.getInstance();
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase", e);
            // Re-throw as RuntimeException to prevent application startup if Firebase cannot be initialized
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}