# TODO — Atlantis (open issues)

Living backlog for the architecture migration (stages A–D done, E–J open
below) and for defects found along the way. This file is the single source
of truth for "what is left"; `_AI/NOTES.md` keeps operational learnings.

## Phases (the quarter) — checklist

Merged here 2026-10-18 from `_AI/MASTER-PLAN.md` (deleted, same change):
one task file — phases order the quarter, "Next steps" below orders the
week. A phase is done only when its exit holds **in a game**, never on
reading green.

- [x] **Phase 0 — Foundations.** Headless OpenBW loop bounded (§17), fast
  suite + frozen ArchUnit store, Java 1.8 everywhere, twin diagnostics
  (`EVENTS.jsonl`, `BUILDINGS.json`) + query tooling. Exit was: a game
  attaches, plays bounded, leaves queryable logs. Met.
- [ ] **Phase 1 — Production correctness (top priority).** The memorized
  opening plays itself (BO ownership, worker flow, reservations that never
  starve the pump); every issuance lands in `DECISIONS.log` with its reason.
  Exit: an opening with zero builder churn, zero duplicate sites, worker
  stream unbroken, every row attributable. Substantially met (script-clean
  opening 080114, 720-row attributed log) — remaining: #95 (reserved-250
  watch), #96 (Wine verification batch). 2026-10-17: same-tile re-request
  loop closed (proximity finish by footprint overlap + CONSTRUCTION_CANCEL
  twin; 5x-in-a-minute [119,18] pile-up gone, verified in 743 s OpenBW game),
  scout roam fallback landed (no idle in 12+ min).
- [ ] **Phase 2 — Decision board completion.** Per-gate legacy deletions
  with one game of proof each (ratchet, never batch rewrites); absorption
  slice by slice until the old trio dies at zero. Exit: one issuer, one
  log, no silent authors. Tasks: Next steps 1–2, goal-board track (#91).
- [ ] **Phase 3 — Combat cluster (structure done, behavior pending).** One
  cohesive cluster between mission anchors, evaluator fight/flee with a 10
  s retreat latch, role executors with parameterized kiting. Exit: an
  OpenBW game shows one group moving between anchors and winning an even
  fight it should win. Tasks: `__PLAN-COMBAT.md` step 5, blocked on #94.
- [ ] **Phase 4 — Placement cutover completion.** Catalogue planner
  default, legacy finder deleted per `PLACEMENT-CUTOVER-PLAN.md` +
  `redesign/03_PLACEMENT.md` (starts after Phase 1). Exit: zero `Can't
  find place` across retained runs, legacy path gone.
- [ ] **Phase 5 — Production V2 LIVE + architecture migration.** V2 default
  behind the #45 deletion ordering; stages E–J below (after Phases 1–2).
  Exit: V2 LIVE by default with parity numbers, ArchUnit store at zero.
  Diagnosed 2026-10-19 (mechanisms named with run ids; fast-loop slices
  landed after, each stub-pinned, no games per owner direction):
  full 1209 s LIVE games play fast but thin (10 warps: Pylon=2 Gateway=1
  vs legacy ~40/9). (a) Core unplaceable: planner refusal twin proves
  power (`Not powered by a Pylon x517/517`, `20261017_135655`) - V2
  pylons land wherever, powered space fills, tech starves; legacy
  avoided this with pylon scoring (unwired `PylonPlacementScore`) and
  pull-forward. OPEN - wiring needs a multi-candidate finder (the legacy
  one returns a single tile; inventing candidates is worse - see the NOTE
  in `LegacyPlacementPlanner`). (b) Legacy queue machinery fires on unit events inside
  LIVE (PREPARING orphans with builders, ORDER_NOT_READY rows, 200-300
  leaked reservations via status-flip reserve/release imbalance) -
  LANDED: single-policy gate (`Queue.refresh` no-ops in LIVE,
  `QueueLiveGateTest`). (c) Supply-goal suppression by unfinished-forever
  providers (`supplyProvidersComing` never clears a stuck warp).
  Twin LANDED (`SUPPLY_BLOCKED` samples + blockers); scheduler
  standing-vs-inflight split LANDED (`inFlightFrom` - the 25/25 death was
  the exists-check, not a stuck warp); tripwire re-tuned twice (blocked
  hoard only, warping provider suppresses). A true stuck-provider guard
  stays unproven - no run has shown one since. (d) Worker trains: 20 V2 train orders, +7 net probes in 12 min -
  twin lie, not starvation: dedup-hits logged "trained" without issuing
  (train + research paths now report issued=false,
  `AlreadyTrainingIsNotIssuedTest`, `AlreadyResearchingIsNotIssuedTest`).
  Same sweep found V2 research never dispatched at all: multi-word
  facility short names (`CyberneticsC`) resolved to null - LANDED
  (`FacilityNameRoundTripTest`, fallback in `getByName`).
  Next slices in this order: power-smart siting, longer-game proof
  (Singularity live fire, V2 gas order, parity), then the default flip.
  `202456` (V2 LIVE, 1055 s): gas order FIRED (Assimilator 4:08@27,
  exact geyser); Core+Forge live (tech gap closed); Singularity still
  not live; parity 0.63 vs 0.83 buildings/min (legacy `200123`) -
  thin, drivers now no-Dragoons + no-Nexus-2, not missing tech.
  `210732`-era follow-up: Singularity FIRED live on V2 (`202456`,
  researched 5:16). Nexus demand fires (pinned `V2ExpansionDemandTest`
  4/4); `202456` never warped it for a measured reason, not timing:
  the site resolves to our own base tile [9,47] and the proximity
  finish cancels it 16k times (frames 8317-24620) - V2 has no expansion
  siting (03_PLACEMENT.md C14, open), the generic door keeps offering
  the main-base footprint. Filed as #150 (backoff vs real siting is
  the design call).
- [ ] **Phase 6 — Measurement and learning.** Micro-scenario battery with a
  frozen baseline; parameter search over role vectors; fingerprint-driven
  strategy selection. Exit: a tuning loop that improves a frozen scenario
  table, unattended. (After Phase 3 + decision log.)
- [ ] **Phase 7 — Expansion.** Second goal vocabulary (owner-directed),
  sweeps vs real bots, OpenBW-vs-live parity. Nothing starts early.

Non-rules: never batch rewrites; never gate commits on full games; never
trade a measurement for an explanation (§3a). Deletions wait for per-gate
game proof, not for courage. A phase's exit is verified by execution.

## Current owner priorities (standing since 2026-10-09, confirmed 2026-10-18)

1. **OpenBW simulations must be fast and self-terminating** - CONVENTIONS §17:
   `TIMEOUT_SECONDS=150` wall-clock and `INGAME_TIME=20` game minutes, both set
   once at the top of `scripts/run-openbw-e2e.sh` (raised 120 -> 150 by the
   owner 2026-10-15, commit `da9940db`). Every future run mode gets the same pair.
2. **Finish Production V2** - `_AI/NEXT.md` #45 and `_AI/redesign/01_PRODUCTION.md`.
3. **Placement** - the rewrite landed (`_AI/redesign/03_PLACEMENT.md`); the
   cut-over and the rest of Protoss are what remain.
4. **OpenBW tests** - `_AI/IDEA-E2E-TESTS.md` §3.5 has the ordered steps.

The index of which document answers which question is `_AI/README.md`.

## Process (normative)

- Every item has a **stable number** (`#1`, `#2`, ...). Numbers are never
  reused and never renumbered, so an old commit or chat answer can always be
  mapped back to the item it referred to.
- **When an item is closed, delete its line from this file** (do not move it
  to a "done" section — the git history is the archive) and say in the commit
  message `Closes #<n>` with the reason it is actually done (test result,
  game-run id, ArchUnit output).
- New items are appended with the next free number, grouped by area.
- Work proceeds in any order; prefer items that unblock the most downstream
  work, not the easiest ones.
- One logical change per commit, per `_AI/CONVENTIONS.md` §6.
- An item is only closed when it is **verified by execution** (build, test
  run, or game run), not when the code merely compiles.

## Next steps (ordered - this section survives context compression)

1. **Per-gate legacy deletions**, one game of proof each (ratchet):
   no candidate yet - feeder coverage re-measured 2026-10-16 across
   retained runs: 29 sustained-dragoon + 2 sustained-zealot rows, ZERO
   supply/gateway/core/assimilator. Re-measured 2026-10-17 incl. the
   743 s long run: board tops (supply-pylon x2, core, workers, sustained-
   dragoon, wall-in) but ZERO BOARD_FED - the pumps do ~everything, the
   feeder fills silence - wrong direction for deletion; buildings are
   all-legacy. Board-invisible production (Forge, cannons) is
   capability, not duplication. Deletions wait for routine feeder
   coverage, never batch rewrites. Re-measured 2026-10-17 across all 12
   retained runs: 9 BOARD_FED rows total (8 sustained-dragoon + 1
   sustained-zealot), ZERO supply/gateway/core/assimilator - the feeder
   still only fires sustained-army rows, never a per-gate row; no deletion
   candidate, direction unchanged.
2. **Decision absorption:** board takes over decision sites one by one
   (8 files with the exact singular import, measured 2026-10-17,
   re-verified 2026-10-17: still 8 on the same excluded tracks,
   production holds ZERO -
   was 47: 24 died with the combat cutover, 1 orphan (TransitionToCorsairs)
   absorbed, 14 more with the targeting + battle src-old sweeps;
   counting down, no new ones per §21); the class dies at zero,
   not before. All 8 survivors sit on excluded tracks (battle/micro,
   interrupt, terran-info) - production holds ZERO imports. Two tracks:
   production sites route through board state (first slice: the worker
   Core-veto, `WorkerCoreVetoBoardTest` 2/2; second slice 2026-10-16:
   board names the enemy race, Stargate Terran veto reads it,
   `StargateEnemyRaceBoardTest` 2/2; slices 3-5 2026-10-17: Stargate
   Protoss/Zerg branches + the Singularity trio read dragoons, army,
   hydras/lurkers/mutas, carrier-tech and sparta off the board,
   `AbsorbedBranchesBoardTest` 3/3 - production holds ZERO Decision imports - the type survives
   only on battle/micro/A/interrupt/terran-info tracks (combat cluster,
   stages E/H), combat-micro sites die with
   the manager-tree migration (stages E/H), never through the board
   (no per-unit micro on it). Fast-loop slices 2026-10-18, no game runs
   per owner direction (demand matrices pin in stub states instead):
   wall-in WHEN on the board (`WallInGoalTest` 5/5 + feeder seams,
   emitter obeys funding fail-open); cannon COUNT matrix moved
   (`CannonGoalTest` 7/7, legacy matrix deleted, ladder pins updated);
   Forge timing moved (`ForgeGoalTest` 8/8, 4 new state fields).
   REFUSED for now, recorded not dropped: robotics/observer demand
   (nested pressure + rush-strategy semantics) and the gateway threshold
   thicket (first-match chain) migrate only with a game proof each -
   mirroring them blind risks real regressions no stub test can catch.
3. **Pylon closure:** Wine re-run with the current jar (convertible
   waiters + walking-builder shield are in it) for the first-Pylon
   proof (#84); corner-stall game for doctrine-vs-leash (#81).
   Owner-run; autonomous work above does not wait on it.
   OpenBW control (not the proof itself): first Pylon warps in every
   retained run, 0:24-0:26 on adhoc tiers, 0:50-0:53 on short/long
   (BO expects supply 8) - the order-to-warp pipeline is healthy;
   Wine's failure is builder-side, per #84's diagnosis.
4. **Game-proof backlog** (all need a quiet run, OpenBW or flagged
   Wine): 700 s+ rich run (#71), L2/L3 (#85), builder-variance
   leftovers (#74).
5. **Second goal vocabulary** (Terran or midgame): triggers the
   `goals/` split by vocabulary - until then no new packages.
   Assessed 2026-10-16, no clean midgame slice: tech goals preempt
   #45 ordering, fortification is planner-owned (two sources of truth),
   observers already have a pump - vocabulary grows on owner
   direction, not on availability.
6. Unchanged long-term ordering: #45 deletion order, rewrite order
   #144-#147, #8 V2 LIVE runs, stages E–J below.

## Test health

Re-measured 2026-10-17 (`rm -rf out` first): unit 724/724 green, 0
skipped (was 721 - incl. DecisionLogTest 2/2, EventLog truncation pin
and WarpingBuildingBlocksOverlapTest 4/4).
Architecture advisory
9/11, strict gate 5/7 (was recorded 10/11 - record was stale): two
rules fire, both pre-existing unfrozen edges, neither fixed here
(both carry live behavior, Stage E/H territory, never grow the
store): `architectureMustNotDependOnFeatures` via
`Manager.invokeFromParent` → `SleepUntilFrameGate.mustSleep` (sleep
gate, #67) and `coreMustNotDependOnOtherContexts` via
`SpecialCommander.subcommanders` → `ForceExitWhenInvalidState`
(added 2026-10-17). Store diff empty after both runs.
Suite green 2026-10-18: 683 passing, 0 skipped (owner batch:
PREPARING sites reserved+stable, painter warping labels, important-only
DECISIONS, role-named logs; plus earlier combat cutover:
`src/atlantis/combat/` → `src-old/`, `atlantis.battle` extracted,
15 combat-behavior tests deleted incl. the rush twins, kept tests
repointed; `UnworkedConstructionSurvivesNearbyBuildingTest`, `StreetGridDoctrineTest`
1/1, `SustainedFlowGoalTest` 6/6, +2 feeder army rows, ladder re-pinned,
`ScoutFallbackEnemyBasesTest` 2/2, `GoalBoardWalletTest` 3/3, +2 feeder
enforcement rows, `StaleDuplicateFinishesOnItsTilesTest` 2/2,
`EarlyConstantWorkerFlowTest` 3/3, `TechCoveredSkipsRequeueTest` 3/3,
`UpgradeCompletionTrackerTest` 3/3, `LogKeySeverityTest` 3/3,
`PumpDecisionMatrixTest` 7/7, `ScoutTerminalAnchorTest` 2/2,
`WorkerQueueDecisionTest` 3/3, `OnGameEndLatchTest` 4/4; selected runs:
`PylonSearchBreakdownTest` 2/2, `GasRefreshKeepsDecidedSiteTest` 1/1, `PreparingSiteSurvivesSiblingTest`
1/1, `PylonLastResortTest` 2/2, `FirstPylonTimingTest` 1/1,
`FirstPylonTravelsEarlyTest` 2/2, `RefreshCancelNeedsBuildingTest` 3/3,
`SupplyNineWorkerMandateTest` 2/2, `PlacementFailureNamesDominantRuleTest` 2/2,
`NoPreparingWhileWarpingTest` 2/2, `WorkerCoreVetoBoardTest` 2/2,
`ReservationWaitsForMinSupplyTest` 2/2,
`ThirdGatewayBlockedWhileTwoOutstandingTest` 1/1,
`WarpedOrderIsNotResitedTest` 2/2,
`StargateEnemyRaceBoardTest` 2/2,
`ADecisionTest` 3/3, `DecisionDispatcherTest` 3/3,
`ProducerQuestionsTest` 4/4, `BuildOrderOwnsGameTest` 5/5);
architecture 10/11 (the one known structural failure); rush-defense
twins green (1/1 each, re-verified today). Re-run
`bash scripts/run-tests.sh` - counts written here rot. What is open is
*quality* of the data behind it, not a red test.

## Goal board track (active)

- **#91 - GoalBoard: ranked arbitration without OpenBW.** Owner direction
  1A/2A/3A. Pure board (`atlantis.decisions.board`, zero engine imports):
  `BoardState` snapshot + ladder + in-flight commitment with emergency
  break. Domain vocabulary lives next door (`decisions.goals.protoss`:
  supply, detection, workers, gateway, core, assimilator, army, expansion;
  CONVENTIONS §21). Ranking proves in milliseconds (`GoalBoardTest` 7/7).
  Consumers, all live: advisor (BOARD opinion log), emergency-only worker
  driver (`BoardEmergency`), feeder (missing rows for supply/gateway/
  core/assimilator plus sustained army rows when the pumps go silent -
  workers/expansion/detection stay with their owners). Wallet done:
  price classes on the goals, funded subset, feeder gated on funding,
  enforcement at the single construction door (fail-open). Commitment
  wired: `top` finally gets the live in-flight set (queue rows +
  constructions via one goal-type map), driver and advisor pass it -
  started work keeps its place. No new
  arbitration gate in legacy producers, ever (ratchet).
  Next: per-gate legacy deletions, one game of proof each (feeder proof
  observed `20261015_143903`: `BOARD_FED supply-pylon min=61 sup=39/41`).
  Relation to #45: the board is the Strategist half without the timeline scheduler
  (deferred - every measured starvation was a priority inversion).

## Stage E — read model (remaining)

- **#3** Migrate production readers of `FoggedUnit` to `UnitSnapshot` once
  the World registry owns lifecycle (a delegation switch has no value
  until then): `BuilderAvoidEnemies:27`, `DeadMan:64`,
  `AFocusPoint:111`, `DoAvoidEnemies:30`,
  `AtlantisJfap.isValidUnit:72` (borderline). `ProcessAttackUnit:56`
  died with the combat cutover (now `src-old/`). The rest was triaged out
  (same object, worse GC) or dies with lifecycle.

## Stage F — cache purge (Select query caches gone 2026-10-19, see below)

- `core.world.UnitIndex` answers from frozen per-frame lists (ours/all/
  enemy/neutral side sources); every `Select` query key migrated with
  identical predicates (incl. alive/completed subtleties, TreeSet order,
  De Morgan on the 3-flag enemy filter); `clearCache` resets frames.
  The five unexplained TTLs (30, 31, 53, 73, 293) are deleted with them;
  `Select.java` holds zero live cache reads (only the derived-selection
  cache via `cloneByRemovingIf` remains - a different concept).
  Acceptance failure set byte-identical to the clean-tree baseline twice
  (one real catch on the way: side-membership is list membership, never
  a predicate - sideless test doubles leaked across sides).
- `Select.main()` (90 call sites) now answers fresh per frame. Inventory:
  `DOCS/SELECT-CACHES.md` (frozen 2026-10-03, historical); neutral cleanups done per
  `_AI/work-orders/WO-F-neutral-cache-cleanup.md`.

## Stage G — race strategies

- **#8** Seam + one end-to-end subsystem migrated as proof (ADR 0007
  accepted; pilot: `ProductionRacePolicy` + 3 impls, V2 DRY_RUN zero
  errors). First LIVE probe `20261015_171504` (short): attaches, plays,
  builds opening (2 Pylons, Gateway, 3 Zealots, Forge, Assimilator), no
  crash, no exceptions. First LIVE victory `20261015_193721` (long,
  755 s, 5/0 kills, +500): full game to natural end on V2, 8 warps, all
  rows minSupply 0 (no BO expectation reaches the ledger - the deferred
  timeline scheduler). Two watches: Pylon #1 warped at sup5 twice now
  (pull-forward or waste - undecided, needs a side-by-side); third V2
  datum `202456`: sup5/0:26/bank4 vs legacy `210732` sup8/0:52/bank46
  the same week - V2 consistently ~26 s earlier on an empty bank,
  still undecided. Forge
  divergence still tracked, not regressed. LIVE retry `20261016_131515`
  (133 s wall, full 1209 game-s, 0 exceptions, legacy pumps provably
  out - no DRAGOON/SINGULARITY/BOARD rows, 24k V2 lines): V2 runs the
  whole loop, but thin (10 warps/20 min vs legacy ~40/9) - the parity
  gap is live-measured, default stays off, deletions stay ordered.
  Parity numbers (`131515` LIVE vs `151714` legacy): 2.4 vs 5.8
  warps/min; V2 built no Core (hence no Dragoons), no Nexus, and two
  Forges - tech coverage is the gap, exactly per the ordering.

## Stage H — god-class split

- **#9** `A` is down to 43 public static methods (from 163). Left:
  resource/supply facades (~1400 call sites), clock arithmetic, race
  predicates (Stage G), wall-clock formatters. The facade destination
  (`AGame` vs `EconomyContext`) belongs with Stage G/I - do not start
  it here. (Wall-clock extraction tried and reverted 2026-10-09: trivial
  split, unreliable edit path, zero behaviour change.)
- **#10** Split `Selection` (226 public surface methods, measured
  2026-10-17: 166 one-line delegates into 3 seams, 60 remaining) along
  internal seams, with a test pinning selection semantics before/after.
  with a test pinning selection semantics before/after. Four slices landed
  (radius family → `SelectionRadius`, membership filters →
  `SelectionMembership`, ordering → `SelectionSorting` in two parts:
  sorts then nearest-finders, each with a pin test, zero call-site
  changes): cross-context readers stay put until their dependencies are
  fixed - moving them re-spells frozen violations instead of removing
  them (measured, see NOTES.md arch mechanics; slices 3-4 verified:
  same 2 pre-existing failures, store diff empty). Remainder is
  accessors (`units()`, `first/last/random`, `size/count/isEmpty`),
  thin dispatchers and multi-statement logic needing designed splits,
  not verbatim moves.
- **#11** Split `AUnit` (577 public methods, measured 2026-10-17): the
  order-emission surface needs no verbatim slice - `AUnit` holds zero
  direct engine-order calls (all 17 `u().` uses are pure reads; emission
  flows through the 45 `AUnitOrders` defaults into the `OrderSink` seam).
  Per-frame orchestration lives outside it too (no `update`/`onFrame`/
  `handle` in `AUnit` - the invoke chain is `architecture.Manager` /
  `Commander`, the unit only carries the `manager` field + history for
  the #18 inspection). What remains in `AUnit` is state + predicates:
  the split starts at per-condition logic, if at all.

## Stage I — physical migration

- **#12** Package move to bounded contexts per
  `DOCS/ARCHITECTURE-CONTEXT-MAP.md`, mechanical and separately
  revertable per context, only after F–H.

## Stage J — fortress

- **#13** Zero-violation baseline, then flip the frozen store to hard
  rules. The 146 + 23 are structural (unit-centric Commander/Manager
  framework → Stages E/H), not a sweep (re-measured 2026-10-17, was
  recorded 255 + 24 - the combat cutover deletions shrank the core
  rule); the information rule triages to
  live strategy-response logic (Stage G seams, not line deletions).
  Traps, paid for: a change must remove violations from one rule, not
  move them into another; `rm -rf out` before architecture runs after
  moving classes (`javac -d` leaves stale `.class` files behind).
  Detail: `_AI/work-orders/WO-13-mechanical-archunit-sweep.md`.

## Needs a game (quiet OpenBW run or flagged Wine run)

- **#150** Opening Gateway statuses lie (owner report 2026-10-17, Wine
  game 22:07, single base [112,10]): (a) Pylon PREPARING 0:14 with no
  object shown, adopted 0:47 - correct but invisible (map markers skip
  PREPARING; sidebar lists it); painted orange since 7c458cca, visual
  proof pending owner; (b) GW#2 At-10 funding-blocked 1749-1830
  (`BUILD_REFUSED ... funding` names it live - twin works on Wine),
  requested 1831, IN_PROGRESS by ~1980 - slow on real-time clock, not
  stuck; (c) GW#3 site [112,12] picked the frame GW#2 warped at
  [112,10] (shared row) - same-frame race invisible to pick-time
  guards, which see the warping unit only after it lists. LANDED:
  `OverlapsOurUnfinishedBuilding` in the spiral (210732/214926);
  adopt-time revalidation re-sites stale picks instead of adopting
  them (live proof 220122 AND Wine 23:20: resite [94,111]->[86,110],
  clean warp, no duplicate - the owner's exact case, fixed live);
  `BUILD_REFUSED`
  adopt-site-blocked/adopt-no-free-builder names the silent adopt
  exits; CONSTRUCTION_CANCEL proximity twin names the covering
  building (unmasks silent self-finish loops); advance seam pinned
  (`ConstructionStatusAdvanceTest` 4/4); E2E opening-status gate
  (`EXPECT_OPENING_STATUS=1`: Pylon/Gateway BUILDING_STARTED + ledger
  warps; self-test RED/GREEN on fixtures, live proof pending a clean
  short game - the 30 s cap keeps killing shorts inconclusively).
  Writer-level fix landed: EventLog/DecisionLog truncate on first
  flush per JVM (pinned, incl. simulated restart), so no launcher
  can accumulate games anymore - scripts keep their pre-run rm as
  belt-and-braces. Wine logs were unusable for
  all of this (7 MB EVENTS.jsonl spanning 6 games) - fixed by clearing
  the write dir pre-game like OpenBW. Still open, needs a game where
  two same-type warps overlap: wrong-sibling adoption
  (`CANNOT define construction` lines) is the last unproven branch;
  everything else on this item has a live twin or a live resite. No
  blind patch.
- **#149** Causeless early exit, 4 occurrences, 1 explained:
  `234004` (V2, 5:43) is the hoard tripwire firing by design
  (`ForceExitLocallyInOpenBW`: bank 606, supply 40/41, nothing coming -
  named in E2E_DEBUG, `GAME_END` twin attached the state). Still open:
  `131407` (V2 LIVE, 4:07), `210732` (legacy, 5:08) and `213248`
  (legacy rich, 9:50) end clean with no force/leave/GG/tripwire line,
  all enemyless, all healthy at the cut (roster still growing).
  Self-terminate paths audited one by one and exonerated (OnGameEnd
  only runs at the end, ForceExit/time prints absent, time limits far
  above, launcher exits post-loop, server log silent, tail frames show
  healthy mid-game activity). Remaining suspect is host-side (engine
  ends an enemyless match) - needs C++ host debugging (owner call,
  shared platform). GAME_END twin stays as the witness for the next
  one. No blind patch.
- **#71** Readiness gate flipped (was blocking rich, name said poor):
  stub inversion pin 2/2. Predicate verified sound (fires at 700 s,
  1 base, bank under 550 - reachable per the 912 s `210002` run).
  Needs a 700 s+ game; the named
  `ORDER_SAVING_FOR_EXPANSION` line is the signature to look for
  (20261014_172954 reached only 324 s; `20261015_185046` reached 575 s
  in 141 s wall - heavy economies sim ~4x, so 700 s needs a lighter
  game or more wall). Absent in all retained runs incl. the 743 s one
  (3 bases there - the predicate correctly stays silent, it wants 1).
  `200123` is the first 1-base 700 s+ run (944 s, Nexus=1): ORDER_SAVING
  still 0, bank peak 8883 (predicate wants < 550) - correctly silent.
- **#81** Corner-stall Pylon search (top-right base [119,10]): 1953
  refusals, grid-modulo + doctrine kills (a mislabel fix already
  landed, zero behaviour). Needs the corner-stall game to decide
  doctrine vs leash-relax (owner call, same class as the zero-supply
  yield). Game happened (`WINE_LOG`, corner [119,10], 46 fails,
  Pylons still landed, supply unblocked to 33): ranked refusals are
  physical 91k, PTY-modulo 33k, PTX-modulo 7k, boundaries ~274 -
  leash never bound (doctrine wins on evidence, 99.5% tile rules).
  Suspects: 11x7 street grid (~30%, aesthetics not engine law, escapes
  only at free 0 / free<=5-banked-isolated - the 25/25 pain sat at
  free 6-7 just outside); physical footprint (~70%, mostly true
  negatives). Instruments: refusal-breakdown accessor +
  `PylonSearchBreakdownTest` (attribution plumbing) +
  `StreetGridDoctrineTest` (grid + both hatches + free-6 shut). The
  doctrine change itself stays owner call. Total failure since removed by a
  last-resort ladder (strict, relaxed margins, first engine-buildable tile
  in ring order): doctrine untouched, `PYLON_FIND_FAILED` prints only when
  all passes fail (`PylonLastResortTest` 2/2, RED then GREEN).
- **#84** Wine Pylon ordered but unbuilt: late-game instance (5:01 +
  38 s, bank hoarded) and early-game instance (READY from ~100 s, 9/9
  + 400 bank at log end). Worker stall, placement refusal, null
  builder and cancels all ruled out from flag-off logs. Needs a
  `BUILDER_LOG=1` Wine game (builder? site? no attempt?).
- **#74** Wine builder variance remainder (requeue/distances/debounce
  halves verified): needs a flagged Wine game for alternating
  Builder/Gather on a site and the exact 1.4-tile parking case.
- **#85** Armor unchained from weapons-L3 to L1 (stub pin); flat rich
  gates (~2x above L2/L3 engine costs) read coherent (Forge → weapons,
  rich checks, anti-spam, TooWeakToTech) - tuning, not defect, owner
  side of D5. Untouched. `TECH_CHECK` twin live (`170009`: 430 rows);
  the refusing gate named in the open: weapons wants 700 bank or 3
  bases (peak 517 here, 671 in `165345` - just under). Chain check
  2026-10-17: both clean games never reach the Forge trigger at all
  (supplyUsed peaks 42/33 < `buildAt` minimum 46), so research never
  starts for coherent reasons, not a stuck gate. `213248`: Forge built
  8:29 but post-Forge bank maxed at 641 < 700 (2 bases) - still
  correctly silent; pending a post-Forge 700-bank or 3rd-base window.
  743 s run hit
  bank 900-1300 from 10:58 and research STARTED at 11:16
  (beingRes through 11:39) but the wall clock ended the game before
  completion - still needs a longer window after research starts,
  not just a 700-bank moment. `200123` (944 s, bank peak 8883, NO
  Forge): TECH_CHECK 602 rows all false, UPGRADE_COMPLETED none -
  without a Forge the gate is correctly silent at any bank.
- **#82** Terran off-site completion may leak (unobserved, both Terran
  runs built on-site). Needs a game showing it. No blind fix.
- **#92** Second Gateway ~3 min late on Wine (BO `At 10` #3 → re-queued
  4:39 sup35, accepted 4:40+ six times same tile; first Gateway accepted
  1:09). OpenBW control (`20261015_141835`) warps both Gateways on time
  (1:14 sup9, 1:37 sup10). Instrumented, not patched: `BUILDINGS.json`
  ledger (warp time + supply vs order expectation, both runner paths).
  Needs a Wine game with the ledger - the late/missing Gateway row names
  the layer. No blind patch.
- **#94** Enemy never spawns on OpenBW (host auto-menu path): 6+
  enemyless games across TauCross/Python, Zerg/Terran (`enemy: ` empty,
  0/0, scouts find nothing in 18 min). Plumbing verified correct;
  failure is deeper (slot never reaches occupied/computer_game).
  Blocks all fighting proof (cluster FIGHT/FLEE, kiting, rush twins).
  Paths, cheapest last: owner-authored UMS map with a computer-slot
  enemy (no editor on machine); C++ host slot debugging (shared
  platform, owner call); Wine games with real opponents (works today).
  Detail: `_AI/CHALLENGES/StationaryEnemy.md`.

## Needs owner or upstream (blocked here)

- **#97** Own map-analysis library (owner direction 2026-10-18, full
  charter: `_AI/redesign/04_MAPLIB.md` - this line is only the pointer).
  Big separate project by design: pure map answers (model, defense spots,
  drop spots, wall anchors + natural wall-in with Pylon/Gateway/cannons),
  placement/execution stay in Atlantis; starts as `map.analysis`, extracts
  to a module when stable. Checklist: [x] M0 diet (dead ports deleted,
  3 fallbacks own, whole jbweb tree gone) [x] M1 immutable model
  (BaseRoles pure + wired) [x] M2 defense spots live (coverage ranker
  both paths, unpowered guard, 897 s proof) [x] M3 drop spots/islands
  (area graph pure) [x] M4 anchors model (tile row + door pure)
  [ ] M4 wall-in goal + game proof [ ] M5 module extraction (assessed
  2026-10-17: 7 files, engine-import-free except own map types +
  `AUnitType`; single engine-coupled adapter (`EngineGroundLink`) already
  isolated behind `GroundLink` - physical extraction is build/repo
  mechanics gated on owner timing). JBWEB fork
  (separate repo, walls/JPS only) plugs in at M4 if own anchors prove
  weak. Unblocks nothing above; nothing
  above blocks M0.


- **#95** Reserved-250 ghost, unresolved watch (2026-10-17): a deadlocked
  game showed global reserved=250 at supply 4 with bank 74 and nothing
  READY - no author found (READY/IN_PROGRESS paths audited, equality is
  value-based, feeder silent). Healthy games since show a clean
  trajectory (0 early, single-row earmarks late). Absent again in the
  743 s OpenBW game and the 2026-10-17 Wine game (the 250s there are
  per-row earmarks in flowing rich games: Dragoon row at 70/107 bank
  1256, Gateway ORDER_NOT_READY at 27/25 - live demand, not deadlock).
  If it reappears, the `reserved` column plus DECISIONS.log name it;
  until then no blind fix. Re-checked 2026-10-17 across all 12 retained
  runs: 5 per-row earmarks >= 250, all at supply 30+ in flowing games
  (lowest: Pylon 300 at sup 30/33 bank 10 mid-game) - the supply-4 /
  bank-74 deadlock shape absent again.
- **#96** Wine verification batch, owner-run: painter warping labels
  (`Gateway @12 [NOT_STARTED]`), WINE_LOG/_DEBUG/_DECISIONS triple,
  E2E_LOG/_DEBUG/_DECISIONS stable files, Missions/BO-mission unenaction sanity.
  Jar 12.1 MB already carries it all.
  Open tuning question for the batch: Pylon prepared at 15 warps ~2:24,
  past the 17/17 block - adoption+travel+warp (~25 s) outruns the 15→17
  window (~25 s), so any churn lands past the block (churn itself fixed
  2026-10-17; lever if still late is `IsReadyToProduceOrder` pull-forward
  amounts, currently +1 below 19 / +3 above - earlier pylons trade tech
  minerals for block time).

- **#34** Stub twins hold (4pool/9pool baselines pinned); a real
  opponent needs a scripted UMS map (one client per OpenBW host) -
  owner-supplied, tier stays owner-run.
- **#65 / #68** CherryVis code fixes are in (types, legend, seed,
  writer path, `--cvis` plumbing); what remains is the owner opening a
  real trace in the viewer (plus the `[BW<->CP Matching]` console line
  if `???` persists).
- **#77** Acceptance tier: 6 stale pins, all judgment calls (3 combat-
  eval numbers vs ADR 0006, ZealotSelfDefense open question, 2 Terran
  harness pins vs hardcoded race) - owner's call.
- **#78 / #79** Replay desync is verified upstream (OpenBW #27, 2019,
  second Linux confirmation 2021, open/unassigned). Live log is truth,
  replays are position-viewing only. Engine surgery out of scope;
  owner runs a separate model on fully-working OpenBW.

## Design and strategy (discuss without a game; code needs proof)

- **#45** Production v2 deletion ordering (safe order, each step green):
  LIVE-by-default verified in game first; dynamic commanders only after
  v2 covers tech + race army; `Queue/**` + order types last; never
  bottom-up (a bot that can't research is worse than debt).
  `Construction/**` stays by design (v2 delegates builders to it).
  Tech/army/gas coverage landed 2026-10-18 without a game (owner
  direction: snapshot fields + Dragoon advancement + Singularity want +
  gas want with exact-geyser siting, all stub-pinned; one DRY_RUN short
  run shows V2 planning Dragoons/Assimilators behavior-neutrally).
  Singularity live fire LANDED on V2 (`202456`, researched 5:16);
  V2 gas order fired too (Assimilator 4:08). Race-army gap named 2026-10-17: the advancement is a
  floor fill, so a standing Zealot army at/above workers/4 freezes the
  Dragoon transition forever (`202456`: Core from 4:35, 7 Zealots, zero
  Dragoons - demand-side, pinned in `V2DragoonAdvancementTest` 3/3);
  whether the floor should convert is an owner call.
- **#144–#147** Rewrite order, clean-room (owner direction 2026-10-17:
  Queue, site-finding, APositionFinder and SupplyCommander each properly
  planned and built anew, then the old path moves to src-old/). Safe
  sequence, never bottom-up (#45): each lands specced, stub-pinned,
  default-flipped with a game proof, and only then the legacy files move.
  - **#144** SupplyCommander (+ fallback bands) → the board supply goal
    owns all supply demand (DynamicGoals.supplyGoal + SUPPLY_BLOCKED twin
    already name the need; the feeder already feeds supply-pylon rows).
    Exit: one author, supply pumps silent, `SupplyCommander/**` +
    fallback in src-old/.
  - **#145** Site-finding chain (`NewConstructionRequest` + `FindPosition`/
    `DefineExactPositionForNewConstruction`, the "ConstructionFinder") →
    catalogue-only sited demand through the adapter funnel
    (03_PLACEMENT.md; exact tile honored verbatim, power + refusal twins).
    Exit: every construction sited by the catalogue, the chain in src-old/.
  - **#146** `APositionFinder` legacy path → deleted after the catalogue
    default + parity (D1 pointer; ~25 files still route through it).
    Exit: zero `Can't find place` across retained runs, legacy path gone.
  - **#147** `Queue/**` + order types → stateless demand/schedule
    (01_PRODUCTION.md scheduler + dispatcher; single policy already
    gated). LAST: a bot that can't queue can't play. Exit: V2 LIVE by
    default with parity numbers, queue in src-old/.
## Current game state (updated 2026-10-18)

- Long `20261017_213248` (79 s wall, 590 game-s, legacy rich: Nexus=2
  Probe=34 Pylon=9 Gateway=9 Zealot=10 Dragoon=10 Assimilator=2 Core=1
  Forge=1): Forge full-twin loop live - BUILD #123 at 8:29
  (supply 58, bank 483) → PREPARING [116,19] → BUILDING_STARTED →
  ledger warp 8:34 → roster Forge=1, zero silent steps. `GAME_END`
  first fire (won=false, 14058, 78/90, bank 453/308). Game ended 9:50
  unnamed (no force/leave/GG; #149 pattern, now WITH state).
  Post-Forge bank max 641 < 700 → research correctly silent (#85
  still pending a post-Forge 700-bank/3rd-base window). `BUILD_REFUSED`
  2740x (Gateway warping-link 762 new live, Pylon cap 1021,
  core-priority 796). `E2E_DECISIONS` clean (339 rows, maxframe ==
  game). 0/0 kills (#94).

- Long `20261017_210732` (46 s wall, 308 game-s, legacy: Nexus=1 Probe=24
  Zealot=4 Pylon=6 Gateway=2 Dragoon=1 Assimilator=1 Core=1): FIRST CLEAN
  `E2E_DECISIONS` (11 rows, maxframe matches the game - the pre-run rm
  fix verified). `BUILD_REFUSED` fired 1087x, all named-correct: Pylon
  poverty 204, pylon-cap 155, core-priority 682 (the Core landed - the
  priority rule works, documented live); Gateway poverty 46. Core BO#9
  waited 14 NOT_READY then warped. Gateway [49,107] engine-refused x2 +
  null-position x5 (loud, not a silent pile-up). Game ended 5:08 by
  unnamed cause (no force/leave/GG line; exit-path jstack fired post-game
  per the O3 pattern) - second occurrence of the `131407` shape, filed
  as #149. TECH_CHECK 126x all false (no Forge - #85 still pending).
  0/0 kills (#94).

- Long V2 LIVE `20261017_202456` (139 s wall, 1055 game-s, Protoss:
  Zealot=7 Probe=25 Forge=1 Pylon=6 Core=1 Assimilator=1 Nexus=1
  Gateway=2): exit 143 is the wall cap. 18 warps (11 buildings + 7
  Zealots, zero Dragoons). Tech coverage closed vs `131515`: Forge
  2:59@15, Assimilator 4:08@27 on the exact geyser tile (V2 gas order
  live fire), Core 4:35@31 - the frame-2 `Not powered x523/523`
  decline resolved once Pylons lit space. No Dragoon transition and no
  second Nexus in 17.6 min - remaining thinness drivers. Legacy pumps
  provably out (1 GAS_FIX, no DRAGOON/BOARD/SINGULARITY rows);
  `BUILD_REFUSED` 0 (V2 delegates builders past the twinned legacy
  door). NOTE: this run's `E2E_DECISIONS` is stale pre-fix accumulation
  (identical 12223 rows) - read V2 from EVENTS/ledger/roster only.
- Long `20261017_200123` (138 s wall, 944 game-s, 1 base: Nexus=1 Probe=22
  Pylon=7 Gateway=4 Dragoon=3 Zealot=6, single Core, NO Forge): exit 143
  is the wall cap (game healthy at the cut). 23 ledger rows (unit rows
  log train issues, not completions - the 4th Dragoon was still
  training at the cut, hence 3 standing). TECH_CHECK 602 rows all false with bank peak 8883
  (no Forge - the gate is correctly silent at any bank); ORDER_SAVING 0
  (1 base but bank >> 550 - correctly silent); scout idle 0, proximity
  twins flowing. 0/0 kills (enemy never spawns, #94).
- Long `20261017_173138` (140 s wall, 743 game-s, rich: Nexus=3 Probe=52
  Pylon=14 Gateway=11 Dragoon=11 Forge + 4 cannons): proximity-fix proof -
  same-tile Pylon re-requests no longer pile up silently (the [119,18]
  loop of the short game is gone; remaining re-requests land on
  `CONSTRUCTION_CANCEL ... proximity finish` twins instead of vanishing).
  No `Scout idle` in 12+ minutes (roam fallback live).
  Exit 143 (wall cap, not a bot crash) - game still healthy at the cut.

- Long `20261017_091624` (883 s, 55-unit cluster, no enemy spawned):
  Phase-3 movement proof - DEFEND→ATTACK at 8 army, anchors main →
  natural → enemy corner [9,45], group holds there; CLUSTER twin
  (posture/tactic/size/anchor) verified flowing. Fighting unproven:
  host spawned no enemy in 4 straight games (environmental).
  Unexplored-start fallback landed (else ATTACK sat home 18 min).

- Long `20261017_085719` (full 1209 game-s, macro: Nexus=7 Probe=86
  Pylon=23 Gateway=21, single Core): request-time rule proof - exactly
  ONE CyberneticsC (dynamic, 11:02@117) in a 20-minute game; no second
  site ever picked behind a coming Core. Creation caps + request rule
  compose.

- Long `20261017_084517` (127 s wall, 979 game-s, rich: Nexus=5 Probe=56
  Pylon=23 Gateway=7 Dragoon=14, single Core): absorption proof -
  opening script-clean (Pylon 0:50@8, Gateway 1:27@10, Gateway 1:48@12),
  2 Stargates warped (absorbed branches fire), SINGULARITY_CHECK x318 +
  APPLIES x4 flowing, BUILD_BREAKDOWN x0. Same numbers, new home.

- Long `20261017_080114` (58 s wall, FULL 1209 game-s, rich: Nexus=7
  Probe=56 Pylon=27 Gateway=9 Dragoon=24, cap-inconclusive): Phase-1
  proof - opening script-clean (Pylon 0:51@8, Gateway 1:25@10, Gateway
  1:53@12, Zealots, Core 3:02@23, ONE of each), exactly ONE
  CyberneticsC in 20 min (no duplicate Core/Tech - the outstanding cap
  holds live), reserved trajectory healthy (0 early, single-row 150
  late - the 0:07 250-ghost never reappeared with flow restored),
  DECISIONS.log 720 rows (26 BUILD + 9 ARMY from BUILD_ORDER, rest
  dynamic with reasons).

- Short `20261016_220831` (4 s wall, post-cutover boot proof): HELLO,
  Pylon=1 Gateway=2 Probe=12, 0 exceptions - production, placement and
  economy play with no combat/ linked. Scenario gate fails on army
  (no behavior yet, expected per __PLAN-COMBAT step 5).
- Long `20261016_195924` (43 s wall, defeat, Probe=25 Zealot=8 in 402
  game-s): WORKER-QUEUE PROOF - 16 TRAIN_ATTEMPT rows on busy bases
  (free=False, remaining 60→21), all result True; 9/9 blocks correctly
  refuse. Caveat, labelled hypothesis: queue depth never read above 1
  and the bank stood static across consecutive True trains, so the
  append itself is unproven on OpenBW (possible engine-fidelity gap,
  same class as regionCount/buildUnit - real-BWAPI semantics allow the
  append, JBWAPI checks pass). Wine twin decides; twin volume sane
  (36 rows vs 282 after 1/30 sampling).

- Long `20261016_173718` (139 s wall, 558 s game, rich: Nexus=2
  Probe=30 Dragoon=11): mandate-rule proof - Pylon 0:54 sup9,
  Gateway #1 1:26 sup10 at bank 2 (at-supply, no stockpile),
  `WORKER_GATE` x0 all game, benign choke markers only.
- Long `20261016_172341` (6 s wall, defeat 3:05, Gateway=2 Zealot=1):
  LVL verification - zero HIGH/CRITICAL rows (nothing severe; the
  opening played clean and lost a fight), `--min-level` filters
  correctly, E2E_LOG 0 diagnostics. Opening timings match the
  control to the second (Pylon 0:54, Gateway 1:19 sup9) - enforcement
  does not starve the opening.
- Long `20261016_164913` (141 s wall, 579 s game, monster: Nexus=2
  Probe=46 Stargate+Corsair, single Core): no ORDER_SAVING, scout
  idle x7 (busy game), benign choke markers only. `UPGRADE_COMPLETED`
  twin committed, verification running.
- Long `20261016_164340` (140 s wall, 563 s game, rich: Nexus=2
  Probe=38 PhotonC=3, single Core): tech-fix proof - ONE Core warp
  (3:11, original row #9, no re-request ever needed), WTF x0 in the
  whole game, ORDER_SAVING x0, 0 exceptions. The disease the two
  fixes address simply did not occur.
- Long `20261016_163658` (140 s wall, 605 s game, rich: Nexus=2
  Probe=33 Stargate=1, single Core): CLOSED #72 - `SINGULARITY_CHECK`
  shows research in progress 5:22-6:55, engine-researched from 7:07
  (static flag follows, 48 rows), no stuck flag. Scout idle x1.
  ORDER_SAVING x0.
- Long `20261016_163357` (FIRST PvT, 15 s wall, defeat 5:13,
  Probe=22 Zealot=3 Dragoon=1): Terran rushed us dead early - no
  baseline exists for this matchup (rush twins are Zerg-scripted),
  one game is not a defect per §3a, recorded as the datum to beat.
- Long `20261016_153026` (141 s wall, 593 s game, rich: Nexus=2
  Pylon=14 Gateway=6 Zealot=14, single Core): re-request fix proof -
  WTF 1x in the whole game (vs 7-in-10s), the 22x `At 60 Gateway*`
  rows are a working pipeline (3 warps 7:34/7:47/9:21, totalOfType
  pinned at 2), no same-tile spin, 0 real exceptions.
- Long `20261016_162455` (46 s wall, defeat 7:30, THREE standing
  Cores): duplicate-tech pathology live - stale overdue deaths
  re-ordered while siblings stood/warped. Fixed: covered tech skips
  the overdue requeue (`TechCoveredSkipsRequeueTest` 3/3); truly lost
  tech still requeues, pump covers dropped warps.
- Long `20261016_152205` (PvP tilt, 18 s wall, 336 game-s): short
  mirror, no #72 data. `SINGULARITY_CHECK` twin live from frame 61
  (all-false early, as designed). Scout idle x3, early residual.
- Long `20261016_151714` (141 s wall, 628 s game, rich: Probe=37
  Dragoon=13 Nexus=2, cap-inconclusive): lottery blank - no Core
  cancel (#88 went to `131201` instead), no `ORDER_SAVING` (#71),
  `SINGULARITY_APPLIES` x8 climbing to 348/372 then silence with no
  completion marker anywhere (#72: completion unobservable, hence
  the CHECK twin).
- Long `20261016_145027` (44 s wall, 423 s game, Probe=26 Gateway=4
  Dragoon=4): worker-flow proof - `WORKER_GATE` x0 in the whole game,
  Gateway #2 warped 1:19 sup9 at bank 30 (no 390 shape), 0 real
  exceptions. Probe warps leave no ledger rows (direct train orders),
  so continuity reads from the roster slope + zero refusals.
- Long `20261016_133939` (42 s wall, defeat 6:56, Nexus=2 Pylon=7
  Gateway=3 Dragoon=2): twin verification run - `CONSTRUCTION_PREPARING`
  x24, `CONSTRUCTION_CANCEL` x4, `PLACEMENT_FAILED` x1 (Gateway,
  unpowered), all queryable; `BUILD_BREAKDOWN` x0 (nothing stalled),
  E2E_LOG 0 diagnostics, 0 real exceptions. D2 fix regresses nothing
  (Core warped, no same-tile stall).
- Wine (owner live game): 600-mineral stall diagnosed - a Core
  construction survived its own warped building on its exact tiles
  (builder held 369f, `canBuild000=false`), shielded by the
  unworked-adoption exemption; same order re-requested ~12x pre-warp
  (transient occupancy cancelling). Fixed: exact-tiles finish above
  all exemptions (`StaleDuplicateFinishesOnItsTilesTest` 2/2). A second
  loop in the same game was sharper: a miner crossing the Core site
  cancelled it 7x in 10s (#126-#132, bank 230-308 vs 200 price),
  each minting a duplicate reservation until the bank froze near 600.
  Fixed: refresh cancels only on standing buildings
  (`RefreshCancelNeedsBuildingTest` 3/3, RED then GREEN). Live
  `20261016_135515_long` (590 s, rich: Nexus=2 Pylon=14 Gateway=6
  Zealot=19): 2 legitimate cancels in the whole game (vs 7-in-10s),
  no order requested 5+ times, `BUILD_BREAKDOWN` x0, `POSITION_REFRESH`
  twins flowing. The
  early Pylon wait (bank 90) was healthy just-in-time, not the disease.
  Diagnostics moved to keyed JSON twins, all on stderr; the one stdout
  polluter (`PREVENT ... Enforce Pylon first`) moved too.
- Long `20261016_132523` (93 s wall, 471 s game, rich: Pylon=9
  Zealot=13 Dragoon=7 Gateway=5): 7 BOARD rows, stable tops
  (supply-pylon x1, gateway/army/expansion x2 each - commitment
  holding, no flap), `BOARD_FED` sustained-dragoon x2, 0 real
  exceptions (3 choke-path trace markers, pre-existing).
- LIVE `20261016_131407` (9 s wall, 247 game-s, roster Pylon=2
  Gateway=1 Zealot=3 Forge=1, 0 exceptions): INCONCLUSIVE, not evidence
  - the bot exited at 4:07 for no logged reason with no enemy spawned
  (`enemy: ` empty). Retry in flight; if it repeats, the early-exit
  path (not V2) is the hunt.
- Long `20261016_131201` (61 s wall, defeat 7:17, Nexus=2 Pylon=8
  Gateway=3 Dragoon=2, 0 exceptions): CLOSED #88 (Core cancel 33 s at
  [16,44] -> requeue veto -> re-request [2,34] -> warp 3:02, roster
  CyberneticsC=1). Twins flowing (`SINGULARITY_APPLIES` x1, gas never
  held for completion).
- Long `20261016_130037` (141 s wall, 570 s game, rich: Probe=37
  Dragoon=11 Nexus=2, cap-inconclusive): lottery blank - no Core cancel
  (#88), no `ORDER_SAVING` (#71), `SINGULARITY_APPLIES` x3 firing at
  2 Dragoons + gas 180-204 but bank never holding 150/150 (#72
  unproven); twins flowing (`GAS_FIX` x5, `DRAGOON_TRAIN` x13).
- Owner long `20261016_104914` (corner spawn [119,10], Nexus=2 Pylon=9
  Gateway=4 Dragoon=6, cap-inconclusive): 7x scout-idle (now terminal
  fallback to unexplored natural/third/fourth), 12x `PYLON_FIND_FAILED`
  + 3x `Can't find place` at supply 21-27 (now last-resort ladder;
  later Pylons landed anyway once the natural opened ground).
- Last recorded `--long` pass: 210002, 912 s, exit 0 (Nexus=6 Dragoon=7
  Forge=1, first ever; gas 270+; Singularity correctly idle at one
  Dragoon). Log not retained.
- Today: short FAILs its gate (4:40 game-time in 30 s wall, dragoons=0
  vs 1 expected); long FAILs its gate twice (8:51 with dragoons=6
  zealots=12; corner spawn [119,10] with 42x `Can't find place` -
  #81 reproduces). The 13:24 runs had 0 placement failures.
- Feeder: first `BOARD_FED` observed `20261015_143903` (supply-pylon at
  a 39/41 block); 0 in all other retained logs.
  `ORDER_SAVING_FOR_EXPANSION` 0 everywhere (string present in
  `IsReadyToProduceOrder:76`, never fires - `20261015_150026` long run
  included). Raw counts, no diagnosis.
- Long `20261015_150026` (8:43 game-time, 0 placement failures, 20
  ledger rows): Gateway BO #3 warped twice (3:36 sup23, 4:12 sup27);
  dynamic Gateway #143 cancelled by refresh at `[94,111]` and
  re-requested; no Core cancel (Core warped 2:36, #88 still needs its
  game); Forge warped 6:56.
- Long `20261015_185046` (141 s wall, 575 s game, 0 kills, rich
  economy: Nexus=2 Probe=31 Pylon=13 Gateway=5 Zealot=15 Core Dragoon=6
  Forge; shutdown-hook summary worked): `SINGULARITY_APPLIES` firing,
  completion unproven (#72); 2 Pylon refresh-cancels, no Core cancel
  (#88), no `ORDER_SAVING` (#71); hang-watcher fired once on
  busy-compute (57 s CPU, not deadlock).
- Long `20261015_170339` (guarded jar, 9:30, 49 warps, 1 Assimilator
  refusal + 1 refresh Gateway cancel): Gateway adoptions sparse against
  120 same-tile accepts - the adopted-order path is closed, the live
  loop is not (see #57). Quiet `20261015_165943` (7:22, 42 rows, no
  cancels, Gateways on time at 9/10): clean baseline, nothing to fix.
  Per-add attribution without stacks since `CONSTRUCTION_REQUESTED`
  rows (`20261015_185504`: all 4 via `ProduceBuilding.produceBuilding`).
- Long `20261015_191932` (141 s wall, 539 s game, rich economy, 44
  warps, shutdown summary worked): same-tile accepts max 6-in-2-s
  bursts converging to warps (was 41-121 sustained) - closes #57.
- Watch datum (unattributed): `PhotonC ADDED TO QUEUE` x15 between
  8:28-8:51 game-time (`20261015_132452`). Candidate mechanism fixed:
  fortification demand ignored PREPARING sites and re-fired rows faster
  than builders arrived (now counted); rows predate the ledger so the
  shape itself stays unconfirmed.
- Shutdown-hook summary proven `20261015_181553`: a wall-killed short
  run now ends with `### Stopped (no game end reached) after 302
  seconds. ###` + roster instead of mid-frame silence.
- Long `20261016_104113` (140 s wall, cap-inconclusive, Gateway=3 Core=1
  Assimilator=1 Zealot=5): first game on the event twins - `GAS_FIX`
  ok-row for [86,118] at 3:55, old strings 0 in both logs, Assimilator
  warped with no cancel cascade, 0 exceptions. The two `WTF` lines are
  Gateway (non-gas path, re-queued by design). `DRAGOON_TRAIN` /
  `SINGULARITY_APPLIES` unfired (no Dragoons) - code-verified, live fire
  pending a Dragoon game.
- Wine first Pylon still unbuilt in both instances (see #84); the
  convertible-waiters fix plus walking-builder shield are in the jar,
  final proof is the owner's re-run.
