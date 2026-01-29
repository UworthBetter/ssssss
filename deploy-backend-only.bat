@echo off
chcp 65001 > nul
echo ========================================
echo       健康管理平台 - 后端部署准备
echo ========================================
echo.

if not exist dist mkdir dist
if not exist dist\backend mkdir dist\backend

echo [1/3] 复制后端jar包...
copy /Y "qkyd-admin\target\qkyd-admin.jar" "dist\backend\qkyd-admin.jar"
if %errorlevel% neq 0 (
    echo 复制jar包失败！请先运行 mvn clean package
    pause
    exit /b 1
)
echo jar包复制完成！
echo.

echo [2/3] 复制配置文件...
copy /Y docker-compose.yml dist\
copy /Y Dockerfile.backend dist\
copy /Y deploy.env dist\
copy /Y application-prod.yml dist\
xcopy /E /I /Y docker dist\docker\
echo 配置文件复制完成！
echo.

echo [3/3] 创建部署说明...
(
echo # 健康管理平台 - 后端部署指南
echo.
echo ## 服务器信息
echo - 实例ID: lhins-nty01vai
echo - 公网IP: 212.64.84.112
echo - 地域: ap-shanghai
echo.
echo ## 后端部署步骤
echo.
echo ### 1. 上传文件
echo 将 dist 目录下的所有文件上传到服务器：
echo ```bash
echo scp -r dist/* root@212.64.84.112:/root/qkyd/
echo ```
echo.
echo ### 2. 在服务器上执行
echo ```bash
echo ssh root@212.64.84.112
echo cd /root/qkyd
echo docker-compose up -d
echo ```
echo.
echo ### 3. 验证服务
echo ```bash
echo # 查看容器状态
echo docker-compose ps
echo.
echo # 查看后端日志
echo docker logs qkyd-backend
echo.
echo # 健康检查
echo curl http://localhost:8080/actuator/health
echo ```
echo.
echo ### 4. 访问应用
echo - 后端API: http://212.64.84.112:8080
echo.
echo ## 常用命令
echo ```bash
echo # 停止服务
echo docker-compose down
echo.
echo # 重启服务
echo docker-compose restart
echo.
echo # 查看日志
echo docker-compose logs -f backend
echo ```
echo.
echo ## 注意事项
echo 1. 首次部署需要在服务器上安装MySQL，或使用云数据库
echo 2. 修改 deploy.env 中的数据库配置
echo 3. 前端需要单独部署（使用Nginx或云托管）
echo 4. 确保服务器防火墙已开放80和8080端口
echo.
) > dist\README.md

echo 部署说明创建完成！
echo.

echo ========================================
echo       后端部署准备完成！
echo ========================================
echo.
echo 部署文件已准备在 dist 目录
echo.
echo 下一步：上传 dist 目录到服务器
echo 服务器: 212.64.84.112
echo.
pause
