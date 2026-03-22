import type { PlatformNotificationItem } from './PlatformNotificationEntry.vue'

export type PlatformCenter = 'workbench' | 'subject' | 'event' | 'device' | 'ai'
export type PlatformEntityKind = 'subject' | 'event' | 'device'

export interface PlatformEntityRef {
  kind: PlatformEntityKind
  name: string
  query?: Record<string, string>
}

export interface PlatformNotificationTarget {
  path: string
  query?: Record<string, string>
}

export interface PlatformNotificationRecord extends PlatformNotificationItem {
  target: PlatformNotificationTarget
}

export interface PlatformSearchPresentation {
  label: string
  placeholder: string
  hint: string
}

export interface PlatformActionDispatchOptions {
  entities?: Partial<Record<PlatformEntityKind, PlatformEntityRef>>
  onExportCsv?: () => void
  onExportReport?: () => void
  onUnhandled?: (action: string) => void
}
