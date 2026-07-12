package vn.edu.primary.teacher_support.util;

import java.util.Locale;
import java.util.Map;

/**
 * Chuyển mã hành động (VIEW_USERS_LIST, CREATE_LESSONS...) sang nhãn tiếng Việt có dấu.
 */
public final class ActionLogLabels {
    private static final Map<String, String> MODULES = Map.ofEntries(
            Map.entry("USERS", "người dùng"),
            Map.entry("USER", "người dùng"),
            Map.entry("PROFILE", "hồ sơ"),
            Map.entry("PERSONAL", "hồ sơ"),
            Map.entry("TOGGLE_STATUS", "người dùng"),
            Map.entry("TOGGLE_SHARING", "câu hỏi"),
            Map.entry("SCHOOL", "trường học"),
            Map.entry("CLASSES", "lớp phụ trách"),
            Map.entry("AUTH", "xác thực"),
            Map.entry("LOGOUT", "đăng xuất"),
            Map.entry("ROLES", "vai trò"),
            Map.entry("PERMISSIONS", "quyền"),
            Map.entry("CLASSROOMS", "lớp học"),
            Map.entry("CLASSROOM", "lớp học"),
            Map.entry("SUBJECTS", "môn học"),
            Map.entry("SUBJECT", "môn học"),
            Map.entry("CATEGORIES", "danh mục"),
            Map.entry("CATEGORY", "danh mục"),
            Map.entry("LESSONS", "bài giảng"),
            Map.entry("LESSON", "bài giảng"),
            Map.entry("DRAFTS", "bài giảng"),
            Map.entry("DRAFT", "bài giảng"),
            Map.entry("TESTS", "bài kiểm tra"),
            Map.entry("TEST", "bài kiểm tra"),
            Map.entry("EXERCISES", "bài tập"),
            Map.entry("EXERCISE", "bài tập"),
            Map.entry("EXAMS", "bài kiểm tra"),
            Map.entry("EXAM", "bài kiểm tra"),
            Map.entry("QUESTIONS", "câu hỏi"),
            Map.entry("QUESTION", "câu hỏi"),
            Map.entry("RESOURCES", "tài nguyên"),
            Map.entry("IMAGES", "hình ảnh"),
            Map.entry("IMAGE", "hình ảnh"),
            Map.entry("SAVE", "hình ảnh"),
            Map.entry("UPLOAD_IMAGE", "hình ảnh"),
            Map.entry("UPLOAD_AUDIO", "âm thanh"),
            Map.entry("TTS", "âm thanh TTS"),
            Map.entry("AUDIOS", "âm thanh"),
            Map.entry("AUDIO", "âm thanh"),
            Map.entry("NOTIFICATIONS", "thông báo"),
            Map.entry("NOTIFICATION", "thông báo"),
            Map.entry("FEEDBACK", "phản hồi"),
            Map.entry("GUIDES", "hướng dẫn"),
            Map.entry("GUIDE", "hướng dẫn"),
            Map.entry("DASHBOARD", "bảng điều khiển"),
            Map.entry("ACTION_LOGS", "nhật ký hành động"),
            Map.entry("ACCESS_LOGS", "nhật ký truy cập"),
            Map.entry("TEXTBOOKS", "sách giáo khoa"),
            Map.entry("TEXTBOOK", "sách giáo khoa"),
            Map.entry("TEMPLATES", "mẫu bài giảng"),
            Map.entry("TEMPLATE", "mẫu bài giảng"),
            Map.entry("POSTS", "bài đăng"),
            Map.entry("CLASSROOM_POSTS", "bài đăng lớp học"),
            Map.entry("ASSIGNMENTS", "bài tập"),
            Map.entry("ASSIGNMENT", "bài tập"),
            Map.entry("COMMENTS", "bình luận"),
            Map.entry("MEMBERS", "thành viên"),
            Map.entry("INVITE", "lời mời"),
            Map.entry("INVITATIONS", "lời mời"),
            Map.entry("CLASSROOM_SHARES", "chia sẻ bài giảng"),
            Map.entry("CLASSROOM_SHARE", "chia sẻ bài giảng"),
            Map.entry("SHARES", "chia sẻ bài giảng"),
            Map.entry("SHARE", "chia sẻ bài giảng"),
            Map.entry("OAUTH2", "đăng nhập Google"),
            Map.entry("CANVAS", "canvas"),
            Map.entry("TRANSLATE", "dịch thuật"),
            Map.entry("PRONUNCIATION", "phát âm"),
            Map.entry("CHECK", "phát âm"),
            Map.entry("DOCX", "tài liệu Word"),
            Map.entry("SYSTEM", "hệ thống")
    );

    private static final Map<String, String> EXACT = Map.ofEntries(
            Map.entry("LOGIN", "Đăng nhập"),
            Map.entry("LOGIN_FAILED", "Đăng nhập thất bại"),
            Map.entry("LOGOUT", "Đăng xuất"),
            Map.entry("CHANGE_PASSWORD", "Đổi mật khẩu"),
            Map.entry("PASSWORD_CHANGE", "Đổi mật khẩu"),
            Map.entry("USER_LOCK", "Khóa tài khoản"),
            Map.entry("SHARE_LESSONS_CLASS", "Chia sẻ bài giảng vào lớp học"),
            Map.entry("SHARE_LESSON_CLASS", "Chia sẻ bài giảng vào lớp học"),
            Map.entry("SHARE_LESSONS", "Chia sẻ bài giảng cho giáo viên"),
            Map.entry("SHARE_LESSON", "Chia sẻ bài giảng cho giáo viên"),
            Map.entry("CREATE_CLASSROOM_POSTS", "Tạo bài đăng trên lớp học"),
            Map.entry("UPDATE_CLASSROOM_POSTS", "Cập nhật bài đăng lớp học"),
            Map.entry("DELETE_CLASSROOM_POSTS", "Xóa bài đăng lớp học"),
            Map.entry("CREATE_POSTS", "Tạo bài đăng trên lớp học"),
            Map.entry("UPDATE_POSTS", "Cập nhật bài đăng lớp học"),
            Map.entry("DELETE_POSTS", "Xóa bài đăng lớp học"),
            Map.entry("CREATE_CLASSROOM_ANNOUNCEMENT", "Tạo thông báo trên lớp học"),
            Map.entry("UPDATE_CLASSROOM_ANNOUNCEMENT", "Cập nhật thông báo lớp học"),
            Map.entry("DELETE_CLASSROOM_ANNOUNCEMENT", "Xóa thông báo lớp học"),
            Map.entry("CREATE_CLASSROOM_ASSIGNMENT", "Tạo bài tập trên lớp học"),
            Map.entry("UPDATE_CLASSROOM_ASSIGNMENT", "Cập nhật bài tập lớp học"),
            Map.entry("DELETE_CLASSROOM_ASSIGNMENT", "Xóa bài tập lớp học"),
            Map.entry("CREATE_CLASSROOM_TEST", "Tạo bài kiểm tra trên lớp học"),
            Map.entry("UPDATE_CLASSROOM_TEST", "Cập nhật bài kiểm tra lớp học"),
            Map.entry("DELETE_CLASSROOM_TEST", "Xóa bài kiểm tra lớp học"),
            Map.entry("CREATE_COMMENTS", "Thêm bình luận"),
            Map.entry("UPDATE_COMMENTS", "Cập nhật bình luận"),
            Map.entry("DELETE_COMMENTS", "Xóa bình luận"),
            Map.entry("INVITE_CLASSROOM_MEMBER", "Mời thành viên vào lớp học"),
            Map.entry("IMPORT_CLASSROOM_MEMBERS", "Nhập danh sách thành viên từ Excel"),
            Map.entry("REMOVE_CLASSROOM_MEMBER", "Xóa thành viên khỏi lớp học"),
            Map.entry("RESEND_CLASSROOM_INVITATION", "Gửi lại lời mời vào lớp"),
            Map.entry("REVOKE_CLASSROOM_INVITATION", "Thu hồi lời mời vào lớp"),
            Map.entry("RESET_CLASSROOM_INVITE_LINK", "Đặt lại liên kết mời lớp"),
            Map.entry("RESET_CLASSROOM_CODE", "Đặt lại mã lớp học"),
            Map.entry("CREATE_CLASSROOMS", "Tạo lớp học"),
            Map.entry("UPDATE_CLASSROOMS", "Cập nhật lớp học"),
            Map.entry("DELETE_CLASSROOMS", "Xóa lớp học"),
            Map.entry("CREATE_INVITE", "Mời thành viên vào lớp học"),
            Map.entry("CREATE_INVITATIONS", "Mời thành viên vào lớp học"),
            Map.entry("SAVE_IMAGES", "Lưu hình ảnh vào thư viện"),
            Map.entry("UPLOAD_IMAGES", "Tải hình ảnh lên"),
            Map.entry("GENERATE_IMAGES", "Tạo hình ảnh AI"),
            Map.entry("DELETE_IMAGES", "Xóa hình ảnh"),
            Map.entry("CREATE_SAVE", "Lưu hình ảnh vào thư viện"),
            Map.entry("CREATE_UPLOAD_IMAGE", "Tải hình ảnh lên"),
            Map.entry("UPLOAD_UPLOAD_IMAGE", "Tải hình ảnh lên"),
            Map.entry("SAVE_TTS", "Lưu âm thanh vào thư viện"),
            Map.entry("UPLOAD_TTS", "Tải âm thanh lên"),
            Map.entry("CREATE_UPLOAD_AUDIO", "Tải âm thanh lên"),
            Map.entry("CONVERT_TTS", "Chuyển văn bản thành giọng nói"),
            Map.entry("CREATE_TTS", "Lưu âm thanh vào thư viện"),
            Map.entry("CREATE_CONVERT", "Chuyển văn bản thành giọng nói"),
            Map.entry("CHECK_PRONUNCIATION", "Kiểm tra phát âm"),
            Map.entry("CREATE_CHECK", "Kiểm tra phát âm"),
            Map.entry("CREATE_PRONUNCIATION", "Kiểm tra phát âm"),
            Map.entry("CREATE_EXERCISE", "Tạo bài tập"),
            Map.entry("UPDATE_EXERCISE", "Cập nhật bài tập"),
            Map.entry("DELETE_EXERCISE", "Xóa bài tập"),
            Map.entry("CREATE_EXAM", "Tạo bài kiểm tra"),
            Map.entry("UPDATE_EXAM", "Cập nhật bài kiểm tra"),
            Map.entry("DELETE_EXAM", "Xóa bài kiểm tra"),
            Map.entry("CREATE_TESTS", "Tạo bài kiểm tra"),
            Map.entry("UPDATE_TESTS", "Cập nhật bài kiểm tra"),
            Map.entry("DELETE_TESTS", "Xóa bài kiểm tra"),
            Map.entry("CREATE_LESSONS", "Tạo bài giảng"),
            Map.entry("UPDATE_LESSONS", "Cập nhật bài giảng"),
            Map.entry("DELETE_LESSONS", "Xóa bài giảng"),
            Map.entry("CREATE_DRAFTS", "Tạo bài giảng"),
            Map.entry("UPDATE_DRAFTS", "Cập nhật bài giảng"),
            Map.entry("DELETE_DRAFTS", "Xóa bài giảng"),
            Map.entry("CREATE_BILINGUAL_LESSON", "Tạo bài giảng song ngữ"),
            Map.entry("TRANSLATE_TRANSLATE", "Tạo bài giảng song ngữ"),
            Map.entry("CREATE_TRANSLATE", "Tạo bài giảng song ngữ"),
            Map.entry("TRANSLATE_LESSONS", "Tạo bài giảng song ngữ"),
            Map.entry("TOGGLE_USER_STATUS", "Khóa/mở khóa tài khoản"),
            Map.entry("UPDATE_TOGGLE_STATUS", "Khóa/mở khóa tài khoản"),
            Map.entry("USER_LOCK", "Khóa tài khoản"),
            Map.entry("TOGGLE_QUESTION_SHARING", "Chia sẻ/bỏ chia sẻ câu hỏi"),
            Map.entry("UPDATE_TOGGLE_SHARING", "Chia sẻ/bỏ chia sẻ câu hỏi"),
            Map.entry("UPDATE_PROFILE", "Cập nhật hồ sơ cá nhân"),
            Map.entry("UPDATE_PERSONAL", "Cập nhật hồ sơ cá nhân"),
            Map.entry("UPDATE_SCHOOL_INFO", "Cập nhật thông tin trường"),
            Map.entry("UPDATE_SCHOOL", "Cập nhật thông tin trường"),
            Map.entry("UPDATE_CLASSES_INFO", "Cập nhật lớp phụ trách"),
            Map.entry("UPDATE_CLASSES", "Cập nhật lớp phụ trách"),
            Map.entry("UPDATE_AVATAR", "Cập nhật ảnh đại diện")
    );

    private ActionLogLabels() {}

    public static String label(String action) {
        if (action == null || action.isBlank()) return "Thao tác không xác định";
        String code = action.trim().toUpperCase(Locale.ROOT);
        if (EXACT.containsKey(code)) return EXACT.get(code);

        if (code.startsWith("VIEW_") && code.endsWith("_LIST")) {
            return "Xem danh sách " + moduleOf(code.substring(5, code.length() - 5));
        }
        if (code.startsWith("VIEW_") && code.endsWith("_DETAIL")) {
            return "Xem chi tiết " + moduleOf(code.substring(5, code.length() - 7));
        }
        if (code.startsWith("CREATE_")) return "Tạo " + moduleOf(code.substring(7));
        if (code.startsWith("UPDATE_")) return "Cập nhật " + moduleOf(code.substring(7));
        if (code.startsWith("DELETE_")) return "Xóa " + moduleOf(code.substring(7));
        if (code.startsWith("EXPORT_")) return "Xuất " + moduleOf(code.substring(7));
        if (code.startsWith("UPLOAD_")) return "Tải lên " + moduleOf(code.substring(7));
        if (code.startsWith("SHARE_") && code.endsWith("_CLASS")) {
            String mod = moduleOf(code.substring(6, code.length() - 6));
            return mod.contains("bài giảng") ? "Chia sẻ bài giảng vào lớp học" : "Chia sẻ " + mod + " vào lớp học";
        }
        if (code.startsWith("SHARE_")) {
            String mod = moduleOf(code.substring(6));
            return mod.contains("bài giảng") ? "Chia sẻ bài giảng cho giáo viên" : "Chia sẻ " + mod;
        }
        if (code.startsWith("JOIN_")) return "Tham gia " + moduleOf(code.substring(5));
        if (code.startsWith("SUBMIT_")) return "Nộp " + moduleOf(code.substring(7));
        if (code.startsWith("GENERATE_")) return "Tạo (AI) " + moduleOf(code.substring(9));
        if (code.startsWith("SAVE_")) return "Lưu " + moduleOf(code.substring(5));
        if (code.startsWith("TRANSLATE_")) return "Dịch " + moduleOf(code.substring(10));
        if (code.startsWith("CONVERT_")) return "Chuyển đổi " + moduleOf(code.substring(8));
        if (code.startsWith("CHECK_")) return "Kiểm tra " + moduleOf(code.substring(6));
        if (code.startsWith("EXTRACT_")) return "Trích xuất " + moduleOf(code.substring(8));

        return code.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    /** Mô tả ngắn cho feed "Hoạt động gần đây": "đã xem danh sách người dùng". */
    public static String describe(String action, String status) {
        return describe(action, status, null);
    }

    public static String describe(String action, String status, String descriptionJson) {
        String label = label(action);
        String classroomName = classroomNameOf(descriptionJson);
        if (classroomName != null && !classroomName.isBlank()) {
            label = label + ": " + classroomName;
        }
        boolean success = status == null || "SUCCESS".equalsIgnoreCase(status);
        String verbPhrase = label.isEmpty() ? "thao tác" : Character.toLowerCase(label.charAt(0)) + label.substring(1);
        return success ? "đã " + verbPhrase : "thực hiện thất bại: " + verbPhrase;
    }

    private static String classroomNameOf(String description) {
        if (description == null || description.isBlank()) return null;
        int key = description.indexOf("\"classroomName\"");
        if (key < 0) return null;
        int colon = description.indexOf(':', key);
        int firstQuote = description.indexOf('"', colon + 1);
        int secondQuote = firstQuote < 0 ? -1 : description.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) return null;
        String name = description.substring(firstQuote + 1, secondQuote).trim();
        return name.isEmpty() ? null : name;
    }

    private static String moduleOf(String rawModule) {
        if (rawModule == null || rawModule.isBlank()) return "mục";
        String key = rawModule.toUpperCase(Locale.ROOT).replace('-', '_');
        return MODULES.getOrDefault(key, key.toLowerCase(Locale.ROOT).replace('_', ' '));
    }
}
