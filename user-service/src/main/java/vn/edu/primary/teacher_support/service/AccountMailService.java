package vn.edu.primary.teacher_support.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.edu.primary.teacher_support.entity.User;

@Service
public class AccountMailService {

    private static final Logger log = LoggerFactory.getLogger(AccountMailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.test-mode:false}")
    private boolean mailTestMode;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public AccountMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendAccountLockedEmail(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
        String subject = "Tài khoản của bạn đã bị khóa - TeachPrimary";
        String html = buildTemplate(
                "Tài khoản đã bị khóa",
                "Xin chào <strong>" + escape(displayName(user)) + "</strong>,",
                "Tài khoản <strong>" + escape(user.getUsername()) + "</strong> của bạn đã bị quản trị viên khóa (Ngừng hoạt động).",
                "Bạn sẽ không thể đăng nhập cho đến khi tài khoản được mở khóa lại. Nếu bạn cho rằng đây là nhầm lẫn, vui lòng liên hệ quản trị viên hệ thống."
        );
        sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendAccountUnlockedEmail(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
        String subject = "Tài khoản của bạn đã được mở khóa - TeachPrimary";
        String html = buildTemplate(
                "Tài khoản đã được mở khóa",
                "Xin chào <strong>" + escape(displayName(user)) + "</strong>,",
                "Tài khoản <strong>" + escape(user.getUsername()) + "</strong> của bạn đã được mở khóa và có thể đăng nhập lại bình thường.",
                "Nếu bạn không yêu cầu thay đổi này, vui lòng liên hệ quản trị viên hệ thống."
        );
        sendHtml(user.getEmail(), subject, html);
    }

    @Async
    public void sendRoleChangedEmail(User user, String oldRole, String newRole) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
        if (oldRole == null || newRole == null || oldRole.equalsIgnoreCase(newRole)) return;
        String subject = "Quyền tài khoản của bạn đã được thay đổi - TeachPrimary";
        String html = buildTemplate(
                "Quyền tài khoản đã thay đổi",
                "Xin chào <strong>" + escape(displayName(user)) + "</strong>,",
                "Quản trị viên đã thay đổi quyền tài khoản <strong>" + escape(user.getUsername()) + "</strong> của bạn từ <strong>"
                        + escape(roleLabel(oldRole)) + "</strong> sang <strong>" + escape(roleLabel(newRole)) + "</strong>.",
                "Vui lòng đăng nhập lại để áp dụng quyền mới. Nếu bạn không yêu cầu thay đổi này, hãy liên hệ quản trị viên."
        );
        sendHtml(user.getEmail(), subject, html);
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }

    private String roleLabel(String role) {
        if (role == null) return "Không xác định";
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "Admin";
            case "TEACHER" -> "Giáo viên";
            case "STUDENT" -> "Học sinh";
            default -> role;
        };
    }

    private String buildTemplate(String title, String greeting, String body, String footerNote) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto; padding: 32px; border: 1px solid #e5e7eb; border-radius: 16px;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display:inline-block; background: linear-gradient(135deg, #7c3aed, #0ea5e9); border-radius: 12px; padding: 12px 20px;">
                            <span style="color:white; font-size: 20px; font-weight: bold;">📚 TeachPrimary</span>
                        </div>
                    </div>
                    <h2 style="color: #1f2937; text-align: center; margin-bottom: 16px;">%s</h2>
                    <p style="color: #374151; line-height: 1.6; margin-bottom: 12px;">%s</p>
                    <div style="background: #f5f3ff; border: 1px solid #ddd6fe; border-radius: 12px; padding: 16px; color: #4c1d95; line-height: 1.6; margin-bottom: 16px;">
                        %s
                    </div>
                    <p style="color: #6b7280; font-size: 14px; line-height: 1.6;">%s</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #d1d5db; font-size: 11px; text-align: center;">© 2026 TeachPrimary — Hệ thống hỗ trợ giáo viên tiểu học</p>
                </div>
                """.formatted(title, greeting, body, footerNote);
    }

    private void sendHtml(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String fromAddress = (mailUsername != null && !mailUsername.isBlank()) ? mailUsername : "no-reply@teachprimary.local";
            helper.setFrom(fromAddress, "TeachPrimary");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            if (mailTestMode) {
                log.info("[ACCOUNT MAIL TEST MODE] To: {} | Subject: {}", toEmail, subject);
                log.debug("[ACCOUNT MAIL TEST MODE] HTML:\n{}", html);
            } else {
                mailSender.send(message);
                log.info("Account notification email sent to {}", toEmail);
            }
        } catch (Exception e) {
            log.error("Failed to send account email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
