<template>
  <div class="page-shell">
    <div class="panel section">
      <div class="toolbar">
        <el-input v-model="query.name" placeholder="设备名称" clearable style="width: 220px" />
        <el-input v-model="query.imei" placeholder="IMEI" clearable style="width: 220px" />
        <el-input v-model="query.userId" placeholder="用户ID" clearable style="width: 160px" />
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openCreate">新增设备</el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="设备名称" min-width="150" />
        <el-table-column prop="imei" label="IMEI" min-width="180" />
        <el-table-column prop="type" label="设备类型" min-width="140" />
        <el-table-column prop="userId" label="绑定用户ID" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row.id)">编辑</el-button>
            <el-button text type="danger" @click="removeItem(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑设备' : '新增设备'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="IMEI" prop="imei">
          <el-input v-model="form.imei" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model="form.type" />
        </el-form-item>
        <el-form-item label="绑定用户ID">
          <el-input-number v-model="form.userId" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createDevice, deleteDevice, getDevice, listDevices, updateDevice, type DeviceInfo } from '@/api/health'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const list = ref<DeviceInfo[]>([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  imei: '',
  userId: ''
})

const initialForm = (): DeviceInfo => ({
  name: '',
  imei: '',
  type: '',
  userId: undefined
})

const form = reactive<DeviceInfo>(initialForm())

const rules: FormRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  imei: [{ required: true, message: '请输入 IMEI', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listDevices(query)
    list.value = (res.rows || []) as DeviceInfo[]
    total.value = Number(res.total || 0)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.pageNum = 1
  query.pageSize = 10
  query.name = ''
  query.imei = ''
  query.userId = ''
  fetchList()
}

const resetForm = () => {
  Object.assign(form, initialForm())
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (id: number) => {
  const res = await getDevice(id)
  Object.assign(form, res.data || {})
  dialogVisible.value = true
}

const submit = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateDevice(form)
      ElMessage.success('设备修改成功')
    } else {
      await createDevice(form)
      ElMessage.success('设备新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

const removeItem = async (id: number) => {
  await ElMessageBox.confirm('确认删除该设备吗？', '提示', { type: 'warning' })
  await deleteDevice([id])
  ElMessage.success('删除成功')
  fetchList()
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
