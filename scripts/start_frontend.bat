@echo off
chcp 65001
setlocal enabledelayedexpansion

echo ==========================================
echo       🚀 启动前端服务 (Vue 3 + Vite)
echo ==========================================
echo.

cd /d "D:\jishe\1.19\RuoYi-Vue3-Modern"

echo [1/2] 安装依赖...
call npm install
if %errorlevel% neq 0 (
    echo [ERROR] npm install 失败
    pause
    exit /b 1
)

echo.
echo [2/2] 启动开发服务器...
call npm run dev

pause
