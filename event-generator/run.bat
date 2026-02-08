@echo off
REM Build and run script for event generator (Windows)

echo Building event generator...
call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo Starting event generator...
    java -jar target\event-generator-1.0.0.jar
) else (
    echo Build failed!
    exit /b 1
)
