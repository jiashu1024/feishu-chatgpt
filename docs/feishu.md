1. 浏览器登录[飞书](https://aiw8qncwo9a.feishu.cn/accounts/page/login?redirect_uri=https%3A%2F%2Faiw8qncwo9a.feishu.cn%2Fadmin%2Findex&app_id=13)

2. 创建新企业账号(个人用户无法使用应用机器人)

![image-20230623033930446](./assets/image-20230623033930446.png)

3. [创建应用](https://open.feishu.cn/app?lang=zh-CN)

 配置应用能力

<img src="./assets/image-20230623033948491.png" alt="image-20230623033948491" style="zoom:25%;" />

 配置Encrypt Key，自定义个密码输进去就ok了，后面配置文件要用

<img src="./assets/image-20230623034003565.png" alt="image-20230623034003565" style="zoom:25%;" />

所有的权限都开通
同时将通讯录下所有权限都开通

<img src="./assets/image-20230623034018518.png" alt="image-20230623034018518" style="zoom:25%;" />

填写application-sample.yaml配置文件，其中 redis 相关的如果用 docker 方式启动可以保持默认

```shell
my-config:
  app-id:    #见下图   
  app-secret:  #见下图
  encrypt-key:  #见下图
  verification-token:   #见下图
  proxy-url: http://rnproxy.zhangjiashu.tech #已挂，自建或者使用别人的
  user-name: admin   #前端页面的登录账号和密码
  password: admin
```



<img src="./assets/image-20230623034037529.png" alt="image-20230623034037529" style="zoom:25%;" />



配置文件的代理可选择默认用我的代理(已经挂掉)，或者自建，相关项目链接： [教程](https://github.com/linweiyuan/go-chatgpt-api)

前端配置.env文件

```shell
VUE_APP_API_BASE_URL=http://ip:9001/    #将 ip 换成服务器的 ip 就行
```



启动项目，项目根目录下运行

```shell
sudo docker-compose up -d

#查看日志
sudo docker-compose logs -f
```

保证服务器防火墙端口9001打开，前端需要放开 9002 端口

浏览器打开输入http://ip:9001/ping，如果响应pong则9001端口打开了

配置事件订阅地址

<img src="./assets/image-20230623034053296.png" alt="image-20230623034053296" style="zoom:25%;" />

配置卡片事件地址

<img src="./assets/image-20230623034110515.png" alt="image-20230623034110515" style="zoom:25%;" />



<img src="./assets/image-20230623034121825.png" alt="image-20230623034121825" style="zoom:25%;" />

发布应用

<img src="./assets/image-20230623034141206.png" alt="image-20230623034141206" style="zoom:33%;" />



申请线上发布

审核https://pw6tyxlat7m.feishu.cn/admin/appCenter/audit/

<img src="./assets/image-20230623034154112.png" alt="image-20230623034154112" style="zoom:25%;" />

搜索你的应用名称

<img src="./assets/image-20230623034214490.png" alt="image-20230623034214490" style="zoom:25%;" />

私聊即可使用

在前端项目的账号管理页面添加账号即可

