package com.example.acespringbackend.utility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting text content from PDF documents.
 * It provides methods to extract full text and to parse specific sections
 * like 'experience' and 'skills' from the extracted text, typically from resumes.
 */
public class PdfTextExtractor {

    /**
     * Extracts all text content from a given PDF byte array.
     * This method is suitable for processing PDF files received as raw bytes,
     * for example, from a web upload in a reactive environment.
     *
     * @param fileBytes The byte array representing the PDF file.
     * @return A String containing all extracted text from the PDF.
     * @throws IOException If an error occurs during PDF parsing or text extraction.
     */
    public static String extractText(byte[] fileBytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes);
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Extracts text from a PDF byte array twice and performs a basic verification
     * by comparing the lengths of the two extracted texts.
     * If the lengths are very similar (difference less than 5 characters),
     * it returns the first extracted text, indicating a reliable extraction.
     * Otherwise, it returns an empty string, suggesting an unreliable extraction.
     *
     * @param bytes The byte array representing the PDF file.
     * @return The extracted text if deemed reliable, otherwise an empty string.
     * @throws IOException If an error occurs during PDF parsing or text extraction.
     */
    public static String extractTextTwiceAndVerify(byte[] bytes) throws IOException {
        String text1 = extractText(bytes);
        // It's good practice to create a new InputStream for the second read,
        // though extractText itself handles stream creation from bytes.
        String text2 = extractText(bytes); // Re-use the extractText method with the same bytes

        // Perform a simple length comparison for consistency check
        if (Math.abs(text1.length() - text2.length()) < 5) {
            return text1;
        } else {
            // Returning an empty string implies an issue with consistent extraction.
            // Consider throwing a custom exception here for clearer error handling
            // in calling services, e.g., throw new PdfExtractionException("PDF content extraction was inconsistent.");
            return "";
        }
    }

    /**
     * Extracts specific content blocks (like 'experience' and 'skills') from a given full text,
     * typically obtained from a resume PDF. It uses regular expressions to identify common section headings
     * and capture the content under them.
     *
     * @param fullText The complete text extracted from a PDF.
     * @return A Map where keys are block names ("experience", "skills") and values are the extracted content
     * for those blocks. Returns empty strings for blocks not found.
     */
    public static Map<String, String> extractSpecificBlocks(String fullText) {
        Map<String, String> blocks = new HashMap<>();
        // Initialize with empty strings to ensure keys are always present
        blocks.put("experience", "");
        blocks.put("skills", "");

        // Normalize whitespace and newlines for consistent regex matching
        String normalizedText = fullText.replaceAll("\\s*\\n\\s*", " ").trim();

        // Regex to identify common resume section headings (case-insensitive)
        // This pattern uses a lookahead assertion to match content up to the next heading or end of string.
        String sectionHeadings = "(Objective|Summary|Experience|Work Experience|Professional Experience|Skills|Technical Skills|Core Competencies|Education|Projects|Certifications|Awards|Publications|Volunteer Experience|References)";
        Pattern blockPattern = Pattern.compile("(?i)\\b(" + sectionHeadings + ")\\b\\s*(.*?)(?=\\b" + sectionHeadings + "\\b|$)", Pattern.DOTALL);
        Matcher matcher = blockPattern.matcher(normalizedText);

        // Temporarily store all found sections by their exact heading
        Map<String, String> tempBlocks = new HashMap<>();
        while (matcher.find()) {
            String heading = matcher.group(1);
            String content = matcher.group(2).trim();
            tempBlocks.put(heading, content);
        }

        // Map the extracted sections to the standardized "experience" and "skills" keys
        for (Map.Entry<String, String> entry : tempBlocks.entrySet()) {
            String heading = entry.getKey().toLowerCase();
            String content = entry.getValue();

            if (heading.contains("experience") || heading.contains("work experience") || heading.contains("professional experience")) {
                blocks.put("experience", content);
            } else if (heading.contains("skills") || heading.contains("technical skills") || heading.contains("core competencies") || heading.contains("expertise")) {
                blocks.put("skills", content);
            }
        }

        return blocks;
    }
}