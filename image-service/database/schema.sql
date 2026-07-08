-- =====================================================
-- Database: Image Generation Service Database
-- Description: Database for Image Generation Service
-- =====================================================

-- Drop existing database if exists (for testing only)
-- DROP DATABASE IF EXISTS image_db;

-- Create database
CREATE DATABASE IF NOT EXISTS image_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE image_db;

-- =====================================================
-- Table: image_records
-- Description: Store generated images and metadata
-- =====================================================
CREATE TABLE IF NOT EXISTS image_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,

  -- Image content
  description TEXT NOT NULL COMMENT 'Description used to generate the image',
  image_url VARCHAR(500) NOT NULL COMMENT 'URL from Cloudinary',

  -- User information
  user_id BIGINT NOT NULL COMMENT 'ID of user who created the image',
  user_name VARCHAR(255) COMMENT 'Name of user who created the image',
  subject VARCHAR(255) COMMENT 'Subject name for the saved image',

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
  COMMENT='Table storing generated image records';

-- =====================================================
-- Display created tables
-- =====================================================
SHOW TABLES;
SHOW CREATE TABLE image_records;

-- =====================================================