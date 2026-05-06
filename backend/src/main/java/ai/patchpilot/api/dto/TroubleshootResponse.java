package ai.patchpilot.api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TroubleshootResponse {

    private Long ticketId;

    /** Structured Claude diagnosis — see {@link DiagnosisResult}. */
    private DiagnosisResult diagnosis;

    private LocalDateTime createdAt;
}