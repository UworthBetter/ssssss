<template>
  <PlatformPageShellV2
    title="规则执行概览"
    subtitle="基于真实异常检测结果统计当前各类规则的命中、风险等级与最近触发情况。"
    eyebrow="AI RULE OVERVIEW"
  >
    <div class="rule-stats">
      <div v-for="item in ruleStats" :key="item.label" class="rs-card">
        <span class="rs-num" :style="{ color: item.color }">{{ item.value }}</span>
        <span class="rs-label">{{ item.label }}</span>
      </div>
    </div>

    <div class="category-bar">
      <button
        v-for="category in categories"
        :key="category.value"
        class="cat-btn"
        :class="{ active: activeCategory === category.value }"
        @click="activeCategory = category.value"
      >
        {{ category.label }}
      </button>
    </div>

    <div class="rules-grid">
      <div v-for="rule in filteredRules" :key="rule.key" class="rule-card">
        <div class="rc-header">
          <div class="rc-icon" :style="{ background: rule.bg }">
            <el-icon :size="16"><component :is="rule.icon" /></el-icon>
          </div>
          <div class="rc-title-area">
            <span class="rc-name">{{ rule.name }}</span>
            <span class="rc-category">{{ rule.categoryLabel }}</span>
          </div>
          <el-tag :type="levelTagType(rule.level)" size="small" effect="light">{{ levelLabel[rule.level] }}</el-tag>
        </div>

        <p class="rc-desc">{{ rule.description }}</p>

        <div class="rc-conditions">
          <div v-for="item in rule.conditions" :key="item.key" class="cond-row">
            <span class="cond-key">{{ item.key }}</span>
            <span class="cond-op">=</span>
            <span class="cond-val" :class="{ 'val-high': item.high }">{{ item.value }}</span>
          </div>
        </div>

        <div class="rc-footer">
          <div class="rc-meta">
            <span class="rc-trigger-count">命中 {{ rule.triggerCount }} 次</span>
            <span class="rc-trigger-count">对象 {{ rule.subjectCount }} 人</span>
          </div>
          <span class="rc-last-time">{{ rule.lastTime }}</span>
        </div>
      </div>

      <el-empty v-if="!filteredRules.length" description="暂无规则命中数据" />
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { PlatformPageShellV2 } from '@/components/platform'
import { getRecentAbnormal } from '@/api/ai'

type RuleLevel = 'critical' | 'high' | 'medium' | 'low'

interface RecentAbnormalItem {
  userId?: number | string
  patientName?: string
  abnormalType?: string
  abnormalValue?: string
  riskLevel?: string
  createTime?: string
  readTime?: string
  detectedTime?: string
}

interface RuleCard {
  key: string
  name: string
  category: string
  categoryLabel: string
  level: RuleLevel
  description: string
  triggerCount: number
  subjectCount: number
  lastTime: string
  bg: string
  icon: string
  conditions: Array<{ key: string; value: string; high?: boolean }>
}

const activeCategory = ref('all')
const rules = ref<RuleCard[]>([])

const levelLabel: Record<RuleLevel, string> = { critical: '紧急', high: '高风险', medium: '中风险', low: '低风险' }
const levelTagType = (level: RuleLevel) => ({ critical: 'danger', high: 'warning', medium: '', low: 'success' }[level] as 'danger' | 'warning' | '' | 'success')

const categories = computed(() => [
  { label: '全部', value: 'all' },
  ...Array.from(new Set(rules.value.map((item) => item.category))).map((value) => ({
    label: rules.value.find((item) => item.category === value)?.categoryLabel || value,
    value
  }))
])

const filteredRules = computed(() => activeCategory.value === 'all' ? rules.value : rules.value.filter((item) => item.category === activeCategory.value))

const ruleStats = computed(() => [
  { label: '规则类型', value: rules.value.length, color: '#3b82f6' },
  { label: '高风险规则', value: rules.value.filter((item) => ['critical', 'high'].includes(item.level)).length, color: '#ef4444' },
  { label: '累计命中', value: rules.value.reduce((sum, item) => sum + item.triggerCount, 0), color: '#f59e0b' },
  { label: '覆盖对象', value: rules.value.reduce((sum, item) => sum + item.subjectCount, 0), color: '#10b981' }
])

const resolveRuleType = (abnormalType?: string) => {
  const text = String(abnormalType || '').toLowerCase()
  if (text.includes('心率')) return { key: 'heartRate', label: '心率规则', icon: 'Odometer', bg: 'linear-gradient(135deg,#fee2e2,#fca5a5)' }
  if (text.includes('血氧')) return { key: 'spo2', label: '血氧规则', icon: 'Aim', bg: 'linear-gradient(135deg,#dcfce7,#86efac)' }
  if (text.includes('血压')) return { key: 'bloodPressure', label: '血压规则', icon: 'DataAnalysis', bg: 'linear-gradient(135deg,#dbeafe,#93c5fd)' }
  if (text.includes('体温')) return { key: 'temperature', label: '体温规则', icon: 'Sunny', bg: 'linear-gradient(135deg,#ffedd5,#fdba74)' }
  if (text.includes('围栏')) return { key: 'fence', label: '围栏规则', icon: 'Guide', bg: 'linear-gradient(135deg,#fef3c7,#fcd34d)' }
  if (text.includes('sos') || text.includes('求救')) return { key: 'sos', label: '求救规则', icon: 'Bell', bg: 'linear-gradient(135deg,#fee2e2,#fecaca)' }
  if (text.includes('离线') || text.includes('信号')) return { key: 'device', label: '设备规则', icon: 'Monitor', bg: 'linear-gradient(135deg,#e2e8f0,#cbd5e1)' }
  return { key: 'activity', label: '活动规则', icon: 'DataLine', bg: 'linear-gradient(135deg,#e9d5ff,#c4b5fd)' }
}

const resolveLevel = (riskLevel?: string): RuleLevel => {
  const text = String(riskLevel || '').toLowerCase()
  if (['critical', 'danger', '紧急'].some((item) => text.includes(item))) return 'critical'
  if (['high', '高'].some((item) => text.includes(item))) return 'high'
  if (['medium', 'warning', '中'].some((item) => text.includes(item))) return 'medium'
  return 'low'
}

const formatTime = (value?: string) => {
  const timestamp = value ? new Date(value).getTime() : NaN
  if (!Number.isFinite(timestamp)) return '暂无'
  const date = new Date(timestamp)
  return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const fetchRules = async () => {
  const res = await getRecentAbnormal(100)
  const recentRows = (res.data || []) as RecentAbnormalItem[]
  const grouped = new Map<string, RecentAbnormalItem[]>()

  recentRows.forEach((item) => {
    const { key } = resolveRuleType(item.abnormalType)
    if (!grouped.has(key)) grouped.set(key, [])
    grouped.get(key)?.push(item)
  })

  rules.value = Array.from(grouped.entries()).map(([key, items]) => {
    const config = resolveRuleType(items[0]?.abnormalType)
    const level = items.reduce<RuleLevel>((highest, current) => {
      const order: RuleLevel[] = ['low', 'medium', 'high', 'critical']
      const currentLevel = resolveLevel(current.riskLevel)
      return order.indexOf(currentLevel) > order.indexOf(highest) ? currentLevel : highest
    }, 'low')

    const subjectSet = new Set(items.map((item) => String(item.userId || item.patientName || 'unknown')))
    const latest = items
      .map((item) => item.detectedTime || item.createTime || item.readTime)
      .filter(Boolean)
      .sort()
      .at(-1)

    return {
      key,
      name: `${config.label}执行`,
      category: config.key,
      categoryLabel: config.label,
      level,
      description: `当前基于真实告警流统计 ${config.label} 的命中情况与风险强度。`,
      triggerCount: items.length,
      subjectCount: subjectSet.size,
      lastTime: formatTime(latest),
      bg: config.bg,
      icon: config.icon,
      conditions: [
        { key: '最新触发', value: items[0]?.abnormalType || '无', high: level === 'critical' || level === 'high' },
        { key: '最近指标', value: items[0]?.abnormalValue || '--' },
        { key: '最高风险', value: levelLabel[level], high: level === 'critical' || level === 'high' }
      ]
    }
  }).sort((a, b) => b.triggerCount - a.triggerCount)
}

onMounted(() => {
  fetchRules()
})
</script>

<style scoped lang="scss">
.rule-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.rs-card { background: #fff; border-radius: 14px; padding: 20px 22px; display: flex; flex-direction: column; gap: 6px; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 8px rgba(0,0,0,0.03); }
.rs-num { font-size: 36px; font-weight: 800; line-height: 1; }
.rs-label { font-size: 13px; color: #64748b; }
.category-bar { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 20px; }
.cat-btn { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border-radius: 8px; border: 1px solid #e2e8f0; background: #fff; cursor: pointer; font-size: 13px; font-weight: 500; color: #64748b; transition: all 0.2s; }
.cat-btn.active { background: #eff6ff; border-color: #93c5fd; color: #2563eb; font-weight: 700; }
.rules-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.rule-card { background: #fff; border-radius: 16px; padding: 20px; border: 1px solid rgba(226,232,240,0.7); box-shadow: 0 2px 12px rgba(0,0,0,0.04); display: flex; flex-direction: column; gap: 14px; }
.rc-header { display: flex; align-items: center; gap: 12px; }
.rc-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #475569; flex-shrink: 0; }
.rc-title-area { flex: 1; min-width: 0; }
.rc-name { font-size: 15px; font-weight: 700; color: #0f172a; display: block; }
.rc-category { font-size: 12px; color: #94a3b8; }
.rc-desc { font-size: 13px; color: #475569; line-height: 1.6; margin: 0; }
.rc-conditions { background: #f8fafc; border-radius: 10px; padding: 10px 14px; display: flex; flex-direction: column; gap: 6px; }
.cond-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.cond-key { color: #64748b; flex-shrink: 0; }
.cond-op { color: #94a3b8; font-family: monospace; }
.cond-val { font-weight: 600; color: #0f172a; }
.val-high { color: #ef4444; }
.rc-footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.rc-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.rc-trigger-count { font-size: 12px; color: #64748b; }
.rc-last-time { font-size: 12px; color: #94a3b8; }
@media (max-width: 1024px) { .rule-stats { grid-template-columns: repeat(2, 1fr); } }
</style>
