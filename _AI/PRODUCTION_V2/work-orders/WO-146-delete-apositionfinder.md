# WO-146 — delete the legacy `APositionFinder` path (NEXT.md #146, part: inventory)

## Goal

Zero `Can't find place` across retained runs, legacy path gone. This
order is the inventory + checklist; the chain move itself rides with
WO-145 step 4 (same commit may close both when the exit holds).

## Non-goals

- Designing catalogue arms (WO-145). This order deletes, never designs.
- Touching the request door, BO rows, V2 scheduler (consumers, not the path).

## The inventory (measured 2026-10-18: 23 files reference `APositionFinder`)

**The chain (moves to `src-old/` with WO-145 step 4):**
`position/APositionFinder.java`, `position/ASpecialPositionFinder.java`,
`position/FindPosition.java`, `position/protoss/GatewayPosition.java`,
`position/protoss/PylonPosition.java`,
`position/modifier/PositionModifier.java`,
`position/terran/SupplyDepotPositionFinder.java`,
`position/base/` (4 files: `FindPositionForBase`,
`FindPositionForBaseNearestFree`, `NearestFreeBase`, `NextBasePosition`).

**Callers that must be re-pointed BEFORE the move (each a checklist row,
checked = calls the funnel or is gone):**

- [ ] `position/FindPosition.java:106` — `findStandardPosition` (the
  funnel entry: catalogue first, legacy fallback). Dies with the chain;
  the funnel entry moves to the door.
- [ ] `position/RequestBuildingNear` — sited entry. Same.
- [ ] `position/base/*` (4 files) — expansion siting. LAST (blocked on
  C14); until then they keep calling the legacy path loudly.
- [ ] `position/modifier/PositionModifier.java` (6 call sites) — enemy
  choke/natural attack siting. Needs a catalogue arm or a justified
  dedicated finder each; none may silently keep the ring search.
- [ ] `position/terran/SupplyDepotPositionFinder.java:35` — D8 stub
  territory; Terran stays legacy until D8 lifts.
- [ ] `production/v2/LegacyPlacementPlanner.java:191` — **critical path**:
  the V2 scheduler itself sits through the legacy finder
  (`findStandardPosition`, single tile, FIRST_CANDIDATE). #146 cannot
  close while V2 LIVE plans through it; the catalogue arm for V2
  (multi-candidate resolver + power scoring) is the flip, with the
  no-block parity proof from WO-145 step 1.
- [ ] `production/constructions/*` door files (`NewConstructionRequest`,
  `Construction`, `ConstructionRequests`, `ConstructionStatusChanger`,
  `cancelling/CancelNotStarted`) — reference only (cache clears, types);
  re-point or delete with the chain, no behavior.
- [ ] `production/requests/AntiLandBuildingCommander.java`,
  `battle/zerg/ZergCreepColony.java` — confirm they reach siting only
  through the door/adapter, never the ring directly.
- [ ] `config/env/Env.java:186` — the `PLACEMENT` flag docs; flip the
  default per WO-145 step 2, keep the flag as the lever.

## Steps

1. Check each row above; a row that already calls the funnel gets
   checked with the commit that did it.
2. V2 planner arm lands (catalogue, multi-candidate) with parity proof.
3. Flip default (WO-145 step 2), arms (WO-145 step 3).
4. Move the chain; `grep -rn "APositionFinder" src/atlantis` shows only
   history comments. Suite + ArchUnit green, no store growth.
5. Close #145 and #146 together per §7 with run ids.

## Verification

- `Can't find place` count across retained runs: zero before and after.
- Post-move grep (step 4) + suite + ArchUnit in the commit message.

## Stop rules

- A caller with no catalogue arm and no dedicated finder → stop, it
  keeps the legacy path loudly, do not delete under it.
- V2 LIVE still plans through `LegacyPlacementPlanner` → stop, step 2
  is not done, the flip waits.
- Any urge to redesign the door or the scheduler here → stop. That is
  #144/#147.
