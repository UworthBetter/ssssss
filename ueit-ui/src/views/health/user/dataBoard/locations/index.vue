<template>
  <div :class="className" :style="{height:height,width:width}"/>
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
      setOptions({ readTimeData, longitudeData, latitudeData } = {}) {
        this.chart.setOption({
          xAxis: {
            data: readTimeData,
            axisTick: {
              show: true,
              alignWithLabel: true
            }
          },
          grid: {
            left: 10,
            right: 40,
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
          yAxis: [
            {
              type: 'value',
              name: '经度',
              position: 'left',
              alignTicks: true,
              axisLine: {
                show: true
              },
              axisLabel: {
                formatter: '{value}'
              }
            },
            {
              type: 'value',
              name: '纬度',
              position: 'right',
              alignTicks: true,
              axisLine: {
                show: true,
                lineStyle: {
                  color: '#686174'
                }
              },
              axisLabel: {
                formatter: '{value}'
              }
            }
          ]
          ,
          legend: {
            data: ['经度', '纬度']
          },
          series: [{
            name: '经度', itemStyle: {
              normal: {
                color: '#579391',
                lineStyle: {
                  color: '#579391'
                }
              }
            },
            type: 'bar',
            data: longitudeData,
            animationDuration: 2800,
            animationEasing: 'cubicInOut'
          },
            {
              name: '纬度', itemStyle: {
                normal: {
                  color: '#686174',
                  lineStyle: {
                    color: '#686174'
                  }
                }
              },
              yAxisIndex: 1,
              type: 'bar',
              data: latitudeData,
              animationDuration: 2800,
              animationEasing: 'cubicInOut'
            }
          ]
        })
      }
    }
  }
</script>
