package ai.patchpilot.api.controller;

import ai.patchpilot.api.dto.KbArticleSummary;
import ai.patchpilot.api.model.KnowledgeBaseArticle;
import ai.patchpilot.api.repository.KnowledgeBaseArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
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

    private KnowledgeBaseArticle.Category parseCategory(String value) {
        try {
            return KnowledgeBaseArticle.Category.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
