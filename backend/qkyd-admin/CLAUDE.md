# qkyd-admin 模块

[根目录](../CLAUDE.md) > **qkyd-admin**

---

## 变更记录 (Changelog)

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 初始化模块文档 |

---

## 模块职责

qkyd-admin 是整个应用的 Web 服务入口模块，负责：

1. **应用启动**：包含 `RuoYiApplication` 主启动类
2. **控制器层**：系统管理、监控、通用功能的 REST API
3. **配置管理**：Swagger、应用配置的加载

---

## 入口与启动

### 主启动类
```java
// 文件: src/main/java/com/qkyd/RuoYiApplication.java
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuoYiApplication.class, args);
    }
}
```

### 启动参数
- **默认端口**: 8098
- **上下文路径**: `/`
- **配置文件**: `application.yml`

---

## 对外接口

### 控制器分类

#### 1. 系统管理 (system)
| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/system/user` | `SysUserController` | 用户管理 |
| `/system/role` | `SysRoleController` | 角色管理 |
| `/system/menu` | `SysMenuController` | 菜单管理 |
| `/system/dept` | `SysDeptController` | 部门管理 |
| `/system/post` | `SysPostController` | 岗位管理 |
| `/system/dict/*` | `SysDictTypeController` | 字典管理 |
| `/system/config` | `SysConfigController` | 参数配置 |
| `/system/notice` | `SysNoticeController` | 通知公告 |
| `/system/login` | `SysLoginController` | 登录认证 |
| `/system/register` | `SysRegisterController` | 用户注册 |

#### 2. 系统监控 (monitor)
| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/monitor/online` | `SysUserOnlineController` | 在线用户 |
| `/monitor/logininfor` | `SysLogininforController` | 登录日志 |
| `/monitor/operlog` | `SysOperlogController` | 操作日志 |
| `/monitor/server` | `ServerController` | 服务监控 |
| `/monitor/cache` | `CacheController` | 缓存监控 |

#### 3. 通用功能 (common)
| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/common/captcha` | `CaptchaController` | 验证码 |
| `/common/upload` | `CommonController` | 文件上传 |

#### 4. 测试工具 (tool)
| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/tool/test` | `TestController` | 测试接口 |

---

## 关键依赖与配置

### Maven 依赖
```xml
<!-- SpringDoc OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>

<!-- MySQL 驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- 依赖的业务模块 -->
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-framework</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-system</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-quartz</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-generator</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-health</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-ai</artifactId>
</dependency>
```

### 配置文件
- **主配置**: `src/main/resources/application.yml`
- **数据源配置**: `src/main/resources/application-druid.yml`
- **MyBatis 配置**: `src/main/resources/mybatis/mybatis-config.xml`
- **日志配置**: `src/main/resources/logback.xml`

---

## 数据模型

本模块不直接定义数据模型，所有实体类在业务模块中定义：

| 数据类型 | 来源模块 |
|---------|----------|
| 系统用户、角色、菜单 | `qkyd-system` |
| 健康数据、设备信息 | `qkyd-health` |
| AI 分析结果 | `qkyd-ai` |

---

## 测试与质量

### 单元测试
- 测试目录：`src/test/java/`
- 运行命令：`mvn test -Dtest=*ControllerTest`

### API 文档
- **Swagger UI**: http://localhost:8098/doc.html
- **OpenAPI JSON**: http://localhost:8098/v3/api-docs

---

## 常见问题 (FAQ)

### Q1: 如何修改默认端口？
编辑 `application.yml`：
```yaml
server:
  port: 8098  # 修改为目标端口
```

### Q2: 如何添加新的控制器？
1. 在 `src/main/java/com/qkyd/web/controller/` 下创建控制器类
2. 添加 `@RestController` 和 `@RequestMapping` 注解
3. 注入业务 Service（来自其他模块）

### Q3: 如何配置跨域？
已通过 `ResourcesConfig` 配置跨域，默认允许所有来源。

---

## 相关文件清单

```
qkyd-admin/
├── src/main/java/com/qkyd/
│   ├── RuoYiApplication.java              # 启动类
│   ├── RuoYiServletInitializer.java       # Servlet 初始化
│   └── web/
│       ├── controller/
│       │   ├── common/                    # 通用控制器
│       │   ├── monitor/                   # 监控控制器
│       │   ├── system/                    # 系统管理控制器
│       │   └── tool/                      # 工具控制器
│       └── core/config/SwaggerConfig.java # Swagger 配置
├── src/main/resources/
│   ├── application.yml                    # 主配置
│   ├── application-druid.yml              # 数据源配置
│   ├── mybatis/mybatis-config.xml         # MyBatis 配置
│   └── logback.xml                        # 日志配置
└── pom.xml                                # Maven 配置
```

---

**最后更新**: 2026-02-01 14:18:39
