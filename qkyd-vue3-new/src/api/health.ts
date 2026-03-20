import request from '@/utils/request'

export interface ListQuery {
  pageNum: number
  pageSize: number
  [key: string]: unknown
}

export interface HealthSubject {
  subjectId?: number
  deptId?: number
  subjectName: string
  nickName: string
  phonenumber?: string
  email?: string
  age?: number
  sex?: string
  status?: string
  remark?: string
}

export interface DeviceInfo {
  id?: number
  userId?: number
  name: string
  imei: string
  type?: string
}

export interface ExceptionAlert {
  id?: number
  userId?: number
  deviceId?: number
  type?: string
  value?: string
  state?: string
  location?: string
  updateContent?: string
  createTime?: string
}

export function listSubjects(params: ListQuery) {
  return request({
    url: '/health/subject/list',
    method: 'get',
    params
  })
}

export function getSubject(subjectId: number) {
  return request({
    url: `/health/subject/${subjectId}`,
    method: 'get'
  })
}

export function createSubject(data: HealthSubject) {
  return request({
    url: '/health/subject',
    method: 'post',
    data
  })
}

export function updateSubject(data: HealthSubject) {
  return request({
    url: '/health/subject',
    method: 'put',
    data
  })
}

export function deleteSubject(subjectIds: number[]) {
  return request({
    url: `/health/subject/${subjectIds.join(',')}`,
    method: 'delete'
  })
}

export function listDevices(params: ListQuery) {
  return request({
    url: '/health/deviceInfo/list',
    method: 'get',
    params
  })
}

export function getDevice(id: number) {
  return request({
    url: `/health/deviceInfo/${id}`,
    method: 'get'
  })
}

export function createDevice(data: DeviceInfo) {
  return request({
    url: '/health/deviceInfo',
    method: 'post',
    data
  })
}

export function updateDevice(data: DeviceInfo) {
  return request({
    url: '/health/deviceInfo',
    method: 'put',
    data
  })
}

export function deleteDevice(ids: number[]) {
  return request({
    url: `/health/deviceInfo/${ids.join(',')}`,
    method: 'delete'
  })
}

export function listExceptions(params: ListQuery) {
  return request({
    url: '/health/exception/list',
    method: 'get',
    params
  })
}

export function listExceptionsByUserId(userId: number, params: ListQuery) {
  return request({
    url: '/health/exception/listByUserId',
    method: 'get',
    params: {
      userId,
      ...params
    }
  })
}

export function getException(id: number) {
  return request({
    url: `/health/exception/${id}`,
    method: 'get'
  })
}

export function updateException(data: ExceptionAlert) {
  return request({
    url: '/health/exception',
    method: 'put',
    data
  })
}
