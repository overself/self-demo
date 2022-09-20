<template>
  <div>
    <TypeNav></TypeNav>
    <ListContainer></ListContainer>
    <Recommend></Recommend>
    <Rank></Rank>
    <Like></Like>
    <Floor></Floor>
    <Brand></Brand>
  </div>
</template>

<script>
import ListContainer from "@/views/home/listContainer";
import Recommend from "@/views/home/recommend";
import Rank from "@/views/home/rank";
import Like from "@/views/common/like";
import Floor from "@/views/home/floor";
import Brand from "@/views/home/brand";
export default {
  name: 'home',
  components: {
    Brand,
    Floor,
    ListContainer,
    Recommend,
    Rank,
    Like
  }
}
</script>

<style>

</style>
