package ai.patchpilot.api.controller;

import ai.patchpilot.api.dto.KbArticleDetail;
import ai.patchpilot.api.dto.KbArticleSummary;
import ai.patchpilot.api.model.KnowledgeBaseArticle;
import ai.patchpilot.api.repository.KnowledgeBaseArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KbController {

    private final KnowledgeBaseArticleRepository kbRepo;

    @GetMapping
    public List<KbArticleSummary> listArticles(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "8") int limit
    ) {
        long startedAt = System.currentTimeMillis();
        int clampedLimit = Math.max(1, Math.min(50, limit));

        List<KbArticleSummary> results;

        if (category != null && !category.isBlank()) {
            KnowledgeBaseArticle.Category cat = parseCategory(category);
            if (cat == null) {
                results = List.of();
            } else {
                results = kbRepo.findByCategoryOrderByIdAsc(cat).stream()
                        .limit(clampedLimit)
                        .map(KbArticleSummary::from)
                        .toList();
            }
        } else {
            results = kbRepo.findAll(PageRequest.of(0, clampedLimit, Sort.by("id").ascending()))
                    .stream()
                    .map(KbArticleSummary::from)
                    .toList();
        }

        log.info("GET /api/kb category={} limit={} returned {} articles in {}ms",
                category, clampedLimit, results.size(), System.currentTimeMillis() - startedAt);

        return results;
    }

    /**
     * Returns the full content of a single KB article for the detail modal.
     * 200 with the article if found, 404 if no article matches the id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KbArticleDetail> getArticle(@PathVariable Long id) {
        long startedAt = System.currentTimeMillis();

        return kbRepo.findById(id)
                .map(article -> {
                    log.info("GET /api/kb/{} returned article '{}' in {}ms",
                            id, article.getTitle(), System.currentTimeMillis() - startedAt);
                    return ResponseEntity.ok(KbArticleDetail.from(article));
                })
                .orElseGet(() -> {
                    log.info("GET /api/kb/{} not found in {}ms",
                            id, System.currentTimeMillis() - startedAt);
                    return ResponseEntity.notFound().build();
                });
    }

    private KnowledgeBaseArticle.Category parseCategory(String value) {
        try {
            return KnowledgeBaseArticle.Category.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
