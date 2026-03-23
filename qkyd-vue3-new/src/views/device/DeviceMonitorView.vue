<template>
  <PlatformPageShellV2
    title="设备状态监控"
    subtitle="实时掌握所有在线设备的连接质量、电量状况和数据上传情况，快速识别异常设备并触发处置。"
    eyebrow="DEVICE MONITOR"
    status-note="演示版 · 当前使用模拟数据"
    status-tone="warning"
  >
    <!-- 实时状态概览 -->
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

    <!-- 筛选栏 -->
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
        <el-input v-model="searchText" placeholder="搜索设备名 / IMEI..." prefix-icon="Search" clearable style="width: 240px" />
        <el-button :icon="Refresh" circle @click="handleRefresh" :loading="refreshing" title="刷新状态" />
      </div>
    </div>

    <!-- 设备卡片网格 -->
    <div class="device-grid">
      <div v-for="device in filteredDevices" :key="device.id" class="device-card" :class="device.status">
        <div class="dc-header">
          <div class="dc-status-dot" :class="device.status"></div>
          <span class="dc-name">{{ device.name }}</span>
          <el-tag :type="statusTagType(device.status)" size="small" effect="light">{{ statusLabel[device.status] }}</el-tag>
        </div>
        <div class="dc-imei">IMEI: {{ device.imei }}</div>
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
        <div class="dc-footer">
          <span class="dc-bound">{{ device.boundTo ? '绑定: ' + device.boundTo : '未绑定对象' }}</span>
          <span class="dc-upload">{{ device.lastUpload }}</span>
        </div>
        <div class="dc-actions">
          <el-button size="small" plain @click="">查看详情</el-button>
          <el-button v-if="device.status === 'offline'" size="small" type="warning" plain>发送通知</el-button>
        </div>
      </div>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'

type DeviceStatus = 'online' | 'offline' | 'warning'

const activeTab = ref<'all' | DeviceStatus>('all')
const searchText = ref('')
const refreshing = ref(false)

const statusLabel: Record<DeviceStatus, string> = { online: '在线', offline: '离线', warning: '告警' }
const statusTagType = (s: DeviceStatus) => ({ online: 'success', offline: 'info', warning: 'warning' }[s] as any)
const batteryColor = (pct: number) => pct > 60 ? '#10b981' : pct > 30 ? '#f59e0b' : '#ef4444'
const signalLabel = (n: number) => ['无信号', '弱', '中', '强'][n] || '未知'

const allDevices = [
  { id: 1, name: '耆康手环-A01', imei: '861234567890001', status: 'online' as DeviceStatus, battery: 82, signal: 4, boundTo: '李秀英', lastUpload: '刚刚' },
  { id: 2, name: '耆康手环-A02', imei: '861234567890002', status: 'offline' as DeviceStatus, battery: 12, signal: 0, boundTo: '王建国', lastUpload: '3小时前' },
  { id: 3, name: '心率贴-B01', imei: '861234567890003', status: 'warning' as DeviceStatus, battery: 45, signal: 2, boundTo: '张淑芬', lastUpload: '15分钟前' },
  { id: 4, name: '耆康手环-A03', imei: '861234567890004', status: 'online' as DeviceStatus, battery: 91, signal: 4, boundTo: '赵志强', lastUpload: '2分钟前' },
  { id: 5, name: '睡眠监测仪-C01', imei: '861234567890005', status: 'online' as DeviceStatus, battery: 67, signal: 3, boundTo: '陈美华', lastUpload: '刚刚' },
  { id: 6, name: '耆康手环-A04', imei: '861234567890006', status: 'offline' as DeviceStatus, battery: 5, signal: 0, boundTo: '', lastUpload: '1天前' },
  { id: 7, name: '心率贴-B02', imei: '861234567890007', status: 'online' as DeviceStatus, battery: 78, signal: 4, boundTo: '刘德华', lastUpload: '刚刚' },
  { id: 8, name: '血压仪-D01', imei: '861234567890008', status: 'warning' as DeviceStatus, battery: 33, signal: 1, boundTo: '李秀英', lastUpload: '40分钟前' },
]

const statusCards = [
  { label: '全部设备', value: allDevices.length, cls: 'total', icon: 'Monitor', bg: 'linear-gradient(135deg,#eff6ff,#dbeafe)' },
  { label: '在线', value: allDevices.filter(d => d.status === 'online').length, cls: 'online', icon: 'CircleCheck', bg: 'linear-gradient(135deg,#f0fdf4,#dcfce7)' },
  { label: '离线', value: allDevices.filter(d => d.status === 'offline').length, cls: 'offline', icon: 'CircleClose', bg: 'linear-gradient(135deg,#f8fafc,#e2e8f0)' },
  { label: '告警', value: allDevices.filter(d => d.status === 'warning').length, cls: 'warning', icon: 'Warning', bg: 'linear-gradient(135deg,#fffbeb,#fef3c7)' },
]

const tabs = [
  { label: '全部', value: 'all', count: allDevices.length },
  { label: '在线', value: 'online', count: allDevices.filter(d => d.status === 'online').length },
  { label: '离线', value: 'offline', count: allDevices.filter(d => d.status === 'offline').length },
  { label: '告警', value: 'warning', count: allDevices.filter(d => d.status === 'warning').length },
]

const filteredDevices = computed(() => {
  let list = allDevices
  if (activeTab.value !== 'all') list = list.filter(d => d.status === activeTab.value)
  if (searchText.value) list = list.filter(d => d.name.includes(searchText.value) || d.imei.includes(searchText.value))
  return list
})

const handleRefresh = () => {
  refreshing.value = true
  setTimeout(() => { refreshing.value = false }, 800)
}
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
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  position: relative;
  overflow: hidden;
}

.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #3b82f6; flex-shrink: 0;
}

.stat-info { flex: 1; }
.stat-num { font-size: 30px; font-weight: 800; color: #0f172a; display: block; line-height: 1.1; }
.stat-label { font-size: 13px; color: #64748b; }
.stat-indicator {
  position: absolute; right: 0; top: 0; bottom: 0; width: 4px;
  &.online { background: #10b981; }
  &.offline { background: #94a3b8; }
  &.warning { background: #f59e0b; }
  &.total { background: #3b82f6; }
}

/* 筛选栏 */
.filter-bar {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px; flex-wrap: wrap; gap: 12px;
}
.filter-tabs { display: flex; gap: 4px; }
.filter-tab {
  padding: 8px 16px; border-radius: 8px; border: 1px solid #e2e8f0;
  background: #fff; cursor: pointer; font-size: 13px; font-weight: 500; color: #64748b;
  display: flex; align-items: center; gap: 6px;
  transition: all 0.2s;
  &:hover { background: #f8fafc; border-color: #cbd5e1; }
  &.active { background: #eff6ff; border-color: #93c5fd; color: #2563eb; font-weight: 700; }
}
.tab-count {
  background: #f1f5f9; padding: 0 6px; border-radius: 10px; font-size: 11px;
  .active & { background: #dbeafe; color: #2563eb; }
}
.filter-right { display: flex; align-items: center; gap: 10px; }

/* 设备卡片网格 */
.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.device-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  display: flex; flex-direction: column; gap: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.06); }
  &.offline { border-color: #e2e8f0; opacity: 0.8; }
  &.warning { border-color: #fcd34d; }
  &.online { border-color: rgba(16,185,129,0.2); }
}

.dc-header {
  display: flex; align-items: center; gap: 8px;
  .dc-name { flex: 1; font-size: 14px; font-weight: 700; color: #0f172a; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}
.dc-status-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
  &.online { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.5); animation: pulse 2s infinite; }
  &.offline { background: #94a3b8; }
  &.warning { background: #f59e0b; box-shadow: 0 0 6px rgba(245,158,11,0.5); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.dc-imei { font-size: 11px; color: #94a3b8; font-family: monospace; }

.dc-metrics { display: flex; flex-direction: column; gap: 8px; }
.dcm-item { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.dcm-label { width: 28px; color: #64748b; flex-shrink: 0; }
.dcm-progress { flex: 1; }
.dcm-val { width: 32px; text-align: right; font-weight: 600; color: #0f172a; }

.signal-bars {
  flex: 1; display: flex; align-items: flex-end; gap: 3px; height: 16px;
}
.signal-bar {
  width: 6px; border-radius: 2px 2px 0 0; background: #e2e8f0;
  &:nth-child(1) { height: 4px; }
  &:nth-child(2) { height: 7px; }
  &:nth-child(3) { height: 11px; }
  &:nth-child(4) { height: 15px; }
  &.active { background: #3b82f6; }
}

.dc-footer {
  display: flex; justify-content: space-between;
  font-size: 12px; color: #94a3b8;
}

.dc-actions { display: flex; gap: 8px; }

@media (max-width: 1200px) {
  .status-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
