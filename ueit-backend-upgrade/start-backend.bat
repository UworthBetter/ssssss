@echo off
chcp 65001 >nul
echo ====================================
echo 健康管理平台 - 后端启动脚本 (升级版)
echo ====================================
echo.
echo 版本信息:
echo   Spring Boot: 3.2.2
echo   Java: 21
echo.
echo 正在启动后端...
echo.

set AppName=ueit-admin.jar
set JAVA_HOME=C:\Program Files (x86)\java\jdk-21.0.4+7
set JVM_OPTS="-Dname=%AppName% -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

"%JAVA_HOME%\bin\java" %JVM_OPTS% -jar %AppName%

echo 启动成功！
echo.
echo 访问地址:
echo   后端 API: http://localhost:8098
echo   API 文档: http://localhost:8098/swagger-ui.html
echo.
