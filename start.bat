@echo off
echo ========================================================
echo  Exam Sitting Arrangement Generator - One-Click Launcher
echo ========================================================
echo.

if not exist bin\com\examseating\Main.class (
    echo Compiling application files...
    call compile.bat
)

call run.bat
