-- =====================================================
-- Database: Test Service Database
-- Description: Database for Test Management Service
-- =====================================================

-- Create database
CREATE DATABASE IF NOT EXISTS test_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE test_db;

-- =====================================================
-- Table: tests
-- Description: Store test information and metadata
-- =====================================================
CREATE TABLE IF NOT EXISTS tests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  
  -- Test basic information
  name VARCHAR(255) NOT NULL COMMENT 'Test name',
  subject VARCHAR(255) NOT NULL COMMENT 'Subject name',
  grade VARCHAR(50) COMMENT 'Grade/Class level (e.g., 6A, 10B)',
  duration INT NOT NULL COMMENT 'Duration in minutes',
  description TEXT COMMENT 'Test description',
  
  -- User information
  created_by BIGINT NOT NULL COMMENT 'ID of user who created the test',
  created_by_name VARCHAR(255) NOT NULL COMMENT 'Name of user who created the test',
  
  -- File storage
  docx_file_url VARCHAR(500) COMMENT 'URL of DOCX file from Cloudinary',
  cloudinary_public_id VARCHAR(255) COMMENT 'Public ID from Cloudinary',
  
  -- Statistics
  total_points INT DEFAULT 0 COMMENT 'Total points for the test',
  question_count INT DEFAULT 0 COMMENT 'Number of questions',
  test_type ENUM('EXAM', 'EXERCISE') NOT NULL DEFAULT 'EXAM' COMMENT 'Loại bài: EXAM = Bài kiểm tra, EXERCISE = Bài tập',
  
  -- Status management
  status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT 'Test status',
  
  -- Start and end times
  start_at DATETIME COMMENT 'Thời gian bắt đầu làm bài (students can only take from this time)',
  end_at DATETIME COMMENT 'Thời gian kết thúc làm bài',

  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Test creation time',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  
  -- Indexes for better query performance
  INDEX idx_created_by (created_by),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at),
  INDEX idx_test_name (name)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='Table storing test information';

-- =====================================================
-- Table: classroom_posts
-- Description: Store classroom stream items and metadata in test_db
-- =====================================================
CREATE TABLE IF NOT EXISTS classroom_posts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  external_post_id BIGINT COMMENT 'ID bài đăng trong classroom-service',
  classroom_id BIGINT NOT NULL COMMENT 'ID lớp học',
  classroom_code VARCHAR(100) COMMENT 'Mã lớp học',
  author_id BIGINT NOT NULL COMMENT 'ID người tạo bài đăng',
  author_name VARCHAR(255) NOT NULL COMMENT 'Tên người tạo bài đăng',
  teacher_name VARCHAR(255) COMMENT 'Tên giáo viên quản lý lớp',
  post_type ENUM('ANNOUNCEMENT', 'ASSIGNMENT', 'TEST') NOT NULL DEFAULT 'ANNOUNCEMENT' COMMENT 'Loại bài đăng',
  title VARCHAR(500) COMMENT 'Tiêu đề bài đăng',
  content TEXT COMMENT 'Nội dung bài đăng',
  attempt_limit INT COMMENT 'Số lần làm bài',
  question_count INT COMMENT 'Số câu hỏi',
  max_points INT COMMENT 'Điểm tối đa',
  start_at DATETIME COMMENT 'Thời gian bắt đầu bài tập/bài kiểm tra',
  duration_minutes INT COMMENT 'Thời lượng (phút)',
  reference_test_id BIGINT COMMENT 'ID bài kiểm tra tham chiếu',
  reference_test_name VARCHAR(255) COMMENT 'Tên bài kiểm tra tham chiếu',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo bài đăng',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật cuối cùng',
  INDEX idx_classroom_id (classroom_id),
  INDEX idx_external_post_id (external_post_id),
  INDEX idx_author_id (author_id),
  INDEX idx_created_at (created_at),
  INDEX idx_post_type (post_type)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='Lưu dữ liệu bài đăng lớp học để đồng bộ với classroom-service';

-- =====================================================
-- Table: test_questions
-- Description: Store individual questions for tests
-- =====================================================
CREATE TABLE IF NOT EXISTS test_questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  
  -- Relationship to test
  test_id BIGINT COMMENT 'Reference to test; nullable for standalone teacher questions',
  
  -- Question type
  type VARCHAR(50) NOT NULL COMMENT 'Type of question',

  -- Question content
  content LONGTEXT NOT NULL COMMENT 'Question content/text',
  points INT NOT NULL COMMENT 'Points for this question',
  
  -- Multiple choice specific
  title VARCHAR(255) COMMENT 'Question title',
  number_questions INT COMMENT 'Number of questions (for grouped questions)',
  answers_json JSON COMMENT 'JSON array of answers and correct answer',
  
  -- Matching question specific
  matching_pairs_json LONGTEXT COMMENT 'JSON array of matching pairs (supports unlimited pairs)',
  
  -- Fill in blank question specific
  text_with_blanks LONGTEXT COMMENT 'Text containing blank markers',
  blanks_json JSON COMMENT 'JSON array of blank answers',
  
  -- Essay question specific
  prompt TEXT COMMENT 'Essay prompt',
  max_length INT COMMENT 'Maximum answer length',
  rubric VARCHAR(500) COMMENT 'Scoring rubric or notes',
  
  -- Audio question specific
  audio_url VARCHAR(500) COMMENT 'URL of audio file',
  transcript TEXT COMMENT 'Transcript of the audio',
  
  -- Ordering
  order_index INT NOT NULL DEFAULT 0 COMMENT 'Order of question in test',
  
  -- Sharing and ownership
  created_by BIGINT NOT NULL COMMENT 'ID of user who created this question',
  created_by_name VARCHAR(255) NOT NULL COMMENT 'Name of user who created this question',
  is_shared BOOLEAN DEFAULT FALSE COMMENT 'Whether this question is shared with other teachers (FALSE = private)',
  
  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When question was created',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When question was last updated',
  
  -- Foreign key and indexes
  FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE SET NULL,
  INDEX idx_test_id (test_id),
  INDEX idx_order_index (order_index),
  INDEX idx_question_type (type),
  INDEX idx_created_by (created_by),
  INDEX idx_is_shared (is_shared)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='Table storing test questions';

ALTER TABLE test_questions MODIFY COLUMN test_id BIGINT NULL;

-- =====================================================
-- Display created tables
-- =====================================================
SHOW TABLES;
SHOW CREATE TABLE tests;
SHOW CREATE TABLE test_questions;

-- =====================================================
-- Table: test_attempts
-- Description: Store attempts/submissions by students
-- =====================================================
CREATE TABLE IF NOT EXISTS test_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  test_id BIGINT COMMENT 'Reference to test',
  user_id BIGINT NOT NULL,
  user_name VARCHAR(255) NOT NULL,
  started_at DATETIME,
  submitted_at DATETIME,
  duration_seconds INT,
  duration_minutes INT,
  score INT,
  max_score INT,
  status VARCHAR(100),
  is_submitted TINYINT(1) DEFAULT 0,
  answers_json JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE SET NULL,
  INDEX idx_test_id (test_id),
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Student attempts for tests';
