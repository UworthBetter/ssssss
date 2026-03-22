# Platform search API contract proposal

This document defines the dedicated backend search API that should replace the
current frontend aggregation layer.

## Purpose

The frontend now aggregates existing subject, device, and event list APIs to
provide real search results. That works for the current slice, but the platform
needs one canonical backend search endpoint so ranking, identity, and access
rules live in one place.

## Proposed endpoint

- Method: `GET`
- Path: `/platform/search`

## Request parameters

- `keyword`: required string
- `center`: optional enum
  - `workbench`
  - `subject`
  - `event`
  - `device`
  - `ai`
- `limit`: optional integer, default `10`
- `types`: optional comma-separated list
  - `subject`
  - `device`
  - `event`

## Response shape

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "keyword": "张建国",
    "results": [
      {
        "id": "subject:1024",
        "kind": "subject",
        "title": "张建国",
        "subtitle": "对象中心 · 对象账号 ZJG1024",
        "description": "手机号 13800000000",
        "score": 100,
        "matchedFields": ["nickName", "phonenumber"],
        "target": {
          "path": "/subject",
          "query": {
            "subjectId": "1024"
          }
        },
        "raw": {
          "subjectId": 1024,
          "subjectName": "ZJG1024"
        }
      }
    ]
  }
}
```

## Required backend guarantees

The backend endpoint must guarantee the following behaviors:

1. It returns one unified ordered result set across subjects, devices, and
   events.
2. It includes stable identity fields, not only display names.
3. It returns backend-owned ranking, so the frontend does not guess which item
   is best.
4. It returns route-ready `target` data that the platform shell can consume
   directly.
5. It respects permissions so operators only see entities they can open.

## Migration plan

Move to the dedicated endpoint in these steps:

1. Keep the current frontend aggregation layer as a compatibility fallback.
2. Add a feature flag or detection layer in `platformSearch.ts`.
3. Prefer `/platform/search` when the backend exposes it.
4. Remove the aggregation layer after the dedicated endpoint is stable in
   production.
