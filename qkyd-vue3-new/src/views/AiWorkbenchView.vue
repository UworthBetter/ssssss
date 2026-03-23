<template>
  <div :class="['ai-command-center', `theme-${settingsForm.theme}`]">
    <PlatformPageShell
      title="AI 决策辅助舱"
      subtitle="沉浸式无干扰环境，专注处理医疗健康复杂推断。"
      eyebrow="FOCUS MODE"
    >
      <template #headerExtra>
        <div class="header-actions">
          <PlatformSearchEntry
            :label="searchPresentation.label"
            :placeholder="searchPresentation.placeholder"
            :hint="searchPresentation.hint"
            @click="handleSearchClick"
          />
          <PlatformNotificationEntry
            title="通知"
            :unread-count="notificationItems.length"
            :items="notificationItems"
            @click-item="handleNotificationItem"
            @click-all="handleNotificationClick"
            style="margin-left: 12px; margin-right: 12px;"
          />
          <el-button circle plain size="small" @click="showSettingsDrawer = true">
            <el-icon><Setting /></el-icon>
          </el-button>
        </div>
      </template>

      <template #toolbar>
        <div class="ai-focus-layout toolbar-focus-pad">
          <div class="metrics-pill-row">
            <div class="metric-pill">
              <el-icon><ChatDotRound /></el-icon>
              <span>分析 <strong>{{ aiMessageCount }}</strong></span>
            </div>
            <div class="metric-pill">
              <el-icon><Grid /></el-icon>
              <span>结构化 <strong>{{ structuredInsightCount }}</strong></span>
            </div>
            <div class="metric-pill danger">
              <el-icon><Warning /></el-icon>
              <span>高风险 <strong>{{ highRiskCount }}</strong></span>
            </div>
            <div class="metric-pill success">
              <el-icon><VideoPlay /></el-icon>
              <span>动作 <strong>{{ pendingActionCount }}</strong></span>
            </div>
          </div>
          <div class="mode-row">
            <button
              v-for="task in quickTasks"
              :key="task.label"
              type="button"
              class="mode-chip"
              :class="{ active: activeTaskLabel === task.label }"
              @click="runQuickTask(task)"
            >
              <span class="mode-chip-title">{{ task.label }}</span>
              <span class="mode-chip-desc">{{ task.description }}</span>
            </button>
          </div>
        </div>
      </template>

      <div class="ai-focus-layout">
        <main class="workbench-center-pane">
          <section class="conversation-panel">
            <header class="conversation-header">
              <div>
                <p class="section-eyebrow">分析线程</p>
                <h3>{{ settingsForm.botName }} 智能决策中枢</h3>
                <span class="conversation-subtitle">基于医疗大模型引擎，确保结构化动作的可靠性。</span>
              </div>

              <el-dropdown trigger="click" @command="handleHeaderCommand">
                <el-button circle plain size="small">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="clear">
                      <el-icon><Delete /></el-icon> 重置舱室
                    </el-dropdown-item>
                    <el-dropdown-item command="settings">
                      <el-icon><Setting /></el-icon> 助理偏好
                    </el-dropdown-item>
                    <el-dropdown-item command="exportChat" divided>
                      <el-icon><Document /></el-icon> 下载推断记录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </header>

            <div ref="chatScroll" class="chat-viewport">
              <transition-group name="chat-flow" tag="div" class="chat-list">
                <div v-for="msg in messages" :key="msg.id" :class="['message-box', msg.role]">
                  <div class="msg-avatar" :class="msg.role">
                    <el-icon v-if="msg.role === 'ai'" size="20"><Monitor /></el-icon>
                    <el-icon v-else size="20"><Avatar /></el-icon>
                  </div>

                  <div class="msg-content-wrapper">
                    <div class="msg-bubble" :class="{ 'rich-media': msg.type !== 'text' || Boolean(msg.insight) }">
                      <div v-if="msg.insight" class="insight-card">
                        <div class="insight-head">
                          <div>
                            <p class="section-eyebrow">推理结论</p>
                            <h4>{{ msg.insight.title }}</h4>
                          </div>
                          <el-tag :type="riskTagType(msg.insight.riskLevel)" effect="light">
                            {{ riskLabelMap[msg.insight.riskLevel] }}
                          </el-tag>
                        </div>

                        <p class="insight-summary">{{ msg.insight.summary }}</p>

                        <div class="insight-grid">
                          <div class="insight-block">
                            <span class="block-label">证据要素</span>
                            <ul>
                              <li v-for="evidence in msg.insight.evidence" :key="`${evidence.label}-${evidence.detail}`">
                                <strong>{{ evidence.label }}</strong>
                                <span>{{ evidence.detail }}</span>
                              </li>
                            </ul>
                          </div>

                          <div class="insight-block">
                            <span class="block-label">溯源实体</span>
                            <ul>
                              <li v-for="entity in msg.insight.entities" :key="`${entity.kind}-${entity.value}`">
                                <strong>{{ entity.name }}</strong>
                                <span>{{ entity.kindLabel }} · {{ entity.value }}</span>
                              </li>
                            </ul>
                          </div>
                        </div>
                      </div>

                      <div v-if="msg.type === 'text'" v-html="msg.content" class="md-text"></div>

                      <div v-if="msg.type === 'table'" class="widget-box">
                        <div class="widget-header">
                          <el-icon><List /></el-icon>
                          <span>重点拦截名单</span>
                        </div>
                        <el-table :data="msg.data" size="small" class="glass-table" :row-class-name="tableRowClassName">
                          <el-table-column prop="name" label="姓名" width="90" />
                          <el-table-column prop="room" label="房间号" width="100" />
                          <el-table-column prop="issue" label="异常类型" />
                          <el-table-column prop="time" label="发生时间" width="140" />
                        </el-table>
                      </div>

                      <div v-if="msg.type === 'chart'" class="widget-box">
                        <div class="widget-header">
                          <el-icon><TrendCharts /></el-icon>
                          <span>动态监测图谱</span>
                        </div>
                        <div :id="'chart-' + msg.id" class="echarts-container"></div>
                      </div>
                    </div>

                    <transition name="fade">
                      <div v-if="msg.role === 'ai' && msg.actions && msg.actions.length" class="action-chips">
                        <button
                          v-for="(action, idx) in msg.actions"
                          :key="idx"
                          type="button"
                          class="chip-btn"
                          @click="handleAction(action, msg)"
                        >
                          {{ action }}
                        </button>
                      </div>
                    </transition>
                  </div>
                </div>
                <div v-if="isTyping" key="typing" class="message-box ai">
                  <div class="msg-avatar ai">
                    <el-icon size="20"><Monitor /></el-icon>
                  </div>
                  <div class="msg-content-wrapper">
                    <div class="msg-bubble typing">
                      <span class="typing-text">计算集群正在分析...</span>
                      <div class="dot-flashing"></div>
                    </div>
                  </div>
                </div>
              </transition-group>
            </div>

            <div class="floating-input-zone">
              <div class="pill-input-box" :class="{ focused: isInputFocused }">
                <div v-if="isTyping" class="input-header-status">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>正在快速检索与分析...</span>
                </div>
                <el-input
                  v-model="inputText"
                  type="textarea"
                  :autosize="{ minRows: 1, maxRows: 6 }"
                  resize="none"
                  :placeholder="`向 ${settingsForm.botName} 发起深度调度，获取结论与处置建议...`"
                  @focus="isInputFocused = true"
                  @blur="isInputFocused = false"
                  @keyup.enter.exact.prevent="sendMessage"
                />
                <div class="input-footer">
                  <div class="sec-footer">
                    <el-icon><Lock /></el-icon>
                    医疗核心数据链路受控 · {{ settingsForm.botName }}
                  </div>
                  <button
                    type="button"
                    class="send-action-btn"
                    :class="{ ready: inputText.trim().length > 0 }"
                    :disabled="isTyping"
                    @click="sendMessage"
                  >
                    <el-icon v-if="!isTyping"><Position /></el-icon>
                    <el-icon v-else class="spin"><Loading /></el-icon>
                  </button>
                </div>
              </div>
            </div>
          </section>
        </main>
      </div>
    </PlatformPageShell>

    <el-drawer
      v-model="showSettingsDrawer"
      title="助理配置矩阵"
      direction="rtl"
      size="380px"
      class="glass-drawer"
      :show-close="true"
    >
      <div class="drawer-settings-content">
        <el-form label-position="top" :model="settingsForm">
          <el-form-item label="核心名称" class="setting-item">
            <el-input v-model="settingsForm.botName" placeholder="命名您的引擎" clearable>
              <template #prefix><el-icon><Service /></el-icon></template>
            </el-input>
            <div class="form-tip">同步于工作台所有核心汇报视窗。</div>
          </el-form-item>

          <el-form-item label="输出策略" class="setting-item">
            <el-radio-group v-model="settingsForm.tone" class="custom-radio-group">
              <el-radio-button label="professional">严密逻辑</el-radio-button>
              <el-radio-button label="lively">亲切平易</el-radio-button>
              <el-radio-button label="concise">极简图文</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="光污染保护主题" class="setting-item">
            <el-select v-model="settingsForm.theme">
              <el-option label="静幽紫" value="purple" />
              <el-option label="医疗蓝" value="blue" />
              <el-option label="生态绿" value="green" />
              <el-option label="警示红" value="pink" />
            </el-select>
          </el-form-item>

          <el-form-item label="语音播报链路" class="setting-item inline-setting">
            <el-switch v-model="settingsForm.autoSpeak" />
            <span class="setting-inline-tip">启用结构化警报音频直读</span>
          </el-form-item>
        </el-form>

        <div class="drawer-actions">
          <el-button @click="showSettingsDrawer = false">撤销</el-button>
          <el-button type="primary" @click="saveSettings">应用架构</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>
<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Avatar,
  ChatDotRound,
  Delete,
  Document,
  Grid,
  List,
  Loading,
  Lock,
  Microphone,
  Monitor,
  MoreFilled,
  Position,
  Service,
  Setting,
  TrendCharts,
  VideoPlay,
  Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { chatAi, getAbnormalTrend, getRecentAbnormal, type AbnormalTrendPoint } from '@/api/ai'
import { useHealthRealtimeStream } from '@/composables/useHealthRealtimeStream'
import {
  dispatchPlatformAction,
  getPlatformSearchPresentation,
  loadPlatformNotifications,
  navigatePlatformEntity,
  openAllPlatformNotifications,
  openPlatformNotification,
  openPlatformSearch,
  PlatformNotificationEntry,
  PlatformPageShell,
  PlatformSearchEntry,
  type PlatformNotificationRecord
} from '@/components/platform'

interface InsightEvidence {
  label: string
  detail: string
}

type EntityKind = 'subject' | 'device' | 'event'
type RiskLevel = 'high' | 'medium' | 'low'

interface RelatedEntity {
  kind: EntityKind
  kindLabel: string
  name: string
  value: string
  query?: Record<string, string>
}

interface StructuredInsight {
  title: string
  summary: string
  riskLevel: RiskLevel
  evidence: InsightEvidence[]
  entities: RelatedEntity[]
  recommendedActions: string[]
}

interface ChatMessage {
  id: number
  role: 'user' | 'ai'
  type: 'text' | 'table' | 'chart'
  content: string
  data?: Record<string, string>[]
  actions?: string[]
  insight?: StructuredInsight
}

interface QuickTask {
  label: string
  description: string
  prompt: string
  tag: string
}

interface RecentAbnormalItem {
  id?: number | string
  userId?: number | string
  patientName?: string
  abnormalType?: string
  riskLevel?: string
  abnormalValue?: string
  confidence?: number | string
  deviceName?: string
  source?: string
  detectedTime?: string
  createTime?: string
}

const router = useRouter()

const riskLabelMap: Record<RiskLevel, string> = {
  high: '高风险',
  medium: '中风险',
  low: '低风险'
}

const toneLabelMap: Record<string, string> = {
  professional: '专业严谨',
  lively: '活泼亲切',
  concise: '简明扼要'
}

const quickTasks: QuickTask[] = [
  { label: '风险研判', description: '快速输出高风险对象、证据与后续动作建议。', prompt: '请研判今日高风险健康事件，并输出结论、证据与动作建议。', tag: '重点' },
  { label: '睡眠趋势分析', description: '分析重点对象近七天睡眠趋势，并识别需要关注的人。', prompt: '分析重点对象近七天睡眠趋势，并识别需要关注的人。', tag: '趋势' },
  { label: '异常解释', description: '把异常名单升级为原因解释与处置建议。', prompt: '解释最近的心率异常名单，并给出后续动作建议。', tag: '解释' },
  { label: '报告生成', description: '沉淀成面向业务的周期分析报告。', prompt: '生成本周健康巡检分析报告，并附上动作建议。', tag: '报告' }
]

const inputText = ref('')
const isTyping = ref(false)
const isInputFocused = ref(false)
const chatScroll = ref<HTMLElement | null>(null)
const activeTaskLabel = ref('风险研判')
const showSettingsDrawer = ref(false)

const settingsForm = ref({
  botName: '辅助决策',
  tone: 'professional',
  autoSpeak: false,
  theme: 'blue'
})
const searchPresentation = getPlatformSearchPresentation('ai')

const buildEntity = (kind: EntityKind, name: string, value: string, query: Record<string, string> = {}): RelatedEntity => ({
  kind,
  kindLabel: kind === 'subject' ? '对象' : kind === 'device' ? '设备' : '事件',
  name,
  value,
  query
})

const getInitialMessage = (): ChatMessage => ({
  id: 1,
  role: 'ai',
  type: 'text',
  content: '您好，我是您的 AI 决策助理。我会把分析结果整理成结论、证据、关联实体和建议动作。',
  insight: {
    title: '工作台已就绪',
    summary: '当前 AI 工作台已经具备结构化结果展示与跨中心联动能力，可以直接跳转到对象、事件、设备中心继续处置。',
    riskLevel: 'low',
    evidence: [
      { label: '阶段', detail: 'P6-001 聚焦真实跳转与共享联动' },
      { label: '当前能力', detail: '已具备对象、设备、事件路由联动和结构化输出能力。' }
    ],
    entities: [
      buildEntity('event', '事件中心', '接收 AI 研判结果', { type: 'AI研判' }),
      buildEntity('subject', '对象中心', '接收重点关注对象', { keyword: '张建国' }),
      buildEntity('device', '设备中心', '接收设备运营动作', { name: '夜间监测设备组' })
    ],
    recommendedActions: ['开始风险研判', '生成报告并下载']
  },
  actions: ['开始风险研判', '生成报告并下载']
})

const messages = ref<ChatMessage[]>([getInitialMessage()])
const recentAbnormalRows = ref<RecentAbnormalItem[]>([])
const abnormalTrend = ref<AbnormalTrendPoint[]>([])
const dashboardRefreshTimer = ref<number | null>(null)
const riskChartInstance = ref<echarts.ECharts | null>(null)
const trendChartInstance = ref<echarts.ECharts | null>(null)
const realtimeRefreshTimer = ref<number | null>(null)

const normalizedRiskLevel = (riskLevel: unknown): RiskLevel => {
  const value = String(riskLevel ?? '').trim().toLowerCase()
  if (['critical', 'high', 'danger'].includes(value)) return 'high'
  if (['medium', 'warning'].includes(value)) return 'medium'
  return 'low'
}

const recentRiskSummary = computed(() => recentAbnormalRows.value.reduce((summary, row) => {
  const level = normalizedRiskLevel(row.riskLevel)
  summary[level] += 1
  return summary
}, { high: 0, medium: 0, low: 0 } as Record<RiskLevel, number>))

const aiMessageCount = computed(() => {
  const trendTotal = abnormalTrend.value.reduce((sum, point) => sum + Number(point.value || 0), 0)
  return trendTotal || messages.value.filter((msg) => msg.role === 'ai').length
})
const structuredInsightCount = computed(() => recentAbnormalRows.value.length || messages.value.filter((msg) => Boolean(msg.insight)).length)
const highRiskCount = computed(() => recentRiskSummary.value.high || messages.value.filter((msg) => msg.insight?.riskLevel === 'high').length)
const latestInsight = computed(() => [...messages.value].reverse().find((msg) => msg.insight)?.insight ?? null)
const pendingActionCount = computed(() => {
  const backendPending = recentRiskSummary.value.high + recentRiskSummary.value.medium
  return backendPending || latestInsight.value?.recommendedActions.length || 0
})
const notificationItems = ref<PlatformNotificationRecord[]>([])

const refreshNotifications = async () => {
  const subjectEntity = latestInsight.value?.entities.find((item) => item.kind === 'subject')
  const deviceEntity = latestInsight.value?.entities.find((item) => item.kind === 'device')
  const eventEntity = latestInsight.value?.entities.find((item) => item.kind === 'event')

  notificationItems.value = await loadPlatformNotifications('ai', {
    subjectName: subjectEntity?.name,
    deviceName: deviceEntity?.name,
    eventType: eventEntity?.query?.type || eventEntity?.name
  })
  /*
  myChart.setOption({
    legend: { data: ['异常事件'], top: 0, right: 0 },
    xAxis: { data: trendPoints.map(point => point.label) },
    series: [
      {
        name: '异常事件',
        type: 'line',
        data: trendPoints.map(point => Number(point.value || 0)),
        smooth: true,
        lineStyle: { color: chartColor, width: 3 },
        itemStyle: { color: chartColor },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${chartColor}33` },
            { offset: 1, color: `${chartColor}00` }
          ])
        }
      }
    ]
  }) */
}

const getThemeChartColor = () => (
  settingsForm.value.theme === 'purple'
    ? '#4f46e5'
    : settingsForm.value.theme === 'pink'
      ? '#e11d48'
      : settingsForm.value.theme === 'green'
        ? '#059669'
        : '#2563eb'
)

const normalizeRecentAbnormalRows = (rows: unknown): RecentAbnormalItem[] => {
  if (!Array.isArray(rows)) return []
  return rows.map((item) => {
    const row = (item || {}) as Record<string, unknown>
    return {
      id: row.id as number | string | undefined,
      userId: row.userId as number | string | undefined,
      patientName: typeof row.patientName === 'string' ? row.patientName : '',
      abnormalType: typeof row.abnormalType === 'string' ? row.abnormalType : '',
      riskLevel: typeof row.riskLevel === 'string' ? row.riskLevel : '',
      abnormalValue: typeof row.abnormalValue === 'string' ? row.abnormalValue : '',
      confidence: row.confidence as number | string | undefined,
      deviceName: typeof row.deviceName === 'string' ? row.deviceName : '',
      source: typeof row.source === 'string' ? row.source : '',
      detectedTime: typeof row.detectedTime === 'string' ? row.detectedTime : '',
      createTime: typeof row.createTime === 'string' ? row.createTime : ''
    }
  })
}

const normalizeTrendRows = (rows: unknown): AbnormalTrendPoint[] => {
  if (!Array.isArray(rows)) return []
  return rows.map((item) => {
    const point = (item || {}) as Record<string, unknown>
    return {
      label: String(point.label ?? ''),
      value: Number(point.value ?? 0)
    }
  }).filter(point => point.label)
}

const fetchWorkbenchDashboardData = async () => {
  try {
    const [recentRes, trendRes] = await Promise.all([
      getRecentAbnormal(60),
      getAbnormalTrend(undefined, 12)
    ])
    recentAbnormalRows.value = normalizeRecentAbnormalRows(recentRes?.data)
    abnormalTrend.value = normalizeTrendRows(trendRes?.data)
  } catch {
    recentAbnormalRows.value = []
    abnormalTrend.value = []
  }
}

const buildRecentTableRows = () => {
  if (!recentAbnormalRows.value.length) {
    return [
      { name: '暂无数据', room: '-', issue: '等待后端异常流', time: '-' }
    ]
  }

  return recentAbnormalRows.value.slice(0, 6).map((row, index) => ({
    name: row.patientName || `对象-${row.userId || index + 1}`,
    room: row.deviceName || '-',
    issue: [row.abnormalType, row.abnormalValue].filter(Boolean).join(' / ') || '-',
    time: row.detectedTime || row.createTime || '-'
  }))
}

const clearRealtimeRefreshTimer = () => {
  if (realtimeRefreshTimer.value !== null) {
    window.clearTimeout(realtimeRefreshTimer.value)
    realtimeRefreshTimer.value = null
  }
}

const scheduleWorkbenchRefresh = () => {
  clearRealtimeRefreshTimer()
  realtimeRefreshTimer.value = window.setTimeout(async () => {
    await fetchWorkbenchDashboardData()
    nextTick(() => {
      renderDashboardCharts()
    })
    realtimeRefreshTimer.value = null
  }, 400)
}

const pushRealtimeAlertMessage = (payload: Record<string, unknown>) => {
  const patientName = typeof payload.patientName === 'string' && payload.patientName ? payload.patientName : '监测对象'
  const abnormalType = typeof payload.abnormalType === 'string' && payload.abnormalType ? payload.abnormalType : '异常事件'
  const abnormalValue = typeof payload.abnormalValue === 'string' && payload.abnormalValue ? payload.abnormalValue : ''
  const summary = [patientName, abnormalType, abnormalValue].filter(Boolean).join(' / ')

  messages.value.push({
    id: Date.now(),
    role: 'ai',
    type: 'text',
    content: `实时更新：${summary}`
  })
  scrollToBottom()
}

const realtimeStream = useHealthRealtimeStream({
  onAbnormalAlert(payload) {
    pushRealtimeAlertMessage(payload)
    scheduleWorkbenchRefresh()
  },
  onHealthData() {
    scheduleWorkbenchRefresh()
  },
  onRiskScore() {
    scheduleWorkbenchRefresh()
  }
})

const speakText = (text: string) => {
  if (!settingsForm.value.autoSpeak || !('speechSynthesis' in window)) return
  window.speechSynthesis.cancel()
  const plainText = text.replace(/<[^>]+>/g, '').replace(/&[^;]+;/g, '')
  const utterance = new SpeechSynthesisUtterance(plainText)
  utterance.lang = 'zh-CN'
  utterance.rate = settingsForm.value.tone === 'lively' ? 1.08 : 1.0
  utterance.pitch = settingsForm.value.tone === 'lively' ? 1.15 : 1.0
  window.speechSynthesis.speak(utterance)
}

const saveSettings = () => {
  showSettingsDrawer.value = false
  ElMessage.success(`设置已更新，当前助理：${settingsForm.value.botName}`)
  const autoSpeak = settingsForm.value.autoSpeak
  settingsForm.value.autoSpeak = true
  speakText(`设置已保存，我是您的助理 ${settingsForm.value.botName}`)
  settingsForm.value.autoSpeak = autoSpeak
}

const pokeMascot = () => {
  if (isTyping.value) return
  const phrases = [
    `别担心，${settingsForm.value.botName} 正在持续跟踪风险变化。`,
    `${settingsForm.value.botName} 已准备好整理证据和动作建议。`,
    '今天也继续把监测结果沉淀成可执行的工作流。',
    '如果需要，我现在就可以开始风险研判。'
  ]
  const phrase = phrases[Math.floor(Math.random() * phrases.length)]
  messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: phrase })
  speakText(phrase)
  scrollToBottom()
}

const handleHeaderCommand = (command: string) => {
  if (command === 'clear') {
    messages.value = [getInitialMessage()]
    ElMessage.success('对话已清空')
    return
  }
  if (command === 'settings') {
    showSettingsDrawer.value = true
    return
  }
  if (command === 'exportChat') {
    ElMessage.info('导出对话能力将在后续阶段接入')
  }
}

const buildPromptModifier = () => {
  if (settingsForm.value.tone === 'professional') return '\n(系统：请使用专业、客观的语气回答，并优先输出结构化结论。)'
  if (settingsForm.value.tone === 'lively') return '\n(系统：请使用亲切、清晰的语气回答，并保持结论、证据和动作的结构化输出。)'
  return '\n(系统：请尽量简洁，优先输出结论、证据和后续动作。)'
}

const buildStructuredInsight = (text: string, replyText: string, type: ChatMessage['type'], actions: string[]): StructuredInsight => {
  const riskLevel: RiskLevel = text.includes('高风险') || text.includes('异常') || text.includes('心率') ? 'high' : text.includes('趋势') || text.includes('睡眠') || text.includes('预测') ? 'medium' : 'low'
  let entities: RelatedEntity[]

  if (type === 'table') {
    entities = [
      buildEntity('subject', '张建国', 'A-302 / 心率异常', { keyword: '张建国' }),
      buildEntity('subject', '李桂英', 'B-105 / 心率异常', { keyword: '李桂英' }),
      buildEntity('event', '异常事件池', '建议创建巡检工单', { type: '心率异常' })
    ]
  } else if (type === 'chart') {
    entities = [
      buildEntity('subject', '重点对象组', '近 7 天睡眠波动明显升高', { keyword: '重点对象组' }),
      buildEntity('device', '夜间监测设备组', '建议提高夜间采样频率', { name: '夜间监测设备组' }),
      buildEntity('event', '趋势分析事件', '建议触发复核流程', { type: '睡眠趋势' })
    ]
  } else {
    entities = [
      buildEntity('event', 'AI 分析结果', '待转业务动作', { type: 'AI研判' }),
      buildEntity('subject', '重点关注对象', '可纳入后续跟踪', { keyword: '重点关注对象' })
    ]
  }

  return {
    title: type === 'table' ? '异常名单研判结果' : type === 'chart' ? '趋势分析结果' : '综合分析结果',
    summary: replyText.replace(/<br\s*\/?>/g, ' ').replace(/<[^>]+>/g, '').slice(0, 120),
    riskLevel,
    evidence: [
      { label: '触发指令', detail: text },
      { label: '输出类型', detail: type === 'table' ? '名单整理与处置建议' : type === 'chart' ? '趋势图表与波动分析' : '文本结论与建议' },
      { label: '当前上下文', detail: 'AI 中心 / 风险研判与报告生成' }
    ],
    entities,
    recommendedActions: actions.length ? actions : ['创建事件', '加入重点关注']
  }
}
const pushAiResponse = (text: string, replyText: string, type: ChatMessage['type'], data: Record<string, string>[] | undefined, actions: string[]) => {
  const msgId = Date.now()
  const insight = buildStructuredInsight(text, replyText, type, actions)
  messages.value.push({ id: msgId, role: 'ai', type, content: replyText, data, actions: insight.recommendedActions, insight })
  speakText(insight.summary)
  if (type === 'chart') nextTick(() => renderChart(`chart-${msgId}`))
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return
  messages.value.push({ id: Date.now(), role: 'user', type: 'text', content: text })
  inputText.value = ''
  isTyping.value = true
  scrollToBottom()

  try {
    const res = await chatAi(text + buildPromptModifier())
    let replyText = typeof res.data === 'string' ? res.data : JSON.stringify(res.data, null, 2)
    replyText = replyText.replace(/\n/g, '<br/>')

    let type: ChatMessage['type'] = 'text'
    let data: Record<string, string>[] | undefined
    let actions: string[] = []

    if (text.includes('心率') || text.includes('名单') || text.includes('导出')) {
      type = 'table'
      data = [
        { name: '张建国', room: 'A-302', issue: '心动过缓（45bpm）', time: '02:15 - 02:40' },
        { name: '李桂英', room: 'B-105', issue: '心率飙升（110bpm）', time: '04:22 - 04:30' }
      ]
      actions = ['导出 Excel 名单', '下发巡检工单']
    } else if (text.includes('分析') || text.includes('趋势') || text.includes('睡眠') || text.includes('预测')) {
      type = 'chart'
      actions = ['调整设备监测频率', '生成报告并下载']
    } else if (text.includes('报告')) {
      actions = ['生成报告并下载', '创建事件']
    } else {
      actions = ['创建事件', '加入重点关注']
    }

    if (type === 'table') {
      data = buildRecentTableRows()
    }

    pushAiResponse(text, replyText, type, data, actions)
  } catch (error: any) {
    messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: `服务暂时不可用，请稍后重试。<br/><span style="color:#d14d72;font-size:13px;">(${error.message || '网络错误'})</span>` })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

const runQuickTask = (task: QuickTask) => {
  activeTaskLabel.value = task.label
  inputText.value = task.prompt
  sendMessage()
}

const askCopilot = (text: string) => {
  inputText.value = text
  sendMessage()
}

const exportToCsv = (tableData?: Record<string, string>[]) => {
  if (!tableData || tableData.length === 0) {
    ElMessage.warning('数据为空')
    return
  }
  const headers = ['姓名', '房间号', '异常类型', '发生时间']
  const keys = ['name', 'room', 'issue', 'time']
  const csvRows = [headers.join(',')]
  tableData.forEach((row) => csvRows.push(keys.map((key) => `"${row[key] || ''}"`).join(',')))
  const url = URL.createObjectURL(new Blob(['\uFEFF' + csvRows.join('\n')], { type: 'text/csv;charset=utf-8;' }))
  const link = document.createElement('a')
  link.href = url
  link.download = `数据导出_${Date.now()}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

const exportToWord = () => {
  const html = `<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'><head><meta charset='utf-8'><title>报告</title></head><body><h1 style="text-align:center;color:${settingsForm.value.theme === 'purple' ? '#3f3cbb' : '#1d4ed8'};">健康分析报告</h1><p><strong>生成时间：</strong>${new Date().toLocaleString()}</p></body></html>`
  const url = URL.createObjectURL(new Blob([html], { type: 'application/msword;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = `${settingsForm.value.botName}_分析报告_${Date.now()}.doc`
  link.click()
  URL.revokeObjectURL(url)
}

const findEntityByKind = (insight: StructuredInsight | undefined, kind: EntityKind) => insight?.entities.find((item) => item.kind === kind)

const buildActionEntities = (insight?: StructuredInsight) => ({
  subject: findEntityByKind(insight, 'subject'),
  event: findEntityByKind(insight, 'event'),
  device: findEntityByKind(insight, 'device')
})

const handleAction = async (action: string, msg?: ChatMessage) => {
  if (action === '开始风险研判') {
    askCopilot('请研判今日高风险健康事件，并输出结论、证据与动作建议。')
    return
  }
  if (action === '导出 Excel 名单') {
    exportToCsv(msg?.data)
    messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: `${settingsForm.value.botName} 建议将这些高风险对象转入事件中心继续处置。` })
    speakText('名单已导出，请继续关注高风险对象。')
    scrollToBottom()
    return
  }
  if (action === '生成报告并下载') {
    exportToWord()
    messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: `${settingsForm.value.botName} 已整理好报告并发送了 Word 文档下载。` })
    speakText('报告已生成并发送。')
    scrollToBottom()
    return
  }

  const sourceInsight = msg?.insight || latestInsight.value || undefined

  const handled = await dispatchPlatformAction(router, action, {
    entities: buildActionEntities(sourceInsight),
    onUnhandled: (unhandledAction) => {
      ElMessage.success(`动作已记录：${unhandledAction}`)
    }
  })

  if (!handled) {
    ElMessage.success(`动作已记录：${action}`)
  }
}

const openEntity = async (entity: RelatedEntity) => {
  await navigatePlatformEntity(router, entity)
}

const renderChart = (domId: string) => {
  const dom = document.getElementById(domId)
  if (!dom) return
  const myChart = echarts.init(dom)
  const chartColor = getThemeChartColor()
  const trendPoints = abnormalTrend.value.length
    ? abnormalTrend.value
    : Array.from({ length: 6 }, (_, index) => ({ label: `${index + 1}`, value: 0 }))

  myChart.setOption({
    grid: { left: '2%', right: '2%', bottom: '5%', top: '15%', containLabel: true },
    tooltip: { trigger: 'axis' },
    legend: { data: ['收缩压', '舒张压'], top: 0, right: 0 },
    xAxis: { type: 'category', data: ['一', '二', '三', '四', '五', '六', '日'], axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: 'rgba(0,0,0,0.08)' } } },
    series: [
      { name: '收缩压', type: 'line', data: [120, 122, 145, 125, 118, 121, 120], smooth: true, lineStyle: { color: '#d14d72', width: 3 }, itemStyle: { color: '#d14d72' } },
      { name: '舒张压', type: 'line', data: [80, 82, 95, 85, 78, 80, 81], smooth: true, lineStyle: { color: chartColor, width: 3 }, itemStyle: { color: chartColor }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [ { offset: 0, color: `${chartColor}33` }, { offset: 1, color: `${chartColor}00` } ]) } }
    ]
  })
  myChart.setOption({
    legend: { data: ['异常事件'], top: 0, right: 0 },
    xAxis: { data: trendPoints.map(point => point.label) },
    series: [
      {
        name: '异常事件',
        type: 'line',
        data: trendPoints.map(point => Number(point.value || 0)),
        smooth: true,
        lineStyle: { color: chartColor, width: 3 },
        itemStyle: { color: chartColor },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${chartColor}33` },
            { offset: 1, color: `${chartColor}00` }
          ])
        }
      }
    ]
  })
}
const riskTagType = (riskLevel: RiskLevel) => {
  if (riskLevel === 'high') return 'danger'
  if (riskLevel === 'medium') return 'warning'
  return 'success'
}

const tableRowClassName = ({ rowIndex }: { rowIndex: number }) => (rowIndex % 2 === 1 ? 'glass-row-striped' : '')

const scrollToBottom = () => nextTick(() => {
  if (chatScroll.value) chatScroll.value.scrollTop = chatScroll.value.scrollHeight
})

const handleSearchClick = async () => {
  await openPlatformSearch(router, 'ai')
}

const handleNotificationItem = async (item: PlatformNotificationRecord) => {
  await openPlatformNotification(router, item)
}

const handleNotificationClick = async () => {
  await openAllPlatformNotifications(router, 'ai')
}

const renderDashboardCharts = () => {
  const riskDom = document.getElementById('riskChart')
  const trendDom = document.getElementById('trendChart')
  const chartColor = getThemeChartColor()
  const riskSummary = recentRiskSummary.value
  const trendPoints = abnormalTrend.value.length
    ? abnormalTrend.value
    : Array.from({ length: 6 }, (_, index) => ({ label: `${index + 1}`, value: 0 }))
  
  if (riskDom) {
    riskChartInstance.value?.dispose()
    const riskChart = echarts.init(riskDom)
    riskChartInstance.value = riskChart
    riskChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center', itemStyle: { borderWidth: 0 } },
      series: [
        {
          name: '风险分布',
          type: 'pie',
          radius: ['45%', '75%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false, position: 'center' },
          labelLine: { show: false },
          data: [
            { value: highRiskCount.value || 3, name: '高风险', itemStyle: { color: '#d14d72' } },
            { value: 12, name: '中风险', itemStyle: { color: '#fbbf24' } },
            { value: structuredInsightCount.value || 45, name: '低风险', itemStyle: { color: '#10b981' } }
          ]
        }
      ]
    })
    riskChart.setOption({
      series: [
        {
          data: [
            { value: riskSummary.high, name: '高风险', itemStyle: { color: '#d14d72' } },
            { value: riskSummary.medium, name: '中风险', itemStyle: { color: '#fbbf24' } },
            { value: riskSummary.low, name: '低风险', itemStyle: { color: '#10b981' } }
          ]
        }
      ]
    })
  }

  if (trendDom) {
    trendChartInstance.value?.dispose()
    const trendChart = echarts.init(trendDom)
    trendChartInstance.value = trendChart

    trendChart.setOption({
      grid: { left: '2%', right: '5%', bottom: '5%', top: '15%', containLabel: true },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00'], axisLine: { show: false }, axisTick: { show: false } },
      yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: 'rgba(0,0,0,0.04)' } } },
      series: [
        { 
          name: '解析量', 
          type: 'bar', 
          barWidth: '20px',
          itemStyle: { color: chartColor, borderRadius: [4, 4, 0, 0] },
          data: [12, 34, 45, 23, 56, 18] 
        }
      ]
    })
    trendChart.setOption({
      xAxis: { data: trendPoints.map(point => point.label) },
      series: [
        {
          name: '分析量',
          type: 'bar',
          data: trendPoints.map(point => Number(point.value || 0)),
          itemStyle: { color: chartColor, borderRadius: [4, 4, 0, 0] }
        }
      ]
    })
  }
}

watch(() => latestInsight.value, () => {
  refreshNotifications()
}, { immediate: true })

watch(
  () => [settingsForm.value.theme, abnormalTrend.value, recentAbnormalRows.value],
  () => {
    nextTick(() => {
      renderDashboardCharts()
    })
  },
  { deep: true }
)

watch(
  () => recentAbnormalRows.value[0]?.userId,
  (patientId) => {
    realtimeStream.subscribePatient(patientId ?? null)
  }
)

onMounted(async () => {
  refreshNotifications()
  await fetchWorkbenchDashboardData()
  nextTick(() => {
    renderDashboardCharts()
  })
  dashboardRefreshTimer.value = window.setInterval(async () => {
    await fetchWorkbenchDashboardData()
    nextTick(() => {
      renderDashboardCharts()
    })
  }, 30000)
})

onUnmounted(() => {
  if (dashboardRefreshTimer.value !== null) {
    window.clearInterval(dashboardRefreshTimer.value)
    dashboardRefreshTimer.value = null
  }
  clearRealtimeRefreshTimer()
  riskChartInstance.value?.dispose()
  trendChartInstance.value?.dispose()
  riskChartInstance.value = null
  trendChartInstance.value = null
})
</script>
<style scoped lang="scss">
.ai-command-center {
  --ai-primary: #0f172a;
  --ai-primary-soft: #f1f5f9;
  --ai-danger-soft: #fff1f2;
  --ai-success-soft: #ecfdf5;
  --ai-border: #e2e8f0;
  --ai-bg: #f8fafc;
  --ai-card: #ffffff;
  min-height: 100%;
  background: var(--ai-bg);
}

.ai-command-center :deep(.platform-page-card) {
  background: transparent;
}

.ai-command-center :deep(.platform-page-toolbar) {
  background: transparent;
  border-bottom: 1px solid var(--ai-border);
}

.ai-command-center :deep(.platform-page-aside) {
  background: transparent;
}

.ai-command-center.theme-blue {
  --ai-primary: #2563eb;
  --ai-primary-soft: #eff6ff;
}

.ai-command-center.theme-purple {
  --ai-primary: #4f46e5;
  --ai-primary-soft: #eef2ff;
}

.ai-command-center.theme-green {
  --ai-primary: #059669;
  --ai-primary-soft: #ecfdf5;
}

.ai-command-center.theme-pink {
  --ai-primary: #e11d48;
  --ai-primary-soft: #fff1f2;
}

.header-actions,
.toolbar-stack,
.aside-stack,
.entity-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.workbench-split-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
  align-items: stretch;
}

.workbench-left-pane {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.workbench-center-pane {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.metrics-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: 12px;
  background: var(--ai-card);
  border: 1px solid var(--ai-border);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.02);
}

.metric-item.danger-soft {
  background: var(--ai-danger-soft);
  border-color: #fecdd3;
}

.metric-item.danger-soft .metric-icon {
  background: #ffe4e6;
  color: #e11d48;
}

.metric-item.success-soft {
  background: var(--ai-success-soft);
  border-color: #a7f3d0;
}

.metric-item.success-soft .metric-icon {
  background: #d1fae5;
  color: #059669;
}

.metric-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-label {
  font-size: 11px;
  font-weight: 700;
  color: #667085;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.metric-value {
  font-size: 18px;
  line-height: 1.1;
  color: #14213d;
}

.metric-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--ai-primary-soft);
  color: var(--ai-primary);
  font-size: 16px;
}

.chart-panel {
  display: flex;
  flex-direction: column;
  padding: 16px;
  min-height: 240px;
}.workbench-split-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
  align-items: stretch;
}

.workbench-left-pane {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.workbench-center-pane {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.panel,
.inner-panel {
  border: 1px solid var(--ai-border);
  background: var(--ai-card);
  border-radius: 12px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.02);
}

.task-rail,
.conversation-panel,
.aside-card {
  padding: 18px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-head.compact {
  margin-bottom: 10px;
}

.section-head h3,
.section-head h4,
.conversation-header h3,
.insight-head h4 {
  margin: 4px 0 0;
  color: #14213d;
}

.mode-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.mode-chip,
.task-card,
.entity-chip,
.action-tile,
.chip-btn {
  border: 1px solid rgba(0,0,0,0.1);
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mode-chip:hover,
.task-card:hover,
.entity-chip:hover,
.action-tile:hover,
.chip-btn:hover {
  transform: translateY(-1px);
  border-color: var(--ai-primary);
  background: #fdfdfd;
  box-shadow: 0 4px 12px rgba(63, 60, 187, 0.04);
}

.mode-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 6px;
  text-align: left;
}

.mode-chip.active {
  background: var(--ai-primary-soft);
  border-color: rgba(63, 60, 187, 0.3);
}

.mode-chip-title {
  font-size: 14px;
  font-weight: 700;
  color: #14213d;
}

.mode-chip-desc {
  font-size: 12px;
  color: #667085;
  line-height: 1.5;
}

.task-card {
  width: 100%;
  padding: 16px;
  border-radius: 18px;
  text-align: left;
}

.task-card-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.task-card-top strong {
  color: #14213d;
}

.task-card-top span {
  font-size: 12px;
  color: var(--ai-primary);
  font-weight: 700;
}

.task-card p,
.note-list,
.preference-list {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #667085;
}

.note-card {
  padding: 16px;
}

.note-list,
.preference-list {
  padding-left: 18px;
}

.conversation-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 760px;
}

.conversation-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.chat-viewport {
  flex: 1;
  max-height: 720px;
  overflow: auto;
  padding-right: 6px;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-box {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-box.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--ai-primary-soft);
  color: var(--ai-primary);
  flex: 0 0 auto;
}

.msg-avatar.user {
  background: #f1f5f9;
  color: #475569;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.msg-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: min(100%, 840px);
}

.message-box.user .msg-content-wrapper {
  align-items: flex-end;
}

.msg-bubble {
  padding: 16px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  background: var(--ai-card);
  border: 1px solid var(--ai-border);
  min-width: 220px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.message-box.user .msg-bubble {
  background: var(--ai-primary-soft);
  border-color: transparent;
  color: #0f172a;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
}

.message-box.user .md-text {
  color: #0f172a;
}

.insight-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(209, 215, 224, 0.82);
}

.insight-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.insight-summary,
.md-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: #344054;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.insight-block {
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
}

.insight-block ul {
  margin: 10px 0 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.insight-block li,
.preference-list li {
  color: #475467;
  line-height: 1.6;
}.insight-block strong,
.preference-list strong,
.entity-chip strong {
  display: block;
  color: #14213d;
}

.widget-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.widget-header {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #14213d;
  font-weight: 700;
}

.echarts-container {
  width: 100%;
  height: 260px;
}

.action-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip-btn,
.action-tile,
.entity-chip {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13px;
  color: #14213d;
}

.entity-chip {
  text-align: left;
}

.entity-chip span {
  display: block;
  margin-top: 3px;
  color: #667085;
}

.floating-input-zone {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 16px;
  background: var(--ai-card);
}

.pill-input-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 16px;
  border: 1px solid var(--ai-border);
  background: var(--ai-bg);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  transition: border-color 0.2s;
}

.pill-input-box.focused {
  border-color: var(--ai-primary);
  background: #fff;
}

.input-header-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--ai-primary);
  font-weight: 600;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
}

.send-action-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: #e2e8f0;
  color: #64748b;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.2s;
}

.send-action-btn.ready {
  background: var(--ai-primary);
  color: #fff;
}

.sec-footer {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #94a3b8;
}

.aside-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preference-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.drawer-settings-content,
.drawer-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.inline-setting {
  display: flex;
  align-items: center;
  gap: 12px;
}

.typing {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.dot-flashing {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--ai-primary);
  box-shadow: 14px 0 0 rgba(63, 60, 187, 0.5), 28px 0 0 rgba(63, 60, 187, 0.25);
}

.spin {
  animation: spin 0.8s linear infinite;
}

.chat-flow-enter-active,
.chat-flow-leave-active,
.fade-enter-active,
.fade-leave-active,
.fade-bounce-enter-active,
.fade-bounce-leave-active {
  transition: all 0.25s ease;
}

.chat-flow-enter-from,
.chat-flow-leave-to,
.fade-enter-from,
.fade-leave-to,
.fade-bounce-enter-from,
.fade-bounce-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1280px) {
  .workbench-split-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .metrics-panel {
    grid-template-rows: auto;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .mode-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .metrics-panel,
  .mode-row,
  .insight-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .conversation-panel {
    min-height: auto;
  }

  .conversation-header,
  .dynamic-mascot-zone,
  .preference-list li {
    flex-direction: column;
    align-items: flex-start;
  }

  .pill-input-box {
    grid-template-columns: minmax(0, 1fr);
  }
}

.ai-focus-layout {
  max-width: 1040px;
  margin: 0 auto;
  width: 100%;
}

.toolbar-focus-pad {
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.metrics-pill-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.metric-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: var(--ai-card);
  border: 1px solid var(--ai-border);
  border-radius: 999px;
  font-size: 13px;
  color: #475569;
  box-shadow: 0 1px 2px 0 rgba(0,0,0,0.02);
}

.metric-pill.danger {
  background: var(--ai-danger-soft);
  color: #e11d48;
  border-color: #fecdd3;
}

.metric-pill.success {
  background: var(--ai-success-soft);
  color: #059669;
  border-color: #a7f3d0;
}

.conversation-panel {
  padding: 24px;
}

</style>

