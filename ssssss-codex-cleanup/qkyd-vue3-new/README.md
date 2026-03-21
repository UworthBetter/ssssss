# 系统管理平台

基于 Vue 3 + Element Plus + TypeScript 的后台管理系统

## 技术栈

- Vue 3
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Vite

## 功能特性

- 登录/登出
- 用户管理
- 菜单管理
- 角色管理
- 首页仪表盘

## 项目结构

```
src/
├── api/          # API 接口
├── assets/       # 静态资源
├── components/   # 公共组件
├── layout/       # 布局组件
├── router/       # 路由配置
├── store/        # 状态管理
├── types/        # 类型定义
├── utils/        # 工具函数
├── views/        # 页面视图
└── main.ts       # 入口文件
```

## 安装依赖

```bash
npm install
```

## 启动项目

```bash
npm run dev
```

## 构建项目

```bash
npm run build
```

## 后端接口

- 登录: POST /login
- 获取用户信息: GET /getInfo
- 获取菜单: GET /system/menu/treeselect
- 用户列表: GET /system/user/list

## 开发说明

- 代码遵循 Vue 3 Composition API 规范
- 使用 TypeScript 进行类型检查
- 遵循 Element Plus 组件使用规范
