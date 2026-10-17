# TODO — Atlantis (open issues)

Living backlog for the architecture migration (stages A–D done, E–J open
below) and for defects found along the way. This file is the single source
of truth for "what is left"; `_AI/NOTES.md` keeps operational learnings.

## Current owner priorities (2026-10-09)

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
   supply/gateway/core/assimilator. The pumps do ~everything, the
   feeder fills silence - wrong direction for deletion; buildings are
   all-legacy. Board-invisible production (Forge, cannons) is
   capability, not duplication. Deletions wait for routine feeder
   coverage, never batch rewrites.
2. **Decision absorption:** board takes over decision sites one by one
   (20 files with the exact singular import, measured 2026-10-17 -
   was 47: 24 died with the combat cutover, 1 orphan (TransitionToCorsairs)
   absorbed; counting down, no new ones per §21); the class dies at zero,
   not before. Two tracks:
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
   (no per-unit micro on it).
3. **Pylon closure:** Wine re-run with the current jar (convertible
   waiters + walking-builder shield are in it) for the first-Pylon
   proof (#84); corner-stall game for doctrine-vs-leash (#81).
   Owner-run; autonomous work above does not wait on it.
4. **Game-proof backlog** (all need a quiet run, OpenBW or flagged
   Wine): 700 s+ rich run (#71), L2/L3 (#85), builder-variance
   leftovers (#74).
5. **Second goal vocabulary** (Terran or midgame): triggers the
   `goals/` split by vocabulary - until then no new packages.
   Assessed 2026-10-16, no clean midgame slice: tech goals preempt
   #45 ordering, fortification is planner-owned (two sources of truth),
   observers already have a pump - vocabulary grows on owner
   direction, not on availability.
6. Unchanged long-term ordering: #45 deletion order, #8 V2 LIVE runs,
   stages E–J below.

## Test health

Suite green 2026-10-18: 615 passing, 1 skipped (decision-board migration:
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
  until then): `ProcessAttackUnit:56`, `BuilderAvoidEnemies:27`,
  `DeadMan:25`, `AFocusPoint:111`, `DoAvoidEnemies:30`,
  `AtlantisJfap.isValidUnit:71` (borderline). The rest was triaged out
  (same object, worse GC) or dies with lifecycle.

## Stage F — cache purge

- Five unexplained TTLs above one frame (30, 31, 53, 73, 293) remain
  (91 went with the deleted `firstBuilding` entry);
  `Select.main()` (90 call sites) is cached 2.4 s. Inventory:
  `DOCS/SELECT-CACHES.md`; neutral cleanups done per
  `_AI/work-orders/WO-F-neutral-cache-cleanup.md`.
- **#5** Per-frame query service in `core.world` with explicit per-frame
  invalidation. One cache at a time, fewest readers first.
- **#6** Delete migrated static caches from `Select` (no shims, rewire
  call sites in-commit); re-freeze the ArchUnit store only for genuine
  removals.

## Stage G — race strategies

- **#8** Seam + one end-to-end subsystem migrated as proof (ADR 0007
  accepted; pilot: `ProductionRacePolicy` + 3 impls, V2 DRY_RUN zero
  errors). First LIVE probe `20261015_171504` (short): attaches, plays,
  builds opening (2 Pylons, Gateway, 3 Zealots, Forge, Assimilator), no
  crash, no exceptions. First LIVE victory `20261015_193721` (long,
  755 s, 5/0 kills, +500): full game to natural end on V2, 8 warps, all
  rows minSupply 0 (no BO expectation reaches the ledger - the deferred
  timeline scheduler). Two watches: Pylon #1 warped at sup5 twice now
  (pull-forward or waste - undecided, needs a side-by-side); Forge
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
- **#10** Split `Selection` (235 public methods) along internal seams,
  with a test pinning selection semantics before/after.
- **#11** Split `AUnit` (~623 methods): order-emission surface first
  (`AUnitOrders` half-owns it), then per-frame orchestration, then
  per-condition logic.

## Stage I — physical migration

- **#12** Package move to bounded contexts per
  `DOCS/ARCHITECTURE-CONTEXT-MAP.md`, mechanical and separately
  revertable per context, only after F–H.

## Stage J — fortress

- **#13** Zero-violation baseline, then flip the frozen store to hard
  rules. The 255 + 24 are structural (unit-centric Commander/Manager
  framework → Stages E/H), not a sweep; the information rule triages to
  live strategy-response logic (Stage G seams, not line deletions).
  Traps, paid for: a change must remove violations from one rule, not
  move them into another; `rm -rf out` before architecture runs after
  moving classes (`javac -d` leaves stale `.class` files behind).
  Detail: `_AI/work-orders/WO-13-mechanical-archunit-sweep.md`.

## Needs a game (quiet OpenBW run or flagged Wine run)

- **#71** Readiness gate flipped (was blocking rich, name said poor):
  stub inversion pin 2/2. Predicate verified sound (fires at 700 s,
  1 base, bank under 550 - reachable per the 912 s `210002` run).
  Needs a 700 s+ game; the named
  `ORDER_SAVING_FOR_EXPANSION` line is the signature to look for
  (20261014_172954 reached only 324 s; `20261015_185046` reached 575 s
  in 141 s wall - heavy economies sim ~4x, so 700 s needs a lighter
  game or more wall).
- **#81** Corner-stall Pylon search (top-right base [119,10]): 1953
  refusals, grid-modulo + doctrine kills (a mislabel fix already
  landed, zero behaviour). Needs the corner-stall game to decide
  doctrine vs leash-relax (owner call, same class as the zero-supply
  yield). Game happened (`last-wine.log`, corner [119,10], 46 fails,
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
  bases (peak 517 here, 671 in `165345` - just under). Needs a
  700-bank game proving L2/L3.
- **#82** Terran off-site completion may leak (unobserved, both Terran
  runs built on-site). Needs a game showing it. No blind fix.
- **#92** Second Gateway ~3 min late on Wine (BO `At 10` #3 → re-queued
  4:39 sup35, accepted 4:40+ six times same tile; first Gateway accepted
  1:09). OpenBW control (`20261015_141835`) warps both Gateways on time
  (1:14 sup9, 1:37 sup10). Instrumented, not patched: `BUILDINGS.json`
  ledger (warp time + supply vs order expectation, both runner paths).
  Needs a Wine game with the ledger - the late/missing Gateway row names
  the layer. No blind patch.

## Needs owner or upstream (blocked here)

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
## Current game state (2026-10-15)

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
  correctly, bot.log 0 diagnostics. Opening timings match the
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
  bot.log 0 diagnostics, 0 real exceptions. D2 fix regresses nothing
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
