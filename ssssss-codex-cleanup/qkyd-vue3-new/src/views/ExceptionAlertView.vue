<template>
  <div class="page-shell">
    <div class="panel section">
      <div class="toolbar">
        <el-input v-model="query.type" placeholder="异常类型" clearable style="width: 180px" />
        <el-select v-model="query.state" placeholder="处理状态" clearable style="width: 140px">
          <el-option label="未处理" value="0" />
          <el-option label="已处理" value="1" />
        </el-select>
        <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 180px" />
        <el-date-picker
          v-model="exceptionTimeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="异常开始时间"
          end-placeholder="异常结束时间"
          style="width: 320px"
        />
        <el-input-number v-model="userIdFilter" :min="0" :controls="false" placeholder="用户ID" />
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" :loading="exporting" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="nickName" label="姓名" min-width="100" />
        <el-table-column label="性别" width="90">
          <template #default="{ row }">
            {{ sexLabel(row.sex) }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="90" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="deviceId" label="设备ID" min-width="100" />
        <el-table-column prop="type" label="异常类型" min-width="120" />
        <el-table-column prop="value" label="指标值" min-width="120" />
        <el-table-column prop="state" label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.state === '1' ? 'success' : 'danger'">
              {{ row.state === '1' ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateBy" label="处理人" min-width="120" />
        <el-table-column prop="updateTime" label="处理时间" min-width="170" />
        <el-table-column prop="location" label="位置" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="异常时间" min-width="170" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openHandle(row)">处置</el-button>
            <el-button text @click="showDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @change="fetchList"
        />
      </div>
    </div>

    <el-dialog v-model="handleDialogVisible" title="异常处置" width="560px">
      <el-form :model="handleForm" label-width="96px">
        <el-form-item label="用户ID">
          <el-input :model-value="handleForm.userId" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input :model-value="handleForm.nickName" disabled />
        </el-form-item>
        <el-form-item label="性别">
          <el-input :model-value="sexLabel(handleForm.sex)" disabled />
        </el-form-item>
        <el-form-item label="年龄">
          <el-input :model-value="handleForm.age" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input :model-value="handleForm.phone" disabled />
        </el-form-item>
        <el-form-item label="设备ID">
          <el-input :model-value="handleForm.deviceId" disabled />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-input :model-value="handleForm.type" disabled />
        </el-form-item>
        <el-form-item label="指标值">
          <el-input :model-value="handleForm.value" disabled />
        </el-form-item>
        <el-form-item label="位置">
          <el-input :model-value="handleForm.location" disabled />
        </el-form-item>
        <el-form-item label="异常时间">
          <el-input :model-value="handleForm.createTime" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="handleForm.state">
            <el-radio value="0">未处理</el-radio>
            <el-radio value="1">已处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="handleForm.updateContent" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitHandle">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      title="异常详情"
      width="96vw"
      top="2vh"
      class="detail-dialog"
    >
      <div class="detail-map" v-if="hasLocationPoint">
        <iframe
          :src="mapEmbedUrl"
          title="异常位置地图"
          loading="lazy"
          referrerpolicy="no-referrer-when-downgrade"
        />
      </div>
      <div v-else class="detail-map detail-map--empty">
        暂无可用经纬度，暂时无法展示地图
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ detailData.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailData.nickName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ sexLabel(detailData.sex) }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ detailData.age ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="设备ID">{{ detailData.deviceId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">{{ detailData.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="指标值">{{ detailData.value || '-' }}</el-descriptions-item>
        <el-descriptions-item label="异常时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          {{ detailData.state === '1' ? '已处理' : '未处理' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detailData.updateBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ detailData.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地理位置" :span="2">{{ detailData.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经纬度" :span="2">
          {{ locationPointText }}
        </el-descriptions-item>
        <el-descriptions-item label="处理说明" :span="2">{{ detailData.updateContent || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailData.state !== '1'"
          type="primary"
          @click="handleDetailNow"
        >
          现在处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  exportExceptions,
  getException,
  listExceptions,
  listExceptionsByUserId,
  updateException,
  type ExceptionAlert
} from '@/api/health'

type ExceptionQuery = {
  pageNum: number
  pageSize: number
  type: string
  state: string
  phone: string
  startCreateTime?: Date
  endCreateTime?: Date
}

const loading = ref(false)
const saving = ref(false)
const exporting = ref(false)
const list = ref<ExceptionAlert[]>([])
const total = ref(0)
const userIdFilter = ref<number | undefined>(undefined)
const exceptionTimeRange = ref<[Date, Date] | []>([])

const handleDialogVisible = ref(false)
const detailVisible = ref(false)
const detailData = ref<ExceptionAlert>({})

const query = reactive<ExceptionQuery>({
  pageNum: 1,
  pageSize: 10,
  type: '',
  state: '',
  phone: '',
  startCreateTime: undefined,
  endCreateTime: undefined
})

const handleForm = reactive<ExceptionAlert>({
  id: undefined,
  userId: undefined,
  nickName: '',
  sex: '',
  age: undefined,
  phone: '',
  deviceId: undefined,
  type: '',
  value: '',
  location: '',
  createTime: '',
  state: '1',
  updateContent: ''
})

const sexLabel = (sex?: string) => {
  if (sex === '0') return '男'
  if (sex === '1') return '女'
  return '未知'
}

const locationPointText = computed(() => {
  if (detailData.value.longitude == null || detailData.value.latitude == null) {
    return '-'
  }
  return `${detailData.value.longitude}, ${detailData.value.latitude}`
})

const hasLocationPoint = computed(() => {
  return detailData.value.longitude != null && detailData.value.latitude != null
})

const mapEmbedUrl = computed(() => {
  if (!hasLocationPoint.value) {
    return ''
  }
  const latitude = Number(detailData.value.latitude)
  const longitude = Number(detailData.value.longitude)
  const left = longitude - 0.01
  const right = longitude + 0.01
  const top = latitude + 0.01
  const bottom = latitude - 0.01
  return `https://www.openstreetmap.org/export/embed.html?bbox=${left}%2C${bottom}%2C${right}%2C${top}&layer=mapnik&marker=${latitude}%2C${longitude}`
})

const syncTimeRangeToQuery = () => {
  query.startCreateTime = exceptionTimeRange.value[0]
  query.endCreateTime = exceptionTimeRange.value[1]
}

const fetchList = async () => {
  loading.value = true
  try {
    syncTimeRangeToQuery()
    const res = userIdFilter.value
      ? await listExceptionsByUserId(userIdFilter.value, query)
      : await listExceptions(query)
    list.value = (res.rows || []) as ExceptionAlert[]
    total.value = Number(res.total || 0)
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  exporting.value = true
  try {
    syncTimeRangeToQuery()
    const blob = await exportExceptions({
      ...query,
      userId: userIdFilter.value
    })
    const downloadUrl = window.URL.createObjectURL(blob as Blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `异常告警数据_${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

const resetQuery = () => {
  query.pageNum = 1
  query.pageSize = 10
  query.type = ''
  query.state = ''
  query.phone = ''
  query.startCreateTime = undefined
  query.endCreateTime = undefined
  exceptionTimeRange.value = []
  userIdFilter.value = undefined
  fetchList()
}

const openHandle = (row: ExceptionAlert) => {
  Object.assign(handleForm, {
    id: row.id,
    userId: row.userId,
    nickName: row.nickName || '',
    sex: row.sex || '',
    age: row.age,
    phone: row.phone || '',
    deviceId: row.deviceId,
    type: row.type || '',
    value: row.value || '',
    location: row.location || '',
    createTime: row.createTime || '',
    state: row.state || '1',
    updateContent: row.updateContent || ''
  })
  handleDialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.id) {
    return
  }
  saving.value = true
  try {
    await updateException(handleForm)
    ElMessage.success('更新成功')
    handleDialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

const showDetail = async (id?: number) => {
  if (!id) {
    return
  }
  const res = await getException(id)
  detailData.value = (res.data || {}) as ExceptionAlert
  detailVisible.value = true
}

const handleDetailNow = () => {
  detailVisible.value = false
  openHandle(detailData.value)
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.section {
  padding: 16px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.detail-map {
  margin-bottom: 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.detail-map iframe {
  width: 100%;
  height: 72vh;
  border: 0;
  display: block;
}

.detail-map--empty {
  height: 72vh;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
}

:deep(.detail-dialog .el-dialog) {
  max-width: 96vw;
}

:deep(.detail-dialog .el-dialog__body) {
  max-height: 90vh;
  overflow-y: auto;
}
</style>
