import request from '@/utils/request'

// 查询围栏列表
export function listFence(query) {
  return request({
    url: '/health/fence/list',
    method: 'get',
    params: query
  })
}

// 查询围栏详细
export function getFence(id) {
  return request({
    url: '/health/fence/' + id,
    method: 'get'
  })
}

// 新增围栏
export function addFence(data) {
  return request({
    url: '/health/fence',
    method: 'post',
    data: data
  })
}

// 修改围栏
export function updateFence(data) {
  return request({
    url: '/health/fence',
    method: 'put',
    data: data
  })
}
// 修改围栏状态
export function updateFenceStatus(id, status) {
  const data = { id, status }
  return request({
    url: '/health/fence/updateFenceStatus',
    method: 'put',
    data: data
  })
}

// 删除围栏
export function delFence(id) {
  return request({
    url: '/health/fence/' + id,
    method: 'delete'
  })
}
