@echo off
chcp 65001 >nul
echo ====================================
echo   使用Vue2启动前端（临时方案）
echo ====================================
echo.
echo 说明：
echo 由于data-view大屏组件库与Vue3不兼容，
echo 暂时使用Vue2版本以确保正常运行。
echo.
echo 正在停止Vue3前端...
taskkill /F /IM node.exe >nul 2>&1
timeout /t 2 /nobreak >nul
echo.
echo 正在启动Vue2前端...
cd /d d:\jishe\1.19\ueit-ui
start /MIN "Frontend-Vue2" cmd /c "npm run dev"
timeout /t 3 /nobreak >nul
echo.
echo ====================================
echo   Vue2前端已启动
echo ====================================
echo 访问地址: http://localhost:8080
echo.
echo 如需切换回Vue3，运行: start-frontend-vue3.bat
pause
