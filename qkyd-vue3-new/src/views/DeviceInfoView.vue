<template>
  <PlatformPageShell
    title="设备中心"
    subtitle="统一承载设备运营状态、绑定关系、健康度和异常线索，为后续设备运营工作台打下结构基础。"
    eyebrow="DEVICE CENTER"
    aside-title="设备运营预览"
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
          <el-input v-model="query.name" placeholder="设备名称" clearable style="width: 220px" />
          <el-input v-model="query.imei" placeholder="IMEI" clearable style="width: 220px" />
          <el-input v-model="query.userId" placeholder="绑定对象ID" clearable style="width: 160px" />
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="openCreate">新增设备</el-button>
        </div>
      </div>
    </template>

    <div class="panel section">
      <el-table v-loading="loading" :data="list" stripe highlight-current-row @current-change="handleCurrentChange" @row-click="handleRowSelect">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="设备名称" min-width="150" />
        <el-table-column prop="imei" label="IMEI" min-width="180" />
        <el-table-column prop="type" label="设备类型" min-width="140" />
        <el-table-column label="在线状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="getOnlineStatus(row) === 'online' ? 'success' : 'warning'" effect="light">{{ getOnlineStatus(row) === 'online' ? '在线' : '离线' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定对象" min-width="120">
          <template #default="{ row }">{{ getBoundSubjectLabel(row) }}</template>
        </el-table-column>
        <el-table-column label="健康度" min-width="100">
          <template #default="{ row }"><el-progress :percentage="getHealthScore(row)" :stroke-width="8" :show-text="false" /></template>
        </el-table-column>
        <el-table-column label="最近上传" min-width="130">
          <template #default="{ row }">{{ getLastUpload(row) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row.id)">编辑</el-button>
            <el-button text @click="handleRowSelect(row)">查看运营</el-button>
            <el-button text type="danger" @click="removeItem(row.id)">删除</el-button>
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
              <div class="aside-card-title">当前选中设备</div>
              <p class="detail-subtitle">第一版设备运营预览聚焦状态、绑定对象、健康度和后续动作。</p>
            </div>
            <el-tag v-if="selectedDevice" :type="getOnlineStatus(selectedDevice) === 'online' ? 'success' : 'warning'" effect="light">{{ getOnlineStatus(selectedDevice) === 'online' ? '在线' : '离线' }}</el-tag>
          </div>

          <div v-if="selectedDevice" class="detail-body">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="设备ID">{{ selectedDevice.id || '-' }}</el-descriptions-item>
              <el-descriptions-item label="设备名称">{{ selectedDevice.name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="IMEI">{{ selectedDevice.imei || '-' }}</el-descriptions-item>
              <el-descriptions-item label="设备类型">{{ selectedDevice.type || '-' }}</el-descriptions-item>
              <el-descriptions-item label="绑定对象">{{ getBoundSubjectLabel(selectedDevice) }}</el-descriptions-item>
              <el-descriptions-item label="最近上传">{{ getLastUpload(selectedDevice) }}</el-descriptions-item>
            </el-descriptions>

            <div class="detail-grid">
              <div class="mini-card">
                <span class="mini-label">健康度</span>
                <strong class="mini-value">{{ getHealthScore(selectedDevice) }}%</strong>
              </div>
              <div class="mini-card">
                <span class="mini-label">规则状态</span>
                <strong class="mini-value">{{ getRuleStatus(selectedDevice) }}</strong>
              </div>
            </div>

            <div class="tag-block">
              <div class="block-title">运营标签</div>
              <div class="tag-list">
                <el-tag v-for="item in operationTags" :key="item" effect="light" round>{{ item }}</el-tag>
              </div>
            </div>

            <div class="workflow-actions">
              <el-button type="primary" @click="openEdit(selectedDevice.id)">编辑设备</el-button>
              <el-button @click="openSubjectLink">查看对象</el-button>
              <el-button @click="openRulePlaceholder">查看规则</el-button>
            </div>
          </div>

          <div v-else class="empty-detail">
            <div class="empty-title">尚未选中设备</div>
            <p>从左侧列表中选择一个设备后，这里会展示第一版设备运营预览内容。</p>
          </div>
        </div>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑设备' : '新增设备'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="设备名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="IMEI" prop="imei"><el-input v-model="form.imei" /></el-form-item>
        <el-form-item label="设备类型"><el-input v-model="form.type" /></el-form-item>
        <el-form-item label="绑定用户ID"><el-input-number v-model="form.userId" :min="1" /></el-form-item>
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
  openPlatformSearch
} from '@/components/platform'
import { createDevice, deleteDevice, getDevice, listDevices, updateDevice, type DeviceInfo } from '@/api/health'
import { useRouteQueryListSync } from '@/composables/useRouteQueryListSync'

type OnlineStatus = 'online' | 'offline'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const list = ref<DeviceInfo[]>([])
const total = ref(0)
const selectedDevice = ref<DeviceInfo | null>(null)
const route = useRoute()
const router = useRouter()
const searchPresentation = getPlatformSearchPresentation('device')

const query = reactive({ pageNum: 1, pageSize: 10, name: '', imei: '', userId: '' })
const applyRouteQuery = (routeQuery: LocationQuery) => {
  query.pageNum = 1
  query.name = String(routeQuery.name || routeQuery.keyword || '')
  query.imei = String(routeQuery.imei || '')
  query.userId = String(routeQuery.userId || '')
}

const initialForm = (): DeviceInfo => ({ name: '', imei: '', type: '', userId: undefined })
const form = reactive<DeviceInfo>(initialForm())
const rules: FormRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  imei: [{ required: true, message: '请输入 IMEI', trigger: 'blur' }]
}

const getOnlineStatus = (row: DeviceInfo): OnlineStatus => (Number(row.id || 0) % 3 === 0 ? 'offline' : 'online')
const getHealthScore = (row: DeviceInfo) => {
  const base = 78 + (Number(row.id || 0) % 18)
  return getOnlineStatus(row) === 'online' ? base : Math.max(42, base - 28)
}
const getLastUpload = (row: DeviceInfo) => (getOnlineStatus(row) === 'offline' ? '2 小时前' : Number(row.id || 0) % 2 === 0 ? '5 分钟前' : '刚刚')
const getBoundSubjectLabel = (row: DeviceInfo) => (!row.userId ? '未绑定对象' : `对象 #${row.userId}`)
const getRuleStatus = (row: DeviceInfo) => (getOnlineStatus(row) === 'online' ? '规则正常' : '待校验')
const operationTags = computed(() => {
  if (!selectedDevice.value) return []
  const tags = [selectedDevice.value.userId ? '已绑定' : '待绑定']
  if (getOnlineStatus(selectedDevice.value) === 'offline') tags.push('离线预警', '建议巡检')
  else tags.push('运行稳定')
  if (getHealthScore(selectedDevice.value) < 70) tags.push('健康度偏低')
  return tags
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listDevices(query)
    list.value = (res.rows || []) as DeviceInfo[]
    total.value = Number(res.total || 0)
    syncSelectedAfterFetch()
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
const resetForm = () => Object.assign(form, initialForm())
const openCreate = () => { resetForm(); dialogVisible.value = true }
const handleRowSelect = (row: DeviceInfo) => { selectedDevice.value = row }
const handleCurrentChange = (row?: DeviceInfo) => { if (row) selectedDevice.value = row }
const openEdit = async (id?: number) => {
  if (!id) return
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
const removeItem = async (id?: number) => {
  if (!id) return
  await ElMessageBox.confirm('确认删除该设备吗？', '提示', { type: 'warning' })
  await deleteDevice([id])
  ElMessage.success('删除成功')
  fetchList()
}
const handleSearchClick = async () => {
  await openPlatformSearch(router, 'device')
}
const openSubjectLink = async () => {
  if (!selectedDevice.value?.userId) return ElMessage.info('当前设备暂无绑定对象')
  await dispatchPlatformAction(router, '查看对象', {
    entities: {
      subject: {
        kind: 'subject',
        name: `对象 #${selectedDevice.value.userId}`,
        query: { keyword: String(selectedDevice.value.userId) }
      }
    }
  })
}
const openRulePlaceholder = async () => {
  await dispatchPlatformAction(router, '查看规则')
}

const { install: installRouteQuerySync, syncSelectedAfterFetch } = useRouteQueryListSync<DeviceInfo>({
  route,
  list,
  selected: selectedDevice,
  applyQuery: applyRouteQuery,
  resolveMatchedItem: ({ list, routeQuery, fallbackSelected }) => {
    const routeDeviceId = String(routeQuery.deviceId || '')
    const routeDeviceName = String(routeQuery.name || routeQuery.keyword || '')
    const routeImei = String(routeQuery.imei || '')

    const matched = list.find(
      (item) =>
        (routeDeviceId && String(item.id || '') === routeDeviceId) ||
        (routeDeviceName && item.name === routeDeviceName) ||
        (routeImei && item.imei === routeImei)
    )
    if (matched) return matched

    if (fallbackSelected) {
      return list.find((item) => item.id === fallbackSelected.id)
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
