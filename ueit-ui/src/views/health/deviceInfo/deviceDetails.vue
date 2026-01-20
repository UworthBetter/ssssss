<template>
  <div style="margin-left: 20px; margin-right: 20px;">
    <h2>概况</h2>
    <table>
      <tr>
        <td class="s7">设备名称</td>
        <td class="s6">{{ deviceInfo.name }}</td>
        <td class="s7">设备IMEI</td>
        <td class="s6">{{ deviceInfo.imei }}</td>
        <td class="s7">设备型号</td>
        <td class="s6"><dict-tag :options="dict.type.device_type" :value="deviceInfo.type"/></td>
      </tr>
      <tr>
        <td class="s7">创建时间</td>
        <td class="s6">{{ deviceInfo.createTime }}</td>
        <td class="s7">电量</td>
        <td class="s6">{{ deviceDetailsInfo.batteryLevel }}</td>
        <td class="s7">步数</td>
        <td class="s6">{{ deviceDetailsInfo.step }}</td>
      </tr>
      <tr>
        <td class="s7">最后通讯时间</td>
        <td class="s6" colspan="5">{{ deviceDetailsInfo.lastCommunicationTime }}</td>
      </tr>
    </table>

    <h2>告警</h2>
    <table>
      <tr>
        <td class="s2">最近告警内容</td>
        <td class="s3"><dict-tag :options="dict.type.warn_type" :value="deviceDetailsInfo.alarmContent"/></td>
        <td class="s2">最近告警时间</td>
        <td class="s3">{{ deviceDetailsInfo.alarmTime }}</td>
      </tr>
    </table>

    <h2>体温</h2>
    <table>
      <tr>
        <td class="s2">测量值</td>
        <td class="s3">{{ deviceDetailsInfo.temp }}</td>
        <td class="s2">测量时间</td>
        <td class="s3">{{ deviceDetailsInfo.tempTime }}</td>
      </tr>
    </table>

    <h2>心率</h2>
    <table>
      <tr>
        <td class="s2">测量值</td>
        <td class="s3">{{ deviceDetailsInfo.heartRate }}</td>
        <td class="s2">测量时间</td>
        <td class="s3">{{ deviceDetailsInfo.heartRateTime }}</td>
      </tr>
    </table>

    <h2>血氧</h2>
    <table>
      <tr>
        <td class="s2">测量值</td>
        <td class="s3">{{ deviceDetailsInfo.spo2 }}</td>
        <td class="s2">测量时间</td>
        <td class="s3">{{ deviceDetailsInfo.spo2Time }}</td>
      </tr>
    </table>

    <h2>血压</h2>
    <table>
      <tr>
        <td class="s7">舒张压</td>
        <td class="s6">{{ deviceDetailsInfo.bloodDiastolic }}</td>
        <td class="s7">收缩压</td>
        <td class="s6">{{ deviceDetailsInfo.bloodSystolic }}</td>
        <td class="s7">测量时间</td>
        <td class="s6">{{ deviceDetailsInfo.bloodTime }}</td>
      </tr>
    </table>

    <h2>位置</h2>
    <table>
      <tr>
        <td class="s7">经度</td>
        <td class="s6">{{ deviceDetailsInfo.longitude }}</td>
        <td class="s7">纬度</td>
        <td class="s6">{{ deviceDetailsInfo.latitude }}</td>
        <td class="s7">定位方式</td>
        <td class="s6"><dict-tag :options="dict.type.location_type" :value="deviceDetailsInfo.type"/></td>
      </tr>
      <tr>
        <td class="s7">定位时间</td>
        <td class="s6">{{ deviceDetailsInfo.positioningTime }}</td>
        <td class="s7">位置</td>
        <td class="s6" colspan="3">{{ deviceDetailsInfo.location }}</td>
      </tr>
    </table>
  </div>
</template>

<script>
  import { getDeviceInfo } from "@/api/health/deviceInfo";
  import { getDeviceInfoExt } from "@/api/health/deviceInfoExt";

  export default {
    name: "deviceDetails",
    dicts: ['device_type','location_type','warn_type'],
    data() {
      return {
        //设备信息
        deviceInfo: {
          userId: null,
          name: null,
          imei: null,
          type: null,
          createTime: null,
        },
        //设备详情
        deviceDetailsInfo: {
          lastCommunicationTime: null,
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
          type: null,
          positioningTime: null,
        }
      };
    },
    created() {
      // 获取传递过来的参数
      // const deviceId = this.$route.query.deviceId;
      //router下的index.js方式
      const deviceId = this.$route.params.deviceId;
      getDeviceInfo(deviceId).then(response => {
        this.deviceInfo = response.data;
      });
      getDeviceInfoExt(deviceId).then(response => {
        this.deviceDetailsInfo = response.data;
      });
    },
  };
</script>

<style scoped>
  table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 20px;
  }

  table td {
    padding: 10px;
    border: 1px solid #ccc;
  }

  h2 {
    margin-top: 30px;
    font-size: 18px;
  }

  td {
    padding: 10px;
  }

  .s1 {
    width: 100px;
    background-color: #f2f2f2;
    font-size: 14px;
  }

  .s2 {
    width: 120px;
    background-color: #f2f2f2;
    font-size: 14px;
  }

  .s3 {
    width: 240px;
    font-size: 14px;
  }

  .s4 {
    width: 160px;
    font-size: 14px;
  }
  .s5 {
    font-size: 14px;
  }
  .s6 {
    width: 280px;
    font-size: 14px;
  }
  .s7 {
    width: 90px;
    background-color: #f2f2f2;
    font-size: 14px;
  }
</style>
