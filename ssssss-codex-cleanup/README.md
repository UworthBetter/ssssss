# qkyd 健康管理平台（最小可运行版）

本仓库已按“最小可运行”标准瘦身，仅保留后端 Maven 聚合、核心模块目录、前端模块目录和数据库脚本目录。

## 保留内容

- `pom.xml`
- `README.md`
- `application-prod.yml`
- 模块目录：
  - `qkyd-admin`
  - `qkyd-framework`
  - `qkyd-system`
  - `qkyd-common`
  - `qkyd-health`
  - `qkyd-ai`
  - `qkyd-quartz`
  - `qkyd-generator`
  - `qkyd-ai-service`
  - `qkyd-vue3-new`
- `sql`

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6+
- Node.js 18+（前端开发时需要）

## 后端启动

1. 在仓库根目录编译：

```bash
mvn clean package -DskipTests
```

2. 启动后端服务：

```bash
java -jar qkyd-admin/target/qkyd-admin.jar
```

3. 或在 IDE 中启动主类：

- `com.qkyd.RuoYiApplication`

默认端口：`8098`

## 前端启动

1. 进入前端目录：

```bash
cd qkyd-vue3-new
```

2. 安装依赖并启动：

```bash
npm install --registry=https://registry.npmmirror.com
npm run dev
```

默认访问地址：`http://localhost:8080`

## 数据库初始化

```bash
mysql -u root -p qkyd_jkpt < sql/qkyd_jkpt.sql
```

## 说明

本 README 只描述当前精简后的仓库结构与最小运行流程。
