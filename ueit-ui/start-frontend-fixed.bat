@echo off
chcp 65001 >nul
echo ====================================
echo 健康管理平台 - 前端启动脚本
echo ====================================
echo.
echo 后端地址: http://localhost:8098
echo 前端地址: http://localhost
echo.
echo 正在启动前端开发服务器...
echo.

cd /d "%~dp0"

if not exist "node_modules" (
    echo ====================================
    echo 检测到未安装依赖，正在安装...
    echo ====================================
    call npm install --registry=https://registry.npmmirror.com
    if errorlevel 1 (
        echo.
        echo ====================================
        echo 依赖安装失败！
        echo ====================================
        pause
        exit /b 1
    )
    echo.
    echo ====================================
    echo 依赖安装完成！
    echo ====================================
    echo.
)

echo 设置 Node.js 兼容性环境变量...
set NODE_OPTIONS=--openssl-legacy-provider

echo.
echo ====================================
echo   正在启动前端...
echo ====================================
echo.
echo 浏览器会自动打开
echo 如果没有自动打开，请手动访问: http://localhost
echo.
echo 按 Ctrl+C 可以停止
echo.

call npm run dev

echo.
echo ====================================
echo   前端已停止
echo ====================================
pause
