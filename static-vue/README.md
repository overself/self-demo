# vuedemo

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).


## 参考

### axios请求
http://www.axios-js.com/zh-cn/docs/vue-axios.html
xios 是一个基于 promise 的 HTTP 库，可以用在浏览器和 node.js 中。

#### 特性
- 从浏览器中创建 XMLHttpRequests
- 从 node.js 创建 http 请求
- 支持 Promise API
- 拦截请求和响应
- 转换请求数据和响应数据
- 取消请求
- 自动转换 JSON 数据
- 客户端支持防御 XSRF

#### 安装：
```
npm install axios
```

### 前端代理webpack
#### 作用
- 解决跨域问题
- 根据API指向不同的请求服务

#### 配置
https://www.webpackjs.com/


### nprogress 进度管理
#### 作用
- web请求服务进度条
#### 配置
npm install nprogress

### vuex 状态管理
Vuex 是一个专为 Vue.js 应用程序开发的状态管理模式 + 库。它采用集中式存储管理应用的所有组件的状态，并以相应的规则保证状态以一种可预测的方式发生变化
如果您的应用够简单，您最好不要使用 Vuex。一个简单的 store 模式就足够您所需了
#### 什么是“状态管理模式”
- 状态，驱动应用的数据源；
- 视图，以声明方式将状态映射到视图；
- 操作，响应在视图上的用户输入导致的状态变化。

#### 作用
- 管理共享状态

#### 配置
npm install vuex

