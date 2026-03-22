# Multi-agent event insight design

This document defines the first production-oriented design for turning a raw
exception into a structured event insight card. The goal is to connect the
current event center, health data services, and AI layer into one controlled
workflow instead of a loose set of page-level calls.

## Goal

The system must accept one abnormal event, enrich it with subject and device
context, estimate risk, and return a concise recommendation card for the
administrator.

The design is aligned to the current qkyd modules:

- `qkyd-health` owns exception, subject, device, trend, and alert data.
- `qkyd-ai` owns the orchestration API and the insight result model.
- `qkyd-vue3-new` renders the event center card and operator actions.

## Agent roles

Each agent has one job and one output shape. The team must not let agents
blend responsibilities, because that makes results harder to audit and retry.

- `Agent 1 - event parser`: extracts `abnormalType`, `occurredAt`,
  `subjectId`, `deviceId`, `metricName`, and `metricValue` from the raw event.
- `Agent 2 - context enricher`: collects age, chronic conditions, recent trend,
  historical abnormal count, and current device state.
- `Agent 3 - risk assessor`: returns `riskLevel`, `possibleCauses`, and
  `requiresImmediateAction`.
- `Agent 4 - action recommender`: returns `notifyWho`, `suggestedActions`,
  `offlineCheck`, `contactFamily`, and `contactInstitution`.

The orchestrator must merge these outputs into one final insight payload and
preserve the intermediate results for tracing.

## Orchestration flow

The runtime flow must be deterministic enough to retry and explain, even if one
agent uses LLM-based reasoning.

1. Receive an event identifier or a raw exception payload from the event
   center.
2. Load the canonical event record from `qkyd-health`.
3. Run `Agent 1` to normalize the event into structured fields.
4. Run `Agent 2` in parallel against subject, trend, device, and history data
   sources.
5. Feed the parser output and enriched context into `Agent 3`.
6. Feed the risk result into `Agent 4`.
7. Merge all outputs into one response and persist the insight snapshot.

The orchestrator must return a single response even when some context sources
are missing. In that case, it must mark the missing fields and lower the
confidence score.

## Data contract

The insight card must use one stable response contract so the frontend does not
need to guess which fields exist.

```json
{
  "eventId": 4001,
  "summary": "Heart rate anomaly, 132 bpm",
  "parsedEvent": {
    "abnormalType": "HEART_RATE_HIGH",
    "occurredAt": "2026-03-22T18:42:00+08:00",
    "subjectId": 10001,
    "deviceId": 3001,
    "metricName": "heartRate",
    "metricValue": "132"
  },
  "context": {
    "age": 82,
    "chronicDiseases": ["hypertension", "coronary disease"],
    "recentTrend": "upward over the last 3 days",
    "historyCount7d": 4,
    "deviceOnline": true
  },
  "risk": {
    "level": "HIGH",
    "possibleCauses": ["sustained tachycardia", "post-activity spike"],
    "requiresImmediateAction": true,
    "confidence": 0.86
  },
  "advice": {
    "notifyWho": ["duty nurse", "family"],
    "suggestedActions": ["call back within 5 minutes", "recheck heart rate and SpO2"],
    "offlineCheck": true,
    "contactFamily": true,
    "contactInstitution": false
  },
  "trace": {
    "agentVersions": {
      "parser": "v1",
      "context": "v1",
      "risk": "v1",
      "advice": "v1"
    },
    "missingFields": [],
    "generatedAt": "2026-03-22T18:45:00+08:00"
  }
}
```

The frontend must read from this contract only. If a field is absent, the UI
must render a neutral fallback instead of failing.

## Fallback strategy

The first release must remain useful even when some services are missing or a
model call fails.

- If `Agent 2` cannot load subject history, keep the parsed event and device
  state, then downgrade confidence.
- If `Agent 3` times out, fall back to a rule-based risk profile derived from
  event type and threshold severity.
- If `Agent 4` fails, return the risk result plus a minimal operator action
  set, such as "review manually" and "notify duty staff".
- If the upstream event record is incomplete, return a partial insight instead
  of a hard error.

The orchestrator must record which fallback path it used so operators can see
why the response was partial.

## Risk controls

The insight flow touches sensitive health data, so the design must keep the
operating rules explicit.

- Use deterministic rules for hard-stop cases such as device offline, extreme
  thresholds, or repeated high-severity events.
- Minimize sensitive data sent to the LLM. Pass only the fields needed for the
  current step.
- Log the input event, the merged output, the fallback path, and the final
  decision summary.
- Enforce request timeouts and concurrency limits so one slow event cannot
  block the queue.
- Keep the output schema stable and versioned so changes do not break the event
  center card.
- Separate recommendation from execution. The insight card may suggest actions,
  but it must not auto-trigger irreversible operations.

## Phased rollout

The implementation should land in small slices so the team can validate each
step against the current repo.

### Phase 1: Backend contract

Add the insight DTOs and one orchestrator endpoint in `qkyd-ai`. Start with a
single synchronous request path and use current `qkyd-health` data sources and
fallback logic where possible.

### Phase 2: Event center card

Add an event insight card in the `ExceptionAlertView` workflow panel. Show the
abnormal summary, risk level, analysis reasons, and suggested actions.

### Phase 3: Persistence and traceability

Store the merged insight result in a snapshot table or record so operators can
reopen a past decision without rerunning the full chain.

### Phase 4: Hardening

Add timeout handling, cache reuse, clearer reason codes, and stronger audit
fields. At this point the orchestrator can also split more work into async
subtasks if the synchronous path becomes too slow.

## Ownership map

This feature needs clear module ownership so future changes stay small.

- `qkyd-health`: source of truth for exception records, subject profiles,
  device status, and recent health trend data.
- `qkyd-ai`: orchestration API, agent adapters, merged insight DTOs, and
  persistence hooks.
- `qkyd-vue3-new`: event center card, loading and empty states, and follow-up
  navigation into subject or device detail.

## Next steps

1. Implement the backend insight DTO and orchestration endpoint in `qkyd-ai`.
2. Wire the event center to the new endpoint and render the operator card.
3. Add insight persistence and trace fields once the first synchronous path is
   stable.
