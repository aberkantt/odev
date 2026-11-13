@echo off
rem Testleri derler ve calistirir (Windows). Gerekli: JDK 17 veya ustu
cd /d "%~dp0"
if exist out rmdir /s /q out
javac -encoding UTF-8 -d out -sourcepath src src\hastane\test\SistemTestleri.java
if errorlevel 1 goto hata
java -cp out hastane.test.SistemTestleri
goto son
:hata
echo.
echo Derleme hatasi! JDK 17 veya ustu kurulu mu? Kontrol: javac -version
:son
pause
