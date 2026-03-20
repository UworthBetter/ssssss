@echo off
chcp 65001
setlocal enabledelayedexpansion

echo.
echo ==========================================
echo      📊 项目结构检查
echo ==========================================
echo.

echo [INFO] 检查当前目录...
cd /d "D:\jishe\1.19"
dir /B | findstr /I "RuoYi\|frontend\|vue\|vite"

echo.
echo [INFO] 列出所有目录...
dir /B /A:D

echo.
echo [INFO] 查找package.json文件...
dir /B /S | findstr "package.json"

echo.
echo [INFO] 检查前端项目位置...
if exist "RuoYi-Vue3-Modern\package.json" (
    echo [OK] 找到前端项目: RuoYi-Vue3-Modern
) else if exist "frontend-service\package.json" (
    echo [OK] 找到前端项目: frontend-service
) else if exist "frontend\package.json" (
    echo [OK] 找到前端项目: frontend
) else (
    echo [WARN] 未找到前端项目package.json
)

echo.
echo [INFO] 检查后端项目...
if exist "qkyd-admin\pom.xml" (
    echo [OK] 找到后端项目: qkyd-admin
)

echo.
echo [INFO] 检查Python项目...
if exist "python-algorithms\main.py" (
    echo [OK] 找到Python项目: python-algorithms
)

echo.
echo ==========================================
echo      检查完成
echo ==========================================
echo.

pause
