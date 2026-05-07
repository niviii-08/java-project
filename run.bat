@echo off
echo ============================================
echo  Pathfinder - Career Guidance System
echo  Build and Run Script (Windows)
echo ============================================

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Install JDK 17 or higher.
    pause
    exit /b 1
)

REM Check for MySQL JDBC driver
if not exist "lib\mysql-connector-java.jar" (
    echo ERROR: MySQL JDBC driver not found.
    echo Download mysql-connector-java-8.x.x.jar from:
    echo https://dev.mysql.com/downloads/connector/j/
    echo Place it in the lib\ folder as mysql-connector-java.jar
    pause
    exit /b 1
)

echo [1/3] Cleaning old build...
if exist out rmdir /s /q out
mkdir out

echo [2/3] Compiling source files...
dir /s /b src\*.java > sources.txt
javac -cp "lib\mysql-connector-java.jar" -d out @sources.txt
if errorlevel 1 (
    echo COMPILATION FAILED
    del sources.txt
    pause
    exit /b 1
)
del sources.txt

echo [3/3] Running Pathfinder...
java -cp "out;lib\mysql-connector-java.jar" com.pathfinder.Main

pause
