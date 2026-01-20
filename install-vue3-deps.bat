@echo off
chcp 65001 >nul
echo ====================================
echo   安装Vue3缺失的依赖包
echo ====================================
echo.
cd /d d:\jishe\1.19\RuoYi-Vue3-Modern

echo [1/2] 清理npm缓存...
npm cache clean --force
echo.

echo [2/2] 安装缺失依赖...
echo 正在安装 screenfull, data-view, amap等依赖...
echo.
npm install
echo.

echo ====================================
echo   安装完成！
echo ====================================
echo.
echo 现在可以启动前端了
pause
