# HƯỚNG DẪN SỬ DỤNG DỮ LIỆU MẪU - TEST SERVICE

## 📋 Tổng Quan

File `sample_data.sql` chứa dữ liệu mẫu đầy đủ cho test-service bao gồm:

- **60 câu hỏi** đa dạng với nhiều loại khác nhau
- **5 bài kiểm tra** (EXAM) cho các lớp 1, 2, 3
- **5 bài tập** (EXERCISE) cho học sinh làm ở nhà
- **4 bài nộp mẫu** từ học sinh
- **10 bài đăng lớp học** liên kết với classroom-service

## 📊 Chi Tiết Dữ Liệu

### 1. Câu Hỏi (60 câu)

#### Phân loại theo loại câu hỏi:
- **Trắc nghiệm (MULTIPLE_CHOICE)**: ~25 câu
  - Toán: Phép tính cơ bản, so sánh số, hình học
  - Ngữ văn: Từ loại, loại câu, từ vựng
  - Tiếng Anh: Colors, numbers, vocabulary
  - Khoa học: Môi trường sống, thời tiết
  - Đạo đức & Mỹ thuật

- **Điền vào chỗ trống (FILL_IN_BLANK)**: ~10 câu
  - Hoàn thành phép tính
  - Điền từ thích hợp
  - Bài toán có lời văn

- **Ghép cặp (MATCHING)**: ~10 câu
  - Ghép phép tính với đáp án
  - Ghép hình với tên gọi
  - Ghép đơn vị đo

- **Tự luận (ESSAY)**: ~15 câu
  - Giải bài toán có lời văn
  - Viết đoạn văn ngắn
  - Giải thích khái niệm

#### Phân loại theo môn học:
- Toán: ~25 câu (lớp 1, 2, 3)
- Ngữ văn: ~8 câu
- Tiếng Anh: ~7 câu
- Khoa học: ~7 câu
- Đạo đức & Mỹ thuật: ~6 câu
- Âm nhạc: ~3 câu

### 2. Bài Kiểm Tra (5 bài - EXAM)

| Tên Bài | Môn | Lớp | Thời gian | Số câu | Điểm |
|---------|-----|-----|-----------|---------|------|
| Kiểm tra giữa kỳ I - Toán | Toán | 1 | 30 phút | 10 | 125 |
| Kiểm tra cuối kỳ I - Toán | Toán | 2 | 40 phút | 11 | 160 |
| Kiểm tra 15 phút - Chương 2 | Toán | 3 | 15 phút | 6 | 95 |
| Kiểm tra giữa kỳ - Ngữ văn | Ngữ văn | 2 | 35 phút | 7 | 115 |
| Kiểm tra cuối học kỳ - Khoa học | Khoa học | 1 | 30 phút | 6 | 80 |

### 3. Bài Tập (5 bài - EXERCISE)

| Tên Bài | Môn | Lớp | Thời gian | Số câu | Điểm |
|---------|-----|-----|-----------|---------|------|
| Bài tập về nhà - Phép cộng | Toán | 1 | 20 phút | 4 | 55 |
| Bài tập tuần 5 - Bảng nhân | Toán | 2 | 15 phút | 3 | 45 |
| Bài tập ôn tập - Chương 1 & 2 | Toán | 3 | 25 phút | 2 | 40 |
| Unit 1 - Homework | Tiếng Anh | 2 | 20 phút | 6 | 75 |
| Bài tập - Đạo đức và Mỹ thuật | Đạo đức | 1 | 15 phút | 5 | 70 |

## 🚀 Cách Sử Dụng

### Bước 1: Chuẩn bị

Đảm bảo MySQL/MariaDB đã được cài đặt và database `test_db` đã được tạo:

```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS test_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE test_db;
```

### Bước 2: Chạy Schema (nếu chưa có)

```bash
mysql -u root -p test_db < schema.sql
```

### Bước 3: Import Dữ Liệu Mẫu

**Cách 1: Từ MySQL prompt**
```sql
USE test_db;
SOURCE d:/Test/PrimarySchoolTeacherSupportSystemBackend/test-service/database/sample_data.sql;
```

**Cách 2: Từ command line**
```bash
cd d:\Test\PrimarySchoolTeacherSupportSystemBackend\test-service\database
mysql -u root -p test_db < sample_data.sql
```

**Cách 3: Sử dụng script có sẵn (Windows)**
```bash
cd d:\Test\PrimarySchoolTeacherSupportSystemBackend\test-service\database
setup_database.bat
```

### Bước 4: Kiểm Tra Dữ Liệu

```sql
USE test_db;

-- Xem tất cả bài kiểm tra
SELECT id, name, subject, grade, test_type, status, question_count, total_points 
FROM tests;

-- Xem câu hỏi theo bài kiểm tra
SELECT t.name as test_name, tq.type, tq.content, tq.points 
FROM test_questions tq
JOIN tests t ON tq.test_id = t.id
ORDER BY t.id, tq.order_index;

-- Xem bài nộp của học sinh
SELECT ta.user_name, t.name as test_name, ta.score, ta.max_score, ta.submitted_at
FROM test_attempts ta
JOIN tests t ON ta.test_id = t.id;

-- Xem bài đăng lớp học
SELECT classroom_code, post_type, title, reference_test_name, start_at
FROM classroom_posts
ORDER BY created_at DESC;
```

## ⚙️ Tùy Chỉnh Dữ Liệu

### 1. Cập nhật User IDs

Dữ liệu mẫu sử dụng các user ID giả định. Bạn cần lấy user ID thực từ database `user_db`:

```sql
-- Xem danh sách giáo viên
SELECT id, full_name, email, role FROM user_db.users WHERE role = 'TEACHER';

-- Cập nhật created_by trong test_questions
UPDATE test_db.test_questions 
SET created_by = <user_id_thuc>, created_by_name = '<ten_giao_vien>'
WHERE created_by = 1;  -- ID cũ

-- Cập nhật trong tests
UPDATE test_db.tests 
SET created_by = <user_id_thuc>, created_by_name = '<ten_giao_vien>'
WHERE created_by = 1;
```

### 2. Cập nhật Classroom IDs

```sql
-- Xem danh sách lớp học
SELECT id, name, class_code, teacher_id FROM classroom_db.classrooms;

-- Cập nhật classroom_posts
UPDATE test_db.classroom_posts 
SET classroom_id = <classroom_id_thuc>, classroom_code = '<ma_lop_thuc>'
WHERE classroom_id = 1;
```

### 3. Thêm Ảnh và Audio

Dữ liệu mẫu không bao gồm URL ảnh/audio. Bạn có thể thêm sau:

```sql
-- Thêm ảnh cho câu hỏi (image_url hiện chưa có trong schema, cần thêm column)
ALTER TABLE test_questions ADD COLUMN image_url VARCHAR(500) COMMENT 'URL of image';

UPDATE test_questions 
SET image_url = 'https://res.cloudinary.com/your-cloud/image/cat.png'
WHERE id = 43;  -- Câu hỏi "What is this? 🍎"

-- Cập nhật audio_url cho câu hỏi phát âm
UPDATE test_questions 
SET audio_url = 'https://res.cloudinary.com/your-cloud/audio/pronunciation.mp3'
WHERE type = 'AUDIO' OR content LIKE '%nghe%';
```

### 4. Điều Chỉnh Thời Gian

```sql
-- Cập nhật thời gian bắt đầu/kết thúc cho phù hợp
UPDATE tests 
SET start_at = '2026-07-10 08:00:00',
    end_at = '2026-07-17 23:59:59'
WHERE id = 1;

-- Cập nhật thời gian cho tất cả bài kiểm tra (dời sang tuần sau)
UPDATE tests 
SET start_at = DATE_ADD(start_at, INTERVAL 7 DAY),
    end_at = DATE_ADD(end_at, INTERVAL 7 DAY);
```

## 📝 Thêm Dữ Liệu Mới

### Thêm Câu Hỏi Mới

```sql
-- Câu hỏi trắc nghiệm
INSERT INTO test_questions (
    test_id, type, content, points, title, answers_json, 
    order_index, created_by, created_by_name, is_shared, created_at
) VALUES (
    NULL,  -- NULL nếu là câu hỏi độc lập
    'MULTIPLE_CHOICE',
    '3 x 4 = ?',
    10,
    'Bảng nhân 3',
    '{"answers": ["10", "11", "12", "13"], "correctAnswer": 2}',
    1,
    1,
    'Cô Lan',
    TRUE,
    NOW()
);

-- Câu hỏi điền vào chỗ trống
INSERT INTO test_questions (
    test_id, type, content, points, text_with_blanks, blanks_json,
    order_index, created_by, created_by_name, is_shared, created_at
) VALUES (
    NULL,
    'FILL_IN_BLANK',
    'Hoàn thành phép tính',
    15,
    '_____ + 7 = 15',
    '[{"index": 0, "correctAnswer": "8"}]',
    1,
    1,
    'Cô Lan',
    TRUE,
    NOW()
);
```

### Tạo Bài Kiểm Tra Mới

```sql
-- Bước 1: Tạo bài kiểm tra
INSERT INTO tests (
    name, subject, grade, duration, description,
    created_by, created_by_name, test_type, status,
    start_at, end_at
) VALUES (
    'Kiểm tra học kỳ II - Toán',
    'Toán',
    '2',
    45,
    'Kiểm tra tổng hợp cuối năm',
    1,
    'Cô Lan',
    'EXAM',
    'PUBLISHED',
    '2026-07-20 08:00:00',
    '2026-07-27 23:59:59'
);

-- Bước 2: Lấy ID vừa tạo và gán câu hỏi vào bài kiểm tra
SET @new_test_id = LAST_INSERT_ID();

UPDATE test_questions 
SET test_id = @new_test_id, order_index = 1
WHERE id IN (10, 11, 12, 13, 14);

-- Bước 3: Cập nhật thống kê
UPDATE tests 
SET question_count = 5,
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @new_test_id)
WHERE id = @new_test_id;
```

## 🔍 Truy Vấn Hữu Ích

### Thống kê câu hỏi

```sql
-- Số câu hỏi theo loại
SELECT type, COUNT(*) as so_luong, SUM(points) as tong_diem
FROM test_questions
GROUP BY type;

-- Số câu hỏi theo giáo viên
SELECT created_by_name, COUNT(*) as so_cau_hoi, 
       SUM(CASE WHEN is_shared = TRUE THEN 1 ELSE 0 END) as cau_chia_se
FROM test_questions
GROUP BY created_by_name;
```

### Thống kê bài kiểm tra

```sql
-- Bài kiểm tra theo trạng thái
SELECT status, test_type, COUNT(*) as so_luong
FROM tests
GROUP BY status, test_type;

-- Bài kiểm tra sắp diễn ra
SELECT name, subject, grade, start_at, end_at
FROM tests
WHERE start_at > NOW() AND status = 'PUBLISHED'
ORDER BY start_at;
```

### Thống kê bài nộp

```sql
-- Điểm trung bình theo bài kiểm tra
SELECT t.name, AVG(ta.score) as diem_trung_binh,
       COUNT(*) as so_bai_nop
FROM test_attempts ta
JOIN tests t ON ta.test_id = t.id
GROUP BY t.id, t.name;

-- Học sinh có điểm cao nhất
SELECT user_name, t.name as test_name, score, max_score,
       ROUND((score / max_score * 100), 2) as phan_tram
FROM test_attempts ta
JOIN tests t ON ta.test_id = t.id
ORDER BY phan_tram DESC
LIMIT 10;
```

## ⚠️ Lưu Ý Quan Trọng

1. **Foreign Keys**: Dữ liệu giả định user_id và classroom_id. Cần cập nhật với ID thực từ các service khác.

2. **Ảnh và Audio**: Chưa có URL thực. Cần upload lên Cloudinary và cập nhật sau.

3. **Thời gian**: Các thời gian start_at/end_at cần điều chỉnh cho phù hợp với thời gian thực tế.

4. **Encoding**: File sử dụng UTF-8. Đảm bảo MySQL đã set:
   ```sql
   SET NAMES utf8mb4;
   ```

5. **Backup**: Nên backup database trước khi import:
   ```bash
   mysqldump -u root -p test_db > backup_test_db.sql
   ```

## 🆘 Xử Lý Lỗi

### Lỗi Foreign Key

Nếu gặp lỗi foreign key khi insert:

```sql
SET FOREIGN_KEY_CHECKS = 0;
-- Chạy các câu lệnh INSERT
SET FOREIGN_KEY_CHECKS = 1;
```

### Xóa Dữ Liệu và Làm Lại

```sql
-- Xóa tất cả dữ liệu mẫu
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE test_attempts;
DELETE FROM test_questions;
DELETE FROM classroom_posts;
DELETE FROM tests;
SET FOREIGN_KEY_CHECKS = 1;

-- Import lại
SOURCE sample_data.sql;
```

### Reset Auto Increment

```sql
ALTER TABLE tests AUTO_INCREMENT = 1;
ALTER TABLE test_questions AUTO_INCREMENT = 1;
ALTER TABLE test_attempts AUTO_INCREMENT = 1;
ALTER TABLE classroom_posts AUTO_INCREMENT = 1;
```

## 📞 Liên Hệ

Nếu có vấn đề hoặc câu hỏi, vui lòng liên hệ team phát triển.

---

**Chúc bạn sử dụng thành công! 🎉**
