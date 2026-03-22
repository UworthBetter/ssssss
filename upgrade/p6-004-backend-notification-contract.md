# P6-004 backend-driven notification contract

This slice moves platform notifications away from static frontend examples and
onto real backend-backed entity data, while keeping the existing notification
entry UI stable.

## What changed

- Added `src/api/platformNotifications.ts` as the first notification adapter
  for the platform shell.
- Kept `PlatformNotificationEntry.vue` as a rendering component and removed its
  static sample payloads.
- Switched the AI center and event center to load notifications asynchronously
  from the new adapter.
- Kept `platformServices.ts` focused on navigation and action behavior, while
  notifications now come from real API rows.

## Current data sources

The current backend does not expose a dedicated platform notifications API, so
the adapter builds notifications from existing entity endpoints:

- `GET /health/exception/list`
- `GET /ai/abnormal/recent`
- `GET /health/subject/list`
- `GET /health/deviceInfo/list`

## Contract shape

The adapter still returns the shared `PlatformNotificationRecord` shape so the
UI layer does not need a second notification model.

- `id`
- `title`
- `description`
- `time`
- `level`
- `target.path`
- `target.query`

## Behavior

- Event center notifications now prioritize pending exception rows.
- AI center notifications now combine recent abnormal rows with the latest AI
  context when related subject or device entities are available.
- When backend data is empty or unavailable, the adapter falls back to one
  minimal reminder so the shell does not look broken.

## Follow-up

- Replace the adapter with a dedicated backend notifications endpoint when the
  platform exposes one.
- Add read state, category, and SLA metadata once the backend supports them.
- Extend the live notification feed to subject and device centers after this
  event and AI slice is validated.
