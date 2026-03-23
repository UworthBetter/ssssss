export { default as PlatformPageShell } from './PlatformPageShell.vue'
export { default as PlatformPageShellV2 } from './PlatformPageShellV2.vue'
export { default as PlatformSearchEntry } from './PlatformSearchEntry.vue'
export { default as PlatformNotificationEntry } from './PlatformNotificationEntry.vue'
export type { PlatformNotificationItem } from './PlatformNotificationEntry.vue'
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
