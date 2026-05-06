package ai.patchpilot.api.controller;

import ai.patchpilot.api.dto.DiagnosisResult;
import ai.patchpilot.api.dto.TroubleshootRequest;
import ai.patchpilot.api.dto.TroubleshootResponse;
import ai.patchpilot.api.model.TroubleshootingTicket;
import ai.patchpilot.api.repository.KnowledgeBaseArticleRepository;
import ai.patchpilot.api.repository.TroubleshootingTicketRepository;
import ai.patchpilot.api.service.ClaudeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TroubleshootController {

    private final KnowledgeBaseArticleRepository kbRepo;
    private final TroubleshootingTicketRepository ticketRepo;
    private final ClaudeService claudeService;

    public TroubleshootController(KnowledgeBaseArticleRepository kbRepo,
                                  TroubleshootingTicketRepository ticketRepo,
                                  ClaudeService claudeService) {
        this.kbRepo = kbRepo;
        this.ticketRepo = ticketRepo;
        this.claudeService = claudeService;
    }

    /**
     * Accepts an installation problem, matches relevant KB articles, calls Claude for
     * a diagnosis, persists the ticket with the raw JSON, and returns a typed response.
     *
     * <p>The diagnosis is stored as raw JSON in the DB (cheap reads, single-document
     * access) but parsed into {@link DiagnosisResult} at the API boundary so frontend
     * consumers get clean structured data.</p>
     */
    @PostMapping("/diagnose")
    public ResponseEntity<TroubleshootResponse> diagnose(@Valid @RequestBody TroubleshootRequest request) {
        // Use software name and first 200 chars of error message as search terms
        String errorKeywords = request.getErrorMessage().length() > 200
                ? request.getErrorMessage().substring(0, 200)
                : request.getErrorMessage();

        var relevantArticles = kbRepo.findBySoftwareContainingIgnoreCaseOrErrorPatternContainingIgnoreCase(
                request.getSoftware(), errorKeywords);

        // 1. Call Claude — returns raw JSON string for persistence
        String rawDiagnosis = claudeService.diagnose(request, relevantArticles);

        // 2. Persist the ticket with the raw JSON in a TEXT column
        TroubleshootingTicket ticket = TroubleshootingTicket.builder()
                .software(request.getSoftware())
                .softwareVersion(request.getSoftwareVersion())
                .osVersion(request.getOsVersion())
                .errorMessage(request.getErrorMessage())
                .userAttempts(request.getUserAttempts())
                .severity(request.getSeverity())
                .aiDiagnosis(rawDiagnosis)
                .build();
        ticket = ticketRepo.save(ticket);

        // 3. Parse the raw JSON into a typed DTO for the API response
        DiagnosisResult diagnosis = claudeService.parseDiagnosis(rawDiagnosis);

        return ResponseEntity.ok(TroubleshootResponse.builder()
                .ticketId(ticket.getId())
                .diagnosis(diagnosis)
                .createdAt(ticket.getCreatedAt())
                .build());
    }

    /**
     * Returns all troubleshooting tickets ordered newest first.
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<TroubleshootingTicket>> getTickets() {
        return ResponseEntity.ok(ticketRepo.findAllByOrderByCreatedAtDesc());
    }

    /**
     * Lightweight health check for uptime monitoring and smoke tests.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "app", "PatchPilot",
                "version", "0.1.0"
        ));
    }
}