# QKYD platform upgrade status

Use this file as the single handoff record between sessions.

## Snapshot

- `current_phase`: P6
- `active_item`: P6 complete
- `state`: done
- `last_updated`: 2026-03-22

## Done this session

- Completed `P6-001` by replacing AI entity and action placeholder toasts with
  real route handoff into the subject, event, and device centers.
- Rebuilt `AiWorkbenchView.vue` into a clean UTF-8 page with unified Chinese
  product copy while preserving the structured workbench layout.
- Rebuilt `HealthSubjectView.vue`, `DeviceInfoView.vue`, and
  `ExceptionAlertView.vue` into clean UTF-8 Vue SFCs after historical encoding
  noise began breaking template parsing.
- Added route-query-driven filter hydration in the subject, event, and device
  centers so cross-center jumps arrive with prefilled context.
- Preserved the current health-related backend APIs while hardening the first
  real AI-to-business-center handoff.
- Verified that the current backend is online, but unauthenticated data calls
  return `401` rather than a transport failure.
- Hardened `request.ts` so expired login state now shows a clear message and
  redirects back to the login page instead of surfacing a garbled generic
  failure.
- Hardened `DashboardView.vue` so it uses partial-failure tolerance instead of
  failing the full data chain when one request in the batch rejects.
- Completed `P6-002` by extracting shared platform search, notification, and
  action-dispatch behavior into `src/components/platform/platformServices.ts`.
- Rewired the AI, event, subject, and device centers to consume the new shared
  platform services instead of page-local search and action stubs.
- Cleaned the default copy in the shared search and notification entry
  components so shared platform entry points now use stable Chinese text.
- Completed `P6-003` by introducing `src/api/platformSearch.ts` as the first
  backend-driven global search contract built on top of the existing subject,
  device, and event list APIs.
- Replaced heuristic platform search routing with a real search flow that
  queries backend-backed entity lists, normalizes results, ranks candidates,
  and opens the best match.
- Rebuilt `platformServices.ts` into a clean UTF-8 shared service file while
  landing the new search contract, so platform copy and behaviors are now
  stable in one place.
- Completed `P6-004` by introducing `src/api/platformNotifications.ts` as the
  first backend-driven notification adapter built on top of existing exception,
  AI abnormal, subject, and device APIs.
- Rewired the AI center and event center to load notification entries from the
  new backend-backed adapter instead of static frontend notification examples.
- Added a multi-result selector to global search so ambiguous backend-backed
  search results now require operator choice instead of auto-opening the first
  match.
- Added a dedicated backend search API proposal in
  `upgrade/platform-search-api-contract.md` so the current frontend aggregation
  layer has a clear replacement path.
- Completed `P6-006` by introducing `src/composables/useRouteQueryListSync.ts`
  and rewiring the subject, device, and event centers to share one route-query
  hydration and selected-row restoration flow.
- Completed `P6-007` by switching `platformSearch.ts` to prefer
  `GET /platform/search` while keeping the aggregated entity-list search as a
  compatibility fallback.

## Verification

- `npm run build` passed in `qkyd-vue3-new` after the `P6-001` cross-center
  handoff changes.
- `npm run build` still passed after the auth-aware request handling and
  dashboard partial-failure resilience changes.
- `npm run build` still passed after landing the shared platform services in
  `P6-002`.
- `npm run build` still passed after replacing heuristic search with the
  backend-driven global search contract in `P6-003`.
- `npm run build` still passed after landing the backend-driven notification
  adapter and multi-result search selection flow.
- `npm run build` still passed after landing the shared route-query sync helper
  and the canonical platform search switch path.

## Next item

1. P6 is complete.
2. If the next round continues, start from acceptance review, wider smoke
   testing, or a later performance pass around bundle size.

## Blockers

- None for the P6 implementation slice.

## Risks to watch

- Cross-center routing still relies on route query context instead of stable
  entity join endpoints, even though search is now backed by real API rows.
- Notifications now use backend-backed entity rows, but they still depend on an
  adapter layer because the backend does not yet expose one dedicated platform
  notifications endpoint.
- The canonical `/platform/search` endpoint is now the preferred frontend path,
  but unauthenticated probing still returns `401`, so authenticated runtime
  validation remains important.
- The AI and app shell bundles are still large and should be revisited in a
  later performance pass.

## Handoff

- What changed: AI, subject, event, and device centers now share the first real
  route handoff instead of placeholder toasts, and the affected pages were
  rebuilt into clean UTF-8 files. Request handling and the dashboard polling
  flow are now more resilient to expired login state and partial request
  failures. Search, notification, and action dispatch are now extracted into a
  shared platform service layer. Global search is now backed by real backend
  entity lists through the new aggregated platform search contract. AI and
  event notifications now also load from a backend-backed adapter, and
  ambiguous search hits now open a result picker instead of auto-jumping. The
  subject, device, and event centers now share one route-query sync helper, and
  frontend search now prefers `/platform/search` with a compatibility fallback.
- What remains: route handoff should keep moving toward stronger entity
  identity instead of query-only linkage, and the frontend still needs to swap
  from notification/search adapters to fully canonical backend platform
  endpoints when those services mature.
- Exact next step: P6 is complete. If work continues, start from acceptance
  review, broader smoke testing, or a focused performance hardening pass.
- Avoid breaking: keep the new route-query handoff behavior intact between AI,
  subject, event, and device centers while iterating on shared platform
  services.
