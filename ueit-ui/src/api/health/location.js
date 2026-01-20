import request from '@/utils/request'

// 查询定位数据列表
export function listLocation(query) {
  return request({
    url: '/health/location/list',
    method: 'get',
    params: query
  })
}

// 查询定位数据详细
export function getLocation(id) {
  return request({
    url: '/health/location/' + id,
    method: 'get'
  })
}

// 新增定位数据
export function addLocation(data) {
  return request({
    url: '/health/location',
    method: 'post',
    data: data
  })
}

// 修改定位数据
export function updateLocation(data) {
  return request({
    url: '/health/location',
    method: 'put',
    data: data
  })
}

// 删除定位数据
export function delLocation(id) {
  return request({
    url: '/health/location/' + id,
    method: 'delete'
  })
}
