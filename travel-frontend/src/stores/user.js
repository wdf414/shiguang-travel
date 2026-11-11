import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils'
import * as userApi from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(storage.get('token') || '')
  const userInfo = ref(storage.get('userInfo') || null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(loginForm) {
    const data = await userApi.login(loginForm)
    token.value = data.token
    userInfo.value = data.user
    storage.set('token', data.token)
    storage.set('userInfo', data.user)
    return data
  }

  async function register(registerForm) {
    return await userApi.register(registerForm)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    storage.remove('token')
    storage.remove('userInfo')
  }

  async function fetchUserInfo() {
    const data = await userApi.getUserInfo()
    userInfo.value = data
    storage.set('userInfo', data)
  }

  async function updateUserInfo(info) {
    await userApi.updateUserInfo(info)
    userInfo.value = { ...userInfo.value, ...info }
    storage.set('userInfo', userInfo.value)
  }

  return { token, userInfo, isLoggedIn, login, register, logout, fetchUserInfo, updateUserInfo }
})
