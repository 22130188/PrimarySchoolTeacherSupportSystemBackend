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
    private final AccountMailService accountMailService;

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
    public UserResponse createUser(CreateUserRequest req, User actor) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        Role.RoleName roleName = parseRole(req.getRole());
        if (roleName == Role.RoleName.ADMIN) {
            throw new RuntimeException("Không được tạo người dùng với vai trò Admin");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        String encodedPassword = passwordEncoder.encode(req.getPassword());
        user.setPassword(encodedPassword);
        user.setPasswordHash(encodedPassword);
        user.setFullName(req.getUsername());
        user.setPhone(req.getPhone());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setSchoolName(req.getSchoolName());
        user.setAvatarUrl(req.getAvatarUrl());
        user.setIsActive(true);
        user.setIsEmailVerified(false);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRole(roleName);
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
    public UserResponse updateUser(Long id, UpdateUserRequest req, User actor) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        String oldRoleName = user.getRoles().stream()
                .map(Role::getName)
                .max(Comparator.comparingInt(this::rolePriority))
                .map(Enum::name)
                .orElse(user.getRole() != null ? user.getRole().name() : null);

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
            String encodedPassword = passwordEncoder.encode(req.getPassword());
            user.setPassword(encodedPassword);
            user.setPasswordHash(encodedPassword);
        }

        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null) user.setDateOfBirth(req.getDateOfBirth());
        if (req.getSchoolName() != null) user.setSchoolName(req.getSchoolName());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());

        Role.RoleName newRoleName = null;
        boolean roleChanged = false;

        if (req.getRole() != null && !req.getRole().isBlank()) {
            newRoleName = parseRole(req.getRole());
            Role.RoleName oldRole = oldRoleName != null ? Role.RoleName.valueOf(oldRoleName) : null;
            validateRoleChange(actor, user, oldRole, newRoleName);

            Role newRole = roleRepository.findByName(newRoleName)
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + req.getRole()));

            roleChanged = oldRoleName == null || !oldRoleName.equalsIgnoreCase(newRoleName.name());
            user.setRole(newRoleName);
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
        if (roleChanged && newRoleName != null) {
            accountMailService.sendRoleChangedEmail(saved, oldRoleName, newRoleName.name());
        }
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id, User actor) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        if (isSameUser(actor, user)) {
            throw new RuntimeException("Bạn không thể khóa/mở khóa tài khoản của chính mình");
        }
        // Cột role NOT NULL — khôi phục từ user_roles nếu bị null
        if (user.getRole() == null) {
            Role.RoleName primaryRole = user.getRoles().stream()
                    .map(Role::getName)
                    .max(Comparator.comparingInt(this::rolePriority))
                    .orElse(Role.RoleName.STUDENT);
            user.setRole(primaryRole);
        }
        Boolean current = user.getIsActive();
        boolean nextActive = current == null || !current;
        user.setIsActive(nextActive);
        User saved = userRepository.save(user);
        if (nextActive) {
            accountMailService.sendAccountUnlockedEmail(saved);
        } else {
            accountMailService.sendAccountLockedEmail(saved);
        }
        return UserResponse.from(saved);
    }

    @Transactional
    public void deleteUser(Long id, User actor) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        if (isSameUser(actor, user)) {
            throw new RuntimeException("Bạn không thể xóa tài khoản của chính mình");
        }
        if (isAdminUser(user)) {
            throw new RuntimeException("Không được xóa tài khoản Admin");
        }
        userRepository.deleteById(id);
    }

    private void validateRoleChange(User actor, User target, Role.RoleName oldRole, Role.RoleName newRole) {
        if (newRole == null || oldRole == newRole) return;

        if (isSameUser(actor, target)) {
            throw new RuntimeException("Bạn không thể thay đổi vai trò của chính mình");
        }
        if (newRole == Role.RoleName.ADMIN) {
            throw new RuntimeException("Không được đổi người dùng thành Admin");
        }
        if (oldRole == Role.RoleName.ADMIN) {
            throw new RuntimeException("Không được hạ quyền một tài khoản Admin");
        }
    }

    private boolean isSameUser(User actor, User target) {
        if (actor == null || target == null) return false;
        if (actor.getId() != null && target.getId() != null && actor.getId().equals(target.getId())) {
            return true;
        }
        return actor.getUsername() != null
                && target.getUsername() != null
                && actor.getUsername().equalsIgnoreCase(target.getUsername());
    }

    private boolean isAdminUser(User user) {
        if (user == null) return false;
        if (user.getRole() == Role.RoleName.ADMIN) return true;
        return user.getRoles() != null
                && user.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ADMIN);
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
