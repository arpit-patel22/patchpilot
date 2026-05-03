package ai.patchpilot.api.repository;

import ai.patchpilot.api.model.KnowledgeBaseArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeBaseArticleRepository extends JpaRepository<KnowledgeBaseArticle, Long> {

    List<KnowledgeBaseArticle> findBySoftwareContainingIgnoreCaseOrErrorPatternContainingIgnoreCase(
            String software, String errorPattern);
}
