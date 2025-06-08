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