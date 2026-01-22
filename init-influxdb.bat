@echo off
chcp 65001 >nul
echo ========================================
echo InfluxDB数据库初始化脚本
echo ========================================
echo.

echo 检查InfluxDB容器是否运行...
docker ps | findstr influxdb >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] InfluxDB未运行，请先执行 start-datastores.bat
    pause
    exit /b 1
)

echo.
echo 正在初始化InfluxDB数据库...
echo.

echo 执行命令: docker exec -it influxdb influx
echo.
echo 请在打开的终端中依次执行以下命令:
echo.
echo   1. CREATE USER admin WITH PASSWORD 'admin' WITH ALL PRIVILEGES
echo   2. CREATE DATABASE health_data
echo   3. CREATE RETENTION POLICY "one_year" ON "health_data" DURATION 365d REPLICATION 1 DEFAULT
echo   4. SHOW DATABASES
echo   5. SHOW USERS
echo   6. exit
echo.
echo ========================================
pause

docker exec -it influxdb influx

echo.
echo ========================================
echo InfluxDB初始化完成！
echo ========================================
echo.
pause
