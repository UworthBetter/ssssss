# P6-003 backend-driven global search contract

## Goal

Replace heuristic platform search routing with a backend-backed contract that
aggregates real subject, device, and event data before deciding where to send
the operator.

## What changed

- Added `src/api/platformSearch.ts` as the first shared backend search contract
  for the platform shell.
- Reused existing backend list APIs instead of inventing a fake frontend-only
  search model:
  - `GET /health/subject/list`
  - `GET /health/deviceInfo/list`
  - `GET /health/exception/list`
  - `GET /health/exception/listByUserId`
- Normalized subject, device, and event matches into one result shape with:
  - `kind`
  - `title`
  - `subtitle`
  - `description`
  - `score`
  - `target`
- Replaced the old keyword heuristic in `platformServices.ts` with a real
  search flow:
  1. Prompt for the keyword
  2. Query backend-backed entity lists concurrently
  3. Deduplicate and rank matches
  4. Route to the highest-confidence result
  5. Tell the operator how many matches were found

## Contract notes

- This is a frontend-level aggregation contract built on top of existing
  backend list APIs.
- The contract is already backend-driven because result eligibility now comes
  from real API rows instead of a local regex guess.
- The current backend still does not expose one dedicated `/platform/search`
  endpoint, so this slice intentionally uses an orchestration layer in the
  frontend.

## Result ranking

- Exact field hits are ranked above contains matches.
- The active center gets a small boost so operators searching inside the device
  or event center are more likely to stay in their current workflow when there
  are tied candidates.
- Query payloads are shaped to match current page hydration behavior so the
  destination page can immediately reload the relevant list.

## Follow-up

- Replace the current aggregated contract with a dedicated backend search API
  once the backend exposes one.
- Add a result picker UI when operators need to choose among several strong
  matches instead of auto-opening the top hit.
- Extend the contract with stronger entity IDs and join metadata when the
  platform gets cross-center identity endpoints.
