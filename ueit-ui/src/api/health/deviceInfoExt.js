import request from '@/utils/request'

// 查询设备信息扩展列表
export function listDeviceInfoExt(query) {
  return request({
    url: '/health/deviceInfoExt/list',
    method: 'get',
    params: query
  })
}

// 查询设备信息扩展详细
export function getDeviceInfoExt(deviceId) {
  return request({
    url: '/health/deviceInfoExt/' + deviceId,
    method: 'get'
  })
}

// 新增设备信息扩展
export function addDeviceInfoExt(data) {
  return request({
    url: '/health/deviceInfoExt',
    method: 'post',
    data: data
  })
}

// 修改设备信息扩展
export function updateDeviceInfoExt(data) {
  return request({
    url: '/health/deviceInfoExt',
    method: 'put',
    data: data
  })
}

// 删除设备信息扩展
export function delDeviceInfoExt(deviceId) {
  return request({
    url: '/health/deviceInfoExt/' + deviceId,
    method: 'delete'
  })
}
