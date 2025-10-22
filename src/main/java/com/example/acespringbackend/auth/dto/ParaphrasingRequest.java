package com.example.acespringbackend.auth.dto;

/**
 * Data Transfer Object (DTO) for encapsulating a request to a paraphrasing service.
 * This DTO includes the input text, desired output characteristics (tone, style, word limit),
 * and flags for specific processing modes like researched mode or cover letter generation.
 */
public class ParaphrasingRequest {

    /**
     * The original text or content that needs to be paraphrased. This is the primary input.
     */
    private String input;

    /**
     * The desired emotional or stylistic tone for the paraphrased output (e.g., "professional", "friendly", "neutral").
     */
    private String tone;

    /**
     * The desired stylistic category for the paraphrased output (e.g., "formal", "informal", "academic", "creative").
     */
    private String style;

    /**
     * An optional job description string. This is primarily used when the "researched mode"
     * or "auto cover letter mode" is enabled, allowing the paraphrasing service to tailor
     * the output based on job-specific language and requirements.
     */
    private String jobDescription;

    /**
     * A comma-separated string of keywords that should ideally be included or emphasized
     * in the paraphrased output.
     */
    private String keywords;

    /**
     * An optional word limit for the paraphrased output. The service will attempt to
     * generate output within this specified word count.
     */
    private Integer wordLimit;

    /**
     * A boolean flag indicating whether the paraphrasing service should provide
     * suggestions or highlight certain parts of the output (e.g., bold/italic keywords).
     * This might involve special formatting in the response.
     */
    private Boolean enableSuggestions;

    /**
     * A boolean flag indicating if the request is specifically for generating
     * an automated cover letter based on the provided input and job description.
     * {@code true} if cover letter generation is requested, {@code false} otherwise.
     */
    private Boolean autoCoverLetterMode;

    /**
     * A boolean flag indicating if the "researched mode" should be applied.
     * In this mode, the service might analyze the {@code jobDescription} to tailor
     * the paraphrasing, often used for resume or cover letter optimization.
     * {@code true} if "researched" mode is requested, {@code false} otherwise.
     */
    private Boolean researchedMode;

    /**
     * Default no-argument constructor for {@code ParaphrasingRequest}.
     * This constructor is essential for deserialization frameworks (like Spring)
     * to automatically map JSON or form data into an instance of this object.
     */
    public ParaphrasingRequest() {
    }

    /**
     * Constructs a new {@code ParaphrasingRequest} with all possible parameters.
     * This comprehensive constructor allows for direct instantiation with full control
     * over all request attributes.
     *
     * @param input               The original text to be paraphrased.
     * @param tone                The desired tone for the output.
     * @param style               The desired style for the output.
     * @param jobDescription      An optional job description for context.
     * @param keywords            Keywords to include or emphasize.
     * @param wordLimit           Optional word limit for the output.
     * @param enableSuggestions   Flag to enable highlighting/suggestions in the output.
     * @param autoCoverLetterMode Flag for automated cover letter generation mode.
     * @param researchedMode      Flag for researched (job description analysis) mode.
     */
    public ParaphrasingRequest(String input, String tone, String style, String jobDescription,
                               String keywords, Integer wordLimit, Boolean enableSuggestions,
                               Boolean autoCoverLetterMode, Boolean researchedMode) {
        this.input = input;
        this.tone = tone;
        this.style = style;
        this.jobDescription = jobDescription;
        this.keywords = keywords;
        this.wordLimit = wordLimit;
        this.enableSuggestions = enableSuggestions;
        this.autoCoverLetterMode = autoCoverLetterMode;
        this.researchedMode = researchedMode;
    }

    // --- Getters ---

    /**
     * Retrieves the input text for paraphrasing.
     *
     * @return The input text as a {@link String}.
     */
    public String getInput() {
        return input;
    }

    /**
     * Retrieves the desired tone for the paraphrased output.
     *
     * @return The tone as a {@link String}.
     */
    public String getTone() {
        return tone;
    }

    /**
     * Retrieves the desired style for the paraphrased output.
     *
     * @return The style as a {@link String}.
     */
    public String getStyle() {
        return style;
    }

    /**
     * Retrieves the optional job description.
     *
     * @return The job description as a {@link String}, or {@code null} if not provided.
     */
    public String getJobDescription() {
        return jobDescription;
    }

    /**
     * Retrieves the keywords string.
     *
     * @return The keywords as a {@link String}, or {@code null} if not provided.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Retrieves the optional word limit for the output.
     *
     * @return The word limit as an {@link Integer}, or {@code null} if not specified.
     */
    public Integer getWordLimit() {
        return wordLimit;
    }

    /**
     * Retrieves the flag indicating if suggestions should be enabled.
     *
     * @return {@code true} if suggestions are enabled, {@code false} otherwise, or {@code null} if not set.
     */
    public Boolean getEnableSuggestions() {
        return enableSuggestions;
    }

    /**
     * Retrieves the flag for automated cover letter generation mode.
     *
     * @return {@code true} if auto cover letter mode is enabled, {@code false} otherwise, or {@code null} if not set.
     */
    public Boolean getAutoCoverLetterMode() {
        return autoCoverLetterMode;
    }

    /**
     * Retrieves the flag for researched mode.
     *
     * @return {@code true} if researched mode is enabled, {@code false} otherwise, or {@code null} if not set.
     */
    public Boolean getResearchedMode() {
        return researchedMode;
    }


    // --- Setters ---

    /**
     * Sets the input text for paraphrasing.
     *
     * @param input The input text to set.
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * Sets the desired tone for the paraphrased output.
     *
     * @param tone The tone to set.
     */
    public void setTone(String tone) {
        this.tone = tone;
    }

    /**
     * Sets the desired style for the paraphrased output.
     *
     * @param style The style to set.
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Sets the optional job description.
     *
     * @param jobDescription The job description to set.
     */
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    /**
     * Sets the keywords string.
     *
     * @param keywords The keywords to set.
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Sets the optional word limit for the output.
     *
     * @param wordLimit The word limit to set.
     */
    public void setWordLimit(Integer wordLimit) {
        this.wordLimit = wordLimit;
    }

    /**
     * Sets the flag to enable or disable suggestions.
     *
     * @param enableSuggestions The boolean flag to set.
     */
    public void setEnableSuggestions(Boolean enableSuggestions) {
        this.enableSuggestions = enableSuggestions;
    }

    /**
     * Sets the flag for automated cover letter generation mode.
     *
     * @param autoCoverLetterMode The boolean flag for auto cover letter mode to set.
     */
    public void setAutoCoverLetterMode(Boolean autoCoverLetterMode) {
        this.autoCoverLetterMode = autoCoverLetterMode;
    }

    /**
     * Sets the flag for researched mode.
     *
     * @param researchedMode The boolean flag for researched mode to set.
     */
    public void setResearchedMode(Boolean researchedMode) {
        this.researchedMode = researchedMode;
    }
}