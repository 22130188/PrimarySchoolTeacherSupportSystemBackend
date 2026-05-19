package vn.edu.primary.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tests")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String subject;

    @Column
    private String grade; 
    @Column(nullable = false)
    private Integer duration; 

    @Column(nullable = false)
    private Long createdBy; 

    @Column(nullable = false)
    private String createdByName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String docxFileUrl; 

    @Column
    private String description;

    @Column
    private Integer totalPoints;

    @Column
    private Integer questionCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 50)
    private TestType testType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestStatus status; 

    @Column
    private String cloudinaryPublicId; 

    @Column
    private String lessonContentName;
}
