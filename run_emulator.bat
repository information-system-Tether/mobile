@echo off
title Android Emulator - Tether
echo ========================================================
echo  Zapusk Android Emulator (Medium_Phone)...
echo ========================================================
set ANDROID_SDK_ROOT=C:\Users\User\AppData\Local\Android\Sdk
set PATH=%ANDROID_SDK_ROOT%\emulator;%ANDROID_SDK_ROOT%\platform-tools;%PATH%

start "" "%ANDROID_SDK_ROOT%\emulator\emulator.exe" -avd Medium_Phone

echo.
echo Emulator zapushchen! Podozhdite zagruzki ekrana telefona...
echo Posle zagruzki prilozhenie Tether uzhe ustanovleno.
timeout /t 10
exit

