<template>
  <div class="context-filter-bar panel">
    <div class="filter-row">
      <div class="filter-chip">
        <span class="chip-label">时间</span>
        <el-select v-model="state.timeRange" size="small" style="width: 124px" @change="emitChange">
          <el-option v-for="option in timeRangeOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </div>

      <div class="filter-chip">
        <span class="chip-label">机构</span>
        <el-select v-model="state.region" size="small" style="width: 138px" @change="emitChange">
          <el-option v-for="option in regionOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </div>

      <div class="filter-chip">
        <span class="chip-label">风险</span>
        <el-select v-model="state.riskLevel" size="small" style="width: 120px" @change="emitChange">
          <el-option v-for="option in riskOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </div>

      <div class="filter-chip">
        <span class="chip-label">状态</span>
        <el-select v-model="state.status" size="small" style="width: 128px" @change="emitChange">
          <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </div>
    </div>

    <div class="action-row">
      <div class="context-summary">
        <span>{{ summaryLabel }}</span>
        <strong>{{ summaryValue }}</strong>
      </div>
      <div class="action-group">
        <el-button size="small" text @click="reset">重置</el-button>
        <el-button size="small" type="primary" @click="confirm">应用筛选</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch } from 'vue'

export interface PlatformContextFilters {
  timeRange: string
  region: string
  riskLevel: string
  status: string
}

interface OptionItem {
  label: string
  value: string
}

interface Props {
  modelValue?: PlatformContextFilters
  summaryLabel?: string
  summaryValue?: string
  timeRangeOptions?: OptionItem[]
  regionOptions?: OptionItem[]
  riskOptions?: OptionItem[]
  statusOptions?: OptionItem[]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({
    timeRange: 'today',
    region: 'all',
    riskLevel: 'all',
    status: 'all'
  }),
  summaryLabel: '当前上下文',
  summaryValue: '全平台',
  timeRangeOptions: () => [
    { label: '今天', value: 'today' },
    { label: '近 7 天', value: '7d' },
    { label: '近 30 天', value: '30d' }
  ],
  regionOptions: () => [
    { label: '全部机构', value: 'all' },
    { label: '东区', value: 'east' },
    { label: '西区', value: 'west' }
  ],
  riskOptions: () => [
    { label: '全部风险', value: 'all' },
    { label: '高风险', value: 'high' },
    { label: '中风险', value: 'medium' },
    { label: '低风险', value: 'low' }
  ],
  statusOptions: () => [
    { label: '全部状态', value: 'all' },
    { label: '待处理', value: 'pending' },
    { label: '处理中', value: 'processing' },
    { label: '已关闭', value: 'closed' }
  ]
})

const emit = defineEmits<{
  'update:modelValue': [value: PlatformContextFilters]
  change: [value: PlatformContextFilters]
  reset: []
  confirm: [value: PlatformContextFilters]
}>()

const state = reactive<PlatformContextFilters>({
  timeRange: props.modelValue.timeRange,
  region: props.modelValue.region,
  riskLevel: props.modelValue.riskLevel,
  status: props.modelValue.status
})

watch(
  () => props.modelValue,
  (value) => {
    state.timeRange = value.timeRange
    state.region = value.region
    state.riskLevel = value.riskLevel
    state.status = value.status
  },
  { deep: true }
)

const snapshot = () => ({
  timeRange: state.timeRange,
  region: state.region,
  riskLevel: state.riskLevel,
  status: state.status
})

const emitChange = () => {
  const value = snapshot()
  emit('update:modelValue', value)
  emit('change', value)
}

const reset = () => {
  state.timeRange = props.modelValue.timeRange
  state.region = props.modelValue.region
  state.riskLevel = props.modelValue.riskLevel
  state.status = props.modelValue.status
  emit('reset')
  emitChange()
}

const confirm = () => {
  const value = snapshot()
  emit('confirm', value)
}

const summaryLabel = computed(() => props.summaryLabel)
const summaryValue = computed(() => props.summaryValue)
</script>

<style scoped lang="scss">
.context-filter-bar {
  width: 100%;
  padding: 14px;
  border-radius: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(221, 227, 233, 0.84);
}

.chip-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-sub);
  white-space: nowrap;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.context-summary {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;

  span {
    font-size: 12px;
    color: var(--text-sub);
  }

  strong {
    font-size: 14px;
    color: var(--text-main);
  }
}

.action-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

@media (max-width: 960px) {
  .context-filter-bar {
    align-items: stretch;
  }

  .action-row {
    width: 100%;
    margin-left: 0;
    justify-content: space-between;
  }
}
</style>
