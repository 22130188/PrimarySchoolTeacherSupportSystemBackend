-- Insert all categories at once
INSERT IGNORE INTO categories (type, name, code, description, grade, subject, is_active, created_by_user_id, created_at, updated_at) VALUES
  -- Grade (Khối học)
  ('grade', 'A', 'GRADE_A', 'Khối A', NULL, NULL, TRUE, NULL, NOW(), NOW()),
  ('grade', 'B', 'GRADE_B', 'Khối B', NULL, NULL, TRUE, NULL, NOW(), NOW()),
  ('grade', 'C', 'GRADE_C', 'Khối C', NULL, NULL, TRUE, NULL, NOW(), NOW()),
  -- Class (Lớp học)
  ('class', '1A', 'CLASS_1A', 'Lớp 1A', '1', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '1B', 'CLASS_1B', 'Lớp 1B', '1', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '1C', 'CLASS_1C', 'Lớp 1C', '1', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '2A', 'CLASS_2A', 'Lớp 2A', '2', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '2B', 'CLASS_2B', 'Lớp 2B', '2', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '2C', 'CLASS_2C', 'Lớp 2C', '2', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '3A', 'CLASS_3A', 'Lớp 3A', '3', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '3B', 'CLASS_3B', 'Lớp 3B', '3', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '3C', 'CLASS_3C', 'Lớp 3C', '3', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '4A', 'CLASS_4A', 'Lớp 4A', '4', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '4B', 'CLASS_4B', 'Lớp 4B', '4', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '4C', 'CLASS_4C', 'Lớp 4C', '4', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '5A', 'CLASS_5A', 'Lớp 5A', '5', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '5B', 'CLASS_5B', 'Lớp 5B', '5', NULL, TRUE, NULL, NOW(), NOW()),
  ('class', '5C', 'CLASS_5C', 'Lớp 5C', '5', NULL, TRUE, NULL, NOW(), NOW()),
  -- Subject (Môn học)
  ('subject', 'Toán', 'MATH', 'Môn Toán', NULL, NULL, TRUE, NULL, NOW(), NOW()),
  ('subject', 'Tiếng Việt', 'VIETNAMESE', 'Môn Tiếng Việt', NULL, NULL, TRUE, NULL, NOW(), NOW()),
  ('subject', 'Tiếng Anh', 'ENGLISH', 'Môn Tiếng Anh', NULL, NULL, TRUE, NULL, NOW(), NOW());
