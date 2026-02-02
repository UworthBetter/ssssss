# qkyd-generator 模块

[根目录](../CLAUDE.md) > **qkyd-generator**

---

## 变更记录 (Changelog)

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 初始化模块文档 |

---

## 模块职责

qkyd-generator 是代码生成器模块，提供：

1. **表管理**：数据库表导入、配置、预览
2. **代码生成**：基于 Velocity 模板生成 Java、Vue、XML 代码
3. **代码预览**：实时预览生成的代码
4. **代码下载**：打包下载生成的代码
5. **模板自定义**：支持自定义代码模板

---

## 入口与启动

### 配置类
```java
// 文件: src/main/java/com/qkyd/generator/config/GenConfig.java
@Configuration
public class GenConfig {
    // 代码生成配置
}
```

---

## 对外接口

### REST API

| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/tool/gen/list` | `GenController` | 表列表 |
| `/tool/gen/{id}` | `GenController` | 表详情 |
| `/tool/gen/db/list` | `GenController` | 数据库表列表 |
| `/tool/gen/importTable` | `GenController` | 导入表 |
| `/tool/gen/create` | `GenController` | 创建代码 |
| `/tool/gen/code/{id}` | `GenController` | 预览代码 |
| `/tool/gen/download/{id}` | `GenController` | 下载代码 |
| `/tool/gen/edit` | `GenController` | 修改配置 |
| `/tool/gen/remove` | `GenController` | 删除表 |
| `/tool/gen/drill/{id}` | `GenController` | 表详情钻取 |

---

## 数据模型

### 实体类
| 类名 | 表名 | 功能 |
|------|------|------|
| `GenTable` | `gen_table` | 代码生成业务表 |
| `GenTableColumn` | `gen_table_column` | 代码生成字段 |

### 表配置字段
| 字段 | 类型 | 说明 |
|------|------|------|
| tableId | Long | 表ID |
| tableName | String | 表名称 |
| tableComment | String | 表描述 |
| className | String | 实体类名 |
| functionName | String | 功能名称 |
| functionAuthor | String | 作者 |
| genType | String | 生成代码路径 |
| genPath | String | 生成路径 |
| tplCategory | String | 模板类型 |
| packageName | String | 包名 |
| moduleName | String | 模块名 |
| businessName | String | 业务名 |
| columns | List | 列信息 |

---

## 核心服务

### 表服务
```java
// 文件: src/main/java/com/qkyd/generator/service/IGenTableService.java
public interface IGenTableService {
    // 表管理、代码生成
}
```

### 列服务
```java
// 文件: src/main/java/com/qkyd/generator/service/IGenTableColumnService.java
public interface IGenTableColumnService {
    // 列管理
}
```

---

## 工具类 (util)

| 类名 | 功能 |
|------|------|
| `GenUtils` | 代码生成工具 |
| `VelocityUtils` | Velocity 模板工具 |
| `VelocityInitializer` | Velocity 初始化器 |

---

## 代码生成模板

### 模板位置
```
src/main/resources/vm/
├── java/
│   ├── domain.java.vm           # 实体类模板
│   ├── mapper.java.vm           # Mapper 接口模板
│   ├── service.java.vm          # Service 接口模板
│   ├── serviceImpl.java.vm      # Service 实现模板
│   └── controller.java.vm       # Controller 模板
├── xml/
│   └── mapper.xml.vm            # MyBatis XML 模板
├── vue/
│   ├── api.js.vm                # API JS 模板
│   └── index.vue.vm             # Vue 页面模板
└── sql/
    └── menu.sql.vm              # 菜单 SQL 模板
```

---

## 生成代码结构

### 后端代码
```
com/qkyd/{moduleName}/
├── domain/{ClassName}.java         # 实体类
├── mapper/{ClassName}Mapper.java   # Mapper 接口
├── service/I{ClassName}Service.java # Service 接口
├── service/impl/{ClassName}ServiceImpl.java # Service 实现
└── controller/{ClassName}Controller.java # Controller
```

### 前端代码
```
api/{businessName}.js               # API 定义
views/{businessName}/index.vue      # 页面组件
```

---

## 字段类型映射

### 数据库到 Java 类型
| 数据库类型 | Java 类型 |
|------------|-----------|
| char, varchar | String |
| tinyint, smallint, int | Integer |
| bigint | Long |
| float | Float |
| double | Double |
| decimal | BigDecimal |
| date, datetime | Date |
| blob | byte[] |

---

## 查询类型

| 类型 | 说明 | 示例 |
|------|------|------|
| EQ | 等于 | `=` |
| NE | 不等于 | `!=` |
| GT | 大于 | `>` |
| GTE | 大于等于 | `>=` |
| LT | 小于 | `<` |
| LTE | 小于等于 | `<=` |
| LIKE | 模糊查询 | `LIKE` |
| BETWEEN | 范围查询 | `BETWEEN` |

---

## 相关文件清单

```
qkyd-generator/
├── src/main/java/com/qkyd/generator/
│   ├── config/
│   │   └── GenConfig.java
│   ├── controller/
│   │   └── GenController.java
│   ├── domain/
│   │   ├── GenTable.java
│   │   └── GenTableColumn.java
│   ├── mapper/
│   │   ├── GenTableMapper.java
│   │   └── GenTableColumnMapper.java
│   ├── service/
│   │   ├── IGenTableService.java
│   │   ├── IGenTableColumnService.java
│   │   ├── impl/
│   │   │   ├── GenTableServiceImpl.java
│   │   │   └── GenTableColumnServiceImpl.java
│   └── util/
│       ├── GenUtils.java
│       ├── VelocityUtils.java
│       └── VelocityInitializer.java
├── src/main/resources/
│   ├── vm/                        # Velocity 模板
│   │   ├── java/
│   │   ├── xml/
│   │   ├── vue/
│   │   └── sql/
│   └── mapper/generator/
│       ├── GenTableMapper.xml
│       └── GenTableColumnMapper.xml
└── pom.xml
```

---

**最后更新**: 2026-02-01 14:18:39
