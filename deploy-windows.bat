@echo off
chcp 65001 > nul
echo ========================================
echo       健康管理平台 - 部署脚本
echo ========================================
echo.

echo [1/6] 构建后端项目...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 后端构建失败！
    pause
    exit /b 1
)
echo 后端构建完成！
echo.

echo [2/6] 构建前端项目...
cd RuoYi-Vue3-Modern
call npm install --registry=https://registry.npmmirror.com
if %errorlevel% neq 0 (
    echo 前端依赖安装失败！
    pause
    exit /b 1
)
call npm run build:prod
if %errorlevel% neq 0 (
    echo 前端构建失败！
    pause
    exit /b 1
)
cd ..
echo 前端构建完成！
echo.

echo [3/6] 准备部署文件...
if not exist dist mkdir dist
if not exist dist\backend mkdir dist\backend
if not exist dist\frontend mkdir dist\frontend
echo 部署目录创建完成！
echo.

echo [4/6] 复制构建产物...
xcopy /E /I /Y qkyd-admin\target\qkyd-admin.jar dist\backend\
xcopy /E /I /Y RuoYi-Vue3-Modern\dist dist\frontend\
copy /Y docker-compose.yml dist\
copy /Y Dockerfile.backend dist\
copy /Y Dockerfile.frontend dist\
xcopy /E /I /Y docker dist\docker\
copy /Y deploy.env dist\
copy /Y application-prod.yml dist\
echo 构建产物复制完成！
echo.

echo [5/6] 创建部署说明...
echo # 部署说明 > dist\README.md
echo. >> dist\README.md
echo ## 服务器信息 >> dist\README.md
echo - 实例ID: lhins-nty01vai >> dist\README.md
echo - 公网IP: 212.64.84.112 >> dist\README.md
echo - 地域: ap-shanghai >> dist\README.md
echo. >> dist\README.md
echo ## 部署步骤 >> dist\README.md
echo 1. 将dist目录上传到服务器 >> dist\README.md
echo 2. 在服务器上执行: docker-compose up -d >> dist\README.md
echo 3. 访问 http://212.64.84.112 查看前端 >> dist\README.md
echo 4. 访问 http://212.64.84.112:8080 查看后端 >> dist\README.md
echo. >> dist\README.md
echo ## 端口说明 >> dist\README.md
echo - 前端: 80 >> dist\README.md
echo - 后端: 8080 >> dist\README.md
echo 部署说明创建完成！
echo.

echo ========================================
echo       构建完成！准备部署
echo ========================================
echo.
echo 部署文件已准备在 dist 目录
echo.
echo 下一步：上传 dist 目录到服务器
echo 服务器: 212.64.84.112
echo.
pause
