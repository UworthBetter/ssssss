<template>
  <PlatformPageShell
    title="事件中心"
    subtitle="围绕异常事件完成筛选、研判、处置和关闭的统一工作流，并为后续状态流转与协同联动预留稳定骨架。"
    eyebrow="EVENT CENTER"
    aside-title="事件详情"
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
      </div>
    </template>

    <template #toolbar>
      <div class="toolbar-stack">
        <PlatformContextFilterBar
          v-model="contextFilters"
          summary-label="当前工作上下文"
          summary-value="事件中心 / 研判与处置流"
          @confirm="handleContextConfirm"
          @reset="handleContextReset"
        />

        <div class="toolbar">
          <el-input v-model="query.type" placeholder="异常类型" clearable style="width: 180px" />
          <el-select v-model="query.state" placeholder="处理状态" clearable style="width: 140px">
            <el-option label="待处理" value="0" />
            <el-option label="已处理" value="1" />
          </el-select>
          <el-input-number v-model="userIdFilter" :min="0" :controls="false" placeholder="按用户ID查询" />
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>
    </template>

    <div class="panel section">
      <el-table v-loading="loading" :data="list" stripe highlight-current-row @current-change="handleCurrentChange" @row-click="handleRowSelect">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="nickName" label="姓名" min-width="100" />
        <el-table-column prop="userId" label="用户ID" min-width="100" />
        <el-table-column prop="deviceId" label="设备ID" min-width="100" />
        <el-table-column prop="type" label="异常类型" min-width="120" />
        <el-table-column prop="value" label="指标值" min-width="120" />
        <el-table-column label="事件阶段" min-width="120">
          <template #default="{ row }">
            <el-tag :type="workflowTagType(getWorkflowState(row))" effect="light">{{ workflowLabelMap[getWorkflowState(row)] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.state === '1' ? 'success' : 'danger'">{{ row.state === '1' ? '已处理' : '待处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发生时间" min-width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openHandle(row)">处置</el-button>
            <el-button text @click="showDetail(row.id)">详情</el-button>
            <el-button text @click="handleRowSelect(row)">查看面板</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, sizes, prev, pager, next" @change="fetchList" />
      </div>
    </div>

    <template #aside>
      <div class="aside-stack">
        <PlatformNotificationEntry
          title="事件协同通知"
          :unread-count="notificationItems.length"
          :items="notificationItems"
          @click-item="handleNotificationItem"
          @click-all="handleNotificationClick"
        />

        <div class="panel aside-card insight-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">AI 研判卡</div>
              <p class="detail-subtitle">多 Agent 先解析事件，再补充上下文并生成处置建议。</p>
            </div>
            <el-tag v-if="selectedEvent && eventInsight" :type="riskTagType(eventInsight.riskLevel)" effect="light">
              {{ eventInsight.riskLabel }}
            </el-tag>
            <el-tag v-else-if="insightLoading" type="info" effect="light">研判中</el-tag>
            <el-tag v-else type="info" effect="light">待生成</el-tag>
          </div>

          <div v-if="selectedEvent" class="detail-body">
            <div v-if="insightLoading" class="insight-loading">
              <div class="loading-line loading-line-lg"></div>
              <div class="loading-line"></div>
              <div class="loading-line"></div>
              <div class="loading-line loading-line-short"></div>
            </div>

            <template v-else-if="eventInsight">
              <div class="insight-overview">
                <div class="insight-label">异常概述</div>
                <p class="insight-text">{{ eventInsight.overview }}</p>
              </div>

              <div class="insight-section">
                <div class="insight-label">多 Agent 处理过程</div>
                <div class="process-list">
                  <div v-for="(step, index) in eventInsight.processSteps" :key="step.key" class="process-item">
                    <div class="process-index">{{ index + 1 }}</div>
                    <div class="process-content">
                      <div class="process-head">
                        <span class="process-title">{{ step.title }}</span>
                        <el-tag size="small" :type="processTagType(step.status)" effect="light">
                          {{ processStatusLabel(step.status) }}
                        </el-tag>
                      </div>
                      <p class="process-summary">{{ step.summary }}</p>
                      <p class="process-detail">{{ step.detail }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="insight-section">
                <div class="insight-label">风险等级</div>
                <div class="insight-row">
                  <el-tag :type="riskTagType(eventInsight.riskLevel)" effect="light">
                    {{ eventInsight.riskLabel }}
                  </el-tag>
                  <span class="insight-note">
                    {{ eventInsight.immediateAction ? '建议立即处理' : '可先观察并补充上下文' }}
                  </span>
                </div>
              </div>

              <div class="insight-section">
                <div class="insight-label">分析理由</div>
                <div class="insight-chips">
                  <span v-for="reason in eventInsight.reasons.slice(0, 4)" :key="reason" class="insight-chip">
                    {{ reason }}
                  </span>
                </div>
              </div>

              <div class="insight-section">
                <div class="insight-label">通知对象</div>
                <div class="insight-chips">
                  <span v-for="target in eventInsight.notifyWho.slice(0, 4)" :key="target" class="insight-chip">
                    {{ target }}
                  </span>
                </div>
              </div>

              <div class="insight-section">
                <div class="insight-label">建议动作</div>
                <div class="insight-chips">
                  <span v-for="action in eventInsight.suggestedActions.slice(0, 4)" :key="action" class="insight-chip insight-chip-action">
                    {{ action }}
                  </span>
                </div>
              </div>

              <p class="insight-footnote">
                {{ insightError ? `兜底显示：${insightError}` : eventInsight.sourceLabel }}
              </p>
            </template>

            <div v-else class="empty-detail">
              当前事件暂未生成研判结果，切换事件后会自动刷新。
            </div>
          </div>

          <div v-else class="empty-detail">
            从左侧列表中选择一个事件后，这里会展示多 Agent 研判卡。
          </div>
        </div>

        <div class="panel aside-card detail-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">当前选中事件</div>
              <p class="detail-subtitle">右侧详情面板是第一版事件工作流骨架，已经支持对象和设备的真实跳转。</p>
            </div>
            <el-tag v-if="selectedEvent" :type="workflowTagType(selectedWorkflowState)" effect="light">{{ workflowLabelMap[selectedWorkflowState] }}</el-tag>
          </div>

          <div v-if="selectedEvent" class="detail-body">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="事件ID">{{ selectedEvent.id || '-' }}</el-descriptions-item>
              <el-descriptions-item label="服务对象">{{ selectedEvent.nickName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="用户ID">{{ selectedEvent.userId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="设备ID">{{ selectedEvent.deviceId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="异常类型">{{ selectedEvent.type || '-' }}</el-descriptions-item>
              <el-descriptions-item label="发生位置">{{ selectedEvent.location || '-' }}</el-descriptions-item>
            </el-descriptions>

            <div class="workflow-block">
              <div class="block-title">事件工作流</div>
              <el-steps :active="workflowStepIndex(selectedWorkflowState)" finish-status="success" simple>
                <el-step title="新告警" />
                <el-step title="待研判" />
                <el-step title="处理中" />
                <el-step title="已关闭" />
              </el-steps>
            </div>

            <div class="workflow-form">
              <div class="block-title">协同字段</div>
              <el-form label-position="top">
                <el-form-item label="事件阶段">
                  <el-select v-model="workflowDraft">
                    <el-option v-for="item in workflowOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="责任人">
                  <el-select v-model="ownerDraft">
                    <el-option v-for="item in ownerOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="SLA">
                  <el-select v-model="slaDraft">
                    <el-option label="30 分钟" value="30m" />
                    <el-option label="2 小时" value="2h" />
                    <el-option label="24 小时" value="24h" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>

            <div class="workflow-actions">
              <el-button type="primary" @click="openHandle(selectedEvent)">进入处置</el-button>
              <el-button @click="jumpToSubject(selectedEvent)">查看对象</el-button>
              <el-button @click="jumpToDevice(selectedEvent)">查看设备</el-button>
            </div>
          </div>

          <div v-else class="empty-detail">
            <div class="empty-title">尚未选中事件</div>
            <p>从左侧列表中选择一个事件后，这里会展示协同字段、工作流阶段和跨中心跳转入口。</p>
          </div>
        </div>
      </div>
    </template>

    <el-dialog v-model="handleDialogVisible" title="异常处置" width="560px">
      <el-form :model="handleForm" label-width="96px">
        <el-form-item label="异常ID"><el-input v-model="handleForm.id" disabled /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="handleForm.state">
            <el-radio value="0">待处理</el-radio>
            <el-radio value="1">已处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明"><el-input v-model="handleForm.updateContent" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitHandle">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="异常详情" width="760px">
      <pre class="json-block">{{ JSON.stringify(detailData, null, 2) }}</pre>
    </el-dialog>
  </PlatformPageShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter, type LocationQuery } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  PlatformContextFilterBar,
  PlatformNotificationEntry,
  PlatformPageShell,
  PlatformSearchEntry,
  dispatchPlatformAction,
  getPlatformSearchPresentation,
  loadPlatformNotifications,
  openAllPlatformNotifications,
  openPlatformNotification,
  openPlatformSearch,
  type PlatformContextFilters,
  type PlatformNotificationRecord
} from '@/components/platform'
import {
  getEventInsight,
  type EventInsightPayload,
  type EventInsightTrace,
  type EventInsightTraceStep
} from '@/api/ai'
import { getException, listExceptions, listExceptionsByUserId, updateException, type ExceptionAlert } from '@/api/health'
import { useRouteQueryListSync } from '@/composables/useRouteQueryListSync'

type WorkflowState = 'new' | 'triage' | 'in_progress' | 'closed'
type InsightRiskLevel = 'high' | 'medium' | 'low' | 'unknown'
type AgentStepStatus = 'done' | 'partial' | 'fallback' | 'waiting'

interface InsightProcessStep {
  key: string
  title: string
  summary: string
  detail: string
  status: AgentStepStatus
}

interface NormalizedInsight {
  overview: string
  riskLevel: InsightRiskLevel
  riskLabel: string
  immediateAction: boolean
  reasons: string[]
  notifyWho: string[]
  suggestedActions: string[]
  sourceLabel: string
  usesFallback: boolean
  processSteps: InsightProcessStep[]
}

const workflowLabelMap: Record<WorkflowState, string> = { new: '新告警', triage: '待研判', in_progress: '处理中', closed: '已关闭' }
const workflowOptions = [
  { label: '新告警', value: 'new' },
  { label: '待研判', value: 'triage' },
  { label: '处理中', value: 'in_progress' },
  { label: '已关闭', value: 'closed' }
] as const
const ownerOptions = ['值班护士 A', '值班护士 B', '平台运营', '医生工作台']
const insightRiskLabelMap: Record<InsightRiskLevel, string> = {
  high: '高风险',
  medium: '中风险',
  low: '低风险',
  unknown: '待判断'
}

const loading = ref(false)
const saving = ref(false)
const list = ref<ExceptionAlert[]>([])
const total = ref(0)
const userIdFilter = ref<number | undefined>(undefined)
const route = useRoute()
const router = useRouter()
const searchPresentation = getPlatformSearchPresentation('event')
const handleDialogVisible = ref(false)
const detailVisible = ref(false)
const detailData = ref<Record<string, unknown>>({})
const contextFilters = ref<PlatformContextFilters>({ timeRange: 'today', region: 'all', riskLevel: 'all', status: 'all' })
const query = reactive({ pageNum: 1, pageSize: 10, type: '', state: '' })
const routeDeviceId = ref('')
const applyRouteQuery = (routeQuery: LocationQuery) => {
  query.pageNum = 1
  query.type = String(routeQuery.type || routeQuery.keyword || '')
  query.state = String(routeQuery.state || '')
  routeDeviceId.value = String(routeQuery.deviceId || '')
  const userId = Number(routeQuery.userId || 0)
  userIdFilter.value = Number.isFinite(userId) && userId > 0 ? userId : undefined
}

const handleForm = reactive<ExceptionAlert>({ id: undefined, state: '1', updateContent: '' })
const selectedEvent = ref<ExceptionAlert | null>(null)
const workflowDraft = ref<WorkflowState>('triage')
const ownerDraft = ref('平台运营')
const slaDraft = ref('2h')
const notificationItems = ref<PlatformNotificationRecord[]>([])
const insightLoading = ref(false)
const insightError = ref('')
const eventInsight = ref<NormalizedInsight | null>(null)
let insightRequestSeq = 0

const isRecord = (value: unknown): value is Record<string, unknown> =>
  Boolean(value) && typeof value === 'object' && !Array.isArray(value)

const getText = (...values: unknown[]) => {
  for (const value of values) {
    if (value == null) continue
    if (typeof value === 'string' && value.trim()) return value.trim()
    if (typeof value === 'number' && Number.isFinite(value)) return String(value)
    if (typeof value === 'boolean') return value ? '是' : '否'
  }
  return ''
}

const getTextList = (value: unknown) => {
  if (Array.isArray(value)) {
    return value
      .flatMap((item) => getText(item))
      .map((item) => item.trim())
      .filter(Boolean)
  }

  const text = getText(value)
  if (!text) return []
  if (/[,，;；、\n]/.test(text)) {
    return text
      .split(/[,，;；、\n]+/)
      .map((item) => item.trim())
      .filter(Boolean)
  }
  return [text]
}

const getRecordList = (value: unknown): Record<string, unknown>[] => {
  if (!Array.isArray(value)) return []
  return value.filter((item): item is Record<string, unknown> => isRecord(item))
}

const uniqueTextList = (items: string[]) => Array.from(new Set(items.map((item) => item.trim()).filter(Boolean)))

const pickFromPaths = (source: unknown, paths: string[][]) => {
  for (const path of paths) {
    let current: unknown = source
    for (const key of path) {
      if (!isRecord(current)) {
        current = undefined
        break
      }
      current = current[key]
    }
    const text = getText(current)
    if (text) return text
  }
  return ''
}

const normalizeRiskLevel = (value: unknown): InsightRiskLevel => {
  const text = getText(value).toLowerCase()
  if (!text) return 'unknown'
  if (['high', 'danger', 'critical', 'urgent', 'severe', '高', '严重', '高风险'].some((item) => text.includes(item))) {
    return 'high'
  }
  if (['medium', 'warning', 'warn', 'moderate', '中', '中风险'].some((item) => text.includes(item))) {
    return 'medium'
  }
  if (['low', 'normal', 'safe', 'minor', '低', '低风险'].some((item) => text.includes(item))) {
    return 'low'
  }
  return 'unknown'
}

const countFilledValues = (values: unknown[]) =>
  values.reduce((count, value) => count + (getTextList(value).length || getText(value) ? 1 : 0), 0)

const resolveStepStatus = (resolvedCount: number, targetCount: number, usesFallback: boolean): AgentStepStatus => {
  if (usesFallback && resolvedCount > 0) return 'fallback'
  if (resolvedCount >= targetCount) return 'done'
  if (resolvedCount > 0) return 'partial'
  return 'waiting'
}

const formatBooleanStatus = (value: unknown, yesLabel: string, noLabel: string) => {
  if (typeof value !== 'boolean') return '待确认'
  return value ? yesLabel : noLabel
}

const buildProcessSteps = (
  row: ExceptionAlert,
  parsed: Record<string, unknown>,
  context: Record<string, unknown>,
  risk: Record<string, unknown>,
  advice: Record<string, unknown>,
  usesFallback: boolean,
  riskLevel: InsightRiskLevel,
  notifyWho: string[],
  suggestedActions: string[]
): InsightProcessStep[] => {
  const parserResolved = countFilledValues([
    parsed.abnormalType,
    parsed.occurredAt,
    parsed.userId,
    parsed.deviceId,
    parsed.metricName,
    parsed.metricValue
  ])
  const contextResolved = countFilledValues([
    context.age,
    context.chronicDiseases,
    context.recentHealthTrend || context.recentTrend,
    context.historicalAbnormalCount,
    context.recentSameTypeCount,
    context.deviceStatus,
    context.deviceStatusReason,
    context.lastKnownLocation
  ])
  const riskResolved = countFilledValues([
    risk.riskLevel,
    risk.riskScore,
    risk.possibleCauses,
    risk.analysisReasons,
    risk.ruleHits
  ])
  const adviceResolved = countFilledValues([
    advice.notifyWho,
    advice.suggestedActions,
    advice.offlineCheck,
    advice.contactFamily,
    advice.contactOrganization
  ])

  return [
    {
      key: 'parser',
      title: 'Agent 1 事件解析',
      summary: getText(parsed.abnormalType, row.type) || '待识别异常类型',
      detail: usesFallback
        ? `使用兜底规则补齐 ${parserResolved}/6 项解析字段`
        : `已提取 ${parserResolved}/6 项解析字段`,
      status: resolveStepStatus(parserResolved, 6, usesFallback)
    },
    {
      key: 'context',
      title: 'Agent 2 上下文补充',
      summary: [
        getText(context.age) ? `${getText(context.age)} 岁` : '年龄待补充',
        getText(context.deviceStatus) || '设备状态待补充'
      ].join(' / '),
      detail: usesFallback
        ? `采用兜底上下文，已补全 ${contextResolved}/8 项`
        : `已补全 ${contextResolved}/8 项上下文字段`,
      status: resolveStepStatus(contextResolved, 8, usesFallback)
    },
    {
      key: 'risk',
      title: 'Agent 3 风险评估',
      summary: `${insightRiskLabelMap[riskLevel]} / ${formatBooleanStatus(risk.immediateAction, '建议立即处理', '可继续观察')}`,
      detail: `已生成 ${getTextList(risk.analysisReasons).length || 0} 条理由，命中 ${getTextList(risk.ruleHits).length || 0} 条规则`,
      status: resolveStepStatus(riskResolved, 5, usesFallback)
    },
    {
      key: 'advice',
      title: 'Agent 4 处置建议',
      summary: notifyWho.length ? `通知 ${notifyWho.slice(0, 2).join('、')}` : '待生成通知对象',
      detail: `${suggestedActions.length} 项建议动作 / ${formatBooleanStatus(advice.offlineCheck, '建议线下核查', '暂不要求线下核查')}`,
      status: resolveStepStatus(adviceResolved, 5, usesFallback)
    }
  ]
}

const normalizeProcessStatus = (value: unknown): AgentStepStatus => {
  const text = getText(value).toLowerCase()
  if (['done', 'completed', 'success', 'finished'].includes(text)) return 'done'
  if (['partial', 'degraded'].includes(text)) return 'partial'
  if (['fallback', 'mock', 'derived'].includes(text)) return 'fallback'
  return 'waiting'
}

const buildProcessStepsFromTrace = (trace: EventInsightTrace | Record<string, unknown> | undefined) => {
  if (!trace || !isRecord(trace)) return []

  return getRecordList(trace.steps)
    .map((step) => step as EventInsightTraceStep)
    .map((step, index): InsightProcessStep => ({
      key: getText(step.agentKey) || `trace-step-${index}`,
      title: getText(step.agentName) || `Agent ${index + 1}`,
      summary: getText(step.summary) || '待补充',
      detail: (() => {
        const detail = getText(step.detail)
        const resolved = getText(step.resolvedCount)
        const target = getText(step.targetCount)
        if (detail) return detail
        if (resolved && target) return `已完成 ${resolved}/${target} 项`
        return '暂无详细信息'
      })(),
      status: normalizeProcessStatus(step.status)
    }))
}

const buildFallbackInsight = (row: ExceptionAlert): NormalizedInsight => {
  const overviewParts = [
    row.nickName || `用户 ${row.userId || '-'}`,
    row.type ? `${row.type}异常` : '异常事件',
    row.value ? `指标 ${row.value}` : ''
  ].filter(Boolean)

  const eventType = getText(row.type).toLowerCase()
  const numericValue = Number.parseFloat(String(row.value || '').replace(/[^\d.]/g, ''))
  const riskLevel = (() => {
    if (eventType.includes('sos')) return 'high'
    if (eventType.includes('心率')) {
      if (Number.isFinite(numericValue) && numericValue >= 120) return 'high'
      if (Number.isFinite(numericValue) && numericValue >= 100) return 'medium'
      return 'medium'
    }
    if (eventType.includes('血氧')) {
      if (Number.isFinite(numericValue) && numericValue < 90) return 'high'
      if (Number.isFinite(numericValue) && numericValue < 95) return 'medium'
      return 'medium'
    }
    if (eventType.includes('体温')) {
      if (Number.isFinite(numericValue) && numericValue >= 38.5) return 'medium'
      return 'low'
    }
    if (eventType.includes('围栏')) return 'medium'
    return 'unknown'
  })()

  const reasons = [
    row.type ? `异常类型：${row.type}` : '异常类型待补充',
    row.value ? `当前指标：${row.value}` : '指标值待补充',
    row.deviceId ? `关联设备：${row.deviceId}` : '设备信息待补充'
  ]

  const notifyWho =
    riskLevel === 'high'
      ? ['值班护士', '家属', '机构值班人']
      : riskLevel === 'medium'
        ? ['值班护士', '家属']
        : ['值班护士']

  const suggestedActions =
    riskLevel === 'high'
      ? ['立即电话回访', '安排线下核查', '同步家属']
      : riskLevel === 'medium'
        ? ['复测指标', '持续观察', '通知值班护士']
        : ['记录并复核', '继续观察']

  return {
    overview: `${overviewParts.join('，') || '当前事件待补充'}。`,
    riskLevel,
    riskLabel: insightRiskLabelMap[riskLevel],
    immediateAction: riskLevel === 'high',
    reasons,
    notifyWho,
    suggestedActions,
    sourceLabel: '本地兜底',
    usesFallback: true,
    processSteps: buildProcessSteps(
      row,
      { abnormalType: row.type, metricValue: row.value, deviceId: row.deviceId, userId: row.userId },
      {},
      { riskLevel, immediateAction: riskLevel === 'high' },
      { notifyWho, suggestedActions, offlineCheck: riskLevel === 'high' },
      true,
      riskLevel,
      notifyWho,
      suggestedActions
    )
  }
}

const normalizeInsight = (payload: EventInsightPayload | Record<string, unknown> | unknown, row: ExceptionAlert): NormalizedInsight => {
  const source = (isRecord(payload) ? (isRecord(payload.data) ? payload.data : payload) : {}) as Record<string, unknown>
  const hasBackendPayload = Object.keys(source).length > 0
  const parsed = isRecord(source.parsedEvent) ? source.parsedEvent : {}
  const risk = isRecord(source.risk) ? source.risk : {}
  const advice = isRecord(source.advice) ? source.advice : {}
  const context = isRecord(source.context) ? source.context : {}
  const trace = isRecord(source.trace) ? source.trace : {}

  const riskLevel = normalizeRiskLevel(
    pickFromPaths(source, [
      ['riskLevel'],
      ['risk', 'level'],
      ['risk', 'riskLevel'],
      ['level']
    ])
  )

  const overview =
    pickFromPaths(source, [
      ['summary'],
      ['overview'],
      ['abnormalOverview'],
      ['title']
    ]) ||
    [
      row.nickName || `用户 ${row.userId || '-'}`,
      row.type ? `${row.type}异常` : '异常事件',
      row.value ? `指标 ${row.value}` : ''
    ]
      .filter(Boolean)
      .join('，')
      .concat('。')

  const reasons = uniqueTextList([
    ...getTextList(source.analysisReasons),
    ...getTextList(source.reasons),
    ...getTextList(source.possibleCauses),
    ...getTextList(source.possibleReasons),
    ...getTextList(risk.analysisReasons),
    ...getTextList(risk.possibleCauses),
    ...getTextList(risk.possibleReasons),
    ...getTextList(risk.reasonCodes),
    ...getTextList(risk.ruleHits).map((item) => `命中规则：${item}`),
    getText(parsed.abnormalType) ? `异常类型：${getText(parsed.abnormalType)}` : '',
    getText(parsed.metricName) ? `指标名称：${getText(parsed.metricName)}` : '',
    getText(parsed.metricValue) ? `指标值：${getText(parsed.metricValue)}` : '',
    getText(context.recentHealthTrend || context.recentTrend || context.healthTrend)
      ? `健康趋势：${getText(context.recentHealthTrend || context.recentTrend || context.healthTrend)}`
      : '',
    getTextList(context.chronicDiseases).length
      ? `基础疾病：${getTextList(context.chronicDiseases).join('、')}`
      : '',
    getText(context.deviceStatus) ? `设备状态：${getText(context.deviceStatus)}` : '',
    getText(context.deviceStatusReason) ? `状态说明：${getText(context.deviceStatusReason)}` : '',
    row.deviceId ? `关联设备：${row.deviceId}` : '',
    row.value ? `当前指标：${row.value}` : ''
  ])

  const notifyWho = uniqueTextList([
    ...getTextList(source.notifyWho),
    ...getTextList(advice.notifyWho)
  ])

  const suggestedActions = uniqueTextList([
    ...getTextList(source.suggestedActions),
    ...getTextList(source.actions),
    ...getTextList(advice.suggestedActions),
    ...getTextList(advice.actions)
  ])
  const generatedAt = getText(source.generatedAt)
  const traceSteps = buildProcessStepsFromTrace(trace)
  const usesFallback =
    Boolean(trace.fallbackUsed) ||
    !hasBackendPayload ||
    getText(parsed.source).includes('fallback') ||
    getText(context.dataConfidence).includes('fallback')

  const fallback = buildFallbackInsight(row)

  return {
    overview: overview || fallback.overview,
    riskLevel,
    riskLabel: insightRiskLabelMap[riskLevel],
    immediateAction:
      Boolean(source.immediateAction) ||
      Boolean(risk.immediateAction) ||
      Boolean(advice.immediateAction) ||
      fallback.immediateAction,
    reasons: reasons.length ? reasons : fallback.reasons,
    notifyWho: notifyWho.length ? notifyWho : fallback.notifyWho,
    suggestedActions: suggestedActions.length ? suggestedActions : fallback.suggestedActions,
    sourceLabel: hasBackendPayload ? getText(source.source, parsed.source) || (generatedAt ? `生成于 ${generatedAt}` : '后端研判') : fallback.sourceLabel,
    usesFallback,
    processSteps: traceSteps.length
      ? traceSteps
      : buildProcessSteps(
          row,
          parsed,
          context,
          risk,
          advice,
          usesFallback,
          riskLevel,
          notifyWho.length ? notifyWho : fallback.notifyWho,
          suggestedActions.length ? suggestedActions : fallback.suggestedActions
        )
  }
}

const getWorkflowState = (row: ExceptionAlert): WorkflowState => {
  if (String(row.state) === '1') return 'closed'
  const signature = Number(row.id || 0) % 3
  if (signature === 0) return 'new'
  if (signature === 1) return 'triage'
  return 'in_progress'
}
const selectedWorkflowState = computed(() => (!selectedEvent.value ? 'triage' : workflowDraft.value || getWorkflowState(selectedEvent.value)))
const workflowTagType = (state: WorkflowState) => (state === 'closed' ? 'success' : state === 'in_progress' ? 'warning' : state === 'new' ? 'danger' : 'info')
const workflowStepIndex = (state: WorkflowState) => (state === 'new' ? 0 : state === 'triage' ? 1 : state === 'in_progress' ? 2 : 3)
const riskTagType = (riskLevel: InsightRiskLevel) => {
  if (riskLevel === 'high') return 'danger'
  if (riskLevel === 'medium') return 'warning'
  if (riskLevel === 'low') return 'success'
  return 'info'
}
const processTagType = (status: AgentStepStatus) => {
  if (status === 'done') return 'success'
  if (status === 'partial') return 'warning'
  if (status === 'fallback') return 'danger'
  return 'info'
}
const processStatusLabel = (status: AgentStepStatus) => {
  if (status === 'done') return '已完成'
  if (status === 'partial') return '部分完成'
  if (status === 'fallback') return '兜底'
  return '待处理'
}

const syncSelectedEventDrafts = (row: ExceptionAlert | null) => {
  if (!row) {
    workflowDraft.value = 'triage'
    ownerDraft.value = '平台运营'
    slaDraft.value = '2h'
    return
  }
  workflowDraft.value = getWorkflowState(row)
  ownerDraft.value = String(row.state) === '1' ? '平台运营' : '值班护士 A'
  slaDraft.value = String(row.state) === '1' ? '24h' : '2h'
}

const refreshNotifications = async (row: ExceptionAlert | null = selectedEvent.value) => {
  try {
    notificationItems.value = await loadPlatformNotifications('event', {
      subjectName: row?.nickName,
      deviceName: row?.deviceId ? `设备 #${row.deviceId}` : undefined,
      eventType: row?.type
    })
  } catch {
    notificationItems.value = []
  }
}

const loadEventInsight = async (row: ExceptionAlert | null) => {
  const requestId = ++insightRequestSeq

  if (!row?.id) {
    eventInsight.value = null
    insightError.value = ''
    insightLoading.value = false
    return
  }

  insightLoading.value = true
  insightError.value = ''

  try {
    const response = await getEventInsight(row.id)
    if (requestId !== insightRequestSeq) return
    eventInsight.value = normalizeInsight(response, row)
  } catch (error) {
    if (requestId !== insightRequestSeq) return
    eventInsight.value = buildFallbackInsight(row)
    const rawMessage = error instanceof Error ? error.message : '研判结果暂不可用'
    insightError.value = /No static resource|404/i.test(rawMessage)
      ? 'lighthouse 暂未部署事件研判接口，当前显示本地兜底研判'
      : rawMessage
  } finally {
    if (requestId === insightRequestSeq) {
      insightLoading.value = false
    }
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = userIdFilter.value ? await listExceptionsByUserId(userIdFilter.value, query) : await listExceptions(query)
    list.value = (res.rows || []) as ExceptionAlert[]
    total.value = Number(res.total || 0)
    syncSelectedAfterFetch()
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.pageNum = 1
  query.pageSize = 10
  query.type = ''
  query.state = ''
  userIdFilter.value = undefined
  fetchList()
}
const handleRowSelect = (row: ExceptionAlert) => { selectedEvent.value = row }
const handleCurrentChange = (row?: ExceptionAlert) => { if (row) selectedEvent.value = row }
const openHandle = (row: ExceptionAlert) => {
  handleForm.id = row.id
  handleForm.state = row.state || '1'
  handleForm.updateContent = row.updateContent || ''
  handleDialogVisible.value = true
}
const submitHandle = async () => {
  if (!handleForm.id) return
  saving.value = true
  try {
    await updateException(handleForm)
    ElMessage.success('处置结果已更新')
    handleDialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}
const showDetail = async (id: number) => {
  const res = await getException(id)
  detailData.value = (res.data || {}) as Record<string, unknown>
  detailVisible.value = true
}
const jumpToSubject = async (row: ExceptionAlert) => {
  await dispatchPlatformAction(router, '查看对象', {
    entities: {
      subject: {
        kind: 'subject',
        name: row.nickName || `对象 #${row.userId || '-'}`,
        query: { keyword: String(row.userId || row.nickName || '') }
      }
    }
  })
}

const jumpToDevice = async (row: ExceptionAlert) => {
  await dispatchPlatformAction(router, '查看设备', {
    entities: {
      device: {
        kind: 'device',
        name: row.deviceId ? `设备 #${row.deviceId}` : '关联设备',
        query: { deviceId: String(row.deviceId || ''), userId: String(row.userId || '') }
      }
    }
  })
}

const handleSearchClick = async () => {
  await openPlatformSearch(router, 'event')
}

const handleNotificationItem = async (item: PlatformNotificationRecord) => {
  await openPlatformNotification(router, item)
}

const handleNotificationClick = async () => {
  await openAllPlatformNotifications(router, 'event')
}
const handleContextConfirm = () => ElMessage.success('上下文筛选已记录')
const handleContextReset = () => ElMessage.info('上下文筛选已重置')

watch(selectedEvent, (row) => {
  syncSelectedEventDrafts(row)
  void refreshNotifications(row)
  void loadEventInsight(row)
}, { immediate: true })
const { install: installRouteQuerySync, syncSelectedAfterFetch } = useRouteQueryListSync<ExceptionAlert>({
  route,
  list,
  selected: selectedEvent,
  applyQuery: applyRouteQuery,
  resolveMatchedItem: ({ list, fallbackSelected }) => {
    const matched = list.find(
      (item) =>
        (routeDeviceId.value && String(item.deviceId || '') === routeDeviceId.value) ||
        (userIdFilter.value && Number(item.userId || 0) === userIdFilter.value)
    )
    if (matched) return matched

    if (fallbackSelected) {
      return list.find((item) => item.id === fallbackSelected.id)
    }

    return null
  },
  fetchList
})

installRouteQuerySync()
</script>

<style scoped lang="scss">
.toolbar-stack { display: flex; flex-direction: column; gap: 14px; }
.toolbar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.section { padding: 16px; }
.header-actions { width: min(360px, 100%); }
.aside-stack { display: flex; flex-direction: column; gap: 14px; }
.aside-card { padding: 16px; border-radius: 18px; }
.insight-card { display: flex; flex-direction: column; gap: 14px; }
.detail-card, .detail-body, .workflow-block, .workflow-form { display: flex; flex-direction: column; gap: 16px; }
.detail-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
.aside-card-title, .block-title { font-size: 14px; font-weight: 700; color: var(--text-main); }
.detail-subtitle { margin: 6px 0 0; font-size: 12px; line-height: 1.5; color: var(--text-sub); }
.workflow-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.insight-overview {
  padding: 12px 14px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(63, 60, 187, 0.08), rgba(63, 60, 187, 0.02));
  border: 1px solid rgba(63, 60, 187, 0.12);
}
.insight-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-sub);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.insight-text {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-main);
}
.insight-section { display: flex; flex-direction: column; gap: 8px; }
.insight-row { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.insight-note { font-size: 12px; color: var(--text-sub); }
.process-list { display: flex; flex-direction: column; gap: 10px; }
.process-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid rgba(221, 227, 233, 0.92);
  background: rgba(255, 255, 255, 0.86);
}
.process-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: rgba(63, 60, 187, 0.1);
  color: var(--text-main);
  font-size: 12px;
  font-weight: 700;
}
.process-content { min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.process-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; }
.process-title { font-size: 13px; font-weight: 700; color: var(--text-main); }
.process-summary, .process-detail { margin: 0; font-size: 12px; line-height: 1.6; color: var(--text-sub); }
.process-summary { color: var(--text-main); }
.insight-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.insight-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(221, 227, 233, 0.92);
  background: rgba(255, 255, 255, 0.9);
  font-size: 12px;
  line-height: 1.4;
  color: var(--text-main);
}
.insight-chip-action {
  background: rgba(63, 60, 187, 0.06);
  border-color: rgba(63, 60, 187, 0.12);
}
.insight-footnote { margin: 0; font-size: 12px; color: var(--text-sub); }
.insight-loading { display: flex; flex-direction: column; gap: 10px; }
.loading-line {
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(221, 227, 233, 0.6), rgba(221, 227, 233, 0.94), rgba(221, 227, 233, 0.6));
  background-size: 200% 100%;
  animation: shimmer 1.2s ease infinite;
}
.loading-line-lg { width: 82%; height: 18px; }
.loading-line-short { width: 60%; }
.empty-detail { padding: 16px; border-radius: 16px; background: rgba(255,255,255,.56); border: 1px dashed rgba(221,227,233,.88); }
.empty-title { margin-bottom: 8px; font-size: 14px; font-weight: 700; color: var(--text-main); }
.pagination { margin-top: 14px; display: flex; justify-content: flex-end; }
.json-block { margin: 0; max-height: 60vh; overflow: auto; padding: 16px; border-radius: 16px; background: #f8fafc; }

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}
</style>
