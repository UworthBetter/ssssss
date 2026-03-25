<template>
  <PlatformPageShellV2
    title="风险画像"
    subtitle="基于真实异常事件、设备状态与生命体征快照生成对象风险分层与干预建议。"
    eyebrow="RISK PROFILE"
  >
    <div class="summary-grid">
      <div v-for="card in summaryCards" :key="card.label" class="summary-card" :class="card.cls">
        <div class="sc-left">
          <span class="sc-num">{{ card.count }}</span>
          <span class="sc-label">{{ card.label }}</span>
        </div>
        <div class="sc-right">
          <div class="sc-bar-bg">
            <div class="sc-bar" :style="{ width: `${card.percent}%`, background: card.color }"></div>
          </div>
          <span class="sc-pct">{{ card.percent }}%</span>
        </div>
      </div>
    </div>

    <div class="section-row">
      <div class="panel-card risk-list-card">
        <div class="panel-header">
          <span class="panel-title">对象风险排序</span>
          <div class="header-filter">
            <el-select v-model="filterLevel" placeholder="全部等级" clearable size="small" style="width: 120px">
              <el-option label="高风险" value="high" />
              <el-option label="中风险" value="medium" />
              <el-option label="低风险" value="low" />
            </el-select>
          </div>
        </div>
        <div class="risk-rows">
          <div
            v-for="(item, index) in filteredRiskList"
            :key="item.id"
            class="risk-row"
            :class="{ active: selectedId === item.id }"
            @click="selectedId = item.id"
          >
            <div class="rr-rank" :class="index < 3 ? 'top-' + (index + 1) : ''">{{ index + 1 }}</div>
            <el-avatar :size="36" :class="'avatar-' + item.level">{{ item.name.charAt(0) }}</el-avatar>
            <div class="rr-info">
              <span class="rr-name">{{ item.name }}</span>
              <div class="rr-tags">
                <el-tag v-for="tag in item.tags" :key="tag" size="small" effect="plain" class="mini-tag">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="rr-score-area">
              <span class="rr-score" :class="item.level">{{ item.score }}</span>
              <div class="risk-badge" :class="'risk-' + item.level">{{ levelLabel[item.level] }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="selectedProfile" class="panel-card profile-detail">
        <div class="pd-header">
          <el-avatar :size="60" :class="'avatar-' + selectedProfile.level">{{ selectedProfile.name.charAt(0) }}</el-avatar>
          <div class="pd-name-area">
            <h3>{{ selectedProfile.name }}</h3>
            <p>{{ selectedProfile.age }}岁 · {{ selectedProfile.sex }} · 风险分 {{ selectedProfile.score }}</p>
          </div>
          <div class="risk-badge lg" :class="'risk-' + selectedProfile.level">{{ levelLabel[selectedProfile.level] }}</div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">风险维度</div>
          <div class="radar-mock">
            <div v-for="dim in selectedProfile.dims" :key="dim.name" class="radar-row">
              <span class="radar-label">{{ dim.name }}</span>
              <div class="radar-bar-bg">
                <div class="radar-bar" :style="{ width: `${dim.score}%`, background: riskColor(selectedProfile.level) }"></div>
              </div>
              <span class="radar-val">{{ dim.score }}</span>
            </div>
          </div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">AI 风险标签</div>
          <div class="tag-cloud">
            <el-tag v-for="tag in selectedProfile.tags" :key="tag" effect="plain" round class="risk-tag">{{ tag }}</el-tag>
          </div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">干预建议</div>
          <div class="suggestions">
            <div v-for="suggestion in selectedProfile.suggestions" :key="suggestion" class="suggestion-item">
              <el-icon class="s-icon"><InfoFilled /></el-icon>
              <span>{{ suggestion }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'
import { listDevices, listDeviceExtensions, listExceptions, listSubjects, type DeviceInfo, type DeviceInfoExtend, type ExceptionAlert, type HealthSubject } from '@/api/health'

type RiskLevel = 'high' | 'medium' | 'low'

interface RiskProfileItem {
  id: number
  name: string
  age: number | string
  sex: string
  level: RiskLevel
  score: number
  tags: string[]
  dims: Array<{ name: string; score: number }>
  suggestions: string[]
}

const filterLevel = ref<RiskLevel | ''>('')
const selectedId = ref<number>()
const list = ref<RiskProfileItem[]>([])

const levelLabel: Record<RiskLevel, string> = { high: '高风险', medium: '中风险', low: '低风险' }
const riskColor = (level: RiskLevel) => ({ high: '#ef4444', medium: '#f59e0b', low: '#10b981' }[level])

const parseNumber = (value: unknown, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const buildSuggestions = (level: RiskLevel, tags: string[]) => {
  if (level === 'high') return ['建议优先回访并核查近期异常原因', '保持设备在线并安排重点监护', '必要时同步家属与护理人员']
  if (level === 'medium') return ['建议跟踪近 24 小时体征变化', '关注设备电量与通信稳定性', '结合异常类型调整巡检频次']
  return ['当前状态平稳，维持常规监护', '继续保持设备在线和日常活动记录']
}

const filteredRiskList = computed(() => filterLevel.value ? list.value.filter((item) => item.level === filterLevel.value) : list.value)
const selectedProfile = computed(() => filteredRiskList.value.find((item) => item.id === selectedId.value) || filteredRiskList.value[0])

const summaryCards = computed(() => {
  const total = Math.max(list.value.length, 1)
  return [
    { label: '高风险', count: list.value.filter((item) => item.level === 'high').length, cls: 'high', color: '#ef4444' },
    { label: '中风险', count: list.value.filter((item) => item.level === 'medium').length, cls: 'medium', color: '#f59e0b' },
    { label: '低风险', count: list.value.filter((item) => item.level === 'low').length, cls: 'low', color: '#10b981' },
    { label: '监护总数', count: list.value.length, cls: 'total', color: '#3b82f6' }
  ].map((item) => ({ ...item, percent: Math.round((item.count / total) * 100) }))
})

const fetchData = async () => {
  const [subjectRes, deviceRes, extensionRes, exceptionRes] = await Promise.all([
    listSubjects({ pageNum: 1, pageSize: 200 }),
    listDevices({ pageNum: 1, pageSize: 200 }),
    listDeviceExtensions({ pageNum: 1, pageSize: 200 }),
    listExceptions({ pageNum: 1, pageSize: 200 })
  ])

  const subjects = (subjectRes.rows || []) as HealthSubject[]
  const devices = (deviceRes.rows || []) as DeviceInfo[]
  const extensions = (extensionRes.rows || []) as DeviceInfoExtend[]
  const exceptions = (exceptionRes.rows || []) as ExceptionAlert[]

  // 建立 deviceId → extension 映射
  const extByDeviceId = new Map<number, DeviceInfoExtend>()
  extensions.forEach((item) => {
    const deviceId = Number(item.deviceId || 0)
    if (deviceId) extByDeviceId.set(deviceId, item)
  })

  // 通过设备表建立 userId → extension 映射
  const extensionMap = new Map<number, DeviceInfoExtend>()
  devices.forEach((device) => {
    const userId = Number(device.userId || 0)
    const deviceId = Number(device.id || 0)
    if (!userId || !deviceId) return
    const ext = extByDeviceId.get(deviceId)
    if (ext) extensionMap.set(userId, ext)
  })

  const exceptionMap = new Map<number, ExceptionAlert[]>()
  exceptions.forEach((item) => {
    const userId = Number(item.userId || 0)
    if (!userId) return
    if (!exceptionMap.has(userId)) exceptionMap.set(userId, [])
    exceptionMap.get(userId)?.push(item)
  })

  list.value = subjects.map((subject) => {
    const id = Number(subject.subjectId || 0)
    const extension = extensionMap.get(id)
    const events = exceptionMap.get(id) || []
    const unresolved = events.filter((item) => String(item.state || '0') !== '1').length
    const battery = parseNumber(extension?.batteryLevel, 0)
    const offline = extension?.lastCommunicationTime ? (Date.now() - new Date(extension.lastCommunicationTime).getTime() > 60 * 60 * 1000) : true
    const heartRisk = parseNumber(extension?.heartRate, 0) > 100 || parseNumber(extension?.heartRate, 0) < 55 ? 78 : 28
    const oxygenRisk = parseNumber(extension?.spo2, 0) > 0 && parseNumber(extension?.spo2, 0) < 95 ? 76 : 22
    const tempRisk = parseNumber(extension?.temp, 0) >= 37.3 ? 70 : 18
    const deviceRisk = offline ? 82 : battery && battery <= 20 ? 66 : 24
    const exceptionRisk = Math.min(95, unresolved * 24 + events.length * 8)
    const score = Math.min(98, Math.round((heartRisk + oxygenRisk + tempRisk + deviceRisk + exceptionRisk) / 5))
    const level: RiskLevel = score >= 70 ? 'high' : score >= 45 ? 'medium' : 'low'

    const tags = [
      unresolved ? `${unresolved} 条未闭环异常` : '异常可控',
      offline ? '设备离线' : '设备在线',
      battery && battery <= 20 ? '低电量' : '',
      extension?.alarmContent ? '有设备告警' : '',
      extension?.heartRate ? `心率 ${extension.heartRate}` : '',
      extension?.spo2 ? `血氧 ${extension.spo2}%` : ''
    ].filter(Boolean)

    return {
      id,
      name: subject.nickName || subject.subjectName || `对象 #${id}`,
      age: subject.age || '--',
      sex: subject.sex || '未知',
      level,
      score,
      tags,
      dims: [
        { name: '异常活跃度', score: exceptionRisk },
        { name: '设备连续性', score: deviceRisk },
        { name: '心率风险', score: heartRisk },
        { name: '血氧风险', score: oxygenRisk },
        { name: '体温风险', score: tempRisk }
      ],
      suggestions: buildSuggestions(level, tags)
    }
  }).sort((a, b) => b.score - a.score)

  if (!selectedId.value && list.value.length) {
    selectedId.value = list.value[0].id
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.summary-card { background: #fff; border-radius: 14px; padding: 18px 20px; display: flex; justify-content: space-between; align-items: center; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 8px rgba(0,0,0,0.03); border-left: 4px solid transparent; }
.summary-card.high { border-left-color: #ef4444; }
.summary-card.medium { border-left-color: #f59e0b; }
.summary-card.low { border-left-color: #10b981; }
.summary-card.total { border-left-color: #3b82f6; }
.sc-left { display: flex; flex-direction: column; gap: 4px; }
.sc-num { font-size: 32px; font-weight: 800; color: #0f172a; line-height: 1; }
.sc-label { font-size: 13px; color: #64748b; }
.sc-right { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; width: 80px; }
.sc-bar-bg { width: 100%; height: 4px; background: #f1f5f9; border-radius: 2px; }
.sc-bar { height: 4px; border-radius: 2px; transition: width 0.5s; }
.sc-pct { font-size: 12px; color: #94a3b8; }
.section-row { display: flex; gap: 20px; align-items: flex-start; }
.panel-card { background: #fff; border-radius: 16px; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 12px rgba(0,0,0,0.04); overflow: hidden; }
.panel-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid #f1f5f9; }
.panel-title { font-size: 14px; font-weight: 700; color: #0f172a; }
.risk-list-card { flex: 1; }
.risk-rows { padding: 8px; }
.risk-row { display: flex; align-items: center; gap: 12px; padding: 12px 12px; border-radius: 10px; cursor: pointer; transition: all 0.2s; }
.risk-row:hover { background: #f8fafc; }
.risk-row.active { background: #eff6ff; }
.risk-row :deep(.el-avatar) { font-size: 14px; font-weight: 700; color: #fff; flex-shrink: 0; }
.risk-row :deep(.avatar-high) { background: linear-gradient(135deg, #ef4444, #f87171); }
.risk-row :deep(.avatar-medium) { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.risk-row :deep(.avatar-low) { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.rr-rank { width: 24px; height: 24px; border-radius: 6px; background: #f1f5f9; color: #64748b; font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.top-1 { background: #fef3c7; color: #b45309; }
.top-2 { background: #e2e8f0; color: #475569; }
.top-3 { background: #fde8d8; color: #9a3412; }
.rr-info { flex: 1; min-width: 0; }
.rr-name { font-size: 14px; font-weight: 600; color: #0f172a; display: block; }
.rr-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 4px; }
.mini-tag { font-size: 11px !important; }
.rr-score-area { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.rr-score { font-size: 20px; font-weight: 800; }
.rr-score.high { color: #ef4444; }
.rr-score.medium { color: #f59e0b; }
.rr-score.low { color: #10b981; }
.risk-badge { display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.risk-high { color: #b91c1c; background: #fef2f2; }
.risk-medium { color: #b45309; background: #fffbeb; }
.risk-low { color: #047857; background: #ecfdf5; }
.lg { padding: 4px 12px; font-size: 13px; }
.profile-detail { flex: 0 0 360px; }
.pd-header { display: flex; align-items: center; gap: 16px; padding: 20px; }
.pd-header :deep(.el-avatar) { font-size: 22px; font-weight: 700; color: #fff; flex-shrink: 0; }
.pd-header :deep(.avatar-high) { background: linear-gradient(135deg, #ef4444, #f87171); }
.pd-header :deep(.avatar-medium) { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.pd-header :deep(.avatar-low) { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.pd-name-area { flex: 1; }
.pd-name-area h3 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }
.pd-name-area p { margin: 4px 0 0; font-size: 13px; color: #64748b; }
.pd-section { padding: 0 20px 20px; }
.pd-section-title { font-size: 13px; font-weight: 700; color: #64748b; margin-bottom: 12px; text-transform: uppercase; letter-spacing: 0.05em; }
.radar-mock { display: flex; flex-direction: column; gap: 10px; }
.radar-row { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.radar-label { width: 72px; color: #64748b; flex-shrink: 0; }
.radar-bar-bg { flex: 1; height: 6px; background: #f1f5f9; border-radius: 3px; }
.radar-bar { height: 6px; border-radius: 3px; transition: width 0.5s; }
.radar-val { width: 28px; text-align: right; font-weight: 600; color: #0f172a; }
.tag-cloud { display: flex; flex-wrap: wrap; gap: 8px; }
.risk-tag { border-radius: 8px; }
.suggestions { display: flex; flex-direction: column; gap: 8px; }
.suggestion-item { display: flex; align-items: flex-start; gap: 8px; font-size: 13px; color: #475569; line-height: 1.5; }
.s-icon { color: #3b82f6; margin-top: 2px; flex-shrink: 0; }
@media (max-width: 1200px) { .summary-grid { grid-template-columns: repeat(2, 1fr); } .section-row { flex-direction: column; } .profile-detail { flex: none; width: 100%; } }
</style>
