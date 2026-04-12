package vn.edu.primary.teacher_support.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${classroom.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendInvitationEmail(String toEmail, String classroomName, String teacherName, String invitationToken) {
        String joinUrl = frontendBaseUrl + "/join/invitation?token=" + invitationToken;
        String subject = "Lời mời tham gia lớp học: " + classroomName + " - TeachAI";

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 32px; border: 1px solid #e5e7eb; border-radius: 16px;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display:inline-block; background: linear-gradient(135deg, #7c3aed, #0ea5e9); border-radius: 12px; padding: 12px 20px;">
                            <span style="color:white; font-size: 20px; font-weight: bold;">📚 TeachAI</span>
                        </div>
                    </div>
                    <h2 style="color: #1f2937; text-align: center; margin-bottom: 8px;">Lời mời tham gia lớp học</h2>
                    <p style="color: #6b7280; text-align: center; margin-bottom: 16px;">
                        Giáo viên <strong>%s</strong> đã mời bạn tham gia lớp học:
                    </p>
                    <div style="background: #f5f3ff; border: 2px solid #7c3aed; border-radius: 12px; padding: 20px; text-align: center; margin-bottom: 24px;">
                        <span style="font-size: 24px; font-weight: bold; color: #6d28d9;">%s</span>
                    </div>
                    <div style="text-align: center; margin-bottom: 24px;">
                        <a href="%s"
                           style="display: inline-block; background: linear-gradient(135deg, #7c3aed, #0ea5e9); color: white; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 16px;">
                            Tham gia lớp học
                        </a>
                    </div>
                    <p style="color: #9ca3af; font-size: 13px; text-align: center;">
                        Hoặc copy link này vào trình duyệt:<br/>
                        <a href="%s" style="color: #6d28d9; word-break: break-all;">%s</a>
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #d1d5db; font-size: 11px; text-align: center;">© 2025 TeachAI — Hệ thống hỗ trợ giáo viên tiểu học</p>
                </div>
                """
                .formatted(teacherName, classroomName, joinUrl, joinUrl, joinUrl);

        sendHtmlEmail(toEmail, subject, html);
    }

    public void sendRegistrationInviteEmail(String toEmail, String classroomName, String teacherName,
            String invitationToken) {
        String registerUrl = frontendBaseUrl + "/register?invite_token=" + invitationToken;
        String subject = "Mời bạn đăng ký và tham gia lớp học: " + classroomName + " - TeachAI";

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 32px; border: 1px solid #e5e7eb; border-radius: 16px;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display:inline-block; background: linear-gradient(135deg, #7c3aed, #0ea5e9); border-radius: 12px; padding: 12px 20px;">
                            <span style="color:white; font-size: 20px; font-weight: bold;">📚 TeachAI</span>
                        </div>
                    </div>
                    <h2 style="color: #1f2937; text-align: center; margin-bottom: 8px;">Mời tham gia lớp học</h2>
                    <p style="color: #6b7280; text-align: center; margin-bottom: 16px;">
                        Giáo viên <strong>%s</strong> đã mời bạn tham gia lớp học <strong>%s</strong>.<br/>
                        Vui lòng đăng ký tài khoản để tham gia.
                    </p>
                    <div style="text-align: center; margin-bottom: 24px;">
                        <a href="%s"
                           style="display: inline-block; background: linear-gradient(135deg, #7c3aed, #0ea5e9); color: white; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 16px;">
                            Đăng ký & Tham gia
                        </a>
                    </div>
                    <p style="color: #9ca3af; font-size: 13px; text-align: center;">
                        Hãy đăng ký bằng đúng email <strong>%s</strong> để nhận lời mời.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #d1d5db; font-size: 11px; text-align: center;">© 2025 TeachAI — Hệ thống hỗ trợ giáo viên tiểu học</p>
                </div>
                """
                .formatted(teacherName, classroomName, registerUrl, toEmail);

        sendHtmlEmail(toEmail, subject, html);
    }

    private void sendHtmlEmail(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("22130248@st.hcmuaf.edu.vn", "TeachAI");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);

            log.info("Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
