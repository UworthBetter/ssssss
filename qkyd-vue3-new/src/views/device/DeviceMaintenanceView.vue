<template>
  <PlatformPageShellV2
    title="设备运维"
    subtitle="基于真实设备扩展数据识别低电量、离线和告警设备，形成当日巡检清单。"
    eyebrow="DEVICE MAINTENANCE"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <el-input v-model="keyword" placeholder="搜索设备名 / 对象" prefix-icon="Search" clearable style="width: 240px" />
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 160px">
          <el-option label="需巡检" value="inspect" />
          <el-option label="低电量" value="battery" />
          <el-option label="离线" value="offline" />
          <el-option label="有告警" value="alarm" />
        </el-select>
        <el-button type="primary" @click="fetchData">刷新清单</el-button>
      </div>
    </template>

    <div class="summary-grid">
      <div v-for="card in summaryCards" :key="card.label" class="summary-card">
        <span class="sc-value" :style="{ color: card.color }">{{ card.value }}</span>
        <span class="sc-label">{{ card.label }}</span>
      </div>
    </div>

    <div class="panel-card">
      <div class="panel-header">
        <span class="panel-title">今日运维清单</span>
        <span class="panel-sub">{{ filteredRows.length }} 台设备</span>
      </div>
      <el-table v-loading="loading" :data="filteredRows" style="width: 100%">
        <el-table-column prop="deviceName" label="设备" min-width="160" />
        <el-table-column prop="subjectName" label="绑定对象" min-width="120" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="row.tagType" size="small" effect="light">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="batteryText" label="电量" min-width="90" />
        <el-table-column prop="lastCommunicationText" label="最近通信" min-width="120" />
        <el-table-column prop="issue" label="问题概述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="action" label="建议动作" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="openDevice(row.deviceId)">查看设备</el-button>
            <el-button size="small" text @click="openSubject(row.userId)" :disabled="!row.userId">查看对象</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { PlatformPageShellV2 } from '@/components/platform'
import { listDeviceExtensions, listDevices, listSubjects, type DeviceInfo, type DeviceInfoExtend, type HealthSubject } from '@/api/health'

interface MaintenanceRow {
  deviceId?: number
  userId?: number
  deviceName: string
  subjectName: string
  statusLabel: string
  tagType: 'success' | 'warning' | 'info' | 'danger'
  batteryText: string
  lastCommunicationText: string
  issue: string
  action: string
  flags: string[]
}

const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const rows = ref<MaintenanceRow[]>([])

const parseNumber = (value: unknown, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const parseTimestamp = (value?: string) => {
  if (!value) return 0
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

const formatRelativeTime = (value?: string) => {
  const timestamp = parseTimestamp(value)
  if (!timestamp) return '暂无'
  const diffMinutes = Math.max(0, Math.round((Date.now() - timestamp) / 60000))
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`
  const diffHours = Math.round(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours} 小时前`
  return `${Math.round(diffHours / 24)} 天前`
}

const buildRow = (device: DeviceInfo, extension?: DeviceInfoExtend, subject?: HealthSubject): MaintenanceRow => {
  const battery = parseNumber(extension?.batteryLevel, 0)
  const lastCommunication = parseTimestamp(extension?.lastCommunicationTime)
  const offline = !lastCommunication || Date.now() - lastCommunication > 60 * 60 * 1000
  const lowBattery = battery > 0 && battery <= 20
  const alarm = Boolean(String(extension?.alarmContent || '').trim())
  const flags = [
    offline ? 'offline' : '',
    lowBattery ? 'battery' : '',
    alarm ? 'alarm' : '',
    offline || lowBattery || alarm ? 'inspect' : ''
  ].filter(Boolean)

  const statusLabel = offline ? '离线' : alarm ? '告警' : lowBattery ? '低电量' : '正常'
  const tagType = offline ? 'info' : alarm ? 'danger' : lowBattery ? 'warning' : 'success'
  const issue = alarm
    ? String(extension?.alarmContent)
    : offline
      ? '设备最近 1 小时未通信'
      : lowBattery
        ? `设备剩余电量 ${battery}%`
        : '设备状态正常'
  const action = alarm
    ? '优先核查告警原因并联系对象'
    : offline
      ? '检查网络、佩戴状态和设备在线情况'
      : lowBattery
        ? '安排充电或更换电池'
        : '保持常规巡检'

  return {
    deviceId: device.id,
    userId: device.userId,
    deviceName: device.name || `设备 #${device.id || '-'}`,
    subjectName: subject?.nickName || subject?.subjectName || '未绑定对象',
    statusLabel,
    tagType,
    batteryText: battery ? `${battery}%` : '--',
    lastCommunicationText: formatRelativeTime(extension?.lastCommunicationTime),
    issue,
    action,
    flags
  }
}

const summaryCards = computed(() => [
  { label: '设备总数', value: rows.value.length, color: '#3b82f6' },
  { label: '需巡检', value: rows.value.filter((item) => item.flags.includes('inspect')).length, color: '#f59e0b' },
  { label: '低电量', value: rows.value.filter((item) => item.flags.includes('battery')).length, color: '#ef4444' },
  { label: '离线', value: rows.value.filter((item) => item.flags.includes('offline')).length, color: '#94a3b8' }
])

const filteredRows = computed(() => {
  const text = keyword.value.trim()
  return rows.value.filter((item) => {
    if (statusFilter.value && !item.flags.includes(statusFilter.value)) return false
    if (!text) return true
    return [item.deviceName, item.subjectName].some((field) => field.includes(text))
  })
})

const fetchData = async () => {
  loading.value = true
  try {
    const [deviceRes, extRes, subjectRes] = await Promise.all([
      listDevices({ pageNum: 1, pageSize: 200 }),
      listDeviceExtensions({ pageNum: 1, pageSize: 200 }),
      listSubjects({ pageNum: 1, pageSize: 200 })
    ])

    const devices = (deviceRes.rows || []) as DeviceInfo[]
    const extensions = (extRes.rows || []) as DeviceInfoExtend[]
    const subjects = (subjectRes.rows || []) as HealthSubject[]

    const extMap = new Map<number, DeviceInfoExtend>()
    extensions.forEach((item) => {
      if (item.deviceId != null) extMap.set(Number(item.deviceId), item)
    })

    const subjectMap = new Map<number, HealthSubject>()
    subjects.forEach((item) => {
      const id = Number(item.subjectId || 0)
      if (id) subjectMap.set(id, item)
    })

    rows.value = devices.map((device) => buildRow(device, extMap.get(Number(device.id || 0)), subjectMap.get(Number(device.userId || 0))))
  } finally {
    loading.value = false
  }
}

const openDevice = (deviceId?: number) => {
  if (!deviceId) return
  router.push({ path: '/device', query: { deviceId: String(deviceId) } })
}

const openSubject = (userId?: number) => {
  if (!userId) return
  router.push({ path: '/subject', query: { userId: String(userId) } })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.toolbar-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.summary-card { background: #fff; border-radius: 14px; padding: 18px 20px; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 8px rgba(0,0,0,0.03); display: flex; flex-direction: column; gap: 6px; }
.sc-value { font-size: 30px; font-weight: 800; line-height: 1; }
.sc-label { font-size: 13px; color: #64748b; }
.panel-card { background: #fff; border-radius: 16px; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 12px rgba(0,0,0,0.04); overflow: hidden; }
.panel-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid #f1f5f9; }
.panel-title { font-size: 14px; font-weight: 700; color: #0f172a; }
.panel-sub { font-size: 12px; color: #94a3b8; }
@media (max-width: 1024px) { .summary-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
