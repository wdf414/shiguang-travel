<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Microsoft YaHei', sans-serif;
}

.date-picker-popper,
.el-popper {
  z-index: 99999 !important;
}

.el-dialog {
  overflow: visible !important;
}
</style>
