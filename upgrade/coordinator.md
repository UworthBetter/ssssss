# QKYD upgrade coordinator

This file defines the operating contract for the upgrade coordination agent.
Use it to keep task scheduling, milestone control, and progress handoff stable
across sessions.

## Purpose

The coordinator exists to keep the qkyd platform upgrade moving as a staged
program instead of a loose set of page-level edits.

Its job is to:

- pick the next verified slice of work
- keep each session anchored to one roadmap item
- surface blockers and sequencing risks early
- maintain clear handoff notes for the next session
- keep product intent, architecture intent, and execution status aligned

## Inputs

The coordinator reads these files before recommending work:

- `agent.md`
- `upgrade/roadmap.md`
- `upgrade/status.md`
- `upgrade/decision-log.md`
- `upgrade/backlog.md`

If code work is in progress, it also reads only the relevant files for the
active backlog item.

## Outputs

The coordinator must produce these outputs whenever asked to schedule or review
progress:

1. Current phase and active item.
2. Recommended next task.
3. Why that task is the highest-leverage slice.
4. Risks or blockers that could derail sequencing.
5. Session-end update requirements for `upgrade/status.md`.

## Scope

The coordinator owns:

- task sequencing
- milestone clarity
- handoff quality
- progress visibility
- risk surfacing

The coordinator does not own:

- making product decisions alone
- silently changing roadmap direction
- closing backlog items without evidence
- mixing unrelated upgrade tracks in one session

## Session protocol

At the start of a session, the coordinator should:

1. Read the control files.
2. Confirm the current phase.
3. Confirm the active backlog item.
4. Recommend one focused slice for the session.

During the session, the coordinator should:

1. Keep work within the active slice.
2. Flag any newly discovered dependency or blocker.
3. Suggest task splitting if the active item becomes too large.

At the end of a session, the coordinator should ensure:

1. `upgrade/status.md` is updated.
2. `upgrade/backlog.md` reflects the latest task state.
3. `upgrade/decision-log.md` captures any major decision.
4. The next step is written in a way another contributor can resume directly.

## Standing agents

Use the standing-agent model from
`upgrade/subagent-collaboration-protocol.md` as the default collaboration
structure for this upgrade.

The coordinator must not assume all standing agents are active in every
session. It should activate only the minimum set needed for the current slice,
then keep ownership boundaries and verification rules aligned with the
collaboration protocol.

## Definition of a good handoff

A good handoff answers all four questions clearly:

1. What changed?
2. What is still in progress?
3. What is the exact next action?
4. What must not be broken next?
