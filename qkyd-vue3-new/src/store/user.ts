import { defineStore } from 'pinia'
import { getCaptcha, getInfo, login, logout, type LoginPayload } from '@/api/user'

interface UserInfo {
  userId?: number
  userName?: string
  nickName?: string
  phonenumber?: string
  avatar?: string
  [key: string]: unknown
}

interface UserState {
  token: string
  userInfo: UserInfo
  captcha: string
  captchaEnabled: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    userInfo: {},
    captcha: '',
    captchaEnabled: true
  }),
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },
    clearToken() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
    },
    async handleLogin(loginData: LoginPayload) {
      const res = await login(loginData)
      this.setToken(res.token)
      return res
    },
    async fetchUserInfo() {
      const res = await getInfo()
      this.userInfo = (res.user || {}) as UserInfo
      return res
    },
    async handleLogout() {
      await logout()
      this.clearToken()
    },
    async fetchCaptcha() {
      const res = await getCaptcha()
      const captchaImg = res.img || ''
      this.captcha = captchaImg
        ? captchaImg.startsWith('data:image')
          ? captchaImg
          : `data:image/jpeg;base64,${captchaImg}`
        : ''
      this.captchaEnabled = res.captchaEnabled !== false
      return res
    }
  }
})
