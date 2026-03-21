# qkyd-framework 模块

[根目录](../CLAUDE.md) > **qkyd-framework**

---

## 变更记录 (Changelog)

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 初始化模块文档 |

---

## 模块职责

qkyd-framework 是核心框架模块，提供：

1. **Spring Security 配置**：认证与授权框架
2. **AOP 切面**：日志、数据权限、限流、数据源切换
3. **安全过滤器**：JWT 认证过滤器
4. **全局异常处理**：统一异常响应
5. **系统服务**：登录、权限、令牌管理
6. **动态数据源**：主从数据源切换
7. **异步任务**：异步管理器
8. **Web 监控**：服务器监控信息

---

## 核心配置 (config)

| 配置类 | 功能 |
|--------|------|
| `SecurityConfig` | Spring Security 安全配置 |
| `MyBatisConfig` | MyBatis 配置 |
| `MessageSourceConfig` | 国际化消息配置 |
| `ApplicationConfig` | 应用配置 |
| `CaptchaConfig` | 验证码配置 |
| `DruidConfig` | Druid 数据源配置 |
| `RedisConfig` | Redis 配置 |
| `ThreadPoolConfig` | 线程池配置 |
| `ResourcesConfig` | 资源配置（跨域等） |
| `ServerConfig` | 服务器配置 |
| `FilterConfig` | 过滤器配置 |
| `FastJson2JsonRedisSerializer` | Redis JSON 序列化 |
| `KaptchaTextCreator` | 验证码文本生成器 |
| `DruidProperties` | Druid 属性配置 |
| `PermitAllUrlProperties` | 匿名访问 URL 配置 |

---

## AOP 切面 (aspectj)

| 切面类 | 功能 |
|--------|------|
| `LogAspect` | 操作日志记录切面 |
| `DataScopeAspect` | 数据权限过滤切面 |
| `DataSourceAspect` | 动态数据源切换切面 |
| `RateLimiterAspect` | 限流切面 |

---

## 安全框架 (security)

### 过滤器
| 类名 | 功能 |
|------|------|
| `JwtAuthenticationTokenFilter` | JWT 认证过滤器 |

### 处理器 (handle)
| 类名 | 功能 |
|------|------|
| `AuthenticationEntryPointImpl` | 认证失败处理 |
| `LogoutSuccessHandlerImpl` | 退出成功处理 |

### 上下文 (context)
| 类名 | 功能 |
|------|------|
| `AuthenticationContextHolder` | 认证上下文持有者 |
| `PermissionContextHolder` | 权限上下文持有者 |

---

## Web 服务 (web)

### 异常处理
| 类名 | 功能 |
|------|------|
| `GlobalExceptionHandler` | 全局异常处理器 |

### 服务 (service)
| 类名 | 功能 |
|------|------|
| `TokenService` | 令牌服务 |
| `SysLoginService` | 登录服务 |
| `SysPasswordService` | 密码服务 |
| `SysPermissionService` | 权限服务 |
| `UserDetailsServiceImpl` | 用户详情服务 |
| `PermissionService` | 权限验证服务 |

### 监控 (domain/server)
| 类名 | 功能 |
|------|------|
| `Server` | 服务器信息 |
| `Cpu` | CPU 信息 |
| `Jvm` | JVM 信息 |
| `Mem` | 内存信息 |
| `Sys` | 系统信息 |
| `SysFile` | 文件信息 |

---

## 数据源 (datasource)

| 类名 | 功能 |
|------|------|
| `DynamicDataSource` | 动态数据源 |
| `DynamicDataSourceContextHolder` | 数据源上下文持有者 |

---

## 异步任务 (manager)

| 类名 | 功能 |
|------|------|
| `AsyncManager` | 异步任务管理器 |
| `ShutdownManager` | 关闭管理器 |
| `factory/AsyncFactory` | 异步任务工厂 |

---

## 拦截器 (interceptor)

| 类名 | 功能 |
|------|------|
| `RepeatSubmitInterceptor` | 防重复提交拦截器 |
| `impl/SameUrlDataInterceptor` | 同 URL 数据拦截器 |

---

## 配置属性 (config/properties)

| 类名 | 功能 |
|------|------|
| `DruidProperties` | Druid 数据源属性 |
| `PermitAllUrlProperties` | 匿名访问 URL 属性 |

---

## 安全认证流程

### 登录流程
```
1. 用户提交登录请求 (SysLoginController)
2. SysLoginService 验证用户名密码
3. TokenService 生成 JWT 令牌
4. 返回令牌给前端
```

### 请求认证流程
```
1. 前端携带 JWT 令牌请求
2. JwtAuthenticationTokenFilter 过滤器拦截
3. TokenService 验证令牌有效性
4. 设置认证信息到 SecurityContext
5. 请求到达 Controller
```

---

## 数据权限控制

### DataScope 注解
```java
@DataScope(deptAlias = "d", userAlias = "u")
public List<SysUser> selectUserList(SysUser user) {
    // 自动添加数据权限过滤条件
}
```

### 支持的数据范围
- 全部数据权限
- 自定义数据权限
- 本部门数据权限
- 本部门及以下数据权限
- 仅本人数据权限

---

## 动态数据源

### 注解方式切换
```java
@DataSource(DataSourceType.SLAVE)
public List<SysUser> selectUserListFromSlave() {
    // 查询从库数据
}
```

### 数据源类型
- `MASTER`：主数据源（写）
- `SLAVE`：从数据源（读）

---

## 操作日志

### @Log 注解
```java
@Log(title = "用户管理", businessType = BusinessType.INSERT)
@PostMapping
public AjaxResult add(@RequestBody SysUser user) {
    // 自动记录操作日志
}
```

### 日志内容
- 操作模块
- 操作类型
- 请求参数
- 返回结果
- 操作人员
- 操作时间
- IP 地址

---

## 限流控制

### @RateLimiter 注解
```java
@RateLimiter(time = 10, count = 5)
@GetMapping("/test")
public AjaxResult test() {
    // 10秒内最多访问5次
}
```

---

## 相关文件清单

```
qkyd-framework/
├── src/main/java/com/qkyd/framework/
│   ├── aspectj/                # AOP 切面
│   │   ├── LogAspect.java
│   │   ├── DataScopeAspect.java
│   │   ├── DataSourceAspect.java
│   │   └── RateLimiterAspect.java
│   ├── config/                 # 配置类
│   │   ├── SecurityConfig.java
│   │   ├── MyBatisConfig.java
│   │   ├── MessageSourceConfig.java
│   │   ├── ApplicationConfig.java
│   │   ├── CaptchaConfig.java
│   │   ├── DruidConfig.java
│   │   ├── RedisConfig.java
│   │   ├── ThreadPoolConfig.java
│   │   ├── ResourcesConfig.java
│   │   ├── ServerConfig.java
│   │   ├── FilterConfig.java
│   │   ├── FastJson2JsonRedisSerializer.java
│   │   ├── KaptchaTextCreator.java
│   │   └── properties/
│   ├── datasource/             # 动态数据源
│   ├── interceptor/            # 拦截器
│   ├── manager/                # 异步任务管理
│   │   ├── AsyncManager.java
│   │   ├── ShutdownManager.java
│   │   └── factory/
│   ├── security/               # 安全框架
│   │   ├── context/
│   │   ├── filter/
│   │   └── handle/
│   ├── web/                    # Web 服务
│   │   ├── domain/
│   │   │   └── server/
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java
│   │   └── service/
│   │       ├── TokenService.java
│   │       ├── SysLoginService.java
│   │       ├── SysPasswordService.java
│   │       ├── SysPermissionService.java
│   │       ├── UserDetailsServiceImpl.java
│   │       └── PermissionService.java
│   └── resources/              # 资源文件
│       └── mapper/             # MyBatis 映射
└── pom.xml
```

---

**最后更新**: 2026-02-01 14:18:39
