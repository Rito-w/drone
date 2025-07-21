import http from '@/utils/http'

/**
 * 用户登录请求参数
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  phone?: string
  email?: string
  userType: number
  status: number
}

/**
 * 用户API
 */
export const userApi = {
  /**
   * 用户登录
   */
  login(data: LoginRequest): Promise<string> {
    return http.post('/api/user/login', data)
  },

  /**
   * 获取用户信息
   */
  getUserInfo(id: number): Promise<UserInfo> {
    return http.get(`/api/user/${id}`)
  },

  /**
   * 健康检查
   */
  health(): Promise<string> {
    return http.get('/api/user/health')
  }
}