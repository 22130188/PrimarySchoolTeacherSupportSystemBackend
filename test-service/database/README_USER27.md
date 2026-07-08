# 📚 DỮ LIỆU MẪU CHO USER: van41527@gmail.com

## 🎯 Tổng Quan

File **`insert_sample_data_user27.sql`** tạo dữ liệu mẫu đầy đủ cho test-service với user **van41527** (ID: **27**).

### ✨ Đặc Điểm

- ✅ **NHIỀU câu hỏi** (130 câu) - phù hợp yêu cầu
- ✅ **ÍT bài kiểm tra** (4 bài) - theo yêu cầu
- ✅ **Đa dạng loại câu hỏi**: Trắc nghiệm, Điền chỗ trống, Ghép cặp, Tự luận
- ✅ **Phân theo lớp**: Lớp 1, 2, 3
- ✅ **Nội dung thực tế**: Theo chương trình Toán tiểu học Việt Nam

## 📊 Chi Tiết

| Loại | Số lượng | Mô tả |
|------|----------|-------|
| **Câu hỏi trắc nghiệm** | 85 | Phép tính, so sánh, hình học |
| **Điền vào chỗ trống** | 20 | Hoàn thành phép tính, đơn vị đo |
| **Ghép cặp** | 15 | Ghép phép tính, hình học, đo lường |
| **Tự luận** | 10 | Bài toán có lời văn, giải thích |
| **Bài kiểm tra (EXAM)** | 2 | Lớp 1, 2 |
| **Bài tập (EXERCISE)** | 2 | Lớp 2, 3 |

### 📝 Các Bài Kiểm Tra/Bài Tập

1. **Kiểm tra giữa kỳ I - Toán lớp 1**
   - 15 câu, 150 điểm, 30 phút
   - Phép cộng trừ 0-10, hình học

2. **Kiểm tra cuối kỳ I - Toán lớp 2**
   - 20 câu, 200 điểm, 40 phút
   - Bảng nhân 2-5, cộng trừ có nhớ

3. **Bài tập tuần 10 - Toán lớp 3**
   - 18 câu, 180 điểm, 35 phút
   - Nhân chia số có 3 chữ số, bảng nhân 6-9

4. **Bài tập ôn tập - Các dạng câu hỏi**
   - 15 câu, 250 điểm, 30 phút
   - Điền chỗ trống + Ghép cặp

## 🚀 Cách Sử Dụng

### Cách 1: Dùng Script (Khuyến nghị ⭐)

```bash
cd d:\Test\PrimarySchoolTeacherSupportSystemBackend\test-service\database
run_insert_user27.bat
```

### Cách 2: MySQL Command Line

```bash
mysql -u root -p test_db < insert_sample_data_user27.sql
```

### Cách 3: MySQL Workbench

1. Mở file `insert_sample_data_user27.sql`
2. Chọn schema `test_db`
3. Click **Execute** (⚡)

## ✅ Kiểm Tra Sau Khi Import

```sql
-- Kiểm tra số câu hỏi
SELECT COUNT(*) as 'Tổng câu hỏi' 
FROM test_questions 
WHERE created_by = 27;

-- Xem các bài kiểm tra
SELECT name, subject, grade, test_type, question_count, total_points
FROM tests 
WHERE created_by = 27;

-- Xem câu hỏi theo loại
SELECT type, COUNT(*) as so_luong
FROM test_questions
WHERE created_by = 27
GROUP BY type;
```

## 📝 Lưu Ý Quan Trọng

### ⚠️ Chưa Có:
- ❌ **Ảnh** (image_url) - Bạn sẽ tự thêm sau khi upload lên Cloudinary
- ❌ **Audio** (audio_url) - Bạn sẽ tự thêm sau
- ❌ **classroom_posts** - Cần classroom_id thực từ classroom_db

### ✅ Đã Có:
- ✅ User ID đúng: **27** (van41527@gmail.com)
- ✅ Câu hỏi is_shared = TRUE (chia sẻ với giáo viên khác)
- ✅ Bài kiểm tra status = PUBLISHED
- ✅ Thời gian hợp lý: 2026-07-05 đến 2026-07-19

## 🎨 Thêm Ảnh/Audio

Sau khi upload lên Cloudinary, cập nhật:

```sql
-- Thêm ảnh cho câu hỏi
UPDATE test_questions 
SET image_url = 'https://res.cloudinary.com/your-account/image/question1.png'
WHERE id = 1;

-- Thêm audio cho câu hỏi
UPDATE test_questions 
SET audio_url = 'https://res.cloudinary.com/your-account/audio/question1.mp3'
WHERE id = 5;
```

## 🗑️ Xóa Dữ Liệu (nếu cần làm lại)

```sql
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM test_questions WHERE created_by = 27;
DELETE FROM tests WHERE created_by = 27;
SET FOREIGN_KEY_CHECKS = 1;

-- Sau đó chạy lại file SQL
```

## 📞 Thống Kê Nhanh

```sql
-- Tổng quan
SELECT 
    'Câu hỏi' as Loai,
    COUNT(*) as So_luong,
    SUM(points) as Tong_diem
FROM test_questions 
WHERE created_by = 27

UNION ALL

SELECT 
    'Bài kiểm tra/Bài tập',
    COUNT(*),
    SUM(total_points)
FROM tests 
WHERE created_by = 27;
```

## 🎓 Ví Dụ Câu Hỏi

### Trắc nghiệm:
- 2 + 3 = ? → Đáp án: 5
- Hình nào có 4 góc vuông? → Hình vuông

### Điền chỗ trống:
- 5 + _____ = 10 → Đáp án: 5
- 1 mét = _____ centimet → Đáp án: 100

### Ghép cặp:
- Nối phép tính với kết quả
- Ghép hình với tên gọi

### Tự luận:
- Giải bài toán có lời văn
- Tính chu vi hình chữ nhật

---

**🎉 Chúc bạn sử dụng thành công!**

_Tạo bởi: Kiro AI Assistant_  
_Ngày: 2026-07-04_  
_User: van41527@gmail.com (ID: 27)_
