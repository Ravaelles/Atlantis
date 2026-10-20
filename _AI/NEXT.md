# TODO — Atlantis (open issues)

Living backlog for the architecture migration (stages A–D done, E–J open
below) and for defects found along the way. This file is the single source
of truth for "what is left"; `_AI/NOTES.md` keeps operational learnings.
The direction everything here converges to lives in
`_AI/00-GOAL-APPROACH.md` (board-first loop, layers, growth rules);
the execution order for the current quarter in `_AI/__MIGRATION.md`
(combat-first, then legacy deletion).

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
  2026-10-20: FIRST V2 LIVE GAME ON WINE (11.8 game-min vs Garm
  Brood, WON 37/15 +300 - archived under out/wine_runs/): Pylon
  0:32, Gateway 1:27, Forge 1:54, Assimilator 3:22, Core 4:03,
  Dragoon 14, Zealot 5, Gateways 3, Pylons 6 - tech/army flow
  SUPERIOR to same-day legacy (Forge 6:58, NO Core, 0 Dragoons,
  lost 7/29). 701 V2_DISPATCH + 2735 V2_GOAL_SOURCE fire. Gaps:
  no Nexus#2 (C14 expansion siting, V2 has none), Pylon demand
  dual-authored live (build-order 1633 + dynamic 1102 rows -
  WO-144 single-author still open). N=1 per side, different Zerg
  play - direction stark, parity unproven.
  Diagnosed 2026-10-19 (mechanisms named with run ids; fast-loop slices
  landed after, each stub-pinned, no games per owner direction):
  full 1209 s LIVE games play fast but thin (10 warps: Pylon=2 Gateway=1
  vs legacy ~40/9). (a) Core unplaceable: planner refusal twin proves
  power (`Not powered by a Pylon x517/517`, `20261017_135655`) - V2
  pylons land wherever, powered space fills, tech starves; legacy
  avoided this with pylon scoring (unwired `PylonPlacementScore`) and
  pull-forward. LANDED stub-pinned: `LegacyPlacementPlanner` re-queries
  validated ring tiles around the first answer and picks max
  `PylonPlacementScore` (`PylonPowerSiting` + `PylonPowerSitingTest` 4/4
  + planner wiring test; no invented candidates, ties keep the finder).
  Game proof (parity runs showing powered tech ground) still pending. (b) Legacy queue machinery fires on unit events inside
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
  Short datum 2026-10-18 (`103411`, V2 LIVE, 20 s bot-cap, gate
  unreached - inconclusive per §17, but the shape matches (b) above:
  481 CONSTRUCTION_CANCEL + 31 SUPPLY_BLOCKED in 629 rows, 0 warps).
  Long datum 2026-10-18 (`143419`, V2 LIVE, 155 s wall, 1062 game-s:
  Probe=25 Zealot=7 Pylon=6 Gateway=2 Core=1 Forge=2 Assimilator=1
  Nexus=1, zero Dragoons): 19 warps, ALL within the first 5.6 min
  (last warp frame 8072, 3.39 warps/min), then 12 min of silence.
  The silence is one Nexus order spinning: 17114 proximity-finish
  cancels at [117,12] (standing Nexus covers it) - the C14 shape from
  `202456` reproduces, worse (16k -> 17k). Dragoon=0 with 7 Zealots
  is the floor freeze live again; 2 Forges is duplicate tech on the
  V2 path; supply ends 18 free / 0 coming (no block - demand, not
  supply, is thin).
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
   no candidate yet (history in git: 10-16 zero per-gate rows, 10-17
   nine sustained-army rows, 10-18 one supply-pylon rescue in 214926 -
   never routine). Re-measured 2026-10-19 across the 3 fresh runs:
   0 BOARD_FED everywhere, 83 BUILD rows all legacy/demand-direct -
   the feeder still fires nothing; no candidate. 4th run same day
   (`105921` long, 16546f/11:34, exit 143 wall-cap): BOARD_FED 1
   (sustained-dragoon 5:16, pumps already training since 4:12, zero
   BOARD-authored DECISIONS) - rescue, not routine; no candidate. 5th
   run 2026-10-20 (Wine FE short): BOARD_FED 3x on 3 different
   demands - gateway 2:45, assimilator 4:53, supply-pylon 5:38 -
   every one built within 600 frames (ORDER_NOT_READY clears,
   CONSTRUCTION_REQUESTED +13f, BUILDING_STARTED +113f); pumps
   silent on all three. First routine-shaped evidence: nominates
   the legacy demand producers behind those rows for per-gate
   deletion (proof game after the cut still required). Deletions
   wait for routine feeder coverage, never batch rewrites;
   board-invisible production (Forge, cannons) is capability,
   not duplication.
2. **Decision absorption:** board takes over decision sites one by one
   (7 files with the exact singular import, re-verified 2026-10-20:
   DTDontAvoid's import was dead (only commented-out references) and is
   deleted - the rest still 7 on the same excluded tracks, production
   holds ZERO - was 47: 24 died with the combat cutover, 1 orphan
   (TransitionToCorsairs) absorbed, 14 more with the targeting + battle
   src-old sweeps; counting down, no new ones per §21); the class dies
   at zero, not before. All 7 survivors sit on excluded tracks
   (battle/micro, interrupt, terran-info) - production holds ZERO imports
   (re-verified by grep 2026-10-20). Two tracks:
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

Re-measured 2026-10-19 (`rm -rf out` first): unit 894/894 green, 0
skipped (was 763, then 783 - live-guard, demand-context,
standing-cover, adopt, sited-door, forecast, rally and twin pins
landed since; acceptance 17 reds byte-identical between the
pre-session baseline worktree (6ae7ec1f) and the current tree -
verified by worktree diff this session, zero regressions from it;
counts rot, re-measure before quoting). 2026-10-20: unit 994/994
(BuildGate registry + order test, EarlierReserve divergence pin,
SelectionViews slices 7-8, AUnitHealth slice 1, CombatTuning move +
holder tests, Core gate oldest-wins fix, FightTrace + table test,
PylonNeedsRoomAround 4/4, BaselessAttackExplores 2/2; 2026-10-21:
unit 991/991 (new tests minus the 16 OpenBWLauncherTest moved to
the full tier by tag; acceptance still 17 reds byte-identical to
the baseline every run - re-verified by worktree diff).
Architecture advisory
9/11, strict gate 5/7 (was recorded 10/11 - record was stale): two
rules fire, both pre-existing unfrozen edges, neither fixed here
(both carry live behavior, Stage E/H territory, never grow the
store): `architectureMustNotDependOnFeatures` via
`Manager.invokeFromParent` → `SleepUntilFrameGate.mustSleep` (sleep
gate, #67) and `coreMustNotDependOnOtherContexts` via
`SpecialCommander.subcommanders` → `ForceExitWhenInvalidState`
(added 2026-10-17). Store diff empty after both runs. Re-run
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
  until then): `DoAvoidEnemies:30`, `AtlantisJfap.isValidUnit:72`
  (both live, both on battle tracks - they die with the manager-tree
  migration, stages E/H, never through a delegation switch).
  `BuilderAvoidEnemies`, `DeadMan`, `AFocusPoint`, `ProcessAttackUnit`
  are gone from `src/` (combat cutover, now `src-old/`). Re-verified
  2026-10-19: two live readers beyond the item's list, both out of
  scope for a delegation switch - `BuilderAvoidEnemies.defineEnemy`
  falls back to `FoggedUnit` when no enemy is visible (builder micro,
  dies with the manager tree), and `OffensiveTurrets:41` is a commented
  reference, not a reader. No switch; the item still waits on E/H.

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
  with a test pinning selection semantics before/after. Five slices
  landed (radius family → `SelectionRadius`, membership filters →
  `SelectionMembership`, ordering → `SelectionSorting` in two parts:
  sorts then nearest-finders, then element accessors → `SelectionAccessors`:
  first/last/random(+seed), each with a pin test, zero call-site
  changes; sixth slice 2026-10-20 (container views → `SelectionViews`:
  list/units/count/size/isEmpty-family, `SelectionViewsTest` 3/3,
  sensitivity-verified); seventh slice the same day (list-mutation
  family: add x3, exclude x3, reverse, removeDuplicates, limit,
  countOfType, +4 tests); eighth slice (aggregates totalHp/totalHits/
  averageShields, +1 test): cross-context readers stay put until their dependencies are
  fixed - moving them re-spells frozen violations instead of removing
  them (measured, see NOTES.md arch mechanics; slices verified:
  same 2 pre-existing failures, store diff empty). Remainder is
  container views (`units()`, `list()`, `size/count/isEmpty`),
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
  the split starts at per-condition logic, if at all. First slice
  2026-10-20: the hp/wound predicate family
  (`AUnitHealth`: isFullyHealthy, hpPercent x2, woundHp,
  woundPercent, woundPercentMin/Max, isWounded, `AUnitHealthTest`
  3/3) moved verbatim with one-line delegates, zero call-site
  changes - the Selection-slice recipe.

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
  live strategy-response logic (Stage G seams, not line deletions -
  35 entries after 7 dead-code deletions 2026-10-19
  (RaceStrategyResponse anti-air/anti-land managers: dispatch
  commented out since 75d7650f, zero callers, zero test refs - the
  same-day "all 42 verified structural" reading was wrong on those
  7); rest are live Terran/Zerg responses and Strategy.setTo
  framework edges);
  the util mechanical rule stands at zero (store empty).
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
  warps; self-test RED/GREEN on fixtures; live proof LANDED 2026-10-18
  (`104922` short, 19 s wall: Pylon STARTED 0:47@8, Gateway 1:20@10 +
  resite 1:42@12, 18 ledger rows, SCENARIO PASSED with the gate
  exported - the 30 s cap finally let one through).
  Writer-level fix landed: EventLog/DecisionLog truncate on first
  flush per JVM (pinned, incl. simulated restart), so no launcher
  can accumulate games anymore - scripts keep their pre-run rm as
  belt-and-braces. Wine logs were unusable for
  all of this (7 MB EVENTS.jsonl spanning 6 games) - fixed by clearing
  the write dir pre-game like OpenBW. Wrong-sibling adoption stub-pinned
  2026-10-18 (overlap test: positional, then fallback candidates=1, then
  CANNOT exit - `AdoptedConstructionTwinTest` 3/3, RED then GREEN); the
  live overlap game is still unplayed. Chain twins carry cid/oid since
  2026-10-19 (REQUESTED/PREPARING/STARTED/CANCEL/ledger/ADOPTED +
  orphan-drop + query `--where cid=`), and the first live run with them
  (`150006` short) caught a cross-order same-tile double-PREPARING:
  dynamic Pylon cid=63 and BO Pylon cid=64, same tile [79,107] 3 frames
  apart; 64 STARTED and warped, 63 left zero further rows through frame
  7527 (orphan vs silent drop - the new drop twin decides on the next
  run). Pick-time guard (`overlapsUnfinishedSite`, same-file spiral)
  should have refused the second pick; why it did not is the next
  measured branch - no blind patch. FE scoping landed the same day
  (wall-in only on Forge Expand; live `104922` on non-FE shows zero
  wall-in BOARD/wall rows). Remainder: owner visual proof of orange
  PREPARING (a). Live overlap game still unplayed; the resite seam
  fires live (adopt-resited 1x in each of two long runs). No blind patch.
- **#151** Dynamic demand re-requests a proximity-covered tile forever
  (Wine TauCross, oid=118 "At 32 Gateway*": 255 chain rows 4:24-4:50 -
  REQUESTED then proximity-cancelled the same frame, ~20 constructions
  on tile [105,110], several STARTED with a builder then killed the
  same frame because standing Gateway [103,112] grazes the footprint
  2x1 tiles). Second instance, fully attributed (Wine, decidable now
  that BUILD rows carry oid+context): oid 72 "At 30 Gateway*" 39 BUILD
  rows + oid 78 "At 37 Gateway*" 35 rows, every one a fresh cid on
  [116,19] cancelled same-frame against standing Gateway [114,18];
  both eventually warped elsewhere ([116,23] 5:07, [123,26] 5:38).
  Landed 2026-10-18: spiral checklist refuses standing-covered sites
  (mirror of the finish overlap half, stub-pinned 5/5); PREPARING
  adopts still walk into cover (Pylon #11: 2 adopts on [119,12]) and
  the sited path re-requests uncovered (PhotonC [115,15] 21x) - those
  two doors stay open. 2026-10-19: the prepare-side unlink is twinned
  (`prepare-site-overlap`, stub-pinned `PrepareOverlapTwinTest` - FE
  run `112337` burned 40 construction ids for 1 flushed PREPARING;
  the missing rows fit the 720-frame flush buffer lost on the kill
  as well as silent unlinks, so the twin (not the datum) decides
  the next occurrence. No merge of the two overlap checks: read
  2026-10-19, they cover disjoint worlds by construction (spiral
  reads live units - warping buildings whose reservation is gone;
  pick-time reads reservations - PREPARING with no unit yet),
  sharing only the footprint math. Adopt side re-sites since the
  resite seam (same [119,12] case cited in code), proof pending
  like the rest. Proof: a covered-tile game where the loop is gone
  (long OpenBW 16575f: new refusal 0 fires, no same-tile storm).
  2026-10-20: two short Wine games too quiet for new instances
  (supply-50 exits, no construction twins past the opening) -
  still awaiting the covered-tile game.
- **#152** Dragoon kiting: won 1v1 live repeatedly (Wine 1Drag_v_1Zeal,
  owner 2026-10-19; now the overnight tuning duel). Landed since:
  HIT twin (lost/dist/cooldown/moving/positions per hit) + SHOT twin
  (shot-fired vs holding-shot) + per-cycle fight-timeline + per-game
  digest with invariant flags (HITS_TAKEN <= 2, shot cadence vs 30f);
  margin rule with targeted+facing terms; no-reissue + ride discipline;
  fromBest escape choice; glue-shoot; frozen-STOP reset; no-respam step
  cadence (10f, Stardust latency+6); micro-step gain gate; Stardust
  desired-distance hold (no fixed 2.0-tile shove past the firing point -
  pinned, sensitivity-verified vs old behavior). Open: (a)
  wall-aware steps (PurpleWave `MicroPathing.getThreatAwarePath`
  direction, see `_AI/CHALLENGES/MicroKiting.md` for file refs -
  deferred for now, production first, owner 2026-10-19);
  (b) macro-game kite proof (OpenBW spawns no enemies; needs a Wine
  macro game, owner-run): 1v1 proof does not cover focus fire,
  traffic, or the retreat latch (8v8 caveat, measured: human-slot
  UMS maps like `8Drag_v_8Drag` spawn nobody on Wine either - 8 own
  Dragoons idle, 0/0 - the same unfillable-slot class as #94; only
  maps whose enemy actually spawns fight - so far only the 1v1 Zealot
  does); (c) the overnight tuner
  (`scripts/run-wine-batch.sh`, ParamTweaker randomizes 12 kite
  coefficients in-JVM, CSV scoreboard: W/L, seconds, killed, lost,
  hits, shots, values) decides the numbers - see `_AI/TOOLS.md`.
  2026-10-20: all CSV hits/shots/score columns before this date are
  remnant-corrupted (evaluator counted the post-720f-flush buffer, not
  the game - measured 7 vs 33 filed SHOT rows; now flushes and counts
  the file, batch copies the fixed-name row deterministically).
  Mechanism re-verified the same day (two Wine 1v1 duels produce
  countable flushed logs: 46/32 SHOT rows from file, VICTORY + own
  14-hits counter in the log); full 10-game batch 2026-10-20:
  10/10 WINS ~20 s each, hits 6-19 (avg 11.4), tuner randomizes per
  game through the CombatTuning record (incl. the 3 new pinch
  params); the overnight batch stays a standing vehicle for the
  real sweep. 2026-10-20 UMS duel on the fixed anchor rule
  (baseless ATTACK marches from the army center): WIN 1/0 in 499
  frames, 34 stepped-aside vs 9 stuck, cadence 33 - the idle-dragoon
  shape is gone.
  Related open question (WO-144): which goal re-fires V2 Pylon pairs
  (BO row without coming-suppression vs pipeline overlap vs door hole)
  - goal-source twin LANDED (V2_GOAL_SOURCE on provider goals at
  collectGoals, lists tag at creation, pinned V2GoalSourceTest 5/5):
  the next live pair reads its authors off the source column.
- **#154** Per-unit attack readiness (owner 2026-10-20, Stardust over
  PurpleWave for single-unit micro): attack iff `cooldown <=
  latency(unit) + 2`, where latency is strictly per-unit (reaction +
  remaining engine frames) - never a flat number, and never the
  PurpleWave-copied `UnitAttackWaitFrames.effectiveStopFrames` table
  (per-type constants, currently half-dead: `unitAlreadyStartedAttackAnimation`
  returns false unconditionally with its body commented out (one live
  caller, `ProtossContinueAttack:108`), `waitedLongEnoughForStartedAttack`
  and `AUnit.attackWaitFrames` have zero callers - audit will likely end
  in deletion, not rewiring). 2026-10-20: interim constant GONE -
  the rule reads the live engine latency per decision
  (`SleepUntilFrameGate.latencyFrames`, fallback 3 gameless; +2
  Stardust margin at the call site as before); Wine proof the same
  day: two 1v1 duels, WIN 1/0 both, shot cadence median 33 frames
  (~30f cooldown + latency, intact), movement 37:18 and 31:3
  stepped-aside vs stuck-no-step, bot's own counter 14 hits duel 1.
  2026-10-21: audit ended in deletion as predicted - the whole
  `UnitAttackWaitFrames` table is gone (zero callers for
  `waitedLongEnoughForStartedAttack` / `AUnit.attackWaitFrames`;
  the one live caller read a method returning false unconditionally,
  so its branch never fired - removed with a comment, zero behavior
  change, suite 991/991 + arch 5/7 unchanged). The live rule stays
  `cooldown <= latency(unit) + 2`, read per decision.
  Stub-pin the rule, then prove shot cadence on the Wine
  1v1 duel (SHOT twin already carries fired-vs-holding). Details:
  `_AI/CHALLENGES/MicroKiting.md` ("Stardust reference").
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
  healthy mid-game activity). 4th occurrence 2026-10-18 (`144516`:
  43 s wall, 438 game-s, healthy 56/73 bank 631/366, Forge@9210,
  enemyless, GAME_END state attached, zero force/leave/GG/tripwire
  lines - same shape). Remaining suspect is host-side (engine
  ends an enemyless match) - needs C++ host debugging (owner call,
  shared platform). GAME_END twin stays as the witness for the next
  one. No blind patch. 2026-10-20: no occurrence in 4 live games
  this turn (2 Wine macro + 2 Wine UMS duels - all ended by kill
  or VICTORY with tails, none silent). Contrast datum 2026-10-18 (`144920`: host died
  visibly at 846f/35s - client printed `failed, disconnecting` + `No
  server proc ID`, hang-watcher jstacked, runner ended at 72s). The four
  #149 runs show NONE of those lines: when the host dies the client
  says so, so their silent ends are not a host crash of this shape -
  the game ended while both sides looked alive, and only C++ host
  debugging can say why.
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
  Lottery 2026-10-19 (`154329`: 428 s, Nexus=1): still short of 700 s,
  never a saving shape.
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
  Implemented 2026-10-20 (`PylonNeedsRoomAround`: with SupplyFree>=2
  the four +-4 diagonal tiles must be walkable; poor supply bypasses;
  wired right after TooCloseToOtherPylons, pinned 4/4) - proof
  2026-10-21 on 7th_rav (Wine, hidden, fresh jar with the rule +
  anchor fix + pinch in it): 6/6 Pylons warped, first 0:50, zero
  supply blocks, zero room-around refusals fired, opening healthy
  (Gateway 1:20, Core 2:23, Dragoon 3:26). The map never offered a
  true corner-stall shape (mid-base tiles [112,29]/[108,25]/[118,34]),
  so this is a no-harm proof, not a firing proof - the firing path
  still rests on the 4/4 stub pin until a real corner shape appears.
  No misfire observed; the rule stays.
- **#84** Wine Pylon ordered but unbuilt: late-game instance (5:01 +
  38 s, bank hoarded) and early-game instance (READY from ~100 s, 9/9
  + 400 bank at log end). Worker stall, placement refusal, null
  builder and cancels all ruled out from flag-off logs. Needs a
  `BUILDER_LOG=1` Wine game (builder? site? no attempt?). First datum
  2026-10-19 (TauCross, BUILDER_LOG=1): Pylon#1 reserved worker #5 at
  pick 0:13, STARTED 0:46, warped [16,46] 0:58 - pipeline healthy on
  this game; the two named instances were other games.
- **#74** Wine builder variance remainder (requeue/distances/debounce
  halves verified): needs a flagged Wine game for alternating
  Builder/Gather on a site and the exact 1.4-tile parking case.
  Instance observed 2026-10-19 (TauCross Wine, BUILDER_LOG=1): a Pylon#1
  builder pathing-stalled at map edge [3,47] for ~90 frames (MOVE order
  re-issued, distToSite frozen, BUILD_BREAKDOWN x2) and self-recovered -
  the Pylon warped on schedule. Transient stall, twin worked as
  designed, no code change (would be blind). The reservation→starter
  link reads cleanly off the new cid/oid chain on the next flagged run.
- **#85** Armor unchained from weapons-L3 to L1 (stub pin); flat rich
  gates (~2x above L2/L3 engine costs) read coherent (Forge → weapons,
  rich checks, anti-spam, TooWeakToTech) - tuning, not defect, owner
  side of D5. Untouched. `TECH_CHECK` twin live (`170009`: 430 rows);
  the refusing gate named in the open: weapons wants 700 bank or 3
  bases (peak 517 here, 671 in `165345` - just under). 2026-10-18: the
  Python window (Forge 3:58, bank 784, TECH_CHECK false at 772/306)
  is refused one gate earlier - `combat-10` (8 combat units; the bank
  clause never evaluates). Named live from now on: the TECH_CHECK twin
  carries `refusedBy` (Weapons + Armor, pinned in
  `ProtossUpgradesLevelGatingTest` 4/4). Tuning, still owner side of D5.
  Chain check 2026-10-17: both clean games never reach the Forge trigger at all
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
  2026-10-19 (`185237`): refusedBy names the gates live (no-forge 204,
  bank-700-or-3rd 93, weapons-l1 108) - the twin works, tuning stays
  owner-side (D5).
- **#82** Terran off-site completion may leak (unobserved, both Terran
  runs built on-site). 2026-10-18 datum (`143800`, Terran vs Zerg, 212
  game-s): the bot plays Terran with the Protoss build order (`Zealot
  into Goon`) and builds nothing at all (CommandC=1 SCV=4, idle) - D8
  Protoss-only, so the leak is unobservable until Terran play exists.
  The adhoc tier also has no Terran expectations (Protoss gates fail
  it trivially). Needs a game showing it. No blind fix.
- **#92** Second Gateway ~3 min late on Wine (BO `At 10` #3 → re-queued
  4:39 sup35, accepted 4:40+ six times same tile; first Gateway accepted
  1:09). OpenBW control (`20261015_141835`) warps both Gateways on time
  (1:14 sup9, 1:37 sup10). Instrumented, not patched: `BUILDINGS.json`
  ledger (warp time + supply vs order expectation, both runner paths).
  Needs a Wine game with the ledger - the late/missing Gateway row names
  the layer. First datum 2026-10-19 (TauCross): Gateways #2/#3 on time
  (1:24@10, 1:51@12); instead a triple dynamic ordering at 49 (#60/#64/
  #61 within 162 frames) - same duplicate class as supply, not lateness.
  Re-verified 2026-10-19: #2/#3 on time 1:27-1:28/1:49-2:00 in all
  three fresh OpenBW runs, dynamic Gateways on schedule - OpenBW
  control clean, Wine proof still owner-side. No blind patch.
- **#94** Enemy never spawns on OpenBW (host auto-menu path): 6+
  enemyless games across TauCross/Python, Zerg/Terran (`enemy: ` empty,
  0/0, scouts find nothing in 18 min). Plumbing verified correct;
  failure is deeper (slot never reaches occupied/computer_game).
  Blocks all fighting proof (cluster FIGHT/FLEE, kiting, rush twins).
  Paths, cheapest last: owner-authored UMS map with a computer-slot
  enemy (no editor on machine); C++ host slot debugging (shared
  platform, owner call); Wine games with real opponents (works today).
  2026-10-18: Protoss enemy also spawns nothing (`143846` PvP, 361
  game-s, `enemy: ` empty, 0/0 - 7th enemyless game across 3 enemy
  races). Race-independent; host-side only.
  2026-10-20: WITHDRAWN as a regression (see below) - what looked
  like a hung host (server log cut mid-line, no HELLO, 1% CPU) was
  an empty slot, not a stuck engine: the game runs, the bot attaches,
  but 1Drag_v_1Zeal seats it in a unit-less EitherPreferHuman slot
  (verified in AutoMenuManager.cpp: first EitherPreferHuman slot
  wins; ENEMY_RACE overwrites the other Either slots). The
  "mid-line cut" is stdio buffering on the kill, the 1% CPU is an
  idle wait. The creation lines in old logs come only from
  TestAIModule (TestModule.cpp:37), which the server path never
  loads - so grepping server.log for spawns proves nothing either
  way. A scenario-load check on that signal was added and removed
  the same day (would false-FAIL every healthy game). Correct
  vehicle: maps whose bot slot holds units (`M&M_v_ZealDrag`:
  PASS 5/2 on the rebuilt engine) + the bot-side 0/0/0 verdict
  for vacuous games. Engine C++ stays owner territory, but nothing
  here needs it.
  2026-10-20, second reading: the slot story is UNPROVEN either
  way, not refuted. Mixed evidence: the 12:59 UMS log shows our
  Dragoon=1 with zero buildings warped (preplaced unit, bot HAS a
  slot), while the 2026-10-19 run on the same map had no units at
  all - assignment varies in a way no log explains yet. What IS
  settled: with a unit but no base the bot idled a whole game
  (Forge FE head, no scouts, no engage, Defeat by map timer), and
  that failure mode is now fixed regardless of slots (baseless
  ATTACK marches from the army center; proved WIN 1/0 in 499
  frames on Wine, 34 stepped-aside vs 9 stuck, cadence 33).
  Default unknown-matchup BO flipped to Zealot into Goon the same
  day (Forge FE armed an economy opener on a duel map).
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
  [ ] M4 wall-in goal + game proof (2026-10-20: siting proof LANDED on
  a forced-FE short Wine game - Forge 1:45, Pylon warped ON the anchor
  [50,106] 0:56, PhotonC sited at anchor+36px 2:38; LONG FE game the
  same day (11.7 min, archived): wall pieces warped (Forge 2:07,
  PhotonC 2:36) but the base fell 9/42 ~12:00 with bank 3750 hoarded
  - siting proven, HOLD not proven (zealot line wiped 8:30, dragoons
  10:30; open whether the wall ever had the DPS)) [ ] M5 module extraction (assessed
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
  bank-74 deadlock shape absent again. Re-checked 2026-10-18: earmarks
  >= 250 in 13 runs, all at supply 22+ in flowing games (lowest: 300 at
  sup 22) - the deadlock shape absent again. Re-checked 2026-10-19
  across 25 retained EVENTS.jsonl (24 files + the live game log):
  earmarks >= 250 in 3 runs only, all at supply 20+ with BUILDING_STARTED
  flowing (ums-core min sup 20, ums2 min sup 20, live log one frame-3
  BO-load row at sup 10/bank 250) - the supply<=10/bank<150 deadlock
  shape absent everywhere. Standing caveat: the ORDER_NOT_READY twin
  only fires above price-32 bank, so a sub-floor deadlock would leave
  no twin rows at all - the sweep instruments the visible band only.
  Re-checked 2026-10-20 across 12 retained logs (earmarks >= 250 in
  3 runs, all frame 4500+ mid/late game, banks 144-658 - the
  supply-4/bank-74 deadlock shape absent again).
- **#96** Wine verification batch, owner-run: painter warping labels
  (`Gateway @12 [NOT_STARTED]`), WINE_LOG/_DEBUG/_DECISIONS triple,
  E2E_LOG/_DEBUG/_DECISIONS stable files, Missions/BO-mission unenaction sanity.
  Jar 12.1 MB already carries it all.
  2026-10-20: file-outputs half verified on a real Wine game
  (WINE_LOG/_DEBUG/_DECISIONS triple + BUILDINGS.json all collected,
  non-empty); painter labels stay a visual owner check. Batch itself
  still owner-run.
  Open tuning question for the batch: Pylon prepared at 15 warps ~2:24,
  past the 17/17 block - adoption+travel+warp (~25 s) outruns the 15→17
  window (~25 s), so any churn lands past the block (churn itself fixed
  2026-10-17; lever if still late is `IsReadyToProduceOrder` pull-forward
  amounts, currently +1 below 19 / +3 above - earlier pylons trade tech
  minerals for block time).

- **#34** Stub twins hold (4pool/9pool baselines pinned); a real
  opponent needs a scripted UMS map (one client per OpenBW host) -
  owner-supplied, tier stays owner-run. Probed 2026-10-19 with
  `USE_MAP_SETTINGS` in the runner: `ZealDrag_v_Lings` runs the full
  1209 s but hands us a melee start (Nexus+probes, no army) and spawns
  no enemy (0/0) - no fight to assert; `8Drag_v_8Drag` ends at frame 0
  (human slots) with the exit-path backstop firing. Neither map yields
  a battle; the tier still waits on an owner-authored computer-slot map.
- **#65 / #68** CherryVis code fixes are in (types, legend, seed,
  writer path, `--cvis` plumbing); what remains is the owner opening a
  real trace in the viewer (plus the `[BW<->CP Matching]` console line
  if `???` persists).
- **#77** Acceptance tier: 6 stale pins, all judgment calls (3 combat-
  eval numbers vs ADR 0006, ZealotSelfDefense open question, 2 Terran
  harness pins vs hardcoded race) - owner's call. Re-measured 2026-10-19:
  unit 743/743, acceptance 748/0, scenarios 9/9 green; the only reds
  anywhere are the 2 known pre-existing ArchUnit edges. The 6 pins no
  longer exist as red tests (cutover deletions absorbed them); what
  remains is the judgment part (ADR numbers, open question, Terran
  harness), still owner-side.
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
  RULED 2026-10-19 (owner): Dragoons ASAP whenever the Core stands
  and 50 gas is banked regardless of Zealots; Zealots without gas are
  a 275-mineral dump, pre-Core opening Zealots ungated. LANDED in
  `DynamicGoals.addArmyGoal` + tests, live proof `203655` (8 Dragoons,
  first 5:25, was 0 pre-rule).
- **#144–#147** Rewrite order, clean-room (owner direction 2026-10-17:
  Queue, site-finding, APositionFinder and SupplyCommander each properly
  planned and built anew, then the old path moves to src-old/). Safe
  sequence, never bottom-up (#45): each lands specced, stub-pinned,
  default-flipped with a game proof, and only then the legacy files move.
  - **#144** SupplyCommander (+ fallback bands) → the board supply goal
    owns all supply demand (DynamicGoals.supplyGoal + SUPPLY_BLOCKED twin
    already name the need; the feeder already feeds supply-pylon rows).
    Exit: one author, supply pumps silent, `SupplyCommander/**` +
    fallback in src-old/. 2026-10-20: dual authorship confirmed live
    (V2 Wine game: Pylon goals build-order 1633 + dynamic 1102 rows) -
    the pumps are NOT silent, the feeder does NOT cover routinely;
    no candidate, same as step 1.
  - **#145** Site-finding chain (`NewConstructionRequest` + `FindPosition`/
    `DefineExactPositionForNewConstruction`, the "ConstructionFinder") →
    catalogue-only sited demand through the adapter funnel
    (03_PLACEMENT.md; exact tile honored verbatim, power + refusal twins).
    Exit: every construction sited by the catalogue, the chain in src-old/.
  - **#146** `APositionFinder` legacy path → deleted after the catalogue
    default + parity (D1 pointer; ~25 files still route through it).
    Exit: zero `Can't find place` across retained runs, legacy path gone.
    2026-10-20: 33 PLACEMENT_FAILED in one Wine macro game - nowhere
    near zero, no deletion.
  - **#147** `Queue/**` + order types → stateless demand/schedule
    (01_PRODUCTION.md scheduler + dispatcher; single policy already
    gated). LAST: a bot that can't queue can't play. Exit: V2 LIVE by
    default with parity numbers, queue in src-old/.
- **#155** `ProduceBuilding` gates as a registry, not a 10-`if` chain
  (SOLID review 2026-10-19, agreed, file deleted after consolidation):
  every `if (...) refuse(...)` in `requestBuilding()` becomes a named
  `BuildGate` in explicit order (live-construction, pylon-cap,
  pylon-overstock, unstarted-pylon, earlier-reserve, core-priority,
  power-not-funded, funding), each pinned in isolation. The order is
  semantic today and nowhere written as a contract - the registry
  makes it one. Slices per gate with a game proof each (a gate
  reordered wrongly re-trips an earlier refusal); feeds naturally
  into #147 (the stateless scheduler consumes gates, not ifs).
  Registry LANDED 2026-10-20 (`BuildGate` + `BuildGates`, 11 gates,
  `BuildGateOrderTest` pins the sequence, zero behavior change).
  First per-gate proof FAILED live the same day and forced a fix:
  core-single-instance deadlocked BO #9 vs dynamic #24 for 8
  game-minutes (siteless rows counted as "coming") - now only
  genuinely coming work blocks, oldest READY waiter proceeds
  (`CoreSingleInstanceGateTest` 3/3); Wine re-prove GREEN the same
  day (short flagged game: Core STARTED once, warped 2:58@23, ZERO
  core-single-instance refusals all game vs 70+ before).
  Gate evidence from live games (all named, firing as designed):
  earlier-reserve 14 + min-supply 2 + unstarted-pylon 2 + funding 3
  + pylon-cap 3 + pylon-overstock 1 (FE short game), Core gate quiet
  across two more games. Next gate proofs ride the next macro game.
- **#156** One `EarlierDemand` reserve (SOLID review, agreed): three
  models of "already reserved" exist (`ProduceBuilding`
  earlierUnfinished*, `ShouldNotTravelToConstructYet.earlierUnpaid`,
  `GoalBoardWallet`/`ReservedResources`) with different sorting and
  exclusions - every duplicate-Pylon race reads a different sum.
  Unify behind one ordered reserve ("already paid = has building
  unit") with twin-pinned duplicate rows as the proof; WO-145/147
  territory, never a blind merge.
- **#157** Semantic `ConstructionOrderStatus` queries (SOLID review,
  agreed): new statuses force edits in ~6 reader classes each. Add
  enum-level answers first (`isWaiting`, `countsAsDemand`,
  `isTerminal` - additive, stub-pinned, zero behavior change), then
  migrate readers one by one with a game proof each (construction
  is the most Wine-proven area; no batch migration). LANDED
  2026-10-20: all nine PREPARING readers (skip/cancel/overdue
  guards, builderless-site filter, twin path label, adopt path +
  guard) read isWaiting() instead of re-spelling the disjunction -
  same predicate, zero behavior change, suite green
  (`ConstructionOrderStatusTest`, `CoreSingleInstanceGateTest`
  carry the semantics).
- **#159** Pinch retreat proof on `Drag_v_LingsUpg` (owner 2026-10-20:
  hurt Dragoons surrounded by 2+ melee inside 2 tiles retreat
  instead of standing into the pocket; `DragoonPinch*` knobs in the
  tuner). A/B started 2026-10-20: WITH pinch 18/9 lost, first death
  frame 97, 197 stepped-aside vs 5 stuck, 282 hits taken; WITHOUT
  19/10, first death frame 163, 164 vs 11 stuck, 301 hits. Direction
  right (more yielding, less glue, fewer hits) but N=1 each and the
  spawn decides first blood on instant-contact maps - no verdict.
  Proper proof needs the spread-start scenario (StarEdit, armies
  apart so kiting decides) + bigger N; `scripts/kill-leftovers.sh`
  before every game, one box one game (cross-attach measured).
- **#160** Automated tuner winner selection (owner 2026-10-21:
  selection is eyeballed today, make it the best long-term loop).
  Steps, in order, each a game-proven slice: (1) write the objective
  down in code - win first, then hits-taken asc, then seconds asc,
  with a per-map par row, instead of eyeballing batch.csv;
  (2) elitist hall of fame: keep top-N param vectors across
  batches, mutate around them with a shrinking radius instead of
  uniform random in fixed bounds; (3) statistical bar: a vector
  must win K-of-N replicates before promotion (duels are noisy -
  measured 97 vs 163 first-blood frames on identical code);
  (4) freeze-then-expand: hold proven params fixed while exploring
  the rest, so one bad dimension never sinks good ones;
  (5) archive every batch (params + digest + verdict) for later
  meta-analysis. Runs overnight/weekend on owner call (box-exclusive),
  never in the fast loop. Micro-only for now (#152); macro needs
  its own objective later, out of scope until micro converges.
  2026-10-21: step 1 LANDED (`TunerObjective` + `TunerObjectiveTest`
  7/7: win, then hits asc, then seconds asc, `?` sorts last,
  batch.csv parser, deterministic tie-break; pure tooling, game
  loop untouched, suite 998/998 + arch unchanged). 2026-10-21:
  step 2 LANDED (`TunerHallOfFame` + `TunerHallOfFameTest` 5/5:
  bounded top-N store ranked by the objective, radius mutation
  clamped to per-param bounds, `?`-safe file round-trip, garbage
  lines skipped; wiring into the batch init rides the intensive
  session with its own batch proof, never blind). 2026-10-21:
  step 3 LANDED (`TunerPromotion` + `TunerPromotionTest` 4/4:
  K-of-N replicate bar, short records never promote; suite
  1007/1007 + arch unchanged). Steps 4-5 open
  (freeze-then-expand, archive).
## Current game state (updated 2026-10-21)

- Wine `1Drag_v_1Zeal.scm` (UMS micro): baseless ATTACK now marches
  from the army center (was: idle to the wall kill); pinch retreat
  (<80 hp + 2+ melee in 2 tiles) yields instead of standing into
  the pocket; tuner mutates all CombatTuning knobs incl. pinch;
  default unknown-matchup BO is Zealot into Goon (was Forge FE).
  Latest duel: 1/0 WIN in 499 frames, 34 stepped-aside vs 9 stuck,
  shot cadence median 33. Pinch A/B on `Drag_v_LingsUpg`: WITH
  18/9 lost / first death f97 / 197 vs 5 stepped:stuck; WITHOUT
  19/10 / first death f163 / 164 vs 11 - direction right, N=1 each,
  spawn decides first blood, no verdict (needs spread-start map +
  bigger N, NEXT #159). Tuner batch: 10/10 wins (NEXT #152).
- OpenBW UMS (`M&M_v_ZealDrag`): PASS 5/2 KILLED/LOST on the rebuilt
  engine - the working OpenBW proof vehicle. `1Drag_v_1Zeal` seats
  the bot in a unit-less human slot (verified slot-pick code);
  whether the enemy Zealot even spawns there is unproven either way
  (mixed evidence: bot got a preplaced Dragoon on 12:59, nothing on
  10-19). Wine stays the duel vehicle; OpenBW hosting is flaky
  run to run (12:59 played 1209 frames, 13:13 never published).
- OpenBW `TauCross` (`113933` long, FE forced reverted probe #2,
  12493f/8:44, exit 143 wall-cap - inconclusive as verdict, rich as
  data): FE OPENS (Forge 1:25, Core, 45 warps, Dragoons 10) - the
  `112337` "jam at Forge" was the disconnect, not the logic. BOARD
  top `wall-in` once (5:14); sited exact pieces after: PhotonC
  [19,19]/[18,22] (6:08/6:47), Pylon [12,46] (8:11, wall power),
  PhotonC [14,43] (8:42); NO At-0 Gateway plug warped. Geometry
  twin LANDED (WALL_ANCHOR_WARP: warp tile vs anchor row + exact
  planned tile, pinned WallAnchorWarpTwinTest 5/5) - read on the next
  FE game; survival unprovable on
  OpenBW (#94). Twin stream intact to the cap (253 rows past the
  last 720-frame interval - shutdown flush verified live).
  prepare-site-overlap 0 (no overlap case). Watcher quiet (no
  false-positive jstack - the EVENTS watch verified live).
- OpenBW `TauCross` (`112337` long, FE strategy forced as a reverted
  probe - `Forge FE vZ` vs Zerg, 138 s wall, exit 143 wall-cap):
  the run ended by CLIENT DISCONNECT (~1:28 game time:
  `Reconnecting...`, then `Stopped (no game end reached) after 95
  seconds`), not by a proven opening jam. Pylon #1 warped 1:07@8
  [50,106]; Forge (oid=2, minSupply 11) decided once at 1:28
  (`prev=cid41:PREPARING path=adopt`, frame 2120) with zero twin
  rows after - but the twin stream itself stops at frame 1558:
  EventLog buffers 720 frames between flushes and the kill dropped
  the tail (shutdown hook now flushes, so the next capped run keeps
  it). Construction ids burned 1->41 for 1 flushed PREPARING -
  the burn is real (in-memory counter) but the missing rows are
  buffer loss, not proof of silent unlinks; the `prepare-site-overlap`
  twin stands as instrumentation (stub-pinned), undecided live.
  Forge fate unknown; wall-in unreached. The disconnect shape matches
  the known host-death class (contrast #149), not a new item.
- OpenBW `TauCross` (`105921` long, legacy default, 16546f/11:34,
  exit 143 wall-cap - inconclusive as verdict, rich as data): Pylon
  0:52@8 (control holds for #84), Gateway 1:24/1:53, Core 3:02,
  Dragoons 12 from 4:12, Nexus=2, RoboticsF 11:28; BOARD_FED 1
  (sustained-dragoon 5:16, rescue not routine); #71 correctly silent
  (2 bases, ORDER_SAVING 0, end bank 288); hang-watcher jstacked a
  40 s main-thread spin in Cache/Selection at 11:00:03, recovered
  (#153). Retry `110411` same day: same shape (16063f/11:14,
  exit 143 wall-cap, Nexus=2, ORDER_SAVING 0, Pylon 0:52@8,
  BOARD_FED 1, watcher again same signature) - #71 stays lottery
  (the bot expands on its own; no forced 1-base shape, no more
  retries until the shape is forced or luck lands).
- OpenBW `TauCross` (`203655`, PRODUCTION_V2=LIVE with rule+lag+>60,
  21933f/15 min): Dragoons 8, first warps 5:25 - the ASAP rule fires
  live (was 0 in the pre-rule LIVE game). Pylons 7 with one 150f
  pair, both tiles named by CLOSE_WARPS; twin + blocker-construction
  rows fire as designed. CANCEL 691 still dominated by the Nexus loop
  (C14 untouched).
- OpenBW `TauCross` (`194718`, PRODUCTION_V2=LIVE with the PurpleWave
  rule, 21905f/15 min): 5 Pylons, Dragoons 4 (floor not frozen this
  game), suppression engages live (blocker-construction twins,
  coming=2 stops demand once two travel). Residual pairs come from a
  per-frame mint-cancel stream on covered V2-sited tiles (28 fresh
  Pylon plans in 300f, 51 proximity cancels) - doomed sites die
  same-frame so no coming-count sees them (WO-146 siting +
  WO-147 in-flight). Unstarted pile-up: zero instances.
- OpenBW `TauCross` (`191444`, PRODUCTION_V2=LIVE, 23026f/16 min,
  wall-cap 143): tech on time (Forge 1:50, Core 4:12, gas 3:54),
  6 Pylons with no supply block and 97 SUPPLY_BLOCKED samples - but
  the migration is NOT ready: 609 Nexus cancel/re-request cycles
  (expansion siting C14, Nexus=2 never), 0 Dragoons (floor freeze),
  Pylon pairs 89f/72f apart (now attributable: V2_GOAL_SOURCE
  names the emitting list per provider goal, V2_DISPATCH rows every
  issued/refused plan item - both LANDED stub-pinned, read on the next
  live run), 18 warps vs ~40 legacy. Blockers, in order: C14,
  Dragoons, parity (#45 ordering stands).
- OpenBW `TauCross` (`185357`, PLACEMENT=catalogue, 9614f, ended
  environmentally like the clean run above): catalogue answered 6
  (2 Pylon + 4 Gateway), declined 29 named (no-builder 20,
  dedicated-finder 2, legacy-refused 7), 6 REQUESTED vs 1 CANCEL,
  25 warps; 1 residual Can't-find-place (Gateway #86 At 44, known
  late-dynamic class, NEW class: none). WO-145 parity direction
  holds; flip waits on Gateway-arm coverage. 2026-10-20: adapter
  verified wired in both funnels (sited via RequestBuildingNear:132,
  queue via APositionFinder:47, same decline→legacy contract);
  Gateway-arm parity still needs a SiteTwin-read game.
- OpenBW `TauCross` (`185237` DRY_RUN short): V2 plans the opening
  provider 16x at stable (112,8) while legacy warps [113,15] -
  demand parity in the window, tile a few tiles off (known siting
  gap). Late: V2 wants ZERO Pylons past frame 5000 while legacy
  warps 3 - suppression against in-flight providers or a demand
  miss; the LIVE run (WO-144 step 3) decides.
- OpenBW `TauCross` (9149f, 6.4 game-min, engine VICTORY at Killed:1 -
  environmental end, rich datum): 14 BUILD rows, every order exactly
  one row, each naming its cause (`prev=cid:PREPARING path=adopt` vs
  `prev=link-null path=new` + oid join key). Zero same-tile storms,
  4 REQUESTED vs 2 benign orphan-drop cancels, roster Nexus=1
  Probe=26 Pylon=7 Gateway=3 Dragoon=2. The DECISIONS readability bar
  holds: one demand, one row. New doors live-dormant this game
  (standing-cover 0 fires, live-construction 0 fires), no regression
  in the refusal profile.

- Wine `TauCross` LONG (2026-10-20, 10.8 game-min, 7/29 loss to Garm
  Brood, formation Phase A live in the jar): FIGHT 301 / FLEE 82,
  KITE 140 + HIT 409 + SHOT 7871 flowing - the cluster fought, no
  freeze, no crash. Loss driver is production, not combat: Forge
  6:58 (late, no-forge 230 TECH_CHECKs), NO Core all game at bank
  3750 (the single-instance mutual lock, fixed same day - see the
  #155 note below), zero Dragoons, Nexus#3 10:28. Pylon#1 healthy
  (0:47@8, #84 datum); Gateways #2/#3 on time 1:26/1:51 (#92 datum,
  no Wine lateness this game); ORDER_SAVING 0 (2-3 bases, #71 wants
  a 1-base 700 s shape - 649 s reached, just short; FE long game
  the same day reached 707 s but with 3 bases + bank 3750, so
  correctly silent again - the shape, not the duration, is missing;
  legacy long attempt the same day died on attach ("No server proc
  ID", IDE build running 30+ min on the box - killed as
  inconclusive, never a verdict). WINE_LOG /
  WINE_DEBUG / WINE_DECISIONS triple + BUILDINGS.json collected
  (#96 file-outputs half verified; painter labels stay visual).
  Open question: SUPPLY_BLOCKED twin silent through the 90/90
  blocks (twin gap?).

- Wine `TauCross` LONG FE (2026-10-20, 11.7 game-min, 9/42 loss,
  FE-forced + reverted right after): Forge 2:07, PhotonC sited at
  anchor+36px and warped 2:36, Core 4:03 (gate fix holds - ZERO
  core-single-instance refusals all game), Dragoon 5:59 x14,
  Nexus#2 only 6:58, RoboticsF 10:45. Singularity researched 10:07
  (#85: the post-Forge window EXISTS and completes - 64
  bank-700-or-3rd + 216 weapons-l1 + 152 combat-10 TECH rows).
  707 s reached but 3 bases + bank 3750 at death, so ORDER_SAVING
  correctly silent again (#71 still wants the 1-base shape).
  BUILD_REFUSED guards healthy (earlier-reserve 14, min-supply 2,
  unstarted-pylon 1 - #155 per-gate evidence). Loss: zealot line
  wiped ~8:30, dragoons ~10:30, bank hoarded unspent - same thin
  shape, now WITH tech. Per-run archive works (out/wine_runs/+latest).

- OpenBW `TauCross` LONG (16575f, 11.5 game-min, wall-cap exit 143 -
  inconclusive as verdict, rich as data): roster Nexus=2 Probe=44
  Pylon=15 Gateway=7 Dragoon=10 Forge=1 PhotonC=1 RoboticsF=1. 57
  BUILD rows, every legacy-path order exactly one row (new oid+context
  suffix live: adopts name their cid, re-requests name link-null).
  No same-tile REQUESTED+CANCEL storm; new standing-cover refusal 0
  fires (spiral offered no covered tile). live-construction refusals 0
  (guard dormant, no regression). Open doors, named: sited PhotonC
  21x at [115,15], two PREPARING Pylon adopts into cover (#151).

- Wine `TauCross` SHORT (exit at supply 51 via `ForceExitAtSupply`,
  clean end, ~6 min game-s: 26 ledger rows, BUILDER_LOG=1, full new
  code incl. STARTING + power-not-funded): peak free 26. Per-author
  triggers hold (every Pylon ordered at free <= 6), but same-need
  duplicates land together anyway: BO#8 + dynamic #26 same frame
  (At 21), dynamic #67 + #68 130 frames apart (both At 23), each pair
  warping into one supply block. The queue-door spam cap (refuse above
  12 free) is the backstop, not the cure - single author (WO-144) is.
  STARTING twinned live (66 rows, builder travelling to site).
  power-not-funded never fired (Pylon#1 up before gateways needed
  builders). 1 kill, no real fight yet - engage/cluster await contact.

- Wine `TauCross` SHORT, first run (exit at supply 51 via `ForceExitAtSupply`,
  clean end, 364 game-s: Nexus=1 Probe=25 Gateway=3 Dragoon=4 Zealot=7
  Pylon=6 CyberneticsC=1 Assimilator=1, 24 ledger rows, BUILDER_LOG=1):
  supply-cap doctrine HELD that game - peak free 14 all game (was 27),
  4 BO + 2 single dynamic Pylons, zero same-need duplicates,
  SUPPLY_BLOCKED 0 (no starvation). Same-builder adoption live
  (Pylon#1: reserved #22 at pick 0:13, builder 22 at start 0:46).
  Gateways #2/#3 on time (1:20/1:42), one dynamic #24. 0/0 kills.
  Variance vs the second run above is the finding: the cap bounds the
  trigger, not the outcome - same-frame pairs and lag stacking decide
  the peak, which is why the door is a backstop and WO-144 the cure.

- Wine `TauCross` (910 s, vs real Zerg Grendel Brood, 21/51 kills -4175:
  Nexus=2 Probe=36 Dragoon=2 Pylon=9 Gateway=5 RoboticsF=1, 53 ledger
  rows, BUILDER_LOG=1, feasibly the first game on the cid/oid twins +
  supply cap + engage fix): ENGAGEMENT PROVEN - 21 army kills with zero
  static defense (FIGHT 283 + FLEE 300 CLUSTER rows, latch both ways;
  winning proof still pending, -4175). Supply cap partial: per-author
  triggers hold, but same-need duplicates persist (Pylon #50/#51,
  #67/#68, #101/#102; gateway triple at 49) - mid-game peak free 26
  (WO-144). Pylon#1 healthy (builder#5 at pick 0:13 → STARTED 0:46 →
  warp 0:58, #84 datum). Gateways #2/#3 on time 1:24/1:51 (#92 pattern
  not reproduced). Bank peak 689, 2 bases, ORDER_SAVING 0.

- Long `20261018_143050` (155 s wall, 657 game-s, legacy rich on the
  FE-gated tree: Nexus=2 Probe=52 Pylon=13 Gateway=6 Dragoon=18 Forge=1
  PhotonC=5, 67 ledger rows): first long game after the wall-in FE
  gate - healthy, no regression. wall-in in 0/10 BOARD rows, zero
  `At 0 Gateway` wall orders (gate holds for 11 game-minutes, not just
  the short tier). Forge warped 4:48@63, post-Forge bank max 559 < 700
  so research correctly silent (#85 still pending its window).
  Adhoc gate missed on Nexus>=3 only (expansion timing, not a defect).
  `ORDER_SAVING` 0 (2 bases - correctly silent, #71 wants 1).

- Long `20261018_144015` (155 s wall, 677 game-s, legacy rich on
  Python 1.3, first non-TauCross long: Nexus=2 Probe=51 Pylon=13
  Gateway=6 Dragoon=13 Zealot=2 Forge=1 Stargate=1 + Corsair,
  RoboticsF, PhotonC, 65 ledger rows): map-independence holds, FE
  gate holds (wall-in 0/10 BOARD). Forge 3:58@63, post-Forge bank 784
  at 4:42 - the #85 window EXISTS here: Singularity researched 7:53
  (started 6:06, engineRes+staticRes true; UPGRADE_COMPLETED twin
  never fired - twin gap, not engine - WIRED 2026-10-19: the
  observation sat in handle() behind the gate, a researched upgrade
  refuses so handle() never saw the rise; note() now runs in the base
  applies() (Armor wired in its own, Storm is a tech and stays out),
  pinned by UpgradeCompletedBehindGateTest 1/1, single-level and
  max-level rises both fire). Weapons TECH_CHECK still false
  at 772/306 bank (refusing gate unnamed in the twin - the open half
  of #85). 2 `Can't find place` (Gateway #87/#88 8:27, unpowered
  dynamic rows - known class, #146). GAME_END present, cap exit (#149
  no occurrence). `ORDER_SAVING` 0 on 2 bases (#71 wants 1).

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
- Long `20261017_091624` (883 s, 55-unit cluster, no enemy spawned):
  Phase-3 movement proof - DEFEND→ATTACK at 8 army, anchors main →
  natural → enemy corner [9,45], group holds there; CLUSTER twin
  (posture/tactic/size/anchor) verified flowing. Fighting unproven:
  host spawned no enemy in 4 straight games (environmental).
  Unexplored-start fallback landed (else ATTACK sat home 18 min).

- Long `20261017_080114` (58 s wall, FULL 1209 game-s, rich: Nexus=7
  Probe=56 Pylon=27 Gateway=9 Dragoon=24, cap-inconclusive): Phase-1
  proof - opening script-clean (Pylon 0:51@8, Gateway 1:25@10, Gateway
  1:53@12, Zealots, Core 3:02@23, ONE of each), exactly ONE
  CyberneticsC in 20 min (no duplicate Core/Tech - the outstanding cap
  holds live), reserved trajectory healthy (0 early, single-row 150
  late - the 0:07 250-ghost never reappeared with flow restored),
  DECISIONS.log 720 rows (26 BUILD + 9 ARMY from BUILD_ORDER, rest
  dynamic with reasons).

- Long `20261016_163357` (FIRST PvT, 15 s wall, defeat 5:13,
  Probe=22 Zealot=3 Dragoon=1): Terran rushed us dead early - no
  baseline exists for this matchup (rush twins are Zerg-scripted),
  one game is not a defect per §3a, recorded as the datum to beat.
- Long `20261016_151714` (141 s wall, 628 s game, rich: Probe=37
  Dragoon=13 Nexus=2, cap-inconclusive): lottery blank - no Core
  cancel (#88 went to `131201` instead), no `ORDER_SAVING` (#71),
  `SINGULARITY_APPLIES` x8 climbing to 348/372 then silence with no
  completion marker anywhere (#72: completion unobservable, hence
  the CHECK twin).
- LIVE `20261016_131407` (9 s wall, 247 game-s, roster Pylon=2
  Gateway=1 Zealot=3 Forge=1, 0 exceptions): INCONCLUSIVE, not evidence
  - the bot exited at 4:07 for no logged reason with no enemy spawned
  (`enemy: ` empty). Retry in flight; if it repeats, the early-exit
  path (not V2) is the hunt.
- Last recorded `--long` pass: 210002, 912 s, exit 0 (Nexus=6 Dragoon=7
  Forge=1, first ever; gas 270+; Singularity correctly idle at one
  Dragoon). Log not retained.
- Feeder: first `BOARD_FED` observed `20261015_143903` (supply-pylon at
  a 39/41 block); 0 in all other retained logs.
  `ORDER_SAVING_FOR_EXPANSION` 0 everywhere (string present in
  `IsReadyToProduceOrder:76`, never fires - `20261015_150026` long run
  included). Raw counts, no diagnosis.
- Long `20261015_185046` (141 s wall, 575 s game, 0 kills, rich
  economy: Nexus=2 Probe=31 Pylon=13 Gateway=5 Zealot=15 Core Dragoon=6
  Forge; shutdown-hook summary worked): `SINGULARITY_APPLIES` firing,
  completion unproven (#72); 2 Pylon refresh-cancels, no Core cancel
  (#88), no `ORDER_SAVING` (#71); hang-watcher fired once on
  busy-compute (57 s CPU, not deadlock).
- Wine first Pylon still unbuilt in both instances (see #84); the
  convertible-waiters fix plus walking-builder shield are in the jar,
  final proof is the owner's re-run.
