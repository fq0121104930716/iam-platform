<template>
  <div class="tenant-switcher">
    <span class="label">当前租户:</span>
    <el-select
      v-model="selectedTenantId"
      @change="handleTenantChange"
      placeholder="选择租户"
      style="width: 200px"
      filterable
    >
      <el-option
        v-for="tenant in tenantStore.tenants"
        :key="tenant.tenantId"
        :label="tenant.tenantName"
        :value="tenant.tenantId"
      />
    </el-select>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useTenantStore } from '@/stores/tenant'
import { useAuthStore } from '@/stores/auth'

const tenantStore = useTenantStore()
const authStore = useAuthStore()

const selectedTenantId = ref(tenantStore.currentTenantId)

onMounted(async () => {
  if (authStore.userId) {
    await tenantStore.loadUserTenants(authStore.userId)
    selectedTenantId.value = tenantStore.currentTenantId
  }
})

const handleTenantChange = async (tenantId) => {
  try {
    await tenantStore.switchTenant(authStore.userId, tenantId)
  } catch (error) {
    ElMessage.error('切换租户失败')
  }
}
</script>

<style scoped>
.tenant-switcher {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  color: #606266;
  font-size: 14px;
}
</style>
