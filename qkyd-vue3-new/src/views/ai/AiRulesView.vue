<template>
  <PlatformPageShellV2
    title="规则配置"
    subtitle="统一管理各类 AI 告警触发阈值、规则组合策略与通知链路，灵活适配不同对象的监护需求。"
    eyebrow="AI RULE CONFIG"
    status-note="演示版 · 当前使用模拟数据"
    status-tone="warning"
  >
    <template #headerExtra>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增规则</el-button>
    </template>

    <!-- 规则统计 -->
    <div class="rule-stats">
      <div v-for="s in ruleStats" :key="s.label" class="rs-card">
        <span class="rs-num" :style="{ color: s.color }">{{ s.value }}</span>
        <span class="rs-label">{{ s.label }}</span>
      </div>
    </div>

    <!-- 规则分类标签 -->
    <div class="category-bar">
      <button
        v-for="cat in categories"
        :key="cat.value"
        class="cat-btn"
        :class="{ active: activeCategory === cat.value }"
        @click="activeCategory = cat.value"
      >
        <el-icon><component :is="cat.icon" /></el-icon>
        {{ cat.label }}
      </button>
    </div>

    <!-- 规则卡片列表 -->
    <div class="rules-grid">
      <div v-for="rule in filteredRules" :key="rule.id" class="rule-card" :class="{ disabled: !rule.enabled }">
        <div class="rc-header">
          <div class="rc-icon" :style="{ background: categoryColor(rule.category) }">
            <el-icon :size="16"><component :is="categoryIcon(rule.category)" /></el-icon>
          </div>
          <div class="rc-title-area">
            <span class="rc-name">{{ rule.name }}</span>
            <span class="rc-category">{{ categoryLabel(rule.category) }}</span>
          </div>
          <el-switch v-model="rule.enabled" @change="handleToggle(rule)" />
        </div>

        <p class="rc-desc">{{ rule.description }}</p>

        <div class="rc-conditions">
          <div v-for="cond in rule.conditions" :key="cond.key" class="cond-row">
            <span class="cond-key">{{ cond.key }}</span>
            <span class="cond-op">{{ cond.op }}</span>
            <span class="cond-val" :class="{ 'val-high': cond.high }">{{ cond.value }}</span>
          </div>
        </div>

        <div class="rc-footer">
          <div class="rc-meta">
            <el-tag :type="levelTagType(rule.level)" size="small" effect="light">{{ levelLabel[rule.level] }}</el-tag>
            <span class="rc-trigger-count">命中 {{ rule.triggerCount }} 次</span>
          </div>
          <div class="rc-actions">
            <el-button size="small" text type="primary" @click="openEdit(rule)">编辑</el-button>
            <el-button size="small" text type="danger" @click="deleteRule(rule.id)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingRule?.id ? '编辑规则' : '新增规则'" width="560px">
      <el-form v-if="form" :model="form" label-width="96px">
        <el-form-item label="规则名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="规则类别">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="心率监测" value="heartRate" />
            <el-option label="血氧监测" value="spo2" />
            <el-option label="跌倒检测" value="fall" />
            <el-option label="睡眠监控" value="sleep" />
            <el-option label="综合风险" value="risk" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="form.level" style="width: 100%">
            <el-option label="紧急" value="critical" />
            <el-option label="高级" value="high" />
            <el-option label="中级" value="medium" />
            <el-option label="低级" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRule">保存规则</el-button>
      </template>
    </el-dialog>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { PlatformPageShellV2 } from '@/components/platform'

type RuleLevel = 'critical' | 'high' | 'medium' | 'low'
type RuleCategory = 'heartRate' | 'spo2' | 'fall' | 'sleep' | 'risk'

const activeCategory = ref<'all' | RuleCategory>('all')
const dialogVisible = ref(false)
const editingRule = ref<any>(null)
const form = reactive<any>({})

const levelLabel: Record<RuleLevel, string> = { critical: '紧急', high: '高级', medium: '中级', low: '低级' }
const levelTagType = (l: RuleLevel) => ({ critical: 'danger', high: 'warning', medium: '', low: 'success' }[l] as any)

const categoryLabel = (cat: RuleCategory) => ({ heartRate: '心率', spo2: '血氧', fall: '跌倒', sleep: '睡眠', risk: '综合' }[cat])
const categoryIcon = (cat: RuleCategory) => ({ heartRate: 'Odometer', spo2: 'Aim', fall: 'Warning', sleep: 'Moon', risk: 'DataAnalysis' }[cat])
const categoryColor = (cat: RuleCategory) => ({
  heartRate: 'linear-gradient(135deg,#fee2e2,#fca5a5)',
  spo2: 'linear-gradient(135deg,#dcfce7,#86efac)',
  fall: 'linear-gradient(135deg,#fef3c7,#fcd34d)',
  sleep: 'linear-gradient(135deg,#ede9fe,#c4b5fd)',
  risk: 'linear-gradient(135deg,#dbeafe,#93c5fd)',
}[cat])

const categories = [
  { label: '全部', value: 'all', icon: 'Grid' },
  { label: '心率', value: 'heartRate', icon: 'Odometer' },
  { label: '血氧', value: 'spo2', icon: 'Aim' },
  { label: '跌倒', value: 'fall', icon: 'Warning' },
  { label: '睡眠', value: 'sleep', icon: 'Moon' },
  { label: '综合风险', value: 'risk', icon: 'DataAnalysis' },
]

const rules = ref([
  { id: 1, name: '心率过速告警', category: 'heartRate' as RuleCategory, level: 'critical' as RuleLevel, enabled: true, description: '当检测心率连续 5 分钟超过 100 bpm 时，立即触发紧急告警并通知医护人员。', conditions: [{ key: '心率', op: '>', value: '100 bpm', high: true }, { key: '持续时长', op: '≥', value: '5 分钟', high: false }], triggerCount: 12 },
  { id: 2, name: '心率过缓预警', category: 'heartRate' as RuleCategory, level: 'high' as RuleLevel, enabled: true, description: '当心率低于 50 bpm 持续超过 3 分钟时，触发高级预警。', conditions: [{ key: '心率', op: '<', value: '50 bpm', high: true }, { key: '持续时长', op: '≥', value: '3 分钟', high: false }], triggerCount: 5 },
  { id: 3, name: '血氧低饱和度告警', category: 'spo2' as RuleCategory, level: 'critical' as RuleLevel, enabled: true, description: '血氧饱和度低于 93% 且持续 2 分钟以上，触发紧急告警。', conditions: [{ key: 'SpO₂', op: '<', value: '93%', high: true }, { key: '持续时长', op: '≥', value: '2 分钟', high: false }], triggerCount: 3 },
  { id: 4, name: '跌倒行为检测', category: 'fall' as RuleCategory, level: 'critical' as RuleLevel, enabled: true, description: '加速度传感器检测到疑似跌倒事件时，立即派发通知并记录位置。', conditions: [{ key: '加速度变化', op: '>', value: '4g（阈值）', high: true }, { key: '静止状态', op: '持续', value: '> 30s', high: false }], triggerCount: 2 },
  { id: 5, name: '睡眠异常监测', category: 'sleep' as RuleCategory, level: 'medium' as RuleLevel, enabled: false, description: '当睡眠时段内心率波动超过 25 bpm 且同时伴有体动异常时，触发中级预警。', conditions: [{ key: '心率波动', op: '>', value: '25 bpm', high: false }, { key: '体动指数', op: '>', value: '70', high: false }], triggerCount: 8 },
  { id: 6, name: '综合高风险评估', category: 'risk' as RuleCategory, level: 'high' as RuleLevel, enabled: true, description: '当AI综合风险评分超过75分，且近24小时内已有异常记录，触发高级预警并向主管报告。', conditions: [{ key: '风险评分', op: '>', value: '75分', high: true }, { key: '近24h异常', op: '≥', value: '1次', high: false }], triggerCount: 19 },
])

const ruleStats = [
  { label: '规则总数',   value: rules.value.length,                                   color: '#3b82f6' },
  { label: '已启用',    value: rules.value.filter(r => r.enabled).length,             color: '#10b981' },
  { label: '已禁用',    value: rules.value.filter(r => !r.enabled).length,            color: '#94a3b8' },
  { label: '本月命中',  value: rules.value.reduce((s, r) => s + r.triggerCount, 0),   color: '#f59e0b' },
]

const filteredRules = computed(() =>
  activeCategory.value === 'all' ? rules.value : rules.value.filter(r => r.category === activeCategory.value)
)

const openCreate = () => {
  editingRule.value = null
  Object.assign(form, { name: '', category: 'heartRate', level: 'medium', description: '', enabled: true })
  dialogVisible.value = true
}

const openEdit = (rule: any) => {
  editingRule.value = rule
  Object.assign(form, { ...rule })
  dialogVisible.value = true
}

const submitRule = () => {
  if (editingRule.value?.id) {
    Object.assign(editingRule.value, form)
    ElMessage.success('规则已更新')
  } else {
    rules.value.push({ ...form, id: Date.now(), triggerCount: 0, conditions: [] })
    ElMessage.success('规则已创建')
  }
  dialogVisible.value = false
}

const deleteRule = async (id: number) => {
  await ElMessageBox.confirm('删除后无法恢复，确认删除该规则吗？', '提示', { type: 'warning' })
  const idx = rules.value.findIndex(r => r.id === id)
  if (idx >= 0) rules.value.splice(idx, 1)
  ElMessage.success('规则已删除')
}

const handleToggle = (rule: any) => {
  ElMessage.success(rule.enabled ? `规则「${rule.name}」已启用` : `规则「${rule.name}」已禁用`)
}
</script>

<style scoped lang="scss">
.rule-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.rs-card {
  background: #fff; border-radius: 14px; padding: 20px 22px;
  display: flex; flex-direction: column; gap: 6px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.rs-num { font-size: 36px; font-weight: 800; line-height: 1; }
.rs-label { font-size: 13px; color: #64748b; }

/* 分类标签 */
.category-bar {
  display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 20px;
}
.cat-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px; border: 1px solid #e2e8f0;
  background: #fff; cursor: pointer; font-size: 13px; font-weight: 500; color: #64748b;
  transition: all 0.2s;
  &:hover { background: #f8fafc; }
  &.active { background: #eff6ff; border-color: #93c5fd; color: #2563eb; font-weight: 700; }
}

/* 规则卡片 */
.rules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.rule-card {
  background: #fff; border-radius: 16px; padding: 20px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  display: flex; flex-direction: column; gap: 14px;
  transition: all 0.2s;
  &:hover { box-shadow: 0 6px 20px rgba(0,0,0,0.07); transform: translateY(-1px); }
  &.disabled { opacity: 0.65; }
}

.rc-header {
  display: flex; align-items: center; gap: 12px;
}
.rc-icon {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #475569; flex-shrink: 0;
}
.rc-title-area { flex: 1; min-width: 0; }
.rc-name { font-size: 15px; font-weight: 700; color: #0f172a; display: block; }
.rc-category { font-size: 12px; color: #94a3b8; }

.rc-desc { font-size: 13px; color: #475569; line-height: 1.6; margin: 0; }

.rc-conditions {
  background: #f8fafc; border-radius: 10px; padding: 10px 14px;
  display: flex; flex-direction: column; gap: 6px;
}
.cond-row {
  display: flex; align-items: center; gap: 8px; font-size: 13px;
  .cond-key { color: #64748b; flex-shrink: 0; }
  .cond-op { color: #94a3b8; font-family: monospace; }
  .cond-val { font-weight: 600; color: #0f172a; &.val-high { color: #ef4444; } }
}

.rc-footer {
  display: flex; align-items: center; justify-content: space-between;
}
.rc-meta { display: flex; align-items: center; gap: 10px; }
.rc-trigger-count { font-size: 12px; color: #94a3b8; }
.rc-actions { display: flex; gap: 4px; }

@media (max-width: 1024px) {
  .rule-stats { grid-template-columns: repeat(2, 1fr); }
}
</style>
