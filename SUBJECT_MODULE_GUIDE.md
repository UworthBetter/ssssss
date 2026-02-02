# 服务对象管理模块 - 使用指南

## 📦 已创建的文件

### 后端代码 (qkyd-health模块)
```
qkyd-health/src/main/java/com/qkyd/health/
├── controller/HealthSubjectController.java    # 控制器
├── domain/HealthSubject.java                  # 实体类
├── mapper/HealthSubjectMapper.java            # Mapper接口
├── service/IHealthSubjectService.java         # Service接口
└── service/impl/HealthSubjectServiceImpl.java # Service实现

qkyd-health/src/main/resources/mapper/health/
└── HealthSubjectMapper.xml                    # Mapper XML
```

### 前端代码 (RuoYi-Vue3-Modern)
```
RuoYi-Vue3-Modern/src/
├── api/health/subject.js                      # API接口
└── views/health/subject/index.vue             # 页面组件
```

### SQL脚本
```
sql/
├── create_health_subject_table.sql            # 建表脚本
└── temp_restore_generator.sql                 # 恢复代码生成器
```

---

## 🚀 部署步骤

### 步骤1：创建数据库表

```bash
mysql -u root -p qkyd_jkpt < sql/create_health_subject_table.sql
```

### 步骤2：确保菜单已配置

如果之前执行过菜单优化脚本，服务对象管理菜单应该已存在。

检查菜单ID 3001是否存在：
```sql
SELECT * FROM sys_menu WHERE menu_id = 3001;
```

如果没有，执行：
```bash
mysql -u root -p qkyd_jkpt < sql/optimize_system_menus.sql
```

### 步骤3：重启后端服务

```bash
# 在 qkyd-admin 目录下
mvn clean package -DskipTests
java -jar target/qkyd-admin.jar
```

### 步骤4：访问页面

登录系统后，在菜单中找到：
**健康监测** > **服务对象管理**

---

## 📋 功能说明

### 列表功能
- ✅ 按分组树形筛选
- ✅ 按姓名、手机号、状态搜索
- ✅ 分页显示
- ✅ 列显示控制

### 操作功能
- ✅ 新增服务对象
- ✅ 修改服务对象
- ✅ 删除服务对象（逻辑删除）
- ✅ 导出Excel
- ✅ 绑定设备（预留）

### 字段说明
| 字段 | 说明 |
|------|------|
| 姓名 | 服务对象真实姓名 |
| 归属分组 | 监测分组（复用sys_dept） |
| 手机号 | 联系方式 |
| 年龄 | 年龄 |
| 性别 | 男/女/未知 |
| 状态 | 正常/停用 |

---

## 🔧 后续扩展

### 1. 设备绑定功能
当前页面已预留绑定设备按钮，需要：
- 创建 `health_device_binding` 表
- 实现绑定API
- 完善 `submitBind` 方法

### 2. 与健康数据关联
- 在健康数据表中关联 `subject_id`
- 实现服务对象健康档案查看

### 3. 权限细化
当前使用权限标识：
- `health:subject:list` - 查询
- `health:subject:add` - 新增
- `health:subject:edit` - 修改
- `health:subject:remove` - 删除
- `health:subject:export` - 导出
- `health:subject:bind` - 绑定设备

---

## ⚠️ 注意事项

1. **部门分组复用**：使用了 `sys_dept` 表作为监测分组，可在系统管理>监测分组中维护
2. **逻辑删除**：删除操作是逻辑删除（del_flag=2），数据仍保留
3. **没有登录功能**：服务对象不需要登录系统，所以没有密码字段

---

## 📝 测试数据

执行建表脚本后，已自动插入3条测试数据：
- 张三（社区A组）
- 李四（社区A组）
- 王五（社区B组）
