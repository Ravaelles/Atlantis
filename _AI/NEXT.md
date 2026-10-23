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
   no candidate yet. Deletions wait for routine feeder coverage, never
   batch rewrites; board-invisible production (Forge, cannons) is
   capability, not duplication.
   Per-game tally (§1) + re-sweep history (§2) → _AI/WINE-MACRO-DATUMS.md
   (cumulative: 172 rescue / 0 routine, our games only — foreign 091345's
   rescue row counts in re-sweep rows, never in tally; 230 retained logs /
   155 rows, incl. foreign 141623 (concurrent unattributed run, 3 rescue
   rows, never in tally); B91 twin returns (15th) + zero-waste 13/13, 2 feds;
   tally corrected (B58 slip -3).
2. **Decision absorption:** board takes over decision sites one by one
   (7 files with the exact singular import, re-verified 2026-10-20:
   DTDontAvoid's import was dead (only commented-out references) and is
   deleted - the rest still 7 on the same excluded tracks, production
   holds ZERO (re-verified 2026-10-22: same 7 files, all on excluded
   battle/micro/interrupt/terran-info tracks) - was 47: 24 died with the combat cutover, 1 orphan
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
   Model-run headless Wine now (allowance 2026-10-22); no longer blocked.
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
2026-10-22: unit 1267/1267 (Behavior vocabulary + commander
table-of-contents, Region center fallback, melee veto + glide
guard, arc matrix + registry, 3 new pin files; acceptance unmeasured
this turn - last measured 17 reds, byte-identical to baseline).
2026-10-22 re-measure after the #182/#183 commits
(`bash scripts/run-tests.sh`): unit 1279/1279 (FormationHold 11/11,
HoldTwin 3/3, HolderUnstickSim 1/1 included); architecture advisory
unchanged 9/11 (same 2 pre-existing edges, empty store diff); our
files checkstyle-clean (the 100 whole-tree violations are pre-existing
debt in foreign files, untouched).
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
  same 2 pre-existing failures, store diff empty). 2026-10-21 ninth
  slice (`inShootRangeOf` x2 + `combatBuildingsAnti` dispatcher to
  `SelectionMembership`, dispatch pinned first): the remainder text
  above predates slices 6-8 (views, mutations, aggregates already
  moved) - the rest is thin dispatchers and multi-statement logic
  needing designed splits, not verbatim moves.
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
  changes - the Selection-slice recipe. Second slice 2026-10-21:
  near-counters (`AUnitNearCounts`). Third slice the same day:
  hp thresholds into `AUnitHealth`, shield family into new
  `AUnitShield` seam (NaN contract travels with the code); the pin
  tests caught a latent bug on the way (`shieldWoundPercent`
  multiplied by 100/maxShields again - dead code, no callers, fixed
  in the seam with the reason recorded).

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
  run). 2026-10-20 answer from the retained TauCross macro: NEITHER
  orphan NOR silent drop - the [123,26] Gateway WARPED 8:06 (ledger
  over verdict), and the twin gaps are named instead: no STARTED row
  for warping cid=96 (builder-path gap), and cid=98/oid=67 CANCELs
  "Null position" with zero REQUESTED rows anywhere. Mechanism for
  the second: `ProductionOrder.cancel` -> linked-construction cancel
  -> CANCEL twin, while REQUESTED fires only post-addConstruction
  with a site - a siteless linked construction terminates without
  ever originating. Mint site ANSWERED 2026-10-21 (comment at the mint,
  no patch): NewConstructionRequest links bidirectionally (setProductionOrder
  -> setConstruction) BEFORE siting, while REQUESTED fires only
  post-addConstruction with a tile - so read the CANCEL reason for these.
  Pick-time guard (`overlapsUnfinishedSite`, same-file spiral)
  should have refused the second pick; ANSWERED 2026-10-21 (no patch):
  the guard's world is unfinished-buildings-only by pinned design, so
  the standing Gateway [114,18] is skipped and proximity-finish cancels
  the grazer same-frame (disjoint worlds, same as #151's no-merge).
  FE scoping landed the same day
  (wall-in only on Forge Expand; live `104922` on non-FE shows zero
  wall-in BOARD/wall rows). Remainder: owner visual proof of orange
  PREPARING (a). Live overlap game still unplayed; the resite seam
  fires live (adopt-resited 1x in each of two long runs). 2026-10-21:
  zero ADOPTED rows in 2 fresh macros (20:09 + 7:53) - consistent, not
  broken: adoption needs a waiting PREPARING site and both games were
  builder-rich (sites start immediately, nothing waits to adopt;
  storms mint+cancel same-frame so nothing survives to adopt). No blind patch.
  2026-10-21 Wine macro: twin gap named - warping Observatory
  [118,30] (STARTED 12:35) whose builder #126 died to lings 12:28
  leaves zero further rows (no cancel, no death, no warp): the
  orphan-drop twin does not cover builder-dead warps. 2026-10-21
  mechanism, no blind patch: demoteStalledInProgress checks
  buildingNearSite FIRST, so a standing warp entity self-matches
  and shields everything below (incl. the archive-aware dead
  branch) - correct for a progressing Protoss warp, fatal for a
  frozen one. The log cannot tell "frozen entity" from "completed
  but unlogged" (no Observatory death twin, no ledger row either
  way); the fix is progress-based demote (building HP static over
  N frames), not presence-based, pinned in stub + proven on a
  builder-death game - WO-sized, not a drive-by. 2026-10-21 liveness
  resolution: isDead() already consults the archive, so the demote
  path was archive-aware all along - the blind one was the retry
  path (FIXED: dropBuilderlessNotStarted honors UnitsArchive,
  RED->GREEN pinned, game proof pending on the next builder-death
  shape). A blocked-demote twin without a progress signal would
  mislabel healthy builderless warps, so no twin until the signal
  exists; the signal design is the WO slice.
  2026-10-21 Wine macro: 57 DISTINCT Gateway oids each CANCEL "Null
  position" once (siteless mint death, no storm - the untwinned door
  at scale, read the CANCEL reason per the mint answer); Stargate*
  #134 refused at covered [105,2] then warped [123,26] 9:07 (guard
  correct, resite worked).
 2026-10-22 Wine TauCross macro (VICTORY 6:49, BUILDER_LOG=1): quiet game - 0 CANCELs, 0 ADOPTED (builder-rich, nothing waited), no overlap rows; resite/adopt seams unfired, not broken.
2026-10-26 macro 061958 (VICTORY 1/0, 379s game, zero own deaths): 0 ADOPTED; 0 CANCELs; DEMOTE_STALLED twin 1x (Assimilator [7,39] f4700 stoodStill 90f, warped f4705 per ledger - thirteenth live demote, recovered; FIRST Assimilator demote, fifth stalled type after Pylon/Gateway/CyberneticsC/Forge); BREAKDOWN twin 1x Gateway [14,51] f2040 builder-on-site (fifth occurrence of the deterministic opening tile, self-recovered); all 13 STARTED warped (7 Pylon + 4 Gateway + CyberneticsC f4279 + Assimilator f4705); Pylon#1 f952 [10,52]; Nexus queued once (NOT_READY f8246) never started (final Nexus=1). No refresh-cancel this game, no zombie row, no core-single-instance block - the B22 lock stays gate+cancel-specific.
  → full chain: _AI/WINE-MACRO-DATUMS.md §3

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
  2026-10-21 Wine macro IS the game, at unprecedented scale: 463
  proximity-finish CANCELs in 4.1 game-min (9:45-13:50), THREE Pylon
  oids (152: 227, 212: 163, 151: 73) re-minting tile [119,12]
  against the STANDING Pylon covering it. Mint path traced: the legacy
  ConstructionFinder chain (`DefineExactPositionForNewConstruction`),
  not the spiral - no local patch (would duplicate the guard in the
  dying path); the fix rides #145, this storm class is its acceptance
  test (goes quiet after cutover).
 2026-10-22 Wine TauCross macro (VICTORY 6:49): no same-tile storms - 0 proximity CANCELs in 6.8 game-min; spiral and mint both quiet.

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
  real sweep. 2026-10-21 replication, 10/10 WINS, 20-23 game-sec,
  hits 6-16, shots 13-17 (same shape, tighter spread; per-game
  EVENTS + tweaker.csv + SUMMARY archived under PARAM_TWEAKER_LOGS,
  vehicle proven end to end). 2026-10-21 easy-map duels
  (ZealDrag_v_Lings2_easy, 12 ours vs 26 lings verified by smoke,
  3/5 W): losers fight to the end (FIGHT 127-156, FLEE 0-29),
  winners mix FIGHT+FLEE - the evaluator never refuses the open
  blob, and FLEE is banned baseless anyway (falls through to
  fight), so unwinnable opens have no exit today. 2026-10-20 UMS
  duel on the fixed anchor rule
  (baseless ATTACK marches from the army center): WIN 1/0 in 499
  frames, 34 stepped-aside vs 9 stuck, cadence 33 - the idle-dragoon
  shape is gone.
  2026-10-23 projected-contact kite LANDED in DragoonKitePolicy
  (owner doctrine: kite while cooling inside melee reach, healthy 2.0
  / wounded 2.6 tiles, never overriding an imminent shot; 4 pins
  reshaped to the new doctrine). V0 validation batch (rule, no
  latch, PARAM_TWEAKER=0): 3x VICTORY 1/0, hits 16/16/15 - the rule
  alone does not move hits vs history (12-18).
  2026-10-23 finishedShooting escape release LANDED (DuelMemory latch:
  observed projectile aged 1-2 frames = damage committed; FightTarget
  releases the swing hold + settle on it). V1 batch (rule+latch):
  9/14/12 + confirm 13/12, all VICTORY 1/0 - mean 12.0 vs V0 15.7.
  Mechanism proven live (SHOT released-finished 30 rows/game, 2 per
  cycle at cd 20-24 - damage lands ~cd 22, cooldown ticks from order
  issue, not from damage); KITE cd-histograms identical across V0/V1,
  so the gain is 1-3f earlier first steps flipping zealot swings to
  misses, not a different step pattern. n=3+2 vs 3 is suggestive,
  not conclusive - bigger batch or the V2 push decides.
  2026-10-23 V2 push (window [0,2], release on the firing frame -
  bullets update before battle decisions, so age 0 is already
  committed): 10/13/8, all VICTORY 1/0, mean 10.3. No break, no
  freeze, kills intact - the floor is reached (no earlier release
  exists: without a projectile there is nothing committed). Live
  proof the wider window fires (released-finished 45 rows/game,
  3 per cycle). Ladder totals: V0 15.7 -> V1 12.0 -> V2 10.3.
 2026-10-23 V2 significance batch (6 games, same build): 13/10/6/8/10/8,
 all VICTORY 1/0 - combined V2 mean 9.6 over 9 games vs V0 15.7 over 3.
 The floor holds: no freeze, no DPS loss anywhere on the ladder.
 2026-10-23 step-amplitude grid (owner question: escape steps stuck at
 1.7-2.0 tiles): KiteStepTiles 2.0/2.5/3.0 x 3-4 games, PARAM_TWEAKER=0:
 S2.0 11/13/4/14 (mean 10.5), S2.5 10/10/18 (12.7), S3.0 11/12/10 (11.0),
 all VICTORY 1/0 - no gradient. Amplitude is NOT the binding constraint
 (consistent with the 24% no-step rate: longer steps need room the map
 does not have, and the S2.5 18-hit game showed 39% no-step). Latency
 was the constraint and the latch fixed it; the remainder is separation
 physics (~1px/f) + pocket geometry. No ParamTweaker random sweep needed
 for this knob - answered by grid, cheaper and interpretable.
  Game 3 of V2 may have run with the #183 migration in-tree
  (jar rebuild race) - proven behavior-neutral (the only caller
  of the migrated consumer is dead code), batch stands.
  2026-10-26 DEMOTE_STALLED twin LANDED (cid/type/tile/reason per
  demote): the 231335 first live sighting had only a builderLog-gated
  console line - the recovery half of the stall lifecycle is named now.
  Related open question (WO-144): which goal re-fires V2 Pylon pairs
  (BO row without coming-suppression vs pipeline overlap vs door hole)
  - goal-source twin LANDED (V2_GOAL_SOURCE on provider goals at
  collectGoals, lists tag at creation, pinned V2GoalSourceTest 5/5):
  the next live pair reads its authors off the source column.
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
  or VICTORY with tails, none silent). 2026-10-20 probe `162410`
  (OpenBW 1v1): killed at 20 s by BOT_KILL_SECONDS with full tail
  (Connected, no Connection successful, thread dump) - a NAMED end,
  explicitly not the silent shape. Contrast datum 2026-10-18 (`144920`: host died
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
  (2026-10-21 TauCross: 9.7 game-min but 2 bases from 6:59, ORDER_SAVING
  0 - long enough, wrong shape; the 1-base lottery continues).
  (20261014_172954 reached only 324 s; `20261015_185046` reached 575 s
  in 141 s wall - heavy economies sim ~4x, so 700 s needs a lighter
  game or more wall). Absent in all retained runs incl. the 743 s one
  (3 bases there - the predicate correctly stays silent, it wants 1).
  `200123` is the first 1-base 700 s+ run (944 s, Nexus=1): ORDER_SAVING
  still 0, bank peak 8883 (predicate wants < 550) - correctly silent.
  Lottery 2026-10-19 (`154329`: 428 s, Nexus=1): still short of 700 s,
  never a saving shape. 2026-10-21 TauCross macro: 1 base, 670 s, ORDER_SAVING 0
  (short of 700 s again - the 1-base lottery continues).
  2026-10-21 Wine macro (20:09, 4 bases from 6:56): ORDER_SAVING 0 -
  correctly silent again (multi-base, the shape not the duration).
  2026-10-21 second Wine macro (7:53 Defeat, 1 base the whole game):
  ORDER_SAVING 0 - right shape (1 base), 227 s short of 700. The
  lottery continues, one axis at a time.
 2026-10-22 Wine TauCross macro (VICTORY 6:49, 1 base, BUILDER_LOG=1): no Nexus warp all game - right shape, 409 s short of 700 s; the 1-base lottery continues.
 2026-10-26 macro 091041 (367s VICTORY 1/0, 1 base, Nexus never queued, Nexus=1, no Forge): right shape, still far too short of the 700 s+ lottery.
  → full chain: _AI/WINE-MACRO-DATUMS.md §4

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
  No misfire observed; the rule stays. 2026-10-21 TauCross macro: zero
  room-around refusals, zero PYLON_FIND_FAILED, 9 Pylons warped - second
  no-harm datum, still no firing shape.
- **#84** Wine Pylon ordered but unbuilt: late-game instance (5:01 +
  38 s, bank hoarded) and early-game instance (READY from ~100 s, 9/9
  + 400 bank at log end). Worker stall, placement refusal, null
  builder and cancels all ruled out from flag-off logs. Needs a
  `BUILDER_LOG=1` Wine game (builder? site? no attempt?). First datum
  2026-10-19 (TauCross, BUILDER_LOG=1): Pylon#1 reserved worker #5 at
  pick 0:13, STARTED 0:46, warped [16,46] 0:58 - pipeline healthy on
  this game; the two named instances were other games. Second datum
  2026-10-21 (TauCross vs Zerg, BUILDER_LOG=1, 9.7 game-min Defeat):
  55 reservation/assignment lines flow; BUILD_BREAKDOWN on Gateway
  [123,26] (worker frozen 74f at dist 17.2, probe #197 in footprint,
  canBuild=false all variants) then self-recovered, walked in, and
  the Gateway WARPED 8:06 (ledger) - stall-with-recovery, second
  instance of the 10-19 [3,47] class, NOT a third #84 stall (builder
  moved and delivered). Correction trail: EVENTS carries no STARTED
  row for cid=96 (STARTED-twin gap on the builder path - warp proven
  by ledger, twin silent), and cid=98/oid=67 CANCELs "Null position"
  at 8:07 with zero REQUESTED rows anywhere (terminal without
  origin - request-twin gap on some re-request door; next: read the
  removal/mint paths for the untwinned exit, no blind patch).
  2026-10-21 Wine macro: Pylon#1 warped 0:48@8 (BO expects supply
  8) - pipeline healthy, no new #84 instance.
  2026-10-21 twin gap closed in code (stub-pinned
  `WarpStartedTwinTest` 2/2, suite + checkstyle green): one STARTED
  row per construction via `ProduceBuilding.noteStarted` - adopt names
  the site as before, the warp observer names the warp tile when
  adoption never rowed it (the cid=96 class). Live proof rides the
  next legacy-minted warp game.
  2026-10-21 mint-answer verified, no twin added per the decision:
  retained TauCross macros show siteless CANCELs carrying cid+oid+
  reason (e.g. cid=102/oid=82 "Null position for Assimilator",
  tile "-") - the CANCEL reason reads as designed.
 2026-10-22 Wine TauCross macro (VICTORY 6:49, BUILDER_LOG=1): pipeline healthy - Pylon#1 0:39@8, 17 STARTED with 0 CANCELs, zero stuck constructions, no new #84 instance and no twin gaps this game.
2026-10-26 macro 091041 (VICTORY 1/0, 367s game, zero own deaths): 0 ADOPTED; 1 CANCEL (Pylon [97,114] f4324 resited [98,115] warped - cancel-and-replace, 17f gap); 0 DEMOTEs; 0 BREAKDOWNs; deaths 0, kills 1 per verdict (unattributed in archive); Pylon#1 f964 [92,113] (bottom spawn); Nexus never queued (final Nexus=1).
  → full chain: _AI/WINE-MACRO-DATUMS.md §5

- **#74** Wine builder variance remainder (requeue/distances/debounce
  halves verified): needs a flagged Wine game for alternating
  Builder/Gather on a site and the exact 1.4-tile parking case.
  Instance observed 2026-10-19 (TauCross Wine, BUILDER_LOG=1): a Pylon#1
  builder pathing-stalled at map edge [3,47] for ~90 frames (MOVE order
  re-issued, distToSite frozen, BUILD_BREAKDOWN x2) and self-recovered -
  the Pylon warped on schedule. Transient stall, twin worked as
  designed, no code change (would be blind). The reservation→starter
  link reads cleanly off the new cid/oid chain on the next flagged run.
  2026-10-21 Wine macro (BUILDER_LOG=1): Pylon [123,30] builder #128
  stalled 159f travelling then moved (self-recovered); Gateway
  [105,16] two builders (#5, #117) stalled 119/134f same frame, warped
  7:30 - stall-with-recovery, twin worked as designed, no code change
  (would be blind). The dual-builder same-tile stall is the
  alternating-handoff shape to watch for. G1 short flagged macro: clean.
 2026-10-22 Wine TauCross macro (VICTORY 6:49, BUILDER_LOG=1): transient stalls only - 13 MOVE_BUILD (max 133f) + 4 BUILD, all self-recovered, 17/17 warped; no alternating-handoff shape.
 2026-10-26 macro 084359: same schema (0 MOVE_BUILD rows); 3 twin breakdowns (Gateway [14,51] builder-on-site f2040 - same frame as B49's, deterministic opening-tile signature; Pylon [24,44] x2 unexplored 2/4 attempt-phase; +7 untwinned transcript rows - twin fires per construction early only); 15 warps from 16 STARTED rows (attempt-only oid44 never STARTED; every started order warped incl. resites); zero alternating-handoff evidence either way.
  → full chain: _AI/WINE-MACRO-DATUMS.md §6

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
  2026-10-21 Wine macro (20:09, Forge 6:52): Singularity completed
  7:30, Legs 13:37, Weapons L1 14:33, L2 17:43 (UPGRADE_COMPLETED twin
  4x, 4 bases by 15:54) - the post-Forge window EXISTS and completes
  through L2; L3 still never reached, still pending its richer window.
  2026-10-21 second Wine macro (7:53 Defeat): Singularity completed
  7:30 anyway - the window opens even in a lost short game.
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
  control clean, Wine proof still owner-side. 2026-10-21 Wine macro
  datum (TauCross, ledger): Gateways 1:27/1:53/4:11/6:01 + dynamic
  7:51 [105,16] and 8:06 [123,26], all warped - lateness pattern
  not reproduced. No blind patch.
  2026-10-21: 5th-8th clean runs in a row (1:25-1:28 / 1:47-1:56,
  all warps) vs 3 late runs in the archive (2:37, 3:10/3:52,
  2:53/3:03) - intermittent, still no blind patch; the ledger
  keeps naming the layer when it recurs.
  2026-10-21 Wine macro: Gateways 1:26@11/1:51@13 + 4:05/6:30/7:20
  + 17:19/18:18, all warped - lateness not reproduced again.
 2026-10-22 Wine TauCross macro (VICTORY 6:49): Gateways #2/#3 on time 1:26/1:51 (BO audit flags +1 supply on #2/#3/#5 - one-supply lag, all warped); lateness not reproduced.
  B52 breaks the Cyber part while gateways hold +1: Cyber warped sup14 vs At22 (-8, lowest on record, prior min 17); P8 sup21 on plan (rare, 1 prior in chain); P11 +4. Gateway lateness still not reproduced.
2026-10-26 macro 091041: systematic +1 lag (Gateways #2/#3 sup 11/13, Pylon #5 sup 17 vs At 15 (+1), Pylon #8 sup 22 vs At 21 (+1), Pylon #11 sup 29 vs At 27 (+2); CyberneticsC sup 22 vs At 22 ON PLAN, warped clean; Assimilator sup 23 vs At 23 ON PLAN, warped clean; console audit 4 off-plan (GW#2, GW#3, P5, P11) - matches the ledger; +2 flags while +1 passes (audit tolerance confirmed again) - lateness not reproduced.
  → full chain: _AI/WINE-MACRO-DATUMS.md §7

- **#94** Enemy never spawns on OpenBW (host auto-menu path): 6+
  enemyless games across TauCross/Python, Zerg/Terran (`enemy: ` empty,
  0/0, scouts find nothing in 18 min). Plumbing verified correct;
  failure is deeper (slot never reaches occupied/computer_game).
  Blocks all fighting proof (cluster FIGHT/FLEE, kiting, rush twins).
  Paths, cheapest last: owner-authored UMS map with a computer-slot
  enemy (no editor on machine); C++ host slot debugging (shared
  platform, owner call); Wine games with real opponents (works today).
  2026-10-23: sim zombie (`tests.e2e.ZombieAttacksNearestUnit`, FakeUnit-only)
  is not engine proof - Wine vehicle stands for fighting proof.
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
  all - assignment varies in a way no log explains yet.
  2026-10-21, third reading (other model, engine-side, tests pinned
  in OpenBW): the 137-vs-14 gap is NOT a missing Zealot - declared
  = visible + Special_Map_Revealer helpers (slot 1: 14 + 123 = 137;
  the 123 never enter visible_units by design). The server slot line
  now prints all three numbers, so nobody hunts "lost" units again.
  What REMAINS open is the client stall past Connected (server
  publishes deterministically) - next: DEBUG the OpenBWRunner attach
  path for the blocking call. 2026-10-20 probe (`162410`, fresh jar,
  1Drag_v_1Zeal): server PUBLISHED 1 live game, client printed
  "Connected" (UMS_LOG line 16) and never "Connection successful";
  a thread dump names the block: main in native socket read <-
  `ClientConnectionPosix.waitForServerData:90`
  (`while (read() != 2)`) <- `Client.connect:181` <- `reconnect:66`.
  Killed at 20 s by BOT_KILL_SECONDS, 0/0/0. So the stall IS the
  connect-time handshake read; the server's byte 2 never goes out
  (their handshake probe: NO_BYTE_2 after 12 s on a live server,
  negative controls pass) because `GameImpl::update()` - the only
  path to `server.update()` -> `callOnFrame()` -> byte 2 - is dead
  code in the OpenBW build (no StarCraft.exe hook; `BWData.cpp`
  drives `next_frame()` directly). Our client-side dump is
  consistent (blocked read, nothing to receive). 2026-10-20 their
  pump landed (server log: `handshake sent from update()`):
  HANDSHAKE CLOSED - 2/2 probes print `Connection successful`
  (1v1 `170529`, M&M `170755`/`171136`, fresh jar both sides).
  Binding NPE fixed with it (units array field-init + no-wipe
  guard, JBWAPI-Atlantis `8cc9ef2`; early-init attempt withdrawn
  same day - player table is empty pre-tick, AIOOBE measured).
  New measured layer, engine-side: the match never ticks on UMS -
  zero frame lines in server.log, no MatchStart, bot loop never
  entered (no HELLO/BUILD ORDER, kill at 20 s with tail), although
  slots hold real armies (M&M: 30 + 14 visible). Client cannot fix
  a non-ticking match (players/forces publish on tick). Precise
  ask: extend their handshake probe past BYTE_2_OK to frames +
  MatchStart arrival; the pump must tick the match, not just the
  handshake. 2026-10-20 melee E2E (`173536` long, fresh jar both
  sides): same shape on MELEE - PUBLISHED, Connection successful,
  zero frame lines, no MatchStart, players NPE pre-MatchStart
  (units guard held, next layer confirmed), kill at 20 s. Melee
  played 1209+ frames Oct 19 on this vehicle, so the tick died
  with the pump change - regression pointer, not a new unknown.
  2026-10-21: their pump ticks frames (313 delivered, 312 answered
  on M&M; slots hold 30+14 visible). Remaining gap, precisely
  bounded: no MatchStart, ever, and the player table is empty
  (`getPlayerCount()==0` -> AIOOBE inside `init()`, measured twice).
  Client side is now maximally robust and committed
  (JBWAPI-Atlantis: units field-init, lazy init on first event
  with retry + no-wipe guard; Atlantis: idempotent onStart +
  first-frame bootstrap) - none of it can conjure a match start.
  2026-10-21 CORRECTION and closure: with the ticking match,
  MatchStart DOES arrive (HELLO printed), players DO publish, init
  succeeds - run `183729` played to 3:00 game time with an 82k-row
  fight (CLUSTER/FIGHT flowing, 17 hits taken, marines trading
  and dying). The frame-2 Defeat was OURS, not the engine's:
  `ForceExitWhenInvalidState` misfired on supply 0/0 + bank 10000
  (absent supply data read as no army) - fixed the same day
  (unpublished supply never trips; pinned
  `ForceExitNeedsPublishedSupplyTest` 2/2; their
  GameEvents.cpp:150 theory falsified by our own ForceExit
  banner + GAME_END twin at frame 2). What the unblocked game
  surfaced became NEXT #163, closed the same day (commit
  `a598229c2`: SHOT counts acquisitions by design, UNIT_DIED latch
  holds 3 rows for 3 marines). Cross-platform confirmation
  2026-10-21: per-unit SHOT rate identical on Wine and OpenBW
  (~0.5/frame - totals scale with duration x units, no twin bug),
  UNIT_DIED fires for our deaths only (silent on Wine 1Drag with
  10 kills and 0 own losses - by design, not a miss). Verdict
  quality (W/L) still needs longer than the 30 s UMS cap.
  Detail: `docs/openbw-run-regression.md` (theirs). What IS
  settled: with a unit but no base the bot idled a whole game
  (Forge FE head, no scouts, no engage, Defeat by map timer), and
  that failure mode is now fixed regardless of slots (baseless
  ATTACK marches from the army center; proved WIN 1/0 in 499
  frames on Wine, 34 stepped-aside vs 9 stuck, cadence 33).
  Default unknown-matchup BO flipped to Zealot into Goon the same
  day (Forge FE armed an economy opener on a duel map).
  2026-10-21 hosting fix (Atlantis scripts, no engine change):
  `run-openbw-ums.sh` + `--smoke` pass ENEMY_COUNT=7 (was the auto-menu
  default 1, which closed every EitherPreferComputer slot past the first
  and deleted scenario armies on slots 2+: 8Drag spawned 8 of 16 Dragoons
  and the match never started, bot hung at connect). One command now
  plays all three duel maps: 1Drag PASS 0/0/10 (506f), 8Drag PASS 18
  hits (307f), LingsUpg2 PASS 66 hits (195f, map self-healed from the
  live tree into the stale harness snapshot). 8Drag/LingsUpg2 inflate
  the bot KILLED counter (~7k/~31k - per-frame re-fire class, BUGS
  B-42); TIMES_HIT stays sane, verdict unaffected. Also fixed: `--game`
  mistook the bot's own 124 (inner 20 s cap) for the outer 180 s
  timeout; the inner KILLED: line now disambiguates.
  Who plays the enemy on each platform (OpenBW: nobody - engine default
  AI only; Wine: the map's scripts): `_AI/CHALLENGES/UmsDuelOpponents.md`.
  Detail: `_AI/CHALLENGES/StationaryEnemy.md`.
  2026-10-21 OpenBW speed + verdict slice (this model): the engine
  ticks ~600 fps on current builds (12.7k frames inside the 20 s bot
  cap - the 24 fps reading in the other model's research doc dates
  from the pre-pump stall era; the hasSynced gating it verified in
  code still holds, it is just no longer the bottleneck). Two fixes
  on our side: (a) halt() instead of exit() in the OpenBW exit path
  (the JVM lingered to SIGKILL, burning the cap + 5 s every game -
  now 20 s/124 instead of 25 s/137); (b) WIN/LOSS/DRAW verdicts in
  `build-openbw.sh --game` (a blanket PASS hid undecided games -
  DRAW = completed with zero losses, investigate engagement not
  defeat; fixture-pinned 8/8, live DRAW proven 0/0/6 on 1Drag);
  plus a `TIMEOUT_SECONDS` env knob (15 s smoke plays 175 game-sec).
  Stronger evidence on the 1Drag slot problem the same day: the
  enemy zealot deals damage at 0.5 tiles (HIT rows) while `enemy: `
  stays empty all game - spawned and hostile, but never visible to
  the client. Engine-side (other model's territory), filed here as
  the shape to name, not to fix blind.

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
  10:30; open whether the wall ever had the DPS). 2026-10-21
  re-analysis of that loss: only 14 of 370 HITs landed near the wall
  anchor [106,42]; deaths cluster at [54,110] then [70,86], away from
  it. No attack onto the wall occurred, so the game cannot settle
  DPS-sufficiency either way - HOLD proof needs an attack onto the
  wall (or a sim siege), not just another FE macro.) [ ] M5 module extraction (assessed
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
  supply-4/bank-74 deadlock shape absent again). Re-checked 2026-10-21
  TauCross macro: lowest-supply reserved>=250 at sup 20 (Pylon,
  supply-blocked with bank 302, game flowed to 11:15) - the supply-4
  deadlock shape absent again. Re-checked 2026-10-21 across 99 archived
  Wine + 46 UMS logs (ORDER_NOT_READY twin supplyUsed): 1136 rows
  >= 250, lowest sup 10 (frame-3 BO-load CyberneticsC, bank 250,
  transient like the earlier sup-10 row) then sup 13+ in flowing games -
  the stuck-at-low-supply shape absent everywhere.
 2026-10-22 re-sweep across all 45 retained EVENTS logs: zero reserved>=250 rows at supply<=10 - the deadlock shape absent everywhere again.
 Re-checked 2026-10-26 (180 unique logs, incl. 091041): still only the four artifact instances - same test-boot shape, no deadlock shape.
 Re-checked 2026-10-26 (214 unique logs): 4 reserved>=250 rows at supply<=10, all frame-3 BO-load CyberneticsC at sup 10 (test-boot transient, same family as the sup-10 artifact) - the supply-4/bank-74 deadlock shape absent everywhere.
  → full chain: _AI/WINE-MACRO-DATUMS.md §8

- **#96** Wine verification batch (headless, model or owner): painter warping labels
  (`Gateway @12 [NOT_STARTED]`), WINE_LOG/_DEBUG/_DECISIONS triple,
  E2E_LOG/_DEBUG/_DECISIONS stable files, Missions/BO-mission unenaction sanity.
  Jar 12.1 MB already carries it all.
  2026-10-20: file-outputs half verified on a real Wine game
  (WINE_LOG/_DEBUG/_DECISIONS triple + BUILDINGS.json all collected,
  non-empty); painter labels stay a visual owner check (eyes only -
  the batch itself runs headless, model or owner).
  Open tuning question for the batch: Pylon prepared at 15 warps ~2:24,
  past the 17/17 block - adoption+travel+warp (~25 s) outruns the 15→17
  window (~25 s), so any churn lands past the block (churn itself fixed
  2026-10-17; lever if still late is `IsReadyToProduceOrder` pull-forward
  amounts, currently +1 below 19 / +3 above - earlier pylons trade tech
  minerals for block time).
 2026-10-22 Wine TauCross macro: file-outputs half now complete - WINE_LOG/_DEBUG/_DECISIONS triple + BUILDINGS.json all collected non-empty; only the painter labels stay owner-visual (eyes only).
 2026-10-23 file-outputs re-verified on macro 212818 (triple + BUILDINGS.json all non-empty). E2E_LOG is OpenBW-runner-side (run-openbw-e2e.sh), not a Wine artifact - narrowed out of this item. Missions unenaction sanity now instrumented: global mission transitions row a MISSION twin (from/to/reason, change-only - followPosture runs every frame), pinned MissionTwinTest; painter labels + a game exercising missions stay owner-side.
 2026-10-26 file-outputs re-verified on macro 235430 (triple + BUILDINGS.json all non-empty). CherryVis trace proven on Wine 2026-10-23 (B53 110209 game: CVIS/ with all 4 artifacts archived, GENERATE_CHERRYVIS_REPLAY=true default in ENV-WINE-FAST; viewer opening still owner-side). UMS duel runs collect EVENTS.jsonl + WINE_LOG but no BUILDINGS.json (baseless by design) and no DECISIONS.log - answered the same day: by design, not a gap (DecisionLog.flush() no-ops on empty rows; record() fires only from production paths, duels produce nothing).
 2026-10-26 attribution rule (learned the hard way): a live console belongs to an archived game only if Pylon#1 tile + maxframe + STARTED rows match - similar-looking macros blur together in memory. Never quote console details (verdict, kills, BO audit) into a game datum without the archive cross-check.
 2026-10-26 replay naming closed (owner: replays lacked opponent + engine): engine template stays the proven %MAP% + strftime (any extra placeholder silently saves NOTHING on the macro path - B64 ENEMYNAMES, B65/B66 BOTRACE/ENEMYRACES, B69 static PvsZ all zero-rep; duel path unaffected, saved PvsTZ once), the script bakes MAP_PvsZ_wine_DATE at archive time from its known races (first live proof B72: engine rep + rich copy, both 71KB). Replay *saving* itself is flaky on the macro path (old template 7/9 incl. B70/B71 no-rep; needs engine-side look, owner territory) - the rename rides every save but cannot conjure one; teardown grace guard (20s wait-for-rep) in place: B72+B74 both saved promptly (no wait triggered), so the guard is cheap insurance with no live proof it ever fires - keep until it does or a season passes without a slow save.

- **#34** Stub twins hold (4pool/9pool baselines pinned); a real
  opponent needs a scripted UMS map (one client per OpenBW host) -
  owner-supplied, tier stays owner-run. Sim zombie covers reproducible
  defense in-process only, never engine proof. Probed 2026-10-19 with
  `USE_MAP_SETTINGS` in the runner: `ZealDrag_v_Lings` runs the full
  1209 s but hands us a melee start (Nexus+probes, no army) and spawns
  no enemy (0/0) - no fight to assert; `8Drag_v_8Drag` ends at frame 0
  (human slots) with the exit-path backstop firing. Neither map yields
  a battle; the tier still waits on an owner-authored computer-slot map.
- **#65 / #68** CherryVis code fixes are in (types, legend, seed,
  writer path, `--cvis` plumbing); what remains is the owner opening a
  real trace in the viewer (plus the `[BW<->CP Matching]` console line
  if `???` persists).
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
 2026-10-23 macro 225319 behavior baseline (legacy pumps, pre-cutover): "Protoss rich but idle" console ~40x (bank 600-780, gas ~300, supply 40-49/65, 3 free gateways, dragoon/zealot HigherPriorityWaits) - army pumps idle with bank; game still VICTORY 3/0 by supply-cap exit, shape recorded, no verdict.
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
    2026-10-26 DRY_RUN macro 002228 re-proof LANDED: V2_GOAL_SOURCE
    2676 rows, Pylon goals dynamic 1715 + build-order 961 - dual
    authorship confirmed live again on the current tree; pumps still
    not silent (BOARD_FED rescues with pumps present) - still no
    candidate, same as step 1. The twin answers WO-144's question
    live: both lists fire Pylon goals, the source column tells
    which when a duplicate pair lands.
    No duplicate Pylon pair landed this game (2 Pylon requests, 0
    proximity cancels), so the storm-class adjudication itself still
    waits for a storm game - authorship firing is proven, attribution
    of a pair is not.
    2026-10-26: V2_GOAL_SOURCE twin silent in all 140 retained logs -
    root-caused, not a twin bug (wired + pinned): PRODUCTION_V2
    defaults OFF and no Wine env sets DRY_RUN, so collectGoals never
    runs. Authorship evidence since 10-20 rides DRY_RUN games only;
    routine macros (V2-off) cannot re-verify it. Next: one DRY_RUN
    macro to re-confirm dual authorship on the current tree - DONE
    2026-10-26 (002228, see datum above); repeat on behavior changes
    to the scheduler, not on a schedule.
  - **#145** Site-finding chain (`NewConstructionRequest` + `FindPosition`/
    `DefineExactPositionForNewConstruction`, the "ConstructionFinder") →
    catalogue-only sited demand through the adapter funnel
    (03_PLACEMENT.md; exact tile honored verbatim, power + refusal twins).
    Exit: every construction sited by the catalogue, the chain in src-old/.
    2026-10-21 mechanism + acceptance test (Wine macro): the [119,12]
    storm (463 same-tile proximity-finish CANCELs, 3 Pylon oids,
    4.1 game-min) mints through THIS chain
    (`DefineExactPositionForNewConstruction.exactPositionForNewConstruction`),
    which has no standing-cover check - the spiral's refusal lives in
    the other funnel (`RequestBuildingNear`). No local patch (would
    duplicate the guard in the dying path); after cutover this storm
    class goes quiet (0 same-tile storms) because the catalogue refuses
    standing-covered sites.
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
  across two more games. Next gate proofs ride the next macro game. 2026-10-21 TauCross macro: min-supply 7, core-priority 1, earlier-reserve 6, funding 1, pylon-cap 8, pylon-overstock 7, unstarted-pylon 3 - all previously-proven gates re-fire, no new gate proven. 2026-10-21 Wine macro (20:09): earlier-reserve 92 + pylon-cap 41 + prepare-site-overlap 19 (first live fire of the young spiral guard: Stargate* refused at covered [105,2] 3x, warped [123,26] 9:07) + unstarted-pylon 8 - evidence (Stargate case now complete: 15 correct refusals, resite, warp - proof CLAIMED below). 2026-10-21 Wine macro: prepare-site-overlap PROOF on the Stargate case (15 correct refusals of covered [105,2], resited, warped [123,26] 9:07 + second [123,33] 9:17) - first per-gate proof from this game. 2026-10-21 second Wine macro (7:53 Defeat, quiet construction game: CANCEL 1, REQUESTED 10): min-supply 4, funding 2, core-priority 2, pylon-cap 1, unstarted-pylon 1, warping-link 1 (first live sighting: PhotonC #60 @ [19,19] 7:33@41, bank 254, duplicate-cannon-shaped) - previously-proven gates re-fire, no earlier-reserve storm this time. 2026-10-21 audit: all 11 gates have stub behavior pins (LiveConstructionGuardTest 6x, power-not-funded fires+passes in BuildRefusedTwinTest); the two never-fired-live gates are narrow by design, not broken - power-not-funded needs Pylon-less poverty no BO produces, live-construction needs an adopted-live re-request and cancelled rows are terminal (that's why the 463-storm never tripped it: terminal is not live). No more pins without games; live proof rides shapes that may never come - accept stub coverage for these two. Observatory [101,5]: 4 refusals correct (tile taken by warping Citadel 11:30), resited [118,30] 11:37, builder #126 killed by ling raid 12:28, warp froze with zero further twin rows (no orphan-drop twin for a builder-dead warp - twin gap filed in #150, not a guard failure).
 2026-10-22 Wine TauCross macro (VICTORY 6:49): previously-proven gates re-fire - min-supply 6, pylon-cap 6, earlier-reserve 3, unstarted-pylon 3, funding 2, core-priority 2; no new gate proven.
 2026-10-26 macro 091041 (13 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 1x (Pylon), core-priority 1x (Pylon), pylon-cap 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (same four stub-pinned); zero core-single-instance.
 2026-10-26 lock-pair comparison B22=060844 vs B36=072350 (archive-verified, no behavior change): same zombie oid 9 both games (BO #9 Cyber: STARTED f4331 cid52 / f3901 cid49, refresh-cancelled f4393/f4117, first refusal 15f/6f later); re-request oid differs (26 vs 24 - dynamic counter, zombie constant); same 300f grid (B22: 34 rows = 17 perfect same-frame pairs f4408-f9208; B36: 33 rows f4123-f8923, opens as same-frame pairs on the grid, phasing drifts later in the game, zombie missing once 16-vs-17); both terminal (zero Cyber warps either game). Invariant is two bare Cyber rows coexisting on the 300f grid, not their phase - consistent with the order-row retirement fix staying owner-side.
  → full chain: _AI/WINE-MACRO-DATUMS.md §9

- **#156** One `EarlierDemand` reserve (SOLID review, agreed): three
  models of "already reserved" exist (`ProduceBuilding`
  earlierUnfinished*, `ShouldNotTravelToConstructYet.earlierUnpaid`,
  `GoalBoardWallet`/`ReservedResources`) with different sorting and
  exclusions - every duplicate-Pylon race reads a different sum.
  Inventory 2026-10-21 (four models, not three): (A) `earlierUnfinishedCost`
  - universe live constructions, order by order id, excludes self /
  orderless / null-type / already-paid (live building unit = warping
  counts as paid), sums full prices; read by earlier-reserve +
  funding gates (they agree with each other). (B) `earlierUnpaid` -
  universe queue rows in priority order, stop at self, orderless self
  counts everything (hold rather than overspend); same paid rule.
  (C) `GoalBoardWallet.funded` - universe ranked GOALS (ladder order,
  neither queue nor id order), top-down virtual-bank spend.
  (D) `ReservedResources` - mutable global earmark counter (reserve/
  release, capped 500/300), no "already paid" concept at all.
  Divergence trigger: any queue re-prioritization moves B but not A;
  C moves on ladder re-rank. The duplicate-Pylon race (WO-144) reads
  a different sum from each - unification picks ONE ordering
  (queue position, the only one all producers share) with "already
  paid = has building unit" as the single exclusion.
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
  carry the semantics). 2026-10-21 sweep: no re-spelled disjunctions
  left in prod (countPreparingOfType, adopt-prepared check, painter
  orange tile migrated the same day, pinned by existing acceptance +
  unit coverage); the remaining status() uses are single-state checks
  or different predicates (hasStarted mixes, buildingUnit==null pairs),
  not disjunctions - migration complete pending per-reader game proof,
  which rides normal macros (no dedicated games).
 2026-10-22 sweep: zero re-spelled disjunctions left in prod (all readers via isWaiting()); Wine TauCross macro (VICTORY 6:49, 17 warps, 0 CANCELs) is another clean per-reader run.
 2026-10-22 re-sweep: still zero (only the isWaiting() definition, one
 isWaiting-based radius reader, and single-state checks).
 2026-10-26 re-sweep: still zero re-spelled disjunctions in prod (the
 only PREPARING equality left is the isWaiting() definition itself).
 2026-10-26 re-sweep: still zero (grep finds only the definition).

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
  1007/1007 + arch unchanged). 2026-10-21: step 4 LANDED
  (`mutateAroundUnfrozen` + test 6/6 in `TunerHallOfFameTest`:
  frozen params ride at champion values, rest explore inside
  bounds; suite 1008/1008 + arch unchanged). Step 5 (archive)
  LANDED 2026-10-21: run-wine-batch.sh writes OUTDIR/manifest.txt in
  TunerBatchManifest.render's line format (map, jar md5, per game: dir,
  verdict, hits, seconds, param digest + full vector; invalid games stay
  in batch.log), proven by a 1-game batch (WIN, manifest validated:
  structure, digest recomputation, batch.csv agreement). N-game sweeps run headless in background, model or owner (allowance
  2026-10-22); one game at a time, small batches first (single games
  are noisy). Step 6 (gradient loop) LANDED
  2026-10-21: TunerFitness TIMES_HIT x GAME_minutes (lower wins; a LOSS
  carries +1M so one scalar orders everything and losses still tune
  toward dying slower), abstract TuningScenario + concrete
  DuelHitsMinutesScenario (map, 3 replicates, all 26 params with a
  bounds registry as the single source of truth), TunerGradient
  (least-squares slopes vote the sign, span-fraction descent,
  frozen-respecting jitter, integral rendering for int params),
  TunerProposer + TunerDriver (hof + manifest in, env fragment out,
  BEST/STEP/RADIUS out, map-mismatch warning) and run-wine-tune.sh
  (batch, propose, halve step/radius when the best stops improving).
  Proved live: 2 rounds x 2 duels, best fitness 4.33 -> 2.67
  (13 -> 8 hits taken). 30-game run 2026-10-21 (10 rounds x 3, all wins):
  best 2.93 (8 hits / 22 s, round 8); round means 15-19 hits, spikes to 24 -
  duel noise dominates the gradient at K=3, so efficacy is unproven
  (K-of-N + more rounds needed); the loop machinery itself ran clean.
  Lesson: awk follows LC_NUMERIC - the halve-on-plateau schedule silently
  emitted commas (step=0,0250), which the driver rejected back to defaults.
  All schedule math now runs under LC_ALL=C. Engines are addressable (TuningEngine +
  wine/openbw/sim descriptors, --engine flag); only wine tunes today,
  the others fail loudly with their reason (no tweaker rows on
  OpenBW, no sim batch runner yet). Step 6 (gradient loop) LANDED
  2026-10-21: scalar fitness TIMES_HIT x GAME_minutes (lower wins,
  WIN always outranks LOSS), abstract TuningScenario + concrete
  DuelHitsMinutesScenario (map, replicates, full 26-param bounds as
  the single source of truth), TunerGradient (least-squares slopes,
  span-fraction descent, frozen-respecting jitter), TunerProposer +
  TunerDriver (halloffame + manifest in, KEY=VALUE fragment out) and
  the run-wine-tune.sh outer loop (batch, propose, halve step/radius
  when the best stops improving). Proven live: 2 rounds x 2 duels,
  best fitness 4.33 -> 2.67 (13 -> 8 hits), ints render integral so
  the factory never silently randomizes.
  2026-10-21 sim batch runner LANDED (NEXT #160-sim): `SimTunerBatch`
  in-JVM duels with the wine-batch contract (tweaker.csv + batch.csv
  + manifest.txt), `scripts/run-sim-batch.sh` wrapper, by-value
  `apply()` for all 32 params, SimEngine wired (`--engine=sim` works),
  batch->parse->driver loop proven in-test (SimTunerBatchTest 6/6)
  plus a live 1-game run (WIN, digest + override honored).
- **#164** OpenBW takeover scope (owner 2026-10-20: this model leads
  logic/changes, other model gets independent file-scoped tasks).
  Single source of truth: `/sc-ai/OpenBW/openbw` clean fork
  @`801e238` (`feature/support-ums-maps`) is the canonical engine;
  the harness copy `StardustDevEnvironment/3rdparty/openbw` is a build
  artifact, never edited directly (install flow via
  `scripts/build-openbw.sh --install` with backups, never blind rsync
  or plain `cp`). Atlantis owns bot logic + sim + twins; engine C++
  changes land only via the install flow + `tests/run.sh` green.
  Baselines 2026-10-20: host smoke `test-openbw-host.sh` PASS
  (BYTE_2_OK + MATCH_TICKING + MatchStart=YES + 12/12); engine
  `tests/run.sh --default-maps` ALL PASSED with
  `UMS_TEST_DATA_DIR=/tmp/scdata` (MPQ symlinks fix case-sensitive
  names); fast suite 1018/0 + sim 13/13 + strict arch 5/7 (2 known
  pre-existing edges). Duel vehicles: Wine stays the 1v1 proof
  (1Drag WIN 1/0); OpenBW `1Drag_v_1Zeal` is INCONCLUSIVE (0/0 kills,
  enemy slot empty, own dragoon takes hits then ends full HP);
  `M&M_v_ZealDrag` is the working OpenBW vehicle (slot 1: 14 visible
  + revealers, marines die to Zealot#277, verdict cut by timeout).
  Constraints carry over: Java 1.8 (`--release 8`); 120 s command
  budget, OpenBW sim cap 150 s wall; one game per box; Wine only on
  explicit per-case approval; never `git push`; behavior changes
  need game proof; fast suite green before commit; never touch or
  commit `src/main/Main.java` (owner-modified).
- **#166** Other-model task file (independent, test-gated, file-scoped;
  hand over only on their done-signal, then re-verify host smoke +
  engine tests before taking over): A3.3 revealers filter (decide +
  pin), B2 reconnect test (pin intended fail), E3 slots extraction
  (leaf function out of `BWData.cpp`), A3.1 map-failure survey
  (which maps `error()` on rescue-passive/unknown-flag), B3 real-CHK
  fixture (copy small real `scenario.chk`, flip one field), C1/C3
  script split (host-start vs bot-run, single SHM cleanup), plus Wine
  A1.4b spec (2 slots, Victory for player 1 at frame N -> what happens
  to player 2; do NOT change propagation before it; gate any change on
  `use_map_settings`). No verbatim StarEngine restore (revert
  `b60a7029` stands - new test-scope sim instead).
  (a) pinch default gate OFF vs keep ON; (b) overnight tuner batch +
  map; (c) V2 LIVE default flip per #45 ordering; (d) Wine A1.4b
  measurement approval (map + script ready first); (e) long
  macro/quiet-box games for #71 / #81 / #155 backlog (700 s+ shape,
  7th_rav proof, per-gate proofs); (f) commit permission for
  `docs/` + other-model scripts (until then: review only).
  Optional `IDEAS.md` only if the ideas list outgrows NEXT.
- **#170** Choke-point battle doctrine + SquadScout (owner 2026-10-20,
  highest priority): never push a narrow choke into a nearby enemy
  without crushing superiority; on defense wait as long as possible;
  stand ~10 tiles off the choke center (unseen, in range to collapse)
  so the enemy commits into the funnel and dies retreating. SquadScout:
  detach one unit as bait - outside the formation - to pull the enemy
  into the funnel while the line stands formed (works on open ground
  too: formed line shoots first). Architecture: `BattleStrategy`
  decider (one class: WHICH approach, from the detected setup) +
  one class per approach (concrete decisions, no shared knobs);
  strategies plug per detected setup (choke near? outnumbered?
  duel?). First slice: the decider + standoff/hold-vs-push choice;
  bait and collapse ride later slices, each with sim + game proof.
  2026-10-21 live-shape finding (2 full Wine macros, 30 game-min):
  the strategy-regroup branch fired 0x (regroup fallback
  "ClusterMove" 9x) - the army is either in contact (fight branch)
  or away from chokes when quiet. The no-contact-near-choke shape
  these games never produce is exactly what the live proof needs;
  duels can't supply it either (baseless forces kite/fallback).
- **#171** High-ground choke scoring (owner 2026-10-20, formation
  follow-up): for every choke, score BOTH sides as defensive spots -
  height delta first. A wide choke with a step is not "pointless": the
  low side cannot see up while the high side sees down (vision
  asymmetry), and shooting uphill misses (engine behavior - pin the
  exact factor against OpenBW before tuning it, never hardcode a
  remembered percent). Score = height advantage + funnel width +
  standoff room; cache per choke per side at map load (static data,
  map-analysis owned; compute once, read everywhere). Use: once the
  enemy base is known, pick the best defensive position on the path
  our base -> enemy base, go there and wait (feeds
  `ChokeStandoff.holdPoint`: prefer the high side, fall back to the
  10-tile rule on flat). Proof: pure unit tests on synthetic choke
  geometry (high side outscores low; stepped-wide beats narrow-flat),
  then a live game where the line demonstrably holds the high side.
 2026-10-22 wiring LANDED (high side preferred via HighGround.preferFoeSide + ChokeStandoff.chooseHold + foe-side candidate, #170-guarded: never march through the enemy to reach it). Pins: preferFoeSide 4/4 + chooseHold (mutation-proofed); the holdPointFor wiring itself is live-only by construction (test heights always read unknown, so the fallback path is behavior-identical in-JVM) - the TauCross macro never engaged it (Hydralisks are ranged, hold correctly silent). Live proof still rides a melee-choke game.
- **#172** Region-aware escape destinations (owner 2026-10-20, PurpleWave
  match review: losing won games to corner traps, Dragoons most of all).
  Small units slip through, large ones (Dragoon) die on a failed escape -
  and two early Dragoons decided a game. Stardust read, no copy: boids
  (separation + target weights, marked TODO-tune upstream) + walkable
  rays (furthest walkable >= minDist along a vector) + collision vectors
  + NoGoAreas + unstick-via-STOP; cluster flee is retreat-to-main (arc
  disabled as unsafe). There is NO region-aware destination scoring
  there - this is our own work. Ours today: `MoveAway` (straight +
  lateral aims, best-distance pick with min-gain floor) + margin kite,
  no region/size/peninsula awareness. Architecture (`battle.escape`,
  one reason per class): (1) `RegionShape` probe - min width/height of
  the current region + 8-direction walkable-boundary rays from the unit
  (Stardust-style ray march, per unit size: large units need wider
  clearance sooner), plus region-center pull (edges raise corner risk);
  peninsulas score as traps whatever the raw width. (2) Candidates: 8
  dirs x distances with the 8-tile escape minimum as the want (not a
  promise - the nearest obstacle decides), each validated walkable to
  the end. (3) Scoring, PurpleWave-detailed: corner/peninsula penalty,
  minerals/geysers/buildings on path or dest, ALL known enemies within
  20 tiles of the dest (not just the nearest - the pocket is made of
  the second Zealot), size factor (large = earlier caution, wider
  berth). (4) Objective: minimize hits taken AND time-to-win: shoot
  every cooldown (fleeing forever is losing slowly - the DuelKite
  standoff stays the engage half, this is the disengage half).
  (5) Tuning: `EscapeTuning` record with per-map overrides, optimized
  by the batch tuner (NEXT #160 objective: win, then hits, then
  seconds). Slices, each sim-proven (arena edges stand in for region
  borders; obstacle flavors extend the sim) then live-proven: probe +
  min-dim caution; 8-dir walkable candidates; static-obstacle +
  corner scoring; enemy-20 avoidance; size-aware clearance; tuner
  wiring. First consumer: `DuelKite.yieldStep` (replaces
  `MoveAway.bestDestination`), second: `ARunningManager`.
  2026-10-21 tuner wiring landed in code (no behavior change while
  the tweaker is off): `EscapeTuning` record (6 weights/radii, same
  recipe as CombatTuning, defaults equal the hand-tuned constants) +
  registration and bounds (32 params, registry test counts it) +
  `ParamTweakerFactory.apply` by-value write path. Slices #174-182
  (probe, candidates, static scoring, corner + enemy-20, size-aware,
  DuelKite wiring, pinch gate, second consumer, high-ground scorer)
  are closed; what remains is live superiority proof (needs games)
  plus tuner efficacy. Center-pull deliberately not added: the
  corner rays measure edge risk directly at each destination, a
  center prior would bias away from measured data.
- **#173** Owner escape doctrine: long-vector-first (owner 2026-10-21,
 1Drag_v_1Zeal edge-freeze; live Wine datum 2026-10-22: the dragoon
 flees short and takes 15 hits). Mechanism verified in code: short
 steps die in validation, not in scoring - `MoveAway.bestDestination`
 vetoes clamped micro-steps (half-distance gain floor) and
 `IsReasonablePositionToRunTo` gates (walkable(2), ground-distance,
 minerals), so every direction returns null at a wall and the caller
 fights in place; the scored pick never rejects, it returns the
 least-bad short step (shuffle into the pocket). Staged: (a) NOW:
 detector-first long escape - away-ray clearance <5 tiles means the
 straight-5 is blocked, so deflect at full length (the 8-dir long
 pick yields the sidestep emergently); open ground keeps the tactical
 short step bit-identical, boxed pockets behave as today. The kite
 footwork (yield-to-hold) stays short - lengthening applies to the
 failed escape, never to the standoff. (b) NEXT: explicit
 angle-stepping if the long pick still traps; the `EscapeCandidates`
 4/8/12 rings (no prod callers today) belong to this step. (c) LATER:
 precomputed peninsula/cul-de-sac map (cornerPenalty is local-only,
 ~6-tile horizon). (d) LATER: hard-map tuning vs multiple chasers
 (sim batch + TunerObjective already exist; enemyPressure already
 counts ALL in radius). Deliberately NOT primary: shorten-and-retry
 (shortening is what re-traps) and a long leg inside pure
 `pick()` (walkability is prefix-closed - verified no-fire; the
 differential lives in the validation clamps and gain floors, not
 in the scoring). Proof: unit pins + Wine duel (fewer hits than
 the 15-hit datum, no standstill frames). 2026-10-22 3-duel mini-batch on the current tree (all VICTORY 20-22 s): 16/16/14 hits taken, mean 15.3 vs the 15-hit datum - NOT proven (noise band), standstill clean (113 stepped-aside, 1 frozen-reset, 11 stuck-no-step across the batch). Honest gap: no twin marks the long deflect firing, so the batch cannot say whether the branch even engaged - observability first, then measurement. 2026-10-22 observability LANDED: the long branch rows `long-deflect` via KiteTwin (unthrottled, rarity is the throttle; diagnostics wrapped so they never break the decision), pinned by twin-presence + twin-absence tests, mutation-proofed. Re-run the duel batch to read engagement off the rows.
 2026-10-22 re-run, 5 duels on the #182/#183 tree (all VICTORY 20-23 s,
 497-563f, hits 14/17/16/14/12): zero `long-deflect` rows in 254 KITE
 rows (182 stepped-aside, 69 stuck-no-step, 3 frozen-reset) - the branch
 never engaged on open ground, which is the designed shape (deflect
 fires only when a wall eats the ray; open ground keeps the short step
 bit-identical). Engagement proof still needs a walled pocket map, not
 more open duels.
 2026-10-26 yield-path visibility LANDED (KiteTwin.yielded rows
 `duel-yield` next to stepped-aside, pin fires on the act-level yield
 board): 2-duel batch on the current tree reads 2+3 duel-yield rows -
 the pickEscape path IS alive, just rare (the cooling kite preempts
 the margin first on most cycles). So the long-deflect zero is
 explained, not vacuous: the branch gets ~2-3 chances per game and
 the maps are open (zoo batch: 14/8/4 VICTORY, still zero deflects
 despite 23% no-step in game 1 - where the away-ray is eaten, no
 5-tile pick exists either, so silence is correct). Main kite path
 deliberately still on MoveAway (rollout order: DuelKite yield
 first, ARunningManager second). Remaining proof unchanged: a truly
 walled pocket map, not more open duels.
 2026-10-23 kite-distance grid, 1Drag_v_1Zeal, all-33-pinned baseline +
 3 one-knob arms (K=3 each, headless Wine): A defaults 18/15/19 (mean
 17.3), B step 3.0 (was 2.0) 12/18/13 (14.3), C margin-base 2.0 (was
 1.0) 15/14/9 (12.7), D both 14/6/16 (12.0, best single 6). Directional
 win (foe>1.5 tiles share: 33% -> 50% -> 58% -> 72%), far from the 0-2
 target. Binding truth: dragoon topSpeed 5.0 vs zealot 4.0 px/f - the
 dragoon CAN open distance, so the residual contact is standstill per
 shot cycle (windup + settle + rearm latency while the chaser closes),
 not a speed cap. D-game2 anatomy: 4 of 6 hits land before f130
 (approach + first contacts), 2 on one f277 re-close; longest holds
 at 2-4 tiles (shots at dist 35-40) with zero hits. Next: push the
 margin out (E: margin 2.5 + step 3.0) instead of more blind grid.
 2026-10-23 round 2 (K=4): E (margin 2.5 + step 3.0) 12/18/14/15
 (mean 14.75) does NOT beat D; D-replicates 4/16/13/11 (D combined
 n=7: mean 11.4, best singles 6 and 4). Knob plateau confirmed:
 pushing margin 2.0->2.5 adds nothing (difference within noise,
 t~1.4). Same-config spread (4..16) dominates between-config gaps.
 D-rep game-1 vs game-2 anatomy: 4-hit game escapes to 2.7-4.4 tiles
 by f144 and holds it (0 further hits); 16-hit game oscillates
 0.5-1.9 all game. Bistable: the opening ~100f decides the attractor
 (escape vs glued), knobs only shift the odds. Next lever is opening
 discipline + per-cycle standstill, not longer steps.
 2026-10-26 the per-cycle standstill lever landed (finishedShooting
 escape release, #152): V0 15.7 -> V1 12.0 -> V2 9.6-10.3 across
 17 duels, all VICTORY - the step-amplitude grid run after it (2.0 /
 2.5 / 3.0, no gradient) confirms the diagnosis from both sides.

- **#175** Arc retreat toward the squad (owner 2026-10-21, LOW priority,
 future): a Dragoon costs ~two Zealots, so one dragoon kiting away from
 one zealot or two lings weakens the army; straight-line fleeing splits
 the army apart (strategic cost outweighing the micro gain). Idea: while
 within ~15 tiles of the own squad center, flee along an arc curving back
 toward the squad instead of straight away - stay in firing range of the
 pack, keep focus fire on the weakest enemy, kill it faster, win the
 fight. Uncertain per owner (may not work as imagined) and explicitly
 NOT urgent; test vehicle is the Dragoons-vs-Zealots map. Prove in sim
 first (arc policy vs straight policy, N games: win rate, time-to-win,
 army-split distance), then live. (#174 stays with the tuner slice
 series; this takes the next free number.)
 2026-10-22 sim-first slice LANDED (no behavior change, default blend 0):
 `EscapeArc` pure geometry (blend rotates the away-vector toward the
 squad, length kept) + `EscapeTuning.arcBlend` (deliberately untuned,
 no registry row until efficacy proves) wired default-off into the
 margin-kite branch (Alpha roster fallback: units carry no back-ref,
 `unit.squad()` reads null live and in sim). Sim A/B 3v3 dragoons vs
 zealots, 400 ticks each: peak split 4.21 -> 3.10 tiles, deaths equal
 (0/2 both), shots 32 -> 39 (the arc stays in range). Pinned
 (`EscapeArcTest` 4/4, `ArcRetreatSimTest` A/B, both mutation-proof).
 2026-10-22 efficacy matrix + registration: blend sweep {0, 0.5, 1.0}
 on 3v3 (split 18.17 -> 1.99 / 1.98, kills 1 -> 2 / 2, shots 18 ->
 42 / 50, zero dragoon donations) and 3v5 (split 10.68 -> 1.99,
 kills 2 -> 3, shots 33 -> 38, zero donations) - the arc collapses
 the scatter ~9x/~5x and doubles the guns on both geometries.
 Full bend marginally tightest on 3v3, so no basis to crown 0.5:
 default stays 0, `EscapeArcBlend` registered 0..1 (bounds +
 by-name apply + 3 count tests 32->33) and the tuner picks the
 value. Pinned (`halfBlendBeatsStraightAndFull`,
 `arcHoldsWhenOutnumbered`, mutation-proofed).
 2026-10-23 follow-up (no code): the chew-kites doctrine costs the arc
 arm one dragoon in the contact-start 3v5 (stand donated 0) - the bend
 aims at the squad, which in a surround is into the chewers. The arc
 deaths bound now allows +1 with the reason; contact-aware bending
 (bend away from contact, not just toward the squad) is open work for
 the #175 slice series.
 2026-10-22 forced-blend duel (1Drag, all 33 params pinned from a
 per-game env, `EscapeArcBlend=0.5 (ENV)` in the log): WIN in 22 s,
 1/0, 12 hits, 14 shots - inside the baseline band (10/10 wins,
 20-23 s, 6-16 hits), so no regression and no miracle either.
 Wiring proven end to end (33/33 values from env, zero random);
 the value decision stays with the tuner - one game is noise.
 2026-10-22 death-log dedup (owner): one issued order means one journal
 row - the `lastCommandIssuedNow` attempt row doubled every issue row.
 Pinned `DecisionJournalTest` 9/9.
- **#178** Zealot kiting (owner 2026-10-21/22): zealots never yield,
 so vs faster-attacking enemies (zerglings attack faster than
 zealots) they stand through the cooldown and bleed. Rule: after
 the zealot attacks with cooldown remaining, withdraw a step when
 (a) wounded, (b) locally outnumbered (more enemies than allies
 around), or (c) the foe attacks faster - skip entirely while
 ~unhurt (~10 damage is no wound). Mirror of the dragoon DuelKite
 footwork, zealot-flavored: the step must not break the line on
 open ground, only save HP that would otherwise be donated.
 Prove in sim first (zealot vs lings: HP saved per cooldown),
 then live on the ling maps (headless Wine, model-run).
 2026-10-22 sim slice: the withdraw intent never reached the ground -
 `ZealotWithdraw.should` gated into `shouldKite`, but the step math
 below is ranged-hold (desired minus dist <= 0 for melee-vs-melee),
 so every withdrawing zealot read stuck-no-step (sim: 130/130, zero
 moves) and stood through the cooldown anyway. The margin branch now
 takes a full footwork step for withdrawing zealots (cadence gate
 unchanged, direction still MoveAway; dragoon math bit-identical).
 Pinned `ZealotWithdrawSimTest` (wounded vs 2 lings: displaces, move
 steps, still trades; mutation-proofed). Live proof rides the
 headless Wine ling maps (model-run).
 2026-10-22 first model-run headless game (rule §14 allowance):
 ZealDrag_v_LingsUpg VICTORY 32/5 in 25 s, 56.2 SHOT/100f (win band),
 KITE 132 stepped-aside / 0 stuck-no-step (timeout games read
 122/173 stuck). Consistent with the executor, not an isolation of
 it - the sim pin stays the isolation proof; single games are noisy
 by the §14 discipline.
 2026-10-23 second model-run headless game, pre-fix jar (VICTORY 32/3
 in 33 s, 794f): both dragoon deaths kiting, not standing (#113 HOLD
 f0 -> OTHER f24 -> VETO f59, then KITE from f103 to death f144;
 #130 Unstick/MakeWay/Kite rows throughout; 0 FROZEN) - deaths in
 motion even before the chew-voids fix; the fix targets the stand,
 not the outcome.
 2026-10-23 model-run headless game on the current tree (VICTORY 32/7
 in 37 s, 893f): all 7 deaths ClusterKite, zero standing - zealots
 withdraw instead of donating contact. KITE 290 rows (272
 stepped-aside / 13 frozen-reset / 5 stuck-no-step): the sim-predicted
 pathology (130/130 stuck, zero moves) is gone live. Consistent with
 the executor; single-game noise per §14.
- **#179** ZealDrag_v_Zeal coordination + zealot hold-fire discipline
 (owner 2026-10-21/22): on the new map the bot first does not move
 at all, then loses on zealot/dragoon miscoordination - zealots
 break formation and attack first. Discipline: until a ranged
 weapon fires inside the cluster (assuming the cluster has one),
 zealots do not attack on their own; the moment a zealot is
 attacked, break formation and attack. Test vehicle is the map
 itself (ZealDrag_v_Zeal.scm): first prove the march (no
 standstill, #177), then the coordination (win, not just move).
 2026-10-22 formation hold (owner, Stardust doctrine): engaging no
 longer dissolves the line - with a melee-only enemy and nobody in
 contact (2.5 tiles, damage taken, own ranged able to fire), units
 march at formation slots instead of charging one by one. FLEE and
 ranged enemies keep the old behavior; duels fall through (slots
 need 3+ units). Pinned `FormationHoldTest` 5/5; `ArcRetreatSimTest`
 re-geometrized to an in-contact start (the hold owns the approach
 now). Live proof rides the next headless Wine game.
 2026-10-22 kite outranks holding, unconditionally (owner: ClusterHold
 dragoons stood while chewed): FormationHold breaks on any close
 enemy that can hit the unit - the earlier wound/cooldown gate
 stayed silent through shields (HP history misses them) and past
 contact range. The glued-trade branch is gone too: only an
 in-flight swing still trades. `ClusterHold` renamed to `FormHold`
 (order tag, not a manager - managers live in the Commander tree).
 Live sim tripwire `PhalanxVsPackSimTest` (2+2 vs 5 zealots, 600
 ticks, in-JVM): no zealot inside melee reach, no dragoon under
 150 hp, every dragoon issues attack orders. Null-position
 hardening along the way (roster outlives corpses in the sim):
 formation slots filter the dead, targeting skips addressless
 enemies. Suite green, both new pins mutation-proofed.
 2026-10-22 fogged snapshots, own copies only (owner: store our own
 APosition, never invisible coordinates; drop the address when its
 tile is seen empty): updateLastPosition skips invisible units, and
 foggedUnitNoLongerWhereItWasBefore clears the address instead of
 re-snapshotting from an invisible unit. The unit stays known, only
 the address goes - the ghost chain falls through to unexplored
 ground. Pinned FoggedPositionTest 2/2, both branches mutation-proofed.
 2026-10-22 kite outranks holding and trading (owner: ClusterHold /
 ClusterReengage dragoons stood while zealots chewed them): holding
 breaks when a close enemy can hit the unit and the weapon cools
 (cd >= 8) or it is wounded (< 80 hp + shields); the glued-trade
 branch is gone, only an in-flight swing still trades. A re-arm
 landing window was tried and withdrawn the same day: it starved
 sim DPS (no order-cancellation model there) with no live proof,
 so re-arm stays on the equality discipline plus the post-swing
 settle gate. Pinned (needsKite, glued-trade removal), suite green.
 2026-10-22 headless ZealDrag_v_Zeal (Defeat 6/9, 40 s): the ghost
 readout works end to end - none (f0-93, marching on the anchor)
 then contact f124 to the end, closing 161 -> 6. Approach marches
 from frame 0 (no standstill; scout release holds). 2 ClusterForm
 attack rows: slotted units do fight from the arc. All 9 deaths
 ClusterKite, zero ClusterHold - the hold never false-triggered.
 Zero FROZEN rows all game (deployment gate holds back the opening
 storm). Loss shape: brawl lost after contact, FLEE from f372 with
 a stationary center (69,34)->(67,34) while dying - retreat that
 goes nowhere (visible-pivot slots face the pursuer on FLEE,
 pre-existing, not this slice). Single-game variance stays high
 (12/5 win vs 4/9 and 6/9 losses vs 8/3 timeout on one build).
 2026-10-22 shoot-now targeting (owner: dragoons walked past
 in-range targets to chase a squadmate's kill 10 tiles out, taking
 5-6 hits): `AcquireTarget` returns the weakest valid in-range
 target first (focus preserved inside range, no walk-past), squad/
 shared/nearest stay as fallbacks; re-arm fires once per switch
 and waits while a step rides (landing window = latency + 1) so
 the chase no longer cancels the step - alternating spam verified
 fixed in-test (`move, attackUnit, move, attackUnit` without the
 gate). Sight snapshots into the ghost march even when the fight
 declines. Pinned `AcquireTargetTest` 4/4 + re-arm pin (both
 mutation-proofed); full suite green.
 2026-10-22 post-swing settle (owner hypothesis CONFIRMED in
 PurpleWave `AttackDelay.scala`: units stick when a move lands
 right after the attack animation - wait start + stopFrames + 2,
 +2 empirically eliminates sticking; second trigger: retargeting
 a chasing unit mid-swing). Our `holdSwing` covered only the
 animation itself; `attackSettleHold` owns the frames after it,
 calibrating per unit from observed swings (no hand stop frames).
 Pinned `AttackSettleTest` 2/2, mutation-proofed; sims unaffected
 (no attack frames there), live proof rides the next headless
 Wine game.
 2026-10-22 headless ZealDrag_v_Zeal: Defeat 10/9 in 88 s, 0
 ClusterHold rows - correct, both armies collided into instant
 contact (hold only owns the 2.5-9 tile approach window). FROZEN
 fired 63x incl. a frame-40 whole-army storm while deploying:
 freezing now requires banked deployment first.
 2026-10-22 frozen error row (owner): the second consecutive freeze
 files `minSec Dragoon#12 FROZEN <order> <ago>f ago, still <n>f` to
 the error log via `AConsole.errPrintln` - the block hunt starts
 from unit + last order + its age. Pinned (streak, error frame,
 reset), mutation-proofed.
 2026-10-22 frozen-unit detection (owner: a dragoon held a move
 mission standing still until Hold Position was pressed by hand):
 `FrozenWatch` notes per-frame displacement in the battle hook and
 the painter draws four unfilled purple rings (10/12/14/16) on units
 still 40+ frames with a move/attack order riding and no cooldown;
 trading blows and cooldowns are excluded. A throttled FROZEN twin
 row names the unit for headless logs. Pinned `FrozenWatchTest`
 5/5, mutation-proofed; the painter hook issues no orders.
 2026-10-22: VICTORY 12/5 in 43 s with hold+withdraw+long-vector
 +reinforce (first zealot death pushed to f243, dragoon 186 zero
 hits all game, no idle half; telemetry+pins hold).
 2026-10-22 headless ZealDrag_v_Zeal (Defeat 5/9 in 44 s, first game
 on the Behavior + commander-story commit): journal tags unchanged
 by the rename (all 9 death tooltips ClusterKite, zero FormHold -
 no stand-and-die holds; SHOT 385 + ATTACK 22, instant contact so
 the hold correctly never fired). March from frame 0 (cx 56->68,
 ghost none->contact, foe 161->10). FLEE from f372 displaces while
 dying (deaths drift (69,35)->(56,41), ~13 tiles - the old
 550-frame static center is gone; FLEE issues no arcs). Zero
 FROZEN rows; one KITE frozen-reset (kite preemption breaks a
 stuck unit). Sweep unexercised live (contact visible throughout)
 - stays sim-proven only. Single-game noise per §14; loss shape is
 a lost even brawl + unwinnable run, not a formation freeze.
 2026-10-22 headless ZealDrag_v_Zeal (Defeat 7/9 in 43 s, first game
 on the table-of-contents refactor): journal byte-identical by
 construction (same calls, same order, frozen tags) - all 9 deaths
 ClusterKite, zero FROZEN, SHOT 308 + ATTACK 18, KITE 88
 stepped-aside + 23 frozen-reset. CLUSTER twin flows through the
 new ClusterTwin.sample (34 rows, ghost none->contact). Result in
 the known variance band (wins 12/5 vs losses 4/9, 6/9, 5/9,
 10/9); what the game proves is no new pathology, not the score.
 2026-10-22 owner's ZealDrag_v_Zeal game (ended 0:24, truncated logs):
 Dragoon#165 died kiting while its reengage glide ordered into the
 unwalkable [43,53] every frame - move() refuses unwalkable ground
 and the wait below pinned the unit standing while Zealot#199 chewed
 it. Plus the standing-into-chews report: owner's hard rule is now
 code - melee within 2 tiles vetoes formation everywhere
 (`FormationHold.hasCloseMeleeEnemy`, raw distance, no plumbing;
 enforced in shouldHold/holdIfCalm AND the Regroup advance, which
 had no contact gate at all), and an unwalkable fireAt is nulled so
 the frame chases with an attack instead of waiting into the wall.
 Pinned (veto pair + Regroup advance + wall chase, all
 mutation-proofed). Honest gap: the user's game ended at 0:24 with
 no EVENTS, so the full standing shape stays underdetermined -
 live proof of both fixes rides the next Wine game.
 2026-10-22 headless ZealDrag_v_Zeal (Defeat 1/9 in 41 s, first game
 on veto + glide guard): zero unwalkable errors in WINE_DEBUG (the
 [43,53] shape did not recur), zero FROZEN, all 9 deaths
 ClusterKite - no FormHold deaths. The single FormHold order (f383,
 dragoon#96 attacking from the hold) transitioned to kite and died
 kiting at f622: the hold no longer pins anyone. Razor edge noted:
 telemetry sampled nearest foe at 1.8 tiles one second after the
 f383 hold - within a second's movement of the 2.0 veto line, so
 not a proven miss, but the boundary is live-fire thin. Loss
 driver this game is not standing: one enemy zealot (#195) took 8
 of 9 kills unfocused while the army kited (94 stepped-aside + 14
 frozen-reset, movement healthy).
 2026-10-22 headless ZealDrag_v_Zeal (Defeat 1/9 in 33 s): opening
 sweep again at f0 (Regions fix holding), contact f124, brawl
 lost by f562 (7 deaths around (65,40)), last dragoons run to
 (50,48) and die f766/782. Zero unwalkable, zero FROZEN, all 9
 deaths ClusterKite - the standing shapes stay fixed. Loss
 driver, same as the 1/9 before: dragoons die in the first
 contact minute (#162 f421 here, #186 f218 last time) and the
 zealots get mopped after - DPS lost early, not formation
 standing. Six-game Zeal band: one 12/5 win vs five losses;
 the win needs the early brawl, not the march.
 2026-10-22 two more games on the #182/#183 tree (Defeat 3/9, 662f +
 Defeat 3/9, 794f): coordination held first (~150f of FormHold attacks
 in the journals, zealots broke to the fight as designed), then both
 brawls lost the same way - dragoons die in the first contact minute
 and zealots get mopped after. Eight-game band: one 12/5 win vs seven
 losses; the freeze shapes are gone (zero FROZEN, all deaths mobile -
 see #182), what is open is winnability (early-brawl DPS), not
 coordination.
 2026-10-26 headless ZealDrag_v_Zeal on the current tree (Defeat 3/9 in
 37 s, 893f): ninth band game, same shape - coordination held first
 (HOLD 9x: FormHold attacks in the journals, zealots broke to the
 fight), veto broke the rest (VETO 30x). Zero FROZEN, zero standing
 deaths (all 9 ClusterKite, incl. 2x ClusterUnstick live). Latch
 fires in multi-foe too (released-finished 156 rows). KITE 218
 (179 stepped / 26 frozen-reset / 13 stuck). Brawl lost on DPS
 (3 kills), not on coordination or freezing.
- **#180** Per-second unit telemetry (owner 2026-10-21/22, extends
 #176): the death log gains meaning only with the fight around
 it. Every second log per unit: zealot-dragoon distances, HP,
 and enemy count within 2 tiles of the unit. Store compactly
 (throttled twin, one row per unit per second, not per frame);
 the dead-unit log then shows the class that decided (#176) plus
 the 5-second approach that killed it. Delete or thin when the
 coordination items close - diagnostics, not a permanent feed.

- **#181** Split-cluster reinforcement (owner 2026-10-22, HIGH priority):
 ZealDrag_v_Zeal live: the cluster splits in two - one group is being
 shredded by many zealots while the other half waits and does not
 fight. Unknown yet whether two clusters or one split cluster, but
 they start as one - detect the shape. Direction: (a) detect: per
 frame, if one part of the army takes damage while another part has
 no orders/no target for N seconds, that is the split - log it
 loudly (extends #180 telemetry); (b) if two clusters: merge toward
 the fighting one (the idle cluster's anchor becomes the fight);
 (c) if one cluster: forbid waiting - units without a target join
 the fight instead of holding formation slots. Suspects before
 measuring: engageable range caps (nearestEngageableEnemy 9),
 squad assignment splitting the army, anchor/formation fallback
 holding the far half. Prove on ZealDrag_v_Zeal: no idle half
 while the other half dies (per-part damage + orders in the log).
 2026-10-22 code LANDED (UMS-only reinforce to the freshest army
 contact, 240-frame freshness brake, pinned 5/5 incl. a negative
 melee control, mutation-proofed; verified from retained logs):
 ZealDrag_v_Zeal VICTORY 12/5 in 43 s, no idle half; LingsUpg is
 high-variance - 2 VICTORY (32/3, 32/4, both under 40 s) against 4
 wall-timeout Defeats (202 s cap, 16/1, 25/3, 29/4, 30/5) in which
 the army marches but never finishes. The timeout shape (5-9
 SHOT/100f vs 47-55 in wins, foe=-1 late) is the open end, not the
 reinforce.
 2026-10-22 the open end moved: with region centers repaired
 (degenerate-box fallback) the ghost chain reaches sweep, and
 ZealDrag_v_LingsUpg is a VICTORY 31/8 in 31 s - ghost=sweep from
 frame 0, no foe=-1 tail, no parking. The timeout shape was the
 dead Regions path, not the reinforce.
 2026-10-23 vanguard/ghost recall fixed (owner report: after killing
 half the enemy the army parks while the rest hides, one unit pings
 HOLD/UNSTICK on a ghost instead of hunting - game 200532, dragoon#214
 f1399-1956 HOLD/UNSTICK x5 on fogged contacts 8 tiles out, army in
 DEFEND): FormationHold declines ghost contacts unless the unit is
 really threatened (enemiesThatCanAttackMe(3.2) > 0, owner rule, raw
 distance like the veto) - new GHOST verdict, hunt owns a safe unit,
 the cluster owns a threatened one. Threatened ghost still falls
 back; visible far enemies still hold the arc (both pinned);
 threatened-by-melee still vetoes first. Pinned 16/16 FormationHold.
 Note: the looping unit was NOT the vanguard (lowest-id alive was
 #106) - the gate keys on ghost+unthreatened, no scout special-case.
 2026-10-23 nobody waits while the cluster fights (owner: ranged
 advance to range, melee join - holding slots during someone else's
 brawl is over): Regroup.march reinforces an ONGOING fight (new
 ArmyContact.isOngoingFight, 72f, not the 240f freshness) ahead of
 formation slots and the ghost creep, UMS+FIGHT only (melee keeps
 anchor discipline). Pinned: ongoing window 1f/99f, reinforce beats
 slots with 3 ours, idle army keeps slots without a fight; suite
 1293/1293. Live shape it targets (game 200532): half the army parks
 while the rest fights; the idle half held slots with no target
 instead of joining.
 2026-10-26 headless ZealDrag_v_Zeal (Defeat 3/9): no idle half - all
 9 units took 219-301 damage each, everybody engaged until death
 (per-part damage from the log, the item's own detector). Weak
 positive for the reinforce (one game, lost brawl); the 200532
 park-while-fighting shape did not recur.

Priority note (owner 2026-10-22): the three items below slot BELOW
the production ratchet (#45, #144-147 per-gate deletions) - small
sim-pinned slices, game-verified one at a time, never preempting
the deletion order. Combat work stays off the commanders
(CONVENTIONS §23): new logic lives in FormationHold / FightTarget /
AcquireTarget seams, BattleCommander only keeps delegating.

- **#182** FormHold stands into chews; frozen holders have no remedy (owner 2026-10-22, HIGH priority):
 ZealDrag_v_Zeal live, game 144553: Dragoon#162 holds formation slots f82-282 while 5-8 zealots stay within 2 tiles (telemetry n2), hp 260→~90, then kites f362+ with ZERO displacement until death (ux/uy frozen f112-412) - stood 300 frames, died standing. Zero FROZEN rows all game.
 Mechanism, verified in code (no guessing): (a) all proximity gates (veto 2.0, contact 2.5, needsKite) read `unit.enemiesNear()` (AliveEnemies, 5-frame cached selection) while shooting and telemetry read `Select.enemy().combatUnits()` - two membership sources; when they disagree the hold is blind and only HP-drop breaks it; (b) the damage gate is structurally dead for steady holders, see #183; (c) remedy gap: `unstickFrozenQueue` (FightTarget) only watches kite-steppers (`lastStep` fresh <48f) - a holder takes no kite steps, so it can never trip; FrozenWatch is passive by design (paints + logs) and DEPLOYED-gated (never flags units that banked <3 tiles of movement - #162 banked ~2).
 Direction (senior): commanders stay clean - remedy lives in FightTarget next to unstickFrozenQueue, detection stays in FrozenWatch/UnitTelemetry. (1) Owner doctrine, no exceptions: melee within 3 tiles breaks the hold - veto 2.0→3.0, contact stays 2.5 as the inner ring (widening alone fixes nothing while (a) stands, hence (2)-(4) first); (2) HOLD twin row (throttled): veto/contact/damage/nearest-listed inputs + verdict, so the next report is decidable from logs instead of forensics; (3) extend the unstick watch to holders (no kite step riding + no displacement + order age): STOP once + fresh order, same cadence discipline as the kite path, never every frame; (4) relax FrozenWatch DEPLOYED gate for already-decided units (a unit with a fresh combat order that never displaced is the stuck shape even at 0 banked tiles) - or document why not after measuring false positives on the opening march.
 Prove in sim first (holder with wedged queue gets STOP + displaces; no-op on healthy holders - mutation-proofed), then live on ZealDrag_v_Zeal: no 200f stands, FormHold deaths carry prior FROZEN/until rows or none at all. Non-goals: touching the kite/fight order flow; retreat logic untouched.
 2026-10-22 sim-first slice LANDED (no behavior change without the trip):
 veto 2.0→3.0, HoldVerdict + throttled HOLD twin (flip + 150f heartbeat,
 foe dist + foes-in-2), holder unstick (48f still + >1 tile from slot →
 STOP once + forgetMotion). Pins: FormationHold 11/11 (veto 2.9 breaks,
 3.5 holds; wound breaks an otherwise calm hold with unwounded-twin
 control), HoldTwin 3/3 (verdict rows, throttle ≤3 rows/200f, flips
 always row), HolderUnstickSim 1/1 (wedged STOPs ≥1, healthy marchers
 0 STOPs + displace, UNSTICK row in the log); suite 1279/1279, our
 files checkstyle-clean. Live proof pending: ZealDrag_v_Zeal (no 200f
 stands; FormHold deaths carry prior FROZEN/UNSTICK rows or none).
 2026-10-22 live proof, 2 ZealDrag_v_Zeal games on this tree (Defeat
 3/9 in 662f + Defeat 3/9 in 794f): the #182 freeze shape did NOT
 recur - zero FROZEN rows both games, every death tooltip ClusterKite
 (all 9+9 died moving), UNSTICK fired live (1 hold-unstick + 17 kite
 frozen-resets in game 1), the veto fires on the boundary live (HOLD
 foe 27-30 x10 = 2.7-3.0 tiles), throttle holds (6-10 HOLD rows/game).
 New shape found instead, twice, same signature (dragoon#188 g1:
 ~280f f161-438; dragoon#98 g2: ~160f f264-426): enemy inside 3 tiles,
 hp full, ZERO kite steps, same tile, hold correctly broken (VETO) -
 a fight-loop stall, not a hold freeze. Evidence: SHOT dist jumps
 34->19->30->20->6->13 at a standing unit (target switching, not one
 chewer), telemetry tgt flips 178->156->169->211 across the stall,
 unit.target() null in UNIT_TRACE throughout (engine holds no order
 while the funnel names targets), `shot-fired` rows on cooldown 26/16
 and 3 fires in 4 frames (order spam, not projectiles - the twin logs
 issuance). Both units reacted only to damage (first HIT f439 -> first
 KITE f482; f360 -> f426) and died displacing. Fight code untouched by
 the #182/#183 commits, so this is not their regression - it is the
 churn the #184 ladder is designed to bound (rule 0 + switch bound).
 No implementation here (#184 stays sequenced post-cut); the sim/repro
 isolation is the next measurement.
 2026-10-23 rearm side verified clean in the 1v1 latch batches (V0/V1/V2,
 11 games): shot-rows 16/16/22 per game with 3-in-4f bursts are re-arms
 after glides (null->foe transitions), not cancelled shots - all games
 VICTORY 1/0 with normal frame counts, DPS intact. Rearm's own
 discipline (re-issue only on target change + step-ride guards) holds;
 the #182 spam shape needs multi-foe target churn to manifest, which
 duels cannot produce - still #184's switch bound to own.
 2026-10-23 sim isolation LANDED (PocketVsOpenFieldKiteTest 2/2): same
 logic (BattleCommander.act), same threat, only geometry differs - open
 field kites (34 stepped-aside, 0 stuck, displaces 15.2 tiles), walled
 pocket stands and trades (157 stuck-no-step vs 23 validated steps under
 contact, displaces 1.0 tile, 11 shots fired, takes hits). Pocket-vs-logic
 decided for the trap shape: geometry alone explains the stand-and-trade,
 logic issues steps in both. Mechanism note: a THICK back wall reproduces
 the stuck signature (aims land inside the mass, spiral shift finds only
 the unit's own corridor cells, short-distance gate kills them); a thin
 wall instead validates aims beyond it (reachable-looking, sim-blocked
 around the corner - stepped-aside rows, zero net displacement). Row
 flavor depends on trap tightness, displacement separation (~15x) holds
 in both. Vehicle: arena-backed MapTiles.Source for the run, restored in
 finally (FakeMapTiles answers walkable-everywhere otherwise).
 2026-10-26 #182(4) decided: KEEP the DEPLOYED gate, no relaxation -
 documented why instead of measured-away. (a) The gate guards a measured
 live artifact, not a hypothesis: pre-gate, all 9 units flagged at f40
 while still deploying (game-start actuation lag under frameskip - orders
 ride, the engine has not moved anything yet). Any "fresh combat order"
 distinguisher re-admits exactly that flood, because deployment orders
 ARE combat orders; telling lag from wedged-from-start needs a game-clock
 hack with its own live proof. (b) The sim cannot clear the relaxation:
 sim movement actuates on frame 1, so a relaxed gate shows zero false
 positives in-sim while the live flood stands - sim evidence here is
 one-sided by construction. (c) The blind spot is diagnostic-only: the
 remedy side never needed banking - holder unstick trips on 48f still +
 >1 tile from slot (WEDGE_STILL_FRAMES/ARRIVED_TILES, no banked tiles),
 kite unstick on step-riding + 48f motionless, and both row it (HOLD
 UNSTICK verdict, KITE frozen-reset - fired live). A wedged-from-start
 unit gets STOP + fresh queue without ever rowing FROZEN. No code change.

- **#183** `isUnderAttack()` is dead for steady holders (owner 2026-10-22, HIGH priority):
 Pipeline order (FramePipeline.steps): UnitStateCommander (step 2) runs UnitStateManager for every unit BEFORE BattleCommander (step 6); the manager appends current `hp()` every frame it runs, and the order-latency gate skips it exactly when an order is in flight (`SleepUntilFrameGate.mustSleep`). A holder that stopped re-issuing (same slot) gets fresh history every frame, so `hp() < last` is false by construction - the damage gate in shouldHold, the #179 zealot hold-fire discipline ("break the moment attacked"), and the Terran/repair/interrupt consumers all read a signal that can only fire on gate-skipped frames. (Side correction for the record: the `needsKite` comment "HP history misses shield damage" is stale - `hp()` = hitPoints+shields and the history stores `hp()`; shields were never blind, freshness was.)
 Direction: damage detection must not depend on manager-updated history. Snapshot previous-frame hp in the battle hook (BattleCommander.observe, own map, cleared in clearForTests) and answer `tookDamageSinceLastBattleFrame` from it; rewire the hold + zealot gates to the battle-side signal; audit-then-migrate the Terran/repair/interrupt consumers one by one (no batch). Alternative considered and rejected: moving UnitStateManager after battle - pipeline-global, touches everything, same class of coupling that created this.
 Prove: unit tests (damage between observes flips the bit; identical hp does not) + sim (holder breaks on first damage frame) + live: hold breaks within ~5 frames of first damage on ZealDrag_v_Zeal. Non-goal: changing what counts as damage (shields already count).
 2026-10-22 LANDED: battle-hook hp snapshot (DuelMemory.noteBattleFrame
 in observe, latch tookDamageSinceLastBattleFrame, cleared in
 clearForTests) rewired into shouldHold; pinned by
 damageBreaksAnOtherwiseCalmHold (far enemy, one wound breaks, unwounded
 twin keeps holding) in FormationHold 11/11; suite 1279/1279. Live proof
 pending (hold breaks within ~5 frames of first damage on ZealDrag_v_Zeal).
 2026-10-22 live read, 2 ZealDrag_v_Zeal games: DAMAGE verdict 0 rows
 both games - expected, not a latch failure: VETO (melee inside 3) and
 the guns-in-range gate shadow it by design on every board the games
 produced (holds exist only at foe 8.7-9.0 while marching, then VETO or
 guns own the unit). The latch stays a backstop for boards the games
 never produced; the sim pin carries the mechanism proof.
 Correction 2026-10-22: the #179 zealot gate never read this signal
 (ZealotHoldFire.isWounded compares hp+shields against MAX, immune to the
 freshness bug) - no migration needed there. Real consumers of the
 history-backed flags are the manager's own lastUnderAttack/hitCount,
 read only on Terran lift/scout/repair, ChokeBlocker and Defend-interrupt
 tracks: audit-then-migrate those one by one, no batch.
 2026-10-23 audit outcome: PreventAttacksInMissionDefend (the one live
 Protoss consumer, defend-mission units are battle-observed) MIGRATED
 to DuelMemory.damagedLessThanAgo(45), pinned 2/2. The rest stay on
 history, each for a stated reason, no blind migration: Terran
 lift/scout/repair (7 sites) + ChokeBlocker (incl. the lone
 underAttackLessThanAgo selector caller) are Terran-only tracks on
 units the battle hook never observes - the latch would never fire
 for them, migration would silently neuter them; and D8 is
 Protoss-only, so all of it is dormant anyway.

- **#184** Targeting: ladder refactor of ATargeting (ordered rules, coordination dropped) (owner 2026-10-22):
 Complaint, mechanism verified (no guessing): dragoons keep a far target while a low-HP zealot chews them. Two compounding causes read off the call chain: (a) `focusInRangeTarget` stickiness (AcquireTarget.java:66-70) returns the current target without scanning when it is valid and in range - the chewer is never considered; (b) `ASquadTargeting.isValidTarget` bans `hp<=15`, ALL zerglings and ALL workers from EVER being targeted - an 8hp chewer is literally unselectable, so the kite/focus chain shoots past it. The user's one-shot rule is the exact inverse of the current ban.
 Agreed refactor shape (owner 2026-10-22): full audit first - ~1150 live lines across 13 files, measured file by file (the earlier "mostly commented out" estimate was wrong and is withdrawn - the ladder is real knowledge). Sequencing (owner): the ladder is NOT next - it lands only AFTER the production v1 cut (#45, #144-147); until then this item is analyzed-and-pinned, not implemented.
 Keep the ladder, review the coordination with care (owner: "do wyrzucenia" means think hard whether removal makes sense and where, not a deletion order): squad-last-target, leader-picked target and current-target stickiness are removal CANDIDATES, never pre-decided. Verified facts for that review: the old coordination code has zero callers (nothing in src/ references atlantis.combat.targeting.*) and does not ship (build packages atlantis//main//jfap//bweb//jbweb//jps, src-old compiles as a canary only) - but the CONCEPTS live on in current code (StickyTarget.currentTarget, FocusFire.sharedTarget, ASquadTargeting.useSquadTargetIfPossible, all called from AcquireTarget), so any removal touches live behavior and each rule goes only with a sim proof of no regression (if sim shows churn or focus loss, the rule stays bounded instead of deleted). Concretely, as an ordered RULE LIST in atlantis.battle.targeting (new TargetLadder, pure static functions, no new package): (0) wounded→closest when hurt (ShouldTargetClosestEnemy - the chewed-hits-back rule); (1) melee-proximity no-target gate (old line 13: melee within 2.5 → null, run instead - dovetails with #182: a unit that must not shoot must be owned by movement that frame); (2) one-shot killable in range, non-swarm (exact inverse of the hp<=15 ban; lings keep SpreadFire - pack focus on swarm wastes DPS, verified old wisdom, not a contradiction); (3) mostWoundedOrNearest in range (dragoon-priority vs Protoss per GoonGoonz; zealot radius escalation 1→2 per ImpZealot_A/B); (4) C5a chase (nearest within 9, attackable+2); (5) fallback nearest engageable (existing, untouched).
 Dropped for now, with reasons: the three coordination prefs above; Terran-specific (tanks, firebats, wraiths, vessels, SCV-repair); air incl. observers/carriers (no air on duel maps); transports; buildings/bases/creep/pylons (macro-map logic, not duels); disabled code (TargetingWhenDefendingAroundFocus returns false); Hydra-in-range special (covered by rule 3 unless sim says otherwise).
 Performance (owner ask): ladder short-circuits top-down (cheapest/highest-hit rules first: chewer check and no-target gate are O(nearby) before any radius scan); no per-frame garbage beyond the current chain (reuse Selection views, no intermediate lists per rule); per-rule debug tag into the journal (extends #176 attribution, so live logs show which rule fired). Readability (owner ask): one ordered list, each rule a named pure function with its sim pin; old code stays untouched in src-old as the archive (no file moves dragging dead branches and deleted-package imports).
 Audit first, in this order: why the `hp<=15`, `isZergling`, `isWorker` bans exist (overkill? morph states? engine quirks?) - each ban gets a named reason or gets deleted; same for `preventForZealots` interplay with #179. Tests: sim pins (8hp chewer gets focused; far-target stickiness breaks when chewed; no churn: switches bounded per frame); suite green; live proof on ZealDrag_v_Zeal + LingsUpg (kill attribution per target class already exists via #176).
 Non-goals: building targeting, air, transports, Terran-specific branches (D8 covers all non-Protoss-enemy scope; Zerg-relevant rules stay - LingsUpg is a proof vehicle). Coordination prefs are review candidates (see above), not pre-decided removals. Relation to #185: the ladder built here is the learner's future action space; hand-written gate conditions are what the learner will replace.
 2026-10-22 live observation for the audit (NOT implementation - the
 ladder stays sequenced post-cut): 2 ZealDrag_v_Zeal games show the
 churn the ladder must bound - attack orders re-issued onto switching
 targets (SHOT dist 34->19->30->20->6->13 at a standing unit,
 telemetry tgt flips, `shot-fired` on cooldown 26/16 and 3 fires in 4
 frames = order spam, not projectiles) while holdSwing/settle starve
 the kite branch for ~160-280f (zero KITE, full hp, VETO correctly
 broken). The "no churn: switches bounded per frame" test requirement
 above is exactly this shape; no new code until the cut lands.
 2026-10-23 fight-mirror gate LANDED (owner: FormHold never outranks
 kiting): decide() asks Kite.shouldKite with the fight's own threat
 (nearestEngageableEnemy inside the kite envelope) before the raw
 canAttackMe(3) backstop - same membership source, same predicate, no
 second opinion. Pinned 13/13 FormationHold (margin band declines at
 hp 48 / ling 3.12 while raw stays blind; full-hp control still holds).
 Live read on the user's Dragoon#165 (game 185100, f298 HOLD at foe 4.5
 then death f429): the f265/f298 holds were legitimate approach
 (fight agreed - its margin was satisfied too), the kill came under
 fight ownership (VETO since f305, meat-screen stand, no shots, kite
 only at f400) - not a hold-into-chews, so this gate does not rewrite
 that death; it closes the band class. Meat-screen stand itself is a
 separate owner call.
 2026-10-23 chewed voids every stand (owner order, absolute kite
 precedence): Kite.hasMeatScreen returns false while a melee enemy is
 in hitting range (new CHEWING_TILES 0.5 seam + isBeingChewed), and
 the same chew voids the surrounded stand and the low-hp imminent-shot
 hold. Repro StarEngine scenario (4 dragoons + 3 zealots vs 8 zealots,
 ~10 tiles): 348 chewed frames / 348 max-consecutive before, 196 / 23
 after, doctrine counter kiteFalseWhileChewed 333 -> 0 (pinned in-test).
 Cost, measured honestly: arc 3v5 contact-start donates 1 dragoon where
 the stand donated 0 (straight arm holds 0 while kiting too - the donor
 is the arc bend toward the squad, i.e. into the surround, not kiting
 itself). Arc deaths bound relaxed to +1 with the reason; the split
 bound (the test's main claim) untouched; contact-aware bend filed as
 #175 follow-up, no code.
 2026-10-23 live after-proof, ZealDrag_v_Zeal game 194747 on the
 post-fix jar (Defeat 8/5 in 202s, 8 kills vs 3/9 before): Dragoon#96
 HOLD f93 (foe 8.9) -> OTHER f134 -> VETO f140, never held again;
 first KITE f162 stepped-aside BEFORE first damage f168; 17 KITE rows
 with displacement throughout, death f315 with an all-ClusterKite/
 Unstick journal, zero FormHold. Worst chew 7 telemetry rows (~168f)
 is pursuit contact while kiting (equal-speed enemy starting on top),
 not a stand: pre-fix #165 showed 0 kite rows and 0 displacement over
 ~300f. Doctrine holds live.
 2026-10-23 rearm must not cancel a riding step (owner: kite too short,
 zealots re-attack, worst under 40 hp): a fresh attackUnit every frame
 retargets and each issue cancels the step - live Dragoon#96 showed 17
 stepped-aside rows with zero displacement and 20 hits in ~150f, while
 firing full DPS every ~34f. New guard in rearmAttack: no fresh attack
 order while a kite step rides (own cadence clock, not the glide
 clock). Calibrated, not guessed: unconditional guard wins the chew
 sim (196->14 frames) but breaks 4 DPS-race pins (duels won trading,
 trio wiping pressers, withdrawing wounded surviving); the line is
 drawn at 4+ melee in 2 tiles (SWAMPED_MELEE_COUNT: up to three the
 guns still win it per those pins, four and up only donate). Sim
 outcome: chewed 196->110, max-consecutive 23->20, min dragoon hp
 132->220, foes alive 4->5 (the DPS price, named). Pinned 3/3
 KiteStepRideSimTest (swamped holds fire, lone chewer gets shot,
 no-step re-arms).
 2026-10-23 live after-proof, game 210942 on the post-fix jar
 (Defeat 8/1 in 202s, 8 kills vs 3/9 before): worst dragoon chew 5
 telemetry rows (~120f, hp 132->69, survived), 37 KITE rows with
 displacement, death journal all-ClusterKite/Unstick with zero
 FormHold, GHOST verdict fired live on two dragoons. Only one zealot
 lost all game. Pre-fix #165 stood 300f with 0 kite rows to death;
 post-fix dragoons kite from before first damage. Doctrine holds live.
 Side observation, no code this turn: the ghost creep ordered 12
 moves into unwalkable tiles (one wall area, 4 distinct tiles, all
 refused safely by the move door with a full stack trace each in
 WINE_DEBUG). The orders never land, but the creep stalls there
 instead of sliding along the wall; candidate follow-up: walkability
 check before ordering in GhostAdvance.move, pending a repro pin.
 2026-10-23 meat-screen stand withdrawn (owner order, kiting regardless
 of health): screened dragoons kite by margin like everyone else -
 the screen doctrine made healthy dragoons skip kiting entirely until
 contact (live: chewed while full-hp, sim 333/333 on screen). Removed
 Kite.hasMeatScreen + its 3 uses (shouldKite, lowHp-yield, DuelKite
 yield); DoctrineTest pins renamed to the new discipline. Sim repro
 after: chewed 196 -> 74 frames, max-consecutive 23 -> 10 (< 1 s),
 min dragoon hp 132 -> 220, doctrine counter stays 0.
 2026-10-23 nearly-dead are finished first (owner live report, agreed
 #184 rule (2) taken early as a one-line bugfix, not the ladder): the
 old hp<=15 ban in isValidTarget made sub-15hp enemies unselectable,
 so dragoons shot the healthiest chewer and keepable dropped wounded
 targets mid-finish (churn). Ban deleted; dead units were and are
 filtered by isAlive in every caller. Pinned: nearlyDeadIsFinishedFirst
 + woundedTargetStaysKeepable. Ling/worker bans audited 2026-10-26
 (no code, ladder still sequenced post-cut): both gate only the focus
 paths (focusInRange/keepable/shared/squad via isValidTarget) - never
 nearest-engageable or the breach path (both skip validity, verified
 in code), so swarm is still engaged, just never focused. Reason:
 anti-overkill pack discipline (SpreadFire wisdom), not a targeting
 gap. Open sim question (not a deletion): whether keepable should
 finish sub-35hp swarm. preventForZealots (no focus on in-contact
 ranged) is complementary to #179 (which gates WHETHER zealots break),
 not contradictory; contact itself is owned by the #182 3-tile veto.
 Which fallback lands ling kills is untwinned - open measurement.

- **#185** Learned behavior gating over a faithful Java sim (owner 2026-10-22):
 Direction (owner): keep the behaviors, learn WHEN - the tuner tunes global scalars, the pain is state-conditional gates (2.5 tiles etc. hand-set, each new Behavior multiplies hand-gating complexity). A policy over the fixed Behavior set, from features (distance, hp, shields, cooldown, counts, terrain), decides the behavior; thresholds stop being hand-written. No live snapshots (owner decision: synthetic scripted maps instead - scenario control plus mandatory enemy-policy variety so gates cannot overfit one script; deterministic fitness is traded away knowingly). Sequencing: AFTER the production v1 cut (#45, #144-147); design now, implementation later, other-model implementable with sim-pinned slices + review.
 Stage A - fidelity loop (gates everything else): enumerate the sim-vs-engine gaps and close them in Java, gap by gap, each sim-pinned: order issue/cancel/replace latency + stickiness (the known DPS divergence), accel/turn/top-speed per type, weapon cooldown/damage/animation timing, collision/pushing, fog/visibility for targeting inputs. Validation protocol: twin-logged live trajectories (UNIT_TRACE ux,uy + cooldown + hits are already logged) vs sim replay of the same initial conditions; error metric per unit per frame (positional drift + outcome delta). Falsification trigger, written down in advance: if after closing the listed gaps the sim-vs-live outcome delta on frozen scenarios stays above bar (win-rate delta >20pp or mean positional drift >2 tiles at 600 ticks), THEN revisit C++ (Stardust shared-memory model) - until then Java stands (speed was never the measured bottleneck; fidelity is the work). Abandoning the project over speed alone is off the table without this measurement.
 Stage B - scripted minimap scenarios (64x64 presets: formation circle vs static enemy with aggro-6, pocket/wall variants, outnumbered variants) as sim fixtures with deterministic seeds; enemy POLICY variety mandatory (static, chase-nearest, focus-weakest, kite-back); winning shapes become fast-suite tripwires.
 Stage C - learner, interpretable first: contextual bandit / decision trees over the feature set → Behavior, deployed as readable rules (journal tags keep logging chosen behavior + top features, so post-hoc analysis survives and the owner's automation goal is met without a black box). Neural net only on plateau, behind the same seam (features in, Behavior out) - and never trained on a sim with known dynamics gaps (Stage A bar first). The hand ladder (#184 TargetLadder) becomes the learner's action space; hand-written gate conditions are what the learner replaces.
 Architecture shape (owner 2026-10-26, agreed after review with a second model): modular Behaviors, NOT nested ifs, NOT the old Manager framework. Each behavior is a fresh small class with applies() (gate) + act() (order) - no usedManager bookkeeping, no interrupt chains, no per-unit manager field; that baggage stays buried with the old tree. The order lives ONCE, as an explicit ordered list (precedent already in-house: Commander.subcommanders() documents "Order matters: it is the execution order") - visible, re-orderable, testable, which is exactly what nested ifs hide. The second model's objection (reordering managers broke everything) is answered by construction, not by file layout: disjoint-or-explicitly-prioritized guards, ONE order, and order-sensitivity pins in sim (swap two adjacent branches → the test names the intended change or fails). Ifs vs classes was never the real axis; implicit vs explicit order was.
 Gating seam per behavior (the NN plug point, mirrors ProductionV2Mode OFF/DRY_RUN/LIVE incl. typo-safe default OFF): each gate answers from the hand rule OR from the NN score behind a flag - per-behavior flags like V2's per-gate flips, so rollout is gradual, plus a global kill-switch. Fallback is mandatory, not optional: NN uncertain or output missing → hand rule decides, so the net is never a single point of failure in a live game. DRY_RUN mode is the trust builder: the net logs what it WOULD pick next to what deterministic picked, per frame - disagreement rows plus outcomes become the evidence base, no blindness, no faith required. Deterministic mode stays available permanently (owner requirement): same binary, flag-selected, doubles as the A/B baseline that proves the net (frozen sim scenarios, NN vs deterministic, efficacy bar before any live takeover).
 Migration is slices, never big-bang (production cut stays the priority): extract current branches (retreat, leave-breached, hold, kite/glide/re-arm pieces, regroup...) into Behavior classes one by one, order kept 1:1 at first (zero behavior change, suite green each slice), only then does the order become data. BattleCommander.act() iterates the chain - pure delegation, CONVENTIONS §23 intact. Guard design lives in `_AI/redesign/05_BEHAVIOR_GUARDS.md` (spec only until the production cut lands): sequential levels, disjoint guards, continuity by narrowing.
 Non-goals: C++ rewrite (revisit only on the falsification trigger); end-to-end pixel policies; tuner scalar loop untouched (it keeps tuning globals); #184 coordination review stays separate (action space, not gating).

- **#186** ProtossAttackHoldToShoot: real firing range vs nominal weapon range (owner 2026-10-26, from the owner's old code under the same name).
 The nominal dragoon range (4.0 tiles) is NOT the range at which a shot actually lands while both sides move. From the moment we start stopping (braking distance at current speed) through windup to the damage frame, a chasing enemy keeps closing - by the time the shot can fire it is drastically closer than nominal range suggests. This is why holding further out showed nothing (E-hold65: 17/14/15, back at baseline): the hold point was never the binding constraint, the stop-to-shoot window is.
 Mechanism: compute the stop point EARLY from our speed, the enemy's relative speed and angle, braking distance, windup-to-damage frames, and order latency. Then stand and wait: the instant the enemy enters true firing range, shoot, and resume kiting. Key informational advantage (owner): enemy intent is readable - if the enemy has us as its order target (checkable via orderTarget reads), is moving, and faces us, it IS coming, so waiting is correct; no guessing.
 Non-override rule (owner): no other behavior may cap the escape. In particular DuelKite must not pull a chased unit back into contact via its standoff; owner candidate: hold at range-0.15 instead of range-0.4, pending measurement (current 3.6 hard-coded in DuelKite.holdDistance; tuner knob KiteStandoffInsetTiles exists but is unwired - wiring it is part of this item, not a separate one).
 Assumptions to pin individually before use (each gets a sim pin + live proof, none hard-coded from memory): braking distance per type at full speed; windup frames to damage frame; live order latency (SleepUntilFrameGate.latencyFrames); enemy top speeds from the binding (measured 2026-10-26: Dragoon 5.0 vs Zealot 4.0 px/f - re-verify, never trust the remembered numbers); facing/commitment read reliability (isOtherFacingThisUnit 1.1 rad window vs noise); fog effect on orderTarget reads.
 Acceptance: sim-first (1v1 duel sim A/B: hold-early vs hold-at-nominal; must keep the win while cutting hits toward 0-2; mutation-proofed), then live Wine 1Drag proof (same bar). Non-goal: changing what counts as a hit (shields already count); C++ changes (OpenBW engine untouched).
 Relation: #184 (standoff distance is a targeting/kiting threshold candidate, not pre-decided); #185 (this is exactly one of the hand-written gates the learner will replace - features: our speed, relative speed, angle, cooldown, distance, orderTarget commitment).
 Slice 1 LANDED 2026-10-26 (knob wiring, sim-measured): `DuelKite.holdDistance` rides `KiteStandoffInsetTiles` (hard-coded 0.4 deleted); default stays 0.2 - the T-duel sim reads taken 40/40/40 across {0.4, 0.2, 0.15}, so the sim cannot discriminate (amplitude is not the binding constraint, per #152) and the 0.15 candidate rides the live Wine proof, tuner-accessible now. Pins: `holdDistanceRidesTheStandoffKnob` + 3-arm `standoffInsetVariantsAllWinDuel` (win preserved every arm). Suite 1337/1337. Remainder: stop-point computation (braking/windup/latency/speeds/facing/orderTarget pins each), then live 1Drag proof.
 Slice 2 LANDED 2026-10-26 (read-reliability pins): `FakeUnit` carries a settable `orderTarget` + override (production reads BWAPI `u.getOrderTarget()`, the stub always answered null - the commitment read was untestable); `ChaserCommitmentTest` 3/3 pins the #186 triple (order target + moving + facing inside 1.1 rad) and the fog-equivalent (unset reads as no commitment). Speeds/cooldowns were already pinned (`engineDataPins`); facing windows were already pinned (`facingHelperAgreesWithTheRawVector`). Suite 1340/1340. Remainder: the stop-point pure function (braking/windup as parameters, measured against SimWorld/engine), its manager wiring, then live 1Drag proof.
 Slice 3 LANDED 2026-10-26 (timing-model pins, `StopPointTimingPinsTest` 3/3): stub latency fallback is 3f (the duel gate's undocumented floor, now explicit); dragoon damage lands 6 ticks after attack start IN THIS SIM - measured, and it exposes a real finding: WINDUP keys ("Protoss_Dragoon") never match `type().name()` ("Dragoon", prefixes stripped), so every type falls through to default 6 and the map's 8/8/5 entries are dead (live evidence supports 8: damage ~cd 22 on a 30f cooldown). Fixing the lookup re-baselines every sim number - filed as its own slice, not smuggled here. Braking measured true: full-speed dragoon stops in 6.0px over 7 ticks (matches the 0.55-decay series, confirmed not fitted; published positions freeze earlier on pixel snap - pins read the precise accumulator via a test-only accessor). Suite 1343/1343.
 Slice 3b LANDED 2026-10-26 (WINDUP lookup fix + re-baselining): `WINDUP.getOrDefault` read `type().name()` ("Dragoon") against "Protoss_Dragoon" keys, so every type ran windup 6 and the map entries were dead - now reads `fullName()`. Fallout triaged: the windup pin now asserts the intended 8, and one scenario (`woundedDragoonsWithdrawAndSurvive`) needed recalibration to its documented intent (4 lings lethal under correct physics - dragoons shoot slower, lings chew faster; hp band untouched for pinch-line semantics, one presser fewer). No production code touched - sim harness only. Suite 1354/1354. The duel sims never noticed (taken bounds held throughout).
 Slice 4 LANDED 2026-10-26 (stop-point race, pure function + 6 pins, no wiring yet): `ProtossAttackHoldToShoot.shouldHoldEarly` answers the race given R* (nominal minus enemy cover during our windup/latency plus our braking glide, computed by a later slice): hold iff the weapon is ready on enemy arrival (`reachIn >= readyIn`); runners never hold; inside-R* holds only in the fire-now sliver. Key insight pinned: the caller invokes it under `distTo < hold`, so with R* below the hold the rule can actually fire (with R* at nominal it would be dead). Suite 1349/1349. Slice 5 LANDED 2026-10-26 (R* estimator + wiring + sim verdict): `effectiveRange` = foe edge-to-edge reach + chaser cover during windup - braking glide (new `bodyRadiusPx` engine seam; tile truncation rounded melee bodies to zero); `DuelKite.shouldYield` stands when the race favors waiting (2f hold margin = the imminent-shot gate's own slop). Sim verdict: T-duel taken unchanged 40/40/40, win preserved every arm - the sim cannot discriminate the wiring either; the owner's gate-edge pins (cd 6 yields, imminent 5 holds) all still pass. Suite 1354/1354. Remainder: live 1Drag proof (same bar: win + hits toward 0-2).
 Live proof LANDED 2026-10-26 (Wine 1Drag batch n=3, PARAM_TWEAKER=0, full stack: latch + window + hold-early wiring): 3/3 VICTORY, hits 13/8/8 (mean 9.7) vs V0 16/16/15 (15.7) and V2 9.6-10.3 - win kept, hits in the V2 band, no regression and no clear wiring-attributable gain at n=3 (same discrimination limit as the sim; a same-jar wiring-off A/B was not run). All hits while kiting except one boxed stand (pocket, not the race). The bar holds as win-preservation + V2-band hits; 0-2 remains open territory.


- **#187** Scout persists in the enemy main (owner report 2026-10-23, critical): a scout that finds the enemy main with no battle units yet leaves and wanders to far expos instead of watching. Required: stay in/around the enemy main gathering intel until battle units chase it out; then keep a circling mission nearby and retreat (natural, third base) only when hurt. No unprompted wander to unscouted expos while the main still has unseen area. Sim pin first (scout-vs-chaser: intel frames in main dominate the pre-contact window), live proof after on a game with a real enemy main (Wine with a real opponent; the UMS vehicle once #34 lands). Update 2026-10-26: clock-cutoff slice LANDED (sim-pinned): `ScoutNearEnemyBase` no longer declines past 340 s - the ancient gate evicted unchased scouts into far-expo wander; leaving is the chase machinery's call only (5/5 `ScoutCirclesEnemyMainTest`, suite 1335/1335). Live proof still pending.
- **#189** Log priority adoption (owner 2026-10-26, remainder after the
  central slice): every console line now carries `[LOW]/[MED]/[HIGH]/[CRIT]`
  (untagged call sites read `[MED]`), event rows always carry short `"lvl"`,
  `[TEMP]` marks removable lines, `query-events.py --group KEY` prints the
  `KEY:{frame:{params}}` shape (CONVENTIONS §27). Slice 1 LANDED: error
  channel HIGH by construction (`ErrorLog.print`, debug stays LOW),
  OpenBW FATAL is CRITICAL, init/map warnings HIGH, force-exit banner
  HIGH, all six cancel paths HIGH (matches the CONSTRUCTION_CANCEL=HIGH
  policy). Slice 2 LANDED: the other two force-exit banners + time-limit
  notices HIGH, build-order load failure + RACE MISMATCH block + race-compare
  failure HIGH (the handled UMS early-exception stays MEDIUM by design).
  Slice 3 LANDED: throttle keys are stable slugs, never the rendered
  message (live values silently disabled the once-per-minute throttle -
  QUEUE_BASE twin rows every refresh; ~25 call sites migrated, routine
  gas assignment rides LOW). Suite 1334/1334. Slice 4 verdict (measured,
  no code): console side is DONE - B75-B79 macros carry 11-15 MED lines
  per game, all one-shot startup/audit/exit, and UMS duels 0-9 (banners,
  hit totals); there is no remaining routine MED flow on console. The
  high-volume event-side MED (UNIT_TELEMETRY, ORDER_NOT_READY, CLUSTER,
  TECH/SINGULARITY_CHECK) is the diagnostic dataset, not flow -
  reclassifying it would fight #180 (thin/delete on owner call) and the
  #95 watch, so it stays MED by design. #189 console work closed;
  `[TEMP]` lines die per session.
- **#188** ClusterFlee overhaul live proof (owner report 2026-10-26: fleeing into death, fleeing with no visible enemy, scattered advance/retreat). Landed, sim-pinned: stragglers rally home on FLEE (never onto a forward-kiting leader); chased keeps ClusterFlee/RUN_ENEMY, unchased walks home as ClusterOrganizedRetreat/MOVE_FORMATION; blocking army between cluster and home forces FIGHT (Stardust rule, latch bypassed). Proven: new unit/chain tests green, full suite green, no-regression across clean Wine macros (homeward FLEEs, 0 scatter deaths). Pending: a real fight showing (a) organized tag in a death log, (b) a blocking-geometry fight-through, (c) cohesion through a lost fight. Update 2026-10-23: (a)+(c) partially closed by B58 - chased-FLEE + home-rally verified live (Zealot#145 died chased, both FLEE dests home; straggler-to-leader not reproduced); organized branch taken on every foe-unknown FLEE frame by construction, no death ever carried its tag; density rule deferred with measurement (max combat spread 2.7 tiles across 53 buckets, existing 2-tile leash + formation slots suffice unless scatter recurs). (b) still open live - no reconstructable blocker geometry in any twin (no enemy positions); sim proof done instead (BlockingForceSimTest: same wounded force both arms, blocked attacks through, lateral walks home untouched). Advance cohesion reviewed against Stardust (UnitCluster::move + moveAsBall boids + narrow-choke exclusions): our formation slots + 2-tile gather leash + GhostAdvance line-march cover the same intent, measured spreads stay ≤2.7, no change without scatter evidence.

## Current game state (updated 2026-10-21)

   Snapshot 2026-10-21 → _AI/WINE-MACRO-DATUMS.md §10 (frozen reference;
   newer macro evidence lives in the §1–§9 chains).
