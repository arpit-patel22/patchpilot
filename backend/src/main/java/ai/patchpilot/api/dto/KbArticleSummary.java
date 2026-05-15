package ai.patchpilot.api.dto;

import ai.patchpilot.api.model.KnowledgeBaseArticle;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KbArticleSummary {

    private Long id;
    private String title;
    private String category;
    private String snippet;

    public static KbArticleSummary from(KnowledgeBaseArticle entity) {
        String snippet = null;
        if (entity.getRootCause() != null && !entity.getRootCause().isBlank()) {
            snippet = entity.getRootCause().length() > 150
                    ? entity.getRootCause().substring(0, 150) + "..."
                    : entity.getRootCause();
        }
        return KbArticleSummary.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .category(entity.getCategory() != null ? entity.getCategory().name() : null)
                .snippet(snippet)
                .build();
    }
}
