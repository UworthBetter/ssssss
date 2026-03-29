<template>
  <PlatformPageShell
    title="多Agent协同中心"
    subtitle="面向真实健康异常事件，展示感知、预警、评估、决策、执行五类 Agent 的真实协同轨迹。"
    eyebrow="AGENT COLLAB"
    aside-title="协同侧栏"
    aside-width="340px"
  >
    <template #headerExtra>
      <div class="actions">
        <el-button type="primary" :icon="VideoPlay" :disabled="!selectedEvent" :loading="actionLoading" @click="startSelected">
          启动协同
        </el-button>
        <el-button :icon="RefreshRight" :disabled="!selectedEvent" :loading="detailLoading" @click="loadSelected(true)">
          刷新状态
        </el-button>
      </div>
    </template>

    <template #toolbar>
      <div class="actions">
        <el-input v-model="filters.keyword" placeholder="搜索事件ID / 姓名 / 异常类型" clearable style="width: 280px" />
        <el-select v-model="filters.type" placeholder="异常类型" clearable style="width: 180px">
          <el-option v-for="type in typeOptions" :key="type" :label="type" :value="type" />
        </el-select>
        <el-select v-model="filters.state" placeholder="处置状态" clearable style="width: 140px">
          <el-option label="处理中" value="0" />
          <el-option label="已闭环" value="1" />
        </el-select>
        <el-button type="primary" @click="fetchEvents">刷新事件</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </template>

    <div class="stats">
      <div v-for="card in cards" :key="card.label" class="stat">
        <div class="label">{{ card.label }}</div>
        <div class="value">{{ card.value }}</div>
        <div class="hint">{{ card.hint }}</div>
      </div>
    </div>

    <div class="layout">
      <section class="panel">
        <div class="panel-head">
          <div>
            <h3>真实事件队列</h3>
            <p>从事件表选择一个异常，右侧会直接读取真实协同状态。</p>
          </div>
          <el-tag type="info" effect="light">{{ filteredEvents.length }} 条</el-tag>
        </div>

        <el-table v-loading="eventsLoading" :data="pagedEvents" stripe highlight-current-row row-key="id" @row-click="selectEvent">
          <el-table-column prop="id" label="事件ID" width="90" />
          <el-table-column label="老人姓名" min-width="110">
            <template #default="{ row }">{{ row.nickName || row.subjectName || '-' }}</template>
          </el-table-column>
          <el-table-column prop="type" label="异常类型" min-width="140" />
          <el-table-column prop="value" label="异常值" min-width="110" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="String(row.state ?? '0') === '1' ? 'success' : 'warning'" effect="light">
                {{ String(row.state ?? '0') === '1' ? '已闭环' : '处理中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发生时间" min-width="170" />
        </el-table>

        <div class="pager">
          <el-pagination v-model:current-page="page" :page-size="pageSize" :total="filteredEvents.length" layout="total, prev, pager, next" />
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <div>
            <h3>协同工作台</h3>
            <p v-if="selectedEvent">当前事件 {{ selectedEvent.id }}，所有状态均来自后端真实协同接口。</p>
            <p v-else>请先选择一个事件。</p>
          </div>
          <div class="actions" v-if="selectedEvent">
            <el-tag :type="selectedEventTag.type" effect="light">{{ selectedEventTag.label }}</el-tag>
            <el-tag :type="riskTagType(selectedRiskLevel)" effect="light">{{ riskLabel(selectedRiskLevel) }}</el-tag>
          </div>
        </div>

        <el-empty v-if="!selectedEvent" description="请选择一个异常事件查看真实多Agent协同。" />

        <template v-else>
          <div class="overview">
            <div class="meta"><span>服务对象</span><strong>{{ selectedEvent.nickName || selectedEvent.subjectName || '-' }}</strong></div>
            <div class="meta"><span>异常类型</span><strong>{{ selectedEvent.type || '-' }}</strong></div>
            <div class="meta"><span>当前阶段</span><strong>{{ stageLabel(status?.stage) }}</strong></div>
            <div class="meta"><span>主责 Agent</span><strong>{{ currentAgentName }}</strong></div>
            <div class="meta"><span>下一跳 Agent</span><strong>{{ nextAgentName }}</strong></div>
            <div class="meta"><span>人工介入</span><strong>{{ status?.auto_execute === false ? '需要确认' : '自动执行' }}</strong></div>
          </div>

          <div class="agent-grid">
            <button v-for="agent in agentCards" :key="agent.key" class="agent" :class="agent.status" @click="activeTab = agent.key">
              <div class="agent-top">
                <div class="icon" :class="agent.tone"><component :is="agent.icon" /></div>
                <div>
                  <div class="agent-name">{{ agent.name }}</div>
                  <div class="muted">{{ statusLabel(agent.status) }}</div>
                </div>
              </div>
              <div class="agent-summary">{{ agent.summary }}</div>
              <div class="muted">{{ agent.timeText }} · {{ agent.durationText }}</div>
            </button>
          </div>

          <div class="timeline">
            <div v-for="agent in agentCards" :key="`${agent.key}-tl`" class="timeline-item">
              <div class="dot" :class="agent.status"><component :is="agent.icon" /></div>
              <div class="content">
                <div class="row">
                  <strong>{{ agent.name }}</strong>
                  <span class="muted">{{ agent.timeText }}</span>
                </div>
                <div>{{ agent.summary }}</div>
                <div class="muted">{{ agent.detail }}</div>
              </div>
            </div>
          </div>

          <el-tabs v-model="activeTab">
            <el-tab-pane v-for="agent in agentCards" :key="agent.key" :label="agent.name" :name="agent.key">
              <el-empty v-if="!agent.trace" description="该 Agent 尚未执行。" />
              <div v-else class="detail-block">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="状态">
                    <el-tag :type="statusTagType(agent.status)" effect="light">{{ statusLabel(agent.status) }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="交接目标">{{ agent.trace.handoffTo || '无' }}</el-descriptions-item>
                  <el-descriptions-item label="开始时间">{{ formatDateTime(agent.trace.startedAt) }}</el-descriptions-item>
                  <el-descriptions-item label="完成时间">{{ formatDateTime(agent.trace.finishedAt) }}</el-descriptions-item>
                  <el-descriptions-item label="耗时">{{ formatDuration(agent.trace.durationMs) }}</el-descriptions-item>
                  <el-descriptions-item label="摘要">{{ agent.trace.summary || '-' }}</el-descriptions-item>
                </el-descriptions>
                <p>{{ agent.trace.detail || '暂无详细说明。' }}</p>
                <div class="kv" v-for="entry in detailEntries(agent.trace.details)" :key="`${agent.key}-${entry.key}`">
                  <span>{{ detailKeyLabel(entry.key) }}</span>
                  <strong>{{ entry.value }}</strong>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="综合洞察" name="insight">
              <el-empty v-if="!insightView" description="当前事件暂无综合洞察。" />
              <div v-else class="detail-block">
                <div class="row">
                  <strong>AI 医学洞察</strong>
                  <el-tag :type="riskTagType(insightView.riskLevel)" effect="light">{{ riskLabel(insightView.riskLevel) }}</el-tag>
                </div>
                <p>{{ insightView.overview }}</p>
                <div class="chips" v-if="insightView.reasons.length">
                  <span v-for="reason in insightView.reasons" :key="reason" class="chip">{{ reason }}</span>
                </div>
                <div class="chips" v-if="insightView.actions.length">
                  <span v-for="action in insightView.actions" :key="action" class="chip action">{{ action }}</span>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </template>
      </section>
    </div>

    <template #aside>
      <div class="side">
        <div class="panel side-card">
          <div class="side-title">事件概况</div>
          <el-descriptions v-if="selectedEvent" :column="1" border size="small">
            <el-descriptions-item label="事件ID">{{ selectedEvent.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="异常类型">{{ selectedEvent.type || '-' }}</el-descriptions-item>
            <el-descriptions-item label="风险等级">{{ riskLabel(selectedRiskLevel) }}</el-descriptions-item>
            <el-descriptions-item label="执行状态">{{ executionLabel(status?.execution_status) }}</el-descriptions-item>
            <el-descriptions-item label="累计耗时">{{ totalDurationLabel }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="请选择事件。" />
        </div>

        <div class="panel side-card">
          <div class="side-title">审计留痕</div>
          <div v-if="auditTrail.length" class="list">
            <div v-for="(item, index) in auditTrail" :key="`${item.operation_type}-${index}`" class="list-item">
              <div class="row">
                <strong>{{ item.operation_type || 'AUDIT' }}</strong>
                <span class="muted">{{ formatDateTime(item.created_at) }}</span>
              </div>
              <div>{{ item.operation_detail || '-' }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无审计记录。" />
        </div>

        <div class="panel side-card">
          <div class="side-title">最近洞察快照</div>
          <div v-if="snapshots.length" class="list">
            <div v-for="snapshot in snapshots" :key="snapshot.id" class="list-item">
              <div class="row">
                <strong>#{{ snapshot.id }}</strong>
                <span class="muted">{{ formatDateTime(snapshot.generated_at || snapshot.createTime) }}</span>
              </div>
              <div>{{ snapshot.summary || '暂无摘要' }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无快照。" />
        </div>
      </div>
    </template>
  </PlatformPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ChatLineRound, DataLine, RefreshRight, Setting, TrendCharts, VideoPlay, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { PlatformPageShell } from '@/components/platform'
import { getEventInsight, type EventInsightPayload } from '@/api/ai'
import { listExceptions, type ExceptionAlert } from '@/api/health'
import { getProcessingChain, startProcessingChain } from '@/api/processingChain'

interface ExceptionEvent extends ExceptionAlert { nickName?: string; subjectName?: string }
interface TraceItem { agentKey?: string; status?: string; summary?: string; detail?: string; handoffTo?: string; startedAt?: string; finishedAt?: string; durationMs?: number | string; details?: Record<string, unknown> }
interface AuditItem { operation_type?: string; operation_detail?: string; created_at?: string }
interface SnapshotItem { id?: number | string; summary?: string; generated_at?: string; createTime?: string }
interface StatusPayload { stage?: string; risk_level?: string; execution_status?: string; auto_execute?: boolean; totalDuration?: number | string; agentTrace?: TraceItem[]; auditTrail?: AuditItem[]; recentInsightSnapshots?: SnapshotItem[] }

const AGENTS = [
  { key: 'perception', name: '感知 Agent', tone: 'cyan', icon: DataLine },
  { key: 'warning', name: '预警 Agent', tone: 'red', icon: WarningFilled },
  { key: 'assess', name: '评估 Agent', tone: 'amber', icon: TrendCharts },
  { key: 'decision', name: '决策 Agent', tone: 'purple', icon: ChatLineRound },
  { key: 'execute', name: '执行 Agent', tone: 'green', icon: Setting }
]

const filters = reactive({ keyword: '', type: '', state: '' })
const page = ref(1)
const pageSize = 8
const eventsLoading = ref(false)
const detailLoading = ref(false)
const actionLoading = ref(false)
const eventList = ref<ExceptionEvent[]>([])
const selectedId = ref<number | string | null>(null)
const status = ref<StatusPayload | null>(null)
const insight = ref<EventInsightPayload | null>(null)
const activeTab = ref('perception')
let timer: ReturnType<typeof window.setInterval> | null = null

const typeOptions = computed(() => Array.from(new Set(eventList.value.map(item => String(item.type || '').trim()).filter(Boolean))))
const filteredEvents = computed(() => eventList.value.filter(item => {
  if (filters.type && item.type !== filters.type) return false
  if (filters.state && String(item.state ?? '0') !== filters.state) return false
  const keyword = filters.keyword.trim().toLowerCase()
  if (!keyword) return true
  return [item.id, item.nickName, item.subjectName, item.type, item.value].some(v => String(v || '').toLowerCase().includes(keyword))
}))
const pagedEvents = computed(() => filteredEvents.value.slice((page.value - 1) * pageSize, page.value * pageSize))
const selectedEvent = computed(() => eventList.value.find(item => String(item.id) === String(selectedId.value ?? '')) || null)
const traceMap = computed(() => new Map((status.value?.agentTrace || []).map(item => [String(item.agentKey || ''), item])))
const auditTrail = computed(() => status.value?.auditTrail || [])
const snapshots = computed(() => status.value?.recentInsightSnapshots || [])
const selectedRiskLevel = computed(() => normalizeRisk(status.value?.risk_level || inferRisk(selectedEvent.value)))
const selectedEventTag = computed(() => String(selectedEvent.value?.state ?? '0') === '1' ? { label: '已闭环', type: 'success' } : { label: '处理中', type: 'warning' })
const totalDurationLabel = computed(() => status.value?.totalDuration !== undefined && status.value?.totalDuration !== null ? `${status.value.totalDuration} 分钟` : '未统计')

const agentCards = computed(() => AGENTS.map(agent => {
  const trace = traceMap.value.get(agent.key)
  const state = normalizeStatus(trace?.status)
  return {
    ...agent,
    status: state,
    summary: trace?.summary || pendingSummary(agent.key),
    detail: trace?.detail || pendingDetail(agent.key),
    timeText: formatDateTime(trace?.finishedAt || trace?.startedAt),
    durationText: formatDuration(trace?.durationMs),
    trace
  }
}))

const currentAgentName = computed(() => agentCards.value.find(item => item.status === 'processing')?.name || [...agentCards.value].reverse().find(item => item.status === 'completed')?.name || '未开始')
const nextAgentName = computed(() => {
  const handoff = agentCards.value.find(item => item.status === 'processing')?.trace?.handoffTo
  return AGENTS.find(item => item.key === handoff)?.name || agentCards.value.find(item => item.status === 'pending')?.name || '无'
})

const insightView = computed(() => {
  if (!insight.value) return null
  return {
    overview: String(insight.value.overview || insight.value.summary || '暂无概览'),
    riskLevel: normalizeRisk(insight.value.risk?.level || insight.value.risk?.riskLevel || insight.value.riskLevel || selectedRiskLevel.value),
    reasons: toArray(insight.value.analysisReasons || insight.value.reasons || insight.value.risk?.possibleCauses),
    actions: toArray(insight.value.suggestedActions || insight.value.actions || insight.value.advice?.suggestedActions)
  }
})

const cards = computed(() => [
  { label: '真实事件总数', value: eventList.value.length, hint: '来自异常事件表' },
  { label: '待闭环事件', value: eventList.value.filter(item => String(item.state ?? '0') !== '1').length, hint: '仍在持续跟进' },
  { label: '已闭环事件', value: eventList.value.filter(item => String(item.state ?? '0') === '1').length, hint: '已完成处置' },
  { label: '已执行 Agent', value: agentCards.value.filter(item => item.status !== 'pending').length, hint: '当前选中事件' }
])

async function fetchEvents() {
  eventsLoading.value = true
  try {
    const res = await listExceptions({ pageNum: 1, pageSize: 100 })
    eventList.value = (res.rows || []) as ExceptionEvent[]
    if (!selectedId.value && eventList.value.length) selectedId.value = eventList.value[0].id ?? null
    if (selectedId.value && !eventList.value.some(item => String(item.id) === String(selectedId.value))) selectedId.value = eventList.value[0]?.id ?? null
  } catch {
    ElMessage.error('加载异常事件失败')
  } finally {
    eventsLoading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.type = ''
  filters.state = ''
  page.value = 1
}

function selectEvent(row: ExceptionEvent) {
  selectedId.value = row.id ?? null
}

async function loadSelected(refreshInsight = false) {
  if (!selectedEvent.value?.id) return
  detailLoading.value = true
  const [statusRes, insightRes] = await Promise.allSettled([
    getProcessingChain(selectedEvent.value.id),
    getEventInsight(selectedEvent.value.id, { refresh: refreshInsight })
  ])
  status.value = statusRes.status === 'fulfilled' && statusRes.value?.data ? statusRes.value.data as StatusPayload : null
  insight.value = insightRes.status === 'fulfilled' ? insightRes.value?.data as EventInsightPayload : null
  detailLoading.value = false
}

async function startSelected() {
  if (!selectedEvent.value?.id) return
  actionLoading.value = true
  try {
    await startProcessingChain(selectedEvent.value.id, {
      abnormalId: selectedEvent.value.id,
      abnormalType: selectedEvent.value.type,
      abnormalValue: selectedEvent.value.value,
      userId: selectedEvent.value.userId,
      deviceId: selectedEvent.value.deviceId,
      location: selectedEvent.value.location
    })
    ElMessage.success('已启动真实 Agent 协同')
    await loadSelected(true)
  } catch {
    ElMessage.error('启动协同失败')
  } finally {
    actionLoading.value = false
  }
}

function normalizeStatus(value?: string) {
  const v = String(value || '').toLowerCase()
  if (['completed', 'success', 'done'].includes(v)) return 'completed'
  if (['processing', 'running', 'active'].includes(v)) return 'processing'
  if (['failed', 'error'].includes(v)) return 'failed'
  if (v === 'skipped') return 'skipped'
  return 'pending'
}

function statusLabel(value?: string) { return ({ pending: '待执行', processing: '执行中', completed: '已完成', failed: '失败', skipped: '已跳过' } as Record<string, string>)[normalizeStatus(value)] }
function statusTagType(value?: string) { return ({ pending: 'info', processing: 'warning', completed: 'success', failed: 'danger', skipped: '' } as Record<string, string>)[normalizeStatus(value)] || 'info' }
function normalizeRisk(value?: string) { const v = String(value || '').toLowerCase(); return ['high', 'danger', 'critical'].includes(v) ? 'high' : ['medium', 'warning'].includes(v) ? 'medium' : 'low' }
function riskLabel(value?: string) { return ({ high: '高风险', medium: '中风险', low: '低风险' } as Record<string, string>)[normalizeRisk(value)] }
function riskTagType(value?: string) { return ({ high: 'danger', medium: 'warning', low: 'success' } as Record<string, string>)[normalizeRisk(value)] || 'info' }
function stageLabel(value?: string) { return ({ DETECTED: '已检测', INSIGHT_BUILT: '已完成洞察', RISK_ASSESSED: '已完成评估', DISPOSITION_SUGGESTED: '已生成处置', COMPLETED: '已完成闭环', FAILED: '执行失败' } as Record<string, string>)[String(value || '')] || '未启动' }
function executionLabel(value?: string) { return ({ SUCCESS: '成功', FAILED: '失败', PENDING: '待执行' } as Record<string, string>)[String(value || '')] || '未执行' }
function inferRisk(event: ExceptionEvent | null) { const type = String(event?.type || '').toLowerCase(); return ['跌倒', '心率', '血氧', '围栏', 'sos'].some(k => type.includes(k)) ? 'high' : ['体温', '离线'].some(k => type.includes(k)) ? 'medium' : 'low' }
function pendingSummary(key: string) { return ({ perception: '等待接入设备上下文', warning: '等待感知结果', assess: '等待预警结果', decision: '等待评估结果', execute: '等待决策结果' } as Record<string, string>)[key] || '等待执行' }
function pendingDetail(key: string) { return ({ perception: '当前事件尚未进入真实协同流程。', warning: '尚未拿到上游输入。', assess: '尚未完成风险评分。', decision: '尚未生成医学洞察。', execute: '尚未触发处置动作。' } as Record<string, string>)[key] || '等待执行' }
function formatDateTime(value?: string | number | null) { if (!value) return '未记录'; const date = new Date(value); return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString('zh-CN', { hour12: false }) }
function formatDuration(value?: number | string | null) { const n = Number(value); return Number.isFinite(n) && n > 0 ? (n < 1000 ? `${n} ms` : `${(n / 1000).toFixed(2)} s`) : '未统计' }
function toArray(value: unknown) { return Array.isArray(value) ? value.map(item => String(item)).filter(Boolean) : typeof value === 'string' ? value.split(/[\n,，、]/).map(item => item.trim()).filter(Boolean) : [] }
function detailEntries(details?: Record<string, unknown>) { return Object.entries(details || {}).filter(([, v]) => v !== null && v !== undefined && v !== '').map(([key, value]) => ({ key, value: Array.isArray(value) ? value.join('、') : typeof value === 'object' ? JSON.stringify(value) : String(value) })) }
function detailKeyLabel(key: string) { return ({ subjectName: '对象姓名', deviceId: '设备ID', abnormalType: '异常类型', abnormalValue: '异常值', location: '位置', integrity: '数据完整度', priority: '优先级', riskScore: '风险评分', riskLevel: '风险等级', suggestedActions: '建议动作', notifyWho: '通知对象', disposition: '处置建议', notificationLevel: '通知级别', autoExecute: '自动执行' } as Record<string, string>)[key] || key }

watch(() => selectedId.value, async value => {
  activeTab.value = 'perception'
  status.value = null
  insight.value = null
  if (value) await loadSelected()
})

watch(() => [filters.keyword, filters.type, filters.state], () => { page.value = 1 })

onMounted(async () => {
  await fetchEvents()
  timer = window.setInterval(() => {
    void fetchEvents()
    if (selectedId.value) void loadSelected()
  }, 15000)
})

onUnmounted(() => { if (timer) window.clearInterval(timer) })
</script>

<style scoped lang="scss">
.actions,.row{display:flex;align-items:center;gap:12px;flex-wrap:wrap}
.stats{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:16px}
.stat,.panel{background:#fff;border:1px solid #e2e8f0;border-radius:18px;box-shadow:0 8px 24px rgba(15,23,42,.05)}
.stat{padding:18px}
.label,.hint,.muted{color:#64748b;font-size:12px}
.value{margin-top:10px;font-size:30px;font-weight:800;color:#0f172a}
.hint{margin-top:8px}
.layout{display:grid;grid-template-columns:minmax(400px,.9fr) minmax(0,1.4fr);gap:20px}
.panel{padding:20px}
.panel-head{display:flex;justify-content:space-between;align-items:flex-start;gap:16px;margin-bottom:16px}
.panel-head h3{margin:0;color:#0f172a}
.panel-head p{margin:6px 0 0;color:#64748b;font-size:13px;line-height:1.6}
.pager{display:flex;justify-content:flex-end;margin-top:16px}
.overview{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;margin-bottom:16px}
.meta,.detail-block,.side-card,.list-item{background:#f8fafc;border:1px solid #e2e8f0;border-radius:14px}
.meta{padding:14px;display:flex;flex-direction:column;gap:6px}
.meta span{font-size:12px;color:#64748b}
.meta strong,.agent-name{color:#0f172a}
.agent-grid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:12px;margin-bottom:16px}
.agent{padding:14px;border:1px solid #e2e8f0;border-radius:16px;background:#fff;text-align:left;cursor:pointer}
.agent.processing{border-color:#f59e0b}.agent.completed{border-color:#10b981}.agent.failed{border-color:#ef4444}.agent.skipped{border-color:#94a3b8}
.agent-top{display:flex;gap:10px;align-items:center}
.icon{width:36px;height:36px;border-radius:12px;display:flex;align-items:center;justify-content:center}
.icon.cyan{background:#eff6ff;color:#0ea5e9}.icon.red{background:#fef2f2;color:#ef4444}.icon.amber{background:#fffbeb;color:#f59e0b}.icon.purple{background:#f5f3ff;color:#8b5cf6}.icon.green{background:#ecfdf5;color:#10b981}
.agent-summary{margin:12px 0;color:#334155;font-size:13px;line-height:1.6;min-height:42px}
.timeline{display:flex;flex-direction:column;gap:12px;margin-bottom:16px}
.timeline-item{display:grid;grid-template-columns:34px minmax(0,1fr);gap:12px}
.dot{width:34px;height:34px;border-radius:999px;background:#e2e8f0;display:flex;align-items:center;justify-content:center;color:#475569}
.dot.processing{background:#fef3c7;color:#d97706}.dot.completed{background:#dcfce7;color:#15803d}.dot.failed{background:#fee2e2;color:#dc2626}.dot.skipped{background:#e2e8f0;color:#64748b}
.content{padding-bottom:6px}
.detail-block{padding:16px}
.detail-block p{margin:12px 0;color:#334155;line-height:1.7}
.kv{display:flex;justify-content:space-between;gap:12px;padding:10px 0;border-top:1px dashed #dbeafe}
.chips{display:flex;gap:10px;flex-wrap:wrap}
.chip{padding:6px 10px;border-radius:999px;background:#e2e8f0;font-size:12px;color:#334155}
.chip.action{background:#dbeafe;color:#1d4ed8}
.side{display:flex;flex-direction:column;gap:16px}
.side-card{padding:16px}
.side-title{margin-bottom:12px;font-weight:700;color:#0f172a}
.list{display:flex;flex-direction:column;gap:12px}
.list-item{padding:12px}
@media (max-width:1400px){.layout{grid-template-columns:1fr}.agent-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}
@media (max-width:960px){.stats,.overview,.agent-grid{grid-template-columns:1fr}.panel-head,.row{align-items:flex-start;flex-direction:column}}
</style>
