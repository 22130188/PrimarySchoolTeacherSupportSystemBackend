package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "help_guides", indexes = @Index(name = "idx_help_guide_published_order", columnList = "published,sort_order"))
@Getter @Setter @NoArgsConstructor
public class Guide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 160) private String slug;
    @Column(nullable = false, length = 220) private String title;
    @Column(length = 600) private String description;
    @Column(columnDefinition = "TEXT") private String note;
    @Column(nullable = false) private boolean published = true;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC") private List<GuideStep> steps = new ArrayList<>();
    public void replaceSteps(List<GuideStep> values) { steps.clear(); values.forEach(step -> { step.setGuide(this); steps.add(step); }); }
    @PreUpdate void touch() { updatedAt = LocalDateTime.now(); }
}
