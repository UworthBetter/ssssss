# P6-001 cross-center handoff

This slice hardens the first real business handoff between the AI workbench and
three business centers.

## Goal

The goal is to replace placeholder toasts with real route handoff so AI output
can send a user into the correct business context.

## What changed

- Rebuilt `AiWorkbenchView.vue` with clean Chinese product copy and preserved
  the structured workbench layout.
- Added real route handoff from AI entities and actions into:
  - `/subject`
  - `/event`
  - `/device`
- Rebuilt the subject, event, and device center views into clean UTF-8 Vue SFCs
  after historical encoding noise started breaking template parsing.
- Added route-query-driven filter hydration in the subject, event, and device
  centers so a cross-center jump arrives with useful context already applied.
- Preserved existing backend APIs for subject, device, and exception data.

## Handoff behavior now in place

- AI entity chips now navigate to the matching center instead of showing a
  placeholder toast.
- AI action buttons now dispatch to real centers when the action implies a
  follow-up business flow.
- Subject center accepts object-oriented query context and can jump onward to
  the device center and event center.
- Device center accepts device or user context and can jump to the subject
  center.
- Event center accepts event type, user, and device context and can jump to the
  subject center or device center.

## Verification

- `npm run build` passed in `qkyd-vue3-new` on March 22, 2026.

## Follow-up

- Move from route-query handoff to stronger entity identity handoff when backend
  APIs expose richer cross-entity lookup.
- Harden platform search and notification into shared services instead of page
  local placeholders.
- Review AI and platform bundle size during the next hardening pass.
