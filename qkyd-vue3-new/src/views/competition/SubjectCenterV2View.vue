<template>
  <PlatformPageShell
    title="对象中心"
    subtitle="统一承载服务对象档案、风险画像、设备绑定和关联事件，为后续对象 360 详情页建立稳定结构。"
    eyebrow="SUBJECT CENTER"
    aside-title="对象 360 预览"
    aside-width="360px"
  >
    <template #headerExtra>
      <div class="header-actions">
        <PlatformSearchEntry
          :label="searchPresentation.label"
          :placeholder="searchPresentation.placeholder"
          :hint="searchPresentation.hint"
          @click="handleSearchClick"
        />
      </div>
    </template>

    <template #toolbar>
      <div class="toolbar-stack">
        <div class="toolbar">
          <el-input
            v-model="query.keyword"
            placeholder="搜索姓名、房间号或异常标签"
            clearable
            style="width: 240px"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="query.region" placeholder="所在区域" clearable style="width: 140px">
            <el-option label="东区" value="east" />
            <el-option label="西区" value="west" />
          </el-select>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="openCreate">新增对象</el-button>
        </div>
      </div>
    </template>

    <div class="panel section">
      <el-table v-loading="loading" :data="list" stripe highlight-current-row @current-change="handleCurrentChange" @row-click="handleRowSelect">
        <el-table-column prop="subjectId" label="ID" width="90" />
        <el-table-column prop="subjectName" label="对象账号" min-width="120" />
        <el-table-column prop="nickName" label="姓名" min-width="120" />
        <el-table-column prop="age" label="年龄" width="90" />
        <el-table-column label="性别" width="90">
          <template #default="{ row }">{{ sexLabel(row.sex) }}</template>
        </el-table-column>
        <el-table-column label="风险等级" min-width="110">
          <template #default="{ row }">
            <el-tag :type="riskTagType(getRiskLevel(row))" effect="light">{{ riskLabelMap[getRiskLevel(row)] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phonenumber" label="手机号" min-width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'warning'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row.subjectId)">编辑</el-button>
            <el-button text @click="handleRowSelect(row)">查看 360</el-button>
            <el-button text type="danger" @click="removeItem(row.subjectId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, sizes, prev, pager, next" @change="fetchList" />
      </div>
    </div>

    <template #aside>
      <div class="aside-stack">
        <div class="panel aside-card detail-card">
          <div class="detail-head">
            <div>
              <div class="aside-card-title">当前选中对象</div>
              <p class="detail-subtitle">第一版对象 360 预览聚焦基础档案、设备摘要、事件摘要和 AI 标签。</p>
            </div>
            <el-tag v-if="selectedSubject" :type="riskTagType(selectedRiskLevel)" effect="light">{{ riskLabelMap[selectedRiskLevel] }}</el-tag>
          </div>

          <div v-if="selectedSubject" class="detail-body">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="对象ID">{{ selectedSubject.subjectId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="对象账号">{{ selectedSubject.subjectName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ selectedSubject.nickName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ selectedSubject.phonenumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ selectedSubject.status === '0' ? '正常' : '停用' }}</el-descriptions-item>
            </el-descriptions>

            <div class="detail-grid">
              <div class="mini-card">
                <span class="mini-label">绑定设备</span>
                <strong class="mini-value">{{ deviceSummary }}</strong>
              </div>
              <div class="mini-card">
                <span class="mini-label">最近异常</span>
                <strong class="mini-value">{{ eventSummary }}</strong>
              </div>
            </div>

            <div class="tag-block">
              <div class="block-title">AI 风险标签</div>
              <div class="tag-list">
                <el-tag v-for="item in aiTags" :key="item" effect="light" round>{{ item }}</el-tag>
              </div>
            </div>

            <div class="workflow-actions">
              <el-button type="primary" @click="openEdit(selectedSubject.subjectId)">编辑对象</el-button>
              <el-button @click="openDeviceLink">查看设备</el-button>
              <el-button @click="openEventLink">查看事件</el-button>
            </div>
          </div>

          <div v-else class="empty-detail">
            <div class="empty-title">尚未选中对象</div>
            <p>从左侧列表中选择一个对象后，这里会展示第一版 360 预览内容。</p>
          </div>
        </div>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" :title="form.subjectId ? '编辑服务对象' : '新增服务对象'" width="580px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="对象账号" prop="subjectName"><el-input v-model="form.subjectName" /></el-form-item>
        <el-form-item label="姓名" prop="nickName"><el-input v-model="form.nickName" /></el-form-item>
        <el-form-item label="手机号" prop="phonenumber"><el-input v-model="form.phonenumber" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="form.age" :min="1" :max="120" /></el-form-item>
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
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </PlatformPageShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter, type LocationQuery } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  PlatformPageShell,
  PlatformSearchEntry,
  dispatchPlatformAction,
  getPlatformSearchPresentation,
  openPlatformSearch,
} from '@/components/platform'
import { createSubject, deleteSubject, getSubject, listSubjects, updateSubject, type HealthSubject } from '@/api/health'
import { useRouteQueryListSync } from '@/composables/useRouteQueryListSync'
import { Search } from '@element-plus/icons-vue'

type SubjectRiskLevel = 'high' | 'medium' | 'low'

const riskLabelMap: Record<SubjectRiskLevel, string> = { high: '高关注', medium: '中关注', low: '低关注' }
const loading = ref(false)
const saving = ref(false)
const list = ref<HealthSubject[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const selectedSubject = ref<HealthSubject | null>(null)
const route = useRoute()
const router = useRouter()
const searchPresentation = getPlatformSearchPresentation('subject')

const query = reactive({ pageNum: 1, pageSize: 12, keyword: '', region: '' })
const applyRouteQuery = (routeQuery: LocationQuery) => {
  query.pageNum = 1
  query.keyword = String(routeQuery.keyword || '')
  query.region = String(routeQuery.region || '')
}

const initialForm = (): HealthSubject => ({ subjectName: '', nickName: '', phonenumber: '', email: '', age: undefined, sex: '0', status: '0', remark: '' })
const form = reactive<HealthSubject>(initialForm())
const rules: FormRules = {
  subjectName: [{ required: true, message: '请输入对象账号', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const getRiskLevel = (row: HealthSubject): SubjectRiskLevel => (row.status === '1' ? 'high' : Number(row.age || 0) >= 75 ? 'medium' : 'low')
const selectedRiskLevel = computed(() => (selectedSubject.value ? getRiskLevel(selectedSubject.value) : 'low'))
const deviceSummary = computed(() => (!selectedSubject.value ? '-' : selectedSubject.value.status === '1' ? '1 台设备待校验' : '2 台设备在线'))
const eventSummary = computed(() => (!selectedSubject.value ? '-' : getRiskLevel(selectedSubject.value) === 'high' ? '近 24h 有 2 条异常' : '近 7d 无新增异常'))
const aiTags = computed(() => {
  if (!selectedSubject.value) return []
  const risk = getRiskLevel(selectedSubject.value)
  if (risk === 'high') return ['重点关注', '需要回访', '设备状态待确认']
  if (risk === 'medium') return ['高龄对象', '建议跟踪睡眠']
  return ['状态稳定', '低风险']
})

const sexLabel = (sex?: string) => (sex === '0' ? '男' : sex === '1' ? '女' : '未知')
const riskTagType = (risk: SubjectRiskLevel) => (risk === 'high' ? 'danger' : risk === 'medium' ? 'warning' : 'success')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listSubjects(query)
    list.value = (res.rows || []) as HealthSubject[]
    total.value = Number(res.total || 0)
    syncSelectedAfterFetch()
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
const resetForm = () => Object.assign(form, initialForm())
const handleRowSelect = (row: HealthSubject) => { selectedSubject.value = row }
const handleCurrentChange = (row?: HealthSubject) => { if (row) selectedSubject.value = row }
const openCreate = () => { resetForm(); dialogVisible.value = true }
const openEdit = async (subjectId?: number) => {
  if (!subjectId) return
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
const removeItem = async (subjectId?: number) => {
  if (!subjectId) return
  await ElMessageBox.confirm('删除后不可恢复，确认继续吗？', '提示', { type: 'warning' })
  await deleteSubject([subjectId])
  ElMessage.success('删除成功')
  fetchList()
}
const handleSearchClick = async () => {
  await openPlatformSearch(router, 'subject')
}
const openDeviceLink = async () => {
  if (!selectedSubject.value) return ElMessage.info('请先选择一个对象')
  await dispatchPlatformAction(router, '查看设备', {
    entities: {
      device: {
        kind: 'device',
        name: `${selectedSubject.value.nickName || selectedSubject.value.subjectName} 的设备`,
        query: { userId: String(selectedSubject.value.subjectId || '') }
      }
    }
  })
}
const openEventLink = async () => {
  if (!selectedSubject.value) return ElMessage.info('请先选择一个对象')
  await dispatchPlatformAction(router, '查看事件', {
    entities: {
      event: {
        kind: 'event',
        name: `${selectedSubject.value.nickName || selectedSubject.value.subjectName} 的事件`,
        query: { userId: String(selectedSubject.value.subjectId || '') }
      }
    }
  })
}

const { install: installRouteQuerySync, syncSelectedAfterFetch } = useRouteQueryListSync<HealthSubject>({
  route,
  list,
  selected: selectedSubject,
  applyQuery: applyRouteQuery,
  resolveMatchedItem: ({ list, routeQuery, fallbackSelected }) => {
    const routeSubjectName = String(routeQuery.subjectName || routeQuery.keyword || '')
    if (routeSubjectName) {
      const matched = list.find(
        (item) =>
          item.subjectName === routeSubjectName ||
          item.nickName === routeSubjectName ||
          String(item.subjectId || '') === routeSubjectName
      )
      if (matched) return matched
    }

    if (fallbackSelected) {
      return list.find((item) => item.subjectId === fallbackSelected.subjectId)
    }

    return null
  },
  fetchList
})

installRouteQuerySync()
</script>

<style scoped lang="scss">
.toolbar-stack { display: flex; flex-direction: column; gap: 14px; }
.toolbar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.section { padding: 16px; }
.header-actions { width: min(360px, 100%); }
.aside-stack { display: flex; flex-direction: column; gap: 14px; }
.aside-card { padding: 16px; border-radius: 18px; }
.detail-card, .detail-body { display: flex; flex-direction: column; gap: 16px; }
.detail-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
.aside-card-title, .block-title { font-size: 14px; font-weight: 700; color: var(--text-main); }
.detail-subtitle { margin: 6px 0 0; font-size: 12px; line-height: 1.5; color: var(--text-sub); }
.detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.mini-card { display: flex; flex-direction: column; gap: 6px; padding: 12px; border-radius: 14px; background: rgba(255,255,255,.56); border: 1px solid rgba(221,227,233,.84); }
.mini-label { font-size: 12px; color: var(--text-sub); }
.mini-value { font-size: 16px; font-weight: 700; color: var(--text-main); }
.tag-block { display: flex; flex-direction: column; gap: 12px; }
.tag-list, .workflow-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.empty-detail { padding: 16px; border-radius: 16px; background: rgba(255,255,255,.56); border: 1px dashed rgba(221,227,233,.88); }
.empty-title { margin-bottom: 8px; font-size: 14px; font-weight: 700; color: var(--text-main); }
.pagination { margin-top: 14px; display: flex; justify-content: flex-end; }
@media (max-width: 640px) { .detail-grid { grid-template-columns: minmax(0, 1fr); } }
</style>
