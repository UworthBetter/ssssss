<template>
  <PlatformPageShellV2
    title="风险画像"
    subtitle="对服务对象进行多维度风险评估，自动生成风险标签与等级分布，辅助关键对象精准干预。"
    eyebrow="RISK PROFILE"
    status-note="演示版 · 当前使用模拟数据"
    status-tone="warning"
  >
    <!-- 汇总卡片 -->
    <div class="summary-grid">
      <div v-for="card in summaryCards" :key="card.label" class="summary-card" :class="card.cls">
        <div class="sc-left">
          <span class="sc-num">{{ card.count }}</span>
          <span class="sc-label">{{ card.label }}</span>
        </div>
        <div class="sc-right">
          <div class="sc-bar-bg">
            <div class="sc-bar" :style="{ width: (card.count / totalSubjects * 100) + '%', background: card.color }"></div>
          </div>
          <span class="sc-pct">{{ Math.round(card.count / totalSubjects * 100) }}%</span>
        </div>
      </div>
    </div>

    <!-- 对象风险矩阵 -->
    <div class="section-row">
      <!-- 风险列表 -->
      <div class="panel-card risk-list-card">
        <div class="panel-header">
          <span class="panel-title">对象风险排名</span>
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
            v-for="(item, i) in filteredRiskList"
            :key="item.id"
            class="risk-row"
            :class="{ active: selectedId === item.id }"
            @click="selectedId = item.id"
          >
            <div class="rr-rank" :class="i < 3 ? 'top-' + (i + 1) : ''">{{ i + 1 }}</div>
            <el-avatar :size="36" :class="'avatar-' + item.level">{{ item.name.charAt(0) }}</el-avatar>
            <div class="rr-info">
              <span class="rr-name">{{ item.name }}</span>
              <div class="rr-tags">
                <el-tag v-for="t in item.tags" :key="t" size="small" effect="plain" class="mini-tag">{{ t }}</el-tag>
              </div>
            </div>
            <div class="rr-score-area">
              <span class="rr-score" :class="item.level">{{ item.score }}</span>
              <div class="risk-badge" :class="'risk-' + item.level">{{ levelLabel[item.level] }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧画像详情 -->
      <div class="panel-card profile-detail" v-if="selectedProfile">
        <div class="pd-header">
          <el-avatar :size="60" :class="'avatar-' + selectedProfile.level">{{ selectedProfile.name.charAt(0) }}</el-avatar>
          <div class="pd-name-area">
            <h3>{{ selectedProfile.name }}</h3>
            <p>{{ selectedProfile.age }}岁 · {{ selectedProfile.sex }} · 风险分 {{ selectedProfile.score }}</p>
          </div>
          <div class="risk-badge lg" :class="'risk-' + selectedProfile.level">{{ levelLabel[selectedProfile.level] }}</div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">风险评分雷达</div>
          <div class="radar-mock">
            <div v-for="dim in selectedProfile.dims" :key="dim.name" class="radar-row">
              <span class="radar-label">{{ dim.name }}</span>
              <div class="radar-bar-bg">
                <div class="radar-bar" :style="{ width: dim.score + '%', background: riskColor(selectedProfile.level) }"></div>
              </div>
              <span class="radar-val">{{ dim.score }}</span>
            </div>
          </div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">AI 风险标签</div>
          <div class="tag-cloud">
            <el-tag v-for="t in selectedProfile.tags" :key="t" effect="plain" round class="risk-tag">{{ t }}</el-tag>
          </div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">干预建议</div>
          <div class="suggestions">
            <div v-for="s in selectedProfile.suggestions" :key="s" class="suggestion-item">
              <el-icon class="s-icon"><InfoFilled /></el-icon>
              <span>{{ s }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'

type RiskLevel = 'high' | 'medium' | 'low'

const filterLevel = ref<RiskLevel | ''>('')
const selectedId = ref<number>(1)

const levelLabel: Record<RiskLevel, string> = { high: '高风险', medium: '中风险', low: '低风险' }
const riskColor = (level: RiskLevel) => ({ high: '#ef4444', medium: '#f59e0b', low: '#10b981' }[level])

const riskList = [
  { id: 1, name: '李秀英', age: 78, sex: '女', level: 'high' as RiskLevel, score: 87, tags: ['重点监护', '设备断连', '心率异常'], dims: [{ name: '心脏风险', score: 88 }, { name: '摔倒风险', score: 75 }, { name: '认知风险', score: 60 }, { name: '用药依从', score: 45 }, { name: '社交孤立', score: 70 }], suggestions: ['建议立即联系家属进行情况核实', '开展上门医护回访', '检查设备连接状态并及时更换电池'] },
  { id: 2, name: '王建国', age: 82, sex: '男', level: 'high' as RiskLevel, score: 81, tags: ['高龄对象', '血压偏高', '需回访'], dims: [{ name: '心脏风险', score: 78 }, { name: '摔倒风险', score: 82 }, { name: '认知风险', score: 55 }, { name: '用药依从', score: 60 }, { name: '社交孤立', score: 50 }], suggestions: ['每周定期进行电话随访', '增加血压监测频次', '评估居家安全环境'] },
  { id: 3, name: '赵志强', age: 75, sex: '男', level: 'medium' as RiskLevel, score: 58, tags: ['血糖波动', '建议跟踪'], dims: [{ name: '心脏风险', score: 50 }, { name: '摔倒风险', score: 40 }, { name: '认知风险', score: 35 }, { name: '用药依从', score: 70 }, { name: '社交孤立', score: 30 }], suggestions: ['密切关注血糖变化趋势', '督促规律服药', '建议每月线下复查'] },
  { id: 4, name: '张淑芬', age: 69, sex: '女', level: 'medium' as RiskLevel, score: 52, tags: ['睡眠不佳', '轻度焦虑'], dims: [{ name: '心脏风险', score: 42 }, { name: '摔倒风险', score: 35 }, { name: '认知风险', score: 38 }, { name: '用药依从', score: 80 }, { name: '社交孤立', score: 55 }], suggestions: ['建议进行睡眠质量干预', '引导参与社区活动以改善焦虑'] },
  { id: 5, name: '陈美华', age: 71, sex: '女', level: 'low' as RiskLevel, score: 28, tags: ['状态稳定', '活跃用户'], dims: [{ name: '心脏风险', score: 25 }, { name: '摔倒风险', score: 20 }, { name: '认知风险', score: 15 }, { name: '用药依从', score: 90 }, { name: '社交孤立', score: 10 }], suggestions: ['维持现有监测频率', '鼓励继续保持活跃生活方式'] },
  { id: 6, name: '刘德华', age: 67, sex: '男', level: 'low' as RiskLevel, score: 22, tags: ['低风险', '定期检测'], dims: [{ name: '心脏风险', score: 20 }, { name: '摔倒风险', score: 18 }, { name: '认知风险', score: 12 }, { name: '用药依从', score: 88 }, { name: '社交孤立', score: 15 }], suggestions: ['按计划进行年度健康评估'] },
]

const totalSubjects = riskList.length
const summaryCards = [
  { label: '高风险', count: riskList.filter(r => r.level === 'high').length, cls: 'high', color: '#ef4444' },
  { label: '中风险', count: riskList.filter(r => r.level === 'medium').length, cls: 'medium', color: '#f59e0b' },
  { label: '低风险', count: riskList.filter(r => r.level === 'low').length, cls: 'low', color: '#10b981' },
  { label: '监护总数', count: totalSubjects, cls: 'total', color: '#3b82f6' },
]

const filteredRiskList = computed(() =>
  filterLevel.value ? riskList.filter(r => r.level === filterLevel.value) : riskList
)
const selectedProfile = computed(() => riskList.find(r => r.id === selectedId.value))
</script>

<style scoped lang="scss">
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  border-left: 4px solid transparent;
  &.high { border-left-color: #ef4444; }
  &.medium { border-left-color: #f59e0b; }
  &.low { border-left-color: #10b981; }
  &.total { border-left-color: #3b82f6; }
}
.sc-left { display: flex; flex-direction: column; gap: 4px; }
.sc-num { font-size: 32px; font-weight: 800; color: #0f172a; line-height: 1; }
.sc-label { font-size: 13px; color: #64748b; }
.sc-right { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; width: 80px; }
.sc-bar-bg { width: 100%; height: 4px; background: #f1f5f9; border-radius: 2px; }
.sc-bar { height: 4px; border-radius: 2px; transition: width 0.5s; }
.sc-pct { font-size: 12px; color: #94a3b8; }

.section-row {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.panel-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  overflow: hidden;
}
.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #f1f5f9;
}
.panel-title { font-size: 14px; font-weight: 700; color: #0f172a; }

.risk-list-card { flex: 1; }
.risk-rows { padding: 8px; }
.risk-row {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 12px; border-radius: 10px; cursor: pointer; transition: all 0.2s;
  &:hover { background: #f8fafc; }
  &.active { background: #eff6ff; }
  .el-avatar {
    font-size: 14px; font-weight: 700; color: #fff; flex-shrink: 0;
    &.avatar-high { background: linear-gradient(135deg, #ef4444, #f87171); }
    &.avatar-medium { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
    &.avatar-low { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
  }
}
.rr-rank {
  width: 24px; height: 24px; border-radius: 6px;
  background: #f1f5f9; color: #64748b;
  font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  &.top-1 { background: #fef3c7; color: #b45309; }
  &.top-2 { background: #e2e8f0; color: #475569; }
  &.top-3 { background: #fde8d8; color: #9a3412; }
}
.rr-info { flex: 1; min-width: 0; }
.rr-name { font-size: 14px; font-weight: 600; color: #0f172a; display: block; }
.rr-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 4px; }
.mini-tag { font-size: 11px !important; }
.rr-score-area { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.rr-score {
  font-size: 20px; font-weight: 800;
  &.high { color: #ef4444; }
  &.medium { color: #f59e0b; }
  &.low { color: #10b981; }
}
.risk-badge {
  display: inline-flex; align-items: center;
  padding: 2px 8px; border-radius: 20px; font-size: 12px; font-weight: 600;
  &.risk-high { color: #b91c1c; background: #fef2f2; }
  &.risk-medium { color: #b45309; background: #fffbeb; }
  &.risk-low { color: #047857; background: #ecfdf5; }
  &.lg { padding: 4px 12px; font-size: 13px; }
}

.profile-detail { flex: 0 0 360px; }
.pd-header {
  display: flex; align-items: center; gap: 16px;
  padding: 20px;
  .el-avatar {
    font-size: 22px; font-weight: 700; color: #fff; flex-shrink: 0;
    &.avatar-high { background: linear-gradient(135deg, #ef4444, #f87171); }
    &.avatar-medium { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
    &.avatar-low { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
  }
  h3 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }
  p { margin: 4px 0 0; font-size: 13px; color: #64748b; }
  .pd-name-area { flex: 1; }
}
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
.suggestion-item {
  display: flex; align-items: flex-start; gap: 8px;
  font-size: 13px; color: #475569; line-height: 1.5;
  .s-icon { color: #3b82f6; margin-top: 2px; flex-shrink: 0; }
}

@media (max-width: 1200px) {
  .summary-grid { grid-template-columns: repeat(2, 1fr); }
  .section-row { flex-direction: column; }
  .profile-detail { flex: none; width: 100%; }
}
</style>
