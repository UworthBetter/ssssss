<template>
  <PlatformPageShellV2
    title="健康报告"
    subtitle="按对象或组织维度生成阶段性健康状况汇总，支持导出打印，辅助家属与医护人员了解监护情况。"
    eyebrow="HEALTH REPORT"
    status-note="演示版 · 当前使用模拟数据"
    status-tone="warning"
  >
    <template #headerExtra>
      <div class="header-actions">
        <el-button :icon="Printer" plain>打印报告</el-button>
        <el-button type="primary" :icon="Download">导出 PDF</el-button>
      </div>
    </template>

    <template #toolbar>
      <div class="toolbar-row">
        <el-input v-model="searchName" placeholder="搜索对象姓名..." prefix-icon="Search" clearable style="width: 220px" />
        <el-select v-model="filterLevel" placeholder="风险等级" clearable style="width: 140px">
          <el-option label="高关注" value="high" />
          <el-option label="中关注" value="medium" />
          <el-option label="低关注" value="low" />
        </el-select>
        <el-date-picker v-model="reportDate" type="month" placeholder="报告月份" style="width: 160px" />
        <el-button type="primary" @click="">生成报告</el-button>
      </div>
    </template>

    <!-- 报告卡片 -->
    <div class="report-grid">
      <div v-for="sub in filteredSubjects" :key="sub.id" class="report-card">
        <!-- 头部 -->
        <div class="rp-header">
          <el-avatar :size="52" :class="'avatar-' + sub.level">{{ sub.name.charAt(0) }}</el-avatar>
          <div class="rp-basic">
            <h3 class="rp-name">{{ sub.name }}</h3>
            <p class="rp-meta">{{ sub.age }}岁 · {{ sub.sex }} · {{ sub.period }}</p>
          </div>
          <div class="rp-level" :class="'level-' + sub.level">{{ levelLabel[sub.level] }}</div>
        </div>

        <!-- 体征摘要 -->
        <div class="rp-vitals">
          <div v-for="v in sub.vitals" :key="v.label" class="vital-item">
            <span class="vi-label">{{ v.label }}</span>
            <span class="vi-val" :class="v.warn ? 'warn' : ''">{{ v.value }}</span>
            <span class="vi-status" :class="v.warn ? 'bad' : 'good'">{{ v.warn ? '↑偏高' : '正常' }}</span>
          </div>
        </div>

        <!-- 关键事件 -->
        <div class="rp-events" v-if="sub.events.length">
          <div class="rp-section-title">关键事件 ({{ sub.events.length }})</div>
          <div v-for="ev in sub.events" :key="ev" class="event-item">
            <div class="ev-dot" :class="sub.level"></div>
            {{ ev }}
          </div>
        </div>

        <!-- 健康得分 -->
        <div class="rp-score-section">
          <div class="rp-section-title">本月健康评分</div>
          <div class="score-bar-area">
            <div class="score-track">
              <div class="score-fill" :style="{ width: sub.score + '%', background: scoreColor(sub.score) }"></div>
            </div>
            <span class="score-num" :style="{ color: scoreColor(sub.score) }">{{ sub.score }}</span>
          </div>
        </div>

        <!-- 医护建议 -->
        <div class="rp-suggestion">
          <el-icon class="sg-icon"><ChatLineSquare /></el-icon>
          <span>{{ sub.suggestion }}</span>
        </div>

        <div class="rp-footer">
          <el-button size="small" plain :icon="Download">导出此报告</el-button>
          <el-button size="small" plain>查看详情</el-button>
        </div>
      </div>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Download, Printer, ChatLineSquare } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'

type RiskLevel = 'high' | 'medium' | 'low'

const searchName = ref('')
const filterLevel = ref<RiskLevel | ''>('')
const reportDate  = ref(null)

const levelLabel: Record<RiskLevel, string> = { high: '高关注', medium: '中关注', low: '低关注' }
const scoreColor  = (s: number) => s >= 80 ? '#10b981' : s >= 60 ? '#f59e0b' : '#ef4444'

const subjects = [
  {
    id: 1, name: '李秀英', age: 78, sex: '女', level: 'high' as RiskLevel, period: '2026年3月',
    vitals: [
      { label: '平均心率', value: '96 bpm', warn: true },
      { label: '平均血氧', value: '95.2%', warn: true },
      { label: '平均体温', value: '36.8℃', warn: false },
    ],
    events: ['03/15 心率过速告警（108bpm）', '03/20 血氧偏低警报（93%）', '03/22 设备断连超 2h'],
    score: 52,
    suggestion: '建议增加本月医护回访频次，重点关注心率与血氧波动，协调家属共同监护。'
  },
  {
    id: 2, name: '王建国', age: 82, sex: '男', level: 'high' as RiskLevel, period: '2026年3月',
    vitals: [
      { label: '平均心率', value: '78 bpm', warn: false },
      { label: '平均血氧', value: '96.1%', warn: false },
      { label: '平均体温', value: '36.6℃', warn: false },
    ],
    events: ['03/18 设备离线超 3h（电池耗尽）', '03/21 综合风险评分升至 81'],
    score: 64,
    suggestion: '设备维护需加强，建议每月更换一次电池。综合风险评分较高，保持跟踪。'
  },
  {
    id: 3, name: '赵志强', age: 75, sex: '男', level: 'medium' as RiskLevel, period: '2026年3月',
    vitals: [
      { label: '平均心率', value: '72 bpm', warn: false },
      { label: '平均血氧', value: '97.3%', warn: false },
      { label: '平均体温', value: '36.5℃', warn: false },
    ],
    events: ['03/12 睡眠质量下降（深度睡眠 < 15%）'],
    score: 75,
    suggestion: '整体状态稳定，睡眠质量需关注，建议调整作息并减少夜间噪音干扰。'
  },
  {
    id: 4, name: '陈美华', age: 71, sex: '女', level: 'low' as RiskLevel, period: '2026年3月',
    vitals: [
      { label: '平均心率', value: '68 bpm', warn: false },
      { label: '平均血氧', value: '98.2%', warn: false },
      { label: '平均步数', value: '5,240 步', warn: false },
    ],
    events: [],
    score: 92,
    suggestion: '本月各项指标优秀，保持现有活跃生活状态，继续按时检测即可。'
  },
]

const filteredSubjects = computed(() => {
  let res = subjects
  if (filterLevel.value) res = res.filter(s => s.level === filterLevel.value)
  if (searchName.value) res = res.filter(s => s.name.includes(searchName.value))
  return res
})
</script>

<style scoped lang="scss">
.header-actions { display: flex; gap: 10px; }
.toolbar-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }

.report-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 20px;
}

.report-card {
  background: #fff; border-radius: 18px; padding: 24px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
  display: flex; flex-direction: column; gap: 18px;
  transition: all 0.2s;
  &:hover { box-shadow: 0 8px 28px rgba(0,0,0,0.08); transform: translateY(-2px); }
}

.rp-header { display: flex; align-items: center; gap: 14px; }
.el-avatar {
  font-size: 20px; font-weight: 700; color: #fff; flex-shrink: 0;
  &.avatar-high   { background: linear-gradient(135deg, #ef4444, #f87171); }
  &.avatar-medium { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
  &.avatar-low    { background: linear-gradient(135deg, #10b981, #34d399); }
}
.rp-basic { flex: 1; }
.rp-name { margin: 0; font-size: 18px; font-weight: 800; color: #0f172a; }
.rp-meta { margin: 4px 0 0; font-size: 13px; color: #64748b; }
.rp-level {
  padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 700;
  &.level-high   { color: #b91c1c; background: #fef2f2; }
  &.level-medium { color: #b45309; background: #fffbeb; }
  &.level-low    { color: #047857; background: #ecfdf5; }
}

.rp-vitals {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 0;
  background: #f8fafc; border-radius: 12px; overflow: hidden;
  border: 1px solid #f1f5f9;
}
.vital-item {
  display: flex; flex-direction: column; align-items: center;
  padding: 12px 8px; gap: 3px; border-right: 1px solid #f1f5f9;
  &:last-child { border-right: none; }
}
.vi-label { font-size: 11px; color: #94a3b8; }
.vi-val { font-size: 15px; font-weight: 700; color: #0f172a; &.warn { color: #ef4444; } }
.vi-status {
  font-size: 11px; font-weight: 600;
  &.good { color: #10b981; }
  &.bad  { color: #f59e0b; }
}

.rp-section-title { font-size: 12px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; }

.rp-events { display: flex; flex-direction: column; gap: 8px; }
.event-item {
  display: flex; align-items: center; gap: 10px;
  font-size: 13px; color: #475569;
}
.ev-dot {
  width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0;
  &.high   { background: #ef4444; }
  &.medium { background: #f59e0b; }
  &.low    { background: #10b981; }
}

.score-bar-area { display: flex; align-items: center; gap: 12px; }
.score-track { flex: 1; height: 8px; background: #f1f5f9; border-radius: 4px; }
.score-fill { height: 8px; border-radius: 4px; transition: width 0.5s; }
.score-num { font-size: 22px; font-weight: 800; width: 36px; text-align: right; }

.rp-suggestion {
  display: flex; gap: 10px; align-items: flex-start;
  background: #f8fafc; border-radius: 10px; padding: 12px;
  font-size: 13px; color: #475569; line-height: 1.6;
  .sg-icon { color: #3b82f6; margin-top: 2px; flex-shrink: 0; }
}

.rp-footer { display: flex; gap: 10px; border-top: 1px solid #f1f5f9; padding-top: 14px; }
</style>
