@echo off
chcp 65001 >nul
echo ========================================
echo    健康管理平台 - 云服务器自动部署
echo ========================================
echo.

set "SERVER_IP=212.64.84.112"
set "SERVER_USER=root"
set "SERVER_PASSWORD=M)D4k_3udwVN?"

echo [服务器信息]
echo IP: %SERVER_IP%
echo 实例ID: lhins-nty01vai
echo 地域: ap-shanghai
echo.

echo 检查PuTTY工具...
where pscp >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到pscp工具!
    echo 请安装PuTTY: https://www.chiark.greenend.org.uk/~sgtatham/putty/
    echo 或将PuTTY安装目录添加到系统PATH环境变量
    pause
    exit /b 1
)

where plink >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到plink工具!
    echo 请安装PuTTY: https://www.chiark.greenend.org.uk/~sgtatham/putty/
    echo 或将PuTTY安装目录添加到系统PATH环境变量
    pause
    exit /b 1
)

echo PuTTY工具检查完成!
echo.

echo.
echo ========================================
echo [1/5] 准备上传文件...
echo ========================================
echo.

set "LOCAL_JAR=d:\jishe\1.19\qkyd-admin\target\qkyd-admin.jar"
set "LOCAL_CONFIG=d:\jishe\1.19\application-prod.yml"
set "LOCAL_COMPOSE=d:\jishe\1.19\dist\docker-compose.yml"

if not exist "%LOCAL_JAR%" (
    echo [错误] 后端jar包不存在: %LOCAL_JAR%
    echo 请先执行构建: mvn clean package -DskipTests
    pause
    exit /b 1
)

if not exist "%LOCAL_CONFIG%" (
    echo [错误] 配置文件不存在: %LOCAL_CONFIG%
    pause
    exit /b 1
)

if not exist "%LOCAL_COMPOSE%" (
    echo [错误] docker-compose文件不存在: %LOCAL_COMPOSE%
    pause
    exit /b 1
)

echo 文件检查完成!
echo.

echo ========================================
echo [2/5] 上传后端jar包...
echo ========================================
pscp -batch -pw %SERVER_PASSWORD% "%LOCAL_JAR%" %SERVER_USER%@%SERVER_IP%:/root/qkyd/backend/
if %errorlevel% neq 0 (
    echo [错误] jar包上传失败!
    pause
    exit /b 1
)
echo jar包上传成功!
echo.

echo ========================================
echo [3/5] 上传配置文件...
echo ========================================
pscp -batch -pw %SERVER_PASSWORD% "%LOCAL_CONFIG%" %SERVER_USER%@%SERVER_IP%:/root/qkyd/
if %errorlevel% neq 0 (
    echo [错误] 配置文件上传失败!
    pause
    exit /b 1
)
echo 配置文件上传成功!
echo.

pscp -batch -pw %SERVER_PASSWORD% "%LOCAL_COMPOSE%" %SERVER_USER%@%SERVER_IP%:/root/qkyd/
if %errorlevel% neq 0 (
    echo [错误] docker-compose文件上传失败!
    pause
    exit /b 1
)
echo docker-compose文件上传成功!
echo.

echo ========================================
echo [4/5] 在服务器上启动服务...
echo ========================================
echo.

echo 正在创建docker-compose.yml...
if not exist d:\jishe\1.19\dist\docker-compose-backend.yml (
    (
        echo version: '3.8'
        echo.
        echo services:
        echo   backend:
        echo     image: eclipse-temurin:17-jre-jammy
        echo     container_name: qkyd-backend
        echo     restart: always
        echo     ports:
        echo       - "8080:8080"
        echo     environment:
        echo       - SPRING_PROFILES_ACTIVE=prod
        echo       - TZ=Asia/Shanghai
        echo       - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
        echo     volumes:
        echo       - /root/qkyd/logs:/app/logs
        echo       - /root/qkyd/upload:/app/upload
        echo       - /root/qkyd/config:/app/config
        echo       - /root/qkyd/application-prod.yml:/app/application.yml
        echo       - /root/qkyd/backend/app.jar:/app/app.jar
        echo     command: sh -c "java $$JAVA_OPTS -jar /app/app.jar --spring.config.location=classpath:/,file:/app/"
        echo     networks:
        echo       - qkyd-network
        echo     healthcheck:
        echo       test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
        echo       interval: 30s
        echo       timeout: 3s
        echo       retries: 3
        echo       start_period: 60s
        echo.
        echo networks:
        echo   qkyd-network:
        echo     driver: bridge
    ) > d:\jishe\1.19\dist\docker-compose-backend.yml
)

echo docker-compose.yml创建完成!
echo.

echo 上传docker-compose.yml...
pscp -batch -pw %SERVER_PASSWORD% d:\jishe\1.19\dist\docker-compose-backend.yml %SERVER_USER%@%SERVER_IP%:/root/qkyd/docker-compose.yml
if %errorlevel% neq 0 (
    echo [错误] docker-compose.yml上传失败!
    pause
    exit /b 1
)
echo docker-compose.yml上传成功!
echo.

echo 正在连接服务器...
plink -batch -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "mkdir -p /root/qkyd/{backend,logs,upload,config} && mv /root/qkyd/backend/qkyd-admin.jar /root/qkyd/backend/app.jar"

if %errorlevel% neq 0 (
    echo [错误] 创建目录和重命名jar包失败!
    pause
    exit /b 1
)

echo 目录准备完成!
echo.

echo 停止旧容器并启动新容器...
plink -batch -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "cd /root/qkyd && docker-compose down && docker-compose up -d"

if %errorlevel% neq 0 (
    echo [错误] 启动容器失败!
    pause
    exit /b 1
)

echo 容器启动成功!
echo.

echo 容器启动中,等待30秒...
timeout /t 30 /nobreak >nul

echo.
echo 检查容器状态:
plink -batch -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "docker ps | grep qkyd-backend"

echo.
echo 检查服务健康状态:
plink -batch -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "curl -s http://localhost:8080/actuator/health || echo 服务还在启动中..."

echo.
echo ========================================
echo [5/5] 部署完成!
echo ========================================
echo.
echo 部署已完成! 请访问以下地址验证:
echo - 后端API: http://%SERVER_IP%:8080
echo - 健康检查: http://%SERVER_IP%:8080/actuator/health
echo.
echo 查看日志:
echo docker logs qkyd-backend
echo.
echo 管理命令:
echo pscp -pw %SERVER_PASSWORD% file.txt %SERVER_USER%@%SERVER_IP%:/root/
echo plink -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "cd /root/qkyd && docker-compose ps"
echo plink -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "docker logs -f qkyd-backend"
echo.
pause
