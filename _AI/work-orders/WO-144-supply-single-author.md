# WO-144 — supply single author (NEXT.md #144, part: the spec)

## Goal

The board supply goal owns ALL supply demand. Then supply pumps go
silent and `SupplyCommander/**` + `ProduceFallbackSupplyWhenSupplyLow`
(+ Depot twin) move to `src-old/`. Exit per NEXT #144: one author,
supply pumps silent.

## Non-goals (do not touch)

- Sited power demand (cannon Pylons via `BuildPylonFirst`, wall pieces
  via `ProduceNaturalWallIn`, per-base via `ProducePylonNearEveryBase`).
  Those are exact-placement power/site wants, not supply wants - they
  become exact-demand through their owning goals, they are not deleted
  with the supply pumps.
- Build-order rows while the BO owns the game (`BuildOrderOwnsGame`).
  The opening Pylons (At 8/15/21/27) stay until the V2 timeline owns
  the opening, which is #147 territory, not this order.
- Door guards in `ProduceBuilding` (`pylon-cap`, `warping-link`,
  `funding`, `core-priority`). They stay - one author changes who
  asks, never the door that refuses.
- Terran/Zerg assemblies (D8, Protoss-only).

## Prerequisites

- Read `DynamicGoals.supplyGoal` (WORRY_LEVEL 4 + providers-coming
  suppression), `ProduceFallbackSupplyWhenSupplyLow.minFreeSupplyToAct`
  (bands 2/4/8/10/12/16/16/22 by total + 350-mineral bonus),
  `SupplyCommander.handle` (bands by total + `tooFewSupplyLeftAsForGateways`
  + in-flight caps), `PullForwardGoals` (supply rule lives in
  `DynamicGoals`, `contribute` currently emits gas-only),
  `GoalBoardFeeder` (supply-pylon row, `inFlight` covers queue rows +
  constructions), `GameStateSnapshot.twinSuppressedSupply`.
- Baseline: full suite + ArchUnit numbers, written down.

## The five behavior deltas (each needs its own game proof)

1. **Foresight bands vs flat 4.** Legacy orders at free <= 8-22 by total
   (a warp is ~600f; ordering at 4 still blocks 11-37 s WITH bank,
   measured on Wine). The board goal fires at free <= 4. Parity needs
   the scheduler to start the provider early enough that it COMPLETES
   before the block - `ResourceTimeline` arithmetic, stub-pinned first,
   then a game showing no block at 17/17 and 25/25.
2. **Gateway foresight** (`tooFewSupplyLeftAsForGateways`: total >= 30,
   order when free < 2*gateways (+1 when bank >= 120)). Either the
   board goal learns it or a game shows the flat rule never blocks
   behind 2-4 Gateways. Do not drop it on reading.
3. **Providers-coming suppression is load-bearing.** Without it the goal
   fires every frame until warp and the scheduler orders a Pylon per
   frame (the "2800 ordered Pylons" run). Any rewrite keeps an
   equivalent suppression with the SUPPLY_BLOCKED twin naming what it
   waits on (sample + blocker rows, `twinSuppressedSupply`).
4. **Pipeline overlap is doctrine.** Two in flight max while poor
   (owner: the next Pylon goes out BEFORE the previous completes).
   The `pylon-cap` door guard already enforces it for every author -
   verify, do not re-implement.
5. **Opening handoff.** BO rows own the start; the board goal must not
   double-order Pylon #1 (minSupply 0 goal vs At-8 row). DRY_RUN first:
   V2 planning alongside legacy must show zero duplicate supply rows.

## Steps (one commit per step, in order)

1. **Demand matrix, stub-pinned** (no game): supply-goal WHEN over
   (free, coming, total, bank) x (BO owns / board owns). New test next
   to `SupplyGoalTest` (LANDED 2026-10-18: 3/3 pins WORRY 4/5,
   coming-suppression at free 0, contribute end to end). No production change.
2. **DRY_RUN parity on supply rows** (short OpenBW run): with legacy
   executing, V2 DRY_RUN must plan the same provider count ±0 within
   the opening window. Evidence 2026-10-18 (`105055` DRY_RUN short):
   V2 `would issue` the opening Pylon 6x frames 1131-1136 (one demand,
   re-planned per frame by design), tile wobbles once (112,8)->(113,15)
   then stable - and legacy warped exactly [113,15] at 1247. Demand
   parity holds; tile stabilizes in 1 frame. Log diff in the commit.
3. **LIVE flip, one game proof** (long OpenBW run): no supply block at
   17/17 or 25/25, no duplicate Pylon warps, SUPPLY_BLOCKED samples
   flowing, feeder supply-pylon row only as rescue. Then and only then:
4. **Move to `src-old/`**: `SupplyCommander`, `ProduceFallbackSupplyWhenSupplyLow`,
   `ProduceFallbackPylonWhenSupplyLow`, `ProduceFallbackDepotWhenSupplyLow`.
   Same cycle, suite green, ArchUnit store shrinks or holds.
5. **NEXT #144 line**: narrow to done with the run id, or close per §7.

## Verification

- Step 1: new test class green, suite 728/728.
- Step 2: DRY_RUN log shows V2 supply rows == legacy supply rows.
- Step 3: long run roster shows continuous supply (no 9/9 or 17/17
  stall), ledger shows no same-block duplicate warps.
- Step 4: `grep -rn "SupplyCommander\|FallbackSupply" src/` empty,
  suite + ArchUnit green with no store growth.

## Stop rules

- A step needs a game the queue cannot give (150 s cap kills it) → stop,
  record the run id, do not widen timeouts silently (§17).
- DRY_RUN shows a duplicate supply row → stop, the handoff (delta 5)
  is wrong, do not flip.
- LIVE shows any supply block → stop, foresight (delta 1/2) is wrong,
  revert the flip, keep legacy.
- Any urge to also move sited power demand or BO rows → stop. That is
  #145/#147, not this order.
