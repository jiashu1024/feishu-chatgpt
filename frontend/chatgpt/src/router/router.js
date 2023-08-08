import Vue from 'vue';
import VueRouter from 'vue-router';

// 导入需要路由的组件

import MainPanel from '../components/MainPanel.vue';
import AccountManage from '../components/AccountManage.vue';
import UsageStatistics from '../components/UsageStatistics.vue';
import Login from '../components/Login.vue';
import Index from '../components/Index.vue';


// 安装Vue Router插件
Vue.use(VueRouter);

// 配置路由
const routes = [

    { path: '/', component: Login},
    {
        path: '/',
        component: Index,
        redirect: '/main',
        children: [
            { path: 'main', component: MainPanel },
            { path: 'account', component: AccountManage },
            { path: 'statistics', component: UsageStatistics },]
    },
    

];

const router = new VueRouter({
    mode: 'history',
    routes,
});

export default router;
