package vn.edu.primary.teacher_support.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int PASSWORD_RESET_RESEND_SECONDS = 60;
    private static final String REGISTRATION = "REGISTRATION";
    private static final String PASSWORD_RESET = "PASSWORD_RESET";

    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.test-mode:false}")
    private boolean mailTestMode;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastPasswordResetSentAt = new ConcurrentHashMap<>();

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email) {
        sendOtp(email, REGISTRATION);
    }

    public boolean verifyOtp(String email, String otp) {
        return verifyOtp(email, otp, REGISTRATION);
    }

    public void sendPasswordResetOtp(String email) {
        String key = storeKey(email.trim().toLowerCase(), PASSWORD_RESET);
        LocalDateTime lastSentAt = lastPasswordResetSentAt.get(key);
        if (lastSentAt != null
                && LocalDateTime.now().isBefore(lastSentAt.plusSeconds(PASSWORD_RESET_RESEND_SECONDS))) {
            return;
        }
        sendOtp(email, PASSWORD_RESET);
        lastPasswordResetSentAt.put(key, LocalDateTime.now());
    }

    public boolean verifyPasswordResetOtp(String email, String otp) {
        return verifyOtp(email, otp, PASSWORD_RESET);
    }

    private void sendOtp(String email, String purpose) {
        String normalizedEmail = email.trim().toLowerCase();
        String otp = generateOtp();
        otpStore.put(storeKey(normalizedEmail, purpose),
                new OtpEntry(otp, LocalDateTime.now().plusMinutes(5), 0));
        sendOtpEmail(normalizedEmail, otp, purpose);
    }

    private boolean verifyOtp(String email, String otp, String purpose) {
        String key = storeKey(email.trim().toLowerCase(), purpose);
        OtpEntry entry = otpStore.get(key);

        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            otpStore.remove(key);
            return false;
        }
        if (!entry.otp().equals(otp.trim())) {
            int attempts = entry.failedAttempts() + 1;
            if (attempts >= MAX_VERIFY_ATTEMPTS) otpStore.remove(key);
            else otpStore.put(key, new OtpEntry(entry.otp(), entry.expiresAt(), attempts));
            return false;
        }

        otpStore.remove(key);
        return true;
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String storeKey(String email, String purpose) {
        return purpose + ":" + email;
    }

    private void sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String fromAddress = (mailUsername != null && !mailUsername.isBlank()) ? mailUsername : "no-reply@teachai.local";
            helper.setFrom(fromAddress, "TeachAI");
            helper.setTo(toEmail);
            boolean passwordReset = PASSWORD_RESET.equals(purpose);
            String subject = passwordReset ? "Mã OTP đặt lại mật khẩu - TeachAI" : "Mã xác thực OTP - TeachAI";
            String title = passwordReset ? "Đặt lại mật khẩu" : "Xác thực tài khoản";
            String description = passwordReset
                    ? "Dùng mã dưới đây để đặt lại mật khẩu của bạn:"
                    : "Mã OTP của bạn là:";
            helper.setSubject(subject);

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 32px; border: 1px solid #e5e7eb; border-radius: 16px;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display:inline-block; background: linear-gradient(135deg, #7c3aed, #0ea5e9); border-radius: 12px; padding: 12px 20px;">
                            <span style="color:white; font-size: 20px; font-weight: bold;">📚 TeachAI</span>
                        </div>
                    </div>
                    <h2 style="color: #1f2937; text-align: center; margin-bottom: 8px;">%s</h2>
                    <p style="color: #6b7280; text-align: center; margin-bottom: 24px;">%s</p>
                    <div style="background: #f5f3ff; border: 2px dashed #7c3aed; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 24px;">
                        <span style="font-size: 40px; font-weight: bold; letter-spacing: 12px; color: #6d28d9;">%s</span>
                    </div>
                    <p style="color: #9ca3af; font-size: 13px; text-align: center;">Mã có hiệu lực trong <strong>5 phút</strong>. Không chia sẻ mã này với ai.</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #d1d5db; font-size: 11px; text-align: center;">© 2025 TeachAI — Hệ thống hỗ trợ giáo viên tiểu học</p>
                </div>
                """.formatted(title, description, otp);

            helper.setText(html, true);

            if (mailTestMode) {
                log.info("[OTP TEST MODE] To: {} | OTP: {}", toEmail, otp);
                log.debug("[OTP TEST MODE] HTML:\n{}", html);
            } else {
                mailSender.send(message);
            }

        } catch (Exception e) {
            log.error("Không thể gửi email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    private record OtpEntry(String otp, LocalDateTime expiresAt, int failedAttempts) {}
}
