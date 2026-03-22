# P5-001 AI workbench structured output model

## Goal

Turn the current AI page from a demo-like chat surface into a first platform-style decision workbench.

## What changed

- Wrapped the AI page in the shared `PlatformPageShell`.
- Added platform-level `headerExtra`, `toolbar`, and `aside` zones.
- Introduced quick-task entry points for risk review, sleep trend analysis, abnormal explanation, and report generation.
- Added structured AI result cards inside the conversation flow.
- Added evidence, related-entity, and action sections to AI results.
- Added an aside panel that surfaces the latest structured result and suggested actions.
- Preserved the current chat flow, chart rendering, CSV export, and report export behavior.

## Result model used in this slice

Each AI result now maps into a local structured object with:

- `title`
- `summary`
- `riskLevel`
- `evidence[]`
- `entities[]`
- `recommendedActions[]`

## What remains for later phases

- Replace placeholder entity links with real route handoff into event, subject, and device centers.
- Replace local inference rules with backend-linked entity and action payloads.
- Move search, notification, and action dispatch into hardened shared platform services.

## Verification

- `npm run build` passed in `qkyd-vue3-new` after the AI workbench restructuring.