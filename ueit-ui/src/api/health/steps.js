import request from '@/utils/request'

// 查询步数列表
export function listSteps(query) {
  return request({
    url: '/health/steps/list',
    method: 'get',
    params: query
  })
}

// 查询步数详细
export function getSteps(id) {
  return request({
    url: '/health/steps/' + id,
    method: 'get'
  })
}

// 新增步数
export function addSteps(data) {
  return request({
    url: '/health/steps',
    method: 'post',
    data: data
  })
}

// 修改步数
export function updateSteps(data) {
  return request({
    url: '/health/steps',
    method: 'put',
    data: data
  })
}

// 删除步数
export function delSteps(id) {
  return request({
    url: '/health/steps/' + id,
    method: 'delete'
  })
}
