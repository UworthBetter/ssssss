<template>
  <PlatformPageShellV2
    title="仿真设备控制台"
    subtitle="管理员可在这里控制仿真设备数据的开始、终止与单次触发，快速驱动 dashboard 与设备中心产生实时数据。"
    eyebrow="DEVICE SIMULATOR"
    :status-note="statusNote"
    :status-tone="statusTone"
    aside-title="运行摘要"
  >
    <template #headerExtra>
      <el-space wrap>
        <el-tag type="danger" effect="dark" round>管理员专用</el-tag>
        <el-button :icon="Refresh" plain @click="loadStatus" :loading="loading">刷新状态</el-button>
      </el-space>
    </template>

    <div class="sim-page">
      <section class="hero-panel">
        <div class="hero-copy">
          <p class="hero-kicker">Live Control</p>
          <h2>一键驱动仿真设备开始上报</h2>
          <p>
            当前控制台直接接入后端仿真设备引擎。开始后会持续生成健康指标、位置和异常数据，停止后则暂停新的仿真上报。
          </p>
        </div>
        <div class="hero-pulse" :class="{ active: simulatorStatus?.effectiveEnabled }">
          <span class="pulse-core"></span>
          <span class="pulse-ring ring-one"></span>
          <span class="pulse-ring ring-two"></span>
          <strong>{{ simulatorStatus?.effectiveEnabled ? 'RUNNING' : 'PAUSED' }}</strong>
        </div>
      </section>

      <section class="metrics-grid">
        <article class="metric-card blue">
          <span class="metric-label">有效设备数</span>
          <strong class="metric-value">{{ simulatorStatus?.effectiveSubjectCount ?? '--' }}</strong>
          <span class="metric-sub">当前参与仿真的对象数</span>
        </article>
        <article class="metric-card green">
          <span class="metric-label">累计 Tick</span>
          <strong class="metric-value">{{ simulatorStatus?.tickCounter ?? '--' }}</strong>
          <span class="metric-sub">已生成的仿真批次数</span>
        </article>
        <article class="metric-card amber">
          <span class="metric-label">档案缓存</span>
          <strong class="metric-value">{{ simulatorStatus?.profileCacheSize ?? '--' }}</strong>
          <span class="metric-sub">当前保留的模拟对象</span>
        </article>
        <article class="metric-card slate">
          <span class="metric-label">运行模式</span>
          <strong class="metric-value metric-text">{{ modeLabel }}</strong>
          <span class="metric-sub">配置模式 / 运行时覆盖</span>
        </article>
      </section>

      <section class="control-grid">
        <article class="control-card primary">
          <div class="card-head">
            <div>
              <p class="card-kicker">Start / Stop</p>
              <h3>运行控制</h3>
            </div>
            <el-tag :type="simulatorStatus?.running ? 'warning' : 'success'" effect="light" round>
              {{ simulatorStatus?.running ? '处理中' : '可操作' }}
            </el-tag>
          </div>
          <p class="card-desc">开始后会按后端定时节奏持续产出仿真数据，停止后保留现有数据但不再新增。</p>
          <div class="action-row">
            <el-button
              type="primary"
              size="large"
              :loading="submitting === 'start'"
              :disabled="simulatorStatus?.running"
              @click="handleStart"
            >
              开始产生数据
            </el-button>
            <el-button
              size="large"
              :loading="submitting === 'stop'"
              :disabled="!simulatorStatus?.effectiveEnabled && !simulatorStatus?.runtimeEnabledOverride"
              @click="handleStop"
            >
              终止产生数据
            </el-button>
            <el-button
              size="large"
              plain
              :loading="submitting === 'reset'"
              @click="handleReset"
            >
              恢复配置模式
            </el-button>
          </div>
        </article>

        <article class="control-card">
          <div class="card-head">
            <div>
              <p class="card-kicker">Subjects</p>
              <h3>仿真规模</h3>
            </div>
            <span class="mini-note">上限 {{ simulatorStatus?.maxSupportedSubjectCount ?? '--' }}</span>
          </div>
          <p class="card-desc">调整当前参与仿真的对象数量。开始仿真和单次触发都会优先使用这里的数量。</p>
          <div class="subject-editor">
            <el-input-number
              v-model="subjectCount"
              :min="1"
              :max="maxSubjectCount"
              :step="1"
              controls-position="right"
              size="large"
            />
            <el-button type="primary" plain :loading="submitting === 'subject'" @click="handleSubjectCountUpdate">
              应用数量
            </el-button>
          </div>
        </article>

        <article class="control-card accent">
          <div class="card-head">
            <div>
              <p class="card-kicker">Manual Tick</p>
              <h3>单次注入</h3>
            </div>
            <el-tag type="info" effect="light" round>即时触发</el-tag>
          </div>
          <p class="card-desc">不等待定时任务，立即生成一批仿真数据，适合演示和联调时手动触发页面变化。</p>
          <div class="action-row">
            <el-button type="warning" size="large" :loading="submitting === 'tick'" @click="handleTick">
              立即生成一轮数据
            </el-button>
          </div>
        </article>
      </section>
    </div>

    <template #aside>
      <div class="summary-stack">
        <div class="summary-item">
          <span>当前状态</span>
          <strong>{{ simulatorStatus?.effectiveEnabled ? '运行中' : '已暂停' }}</strong>
        </div>
        <div class="summary-item">
          <span>配置启用</span>
          <strong>{{ yesNo(simulatorStatus?.configuredEnabled) }}</strong>
        </div>
        <div class="summary-item">
          <span>运行时覆盖</span>
          <strong>{{ overrideLabel }}</strong>
        </div>
        <div class="summary-item">
          <span>数据集初始化</span>
          <strong>{{ yesNo(simulatorStatus?.datasetInitialized) }}</strong>
        </div>
        <div class="summary-item">
          <span>旧数据清理</span>
          <strong>{{ yesNo(simulatorStatus?.legacyDataSanitized) }}</strong>
        </div>
        <div class="summary-item">
          <span>最近桶位</span>
          <strong>{{ simulatorStatus?.lastGeneratedBucket ?? '--' }}</strong>
        </div>
      </div>
      <div class="aside-tip">
        <h4>使用建议</h4>
        <p>演示时可以先点击“开始产生数据”，等待 1 到 2 个周期后观察 dashboard 与设备状态变化。</p>
        <p>如果需要立刻看到效果，再补一次“立即生成一轮数据”会更直接。</p>
      </div>
    </template>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { PlatformPageShellV2 } from '@/components/platform'
import {
  getDeviceSimulatorStatus,
  resetDeviceSimulator,
  startDeviceSimulator,
  stopDeviceSimulator,
  triggerDeviceSimulatorTick,
  updateDeviceSimulatorSubjectCount,
  type DeviceSimulatorStatus
} from '@/api/demo'

type ActionKey = '' | 'start' | 'stop' | 'reset' | 'tick' | 'subject'

const loading = ref(false)
const submitting = ref<ActionKey>('')
const simulatorStatus = ref<DeviceSimulatorStatus | null>(null)
const subjectCount = ref(1)
let timer: ReturnType<typeof setInterval> | null = null

const maxSubjectCount = computed(() => Math.max(1, simulatorStatus.value?.maxSupportedSubjectCount ?? 1))

const statusNote = computed(() =>
  simulatorStatus.value?.effectiveEnabled ? '仿真设备正在持续产生数据' : '仿真设备当前处于暂停状态'
)

const statusTone = computed(() => (simulatorStatus.value?.effectiveEnabled ? 'success' : 'info'))

const modeLabel = computed(() => {
  if (!simulatorStatus.value) {
    return '--'
  }
  return simulatorStatus.value.runtimeEnabledOverride === null ? '配置模式' : '运行时覆盖'
})

const overrideLabel = computed(() => {
  const override = simulatorStatus.value?.runtimeEnabledOverride
  if (override === null || override === undefined) {
    return '未覆盖'
  }
  return override ? '强制开启' : '强制关闭'
})

const yesNo = (value?: boolean | null) => (value ? '是' : '否')

const syncSubjectCount = () => {
  const current = simulatorStatus.value?.runtimeSubjectCountOverride ?? simulatorStatus.value?.effectiveSubjectCount
  if (current) {
    subjectCount.value = Math.min(maxSubjectCount.value, Math.max(1, current))
  }
}

const loadStatus = async (silent = false) => {
  if (!silent) {
    loading.value = true
  }
  try {
    const res = await getDeviceSimulatorStatus()
    simulatorStatus.value = (res.data || null) as DeviceSimulatorStatus | null
    syncSubjectCount()
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

const runAction = async (action: ActionKey, runner: () => Promise<unknown>, successMessage: string) => {
  submitting.value = action
  try {
    await runner()
    ElMessage.success(successMessage)
    await loadStatus(true)
  } finally {
    submitting.value = ''
  }
}

const normalizedSubjectCount = () => Math.min(maxSubjectCount.value, Math.max(1, Number(subjectCount.value || 1)))

const handleStart = () =>
  runAction(
    'start',
    () => startDeviceSimulator(normalizedSubjectCount()),
    '仿真设备已开始产生数据'
  )

const handleStop = () =>
  runAction(
    'stop',
    () => stopDeviceSimulator(),
    '仿真设备已停止产生新数据'
  )

const handleReset = () =>
  runAction(
    'reset',
    () => resetDeviceSimulator(),
    '已恢复为配置模式'
  )

const handleTick = () =>
  runAction(
    'tick',
    () => triggerDeviceSimulatorTick(normalizedSubjectCount()),
    '已手动生成一轮仿真数据'
  )

const handleSubjectCountUpdate = () => {
  subjectCount.value = normalizedSubjectCount()
  return runAction(
    'subject',
    () => updateDeviceSimulatorSubjectCount(subjectCount.value),
    '仿真对象数量已更新'
  )
}

watch(maxSubjectCount, () => {
  subjectCount.value = normalizedSubjectCount()
})

onMounted(async () => {
  await loadStatus()
  timer = setInterval(() => {
    loadStatus(true)
  }, 15000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
.sim-page {
  display: grid;
  gap: 22px;
}

.hero-panel {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) 220px;
  gap: 24px;
  padding: 30px 32px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(16, 185, 129, 0.18), transparent 30%),
    radial-gradient(circle at bottom left, rgba(59, 130, 246, 0.18), transparent 34%),
    linear-gradient(135deg, #0f172a, #16324f 58%, #0f766e);
  color: #f8fafc;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.2);
}

.hero-panel::after {
  content: '';
  position: absolute;
  inset: auto -80px -120px auto;
  width: 240px;
  height: 240px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  filter: blur(2px);
}

.hero-copy {
  position: relative;
  z-index: 1;

  h2 {
    margin: 10px 0 12px;
    font-size: 34px;
    line-height: 1.08;
    letter-spacing: -0.03em;
  }

  p {
    margin: 0;
    max-width: 720px;
    color: rgba(241, 245, 249, 0.82);
    line-height: 1.7;
  }
}

.hero-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: rgba(148, 163, 184, 0.92);
}

.hero-pulse {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  min-height: 180px;
  color: #dbeafe;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.pulse-core {
  width: 74px;
  height: 74px;
  border-radius: 999px;
  background: linear-gradient(135deg, #34d399, #38bdf8);
  box-shadow: 0 0 0 10px rgba(255, 255, 255, 0.08), 0 18px 30px rgba(52, 211, 153, 0.26);
}

.pulse-ring {
  position: absolute;
  border: 1px solid rgba(125, 211, 252, 0.45);
  border-radius: 999px;
  opacity: 0;
}

.hero-pulse.active .pulse-ring {
  animation: pulseRing 2.4s ease-out infinite;
}

.hero-pulse.active .ring-two {
  animation-delay: 1.1s;
}

.hero-pulse strong {
  position: absolute;
  bottom: 16px;
}

.ring-one {
  width: 120px;
  height: 120px;
}

.ring-two {
  width: 164px;
  height: 164px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  position: relative;
  overflow: hidden;
  padding: 20px 22px;
  border-radius: 22px;
  color: #0f172a;
  min-height: 132px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-card::before {
  content: '';
  position: absolute;
  inset: auto -18px -28px auto;
  width: 96px;
  height: 96px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
}

.metric-card.blue {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
}

.metric-card.green {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
}

.metric-card.amber {
  background: linear-gradient(135deg, #fffbeb, #fde68a);
}

.metric-card.slate {
  background: linear-gradient(135deg, #f8fafc, #e2e8f0);
}

.metric-label,
.metric-sub {
  position: relative;
  z-index: 1;
}

.metric-label {
  font-size: 13px;
  color: #475569;
}

.metric-value {
  position: relative;
  z-index: 1;
  font-size: 38px;
  line-height: 1;
  letter-spacing: -0.04em;
}

.metric-text {
  font-size: 30px;
}

.metric-sub {
  color: #64748b;
  font-size: 12px;
}

.control-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  align-items: stretch;
}

.control-card {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.85);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.05);
  padding: 24px;
  display: grid;
  gap: 18px;
}

.control-card.primary {
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.08), transparent 34%),
    #ffffff;
}

.control-card.accent {
  background:
    radial-gradient(circle at top right, rgba(245, 158, 11, 0.12), transparent 34%),
    #ffffff;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.card-kicker {
  margin: 0 0 6px;
  color: #3b82f6;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.card-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
  letter-spacing: -0.02em;
}

.card-desc {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
  font-size: 14px;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.subject-editor {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.mini-note {
  color: #64748b;
  font-size: 12px;
  padding-top: 4px;
}

.summary-stack {
  display: grid;
  gap: 12px;
}

.summary-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border: 1px solid rgba(226, 232, 240, 0.8);

  span {
    color: #64748b;
    font-size: 13px;
  }

  strong {
    color: #0f172a;
    font-size: 14px;
  }
}

.aside-tip {
  margin-top: 20px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.08), rgba(59, 130, 246, 0.08));
  border: 1px solid rgba(125, 211, 252, 0.22);

  h4 {
    margin: 0 0 10px;
    color: #0f172a;
    font-size: 15px;
  }

  p {
    margin: 0;
    color: #475569;
    line-height: 1.7;
    font-size: 13px;
  }

  p + p {
    margin-top: 8px;
  }
}

@keyframes pulseRing {
  0% {
    opacity: 0;
    transform: scale(0.76);
  }
  20% {
    opacity: 0.9;
  }
  100% {
    opacity: 0;
    transform: scale(1.16);
  }
}

@media (max-width: 1200px) {
  .metrics-grid,
  .control-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .hero-panel,
  .metrics-grid,
  .control-grid {
    grid-template-columns: 1fr;
  }

  .hero-pulse {
    min-height: 140px;
  }
}

@media (max-width: 640px) {
  .hero-panel,
  .control-card,
  .metric-card {
    padding: 20px;
    border-radius: 20px;
  }

  .hero-copy h2 {
    font-size: 28px;
  }

  .metric-value {
    font-size: 30px;
  }

  .metric-text {
    font-size: 24px;
  }

  .subject-editor,
  .action-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
