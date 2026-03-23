<template>
  <PlatformPageShell
    title="处理链追踪"
    subtitle="追踪每一条异常事件从检测到闭环的完整处理过程，透视 AI 决策链路与处置执行细节。"
    eyebrow="PROCESSING CHAIN"
    aside-title="处理链详情"
    aside-width="420px"
  >
    <template #headerExtra>
      <div class="header-actions">
        <PlatformSearchEntry
          :label="searchPresentation.label"
          :placeholder="searchPresentation.placeholder"
          :hint="searchPresentation.hint"
          @click="handleSearchClick"
        />
      </div>
    </template>

    <template #toolbar>
      <div class="toolbar-stack">
        <div class="toolbar">
          <el-input v-model="query.keyword" placeholder="搜索异常/节点" clearable style="width: 200px" />
          <el-select v-model="query.status" placeholder="节点状态" clearable style="width: 140px">
            <el-option label="运行中" value="running" />
            <el-option label="已完成" value="done" />
            <el-option label="异常" value="error" />
          </el-select>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>
    </template>

    <div class="panel section">
      <!-- 统计摘要卡片 -->
      <div class="chain-summary-row">
        <div class="summary-card">
          <div class="sc-label">事件总数</div>
          <div class="sc-value digital-font">{{ total }}</div>
        </div>
        <div class="summary-card">
          <div class="sc-label">待处理</div>
          <div class="sc-value digital-font text-warning">{{ pendingCount }}</div>
        </div>
        <div class="summary-card">
          <div class="sc-label">已完成链路</div>
          <div class="sc-value digital-font text-success">{{ resolvedCount }}</div>
        </div>
        <div class="summary-card">
          <div class="sc-label">平均链路耗时</div>
          <div class="sc-value digital-font">{{ avgChainDuration }}<span class="sc-unit">ms</span></div>
        </div>
      </div>

      <!-- 事件列表 -->
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        highlight-current-row
        @current-change="handleCurrentChange"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="事件ID" width="90" />
        <el-table-column prop="nickName" label="服务对象" min-width="100" />
        <el-table-column prop="type" label="异常类型" min-width="120" />
        <el-table-column prop="value" label="指标值" min-width="100" />
        <el-table-column label="链路进度" min-width="180">
          <template #default="{ row }">
            <div class="chain-inline-progress">
              <div class="chain-dots">
                <div
                  v-for="(stage, i) in getChainStages(row)"
                  :key="i"
                  class="chain-dot"
                  :class="stage.status"
                  :title="stage.name"
                />
              </div>
              <span class="chain-label">{{ getChainLabel(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.state === '1' ? 'success' : 'danger'">{{ row.state === '1' ? '已闭环' : '处理中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发生时间" min-width="170" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @change="fetchList"
        />
      </div>
    </div>

    <template #aside>
      <div class="aside-stack">
        <PlatformNotificationEntry
          title="事件链路通知"
          :unread-count="notificationItems.length"
          :items="notificationItems"
          @click-item="handleNotificationItem"
          @click-all="handleNotificationClick"
        />

        <!-- 选中事件的处理链详情 -->
        <div class="panel aside-card chain-detail-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">处理链时间轴</div>
              <p class="detail-subtitle">展示事件从检测到闭环的完整处理过程。</p>
            </div>
            <el-tag v-if="selectedEvent" :type="selectedEvent.state === '1' ? 'success' : 'warning'" effect="light">
              {{ selectedEvent.state === '1' ? '已闭环' : '进行中' }}
            </el-tag>
          </div>

          <div v-if="selectedEvent" class="detail-body">
            <!-- 事件基本信息 -->
            <div class="chain-info-bar">
              <div class="info-item">
                <span class="info-label">事件ID</span>
                <span class="info-value digital-font">{{ selectedEvent.id }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">对象</span>
                <span class="info-value">{{ selectedEvent.nickName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">类型</span>
                <span class="info-value">{{ selectedEvent.type || '-' }}</span>
              </div>
            </div>

            <!-- 处理链时间轴 -->
            <div v-if="chainLoading" class="chain-loading">
              <div class="loading-line loading-line-lg"></div>
              <div class="loading-line"></div>
              <div class="loading-line"></div>
              <div class="loading-line loading-line-short"></div>
            </div>

            <div v-else-if="chainData" class="chain-timeline">
              <div
                v-for="(stage, index) in chainData.stages"
                :key="index"
                class="tl-item"
                :class="`tl-${stage.status}`"
              >
                <div class="tl-marker">
                  <div class="tl-dot">
                    <el-icon v-if="stage.status === 'completed'"><Check /></el-icon>
                    <el-icon v-else-if="stage.status === 'processing'"><Loading /></el-icon>
                    <el-icon v-else><Clock /></el-icon>
                  </div>
                  <div v-if="index < chainData.stages.length - 1" class="tl-line" />
                </div>
                <div class="tl-body">
                  <div class="tl-head">
                    <span class="tl-name">{{ stage.name }}</span>
                    <span v-if="stage.timestamp" class="tl-time digital-font">{{ formatTime(stage.timestamp) }}</span>
                  </div>
                  <div v-if="stage.details" class="tl-details">
                    <template v-if="typeof stage.details === 'object'">
                      <div v-for="(val, key) in flattenDetails(stage.details)" :key="key" class="tl-detail-row">
                        <span class="tl-detail-key">{{ formatDetailKey(key) }}:</span>
                        <span class="tl-detail-val">{{ val }}</span>
                      </div>
                    </template>
                    <template v-else>
                      <p class="tl-detail-text">{{ stage.details }}</p>
                    </template>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty-detail">
              处理链数据加载失败或暂无数据,请重新选择事件。
            </div>

            <!-- 快捷操作 -->
            <div class="chain-actions">
              <el-button type="primary" size="small" @click="jumpToEvent(selectedEvent)">进入事件中心</el-button>
              <el-button size="small" @click="jumpToSubject(selectedEvent)">查看对象</el-button>
              <el-button size="small" @click="jumpToDevice(selectedEvent)">查看设备</el-button>
            </div>
          </div>

          <div v-else class="empty-detail">
            <div class="empty-title">尚未选中事件</div>
            <p>从左侧列表中选择一个事件后，这里会展示该事件的完整处理链时间轴。</p>
          </div>
        </div>

        <!-- AI 研判摘要 -->
        <div class="panel aside-card insight-summary-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">AI 研判摘要</div>
              <p class="detail-subtitle">基于多 Agent 协同生成的处置建议。</p>
            </div>
            <el-button
              v-if="selectedEvent"
              text
              type="primary"
              :loading="insightLoading"
              @click="fetchInsight"
            >
              刷新研判
            </el-button>
          </div>

          <div v-if="selectedEvent && insightData" class="detail-body">
            <div class="insight-mini">
              <div class="im-row">
                <span class="im-label">风险等级</span>
                <el-tag :type="insightRiskTagType" effect="light" size="small">{{ insightData.riskLabel }}</el-tag>
              </div>
              <div class="im-row">
                <span class="im-label">概述</span>
                <span class="im-value">{{ insightData.overview }}</span>
              </div>
              <div v-if="insightData.reasons.length" class="im-row">
                <span class="im-label">分析理由</span>
                <div class="im-chips">
                  <span v-for="r in insightData.reasons.slice(0, 3)" :key="r" class="insight-chip">{{ r }}</span>
                </div>
              </div>
              <div v-if="insightData.suggestedActions.length" class="im-row">
                <span class="im-label">建议动作</span>
                <div class="im-chips">
                  <span v-for="a in insightData.suggestedActions.slice(0, 3)" :key="a" class="insight-chip insight-chip-action">{{ a }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="insightLoading" class="detail-body">
            <div class="chain-loading">
              <div class="loading-line loading-line-lg"></div>
              <div class="loading-line"></div>
            </div>
          </div>
          <div v-else class="empty-detail">
            {{ selectedEvent ? '暂无研判数据，点击刷新研判获取。' : '选中事件后展示 AI 研判摘要。' }}
          </div>
        </div>
      </div>
    </template>
  </PlatformPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Loading, Clock } from '@element-plus/icons-vue'
import {
  dispatchPlatformAction,
  getPlatformSearchPresentation,
  loadPlatformNotifications,
  openAllPlatformNotifications,
  openPlatformNotification,
  openPlatformSearch,
  PlatformNotificationEntry,
  PlatformPageShell,
  PlatformSearchEntry,
  type PlatformNotificationRecord
} from '@/components/platform'
import { listExceptions, type ExceptionAlert } from '@/api/health'
import { getProcessingChain } from '@/api/processingChain'
import { getEventInsight } from '@/api/ai'

interface ChainStage {
  name: string
  status: 'completed' | 'processing' | 'pending'
  timestamp?: string
  details?: Record<string, any> | string
}

interface ChainData {
  stages: ChainStage[]
  totalDuration: number
}

interface InsightSummary {
  overview: string
  riskLevel: string
  riskLabel: string
  reasons: string[]
  suggestedActions: string[]
}

const router = useRouter()
const searchPresentation = getPlatformSearchPresentation('event')

const loading = ref(false)
const list = ref<ExceptionAlert[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const selectedEvent = ref<ExceptionAlert | null>(null)
const chainLoading = ref(false)
const chainData = ref<ChainData | null>(null)
const insightLoading = ref(false)
const insightData = ref<InsightSummary | null>(null)
const notificationItems = ref<PlatformNotificationRecord[]>([])

// 链路缓存
const chainCache = new Map<string | number, ChainData>()
const enrichExceptionWithChain = <T extends ExceptionAlert>(item: T) => item
const generateMockProcessingChain = (eventId: string | number): ChainData => ({
  stages: [
    { name: '事件检测', status: 'completed', timestamp: new Date().toISOString(), details: `事件 ${eventId} 被检测到。` },
    { name: 'AI 研判', status: 'processing', timestamp: new Date(Date.now() + 10000).toISOString(), details: '正在进行多模态分析...' },
    { name: '处置建议', status: 'pending' },
    { name: '执行处置', status: 'pending' },
    { name: '效果评估', status: 'pending' }
  ],
  totalDuration: 0
})

const normalizeChainStatus = (status: unknown): ChainStage['status'] => {
  const normalized = String(status ?? '').trim().toLowerCase()
  if (['success', 'completed', 'done', 'resolved', 'finished'].includes(normalized)) return 'completed'
  if (['running', 'processing', 'in_progress', 'active'].includes(normalized)) return 'processing'
  return 'pending'
}

const normalizeChainData = (payload: any): ChainData | null => {
  if (!payload || !Array.isArray(payload.stages) || !payload.stages.length) return null

  const stages = payload.stages
    .map((stage: any) => {
      const name = String(stage?.name ?? stage?.stageName ?? '').trim()
      if (!name) return null
      return {
        name,
        status: normalizeChainStatus(stage?.status),
        timestamp: typeof stage?.timestamp === 'string' ? stage.timestamp : undefined,
        details: stage?.details ?? stage?.detail ?? stage?.metadata
      } as ChainStage
    })
    .filter(Boolean) as ChainStage[]

  if (!stages.length) return null

  return {
    stages,
    totalDuration: Number(payload.totalDuration ?? 0) || 0
  }
}

const primeChainCache = async (rows: ExceptionAlert[]) => {
  const eventIds = rows
    .map(row => row.id)
    .filter((id): id is string | number => id !== undefined && id !== null)
    .filter(id => !chainCache.has(id))

  if (!eventIds.length) return

  const results = await Promise.allSettled(eventIds.map(eventId => getProcessingChain(eventId)))
  results.forEach((result, index) => {
    if (result.status !== 'fulfilled') return
    const normalized = normalizeChainData(result.value?.data)
    if (normalized) {
      chainCache.set(eventIds[index], normalized)
    }
  })
}

// ============ API 调用 ============
const fetchList = async () => {
  try {
    loading.value = true
    const res = await listExceptions({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword,
      status: query.status
    } as any)

    const rows = (res.rows || []) as ExceptionAlert[]
    // 为每条记录补充链路状态
    list.value = rows.map((item: any) => enrichExceptionWithChain(item))
    total.value = res.total || 0
    list.value = rows
    await primeChainCache(rows)

    const activeEventId = selectedEvent.value?.id
    if (activeEventId && !chainData.value && chainCache.has(activeEventId)) {
      chainData.value = chainCache.get(activeEventId) || null
    }
  } catch (e) {
    console.error('加载事件列表失败:', e)
    ElMessage.error('事件列表加载失败')
  } finally {
    loading.value = false
  }
}

const fetchChain = async (eventId: string | number) => {
  chainLoading.value = true
  chainData.value = chainCache.get(eventId) || null
  let requestFailed = false
  try {
    const res = await getProcessingChain(eventId)
    const normalized = normalizeChainData(res.data)
    if (res.data && res.data.stages) {
      chainData.value = res.data as ChainData
    } else {
      // 后端可能暂未返回，使用 mock 作为降级
      chainData.value = generateMockProcessingChain(eventId) as ChainData
    }
    // 缓存链路信息
    if (normalized) {
      chainData.value = normalized
    }
    if (chainData.value) {
      chainCache.set(eventId, chainData.value)
    }
  } catch {
    requestFailed = true
    chainData.value = chainCache.get(eventId) || null
    // 降级到模拟数据
    chainData.value = generateMockProcessingChain(eventId) as ChainData
  } finally {
    if (requestFailed) {
      chainData.value = chainCache.get(eventId) || null
    }
    chainLoading.value = false
  }
}

const fetchInsight = async () => {
  if (!selectedEvent.value?.id) return
  insightLoading.value = true
  insightData.value = null
  try {
    const res = await getEventInsight(selectedEvent.value.id)
    const payload = res.data || {}
    insightData.value = normalizeInsight(payload)
  } catch {
    insightData.value = null
  } finally {
    insightLoading.value = false
  }
}

const loadNotifications = async () => {
  notificationItems.value = await loadPlatformNotifications('event')
}

// ============ 数据处理 ============
const normalizeInsight = (payload: any): InsightSummary => {
  const getText = (...vals: unknown[]) => {
    for (const v of vals) {
      if (typeof v === 'string' && v.trim()) return v.trim()
    }
    return ''
  }
  const getList = (v: unknown) => {
    if (Array.isArray(v)) return v.map(String).filter(Boolean)
    const t = getText(v)
    return t ? t.split(/[,，;；、\n]+/).map(s => s.trim()).filter(Boolean) : []
  }

  const riskLevel = getText(payload.risk?.riskLevel, payload.risk?.level, payload.riskLevel, '').toLowerCase()
  const riskLabelMap: Record<string, string> = { high: '高风险', medium: '中风险', low: '低风险' }
  const normalizedLevel = ['high', 'danger', 'critical'].some(k => riskLevel.includes(k)) ? 'high'
    : ['medium', 'warning'].some(k => riskLevel.includes(k)) ? 'medium'
    : ['low', 'normal'].some(k => riskLevel.includes(k)) ? 'low' : 'unknown'

  return {
    overview: getText(payload.summary, payload.overview, payload.abnormalOverview, '暂无概述'),
    riskLevel: normalizedLevel,
    riskLabel: riskLabelMap[normalizedLevel] || '待判断',
    reasons: getList(payload.risk?.possibleCauses || payload.risk?.analysisReasons || payload.possibleCauses || payload.analysisReasons),
    suggestedActions: getList(payload.advice?.suggestedActions || payload.advice?.actions || payload.suggestedActions || payload.actions)
  }
}

const getChainStages = (row: any) => {
  if (row.stages) return row.stages
  const cached = row.id ? chainCache.get(row.id) : null
  return cached?.stages || []
}

const getChainLabel = (row: any) => {
  const stages = getChainStages(row)
  if (!stages.length) return '-'
  const completed = stages.filter((s: any) => s.status === 'completed').length
  const processing = stages.find((s: any) => s.status === 'processing')
  if (processing) return processing.name
  if (completed === stages.length) return '已闭环'
  return `${completed}/${stages.length}`
}

const flattenDetails = (details: Record<string, any>) => {
  const result: Record<string, string> = {}
  for (const [key, val] of Object.entries(details)) {
    if (val == null) continue
    if (Array.isArray(val)) {
      result[key] = val.map(item => typeof item === 'object' ? JSON.stringify(item) : String(item)).join(', ')
    } else if (typeof val === 'object') {
      for (const [subKey, subVal] of Object.entries(val)) {
        result[subKey] = String(subVal ?? '')
      }
    } else {
      result[key] = String(val)
    }
  }
  return result
}

const formatDetailKey = (key: string) => {
  const labelMap: Record<string, string> = {
    abnormalType: '异常类型',
    abnormalValue: '异常值',
    snapshotCount: '快照数',
    summary: '阶段摘要',
    detail: '执行说明',
    progress: '完成度',
    agentKey: 'Agent 标识',
    riskLevel: '风险等级',
    riskScore: '风险分数',
    suggestion: '处置建议',
    notificationLevel: '通知等级',
    autoExecute: '自动执行',
    executionStatus: '执行状态',
    executionResult: '执行结果',
    actualOutcome: '实际结果',
    feedbackScore: '反馈分数',
    auditCount: '审计记录数'
  }
  return labelMap[key] || key
}

const formatTime = (timestamp: string) => {
  try {
    return new Date(timestamp).toLocaleTimeString('zh-CN')
  } catch {
    return timestamp
  }
}

// ============ 统计指标 ============
const pendingCount = computed(() => list.value.filter((r: ExceptionAlert) => String(r.state ?? '0') !== '1').length)
const resolvedCount = computed(() => list.value.filter((r: ExceptionAlert) => String(r.state) === '1').length)
const avgChainDuration = computed(() => {
  if (!chainCache.size) return 0
  return Math.round(
    Array.from(chainCache.values()).reduce((sum, chain) => {
      if (chain.totalDuration > 0) return sum + chain.totalDuration
      return sum + chain.stages.length * 15
    }, 0) / chainCache.size
  )
})

const insightRiskTagType = computed(() => {
  if (!insightData.value) return 'info'
  const m: Record<string, string> = { high: 'danger', medium: 'warning', low: 'success' }
  return m[insightData.value.riskLevel] || 'info'
})

// ============ 事件处理 ============
const handleRowClick = (row: ExceptionAlert) => {
  selectedEvent.value = row
  if (row.id) {
    fetchChain(row.id)
    fetchInsight()
  }
}

const handleCurrentChange = (row: ExceptionAlert | null) => {
  if (row) handleRowClick(row)
}

const resetQuery = () => {
  query.keyword = ''
  query.status = ''
  query.pageNum = 1
  fetchList()
}

const handleSearchClick = async () => {
  await openPlatformSearch(router, 'tech')
}
const handleNotificationItem = (item: PlatformNotificationRecord) => openPlatformNotification(router, item)
const handleNotificationClick = () => openAllPlatformNotifications(router)

const jumpToEvent = (row: ExceptionAlert) => {
  router.push({ path: '/event', query: { type: row.type || '', userId: String(row.userId || '') } })
}
const jumpToSubject = (row: ExceptionAlert) => {
  if (row.userId) router.push({ path: '/subject', query: { userId: String(row.userId) } })
}
const jumpToDevice = (row: ExceptionAlert) => {
  if (row.deviceId) router.push({ path: '/device', query: { deviceId: String(row.deviceId) } })
}

// ============ 生命周期 ============
onMounted(() => {
  fetchList()
  loadNotifications()
})
</script>

<style scoped lang="scss">
.toolbar-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.panel.section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

// 统计卡片
.chain-summary-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.summary-card {
  padding: 16px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 6px;

  .sc-label {
    font-size: 12px;
    color: #64748b;
    font-weight: 500;
  }

  .sc-value {
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
    line-height: 1;
  }

  .sc-unit {
    font-size: 12px;
    color: #94a3b8;
    font-weight: 400;
    margin-left: 2px;
  }
}

.text-warning { color: #f59e0b; }
.text-success { color: #10b981; }
.text-danger { color: #ef4444; }
.digital-font { font-family: 'Courier New', monospace; }

// 链路内联进度
.chain-inline-progress {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chain-dots {
  display: flex;
  gap: 3px;
  align-items: center;
}

.chain-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1.5px solid #d1d5db;
  background: white;
  transition: all 0.2s;

  &.completed {
    background: #10b981;
    border-color: #10b981;
  }

  &.processing {
    background: #f59e0b;
    border-color: #f59e0b;
    animation: pulse-dot 1.5s infinite;
  }

  &.pending {
    background: #e5e7eb;
    border-color: #d1d5db;
  }
}

.chain-label {
  font-size: 11px;
  color: #64748b;
}

// 侧栏
.aside-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel.aside-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.aside-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.detail-subtitle {
  font-size: 12px;
  color: #94a3b8;
  margin: 4px 0 0;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-detail {
  padding: 24px 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;

  .empty-title {
    font-weight: 600;
    color: #64748b;
    margin-bottom: 8px;
  }
}

// 链路信息条
.chain-info-bar {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 6px;
  border-left: 3px solid #0ea5e9;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 11px;
  color: #94a3b8;
}

.info-value {
  font-size: 13px;
  color: #1e293b;
  font-weight: 500;
}

// 时间轴
.chain-timeline {
  display: flex;
  flex-direction: column;
}

.tl-item {
  display: flex;
  gap: 12px;
  position: relative;

  &.tl-completed .tl-dot {
    background: #10b981;
    border-color: #10b981;
    color: white;
  }

  &.tl-processing .tl-dot {
    background: #f59e0b;
    border-color: #f59e0b;
    color: white;
  }

  &.tl-pending .tl-dot {
    background: #e5e7eb;
    border-color: #d1d5db;
    color: #94a3b8;
  }
}

.tl-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.tl-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #d1d5db;
  background: white;
  font-size: 12px;
  z-index: 1;
}

.tl-line {
  width: 2px;
  flex: 1;
  min-height: 16px;
  background: #e5e7eb;
}

.tl-body {
  flex: 1;
  padding-bottom: 16px;
}

.tl-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.tl-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.tl-time {
  font-size: 11px;
  color: #94a3b8;
}

.tl-details {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.tl-detail-row {
  display: flex;
  gap: 6px;
  font-size: 12px;
  padding: 2px 8px;
  background: #f8fafc;
  border-radius: 3px;
}

.tl-detail-key {
  color: #64748b;
  font-weight: 500;
  min-width: 60px;
  flex-shrink: 0;
}

.tl-detail-val {
  color: #334155;
  word-break: break-all;
}

.tl-detail-text {
  font-size: 12px;
  color: #64748b;
  margin: 0;
}

// 快捷操作
.chain-actions {
  display: flex;
  gap: 8px;
  padding-top: 4px;
}

// 研判摘要
.insight-mini {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.im-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  font-size: 13px;
}

.im-label {
  color: #64748b;
  font-weight: 500;
  min-width: 60px;
  flex-shrink: 0;
}

.im-value {
  color: #334155;
  line-height: 1.5;
}

.im-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.insight-chip {
  padding: 2px 8px;
  background: #f1f5f9;
  border-radius: 3px;
  font-size: 11px;
  color: #475569;

  &.insight-chip-action {
    background: #eff6ff;
    color: #2563eb;
  }
}

// 加载占位
.chain-loading {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 0;
}

.loading-line {
  height: 12px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  width: 100%;

  &.loading-line-lg { height: 16px; width: 80%; }
  &.loading-line-short { width: 50%; }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
