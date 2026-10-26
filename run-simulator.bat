@echo off
title Ulanzi TC001 Companion - Android Simulator Launcher
echo ========================================================
echo Checking environment and cleaning stale emulator locks...
echo ========================================================
taskkill /F /IM emulator.exe /T 2>nul
taskkill /F /IM qemu-system-x86_64.exe /T 2>nul
del /F /Q "%USERPROFILE%\.android\avd\medium_phone.avd\*.lock" 2>nul

echo ========================================================
echo Launching Android Virtual Device GUI window...
echo ========================================================
start "" "%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" -avd medium_phone

echo Waiting for emulator to start and report adb presence...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" wait-for-device

echo Waiting for Android user-space boot to complete...
:wait_boot
for /f "tokens=*" %%a in ('"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" shell getprop sys.boot_completed 2^>nul') do set BOOT_STATE=%%a
if not "%BOOT_STATE%"=="1" (
    timeout /t 2 /nobreak >nul
    goto wait_boot
)

echo Installing latest Companion App debug APK...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" install -r "%~dp0app\build\outputs\apk\debug\app-debug.apk"

echo Starting MainActivity...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" shell am start -n com.hackra.ulanzi.companion/.MainActivity

echo.
echo ========================================================
echo Application launched! The virtual phone window is open.
echo ========================================================
