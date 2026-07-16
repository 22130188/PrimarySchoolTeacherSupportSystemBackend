package vn.edu.primary.teacher_support.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionLogLabelsTest {

    @Test
    void usesVietnameseLabelsForPasswordResetActions() {
        assertEquals("Yêu cầu OTP đặt lại mật khẩu",
                ActionLogLabels.label("REQUEST_PASSWORD_RESET_OTP"));
        assertEquals("Xác thực OTP đặt lại mật khẩu",
                ActionLogLabels.label("VERIFY_PASSWORD_RESET_OTP"));
        assertEquals("Đặt lại mật khẩu",
                ActionLogLabels.label("RESET_PASSWORD"));
    }
}
