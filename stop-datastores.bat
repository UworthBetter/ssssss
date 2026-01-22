@echo off
chcp 65001 >nul
echo ========================================
echo 耆康云盾 - 停止数据存储服务
echo ========================================
echo.

echo 确定要停止所有数据存储服务吗? (Y/N)
set /p confirm=
if /i not "%confirm%"=="Y" (
    echo [取消] 操作已取消
    pause
    exit /b 0
)

echo.
echo [1/3] 停止Redis...
docker ps | findstr redis >nul 2>&1
if %errorlevel% equ 0 (
    docker stop redis
    docker rm redis
    echo [成功] Redis已停止
) else (
    echo [信息] Redis未运行
)

echo.
echo [2/3] 停止InfluxDB...
docker ps | findstr influxdb >nul 2>&1
if %errorlevel% equ 0 (
    docker stop influxdb
    docker rm influxdb
    echo [成功] InfluxDB已停止
) else (
    echo [信息] InfluxDB未运行
)

echo.
echo [3/3] 停止MinIO...
docker ps | findstr minio >nul 2>&1
if %errorlevel% equ 0 (
    docker stop minio
    docker rm minio
    echo [成功] MinIO已停止
) else (
    echo [信息] MinIO未运行
)

echo.
echo ========================================
echo 所有数据存储服务已停止！
echo ========================================
echo.
pause
