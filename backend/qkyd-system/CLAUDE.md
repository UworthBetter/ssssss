# qkyd-system 模块

[根目录](../CLAUDE.md) > **qkyd-system**

---

## 变更记录 (Changelog)

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 初始化模块文档 |

---

## 模块职责

qkyd-system 是系统管理模块，提供：

1. **用户管理**：用户增删改查、密码重置、状态管理
2. **角色管理**：角色权限分配、数据权限配置
3. **菜单管理**：菜单树管理、按钮权限配置
4. **部门管理**：组织机构树形管理
5. **岗位管理**：岗位信息维护
6. **字典管理**：系统字典类型和数据管理
7. **参数配置**：系统参数动态配置
8. **通知公告**：系统通知发布
9. **操作日志**：用户操作日志记录查询
10. **登录日志**：登录日志记录查询

---

## 数据模型

### 核心实体 (domain)
| 类名 | 表名 | 功能 |
|------|------|------|
| `SysUser` | `sys_user` | 系统用户 |
| `SysRole` | `sys_role` | 系统角色 |
| `SysMenu` | `sys_menu` | 系统菜单 |
| `SysDept` | `sys_dept` | 系统部门 |
| `SysPost` | `sys_post` | 系统岗位 |
| `SysDictType` | `sys_dict_type` | 字典类型 |
| `SysDictData` | `sys_dict_data` | 字典数据 |
| `SysConfig` | `sys_config` | 系统参数 |
| `SysNotice` | `sys_notice` | 通知公告 |
| `SysOperLog` | `sys_oper_log` | 操作日志 |
| `SysLogininfor` | `sys_logininfor` | 登录日志 |
| `SysUserOnline` | 在线用户 | 视图 |
| `SysUserRole` | `sys_user_role` | 用户角色关联 |
| `SysUserPost` | `sys_user_post` | 用户岗位关联 |
| `SysRoleMenu` | `sys_role_menu` | 角色菜单关联 |
| `SysRoleDept` | `sys_role_dept` | 角色部门关联 |

### 视图对象 (domain/vo)
| 类名 | 功能 |
|------|------|
| `MetaVo` | 元数据视图 |
| `RouterVo` | 路由视图对象（用于前端路由生成） |

---

## 服务层

### 核心服务接口
| 服务接口 | 功能 |
|----------|------|
| `ISysUserService` | 用户管理服务 |
| `ISysRoleService` | 角色管理服务 |
| `ISysMenuService` | 菜单管理服务 |
| `ISysDeptService` | 部门管理服务 |
| `ISysPostService` | 岗位管理服务 |
| `ISysDictTypeService` | 字典类型服务 |
| `ISysDictDataService` | 字典数据服务 |
| `ISysConfigService` | 参数配置服务 |
| `ISysNoticeService` | 通知公告服务 |
| `ISysOperLogService` | 操作日志服务 |
| `ISysLogininforService` | 登录日志服务 |
| `ISysUserOnlineService` | 在线用户服务 |

### 服务实现 (impl)
所有服务实现类位于 `service/impl/` 目录下。

---

## 数据访问层

### Mapper 接口
| Mapper | 功能 |
|--------|------|
| `SysUserMapper` | 用户数据访问 |
| `SysRoleMapper` | 角色数据访问 |
| `SysMenuMapper` | 菜单数据访问 |
| `SysDeptMapper` | 部门数据访问 |
| `SysPostMapper` | 岗位数据访问 |
| `SysDictTypeMapper` | 字典类型数据访问 |
| `SysDictDataMapper` | 字典数据访问 |
| `SysConfigMapper` | 参数配置数据访问 |
| `SysNoticeMapper` | 通知公告数据访问 |
| `SysOperLogMapper` | 操作日志数据访问 |
| `SysLogininforMapper` | 登录日志数据访问 |
| `SysUserRoleMapper` | 用户角色关联数据访问 |
| `SysUserPostMapper` | 用户岗位关联数据访问 |
| `SysRoleMenuMapper` | 角色菜单关联数据访问 |
| `SysRoleDeptMapper` | 角色部门关联数据访问 |

---

## 权限模型

### RBAC 权限模型
```
用户 (SysUser)
  ├─ 用户岗位 (SysUserPost)
  │    └─ 岗位 (SysPost)
  └─ 用户角色 (SysUserRole)
       └─ 角色 (SysRole)
            ├─ 角色菜单 (SysRoleMenu)
            │    └─ 菜单 (SysMenu)
            └─ 角色部门 (SysRoleDept)
                 └─ 部门 (SysDept)
```

### 数据权限
- 全部数据权限
- 自定义数据权限
- 本部门数据权限
- 本部门及以下数据权限
- 仅本人数据权限

---

## 菜单类型

| 类型 | 编码 | 说明 |
|------|------|------|
| 目录 | M | 一级菜单 |
| 菜单 | C | 页面菜单 |
| 按钮 | F | 页面按钮权限 |

---

## 字典类型

### 常用系统字典
- `sys_user_sex`：用户性别
- `sys_show_hide`：显示隐藏
- `sys_normal_disable`：正常停用
- `sys_job_status`：任务状态
- `sys_job_group`：任务分组
- `sys_yes_no`：是否
- `sys_notice_type`：通知类型
- `sys_notice_status`：通知状态
- `sys_oper_type`：操作类型
- `sys_common_status`：通用状态

---

## 相关文件清单

```
qkyd-system/
├── src/main/java/com/qkyd/system/
│   ├── domain/                 # 实体类
│   │   ├── SysUser.java
│   │   ├── SysRole.java
│   │   ├── SysMenu.java
│   │   ├── SysDept.java
│   │   ├── SysPost.java
│   │   ├── SysDictType.java
│   │   ├── SysDictData.java
│   │   ├── SysConfig.java
│   │   ├── SysNotice.java
│   │   ├── SysOperLog.java
│   │   ├── SysLogininfor.java
│   │   ├── SysUserOnline.java
│   │   ├── SysUserRole.java
│   │   ├── SysUserPost.java
│   │   ├── SysRoleMenu.java
│   │   ├── SysRoleDept.java
│   │   ├── SysCache.java
│   │   └── vo/
│   │       ├── MetaVo.java
│   │       └── RouterVo.java
│   ├── mapper/                 # 数据访问层
│   │   ├── SysUserMapper.java
│   │   ├── SysRoleMapper.java
│   │   ├── SysMenuMapper.java
│   │   ├── SysDeptMapper.java
│   │   ├── SysPostMapper.java
│   │   ├── SysDictTypeMapper.java
│   │   ├── SysDictDataMapper.java
│   │   ├── SysConfigMapper.java
│   │   ├── SysNoticeMapper.java
│   │   ├── SysOperLogMapper.java
│   │   ├── SysLogininforMapper.java
│   │   ├── SysUserRoleMapper.java
│   │   ├── SysUserPostMapper.java
│   │   ├── SysRoleMenuMapper.java
│   │   └── SysRoleDeptMapper.java
│   └── service/                # 服务层
│       ├── ISysUserService.java
│       ├── ISysRoleService.java
│       ├── ISysMenuService.java
│       ├── ISysDeptService.java
│       ├── ISysPostService.java
│       ├── ISysDictTypeService.java
│       ├── ISysDictDataService.java
│       ├── ISysConfigService.java
│       ├── ISysNoticeService.java
│       ├── ISysOperLogService.java
│       ├── ISysLogininforService.java
│       ├── ISysUserOnlineService.java
│       └── impl/                # 服务实现
│           ├── SysUserServiceImpl.java
│           ├── SysRoleServiceImpl.java
│           ├── SysMenuServiceImpl.java
│           ├── SysDeptServiceImpl.java
│           ├── SysPostServiceImpl.java
│           ├── SysDictTypeServiceImpl.java
│           ├── SysDictDataServiceImpl.java
│           ├── SysConfigServiceImpl.java
│           ├── SysNoticeServiceImpl.java
│           ├── SysOperLogServiceImpl.java
│           ├── SysLogininforServiceImpl.java
│           └── SysUserOnlineServiceImpl.java
└── src/main/resources/mapper/system/  # MyBatis XML 映射
    ├── SysUserMapper.xml
    ├── SysRoleMapper.xml
    ├── SysMenuMapper.xml
    ├── SysDeptMapper.xml
    ├── SysPostMapper.xml
    ├── SysDictTypeMapper.xml
    ├── SysDictDataMapper.xml
    ├── SysConfigMapper.xml
    ├── SysNoticeMapper.xml
    ├── SysOperLogMapper.xml
    ├── SysLogininforMapper.xml
    ├── SysUserRoleMapper.xml
    ├── SysUserPostMapper.xml
    ├── SysRoleMenuMapper.xml
    └── SysRoleDeptMapper.xml
```

---

**最后更新**: 2026-02-01 14:18:39
