<template>
  <PlatformPageShellV2
    title="维护记录"
    subtitle="追踪每台设备的固件版本、维修历史和操作日志，保障设备全生命周期的运营可见性。"
    eyebrow="DEVICE MAINTENANCE"
    status-note="演示版 · 当前使用模拟数据"
    status-tone="warning"
  >
    <template #toolbar>
      <div class="toolbar-row">
        <el-input v-model="query.deviceName" placeholder="设备名称 / IMEI" prefix-icon="Search" clearable style="width: 240px" />
        <el-select v-model="query.type" placeholder="维护类型" clearable style="width: 160px">
          <el-option label="固件升级" value="firmware" />
          <el-option label="电池更换" value="battery" />
          <el-option label="维修检修" value="repair" />
          <el-option label="例行巡检" value="inspect" />
        </el-select>
        <el-date-picker v-model="query.date" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 240px" />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" :icon="Plus" @click="dialogVisible = true">新增记录</el-button>
      </div>
    </template>

    <div class="main-layout">
      <!-- 维护统计 -->
      <div class="maintenance-stats">
        <div v-for="s in stats" :key="s.label" class="ms-card">
          <span class="ms-num" :style="{ color: s.color }">{{ s.value }}</span>
          <span class="ms-label">{{ s.label }}</span>
        </div>
      </div>

      <!-- 记录表格 -->
      <div class="panel-card">
        <div class="panel-header">
          <span class="panel-title">维护操作记录</span>
          <el-tag type="info" size="small">本月共 {{ records.length }} 条</el-tag>
        </div>
        <el-table :data="filteredRecords" stripe style="width: 100%">
          <el-table-column type="index" width="50" label="#" />
          <el-table-column label="设备信息" min-width="200">
            <template #default="{ row }">
              <div class="device-cell">
                <span class="dc-name">{{ row.deviceName }}</span>
                <span class="dc-imei">{{ row.imei }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="维护类型" width="120">
            <template #default="{ row }">
              <el-tag :type="typeConfig[row.type]?.tagType" size="small" effect="light">
                {{ typeConfig[row.type]?.label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人员" width="100" />
          <el-table-column prop="date" label="操作时间" min-width="160" />
          <el-table-column label="固件版本" width="120">
            <template #default="{ row }">
              <span class="version-badge" v-if="row.firmware">{{ row.firmware }}</span>
              <span class="text-muted" v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="结果" width="90">
            <template #default="{ row }">
              <el-tag :type="row.result === 'success' ? 'success' : 'danger'" size="small" effect="light">
                {{ row.result === 'success' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="page" :page-size="10" :total="filteredRecords.length" background layout="total, prev, pager, next" />
        </div>
      </div>
    </div>

    <!-- 新增记录对话框 -->
    <el-dialog v-model="dialogVisible" title="新增维护记录" width="520px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="设备名称"><el-input v-model="form.deviceName" placeholder="输入设备名称" /></el-form-item>
        <el-form-item label="IMEI"><el-input v-model="form.imei" placeholder="设备 IMEI 号" /></el-form-item>
        <el-form-item label="维护类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="固件升级" value="firmware" />
            <el-option label="电池更换" value="battery" />
            <el-option label="维修检修" value="repair" />
            <el-option label="例行巡检" value="inspect" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人员"><el-input v-model="form.operator" /></el-form-item>
        <el-form-item label="固件版本"><el-input v-model="form.firmware" placeholder="如 v2.1.3（非固件升级可留空）" /></el-form-item>
        <el-form-item label="操作结果">
          <el-radio-group v-model="form.result">
            <el-radio value="success">成功</el-radio>
            <el-radio value="fail">失败</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </PlatformPageShellV2>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { PlatformPageShellV2 } from '@/components/platform'

const page = ref(1)
const dialogVisible = ref(false)
const query = reactive({ deviceName: '', type: '', date: null as any })
const form = reactive({ deviceName: '', imei: '', type: 'inspect', operator: '', firmware: '', result: 'success', remark: '' })

const typeConfig: Record<string, { label: string; tagType: string }> = {
  firmware: { label: '固件升级', tagType: 'primary' },
  battery:  { label: '电池更换', tagType: 'warning' },
  repair:   { label: '维修检修', tagType: 'danger' },
  inspect:  { label: '例行巡检', tagType: 'success' },
}

const records = ref([
  { id: 1, deviceName: '耆康手环-A01', imei: '861234567890001', type: 'firmware', operator: '张工', date: '2026-03-20 09:15', firmware: 'v2.3.1', result: 'success', remark: '例行版本升级，修复睡眠检测精度问题' },
  { id: 2, deviceName: '耆康手环-A02', imei: '861234567890002', type: 'battery', operator: '李工', date: '2026-03-18 14:30', firmware: '', result: 'success', remark: '电池电量低于10%，已更换新电池' },
  { id: 3, deviceName: '心率贴-B01',   imei: '861234567890003', type: 'repair',  operator: '王工', date: '2026-03-15 10:00', firmware: '', result: 'success', remark: '传感器接触不良，已重新焊接' },
  { id: 4, deviceName: '耆康手环-A03', imei: '861234567890004', type: 'inspect', operator: '张工', date: '2026-03-10 11:00', firmware: '', result: 'success', remark: '本月例行巡检，状态正常' },
  { id: 5, deviceName: '血压仪-D01',   imei: '861234567890008', type: 'firmware', operator: '陈工', date: '2026-03-08 16:20', firmware: 'v1.5.0', result: 'fail', remark: '升级失败，设备重启后恢复旧版本，待跟进' },
  { id: 6, deviceName: '睡眠监测仪-C01', imei: '861234567890005', type: 'inspect', operator: '李工', date: '2026-03-05 09:00', firmware: '', result: 'success', remark: '例行巡检' },
])

const stats = [
  { label: '本月维护总次', value: records.value.length, color: '#3b82f6' },
  { label: '成功', value: records.value.filter(r => r.result === 'success').length, color: '#10b981' },
  { label: '失败待跟进', value: records.value.filter(r => r.result === 'fail').length, color: '#ef4444' },
  { label: '固件升级', value: records.value.filter(r => r.type === 'firmware').length, color: '#8b5cf6' },
]

const filteredRecords = computed(() => {
  let res = records.value
  if (query.deviceName) res = res.filter(r => r.deviceName.includes(query.deviceName) || r.imei.includes(query.deviceName))
  if (query.type) res = res.filter(r => r.type === query.type)
  return res
})

const handleSearch = () => {}
const handleReset = () => { query.deviceName = ''; query.type = ''; query.date = null }
const submitForm = () => {
  records.value.unshift({ id: Date.now(), ...form, date: new Date().toLocaleString('zh-CN') })
  dialogVisible.value = false
  ElMessage.success('维护记录已添加')
  Object.assign(form, { deviceName: '', imei: '', type: 'inspect', operator: '', firmware: '', result: 'success', remark: '' })
}
</script>

<style scoped lang="scss">
.toolbar-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }

.main-layout { display: flex; flex-direction: column; gap: 20px; }

.maintenance-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.ms-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  display: flex; flex-direction: column; align-items: flex-start; gap: 6px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.ms-num { font-size: 36px; font-weight: 800; line-height: 1; }
.ms-label { font-size: 13px; color: #64748b; }

.panel-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(226,232,240,0.7);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  overflow: hidden;
}
.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #f1f5f9;
}
.panel-title { font-size: 14px; font-weight: 700; color: #0f172a; }

.device-cell { display: flex; flex-direction: column; gap: 2px; }
.dc-name { font-size: 14px; font-weight: 600; color: #0f172a; }
.dc-imei { font-size: 11px; color: #94a3b8; font-family: monospace; }

.version-badge {
  display: inline-block; padding: 2px 8px; background: #f3e8ff; color: #7c3aed;
  border-radius: 6px; font-size: 12px; font-weight: 600; font-family: monospace;
}
.text-muted { color: #94a3b8; }

.pagination { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid #f1f5f9; }

@media (max-width: 1024px) {
  .maintenance-stats { grid-template-columns: repeat(2, 1fr); }
}
</style>
