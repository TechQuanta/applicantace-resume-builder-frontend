//package com.example.acespringbackend.service;
//
//import com.adobe.pdfservices.operation.ExecutionContext;
//import com.adobe.pdfservices.operation.auth.Credentials;
//import com.adobe.pdfservices.operation.exception.ServiceApiException;
//import com.adobe.pdfservices.operation.exception.ServiceUsageException;
//import com.adobe.pdfservices.operation.io.FileRef;
//import com.adobe.pdfservices.operation.pdfops.CombineFilesOperation;
//import com.adobe.pdfservices.operation.pdfops.CreatePDFOperation;
//import com.adobe.pdfservices.operation.pdfops.ExportPDFOperation;
//import com.adobe.pdfservices.operation.pdfops.LinearizePDFOperation;
//import com.adobe.pdfservices.operation.pdfops.ProtectPDFOperation;
//import com.adobe.pdfservices.operation.pdfops.SplitPDFOperation;
//import com.adobe.pdfservices.operation.pdfops.options.exportpdf.ExportPDFTargetFormat;
//import com.adobe.pdfservices.operation.pdfops.options.createpdf.CreatePDFOptions;
//import com.adobe.pdfservices.operation.pdfops.options.createpdf.CreatePDFFromHTMPOptions; // For HTML to PDF
//import com.adobe.pdfservices.operation.pdfops.options.protectpdf.PasswordProtectionOption;
//import com.adobe.pdfservices.operation.pdfops.options.protectpdf.ProtectPDFOptions;
//import com.adobe.pdfservices.operation.pdfops.options.splitpdf.SplitPDFOptions;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardCopyOption;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//public class PdfService {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(PdfService.class);
//
//    @Value("${adobe.pdfservices.credentials.json}")
//    private String credentialsJsonPath;
//
//    @Value("${adobe.pdfservices.privatekey}")
//    private String privateKeyPath;
//
//    private Credentials credentials;
//
//    @PostConstruct
//    public void init() {
//        try {
//            // Load credentials from resources
//            File credentialsFile = new ClassPathResource(credentialsJsonPath).getFile();
//            File privateKeyFile = new ClassPathResource(privateKeyPath).getFile();
//
//            this.credentials = Credentials.serviceAccountCredentialsBuilder()
//                    .fromFile(credentialsFile.getAbsolutePath())
//                    .withPrivateKey(privateKeyFile.getAbsolutePath())
//                    .build();
//
//            LOGGER.info("Adobe PDF Services credentials loaded successfully.");
//        } catch (IOException e) {
//            LOGGER.error("Failed to load Adobe PDF Services credentials: " + e.getMessage(), e);
//            throw new RuntimeException("Failed to load Adobe PDF Services credentials", e);
//        }
//    }
//
//    private ExecutionContext createExecutionContext() {
//        return ExecutionContext.create(credentials);
//    }
//
//    /**
//     * Converts a PDF to a specified target format (e.g., DOCX, XLSX, PNG, JPG).
//     *
//     * @param pdfFile      The input PDF file.
//     * @param targetFormat The desired output format (e.g., ExportPDFTargetFormat.DOCX).
//     * @return A byte array of the converted file.
//     * @throws IOException
//     * @throws ServiceApiException
//     * @throws ServiceUsageException
//     */
//    public byte[] convertPdf(MultipartFile pdfFile, ExportPDFTargetFormat targetFormat) throws IOException, ServiceApiException, ServiceUsageException {
//        Path tempInputPath = Files.createTempFile("input-pdf-", ".pdf");
//        Files.copy(pdfFile.getInputStream(), tempInputPath, StandardCopyOption.REPLACE_EXISTING);
//        FileRef inputPdf = FileRef.createFromLocalFile(tempInputPath.toString());
//
//        ExportPDFOperation exportPDFOperation = ExportPDFOperation.createNew(targetFormat);
//        exportPDFOperation.setInput(inputPdf);
//
//        FileRef result = exportPDFOperation.execute(createExecutionContext());
//
//        byte[] outputBytes = result.readAllBytes();
//        Files.deleteIfExists(tempInputPath);
//        return outputBytes;
//    }
//
//    /**
//     * Combines multiple PDF files into a single PDF.
//     *
//     * @param pdfFiles A list of input PDF files.
//     * @return A byte array of the combined PDF.
//     * @throws IOException
//     * @throws ServiceApiException
//     * @throws ServiceUsageException
//     */
//    public byte[] combinePdfs(List<MultipartFile> pdfFiles) throws IOException, ServiceApiException, ServiceUsageException {
//        CombineFilesOperation combineFilesOperation = CombineFilesOperation.createNew();
//        List<Path> tempPaths = new ArrayList<>();
//
//        for (MultipartFile file : pdfFiles) {
//            Path tempPath = Files.createTempFile("input-combine-", ".pdf");
//            Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);
//            combineFilesOperation.addInput(FileRef.createFromLocalFile(tempPath.toString()));
//            tempPaths.add(tempPath);
//        }
//
//        FileRef result = combineFilesOperation.execute(createExecutionContext());
//        byte[] outputBytes = result.readAllBytes();
//
//        for (Path tempPath : tempPaths) {
//            Files.deleteIfExists(tempPath);
//        }
//        return outputBytes;
//    }
//
//    /**
//     * Splits a PDF into multiple PDFs based on page ranges or number of pages.
//     *
//     * @param pdfFile    The input PDF file.
//     * @param numPages   The number of pages per split file (e.g., 1 to split into single-page PDFs).
//     * @return A list of byte arrays, each representing a split PDF.
//     * @throws IOException
//     * @throws ServiceApiException
//     * @throws ServiceUsageException
//     */
//    public List<byte[]> splitPdf(MultipartFile pdfFile, int numPages) throws IOException, ServiceApiException, ServiceUsageException {
//        Path tempInputPath = Files.createTempFile("input-split-", ".pdf");
//        Files.copy(pdfFile.getInputStream(), tempInputPath, StandardCopyOption.REPLACE_EXISTING);
//        FileRef inputPdf = FileRef.createFromLocalFile(tempInputPath.toString());
//
//        SplitPDFOperation splitPDFOperation = SplitPDFOperation.createNew();
//        splitPDFOperation.setInput(inputPdf);
//        splitPDFOperation.setOptions(SplitPDFOptions.pageCount(numPages)); // Split into `numPages` pages per file
//
//        List<FileRef> result = splitPDFOperation.execute(createExecutionContext());
//        List<byte[]> splitPdfBytes = new ArrayList<>();
//        for (FileRef fileRef : result) {
//            splitPdfBytes.add(fileRef.readAllBytes());
//        }
//
//        Files.deleteIfExists(tempInputPath);
//        return splitPdfBytes;
//    }
//
//    /**
//     * Protects a PDF with an open password and a permissions password.
//     *
//     * @param pdfFile          The input PDF file.
//     * @param openPassword     Password required to open the PDF.
//     * @param permissionsPassword Password required to change permissions.
//     * @return A byte array of the protected PDF.
//     * @throws IOException
//     * @throws ServiceApiException
//     * @throws ServiceUsageException
//     */
//    public byte[] protectPdf(MultipartFile pdfFile, String openPassword, String permissionsPassword) throws IOException, ServiceApiException, ServiceUsageException {
//        Path tempInputPath = Files.createTempFile("input-protect-", ".pdf");
//        Files.copy(pdfFile.getInputStream(), tempInputPath, StandardCopyOption.REPLACE_EXISTING);
//        FileRef inputPdf = FileRef.createFromLocalFile(tempInputPath.toString());
//
//        ProtectPDFOperation protectPDFOperation = ProtectPDFOperation.createNew();
//        protectPDFOperation.setInput(inputPdf);
//
//        // Define permissions (e.g., disable printing, copying, editing)
//        ProtectPDFOptions protectPDFOptions = ProtectPDFOptions.passwordProtectionOptionsBuilder()
//                .withOpenPassword(openPassword)
//                .withPermissionsPassword(permissionsPassword)
//                .withPermission(PasswordProtectionOption.PrintPermission.NOT_ALLOWED)
//                .withPermission(PasswordProtectionOption.EditPermission.NOT_ALLOWED)
//                .withPermission(PasswordProtectionOption.CopyContentPermission.NOT_ALLOWED)
//                .build();
//        protectPDFOperation.setOptions(protectPDFOptions);
//
//        FileRef result = protectPDFOperation.execute(createExecutionContext());
//        byte[] outputBytes = result.readAllBytes();
//
//        Files.deleteIfExists(tempInputPath);
//        return outputBytes;
//    }
//
//    /**
//     * Creates a PDF from an HTML file.
//     *
//     * @param htmlFile The input HTML file.
//     * @return A byte array of the created PDF.
//     * @throws IOException
//     * @throws ServiceApiException
//     * @throws ServiceUsageException
//     */
//    public byte[] createPdfFromHtml(MultipartFile htmlFile) throws IOException, ServiceApiException, ServiceUsageException {
//        Path tempInputPath = Files.createTempFile("input-html-", ".html");
//        Files.copy(htmlFile.getInputStream(), tempInputPath, StandardCopyOption.REPLACE_EXISTING);
//        FileRef inputHtml = FileRef.createFromLocalFile(tempInputPath.toString());
//
//        CreatePDFOperation createPDFOperation = CreatePDFOperation.createNew();
//        createPDFOperation.setInput(inputHtml);
//
//        FileRef result = createPDFOperation.execute(createExecutionContext());
//        byte[] outputBytes = result.readAllBytes();
//
//        Files.deleteIfExists(tempInputPath);
//        return outputBytes;
//    }
//
//
//    // --- Helper method for temporarily storing files ---
//    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
//        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename());
//        try (InputStream is = file.getInputStream()) {
//            Files.copy(is, convFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        }
//        return convFile;
//    }
//}
package com.example.acespringbackend.service;

import com.adobe.pdfservices.operation.ExecutionContext;
import com.adobe.pdfservices.operation.auth.Credentials;
import com.adobe.pdfservices.operation.auth.ServicePrincipalCredentials;
import com.adobe.pdfservices.operation.exception.ServiceApiException;
import com.adobe.pdfservices.operation.exception.ServiceUsageException;
import com.adobe.pdfservices.operation.io.FileRef;
import com.adobe.pdfservices.operation.io.StreamRef;
import com.adobe.pdfservices.operation.pdfops.CreatePDFOperation;
import com.adobe.pdfservices.operation.pdfops.options.createpdf.CreatePDFOptions;
import com.adobe.pdfservices.operation.pdfops.options.createpdf.CreatePDFFromHTMLOptions;
import com.adobe.pdfservices.operation.pdfops.options.createpdf.SupportedSourceFormat;
import com.example.acespringbackend.auth.dto.AdobeProcessResponse;
import com.example.acespringbackend.auth.dto.FileProcessRequest; // Use the renamed DTO

import org.apache.commons.io.IOUtils; // For IOUtils.toByteArray
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service for interacting with Adobe PDF Services API.
 * This service handles downloading files from external URLs, processing them
 * (e.g., converting to PDF), and managing temporary files.
 */
@Service
public class AdobePdfService {

    private static final Logger log = LoggerFactory.getLogger(AdobePdfService.class);

    @Value("${adobe.pdfservices.client-id}")
    private String clientId;

    @Value("${adobe.pdfservices.client-secret}")
    private String clientSecret;

    @Value("${adobe.pdfservices.private-key-path}")
    private String privateKeyPath;

    @Value("${adobe.pdfservices.default-input-extension:pdf}")
    private String defaultInputExtension;

    private Credentials credentials;
    private final WebClient webClient;

    public AdobePdfService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Initializes Adobe PDF Services credentials after properties are injected.
     */
    @PostConstruct
    public void init() {
        try {
            // Read private key from classpath or file system
            InputStream privateKeyInputStream = getClass().getClassLoader().getResourceAsStream(privateKeyPath.replace("classpath:", ""));
            if (privateKeyInputStream == null) {
                // Try as a file path if not found in classpath
                java.io.File privateKeyFile = new java.io.File(privateKeyPath);
                if (privateKeyFile.exists() && privateKeyFile.isFile()) {
                    privateKeyInputStream = new FileInputStream(privateKeyFile);
                } else {
                    throw new IOException("Adobe private.key file not found at " + privateKeyPath);
                }
            }
            this.credentials = ServicePrincipalCredentials.builder()
                    .withClientId(clientId)
                    .withClientSecret(clientSecret)
                    .withPrivateKey(privateKeyInputStream)
                    .build();
            log.info("Adobe PDF Services credentials initialized successfully.");
        } catch (IOException e) {
            log.error("Failed to load Adobe PDF Services private key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load Adobe private key", e);
        }
    }

    /**
     * Processes a file by first downloading it from the provided URL, then
     * converting it to PDF using Adobe PDF Services, and finally storing
     * the result temporarily.
     *
     * @param request The FileProcessRequest containing the file URL and desired output format.
     * @return A Mono emitting an AdobeProcessResponse with the download URL of the processed file.
     */
    public Mono<AdobeProcessResponse> processFileFromUrl(FileProcessRequest request) {
        return downloadFile(request.getFileUrl())
                .flatMap(downloadedFilePath -> {
                    log.info("Successfully downloaded file from URL: {}", request.getFileUrl());
                    String originalFileName = Paths.get(request.getFileUrl()).getFileName().toString();
                    String inputExtension = getFileExtension(originalFileName);

                    // Determine the Adobe operation based on input/output formats
                    if (inputExtension.equalsIgnoreCase("html") || request.getOutputFormat().equalsIgnoreCase("pdf")) {
                        // Example: Convert HTML to PDF or other supported conversions
                        // For simplicity, this example assumes converting to PDF.
                        // You can extend this with more operations (e.g., ExportPDF, OCR, etc.)
                        return convertToPdf(downloadedFilePath, inputExtension, originalFileName);
                    } else {
                        // Handle other generic processing or throw an error if unsupported
                        log.error("Unsupported file processing: Input extension {} or Output format {}", inputExtension, request.getOutputFormat());
                        try {
                            Files.deleteIfExists(downloadedFilePath); // Clean up temp file
                        } catch (IOException e) {
                            log.error("Failed to delete temp file: {}", downloadedFilePath, e);
                        }
                        return Mono.just(new AdobeProcessResponse(false,
                                "Unsupported file processing operation.", null, null));
                    }
                })
                .doOnError(e -> log.error("Error during file processing: {}", e.getMessage(), e))
                .onErrorResume(e -> Mono.just(new AdobeProcessResponse(false,
                        "Failed to process file: " + e.getMessage(), null, null)));
    }

    /**
     * Downloads a file from a given URL to a temporary location.
     *
     * @param fileUrl The URL of the file to download.
     * @return A Mono emitting the Path to the downloaded temporary file.
     */
    private Mono<Path> downloadFile(String fileUrl) {
        return Mono.fromCallable(() -> {
            log.info("Attempting to download file from URL: {}", fileUrl);
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "adobe-temp");
            Files.createDirectories(tempDir); // Ensure temp directory exists

            String fileName = UUID.randomUUID().toString() + "-" + Paths.get(fileUrl).getFileName().toString();
            Path tempFilePath = tempDir.resolve(fileName);

            return webClient.get()
                    .uri(fileUrl)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .map(DataBuffer::asByteBuffer)
                    .collect(
                            () -> {
                                try {
                                    return Files.newByteChannel(tempFilePath,
                                            java.nio.file.StandardOpenOption.CREATE,
                                            java.nio.file.StandardOpenOption.WRITE,
                                            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                                } catch (IOException e) {
                                    throw new RuntimeException("Error opening file channel", e);
                                }
                            },
                            (channel, buffer) -> {
                                try {
                                    while (buffer.hasRemaining()) {
                                        channel.write(buffer);
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException("Error writing to file channel", e);
                                }
                            })
                    .doOnSuccess(channel -> {
                        try {
                            channel.close();
                            log.info("File downloaded to temporary path: {}", tempFilePath);
                        } catch (IOException e) {
                            log.error("Error closing file channel: {}", e.getMessage());
                        }
                    })
                    .thenReturn(tempFilePath)
                    .subscribeOn(Schedulers.boundedElastic()) // Offload I/O to a dedicated scheduler
                    .toFuture().get(); // Blocking for simplicity in Mono.fromCallable, consider pure reactive chain
        }).subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Failed to download file from {}: {}", fileUrl, e.getMessage(), e));
    }


    /**
     * Converts an input file (from a temporary path) to PDF using Adobe PDF Services.
     *
     * @param inputFilePath Path to the downloaded input file.
     * @param inputExtension The original extension of the input file (e.g., "html", "docx", "jpeg").
     * @param originalFileName The original name of the file before processing.
     * @return A Mono emitting an AdobeProcessResponse with the download URL of the processed PDF.
     */
    private Mono<AdobeProcessResponse> convertToPdf(Path inputFilePath, String inputExtension, String originalFileName) {
        return Mono.fromCallable(() -> {
            log.info("Converting file {} to PDF using Adobe PDF Services. Input Extension: {}", originalFileName, inputExtension);
            Path outputFilePath = null;
            try {
                ExecutionContext executionContext = ExecutionContext.create(credentials);

                FileRef inputFile = null;
                CreatePDFOperation createPDFOperation;

                // Determine the input format for CreatePDFOperation
                SupportedSourceFormat sourceFormat;
                switch (inputExtension.toLowerCase()) {
                    case "docx":
                    case "doc":
                        sourceFormat = SupportedSourceFormat.DOCX;
                        break;
                    case "pptx":
                    case "ppt":
                        sourceFormat = SupportedSourceFormat.PPTX;
                        break;
                    case "xlsx":
                    case "xls":
                        sourceFormat = SupportedSourceFormat.XLSX;
                        break;
                    case "jpeg":
                    case "jpg":
                        sourceFormat = SupportedSourceFormat.JPEG;
                        break;
                    case "png":
                        sourceFormat = SupportedSourceFormat.PNG;
                        break;
                    case "tiff":
                    case "tif":
                        sourceFormat = SupportedSourceFormat.TIFF;
                        break;
                    case "bmp":
                        sourceFormat = SupportedSourceFormat.BMP;
                        break;
                    case "gif":
                        sourceFormat = SupportedSourceFormat.GIF;
                        break;
                    // Add more cases as supported by Adobe PDF Services SDK for CreatePDFOperation
                    case "html":
                        // For HTML, use CreatePDFFromHTMLOptions
                        inputFile = FileRef.createFromFile(inputFilePath.toAbsolutePath().toString());
                        CreatePDFFromHTMLOptions htmlOptions = CreatePDFFromHTMLOptions.htmlOptionsBuilder().build();
                        createPDFOperation = CreatePDFOperation.createNew().fromHTML(inputFile, htmlOptions);
                        break;
                    case "pdf": // If input is already PDF, simply use it as input for other ops or return as is
                        log.info("Input file is already PDF. No conversion needed for CreatePDFOperation.");
                        inputFile = FileRef.createFromFile(inputFilePath.toAbsolutePath().toString());
                        // If the goal is just to serve the downloaded PDF, this might be a direct return point.
                        // For now, we'll let it pass through CreatePDFOperation as a PDF input, which is less common but valid.
                        createPDFOperation = CreatePDFOperation.createNew().fromPDF(inputFile);
                        break;
                    default:
                        // Fallback or error for unsupported formats
                        log.warn("Unsupported input extension '{}' for CreatePDFOperation. Attempting with default PDF import or throwing error.", inputExtension);
                        // If it's a generic file and you still want to try to convert,
                        // you'd typically need to specify the source format if not auto-detectable.
                        // For simplicity, we'll assume PDF input or throw error.
                        // If the file is genuinely unknown, Adobe might reject it.
                        throw new IllegalArgumentException("Unsupported input file format: " + inputExtension);
                }

                if (!inputExtension.equalsIgnoreCase("html") && !inputExtension.equalsIgnoreCase("pdf")) {
                    inputFile = FileRef.createFromFile(inputFilePath.toAbsolutePath().toString(), sourceFormat);
                    CreatePDFOptions createPdfOptions = CreatePDFOptions.builder().build(); // Basic options
                    createPDFOperation = CreatePDFOperation.createNew().fromFileRef(inputFile, createPdfOptions);
                }

                // If input extension was HTML, createPDFOperation would be initialized above.
                // If it was another format, it should also be initialized above.
                if (createPDFOperation == null) {
                    throw new IllegalStateException("Adobe PDF Services operation could not be initialized for input extension: " + inputExtension);
                }

                // Execute the operation
                FileRef result = createPDFOperation.execute(executionContext);

                // Save the result to a temporary file
                String outputFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + ".pdf";
                Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "adobe-processed");
                Files.createDirectories(tempDir);
                outputFilePath = tempDir.resolve(outputFileName);

                result.saveAsFile(outputFilePath.toAbsolutePath().toString());
                log.info("Processed file saved to: {}", outputFilePath);

                // For frontend download, we need to serve this file.
                // This example returns a direct path, but in a real app, you'd
                // serve this via a dedicated Spring endpoint that streams this file.
                // For simplicity, we'll return a placeholder URL. In your controller,
                // you would map /download/{fileName} to serve files from this temp directory.
                String downloadUrl = "/api/adobe/download/" + outputFilePath.getFileName().toString();

                return new AdobeProcessResponse(true, "File processed successfully!", outputFileName, downloadUrl);

            } catch (ServiceApiException | ServiceUsageException | IOException | IllegalArgumentException e) {
                log.error("Error processing file with Adobe PDF Services for {}: {}", originalFileName, e.getMessage(), e);
                return new AdobeProcessResponse(false, "Adobe PDF Services processing failed: " + e.getMessage(), null, null);
            } finally {
                // Clean up the input temporary file
                try {
                    Files.deleteIfExists(inputFilePath);
                    if (outputFilePath != null) {
                        // Keep output file for download for a short period, or manage its deletion
                        // For production, consider a scheduled cleanup task for temp files
                        log.info("Input temp file cleaned up: {}", inputFilePath);
                    }
                } catch (IOException e) {
                    log.warn("Failed to delete temporary input file {}: {}", inputFilePath, e.getMessage());
                }
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Extracts the file extension from a given filename.
     * @param fileName The full file name.
     * @return The file extension (e.g., "pdf", "docx"), or an empty string if no extension.
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return defaultInputExtension; // Fallback to a default if no extension
    }
}