<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
<!--      <el-form-item label="姓名" prop="nickName">-->
<!--        <el-input-->
<!--          v-model="queryParams.nickName"-->
<!--          placeholder="请输入姓名"-->
<!--          clearable-->
<!--          @keyup.enter.native="handleQuery"-->
<!--        />-->
<!--      </el-form-item>-->
<!--      <el-form-item label="手机号" prop="phone">-->
<!--        <el-input-->
<!--          v-model="queryParams.phone"-->
<!--          placeholder="请输入手机号"-->
<!--          clearable-->
<!--          @keyup.enter.native="handleQuery"-->
<!--        />-->
<!--      </el-form-item>-->
      <el-form-item label="IMEI" prop="imei">
        <el-input
          v-model="queryParams.imei"
          placeholder="请输入IMEI信息"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <!--      <el-form-item label="序列号" prop="serialNumber">-->
      <!--        <el-input-->
      <!--          v-model="queryParams.serialNumber"-->
      <!--          placeholder="请输入序列号"-->
      <!--          clearable-->
      <!--          @keyup.enter.native="handleQuery"-->
      <!--        />-->
      <!--      </el-form-item>-->
      <!--      <el-form-item label="状态" prop="wearState">-->
      <!--        <el-select v-model="queryParams.wearState" placeholder="请选择状态" clearable>-->
      <!--          <el-option-->
      <!--            v-for="dict in dict.type.data_wear_state"-->
      <!--            :key="dict.value"-->
      <!--            :label="dict.label"-->
      <!--            :value="dict.value"-->
      <!--          />-->
      <!--        </el-select>-->
      <!--      </el-form-item>-->
      <!--      <el-form-item label="设备类型" prop="type">-->
      <!--        <el-select v-model="queryParams.type" placeholder="请选择设备类型" clearable>-->
      <!--          <el-option-->
      <!--            v-for="dict in dict.type.data_device_type"-->
      <!--            :key="dict.value"-->
      <!--            :label="dict.label"-->
      <!--            :value="dict.value"-->
      <!--          />-->
      <!--        </el-select>-->
      <!--      </el-form-item>-->
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['data:device:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['data:device:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" align="center" prop="deviceId"/>
      <el-table-column label="姓名" align="center" prop="nickName"/>
      <el-table-column label="手机号" align="center" prop="phone">
        <template slot-scope="scope">
          <el-link type="primary" :underline="false" @click="handleDataBoard(scope.row)">{{scope.row.phone}}</el-link>
        </template>
      </el-table-column>
      <!--<el-table-column label="系统软件版本号" align="center" prop="sysVersion"/>
      <el-table-column label="Wifi地址" align="center" prop="wifiAddress"/>
      <el-table-column label="蓝牙MAC地址" align="center" prop="bluetoothAddress"/>
      <el-table-column label="ipv4地址" align="center" prop="ipv4"/>
      <el-table-column label="ipv6地址" align="center" prop="ipv6"/>
      <el-table-column label="入网模式" align="center" prop="networkAccessMode" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.data_network_access_mode" :value="scope.row.networkAccessMode"/>
        </template>
      </el-table-column>-->
      <!--      <el-table-column label="序列号" align="center" prop="serialNumber"/>-->
      <!--<el-table-column label="设备名称" align="center" prop="deviceName"/>-->
      <el-table-column label="IMEI信息" align="center" prop="imei"/>
<!--      <el-table-column label="状态" align="center" prop="wearState">-->
<!--        <template slot-scope="scope">-->
<!--          <dict-tag :options="dict.type.data_wear_state" :value="scope.row.wearState"/>-->
<!--        </template>-->
<!--      </el-table-column>-->
      <el-table-column label="电量" align="center" prop="batteryLevel"/>
      <el-table-column label="设备类型" align="center" prop="type">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.device_type" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="读取时间" align="center" prop="readTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.readTime) }}</span>
        </template>
      </el-table-column>
<!--      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">-->
<!--        <template slot-scope="scope">-->
<!--          <el-button-->
<!--            size="mini"-->
<!--            type="text"-->
<!--            icon="el-icon-edit"-->
<!--            @click="handleFrequency(scope.row)"-->
<!--            v-hasPermi="['data:device:frequency']"-->
<!--          >采集频率-->
<!--          </el-button>-->
<!--          <el-button-->
<!--            size="mini"-->
<!--            type="text"-->
<!--            icon="el-icon-user"-->
<!--            @click="handleContact(scope.row)"-->
<!--            v-hasPermi="['data:device:contact']"-->
<!--          >紧急联系人-->
<!--          </el-button>-->
<!--        </template>-->
<!--      </el-table-column>-->
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改数据采集频率对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="心率测量频率" prop="heartRate">
          <el-input-number v-model="form.heartRate" :min="1" :max="200" label="请输入心率测量频率"></el-input-number>
        </el-form-item>
        <el-form-item label="血氧测量频率" prop="spo2">
          <el-input-number v-model="form.spo2" :min="1" :max="200" label="请输入血氧测量频率"></el-input-number>
        </el-form-item>
        <el-form-item label="体温测量频率" prop="temperature">
          <el-input-number v-model="form.temperature" :min="1" :max="200" label="请输入体温测量频率"></el-input-number>
        </el-form-item>
        <el-form-item label="定位数据采集频率" prop="gps">
          <el-input-number v-model="form.gps" :min="1" :max="200" label="请输入定位数据采集频率"></el-input-number>
        </el-form-item>
        <el-form-item label="其他数据采集频率" prop="other">
          <el-input-number v-model="form.other" :min="1" :max="200" label="请输入其他数据采集频率"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFormFrequency">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 新增设备给用户对话框 -->
    <el-dialog :title="title" :visible.sync="apen" width="800px" append-to-body>
      <el-form :model="queryParamsWithout" ref="queryFormWithout" size="small" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="IMEI" prop="imei">
          <el-input
            v-model="queryParamsWithout.imei"
            placeholder="请输入IMEI信息"
            clearable
            @keyup.enter.native="handleQueryWithout"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQueryWithout">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQueryWithout">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="deviceInfoList" @selection-change="handleSelectionChangeDevice">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" align="center" prop="deviceId" />
        <el-table-column property="name" label="设备名称" width="200"></el-table-column>
        <el-table-column property="imei" label="IMEI信息" width="200"></el-table-column>
        <el-table-column property="type" label="设备型号" width="200">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.device_type" :value="scope.row.type"/>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="totalDevice > 0"
        :total="totalDevice"
        :page.sync="queryParamsWithout.pageNum"
        :limit.sync="queryParamsWithout.pageSize"
        @pagination="getWithoutList"
      />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitDeviceForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  // import {listFrequency, getFrequency, delFrequency, addFrequency, updateFrequency} from "@/api/data/frequency";
  import { getUser, addDeviceToUser, listDevice, deviceListWithoutUser } from '@/api/system/user'


  export default {
    name: 'Device',
    dicts: ['device_type'],
    // dicts: ['data_network_access_mode', 'data_charging_status', 'data_device_type', 'data_wear_state', 'device_type'],
    data() {
      return {
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        deviceIds: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        totalDevice: 0,
        // 设备信息表格数据
        deviceList: [],
        // 弹出层标题
        title: '',
        // 设备信息表格数据
        deviceInfoList: [],
        // 是否显示弹出层
        open: false,
        // 给用户新增设备
        apen: false,
        nickName: null,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          id: null,
          userId: this.$route.params && this.$route.params.userId,
          phone: null,
          sysVersion: null,
          wifiAddress: null,
          bluetoothAddress: null,
          ipv4: null,
          ipv6: null,
          networkAccessMode: null,
          serialNumber: null,
          deviceName: null,
          imei: null,
          charging: null,
          batteryLevel: null,
          readTime: null,
          stepsTime: null,
          sleepTime: null,
          wearState: this.$route.params && this.$route.params.wearState,
          nickName: null
        },
        //没有分配主人的设备的查询参数
        queryParamsWithout: {
          pageNum: 1,
          pageSize: 10,
          imei: null
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          heartRate: [
            {
              required: true,
              message: '请输入心率测量频率',
              trigger: 'blur'
            }
          ],
          spo2: [
            {
              required: true,
              message: '请输入血氧测量频率',
              trigger: 'blur'
            }
          ],
          temperature: [
            {
              required: true,
              message: '请输入体温测量频率',
              trigger: 'blur'
            }
          ],
          gps: [
            {
              required: true,
              message: '请输入定位数据采集频率',
              trigger: 'blur'
            }
          ],
          other: [
            {
              required: true,
              message: '请输入其他数据采集频率',
              trigger: 'blur'
            }
          ]
        }
      }
    },
    created() {
      this.getList()
      getUser(this.$route.params && this.$route.params.userId).then(response => {
        this.nickName = response.data.nickName;
      });
    },
    methods: {
      /** 查询设备信息列表 */
      getList() {
        this.loading = true
        listDevice(this.queryParams).then(response => {
          this.deviceList = response.rows
          this.total = response.total
          this.loading = false
        })
      },
      getWithoutList() {
        deviceListWithoutUser(this.queryParamsWithout).then(response => {
          this.deviceInfoList = response.rows
          this.totalDevice = response.total
          this.loading = false
        })
      },
      // 取消按钮
      cancel() {
        this.open = false
        this.apen = false
        this.reset()
        this.resetDevice()
        this.resetWithout()
      },
      // 表单重置
      reset() {
        this.form = {
          deviceId: null,
          heartRate: null,
          spo2: null,
          temperature: null,
          gps: null,
          other: null,
          createTime: null,
          updateTime: null
        }
        this.resetForm('form')
      },
      resetWithout() {
        this.queryParamsWithout.pageNum = 1
        this.queryParamsWithout.pageSize = 10
        this.queryParamsWithout.imei = null
      },
      // 表单重置
      resetDevice() {
        this.form = {
          deviceId: null,
          heartRate: null,
          spo2: null,
          temperature: null,
          gps: null,
          other: null,
          createTime: null,
          updateTime: null
        }
        this.resetForm('form')
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.pageNum = 1
        this.getList()
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.resetForm('queryForm')
        this.handleQuery()
      },
      /** 搜索按钮操作 没有主人的设备*/
      handleQueryWithout(){
        this.queryParamsWithout.pageNum = 1
        this.getWithoutList()
      },
      /** 重置按钮操作 */
      resetQueryWithout() {
        this.resetForm('queryFormWithout')
        this.handleQueryWithout()
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.id)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      // 多选框选中数据
      handleSelectionChangeDevice(selection) {
        this.deviceIds = selection.map(item => item.deviceId)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 客户管理 */
      handleCustomer(row) {
        const phonenumber = row.phone
        this.$router.push({
          name: 'CustomerSearch',
          params: {
            phonenumber: phonenumber
          }
        })
      },
      /** 健康数据看板 */
      handleDataBoard: function(row) {
        const userId = row.userId
        this.$router.push('/user-dataBoard/' + userId)
      },
      /** 数据采集频率 */
      handleFrequency(row) {
        this.reset()
        const deviceId = row.id || this.ids
        getFrequency(deviceId).then(response => {
          this.form = response.data
          this.open = true
          this.title = '修改数据采集频率'
        })
      },
      /** 紧急联系人 */
      handleContact(row) {
        const userId = row.userId
        const deviceId = row.id
        const type = row.type
        this.$router.push('/device-contact/user/' + userId + '/device/' + deviceId + '/type/' + type)
      },
      /** 提交按钮 */
      submitFormFrequency() {
        this.$refs['form'].validate(valid => {
          if (valid) {
            if (this.form.deviceId != null) {
              updateFrequency(this.form).then(response => {
                this.$modal.msgSuccess('修改成功')
                this.open = false
                this.getList()
              })
            } else {
              this.$modal.msgError('修改失败')
            }
          }
        })
      },
      submitDeviceForm(row) {
        const deviceIds = row.id || this.deviceIds;
        if (deviceIds > 0 || deviceIds.length > 0) {
          const userId = this.queryParams.userId;
          const submitDeviceParams = {
            deviceIds: deviceIds.join(','),
            userId: userId
          }
          this.$modal.confirm('是否确定把编号为"' + deviceIds + '"的设备分配给"' + this.nickName + '"？').then(function() {
            return addDeviceToUser(submitDeviceParams);
          }).then(() => {
            this.getList();
            this.$modal.msgSuccess("新增成功");
          }).catch(() => {
          });
          this.apen = false
          this.resetWithout()
        } else {
          this.$modal.msgError("请选择要新增给该用户的设备");
        }
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('data/export', {
          ...this.queryParams
        }, `device_${new Date().getTime()}.xlsx`)
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.loading = true;
        this.resetDevice()
        deviceListWithoutUser(this.queryParamsWithout).then(response => {
          this.deviceInfoList = response.rows
          this.totalDevice = response.total
          this.loading = false
        })
        this.apen = true
        this.title = '新增设备给用户'
      }
    }
  }
</script>
