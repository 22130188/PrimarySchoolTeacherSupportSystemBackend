package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_classes")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"grade", "subject"})
@ToString(exclude = "user")
public class TeacherClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private String subject;

    public TeacherClass(User user, String grade, String subject) {
        this.user = user;
        this.grade = grade;
        this.subject = subject;
    }
}