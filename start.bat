@echo off
title StudyPulse - Gamified Study & To-Do Planner
echo ========================================================
echo   StudyPulse - Gamified Study & To-Do Planner
echo ========================================================
echo.
echo [1/2] Checking Java environment...
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not found in PATH!
    echo Please install Java 17+ or configure PATH.
    pause
    exit /b 1
)

echo [2/2] Starting StudyPulse Backend...
echo.
echo Opening browser at http://localhost:8080 ...
start "" "http://localhost:8080"
echo.
if exist "target\study-pulse-app-1.0.0.jar" (
    java -jar target\study-pulse-app-1.0.0.jar
) else (
    echo Compiling and running via Maven...
    mvn spring-boot:run
)
pause
