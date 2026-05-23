package ai.patchpilot.api.dto;

import ai.patchpilot.api.model.KnowledgeBaseArticle;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Full KB article shape for the article detail modal (GET /api/kb/{id}).
 * Differs from KbArticleSummary by including rootCause and resolutionSteps in full.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KbArticleDetail {

    private Long id;
    private String title;
    private String software;
    private String osTarget;
    private String category;
    private String rootCause;
    private String resolutionSteps;
    private LocalDateTime createdAt;

    public static KbArticleDetail from(KnowledgeBaseArticle entity) {
        return KbArticleDetail.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .software(entity.getSoftware())
                .osTarget(entity.getOsTarget())
                .category(entity.getCategory() != null ? entity.getCategory().name() : null)
                .rootCause(entity.getRootCause())
                .resolutionSteps(entity.getResolutionSteps())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}