export { default as PlatformPageShell } from './PlatformPageShell.vue'
export { default as PlatformSearchEntry } from './PlatformSearchEntry.vue'
export { default as PlatformNotificationEntry } from './PlatformNotificationEntry.vue'
export { default as PlatformContextFilterBar } from './PlatformContextFilterBar.vue'
export type { PlatformNotificationItem } from './PlatformNotificationEntry.vue'
export type { PlatformContextFilters } from './PlatformContextFilterBar.vue'
export {
  buildPlatformNotifications,
  dispatchPlatformAction,
  getPlatformSearchPresentation,
  navigatePlatformEntity,
  openAllPlatformNotifications,
  openPlatformNotification,
  openPlatformSearch
} from './platformServices'
export { loadPlatformNotifications } from '@/api/platformNotifications'
export type {
  PlatformActionDispatchOptions,
  PlatformCenter,
  PlatformEntityKind,
  PlatformEntityRef,
  PlatformNotificationRecord,
  PlatformNotificationTarget,
  PlatformSearchPresentation
} from './platformTypes'
