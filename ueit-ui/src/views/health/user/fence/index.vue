<style lang="scss" scoped>
  .box {
    width: 100%;
    height: calc(100vh - 84px);
    margin-bottom: 6px;
    margin-left: 10px;
    position: relative;

    #map {
      width: 100%;
      height: 100%;
    }

    .mapInpVal{
      position: absolute;
      z-index: 111;
      top: 35vh;
      left: 40px;
      outline: none;
      border: none;
      width: 240px;
      height: 45px;
      background: #fff;
      box-shadow: 3px 3px 10px rgb(0,0,0,0.2);
      padding-left: 20px;
      border-radius: 4px;
      border: 1px solid #ccc;
    }
    .mapList{
      position: absolute;
      z-index: 111;
      top: calc(35vh + 55px);
      left: 40px;
      width: 240px;
      max-height: 300px;
      overflow-x: hidden;
      overflow-y: auto;
      background: #fff;
      padding: 10px;
      li{
        list-style: none;
        transition: all .2s;
        border-radius: 4px;
        padding: 10px;
        div{
          font-size: 14px;
          font-weight: bold;
        }
        p{
          font-size: 12px;
          color: #666;
          margin-top: 6px;
          margin-bottom: 0px;
        }
      }
      li:hover{
        cursor: pointer;
        background: #ececec;
      }
    }

    .formS {
      display: flex;
      justify-content: space-between;
    }
  }
</style>

<!--<style>-->
<!--    .rowsss{-->
<!--        display: flex;-->
<!--        flex-wrap: wrap;-->
<!--        div{-->
<!--          width: 30%;-->
<!--        }-->
<!--        .selectsss{-->
<!--          input{-->
<!--            width: 260px;-->
<!--          }-->
<!--        }-->
<!--    }-->
<!--</style>-->

<template>
  <div class="box">
    <div style="height:20px"></div>
    <el-form style="margin: 0 10px;" ref="form" :model="form" :rules="rules" label-width="80px">
      <el-row >
<!--        <el-col :span="12" :xs="24" :sm="12" :md="8">-->
<!--          <el-form-item label="围栏类型" prop="fenceType">-->
<!--            <el-select v-model="form.fenceType" placeholder="请选择围栏类型" >-->
<!--              <el-option-->
<!--                v-for="dict in dict.type.fence_type"-->
<!--                :key="dict.value"-->
<!--                :label="dict.label"-->
<!--                :value="dict.value"-->
<!--              ></el-option>-->
<!--            </el-select>-->
<!--          </el-form-item>-->
<!--        </el-col>-->
        <el-col :span="12" :xs="24" :sm="12" :md="8">
          <el-form-item label="围栏名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入围栏名称" style="width: 260px;" />
          </el-form-item>
        </el-col>
        <el-col :span="12" :xs="24" :sm="12" :md="8">
          <el-form-item label="围栏详情" prop="detail">
            <el-input v-model="form.detail" placeholder="请输入围栏详情" style="width: 260px;" />
          </el-form-item>
        </el-col>
        <el-col :span="12" :xs="24" :sm="12" :md="8">
          <el-form-item label="半径" prop="radius">
            <el-input v-model="form.radius" placeholder="请输入半径" style="width: 260px;" />
          </el-form-item>
        </el-col>
        <el-col :span="12" :xs="24" :sm="12" :md="8">
          <el-form-item label="报警类型" prop="warnType">
            <el-select v-model="form.warnType" placeholder="请选择报警类型">
              <el-option
                v-for="dict in dict.type.fence_warn_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" :xs="24" :sm="12" :md="8">
          <el-form-item label="报警时段" prop="dateRange">
            <el-date-picker v-model="form.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd" @input="changeInsertShow"></el-date-picker>
          </el-form-item>
        </el-col>
        <el-col :span="12" :xs="24" :sm="12" :md="8">
          <el-form-item label="围栏状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio
                v-for="dict in dict.type.fence_status"
                :key="dict.value"
                :label="dict.value"
              >{{ dict.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <hr>
    <div style="float: right;padding: 15px;margin: 0px 100px 6px 40px;">
      <el-button type="danger" size="medium" @click="getClear" style="margin-right: 30px;">清除围栏</el-button>
      <el-button type="primary" size="medium" @click="getSave" class="timeBtn">保存</el-button>
      <el-button size="medium" @click="getCancel">取消</el-button>
    </div>
    <div id="map" ref="map"></div>
    <input placeholder="搜索位置" v-model="mapInpVal" class="mapInpVal" @input="serchMap" @focus="mapListShow = true">
    <div class="mapList" v-if="mapPoiList.length != 0 && mapListShow">
       <li v-for="(item,index) in mapPoiList" :key="index" @click="selectLoca(item.location)">
          <div>{{item.name}}</div>
         <p>{{item.address}}</p>
       </li>
    </div>
  </div>
</template>

<script>
  import { listFence, getFence, delFence, addFence, updateFence } from '@/api/health/fence'
  import { realTimeTracking } from '@/api/system/user'
  import {parseTime} from "@/utils/ruoyi";
  export default {
    name: 'index',
    dicts: ['fence_type', 'fence_warn_type', 'fence_status', 'fence_shape', 'warn_level'],
    data() {
      return {
        //上一页面传来的用户id、fenceId
        userId: null,
        fenceId: null,
        map: null,
        circle: null,
        // 经纬度
        nowLng: null,
        nowLat: null,
        //圆心经纬度
        centerLng: null,
        centerLat: null,
        readTime: '',
        // 当前位置信息
        NowLocation: null,
        radius: null,
        isCircleDrawn: false, // 记录圆是否已经绘制
        isDrawing: false,
        createTime: null,
        // dateRange: [parseTime((new Date()), '{y}-{m}-{d}'), parseTime(new Date(), '{y}-{m}-{d}')],
        // 表单参数
        form: {
          userId: null,
          name: null,
          fenceType: null,
          detail: null,
          radius: null,
          warnType: null,
          status: null,
          longitude: null,
          latitude: null,
          // shape: null,
          // level: null,
          // beginReadTime: null,
          // endReadTime: null,
          dateRange: [parseTime((new Date()), '{y}-{m}-{d}'), parseTime(new Date(), '{y}-{m}-{d}')]
        },
        // 表单校验
        rules: {
          // shape: [
          //   { required: true, message: '请选择围栏形状', trigger: 'blur' }
          // ],
          fenceType: [
            { required: true, message: '请选择围栏类型', trigger: 'blur' }
          ],
          name: [
            { required: true, message: '请输入围栏名称', trigger: 'blur' }
          ],
          detail: [
            { required: true, message: '请输入围栏详情', trigger: 'blur' }
          ],
          radius: [
            { required: true, message: '请输入围栏半径', trigger: 'blur' }
          ],
          warnType: [
            { required: true, message: '请选择报警类型', trigger: 'blur' }
          ],
          dateRange: [
            { required: true, message: '请选择报警时段', trigger: 'blur' }
          ],
          // level: [
          //   { required: true, message: '请选择报警等级', trigger: 'blur' }
          // ]
        },
        //  地图围栏暂存
        mapCircle:null,
        circleEditor:null,
      //  搜索的关键词
        mapInpVal:'',
        serchMapTimer:null,
        mapPoiList:[],
        mapListShow:false
      }
    },
    mounted() {
      // 获取传递过来的参数
      var userIdAndFenceIdStr = this.$route.params.userId;
      //userIdAndFenceIdStr可能包含userId和FenceId
      if (userIdAndFenceIdStr.includes('&')) {
        var userIdAndFenceId = userIdAndFenceIdStr.split("&").map(Number);
        this.userId = userIdAndFenceId[0];
        this.fenceId = userIdAndFenceId[1];
        //获取该围栏的信息，并保存到form中
        getFence(this.fenceId).then(response => {
          this.form = response.data;
          this.form.dateRange = [response.data.beginReadTime, response.data.endReadTime];
          this.getLocation();
        });
      } else {
        this.userId = Number(userIdAndFenceIdStr);
        // 围栏状态会在页面加载时被设置为生效
        this.form.status = "1";
        this.getLocation();
      }
    },
    methods: {
      // 地图
      getMap() {
        // 创建地图实例
        this.map = new AMap.Map('map', {
          // 中心坐标
          center: [this.nowLng, this.nowLat],
          resizeEnable: true,
          // 缩放级别
          zoom: 15
        })
        // 初始化 地图围栏位置
        if (this.form.longitude && this.form.longitude && this.form.radius) this.mapClcik('init',this.form)
        // 监听鼠标点击事件
        this.map.on('click', e => this.mapClcik(e))
      },
      // 请求实时跟踪接口
      getLocation() {
        let params = {
          coordinateType: 'GCJ02',
          userId: this.userId
        }
        // 实时跟踪
        realTimeTracking(params).then(res => {
          if (res.code != 200) {
            this.$message({
              message: '实时跟踪出错,刷新试试',
              type: 'error',
              duration: 1500
            })
          } else {
            if (!res.data){
              this.$message({
                message: '实时跟踪出错,刷新试试',
                type: 'error',
                duration: 1500
              })
              return
            }
            this.readTime = res.data.readTime
            this.nowLng = res.data.longitude
            this.nowLat = res.data.latitude
            this.getMap()
          }
        })
      },
      //清除方法
      getClear() {
        this.$modal.confirm('此操作将永久删除该数据, 是否继续?').then(() => {
          if(this.mapCircle) {
            this.mapCircle.setMap(null);
            this.mapCircle = null;
            this.isCircleDrawn = false;
            this.form.radius = null;
            if (this.circleEditor) this.circleEditor.close(),this.circleEditor = null
          }
        });
      },
      //保存方法
      getSave() {
        this.$refs['form'].validate(valid => {
          if (valid) {
            // 赋值变量
            let beginReadTime =  this.form.dateRange[0]
            let endReadTime   =  this.form.dateRange[1]
            // 删除原有数据
            delete this.form.dateRange
            // 重新赋值表结构和数据
            this.form.beginReadTime = beginReadTime
            this.form.endReadTime = endReadTime
            this.form.userId = this.userId
            if (this.form.id != null) {
              updateFence(this.form).then(response => {
                this.$modal.msgSuccess('修改成功')
                this.open = false
                // 关闭当前页签，回到首页
                this.$tab.closePage();
                // this.$router.push("/user-fenceList/" + this.userId);
              })
            } else {
              addFence(this.form).then(response => {
                this.$modal.msgSuccess('新增成功')
                this.open = false
                // 关闭当前页签，回到首页
                this.$tab.closePage();
                // this.$router.push("/user-fenceList/" + this.userId);
              })
            }
          }
        })
      },
      //取消方法
      getCancel() {
        // 关闭当前页签，回到首页
        this.$tab.closePage();
        // this.$router.go(-1); // 关闭编辑页面并返回上一个页面
        // this.$router.push("/user-fenceList/" + this.userId);
      },
      //  监听搜索输入框
      serchMap(){

          if (this.serchMapTimer) clearTimeout(this.serchMapTimer)

          this.serchMapTimer = setTimeout(() => {

              this.serchMapTimer = null

              if (!this.mapInpVal){
                return
              }

              // 处理接口请求
              AMap.plugin('AMap.PlaceSearch',() => {

                  let autoOptions = {city: '全国'}

                  let placeSearch = new AMap.PlaceSearch(autoOptions)
                  // 搜索
                  placeSearch.search(this.mapInpVal,(sta,res) => {

                    if (res.poiList.pageSize < 1){
                        this.$message.error('没有可用的位置');
                        return
                    }
                    this.mapPoiList = res.poiList.pois
                    this.mapListShow = true
                  })

              })

          },300)

      },
    //  监听搜索结果点击
      selectLoca(item){

        this.mapListShow = false
        this.map.setFitView()
        // 设置地图的中心点
        this.map.setCenter([item.lng, item.lat])

      },
    //  地图点击事件
      mapClcik(e,data){
        if (!this.mapCircle){
          let lat = e == 'init' ? data.latitude  : e.lnglat.lat
          let lng = e == 'init' ? data.longitude : e.lnglat.lng
          let rad = e == 'init' ? data.radius    : 1000
          // 画圆
          this.mapCircle = new AMap.Circle({
              center: [lng,lat],
              radius: rad, //半径
              borderWeight: 3,
              strokeColor: "#FF33FF",
              strokeOpacity: 1,
              strokeWeight: 6,
              strokeOpacity: 0.2,
              fillOpacity: 0.4,
              strokeStyle: 'dashed',
              strokeDasharray: [10, 10],
              // 线样式还支持 'dashed'
              fillColor: '#1791fc',
              zIndex: 50,
            })
            // 添加 围栏
            this.map.add(this.mapCircle);
            // 赋值 表单数据
            this.form.latitude  = lat
            this.form.longitude = lng
            this.form.radius    = rad
            // 添加围栏操作 事件
            this.circleEditor = new AMap.CircleEditor(this.map, this.mapCircle)
            // 开启编辑
            this.circleEditor.open()
            // 监听移动
            this.circleEditor.on('move', (m) => {
                this.form.latitude  = m.lnglat.lat;
                this.form.longitude = m.lnglat.lng;
            })
            // 监听缩放
            this.circleEditor.on('adjust', a => this.form.radius = a.radius)
        }

      },
      // 强制刷新报警时段
      changeInsertShow(val) {
        this.$forceUpdate()
      }
    }

  }
</script>
