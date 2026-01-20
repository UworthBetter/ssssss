# 健康管理平台 - 快速启动指南

## 📋 系统信息

- **项目名称**: 健康管理平台
- **后端框架**: Spring Boot 3.2.2 (升级版)
- **前端框架**: Vue 2.6
- **数据库**: MySQL 8.0+
- **缓存**: Redis

## 🚀 快速启动

### 方式1: 使用主菜单（推荐）

```bash
双击运行: main-menu.bat
```

菜单选项：
- `[1]` 启动后端（需要Java 21）
- `[2]` 启动前端（Vue3）
- `[3]` 一键启动（后端 + 前端）
- `[4]` 查看运行状态
- `[5]` 停止所有服务
- `[6]` 重新启动所有服务
- `[0]` 退出

⚠️ **重要提示**：后端需要Java 21或更高版本，系统已自动配置。

### 方式2: 停止服务

```bash
双击运行: stop-simple.bat
```

### 方式3: 手动启动

**启动后端（必须使用Java 21）:**
```bash
cd d:\jishe\1.19\ueit-backend-upgrade
"C:\Program Files (x86)\java\jdk-21.0.4+7\bin\java.exe" -jar ueit-admin.jar
```

**启动前端（Vue3）:**
```bash
cd d:\jishe\1.19\RuoYi-Vue3-Modern
npm run dev
```

## 📍 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| **前端** | http://localhost:8080 | 用户界面 |
| **后端 API** | http://localhost:8098 | RESTful 接口 |
| **API 文档** | http://localhost:8098/swagger-ui.html | 接口文档 |
| **Druid 监控** | http://localhost:8098/druid | 数据库监控 |

## 🔑 默认账号

- **用户名**: `admin`
- **密码**: `admin123`

## 🔧 常用命令

### 检查服务状态

**Windows PowerShell:**
```powershell
# 检查端口占用
netstat -ano | findstr "8080"
netstat -ano | findstr "8098"

# 查看进程
tasklist | findstr "java"
tasklist | findstr "node"
```

### 停止服务

**停止后端:**
```bash
taskkill /F /IM java.exe
```

**停止前端:**
```bash
taskkill /F /IM node.exe
```

**停止所有:**
```bash
stop-simple.bat
```

### 手动停止端口进程

```bash
# 查找占用 8080 端口的进程
netstat -ano | findstr ":8080"

# 停止指定 PID 的进程
taskkill /F /PID <PID>
```

## 🛠️ 故障排除

### 前端无法启动

1. **检查端口占用:**
   ```bash
   netstat -ano | findstr ":8080"
   ```

2. **停止占用进程:**
   ```bash
   taskkill /F /PID <PID>
   ```

3. **清理缓存并重启:**
   ```bash
   cd d:\jishe\1.19\ueit-ui
   rmdir /s /q node_modules\.cache
   npm run dev
   ```

### 后端无法启动

1. **检查 MySQL:**
   - 确认 MySQL 服务正在运行
   - 确认数据库 `ueit_jkpt` 已创建

2. **检查 Redis:**
   - 确认 Redis 服务正在运行
   - 默认端口: 6379

3. **查看错误日志:**
   ```bash
   java -jar ueit-admin.jar
   ```

### 数据库问题

**添加 age 字段:**
```bash
cd d:\jishe\1.19
python add_age_field.py
```

**检查数据库结构:**
```bash
cd d:\jishe\1.19
python check_all_fields.py
```

## 📂 项目结构

```
d:\jishe\1.19\
├── ueit-admin\                    # 后端主模块（原版本）
├── ueit-backend-upgrade\          # 后端升级版（推荐使用）
│   ├── ueit-admin.jar            # 可执行 JAR 包
│   └── start-backend.bat         # 启动脚本
├── ueit-ui\                       # 前端项目
│   ├── package.json              # 前端依赖配置
│   └── vue.config.js            # Vue 配置
├── main-menu.bat                  # 主启动菜单 ⭐
├── stop-simple.bat                # 停止所有服务 ⭐
├── add_age_field.py               # 数据库修复工具
└── check_all_fields.py            # 数据库检查工具
```

## 📞 技术支持

- **后端日志**: 查看启动窗口的输出
- **前端日志**: 查看浏览器控制台 (F12)
- **API 测试**: 使用 Postman 或浏览器访问 /swagger-ui.html

## ⚠️ 注意事项

1. **启动顺序**: 先启动 MySQL 和 Redis，再启动后端，最后启动前端
2. **端口冲突**:
   - 前端默认: 8080 (如果被占用会自动改)
   - 后端: 8098
3. **Java 版本**: 后端必须使用 Java 21（路径：`C:\Program Files (x86)\java\jdk-21.0.4+7`）
4. **Node.js 版本**: 前端需要 Node.js 8.9 或更高版本

---

**最后更新**: 2026-01-19
**版本**: v1.0
