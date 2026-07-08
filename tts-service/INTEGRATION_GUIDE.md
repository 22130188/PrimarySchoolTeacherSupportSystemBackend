# TTS Microservice - Complete Integration Guide

## 📋 Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Component Setup](#component-setup)
3. [Integration Flow](#integration-flow)
4. [API Endpoints](#api-endpoints)
5. [Environment Configuration](#environment-configuration)
6. [Testing](#testing)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (React)                         │
│  TTSPage.jsx → TTSService.js (API calls)                        │
└──────────────────────────┬──────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ API Gateway  │  │  Auth Service│  │ TTS Service  │
│  (Port 8080) │  │ (Port 8082)  │  │ (Port 8084)  │
└──────────────┘  └──────────────┘  └──────┬───────┘
                                            │
                    ┌───────────────────────┼───────────────────────┐
                    │                       │                       │
                    ▼                       ▼                       ▼
            ┌─────────────────┐   ┌──────────────┐   ┌───────────────┐
            │ Python TTS API  │   │   Cloudinary │   │  TTS Database │
            │  (Port 8000)    │   │   (Storage)  │   │   (MySQL)     │
            └─────────────────┘   └──────────────┘   └───────────────┘
```

## Component Setup

### 1. React Component (Frontend)

**Location:** `src/views/TTSPage.jsx`

```bash
# Key Features:
- Text input with validation (max 5000 characters)
- Real-time audio player
- Save/Delete audio functionality
- Saved audios history sidebar
- Responsive Tailwind CSS design
```

**Dependencies:**
```json
{
  "react": "^19.2.0",
  "react-router-dom": "^7.13.1",
  "tailwindcss": "^4.2.2",
  "zustand": "^5.0.12",
  "lucide-react": "^0.577.0"
}
```

### 2. TTS Microservice (Spring Boot)

**Location:** `tts-service/`

Structure:
```
tts-service/
├── pom.xml                          # Maven configuration
├── src/main/java/vn/edu/primary/tts/
│   ├── TTSServiceApplication.java   # Main application class
│   ├── config/
│   │   ├── CloudinaryConfig.java
│   │   └── CloudinaryProvider.java
│   ├── controller/
│   │   └── TTSController.java       # REST endpoints
│   ├── dto/
│   │   ├── TTSConvertRequest.java
│   │   ├── SaveAudioRequest.java
│   │   ├── AudioRecordResponse.java
│   │   └── ApiResponse.java
│   ├── entity/
│   │   └── AudioRecord.java         # JPA Entity
│   ├── repository/
│   │   └── AudioRecordRepository.java
│   ├── security/
│   │   ├── JwtFilter.java
│   │   └── JwtProvider.java
│   └── service/
│       ├── TTSService.java
│       └── impl/
│           └── TTSServiceImpl.java
├── src/main/resources/
│   └── application.yml              # Configuration
└── database/
    ├── schema.sql
    └── DATABASE_SETUP.md
```

### 3. Python TTS API

**Location:** `api-python/`

```
api-python/
├── app/
│   ├── main.py              # FastAPI app
│   ├── api/
│   │   └── tts.py           # TTS routes
│   ├── services/
│   │   └── tts_service.py   # gTTS service
│   ├── schemas/
│   │   └── tts_schema.py    # Request/Response models
│   └── core/
│       └── config.py        # Cloudinary config
├── requirements.txt
└── .env                     # Environment variables
```

## Integration Flow

### User converts text to speech:

```
1. User enters Vietnamese text in TTSPage
                  ↓
2. Clicks "Chuyển đổi" button
                  ↓
3. Frontend calls TTSService.convertTextToSpeech()
                  ↓
4. HTTP POST to http://api-gateway/api/tts/convert
   - Authorization: Bearer <JWT_TOKEN>
   - Body: { text: "Xin chào" }
                  ↓
5. API Gateway routes to TTS Service
                  ↓
6. TTS Service validates JWT and extracts userId
                  ↓
7. Calls Python API: POST http://python-tts-api/api/tts/convert
                  ↓
8. Python API uses gTTS to convert text → output_audio_TIMESTAMP.mp3
                  ↓
9. TTS Service uploads MP3 to Cloudinary
                  ↓
10. Cloudinary returns audioUrl (secure HTTPS URL)
                  ↓
11. TTS Service returns audioUrl to Frontend
                  ↓
12. Frontend displays audio player with playback controls
```

### User saves audio:

```
1. User clicks "Lưu âm thanh" button
                  ↓
2. Frontend calls TTSService.saveAudio()
                  ↓
3. HTTP POST to http://api-gateway/api/tts/save
   - Authorization: Bearer <JWT_TOKEN>
   - Body: { text, audioUrl, userId }
                  ↓
4. TTS Service saves to MySQL database
                  ↓
5. AudioRecord entity created with:
   - id: auto-generated
   - text: original text
   - audioUrl: Cloudinary URL
   - userId: from JWT token
   - createdAt: current timestamp
                  ↓
6. Frontend displays success message
                  ↓
7. Refreshes saved audios list via GET /api/tts/audios/{userId}
```

## API Endpoints

### Frontend APIs (called by TTSPage)

#### 1. Convert Text to Speech
```
POST /api/tts/convert
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "text": "Xin chào, đây là một quả táo đỏ"
}

Response 200:
{
  "success": true,
  "message": "Chuyển đổi thành công",
  "data": {
    "audioUrl": "https://res.cloudinary.com/...audio.mp3",
    "text": "Xin chào, đây là một quả táo đỏ"
  }
}
```

#### 2. Save Audio
```
POST /api/tts/save
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "text": "Original Vietnamese text",
  "audioUrl": "https://res.cloudinary.com/.../audio.mp3",
  "userId": 123
}

Response 201:
{
  "success": true,
  "message": "Lưu âm thanh thành công",
  "data": {
    "id": 1,
    "text": "Original Vietnamese text",
    "audioUrl": "https://res.cloudinary.com/.../audio.mp3",
    "userId": 123,
    "createdAt": "2026-04-06T10:30:00"
  }
}
```

#### 3. Get User's Saved Audios
```
GET /api/tts/audios/{userId}
Authorization: Bearer <JWT_TOKEN>

Response 200:
{
  "success": true,
  "message": "Lấy danh sách thành công",
  "data": [
    {
      "id": 1,
      "text": "Text 1",
      "audioUrl": "https://...",
      "userId": 123,
      "createdAt": "2026-04-06T10:30:00"
    },
    {
      "id": 2,
      "text": "Text 2",
      "audioUrl": "https://...",
      "userId": 123,
      "createdAt": "2026-04-06T10:25:00"
    }
  ]
}
```

#### 4. Delete Audio
```
DELETE /api/tts/audios/{audioId}
Authorization: Bearer <JWT_TOKEN>

Response 200:
{
  "success": true,
  "message": "Xóa âm thanh thành công",
  "data": null
}
```

### Backend APIs (internal)

#### Python TTS API
```
POST /api/tts/convert
Content-Type: application/json

{
  "text": "Xin chào"
}

Response 200:
{
  "success": true,
  "message": "Chuyển đổi thành công",
  "filename": "output_audio_20260406_103000_123456.mp3"
}
```

## Environment Configuration

### Frontend (.env or Vite config)

```env
VITE_API_URL=http://localhost:8080/api
# or in production
VITE_API_URL=https://your-domain.com/api
```

### TTS Service (tts-service/src/main/resources/application.yml)

```yaml
spring:
  application:
    name: tts-service
  jpa:
    hibernate:
      ddl-auto: update
  datasource:
    url: jdbc:mysql://localhost:3306/tts_db?useSSL=false&serverTimezone=UTC
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

server:
  port: 8084
  servlet:
    context-path: /api/tts

jwt:
  secret-key: your_super_secret_key_change_in_production_very_long_string
  expiration: 86400000  # 24 hours

python:
  tts:
    api-url: http://localhost:8000/api
    timeout: 30000

cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}
  folder: tts-audios
```

### Python TTS API (.env)

```env
# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# FastAPI
HOST=0.0.0.0
PORT=8000
```

### Docker Compose (optional, for local development)

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: tts_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  python-tts:
    build: ./api-python
    ports:
      - "8000:8000"
    environment:
      CLOUDINARY_CLOUD_NAME: ${CLOUDINARY_CLOUD_NAME}
      CLOUDINARY_API_KEY: ${CLOUDINARY_API_KEY}
      CLOUDINARY_API_SECRET: ${CLOUDINARY_API_SECRET}
    depends_on:
      - mysql

volumes:
  mysql_data:
```

## Testing

### 1. Test Python TTS API

```bash
# Start Python API
cd api-python
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --port 8000

# Test endpoint
curl -X POST http://localhost:8000/api/tts/convert \
  -H "Content-Type: application/json" \
  -d '{"text": "Xin chào"}'
```

### 2. Test TTS Service

```bash
# Build and run
mvn clean package -DskipTests
java -jar tts-service/target/tts-service-0.0.1-SNAPSHOT.jar

# Health check
curl http://localhost:8084/api/tts/convert/health

# Test convert
curl -X POST http://localhost:8084/api/tts/convert \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"text": "Xin chào"}'
```

### 3. Database Verification

```sql
USE tts_db;

-- Check table structure
DESCRIBE audio_records;

-- Query saved audios
SELECT * FROM audio_records 
WHERE user_id = 123 
ORDER BY created_at DESC;

-- Count records
SELECT COUNT(*) as total_audios FROM audio_records;
```

### 4. Frontend Testing

```bash
# Navigate to TTSPage
http://localhost:5173/tts

# Test features:
1. Enter Vietnamese text
2. Click "Chuyển đổi"
3. Test audio playback
4. Click "Lưu âm thanh"
5. Verify in saved audios list
6. Test delete functionality
```

## Troubleshooting

### Issue: JWT Token validation fails
- Check token format: `Bearer <token>`
- Verify JWT secret key matches between services
- Check token expiration

### Issue: Python API returns error
- Ensure Python dependencies installed: `pip install gtts cloudinary`
- Check Cloudinary credentials
- Verify text encoding (UTF-8)

### Issue: Cloudinary upload fails
- Verify API credentials
- Check folder permission in Cloudinary
- Ensure MP3 file is valid

### Issue: Database connection error
- MySQL server running on port 3306
- Correct username/password in application.yml
- Database `tts_db` exists

## Production Deployment

1. **Environment Variables:** Use secure credential management (AWS Secrets, Azure Key Vault)
2. **SSL/TLS:** Enable HTTPS for all endpoints
3. **CORS:** Configure only allowed frontend domains
4. **Rate Limiting:** Implement API rate limits
5. **Monitoring:** Set up logging and monitoring (ELK, DataDog)
6. **Database:** Use managed MySQL service (AWS RDS, Azure Database)
7. **Cloudinary:** Verify rate limits and storage usage
