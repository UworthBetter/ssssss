# QKYD platform upgrade status

Use this file as the single handoff record between sessions.

## Snapshot

- `current_phase`: P7
- `active_item`: P7-001 rebuild the home workbench into a competition-grade risk command screen
- `state`: in_progress
- `last_updated`: 2026-03-23

## Done this session

- Completed `P7-006a` by adding demo-only secondary pages for subject health
  records, subject risk profile, device monitor, device maintenance, AI model
  logs, AI rule configuration, operation reports, and health reports.
- Extended the platform navigation so those secondary pages are reachable from
  the grouped platform shell, including a new report-center route group for the
  competition presentation flow.
- Added one shared demo boundary to `PlatformPageShellV2` and marked the new
  secondary pages as `演示版 · 当前使用模拟数据` so the frontend can be shipped
  and demoed without pretending that the data contracts are already real.
- Validated the new frontend slice with `npm run build` and prepared it for a
  frontend-only Lighthouse deployment instead of dragging unrelated backend
  workspace changes into the same submission.
- Replanned the next upgrade stage around competition demonstration value and
  practical problem-solving clarity instead of continuing deeper trust-layer
  microfeatures in the event-center history flow.
- Added `P7-001` through `P7-006` to `upgrade/backlog.md` so the next slices
  now target a stronger first-screen story, curated demo cases, continuous-risk
  subject storytelling, action-oriented AI output, guided demo flow, and
  stronger presentation data.
- Logged `D-036` to treat judge comprehension of real usefulness as the next
  optimization target, with the home workbench as the first priority because it
  controls the first ten seconds of product understanding.
- Completed `P5-016` by separating fallback provenance from snapshot-history
  freshness in the event-center combined viewer so operators now see one
  freshness badge for timeliness and one secondary badge for fallback origin.
- Updated `EventInsightServiceImpl` so snapshot-summary freshness now expresses
  recency only, while `fallbackUsed` remains the independent provenance signal
  for historical records.
- Updated `ExceptionAlertView.vue` so the history list renders risk,
  freshness, and fallback badges side by side, with compatibility fallback
  logic that keeps freshness time-based even when summary freshness metadata is
  missing.
- Logged `D-035` to keep recency and provenance as separate operator signals
  instead of overloading one history badge with both meanings.
- Completed `P5-015` by adding backend-authored freshness fields to
  `EventInsightSnapshotSummaryDTO`, enriching snapshot-history rows in
  `EventInsightServiceImpl`, and exposing one lightweight freshness badge for
  each historical record in the combined event-center history viewer.
- Updated `qkyd-vue3-new/src/api/ai.ts` and `ExceptionAlertView.vue` so the
  history list now prefers backend snapshot-summary freshness metadata, falls
  back to `generatedAt` and `fallbackUsed` when needed, and renders the result
  as a compact tag beside the existing risk tag.
- Logged `D-034` to keep historical snapshots read-only and auditable while
  surfacing freshness as scan-time state instead of introducing implicit
  auto-refresh semantics inside history browsing.
- Completed `P5-014` by adding a backend-authored `freshness` object to
  `EventInsightResultDTO` and populating it for both the latest-insight path
  and historical snapshot detail reads.
- Updated `EventInsightServiceImpl` so the service now emits one canonical
  freshness `state`, `tone`, and `note` based on fallback usage and snapshot
  age, while still enriching older stored snapshots on read without requiring
  a migration.
- Updated `qkyd-vue3-new/src/api/ai.ts` and `ExceptionAlertView.vue` so the
  event-center UI now prefers backend freshness metadata for both the latest
  AI card and historical detail preview, but keeps `generatedAt`-based local
  fallback for compatibility with older payloads.
- Aligned the latest-insight scope note with the current product capability so
  the event-center card now states that operators can open historical
  snapshots through the existing "查看历史" entry.
- Logged `D-033` to keep freshness semantics authored in the backend payload
  while preserving the current frontend compatibility path.
- Completed `P5-013` by collapsing the event-center snapshot history list and
  historical detail view into one combined read-only viewer inside the same
  dialog.
- Updated `ExceptionAlertView.vue` so the history experience now renders a
  left-side snapshot list and a right-side inline historical insight preview,
  auto-selects the latest available snapshot, and removes the extra detail
  dialog hop from the operator flow.
- Kept the existing latest-insight, snapshot-history, and snapshot-detail API
  boundaries unchanged while logging `D-032` to make the unified viewer the
  default operator experience for snapshot browsing.
- Completed `P5-012` by adding a dedicated historical snapshot detail-read path
  in `qkyd-ai` and wiring the event-center history dialog to open one
  historical insight payload in a separate read-only detail view.
- Added `selectByIdAndEventId` in the snapshot mapper plus a new
  `GET /ai/event/insight/{eventId}/snapshots/{snapshotId}` controller route so
  historical payload reads stay separate from the current latest-insight API.
- Updated `qkyd-vue3-new/src/api/ai.ts` and `ExceptionAlertView.vue` so each
  history item now exposes a "查看详情" action and opens a read-only historical
  insight detail dialog that reuses the existing normalized insight rendering
  structure.
- Logged `D-031` to keep historical payload reads on their own path and avoid
  overloading the semantics of `GET /ai/event/insight/{eventId}`.
- Completed `P5-011` by adding a read-only snapshot history path for event
  insight in both `qkyd-ai` and the event-center AI card without disturbing the
  existing latest-insight endpoint.
- Added a new backend snapshot-summary DTO, mapper query, service method, and
  `GET /ai/event/insight/{eventId}/snapshots` controller entry so operators can
  request recent snapshot summaries for one event.
- Updated `qkyd-vue3-new/src/api/ai.ts` and `ExceptionAlertView.vue` so the AI
  card now exposes a "查看历史" entry beside "刷新研判" and opens a read-only
  history dialog that shows recent snapshot time, summary, risk, and fallback
  state.
- Logged `D-030` to keep snapshot history read-only for now and avoid adding
  restore or replay semantics before operators prove they need that behavior.
- Completed `P5-010` by clarifying in the event-center AI card that the
  current operator experience only exposes the latest insight snapshot and does
  not yet provide snapshot history.
- Updated `ExceptionAlertView.vue` so the AI card now pairs freshness guidance
  with a direct latest-snapshot scope note, keeping operator expectations
  aligned with the actual backend and UI capability.
- Logged `D-029` to keep snapshot history out of scope for now and make the
  latest-only boundary explicit in the current event-center UI.
- Completed `P5-009` by adding snapshot freshness guidance to the event-center
  AI insight card so operators can see when the current result was generated
  and get a lightweight refresh cue when the snapshot is aging.
- Updated `ExceptionAlertView.vue` to derive a freshness note from the existing
  backend `generatedAt` field, keep fallback insight states visually distinct,
  and surface a warning-style hint when the current insight is older than the
  lightweight UI threshold.
- Logged `D-028` to keep freshness guidance on the frontend side for now by
  reusing the existing timestamp contract instead of introducing a new backend
  freshness field.
- Completed `P0-003` by re-running the clean reactor verification from
  `backend` and confirming that
  `mvn -pl qkyd-ai -am -DskipTests clean compile` now passes end to end through
  `qkyd-common`, `qkyd-system`, `qkyd-health`, and `qkyd-ai`.
- Used one read-only subagent plus local build forensics to verify that
  `qkyd-health` still declares a compile dependency on `qkyd-system`, the root
  reactor order is correct, no structural module cycle exists, and the clean
  build now resolves `qkyd-system` on the `qkyd-health` compile classpath.
- Logged `D-027` to treat `backend/pom.xml` as the canonical verification entry
  for future clean reactor builds and to keep the same clean compile command as
  the proof step for later backend slices.
- Rebuilt `backend/qkyd-ai/.../EventInsightServiceImpl.java` as clean UTF-8
  source while preserving the existing event insight contract, snapshot
  persistence flow, and `qkyd-health` context-enrichment integration that the
  current workspace already depends on.
- Finished the backend-only `P5-007` minimum path in `qkyd-ai` by wiring
  snapshot reuse, stale detection, and optional `refresh=true` handling into
  the event insight service without changing the `GET /ai/event/insight/{eventId}`
  response DTO shape.
- Completed `P5-008` by adding the first operator-facing refresh entry directly
  inside the event-center AI insight card so staff can request a fresh
  recomputation for the selected event without leaving the sidebar workflow.
- Updated `qkyd-vue3-new/src/api/ai.ts` so `getEventInsight` now accepts an
  optional `refresh` flag and forwards it to
  `GET /ai/event/insight/{eventId}` as a compatible query parameter.
- Updated `ExceptionAlertView.vue` so the AI card now exposes a
  "刷新研判" action, shows a dedicated refresh loading state, and reports
  success or fallback warnings after one-shot manual recomputation.
- Logged `D-026` to keep the first manual refresh control local to the event
  insight card instead of introducing a separate page-level refresh workflow.
- Completed `P5-007` by adding a compatible refresh path to
  `GET /ai/event/insight/{eventId}` through an optional `refresh` query
  parameter while keeping the response contract unchanged.
- Updated `IEventInsightService` and `EventInsightServiceImpl` so the event
  insight flow now supports both explicit refresh requests and backend stale
  snapshot detection.
- Added stale-snapshot checks that compare the latest persisted snapshot
  against real health-context timestamps such as location time, binding time,
  and latest report date before deciding whether to reuse or recompute.
- Logged `D-025` to make "automatic stale detection plus optional manual
  refresh" the default refresh-control strategy for event insight snapshots.
- Completed `P5-004` by adding a new `qkyd-health` event insight context
  aggregator and wiring `qkyd-ai` to prefer real health-domain context fields
  over local heuristics for age, latest location, report-based trend, history,
  and device signal status.
- Added `EventInsightHealthContext`,
  `IEventInsightHealthContextService`, and
  `EventInsightHealthContextService` in `qkyd-health` to aggregate subject,
  location, binding, report, and exception history into one AI-facing context
  object.
- Updated `EventInsightServiceImpl` so real `qkyd-health` context now feeds
  `recentHealthTrend`, `historicalAbnormalCount`, `recentSameTypeCount`,
  `deviceStatus`, `deviceStatusReason`, `lastKnownLocation`, and
  `dataConfidence`, while keeping the previous heuristic logic as a safe
  fallback.
- Logged `D-024` to make the `qkyd-health` context aggregator the default
  source of truth for event insight context enrichment.
- Completed `P5-003` by adding backend event insight snapshot persistence in
  `qkyd-ai` with a new `ai_event_insight_snapshot` table, mapper, and snapshot
  record entity.
- Updated `EventInsightServiceImpl` so `GET /ai/event/insight/{eventId}` now
  prefers the latest stored snapshot for the same event and falls back to live
  recomputation only when no snapshot can be loaded.
- Persisted the full serialized `EventInsightResultDTO` payload as the
  canonical snapshot body while also storing summary, risk, orchestrator
  version, fallback flag, and timestamps for future filtering and audits.
- Added `backend/sql/create_ai_event_insight_snapshot_table.sql` as the first
  migration script for snapshot storage.
- Logged `D-023` to keep full-payload snapshot storage and snapshot-first reads
  as the current persistence strategy behind the stable event insight contract.
- Added `upgrade/subagent-collaboration-protocol.md` to define the standing
  agent model, write boundaries, activation rules, handoff format, and quality
  gates for stable multi-agent upgrade work.
- Updated `upgrade/coordinator.md` so the coordinator now defers to the shared
  collaboration protocol instead of a stale fixed-agent list.
- Logged `D-022` to make single-owner shared boundaries and staged agent
  activation the default execution model for future upgrade sessions.
- Added `P0-002` to `upgrade/backlog.md` as the completed collaboration-control
  artifact for the upgrade program.
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

- `npm run build` passed in `qkyd-vue3-new` after the new demo-only secondary
  pages and mock-data status badges landed.
- `mvn -pl qkyd-ai -am -DskipTests clean compile` passed in `backend` after the
  snapshot-history provenance split landed.
- `npm run build` passed in `qkyd-vue3-new` after the event-center history list
  rendered separate freshness and fallback badges.
- `mvn -pl qkyd-ai -am -DskipTests clean compile` passed in `backend` after the
  snapshot-summary freshness enrichment changes landed.
- `npm run build` passed in `qkyd-vue3-new` after the history-list freshness
  badges landed in the event center.
- `mvn -pl qkyd-ai -am -DskipTests clean compile` passed in `backend` after the
  event insight freshness field and service enrichment changes landed.
- `npm run build` passed in `qkyd-vue3-new` after the event-center freshness
  contract consumer and latest-scope-note alignment landed.
- `npm run build` passed in `qkyd-vue3-new` after the combined snapshot history
  viewer landed in the event center.
- `mvn -pl qkyd-ai -am -DskipTests clean compile` passed in `backend` after the
  historical snapshot detail endpoint landed.
- `npm run build` passed in `qkyd-vue3-new` after the historical snapshot
  detail dialog landed.
- `mvn -pl qkyd-ai -am -DskipTests clean compile` passed in `backend` after the
  read-only snapshot history endpoint landed.
- `npm run build` passed in `qkyd-vue3-new` after the event-center snapshot
  history dialog landed.
- `npm run build` passed in `qkyd-vue3-new` after the latest-snapshot scope
  clarification landed in the event-center AI card.
- `npm run build` passed in `qkyd-vue3-new` after the snapshot freshness hint
  landed in the event-center AI card.
- `mvn -pl qkyd-ai -am -DskipTests clean compile` passed in `backend` after the
  clean-build verification rerun for `P0-003`.
- `cmd /c "mvn -pl qkyd-health -am -DskipTests dependency:tree -Dincludes=com.qkyd:qkyd-system"`
  passed in `backend` and confirmed the `qkyd-health -> qkyd-system` compile
  dependency remains present.
- `mvn -pl qkyd-ai -am -DskipTests compile` passed in `backend` after the
  `EventInsightServiceImpl.java` UTF-8 rebuild and backend refresh-control
  implementation landed.
- `npm run build` passed in `qkyd-vue3-new` after the event-center manual
  refresh entry landed.
- `mvn -pl qkyd-ai -am -DskipTests compile` passed in `backend` after the
  snapshot refresh-control changes landed.
- `mvn -pl qkyd-health -am -DskipTests compile` passed in `backend` after the
  new context aggregator landed.
- `mvn -pl qkyd-ai -am -DskipTests compile` passed in `backend` after the real
  context integration landed.
- `mvn -pl qkyd-ai -am -DskipTests compile` passed in `backend` after the
  snapshot persistence changes landed.
- Focused documentation review of the new collaboration protocol against the
  current `P5` status, control files, and platform ownership boundaries.
- `mvn -pl qkyd-ai -am -DskipTests compile` passed in `backend`.
- `mvn -pl qkyd-admin -am -DskipTests package` passed in `backend`.
- `npm run build` passed in `qkyd-vue3-new`.

## Next item

1. First, land `P7-001` by turning the home workbench into a competition-grade
   risk command screen that links naturally into the new demo-only secondary
   pages.
2. Then curate three strong demo cases so the newly added subject, device, AI,
   and report pages each serve one clear judge-facing story.
3. Only after the guided demo route stabilizes, replace the highest-value demo
   surfaces with backend-backed data.

1. Land `P7-001` by turning the home workbench into a competition-grade risk
   command screen with high-risk counts, urgent objects, trend change, and
   pending-action framing.
2. After the home screen is readable in ten seconds, curate three strong demo
   cases that exercise the event, subject, and AI story end to end.
3. Then strengthen the subject center so the “continuous risk management”
   value is obvious without narration-heavy explanation.

## Blockers

- None for the current frontend submission slice.
- None for the current slice.

## Risks to watch

- The new secondary pages are intentionally demo-only and currently use mock
  data, so future contributors must not describe them as backend-complete
  features until the real contracts land.
- Snapshot reads now support stale detection, but the current policy only
  compares a few high-signal timestamps; later slices may still need richer
  invalidation rules if more dynamic health sources join the flow.
- Chronic-disease reasoning still falls back to heuristic interpretation
  because the current health-domain joins do not yet expose a reliable disease
  history source for event insight.
- The frontend still keeps a compatibility fallback for missing `trace` data;
  remove that inference path only after all target environments run the new
  backend contract.

## Handoff

- What changed in the latest slice: the frontend now includes a new set of
  secondary competition pages across subject, device, AI, and report centers,
  and those pages are reachable through the shared platform navigation. They
  intentionally use curated mock data, and the shared shell now labels them as
  demo-only so the team can submit and deploy the slice without overstating
  backend readiness.
- What remains after this slice: the home workbench still needs to become the
  strongest first screen, and the new secondary pages still need a guided
  end-to-end demo route plus selective backend integration for the
  highest-value stories.
- Exact next step for the next contributor: rebuild the home workbench so its
  key cards, actions, and jump paths drive directly into the strongest new demo
  pages in under ten seconds of judge attention.
- Avoid breaking in the next slice: keep the demo-only labels on mock-data
  pages until real contracts replace them, do not mix unrelated backend
  workspace changes into the frontend submission, and preserve the current
  grouped navigation because it is now the entry point for the P7 presentation
  flow.
- What changed: the workspace now has a standing subagent collaboration
  protocol that defines who owns the shared frontend layer, who owns the event
  insight contract, which agents may run in parallel, and how every multi-agent
  session must close with verification and handoff records. The coordinator
  document now points to this protocol instead of an outdated named-agent list.
  The backend also now persists and reuses the latest event insight snapshot
  through `qkyd-ai`, so repeated opens of the same event can return a stored
  structured result without rerunning the full orchestration flow. The event
  insight flow now also consumes a real `qkyd-health` context aggregator for
  age, latest location, report summary, history counts, and device signal
  status, while retaining heuristic fallback only where the health domain still
  lacks a trustworthy source. Snapshot reuse now also supports backend stale
  detection and an explicit `refresh=true` bypass path without changing the
  response DTO. The event-center AI insight card now also exposes a local
  operator-facing refresh action that triggers one-shot recomputation through
  that same backend path and reports refresh feedback in the sidebar. The clean
  reactor verification now also passes again from `backend`, and the dependency
  chain between `qkyd-health` and `qkyd-system` has been revalidated. The same
  event card now also shows a lightweight freshness note based on `generatedAt`
  so operators can tell whether the current insight is fresh, aging, or a
  fallback result without leaving the panel. It now also states clearly that
  the current experience only shows the latest insight snapshot and does not
  yet expose snapshot history. The event center now also has a read-only
  snapshot history dialog backed by a dedicated snapshot-summary API, so
  operators can inspect recent insight records without affecting the current
  latest-insight flow. The same history flow now also supports opening one
  historical snapshot as a read-only detail payload through a dedicated
  snapshot-detail endpoint, without changing the meaning of the current
  latest-insight API. The event-center history experience has now also been
  collapsed into one combined read-only viewer with inline preview, reducing
  dialog hopping while preserving the current backend API boundaries. Event
  insight payloads now also carry one backend-authored `freshness` object so
  latest and historical reads share the same freshness note and tone, while
  the frontend still falls back to `generatedAt`-based derivation for older
  snapshots and mixed-version environments. The snapshot history summary rows
  now also carry backend-authored freshness metadata and render a compact
  freshness badge directly in the combined viewer list so operators can scan
  historical relevance without opening each record one by one. The history list
  now also renders fallback provenance as its own secondary badge, so recency
  and source semantics are no longer conflated in one label.
- What remains: the platform base is credible, but the presentation layer still
  needs a much stronger first impression, clearer demo stories, and more
  visible action outcomes to score well in a competition setting.
- Exact next step: rebuild the home workbench so a judge can understand the
  product's usefulness in ten seconds without needing a presenter to translate
  the interface.
- Avoid breaking: keep `GET /ai/event/insight/{eventId}` stable, preserve the
  single-owner rule for shared platform files and event insight DTOs, let only
  the orchestrator update `upgrade/*.md`, and do not change snapshot payload
  semantics outside the `ai-contract-owner` boundary.
