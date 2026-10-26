@echo off
title Ulanzi TC001 Virtual Clock Simulator (Port 18080)
echo ========================================================
echo Starting Virtual Ulanzi TC001 Clock Simulator...
echo Listening on port 18080 (http://127.0.0.1:18080)
echo Inside Android emulator, connect to http://10.0.2.2:18080
echo ========================================================
python "%~dp0virtual_clock_simulator.py"
pause
