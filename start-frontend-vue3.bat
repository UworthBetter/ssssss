@echo off
chcp 65001 >nul
echo ====================================
echo   启动前端 - Vue3 + Vite
echo ====================================
echo.
echo 正在检查依赖...

cd /d d:\jishe\1.19\RuoYi-Vue3-Modern

if not exist "node_modules" (
    echo.
    echo 首次启动，正在安装依赖...
    echo.
    npm install
    echo.
    echo 依赖安装完成！
    echo.
)

echo.
echo 正在启动前端服务...
echo 端口: 8080
echo 后端API: http://localhost:8098
echo.
echo 按 Ctrl+C 停止服务
echo ====================================
echo.

npm run dev
pause
