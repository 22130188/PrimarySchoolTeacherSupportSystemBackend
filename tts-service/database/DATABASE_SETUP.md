# TTS Service - Database Setup Guide

## Prerequisites
- MySQL Server 8.0 or higher
- MySQL CLI or MySQL Workbench

## Setup Steps

### Option 1: Using MySQL CLI

```bash
# Connect to MySQL
mysql -u root -p

# Run the SQL script
source /path/to/tts-service/database/schema.sql

# Verify
USE tts_db;
SHOW TABLES;
DESCRIBE audio_records;
```

### Option 2: Using MySQL Workbench

1. Open MySQL Workbench
2. Connect to your MySQL server
3. Go to File > Open SQL Script
4. Select `schema.sql`
5. Press Ctrl+Shift+Enter to execute all queries

### Option 3: Using Spring Boot Initialization

The database will be automatically created on the first application startup due to `spring.jpa.hibernate.ddl-auto=update` setting in `application.yml`.

To auto-create the schema:
1. Start the TTS Service application
2. Check logs for successful table creation

## Database Configuration

### application.yml settings for database connection:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tts_db?useSSL=false&serverTimezone=UTC
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create/update tables
```

## Verify Installation

```sql
USE tts_db;

-- Check existing tables
SHOW TABLES;

-- Check audio_records table structure
DESCRIBE audio_records;

-- Check indexes
SHOW INDEX FROM audio_records;

-- Sample query to retrieve audios for a user
SELECT * FROM audio_records 
WHERE user_id = 1 
ORDER BY created_at DESC;
```

## Backup Database

```bash
# Dump database structure and data
mysqldump -u root -p tts_db > tts_db_backup.sql

# Restore from backup
mysql -u root -p tts_db < tts_db_backup.sql
```

## Database Cleanup (if needed)

```sql
-- WARNING: This will delete all data
TRUNCATE TABLE audio_records;

-- Drop the entire database
DROP DATABASE tts_db;
```

## Troubleshooting

### Issue: "Connection refused"
- Ensure MySQL server is running
- Check MySQL port (default 3306)
- Verify credentials in application.yml

### Issue: "Unknown database 'tts_db'"
- Run the schema.sql script
- Or let Spring Boot auto-create it on application startup

### Issue: "Table doesn't exist"
- Check that schema.sql was executed successfully
- Verify the database connection URL in application.yml

## Notes

- The `audio_records` table uses InnoDB engine for transaction support
- UTF-8 character set ensures Vietnamese text is properly stored
- Indexes are created on `user_id` and `created_at` for better query performance
- Automatic timestamp management: `created_at` set on insert, `updated_at` updated on modify
