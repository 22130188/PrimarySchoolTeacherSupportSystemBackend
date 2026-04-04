package vn.edu.primary.teacher_support.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final JavaMailSender mailSender;

    // Lưu OTP tạm thời trong bộ nhớ: email → OtpEntry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    //  Tạo và gửi OTP
    public void sendOtp(String email) {
        String otp = generateOtp();

        // Lưu vào store, hết hạn sau 5 phút
        otpStore.put(email, new OtpEntry(otp, LocalDateTime.now().plusMinutes(5)));

        // Gửi email
        sendOtpEmail(email, otp);
    }

    //  Xác thực OTP
    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);

        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            otpStore.remove(email);
            return false;
        }
        if (!entry.otp().equals(otp.trim())) return false;

        // Xác thực thành công → xóa khỏi store
        otpStore.remove(email);
        return true;
    }

    //  Helper: tạo OTP 6 số
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    // Helper: gửi email HTML đẹp
    private void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("22130248@st.hcmuaf.edu.vn", "TeachAI");
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP - TeachAI");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 32px; border: 1px solid #e5e7eb; border-radius: 16px;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display:inline-block; background: linear-gradient(135deg, #7c3aed, #0ea5e9); border-radius: 12px; padding: 12px 20px;">
                            <span style="color:white; font-size: 20px; font-weight: bold;">📚 TeachAI</span>
                        </div>
                    </div>
                    <h2 style="color: #1f2937; text-align: center; margin-bottom: 8px;">Xác thực tài khoản</h2>
                    <p style="color: #6b7280; text-align: center; margin-bottom: 24px;">Mã OTP của bạn là:</p>
                    <div style="background: #f5f3ff; border: 2px dashed #7c3aed; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 24px;">
                        <span style="font-size: 40px; font-weight: bold; letter-spacing: 12px; color: #6d28d9;">%s</span>
                    </div>
                    <p style="color: #9ca3af; font-size: 13px; text-align: center;">Mã có hiệu lực trong <strong>5 phút</strong>. Không chia sẻ mã này với ai.</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #d1d5db; font-size: 11px; text-align: center;">© 2025 TeachAI — Hệ thống hỗ trợ giáo viên tiểu học</p>
                </div>
                """.formatted(otp);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    //  Inner record lưu OTP + thời gian hết hạn
    private record OtpEntry(String otp, LocalDateTime expiresAt) {}
}