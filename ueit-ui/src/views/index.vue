<style>
  .dv-scroll-board .rows .ceil {
    padding: 0px !important;
  }
</style>
<style lang="scss" scoped>
  .box {
    width: 100%;
    position: relative;
    background: url('../assets/images/indexIMg/pic-1.jpg') no-repeat;
    background-size: 100% 100%;
    padding-bottom: 30px;
    user-select: none;
    overflow: hidden;
    // 头部
    .pageTitle {
      width: 100%;
      height: 110px;
      position: relative;
      left: 0px;
      top: 0px;

      img {
        width: 100%;
        height: 100%;
      }

      .tit {
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        font-size: calc(100vw * 40 / 1980);
        letter-spacing: 1.5px;
        color: #3b9cfd;
        text-align: center;
        line-height: 105px;
      }

      .leftInfo {
        position: absolute;
        top: 0px;
        left: 70px;
        height: 55px;
        line-height: 55px;
        color: #fff;
        font-size: calc(100vw * 18 / 1980);
        letter-spacing: -1px;
        display: flex;
      }
    }

    // 边框正方角
    i {
      position: absolute;
      display: block;
      width: 10px;
      height: 10px;
      background: #041531;
      border: 1px solid #3b9cfd;
      box-shadow: 0px 0px 10px #3b9cfd;
    }

    // 数据总览
    .overview {
      position: relative;
      left: 2%;
      margin-top: 30px;
      width: 96%;
      height: 160px;
      border: 1px solid #3b9cfd;

      .list {
        width: 100%;
        height: 100%;
        position: absolute;
        top: 0px;
        left: 0px;
        display: flex;
        justify-content: space-evenly;

        li {
          height: 100%;
          width: 14%;
          position: relative;
          display: flex;
          align-items: center;

          .imgBox {
            cursor: pointer;
            width: 40%;
            text-align: center;

            img {
              width: 65%;
            }
          }

          .font {
            cursor: pointer;
            width: 58%;
            color: #ffff;
            font-size: calc(100vw * 16 / 1980);
            letter-spacing: 1px;
            font-weight: 100;
            padding: 2% 0 0 2%;

            h1 {
              margin-top: 5px;
              margin-bottom: 5px;
              color: #30c96e;
              text-shadow: 0px 0px 3px #30c96e;
              font-weight: 700;
              font-size: calc(100vw * 36 / 1980);

              span {
                font-size: calc(100vw * 16 / 1980);
              }
            }
          }
        }
      }
    }

    // 底部盒子
    .bom {
      position: relative;
      left: 2%;
      margin-top: 30px;
      width: 96%;
      height: 720px;
      display: flex;

      .bomLeft {
        width: 27%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .list {
          position: relative;
          width: 100%;
          height: 162px;
          border: 1px solid #3b9cfd;

          .topImg {
            width: 98%;
            margin-left: 1%;
            margin-top: 3px;
          }

          .listTitle {
            color: #409cff;
            font-size: calc(100vw * 18 / 1980);
            letter-spacing: 1px;
            position: absolute;
            top: 8px;
            left: 20px;
          }

          .more {
            position: absolute;
            top: 10px;
            right: 15px;
            color: #fcff00;
            font-size: calc(100vw * 18 / 1980);
            transition: all .2s;
          }

          .more:hover {
            cursor: pointer;
            opacity: .5;
          }
        }

        .list:nth-child(2),
        .list:nth-child(3),
        .list:nth-child(4) {
          li {
            div:nth-child(4) {
              color: #fcff00;
            }
          }
        }
      }

      .bomCenter {
        position: relative;
        width: 40%;
        margin: 0px 3% 0px 3%;
        height: 100%;

        #map {
          position: absolute;
          left: 3%;
          top: 3%;
          width: 94%;
          height: 94%;
          border-radius: 5px;
        }
      }

      .bomRight {
        width: 27%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .list {
          position: relative;
          width: 100%;
          height: 162px;
          border: 1px solid #3b9cfd;

          .topImg {
            width: 98%;
            margin-left: 1%;
            margin-top: 3px;
          }

          .listTitle {
            color: #409cff;
            font-size: calc(100vw * 18 / 1980);
            letter-spacing: 1px;
            position: absolute;
            top: 8px;
            left: 20px;
          }

          .more {
            position: absolute;
            top: 10px;
            right: 15px;
            color: #fcff00;
            font-size: calc(100vw * 18 / 1980);
            transition: all .2s;
          }

          .more:hover {
            cursor: pointer;
            opacity: .5;
          }

          .liBox {
            height: 75px;
            overflow: hidden;
          }

          li {
            width: 92%;
            margin-left: 4%;
            font-size: 12px;
            color: #fff;
            display: flex;
            height: 25px;
            line-height: 25px;
            justify-content: space-between;
            transition: all .3s;

            img {
              width: 12px;
              height: 12px;
              margin-top: 5px;
              margin-right: 12px;
              transition: all .2s;
            }

            img:hover {
              cursor: pointer;
              opacity: .5;
            }
          }

          .Ec {
            position: absolute;
            left: 35%;
            top: 0px;
            width: 30%;
            height: 100%;
          }
        }
      }

      .dvLi {
        height: 95px;
        width: 92%;
        margin: 0 auto;
      }
    }
  }
</style>
<style>
  .amap-info-content {
    border-radius: 10px;
    background: rgba(48, 48, 48, 0.5);
    backdrop-filter: blur(10px);
    font-size: 14px;
    color: #fff;
    letter-spacing: 1px;
    font-weight: 100;
    box-shadow: 3px 3px 10px #d3d3d3;
    padding: 8px 30px 7px 30px !important;
  }

  .amap-info-sharp {
    display: none !important;
  }
</style>
<template>
  <div class="box">
    <!-- 头部 -->
    <div class="pageTitle">
      <span class="tit">{{deptName}}统计总览</span>
      <div class="leftInfo">
        <div v-html="nowTime"></div>
        <div v-html="weather"></div>
      </div>
      <img src="../assets/images/indexIMg/pic-2.png">
      <div class="topS" style="position:absolute; right:50px; top:10px">
        <dv-decoration-3 style="width:250px;height:30px;"/>
      </div>
      <svg-icon :icon-class="isFullscreen?'exit-fullscreen':'fullscreen'" @click="openNewWindow"
                style="color: white;position:absolute; right:320px; top:17px; cursor: pointer;"/>
    </div>
    <!-- 顶部的数据总览 -->
    <div class="overview">
      <i style="left:-4px;top:-4px"></i>
      <i style="right:-4px;top:-4px"></i>
      <i style="left:-4px;bottom:-4px"></i>
      <i style="right:-4px;bottom:-4px"></i>
      <div class="list">
        <li>
          <div class="imgBox" @click="goExceptionPage('temperature')"><img src="../assets/images/indexIMg/1.png"></div>
          <div class="font" @click="goExceptionPage('temperature')">
            体温异常数据
            <h1> {{temperatures}} <span>次</span></h1>
          </div>
        </li>
        <li>
          <div class="imgBox" @click="goExceptionPage('heart_rate')"><img src="../assets/images/indexIMg/2.png"></div>
          <div class="font" @click="goExceptionPage('heart_rate')">
            心率异常数据
            <h1> {{heart_rates}} <span>次</span></h1>
          </div>
        </li>
        <li>
          <div class="imgBox" @click="goExceptionPage('spo2')"><img src="../assets/images/indexIMg/3.png"></div>
          <div class="font" @click="goExceptionPage('spo2')">
            血氧异常数据
            <h1> {{spo2s}} <span>次</span></h1>
          </div>
        </li>
        <li style="cursor: auto;">
          <div class="imgBox" @click="goExceptionPage('blood')"><img src="../assets/images/indexIMg/5.png"></div>
          <div class="font" @click="goExceptionPage('blood')">
            血压异常数据
            <h1> {{bloods}} <span>次</span></h1>
          </div>
        </li>
        <li style="cursor: auto;">
          <div class="imgBox" @click="goExceptionPage('step')"><img src="../assets/images/indexIMg/4.png"></div>
          <div class="font" @click="goExceptionPage('step')">
            步数异常数据
            <h1> {{steps}} <span>次</span></h1>
          </div>
        </li>
        <li style="cursor: auto;">
          <div class="imgBox" @click="goExceptionPage('fence')"><img src="../assets/images/indexIMg/4.png"></div>
          <div class="font" @click="goExceptionPage('fence')">
            围栏异常数据
            <h1> {{fences}} <span>次</span></h1>
          </div>
        </li>
        <li>
          <div class="imgBox" @click="goExceptionPage('sos')"><img src="../assets/images/indexIMg/6.png"></div>
          <div class="font" @click="goExceptionPage('sos')">
            SOS求助数据
            <h1> {{soss}} <span>次</span></h1>
          </div>
        </li>
      </div>
    </div>
    <!-- 底部 -->
    <div class="bom">
      <!-- 底部左侧 -->
      <div class="bomLeft">
        <div class="list">
          <i style="left:-4px;top:-4px"></i>
          <i style="right:-4px;top:-4px"></i>
          <i style="left:-4px;bottom:-4px"></i>
          <i style="right:-4px;bottom:-4px"></i>
          <img src="../assets/images/indexIMg/pic-4.png" class="topImg">
          <div class="listTitle">实时数据</div>
          <!--					<div class="more el-icon-more" @click="goToException(0)"></div>-->

          <dv-scroll-board :config="config" class="dvLi" ref="config"/>

        </div>
        <div class="list">
          <i style="left:-4px;top:-4px"></i>
          <i style="right:-4px;top:-4px"></i>
          <i style="left:-4px;bottom:-4px"></i>
          <i style="right:-4px;bottom:-4px"></i>
          <img src="../assets/images/indexIMg/pic-4.png" class="topImg">
          <div class="listTitle">体温异常</div>
          <div class="more el-icon-more" @click="goExceptionPage('temperature')"></div>
          <dv-scroll-board :config="temperature" class="dvLi" ref="temperature"/>
        </div>
        <div class="list">
          <i style="left:-4px;top:-4px"></i>
          <i style="right:-4px;top:-4px"></i>
          <i style="left:-4px;bottom:-4px"></i>
          <i style="right:-4px;bottom:-4px"></i>
          <img src="../assets/images/indexIMg/pic-4.png" class="topImg">
          <div class="listTitle">心率异常</div>
          <div class="more el-icon-more" @click="goExceptionPage('heart_rate')"></div>

          <dv-scroll-board :config="heart_rate" class="dvLi" ref="heart_rate"/>
        </div>
        <div class="list">
          <i style="left:-4px;top:-4px"></i>
          <i style="right:-4px;top:-4px"></i>
          <i style="left:-4px;bottom:-4px"></i>
          <i style="right:-4px;bottom:-4px"></i>
          <img src="../assets/images/indexIMg/pic-4.png" class="topImg">
          <div class="listTitle">血氧异常</div>
          <div class="more el-icon-more" @click="goExceptionPage('spo2')"></div>
          <dv-scroll-board :config="spo2" class="dvLi" ref="spo2"/>
        </div>
      </div>
      <!-- 底部中侧 -->
      <div class="bomCenter">
        <dv-border-box-1 :reverse="true" :dur=10>
          <div id="map"></div>
        </dv-border-box-1>
      </div>
      <!-- 底部右侧 -->
      <div class="bomRight">
        <div class="list">
          <i style="left:-4px;top:-4px"></i>
          <i style="right:-4px;top:-4px"></i>
          <i style="left:-4px;bottom:-4px"></i>
          <i style="right:-4px;bottom:-4px"></i>
          <img src="../assets/images/indexIMg/pic-4.png" class="topImg">
          <div class="listTitle">血压异常</div>
          <div class="more el-icon-more" @click="goExceptionPage('blood')"></div>
          <dv-scroll-board :config="blood" class="dvLi" ref="blood"/>
        </div>
        <div class="list">
          <i style="left:-4px;top:-4px"></i>
          <i style="right:-4px;top:-4px"></i>
          <i style="left:-4px;bottom:-4px"></i>
          <i style="right:-4px;bottom:-4px"></i>
          <img src="../assets/images/indexIMg/pic-4.png" class="topImg">
          <div class="listTitle">步数异常</div>
          <div class="more el-icon-more" @click="goExceptionPage('step')"></div>
          <dv-scroll-board :config="step" class="dvLi" ref="step"/>
        </div>
        <div class="list">
          <i style="left:-4px;top:-4px"></i>
          <i style="right:-4px;top:-4px"></i>
          <i style="left:-4px;bottom:-4px"></i>
          <i style="right:-4px;bottom:-4px"></i>
          <img src="../assets/images/indexIMg/pic-4.png" class="topImg">
          <div class="listTitle">围栏异常</div>
          <div class="more el-icon-more" @click="goExceptionPage('fence')"></div>
          <dv-scroll-board :config="fence" class="dvLi" ref="fence"/>
        </div>
        <div class="list">
          <i style="left:-4px;top:-4px"></i>
          <i style="right:-4px;top:-4px"></i>
          <i style="left:-4px;bottom:-4px"></i>
          <i style="right:-4px;bottom:-4px"></i>
          <img src="../assets/images/indexIMg/pic-4.png" class="topImg">
          <div class="listTitle">SOS求助</div>
          <div class="more el-icon-more" @click="goExceptionPage('sos')"></div>
          <dv-scroll-board :config="sos" class="dvLi" ref="sos"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import * as echarts from 'echarts'
  import {exception, realTimeData, indexUserLocation} from "@/api/index/index"
  import screenfull from 'screenfull';

  import {getUserProfile} from "@/api/system/user";

  export default {
    props: {
      indexFullscreen: {
        type: Boolean,
        default: false
      },
    },
    data() {
      return {
        // 是否全屏
        isFullscreen: this.indexFullscreen,
        // 用户部门名称
        deptName: "运行",
        // 当前时间
        nowTime: '',
        // 天气状况
        weather: '',
        imgs: require('../assets/images/indexIMg/pic-5.png'),
        temperatures: 0,
        heart_rates: 0,
        spo2s: 0,
        soss: 0,
        fences: 0,
        bloods: 0,
        steps: 0,
        charges: 0,
        mergedConfig: null,
        needCalc: false,
        // 实时数据
        config: {
          evenRowBGC: '#03152f',
          oddRowBGC: '#03152f',
          rowNum: 3,
          data: [
            [
              `<div style="display:flex; justify-content: space-between;  ">` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `</div>`
            ]
          ]
        },
        // 体温数据
        temperature: {
          evenRowBGC: '#03152f',
          oddRowBGC: '#03152f',
          rowNum: 3,
          data: [
            [
              `<div style="display:flex; justify-content: space-between;">` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div style="color:#f4fa02;"></div>` +
              `<div></div>` +
              // `<div class="el-icon-success" style="color:#f4fa02;line-height: 2; font-size:12px;"></div>`+
              `</div>`
            ]
          ]
        },
        // 心率数据
        heart_rate: {
          evenRowBGC: '#03152f',
          oddRowBGC: '#03152f',
          rowNum: 3,
          data: [
            [
              `<div style="display:flex; justify-content: space-between;">` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div style="color:#f4fa02;"></div>` +
              `<div></div>` +
              // `<div class="el-icon-success" style="color:#f4fa02;line-height: 2; font-size:12px;"></div>`+
              `</div>`
            ]
          ]
        },
        // 血氧数据
        spo2: {
          evenRowBGC: '#03152f',
          oddRowBGC: '#03152f',
          rowNum: 3,
          data: [
            [
              `<div style="display:flex; justify-content: space-between;">` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div style="color:#f4fa02;"></div>` +
              `<div></div>` +
              // `<div class="el-icon-success" style="color:#f4fa02;line-height: 2; font-size:12px;"></div>`+
              `</div>`
            ]
          ]
        },
        // fence数据
        fence: {
          evenRowBGC: '#03152f',
          oddRowBGC: '#03152f',
          rowNum: 3,
          data: [
            [
              `<div style="display:flex; justify-content: space-between;">` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              // `<div class="el-icon-success" style="color:#f4fa02;line-height: 2; font-size:12px;"></div>`+
              `</div>`
            ]
          ]
        },
        // sos数据
        sos: {
          evenRowBGC: '#03152f',
          oddRowBGC: '#03152f',
          rowNum: 3,
          data: [
            [
              `<div style="display:flex; justify-content: space-between;">` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              // `<div class="el-icon-success" style="color:#f4fa02;line-height: 2; font-size:12px;"></div>`+
              `</div>`
            ]
          ]
        },
        // step数据
        step: {
          evenRowBGC: '#03152f',
          oddRowBGC: '#03152f',
          rowNum: 3,
          data: [
            [
              `<div style="display:flex; justify-content: space-between;">` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              // `<div class="el-icon-success" style="color:#f4fa02;line-height: 2; font-size:12px;"></div>`+
              `</div>`
            ]
          ]
        },
        // blood数据
        blood: {
          evenRowBGC: '#03152f',
          oddRowBGC: '#03152f',
          rowNum: 3,
          data: [
            [
              `<div style="display:flex; justify-content: space-between;">` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              `<div></div>` +
              // `<div class="el-icon-success" style="color:#f4fa02;line-height: 2; font-size:12px;"></div>`+
              `</div>`
            ]
          ]
        },
        // 最新id
        idList: {
          heart_rateId: '',
          sosId: '',
          fenceId: '',
          bloodId: '',
          stepId: '',
          spo2Id: '',
          temperatureId: '',
          charge: ''
        },
        heart_rateRows: [],
        temperatureRows: [],
        spo2Rows: [],
        sosRows: [],
        fenceRows: [],
        bloodRows: [],
        stepRows: [],
        chargeRows: [],
        realTimeDataRows: [],
        // 点位图标
        imgTwo: require('@/assets/images/watch.png'),
      }
    },
    methods: {
      getSW(list) {
        let south = null
        let west = null
        for (let item of list) {
          if ((west && item.longitude < west) || !west) {
            west = item.longitude
          }
          if ((south && item.latitude < south) || !south) {
            south = item.latitude
          }
        }
        return [west, south]
      },
      getNE(list) {
        let north = null
        let east = null
        for (let item of list) {
          if ((east && item.longitude > east) || !east) {
            east = item.longitude
          }
          if ((north && item.latitude > north) || !north) {
            north = item.latitude
          }
        }

        return [east, north]
      },
      // 获取当前时间
      getTime() {
        var dates = new Date();
        let year = dates.getFullYear();
        let mounth = (dates.getMonth() + 1).toString().padStart(2, '0');
        let day = dates.getDate().toString().padStart(2, '0');
        let hour = dates.getHours().toString().padStart(2, '0');
        let minutes = dates.getMinutes().toString().padStart(2, '0');
        var weeks = dates.getDay();//得到周几
        var arr_week = new Array("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六");
        var week = arr_week[weeks]
        this.nowTime = `
			<span>` + year + ` 年 ` + mounth + ` 月 ` + day + ` 日</span>
			<span style="margin:0 15px 0 15px;">` + week + `</span>
			<span style="margin:0 30px 0 0;">` + hour + ` : ` + minutes + `</span>`
      },
      // 获取城市再获取天气
      getWeather() {
        let that = this
        // ip地址城市查询
        AMap.plugin('AMap.CitySearch', function () {
          new AMap.CitySearch().getLocalCity(function (status, result) {
            if (status === 'complete' && result.info === 'OK') {
              //天气查询
              AMap.plugin('AMap.Weather', function () {
                new AMap.Weather().getLive(result.city, function (err, data) {
                  that.weather = `
									<span>` + data.city + `</span>
									<span style="margin:0 15px 0 15px;">` + data.temperature + `℃</span>
									<span>` + data.weather + `</span>
								`
                });
              });
            }
          })
        })
      },
      // 获取地图 参数是 获取设备信息 的接口
      getMap(list) {

        var cluster, markers = [];

        // 创建地图
        var map = new AMap.Map("map", {
          resizeEnable: true,
          // 缩放级别
          zoom: 8
        })

        var icon = new AMap.Icon({
          size: new AMap.Size(50, 50),    // 图标尺寸
          image: this.imgTwo,  // Icon的图像
          imageOffset: new AMap.Pixel(0, 0),  // 图像相对展示区域的偏移量，适于雪碧图等
          imageSize: new AMap.Size(50, 50)   // 根据所设置的大小拉伸或压缩图片
        });
        // map遍历设备信息数据
        list.map((data) => {
          // 海量生成marker点
          var marker = new AMap.Marker({
            icon: icon,
            position: new AMap.LngLat(data.longitude, data.latitude)
          })
          // 窗体信息
          let infoWindow = new AMap.InfoWindow({
            content: " " + data.nickName + " ",
            offset: new AMap.Pixel(15, -30),
          });
          //监听marker的点击事件
          AMap.event.addListener(marker, "click", function (e) {
            infoWindow.open(
              map,
              // 窗口信息的位置
              marker.getPosition(data.longitude, data.latitude)
            );
          })
          markers.push(marker)

          //添加监听事件，当前缩放级别
          AMap.event.addListener(map, 'zoomend', function () {
            var zoom = map.getZoom();
            // 关闭信息窗体
            map.clearInfoWindow(infoWindow);
          });
        })
        // 点聚合配置
        cluster = new AMap.MarkerClusterer(map, markers, {
          // 精确度
          gridSize: 1
        });

        const sw = this.getSW(list) // 循环所有的点标记，返回最西南的一个经纬度
        const ne = this.getNE(list) // 循环所有的点标记，返回最东北的一个经纬度
        var mybounds = new AMap.Bounds(sw, ne)
        map.setBounds(mybounds)
      },
      // 跳转指定页面
      goToException(num) {
        if (num == 0) {
          console.log('跳转实时数据页面');
        } else if (num == 4) {
          console.log('跳转设备充电页面');
        } else {
          console.log('跳转异常数据页面并判断传参');
        }
      },
      // 页面初始化请求数据
      getException(pageNum) {
        // 心率
        exception({type: 'heart_rate', id: this.idList.heart_rateId, pageNum: pageNum}).then(res => {
          if (res.rows.length === 0) {
            // console.log('not data');
          } else {
            // 赋值最新id
            this.idList.heart_rateId = res.rows[res.rows.length - 1].id
            // 总数赋值
            this.heart_rates = res.total
            if (this.heart_rateRows.length !== 0) {
              // console.log("更新")
              // 请求几条  就删除最前面的几条
              this.heart_rateRows.splice(0, res.rows.length)
            }
            // 遍历每一条数据 push
            res.rows.forEach(item => {
              this.heart_rateRows.push([
                `<div onclick="goDetailPage(` + item.id + `)" style="display:flex; justify-content: space-between; cursor: pointer;">` +
                `<div style="min-width: 60px;text-align: center;">` + item.nickName + `</div>` +
                `<div style="min-width: 20px;text-align: center;">` + this.judgeSex(item.sex) + `</div>` +
                `<div style="min-width: 30px;text-align: center;">` + item.age + `岁</div>` +
                `<div style="min-width: 30px; color:#f4fa02;text-align: center;">` + item.value + `</div>` +
                `<div style="min-width: 150px;text-align: center;">` + item.readTime + `</div>` +
                `<div class="el-icon-success" style="color:#f4fa02;line-height: 2.5; font-size:12px;"></div>` +
                `</div>`
              ])
            })
            // 修改滚动内容
            this.$refs['heart_rate'].updateRows(this.heart_rateRows)
          }
        })

        // 体温
        exception({type: 'temperature', id: this.idList.temperatureId, pageNum: pageNum}).then(res => {
          if (res.rows.length === 0) {
            // console.log('not data');
          } else {
            // 赋值最新id
            this.idList.temperatureId = res.rows[res.rows.length - 1].id
            if (this.temperatureRows.length !== 0) {
              // 请求几条  就删除最前面的几条
              this.temperatureRows.splice(0, res.rows.length)
            }
            // 总数赋值
            this.temperatures = res.total
            // 遍历每一条数据 push
            res.rows.forEach(item => {
              this.temperatureRows.push([
                `<div onclick="goDetailPage(` + item.id + `)" style="display:flex; justify-content: space-between;cursor: pointer;">` +
                `<div style="min-width: 60px;text-align: center;">` + item.nickName + `</div>` +
                `<div style="min-width: 20px;text-align: center;">` + this.judgeSex(item.sex) + `</div>` +
                `<div style="min-width: 30px;text-align: center;">` + item.age + `岁</div>` +
                `<div style="min-width: 30px; color:#f4fa02;text-align: center;">` + item.value + `℃</div>` +
                `<div style="min-width:150px;text-align: center;">` + item.readTime + `</div>` +
                `<div class="el-icon-success" style="color:#f4fa02;line-height: 2.5; font-size:12px;"></div>` +
                `</div>`
              ])
            })
            // 修改滚动内容
            this.$refs['temperature'].updateRows(this.temperatureRows)
          }
        })

        // 血氧
        exception({type: 'spo2', id: this.idList.spo2Id, pageNum: pageNum}).then(res => {
          if (res.rows.length === 0) {
            // console.log('not data');
          } else {
            // 赋值最新id
            this.idList.spo2Id = res.rows[res.rows.length - 1].id
            if (this.spo2Rows.length !== 0) {
              // 请求几条  就删除最前面的几条
              this.spo2Rows.splice(0, res.rows.length)
            }
            // 总数赋值
            this.spo2s = res.total
            // 遍历每一条数据 push
            res.rows.forEach(item => {
              this.spo2Rows.push([
                `<div onclick="goDetailPage(` + item.id + `)" style="display:flex; justify-content: space-between;cursor: pointer;">` +
                `<div style="min-width: 60px;text-align: center;">` + item.nickName + `</div>` +
                `<div style="min-width: 20px;text-align: center;">` + this.judgeSex(item.sex) + `</div>` +
                `<div style="min-width: 30px;text-align: center;">` + item.age + `岁</div>` +
                `<div style="min-width: 30px; color:#f4fa02;text-align: center;">` + item.value + `%</div>` +
                `<div style="min-width: 150px;text-align: center;">` + item.readTime + `</div>` +
                `<div class="el-icon-success" style="color:#f4fa02;line-height: 2.5; font-size:12px;"></div>` +
                `</div>`
              ])
            })
            // 修改滚动内容
            this.$refs['spo2'].updateRows(this.spo2Rows)
          }
        })
        // fence
        exception({type: 'fence', id: this.idList.fenceId, pageNum: pageNum}).then(res => {
          if (res.rows.length === 0) {
            // console.log('not data');
          } else {
            // 赋值最新id
            this.idList.fenceId = res.rows[res.rows.length - 1].id
            if (this.fenceRows.length !== 0) {
              // 请求几条  就删除最前面的几条
              this.fenceRows.splice(0, res.rows.length)
            }
            // 总数赋值
            this.fences = res.total
            // 遍历每一条数据 push
            res.rows.forEach(item => {
              this.fenceRows.push([
                `<div onclick="goDetailPage(` + item.id + `)" style="display:flex; justify-content: space-between;cursor: pointer;">` +
                `<div style="min-width: 60px;text-align: center;">` + item.nickName + `</div>` +
                `<div style="min-width: 20px;text-align: center;">` + this.judgeSex(item.sex) + `</div>` +
                `<div style="min-width: 30px;text-align: center;">` + item.age + `岁</div>` +
                `<div style="min-width: 30px; color:#f4fa02;text-align: center;">` + item.value + `</div>` +
                `<div style="min-width: 150px;text-align: center;">` + item.readTime + `</div>` +
                `<div class="el-icon-success" style="color:#f4fa02;line-height: 2.5; font-size:12px;"></div>` +
                `</div>`
              ])
            })
            // 修改滚动内容
            this.$refs['fence'].updateRows(this.fenceRows)
          }
        }),
        // SOS求助
        exception({type: 'sos', id: this.idList.sosId, pageNum: pageNum}).then(res => {
          if (res.rows.length === 0) {
            // console.log('not data');
          } else {
            // 赋值最新id
            this.idList.sosId = res.rows[res.rows.length - 1].id
            if (this.sosRows.length !== 0) {
              // 请求几条  就删除最前面的几条
              this.sosRows.splice(0, res.rows.length)
            }
            // 总数赋值
            this.soss = res.total
            // 遍历每一条数据 push
            res.rows.forEach(item => {
              this.sosRows.push([
                `<div onclick="goDetailPage(` + item.id + `)" style="display:flex; justify-content: space-between;cursor: pointer;">` +
                `<div style="min-width: 60px;text-align: center;">` + item.nickName + `</div>` +
                `<div style="min-width: 20px;text-align: center;">` + this.judgeSex(item.sex) + `</div>` +
                `<div style="min-width: 30px;text-align: center;">` + item.age + `岁</div>` +
                // `<div style="min-width: 30px; color:#f4fa02;text-align: center;">` + item.value + `</div>` +
                `<div style="min-width: 30px; color:#f4fa02;text-align: center;">SOS</div>` +
                `<div style="min-width: 150px;text-align: center;">` + item.readTime + `</div>` +
                `<div class="el-icon-success" style="color:#f4fa02;line-height: 2.5; font-size:12px;"></div>` +
                `</div>`
              ])
            })
            // 修改滚动内容
            this.$refs['sos'].updateRows(this.sosRows)
          }
        }),
        // BLOOD求助
        exception({type: 'blood', id: this.idList.bloodId, pageNum: pageNum}).then(res => {
          if (res.rows.length === 0) {
            // console.log('not data');
          } else {
            // 赋值最新id
            this.idList.bloodId = res.rows[res.rows.length - 1].id
            if (this.bloodRows.length !== 0) {
              // 请求几条  就删除最前面的几条
              this.bloodRows.splice(0, res.rows.length)
            }
            // 总数赋值
            this.bloods = res.total
            // 遍历每一条数据 push
            res.rows.forEach(item => {
              this.bloodRows.push([
                `<div onclick="goDetailPage(` + item.id + `)" style="display:flex; justify-content: space-between;cursor: pointer;">` +
                `<div style="min-width: 60px;text-align: center;">` + item.nickName + `</div>` +
                `<div style="min-width: 20px;text-align: center;">` + this.judgeSex(item.sex) + `</div>` +
                `<div style="min-width: 30px;text-align: center;">` + item.age + `岁</div>` +
                `<div style="min-width: 30px; color:#f4fa02;text-align: center;">` + item.value + `</div>` +
                // `<div style="min-width: 30px; color:#f4fa02;text-align: center;">BLOOD</div>` +
                `<div style="min-width: 150px;text-align: center;">` + item.readTime + `</div>` +
                `<div class="el-icon-success" style="color:#f4fa02;line-height: 2.5; font-size:12px;"></div>` +
                `</div>`
              ])
            })
            // 修改滚动内容
            this.$refs['blood'].updateRows(this.bloodRows)
          }
        }),
        // STEP求助
        exception({type: 'step', id: this.idList.stepId, pageNum: pageNum}).then(res => {
          if (res.rows.length === 0) {
            // console.log('not data');
          } else {
            // 赋值最新id
            this.idList.stepId = res.rows[res.rows.length - 1].id
            if (this.stepRows.length !== 0) {
              // 请求几条  就删除最前面的几条
              this.stepRows.splice(0, res.rows.length)
            }
            // 总数赋值
            this.steps = res.total
            // 遍历每一条数据 push
            res.rows.forEach(item => {
              this.stepRows.push([
                `<div onclick="goDetailPage(` + item.id + `)" style="display:flex; justify-content: space-between;cursor: pointer;">` +
                `<div style="min-width: 60px;text-align: center;">` + item.nickName + `</div>` +
                `<div style="min-width: 20px;text-align: center;">` + this.judgeSex(item.sex) + `</div>` +
                `<div style="min-width: 30px;text-align: center;">` + item.age + `岁</div>` +
                `<div style="min-width: 30px; color:#f4fa02;text-align: center;">` + item.value + `</div>` +
                // `<div style="min-width: 30px; color:#f4fa02;text-align: center;">BLOOD</div>` +
                `<div style="min-width: 150px;text-align: center;">` + item.readTime + `</div>` +
                `<div class="el-icon-success" style="color:#f4fa02;line-height: 2.5; font-size:12px;"></div>` +
                `</div>`
              ])
            })
            // 修改滚动内容
            this.$refs['step'].updateRows(this.stepRows)
          }
        })
      },
      // 实时数据
      getRealTimeData() {
        realTimeData().then(res => {
          if (res.data.length > 0) {
            // 遍历每一条数据 push
            res.data.forEach(item => {
              this.realTimeDataRows.push([
                `<div onclick="goDataBoard(` + item.userId + `)" style="display:flex; justify-content: space-between; cursor: pointer;">` +
                `<div style="min-width: 60px;text-align: center;">` + item.nickName + `</div>` +
                `<div style="min-width: 25px;text-align: center;">` + this.judgeSex(item.sex) + `</div>` +
                `<div style="min-width: 25px;text-align: center;">` + item.age + `岁</div>` +
                `<div style="min-width: 25px;text-align: center;color:#ffb980">` + (item.heartRateValue === null ? "-" : item.heartRateValue) + `</div>` +
                `<div style="min-width: 25px;text-align: center;color: #f384ab">` + (item.spo2Value === null ? "-" : item.spo2Value + "%") + `</div>` +
                `<div style="min-width: 40px;text-align: center;color: #22c3aa">` + (item.temperatureValue === null ? "-" : item.temperatureValue + "℃") + `</div>` +
                `<div style="min-width: 150px;text-align: center;">` + item.readTime + `</div>` +
                `<div  class="el-icon-data-line" style="color:#f4fa02;line-height: 2.5; font-size:12px;"></div>` +
                `</div>`
              ])
            })
            // 修改滚动内容
            this.$refs['config'].updateRows(this.realTimeDataRows)
          }
        })
      },
      // 判断性别
      judgeSex(num) {
        let sex = ''
        if (num == 0) {
          sex = '男'
        } else if (num == 1) {
          sex = '女'
        } else {
          sex = '未知'
        }
        return sex;
      },
      goDetailPage(id) {
        this.$router.push('/exception-view/' + id)
      },
      // 跳转异常列表
      goExceptionPage(type) {
        this.$router.push({name: "exceptionSearch", params: {type: type}})
      },
      // 获取当前用户信息
      getUser() {
        getUserProfile().then(response => {
          this.deptName = response.data.dept.deptName
        });
      },
      goDevicePage() {
        this.$router.push({name: "deviceSearch", params: {wearState: "0"}});
      },
      goDataBoard(userId) {
        this.$router.push("/user-dataBoard/" + userId);
      },
      // 全屏大屏
      openNewWindow() {
        screenfull.toggle();
        if (!screenfull.isFullscreen) {
          this.$router.push("/fullIndex")
        } else {
          this.$router.push("/index")
        }
      },
      change() {
        this.isFullscreen = screenfull.isFullscreen
      },
      init() {
        if (screenfull.isEnabled) {
          screenfull.on('change', this.change)
        }
      },
      destroy() {
        if (screenfull.isEnabled) {
          screenfull.off('change', this.change)
        }
      },
      updateVRows(rows, animationIndex) {
        const { mergedConfig, animationHandler, animation } = this

        this.mergedConfig = {
          ...mergedConfig,
          data: [...rows]
        }

        this.needCalc = true

        if (typeof animationIndex === 'number') this.animationIndex = animationIndex
        if (!animationHandler) animation(true)
      }
    },
    created() {
      window.goDetailPage = this.goDetailPage
      window.goDataBoard = this.goDataBoard
    },
    mounted() {
      // 获取用户信息
      this.getUser();
      // 每一秒 获取时间
      this.getTime()
      setInterval(() => this.getTime(), 1000)
      // 获取天气
      this.getWeather()
      // 请求异常数据
      this.getException(1)
      //实时数据
      this.getRealTimeData()
      // 定时请求异常数据  两分钟请求一次
      setInterval(() => this.getException(1), 120000)
      // 设备位置
      indexUserLocation({coordinateType: 'GCJ02'}).then(res => {
        res.data.forEach(item => {
          item.url = '../../src/assets/images/watch.png'
        });
        // 获取地图          src/assets/images/watch.png

        this.getMap(res.data)
      })
      this.init()
    },
    beforeDestroy() {
      this.destroy()
    },
  }
</script>

