import request from './request'

// 认证相关接口（M1 实现后端后启用）

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  email: string
  password: string
}

/** 登录返回：token + 用户信息（与后端 UserVO 对齐） */
export interface LoginResult {
  token: string
  userInfo: {
    id: number
    username: string
    nickname: string
    email: string
    avatar?: string
    role: 'ADMIN' | 'USER'
    status?: number
    createTime?: string
  }
}

export function login(data: LoginParams) {
  return request.post<never, LoginResult>('/auth/login', data)
}

export function register(data: RegisterParams) {
  return request.post('/auth/register', data)
}

export function getMe() {
  return request.get('/auth/me')
}

export function logout() {
  return request.post('/auth/logout')
}
