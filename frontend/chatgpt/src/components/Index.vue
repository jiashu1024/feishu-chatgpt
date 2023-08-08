<template>
  <div class="container">
    <div class="sidebar">
      <div class="menu">
        <el-menu
          :default-active="activeIndex"
          class="new-el-menu--sidebar"
          background-color="#1D1F24"
          text-color="#8c95b0"
          active-text-color="#ffffff"
          :collapse-transition="true"
          @select="handleSelect"
          @open="handleOpen"
          @close="handleClose"
          router
          :collapse="foldTheSidebar"
        >
          <el-menu-item index="0" class="logo" disabled>
            <img class="avatar" src="../assets/logo.jpg" alt="" />
            <span slot="title" class="logoText">ChatGPT后台</span>
          </el-menu-item>
          <el-menu-item index="/main">
            <i class="el-icon-menu"></i>
            <span slot="title">主面板</span>
          </el-menu-item>
          <el-menu-item index="/account">
            <i class="el-icon-setting"></i>
            <span slot="title">账号管理</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <i class="el-icon-pie-chart"></i>
            <span slot="title">统计</span>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
    <div class="main">
      <div class="navbar">
        <!-- 导航链接 -->
        <div v-on:click="changeFoldStatus">
          <i v-if="!foldTheSidebar" class="fold el-icon-s-fold"></i>
          <i v-if="foldTheSidebar" class="fold el-icon-s-unfold"></i>
        </div>
        <div class="crumbs">
          <div class="crumbsContent">{{ breadcrumb }}</div>
        </div>
      </div>
      <div class="content">
        <router-view></router-view>
        <!-- 主体内容 -->
      </div>
    </div>
  </div>
</template>
  
<style>

template {
  height: 100%;
}

.container {
  background-color: #131417;
  /* height: 100%; */
  display: flex;
  padding: 16px 0;
  min-height: 100vh;


}

.sidebar {
  background-color: #1d1f24;
  border-radius: 0 8px 8px 0;
  box-shadow: 0 0 10px 0 #090a0b;
  /* margin-bottom: 10vh; */
  height: 95vh;
  min-height: 600px;
  min-width: 60px;
  /* margin-top: 10vh; */

}

.logo {
  border-bottom: 2px #282c38 solid;

  cursor: default !important;
  opacity: 1 !important;
  margin-bottom: 10px;
}

.logo .el-tooltip {
  left: -8px !important;
}

.logoText {
  color: #ffffff;
  font-size: 16px;
  margin-left: 10px;
}

.new-el-menu--sidebar {
  width: 100%;
}

.new-el-menu--sidebar:not(.el-menu--collapse) {
  width: 180px;
}

.menu {
  display: flex;
  padding-top: 40px;
  justify-content: center;
}

.el-menu {
  border: 0 !important;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.navbar {
  margin: 0px 16px;
  height: 30px;
  display: flex;
  padding: 16px;
  border-radius: 20px;
  box-shadow: 0 0 10px 0 #090a0b;
  background-color: #1d1f24;
  align-items: center;
}

.navbar i.fold {
  color: #ffffff;
  font-size: 24px;
  cursor: pointer;
}

.crumbs {
  margin-left: 20px;
  color: #ffffff !important;
  font-size: 18px;
}

.content {
  /* height: calc(100vh - 50px); */
  padding: 16px;
  /* margin-bottom: 15vh; */
}
</style>
  <script>
export default {
  name: "Index",
 
  data() {
    return {
      property: "value",
      foldTheSidebar: true,
      breadcrumb: "主面板",
      activeIndex: this.$route.path,
    };
  },
  mounted() {
     this.switchToHome(this.$route.path);
  },
  methods: {
    handleOpen() {},
    handleClose() {},
    handleSelect(key, keyPath) {
      // 处理导航菜单点击事件
      this.switchToHome(key);
    },
    changeFoldStatus() {
      this.foldTheSidebar = !this.foldTheSidebar;
    },
    switchToHome(key) {
      switch (key) {
        case "/main":
          this.breadcrumb = "主面板";
          break;
        case "/account":
          this.breadcrumb = "账号管理";
          break;
        case "/statistics":
          this.breadcrumb = "统计";
          break;
      }
    },
  },
};
</script>
