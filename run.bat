@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out call build.bat
java -Xss16m -cp out forkjoinsort.Main %*
