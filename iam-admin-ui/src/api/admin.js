import api from './index'

export const adminApi = {
  // Dashboard
  getDashboard(userId, currentTenantId) {
    return api.get('/admin/dashboard', {
      params: { userId, currentTenantId }
    })
  },

  // Tenant switch
  switchTenant(userId, tenantId) {
    return api.post('/admin/tenants/switch', { userId, tenantId })
  },

  // User tenants
  getUserTenants(userId) {
    return api.get(`/admin/users/${userId}/tenants`)
  },

  // Users
  getUsers(params) {
    return api.get('/v1/users', { params })
  },
  getUser(userId) {
    return api.get(`/v1/users/${userId}`)
  },
  createUser(data) {
    return api.post('/v1/users', data)
  },
  updateUser(userId, data) {
    return api.put(`/v1/users/${userId}`, data)
  },
  deleteUser(userId) {
    return api.delete(`/v1/users/${userId}`)
  },

  // Tenants
  getTenants(params) {
    return api.get('/v1/tenants', { params })
  },
  createTenant(data) {
    return api.post('/v1/tenants', data)
  },
  updateTenant(tenantId, data) {
    return api.put(`/v1/tenants/${tenantId}`, data)
  },

  // User-Tenant mappings
  getUserTenantMappings(userId) {
    return api.get(`/v1/user-tenants/user/${userId}`)
  },
  createUserTenantMapping(userId, tenantId, data) {
    return api.post(`/v1/user-tenants/${userId}/tenants/${tenantId}`, data)
  },
  suspendUserTenantMapping(mappingId) {
    return api.put(`/v1/user-tenants/${mappingId}/suspend`)
  },
  reactivateUserTenantMapping(mappingId) {
    return api.put(`/v1/user-tenants/${mappingId}/reactivate`)
  },

  // Roles
  getRoles(tenantId) {
    return api.get('/v1/roles', { params: { tenantId } })
  },
  createRole(data) {
    return api.post('/v1/roles', data)
  },
  updateRole(roleId, data) {
    return api.put(`/v1/roles/${roleId}`, data)
  },
  deleteRole(roleId) {
    return api.delete(`/v1/roles/${roleId}`)
  },
  assignRoleToUser(userId, tenantId, roleId) {
    return api.post(`/v1/user-tenants/${userId}/roles`, { tenantId, roleId })
  },
  revokeRoleFromUser(mappingId) {
    return api.delete(`/v1/user-tenants/roles/${mappingId}`)
  },

  // Platform menus
  getPlatformMenus() {
    return api.get('/v1/menus/platform')
  },
  createMenu(data) {
    return api.post('/v1/menus/platform', data)
  },
  updateMenu(menuId, data) {
    return api.put(`/v1/menus/platform/${menuId}`, data)
  },
  deleteMenu(menuId) {
    return api.delete(`/v1/menus/platform/${menuId}`)
  },
  configureTenantMenu(tenantId, menuId, enabled) {
    return api.post(`/v1/menus/tenant/${tenantId}/menu/${menuId}`, { enabled })
  },
  getTenantMenus(tenantId) {
    return api.get(`/v1/menus/tenant/${tenantId}`)
  },

  // Applications
  getApplications(params) {
    return api.get('/v1/applications', { params })
  },
  getApplication(appId) {
    return api.get(`/v1/applications/${appId}`)
  },
  createApplication(data) {
    return api.post('/v1/applications', data)
  },
  updateApplication(appId, data) {
    return api.put(`/v1/applications/${appId}`, data)
  },
  deleteApplication(appId) {
    return api.delete(`/v1/applications/${appId}`)
  },

  // Application resources
  createAppResource(appId, data) {
    return api.post(`/v1/applications/${appId}/resources`, data)
  },
  updateAppResource(resourceId, data) {
    return api.put(`/v1/applications/resources/${resourceId}`, data)
  },
  deleteAppResource(resourceId) {
    return api.delete(`/v1/applications/resources/${resourceId}`)
  },

  // Application-tenant mapping
  assignAppToTenant(appId, tenantId) {
    return api.post(`/v1/applications/${appId}/tenants/${tenantId}`)
  },
  enableAppForTenant(appMappingId) {
    return api.put(`/v1/applications/tenants/${appMappingId}/enable`)
  },
  disableAppForTenant(appMappingId) {
    return api.put(`/v1/applications/tenants/${appMappingId}/disable`)
  },
  getTenantApplications(tenantId) {
    return api.get(`/v1/applications/tenant/${tenantId}`)
  }
}
