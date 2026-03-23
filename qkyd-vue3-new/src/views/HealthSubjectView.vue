<template>
  <!-- 移除了 aside 相关的属性，让页面成为全宽布局 -->
  <PlatformPageShellV2
    title="对象中心"
    subtitle="统一承载服务对象档案、风险画像、设备绑定和关联事件，为后续对象 360 详情页建立稳定结构。"
    eyebrow="SUBJECT CENTER"
  >
    <template #headerExtra>
      <div class="header-actions">
        <PlatformSearchEntry
          :label="searchPresentation.label"
          :placeholder="searchPresentation.placeholder"
          :hint="searchPresentation.hint"
          @click="handleSearchClick"
        />
        <el-button type="primary" class="modern-btn" @click="openCreate">
          <el-icon class="mr-1"><Plus /></el-icon> 新增对象
        </el-button>
      </div>
    </template>

    <template #toolbar>
      <div class="toolbar-stack">
        <div class="modern-toolbar">
          <div class="search-group">
            <el-input v-model="query.subjectName" placeholder="搜索账号 / 姓名..." prefix-icon="Search" clearable class="modern-input" />
            <el-input v-model="query.phonenumber" placeholder="手机号" prefix-icon="Phone" clearable class="modern-input" />
            <el-select v-model="query.status" placeholder="全部状态" clearable class="modern-select">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
            <el-button type="primary" plain @click="fetchList">查询</el-button>
            <el-button text @click="resetQuery">重置</el-button>
          </div>
        </div>
      </div>
    </template>

    <div class="modern-table-container">
      <el-table 
        v-loading="loading" 
        :data="list" 
        class="modern-table"
        highlight-current-row 
        @current-change="handleCurrentChange" 
        @row-click="handleRowClick"
      >
        <!-- 复合列：用户信息 -->
        <el-table-column label="服务对象" min-width="220" fixed="left">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="40" class="user-avatar" :class="'avatar-' + getRiskLevel(row)">
                {{ row.nickName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="user-info">
                <span class="user-name">{{ row.nickName || '未知姓名' }}</span>
                <span class="user-id">ID: {{ row.subjectName || row.subjectId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 整合列：人口统计信息 -->
        <el-table-column label="生理特征" min-width="120">
          <template #default="{ row }">
            <span class="demographic-text">{{ row.age ? row.age + '岁' : '-' }} · {{ sexLabel(row.sex) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="风险评估" min-width="140">
          <template #default="{ row }">
            <div class="risk-badge" :class="'risk-' + getRiskLevel(row)">
              <span class="dot"></span>
              {{ riskLabelMap[getRiskLevel(row)] }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="phonenumber" label="联系方式" min-width="140">
          <template #default="{ row }">
            <span class="phone-text">{{ row.phonenumber || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'" effect="light" round size="small">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openEdit(row.subjectId)">编辑</el-button>
            <el-button link type="primary" @click.stop="handleRowClick(row)">360 视图</el-button>
            <el-button link type="danger" @click.stop="removeItem(row.subjectId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination 
          v-model:current-page="query.pageNum" 
          v-model:page-size="query.pageSize" 
          :total="total" 
          background
          layout="total, sizes, prev, pager, next" 
          @change="fetchList" 
        />
      </div>
    </div>

    <!-- 现代化的抽屉式 360 视图 -->
    <el-drawer
      v-model="drawerVisible"
      title="对象 360 视图"
      size="440px"
      :with-header="false"
      destroy-on-close
      class="modern-drawer"
    >
      <div v-if="selectedSubject" class="drawer-inner">
        <!-- 抽屉头部：用户概览 -->
        <div class="drawer-header">
          <div class="drawer-close" @click="drawerVisible = false">
            <el-icon><Close /></el-icon>
          </div>
          <div class="profile-hero">
            <el-avatar :size="64" class="hero-avatar" :class="'avatar-' + selectedRiskLevel">
              {{ selectedSubject.nickName?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="hero-info">
              <h2>{{ selectedSubject.nickName || '未知姓名' }}</h2>
              <p>@{{ selectedSubject.subjectName }}</p>
            </div>
            <div class="hero-status">
              <el-tag :type="riskTagType(selectedRiskLevel)" effect="dark" round>
                {{ riskLabelMap[selectedRiskLevel] }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="drawer-body">
          <!-- 数据核心指标卡片 -->
          <div class="metric-grid">
            <div class="metric-card cursor-pointer" @click="openDeviceLink">
              <div class="metric-icon bg-blue"><el-icon><Monitor /></el-icon></div>
              <div class="metric-data">
                <span class="label">绑定设备</span>
                <strong class="value">{{ deviceSummary }}</strong>
              </div>
            </div>
            <div class="metric-card cursor-pointer" @click="openEventLink">
              <div class="metric-icon bg-orange"><el-icon><Warning /></el-icon></div>
              <div class="metric-data">
                <span class="label">近期异常</span>
                <strong class="value">{{ eventSummary }}</strong>
              </div>
            </div>
          </div>

          <!-- AI 标签区域 -->
          <div class="info-section">
            <h3 class="section-title">AI 风险标签</h3>
            <div class="tag-cloud">
              <el-tag v-for="item in aiTags" :key="item" effect="plain" round class="modern-tag">
                {{ item }}
              </el-tag>
            </div>
          </div>

          <!-- 详细档案信息 -->
          <div class="info-section">
            <h3 class="section-title">基础档案</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">系统 ID</span>
                <span class="info-value">{{ selectedSubject.subjectId || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">年龄 / 性别</span>
                <span class="info-value">{{ selectedSubject.age ? selectedSubject.age + '岁' : '-' }} · {{ sexLabel(selectedSubject.sex) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">联系电话</span>
                <span class="info-value">{{ selectedSubject.phonenumber || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">当前状态</span>
                <span class="info-value" :class="selectedSubject.status === '0' ? 'text-green' : 'text-gray'">
                  {{ selectedSubject.status === '0' ? '正常服务中' : '已停用' }}
                </span>
              </div>
              <div class="info-item" v-if="selectedSubject.remark">
                <span class="info-label">备注说明</span>
                <span class="info-value text-wrap">{{ selectedSubject.remark }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="drawer-footer">
          <el-button class="full-btn" plain @click="openEdit(selectedSubject.subjectId)">编辑档案资料</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 表单保持不变 -->
    <el-dialog v-model="dialogVisible" :title="form.subjectId ? '编辑服务对象' : '新增服务对象'" width="580px" class="modern-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="对象账号" prop="subjectName"><el-input v-model="form.subjectName" placeholder="登录账号或唯一标识" /></el-form-item>
        <el-form-item label="姓名" prop="nickName"><el-input v-model="form.nickName" placeholder="真实姓名" /></el-form-item>
        <el-form-item label="手机号" prop="phonenumber"><el-input v-model="form.phonenumber" placeholder="联系电话" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="电子邮箱" /></el-form-item>
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
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="添加一些备注信息..." /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存更改</el-button>
      </template>
    </el-dialog>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter, type LocationQuery } from 'vue-router'
import { Plus, Search, Phone, Monitor, Warning, Close } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  PlatformPageShellV2,
  PlatformSearchEntry,
  dispatchPlatformAction,
  getPlatformSearchPresentation,
  openPlatformSearch
} from '@/components/platform'
import { createSubject, deleteSubject, getSubject, listSubjects, updateSubject, type HealthSubject } from '@/api/health'
import { useRouteQueryListSync } from '@/composables/useRouteQueryListSync'

type SubjectRiskLevel = 'high' | 'medium' | 'low'

const riskLabelMap: Record<SubjectRiskLevel, string> = { high: '高关注', medium: '中关注', low: '低关注' }
const loading = ref(false)
const saving = ref(false)
const list = ref<HealthSubject[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const drawerVisible = ref(false) // 控制抽屉的开关
const formRef = ref<FormInstance>()
const selectedSubject = ref<HealthSubject | null>(null)
const route = useRoute()
const router = useRouter()
const searchPresentation = getPlatformSearchPresentation('subject')

const query = reactive({ pageNum: 1, pageSize: 10, subjectName: '', phonenumber: '', status: '' })
const applyRouteQuery = (routeQuery: LocationQuery) => {
  query.pageNum = 1
  query.subjectName = String(routeQuery.subjectName || routeQuery.keyword || '')
  query.phonenumber = String(routeQuery.phonenumber || '')
  query.status = String(routeQuery.status || '')
}

const initialForm = (): HealthSubject => ({ subjectName: '', nickName: '', phonenumber: '', email: '', age: undefined, sex: '0', status: '0', remark: '' })
const form = reactive<HealthSubject>(initialForm())
const rules: FormRules = {
  subjectName: [{ required: true, message: '请输入对象账号', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const getRiskLevel = (row: HealthSubject): SubjectRiskLevel => (row.status === '1' ? 'high' : Number(row.age || 0) >= 75 ? 'medium' : 'low')
const selectedRiskLevel = computed(() => (selectedSubject.value ? getRiskLevel(selectedSubject.value) : 'low'))

// Mocking some varied data based on risk for visual appeal
const deviceSummary = computed(() => {
  if (!selectedSubject.value) return '-'
  const risk = getRiskLevel(selectedSubject.value)
  if (risk === 'high') return '1 台设备断连'
  return risk === 'medium' ? '2 台设备在线' : '1 台设备在线'
})

const eventSummary = computed(() => {
  if (!selectedSubject.value) return '-'
  const risk = getRiskLevel(selectedSubject.value)
  if (risk === 'high') return '近24h 发现异常'
  return risk === 'medium' ? '存在心率波动' : '状态平稳'
})

const aiTags = computed(() => {
  if (!selectedSubject.value) return []
  const risk = getRiskLevel(selectedSubject.value)
  if (risk === 'high') return ['重点监护', '需要回访', '设备异常']
  if (risk === 'medium') return ['高龄对象', '建议跟踪睡眠', '血压波动']
  return ['状态稳定', '低风险', '活跃用户']
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

const handleRowClick = (row: HealthSubject) => { 
  selectedSubject.value = row;
  drawerVisible.value = true; // 点击行时打开抽屉
}

const handleCurrentChange = (row?: HealthSubject) => { 
  if (row) selectedSubject.value = row 
}

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
  if (selectedSubject.value?.subjectId === subjectId) drawerVisible.value = false
  fetchList()
}

const handleSearchClick = async () => { await openPlatformSearch(router, 'subject') }

const openDeviceLink = async () => {
  if (!selectedSubject.value) return
  drawerVisible.value = false // 导航前关闭抽屉
  await dispatchPlatformAction(router, '查看设备', {
    entities: { device: { kind: 'device', name: `${selectedSubject.value.nickName} 的设备`, query: { userId: String(selectedSubject.value.subjectId) } } }
  })
}

const openEventLink = async () => {
  if (!selectedSubject.value) return
  drawerVisible.value = false
  await dispatchPlatformAction(router, '查看事件', {
    entities: { event: { kind: 'event', name: `${selectedSubject.value.nickName} 的事件`, query: { userId: String(selectedSubject.value.subjectId) } } }
  })
}

const { install: installRouteQuerySync, syncSelectedAfterFetch } = useRouteQueryListSync<HealthSubject>({
  route, list, selected: selectedSubject, applyQuery: applyRouteQuery,
  resolveMatchedItem: ({ list, routeQuery, fallbackSelected }) => {
    const routeSubjectName = String(routeQuery.subjectName || routeQuery.keyword || '')
    if (routeSubjectName) {
      const matched = list.find((item) => item.subjectName === routeSubjectName || item.nickName === routeSubjectName || String(item.subjectId || '') === routeSubjectName)
      if (matched) return matched
    }
    if (fallbackSelected) return list.find((item) => item.subjectId === fallbackSelected.subjectId)
    return null
  },
  fetchList
})

installRouteQuerySync()
</script>

<style scoped lang="scss">
/* --- 页面整体架构 & 头部 --- */
.toolbar-stack { display: flex; flex-direction: column; gap: 16px; margin-bottom: 8px; }
.header-actions { display: flex; align-items: center; gap: 12px; }

.modern-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.search-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  
  .modern-input, .modern-select {
    width: 200px;
    :deep(.el-input__wrapper) {
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      border-radius: 8px;
    }
  }
}

/* --- 现代化卡片式表格 --- */
.modern-table-container {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  padding: 8px 16px 24px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  margin-top: 8px;
}

:deep(.modern-table) {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #ffffff;
  --el-table-header-text-color: #64748b;
  font-size: 14px;
  
  .el-table__inner-wrapper::before { display: none; } /* 去除底部多余的线 */
  
  th.el-table__cell {
    font-weight: 600;
    border-bottom: 2px solid #f1f5f9;
    padding: 12px 0;
  }
  
  td.el-table__cell {
    padding: 16px 0; /* 增加行高呼吸感 */
    border-bottom: 1px solid #f8fafc;
    transition: background-color 0.2s ease;
    cursor: pointer; /* 提示可点击 */
  }

  .el-table__row:hover td.el-table__cell {
    background-color: #f8fafc !important;
  }
}

/* 复合列：用户 Profile */
.user-cell {
  display: flex;
  align-items: center;
  gap: 14px;
  
  .user-avatar {
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    &.avatar-high { background: linear-gradient(135deg, #ef4444, #f87171); }
    &.avatar-medium { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
    &.avatar-low { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
  }
  
  .user-info {
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    .user-name {
      font-weight: 600;
      color: #0f172a;
      font-size: 14px;
      line-height: 1.4;
    }
    
    .user-id {
      font-size: 12px;
      color: #94a3b8;
    }
  }
}

/* 状态徽章 */
.risk-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  
  .dot {
    width: 6px; height: 6px; border-radius: 50%;
  }
  
  &.risk-high { color: #b91c1c; background: #fef2f2; .dot { background: #ef4444; } }
  &.risk-medium { color: #b45309; background: #fffbeb; .dot { background: #f59e0b; } }
  &.risk-low { color: #047857; background: #ecfdf5; .dot { background: #10b981; } }
}

.demographic-text, .phone-text {
  color: #475569;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* --- 侧边抽屉 360 视图 (核心重构) --- */
:deep(.modern-drawer) {
  .el-drawer__body { padding: 0; background: #f8fafc; }
}

.drawer-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.drawer-header {
  position: relative;
  padding: 40px 32px 32px;
  background: #ffffff;
  border-bottom: 1px solid #f1f5f9;
  
  .drawer-close {
    position: absolute;
    top: 20px; right: 20px;
    width: 32px; height: 32px;
    border-radius: 50%;
    background: #f1f5f9;
    display: flex; align-items: center; justify-content: center;
    cursor: pointer; color: #64748b;
    transition: all 0.2s;
    &:hover { background: #e2e8f0; color: #0f172a; }
  }
  
  .profile-hero {
    display: flex; flex-direction: column; align-items: center; text-align: center; gap: 12px;
    
    .hero-avatar {
      font-size: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); border: 4px solid #fff;
    }
    .hero-info {
      h2 { margin: 0; font-size: 20px; color: #0f172a; font-weight: 700; }
      p { margin: 4px 0 0; font-size: 14px; color: #64748b; }
    }
  }
}

.drawer-body {
  flex: 1; overflow-y: auto; padding: 24px 32px;
  display: flex; flex-direction: column; gap: 24px;
}

.metric-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
  
  .metric-card {
    background: #ffffff; border-radius: 16px; padding: 16px;
    display: flex; align-items: center; gap: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.02);
    border: 1px solid #f1f5f9; transition: transform 0.2s;
    
    &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
    
    .metric-icon {
      width: 40px; height: 40px; border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      font-size: 20px; color: #fff;
      &.bg-blue { background: linear-gradient(135deg, #60a5fa, #3b82f6); }
      &.bg-orange { background: linear-gradient(135deg, #fbbf24, #f59e0b); }
    }
    
    .metric-data {
      display: flex; flex-direction: column;
      .label { font-size: 12px; color: #64748b; margin-bottom: 2px; }
      .value { font-size: 16px; font-weight: 700; color: #0f172a; }
    }
  }
}

.info-section {
  background: #ffffff; border-radius: 16px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02); border: 1px solid #f1f5f9;
  
  .section-title {
    margin: 0 0 16px; font-size: 15px; font-weight: 600; color: #0f172a;
  }
}

.tag-cloud {
  display: flex; flex-wrap: wrap; gap: 8px;
  .modern-tag { border-radius: 8px; font-weight: 500; }
}

.info-list {
  display: flex; flex-direction: column; gap: 14px;
  .info-item {
    display: flex; justify-content: space-between; align-items: flex-start;
    font-size: 14px; line-height: 1.5;
    
    .info-label { color: #64748b; flex: 0 0 90px; }
    .info-value { color: #0f172a; font-weight: 500; text-align: right; }
    .text-green { color: #10b981; }
    .text-gray { color: #94a3b8; }
    .text-wrap { word-break: break-all; text-align: left; flex: 1; }
  }
}

.drawer-footer {
  padding: 20px 32px; background: #ffffff; border-top: 1px solid #f1f5f9;
  .full-btn { width: 100%; border-radius: 8px; height: 40px; font-weight: 600; }
}

.cursor-pointer { cursor: pointer; }
</style>