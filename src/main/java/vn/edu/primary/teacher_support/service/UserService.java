package vn.edu.primary.teacher_support.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.UpdateProfileRequest;
import vn.edu.primary.teacher_support.entity.TeacherClass;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    @Transactional
    public User updatePersonal(String username, UpdateProfileRequest req) {
        User user = findByUsername(username);
        if (req.getFullName() != null && !req.getFullName().isBlank())
            user.setUsername(req.getFullName().trim());
        if (req.getPhone() != null)
            user.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null && !req.getDateOfBirth().isBlank()) {
            try {
                user.setDateOfBirth(LocalDate.parse(req.getDateOfBirth(), DateTimeFormatter.ISO_DATE));
            } catch (Exception ignored) {}
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateSchool(String username, UpdateProfileRequest req) {
        User user = findByUsername(username);
        if (req.getSchoolName() != null)
            user.setSchoolName(req.getSchoolName().trim());
        return userRepository.save(user);
    }

    @Transactional
    public User updateClasses(String username, List<UpdateProfileRequest.ClassItem> classes) {
        User user = findByUsername(username);
        user.getTeacherClasses().clear();
        if (classes != null) {
            for (UpdateProfileRequest.ClassItem item : classes) {
                if (item.getGrade() != null && item.getSubject() != null) {
                    user.getTeacherClasses().add(new TeacherClass(user, item.getGrade(), item.getSubject()));
                }
            }
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateAvatarUrl(String username, String avatarUrl) {
        User user = findByUsername(username);
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, String currentPass, String newPass) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(currentPass, user.getPassword()))
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);
    }

    public Map<String, Object> toDTO(User user) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id",              user.getId());
        dto.put("username",        user.getUsername());
        dto.put("email",           user.getEmail());
        dto.put("avatarUrl",       user.getAvatarUrl());
        dto.put("phone",           user.getPhone());
        dto.put("dateOfBirth",     user.getDateOfBirth());
        dto.put("schoolName",      user.getSchoolName());
        dto.put("isEmailVerified", user.getIsEmailVerified());
        dto.put("roles",           user.getRoles().stream()
                .map(r -> r.getName().name()).collect(Collectors.toList()));
        if (user.getTeacherClasses() != null && !user.getTeacherClasses().isEmpty()) {
            dto.put("teacherClasses", user.getTeacherClasses().stream().map(tc -> {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("grade",   tc.getGrade());
                m.put("subject", tc.getSubject());
                return m;
            }).collect(Collectors.toList()));
        }
        if (user.getStudentInfo() != null)
            dto.put("grade", user.getStudentInfo().getGrade());
        return dto;
    }
}