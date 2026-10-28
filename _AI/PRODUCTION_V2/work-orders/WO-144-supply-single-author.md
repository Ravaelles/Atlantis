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

1. **Foresight bands vs flat 4.** Legacy bands were free <= 8-22 by
   total; CAPPED 2026-10-19 (owner order, Wine 38/65): every band
   triggers at 6 or below, free never above 12-14. The board goal
   fires at free <= 4. Parity needs
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
   Fallback side pinned 2026-10-19 (`FallbackBandsParityTest` 5/5: capped
   bands 2/4/6/6/6/6 poor, +1 rich below the cap, -1 past 200; divergence
   window exactly free 5-6 where legacy fires and the board stands down).
2. **DRY_RUN parity on supply rows** (short OpenBW run): with legacy
   executing, V2 DRY_RUN must plan the same provider count ±0 within
   the opening window. Evidence 2026-10-18 (`105055` DRY_RUN short):
   V2 `would issue` the opening Pylon 6x frames 1131-1136 (one demand,
   re-planned per frame by design), tile wobbles once (112,8)->(113,15)
   then stable - and legacy warped exactly [113,15] at 1247. Demand
   parity holds; tile stabilizes in 1 frame. Log diff in the commit.
   Second datum 2026-10-19 (`185237` DRY_RUN short, same map): opening
   window 900-1500 holds the same shape (V2 Pylon 16x at stable
   (112,8), legacy warps [113,15] at 1247 - demand parity, tile a few
   tiles off, the known siting gap). Late divergence noted, two
   readings: V2 plans ZERO Pylons after frame 5000 while legacy warps
   3 (#11/#36/#37) - correct suppression against in-flight legacy
   providers, or a demand miss; the LIVE run (step 3) decides.
3. **LIVE flip, one game proof** (long OpenBW run): no supply block at
   17/17 or 25/25, no duplicate Pylon warps, SUPPLY_BLOCKED samples
   flowing, feeder supply-pylon row only as rescue. Then and only then:
   First attempt 2026-10-19 (`191444` V2 LIVE, 23026f/16 min): no
   blocks (free healthy throughout, 97 SUPPLY_BLOCKED samples), tech
   on time (Forge 1:50, Core 4:12, gas 3:54) - but step NOT met: 609
   Nexus cancel/re-request cycles on the refresh timer (Nexus=2
   never, C14), 0 Dragoons (floor freeze), Pylon pairs 89f/72f apart
   unattributable (minSupply-0 rows, no DECISIONS in the V2 path).
   C14 + Dragoons + V2 audit first. Second attempt 2026-10-19
   (`194718` V2 LIVE with the PurpleWave rule + lag cover, 21905f):
   pairs persist through a different mechanism - a per-frame
   mint-cancel stream on covered tiles (28 fresh Pylon plans in
   300 frames, 51 proximity cancels on [9,47]-class tiles): doomed
   constructions die same-frame so no coming-count can see them.
   Suppression itself engages (blocker-construction twins fire,
   coming=2 stops the stream once two travel). Root causes left:
   V2 siting blindness (WO-146) + cross-frame builder commits
   (WO-147 in-flight). The unstarted pile-up class is closed.
   Third attempt 2026-10-19 (`203655` V2 LIVE with rule+lag+>60,
   21933f/15 min): Dragoons 8 (first 5:25) - the ASAP rule fires
   live; one 150f pair left, named by CLOSE_WARPS with both tiles.
   Open: whether the remaining pair is a BO-row re-fire
   (BuildOrderGoals dedups per frame but has no coming-suppression),
   pipeline-legal warping overlap, or a hole - the goal-source twin
   decides.
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
