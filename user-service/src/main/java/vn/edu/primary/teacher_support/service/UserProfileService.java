package vn.edu.primary.teacher_support.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.*;
import vn.edu.primary.teacher_support.repository.RoleRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserRepository userRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User updatePersonal(User user, UpdatePersonalRequest request) {
        // Kiểm tra xem tên người dùng đã được người dùng khác sử dụng chưa
        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("Username đã được sử dụng");
            }
        }

        // Kiểm tra xem email đó đã được người dùng khác sử dụng chưa
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã được sử dụng");
            }
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPosition(request.getPosition());
        user.setPhone(request.getPhone());

        return userRepository.save(user);
    }

    @Transactional
    public User updateSchool(User user, UpdateSchoolRequest request) {
        user.setSchoolName(request.getSchoolName());
        return userRepository.save(user);
    }

    @Transactional
    public User updateClasses(User user, UpdateClassesRequest request) {
        // Chỉ giáo viên mới có thể cập nhật lớp học
        if (!isTeacher(user)) {
            throw new RuntimeException("Chỉ giáo viên mới có thể cập nhật lớp học");
        }

        // Xóa các lớp hiện có
        user.getTeacherClasses().clear();

        // Thêm các lớp mới
        if (request.getClasses() != null && !request.getClasses().isEmpty()) {
            for (UpdateClassesRequest.TeacherClassDto dto : request.getClasses()) {
                TeacherClass teacherClass = new TeacherClass(user, dto.getGrade(), dto.getSubject());
                user.getTeacherClasses().add(teacherClass);
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateAvatar(User user, UpdateAvatarRequest request) {
        user.setAvatarUrl(request.getAvatarUrl());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        // Xác minh mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private boolean isTeacher(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TEACHER);
    }
}