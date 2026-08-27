import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as authApi from '@/api/auth'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: 'ADMIN' | 'USER'
}

/**
 * 用户态：token 与用户信息持久化到 localStorage，刷新不丢
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem('userInfo') || 'null'),
  )

  function setToken(value: string) {
    token.value = value
    localStorage.setItem('token', value)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  async function login(params: authApi.LoginParams) {
    // TODO M1: 调用后端登录接口，成功后写入 token 与用户信息
    const data = (await authApi.login(params)) as unknown as authApi.LoginResult
    setToken(data.token)
    setUserInfo(data.userInfo)
  }

  function clear() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, setToken, setUserInfo, login, clear }
})
