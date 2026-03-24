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
  createTime?: string
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

export interface DeviceInfoExtend {
  id?: number
  userId?: number
  deviceId?: number
  nickName?: string
  lastCommunicationTime?: string
  batteryLevel?: number | string
  step?: number | string
  alarmContent?: string
  alarmTime?: string
  temp?: number | string
  tempTime?: string
  heartRate?: number | string
  heartRateTime?: string
  bloodDiastolic?: number | string
  bloodSystolic?: number | string
  bloodTime?: string
  spo2?: number | string
  spo2Time?: string
  longitude?: number | string
  latitude?: number | string
  location?: string
  type?: string
}

export interface VitalRecord {
  id?: number
  userId?: number
  deviceId?: number
  value?: number | string
  createTime?: string
  readTime?: string
}

export interface BloodRecord {
  id?: number
  userId?: number
  deviceId?: number
  systolic?: number | string
  diastolic?: number | string
  value?: string
  createTime?: string
  readTime?: string
}

export interface StepRecord {
  id?: number
  userId?: number
  deviceId?: number
  date?: string
  dateTime?: string
  value?: number | string
  calories?: number | string
  readTime?: string
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

export function listDeviceExtensions(params: ListQuery) {
  return request({
    url: '/health/deviceInfoExt/list',
    method: 'get',
    params
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

export function listHeartRates(params: ListQuery) {
  return request({
    url: '/health/heartRate/list',
    method: 'get',
    params
  })
}

export function listBloods(params: ListQuery) {
  return request({
    url: '/health/blood/list',
    method: 'get',
    params
  })
}

export function listSpo2s(params: ListQuery) {
  return request({
    url: '/health/spo2/list',
    method: 'get',
    params
  })
}

export function listTemps(params: ListQuery) {
  return request({
    url: '/health/temp/list',
    method: 'get',
    params
  })
}

export function listSteps(params: ListQuery) {
  return request({
    url: '/health/steps/list',
    method: 'get',
    params
  })
}
