import { defineStore } from 'pinia'
import { loginApi, registerApi, profileApi, logoutApi } from '@/api/auth'

const TOKEN_KEY = 'campus-forum-token'
const USER_KEY = 'campus-forum-user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.roles?.includes('administrator') || false
  },
  actions: {
    bootstrap() {
      this.token = localStorage.getItem(TOKEN_KEY) || ''
      const userJson = localStorage.getItem(USER_KEY)
      this.user = userJson ? JSON.parse(userJson) : null
    },
    persist() {
      localStorage.setItem(TOKEN_KEY, this.token || '')
      if (this.user) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    async login(payload) {
      const res = await loginApi(payload)
      this.token = res.token
      this.user = res.user
      this.persist()
    },
    async register(payload) {
      const res = await registerApi(payload)
      if (res && res.token) {
        this.token = res.token
        this.user = res.user
        this.persist()
      } else {
        // 邮箱验证模式下不发放 token，先清空登录态
        this.clear()
      }
      return res
    },
    async fetchProfile() {
      if (!this.token) return
      try {
        this.user = await profileApi()
        this.persist()
      } catch (error) {
        this.clear()
      }
    },
    async logout() {
      if (this.token) {
        try {
          await logoutApi()
        } catch (error) {
          // 忽略服务端退出失败，保持前端状态可清理
        }
      }
      this.clear()
    },
    clear() {
      this.token = ''
      this.user = null
      this.persist()
    }
  }
})
