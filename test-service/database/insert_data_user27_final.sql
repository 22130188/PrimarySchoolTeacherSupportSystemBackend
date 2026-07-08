-- =====================================================
-- DỮ LIỆU MẪU CHO USER: van41527@gmail.com (ID: 27)
-- Nhiều câu hỏi (150+), Ít bài kiểm tra (4 bài)
-- =====================================================

USE test_db;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa dữ liệu cũ
DELETE FROM test_attempts WHERE test_id IN (SELECT id FROM tests WHERE created_by = 27);
DELETE FROM test_questions WHERE created_by = 27;
DELETE FROM classroom_posts WHERE author_id = 27;
DELETE FROM tests WHERE created_by = 27;

-- ============================================
-- PHẦN 1: 50 CÂU TRẮC NGHIỆM (MULTIPLE_CHOICE)
-- ============================================

-- Toán lớp 1 (20 câu)
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MULTIPLE_CHOICE', '1 + 1 = ?', 10, 'Phép cộng', '{"answers": ["1", "2", "3", "4"], "correctAnswer": 1}', 1, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 + 3 = ?', 10, 'Phép cộng', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 2, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '4 + 4 = ?', 10, 'Phép cộng', '{"answers": ["6", "7", "8", "9"], "correctAnswer": 2}', 3, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 + 5 = ?', 10, 'Phép cộng', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 4, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '6 + 4 = ?', 10, 'Phép cộng', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 5, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '7 + 3 = ?', 10, 'Phép cộng', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 6, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 + 2 = ?', 10, 'Phép cộng', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 7, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 - 2 = ?', 10, 'Phép trừ', '{"answers": ["1", "2", "3", "4"], "correctAnswer": 2}', 8, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '7 - 3 = ?', 10, 'Phép trừ', '{"answers": ["3", "4", "5", "6"], "correctAnswer": 1}', 9, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '10 - 5 = ?', 10, 'Phép trừ', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 10, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số nào lớn hơn: 7 hay 5?', 10, 'So sánh', '{"answers": ["5", "7", "Bằng nhau", "Không so sánh được"], "correctAnswer": 1}', 11, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số liền sau của 8 là?', 10, 'Dãy số', '{"answers": ["7", "8", "9", "10"], "correctAnswer": 2}', 12, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số liền trước của 5 là?', 10, 'Dãy số', '{"answers": ["3", "4", "6", "7"], "correctAnswer": 1}', 13, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Hình nào có 3 cạnh?', 10, 'Hình học', '{"answers": ["Hình vuông", "Hình tam giác", "Hình tròn", "Hình chữ nhật"], "correctAnswer": 1}', 14, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Hình nào có 4 góc vuông?', 10, 'Hình học', '{"answers": ["Hình tam giác", "Hình tròn", "Hình vuông", "Hình oval"], "correctAnswer": 2}', 15, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Hình nào không có góc?', 10, 'Hình học', '{"answers": ["Hình vuông", "Hình tam giác", "Hình chữ nhật", "Hình tròn"], "correctAnswer": 3}', 16, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '9 + 1 = ?', 10, 'Phép cộng', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 17, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 - 4 = ?', 10, 'Phép trừ', '{"answers": ["3", "4", "5", "6"], "correctAnswer": 1}', 18, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '6 - 2 = ?', 10, 'Phép trừ', '{"answers": ["3", "4", "5", "6"], "correctAnswer": 1}', 19, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số nào nhỏ nhất: 3, 7, 5, 9?', 10, 'So sánh', '{"answers": ["3", "7", "5", "9"], "correctAnswer": 0}', 20, 27, 'van41527', TRUE);

-- Toán lớp 2 (20 câu)
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MULTIPLE_CHOICE', '12 + 15 = ?', 10, 'Phép cộng', '{"answers": ["25", "26", "27", "28"], "correctAnswer": 2}', 21, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '23 + 14 = ?', 10, 'Phép cộng', '{"answers": ["35", "36", "37", "38"], "correctAnswer": 2}', 22, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '45 + 32 = ?', 10, 'Phép cộng', '{"answers": ["75", "76", "77", "78"], "correctAnswer": 2}', 23, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '57 - 24 = ?', 10, 'Phép trừ', '{"answers": ["31", "32", "33", "34"], "correctAnswer": 2}', 24, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '68 - 42 = ?', 10, 'Phép trừ', '{"answers": ["24", "25", "26", "27"], "correctAnswer": 2}', 25, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 × 3 = ?', 10, 'Bảng nhân 2', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 2}', 26, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 × 4 = ?', 10, 'Bảng nhân 2', '{"answers": ["6", "7", "8", "9"], "correctAnswer": 2}', 27, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 × 5 = ?', 10, 'Bảng nhân 2', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 28, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '3 × 3 = ?', 10, 'Bảng nhân 3', '{"answers": ["6", "7", "8", "9"], "correctAnswer": 3}', 29, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '3 × 4 = ?', 10, 'Bảng nhân 3', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 3}', 30, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 × 2 = ?', 10, 'Bảng nhân 5', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 31, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 × 3 = ?', 10, 'Bảng nhân 5', '{"answers": ["10", "12", "15", "20"], "correctAnswer": 2}', 32, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '10 ÷ 2 = ?', 10, 'Bảng chia 2', '{"answers": ["3", "4", "5", "6"], "correctAnswer": 2}', 33, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 ÷ 2 = ?', 10, 'Bảng chia 2', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 2}', 34, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '12 ÷ 3 = ?', 10, 'Bảng chia 3', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 2}', 35, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '15 ÷ 5 = ?', 10, 'Bảng chia 5', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 1}', 36, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '1 giờ = ? phút', 10, 'Đo thời gian', '{"answers": ["30", "45", "60", "90"], "correctAnswer": 2}', 37, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '1 m = ? cm', 10, 'Đo độ dài', '{"answers": ["10", "50", "100", "1000"], "correctAnswer": 2}', 38, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số nào lớn nhất: 45, 54, 44, 55?', 10, 'So sánh', '{"answers": ["45", "54", "44", "55"], "correctAnswer": 3}', 39, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '4 × 4 = ?', 10, 'Bảng nhân 4', '{"answers": ["12", "14", "16", "18"], "correctAnswer": 2}', 40, 27, 'van41527', TRUE);

-- Toán lớp 3 (10 câu)
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MULTIPLE_CHOICE', '123 + 234 = ?', 10, 'Phép cộng 3 chữ số', '{"answers": ["355", "356", "357", "358"], "correctAnswer": 2}', 41, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '456 - 321 = ?', 10, 'Phép trừ 3 chữ số', '{"answers": ["133", "134", "135", "136"], "correctAnswer": 2}', 42, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '234 × 2 = ?', 10, 'Nhân số 3 chữ số', '{"answers": ["466", "467", "468", "469"], "correctAnswer": 2}', 43, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '144 ÷ 12 = ?', 10, 'Phép chia', '{"answers": ["10", "11", "12", "13"], "correctAnswer": 2}', 44, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '6 × 7 = ?', 10, 'Bảng nhân 6', '{"answers": ["40", "41", "42", "43"], "correctAnswer": 2}', 45, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '7 × 8 = ?', 10, 'Bảng nhân 7', '{"answers": ["54", "55", "56", "57"], "correctAnswer": 2}', 46, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 × 9 = ?', 10, 'Bảng nhân 8', '{"answers": ["70", "71", "72", "73"], "correctAnswer": 2}', 47, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '9 × 9 = ?', 10, 'Bảng nhân 9', '{"answers": ["79", "80", "81", "82"], "correctAnswer": 2}', 48, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Chu vi hình vuông cạnh 5cm?', 15, 'Hình học', '{"answers": ["15cm", "20cm", "25cm", "30cm"], "correctAnswer": 1}', 49, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '1 km = ? m', 10, 'Đo độ dài', '{"answers": ["10", "100", "1000", "10000"], "correctAnswer": 2}', 50, 27, 'van41527', TRUE);


-- ============================================
-- PHẦN 2: 50 CÂU ĐIỀN VÀO CHỖ TRỐNG (FILL_IN_BLANK)
-- ============================================

-- Toán lớp 1 (20 câu)
INSERT INTO test_questions (test_id, type, content, points, text_with_blanks, blanks_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '2 + _____ = 5', '[{"index": 0, "correctAnswer": "3"}]', 51, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '3 + _____ = 8', '[{"index": 0, "correctAnswer": "5"}]', 52, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '5 + _____ = 10', '[{"index": 0, "correctAnswer": "5"}]', 53, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '4 + _____ = 9', '[{"index": 0, "correctAnswer": "5"}]', 54, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '7 + _____ = 10', '[{"index": 0, "correctAnswer": "3"}]', 55, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép trừ', 15, '_____ - 3 = 5', '[{"index": 0, "correctAnswer": "8"}]', 56, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép trừ', 15, '_____ - 4 = 6', '[{"index": 0, "correctAnswer": "10"}]', 57, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép trừ', 15, '10 - _____ = 7', '[{"index": 0, "correctAnswer": "3"}]', 58, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép trừ', 15, '9 - _____ = 4', '[{"index": 0, "correctAnswer": "5"}]', 59, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép trừ', 15, '8 - _____ = 3', '[{"index": 0, "correctAnswer": "5"}]', 60, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số liền sau', 15, 'Số liền sau của 5 là _____', '[{"index": 0, "correctAnswer": "6"}]', 61, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số liền trước', 15, 'Số liền trước của 7 là _____', '[{"index": 0, "correctAnswer": "6"}]', 62, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành bài toán', 20, 'Lan có _____ quả táo. Lan cho bạn 2 quả. Lan còn 5 quả.', '[{"index": 0, "correctAnswer": "7"}]', 63, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành bài toán', 20, 'An có 6 viên bi. An mua thêm _____ viên. An có 10 viên bi.', '[{"index": 0, "correctAnswer": "4"}]', 64, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền từ', 15, 'Hình có 3 cạnh là hình _____', '[{"index": 0, "correctAnswer": "tam giác"}]', 65, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền từ', 15, 'Hình có 4 góc vuông là hình _____', '[{"index": 0, "correctAnswer": "vuông"}]', 66, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành câu', 20, 'Trong 1 tuần có _____ ngày.', '[{"index": 0, "correctAnswer": "7"}]', 67, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số', 15, '1, 2, 3, _____, 5, 6', '[{"index": 0, "correctAnswer": "4"}]', 68, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số', 15, '10, 9, 8, _____, 6, 5', '[{"index": 0, "correctAnswer": "7"}]', 69, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành', 20, '_____ + _____ = 10', '[{"index": 0, "correctAnswer": "5"}, {"index": 1, "correctAnswer": "5"}]', 70, 27, 'van41527', TRUE);

-- Toán lớp 2 (20 câu)
INSERT INTO test_questions (test_id, type, content, points, text_with_blanks, blanks_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '12 + _____ = 25', '[{"index": 0, "correctAnswer": "13"}]', 71, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '23 + _____ = 50', '[{"index": 0, "correctAnswer": "27"}]', 72, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép trừ', 15, '45 - _____ = 20', '[{"index": 0, "correctAnswer": "25"}]', 73, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép trừ', 15, '_____ - 18 = 30', '[{"index": 0, "correctAnswer": "48"}]', 74, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào bảng nhân', 15, '2 × _____ = 8', '[{"index": 0, "correctAnswer": "4"}]', 75, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào bảng nhân', 15, '3 × _____ = 12', '[{"index": 0, "correctAnswer": "4"}]', 76, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào bảng nhân', 15, '5 × _____ = 20', '[{"index": 0, "correctAnswer": "4"}]', 77, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào bảng nhân', 15, '4 × _____ = 16', '[{"index": 0, "correctAnswer": "4"}]', 78, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép chia', 15, '10 ÷ _____ = 5', '[{"index": 0, "correctAnswer": "2"}]', 79, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép chia', 15, '20 ÷ _____ = 5', '[{"index": 0, "correctAnswer": "4"}]', 80, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép chia', 15, '15 ÷ _____ = 5', '[{"index": 0, "correctAnswer": "3"}]', 81, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép chia', 15, '12 ÷ _____ = 4', '[{"index": 0, "correctAnswer": "3"}]', 82, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, '1 giờ = _____ phút', '[{"index": 0, "correctAnswer": "60"}]', 83, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, '1 mét = _____ centimet', '[{"index": 0, "correctAnswer": "100"}]', 84, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, '1 ki-lô-gam = _____ gam', '[{"index": 0, "correctAnswer": "1000"}]', 85, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, '1 lít = _____ mi-li-lít', '[{"index": 0, "correctAnswer": "1000"}]', 86, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành bài toán', 20, 'Mẹ mua _____ quả cam. Mẹ cho em 5 quả. Mẹ còn 10 quả.', '[{"index": 0, "correctAnswer": "15"}]', 87, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành bài toán', 20, 'Lớp có 25 học sinh nam và _____ học sinh nữ. Tổng cộng có 48 học sinh.', '[{"index": 0, "correctAnswer": "23"}]', 88, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số', 20, '_____ + 15 = 40', '[{"index": 0, "correctAnswer": "25"}]', 89, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số', 20, '60 - _____ = 35', '[{"index": 0, "correctAnswer": "25"}]', 90, 27, 'van41527', TRUE);

-- Toán lớp 3 (10 câu)
INSERT INTO test_questions (test_id, type, content, points, text_with_blanks, blanks_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'FILL_IN_BLANK', 'Điền số', 15, '_____ + 234 = 500', '[{"index": 0, "correctAnswer": "266"}]', 91, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số', 15, '678 - _____ = 345', '[{"index": 0, "correctAnswer": "333"}]', 92, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào bảng nhân', 15, '_____ × 6 = 36', '[{"index": 0, "correctAnswer": "6"}]', 93, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào bảng nhân', 15, '7 × _____ = 49', '[{"index": 0, "correctAnswer": "7"}]', 94, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào phép chia', 15, '48 ÷ _____ = 8', '[{"index": 0, "correctAnswer": "6"}]', 95, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào phép chia', 15, '_____ ÷ 9 = 9', '[{"index": 0, "correctAnswer": "81"}]', 96, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, '1 ki-lô-mét = _____ mét', '[{"index": 0, "correctAnswer": "1000"}]', 97, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền vào công thức', 20, 'Chu vi hình vuông = _____ × cạnh', '[{"index": 0, "correctAnswer": "4"}]', 98, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền vào công thức', 20, 'Chu vi hình chữ nhật = (dài + _____) × 2', '[{"index": 0, "correctAnswer": "rộng"}]', 99, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành', 20, 'Một năm có _____ tháng', '[{"index": 0, "correctAnswer": "12"}]', 100, 27, 'van41527', TRUE);


-- ============================================
-- PHẦN 3: 30 CÂU GHÉP CẶP (MATCHING)
-- ============================================

-- Toán lớp 1-2 (15 câu)
INSERT INTO test_questions (test_id, type, content, points, title, matching_pairs_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MATCHING', 'Nối phép tính với kết quả', 20, 'Ghép phép cộng', 
'[{"left": "2 + 3", "right": "5"}, {"left": "4 + 1", "right": "5"}, {"left": "6 - 1", "right": "5"}, {"left": "10 ÷ 2", "right": "5"}]', 
101, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Nối phép tính với kết quả', 20, 'Ghép phép nhân', 
'[{"left": "2 × 3", "right": "6"}, {"left": "3 × 3", "right": "9"}, {"left": "4 × 2", "right": "8"}, {"left": "5 × 2", "right": "10"}]', 
102, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Nối hình với đặc điểm', 20, 'Nhận biết hình học', 
'[{"left": "Hình có 3 cạnh", "right": "Hình tam giác"}, {"left": "Hình có 4 cạnh bằng nhau", "right": "Hình vuông"}, {"left": "Hình không có góc", "right": "Hình tròn"}, {"left": "Hình có 4 góc vuông", "right": "Hình chữ nhật"}]', 
103, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép đơn vị đo', 20, 'Đo lường', 
'[{"left": "Đo chiều dài", "right": "Mét (m)"}, {"left": "Đo khối lượng", "right": "Ki-lô-gam (kg)"}, {"left": "Đo thời gian", "right": "Giờ (h)"}, {"left": "Đo dung tích", "right": "Lít (l)"}]', 
104, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép số với chữ', 15, 'Đọc số', 
'[{"left": "10", "right": "Mười"}, {"left": "20", "right": "Hai mươi"}, {"left": "50", "right": "Năm mươi"}, {"left": "100", "right": "Một trăm"}]', 
105, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng nhân 3', 20, 'Bảng nhân 3', 
'[{"left": "3 × 2", "right": "6"}, {"left": "3 × 3", "right": "9"}, {"left": "3 × 4", "right": "12"}, {"left": "3 × 5", "right": "15"}]', 
106, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng nhân 4', 20, 'Bảng nhân 4', 
'[{"left": "4 × 2", "right": "8"}, {"left": "4 × 3", "right": "12"}, {"left": "4 × 4", "right": "16"}, {"left": "4 × 5", "right": "20"}]', 
107, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng chia', 20, 'Bảng chia', 
'[{"left": "6 ÷ 2", "right": "3"}, {"left": "8 ÷ 2", "right": "4"}, {"left": "10 ÷ 2", "right": "5"}, {"left": "12 ÷ 2", "right": "6"}]', 
108, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép số chẵn lẻ', 15, 'Số chẵn - Số lẻ', 
'[{"left": "2", "right": "Số chẵn"}, {"left": "3", "right": "Số lẻ"}, {"left": "4", "right": "Số chẵn"}, {"left": "5", "right": "Số lẻ"}]', 
109, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép tháng với số ngày', 20, 'Tháng và ngày', 
'[{"left": "Tháng 1", "right": "31 ngày"}, {"left": "Tháng 2", "right": "28 hoặc 29 ngày"}, {"left": "Tháng 4", "right": "30 ngày"}, {"left": "Tháng 12", "right": "31 ngày"}]', 
110, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép so sánh', 15, 'So sánh số', 
'[{"left": "5 > 3", "right": "Đúng"}, {"left": "7 < 5", "right": "Sai"}, {"left": "8 = 8", "right": "Đúng"}, {"left": "4 > 6", "right": "Sai"}]', 
111, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép phân số', 20, 'Phân số cơ bản', 
'[{"left": "1/2", "right": "Một nửa"}, {"left": "1/3", "right": "Một phần ba"}, {"left": "1/4", "right": "Một phần tư"}, {"left": "1/5", "right": "Một phần năm"}]', 
112, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép tiền Việt Nam', 20, 'Tiền VN', 
'[{"left": "1.000đ", "right": "Một nghìn đồng"}, {"left": "5.000đ", "right": "Năm nghìn đồng"}, {"left": "10.000đ", "right": "Mười nghìn đồng"}, {"left": "20.000đ", "right": "Hai mươi nghìn đồng"}]', 
113, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép ngày trong tuần', 20, 'Ngày trong tuần', 
'[{"left": "Thứ 2", "right": "Đầu tuần"}, {"left": "Thứ 6", "right": "Cuối tuần làm việc"}, {"left": "Thứ 7", "right": "Ngày nghỉ"}, {"left": "Chủ nhật", "right": "Ngày nghỉ"}]', 
114, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng nhân 5', 20, 'Bảng nhân 5', 
'[{"left": "5 × 2", "right": "10"}, {"left": "5 × 3", "right": "15"}, {"left": "5 × 4", "right": "20"}, {"left": "5 × 5", "right": "25"}]', 
115, 27, 'van41527', TRUE);

-- Toán lớp 3 (15 câu)
INSERT INTO test_questions (test_id, type, content, points, title, matching_pairs_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MATCHING', 'Ghép bảng nhân 6', 20, 'Bảng nhân 6', 
'[{"left": "6 × 2", "right": "12"}, {"left": "6 × 3", "right": "18"}, {"left": "6 × 4", "right": "24"}, {"left": "6 × 5", "right": "30"}]', 
116, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng nhân 7', 20, 'Bảng nhân 7', 
'[{"left": "7 × 2", "right": "14"}, {"left": "7 × 3", "right": "21"}, {"left": "7 × 4", "right": "28"}, {"left": "7 × 5", "right": "35"}]', 
117, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng nhân 8', 20, 'Bảng nhân 8', 
'[{"left": "8 × 2", "right": "16"}, {"left": "8 × 3", "right": "24"}, {"left": "8 × 4", "right": "32"}, {"left": "8 × 5", "right": "40"}]', 
118, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng nhân 9', 20, 'Bảng nhân 9', 
'[{"left": "9 × 2", "right": "18"}, {"left": "9 × 3", "right": "27"}, {"left": "9 × 4", "right": "36"}, {"left": "9 × 5", "right": "45"}]', 
119, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng chia 3', 20, 'Bảng chia 3', 
'[{"left": "9 ÷ 3", "right": "3"}, {"left": "12 ÷ 3", "right": "4"}, {"left": "15 ÷ 3", "right": "5"}, {"left": "18 ÷ 3", "right": "6"}]', 
120, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng chia 4', 20, 'Bảng chia 4', 
'[{"left": "12 ÷ 4", "right": "3"}, {"left": "16 ÷ 4", "right": "4"}, {"left": "20 ÷ 4", "right": "5"}, {"left": "24 ÷ 4", "right": "6"}]', 
121, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép đơn vị đo độ dài', 20, 'Đơn vị đo', 
'[{"left": "1 m", "right": "100 cm"}, {"left": "1 km", "right": "1000 m"}, {"left": "1 dm", "right": "10 cm"}, {"left": "1 cm", "right": "10 mm"}]', 
122, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép đơn vị đo khối lượng', 20, 'Đơn vị đo', 
'[{"left": "1 kg", "right": "1000 g"}, {"left": "1 tạ", "right": "100 kg"}, {"left": "1 tấn", "right": "1000 kg"}, {"left": "1 yến", "right": "10 kg"}]', 
123, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép công thức hình học', 25, 'Công thức', 
'[{"left": "Chu vi hình vuông", "right": "Cạnh × 4"}, {"left": "Chu vi hình chữ nhật", "right": "(Dài + Rộng) × 2"}, {"left": "Diện tích hình vuông", "right": "Cạnh × Cạnh"}, {"left": "Diện tích hình chữ nhật", "right": "Dài × Rộng"}]', 
124, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép tính chất', 20, 'Tính chất phép tính', 
'[{"left": "a + b", "right": "b + a (giao hoán)"}, {"left": "a × b", "right": "b × a (giao hoán)"}, {"left": "a + 0", "right": "a"}, {"left": "a × 1", "right": "a"}]', 
125, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép số La Mã', 20, 'Số La Mã', 
'[{"left": "I", "right": "1"}, {"left": "V", "right": "5"}, {"left": "X", "right": "10"}, {"left": "L", "right": "50"}]', 
126, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép hình khối', 20, 'Hình khối', 
'[{"left": "Hình lập phương", "right": "6 mặt vuông"}, {"left": "Hình hộp chữ nhật", "right": "6 mặt chữ nhật"}, {"left": "Hình trụ", "right": "2 đáy tròn"}, {"left": "Hình cầu", "right": "Không có cạnh"}]', 
127, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép góc', 20, 'Các loại góc', 
'[{"left": "Góc vuông", "right": "90 độ"}, {"left": "Góc nhọn", "right": "< 90 độ"}, {"left": "Góc tù", "right": "> 90 độ"}, {"left": "Góc bẹt", "right": "180 độ"}]', 
128, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép thứ tự phép tính', 20, 'Thứ tự thực hiện', 
'[{"left": "Bước 1", "right": "Ngoặc"}, {"left": "Bước 2", "right": "Nhân, chia"}, {"left": "Bước 3", "right": "Cộng, trừ"}, {"left": "Cùng cấp", "right": "Trái sang phải"}]', 
129, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép đơn vị thời gian', 20, 'Thời gian', 
'[{"left": "1 phút", "right": "60 giây"}, {"left": "1 giờ", "right": "60 phút"}, {"left": "1 ngày", "right": "24 giờ"}, {"left": "1 tuần", "right": "7 ngày"}]', 
130, 27, 'van41527', TRUE);


-- ============================================
-- PHẦN 4: 20 CÂU TỰ LUẬN (ESSAY)
-- ============================================

INSERT INTO test_questions (test_id, type, content, points, prompt, max_length, rubric, order_index, created_by, created_by_name, is_shared) VALUES
-- Toán lớp 1 (5 câu)
(NULL, 'ESSAY', 'Giải bài toán có lời văn', 20, 
'Lan có 8 cây bút. Lan cho bạn 3 cây bút. Hỏi Lan còn lại bao nhiêu cây bút? Viết bài giải.', 
500, 'Cần có bài giải và đáp số đúng (5 cây bút)', 131, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải bài toán', 20, 
'Lớp 1A có 15 học sinh nam và 13 học sinh nữ. Hỏi lớp 1A có tất cả bao nhiêu học sinh? Viết bài giải.', 
500, 'Có bài giải và tính đúng (28 học sinh)', 132, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải thích', 15, 
'Em hãy giải thích tại sao 3 + 2 = 2 + 3?', 
300, 'Giải thích được tính chất giao hoán của phép cộng', 133, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Vẽ và giải thích', 20, 
'Hãy mô tả đặc điểm của hình tam giác. Hình tam giác có mấy cạnh? Có mấy góc?', 
400, 'Mô tả đúng: 3 cạnh, 3 góc', 134, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải bài toán số học', 20, 
'An có 10 viên bi. Bạn cho An thêm 5 viên bi. Hỏi bây giờ An có bao nhiêu viên bi? Viết bài giải.', 
400, 'Có bài giải và đáp số đúng (15 viên bi)', 135, 27, 'van41527', TRUE),

-- Toán lớp 2 (8 câu)
(NULL, 'ESSAY', 'Bài toán có nhiều phép tính', 25, 
'Mẹ mua 3 túi kẹo, mỗi túi có 8 viên kẹo. Mẹ cho em và anh mỗi người 10 viên. Hỏi còn lại bao nhiêu viên kẹo? Viết bài giải chi tiết.', 
600, 'Cần có: Tính tổng số kẹo (3×8=24), trừ đi số kẹo cho (10+10=20), còn lại 4 viên', 136, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán thực tế', 25, 
'An có 50.000 đồng. An mua 2 quyển vở, mỗi quyển 5.000 đồng và 1 cây bút 8.000 đồng. Hỏi An còn lại bao nhiêu tiền? Viết bài giải.', 
600, 'Có bài giải đầy đủ các bước: 5.000×2 + 8.000 = 18.000, 50.000 - 18.000 = 32.000đ', 137, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán chia đều', 20, 
'Bố mua 24 quả cam. Bố chia đều cho 3 người con. Hỏi mỗi người được bao nhiêu quả cam? Viết bài giải.', 
400, 'Có bài giải và đáp số: 24 ÷ 3 = 8 quả cam', 138, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán tìm số', 25, 
'Tổng của hai số là 50. Biết số thứ nhất là 23. Tìm số thứ hai. Viết bài giải.', 
400, 'Có bài giải: 50 - 23 = 27', 139, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải thích tính chất', 20, 
'Em hãy giải thích tại sao 5 × 2 = 2 × 5?', 
400, 'Giải thích được tính chất giao hoán của phép nhân', 140, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán so sánh', 20, 
'Lan có 25 viên bi. An có 18 viên bi. Hỏi Lan có nhiều hơn An bao nhiêu viên bi? Viết bài giải.', 
400, 'Có bài giải: 25 - 18 = 7 viên bi', 141, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Vẽ hình và tính', 25, 
'Vẽ một hình chữ nhật có chiều dài 6cm và chiều rộng 4cm. Hãy tính chu vi của hình chữ nhật đó.', 
500, 'Có công thức: P = (6 + 4) × 2 = 20cm', 142, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán đo lường', 20, 
'Một đoạn đường dài 100m. An đi được 45m. Hỏi An còn phải đi bao nhiêu mét nữa? Viết bài giải.', 
400, 'Có bài giải: 100 - 45 = 55m', 143, 27, 'van41527', TRUE),

-- Toán lớp 3 (7 câu)
(NULL, 'ESSAY', 'Bài toán chu vi', 25, 
'Một hình chữ nhật có chiều dài 8cm và chiều rộng 5cm. Tính chu vi và diện tích của hình chữ nhật đó. Viết bài giải và công thức.', 
600, 'Chu vi: (8+5)×2 = 26cm, Diện tích: 8×5 = 40cm²', 144, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán diện tích', 25, 
'Một mảnh vườn hình vuông có cạnh dài 6m. Tính chu vi và diện tích của mảnh vườn đó.', 
500, 'Chu vi: 6×4 = 24m, Diện tích: 6×6 = 36m²', 145, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán tổng hợp', 30, 
'Một cửa hàng có 345 quả táo. Buổi sáng bán được 128 quả, buổi chiều bán được 95 quả. Hỏi cửa hàng còn lại bao nhiêu quả táo? Viết bài giải.', 
600, 'Số táo bán: 128 + 95 = 223, Còn lại: 345 - 223 = 122 quả', 146, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán có nhiều bước', 30, 
'Một xưởng may có 450 mét vải. Dùng 135 mét may áo, 178 mét may quần. Hỏi xưởng còn lại bao nhiêu mét vải? Viết bài giải chi tiết.', 
600, 'Đã dùng: 135 + 178 = 313m, Còn lại: 450 - 313 = 137m', 147, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán tư duy', 30, 
'Trong vườn có 15 con gà và 12 con vịt. Hỏi trong vườn có bao nhiêu con chim? Trong vườn có tất cả bao nhiêu cái chân? Viết bài giải.', 
600, 'Tổng số chim: 15 + 12 = 27 con, Số chân: 27 × 2 = 54 cái chân', 148, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải thích khái niệm', 25, 
'Phân số là gì? Hãy giải thích ý nghĩa của phân số 1/2 và cho ví dụ minh họa.', 
500, 'Giải thích được phân số là một phần của tổng thể, 1/2 là một nửa', 149, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán quy luật', 25, 
'Tìm số tiếp theo trong dãy số: 2, 4, 6, 8, ... ? Giải thích quy luật của dãy số này.', 
400, 'Số tiếp theo là 10, quy luật: mỗi số cách nhau 2 đơn vị', 150, 27, 'van41527', TRUE);


-- ============================================
-- PHẦN 5: TẠO 4 BÀI KIỂM TRA/BÀI TẬP
-- ============================================

-- BÀI 1: Kiểm tra Toán lớp 1
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra giữa kỳ I - Toán lớp 1', 'Toán', '1', 30, 
'Kiểm tra phép cộng trừ trong phạm vi 10, nhận biết hình học cơ bản', 
27, 'van41527', 0, 0, 'EXAM', 'PUBLISHED', 
'2026-07-10 08:00:00', '2026-07-17 23:59:59', NOW());

SET @test1_id = LAST_INSERT_ID();

-- Gán câu hỏi cho bài 1: 10 câu trắc nghiệm + 5 câu điền chỗ trống + 2 câu ghép cặp + 2 câu tự luận (19 câu)
UPDATE test_questions SET test_id = @test1_id, order_index = 1 WHERE id IN (1,2,3,4,5,6,7,8,9,10);
UPDATE test_questions SET test_id = @test1_id, order_index = 11 WHERE id IN (51,52,53,54,55);
UPDATE test_questions SET test_id = @test1_id, order_index = 16 WHERE id IN (101,103);
UPDATE test_questions SET test_id = @test1_id, order_index = 18 WHERE id IN (131,132);

UPDATE tests SET 
    question_count = (SELECT COUNT(*) FROM test_questions WHERE test_id = @test1_id),
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @test1_id)
WHERE id = @test1_id;

-- BÀI 2: Bài tập Toán lớp 2
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Bài tập tuần 12 - Bảng nhân và chia', 'Toán', '2', 25, 
'Luyện tập bảng nhân 2, 3, 4, 5 và bảng chia tương ứng', 
27, 'van41527', 0, 0, 'EXERCISE', 'PUBLISHED', 
'2026-07-08 00:00:00', '2026-07-15 23:59:59', NOW());

SET @test2_id = LAST_INSERT_ID();

-- Gán câu hỏi cho bài 2: 10 câu trắc nghiệm + 8 câu điền chỗ trống + 5 câu ghép cặp (23 câu)
UPDATE test_questions SET test_id = @test2_id, order_index = 1 WHERE id IN (26,27,28,29,30,31,32,33,34,35);
UPDATE test_questions SET test_id = @test2_id, order_index = 11 WHERE id IN (75,76,77,78,79,80,81,82);
UPDATE test_questions SET test_id = @test2_id, order_index = 19 WHERE id IN (102,106,107,108,115);

UPDATE tests SET 
    question_count = (SELECT COUNT(*) FROM test_questions WHERE test_id = @test2_id),
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @test2_id)
WHERE id = @test2_id;

-- BÀI 3: Kiểm tra Toán lớp 2 - Tổng hợp
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra cuối kỳ I - Toán lớp 2', 'Toán', '2', 40, 
'Kiểm tra tổng hợp: phép tính, bảng nhân chia, đo lường, bài toán có lời văn', 
27, 'van41527', 0, 0, 'EXAM', 'PUBLISHED', 
'2026-07-12 08:00:00', '2026-07-19 23:59:59', NOW());

SET @test3_id = LAST_INSERT_ID();

-- Gán câu hỏi cho bài 3: 8 câu trắc nghiệm + 6 câu điền chỗ trống + 3 câu ghép cặp + 4 câu tự luận (21 câu)
UPDATE test_questions SET test_id = @test3_id, order_index = 1 WHERE id IN (21,22,23,24,25,36,37,38);
UPDATE test_questions SET test_id = @test3_id, order_index = 9 WHERE id IN (71,72,73,83,84,87);
UPDATE test_questions SET test_id = @test3_id, order_index = 15 WHERE id IN (104,109,113);
UPDATE test_questions SET test_id = @test3_id, order_index = 18 WHERE id IN (136,137,138,141);

UPDATE tests SET 
    question_count = (SELECT COUNT(*) FROM test_questions WHERE test_id = @test3_id),
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @test3_id)
WHERE id = @test3_id;

-- BÀI 4: Bài tập Toán lớp 3
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Bài tập ôn tập - Toán lớp 3', 'Toán', '3', 35, 
'Ôn tập phép nhân chia số có 3 chữ số, bảng nhân 6-9, chu vi diện tích', 
27, 'van41527', 0, 0, 'EXERCISE', 'PUBLISHED', 
'2026-07-05 00:00:00', '2026-07-12 23:59:59', NOW());

SET @test4_id = LAST_INSERT_ID();

-- Gán câu hỏi cho bài 4: 10 câu trắc nghiệm + 8 câu điền chỗ trống + 6 câu ghép cặp + 4 câu tự luận (28 câu)
UPDATE test_questions SET test_id = @test4_id, order_index = 1 WHERE id IN (41,42,43,44,45,46,47,48,49,50);
UPDATE test_questions SET test_id = @test4_id, order_index = 11 WHERE id IN (91,92,93,94,95,96,97,98);
UPDATE test_questions SET test_id = @test4_id, order_index = 19 WHERE id IN (116,117,118,119,122,124);
UPDATE test_questions SET test_id = @test4_id, order_index = 25 WHERE id IN (144,145,146,147);

UPDATE tests SET 
    question_count = (SELECT COUNT(*) FROM test_questions WHERE test_id = @test4_id),
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @test4_id)
WHERE id = @test4_id;

-- ============================================
-- PHẦN 6: THỐNG KÊ VÀ HOÀN TẤT
-- ============================================

SET FOREIGN_KEY_CHECKS = 1;

-- Hiển thị thống kê
SELECT '========================================' as '';
SELECT '✓ IMPORT DỮ LIỆU THÀNH CÔNG!' as 'THONG_BAO';
SELECT '========================================' as '';
SELECT 'User: van41527@gmail.com (ID: 27)' as '';
SELECT '========================================' as '';

SELECT 'Loại câu hỏi' as 'LOAI', COUNT(*) as 'SO_LUONG' FROM test_questions WHERE created_by = 27 GROUP BY 'Loại câu hỏi'
UNION ALL
SELECT CONCAT('- ', type), COUNT(*) FROM test_questions WHERE created_by = 27 GROUP BY type
UNION ALL
SELECT '--------', '--------'
UNION ALL
SELECT 'Bài kiểm tra/Bài tập', COUNT(*) FROM tests WHERE created_by = 27
UNION ALL
SELECT '- EXAM (Kiểm tra)', COUNT(*) FROM tests WHERE created_by = 27 AND test_type = 'EXAM'
UNION ALL
SELECT '- EXERCISE (Bài tập)', COUNT(*) FROM tests WHERE created_by = 27 AND test_type = 'EXERCISE';

SELECT '========================================' as '';
SELECT 'CHI TIẾT CÁC BÀI' as '';
SELECT '========================================' as '';

SELECT 
    name as 'Tên bài',
    subject as 'Môn',
    grade as 'Lớp',
    test_type as 'Loại',
    question_count as 'Số câu',
    total_points as 'Tổng điểm',
    duration as 'Phút',
    DATE_FORMAT(start_at, '%d/%m/%Y') as 'Bắt đầu',
    DATE_FORMAT(end_at, '%d/%m/%Y') as 'Kết thúc'
FROM tests 
WHERE created_by = 27
ORDER BY grade, test_type;

SELECT '========================================' as '';
SELECT '📝 LƯU Ý:' as '';
SELECT '- Tổng: 150 câu hỏi, 4 bài kiểm tra/bài tập' as '';
SELECT '- Tất cả câu hỏi đều có đáp án chi tiết' as '';
SELECT '- Chưa có ảnh/audio (bạn tự thêm sau)' as '';
SELECT '========================================' as '';

/*
╔════════════════════════════════════════════════════════════╗
║  HƯỚNG DẪN SỬ DỤNG                                        ║
╚════════════════════════════════════════════════════════════╝

📌 FILE NÀY TẠO CHO: van41527@gmail.com (ID: 27)

📊 NỘI DUNG ĐÃ TẠO:
   • 150 câu hỏi:
     - 50 câu trắc nghiệm (MULTIPLE_CHOICE)
     - 50 câu điền vào chỗ trống (FILL_IN_BLANK)
     - 30 câu ghép cặp (MATCHING)
     - 20 câu tự luận (ESSAY)
   
   • 4 bài kiểm tra/bài tập:
     - 2 bài kiểm tra (EXAM): Lớp 1, Lớp 2
     - 2 bài tập (EXERCISE): Lớp 2, Lớp 3

🔧 CÁCH CHẠY:
   mysql -u root -p test_db < insert_data_user27_final.sql

✅ KIỂM TRA SAU KHI CHẠY:
   SELECT COUNT(*) FROM test_questions WHERE created_by = 27;
   SELECT * FROM tests WHERE created_by = 27;

📝 ĐẶC ĐIỂM:
   ✓ Tất cả câu hỏi đều có đáp án chi tiết trong JSON
   ✓ Câu trắc nghiệm: answers_json với correctAnswer
   ✓ Câu điền chỗ trống: blanks_json với correctAnswer
   ✓ Câu ghép cặp: matching_pairs_json với cặp left-right
   ✓ Câu tự luận: rubric (tiêu chí chấm điểm)

⚠️ LƯU Ý:
   - Chưa có ảnh/audio (bạn sẽ tự thêm)
   - Thời gian: 05/07/2026 - 19/07/2026 (có thể điều chỉnh)
   - Cần classroom_id thực nếu muốn tạo classroom_posts

🎨 THÊM ẢNH/AUDIO (sau khi upload):
   UPDATE test_questions 
   SET image_url = 'https://res.cloudinary.com/.../image.png'
   WHERE id = 1;

╚════════════════════════════════════════════════════════════╝
*/
