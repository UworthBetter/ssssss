import { listDevices, listExceptions, listSubjects, type DeviceInfo, type ExceptionAlert, type HealthSubject } from '@/api/health'
import { getRecentAbnormal } from '@/api/ai'
import type { PlatformCenter, PlatformNotificationRecord } from '@/components/platform/platformTypes'

interface NotificationContext {
  subjectName?: string
  deviceName?: string
  eventType?: string
}

const formatTime = (value?: string) => (value ? String(value).slice(5, 16).replace('T', ' ') : '刚刚')

const buildFallbackNotifications = (center: PlatformCenter, context: NotificationContext = {}): PlatformNotificationRecord[] => {
  if (center === 'ai') {
    return [
      {
        id: 'ai-fallback-1',
        title: 'AI 研判待确认',
        description: '当前暂无实时通知，已保留 AI 研判入口。',
        time: '刚刚',
        level: 'warning',
        target: { path: '/event', query: { type: context.eventType || 'AI研判' } }
      }
    ]
  }

  if (center === 'event') {
    return [
      {
        id: 'event-fallback-1',
        title: '事件中心暂无新通知',
        description: '当前没有命中的待处理异常，稍后可再次刷新。',
        time: '刚刚',
        level: 'info',
        target: { path: '/event' }
      }
    ]
  }

  return [
    {
      id: `${center}-fallback-1`,
      title: '平台通知暂不可用',
      description: '当前中心尚未接入专属通知源，已保留默认入口。',
      time: '刚刚',
      level: 'info',
      target: { path: center === 'ai' ? '/ai/workbench' : center === 'event' ? '/event' : '/workbench/overview' }
    }
  ]
}

const mapExceptionToNotification = (row: ExceptionAlert): PlatformNotificationRecord => ({
  id: `event:${row.id || `${row.userId}-${row.deviceId}-${row.type}`}`,
  title: `${row.type || '异常事件'}待处理`,
  description: `对象 ${row.nickName || row.userId || '-'} / 设备 ${row.deviceId || '-'}，${row.location || '待确认位置'}`,
  time: formatTime(row.createTime),
  level: row.state === '1' ? 'info' : 'danger',
  target: {
    path: '/event',
    query: {
      type: row.type || '',
      userId: String(row.userId || ''),
      deviceId: String(row.deviceId || '')
    }
  }
})

const mapSubjectToNotification = (row: HealthSubject): PlatformNotificationRecord => ({
  id: `subject:${row.subjectId || row.subjectName}`,
  title: `对象画像可复核`,
  description: `${row.nickName || row.subjectName} 已进入当前分析范围，可继续查看对象 360。`,
  time: '刚刚',
  level: row.status === '1' ? 'warning' : 'info',
  target: {
    path: '/subject',
    query: {
      subjectName: row.subjectName || row.nickName || ''
    }
  }
})

const mapDeviceToNotification = (row: DeviceInfo): PlatformNotificationRecord => ({
  id: `device:${row.id || row.imei}`,
  title: `设备策略待确认`,
  description: `${row.name || '设备'} 已命中当前分析上下文，可继续查看设备运营状态。`,
  time: '刚刚',
  level: 'info',
  target: {
    path: '/device',
    query: {
      imei: row.imei || '',
      name: row.name || ''
    }
  }
})

const normalizeRecentAbnormal = (payload: unknown): ExceptionAlert[] => {
  if (Array.isArray(payload)) return payload as ExceptionAlert[]
  if (payload && typeof payload === 'object') {
    const data = (payload as { data?: unknown; rows?: unknown }).data ?? (payload as { rows?: unknown }).rows
    if (Array.isArray(data)) return data as ExceptionAlert[]
  }
  return []
}

export const loadPlatformNotifications = async (
  center: PlatformCenter,
  context: NotificationContext = {}
): Promise<PlatformNotificationRecord[]> => {
  try {
    if (center === 'event') {
      const exceptionRes = await listExceptions({
        pageNum: 1,
        pageSize: 3,
        type: context.eventType || '',
        state: '0'
      })
      const records = ((exceptionRes.rows || []) as ExceptionAlert[]).map(mapExceptionToNotification)
      return records.length ? records : buildFallbackNotifications(center, context)
    }

    if (center === 'ai') {
      const requests: Promise<unknown>[] = [
        listExceptions({ pageNum: 1, pageSize: 2, type: context.eventType || '', state: '0' }),
        getRecentAbnormal(2)
      ]

      if (context.subjectName) {
        requests.push(listSubjects({ pageNum: 1, pageSize: 1, subjectName: context.subjectName }))
      }
      if (context.deviceName) {
        requests.push(listDevices({ pageNum: 1, pageSize: 1, name: context.deviceName }))
      }

      const settled = await Promise.allSettled(requests)
      const records: PlatformNotificationRecord[] = []

      const pendingEvents = settled[0]?.status === 'fulfilled'
        ? (((settled[0].value as { rows?: ExceptionAlert[] }).rows || []) as ExceptionAlert[])
        : []
      records.push(...pendingEvents.map(mapExceptionToNotification))

      const recentAbnormal = settled[1]?.status === 'fulfilled'
        ? normalizeRecentAbnormal(settled[1].value)
        : []
      records.push(...recentAbnormal.slice(0, 1).map((item) => ({
        id: `ai-abnormal:${item.id || `${item.userId}-${item.deviceId}-${item.type}`}`,
        title: 'AI 最近异常已更新',
        description: `${item.type || '异常事件'}进入 AI 工作台，可继续生成处置建议。`,
        time: formatTime(item.createTime),
        level: 'warning',
        target: {
          path: '/event',
          query: {
            type: item.type || '',
            userId: String(item.userId || ''),
            deviceId: String(item.deviceId || '')
          }
        }
      })))

      const subjectResult = settled.find((item, index) => index >= 2 && item.status === 'fulfilled' && Array.isArray((item.value as { rows?: unknown[] }).rows) && ((item.value as { rows?: unknown[] }).rows || []).some((row) => 'subjectName' in (row as object)))
      const deviceResult = settled.find((item, index) => index >= 2 && item.status === 'fulfilled' && Array.isArray((item.value as { rows?: unknown[] }).rows) && ((item.value as { rows?: unknown[] }).rows || []).some((row) => 'imei' in (row as object)))

      if (subjectResult && subjectResult.status === 'fulfilled') {
        const subjectRow = ((subjectResult.value as { rows?: HealthSubject[] }).rows || [])[0]
        if (subjectRow) records.push(mapSubjectToNotification(subjectRow))
      }

      if (deviceResult && deviceResult.status === 'fulfilled') {
        const deviceRow = ((deviceResult.value as { rows?: DeviceInfo[] }).rows || [])[0]
        if (deviceRow) records.push(mapDeviceToNotification(deviceRow))
      }

      const deduped = Array.from(new Map(records.map((item) => [item.id, item])).values()).slice(0, 3)
      return deduped.length ? deduped : buildFallbackNotifications(center, context)
    }

    return buildFallbackNotifications(center, context)
  } catch {
    return buildFallbackNotifications(center, context)
  }
}
