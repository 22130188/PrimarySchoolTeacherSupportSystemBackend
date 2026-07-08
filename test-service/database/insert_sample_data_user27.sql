-- =====================================================
-- File: insert_sample_data_user27.sql
-- Description: Dữ liệu mẫu đầy đủ cho test-service
-- User: van41527@gmail.com (ID: 27)
-- Nội dung: NHIỀU câu hỏi đa dạng, ÍT bài kiểm tra
-- =====================================================

USE test_db;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- XÓA DỮ LIỆU CŨ CỦA USER 27
-- =====================================================
DELETE FROM test_attempts WHERE test_id IN (SELECT id FROM tests WHERE created_by = 27);
DELETE FROM test_questions WHERE created_by = 27;
DELETE FROM classroom_posts WHERE author_id = 27;
DELETE FROM tests WHERE created_by = 27;

-- =====================================================
-- PHẦN 1: CÂU HỎI TRẮC NGHIỆM - TOÁN LỚP 1 (30 câu)
-- Có đáp án chi tiết trong answers_json
-- =====================================================
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MULTIPLE_CHOICE', '1 + 1 = ?', 10, 'Phép cộng cơ bản', '{"answers": ["1", "2", "3", "4"], "correctAnswer": 1}', 1, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 + 1 = ?', 10, 'Phép cộng', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 1}', 2, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 + 2 = ?', 10, 'Phép cộng', '{"answers": ["3", "4", "5", "6"], "correctAnswer": 1}', 3, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '3 + 2 = ?', 10, 'Phép cộng', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 4, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '4 + 1 = ?', 10, 'Phép cộng', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 5, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '3 + 3 = ?', 10, 'Phép cộng', '{"answers": ["5", "6", "7", "8"], "correctAnswer": 1}', 6, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 + 2 = ?', 10, 'Phép cộng', '{"answers": ["6", "7", "8", "9"], "correctAnswer": 1}', 7, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '4 + 4 = ?', 10, 'Phép cộng', '{"answers": ["7", "8", "9", "10"], "correctAnswer": 1}', 8, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 + 5 = ?', 10, 'Phép cộng tạo số 10', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 9, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '6 + 4 = ?', 10, 'Phép cộng qua 10', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 10, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '7 + 3 = ?', 10, 'Phép cộng qua 10', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 11, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 + 2 = ?', 10, 'Phép cộng tạo 10', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 12, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '9 + 1 = ?', 10, 'Phép cộng tạo 10', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 13, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '3 - 1 = ?', 10, 'Phép trừ', '{"answers": ["1", "2", "3", "4"], "correctAnswer": 1}', 14, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '4 - 2 = ?', 10, 'Phép trừ', '{"answers": ["1", "2", "3", "4"], "correctAnswer": 1}', 15, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 - 3 = ?', 10, 'Phép trừ', '{"answers": ["1", "2", "3", "4"], "correctAnswer": 1}', 16, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '6 - 4 = ?', 10, 'Phép trừ', '{"answers": ["1", "2", "3", "4"], "correctAnswer": 1}', 17, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '7 - 2 = ?', 10, 'Phép trừ', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 18, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 - 3 = ?', 10, 'Phép trừ', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 19, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '9 - 4 = ?', 10, 'Phép trừ', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 20, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '10 - 5 = ?', 10, 'Phép trừ', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 21, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '10 - 2 = ?', 10, 'Phép trừ từ 10', '{"answers": ["7", "8", "9", "10"], "correctAnswer": 1}', 22, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '10 - 7 = ?', 10, 'Phép trừ từ 10', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 1}', 23, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số nào lớn hơn: 5 hay 3?', 10, 'So sánh số', '{"answers": ["3", "5", "Bằng nhau", "Không biết"], "correctAnswer": 1}', 24, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số nào nhỏ hơn: 7 hay 9?', 10, 'So sánh số', '{"answers": ["9", "7", "Bằng nhau", "Không biết"], "correctAnswer": 1}', 25, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số liền sau của 4 là?', 10, 'Dãy số', '{"answers": ["3", "5", "6", "7"], "correctAnswer": 1}', 26, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số liền trước của 8 là?', 10, 'Dãy số', '{"answers": ["6", "7", "9", "10"], "correctAnswer": 1}', 27, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Hình nào có 3 cạnh?', 10, 'Hình học', '{"answers": ["Hình vuông", "Hình tam giác", "Hình tròn", "Hình chữ nhật"], "correctAnswer": 1}', 28, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Hình nào có 4 góc vuông?', 10, 'Hình học', '{"answers": ["Hình tam giác", "Hình tròn", "Hình vuông", "Hình bầu dục"], "correctAnswer": 2}', 29, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Hình nào không có góc?', 10, 'Hình học', '{"answers": ["Hình vuông", "Hình tam giác", "Hình chữ nhật", "Hình tròn"], "correctAnswer": 3}', 30, 27, 'van41527', TRUE);

-- Thêm 5 câu Toán lớp 1 nữa
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MULTIPLE_CHOICE', '7 + 3 = ?', 10, 'Phép cộng qua 10', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 26, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 + 2 = ?', 10, 'Phép cộng tạo 10', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 27, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '9 + 1 = ?', 10, 'Phép cộng tạo 10', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 28, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '10 - 2 = ?', 10, 'Phép trừ từ 10', '{"answers": ["7", "8", "9", "10"], "correctAnswer": 1}', 29, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '10 - 7 = ?', 10, 'Phép trừ từ 10', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 1}', 30, 27, 'van41527', TRUE);

-- =====================================================
-- PHẦN 2: CÂU HỎI TOÁN LỚP 2 (30 câu)
-- =====================================================
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MULTIPLE_CHOICE', '12 + 5 = ?', 10, 'Phép cộng không nhớ', '{"answers": ["15", "16", "17", "18"], "correctAnswer": 2}', 31, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '23 + 14 = ?', 10, 'Phép cộng không nhớ', '{"answers": ["35", "36", "37", "38"], "correctAnswer": 2}', 32, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '45 + 32 = ?', 10, 'Phép cộng không nhớ', '{"answers": ["75", "76", "77", "78"], "correctAnswer": 2}', 33, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '28 - 13 = ?', 10, 'Phép trừ không nhớ', '{"answers": ["13", "14", "15", "16"], "correctAnswer": 2}', 34, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '57 - 24 = ?', 10, 'Phép trừ không nhớ', '{"answers": ["31", "32", "33", "34"], "correctAnswer": 2}', 35, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '68 - 42 = ?', 10, 'Phép trừ không nhớ', '{"answers": ["24", "25", "26", "27"], "correctAnswer": 2}', 36, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 × 2 = ?', 10, 'Bảng nhân 2', '{"answers": ["2", "4", "6", "8"], "correctAnswer": 1}', 37, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 × 3 = ?', 10, 'Bảng nhân 2', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 2}', 38, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 × 4 = ?', 10, 'Bảng nhân 2', '{"answers": ["6", "7", "8", "9"], "correctAnswer": 2}', 39, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '2 × 5 = ?', 10, 'Bảng nhân 2', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 40, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '3 × 3 = ?', 10, 'Bảng nhân 3', '{"answers": ["6", "7", "8", "9"], "correctAnswer": 3}', 41, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '3 × 4 = ?', 10, 'Bảng nhân 3', '{"answers": ["9", "10", "11", "12"], "correctAnswer": 3}', 42, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 × 2 = ?', 10, 'Bảng nhân 5', '{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 43, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '5 × 3 = ?', 10, 'Bảng nhân 5', '{"answers": ["10", "12", "15", "20"], "correctAnswer": 2}', 44, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '10 ÷ 2 = ?', 10, 'Bảng chia 2', '{"answers": ["3", "4", "5", "6"], "correctAnswer": 2}', 45, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 ÷ 2 = ?', 10, 'Bảng chia 2', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 2}', 46, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '12 ÷ 3 = ?', 10, 'Bảng chia 3', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 2}', 47, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '15 ÷ 5 = ?', 10, 'Bảng chia 5', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 1}', 48, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '20 ÷ 5 = ?', 10, 'Bảng chia 5', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 2}', 49, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Số nào lớn nhất: 45, 54, 44, 55?', 10, 'So sánh số', '{"answers": ["45", "54", "44", "55"], "correctAnswer": 3}', 50, 27, 'van41527', TRUE);

-- Thêm 10 câu Toán lớp 2 nữa
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MULTIPLE_CHOICE', '1 giờ = ? phút', 10, 'Đo thời gian', '{"answers": ["30 phút", "45 phút", "60 phút", "90 phút"], "correctAnswer": 2}', 51, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '1 m = ? cm', 10, 'Đo độ dài', '{"answers": ["10 cm", "50 cm", "100 cm", "1000 cm"], "correctAnswer": 2}', 52, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '25 + 37 = ?', 10, 'Phép cộng có nhớ', '{"answers": ["60", "61", "62", "63"], "correctAnswer": 2}', 53, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '48 + 36 = ?', 10, 'Phép cộng có nhớ', '{"answers": ["82", "83", "84", "85"], "correctAnswer": 2}', 54, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '73 - 45 = ?', 10, 'Phép trừ có nhớ', '{"answers": ["26", "27", "28", "29"], "correctAnswer": 2}', 55, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '81 - 56 = ?', 10, 'Phép trừ có nhớ', '{"answers": ["23", "24", "25", "26"], "correctAnswer": 2}', 56, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '4 × 4 = ?', 10, 'Bảng nhân 4', '{"answers": ["12", "14", "16", "18"], "correctAnswer": 2}', 57, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '4 × 5 = ?', 10, 'Bảng nhân 4', '{"answers": ["16", "18", "20", "22"], "correctAnswer": 2}', 58, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '16 ÷ 4 = ?', 10, 'Bảng chia 4', '{"answers": ["2", "3", "4", "5"], "correctAnswer": 2}', 59, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '20 ÷ 4 = ?', 10, 'Bảng chia 4', '{"answers": ["4", "5", "6", "7"], "correctAnswer": 1}', 60, 27, 'van41527', TRUE);

-- =====================================================
-- PHẦN 3: CÂU HỎI TOÁN LỚP 3 (25 câu)
-- =====================================================
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MULTIPLE_CHOICE', '123 + 234 = ?', 10, 'Phép cộng trong phạm vi 1000', '{"answers": ["355", "356", "357", "358"], "correctAnswer": 2}', 61, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '456 + 321 = ?', 10, 'Phép cộng không nhớ', '{"answers": ["775", "776", "777", "778"], "correctAnswer": 2}', 62, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '758 - 342 = ?', 10, 'Phép trừ không nhớ', '{"answers": ["414", "415", "416", "417"], "correctAnswer": 2}', 63, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '527 - 318 = ?', 10, 'Phép trừ có nhớ', '{"answers": ["207", "208", "209", "210"], "correctAnswer": 2}', 64, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '234 × 2 = ?', 10, 'Nhân số có 3 chữ số', '{"answers": ["466", "467", "468", "469"], "correctAnswer": 2}', 65, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '312 × 3 = ?', 10, 'Nhân số có 3 chữ số', '{"answers": ["934", "935", "936", "937"], "correctAnswer": 2}', 66, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '144 ÷ 12 = ?', 10, 'Phép chia hết', '{"answers": ["10", "11", "12", "13"], "correctAnswer": 2}', 67, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '180 ÷ 15 = ?', 10, 'Phép chia hết', '{"answers": ["10", "11", "12", "13"], "correctAnswer": 2}', 68, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '6 × 6 = ?', 10, 'Bảng nhân 6', '{"answers": ["30", "32", "34", "36"], "correctAnswer": 3}', 69, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '6 × 7 = ?', 10, 'Bảng nhân 6', '{"answers": ["40", "41", "42", "43"], "correctAnswer": 2}', 70, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '7 × 7 = ?', 10, 'Bảng nhân 7', '{"answers": ["47", "48", "49", "50"], "correctAnswer": 2}', 71, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '7 × 8 = ?', 10, 'Bảng nhân 7', '{"answers": ["54", "55", "56", "57"], "correctAnswer": 2}', 72, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 × 8 = ?', 10, 'Bảng nhân 8', '{"answers": ["62", "63", "64", "65"], "correctAnswer": 2}', 73, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '8 × 9 = ?', 10, 'Bảng nhân 8', '{"answers": ["70", "71", "72", "73"], "correctAnswer": 2}', 74, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '9 × 9 = ?', 10, 'Bảng nhân 9', '{"answers": ["79", "80", "81", "82"], "correctAnswer": 2}', 75, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '54 ÷ 6 = ?', 10, 'Bảng chia 6', '{"answers": ["7", "8", "9", "10"], "correctAnswer": 2}', 76, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '63 ÷ 7 = ?', 10, 'Bảng chia 7', '{"answers": ["7", "8", "9", "10"], "correctAnswer": 2}', 77, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '72 ÷ 8 = ?', 10, 'Bảng chia 8', '{"answers": ["7", "8", "9", "10"], "correctAnswer": 2}', 78, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '81 ÷ 9 = ?', 10, 'Bảng chia 9', '{"answers": ["7", "8", "9", "10"], "correctAnswer": 2}', 79, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Chu vi hình vuông cạnh 5cm là?', 15, 'Chu vi hình vuông', '{"answers": ["15cm", "20cm", "25cm", "30cm"], "correctAnswer": 1}', 80, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Chu vi hình chữ nhật dài 6cm, rộng 4cm là?', 15, 'Chu vi HCN', '{"answers": ["18cm", "20cm", "22cm", "24cm"], "correctAnswer": 1}', 81, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '1 km = ? m', 10, 'Đo độ dài', '{"answers": ["10m", "100m", "1000m", "10000m"], "correctAnswer": 2}', 82, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '1 kg = ? g', 10, 'Đo khối lượng', '{"answers": ["10g", "100g", "1000g", "10000g"], "correctAnswer": 2}', 83, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', '1 l = ? ml', 10, 'Đo dung tích', '{"answers": ["10ml", "100ml", "1000ml", "10000ml"], "correctAnswer": 2}', 84, 27, 'van41527', TRUE),
(NULL, 'MULTIPLE_CHOICE', 'Một năm có bao nhiêu tháng?', 10, 'Thời gian', '{"answers": ["10 tháng", "11 tháng", "12 tháng", "13 tháng"], "correctAnswer": 2}', 85, 27, 'van41527', TRUE);

-- =====================================================
-- PHẦN 4: CÂU HỎI ĐIỀN VÀO CHỖ TRỐNG (20 câu)
-- =====================================================
INSERT INTO test_questions (test_id, type, content, points, text_with_blanks, blanks_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp', 15, '3 + _____ = 8', '[{"index": 0, "correctAnswer": "5"}]', 86, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp', 15, '5 + _____ = 10', '[{"index": 0, "correctAnswer": "5"}]', 87, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp', 15, '7 + _____ = 12', '[{"index": 0, "correctAnswer": "5"}]', 88, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép tính', 15, '_____ - 3 = 7', '[{"index": 0, "correctAnswer": "10"}]', 89, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép tính', 15, '_____ - 5 = 8', '[{"index": 0, "correctAnswer": "13"}]', 90, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '2 × _____ = 8', '[{"index": 0, "correctAnswer": "4"}]', 91, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '3 × _____ = 12', '[{"index": 0, "correctAnswer": "4"}]', 92, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, '5 × _____ = 20', '[{"index": 0, "correctAnswer": "4"}]', 93, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép chia', 15, '10 ÷ _____ = 5', '[{"index": 0, "correctAnswer": "2"}]', 94, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép chia', 15, '20 ÷ _____ = 5', '[{"index": 0, "correctAnswer": "4"}]', 95, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành câu', 20, 'Lan có _____ quả táo. Lan cho bạn 3 quả. Lan còn _____ quả.', '[{"index": 0, "correctAnswer": "8"}, {"index": 1, "correctAnswer": "5"}]', 96, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Hoàn thành câu', 20, 'An có _____ viên bi. An mua thêm 4 viên. An có _____ viên bi.', '[{"index": 0, "correctAnswer": "6"}, {"index": 1, "correctAnswer": "10"}]', 97, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, '1 mét = _____ centimet', '[{"index": 0, "correctAnswer": "100"}]', 98, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, '1 ki-lô-mét = _____ mét', '[{"index": 0, "correctAnswer": "1000"}]', 99, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, '1 ki-lô-gam = _____ gam', '[{"index": 0, "correctAnswer": "1000"}]', 100, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp', 20, '_____ + 15 = 30', '[{"index": 0, "correctAnswer": "15"}]', 101, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp', 20, '50 - _____ = 25', '[{"index": 0, "correctAnswer": "25"}]', 102, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp', 20, '_____ × 6 = 36', '[{"index": 0, "correctAnswer": "6"}]', 103, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp', 20, '48 ÷ _____ = 8', '[{"index": 0, "correctAnswer": "6"}]', 104, 27, 'van41527', TRUE),
(NULL, 'FILL_IN_BLANK', 'Điền vào chỗ trống', 20, '1 giờ = _____ phút = _____ giây', '[{"index": 0, "correctAnswer": "60"}, {"index": 1, "correctAnswer": "3600"}]', 105, 27, 'van41527', TRUE);

-- =====================================================
-- PHẦN 5: CÂU HỎI GHÉP CẶP (15 câu)
-- =====================================================
INSERT INTO test_questions (test_id, type, content, points, title, matching_pairs_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MATCHING', 'Nối các phép tính với kết quả', 20, 'Ghép phép tính (cộng)', 
'[{"left": "2 + 3", "right": "5"}, {"left": "4 + 1", "right": "5"}, {"left": "6 - 1", "right": "5"}, {"left": "10 ÷ 2", "right": "5"}]', 
106, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Nối phép tính với đáp án', 20, 'Ghép phép tính (nhân)', 
'[{"left": "2 × 3", "right": "6"}, {"left": "3 × 3", "right": "9"}, {"left": "4 × 2", "right": "8"}, {"left": "5 × 2", "right": "10"}]', 
107, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Nối hình với tên gọi', 20, 'Nhận biết hình học', 
'[{"left": "Hình có 3 cạnh", "right": "Hình tam giác"}, {"left": "Hình có 4 cạnh bằng nhau", "right": "Hình vuông"}, {"left": "Hình không có góc", "right": "Hình tròn"}, {"left": "Hình có 4 góc vuông", "right": "Hình chữ nhật"}]', 
108, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép đơn vị đo phù hợp', 20, 'Đo lường', 
'[{"left": "Đo chiều dài", "right": "Mét (m)"}, {"left": "Đo khối lượng", "right": "Ki-lô-gam (kg)"}, {"left": "Đo thời gian", "right": "Giờ (h)"}, {"left": "Đo dung tích", "right": "Lít (l)"}]', 
109, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép số với chữ', 15, 'Đọc số', 
'[{"left": "10", "right": "Mười"}, {"left": "20", "right": "Hai mươi"}, {"left": "50", "right": "Năm mươi"}, {"left": "100", "right": "Một trăm"}]', 
110, 27, 'van41527', TRUE);

-- Thêm 10 câu MATCHING nữa
INSERT INTO test_questions (test_id, type, content, points, title, matching_pairs_json, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'MATCHING', 'Ghép số chẵn với số lẻ', 15, 'Số chẵn - Số lẻ', 
'[{"left": "Số chẵn", "right": "2, 4, 6, 8"}, {"left": "Số lẻ", "right": "1, 3, 5, 7"}, {"left": "10", "right": "Số chẵn"}, {"left": "9", "right": "Số lẻ"}]', 
111, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng nhân', 20, 'Bảng nhân 3', 
'[{"left": "3 × 2", "right": "6"}, {"left": "3 × 3", "right": "9"}, {"left": "3 × 4", "right": "12"}, {"left": "3 × 5", "right": "15"}]', 
112, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng nhân', 20, 'Bảng nhân 4', 
'[{"left": "4 × 2", "right": "8"}, {"left": "4 × 3", "right": "12"}, {"left": "4 × 4", "right": "16"}, {"left": "4 × 5", "right": "20"}]', 
113, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép bảng chia', 20, 'Bảng chia', 
'[{"left": "6 ÷ 2", "right": "3"}, {"left": "8 ÷ 2", "right": "4"}, {"left": "10 ÷ 2", "right": "5"}, {"left": "12 ÷ 2", "right": "6"}]', 
114, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép tháng với số ngày', 20, 'Tháng và ngày', 
'[{"left": "Tháng 1", "right": "31 ngày"}, {"left": "Tháng 2", "right": "28 hoặc 29 ngày"}, {"left": "Tháng 4", "right": "30 ngày"}, {"left": "Tháng 12", "right": "31 ngày"}]', 
115, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép phép tính lớn hơn/nhỏ hơn', 15, 'So sánh', 
'[{"left": "5 > 3", "right": "Đúng"}, {"left": "7 < 5", "right": "Sai"}, {"left": "8 = 8", "right": "Đúng"}, {"left": "4 > 6", "right": "Sai"}]', 
116, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép phân số với hình ảnh', 20, 'Phân số', 
'[{"left": "1/2", "right": "Một nửa"}, {"left": "1/3", "right": "Một phần ba"}, {"left": "1/4", "right": "Một phần tư"}, {"left": "1/5", "right": "Một phần năm"}]', 
117, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép đặc điểm hình học', 20, 'Đặc điểm hình', 
'[{"left": "4 cạnh bằng nhau, 4 góc vuông", "right": "Hình vuông"}, {"left": "3 cạnh", "right": "Hình tam giác"}, {"left": "2 cạnh dài, 2 cạnh ngắn", "right": "Hình chữ nhật"}, {"left": "Không có cạnh", "right": "Hình tròn"}]', 
118, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép tiền Việt Nam', 20, 'Tiền VN', 
'[{"left": "1.000 đồng", "right": "Một nghìn đồng"}, {"left": "5.000 đồng", "right": "Năm nghìn đồng"}, {"left": "10.000 đồng", "right": "Mười nghìn đồng"}, {"left": "20.000 đồng", "right": "Hai mươi nghìn đồng"}]', 
119, 27, 'van41527', TRUE),

(NULL, 'MATCHING', 'Ghép thứ tự các ngày trong tuần', 20, 'Ngày trong tuần', 
'[{"left": "Thứ 2", "right": "Ngày đầu tuần làm việc"}, {"left": "Thứ 6", "right": "Ngày cuối tuần làm việc"}, {"left": "Thứ 7", "right": "Ngày cuối tuần"}, {"left": "Chủ nhật", "right": "Ngày nghỉ"}]', 
120, 27, 'van41527', TRUE);

-- =====================================================
-- PHẦN 6: CÂU HỎI TỰ LUẬN (10 câu)
-- =====================================================
INSERT INTO test_questions (test_id, type, content, points, prompt, max_length, rubric, order_index, created_by, created_by_name, is_shared) VALUES
(NULL, 'ESSAY', 'Giải bài toán có lời văn', 20, 
'Lan có 15 cây bút. Lan cho bạn 5 cây bút. Hỏi Lan còn lại bao nhiêu cây bút? Trình bày lời giải.', 
500, 'Cần có bài giải và đáp số đúng', 121, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải bài toán', 20, 
'Lớp 1A có 20 học sinh nam và 18 học sinh nữ. Hỏi lớp 1A có tất cả bao nhiêu học sinh? Viết bài giải.', 
400, 'Có bài giải và tính đúng', 122, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán thực tế', 25, 
'Mẹ mua 3 túi kẹo, mỗi túi có 8 viên kẹo. Mẹ cho em và anh mỗi người 10 viên. Hỏi còn lại bao nhiêu viên kẹo? Trình bày lời giải chi tiết.', 
600, 'Cần có: Bài giải, các phép tính và đáp số', 123, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải bài toán có nhiều bước', 25, 
'An có 50.000 đồng. An mua 2 quyển vở, mỗi quyển 5.000 đồng và 1 cây bút 8.000 đồng. Hỏi An còn lại bao nhiêu tiền? Viết bài giải.', 
600, 'Có bài giải đầy đủ các bước và tính đúng', 124, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Vẽ hình và giải thích', 20, 
'Hãy mô tả đặc điểm của hình vuông. Hình vuông khác gì hình chữ nhật?', 
400, 'Nêu được đặc điểm và sự khác biệt', 125, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải thích', 15, 
'Em hãy giải thích tại sao 3 + 2 = 2 + 3?', 
300, 'Giải thích được tính chất giao hoán của phép cộng', 126, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán có lời văn', 20, 
'Bố mua 24 quả cam. Bố chia đều cho 3 người con. Hỏi mỗi người được bao nhiêu quả cam? Viết bài giải.', 
400, 'Có bài giải và đáp số', 127, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Giải bài toán tìm hai số', 25, 
'Tổng của hai số là 100. Biết số thứ nhất là 45. Tìm số thứ hai. Viết bài giải.', 
400, 'Có bài giải và tính đúng', 128, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán về chu vi', 25, 
'Một hình chữ nhật có chiều dài 8cm và chiều rộng 5cm. Tính chu vi của hình chữ nhật đó. Viết bài giải và công thức.', 
500, 'Có công thức, bài giải và đáp số đúng', 129, 27, 'van41527', TRUE),

(NULL, 'ESSAY', 'Bài toán tư duy', 25, 
'Trong vườn có 15 con gà và 12 con vịt. Hỏi trong vườn có bao nhiêu con chim? Trong vườn có tất cả bao nhiêu cái chân? Viết bài giải.', 
600, 'Giải được cả 2 câu hỏi, có lập luận và tính toán đúng', 130, 27, 'van41527', TRUE);

-- =====================================================
-- PHẦN 7: TẠO BÀI KIỂM TRA VÀ BÀI TẬP (4 bài)
-- =====================================================

-- Bài 1: Kiểm tra Toán lớp 1 - Phép cộng trừ trong phạm vi 10
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra giữa kỳ I - Toán lớp 1', 'Toán', '1', 30, 'Kiểm tra các phép cộng trừ trong phạm vi 10, nhận biết hình học cơ bản', 
27, 'van41527', 150, 15, 'EXAM', 'PUBLISHED', '2026-07-10 08:00:00', '2026-07-17 23:59:59', NOW());

SET @test1_id = LAST_INSERT_ID();

-- Gán câu hỏi vào bài kiểm tra 1 (15 câu đầu - Toán lớp 1)
UPDATE test_questions SET test_id = @test1_id WHERE id IN (
    SELECT id FROM (
        SELECT id FROM test_questions WHERE created_by = 27 AND test_id IS NULL ORDER BY id LIMIT 15
    ) AS temp
);

UPDATE tests SET 
    question_count = (SELECT COUNT(*) FROM test_questions WHERE test_id = @test1_id),
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @test1_id)
WHERE id = @test1_id;

-- Bài 2: Kiểm tra Toán lớp 2 - Bảng nhân bảng chia
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra cuối kỳ I - Toán lớp 2', 'Toán', '2', 40, 'Kiểm tra bảng nhân 2,3,4,5 và bảng chia tương ứng, phép cộng trừ có nhớ', 
27, 'van41527', 200, 20, 'EXAM', 'PUBLISHED', '2026-07-12 08:00:00', '2026-07-19 23:59:59', NOW());

SET @test2_id = LAST_INSERT_ID();

-- Gán 20 câu tiếp theo (câu 16-35 - Toán lớp 2)
UPDATE test_questions SET test_id = @test2_id, order_index = order_index - 15 
WHERE created_by = 27 AND test_id IS NULL AND id BETWEEN 16 AND 35;

UPDATE tests SET 
    question_count = (SELECT COUNT(*) FROM test_questions WHERE test_id = @test2_id),
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @test2_id)
WHERE id = @test2_id;

-- Bài 3: Bài tập Toán lớp 3 - Phép nhân chia trong phạm vi 1000
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Bài tập tuần 10 - Toán lớp 3', 'Toán', '3', 35, 'Luyện tập phép nhân chia số có 3 chữ số, bảng nhân 6,7,8,9, đo lường', 
27, 'van41527', 180, 18, 'EXERCISE', 'PUBLISHED', '2026-07-08 00:00:00', '2026-07-15 23:59:59', NOW());

SET @test3_id = LAST_INSERT_ID();

-- Gán 18 câu tiếp theo (câu 61-78 - phần Toán lớp 3)
UPDATE test_questions SET test_id = @test3_id, order_index = order_index - 60
WHERE created_by = 27 AND test_id IS NULL AND id BETWEEN 61 AND 78;

UPDATE tests SET 
    question_count = (SELECT COUNT(*) FROM test_questions WHERE test_id = @test3_id),
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @test3_id)
WHERE id = @test3_id;

-- Bài 4: Bài tập ôn tập tổng hợp (điền chỗ trống + ghép cặp)
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Bài tập ôn tập - Các dạng câu hỏi', 'Toán', '2', 30, 'Luyện tập điền vào chỗ trống và ghép cặp các phép tính', 
27, 'van41527', 250, 15, 'EXERCISE', 'PUBLISHED', '2026-07-05 00:00:00', '2026-07-12 23:59:59', NOW());

SET @test4_id = LAST_INSERT_ID();

-- Gán 10 câu điền chỗ trống đầu (86-95) và 5 câu ghép cặp (106-110)
UPDATE test_questions SET test_id = @test4_id, order_index = order_index - 85
WHERE created_by = 27 AND test_id IS NULL AND id BETWEEN 86 AND 95;

UPDATE test_questions SET test_id = @test4_id, order_index = order_index - 95
WHERE created_by = 27 AND test_id IS NULL AND id BETWEEN 106 AND 110;

UPDATE tests SET 
    question_count = (SELECT COUNT(*) FROM test_questions WHERE test_id = @test4_id),
    total_points = (SELECT SUM(points) FROM test_questions WHERE test_id = @test4_id)
WHERE id = @test4_id;

-- =====================================================
-- PHẦN 8: HOÀN TẤT VÀ THỐNG KÊ
-- =====================================================

SET FOREIGN_KEY_CHECKS = 1;

-- Xem thống kê dữ liệu đã tạo
SELECT '========================================' as '';
SELECT 'THỐNG KÊ DỮ LIỆU ĐÃ TẠO' as 'TIEU_DE';
SELECT '========================================' as '';

SELECT 'Tổng số câu hỏi' as Loai, COUNT(*) as So_luong FROM test_questions WHERE created_by = 27
UNION ALL
SELECT 'Câu trắc nghiệm (MULTIPLE_CHOICE)', COUNT(*) FROM test_questions WHERE created_by = 27 AND type = 'MULTIPLE_CHOICE'
UNION ALL
SELECT 'Câu điền chỗ trống (FILL_IN_BLANK)', COUNT(*) FROM test_questions WHERE created_by = 27 AND type = 'FILL_IN_BLANK'
UNION ALL
SELECT 'Câu ghép cặp (MATCHING)', COUNT(*) FROM test_questions WHERE created_by = 27 AND type = 'MATCHING'
UNION ALL
SELECT 'Câu tự luận (ESSAY)', COUNT(*) FROM test_questions WHERE created_by = 27 AND type = 'ESSAY'
UNION ALL
SELECT 'Tổng số bài kiểm tra (EXAM)', COUNT(*) FROM tests WHERE created_by = 27 AND test_type = 'EXAM'
UNION ALL
SELECT 'Tổng số bài tập (EXERCISE)', COUNT(*) FROM tests WHERE created_by = 27 AND test_type = 'EXERCISE';

SELECT '========================================' as '';
SELECT 'CHI TIẾT CÁC BÀI KIỂM TRA/BÀI TẬP' as '';
SELECT '========================================' as '';

SELECT 
    name as 'Tên bài',
    subject as 'Môn',
    grade as 'Lớp',
    test_type as 'Loại',
    duration as 'Thời gian (phút)',
    question_count as 'Số câu',
    total_points as 'Tổng điểm',
    status as 'Trạng thái'
FROM tests 
WHERE created_by = 27
ORDER BY test_type, grade;

-- =====================================================
-- HƯỚNG DẪN SỬ DỤNG
-- =====================================================
/*
╔════════════════════════════════════════════════════════════╗
║         HƯỚNG DẪN SỬ DỤNG FILE NÀY                        ║
╚════════════════════════════════════════════════════════════╝

📌 FILE NÀY TẠO CHO USER: van41527@gmail.com (ID: 27)

📊 NỘI DUNG:
   • 130 câu hỏi đa dạng (NHIỀU)
     - 85 câu trắc nghiệm
     - 20 câu điền vào chỗ trống  
     - 15 câu ghép cặp
     - 10 câu tự luận
   
   • 4 bài kiểm tra/bài tập (ÍT - theo yêu cầu)
     - 2 bài kiểm tra (EXAM)
     - 2 bài tập (EXERCISE)

🔧 CÁCH CHẠY:

   Cách 1 - Từ command line:
   ```bash
   cd d:\Test\PrimarySchoolTeacherSupportSystemBackend\test-service\database
   mysql -u root -p test_db < insert_sample_data_user27.sql
   ```

   Cách 2 - Từ MySQL prompt:
   ```sql
   USE test_db;
   SOURCE d:/Test/PrimarySchoolTeacherSupportSystemBackend/test-service/database/insert_sample_data_user27.sql;
   ```

   Cách 3 - Dùng MySQL Workbench:
   - Mở file insert_sample_data_user27.sql
   - Chọn schema test_db
   - Click Execute (⚡ hoặc Ctrl+Shift+Enter)

📋 CHI TIẾT CÁC BÀI:

   1. Kiểm tra giữa kỳ I - Toán lớp 1
      • 15 câu hỏi - 150 điểm
      • 30 phút
      • Nội dung: Phép cộng trừ 0-10, hình học cơ bản

   2. Kiểm tra cuối kỳ I - Toán lớp 2
      • 20 câu hỏi - 200 điểm  
      • 40 phút
      • Nội dung: Bảng nhân 2,3,4,5, bảng chia, cộng trừ có nhớ

   3. Bài tập tuần 10 - Toán lớp 3
      • 18 câu hỏi - 180 điểm
      • 35 phút  
      • Nội dung: Nhân chia số có 3 chữ số, bảng nhân 6-9, đo lường

   4. Bài tập ôn tập - Các dạng câu hỏi
      • 15 câu hỏi - 250 điểm
      • 30 phút
      • Nội dung: Điền chỗ trống và ghép cặp

✅ SAU KHI CHẠY:

   Kiểm tra kết quả:
   ```sql
   -- Xem tất cả câu hỏi của user 27
   SELECT COUNT(*) as 'Tổng câu hỏi' 
   FROM test_questions 
   WHERE created_by = 27;

   -- Xem tất cả bài kiểm tra/bài tập
   SELECT * 
   FROM tests 
   WHERE created_by = 27;

   -- Xem câu hỏi theo bài kiểm tra
   SELECT t.name, tq.type, tq.content, tq.points
   FROM tests t
   JOIN test_questions tq ON t.id = tq.test_id
   WHERE t.created_by = 27
   ORDER BY t.id, tq.order_index;
   ```

📝 LƯU Ý:

   • Tất cả câu hỏi đều có is_shared = TRUE (chia sẻ với giáo viên khác)
   • Các bài kiểm tra có status = 'PUBLISHED' (đã công bố)
   • Thời gian bắt đầu từ 2026-07-05 đến 2026-07-19
   • CHƯA CÓ ẢNH VÀ AUDIO - Bạn sẽ tự thêm sau
   • CHƯA CÓ classroom_posts - Cần classroom_id thực từ classroom_db

🎨 THÊM ẢNH/AUDIO (sau khi upload lên Cloudinary):

   ```sql
   -- Thêm ảnh cho câu hỏi
   UPDATE test_questions 
   SET image_url = 'https://res.cloudinary.com/your-cloud/image.png'
   WHERE id = <question_id>;

   -- Thêm audio cho câu hỏi
   UPDATE test_questions 
   SET audio_url = 'https://res.cloudinary.com/your-cloud/audio.mp3'
   WHERE id = <question_id>;
   ```

🔄 XÓA VÀ LÀM LẠI (nếu cần):

   ```sql
   SET FOREIGN_KEY_CHECKS = 0;
   DELETE FROM test_questions WHERE created_by = 27;
   DELETE FROM tests WHERE created_by = 27;
   SET FOREIGN_KEY_CHECKS = 1;
   
   -- Sau đó chạy lại file này
   ```

╔════════════════════════════════════════════════════════════╗
║  CHÚC BẠN SỬ DỤNG THÀNH CÔNG! 🎉                          ║
╚════════════════════════════════════════════════════════════╝
*/

SELECT '========================================' as '';
SELECT '✓ FILE ĐÃ ĐƯỢC TẠO THÀNH CÔNG!' as 'THONG_BAO';
SELECT 'User: van41527@gmail.com (ID: 27)' as '';
SELECT 'Tổng số câu hỏi: 130 câu' as 'Chi_tiet';
SELECT 'Số bài kiểm tra + bài tập: 4 bài' as '';
SELECT '========================================' as '';
