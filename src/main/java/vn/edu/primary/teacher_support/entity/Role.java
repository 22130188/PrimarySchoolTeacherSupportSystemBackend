package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {

    public enum RoleName {
        STUDENT, TEACHER, ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;

    public Role(RoleName name) {
        this.name = name;
    }
}