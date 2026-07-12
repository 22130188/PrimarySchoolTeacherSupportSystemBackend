package vn.edu.primary.teacher_support.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.entity.Guide;
import vn.edu.primary.teacher_support.entity.GuideStep;
import vn.edu.primary.teacher_support.repository.GuideRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StudentGuideDataInitializer implements CommandLineRunner {
    private final GuideRepository guideRepository;

    @Override
    @Transactional
    public void run(String... args) {
        saveIfMissing(studentGettingStarted());
        saveIfMissing(studentClassroom());
        saveIfMissing(studentLearning());
        saveIfMissing(studentTools());
        saveIfMissing(studentSupport());
    }

    private void saveIfMissing(Guide guide) {
        if (guideRepository.findBySlug(guide.getSlug()).isEmpty()) guideRepository.save(guide);
    }

    private Guide studentGettingStarted() {
        return guide("hoc-sinh-bat-dau", "Học sinh — Bắt đầu sử dụng", "Đăng ký, đăng nhập và thiết lập hồ sơ học sinh.", 6,
                step("Đăng ký tài khoản", "Chọn Đăng ký, chọn vai trò Học sinh và nhập đầy đủ thông tin trường, khối và lớp."),
                step("Đăng nhập", "Đăng nhập để xem bài giảng, bài tập và bài kiểm tra mới nhất từ các lớp."),
                step("Cập nhật hồ sơ", "Mở Hồ sơ cá nhân để cập nhật ảnh đại diện và thông tin học sinh."));
    }
    private Guide studentClassroom() {
        return guide("hoc-sinh-lop-hoc", "Học sinh — Lớp học và trao đổi", "Tham gia lớp, xem bảng tin và trao đổi trong lớp.", 7,
                step("Tham gia lớp", "Nhập mã lớp hoặc mở liên kết mời và xác nhận tham gia."),
                step("Xem nội dung lớp", "Xem Bảng tin, Bài tập, Bài kiểm tra, Bài giảng và Thành viên."),
                step("Trao đổi", "Đọc thông báo, đăng nội dung khi được phép và bình luận đúng chủ đề."));
    }
    private Guide studentLearning() {
        return guide("hoc-sinh-hoc-va-lam-bai", "Học sinh — Học bài và làm bài", "Xem bài giảng, làm bài và theo dõi kết quả.", 8,
                step("Mở bài được giao", "Chọn Bài giảng, Bài tập hoặc Bài kiểm tra trong lớp."),
                step("Làm và nộp bài", "Đọc hướng dẫn, thời gian, số lượt làm và kiểm tra đáp án trước khi nộp."),
                step("Xem kết quả", "Xem điểm, lịch sử làm bài và làm lại khi vẫn còn lượt."));
    }
    private Guide studentTools() {
        return guide("hoc-sinh-cong-cu-hoc-tap", "Học sinh — Công cụ học tập và học liệu", "Sử dụng giọng đọc, dịch thuật, phát âm, hình ảnh và sách giáo khoa.", 9,
                step("Luyện nghe và dịch", "Dùng giọng đọc và dịch Việt–Anh hoặc Anh–Việt theo ngữ cảnh."),
                step("Luyện phát âm", "Nghe mẫu, ghi âm và xem kết quả để luyện lại."),
                step("Sử dụng học liệu", "Duyệt sách theo môn, khối và dùng công cụ hình ảnh khi cần."));
    }
    private Guide studentSupport() {
        return guide("hoc-sinh-ho-tro", "Học sinh — Trợ giúp và báo lỗi", "Xem hướng dẫn, gửi phản hồi và nhận trả lời.", 10,
                step("Mở Trợ giúp", "Chọn Trợ giúp để mở trung tâm hướng dẫn."),
                step("Gửi phản hồi", "Ghi rõ thao tác và vấn đề; không gửi mật khẩu."),
                step("Theo dõi trả lời", "Xem câu trả lời của quản trị viên trong Thông báo."));
    }

    private Guide guide(String slug, String title, String description, int sortOrder, GuideStep... steps) {
        Guide guide = new Guide();
        guide.setSlug(slug); guide.setTitle(title); guide.setDescription(description);
        guide.setPublished(true); guide.setSortOrder(sortOrder);
        List<GuideStep> ordered = new ArrayList<>();
        for (int i = 0; i < steps.length; i++) { steps[i].setSortOrder(i); ordered.add(steps[i]); }
        guide.replaceSteps(ordered);
        return guide;
    }

    private GuideStep step(String title, String content) {
        GuideStep step = new GuideStep(); step.setTitle(title); step.setContent(content); return step;
    }
}
