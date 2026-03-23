# 方案A实现步骤

## 📋 已完成的文件

✅ `src/components/ProcessingChainPanel.vue` - 处理链详情面板
✅ `src/components/ProcessingChainProgress.vue` - 处理链进度条
✅ `src/api/processingChain.ts` - API服务
✅ `src/utils/mockProcessingChain.ts` - 模拟数据生成器
✅ `INTEGRATION_GUIDE.md` - 集成指南

---

## 🔧 集成步骤

### 步骤1: 导入组件和API

在 `src/views/DashboardView.vue` 的 `<script setup>` 中添加:

```typescript
import ProcessingChainPanel from '@/components/ProcessingChainPanel.vue'
import ProcessingChainProgress from '@/components/ProcessingChainProgress.vue'
import { getProcessingChain } from '@/api/processingChain'
import { generateMockProcessingChain, enrichExceptionWithChain } from '@/utils/mockProcessingChain'

// 新增状态
const showChainPanel = ref(false)
const selectedChainData = ref<any>(null)
const processingChain = ref<any>(null)
```

### 步骤2: 修改异常列表数据

在获取异常列表后，为每条数据添加处理链信息:

```typescript
// 在 getIndexException() 或 getRecentAbnormal() 的回调中
const response = await getIndexException()
exceptionList.value = response.data.map((item: any) =>
  enrichExceptionWithChain(item)
)
```

### 步骤3: 修改表格模板

在 `EVENT STREAM` 表格中添加处理进度列:

```vue
<el-table-column prop="processingProgress" label="处理进度" min-width="150">
  <template #default="{ row }">
    <ProcessingChainProgress v-if="row.stages" :stages="row.stages" />
  </template>
</el-table-column>
```

### 步骤4: 修改行点击事件

替换现有的 `handleRowClick` 函数:

```typescript
const handleRowClick = async (row: ExceptionRow) => {
  try {
    // 使用模拟数据（开发阶段）
    // const response = await getProcessingChain(row.id)
    const response = { data: generateMockProcessingChain(row.id) }

    processingChain.value = response.data
    selectedChainData.value = row
    showChainPanel.value = true
  } catch (error) {
    ElMessage.error('获取处理链详情失败')
  }
}
```

### 步骤5: 添加组件到模板

在 `</template>` 前添加:

```vue
<!-- 处理链详情面板 -->
<ProcessingChainPanel
  v-model="showChainPanel"
  :data="selectedChainData"
  :chain="processingChain"
/>
```

### 步骤6: 注册组件

在 `<script setup>` 中确保组件已导入（Vue 3 自动注册）

---

## 🧪 测试

### 本地测试（使用模拟数据）

```typescript
// 在 handleRowClick 中使用模拟数据
const handleRowClick = async (row: ExceptionRow) => {
  const response = { data: generateMockProcessingChain(row.id) }
  processingChain.value = response.data
  selectedChainData.value = row
  showChainPanel.value = true
}
```

### 集成测试（使用真实API）

```typescript
// 后端API准备好后，切换到真实API
const handleRowClick = async (row: ExceptionRow) => {
  try {
    const response = await getProcessingChain(row.id)
    processingChain.value = response.data
    selectedChainData.value = row
    showChainPanel.value = true
  } catch (error) {
    ElMessage.error('获取处理链详情失败')
  }
}
```

---

## 📊 完整的DashboardView.vue修改示例

### 导入部分

```typescript
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Location, Close } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { PieChart, LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent } from 'echarts/components'
import { LabelLayout } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import AMapLoader from '@amap/amap-jsapi-loader'
import { ElMessage } from 'element-plus'
import { getRecentAbnormal } from '@/api/ai'
import { getAgeSexGroupCount, getIndexException, getRealTimeData } from '@/api/index'
// 新增导入
import ProcessingChainPanel from '@/components/ProcessingChainPanel.vue'
import ProcessingChainProgress from '@/components/ProcessingChainProgress.vue'
import { getProcessingChain } from '@/api/processingChain'
import { generateMockProcessingChain, enrichExceptionWithChain } from '@/utils/mockProcessingChain'

echarts.use([PieChart, LineChart, TooltipComponent, GridComponent, LabelLayout, CanvasRenderer])

// ... 现有接口定义

// 新增状态
const showChainPanel = ref(false)
const selectedChainData = ref<any>(null)
const processingChain = ref<any>(null)

// ... 现有状态
```

### 修改异常列表获取

```typescript
const loadExceptionList = async () => {
  try {
    fetching.value = true
    const response = await getIndexException()
    // 为每条异常添加处理链信息
    exceptionList.value = response.data.map((item: any) =>
      enrichExceptionWithChain(item)
    )
  } catch (error) {
    console.error('Failed to load exception list:', error)
  } finally {
    fetching.value = false
  }
}
```

### 修改行点击事件

```typescript
const handleRowClick = async (row: ExceptionRow) => {
  try {
    // 开发阶段使用模拟数据
    const response = { data: generateMockProcessingChain(row.id) }
    // 生产环境切换到真实API
    // const response = await getProcessingChain(row.id)

    processingChain.value = response.data
    selectedChainData.value = row
    showChainPanel.value = true
  } catch (error) {
    ElMessage.error('获取处理链详情失败')
  }
}
```

### 表格模板修改

```vue
<el-table :data="filteredExceptionList" height="100%" class="holo-table clickable-table" @row-click="handleRowClick">
  <el-table-column prop="nickName" label="姓名" min-width="80" />
  <el-table-column prop="type" label="类型" min-width="110" />
  <!-- 新增处理进度列 -->
  <el-table-column prop="processingProgress" label="处理进度" min-width="150">
    <template #default="{ row }">
      <ProcessingChainProgress v-if="row.stages" :stages="row.stages" />
    </template>
  </el-table-column>
  <el-table-column prop="state" label="状态" min-width="90">
    <template #default="{ row }">
      <span :class="['status-dot', row.state === '1' ? 'dot-ok' : 'dot-warn']"></span>
      <span :class="['status-text', row.state === '1' ? 'text-ok' : 'text-warn']">
        {{ row.state === '1' ? 'RESOLVED' : 'PENDING' }}
      </span>
    </template>
  </el-table-column>
  <el-table-column prop="location" label="位置" min-width="200" show-overflow-tooltip />
</el-table>
```

### 模板底部添加面板

```vue
<!-- 处理链详情面板 -->
<ProcessingChainPanel
  v-model="showChainPanel"
  :data="selectedChainData"
  :chain="processingChain"
/>
```

---

## 🎯 验收清单

- [ ] 异常列表中显示处理进度条
- [ ] 点击异常行打开详情面板
- [ ] 详情面板显示完整的处理链
- [ ] 时间轴正确显示各阶段
- [ ] 各阶段的详情信息正确展示
- [ ] 样式美观，符合现有设计
- [ ] 响应式设计正常
- [ ] 没有控制台错误

---

## 🚀 后续优化

### 实时更新

```typescript
// 使用 WebSocket 实时更新处理链
const ws = new WebSocket('ws://your-api/event-processing')
ws.onmessage = (event) => {
  const data = JSON.parse(event.data)
  if (data.eventId === selectedChainData.value?.id) {
    processingChain.value = data
  }
}
```

### 动画效果

```vue
<!-- 在 ProcessingChainPanel.vue 中添加过渡动画 -->
<transition name="slide-fade">
  <div v-if="visible" class="chain-container">
    <!-- ... -->
  </div>
</transition>
```

### 导出功能

```typescript
const exportChainData = () => {
  const data = JSON.stringify(processingChain.value, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `chain-${selectedChainData.value.id}.json`
  a.click()
}
```

---

## 📞 常见问题

**Q: 如何调试处理链数据?**
A: 在浏览器控制台中查看 `processingChain.value`

**Q: 如何修改处理链的阶段?**
A: 修改 `ProcessingChainPanel.vue` 中的 `v-else-if` 分支

**Q: 如何添加自定义样式?**
A: 修改组件中的 `<style scoped>` 部分

**Q: 如何处理加载失败?**
A: 在 `handleRowClick` 中添加 try-catch 和错误提示

---

## 📝 总结

这个实现方案提供了:

✅ 完整的处理链可视化
✅ 清晰的时间轴展示
✅ 详细的阶段信息
✅ 美观的UI设计
✅ 易于集成和扩展

预计集成时间: **30-60分钟**
