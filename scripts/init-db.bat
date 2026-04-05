@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0.."

if not exist "database\init.sql" (
  echo [ERROR] Not found: database\init.sql
  pause
  exit /b 1
)

if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306

set /p DB_USER="Enter MySQL username: "
set /p DB_PASS="Enter MySQL password: "

echo.
echo [INFO] Using DB: HOST=%DB_HOST% PORT=%DB_PORT% USER=%DB_USER%
echo.

echo [INFO] Importing database\init.sql ...
mysql -h %DB_HOST% -P %DB_PORT% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 < database\init.sql
set EXIT_CODE=%ERRORLEVEL%

echo.
if not "%EXIT_CODE%"=="0" (
  echo [ERROR] Database init failed. Check MySQL and credentials.
) else (
  echo [INFO] Database init success.
)
pause
exit /b %EXIT_CODE%