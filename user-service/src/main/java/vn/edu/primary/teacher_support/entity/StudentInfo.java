package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_info")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "user")
public class StudentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String grade;

    public StudentInfo(User user, String grade) {
        this.user = user;
        this.grade = grade;
    }
}