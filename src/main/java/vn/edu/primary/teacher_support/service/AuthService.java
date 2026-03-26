package vn.edu.primary.teacher_support.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.RegisterRequest;
import vn.edu.primary.teacher_support.entity.*;
import vn.edu.primary.teacher_support.repository.RoleRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository   userRepository;
    private final RoleRepository   roleRepository;
    private final PasswordEncoder  passwordEncoder;
    private final JwtService       jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
    }

    // REGISTER
    @Transactional
    public User register(RegisterRequest req) {

        // Kiểm tra trùng username / email
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Tạo user
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setSchoolName(req.getSchoolName());
        user.setIsEmailVerified(true);

        // Gán role
        Role.RoleName roleName = parseRole(req.getRole());
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        // Lưu user trước để có ID
        User saved = userRepository.save(user);

        // Học sinh: lưu lớp
        if (roleName == Role.RoleName.STUDENT) {
            if (req.getGrade() != null && !req.getGrade().isBlank()) {
                StudentInfo si = new StudentInfo(saved, req.getGrade());
                saved.setStudentInfo(si);
            }
        }

        // Giáo viên: lưu danh sách lớp+môn
        if (roleName == Role.RoleName.TEACHER) {
            if (req.getClasses() != null && !req.getClasses().isEmpty()) {
                Set<TeacherClass> teacherClasses = new HashSet<>();
                for (RegisterRequest.TeacherClassDto dto : req.getClasses()) {
                    if (dto.getGrade() != null && dto.getSubject() != null) {
                        teacherClasses.add(new TeacherClass(saved, dto.getGrade(), dto.getSubject()));
                    }
                }
                saved.setTeacherClasses(teacherClasses);
            }
            // Nếu không có lớp → vẫn lưu được (giáo viên mới)
        }

        return userRepository.save(saved);
    }

    // LOGIN - token + role
    public String[] login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!user.getIsActive()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        String token = jwtService.generateToken(user);
        String[] result = new String[3];
        result[0] = token;
        
        // role
        Role.RoleName primaryRole = user.getRoles().stream()
                .map(Role::getName)
                .max(java.util.Comparator.comparingInt(this::rolePriority))
                .orElse(Role.RoleName.STUDENT);
        
        result[1] = String.valueOf(getRoleId(primaryRole));
        result[2] = primaryRole.name();
        
        return result;
    }

    private Integer getRoleId(Role.RoleName roleName) {
        return switch (roleName) {
            case STUDENT -> 1;
            case TEACHER -> 2;
            case ADMIN -> 3;
        };
    }

    private int rolePriority(Role.RoleName roleName) {
        return switch (roleName) {
            case STUDENT -> 0;
            case TEACHER -> 1;
            case ADMIN -> 2;
        };
    }

    private Role.RoleName parseRole(String role) {
        return switch (role.toUpperCase()) {
            case "STUDENT"  -> Role.RoleName.STUDENT;
            case "TEACHER"  -> Role.RoleName.TEACHER;
            case "ADMIN"    -> Role.RoleName.ADMIN;
            default -> throw new RuntimeException("Role không hợp lệ: " + role);
        };
    }
}