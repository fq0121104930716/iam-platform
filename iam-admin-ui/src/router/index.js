import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'tenants',
        name: 'Tenants',
        component: () => import('@/views/TenantList.vue'),
        meta: { title: '租户管理' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('@/views/ApplicationList.vue'),
        meta: { title: '应用管理' }
      },
      {
        path: 'menus',
        name: 'Menus',
        component: () => import('@/views/MenuManagement.vue'),
        meta: { title: '菜单管理' }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/RoleList.vue'),
        meta: { title: '角色管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard for authentication
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
