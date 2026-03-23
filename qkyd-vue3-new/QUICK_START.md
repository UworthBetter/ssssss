# 快速开始指南

## 🎯 5分钟快速集成

### 1️⃣ 复制组件文件

已为你创建的文件:
```
✅ src/components/ProcessingChainPanel.vue
✅ src/components/ProcessingChainProgress.vue
✅ src/api/processingChain.ts
✅ src/utils/mockProcessingChain.ts
```

### 2️⃣ 在DashboardView.vue中导入

```typescript
// 在 <script setup> 顶部添加
import ProcessingChainPanel from '@/components/ProcessingChainPanel.vue'
import ProcessingChainProgress from '@/components/ProcessingChainProgress.vue'
import { generateMockProcessingChain, enrichExceptionWithChain } from '@/utils/mockProcessingChain'

// 新增状态
const showChainPanel = ref(false)
const selectedChainData = ref<any>(null)
const processingChain = ref<any>(null)
```

### 3️⃣ 修改异常列表加载

```typescript
// 在 loadData() 或 getIndexException() 的回调中
const response = await getIndexException()
exceptionList.value = response.data.map((item: any) =>
  enrichExceptionWithChain(item)
)
```

### 4️⃣ 添加行点击事件

```typescript
const handleRowClick = async (row: ExceptionRow) => {
  const response = { data: generateMockProcessingChain(row.id) }
  processingChain.value = response.data
  selectedChainData.value = row
  showChainPanel.value = true
}
```

### 5️⃣ 修改表格模板

在 `EVENT STREAM` 表格中:

```vue
<!-- 添加处理进度列 -->
<el-table-column prop="processingProgress" label="处理进度" min-width="150">
  <template #default="{ row }">
    <ProcessingChainProgress v-if="row.stages" :stages="row.stages" />
  </template>
</el-table-column>

<!-- 修改行点击事件 -->
<el-table @row-click="handleRowClick">
```

### 6️⃣ 添加详情面板

在模板底部添加:

```vue
<ProcessingChainPanel
  v-model="showChainPanel"
  :data="selectedChainData"
  :chain="processingChain"
/>
```

---

## ✨ 效果预览

### 异常列表
```
姓名    类型        处理进度              状态      位置
─────────────────────────────────────────────────────────
张三    心率异常    ████████████ 100%    RESOLVED  北京
李四    血氧异常    ████████░░░░  67%    PENDING   上海
王五    体温异常    ░░░░░░░░░░░░   0%    PENDING   广州
```

### 点击异常后的详情面板
```
┌─────────────────────────────────────────┐
│  处理链详情                              │
├─────────────────────────────────────────┤
│  患者: 张三  异常类型: 心率异常          │
│  事件ID: EVT-20260323-001  总耗时: 6ms  │
│                                         │
│  [1] 异常检测 ✓                         │
│      心率异常: 120 bpm (高)             │
│      血氧异常: 92% (中)                 │
│                                         │
│  [2] 事件产生 ✓                         │
│      事件类型: 心血管异常                │
│                                         │
│  [3] AI解析与上下文补全 ✓               │
│      患者信息: 张三, 65岁, 男            │
│      病史: 高血压, 糖尿病                │
│                                         │
│  [4] 风险评估 ✓                         │
│      风险等级: 高风险                    │
│      风险分数: 8.5/10                   │
│      置信度: 92%                        │
│                                         │
│  [5] 处置建议 ✓                         │
│      建议: 立即就医                      │
│      优先级: P1 (紧急)                  │
│                                         │
│  [6] 自动执行 ✓                         │
│      执行状态: 成功                      │
│      通知对象: 患者, 家属, 医生          │
│                                         │
│  [7] 留痕记录 ✓                         │
│      算法版本: v2.0                     │
│      处理耗时: 6ms                      │
└─────────────────────────────────────────┘
```

---

## 🧪 测试

### 本地测试

1. 启动开发服务器
   ```bash
   npm run dev
   ```

2. 打开浏览器访问应用

3. 在异常列表中看到处理进度条

4. 点击任意异常行

5. 应该看到处理链详情面板弹出

### 验证清单

- [ ] 异常列表显示处理进度条
- [ ] 点击异常行打开详情面板
- [ ] 详情面板显示7个处理阶段
- [ ] 时间轴正确显示
- [ ] 各阶段信息正确展示
- [ ] 没有控制台错误

---

## 🔧 常见问题

### Q: 处理进度条不显示?
A: 检查异常数据是否包含 `stages` 字段。使用 `enrichExceptionWithChain()` 函数添加。

### Q: 详情面板打不开?
A: 检查 `handleRowClick` 是否正确绑定到表格的 `@row-click` 事件。

### Q: 数据显示不正确?
A: 检查 `generateMockProcessingChain()` 返回的数据结构是否正确。

### Q: 样式不对?
A: 确保 Element Plus 和 SCSS 已正确配置。

---

## 📊 数据结构

### 异常行数据
```typescript
interface ExceptionRow {
  id: string | number
  nickName: string
  type: string
  state: string
  location: string
  stages?: Array<{
    name: string
    status: 'completed' | 'processing' | 'pending'
  }>
}
```

### 处理链数据
```typescript
interface ProcessingChain {
  stages: Array<{
    name: string
    status: 'completed' | 'processing' | 'pending'
    timestamp: string
    details: Record<string, any>
  }>
  totalDuration: number
}
```

---

## 🚀 下一步

### 短期 (本周)
- [ ] 集成到DashboardView
- [ ] 本地测试验证
- [ ] 调整样式和布局

### 中期 (本月)
- [ ] 连接真实API
- [ ] 添加实时更新
- [ ] 性能优化

### 长期 (下月)
- [ ] 添加导出功能
- [ ] 添加动画效果
- [ ] 添加更多交互

---

## 📞 技术支持

如有问题，请查看:
- `INTEGRATION_GUIDE.md` - 详细集成指南
- `IMPLEMENTATION_STEPS.md` - 完整实现步骤
- `DASHBOARD_INTEGRATION_EXAMPLE.vue` - 完整代码示例
- `FRONTEND_DISPLAY_ANALYSIS.md` - 设计分析文档

---

## ✅ 集成完成标志

当你看到以下效果时，说明集成成功:

✅ 异常列表中每行都有处理进度条
✅ 点击异常行弹出详情面板
✅ 详情面板显示完整的处理链
✅ 时间轴清晰展示各阶段
✅ 没有控制台错误

---

**预计集成时间: 30-60分钟**

**难度等级: ⭐⭐ (中等)**
