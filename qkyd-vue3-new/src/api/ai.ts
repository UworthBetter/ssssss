import request from '@/utils/request'

export function chatAi(message: string) {
  return request({
    url: '/ai/chat',
    method: 'post',
    timeout: 60000,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    // Send JSON string payload to avoid axios defaulting plain strings to form-urlencoded.
    data: JSON.stringify(message)
  })
}

export function detectFall(data: Record<string, unknown>) {
  return request({
    url: '/ai/fall/detect',
    method: 'post',
    data
  })
}

export function assessRisk(data: Record<string, unknown>) {
  return request({
    url: '/ai/risk/assess',
    method: 'post',
    data
  })
}

export function analyzeTrend(data: Record<string, unknown>) {
  return request({
    url: '/ai/trend/analyze',
    method: 'post',
    data
  })
}

export function checkQuality(data: Record<string, unknown>) {
  return request({
    url: '/ai/quality/check',
    method: 'post',
    data
  })
}

export function detectAbnormal(data: Record<string, unknown>) {
  return request({
    url: '/ai/abnormal/detect',
    method: 'post',
    data
  })
}

export function getRecentAbnormal(limit = 10) {
  return request({
    url: '/ai/abnormal/recent',
    method: 'get',
    params: {
      limit
    }
  })
}
