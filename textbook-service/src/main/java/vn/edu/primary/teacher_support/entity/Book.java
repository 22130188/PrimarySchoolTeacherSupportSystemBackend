package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer grade;

    @Column(nullable = false, length = 50)
    private String subject;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "slug_id", nullable = false, unique = true, length = 190)
    private String slugId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "book_type", nullable = false, length = 50)
    private String bookType = "SGK";

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("pageNumber ASC")
    private List<Page> pages;
}
