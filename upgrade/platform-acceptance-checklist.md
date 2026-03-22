# Platform acceptance checklist

This document defines the first end-to-end acceptance round after the P6
platform hardening work. Use it as the working checklist for search,
notifications, and cross-center navigation.

## Acceptance summary

This round is intended to confirm that the upgraded frontend behaves like one
platform instead of a set of isolated pages. The current codebase already
passes build verification, and the checklist below separates what is already
backed by static verification from what still needs logged-in manual testing.

## Scope

This checklist covers three platform behaviors that now tie the product
experience together:

- global search
- backend-backed notifications
- cross-center navigation between subject, event, device, and AI centers

It also includes the shared route-query hydration behavior that keeps context
stable after navigation.

## Pre-checks

Run these checks before manual acceptance starts.

| ID | Check | Expected result | Evidence | Status |
| --- | --- | --- | --- | --- |
| PRE-001 | Production build | `npm run build` passes in `qkyd-vue3-new` | Static verification | Passed |
| PRE-002 | Platform routes exist | `/workbench/overview`, `/subject`, `/event`, `/device`, `/ai/workbench` all exist | Code review | Passed |
| PRE-003 | Shared platform services exist | Search, notification, and action dispatch are centralized | Code review | Passed |
| PRE-004 | Shared route-query sync exists | Subject, device, and event centers use one shared route-query sync helper | Code review | Passed |
| PRE-005 | Search canonical path exists | Frontend search prefers `/platform/search` and keeps a compatibility fallback | Code review | Passed |

## Search checklist

Use this section to validate that operators can find the right entity without
being forced into the wrong page.

| ID | Scenario | Steps | Expected result | Evidence | Status |
| --- | --- | --- | --- | --- | --- |
| SEARCH-001 | Search from subject center by name | Open **对象中心**. Trigger **全局搜索**. Enter a known subject name. | The result picker opens if there are several matches. The selected result opens the subject center with matching query context. | Manual, logged in | Pending |
| SEARCH-002 | Search from subject center by phone number | Open **对象中心**. Search by a known phone number. | The matching subject opens and the page applies the subject query context. | Manual, logged in | Pending |
| SEARCH-003 | Search from device center by IMEI | Open **设备中心**. Search by a known IMEI. | The matching device opens and the page applies the IMEI context. | Manual, logged in | Pending |
| SEARCH-004 | Search from event center by event type | Open **事件中心**. Search by a known exception type. | The matching event list opens and the event type filter is applied. | Manual, logged in | Pending |
| SEARCH-005 | Search from AI center by user ID | Open **AI 中心**. Search by a known numeric user ID. | Search returns one or more results. The selected result opens a valid target center. | Manual, logged in | Pending |
| SEARCH-006 | Multi-result selection | Use a keyword known to match several entities. | The selector prompts for a result number instead of auto-opening the first hit. | Manual, logged in | Pending |
| SEARCH-007 | No-result handling | Search for a keyword that should not exist. | The UI shows a clear no-result message and does not navigate. | Manual, logged in | Pending |
| SEARCH-008 | Canonical endpoint fallback | Run a normal search while logged in. If `/platform/search` is unavailable, verify search still returns aggregated results. | Search remains usable and does not break the operator flow. | Manual, logged in | Pending |
| SEARCH-009 | Search security | Try a keyword that contains HTML or script-like content. | The selector must not execute injected content. It should render the text safely or reject the input. | Manual, logged in | Pending |

## Notification checklist

Use this section to validate that notifications come from backend-backed data
adaptation instead of static examples and that they open the right destination.

| ID | Scenario | Steps | Expected result | Evidence | Status |
| --- | --- | --- | --- | --- | --- |
| NOTIFY-001 | Event center notifications load | Open **事件中心** while logged in. | The notification card shows live backend-backed items or one minimal fallback reminder. | Manual, logged in | Pending |
| NOTIFY-002 | Event notification click-through | Click one notification item in **事件中心**. | The app navigates to the event center target path and keeps the related query context. | Manual, logged in | Pending |
| NOTIFY-003 | Event notification “view all” | Click **查看全部通知** in **事件中心**. | The app stays in or returns to the event center root with a stable landing path. | Manual, logged in | Pending |
| NOTIFY-004 | AI center notifications load | Open **AI 中心** while logged in. | The notification card shows items based on recent abnormal data and current AI-related context. | Manual, logged in | Pending |
| NOTIFY-005 | AI notification click-through | Click one notification item in **AI 中心**. | The app navigates to a valid event, subject, or device target with usable query context. | Manual, logged in | Pending |
| NOTIFY-006 | Empty notification state | Use an account or context with no matching backend rows. | The notification card shows the designed empty or fallback reminder instead of broken static sample data. | Manual, logged in | Pending |
| NOTIFY-007 | Notification severity mapping | Compare `info`, `warning`, and `danger` items. | Badge color, copy, and business urgency stay aligned. | Manual, logged in | Pending |

## Cross-center navigation checklist

Use this section to validate the main platform loop between AI, events,
subjects, and devices.

| ID | Scenario | Steps | Expected result | Evidence | Status |
| --- | --- | --- | --- | --- | --- |
| LINK-001 | Event to subject | In **事件中心**, click **查看对象** from the detail panel. | The subject center opens and applies the route-derived query context. | Manual, logged in | Pending |
| LINK-002 | Event to device | In **事件中心**, click **查看设备** from the detail panel. | The device center opens and applies the route-derived query context. | Manual, logged in | Pending |
| LINK-003 | Subject to event | In **对象中心**, click **查看事件**. | The event center opens and applies the related user context. | Manual, logged in | Pending |
| LINK-004 | Subject to device | In **对象中心**, click **查看设备**. | The device center opens and applies the related user context. | Manual, logged in | Pending |
| LINK-005 | Device to subject | In **设备中心**, click **查看对象**. | The subject center opens with the related entity context when a bound subject exists. | Manual, logged in | Pending |
| LINK-006 | Device to rule entry | In **设备中心**, click **查看规则**. | The app lands in the device center with the rule-related query context and guidance message. | Manual | Pending |
| LINK-007 | AI action to event | In **AI 中心**, trigger an action such as **创建事件** or **下发巡检工单**. | The event center opens with the correct action-driven context. | Manual, logged in | Pending |
| LINK-008 | AI action to subject | In **AI 中心**, trigger **加入重点关注** or **查看对象**. | The subject center opens with the expected subject context. | Manual, logged in | Pending |
| LINK-009 | AI action to device | In **AI 中心**, trigger **调整设备监测频率** or **查看设备**. | The device center opens with the expected device context. | Manual, logged in | Pending |
| LINK-010 | Deep-link refresh | Open a subject, device, or event page with route query parameters, then refresh the page. | The shared route-query sync reapplies local filters and restores the most relevant selected row. | Manual, logged in | Pending |

## Static verification notes

The following points are already backed by code review or build evidence:

- Global search uses a shared entry point and a shared service.
- Multi-result search selection is implemented.
- Subject, device, and event centers share one route-query sync helper.
- Notifications in AI and event centers now come from a backend-backed adapter,
  not the old static example payloads.
- The frontend now prefers the canonical `/platform/search` path while keeping
  a compatibility fallback.

## Known risks

The checklist should explicitly watch these risks during manual acceptance.

- Cross-center routing still relies on query-based identity rather than stable
  entity join endpoints.
- The backend does not yet expose a dedicated platform notifications endpoint,
  so notifications still depend on an adapter layer.
- The canonical `/platform/search` endpoint is preferred, but unauthenticated
  probing still returns `401`, so logged-in validation remains mandatory.
- Search result markup currently uses HTML rendering in the selector flow, so
  XSS-focused testing is important.

## Acceptance result

Use this section after the manual round finishes.

- Result: Pending
- Reviewer:
- Date:
- Notes:
