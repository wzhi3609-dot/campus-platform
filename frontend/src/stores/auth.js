// Pinia 认证状态管理模块：管理用户登录 Token、用户信息，提供登录/注册/登出操作及登录状态计算属性
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(credentials) {
    const res = await request.post('/auth/login', credentials)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }

  async function register(data) {
    const res = await request.post('/auth/register', data)
    return res
  }

  async function adminLogin(credentials) {
    const res = await request.post('/auth/admin-login', credentials)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, login, register, adminLogin, logout }
})
