<style lang="scss" scoped>
  .box{
    width:100%;
    height:calc(100vh - 144px);
    position: relative;
  }
  #map{
    width:100%;
    height:100%;
  }
  .btn{
    width:380px;
    display: flex;
    justify-content: space-evenly;
    align-items: center;
    .but{
      height:60%;
    }
  }
  .select{
    margin-top:1.2vh;
    margin-left:10px;
  }
  .menu{
    position: relative;
    width:100%;
    height:60px;
    background: #ffffff;
    // background: rgba(255, 255, 255, 0.7);
    // backdrop-filter: blur(8px);
    border-bottom-left-radius:5px;
    border-bottom-right-radius:5px;
    box-shadow: 3px 3px 10px #dedede;
    display:flex;
    z-index: 1000;
  }
  .lin{
    width:15%;
    margin-top: 26px;
    margin-left:35px;
    height:4px;
    border-radius:20px;
    background:rgba(255, 255, 255, 0.3);
    transition: all .3s;
    position: relative;
    .font{
      color: #fff;
      font-size: 12px;
      letter-spacing: 1px;
      position: absolute;
      right: -55px;
      top:-4px;
    }
    .linX{
      width:0%;
      height:100%;
      border-radius:20px;
      background:rgba(255, 255, 255,1);
      position: relative;
      .linR{
        position: absolute;
        width:12px;
        padding-bottom:12px;
        border-radius:50%;
        background:rgba(255, 255, 255, 1);
        right:-6px;
        top:50%;
        margin-top:-6px;
        cursor: pointer;
      }
    }
  }
  .timeSelect{
    position:absolute;
    width:650px;
    height:100%;
    right:0px;
    display: flex;
    align-items: center;
    .timeBtn{
      margin-left: 10px;
    }
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
<style>
  .amap-info-content{
    border-radius: 12px;
    background: rgba(48, 48, 48, 0.5);
    backdrop-filter: blur(10px);
    font-size: 14px;
    color: #fff;
    letter-spacing: 1px;
    font-weight: 100;
    box-shadow:3px 3px 10px #d3d3d3;
    padding:8px 18px 7px 18px !important;
  }
  .amap-info-sharp{
    display: none !important;
  }
  .amap-info-close{
    display: none !important;
  }
</style>

<template>
  <div class="box">
    <!-- 菜单 -->
    <div class="menu" ref="menu">
      <!-- 按钮 -->
      <!-- <div class="btn" @click="btn('pla')">
          <div class="leftImg" v-show="playState == 0"><img src="@/assets/images/lsgj/ks.png" class="ks"></div>
          <div class="font" v-show="playState == 0">开始</div>

          <div class="leftImg" v-show="playState == 2"><img src="@/assets/images/lsgj/ks.png" class="ks"></div>
          <div class="font" v-show="playState == 2">继续</div>

          <div class="leftImg" v-show="playState == 1 "><img src="@/assets/images/lsgj/zt.png" class="zt" ></div>
          <div class="font" v-show="playState == 1">暂停</div>
      </div>
      <div class="btn" @click="btn('loc')">
          <div class="leftImg"><img src="@/assets/images/lsgj/dw.png"></div>
          <div class="font">定位</div>
      </div>
      <div class="btn" @click="btn('ref')">
          <div class="leftImg"><img src="@/assets/images/lsgj/sx.png" class="sx"></div>
          <div class="font">重置</div>
      </div>
      <div class="btn" @click="lin = !lin">
          <div class="leftImg"><img src="@/assets/images/lsgj/tz.png" class="tz"></div>
          <div class="font">位置</div>
      </div> -->

      <!--<div class="btn">
        <el-button class="but" icon="el-icon-video-play"   size="medium" @click="btn('pla')" v-show="playState == 0" style="margin-left:10px;">开始</el-button>
        <el-button class="but" icon="el-icon-video-play"   size="medium" @click="btn('pla')" v-show="playState == 2">继续</el-button>
        <el-button class="but" icon="el-icon-video-pause"  size="medium" @click="btn('pla')" v-show="playState == 1">暂停</el-button>

        <el-button class="but" icon="el-icon-position"     size="medium" @click="btn('loc')">定位</el-button>
        <el-button class="but" icon="el-icon-refresh-left" size="medium" @click="btn('ref')">重置轨迹</el-button>
      </div>-->
      <!-- <el-select v-model="selectValue" placeholder="选择速度" class="select">
         <el-option v-for="item in options" :key="item.index" :value="item"></el-option>
     </el-select> -->

      <!-- 进度条 -->
      <div class="lin" ref="lin" :style="lin == true ? 'opacity:1;' : 'opacity:0;' ">
        <div class="linX" ref="linX">
          <div class="linR" ref="linR"></div>
        </div>
      </div>
      <!-- 时间选择器 -->
      <div class="timeSelect">
        <el-date-picker
          v-model          ="ELtime"
          type             ="datetimerange"
          range-separator  ="至"
          start-placeholder="开始日期"
          end-placeholder  ="结束日期"
          value-format     ="yyyy-MM-dd HH:mm:ss"
          :default-time    ="['00:00:00', '23:59:59']">
        </el-date-picker>
        <el-button type="primary" icon="el-icon-search" size="medium" @click="getPathList" class="timeBtn">搜索</el-button>
        <el-button icon="el-icon-refresh" size="medium" @click="reset">重置</el-button>
      </div>
    </div>
    <!-- 地图 -->
    <div id="map"></div>
    <!-- 提示 -->
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
  import {pathList} from "@/api/system/user";
  import {parseTime} from "@/utils/ruoyi";

  export default {
    data() {
      return {
        // 消息提示
        tips      : false,
        tipsTitle : '',
        tipsMsg   : '',
        // 地图组件
        map       : null,
        // 小车
        marker    : null,
        // 点位图标
        imgs      : require('@/assets/images/lsgj/1.png'),
        // 信息窗
        carWindow : null,
        // 路线
        polyline  : null,
        // 速度
        speeds    : 8000,
        // 轨迹
        pathList  : [],
        // 播放状态  0开始 1暂停 2继续
        playState : 0,
        // 调整进度
        lin       : false,
        // 进度
        schedule  : 0,
        // 选择时间
        ELtime    : [parseTime(new Date().setHours(0,0,0,0)),parseTime(new Date())],
        // 请求参数
        userId    : this.$route.params.userId,
        // 页面首次进入状态
        pageInit  : true,
        // options   : ['1.0','2.0','5.0'],
        // selectValue:'1.0'
        // 步行导航路线数组
        resPath:[]
      }
    },
    // watch:{
    //     selectValue(newVal){
    //         if (newVal == '1.0') {
    //             this.marker.moveAlong(this.pathList, this.speeds)
    //         }
    //         else if(newVal == '2.0'){
    //             this.marker.moveAlong(this.pathList, this.speeds * 2)
    //         }
    //         else{
    //             this.marker.moveAlong(this.pathList, this.speeds * 5)
    //         }
    //     }
    // },
    methods:{
      //报错信息框
      tipsShow(title,msg){
        this.tips      = true
        this.tipsTitle = title
        this.tipsMsg   = msg
      },
      // 创建地图
      getMap(){
        // 按钮状态恢复
        this.playState = 0
        // 地图
        this.map = new AMap.Map("map", {
          center: [this.pathList[0][0], this.pathList[0][1]],
          resizeEnable: true,
          //使用3D视图
          viewMode:'3D',
          zoom: 12,
        })
        var icon = new AMap.Icon({
          size: new AMap.Size(50, 50),    // 图标尺寸
          image: this.imgs,  // Icon的图像
          imageOffset: new AMap.Pixel(0, 0),  // 图像相对展示区域的偏移量，适于雪碧图等
          imageSize: new AMap.Size(50, 50)   // 根据所设置的大小拉伸或压缩图片
        });
        // 小车
        this.marker = new AMap.Marker({
          map: this.map,
          position:[this.pathList[0][0], this.pathList[0][1]],
          icon: icon,
          offset: new AMap.Pixel(-24, -40),
        })
        // 信息框
        this.carWindow = new AMap.InfoWindow({
          offset: new AMap.Pixel(0, -45)
        })
        this.carWindow.open(this.map, this.marker.getPosition());

        // 路线
        for (let i = 0; i < this.pathList.length - 1; i++) {

          let staPath = this.pathList[i]
          let endPath = this.pathList[i + 1]

          // 步行路线对象
          let walking = new AMap.Walking({ map: this.map, hideMarkers: true })

          // 处理
          walking.search(staPath,endPath,(status, result) => {

            if (status == 'complete'){

              // let Arr = result.routes[0].steps
              //
              // Arr.forEach(item => {
              //   this.resPath.push([item.start_location.lng,item.start_location.lat])
              //   // this.resPath.push([item.end_location.lng,item.end_location.lat])
              // })

            }

          })

        }

        // setTimeout(() => {
        //   console.log(this.resPath)
        // },500)

        // this.polyline = new AMap.Polyline({
        //   map: this.map,
        //   path: this.pathList,
        //   showDir:true,
        //   strokeColor: "#28F",
        //   strokeWeight: 8,
        // })
        // // 监听汽车行走
        // AMap.event.addListener(this.marker, 'moving', (e)=> {
        //   // 当前位置
        //   let nowPath = e.passedPath[e.passedPath.length - 1];
        //   // 设置信息框跟随
        //   this.carWindow.setPosition(nowPath)
        //   // 信息框内容修改
        //   // this.carWindow.setContent(`<div class="info">前往拯救世界....<div>`)
        //   // 调整进度
        //   // var index = this.pathList.indexOf(nowPath,0) + 1
        //   // if (index != 0) {
        //   //     this.$refs.linX.style.width = index / this.pathList.length *100 + '%'
        //   // }
        // })
        // // 监听汽车行走结束
        // AMap.event.addListener(this.marker, 'movealong', (e)=> {
        //   // 汽车重新开始
        //   this.playState = 0
        //   // 信息框内容修改
        //   // this.carWindow.setContent(`<div class="info">到达怪物老巢<div>`);
        // })
        // 居中
        this.map.setFitView()
      },
      // 菜单按钮
      btn(status){
        if (this.pathList.length == 0) {
          // this.tipsShow('tips','请先选择有效的时间段')
          this.$message({
            message: '请先选择有效的时间段',
            type: 'error',
            duration:1500
          });
        }
        else{
          // 定位
          if ( status == 'loc' ) {
            this.map.setFitView()
          }
          // 重置
          else if( status == 'ref' ) {
            // 恢复进度条
            this.$refs.linX.style.width = '0%'
            // 更换按钮状态
            this.playState = 0
            // 重置
            this.marker.moveAlong(this.pathList, this.speeds)
            setTimeout(() => this.marker.pauseMove() , 30);
          }
          // 播放 or 开始
          else if( status == 'pla' ){
            if (this.playState == 0) {
              // this.marker.moveAlong(this.pathList, this.speeds)
              this.marker.moveAlong(this.resPath, this.speeds)
              this.playState = 1
            }
            else if (this.playState == 1) {
              this.marker.pauseMove();
              this.playState = 2
            }
            else if (this.playState == 2) {
              this.marker.resumeMove();
              this.playState = 1
            }
          }
        }
      },
      // 监听进度
      listenLin(){
        let lin  = this.$refs.lin
        let linX = this.$refs.linX
        let linR = this.$refs.linR
        let linMax = lin.offsetWidth - linX.offsetWidth

        var that = this
        linR.addEventListener('mousedown',function(e){
          that.carWindow.close();
          that.marker.pauseMove();
          that.playState = 2
          //初始化鼠标开始拖拽的点击位置
          var nInitX = e.clientX;
          //初始化滑块位置
          var nInitLeft = this.offsetLeft;
          //页面绑定鼠标移动事件
          document.onmousemove = e =>{
            //鼠标移动时取消默认行为，避免选中其他元素或文字
            e.preventDefault();
            //获取鼠标移动后滑块应该移动到的位置
            let nX = e.clientX - nInitX + nInitLeft;
            //限制滑块最大移动位置
            if(nX>=linMax) nX = linMax
            //限制滑块最小移动位置
            if(nX<=0) nX = 0
            // 修改线的宽度
            linX.style.width = nX + 'px'
            that.schedule = linX.offsetWidth / lin.offsetWidth * 100

            if (that.schedule < 97 && that.schedule > 0) {

              //手动拖动进度条过程中触发：移动车辆，定位车辆回放位置
              var currentIndex = Math.round(that.pathList.length * parseInt(that.schedule) / 100);
              var vehicleLocation = that.pathList[currentIndex];
              that.marker.setPosition(new AMap.LngLat(vehicleLocation.lng, vehicleLocation.lat));

            }
          }
        });
        //结束监听
        linR.addEventListener('mousedown',function(event){
          document.onmouseup = function(){
            document.onmousemove = null;
            document.onmouseup   = null;

            that.lin = false
            setTimeout(() => {
              linX.style.width = 0 + '%'
              that.schedule = linX.offsetWidth / lin.offsetWidth * 100
            }, 300);

            that.carWindow.open(that.map, that.marker.getPosition());
            var replayPath = [];
            for (var i = Math.round(that.pathList.length * that.schedule / 100); i < that.pathList.length; i++) {
              replayPath.push(new AMap.LngLat(that.pathList[i].lng, that.pathList[i].lat));
            }
            that.marker.moveAlong(replayPath, that.speeds);
            that.playState = 1
          }
        })
      },
      // 搜索 and 请求数据
      getPathList(){
        let params = {
          coordinateType: 'GCJ02',
          beginReadTime : this.ELtime[0],
          endReadTime   : this.ELtime[1],
          userId        : this.userId
        }
        pathList(params).then(res => {
          if (res.code != 200) {
            // this.tipsShow('Tips','历史轨迹请求失败,刷新试试')
            this.$message({
              message: '历史轨迹请求失败,刷新试试',
              type: 'error',
              duration:1500
            });
          }
          else if(res.data.length == 0 || res.data == null || res.data == undefined){
            if (this.pageInit == false) {
              // this.tipsShow('Tips','此时间段轨迹为空')
              this.$message({
                message: '此时间段轨迹为空',
                type: 'error',
                duration:1500
              });
            }
            else{
              this.pageInit = false
              this.map = new AMap.Map("map", {
                zoom: 12,
              })
            }
          }
          else{
            // 原数组清空
            this.pathList = []
            // 遍历数据
            res.data.forEach( item => this.pathList.push([Number(item.longitude),Number(item.latitude)]))
            // 渲染地图 监听进度
            setTimeout(() => {
              this.getMap()
              this.listenLin()
            }, 500);
          }
        })
      },
      // 重置
      reset(){
        this.ELtime = [parseTime(new Date().setHours(0,0,0,0)),parseTime(new Date())]
        this.getPathList()
      }
    },
    mounted() {
      // 请求数据
      this.getPathList()
    }
  }
</script>
