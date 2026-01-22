@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

:menu
cls
echo ====================================
echo     健康管理平台 - 启动菜单
echo ====================================
echo.
echo 当前运行状态检查...
echo.

:: 检查后端状态
netstat -ano | findstr ":8098" | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo   [后端] 运行中 (端口 8098)
) else (
    echo   [后端] 未运行
)

:: 检查前端状态
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo   [前端] 运行中 (端口 8080)
) else (
    echo   [前端] 未运行
)

echo.
echo ====================================
echo 请选择操作：
echo ====================================
echo.
echo [1] 启动后端 (升级版 - Spring Boot 3.2.2 + Java 17)
echo [2] 启动前端 (Vue3 + Vite - 端口 8080)
echo [3] 一键启动 (后端 + Vue3前端)
echo [4] 查看运行状态
echo [5] 停止所有服务
echo [6] 重新启动所有服务
echo [0] 退出
echo.
echo ====================================

set /p choice="请输入选项: "

if "%choice%"=="1" goto start-backend
if "%choice%"=="2" goto start-frontend-vue3
if "%choice%"=="3" goto start-all-vue3
if "%choice%"=="4" goto check-status
if "%choice%"=="5" goto stop-all
if "%choice%"=="6" goto restart-all
if "%choice%"=="0" goto exit
goto menu

:start-backend
echo.
echo 正在启动后端...
call d:\jishe\1.19\ueit-backend-upgrade\start-backend.bat
pause
goto menu

:start-frontend-vue3
echo.
echo 正在启动前端 (Vue3)...
cd /d d:\jishe\1.19\RuoYi-Vue3-Modern
start /MIN "Frontend" cmd /c "npm run dev"
timeout /t 3 /nobreak >nul
echo.
echo 前端已启动: http://localhost:8080
pause
goto menu

:start-all-vue3
echo.
echo 正在启动所有服务...
echo.
echo [1/2] 启动后端...
start /MIN "Backend" cmd /c "cd /d d:\jishe\1.19\ueit-backend-upgrade && \"C:\Program Files (x86)\java\jdk-21.0.4+7\bin\java.exe\" -jar ueit-admin.jar"
timeout /t 3 /nobreak >nul
echo.
echo [2/2] 启动前端 (Vue3)...
cd /d d:\jishe\1.19\RuoYi-Vue3-Modern
start /MIN "Frontend" cmd /c "npm run dev"
timeout /t 3 /nobreak >nul
echo.
echo ====================================
echo 启动完成！
echo 后端: http://localhost:8098
echo 前端: http://localhost:8080
echo 技术栈: Vue3 + Element Plus + Vite
echo ====================================
pause
goto menu

:check-status
echo.
echo ====================================
echo 当前运行状态
echo ====================================
echo.

echo 后端服务:
netstat -ano | findstr ":8098" | findstr "LISTENING"
if %errorlevel% equ 0 (
    echo   状态: 运行中
) else (
    echo   状态: 未运行
)

echo.
echo 前端服务:
netstat -ano | findstr ":8080" | findstr "LISTENING"
if %errorlevel% equ 0 (
    echo   状态: 运行中
) else (
    echo   状态: 未运行
)

echo.
echo MySQL 服务:
tasklist | findstr "mysqld.exe" >nul
if %errorlevel% equ 0 (
    echo   状态: 运行中
) else (
    echo   状态: 未运行
)

echo.
echo Redis 服务:
tasklist | findstr "redis-server.exe" >nul
if %errorlevel% equ 0 (
    echo   状态: 运行中
) else (
    echo   状态: 未运行
)

echo.
echo ====================================
pause
goto menu

:stop-all
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
goto menu

:restart-all
echo.
echo 正在停止所有服务...
echo 正在停止后端（Java）...
taskkill /F /IM java.exe
echo 正在停止前端（Node.js）...
taskkill /F /IM node.exe
timeout /t 2 /nobreak >nul
echo.
echo 正在重新启动...
goto start-all-vue3

:exit
exit
