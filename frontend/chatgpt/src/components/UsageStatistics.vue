<template>
  <div class="statistics">
    <div class="firstRow">
      <div id="questionSummary" style="width: 100%; height: 400px"></div>
    </div>
    <div class="secondRow">
      <div id="modelChart">你好</div>
      <div id="freeReqChart"></div>
      <div id="plusReqChart"></div>
    </div>
  </div>
</template>
      
    <style>
.statistics {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.secondRow {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  width: 100%;
  height: 100%;
  min-height: 35vh;
}

#modelChart {
  width: 400px;
  height: 300px;
}

#freeReqChart {
  width: 300px;
  height: 300px;
}

#plusReqChart {
  width: 300px;
  height: 300px;
}

</style>
      <script>
import * as echarts from "echarts";
import { getQuestionSummary, getHistogramData } from "../util/api";
export default {
  name: "UsageStatistics",
  props: {},
  data() {
    return {
      questionSummaryChart: null,
      questionSummaryOptin: {
        title: {
          text: "请求统计",

          textStyle: {
            color: "#44cef6",
          },
          left: "center",
        },
        grid: {
          top: "20%", // Adjusting the grid position from the top
          bottom: "10%", // Adjusting the grid position from the bottom
        },
        xAxis: {
          type: "category",
          data: [],
          axisLabel: {
            textStyle: {
              color: "#fff",
            },
          },
        },
        yAxis: {
          type: "value",
          axisLabel: {
            textStyle: {
              color: "#fff",
            },
          },
        },
        legend: {
          data: ["GPT4Success", "GPT3Success", "GPT4Fail", "GPT3Fail"],
          textStyle: {
            color: "#fff",
          },
          selectedMode: "multiple",
          selected: {
            GPT4Success: "#ccc",
            GPT3Success: "#ccc",
            GPT4Fail: "#ccc",
            GPT3Fail: "#ccc",
          },
          top: "12%", // Adjusting the legend position from the top
          left: "center", // Centering the legend horizontally
        },
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "line",
          },
        },
        series: [
          {
            name: "GPT4Success",
            data: [],
            type: "line",
            smooth: true,
          },
          {
            name: "GPT3Success",
            data: [],
            type: "line",
            smooth: true,
          },
          {
            name: "GPT4Fail",
            data: [],
            type: "line",
            lineStyle: {
              type: "dashed",
            },
            smooth: true,
          },
          {
            name: "GPT3Fail",
            data: [],
            type: "line",
            lineStyle: {
              type: "dashed",
            },
            smooth: true,
          },
        ],
      },
      modelChart: null,
      modelChartOption: {
        title: {
          text: "模型使用次数统计",
          left: "center",
          textStyle: {
            color: "#FFFFFF", // 设置标题颜色为白色
          },
        },
        tooltip: {
          trigger: "item",
        },
        series: [
          {
            name: "模型统计",
            type: "pie",
            radius: "50%",
            data: [{}],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: "rgba(0, 0, 0, 0.5)",
              },
            },
            label: {
              color: "#FFFFFF", // 设置饼图标签颜色为白色
            },
          },
        ],
      },
      freeReqChart: null,
      freeReqChartOption: {
        title: {
          text: "免费模型次数统计",
          left: "center",
        
          textStyle: {
            color: "#FFFFFF",
          },
        },
        tooltip: {
          trigger: "item",
        },
        series: [
          {
            name: "Free次数",
            type: "pie",
            radius: ["40%", "70%"],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 3,
              borderColor: "#fff",
              
            },
            label: {
              show: false,
              position: "center",
              color: "#FFFFFF",
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 40,
                fontWeight: "bold",
                color: (params) => params.color, // 使得弹出字体颜色与对应区域颜色一致
              },
            },
            labelLine: {
              show: false,
            },
            data: [],
          },
        ],
      },
      plusReqChart: null,
      plusReqChartOption:  {
        title: {
          text: "付费模型次数统计",
          left: "center",
        
          textStyle: {
            color: "#FFFFFF",
          },
        },
        tooltip: {
          trigger: "item",
        },
        series: [
          {
            name: "Plus次数",
            type: "pie",
            radius: ["40%", "70%"],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 3,
              borderColor: "#fff",
              
            },
            label: {
              show: false,
              position: "center",
              color: "#FFFFFF",
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 40,
                fontWeight: "bold",
                color: (params) => params.color, // 使得弹出字体颜色与对应区域颜色一致
              },
            },
            labelLine: {
              show: false,
            },
            data: [],
          },
        ],
      },
    };
  },
  mounted() {
    this.showQuestionSummary();
    this.showHistogramChart();
  },
  methods: {
    getRandomColor() {
      // var letters = "0123456789ABCDEF";
      // var color = "#";
      // for (var i = 0; i < 6; i++) {
      //   color += letters[Math.floor(Math.random() * 16)];
      // }
      // return color;
      // 为了生成淡颜色，我们让色相（Hue）在 0-360 之间随机，保持饱和度（Saturation）在 50%-100% 之间，亮度（Lightness）在 70%-90% 之间。
      const hue = Math.floor(Math.random() * 361);
      const saturation = Math.floor(Math.random() * 51) + 50; // 50% - 100%
      const lightness = Math.floor(Math.random() * 21) + 70; // 70% - 90%

      // 返回 HSL 字符串
      return `hsl(${hue},${saturation}%,${lightness}%)`;
    },

    async showQuestionSummary() {
      if (this.questionSummaryChart == null) {
        this.questionSummaryChart = echarts.init(document.getElementById("questionSummary"));
      }
      getQuestionSummary(30)
        .then((res) => {
          if (res.status == 200) {
            // console.log(res.data);
            var option = this.questionSummaryOptin;

            var questionSummaryList = res.data;

            option.xAxis.data = [];
            option.series[0].data = [];
            option.series[1].data = [];
            option.series[2].data = [];
            option.series[3].data = [];
            for (var i = 0; i < questionSummaryList.length; i++) {
              option.xAxis.data.push(questionSummaryList[i].date.substring(5));
              option.series[0].data.push(questionSummaryList[i].plusSuccessCount);
              option.series[1].data.push(questionSummaryList[i].freeSuccessCount);
              option.series[2].data.push(questionSummaryList[i].plusFailCount);
              option.series[3].data.push(questionSummaryList[i].freeFailCount);
            }
            this.questionSummaryChart.setOption(option);
          } else {
            this.$message({
              message: "获取问题统计失败",
              type: "error",
            });
          }
        })
        .catch((err) => {
          this.$message({
            message: err,
            type: "error",
          });
        });
    },

    async showHistogramChart() {
      if (this.modelChart == null) {
        this.modelChart = echarts.init(document.getElementById("modelChart"));
        this.modelChart.setOption(this.modelChartOption);
      }
      if (this.freeReqChart == null) {
        this.freeReqChart = echarts.init(document.getElementById("freeReqChart"));
        this.freeReqChart.setOption(this.freeReqChartOption);
      }
      if (this.plusReqChart == null) {
        this.plusReqChart = echarts.init(document.getElementById("plusReqChart"));
        this.plusReqChart.setOption(this.plusReqChartOption);
      }

      getHistogramData()
        .then((res) => {
          if (res.status == 200) {
            console.log(res.data);
            let histogramData = res.data;
            let modelData = histogramData.modelHistogramData;
            this.modelChartOption.series[0].data = [];
            this.freeReqChartOption.series[0].data = [];
            this.plusReqChartOption.series[0].data = [];
            Object.entries(modelData).forEach(([key, value]) => {
              let o = {};
              o.name = key;
              o.value = value;

              let itemStyle = {};
              itemStyle.color = this.getRandomColor();
              o.itemStyle = itemStyle;
              this.modelChartOption.series[0].data.push(o);
            });
            let freeReqData = histogramData.freeModelHistogramData;
            Object.entries(freeReqData).forEach(([key, value]) => {
              let o = {};
              o.name = key;
              o.value = value;

              let itemStyle = {};
              itemStyle.color = this.getRandomColor();
              o.itemStyle = itemStyle;
              this.freeReqChartOption.series[0].data.push(o);
            });

            let plusReqData = histogramData.plusModelHistogramData;
            Object.entries(plusReqData).forEach(([key, value]) => {
              let o = {};
              o.name = key;
              o.value = value;

              let itemStyle = {};
              itemStyle.color = this.getRandomColor();
              o.itemStyle = itemStyle;
              this.plusReqChartOption.series[0].data.push(o);
            });

            this.modelChart.setOption(this.modelChartOption);
            this.freeReqChart.setOption(this.freeReqChartOption);
            this.plusReqChart.setOption(this.plusReqChartOption);
          } else {
            this.$message({
              message: "获取模型统计失败",
              type: "error",
            });
          }
        })
        .catch((err) => {
          this.$message({
            message: err,
            type: "error",
          });
        });
    },
  },
};
</script>
    