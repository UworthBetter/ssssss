@echo off
chcp 65001 >nul
cls
echo ====================================
echo 启动升级版本后端
echo ====================================
echo.

echo 1. 检查并停止原版本...
for /f "usebackq tokens=1" %%a in ('jps -l ^| findstr ueit-admin.jar') do (
    set old_pid=%%a
)

if defined old_pid (
    echo 发现原版本运行中，PID: %old_pid%
    taskkill /F /PID %old_pid% >nul 2>&1
    echo 已停止原版本
    timeout /t 2 /nobreak >nul
)

echo.
echo 2. 检查升级版本是否在运行...
for /f "usebackq tokens=1" %%a in ('jps -l ^| findstr ueit-admin.jar') do (
    set pid=%%a
)

if defined pid (
    echo 警告：升级版本已经在运行中，PID: %pid%
    echo 先停止...
    taskkill /F /PID %pid% >nul 2>&1
    timeout /t 2 /nobreak >nul
)

echo.
echo 3. 启动升级版本...
echo 日志将显示在新窗口中
echo.

set JVM_OPTS="-Dname=ueit-admin -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

start cmd /k "title UEIT Backend Upgrade - Spring Boot 3.2.2" cmd /c "java %JVM_OPTS% -jar ueit-admin.jar && pause"

echo.
echo 4. 等待启动...
timeout /t 5 /nobreak >nul

echo.
echo 5. 检查启动状态...
for /f "usebackq tokens=1" %%a in ('jps -l ^| findstr ueit-admin.jar') do (
    set new_pid=%%a
)

if defined new_pid (
    echo.
    echo ====================================
    echo   启动成功
    echo ====================================
    echo.
    echo PID: %new_pid%
    echo.
    echo 访问地址:
    echo   后端 API:   http://localhost:8098
    echo   API 文档:   http://localhost:8098/swagger-ui.html
    echo   Druid 监控: http://localhost:8098/druid
    echo.
) else (
    echo.
    echo ====================================
    echo   启动失败
    echo ====================================
    echo.
    echo 请检查:
    echo   1. Java 版本是否为 17 或更高
    echo   2. MySQL 是否启动
    echo   3. Redis 是否启动
    echo.
)

echo.
pause
