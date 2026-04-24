import { defineStore } from 'pinia'
import request from '../utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    menus: [],
    permissions: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.userInfo,
    menuTree: (state) => state.menus
  },

  actions: {
    async login(username, password) {
      const res = await request.post('/user/login', { username, password })
      if (res.code === 200) {
        this.userInfo = res.data
        this.menus = res.data.menus || []
        this.permissions = res.data.permissions || []
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        localStorage.setItem('menus', JSON.stringify(res.data.menus || []))
        localStorage.setItem('permissions', JSON.stringify(res.data.permissions || []))
      }
      return res
    },

    logout() {
      this.userInfo = null
      this.menus = []
      this.permissions = []
      localStorage.removeItem('userInfo')
      localStorage.removeItem('menus')
      localStorage.removeItem('permissions')
    },

    restoreState() {
      const userInfo = localStorage.getItem('userInfo')
      const menus = localStorage.getItem('menus')
      const permissions = localStorage.getItem('permissions')
      
      if (userInfo) {
        this.userInfo = JSON.parse(userInfo)
      }
      if (menus) {
        this.menus = JSON.parse(menus)
      }
      if (permissions) {
        this.permissions = JSON.parse(permissions)
      }
    },

    hasPermission(permission) {
      return this.permissions.includes(permission)
    }
  }
})
