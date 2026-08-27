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

export interface LoginResult {
  token: string
  userInfo: {
    id: number
    username: string
    nickname: string
    avatar?: string
    role: 'ADMIN' | 'USER'
  }
}

export function login(data: LoginParams) {
  return request.post('/auth/login', data)
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
