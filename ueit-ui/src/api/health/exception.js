import request from '@/utils/request'

// 查询异常数据列表
export function listException(query) {
  return request({
    url: '/health/exception/list',
    method: 'get',
    params: query
  })
}
// 根据UserId查询异常数据列表
export function listByUserId(data) {
  return request({
    url: '/health/exception/listByUserId',
    method: 'get',
    params: data
  })
}
// 查询异常数据详细
export function getException(id) {
  return request({
    url: '/health/exception/' + id,
    method: 'get'
  })
}
// 查询异常数据详细
export function getExceptionT(data) {
  return request({
    url: '/health/exception/T',
    method: 'get',
    params: data
  })
}
// 新增异常数据
export function addException(data) {
  return request({
    url: '/health/exception',
    method: 'post',
    data: data
  })
}

// 修改异常数据
export function updateException(data) {
  return request({
    url: '/health/exception',
    method: 'put',
    data: data
  })
}

// 删除异常数据
export function delException(id) {
  return request({
    url: '/health/exception/' + id,
    method: 'delete'
  })
}
