package vn.edu.primary.teacher_support.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ForgotPasswordService {

    private static final int RESET_TOKEN_BYTES = 32;
    private static final int RESET_TOKEN_MINUTES = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, ResetTokenEntry> resetTokens = new ConcurrentHashMap<>();

    public ForgotPasswordService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public void requestOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(user ->
                otpService.sendPasswordResetOtp(user.getEmail()));
    }

    public String verifyOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isEmpty()
                || !otpService.verifyPasswordResetOtp(normalizedEmail, otp)) {
            throw new RuntimeException("Mã OTP không đúng hoặc đã hết hạn");
        }

        byte[] tokenBytes = new byte[RESET_TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        resetTokens.put(normalizedEmail,
                new ResetTokenEntry(hash(token), LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES)));
        return token;
    }

    @Transactional
    public void resetPassword(String email, String resetToken, String newPassword) {
        String normalizedEmail = normalizeEmail(email);
        ResetTokenEntry entry = resetTokens.get(normalizedEmail);
        if (entry == null || LocalDateTime.now().isAfter(entry.expiresAt())
                || !MessageDigest.isEqual(entry.tokenHash(), hash(resetToken))) {
            resetTokens.remove(normalizedEmail);
            throw new RuntimeException("Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        resetTokens.remove(normalizedEmail);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private byte[] hash(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }

    private record ResetTokenEntry(byte[] tokenHash, LocalDateTime expiresAt) {}
}
