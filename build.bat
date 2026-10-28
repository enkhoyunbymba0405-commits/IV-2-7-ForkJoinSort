@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
if exist sources.txt del sources.txt
rem ХАРЬЦАНГУЙ зам бичнэ: argfile дотор хашилт хэрэглэвэл "\" нь escape болдог
rem (src\forkjoinsort -> \f = formfeed), мөн харьцангуй замд зай байхгүй.
set "ROOT=%~dp0"
for /r "%~dp0src" %%f in (*.java) do (
  set "P=%%f"
  set "P=!P:%ROOT%=!"
  echo !P!>>sources.txt
)
echo tests\TestSuite.java>>sources.txt
javac -encoding UTF-8 -d out @sources.txt
set RC=%ERRORLEVEL%
if exist sources.txt del sources.txt
if not "%RC%"=="0" (
  echo.
  echo KOMPAIL AMJILTGUI. Deerh aldaag harna uu.
  exit /b %RC%
)
echo Kompail amjilttai. Ajilluulah: run.bat
exit /b 0
