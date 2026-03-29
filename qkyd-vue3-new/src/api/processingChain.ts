import request from '@/utils/request'

export const startProcessingChain = (eventId: string | number, data?: Record<string, unknown>) => {
  return request.post(`ai/event-processing/start/${eventId}`, data || {})
}

export const getProcessingChain = (eventId: string | number) => {
  return request.get(`ai/event-processing/status/${eventId}`)
}

export const getAnomalyDetail = (eventId: string | number) => {
  return request.get(`ai/event-processing/status/${eventId}`)
}

export const getAuditLog = (eventId: string | number) => {
  return request.get(`ai/event-processing/audit-trail/${eventId}`)
}
