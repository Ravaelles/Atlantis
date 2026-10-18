# WO-145 — site-finding chain to catalogue-only (NEXT.md #145, part: the spec)

## Goal

Every construction sited by the catalogue through the adapter funnel
(`CatalogueSiteAdapter`: exact honored verbatim, power + refusal twins).
Then the legacy chain moves to `src-old/`. Exit per NEXT #145: every
construction sited by the catalogue, the chain in src-old/.

## Non-goals (do not touch)

- The request door (`NewConstructionRequest.requestConstructionOf`:
  creates the `Construction`, pulls Pylons, refuses loudly). Only the
  "which tile" question moves; creation/validation/refusal stay.
- Power + physical validation inside `propose()` (`CanPhysicallyBuildHere`
  + `IsPoweredByAPylon`). Subsuming them is a later slice with its own
  A/B proof (measured 2026-10-18: skipping them loops 236 requests vs
  15 on the control).
- Non-Protoss siting (D8, Protoss-only) and the BO ownership timing.
- `V2` scheduler consumption of the same `PlacementPlanner` seam (works
  today, untouched).

## Prerequisites

- Read `FindPosition.findForBuilding` (kind dispatch: gas / base /
  depot / combat / standard), `APositionFinder.findStandardPosition`
  (funnel entry, then legacy ring fallback),
  `RequestBuildingNear` (sited entry, then legacy fallback),
  `CatalogueSiteAdapter` (flag gate, `SiteScope`, propose + validate +
  twin, null-means-legacy contract), `DefineExactPositionForNewConstruction`.
- Baseline: full suite + ArchUnit numbers, written down.

## The map (what goes where)

| Today | After |
|---|---|
| `FindPosition` kind dispatch | stays as the dispatcher until every arm is catalogue-served, then deleted whole |
| `APositionFinder` ring search (+ cache) | deleted; funnel is the only `siteFor` |
| `DefineExactPositionForNewConstruction` | deleted; exactness is `constraintFor` (exact/neighbourhood/anywhere, pinned) |
| Gas (`GasBuildingPositionFinder`, exact geyser) | catalogue exact-tile arm OR justified dedicated finder (exactness is proven either way, no silent third path) |
| Bases (`FindPositionForBase`, expansion siting) | LAST, blocked on C14 (04_MAPLIB): no real expansion siting exists yet (`#150` Nexus-2 main-base-footprint loop). Until C14 lands, bases keep the dedicated finder loudly, never via a faked catalogue answer |
| Depots / combat buildings | catalogue arms with A/B parity each (cannon coverage ranker already wired per M2) |
| `position/*` recovery (`RefreshConstructionPosition` etc.) | dies with the chain; refresh semantics re-derived from catalogue passes, not ported line by line |

## Steps (one commit per step, in order)

1. **Parity A/B, flag off vs on** (long OpenBW runs, same map): zero
   `Can't find place` delta, no new placement-failure class, builder
   travel sane, CATALOGUE_SITE declines all named. The flip waits on
   this, not on courage.
2. **Default flip** (`PLACEMENT=catalogue` default): one long run,
   same bars. Roll back on any new failure class - the flag (not a
   revert commit) is the lever, then a forward fix.
3. **Gas + depot + combat arms** through the funnel, one arm per game
   proof each. Bases stay dedicated until C14.
4. **Move the chain to `src-old/`**: `FindPosition`,
   `DefineExactPositionForNewConstruction`, `APositionFinder`,
   `position/*` recovery. `NewConstructionRequest` stays (the door).
   Suite green, ArchUnit store shrinks or holds.
5. **NEXT #145 line**: narrow or close per §7 with run ids.

## Verification

- Steps 1-3: run ids + `Can't find place` counts + CATALOGUE_SITE
  decline breakdown in the commit message.
- Step 4: `grep -rn "APositionFinder\|FindPosition" src/atlantis`
  shows only the door's comments/history, suite + ArchUnit green with
  no store growth.

## Stop rules

- Any new `Can't find place` class on the flipped run → stop, back to
  flag-off, the arm (not the flip) is wrong.
- An arm needs expansion siting (C14) → stop, bases stay dedicated,
  note it, do not fake it.
- Any urge to also move the request door, BO rows or V2 → stop. Those
  are #144/#147, not this order.
