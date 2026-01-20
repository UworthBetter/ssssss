import request from '@/utils/request'

// 查询血压数据列表
export function listBlood(query) {
  return request({
    url: '/health/blood/list',
    method: 'get',
    params: query
  })
}

// 查询血压数据详细
export function getBlood(id) {
  return request({
    url: '/health/blood/' + id,
    method: 'get'
  })
}

// 新增血压数据
export function addBlood(data) {
  return request({
    url: '/health/blood',
    method: 'post',
    data: data
  })
}

// 修改血压数据
export function updateBlood(data) {
  return request({
    url: '/health/blood',
    method: 'put',
    data: data
  })
}

// 删除血压数据
export function delBlood(id) {
  return request({
    url: '/health/blood/' + id,
    method: 'delete'
  })
}
