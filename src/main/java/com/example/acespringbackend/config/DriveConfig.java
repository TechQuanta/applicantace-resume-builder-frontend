package com.example.acespringbackend.config;

import com.example.acespringbackend.service.DriveProperties;
import com.example.acespringbackend.utility.DriveUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DriveConfig class provides Spring Beans for Google Drive related configurations.
 * It sets up DriveProperties and ensures DriveUtility can be injected.
 */
@Configuration
public class DriveConfig {

    private static final Logger log = LoggerFactory.getLogger(DriveConfig.class);

    // Inject raw values directly from application.properties
    @Value("${google.drive.master.folder.id}")
    private String masterFolderId;

    @Value("${user.drive.quota.mb:10}")
    private double userDriveQuotaMb;

    @Value("${user.max.individual.file.size.mb:3}")
    private double maxIndividualFileSizeMb;

    /**
     * Creates and configures a DriveProperties bean. This centralizes the Drive-related
     * configuration values, making them easily injectable into other services.
     *
     * @return A DriveProperties object containing the configured Google Drive properties.
     */
    @Bean
    public DriveProperties driveProperties() {
        long maxUserSpaceBytes = (long) (userDriveQuotaMb * 1024 * 1024);
        long maxIndividualFileSizeBytes = (long) (maxIndividualFileSizeMb * 1024 * 1024);

        log.info("DriveConfig: Initializing DriveProperties with Master Folder ID: {}", masterFolderId);
        log.info("DriveConfig: User Drive Quota: {} MB ({} bytes)", userDriveQuotaMb, maxUserSpaceBytes);
        log.info("DriveConfig: Max Individual File Size: {} MB ({} bytes)", maxIndividualFileSizeMb, maxIndividualFileSizeBytes);

        return new DriveProperties(masterFolderId, maxUserSpaceBytes, maxIndividualFileSizeBytes);
    }

    /**
     * Provides the DriveUtility bean. Since DriveUtility is a component,
     * Spring will automatically handle its dependencies. This method
     * makes sure it's available as a bean.
     *
     * @param applicationContext The Spring ApplicationContext.
     * @param driveProperties The DriveProperties bean, auto-wired by Spring.
     * @return An instance of DriveUtility.
     */
    @Bean
    public DriveUtility driveUtility(ApplicationContext applicationContext, DriveProperties driveProperties) {
        return new DriveUtility(applicationContext, driveProperties);
    }
}