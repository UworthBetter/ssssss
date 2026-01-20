<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入异常人员手机号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="异常类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择异常类型" clearable>
          <el-option
            v-for="dict in dict.type.health_data_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.exception_state"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <!--只添加了组件未实现时间范围查询功能-->
      <el-form-item label="异常时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
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
          @click="handleExport"
          v-hasPermi="['system:exception:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="exceptionList" @selection-change="handleSelectionChange">
      <!--<el-table-column type="selection" width="55" align="center" />-->
      <el-table-column label="ID" align="center" prop="id" />
      <!--<el-table-column label="异常人员" align="center" prop="userId" />-->
      <el-table-column label="姓名" align="center" prop="nickName" />
      <el-table-column label="手机号码" align="center" prop="phone" width="115">
        <template slot-scope="scope">
          <el-link type="primary" :underline="false" @click="handleDataBoard(scope.row)">{{scope.row.phone}}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="性别" align="center" prop="sex" >
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_user_sex" :value="scope.row.sex"/>
        </template>
      </el-table-column>
      <el-table-column label="年龄" align="center" prop="age" />
      <el-table-column label="异常类型" align="center" prop="type">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.health_data_type" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="异常值" align="center" prop="value" />
      <el-table-column label="状态" align="center" prop="state">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.exception_state" :value="scope.row.state"/>
        </template>
      </el-table-column>
      <el-table-column label="异常时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="updateBy" />
      <el-table-column label="处理时间" align="center" prop="updateTimeWho" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-if="scope.row.state == 0"
                     size="mini"
                     type="text"
                     icon="el-icon-edit"
                     @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:exception:edit']"
          >处理</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="goPage(scope.row)"
          >查看</el-button>
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

    <!-- 添加或修改异常数据对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="nickName">
          <el-input v-model="form.nickName" placeholder="姓名" readonly />
        </el-form-item>
        <el-form-item label="性别" prop="nickName">
          <el-input v-model="form.sex" placeholder="性别" readonly />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="form.age" placeholder="年龄" readonly />
        </el-form-item>
        <el-form-item label="异常类型" prop="type">
          <el-input v-model="form.type" placeholder="异常类型" readonly />
        </el-form-item>
        <el-form-item label="异常值" prop="value">
          <el-input v-model="form.value" placeholder="异常值" readonly />
        </el-form-item>
        <el-form-item label="异常时间" prop="createTime">
          <el-input v-model="form.createTime" placeholder="异常时间" readonly />
        </el-form-item>
        <div v-if="form.state == 1">
          <el-form-item label="处理人" prop="updateBy">
            <el-input v-model="form.updateBy" placeholder="处理人" readonly />
          </el-form-item>
          <el-form-item label="处理时间" prop="state">
            <el-input v-model="form.updateTime" placeholder="处理时间" readonly />
          </el-form-item>
        </div>
        <el-form-item label="处理说明" prop="updateContent">
          <el-input v-model="form.updateContent" type="textarea" placeholder="处理说明" :readonly="form.state == 1" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="this.form.state == 0" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import { listException,listByUserId, getException, delException, addException, updateException } from "@/api/health/exception";
  export default {
    name: "ExceptionList",
    dicts: ['health_data_type', 'exception_state', 'sys_user_sex'],
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
        // 异常数据表格数据
        exceptionList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          userId: null,
          type: this.$route.params.type,
          state: null,
          createTime: null,
          startCreateTime: null,
          endCreateTime: null
        },
        dateRange: [],
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          updateContent: [
            { required: true, message: '请填写处理说明', trigger: 'blur' }
          ]
        }
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询异常数据列表 */
      getList() {
        this.loading = true;
        // const userId = this.$route.query.userId;
        // 获取传递过来的参数
        const userId = this.$route.params && this.$route.params.userId;
        this.queryParams.userId = userId;
        if (this.dateRange[0] && this.dateRange[1]) {
          this.queryParams.startCreateTime = this.dateRange[0];
          this.queryParams.endCreateTime = this.dateRange[1];
        }
        listException(this.queryParams).then(response => {
          this.exceptionList = response.rows;
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
          id: null,
          userId: null,
          userIdWho: null,
          type: null,
          value: null,
          longitude: null,
          latitude: null,
          state: null,
          createTime: null,
          updateByWho: null,
          updateTimeWho: null,
          updateContent: null
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
        this.dateRange = [];
        this.queryParams.startCreateTime = null;
        this.queryParams.endCreateTime = null;
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.id)
        this.single = selection.length!==1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加异常数据";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids
        getException(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "处理异常数据";
          // this.form.type = this.selectDictLabel(this.dict.type.health_data_type, response.data.type);
          // this.form.sex = this.selectDictLabel(this.dict.type.sys_user_sex, response.data.sex);
          // this.form.stateText = this.selectDictLabel(this.dict.type.exception_state, response.data.state);
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateException(this.form).then(response => {
                this.$modal.msgSuccess("处理成功");
                this.open = false;
                this.getList();
              });
            } else {
              addException(this.form).then(response => {
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
        const ids = row.id || this.ids;
        this.$modal.confirm('是否确认删除异常数据编号为"' + ids + '"的数据项？').then(function() {
          return delException(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('health/exception/export', {
          ...this.queryParams
        }, `exception_${new Date().getTime()}.xlsx`)
      },
      // 跳转页面详情
      goPage(row){
        this.$router.push("/exception-view/" + row.id);
      },
      /** 健康数据看板 */
      handleDataBoard: function(row) {
        const userId = row.userIdWho;
        this.$router.push("/user-dataBoard/" + userId);
      },
    }
  };
</script>
