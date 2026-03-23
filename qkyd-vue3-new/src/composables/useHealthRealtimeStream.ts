import { onBeforeUnmount, onMounted } from 'vue'

export interface HealthRealtimeMessage {
  type: string
  patientId?: number | string
  data?: Record<string, unknown>
  timestamp?: number
  [key: string]: unknown
}

interface UseHealthRealtimeStreamOptions {
  onMessage?: (message: HealthRealtimeMessage) => void
  onAbnormalAlert?: (payload: Record<string, unknown>, message: HealthRealtimeMessage) => void
  onHealthData?: (payload: Record<string, unknown>, message: HealthRealtimeMessage) => void
  onRiskScore?: (payload: Record<string, unknown>, message: HealthRealtimeMessage) => void
}

let socket: WebSocket | null = null
let heartbeatTimer: number | null = null
let reconnectTimer: number | null = null
let retainCount = 0
let subscribedPatientId: string | number | null = null
let isIntentionalClose = false
let listenerSeed = 0

const listeners = new Map<number, (message: HealthRealtimeMessage) => void>()

const getRealtimeSocketUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws/health/data`
}

const clearHeartbeat = () => {
  if (heartbeatTimer !== null) {
    window.clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

const clearReconnect = () => {
  if (reconnectTimer !== null) {
    window.clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

const notifyListeners = (message: HealthRealtimeMessage) => {
  listeners.forEach((listener) => {
    try {
      listener(message)
    } catch {
      // Keep the shared stream alive even if one consumer fails.
    }
  })
}

const sendSubscribe = (patientId: string | number | null | undefined) => {
  if (!socket || socket.readyState !== WebSocket.OPEN) return
  if (patientId == null || patientId === '') return

  socket.send(JSON.stringify({
    type: 'subscribe',
    patientId
  }))
}

const closeSocket = () => {
  isIntentionalClose = true
  clearHeartbeat()
  clearReconnect()
  if (socket) {
    socket.close()
    socket = null
  }
}

const scheduleReconnect = () => {
  if (retainCount <= 0 || reconnectTimer !== null) return
  reconnectTimer = window.setTimeout(() => {
    reconnectTimer = null
    ensureSocket()
  }, 3000)
}

function ensureSocket() {
  if (typeof window === 'undefined') return
  if (socket && [WebSocket.OPEN, WebSocket.CONNECTING].includes(socket.readyState)) return

  isIntentionalClose = false
  clearReconnect()
  socket = new WebSocket(getRealtimeSocketUrl())

  socket.onopen = () => {
    clearHeartbeat()
    heartbeatTimer = window.setInterval(() => {
      if (socket?.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'heartbeat' }))
      }
    }, 20000)

    sendSubscribe(subscribedPatientId)
  }

  socket.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data) as HealthRealtimeMessage
      notifyListeners(message)
    } catch {
      // Ignore malformed payloads and keep the socket alive.
    }
  }

  socket.onclose = () => {
    socket = null
    clearHeartbeat()
    if (!isIntentionalClose) {
      scheduleReconnect()
    }
  }

  socket.onerror = () => {
    // Connection fallback is handled by polling and reconnect scheduling.
  }
}

export const useHealthRealtimeStream = (options: UseHealthRealtimeStreamOptions = {}) => {
  const listenerId = ++listenerSeed
  let active = false

  const listener = (message: HealthRealtimeMessage) => {
    options.onMessage?.(message)
    const payload = (message.data ?? {}) as Record<string, unknown>

    if (message.type === 'abnormalAlert') {
      options.onAbnormalAlert?.(payload, message)
      return
    }
    if (message.type === 'healthData') {
      options.onHealthData?.(payload, message)
      return
    }
    if (message.type === 'riskScore') {
      options.onRiskScore?.(payload, message)
    }
  }

  const connect = () => {
    if (active) return
    active = true
    retainCount += 1
    listeners.set(listenerId, listener)
    ensureSocket()
  }

  const disconnect = () => {
    if (!active) return
    active = false
    listeners.delete(listenerId)
    retainCount = Math.max(0, retainCount - 1)
    if (retainCount === 0) {
      closeSocket()
    }
  }

  const subscribePatient = (patientId: string | number | null | undefined) => {
    if (patientId == null || patientId === '') return
    subscribedPatientId = patientId
    sendSubscribe(patientId)
  }

  onMounted(() => {
    connect()
  })

  onBeforeUnmount(() => {
    disconnect()
  })

  return {
    connect,
    disconnect,
    subscribePatient
  }
}
