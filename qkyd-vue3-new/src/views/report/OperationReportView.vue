<template>
  <PlatformPageShellV2
    title="运营报表"
    subtitle="汇总平台日、周、月度核心运营指标，直观呈现设备在线率、告警响应效率与对象服务情况。"
    eyebrow="OPERATION REPORT"
    status-note="演示版 · 当前使用模拟数据"
    status-tone="warning"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <div class="period-tabs">
          <button
            v-for="p in periods"
            :key="p.value"
            class="period-btn"
            :class="{ active: activePeriod === p.value }"
            @click="activePeriod = p.value"
          >{{ p.label }}</button>
        </div>
        <el-date-picker v-if="activePeriod === 'custom'" v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" />
        <el-button :icon="Download" plain>导出报表</el-button>
      </div>
    </template>

    <!-- KPI 卡片区 -->
    <div class="kpi-grid">
      <div v-for="kpi in kpiCards" :key="kpi.label" class="kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">{{ kpi.label }}</span>
          <div class="kpi-trend" :class="kpi.trend">
            <el-icon v-if="kpi.trend === 'up'"><ArrowUp /></el-icon>
            <el-icon v-else-if="kpi.trend === 'down'"><ArrowDown /></el-icon>
            <span>{{ kpi.change }}</span>
          </div>
        </div>
        <div class="kpi-value">{{ kpi.value }}</div>
        <div class="kpi-compare">对比上{{ periodUnit }}：{{ kpi.prevValue }}</div>
        <div class="kpi-sparkline">
          <div v-for="(h, i) in kpi.sparkline" :key="i" class="spark-bar" :style="{ height: (h / Math.max(...kpi.sparkline) * 36) + 'px', background: kpi.color }"></div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section">
      <!-- 告警分布 -->
      <div class="panel-card chart-col">
        <div class="panel-header">
          <span class="panel-title">告警类型分布</span>
          <span class="panel-sub">本{{ periodUnit }}</span>
        </div>
        <div class="donut-chart">
          <svg viewBox="0 0 120 120" width="120" height="120">
            <circle cx="60" cy="60" r="50" fill="none" stroke="#f1f5f9" stroke-width="16" />
            <circle cx="60" cy="60" r="50" fill="none" stroke="#ef4444" stroke-width="16"
              :stroke-dasharray="`${alertShare} 314`" stroke-dashoffset="-8" stroke-linecap="round" transform="rotate(-90 60 60)" />
            <circle cx="60" cy="60" r="50" fill="none" stroke="#f59e0b" stroke-width="16"
              :stroke-dasharray="`${warnShare} 314`" :stroke-dashoffset="-(8 + alertShare)" stroke-linecap="round" transform="rotate(-90 60 60)" />
            <text x="60" y="56" text-anchor="middle" font-size="18" font-weight="800" fill="#0f172a">{{ totalAlerts }}</text>
            <text x="60" y="70" text-anchor="middle" font-size="9" fill="#94a3b8">总告警</text>
          </svg>
          <div class="donut-legend">
            <div v-for="item in alertTypes" :key="item.label" class="dl-item">
              <span class="dl-dot" :style="{ background: item.color }"></span>
              <span class="dl-label">{{ item.label }}</span>
              <span class="dl-val">{{ item.count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 每日趋势 -->
      <div class="panel-card chart-col-wide">
        <div class="panel-header">
          <span class="panel-title">每日数据采集量 & 告警数</span>
          <div class="legend-row">
            <span class="ld"><span class="ld-dot" style="background:#3b82f6"></span>数据采集(k)</span>
            <span class="ld"><span class="ld-dot" style="background:#ef4444"></span>告警次数</span>
          </div>
        </div>
        <div class="trend-chart">
          <div v-for="(d, i) in trendData" :key="i" class="trend-col">
            <div class="tc-bars">
              <div class="tc-bar data-bar" :style="{ height: (d.data / maxData * 120) + 'px' }" :title="d.data + 'k'"></div>
              <div class="tc-bar alert-bar" :style="{ height: (d.alerts / maxAlerts * 120) + 'px' }" :title="d.alerts + ' 次'"></div>
            </div>
            <span class="tc-day">{{ d.day }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 响应时效表 -->
    <div class="panel-card">
      <div class="panel-header">
        <span class="panel-title">告警响应时效分析</span>
      </div>
      <el-table :data="responseTable" style="width: 100%">
        <el-table-column prop="type" label="告警类型" min-width="140" />
        <el-table-column prop="count" label="触发次数" min-width="90" />
        <el-table-column label="平均响应时长" min-width="150">
          <template #default="{ row }">
            <div class="response-bar-row">
              <div class="rb-bg">
                <div class="rb-fill" :style="{ width: (row.avgMin / maxAvgMin * 100) + '%', background: row.avgMin < 5 ? '#10b981' : row.avgMin < 15 ? '#f59e0b' : '#ef4444' }"></div>
              </div>
              <span>{{ row.avgMin }} 分钟</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="已处置率" min-width="100">
          <template #default="{ row }">
            <span :class="row.resolveRate >= 90 ? 'rate-good' : 'rate-warn'">{{ row.resolveRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="评级" width="90">
          <template #default="{ row }">
            <el-tag :type="row.avgMin < 5 ? 'success' : row.avgMin < 15 ? '' : 'danger'" size="small" effect="light">
              {{ row.avgMin < 5 ? '优秀' : row.avgMin < 15 ? '良好' : '待改进' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Download, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'

const activePeriod = ref<'day' | 'week' | 'month' | 'custom'>('week')
const dateRange = ref(null)
const periodUnit = computed(() => ({ day: '日', week: '周', month: '月', custom: '期' }[activePeriod.value]))
const periods = [
  { label: '今日',  value: 'day' },
  { label: '本周',  value: 'week' },
  { label: '本月',  value: 'month' },
  { label: '自定义', value: 'custom' },
]

const kpiCards = [
  { label: '设备在线率', value: '87.5%', prevValue: '83.2%', change: '+4.3%', trend: 'up', color: '#3b82f6', sparkline: [78,80,82,85,83,87,88] },
  { label: '告警总数', value: '48', prevValue: '61', change: '-21.3%', trend: 'down', color: '#ef4444', sparkline: [9,12,8,7,6,5,3] },
  { label: '服务对象数', value: '156', prevValue: '148', change: '+5.4%', trend: 'up', color: '#10b981', sparkline: [140,143,148,150,152,153,156] },
  { label: '平均响应时长', value: '8.2 min', prevValue: '11.4 min', change: '-28%', trend: 'down', color: '#8b5cf6', sparkline: [15,12,11,10,9,9,8] },
]

const alertTypes = [
  { label: '心率异常', count: 22, color: '#ef4444' },
  { label: '设备离线', count: 15, color: '#f59e0b' },
  { label: '血氧偏低', count: 7,  color: '#8b5cf6' },
  { label: '其他',     count: 4,  color: '#94a3b8' },
]
const totalAlerts = alertTypes.reduce((s, a) => s + a.count, 0)
const alertShare = computed(() => (alertTypes[0].count / totalAlerts) * 314)
const warnShare  = computed(() => (alertTypes[1].count / totalAlerts) * 314)

const trendData = [
  { day: '周一', data: 42, alerts: 8 },
  { day: '周二', data: 55, alerts: 12 },
  { day: '周三', data: 48, alerts: 6 },
  { day: '周四', data: 60, alerts: 9 },
  { day: '周五', data: 52, alerts: 7 },
  { day: '周六', data: 35, alerts: 5 },
  { day: '周日', data: 30, alerts: 3 },
]
const maxData   = Math.max(...trendData.map(d => d.data))
const maxAlerts = Math.max(...trendData.map(d => d.alerts))

const responseTable = [
  { type: '心率异常', count: 22, avgMin: 4.3, resolveRate: 96 },
  { type: '设备离线', count: 15, avgMin: 12.5, resolveRate: 87 },
  { type: '血氧偏低', count: 7,  avgMin: 3.8, resolveRate: 100 },
  { type: '跌倒告警', count: 2,  avgMin: 2.1, resolveRate: 100 },
  { type: '其他告警', count: 4,  avgMin: 18.2, resolveRate: 75 },
]
const maxAvgMin = Math.max(...responseTable.map(r => r.avgMin))
</script>

<style scoped lang="scss">
.toolbar-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.period-tabs { display: flex; gap: 4px; }
.period-btn {
  padding: 7px 14px; border-radius: 8px; border: 1px solid #e2e8f0;
  background: #fff; cursor: pointer; font-size: 13px; font-weight: 500; color: #64748b;
  transition: all 0.2s;
  &:hover { background: #f8fafc; }
  &.active { background: #eff6ff; border-color: #93c5fd; color: #2563eb; font-weight: 700; }
}

.kpi-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
}
.kpi-card {
  background: #fff; border-radius: 14px; padding: 18px 20px;
  border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  display: flex; flex-direction: column; gap: 4px;
}
.kpi-top { display: flex; justify-content: space-between; align-items: flex-start; }
.kpi-label { font-size: 13px; color: #64748b; }
.kpi-trend {
  display: flex; align-items: center; gap: 3px; font-size: 12px; font-weight: 600; border-radius: 6px; padding: 2px 7px;
  &.up { color: #10b981; background: #f0fdf4; }
  &.down { color: #3b82f6; background: #eff6ff; }
}
.kpi-value { font-size: 28px; font-weight: 800; color: #0f172a; line-height: 1.2; }
.kpi-compare { font-size: 12px; color: #94a3b8; }
.kpi-sparkline { display: flex; align-items: flex-end; gap: 3px; height: 40px; margin-top: 8px; }
.spark-bar { flex: 1; border-radius: 2px; opacity: 0.6; min-height: 3px; transition: height 0.4s; }

.chart-section { display: flex; gap: 16px; margin-bottom: 24px; }
.panel-card {
  background: #fff; border-radius: 16px;
  border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 12px rgba(0,0,0,0.04); overflow: hidden;
  margin-bottom: 20px;
}
.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #f1f5f9;
}
.panel-title { font-size: 14px; font-weight: 700; color: #0f172a; }
.panel-sub { font-size: 12px; color: #94a3b8; }

.chart-col { flex: 0 0 280px; }
.chart-col-wide { flex: 1; }

.donut-chart { display: flex; align-items: center; gap: 24px; padding: 20px; }
.donut-legend { display: flex; flex-direction: column; gap: 10px; }
.dl-item { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.dl-dot { width: 10px; height: 10px; border-radius: 3px; flex-shrink: 0; }
.dl-label { flex: 1; color: #475569; }
.dl-val { font-weight: 700; color: #0f172a; }

.legend-row { display: flex; gap: 16px; }
.ld { display: flex; align-items: center; gap: 5px; font-size: 12px; color: #64748b; }
.ld-dot { width: 8px; height: 8px; border-radius: 2px; }

.trend-chart { display: flex; align-items: flex-end; gap: 12px; padding: 16px 20px 8px; height: 160px; }
.trend-col { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; }
.tc-bars { display: flex; align-items: flex-end; gap: 3px; }
.tc-bar { width: 14px; border-radius: 3px 3px 0 0; transition: height 0.4s; min-height: 4px; }
.data-bar { background: linear-gradient(to top, #3b82f6, #93c5fd); }
.alert-bar { background: linear-gradient(to top, #ef4444, #fca5a5); }
.tc-day { font-size: 11px; color: #94a3b8; }

.response-bar-row { display: flex; align-items: center; gap: 10px; }
.rb-bg { flex: 1; height: 6px; background: #f1f5f9; border-radius: 3px; }
.rb-fill { height: 6px; border-radius: 3px; transition: width 0.5s; }
.rate-good { color: #10b981; font-weight: 700; }
.rate-warn { color: #f59e0b; font-weight: 700; }

@media (max-width: 1200px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-section { flex-direction: column; }
  .chart-col { flex: none; }
}
</style>
