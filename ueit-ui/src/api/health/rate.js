import request from '@/utils/request'

// 查询心率数据列表
export function listRate(query) {
  return request({
    url: '/health/rate/list',
    method: 'get',
    params: query
  })
}

// 查询心率数据详细
export function getRate(id) {
  return request({
    url: '/health/rate/' + id,
    method: 'get'
  })
}

// 新增心率数据
export function addRate(data) {
  return request({
    url: '/health/rate',
    method: 'post',
    data: data
  })
}

// 修改心率数据
export function updateRate(data) {
  return request({
    url: '/health/rate',
    method: 'put',
    data: data
  })
}

// 删除心率数据
export function delRate(id) {
  return request({
    url: '/health/rate/' + id,
    method: 'delete'
  })
}
