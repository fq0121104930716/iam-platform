<template>
  <div class="dashboard">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.totalUsers || 0 }}</p>
              <p class="stat-label">总用户数</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon :size="32"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.totalTenants || 0 }}</p>
              <p class="stat-label">总租户数</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon :size="32"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.totalApplications || 0 }}</p>
              <p class="stat-label">总应用数</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon :size="32"><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.totalRoles || 0 }}</p>
              <p class="stat-label">总角色数</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户趋势</span>
            </div>
          </template>
          <div id="user-chart" style="height: 300px"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in recentActivities"
              :key="index"
              :timestamp="activity.timestamp"
              placement="top"
            >
              <p>{{ activity.description }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useTenantStore } from '@/stores/tenant'
import { useAuthStore } from '@/stores/auth'
import { adminApi } from '@/api/admin'

const tenantStore = useTenantStore()
const authStore = useAuthStore()

const statistics = ref({})
const recentActivities = ref([])

onMounted(async () => {
  if (authStore.userId) {
    try {
      const data = await adminApi.getDashboard(authStore.userId, tenantStore.currentTenantId)
      if (data.statistics) {
        statistics.value = data.statistics
      }
      recentActivities.value = [
        { timestamp: '2026-05-17 10:00', description: '新用户注册: admin@company.com' },
        { timestamp: '2026-05-17 09:30', description: '租户配置变更: tenant-a' },
        { timestamp: '2026-05-17 09:00', description: '应用授权: OA系统 -> tenant-b' },
        { timestamp: '2026-05-16 17:00', description: '角色权限更新: admin-role' }
      ]
    } catch (error) {
      console.error('Failed to load dashboard data:', error)
    }
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.stat-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin: 5px 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>
