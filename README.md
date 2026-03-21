# 耆康云盾健康监测平台（qkyd）

本仓库为耆康云盾（qkyd）健康监测平台源码，包含后端微模块、
前端管理端与数据库初始化脚本。项目聚焦健康监测、风险预警、
设备管理、智能分析与平台运营。

## 模块结构

- `backend/qkyd-admin`：后端启动与 Web 接口聚合
- `backend/qkyd-framework`：安全、配置、拦截器等基础框架
- `backend/qkyd-system`：系统管理、权限、菜单与组织模型
- `backend/qkyd-health`：健康业务领域模块
- `backend/qkyd-ai`：AI 相关业务模块
- `backend/qkyd-quartz`：定时任务模块
- `backend/sql`：数据库结构与初始化数据
- `qkyd-vue3-new`：Vue 3 前端管理端

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6+
- Node.js 18+

## 后端启动

1. 在 `backend` 目录编译：

```bash
mvn clean package -DskipTests
```

2. 启动服务：

```bash
java -jar qkyd-admin/target/qkyd-admin.jar
```

3. 或在 IDE 启动主类：

- `com.qkyd.QkydApplication`

默认端口：`8098`

## 前端启动

1. 进入前端目录：

```bash
cd qkyd-vue3-new
```

2. 安装依赖并运行：

```bash
npm install --registry=https://registry.npmmirror.com
npm run dev
```

默认地址：`http://localhost:8080`

## 数据库初始化

```bash
mysql -u root -p qkyd_jkpt < backend/sql/qkyd_jkpt.sql
```

