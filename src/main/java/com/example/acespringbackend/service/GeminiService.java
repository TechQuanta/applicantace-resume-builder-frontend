package com.example.acespringbackend.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public String getGeminiReply(String prompt, String apiKey) throws IOException, InterruptedException {
        String escapedPrompt = prompt.replace("\"", "\\\"");
        String jsonRequest = """
        {
            "contents": [ {
                "parts": [ { "text": "%s" } ]
            }]
        }
        """.formatted(escapedPrompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(geminiApiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Gemini API returned non-200 status: " + response.statusCode());
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray candidates = json.optJSONArray("candidates");

        if (candidates != null && candidates.length() > 0) {
            return candidates
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        }

        throw new IOException("No valid content found in Gemini API response.");
    }
}
