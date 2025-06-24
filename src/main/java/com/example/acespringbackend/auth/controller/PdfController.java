//package com.example.acespringbackend.auth.controller;
//
//import com.adobe.pdfservices.operation.exception.ServiceApiException;
//import com.adobe.pdfservices.operation.exception.ServiceUsageException;
//import com.adobe.pdfservices.operation.pdfops.options.exportpdf.ExportPDFTargetFormat;
//import com.example.acespringbackend.service.PdfService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/pdf")
//public class PdfController {
//
//    private final PdfService pdfService;
//
//    @Autowired
//    public PdfController(PdfService pdfService) {
//        this.pdfService = pdfService;
//    }
//
//    @PostMapping("/convert")
//    public ResponseEntity<byte[]> convertPdf(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("format") String format) {
//        try {
//            ExportPDFTargetFormat targetFormat = ExportPDFTargetFormat.valueOf(format.toUpperCase());
//            byte[] convertedPdf = pdfService.convertPdf(file, targetFormat);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(getMediaTypeForFormat(targetFormat));
//            headers.setContentDispositionFormData("attachment", "converted." + format.toLowerCase());
//            return new ResponseEntity<>(convertedPdf, headers, HttpStatus.OK);
//
//        } catch (IllegalArgumentException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target format. Supported: DOCX, XLSX, PNG, JPG, HTML, RTF.");
//        } catch (IOException | ServiceApiException | ServiceUsageException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error converting PDF: " + e.getMessage(), e);
//        }
//    }
//
//    @PostMapping("/combine")
//    public ResponseEntity<byte[]> combinePdfs(@RequestParam("files") List<MultipartFile> files) {
//        if (files == null || files.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No files provided for combining.");
//        }
//        try {
//            byte[] combinedPdf = pdfService.combinePdfs(files);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("attachment", "combined.pdf");
//            return new ResponseEntity<>(combinedPdf, headers, HttpStatus.OK);
//
//        } catch (IOException | ServiceApiException | ServiceUsageException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error combining PDFs: " + e.getMessage(), e);
//        }
//    }
//
//    @PostMapping("/split")
//    public ResponseEntity<List<byte[]>> splitPdf(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "numPages", defaultValue = "1") int numPages) {
//        try {
//            List<byte[]> splitPdfs = pdfService.splitPdf(file, numPages);
//
//            // For simplicity, we're returning a list of byte arrays.
//            // In a real application, you might want to zip them or provide links to download.
//            return new ResponseEntity<>(splitPdfs, HttpStatus.OK);
//
//        } catch (IOException | ServiceApiException | ServiceUsageException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error splitting PDF: " + e.getMessage(), e);
//        }
//    }
//
//    @PostMapping("/protect")
//    public ResponseEntity<byte[]> protectPdf(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("openPassword") String openPassword,
//            @RequestParam("permissionsPassword") String permissionsPassword) {
//        try {
//            byte[] protectedPdf = pdfService.protectPdf(file, openPassword, permissionsPassword);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("attachment", "protected.pdf");
//            return new ResponseEntity<>(protectedPdf, headers, HttpStatus.OK);
//
//        } catch (IOException | ServiceApiException | ServiceUsageException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error protecting PDF: " + e.getMessage(), e);
//        }
//    
//
//    @PostMapping("/createFromHtml")
//    public ResponseEntity<byte[]> createPdfFromHtml(@RequestParam("file") MultipartFile file) {
//        try {
//            byte[] createdPdf = pdfService.createPdfFromHtml(file);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("attachment", "created_from_html.pdf");
//            return new ResponseEntity<>(createdPdf, headers, HttpStatus.OK);
//
//        } catch (IOException | ServiceApiException | ServiceUsageException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating PDF from HTML: " + e.getMessage(), e);
//        }
//    }
//
//    // Helper method to determine MediaType for response
//    private MediaType getMediaTypeForFormat(ExportPDFTargetFormat format) {
//        switch (format) {
//            case DOCX: return MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
//            case XLSX: return MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            case PNG: return MediaType.IMAGE_PNG;
//            case JPG: return MediaType.IMAGE_JPEG;
//            case HTML: return MediaType.TEXT_HTML;
//            case RTF: return MediaType.valueOf("application/rtf");
//            default: return MediaType.APPLICATION_OCTET_STREAM;
//        }
//    }
//}
package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.FileProcessResponse;
import com.example.acespringbackend.auth.dto.FileProcessRequest; // Use the renamed DTO
import com.example.acespringbackend.service.AdobePdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid; // For @Valid annotation

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File; // For java.io.File

/**
 * REST Controller for handling Adobe PDF Services related operations.
 * Exposes endpoints for processing files from URLs and downloading processed files.
 */
@RestController
@RequestMapping("/api/adobe")
public class AdobeController {

    private static final Logger log = LoggerFactory.getLogger(AdobeController.class);

    private final AdobePdfService adobePdfService;

    // Base directory for processed files (should match AdobePdfService temp path prefix)
    private final Path processedFilesBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "adobe-processed");

    public AdobeController(AdobePdfService adobePdfService) {
        this.adobePdfService = adobePdfService;
    }

    /**
     * Endpoint to trigger file processing via Adobe PDF Services by providing a file URL.
     * The backend downloads the file, processes it, and provides a download link.
     *
     * @param request The FileProcessRequest containing the URL of the file to process.
     * @return A Mono emitting an AdobeProcessResponse with details of the processed file.
     */
    @PostMapping("/process-from-url")
    public Mono<ResponseEntity<AdobeProcessResponse>> processFileFromUrl(@Valid @RequestBody FileProcessRequest request) {
        log.info("Received request to process file from URL: {}", request.getFileUrl());
        return adobePdfService.processFileFromUrl(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        log.info("File processing successful. Download URL: {}", response.getDownloadUrl());
                        return ResponseEntity.ok(response);
                    } else {
                        log.error("File processing failed: {}", response.getMessage());
                        return ResponseEntity.badRequest().body(response);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Unhandled error during file processing from URL: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new AdobeProcessResponse(false, "Internal server error during processing: " + e.getMessage(), null, null)));
                });
    }

    /**
     * Endpoint to download a processed file.
     * This endpoint serves files from the temporary 'adobe-processed' directory on the backend.
     * In a production environment, you might store these in cloud storage (e.g., S3, Google Cloud Storage)
     * and provide signed URLs instead of serving directly from the server.
     *
     * @param fileName The name of the file to download (e.g., "your_processed_file.pdf").
     * @return A ResponseEntity containing the file as a Resource.
     */
    @GetMapping("/download/{fileName}")
    public Mono<ResponseEntity<Resource>> downloadProcessedFile(@PathVariable String fileName) {
        return Mono.fromCallable(() -> {
            Path filePath = processedFilesBaseDir.resolve(fileName).normalize();
            log.info("Attempting to serve file for download: {}", filePath);

            if (!filePath.startsWith(processedFilesBaseDir)) {
                // Security check: Prevent directory traversal attacks
                log.warn("Attempted directory traversal detected for file: {}", fileName);
                return ResponseEntity.badRequest().build();
            }

            try {
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() || resource.isReadable()) {
                    String contentType = Files.probeContentType(filePath);
                    if (contentType == null) {
                        contentType = "application/octet-stream"; // Default content type
                    }

                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                            .body(resource);
                } else {
                    log.warn("File not found or not readable: {}", fileName);
                    return ResponseEntity.notFound().build();
                }
            } catch (MalformedURLException e) {
                log.error("Malformed URL for file {}: {}", fileName, e.getMessage(), e);
                return ResponseEntity.badRequest().build();
            } catch (IOException e) {
                log.error("Error reading file {}: {}", fileName, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}