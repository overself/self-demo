<template>
  <div class="preview">
    <div class="jqzoom">
      <img src="../images/s1.png"/>
    </div>
  </div>
</template>

<script>
export default {
  name: "Zoom",
  data: function (){
    return {
      currentIndex:0
    }
  }
}
</script>

<style scoped lang="less">
.preview {
  width: 400px;
  height: 400px;
  border: 1px solid #DFDFDF;

  .jqzoom {
    cursor: pointer;
    width: 400px;
    height: 400px;
    position: relative;

    img {
      width: 100%;
    }

    .mask {
      width: 200px;
      height: 200px;
      background: rgba(255, 255, 255, .5);
      position: absolute;
      left: 0;
      top: 0;
      border: 1px solid #ddd;
    }
  }

  .maxbox {
    width: 400px;
    height: 400px;
    position: absolute;
    left: 420px;
    top: 0px;
    overflow: hidden;
    z-index: 20;
    border: 1px solid #ddd;

    img {
      width: 800px;
      height: 800px;
      display: block;
    }
  }
}

</style>