<template>
  <PlatformPageShellV2
    title="设备状态监测"
    subtitle="直接读取后端设备与扩展信息，实时展示连接状态、电量、最近上传与告警情况。"
    eyebrow="DEVICE MONITOR"
  >
    <div class="status-grid">
      <div v-for="card in statusCards" :key="card.label" class="stat-card" :class="card.cls">
        <div class="stat-icon" :style="{ background: card.bg }">
          <el-icon :size="22"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ card.value }}</span>
          <span class="stat-label">{{ card.label }}</span>
        </div>
        <div class="stat-indicator" :class="card.cls"></div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
          <span class="tab-count">{{ tab.count }}</span>
        </button>
      </div>
      <div class="filter-right">
        <el-input
          v-model="searchText"
          placeholder="搜索设备名 / IMEI / 绑定对象"
          prefix-icon="Search"
          clearable
          style="width: 260px"
        />
        <el-button :icon="Refresh" circle @click="fetchData" :loading="loading" title="刷新状态" />
      </div>
    </div>

    <div v-loading="loading" class="device-grid">
      <div v-for="device in filteredDevices" :key="device.id" class="device-card" :class="device.status">
        <div class="dc-header">
          <div class="dc-status-dot" :class="device.status"></div>
          <span class="dc-name">{{ device.name }}</span>
          <el-tag :type="statusTagType(device.status)" size="small" effect="light">{{ statusLabel[device.status] }}</el-tag>
        </div>
        <div class="dc-imei">IMEI: {{ device.imei || '-' }}</div>
        <div class="dc-metrics">
          <div class="dcm-item">
            <span class="dcm-label">电量</span>
            <el-progress
              :percentage="device.battery"
              :stroke-width="6"
              :color="batteryColor(device.battery)"
              :show-text="false"
              class="dcm-progress"
            />
            <span class="dcm-val">{{ device.battery }}%</span>
          </div>
          <div class="dcm-item">
            <span class="dcm-label">信号</span>
            <div class="signal-bars">
              <div v-for="n in 4" :key="n" class="signal-bar" :class="{ active: device.signal >= n }"></div>
            </div>
            <span class="dcm-val">{{ signalLabel(device.signal) }}</span>
          </div>
        </div>
        <div class="device-vitals">
          <span>心率 {{ device.heartRateText }}</span>
          <span>血氧 {{ device.spo2Text }}</span>
          <span>步数 {{ device.stepText }}</span>
        </div>
        <div v-if="device.alertContent" class="device-alert">{{ device.alertContent }}</div>
        <div class="dc-footer">
          <span class="dc-bound">{{ device.boundTo || '未绑定对象' }}</span>
          <span class="dc-upload">{{ device.lastUpload }}</span>
        </div>
        <div class="dc-actions">
          <el-button size="small" plain @click="openDevice(device.id)">查看详情</el-button>
          <el-button size="small" plain @click="openSubject(device.userId)" :disabled="!device.userId">查看对象</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && !filteredDevices.length" description="暂无设备数据" />
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'
import { listDeviceExtensions, listDevices, listSubjects, type DeviceInfo, type DeviceInfoExtend, type HealthSubject } from '@/api/health'

type DeviceStatus = 'online' | 'offline' | 'warning'

interface DeviceCard {
  id?: number
  userId?: number
  name: string
  imei: string
  status: DeviceStatus
  battery: number
  signal: number
  boundTo: string
  lastUpload: string
  heartRateText: string
  spo2Text: string
  stepText: string
  alertContent: string
}

const router = useRouter()
const activeTab = ref<'all' | DeviceStatus>('all')
const searchText = ref('')
const loading = ref(false)
const deviceCards = ref<DeviceCard[]>([])

const statusLabel: Record<DeviceStatus, string> = { online: '在线', offline: '离线', warning: '告警' }
const statusTagType = (status: DeviceStatus) => ({ online: 'success', offline: 'info', warning: 'warning' }[status] as 'success' | 'info' | 'warning')
const batteryColor = (pct: number) => (pct > 60 ? '#10b981' : pct > 30 ? '#f59e0b' : '#ef4444')
const signalLabel = (level: number) => ['无', '弱', '中', '良', '强'][level] || '未知'

const parseNumber = (value: unknown, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const parseTime = (value?: string) => {
  if (!value) return 0
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

const formatRelativeTime = (value?: string) => {
  const timestamp = parseTime(value)
  if (!timestamp) return '暂无上报'

  const diffMinutes = Math.max(0, Math.round((Date.now() - timestamp) / 60000))
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`
  const diffHours = Math.round(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours} 小时前`
  const diffDays = Math.round(diffHours / 24)
  return `${diffDays} 天前`
}

const getSignalLevel = (lastCommunicationTime?: string) => {
  const timestamp = parseTime(lastCommunicationTime)
  if (!timestamp) return 0

  const diffMinutes = Math.max(0, (Date.now() - timestamp) / 60000)
  if (diffMinutes <= 5) return 4
  if (diffMinutes <= 15) return 3
  if (diffMinutes <= 30) return 2
  if (diffMinutes <= 60) return 1
  return 0
}

const getDeviceStatus = (ext?: DeviceInfoExtend): DeviceStatus => {
  const lastCommunicationTime = parseTime(ext?.lastCommunicationTime)
  const battery = parseNumber(ext?.batteryLevel, 0)
  const hasAlarm = Boolean(String(ext?.alarmContent || '').trim())

  if (!lastCommunicationTime || Date.now() - lastCommunicationTime > 60 * 60 * 1000) {
    return 'offline'
  }
  if (hasAlarm || battery <= 20) {
    return 'warning'
  }
  return 'online'
}

const tabs = computed(() => {
  const total = deviceCards.value.length
  const online = deviceCards.value.filter((item) => item.status === 'online').length
  const offline = deviceCards.value.filter((item) => item.status === 'offline').length
  const warning = deviceCards.value.filter((item) => item.status === 'warning').length

  return [
    { label: '全部', value: 'all' as const, count: total },
    { label: '在线', value: 'online' as const, count: online },
    { label: '离线', value: 'offline' as const, count: offline },
    { label: '告警', value: 'warning' as const, count: warning }
  ]
})

const statusCards = computed(() => [
  { label: '全部设备', value: deviceCards.value.length, cls: 'total', icon: 'Monitor', bg: 'linear-gradient(135deg,#eff6ff,#dbeafe)' },
  { label: '在线', value: deviceCards.value.filter((item) => item.status === 'online').length, cls: 'online', icon: 'CircleCheck', bg: 'linear-gradient(135deg,#f0fdf4,#dcfce7)' },
  { label: '离线', value: deviceCards.value.filter((item) => item.status === 'offline').length, cls: 'offline', icon: 'CircleClose', bg: 'linear-gradient(135deg,#f8fafc,#e2e8f0)' },
  { label: '告警', value: deviceCards.value.filter((item) => item.status === 'warning').length, cls: 'warning', icon: 'Warning', bg: 'linear-gradient(135deg,#fffbeb,#fef3c7)' }
])

const filteredDevices = computed(() => {
  const keyword = searchText.value.trim()
  return deviceCards.value.filter((item) => {
    if (activeTab.value !== 'all' && item.status !== activeTab.value) {
      return false
    }
    if (!keyword) {
      return true
    }
    return [item.name, item.imei, item.boundTo].some((field) => field.includes(keyword))
  })
})

const buildDeviceCard = (
  device: DeviceInfo,
  extMap: Map<number, DeviceInfoExtend>,
  subjectMap: Map<number, HealthSubject>
): DeviceCard => {
  const ext = extMap.get(Number(device.id || 0))
  const subject = subjectMap.get(Number(device.userId || 0))
  const status = getDeviceStatus(ext)
  const battery = Math.min(100, Math.max(0, parseNumber(ext?.batteryLevel, status === 'offline' ? 0 : 60)))

  return {
    id: device.id,
    userId: device.userId,
    name: device.name || `设备 #${device.id || '-'}`,
    imei: device.imei || '-',
    status,
    battery,
    signal: getSignalLevel(ext?.lastCommunicationTime),
    boundTo: subject?.nickName || subject?.subjectName || ext?.nickName || '',
    lastUpload: formatRelativeTime(ext?.lastCommunicationTime),
    heartRateText: ext?.heartRate ? `${ext.heartRate} bpm` : '--',
    spo2Text: ext?.spo2 ? `${ext.spo2}%` : '--',
    stepText: ext?.step ? `${ext.step}` : '--',
    alertContent: String(ext?.alarmContent || '').trim()
  }
}

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
      if (item.deviceId != null) {
        extMap.set(Number(item.deviceId), item)
      }
    })

    const subjectMap = new Map<number, HealthSubject>()
    subjects.forEach((item) => {
      const id = Number(item.subjectId || 0)
      if (id) {
        subjectMap.set(id, item)
      }
    })

    deviceCards.value = devices.map((device) => buildDeviceCard(device, extMap, subjectMap))
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
.status-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid rgba(226, 232, 240, 0.7);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  position: relative;
  overflow: hidden;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  flex-shrink: 0;
}

.stat-info { flex: 1; }
.stat-num { font-size: 30px; font-weight: 800; color: #0f172a; display: block; line-height: 1.1; }
.stat-label { font-size: 13px; color: #64748b; }

.stat-indicator {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.stat-indicator.online { background: #10b981; }
.stat-indicator.offline { background: #94a3b8; }
.stat-indicator.warning { background: #f59e0b; }
.stat-indicator.total { background: #3b82f6; }

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-tabs { display: flex; gap: 4px; flex-wrap: wrap; }

.filter-tab {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.filter-tab:hover { background: #f8fafc; border-color: #cbd5e1; }
.filter-tab.active { background: #eff6ff; border-color: #93c5fd; color: #2563eb; font-weight: 700; }
.tab-count { background: #f1f5f9; padding: 0 6px; border-radius: 10px; font-size: 11px; }
.filter-right { display: flex; align-items: center; gap: 10px; }

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  align-items: start;
}

.device-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px;
  border: 1px solid rgba(226, 232, 240, 0.7);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.device-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06); }
.device-card.offline { border-color: #dbe4ef; opacity: 0.82; }
.device-card.warning { border-color: #fcd34d; }
.device-card.online { border-color: rgba(16, 185, 129, 0.22); }

.dc-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dc-name {
  flex: 1;
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dc-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dc-status-dot.online { background: #10b981; box-shadow: 0 0 6px rgba(16, 185, 129, 0.5); }
.dc-status-dot.offline { background: #94a3b8; }
.dc-status-dot.warning { background: #f59e0b; box-shadow: 0 0 6px rgba(245, 158, 11, 0.5); }

.dc-imei { font-size: 11px; color: #94a3b8; font-family: monospace; }
.dc-metrics { display: flex; flex-direction: column; gap: 8px; }
.dcm-item { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.dcm-label { width: 28px; color: #64748b; flex-shrink: 0; }
.dcm-progress { flex: 1; }
.dcm-val { width: 32px; text-align: right; font-weight: 600; color: #0f172a; }

.signal-bars {
  flex: 1;
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 16px;
}

.signal-bar {
  width: 6px;
  border-radius: 2px 2px 0 0;
  background: #e2e8f0;
}

.signal-bar:nth-child(1) { height: 4px; }
.signal-bar:nth-child(2) { height: 7px; }
.signal-bar:nth-child(3) { height: 11px; }
.signal-bar:nth-child(4) { height: 15px; }
.signal-bar.active { background: #3b82f6; }

.device-vitals {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  font-size: 12px;
  color: #475569;
  background: #f8fafc;
  border-radius: 12px;
  padding: 10px 12px;
}

.device-alert {
  border-radius: 10px;
  background: #fff7ed;
  color: #c2410c;
  padding: 10px 12px;
  font-size: 12px;
  line-height: 1.5;
}

.dc-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
  color: #94a3b8;
}

.dc-bound {
  color: #475569;
  font-weight: 600;
}

.dc-actions { display: flex; gap: 8px; }

@media (max-width: 1200px) {
  .status-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .status-grid { grid-template-columns: 1fr; }
}
</style>
