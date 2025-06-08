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
//    }
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