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

        <div class="panel-card chart-panel-v2">
          <div class="cpv2-inner">
            <!-- 左：趋势图 -->
            <div class="cpv2-left">
              <div class="cpv2-header">
                <span class="panel-title">{{ chartTitle }}</span>
                <div class="cpv2-stats">
                  <span class="stat-badge">最大 <b>{{ chartStatMax }}</b></span>
                  <span class="stat-badge">最小 <b>{{ chartStatMin }}</b></span>
                  <span class="stat-badge">均值 <b>{{ chartStatAvg }}</b></span>
                </div>
              </div>
              <div class="cpv2-chart-wrap">
                <div class="chart-yaxis">
                  <span class="yaxis-line top"></span>
                  <span class="yaxis-line mid"></span>
                  <span class="yaxis-line bot"></span>
                </div>
                <div class="chart-bars-v2">
                  <div v-for="bar in chartBars" :key="bar.dayKey" class="bar-group-v2">
                    <div class="bar-col-v2">
                      <div
                        v-if="filterMetric !== 'spo2' && filterMetric !== 'temperature' && filterMetric !== 'steps'"
                        class="bar hr-bar"
                        :style="{ height: bar.hrHeight + '%' }"
                      >
                        <span class="bar-tip">{{ bar.hrText }}</span>
                      </div>
                      <div
                        v-if="filterMetric !== 'heartRate' && filterMetric !== 'temperature' && filterMetric !== 'steps'"
                        class="bar spo2-bar"
                        :style="{ height: bar.spo2Height + '%' }"
                      >
                        <span class="bar-tip">{{ bar.spo2Text }}</span>
                      </div>
                      <div
                        v-if="filterMetric === 'temperature'"
                        class="bar temp-bar"
                        :style="{ height: bar.tempHeight + '%' }"
                      >
                        <span class="bar-tip">{{ bar.tempText }}</span>
                      </div>
                      <div
                        v-if="filterMetric === 'steps'"
                        class="bar step-bar"
                        :style="{ height: bar.stepHeight + '%' }"
                      >
                        <span class="bar-tip">{{ bar.stepText }}</span>
                      </div>
                    </div>
                    <span class="bar-label">{{ bar.day }}</span>
                  </div>
                </div>
              </div>
              <div class="cpv2-legend">
                <template v-if="filterMetric === 'temperature'">
                  <span class="legend-dot" style="background:#f59e0b"></span><span>体温</span>
                </template>
                <template v-else-if="filterMetric === 'steps'">
                  <span class="legend-dot" style="background:#a855f7"></span><span>步数</span>
                </template>
                <template v-else>
                  <span v-if="filterMetric !== 'spo2'" class="legend-dot" style="background:#3b82f6"></span>
                  <span v-if="filterMetric !== 'spo2'">心率</span>
                  <span v-if="filterMetric !== 'heartRate'" class="legend-dot" style="background:#10b981; margin-left:12px"></span>
                  <span v-if="filterMetric !== 'heartRate'">血氧</span>
                </template>
              </div>
            </div>
            <!-- 右：健康评分 -->
            <div class="cpv2-right">
              <div class="health-score-wrap">
                <svg class="score-ring" viewBox="0 0 120 120">
                  <circle cx="60" cy="60" r="50" fill="none" stroke="#f1f5f9" stroke-width="10"/>
                  <circle
                    cx="60" cy="60" r="50" fill="none"
                    :stroke="healthScoreColor"
                    stroke-width="10"
                    stroke-linecap="round"
                    :stroke-dasharray="`${healthScoreDash} 314`"
                    stroke-dashoffset="78.5"
                    style="transition: stroke-dasharray 0.8s ease"
                  />
                </svg>
                <div class="score-center">
                  <span class="score-num">{{ healthScore }}</span>
                  <span class="score-label">健康评分</span>
                </div>
              </div>
              <div class="metric-status-list">
                <div v-for="ms in metricStatusList" :key="ms.key" class="metric-status-row">
                  <span class="ms-icon" :style="{ background: ms.bg }"><el-icon :size="14"><component :is="ms.icon" /></el-icon></span>
                  <span class="ms-name">{{ ms.label }}</span>
                  <span class="ms-val">{{ ms.value }}<small>{{ ms.unit }}</small></span>
                  <el-tag :type="ms.tagType" size="small" effect="light" class="ms-tag">{{ ms.tagText }}</el-tag>
                </div>
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
  listDevices,
  listDeviceExtensions,
  listExceptions,
  listHeartRates,
  listSpo2s,
  listSteps,
  listSubjects,
  listTemps,
  type DeviceInfo,
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
  tempHeight: number
  stepHeight: number
  hrText: string
  spo2Text: string
  tempText: string
  stepText: string
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

  const tempByDay = new Map<string, number[]>()
  const stepByDay = new Map<string, number[]>()

  getRecordsByRange(tempRecords.value).forEach((item) => {
    const key = formatDayKey(item.createTime || '')
    if (!tempByDay.has(key)) tempByDay.set(key, [])
    tempByDay.get(key)?.push(parseNumber(item.value))
  })

  getRecordsByRange(stepRecords.value).forEach((item) => {
    const key = item.date || formatDayKey(item.createTime || '')
    if (!stepByDay.has(key)) stepByDay.set(key, [])
    stepByDay.get(key)?.push(parseNumber(item.value))
  })

  const allStepValues = [...stepByDay.values()].flat().filter(Boolean)
  const maxStep = allStepValues.length ? Math.max(...allStepValues) : 1

  return getRangeKeys().map((key) => {
    const date = new Date(key)
    const heartValues = (heartByDay.get(key) || []).filter(Boolean)
    const spo2Values = (spo2ByDay.get(key) || []).filter(Boolean)
    const tempValues = (tempByDay.get(key) || []).filter(Boolean)
    const stepValues = (stepByDay.get(key) || []).filter(Boolean)
    const hrAverage = heartValues.length ? heartValues.reduce((sum, value) => sum + value, 0) / heartValues.length : 0
    const spo2Average = spo2Values.length ? spo2Values.reduce((sum, value) => sum + value, 0) / spo2Values.length : 0
    const tempAverage = tempValues.length ? tempValues.reduce((sum, value) => sum + value, 0) / tempValues.length : 0
    const stepTotal = stepValues.length ? stepValues.reduce((sum, value) => sum + value, 0) : 0

    return {
      day: formatDayLabel(date),
      dayKey: key,
      hrHeight: hrAverage ? Math.min(100, Math.max(5, (hrAverage / 140) * 100)) : 0,
      spo2Height: spo2Average ? Math.min(100, Math.max(5, ((spo2Average - 88) / 12) * 100)) : 0,
      tempHeight: tempAverage ? Math.min(100, Math.max(5, ((tempAverage - 35) / 4) * 100)) : 0,
      stepHeight: stepTotal ? Math.min(100, Math.max(5, (stepTotal / maxStep) * 100)) : 0,
      hrText: hrAverage ? `${hrAverage.toFixed(0)} bpm` : '暂无心率',
      spo2Text: spo2Average ? `${spo2Average.toFixed(0)}%` : '暂无血氧',
      tempText: tempAverage ? `${tempAverage.toFixed(1)}°C` : '暂无体温',
      stepText: stepTotal ? `${stepTotal} 步` : '暂无步数'
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

const chartStatValues = computed(() => {
  const metric = filterMetric.value
  let values: number[] = []
  if (metric === 'temperature') {
    values = getRecordsByRange(tempRecords.value).map((r) => parseNumber(r.value)).filter(Boolean)
  } else if (metric === 'steps') {
    values = getRecordsByRange(stepRecords.value).map((r) => parseNumber(r.value)).filter(Boolean)
  } else if (metric === 'spo2') {
    values = getRecordsByRange(spo2Records.value).map((r) => parseNumber(r.value)).filter(Boolean)
  } else {
    values = getRecordsByRange(heartRates.value).map((r) => parseNumber(r.value)).filter(Boolean)
  }
  return values
})

const chartStatMax = computed(() => {
  const vals = chartStatValues.value
  return vals.length ? Math.max(...vals).toFixed(filterMetric.value === 'steps' ? 0 : 1) : '--'
})

const chartStatMin = computed(() => {
  const vals = chartStatValues.value
  return vals.length ? Math.min(...vals).toFixed(filterMetric.value === 'steps' ? 0 : 1) : '--'
})

const chartStatAvg = computed(() => {
  const vals = chartStatValues.value
  if (!vals.length) return '--'
  const avg = vals.reduce((s, v) => s + v, 0) / vals.length
  return avg.toFixed(filterMetric.value === 'steps' ? 0 : 1)
})

const healthScore = computed(() => {
  const userId = Number(selectedSubjectId.value || 0)
  const exceptions = [] as number[]
  const ext = subjectExtensionMap.value.get(userId)
  const hrVals = getRecordsByRange(heartRates.value).map((r) => parseNumber(r.value)).filter(Boolean)
  const spo2Vals = getRecordsByRange(spo2Records.value).map((r) => parseNumber(r.value)).filter(Boolean)
  const tempVals = getRecordsByRange(tempRecords.value).map((r) => parseNumber(r.value)).filter(Boolean)
  if (hrVals.some((v) => v > 100 || v < 55)) exceptions.push(1)
  if (spo2Vals.some((v) => v < 95)) exceptions.push(1)
  if (tempVals.some((v) => v >= 37.3)) exceptions.push(1)
  if (ext?.alarmContent && String(ext.alarmContent).trim()) exceptions.push(1)
  const count = exceptions.length
  if (count === 0) return 95
  if (count === 1) return 80
  return 65
})

const healthScoreColor = computed(() => {
  const s = healthScore.value
  if (s >= 90) return '#10b981'
  if (s >= 75) return '#f59e0b'
  return '#ef4444'
})

const healthScoreDash = computed(() => {
  // circumference = 2 * π * 50 ≈ 314
  return ((healthScore.value / 100) * 314).toFixed(1)
})

const metricStatusList = computed(() => {
  const hrVals = getRecordsByRange(heartRates.value).map((r) => parseNumber(r.value)).filter(Boolean)
  const spo2Vals = getRecordsByRange(spo2Records.value).map((r) => parseNumber(r.value)).filter(Boolean)
  const tempVals = getRecordsByRange(tempRecords.value).map((r) => parseNumber(r.value)).filter(Boolean)
  const stepVals = getRecordsByRange(stepRecords.value).map((r) => parseNumber(r.value)).filter(Boolean)

  const latestHr = hrVals.at(-1) ?? 0
  const latestSpo2 = spo2Vals.at(-1) ?? 0
  const latestTemp = tempVals.at(-1) ?? 0
  const latestStep = stepVals.at(-1) ?? 0

  const hrStatus = latestHr > 100 ? 'warning' : latestHr > 0 && latestHr < 55 ? 'danger' : 'success'
  const hrTag = latestHr > 100 ? '偏高' : latestHr > 0 && latestHr < 55 ? '偏低' : latestHr ? '正常' : '暂无'
  const spo2Status = latestSpo2 > 0 && latestSpo2 < 95 ? 'danger' : latestSpo2 ? 'success' : 'info'
  const spo2Tag = latestSpo2 > 0 && latestSpo2 < 95 ? '偏低' : latestSpo2 ? '正常' : '暂无'
  const tempStatus = latestTemp >= 37.3 ? 'warning' : latestTemp > 0 && latestTemp < 36 ? 'danger' : latestTemp ? 'success' : 'info'
  const tempTag = latestTemp >= 37.3 ? '偏高' : latestTemp > 0 && latestTemp < 36 ? '偏低' : latestTemp ? '正常' : '暂无'
  const stepTag = latestStep >= 6000 ? '达标' : latestStep > 0 ? '不足' : '暂无'
  const stepStatus = latestStep >= 6000 ? 'success' : latestStep > 0 ? 'warning' : 'info'

  return [
    { key: 'hr', label: '心率', value: latestHr ? `${latestHr}` : '--', unit: ' bpm', icon: Odometer, bg: 'linear-gradient(135deg,#eff6ff,#dbeafe)', tagType: hrStatus as any, tagText: hrTag },
    { key: 'spo2', label: '血氧', value: latestSpo2 ? `${latestSpo2}` : '--', unit: ' %', icon: Aim, bg: 'linear-gradient(135deg,#f0fdf4,#dcfce7)', tagType: spo2Status as any, tagText: spo2Tag },
    { key: 'temp', label: '体温', value: latestTemp ? latestTemp.toFixed(1) : '--', unit: ' °C', icon: Sunny, bg: 'linear-gradient(135deg,#fff7ed,#fed7aa)', tagType: tempStatus as any, tagText: tempTag },
    { key: 'step', label: '步数', value: latestStep ? `${latestStep}` : '--', unit: ' 步', icon: Bicycle, bg: 'linear-gradient(135deg,#fdf4ff,#f3e8ff)', tagType: stepStatus as any, tagText: stepTag }
  ]
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
    const [subjectRes, deviceRes, extensionRes, exceptionRes] = await Promise.all([
      listSubjects({ pageNum: 1, pageSize: 200 }),
      listDevices({ pageNum: 1, pageSize: 200 }),
      listDeviceExtensions({ pageNum: 1, pageSize: 200 }),
      listExceptions({ pageNum: 1, pageSize: 200 })
    ])

    subjects.value = (subjectRes.rows || []) as HealthSubject[]

    const extByDeviceId = new Map<number, DeviceInfoExtend>()
    ;((extensionRes.rows || []) as DeviceInfoExtend[]).forEach((item) => {
      const deviceId = Number(item.deviceId || 0)
      if (deviceId) extByDeviceId.set(deviceId, item)
    })

    const extensionMap = new Map<number, DeviceInfoExtend>()
    ;((deviceRes.rows || []) as DeviceInfo[]).forEach((device) => {
      const userId = Number(device.userId || 0)
      const deviceId = Number(device.id || 0)
      if (!userId || !deviceId) return
      const ext = extByDeviceId.get(deviceId)
      if (ext) extensionMap.set(userId, ext)
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

.legend-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-right: 4px; }

/* ===== 升级版图表面板 ===== */
.chart-panel-v2 { overflow: visible; }

.cpv2-inner {
  display: flex;
  gap: 0;
  min-height: 280px;
}

.cpv2-left {
  flex: 1 1 65%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 16px 20px 14px;
  border-right: 1px solid #f1f5f9;
}

.cpv2-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.cpv2-stats {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.stat-badge {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 2px 10px;
  font-size: 11px;
  color: #64748b;
}

.stat-badge b { color: #0f172a; font-weight: 700; }

.cpv2-chart-wrap {
  flex: 1;
  position: relative;
  display: flex;
  min-height: 0;
}

.chart-yaxis {
  position: absolute;
  inset: 0;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-bottom: 20px;
}

.yaxis-line {
  display: block;
  width: 100%;
  height: 1px;
  background: repeating-linear-gradient(90deg, #e2e8f0 0, #e2e8f0 4px, transparent 4px, transparent 10px);
  opacity: 0.7;
}

.chart-bars-v2 {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 180px;
  width: 100%;
  padding-bottom: 20px;
  position: relative;
  z-index: 1;
}

.bar-group-v2 {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.bar-col-v2 {
  flex: 1;
  width: 100%;
  height: 0;
  min-height: 0;
  display: flex;
  align-items: flex-end;
  gap: 3px;
  justify-content: center;
  position: relative;
}

.bar {
  width: 14px;
  border-radius: 4px 4px 0 0;
  transition: height 0.5s cubic-bezier(.4,0,.2,1);
  min-height: 0;
  position: relative;
  cursor: default;
}

.bar:hover .bar-tip {
  opacity: 1;
  transform: translateX(-50%) translateY(-4px);
}

.bar-tip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%) translateY(0);
  background: #0f172a;
  color: #fff;
  font-size: 10px;
  white-space: nowrap;
  padding: 3px 7px;
  border-radius: 6px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s, transform 0.2s;
  margin-bottom: 4px;
  z-index: 10;
}

.bar-tip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 4px solid transparent;
  border-top-color: #0f172a;
}

.hr-bar   { background: linear-gradient(to top, #3b82f6, #93c5fd); }
.spo2-bar { background: linear-gradient(to top, #10b981, #6ee7b7); }
.temp-bar { background: linear-gradient(to top, #f59e0b, #fcd34d); }
.step-bar { background: linear-gradient(to top, #a855f7, #d8b4fe); }
.bar-label { font-size: 11px; color: #94a3b8; }

.cpv2-legend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
  margin-top: 6px;
}

/* ===== 右侧健康评分 ===== */
.cpv2-right {
  flex: 0 0 35%;
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
  gap: 16px;
  min-width: 0;
}

.health-score-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.score-ring {
  width: 100px;
  height: 100px;
}

.score-center {
  position: relative;
  margin-top: -74px;
  width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  pointer-events: none;
}

.score-num {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
}

.score-label {
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
}

.score-desc {
  font-size: 12px;
  font-weight: 600;
  text-align: center;
}

.metric-status-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.metric-status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
}

.ms-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  flex-shrink: 0;
  font-size: 14px;
}

.ms-name {
  font-size: 12px;
  color: #64748b;
  width: 28px;
  flex-shrink: 0;
}

.ms-val {
  flex: 1;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.ms-val small { font-size: 11px; color: #94a3b8; font-weight: 400; }

.ms-tag { flex-shrink: 0; }

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
