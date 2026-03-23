# 快速参考卡

## 🎯 核心代码片段

### 1. 导入
```typescript
import ProcessingChainPanel from '@/components/ProcessingChainPanel.vue'
import ProcessingChainProgress from '@/components/ProcessingChainProgress.vue'
import { generateMockProcessingChain, enrichExceptionWithChain } from '@/utils/mockProcessingChain'
```

### 2. 状态
```typescript
const showChainPanel = ref(false)
const selectedChainData = ref<any>(null)
const processingChain = ref<any>(null)
```

### 3. 数据加载
```typescript
const response = await getIndexException()
exceptionList.value = response.data.map((item: any) =>
  enrichExceptionWithChain(item)
)
```

### 4. 事件处理
```typescript
const handleRowClick = async (row: ExceptionRow) => {
  const response = { data: generateMockProcessingChain(row.id) }
  processingChain.value = response.data
  selectedChainData.value = row
  showChainPanel.value = true
}
```

### 5. 表格列
```vue
<el-table-column prop="processingProgress" label="处理进度" min-width="150">
  <template #default="{ row }">
    <ProcessingChainProgress v-if="row.stages" :stages="row.stages" />
  </template>
</el-table-column>
```

### 6. 表格事件
```vue
<el-table @row-click="handleRowClick">
```

### 7. 详情面板
```vue
<ProcessingChainPanel
  v-model="showChainPanel"
  :data="selectedChainData"
  :chain="processingChain"
/>
```

---

## 📊 数据结构速查

### 异常行
```typescript
{
  id: 'EVT-001',
  nickName: '张三',
  type: '心率异常',
  state: '1',
  location: '北京',
  stages: [
    { name: '异常检测', status: 'completed' },
    { name: '事件产生', status: 'completed' },
    // ...
  ]
}
```

### 处理链
```typescript
{
  stages: [
    {
      name: '异常检测',
      status: 'completed',
      timestamp: '2026-03-23T09:15:30Z',
      details: { anomalies: [...] }
    },
    // ... 6 more stages
  ],
  totalDuration: 6
}
```

---

## 🔧 常用命令

### 开发
```bash
npm run dev          # 启动开发服务器
npm run build        # 构建生产版本
npm run preview      # 预览生产版本
```

### 测试
```bash
npm run test         # 运行测试
npm run test:ui      # UI测试
npm run coverage     # 覆盖率报告
```

### 代码质量
```bash
npm run lint         # 代码检查
npm run format       # 代码格式化
```

---

## 🎨 样式速查

### 进度条
```vue
<ProcessingChainProgress :stages="row.stages" />
```

### 详情面板
```vue
<ProcessingChainPanel
  v-model="showChainPanel"
  :data="selectedChainData"
  :chain="processingChain"
/>
```

---

## 🐛 常见问题速查

| 问题 | 解决方案 |
|------|----------|
| 进度条不显示 | 检查 stages 字段是否存在 |
| 面板打不开 | 检查 handleRowClick 是否绑定 |
| 数据显示错误 | 检查数据结构是否正确 |
| 样式不对 | 检查 CSS 是否加载 |
| 控制台错误 | 查看浏览器控制台错误信息 |

---

## 📱 响应式断点

```scss
// 手机
@media (max-width: 768px) {
  // 手机样式
}

// 平板
@media (min-width: 768px) and (max-width: 1024px) {
  // 平板样式
}

// 桌面
@media (min-width: 1024px) {
  // 桌面样式
}
```

---

## 🎯 集成时间表

| 步骤 | 时间 | 状态 |
|------|------|------|
| 导入组件 | 5分钟 | ⏳ |
| 添加状态 | 5分钟 | ⏳ |
| 修改数据加载 | 5分钟 | ⏳ |
| 添加事件处理 | 5分钟 | ⏳ |
| 修改表格模板 | 10分钟 | ⏳ |
| 添加详情面板 | 5分钟 | ⏳ |
| 测试验证 | 20分钟 | ⏳ |
| **总计** | **55分钟** | ⏳ |

---

## 📚 文档导航

```
项目文档
├── QUICK_START.md (5分钟)
├── QUICK_REFERENCE.md (本文档)
├── INTEGRATION_GUIDE.md (15分钟)
├── IMPLEMENTATION_STEPS.md (20分钟)
├── DASHBOARD_INTEGRATION_EXAMPLE.vue (代码示例)
├── PROJECT_SUMMARY.md (项目总结)
└── CHECKLIST.md (检查清单)
```

---

## 🚀 快速开始

### 第1分钟: 复制文件
```bash
# 文件已创建在:
# - src/components/ProcessingChainPanel.vue
# - src/components/ProcessingChainProgress.vue
# - src/api/processingChain.ts
# - src/utils/mockProcessingChain.ts
```

### 第2-5分钟: 导入和添加状态
```typescript
// 在 DashboardView.vue 中
import ProcessingChainPanel from '@/components/ProcessingChainPanel.vue'
import ProcessingChainProgress from '@/components/ProcessingChainProgress.vue'
import { generateMockProcessingChain, enrichExceptionWithChain } from '@/utils/mockProcessingChain'

const showChainPanel = ref(false)
const selectedChainData = ref<any>(null)
const processingChain = ref<any>(null)
```

### 第6-10分钟: 修改数据和事件
```typescript
// 修改数据加载
exceptionList.value = response.data.map((item: any) =>
  enrichExceptionWithChain(item)
)

// 添加事件处理
const handleRowClick = async (row: ExceptionRow) => {
  const response = { data: generateMockProcessingChain(row.id) }
  processingChain.value = response.data
  selectedChainData.value = row
  showChainPanel.value = true
}
```

### 第11-15分钟: 修改模板
```vue
<!-- 添加进度条列 -->
<el-table-column prop="processingProgress" label="处理进度" min-width="150">
  <template #default="{ row }">
    <ProcessingChainProgress v-if="row.stages" :stages="row.stages" />
  </template>
</el-table-column>

<!-- 修改行点击 -->
<el-table @row-click="handleRowClick">

<!-- 添加详情面板 -->
<ProcessingChainPanel
  v-model="showChainPanel"
  :data="selectedChainData"
  :chain="processingChain"
/>
```

### 第16-20分钟: 测试
```bash
npm run dev
# 打开浏览器，点击异常行，验证功能
```

---

## ✅ 验收清单 (简版)

- [ ] 异常列表显示进度条
- [ ] 点击异常行打开面板
- [ ] 面板显示7个阶段
- [ ] 时间轴显示正确
- [ ] 没有控制台错误

---

## 🔗 相关链接

- [Vue 3 文档](https://vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)
- [TypeScript 文档](https://www.typescriptlang.org/)

---

## 💡 提示

💡 **提示1**: 使用模拟数据快速测试，后期再连接真实API

💡 **提示2**: 查看浏览器控制台了解数据结构

💡 **提示3**: 使用 Vue DevTools 调试组件状态

💡 **提示4**: 参考 DASHBOARD_INTEGRATION_EXAMPLE.vue 了解完整代码

💡 **提示5**: 遇到问题先查看 CHECKLIST.md

---

**最后更新**: 2026-03-23

**版本**: 1.0
