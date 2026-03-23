<template>
  <PlatformPageShellV2
    title="健康档案"
    subtitle="汇聚服务对象的体征历史数据，支持趋势分析与阶段性健康回顾，辅助医护决策。"
    eyebrow="HEALTH RECORDS"
    status-note="演示版 · 当前使用模拟数据"
    status-tone="warning"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <el-input v-model="searchName" placeholder="搜索对象姓名 / 账号..." prefix-icon="Search" clearable style="width: 240px" />
        <el-select v-model="filterMetric" placeholder="体征指标" clearable style="width: 160px">
          <el-option label="全部指标" value="" />
          <el-option label="心率" value="heartRate" />
          <el-option label="血压" value="bloodPressure" />
          <el-option label="血氧" value="spo2" />
          <el-option label="体温" value="temperature" />
          <el-option label="步数" value="steps" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </template>

    <div class="records-layout">
      <!-- 左侧：对象列表 -->
      <div class="subject-panel panel-card">
        <div class="panel-header">
          <span class="panel-title">服务对象</span>
          <el-tag size="small" type="info">{{ mockSubjects.length }} 人</el-tag>
        </div>
        <div class="subject-list">
          <div
            v-for="s in mockSubjects"
            :key="s.id"
            class="subject-item"
            :class="{ active: selectedSubjectId === s.id }"
            @click="selectedSubjectId = s.id"
          >
            <el-avatar :size="36" :class="'avatar-' + s.risk">{{ s.name.charAt(0) }}</el-avatar>
            <div class="subject-item-info">
              <span class="sname">{{ s.name }}</span>
              <span class="sage">{{ s.age }}岁 · {{ s.sex }}</span>
            </div>
            <div class="risk-dot" :class="'dot-' + s.risk"></div>
          </div>
        </div>
      </div>

      <!-- 右侧：档案详情 -->
      <div class="detail-area" v-if="selectedSubject">
        <!-- 概览卡片 -->
        <div class="overview-cards">
          <div v-for="card in overviewCards" :key="card.label" class="ov-card">
            <div class="ov-icon" :style="{ background: card.bg }">
              <el-icon :size="20"><component :is="card.icon" /></el-icon>
            </div>
            <div class="ov-data">
              <span class="ov-label">{{ card.label }}</span>
              <strong class="ov-value">{{ card.value }}</strong>
              <span class="ov-unit">{{ card.unit }}</span>
            </div>
            <div class="ov-trend" :class="card.trend">
              <el-icon v-if="card.trend === 'up'"><ArrowUp /></el-icon>
              <el-icon v-else-if="card.trend === 'down'"><ArrowDown /></el-icon>
              <span v-else>—</span>
            </div>
          </div>
        </div>

        <!-- 趋势图占位 -->
        <div class="panel-card chart-panel">
          <div class="panel-header">
            <span class="panel-title">心率趋势（近 7 天）</span>
            <div class="chart-legend">
              <span class="legend-dot" style="background:#3b82f6"></span>心率
              <span class="legend-dot" style="background:#10b981; margin-left:12px"></span>血氧
            </div>
          </div>
          <div class="chart-placeholder">
            <div class="chart-bars">
              <div v-for="(bar, i) in chartBars" :key="i" class="bar-group">
                <div class="bar-col">
                  <div class="bar hr-bar" :style="{ height: bar.hr + '%' }"></div>
                  <div class="bar spo2-bar" :style="{ height: bar.spo2 + '%' }"></div>
                </div>
                <span class="bar-label">{{ bar.day }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 近期记录表 -->
        <div class="panel-card">
          <div class="panel-header">
            <span class="panel-title">近期体征记录</span>
          </div>
          <el-table :data="recentRecords" size="small" style="width: 100%">
            <el-table-column prop="time" label="采集时间" min-width="150" />
            <el-table-column label="心率(bpm)" min-width="100">
              <template #default="{ row }">
                <span :class="row.hrStatus">{{ row.heartRate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="spo2" label="血氧(%)" min-width="90" />
            <el-table-column prop="temp" label="体温(℃)" min-width="90" />
            <el-table-column prop="steps" label="步数" min-width="80" />
            <el-table-column label="状态" min-width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'normal' ? 'success' : 'warning'" size="small" effect="light">
                  {{ row.status === 'normal' ? '正常' : '偏高' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 未选中时的空态 -->
      <div class="empty-area" v-else>
        <el-icon class="empty-icon" :size="64"><UserFilled /></el-icon>
        <p>从左侧选择一位服务对象，查看其健康档案</p>
      </div>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ArrowUp, ArrowDown, UserFilled, Odometer, Aim, Sunny, Bicycle } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'

const searchName = ref('')
const filterMetric = ref('')
const dateRange = ref<[Date, Date] | null>(null)
const selectedSubjectId = ref<number>(1)

const mockSubjects = [
  { id: 1, name: '李秀英', age: 78, sex: '女', risk: 'high' },
  { id: 2, name: '王建国', age: 82, sex: '男', risk: 'medium' },
  { id: 3, name: '张淑芬', age: 69, sex: '女', risk: 'low' },
  { id: 4, name: '赵志强', age: 75, sex: '男', risk: 'medium' },
  { id: 5, name: '陈美华', age: 71, sex: '女', risk: 'low' },
]

const selectedSubject = computed(() => mockSubjects.find(s => s.id === selectedSubjectId.value))

const overviewCards = computed(() => [
  { label: '平均心率', value: '76', unit: 'bpm', icon: 'Odometer', bg: 'linear-gradient(135deg,#eff6ff,#dbeafe)', trend: 'stable' },
  { label: '平均血氧', value: '97.2', unit: '%', icon: 'Aim', bg: 'linear-gradient(135deg,#f0fdf4,#dcfce7)', trend: 'up' },
  { label: '平均体温', value: '36.5', unit: '℃', icon: 'Sunny', bg: 'linear-gradient(135deg,#fff7ed,#fed7aa)', trend: 'stable' },
  { label: '今日步数', value: '3,240', unit: '步', icon: 'Bicycle', bg: 'linear-gradient(135deg,#fdf4ff,#f3e8ff)', trend: 'down' },
])

const chartBars = [
  { day: '周一', hr: 62, spo2: 90 },
  { day: '周二', hr: 75, spo2: 88 },
  { day: '周三', hr: 58, spo2: 92 },
  { day: '周四', hr: 80, spo2: 85 },
  { day: '周五', hr: 70, spo2: 94 },
  { day: '周六', hr: 65, spo2: 89 },
  { day: '周日', hr: 72, spo2: 91 },
]

const recentRecords = [
  { time: '2026-03-23 08:12', heartRate: 74, hrStatus: '', spo2: 97, temp: 36.5, steps: 420, status: 'normal' },
  { time: '2026-03-23 10:35', heartRate: 92, hrStatus: 'val-warn', spo2: 96, temp: 36.6, steps: 1280, status: 'warn' },
  { time: '2026-03-23 13:00', heartRate: 78, hrStatus: '', spo2: 98, temp: 36.4, steps: 2100, status: 'normal' },
  { time: '2026-03-23 15:20', heartRate: 81, hrStatus: '', spo2: 97, temp: 36.5, steps: 2890, status: 'normal' },
  { time: '2026-03-23 17:45', heartRate: 76, hrStatus: '', spo2: 97, temp: 36.5, steps: 3240, status: 'normal' },
]

const handleSearch = () => {}
const handleReset = () => { searchName.value = ''; filterMetric.value = ''; dateRange.value = null }
</script>

<style scoped lang="scss">
.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.records-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  min-height: 500px;
}

.panel-card {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.7);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

/* 左侧对象列表 */
.subject-panel {
  flex: 0 0 220px;
  max-height: 640px;
  display: flex;
  flex-direction: column;
}

.subject-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.subject-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover { background: #f8fafc; }
  &.active { background: #eff6ff; }

  .el-avatar {
    font-size: 14px; font-weight: 700; color: #fff; flex-shrink: 0;
    &.avatar-high { background: linear-gradient(135deg, #ef4444, #f87171); }
    &.avatar-medium { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
    &.avatar-low { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
  }

  .subject-item-info {
    flex: 1;
    display: flex; flex-direction: column;
    .sname { font-size: 14px; font-weight: 600; color: #0f172a; }
    .sage { font-size: 12px; color: #64748b; }
  }

  .risk-dot {
    width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
    &.dot-high { background: #ef4444; }
    &.dot-medium { background: #f59e0b; }
    &.dot-low { background: #10b981; }
  }
}

/* 右侧详情 */
.detail-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.ov-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid rgba(226, 232, 240, 0.7);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.ov-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #3b82f6; flex-shrink: 0;
}

.ov-data {
  flex: 1;
  display: flex; flex-direction: column;
  .ov-label { font-size: 12px; color: #64748b; }
  .ov-value { font-size: 22px; font-weight: 800; color: #0f172a; line-height: 1.2; }
  .ov-unit { font-size: 11px; color: #94a3b8; }
}

.ov-trend {
  font-size: 14px;
  &.up { color: #10b981; }
  &.down { color: #ef4444; }
  &.stable { color: #94a3b8; }
}

/* 图表 */
.chart-panel { .panel-header { padding-bottom: 16px; } }
.chart-legend { display: flex; align-items: center; font-size: 12px; color: #64748b; }
.legend-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-right: 4px; }

.chart-placeholder {
  padding: 20px 20px 8px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 140px;
}

.bar-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.bar-col {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  gap: 3px;
  justify-content: center;
}

.bar {
  width: 12px;
  border-radius: 4px 4px 0 0;
  transition: height 0.4s ease;
  min-height: 4px;
  &.hr-bar { background: linear-gradient(to top, #3b82f6, #93c5fd); }
  &.spo2-bar { background: linear-gradient(to top, #10b981, #6ee7b7); }
}

.bar-label { font-size: 11px; color: #94a3b8; }

/* 值警告色 */
:deep(.val-warn) { color: #f59e0b; font-weight: 600; }

/* 空态 */
.empty-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #94a3b8;
  .empty-icon { opacity: 0.3; }
  p { font-size: 14px; }
}

@media (max-width: 1200px) {
  .overview-cards { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 960px) {
  .records-layout { flex-direction: column; }
  .subject-panel { flex: none; width: 100%; max-height: 200px; }
}
</style>
