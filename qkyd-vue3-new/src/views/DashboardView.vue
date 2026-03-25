<template>
  <div class="page-shell light-glass-theme">
    
    <!-- ================= 0. 底层：全屏三维地图 ================= -->
    <div ref="amapRef" class="amap-fullscreen-bg"></div>
    <div v-if="mapLoadFailed" class="amap-fullscreen-bg map-core map-failed-fallback">
      <div class="radar-scan"></div>
      <el-icon class="map-icon"><Location /></el-icon>
      <h2>MAP SIGNAL LOST</h2>
      <p>高德地图初始化失败，请检查 Key 配置</p>
    </div>

    <!-- ================= 1. 顶部居中：战术胶囊信标 (HUD Pills) ================= -->
    <div class="hud-top-center">
      <div 
        class="hud-pill" 
        v-for="item in allPieCards" 
        :key="item.key"
        :class="[
          `pill--${item.key}`, 
          { 'is-active': activePill === item.key, 'is-inactive': activePill && activePill !== item.key }
        ]"
        @click="togglePill(item)"
      >
        <div class="pill-ring"></div>
        <span class="pill-label">{{ item.label }}</span>
        <span class="pill-value digital-font">{{ item.value }}</span>
      </div>
    </div>

    <!-- ================= 2. 左侧：全息渐变翼 ================= -->
    <div class="hud-side-panel hud-left fade-left">
      <!-- 价值卡片 (整合进左侧面板避免重叠) -->
      <div class="hud-value-cards">
        <div class="value-card">
          <div class="vc-title" title="今日异常对象">今日异常对象</div>
          <div class="vc-value digital-font">{{ valueCardsData.abnormalObjects }}<span class="vc-unit">人</span></div>
        </div>
        <div class="value-card">
          <div class="vc-title" title="高风险预警">高风险预警</div>
          <div class="vc-value digital-font text-danger">{{ valueCardsData.highRisk }}<span class="vc-unit">次</span></div>
        </div>
        <div class="value-card">
          <div class="vc-title" title="平均响应时长">平均响应时长</div>
          <div class="vc-value digital-font text-warning">{{ valueCardsData.avgResponseTime }}<span class="vc-unit">min</span></div>
        </div>
        <div class="value-card">
          <div class="vc-title" title="已闭环占比">已闭环占比</div>
          <div class="vc-value digital-font text-success">{{ valueCardsData.closedRatio }}<span class="vc-unit">%</span></div>
        </div>
      </div>

      <transition name="panel-fade" mode="out-in">
        <div v-if="!activePill" class="hud-content-block flex-fill" key="default">
          <div class="holo-title">EVENT STREAM <span>// 实时异常流</span></div>
          <div class="table-wrapper">
            <el-table :data="filteredExceptionList" height="100%" class="holo-table clickable-table" @row-click="handleRowClick">
              <el-table-column prop="nickName" label="姓名" min-width="60" />
              <el-table-column prop="type" label="类型" min-width="90" />
              <el-table-column prop="state" label="状态" min-width="80">
                <template #default="{ row }">
                  <span :class="['status-dot', row.state === '1' ? 'dot-ok' : 'dot-warn']"></span>
                  <span :class="['status-text', row.state === '1' ? 'text-ok' : 'text-warn']">
                    {{ row.state === '1' ? 'DONE' : 'PEND' }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <div v-else class="hud-content-block flex-fill" key="trend">
          <div class="trend-header">
            <div class="trend-title">
              <span class="dot" :style="{ backgroundColor: activeColor }"></span>
              {{ activePillName }} <span>// 24H 波动时序追踪</span>
            </div>
            <el-icon class="close-btn" @click="togglePill(null)"><Close /></el-icon>
          </div>
          <div ref="trendChartRef" class="trend-chart-container" style="height: 100%; min-height: 250px; width: 100%;"></div>
        </div>
      </transition>
    </div>

    <!-- ================= 3. 右侧：全息渐变翼 ================= -->
    <div class="hud-side-panel hud-right fade-right">
      <transition name="panel-fade" mode="out-in">
        <div :key="activePill ? 'active' : 'default'" class="hud-content-block h-full flex-fill" style="display: flex; flex-direction: column;">
          <div class="hud-content-block">
            <div class="holo-title">
              {{ activePill ? 'STATUS' : 'ANALYSIS' }} 
              <span>{{ activePill ? '// 处理状态分布' : '// 异常占比' }}</span>
            </div>
            <div ref="kpiPieRef" class="kpi-pie-slim" />
          </div>

          <div class="hud-content-block flex-fill ai-logs-block" style="margin-top: 24px;">
            <div class="holo-title">
              {{ activePill ? 'RELATED LOGS' : 'AI LOGS' }}
              <span>// {{ activePill ? '相关判定' : '最新判定' }}</span>
            </div>
            <div class="ai-log-feed">
              <!-- 空态：系统正常占位 -->
              <div v-if="!aiLogFeedItems.length" class="ai-log-nominal">
                <div class="nominal-pulse"></div>
                <div class="nominal-text">
                  <span class="nominal-label">SYSTEM NOMINAL</span>
                  <span class="nominal-sub">无异常研判记录</span>
                </div>
              </div>
              <!-- 研判条目 Feed -->
              <div
                v-for="(item, i) in aiLogFeedItems"
                :key="i"
                class="ai-log-item"
                :class="`risk-${item._riskKey}`"
                @click="handleAiLogClick(item)"
              >
                <div class="ali-indicator">
                  <div class="ali-dot" :style="activePill ? { background: activeColor, boxShadow: `0 0 6px ${activeColor}` } : {}"></div>
                </div>
                <div class="ali-body">
                  <div class="ali-top">
                    <span class="ali-name" @click.stop="handleAiLogSubjectClick(item)">{{ item.patientName }}</span>
                    <span class="ali-type" :style="activePill ? { color: activeColor } : {}">{{ item.abnormalType }}</span>
                  </div>
                  <div class="ali-bottom">
                    <span class="ali-risk-badge" :class="`badge-${item._riskKey}`">{{ item._riskLabel }}</span>
                    <span v-if="item._conf" class="ali-conf">AI {{ item._conf }}%</span>
                    <span v-if="item._timeStr" class="ali-time digital-font">{{ item._timeStr }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- ================= 4. 底部：系统状态横条 ================= -->
    <div class="hud-bottom-center fade-bottom">
      <div class="status-bar">
        <!-- 左侧：统计指标组 -->
        <div class="sb-stats">
          <div class="sb-stat">
            <span class="sb-label">总事件</span>
            <span class="sb-value digital-font">{{ filteredExceptionList.length }}</span>
          </div>
          <div class="sb-divider"></div>
          <div class="sb-stat">
            <span class="sb-label">已处置</span>
            <span class="sb-value digital-font text-success">{{ filteredExceptionList.filter(r => r.state === '1').length }}</span>
          </div>
          <div class="sb-divider"></div>
          <div class="sb-stat">
            <span class="sb-label">待处置</span>
            <span class="sb-value digital-font text-warning">{{ filteredExceptionList.filter(r => r.state !== '1').length }}</span>
          </div>
          <div class="sb-divider"></div>
          <div class="sb-stat">
            <span class="sb-label">处置率</span>
            <span class="sb-value digital-font text-success">{{ filteredExceptionList.length ? ((filteredExceptionList.filter(r => r.state === '1').length / filteredExceptionList.length) * 100).toFixed(0) : 0 }}%</span>
          </div>
        </div>
        <!-- 中间：分隔线 + LIVE 标识 -->
        <div class="sb-live">
          <span class="live-dot"></span>
          <span class="live-label">实时动态</span>
        </div>
        <!-- 右侧：最新事件跑马灯 -->
        <div class="sb-ticker-wrap">
          <div class="sb-ticker" v-if="filteredExceptionList.length">
            <span
              v-for="(row, i) in filteredExceptionList.slice(0, 6)"
              :key="i"
              class="ticker-item"
              :class="row.state !== '1' ? 'ticker-warn' : 'ticker-ok'"
              @click="handleRowClick(row)"
            >
              <span class="ticker-name">{{ row.nickName }}</span>
              <span class="ticker-type">{{ row.type }}</span>
              <span class="ticker-status">{{ row.state === '1' ? '已处置' : '待处置' }}</span>
              <span class="ticker-sep">·</span>
            </span>
          </div>
          <div class="sb-ticker-empty" v-else>
            <span class="digital-font">-- 暂无异常事件 --</span>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Close } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { PieChart, LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent } from 'echarts/components'
import { LabelLayout } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import AMapLoader from '@amap/amap-jsapi-loader'
import { ElMessage } from 'element-plus'
import { getAbnormalTrend, getRecentAbnormal } from '@/api/ai'
import { getIndexException, getRealTimeData } from '@/api/index'
import { useHealthRealtimeStream } from '@/composables/useHealthRealtimeStream'

echarts.use([PieChart, LineChart, TooltipComponent, GridComponent, LabelLayout, CanvasRenderer])

interface GenericRow {
  [key: string]: unknown
}

interface ExceptionRow extends GenericRow {
  id?: number | string
  userId?: number | string
  nickName?: string
  type?: string
  state?: string | number
  location?: string
  readTime?: string
  createTime?: string
  longitude?: number | string
  latitude?: number | string
  _lng?: number
  _lat?: number
  _markerKey?: string
}

interface RecentAbnormalRow {
  patientName: string
  abnormalType: string
  riskLevel: string
  userId?: number | string
  eventId?: number | string
  abnormalValue?: string
  createTime?: string
  readTime?: string
  detectedTime?: string
  confidence?: number | string
  state?: string
  location?: string
}

interface TrendPoint {
  label: string
  value: number
}

declare global {
  interface Window {
    _AMapSecurityConfig?: { securityJsCode?: string }
    AMap?: any
  }
}

const realTimeData = ref<Record<string, unknown>>({})
const ageSexTable = ref<Array<{ label: string; value: number }>>([])
const recentAbnormal = ref<RecentAbnormalRow[]>([])
const exceptionList = ref<ExceptionRow[]>([])
const fetching = ref(false)
const router = useRouter()
const activePill = ref<string | null>(null)
const activePillName = ref<string>('')
const activeColor = ref<string>('#0ea5e9')
const trendData = ref<TrendPoint[]>([])
const trendChartRef = ref<HTMLDivElement | null>(null)
let trendChartInstance: echarts.ECharts | null = null

const pillColors: Record<string, string> = {
  step: '#0ea5e9',
  fence: '#f59e0b',
  sos: '#ef4444',
  temperature: '#06b6d4',
  heart: '#ec4899',
  oxygen: '#10b981',
  pressure: '#8b5cf6'
}

const trendMetricTypeMap: Record<string, string> = {
  step: 'step',
  fence: 'fence',
  sos: 'sos',
  temperature: 'temperature',
  heart: 'heart_rate',
  oxygen: 'spo2',
  pressure: 'blood_pressure'
}

const amapRef = ref<HTMLDivElement | null>(null)
const mapLoadFailed = ref(false)
let amapInstance: any = null
let amapInfoWindow: any = null
let mapMarkers: any[] = []
const markerMap = new Map<string, any>()
let lastExceptionDigest = ''
let lastFitSignature = ''
let fitViewTimer: ReturnType<typeof setTimeout> | null = null
let refreshTimer: ReturnType<typeof setInterval> | null = null
let resizeTimer: ReturnType<typeof setTimeout> | null = null
let realtimeRefreshTimer: ReturnType<typeof setTimeout> | null = null
let pollRound = 0
let amapLoaderPromise: Promise<any> | null = null

const MAP_CENTER: [number, number] = [116.397428, 39.90923]
const POLL_INTERVAL = 30000
const DETAIL_REFRESH_EVERY_ROUNDS = 1

const clearRealtimeRefresh = () => {
  if (realtimeRefreshTimer) {
    clearTimeout(realtimeRefreshTimer)
    realtimeRefreshTimer = null
  }
}

const scheduleRealtimeRefresh = () => {
  clearRealtimeRefresh()
  realtimeRefreshTimer = setTimeout(() => {
    void fetchAll(true)
  }, 400)
}

const realtimeStream = useHealthRealtimeStream({
  onAbnormalAlert() {
    scheduleRealtimeRefresh()
  },
  onHealthData() {
    scheduleRealtimeRefresh()
  },
  onRiskScore() {
    scheduleRealtimeRefresh()
  }
})

const syncRealtimeSubscriptions = (recentRows: RecentAbnormalRow[], exceptionRows: ExceptionRow[]) => {
  const ids = new Set<string | number>()
  recentRows.forEach((row) => {
    if (row.userId != null && row.userId !== '') ids.add(row.userId)
  })
  exceptionRows.forEach((row) => {
    if (row.userId != null && row.userId !== '') ids.add(row.userId)
  })
  realtimeStream.subscribePatients(Array.from(ids))
}

const toNumber = (value: unknown, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const metricValue = (aliases: string[]) => {
  for (const key of aliases) {
    if (Object.prototype.hasOwnProperty.call(realTimeData.value, key)) {
      return toNumber(realTimeData.value[key], 0)
    }
  }
  return 0
}

const isHighRiskLevel = (value: unknown) => {
  const text = String(value ?? '').trim().toLowerCase()
  if (!text) return false
  return ['high', 'critical', 'danger', '严重', '高', '高风险', '紧急'].some((keyword) => text.includes(keyword))
}

const isHighRiskException = (row: Pick<ExceptionRow, 'type'>) => {
  const type = String(row.type ?? '').toLowerCase()
  return ['sos', '求助', '心率', '血氧', '体温', '血压'].some((keyword) => type.includes(keyword))
}

const uniqueAbnormalObjectCount = computed(() => {
  const source = exceptionList.value
  const unique = new Set<string>()

  source.forEach((row) => {
    const identity = row.userId ?? row.nickName
    if (identity !== undefined && identity !== null && String(identity).trim() !== '') {
      unique.add(String(identity))
    }
  })

  if (unique.size > 0) return unique.size

  recentAbnormal.value.forEach((row) => {
    const identity = row.userId ?? row.patientName
    if (identity !== undefined && identity !== null && String(identity).trim() !== '') {
      unique.add(String(identity))
    }
  })

  return unique.size
})

const highRiskAlertCount = computed(() => {
  const recentCount = recentAbnormal.value.filter((row) => isHighRiskLevel(row.riskLevel)).length
  if (recentCount > 0) return recentCount

  const source = exceptionList.value
  return source.filter((row) => String(row.state ?? '0') !== '1' && isHighRiskException(row)).length
})

const avgResponseMinutes = computed(() => {
  const source = exceptionList.value
  const now = Date.now()
  const durations = source
    .filter((row) => String(row.state ?? '0') !== '1')
    .map((row) => row.readTime ?? row.createTime)
    .map((value) => new Date(String(value ?? '')).getTime())
    .filter((timestamp) => Number.isFinite(timestamp) && timestamp > 0)
    .map((timestamp) => Math.max(0, (now - timestamp) / 60000))

  if (durations.length === 0) return 0
  const average = durations.reduce((sum, item) => sum + item, 0) / durations.length
  return Number(average.toFixed(1))
})

const closedRatioValue = computed(() => {
  const source = exceptionList.value
  if (source.length === 0) return 0
  const resolved = source.filter((row) => String(row.state ?? '0') === '1').length
  return Number(((resolved / source.length) * 100).toFixed(1))
})

const valueCardsData = computed(() => {
  return {
    abnormalObjects: metricValue(['abnormalUserCount', 'todayExceptionUsers']) || uniqueAbnormalObjectCount.value,
    highRisk: metricValue(['highRiskCount', 'todayHighRisk']) || highRiskAlertCount.value,
    avgResponseTime: metricValue(['avgResponseTime']) || avgResponseMinutes.value,
    closedRatio: metricValue(['closedRatio']) || closedRatioValue.value
  }
})

const topAlertCards = computed(() => [
  { key: 'step', label: '步数异常', keywords: ['步'], value: metricValue(['stepExceptionCount', 'stepAbnormalCount', 'stepAlertCount']) },
  { key: 'fence', label: '围栏预警', keywords: ['围栏', '越界'], value: metricValue(['fenceExceptionCount', 'geofenceExceptionCount', 'fenceAlertCount']) },
  { key: 'sos', label: 'SOS 呼救', keywords: ['SOS', '求救'], value: metricValue(['sosHelpCount', 'sosCount', 'sosExceptionCount']) }
])

const kpiCards = computed(() => [
  { key: 'temperature', label: '体温', keywords: ['温'], value: metricValue(['temperatureExceptionCount', 'tempExceptionCount']) },
  { key: 'heart', label: '心率', keywords: ['心率', '心跳'], value: metricValue(['heartRateExceptionCount', 'hrExceptionCount']) },
  { key: 'oxygen', label: '血氧', keywords: ['氧'], value: metricValue(['spo2ExceptionCount', 'bloodOxygenExceptionCount']) },
  { key: 'pressure', label: '血压', keywords: ['压'], value: metricValue(['bloodPressureExceptionCount', 'bpExceptionCount']) }
])

const kpiPieRef = ref<HTMLDivElement | null>(null)
let kpiPieChart: echarts.ECharts | null = null

const stablePieCards = computed(() => [
  { key: 'step', label: '步数异常', keywords: ['step', 'Step', '步', '活动'], value: metricValue(['stepExceptionCount', 'stepAbnormalCount', 'stepAlertCount']) },
  { key: 'fence', label: '围栏预警', keywords: ['fence', 'Fence', 'geofence', '围栏', '越界'], value: metricValue(['fenceExceptionCount', 'geofenceExceptionCount', 'fenceAlertCount']) },
  { key: 'sos', label: 'SOS 呼救', keywords: ['sos', 'SOS', '求助'], value: metricValue(['sosHelpCount', 'sosCount', 'sosExceptionCount']) },
  { key: 'temperature', label: '体温', keywords: ['temperature', 'Temperature', '体温'], value: metricValue(['temperatureExceptionCount', 'tempExceptionCount']) },
  { key: 'heart', label: '心率', keywords: ['heart', 'Heart', 'pulse', '心率', '心跳'], value: metricValue(['heartRateExceptionCount', 'hrExceptionCount']) },
  { key: 'oxygen', label: '血氧', keywords: ['oxygen', 'Oxygen', 'spo2', 'SpO2', '血氧'], value: metricValue(['spo2ExceptionCount', 'bloodOxygenExceptionCount']) },
  { key: 'pressure', label: '血压', keywords: ['pressure', 'Pressure', 'blood pressure', '血压'], value: metricValue(['bloodPressureExceptionCount', 'bpExceptionCount']) }
])

const allPieCards = computed(() => stablePieCards.value)

const displayRecentAbnormal = computed(() => {
  if (!activePill.value) return recentAbnormal.value
  const item = allPieCards.value.find((card) => card.key === activePill.value)
  if (!item?.keywords?.length) return recentAbnormal.value
  return recentAbnormal.value.filter((log) => matchesKeywords(log.abnormalType, item.keywords))
})

const normalizeKeywordText = (value: unknown) => String(value ?? '')
  .trim()
  .toLowerCase()
  .replaceAll('_', ' ')
  .replaceAll('-', ' ')

const matchesKeywords = (value: unknown, keywords?: string[]) => {
  const normalizedValue = normalizeKeywordText(value)
  if (!normalizedValue || !keywords?.length) return false
  return keywords.some((keyword) => {
    const normalizedKeyword = normalizeKeywordText(keyword)
    return normalizedKeyword ? normalizedValue.includes(normalizedKeyword) : false
  })
}

const resolveAlertKeywords = (value: unknown) => {
  const matchedCard = allPieCards.value.find((card) => matchesKeywords(value, card.keywords))
  return matchedCard?.keywords ?? [String(value ?? '')]
}

const resolveRecentTime = (row: Pick<RecentAbnormalRow, 'detectedTime' | 'createTime' | 'readTime'>) => {
  const raw = row.detectedTime ?? row.createTime ?? row.readTime
  const timestamp = raw ? new Date(String(raw)).getTime() : NaN
  return Number.isFinite(timestamp) ? timestamp : 0
}

const isPlaceholderPatientName = (value: unknown) => {
  const text = String(value ?? '').trim()
  if (!text || text === '-') return true
  const normalized = text.toLowerCase()
  return normalized === 'unknown'
    || normalized === 'demo patient'
    || /^patient-\d+$/.test(normalized)
    || /^user[\s-]?\d+$/.test(normalized)
    || /^对象\s*#?\d+$/.test(text)
    || /^用户\s*#?\d+$/.test(text)
}

const resolveExceptionTime = (row: Pick<ExceptionRow, 'readTime' | 'createTime'>) => {
  const raw = row.readTime ?? row.createTime
  const timestamp = raw ? new Date(String(raw)).getTime() : NaN
  return Number.isFinite(timestamp) ? timestamp : 0
}

const inferRiskLevelFromException = (row: Pick<ExceptionRow, 'type' | 'state'>) => {
  return isHighRiskException(row) ? 'high' : String(row.state ?? '0') === '1' ? 'low' : 'medium'
}

const buildDetailKey = (row: Partial<RecentAbnormalRow>) => {
  return String(row.eventId ?? `${row.userId ?? row.patientName ?? 'unknown'}|${row.abnormalType ?? 'unknown'}|${resolveRecentTime(row as RecentAbnormalRow)}`)
}

const findMatchedException = (row: RecentAbnormalRow, source: ExceptionRow[]) => {
  const rowKeywords = resolveAlertKeywords(row.abnormalType)
  const rowUserId = row.userId != null ? String(row.userId) : ''
  const rowTime = resolveRecentTime(row)
  const candidates = source.filter((item) => {
    if (rowUserId && item.userId != null && String(item.userId) !== rowUserId) {
      return false
    }
    return matchesKeywords(item.type, rowKeywords)
  })

  if (!candidates.length) return null

  return candidates.reduce<ExceptionRow | null>((best, current) => {
    if (!best) return current
    if (rowTime <= 0) {
      return resolveExceptionTime(current) > resolveExceptionTime(best) ? current : best
    }

    const currentDiff = Math.abs(resolveExceptionTime(current) - rowTime)
    const bestDiff = Math.abs(resolveExceptionTime(best) - rowTime)
    if (currentDiff !== bestDiff) {
      return currentDiff < bestDiff ? current : best
    }
    return resolveExceptionTime(current) > resolveExceptionTime(best) ? current : best
  }, null)
}

const detailRowsForPanel = computed(() => {
  const exceptionCandidates = activePill.value ? filteredExceptionList.value : exceptionList.value
  const matchedExceptionKeys = new Set<string>()

  const mergedFromRecent = displayRecentAbnormal.value.map((row) => {
    const matched = findMatchedException(row, exceptionCandidates)
    if (matched?._markerKey) {
      matchedExceptionKeys.add(String(matched._markerKey))
    }

    const detectedTime = row.detectedTime ?? matched?.readTime ?? matched?.createTime
    const patientName = isPlaceholderPatientName(row.patientName)
      ? String(matched?.nickName ?? row.patientName ?? '-')
      : row.patientName
    return {
      ...row,
      patientName,
      eventId: row.eventId ?? matched?.id,
      location: row.location ?? matched?.location,
      state: String(matched?.state ?? row.state ?? '0'),
      detectedTime,
      riskLevel: row.riskLevel || inferRiskLevelFromException(matched ?? {}),
      _detailKey: buildDetailKey({
        ...row,
        eventId: row.eventId ?? matched?.id,
        detectedTime
      }),
      _eventTime: resolveRecentTime({
        ...row,
        detectedTime
      })
    }
  })

  const fallbackFromExceptions = exceptionCandidates
    .filter((row) => !matchedExceptionKeys.has(String(row._markerKey ?? '')))
    .map((row) => ({
      patientName: String(row.nickName ?? (row.userId ? `用户${row.userId}` : '-')),
      abnormalType: String(row.type ?? '-'),
      riskLevel: inferRiskLevelFromException(row),
      userId: row.userId,
      eventId: row.id,
      abnormalValue: undefined,
      createTime: row.createTime,
      readTime: row.readTime,
      detectedTime: row.readTime ?? row.createTime,
      confidence: undefined,
      state: String(row.state ?? '0'),
      location: row.location,
      _detailKey: buildDetailKey({
        patientName: row.nickName,
        abnormalType: row.type,
        userId: row.userId,
        eventId: row.id,
        detectedTime: row.readTime ?? row.createTime
      }),
      _eventTime: resolveExceptionTime(row)
    }))

  const deduped = new Map<string, (typeof mergedFromRecent)[number]>()
  ;[...mergedFromRecent, ...fallbackFromExceptions]
    .sort((a, b) => Number(b._eventTime ?? 0) - Number(a._eventTime ?? 0))
    .forEach((row) => {
      const key = String(row.eventId ?? row._detailKey)
      if (!deduped.has(key)) {
        deduped.set(key, row)
      }
    })

  return Array.from(deduped.values()).slice(0, 8)
})

// ---- AI LOG FEED ----
const MOCK_AI_LOGS: RecentAbnormalRow[] = [
  { patientName: '张三', abnormalType: '心率过速', riskLevel: 'high', createTime: new Date(Date.now() - 3 * 60000).toISOString() },
  { patientName: '李四', abnormalType: '围栏越界', riskLevel: 'medium', createTime: new Date(Date.now() - 11 * 60000).toISOString() },
  { patientName: '王五', abnormalType: '血氧偏低', riskLevel: 'high', createTime: new Date(Date.now() - 25 * 60000).toISOString() },
  { patientName: '赵六', abnormalType: 'SOS 求救', riskLevel: 'critical', createTime: new Date(Date.now() - 38 * 60000).toISOString() },
  { patientName: '陈七', abnormalType: '步数异常', riskLevel: 'low', createTime: new Date(Date.now() - 52 * 60000).toISOString() },
]

const normalizeRisk = (level: string) => {
  const l = String(level ?? '').toLowerCase()
  if (['critical', 'danger', '严重', '紧急'].some(k => l.includes(k))) return { key: 'critical', label: '严重' }
  if (['high', '高', '高风险'].some(k => l.includes(k))) return { key: 'high', label: '高风险' }
  if (['medium', 'warning', '中', '中风险'].some(k => l.includes(k))) return { key: 'medium', label: '中风险' }
  return { key: 'low', label: '低风险' }
}

const fmtRelativeTime = (ts?: string) => {
  if (!ts) return ''
  const diff = Math.round((Date.now() - new Date(ts).getTime()) / 60000)
  if (!Number.isFinite(diff) || diff < 0) return ''
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}min ago`
  return `${Math.round(diff / 60)}h ago`
}

const aiLogFeedItems = computed(() => {
  return detailRowsForPanel.value.map((row) => {
    const risk = normalizeRisk(row.riskLevel)
    const confidenceValue = Number(row.confidence)
    const confidence = Number.isFinite(confidenceValue)
      ? Math.max(0, Math.min(100, Math.round(confidenceValue)))
      : null
    return {
      ...row,
      _riskKey: risk.key,
      _riskLabel: risk.label,
      _conf: confidence,
      _timeStr: fmtRelativeTime(row.detectedTime ?? row.createTime ?? row.readTime)
    }
  })
})

const resolveSubjectKeyword = (row: Pick<RecentAbnormalRow, 'patientName' | 'userId'>) => {
  if (!isPlaceholderPatientName(row.patientName)) {
    return String(row.patientName ?? '').trim()
  }
  if (row.userId != null && String(row.userId).trim() !== '') {
    return String(row.userId)
  }
  return String(row.patientName ?? '').trim()
}

const handleAiLogSubjectClick = (row: Pick<RecentAbnormalRow, 'patientName' | 'userId'>) => {
  const keyword = resolveSubjectKeyword(row)
  if (!keyword) return
  void router.push({ path: '/subject', query: { keyword } })
}

const normalizeEventTypeQuery = (value: unknown) => {
  const text = String(value || '').trim()
  const lower = text.toLowerCase()
  if (!text) return ''
  if (lower.includes('heart') || text.includes('心率')) return '心率异常'
  if (lower.includes('spo2') || lower.includes('oxygen') || text.includes('血氧')) return '血氧异常'
  if (lower.includes('pressure') || lower.includes('blood') || text.includes('血压')) return '血压异常'
  if (lower.includes('temp') || lower.includes('temperature') || text.includes('体温')) return '体温异常'
  if (lower.includes('fence') || text.includes('围栏') || text.includes('越界')) return '围栏越界'
  if (lower.includes('sos') || text.includes('求救') || text.includes('求助')) return 'SOS求救'
  if (lower.includes('offline') || text.includes('离线')) return '设备离线'
  if (lower.includes('activity') || text.includes('活动') || text.includes('步数')) return '活动量异常'
  if (lower.includes('signal') || text.includes('信号')) return '设备信号异常'
  return text
}

const handleAiLogClick = (row: Pick<RecentAbnormalRow, 'userId' | 'abnormalType' | 'patientName'>) => {
  const query: Record<string, string> = {}
  if (row.userId != null && String(row.userId).trim() !== '') {
    query.userId = String(row.userId)
  }
  if (row.abnormalType && String(row.abnormalType).trim() !== '') {
    query.type = normalizeEventTypeQuery(row.abnormalType)
  }

  if (Object.keys(query).length > 0) {
    void router.push({ path: '/event', query })
    return
  }

  handleAiLogSubjectClick(row)
}

const pieData = computed(() => {
  if (activePill.value) {
    let resolved = 0
    let pending = 0
    detailRowsForPanel.value.forEach((row) => {
      if (String(row.state ?? '0') === '1') resolved++
      else pending++
    })
    if (resolved === 0 && pending === 0) return [{ name: '暂无记录', value: 1, itemStyle: { color: '#cbd5e1' } }]
    return [
      { name: '已解决', value: resolved, itemStyle: { color: '#10b981' } },
      { name: '待处理', value: pending, itemStyle: { color: '#f59e0b' } }
    ]
  }

  const source = allPieCards.value.map((item) => ({ name: item.label, value: Math.max(0, item.value) }))
  const total = source.reduce((sum, item) => sum + item.value, 0)
  return total > 0 ? source : source.map((item) => ({ ...item, value: 1 }))
})

const pieColors = ['#0ea5e9', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899']

const renderKpiPieChart = () => {
  if (!kpiPieChart) return
  kpiPieChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: 'rgba(14, 165, 233, 0.2)',
      textStyle: { color: '#334155', fontSize: 12 },
      formatter: '{b}: <span style="color:#0ea5e9; font-weight:bold;">{c}</span> ({d}%)'
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: { borderRadius: 4, borderColor: 'rgba(255,255,255,1)', borderWidth: 2 },
        label: { color: '#64748b', fontSize: 10, formatter: '{b}\n{d}%' },
        labelLine: { length: 5, length2: 5, lineStyle: { color: '#cbd5e1' } },
        data: pieData.value.map((item, index) => ({
          ...item,
          itemStyle: (item as any).itemStyle ? (item as any).itemStyle : { color: pieColors[index % pieColors.length] }
        }))
      }
    ]
  })
}

const togglePill = async (item: any) => {
  if (!item) {
    activePill.value = null
    trendData.value = []
    setTimeout(() => filterMapMarkers(), 50)
    return
  }

  if (activePill.value === item.key) {
    activePill.value = null
    trendData.value = []
    setTimeout(() => filterMapMarkers(), 50)
  } else {
    activePill.value = item.key
    activePillName.value = item.label
    activeColor.value = pillColors[item.key] || '#0ea5e9'
    setTimeout(() => filterMapMarkers(item.keywords), 50)
  }
}

const buildFallbackTrendData = () => {
  const times: string[] = []
  const data: number[] = []
  const now = new Date()

  for (let i = 24; i >= 0; i--) {
    const t = new Date(now.getTime() - i * 60 * 60 * 1000)
    times.push(`${String(t.getHours()).padStart(2, '0')}:00`)
    data.push(0)
  }
  return { times, data }
}

const normalizeTrendData = (input: unknown) => {
  if (!Array.isArray(input)) return [] as TrendPoint[]
  return input.map((item) => {
    const row = (item ?? {}) as Record<string, unknown>
    return {
      label: String(row.label ?? row.time ?? ''),
      value: toNumber(row.value, 0)
    }
  }).filter((item) => item.label)
}

const loadTrendData = async (pillKey: string) => {
  const metricType = trendMetricTypeMap[pillKey]
  if (!metricType) {
    trendData.value = []
    return
  }

  try {
    const response = await getAbnormalTrend(metricType, 25)
    trendData.value = normalizeTrendData(response.data)
  } catch (error) {
    trendData.value = []
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChartRef.value)
  }

  const { times, data } = trendData.value.length > 0
    ? {
        times: trendData.value.map((item) => item.label),
        data: trendData.value.map((item) => item.value)
      }
    : buildFallbackTrendData()
  const color = activeColor.value

  trendChartInstance.setOption({
    grid: { top: 30, right: 10, bottom: 20, left: 0, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: color,
      textStyle: { color: '#334155' },
      axisPointer: { type: 'line', lineStyle: { color, type: 'dashed' } }
    },
    xAxis: {
      type: 'category',
      data: times,
      boundaryGap: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 10, margin: 12 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: 'rgba(0,0,0,0.05)' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 }
    },
    series: [
      {
        name: activePillName.value,
        type: 'line',
        data,
        smooth: 0.4,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        itemStyle: { color },
        lineStyle: { width: 3, shadowColor: color, shadowBlur: 10, shadowOffsetY: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${color}80` },
            { offset: 1, color: `${color}00` }
          ])
        }
      }
    ]
  })
}

const filteredExceptionList = computed(() => {
  if (!activePill.value) return exceptionList.value
  const item = allPieCards.value.find((card) => card.key === activePill.value)
  if (!item?.keywords?.length) return exceptionList.value

  return exceptionList.value.filter((ex) =>
    item.keywords.some((kw) => String(ex.type ?? '').includes(kw))
  )
})

const normalizeAgeSexTable = (input: unknown) => {
  if (!input || typeof input !== 'object' || Array.isArray(input)) {
    return [] as Array<{ label: string; value: number }>
  }

  const source = input as Record<string, unknown>
  const schema: Array<{ key: string; label: string }> = [
    { key: 'a', label: '6-14岁' },
    { key: 'b', label: '15-19岁' },
    { key: 'c', label: '20-39岁' },
    { key: 'd', label: '40-59岁' },
    { key: 'e', label: '60岁及以上' },
    { key: 'man', label: '男性' },
    { key: 'woman', label: '女性' },
    { key: 'nono', label: '未知性别' }
  ]

  const rows = schema
    .filter((item) => Object.prototype.hasOwnProperty.call(source, item.key))
    .map((item) => ({ label: item.label, value: toNumber(source[item.key], 0) }))

  if (rows.length > 0) return rows

  return Object.entries(source).map(([label, value]) => ({
    label,
    value: toNumber(value, 0)
  }))
}

const normalizeRecentAbnormal = (input: unknown): RecentAbnormalRow[] => {
  if (!Array.isArray(input)) return []
  return input.map((row) => {
    const item = (row ?? {}) as Record<string, unknown>
    const patientName = String(item.patientName ?? item.nickName ?? (item.userId ? `用户${item.userId}` : '-'))
    const abnormalType = String(item.abnormalType ?? item.metricType ?? item.type ?? '-')
    const riskLevel = String(item.riskLevel ?? item.level ?? '-')
    const abnormalValue = item.abnormalValue != null ? String(item.abnormalValue) : undefined
    return {
      patientName,
      abnormalType,
      riskLevel,
      userId: item.userId as number | string | undefined,
      eventId: item.eventId as number | string | undefined ?? item.id as number | string | undefined,
      abnormalValue,
      createTime: item.createTime ? String(item.createTime) : undefined,
      readTime: item.readTime ? String(item.readTime) : undefined,
      detectedTime: item.detectedTime ? String(item.detectedTime) : undefined,
      confidence: item.confidence as number | string | undefined,
      state: item.state != null ? String(item.state) : undefined,
      location: item.location ? String(item.location) : undefined
    }
  })
}

const parseLngLatFromLocation = (location: unknown): [number, number] | null => {
  if (typeof location !== 'string') return null
  const matched = location.match(/(-?\d+(?:\.\d+)?)\s*[,，]\s*(-?\d+(?:\.\d+)?)/)
  if (!matched) return null
  const lng = Number(matched[1])
  const lat = Number(matched[2])
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) return null
  return [lng, lat]
}

const fallbackLngLat = (index: number): [number, number] => {
  const offsetLng = ((index % 7) - 3) * 0.01
  const offsetLat = ((Math.floor(index / 7) % 5) - 2) * 0.01
  return [MAP_CENTER[0] + offsetLng, MAP_CENTER[1] + offsetLat]
}

const getMarkerKey = (item: ExceptionRow, index: number) => {
  if (item.id !== undefined && item.id !== null && String(item.id).trim() !== '') {
    return `id:${String(item.id)}`
  }
  const nick = String(item.nickName ?? '')
  const type = String(item.type ?? '')
  const location = String(item.location ?? '')
  return `row:${nick}|${type}|${location}|${index}`
}

const normalizeExceptionList = (input: unknown): ExceptionRow[] => {
  if (!Array.isArray(input)) return []
  return input.map((row, index) => {
    const item = (row ?? {}) as ExceptionRow
    const fromLocation = parseLngLatFromLocation(item.location)
    const lng = Number(item.longitude)
    const lat = Number(item.latitude)
    const [fallbackLng, fallbackLat] = fallbackLngLat(index)

    return {
      ...item,
      state: String(item.state ?? '0'),
      _lng: Number.isFinite(lng) ? lng : (fromLocation?.[0] ?? fallbackLng),
      _lat: Number.isFinite(lat) ? lat : (fromLocation?.[1] ?? fallbackLat),
      _markerKey: getMarkerKey(item, index)
    }
  })
}

const buildMockExceptions = (seedRows: RecentAbnormalRow[]): ExceptionRow[] => {
  const base = seedRows.length > 0
    ? seedRows
    : [
      { patientName: '演示对象A', abnormalType: '心率异常', riskLevel: 'high' },
      { patientName: '演示对象B', abnormalType: '围栏越界', riskLevel: 'medium' },
      { patientName: '演示对象C', abnormalType: 'SOS求救', riskLevel: 'critical' }
    ]

  return base.slice(0, 8).map((item, index) => {
    const [lng, lat] = fallbackLngLat(index)
    return {
      id: `mock-${index}`,
      nickName: item.patientName || `演示对象${index + 1}`,
      type: item.abnormalType || '异常告警',
      state: index % 3 === 0 ? '1' : '0',
      location: `演示坐标 ${lng.toFixed(6)}, ${lat.toFixed(6)}`,
      _lng: lng,
      _lat: lat,
      _markerKey: `mock-${index}`
    }
  })
}

const initAmap = async () => {
  if (!amapRef.value) return

  const amapKey = import.meta.env.VITE_AMAP_KEY as string | undefined
  const amapSecurityJsCode = import.meta.env.VITE_AMAP_SECURITY_JS_CODE as string | undefined
  if (!amapKey || !amapSecurityJsCode) {
    mapLoadFailed.value = true
    return
  }

  window._AMapSecurityConfig = { securityJsCode: amapSecurityJsCode }

  try {
    if (!amapLoaderPromise) {
      amapLoaderPromise = AMapLoader.load({
        key: amapKey,
        version: '2.0',
        plugins: ['AMap.Scale', 'AMap.Marker', 'AMap.InfoWindow']
      })
    }
    const AMap = await amapLoaderPromise

    const isLowPowerDevice = ((navigator.hardwareConcurrency ?? 8) <= 4)
    amapInstance = new AMap.Map(amapRef.value, {
      viewMode: isLowPowerDevice ? '2D' : '3D',
      zoom: 14,
      center: MAP_CENTER,
      mapStyle: 'amap://styles/light',
      pitch: isLowPowerDevice ? 0 : 45,
      skyColor: '#f1f5f9',
      showBuildingBlock: !isLowPowerDevice
    })

    amapInfoWindow = new AMap.InfoWindow({ isCustom: true, autoMove: true, offset: new AMap.Pixel(0, -25) })
    amapInstance.addControl(new AMap.Scale({ position: 'RB', offset: new AMap.Pixel(300, 180) }))
    mapLoadFailed.value = false

    if (exceptionList.value.length > 0) {
      syncMapMarkers(exceptionList.value)
      const active = allPieCards.value.find((card) => card.key === activePill.value)
      filterMapMarkers(active?.keywords)
    }
  } catch (error) {
    mapLoadFailed.value = true
    ElMessage.error('地图引擎初始化失败')
  }
}

const buildMarkerContent = (row: ExceptionRow) => {
  const isResolved = String(row.state) === '1'
  const isCritical = String(row.type ?? '').toUpperCase().includes('SOS') || String(row.type ?? '').includes('求救')
  const matchedPill = allPieCards.value.find((pill) =>
    pill.keywords?.some((kw) => String(row.type ?? '').includes(kw))
  )
  const customColor = matchedPill && pillColors[matchedPill.key] ? pillColors[matchedPill.key] : null

  let markerColorClass = isResolved ? 'pulse-resolved' : 'pulse-custom'
  let customStyle = ''
  let coreStyle = ''

  if (!isResolved) {
    if (customColor) {
      customStyle = `color: ${customColor};`
      coreStyle = `background: ${customColor};`
    } else {
      markerColorClass = isCritical ? 'pulse-danger' : 'pulse-warning'
    }
  }

  return `<div class="holo-marker ${markerColorClass}" style="${customStyle}"><div class="core" style="${coreStyle}"></div><div class="ripple"></div></div>`
}

const syncMapMarkers = (rows: ExceptionRow[]) => {
  if (!amapInstance || !window.AMap) return

  const nextKeys = new Set<string>()
  const addedMarkers: any[] = []

  rows.forEach((row, index) => {
    if (!Number.isFinite(row._lng) || !Number.isFinite(row._lat)) return
    const key = row._markerKey || getMarkerKey(row, index)
    nextKeys.add(key)

    const marker = markerMap.get(key)
    const position: [number, number] = [row._lng as number, row._lat as number]
    const content = buildMarkerContent(row)

    if (marker) {
      marker.setPosition(position)
      marker.setExtData(row)
      marker.setContent(content)
      return
    }

    const nextMarker = new window.AMap.Marker({
      position,
      content,
      anchor: 'center',
      extData: row
    })
    nextMarker.on('click', () => openTechInfoWindow(nextMarker))
    markerMap.set(key, nextMarker)
    addedMarkers.push(nextMarker)
  })

  const removedMarkers: any[] = []
  markerMap.forEach((marker, key) => {
    if (!nextKeys.has(key)) {
      removedMarkers.push(marker)
      markerMap.delete(key)
    }
  })

  if (removedMarkers.length > 0) {
    amapInstance.remove(removedMarkers)
  }
  if (addedMarkers.length > 0) {
    amapInstance.add(addedMarkers)
  }

  mapMarkers = Array.from(markerMap.values())
}

const scheduleFitView = (markers: any[], signature: string) => {
  if (!amapInstance || markers.length === 0) return
  if (signature === lastFitSignature) return

  lastFitSignature = signature
  if (fitViewTimer) {
    clearTimeout(fitViewTimer)
  }
  fitViewTimer = setTimeout(() => {
    amapInstance?.setFitView(markers, false)
  }, 80)
}

const filterMapMarkers = (keywords?: string[]) => {
  if (!amapInstance || !mapMarkers.length) return

  amapInfoWindow?.close()
  const visibleMarkers: any[] = []

  mapMarkers.forEach((marker) => {
    const data = marker.getExtData()
    if (!keywords || keywords.some((kw) => String(data.type ?? '').includes(kw))) {
      marker.show()
      visibleMarkers.push(marker)
    } else {
      marker.hide()
    }
  })

  if (visibleMarkers.length > 0 && keywords) {
    scheduleFitView(visibleMarkers, `kw:${keywords.join('|')}|${visibleMarkers.length}`)
  } else if (!keywords) {
    scheduleFitView(mapMarkers, `all:${mapMarkers.length}`)
  }
}

const openTechInfoWindow = (marker: any) => {
  const data = marker.getExtData()
  const isCritical = String(data.type ?? '').toUpperCase().includes('SOS')
  const isResolved = String(data.state) === '1'
  const infoHtml = `
    <div class="holo-info-window ${isCritical ? 'critical' : 'warning'}">
      <div class="info-header">
        <span class="info-tag">${isResolved ? 'RESOLVED' : (isCritical ? 'CRITICAL ALERT' : 'WARNING')}</span>
        <h4 class="info-title">${data.type || '未知异常'}</h4>
      </div>
      <div class="info-body">
        <p>目标: <span class="digital-font hl">${data.nickName || 'Unknown'}</span></p>
        <p>位置: <span>${data.location || '坐标解析失败'}</span></p>
      </div>
    </div>
  `
  amapInfoWindow.setContent(infoHtml)
  amapInfoWindow.open(amapInstance, marker.getPosition())
}

const handleRowClick = (row: ExceptionRow) => {
  if (!amapInstance || !Number.isFinite(row._lng) || !Number.isFinite(row._lat)) return
  amapInstance.panTo([row._lng, row._lat])
  const targetMarker = row._markerKey ? markerMap.get(row._markerKey) : mapMarkers.find((marker) => {
    const pos = marker.getPosition()
    return pos.lng === row._lng && pos.lat === row._lat
  })
  if (targetMarker) setTimeout(() => openTechInfoWindow(targetMarker), 300)
}

const fetchAll = async (forceFull = false) => {
  if (fetching.value) return

  fetching.value = true
  try {
    const shouldRefreshDetails = forceFull || (pollRound % DETAIL_REFRESH_EVERY_ROUNDS === 0)
    const requests: Promise<unknown>[] = [getRealTimeData()]
    if (shouldRefreshDetails) {
      requests.push(getRecentAbnormal(8), getIndexException('all', 1))
    }

    const results = await Promise.all(requests)
    const rtRes = results[0] as Record<string, any>
    realTimeData.value = (rtRes.data ?? {}) as Record<string, unknown>

    if (shouldRefreshDetails) {
      const recentRes = results[1] as Record<string, any>
      const exceptionRes = results[2] as Record<string, any>

      recentAbnormal.value = normalizeRecentAbnormal(recentRes.data)

      const rawExceptions = exceptionRes.rows ?? exceptionRes.data ?? exceptionRes.list
      const normalizedExceptions = normalizeExceptionList(rawExceptions)
      exceptionList.value = normalizedExceptions
      syncRealtimeSubscriptions(recentAbnormal.value, exceptionList.value)

      if (amapInstance) {
        const digest = exceptionList.value
          .map((row) => `${row._markerKey}|${row._lng}|${row._lat}|${row.state}|${row.type}`)
          .join(';')
        if (digest !== lastExceptionDigest) {
          lastExceptionDigest = digest
          syncMapMarkers(exceptionList.value)
        }
        const active = allPieCards.value.find((card) => card.key === activePill.value)
        filterMapMarkers(active?.keywords)
      }
    }
    if (activePill.value) {
      await loadTrendData(activePill.value)
      renderTrendChart()
    }
    pollRound += 1
  } catch (error) {
    ElMessage.error('系统数据链路中断')
  } finally {
    fetching.value = false
  }
}

watch(() => pieData.value.map((item) => item.value).join('|'), () => {
  if (kpiPieChart) {
    renderKpiPieChart()
  }
})

watch(activePill, async () => {
  await nextTick()
  // 处理饼状图 re-init
  if (kpiPieRef.value) {
    if (kpiPieChart) {
      kpiPieChart.dispose()
      kpiPieChart = null
    }
    kpiPieChart = echarts.init(kpiPieRef.value)
    renderKpiPieChart()
  }
  // 处理趋势图 re-init (如果存在)
  if (trendChartRef.value) {
    if (trendChartInstance) {
      trendChartInstance.dispose()
      trendChartInstance = null
    }
    trendChartInstance = echarts.init(trendChartRef.value)
    if (activePill.value) {
      await loadTrendData(activePill.value)
    }
    renderTrendChart()
  }
})

const handleResize = () => {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    kpiPieChart?.resize()
    trendChartInstance?.resize()
    amapInstance?.resize?.()
  }, 120)
}

onMounted(async () => {
  // 先并行获取数据和初始化地图
  await Promise.all([fetchAll(true), initAmap()])
  
  // 关键：在数据更新后的下一个 tick 初始化图表，确保容器尺寸和 key 状态稳定
  await nextTick()
  
  if (!kpiPieChart && kpiPieRef.value) {
    kpiPieChart = echarts.init(kpiPieRef.value)
  }
  renderKpiPieChart()
  
  // 如果是第一次加载且有数据，多做一次 resize 以防容器计算延迟
  setTimeout(() => kpiPieChart?.resize(), 100)

  refreshTimer = setInterval(() => {
    void fetchAll()
  }, POLL_INTERVAL)

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  if (fitViewTimer) {
    clearTimeout(fitViewTimer)
    fitViewTimer = null
  }
  if (resizeTimer) {
    clearTimeout(resizeTimer)
    resizeTimer = null
  }
  clearRealtimeRefresh()
  markerMap.clear()
  kpiPieChart?.dispose()
  trendChartInstance?.dispose()
  amapInfoWindow?.close()
  amapInstance?.destroy()
})
</script>

<style scoped lang="scss">
@import url('https://fonts.googleapis.com/css2?family=Share+Tech+Mono&display=swap');

/* ================== 1. 核心布局 (亮色霜白主题) ================== */
.page-shell {
  position: relative; width: 100%; height: calc(100vh - 84px); 
  min-height: 600px; overflow: hidden; background-color: #f1f5f9; color: #334155; 
}

.amap-fullscreen-bg {
  position: absolute; inset: 0; z-index: 1; transition: filter 0.5s ease;
}

.hud-top-center, .hud-side-panel, .hud-bottom-center, .trend-overlay-panel {
  position: absolute; z-index: 10; pointer-events: none; 
}

/* ================== 2. 全息边缘渐变 (明亮毛玻璃) ================== */
.fade-left { background: linear-gradient(90deg, rgba(248, 250, 252, 0.95) 0%, rgba(248, 250, 252, 0.65) 50%, transparent 100%); }
.fade-right { background: linear-gradient(270deg, rgba(248, 250, 252, 0.95) 0%, rgba(248, 250, 252, 0.65) 50%, transparent 100%); }
.fade-bottom { background: linear-gradient(0deg, rgba(248, 250, 252, 0.95) 0%, rgba(248, 250, 252, 0.7) 40%, transparent 100%); }

.hud-side-panel {
  top: 0; bottom: 0; width: 320px; display: flex; flex-direction: column; padding: 60px 16px 20px; gap: 24px;
  transition: opacity 0.4s ease, transform 0.4s ease;
}
.hud-left { left: 0; padding-right: 20px; } 
.hud-right { right: 0; padding-left: 20px; }
.hud-bottom-center {
  bottom: 0; left: 320px; right: 320px; height: 72px; padding: 0 40px;
  display: flex; align-items: center;
  transition: opacity 0.4s ease, transform 0.4s ease;
}

/* 焦点模式激活时，周边面板退居二线 */
.panel-dim { opacity: 0.15; transform: scale(0.98); pointer-events: none !important; }

.hud-content-block { pointer-events: auto; display: flex; flex-direction: column; position: relative; }
.h-full { height: 100%; }
.flex-fill { flex: 1; min-height: 0; }
.table-wrapper { flex: 1; height: 0; }
.digital-font { font-family: 'Share Tech Mono', monospace; }

/* ================== 3. UI 元素极简化 ================== */
.holo-title {
  font-family: 'Share Tech Mono', monospace; font-size: 14px; color: #0ea5e9; font-weight: bold; letter-spacing: 1px;
  margin-bottom: 12px; padding-bottom: 4px; border-bottom: 1px solid rgba(14, 165, 233, 0.2);
  display: flex; align-items: baseline; gap: 8px;
  span { font-size: 10px; color: #94a3b8; font-family: sans-serif; font-weight: normal; }
}

/* 价值大屏卡片 (改为侧边栏内部网格布局) */
.hud-value-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  pointer-events: auto;
  flex-shrink: 0;
  margin-bottom: 24px;
}
.value-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  padding: 10px 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  width: 100%;
  transition: all 0.3s;
}
.value-card:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
}
.vc-title { font-size: 11px; color: #64748b; margin-bottom: 4px; font-weight: bold; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.vc-value { font-size: 20px; font-weight: bold; color: #0ea5e9; display: flex; align-items: baseline; gap: 4px; line-height: 1; }
.vc-unit { font-size: 11px; font-weight: normal; color: #94a3b8; font-family: sans-serif; }
.text-danger { color: #ef4444 !important; }
.text-warning { color: #f59e0b !important; }
.text-success { color: #10b981 !important; }

/* 顶部居中：胶囊信标 */
.hud-top-center {
  position: absolute;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  pointer-events: auto;
  z-index: 20;
  width: auto;
  max-width: 60%;
  justify-content: center;
  flex-wrap: nowrap; /* 保持单行，避免向下挤压 */
}

.mock-data-tip {
  width: 100%;
  text-align: center;
  font-size: 12px;
  color: #a16207;
  background: rgba(254, 243, 199, 0.95);
  border: 1px solid rgba(217, 119, 6, 0.35);
  border-radius: 12px;
  padding: 6px 12px;
  line-height: 1.2;
}
.hud-pill {
  display: flex; align-items: center; gap: 8px; background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(8px);
  border: 1px solid rgba(0, 0, 0, 0.05); border-radius: 30px; padding: 4px 14px 4px 6px; box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  cursor: pointer; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.hud-pill:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); }
/* 激活态和失去焦点态 */
.hud-pill.is-active { transform: scale(1.08); background: #ffffff; border-color: rgba(0,0,0,0.1); box-shadow: 0 10px 25px rgba(0,0,0,0.15); z-index: 2; }
.hud-pill.is-inactive { opacity: 0.4; transform: scale(0.95); }

.pill-ring { width: 12px; height: 12px; border-radius: 50%; background: #94a3b8; transition: all 0.3s; }
.pill-label { font-size: 12px; color: #64748b; white-space: nowrap; transition: color 0.3s; }
.pill-value { font-size: 20px; font-weight: bold; color: #0f172a; transition: color 0.3s; }

.pill--step .pill-ring { background: #0ea5e9; box-shadow: 0 0 8px rgba(14,165,233,0.5); }
.pill--fence .pill-ring { background: #f59e0b; box-shadow: 0 0 8px rgba(245,158,11,0.5); }
.pill--sos { border-color: rgba(239, 68, 68, 0.3); }
.pill--sos .pill-ring { background: #ef4444; box-shadow: 0 0 8px rgba(239,68,68,0.5); animation: blink 1s infinite; }
.pill--sos .pill-value, .pill--sos.is-active .pill-label { color: #ef4444; }
.pill--temperature .pill-ring { background: #06b6d4; box-shadow: 0 0 8px rgba(6,182,212,0.5); }
.pill--heart .pill-ring { background: #ec4899; box-shadow: 0 0 8px rgba(236,72,153,0.5); }
.pill--oxygen .pill-ring { background: #10b981; box-shadow: 0 0 8px rgba(16,185,129,0.5); }
.pill--pressure .pill-ring { background: #8b5cf6; box-shadow: 0 0 8px rgba(139,92,246,0.5); }

/* ================== 3.5 波动时序追踪大盘 (New Trend Panel) ================== */
.trend-overlay-panel {
  pointer-events: auto; top: 85px; left: 50%; transform: translateX(-50%); width: 700px; height: 320px;
  background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(20px); border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5); box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  display: flex; flex-direction: column; padding: 20px; z-index: 15;
}
.trend-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.trend-title {
  font-size: 16px; font-weight: bold; color: #1e293b; display: flex; align-items: center; gap: 8px;
  .dot { width: 8px; height: 8px; border-radius: 50%; box-shadow: 0 0 8px currentColor; }
  span { font-size: 11px; font-weight: normal; color: #94a3b8; font-family: monospace; }
}
.close-btn { font-size: 20px; color: #94a3b8; cursor: pointer; transition: color 0.3s; }
.close-btn:hover { color: #ef4444; }
.trend-chart-container { flex: 1; width: 100%; }

/* 面板掉落动画 */
.panel-drop-enter-active, .panel-drop-leave-active { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.panel-drop-enter-from, .panel-drop-leave-to { opacity: 0; transform: translate(-50%, -20px) scale(0.95); }

/* 快响应侧边栏过渡 */
.panel-fade-enter-active, .panel-fade-leave-active { transition: opacity 0.15s ease-out; }
.panel-fade-enter-from, .panel-fade-leave-to { opacity: 0; }

/* 图表/表格通用样式 */
.kpi-pie-slim { width: 100%; height: 180px; }
.holo-table {
  background: transparent !important; --el-table-bg-color: transparent; --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent; --el-table-border-color: rgba(0, 0, 0, 0.04);
  --el-table-row-hover-bg-color: rgba(0, 0, 0, 0.02); --el-table-text-color: #475569; --el-table-header-text-color: #64748b;
}
.holo-table :deep(.el-table__cell) { padding: 4px 0 !important; font-size: 11px; }
:deep(.el-table th.el-table__cell) { border-bottom: 1px solid rgba(0, 0, 0, 0.08) !important; font-weight: 600; }
:deep(.el-table td.el-table__cell) { border-bottom: 1px dashed rgba(0, 0, 0, 0.04) !important; }
:deep(.el-table::before) { display: none; }
.clickable-table :deep(.el-table__row) { cursor: pointer; }

.status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 6px; }
.dot-ok { background: #10b981; } .dot-warn { background: #f59e0b; }
.status-text { font-family: 'Share Tech Mono', monospace; font-size: 10px; font-weight: bold; }
.text-ok { color: #10b981; } .text-warn { color: #f59e0b; }
.tech-highlight { color: #ef4444; font-family: 'Share Tech Mono', monospace; font-weight: bold; }

/* ================== 4. 地图组件 ================== */
.map-failed-fallback {
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  background: radial-gradient(circle at center, #f8fafc 0%, #e2e8f0 100%); z-index: 2; pointer-events: auto;
  .map-icon { font-size: 40px; color: #ef4444; margin-bottom: 16px; animation: blink 1.5s infinite; }
  h2 { color: #ef4444; font-family: 'Share Tech Mono', monospace; letter-spacing: 2px; margin:0 0 8px; font-size: 18px; }
  p { color: #64748b; font-size: 12px; margin: 0; }
}
.radar-scan {
  position: absolute; top: 50%; left: 50%; width: 60vmin; height: 60vmin; transform: translate(-50%, -50%);
  background: conic-gradient(from 0deg, transparent 70%, rgba(14, 165, 233, 0.1) 100%);
  animation: radar-spin 3s linear infinite; border-radius: 50%; border: 1px dashed rgba(14, 165, 233, 0.15);
}

:deep(.holo-marker) {
  position: relative; width: 16px; height: 16px; display: flex; justify-content: center; align-items: center; cursor: pointer;
  .core { width: 6px; height: 6px; border-radius: 50%; position: relative; z-index: 2; box-shadow: 0 0 6px currentColor; }
  .ripple { position: absolute; top: 50%; left: 50%; width: 100%; height: 100%; transform: translate(-50%, -50%); border-radius: 50%; border: 1px solid currentColor; opacity: 0; animation: radar-pulse 1.5s ease-out infinite; z-index: 1; }
}
:deep(.pulse-danger) { color: #ef4444; .core { background: #ef4444; } }
:deep(.pulse-warning) { color: #f59e0b; .core { background: #f59e0b; } }
:deep(.pulse-resolved) { color: #10b981; .core { background: #10b981; } .ripple { animation-duration: 2.4s; opacity: 0.45; } }

:deep(.holo-info-window) {
  background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(8px);
  border-left: 3px solid; padding: 12px 16px; width: 220px; color: #334155; 
  box-shadow: 0 10px 25px rgba(0,0,0,0.1); border-radius: 0 4px 4px 0;
  .info-header { margin-bottom: 8px; }
  .info-tag { font-family: 'Share Tech Mono', monospace; font-size: 10px; margin-bottom: 4px; display: block; }
  .info-title { margin: 0; font-size: 14px; font-weight: bold; }
  .info-body p { margin: 0 0 4px; font-size: 11px; color: #64748b; }
  .hl { color: #0f172a; font-weight: bold; }
}
:deep(.holo-info-window.warning) { border-color: #f59e0b; .info-tag, .info-title { color: #f59e0b; } }
:deep(.holo-info-window.critical) { border-color: #ef4444; .info-tag, .info-title { color: #ef4444; } }

@keyframes radar-spin { 100% { transform: translate(-50%, -50%) rotate(360deg); } }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
@keyframes radar-pulse { 0% { width: 6px; height: 6px; opacity: 1; border-width: 2px; } 100% { width: 40px; height: 40px; opacity: 0; border-width: 1px; } }

@media (max-width: 1199px) {
  .page-shell { overflow: auto; height: auto; display: flex; flex-direction: column; background: #f8fafc; }
  .amap-fullscreen-bg { position: relative; height: 400px; order: 1; z-index: 1; }
  .hud-value-cards { display: flex; flex-direction: row; flex-wrap: wrap; justify-content: center; margin-bottom: 24px; }
  
  .hud-top-center { position: relative; inset: auto; transform: none; order: 3; margin: 0 auto 16px; width: 100%; max-width: none; flex-wrap: wrap; }
  
  .trend-overlay-panel { position: relative; inset: auto; transform: none; width: 100%; max-width: 700px; margin: 0 auto 16px; order: 4; }
  .hud-side-panel, .hud-bottom-center { position: relative; inset: auto; width: 100%; max-width: none; transform: none; padding: 16px; background: none !important; pointer-events: auto; }
  .hud-side-panel { order: 5; gap: 16px; }
  .hud-bottom-center { order: 6; height: auto; }
  .table-wrapper { height: 250px; } 
}
// ---- AI LOG FEED ----
.ai-logs-block {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.ai-log-feed {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 2px 0;
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }
}

// 空态
.ai-log-nominal {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px 0;
}

.nominal-pulse {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.6);
  animation: nominal-beat 2.4s ease-in-out infinite;
}

@keyframes nominal-beat {
  0%, 100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.6); }
  50% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
}

.nominal-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nominal-label {
  font-family: 'Share Tech Mono', monospace;
  font-size: 11px;
  color: #10b981;
  letter-spacing: 0.05em;
}

.nominal-sub {
  font-size: 10px;
  color: #64748b;
}

// 条目
.ai-log-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 7px 10px;
  border-radius: 6px;
  background: rgba(255,255,255,0.7);
  border: 1px solid rgba(0,0,0,0.06);
  transition: background 0.15s;
  cursor: pointer;

  &:hover {
    background: rgba(255,255,255,0.95);
  }

  // 风险色边框
  &.risk-critical { border-left: 2px solid #ef4444; }
  &.risk-high     { border-left: 2px solid #f97316; }
  &.risk-medium   { border-left: 2px solid #f59e0b; }
  &.risk-low      { border-left: 2px solid #10b981; }
}

.ali-indicator {
  display: flex;
  align-items: center;
  padding-top: 3px;
  flex-shrink: 0;
}

.ali-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;

  .risk-critical & { background: #ef4444; box-shadow: 0 0 5px #ef4444; }
  .risk-high &     { background: #f97316; box-shadow: 0 0 5px #f97316; }
  .risk-medium &   { background: #f59e0b; box-shadow: 0 0 5px #f59e0b; }
  .risk-low &      { background: #10b981; box-shadow: 0 0 5px #10b981; }
}

.ali-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ali-top {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.ali-name {
  font-size: 12px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  flex-shrink: 0;
  cursor: pointer;

  &:hover {
    color: #0ea5e9;
  }
}

.ali-type {
  font-size: 11px;
  color: #475569;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  .risk-critical & { color: #dc2626; }
  .risk-high &     { color: #ea580c; }
  .risk-medium &   { color: #d97706; }
  .risk-low &      { color: #059669; }
}

.ali-bottom {
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
}

.ali-risk-badge {
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 3px;
  letter-spacing: 0.04em;
  text-transform: uppercase;

  &.badge-critical { background: rgba(239,68,68,0.12); color: #dc2626; }
  &.badge-high     { background: rgba(249,115,22,0.12); color: #c2410c; }
  &.badge-medium   { background: rgba(245,158,11,0.12); color: #b45309; }
  &.badge-low      { background: rgba(16,185,129,0.12); color: #047857; }
}

.ali-conf {
  font-size: 9px;
  color: #64748b;
  font-family: 'Share Tech Mono', monospace;
}

.ali-time {
  font-size: 9px;
  color: #94a3b8;
  margin-left: auto;
}

/* ================== 底部状态横条 ================== */
.status-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  height: 52px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: 14px;
  padding: 0 20px;
  box-shadow: 0 -2px 16px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.04);
  pointer-events: auto;
}

.sb-stats {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.sb-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 12px;
  gap: 2px;
}

.sb-label {
  font-family: 'Share Tech Mono', monospace;
  font-size: 9px;
  color: #94a3b8;
  letter-spacing: 0.08em;
  white-space: nowrap;
}

.sb-value {
  font-size: 18px;
  font-weight: bold;
  color: #0ea5e9;
  line-height: 1;
}

.sb-divider {
  width: 1px;
  height: 28px;
  background: rgba(14, 165, 233, 0.15);
  flex-shrink: 0;
}

.sb-live {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding: 0 12px;
  border-left: 1px solid rgba(14, 165, 233, 0.15);
  border-right: 1px solid rgba(14, 165, 233, 0.15);
}

.live-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 6px #10b981;
  animation: blink 1.2s ease-in-out infinite;
  flex-shrink: 0;
}

.live-label {
  font-family: 'Share Tech Mono', monospace;
  font-size: 10px;
  color: #10b981;
  letter-spacing: 0.1em;
  white-space: nowrap;
}

.sb-ticker-wrap {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
}

.sb-ticker {
  display: flex;
  align-items: center;
  gap: 0;
  animation: ticker-scroll 18s linear infinite;
  white-space: nowrap;
}

.sb-ticker:hover {
  animation-play-state: paused;
}

.ticker-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover {
    background: rgba(14, 165, 233, 0.08);
  }
}

.ticker-name {
  font-family: 'Share Tech Mono', monospace;
  font-size: 12px;
  color: #334155;
  font-weight: 600;
}

.ticker-type {
  font-size: 11px;
  color: #64748b;
}

.ticker-status {
  font-family: 'Share Tech Mono', monospace;
  font-size: 11px;
  font-weight: bold;

  .ticker-warn & { color: #f59e0b; }
  .ticker-ok & { color: #10b981; }
}

.ticker-sep {
  color: #cbd5e1;
  margin: 0 8px;
  font-size: 14px;
}

.sb-ticker-empty {
  font-size: 11px;
  color: #94a3b8;
  letter-spacing: 0.1em;
}

@keyframes ticker-scroll {
  0%   { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
</style>
