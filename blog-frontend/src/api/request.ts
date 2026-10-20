import axios from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

/**
 * 统一返回体，与后端 com.littlecase.myblog.common.Result 对应（PRD 7.5）
 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动携带 JWT（后端 sa-token.token-name = Authorization）
request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = userStore.token
  }
  // FormData 交给浏览器自动带 boundary，避免手动设置 Content-Type 丢 boundary
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

function redirectToLogin() {
  const userStore = useUserStore()
  userStore.clear()
  const current = router.currentRoute.value
  if (current.path !== '/login') {
    router.push({ path: '/login', query: { redirect: current.fullPath } })
  }
}

// 响应拦截器：解包统一返回体、统一错误处理
request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResult
    if (res.code === 0) {
      return res.data as never
    }
    // 未登录 / 登录过期：清理登录态并跳转登录页
    if (res.code === 401) {
      redirectToLogin()
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      redirectToLogin()
    }
    return Promise.reject(error)
  },
)

export default request
