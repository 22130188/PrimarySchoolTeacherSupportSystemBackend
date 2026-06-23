package vn.edu.primary.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "test_questions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = true)
    @JsonBackReference
    private Test test;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private QuestionType type; 

    @Column(nullable = false)
    private String content; 

    @Column(nullable = false)
    private Integer points;

    @Column
    private String title; 
    @Column
    private Integer numberQuestions; 

    @Column(columnDefinition = "JSON")
    private String answersJson; 

    @Column(name = "matching_pairs_json", columnDefinition = "LONGTEXT")
    private String matchingPairsJson; 

    @Column(name = "text_with_blanks", columnDefinition = "LONGTEXT")
    private String textWithBlanks; 
    @Column(name = "blanks_json", columnDefinition = "JSON")
    private String blanksJson; 
    @Column(columnDefinition = "TEXT")
    private String prompt; 
    @Column(name = "max_length")
    private Integer maxLength;

    @Column
    private String rubric; 
    @Column
    private String audioUrl;

    @Column
    private String imageUrl;

    @Column
    private String transcript;

    @Column
    private Integer orderIndex;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "is_shared")
    private Boolean isShared;

    @CreationTimestamp
    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
