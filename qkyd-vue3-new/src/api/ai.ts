import request from '@/utils/request'

export interface EventInsightContext {
  age?: number | string
  chronicDiseases?: string[] | string
  recentTrend?: string
  recentHealthTrend?: string
  historyCount?: number | string
  historicalAbnormalCount?: number | string
  recentSameTypeCount?: number | string
  deviceState?: string
  deviceStatus?: string
  deviceStatusReason?: string
  currentDeviceState?: string
  lastKnownLocation?: string
  dataConfidence?: string
  healthTrend?: string
}

export interface EventInsightRisk {
  level?: string
  riskLevel?: string
  riskScore?: number | string
  immediateAction?: boolean
  possibleCauses?: string[] | string
  possibleReasons?: string[] | string
  analysisReasons?: string[] | string
  reasonCodes?: string[] | string
  ruleHits?: string[] | string
  confidence?: number | string
}

export interface EventInsightAdvice {
  notifyWho?: string[] | string
  actions?: string[] | string
  suggestedActions?: string[] | string
  offlineCheck?: boolean
  contactFamily?: boolean
  contactOrg?: boolean
  contactOrganization?: boolean
  immediateAction?: boolean
}

export interface EventInsightTraceStep {
  agentKey?: string
  agentName?: string
  status?: string
  resolvedCount?: number | string
  targetCount?: number | string
  summary?: string
  detail?: string
}

export interface EventInsightTrace {
  orchestratorVersion?: string
  fallbackUsed?: boolean
  fallbackReason?: string
  missingFields?: string[] | string
  steps?: EventInsightTraceStep[] | unknown
}

export interface EventInsightFreshness {
  state?: string
  tone?: string
  note?: string
}

export interface EventInsightPayload {
  summary?: string
  abnormalOverview?: string
  overview?: string
  parsedEvent?: Record<string, unknown>
  context?: EventInsightContext
  risk?: EventInsightRisk
  advice?: EventInsightAdvice
  trace?: EventInsightTrace
  freshness?: EventInsightFreshness
  riskLevel?: string
  analysisReasons?: string[] | string
  reasons?: string[] | string
  possibleCauses?: string[] | string
  possibleReasons?: string[] | string
  suggestedActions?: string[] | string
  actions?: string[] | string
  notifyWho?: string[] | string
  confidence?: number | string
  source?: string
}

export interface EventInsightSnapshotSummary {
  id?: number | string
  eventId?: number | string
  summary?: string
  riskLevel?: string
  riskScore?: number | string
  orchestratorVersion?: string
  fallbackUsed?: boolean
  freshnessState?: string
  freshnessTone?: string
  freshnessNote?: string
  generatedAt?: string
  createTime?: string
}

export function chatAi(message: string) {
  return request({
    url: 'ai/chat',
    method: 'post',
    timeout: 60000,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    // Send JSON string payload to avoid axios defaulting plain strings to form-urlencoded.
    data: JSON.stringify(message)
  })
}

export function detectFall(data: Record<string, unknown>) {
  return request({
    url: 'ai/fall/detect',
    method: 'post',
    data
  })
}

export function assessRisk(data: Record<string, unknown>) {
  return request({
    url: 'ai/risk/assess',
    method: 'post',
    data
  })
}

export function analyzeTrend(data: Record<string, unknown>) {
  return request({
    url: 'ai/trend/analyze',
    method: 'post',
    data
  })
}

export function checkQuality(data: Record<string, unknown>) {
  return request({
    url: 'ai/quality/check',
    method: 'post',
    data
  })
}

export function detectAbnormal(data: Record<string, unknown>) {
  return request({
    url: 'ai/abnormal/detect',
    method: 'post',
    data
  })
}

export function getEventInsight(
  eventId: number | string,
  options: { refresh?: boolean } = {}
) {
  return request({
    url: `ai/event/insight/${eventId}`,
    method: 'get',
    params: {
      refresh: options.refresh ? 'true' : undefined
    },
    headers: {
      'X-Skip-Error-Message': 'true'
    }
  })
}

export function getEventInsightSnapshots(
  eventId: number | string,
  options: { limit?: number } = {}
) {
  return request({
    url: `ai/event/insight/${eventId}/snapshots`,
    method: 'get',
    params: {
      limit: options.limit ?? 10
    },
    headers: {
      'X-Skip-Error-Message': 'true'
    }
  })
}

export function getEventInsightSnapshot(
  eventId: number | string,
  snapshotId: number | string
) {
  return request({
    url: `ai/event/insight/${eventId}/snapshots/${snapshotId}`,
    method: 'get',
    headers: {
      'X-Skip-Error-Message': 'true'
    }
  })
}

export function getRecentAbnormal(limit = 10) {
  return request({
    url: 'ai/abnormal/recent',
    method: 'get',
    params: {
      limit
    }
  })
}
