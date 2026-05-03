package ai.patchpilot.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_base_articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeBaseArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 100)
    private String software;

    @Size(max = 50)
    private String osTarget;

    @Size(max = 500)
    private String errorPattern;

    @Column(columnDefinition = "TEXT")
    private String rootCause;

    @Column(columnDefinition = "TEXT")
    private String resolutionSteps;

    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Category {
        DEPENDENCY, PERMISSION, ANTIVIRUS, DISK_SPACE, CORRUPTION, REGISTRY, NETWORK, OTHER
    }
}
