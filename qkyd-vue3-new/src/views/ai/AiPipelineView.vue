<template>
  <div class="ai-pipeline">
    <!-- ========== 顶部事件滚动条 ========== -->
    <header class="ticker-bar">
      <span class="ticker-label">实时异常</span>
      <div class="ticker-track">
        <div class="ticker-content" :style="{ animationDuration: tickerEvents.length ? `${tickerEvents.length * 4}s` : '20s' }">
          <span v-for="(ev, i) in [...tickerEvents, ...tickerEvents]" :key="i" class="ticker-item" :class="{ active: selectedEventId === ev.id }" @click="selectEvent(ev)">
            <span class="ticker-icon">{{ typeIcon(ev.type) }}</span>
            <span class="ticker-type">{{ ev.type || '未知' }}</span>
            <span class="ticker-time">{{ fmtTime(ev.createTime) }}</span>
            <span class="ticker-name">{{ ev.nickName || '-' }}</span>
          </span>
        </div>
      </div>
    </header>

    <!-- ========== 流水线节点 ========== -->
    <section class="pipeline-row">
      <div v-for="(node, idx) in pipelineNodes" :key="node.key" class="pipeline-node" :class="[node.status, { selected: activeNode === node.key }]">
        <div class="node-icon-ring" @click="activeNode = node.key">
          <div class="node-icon-inner">
            <el-icon v-if="node.status === 'processing'" class="spin"><Loading /></el-icon>
            <el-icon v-else-if="node.status === 'completed'"><Check /></el-icon>
            <span v-else class="node-idx">{{ idx + 1 }}</span>
          </div>
        </div>
        <div class="node-label">
          <span class="node-title">{{ node.title }}</span>
          <span class="node-sub">{{ node.sub }}</span>
        </div>
        <div v-if="idx < pipelineNodes.length - 1" class="node-connector">
          <div class="connector-line" :class="{ active: node.status === 'completed' || node.status === 'processing' }" />
          <svg class="connector-arrow" :class="{ active: node.status === 'completed' || node.status === 'processing' }" viewBox="0 0 24 24"><path d="M5 12h14m-4-4 4 4-4 4" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
      </div>
    </section>

    <!-- ========== 下方双栏 ========== -->
    <div class="detail-grid">
      <!-- 左栏：节点详情 -->
      <main class="detail-panel">
        <!-- ① 数据采集 -->
        <div v-show="activeNode === 'collect'" class="node-detail">
          <h3 class="detail-title"><span class="detail-badge cyan">①</span> 实时体征数据采集</h3>
          <p class="detail-desc">可穿戴设备通过蓝牙/WiFi 上传心率、血氧、体温、步数等体征数据，采样频率可配置。</p>
          <div id="heartRateChart" class="chart-box" />
          <div class="metric-row">
            <div class="metric-card"><span class="metric-label">当前心率</span><strong class="metric-value cyan">{{ currentHR }} bpm</strong></div>
            <div class="metric-card"><span class="metric-label">血氧饱和度</span><strong class="metric-value green">{{ currentSpO2 }}%</strong></div>
            <div class="metric-card"><span class="metric-label">体温</span><strong class="metric-value amber">{{ currentTemp }}°C</strong></div>
            <div class="metric-card"><span class="metric-label">今日步数</span><strong class="metric-value purple">{{ currentSteps }}</strong></div>
          </div>
        </div>

        <!-- ② 异常检测 -->
        <div v-show="activeNode === 'detect'" class="node-detail">
          <h3 class="detail-title"><span class="detail-badge red">②</span> 滑动窗口异常检测</h3>
          <p class="detail-desc">基于滑动窗口均值偏离算法，当心率偏离最近 N 个采样点均值超过阈值时触发异常告警。</p>
          <div id="detectChart" class="chart-box" />
          <div class="algo-params">
            <div class="param-item"><span class="param-key">窗口大小</span><span class="param-val">10 个采样点</span></div>
            <div class="param-item"><span class="param-key">偏离阈值</span><span class="param-val">30%</span></div>
            <div class="param-item"><span class="param-key">检测到异常</span><span class="param-val red">{{ detectedAnomalies.length }} 次</span></div>
            <div class="param-item"><span class="param-key">最大偏离</span><span class="param-val red">{{ maxDeviation }}%</span></div>
          </div>
        </div>

        <!-- ③ AI 分析 -->
        <div v-show="activeNode === 'analyze'" class="node-detail">
          <h3 class="detail-title"><span class="detail-badge green">③</span> DeepSeek 医学分析</h3>
          <p class="detail-desc">将异常事件上下文发送至 DeepSeek LLM，结合医学知识库生成专业分析报告。</p>
          <div class="ai-prompt-box">
            <div class="prompt-header"><span class="prompt-label">Prompt</span></div>
            <pre class="prompt-text">{{ aiPromptText }}</pre>
          </div>
          <div class="ai-response-box">
            <div class="prompt-header"><span class="prompt-label">AI Response</span><span v-if="aiTyping" class="typing-badge">生成中...</span></div>
            <div class="response-text" v-html="aiResponseHtml" />
          </div>
        </div>

        <!-- ④ 风险评估 -->
        <div v-show="activeNode === 'assess'" class="node-detail">
          <h3 class="detail-title"><span class="detail-badge amber">④</span> ML 风险评分</h3>
          <p class="detail-desc">scikit-learn 逻辑回归模型综合心率、血氧、体温、活动量、历史异常等多维特征预测风险等级。</p>
          <div class="assess-row">
            <div id="gaugeChart" class="chart-box-sm" />
            <div id="radarChart" class="chart-box-sm" />
          </div>
          <div class="risk-info-row">
            <div class="risk-tag" :class="riskLevel">{{ riskLabel }}</div>
            <div class="confidence-bar-wrap">
              <span class="confidence-label">模型置信度</span>
              <div class="confidence-track"><div class="confidence-fill" :style="{ width: confidence + '%' }" /></div>
              <span class="confidence-val">{{ confidence }}%</span>
            </div>
          </div>
        </div>

        <!-- ⑤ 处置建议 -->
        <div v-show="activeNode === 'action'" class="node-detail">
          <h3 class="detail-title"><span class="detail-badge purple">⑤</span> 结构化处置建议</h3>
          <p class="detail-desc">基于 AI 分析和风险评估结果，自动生成可执行的处置建议动作，支持联动对象中心、事件中心闭环。</p>
          <div class="action-cards">
            <div v-for="(act, i) in suggestedActions" :key="i" class="action-card">
              <div class="action-icon">{{ act.icon }}</div>
              <div class="action-body">
                <strong>{{ act.title }}</strong>
                <p>{{ act.desc }}</p>
              </div>
              <el-button type="primary" size="small" round @click="handleAction(act)">执行</el-button>
            </div>
          </div>
        </div>
      </main>

      <!-- 右栏：AI 对话 -->
      <aside class="chat-panel">
        <div class="chat-header">
          <span class="chat-title">AI 决策助手</span>
          <el-tag size="small" effect="dark" type="info">在线</el-tag>
        </div>
        <div class="chat-messages">
          <div v-for="msg in chatMessages" :key="msg.id" :class="['chat-msg', msg.role]">
            <div class="chat-bubble" v-html="msg.content" />
          </div>
          <div v-if="chatTyping" class="chat-msg ai">
            <div class="chat-bubble typing-bubble"><span class="dot-flashing-mini" /></div>
          </div>
        </div>
        <div class="chat-input-row">
          <el-input v-model="chatInput" placeholder="向 AI 提问..." @keyup.enter="sendChat" />
          <el-button type="primary" :loading="chatTyping" @click="sendChat">发送</el-button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, nextTick, watch, computed } from 'vue'
import { Check, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { chatAi } from '@/api/ai'
import { listExceptions, type ExceptionAlert } from '@/api/health'

/* ========== 类型 ========== */
interface TickerEvent { id?: number; type?: string; createTime?: string; nickName?: string; userId?: number; value?: string; deviceId?: number; state?: string; location?: string }
interface ChatMsg { id: number; role: 'user' | 'ai'; content: string }

/* ========== 流水线节点 ========== */
const pipelineNodes = ref([
  { key: 'collect', title: '数据采集', sub: 'IoT 设备上报', status: 'idle' },
  { key: 'detect', title: '异常检测', sub: '滑动窗口算法', status: 'idle' },
  { key: 'analyze', title: 'AI 分析', sub: 'DeepSeek LLM', status: 'idle' },
  { key: 'assess', title: '风险评估', sub: 'ML 模型评分', status: 'idle' },
  { key: 'action', title: '处置建议', sub: '闭环执行', status: 'idle' }
])

const activeNode = ref('collect')
const tickerEvents = ref<TickerEvent[]>([])
const selectedEventId = ref<number | undefined>()
const cycleTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const waveTimer = ref<ReturnType<typeof setInterval> | null>(null)

/* ========== 心率模拟数据 ========== */
const heartRateHistory = ref<number[]>([])
const MAX_POINTS = 30
const currentHR = ref(72)
const currentSpO2 = ref(98)
const currentTemp = ref(36.5)
const currentSteps = ref(3842)
const detectedAnomalies = ref<{ idx: number; value: number; deviation: number }[]>([])
const maxDeviation = computed(() => detectedAnomalies.value.length ? Math.round(Math.max(...detectedAnomalies.value.map(a => a.deviation))) : 0)

/* ========== AI 分析 ========== */
const aiPromptText = ref('')
const aiResponseHtml = ref('')
const aiTyping = ref(false)

/* ========== 风险评估 ========== */
const riskScore = ref(42)
const riskLevel = ref('medium')
const riskLabel = ref('中风险')
const confidence = ref(76)

/* ========== 处置建议 ========== */
const suggestedActions = ref([
  { icon: '🩺', title: '通知医护人员', desc: '向值班护士发送异常预警通知，建议 15 分钟内现场查看。', action: 'notify_medical' },
  { icon: '📋', title: '创建巡检工单', desc: '系统自动创建健康巡检工单，安排照护人员跟进。', action: 'create_task' },
  { icon: '📱', title: '联系家属', desc: '通过短信/App 推送通知监护人家属当前异常情况。', action: 'contact_family' },
  { icon: '📊', title: '生成健康报告', desc: '将本次异常及处理过程记录到个人健康档案。', action: 'gen_report' },
  { icon: '⚙️', title: '调整监测频率', desc: '提高该对象的设备采样频率至每 5 分钟一次。', action: 'adjust_freq' }
])

/* ========== 对话 ========== */
const chatMessages = ref<ChatMsg[]>([
  { id: 1, role: 'ai', content: '你好，我是 AI 决策助手。我会实时分析异常事件并给出处置建议。' }
])
const chatInput = ref('')
const chatTyping = ref(false)

/* ========== 工具函数 ========== */
const typeIcon = (type?: string) => {
  if (!type) return '⚠️'
  const t = type.toLowerCase()
  if (t.includes('心率') || t.includes('heart')) return '❤️'
  if (t.includes('血氧') || t.includes('spo2')) return '🫁'
  if (t.includes('体温') || t.includes('temp')) return '🌡️'
  if (t.includes('离线') || t.includes('offline')) return '📡'
  if (t.includes('sos') || t.includes('求救')) return '🆘'
  if (t.includes('围栏') || t.includes('fence')) return '🚧'
  return '⚠️'
}

const fmtTime = (t?: string) => {
  if (!t) return ''
  const d = new Date(t)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const randRange = (min: number, max: number) => Math.round(min + Math.random() * (max - min))

/* ========== ECharts 实例 ========== */
let hrChart: echarts.ECharts | null = null
let detectChart: echarts.ECharts | null = null
let gaugeChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null

const C = { cyan: '#00f0ff', green: '#00ff88', red: '#ff4757', amber: '#ffa502', purple: '#a29bfe', grid: 'rgba(255,255,255,0.06)', text: '#8892b0' }

/* ========== 心率波形图 ========== */
const initHRChart = () => {
  const dom = document.getElementById('heartRateChart')
  if (!dom) return
  if (hrChart) hrChart.dispose()
  hrChart = echarts.init(dom)
  hrChart.setOption({
    backgroundColor: 'transparent',
    grid: { left: '3%', right: '3%', bottom: '8%', top: '8%', containLabel: true },
    tooltip: { trigger: 'axis', backgroundColor: '#1a1e2e', borderColor: '#2d3561', textStyle: { color: '#e0e0e0' } },
    xAxis: { type: 'category', data: Array.from({ length: MAX_POINTS }, (_, i) => i + 1), axisLine: { lineStyle: { color: C.grid } }, axisLabel: { color: C.text, fontSize: 10 } },
    yAxis: { type: 'value', min: 50, max: 120, axisLine: { show: false }, splitLine: { lineStyle: { color: C.grid } }, axisLabel: { color: C.text, fontSize: 10 } },
    series: [{
      name: '心率', type: 'line', data: heartRateHistory.value, smooth: true, symbol: 'none',
      lineStyle: { color: C.cyan, width: 2, shadowColor: C.cyan, shadowBlur: 8 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,240,255,0.25)' }, { offset: 1, color: 'rgba(0,240,255,0)' }]) },
      markLine: { silent: true, symbol: 'none', lineStyle: { color: C.red, type: 'dashed', width: 1 }, data: [{ yAxis: 100, label: { formatter: '告警线', color: C.red, fontSize: 10 } }] }
    }]
  })
}

const pushHRData = (hr: number) => {
  heartRateHistory.value.push(hr)
  if (heartRateHistory.value.length > MAX_POINTS) heartRateHistory.value.shift()
  currentHR.value = hr
  if (hrChart) {
    hrChart.setOption({
      xAxis: { data: Array.from({ length: heartRateHistory.value.length }, (_, i) => i + 1) },
      series: [{ data: heartRateHistory.value }]
    })
  }
}

/* ========== 异常检测图 ========== */
const initDetectChart = () => {
  const dom = document.getElementById('detectChart')
  if (!dom) return
  if (detectChart) detectChart.dispose()
  detectChart = echarts.init(dom)
  const normalData = heartRateHistory.value.map((v, i) => detectedAnomalies.value.some(a => a.idx === i) ? null : v)
  const anomalyData = heartRateHistory.value.map((v, i) => detectedAnomalies.value.some(a => a.idx === i) ? v : null)
  detectChart.setOption({
    backgroundColor: 'transparent',
    grid: { left: '3%', right: '3%', bottom: '8%', top: '10%', containLabel: true },
    tooltip: { trigger: 'axis', backgroundColor: '#1a1e2e', borderColor: '#2d3561', textStyle: { color: '#e0e0e0' } },
    legend: { data: ['正常', '异常'], textStyle: { color: C.text }, top: 0 },
    xAxis: { type: 'category', data: Array.from({ length: heartRateHistory.value.length }, (_, i) => i + 1), axisLine: { lineStyle: { color: C.grid } }, axisLabel: { color: C.text, fontSize: 10 } },
    yAxis: { type: 'value', min: 50, max: 120, axisLine: { show: false }, splitLine: { lineStyle: { color: C.grid } }, axisLabel: { color: C.text, fontSize: 10 } },
    series: [
      { name: '正常', type: 'bar', data: normalData, itemStyle: { color: C.green, borderRadius: [3, 3, 0, 0] }, barWidth: '40%' },
      { name: '异常', type: 'scatter', data: anomalyData, symbolSize: 16, itemStyle: { color: C.red, shadowColor: C.red, shadowBlur: 10 } }
    ]
  })
}

/* ========== 风险仪表盘 ========== */
const initGaugeChart = () => {
  const dom = document.getElementById('gaugeChart')
  if (!dom) return
  if (gaugeChart) gaugeChart.dispose()
  gaugeChart = echarts.init(dom)
  gaugeChart.setOption({
    backgroundColor: 'transparent',
    series: [{
      type: 'gauge', startAngle: 220, endAngle: -40, min: 0, max: 100,
      pointer: { show: true, length: '60%', width: 4, itemStyle: { color: 'auto' } },
      progress: { show: true, width: 12, roundCap: true },
      axisLine: { lineStyle: { width: 12, color: [[0.4, C.green], [0.7, C.amber], [1, C.red]] } },
      axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
      detail: { valueAnimation: true, fontSize: 28, fontWeight: 700, color: '#e0e0e0', offsetCenter: [0, '60%'], formatter: '{value}' },
      data: [{ value: riskScore.value, name: '风险分' }],
      title: { fontSize: 13, color: C.text, offsetCenter: [0, '85%'] }
    }]
  })
}

/* ========== 雷达图 ========== */
const initRadarChart = () => {
  const dom = document.getElementById('radarChart')
  if (!dom) return
  if (radarChart) radarChart.dispose()
  radarChart = echarts.init(dom)
  radarChart.setOption({
    backgroundColor: 'transparent',
    radar: {
      indicator: [
        { name: '心率', max: 100 }, { name: '血氧', max: 100 },
        { name: '体温', max: 100 }, { name: '活动量', max: 100 }, { name: '历史异常', max: 100 }
      ],
      axisName: { color: C.text, fontSize: 11 },
      splitArea: { areaStyle: { color: ['rgba(0,240,255,0.02)', 'rgba(0,240,255,0.05)'] } },
      axisLine: { lineStyle: { color: C.grid } },
      splitLine: { lineStyle: { color: C.grid } }
    },
    series: [{
      type: 'radar', data: [{
        value: [randRange(30, 90), randRange(20, 60), randRange(10, 50), randRange(20, 80), randRange(10, 70)],
        areaStyle: { color: 'rgba(0,240,255,0.2)' },
        lineStyle: { color: C.cyan, width: 2 },
        itemStyle: { color: C.cyan }
      }]
    }]
  })
}

/* ========== 流水线动画 ========== */
const resetNodes = () => { pipelineNodes.value.forEach(n => { n.status = 'idle' }) }

const runPipeline = async (event?: TickerEvent) => {
  resetNodes()
  pipelineNodes.value[0].status = 'processing'
  activeNode.value = 'collect'
  await sleep(2000)
  heartRateHistory.value = Array.from({ length: MAX_POINTS }, () => randRange(62, 85))
  const anomalyIndices = [randRange(22, 26), randRange(10, 14)]
  anomalyIndices.forEach(idx => {
    if (heartRateHistory.value[idx] !== undefined) {
      const v = randRange(105, 118)
      heartRateHistory.value[idx] = v
      detectedAnomalies.value.push({ idx, value: v, deviation: randRange(32, 45) })
    }
  })
  currentHR.value = heartRateHistory.value[heartRateHistory.value.length - 1]
  pipelineNodes.value[0].status = 'completed'
  await nextTick(); initHRChart()

  pipelineNodes.value[1].status = 'processing'
  activeNode.value = 'detect'
  await sleep(1500)
  pipelineNodes.value[1].status = 'completed'
  await nextTick(); initDetectChart()

  pipelineNodes.value[2].status = 'processing'
  activeNode.value = 'analyze'
  const en = event?.nickName || '张建国'
  const et = event?.type || '心率异常'
  const ev = event?.value || '112bpm（偏离均值 35%）'
  aiPromptText.value = `当前监测到异常事件：对象「${en}」，类型「${et}」，指标值「${ev}」。请结合医学知识分析可能的健康风险原因，并给出处置建议。`
  aiResponseHtml.value = ''
  aiTyping.value = true
  try {
    const res = await chatAi(aiPromptText.value)
    const text = typeof res?.data === 'string' ? res.data : JSON.stringify(res?.data || '', null, 2)
    await typewriterEffect(text, 15)
  } catch {
    await typewriterEffect(`检测到 ${en}（${et}），当前指标值 ${ev}。结合历史数据，该对象近 7 天内出现 ${randRange(1, 5)} 次类似异常。\n\n建议：\n1. 立即通知值班医护人员现场查看\n2. 调高该对象的设备采样频率\n3. 联系家属告知当前情况\n\n风险等级：中风险（置信度 ${randRange(65, 90)}%）`, 15)
  }
  aiTyping.value = false
  pipelineNodes.value[2].status = 'completed'

  pipelineNodes.value[3].status = 'processing'
  activeNode.value = 'assess'
  riskScore.value = randRange(35, 85)
  confidence.value = randRange(60, 95)
  if (riskScore.value > 70) { riskLevel.value = 'high'; riskLabel.value = '高风险' }
  else if (riskScore.value > 40) { riskLevel.value = 'medium'; riskLabel.value = '中风险' }
  else { riskLevel.value = 'low'; riskLabel.value = '低风险' }
  await sleep(1000)
  initGaugeChart(); initRadarChart()
  pipelineNodes.value[3].status = 'completed'

  pipelineNodes.value[4].status = 'processing'
  activeNode.value = 'action'
  await sleep(800)
  pipelineNodes.value[4].status = 'completed'
}

const typewriterEffect = (text: string, interval: number): Promise<void> => {
  return new Promise(resolve => {
    const safe = text.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br/>')
    let idx = 0; aiResponseHtml.value = ''
    const timer = setInterval(() => {
      if (idx >= safe.length) { clearInterval(timer); resolve(); return }
      aiResponseHtml.value += safe[idx]; idx++
    }, interval)
  })
}

const sleep = (ms: number) => new Promise(r => setTimeout(r, ms))

/* ========== 事件选择 ========== */
const selectEvent = async (ev: TickerEvent) => {
  selectedEventId.value = ev.id
  detectedAnomalies.value = []
  await runPipeline(ev)
}

/* ========== 动作处理 ========== */
const handleAction = (act: typeof suggestedActions.value[0]) => { ElMessage.success(`动作已记录：${act.title}`) }

/* ========== 聊天 ========== */
const sendChat = async () => {
  const text = chatInput.value.trim()
  if (!text || chatTyping.value) return
  chatMessages.value.push({ id: Date.now(), role: 'user', content: text.replace(/</g, '&lt;') })
  chatInput.value = ''; chatTyping.value = true
  try {
    const res = await chatAi(text)
    const reply = typeof res?.data === 'string' ? res.data : '分析完成。'
    chatMessages.value.push({ id: Date.now(), role: 'ai', content: reply.replace(/</g, '&lt;').replace(/\n/g, '<br/>') })
  } catch {
    chatMessages.value.push({ id: Date.now(), role: 'ai', content: '服务暂时不可用，请稍后重试。' })
  } finally {
    chatTyping.value = false
    if (chatMessages.value.length > 10) chatMessages.value = chatMessages.value.slice(-10)
  }
}

/* ========== 实时波形模拟 ========== */
const startWaveSim = () => {
  for (let i = 0; i < MAX_POINTS; i++) heartRateHistory.value.push(randRange(65, 82))
  currentSpO2.value = randRange(95, 99); currentTemp.value = +(36 + Math.random() * 0.8).toFixed(1); currentSteps.value = randRange(2000, 6000)
  waveTimer.value = setInterval(() => {
    pushHRData(randRange(62, 85))
    if (Math.random() < 0.1) currentSpO2.value = randRange(95, 99)
    if (Math.random() < 0.05) currentTemp.value = +(36 + Math.random() * 0.8).toFixed(1)
    if (Math.random() < 0.08) currentSteps.value += randRange(5, 30)
  }, 2000)
}

/* ========== 加载真实事件 ========== */
const loadRealEvents = async () => {
  try {
    const res = await listExceptions({ pageNum: 1, pageSize: 20 })
    const rows = (res as any)?.rows || res?.data || []
    if (Array.isArray(rows) && rows.length) {
      tickerEvents.value = rows.map((r: any) => ({ id: r.id, type: r.type || '未知', createTime: r.createTime, nickName: r.nickName || r.subjectName || '-', userId: r.userId, value: r.value, deviceId: r.deviceId, state: r.state, location: r.location }))
    }
  } catch {
    tickerEvents.value = [
      { id: 1, type: '心率异常', createTime: new Date(Date.now() - 300000).toISOString(), nickName: '张建国' },
      { id: 2, type: '血氧偏低', createTime: new Date(Date.now() - 600000).toISOString(), nickName: '李秀英' },
      { id: 3, type: '设备离线', createTime: new Date(Date.now() - 900000).toISOString(), nickName: '王德明' },
      { id: 4, type: '围栏越界', createTime: new Date(Date.now() - 1200000).toISOString(), nickName: '赵玉兰' },
      { id: 5, type: 'SOS求救', createTime: new Date(Date.now() - 1800000).toISOString(), nickName: '陈志强' }
    ]
  }
}

/* ========== 自动循环 ========== */
const startAutoCycle = async () => {
  await loadRealEvents()
  cycleTimer.value = setTimeout(async () => {
    await runPipeline(tickerEvents.value[0])
    const loop = async () => {
      cycleTimer.value = setTimeout(async () => {
        const idx = Math.floor(Math.random() * tickerEvents.value.length)
        await runPipeline(tickerEvents.value[idx] || undefined)
        loop()
      }, 15000)
    }
    loop()
  }, 2000)
}

/* ========== 生命周期 ========== */
let resizeHandler: (() => void) | null = null
onMounted(async () => {
  startWaveSim()
  await nextTick()
  initHRChart(); initGaugeChart(); initRadarChart()
  startAutoCycle()
  resizeHandler = () => { hrChart?.resize(); detectChart?.resize(); gaugeChart?.resize(); radarChart?.resize() }
  window.addEventListener('resize', resizeHandler)
})
onUnmounted(() => {
  if (cycleTimer.value) clearTimeout(cycleTimer.value)
  if (waveTimer.value) clearInterval(waveTimer.value)
  hrChart?.dispose(); detectChart?.dispose(); gaugeChart?.dispose(); radarChart?.dispose()
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
})

watch(activeNode, async () => {
  await nextTick()
  if (activeNode.value === 'collect') hrChart?.resize()
  if (activeNode.value === 'detect') detectChart?.resize()
  if (activeNode.value === 'assess') { gaugeChart?.resize(); radarChart?.resize() }
})
</script>

<style scoped lang="scss">
.ai-pipeline {
  --bg-deep: #f0f4f8; --bg-card: #ffffff; --bg-card-hover: #f8fafc;
  --border: #e2e8f0; --border-bright: #2563eb;
  --text-primary: #1e293b; --text-secondary: #64748b;
  --cyan: #0ea5e9; --green: #10b981; --red: #ef4444; --amber: #f59e0b; --purple: #8b5cf6;
  min-height: 100vh; background: var(--bg-deep); color: var(--text-primary);
  display: flex; flex-direction: column; overflow-x: hidden;
}

.ticker-bar {
  display: flex; align-items: center; gap: 12px; padding: 10px 20px;
  background: linear-gradient(90deg, rgba(14,165,233,0.04), #fff, rgba(14,165,233,0.04));
  border-bottom: 1px solid var(--border); overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.06); border-radius: 12px; }
.ticker-label {
  flex-shrink: 0; font-size: 12px; font-weight: 700; letter-spacing: 0.08em;
  text-transform: uppercase; color: #fff; padding: 3px 10px;
  background: var(--cyan); border-radius: 4px;
}
.ticker-track { flex: 1; overflow: hidden; mask-image: linear-gradient(90deg, transparent, #000 5%, #000 95%, transparent); }
.ticker-content { display: inline-flex; gap: 32px; animation: ticker-scroll linear infinite; white-space: nowrap; }
.ticker-item {
  display: inline-flex; align-items: center; gap: 6px; font-size: 13px;
  cursor: pointer; padding: 4px 10px; border-radius: 6px; transition: background 0.2s;
}
.ticker-item:hover, .ticker-item.active { background: rgba(14,165,233,0.08); }
.ticker-icon { font-size: 14px; }
.ticker-type { color: var(--red); font-weight: 600; }
.ticker-time { color: var(--text-secondary); }
.ticker-name { color: var(--text-primary); font-weight: 500; }
@keyframes ticker-scroll { 0% { transform: translateX(0); } 100% { transform: translateX(-50%); } }

.pipeline-row {
  display: flex; align-items: center; justify-content: center;
  padding: 36px 24px; gap: 0;
  background: linear-gradient(180deg, #fff, var(--bg-deep));
}
.pipeline-node { display: flex; align-items: center; gap: 12px; }
.node-icon-ring {
  width: 64px; height: 64px; border-radius: 50%; border: 2px solid var(--border);
  background: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.4s ease;
}
.pipeline-node.completed .node-icon-ring { border-color: var(--green); box-shadow: 0 0 16px rgba(16,185,129,0.25); background: #ecfdf5; }
.pipeline-node.processing .node-icon-ring { border-color: var(--cyan); box-shadow: 0 0 20px rgba(14,165,233,0.3); animation: pulse-ring 1.5s ease-in-out infinite; }
.pipeline-node.selected .node-icon-ring { border-color: var(--cyan); box-shadow: 0 0 12px rgba(14,165,233,0.2); }
.node-icon-inner {
  width: 44px; height: 44px; border-radius: 50%; background: #f0f9ff;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700; color: var(--text-secondary); background: #f8fafc; transition: all 0.4s;
}
.pipeline-node.completed .node-icon-inner { background: #ecfdf5; color: var(--green); }
.pipeline-node.processing .node-icon-inner { background: #eff6ff; color: var(--cyan); }
.node-idx { font-size: 18px; font-weight: 700; }
.node-label { display: flex; flex-direction: column; gap: 1px; }
.node-title { font-size: 14px; font-weight: 700; color: #1e293b; }
.node-sub { font-size: 11px; color: var(--text-secondary); }
.node-connector { display: flex; align-items: center; width: 80px; margin: 0 4px; }
.connector-line { flex: 1; height: 2px; background: var(--border); transition: all 0.6s; }
.connector-line.active { background: linear-gradient(90deg, var(--green), var(--cyan)); }
.connector-arrow { width: 18px; height: 18px; color: var(--border); transition: color 0.6s; }
.connector-arrow.active { color: var(--cyan); }
@keyframes pulse-ring { 0%,100%{box-shadow:0 0 16px rgba(14,165,233,0.2)} 50%{box-shadow:0 0 28px rgba(14,165,233,0.4)} }

.detail-grid { display: grid; grid-template-columns: 1fr 360px; gap: 0; flex: 1; }
.detail-panel { padding: 20px 24px; overflow-y: auto; max-height: calc(100vh - 200px); background: var(--bg-deep); }
.node-detail { display: flex; flex-direction: column; gap: 16px; }
.detail-title { margin: 0; font-size: 18px; font-weight: 700; display: flex; align-items: center; gap: 8px; }
.detail-badge {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 8px; font-size: 13px; font-weight: 700;
}
.detail-badge.cyan { background: #eff6ff; color: var(--cyan); }
.detail-badge.red { background: #fef2f2; color: var(--red); }
.detail-badge.green { background: #ecfdf5; color: var(--green); }
.detail-badge.amber { background: #fffbeb; color: var(--amber); }
.detail-badge.purple { background: #f5f3ff; color: var(--purple); }
.detail-desc { margin: 0; font-size: 13px; line-height: 1.7; color: var(--text-secondary); padding: 12px 16px; background: #fff; border-radius: 10px; border: 1px solid var(--border); box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.chart-box { width: 100%; height: 260px; }
.chart-box-sm { width: 100%; height: 220px; }
.metric-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.metric-card {
  padding: 14px 16px; background: var(--bg-card); border: 1px solid var(--border);
  border-radius: 12px; display: flex; flex-direction: column; gap: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.metric-label { font-size: 11px; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em; }
.metric-value { font-size: 24px; font-weight: 700; font-family: 'Courier New', monospace; }
.metric-value.cyan { color: #0284c7; }
.metric-value.green { color: #059669; }
.metric-value.amber { color: #d97706; }
.metric-value.purple { color: #7c3aed; }
.algo-params { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.param-item {
  padding: 12px 16px; background: var(--bg-card); border: 1px solid var(--border);
  border-radius: 10px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.param-key { font-size: 12px; color: var(--text-secondary); }
.param-val { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.param-val.red { color: var(--red); }
.ai-prompt-box, .ai-response-box { background: var(--bg-card); border: 1px solid var(--border); border-radius: 12px; overflow: hidden; }
.prompt-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 14px; background: #f8fafc; border-bottom: 1px solid var(--border); border-radius: 12px 12px 0 0;
}
.prompt-label { font-size: 11px; font-weight: 700; color: var(--cyan); text-transform: uppercase; letter-spacing: 0.06em; }
.typing-badge { font-size: 11px; color: var(--amber); animation: blink 1s infinite; }
.prompt-text {
  padding: 14px; margin: 0; font-size: 12px; line-height: 1.7; color: var(--text-secondary);
  white-space: pre-wrap; word-break: break-all; max-height: 120px; overflow-y: auto;
}
.response-text { padding: 14px; font-size: 14px; line-height: 1.8; color: var(--text-primary); background: #fafbfc; border-radius: 0 0 12px 12px; max-height: 200px; overflow-y: auto; }
.assess-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.risk-info-row {
  display: flex; align-items: center; gap: 16px; padding: 12px 16px;
  background: #fff; border: 1px solid var(--border); border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.risk-tag { padding: 6px 16px; border-radius: 6px; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.risk-tag.high { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.risk-tag.medium { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }
.risk-tag.low { background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }
.confidence-bar-wrap { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.confidence-label { font-size: 11px; color: var(--text-secondary); }
.confidence-track { height: 6px; background: #e2e8f0; border-radius: 3px; overflow: hidden; }
.confidence-fill { height: 100%; background: linear-gradient(90deg, #0ea5e9, #10b981); border-radius: 3px; transition: width 0.8s ease; }
.confidence-val { font-size: 13px; font-weight: 700; color: var(--cyan); }
.action-cards { display: flex; flex-direction: column; gap: 10px; }
.action-card {
  display: flex; align-items: center; gap: 14px; padding: 14px 18px;
  background: var(--bg-card); border: 1px solid var(--border); border-radius: 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.action-card:hover { border-color: var(--border-bright); box-shadow: 0 4px 16px rgba(14,165,233,0.1); }
.action-icon { font-size: 24px; flex-shrink: 0; }
.action-body { flex: 1; }
.action-body strong { font-size: 14px; color: var(--text-primary); display: block; margin-bottom: 2px; }
.action-body p { margin: 0; font-size: 12px; color: var(--text-secondary); line-height: 1.5; }

.chat-panel {
  border-left: 1px solid var(--border); display: flex; flex-direction: column;
  background: #fff; max-height: calc(100vh - 200px); background: var(--bg-deep);
}
.chat-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; border-bottom: 1px solid var(--border); }
.chat-title { font-size: 14px; font-weight: 700; }
.chat-messages { flex: 1; overflow-y: auto; padding: 14px; display: flex; flex-direction: column; gap: 10px; }
.chat-msg { display: flex; }
.chat-msg.user { justify-content: flex-end; }
.chat-bubble {
  max-width: 90%; padding: 10px 14px; border-radius: 12px; font-size: 13px; line-height: 1.7;
  background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);
}
.chat-msg.user .chat-bubble { background: #eff6ff; border-color: #bfdbfe; }
.typing-bubble { display: flex; align-items: center; justify-content: center; padding: 12px 20px; }
.dot-flashing-mini {
  width: 6px; height: 6px; border-radius: 50%; background: var(--cyan);
  box-shadow: 10px 0 0 var(--cyan), 20px 0 0 rgba(14,165,233,0.4);
  display: inline-block; animation: blink 1.2s infinite;
}
.chat-input-row { display: flex; gap: 8px; padding: 12px; border-top: 1px solid var(--border); }

.spin { animation: spin 0.8s linear infinite; }
@keyframes spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0.4} }

@media (max-width: 1024px) {
  .detail-grid { grid-template-columns: 1fr; }
  .chat-panel { border-left: none; border-top: 1px solid var(--border); max-height: 400px; }
  .metric-row { grid-template-columns: repeat(2, 1fr); }
  .assess-row { grid-template-columns: 1fr; }
  .node-connector { width: 40px; }
}
</style>
