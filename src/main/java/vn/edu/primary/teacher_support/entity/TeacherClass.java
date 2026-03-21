package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_classes")
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

    public TeacherClass() {}

    public TeacherClass(User user, String grade, String subject) {
        this.user    = user;
        this.grade   = grade;
        this.subject = subject;
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}