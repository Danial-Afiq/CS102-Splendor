@echo off
setlocal

cd /d "%~dp0"
call compile.bat
java -cp classes splendor.app.Main