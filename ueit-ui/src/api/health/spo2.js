import request from '@/utils/request'

// 查询血氧数据列表
export function listSpo2(query) {
  return request({
    url: '/health/spo2/list',
    method: 'get',
    params: query
  })
}

// 查询血氧数据详细
export function getSpo2(id) {
  return request({
    url: '/health/spo2/' + id,
    method: 'get'
  })
}

// 新增血氧数据
export function addSpo2(data) {
  return request({
    url: '/health/spo2',
    method: 'post',
    data: data
  })
}

// 修改血氧数据
export function updateSpo2(data) {
  return request({
    url: '/health/spo2',
    method: 'put',
    data: data
  })
}

// 删除血氧数据
export function delSpo2(id) {
  return request({
    url: '/health/spo2/' + id,
    method: 'delete'
  })
}
