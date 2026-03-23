# 快速集成指南 - 处理链页面

## 一句话总结
已创建完整的处理链页面 `ProcessingChainView.vue`，可以直接运行，无需任何修改。

## 集成步骤 (3步)

### 第1步: 添加路由

在 `src/router/index.ts` 中添加:

```typescript
{
  path: '/processing-chain',
  component: () => import('@/views/ProcessingChainView.vue'),
  meta: { title: '处理链' }
}
```

### 第2步: 添加菜单

在你的菜单配置中添加:

```typescript
{
  path: '/processing-chain',
  component: 'Layout',
  children: [
    {
      path: '',
      component: 'ProcessingChainView',
      meta: { title: '处理链' }
    }
  ]
}
```

### 第3步: 运行

```bash
npm run dev
```

然后访问: `http://localhost:5173/processing-chain`

## 文件位置

- 页面文件: `src/views/ProcessingChainView.vue`
- 已包含所有功能:
  - ✅ 异常列表
  - ✅ 处理进度条
  - ✅ 详情面板
  - ✅ 时间轴
  - ✅ 模拟数据

## 功能说明

### 异常列表
- 显示10条模拟异常数据
- 包含患者名、异常类型、事件时间、处理进度
- 点击行打开详情面板

### 处理进度条
- 显示处理进度百分比
- 颜色表示状态:
  - 绿色: 已完成 (100%)
  - 蓝色: 处理中 (1-99%)
  - 灰色: 待处理 (0%)

### 详情面板
- 显示7个处理阶段
- 时间轴展示处理过程
- 每个阶段显示详情信息
- 状态图标:
  - ✓ 已完成
  - ⟳ 处理中
  - ⏱ 待处理

## 完全独立

这个页面完全独立，不依赖 DashboardView.vue，可以直接运行。

## 后续集成到 DashboardView

如果要集成到 DashboardView 中，只需:

1. 在 DashboardView 中导入组件
2. 添加一个标签页或按钮打开处理链页面
3. 或者直接在表格中添加处理进度列

## 测试数据

页面使用完全模拟数据，包括:
- 10条异常记录
- 7个处理阶段
- 随机的处理进度
- 随机的异常类型

## 样式

使用 Element Plus 组件库，样式已完全集成，无需额外配置。

## 下一步

1. 添加路由
2. 运行项目
3. 访问页面
4. 测试功能
5. 连接真实API (可选)
