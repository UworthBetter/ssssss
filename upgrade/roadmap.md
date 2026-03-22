# QKYD platform upgrade roadmap

This file is the staged execution plan for the qkyd upgrade. Use it to decide
what comes next and to prevent the project from drifting back into isolated
page work.

## Mission

Upgrade the current frontend from a set of feature pages into a platform
product with a stable information architecture, a complete operational loop,
and an AI workbench tied to real business actions.

## Phase map

| Phase | Goal | Key deliverables | Exit criteria |
| --- | --- | --- | --- |
| P0 | Lock direction and control files | `agent.md`, `upgrade/*.md`, shared skill | Team can resume work from files alone |
| P1 | Build platform shell | navigation model, route groups, workspace layout, global filters, notification entry | The app has platform-level structure instead of flat page navigation |
| P2 | Build event center loop | event list shell, detail pane, workflow states, SLA and ownership model | Abnormal handling is no longer only a simple list edit flow |
| P3 | Build subject 360 | subject list stratification, detail workspace, linked devices and events | Subject management becomes a true object center |
| P4 | Build device operations | device health view, binding state, rule visibility, operations slices | Device page supports operations, not only CRUD |
| P5 | Build AI decision workbench | structured AI outputs, evidence panel, action hooks, linked entities | AI results drive real actions and references |
| P6 | Harden platform services | search, cross-links, shared components, consistency cleanup, verification | Core product flow works as one platform |

## Current recommended sequence

1. Finish P0 control system.
2. Start P1 platform shell.
3. Start P2 event center shell.
4. Start P3 subject 360 shell.
5. Start P5 AI workbench linkage after entities and events stabilize.
6. Complete P4 and P6 hardening.

## P1 scope

P1 is the highest leverage phase. It defines the frame that every later page
must fit into.

Deliver these slices:

- top-level information architecture
- grouped route structure
- platform menu model
- workspace header with context state
- placeholders for global search and notification center
- shared layout zones for list-detail and workbench pages

## P2 scope

P2 must turn exception handling into an operational workflow.

Deliver these slices:

- event center page shell
- event list filters and saved views
- detail panel or right-side inspector
- event status model
- owner and SLA placeholders
- timeline area
- links to subject and device

## P3 scope

P3 must turn subject management into a true subject center.

Deliver these slices:

- segmented subject list
- risk and recent activity columns
- subject 360 layout
- linked device summary
- linked event summary
- trend and intervention placeholders
- clear jump paths from event to subject and back

## P4 scope

P4 must make device operations visible and actionable.

Deliver these slices:

- operations-oriented list fields
- device health indicators
- binding visibility
- upload recency indicators
- rule and threshold entry points
- device-to-subject and device-to-event navigation

## P5 scope

P5 must turn AI into a decision tool instead of a chat demo.

Deliver these slices:

- structured answer layout
- evidence section
- entity references
- action buttons or placeholders
- report and event creation hooks
- reusable AI result blocks

## P6 scope

P6 ties the platform together and removes drift.

Deliver these slices:

- shared components and visual consistency cleanup
- global search behavior
- notification and reminder model
- route and entity cross-link cleanup
- verification passes
- backlog burn-down and acceptance review

## Completion test

The upgrade is complete only when all of the following are true:

- The app uses a platform information architecture.
- Events, subjects, devices, and AI outputs are linked.
- The main business loop is visible and actionable.
- Work can resume from the tracking files without missing context.
