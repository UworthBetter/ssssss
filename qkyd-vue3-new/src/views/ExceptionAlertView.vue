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
import { getException, listExceptions, listExceptionsByUserId, updateException, type ExceptionAlert } from '@/api/health'
import { useRouteQueryListSync } from '@/composables/useRouteQueryListSync'

type WorkflowState = 'new' | 'triage' | 'in_progress' | 'closed'

const workflowLabelMap: Record<WorkflowState, string> = { new: '新告警', triage: '待研判', in_progress: '处理中', closed: '已关闭' }
const workflowOptions = [
  { label: '新告警', value: 'new' },
  { label: '待研判', value: 'triage' },
  { label: '处理中', value: 'in_progress' },
  { label: '已关闭', value: 'closed' }
] as const
const ownerOptions = ['值班护士 A', '值班护士 B', '平台运营', '医生工作台']

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

const refreshNotifications = async () => {
  notificationItems.value = await loadPlatformNotifications('event', {
    subjectName: selectedEvent.value?.nickName,
    deviceName: selectedEvent.value?.deviceId ? `设备 #${selectedEvent.value.deviceId}` : undefined,
    eventType: selectedEvent.value?.type
  })
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

watch(selectedEvent, (row) => { syncSelectedEventDrafts(row); refreshNotifications() }, { immediate: true })
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
.detail-card, .detail-body, .workflow-block, .workflow-form { display: flex; flex-direction: column; gap: 16px; }
.detail-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
.aside-card-title, .block-title { font-size: 14px; font-weight: 700; color: var(--text-main); }
.detail-subtitle { margin: 6px 0 0; font-size: 12px; line-height: 1.5; color: var(--text-sub); }
.workflow-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.empty-detail { padding: 16px; border-radius: 16px; background: rgba(255,255,255,.56); border: 1px dashed rgba(221,227,233,.88); }
.empty-title { margin-bottom: 8px; font-size: 14px; font-weight: 700; color: var(--text-main); }
.pagination { margin-top: 14px; display: flex; justify-content: flex-end; }
.json-block { margin: 0; max-height: 60vh; overflow: auto; padding: 16px; border-radius: 16px; background: #f8fafc; }
</style>
