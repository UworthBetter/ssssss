@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title 耆康云盾 - 服务管理控制台
color 0A

:main
cls
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║           耆康云盾 - 服务管理控制台                        ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo 📊 当前端口状态:
echo.
call :check_port 8080 "前端服务"
call :check_port 8098 "后端服务"
call :check_port 5000 "Python算法服务"
echo.
echo ═════════════════════════════════════════════════════════════
echo.
echo 🎯 操作菜单:
echo.
echo   [1] 启动前端服务 (Vue3 - 8080)
echo   [2] 停止前端服务
echo.
echo   [3] 启动后端服务 (Java - 8098)
echo   [4] 停止后端服务
echo.
echo   [5] 启动Python算法服务 (5000)
echo   [6] 停止Python算法服务
echo.
echo   [7] 启动全部服务
echo   [8] 停止全部服务
echo.
echo   [9] 清理所有Node进程 (强制)
echo   [0] 退出
echo.
echo ═════════════════════════════════════════════════════════════
echo.

set /p choice=请选择操作 [0-9]: 

if "%choice%"=="1" goto start_frontend
if "%choice%"=="2" goto stop_frontend
if "%choice%"=="3" goto start_backend
if "%choice%"=="4" goto stop_backend
if "%choice%"=="5" goto start_python
if "%choice%"=="6" goto stop_python
if "%choice%"=="7" goto start_all
if "%choice%"=="8" goto stop_all
if "%choice%"=="9" goto kill_node
if "%choice%"=="0" goto exit

echo.
echo ❌ 无效选择，请重新输入
timeout /t 2 >nul
goto main

:start_frontend
cls
echo.
echo 🚀 正在启动前端服务...
echo.
cd /d "%~dp0RuoYi-Vue3-Modern"
start "前端服务-8080" cmd /k "npm run dev"
timeout /t 3 >nul
echo ✅ 前端服务启动命令已发送
echo.
pause
goto main

:stop_frontend
cls
echo.
echo 🛑 正在停止前端服务 (8080端口)...
echo.
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 .*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
    echo ✅ 已终止进程 PID: %%a
)
timeout /t 1 >nul
echo ✅ 前端服务已停止
echo.
pause
goto main

:start_backend
cls
echo.
echo 🚀 正在启动后端服务...
echo.
cd /d "%~dp0qkyd-admin"
start "后端服务-8098" cmd /k "mvn spring-boot:run"
echo ⏳ 后端服务启动中，请稍候 (约30秒)...
timeout /t 5 >nul
echo ✅ 后端服务启动命令已发送
echo.
pause
goto main

:stop_backend
cls
echo.
echo 🛑 正在停止后端服务 (8098端口)...
echo.
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8098 .*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
    echo ✅ 已终止进程 PID: %%a
)
timeout /t 1 >nul
echo ✅ 后端服务已停止
echo.
pause
goto main

:start_python
cls
echo.
echo 🚀 正在启动Python算法服务...
echo.
cd /d "%~dp0python-algorithms"
start "Python算法服务-5000" cmd /k "python main.py"
timeout /t 2 >nul
echo ✅ Python算法服务启动命令已发送
echo.
pause
goto main

:stop_python
cls
echo.
echo 🛑 正在停止Python算法服务 (5000端口)...
echo.
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5000 .*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
    echo ✅ 已终止进程 PID: %%a
)
timeout /t 1 >nul
echo ✅ Python算法服务已停止
echo.
pause
goto main

:start_all
cls
echo.
echo 🚀 正在启动全部服务...
echo.
call :start_frontend
timeout /t 2 >nul
call :start_backend
timeout /t 2 >nul
call :start_python
echo.
echo ✅ 全部服务启动完成！
echo.
pause
goto main

:stop_all
cls
echo.
echo 🛑 正在停止全部服务...
echo.
call :stop_frontend
timeout /t 1 >nul
call :stop_backend
timeout /t 1 >nul
call :stop_python
echo.
echo ✅ 全部服务已停止
echo.
pause
goto main

:kill_node
cls
echo.
echo ⚠️  正在强制终止所有Node进程...
echo.
taskkill /F /IM node.exe >nul 2>&1
if %errorlevel%==0 (
    echo ✅ 已终止所有Node进程
) else (
    echo ℹ️  没有运行中的Node进程
)
timeout /t 2 >nul
echo.
pause
goto main

:check_port
set port=%~1
set name=%~2
netstat -ano | findstr ":%port% .*LISTENING" >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%port% .*LISTENING" ^| findstr LISTENING') do (
        echo   [%name%] ✅ 运行中 (端口: %port%, PID: %%a)
    )
) else (
    echo   [%name%] ⭕ 未启动 (端口: %port%)
)
goto :eof

:exit
cls
echo.
echo 👋 感谢使用！
echo.
timeout /t 2 >nul
exit /b
