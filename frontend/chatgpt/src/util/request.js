// 引入axios
import axios from 'axios'
import router from '@/router/router.js'

const request = axios.create({
    baseURL: process.env.API_BASE_URL,
    timeout: 5000
})

request.interceptors.request.use(config => {

    const token = localStorage.getItem('token')

    if (token) {
        config.headers['Authorization'] = token;
    }
   
    return config
}, error => {
    // 请求出错时做些事
    return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(response => {
    return response
}, error => {
    // 如果返回的状态码为403，将路由重定向到/login
    if (error.response.status === 403) {
        localStorage.removeItem('token')
        if(router.currentRoute.path != "/") {
            router.push("/");
        }   
    }
    return Promise.reject(error)
})


export default request