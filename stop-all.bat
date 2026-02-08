@echo off
REM Stop all analytics platform services
REM This script stops frontend, backend, event generator, and Docker containers

echo ========================================
echo Stopping Analytics Platform Services
echo ========================================
echo.

REM Stop Node.js (Frontend)
echo [1/5] Stopping Frontend (Node.js)...
taskkill /F /IM node.exe >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Frontend stopped successfully
) else (
    echo No frontend process found or already stopped
)

REM Stop Java processes (Backend + Event Generator)
echo [2/5] Stopping Java processes (Backend + Event Generator)...
taskkill /F /IM java.exe >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Java processes stopped successfully
) else (
    echo No Java processes found or already stopped
)

REM Stop MongoDB Docker container
echo [3/5] Stopping MongoDB container...
docker stop mongodb >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo MongoDB stopped
) else (
    echo MongoDB not running or already stopped
)

REM Stop Redis Docker container
echo [4/5] Stopping Redis container...
docker stop redis >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Redis stopped
) else (
    echo Redis not running or already stopped
)

REM Optional: Remove containers (commented out by default)
REM echo [5/5] Removing containers...
REM docker rm mongodb redis >nul 2>&1

echo.
echo ========================================
echo All services stopped
echo ========================================
echo.

REM Verify all stopped
echo Verification:
echo.

echo Checking Node.js processes:
tasklist /FI "IMAGENAME eq node.exe" 2>nul | find /I /N "node.exe" >nul
if %ERRORLEVEL% EQU 0 (
    echo   WARNING: Some Node.js processes still running
) else (
    echo   ✓ Node.js stopped
)

echo Checking Java processes:
tasklist /FI "IMAGENAME eq java.exe" 2>nul | find /I /N "java.exe" >nul
if %ERRORLEVEL% EQU 0 (
    echo   WARNING: Some Java processes still running
) else (
    echo   ✓ Java stopped
)

echo Checking Docker containers:
docker ps --filter "name=mongodb" --filter "name=redis" --format "{{.Names}}" >nul 2>&1
docker ps --filter "name=mongodb" --filter "name=redis" --format "{{.Names}}" 2>nul | find /I "mongodb" >nul
if %ERRORLEVEL% EQU 0 (
    echo   WARNING: MongoDB still running
) else (
    echo   ✓ MongoDB stopped
)

docker ps --filter "name=redis" --format "{{.Names}}" 2>nul | find /I "redis" >nul
if %ERRORLEVEL% EQU 0 (
    echo   WARNING: Redis still running
) else (
    echo   ✓ Redis stopped
)

echo.
echo To remove Docker containers completely, run:
echo   docker rm mongodb redis
echo.
pause
