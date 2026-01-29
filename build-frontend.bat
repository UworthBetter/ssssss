@echo off
chcp 65001 > nul
cd /d "d:\jishe\1.19\RuoYi-Vue3-Modern"

echo ========================================
echo     构建前端项目
echo ========================================
echo.

echo [1/2] 安装依赖...
call npm install --registry=https://registry.npmmirror.com
if %errorlevel% neq 0 (
    echo 依赖安装失败！
    pause
    exit /b 1
)
echo 依赖安装完成！
echo.

echo [2/2] 构建生产环境...
call npm run build:prod
if %errorlevel% neq 0 (
    echo 前端构建失败！
    pause
    exit /b 1
)
echo 前端构建完成！
echo.

echo ========================================
echo     构建成功！
echo ========================================
pause
