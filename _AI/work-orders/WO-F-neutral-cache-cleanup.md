# WO-F — neutral cache cleanup (NEXT.md Stage F intro, not #5)

## Goal

Two behaviour-neutral cleanups from the `DOCS/SELECT-CACHES.md` inventory.
The per-frame query service (#5) is design work and explicitly out of scope.

## Non-goals

- Do NOT touch the TTLs above one frame (30, 31, 53, 73, 91, 293) - those
  need game runs to re-derive.
- Do NOT start the query service (#5) or delete caches (#6).
- No behaviour changes. If the suite disagrees, the inventory's
  "behaviour-neutral" claim is wrong for that key, not the suite.

## Prerequisites

- Read `DOCS/SELECT-CACHES.md` (the 46-entry inventory) and the top of
  `src/atlantis/units/select/Select.java` (`microCacheForFrames`,
  `clearCache()`, `cacheObject`).
- Baseline: full suite + ArchUnit numbers, written down.

## Part A — `microCacheForFrames` (one commit)

1. `microCacheForFrames` is `protected static int = 1`, written nowhere else,
   used as the TTL by 24 cache keys. TTL `1` and TTL `0` express the same
   one-frame intent, spelled twice.
2. Change the declaration to `= 0`. Nothing else.
3. `bash scripts/run-tests.sh --select-package tests` - must be green with
   baseline counts. (Cache behaviour is time-sensitive: the *full package*,
   not a single class, is the check.)
4. `bash scripts/run-architecture-tests.sh` - must be 7/7 (no production
   graph change is expected).
5. Commit: one line + results. Follow-up (same commit or next): replace the
   24 usages with the literal `0` and delete the field, suite green again.
   If any step is red: revert the whole edit and report (do not bisect by
   "fixing" expectations, do not weaken assertions).

## Part B — `clearCache()` skips `cacheObject` (one commit, analysis first)

1. `Select.clearCache()` clears four caches but not `cacheObject`, so
   `mainOrAnyBuildingPosition` (see `Select.java:747`) lives purely on its
   73-frame TTL. Decide from the code: is any caller relying on that value
   surviving `clearCache()`? Grep the callers of `mainOrAnyBuildingPosition`
   and of `clearCache()`, and say which in the commit message.
2. If nothing relies on it: add `cacheObject.clear()` to `clearCache()`.
   Clearing more can only force recomputation, never serve stale data - but
   the suite is still the judge.
3. Full suite + ArchUnit, baseline counts.
4. Commit with the caller analysis (one or two sentences) and the results.
   If red: revert, keep the analysis in the commit message anyway, report.

## Verification

- Suite green with baseline counts after each part; ArchUnit 7/7.
- `git diff` per part shows only what the part describes.

## Stop rules

- Any red after a revert-clean edit: stop and report, do not adjust TTLs,
  thresholds, or expectations to force green.
- Part B callers show intentional reliance on cross-clear persistence:
  do not implement, write the finding into the commit (or NEXT.md Stage F
  intro) and stop.
