@echo off
setlocal

cd /d "%~dp0"

if exist classes rmdir /s /q classes
mkdir classes

dir /s /b src\*.java > sources.txt
javac -d classes @sources.txt
del sources.txt