<template>
  <div class="admin-layout">
    <el-container style="height: 100%">
      <!-- Sidebar -->
      <el-aside width="240px" class="aside">
        <div class="logo">
          <h2>IAM Platform</h2>
        </div>
        <SidebarMenu />
      </el-aside>

      <el-container>
        <!-- Header -->
        <el-header class="header">
          <div class="header-left">
            <span class="breadcrumb">{{ currentRouteTitle }}</span>
          </div>
          <div class="header-right">
            <TenantSwitcher />
            <el-dropdown>
              <span class="user-info">
                <el-icon><User /></el-icon>
                {{ authStore.user?.username || 'User' }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- Main Content -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SidebarMenu from './SidebarMenu.vue'
import TenantSwitcher from './TenantSwitcher.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentRouteTitle = computed(() => route.meta.title || '')

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #304156;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.breadcrumb {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
