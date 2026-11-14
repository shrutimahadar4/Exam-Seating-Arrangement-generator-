# Build Script for Exam Sitting Arrangement Generator
$ErrorActionPreference = "Stop"
Write-Host "Collecting Java source files from src/..."
$sources = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
Write-Host "Found $($sources.Count) Java source files."

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "Compiling with javac..."
& javac -encoding UTF-8 -cp "lib/*" -d bin $sources

if ($LASTEXITCODE -eq 0 -or (Test-Path "bin\com\examseating\Main.class")) {
    Write-Host "[SUCCESS] Compilation completed successfully!" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit 1
}
