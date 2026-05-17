import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: !!localStorage.getItem('token')
  }),

  getters: {
    userId: (state) => state.user?.id || state.user?.userId
  },

  actions: {
    setToken(token) {
      this.token = token
      this.isAuthenticated = !!token
      localStorage.setItem('token', token)
    },

    setUser(user) {
      this.user = user
    },

    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
    }
  }
})
