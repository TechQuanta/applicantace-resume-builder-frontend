package com.example.acespringbackend.utility;

import com.example.acespringbackend.auth.dto.ParaphrasingRequest;

public class PromptBuilder {

    public static String buildPrompt(ParaphrasingRequest req) {
        String tone = req.getTone() != null ? req.getTone() : "neutral";
        String style = req.getStyle();
        String toneLabel = (style != null && !style.isBlank()) ? tone + " - " + style : tone;

        boolean isCodeQuestion = req.getInput().toLowerCase()
                .matches(".*\\b(code|function|debug|python|java|example)\\b.*");

        if (isCodeQuestion) {
            return String.format("""
                You are a highly knowledgeable programming assistant.
                Please provide a comprehensive and accurate answer to the following coding-related question.
                Deliver the response in a "%s" style.
                If possible, include code examples.

                User's Coding Question:
                "%s"
            """, toneLabel, req.getInput());
        }

        // Resume-style prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            Template: Resume Paraphrasing and Keyword Suggestion Assistant
            Objective: Paraphrase the user's text, ensure it meets a word limit, and optionally suggest/format keywords for highlighting.

            Instructions:
            1. Paraphrase the user's provided text into a "%s" style.
            """.formatted(toneLabel));

        if (req.getWordLimit() != null && !req.getWordLimit().isBlank()) {
            prompt.append("2. Ensure the paraphrased response is within approximately ")
                    .append(req.getWordLimit()).append(" words.\n");
        }

        if (req.isEnableSuggestions()) {
            prompt.append("3. Bold and italicize 3–5 important keywords for highlighting.\n");
        }

        if (req.getJobDescription() != null && !req.getJobDescription().isBlank()) {
            prompt.append("4. Align response with this job description: \"")
                    .append(req.getJobDescription()).append("\"\n");
        }

        prompt.append("\nUser's Text:\n\"").append(req.getInput()).append("\"\n");

        if (req.getKeywords() != null && !req.getKeywords().isBlank()) {
            prompt.append("User Keywords: ").append(req.getKeywords()).append("\n");
        }

        return prompt.toString();
    }
}
