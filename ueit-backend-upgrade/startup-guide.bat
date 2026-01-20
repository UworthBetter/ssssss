@echo off
chcp 65001 >nul
cls
echo ====================================
echo    健康管理平台 - 启动选择
echo ====================================
echo.
echo [1] 测试启动升级版 (自动停止原版本)
echo [2] 启动后端 (升级版 - Spring Boot 3.2.2 + Java 17)
echo [3] 启动后端 (调试模式 - 查看详细日志)
echo [4] 重新构建项目
echo [5] 查看版本信息
echo [6] 退出
echo.
set /p choice=请选择操作 (1-5):

if "%choice%"=="1" (
    echo.
    echo 测试启动升级版（会自动停止原版本）...
    call test-startup.bat
)
if "%choice%"=="2" (
    echo.
    echo 正在启动后端...
    call start-backend.bat
)
if "%choice%"=="3" (
    echo.
    echo 正在启动后端 (调试模式)...
    call start-backend-debug.bat
)
if "%choice%"=="3" (
    echo.
    echo 正在重新构建项目...
    cd /d "%~dp0"
    mvn clean install -DskipTests
    copy ueit-admin\target\ueit-admin.jar .\ueit-admin.jar
    echo.
    echo 构建完成！
    pause
)
if "%choice%"=="4" (
    echo.
    echo ====================================
    echo 版本信息
    echo ====================================
    echo.
    echo [升级版 - ueit-backend-upgrade]
    echo   Spring Boot: 3.2.2
    echo   Java: 17
    echo   SpringDoc: 2.3.0
    echo.
    echo [原版本 - 父目录]
    echo   Spring Boot: 2.5.15
    echo   Java: 8
    echo   Swagger: 3.0.0
    echo.
    pause
)
if "%choice%"=="6" exit
