package mircoservices.apigateway.filter;

import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class ActionLogClassifier {
    private static final Set<String> PREFIXES = Set.of("api", "admin", "student", "internal");
    private static final Set<String> SENSITIVE_MODULES = Set.of(
            "users", "roles", "permissions", "tests", "questions", "classrooms", "subjects", "categories",
            "members", "posts", "invitations"
    );

    private ActionLogClassifier() {}

    static Classification classify(HttpMethod method, String path) {
        return classify(method, path, null);
    }

    static Classification classify(HttpMethod method, String path, String purposeHeader) {
        String normalized = path.toLowerCase(Locale.ROOT);
        if (purposeHeader != null && "bilingual-lesson".equalsIgnoreCase(purposeHeader.trim())) {
            return new Classification("CREATE_BILINGUAL_LESSON", "lessons", resourceId(path), "WARNING");
        }
        if (hasSegment(normalized, "translate") && (hasSegment(normalized, "drafts") || hasSegment(normalized, "lessons"))) {
            return new Classification("CREATE_BILINGUAL_LESSON", "lessons", resourceId(path), "WARNING");
        }
        String module = module(path);
        // Ưu tiên module nghiệp vụ thay vì segment kỹ thuật cuối path
        if (hasSegment(normalized, "login") || hasSegment(normalized, "logout") || normalized.contains("oauth2/code")) {
            module = "auth";
        }
        if (isImagePath(normalized)) module = "images";
        if (isTtsPath(normalized)) module = "tts";
        if (isPronunciationPath(normalized)) module = "pronunciation";
        if (isProfilePath(normalized)) module = "profile";
        if (normalized.contains("toggle-status") || hasSegment(normalized, "toggle_status")) {
            module = "users";
        }
        if (normalized.contains("toggle-sharing") || hasSegment(normalized, "toggle_sharing")) {
            module = "questions";
        }
        // /api/lessons/drafts/** → bài giảng (không để module = drafts)
        if (hasSegment(normalized, "lessons") || hasSegment(normalized, "drafts")
                || hasSegment(normalized, "classroom-shares")
                || (hasSegment(normalized, "shares") && hasSegment(normalized, "lessons"))) {
            if (hasSegment(normalized, "lessons") || hasSegment(normalized, "drafts")
                    || hasSegment(normalized, "classroom-shares")) {
                module = "lessons";
            }
        }
        if (hasSegment(normalized, "invite") || hasSegment(normalized, "invitations") || hasSegment(normalized, "members")) {
            if (hasSegment(normalized, "classrooms")) module = "classrooms";
        }
        String action = action(method, normalized, module);
        String severity = severity(method, normalized, module, action);
        return new Classification(action, module, resourceId(path), severity);
    }

    private static boolean isImagePath(String path) {
        return path.equals("/save") || path.equals("/generate")
                || path.startsWith("/images")
                || path.contains("/canvas/upload-image")
                || path.contains("/canvas/save")
                || path.contains("/api/image");
    }

    private static boolean isTtsPath(String path) {
        return path.startsWith("/api/tts") || path.contains("/canvas/upload-audio");
    }

    private static boolean isPronunciationPath(String path) {
        return path.startsWith("/api/pronunciation") || path.contains("/pronunciation/");
    }

    private static boolean isProfilePath(String path) {
        return path.contains("/api/user/personal")
                || path.contains("/api/user/school")
                || path.contains("/api/user/classes")
                || path.contains("/api/user/avatar")
                || path.contains("/api/user/change-password");
    }

    private static String action(HttpMethod method, String path, String module) {
        if (hasSegment(path, "login") || path.contains("oauth2/code")) return "LOGIN";
        if (hasSegment(path, "logout")) return "LOGOUT";
        if (hasSegment(path, "password")) return "CHANGE_PASSWORD";
        if (hasSegment(path, "export") || hasSegment(path, "download")) return "EXPORT_" + upper(module);
        // Ảnh / TTS
        if (path.equals("/save") || path.endsWith("/save") || path.contains("/save-blob")) {
            return "SAVE_" + upper(module);
        }
        if (path.equals("/generate") || hasSegment(path, "generate")) {
            return "GENERATE_" + upper(module);
        }
        if (path.contains("upload-image") || path.contains("upload-audio") || hasSegment(path, "upload")) {
            return "UPLOAD_" + upper(module);
        }
        // Kiểm tra phát âm
        if (isPronunciationPath(path) && (hasSegment(path, "check") || method == HttpMethod.POST)) {
            return "CHECK_PRONUNCIATION";
        }
        // TTS
        if (isTtsPath(path) && hasSegment(path, "convert")) {
            return "CONVERT_TTS";
        }
        if (isTtsPath(path) && (path.endsWith("/save") || hasSegment(path, "save"))) {
            return "SAVE_TTS";
        }

        // Chia sẻ bài giảng
        if (hasSegment(path, "classroom-shares")) return "SHARE_LESSONS_CLASS";
        if (hasSegment(path, "share") || hasSegment(path, "shares")) return "SHARE_" + upper(module);

        // Khóa / mở khóa tài khoản
        if (path.contains("toggle-status") || hasSegment(path, "toggle_status") || hasSegment(path, "toggle-status")) {
            return "TOGGLE_USER_STATUS";
        }
        // Chia sẻ / bỏ chia sẻ câu hỏi
        if (path.contains("toggle-sharing") || hasSegment(path, "toggle_sharing") || hasSegment(path, "toggle-sharing")) {
            return "TOGGLE_QUESTION_SHARING";
        }
        // Hồ sơ cá nhân
        if (path.contains("/api/user/personal") || hasSegment(path, "personal")) {
            if (method == HttpMethod.PUT || method == HttpMethod.PATCH) return "UPDATE_PROFILE";
        }
        if (path.contains("/api/user/school") || (hasSegment(path, "school") && hasSegment(path, "user"))) {
            if (method == HttpMethod.PUT || method == HttpMethod.PATCH) return "UPDATE_SCHOOL_INFO";
        }
        if (path.contains("/api/user/classes") || (hasSegment(path, "classes") && hasSegment(path, "user"))) {
            if (method == HttpMethod.PUT || method == HttpMethod.PATCH) return "UPDATE_CLASSES_INFO";
        }
        if (path.contains("/avatar") || hasSegment(path, "avatar-url")) {
            if (method == HttpMethod.PUT || method == HttpMethod.PATCH || method == HttpMethod.POST) return "UPDATE_AVATAR";
        }
        if (path.contains("/change-password") || hasSegment(path, "change-password")) {
            return "CHANGE_PASSWORD";
        }

        // Thành viên / mời vào lớp
        if (hasSegment(path, "import-excel")) return "IMPORT_CLASSROOM_MEMBERS";
        if (hasSegment(path, "resend")) return "RESEND_CLASSROOM_INVITATION";
        if (hasSegment(path, "reset-invite-link")) return "RESET_CLASSROOM_INVITE_LINK";
        if (hasSegment(path, "reset-class-code")) return "RESET_CLASSROOM_CODE";
        if (hasSegment(path, "invite") && !hasSegment(path, "invitations")) return "INVITE_CLASSROOM_MEMBER";
        if (method == HttpMethod.DELETE && hasSegment(path, "members")) return "REMOVE_CLASSROOM_MEMBER";
        if (method == HttpMethod.DELETE && hasSegment(path, "invitations")) return "REVOKE_CLASSROOM_INVITATION";

        // Bảng tin / bình luận
        if (hasSegment(path, "comments")) {
            if (method == HttpMethod.POST) return "CREATE_COMMENTS";
            if (method == HttpMethod.DELETE) return "DELETE_COMMENTS";
            if (method == HttpMethod.PUT || method == HttpMethod.PATCH) return "UPDATE_COMMENTS";
        }
        if (hasSegment(path, "posts")) {
            if (method == HttpMethod.POST) return "CREATE_CLASSROOM_POSTS";
            if (method == HttpMethod.PUT || method == HttpMethod.PATCH) return "UPDATE_CLASSROOM_POSTS";
            if (method == HttpMethod.DELETE) return "DELETE_CLASSROOM_POSTS";
        }

        if (hasSegment(path, "join")) return "JOIN_" + upper(module);
        if (hasSegment(path, "submit")) return "SUBMIT_" + upper(module);
        if (hasSegment(path, "generate")) return "GENERATE_" + upper(module);

        // Bài giảng (drafts)
        if ("lessons".equals(module) || hasSegment(path, "drafts")) {
            if (method == HttpMethod.POST) return "CREATE_LESSONS";
            if (method == HttpMethod.PUT || method == HttpMethod.PATCH) return "UPDATE_LESSONS";
            if (method == HttpMethod.DELETE) return "DELETE_LESSONS";
        }

        if (method == HttpMethod.GET) {
            return resourceId(path) == null ? "VIEW_" + upper(module) + "_LIST" : "VIEW_" + upper(module) + "_DETAIL";
        }
        if (method == HttpMethod.POST) return "CREATE_" + upper(module);
        if (method == HttpMethod.PUT || method == HttpMethod.PATCH) return "UPDATE_" + upper(module);
        if (method == HttpMethod.DELETE) return "DELETE_" + upper(module);
        return method.name() + "_" + upper(module);
    }

    private static boolean hasSegment(String path, String segment) {
        for (String part : path.split("/")) {
            if (part.equalsIgnoreCase(segment)) return true;
        }
        return false;
    }

    private static String severity(HttpMethod method, String path, String module, String action) {
        if (action.equals("LOGIN") || action.equals("LOGOUT")) return "INFO";
        if (method == HttpMethod.DELETE || path.contains("permission") || path.contains("role") || action.equals("LOGIN_FAILED")) {
            return "ALERT";
        }
        if ((method == HttpMethod.PUT || method == HttpMethod.PATCH)
                && (SENSITIVE_MODULES.contains(module) || path.contains("password"))) {
            return "DANGER";
        }
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) return "WARNING";
        return "INFO";
    }

    /**
     * Lấy resource cuối path (không phải id/prefix).
     * /api/classrooms/35/posts → posts
     * /api/classrooms → classrooms
     * /api/classrooms/35/invite → invite
     */
    private static String module(String path) {
        List<String> meaningful = new ArrayList<>();
        for (String part : path.split("/")) {
            if (part.isBlank()) continue;
            String lower = part.toLowerCase(Locale.ROOT);
            if (PREFIXES.contains(lower) || looksLikeId(lower)) continue;
            meaningful.add(lower.replace('-', '_'));
        }
        if (meaningful.isEmpty()) return "system";
        return meaningful.get(meaningful.size() - 1);
    }

    static boolean hasResourceId(String path) {
        return resourceId(path) != null;
    }

    private static String resourceId(String path) {
        String[] parts = path.split("/");
        for (int index = parts.length - 1; index >= 0; index--) {
            if (!parts[index].isBlank() && looksLikeId(parts[index])) return parts[index];
        }
        return null;
    }

    private static boolean looksLikeId(String value) {
        return value.matches("\\d+") || value.matches("[0-9a-fA-F]{8}-[0-9a-fA-F-]{27,}");
    }

    private static String upper(String value) {
        return value.toUpperCase(Locale.ROOT).replace('-', '_');
    }

    record Classification(String action, String module, String resourceId, String severity) {}
}
