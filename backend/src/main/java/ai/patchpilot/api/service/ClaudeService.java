package ai.patchpilot.api.service;

import ai.patchpilot.api.dto.TroubleshootRequest;
import ai.patchpilot.api.exception.ClaudeApiException;
import ai.patchpilot.api.model.KnowledgeBaseArticle;
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
    private static final int MAX_TOKENS = 2000;

    private static final String SYSTEM_PROMPT = """
            You are PatchPilot, a senior IT support engineer specializing in diagnosing \
            software installation failures on Windows. When given a user's problem, analyse \
            it together with any relevant knowledge-base articles provided and return \
            STRICT JSON ONLY — no markdown fences, no prose outside the JSON object.

            Required structure (return exactly this, no extra keys):
            {
              "tryThisFirst": "string — the single best first action the user should take right now",
              "causes": [
                {
                  "name": "string — short name for this cause",
                  "probability": <integer 0-100>,
                  "explanation": "string — plain English, 1-2 sentences",
                  "steps": ["string", "string", ...]
                }
              ]
            }

            Rules:
            - Always return exactly 3 causes, ranked by probability descending.
            - Probabilities need not sum to 100; each represents independent likelihood.
            - Each cause must include at least 2 concrete, actionable steps.
            - Never wrap the JSON in markdown code fences or add any text outside it.
            """;

    private final RestClient restClient;

    public ClaudeService(@Value("${ANTHROPIC_API_KEY}") String apiKey) {
        this.restClient = RestClient.builder()
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    /**
     * Sends the user's installation problem to Claude along with matching KB articles
     * and returns the raw JSON diagnosis string.
     *
     * @param request          the validated user request
     * @param relevantArticles KB articles matched to the user's software / error
     * @return raw JSON string conforming to the PatchPilot diagnosis schema
     * @throws ClaudeApiException if the Claude API returns an error or is unreachable
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
            // Strip the opening fence line (```json, ```JSON, ``` etc.)
            int newline = trimmed.indexOf('\n');
            trimmed = (newline != -1) ? trimmed.substring(newline + 1) : trimmed.substring(3);
            // Strip the closing ```
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
