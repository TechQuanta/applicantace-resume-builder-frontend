package com.example.acespringbackend.utility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfTextExtractor {

    /**
     * Extracts text from a PDF file.
     *
     * @param file The MultipartFile representing the PDF.
     * @return The extracted text.
     * @throws IOException If there's an error reading the PDF.
     */
    public static String extractText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream(); PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Extracts text from a PDF file twice and compares character counts to ensure proper extraction.
     *
     * @param file The MultipartFile representing the PDF.
     * @return The extracted text, or an empty string if extraction is unreliable.
     * @throws IOException If there's an error reading the PDF.
     */
    public static String extractTextTwiceAndVerify(MultipartFile file) throws IOException {
        String text1 = extractText(file);
        try (InputStream inputStream = file.getInputStream(); PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text2 = stripper.getText(document);

            if (Math.abs(text1.length() - text2.length()) < 5) {
                System.out.println("PDF extracted consistently. Length: " + text1.length());
                return text1;
            } else {
                System.err.println("PDF extraction inconsistent. Length 1: " + text1.length() + ", Length 2: " + text2.length());
                return "";
            }
        }
    }

    /**
     * Extracts specific blocks (Experience, Skills) from the PDF text based on common headings.
     * This is a heuristic approach and might not work perfectly for all resume formats.
     * It attempts to capture content between a recognized heading and the next likely heading.
     *
     * @param fullText The full extracted text from the PDF.
     * @return A Map containing extracted blocks, e.g., {"experience": "...", "skills": "..."}.
     * Blocks not found will have empty strings as values.
     */
    public static Map<String, String> extractSpecificBlocks(String fullText) {
        System.out.println("\n--- Full PDF Text Received by PdfTextExtractor.extractSpecificBlocks ---");
        System.out.println("\"\"\"\n" + fullText + "\n\"\"\"");
        System.out.println("-----------------------------------------------------------------------\n");

        Map<String, String> blocks = new HashMap<>();
        blocks.put("experience", "");
        blocks.put("skills", ""); // Corrected to "skills" (plural)

        String normalizedText = fullText.replaceAll("\\s*\\n\\s*", " ").trim();

        String sectionHeadings = "(Objective|Summary|Experience|Work Experience|Professional Experience|Skills|Technical Skills|Core Competencies|Education|Projects|Certifications|Awards|Publications|Volunteer Experience|References)";

        Pattern blockPattern = Pattern.compile("(?i)\\b(" + sectionHeadings + ")\\b\\s*(.*?)(?=\\b" + sectionHeadings + "\\b|$)", Pattern.DOTALL);

        Matcher matcher = blockPattern.matcher(normalizedText);

        Map<String, String> tempBlocks = new HashMap<>();
        while (matcher.find()) {
            String heading = matcher.group(1);
            String content = matcher.group(2).trim();
            tempBlocks.put(heading, content);
        }

        for (Map.Entry<String, String> entry : tempBlocks.entrySet()) {
            String heading = entry.getKey().toLowerCase();
            String content = entry.getValue();

            if (heading.contains("experience") || heading.contains("work experience") || heading.contains("professional experience")) {
                blocks.put("experience", content);
            } else if (heading.contains("skills") || heading.contains("technical skills") || heading.contains("core competencies") || heading.contains("expertise")) {
                blocks.put("skills", content);
            }
        }

        System.out.println("\n--- Extracted Resume Blocks (from PdfTextExtractor) ---");
        System.out.println("Experience Block: \n\"\"\"\n" + blocks.get("experience") + "\n\"\"\"");
        System.out.println("Skills Block: \n\"\"\"\n" + blocks.get("skills") + "\n\"\"\"");
        System.out.println("-------------------------------------------------------\n");

        return blocks;
    }
}