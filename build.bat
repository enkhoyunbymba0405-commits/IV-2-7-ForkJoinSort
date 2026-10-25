@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
echo tests\TestSuite.java >> sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt
echo Kompail amjilttai. Ajilluulah: run.bat
