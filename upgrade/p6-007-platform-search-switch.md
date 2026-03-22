# P6-007 canonical platform search switch

This slice switches the frontend search path to prefer the canonical backend
endpoint while keeping the aggregated adapter as a safe fallback.

## What changed

- Updated `src/api/platformSearch.ts` so `searchPlatformEntities()` now prefers
  `GET /platform/search`.
- Kept the current aggregated search adapter as a compatibility fallback when
  the dedicated endpoint is unavailable or not yet returning the expected
  result shape.
- Preserved the multi-result chooser and route-ready result contract already
  used by the platform shell.

## Why the fallback remains

The current production environment still sits behind authentication, and direct
unauthenticated probing returns `401`. That means the frontend can safely
prefer the canonical endpoint now, while still protecting the operator
experience if the dedicated endpoint is not ready in every environment.

## Follow-up

- Remove the fallback aggregation path after the dedicated endpoint is stable in
  production and verified with authenticated traffic.
- Keep the dedicated endpoint aligned with
  `upgrade/platform-search-api-contract.md`.
