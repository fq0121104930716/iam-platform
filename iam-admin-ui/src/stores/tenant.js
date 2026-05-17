import { defineStore } from 'pinia'
import { adminApi } from '@/api/admin'

export const useTenantStore = defineStore('tenant', {
  state: () => ({
    currentTenantId: null,
    tenants: [],
    menus: [],
    applications: []
  }),

  getters: {
    currentTenant: (state) => state.tenants.find(t => t.tenantId === state.currentTenantId),
    isPlatformAdmin: (state) => state.currentTenantId === 0
  },

  actions: {
    async loadUserTenants(userId) {
      try {
        const response = await adminApi.getUserTenants(userId)
        this.tenants = response.tenants || []
        if (this.tenants.length > 0 && !this.currentTenantId) {
          this.currentTenantId = this.tenants[0].tenantId
        }
      } catch (error) {
        console.error('Failed to load user tenants:', error)
      }
    },

    async switchTenant(userId, tenantId) {
      try {
        const response = await adminApi.switchTenant(userId, tenantId)
        if (response.success) {
          this.currentTenantId = tenantId
          await this.loadTenantData(tenantId)
        }
        return response
      } catch (error) {
        console.error('Failed to switch tenant:', error)
        throw error
      }
    },

    async loadTenantData(tenantId) {
      if (!tenantId) return

      try {
        // Load menus
        const menusResponse = await adminApi.getTenantMenus(tenantId)
        this.menus = menusResponse || []

        // Load applications
        const appsResponse = await adminApi.getTenantApplications(tenantId)
        this.applications = appsResponse || []
      } catch (error) {
        console.error('Failed to load tenant data:', error)
      }
    },

    setCurrentTenant(tenantId) {
      this.currentTenantId = tenantId
      this.loadTenantData(tenantId)
    }
  }
})
