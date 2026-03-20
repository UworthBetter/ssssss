import request from '@/utils/request'

export interface LoginPayload {
  username: string
  password: string
  code: string
  uuid: string
}

export function login(data: LoginPayload) {
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

export function getRouters() {
  return request({
    url: '/getRouters',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

export function getCaptcha() {
  return request({
    url: '/captchaImage',
    method: 'get'
  })
}
