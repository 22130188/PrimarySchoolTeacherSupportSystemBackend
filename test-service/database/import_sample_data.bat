@echo off
chcp 65001 >nul
echo ========================================
echo   IMPORT DỮ LIỆU MẪU - TEST SERVICE
echo ========================================
echo.

REM Kiểm tra MySQL có được cài đặt không
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo [LỖI] MySQL không được tìm thấy trong PATH!
    echo Vui lòng cài đặt MySQL hoặc thêm vào PATH.
    pause
    exit /b 1
)

echo [INFO] Đang import dữ liệu mẫu vào database test_db...
echo.

REM Yêu cầu nhập mật khẩu MySQL
set /p MYSQL_PASSWORD="Nhập mật khẩu MySQL root: "

echo.
echo [INFO] Đang chạy file sample_data.sql...
mysql -u root -p%MYSQL_PASSWORD% test_db < sample_data.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   ✓ IMPORT THÀNH CÔNG!
    echo ========================================
    echo.
    echo Dữ liệu đã được tạo:
    echo - 60 câu hỏi đa dạng
    echo - 5 bài kiểm tra
    echo - 5 bài tập
    echo - 4 bài nộp mẫu
    echo - 10 bài đăng lớp học
    echo.
    echo Xem hướng dẫn chi tiết tại: HUONG_DAN_SU_DUNG.md
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   ✗ IMPORT THẤT BẠI!
    echo ========================================
    echo.
    echo Vui lòng kiểm tra:
    echo 1. Mật khẩu MySQL có đúng không
    echo 2. Database test_db đã được tạo chưa
    echo 3. File sample_data.sql có tồn tại không
    echo ========================================
)

echo.
pause
