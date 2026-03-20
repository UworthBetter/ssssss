<template>
  <div class="page-shell">
    <div class="welcome panel">
      <div>
        <h2>健康数据驾驶舱</h2>
        <p>实时查看监测对象状态、异常事件与 AI 风险识别结果。</p>
      </div>
      <el-button type="primary" plain @click="fetchAll">刷新数据</el-button>
    </div>

    <el-row :gutter="14" class="kpi-row">
      <el-col v-for="item in kpiCards" :key="item.key" :xs="24" :sm="12" :lg="6">
        <div class="kpi panel">
          <p class="kpi-label">{{ item.label }}</p>
          <p class="kpi-value">{{ item.value }}</p>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14">
      <el-col :xs="24" :lg="12">
        <div class="panel section">
          <div class="section-head">
            <h3>年龄与性别分布</h3>
          </div>
          <el-table :data="ageSexTable" stripe height="300">
            <el-table-column prop="label" label="分组" min-width="120" />
            <el-table-column prop="value" label="人数" min-width="120" />
          </el-table>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="panel section">
          <div class="section-head">
            <h3>最近 AI 异常记录</h3>
          </div>
          <el-table :data="recentAbnormal" stripe height="300">
            <el-table-column prop="patientName" label="对象" min-width="100" />
            <el-table-column prop="abnormalType" label="类型" min-width="120" />
            <el-table-column prop="riskLevel" label="风险级别" min-width="100" />
            <el-table-column prop="createTime" label="时间" min-width="160" />
          </el-table>
        </div>
      </el-col>
    </el-row>

    <div class="panel section">
      <div class="section-head">
        <h3>异常事件流</h3>
      </div>
      <el-table :data="exceptionList" stripe>
        <el-table-column prop="nickName" label="姓名" min-width="100" />
        <el-table-column prop="type" label="异常类型" min-width="130" />
        <el-table-column prop="value" label="指标值" min-width="120" />
        <el-table-column prop="state" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.state === '1' ? 'success' : 'warning'">
              {{ row.state === '1' ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发生时间" min-width="170" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecentAbnormal } from '@/api/ai'
import { getAgeSexGroupCount, getIndexException, getRealTimeData } from '@/api/index'

interface GenericRow {
  [key: string]: unknown
}

const realTimeData = ref<Record<string, unknown>>({})
const ageSexTable = ref<Array<{ label: string; value: number }>>([])
const recentAbnormal = ref<GenericRow[]>([])
const exceptionList = ref<GenericRow[]>([])

const kpiCards = computed(() => [
  { key: 'online', label: '在线监测数', value: Number(realTimeData.value.onlineCount || 0) },
  { key: 'exception', label: '待处理异常', value: Number(realTimeData.value.unhandledExceptionCount || 0) },
  { key: 'risk', label: '高风险对象', value: Number(realTimeData.value.highRiskCount || 0) },
  { key: 'device', label: '设备总量', value: Number(realTimeData.value.deviceCount || 0) }
])

const toList = (input: unknown): GenericRow[] => {
  if (Array.isArray(input)) {
    return input as GenericRow[]
  }
  if (input && typeof input === 'object') {
    return Object.entries(input as Record<string, unknown>).map(([label, value]) => ({ label, value }))
  }
  return []
}

const fetchAll = async () => {
  try {
    const [rtRes, ageRes, recentRes, exceptionRes] = await Promise.all([
      getRealTimeData(),
      getAgeSexGroupCount(),
      getRecentAbnormal(8),
      getIndexException('all', 1)
    ])

    realTimeData.value = (rtRes.data || {}) as Record<string, unknown>
    ageSexTable.value = toList(ageRes.data).map((item) => ({
      label: String(item.label || '-'),
      value: Number(item.value || 0)
    }))

    recentAbnormal.value = toList(recentRes.data)
    exceptionList.value = toList(exceptionRes.list || exceptionRes.data || exceptionRes.rows)
  } catch (error) {
    ElMessage.error('驾驶舱数据加载失败')
  }
}

onMounted(fetchAll)
</script>

<style scoped lang="scss">
.welcome {
  padding: 18px;
  margin-bottom: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  h2 {
    margin: 0 0 6px;
  }

  p {
    margin: 0;
    color: var(--text-sub);
  }
}

.kpi-row {
  margin-bottom: 14px;
}

.kpi {
  padding: 16px;
}

.kpi-label {
  margin: 0 0 8px;
  color: var(--text-sub);
}

.kpi-value {
  margin: 0;
  font-size: 34px;
  font-weight: 700;
  color: #0f766e;
}

.section {
  padding: 16px;
  margin-bottom: 14px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  h3 {
    margin: 0;
    font-size: 16px;
  }
}
</style>
