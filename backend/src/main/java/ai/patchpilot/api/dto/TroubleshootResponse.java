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
    private String diagnosis;
    private LocalDateTime createdAt;
}
