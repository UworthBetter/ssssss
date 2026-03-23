# QKYD platform upgrade backlog

Use IDs for all meaningful work items. Keep items small enough to complete or
verify within one focused session.

## In progress

| ID | Phase | Item | Status | Exit criteria |
| --- | --- | --- | --- | --- |
| P7-001 | P7 | Rebuild the home workbench into a competition-grade risk command screen | in progress | Judges can understand the project value in 10 seconds through high-risk counts, urgent objects, trend changes, and pending actions without opening other pages |

## Todo

| ID | Phase | Item | Status | Exit criteria |
| --- | --- | --- | --- | --- |
| P7-002 | P7 | Curate three strong end-to-end demo cases across event, subject, and AI flows | todo | The workspace contains three presentation-ready cases that clearly show discovery, explanation, and action for high-value scenarios such as heart-rate anomaly, geofence breach, and SOS |
| P7-003 | P7 | Strengthen the subject center around continuous risk change instead of one-off detail display | todo | Judges can open one subject and immediately see seven-day risk trend, repeated anomalies, focus status, and why the subject is becoming more dangerous |
| P7-004 | P7 | Upgrade the AI workbench from a generic analysis surface to a decision and report surface | todo | The AI page outputs structured conclusion, evidence chain, linked entities, and action/report results that support the competition story |
| P7-005 | P7 | Add a judge-facing guided demo route across home, event, subject, and AI centers | todo | A presenter can complete one crisp three-minute demo without improvising navigation or relying on explanation-heavy detours |
| P7-006 | P7 | Replace weak sample content with presentation-grade domain data and narrative labels | todo | Core pages consistently surface strong examples, realistic anomalies, and business-language labels that make the system feel useful to judges on first contact |

## Done

| ID | Phase | Item | Status | Exit criteria |
| --- | --- | --- | --- | --- |
| P7-006a | P7 | Ship demo-only secondary pages for subject, device, AI, and report centers with curated mock narratives | done | Operators and judges can open the new secondary pages, see clear presentation-grade content, and understand that the pages are demo-only because the UI marks them as using mock data |
| P0-001 | P0 | Create and validate the `qkyd-platform-upgrade` skill | done | Skill folder, metadata, and validation all pass |
| P0-002 | P0 | Define the subagent collaboration protocol and ownership rules | done | Multi-agent upgrade work has one stable playbook for ownership, activation, handoff, and verification |
| P0-003 | P0 | Restore clean reactor compilation for `qkyd-health` and `qkyd-ai` verification | done | `mvn -pl qkyd-ai -am -DskipTests clean compile` passes from `backend` without relying on prebuilt module outputs |
| P1-001 | P1 | Define the top-level platform menu and route grouping | done | Routes are grouped by platform domain instead of flat pages |
| P1-002 | P1 | Define the shared workbench layout zones | done | At least one reusable layout shell exists for platform pages |
| P1-003 | P1 | Add placeholders for global search, notifications, and context filters | done | Platform shell exposes common entry points |
| P2-001 | P2 | Define the event entity states and detail workflow shell | done | Event center page shell and state model are present |
| P3-001 | P3 | Define the subject 360 information structure | done | Subject detail layout covers linked devices, events, and AI |
| P4-001 | P4 | Define the device operations information structure | done | Device page has operations-oriented fields and actions |
| P5-001 | P5 | Define the AI workbench structured output model | done | AI responses map to evidence, entities, and actions |
| P5-002 | P5 | Add the first multi-agent event insight contract between `qkyd-ai` and the event center | done | Operators can open an event and see a structured AI insight card backed by one stable backend endpoint |
| P5-003 | P5 | Persist event insight snapshots and trace metadata | done | Operators can reopen a prior insight without rerunning the full orchestration path |
| P5-004 | P5 | Replace heuristic context enrichment with real health and device joins | done | Event insight context comes from `qkyd-health` domain data instead of deterministic placeholders for the core fields |
| P5-007 | P5 | Add snapshot refresh or invalidation controls for stale event insight context | done | Operators or backend policy can trigger a fresh insight recomputation when persisted context is too stale for safe reuse |
| P5-008 | P5 | Add an operator-facing manual refresh entry for event insight | done | The event center can request `refresh=true` and deliberately bypass stale snapshots when the operator needs a fresh recomputation |
| P5-009 | P5 | Surface snapshot freshness guidance in the event-center AI card | done | Operators can see when the current insight was generated and get a lightweight refresh cue when the snapshot may be aging |
| P5-010 | P5 | Clarify latest-snapshot-only scope in the event-center AI card | done | Operators can see that the current event center only exposes the latest insight snapshot and does not yet offer snapshot history |
| P5-011 | P5 | Add a read-only snapshot history list for event insight | done | Operators can open a small history view for one event and review recent insight snapshots without disturbing the latest-insight flow |
| P5-012 | P5 | Add read-only historical snapshot detail viewing for event insight | done | Operators can open one historical snapshot detail payload without changing the meaning of the current latest-insight API |
| P5-013 | P5 | Merge snapshot history list and read-only detail into one combined viewer | done | Operators can browse recent snapshots and inspect one historical payload inline without leaving the current event context or changing latest-insight semantics |
| P5-014 | P5 | Add a backend-authored freshness state to event insight payloads | done | Latest and historical event insight payloads carry one backend freshness note and tone, while the event-center UI keeps generatedAt-based fallback for compatibility |
| P5-015 | P5 | Add snapshot-history freshness badges for event insight records | done | Operators can distinguish fresh, aging, and fallback historical snapshots at a glance in the combined history viewer without changing latest-insight or snapshot-detail semantics |
| P5-016 | P5 | Split fallback provenance from snapshot freshness into a dedicated history badge | done | Snapshot history shows freshness state and fallback-origin state as separate badges so operators can distinguish “stale” from “fallback-derived” without conflating recency and provenance |
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
