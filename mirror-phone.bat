@echo off
title Ulanzi TC001 Companion - Phone Mirror
echo Connecting to Android Virtual Device (emulator-5554)...
scrcpy -s emulator-5554 --window-title "Ulanzi TC001 Companion - Live Screen" --always-on-top
