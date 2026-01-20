<style lang="scss" scoped>
  .box{
    width:100%;
    height:calc(100vh - 84px);
    position: relative;
    #map{
      width:100%;
      height:100%;
    }
    .userInfo{
      position: absolute;
      top:30px;
      right:30px;
      width:380px;
      border-radius:12px;
      background: #fff;
      box-shadow:1px 1px 10px #ccc;
      overflow: hidden;
      .back{
        position: absolute;
        top:15px;
        left:13px;
        font-size:16px;
        font-weight:600;
        padding-left:23px;
        cursor: pointer;
        transition: all .2s;
        .icon{
          font-size:20px;
          position: absolute;
          left:0px;
          top:1.4px;
        }
      }
      .back:hover{
        opacity: .8;
      }
      .infoErr{
        line-height:430px;
        text-align: center;
        font-size: 14px;
        letter-spacing: 1px;
      }
      .info{
        width:94%;
        margin: 0 auto;
        height:70px;
        margin-top:50px;
        display: flex;
        .leftImg{
          width:20%;
          text-align: center;
          img{
            width:50px;
            margin-top:10px;
            border-radius:50%;
          }
        }
        .center{
          width:48%;
          margin-left: 2%;
          .cenTop{
            width:100%;
            display: flex;
            font-size:16px;
            font-weight:600;
            margin-top:15px;
            height:20px;
            line-height:20px;
            div{
              margin-right:13px;
            }
          }
          .cenBom{
            height:30px;
            line-height:30px;
            font-size: 14px;
            color: #606266;
          }
        }
      }
      .infoList{
        background: #f5f7fa;
        width:90%;
        margin: 0 auto;
        border-radius:8px;
        margin-top:10px;
        margin-bottom: 20px;
        padding:15px 20px 20px 20px;
      }
      .btn{
        width:90%;
        margin: 0 auto;
        text-align: center;
        border-top:1px solid #f1f1f1;
        height:55px;
        line-height:50px;
        position: relative;
        cursor: pointer;
        transition: all .3s;
        font-size: 14px;
        span{
          position: absolute;
          padding-left:8px;
          top:16.5px;
          font-size:16px;
        }
      }
      .btn:hover{
        opacity: .7;
      }
      .disposeInfo{
        width:100%;
        height:100%;
        position: absolute;
        left:-100%;
        top:0px;
        animation: disposeInfo .4s forwards ease-in-out;
        border-radius:12px;
        background: #fff;
      }
      @keyframes disposeInfo { to{
        left:0%;
      }}
    }
  }
</style>

<template>
  <div class="box">
    <div id="map"></div>
    <div class="userInfo">
      <div class="back" @click="back"> <div class="icon el-icon-arrow-left"></div> 返回 </div>
      <div v-if="userData != '' ">
        <div class="info">
          <div class="leftImg">
            <img src="../../../assets/images/nan.png" v-if="userData.sex == 0">
            <img src="../../../assets/images/nv.png"  v-if="userData.sex == 1">
            <img src="../../../assets/images/wen.png" v-if="userData.sex == 2">
          </div>
          <div class="center">
            <div class="cenTop">
              <div class="name" v-if="isNaN(Number(userData.nickName)) == true"> {{userData.nickName}} </div>
              <div class="name" v-if="isNaN(Number(userData.nickName)) == false">{{userData.nickName.substr(-4)}} </div>
              <div class="sex">
                <span v-if="userData.sex == 0"> 男</span>
                <span v-if="userData.sex == 1"> 女 </span>
                <span v-if="userData.sex == 2"> 未知 </span>
              </div>
              <div class="age"> {{userData.age}}岁</div>
            </div>
            <div class="cenBom">
              {{userData.phonenumber}}
            </div>
          </div>
        </div>
        <div class="infoList">
          <div style="font-size:14px; font-weight:600; letter-spacing: 1px;"> <span class="el-icon-map-location" style="color:#409eff;"></span> 定位 </div>
          <div style="font-size:14px; color:#606266; margin-top:8px;">{{userLoc}}</div>

          <div style="font-size:14px; font-weight:600; letter-spacing: 1px; margin-top:12px;"> <span class="el-icon-warning-outline" style="color:#ff4949;"></span> 异常信息 </div>
          <div style="font-size:14px; color:#606266; margin-top:8px;">异常时间：{{userData.createTime}}</div>

          <div v-if="userData.updateBy == '' || userData.updateBy == null || userData.updateBy == undefined"></div>
          <div v-else>
            <div style="font-size:14px; font-weight:600; letter-spacing: 1px; margin-top:12px;"> <span class="el-icon-circle-check" style="color:#3ddc71;"></span> 处理信息 </div>
            <div style="font-size:14px; color:#606266; margin-top:8px;"> <span style="margin-right:10px;">处理人</span> ：{{userData.updateBy}}</div>
            <div style="font-size:14px; color:#606266; margin-top:8px;"> 处理时间：{{userData.updateTime}}</div>
            <div style="font-size:14px; color:#606266; margin-top:8px;"> 处理说明：{{userData.updateContent}}</div>
          </div>

          <el-button type="primary" plain style="margin-top:20px;">
            <dict-tag :options="dict.type.health_data_type" :value="userData.type"/>
          </el-button>
          <el-button type="danger" plain style="margin-left:15px;">{{userData.value}}</el-button>
        </div>
        <div class="btn" v-if="userData.state == 0" @click="disposeInfo = !disposeInfo">现在处理<span class="el-icon-right"></span> </div>
        <div class="disposeInfo" v-show="disposeInfo">
          <div class="back" @click="disposeInfo = !disposeInfo"> <div class="icon el-icon-back"></div> 取消 </div>
          <el-input
            type="textarea"
            style="width:92%; margin:55px 0px 0px 4%;"
            :autosize="{ minRows: 14, maxRows: 4}"
            placeholder="请输入内容"
            v-model="textarea"
            resize="none">
          </el-input>
          <div class="btn" @click="setRes()" style="line-height:55px;">确定处理<span class="el-icon-circle-check" style="color:#3ddc71; top:19px;"></span> </div>
        </div>
      </div>
      <div class="infoErr" v-else>
        加载资源失败 ， 刷新页面试试
      </div>
    </div>
  </div>
</template>

<script>
  import { getExceptionT,updateException } from "@/api/health/exception";
  export default {
    dicts: ['health_data_type', 'exception_state', 'sys_user_sex'],
    data () {
      return {
        //上一页面传来的用户id
        id  : this.$route.params.id,
        // 用户数据
        userData: '',
        // 位置
        userLoc : '',
        // 点位图标
        imgs   : require('@/assets/images/lsgj/1.png'),
        // 处理面板
        disposeInfo:false,
        // 处理信息
        textarea:'',
        // 地图
        icon:null,
        map:null,
        marker:null
      }
    },
    methods:{
      // 获取用户数据
      getUserInfo(ref){
        if (ref == true) {

          getExceptionT({id:this.id,coordinateType:"GCJ02"}).then(res => {
            this.userData = res.data
            this.getMap(ref)
          })
        }
        else{
          getExceptionT({id:this.id,coordinateType:"GCJ02"}).then(res => {
            this.userData = res.data
            this.getMap()
          })
        }
      },
      // 地图
      getMap(ref){
        if (ref == true) {
          //清除点位标记
          this.map.clearMap()
          // 再次加载点位标记
          this.marker = new AMap.Marker({
            position: new AMap.LngLat(this.userData.longitude,this.userData.latitude),
            icon: this.icon
          });
          this.map.add(this.marker);
          // 使当前位置居中
          this.map.setCenter([this.userData.longitude,this.userData.latitude])
          // 逆编码获取地理位置
          AMap.plugin("AMap.Geocoder",()=>{
            var geocoder = new AMap.Geocoder({extensions: "all"})
            geocoder.getAddress([this.userData.longitude,this.userData.latitude], (status, result) => {
              this.userLoc = result.regeocode.formattedAddress
              this.setInfo()
            })
          })
        }
        else{
          // 创建地图
          this.map = new AMap.Map("map", {
            resizeEnable: true,
            // 中心坐标
            center: [this.userData.longitude,this.userData.latitude],
            // 缩放级别
            zoom: 17,
            //使用3D视图
            viewMode:'3D'
          })
          // 点位图标
          this.icon = new AMap.Icon({
            size: new AMap.Size(50, 50),    // 图标尺寸
            image: this.imgs,  // Icon的图像
            imageOffset: new AMap.Pixel(0, 0),  // 图像相对展示区域的偏移量，适于雪碧图等
            imageSize: new AMap.Size(50, 50)   // 根据所设置的大小拉伸或压缩图片
          })
          // 创建一个 Marker 实例：
          this.marker = new AMap.Marker({
            position: new AMap.LngLat(this.userData.longitude,this.userData.latitude),
            icon: this.icon
          })
          // 将创建的点标记添加到已有的地图实例：
          this.map.add(this.marker);
          // 逆编码获取地理位置
          AMap.plugin("AMap.Geocoder",()=>{
            var geocoder = new AMap.Geocoder({extensions: "all"})
            geocoder.getAddress([this.userData.longitude,this.userData.latitude], (status, result) => {
              this.userLoc = result.regeocode.formattedAddress
              this.setInfo()
            })
          })
        }
      },
      // 设置信息
      setInfo(){
        // console.log(isNaN(Number(this.userData.nickName)));
      },
      // 返回
      back(){
        this.$tab.closeOpenPage({ path: "/exception" })
      },
      setRes(){
        if (this.textarea == '') {
          this.$message({
            message: '输入内容为空',
            type: 'error',
            duration:1500
          })
        }
        else{
          updateException({id:this.id,updateContent:this.textarea}).then(res => {
            this.disposeInfo = !this.disposeInfo
            this.$message({
              message: '处理成功',
              type: 'success',
              duration:1500
            })
            this.getUserInfo(true)
          });
        }
      }
    },
    mounted() {
      this.getUserInfo()
    }
  }
</script>

