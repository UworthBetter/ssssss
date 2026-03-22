<template>
  <div :class="['ai-command-center', `theme-${settingsForm.theme}`]">
    <PlatformPageShell
      title="AI 决策工作台"
      subtitle="将聊天回复升级为可追踪、可引用、可执行的分析结果。"
      eyebrow="AI 中心"
      aside-title="洞察面板"
      aside-width="360px"
    >
      <template #headerExtra>
        <div class="header-actions">
          <PlatformSearchEntry
            :label="searchPresentation.label"
            :placeholder="searchPresentation.placeholder"
            :hint="searchPresentation.hint"
            @click="handleSearchClick"
          />
          <el-button circle plain size="small" @click="showSettingsDrawer = true">
            <el-icon><Setting /></el-icon>
          </el-button>
        </div>
      </template>

      <template #toolbar>
        <div class="toolbar-stack">
          <PlatformContextFilterBar
            v-model="contextFilters"
            summary-label="当前分析范围"
            summary-value="AI 中心 / 风险研判与报告生成"
            @confirm="handleContextConfirm"
            @reset="handleContextReset"
          />

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

      <div class="summary-grid">
        <div class="summary-card panel">
          <span class="summary-label">分析轮次</span>
          <strong class="summary-value">{{ aiMessageCount }}</strong>
          <span class="summary-tip">基于当前会话累计生成。</span>
        </div>
        <div class="summary-card panel">
          <span class="summary-label">结构化结果</span>
          <strong class="summary-value">{{ structuredInsightCount }}</strong>
          <span class="summary-tip">AI 输出已关联证据与建议动作。</span>
        </div>
        <div class="summary-card panel danger-soft">
          <span class="summary-label">高风险判断</span>
          <strong class="summary-value">{{ highRiskCount }}</strong>
          <span class="summary-tip">建议优先复核并进入事件流程。</span>
        </div>
        <div class="summary-card panel success-soft">
          <span class="summary-label">待执行动作</span>
          <strong class="summary-value">{{ pendingActionCount }}</strong>
          <span class="summary-tip">可直接联动对象、事件、设备中心。</span>
        </div>
      </div>

      <div class="workbench-grid">
        <section class="task-rail panel">
          <div class="section-head">
            <div>
              <p class="section-eyebrow">分析任务</p>
              <h3>分析场景</h3>
            </div>
            <el-tag type="info" effect="light">工作台入口</el-tag>
          </div>

          <button
            v-for="task in quickTasks"
            :key="`${task.label}-rail`"
            type="button"
            class="task-card"
            @click="runQuickTask(task)"
          >
            <div class="task-card-top">
              <strong>{{ task.label }}</strong>
              <span>{{ task.tag }}</span>
            </div>
            <p>{{ task.description }}</p>
          </button>

          <div class="panel note-card inner-panel">
            <div class="section-head compact">
              <div>
                <p class="section-eyebrow">输出结构</p>
                <h4>这一阶段的升级重点</h4>
              </div>
            </div>
            <ul class="note-list">
              <li>结论必须可复核，而不只停留在聊天回复。</li>
              <li>证据、关联实体、建议动作需要一起呈现。</li>
              <li>现在已经支持跳转到对象中心、事件中心、设备中心。</li>
            </ul>
          </div>
        </section>

        <section class="conversation-panel panel">
          <header class="conversation-header">
            <div>
              <p class="section-eyebrow">分析线程</p>
              <h3>{{ settingsForm.botName }} AI 助理</h3>
              <span class="conversation-subtitle">保留对话方式，同时把每条 AI 回复升级为结构化决策卡。</span>
            </div>

            <el-dropdown trigger="click" @command="handleHeaderCommand">
              <el-button circle plain size="small">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="clear">
                    <el-icon><Delete /></el-icon> 清空当前会话
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon> 助理设置
                  </el-dropdown-item>
                  <el-dropdown-item command="exportChat" divided>
                    <el-icon><Document /></el-icon> 导出对话记录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </header>

          <div ref="chatScroll" class="chat-viewport">
            <transition-group name="chat-flow" tag="div" class="chat-list">
              <div v-for="msg in messages" :key="msg.id" :class="['message-box', msg.role]">
                <div class="msg-avatar" :class="msg.role">
                  <img v-if="msg.role === 'ai'" :src="bot3dImage" class="avatar-image bot-img-3d" alt="Bot" />
                  <el-icon v-else><Avatar /></el-icon>
                </div>

                <div class="msg-content-wrapper">
                  <div class="msg-bubble" :class="{ 'rich-media': msg.type !== 'text' || Boolean(msg.insight) }">
                    <div v-if="msg.insight" class="insight-card">
                      <div class="insight-head">
                        <div>
                          <p class="section-eyebrow">结构化结果</p>
                          <h4>{{ msg.insight.title }}</h4>
                        </div>
                        <el-tag :type="riskTagType(msg.insight.riskLevel)" effect="light">
                          {{ riskLabelMap[msg.insight.riskLevel] }}
                        </el-tag>
                      </div>

                      <p class="insight-summary">{{ msg.insight.summary }}</p>

                      <div class="insight-grid">
                        <div class="insight-block">
                          <span class="block-label">证据</span>
                          <ul>
                            <li v-for="evidence in msg.insight.evidence" :key="`${evidence.label}-${evidence.detail}`">
                              <strong>{{ evidence.label }}</strong>
                              <span>{{ evidence.detail }}</span>
                            </li>
                          </ul>
                        </div>

                        <div class="insight-block">
                          <span class="block-label">关联实体</span>
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
                        <span>{{ settingsForm.botName }} 为您整理的名单</span>
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
                        <span>相关健康趋势</span>
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
                  <img :src="bot3dImage" class="avatar-image bot-img-3d" alt="Bot" />
                </div>
                <div class="msg-content-wrapper">
                  <div class="msg-bubble typing">
                    <span class="typing-text">{{ settingsForm.botName }} 正在生成结构化分析</span>
                    <div class="dot-flashing"></div>
                  </div>
                </div>
              </div>
            </transition-group>
          </div>

          <div class="floating-input-zone">
            <div class="dynamic-mascot-zone" :class="{ 'is-thinking': isTyping }">
              <transition name="fade-bounce">
                <div v-if="isTyping" class="mascot-bubble">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  正在快速分析...
                </div>
                <div v-else-if="isInputFocused" class="mascot-bubble">
                  <el-icon><Microphone /></el-icon>
                  正在监听输入...
                </div>
              </transition>

              <div class="mascot-body" @click="pokeMascot">
                <img :src="bot3dImage" alt="3D Bot" />
              </div>
            </div>

            <div class="pill-input-box" :class="{ focused: isInputFocused }">
              <el-input
                v-model="inputText"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 3 }"
                resize="none"
                :placeholder="`向 ${settingsForm.botName} 发起分析，获取结论、证据和动作建议...`"
                @focus="isInputFocused = true"
                @blur="isInputFocused = false"
                @keyup.enter.exact.prevent="sendMessage"
              />
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

            <div class="sec-footer">
              <el-icon><Lock /></el-icon>
              医疗级数据保护 · {{ settingsForm.botName }} 决策引擎
            </div>
          </div>
        </section>
      </div>

      <template #aside>
        <div class="aside-stack">
          <PlatformNotificationEntry
            title="AI 协同通知"
            :unread-count="notificationItems.length"
            :items="notificationItems"
            @click-item="handleNotificationItem"
            @click-all="handleNotificationClick"
          />

          <div class="panel aside-card">
            <div class="section-head compact">
              <div>
                <p class="section-eyebrow">最新结果</p>
                <h4>最新结构化结果</h4>
              </div>
              <el-tag v-if="latestInsight" :type="riskTagType(latestInsight.riskLevel)" effect="light">
                {{ riskLabelMap[latestInsight.riskLevel] }}
              </el-tag>
            </div>

            <template v-if="latestInsight">
              <p class="aside-summary">{{ latestInsight.summary }}</p>

              <div class="aside-block">
                <span class="block-label">关联实体</span>
                <div class="entity-stack">
                  <button
                    v-for="entity in latestInsight.entities"
                    :key="`${entity.kind}-${entity.value}-aside`"
                    type="button"
                    class="entity-chip"
                    @click="openEntity(entity)"
                  >
                    <strong>{{ entity.name }}</strong>
                    <span>{{ entity.kindLabel }} · {{ entity.value }}</span>
                  </button>
                </div>
              </div>

              <div class="aside-block">
                <span class="block-label">建议动作</span>
                <div class="entity-stack">
                  <button
                    v-for="action in latestInsight.recommendedActions"
                    :key="`${action}-aside`"
                    type="button"
                    class="action-tile"
                    @click="handleAction(action)"
                  >
                    {{ action }}
                  </button>
                </div>
              </div>
            </template>

            <p v-else class="empty-copy">执行一次分析后，这里会展示最新的证据、关联实体和建议动作。</p>
          </div>

          <div class="panel aside-card">
            <div class="section-head compact">
              <div>
                <p class="section-eyebrow">偏好设置</p>
                <h4>助理配置</h4>
              </div>
            </div>
            <ul class="preference-list">
              <li>
                <span>助理名称</span>
                <strong>{{ settingsForm.botName }}</strong>
              </li>
              <li>
                <span>输出语气</span>
                <strong>{{ toneLabelMap[settingsForm.tone] }}</strong>
              </li>
              <li>
                <span>自动朗读</span>
                <strong>{{ settingsForm.autoSpeak ? '开启' : '关闭' }}</strong>
              </li>
            </ul>
          </div>
        </div>
      </template>
    </PlatformPageShell>

    <el-drawer
      v-model="showSettingsDrawer"
      title="助理设置"
      direction="rtl"
      size="380px"
      class="glass-drawer"
      :show-close="true"
    >
      <div class="drawer-settings-content">
        <el-form label-position="top" :model="settingsForm">
          <el-form-item label="助理名称" class="setting-item">
            <el-input v-model="settingsForm.botName" placeholder="给助理起个名字" clearable>
              <template #prefix><el-icon><Service /></el-icon></template>
            </el-input>
            <div class="form-tip">该名称会同步显示在页面头部和 AI 回复中。</div>
          </el-form-item>

          <el-form-item label="输出语气" class="setting-item">
            <el-radio-group v-model="settingsForm.tone" class="custom-radio-group">
              <el-radio-button label="professional">专业严谨</el-radio-button>
              <el-radio-button label="lively">活泼亲切</el-radio-button>
              <el-radio-button label="concise">简明扼要</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="工作台主题" class="setting-item">
            <el-select v-model="settingsForm.theme">
              <el-option label="紫色霓虹" value="purple" />
              <el-option label="蓝色冷静" value="blue" />
              <el-option label="绿色健康" value="green" />
              <el-option label="粉色活力" value="pink" />
            </el-select>
          </el-form-item>

          <el-form-item label="语音播报" class="setting-item inline-setting">
            <el-switch v-model="settingsForm.autoSpeak" />
            <span class="setting-inline-tip">自动朗读结构化结论</span>
          </el-form-item>
        </el-form>

        <div class="drawer-actions">
          <el-button @click="showSettingsDrawer = false">取消</el-button>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Avatar,
  Delete,
  Document,
  List,
  Loading,
  Lock,
  Microphone,
  MoreFilled,
  Position,
  Service,
  Setting,
  TrendCharts
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { chatAi } from '@/api/ai'
import {
  PlatformContextFilterBar,
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
  type PlatformContextFilters,
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
const contextFilters = ref<PlatformContextFilters>({
  timeRange: 'today',
  region: 'all',
  riskLevel: 'all',
  status: 'processing'
})

const settingsForm = ref({
  botName: '小豆',
  tone: 'lively',
  autoSpeak: false,
  theme: 'purple'
})

const bot3dImage = 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Robot.png'
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

const aiMessageCount = computed(() => messages.value.filter((msg) => msg.role === 'ai').length)
const structuredInsightCount = computed(() => messages.value.filter((msg) => Boolean(msg.insight)).length)
const highRiskCount = computed(() => messages.value.filter((msg) => msg.insight?.riskLevel === 'high').length)
const latestInsight = computed(() => [...messages.value].reverse().find((msg) => msg.insight)?.insight ?? null)
const pendingActionCount = computed(() => latestInsight.value?.recommendedActions.length ?? 0)
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
}

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
      { label: '当前上下文', detail: `${contextFilters.value.timeRange} / ${contextFilters.value.region} / ${contextFilters.value.riskLevel}` }
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
  const chartColor = settingsForm.value.theme === 'purple' ? '#4f46e5' : settingsForm.value.theme === 'pink' ? '#e11d48' : settingsForm.value.theme === 'green' ? '#059669' : '#2563eb'

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

const handleContextConfirm = () => {
  ElMessage.success('分析范围已应用')
}

const handleContextReset = () => {
  contextFilters.value = { timeRange: 'today', region: 'all', riskLevel: 'all', status: 'processing' }
}

watch(() => latestInsight.value, () => {
  refreshNotifications()
}, { immediate: true })

onMounted(() => {
  refreshNotifications()
})
</script>
<style scoped lang="scss">
.ai-command-center {
  --ai-primary: #3f3cbb;
  --ai-primary-soft: rgba(63, 60, 187, 0.08);
  --ai-danger-soft: rgba(209, 77, 114, 0.12);
  --ai-success-soft: rgba(28, 126, 96, 0.12);
  --ai-border: rgba(209, 215, 224, 0.92);
  --ai-bg: linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
  --ai-card: rgba(255, 255, 255, 0.86);
  min-height: 100%;
  background: var(--ai-bg);
}

.ai-command-center :deep(.platform-page-card) {
  background: transparent;
}

.ai-command-center :deep(.platform-page-toolbar) {
  background: rgba(255, 255, 255, 0.74);
}

.ai-command-center :deep(.platform-page-aside) {
  background: rgba(255, 255, 255, 0.82);
}

.ai-command-center.theme-blue {
  --ai-primary: #2563eb;
  --ai-primary-soft: rgba(37, 99, 235, 0.08);
}

.ai-command-center.theme-green {
  --ai-primary: #059669;
  --ai-primary-soft: rgba(5, 150, 105, 0.08);
}

.ai-command-center.theme-pink {
  --ai-primary: #e11d48;
  --ai-primary-soft: rgba(225, 29, 72, 0.08);
}

.header-actions,
.toolbar-stack,
.aside-stack,
.entity-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 18px;
  border-radius: 18px;
  background: var(--ai-card);
  border: 1px solid var(--ai-border);
}

.summary-card.danger-soft {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), var(--ai-danger-soft));
}

.summary-card.success-soft {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), var(--ai-success-soft));
}

.summary-label,
.summary-tip,
.section-eyebrow,
.block-label,
.conversation-subtitle,
.aside-summary,
.form-tip,
.setting-inline-tip,
.empty-copy {
  color: #667085;
}

.summary-label,
.section-eyebrow,
.block-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.summary-value {
  font-size: 28px;
  line-height: 1;
  color: #14213d;
}

.summary-tip,
.conversation-subtitle,
.aside-summary,
.form-tip,
.setting-inline-tip,
.empty-copy {
  font-size: 13px;
  line-height: 1.6;
}.workbench-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.panel,
.inner-panel {
  border: 1px solid var(--ai-border);
  background: var(--ai-card);
  border-radius: 20px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
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
  border: 1px solid var(--ai-border);
  background: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.mode-chip:hover,
.task-card:hover,
.entity-chip:hover,
.action-tile:hover,
.chip-btn:hover {
  transform: translateY(-1px);
  border-color: rgba(63, 60, 187, 0.28);
  box-shadow: 0 12px 24px rgba(63, 60, 187, 0.08);
}

.mode-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 16px;
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
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--ai-primary-soft);
  color: var(--ai-primary);
  flex: 0 0 auto;
}

.msg-avatar.user {
  background: rgba(20, 33, 61, 0.08);
  color: #14213d;
}

.avatar-image,
.mascot-body img {
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
  border-radius: 18px;
  background: #fff;
  border: 1px solid var(--ai-border);
  min-width: 220px;
}

.message-box.user .msg-bubble {
  background: linear-gradient(135deg, var(--ai-primary) 0%, #6d5efc 100%);
  color: #fff;
}

.message-box.user .md-text {
  color: #fff;
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
  padding-top: 8px;
  border-top: 1px solid rgba(209, 215, 224, 0.82);
}

.dynamic-mascot-zone {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mascot-body {
  width: 64px;
  height: 64px;
  padding: 8px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(63, 60, 187, 0.12), rgba(99, 102, 241, 0.04));
  cursor: pointer;
}

.mascot-bubble {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 999px;
  background: var(--ai-primary-soft);
  color: var(--ai-primary);
  font-size: 13px;
  font-weight: 700;
}

.pill-input-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  padding: 12px;
  border-radius: 20px;
  border: 1px solid var(--ai-border);
  background: #fff;
}

.pill-input-box.focused {
  border-color: rgba(63, 60, 187, 0.34);
  box-shadow: 0 0 0 4px rgba(63, 60, 187, 0.08);
}

.send-action-btn {
  width: 46px;
  height: 46px;
  border-radius: 16px;
  border: none;
  background: rgba(20, 33, 61, 0.1);
  color: #14213d;
  cursor: pointer;
}

.send-action-btn.ready {
  background: var(--ai-primary);
  color: #fff;
}

.sec-footer {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #667085;
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
  .summary-grid,
  .mode-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .workbench-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .summary-grid,
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
</style>

