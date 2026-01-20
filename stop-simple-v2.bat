@echo off
chcp 65001 >nul
echo ====================================
echo 停止所有服务（简化版）
echo ====================================
echo.

echo 正在停止后端（Java）...
taskkill /F /IM java.exe
echo.

echo 正在停止前端（Node.js）...
taskkill /F /IM node.exe
echo.

echo ====================================
echo 停止完成！
echo ====================================
echo.

pause
