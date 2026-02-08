@echo off
REM Script to test individual components

echo ========================================
echo Component Testing Menu
echo ========================================
echo.
echo 1. Test Backend Only (requires MongoDB + Redis)
echo 2. Test Event Generator Only (with mock endpoint)
echo 3. View Backend Logs
echo 4. View Event Generator Logs
echo 5. View All Logs
echo 6. Stop All Services
echo 7. Exit
echo.

set /p choice="Enter choice (1-7): "

if "%choice%"=="1" goto test_backend
if "%choice%"=="2" goto test_generator
if "%choice%"=="3" goto logs_backend
if "%choice%"=="4" goto logs_generator
if "%choice%"=="5" goto logs_all
if "%choice%"=="6" goto stop_all
if "%choice%"=="7" goto end

:test_backend
echo.
echo Starting MongoDB and Redis...
docker-compose -f docker-compose.test.yml up -d mongodb redis
timeout /t 10 /nobreak > nul
echo.
echo Starting Backend...
docker-compose -f docker-compose.test.yml up -d backend
timeout /t 20 /nobreak > nul
echo.
echo Testing backend endpoints...
echo.
echo Health Check:
curl http://localhost:8080/actuator/health
echo.
echo.
echo Posting test event...
curl -X POST http://localhost:8080/api/events -H "Content-Type: application/json" -d "{\"timestamp\":\"2024-03-15T14:30:00Z\",\"user_id\":\"usr_test\",\"event_type\":\"page_view\",\"page_url\":\"/test\",\"session_id\":\"sess_test\"}"
echo.
echo.
echo Waiting 15 seconds for metrics processing...
timeout /t 15 /nobreak > nul
echo.
echo Active Users:
curl http://localhost:8080/api/analytics/active-users
echo.
echo.
pause
goto end

:test_generator
echo.
echo NOTE: Event generator needs a backend endpoint.
echo Starting with httpbin.org as mock endpoint...
echo.
echo This test will show if the generator can successfully send requests.
echo.
pause
goto end

:logs_backend
echo.
echo Showing backend logs (Ctrl+C to exit)...
docker-compose -f docker-compose.test.yml logs -f backend
goto end

:logs_generator
echo.
echo Showing event generator logs (Ctrl+C to exit)...
docker-compose -f docker-compose.test.yml logs -f event-generator
goto end

:logs_all
echo.
echo Showing all logs (Ctrl+C to exit)...
docker-compose -f docker-compose.test.yml logs -f
goto end

:stop_all
echo.
echo Stopping all services...
docker-compose -f docker-compose.test.yml down
echo.
echo All services stopped.
echo.
pause
goto end

:end
