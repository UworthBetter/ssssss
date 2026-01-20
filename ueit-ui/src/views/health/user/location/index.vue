<style>
    .amap-info-content{
        border-radius: 12px;
        background: rgba(48, 48, 48, 0.5);
        backdrop-filter: blur(10px);
        color: #fff;
        letter-spacing: 1px;
        font-weight: 100;
        box-shadow:3px 3px 10px #d3d3d3;
        padding:10px 15px 9px 20px !important;
        line-height: 1.5;
        font-size: 14px;
    }
    .amap-info-sharp{
        display: none !important;
    }
    .amap-info-close{
        display: none !important;
    }
</style>
<style lang="scss" scoped>
      .box{
        width:100%;
        height:calc(100vh - 84px);
        position: relative;
      }
      #map{
          width:100%;
          height:100%;
      }
      .menu{
        position: absolute;
        top:0px;
        left:0px;
        width:100%;
        height:60px;
        background: #ffffff;
        // background: rgba(255, 255, 255, 0.7);
        // backdrop-filter: blur(8px);
        border-bottom-left-radius:5px;
        border-bottom-right-radius:5px;
        box-shadow: 3px 3px 10px #dedede;
        display:flex;
        z-index: 999;
        display: none;
      }
      .time{
        width:40px;
        height:40px;
        margin: 10px 10px 0px 30px;
        line-height: 37px;
        border-radius:50%;
        text-align:center;
        font-size: 14px;
        color: #000000;
        font-weight: 100;
      }
      .times{
        position: absolute;
        width:50px;
        height:50px;
        background: rgba(255, 255, 255, 0.5);
        backdrop-filter: blur(20px);
        box-shadow:3px 3px 10px #ccc;
        text-align: center;
        font-size:20px;
        line-height:50px;
        top:10px;
        left:10px;
        border-radius:8px;
      }
      .NowLocation{
        position: absolute;
        right:30px;
        top:0px;
        height:100%;
        font-size: 12px;
        color: #000000;
        line-height:60px;
        letter-spacing: 1px;
      }
      .tips{
        position: fixed;
        top:45%;
        left:50%;
        transform: translateX(-50%);
        width:350px;
        height:200px;
        background: rgba(48, 48, 48, 0.7);
        backdrop-filter: blur(10px);
        border-radius:12px;
        z-index: 999;
        animation: tips .4s ease-in-out forwards;
        .title{
            padding:10px 0px 0px 15px;
            font-size: 12px;
            color: #fff;
            font-weight: 100;
            letter-spacing: 2px;
        }
        .content{
            text-align: center;
            font-size: 14px;
            line-height:120px;
            color: #fff;
            font-weight: 100;
            letter-spacing: 2px;
        }
        .TipsBtn{
            position: absolute;
            bottom:0px;
            left:1%;
            width:98%;
            height:50px;
            line-height:50px;
            border-top: 1px solid #ffffff3a;
            font-size: 14px;
            color: #fff;
            font-weight: 100;
            letter-spacing: 2px;
            text-align: center;
            cursor: pointer;
        }
      }
      @keyframes tips {to{
          top:40%;
      }}
</style>

<template>
    <div class="box">
        <div id="map"></div>
        <div class="menu">
            <div class="time"> {{times}} </div>
            <div class="NowLocation">当前位置 : {{NowLocation}}</div>
        </div>
        <div class="times">{{times}}</div>
        <div class="tips" v-show="tips">
            <div class="title">{{tipsTitle}}</div>
            <div class="content">
              {{tipsMsg}}
            </div>
            <div class="TipsBtn" @click="tips = !tips">OK</div>
        </div>
    </div>
</template>

<script>
  import {realTimeTracking} from "@/api/system/user";

  export default {
    name: "Location",
    data() {
        return {
            //上一页面传来的用户id
            userId : this.$route.params.userId,
            // 点位图标
            imgs   : require('@/assets/images/lsgj/1.png'),
            // 经纬度
            nowLng : null,
            nowLat : null,
            readTime:'',
            // 当前位置信息
            NowLocation : null,
            // 倒计时
            times  : 30,
            // 消息提示
            tips : false,
            tipsTitle : '',
            tipsMsg  : '',
            // 定时器
            locationRef : null,
            tenLocation : null,
            oneLocation : null
        }
    },
    methods:{
        //报错信息框
        tipsShow(title,msg){
            this.tips      = true
            this.tipsTitle = title
            this.tipsMsg   = msg
        },
        // 地图
        getMap(){
            // 创建地图
            var map = new AMap.Map("map", {
                resizeEnable: true,
                // 中心坐标
                center: [this.nowLng,this.nowLat],
                // 缩放级别
                zoom: 17,
                //使用3D视图
                viewMode:'3D'
            });

            // 点位图标
            var icon = new AMap.Icon({
                size: new AMap.Size(50, 50),    // 图标尺寸
                image: this.imgs,  // Icon的图像
                imageOffset: new AMap.Pixel(0, 0),  // 图像相对展示区域的偏移量，适于雪碧图等
                imageSize: new AMap.Size(50, 50)   // 根据所设置的大小拉伸或压缩图片
            });
            // 创建一个 Marker 实例：
            var marker = new AMap.Marker({
                position: new AMap.LngLat(this.nowLng,this.nowLat),
                icon: icon
            });
            // 将创建的点标记添加到已有的地图实例：
            map.add(marker);

            // 创建跟速度信息展示框
            var carWindow = new AMap.InfoWindow({
                offset: new AMap.Pixel(17, -35)
            });
            carWindow.open(map, marker.getPosition());

            // 逆编码获取地理位置
            AMap.plugin("AMap.Geocoder",()=>{
              var geocoder = new AMap.Geocoder({
                  extensions: "all"
              });
              geocoder.getAddress([this.nowLng,this.nowLat], (status, result) => {
                  if (status == 'complete') {
                      this.NowLocation = result.regeocode.formattedAddress
                      carWindow.setContent(`
                      <div class="info">当前位置 : `+result.regeocode.formattedAddress+`<div>
                      <div class="info">定位时间 : `+this.readTime+`<div>
                      `);
                  }
                  else{
                    //   this.tipsShow( 'getMap.AMap.plugin','逆编码获取地理位置出错' )
                      this.$message({
                            message: '获取位置出错',
                            type: 'error',
                            duration:1500
                      });
                  }
              });
            })

            // 刷新定位 * 30s
            this.locationRef = setInterval(() => {
                //清除点位标记
                map.clearMap()
                // 再次加载点位标记
                var marker = new AMap.Marker({
                    position: new AMap.LngLat(this.nowLng,this.nowLat),
                    icon: icon
                });
                map.add(marker);
                // 刷新信息框
                carWindow.open(map, marker.getPosition());
                // 使当前位置居中
                map.setCenter([this.nowLng,this.nowLat])
                // 刷新时间
                this.times  = 30
                // 刷新实时位置
                AMap.plugin("AMap.Geocoder",()=>{
                  var geocoder = new AMap.Geocoder({
                      extensions: "all"
                  });
                  geocoder.getAddress([this.nowLng,this.nowLat], (status, result) => {
                      if (status == 'complete') {
                          this.NowLocation = result.regeocode.formattedAddress
                          carWindow.setContent(`
                            <div class="info">当前位置 : `+result.regeocode.formattedAddress+`<div>
                            <div class="info">定位时间 : `+this.readTime+`<div>
                          `);
                      }
                      else{
                        // this.tipsShow( 'getMap.setInterval.AMap.plugin','逆编码获取地理位置出错' )
                        this.$message({
                            message: '获取位置出错',
                            type: 'error',
                            duration:1500
                        });
                      }
                  });
                })
            }, 30000);
        },
        // 请求实时跟踪接口
        getLocation(){
            let params = {
                coordinateType : 'GCJ02',
                userId         : this.userId
            }
            realTimeTracking(params).then(res => {
                if (res.code != 200) {
                    // this.tipsShow( 'getLocation','请求实时跟踪接口出错,刷新试试' )
                    this.$message({
                        message: '实时跟踪出错,刷新试试',
                        type: 'error',
                        duration:1500
                    });
                }
                else {
                  if (res.data) {
                    this.readTime = res.data.readTime
                    this.nowLng = res.data.longitude
                    this.nowLat = res.data.latitude
                    this.getMap()
                  }
                }
            })
        },
        // 刷新请求
        refLocation(){
            let params = {
                coordinateType : 'GCJ02',
                userId         : this.userId
            }
            realTimeTracking(params).then(res => {
                if (res.code != 200) {
                    // this.tipsShow( 'refLocation','刷新实时跟踪接口出错' )
                    this.$message({
                        message: '刷新实时跟踪接口出错',
                        type: 'error',
                        duration:1500
                    });
                }
                else{
                  if (res.data) {
                    this.readTime = res.data.readTime
                    this.nowLng = res.data.longitude
                    this.nowLat = res.data.latitude
                  }
                }
            })
        },
        // 刷新定位测试
        refLocationTest(){
            var Arr = [
                {
                  Lng:116.478935,
                  Lat:39.997761
                },
                {
                  Lng:115.6747029,
                  Lat:34.796239
                },
                {
                  Lng:116.6747029,
                  Lat:34.796239
                },
                {
                  Lng:112.3847029,
                  Lat:34.796239
                },
                {
                  Lng:116.478939,
                  Lat:39.997825
                },
                {
                  Lng:116.48467,
                  Lat:39.999861
                }
            ]

            var ArrNum = 0

            setInterval(() => {
                if (ArrNum < Arr.length) {
                  this.nowLng = Arr[ArrNum].Lng
                  this.nowLat = Arr[ArrNum].Lat
                  ArrNum++
                }
            }, 10000);
        }
    },
    mounted() {
        this.getLocation()
        // 每30秒再次请求
        this.tenLocation = setInterval(() => this.refLocation(), 30000)
        // this.refLocationTest()
        // 每一秒时间 - 1
        this.oneLocation = setInterval(() => this.times!=0 ? this.times -- : console.log('TimeEnd'), 1000)
    },
    destroyed(){
        clearTimeout(this.tenLocation)
        clearTimeout(this.oneLocation)
        clearTimeout(this.locationRef)
    }
  }
</script>
