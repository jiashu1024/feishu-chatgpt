<template>
    <div id="login">
      <div class="loginForm">
        <el-form ref="form" :model="form" :rules="rules" label-width="80px" label-position="top">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input placeholder="请输入密码" v-model="form.password" show-password></el-input>
          </el-form-item>
          <el-form-item class="captcha-container" prop="captcha">
            <div style="display: flex; align-items: center">
              <el-input placeholder="请输入验证码" style="width: 130px" v-model="form.captcha"></el-input>
              <img
                class="captchaImage"
                :src="captchaImageBase64"
                @click="getCaptchaImage"
                alt=""
                style="width: 130px; height: 48px; margin-left: 10px"
              />
            </div>
          </el-form-item>
          <el-form-item class="custom-button">
            <el-button style="width: 130px" plain round @click="handleLogin('form')">登录</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="authent" v-if="authentVisible">
        <img src="../assets/puff.svg" />
        <p>认证中...</p>
      </div>
    </div>
  </template>
    <script>
  import { getCaptcha, login } from "@/util/api";
  export default {
    name: "Login",
    data() {
      return {
        form: {
          username: null,
          password: null,
          captcha: null,
          key: "",
        },
        captchaImageBase64: "",
        authentVisible: true,
        rules: {
          username: [{ required: true, message: "", trigger: "blur" }],
          password: [{ required: true, message: "", trigger: "blur" }],
          captcha: [{ required: true, message: "", trigger: "blur" }],
        },
      };
    },
  
    mounted() {
      var token = localStorage.getItem("token");
      if (token) {
        this.$router.push("/main");
      }

      this.getCaptchaImage();
    },
    methods: {
      getCaptchaImage() {
        getCaptcha()
          .then((res) => {
            this.form.key = res.data.data.key;
            this.captchaImageBase64 = res.data.data.image;
            this.form.captcha = "";
          })
          .catch((err) => {
            this.$message.error(err);
          });
      },
  
      handleLogin(form) {
    
        this.$refs[form].validate((valid) => {
          if (valid) {
            const loginForm = document.querySelector(".loginForm");
            loginForm.classList.add("rotate");
            setTimeout(() => {
              loginForm.classList.add("moveLeft");
            }, 500);
            setTimeout(() => {
              const authentDiv = document.querySelector(".authent");
              this.authentVisible = true;
              authentDiv.classList.add("authentMove");
  
              setTimeout(() => {
                login(this.form)
                  .then((res) => {
                    // console.log(res);
                    if (res.status === 200) {
                      
                      var data = res.data;
  
                      if (data.success) {
                       
                        localStorage.setItem("token", data.data.token);
                        this.$message.success("登录成功");

                        setTimeout(() => {
                        this.$router.push("/main");
                        }, 1000);
                      } else {
                        this.$message.error(data.error);
                        this.getCaptchaImage();
                      }
                    } else {
                      this.$message.error(res.status);
                    }
                  })
                  .catch((err) => {
                    this.$message.error(err);
                  })
                  .finally(() => {
                    authentDiv.classList.remove("authentMove");
                    loginForm.classList.remove("moveLeft");
                    setTimeout(() => {
                      loginForm.classList.remove("rotate");
                    }, 500);
                  });
              }, 1000);
            }, 1000);
          } else {
            // console.log('error submit!!');
          }
        });
      },
    },
  };
  </script>
    
    <style>
  #login {
    height: 100vh;
    width: 100vw;
    display: flex;
    perspective: 1000px;
  
    background: #fc5c7d;
    background: -webkit-linear-gradient(to right bottom, #6a82fb, #fc5c7d);
    background: linear-gradient(to right bottom, #6a82fb, #fc5c7d);
    /* transition: transform 0.2s ease-in-out; */
  }
  
  .authent {
    background: #35394a;
    background: -webkit-linear-gradient(45deg, #35394a 0%, #1f222e 100%);
    background: linear-gradient(45deg, #35394a 0%, #1f222e 100%);
    filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#35394a', endColorstr='#1f222e',GradientType=1 );
    position: absolute;
    left: 0;
    right: 90px;
    margin: auto;
    width: 100px;
    color: white;
    text-transform: uppercase;
    /* 设置圆角 */
    border-radius: 10px;
    letter-spacing: 1px;
    text-align: center;
    padding: 20px 70px;
    top: 100px;
    bottom: 0;
    height: 70px;
    opacity: 0;
    z-index: -999;
    box-shadow: 0px 20px 30px 3px rgba(0, 0, 0, 0.55);
    transition: all 0.5s;
  }
  
  .authentMove {
    opacity: 0.8;
    left: 400px;
  }
  
  .authent p {
    text-align: center;
    color: white;
  }
  
  .loginForm {
    top: 20px;
  
    width: 260px;
    height: 320px;
    border-radius: 10px;
    box-shadow: 0 0 10px 0 #090a0b;
    border-top: 2px solid #d8312a;
    padding: 20px;
  
    position: absolute;
    left: 0;
    right: 0;
    margin: auto;
    top: 0;
    bottom: 0;
    background: #262935;
    opacity: 1;
    /* perspective: 1000px; */
  
    transition: all 0.5s;
    transform: rotateX(0deg);
  }
  
  .rotate {
    pointer-events: none;
    opacity: 0.6;
    filter: blur(1px);
    box-shadow: 0px 20px 30px 3px rgba(0, 0, 0, 0.55);
    top: 100px !important;
    transform: rotateX(70deg);
  }
  
  .loginForm.moveLeft {
    left: -302px !important;
  }
  
  .el-input__inner {
    background-color: transparent !important;
  }
  
  .loginForm:hover {
    transform: scale(1.05);
  }
  
  .captchaImage {
    width: 130px;
    height: 48px;
    cursor: pointer;
  }
  
  .custom-button {
    margin-left: 60px;
  }
  </style>
    