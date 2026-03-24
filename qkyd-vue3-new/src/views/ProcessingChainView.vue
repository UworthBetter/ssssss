<template>
  <PlatformPageShell
    title="处理链追踪"
    subtitle="追踪真实异常事件从检测到闭环的处理过程，查看处理状态、链路节点与 AI 研判摘要。"
    eyebrow="PROCESSING CHAIN"
    aside-title="链路详情"
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
          <el-input v-model="query.keyword" placeholder="搜索异常类型 / 服务对象" clearable style="width: 220px" />
          <el-select v-model="query.status" placeholder="处理状态" clearable style="width: 140px">
            <el-option label="处理中" value="pending" />
            <el-option label="已闭环" value="resolved" />
          </el-select>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>
    </template>

    <div class="panel section">
      <div class="chain-summary-row">
        <div class="summary-card">
          <div class="sc-label">事件总数</div>
          <div class="sc-value digital-font">{{ filteredList.length }}</div>
        </div>
        <div class="summary-card">
          <div class="sc-label">待处理</div>
          <div class="sc-value digital-font text-warning">{{ pendingCount }}</div>
        </div>
        <div class="summary-card">
          <div class="sc-label">已闭环</div>
          <div class="sc-value digital-font text-success">{{ resolvedCount }}</div>
        </div>
        <div class="summary-card">
          <div class="sc-label">平均链路耗时</div>
          <div class="sc-value digital-font">{{ avgChainDuration }}<span class="sc-unit">ms</span></div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="pagedList"
        stripe
        highlight-current-row
        @current-change="handleCurrentChange"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="事件ID" width="90" />
        <el-table-column prop="nickName" label="服务对象" min-width="110" />
        <el-table-column prop="type" label="异常类型" min-width="120" />
        <el-table-column prop="value" label="指标值" min-width="120" />
        <el-table-column label="链路进度" min-width="180">
          <template #default="{ row }">
            <div class="chain-inline-progress">
              <div class="chain-dots">
                <div
                  v-for="(stage, index) in getChainStages(row)"
                  :key="`${row.id}-${index}`"
                  class="chain-dot"
                  :class="stage.status"
                  :title="stage.name"
                />
              </div>
              <span class="chain-label">{{ getChainLabel(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="String(row.state || '0') === '1' ? 'success' : 'danger'">
              {{ String(row.state || '0') === '1' ? '已闭环' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发生时间" min-width="170" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="filteredList.length"
          layout="total, prev, pager, next"
        />
      </div>
    </div>

    <template #aside>
      <div class="aside-stack">
        <div class="panel aside-card detail-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">处理链时间轴</div>
              <p class="detail-subtitle">展示事件检测、研判、处置等真实链路节点。</p>
            </div>
            <el-tag v-if="selectedEvent" :type="String(selectedEvent.state || '0') === '1' ? 'success' : 'warning'" effect="light">
              {{ String(selectedEvent.state || '0') === '1' ? '已闭环' : '处理中' }}
            </el-tag>
          </div>

          <div v-if="selectedEvent" class="detail-body">
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

            <div v-if="chainLoading" class="chain-loading">
              <div class="loading-line loading-line-lg"></div>
              <div class="loading-line"></div>
              <div class="loading-line"></div>
            </div>

            <div v-else-if="chainData?.stages?.length" class="chain-timeline">
              <div v-for="(stage, index) in chainData.stages" :key="`${stage.name}-${index}`" class="tl-item" :class="`tl-${stage.status}`">
                <div class="tl-marker">
                  <div class="tl-dot">
                    <el-icon v-if="stage.status === 'completed'"><Check /></el-icon>
                    <el-icon v-else-if="stage.status === 'processing'"><Loading /></el-icon>
                    <el-icon v-else><Clock /></el-icon>
                  </div>
                  <div v-if="index < chainData.stages.length - 1" class="tl-line"></div>
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

            <div v-else class="empty-detail">后端暂未返回该事件的处理链节点，当前仅展示真实空态。</div>

            <div class="chain-actions">
              <el-button type="primary" size="small" @click="jumpToEvent(selectedEvent)">进入事件中心</el-button>
              <el-button size="small" @click="jumpToSubject(selectedEvent)" :disabled="!selectedEvent.userId">查看对象</el-button>
              <el-button size="small" @click="jumpToDevice(selectedEvent)" :disabled="!selectedEvent.deviceId">查看设备</el-button>
            </div>
          </div>

          <div v-else class="empty-detail">
            <div class="empty-title">尚未选中事件</div>
            <p>从左侧列表选择一个真实事件后，这里会显示该事件的处理链时间轴。</p>
          </div>
        </div>

        <div class="panel aside-card insight-summary-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">AI 研判摘要</div>
              <p class="detail-subtitle">基于真实事件洞察接口生成的风险摘要与建议动作。</p>
            </div>
            <el-button v-if="selectedEvent" text type="primary" :loading="insightLoading" @click="fetchInsight">刷新研判</el-button>
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
                  <span v-for="reason in insightData.reasons.slice(0, 3)" :key="reason" class="insight-chip">{{ reason }}</span>
                </div>
              </div>
              <div v-if="insightData.suggestedActions.length" class="im-row">
                <span class="im-label">建议动作</span>
                <div class="im-chips">
                  <span v-for="action in insightData.suggestedActions.slice(0, 3)" :key="action" class="insight-chip insight-chip-action">{{ action }}</span>
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
            {{ selectedEvent ? '当前事件暂无 AI 研判结果。' : '选中事件后展示 AI 研判摘要。' }}
          </div>
        </div>
      </div>
    </template>
  </PlatformPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check, Clock, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { PlatformPageShell, PlatformSearchEntry, getPlatformSearchPresentation, openPlatformSearch } from '@/components/platform'
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
const page = ref(1)
const pageSize = 10
const sourceList = ref<ExceptionAlert[]>([])
const selectedEvent = ref<ExceptionAlert | null>(null)
const chainLoading = ref(false)
const chainData = ref<ChainData | null>(null)
const insightLoading = ref(false)
const insightData = ref<InsightSummary | null>(null)

const query = reactive({
  keyword: '',
  status: ''
})

const chainCache = new Map<string | number, ChainData>()

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

const filteredList = computed(() => {
  const keyword = query.keyword.trim()
  return sourceList.value.filter((item) => {
    if (query.status === 'pending' && String(item.state || '0') === '1') return false
    if (query.status === 'resolved' && String(item.state || '0') !== '1') return false
    if (!keyword) return true
    return [item.nickName, item.type, item.location].some((field) => String(field || '').includes(keyword))
  })
})

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

const pendingCount = computed(() => filteredList.value.filter((item) => String(item.state || '0') !== '1').length)
const resolvedCount = computed(() => filteredList.value.filter((item) => String(item.state || '0') === '1').length)
const avgChainDuration = computed(() => {
  if (!chainCache.size) return 0
  const total = Array.from(chainCache.values()).reduce((sum, item) => sum + (item.totalDuration || item.stages.length * 15), 0)
  return Math.round(total / chainCache.size)
})

const normalizeInsight = (payload: any): InsightSummary => {
  const toText = (...values: unknown[]) => values.map((item) => String(item || '').trim()).find(Boolean) || ''
  const toList = (value: unknown) => {
    if (Array.isArray(value)) return value.map((item) => String(item)).filter(Boolean)
    const text = toText(value)
    return text ? text.split(/[,，；;\n]+/).map((item) => item.trim()).filter(Boolean) : []
  }

  const riskLevel = toText(payload.risk?.riskLevel, payload.risk?.level, payload.riskLevel).toLowerCase()
  const normalizedLevel = ['high', 'critical', 'danger'].some((item) => riskLevel.includes(item))
    ? 'high'
    : ['medium', 'warning'].some((item) => riskLevel.includes(item))
      ? 'medium'
      : ['low', 'normal'].some((item) => riskLevel.includes(item))
        ? 'low'
        : 'unknown'

  return {
    overview: toText(payload.summary, payload.overview, payload.abnormalOverview, '暂无概述'),
    riskLevel: normalizedLevel,
    riskLabel: { high: '高风险', medium: '中风险', low: '低风险', unknown: '待判定' }[normalizedLevel] || '待判定',
    reasons: toList(payload.risk?.possibleCauses || payload.risk?.analysisReasons || payload.analysisReasons),
    suggestedActions: toList(payload.advice?.suggestedActions || payload.advice?.actions || payload.suggestedActions)
  }
}

const insightRiskTagType = computed(() => {
  if (!insightData.value) return 'info'
  return { high: 'danger', medium: 'warning', low: 'success' }[insightData.value.riskLevel] || 'info'
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listExceptions({ pageNum: 1, pageSize: 100 })
    sourceList.value = (res.rows || []) as ExceptionAlert[]

    if (selectedEvent.value?.id) {
      const matched = sourceList.value.find((item) => item.id === selectedEvent.value?.id)
      selectedEvent.value = matched || null
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('事件列表加载失败')
  } finally {
    loading.value = false
  }
}

const fetchChain = async (eventId: string | number) => {
  chainLoading.value = true
  chainData.value = chainCache.get(eventId) || null
  try {
    const res = await getProcessingChain(eventId)
    const normalized = normalizeChainData(res.data)
    chainData.value = normalized ?? (chainCache.get(eventId) || null)
    if (normalized) {
      chainCache.set(eventId, normalized)
    }
  } catch {
    chainData.value = chainCache.get(eventId) || null
  } finally {
    chainLoading.value = false
  }
}

const fetchInsight = async () => {
  if (!selectedEvent.value?.id) return
  insightLoading.value = true
  insightData.value = null
  try {
    const res = await getEventInsight(selectedEvent.value.id)
    insightData.value = normalizeInsight(res.data || {})
  } catch {
    insightData.value = null
  } finally {
    insightLoading.value = false
  }
}

const getChainStages = (row: ExceptionAlert) => {
  if (!row.id) return []
  return chainCache.get(row.id)?.stages || []
}

const getChainLabel = (row: ExceptionAlert) => {
  const stages = getChainStages(row)
  if (!stages.length) return '暂无链路'
  const processing = stages.find((item) => item.status === 'processing')
  if (processing) return processing.name
  if (stages.every((item) => item.status === 'completed')) return '已闭环'
  return `${stages.filter((item) => item.status === 'completed').length}/${stages.length}`
}

const flattenDetails = (details: Record<string, any>) => {
  const result: Record<string, string> = {}
  Object.entries(details).forEach(([key, value]) => {
    if (value == null) return
    if (Array.isArray(value)) {
      result[key] = value.map((item) => String(item)).join(', ')
      return
    }
    if (typeof value === 'object') {
      Object.entries(value).forEach(([subKey, subValue]) => {
        result[subKey] = String(subValue ?? '')
      })
      return
    }
    result[key] = String(value)
  })
  return result
}

const formatDetailKey = (key: string) => ({
  abnormalType: '异常类型',
  abnormalValue: '异常值',
  summary: '摘要',
  detail: '说明',
  riskLevel: '风险等级',
  suggestion: '建议'
}[key] || key)

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  if (!Number.isFinite(date.getTime())) return timestamp
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

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
  page.value = 1
}

const handleSearchClick = async () => {
  await openPlatformSearch(router, 'event')
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

const jumpToEvent = (row: ExceptionAlert) => {
  router.push({ path: '/event', query: { type: normalizeEventTypeQuery(row.type), userId: String(row.userId || '') } })
}

const jumpToSubject = (row: ExceptionAlert) => {
  if (!row.userId) return
  router.push({ path: '/subject', query: { userId: String(row.userId) } })
}

const jumpToDevice = (row: ExceptionAlert) => {
  if (!row.deviceId) return
  router.push({ path: '/device', query: { deviceId: String(row.deviceId) } })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.toolbar-stack { display: flex; flex-direction: column; gap: 12px; }
.toolbar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.panel.section { display: flex; flex-direction: column; gap: 16px; }
.pagination { display: flex; justify-content: flex-end; padding-top: 8px; }
.header-actions { width: min(360px, 100%); }

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
}

.sc-label { font-size: 12px; color: #64748b; font-weight: 500; }
.sc-value { font-size: 24px; font-weight: 700; color: #1e293b; line-height: 1; }
.sc-unit { font-size: 12px; color: #94a3b8; font-weight: 400; margin-left: 2px; }

.text-warning { color: #f59e0b; }
.text-success { color: #10b981; }
.digital-font { font-family: 'Courier New', monospace; }

.chain-inline-progress { display: flex; flex-direction: column; gap: 4px; }
.chain-dots { display: flex; gap: 3px; align-items: center; }

.chain-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1.5px solid #d1d5db;
  background: white;
}

.chain-dot.completed { background: #10b981; border-color: #10b981; }
.chain-dot.processing { background: #f59e0b; border-color: #f59e0b; animation: pulse-dot 1.5s infinite; }
.chain-dot.pending { background: #e5e7eb; border-color: #d1d5db; }
.chain-label { font-size: 11px; color: #64748b; }

.aside-stack { display: flex; flex-direction: column; gap: 16px; }
.panel.aside-card { background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; }
.detail-card, .detail-body { display: flex; flex-direction: column; gap: 12px; }
.detail-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; gap: 12px; }
.aside-card-title { font-size: 14px; font-weight: 600; color: #1e293b; }
.detail-subtitle { font-size: 12px; color: #94a3b8; margin: 4px 0 0; line-height: 1.5; }

.empty-detail {
  padding: 24px 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

.empty-title { font-weight: 600; color: #64748b; margin-bottom: 8px; }

.chain-info-bar {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 6px;
  border-left: 3px solid #0ea5e9;
}

.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 11px; color: #94a3b8; }
.info-value { font-size: 13px; color: #1e293b; font-weight: 500; }

.chain-timeline { display: flex; flex-direction: column; }

.tl-item { display: flex; gap: 12px; position: relative; }
.tl-marker { display: flex; flex-direction: column; align-items: center; flex-shrink: 0; }

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

.tl-completed .tl-dot { background: #10b981; border-color: #10b981; color: white; }
.tl-processing .tl-dot { background: #f59e0b; border-color: #f59e0b; color: white; }
.tl-pending .tl-dot { background: #e5e7eb; border-color: #d1d5db; color: #94a3b8; }

.tl-line { width: 2px; flex: 1; min-height: 16px; background: #e5e7eb; }
.tl-body { flex: 1; padding-bottom: 16px; }
.tl-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; gap: 10px; }
.tl-name { font-size: 13px; font-weight: 600; color: #1e293b; }
.tl-time { font-size: 11px; color: #94a3b8; }
.tl-details { display: flex; flex-direction: column; gap: 3px; }

.tl-detail-row {
  display: flex;
  gap: 6px;
  font-size: 12px;
  padding: 2px 8px;
  background: #f8fafc;
  border-radius: 3px;
}

.tl-detail-key { color: #64748b; font-weight: 500; min-width: 60px; flex-shrink: 0; }
.tl-detail-val { color: #334155; word-break: break-all; }
.tl-detail-text { font-size: 12px; color: #64748b; margin: 0; line-height: 1.6; }

.chain-actions { display: flex; gap: 8px; padding-top: 4px; flex-wrap: wrap; }

.insight-mini { display: flex; flex-direction: column; gap: 10px; }
.im-row { display: flex; gap: 8px; align-items: flex-start; font-size: 13px; }
.im-label { color: #64748b; font-weight: 500; min-width: 60px; flex-shrink: 0; }
.im-value { color: #334155; line-height: 1.5; }
.im-chips { display: flex; flex-wrap: wrap; gap: 4px; }

.insight-chip {
  padding: 2px 8px;
  background: #f1f5f9;
  border-radius: 3px;
  font-size: 11px;
  color: #475569;
}

.insight-chip-action { background: #eff6ff; color: #2563eb; }

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
}

.loading-line-lg { height: 16px; width: 80%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@media (max-width: 1200px) {
  .chain-summary-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
