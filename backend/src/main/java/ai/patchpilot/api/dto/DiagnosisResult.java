package ai.patchpilot.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * Typed representation of Claude's diagnosis output.
 *
 * <p>Stored in the DB as a raw JSON string on {@code TroubleshootingTicket.aiDiagnosis},
 * but exposed at the API boundary as a structured object so frontend consumers
 * don't have to {@code JSON.parse()} a stringified blob.</p>
 *
 * <p>Mirrors the JSON contract in {@code ClaudeService.SYSTEM_PROMPT} — keep field
 * names in sync if the prompt changes.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiagnosisResult {

    /** The single best first action the user should take immediately. */
    private String tryThisFirst;

    /** Three ranked causes, ordered by probability descending. */
    private List<CauseDetail> causes;
}