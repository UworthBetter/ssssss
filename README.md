# 耆康云盾健康监测平台

> 面向老年群体的智能健康监测与 AI 辅助决策系统

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.5-42b883)
![Python](https://img.shields.io/badge/Python-3.10+-yellow)
![FastAPI](https://img.shields.io/badge/FastAPI-0.109+-009688)

## 项目简介

**耆康云盾**（QKYD）是一套面向养老服务机构的全栈健康监测平台。系统通过智能手表等可穿戴设备实时采集老人的心率、血氧、体温、步数、位置等体征数据，结合机器学习风险预测模型与大语言模型（DeepSeek）医学语义分析，构建从「数据采集 → 异常检测 → AI 决策 → 处置执行 → 审计闭环」的完整智慧健康管理链路。

## 功能特性

| 模块 | 功能 |
|------|------|
| 工作台 | 平台实时总览、AI 决策处理链可视化 |
| 对象中心 | 健康服务对象档案、健康档案管理、个人风险画像 |
| 事件中心 | 异常告警列表、全链路处理追踪 |
| 设备中心 | 智能手表设备管理、设备绑定 |
| AI 中心 | AI 算法工作台、处置规则配置、操作日志 |
| 报表中心 | 运营数据报表、个人健康报告 |

### 体征采集指标

心率、血氧（SpO₂）、体温、步数、血压、实时位置、电子围栏、跌倒检测

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (Vue 3 + Vite)                   │
│           Element Plus · ECharts · DataV · 高德地图       │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP / REST
┌────────────────────────▼────────────────────────────────┐
│              后端 (Spring Boot 3.2.5 · Java 17)          │
│  qkyd-admin · qkyd-health · qkyd-ai · qkyd-system       │
│               JWT 认证 · Quartz 定时任务                  │
└─────────┬──────────────────────────┬────────────────────┘
          │ MySQL / Redis            │ HTTP
┌─────────▼──────────┐   ┌──────────▼──────────────────┐
│  MySQL + Redis     │   │  Python AI 微服务 (FastAPI)  │
│  业务数据 / 缓存    │   │  心率异常检测 · 健康风险预测  │
└────────────────────┘   └─────────────────────────────┘
                                    │ HTTP (DeepSeek API)
                         ┌──────────▼──────────────────┐
                         │       DeepSeek LLM           │
                         │  事件医学洞察 · 智能报告生成  │
                         └─────────────────────────────┘
```

### AI 决策处理链

```
设备数据
  └─→ [异常检测] ─→ 发布事件
          └─→ [事件洞察]   ─→ LLM 补全医学上下文
                  └─→ [风险评估]  ─→ Python ML 计算风险分数(0-100)
                          └─→ [处置规则] ─→ 生成处置建议 / 自动执行
                                  └─→ [操作审计] ─→ 全链路留痕
                                          └─→ [反馈闭环] ─→ 优化模型
```

## 技术栈

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5 | 核心框架 |
| TypeScript | 5.7 | 类型安全 |
| Vite | 6.0 | 构建工具 |
| Vue Router | 4.5 | 路由管理 |
| Pinia | 2.3 | 状态管理 |
| Element Plus | 2.9 | UI 组件库 |
| ECharts | 5.6 | 数据可视化 |
| DataV | 2.10 | 大屏组件 |
| 高德地图 JS API | 1.0 | 地图与围栏 |

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring AI | 1.0.0-M4 | LLM 接入 |
| MyBatis | — | ORM |
| Druid | 1.2.20 | 连接池 |
| JWT | 0.12.5 | 认证授权 |
| Quartz | — | 定时任务 |
| SpringDoc | 2.3.0 | OpenAPI 文档 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6+ | 缓存 / Token |

### AI 微服务

| 技术 | 版本 | 用途 |
|------|------|------|
| Python | 3.10+ | 运行环境 |
| FastAPI | 0.109+ | Web 框架 |
| scikit-learn | 1.4+ | 健康风险预测模型 |
| Pydantic | 2.5+ | 数据校验 |
| DeepSeek API | — | 医学语义分析 |

## 项目结构

```
.
├── backend/                     # Java 后端（Maven 多模块）
│   ├── qkyd-admin/              # 启动入口 & Web 接口聚合（端口 8098）
│   ├── qkyd-framework/          # 安全、Redis、日志等基础框架
│   ├── qkyd-system/             # 用户、角色、菜单、组织管理
│   ├── qkyd-health/             # 健康业务：设备、体征、告警、报告
│   ├── qkyd-ai/                 # AI 决策：事件洞察、风险评估、处置规则
│   ├── qkyd-common/             # 公共工具类
│   ├── qkyd-quartz/             # 定时任务
│   ├── qkyd-ai-service/         # Python AI 微服务（默认端口 8001）
│   └── sql/                     # 数据库初始化脚本
└── qkyd-vue3-new/               # Vue 3 前端管理端
    └── src/
        ├── views/               # 页面组件
        ├── router/              # 路由配置
        ├── api/                 # 后端接口封装
        ├── stores/              # Pinia 状态
        └── layout/              # 整体布局
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6+
- Node.js 18+
- Python 3.10+

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE qkyd_jkpt DEFAULT CHARACTER SET utf8mb4;"

# 导入主库结构与基础数据
mysql -u root -p qkyd_jkpt < backend/sql/qkyd_jkpt.sql
```

### 2. 配置环境变量

复制并修改环境配置：

```bash
cp backend/.env.example backend/.env
```

关键配置项（`backend/.env`）：

```env
# 数据库
DB_HOST=localhost
DB_PORT=3306
DB_NAME=qkyd_jkpt
DB_USERNAME=root
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# DeepSeek AI
AI_BASE_URL=https://api.deepseek.com/v1
AI_API_KEY=your_deepseek_api_key
AI_REPORT_MODEL=deepseek-chat

# Python AI 微服务
AI_SERVICE_URL=http://localhost:8001    # Python AI 微服务地址（默认端口 8001）
```

### 3. 启动后端

```bash
cd backend
mvn clean package -DskipTests
java -jar qkyd-admin/target/qkyd-admin.jar
```

或在 IDE 中运行主类 `com.qkyd.QkydApplication`。

服务启动后访问：`http://localhost:8098`

### 4. 启动 Python AI 微服务

```bash
cd backend/qkyd-ai-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8001
```

服务启动后 API 文档：`http://localhost:8001/docs`

### 5. 启动前端

```bash
cd qkyd-vue3-new
npm install --registry=https://registry.npmmirror.com
npm run dev
```

访问地址：`http://localhost:5173`（Vite 默认端口）

## 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 后端端口 | `8098` | `backend/qkyd-admin/src/main/resources/application.yml` |
| AI 微服务端口 | `8001` | `backend/qkyd-ai-service/main.py`，可通过 `PORT` 环境变量覆盖 |
| DeepSeek 模型 | `deepseek-chat` | 环境变量 `AI_REPORT_MODEL` |
| 心率检测窗口 | `10` 条 | 偏差阈值 30% |
| 风险分数范围 | `0 – 100` | 低 / 中 / 高 / 极高 四级 |

## API 文档

后端启动后，Swagger UI 访问地址：

```
http://localhost:8098/swagger-ui/index.html
```

Python AI 微服务 API 文档：

```
http://localhost:8001/docs
```

## 主要页面说明

- **平台总览**：实时展示在线设备数、活跃对象数、今日事件统计及体征趋势大屏
- **AI 算法工作台**：可视化展示 AI 决策处理链各阶段状态与处理结果
- **对象风险画像**：雷达图展示个人多维健康风险，支持历史趋势对比
- **健康档案**：完整的个人体征历史记录，支持时间范围筛选与导出
- **处理链追踪**：事件全链路追踪，从异常检测到处置执行的每一步操作日志
- **处置规则配置**：按异常类型和风险分数区间灵活配置自动处置规则

## License

MIT
