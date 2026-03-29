package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.*;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.RoleRepository;
import vn.edu.primary.teacher_support.repository.TeacherClassRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getUsers(String keyword, String role) {
        Role.RoleName roleName = (role != null && !role.isBlank()) ? parseRole(role) : null;
        List<User> users = userRepository.searchUsers(keyword, roleName);
        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setSchoolName(req.getSchoolName());
        user.setAvatarUrl(req.getAvatarUrl());
        user.setIsActive(true);
        user.setIsEmailVerified(false);

        Role.RoleName roleName = parseRole(req.getRole());
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        User saved = userRepository.save(user);

        if (roleName == Role.RoleName.STUDENT && req.getGrade() != null && !req.getGrade().isBlank()) {
            StudentInfo si = new StudentInfo(saved, req.getGrade());
            saved.setStudentInfo(si);
        }

        if (roleName == Role.RoleName.TEACHER && req.getTeacherClasses() != null && !req.getTeacherClasses().isEmpty()) {
            Set<TeacherClass> teacherClasses = new HashSet<>();
            for (TeacherClassDto dto : req.getTeacherClasses()) {
                if (dto.getGrade() != null && dto.getSubject() != null) {
                    teacherClasses.add(new TeacherClass(saved, dto.getGrade(), dto.getSubject()));
                }
            }
            saved.setTeacherClasses(teacherClasses);
        }

        saved = userRepository.save(saved);
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            if (!req.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(req.getUsername())) {
                throw new RuntimeException("Username đã tồn tại");
            }
            user.setUsername(req.getUsername());
        }

        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (!req.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(req.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng");
            }
            user.setEmail(req.getEmail());
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null) user.setDateOfBirth(req.getDateOfBirth());
        if (req.getSchoolName() != null) user.setSchoolName(req.getSchoolName());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());

        Role.RoleName newRoleName = null;

        if (req.getRole() != null && !req.getRole().isBlank()) {
            newRoleName = parseRole(req.getRole());
            Role newRole = roleRepository.findByName(newRoleName)
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + req.getRole()));

            user.getRoles().clear();
            user.getRoles().add(newRole);
        } else {
            newRoleName = user.getRoles().stream()
                    .map(Role::getName)
                    .max(Comparator.comparingInt(this::rolePriority))
                    .orElse(null);
        }

        if (newRoleName == Role.RoleName.STUDENT) {
            if (req.getGrade() != null && !req.getGrade().isBlank()) {
                if (user.getStudentInfo() == null) {
                    user.setStudentInfo(new StudentInfo(user, req.getGrade()));
                } else {
                    user.getStudentInfo().setGrade(req.getGrade());
                }
            }
            teacherClassRepository.deleteByUser(user);
            user.getTeacherClasses().clear();
        } else if (newRoleName == Role.RoleName.TEACHER) {
            if (user.getStudentInfo() != null) {
                user.setStudentInfo(null);
            }
            if (req.getTeacherClasses() != null) {
                teacherClassRepository.deleteByUser(user);
                user.getTeacherClasses().clear();
                for (TeacherClassDto dto : req.getTeacherClasses()) {
                    if (dto.getGrade() != null && dto.getSubject() != null) {
                        user.getTeacherClasses().add(new TeacherClass(user, dto.getGrade(), dto.getSubject()));
                    }
                }
            }
        } else if (newRoleName == Role.RoleName.ADMIN) {
            if (user.getStudentInfo() != null) {
                user.setStudentInfo(null);
            }
            teacherClassRepository.deleteByUser(user);
            user.getTeacherClasses().clear();
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        user.setIsActive(!user.getIsActive());
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id);
        }
        userRepository.deleteById(id);
    }

    private Role.RoleName parseRole(String role) {
        return switch (role.toUpperCase()) {
            case "STUDENT" -> Role.RoleName.STUDENT;
            case "TEACHER" -> Role.RoleName.TEACHER;
            case "ADMIN" -> Role.RoleName.ADMIN;
            default -> throw new RuntimeException("Role không hợp lệ: " + role);
        };
    }

    private int rolePriority(Role.RoleName roleName) {
        return switch (roleName) {
            case STUDENT -> 0;
            case TEACHER -> 1;
            case ADMIN -> 2;
        };
    }
}
