import {
  listDevices,
  listExceptions,
  listExceptionsByUserId,
  listSubjects,
  type DeviceInfo,
  type ExceptionAlert,
  type HealthSubject
} from '@/api/health'
import request from '@/utils/request'
import type { PlatformCenter, PlatformEntityKind } from '@/components/platform/platformTypes'

export interface PlatformSearchResult {
  id: string
  kind: PlatformEntityKind
  title: string
  subtitle: string
  description: string
  score: number
  target: {
    path: string
    query: Record<string, string>
  }
}

const SEARCH_PAGE_SIZE = 8
const PLATFORM_SEARCH_LIMIT = 10

const resolveRequestErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }

  if (typeof error === 'object' && error !== null) {
    const response = (error as { response?: { data?: { msg?: string } } }).response
    return String(response?.data?.msg || '')
  }

  return ''
}

const containsKeyword = (value: unknown, keyword: string) =>
  String(value || '').toLowerCase().includes(keyword.toLowerCase())

const exactKeyword = (value: unknown, keyword: string) =>
  String(value || '').trim().toLowerCase() === keyword.trim().toLowerCase()

const withCenterBoost = (kind: PlatformEntityKind, preferredCenter: PlatformCenter) => {
  if (preferredCenter === 'ai' || preferredCenter === 'workbench') return 0
  return preferredCenter === kind ? 8 : 0
}

const dedupeById = <T extends { id: string }>(items: T[]) => {
  const map = new Map<string, T>()
  items.forEach((item) => {
    if (!map.has(item.id) || map.get(item.id)!.score < item.score) {
      map.set(item.id, item)
    }
  })
  return Array.from(map.values())
}

const mapRemoteSearchResult = (item: Record<string, unknown>): PlatformSearchResult | null => {
  const kind = String(item.kind || '') as PlatformEntityKind
  if (!['subject', 'device', 'event'].includes(kind)) {
    return null
  }

  const target = item.target as { path?: string; query?: Record<string, string> } | undefined
  return {
    id: String(item.id || `${kind}:${item.title || item.subtitle || 'unknown'}`),
    kind,
    title: String(item.title || ''),
    subtitle: String(item.subtitle || ''),
    description: String(item.description || ''),
    score: Number(item.score || 0),
    target: {
      path: target?.path || `/${kind}`,
      query: target?.query || {}
    }
  }
}

const buildSubjectResult = (row: HealthSubject, keyword: string, preferredCenter: PlatformCenter): PlatformSearchResult | null => {
  const subjectId = Number(row.subjectId || 0)
  const fields = [row.subjectName, row.nickName, row.phonenumber, subjectId]
  if (!fields.some((field) => containsKeyword(field, keyword))) {
    return null
  }

  const exactField = [row.subjectName, row.nickName, row.phonenumber].some((field) => exactKeyword(field, keyword))
  const query =
    exactKeyword(row.phonenumber, keyword) && row.phonenumber
      ? { phonenumber: row.phonenumber }
      : { subjectName: row.subjectName || row.nickName || keyword }

  return {
    id: `subject:${subjectId || row.subjectName}`,
    kind: 'subject',
    title: row.nickName || row.subjectName || `对象 #${subjectId || '-'}`,
    subtitle: `对象中心 · ${row.subjectName || `对象 #${subjectId || '-'}`}`,
    description: row.phonenumber ? `手机号 ${row.phonenumber}` : '服务对象档案',
    score: (exactField ? 100 : 70) + withCenterBoost('subject', preferredCenter),
    target: {
      path: '/subject',
      query
    }
  }
}

const buildDeviceResult = (row: DeviceInfo, keyword: string, preferredCenter: PlatformCenter): PlatformSearchResult | null => {
  const deviceId = Number(row.id || 0)
  const fields = [row.name, row.imei, row.userId, row.type, deviceId]
  if (!fields.some((field) => containsKeyword(field, keyword))) {
    return null
  }

  const exactField = [row.name, row.imei].some((field) => exactKeyword(field, keyword))
  const query =
    exactKeyword(row.imei, keyword) && row.imei
      ? { imei: row.imei }
      : row.userId && exactKeyword(row.userId, keyword)
        ? { userId: String(row.userId) }
        : { name: row.name || keyword }

  return {
    id: `device:${deviceId || row.imei}`,
    kind: 'device',
    title: row.name || `设备 #${deviceId || '-'}`,
    subtitle: `设备中心 · IMEI ${row.imei || '-'}`,
    description: row.userId ? `绑定对象 #${row.userId}` : row.type || '设备档案',
    score: (exactField ? 100 : 68) + withCenterBoost('device', preferredCenter),
    target: {
      path: '/device',
      query
    }
  }
}

const buildEventResult = (row: ExceptionAlert, keyword: string, preferredCenter: PlatformCenter): PlatformSearchResult | null => {
  const eventId = Number(row.id || 0)
  const fields = [row.type, row.value, row.location, row.userId, row.deviceId, eventId]
  if (!fields.some((field) => containsKeyword(field, keyword))) {
    return null
  }

  const exactField = [row.type, row.userId, row.deviceId].some((field) => exactKeyword(field, keyword))
  const query =
    row.userId && exactKeyword(row.userId, keyword)
      ? { userId: String(row.userId), type: row.type || '' }
      : { type: row.type || keyword }

  return {
    id: `event:${eventId || `${row.userId}-${row.deviceId}-${row.type}`}`,
    kind: 'event',
    title: row.type || `事件 #${eventId || '-'}`,
    subtitle: `事件中心 · 用户 ${row.userId || '-'} / 设备 ${row.deviceId || '-'}`,
    description: row.location || row.value || '异常事件',
    score: (exactField ? 98 : 66) + withCenterBoost('event', preferredCenter),
    target: {
      path: '/event',
      query
    }
  }
}

const searchViaPlatformEndpoint = async (keyword: string, preferredCenter: PlatformCenter) => {
  const response = await request({
    url: '/platform/search',
    method: 'get',
    headers: {
      'X-Skip-Error-Message': 'true'
    },
    params: {
      keyword,
      center: preferredCenter,
      limit: PLATFORM_SEARCH_LIMIT
    }
  })

  const payload = response?.data || {}
  const rawResults = Array.isArray(payload.results) ? payload.results : []

  return {
    keyword: String(payload.keyword || keyword),
    results: rawResults
      .map((item) => mapRemoteSearchResult(item as Record<string, unknown>))
      .filter((item): item is PlatformSearchResult => Boolean(item))
      .sort((left, right) => right.score - left.score)
  }
}

const searchViaAggregatedLists = async (
  keyword: string,
  preferredCenter: PlatformCenter = 'workbench'
) => {
  const normalizedKeyword = keyword.trim()
  const numericKeyword = /^\d+$/.test(normalizedKeyword)

  const subjectRequests = [
    listSubjects({ pageNum: 1, pageSize: SEARCH_PAGE_SIZE, subjectName: normalizedKeyword })
  ]
  if (numericKeyword) {
    subjectRequests.push(
      listSubjects({ pageNum: 1, pageSize: SEARCH_PAGE_SIZE, phonenumber: normalizedKeyword })
    )
  }

  const deviceRequests = [
    listDevices({ pageNum: 1, pageSize: SEARCH_PAGE_SIZE, name: normalizedKeyword })
  ]
  if (numericKeyword) {
    deviceRequests.push(
      listDevices({ pageNum: 1, pageSize: SEARCH_PAGE_SIZE, imei: normalizedKeyword }),
      listDevices({ pageNum: 1, pageSize: SEARCH_PAGE_SIZE, userId: normalizedKeyword })
    )
  }

  const eventRequests = [
    listExceptions({ pageNum: 1, pageSize: SEARCH_PAGE_SIZE, type: normalizedKeyword, state: '' })
  ]
  if (numericKeyword) {
    eventRequests.push(
      listExceptionsByUserId(Number(normalizedKeyword), {
        pageNum: 1,
        pageSize: SEARCH_PAGE_SIZE,
        type: '',
        state: ''
      })
    )
  }

  const [subjectSettled, deviceSettled, eventSettled] = await Promise.all([
    Promise.allSettled(subjectRequests),
    Promise.allSettled(deviceRequests),
    Promise.allSettled(eventRequests)
  ])

  const subjectRows = subjectSettled
    .filter((item): item is PromiseFulfilledResult<{ rows?: HealthSubject[] }> => item.status === 'fulfilled')
    .flatMap((item) => item.value.rows || [])
  const deviceRows = deviceSettled
    .filter((item): item is PromiseFulfilledResult<{ rows?: DeviceInfo[] }> => item.status === 'fulfilled')
    .flatMap((item) => item.value.rows || [])
  const eventRows = eventSettled
    .filter((item): item is PromiseFulfilledResult<{ rows?: ExceptionAlert[] }> => item.status === 'fulfilled')
    .flatMap((item) => item.value.rows || [])

  const results = dedupeById(
    [
      ...subjectRows.map((row) => buildSubjectResult(row, normalizedKeyword, preferredCenter)),
      ...deviceRows.map((row) => buildDeviceResult(row, normalizedKeyword, preferredCenter)),
      ...eventRows.map((row) => buildEventResult(row, normalizedKeyword, preferredCenter))
    ].filter((item): item is PlatformSearchResult => Boolean(item))
  ).sort((left, right) => right.score - left.score)

  return {
    keyword: normalizedKeyword,
    results
  }
}

export const searchPlatformEntities = async (
  keyword: string,
  preferredCenter: PlatformCenter = 'workbench'
) => {
  try {
    return await searchViaPlatformEndpoint(keyword.trim(), preferredCenter)
  } catch (error) {
    const message = resolveRequestErrorMessage(error)
    if (message.includes('登录状态已失效')) {
      throw error
    }

    return searchViaAggregatedLists(keyword, preferredCenter)
  }
}
