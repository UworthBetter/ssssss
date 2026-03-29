<template>
  <div class="chain-progress">
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
    </div>
    <div class="progress-labels">
      <span class="label">{{ currentStage }}</span>
      <span class="percent digital-font">{{ progressPercent }}%</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Stage {
  name: string
  status: 'completed' | 'processing' | 'pending' | 'failed' | 'skipped'
}

const props = defineProps<{
  stages: Stage[]
}>()

const completedCount = computed(() => {
  return props.stages.filter(s => s.status === 'completed').length
})

const processingCount = computed(() => {
  return props.stages.filter(s => s.status === 'processing').length
})

const progressPercent = computed(() => {
  const total = props.stages.length
  const completed = completedCount.value
  const processing = processingCount.value
  return Math.round(((completed + processing * 0.5) / total) * 100)
})

const currentStage = computed(() => {
  const processing = props.stages.find(s => s.status === 'processing')
  if (processing) return processing.name

  const pending = props.stages.find(s => s.status === 'pending')
  if (pending) return pending.name

  return '已完成'
})
</script>

<style scoped lang="scss">
.chain-progress {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;

  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #0ea5e9, #06b6d4);
    transition: width 0.3s ease;
  }
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;

  .label {
    color: #666;
  }

  .percent {
    color: #0ea5e9;
    font-weight: 600;
  }
}

.digital-font {
  font-family: 'Courier New', monospace;
}
</style>
