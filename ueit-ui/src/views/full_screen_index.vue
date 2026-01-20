<template>
  <Index :indexFullscreen="isFullscreen"></Index>

</template>

<script>
  import Index from './index';
  import screenfull from 'screenfull';

  export default {
    dicts: ['health_data_type', 'exception_state', 'sys_user_sex'],
    name: "full_screen_index",
    components: {Index},
    data() {
      return {
        isFullscreen: false,
        eventSource : null,
        // 连接标识
        uuids       : '',
        // 用户id
        userId      : '',
        // token
        token       : this.$store.state.user.token,
        // 实例化一个音频播放器对象
        audio: new Audio(),
        warningUrl: require("../assets/audio/message.mp3"),
      }
    },
    mounted() {
      this.init();
      this.uuid();
      this.connect();
    },
    destroyed(){
      // 关闭连接
      this.eventSource.close()
    },
    beforeDestroy() {
      this.destroy()
    },
    methods: {
      click() {
        if (!screenfull.isEnabled) {
          this.$message({ message: '你的浏览器不支持全屏', type: 'warning' })
          return false
        }
        screenfull.toggle()
      },
      change() {
        this.isFullscreen = screenfull.isFullscreen
      },
      exit(){
        if (!screenfull.isFullscreen) {
          this.$router.push("/index");
        }
      },
      init() {
        if (screenfull.isEnabled) {
          screenfull.on('change', this.change)
        }
        document.addEventListener("fullscreenchange", () => {
          this.exit();
        });
        document.addEventListener("mozfullscreenchange", () => {
          this.exit();
        });
        document.addEventListener("webkitfullscreenchange", () => {
          this.exit();
        });
        document.addEventListener("msfullscreenchange", () => {
          this.exit();
        });
      },
      destroy() {
        if (screenfull.isEnabled) {
          screenfull.off('change', this.change)
        }
      },
      // 生成唯一uuid
      uuid(){
        var s = []
        var hexDigits = '0123456789abcdef'
        for (var i = 0; i < 36; i++) {
          s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1)
        }
        s[14] = '4'
        s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1)
        s[8] = s[13] = s[18] = s[23] = '-'
        var uuid = s.join('')
        this.uuids =  uuid
      },
      // 连接
      connect(){

        var that = this;

        this.eventSource = new EventSource(process.env.VUE_APP_BASE_API + '/system/sse/connect/'+this.uuids+'/'+this.token)


        // 连接成功
        this.eventSource.onopen  =  function (e) {
          console.log('连接成功');
        }
        // 连接错误
        this.eventSource.onerror =  function (e) {
          console.log('连接失败');
        }
        // 返回消息
        this.eventSource.onmessage = function (e) {
          console.log(e.data);
          that.showMessage(e.data);
        }
      },
      goExceptionDetila(id){
        this.$router.push('/exception-view/' + id)
      },
      showMessage(dataString){
        var that = this;
        this.audio.autoplay = true;
        this.audio.src = this.warningUrl;
        if(dataString != null && dataString != ""){
          var data = JSON.parse(dataString);
          if (data.id != null && data.id != ""){
            var exceptionText = this.selectDictLabel(this.dict.type.health_data_type, data.type);
            var sexText = this.selectDictLabel(this.dict.type.sys_user_sex, data.sex);
            this.$notify({
              type: "warning",
              title: exceptionText + '异常告警',
              dangerouslyUseHTMLString: true,
              // position: "bottom-right",
              onClick: function(){
                this.close();
                that.goExceptionDetila(data.id);
              },
              message: '<div>' +
                '     <div>' +
                '       <div><span style="font-size:14px; font-weight:600; letter-spacing: 1px;">' + data.nickName + '</span><span style="margin-left: 8px; margin-right: 8px">' + sexText + '</span><span>' + data.age + '岁</span></div>' +
                '       <div><span>' + data.phonenumber + '</span></div>' +
                '     </div>' +
                '        <div>' +
                (data.type != "sos" && data.type != "fall_down" ?
                  '            <div> <span class="el-icon-tickets" style="color:#ffba00;"></span><span style="font-size:14px; font-weight:600; letter-spacing: 1px;"> 数值 ：</span>' + data.value + '</div>'
                  : "") +
                '            <div> <span class="el-icon-warning-outline" style="color:#ff4949;"></span><span style="font-size:14px; font-weight:600; letter-spacing: 1px;"> 异常时间 ：</span>' + data.createTime + '</div>' +
                '            <div> <span class="el-icon-map-location" style="color:#409eff;"></span><span style="font-size:14px; font-weight:600; letter-spacing: 1px;"> 定位 ：</span> <span>'+data.location+'</span> </div>' +
                '       </div>' +
                '    </div>',
              duration: 0
            });
          }
        }
      }
    }
  }
</script>

<style scoped>

</style>
