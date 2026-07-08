-- =====================================================
-- Database: TTS Service Database
-- Description: Database for Text-to-Speech Service
-- =====================================================

-- Drop existing database if exists (for testing only)
-- DROP DATABASE IF EXISTS tts_db;

-- Create database
CREATE DATABASE IF NOT EXISTS tts_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE tts_db;

-- =====================================================
-- Table: audio_records
-- Description: Store converted audio files and metadata
-- =====================================================
CREATE TABLE IF NOT EXISTS audio_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  
  -- Audio content
  text LONGTEXT NOT NULL COMMENT 'Original Vietnamese text',
  audio_url VARCHAR(500) NOT NULL COMMENT 'URL from Cloudinary',
  
  -- User information
  user_id BIGINT NOT NULL COMMENT 'ID of user who created the audio',
  user_name VARCHAR(255) COMMENT 'Name of user who created the audio',
  audio_name VARCHAR(255) COMMENT 'Display name of the saved audio',
  subject VARCHAR(255) COMMENT 'Subject name for the saved audio',
  
  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  
  -- Indexes for better query performance
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at),
  INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='Table storing TTS audio records';

-- =====================================================
-- Display created tables
-- =====================================================
SHOW TABLES;
SHOW CREATE TABLE audio_records;

-- =====================================================
-- Sample data for testing (OPTIONAL)
-- =====================================================
-- INSERT INTO audio_records (text, audio_url, user_id, user_name)
-- VALUES (
--     'Xin chào, đây là một quả táo đỏ',
--     'https://res.cloudinary.com/example/audio/...',
--     1,
--     'Teacher Name'
-- );

-- SELECT * FROM audio_records WHERE user_id = 1 ORDER BY created_at DESC;
