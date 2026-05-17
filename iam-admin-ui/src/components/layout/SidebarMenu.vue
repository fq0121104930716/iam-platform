<template>
  <el-menu
    :default-active="activeMenu"
    :router="true"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
    class="sidebar-menu"
  >
    <el-menu-item index="/dashboard">
      <el-icon><Odometer /></el-icon>
      <span>仪表盘</span>
    </el-menu-item>

    <el-menu-item index="/tenants">
      <el-icon><OfficeBuilding /></el-icon>
      <span>租户管理</span>
    </el-menu-item>

    <el-menu-item index="/users">
      <el-icon><User /></el-icon>
      <span>用户管理</span>
    </el-menu-item>

    <el-menu-item index="/applications">
      <el-icon><Connection /></el-icon>
      <span>应用管理</span>
    </el-menu-item>

    <el-menu-item index="/menus">
      <el-icon><Menu /></el-icon>
      <span>菜单管理</span>
    </el-menu-item>

    <el-menu-item index="/roles">
      <el-icon><Avatar /></el-icon>
      <span>角色管理</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-menu {
  border-right: none;
}
</style>
