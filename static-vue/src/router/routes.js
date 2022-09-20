/**
 * 配置主路由
 */
/**
 * 配置主路由
 */
//import Home from "@/views/home";
//import Search from "@/views/search";
const Search = () => import('@/views/search')
import Register from "@/views/register";
import Login from "@/views/login";
import Detail from "@/views/detail";
import CartList from "@/views/shop/cartList";
import CartAdd from "@/views/shop/cartAdd";
import GoodsPay from "@/views/order/pay/goodsPay";
import PaySuccess from "@/views/order/pay/paySuccess";
import OrderList from "@/views/order/list/orderList";

//配置路由信息
/**
 * component: () => import('@/views/search')
 * 1. import(modulePath): 动态import引入模块, 被引入的模块会被单独打包
 * 2. 组件配置的是一个函数, 函数中通过import动态加载模块并返回,
 *     初始时函数不会执行, 第一次访问对应的路由才会执行, 也就是说只有一次请求对应的路由路径才会请求加载单独打包的js
 * 作用: 用于提高首屏的加载速度
 */
export default [
    {path: "/home", component: () => import('@/views/home')},
    {path: "/login", component: Login, meta: {"notFooter": true}},
    {
        path: "/search/:keyword?", // 是当前路由的标识名称
        name: "searchGlob", component: Search,
        // props: true
        // props: function ($router) {
        //     return {"keyword":$router.params.keyword,"q1":$router.query.param1}
        // }
        // 将params参数和query参数映射成属性传入路由组件
        props: ($router) => ({"keyword": $router.params.keyword, "q1": $router.query.param1})

    },
    {path: "/register", component: Register, meta: {"notFooter": true}},
    {path: "/detail/:id?", name: "goodsDetail", component: Detail},
    {path: "/goodsPay", name: "goodsPay", component: GoodsPay},
    {path: "/cartList", name: "shopCartList", component: CartList},
    {path: "/cartAdd", name: "addCartSuccess", component: CartAdd},
    {path: "/paySuccess", name: "payOrderSuccess", component: PaySuccess},
    {path: "/orderList", name: "OrderList", component: OrderList},
    {path: "", redirect: "/home"},
]
