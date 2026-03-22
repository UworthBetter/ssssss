# P6-006 shared route-query sync

This slice removes repeated route-query hydration logic from the subject,
device, and event centers.

## What changed

- Added `src/composables/useRouteQueryListSync.ts` as a shared synchronization
  helper for list pages that hydrate query state from route parameters.
- Rewired the subject, device, and event centers to use the shared helper for:
  - route query hydration
  - first-load fetch
  - route-change refresh
  - selected-row restoration after reload

## Why it matters

Before this slice, three platform centers each carried their own copy of:

- `applyRouteQuery`
- `watch(() => route.query, ...)`
- `onMounted(() => ...)`
- matched-row fallback logic

The shared helper keeps the behavior stable while reducing drift between the
centers.

## Follow-up

- Expand the helper only when more list-detail centers adopt the same pattern.
- Keep field mapping and row matching page-local unless a second stable
  abstraction appears naturally.
