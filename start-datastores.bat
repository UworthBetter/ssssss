@echo off
chcp 65001 >nul
echo ========================================
echo 耆康云盾 - 数据存储服务启动脚本
echo ========================================
echo.

echo [1/3] 检查Docker环境...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Docker，请先安装Docker Desktop
    pause
    exit /b 1
)
echo [成功] Docker已安装
echo.

echo [2/3] 启动Redis服务...
docker ps | findstr redis >nul 2>&1
if %errorlevel% neq 0 (
    docker run -d -p 6379:6379 --name redis redis:7-alpine
    echo [成功] Redis已启动 (端口: 6379)
) else (
    echo [信息] Redis已在运行
)
echo.

echo [3/3] 启动InfluxDB服务...
docker ps | findstr influxdb >nul 2>&1
if %errorlevel% neq 0 (
    docker run -d ^
      -p 8086:8086 ^
      -p 8083:8083 ^
      -p 8090:8090 ^
      --name influxdb ^
      influxdb:1.8-alpine
    echo [成功] InfluxDB已启动
    echo         - HTTP API: http://localhost:8086
    echo         - Web UI:   http://localhost:8083
    echo         - 集群端口: 8090
    timeout /t 3 /nobreak >nul
    echo.
    echo [信息] 正在初始化数据库...
    echo [请手动执行以下命令创建管理员账号和数据库:]
    echo   1. docker exec -it influxdb influx
    echo   2. CREATE USER admin WITH PASSWORD 'admin' WITH ALL PRIVILEGES
    echo   3. CREATE DATABASE health_data
    echo   4. CREATE RETENTION POLICY "one_year" ON "health_data" DURATION 365d REPLICATION 1 DEFAULT
    echo   5. exit
) else (
    echo [信息] InfluxDB已在运行
)
echo.

echo [4/4] 启动MinIO服务...
docker ps | findstr minio >nul 2>&1
if %errorlevel% neq 0 (
    docker run -d ^
      -p 9000:9000 ^
      -p 9001:9001 ^
      --name minio ^
      -e "MINIO_ROOT_USER=minioadmin" ^
      -e "MINIO_ROOT_PASSWORD=minioadmin" ^
      minio/minio server /data --console-address ":9001"
    echo [成功] MinIO已启动
    echo         - API:  http://localhost:9000
    echo         - 控制台:http://localhost:9001
    echo         - 账号: minioadmin / minioadmin
) else (
    echo [信息] MinIO已在运行
)
echo.

echo ========================================
echo 所有数据存储服务启动完成！
echo ========================================
echo.
echo 服务状态:
echo [✓] Redis      : http://localhost:6379
echo [✓] InfluxDB   : http://localhost:8086
echo [✓] MinIO      : http://localhost:9000
echo.
echo 可用命令:
echo   查看所有容器: docker ps
echo   停止所有服务: stop-datastores.bat
echo   查看服务日志: docker logs [容器名称]
echo.
pause
