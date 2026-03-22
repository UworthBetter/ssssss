# P1-001 platform menu and route grouping

This note records the first platform-shell decision that moved the qkyd
frontend away from a flat page collection.

## Goal

Define a stable top-level platform structure without rewriting the existing
five feature pages yet. This slice keeps the current pages alive, but changes
the product frame around them.

## Top-level platform menu

The platform now uses five business centers:

- `工作台`
- `对象中心`
- `事件中心`
- `设备中心`
- `AI 中心`

## Canonical routes

The first grouped route set is:

- `/workbench/overview`
- `/subject`
- `/event`
- `/device`
- `/ai/workbench`

Legacy route aliases are preserved temporarily:

- `/dashboard` -> `/workbench/overview`
- `/exception` -> `/event`
- `/ai-workbench` -> `/ai/workbench`

## Current page mapping

The current view components map into the platform shell like this:

| Existing view | Platform group | Current route |
| --- | --- | --- |
| `DashboardView.vue` | `工作台` | `/workbench/overview` |
| `HealthSubjectView.vue` | `对象中心` | `/subject` |
| `ExceptionAlertView.vue` | `事件中心` | `/event` |
| `DeviceInfoView.vue` | `设备中心` | `/device` |
| `AiWorkbenchView.vue` | `AI 中心` | `/ai/workbench` |

## What changed in this slice

- Replaced the flat menu with grouped business-center navigation.
- Replaced the flat route entry model with grouped canonical paths.
- Updated breadcrumb behavior to reflect platform group plus page title.
- Preserved old route compatibility through redirect aliases.

## What did not change yet

- No detail pages were introduced.
- No APIs were changed.
- No list page was redesigned internally.
- No global search, notification, or context filters were implemented yet.

## Why this matters

This slice establishes the product shell that later work will build on:

- `P1-002` can now add a shared platform page shell.
- `P2` can grow `/event` into a real event center.
- `P3` can grow `/subject` into a subject 360 workspace.
- `P5` can grow `/ai/workbench` into a decision workbench tied to entities.
