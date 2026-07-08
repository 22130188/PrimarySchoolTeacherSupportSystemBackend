@echo off
REM =====================================================
REM Setup Test Database - Windows Batch Script
REM =====================================================

echo.
echo ========================================
echo Setting up Test Database...
echo ========================================
echo.

REM Get MySQL credentials from user
set /p mysql_user="Enter MySQL username (default: root): "
if "%mysql_user%"=="" set mysql_user=root

set /p mysql_password="Enter MySQL password (press Enter if none): "

REM Check if schema file exists
if not exist "schema.sql" (
    echo ERROR: schema.sql not found in current directory!
    echo Please run this script from the database directory.
    pause
    exit /b 1
)

REM Connect to MySQL and run schema
echo.
echo Executing schema.sql...
echo.

if "%mysql_password%"=="" (
    mysql -u %mysql_user% < schema.sql
) else (
    mysql -u %mysql_user% -p%mysql_password% < schema.sql
)

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo Database setup completed successfully!
    echo ========================================
    echo.
    echo Verify by running:
    echo   mysql -u %mysql_user% -e "USE test_db; SHOW TABLES;"
    echo.
) else (
    echo.
    echo ERROR: Database setup failed!
    echo Please check MySQL connection and try again.
    echo.
)

pause
