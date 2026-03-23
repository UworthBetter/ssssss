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

---

## 🎉 处理链展示方案 (新增)

### 项目完成

已完成处理链展示功能的完整实现，包括前端组件、API服务、工具函数和详细文档。

### 📦 交付内容

**核心组件** (2个)
- `src/components/ProcessingChainPanel.vue` - 处理链详情面板
- `src/components/ProcessingChainProgress.vue` - 处理链进度条

**API服务** (1个)
- `src/api/processingChain.ts` - 处理链API

**工具函数** (1个)
- `src/utils/mockProcessingChain.ts` - 模拟数据生成器

**文档** (8个)
- `QUICK_START.md` - 5分钟快速开始
- `QUICK_REFERENCE.md` - 快速参考卡
- `INTEGRATION_GUIDE.md` - 详细集成指南
- `IMPLEMENTATION_STEPS.md` - 完整实现步骤
- `DASHBOARD_INTEGRATION_EXAMPLE.vue` - 代码示例
- `PROJECT_SUMMARY.md` - 项目总结
- `CHECKLIST.md` - 检查清单
- `FILES_MANIFEST.md` - 文件清单

### 🚀 快速开始

1. **阅读文档** (5分钟)
   ```bash
   cat QUICK_START.md
   ```

2. **查看代码示例** (5分钟)
   ```bash
   cat DASHBOARD_INTEGRATION_EXAMPLE.vue
   ```

3. **集成代码** (20分钟)
   - 导入组件和工具函数
   - 修改DashboardView.vue
   - 添加事件处理
   - 修改表格模板

4. **测试验证** (10分钟)
   ```bash
   npm run dev
   # 打开浏览器测试
   ```

### 📚 文档导航

| 文档 | 用途 | 时间 |
|------|------|------|
| QUICK_START.md | 快速开始 | 5分钟 |
| QUICK_REFERENCE.md | 快速参考 | 3分钟 |
| INTEGRATION_GUIDE.md | 详细集成 | 15分钟 |
| IMPLEMENTATION_STEPS.md | 完整实现 | 20分钟 |
| PROJECT_SUMMARY.md | 项目总结 | 20分钟 |
| CHECKLIST.md | 检查清单 | 10分钟 |
| FILES_MANIFEST.md | 文件清单 | 5分钟 |

### 🎯 核心功能

1. **处理链进度显示** - 在异常列表中显示紧凑的进度条
2. **处理链详情展示** - 点击异常行打开详情面板，显示7个处理阶段
3. **数据模拟** - 提供完整的模拟数据生成器

### 📊 技术栈

- Vue 3 + Composition API
- TypeScript
- Element Plus
- SCSS

### ✅ 验收标准

- ✅ 所有7个处理阶段显示
- ✅ 时间轴正确展示
- ✅ 详情信息完整
- ✅ 交互流畅
- ✅ 无TypeScript错误
- ✅ 无控制台警告

### 📈 预计集成时间

**总计**: 30-60分钟

- 导入组件: 5分钟
- 添加状态: 5分钟
- 修改数据加载: 5分钟
- 添加事件处理: 5分钟
- 修改表格模板: 10分钟
- 添加详情面板: 5分钟
- 测试验证: 20分钟

### 🔄 后端API对接

当前使用模拟数据，后期可连接真实API:

```
GET /api/ai/event-processing/chain/{eventId}
GET /api/ai/event-processing/detail/{eventId}
GET /api/ai/event-processing/audit/{eventId}
```

详见 `PROJECT_SUMMARY.md` 中的后端API说明。

### 📞 技术支持

遇到问题请查看相关文档或参考 `DASHBOARD_INTEGRATION_EXAMPLE.vue` 中的完整代码。

---

**项目完成日期**: 2026-03-23

**难度等级**: ⭐⭐ (中等)

**状态**: ✅ 完成
