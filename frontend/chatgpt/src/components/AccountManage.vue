<template>
  <div>
    <div class="accountManage">
      <el-table
        :data="accounts"
        :header-cell-style="thStyleFun"
        :cell-style="cellStyleFun"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="account" label="账号" :show-overflow-tooltip="true" width="220">
        </el-table-column>
        <el-table-column prop="running" label="状态" width="80">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.running ? 'green' : scope.row.available ? 'gray' : 'red' }">{{
              scope.row.running ? "运行中" : scope.row.available ? "空闲" : "不可用"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="plusAccount" label="Plus">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.plusAccount ? '#4CAF50' : '#4286f4' }">{{
              scope.row.plusAccount ? "Plus" : "Free"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="normalPublic" label="免费模型权限">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.freePublic ? 'skyblue' : 'orange' }">{{
              scope.row.freePublic ? "Public" : "Private"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="plusPublic" label="付费模型权限">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.plusPublic ? 'skyblue' : 'orange' }">{{
              scope.row.plusPublic ? "Public" : "Private"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ownerUserName" label="添加用户"> </el-table-column>
        <el-table-column prop="createTimeFormatted" label="创建时间"> </el-table-column>
      </el-table>
    </div>
    <el-button class="addAccountButton" @click="clickAddAccountBotton" round>添加账号</el-button>
    <div>
      <el-dialog title="账号信息" :visible.sync="dialogVisible" width="70%" :before-close="handleClose">
        <el-form ref="form" :model="form" label-width="80px">
          <el-form-item label="账号">
            <el-input v-model="form.account"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password"></el-input>
          </el-form-item>
          <el-form-item label="可用性">
            <el-switch v-model="form.available" disabled active-color="#13ce66" inactive-color="#f20c00">
            </el-switch>
          </el-form-item>
          <el-form-item label="运行状态">
            <el-radio v-model="form.running" :label="true">运行中</el-radio>
            <el-radio v-model="form.running" :label="false">空闲</el-radio>
          </el-form-item>
          <el-form-item label="创建人">
            <el-select v-model="selectedUser" placeholder="请选择用户" value-key="openId">
              <el-option v-for="user in users" :key="user.openId" :label="user.name" :value="user">
                <span>{{ user.name }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="plus能力">
            <el-switch v-model="form.plusPublic" active-text="公开" inactive-text="私有"></el-switch>
            <el-transfer
              v-if="!form.plusPublic"
              v-model="form.plusPublicUsers"
              :data="plusTransferData"
              :titles="['未授权', '已授权']"
            ></el-transfer>
          </el-form-item>
          <el-form-item label="free能力">
            <el-switch v-model="form.freePublic" active-text="公开" inactive-text="私有"></el-switch>
            <el-transfer
              v-if="!form.freePublic"
              v-model="form.freePublicUsers"
              :data="freeTransferData"
              :titles="['未授权', '已授权']"
            ></el-transfer>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dealCancel">取 消</el-button>
          <el-button type="primary" :loading="buttomLoadingConfirm" @click="submitTheForm">确 定</el-button>
          <el-button type="danger" :loading="buttomLoadingDelete" @click="handleDeleteAccount"
            >删 除</el-button
          >
        </span>
      </el-dialog>
    </div>
    <div>
      <el-dialog title="账号信息" :visible.sync="addAccountVisible" width="70%" :before-close="dealCancel">
        <el-form ref="ruleForm" :rules="rules" :model="ruleForm" label-width="80px">
          <el-form-item label="账号" prop="account">
            <el-input v-model="ruleForm.account"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="ruleForm.password"></el-input>
          </el-form-item>
          <el-form-item label="创建人" prop="creator">
            <el-select v-model="ruleForm.creator" placeholder="请选择用户" value-key="openId">
              <el-option v-for="user in users" :key="user.openId" :label="user.name" :value="user">
                <span>{{ user.name }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="plus能力">
            <el-switch v-model="ruleForm.plusPublic" active-text="公开" inactive-text="私有"></el-switch>
            <el-transfer
              v-if="!ruleForm.plusPublic"
              v-model="ruleForm.plusPublicUsers"
              :data="plusTransferData"
              :titles="['未授权', '已授权']"
            ></el-transfer>
          </el-form-item>
          <el-form-item label="free能力">
            <el-switch v-model="ruleForm.freePublic" active-text="公开" inactive-text="私有"></el-switch>
            <el-transfer
              v-if="!ruleForm.freePublic"
              v-model="ruleForm.freePublicUsers"
              :data="freeTransferData"
              :titles="['未授权', '已授权']"
            ></el-transfer>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dealCancel">取 消</el-button>
          <el-button
            type="primary"
            :loading="clickAddAccountBottonLoading"
            @click="submitAddAccountFormBottonClick('ruleForm')"
            >确 定</el-button
          >
        </span>
      </el-dialog>
    </div>
  </div>
</template>
      
    <style>
.addAccountButton {
  margin-top: 20px;
}
</style>
      <script>
import { getAllAccount, getAllUser, addAccount, modifyAccount, deleteAccount } from "@/util/api";

export default {
  name: "AccountManage",
  props: {},
  data() {
    return {
      timer: null,
      accounts: [],
      dialogVisible: false,
      form: {},
      users: [],
      selectedUser: {},
      plusTransferData: [],
      freeTransferData: [],
      buttomLoadingDelete: false,
      buttomLoadingConfirm: false,
      addAccountVisible: false,
      clickAddAccountBottonLoading: false,
      ruleForm: {
        account: "",
        password: "",
        creator: null,
        plusPublic: true,
        freePublic: true,
      },
      rules: {
        account: [{ required: true, message: "请输入账号", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        creator: [{ required: true, message: "请选择创建人", trigger: "blur" }],
      },
    };
  },

  mounted() {
    this.fetchAccAcount();
    this.timer = setInterval(() => {
      this.fetchAccAcount();
    }, 1000 * 3);
  },

  beforeDestroy() {
    // 组件销毁前清除定时器
    clearInterval(this.timer);
  },

  methods: {
    fetchAccAcount() {
      getAllAccount()
        .then((res) => {
          let accounts = res.data;
          for (var i = 0; i < accounts.length; i++) {
            let timestamp = accounts[i].createTime;
            let date = new Date(timestamp);
            let formatted = date.toLocaleString("zh-CN", { timeZone: "Asia/Shanghai", hour12: false });
            accounts[i].createTimeFormatted = formatted;
          }
          this.accounts = accounts;
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
    handleRowClick(row) {
      this.dialogVisible = true;
      this.form = row;

      getAllUser()
        .then((res) => {
          let userList = res.data;
          this.users = userList;

          for (var i = 0; i < this.users.length; i++) {
            if (this.users[i].openId == row.ownerOpenId) {
              this.selectedUser = this.users[i];
            }
       
            //如果没有才push
            if (!this.plusTransferData.find((item) => item.key === this.users[i].openId)) {
              this.plusTransferData.push({
                key: this.users[i].openId,
                label: this.users[i].name,
              });
            }

            if (!this.freeTransferData.find((item) => item.key === this.users[i].openId)) {
              this.freeTransferData.push({
                key: this.users[i].openId,
                label: this.users[i].name,
              });
            }
          }
        })
        .catch((err) => {
          this.$message({
            message: err,
            type: "error",
          });
        });
    },

    handleClose() {
      this.dialogVisible = false;
      this.form = {};
    },
    handleDeleteAccount() {
      this.buttomLoadingDelete = true;
      deleteAccount(this.form)
        .then((res) => {
          this.buttomLoadingDelete = false;
          if (res.status == 200) {
            let data = res.data;
            this.dialogVisible = false;
            if (data.success) {
              this.$message({
                message: data.message,
                type: "success",
              });

              this.form = {};
              this.plusTransferData = [];
              this.freeTransferData = [];
            } else {
              this.$message({
                message: data.erroe,
                type: "error",
              });
            }
          } else {
            this.$message({
              message: res.status,
              type: "error",
            });
          }
        })
        .catch((err) => {
          this.buttomLoadingDelete = false;
          this.$message({
            message: err,
            type: "error",
          });
        });
    },
    dealCancel() {
      this.dialogVisible = false;
      this.form = {};
      this.plusTransferData = [];
      this.freeTransferData = [];
      this.addAccountVisible = false;
    },
    submitTheForm() {
      this.buttomLoadingConfirm = true;
      delete this.form.createTimeFormatted;
      modifyAccount(this.form)
        .then((res) => {
          if (res.status == 200) {
            let data = res.data;
            this.dialogVisible = false;
            if (data.success) {
              this.$message({
                message: data.message,
                type: "success",
              });

              this.form = {};
              this.plusTransferData = [];
              this.freeTransferData = [];
            } else {
              this.$message({
                message: data.erroe,
                type: "error",
              });
            }
            this.buttomLoadingConfirm = false;
          } else {
            this.buttomLoadingConfirm = false;
            this.$message({
              message: res.status,
              type: "error",
            });
          }
        })
        .catch((err) => {
          this.buttomLoadingConfirm = false;
          this.$message({
            message: err,
            type: "error",
          });
        });
    },
    clickAddAccountBotton() {
      this.addAccountVisible = true;
      getAllUser()
        .then((res) => {
          let userList = res.data;
          this.users = userList;
          for (var i = 0; i < this.users.length; i++) {
            if (!this.plusTransferData.find((item) => item.key === this.users[i].openId)) {
              this.plusTransferData.push({
                key: this.users[i].openId,
                label: this.users[i].name,
              });
            }

            if (!this.freeTransferData.find((item) => item.key === this.users[i].openId)) {
              this.freeTransferData.push({
                key: this.users[i].openId,
                label: this.users[i].name,
              });
            }
          }
        })
        .catch((err) => {
          this.$message({
            message: err,
            type: "error",
          });
        });
    },
    submitAddAccountFormBottonClick(ruleForm) {
      this.clickAddAccountBottonLoading = true;
      this.$refs[ruleForm].validate((valid) => {
        if (valid) {
         
          let user = this.ruleForm.creator;
          this.ruleForm.ownerUserName = user.name;
          this.ruleForm.ownerOpenId = user.openId;
          delete this.ruleForm.creator;
          addAccount(this.ruleForm)
            .then((res) => {
              console.log(res);
              if (res.status == 200) {
                let data = res.data;
                if (data.success) {
                  this.$message({
                    message: data.message,
                    type: "success",
                  });
                  this.addAccountVisible = false;
                  this.ruleForm = {};
                  this.plusTransferData = [];
                  this.freeTransferData = [];
                } else {
                  this.clickAddAccountBottonLoading = false;
                  this.$message({
                    message: data.error,
                    type: "error",
                  });
                }
              } else {
                this.clickAddAccountBottonLoading = false;
                this.$message({
                  message: res.status,
                  type: "error",
                });
              }
            })
            .catch((err) => {
              this.clickAddAccountBottonLoading = false;
              this.$message({
                message: err,
                type: "error",
              });
            });
        } else {
          return false;
        }
      });
    },
  },
};
</script>
    