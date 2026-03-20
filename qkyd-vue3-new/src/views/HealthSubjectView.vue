<template>
  <div class="page-shell">
    <div class="panel section">
      <div class="toolbar">
        <el-input v-model="query.subjectName" placeholder="服务对象名称" clearable style="width: 220px" />
        <el-input v-model="query.phonenumber" placeholder="手机号" clearable style="width: 220px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 160px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openCreate">新增对象</el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="subjectId" label="ID" width="90" />
        <el-table-column prop="subjectName" label="对象账号" min-width="120" />
        <el-table-column prop="nickName" label="姓名" min-width="120" />
        <el-table-column prop="age" label="年龄" width="90" />
        <el-table-column prop="sex" label="性别" width="90">
          <template #default="{ row }">{{ sexLabel(row.sex) }}</template>
        </el-table-column>
        <el-table-column prop="phonenumber" label="手机号" min-width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'warning'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row.subjectId)">编辑</el-button>
            <el-button text type="danger" @click="removeItem(row.subjectId)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.subjectId ? '编辑服务对象' : '新增服务对象'" width="580px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="对象账号" prop="subjectName">
          <el-input v-model="form.subjectName" />
        </el-form-item>
        <el-form-item label="姓名" prop="nickName">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input v-model="form.phonenumber" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio value="0">男</el-radio>
            <el-radio value="1">女</el-radio>
            <el-radio value="2">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import {
  createSubject,
  deleteSubject,
  getSubject,
  listSubjects,
  updateSubject,
  type HealthSubject
} from '@/api/health'

const loading = ref(false)
const saving = ref(false)
const list = ref<HealthSubject[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  subjectName: '',
  phonenumber: '',
  status: ''
})

const initialForm = (): HealthSubject => ({
  subjectName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  age: undefined,
  sex: '0',
  status: '0',
  remark: ''
})

const form = reactive<HealthSubject>(initialForm())

const rules: FormRules = {
  subjectName: [{ required: true, message: '请输入对象账号', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const sexLabel = (sex?: string) => {
  if (sex === '0') return '男'
  if (sex === '1') return '女'
  return '未知'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listSubjects(query)
    list.value = (res.rows || []) as HealthSubject[]
    total.value = Number(res.total || 0)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.pageNum = 1
  query.pageSize = 10
  query.subjectName = ''
  query.phonenumber = ''
  query.status = ''
  fetchList()
}

const resetForm = () => {
  Object.assign(form, initialForm())
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (subjectId: number) => {
  const res = await getSubject(subjectId)
  Object.assign(form, res.data || {})
  dialogVisible.value = true
}

const submit = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.subjectId) {
      await updateSubject(form)
      ElMessage.success('修改成功')
    } else {
      await createSubject(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

const removeItem = async (subjectId: number) => {
  await ElMessageBox.confirm('删除后不可恢复，确认继续吗？', '提示', { type: 'warning' })
  await deleteSubject([subjectId])
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
