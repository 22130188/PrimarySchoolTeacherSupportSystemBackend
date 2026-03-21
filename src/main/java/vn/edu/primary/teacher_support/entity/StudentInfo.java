package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_info")
public class StudentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String grade;   // VD: 1A, 2B, 5C

    public StudentInfo() {}

    public StudentInfo(User user, String grade) {
        this.user  = user;
        this.grade = grade;
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}