@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo        FinTrack CLI - Build and Test Script
echo ========================================================

set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

if exist "C:\Program Files\Java\jdk-24\bin\jar.exe" (
    set "PATH=C:\Program Files\Java\jdk-24\bin;%PATH%"
)

set MVN_CMD=""
if exist "%SCRIPT_DIR%tools\apache-maven-3.9.6\bin\mvn.cmd" (
    set MVN_CMD="%SCRIPT_DIR%tools\apache-maven-3.9.6\bin\mvn.cmd"
) else (
    where mvn >nul 2>nul
    if !errorlevel! equ 0 (
        set MVN_CMD=mvn
    )
)

if not !MVN_CMD!=="" (
    echo [INFO] Building with Maven: !MVN_CMD!
    call !MVN_CMD! clean package
    if !errorlevel! equ 0 (
        echo [SUCCESS] Maven build and test suite succeeded!
        goto :END
    ) else (
        echo [WARN] Maven build failed or interrupted, falling back to standalone javac build...
    )
)

echo [INFO] Compiling using standalone Java Compiler (javac)...
if not exist "target\classes" mkdir "target\classes"
if not exist "target\test-classes" mkdir "target\test-classes"

:: Gather source files
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -cp "lib\gson-2.10.1.jar" -d target\classes @sources.txt
if !errorlevel! neq 0 (
    echo [ERROR] Source compilation failed!
    del sources.txt
    exit /b 1
)
del sources.txt
echo [SUCCESS] Main classes compiled to target\classes

:: Compile test files
dir /s /b src\test\java\*.java > test-sources.txt
javac -encoding UTF-8 -cp "target\classes;lib\gson-2.10.1.jar;lib\junit-platform-console-standalone-1.10.2.jar" -d target\test-classes @test-sources.txt
if !errorlevel! neq 0 (
    echo [ERROR] Test compilation failed!
    del test-sources.txt
    exit /b 1
)
del test-sources.txt
echo [SUCCESS] Test classes compiled to target\test-classes

:: Run tests via JUnit Console Standalone
echo [INFO] Running JUnit 5 Test Suite...
java -jar lib\junit-platform-console-standalone-1.10.2.jar --class-path "target\classes;target\test-classes;lib\gson-2.10.1.jar" --scan-class-path --fail-if-no-tests
if !errorlevel! neq 0 (
    echo [ERROR] Some unit tests failed!
    exit /b 1
)
echo [SUCCESS] All unit tests passed!

:: Package fat runnable JAR
echo [INFO] Packaging runnable JAR target\fintrack-cli.jar...
cd target\classes
jar -xf "..\..\lib\gson-2.10.1.jar"
jar -cfe "..\fintrack-cli.jar" fintrack.cli.CliApp *
cd ..\..
echo [SUCCESS] Executable JAR built: target\fintrack-cli.jar

:END
echo ========================================================
echo Build complete. Run with: run.bat
echo ========================================================
