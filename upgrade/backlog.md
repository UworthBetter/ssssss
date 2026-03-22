# QKYD platform upgrade backlog

Use IDs for all meaningful work items. Keep items small enough to complete or
verify within one focused session.

## In progress

| ID | Phase | Item | Status | Exit criteria |
| --- | --- | --- | --- | --- |

## Todo

| ID | Phase | Item | Status | Exit criteria |
| --- | --- | --- | --- | --- |
| P5-003 | P5 | Persist event insight snapshots and trace metadata | todo | Operators can reopen a prior insight without rerunning the full orchestration path |
| P5-004 | P5 | Replace heuristic context enrichment with real health and device joins | todo | Event insight context comes from `qkyd-health` domain data instead of deterministic placeholders for the core fields |

## Done

| ID | Phase | Item | Status | Exit criteria |
| --- | --- | --- | --- | --- |
| P0-001 | P0 | Create and validate the `qkyd-platform-upgrade` skill | done | Skill folder, metadata, and validation all pass |
| P1-001 | P1 | Define the top-level platform menu and route grouping | done | Routes are grouped by platform domain instead of flat pages |
| P1-002 | P1 | Define the shared workbench layout zones | done | At least one reusable layout shell exists for platform pages |
| P1-003 | P1 | Add placeholders for global search, notifications, and context filters | done | Platform shell exposes common entry points |
| P2-001 | P2 | Define the event entity states and detail workflow shell | done | Event center page shell and state model are present |
| P3-001 | P3 | Define the subject 360 information structure | done | Subject detail layout covers linked devices, events, and AI |
| P4-001 | P4 | Define the device operations information structure | done | Device page has operations-oriented fields and actions |
| P5-001 | P5 | Define the AI workbench structured output model | done | AI responses map to evidence, entities, and actions |
| P5-002 | P5 | Add the first multi-agent event insight contract between `qkyd-ai` and the event center | done | Operators can open an event and see a structured AI insight card backed by one stable backend endpoint |
| P5-005 | P5 | Visualize the multi-agent handling flow inside the event-center AI card | done | Operators can see parser, context, risk, and advice stages with progress and fallback signals without leaving the event center |
| P5-006 | P5 | Replace frontend-inferred agent step states with backend trace fields | done | The event-center process visualization renders step status from a canonical backend trace contract instead of heuristics |
| P6-001 | P6 | Harden cross-center route handoff between AI, subject, event, and device centers | done | AI entity and action handoff uses real navigation and target centers hydrate route context |
| P6-002 | P6 | Stabilize shared search, notification, and action handoff behaviors | done | Shared platform services replace page-local placeholder logic across centers |
| P6-003 | P6 | Replace heuristic global search routing with a backend-driven search contract | done | Platform search queries real backend-backed entity lists and routes by ranked results instead of keyword heuristics |
| P6-004 | P6 | Replace static notification feeds with a backend-driven platform notification contract | done | Shared notification adapter loads backend-backed reminders and routes them with stable targets |
| P6-005 | P6 | Add a multi-result selector for backend-driven global search and define the dedicated backend search API contract | done | Operators choose among ambiguous search hits and the workspace contains a concrete backend endpoint proposal |
| P6-006 | P6 | Reduce repeated route-query hydration and continue hardening shared platform utilities | done | Shared utility coverage grows and repeated route hydration or platform glue code shrinks across centers |
| P6-007 | P6 | Swap the aggregated search adapter to a dedicated backend platform search endpoint | done | Frontend prefers one canonical `/platform/search` contract instead of orchestrating multiple list APIs |

## Blocked

| ID | Phase | Item | Status | Exit criteria |
| --- | --- | --- | --- | --- |
