# TTS Service - Text-to-Speech Microservice

## 📱 Overview

TTS Service là một microservice trong hệ thống "Primary School Teacher Support System" với chức năng chuyển đổi text tiếng Việt thành giọng nói, lưu trữ audio trên Cloudinary, và quản lý lịch sử âm thanh.

## ✨ Features

- ✅ Chuyển đổi text tiếng Việt → giọng nói (gTTS)
- ✅ Tự động upload audio lên Cloudinary
- ✅ Lưu lịch sử audio vào database
- ✅ REST API với JWT authentication
- ✅ Tích hợp Eureka Service Discovery
- ✅ Responsive UI với React + Tailwind CSS

## 🏗️ Architecture

```
TTS Service
├── Frontend (TTSPage.jsx)
├── Spring Boot Backend (8084)
└── Python API (8000)
    └── Cloudinary (Storage)
    └── MySQL (Database)
```

## 📦 Dependencies

### Frontend
```json
{
  "react": "^19.2.0",
  "lucide-react": "^0.577.0",
  "tailwindcss": "^4.2.2",
  "zustand": "^5.0.12"
}
```

### Backend
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>com.cloudinary</groupId>
  <artifactId>cloudinary-http45</artifactId>
  <version>1.36.0</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.3</version>
</dependency>
```

### Python
```txt
fastapi>=0.104.0
uvicorn>=0.24.0
gtts>=2.3.2
cloudinary>=1.34.0
python-dotenv>=1.0.0
```

## 🚀 Quick Start

### 1. Database Setup

```sql
# Run schema.sql
mysql -u root -p < tts-service/database/schema.sql
```

### 2. Configure Environment

```yaml
# tts-service/src/main/resources/application.yml
cloudinary:
  cloud-name: your_cloud_name
  api-key: your_api_key
  api-secret: your_api_secret
```

```env
# api-python/.env
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### 3. Start Services

```bash
# Terminal 1: Python TTS API
cd api-python
python -m uvicorn app.main:app --reload --port 8000

# Terminal 2: TTS Service (Spring Boot)
cd tts-service
mvn spring-boot:run

# Terminal 3: Frontend
cd PrimarySchoolTeacherSupportSystemFrontEnd
npm run dev
```

### 4. Access Application

- Frontend: http://localhost:5173
- TTS Service: http://localhost:8084/api/tts
- Python API: http://localhost:8000

## 📚 API Documentation

### Convert Text to Speech
```bash
POST /api/tts/convert
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "text": "Xin chào, đây là một quả táo đỏ"
}
```

### Save Audio
```bash
POST /api/tts/save
Authorization: Bearer <JWT_TOKEN>

{
  "text": "Original text",
  "audioUrl": "https://...",
  "userId": 123
}
```

### Get User's Audios
```bash
GET /api/tts/audios/{userId}
Authorization: Bearer <JWT_TOKEN>
```

### Delete Audio
```bash
DELETE /api/tts/audios/{audioId}
Authorization: Bearer <JWT_TOKEN>
```

## 📊 Database Schema

```sql
CREATE TABLE audio_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  text LONGTEXT NOT NULL,
  audio_url VARCHAR(500) NOT NULL,
  user_id BIGINT NOT NULL,
  user_name VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 🎨 UI/UX Features

- **Modern Design:** Gradient background, smooth animations
- **Vietnamese Language:** Full Vietnamese UI and error messages
- **Real-time Validation:** Character count, input validation
- **Audio Player:** HTML5 audio with download option
- **Responsive:** Mobile, tablet, desktop layouts
- **Loading States:** Spinner animations during processing
- **Success/Error Alerts:** Clear user feedback
- **Saved Audios Sidebar:** Quick access to history

## 🔒 Security

- ✅ JWT Token Authentication
- ✅ CORS Configuration
- ✅ Input Validation (max 5000 characters)
- ✅ Secure Cloudinary URLs
- ✅ User ID from JWT Claims

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Python API tests
pytest api-python/tests/

# Frontend tests
npm run test
```

## 📝 Project Structure

```
tts-service/
├── src/main/java/vn/edu/primary/tts/
│   ├── controller/       # REST endpoints
│   ├── service/          # Business logic
│   ├── entity/           # JPA entities
│   ├── repository/       # Database access
│   ├── dto/              # Request/Response models
│   ├── config/           # Spring configurations
│   └── security/         # JWT handling
├── src/main/resources/
│   └── application.yml   # Application config
├── database/
│   ├── schema.sql        # Table definitions
│   └── DATABASE_SETUP.md # Setup guide
└── pom.xml               # Maven config

PrimarySchoolTeacherSupportSystemFrontEnd/
├── src/views/
│   ├── TTSPage.jsx       # Main component
│   └── TTSPage.css       # Styles
├── src/services/
│   └── TTSService.js     # API client
└── src/stores/
    └── authStore.js      # Auth state management
```

## 🐛 Troubleshooting

### "Database connection refused"
- Ensure MySQL is running: `mysql -u root -p`
- Check connection string in application.yml

### "Cloudinary upload fails"
- Verify API credentials
- Check Cloudinary folder permissions
- Ensure internet connection

### "JWT validation failed"
- Check Authorization header format: `Bearer <token>`
- Verify JWT secret key matches
- Check token hasn't expired

### "Python API not responding"
- Start Python server: `python -m uvicorn app.main:app --reload`
- Check port 8000 is available
- Verify gtts library installed: `pip install gtts`

## 📚 Documentation

- [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md) - Complete integration guide
- [DATABASE_SETUP.md](./database/DATABASE_SETUP.md) - Database setup instructions
- [schema.sql](./database/schema.sql) - SQL schema

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/new-feature`
2. Commit changes: `git commit -am 'Add new feature'`
3. Push to branch: `git push origin feature/new-feature`
4. Create Pull Request

## 📄 License

This project is part of Primary School Teacher Support System.

## 👥 Support

For issues and questions, please contact the development team.

---

**Last Updated:** April 2026  
**Version:** 1.0.0  
**Status:** Production Ready
