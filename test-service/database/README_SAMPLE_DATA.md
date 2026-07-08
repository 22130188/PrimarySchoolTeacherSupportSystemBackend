# 📚 DỮ LIỆU MẪU - TEST SERVICE

## 🎯 Tổng Quan Nhanh

File `sample_data.sql` cung cấp **dữ liệu mẫu đầy đủ và đa dạng** cho test-service.

### 📊 Nội Dung

| Loại | Số lượng | Mô tả |
|------|----------|-------|
| **Câu hỏi** | 60 | Trắc nghiệm, Điền chỗ trống, Ghép cặp, Tự luận |
| **Bài kiểm tra** | 5 | Toán (lớp 1,2,3), Ngữ văn, Khoa học |
| **Bài tập** | 5 | Toán, Tiếng Anh, Đạo đức |
| **Bài nộp** | 4 | Mẫu bài làm của học sinh |
| **Bài đăng lớp** | 10 | Liên kết với classroom-service |

### 🚀 Cách Sử Dụng Nhanh

**Windows:**
```bash
cd d:\Test\PrimarySchoolTeacherSupportSystemBackend\test-service\database
import_sample_data.bat
```

**MySQL Command:**
```bash
mysql -u root -p test_db < sample_data.sql
```

### 📖 Tài Liệu

Xem hướng dẫn chi tiết tại: [HUONG_DAN_SU_DUNG.md](./HUONG_DAN_SU_DUNG.md)

### ⭐ Đặc Điểm Nổi Bật

- ✅ **Đa dạng môn học**: Toán, Ngữ văn, Tiếng Anh, Khoa học, Đạo đức, Mỹ thuật
- ✅ **Nhiều lớp học**: Lớp 1, 2, 3
- ✅ **4 loại câu hỏi**: Trắc nghiệm, Điền vào chỗ trống, Ghép cặp, Tự luận
- ✅ **Phân biệt rõ**: Bài kiểm tra (EXAM) vs Bài tập (EXERCISE)
- ✅ **Dữ liệu thực tế**: Câu hỏi phù hợp với chương trình tiểu học Việt Nam
- ✅ **Dễ tùy chỉnh**: Có thể thay đổi thời gian, điểm số, nội dung

### 📝 Lưu Ý

1. **User IDs giả định**: Cần cập nhật với user_id thực từ `user_db`
2. **Classroom IDs giả định**: Cần cập nhật với classroom_id thực từ `classroom_db`
3. **Ảnh & Audio**: Chưa có URL, cần thêm sau khi upload lên Cloudinary
4. **Thời gian**: Điều chỉnh `start_at` và `end_at` cho phù hợp

### 🔧 Tùy Chỉnh Nhanh

```sql
-- Cập nhật user ID
UPDATE test_questions SET created_by = <user_id_thuc> WHERE created_by = 1;

-- Cập nhật thời gian (dời 7 ngày)
UPDATE tests SET 
    start_at = DATE_ADD(start_at, INTERVAL 7 DAY),
    end_at = DATE_ADD(end_at, INTERVAL 7 DAY);
```

### 💡 Ví Dụ Câu Hỏi

**Trắc nghiệm:**
- 2 + 3 = ? (Toán lớp 1)
- What color is the sky? (Tiếng Anh)
- Con vật nào sống dưới nước? (Khoa học)

**Điền vào chỗ trống:**
- 5 + _____ = 10
- Mặt trời mọc ở hướng _____ và lặn ở hướng _____.

**Ghép cặp:**
- Nối phép tính với kết quả
- Ghép hình với tên gọi

**Tự luận:**
- Giải bài toán có lời văn
- Viết đoạn văn về gia đình

---

**🎓 Chúc bạn sử dụng tốt dữ liệu mẫu này!**
