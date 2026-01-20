@echo off
chcp 65001 >nul
echo ====================================
echo 测试后端启动
echo ====================================
echo.
echo 使用Java 21启动后端...
echo 按 Ctrl+C 停止
echo.
cd /d d:\jishe\1.19\ueit-backend-upgrade
"C:\Program Files (x86)\java\jdk-21.0.4+7\bin\java.exe" -jar ueit-admin.jar
