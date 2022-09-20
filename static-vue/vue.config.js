/**
 * 配置参考
 * https://cli.vuejs.org/zh/config/#vue-config-js
 */
const {defineConfig} = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    lintOnSave: false,  // process.env.NODE_ENV !== 'production', //关闭ESLink的语法校验，避免开发语法不必要的问题解析问题，如没有使用的临时变量
    //前端请求服务的代理配置
    devServer : {
        proxy : {
            '/api' : {
                target: 'http://localhost:8088',
                pathRewrite : {"^/api": "/api"}
            }
        }
    }
})
