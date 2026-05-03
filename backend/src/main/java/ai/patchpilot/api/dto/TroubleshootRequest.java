package ai.patchpilot.api.dto;

import ai.patchpilot.api.model.TroubleshootingTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TroubleshootRequest {

    @NotBlank(message = "Software name is required")
    private String software;

    private String softwareVersion;

    @NotBlank(message = "OS version is required")
    private String osVersion;

    @NotBlank(message = "Error message is required")
    private String errorMessage;

    private String userAttempts;

    @NotNull(message = "Severity is required")
    private TroubleshootingTicket.Severity severity;
}
