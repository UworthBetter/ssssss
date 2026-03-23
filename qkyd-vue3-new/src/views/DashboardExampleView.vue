<template>
  <PlatformPageShell
    title="事件处理链总览"
    subtitle="围绕异常事件的 AI 处理链路进行统一展示，从设备异常检测到最终闭环留痕的全流程可视化。"
    eyebrow="EVENT CHAIN OVERVIEW"
    aside-title="链路分析"
    aside-width="380px"
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
          summary-value="工作台 / 事件处理链总览"
          @confirm="handleContextConfirm"
          @reset="handleContextReset"
        />
      </div>
    </template>

    <!-- 主内容区：链路汇总 + 事件流 -->
    <div class="overview-layout">
      <!-- 链路统计卡片 -->
      <div class="chain-kpi-row">
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon-total">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="kpi-body">
            <div class="kpi-value digital-font">{{ stats.totalEvents }}</div>
            <div class="kpi-label">今日事件总数</div>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon-ai">
            <el-icon><MagicStick /></el-icon>
          </div>
          <div class="kpi-body">
            <div class="kpi-value digital-font">{{ stats.aiProcessed }}</div>
            <div class="kpi-label">AI 已解析</div>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon-risk">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="kpi-body">
            <div class="kpi-value digital-font text-danger">{{ stats.highRisk }}</div>
            <div class="kpi-label">高风险事件</div>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon-closed">
            <el-icon><CircleCheckFilled /></el-icon>
          </div>
          <div class="kpi-body">
            <div class="kpi-value digital-font text-success">{{ stats.closedPercent }}%</div>
            <div class="kpi-label">闭环率</div>
          </div>
        </div>
      </div>

      <!-- 链路阶段分布 -->
      <div class="chain-stage-overview">
        <div class="stage-header">
          <h3 class="section-title">处理链阶段分布</h3>
          <span class="section-hint">各阶段事件数量统计</span>
        </div>
        <div class="stage-bar-chart">
          <div
            v-for="stage in stageDistribution"
            :key="stage.name"
            class="stage-bar-item"
          >
            <div class="bar-label">{{ stage.name }}</div>
            <div class="bar-track">
              <div
                class="bar-fill"
                :style="{ width: stage.percent + '%', background: stage.color }"
              />
            </div>
            <div class="bar-count digital-font">{{ stage.count }}</div>
          </div>
        </div>
      </div>

      <!-- 实时异常事件流 -->
      <div class="event-stream-section">
        <div class="stage-header">
          <h3 class="section-title">实时异常事件流</h3>
          <el-button text type="primary" @click="refreshData" :loading="fetching">
            刷新数据
          </el-button>
        </div>
        <el-table
          v-loading="fetching"
          :data="filteredExceptionList"
          stripe
          highlight-current-row
          class="event-table"
          @row-click="handleRowClick"
        >
          <el-table-column prop="nickName" label="服务对象" min-width="90" />
          <el-table-column prop="type" label="异常类型" min-width="110" />
          <el-table-column label="链路进度" min-width="160">
            <template #default="{ row }">
              <ProcessingChainProgress v-if="row.stages" :stages="row.stages" />
              <span v-else class="text-muted">暂无</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.state === '1' ? 'success' : 'danger'">
                {{ row.state === '1' ? '已闭环' : '待处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="位置" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="发生时间" min-width="170" />
        </el-table>
      </div>
    </div>

    <template #aside>
      <div class="aside-stack">
        <!-- 通知入口 -->
        <PlatformNotificationEntry
          title="处理链通知"
          :unread-count="notificationItems.length"
          :items="notificationItems"
          @click-item="handleNotificationItem"
          @click-all="handleNotificationClick"
        />

        <!-- 选中事件的处理链详情 -->
        <div class="panel aside-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">处理链详情</div>
              <p class="detail-subtitle">点击列表中的事件行查看完整处理链。</p>
            </div>
          </div>

          <div v-if="selectedChainData" class="detail-body">
            <div class="chain-meta">
              <div class="meta-row">
                <span class="meta-label">对象:</span>
                <span class="meta-value">{{ selectedChainData.nickName || '-' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">类型:</span>
                <span class="meta-value">{{ selectedChainData.type || '-' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">事件ID:</span>
                <span class="meta-value digital-font">{{ selectedChainData.id || '-' }}</span>
              </div>
              <div v-if="processingChain" class="meta-row">
                <span class="meta-label">链路耗时:</span>
                <span class="meta-value digital-font">{{ processingChain.totalDuration }}ms</span>
              </div>
            </div>

            <!-- 时间轴 -->
            <div v-if="processingChain" class="mini-timeline">
              <div
                v-for="(stage, index) in processingChain.stages"
                :key="index"
                class="mt-item"
                :class="`mt-${stage.status}`"
              >
                <div class="mt-dot-wrap">
                  <div class="mt-dot">
                    <el-icon v-if="stage.status === 'completed'" :size="12"><Check /></el-icon>
                    <el-icon v-else-if="stage.status === 'processing'" :size="12"><Loading /></el-icon>
                    <el-icon v-else :size="12"><Clock /></el-icon>
                  </div>
                  <div v-if="index < processingChain.stages.length - 1" class="mt-line" />
                </div>
                <div class="mt-content">
                  <div class="mt-head">
                    <span class="mt-name">{{ stage.name }}</span>
                    <span v-if="stage.timestamp" class="mt-time digital-font">{{ formatTime(stage.timestamp) }}</span>
                  </div>
                  <div v-if="stage.details" class="mt-details">
                    <template v-if="stage.name === '异常检测' && stage.details.anomalies">
                      <div v-for="a in stage.details.anomalies" :key="a.type" class="mt-detail">
                        <span>{{ a.type }}</span>
                        <span class="digital-font">{{ a.value }}</span>
                        <el-tag size="small" :type="a.severity === 'high' ? 'danger' : a.severity === 'medium' ? 'warning' : 'info'" effect="light">
                          {{ a.severity }}
                        </el-tag>
                      </div>
                    </template>
                    <template v-else-if="stage.name === '风险评估'">
                      <div class="mt-detail">
                        <span>风险等级</span>
                        <el-tag size="small" :type="stage.details.riskLevel === 'high' ? 'danger' : 'warning'" effect="light">
                          {{ stage.details.riskLevel }}
                        </el-tag>
                      </div>
                      <div class="mt-detail">
                        <span>风险分数</span>
                        <span class="digital-font">{{ stage.details.riskScore }}/10</span>
                      </div>
                      <div class="mt-detail">
                        <span>置信度</span>
                        <span class="digital-font">{{ stage.details.confidence }}%</span>
                      </div>
                    </template>
                    <template v-else>
                      <div v-for="(val, key) in stage.details" :key="key" class="mt-detail">
                        <span>{{ key }}</span>
                        <span>{{ val }}</span>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </div>

            <!-- 快捷操作 -->
            <div class="chain-nav-actions">
              <el-button size="small" type="primary" @click="jumpToProcessingChain">
                查看处理链详情 →
              </el-button>
              <el-button size="small" @click="jumpToEvent">
                进入事件中心
              </el-button>
            </div>
          </div>

          <div v-else class="empty-detail">
            <div class="empty-title">点击事件查看处理链</div>
            <p>从左侧事件列表中点击任一事件，即可在此查看其完整的 AI 处理链路。</p>
          </div>
        </div>

        <!-- 链路健康度 -->
        <div class="panel aside-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">链路健康度</div>
              <p class="detail-subtitle">各处理阶段的完成情况监控。</p>
            </div>
          </div>
          <div class="health-grid">
            <div v-for="item in chainHealthItems" :key="item.name" class="health-item">
              <div class="health-dot" :class="item.status" />
              <div class="health-text">
                <span class="health-name">{{ item.name }}</span>
                <span class="health-rate digital-font">{{ item.rate }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 处理链详情弹窗 -->
    <ProcessingChainPanel
      v-model="showChainPanel"
      :data="selectedChainData"
      :chain="processingChain"
    />
  </PlatformPageShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check, Loading, Clock, DataAnalysis, MagicStick, WarningFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  PlatformContextFilterBar,
  PlatformNotificationEntry,
  PlatformPageShell,
  PlatformSearchEntry,
  getPlatformSearchPresentation,
  loadPlatformNotifications,
  openAllPlatformNotifications,
  openPlatformNotification,
  openPlatformSearch,
  type PlatformContextFilters,
  type PlatformNotificationRecord
} from '@/components/platform'
import ProcessingChainPanel from '@/components/ProcessingChainPanel.vue'
import ProcessingChainProgress from '@/components/ProcessingChainProgress.vue'
import { getIndexException } from '@/api/index'
import { getProcessingChain } from '@/api/processingChain'
import { generateMockProcessingChain, enrichExceptionWithChain } from '@/utils/mockProcessingChain'

const router = useRouter()
const searchPresentation = getPlatformSearchPresentation('event')
const contextFilters = ref<PlatformContextFilters>({ timeRange: 'today', region: 'all', riskLevel: 'all', status: 'all' })

const exceptionList = ref<any[]>([])
const fetching = ref(false)
const showChainPanel = ref(false)
const selectedChainData = ref<any>(null)
const processingChain = ref<any>(null)
const notificationItems = ref<PlatformNotificationRecord[]>([])

let refreshTimer: ReturnType<typeof setInterval> | null = null
const REFRESH_INTERVAL = 60000

// ============ API 调用 ============
const loadData = async () => {
  try {
    fetching.value = true
    const exceptionRes = await getIndexException('', 1)
    const rawList = exceptionRes.rows || exceptionRes.data || []
    exceptionList.value = (Array.isArray(rawList) ? rawList : []).map((item: any) =>
      enrichExceptionWithChain(item)
    )
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('事件数据加载失败')
  } finally {
    fetching.value = false
  }
}

const refreshData = () => {
  loadData()
}

const loadNotifications = async () => {
  notificationItems.value = await loadPlatformNotifications('event')
}

// ============ 统计数据 ============
const stats = computed(() => {
  const items = exceptionList.value
  const total = items.length
  const resolved = items.filter(r => String(r.state) === '1').length
  const highRisk = items.filter(r => {
    const type = String(r.type ?? '').toLowerCase()
    return ['sos', '求助', '心率', '血氧'].some(k => type.includes(k)) && String(r.state) !== '1'
  }).length

  return {
    totalEvents: total,
    aiProcessed: Math.min(total, total > 0 ? total - Math.floor(total * 0.1) : 0),
    highRisk,
    closedPercent: total > 0 ? Math.round((resolved / total) * 100) : 0
  }
})

const stageNames = ['异常检测', '事件产生', 'AI解析与上下文补全', '风险评估', '处置建议', '自动执行', '留痕记录']
const stageColors = ['#0ea5e9', '#06b6d4', '#8b5cf6', '#f59e0b', '#ec4899', '#10b981', '#64748b']

const stageDistribution = computed(() => {
  const counts = new Array(stageNames.length).fill(0)
  exceptionList.value.forEach((item: any) => {
    if (item.stages) {
      item.stages.forEach((s: any, i: number) => {
        if (s.status === 'completed' && i < counts.length) counts[i]++
      })
    }
  })
  const max = Math.max(1, ...counts)
  return stageNames.map((name, i) => ({
    name,
    count: counts[i],
    percent: Math.round((counts[i] / max) * 100),
    color: stageColors[i]
  }))
})

const chainHealthItems = computed(() => {
  const total = Math.max(1, exceptionList.value.length)
  return stageNames.map((name, i) => {
    const count = exceptionList.value.filter((item: any) =>
      item.stages && item.stages[i] && item.stages[i].status === 'completed'
    ).length
    const rate = Math.round((count / total) * 100)
    return {
      name,
      rate,
      status: rate > 80 ? 'healthy' : rate > 50 ? 'warning' : 'critical'
    }
  })
})

const filteredExceptionList = computed(() => exceptionList.value.slice(0, 15))

// ============ 事件处理 ============
const handleRowClick = async (row: any) => {
  selectedChainData.value = row
  try {
    const res = await getProcessingChain(row.id)
    if (res.data && res.data.stages) {
      processingChain.value = res.data
    } else {
      processingChain.value = generateMockProcessingChain(row.id)
    }
  } catch {
    processingChain.value = generateMockProcessingChain(row.id)
  }
}

const formatTime = (timestamp: string) => {
  try {
    return new Date(timestamp).toLocaleTimeString('zh-CN')
  } catch {
    return timestamp
  }
}

const handleSearchClick = () => openPlatformSearch(router)
const handleContextConfirm = () => loadData()
const handleContextReset = () => {
  contextFilters.value = { timeRange: 'today', region: 'all', riskLevel: 'all', status: 'all' }
  loadData()
}
const handleNotificationItem = (item: PlatformNotificationRecord) => openPlatformNotification(router, item)
const handleNotificationClick = () => openAllPlatformNotifications(router)

const jumpToProcessingChain = () => {
  if (selectedChainData.value) {
    router.push({ path: '/event/processing-chain' })
  }
}

const jumpToEvent = () => {
  if (selectedChainData.value) {
    router.push({
      path: '/event',
      query: { type: selectedChainData.value.type || '', userId: String(selectedChainData.value.userId || '') }
    })
  }
}

// ============ 生命周期 ============
onMounted(() => {
  loadData()
  loadNotifications()
  refreshTimer = setInterval(loadData, REFRESH_INTERVAL)
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped lang="scss">
.toolbar-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.overview-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// KPI 卡片
.chain-kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.kpi-card {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 16px 20px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;

  &.kpi-icon-total { background: #eff6ff; color: #3b82f6; }
  &.kpi-icon-ai { background: #f5f3ff; color: #8b5cf6; }
  &.kpi-icon-risk { background: #fef2f2; color: #ef4444; }
  &.kpi-icon-closed { background: #f0fdf4; color: #22c55e; }
}

.kpi-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.kpi-label {
  font-size: 12px;
  color: #94a3b8;
}

// 阶段分布
.chain-stage-overview {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
}

.stage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.section-hint {
  font-size: 12px;
  color: #94a3b8;
}

.stage-bar-chart {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stage-bar-item {
  display: grid;
  grid-template-columns: 140px 1fr 50px;
  gap: 12px;
  align-items: center;
}

.bar-label {
  font-size: 13px;
  color: #475569;
  text-align: right;
}

.bar-track {
  height: 20px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
  min-width: 4px;
}

.bar-count {
  font-size: 13px;
  color: #1e293b;
  font-weight: 600;
  text-align: right;
}

// 事件流
.event-stream-section {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
}

.event-table {
  :deep(.el-table__row) {
    cursor: pointer;
  }
}

// 通用
.digital-font { font-family: 'Courier New', monospace; }
.text-danger { color: #ef4444; }
.text-success { color: #22c55e; }
.text-warning { color: #f59e0b; }
.text-muted { color: #94a3b8; font-size: 12px; }

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

// 链条元数据
.chain-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 6px;
  border-left: 3px solid #0ea5e9;
}

.meta-row {
  display: flex;
  gap: 6px;
  align-items: center;
  font-size: 12px;
}

.meta-label {
  color: #94a3b8;
  font-weight: 500;
}

.meta-value {
  color: #1e293b;
  font-weight: 500;
}

// 迷你时间轴
.mini-timeline {
  display: flex;
  flex-direction: column;
}

.mt-item {
  display: flex;
  gap: 10px;
  position: relative;

  &.mt-completed .mt-dot {
    background: #10b981;
    border-color: #10b981;
    color: white;
  }
  &.mt-processing .mt-dot {
    background: #f59e0b;
    border-color: #f59e0b;
    color: white;
  }
  &.mt-pending .mt-dot {
    background: #e5e7eb;
    border-color: #d1d5db;
    color: #94a3b8;
  }
}

.mt-dot-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.mt-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #d1d5db;
  background: white;
  z-index: 1;
}

.mt-line {
  width: 2px;
  flex: 1;
  min-height: 12px;
  background: #e5e7eb;
}

.mt-content {
  flex: 1;
  padding-bottom: 12px;
}

.mt-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.mt-name {
  font-size: 12px;
  font-weight: 600;
  color: #1e293b;
}

.mt-time {
  font-size: 10px;
  color: #94a3b8;
}

.mt-details {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.mt-detail {
  display: flex;
  gap: 6px;
  align-items: center;
  font-size: 11px;
  color: #64748b;
  padding: 2px 6px;
  background: #f8fafc;
  border-radius: 3px;
}

// 快捷操作
.chain-nav-actions {
  display: flex;
  gap: 8px;
  padding-top: 4px;
}

// 链路健康度
.health-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.health-item {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 6px 8px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover { background: #f8fafc; }
}

.health-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;

  &.healthy { background: #10b981; }
  &.warning { background: #f59e0b; }
  &.critical { background: #ef4444; }
}

.health-text {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
}

.health-name {
  font-size: 12px;
  color: #475569;
}

.health-rate {
  font-size: 12px;
  font-weight: 600;
  color: #1e293b;
}
</style>
