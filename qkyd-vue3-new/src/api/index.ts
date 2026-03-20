import request from '@/utils/request'

export function getRealTimeData() {
  return request({
    url: '/index/realTimeData',
    method: 'get'
  })
}

export function getAgeSexGroupCount() {
  return request({
    url: '/index/ageSexGroupCount',
    method: 'get'
  })
}

export function getIndexException(type: string, pageNum = 1) {
  return request({
    url: '/index/exception',
    method: 'get',
    params: {
      type,
      pageNum
    }
  })
}
