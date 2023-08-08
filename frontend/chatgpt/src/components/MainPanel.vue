<template>
  <div class="main-content">
    <div class="first-row">
      <div class="questionSummaryContainer">
        <div id="questionSummary"></div>
      </div>

      <div class="accountStatisticsContainer">
        <div id="accountStatistics"></div>
      </div>
    </div>

    <div class="second-row">
      <div class="questionTable">
        <el-table
          :data="tableData"
          style="width: 100%"
          height="40vh"
          class="cell-no-wrap"
          v-loadmore="loadMore"
          :header-cell-style="thStyleFun"
          :cell-style="cellStyleFun"
        >
          <el-table-column prop="userName" label="用户" > </el-table-column>
          <el-table-column prop="model" label="模型" :min-width="180" :show-overflow-tooltip="true">
          </el-table-column>
          <el-table-column prop="account" label="账号" :min-width="180" :show-overflow-tooltip="true">
          </el-table-column>
          <el-table-column prop="accountLevel" label="账号等级" :show-overflow-tooltip="true">
          </el-table-column>
          <el-table-column prop="status" label="状态" :min-width="100">
            <template slot-scope="scope">
              <span
                :style="{
                  color:
                    scope.row.status === 'SUCCESS'
                      ? 'green'
                      : scope.row.status === 'RUNNING'
                      ? 'orange'
                      : 'red',
                }"
                >{{ scope.row.status }}</span
              >
            </template>
          </el-table-column>
          <el-table-column prop="time" label="日期" :min-width="200" :show-overflow-tooltip="true">
          </el-table-column>
          <el-table-column
            width="auto"
            prop="question"
            label="问题"
            :min-width="180"
            :show-overflow-tooltip="true"
          >
          </el-table-column>
          <el-table-column prop="answer" label="回答" :min-width="300" :show-overflow-tooltip="true">
          </el-table-column>
        </el-table>
      </div>
      <div class="userUsageOrder">
        <div class="orderText">用户使用统计</div>
        <el-table
          :data="userUsageList"
          :show-header="false"
          :border="false"
          style="width: 200px"
          class="order-table"
        >
          <el-table-column prop="avatar" width="45">
            <template slot-scope="scope">
              <el-avatar shape="circle" :src="scope.row.avatar"></el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="userName" width="80">
            <template slot-scope="scope">
              <div class="username" style="color: #88ada6">{{ scope.row.userName }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="allCount" width="60">
            <template slot-scope="scope">
              <div class="usage" style="color: green">{{ scope.row.allCount }}</div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>
    
  <style>
.main-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.first-row {
  display: flex;
  justify-content: space-between;
  padding: 16px;
}

.questionSummaryContainer {
  background-color: #1d1f24;
  padding-top: 10px;
  border-radius: 8px;
  box-shadow: 10px #090a0b;
}

.accountStatisticsContainer {
  background-color: #1d1f24;
  padding-top: 10px;
  border-radius: 8px;
  box-shadow: 10px #090a0b;
}

#questionSummary {
  width: 550px;
  height: 300px;
}

#accountStatistics {
  width: 500px;
  height: 300px;
}

.second-row {
  flex: 1;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  opacity: 0.8;
}

.userUsageOrder {
  display: flex;
  flex-direction: column;
  width: 200px;
  background-color: #1d1f24;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 10px #090a0b;
}

.questionTable {
  width: 800px;
  background-color: #1d1f24;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 10px #090a0b;
  /* height: 400px; */
}



.el-table,
.el-table__expanded-cell {
  background-color: transparent !important;
}
.el-table th,
.el-table tr,
.el-table td {
  background-color: transparent !important;
}

.el-table__row > td {
  border: none;
}
/* 去掉上面的线 */
.el-table th.is-leaf {
  border: none;
}
/* 去掉最下面的那一条线 */
.el-table::before {
  height: 0px;
}

.orderText {
  font-size: 20px;
  color: #8c95b0;
}

.order-table {
  margin-top: 16px;
}

.userUsageOrder .el-table td.el-table__cell {
  border-bottom: none;
  padding: 2px;
  text-align: center;
}

.userUsageOrder .el-table .cell {
  padding: 0px;
}

.userUsageOrder .el-table__row .el-table__cell:nth-child(2) {
  text-align: left;
}
</style>
    <script>
import { getQuestionSummary, getAccountStatistics, getUserUsageStatistics, getRecord } from "@/util/api";
import * as echarts from "echarts";

export default {
  name: "MainPanel",
  props: {},
  data() {
    return {
      timer3: null,
      timer10: null,
      tableData: [],
      avatarSize: "medium",
      userUsageList: [],
      dailyStatistics: {},
      accountStatistics: {},
      questionSummaryChart: null,
      accountStatisticsChart: null,
      cursor: 0,
      accountStatisticsOption: {
        title: {
          text: "账号统计",
          left: "center",
          top: "0%",
          textStyle: {
            color: "#00e09e",
          },
        },
        grid: {
          bottom: "10%",
        },
        xAxis: {
          type: "category",
          data: ["free", "plus"],
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
        series: [
          {
            name: "plusPublic",
            type: "bar",
            data: [],
            itemStyle: {
              color: "#9ECA7F",
            },
            emphasis: {
              focus: "series",
            },
          },
          {
            name: "freePublic",
            type: "bar",
            data: [],
            itemStyle: {
              color: "#5A6FC0",
            },
            emphasis: {
              focus: "series",
            },
          },
          {
            name: "available",
            type: "bar",
            data: [],
            itemStyle: {
              color: "#E78C8C",
            },
            emphasis: {
              focus: "series",
            },
          },
          {
            name: "All",
            type: "bar",
            data: [],
            itemStyle: {
              color: "#F8C291",
            },
            emphasis: {
              focus: "series",
            },
          },
        ],
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow",
          },
        },
        legend: {
          data: ["plusPublic", "freePublic", "available", "All"],
          textStyle: {
            color: "#fff",
          },
          inactiveColor: "#888",
          top: "10%", // 添加了这个值以避免和标题重叠
        },
      },
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
    };
  },
  mounted() {
    this.cursor = 0;

    this.fetchRecord();
    this.showUserUsage();
    this.showQuestionSummary();
    this.showAccountStatistics();
    this.timer3 = setInterval(() => {
      this.showUserUsage();
      if (this.cursor == 10) {
        this.cursor = 0;
        this.fetchRecord();
      }
    }, 3000);
    this.timer10 = setInterval(() => {
      this.showQuestionSummary();
      this.showAccountStatistics();
    }, 10000);
  },

  beforeDestroy() {
    // 组件销毁前清除定时器
    clearInterval(this.timer3);
    clearInterval(this.timer10);
  },
  methods: {
    getRandomColor() {
      var letters = "0123456789ABCDEF";
      var color = "#";
      for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
      }
      return color;
      // 为了生成淡颜色，我们让色相（Hue）在 0-360 之间随机，保持饱和度（Saturation）在 50%-100% 之间，亮度（Lightness）在 70%-90% 之间。
      // const hue = Math.floor(Math.random() * 361);
      // const saturation = Math.floor(Math.random() * 51) + 50; // 50% - 100%
      // const lightness = Math.floor(Math.random() * 21) + 70; // 70% - 90%

      // // 返回 HSL 字符串
      // return `hsl(${hue},${saturation}%,${lightness}%)`;
    },

    getRandomColorSmooth() {
        // 为了生成淡颜色，我们让色相（Hue）在 0-360 之间随机，保持饱和度（Saturation）在 50%-100% 之间，亮度（Lightness）在 70%-90% 之间。
      const hue = Math.floor(Math.random() * 361);
      const saturation = Math.floor(Math.random() * 51) + 50; // 50% - 100%
      const lightness = Math.floor(Math.random() * 21) + 70; // 70% - 90%

      // 返回 HSL 字符串
      return `hsl(${hue},${saturation}%,${lightness}%)`;
    },

    async fetchRecord() {
      //执行一次，然后每隔10秒执行一次

      getRecord(this.cursor)
        .then((res) => {
          if (res.status == 200) {
            let records = res.data;
            console.log(records);
            //将records中每一个的time改到东八区
            for (let i = 0; i < records.length; i++) {
              let timestamp = records[i].time;
              let date = new Date(timestamp);
              let formatted = date.toLocaleString("zh-CN", { timeZone: "Asia/Shanghai", hour12: false });
              records[i].time = formatted;
            }
            if (this.cursor == 0) {
              this.tableData = records;
            } else {
              for (let i = 0; i < records.length; i++) {
                this.tableData.push(records[i]);
              }
            }

            this.cursor += 10;
          } else {
            this.$message({
              message: res.status,
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

    loadMore() {
      this.fetchRecord();
    },

    async showUserUsage() {
      getUserUsageStatistics()
        .then((res) => {
          if (res.status == 200) {
            this.userUsageList = res.data;
          } else {
            this.$message({
              message: res.status,
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

    async showQuestionSummary() {
      if (this.questionSummaryChart == null) {
        this.questionSummaryChart = echarts.init(document.getElementById("questionSummary"));
        this.questionSummaryChart.setOption(this.questionSummaryOptin);
      }
      getQuestionSummary(7)
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

            //设置随机颜色
            for (var i = 0; i < option.series.length; i++) {
              let itemStyle = {};
              itemStyle.color = this.getRandomColor();
              option.series[i].itemStyle = itemStyle;
            }

            // if (this.questionSummaryChart == null) {
            //   this.questionSummaryChart = echarts.init(document.getElementById("questionSummary"));
            // }
            // this.questionSummaryChart = echarts.init(document.getElementById("questionSummary"));
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
    async showAccountStatistics() {
      if (this.accountStatisticsChart == null) {
        this.accountStatisticsChart = echarts.init(document.getElementById("accountStatistics"));
        this.accountStatisticsChart.setOption(this.accountStatisticsOption);
      }
      getAccountStatistics()
        .then((res) => {
          if (res.status == 200) {
            let accountStatistics = res.data;

            var option = this.accountStatisticsOption;

            option.series[1].data.push(accountStatistics.freeAccountFreePublicNum);
            option.series[1].data.push(accountStatistics.plusAccountFreePublicNum);

            option.series[0].data.push(accountStatistics.freeAccountPlusPublicNum);
            option.series[0].data.push(accountStatistics.plusAccountPlusPublicNum);

            option.series[2].data.push(accountStatistics.freeAccountAvailableNum);
            option.series[2].data.push(accountStatistics.plusAccountAvailableNum);

            option.series[3].data.push(accountStatistics.freeAccountNum);
            option.series[3].data.push(accountStatistics.plusAccountNum);

            for (var i = 0; i < option.series.length; i++) {
              let itemStyle = {};
              itemStyle.color = this.getRandomColorSmooth();
              option.series[i].itemStyle = itemStyle;
            }

            this.accountStatisticsChart.setOption(option);
          } else {
            this.$message({
              message: "获取账户统计失败",
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

    thStyleFun() {
      return "text-align:center";
    },

    cellStyleFun() {
      return "text-align:center;font-size: 14px;color:#F8F8F8";
    },
  },
};
</script>
  