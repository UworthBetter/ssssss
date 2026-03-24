<template>
  <PlatformPageShellV2
    title="模型日志"
    subtitle="读取真实 AI 异常检测结果与异常事件，展示最近研判日志、置信度和对象关联信息。"
    eyebrow="AI MODEL LOGS"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索对象 / 事件 ID" prefix-icon="Search" clearable style="width: 240px" />
        <el-select v-model="query.result" placeholder="判定结果" clearable style="width: 140px">
          <el-option label="异常告警" value="alert" />
          <el-option label="风险预警" value="warning" />
          <el-option label="状态正常" value="normal" />
        </el-select>
        <el-select v-model="query.model" placeholder="模型名称" clearable style="width: 180px">
          <el-option v-for="option in modelOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-date-picker
          v-model="query.date"
          type="daterange"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
          style="width: 240px"
        />
        <el-button type="primary" @click="page = 1">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </template>

    <div class="stat-row">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="sc-icon" :style="{ background: card.bg }">
          <el-icon :size="18"><component :is="card.icon" /></el-icon>
        </div>
        <div class="sc-body">
          <span class="sc-value" :style="{ color: card.color }">{{ card.value }}</span>
          <span class="sc-label">{{ card.label }}</span>
        </div>
      </div>
    </div>

    <div class="panel-card">
      <div class="panel-header">
        <span class="panel-title">推理日志流</span>
        <div class="header-right">
          <span class="live-indicator"><span class="live-dot"></span>LIVE</span>
          <el-button size="small" :icon="Download" plain @click="handleExport" :disabled="!filteredLogs.length">导出日志</el-button>
        </div>
      </div>

      <div v-loading="loading" class="log-stream">
        <div v-for="log in pagedLogs" :key="log.id" class="log-entry" :class="log.result" @click="handleLogClick(log)">
          <div class="log-timeline">
            <div class="tl-dot" :class="log.result"></div>
            <div class="tl-line"></div>
          </div>

          <div class="log-content">
            <div class="log-header">
              <span class="log-id">#{{ String(log.id).padStart(6, '0') }}</span>
              <div class="log-result-badge" :class="log.result">{{ resultLabel[log.result] }}</div>
              <span class="log-model-tag">{{ log.model }}</span>
              <span class="log-time">{{ log.time }}</span>
            </div>
            <div class="log-subject" @click.stop="handleSubjectClick(log)">
              <el-icon :size="12"><User /></el-icon>
              {{ log.subject }}
              <span class="log-device">· 设备 {{ log.device }}</span>
            </div>
            <div class="log-detail">{{ log.detail }}</div>
            <div class="log-metrics">
              <div class="metric-pill" v-for="metric in log.metrics" :key="metric.key">
                <span class="mp-key">{{ metric.key }}</span>
                <span class="mp-val" :class="{ warn: metric.warn }">{{ metric.val }}</span>
              </div>
              <div class="confidence-bar">
                <span class="cb-label">置信度</span>
                <div class="cb-bg">
                  <div class="cb-fill" :style="{ width: `${log.confidence}%`, background: confidenceColor(log.confidence) }"></div>
                </div>
                <span class="cb-val">{{ log.confidence }}%</span>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && !pagedLogs.length" description="暂无日志数据" />
      </div>

      <div class="log-pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="filteredLogs.length"
          background
          layout="total, prev, pager, next"
        />
      </div>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Download, User } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'
import { getRecentAbnormal, type AbnormalTrendPoint } from '@/api/ai'
import { listDevices, listExceptions, listSubjects, type DeviceInfo, type ExceptionAlert, type HealthSubject } from '@/api/health'

type LogResult = 'alert' | 'warning' | 'normal'

interface RecentAbnormalItem {
  eventId?: number | string
  userId?: number | string
  patientName?: string
  abnormalType?: string
  abnormalValue?: string
  confidence?: number | string
  riskLevel?: string
  source?: string
  location?: string
  createTime?: string
  readTime?: string
  detectedTime?: string
}

interface LogMetric {
  key: string
  val: string
  warn?: boolean
}

interface AiLogRecord {
  id: number | string
  userId?: number | string
  patientName?: string
  abnormalType?: string
  result: LogResult
  model: string
  subject: string
  device: string
  time: string
  detail: string
  metrics: LogMetric[]
  confidence: number
}

const page = ref(1)
const pageSize = 10
const loading = ref(false)
const logs = ref<AiLogRecord[]>([])
const router = useRouter()

const query = reactive({
  keyword: '',
  result: '' as '' | LogResult,
  model: '',
  date: null as [Date, Date] | null
})

const resultLabel: Record<LogResult, string> = { alert: '异常告警', warning: '风险预警', normal: '状态正常' }
const confidenceColor = (value: number) => (value >= 85 ? '#10b981' : value >= 65 ? '#f59e0b' : '#ef4444')

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

const parseNumber = (value: unknown, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const parseTimestamp = (value?: string) => {
  if (!value) return 0
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

const formatDateTime = (value?: string) => {
  const timestamp = parseTimestamp(value)
  if (!timestamp) return '--'
  const date = new Date(timestamp)
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  const second = `${date.getSeconds()}`.padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}:${second}`
}

const resolveResult = (riskLevel?: string): LogResult => {
  const text = String(riskLevel || '').toLowerCase()
  if (['critical', 'high', 'danger', '严重', '高'].some((keyword) => text.includes(keyword))) return 'alert'
  if (['medium', 'warning', 'warn', '中'].some((keyword) => text.includes(keyword))) return 'warning'
  return 'normal'
}

const resolveModel = (abnormalType?: string) => {
  const text = String(abnormalType || '').toLowerCase()
  if (text.includes('心率')) return { key: 'hr', label: '心率异常检测' }
  if (text.includes('血氧')) return { key: 'spo2', label: '血氧异常检测' }
  if (text.includes('血压')) return { key: 'blood', label: '血压异常检测' }
  if (text.includes('体温')) return { key: 'temp', label: '体温异常检测' }
  if (text.includes('围栏')) return { key: 'fence', label: '围栏越界识别' }
  if (text.includes('sos') || text.includes('求救')) return { key: 'sos', label: '紧急求助识别' }
  if (text.includes('离线') || text.includes('信号')) return { key: 'device', label: '设备状态评估' }
  if (text.includes('活动') || text.includes('步数')) return { key: 'step', label: '活动量分析' }
  return { key: 'risk', label: '综合风险评估' }
}

const buildLogDetail = (item: RecentAbnormalItem, exception?: ExceptionAlert) => {
  const detailParts = [
    item.abnormalType ? `触发类型 ${item.abnormalType}` : '',
    item.abnormalValue ? `指标值 ${item.abnormalValue}` : '',
    exception?.location ? `位置 ${exception.location}` : item.location ? `位置 ${item.location}` : ''
  ].filter(Boolean)

  return detailParts.join('，') || '后端已返回告警事件，等待进一步处置。'
}

const modelOptions = computed(() => {
  const unique = new Map<string, string>()
  logs.value.forEach((item) => {
    const model = resolveModel(item.model)
    unique.set(model.key, model.label)
  })
  return Array.from(unique.entries()).map(([value, label]) => ({ value, label }))
})

const filteredLogs = computed(() => {
  const keyword = query.keyword.trim()
  const range = query.date
  return logs.value.filter((item) => {
    if (query.result && item.result !== query.result) return false
    if (query.model) {
      const model = resolveModel(item.model)
      if (model.key !== query.model) return false
    }
    if (keyword && ![item.subject, item.device, String(item.id)].some((field) => field.includes(keyword))) {
      return false
    }
    if (range?.length === 2) {
      const timestamp = parseTimestamp(item.time.replace(/^(\d{2})-(\d{2})/, `${new Date().getFullYear()}-$1-$2`))
      const start = new Date(range[0]); start.setHours(0, 0, 0, 0)
      const end = new Date(range[1]); end.setHours(23, 59, 59, 999)
      if (timestamp < start.getTime() || timestamp > end.getTime()) return false
    }
    return true
  })
})

const pagedLogs = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredLogs.value.slice(start, start + pageSize)
})

const statCards = computed(() => {
  const total = logs.value.length
  const alertCount = logs.value.filter((item) => item.result === 'alert').length
  const warningCount = logs.value.filter((item) => item.result === 'warning').length
  const averageConfidence = total
    ? `${(logs.value.reduce((sum, item) => sum + item.confidence, 0) / total).toFixed(1)}%`
    : '--'

  return [
    { label: '今日推理数', value: total, color: '#3b82f6', bg: 'linear-gradient(135deg,#eff6ff,#dbeafe)', icon: 'DataLine' },
    { label: '异常告警', value: alertCount, color: '#ef4444', bg: 'linear-gradient(135deg,#fef2f2,#fecaca)', icon: 'Bell' },
    { label: '风险预警', value: warningCount, color: '#f59e0b', bg: 'linear-gradient(135deg,#fffbeb,#fde68a)', icon: 'Warning' },
    { label: '平均置信度', value: averageConfidence, color: '#10b981', bg: 'linear-gradient(135deg,#f0fdf4,#bbf7d0)', icon: 'TrendCharts' }
  ]
})

const fetchLogs = async () => {
  loading.value = true
  try {
    const [recentRes, exceptionRes, deviceRes, subjectRes] = await Promise.all([
      getRecentAbnormal(100),
      listExceptions({ pageNum: 1, pageSize: 200 }),
      listDevices({ pageNum: 1, pageSize: 200 }),
      listSubjects({ pageNum: 1, pageSize: 200 })
    ])

    const recentRows = (recentRes.data || []) as RecentAbnormalItem[]
    const exceptionRows = (exceptionRes.rows || []) as ExceptionAlert[]
    const devices = (deviceRes.rows || []) as DeviceInfo[]
    const subjects = (subjectRes.rows || []) as HealthSubject[]

    const deviceMap = new Map<number, DeviceInfo>()
    devices.forEach((item) => {
      if (item.userId != null) {
        deviceMap.set(Number(item.userId), item)
      }
    })

    const subjectMap = new Map<number, HealthSubject>()
    subjects.forEach((item) => {
      const id = Number(item.subjectId || 0)
      if (id) subjectMap.set(id, item)
    })

    const exceptionMap = new Map<string, ExceptionAlert>()
    exceptionRows.forEach((item) => {
      const key = `${item.userId || ''}|${item.type || ''}`
      if (!exceptionMap.has(key)) {
        exceptionMap.set(key, item)
      }
    })

    logs.value = recentRows.map((item, index) => {
      const userId = Number(item.userId || 0)
      const model = resolveModel(item.abnormalType)
      const exception = exceptionMap.get(`${item.userId || ''}|${item.abnormalType || ''}`)
      const device = deviceMap.get(userId)
      const subject = subjectMap.get(userId)
      const confidence = Math.min(99, Math.max(55, Math.round(parseNumber(item.confidence, resolveResult(item.riskLevel) === 'alert' ? 92 : 76))))
      const exceptionName = (exception as (ExceptionAlert & { nickName?: string }) | undefined)?.nickName
      const displayName = !isPlaceholderPatientName(item.patientName)
        ? String(item.patientName)
        : subject?.nickName || subject?.subjectName || exceptionName || `对象 ${item.userId || '--'}`

      return {
        id: item.eventId || exception?.id || 100000 + index,
        userId: item.userId,
        patientName: displayName,
        abnormalType: item.abnormalType,
        result: resolveResult(item.riskLevel),
        model: model.label,
        subject: displayName,
        device: device?.name || device?.imei || '未关联设备',
        time: formatDateTime(item.detectedTime || item.createTime || item.readTime || exception?.createTime),
        detail: buildLogDetail(item, exception),
        metrics: [
          { key: '类型', val: item.abnormalType || '异常', warn: resolveResult(item.riskLevel) !== 'normal' },
          { key: '指标值', val: item.abnormalValue || '--', warn: resolveResult(item.riskLevel) === 'alert' },
          { key: '位置', val: exception?.location || item.location || '--' }
        ],
        confidence
      } as AiLogRecord
    })
  } finally {
    loading.value = false
  }
}

const resolveSubjectKeyword = (log: Pick<AiLogRecord, 'patientName' | 'userId'>) => {
  if (!isPlaceholderPatientName(log.patientName)) {
    return String(log.patientName ?? '').trim()
  }
  if (log.userId != null && String(log.userId).trim() !== '') {
    return String(log.userId)
  }
  return String(log.patientName ?? '').trim()
}

const handleSubjectClick = (log: Pick<AiLogRecord, 'patientName' | 'userId'>) => {
  const keyword = resolveSubjectKeyword(log)
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

const handleLogClick = (log: Pick<AiLogRecord, 'userId' | 'abnormalType' | 'patientName'>) => {
  const query: Record<string, string> = {}
  if (log.userId != null && String(log.userId).trim() !== '') {
    query.userId = String(log.userId)
  }
  if (log.abnormalType && String(log.abnormalType).trim() !== '') {
    query.type = normalizeEventTypeQuery(log.abnormalType)
  }

  if (Object.keys(query).length > 0) {
    void router.push({ path: '/event', query })
    return
  }

  handleSubjectClick(log)
}

const handleReset = () => {
  query.keyword = ''
  query.result = ''
  query.model = ''
  query.date = null
  page.value = 1
}

const handleExport = () => {
  const blob = new Blob([JSON.stringify(filteredLogs.value, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `ai-logs-${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped lang="scss">
.toolbar-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.sc-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  flex-shrink: 0;
}

.sc-body { display: flex; flex-direction: column; gap: 2px; }
.sc-value { font-size: 24px; font-weight: 800; line-height: 1.1; }
.sc-label { font-size: 12px; color: #64748b; }

.panel-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.panel-title { font-size: 14px; font-weight: 700; color: #0f172a; }
.header-right { display: flex; align-items: center; gap: 12px; }

.live-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  color: #10b981;
  letter-spacing: 0.05em;
}

.live-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10b981;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%,100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.log-stream {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 0;
  min-height: 360px;
}

.log-entry {
  display: flex;
  gap: 0;
  position: relative;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.log-entry:hover { background: #f8fafc; }

.log-entry:last-child .tl-line { display: none; }

.log-timeline {
  flex: 0 0 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tl-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
}

.tl-dot.alert { background: #ef4444; box-shadow: 0 0 6px rgba(239,68,68,0.4); }
.tl-dot.warning { background: #f59e0b; }
.tl-dot.normal { background: #10b981; }

.tl-line { flex: 1; width: 1px; background: #f1f5f9; min-height: 16px; }

.log-content {
  flex: 1;
  padding: 0 0 20px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-header { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.log-id { font-family: monospace; font-size: 12px; color: #94a3b8; }

.log-result-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.log-result-badge.alert { color: #b91c1c; background: #fef2f2; }
.log-result-badge.warning { color: #b45309; background: #fffbeb; }
.log-result-badge.normal { color: #047857; background: #ecfdf5; }

.log-model-tag {
  font-size: 12px;
  color: #7c3aed;
  background: #f3e8ff;
  padding: 2px 8px;
  border-radius: 6px;
}

.log-time { font-size: 12px; color: #94a3b8; margin-left: auto; }

.log-subject {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  width: fit-content;
}

.log-subject:hover { color: #2563eb; }

.log-device { font-weight: 400; color: #94a3b8; }
.log-detail { font-size: 13px; color: #334155; line-height: 1.6; }

.log-metrics {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.metric-pill {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
}

.mp-key { color: #64748b; }
.mp-val { font-weight: 600; color: #0f172a; }
.mp-val.warn { color: #ef4444; }

.confidence-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.cb-label { color: #94a3b8; flex-shrink: 0; }
.cb-bg { width: 80px; height: 4px; background: #f1f5f9; border-radius: 2px; }
.cb-fill { height: 4px; border-radius: 2px; transition: width 0.4s; }
.cb-val { font-weight: 600; color: #475569; }

.log-pagination {
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1024px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
