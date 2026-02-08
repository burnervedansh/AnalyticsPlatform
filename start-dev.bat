@echo off
REM Dev script to start all services for analytics platform
REM This script starts MongoDB, Redis, Backend, Event Generator, and Frontend

echo ========================================
echo Starting Analytics Platform (Dev Mode)
echo ========================================
echo.

REM Check if Docker is running
docker ps >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Docker is not running
    echo Please start Docker Desktop
    pause
    exit /b 1
)

REM Start MongoDB
echo [1/5] Starting MongoDB...
docker ps -a -q --filter "name=mongodb" | findstr . >nul
if %ERRORLEVEL% EQU 0 (
    REM Container exists, check if running
    docker ps -q --filter "name=mongodb" | findstr . >nul
    if %ERRORLEVEL% EQU 0 (
        echo MongoDB already running
    ) else (
        echo Starting stopped MongoDB container...
        docker start mongodb >nul
        echo MongoDB started on port 27017
    )
) else (
    REM Container doesn't exist, create it
    echo Creating new MongoDB container...
    docker run -d --name mongodb -p 27017:27017 mongo:6.0 >nul
    echo MongoDB started on port 27017
)

REM Start Redis
echo [2/5] Starting Redis...
docker ps -a -q --filter "name=redis" | findstr . >nul
if %ERRORLEVEL% EQU 0 (
    REM Container exists, check if running
    docker ps -q --filter "name=redis" | findstr . >nul
    if %ERRORLEVEL% EQU 0 (
        echo Redis already running
    ) else (
        echo Starting stopped Redis container...
        docker start redis >nul
        echo Redis started on port 6379
    )
) else (
    REM Container doesn't exist, create it
    echo Creating new Redis container...
    docker run -d --name redis -p 6379:6379 redis:7.0-alpine >nul
    echo Redis started on port 6379
)

echo.
echo Waiting for services to be ready...
timeout /t 5 /nobreak >nul
echo.

REM Start Backend
echo [3/5] Starting Backend API (port 8080)...
start "Backend API" cmd /k "cd /d %~dp0analytics-backend && java -jar target/analytics-backend-1.0.0.jar"
timeout /t 10 /nobreak >nul

REM Start Event Generator
echo [4/5] Starting Event Generator...
start "Event Generator" cmd /k "cd /d %~dp0event-generator && java -jar target/event-generator-1.0.0.jar"
timeout /t 5 /nobreak >nul

REM Start Frontend
echo [5/5] Starting Frontend Dashboard (port 5173)...
start "Frontend Dashboard" cmd /k "cd /d %~dp0analytics-dashboard && npm run dev"

echo.
echo ========================================
echo All services starting!
echo ========================================
echo.
echo Services:
echo   - MongoDB:      localhost:27017
echo   - Redis:        localhost:6379
echo   - Backend API:  http://localhost:8080
echo   - Event Gen:    Running in background
echo   - Dashboard:    http://localhost:5173
echo.
echo Wait 15-20 seconds, then open:
echo   http://localhost:5173
echo.
echo To stop all services:
echo   - Close all terminal windows
echo   - Run: docker stop mongodb redis
echo.
pause
