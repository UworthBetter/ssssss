# P6-002 platform shared services

This slice extracts the first reusable platform behaviors out of page-local
handlers.

## Goal

The goal is to stop treating search, notification, and action handoff as
per-page one-off logic.

## What changed

- Added `src/components/platform/platformServices.ts` as the first shared
  platform service layer.
- Centralized platform search presentation and search routing heuristics.
- Centralized notification feed construction and notification click routing.
- Centralized action dispatch for common cross-center business actions such as
  creating events, opening subjects, opening devices, and opening rules.
- Updated `AiWorkbenchView.vue`, `ExceptionAlertView.vue`,
  `HealthSubjectView.vue`, and `DeviceInfoView.vue` to use the new shared
  platform services instead of page-local placeholder handlers.
- Cleaned the default copy in `PlatformSearchEntry.vue` and
  `PlatformNotificationEntry.vue` so the shared platform entry points no longer
  carry garbled fallback strings.

## Shared services now in place

- `getPlatformSearchPresentation()`
- `openPlatformSearch()`
- `buildPlatformNotifications()`
- `openPlatformNotification()`
- `openAllPlatformNotifications()`
- `navigatePlatformEntity()`
- `dispatchPlatformAction()`

## Verification

- `npm run build` passed in `qkyd-vue3-new` on March 22, 2026.

## Follow-up

- Move from heuristic search routing to a real backend-backed global search
  contract.
- Replace static notification feeds with live platform notifications when the
  backend exposes them.
- Extract repeated route-query hydration into a smaller shared helper during the
  next P6 hardening slice.
