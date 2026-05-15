package ai.patchpilot.api.service;

import ai.patchpilot.api.dto.DiagnosisResult;
import ai.patchpilot.api.dto.TroubleshootRequest;
import ai.patchpilot.api.exception.ClaudeApiException;
import ai.patchpilot.api.model.KnowledgeBaseArticle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class ClaudeService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-5";

    // Lowered from 2000 -> 1200 as a hard safety ceiling.
    // Expected output is ~1000 tokens after the trim; 1200 leaves margin.
    private static final int MAX_TOKENS = 1200;

    private static final String SYSTEM_PROMPT = """
            You are PatchPilot, a senior IT support engineer specializing in diagnosing \
            software installation failures on Windows. When given a user's problem, analyse \
            it together with any relevant knowledge-base articles provided and return \
            STRICT JSON ONLY — no markdown fences, no prose outside the JSON object.

            Required structure (return exactly this, no extra keys):
            {
              "tryThisFirst": "string — one concrete first action, max 15 words",
              "causes": [
                {
                  "name": "string — short name for this cause, max 6 words",
                  "probability": <integer 0-100>,
                  "explanation": "string — ONE sentence, max 20 words",
                  "steps": ["string", "string", "string"]
                }
              ]
            }

            Strict output rules — follow exactly:
            - Return EXACTLY 3 causes, ranked by probability descending.
            - Probabilities need not sum to 100; each represents independent likelihood.
            - Each cause must have EXACTLY 3 steps — no more, no less.
            - Each step must be ONE action-oriented sentence, max 15 words.
            - Each step must start with a verb (e.g., "Run", "Open", "Delete", "Verify").
            - Each "explanation" must be ONE sentence only — no compound sentences, no semicolons.
            - "tryThisFirst" must be a single concrete action — not a list, not a paragraph.
            - Never wrap the JSON in markdown code fences or add any text outside it.
            - Be specific and concise. Recruiters and IT pros will read this — avoid filler words like "you should", "it is recommended", "try to".
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ClaudeService(@Value("${ANTHROPIC_API_KEY}") String apiKey, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    /**
     * Sends the user's installation problem to Claude along with matching KB articles
     * and returns the raw JSON diagnosis string suitable for DB persistence.
     */
    public String diagnose(TroubleshootRequest request, List<KnowledgeBaseArticle> relevantArticles) {
        String userContent = buildUserPrompt(request, relevantArticles);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "max_tokens", MAX_TOKENS,
                "system", SYSTEM_PROMPT,
                "messages", List.of(Map.of("role", "user", "content", userContent))
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(CLAUDE_API_URL)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            return extractJson((String) content.get(0).get("text"));

        } catch (HttpClientErrorException e) {
            throw new ClaudeApiException(
                    "Claude API rejected the request (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            throw new ClaudeApiException(
                    "Claude API server error (" + e.getStatusCode() + ")", e);
        } catch (RestClientException e) {
            throw new ClaudeApiException("Failed to reach Claude API: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a raw diagnosis JSON string (as stored in the DB) into a typed
     * {@link DiagnosisResult}. Used by the controller to expose structured data
     * to API consumers without leaking the JSON-as-string representation.
     */
    public DiagnosisResult parseDiagnosis(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(rawJson, DiagnosisResult.class);
        } catch (JsonProcessingException e) {
            throw new ClaudeApiException("Stored diagnosis is not valid JSON: " + e.getOriginalMessage(), e);
        }
    }

    /**
     * Strips markdown code fences that Claude sometimes wraps around JSON responses.
     * Handles {@code ```json}, {@code ```JSON}, and plain {@code ```} opening fences.
     * Safe to call with null or empty input — returns the value unchanged.
     */
    String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isEmpty()) {
            return rawResponse;
        }
        String trimmed = rawResponse.trim();
        if (trimmed.toLowerCase().startsWith("```")) {
            int newline = trimmed.indexOf('\n');
            trimmed = (newline != -1) ? trimmed.substring(newline + 1) : trimmed.substring(3);
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }

    private String buildUserPrompt(TroubleshootRequest req, List<KnowledgeBaseArticle> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Installation Problem Report\n\n");
        sb.append("**Software:** ").append(req.getSoftware());
        if (req.getSoftwareVersion() != null && !req.getSoftwareVersion().isBlank()) {
            sb.append(" ").append(req.getSoftwareVersion());
        }
        sb.append("\n");
        sb.append("**OS:** ").append(req.getOsVersion()).append("\n");
        sb.append("**Severity:** ").append(req.getSeverity()).append("\n\n");
        sb.append("**Error message:**\n").append(req.getErrorMessage()).append("\n\n");

        if (req.getUserAttempts() != null && !req.getUserAttempts().isBlank()) {
            sb.append("**What the user has already tried:**\n").append(req.getUserAttempts()).append("\n\n");
        }

        if (!articles.isEmpty()) {
            sb.append("## Relevant Knowledge Base Articles\n\n");
            for (KnowledgeBaseArticle article : articles) {
                sb.append("### ").append(article.getTitle()).append("\n");
                sb.append("**Category:** ").append(article.getCategory()).append("\n");
                sb.append("**Root cause:** ").append(article.getRootCause()).append("\n");
                sb.append("**Resolution steps:**\n").append(article.getResolutionSteps()).append("\n\n");
            }
        }

        sb.append("Diagnose this problem and return the JSON response.");
        return sb.toString();
    }
}