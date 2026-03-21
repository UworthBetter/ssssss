# 耆康云盾健康监测平台前端（qkyd）

本目录为耆康云盾健康监测平台管理端，基于 Vue 3、TypeScript、
Element Plus 与 Vite 构建。

## 技术栈

- Vue 3
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Vite

## 核心能力

- 平台登录与会话管理
- 健康总览与运营看板
- 服务对象管理
- 设备管理
- 异常告警处置
- AI 工作台

## 开发命令

```bash
npm install
npm run dev
```

## 构建命令

```bash
npm run build
```

## 后端接口约定

- 登录：`POST /login`
- 获取用户信息：`GET /getInfo`
- 菜单树：`GET /system/menu/treeselect`
- 用户列表：`GET /system/user/list`
