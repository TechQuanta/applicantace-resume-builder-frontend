package com.example.acespringbackend.service;

import com.example.acespringbackend.utility.PdfTextExtractor;
import com.example.acespringbackend.auth.dto.AtsResponse;
import com.example.acespringbackend.model.AtsResult;
import com.example.acespringbackend.repository.AtsResultRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiService {

    private final String geminiApiUrl;
    private final WebClient webClient;
    private final List<String> apiKeys;
    private final AtomicInteger currentApiKeyIndex = new AtomicInteger(0);
    private final AtsResultRepository atsResultRepository;

    public GeminiService(@Value("${gemini.api.url}") String geminiApiUrl,
                         @Value("${gemini.api.keys}") String apiKeysString,
                         AtsResultRepository atsResultRepository,
                         WebClient.Builder webClientBuilder) {
        this.geminiApiUrl = geminiApiUrl;
        this.apiKeys = Arrays.asList(apiKeysString.split(","));
        this.atsResultRepository = atsResultRepository;
        this.webClient = webClientBuilder.build();
        if (this.apiKeys.isEmpty()) {
            throw new IllegalArgumentException("No Gemini API keys provided in application.properties (gemini.api.keys)");
        }
        System.out.println("GeminiService initialized with " + this.apiKeys.size() + " API key(s).");
    }

    // Changed MultipartFile to byte[]
    public Mono<AtsResponse> getAtsScore(byte[] fileBytes, String originalFileName, boolean isDeepCheck, String jobTitle, String jobDescription, String userEmail, String userId) {
        String fullPdfText;
        try {
            fullPdfText = PdfTextExtractor.extractText(fileBytes); // Use the byte[]
        } catch (IOException e) {
            return Mono.just(new AtsResponse("0", "", "", true, "Error extracting text from PDF: " + e.getMessage()));
        }

        if (fullPdfText == null || fullPdfText.trim().isEmpty()) {
            return Mono.just(new AtsResponse("0", "", "", true, "Could not extract text from the provided PDF file. It might be empty or unreadable."));
        }

        final String finalFullPdfText = fullPdfText;
        final String finalFileName = (originalFileName != null && !originalFileName.isEmpty()) ? originalFileName : "untitled_resume_" + System.currentTimeMillis() + ".pdf";

        List<Map<String, Object>> parts = buildGeminiPromptParts(isDeepCheck, jobTitle, jobDescription, finalFullPdfText);

        return callGemini(parts)
                .flatMap(geminiMarkdownResponse -> {
                    int atsScoreInt = extractScoreFromGeminiResponse(geminiMarkdownResponse);

                    Mono<AtsResult> saveOrUpdateMono;
                    if (userEmail != null && !userEmail.isEmpty()) {
                        saveOrUpdateMono = atsResultRepository.findByUserEmailAndFileName(userEmail, finalFileName)
                                .flatMap(existingResult -> {
                                    existingResult.setAtsScore(atsScoreInt);
                                    existingResult.setFullAtsResponse(geminiMarkdownResponse);
                                    existingResult.setExtractedResumeContent(finalFullPdfText);
                                    existingResult.setJobTitle(jobTitle);
                                    existingResult.setJobDescription(jobDescription);
                                    existingResult.setCheckTimestamp(LocalDateTime.now());
                                    if (userId != null && !userId.isEmpty() && existingResult.getUserId() == null) {
                                        existingResult.setUserId(userId);
                                    }
                                    System.out.println("Updating existing ATS result for email: " + userEmail + ", file: " + finalFileName);
                                    return atsResultRepository.save(existingResult);
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    AtsResult newAtsResult = new AtsResult(
                                            userId,
                                            userEmail,
                                            finalFileName,
                                            jobTitle,
                                            jobDescription,
                                            finalFullPdfText,
                                            geminiMarkdownResponse,
                                            atsScoreInt,
                                            LocalDateTime.now()
                                    );
                                    System.out.println("Creating new ATS result for email: " + userEmail + ", file: " + finalFileName);
                                    return atsResultRepository.save(newAtsResult);
                                }));
                    } else {
                        System.out.println("User email not provided. ATS score result will not be saved to MongoDB.");
                        saveOrUpdateMono = Mono.empty();
                    }

                    return saveOrUpdateMono.then(Mono.just(new AtsResponse(String.valueOf(atsScoreInt), geminiMarkdownResponse, finalFullPdfText, false, null)))
                                           .onErrorResume(e -> {
                                               System.err.println("Error saving/updating ATS result: " + e.getMessage());
                                               return Mono.just(new AtsResponse(String.valueOf(atsScoreInt), geminiMarkdownResponse, finalFullPdfText, true, "Error saving result: " + e.getMessage()));
                                           })
                                           .defaultIfEmpty(new AtsResponse(String.valueOf(atsScoreInt), geminiMarkdownResponse, finalFullPdfText, false, null));
                })
                .onErrorResume(e -> {
                    System.err.println("Error during Gemini API call or processing: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just(new AtsResponse("0", "", finalFullPdfText, true, "Error processing resume: " + e.getMessage()));
                });
    }


    private List<Map<String, Object>> buildGeminiPromptParts(boolean isDeepCheck, String jobTitle, String jobDescription, String fullPdfText) {
        List<Map<String, Object>> parts = new ArrayList<>();
        String resumeContentForPrompt;

        if (isDeepCheck) {
            Map<String, String> blocks = PdfTextExtractor.extractSpecificBlocks(fullPdfText);
            String experienceBlock = blocks.getOrDefault("experience", "");
            String skillsBlock = blocks.getOrDefault("skills", "");

            StringBuilder blockTextBuilder = new StringBuilder();
            if (!experienceBlock.isEmpty()) {
                blockTextBuilder.append("### Experience Block:\n").append(experienceBlock).append("\n\n");
            }
            if (!skillsBlock.isEmpty()) {
                blockTextBuilder.append("### Skills Block:\n").append(skillsBlock).append("\n\n");
            }
            resumeContentForPrompt = blockTextBuilder.toString().trim();

            if (resumeContentForPrompt.isEmpty()) {
                System.out.println("GeminiService: Deep check requested, but specific blocks not found. Falling back to full resume text for detailed analysis.");
                resumeContentForPrompt = fullPdfText;
            }

            parts.add(Map.of("text", "You are an expert ATS (Applicant Tracking System) and HR professional. Your task is to analyze the provided resume content against the given job description and job title."));
            if (jobTitle != null && !jobTitle.trim().isEmpty()) {
                parts.add(Map.of("text", "\n\n### Job Title:\n" + jobTitle));
            }
            if (jobDescription != null && !jobDescription.trim().isEmpty()) {
                parts.add(Map.of("text", "\n\n### Job Description:\n" + jobDescription));
            }
            parts.add(Map.of("text", "\n\n### Candidate Resume Content:\n" + resumeContentForPrompt));
            parts.add(Map.of("text", "\n\nBased on the above, provide a detailed ATS score (0-100). The score should primarily reflect keyword matching, formatting, and overall relevance to the job description. Also, provide specific actionable feedback on how to improve the resume for this particular job, focusing on keywords, experience alignment, and structure. Format your response in Markdown properly with indentation as follows:\n\n**ATS Score:** [SCORE]/100\n\n**Feedback:**\n* [Point 1]\n* [Point 2]\n* [Point 3]...\n\n**Full Extracted Resume Content:**\n```markdown\n" + fullPdfText + "\n```"));

        } else {
            System.out.println("GeminiService: Rough ATS check requested. Sending full resume text to Gemini.");
            resumeContentForPrompt = fullPdfText;
            parts.add(Map.of("text", "You are an expert ATS (Applicant Tracking System). Give a **rough ATS score (0-100)** for the following resume. Focus primarily on its overall structure, clarity of sections like contact info, summary/objective, and work experience. Format your response in Markdown as follows:\n\n**ATS Score:** [SCORE]/100\n\n**Full Extracted Resume Content:**\n```markdown\n" + fullPdfText + "\n```"));
        }
        return parts;
    }


    public Mono<String> getGeminiReply(String prompt) {
        List<Map<String, Object>> contentParts = List.of(Map.of("text", prompt));
        return callGemini(contentParts);
    }

    private Mono<String> callGemini(List<Map<String, Object>> contentParts) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", contentParts)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        System.out.println("Gemini Request Body: " + requestBody);
        if (!contentParts.isEmpty() && contentParts.get(0).containsKey("text")) {
             String promptText = (String) contentParts.get(0).get("text");
             System.out.println("Gemini Prompt Content (first 500 chars): " + promptText.substring(0, Math.min(promptText.length(), 500)) + (promptText.length() > 500 ? "..." : ""));
        }


        return Mono.defer(() -> {
            String currentApiKey = apiKeys.get(currentApiKeyIndex.get());
            String fullGeminiUrl = this.geminiApiUrl + "?key=" + currentApiKey;

            System.out.println("GeminiService - Sending request to Gemini with key index " + currentApiKeyIndex.get());

            return webClient.post()
                    .uri(fullGeminiUrl)
                    .headers(h -> h.addAll(headers))
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("Gemini API HTTP error with key index " + currentApiKeyIndex.get() + ", Status: " + clientResponse.statusCode() + ", Body: " + errorBody);
                                    return Mono.error(new RuntimeException("Gemini API call failed with status: " + clientResponse.statusCode() + " and body: " + errorBody));
                                })
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(responseBody -> {
                        System.out.println("Gemini Raw Response Body: " + responseBody);

                        try {
                            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                            if (candidates != null && !candidates.isEmpty()) {
                                Map<String, Object> firstCandidate = (Map<String, Object>) candidates.get(0);
                                if (firstCandidate != null) {
                                    Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                                    if (content != null) {
                                        List<Map<String, Object>> partsResponse = (List<Map<String, Object>>) content.get("parts");
                                        if (partsResponse != null && !partsResponse.isEmpty()) {
                                            Object textObject = partsResponse.get(0).get("text");
                                            if (textObject instanceof String) {
                                                return (String) textObject;
                                            }
                                        }
                                    }
                                }
                            }
                            System.err.println("Gemini API response structure unexpected: No text content found or incorrect type. Response: " + responseBody);
                            throw new RuntimeException("Gemini API response structure unexpected: No text content found or incorrect type.");
                        } catch (ClassCastException e) {
                            System.err.println("ClassCastException parsing Gemini API response: " + e.getMessage() + ". Raw Response: " + responseBody);
                            throw new RuntimeException("Error parsing Gemini API response: " + e.getMessage(), e);
                        }
                    })
                    .retryWhen(Retry.max(apiKeys.size() - 1)
                        .filter(throwable -> {
                            System.err.println("Retrying Gemini API call due to: " + throwable.getMessage());
                            currentApiKeyIndex.set((currentApiKeyIndex.get() + 1) % apiKeys.size());
                            return true;
                        })
                        .doAfterRetry(retrySignal -> {
                            System.out.println("Attempt " + (retrySignal.totalRetriesInARow() + 1) + " failed. Retrying with next API key.");
                        })
                    )
                    .onErrorResume(e -> {
                        System.err.println("Gemini API error after trying all keys: " + e.getMessage());
                        e.printStackTrace();
                        return Mono.just("⚠️ Gemini API error after trying all keys: " + e.getMessage());
                    });
        });
    }

    public int extractScoreFromGeminiResponse(String geminiResponse) {
        if (geminiResponse == null || geminiResponse.trim().isEmpty()) {
            return 0;
        }

        Pattern pattern = Pattern.compile("(?i)(?:ATS Score:|\\bScore:)?\\s*(\\d+)(?:/100)?");
        Matcher matcher = pattern.matcher(geminiResponse);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                System.err.println("Could not parse score from: " + matcher.group(1));
            }
        }
        return 0;
    }
}