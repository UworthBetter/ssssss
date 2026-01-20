@echo off
chcp 65001 >nul
echo ====================================
echo 健康管理平台 - 后端启动脚本 (调试模式)
echo ====================================
echo.
echo 版本信息:
echo   Spring Boot: 3.2.2
echo   Java: 17
echo.
echo 正在启动后端 (调试模式)...
echo.

set AppName=ueit-admin.jar
set JVM_OPTS="-Dname=%AppName% -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

java %JVM_OPTS% -jar %AppName%

echo.
echo 程序已退出
pause
