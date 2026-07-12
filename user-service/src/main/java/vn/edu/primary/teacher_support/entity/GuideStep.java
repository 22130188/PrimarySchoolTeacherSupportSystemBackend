package vn.edu.primary.teacher_support.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "help_guide_steps", indexes = @Index(name = "idx_help_step_guide_order", columnList = "guide_id,sort_order"))
@Getter @Setter @NoArgsConstructor
public class GuideStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "guide_id", nullable = false) private Guide guide;
    @Column(nullable = false, length = 220) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "image_url", length = 1000) private String imageUrl;
    @Column(name = "image_alt", length = 300) private String imageAlt;
    @Column(name = "video_url", length = 1000) private String videoUrl;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
}
