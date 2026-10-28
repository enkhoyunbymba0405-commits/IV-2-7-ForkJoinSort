@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out call "%~dp0build.bat"
if not %ERRORLEVEL%==0 exit /b %ERRORLEVEL%
java -Xss16m -cp out forkjoinsort.Main %*
