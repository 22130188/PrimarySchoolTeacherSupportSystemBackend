package vn.edu.primary.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
    @JoinColumn(name = "test_id", nullable = false)
    @JsonBackReference
    private Test test;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type; // MULTIPLE_CHOICE, AUDIO

    @Column(nullable = false)
    private String content; // Nội dung câu hỏi

    @Column(nullable = false)
    private Integer points; // Điểm

    @Column
    private String title; // Tên đề (cho trắc nghiệm)

    @Column
    private Integer numberQuestions; // Số câu (cho trắc nghiệm)

    // Dữ liệu JSON cho các loại câu hỏi khác nhau
    @Column(columnDefinition = "JSON")
    private String answersJson; // JSON array chứa đáp án và đáp án đúng

    @Column
    private String audioUrl; // URL file ghi âm (cho audio)

    @Column
    private String imageUrl; // URL ảnh câu hỏi

    @Column
    private String transcript; // Phiên âm từ ghi âm

    @Column
    private Integer orderIndex; // Thứ tự câu hỏi trong bài kiểm tra
}
