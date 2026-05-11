package ai.patchpilot.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketSummary {

    private Long id;
    private String software;
    private String softwareVersion;
    private String osVersion;
    private String errorMessage;
    private String userAttempts;
    private String severity;     // CURIOUS / INCONVENIENT / BLOCKING
    private String status;       // OPEN / RESOLVED / ESCALATED
    private LocalDateTime createdAt;

    // The diagnosis — typed, not a JSON string
    private DiagnosisResult diagnosis;
}