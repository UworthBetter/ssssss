# QKYD platform upgrade status

Use this file as the single handoff record between sessions.

## Snapshot

- `current_phase`: P5
- `active_item`: P5-006 backend trace contract for event insight
- `state`: done
- `last_updated`: 2026-03-22

## Done this session

- Completed `P5-002` by adding the first backend multi-agent event insight
  contract in `qkyd-ai` with `GET /ai/event/insight/{eventId}`.
- Added `EventInsightController`, `IEventInsightService`,
  `EventInsightServiceImpl`, and `EventInsightResultDTO` so the event center
  can request one structured payload with `summary`, `parsedEvent`, `context`,
  `risk`, and `advice`.
- Implemented a deterministic fallback path in the backend insight service so
  the feature still returns a card when some health context is missing.
- Extended `ExceptionAlertView.vue` with the first AI insight card in the right
  sidebar, including loading, empty, and fallback states.
- Completed `P5-005` by visualizing the four-agent processing flow directly
  inside the event-center AI insight card.
- Added per-agent status rows for event parsing, context enrichment, risk
  assessment, and action recommendation, including progress counts and fallback
  signals.
- Added `getEventInsight` and the related payload typings to
  `qkyd-vue3-new/src/api/ai.ts`.
- Added `upgrade/multi-agent-event-insight-design.md` to capture agent roles,
  orchestration flow, data contract, fallback strategy, and rollout steps.
- Completed `P5-006` by adding a real backend `trace` field to the event
  insight contract with orchestrator metadata, missing-field reporting, and
  per-agent step records.
- Rewired the event-center AI card to prefer backend `trace.steps` over
  frontend heuristics, while keeping the previous inference path as a safe
  compatibility fallback.
- Cleaned the backend trace and summary copy so the new agent titles, trace
  details, and fallback reasons render correctly in the operator UI.

## Verification

- `mvn -pl qkyd-ai -am -DskipTests compile` passed in `backend`.
- `mvn -pl qkyd-admin -am -DskipTests package` passed in `backend`.
- `npm run build` passed in `qkyd-vue3-new`.

## Next item

1. Add persistence for the event insight result so operators can reopen past
   decisions without recomputing the full chain.
2. Replace heuristic context derivation in `EventInsightServiceImpl` with real
   device-state, history, and subject-health queries from `qkyd-health`.
3. Decide whether to persist raw `trace.steps` as a snapshot or regenerate
   trace views from stored structured insight fields.

## Blockers

- None for the current P5 slice.

## Risks to watch

- The current event insight service still derives several context fields with
  deterministic heuristics instead of true health-domain joins.
- Event insight results are computed on demand and are not yet persisted, so
  repeated panel opens rerun the orchestration path.
- The frontend still keeps a compatibility fallback for missing `trace` data;
  remove that inference path only after all target environments run the new
  backend contract.

## Handoff

- What changed: the platform now has a first multi-agent event insight slice
  that connects `qkyd-ai`, `qkyd-health`, and the event center UI. Operators
  can select an event and see a structured AI card with abnormal overview, the
  four-agent processing flow, risk level, reasons, notify targets, and
  suggested actions. The card now also consumes canonical backend `trace`
  metadata instead of inferring process state only from result fields.
- What remains: persist the insight result, keep trace data queryable over
  time, and replace heuristic enrichment with real health-domain context.
- Exact next step: introduce an insight snapshot record and decide whether the
  snapshot stores full trace steps or reconstructs them from persisted
  structured fields while keeping the existing API contract stable.
- Avoid breaking: keep `GET /ai/event/insight/{eventId}` stable, and preserve
  the event-center sidebar card contract while tightening backend internals.
