<template>
  <div class="app-container">

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="围栏名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入围栏名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
<!--    <div style="margin-left: 20px; margin-right: 20px;">-->
<!--      <h2>用户信息</h2>-->
<!--      <table>-->
<!--        <tr>-->
<!--          <td style="margin-left: 10px;">设备类型：</td>-->
<!--          <td style="width: 400px;">-->
<!--            <dict-tag :options="dict.type.device_type" :value="deviceInfo.type"/>-->
<!--          </td>-->
<!--          <td>IMEI：</td>-->
<!--          <td style="width: 400px;">{{ deviceInfo.imei }}</td>-->
<!--        </tr>-->
<!--        <tr>-->
<!--          <td style="margin-left: 10px;">设备名称：</td>-->
<!--          <td style="width: 400px;">{{ deviceInfo.deviceName }}</td>-->
<!--&lt;!&ndash;          <td>在线状态：</td>&ndash;&gt;-->
<!--&lt;!&ndash;          <td style="width: 700px;">{{ deviceInfo.createTime }}</td>&ndash;&gt;-->
<!--        </tr>-->
<!--      </table>-->
<!--    </div>-->
<!--    <hr style="background-color: gray; margin-top: 20px;margin-bottom: 3px;">-->
    <div>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            size="mini"
            @click="handleAdd"
            v-hasPermi="['health:fence:add']"
          >新增围栏
          </el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="fenceList" @selection-change="handleSelectionChange">
        <el-table-column label="围栏名称" align="center" prop="name"/>
        <el-table-column label="半径(米)" align="center" prop="radius"/>
        <el-table-column label="报警类型" align="center" prop="warnType">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.fence_warn_type" :value="scope.row.warnType"/>
          </template>
        </el-table-column>
        <el-table-column label="是否启用" align="center" prop="status">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="1"
              inactive-value="2"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['health:fence:edit']"
            >修改
            </el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['health:fence:remove']"
            >删除
            </el-button>
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

<!--      &lt;!&ndash; 添加或修改围栏对话框 &ndash;&gt;-->
<!--      <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>-->
<!--        <el-form ref="form" :model="form" :rules="rules" label-width="80px">-->
<!--          <el-form-item label="用户id" prop="userId">-->
<!--            <el-input v-model="form.userId" placeholder="请输入用户id"/>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="围栏名称" prop="name">-->
<!--            <el-input v-model="form.name" placeholder="请输入围栏名称"/>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="围栏详情" prop="detail">-->
<!--            <el-input v-model="form.detail" placeholder="请输入围栏详情"/>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="半径" prop="radius">-->
<!--            <el-input v-model="form.radius" placeholder="请输入半径"/>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="经度" prop="longitude">-->
<!--            <el-input v-model="form.longitude" placeholder="请输入经度"/>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="纬度" prop="latitude">-->
<!--            <el-input v-model="form.latitude" placeholder="请输入纬度"/>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="围栏形状" prop="shape">-->
<!--            <el-input v-model="form.shape" placeholder="请输入围栏形状"/>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="报警等级：1红；2橙；3黄" prop="level">-->
<!--            <el-input v-model="form.level" placeholder="请输入报警等级：1红；2橙；3黄"/>-->
<!--          </el-form-item>-->
<!--        </el-form>-->
<!--        <div slot="footer" class="dialog-footer">-->
<!--          <el-button type="primary" @click="submitForm">确 定</el-button>-->
<!--          <el-button @click="cancel">取 消</el-button>-->
<!--        </div>-->
<!--      </el-dialog>-->
    </div>
  </div>
</template>

<script>
  import { listFence, getFence, delFence, addFence, updateFence,updateFenceStatus } from '@/api/health/fence'
  import { getDeviceInfo } from "@/api/health/deviceInfo";
  import { getUser, addDeviceToUser, userDeviceListByUserId, listDevice, deviceListWithoutUser } from '@/api/system/user'

  export default {
    name: 'FenceList',
    dicts: ['device_type', 'fence_warn_type'],
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
        // 围栏表格数据
        fenceList: [],
        // 弹出层标题
        title: '',
        // 是否显示弹出层
        open: false,
        // 设备id
        deviceId: null,
        //用户id
        userId: null,
        //设备信息
        deviceInfo: {
          userId: null,
          deviceName: null,
          imei: null,
          type: null,
          createTime: null
        },
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          userId: null,
          name: null,
          fenceType: null,
          detail: null,
          radius: null,
          warnType: null,
          status: null,
          longitude: null,
          latitude: null,
          shape: null,
          level: null
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {}
      }
    },
    created() {
      // 获取传递过来的参数
      this.userId = this.$route.params && this.$route.params.userId;
      // userDeviceListByUserId(this.userId).then(response => {
      //   this.deviceInfo = response.rows[0]
      // })
      this.getList()
    },
    methods: {
      /** 查询围栏列表 */
      getList() {
        this.loading = true
        this.queryParams.userId = this.userId;
        listFence(this.queryParams).then(response => {
          this.fenceList = response.rows
          this.total = response.total
          this.loading = false
        })
      },
      // 取消按钮
      cancel() {
        this.open = false
        this.reset()
      },
      // 表单重置
      reset() {
        this.form = {
          id: null,
          userId: null,
          name: null,
          fenceType: null,
          detail: null,
          radius: null,
          warnType: null,
          status: null,
          longitude: null,
          latitude: null,
          createTime: null,
          shape: null,
          level: null
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
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.id)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        var path = [this.userId]
        this.$router.push("/user-fence/" + this.userId);
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        //修改时把围栏id传过去，再通过围栏id查询userId
        this.$router.push("/user-fence/" + this.userId + "&" + + row.id);
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs['form'].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateFence(this.form).then(response => {
                this.$modal.msgSuccess('修改成功')
                this.open = false
                this.getList()
              })
            } else {
              addFence(this.form).then(response => {
                this.$modal.msgSuccess('新增成功')
                this.open = false
                this.getList()
              })
            }
          }
        })
      },
      /** 删除按钮操作 */
      handleDelete(row) {
        const ids = row.id || this.ids
        this.$modal.confirm('此操作将永久删除该数据, 是否继续?').then(function() {
          return delFence(ids)
        }).then(() => {
          this.getList()
          this.$modal.msgSuccess('删除成功')
        }).catch(() => {
        })
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('system/fence/export', {
          ...this.queryParams
        }, `fence_${new Date().getTime()}.xlsx`)
      },
      // 启用状态修改
      handleStatusChange(row) {
        //启用是1，停用是2
        let text = row.status === "1" ? "启用" : "停用";
        this.$modal.confirm('确认要' + text + ' "' + row.name + '" 电子围栏吗？').then(function() {
          console.log(row)
          return updateFenceStatus(row.id, row.status);
        }).then(() => {
          this.$modal.msgSuccess(text + "成功");
        }).catch(function() {
          row.status = row.status === "1" ? "2" : "1";
        });
      }
    }
  }
</script>
<style>
  /*table {*/
  /*  white-space: nowrap;*/
  /*}*/
  td {
    white-space: nowrap;
  }
</style>
