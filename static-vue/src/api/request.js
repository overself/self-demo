/**
 * Axios 是一个基于 promise 的 HTTP 库，可以用在浏览器和 node.js 中。
 * 针对Axios的二次处理
 */
import axios from "axios";
import nProgress from "nprogress"
import 'nprogress/nprogress.css'

/**
 * 创建实例:可以使用自定义配置新建一个 axios 实例
 * @type {AxiosInstance}
 */
const requests = axios.create({
    baseURL: '/api',
    timeout: 5000,
})

/**
 * 请求拦截器处理
 */
requests.interceptors.request.use((config) => {
    //config: 配置对象，有很重要的对象headers
    //TODO:在发送请求之前做些什么
    nProgress.start();

    return config;
}, (error) => {
    //TODO:对请求错误做些什么
    return Promise.reject(error);
})

/**
 * 响应拦截器处理
 */
requests.interceptors.response.use((res) => {
    //TODO:对响应数据做点什么
    nProgress.done();
    return res.data;
}, error => {
    //TODO:对响应错误做点什么
    return Promise.reject(error);
})

export default requests;
