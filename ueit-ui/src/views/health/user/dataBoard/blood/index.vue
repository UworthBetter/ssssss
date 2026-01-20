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
      setOptions({ readTimeData, diastolicData, systolicData } = {}) {
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
              name: '舒张压',
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
              name: '收缩压',
              position: 'right',
              alignTicks: true,
              axisLine: {
                show: true,
                lineStyle: {
                  color: '#a3ba24'
                }
              },
              axisLabel: {
                formatter: '{value}'
              }
            }
          ]
          ,
          legend: {
            data: ['舒张压', '收缩压']
          },
          series: [{
            name: '舒张压', itemStyle: {
              normal: {
                color: '#d09316',
                lineStyle: {
                  color: '#d09316'
                }
              }
            },
            type: 'bar',
            data: diastolicData,
            animationDuration: 2800,
            animationEasing: 'cubicInOut'
          },
            {
              name: '收缩压', itemStyle: {
                normal: {
                  color: '#a3ba24',
                  lineStyle: {
                    color: '#a3ba24'
                  }
                }
              },
              yAxisIndex: 1,
              type: 'bar',
              data: systolicData,
              animationDuration: 2800,
              animationEasing: 'cubicInOut'
            }
          ]
        })
      }
    }
  }
</script>
