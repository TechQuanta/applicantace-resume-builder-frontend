package com.example.acespringbackend.utility;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to map common MIME types to file extensions.
 * This is helpful for determining the correct file extension when exporting files.
 */
public class MimeTypeMap {

    private static final Map<String, String> MIME_TYPE_TO_EXTENSION = new HashMap<>();

    static {
        // Common document formats
        MIME_TYPE_TO_EXTENSION.put("application/pdf", "pdf");
        MIME_TYPE_TO_EXTENSION.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        MIME_TYPE_TO_EXTENSION.put("application/vnd.oasis.opendocument.text", "odt");
        MIME_TYPE_TO_EXTENSION.put("application/rtf", "rtf");
        MIME_TYPE_TO_EXTENSION.put("text/plain", "txt");
        MIME_TYPE_TO_EXTENSION.put("text/html", "html");
        MIME_TYPE_TO_EXTENSION.put("application/zip", "zip"); // For HTML Zipped

        // Spreadsheet formats
        MIME_TYPE_TO_EXTENSION.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
        MIME_TYPE_TO_EXTENSION.put("application/vnd.oasis.opendocument.spreadsheet", "ods");
        MIME_TYPE_TO_EXTENSION.put("text/csv", "csv");

        // Presentation formats
        MIME_TYPE_TO_EXTENSION.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
        MIME_TYPE_TO_EXTENSION.put("application/vnd.oasis.opendocument.presentation", "odp");

        // Image formats (examples)
        MIME_TYPE_TO_EXTENSION.put("image/jpeg", "jpg");
        MIME_TYPE_TO_EXTENSION.put("image/png", "png");
        MIME_TYPE_TO_EXTENSION.put("image/gif", "gif");
        MIME_TYPE_TO_EXTENSION.put("image/bmp", "bmp");
        MIME_TYPE_TO_EXTENSION.put("image/tiff", "tiff");
        MIME_TYPE_TO_EXTENSION.put("image/webp", "webp");
        MIME_TYPE_TO_EXTENSION.put("image/svg+xml", "svg");
    }

    /**
     * Returns the default file extension for a given MIME type.
     *
     * @param mimeType The MIME type (e.g., "application/pdf").
     * @return The corresponding file extension (e.g., "pdf"), or "bin" if not found.
     */
    public static String getDefaultExtensionFromMimeType(String mimeType) {
        return MIME_TYPE_TO_EXTENSION.getOrDefault(mimeType, "bin"); // Default to binary if no specific extension
    }

	public static boolean isGoogleAppsMimeType(String mimeType) {
		// TODO Auto-generated method stub
		return false;
	}
}
