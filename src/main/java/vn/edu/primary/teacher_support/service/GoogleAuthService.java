package vn.edu.primary.teacher_support.service;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.RoleRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public GoogleAuthService(UserRepository userRepository,
                             RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User findOrCreateUser(OAuth2User oAuth2User) {

        String email     = oAuth2User.getAttribute("email");
        String name      = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        // Nếu đã có tài khoản với email này → trả về
        return userRepository.findByEmail(email).orElseGet(() -> {

            // Tạo user mới với giá trị mặc định cho các field bắt buộc
            User newUser = new User();
            newUser.setEmail(email);

            // Username = phần trước @ của email
            String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
            String username     = generateUniqueUsername(baseUsername);
            newUser.setUsername(username);

            // Mật khẩu random
            newUser.setPassword(UUID.randomUUID().toString());

//            newUser.setAvatarUrl(avatarUrl);

            newUser.setSchoolName("");
            newUser.setIsEmailVerified(true);
            newUser.setIsActive(true);

            // Gán role mặc định là STUDENT
            Role studentRole = roleRepository
                    .findByName(Role.RoleName.STUDENT)
                    .orElseThrow(() -> new RuntimeException("Role STUDENT không tồn tại"));

            Set<Role> roles = new HashSet<>();
            roles.add(studentRole);
            newUser.setRoles(roles);

            return userRepository.save(newUser);
        });
    }

    // Tạo username duy nhất nếu bị trùng
    private String generateUniqueUsername(String base) {
        String candidate = base;
        int suffix = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}