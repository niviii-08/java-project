#!/bin/bash
echo "============================================"
echo " Pathfinder - Career Guidance System"
echo " Build & Run Script (Linux / macOS)"
echo "============================================"

# Check Java
if ! command -v java &>/dev/null; then
    echo "ERROR: Java not found. Install JDK 17+."
    echo "  Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "  macOS:         brew install openjdk@17"
    exit 1
fi

# Check JDBC jar
if [ ! -f "lib/mysql-connector-java.jar" ]; then
    echo "ERROR: MySQL JDBC driver not found."
    echo "Download from: https://dev.mysql.com/downloads/connector/j/"
    echo "Place it as:   lib/mysql-connector-java.jar"
    exit 1
fi

echo "[1/3] Cleaning old build..."
rm -rf out && mkdir out

echo "[2/3] Compiling source files..."
find src -name "*.java" > sources.txt
javac -cp "lib/mysql-connector-java.jar" -d out @sources.txt
STATUS=$?
rm sources.txt

if [ $STATUS -ne 0 ]; then
    echo "COMPILATION FAILED"
    exit 1
fi

echo "[3/3] Running Pathfinder..."
java -cp "out:lib/mysql-connector-java.jar" com.pathfinder.Main
