# qkyd-common 模块

[根目录](../CLAUDE.md) > **qkyd-common**

---

## 变更记录 (Changelog)

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 初始化模块文档 |

---

## 模块职责

qkyd-common 是通用工具模块，提供整个项目的基础设施：

1. **核心基础类**：控制器基类、实体基类、分页支持
2. **工具类**：字符串、日期、文件、加密、HTTP、IP 等
3. **注解定义**：Excel 导出、数据权限、日志、重复提交等
4. **异常处理**：全局异常定义
5. **常量枚举**：系统级常量

---

## 核心组件

### 基础类 (core)

| 类名 | 功能 |
|------|------|
| `BaseController` | 控制器基类，提供通用方法 |
| `AjaxResult` | 统一响应结果封装 |
| `R` | 简化响应结果封装 |
| `BaseEntity` | 实体基类，包含通用字段 |
| `TreeEntity` | 树形实体基类 |
| `TreeSelect` | 树形选择结构 |
| `PageDomain` | 分页参数封装 |
| `TableDataInfo` | 表格数据响应 |
| `TableSupport` | 分页支持工具 |
| `RedisCache` | Redis 缓存封装 |

### 核心实体 (core/domain/entity)
| 类名 | 功能 |
|------|------|
| `SysUser` | 系统用户实体 |
| `SysRole` | 系统角色实体 |
| `SysMenu` | 系统菜单实体 |
| `SysDept` | 系统部门实体 |
| `SysDictType` | 字典类型实体 |
| `SysDictData` | 字典数据实体 |

### 登录模型 (core/domain/model)
| 类名 | 功能 |
|------|------|
| `LoginUser` | 登录用户信息 |
| `LoginBody` | 登录请求体 |
| `RegisterBody` | 注册请求体 |

---

## 工具类 (utils)

### 字符串与文本
| 类名 | 功能 |
|------|------|
| `StringUtils` | 字符串工具 |
| `StrFormatter` | 字符串格式化 |
| `CharsetKit` | 字符集工具 |

### 日期时间
| 类名 | 功能 |
|------|------|
| `DateUtils` | 日期工具 |

### 加密与签名
| 类名 | 功能 |
|------|------|
| `Md5Utils` | MD5 加密 |
| `Base64` | Base64 编码 |

### 文件处理
| 类名 | 功能 |
|------|------|
| `FileUtils` | 文件工具 |
| `FileUploadUtils` | 文件上传 |
| `FileTypeUtils` | 文件类型判断 |
| `ImageUtils` | 图片处理 |
| `MimeTypeUtils` | MIME 类型 |

### HTTP 与 IP
| 类名 | 功能 |
|------|------|
| `HttpUtils` | HTTP 工具 |
| `HttpHelper` | HTTP 辅助 |
| `IpUtils` | IP 地址工具 |
| `AddressUtils` | 地址解析 |

### 其他
| 类名 | 功能 |
|------|------|
| `ServletUtils` | Servlet 工具 |
| `SecurityUtils` | 安全工具 |
| `DictUtils` | 字典工具 |
| `MessageUtils` | 消息工具 |
| `LogUtils` | 日志工具 |
| `ExceptionUtil` | 异常工具 |
| `PageUtils` | 分页工具 |
| `Arith` | 数学计算 |
| `BeanUtils` | Bean 工具 |
| `BeanValidators` | Bean 验证 |
| `SpringUtils` | Spring 工具 |
| `ReflectUtils` | 反射工具 |
| `SqlUtil` | SQL 工具 |

### HTML 处理
| 类名 | 功能 |
|------|------|
| `EscapeUtil` | 转义工具 |
| `HTMLFilter` | HTML 过滤器 |

### Excel 处理
| 类名 | 功能 |
|------|------|
| `ExcelUtil` | Excel 工具 |
| `ExcelHandlerAdapter` | Excel 处理适配器 |

---

## 注解 (annotation)

| 注解 | 功能 |
|------|------|
| `Anonymous` | 匿名访问（无需登录） |
| `DataScope` | 数据权限过滤 |
| `DataSource` | 动态数据源 |
| `Excel` | Excel 导出注解 |
| `Excels` | Excel 导出注解（多个） |
| `Log` | 操作日志记录 |
| `RateLimiter` | 限流注解 |
| `RepeatSubmit` | 防重复提交 |

---

## 枚举 (enums)

| 枚举 | 功能 |
|------|------|
| `BusinessStatus` | 业务状态码 |
| `BusinessType` | 业务类型 |
| `DataSourceType` | 数据源类型 |
| `HttpMethod` | HTTP 方法 |
| `LimitType` | 限流类型 |
| `OperatorType` | 操作者类型 |
| `UserStatus` | 用户状态 |

---

## 常量 (constant)

| 常量类 | 功能 |
|--------|------|
| `Constants` | 通用常量 |
| `CacheConstants` | 缓存常量 |
| `HttpStatus` | HTTP 状态码 |
| `GenConstants` | 代码生成常量 |
| `ScheduleConstants` | 定时任务常量 |
| `UserConstants` | 用户常量 |
| `WatchPushActionType` | 手表推送动作类型 |
| `WatchBNPushActionType` | 手表 B&N 推送动作类型 |

---

## 异常 (exception)

### 基础异常
| 类名 | 功能 |
|------|------|
| `BaseException` | 基础异常 |
| `GlobalException` | 全局异常 |
| `ServiceException` | 服务异常 |
| `UtilException` | 工具异常 |
| `DemoModeException` | 演示模式异常 |

### 用户异常
| 类名 | 功能 |
|------|------|
| `UserException` | 用户异常 |
| `UserNotExistsException` | 用户不存在 |
| `UserPasswordNotMatchException` | 密码不匹配 |
| `UserPasswordRetryLimitExceedException` | 密码重试超限 |
| `BlackListException` | 黑名单异常 |
| `CaptchaException` | 验证码异常 |
| `CaptchaExpireException` | 验证码过期 |

### 文件异常
| 类名 | 功能 |
|------|------|
| `FileException` | 文件异常 |
| `FileUploadException` | 文件上传异常 |
| `FileNameLengthLimitExceededException` | 文件名过长 |
| `FileSizeLimitExceededException` | 文件大小超限 |
| `InvalidExtensionException` | 无效文件扩展名 |

### 任务异常
| 类名 | 功能 |
|------|------|
| `TaskException` | 定时任务异常 |

---

## 过滤器 (filter)

| 类名 | 功能 |
|------|------|
| `XssFilter` | XSS 过滤器 |
| `XssHttpServletRequestWrapper` | XSS 请求包装器 |
| `RepeatableFilter` | 可重复提交过滤器 |
| `RepeatedlyRequestWrapper` | 重复请求包装器 |
| `PropertyPreExcludeFilter` | 属性排除过滤器 |

---

## 配置类 (config)

| 类名 | 功能 |
|------|------|
| `RuoYiConfig` | 若依配置 |

---

## 相关文件清单

```
qkyd-common/
├── src/main/java/com/qkyd/common/
│   ├── annotation/              # 注解定义
│   ├── config/                  # 配置类
│   ├── constant/                # 常量定义
│   ├── core/                    # 核心类
│   │   ├── controller/          # 控制器基类
│   │   ├── domain/              # 核心实体
│   │   ├── page/                # 分页支持
│   │   ├── redis/               # Redis 封装
│   │   └── text/                # 文本工具
│   ├── enums/                   # 枚举定义
│   ├── exception/               # 异常定义
│   │   ├── base/                # 基础异常
│   │   ├── file/                # 文件异常
│   │   ├── job/                 # 任务异常
│   │   └── user/                # 用户异常
│   ├── filter/                  # 过滤器
│   └── utils/                   # 工具类
│       ├── bean/                # Bean 工具
│       ├── file/                # 文件工具
│       ├── html/                # HTML 工具
│       ├── http/                # HTTP 工具
│       ├── ip/                  # IP 工具
│       ├── poi/                 # Excel 工具
│       ├── sign/                # 签名工具
│       ├── sql/                 # SQL 工具
│       └── spring/              # Spring 工具
└── pom.xml
```

---

**最后更新**: 2026-02-01 14:18:39
