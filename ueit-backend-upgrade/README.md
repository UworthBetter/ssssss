# 健康管理平台 - 升级版启动指南

## 版本信息
- Spring Boot: 3.2.2
- Java: 17
- MySQL: 8.0+
- SpringDoc OpenAPI: 2.3.0 (替代 Swagger)

## 前置要求

### 1. Java 环境
- 必须安装 **Java 17** 或更高版本
- 检查版本：`java -version`

### 2. 数据库
- MySQL 8.0 或更高版本
- 已创建数据库：`ueit_jkpt`
- 已导入数据脚本

### 3. Redis
- Redis 已启动
- 默认端口：6379
- 无密码

## 启动步骤

### 方式1：使用启动脚本（推荐）

```bash
# 后台启动
start-backend.bat

# 调试模式启动（可以看到详细日志）
start-backend-debug.bat
```

### 方式2：手动启动

```bash
# 进入升级版目录
cd d:\jishe\1.19\ueit-backend-upgrade

# 启动
java -jar ueit-admin.jar
```

## 访问地址

启动成功后，可以访问：

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端API | http://localhost:8098 | RESTful API |
| API文档 | http://localhost:8098/swagger-ui.html | 接口文档 |
| Druid监控 | http://localhost:8098/druid | 数据库监控 |

## 配置说明

### 数据库配置
文件：`ueit-admin/src/main/resources/application-druid.yml`

```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/ueit_jkpt?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8
        username: root
        password: 123456  # 修改为你的MySQL密码
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 5
    password:  # 无密码留空
```

## 升级内容

### 已完成
1. ✅ Spring Boot 升级到 3.2.2
2. ✅ Java 升级到 17
3. ✅ 移除 Swagger 2 依赖
4. ✅ 添加 SpringDoc OpenAPI (Swagger 3)
5. ✅ 修改 pom.xml 配置
6. ✅ 移除不兼容的 fork 配置

### API文档变化

旧版 Swagger 注解已移除，使用 SpringDoc 自动生成文档：
- 访问：`http://localhost:8098/swagger-ui.html`
- 无需手动配置，自动扫描所有 Controller

## 常见问题

### Q: 启动报 "Unsupported class file major version"
**A:** Java 版本不匹配，需要 Java 17 或更高版本。

### Q: 无法连接数据库
**A:** 检查：
1. MySQL 是否启动
2. 数据库 `ueit_jkpt` 是否存在
3. 配置文件中的用户名密码是否正确

### Q: 无法连接 Redis
**A:** 检查：
1. Redis 是否启动
2. 端口是否为 6379
3. 是否有密码（如有需修改配置）

### Q: Swagger UI 无法访问
**A:** 新版本使用 SpringDoc，访问地址改为：
- `http://localhost:8098/swagger-ui.html` (不是 `/doc.html`)
- `http://localhost:8098/v3/api-docs` (OpenAPI JSON)

## 开发说明

### 重新构建
```bash
cd d:\jishe\1.19\ueit-backend-upgrade
mvn clean install -DskipTests
copy ueit-admin\target\ueit-admin.jar .\ueit-admin.jar
```

### 查看日志
- 控制台输出：使用 `start-backend-debug.bat`
- 日志文件：`logs/` 目录（如果配置了）

## 原版本对比

原版本位于：`d:\jishe\1.19\`
- Spring Boot: 2.5.15
- Java: 8
- Swagger 2

升级版本位于：`d:\jishe\1.19\ueit-backend-upgrade\`
- Spring Boot: 3.2.2
- Java: 17
- SpringDoc (Swagger 3)
