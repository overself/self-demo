<template>
  <div>
    <AsideRight></AsideRight>
    <Header></Header>
    <!-- 路由组件露出出口 -->
    <router-view></router-view>
    <Footer v-show="$route.meta.notFooter === undefined? true: $route.meta.notFooter===false"></Footer>
  </div>
</template>

<script>
import Header from '@/components/header';
import Footer from "@/components/footer";
import AsideRight from "@/components/aside/right";

export default {
  name: 'App',
  components: {
    AsideRight,
    Header,
    Footer
  }
}
</script>

<style scoped>

</style>
