<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="电量" prop="batteryLevel" label-width="110px">
        <el-input
          v-model="queryParams.batteryLevel"
          placeholder="请输入电量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="步数" prop="step" label-width="110px">
        <el-input
          v-model="queryParams.step"
          placeholder="请输入步数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最近告警时间" prop="alarmTime" label-width="110px">
        <el-date-picker clearable
          v-model="queryParams.alarmTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择最近告警时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="体温" prop="temp" label-width="110px">
        <el-input
          v-model="queryParams.temp"
          placeholder="请输入体温"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="体温测量时间" prop="tempTime" label-width="110px">
        <el-date-picker clearable
          v-model="queryParams.tempTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择体温测量时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="心率" prop="heartRate" label-width="110px">
        <el-input
          v-model="queryParams.heartRate"
          placeholder="请输入心率"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="心率测量时间" prop="heartRateTime" label-width="110px">
        <el-date-picker clearable
          v-model="queryParams.heartRateTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择心率测量时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="血压" prop="bloodDiastolic" label-width="110px">
        <el-input
          v-model="queryParams.bloodDiastolic"
          placeholder="请输入血压"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="血压" prop="bloodSystolic" label-width="110px">
        <el-input
          v-model="queryParams.bloodSystolic"
          placeholder="请输入血压"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="血压测量时间" prop="bloodTime" label-width="110px">
        <el-date-picker clearable
          v-model="queryParams.bloodTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择血压测量时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="血氧" prop="spo2" label-width="110px">
        <el-input
          v-model="queryParams.spo2"
          placeholder="请输入血氧"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="血氧测量时间" prop="spo2Time" label-width="110px">
        <el-date-picker clearable
          v-model="queryParams.spo2Time"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择血氧测量时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="经度" prop="longitude" label-width="110px">
        <el-input
          v-model="queryParams.longitude"
          placeholder="请输入经度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="纬度" prop="latitude" label-width="110px">
        <el-input
          v-model="queryParams.latitude"
          placeholder="请输入纬度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="详细地址" prop="location" label-width="110px">
        <el-input
          v-model="queryParams.location"
          placeholder="请输入详细地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['health:deviceInfoExt:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['health:deviceInfoExt:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['health:deviceInfoExt:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['health:deviceInfoExt:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deviceInfoExtList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="deviceId" />
      <el-table-column label="读取时间" align="center" prop="updateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="电量" align="center" prop="batteryLevel" />
      <el-table-column label="步数" align="center" prop="step" />
      <el-table-column label="最近告警内容" align="center" prop="alarmContent" />
      <el-table-column label="最近告警时间" align="center" prop="alarmTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.alarmTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="体温" align="center" prop="temp" />
      <el-table-column label="体温测量时间" align="center" prop="tempTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.tempTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="心率" align="center" prop="heartRate" />
      <el-table-column label="心率测量时间" align="center" prop="heartRateTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.heartRateTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="血压" align="center" prop="bloodDiastolic" />
      <el-table-column label="血压" align="center" prop="bloodSystolic" />
      <el-table-column label="血压测量时间" align="center" prop="bloodTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.bloodTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="血氧" align="center" prop="spo2" />
      <el-table-column label="血氧测量时间" align="center" prop="spo2Time" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.spo2Time, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="经度" align="center" prop="longitude" />
      <el-table-column label="纬度" align="center" prop="latitude" />
      <el-table-column label="详细地址" align="center" prop="location" />
      <el-table-column label="定位方式" align="center" prop="type" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['health:deviceInfoExt:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['health:deviceInfoExt:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改设备信息扩展对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="电量" prop="batteryLevel">
          <el-input v-model="form.batteryLevel" placeholder="请输入电量" />
        </el-form-item>
        <el-form-item label="步数" prop="step">
          <el-input v-model="form.step" placeholder="请输入步数" />
        </el-form-item>
        <el-form-item label="最近告警内容">
          <el-input v-model="form.alarmContent" :min-height="192"/>
        </el-form-item>
        <el-form-item label="最近告警时间" prop="alarmTime">
          <el-date-picker clearable
            v-model="form.alarmTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择最近告警时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="体温" prop="temp">
          <el-input v-model="form.temp" placeholder="请输入体温" />
        </el-form-item>
        <el-form-item label="体温测量时间" prop="tempTime">
          <el-date-picker clearable
            v-model="form.tempTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择体温测量时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="心率" prop="heartRate">
          <el-input v-model="form.heartRate" placeholder="请输入心率" />
        </el-form-item>
        <el-form-item label="心率测量时间" prop="heartRateTime">
          <el-date-picker clearable
            v-model="form.heartRateTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择心率测量时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="血压" prop="bloodDiastolic">
          <el-input v-model="form.bloodDiastolic" placeholder="请输入血压" />
        </el-form-item>
        <el-form-item label="血压" prop="bloodSystolic">
          <el-input v-model="form.bloodSystolic" placeholder="请输入血压" />
        </el-form-item>
        <el-form-item label="血压测量时间" prop="bloodTime">
          <el-date-picker clearable
            v-model="form.bloodTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择血压测量时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="血氧" prop="spo2">
          <el-input v-model="form.spo2" placeholder="请输入血氧" />
        </el-form-item>
        <el-form-item label="血氧测量时间" prop="spo2Time">
          <el-date-picker clearable
            v-model="form.spo2Time"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择血氧测量时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model="form.longitude" placeholder="请输入经度" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model="form.latitude" placeholder="请输入纬度" />
        </el-form-item>
        <el-form-item label="详细地址" prop="location">
          <el-input v-model="form.location" placeholder="请输入详细地址" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDeviceInfoExt, getDeviceInfoExt, delDeviceInfoExt, addDeviceInfoExt, updateDeviceInfoExt } from "@/api/health/deviceInfoExt";

export default {
  name: "DeviceInfoExt",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 设备信息扩展表格数据
      deviceInfoExtList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        batteryLevel: null,
        step: null,
        alarmContent: null,
        alarmTime: null,
        temp: null,
        tempTime: null,
        heartRate: null,
        heartRateTime: null,
        bloodDiastolic: null,
        bloodSystolic: null,
        bloodTime: null,
        spo2: null,
        spo2Time: null,
        longitude: null,
        latitude: null,
        location: null,
        type: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询设备信息扩展列表 */
    getList() {
      this.loading = true;
      listDeviceInfoExt(this.queryParams).then(response => {
        this.deviceInfoExtList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        deviceId: null,
        updateTime: null,
        batteryLevel: null,
        step: null,
        alarmContent: null,
        alarmTime: null,
        temp: null,
        tempTime: null,
        heartRate: null,
        heartRateTime: null,
        bloodDiastolic: null,
        bloodSystolic: null,
        bloodTime: null,
        spo2: null,
        spo2Time: null,
        longitude: null,
        latitude: null,
        location: null,
        type: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.deviceId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加设备信息扩展";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const deviceId = row.deviceId || this.ids
      getDeviceInfoExt(deviceId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改设备信息扩展";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.deviceId != null) {
            updateDeviceInfoExt(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDeviceInfoExt(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const deviceIds = row.deviceId || this.ids;
      this.$modal.confirm('是否确认删除设备信息扩展编号为"' + deviceIds + '"的数据项？').then(function() {
        return delDeviceInfoExt(deviceIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('health/deviceInfoExt/export', {
        ...this.queryParams
      }, `deviceInfoExt_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
