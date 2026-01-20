# 项目文件说明

## 📂 核心文件（保留）

### 🚀 启动和管理脚本
| 文件 | 功能 | 说明 |
|------|------|------|
| **`main-menu.bat`** | 统一启动菜单 | ⭐ 主要入口，管理所有服务 |
| **`stop-simple-v2.bat`** | 停止所有服务 | ⭐ 快速停止后端和前端 |

### 📚 文档
| 文件 | 功能 |
|------|------|
| **`README.md`** | 项目说明文档 |
| **`QUICK_GUIDE.md`** | 快速启动指南 |

### 🛠️ 数据库工具
| 文件 | 功能 |
|------|------|
| **`add_age_field.py`** | 数据库修复工具（添加 age 字段） |
| **`check_all_fields.py`** | 数据库检查工具 |

### 📦 核心目录
| 目录 | 说明 |
|------|------|
| **`ueit-backend-upgrade/`** | 后端升级版（推荐）- Spring Boot 3.2.2 + Java 17 |
| **`ueit-admin/`** | 后端原版本 - Spring Boot 2.5.15 + Java 8 |
| **`ueit-ui/`** | 前端项目 - Vue 2.6 |
| **`ueit-system/`** | 系统模块 |
| **`ueit-common/`** | 公共模块 |
| **`ueit-framework/`** | 框架模块 |
| **`ueit-health/`** | 健康模块 |
| **`ueit-quartz/`** | 定时任务模块 |
| **`ueit-generator/`** | 代码生成器模块 |
| **`sql/`** | 数据库脚本 |
| **`bin/`** | 工具脚本 |
| **`doc/`** | 文档目录 |

### ⚙️ 配置文件
| 文件 | 说明 |
|------|------|
| **`pom.xml`** | Maven 配置文件 |
| **`package-lock.json`** | NPM 依赖锁定文件 |
| **`jkpt.iml`** | IntelliJ IDEA 配置 |
| **`.gitignore`** | Git 忽略配置 |
| **`LICENSE`** | 项目许可证 |

---

## ❌ 已删除的文件（清理）

### 🔧 重复的启动脚本
- `check-process.bat` - 被 main-menu.bat 替代
- `check-status.bat` - 被 main-menu.bat 替代
- `start-debug.bat` - 被 main-menu.bat 替代
- `start-frontend-simple.bat` - 被 main-menu.bat 替代
- `start-frontend-with-log.bat` - 被 main-menu.bat 替代
- `start-frontend.bat` - 被 main-menu.bat 替代
- `main-startup.bat` - 被 main-menu.bat 替代
- `ry.bat` - 旧版启动脚本，被 main-menu.bat 替代
- `ry.sh` - Linux 版本，Windows 不需要

### 🔧 重复的停止脚本
- `stop-all.bat` - 重复
- `stop-force.bat` - 重复
- `stop-simple.bat` - 重复
- `stop.bat` - 重复

### 🛠️ 临时修复脚本（任务已完成）
- `fix_age_field.bat` - 数据库修复
- `setup-local-db.bat` - 数据库设置
- `install-deps.bat` - 依赖安装
- `reinstall-frontend.bat` - 前端重新安装
- `create_placeholder_images.py` - 图片创建
- `fix_all_backend.py` - 后端修复
- `fix_encoding_and_imports.py` - 编码修复
- `fix_health_encoding.py` - 健康模块编码修复
- `master_repair.py` - 主修复脚本
- `reset_and_fix_health.py` - 重置修复
- `reset_remaining_modules.py` - 模块重置
- `strip_swagger.py` - Swagger 清理
- `test_login.py` - 登录测试
- `SecurityConfig.java.bak` - 备份文件

---

## 🚀 快速开始

### 启动项目
```bash
双击运行: main-menu.bat
选择 [3] 一键启动
```

### 停止项目
```bash
双击运行: stop-simple-v2.bat
```

### 查看状态
```bash
双击运行: main-menu.bat
选择 [4] 查看运行状态
```

---

## 📍 访问地址

| 服务 | 地址 |
|------|------|
| **前端** | http://localhost:8080 |
| **后端** | http://localhost:8098 |
| **API 文档** | http://localhost:8098/swagger-ui.html |
| **Druid** | http://localhost:8098/druid |

---

## 📋 默认账号

- 用户名：`admin`
- 密码：`admin123`

---

**最后更新**: 2026-01-19
**版本**: v1.0 - Clean Version
