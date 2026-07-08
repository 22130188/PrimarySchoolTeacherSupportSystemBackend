@echo off
chcp 65001 >nul
cls
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║   IMPORT DỮ LIỆU MẪU - TEST SERVICE (USER 27)        ║
echo ║   User: van41527@gmail.com                            ║
echo ╚════════════════════════════════════════════════════════╝
echo.

REM Kiểm tra MySQL
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo [LỖI] MySQL không được tìm thấy trong PATH!
    echo Vui lòng cài đặt MySQL hoặc thêm vào PATH.
    echo.
    pause
    exit /b 1
)

echo [INFO] Đang kết nối MySQL...
echo.

REM Nhập mật khẩu
set /p MYSQL_PASSWORD="Nhập mật khẩu MySQL root: "
echo.

echo [INFO] Đang import dữ liệu vào database test_db...
echo [INFO] File: insert_sample_data_user27.sql
echo.

mysql -u root -p%MYSQL_PASSWORD% test_db < insert_sample_data_user27.sql

if %errorlevel% equ 0 (
    echo.
    echo ╔════════════════════════════════════════════════════════╗
    echo ║           ✓ IMPORT THÀNH CÔNG!                        ║
    echo ╚════════════════════════════════════════════════════════╝
    echo.
    echo 📊 Dữ liệu đã tạo:
    echo    • 130 câu hỏi đa dạng
    echo      - 85 câu trắc nghiệm
    echo      - 20 câu điền vào chỗ trống
    echo      - 15 câu ghép cặp
    echo      - 10 câu tự luận
    echo.
    echo    • 4 bài kiểm tra/bài tập
    echo      - 2 bài kiểm tra (EXAM)
    echo      - 2 bài tập (EXERCISE)
    echo.
    echo 👤 Tạo cho user: van41527@gmail.com (ID: 27)
    echo.
    echo 📋 Chi tiết các bài:
    echo    1. Kiểm tra giữa kỳ I - Toán lớp 1 (15 câu, 30 phút)
    echo    2. Kiểm tra cuối kỳ I - Toán lớp 2 (20 câu, 40 phút)
    echo    3. Bài tập tuần 10 - Toán lớp 3 (18 câu, 35 phút)
    echo    4. Bài tập ôn tập (15 câu, 30 phút)
    echo.
    echo 💡 Lưu ý:
    echo    - Chưa có ảnh/audio (bạn sẽ tự thêm)
    echo    - Cần cập nhật classroom_id nếu muốn liên kết với lớp học
    echo.
    echo ╚════════════════════════════════════════════════════════╝
) else (
    echo.
    echo ╔════════════════════════════════════════════════════════╗
    echo ║           ✗ IMPORT THẤT BẠI!                          ║
    echo ╚════════════════════════════════════════════════════════╝
    echo.
    echo ⚠ Vui lòng kiểm tra:
    echo    1. Mật khẩu MySQL có đúng không
    echo    2. Database test_db đã được tạo chưa
    echo    3. File insert_sample_data_user27.sql có tồn tại không
    echo    4. MySQL service đang chạy không
    echo.
    echo 💡 Thử chạy lệnh này để tạo database:
    echo    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS test_db;"
    echo.
    echo ╚════════════════════════════════════════════════════════╝
)

echo.
pause
