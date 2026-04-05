@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0.."
set "ROOT_DIR=%CD%"
cd /d "%ROOT_DIR%\frontend"

if not exist "package.json" (
  echo [ERROR] Not found: frontend\package.json. Run from repo root.
  pause
  exit /b 1
)

echo [INFO] Installing frontend dependencies...
call npm install
if not "%ERRORLEVEL%"=="0" (
  echo [ERROR] Frontend install failed.
  pause
  exit /b %ERRORLEVEL%
)

echo [INFO] Starting frontend dev server (Vite), port 3000
echo.

call npm run dev
set EXIT_CODE=%ERRORLEVEL%

echo.
if not "%EXIT_CODE%"=="0" (
  echo [ERROR] Frontend exited with code: %EXIT_CODE%
) else (
  echo [INFO] Frontend stopped.
)
pause
exit /b %EXIT_CODE%
