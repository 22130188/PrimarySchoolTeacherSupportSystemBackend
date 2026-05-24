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

        return userRepository.findByEmail(email).orElseGet(() -> {

            User newUser = new User();
            newUser.setEmail(email);

            String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
            String username     = generateUniqueUsername(baseUsername);
            newUser.setUsername(username);

            newUser.setPassword(UUID.randomUUID().toString());

//            newUser.setAvatarUrl(avatarUrl);

            newUser.setSchoolName("");
            newUser.setIsEmailVerified(true);
            newUser.setIsActive(true);

                Role studentRole = roleRepository
                    .findByName(Role.RoleName.STUDENT)
                    .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.STUDENT)));

                Set<Role> roles = new HashSet<>();
                roles.add(studentRole);
                newUser.setRoles(roles);

            return userRepository.save(newUser);
        });
    }

    private String generateUniqueUsername(String base) {
        String candidate = base;
        int suffix = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}