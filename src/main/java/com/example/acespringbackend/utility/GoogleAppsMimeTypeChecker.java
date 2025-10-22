package com.example.acespringbackend.utility; // Choose an appropriate package for your utilities

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for handling Google Apps specific MIME types.
 */
public class GoogleAppsMimeTypeChecker {

    // A set of known Google Apps MIME types for quick lookup
    private static final Set<String> GOOGLE_APPS_MIME_TYPES;

    static {
        // Initialize the set with common Google Apps MIME types
        Set<String> types = new HashSet<>();
        types.add("application/vnd.google-apps.document");      // Google Docs
        types.add("application/vnd.google-apps.spreadsheet");   // Google Sheets
        types.add("application/vnd.google-apps.presentation");  // Google Slides
        types.add("application/vnd.google-apps.drawing");       // Google Drawings
        types.add("application/vnd.google-apps.script");        // Google Apps Script
        types.add("application/vnd.google-apps.folder");        // Google Drive Folder
        types.add("application/vnd.google-apps.form");          // Google Forms
        types.add("application/vnd.google-apps.site");          // Google Sites
        types.add("application/vnd.google-apps.jam");           // Google Jamboard
        types.add("application/vnd.google-apps.map");           // Google Maps
        // Add any other specific Google Apps MIME types you need to recognize

        GOOGLE_APPS_MIME_TYPES = Collections.unmodifiableSet(types);
    }

    /**
     * Checks if the given MIME type string corresponds to a Google Apps file type.
     *
     * @param mimeType The MIME type string to check (e.g., "application/vnd.google-apps.document").
     * @return true if the MIME type is a recognized Google Apps type, false otherwise.
     */
    public static boolean isGoogleAppsMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return GOOGLE_APPS_MIME_TYPES.contains(mimeType.toLowerCase()); // Use toLowerCase for case-insensitivity
    }

    // You could also add other utility methods here if needed, e.g., to get a friendly name
    public static String getFriendlyGoogleAppsTypeName(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return "Unknown";
        }
        switch (mimeType.toLowerCase()) {
            case "application/vnd.google-apps.document":
                return "Google Doc";
            case "application/vnd.google-apps.spreadsheet":
                return "Google Sheet";
            case "application/vnd.google-apps.presentation":
                return "Google Slide";
            case "application/vnd.google-apps.folder":
                return "Google Drive Folder";
            // Add more cases as needed
            default:
                return "Other Google App File";
        }
    }
}