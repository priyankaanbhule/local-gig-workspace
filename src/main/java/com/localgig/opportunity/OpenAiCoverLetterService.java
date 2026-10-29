package com.localgig.opportunity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiCoverLetterService {
    private final RestClient client;

    @Value("${localgig.openai.api-key:}")
    private String apiKey;

    @Value("${localgig.openai.model:gpt-4.1-mini}")
    private String model;

    public OpenAiCoverLetterService() {
        this.client = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    public String draft(String opportunityTitle, String company, String description,
                        String candidateSummary, List<String> skills) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI drafting is not configured. Set OPENAI_API_KEY in IntelliJ.");
        }

        String prompt = """
                Write a concise, truthful cover letter for this job application.
                Use only facts in the candidate profile below. Do not invent experience,
                qualifications, achievements, or contact details. Keep it under 120 words,
                professional and warm, and return only the letter with no subject or explanation.

                Opportunity: %s
                Company: %s
                Job details: %s
                Candidate profile summary: %s
                Candidate skills: %s
                """.formatted(limit(opportunityTitle, 160), limit(company, 160),
                limit(description, 3500), limit(candidateSummary, 1800),
                limit(String.join(", ", skills == null ? List.of() : skills), 800));

        try {
            Map<String, Object> response = client.post()
                    .uri("/responses")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", model, "input", prompt, "max_output_tokens", 220))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
            String text = extractText(response);
            if (text.isBlank()) throw new IllegalStateException("OpenAI returned no draft");
            return text;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            System.getLogger(OpenAiCoverLetterService.class.getName()).log(
                    System.Logger.Level.ERROR, "OpenAI cover-letter request failed");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The AI service could not create a draft. Check the backend console and try again.");
        }
    }

    private String extractText(Map<String, Object> response) {
        if (response == null || !(response.get("output") instanceof List<?> output)) return "";
        StringBuilder text = new StringBuilder();
        for (Object item : output) {
            if (!(item instanceof Map<?, ?> itemMap) || !(itemMap.get("content") instanceof List<?> content)) continue;
            for (Object part : content) {
                if (part instanceof Map<?, ?> partMap && "output_text".equals(partMap.get("type"))
                        && partMap.get("text") instanceof String value) {
                    if (!text.isEmpty()) text.append("\n");
                    text.append(value);
                }
            }
        }
        return text.toString().trim();
    }

    private String limit(String value, int max) {
        if (value == null) return "";
        String normalized = value.trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max);
    }
}
