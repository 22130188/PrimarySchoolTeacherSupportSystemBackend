package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "classroom_post_attachments", indexes = {
        @Index(name = "idx_post_attachment_post", columnList = "post_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ClassroomPostAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private ClassroomPost post;

    @Column(name = "drive_file_id", nullable = false, length = 128)
    private String driveFileId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "mime_type", length = 160)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "icon_link", columnDefinition = "TEXT")
    private String iconLink;

    @Column(name = "thumbnail_link", columnDefinition = "TEXT")
    private String thumbnailLink;

    @Column(name = "web_view_link", nullable = false, columnDefinition = "TEXT")
    private String webViewLink;

    @Column(name = "web_content_link", columnDefinition = "TEXT")
    private String webContentLink;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
