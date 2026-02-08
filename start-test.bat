@echo off
REM Quick start script for testing the analytics platform with Docker

echo ========================================
echo Analytics Platform - Test Environment
echo ========================================
echo.

echo Checking Docker...
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Docker is not installed or not in PATH
    echo Please install Docker Desktop from: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)

echo Docker is available!
echo.

echo Building and starting services...
echo This may take 5-10 minutes on first run...
echo.

docker-compose -f docker-compose.test.yml up --build -d

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Services started successfully!
    echo ========================================
    echo.
    echo Services running:
    echo   - MongoDB: localhost:27017
    echo   - Redis: localhost:6379
    echo   - Backend API: http://localhost:8080
    echo   - Event Generator: Running in background
    echo.
    echo Waiting for services to be healthy...
    timeout /t 30 /nobreak > nul
    echo.
    echo Testing backend health...
    curl -s http://localhost:8080/actuator/health
    echo.
    echo.
    echo ========================================
    echo Quick Test Commands:
    echo ========================================
    echo.
    echo View logs:
    echo   docker-compose -f docker-compose.test.yml logs -f
    echo.
    echo Test active users:
    echo   curl http://localhost:8080/api/analytics/active-users
    echo.
    echo Test top pages:
    echo   curl http://localhost:8080/api/analytics/top-pages?limit=5
    echo.
    echo Stop services:
    echo   docker-compose -f docker-compose.test.yml down
    echo.
    echo ========================================
    echo.
    pause
) else (
    echo.
    echo ERROR: Failed to start services
    echo Check the logs with: docker-compose -f docker-compose.test.yml logs
    pause
    exit /b 1
)
