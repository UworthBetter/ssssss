# P2-001 event center shell

This note captures the first event-center-specific shell that upgrades the
exception page from a management list into a workflow-oriented event surface.

## Goal

Turn the current exception page into the first real event-center shell without
changing backend interfaces yet.

## What this slice adds

- event-stage tags in the main table
- summary cards for triage, in-progress, and closed counts
- a selected-event detail inspector in the right-side panel
- workflow stage, owner, and SLA placeholders
- a simple timeline block
- object and device jump placeholders

## What remains out of scope

- true backend event entity support
- persistent event owners and SLA policies
- real event timeline history
- saved views and advanced filtering
- subject and device route jump execution

## Why this matters

This slice creates the first operational shell for `P2`, so future work can
replace placeholders with real event-state data instead of redesigning the page
again.
