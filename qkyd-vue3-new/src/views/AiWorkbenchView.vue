<template>
  <div :class="['ai-report-center', `theme-${settingsForm.theme}`]">
    <!-- 顶栏 -->
    <header class="report-topbar">
      <div class="topbar-left">
        <span class="topbar-eyebrow">AI REPORT</span>
        <h2 class="topbar-title">AI 分析报告生成器</h2>
        <p class="topbar-sub">基于真实监控数据，一键生成结构化分析报告</p>
      </div>
      <div class="topbar-right">
        <div class="theme-switcher">
          <button
            v-for="t in themes"
            :key="t.value"
            :class="['theme-dot', t.value, { active: settingsForm.theme === t.value }]"
            :title="t.label"
            @click="settingsForm.theme = t.value"
          />
        </div>
        <el-button type="primary" @click="showChatDrawer = true">
          <el-icon><ChatDotRound /></el-icon>
          问 AI
        </el-button>
        <el-button circle plain @click="showSettingsDrawer = true">
          <el-icon><Setting /></el-icon>
        </el-button>
      </div>
    </header>

    <!-- 主体左右布局 -->
    <div class="report-body">
      <!-- 左侧控制面板 -->
      <aside class="control-panel">
        <div class="panel-section">
          <div class="panel-label">报告类型</div>
          <div class="report-type-list">
            <button
              v-for="rt in reportTypes"
              :key="rt.value"
              :class="['type-btn', { active: selectedReportType === rt.value }]"
              @click="selectedReportType = rt.value"
            >
              <span class="type-btn-title">{{ rt.label }}</span>
              <span class="type-btn-desc">{{ rt.desc }}</span>
            </button>
          </div>
        </div>

        <div class="panel-section">
          <div class="panel-label">统计周期</div>
          <div class="period-tabs">
            <button
              v-for="p in periods"
              :key="p.value"
              :class="['period-tab', { active: selectedPeriod === p.value }]"
              @click="selectedPeriod = p.value"
            >
              {{ p.label }}
            </button>
          </div>
        </div>

        <div class="panel-section">
          <div class="panel-label">对象范围</div>
          <div class="scope-tabs">
            <button
              v-for="s in scopeOptions"
              :key="s.value"
              :class="['scope-tab', { active: selectedScope === s.value }]"
              @click="selectedScope = s.value"
            >
              {{ s.label }}
            </button>
          </div>
        </div>

        <el-button
          type="primary"
          size="large"
          class="generate-btn"
          :loading="generating"
          @click="generateReport"
        >
          <el-icon v-if="!generating"><Document /></el-icon>
          {{ generating ? '生成中...' : '生成报告' }}
        </el-button>

        <div v-if="reportData" class="panel-section export-section">
          <div class="panel-label">导出</div>
          <div class="export-btns">
            <el-button plain size="small" style="flex:1" @click="exportToWord">
              <el-icon><Download /></el-icon> Word
            </el-button>
            <el-button plain size="small" style="flex:1" @click="exportToCsv">
              <el-icon><Download /></el-icon> CSV
            </el-button>
          </div>
        </div>
      </aside>

      <!-- 右侧报告预览区 -->
      <main class="report-preview">
        <!-- 空状态 -->
        <div v-if="!reportData && !generating" class="empty-state">
          <el-icon class="empty-icon" size="64"><DataAnalysis /></el-icon>
          <h3>选择报告类型并点击「生成报告」</h3>
          <p>系统将自动拉取真实监护数据，生成结构化分析报告，并由 AI 提供解读与建议。</p>
          <div class="empty-tips">
            <div v-for="rt in reportTypes" :key="rt.value" class="empty-tip-item">
              <strong>{{ rt.label }}</strong>
              <span>{{ rt.desc }}</span>
            </div>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="generating" class="generating-state">
          <div class="gen-ring" />
          <p>正在拉取数据并生成报告...</p>
        </div>

        <!-- 报告内容 -->
        <div v-if="reportData" class="report-content">
          <div class="report-header-row">
            <div>
              <div class="report-eyebrow">{{ reportData.period }} · {{ reportData.generatedAt }} 生成</div>
              <h2 class="report-title">{{ reportTypes.find(t => t.value === reportData!.reportType)?.label }}</h2>
              <p v-if="reportData.reportSubtitle" class="report-subtitle">{{ reportData.reportSubtitle }}</p>
            </div>
            <el-tag type="info" effect="plain">{{ scopeOptions.find(s => s.value === selectedScope)?.label }}</el-tag>
          </div>

          <div v-if="reportData.showSections.kpi" class="kpi-row">
            <div class="kpi-card">
              <div class="kpi-value">{{ reportData.totalSubjects }}</div>
              <div class="kpi-label">监护对象</div>
            </div>
            <div class="kpi-card danger">
              <div class="kpi-value">{{ reportData.highRiskCount }}</div>
              <div class="kpi-label">高风险对象</div>
            </div>
            <div class="kpi-card warning">
              <div class="kpi-value">{{ reportData.totalEvents }}</div>
              <div class="kpi-label">异常事件</div>
            </div>
            <div class="kpi-card success">
              <div class="kpi-value">{{ reportData.resolvedEvents }}</div>
              <div class="kpi-label">已处置</div>
            </div>
          </div>

          <div v-if="reportData.showSections.abnormalDistribution && reportData.abnormalTypeDistribution.length" class="report-section">
            <div class="section-title">异常类型分布</div>
            <div class="abnormal-distribution">
              <div v-for="item in reportData.abnormalTypeDistribution" :key="item.type" class="dist-item">
                <div class="dist-label">{{ item.type }}</div>
                <div class="dist-bar-wrapper">
                  <div class="dist-bar" :style="{ width: `${Math.min(100, (item.count / Math.max(...reportData.abnormalTypeDistribution.map(d => d.count))) * 100)}%` }" />
                </div>
                <div class="dist-count">{{ item.count }}次</div>
              </div>
            </div>
          </div>

          <div v-if="reportData.showSections.trend" class="report-section">
            <div class="section-title">异常事件趋势</div>
            <div id="reportTrendChart" class="trend-chart" />
          </div>

          <div v-if="reportData.showSections.vitalSignStats && reportData.vitalSignStats" class="report-section">
            <div class="section-title">体征统计</div>
            <div class="vital-signs-grid">
              <div class="vs-card">
                <div class="vs-icon">&#10084;&#65039;</div>
                <div class="vs-content">
                  <div class="vs-value">{{ reportData.vitalSignStats.heartRateAvg }}</div>
                  <div class="vs-label">心率均值(次/分)</div>
                </div>
              </div>
              <div class="vs-card">
                <div class="vs-icon">&#128147;</div>
                <div class="vs-content">
                  <div class="vs-value">{{ reportData.vitalSignStats.spo2Avg }}</div>
                  <div class="vs-label">血氧均值(%)</div>
                </div>
              </div>
              <div class="vs-card">
                <div class="vs-icon">&#127777;&#65039;</div>
                <div class="vs-content">
                  <div class="vs-value">{{ reportData.vitalSignStats.tempAvg }}</div>
                  <div class="vs-label">体温均值(℃)</div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="reportData.showSections.highRiskTable" class="report-section">
            <div class="section-title">高风险对象明细</div>
            <el-table :data="reportData.highRiskSubjects" size="small" stripe style="width:100%">
              <el-table-column prop="name" label="姓名" width="90" />
              <el-table-column prop="age" label="年龄" width="70" />
              <el-table-column prop="riskLevel" label="风险等级" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.riskLevel === 'high' ? 'danger' : 'warning'" size="small" effect="light">
                    {{ row.riskLevel }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="eventCount" label="异常次数" width="80" />
              <el-table-column prop="lastAlarm" label="最近告警" />
            </el-table>
            <div v-if="!reportData.highRiskSubjects.length" class="no-data">暂无高风险对象数据</div>
          </div>

          <div class="report-section ai-interpretation">
            <div class="section-title"><el-icon><Monitor /></el-icon> AI 解读</div>
            <p class="interpretation-text">{{ reportData.aiInterpretation }}</p>
          </div>
        </div>
      </main>
    </div>

    <!-- 对话抽屉 -->
    <el-drawer v-model="showChatDrawer" title="AI 助手" direction="rtl" size="400px" class="chat-drawer">
      <div class="chat-drawer-body">
        <div ref="chatScroll" class="chat-viewport">
          <transition-group name="chat-flow" tag="div" class="chat-list">
            <div v-for="msg in messages" :key="msg.id" :class="['message-box', msg.role]">
              <div :class="['msg-avatar', msg.role]">
                <el-icon v-if="msg.role === 'ai'" size="16"><Monitor /></el-icon>
                <el-icon v-else size="16"><Avatar /></el-icon>
              </div>
              <div class="msg-bubble" v-html="msg.content" />
            </div>
          </transition-group>
          <div v-if="isTyping" class="typing-indicator">
            <span /><span /><span />
          </div>
        </div>
        <div class="chat-input-area">
          <div v-if="reportData" class="context-badge">
            <el-icon><InfoFilled /></el-icon> 已注入报告上下文
          </div>
          <div class="input-row">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              placeholder="输入问题，AI 将基于当前报告数据回复.."
              resize="none"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <el-button type="primary" :loading="isTyping" class="send-btn" @click="sendMessage">
              <el-icon><Position /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 设置抽屉 -->
    <el-drawer v-model="showSettingsDrawer" title="助理配置" direction="rtl" size="360px">
      <div class="drawer-settings-content">
        <el-form label-position="top">
          <el-form-item label="核心名称">
            <el-input v-model="settingsForm.botName" placeholder="命名您的引擎" clearable />
          </el-form-item>
          <el-form-item label="输出策略">
            <el-radio-group v-model="settingsForm.tone">
              <el-radio-button label="professional">专业严谨</el-radio-button>
              <el-radio-button label="lively">亲切易懂</el-radio-button>
              <el-radio-button label="concise">简洁图文</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="自动朗读">
            <el-switch v-model="settingsForm.autoSpeak" />
          </el-form-item>
        </el-form>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { Avatar, ChatDotRound, DataAnalysis, Document, Download, InfoFilled, Loading, Monitor, Position, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { chatAi, type AbnormalTrendPoint } from '@/api/ai'
import { listSubjects, listExceptions, listDeviceExtensions, type ExceptionAlert, type DeviceInfoExtend } from '@/api/health'

interface ChatMessage {
  id: number
  role: 'user' | 'ai'
  content: string
}

interface HighRiskSubject {
  name: string
  age: string
  riskLevel: string
  eventCount: number
  lastAlarm: string
}

interface ReportData {
  reportType: string
  period: string
  generatedAt: string
  totalSubjects: number
  highRiskCount: number
  totalEvents: number
  resolvedEvents: number
  highRiskSubjects: HighRiskSubject[]
  trendPoints: AbnormalTrendPoint[]
  aiInterpretation: string
  abnormalTypeDistribution: { type: string; count: number }[]
  vitalSignStats: { heartRateAvg: number; spo2Avg: number; tempAvg: number } | null
  showSections: {
    kpi: boolean
    trend: boolean
    highRiskTable: boolean
    abnormalDistribution: boolean
    vitalSignStats: boolean
  }
  reportSubtitle?: string
}

const themes = [
  { value: 'blue', label: '蓝色' },
  { value: 'purple', label: '紫色' },
  { value: 'green', label: '绿色' },
  { value: 'pink', label: '粉色' }
]

const reportTypes = [
  { value: 'operation', label: '运营概览报告', desc: '设备、对象、异常综合数据' },
  { value: 'health', label: '健康巡检报告', desc: '体征趋势与风险等级分布' },
  { value: 'highrisk', label: '高风险对象报告', desc: '高风险对象明细与建议' },
  { value: 'personal', label: '个人健康报告', desc: '单一对象完整健康档案' }
]

const periods = [
  { value: 'today', label: '今日', hours: 24 },
  { value: 'week', label: '近7日', hours: 168 },
  { value: 'month', label: '近30日', hours: 720 }
]

const scopeOptions = [
  { value: 'all', label: '全部对象' },
  { value: 'highrisk', label: '高风险' },
  { value: 'medium', label: '中风险' }
]

const settingsForm = ref({ botName: '辅助决策', tone: 'professional', autoSpeak: false, theme: 'blue' })
const selectedReportType = ref('operation')
const selectedPeriod = ref('week')
const selectedScope = ref('all')
const generating = ref(false)
const reportData = ref<ReportData | null>(null)
const showChatDrawer = ref(false)
const showSettingsDrawer = ref(false)
const messages = ref<ChatMessage[]>([{ id: 1, role: 'ai', content: '您好，我是 AI 助手。生成报告后，我可以为您解读数据并回答问题。' }])
const inputText = ref('')
const isTyping = ref(false)
const chatScroll = ref<HTMLElement | null>(null)
let trendChartInstance: echarts.ECharts | null = null

const getThemeColor = () => {
  const map: Record<string, string> = { blue: '#2563eb', purple: '#4f46e5', green: '#059669', pink: '#e11d48' }
  return map[settingsForm.value.theme] || '#2563eb'
}

const parseNumber = (v: unknown, fallback = 0) => { const n = Number(v); return Number.isFinite(n) ? n : fallback }

const extractListRows = <T = Record<string, unknown>>(payload: unknown): T[] => {
  if (Array.isArray(payload)) return payload as T[]
  if (payload && typeof payload === 'object') {
    const maybeRows = (payload as { rows?: unknown; data?: unknown }).rows
    if (Array.isArray(maybeRows)) return maybeRows as T[]
    const maybeData = (payload as { data?: unknown }).data
    if (Array.isArray(maybeData)) return maybeData as T[]
    if (maybeData && typeof maybeData === 'object' && Array.isArray((maybeData as { rows?: unknown }).rows)) {
      return (maybeData as { rows: T[] }).rows
    }
  }
  return []
}

const determineRiskLevel = (ext?: DeviceInfoExtend, events: ExceptionAlert[] = []) => {
  const unresolved = events.filter(e => String(e.state || '0') !== '1').length
  const battery = parseNumber(ext?.batteryLevel, 100)
  const hasAlarm = Boolean(String(ext?.alarmContent || '').trim())
  if (unresolved >= 2 || hasAlarm || battery <= 15) return 'high'
  if (unresolved >= 1 || battery <= 30) return 'medium'
  return 'low'
}

const periodHours = () => periods.find(p => p.value === selectedPeriod.value)?.hours || 168

const getSubjectId = (subject: Record<string, unknown>) => Number(subject.subjectId || subject.userId || 0)

const matchesSelectedScope = (riskLevel: string) => {
  if (selectedScope.value === 'highrisk') return riskLevel === 'high'
  if (selectedScope.value === 'medium') return riskLevel === 'medium'
  return true
}

const isResolvedEvent = (event: ExceptionAlert) => String(event.state) === '1'

const parseEventTime = (event: ExceptionAlert) => {
  const raw = String(event.createTime || event.updateTime || '').trim()
  if (!raw) return null
  const normalized = raw.includes('T') ? raw : raw.replace(' ', 'T')
  const time = new Date(normalized)
  return Number.isNaN(time.getTime()) ? null : time
}

const formatMonthDay = (date: Date) => `${date.getMonth() + 1}-${date.getDate()}`
const formatHour = (date: Date) => `${String(date.getHours()).padStart(2, '0')}:00`

const buildScopedTrendPoints = (events: ExceptionAlert[]): AbnormalTrendPoint[] => {
  const now = new Date()
  const buckets: { label: string; start: number; end: number; value: number }[] = []

  if (selectedPeriod.value === 'today') {
    for (let offset = 23; offset >= 0; offset--) {
      const start = new Date(now)
      start.setMinutes(0, 0, 0)
      start.setHours(now.getHours() - offset)
      const end = new Date(start)
      end.setHours(start.getHours() + 1)
      buckets.push({ label: formatHour(start), start: start.getTime(), end: end.getTime(), value: 0 })
    }
  } else {
    const days = selectedPeriod.value === 'month' ? 30 : 7
    for (let offset = days - 1; offset >= 0; offset--) {
      const start = new Date(now)
      start.setHours(0, 0, 0, 0)
      start.setDate(now.getDate() - offset)
      const end = new Date(start)
      end.setDate(start.getDate() + 1)
      buckets.push({ label: formatMonthDay(start), start: start.getTime(), end: end.getTime(), value: 0 })
    }
  }

  events.forEach((event) => {
    const time = parseEventTime(event)
    if (!time) return
    const bucket = buckets.find(item => time.getTime() >= item.start && time.getTime() < item.end)
    if (bucket) bucket.value += 1
  })

  return buckets.map(({ label, value }) => ({ label, value }))
}

const buildVitalSignStats = (scopedExtensions: DeviceInfoExtend[]) => {
  const heartRates: number[] = []
  const spo2Values: number[] = []
  const temps: number[] = []

  scopedExtensions.forEach((ext) => {
    if (ext.heartRate != null) heartRates.push(parseNumber(ext.heartRate))
    if (ext.spo2 != null) spo2Values.push(parseNumber(ext.spo2))
    const temperature = (ext as DeviceInfoExtend & { temperature?: unknown }).temperature ?? ext.temp
    if (temperature != null) {
      temps.push(parseNumber(temperature))
    }
  })

  return {
    heartRateAvg: heartRates.length ? Math.round(heartRates.reduce((a, b) => a + b, 0) / heartRates.length) : 0,
    spo2Avg: spo2Values.length ? Math.round(spo2Values.reduce((a, b) => a + b, 0) / spo2Values.length) : 0,
    tempAvg: temps.length ? Math.round((temps.reduce((a, b) => a + b, 0) / temps.length) * 10) / 10 : 0
  }
}

const generateReport = async () => {
  generating.value = true
  reportData.value = null
  try {
    const [subjectRes, exceptionRes, extRes] = await Promise.all([
      listSubjects({ pageNum: 1, pageSize: 200 }),
      listExceptions({ pageNum: 1, pageSize: 200 }),
      listDeviceExtensions({ pageNum: 1, pageSize: 200 })
    ])

    const subjects = extractListRows<Record<string, unknown>>(subjectRes)
    const exceptions = extractListRows<ExceptionAlert>(exceptionRes)
    const extensions = extractListRows<DeviceInfoExtend>(extRes)

    const extMap = new Map<number, DeviceInfoExtend>()
    extensions.forEach(e => { if (e.userId) extMap.set(Number(e.userId), e) })

    // 先计算所有对象的风险等级
    const subjectRiskMap = new Map<number, string>()
    subjects.forEach((s) => {
      const uid = Number(s.subjectId || s.userId)
      const ext = extMap.get(uid)
      const userEvents = exceptions.filter(e => Number(e.userId) === uid)
      subjectRiskMap.set(uid, determineRiskLevel(ext, userEvents))
    })

    // 根据 selectedReportType 差异化数据处理
    let filteredSubjects = subjects.filter(subject => matchesSelectedScope(subjectRiskMap.get(getSubjectId(subject)) || 'low'))
    let targetSubject: Record<string, unknown> | null = null

    if (selectedReportType.value === 'personal') {
      targetSubject = filteredSubjects[0] || subjects[0] || null
      filteredSubjects = targetSubject ? [targetSubject] : []
    }

    const filteredSubjectIds = new Set(filteredSubjects.map(subject => getSubjectId(subject)).filter(Boolean))
    const filteredExceptions = exceptions.filter(e => filteredSubjectIds.has(Number(e.userId)))
    const filteredExtensions = extensions.filter(ext => filteredSubjectIds.has(Number(ext.userId)))
    const scopedTrendPoints = buildScopedTrendPoints(filteredExceptions)


    const highRiskSubjects: HighRiskSubject[] = []
    let highRiskCount = 0

    filteredSubjects.forEach((s) => {
      const uid = getSubjectId(s)
      const ext = extMap.get(uid)
      const userEvents = filteredExceptions.filter(e => Number(e.userId) === uid)
      const level = subjectRiskMap.get(uid) || determineRiskLevel(ext, userEvents)
      if (level === 'high') highRiskCount++
      if (level === 'high' || level === 'medium' || selectedReportType.value === 'personal') {
        highRiskSubjects.push({
          name: String(s.subjectName || s.nickName || '未知'),
          age: String(s.age || '-'),
          riskLevel: level,
          eventCount: userEvents.length,
          lastAlarm: String(ext?.alarmTime || userEvents[0]?.createTime || '-')
        })
      }
    })

    const totalEvents = filteredExceptions.length
    const resolvedEvents = filteredExceptions.filter(isResolvedEvent).length
    const reportTypeName = reportTypes.find(r => r.value === selectedReportType.value)?.label || '报告'
    const periodName = periods.find(p => p.value === selectedPeriod.value)?.label || ''

    // 构建高风险对象明细
    const highRiskDetails = highRiskSubjects.slice(0, 10).map(s =>
      `(${s.name}, ${s.age}岁, ${s.riskLevel === 'high' ? '高风险' : '中风险'}, 异常${s.eventCount}次, 最近告警${s.lastAlarm})`
    ).join('; ')

    // 异常事件类型分布统计
    const exceptionTypeMap = new Map<string, number>()
    filteredExceptions.forEach(e => {
      const type = e.type || '未知类型'
      exceptionTypeMap.set(type, (exceptionTypeMap.get(type) || 0) + 1)
    })
    const abnormalTypeDistribution = Array.from(exceptionTypeMap.entries())
      .map(([type, count]) => ({ type, count }))
      .sort((a, b) => b.count - a.count)
    const exceptionTypes = abnormalTypeDistribution
      .map(({ type, count }) => `${type}${count}次`)
      .join(', ')

    // 趋势数据关键点
    const trendValues = scopedTrendPoints.map(p => Number(p.value || 0))
    const trendMax = trendValues.length ? Math.max(...trendValues) : 0
    const trendMin = trendValues.length ? Math.min(...trendValues) : 0
    const trendAvg = trendValues.length ? Math.round(trendValues.reduce((a, b) => a + b, 0) / trendValues.length) : 0

    // 体征统计计算
    let vitalSignStats: { heartRateAvg: number; spo2Avg: number; tempAvg: number } | null = null
    if (selectedReportType.value === 'health' || selectedReportType.value === 'personal') {
      vitalSignStats = buildVitalSignStats(filteredExtensions)
    }

    // 根据 selectedReportType 差异化 prompt 侧重点和前标题
    let promptFocus = ''
    let fallbackText = ''
    let reportSubtitle = ''
    let showSections = {
      kpi: true,
      trend: true,
      highRiskTable: true,
      abnormalDistribution: false,
      vitalSignStats: false
    }

    switch (selectedReportType.value) {
      case 'operation':
        promptFocus = '请从运营角度综合分析设备、对象、异常数据'
        fallbackText = `本周期内共监护 ${filteredSubjects.length} 名对象，发现高风险对象 ${highRiskCount} 人，产生异常事件 ${totalEvents} 次，已处置 ${resolvedEvents} 次。`
        reportSubtitle = '综合设备、对象、异常数据的运营分析'
        showSections = { kpi: true, trend: true, highRiskTable: true, abnormalDistribution: true, vitalSignStats: false }
        break
      case 'health':
        promptFocus = '请重点分析体征趋势与风险等级分布情况'
        fallbackText = `本周期内共监护 ${filteredSubjects.length} 名对象，高风险对象 ${highRiskCount} 人，产生异常事件 ${totalEvents} 次，已处置 ${resolvedEvents} 次。体征趋势方面，最高 ${trendMax} 次，最低 ${trendMin} 次，平均 ${trendAvg} 次。`
        reportSubtitle = '重点关注体征趋势与风险等级分布'
        showSections = { kpi: true, trend: true, highRiskTable: true, abnormalDistribution: true, vitalSignStats: true }
        break
      case 'highrisk':
        promptFocus = '请重点分析高风险对象的异常详情与处置建议'
        fallbackText = `本周期内共监护 ${filteredSubjects.length} 名对象，高风险对象 ${highRiskCount} 人，产生异常事件 ${totalEvents} 次，已处置 ${resolvedEvents} 次。高风险对象明细：${highRiskDetails || '暂无'}。`
        reportSubtitle = '聚焦高风险对象的异常详情与处置建议'
        showSections = { kpi: true, trend: false, highRiskTable: true, abnormalDistribution: true, vitalSignStats: false }
        break
      case 'personal':
        promptFocus = '请生成该对象的个人健康档案分析'
        const personalName = targetSubject ? String(targetSubject.subjectName || targetSubject.nickName || '未知') : '未选择对象'
        fallbackText = `本周期内共监护 ${filteredSubjects.length} 名对象（${personalName}），产生异常事件 ${totalEvents} 次，已处置 ${resolvedEvents} 次。`
        reportSubtitle = `个人健康档案：${personalName}`
        showSections = { kpi: true, trend: true, highRiskTable: false, abnormalDistribution: true, vitalSignStats: true }
        break
      default:
        promptFocus = '请综合分析报告数据'
        fallbackText = `本周期内共监护 ${filteredSubjects.length} 名对象，发现高风险对象 ${highRiskCount} 人，产生异常事件 ${totalEvents} 次，已处置 ${resolvedEvents} 次。`
        showSections = { kpi: true, trend: true, highRiskTable: true, abnormalDistribution: true, vitalSignStats: false }
    }

    let aiText = ''
    try {
      const prompt = `请根据以下数据生成简短的健康报告解读（不超过300字）：
【基础数据】总对象${filteredSubjects.length}人，高风险${highRiskCount}人，异常事件${totalEvents}次，已处置${resolvedEvents}次。
【高风险对象明细】${highRiskDetails || '暂无'}
【异常事件类型分布】${exceptionTypes || '暂无'}
【趋势数据】最高${trendMax}次，最低${trendMin}次，平均${trendAvg}次
【报告类型】${reportTypeName}，【周期】${periodName}
${vitalSignStats ? `【体征统计】心率均值${vitalSignStats.heartRateAvg}次/分，血氧均值${vitalSignStats.spo2Avg}%，体温均值${vitalSignStats.tempAvg}℃` : ''}
${promptFocus}`
      const aiRes = await chatAi(prompt)
      aiText = typeof aiRes?.data === 'string' ? aiRes.data : '数据分析完成，请关注高风险对象并及时跟进异常事件。'
    } catch {
      aiText = fallbackText
    }

    reportData.value = {
      reportType: reportTypeName,
      period: periodName,
      generatedAt: new Date().toLocaleString(),
      totalSubjects: filteredSubjects.length,
      highRiskCount,
      totalEvents,
      resolvedEvents,
      highRiskSubjects: highRiskSubjects.slice(0, 20),
      trendPoints: scopedTrendPoints,
      aiInterpretation: aiText,
      abnormalTypeDistribution,
      vitalSignStats,
      showSections,
      reportSubtitle
    }

    await nextTick()
    renderTrendChart()
  } catch (e) {
    ElMessage.error('生成报告失败，请检查网络或后端服务')
    console.error(e)
  } finally {
    generating.value = false
  }
}

const renderTrendChart = () => {
  const dom = document.getElementById('reportTrendChart')
  if (!dom) return
  trendChartInstance?.dispose()
  trendChartInstance = echarts.init(dom)
  const color = getThemeColor()
  const points = reportData.value?.trendPoints?.length
    ? reportData.value.trendPoints
    : Array.from({ length: 7 }, (_, i) => ({ label: `第${i + 1}天`, value: 0 }))
  trendChartInstance.setOption({
    grid: { left: '2%', right: '2%', bottom: '5%', top: '10%', containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: points.map(p => p.label), axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: 'rgba(0,0,0,0.06)' } } },
    series: [{
      name: '异常事件',
      type: 'line',
      data: points.map(p => Number(p.value || 0)),
      smooth: true,
      lineStyle: { color, width: 3 },
      itemStyle: { color },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: `${color}33` }, { offset: 1, color: `${color}00` }]) }
    }]
  })
}

const riskTagType = (level: string) => level === 'high' ? 'danger' : level === 'medium' ? 'warning' : 'success'
const riskLabel = (level: string) => level === 'high' ? '高风险' : level === 'medium' ? '中风险' : '低风险'

const escapeHtml = (value: string) => value
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;')

const normalizeAiReply = (value: unknown, fallback = 'AI 暂时没有返回有效内容，请稍后重试。') => {
  if (typeof value === 'string') {
    const trimmed = value.trim()
    return trimmed ? trimmed : fallback
  }
  if (value == null) {
    return fallback
  }
  if (typeof value === 'object') {
    try {
      const serialized = JSON.stringify(value, null, 2)
      return serialized && serialized.trim() ? serialized : fallback
    } catch {
      return fallback
    }
  }
  const text = String(value).trim()
  return text || fallback
}

const toRichText = (value: unknown, fallback?: string) => escapeHtml(normalizeAiReply(value, fallback)).replace(/\n/g, '<br/>')

const buildReportContext = () => {
  if (!reportData.value) return ''
  const r = reportData.value
  return `[报告摘要] 类型:${r.reportType} 周期:${r.period} 总对象:${r.totalSubjects}人 高风险:${r.highRiskCount}人 异常事件:${r.totalEvents}次 已处置:${r.resolvedEvents}次`
}

const scrollToBottom = () => nextTick(() => { if (chatScroll.value) chatScroll.value.scrollTop = chatScroll.value.scrollHeight })

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return
  messages.value.push({ id: Date.now(), role: 'user', content: text })
  inputText.value = ''
  isTyping.value = true
  scrollToBottom()
  try {
    const ctx = buildReportContext()
    const prompt = ctx ? `${ctx}\n\n用户问题：${text}` : text
    const res = await chatAi(prompt)
    const reply = typeof res?.data === 'string' ? res.data : '已收到您的问题，正在分析中...'
    messages.value.push({ id: Date.now(), role: 'ai', content: toRichText(reply) })
    scrollToBottom()
  } catch {
    messages.value.push({ id: Date.now(), role: 'ai', content: '请求失败，请检查网络连接。' })
  } finally {
    isTyping.value = false
  }
}

const exportToWord = () => {
  if (!reportData.value) { ElMessage.warning('请先生成报告'); return }
  const r = reportData.value
  const color = getThemeColor()
  const rows = r.highRiskSubjects.map(s => `<tr><td>${s.name}</td><td>${s.age}</td><td>${riskLabel(s.riskLevel)}</td><td>${s.eventCount}</td><td>${s.lastAlarm}</td></tr>`).join('')
  const html = `<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'><head><meta charset='utf-8'><title>${r.reportType}</title></head><body><h1 style="text-align:center;color:${color}">${r.reportType}</h1><p><strong>生成时间：</strong>${r.generatedAt} &nbsp; <strong>周期：</strong>${r.period}</p><table border="1" style="width:100%;border-collapse:collapse"><tr style="background:${color};color:#fff"><th>姓名</th><th>年龄</th><th>风险等级</th><th>异常次数</th><th>最近告警</th></tr>${rows}</table><h3>AI 解读</h3><p>${r.aiInterpretation}</p></body></html>`
  const url = URL.createObjectURL(new Blob([html], { type: 'application/msword;charset=utf-8' }))
  const a = document.createElement('a'); a.href = url; a.download = `${r.reportType}_${Date.now()}.doc`; a.click(); URL.revokeObjectURL(url)
  ElMessage.success('Word 报告已下载')
}

const exportToCsv = () => {
  if (!reportData.value) { ElMessage.warning('请先生成报告'); return }
  const headers = ['姓名', '年龄', '风险等级', '异常次数', '最近告警时间']
  const rows = reportData.value.highRiskSubjects.map(s => [s.name, s.age, riskLabel(s.riskLevel), String(s.eventCount), s.lastAlarm].map(v => `"${v}"`).join(','))
  const csv = [headers.join(','), ...rows].join('\n')
  const url = URL.createObjectURL(new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' }))
  const a = document.createElement('a'); a.href = url; a.download = `风险对象名单_${Date.now()}.csv`; a.click(); URL.revokeObjectURL(url)
  ElMessage.success('CSV 名单已下载')
}

const openChat = () => { showChatDrawer.value = true }

onMounted(() => {})
</script>

<style scoped lang="scss">
.ai-report-center {
  --ai-primary: #2563eb;
  --ai-primary-soft: #eff6ff;
  --ai-danger-soft: #fff1f2;
  --ai-success-soft: #ecfdf5;
  --ai-border: #e2e8f0;
  --ai-bg: #f8fafc;
  --ai-card: #ffffff;
  min-height: 100%;
  background: var(--ai-bg);
  display: flex;
  flex-direction: column;
}
.ai-report-center.theme-blue { --ai-primary: #2563eb; --ai-primary-soft: #eff6ff; }
.ai-report-center.theme-purple { --ai-primary: #4f46e5; --ai-primary-soft: #eef2ff; }
.ai-report-center.theme-green { --ai-primary: #059669; --ai-primary-soft: #ecfdf5; }
.ai-report-center.theme-pink { --ai-primary: #e11d48; --ai-primary-soft: #fff1f2; }

.report-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--ai-card);
  border-bottom: 1px solid var(--ai-border);
  gap: 16px;
}
.topbar-left { display: flex; flex-direction: column; gap: 2px; }
.topbar-eyebrow { font-size: 11px; font-weight: 700; letter-spacing: 0.08em; color: var(--ai-primary); text-transform: uppercase; }
.topbar-title { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }
.topbar-sub { margin: 0; font-size: 13px; color: #64748b; }
.topbar-right { display: flex; align-items: center; gap: 12px; }
.theme-switcher { display: flex; gap: 6px; }
.theme-dot {
  width: 24px; height: 24px; border-radius: 50%; cursor: pointer;
  border: 2px solid transparent; transition: all 0.2s;
}
.theme-dot.active { border-color: #0f172a; transform: scale(1.15); }

.report-body {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 0;
  flex: 1;
  min-height: 0;
}

.control-panel {
  display: flex;
  flex-direction: column;
  gap: 0;
  border-right: 1px solid var(--ai-border);
  background: var(--ai-card);
  overflow-y: auto;
}
.panel-section {
  padding: 16px 20px;
  border-bottom: 1px solid var(--ai-border);
}
.panel-label {
  font-size: 11px; font-weight: 700; letter-spacing: 0.06em;
  text-transform: uppercase; color: #94a3b8; margin-bottom: 10px;
}
.report-type-list { display: flex; flex-direction: column; gap: 8px; }
.type-btn {
  width: 100%; padding: 10px 14px; border-radius: 10px; border: 1px solid var(--ai-border);
  background: transparent; cursor: pointer; text-align: left; transition: all 0.2s;
  display: flex; flex-direction: column; gap: 2px;
}
.type-btn:hover { border-color: var(--ai-primary); background: var(--ai-primary-soft); }
.type-btn.active { border-color: var(--ai-primary); background: var(--ai-primary-soft); }
.type-btn-title { font-size: 13px; font-weight: 600; color: #0f172a; }
.type-btn-desc { font-size: 12px; color: #64748b; }

.period-tabs, .scope-tabs { display: flex; gap: 6px; margin-top: 10px; flex-wrap: wrap; }
.period-tab, .scope-tab {
  padding: 6px 14px; border-radius: 20px; border: 1px solid var(--ai-border);
  background: transparent; cursor: pointer; font-size: 13px; color: #475569; transition: all 0.2s;
}
.period-tab:hover, .scope-tab:hover { border-color: var(--ai-primary); color: var(--ai-primary); }
.period-tab.active, .scope-tab.active {
  background: var(--ai-primary); border-color: var(--ai-primary); color: #fff;
}

.generate-btn { width: 100%; font-size: 15px; font-weight: 700; }

.report-preview {
  flex: 1; display: flex; flex-direction: column; overflow-y: auto;
  background: var(--ai-bg); padding: 24px;
}
.empty-state { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; }
.empty-icon { font-size: 48px; color: var(--ai-primary); opacity: 0.3; }
.empty-tips { display: flex; flex-direction: column; gap: 8px; text-align: center; }
.empty-tip-item { font-size: 13px; color: #94a3b8; }
.no-data { text-align: center; color: #94a3b8; font-size: 13px; padding: 16px 0; }

.generating-state { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 20px; color: #64748b; font-size: 14px; }
.gen-ring {
  width: 48px; height: 48px; border-radius: 50%;
  border: 3px solid var(--ai-primary-soft); border-top-color: var(--ai-primary);
  animation: spin 0.8s linear infinite;
}
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.report-content { padding: 24px; display: flex; flex-direction: column; gap: 20px; }
.report-header-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 4px; }
.report-eyebrow { font-size: 11px; font-weight: 700; letter-spacing: 0.08em; color: var(--ai-primary); text-transform: uppercase; margin-bottom: 4px; }
.report-title { font-size: 22px; font-weight: 700; color: #0f172a; margin: 0 0 6px; }
.report-subtitle { font-size: 13px; color: #64748b; margin: 0 0 6px; }
.section-title { font-size: 13px; font-weight: 700; color: #475569; display: flex; align-items: center; gap: 6px; margin-bottom: 12px; }

.kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.kpi-card {
  background: var(--ai-card); border-radius: 12px;
  border: 1px solid var(--ai-border); padding: 16px 20px;
  display: flex; flex-direction: column; gap: 4px;
}
.kpi-card.danger { background: var(--ai-danger-soft); border-color: #fecdd3; }
.kpi-card.success { background: var(--ai-success-soft); border-color: #a7f3d0; }
.kpi-label { font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; }
.kpi-value { font-size: 28px; font-weight: 700; color: #0f172a; line-height: 1.1; }
.kpi-card.danger .kpi-value { color: #e11d48; }
.kpi-card.success .kpi-value { color: #059669; }

.abnormal-distribution { display: flex; flex-direction: column; gap: 10px; }
.dist-item { display: flex; align-items: center; gap: 10px; }
.dist-label { font-size: 13px; color: #475569; min-width: 80px; }
.dist-bar-wrapper { flex: 1; height: 8px; background: var(--ai-bg); border-radius: 4px; overflow: hidden; }
.dist-bar { height: 100%; background: var(--ai-primary); border-radius: 4px; transition: width 0.3s ease; }
.dist-count { font-size: 13px; font-weight: 600; color: var(--ai-primary); min-width: 50px; text-align: right; }

.trend-chart { width: 100%; height: 220px; }

.vital-signs-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.vs-card { display: flex; align-items: center; gap: 12px; background: var(--ai-card); border: 1px solid var(--ai-border); border-radius: 10px; padding: 16px; }
.vs-icon { font-size: 24px; }
.vs-content { display: flex; flex-direction: column; gap: 2px; }
.vs-value { font-size: 20px; font-weight: 700; color: #0f172a; }
.vs-label { font-size: 12px; color: #64748b; }

.ai-interpretation { background: var(--ai-primary-soft); border-radius: 12px; padding: 16px; }
.interpretation-text { font-size: 14px; line-height: 1.8; color: #1e3a5f; margin: 0; white-space: pre-wrap; }

.export-section { border-bottom: none !important; }
.export-btns { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }

.chat-drawer-body { display: flex; flex-direction: column; height: 100%; padding: 16px; gap: 12px; }
.chat-viewport { flex: 1; overflow-y: auto; padding: 16px; }
.chat-list { display: flex; flex-direction: column; gap: 12px; }
.message-box { display: flex; gap: 10px; align-items: flex-start; }
.message-box.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 32px; height: 32px; border-radius: 8px; flex: 0 0 auto;
  display: flex; align-items: center; justify-content: center;
  background: var(--ai-primary-soft); color: var(--ai-primary);
}
.msg-avatar.user { background: #f1f5f9; color: #475569; }
.msg-bubble {
  max-width: 80%; padding: 10px 14px; border-radius: 12px;
  background: var(--ai-card); border: 1px solid var(--ai-border);
  font-size: 14px; line-height: 1.7; color: #334155;
}
.message-box.user .msg-bubble { background: var(--ai-primary-soft); border-color: transparent; }
.typing-indicator { display: inline-flex; align-items: center; gap: 6px; color: #94a3b8; font-size: 13px; }
.chat-input-area { padding: 16px; border-top: 1px solid var(--ai-border); display: flex; flex-direction: column; gap: 8px; }
.context-badge {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12px; color: var(--ai-primary); background: var(--ai-primary-soft);
  padding: 4px 10px; border-radius: 999px;
}
.input-row { display: flex; gap: 8px; align-items: flex-end; }
.send-btn { height: 76px; }
.drawer-settings-content { display: flex; flex-direction: column; gap: 16px; padding: 16px; }

@media (max-width: 1100px) {
  .report-body { grid-template-columns: minmax(0, 1fr); }
  .control-panel { border-right: none; border-bottom: 1px solid var(--ai-border); }
  .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .vital-signs-grid { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .kpi-row { grid-template-columns: 1fr; }
  .vital-signs-grid { grid-template-columns: 1fr; }
  .report-topbar { flex-direction: column; align-items: flex-start; }
}

.chat-flow-enter-active,
.chat-flow-leave-active,
.fade-enter-active,
.fade-leave-active {
  transition: all 0.25s ease;
}
.chat-flow-enter-from,
.chat-flow-leave-to,
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
