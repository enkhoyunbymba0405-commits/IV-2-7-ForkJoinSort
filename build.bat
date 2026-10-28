@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
if exist sources.txt del sources.txt
rem Зам дотор зай байж болох тул мөр бүрийг хашилтанд авна (javac argfile).
for /r "%~dp0src" %%f in (*.java) do @echo "%%f">>sources.txt
echo "%~dp0tests\TestSuite.java">>sources.txt
javac -encoding UTF-8 -d out @sources.txt
set RC=%ERRORLEVEL%
if exist sources.txt del sources.txt
if not %RC%==0 (
  echo.
  echo KOMPAIL AMJILTGUI. Deerh aldaag harna uu.
  exit /b %RC%
)
echo Kompail amjilttai. Ajilluulah: run.bat
