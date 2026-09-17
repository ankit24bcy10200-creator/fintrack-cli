@echo off
setlocal

set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

if exist "target\fintrack-cli.jar" (
    java -jar target\fintrack-cli.jar
) else if exist "target\classes" (
    java -cp "target\classes;lib\gson-2.10.1.jar" fintrack.cli.CliApp
) else (
    echo [ERROR] Project not built yet! Please run build.bat first.
    pause
)
