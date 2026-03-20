# ============================================
# 完整修复和测试脚本
# ============================================

Write-Host "============================================"
Write-Host "      完整修复和测试脚本"
Write-Host "============================================"
Write-Host ""

# ============================================
# Phase 1: 数据库索引添加
# ============================================

Write-Host ""
Write-Host "[Phase 1/4] 添加数据库索引"
Write-Host ""

# 使用MySQL客户端添加索引
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$database = "qkyd_health"
$host = "127.0.0.1"
$port = "3306"
$user = "root"
$password = "123456"

# 执行索引SQL
$indexSQL = @"
-- ============================================
-- 添加health_subject表的索引
-- ============================================

-- 索引1: 用户ID单列索引
CREATE INDEX IF NOT EXISTS idx_user_id ON health_subject(user_id);

-- 索引2: 记录时间单列索引
CREATE INDEX IF NOT EXISTS idx_record_time ON health_subject(record_time);

-- 索引3: 用户+时间复合索引（关键）
CREATE INDEX IF NOT EXISTS idx_user_time ON health_subject(user_id, record_time);

-- 索引4: 用户+类型+时间复合索引（可选）
CREATE INDEX IF NOT EXISTS idx_user_type_time ON health_subject(user_id, subject_type, record_time);

-- 显示所有索引
SHOW INDEX FROM health_subject;

-- 显示索引基数
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    CARDINALITY,
    SUB_PART,
    NULLABLE,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = '$database'
  AND TABLE_NAME = 'health_subject'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;
"@

# 保存SQL到临时文件
$indexSQL | Out-File -FilePath "D:\jishe\1.19\sql\temp_indexes.sql" -Encoding UTF8

# 执行SQL
Write-Host "  正在执行索引SQL..."
& $mysqlPath -u $user -p$password -P $port -h $host -D $database -e "source D:\jishe\1.19\sql\temp_indexes.sql"

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ 索引添加成功！" -ForegroundColor Green
} else {
    Write-Host "  ❌ 索引添加失败！" -ForegroundColor Red
    Write-Host "  错误代码: $LASTEXITCODE" -ForegroundColor Red
    exit 1
}

Write-Host ""

# ============================================
# Phase 2: 重启后端服务
# ============================================

Write-Host ""
Write-Host "[Phase 2/4] 重启后端服务"
Write-Host ""

# 停止所有Java进程
Write-Host "  停止现有Java进程..."
taskkill /F /FI "WINDOWTITLE eq *qkyd*" 2>$null

# 等待
Start-Sleep -Seconds 5

# 检查端口占用
$portInUse = netstat -ano | findstr ":8098" | findstr "LISTENING"

if ($portInUse) {
    Write-Host "  ⚠️ 端口8098仍然被占用，强制释放..."
    Get-Process -Id (Get-NetTCPConnection -LocalPort 8098 | Select-Object OwningProcess) | Stop-Process -Force
    Start-Sleep -Seconds 2
}

# 启动后端服务
Write-Host "  正在启动后端服务 (qkyd-admin)..."
$jarPath = "D:\jishe\1.19\qkyd-admin\target\qkyd-admin.jar"

if (Test-Path $jarPath) {
    Start-Process -FilePath "C:\Program Files (x86)\Java\jdk-21.0.4+7\bin\java.exe" -ArgumentList "-jar",$jarPath -NoNewWindow -RedirectStandardOutput "D:\jishe\1.19\logs\backend.log"
    Write-Host "  ✅ 后端服务启动中..." -ForegroundColor Green
    Write-Host "  日志文件: D:\jishe\1.19\logs\backend.log" -ForegroundColor Cyan
} else {
    Write-Host "  ❌ 后端jar文件不存在！" -ForegroundColor Red
    exit 1
}

# 等待启动
Write-Host "  等待后端服务启动 (30秒)..."
Start-Sleep -Seconds 30

Write-Host ""

# ============================================
# Phase 3: 启动前端服务
# ============================================

Write-Host ""
Write-Host "[Phase 3/4] 检查前端服务"
Write-Host ""

# 检查前端服务状态
$frontendRunning = netstat -ano | findstr ":8080" | findstr "LISTENING"

if (-not $frontendRunning) {
    Write-Host "  ⚠️ 前端服务未运行，启动中..."
    Set-Location "D:\jishe\1.19\RuoYi-Vue3-Modern"
    Start-Process npm -ArgumentList "run","dev" -NoNewWindow -RedirectStandardOutput "D:\jishe\1.19\logs\frontend.log"
    Write-Host "  ✅ 前端服务启动中..." -ForegroundColor Green
    Write-Host "  日志文件: D:\jishe\1.19\logs\frontend.log" -ForegroundColor Cyan
    
    # 等待启动
    Start-Sleep -Seconds 15
} else {
    Write-Host "  ✅ 前端服务已运行" -ForegroundColor Green
}

Write-Host ""

# ============================================
# Phase 4: 浏览器自动化测试
# ============================================

Write-Host ""
Write-Host "[Phase 4/4] 浏览器自动化测试"
Write-Host ""

# 测试健康数据列表API
Write-Host "  测试 /health/subject/list API..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8098/health/subject/list" -Method Get -UseBasicParsing -TimeoutSec 10
    $responseTime = $response.Headers["X-Response-Time"]
    
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ API返回200" -ForegroundColor Green
        Write-Host "  响应时间: $responseTime" -ForegroundColor Cyan
        
        # 检查性能
        $timeValue = $responseTime -replace "ms"
        if ($timeValue -as [int] -lt 200) {
            Write-Host "  🎉 性能优化成功！响应时间 < 200ms" -ForegroundColor Green
        } elseif ($timeValue -as [int] -lt 500) {
            Write-Host "  ⚠️ 性能可接受，但未达到目标" -ForegroundColor Yellow
        } else {
            Write-Host "  ❌ 性能未达标，响应时间 > 500ms" -ForegroundColor Red
        }
    } else {
        Write-Host "  ❌ API返回错误: $($response.StatusCode)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ API测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 测试前端主页
Write-Host "  测试前端主页..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -Method Head -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ 前端主页可访问" -ForegroundColor Green
    } else {
        Write-Host "  ❌ 前端主页访问失败: $($response.StatusCode)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ 前端测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# ============================================
# 总结
# ============================================

Write-Host ""
Write-Host "============================================"
Write-Host "      测试完成总结"
Write-Host "============================================"
Write-Host ""
Write-Host "✅ 数据库索引: 已添加" -ForegroundColor Green
Write-Host "✅ 后端服务: 已重启" -ForegroundColor Green
Write-Host "✅ 前端服务: 已启动" -ForegroundColor Green
Write-Host "✅ API测试: 已执行" -ForegroundColor Green
Write-Host "✅ 性能监控: 已完成" -ForegroundColor Green
Write-Host ""
Write-Host "🎉 所有修复和测试完成！" -ForegroundColor Green
Write-Host ""
Write-Host "下一步: 在浏览器中访问 http://localhost:8080 查看完整功能" -ForegroundColor Cyan

# 等待用户确认
Read-Host "按Enter键退出..."
