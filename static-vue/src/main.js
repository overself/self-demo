import Vue from 'vue'
import App from './App.vue'

//引入页面路由信息
import router from "@/router";

//全局的组件导入，各个页面可以随时使用
import TypeNav from "@/components/typenav";
Vue.component(TypeNav.name, TypeNav);


Vue.config.productionTip = false
Vue.config.devtools = true;

new Vue({
  render: h => h(App),
  //注册路由信息：注意要K和V一致，省略V【router小写】
  //当书写router的时候，组件身上都有$route和$router两个属性
  //$route：一般获取路由信息，如：路径，params，query等
  //$router: 一般进行编程式导航进行跳转，如：push，replace
  router
}).$mount('#app')
