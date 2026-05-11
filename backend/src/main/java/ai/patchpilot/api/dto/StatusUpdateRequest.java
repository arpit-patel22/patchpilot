package ai.patchpilot.api.dto;

import ai.patchpilot.api.model.TroubleshootingTicket;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Inbound payload for PATCH /api/tickets/{id}/status.
 *
 * <p>Wraps the new status in a body object instead of a query param because PATCH
 * bodies are the conventional REST shape for partial updates — keeps the contract
 * consistent if we add more updatable fields later (e.g. assigned-to, notes).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private TroubleshootingTicket.Status status;
}