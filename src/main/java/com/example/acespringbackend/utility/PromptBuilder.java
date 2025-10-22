package com.example.acespringbackend.utility;

import com.example.acespringbackend.auth.dto.ParaphrasingRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PromptBuilder {

    private static final String PARAPHRASE_INSTRUCTION = "Paraphrase the following text";
    private static final String CL_GENERATION_INSTRUCTION = "Generate a professional cover letter.";
    private static final String TEXT_MARKER_START = "\n\"\"\"\n";
    private static final String TEXT_MARKER_END = "\n\"\"\"\n";

    public String buildPrompt(ParaphrasingRequest req) {
        StringBuilder prompt = new StringBuilder();

        // --- Auto Cover Letter Mode ---
        if (Boolean.TRUE.equals(req.getAutoCoverLetterMode())) {
            prompt.append(CL_GENERATION_INSTRUCTION);

            boolean hasJobDescription = req.getJobDescription() != null && !req.getJobDescription().trim().isEmpty();
            boolean hasInputContent = req.getInput() != null && !req.getInput().trim().isEmpty();

            if (hasJobDescription) {
                prompt.append(" Tailor it specifically for the following job description:").append(TEXT_MARKER_START)
                        .append(req.getJobDescription()).append(TEXT_MARKER_END);
            }

            if (hasInputContent) {
                // For CL mode, input is treated as resume highlights or key info
                prompt.append(" Here is some key information or resume highlights to use:").append(TEXT_MARKER_START)
                        .append(req.getInput()).append(TEXT_MARKER_END);
            } else if (!hasJobDescription) {
                // If neither JD nor input highlights are provided, it's an invalid request for CL mode
                throw new IllegalArgumentException("Oops! To generate a cover letter, please provide either a job description or some key information/resume highlights. We need something to work with!");
            }

            Optional.ofNullable(req.getTone())
                    .filter(s -> !s.isEmpty())
                    .ifPresent(tone -> prompt.append(" Maintain a ").append(tone).append(" tone."));
            Optional.ofNullable(req.getStyle())
                    .filter(s -> !s.isEmpty())
                    .ifPresent(style -> prompt.append(" Use a ").append(style).append(" writing style."));
            Optional.ofNullable(req.getWordLimit())
                    .ifPresent(limit -> prompt.append(" Keep the letter around ").append(limit).append(" words."));

            prompt.append("\nEnsure the cover letter is persuasive, well-structured, and highlights relevant skills/experiences effectively.");

            // Suggestions for Cover Letter Mode
            if (Boolean.TRUE.equals(req.getEnableSuggestions())) {
                if (hasJobDescription) {
                    prompt.append("\n\nBold and italicize any terms from the provided job description that appear relevant in the output.");
                } else if (req.getKeywords() != null && !req.getKeywords().trim().isEmpty()) {
                    prompt.append("\n\nBold and italicize any of the following keywords present in the output: ").append(req.getKeywords()).append(".");
                } else {
                    prompt.append("\n\nBold and italicize important keywords within the generated content.");
                }
            }

            return prompt.toString().trim();
        }

        // --- General Paraphrasing / Chat Mode (if not Auto Cover Letter Mode) ---

        // General validation for input in non-CL mode
        if (req.getInput() == null || req.getInput().trim().isEmpty()) {
            throw new IllegalArgumentException("It looks like your input text is empty! Please provide some text for paraphrasing or analysis.");
        }

        prompt.append(PARAPHRASE_INSTRUCTION);

        Optional.ofNullable(req.getTone())
                .filter(s -> !s.isEmpty())
                .ifPresent(tone -> prompt.append(" in a ").append(tone).append(" tone"));

        Optional.ofNullable(req.getStyle())
                .filter(s -> !s.isEmpty())
                .ifPresent(style -> prompt.append(" using a ").append(style).append(" style"));

        prompt.append(":").append(TEXT_MARKER_START).append(req.getInput()).append(TEXT_MARKER_END);

        // Researched Mode with Job Description
        if (Boolean.TRUE.equals(req.getResearchedMode()) && req.getJobDescription() != null && !req.getJobDescription().trim().isEmpty()) {
            prompt.append("\n\nCritically analyze and enhance the paraphrased text by incorporating or emphasizing terms relevant to the following job description. Focus on making the text more impactful and aligned with the job's requirements:");
            prompt.append(TEXT_MARKER_START).append(req.getJobDescription()).append(TEXT_MARKER_END);
            prompt.append("Ensure the output remains a paraphrase of the original text but is optimized for the job description.");
        } else {
            // Keywords for general paraphrasing (only if not in researched mode with JD)
            Optional.ofNullable(req.getKeywords())
                    .filter(s -> !s.isEmpty())
                    .ifPresent(kw -> prompt.append("\n\nEnsure the paraphrase incorporates these keywords: ").append(kw).append("."));
        }

        Optional.ofNullable(req.getWordLimit())
                .ifPresent(limit -> prompt.append("\n\nKeep the paraphrased output to approximately ").append(limit).append(" words."));

        // Suggestions for General Paraphrasing Mode
        if (Boolean.TRUE.equals(req.getEnableSuggestions())) {
            if (req.getKeywords() != null && !req.getKeywords().trim().isEmpty()) {
                prompt.append("\n\nBold and italicize any of the following keywords present in the output: ").append(req.getKeywords()).append(".");
            } else if (Boolean.TRUE.equals(req.getResearchedMode()) && req.getJobDescription() != null && !req.getJobDescription().trim().isEmpty()) {
                prompt.append("\n\nBold and italicize any terms from the provided job description that appear in the output.");
            } else {
                prompt.append("\n\nBold and italicize important keywords within the paraphrased content.");
            }
        }

        return prompt.toString().trim();
    }
}