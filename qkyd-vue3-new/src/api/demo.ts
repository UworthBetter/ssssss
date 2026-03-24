import request from '@/utils/request'

export interface DeviceSimulatorStatus {
  configuredEnabled: boolean
  effectiveEnabled: boolean
  runtimeEnabledOverride: boolean | null
  configuredSubjectCount: number
  runtimeSubjectCountOverride: number | null
  effectiveSubjectCount: number
  maxSupportedSubjectCount: number
  running: boolean
  datasetInitialized: boolean
  legacyDataSanitized: boolean
  profileCacheSize: number
  tickCounter: number
  lastGeneratedBucket: number | null
}

export function getDeviceSimulatorStatus() {
  return request({
    url: '/demo/device-simulator/status',
    method: 'get'
  })
}

export function startDeviceSimulator(subjectCount?: number) {
  return request({
    url: '/demo/device-simulator/start',
    method: 'post',
    params: subjectCount ? { subjectCount } : {}
  })
}

export function stopDeviceSimulator() {
  return request({
    url: '/demo/device-simulator/stop',
    method: 'post'
  })
}

export function resetDeviceSimulator() {
  return request({
    url: '/demo/device-simulator/reset',
    method: 'post'
  })
}

export function updateDeviceSimulatorSubjectCount(subjectCount: number) {
  return request({
    url: '/demo/device-simulator/subject-count',
    method: 'put',
    params: { subjectCount }
  })
}

export function triggerDeviceSimulatorTick(subjectCount?: number) {
  return request({
    url: '/demo/device-simulator/tick',
    method: 'post',
    params: subjectCount ? { subjectCount } : {}
  })
}
