<template>
  <PlatformPageShellV2
    title="模型日志"
    subtitle="记录所有 AI 推理任务的输入参数、判定结果、置信度和触发链路，支持模型行为溯源与审计。"
    eyebrow="AI MODEL LOGS"
    status-note="演示版 · 当前使用模拟数据"
    status-tone="warning"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索对象 / 事件 ID..." prefix-icon="Search" clearable style="width: 240px" />
        <el-select v-model="query.result" placeholder="判定结果" clearable style="width: 140px">
          <el-option label="异常告警" value="alert" />
          <el-option label="风险预警" value="warning" />
          <el-option label="状态正常" value="normal" />
        </el-select>
        <el-select v-model="query.model" placeholder="模型名称" clearable style="width: 180px">
          <el-option label="心率异常检测" value="hr" />
          <el-option label="跌倒行为识别" value="fall" />
          <el-option label="睡眠质量分析" value="sleep" />
          <el-option label="综合风险评估" value="risk" />
        </el-select>
        <el-date-picker v-model="query.date" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 240px" />
        <el-button type="primary" @click="">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </template>

    <!-- 汇总卡片 -->
    <div class="stat-row">
      <div v-for="s in statCards" :key="s.label" class="stat-card">
        <div class="sc-icon" :style="{ background: s.bg }">
          <el-icon :size="18"><component :is="s.icon" /></el-icon>
        </div>
        <div class="sc-body">
          <span class="sc-value" :style="{ color: s.color }">{{ s.value }}</span>
          <span class="sc-label">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <!-- 日志列表 -->
    <div class="panel-card">
      <div class="panel-header">
        <span class="panel-title">推理日志流</span>
        <div class="header-right">
          <span class="live-indicator">
            <span class="live-dot"></span>LIVE
          </span>
          <el-button size="small" :icon="Download" plain>导出日志</el-button>
        </div>
      </div>

      <div class="log-stream">
        <div v-for="log in filteredLogs" :key="log.id" class="log-entry" :class="log.result">
          <!-- 时间轴点 -->
          <div class="log-timeline">
            <div class="tl-dot" :class="log.result"></div>
            <div class="tl-line"></div>
          </div>

          <!-- 内容区 -->
          <div class="log-content">
            <div class="log-header">
              <span class="log-id">#{{ log.id.toString().padStart(6, '0') }}</span>
              <div class="log-result-badge" :class="log.result">{{ resultLabel[log.result] }}</div>
              <span class="log-model-tag">{{ log.model }}</span>
              <span class="log-time">{{ log.time }}</span>
            </div>
            <div class="log-subject">
              <el-icon :size="12"><User /></el-icon>
              {{ log.subject }}
              <span class="log-device">· 设备 {{ log.device }}</span>
            </div>
            <div class="log-detail">{{ log.detail }}</div>
            <div class="log-metrics">
              <div class="metric-pill" v-for="m in log.metrics" :key="m.key">
                <span class="mp-key">{{ m.key }}</span>
                <span class="mp-val" :class="m.warn ? 'warn' : ''">{{ m.val }}</span>
              </div>
              <div class="confidence-bar">
                <span class="cb-label">置信度</span>
                <div class="cb-bg">
                  <div class="cb-fill" :style="{ width: log.confidence + '%', background: confidenceColor(log.confidence) }"></div>
                </div>
                <span class="cb-val">{{ log.confidence }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="log-pagination">
        <el-pagination v-model:current-page="page" :page-size="10" :total="logs.length" background layout="total, prev, pager, next" />
      </div>
    </div>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Download, User } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'

type LogResult = 'alert' | 'warning' | 'normal'

const page = ref(1)
const query = reactive({ keyword: '', result: '', model: '', date: null as any })

const resultLabel: Record<LogResult, string> = { alert: '异常告警', warning: '风险预警', normal: '状态正常' }
const confidenceColor = (v: number) => v >= 85 ? '#10b981' : v >= 65 ? '#f59e0b' : '#ef4444'

const logs = ref([
  { id: 100421, result: 'alert' as LogResult, model: '心率异常检测', subject: '李秀英 (ID:1)', device: 'A01', time: '2026-03-23 17:42:05', detail: '检测到连续心率超出阈值（> 100 bpm），持续时长 5 分钟，触发立即告警。', metrics: [{ key: '心率', val: '108 bpm', warn: true }, { key: '血氧', val: '95%', warn: false }], confidence: 93 },
  { id: 100420, result: 'warning' as LogResult, model: '综合风险评估', subject: '王建国 (ID:2)', device: 'A02', time: '2026-03-23 16:18:33', detail: '设备长期离线超 2 小时，结合当前风险等级，触发风险预警。', metrics: [{ key: '离线时长', val: '2h 15m', warn: true }, { key: '风险分', val: '81', warn: true }], confidence: 78 },
  { id: 100419, result: 'normal' as LogResult, model: '睡眠质量分析', subject: '陈美华 (ID:5)', device: 'C01', time: '2026-03-23 08:30:00', detail: '昨夜睡眠检测完成，各项指标处于正常区间，深度睡眠占比 22%。', metrics: [{ key: '睡眠总时长', val: '7.2h', warn: false }, { key: '深度睡眠', val: '22%', warn: false }], confidence: 96 },
  { id: 100418, result: 'warning' as LogResult, model: '心率异常检测', subject: '张淑芬 (ID:3)', device: 'B01', time: '2026-03-23 10:12:47', detail: '心率传感器检测数据出现间歇性丢帧，可能存在设备接触问题。', metrics: [{ key: '数据完整率', val: '74%', warn: true }, { key: '心率', val: '82 bpm', warn: false }], confidence: 62 },
  { id: 100417, result: 'normal' as LogResult, model: '跌倒行为识别', subject: '赵志强 (ID:4)', device: 'A03', time: '2026-03-23 09:05:22', detail: '加速度传感器数据正常，未检测到跌倒或剧烈运动异常行为。', metrics: [{ key: '运动强度', val: '低', warn: false }, { key: '步行步数', val: '1,280', warn: false }], confidence: 98 },
  { id: 100416, result: 'alert' as LogResult, model: '综合风险评估', subject: '李秀英 (ID:1)', device: 'D01', time: '2026-03-23 07:50:11', detail: '血压仪检测收缩压达到 158 mmHg，超过高血压预警阈值，已生成事件。', metrics: [{ key: '收缩压', val: '158 mmHg', warn: true }, { key: '舒张压', val: '95 mmHg', warn: true }], confidence: 91 },
])

const filteredLogs = computed(() => {
  let res = logs.value
  if (query.result) res = res.filter(l => l.result === query.result)
  if (query.keyword) res = res.filter(l => l.subject.includes(query.keyword) || String(l.id).includes(query.keyword))
  if (query.model) res = res.filter(l => l.model === ({ hr: '心率异常检测', fall: '跌倒行为识别', sleep: '睡眠质量分析', risk: '综合风险评估' } as any)[query.model])
  return res
})

const statCards = [
  { label: '今日推理次数', value: '1,248', color: '#3b82f6', bg: 'linear-gradient(135deg,#eff6ff,#dbeafe)', icon: 'DataLine' },
  { label: '告警触发', value: logs.value.filter(l => l.result === 'alert').length, color: '#ef4444', bg: 'linear-gradient(135deg,#fef2f2,#fecaca)', icon: 'Bell' },
  { label: '风险预警', value: logs.value.filter(l => l.result === 'warning').length, color: '#f59e0b', bg: 'linear-gradient(135deg,#fffbeb,#fde68a)', icon: 'Warning' },
  { label: '平均置信度', value: '82.3%', color: '#10b981', bg: 'linear-gradient(135deg,#f0fdf4,#bbf7d0)', icon: 'TrendCharts' },
]

const handleReset = () => { query.keyword = ''; query.result = ''; query.model = ''; query.date = null }
</script>

<style scoped lang="scss">
.toolbar-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff; border-radius: 14px; padding: 16px 18px;
  display: flex; align-items: center; gap: 14px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.sc-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #64748b; flex-shrink: 0;
}
.sc-body { display: flex; flex-direction: column; gap: 2px; }
.sc-value { font-size: 24px; font-weight: 800; line-height: 1.1; }
.sc-label { font-size: 12px; color: #64748b; }

.panel-card {
  background: #fff; border-radius: 16px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); overflow: hidden;
}
.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #f1f5f9;
}
.panel-title { font-size: 14px; font-weight: 700; color: #0f172a; }
.header-right { display: flex; align-items: center; gap: 12px; }

.live-indicator {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 11px; font-weight: 700; color: #10b981; letter-spacing: 0.05em;
}
.live-dot {
  width: 7px; height: 7px; border-radius: 50%; background: #10b981;
  animation: pulse 1.5s infinite;
}
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }

/* 日志流 */
.log-stream { padding: 16px 20px; display: flex; flex-direction: column; gap: 0; }

.log-entry {
  display: flex; gap: 0; position: relative;
  &:last-child .tl-line { display: none; }
}

.log-timeline {
  flex: 0 0 28px; display: flex; flex-direction: column; align-items: center;
}
.tl-dot {
  width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; margin-top: 4px;
  &.alert { background: #ef4444; box-shadow: 0 0 6px rgba(239,68,68,0.4); }
  &.warning { background: #f59e0b; }
  &.normal { background: #10b981; }
}
.tl-line { flex: 1; width: 1px; background: #f1f5f9; min-height: 16px; }

.log-content {
  flex: 1; padding: 0 0 20px 12px;
  display: flex; flex-direction: column; gap: 6px;
}

.log-header {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
}
.log-id { font-family: monospace; font-size: 12px; color: #94a3b8; }
.log-result-badge {
  display: inline-flex; align-items: center; padding: 2px 10px;
  border-radius: 20px; font-size: 12px; font-weight: 600;
  &.alert { color: #b91c1c; background: #fef2f2; }
  &.warning { color: #b45309; background: #fffbeb; }
  &.normal { color: #047857; background: #ecfdf5; }
}
.log-model-tag {
  font-size: 12px; color: #7c3aed; background: #f3e8ff;
  padding: 2px 8px; border-radius: 6px;
}
.log-time { font-size: 12px; color: #94a3b8; margin-left: auto; }

.log-subject {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; font-weight: 600; color: #475569;
  .log-device { font-weight: 400; color: #94a3b8; }
}

.log-detail { font-size: 13px; color: #334155; line-height: 1.6; }

.log-metrics {
  display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-top: 4px;
}
.metric-pill {
  display: flex; align-items: center; gap: 5px;
  background: #f8fafc; border: 1px solid #e2e8f0;
  padding: 3px 10px; border-radius: 20px; font-size: 12px;
  .mp-key { color: #64748b; }
  .mp-val { font-weight: 600; color: #0f172a; &.warn { color: #ef4444; } }
}

.confidence-bar {
  display: flex; align-items: center; gap: 8px; font-size: 12px;
  .cb-label { color: #94a3b8; flex-shrink: 0; }
  .cb-bg { width: 80px; height: 4px; background: #f1f5f9; border-radius: 2px; }
  .cb-fill { height: 4px; border-radius: 2px; transition: width 0.4s; }
  .cb-val { font-weight: 600; color: #475569; }
}

.log-pagination { padding: 16px 20px; border-top: 1px solid #f1f5f9; display: flex; justify-content: flex-end; }

@media (max-width: 1024px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
