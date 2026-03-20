<template>
  <div class="page-shell">
    <div class="panel section">
      <div class="toolbar">
        <el-input v-model="query.type" placeholder="异常类型" clearable style="width: 180px" />
        <el-select v-model="query.state" placeholder="处理状态" clearable style="width: 140px">
          <el-option label="待处理" value="0" />
          <el-option label="已处理" value="1" />
        </el-select>
        <el-input-number v-model="userIdFilter" :min="0" :controls="false" placeholder="按用户ID查询" />
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="nickName" label="姓名" min-width="100" />
        <el-table-column prop="userId" label="用户ID" min-width="100" />
        <el-table-column prop="deviceId" label="设备ID" min-width="100" />
        <el-table-column prop="type" label="异常类型" min-width="120" />
        <el-table-column prop="value" label="指标值" min-width="120" />
        <el-table-column prop="state" label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.state === '1' ? 'success' : 'danger'">{{ row.state === '1' ? '已处理' : '待处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发生时间" min-width="170" />
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
        <el-form-item label="异常ID">
          <el-input v-model="handleForm.id" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="handleForm.state">
            <el-radio value="0">待处理</el-radio>
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

    <el-dialog v-model="detailVisible" title="异常详情" width="760px">
      <pre class="json-block">{{ JSON.stringify(detailData, null, 2) }}</pre>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getException,
  listExceptions,
  listExceptionsByUserId,
  updateException,
  type ExceptionAlert
} from '@/api/health'

const loading = ref(false)
const saving = ref(false)
const list = ref<ExceptionAlert[]>([])
const total = ref(0)
const userIdFilter = ref<number | undefined>(undefined)

const handleDialogVisible = ref(false)
const detailVisible = ref(false)
const detailData = ref<Record<string, unknown>>({})

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  type: '',
  state: ''
})

const handleForm = reactive<ExceptionAlert>({
  id: undefined,
  state: '1',
  updateContent: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = userIdFilter.value
      ? await listExceptionsByUserId(userIdFilter.value, query)
      : await listExceptions(query)
    list.value = (res.rows || []) as ExceptionAlert[]
    total.value = Number(res.total || 0)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.pageNum = 1
  query.pageSize = 10
  query.type = ''
  query.state = ''
  userIdFilter.value = undefined
  fetchList()
}

const openHandle = (row: ExceptionAlert) => {
  handleForm.id = row.id
  handleForm.state = row.state || '1'
  handleForm.updateContent = row.updateContent || ''
  handleDialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.id) {
    return
  }
  saving.value = true
  try {
    await updateException(handleForm)
    ElMessage.success('处置结果已更新')
    handleDialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

const showDetail = async (id: number) => {
  const res = await getException(id)
  detailData.value = (res.data || {}) as Record<string, unknown>
  detailVisible.value = true
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.section {
  padding: 16px;
}

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>
