@echo off
chcp 65001 >nul
cd /d "%~dp0"
call "%~dp0build.bat"
if not "%ERRORLEVEL%"=="0" exit /b %ERRORLEVEL%
java -Xss16m -Djava.awt.headless=true -cp out TestSuite
exit /b %ERRORLEVEL%
