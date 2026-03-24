<template>
  <PlatformPageShellV2
    title="运营报表"
    subtitle="按今日与近 7 日真实设备、对象和异常数据生成运营概览。"
    eyebrow="OPERATION REPORT"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <div class="period-tabs">
          <button
            v-for="period in periods"
            :key="period.value"
            class="period-btn"
            :class="{ active: activePeriod === period.value }"
            @click="activePeriod = period.value"
          >
            {{ period.label }}
          </button>
        </div>
        <el-date-picker
          v-if="activePeriod === 'custom'"
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
        />
        <el-button type="primary" @click="fetchData">刷新报表</el-button>
      </div>
    </template>

    <div class="kpi-grid">
      <div v-for="kpi in kpiCards" :key="kpi.label" class="kpi-card">
        <div class="kpi-top">
          <span class="kpi-label">{{ kpi.label }}</span>
          <div class="kpi-trend" :class="kpi.trend">{{ kpi.change }}</div>
        </div>
        <div class="kpi-value">{{ kpi.value }}</div>
        <div class="kpi-compare">{{ kpi.description }}</div>
      </div>
    </div>

    <div class="chart-section">
      <div class="panel-card chart-col">
        <div class="panel-header">
          <span class="panel-title">告警类型分布</span>
          <span class="panel-sub">{{ periodLabel }}</span>
        </div>
        <div class="donut-chart">
          <div v-for="item in alertTypes" :key="item.label" class="dl-item">
            <span class="dl-dot" :style="{ background: item.color }"></span>
            <span class="dl-label">{{ item.label }}</span>
            <span class="dl-val">{{ item.count }}</span>
          </div>
        </div>
      </div>

      <div class="panel-card chart-col-wide">
        <div class="panel-header">
          <span class="panel-title">近 7 日异常趋势</span>
          <div class="legend-row">
            <span class="ld"><span class="ld-dot" style="background:#ef4444"></span>异常次数</span>
          </div>
        </div>
        <div class="trend-chart">
          <div v-for="item in trendData" :key="item.label" class="trend-col">
            <div class="tc-bars">
              <div class="tc-bar alert-bar" :style="{ height: `${item.height}px` }" :title="`${item.value} 次`"></div>
            </div>
            <span class="tc-day">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

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
                <div class="rb-fill" :style="{ width: `${row.width}%`, background: row.color }"></div>
              </div>
              <span>{{ row.avgMin }} 分钟</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="resolveRate" label="已处置率" min-width="100">
          <template #default="{ row }">
            <span :class="row.resolveRate >= 90 ? 'rate-good' : 'rate-warn'">{{ row.resolveRate }}%</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { PlatformPageShellV2 } from '@/components/platform'
import { getAbnormalTrend } from '@/api/ai'
import { getRealTimeData } from '@/api/index'
import { listDeviceExtensions, listDevices, listExceptions, listSubjects, type DeviceInfo, type DeviceInfoExtend, type ExceptionAlert, type HealthSubject } from '@/api/health'

const activePeriod = ref<'day' | 'week' | 'custom'>('week')
const dateRange = ref<[Date, Date] | null>(null)
const subjects = ref<HealthSubject[]>([])
const devices = ref<DeviceInfo[]>([])
const extensions = ref<DeviceInfoExtend[]>([])
const exceptions = ref<ExceptionAlert[]>([])
const realtime = ref<Record<string, unknown>>({})
const trendSource = ref<Array<{ label: string; value: number }>>([])

const periods = [
  { label: '今日', value: 'day' as const },
  { label: '近 7 日', value: 'week' as const },
  { label: '自定义', value: 'custom' as const }
]

const periodLabel = computed(() => ({ day: '今日', week: '近 7 日', custom: '自定义' }[activePeriod.value]))

const parseTimestamp = (value?: string) => {
  if (!value) return 0
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

const parseNumber = (value: unknown, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const inCurrentRange = (value?: string) => {
  const timestamp = parseTimestamp(value)
  if (!timestamp) return false
  const now = new Date()
  let start = new Date(now)
  let end = new Date(now)

  if (activePeriod.value === 'day') {
    start.setHours(0, 0, 0, 0)
    end.setHours(23, 59, 59, 999)
  } else if (activePeriod.value === 'week') {
    start.setHours(0, 0, 0, 0)
    start.setDate(start.getDate() - 6)
    end.setHours(23, 59, 59, 999)
  } else if (dateRange.value?.length === 2) {
    start = new Date(dateRange.value[0])
    end = new Date(dateRange.value[1])
    start.setHours(0, 0, 0, 0)
    end.setHours(23, 59, 59, 999)
  }

  return timestamp >= start.getTime() && timestamp <= end.getTime()
}

const scopedExceptions = computed(() => exceptions.value.filter((item) => inCurrentRange(item.createTime)))

const onlineCount = computed(() => {
  const onlineIds = new Set<number>()
  extensions.value.forEach((item) => {
    const timestamp = parseTimestamp(item.lastCommunicationTime)
    if (item.deviceId != null && timestamp && Date.now() - timestamp <= 60 * 60 * 1000) {
      onlineIds.add(Number(item.deviceId))
    }
  })
  return onlineIds.size
})

const onlineRate = computed(() => {
  if (!devices.value.length) return 0
  return Math.round((onlineCount.value / devices.value.length) * 100)
})

const avgResponseMinutes = computed(() => {
  const pending = scopedExceptions.value.filter((item) => String(item.state || '0') !== '1')
  if (!pending.length) return 0
  const total = pending.reduce((sum, item) => sum + Math.max(0, (Date.now() - parseTimestamp(item.createTime)) / 60000), 0)
  return Number((total / pending.length).toFixed(1))
})

const kpiCards = computed(() => [
  { label: '设备在线率', value: `${onlineRate.value}%`, change: onlineRate.value >= 80 ? '稳定' : '偏低', trend: onlineRate.value >= 80 ? 'up' : 'down', description: `在线设备 ${onlineCount.value}/${devices.value.length}` },
  { label: '告警总数', value: scopedExceptions.value.length, change: scopedExceptions.value.length ? '实时更新' : '无新增', trend: scopedExceptions.value.length ? 'up' : 'down', description: '来自真实异常事件表' },
  { label: '服务对象数', value: subjects.value.length, change: '当前在管', trend: 'up', description: '来自健康对象主数据' },
  { label: '平均响应时长', value: `${avgResponseMinutes.value} min`, change: avgResponseMinutes.value <= 15 ? '可控' : '需关注', trend: avgResponseMinutes.value <= 15 ? 'up' : 'down', description: '按未闭环事件估算' }
])

const alertTypes = computed(() => {
  const palette = ['#ef4444', '#f59e0b', '#8b5cf6', '#94a3b8']
  const groups = new Map<string, number>()
  scopedExceptions.value.forEach((item) => {
    const type = item.type || '其他'
    groups.set(type, (groups.get(type) || 0) + 1)
  })
  return Array.from(groups.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 4)
    .map(([label, count], index) => ({ label, count, color: palette[index % palette.length] }))
})

const trendData = computed(() => {
  const maxValue = Math.max(...trendSource.value.map((item) => item.value), 1)
  return trendSource.value.map((item) => ({
    ...item,
    height: Math.max(12, Math.round((item.value / maxValue) * 120))
  }))
})

const responseTable = computed(() => {
  const groups = new Map<string, ExceptionAlert[]>()
  scopedExceptions.value.forEach((item) => {
    const type = item.type || '其他'
    if (!groups.has(type)) groups.set(type, [])
    groups.get(type)?.push(item)
  })

  return Array.from(groups.entries()).map(([type, items]) => {
    const pending = items.filter((item) => String(item.state || '0') !== '1')
    const avgMin = pending.length
      ? Number((pending.reduce((sum, item) => sum + Math.max(0, (Date.now() - parseTimestamp(item.createTime)) / 60000), 0) / pending.length).toFixed(1))
      : 0
    const resolveRate = items.length
      ? Math.round((items.filter((item) => String(item.state || '0') === '1').length / items.length) * 100)
      : 0
    return {
      type,
      count: items.length,
      avgMin,
      resolveRate,
      width: Math.min(100, avgMin * 4),
      color: avgMin <= 5 ? '#10b981' : avgMin <= 15 ? '#f59e0b' : '#ef4444'
    }
  })
})

const fetchData = async () => {
  const hours = activePeriod.value === 'day' ? 24 : 24 * 7
  const [subjectRes, deviceRes, extensionRes, exceptionRes, realtimeRes, trendRes] = await Promise.all([
    listSubjects({ pageNum: 1, pageSize: 200 }),
    listDevices({ pageNum: 1, pageSize: 200 }),
    listDeviceExtensions({ pageNum: 1, pageSize: 200 }),
    listExceptions({ pageNum: 1, pageSize: 200 }),
    getRealTimeData(),
    getAbnormalTrend(undefined, hours)
  ])

  subjects.value = (subjectRes.rows || []) as HealthSubject[]
  devices.value = (deviceRes.rows || []) as DeviceInfo[]
  extensions.value = (extensionRes.rows || []) as DeviceInfoExtend[]
  exceptions.value = (exceptionRes.rows || []) as ExceptionAlert[]
  realtime.value = (realtimeRes.data || {}) as Record<string, unknown>
  trendSource.value = ((trendRes.data || []) as Array<{ label: string; value: number | string }>).map((item) => ({
    label: item.label,
    value: parseNumber(item.value)
  })).slice(-7)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.toolbar-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.period-tabs { display: flex; gap: 4px; }
.period-btn { padding: 7px 14px; border-radius: 8px; border: 1px solid #e2e8f0; background: #fff; cursor: pointer; font-size: 13px; font-weight: 500; color: #64748b; }
.period-btn.active { background: #eff6ff; border-color: #93c5fd; color: #2563eb; font-weight: 700; }
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.kpi-card { background: #fff; border-radius: 14px; padding: 18px 20px; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 8px rgba(0,0,0,0.03); display: flex; flex-direction: column; gap: 4px; }
.kpi-top { display: flex; justify-content: space-between; align-items: flex-start; }
.kpi-label { font-size: 13px; color: #64748b; }
.kpi-trend { font-size: 12px; font-weight: 600; border-radius: 6px; padding: 2px 7px; }
.kpi-trend.up { color: #10b981; background: #f0fdf4; }
.kpi-trend.down { color: #ef4444; background: #fef2f2; }
.kpi-value { font-size: 28px; font-weight: 800; color: #0f172a; line-height: 1.2; }
.kpi-compare { font-size: 12px; color: #94a3b8; }
.chart-section { display: flex; gap: 16px; margin-bottom: 24px; }
.panel-card { background: #fff; border-radius: 16px; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 12px rgba(0,0,0,0.04); overflow: hidden; margin-bottom: 20px; }
.panel-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid #f1f5f9; }
.panel-title { font-size: 14px; font-weight: 700; color: #0f172a; }
.panel-sub { font-size: 12px; color: #94a3b8; }
.chart-col { flex: 0 0 280px; }
.chart-col-wide { flex: 1; }
.donut-chart { display: flex; flex-direction: column; gap: 12px; padding: 20px; }
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
.tc-bar { width: 18px; border-radius: 3px 3px 0 0; min-height: 4px; transition: height 0.4s; }
.alert-bar { background: linear-gradient(to top, #ef4444, #fca5a5); }
.tc-day { font-size: 11px; color: #94a3b8; }
.response-bar-row { display: flex; align-items: center; gap: 10px; }
.rb-bg { flex: 1; height: 6px; background: #f1f5f9; border-radius: 3px; }
.rb-fill { height: 6px; border-radius: 3px; transition: width 0.5s; }
.rate-good { color: #10b981; font-weight: 700; }
.rate-warn { color: #f59e0b; font-weight: 700; }
@media (max-width: 1200px) { .kpi-grid { grid-template-columns: repeat(2, 1fr); } .chart-section { flex-direction: column; } .chart-col { flex: none; } }
</style>
