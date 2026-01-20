import request from '@/utils/request'

// 查询心率数据列表
export function listTemp(query) {
  return request({
    url: '/health/temp/list',
    method: 'get',
    params: query
  })
}

// 查询心率数据详细
export function getTemp(id) {
  return request({
    url: '/health/temp/' + id,
    method: 'get'
  })
}

// 新增心率数据
export function addTemp(data) {
  return request({
    url: '/health/temp',
    method: 'post',
    data: data
  })
}

// 修改心率数据
export function updateTemp(data) {
  return request({
    url: '/health/temp',
    method: 'put',
    data: data
  })
}

// 删除心率数据
export function delTemp(id) {
  return request({
    url: '/health/temp/' + id,
    method: 'delete'
  })
}
