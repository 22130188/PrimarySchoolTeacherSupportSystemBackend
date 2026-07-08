-- =====================================================
-- File: sample_data.sql
-- Description: Dữ liệu mẫu cho test-service
-- Bao gồm: Nhiều câu hỏi đa dạng, ít bài tập/kiểm tra
-- User: van41527 (ID: 27)
-- =====================================================

USE test_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- PHẦN 1: XÓA DỮ LIỆU CŨ (NẾU CẦN)
-- =====================================================
-- Uncomment các dòng dưới nếu muốn xóa dữ liệu cũ trước khi insert
-- DELETE FROM test_attempts;
-- DELETE FROM test_questions WHERE created_by = 27;
-- DELETE FROM classroom_posts WHERE author_id = 27;
-- DELETE FROM tests WHERE created_by = 27;

-- =====================================================
-- PHẦN 2: TẠO CÂU HỎI TRẮC NGHIỆM (MULTIPLE_CHOICE)
-- =====================================================

-- =====================================================
-- PHẦN 2: TẠO CÂU HỎI TRẮC NGHIỆM (MULTIPLE_CHOICE)
-- User: van41527 (ID: 27)
-- =====================================================

-- Câu hỏi Toán lớp 1 - Cơ bản
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MULTIPLE_CHOICE', '1 + 1 = ?', 10, 'Phép cộng đơn giản', 
'{"answers": ["1", "2", "3", "4"], "correctAnswer": 1}', 1, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '2 + 3 = ?', 10, 'Phép cộng trong phạm vi 10', 
'{"answers": ["3", "4", "5", "6"], "correctAnswer": 2}', 2, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '5 - 2 = ?', 10, 'Phép trừ đơn giản', 
'{"answers": ["1", "2", "3", "4"], "correctAnswer": 2}', 3, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '4 - 1 = ?', 10, 'Phép trừ cơ bản', 
'{"answers": ["2", "3", "4", "5"], "correctAnswer": 1}', 4, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Số nào lớn hơn: 7 hay 5?', 10, 'So sánh số', 
'{"answers": ["5", "7", "Bằng nhau", "Không so sánh được"], "correctAnswer": 1}', 5, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '10 - 5 = ?', 10, 'Phép trừ trong phạm vi 10', 
'{"answers": ["3", "4", "5", "6"], "correctAnswer": 2}', 6, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Hình nào có 4 góc vuông?', 10, 'Nhận biết hình học', 
'{"answers": ["Hình tròn", "Hình tam giác", "Hình vuông", "Hình bầu dục"], "correctAnswer": 2}', 7, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Số liền sau của số 8 là?', 10, 'Dãy số tự nhiên', 
'{"answers": ["7", "8", "9", "10"], "correctAnswer": 2}', 8, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '6 + 4 = ?', 10, 'Phép cộng qua 10', 
'{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 9, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '3 + 7 = ?', 10, 'Phép cộng có tổng là 10', 
'{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 10, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Hình nào có 3 cạnh?', 10, 'Hình học cơ bản', 
'{"answers": ["Hình vuông", "Hình tam giác", "Hình tròn", "Hình chữ nhật"], "correctAnswer": 1}', 11, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '8 + 2 = ?', 10, 'Phép cộng qua 10', 
'{"answers": ["9", "10", "11", "12"], "correctAnswer": 1}', 12, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Số liền trước của số 5 là?', 10, 'Số liền trước', 
'{"answers": ["3", "4", "6", "7"], "correctAnswer": 1}', 13, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '7 - 3 = ?', 10, 'Phép trừ trong phạm vi 10', 
'{"answers": ["3", "4", "5", "6"], "correctAnswer": 1}', 14, 27, 'van41527', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '9 + 1 = ?', 10, 'Phép cộng tạo số tròn chục', 
'{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 15, 27, 'van41527', TRUE, NOW());

-- Câu hỏi Toán lớp 2
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MULTIPLE_CHOICE', '15 + 23 = ?', 10, 'Phép cộng không nhớ', 
'{"answers": ["36", "37", "38", "39"], "correctAnswer": 2}', 8, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '57 - 24 = ?', 10, 'Phép trừ không nhớ', 
'{"answers": ["31", "32", "33", "34"], "correctAnswer": 2}', 9, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '2 x 5 = ?', 10, 'Bảng nhân 2', 
'{"answers": ["8", "9", "10", "11"], "correctAnswer": 2}', 10, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '10 : 2 = ?', 10, 'Bảng chia 2', 
'{"answers": ["3", "4", "5", "6"], "correctAnswer": 2}', 11, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Số nào lớn nhất?', 10, 'So sánh số có hai chữ số', 
'{"answers": ["45", "54", "44", "55"], "correctAnswer": 3}', 12, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '1 giờ có bao nhiêu phút?', 10, 'Đo thời gian', 
'{"answers": ["30 phút", "45 phút", "60 phút", "90 phút"], "correctAnswer": 2}', 13, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '34 + 23 + 12 = ?', 15, 'Phép cộng ba số', 
'{"answers": ["67", "68", "69", "70"], "correctAnswer": 2}', 14, 1, 'Cô Lan', TRUE, NOW());

-- Câu hỏi Toán lớp 3
INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MULTIPLE_CHOICE', '123 + 456 = ?', 10, 'Phép cộng trong phạm vi 1000', 
'{"answers": ["577", "578", "579", "580"], "correctAnswer": 2}', 15, 2, 'Thầy Nam', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '345 x 2 = ?', 10, 'Nhân số có ba chữ số với số có một chữ số', 
'{"answers": ["680", "685", "690", "695"], "correctAnswer": 2}', 16, 2, 'Thầy Nam', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', '144 : 12 = ?', 10, 'Phép chia hết', 
'{"answers": ["10", "11", "12", "13"], "correctAnswer": 2}', 17, 2, 'Thầy Nam', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Chu vi hình vuông cạnh 5cm là?', 15, 'Chu vi hình vuông', 
'{"answers": ["15cm", "20cm", "25cm", "30cm"], "correctAnswer": 1}', 18, 2, 'Thầy Nam', TRUE, NOW());

-- =====================================================
-- PHẦN 3: TẠO CÂU HỎI ĐIỀN VÀO CHỖ TRỐNG (FILL_IN_BLANK)
-- =====================================================

INSERT INTO test_questions (test_id, type, content, points, text_with_blanks, blanks_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp vào chỗ trống', 15, 
'5 + _____ = 10', 
'[{"index": 0, "correctAnswer": "5"}]', 19, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép tính', 15, 
'_____ - 3 = 7', 
'[{"index": 0, "correctAnswer": "10"}]', 20, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'FILL_IN_BLANK', 'Điền số vào chỗ trống', 15, 
'2 x _____ = 8', 
'[{"index": 0, "correctAnswer": "4"}]', 21, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'FILL_IN_BLANK', 'Hoàn thành bài toán', 20, 
'Lan có _____ quả táo. Lan cho bạn 3 quả. Lan còn _____ quả.', 
'[{"index": 0, "correctAnswer": "8"}, {"index": 1, "correctAnswer": "5"}]', 22, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'FILL_IN_BLANK', 'Điền đơn vị đo', 15, 
'1 mét = _____ centimet', 
'[{"index": 0, "correctAnswer": "100"}]', 23, 2, 'Thầy Nam', TRUE, NOW()),

(NULL, 'FILL_IN_BLANK', 'Hoàn thành phép chia', 15, 
'20 : _____ = 5', 
'[{"index": 0, "correctAnswer": "4"}]', 24, 2, 'Thầy Nam', TRUE, NOW()),

(NULL, 'FILL_IN_BLANK', 'Điền số thích hợp', 20, 
'_____ + 15 = 30, vậy _____ = 15', 
'[{"index": 0, "correctAnswer": "15"}, {"index": 1, "correctAnswer": "15"}]', 25, 2, 'Thầy Nam', TRUE, NOW());

-- =====================================================
-- PHẦN 4: TẠO CÂU HỎI GHÉP CẶP (MATCHING)
-- =====================================================

INSERT INTO test_questions (test_id, type, content, points, title, matching_pairs_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MATCHING', 'Nối các phép tính với kết quả phù hợp', 20, 'Ghép phép tính với đáp án', 
'[
  {"left": "2 + 3", "right": "5"},
  {"left": "4 + 1", "right": "5"},
  {"left": "6 - 1", "right": "5"},
  {"left": "10 : 2", "right": "5"}
]', 26, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MATCHING', 'Nối hình với tên gọi', 20, 'Nhận biết hình học', 
'[
  {"left": "Hình có 3 cạnh", "right": "Hình tam giác"},
  {"left": "Hình có 4 cạnh bằng nhau và 4 góc vuông", "right": "Hình vuông"},
  {"left": "Hình không có góc", "right": "Hình tròn"},
  {"left": "Hình có 4 góc vuông", "right": "Hình chữ nhật"}
]', 27, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MATCHING', 'Ghép đơn vị đo phù hợp', 20, 'Đo lường', 
'[
  {"left": "Đo chiều dài", "right": "Mét (m)"},
  {"left": "Đo khối lượng", "right": "Ki-lô-gam (kg)"},
  {"left": "Đo thời gian", "right": "Giờ (h)"},
  {"left": "Đo dung tích", "right": "Lít (l)"}
]', 28, 2, 'Thầy Nam', TRUE, NOW()),

(NULL, 'MATCHING', 'Nối phép nhân với kết quả', 15, 'Bảng nhân', 
'[
  {"left": "2 x 3", "right": "6"},
  {"left": "3 x 3", "right": "9"},
  {"left": "4 x 2", "right": "8"},
  {"left": "5 x 2", "right": "10"}
]', 29, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MATCHING', 'Ghép số với chữ', 15, 'Đọc số', 
'[
  {"left": "10", "right": "Mười"},
  {"left": "20", "right": "Hai mươi"},
  {"left": "50", "right": "Năm mươi"},
  {"left": "100", "right": "Một trăm"}
]', 30, 2, 'Thầy Nam', TRUE, NOW());

-- =====================================================
-- PHẦN 5: TẠO CÂU HỎI TỰ LUẬN (ESSAY)
-- =====================================================

INSERT INTO test_questions (test_id, type, content, points, prompt, max_length, rubric, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'ESSAY', 'Giải bài toán có lời văn', 20, 
'Lan có 15 cây bút. Lan cho bạn 5 cây bút. Hỏi Lan còn lại bao nhiêu cây bút? Trình bày lời giải.', 
500, 'Cần có bài giải và đáp số đúng', 31, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'ESSAY', 'Giải thích', 15, 
'Em hãy giải thích tại sao 3 + 2 = 2 + 3?', 
300, 'Giải thích được tính chất giao hoán của phép cộng', 32, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'ESSAY', 'Bài toán thực tế', 25, 
'Mẹ mua 3 túi kẹo, mỗi túi có 8 viên kẹo. Mẹ cho em và anh mỗi người 10 viên. Hỏi còn lại bao nhiêu viên kẹo? Trình bày lời giải chi tiết.', 
600, 'Cần có: Bài giải, các phép tính và đáp số', 33, 2, 'Thầy Nam', TRUE, NOW()),

(NULL, 'ESSAY', 'Vẽ hình và giải thích', 20, 
'Hãy mô tả đặc điểm của hình vuông. Hình vuông khác gì hình chữ nhật?', 
400, 'Nêu được đặc điểm và sự khác biệt', 34, 2, 'Thầy Nam', TRUE, NOW()),

(NULL, 'ESSAY', 'Bài toán tư duy', 20, 
'An có 20.000 đồng. An mua 1 quyển vở giá 5.000 đồng và 1 cây bút giá 3.000 đồng. Hỏi An còn lại bao nhiêu tiền? Viết bài giải.', 
500, 'Có bài giải và tính đúng', 35, 1, 'Cô Lan', TRUE, NOW());

-- =====================================================
-- PHẦN 6: TẠO THÊM CÂU HỎI ĐA DẠNG CHO NGỮ VĂN
-- =====================================================

INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MULTIPLE_CHOICE', 'Chữ "con" trong "con mèo" có nghĩa là gì?', 10, 'Từ loại', 
'{"answers": ["Danh từ", "Động từ", "Tính từ", "Lượng từ"], "correctAnswer": 3}', 36, 3, 'Cô Hương', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Câu nào sau đây là câu kể?', 10, 'Loại câu', 
'{"answers": ["Em có khỏe không?", "Hãy làm bài tập!", "Hôm nay trời đẹp quá!", "Hôm nay là thứ hai."], "correctAnswer": 3}', 37, 3, 'Cô Hương', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Từ nào sau đây chỉ màu sắc?', 10, 'Từ vựng', 
'{"answers": ["Nhanh", "Xanh", "Cao", "To"], "correctAnswer": 1}', 38, 3, 'Cô Hương', TRUE, NOW());

INSERT INTO test_questions (test_id, type, content, points, text_with_blanks, blanks_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'FILL_IN_BLANK', 'Điền từ thích hợp', 15, 
'Con _____ kêu ó o. Con _____ kêu meo meo.', 
'[{"index": 0, "correctAnswer": "gà"}, {"index": 1, "correctAnswer": "mèo"}]', 39, 3, 'Cô Hương', TRUE, NOW()),

(NULL, 'FILL_IN_BLANK', 'Hoàn thành câu', 15, 
'Mặt trời mọc ở hướng _____ và lặn ở hướng _____.', 
'[{"index": 0, "correctAnswer": "đông"}, {"index": 1, "correctAnswer": "tây"}]', 40, 3, 'Cô Hương', TRUE, NOW());

INSERT INTO test_questions (test_id, type, content, points, prompt, max_length, rubric, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'ESSAY', 'Viết đoạn văn ngắn', 25, 
'Em hãy viết 5-7 câu về gia đình em.', 
600, 'Có đủ 5 câu, viết đúng chính tả', 41, 3, 'Cô Hương', TRUE, NOW()),

(NULL, 'ESSAY', 'Kể chuyện', 20, 
'Em hãy kể về một kỉ niệm vui trong lớp học.', 
500, 'Kể được câu chuyện có đầu, giữa, cuối', 42, 3, 'Cô Hương', TRUE, NOW());

-- =====================================================
-- PHẦN 7: TẠO CÂU HỎI TIẾNG ANH LỚP 1-3
-- =====================================================

INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MULTIPLE_CHOICE', 'What color is the sky?', 10, 'Colors - Màu sắc', 
'{"answers": ["Red", "Blue", "Green", "Yellow"], "correctAnswer": 1}', 43, 4, 'Teacher Sarah', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'How many legs does a dog have?', 10, 'Numbers - Con số', 
'{"answers": ["Two", "Three", "Four", "Five"], "correctAnswer": 2}', 44, 4, 'Teacher Sarah', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'What is this? 🍎', 10, 'Fruits - Trái cây', 
'{"answers": ["Banana", "Apple", "Orange", "Grape"], "correctAnswer": 1}', 45, 4, 'Teacher Sarah', TRUE, NOW());

INSERT INTO test_questions (test_id, type, content, points, text_with_blanks, blanks_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'FILL_IN_BLANK', 'Complete the sentence', 15, 
'My name _____ Lan. I _____ seven years old.', 
'[{"index": 0, "correctAnswer": "is"}, {"index": 1, "correctAnswer": "am"}]', 46, 4, 'Teacher Sarah', TRUE, NOW()),

(NULL, 'FILL_IN_BLANK', 'Fill in the blanks with numbers', 15, 
'One, two, _____, four, _____.', 
'[{"index": 0, "correctAnswer": "three"}, {"index": 1, "correctAnswer": "five"}]', 47, 4, 'Teacher Sarah', TRUE, NOW());

INSERT INTO test_questions (test_id, type, content, points, title, matching_pairs_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MATCHING', 'Match the English word with Vietnamese', 15, 'Vocabulary matching', 
'[
  {"left": "Cat", "right": "Con mèo"},
  {"left": "Dog", "right": "Con chó"},
  {"left": "Book", "right": "Quyển sách"},
  {"left": "Pen", "right": "Cây bút"}
]', 48, 4, 'Teacher Sarah', TRUE, NOW());

-- =====================================================
-- PHẦN 8: TẠO CÂU HỎI KHOA HỌC TỰ NHIÊN
-- =====================================================

INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MULTIPLE_CHOICE', 'Con vật nào sống dưới nước?', 10, 'Môi trường sống', 
'{"answers": ["Con chó", "Con mèo", "Con cá", "Con gà"], "correctAnswer": 2}', 49, 5, 'Cô Thảo', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Cây cần gì để sống?', 10, 'Nhu cầu của cây', 
'{"answers": ["Chỉ cần nước", "Chỉ cần ánh sáng", "Nước, ánh sáng và không khí", "Chỉ cần đất"], "correctAnswer": 2}', 50, 5, 'Cô Thảo', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Ngày có bao nhiêu giờ?', 10, 'Thời gian', 
'{"answers": ["12 giờ", "24 giờ", "30 giờ", "48 giờ"], "correctAnswer": 1}', 51, 5, 'Cô Thảo', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Mùa nào sau đây là mùa lạnh?', 10, 'Các mùa trong năm', 
'{"answers": ["Mùa xuân", "Mùa hè", "Mùa thu", "Mùa đông"], "correctAnswer": 3}', 52, 5, 'Cô Thảo', TRUE, NOW());

INSERT INTO test_questions (test_id, type, content, points, title, matching_pairs_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MATCHING', 'Ghép con vật với nơi ở', 20, 'Môi trường sống', 
'[
  {"left": "Con cá", "right": "Sống trong nước"},
  {"left": "Con chim", "right": "Sống trên cây"},
  {"left": "Con thỏ", "right": "Sống trong hang"},
  {"left": "Con bò", "right": "Sống trong chuồng"}
]', 53, 5, 'Cô Thảo', TRUE, NOW());

INSERT INTO test_questions (test_id, type, content, points, prompt, max_length, rubric, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'ESSAY', 'Quan sát và mô tả', 20, 
'Em hãy mô tả thời tiết hôm nay. Trời có nắng không? Có mưa không? Gió có mạnh không?', 
400, 'Mô tả được ít nhất 3 yếu tố thời tiết', 54, 5, 'Cô Thảo', TRUE, NOW());

-- =====================================================
-- PHẦN 9: TẠO CÂU HỎI ÂM NHẠC VÀ MỸ THUẬT
-- =====================================================

INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MULTIPLE_CHOICE', 'Màu đỏ trộn với màu vàng sẽ được màu gì?', 10, 'Pha màu', 
'{"answers": ["Màu xanh lá", "Màu cam", "Màu tím", "Màu xanh dương"], "correctAnswer": 1}', 55, 6, 'Cô Mai', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Nhạc cụ nào có dây đàn?', 10, 'Nhạc cụ', 
'{"answers": ["Trống", "Đàn guitar", "Kèn", "Sáo"], "correctAnswer": 1}', 56, 6, 'Cô Mai', TRUE, NOW());

INSERT INTO test_questions (test_id, type, content, points, prompt, max_length, rubric, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'ESSAY', 'Cảm nhận về bức tranh', 20, 
'Em hãy mô tả một bức tranh về thiên nhiên mà em thích. Trong tranh có những gì?', 
400, 'Mô tả được các yếu tố trong bức tranh', 57, 6, 'Cô Mai', TRUE, NOW());

-- =====================================================
-- PHẦN 10: TẠO CÂU HỎI ĐẠO ĐỨC
-- =====================================================

INSERT INTO test_questions (test_id, type, content, points, title, answers_json, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'MULTIPLE_CHOICE', 'Khi gặp thầy cô, em nên làm gì?', 10, 'Phép lịch sự', 
'{"answers": ["Chào thầy cô", "Im lặng", "Chạy đi", "La hét"], "correctAnswer": 0}', 58, 1, 'Cô Lan', TRUE, NOW()),

(NULL, 'MULTIPLE_CHOICE', 'Bạn làm gì khi thấy bạn ngã?', 10, 'Hành vi tốt', 
'{"answers": ["Cười bạn", "Giúp bạn đứng dậy", "Làm ngơ", "Chạy đi"], "correctAnswer": 1}', 59, 1, 'Cô Lan', TRUE, NOW());

INSERT INTO test_questions (test_id, type, content, points, prompt, max_length, rubric, order_index, created_by, created_by_name, is_shared, created_at) 
VALUES 
(NULL, 'ESSAY', 'Suy nghĩ về hành động', 20, 
'Em hãy kể về một lần em đã giúp đỡ bạn. Em cảm thấy thế nào khi giúp bạn?', 
500, 'Kể được câu chuyện và cảm nhận', 60, 1, 'Cô Lan', TRUE, NOW());

-- =====================================================
-- PHẦN 11: TẠO BÀI KIỂM TRA VÀ BÀI TẬP
-- =====================================================

-- Bài kiểm tra 1: Toán lớp 1 - Phép cộng trừ trong phạm vi 10
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra giữa kỳ I - Toán', 'Toán', '1', 30, 'Kiểm tra các phép tính cơ bản và nhận biết hình học', 
1, 'Cô Lan', 100, 0, 'EXAM', 'PUBLISHED', '2026-07-05 08:00:00', '2026-07-12 23:59:59', NOW());

SET @test1_id = LAST_INSERT_ID();

-- Thêm câu hỏi vào bài kiểm tra 1
UPDATE test_questions SET test_id = @test1_id, order_index = 1 WHERE id = 1;
UPDATE test_questions SET test_id = @test1_id, order_index = 2 WHERE id = 2;
UPDATE test_questions SET test_id = @test1_id, order_index = 3 WHERE id = 3;
UPDATE test_questions SET test_id = @test1_id, order_index = 4 WHERE id = 4;
UPDATE test_questions SET test_id = @test1_id, order_index = 5 WHERE id = 5;
UPDATE test_questions SET test_id = @test1_id, order_index = 6 WHERE id = 19;
UPDATE test_questions SET test_id = @test1_id, order_index = 7 WHERE id = 20;
UPDATE test_questions SET test_id = @test1_id, order_index = 8 WHERE id = 26;
UPDATE test_questions SET test_id = @test1_id, order_index = 9 WHERE id = 31;
UPDATE test_questions SET test_id = @test1_id, order_index = 10 WHERE id = 58;

-- Cập nhật số câu hỏi và tổng điểm
UPDATE tests SET question_count = 10, total_points = 125 WHERE id = @test1_id;

-- Bài tập 1: Toán lớp 1 - Luyện tập phép cộng
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Bài tập về nhà - Phép cộng', 'Toán', '1', 20, 'Luyện tập các phép cộng đơn giản', 
1, 'Cô Lan', 70, 0, 'EXERCISE', 'PUBLISHED', '2026-07-04 00:00:00', '2026-07-10 23:59:59', NOW());

SET @exercise1_id = LAST_INSERT_ID();

-- Thêm câu hỏi vào bài tập 1
UPDATE test_questions SET test_id = @exercise1_id, order_index = 1 WHERE id = 6;
UPDATE test_questions SET test_id = @exercise1_id, order_index = 2 WHERE id = 7;
UPDATE test_questions SET test_id = @exercise1_id, order_index = 3 WHERE id = 21;
UPDATE test_questions SET test_id = @exercise1_id, order_index = 4 WHERE id = 27;

UPDATE tests SET question_count = 4, total_points = 55 WHERE id = @exercise1_id;

-- Bài kiểm tra 2: Toán lớp 2 - Phép nhân và phép chia
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra cuối kỳ I - Toán', 'Toán', '2', 40, 'Kiểm tra bảng nhân, bảng chia và các phép tính trong phạm vi 100', 
1, 'Cô Lan', 150, 0, 'EXAM', 'PUBLISHED', '2026-07-08 08:00:00', '2026-07-15 23:59:59', NOW());

SET @test2_id = LAST_INSERT_ID();

UPDATE test_questions SET test_id = @test2_id, order_index = 1 WHERE id = 8;
UPDATE test_questions SET test_id = @test2_id, order_index = 2 WHERE id = 9;
UPDATE test_questions SET test_id = @test2_id, order_index = 3 WHERE id = 10;
UPDATE test_questions SET test_id = @test2_id, order_index = 4 WHERE id = 11;
UPDATE test_questions SET test_id = @test2_id, order_index = 5 WHERE id = 12;
UPDATE test_questions SET test_id = @test2_id, order_index = 6 WHERE id = 13;
UPDATE test_questions SET test_id = @test2_id, order_index = 7 WHERE id = 14;
UPDATE test_questions SET test_id = @test2_id, order_index = 8 WHERE id = 22;
UPDATE test_questions SET test_id = @test2_id, order_index = 9 WHERE id = 28;
UPDATE test_questions SET test_id = @test2_id, order_index = 10 WHERE id = 29;
UPDATE test_questions SET test_id = @test2_id, order_index = 11 WHERE id = 33;

UPDATE tests SET question_count = 11, total_points = 160 WHERE id = @test2_id;

-- Bài tập 2: Toán lớp 2 - Bảng nhân 2 và 5
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Bài tập tuần 5 - Bảng nhân', 'Toán', '2', 15, 'Luyện tập bảng nhân 2 và 5', 
1, 'Cô Lan', 50, 0, 'EXERCISE', 'PUBLISHED', '2026-07-03 00:00:00', '2026-07-09 23:59:59', NOW());

SET @exercise2_id = LAST_INSERT_ID();

UPDATE test_questions SET test_id = @exercise2_id, order_index = 1 WHERE id = 23;
UPDATE test_questions SET test_id = @exercise2_id, order_index = 2 WHERE id = 24;
UPDATE test_questions SET test_id = @exercise2_id, order_index = 3 WHERE id = 30;

UPDATE tests SET question_count = 3, total_points = 45 WHERE id = @exercise2_id;

-- Bài kiểm tra 3: Toán lớp 3 - Phép nhân và chia trong phạm vi 1000
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra 15 phút - Chương 2', 'Toán', '3', 15, 'Kiểm tra phép nhân, chia và bài toán có lời văn', 
2, 'Thầy Nam', 80, 0, 'EXAM', 'PUBLISHED', '2026-07-06 09:00:00', '2026-07-13 23:59:59', NOW());

SET @test3_id = LAST_INSERT_ID();

UPDATE test_questions SET test_id = @test3_id, order_index = 1 WHERE id = 15;
UPDATE test_questions SET test_id = @test3_id, order_index = 2 WHERE id = 16;
UPDATE test_questions SET test_id = @test3_id, order_index = 3 WHERE id = 17;
UPDATE test_questions SET test_id = @test3_id, order_index = 4 WHERE id = 18;
UPDATE test_questions SET test_id = @test3_id, order_index = 5 WHERE id = 25;
UPDATE test_questions SET test_id = @test3_id, order_index = 6 WHERE id = 34;

UPDATE tests SET question_count = 6, total_points = 95 WHERE id = @test3_id;

-- Bài tập 3: Toán lớp 3 - Luyện tập tổng hợp
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Bài tập ôn tập - Chương 1 & 2', 'Toán', '3', 25, 'Ôn tập các kiến thức đã học', 
2, 'Thầy Nam', 90, 0, 'EXERCISE', 'PUBLISHED', '2026-07-02 00:00:00', '2026-07-08 23:59:59', NOW());

SET @exercise3_id = LAST_INSERT_ID();

UPDATE test_questions SET test_id = @exercise3_id, order_index = 1 WHERE id = 32;
UPDATE test_questions SET test_id = @exercise3_id, order_index = 2 WHERE id = 35;

UPDATE tests SET question_count = 2, total_points = 40 WHERE id = @exercise3_id;

-- Bài kiểm tra 4: Ngữ văn lớp 2
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra giữa kỳ - Ngữ văn', 'Ngữ văn', '2', 35, 'Kiểm tra từ vựng, ngữ pháp và viết đoạn văn', 
3, 'Cô Hương', 110, 0, 'EXAM', 'PUBLISHED', '2026-07-07 08:00:00', '2026-07-14 23:59:59', NOW());

SET @test4_id = LAST_INSERT_ID();

UPDATE test_questions SET test_id = @test4_id, order_index = 1 WHERE id = 36;
UPDATE test_questions SET test_id = @test4_id, order_index = 2 WHERE id = 37;
UPDATE test_questions SET test_id = @test4_id, order_index = 3 WHERE id = 38;
UPDATE test_questions SET test_id = @test4_id, order_index = 4 WHERE id = 39;
UPDATE test_questions SET test_id = @test4_id, order_index = 5 WHERE id = 40;
UPDATE test_questions SET test_id = @test4_id, order_index = 6 WHERE id = 41;
UPDATE test_questions SET test_id = @test4_id, order_index = 7 WHERE id = 42;

UPDATE tests SET question_count = 7, total_points = 115 WHERE id = @test4_id;

-- Bài tập 4: Tiếng Anh lớp 2
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Unit 1 - Homework', 'Tiếng Anh', '2', 20, 'Practice colors, numbers and vocabulary', 
4, 'Teacher Sarah', 70, 0, 'EXERCISE', 'PUBLISHED', '2026-07-01 00:00:00', '2026-07-07 23:59:59', NOW());

SET @exercise4_id = LAST_INSERT_ID();

UPDATE test_questions SET test_id = @exercise4_id, order_index = 1 WHERE id = 43;
UPDATE test_questions SET test_id = @exercise4_id, order_index = 2 WHERE id = 44;
UPDATE test_questions SET test_id = @exercise4_id, order_index = 3 WHERE id = 45;
UPDATE test_questions SET test_id = @exercise4_id, order_index = 4 WHERE id = 46;
UPDATE test_questions SET test_id = @exercise4_id, order_index = 5 WHERE id = 47;
UPDATE test_questions SET test_id = @exercise4_id, order_index = 6 WHERE id = 48;

UPDATE tests SET question_count = 6, total_points = 75 WHERE id = @exercise4_id;

-- Bài kiểm tra 5: Khoa học lớp 1
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Kiểm tra cuối học kỳ - Khoa học', 'Khoa học', '1', 30, 'Kiểm tra về môi trường sống và thời tiết', 
5, 'Cô Thảo', 80, 0, 'EXAM', 'PUBLISHED', '2026-07-09 08:00:00', '2026-07-16 23:59:59', NOW());

SET @test5_id = LAST_INSERT_ID();

UPDATE test_questions SET test_id = @test5_id, order_index = 1 WHERE id = 49;
UPDATE test_questions SET test_id = @test5_id, order_index = 2 WHERE id = 50;
UPDATE test_questions SET test_id = @test5_id, order_index = 3 WHERE id = 51;
UPDATE test_questions SET test_id = @test5_id, order_index = 4 WHERE id = 52;
UPDATE test_questions SET test_id = @test5_id, order_index = 5 WHERE id = 53;
UPDATE test_questions SET test_id = @test5_id, order_index = 6 WHERE id = 54;

UPDATE tests SET question_count = 6, total_points = 80 WHERE id = @test5_id;

-- Bài tập 5: Mỹ thuật và Đạo đức lớp 1
INSERT INTO tests (name, subject, grade, duration, description, created_by, created_by_name, total_points, question_count, test_type, status, start_at, end_at, created_at)
VALUES 
('Bài tập - Đạo đức và Mỹ thuật', 'Đạo đức', '1', 15, 'Bài tập về phép lịch sự và nhận thức màu sắc', 
1, 'Cô Lan', 60, 0, 'EXERCISE', 'PUBLISHED', '2026-06-30 00:00:00', '2026-07-06 23:59:59', NOW());

SET @exercise5_id = LAST_INSERT_ID();

UPDATE test_questions SET test_id = @exercise5_id, order_index = 1 WHERE id = 55;
UPDATE test_questions SET test_id = @exercise5_id, order_index = 2 WHERE id = 56;
UPDATE test_questions SET test_id = @exercise5_id, order_index = 3 WHERE id = 57;
UPDATE test_questions SET test_id = @exercise5_id, order_index = 4 WHERE id = 59;
UPDATE test_questions SET test_id = @exercise5_id, order_index = 5 WHERE id = 60;

UPDATE tests SET question_count = 5, total_points = 70 WHERE id = @exercise5_id;

-- =====================================================
-- PHẦN 12: TẠO MỘT SỐ BÀI NỘP MẪU (test_attempts)
-- =====================================================

-- Giả sử có học sinh với ID 101, 102, 103 từ user-service
-- Bài nộp cho bài tập 1 (Toán lớp 1)
INSERT INTO test_attempts (test_id, user_id, user_name, started_at, submitted_at, duration_seconds, duration_minutes, score, max_score, status, is_submitted, answers_json, created_at)
VALUES 
(@exercise1_id, 101, 'Nguyễn Văn An', '2026-07-04 14:30:00', '2026-07-04 14:45:00', 900, 15, 50, 55, 'GRADED', 1, 
'{"answers": [{"questionId": 6, "answer": "9"}, {"questionId": 7, "answer": "10"}, {"questionId": 21, "answer": "4"}, {"questionId": 27, "answer": "partial"}]}', 
NOW()),

(@exercise1_id, 102, 'Trần Thị Bình', '2026-07-05 16:00:00', '2026-07-05 16:18:00', 1080, 18, 55, 55, 'GRADED', 1, 
'{"answers": [{"questionId": 6, "answer": "9"}, {"questionId": 7, "answer": "10"}, {"questionId": 21, "answer": "4"}, {"questionId": 27, "answer": "correct"}]}', 
NOW());

-- Bài nộp cho bài kiểm tra 1 (Toán lớp 1)
INSERT INTO test_attempts (test_id, user_id, user_name, started_at, submitted_at, duration_seconds, duration_minutes, score, max_score, status, is_submitted, answers_json, created_at)
VALUES 
(@test1_id, 101, 'Nguyễn Văn An', '2026-07-05 08:30:00', '2026-07-05 08:55:00', 1500, 25, 110, 125, 'GRADED', 1, 
'{"answers": [{"questionId": 1, "answer": "5"}, {"questionId": 2, "answer": "3"}, {"questionId": 3, "answer": "7"}]}', 
NOW()),

(@test1_id, 103, 'Lê Văn Cường', '2026-07-05 09:00:00', '2026-07-05 09:28:00', 1680, 28, 95, 125, 'GRADED', 1, 
'{"answers": [{"questionId": 1, "answer": "5"}, {"questionId": 2, "answer": "2"}, {"questionId": 3, "answer": "7"}]}', 
NOW());

-- =====================================================
-- PHẦN 13: TẠO DỮ LIỆU CLASSROOM_POSTS
-- =====================================================
-- Giả sử có lớp học với ID 1, 2, 3 từ classroom-service

INSERT INTO classroom_posts (external_post_id, classroom_id, classroom_code, author_id, author_name, teacher_name, post_type, title, content, attempt_limit, question_count, max_points, start_at, duration_minutes, reference_test_id, reference_test_name, created_at)
VALUES 
(1001, 1, 'LOP1A01', 1, 'Cô Lan', 'Cô Lan', 'TEST', 'Kiểm tra giữa kỳ I - Toán', 
'Các em làm bài cẩn thận, kiểm tra kỹ trước khi nộp bài.', 
1, 10, 125, '2026-07-05 08:00:00', 30, @test1_id, 'Kiểm tra giữa kỳ I - Toán', NOW()),

(1002, 1, 'LOP1A01', 1, 'Cô Lan', 'Cô Lan', 'ASSIGNMENT', 'Bài tập về nhà - Phép cộng', 
'Bài tập tuần này về phép cộng đơn giản. Các em làm và nộp trước thứ 6.', 
2, 4, 55, '2026-07-04 00:00:00', 20, @exercise1_id, 'Bài tập về nhà - Phép cộng', NOW()),

(1003, 2, 'LOP2A01', 1, 'Cô Lan', 'Cô Lan', 'TEST', 'Kiểm tra cuối kỳ I - Toán', 
'Bài kiểm tra tổng hợp kiến thức học kỳ 1. Thời gian làm bài 40 phút.', 
1, 11, 160, '2026-07-08 08:00:00', 40, @test2_id, 'Kiểm tra cuối kỳ I - Toán', NOW()),

(1004, 2, 'LOP2A01', 1, 'Cô Lan', 'Cô Lan', 'ASSIGNMENT', 'Bài tập tuần 5 - Bảng nhân', 
'Luyện tập bảng nhân 2 và 5. Nộp bài trước thứ 4.', 
3, 3, 45, '2026-07-03 00:00:00', 15, @exercise2_id, 'Bài tập tuần 5 - Bảng nhân', NOW()),

(1005, 3, 'LOP3A01', 2, 'Thầy Nam', 'Thầy Nam', 'TEST', 'Kiểm tra 15 phút - Chương 2', 
'Kiểm tra nhanh về phép nhân và chia. Các em chuẩn bị tốt nhé!', 
1, 6, 95, '2026-07-06 09:00:00', 15, @test3_id, 'Kiểm tra 15 phút - Chương 2', NOW()),

(1006, 3, 'LOP3A01', 2, 'Thầy Nam', 'Thầy Nam', 'ASSIGNMENT', 'Bài tập ôn tập - Chương 1 & 2', 
'Ôn tập các kiến thức đã học ở chương 1 và 2.', 
2, 2, 40, '2026-07-02 00:00:00', 25, @exercise3_id, 'Bài tập ôn tập - Chương 1 & 2', NOW()),

(1007, 2, 'LOP2B01', 3, 'Cô Hương', 'Cô Hương', 'TEST', 'Kiểm tra giữa kỳ - Ngữ văn', 
'Kiểm tra về từ vựng, ngữ pháp và khả năng viết đoạn văn.', 
1, 7, 115, '2026-07-07 08:00:00', 35, @test4_id, 'Kiểm tra giữa kỳ - Ngữ văn', NOW()),

(1008, 2, 'LOP2C01', 4, 'Teacher Sarah', 'Teacher Sarah', 'ASSIGNMENT', 'Unit 1 - Homework', 
'Practice colors, numbers and basic vocabulary. Submit before Friday.', 
2, 6, 75, '2026-07-01 00:00:00', 20, @exercise4_id, 'Unit 1 - Homework', NOW()),

(1009, 1, 'LOP1B01', 5, 'Cô Thảo', 'Cô Thảo', 'TEST', 'Kiểm tra cuối học kỳ - Khoa học', 
'Kiểm tra tổng hợp về môi trường sống và thời tiết.', 
1, 6, 80, '2026-07-09 08:00:00', 30, @test5_id, 'Kiểm tra cuối học kỳ - Khoa học', NOW()),

(1010, 1, 'LOP1A01', 1, 'Cô Lan', 'Cô Lan', 'ASSIGNMENT', 'Bài tập - Đạo đức và Mỹ thuật', 
'Bài tập về phép lịch sự và nhận thức màu sắc. Các em làm vui vẻ nhé!', 
2, 5, 70, '2026-06-30 00:00:00', 15, @exercise5_id, 'Bài tập - Đạo đức và Mỹ thuật', NOW());

-- =====================================================
-- PHẦN 14: HOÀN TẤT VÀ KIỂM TRA
-- =====================================================

SET FOREIGN_KEY_CHECKS = 1;

-- Xem thống kê dữ liệu đã tạo
SELECT 'Tổng số câu hỏi đã tạo:' as Thong_ke, COUNT(*) as So_luong FROM test_questions
UNION ALL
SELECT 'Câu hỏi trắc nghiệm:', COUNT(*) FROM test_questions WHERE type = 'MULTIPLE_CHOICE'
UNION ALL
SELECT 'Câu hỏi điền vào chỗ trống:', COUNT(*) FROM test_questions WHERE type = 'FILL_IN_BLANK'
UNION ALL
SELECT 'Câu hỏi ghép cặp:', COUNT(*) FROM test_questions WHERE type = 'MATCHING'
UNION ALL
SELECT 'Câu hỏi tự luận:', COUNT(*) FROM test_questions WHERE type = 'ESSAY'
UNION ALL
SELECT 'Tổng số bài kiểm tra:', COUNT(*) FROM tests WHERE test_type = 'EXAM'
UNION ALL
SELECT 'Tổng số bài tập:', COUNT(*) FROM tests WHERE test_type = 'EXERCISE'
UNION ALL
SELECT 'Tổng số bài nộp mẫu:', COUNT(*) FROM test_attempts
UNION ALL
SELECT 'Tổng số bài đăng lớp học:', COUNT(*) FROM classroom_posts;

-- =====================================================
-- HƯỚNG DẪN SỬ DỤNG
-- =====================================================
/*
CÁCH SỬ DỤNG FILE NÀY:

1. Kết nối MySQL/MariaDB:
   mysql -u root -p

2. Chạy file SQL này:
   SOURCE d:/Test/PrimarySchoolTeacherSupportSystemBackend/test-service/database/sample_data.sql;
   
   Hoặc từ command line:
   mysql -u root -p test_db < sample_data.sql

3. Kiểm tra dữ liệu:
   USE test_db;
   SELECT * FROM tests;
   SELECT * FROM test_questions WHERE test_id IS NOT NULL;
   SELECT * FROM test_attempts;
   SELECT * FROM classroom_posts;

4. Lưu ý:
   - File này tạo 60 câu hỏi đa dạng
   - 5 bài kiểm tra (EXAM) cho các lớp 1, 2, 3
   - 5 bài tập (EXERCISE) 
   - 4 bài nộp mẫu (test_attempts)
   - 10 bài đăng lớp học (classroom_posts)
   
5. Dữ liệu giả định:
   - User IDs: 1 (Cô Lan), 2 (Thầy Nam), 3 (Cô Hương), 4 (Teacher Sarah), 5 (Cô Thảo)
   - Student IDs: 101, 102, 103
   - Classroom IDs: 1, 2, 3
   - Các ID thực tế cần được lấy từ user-service và classroom-service

6. Tùy chỉnh:
   - Bạn có thể thay đổi start_at, end_at cho phù hợp
   - Có thể thêm ảnh (image_url) và audio (audio_url) bằng cách UPDATE sau
   - Điều chỉnh điểm số (points) và thời gian (duration) theo nhu cầu
*/

-- =====================================================
-- KẾT THÚC FILE
-- =====================================================
SELECT '========================================' as '';
SELECT 'DỮ LIỆU MẪU ĐÃ ĐƯỢC TẠO THÀNH CÔNG!' as 'THONG_BAO';
SELECT '========================================' as '';
SELECT 'Tổng số câu hỏi: 60' as 'Chi_tiet';
SELECT 'Bài kiểm tra (EXAM): 5' as '';
SELECT 'Bài tập (EXERCISE): 5' as '';
SELECT 'Bài nộp mẫu: 4' as '';
SELECT 'Bài đăng lớp học: 10' as '';
SELECT '========================================' as '';
