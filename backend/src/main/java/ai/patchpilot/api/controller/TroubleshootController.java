package ai.patchpilot.api.controller;

import ai.patchpilot.api.dto.DiagnosisResult;
import ai.patchpilot.api.dto.StatusUpdateRequest;
import ai.patchpilot.api.dto.TicketSummary;
import ai.patchpilot.api.dto.TroubleshootRequest;
import ai.patchpilot.api.dto.TroubleshootResponse;
import ai.patchpilot.api.model.TroubleshootingTicket;
import ai.patchpilot.api.repository.KnowledgeBaseArticleRepository;
import ai.patchpilot.api.repository.TroubleshootingTicketRepository;
import ai.patchpilot.api.service.ClaudeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
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
        long startedAt = System.currentTimeMillis();
        log.info("Diagnose request received: software={}, severity={}, errorLength={} chars",
                request.getSoftware(),
                request.getSeverity(),
                request.getErrorMessage().length());

        // Use software name and first 200 chars of error message as search terms
        String errorKeywords = request.getErrorMessage().length() > 200
                ? request.getErrorMessage().substring(0, 200)
                : request.getErrorMessage();

        var relevantArticles = kbRepo.findBySoftwareContainingIgnoreCaseOrErrorPatternContainingIgnoreCase(
                request.getSoftware(), errorKeywords);
        log.debug("KB lookup matched {} relevant articles", relevantArticles.size());

        // 1. Call Claude — returns raw JSON string for persistence
        long claudeStartedAt = System.currentTimeMillis();
        String rawDiagnosis = claudeService.diagnose(request, relevantArticles);
        long claudeElapsedMs = System.currentTimeMillis() - claudeStartedAt;
        log.info("Claude API responded in {}ms", claudeElapsedMs);

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

        long totalElapsedMs = System.currentTimeMillis() - startedAt;
        log.info("Diagnose completed: ticketId={}, totalMs={}, claudeMs={}, kbHits={}",
                ticket.getId(), totalElapsedMs, claudeElapsedMs, relevantArticles.size());

        return ResponseEntity.ok(TroubleshootResponse.builder()
                .ticketId(ticket.getId())
                .diagnosis(diagnosis)
                .createdAt(ticket.getCreatedAt())
                .build());
    }

    /**
     * Returns all troubleshooting tickets ordered newest first.
     * <p>Parses the stored JSON diagnosis into a typed {@link DiagnosisResult}
     * at the API boundary so consumers get the same shape as /diagnose.</p>
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketSummary>> getTickets() {
        long startedAt = System.currentTimeMillis();
        List<TicketSummary> summaries = ticketRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toSummary)
                .toList();
        log.debug("Returned {} tickets in {}ms", summaries.size(), System.currentTimeMillis() - startedAt);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Returns a single ticket by ID, or 404 if not found.
     * <p>Same response shape as /tickets — typed diagnosis, not stringified JSON.</p>
     */
    @GetMapping("/tickets/{id}")
    public ResponseEntity<TicketSummary> getTicket(@PathVariable Long id) {
        TroubleshootingTicket ticket = ticketRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Ticket with id " + id + " not found"
                ));
        return ResponseEntity.ok(toSummary(ticket));
    }

    /**
     * Updates a ticket's status (OPEN / RESOLVED / ESCALATED). Returns 404 if the
     * ticket doesn't exist, 400 if the request body is missing the status field.
     *
     * <p>Transition rules are deliberately liberal: any status can change to any
     * other status. This matches a real support workflow where tickets get
     * reopened, escalated and de-escalated, or resolved in any order.</p>
     */
    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<TicketSummary> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody StatusUpdateRequest request) {
        TroubleshootingTicket ticket = ticketRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Ticket with id " + id + " not found"
                ));

        TroubleshootingTicket.Status previousStatus = ticket.getStatus();
        ticket.setStatus(request.getStatus());
        ticket = ticketRepo.save(ticket);

        log.info("Status updated: ticketId={}, {} -> {}",
                ticket.getId(), previousStatus, request.getStatus());

        return ResponseEntity.ok(toSummary(ticket));
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

    /**
     * Maps a persisted ticket entity to a clean API DTO.
     * <p>Reuses {@link ClaudeService#parseDiagnosis(String)} so JSON parsing
     * logic lives in one place — consistent with /diagnose.</p>
     */
    private TicketSummary toSummary(TroubleshootingTicket ticket) {
        DiagnosisResult diagnosis = null;
        if (ticket.getAiDiagnosis() != null && !ticket.getAiDiagnosis().isBlank()) {
            try {
                diagnosis = claudeService.parseDiagnosis(ticket.getAiDiagnosis());
            } catch (Exception e) {
                // Diagnosis stays null — corrupted row shouldn't crash the whole listing.
                // @JsonInclude(NON_NULL) on TicketSummary will omit the field from the response.
                log.warn("Failed to parse diagnosis for ticketId={}: {}", ticket.getId(), e.getMessage());
            }
        }

        return TicketSummary.builder()
                .id(ticket.getId())
                .software(ticket.getSoftware())
                .softwareVersion(ticket.getSoftwareVersion())
                .osVersion(ticket.getOsVersion())
                .errorMessage(ticket.getErrorMessage())
                .userAttempts(ticket.getUserAttempts())
                .severity(ticket.getSeverity() != null ? ticket.getSeverity().name() : null)
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : null)
                .createdAt(ticket.getCreatedAt())
                .diagnosis(diagnosis)
                .build();
    }
}