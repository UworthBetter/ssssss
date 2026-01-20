<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
  import * as echarts from 'echarts'
  require('echarts/theme/macarons') // echarts theme
  import resize from '../../../../dashboard/mixins/resize'

  export default {
    mixins: [resize],
    props: {
      className: {
        type: String,
        default: 'chart'
      },
      width: {
        type: String,
        default: '100%'
      },
      height: {
        type: String,
        default: '350px'
      },
      autoResize: {
        type: Boolean,
        default: true
      },
      chartData: {
        type: Object,
        required: true
      }
    },
    data() {
      return {
        chart: null
      }
    },
    watch: {
      chartData: {
        deep: true,
        handler(val) {
          this.setOptions(val)
        }
      }
    },
    mounted() {
      this.$nextTick(() => {
        this.initChart()
      })
    },
    beforeDestroy() {
      if (!this.chart) {
        return
      }
      this.chart.dispose()
      this.chart = null
    },
    methods: {
      initChart() {
        this.chart = echarts.init(this.$el, 'macarons')
        this.setOptions(this.chartData)
      },
      // 指定和使用图表的配置项和数据
      setOptions({ readTimeData, valueData } = {}) {
        this.chart.setOption({
          xAxis: {
            data: readTimeData,
            boundaryGap: false,
            axisTick: {
              show: true,
              alignWithLabel: true,
            }
          },
          grid: {
            left: 10,
            right: 10,
            bottom: 20,
            top: 30,
            containLabel: true
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross'
            },
            padding: [5, 10]
          },
          yAxis: {
            axisTick: {
              show: false
            },
            max: function (value) {
              return 100;
            },
            min: function (value) {
              return Math.floor((value.min - 5) / 10) * 10;
            }
          },
          legend: {
            data: ['血氧']
          },
          series: [{
            name: '血氧', itemStyle: {
              normal: {
                color: '#EE4442',
                lineStyle: {
                  color: '#EE4442',
                  width: 2
                }
              }
            },
            smooth: true,
            type: 'line',
            data: valueData,
            animationDuration: 2800,
            animationEasing: 'cubicInOut'
          }]
        })
      }
    }
  }
</script>
