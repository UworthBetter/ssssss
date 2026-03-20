@echo off
chcp 65001
setlocal enabledelayedexpansion

echo.
echo ==========================================
echo      🚀 完整测试和修复流程
echo ==========================================
echo.

set startTime=%time%

echo ==========================================
echo      Phase 1: 添加数据库索引
echo ==========================================
echo.

echo [INFO] 停止所有Java进程...
taskkill /F /IM "java.exe" /FI "WINDOWTITLE eq *qkyd*" 2>nul
taskkill /F /IM "java.exe" /FI "WINDOWTITLE eq *mysql*" 2>nul

echo [OK] Java进程已停止
echo.

echo [INFO] 添加数据库索引...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -P 3306 -h 127.0.0.1 -D qkyd_health -e "CREATE INDEX IF NOT EXISTS idx_user_time ON health_subject(user_id, record_time); CREATE INDEX IF NOT EXISTS idx_user_id ON health_subject(user_id); CREATE INDEX IF NOT EXISTS idx_record_time ON health_subject(record_time); SHOW INDEX FROM health_subject;"

if errorlevel 1 (
    echo [ERROR] 索引添加失败
    echo [INFO] 可能原因：
    echo 1. MySQL服务未启动
    echo 2. 数据库qkyd_health不存在
    echo 3. 表health_subject不存在
    echo 4. 用户名密码错误
    echo.
    echo [ACTION] 请手动在MySQL Workbench中执行以下SQL：
    echo.
    echo USE qkyd_health;
    echo CREATE INDEX IF NOT EXISTS idx_user_time ON health_subject(user_id, record_time);
    echo CREATE INDEX IF NOT EXISTS idx_user_id ON health_subject(user_id);
    echo CREATE INDEX IF NOT EXISTS idx_record_time ON health_subject(record_time);
    echo SHOW INDEX FROM health_subject;
) else (
    echo [OK] 索引添加成功！
)

echo.
timeout /t 2 /nobreak >nul

echo ==========================================
echo      Phase 2: 启动后端服务
echo ==========================================
echo.

echo [INFO] 后端jar路径: D:\jishe\1.19\qkyd-admin\target\qkyd-admin.jar
echo [INFO] Java路径: C:\Program Files (x86)\Java\jdk-21.0.4+7\bin\java.exe

if exist "D:\jishe\1.19\qkyd-admin\target\qkyd-admin.jar" (
    if exist "C:\Program Files (x86)\Java\jdk-21.0.4+7\bin\java.exe" (
        echo [INFO] 启动后端服务...
        start "" "C:\Program Files (x86)\Java\jdk-21.0.4+7\bin\java.exe" -jar "D:\jishe\1.19\qkyd-admin\target\qkyd-admin.jar" -Dserver.port=8098
        
        echo [OK] 后端服务启动中...
        echo [INFO] 日志: D:\jishe\1.19\logs\backend.log
        echo [INFO] 端口: 8098
        
        timeout /t 15 /nobreak >nul
        
        echo [INFO] 等待后端服务完全启动...
    ) else (
        echo [ERROR] Java路径不存在
        echo [PATH] C:\Program Files (x86)\Java\jdk-21.0.4+7\bin\java.exe
    )
) else (
    echo [ERROR] 后端jar文件不存在
    echo [PATH] D:\jishe\1.19\qkyd-admin\target\qkyd-admin.jar
)

echo.

echo ==========================================
echo      Phase 3: 启动前端服务
echo ==========================================
echo.

echo [INFO] 前端项目: RuoYi-Vue3-Modern

if exist "RuoYi-Vue3-Modern\package.json" (
    echo [INFO] 前端项目存在
) else if exist "frontend-service\package.json" (
    echo [INFO] 前端项目存在于 frontend-service
) else (
    echo [WARN] 未找到前端项目
    goto skip_frontend
)

echo [INFO] 启动前端开发服务器...
cd /d D:\jishe\1.19

start "" cmd /c "cd RuoYi-Vue3-Modern && npm run dev"

echo [OK] 前端服务启动中...
echo [INFO] 端口: 8080
echo [INFO] 开发服务器: http://localhost:8080

timeout /t 10 /nobreak >nul

:skip_frontend

echo.

echo ==========================================
echo      Phase 4: 测试和验证
echo ==========================================
echo.

echo [INFO] 等待15秒让服务完全启动...
timeout /t 15 /nobreak >nul

echo.
echo [INFO] 测试后端服务 (http://localhost:8098)...
echo [INFO] 测试前端服务 (http://localhost:8080)...

echo.
echo ==========================================
echo      验证结果
echo ==========================================
echo.

echo [INFO] 请手动验证以下URL：
echo.
echo 后端API: http://localhost:8098/health/subject/list?userId=1
echo 前端主页: http://localhost:8080
echo.
echo [INFO] 预期结果：
echo - 后端响应时间应该 <200ms（有索引后）
echo - 前端页面应该可以正常访问
echo.

echo.
echo ==========================================
echo      完成
echo ==========================================
echo.

set endTime=%time%

echo [INFO] 总耗时: %startTime% - %endTime%

echo.
echo ==========================================
echo      💡 使用提示
echo ==========================================
echo.
echo 1. 如果后端启动失败，检查日志: D:\jishe\1.19\logs\backend.log
echo 2. 如果前端启动失败，检查控制台错误
echo 3. 如果API仍然慢，检查索引是否成功创建
echo 4. 使用MySQL Workbench验证索引: SHOW INDEX FROM health_subject;
echo.

pause
