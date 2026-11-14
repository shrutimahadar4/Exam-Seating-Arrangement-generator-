@echo off
echo ========================================================
echo Compiling Exam Sitting Arrangement Generator...
echo ========================================================

powershell -NoProfile -ExecutionPolicy Bypass -File build.ps1

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Build finished successfully!
) else (
    echo.
    echo [ERROR] Build failed.
)
pause
