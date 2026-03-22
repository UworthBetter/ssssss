# QKYD subagent collaboration protocol

This document defines the default collaboration rules for multi-agent upgrade
work in this repository. Use it to keep subagent work stable, small in scope,
and easy to verify across long-running platform-upgrade sessions.

## Purpose

The upgrade now spans shared platform shell work, backend AI contracts,
health-domain joins, and multiple business centers. Without a clear protocol,
subagents will drift into the same files, redefine the same contracts, or move
faster than the control files can track.

This protocol exists to:

- keep one active slice of work visible at a time
- assign one owner per shared boundary
- let business slices progress in parallel only when their write sets do not
  overlap
- preserve a clear handoff trail in `upgrade/`
- keep verification and quality gates consistent

## Collaboration model

Use one control agent, one shared-boundary agent, and a small set of execution
agents. Do not split work by page alone. Split it by ownership boundary and
upgrade risk.

The default collaboration model is:

1. `upgrade-orchestrator`
2. `platform-governor`
3. `ai-contract-owner`
4. `insight-persistence-agent`
5. `health-context-agent`
6. `event-owner`
7. `subject-owner`
8. `device-owner`
9. `ai-workbench-owner`
10. `workbench-owner`

Do not activate all agents in every session. Activate only the minimum set
needed for the current slice.

## Agent roles and ownership

Each standing agent owns a narrow area. If a task crosses one of these
boundaries, the orchestrator must split it before implementation starts.

### `upgrade-orchestrator`

The orchestrator owns session control and progress records. It does not own
business implementation.

Ownership:

- `agent.md`
- `upgrade/roadmap.md`
- `upgrade/status.md`
- `upgrade/decision-log.md`
- `upgrade/backlog.md`

Responsibilities:

- pick one active slice for the session
- freeze the task boundary before code work starts
- decide which agents can run in parallel
- collect verification evidence
- update the control files before the session ends

### `platform-governor`

The platform governor owns shared frontend behavior. Business-page agents may
consume this layer, but they must not redefine it.

Ownership:

- `qkyd-vue3-new/src/components/platform/**`
- `qkyd-vue3-new/src/router/**`
- `qkyd-vue3-new/src/layout/**`
- `qkyd-vue3-new/src/composables/useRouteQueryListSync.ts`
- `qkyd-vue3-new/src/api/platformSearch.ts`
- `qkyd-vue3-new/src/api/platformNotifications.ts`

Responsibilities:

- platform shell structure
- shared route and query conventions
- notification and search entry behavior
- cross-center action-dispatch contracts
- shared platform types and service helpers

### `ai-contract-owner`

The AI contract owner owns the stable operator-facing event insight contract.
This agent is the only agent that may change the external shape or semantics of
the event insight response.

Ownership:

- `backend/qkyd-ai/src/main/java/com/qkyd/ai/controller/EventInsightController.java`
- `backend/qkyd-ai/src/main/java/com/qkyd/ai/service/IEventInsightService.java`
- `backend/qkyd-ai/src/main/java/com/qkyd/ai/model/dto/EventInsightResultDTO.java`
- `backend/qkyd-ai/src/main/java/com/qkyd/ai/service/impl/EventInsightServiceImpl.java`
- `qkyd-vue3-new/src/api/ai.ts`

Responsibilities:

- event insight response contract
- compatibility rules for new fields
- `trace` semantics
- frontend consumption rules for event insight fields

### `insight-persistence-agent`

The insight persistence agent owns storage and retrieval for event insight
snapshots. This agent improves traceability and replay behavior without
changing the public contract on its own.

Ownership:

- persistence additions in `backend/qkyd-ai/**`
- snapshot-related SQL in `backend/sql/**`

Responsibilities:

- event insight snapshot schema
- snapshot read and write flow
- cache or persistence reuse strategy
- trace snapshot storage strategy

### `health-context-agent`

The health context agent owns the replacement of heuristic context enrichment
with true health-domain joins and queries.

Ownership:

- `backend/qkyd-health/src/main/java/com/qkyd/health/service/**`
- `backend/qkyd-health/src/main/java/com/qkyd/health/mapper/**`
- `backend/qkyd-health/src/main/resources/**`

Responsibilities:

- subject, device, and history joins
- device-state lookup for event insight
- health-context query hardening
- fallback reduction behind the same contract

### `event-owner`

The event owner owns the event-center operator experience only.

Ownership:

- `qkyd-vue3-new/src/views/ExceptionAlertView.vue`
- event-specific use of `qkyd-vue3-new/src/api/ai.ts`
- event-specific use of `qkyd-vue3-new/src/api/health.ts`

Responsibilities:

- event-center layout and workflow
- event insight card rendering
- event detail, SLA, and ownership interactions
- event-to-subject and event-to-device jumps

### `subject-owner`

The subject owner owns the subject-center experience only.

Ownership:

- `qkyd-vue3-new/src/views/HealthSubjectView.vue`

Responsibilities:

- subject list and stratification
- subject 360 preview and detail flow
- subject tags and linked-entity views

### `device-owner`

The device owner owns the device-center experience only.

Ownership:

- `qkyd-vue3-new/src/views/DeviceInfoView.vue`

Responsibilities:

- device operations view
- health and binding visibility
- rule-entry and telemetry summary behavior

### `ai-workbench-owner`

The AI workbench owner owns the AI center experience only.

Ownership:

- `qkyd-vue3-new/src/views/AiWorkbenchView.vue`
- AI-workbench-specific use of `qkyd-vue3-new/src/api/ai.ts`

Responsibilities:

- analysis task entry flow
- structured result rendering
- evidence and action presentation
- operator follow-up flow from the AI center

### `workbench-owner`

The workbench owner owns the home and overview experience only. This role is
deliberately late-stage because the home page still has a different technical
shape from the other centers.

Ownership:

- `qkyd-vue3-new/src/views/DashboardView.vue`
- overview-specific use of `qkyd-vue3-new/src/api/index.ts`

Responsibilities:

- role-aware workbench entry
- algorithm-first overview presentation
- lightweight AI summary presentation

## Activation rules

Use the smallest agent set that can complete the active slice. More agents do
not automatically improve throughput.

As of March 22, 2026, the recommended active set is:

1. `upgrade-orchestrator`
2. `ai-contract-owner`
3. `insight-persistence-agent`
4. `health-context-agent`
5. `event-owner`

Keep `subject-owner`, `device-owner`, `ai-workbench-owner`, and
`workbench-owner` inactive until the event insight persistence and context
hardening slices are stable.

## Non-overlap rules

The fastest way to destabilize the upgrade is to let multiple agents edit the
same shared file or silently redefine the same contract. Treat the following
rules as mandatory.

- Only `upgrade-orchestrator` may edit files in `upgrade/` or `agent.md`.
- Only `platform-governor` may edit shared platform shell files.
- Only `ai-contract-owner` may change the external event insight DTO shape or
  field semantics.
- Execution agents may not expand their scope into shared layers just because a
  local change seems convenient.
- If a task touches both a business page and a shared platform file, split it
  into two tasks with two owners.

Do not run these combinations in parallel:

- `platform-governor` with any business agent on the same `Platform*` file
- `ai-contract-owner` with any other agent on
  `EventInsightResultDTO.java`
- `ai-contract-owner` with any other agent on
  `EventInsightServiceImpl.java`
- multiple business agents on
  `qkyd-vue3-new/src/composables/useRouteQueryListSync.ts`
- `event-owner` and `ai-workbench-owner` when both intend to redefine the AI
  result shape

## Session workflow

Every multi-agent session must follow the same order. This keeps long-running
work resumable and avoids hidden assumptions between agents.

1. Read `upgrade/status.md` and confirm the current phase.
2. Read `upgrade/decision-log.md` and check for recent constraints.
3. Choose one active slice with a clear exit condition.
4. Freeze ownership, write boundaries, and the verification target.
5. Lock any public contract before parallel implementation begins.
6. Activate only the agents needed for that slice.
7. Verify the slice with build, test, or focused review evidence.
8. Update `upgrade/status.md`, `upgrade/backlog.md`, and
   `upgrade/decision-log.md`.

## Quality gates

An agent task is not complete until it passes both scope and verification
gates.

Scope gates:

- the task stays inside the assigned ownership boundary
- the task advances one active slice only
- the task does not silently change a shared contract
- the task leaves a clear handoff artifact

Verification gates:

- code builds or the agent records why build verification was not possible
- frontend changes include a focused behavior check
- backend changes include contract or compile validation
- document-only changes update the related control files

## Handoff format

Each execution agent must close with a short handoff that the orchestrator can
copy into session records.

The handoff must answer:

1. What changed?
2. What remains?
3. What is the exact next step?
4. What files or contracts must not be broken?

## Prompt requirements

Prompts for execution agents must define the boundary before the task. A valid
prompt must include all of the following parts:

- the active slice ID or exact goal
- the owned files or directories
- the files the agent must not edit
- the expected verification evidence
- the required handoff format

If a prompt does not define these items, the orchestrator must tighten the task
before starting work.

## Next steps

Use this protocol together with `upgrade/coordinator.md` and the current
session status in `upgrade/status.md`. When the active P5 slices are complete,
update this protocol again before expanding the standing agent set to the
subject, device, AI workbench, and home-page tracks.
