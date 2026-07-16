package mircoservices.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionLogClassifierTest {

    @Test
    void classifiesPasswordResetRequestWithoutSensitiveData() {
        ActionLogClassifier.Classification result = ActionLogClassifier.classify(
                HttpMethod.POST, "/api/auth/forgot-password/request");

        assertEquals("REQUEST_PASSWORD_RESET_OTP", result.action());
        assertEquals("auth", result.module());
        assertEquals("INFO", result.severity());
        assertTrue(ActionLoggingGlobalFilter.shouldLog(
                HttpMethod.POST, "/api/auth/forgot-password/request"));
    }

    @Test
    void classifiesPasswordResetOtpVerification() {
        ActionLogClassifier.Classification result = ActionLogClassifier.classify(
                HttpMethod.POST, "/api/auth/forgot-password/verify");

        assertEquals("VERIFY_PASSWORD_RESET_OTP", result.action());
        assertEquals("auth", result.module());
        assertEquals("INFO", result.severity());
    }

    @Test
    void classifiesPasswordResetAsDangerousChange() {
        ActionLogClassifier.Classification result = ActionLogClassifier.classify(
                HttpMethod.POST, "/api/auth/forgot-password/reset");

        assertEquals("RESET_PASSWORD", result.action());
        assertEquals("auth", result.module());
        assertEquals("DANGER", result.severity());
    }

    @Test
    void raisesFailedVerificationAndResetToAlert() {
        assertEquals("ALERT", ActionLoggingGlobalFilter.severityFor(
                "VERIFY_PASSWORD_RESET_OTP", "INFO", true));
        assertEquals("ALERT", ActionLoggingGlobalFilter.severityFor(
                "RESET_PASSWORD", "DANGER", true));
        assertEquals("DANGER", ActionLoggingGlobalFilter.severityFor(
                "RESET_PASSWORD", "DANGER", false));
    }
}
