import axios from 'axios'
import { useUserStore } from '@/stores/user'

/**
 * 统一返回体，与后端 com.blog.common.Result 对应（PRD 7.5）
 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器：自动携带 JWT
request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = userStore.token
  }
  return config
})

// 响应拦截器：解包统一返回体、统一错误处理
request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResult
    if (res.code !== 0) {
      // TODO M1: 接入全局 message 提示（n-message-provider 已挂载）
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data as never
  },
  (error) => {
    if (error.response?.status === 401) {
      // 登录过期：清除本地登录态并跳转登录页
      const userStore = useUserStore()
      userStore.clear()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default request
