<style lang="scss" scoped >
  /** 表单布局 **/
  .form-header-title {
    color:#6379bb;
    border-bottom:1px solid #ddd;
    margin:8px 10px 25px 10px;
    padding-bottom:5px;
    font-weight: 600;
    text-align: center;
  }
</style>

<template>
  <div class="dashboard-editor-container">

    <el-row>
      <el-col :span="24" class="chart-wrapper">
        <h2 class="form-header-title h2">基本信息</h2>
        <el-form ref="form" :model="form" label-width="80px">
          <el-row :gutter="32">
            <el-col :xs="24" :sm="12" :lg="6" >
              <el-form-item label="姓名" prop="nickName">
                <el-input v-model="form.nickName" readonly/>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="手机号码" prop="phonenumber">
                <el-input v-model="form.phonenumber" readonly/>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="性别" prop="sex">
                <el-input v-model="form.sex" readonly/>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="年龄" prop="age">
                <el-input v-model="form.age" readonly/>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-col>
    </el-row>

    <el-row :gutter="32">
      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <h2 class="h2 form-header-title">心率</h2>
          <el-form :model="queryParamsRate" ref="queryFormRate" size="small" :inline="true" v-show="showSearch"
                   label-width="68px" style="padding: 0px 20px 0 20px">
            <el-form-item label="读取时间">
              <el-date-picker
                v-model="dateRangeRate"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :default-time="['00:00:00', '23:59:59']"
                :picker-options="pickerOptionsRate">
              </el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQueryRate">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQueryRate">重置</el-button>
            </el-form-item>
          </el-form>
          <div class="" v-loading="loadingRate">
            <chart-rate :chart-data="chartDataRate"/>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <h2 class="h2 form-header-title">血氧</h2>
          <el-form :model="queryParamsSpo2" ref="queryFormSpo2" size="small" :inline="true" v-show="showSearch"
                   label-width="68px" style="padding: 0px 20px 0 20px">
            <el-form-item label="读取时间">
              <el-date-picker
                v-model="dateRangeSpo2"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :default-time="['00:00:00', '23:59:59']"
                :picker-options="pickerOptionsSpo2">
              </el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuerySpo2">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuerySpo2">重置</el-button>
            </el-form-item>
          </el-form>
          <div class="" v-loading="loadingSpo2">
            <chart-spo2 :chart-data="chartDataSpo2"/>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <h2 class="h2 form-header-title">体温</h2>
          <el-form :model="queryParamsTemperature" ref="queryFormTemperature" size="small" :inline="true"
                   v-show="showSearch"
                   label-width="68px" style="padding: 0px 20px 0 20px">
            <el-form-item label="读取时间">
              <el-date-picker
                v-model="dateRangeTemperature"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :default-time="['00:00:00', '23:59:59']"
                :picker-options="pickerOptionsTemperature">
              </el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQueryTemperature">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQueryTemperature">重置</el-button>
            </el-form-item>
          </el-form>
          <div class="" v-loading="loadingTemperature">
            <chart-temperature :chart-data="chartDataTemperature"/>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <h2 class="h2 form-header-title">步数</h2>
          <el-form :model="queryParamsSteps" ref="queryFormSteps" size="small" :inline="true" v-show="showSearch"
                   label-width="68px" style="padding: 0px 20px 0 20px">
            <el-form-item label="读取时间">
              <el-date-picker
                v-model="dateRangeSteps"
                value-format="yyyy-MM-dd"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :picker-options="pickerOptions"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuerySteps">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuerySteps">重置</el-button>
            </el-form-item>
          </el-form>
          <div class="" v-loading="loadingSteps">
            <chart-steps :chart-data="chartDataSteps"/>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <h2 class="h2 form-header-title">血压</h2>
          <el-form :model="queryParamsBlood" ref="queryFormBlood" size="small" :inline="true" v-show="showSearch"
                   label-width="68px" style="padding: 0px 20px 0 20px">
            <el-form-item label="读取时间">
              <el-date-picker
                v-model="dateRangeBlood"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :default-time="['00:00:00', '23:59:59']"
                :picker-options="pickerOptionsBlood"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQueryBlood">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQueryBlood">重置</el-button>
            </el-form-item>
          </el-form>
          <div class="" v-loading="loadingBlood">
            <chart-blood :chart-data="chartDataBlood"/>
          </div>
        </div>
      </el-col>
<!--      <el-col :xs="24" :sm="24" :lg="12">-->
<!--        <div class="chart-wrapper">-->
<!--          <h2 class="h2 form-header-title">定位</h2>-->
<!--          <el-form :model="queryParamsLocation" ref="queryFormLocation" size="small" :inline="true" v-show="showSearch"-->
<!--                   label-width="68px" style="padding: 0px 20px 0 20px">-->
<!--            <el-form-item label="读取时间">-->
<!--              <el-date-picker-->
<!--                v-model="dateRangeLocation"-->
<!--                value-format="yyyy-MM-dd HH:mm:ss"-->
<!--                type="datetimerange"-->
<!--                range-separator="至"-->
<!--                start-placeholder="开始日期"-->
<!--                end-placeholder="结束日期"-->
<!--                :default-time="['00:00:00', '23:59:59']"-->
<!--                :picker-options="pickerOptionsLocation"-->
<!--              ></el-date-picker>-->
<!--            </el-form-item>-->
<!--            <el-form-item>-->
<!--              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQueryLocation">搜索</el-button>-->
<!--              <el-button icon="el-icon-refresh" size="mini" @click="resetQueryLocation">重置</el-button>-->
<!--            </el-form-item>-->
<!--          </el-form>-->
<!--          <div class="" v-loading="loadingLocation">-->
<!--            <chart-location :chart-data="chartDataLocation"/>-->
<!--          </div>-->
<!--        </div>-->
<!--      </el-col>-->
<!--      <el-col :xs="24" :sm="24" :lg="12">-->
<!--        <div class="chart-wrapper">-->
<!--          <h2 class="h2 form-header-title">告警</h2>-->
<!--          <el-form :model="queryParamsAlarm" ref="queryFormAlarm" size="small" :inline="true" v-show="showSearch"-->
<!--                   label-width="68px" style="padding: 0px 20px 0 20px">-->
<!--            <el-form-item label="读取时间">-->
<!--              <el-date-picker-->
<!--                v-model="dateRangeAlarm"-->
<!--                value-format="yyyy-MM-dd HH:mm:ss"-->
<!--                type="datetimerange"-->
<!--                range-separator="至"-->
<!--                start-placeholder="开始日期"-->
<!--                end-placeholder="结束日期"-->
<!--                :default-time="['00:00:00', '23:59:59']"-->
<!--                :picker-options="pickerOptionsAlarm">-->
<!--              </el-date-picker>-->
<!--            </el-form-item>-->
<!--            <el-form-item>-->
<!--              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQueryAlarm">搜索</el-button>-->
<!--              <el-button icon="el-icon-refresh" size="mini" @click="resetQueryAlarm">重置</el-button>-->
<!--            </el-form-item>-->
<!--          </el-form>-->
<!--          <div class="" v-loading="loadingAlarm">-->
<!--            <chart-alarm :chart-data="chartDataAlarm"/>-->
<!--          </div>-->
<!--        </div>-->
<!--      </el-col>-->
    </el-row>
  </div>
</template>

<script>
  import {parseTime} from "@/utils/ruoyi";
  import ChartRate from './rate/index'
  import ChartAlarm from './alarm/index'
  import ChartSpo2 from './spo2/index'
  import ChartTemperature from './temperature/index'
  import ChartSteps from './steps/index'
  import ChartBlood from './blood/index'
  import ChartLocation from './locations/index'
  import {getUser, dataBoard} from "@/api/system/user";

  export default {
    name: 'DataBoard',
    dicts: ['sys_user_sex'],
    //组件
    components: {
      ChartRate,
      ChartAlarm,
      ChartSpo2,
      ChartTemperature,
      ChartSteps,
      ChartBlood,
      ChartLocation
    },

    data() {
      let that = this
      return {
        // 显示搜索条件
        showSearch: true,
        //表单
        form: {},
        // 遮罩层 心率、告警、血氧、体温、步数、血压、位置
        loadingRate: true,
        loadingAlarm: true,
        loadingSpo2: true,
        loadingTemperature: true,
        loadingSteps: true,
        loadingBlood: true,
        loadingLocation: true,
        //心率、告警、血氧、体温、步数、血压、位置 开始时间和结束时间
        startRate: null,
        endRate: null,
        startAlarm: null,
        endAlarm: null,
        startSpo2: null,
        endSpo2: null,
        startTemperature: null,
        endTemperature: null,
        startBlood: null,
        endBlood: null,
        startLocation: null,
        endLocation: null,
        // 心率、告警、血氧、体温、步数、血压、位置读取时间时间范围
        dateRangeRate: [parseTime(new Date().setHours(0, 0, 0, 0)), parseTime(new Date())],
        dateRangeAlarm: [parseTime(new Date().setHours(0, 0, 0, 0)), parseTime(new Date())],
        dateRangeSpo2: [parseTime(new Date().setHours(0, 0, 0, 0)), parseTime(new Date())],
        dateRangeTemperature: [parseTime(new Date().setHours(0, 0, 0, 0)), parseTime(new Date())],
        dateRangeBlood: [parseTime(new Date().setHours(0, 0, 0, 0)), parseTime(new Date())],
        dateRangeLocation: [parseTime(new Date().setHours(0, 0, 0, 0)), parseTime(new Date())],
        dateRangeSteps: [parseTime((new Date().setTime(new Date().getTime() - 3600 * 1000 * 24 * 7)), '{y}-{m}-{d}'), parseTime(new Date(), '{y}-{m}-{d}')],
        // 心率、告警、血氧、体温、步数、血压、位置 查询参数
        queryParamsRate: {
          userId: this.$route.params && this.$route.params.userId,
          type: "heartRate"
        },
        queryParamsAlarm: {
          userId: this.$route.params && this.$route.params.userId,
          type: "alarm"
        },
        queryParamsSpo2: {
          userId: this.$route.params && this.$route.params.userId,
          type: "spo2"
        },
        queryParamsTemperature: {
          userId: this.$route.params && this.$route.params.userId,
          type: "temperature"
        },
        queryParamsSteps: {
          userId: this.$route.params && this.$route.params.userId,
          type: "steps"
        },
        queryParamsBlood: {
          userId: this.$route.params && this.$route.params.userId,
          type: "blood"
        },
        queryParamsLocation: {
          userId: this.$route.params && this.$route.params.userId,
          type: "location"
        },
        // 心率、告警、血氧、体温、步数、血压、位置 echart数据
        chartDataRate: {
          readTimeData: [],
          valueData: []
        },
        chartDataAlarm: {
          readTimeData: [],
          valueData: []
        },
        chartDataSpo2: {
          readTimeData: [],
          valueData: []
        },
        chartDataTemperature: {
          readTimeData: [],
          valueData: []
        },
        chartDataSteps: {
          readTimeData: [],
          valueData: [],
          caloriesData:[]
        },
        chartDataBlood: {
          readTimeData: [],
          diastolicData: [],
          systolicData:[]
        },
        chartDataLocation: {
          readTimeData: [],
          longitudeData: [],
          latitudeData:[]
        },
        //心率、告警、血氧、体温、步数、血压、位置 日期选择器
        pickerOptionsRate: {
          disabledDate: function (date) {
            const startRate = new Date(that.startRate);
            const endRate = new Date(that.endRate);
            if (date < startRate) {
              return true;
            } else if (date > endRate) {
              return true;
            }
          },
          shortcuts: [{
            text: '今天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }]
        },
        pickerOptionsAlarm: {
          disabledDate: function (date) {
            const startAlarm = new Date(that.startAlarm);
            const endAlarm = new Date(that.endAlarm);
            if (date < startAlarm) {
              return true;
            } else if (date > endAlarm) {
              return true;
            }
          },
          shortcuts: [{
            text: '今天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }]
        },
        pickerOptionsSpo2: {
          disabledDate: function (date) {
            const startSpo2 = new Date(that.startSpo2);
            const endSpo2 = new Date(that.endSpo2);
            if (date < startSpo2) {
              return true;
            } else if (date > endSpo2) {
              return true;
            }
          },
          shortcuts: [{
            text: '今天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }]
        },
        pickerOptionsTemperature: {
          disabledDate: function (date) {
            const startTemperature = new Date(that.startTemperature);
            const endTemperature = new Date(that.endTemperature);
            if (date < startTemperature) {
              return true;
            } else if (date > endTemperature) {
              return true;
            }
          },
          shortcuts: [{
            text: '今天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }]
        },
        pickerOptionsBlood: {
          disabledDate: function (date) {
            const startBlood = new Date(that.startBlood);
            const endBlood = new Date(that.endBlood);
            if (date < startBlood) {
              return true;
            } else if (date > endBlood) {
              return true;
            }
          },
          shortcuts: [{
            text: '今天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }]
        },
        pickerOptionsLocation: {
          disabledDate: function (date) {
            const startLocation = new Date(that.startLocation);
            const endLocation = new Date(that.endLocation);
            if (date < startLocation) {
              return true;
            } else if (date > endLocation) {
              return true;
            }
          },
          shortcuts: [{
            text: '今天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              start.setHours(0, 0, 0);
              picker.$emit('pick', [start, end]);
            }
          }]
        },
        pickerOptions: {
          shortcuts: [{
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit('pick', [start, end]);
            }
          }]
        },
      }
    },
    created() {
      const userId = this.$route.params && this.$route.params.userId;
      if (userId) {
        this.loading = true;
        //查询用户信息
        getUser(userId).then(response => {
          this.form = response.data;
          if(this.form.sex){
            for (let item of this.dict.type.sys_user_sex) {
              if (item.value === this.form.sex) {
                this.form.sex = item.label
              }
            }
          }
          this.loading = false;
        });
      }
      //心率、告警、血氧、体温、步数、血压、位置 列表数据
      this.getHeartRate();
      // this.getHeartAlarm();
      this.getSpo2();
      this.getTemperature();
      this.getSteps();
      this.getBlood();
      // this.getLocation();
      this.getConfigKey("sys.heartRate.start").then(response => {
        this.startRate = response.msg;
      });
      this.getConfigKey("sys.heartRate.end").then(response => {
        this.endRate = response.msg;
      });
      this.getConfigKey("sys.alarm.start").then(response => {
        this.startAlarm = response.msg;
      });
      this.getConfigKey("sys.alarm.end").then(response => {
        this.endAlarm = response.msg;
      });
      this.getConfigKey("sys.spo2.start").then(response => {
        this.startSpo2 = response.msg;
      });
      this.getConfigKey("sys.spo2.end").then(response => {
        this.endSpo2 = response.msg;
      });
      this.getConfigKey("sys.temperature.start").then(response => {
        this.startTemperature = response.msg;
      });
      this.getConfigKey("sys.temperature.end").then(response => {
        this.endTemperature = response.msg;
      });
      this.getConfigKey("sys.blood.start").then(response => {
        this.startBlood = response.msg;
      });
      this.getConfigKey("sys.blood.end").then(response => {
        this.endBlood = response.msg;
      });
      this.getConfigKey("sys.location.start").then(response => {
        this.startLocation = response.msg;
      });
      this.getConfigKey("sys.location.end").then(response => {
        this.endLocation = response.msg;
      });
    },
    methods: {
      /** 查询数据列表 */
      getData(queryParams, callback) {
        dataBoard(queryParams).then(response => {
          callback(response.data);
        });
      },
      /** 查询心率数据列表 */
      getHeartRate() {
        if (null != this.dateRangeRate && '' != this.dateRangeRate) {
          this.queryParamsRate.beginReadTime = this.dateRangeRate[0];
          this.queryParamsRate.endReadTime = this.dateRangeRate[1];
        } else {
          this.$modal.msgError("请先选择读取时间范围!");
          return;
        }
        this.loadingRate = true;
        var that = this;
        this.getData(this.queryParamsRate, function (list) {
          that.chartDataRate.readTimeData = [];
          that.chartDataRate.valueData = [];
          list.forEach(function (item, index) {
            that.chartDataRate.readTimeData.push(item.readTime);
            that.chartDataRate.valueData.push(item.value);
          })
          that.loadingRate = false;
        });
      },
      /** 查询告警数据列表 */
      getHeartAlarm() {
        if (null != this.dateRangeAlarm && '' != this.dateRangeAlarm) {
          this.queryParamsAlarm.beginReadTime = this.dateRangeAlarm[0];
          this.queryParamsAlarm.endReadTime = this.dateRangeAlarm[1];
        } else {
          this.$modal.msgError("请先选择读取时间范围!");
          return;
        }
        this.loadingAlarm = true;
        var that = this;
        this.getData(this.queryParamsAlarm, function (list) {
          that.chartDataAlarm.readTimeData = [];
          that.chartDataAlarm.valueData = [];
          list.forEach(function (item, index) {
            that.chartDataAlarm.readTimeData.push(item.readTime);
            that.chartDataAlarm.valueData.push(item.value);
          })
          that.loadingAlarm = false;
        });
      },
      /** 查询血氧数据列表 */
      getSpo2() {
        if (null != this.dateRangeSpo2 && '' != this.dateRangeSpo2) {
          this.queryParamsSpo2.beginReadTime = this.dateRangeSpo2[0];
          this.queryParamsSpo2.endReadTime = this.dateRangeSpo2[1];
        } else {
          this.$modal.msgError("请先选择读取时间范围!");
          return;
        }
        this.loadingSpo2 = true;
        var that = this;
        this.getData(this.queryParamsSpo2, function (list) {
          that.chartDataSpo2.readTimeData = [];
          that.chartDataSpo2.valueData = [];
          list.forEach(function (item, index) {
            that.chartDataSpo2.readTimeData.push(item.readTime);
            that.chartDataSpo2.valueData.push(item.value);
          })
          that.loadingSpo2 = false;
        });
      },
      /** 查询体温数据列表 */
      getTemperature() {
        if (null != this.dateRangeTemperature && '' != this.dateRangeTemperature) {
          this.queryParamsTemperature.beginReadTime = this.dateRangeTemperature[0];
          this.queryParamsTemperature.endReadTime = this.dateRangeTemperature[1];
        } else {
          this.$modal.msgError("请先选择读取时间范围!");
          return;
        }
        this.loadingTemperature = true;
        var that = this;
        this.getData(this.queryParamsTemperature, function (list) {
          that.chartDataTemperature.readTimeData = [];
          that.chartDataTemperature.valueData = [];
          list.forEach(function (item, index) {
            that.chartDataTemperature.readTimeData.push(item.readTime);
            that.chartDataTemperature.valueData.push(item.value);
          })
          that.loadingTemperature = false;
        });
      },
      /** 查询步数数据列表 */
      getSteps() {
        if (null != this.dateRangeSteps && '' != this.dateRangeSteps) {
          this.queryParamsSteps.beginReadTime = this.dateRangeSteps[0] + " 00:00:00";
          this.queryParamsSteps.endReadTime = this.dateRangeSteps[1] + " 23:59:59";
        } else {
          this.$modal.msgError("请先选择读取时间范围!");
          return;
        }
        this.loadingSteps = true;
        var that = this;
        // debugger
        this.getData(this.queryParamsSteps, function (list) {
          that.chartDataSteps.readTimeData = [];
          that.chartDataSteps.valueData = [];
          list.forEach(function (item, index) {
            that.chartDataSteps.readTimeData.push(parseTime(item.readTime, '{y}-{m}-{d}'));
            if (item.value == null) {
              that.chartDataSteps.valueData.push(0);
            } else {
              that.chartDataSteps.valueData.push(item.value);
            }
            if (item.calories == null) {
              that.chartDataSteps.caloriesData.push(0);
            } else {
              that.chartDataSteps.caloriesData.push(item.calories);
            }
          })
          that.loadingSteps = false;
        });
      },
      /** 查询血压数据列表 */
      getBlood() {
        if (null != this.dateRangeBlood && '' != this.dateRangeBlood) {
          this.queryParamsBlood.beginReadTime = this.dateRangeBlood[0];
          this.queryParamsBlood.endReadTime = this.dateRangeBlood[1];
        } else {
          this.$modal.msgError("请先选择读取时间范围!");
          return;
        }
        this.loadingBlood = true;
        var that = this;
        this.getData(this.queryParamsBlood, function (list) {
          that.chartDataBlood.readTimeData = [];
          that.chartDataBlood.diastolicData = [];
          that.chartDataBlood.systolicData = [];
          list.forEach(function (item, index) {
            that.chartDataBlood.readTimeData.push(item.readTime);
            that.chartDataBlood.diastolicData.push(item.diastolic);
            that.chartDataBlood.systolicData.push(item.systolic);
            // if (item.value == null) {
            //   that.chartDataBlood.diastolicData.push(0);
            // } else {
            //   that.chartDataBlood.diastolicData.push(item.diastolic);
            // }
            // if (item.calories == null) {
            //   that.chartDataBlood.systolicData.push(0);
            // } else {
            //   that.chartDataBlood.systolicData.push(item.systolic);
            // }
          })
          that.loadingBlood = false;
        });
      },
      /** 查询定位数据列表 */
      getLocation() {
        if (null != this.dateRangeLocation && '' != this.dateRangeLocation) {
          this.queryParamsLocation.beginReadTime = this.dateRangeLocation[0];
          this.queryParamsLocation.endReadTime = this.dateRangeLocation[1];
        } else {
          this.$modal.msgError("请先选择读取时间范围!");
          return;
        }
        this.loadingLocation = true;
        var that = this;
        this.getData(this.queryParamsLocation, function (list) {
          that.chartDataLocation.readTimeData = [];
          that.chartDataLocation.longitudeData = [];
          that.chartDataLocation.latitudeData = [];
          list.forEach(function (item, index) {
            that.chartDataLocation.readTimeData.push(item.readTime1);
            that.chartDataLocation.longitudeData.push(item.longitude);
            that.chartDataLocation.latitudeData.push(item.latitude);
          })
          that.loadingLocation = false;
        });
      },
      /** 心率 搜索按钮操作 */
      handleQueryRate() {
        this.getHeartRate();
      },
      /** 心率 重置按钮操作 */
      resetQueryRate() {
        this.dateRangeRate = [parseTime(new Date().setHours(0, 0, 0)), parseTime(new Date())];
        this.resetForm("queryFormRate");
        this.handleQueryRate();
      },
      /** 告警 搜索按钮操作 */
      handleQueryAlarm() {
        this.getHeartAlarm();
      },
      /** 告警 重置按钮操作 */
      resetQueryAlarm() {
        this.dateRangeAlarm = [parseTime(new Date().setHours(0, 0, 0)), parseTime(new Date())];
        this.resetForm("queryFormAlarm");
        this.handleQueryAlarm();
      },
      /** 血氧 搜索按钮操作 */
      handleQuerySpo2() {
        this.getSpo2();
      },
      /** 血氧 重置按钮操作 */
      resetQuerySpo2() {
        this.dateRangeSpo2 = [parseTime(new Date().setHours(0, 0, 0)), parseTime(new Date())];
        this.resetForm("queryFormSpo2");
        this.handleQuerySpo2();
      },
      /** 体温 搜索按钮操作 */
      handleQueryTemperature() {
        this.getTemperature();
      },
      /** 体温 重置按钮操作 */
      resetQueryTemperature() {
        this.dateRangeTemperature = [parseTime(new Date().setHours(0, 0, 0)), parseTime(new Date())];
        this.resetForm("queryFormTemperature");
        this.handleQueryTemperature();
      },
      /** 步数 搜索按钮操作 */
      handleQuerySteps() {
        this.getSteps();
      },
      /** 步数 重置按钮操作 */
      resetQuerySteps() {
        this.dateRangeSteps = [parseTime((new Date().setTime(new Date().getTime() - 3600 * 1000 * 24 * 7)), '{y}-{m}-{d}'), parseTime(new Date(), '{y}-{m}-{d}')];
        this.resetForm("queryFormSteps");
        this.handleQuerySteps();
      },
      /** 血压 搜索按钮操作 */
      handleQueryBlood() {
        this.getBlood();
      },
      /** 血压 重置按钮操作 */
      resetQueryBlood() {
        this.dateRangeBlood = [parseTime(new Date().setHours(0, 0, 0)), parseTime(new Date())];
        this.resetForm("queryFormBlood");
        this.handleQueryBlood();
      },
      /** 定位 搜索按钮操作 */
      handleQueryLocation() {
        this.getLocation();
      },
      /** 定位 重置按钮操作 */
      resetQueryLocation() {
        this.dateRangeLocation = [parseTime(new Date().setHours(0, 0, 0)), parseTime(new Date())];
        this.resetForm("queryFormLocation");
        this.handleQueryLocation();
      },
    }
  }
</script>

<style lang="scss" scoped>
  .dashboard-editor-container {
    padding: 32px;
    background-color: rgb(240, 242, 245);
    position: relative;

    .chart-wrapper {
      background: #fff;
      padding: 16px 16px 0;
      margin-bottom: 32px;
    }
  }

  @media (max-width: 1024px) {
    .chart-wrapper {
      padding: 8px;
    }
  }
</style>
