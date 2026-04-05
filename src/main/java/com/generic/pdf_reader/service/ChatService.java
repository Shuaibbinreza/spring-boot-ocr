package com.generic.pdf_reader.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String API_KEY = "AIzaSyAn6hyL1lAJa2M5pwbnIkQ_fdEIv_3rnhY";
    private static final String MODEL = "gemini-2.0-flash";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent?key=" + API_KEY;
    private final OkHttpClient client;
    private final Gson gson;

    public ChatService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public String chatWithPDF(String pdfContent, String userMessage) {
        String prompt = """
            You are a helpful assistant answering questions about the provided PDF document.
            
            Here is the PDF content:
            %s
            
            Based on the PDF content above, please answer the following question:
            %s
            """.formatted(pdfContent, userMessage);

        int maxRetries = 5;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                String result = callGemini(prompt);
                if (!result.startsWith("Error: 429")) {
                    return result;
                }
                log.warn("Rate limited, attempt {}/{}, waiting {}s", attempt + 1, maxRetries, (attempt + 1) * 5000);
                if (attempt < maxRetries - 1) {
                    Thread.sleep((attempt + 1) * 5000L);
                }
            } catch (Exception e) {
                if (attempt == maxRetries - 1) {
                    return "Error: " + e.getMessage();
                }
                try {
                    Thread.sleep((attempt + 1) * 5000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return "Rate limited. Please wait longer or check your API quota.";
    }

    private String callGemini(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        
        JsonObject partObj = new JsonObject();
        partObj.addProperty("text", prompt);
        
        JsonArray parts = new JsonArray();
        parts.add(partObj);
        
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        
        JsonArray contents = new JsonArray();
        contents.add(content);
        requestBody.add("contents", contents);

        Request request = new Request.Builder()
                .url(GEMINI_URL)
                .post(RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                log.error("Gemini API error: {} - {} - {}", response.code(), response.message(), body);
                return "Error: " + response.code() + " - " + response.message();
            }
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            
            if (json.has("candidates") && json.getAsJsonArray("candidates").size() > 0) {
                JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();
                if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                    JsonArray partsArr = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                    if (partsArr.size() > 0) {
                        return partsArr.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
            }
            return "No response from Gemini";
        }
    }
}