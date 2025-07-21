import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi, type LoginRequest, type UserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = ref<boolean>(!!token.value)

  /**
   * 登录
   */
  const login = async (loginData: LoginRequest): Promise<boolean> => {
    try {
      const result = await userApi.login(loginData)
      
      // 保存token
      token.value = result
      localStorage.setItem('token', result)
      isLoggedIn.value = true
      
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }

  /**
   * 退出登录
   */
  const logout = () => {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
    ElMessage.success('已退出登录')
  }

  /**
   * 获取用户信息
   */
  const fetchUserInfo = async (userId: number): Promise<void> => {
    try {
      const result = await userApi.getUserInfo(userId)
      userInfo.value = result
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  /**
   * 清除用户信息
   */
  const clearUserInfo = () => {
    userInfo.value = null
  }

  return {
    // 状态
    token,
    userInfo,
    isLoggedIn,
    
    // 方法
    login,
    logout,
    fetchUserInfo,
    clearUserInfo
  }
})