<template>
  <PlatformPageShellV2
    title="健康报告"
    subtitle="按对象汇总真实设备扩展与异常事件，形成当前健康状态和建议动作。"
    eyebrow="HEALTH REPORT"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <el-input v-model="searchName" placeholder="搜索对象姓名" prefix-icon="Search" clearable style="width: 220px" />
        <el-select v-model="filterLevel" placeholder="风险等级" clearable style="width: 140px">
          <el-option label="高关注" value="high" />
          <el-option label="中关注" value="medium" />
          <el-option label="低关注" value="low" />
        </el-select>
        <el-button type="primary" @click="fetchData">刷新报告</el-button>
      </div>
    </template>

    <div v-loading="loading" class="report-grid">
      <div v-for="item in filteredSubjects" :key="item.id" class="report-card">
        <div class="rp-header">
          <el-avatar :size="52" :class="'avatar-' + item.level">{{ item.name.charAt(0) }}</el-avatar>
          <div class="rp-basic">
            <h3 class="rp-name">{{ item.name }}</h3>
            <p class="rp-meta">{{ item.age }}岁 · {{ item.sex }} · 当前快照</p>
          </div>
          <div class="rp-level" :class="'level-' + item.level">{{ levelLabel[item.level] }}</div>
        </div>

        <div class="rp-vitals">
          <div v-for="vital in item.vitals" :key="vital.label" class="vital-item">
            <span class="vi-label">{{ vital.label }}</span>
            <span class="vi-val" :class="{ warn: vital.warn }">{{ vital.value }}</span>
            <span class="vi-status" :class="vital.warn ? 'bad' : 'good'">{{ vital.warn ? '关注' : '稳定' }}</span>
          </div>
        </div>

        <div v-if="item.events.length" class="rp-events">
          <div class="rp-section-title">关键事件 ({{ item.events.length }})</div>
          <div v-for="event in item.events" :key="event" class="event-item">
            <div class="ev-dot" :class="item.level"></div>
            {{ event }}
          </div>
        </div>

        <div class="rp-score-section">
          <div class="rp-section-title">当前健康评分</div>
          <div class="score-bar-area">
            <div class="score-track">
              <div class="score-fill" :style="{ width: `${item.score}%`, background: scoreColor(item.score) }"></div>
            </div>
            <span class="score-num" :style="{ color: scoreColor(item.score) }">{{ item.score }}</span>
          </div>
        </div>

        <div class="rp-suggestion">
          <el-icon class="sg-icon"><ChatLineSquare /></el-icon>
          <span>{{ item.suggestion }}</span>
        </div>
      </div>

      <el-empty v-if="!loading && !filteredSubjects.length" description="暂无报告数据" />
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ChatLineSquare } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'
import { listDeviceExtensions, listExceptions, listSubjects, type DeviceInfoExtend, type ExceptionAlert, type HealthSubject } from '@/api/health'

type RiskLevel = 'high' | 'medium' | 'low'

interface ReportCard {
  id: number
  name: string
  age: number | string
  sex: string
  level: RiskLevel
  vitals: Array<{ label: string; value: string; warn: boolean }>
  events: string[]
  score: number
  suggestion: string
}

const searchName = ref('')
const filterLevel = ref<RiskLevel | ''>('')
const loading = ref(false)
const cards = ref<ReportCard[]>([])

const levelLabel: Record<RiskLevel, string> = { high: '高关注', medium: '中关注', low: '低关注' }
const scoreColor = (score: number) => (score >= 80 ? '#10b981' : score >= 60 ? '#f59e0b' : '#ef4444')

const parseNumber = (value: unknown, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const determineLevel = (extension?: DeviceInfoExtend, events: ExceptionAlert[] = []): RiskLevel => {
  const unresolved = events.filter((item) => String(item.state || '0') !== '1').length
  const hasAlarm = Boolean(String(extension?.alarmContent || '').trim())
  const battery = parseNumber(extension?.batteryLevel, 0)
  if (unresolved >= 2 || hasAlarm || battery <= 15) return 'high'
  if (unresolved >= 1 || battery <= 30) return 'medium'
  return 'low'
}

const buildSuggestion = (level: RiskLevel, extension?: DeviceInfoExtend) => {
  if (level === 'high') return '建议优先回访对象，核查当前告警与设备佩戴状态。'
  if (level === 'medium') return extension?.batteryLevel ? '建议关注设备电量与今日指标波动，保持跟踪。' : '建议继续观察近 24 小时生命体征变化。'
  return '当前状态稳定，维持常规巡检与日常监护即可。'
}

const filteredSubjects = computed(() => {
  const keyword = searchName.value.trim()
  return cards.value.filter((item) => {
    if (filterLevel.value && item.level !== filterLevel.value) return false
    if (!keyword) return true
    return item.name.includes(keyword)
  })
})

const fetchData = async () => {
  loading.value = true
  try {
    const [subjectRes, extensionRes, exceptionRes] = await Promise.all([
      listSubjects({ pageNum: 1, pageSize: 200 }),
      listDeviceExtensions({ pageNum: 1, pageSize: 200 }),
      listExceptions({ pageNum: 1, pageSize: 200 })
    ])

    const subjects = (subjectRes.rows || []) as HealthSubject[]
    const extensions = (extensionRes.rows || []) as DeviceInfoExtend[]
    const exceptions = (exceptionRes.rows || []) as ExceptionAlert[]

    const extensionMap = new Map<number, DeviceInfoExtend>()
    extensions.forEach((item) => {
      const userId = Number(item.userId || 0)
      if (userId) extensionMap.set(userId, item)
    })

    const exceptionMap = new Map<number, ExceptionAlert[]>()
    exceptions.forEach((item) => {
      const userId = Number(item.userId || 0)
      if (!userId) return
      if (!exceptionMap.has(userId)) exceptionMap.set(userId, [])
      exceptionMap.get(userId)?.push(item)
    })

    cards.value = subjects.map((subject) => {
      const id = Number(subject.subjectId || 0)
      const extension = extensionMap.get(id)
      const events = (exceptionMap.get(id) || []).sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))
      const level = determineLevel(extension, events)
      const unresolved = events.filter((item) => String(item.state || '0') !== '1').length
      const battery = parseNumber(extension?.batteryLevel, 0)
      const score = Math.max(25, 96 - unresolved * 18 - (battery && battery < 20 ? 15 : battery < 35 ? 8 : 0) - (level === 'high' ? 18 : level === 'medium' ? 8 : 0))

      return {
        id,
        name: subject.nickName || subject.subjectName || `对象 #${id}`,
        age: subject.age || '--',
        sex: subject.sex || '未知',
        level,
        vitals: [
          { label: '心率', value: extension?.heartRate ? `${extension.heartRate} bpm` : '--', warn: parseNumber(extension?.heartRate, 0) > 100 || parseNumber(extension?.heartRate, 0) < 55 },
          { label: '血氧', value: extension?.spo2 ? `${extension.spo2}%` : '--', warn: parseNumber(extension?.spo2, 0) > 0 && parseNumber(extension?.spo2, 0) < 95 },
          { label: '体温', value: extension?.temp ? `${extension.temp}°C` : '--', warn: parseNumber(extension?.temp, 0) >= 37.3 },
          { label: '步数', value: extension?.step ? `${extension.step}` : '--', warn: false }
        ],
        events: events.slice(0, 3).map((item) => `${String(item.createTime || '').slice(5, 16)} ${item.type || '异常事件'}`),
        score,
        suggestion: buildSuggestion(level, extension)
      }
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.toolbar-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.report-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 20px; }
.report-card { background: #fff; border-radius: 18px; padding: 24px; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 4px 16px rgba(0,0,0,0.05); display: flex; flex-direction: column; gap: 18px; }
.rp-header { display: flex; align-items: center; gap: 14px; }
.rp-header :deep(.el-avatar) { font-size: 20px; font-weight: 700; color: #fff; flex-shrink: 0; }
.rp-header :deep(.avatar-high) { background: linear-gradient(135deg, #ef4444, #f87171); }
.rp-header :deep(.avatar-medium) { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.rp-header :deep(.avatar-low) { background: linear-gradient(135deg, #10b981, #34d399); }
.rp-basic { flex: 1; }
.rp-name { margin: 0; font-size: 18px; font-weight: 800; color: #0f172a; }
.rp-meta { margin: 4px 0 0; font-size: 13px; color: #64748b; }
.rp-level { padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 700; }
.level-high { color: #b91c1c; background: #fef2f2; }
.level-medium { color: #b45309; background: #fffbeb; }
.level-low { color: #047857; background: #ecfdf5; }
.rp-vitals { display: grid; grid-template-columns: repeat(4, 1fr); gap: 0; background: #f8fafc; border-radius: 12px; overflow: hidden; border: 1px solid #f1f5f9; }
.vital-item { display: flex; flex-direction: column; align-items: center; padding: 12px 8px; gap: 3px; border-right: 1px solid #f1f5f9; }
.vital-item:last-child { border-right: none; }
.vi-label { font-size: 11px; color: #94a3b8; }
.vi-val { font-size: 15px; font-weight: 700; color: #0f172a; }
.vi-val.warn { color: #ef4444; }
.vi-status.good { color: #10b981; font-size: 11px; font-weight: 600; }
.vi-status.bad { color: #f59e0b; font-size: 11px; font-weight: 600; }
.rp-section-title { font-size: 12px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; }
.rp-events { display: flex; flex-direction: column; gap: 8px; }
.event-item { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #475569; }
.ev-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.ev-dot.high { background: #ef4444; }
.ev-dot.medium { background: #f59e0b; }
.ev-dot.low { background: #10b981; }
.score-bar-area { display: flex; align-items: center; gap: 12px; }
.score-track { flex: 1; height: 8px; background: #f1f5f9; border-radius: 4px; }
.score-fill { height: 8px; border-radius: 4px; transition: width 0.5s; }
.score-num { font-size: 22px; font-weight: 800; width: 36px; text-align: right; }
.rp-suggestion { display: flex; gap: 10px; align-items: flex-start; background: #f8fafc; border-radius: 10px; padding: 12px; font-size: 13px; color: #475569; line-height: 1.6; }
.sg-icon { color: #3b82f6; margin-top: 2px; flex-shrink: 0; }
</style>
