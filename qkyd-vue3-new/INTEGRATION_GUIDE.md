# 处理链展示集成指南

## 1. 导入组件和API

在 `DashboardView.vue` 的 `<script setup>` 中添加:

```typescript
import ProcessingChainPanel from '@/components/ProcessingChainPanel.vue'
import ProcessingChainProgress from '@/components/ProcessingChainProgress.vue'
import { getProcessingChain } from '@/api/processingChain'

// 新增状态
const showChainPanel = ref(false)
const selectedChainData = ref<any>(null)
const processingChain = ref<any>(null)
```

## 2. 修改异常列表表格

在 `EVENT STREAM` 表格中添加处理链进度列:

```vue
<!-- 在 el-table-column 中添加 -->
<el-table-column prop="processingProgress" label="处理进度" min-width="150">
  <template #default="{ row }">
    <ProcessingChainProgress :stages="row.stages" />
  </template>
</el-table-column>
```

## 3. 修改行点击事件

替换现有的 `handleRowClick` 函数:

```typescript
const handleRowClick = async (row: ExceptionRow) => {
  try {
    // 获取处理链详情
    const response = await getProcessingChain(row.id)
    processingChain.value = response.data
    selectedChainData.value = row
    showChainPanel.value = true
  } catch (error) {
    ElMessage.error('获取处理链详情失败')
  }
}
```

## 4. 在模板中添加组件

在 `</template>` 前添加:

```vue
<!-- 处理链详情面板 -->
<ProcessingChainPanel
  v-model="showChainPanel"
  :data="selectedChainData"
  :chain="processingChain"
/>
```

## 5. 后端数据结构

后端需要返回以下数据结构:

```typescript
// GET /api/ai/event-processing/chain/{eventId}
{
  "stages": [
    {
      "name": "异常检测",
      "status": "completed",
      "timestamp": "2026-03-23T09:15:30Z",
      "details": {
        "anomalies": [
          {
            "type": "心率异常",
            "value": "120 bpm",
            "severity": "high"
          },
          {
            "type": "血氧异常",
            "value": "92%",
            "severity": "medium"
          }
        ]
      }
    },
    {
      "name": "事件产生",
      "status": "completed",
      "timestamp": "2026-03-23T09:15:31Z",
      "details": {
        "eventType": "心血管异常"
      }
    },
    {
      "name": "AI解析与上下文补全",
      "status": "completed",
      "timestamp": "2026-03-23T09:15:32Z",
      "details": {
        "patientInfo": "张三, 65岁, 男",
        "medicalHistory": "高血压, 糖尿病",
        "currentMedication": "阿司匹林, 美托洛尔"
      }
    },
    {
      "name": "风险评估",
      "status": "completed",
      "timestamp": "2026-03-23T09:15:33Z",
      "details": {
        "riskLevel": "high",
        "riskScore": 8.5,
        "confidence": 92,
        "trend": "恶化"
      }
    },
    {
      "name": "处置建议",
      "status": "completed",
      "timestamp": "2026-03-23T09:15:34Z",
      "details": {
        "suggestion": "立即就医",
        "priority": "P1",
        "department": "心内科"
      }
    },
    {
      "name": "自动执行",
      "status": "completed",
      "timestamp": "2026-03-23T09:15:35Z",
      "details": {
        "executionStatus": "success",
        "notificationTargets": "患者, 家属, 医生"
      }
    },
    {
      "name": "留痕记录",
      "status": "completed",
      "timestamp": "2026-03-23T09:15:36Z",
      "details": {
        "algorithmVersion": "v2.0",
        "processingTime": 6
      }
    }
  ],
  "totalDuration": 6
}
```

## 6. 异常列表数据结构更新

异常列表中需要添加 `stages` 字段:

```typescript
interface ExceptionRow extends GenericRow {
  // ... 现有字段
  stages?: Array<{
    name: string
    status: 'completed' | 'processing' | 'pending'
  }>
}
```

## 7. 完整集成代码示例

```vue
<template>
  <div class="page-shell light-glass-theme">
    <!-- 现有内容 -->

    <!-- 底部：事件雷达流 -->
    <div class="hud-bottom-center fade-bottom">
      <div class="hud-content-block h-full">
        <div class="holo-title">EVENT STREAM <span>// 实时异常流</span></div>
        <div class="table-wrapper">
          <el-table
            :data="filteredExceptionList"
            height="100%"
            class="holo-table clickable-table"
            @row-click="handleRowClick"
          >
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
        </div>
      </div>
    </div>

    <!-- 处理链详情面板 -->
    <ProcessingChainPanel
      v-model="showChainPanel"
      :data="selectedChainData"
      :chain="processingChain"
    />
  </div>
</template>

<script setup lang="ts">
// ... 现有导入
import ProcessingChainPanel from '@/components/ProcessingChainPanel.vue'
import ProcessingChainProgress from '@/components/ProcessingChainProgress.vue'
import { getProcessingChain } from '@/api/processingChain'

// ... 现有状态
const showChainPanel = ref(false)
const selectedChainData = ref<any>(null)
const processingChain = ref<any>(null)

// 修改行点击事件
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
</script>
```

## 8. 样式调整

如果需要调整样式，可以在 DashboardView.vue 中添加:

```scss
// 处理进度列样式
:deep(.el-table__cell) {
  padding: 8px 0;
}
```

## 9. 测试步骤

1. ✅ 确保后端API返回正确的数据结构
2. ✅ 在异常列表中看到处理进度条
3. ✅ 点击异常行打开详情面板
4. ✅ 在详情面板中看到完整的处理链
5. ✅ 验证时间轴显示正确
6. ✅ 验证各阶段的详情信息正确

## 10. 常见问题

**Q: 处理链数据从哪里来?**
A: 从后端 `/api/ai/event-processing/chain/{eventId}` 接口获取

**Q: 如何实时更新处理链?**
A: 可以使用 WebSocket 或定时轮询更新数据

**Q: 如何自定义样式?**
A: 修改 ProcessingChainPanel.vue 中的 `<style>` 部分

**Q: 如何添加更多阶段?**
A: 在 ProcessingChainPanel.vue 中添加新的 `v-else-if` 分支
