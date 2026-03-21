<template>
  <div class="page-shell">
    <div class="panel section">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="AI 对话" name="chat">
          <div class="toolbar">
            <el-input v-model="chatMessage" placeholder="例如：请给出今天的健康巡检建议" clearable />
            <el-button type="primary" :loading="running" @click="runChat">发送</el-button>
          </div>
          <pre class="json-block">{{ chatResult }}</pre>
        </el-tab-pane>

        <el-tab-pane label="跌倒检测" name="fall">
          <el-form :model="fallForm" inline>
            <el-form-item label="acc_x"><el-input-number v-model="fallForm.acc_x" :step="0.1" /></el-form-item>
            <el-form-item label="acc_y"><el-input-number v-model="fallForm.acc_y" :step="0.1" /></el-form-item>
            <el-form-item label="acc_z"><el-input-number v-model="fallForm.acc_z" :step="0.1" /></el-form-item>
            <el-form-item label="年龄"><el-input-number v-model="fallForm.age" :min="1" :max="120" /></el-form-item>
            <el-form-item label="位置"><el-input v-model="fallForm.location" /></el-form-item>
          </el-form>
          <el-button type="primary" :loading="running" @click="runFall">执行检测</el-button>
          <pre class="json-block">{{ fallResult }}</pre>
        </el-tab-pane>

        <el-tab-pane label="风险评估" name="risk">
          <el-form :model="riskForm" inline>
            <el-form-item label="patientId"><el-input-number v-model="riskForm.patientId" :min="1" /></el-form-item>
            <el-form-item label="patientName"><el-input v-model="riskForm.patientName" /></el-form-item>
            <el-form-item label="心率"><el-input-number v-model="riskForm.heartRate" :min="20" :max="220" /></el-form-item>
            <el-form-item label="血氧"><el-input-number v-model="riskForm.spo2" :min="60" :max="100" /></el-form-item>
          </el-form>
          <el-button type="primary" :loading="running" @click="runRisk">执行评估</el-button>
          <pre class="json-block">{{ riskResult }}</pre>
        </el-tab-pane>

        <el-tab-pane label="趋势分析" name="trend">
          <div class="toolbar">
            <el-input
              v-model="trendInput"
              type="textarea"
              :rows="5"
              placeholder='请输入 JSON，例如 {"metric":"heartRate","values":[80,84,88,91]}'
            />
          </div>
          <el-button type="primary" :loading="running" @click="runTrend">执行分析</el-button>
          <pre class="json-block">{{ trendResult }}</pre>
        </el-tab-pane>

        <el-tab-pane label="质量检测" name="quality">
          <div class="toolbar">
            <el-input
              v-model="qualityInput"
              type="textarea"
              :rows="5"
              placeholder='请输入 JSON，例如 {"heartRate":88,"spo2":97}'
            />
          </div>
          <el-button type="primary" :loading="running" @click="runQuality">执行检测</el-button>
          <pre class="json-block">{{ qualityResult }}</pre>
        </el-tab-pane>

        <el-tab-pane label="异常检测" name="abnormal">
          <div class="toolbar">
            <el-input
              v-model="abnormalInput"
              type="textarea"
              :rows="5"
              placeholder='请输入 JSON，例如 {"patientId":1,"patientName":"张三","heartRate":130}'
            />
            <el-button :loading="running" @click="loadRecent">拉取最近异常</el-button>
          </div>
          <el-button type="primary" :loading="running" @click="runAbnormal">执行检测</el-button>
          <pre class="json-block">{{ abnormalResult }}</pre>
          <el-table :data="recentRows" stripe style="margin-top: 12px">
            <el-table-column prop="patientName" label="对象" min-width="100" />
            <el-table-column prop="abnormalType" label="类型" min-width="120" />
            <el-table-column prop="riskLevel" label="风险级别" min-width="100" />
            <el-table-column prop="createTime" label="时间" min-width="160" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  analyzeTrend,
  assessRisk,
  chatAi,
  checkQuality,
  detectAbnormal,
  detectFall,
  getRecentAbnormal
} from '@/api/ai'

const activeTab = ref('chat')
const running = ref(false)

const chatMessage = ref('请给出今日慢病老人的健康关注重点')
const chatResult = ref('{}')

const fallForm = ref({
  acc_x: 1.2,
  acc_y: 2.8,
  acc_z: 9.3,
  age: 72,
  location: '居家卧室',
  timestamp: Date.now(),
  extra: { source: 'manual-ui' }
})
const fallResult = ref('{}')

const riskForm = ref({
  patientId: 1,
  patientName: '测试对象',
  heartRate: 108,
  spo2: 92,
  systolic: 150,
  diastolic: 96
})
const riskResult = ref('{}')

const trendInput = ref('{"metric":"heartRate","values":[82,85,91,95,102]}')
const trendResult = ref('{}')

const qualityInput = ref('{"heartRate":92,"spo2":96,"temperature":36.7}')
const qualityResult = ref('{}')

const abnormalInput = ref('{"patientId":1,"patientName":"张三","heartRate":132,"spo2":88}')
const abnormalResult = ref('{}')
const recentRows = ref<Array<Record<string, unknown>>>([])

const prettify = (input: unknown) => JSON.stringify(input ?? {}, null, 2)

const parseJson = (text: string) => {
  try {
    return JSON.parse(text)
  } catch (error) {
    throw new Error('请输入合法 JSON')
  }
}

const withRun = async (runner: () => Promise<void>) => {
  running.value = true
  try {
    await runner()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '执行失败')
  } finally {
    running.value = false
  }
}

const runChat = async () =>
  withRun(async () => {
    const res = await chatAi(chatMessage.value)
    chatResult.value = prettify(res.data)
  })

const runFall = async () =>
  withRun(async () => {
    fallForm.value.timestamp = Date.now()
    const res = await detectFall(fallForm.value as Record<string, unknown>)
    fallResult.value = prettify(res.data)
  })

const runRisk = async () =>
  withRun(async () => {
    const res = await assessRisk(riskForm.value as Record<string, unknown>)
    riskResult.value = prettify(res.data)
  })

const runTrend = async () =>
  withRun(async () => {
    const res = await analyzeTrend(parseJson(trendInput.value))
    trendResult.value = prettify(res.data)
  })

const runQuality = async () =>
  withRun(async () => {
    const res = await checkQuality(parseJson(qualityInput.value))
    qualityResult.value = prettify(res.data)
  })

const runAbnormal = async () =>
  withRun(async () => {
    const res = await detectAbnormal(parseJson(abnormalInput.value))
    abnormalResult.value = prettify(res.data)
  })

const loadRecent = async () =>
  withRun(async () => {
    const res = await getRecentAbnormal(10)
    recentRows.value = Array.isArray(res.data) ? (res.data as Array<Record<string, unknown>>) : []
  })
</script>

<style scoped lang="scss">
.section {
  padding: 14px 16px;
}
</style>
