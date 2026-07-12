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
public class GuideDataInitializer implements CommandLineRunner {
    private final GuideRepository guideRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (guideRepository.count() > 0) return;

        guideRepository.saveAll(List.of(
                guide("bat-dau", "Bắt đầu sử dụng", "Tạo tài khoản và thiết lập thông tin ban đầu.", 0,
                        step("Đăng ký tài khoản", "Từ trang chủ, chọn “Đăng ký”. Chọn đúng vai trò Giáo viên hoặc Học sinh, sau đó nhập đầy đủ thông tin bắt buộc và kiểm tra lại trước khi tiếp tục."),
                        step("Xác nhận và đăng nhập", "Hoàn tất các bước xác thực theo hướng dẫn của hệ thống. Quay lại trang đăng nhập, nhập tên tài khoản và mật khẩu vừa tạo, sau đó chọn “Đăng nhập”."),
                        step("Cập nhật hồ sơ", "Chọn ảnh đại diện ở góc trên bên phải, mở “Hồ sơ cá nhân” và bổ sung thông tin cá nhân, trường học, lớp học. Kiểm tra kỹ trước khi lưu.")),

                guide("tao-bai-giang", "Tạo bài giảng", "Soạn, chỉnh sửa và chia sẻ bài giảng cho lớp học.", 1,
                        step("Mở danh sách bài giảng", "Đăng nhập bằng tài khoản giáo viên. Trên thanh điều hướng, chọn “Bài giảng” để xem các bài đã tạo, bản nháp và bài được chia sẻ."),
                        step("Tạo bài giảng mới", "Chọn “Tạo bài giảng”, nhập tên bài, môn học, khối lớp và mô tả ngắn. Chọn cách soạn phù hợp như DOCX, PPTX hoặc trình soạn Collabora."),
                        step("Thêm nội dung và học liệu", "Nhập nội dung theo từng phần rõ ràng. Có thể chèn hình ảnh, bảng, hình học, sơ đồ và học liệu minh họa. Nội dung cần phù hợp độ tuổi học sinh tiểu học."),
                        step("Lưu và kiểm tra", "Lưu bản nháp thường xuyên. Xem lại chính tả, kiến thức, hình minh họa và bố cục trước khi hoàn tất bài giảng."),
                        step("Chia sẻ vào lớp học", "Tại danh sách bài giảng, chọn chức năng chia sẻ, chọn đúng lớp học và xác nhận. Học sinh sẽ nhận được nội dung theo lớp đã chọn.")),

                guide("tao-bai-kiem-tra", "Tạo bài kiểm tra", "Tạo câu hỏi, cấu hình đề và giao bài cho học sinh.", 2,
                        step("Mở chức năng Bài kiểm tra", "Đăng nhập bằng tài khoản giáo viên, chọn “Bài kiểm tra” trên thanh điều hướng, sau đó chọn “Tạo bài kiểm tra”."),
                        step("Nhập thông tin chung", "Nhập tên bài kiểm tra, môn học, khối lớp, thời gian làm bài và phần hướng dẫn. Đặt thời gian phù hợp với số lượng và độ khó của câu hỏi."),
                        step("Thêm câu hỏi", "Tạo câu hỏi mới hoặc chọn câu hỏi từ ngân hàng. Nhập đầy đủ nội dung, đáp án, đáp án đúng, hình minh họa nếu cần và điểm số của từng câu."),
                        step("Kiểm tra cấu hình đề", "Xem trước toàn bộ đề; kiểm tra thứ tự câu hỏi, đáp án đúng, tổng điểm, thời gian làm bài và khả năng hiển thị trên màn hình học sinh."),
                        step("Lưu và giao bài", "Lưu bài kiểm tra, chọn lớp học cần giao, thiết lập thời hạn bắt đầu và kết thúc. Sau khi học sinh nộp bài, mở phần kết quả để theo dõi và chấm bài.")),

                guide("quan-ly-lop-hoc", "Quản lý lớp học", "Tạo lớp, mời học sinh và tổ chức nội dung học tập.", 3,
                        step("Tạo hoặc tham gia lớp", "Giáo viên mở “Lớp học” và chọn “Tạo lớp”, sau đó nhập tên lớp, khối và năm học. Học sinh tham gia bằng liên kết hoặc lời mời do giáo viên cung cấp."),
                        step("Quản lý thành viên", "Mở chi tiết lớp và chọn tab thành viên để xem danh sách giáo viên, học sinh; gửi lời mời hoặc quản lý học sinh trong phạm vi quyền được cấp."),
                        step("Đăng thông báo", "Dùng Bảng tin để đăng thông báo, nội dung nhắc nhở hoặc tài liệu chung. Viết tiêu đề rõ ràng và kiểm tra đúng lớp trước khi đăng."),
                        step("Giao bài học và bài kiểm tra", "Mở khu vực bài học của lớp, chọn bài giảng hoặc bài kiểm tra đã chuẩn bị, thiết lập thời hạn nếu có rồi xác nhận giao cho học sinh.")),

                guide("cong-cu-ai-va-hoc-lieu", "Công cụ AI và học liệu", "Dịch, đọc văn bản, luyện phát âm và tạo hình minh họa.", 4,
                        step("Chọn công cụ phù hợp", "Mở “Công cụ AI” hoặc chọn trực tiếp Dịch thuật, Chuyển văn bản thành giọng nói, Luyện phát âm, Tạo ảnh hay Biên tập hình ảnh theo nhu cầu."),
                        step("Nhập yêu cầu rõ ràng", "Chọn đúng ngôn ngữ, môn học hoặc loại nội dung. Viết yêu cầu ngắn gọn nhưng đủ thông tin, nêu rõ khối lớp và mục đích sử dụng."),
                        step("Kiểm tra kết quả AI", "Luôn đọc, nghe hoặc xem lại kết quả do AI tạo. Giáo viên cần kiểm tra kiến thức, từ ngữ, độ chính xác và mức độ phù hợp với học sinh trước khi sử dụng."),
                        step("Lưu và đưa vào bài học", "Tải kết quả xuống hoặc chèn vào bài giảng, bài kiểm tra và học liệu. Đặt tên dễ nhận biết để thuận tiện tìm kiếm và tái sử dụng."),
                        "Nội dung do AI tạo chỉ là tài liệu hỗ trợ. Giáo viên cần kiểm tra trước khi sử dụng trong giảng dạy."),

                guide("bao-loi-hoac-gop-y", "Báo lỗi hoặc góp ý", "Gửi vấn đề ngay tại trang đang sử dụng và nhận trả lời qua thông báo.", 5,
                        step("Mở cửa sổ Trợ giúp", "Bấm nút “Trợ giúp” ở góc dưới bên phải của trang đang sử dụng, sau đó chọn gửi phản hồi hoặc xem hướng dẫn."),
                        step("Chọn loại phản hồi", "Chọn “Báo lỗi” khi một chức năng hoạt động sai hoặc không sử dụng được. Chọn “Góp ý” khi muốn đề xuất thay đổi hoặc bổ sung chức năng."),
                        step("Mô tả vấn đề", "Nhập tiêu đề ngắn gọn. Trong phần mô tả, ghi rõ các bước đã thực hiện, kết quả mong muốn và kết quả thực tế. Không nhập mật khẩu hoặc dữ liệu nhạy cảm."),
                        step("Gửi và theo dõi", "Chọn “Gửi phản hồi”. Quản trị viên sẽ tiếp nhận và xử lý. Khi có câu trả lời, thông báo sẽ xuất hiện trong mục Thông báo của đúng tài khoản đã gửi."))
        ));
    }

    private Guide guide(String slug, String title, String description, int sortOrder, GuideStep... steps) {
        return guide(slug, title, description, sortOrder, List.of(steps), null);
    }

    private Guide guide(String slug, String title, String description, int sortOrder, GuideStep step1, GuideStep step2, GuideStep step3, GuideStep step4, String note) {
        return guide(slug, title, description, sortOrder, List.of(step1, step2, step3, step4), note);
    }

    private Guide guide(String slug, String title, String description, int sortOrder, List<GuideStep> steps, String note) {
        Guide guide = new Guide();
        guide.setSlug(slug);
        guide.setTitle(title);
        guide.setDescription(description);
        guide.setNote(note);
        guide.setPublished(true);
        guide.setSortOrder(sortOrder);
        List<GuideStep> ordered = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            GuideStep step = steps.get(i);
            step.setSortOrder(i);
            ordered.add(step);
        }
        guide.replaceSteps(ordered);
        return guide;
    }

    private GuideStep step(String title, String content) {
        GuideStep step = new GuideStep();
        step.setTitle(title);
        step.setContent(content);
        step.setImageUrl(null);
        step.setImageAlt(null);
        step.setVideoUrl(null);
        return step;
    }
}
