@echo off
echo ========================================================
echo Starting Exam Sitting Arrangement Generator...
echo ========================================================

java -cp "bin;lib/*" com.examseating.Main
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with error code %ERRORLEVEL%.
    pause
)
