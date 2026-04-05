@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0.."

if not exist "backend\pom.xml" (
  echo [ERROR] Not found: backend\pom.xml. Run from repo root.
  pause
  exit /b 1
)

echo [INFO] Starting backend (Spring Boot), port 8080
echo.

mvn -f backend\pom.xml spring-boot:run
set EXIT_CODE=%ERRORLEVEL%

echo.
if not "%EXIT_CODE%"=="0" (
  echo [ERROR] Backend exited with code: %EXIT_CODE%
) else (
  echo [INFO] Backend stopped.
)
pause
exit /b %EXIT_CODE%
