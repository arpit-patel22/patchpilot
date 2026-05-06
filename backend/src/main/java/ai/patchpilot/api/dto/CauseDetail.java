package ai.patchpilot.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * Represents a single ranked root cause within a diagnosis result.
 *
 * <p>Mirrors the JSON contract enforced by Claude in {@code ClaudeService}'s
 * SYSTEM_PROMPT — keep field names in sync if the prompt changes.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CauseDetail {

    /** Short name for the cause (e.g. "Windows Installer Service Corruption"). */
    private String name;

    /** Independent likelihood of this being the cause, 0–100. */
    private Integer probability;

    /** 1–2 sentence plain-English explanation. */
    private String explanation;

    /** Ordered list of concrete remediation steps. */
    private List<String> steps;
}