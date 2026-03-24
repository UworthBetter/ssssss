<template>
  <PlatformPageShellV2
    title="健康档案"
    subtitle="基于真实生命体征、步数和设备扩展数据，查看对象近 7 天健康走势与每日摘要。"
    eyebrow="HEALTH RECORDS"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <el-input v-model="searchName" placeholder="搜索对象姓名 / 账号" prefix-icon="Search" clearable style="width: 240px" />
        <el-select v-model="filterMetric" placeholder="体征指标" clearable style="width: 160px">
          <el-option label="全部指标" value="" />
          <el-option label="心率" value="heartRate" />
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
        <el-button type="primary" @click="fetchSelectedRecords">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </template>

    <div class="records-layout">
      <div class="subject-panel panel-card">
        <div class="panel-header">
          <span class="panel-title">服务对象</span>
          <el-tag size="small" type="info">{{ filteredSubjects.length }} 人</el-tag>
        </div>
        <div v-loading="loadingSubjects" class="subject-list">
          <div
            v-for="subject in filteredSubjects"
            :key="subject.subjectId"
            class="subject-item"
            :class="{ active: selectedSubjectId === subject.subjectId }"
            @click="selectedSubjectId = subject.subjectId"
          >
            <el-avatar :size="36" :class="'avatar-' + getSubjectRisk(subject.subjectId)">{{ getDisplayName(subject).charAt(0) }}</el-avatar>
            <div class="subject-item-info">
              <span class="sname">{{ getDisplayName(subject) }}</span>
              <span class="sage">{{ subject.age || '--' }}岁 · {{ subject.sex || '未知' }}</span>
            </div>
            <div class="risk-dot" :class="'dot-' + getSubjectRisk(subject.subjectId)"></div>
          </div>
          <el-empty v-if="!loadingSubjects && !filteredSubjects.length" description="暂无对象数据" />
        </div>
      </div>

      <div v-if="selectedSubject" class="detail-area" v-loading="loadingRecords">
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
            <div class="ov-trend" :class="card.trend">{{ trendLabel[card.trend] }}</div>
          </div>
        </div>

        <div class="panel-card chart-panel">
          <div class="panel-header">
            <span class="panel-title">{{ chartTitle }}</span>
            <div class="chart-legend">
              <span class="legend-dot" style="background:#3b82f6"></span>心率
              <span class="legend-dot" style="background:#10b981; margin-left:12px"></span>血氧
            </div>
          </div>
          <div class="chart-placeholder">
            <div class="chart-bars">
              <div v-for="bar in chartBars" :key="bar.dayKey" class="bar-group">
                <div class="bar-col">
                  <div class="bar hr-bar" :style="{ height: bar.hrHeight + '%' }" :title="bar.hrText"></div>
                  <div class="bar spo2-bar" :style="{ height: bar.spo2Height + '%' }" :title="bar.spo2Text"></div>
                </div>
                <span class="bar-label">{{ bar.day }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="panel-card">
          <div class="panel-header">
            <span class="panel-title">每日健康摘要</span>
          </div>
          <el-table :data="recentRecords" size="small" style="width: 100%">
            <el-table-column prop="time" label="记录时间" min-width="160" />
            <el-table-column label="心率(bpm)" min-width="100">
              <template #default="{ row }">
                <span :class="row.hrStatus">{{ row.heartRate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="spo2" label="血氧(%)" min-width="90" />
            <el-table-column prop="temp" label="体温(°C)" min-width="100" />
            <el-table-column prop="steps" label="步数" min-width="90" />
            <el-table-column label="状态" min-width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'normal' ? 'success' : 'warning'" size="small" effect="light">
                  {{ row.status === 'normal' ? '正常' : '关注' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div v-else class="empty-area">
        <el-icon class="empty-icon" :size="64"><UserFilled /></el-icon>
        <p>从左侧选择一位服务对象，查看其真实健康档案。</p>
      </div>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { UserFilled, Odometer, Aim, Sunny, Bicycle } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'
import {
  listDeviceExtensions,
  listExceptions,
  listHeartRates,
  listSpo2s,
  listSteps,
  listSubjects,
  listTemps,
  type DeviceInfoExtend,
  type ExceptionAlert,
  type HealthSubject,
  type StepRecord,
  type VitalRecord
} from '@/api/health'

type TrendState = 'up' | 'down' | 'stable'
type RiskState = 'high' | 'medium' | 'low'

interface ChartBar {
  day: string
  dayKey: string
  hrHeight: number
  spo2Height: number
  hrText: string
  spo2Text: string
}

interface DailyRecordRow {
  time: string
  heartRate: string
  hrStatus: string
  spo2: string
  temp: string
  steps: string
  status: 'normal' | 'warn'
}

const searchName = ref('')
const filterMetric = ref('')
const dateRange = ref<[Date, Date] | null>(null)
const selectedSubjectId = ref<number>()

const loadingSubjects = ref(false)
const loadingRecords = ref(false)

const subjects = ref<HealthSubject[]>([])
const subjectRiskMap = ref(new Map<number, RiskState>())
const subjectExtensionMap = ref(new Map<number, DeviceInfoExtend>())

const heartRates = ref<VitalRecord[]>([])
const spo2Records = ref<VitalRecord[]>([])
const tempRecords = ref<VitalRecord[]>([])
const stepRecords = ref<StepRecord[]>([])

const trendLabel: Record<TrendState, string> = { up: '上升', down: '下降', stable: '平稳' }

const getDisplayName = (subject: HealthSubject) => subject.nickName || subject.subjectName || `对象 #${subject.subjectId || '-'}`

const parseNumber = (value: unknown, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const parseTimestamp = (value?: string) => {
  if (!value) return 0
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

const formatDayKey = (value: Date | string) => {
  const date = typeof value === 'string' ? new Date(value) : value
  if (!Number.isFinite(date.getTime())) return ''
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

const formatDayLabel = (value: Date) => `${value.getMonth() + 1}/${value.getDate()}`

const formatDateTime = (value?: string) => {
  const timestamp = parseTimestamp(value)
  if (!timestamp) return '--'
  const date = new Date(timestamp)
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

const getRangeKeys = () => {
  if (dateRange.value?.length === 2) {
    const [start, end] = dateRange.value
    const result: string[] = []
    const cursor = new Date(start)
    cursor.setHours(0, 0, 0, 0)
    const endDate = new Date(end)
    endDate.setHours(0, 0, 0, 0)
    while (cursor.getTime() <= endDate.getTime()) {
      result.push(formatDayKey(cursor))
      cursor.setDate(cursor.getDate() + 1)
    }
    return result.slice(-7)
  }

  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date()
    date.setHours(0, 0, 0, 0)
    date.setDate(date.getDate() - (6 - index))
    return formatDayKey(date)
  })
}

const selectedSubject = computed(() => subjects.value.find((item) => item.subjectId === selectedSubjectId.value))

const filteredSubjects = computed(() => {
  const keyword = searchName.value.trim()
  return subjects.value.filter((item) => {
    if (!keyword) return true
    return [item.nickName, item.subjectName].some((field) => String(field || '').includes(keyword))
  })
})

const getSubjectRisk = (subjectId?: number) => subjectRiskMap.value.get(Number(subjectId || 0)) || 'low'

const getRecordsByRange = <T extends { createTime?: string; date?: string }>(records: T[]) => {
  const rangeKeys = new Set(getRangeKeys())
  return records.filter((item) => {
    const key = item.date || formatDayKey(item.createTime || '')
    return rangeKeys.has(key)
  })
}

const createMetricSummary = (values: number[]) => {
  if (!values.length) {
    return { current: '--', average: '--', trend: 'stable' as TrendState }
  }

  const latest = values[values.length - 1]
  const average = values.reduce((sum, value) => sum + value, 0) / values.length
  const first = values[0]
  const delta = latest - first

  return {
    current: latest.toFixed(1).replace(/\.0$/, ''),
    average: average.toFixed(1).replace(/\.0$/, ''),
    trend: delta > 2 ? 'up' : delta < -2 ? 'down' : 'stable'
  }
}

const overviewCards = computed(() => {
  const ext = subjectExtensionMap.value.get(Number(selectedSubjectId.value || 0))
  const heartSummary = createMetricSummary(getRecordsByRange(heartRates.value).map((item) => parseNumber(item.value)).filter(Boolean))
  const spo2Summary = createMetricSummary(getRecordsByRange(spo2Records.value).map((item) => parseNumber(item.value)).filter(Boolean))
  const tempSummary = createMetricSummary(getRecordsByRange(tempRecords.value).map((item) => parseNumber(item.value)).filter(Boolean))
  const latestStep = [...getRecordsByRange(stepRecords.value)].sort((a, b) => String(a.date || '').localeCompare(String(b.date || ''))).at(-1)
  const latestStepValue = parseNumber(latestStep?.value || ext?.step, 0)
  const previousStepValue = parseNumber([...getRecordsByRange(stepRecords.value)].sort((a, b) => String(a.date || '').localeCompare(String(b.date || ''))).at(-2)?.value, latestStepValue)
  const stepTrend: TrendState = latestStepValue > previousStepValue ? 'up' : latestStepValue < previousStepValue ? 'down' : 'stable'

  return [
    { label: '最近心率', value: heartSummary.current, unit: 'bpm', icon: Odometer, bg: 'linear-gradient(135deg,#eff6ff,#dbeafe)', trend: heartSummary.trend },
    { label: '最近血氧', value: spo2Summary.current, unit: '%', icon: Aim, bg: 'linear-gradient(135deg,#f0fdf4,#dcfce7)', trend: spo2Summary.trend },
    { label: '最近体温', value: tempSummary.current, unit: '°C', icon: Sunny, bg: 'linear-gradient(135deg,#fff7ed,#fed7aa)', trend: tempSummary.trend },
    { label: '最新步数', value: latestStepValue ? `${latestStepValue}` : '--', unit: '步', icon: Bicycle, bg: 'linear-gradient(135deg,#fdf4ff,#f3e8ff)', trend: stepTrend }
  ]
})

const chartBars = computed<ChartBar[]>(() => {
  const heartByDay = new Map<string, number[]>()
  const spo2ByDay = new Map<string, number[]>()

  getRecordsByRange(heartRates.value).forEach((item) => {
    const key = formatDayKey(item.createTime || '')
    if (!heartByDay.has(key)) heartByDay.set(key, [])
    heartByDay.get(key)?.push(parseNumber(item.value))
  })

  getRecordsByRange(spo2Records.value).forEach((item) => {
    const key = formatDayKey(item.createTime || '')
    if (!spo2ByDay.has(key)) spo2ByDay.set(key, [])
    spo2ByDay.get(key)?.push(parseNumber(item.value))
  })

  return getRangeKeys().map((key) => {
    const date = new Date(key)
    const heartValues = (heartByDay.get(key) || []).filter(Boolean)
    const spo2Values = (spo2ByDay.get(key) || []).filter(Boolean)
    const hrAverage = heartValues.length ? heartValues.reduce((sum, value) => sum + value, 0) / heartValues.length : 0
    const spo2Average = spo2Values.length ? spo2Values.reduce((sum, value) => sum + value, 0) / spo2Values.length : 0

    return {
      day: formatDayLabel(date),
      dayKey: key,
      hrHeight: hrAverage ? Math.min(100, Math.max(8, (hrAverage / 140) * 100)) : 8,
      spo2Height: spo2Average ? Math.min(100, Math.max(8, spo2Average)) : 8,
      hrText: hrAverage ? `${hrAverage.toFixed(0)} bpm` : '暂无心率',
      spo2Text: spo2Average ? `${spo2Average.toFixed(0)}%` : '暂无血氧'
    }
  })
})

const chartTitle = computed(() => {
  switch (filterMetric.value) {
    case 'heartRate':
      return '心率趋势（近 7 天）'
    case 'spo2':
      return '血氧趋势（近 7 天）'
    case 'temperature':
      return '体温趋势（近 7 天）'
    case 'steps':
      return '步数趋势（近 7 天）'
    default:
      return '心率与血氧趋势（近 7 天）'
  }
})

const recentRecords = computed<DailyRecordRow[]>(() => {
  const rangeKeys = [...getRangeKeys()].reverse()

  return rangeKeys.map((key) => {
    const heart = getRecordsByRange(heartRates.value)
      .filter((item) => formatDayKey(item.createTime || '') === key)
      .sort((a, b) => parseTimestamp(b.createTime) - parseTimestamp(a.createTime))[0]
    const spo2 = getRecordsByRange(spo2Records.value)
      .filter((item) => formatDayKey(item.createTime || '') === key)
      .sort((a, b) => parseTimestamp(b.createTime) - parseTimestamp(a.createTime))[0]
    const temp = getRecordsByRange(tempRecords.value)
      .filter((item) => formatDayKey(item.createTime || '') === key)
      .sort((a, b) => parseTimestamp(b.createTime) - parseTimestamp(a.createTime))[0]
    const steps = getRecordsByRange(stepRecords.value)
      .filter((item) => item.date === key)
      .sort((a, b) => String(b.date || '').localeCompare(String(a.date || '')))[0]

    const heartValue = parseNumber(heart?.value)
    const spo2Value = parseNumber(spo2?.value)
    const tempValue = parseNumber(temp?.value)
    const stepValue = parseNumber(steps?.value)
    const abnormal = (heartValue && (heartValue > 100 || heartValue < 55)) || (spo2Value && spo2Value < 95) || (tempValue && tempValue >= 37.3)

    return {
      time: formatDateTime(heart?.createTime || spo2?.createTime || temp?.createTime) === '--' ? key : formatDateTime(heart?.createTime || spo2?.createTime || temp?.createTime),
      heartRate: heartValue ? `${heartValue}` : '--',
      hrStatus: abnormal ? 'val-warn' : '',
      spo2: spo2Value ? `${spo2Value}` : '--',
      temp: tempValue ? tempValue.toFixed(1) : '--',
      steps: stepValue ? `${stepValue}` : '--',
      status: abnormal ? 'warn' : 'normal'
    }
  })
})

const fetchSubjects = async () => {
  loadingSubjects.value = true
  try {
    const [subjectRes, extensionRes, exceptionRes] = await Promise.all([
      listSubjects({ pageNum: 1, pageSize: 200 }),
      listDeviceExtensions({ pageNum: 1, pageSize: 200 }),
      listExceptions({ pageNum: 1, pageSize: 200 })
    ])

    subjects.value = (subjectRes.rows || []) as HealthSubject[]

    const extensionMap = new Map<number, DeviceInfoExtend>()
    ;((extensionRes.rows || []) as DeviceInfoExtend[]).forEach((item) => {
      const userId = Number(item.userId || 0)
      if (userId) {
        extensionMap.set(userId, item)
      }
    })
    subjectExtensionMap.value = extensionMap

    const exceptionGroups = new Map<number, ExceptionAlert[]>()
    ;((exceptionRes.rows || []) as ExceptionAlert[]).forEach((item) => {
      const userId = Number(item.userId || 0)
      if (!userId) return
      if (!exceptionGroups.has(userId)) exceptionGroups.set(userId, [])
      exceptionGroups.get(userId)?.push(item)
    })

    const riskMap = new Map<number, RiskState>()
    subjects.value.forEach((subject) => {
      const userId = Number(subject.subjectId || 0)
      const ext = extensionMap.get(userId)
      const exceptions = exceptionGroups.get(userId) || []
      const unresolved = exceptions.filter((item) => String(item.state || '0') !== '1').length
      const hasAlarm = Boolean(String(ext?.alarmContent || '').trim())
      if (unresolved >= 2 || hasAlarm) {
        riskMap.set(userId, 'high')
      } else if (unresolved >= 1) {
        riskMap.set(userId, 'medium')
      } else {
        riskMap.set(userId, 'low')
      }
    })
    subjectRiskMap.value = riskMap

    const nextSelected = filteredSubjects.value[0]?.subjectId || subjects.value[0]?.subjectId
    if (!selectedSubjectId.value && nextSelected) {
      selectedSubjectId.value = nextSelected
    }
  } finally {
    loadingSubjects.value = false
  }
}

const fetchSelectedRecords = async () => {
  if (!selectedSubjectId.value) return

  loadingRecords.value = true
  try {
    const params = { pageNum: 1, pageSize: 500, userId: selectedSubjectId.value } as any
    const [heartRes, spo2Res, tempRes, stepsRes] = await Promise.all([
      listHeartRates(params),
      listSpo2s(params),
      listTemps(params),
      listSteps(params)
    ])

    heartRates.value = (heartRes.rows || []) as VitalRecord[]
    spo2Records.value = (spo2Res.rows || []) as VitalRecord[]
    tempRecords.value = (tempRes.rows || []) as VitalRecord[]
    stepRecords.value = (stepsRes.rows || []) as StepRecord[]
  } finally {
    loadingRecords.value = false
  }
}

const handleReset = () => {
  searchName.value = ''
  filterMetric.value = ''
  dateRange.value = null
  fetchSelectedRecords()
}

watch(selectedSubjectId, () => {
  fetchSelectedRecords()
})

watch(filteredSubjects, (list) => {
  if (!list.length) {
    selectedSubjectId.value = undefined
    return
  }
  if (!list.some((item) => item.subjectId === selectedSubjectId.value)) {
    selectedSubjectId.value = list[0].subjectId
  }
})

onMounted(async () => {
  await fetchSubjects()
  await fetchSelectedRecords()
})
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
  min-height: 520px;
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

.subject-panel {
  flex: 0 0 240px;
  max-height: 680px;
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
}

.subject-item:hover { background: #f8fafc; }
.subject-item.active { background: #eff6ff; }

.subject-item :deep(.el-avatar) {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.subject-item :deep(.avatar-high) { background: linear-gradient(135deg, #ef4444, #f87171); }
.subject-item :deep(.avatar-medium) { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.subject-item :deep(.avatar-low) { background: linear-gradient(135deg, #3b82f6, #60a5fa); }

.subject-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.sname { font-size: 14px; font-weight: 600; color: #0f172a; }
.sage { font-size: 12px; color: #64748b; }

.risk-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-high { background: #ef4444; }
.dot-medium { background: #f59e0b; }
.dot-low { background: #10b981; }

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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.ov-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  flex-shrink: 0;
}

.ov-data {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.ov-label { font-size: 12px; color: #64748b; }
.ov-value { font-size: 22px; font-weight: 800; color: #0f172a; line-height: 1.2; }
.ov-unit { font-size: 11px; color: #94a3b8; }

.ov-trend {
  font-size: 12px;
  font-weight: 700;
}

.ov-trend.up { color: #10b981; }
.ov-trend.down { color: #ef4444; }
.ov-trend.stable { color: #94a3b8; }

.chart-legend { display: flex; align-items: center; font-size: 12px; color: #64748b; }
.legend-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-right: 4px; }

.chart-placeholder { padding: 20px 20px 8px; }

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 160px;
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
}

.hr-bar { background: linear-gradient(to top, #3b82f6, #93c5fd); }
.spo2-bar { background: linear-gradient(to top, #10b981, #6ee7b7); }
.bar-label { font-size: 11px; color: #94a3b8; }

:deep(.val-warn) { color: #f59e0b; font-weight: 600; }

.empty-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #94a3b8;
}

.empty-icon { opacity: 0.3; }

@media (max-width: 1200px) {
  .overview-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 960px) {
  .records-layout { flex-direction: column; }
  .subject-panel { flex: none; width: 100%; max-height: 220px; }
}
</style>
