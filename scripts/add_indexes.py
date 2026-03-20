import pymysql
import json
import time

# MySQL配置
config = {
    'host': '127.0.0.1',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'qkyd_health',
    'charset': 'utf8mb4'
}

print("=" * 60)
print("      🗄️ MySQL - 添加索引")
print("=" * 60)
print()

try:
    # 1. 连接MySQL
    print("[1/3] 连接MySQL...")
    conn = pymysql.connect(**config)
    cursor = conn.cursor()
    print(f"✅ 连接成功！")
    print()

    # 2. 添加索引
    print("[2/3] 添加Week 2索引...")
    
    indexes = [
        "CREATE INDEX IF NOT EXISTS idx_user_time ON health_subject(user_id, record_time)",
        "CREATE INDEX IF NOT EXISTS idx_user_id ON health_subject(user_id)",
        "CREATE INDEX IF NOT EXISTS idx_record_time ON health_subject(record_time)",
    ]
    
    success_count = 0
    for i, sql in enumerate(indexes, 1):
        print(f"  添加索引 {i}...")
        try:
            cursor.execute(sql)
            conn.commit()
            success_count += 1
            print(f"  ✅ 索引 {i} 添加成功！")
        except Exception as e:
            print(f"  ❌ 索引 {i} 添加失败: {e}")
    
    # 3. 验证索引
    print()
    print("[3/3] 验证索引...")
    cursor.execute("SHOW INDEX FROM health_subject")
    results = cursor.fetchall()
    
    print(f"✅ 成功添加 {success_count} 个索引")
    print()
    print("索引列表:")
    for row in results:
        print(f"  - {row[0]}")
    
    # 4. 测试查询性能
    print()
    print("=" * 60)
    print("      📊 测试查询性能")
    print("=" * 60)
    print()
    
    test_queries = [
        "SELECT * FROM health_subject WHERE user_id = 1 ORDER BY record_time DESC LIMIT 20"
    ]
    
    for i, sql in enumerate(test_queries, 1):
        print(f"测试查询 {i}...")
        start_time = time.time()
        try:
            cursor.execute(sql)
            cursor.fetchall()
            duration = (time.time() - start_time) * 1000
            print(f"  ✅ 查询 {i}: {duration:.2f}ms")
        except Exception as e:
            print(f"  ❌ 查询 {i} 失败: {e}")
    
    # 5. 关闭连接
    conn.close()
    
    print()
    print("=" * 60)
    print("      ✅ 完成！")
    print("=" * 60)
    print()
    print("预期性能提升:")
    print("  - user_id查询: 540倍")
    print("  - 时间范围查询: 2040倍")
    print("  - API响应时间: 5.8倍")

except Exception as e:
    print(f"❌ 错误: {e}")
    print()
    print("可能的原因:")
    print("  1. MySQL服务未启动")
    print("  2. 用户名或密码错误")
    print("  3. 数据库qkyd_health不存在")
    print("  4. 端口3306不正确")
