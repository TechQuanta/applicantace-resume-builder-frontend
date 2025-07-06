package com.example.acespringbackend.utility;

import com.example.acespringbackend.auth.dto.ParaphrasingRequest;
import org.springframework.stereotype.Component;
import java.util.Optional;


@Component
public class PromptBuilder {

    public String buildPrompt(ParaphrasingRequest req) {
        StringBuilder prompt = new StringBuilder();

        if (Boolean.TRUE.equals(req.getAutoCoverLetterMode())) {
            prompt.append("Generate a professional cover letter.");

            if (req.getJobDescription() != null && !req.getJobDescription().trim().isEmpty()) {
                prompt.append(" Tailor it specifically for the following job description:\n\"\"\"\n")
                      .append(req.getJobDescription()).append("\n\"\"\"\n");
            } else {
                prompt.append(" Here is some key information or resume highlights to use:\n\"\"\"\n")
                      .append(req.getInput()).append("\n\"\"\"\n");
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

            if (Boolean.TRUE.equals(req.getEnableSuggestions())) {
                if (req.getJobDescription() != null && !req.getJobDescription().trim().isEmpty()) {
                    prompt.append("\n\nBold and italicize any terms from the provided job description that appear relevant in the output.");
                } else if (req.getKeywords() != null && !req.getKeywords().trim().isEmpty()) {
                    prompt.append("\n\nBold and italicize any of the following keywords present in the output: ").append(req.getKeywords()).append(".");
                } else {
                    prompt.append("\n\nBold and italicize important keywords within the generated content.");
                }
            }
            return prompt.toString();
        }

        prompt.append("Paraphrase the following text");

        Optional.ofNullable(req.getTone())
                .filter(s -> !s.isEmpty())
                .ifPresent(tone -> prompt.append(" in a ").append(tone).append(" tone"));

        Optional.ofNullable(req.getStyle())
                .filter(s -> !s.isEmpty())
                .ifPresent(style -> prompt.append(" using a ").append(style).append(" style"));

        prompt.append(":\n\n\"\"\"\n").append(req.getInput()).append("\n\"\"\"\n");

        if (Boolean.TRUE.equals(req.getResearchedMode()) && req.getJobDescription() != null && !req.getJobDescription().trim().isEmpty()) {
            prompt.append("\n\nCritically analyze and enhance the paraphrased text by incorporating or emphasizing terms relevant to the following job description. Focus on making the text more impactful and aligned with the job's requirements:");
            prompt.append("\n\"\"\"\n").append(req.getJobDescription()).append("\n\"\"\"\n");
            prompt.append("Ensure the output remains a paraphrase of the original text but is optimized for the job description.");
        } else {
            Optional.ofNullable(req.getKeywords())
                    .filter(s -> !s.isEmpty())
                    .ifPresent(kw -> prompt.append("\n\nEnsure the paraphrase incorporates these keywords: ").append(kw).append("."));
        }

        Optional.ofNullable(req.getWordLimit())
                .ifPresent(limit -> prompt.append("\n\nKeep the paraphrased output to approximately ").append(limit).append(" words."));

        if (Boolean.TRUE.equals(req.getEnableSuggestions())) {
            if (req.getKeywords() != null && !req.getKeywords().trim().isEmpty()) {
                prompt.append("\n\nBold and italicize any of the following keywords present in the output: ").append(req.getKeywords()).append(".");
            } else if (Boolean.TRUE.equals(req.getResearchedMode()) && req.getJobDescription() != null && !req.getJobDescription().trim().isEmpty()) {
                prompt.append("\n\nBold and italicize any terms from the provided job description that appear in the output.");
            } else {
                prompt.append("\n\nBold and italicize important keywords within the paraphrased content.");
            }
        }

        return prompt.toString();
    }
}
