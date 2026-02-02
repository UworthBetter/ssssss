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

echo ========================================
echo [步骤1] 使用PowerShell自动接受主机密钥...
echo ========================================
echo.

powershell -Command "& {$server='%SERVER_IP%'; $user='%SERVER_USER%'; $pw='%SERVER_PASSWORD%'; $pass=$pw | ConvertTo-SecureString -AsPlainText -Force; $cred=New-Object System.Management.Automation.PSCredential($user,$pass); $session=New-SSHSession -ComputerName $server -Credential $cred -AcceptKey; Remove-SSHSession $session}"

if %errorlevel% neq 0 (
    echo [错误] 使用PowerShell失败,尝试plink方法...
    echo.
    echo ========================================
    echo [步骤1] 手动接受主机密钥
    echo ========================================
    echo.
    echo 请运行以下命令来接受主机密钥:
    echo plink root@212.64.84.112
    echo.
    echo 在提示 "The server's ssh-ed25519 key fingerprint is:" 时
    echo 输入 "y" 然后输入密码: %SERVER_PASSWORD%
    echo.
    echo 完成后按任意键继续...
    pause
)

echo.
echo ========================================
echo [步骤2] 检查文件...
echo ========================================
echo.

set "LOCAL_JAR=d:\jishe\1.19\qkyd-admin\target\qkyd-admin.jar"
set "LOCAL_CONFIG=d:\jishe\1.19\application-prod.yml"

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

echo 文件检查完成!
echo.

echo ========================================
echo [步骤3] 上传文件...
echo ========================================
echo.

echo 上传后端jar包...
pscp -batch -pw %SERVER_PASSWORD% "%LOCAL_JAR%" %SERVER_USER%@%SERVER_IP%:/root/qkyd/backend/
if %errorlevel% neq 0 (
    echo [错误] jar包上传失败!
    pause
    exit /b 1
)
echo jar包上传成功!
echo.

echo 上传配置文件...
pscp -batch -pw %SERVER_PASSWORD% "%LOCAL_CONFIG%" %SERVER_USER%@%SERVER_IP%:/root/qkyd/
if %errorlevel% neq 0 (
    echo [错误] 配置文件上传失败!
    pause
    exit /b 1
)
echo 配置文件上传成功!
echo.

echo ========================================
echo [步骤4] 准备docker-compose.yml...
echo ========================================
echo.

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

echo 上传docker-compose.yml...
pscp -batch -pw %SERVER_PASSWORD% d:\jishe\1.19\dist\docker-compose-backend.yml %SERVER_USER%@%SERVER_IP%:/root/qkyd/docker-compose.yml
if %errorlevel% neq 0 (
    echo [错误] docker-compose.yml上传失败!
    pause
    exit /b 1
)
echo docker-compose.yml上传成功!
echo.

echo ========================================
echo [步骤5] 启动服务...
echo ========================================
echo.

echo 准备服务器环境...
plink -batch -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "mkdir -p /root/qkyd/{backend,logs,upload,config} && mv /root/qkyd/backend/qkyd-admin.jar /root/qkyd/backend/app.jar"
if %errorlevel% neq 0 (
    echo [错误] 环境准备失败!
    pause
    exit /b 1
)

echo 启动Docker容器...
plink -batch -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "cd /root/qkyd && docker-compose down && docker-compose up -d"
if %errorlevel% neq 0 (
    echo [错误] 启动容器失败!
    pause
    exit /b 1
)

echo 容器启动成功!
echo 等待服务启动...
timeout /t 30 /nobreak >nul

echo.
echo 检查容器状态...
plink -batch -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "docker ps | grep qkyd-backend"

echo.
echo 检查服务健康状态...
plink -batch -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "curl -s http://localhost:8080/actuator/health || echo 服务还在启动中..."

echo.
echo ========================================
echo 部署完成!
echo ========================================
echo.
echo 访问地址:
echo - 后端API: http://%SERVER_IP%:8080
echo - 健康检查: http://%SERVER_IP%:8080/actuator/health
echo.
pause
