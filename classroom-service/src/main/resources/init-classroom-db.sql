-- =============================================
-- Database: classroom_db
-- For: classroom-service
-- =============================================

CREATE DATABASE IF NOT EXISTS `classroom_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `classroom_db`;

-- ----------------------------
-- Table: classrooms
-- ----------------------------
CREATE TABLE IF NOT EXISTS `classrooms` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT,
    `name`              VARCHAR(255)    NOT NULL,
    `description`       TEXT            NULL,
    `teacher_id`        BIGINT          NOT NULL,
    `created_by`        BIGINT          NOT NULL,
    `class_code`        VARCHAR(8)      NOT NULL,
    `invite_link_token` VARCHAR(64)     NOT NULL,
    `is_deleted`        TINYINT(1)      DEFAULT 0,
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`        DATETIME        NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_class_code` (`class_code`),
    UNIQUE INDEX `uk_invite_link_token` (`invite_link_token`),
    INDEX `idx_teacher_id` (`teacher_id`)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table: classroom_members
-- ----------------------------
CREATE TABLE IF NOT EXISTS `classroom_members` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT,
    `classroom_id`  BIGINT          NOT NULL,
    `student_id`    BIGINT          NOT NULL,
    `join_type`     VARCHAR(20)     NOT NULL COMMENT 'INVITE_LINK, EMAIL_INVITE, CLASS_CODE',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    `joined_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_classroom_student` (`classroom_id`, `student_id`),
    INDEX `idx_classroom_id` (`classroom_id`),
    INDEX `idx_student_id` (`student_id`),
    CONSTRAINT `fk_member_classroom` FOREIGN KEY (`classroom_id`)
        REFERENCES `classrooms` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table: classroom_invitations
-- ----------------------------
CREATE TABLE IF NOT EXISTS `classroom_invitations` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT,
    `classroom_id`  BIGINT          NOT NULL,
    `email`         VARCHAR(100)    NOT NULL,
    `student_id`    BIGINT          NULL,
    `invited_by`    BIGINT          NOT NULL,
    `token`         VARCHAR(64)     NOT NULL,
    `status`        VARCHAR(20)     NOT NULL COMMENT 'WAITING_REGISTER, INVITED, ACCEPTED, REJECTED, EXPIRED, CANCELLED',
    `expired_at`    DATETIME        NOT NULL,
    `accepted_at`   DATETIME        NULL,
    `rejected_at`   DATETIME        NULL,
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_token` (`token`),
    INDEX `idx_classroom_id` (`classroom_id`),
    INDEX `idx_email` (`email`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_classroom_email_status` (`classroom_id`, `email`, `status`),
    CONSTRAINT `fk_invitation_classroom` FOREIGN KEY (`classroom_id`)
        REFERENCES `classrooms` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
