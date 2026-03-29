<template>
  <el-dialog
    v-model="visible"
    title="处理链详情"
    width="70%"
    class="processing-chain-dialog"
  >
    <div class="chain-container">
      <!-- 基本信息 -->
      <div class="chain-header">
        <div class="header-item">
          <span class="label">患者:</span>
          <span class="value">{{ data.nickName }}</span>
        </div>
        <div class="header-item">
          <span class="label">异常类型:</span>
          <span class="value">{{ data.type }}</span>
        </div>
        <div class="header-item">
          <span class="label">事件ID:</span>
          <span class="value digital-font">{{ data.id }}</span>
        </div>
        <div class="header-item">
          <span class="label">总耗时:</span>
          <span class="value digital-font">{{ chain.totalDuration }}ms</span>
        </div>
      </div>

      <!-- 处理链时间轴 -->
      <div class="chain-timeline">
        <div
          v-for="(stage, index) in chain.stages"
          :key="index"
          class="timeline-item"
          :class="`status-${stage.status}`"
        >
          <!-- 时间轴点 -->
          <div class="timeline-dot">
            <div class="dot-inner">
              <el-icon v-if="stage.status === 'completed'" class="icon-check">
                <Check />
              </el-icon>
              <el-icon v-else-if="stage.status === 'processing'" class="icon-loading">
                <Loading />
              </el-icon>
              <el-icon v-else class="icon-clock">
                <Clock />
              </el-icon>
            </div>
          </div>

          <!-- 时间轴线 -->
          <div v-if="index < chain.stages.length - 1" class="timeline-line"></div>

          <!-- 阶段内容 -->
          <div class="stage-content">
            <div class="stage-header">
              <span class="stage-name">{{ stage.name }}</span>
              <span class="stage-time digital-font">{{ formatTime(stage.timestamp) }}</span>
            </div>

            <!-- 阶段详情 -->
            <div class="stage-details">
              <div v-if="stage.name === '异常检测'" class="details-grid">
                <div v-for="anomaly in stage.details.anomalies" :key="anomaly.type" class="detail-item">
                  <span class="detail-label">{{ anomaly.type }}:</span>
                  <span class="detail-value">{{ anomaly.value }}</span>
                  <span class="detail-severity" :class="`severity-${anomaly.severity}`">
                    {{ anomaly.severity }}
                  </span>
                </div>
              </div>

              <div v-else-if="stage.name === '事件产生'" class="details-grid">
                <div class="detail-item">
                  <span class="detail-label">事件类型:</span>
                  <span class="detail-value">{{ stage.details.eventType }}</span>
                </div>
              </div>

              <div v-else-if="stage.name === 'AI解析与上下文补全'" class="details-grid">
                <div class="detail-item">
                  <span class="detail-label">患者信息:</span>
                  <span class="detail-value">{{ stage.details.patientInfo }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">病史:</span>
                  <span class="detail-value">{{ stage.details.medicalHistory }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">当前用药:</span>
                  <span class="detail-value">{{ stage.details.currentMedication }}</span>
                </div>
              </div>

              <div v-else-if="stage.name === '风险评估'" class="details-grid">
                <div class="detail-item">
                  <span class="detail-label">风险等级:</span>
                  <span class="detail-value" :class="`risk-${stage.details.riskLevel}`">
                    {{ stage.details.riskLevel }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">风险分数:</span>
                  <span class="detail-value digital-font">{{ stage.details.riskScore }}/10</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">置信度:</span>
                  <span class="detail-value digital-font">{{ stage.details.confidence }}%</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">趋势:</span>
                  <span class="detail-value">{{ stage.details.trend }}</span>
                </div>
              </div>

              <div v-else-if="stage.name === '处置建议'" class="details-grid">
                <div class="detail-item">
                  <span class="detail-label">建议:</span>
                  <span class="detail-value">{{ stage.details.suggestion }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">优先级:</span>
                  <span class="detail-value" :class="`priority-${stage.details.priority}`">
                    {{ stage.details.priority }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">建议科室:</span>
                  <span class="detail-value">{{ stage.details.department }}</span>
                </div>
              </div>

              <div v-else-if="stage.name === '自动执行'" class="details-grid">
                <div class="detail-item">
                  <span class="detail-label">执行状态:</span>
                  <span class="detail-value" :class="`exec-${stage.details.executionStatus}`">
                    {{ stage.details.executionStatus }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">通知对象:</span>
                  <span class="detail-value">{{ stage.details.notificationTargets }}</span>
                </div>
              </div>

              <div v-else-if="stage.name === '留痕记录'" class="details-grid">
                <div class="detail-item">
                  <span class="detail-label">算法版本:</span>
                  <span class="detail-value digital-font">{{ stage.details.algorithmVersion }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">处理耗时:</span>
                  <span class="detail-value digital-font">{{ stage.details.processingTime }}ms</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Check, Loading, Clock } from '@element-plus/icons-vue'

interface Stage {
  name: string
  status: 'completed' | 'processing' | 'pending' | 'failed' | 'skipped'
  timestamp: string
  details: Record<string, any>
}

interface ProcessingChain {
  stages: Stage[]
  totalDuration: number
}

interface ExceptionData {
  id: string | number
  nickName: string
  type: string
}

const props = defineProps<{
  modelValue: boolean
  data: ExceptionData
  chain: ProcessingChain
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN')
}
</script>

<style scoped lang="scss">
.processing-chain-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.chain-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chain-header {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  padding: 15px;
  background: rgba(14, 165, 233, 0.05);
  border-radius: 8px;
  border-left: 3px solid #0ea5e9;

  .header-item {
    display: flex;
    gap: 8px;
    align-items: center;

    .label {
      color: #666;
      font-size: 12px;
      font-weight: 500;
    }

    .value {
      color: #333;
      font-size: 14px;
      font-weight: 600;
    }
  }
}

.chain-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
  position: relative;
}

.timeline-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  position: relative;
  border-left: 2px solid #e5e7eb;
  transition: all 0.3s ease;

  &.status-completed {
    border-left-color: #10b981;
    background: rgba(16, 185, 129, 0.02);
  }

  &.status-processing {
    border-left-color: #f59e0b;
    background: rgba(245, 158, 11, 0.02);
  }

  &.status-pending {
    border-left-color: #d1d5db;
    background: rgba(209, 213, 219, 0.02);
  }

  &:hover {
    background: rgba(14, 165, 233, 0.05);
  }
}

.timeline-dot {
  position: relative;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;

  .dot-inner {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: white;
    border: 2px solid #e5e7eb;
    transition: all 0.3s ease;

    .timeline-item.status-completed & {
      background: #10b981;
      border-color: #10b981;
      color: white;
    }

    .timeline-item.status-processing & {
      background: #f59e0b;
      border-color: #f59e0b;
      color: white;
    }

    .icon-check,
    .icon-loading,
    .icon-clock {
      font-size: 16px;
    }
  }
}

.timeline-line {
  position: absolute;
  left: 19px;
  top: 60px;
  width: 2px;
  height: 20px;
  background: #e5e7eb;

  .timeline-item.status-completed ~ .timeline-item .timeline-line {
    background: #10b981;
  }
}

.stage-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  .stage-name {
    font-size: 14px;
    font-weight: 600;
    color: #333;
  }

  .stage-time {
    font-size: 12px;
    color: #999;
  }
}

.stage-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
}

.detail-item {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 4px;
  font-size: 13px;

  .detail-label {
    color: #666;
    font-weight: 500;
    min-width: 80px;
  }

  .detail-value {
    color: #333;
    flex: 1;
    word-break: break-word;
  }

  .detail-severity {
    padding: 2px 8px;
    border-radius: 3px;
    font-size: 11px;
    font-weight: 600;
    white-space: nowrap;

    &.severity-high {
      background: #fee2e2;
      color: #dc2626;
    }

    &.severity-medium {
      background: #fef3c7;
      color: #d97706;
    }

    &.severity-low {
      background: #dbeafe;
      color: #2563eb;
    }
  }
}

.risk-high {
  color: #dc2626;
  font-weight: 600;
}

.risk-medium {
  color: #d97706;
  font-weight: 600;
}

.risk-low {
  color: #2563eb;
  font-weight: 600;
}

.priority-p1 {
  color: #dc2626;
  font-weight: 600;
}

.priority-p2 {
  color: #d97706;
  font-weight: 600;
}

.priority-p3 {
  color: #2563eb;
  font-weight: 600;
}

.exec-success {
  color: #10b981;
  font-weight: 600;
}

.exec-failed {
  color: #dc2626;
  font-weight: 600;
}

.digital-font {
  font-family: 'Courier New', monospace;
}
</style>
