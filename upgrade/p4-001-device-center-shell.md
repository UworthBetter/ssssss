# P4-001 device center shell

This note captures the first device-center slice that upgrades the original
device management page into an operations-oriented device shell.

## Goal

Keep the existing device CRUD flow stable while establishing the first
platform-level device-operations structure.

## What this slice adds

- shared platform shell on the device page
- device summary cards
- online and offline status tags
- binding summary in the main list
- health score indicator
- last-upload placeholder field
- selected-device operations preview panel
- rule-status placeholder
- device timeline placeholder

## What remains out of scope

- real online metrics from backend telemetry
- real device-to-event linkage data
- real device-to-subject linkage data beyond current user binding
- rule management page or detail route

## Why this matters

This slice moves the device page from flat CRUD toward a true device-operations
center without introducing a risky new route system.
