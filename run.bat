@echo off
setlocal

echo ========================================================
echo Starting Smart Library Resource Management System
echo ========================================================

set "JAR_PATH=backend\target\smartlibrary-1.0.0.jar"

if not exist "%JAR_PATH%" (
    echo JAR not found. Building package with Maven...
    where mvn >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        set "MVN_CMD=mvn"
    ) else if exist "C:\Users\tamil\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" (
        set "MVN_CMD=C:\Users\tamil\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd"
    ) else if exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" (
        set "MVN_CMD=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd"
    ) else (
        echo Error: Maven executable could not be found to build the JAR.
        pause
        exit /b 1
    )
    "%MVN_CMD%" package -DskipTests -f backend/pom.xml
)

echo Starting application at http://localhost:8080 ...
start http://localhost:8080/index.html
java -jar "%JAR_PATH%"
pause
