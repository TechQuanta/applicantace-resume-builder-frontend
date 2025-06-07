package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.ParaphrasingRequest;
import com.example.acespringbackend.auth.dto.ParaphrasingResponse;
import com.example.acespringbackend.service.GeminiService;
import com.example.acespringbackend.utility.PromptBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Optional: allow frontend requests
public class ParaphrasingController {

    private final GeminiService geminiService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public ParaphrasingController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<ParaphrasingResponse> handleChat(@RequestBody ParaphrasingRequest request) {
        ParaphrasingResponse response = new ParaphrasingResponse();
        try {
            String prompt = PromptBuilder.buildPrompt(request);
            String reply = geminiService.getGeminiReply(prompt, geminiApiKey);
            response.setContent(reply);
            response.setError(false);
        } catch (Exception e) {
            response.setError(true);
            response.setErrorMessage("Server Error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
