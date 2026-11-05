@echo off
chcp 65001 > nul
cd /d "%~dp0"
echo ========================================================
echo Запуск микросервисов Tether Backend (Auth: 8001, Diary: 8002)
echo ========================================================
python backend\run_backend.py
pause

