package ai.patchpilot.api.dto;

import ai.patchpilot.api.model.TroubleshootingTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Inbound payload for POST /api/diagnose.
 *
 * <p>All size caps are deliberately generous (10k chars for error logs) but exist
 * to prevent abuse — without them, a bot could spam multi-megabyte payloads and
 * drain the Claude API budget on tokenization alone.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TroubleshootRequest {

    @NotBlank(message = "Software name is required")
    @Size(max = 200, message = "Software name must be 200 characters or fewer")
    private String software;

    @Size(max = 100, message = "Software version must be 100 characters or fewer")
    private String softwareVersion;

    @NotBlank(message = "OS version is required")
    @Size(max = 100, message = "OS version must be 100 characters or fewer")
    private String osVersion;

    @NotBlank(message = "Error message is required")
    @Size(max = 10000, message = "Error message must be 10,000 characters or fewer")
    private String errorMessage;

    @Size(max = 2000, message = "User attempts must be 2,000 characters or fewer")
    private String userAttempts;

    @NotNull(message = "Severity is required")
    private TroubleshootingTicket.Severity severity;
}