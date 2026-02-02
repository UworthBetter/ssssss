

```markdown
## 🎯 实时监测大屏综合优化任务

请对以下文件进行功能和布局双重优化：

### 📂 目标文件
- d:\jishe\1.19\RuoYi-Vue3-Modern\src\views\ai\algorithm-dashboard\index.vue
- d:\jishe\1.19\RuoYi-Vue3-Modern\src\views\ai\algorithm-dashboard\components\PatientSelector.vue
- d:\jishe\1.19\RuoYi-Vue3-Modern\src\views\ai\algorithm-dashboard\components\PatientCardList.vue
- d:\jishe\1.19\RuoYi-Vue3-Modern\src\views\ai\algorithm-dashboard\components\AlgorithmVisualization.vue
- d:\jishe\1.19\RuoYi-Vue3-Modern\src\views\ai\algorithm-dashboard\hooks\useRealtimeData.js

---

## 🔴 第一部分：功能修复（P0 优先级）

### 1. 修复 Element Plus 图标问题
**文件**: AlgorithmVisualization.vue 第11-15行

修复前：
```vue
<el-button :icon="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'" />
<el-button icon="el-icon-refresh" />
```

修复后：
```vue
<script setup>
import { VideoPlay, VideoPause, Refresh } from '@element-plus/icons-vue'
// ...
const isPlaying = ref(true)
</script>

<template>
  <el-button :icon="isPlaying ? VideoPause : VideoPlay" @click="toggleAnimation" circle />
  <el-button :icon="Refresh" @click="resetAnimation" circle />
</template>
```

### 2. 统一患者列表数据源（消除重复请求）
**文件**: index.vue + PatientSelector.vue

index.vue 修改：
```javascript
// 第129行后添加统一的患者列表
const patientList = ref([])

// 修改第154-181行 loadOnlinePatients 方法
const loadOnlinePatients = async () => {
  try {
    const res = await listSubject({ pageNum: 1, pageSize: 100 })
    if (res.code === 200 && res.rows) {
      patientList.value = res.rows.map(item => ({
        id: item.id,
        name: item.name,
        age: item.age,
        gender: item.gender,
        healthCondition: item.healthCondition || item.medicalHistory,
        avatar: item.avatar,
        riskLevel: item.riskLevel || 'low'
      }))
      
      // onlinePatients 继承 patientList 数据并扩展实时字段
      onlinePatients.value = patientList.value.map(p => ({
        ...p,
        heartRate: null,
        systolic: null,
        diastolic: null,
        spo2: null,
        hasAbnormal: false,
        lastUpdateTime: null
      }))

      if (onlinePatients.value.length > 0 && !selectedPatientId.value) {
        selectedPatientId.value = onlinePatients.value[0].id
        await loadPatientData(onlinePatients.value[0].id)
      }
    }
  } catch (error) {
    console.error('加载患者列表失败:', error)
  }
}
```

PatientSelector.vue 修改：
```javascript
// 第111-120行改为接收 patients
const props = defineProps({
  patients: { type: Array, default: () => [] },
  modelValue: { type: [String, Number], default: null },
  realtimeData: { type: Object, default: null }
})

// 删除第158-179行 loadPatientList 方法
// 使用 computed 直接使用父组件数据
const patientList = computed(() => props.patients)

// 修改模板中的 el-select 数据源
<el-option
  v-for="patient in patientList"
  :key="patient.id"
  :label="patient.name"
  :value="patient.id"
>
```

index.vue 模板修改（第5-12行）：
```vue
<PatientSelector
  v-model="selectedPatientId"
  :patients="patientList"
  :realtime-data="realtimeData"
  @change="handlePatientChange"
  @refresh="handleRefresh"
  @view-detail="handleViewDetail"
  @generate-report="handleGenerateReport"
/>
```

### 3. 实现查看详情跳转功能
**文件**: index.vue 第260-269行 + 添加路由导入

```javascript
import { useRouter } from 'vue-router'
const router = useRouter()

// 修改第260-263行
const handleViewDetail = (patient) => {
  if (!patient?.id) {
    ElMessage.warning('患者信息不完整')
    return
  }
  router.push(`/health/monitor/patient/${patient.id}`)
}

// 修改第266-269行
const handleGenerateReport = async (patient) => {
  if (!patient?.id) {
    ElMessage.warning('患者信息不完整')
    return
  }
  try {
    // 先显示加载状态
    const loading = ElMessage.info({
      message: `正在生成 ${patient.name} 的健康报告...`,
      duration: 0
    })
    
    // TODO: 替换为实际 API 调用
    // const res = await generateHealthReport(patient.id)
    
    setTimeout(() => {
      loading.close()
      ElMessage.success('报告生成成功！')
      // window.open(res.data.reportUrl)
    }, 1500)
  } catch (error) {
    ElMessage.error('报告生成失败，请稍后重试')
  }
}
```

---

## 🟡 第二部分：布局重构（P1 优先级）

### 目标布局结构
```
┌─────────────────────────────────────────────────────────────────────┐
│ [MetricsPanel - 数字统计栏] (4列: 总患者/在线/异常/平均心率)           │
├─────────────────────────────────────────────────────────────────────┤
│ [PatientSelector - 简化版患者选择器]                                  │
├──────────┬──────────────────────────────────────────┬───────────────┤
│          │                                          │               │
│ 患者列表  │         滑动窗口可视化 (主区域)            │   告警列表     │
│ (260px)  │                                          │  (固定300px)  │
│          │      ┌────────────────────────────┐      │               │
│  ┌───┐   │      │      Canvas 动画            │      │  ┌─────────┐  │
│  │患者A│   │      │    (高度自适应)            │      │  │ 异常项1  │  │
│  ├───┤   │      └────────────────────────────┘      │  ├─────────┤  │
│  │患者B│   │      ┌─────────────┬──────────────┐     │  │ 异常项2  │  │
│  └───┘   │      │   雷达图      │  风险预测流程  │     │  └─────────┘  │
│          │      │  (固定180px) │  (固定180px)  │     │               │
│          │      └─────────────┴──────────────┘     │               │
└──────────┴──────────────────────────────────────────┴───────────────┘
```

### index.vue 布局重构

```vue
<template>
  <div class="algorithm-dashboard-page">
    <!-- 1. 顶部统计栏 - 启用 -->
    <MetricsPanel :metrics="metricsDataArray" :use-data-v="false" />
    
    <!-- 2. 患者选择器 -->
    <PatientSelector
      v-model="selectedPatientId"
      :patients="patientList"
      :realtime-data="realtimeData"
      @change="handlePatientChange"
      @refresh="handleRefresh"
      @view-detail="handleViewDetail"
      @generate-report="handleGenerateReport"
    />
    
    <!-- 3. 主体内容区 - 新三栏布局 -->
    <div class="dashboard-main">
      <!-- 左侧患者列表 -->
      <div class="panel patients-panel">
        <div class="panel-header">在线患者</div>
        <PatientCardList
          :patients="onlinePatients"
          :selected-patient-id="selectedPatientId"
          :loading="realtimeLoading"
          @select-patient="handlePatientCardClick"
        />
      </div>
      
      <!-- 中间可视化区 -->
      <div class="panel viz-panel">
        <AlgorithmVisualization
          :heart-rate-data="heartRateData"
          :window-size="windowSize"
          :threshold="threshold"
          :animation-speed="animationSpeed"
          @on-abnormal-detected="handleAbnormalDetected"
          @on-window-updated="handleWindowUpdated"
        />
        <div class="viz-bottom">
          <FeatureRadarChart :radar-data="radarData" class="radar-section" />
          <RiskPredictionFlow
            :flow-steps="flowSteps"
            :current-step="currentStep"
            :risk-score="riskScore"
            class="risk-section"
            @on-step-change="handleStepChange"
            @on-flow-complete="handleFlowComplete"
          />
        </div>
      </div>
      
      <!-- 右侧告警列表 -->
      <div class="panel alerts-panel">
        <div class="panel-header">实时告警</div>
        <AbnormalAlertList
          :alerts="alertsList"
          :max-display="15"
          :auto-scroll="true"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.algorithm-dashboard-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0f1a 0%, #1a1f2e 100%);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
  
  // 确保 metrics panel 显示
  :deep(.metrics-panel) {
    flex-shrink: 0;
  }
}

// 主体三栏布局
.dashboard-main {
  display: grid;
  grid-template-columns: 260px 1fr 300px;
  gap: 16px;
  flex: 1;
  min-height: 0; // 关键：允许子元素收缩
  
  .panel {
    background: rgba(0, 20, 40, 0.6);
    border: 1px solid rgba(0, 150, 255, 0.2);
    border-radius: 12px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    
    .panel-header {
      padding: 12px 16px;
      background: rgba(0, 50, 80, 0.5);
      border-bottom: 1px solid rgba(0, 150, 255, 0.2);
      font-size: 14px;
      font-weight: 600;
      color: #00d4ff;
      flex-shrink: 0;
    }
  }
  
  // 患者列表面板
  .patients-panel {
    min-width: 260px;
    max-width: 260px;
  }
  
  // 可视化面板
  .viz-panel {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 16px;
    overflow: hidden;
    
    :deep(.algorithm-visualization) {
      flex: 1;
      min-height: 350px;
    }
    
    .viz-bottom {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      height: 200px;
      flex-shrink: 0;
      
      .radar-section,
      .risk-section {
        height: 100%;
      }
    }
  }
  
  // 告警列表面板
  .alerts-panel {
    min-width: 300px;
    max-width: 300px;
    
    :deep(.abnormal-alert-list) {
      flex: 1;
      overflow-y: auto;
    }
  }
}

// 响应式适配
@media (max-width: 1400px) {
  .dashboard-main {
    grid-template-columns: 240px 1fr 280px;
    
    .viz-panel .viz-bottom {
      grid-template-columns: 1fr;
      height: auto;
      
      .radar-section,
      .risk-section {
        height: 160px;
      }
    }
  }
}

@media (max-width: 1200px) {
  .dashboard-main {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr auto;
    
    .patients-panel,
    .alerts-panel {
      max-width: none;
      min-width: auto;
      max-height: 200px;
    }
    
    .viz-panel {
      min-height: 500px;
    }
  }
}
</style>
```

---

## 🟢 第三部分：体验优化（P2 优先级）

### 4. 添加 Loading 状态
**文件**: PatientCardList.vue

```vue
<template>
  <div class="patient-card-list">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton v-for="i in 5" :key="i" animated>
        <template #template>
          <div style="padding: 12px;">
            <el-skeleton-item variant="p" style="width: 60%; margin-bottom: 8px;" />
            <el-skeleton-item variant="text" style="width: 40%;" />
          </div>
        </template>
      </el-skeleton>
    </div>
    
    <!-- 患者卡片列表 -->
    <div v-else class="card-container">
      <!-- 原有代码... -->
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  patients: { type: Array, default: () => [] },
  selectedPatientId: { type: [String, Number], default: null },
  loading: { type: Boolean, default: false } // 新增
})
</script>

<style scoped>
.loading-state {
  padding: 12px;
}
</style>
```

### 5. 优化异常闪烁效果
**文件**: PatientCardList.vue 第261-273行

```scss
// 修改动画，降低频率
&.abnormal {
  animation: pulse 3s ease-in-out infinite; // 改为3秒
  border-color: #ff4d4f;
  border-width: 2px;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.4);
    border-color: #ff4d4f;
  }
  50% {
    box-shadow: 0 0 0 6px rgba(255, 77, 79, 0); // 减小范围
    border-color: #ff7875;
  }
}
```

### 6. 增强空数据状态
**文件**: PatientCardList.vue 第75-77行

```vue
<div v-if="sortedPatients.length === 0 && !loading" class="empty-state">
  <el-empty description="暂无在线患者" :image-size="80">
    <template #description>
      <div style="text-align: center;">
        <p style="color: #a0e0ff; margin-bottom: 8px;">暂无在线患者</p>
        <p style="color: #70a0c0; font-size: 12px;">请检查患者是否已绑定监测设备</p>
      </div>
    </template>
    <el-button type="primary" size="small" @click="emit('refresh')">
      刷新数据
    </el-button>
  </el-empty>
</div>
```

---

## ✅ 验收清单

1. **功能修复**:
   - [ ] AlgorithmVisualization.vue 图标正常显示，无控制台警告
   - [ ] 切换患者时只发起一次 listSubject 请求
   - [ ] 点击"查看详情"正确跳转到 `/health/monitor/patient/:id`
   - [ ] 点击"生成报告"有加载状态和成功提示

2. **布局优化**:
   - [ ] MetricsPanel 统计栏显示在顶部
   - [ ] 三栏布局：患者列表(260px) | 可视化(自适应) | 告警(300px)
   - [ ] 雷达图和风险预测并排在可视化下方
   - [ ] 告警列表可显示 10+ 条记录

3. **响应式**:
   - [ ] 1400px-1200px: 三栏保持，下方图表堆叠
   - [ ] <1200px: 单栏垂直排列，患者/告警限制高度

4. **体验优化**:
   - [ ] PatientCardList 显示骨架屏 loading 状态
   - [ ] 异常卡片闪烁频率降低（3秒一次）
   - [ ] 空数据状态显示刷新按钮

---

## ⚠️ 注意事项

1. 保持原有深色科技风 UI 配色（#00d4ff 主色，#00ff88 成功色，#ff4d4f 危险色）
2. 不要删除原有组件的 props 和事件，只添加新功能
3. 确保 useAlgorithmData Hook 仍然正常工作（用于模拟数据）
4. 响应式断点平滑过渡，避免布局跳跃
```

---

