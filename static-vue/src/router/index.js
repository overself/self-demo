//引入vue-router路由插件
import VueRouter from "vue-router";
//引入Vue
import Vue from "vue";
import routeList from "./routes";

//使用VueRouter插件
Vue.use(VueRouter)

//备份一次VUE原有的push函数，需要再次调用
let orgRouterPush = VueRouter.prototype.push;
//重写push方法
VueRouter.prototype.push = function (location, resolve, reject) {
    //判断是否增加了成功与失败的回调函数
    if (resolve && reject){
        //call和apply：都可以调用函数一次，都可以篡改函数上下文一次
        //call: 参数要用都好分开顺次传递，apply可以使用数据组一次顺序设定
        orgRouterPush.call(this,location,resolve,reject);
    } else {
        orgRouterPush.call(this,location,()=>{},(error)=>{console.log("您重复调用，请修改参数：", error.toString())});
    }
}

//对外暴露VueRouter类的实例
let router = new VueRouter({
    //配置路由
    //第一:路径的前面需要有/(不是二级路由)
    //路径中单词都是小写的
    //component右侧V别给我加单引号【字符串：组件是对象（VueComponent类的实例）】
    routes: routeList,
});

export default router;