# 耆康云盾系统优化执行计划

## 一、问题总结与解决方案对照表

| 问题 | 解决方案 | 执行文件 | 优先级 |
|------|----------|----------|--------|
| "患者"称呼不当 | 统一改为"服务对象" | `sql/database_optimization.sql` | P0 |
| 数据库无外键/索引 | 添加复合索引、统一字段类型 | `sql/database_optimization.sql` | P0 |
| 系统菜单冗余 | 精简改造为健康监测场景 | `sql/optimize_system_menus.sql` | P1 |
| 前端路由混乱 | 新增健康业务路由配置 | `src/router/health-routes.js` | P1 |
| 设备表结构冗余 | 合并 info + extend 表 | `sql/database_optimization.sql` | P2 |

## 二、执行步骤

### 步骤1：数据库优化（立即执行）

```bash
# 1. 备份现有数据库
mysqldump -u root -p qkyd_jkpt > qkyd_jkpt_backup_$(date +%Y%m%d).sql

# 2. 执行优化脚本
mysql -u root -p qkyd_jkpt < sql/database_optimization.sql
```

### 步骤2：菜单权限改造（立即执行）

```bash
# 执行菜单优化脚本
mysql -u root -p qkyd_jkpt < sql/optimize_system_menus.sql
```

### 步骤3：前端适配（本周完成）

1. **复制健康监测路由** - 已创建 `src/router/health-routes.js`
2. **创建服务对象管理页面** - 复制 `views/system/user/index.vue` 改造
3. **隐藏冗余菜单** - 已通过数据库脚本设置 `visible='1'`

### 步骤4：数据迁移（谨慎执行）

```sql
-- 迁移设备扩展表数据到主表（执行前务必备份）
UPDATE qkyd_device_info d
INNER JOIN qkyd_device_info_extend e ON d.id = e.device_id
SET 
    d.last_communication_time = e.last_communication_time,
    d.battery_level = e.battery_level,
    d.step = e.step,
    d.heart_rate = e.heart_rate,
    d.heart_rate_time = e.heart_rate_time,
    d.temp = e.temp,
    d.temp_time = e.temp_time,
    d.blood_diastolic = e.blood_diastolic,
    d.blood_systolic = e.blood_systolic,
    d.blood_time = e.blood_time,
    d.spo2 = e.spo2,
    d.spo2_time = e.spo2_time,
    d.longitude = e.longitude,
    d.latitude = e.latitude,
    d.location = e.location;

-- 确认迁移成功后，删除扩展表
-- DROP TABLE qkyd_device_info_extend;
```

## 三、术语统一规范

| 场景 | 推荐使用 | 避免使用 |
|------|----------|----------|
| 监测对象 | 服务对象、佩戴者 | 患者、病人 |
| 系统使用者 | 操作员、管理员、医生 | 用户（歧义） |
| 设备绑定 | 设备绑定、关联设备 | 设备注册 |
| 告警处理 | 告警确认、告警处理 | 告警消除 |
| 分组管理 | 监测分组、关怀分组 | 部门（OA概念） |

## 四、保留 vs 移除功能清单

### ✅ 保留功能（健康监测必需）
- [x] 登录认证 + JWT Token
- [x] 角色权限控制（RBAC）
- [x] 操作日志审计
- [x] 字典管理（改造为健康专用）
- [x] 在线用户监控
- [x] 服务状态监控

### ❌ 隐藏/禁用功能（与业务无关）
- [ ] 岗位管理（post）
- [ ] 表单构建器（build）
- [ ] 代码生成器（gen）
- [ ] Swagger接口文档（生产环境）
- [ ] 定时任务（暂时隐藏）
- [ ] Druid监控（生产环境关闭）

### 🔄 改造功能（业务适配）
- [ ] 部门管理 → 监测分组管理
- [ ] 用户管理 → 服务对象管理
- [ ] 通知公告 → 系统告警通知

## 五、后续开发建议

### 前端结构规划
```
src/views/
├── health/                    # 健康监测业务模块（新增）
│   ├── subject/index.vue      # 服务对象管理
│   ├── device/index.vue       # 设备管理
│   ├── alert/index.vue        # 告警处理
│   ├── report/index.vue       # 健康报告
│   └── dashboard/index.vue    # 监测大屏
├── ai/                        # AI算法模块（已存在）
│   ├── dashboard/index.vue    # 算法总览
│   └── ...
└── system/                    # 系统管理（精简后保留）
    ├── user/index.vue         # 系统用户（保留但弱化）
    ├── dept/index.vue         # 监测分组
    └── ...
```

### 开发优先级
1. **Week1**: 执行数据库优化、创建服务对象管理页面
2. **Week2**: 设备管理 + 设备绑定功能
3. **Week3**: 告警管理 + 告警处理工作流
4. **Week4**: 健康报告 + PDF导出

## 六、风险提醒

1. **数据库脚本执行前务必备份**
2. **设备扩展表合并操作不可逆，先验证再执行DROP**
3. **菜单隐藏后如需恢复，执行**: `UPDATE sys_menu SET visible='0' WHERE menu_id=xxx;`
