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
   (cumulative: 262 rescue / 0 routine, our games only — foreign 091345's
   rescue row counts in re-sweep rows, never in tally; 296 retained logs
   (incl. foreign duel log 193800_1Drag_v_1Zeal + 2 Micro_2 + 10 1Drag duels + Wine duels 014201/020017, all 0 fed) / 246 rows (census frozen: `rm -rf out` 2026-10-26c wiped out/wine_runs/ (~301 per-run dirs incl. all B-chain evidence) - applied WO-13's procedure without listing out/ first; out/ held logs, not just build. Lost: per-run Wine logs (B-chain re-verification impossible from this box); intact: /sc-ai/REPLAYS (9 FFA), engine reps, src/, _AI/, scripts. Cited numbers stand as executed pre-deletion. Rule going forward: list out/ contents before any wipe; propose (owner call, not implemented): move per-run logs out of the build-output tree. Tally unaffected (per-game DATUMS rows); resweep restarts from new logs; cache-bust confirmed 2026-10-26e (identical command returned stale 296/246 post-wipe, fresh command 0/0 - vary the command when the result matters); FFA archive re-verified intact post-incident 2026-10-26d (9/9 EVENTS.jsonl readable, 24 BOARD_FED rows exact, 9/9 result.json present - surviving evidence is byte-consistent with citations),
   incl. foreign 141623 (concurrent unattributed run, 3 rescue rows,
   never in tally);
   tally corrected (B58 slip -3). Tally scope is the macro-vehicle chain (exact +2 per B-game while interleaved FFA games also fed) - FFA BOARD_FED rows are adjudicated in item prose, never in tally: 24 rows across 9 FFA games (9 zealot + 11 dragoon + 2 core + 1 gateway + 1 supply-pylon), 21 rescue (script-verified, pumps present) + 3 routine-SHAPED, all three explicitly non-candidates: 30B7DE5F core f9780 (bank 10/sup 10-24) + gateway f10200 (bank 10/sup 4-16) in a lost game (31 deaths, supply collapsing), 9415845A core f11670 (bank 37/sup 2-57, game over 234f later) - unconvertible banks, zero conversions after (no Gateway/CyberneticsC STARTED downstream in either game), single-game instances never replicated in a live/contested game; deletion gate still waits for routine coverage that actually converts. Sweep 2026-10-26b: resweep 296 unique / 246 rows, unchanged.
2. **Decision absorption:** board takes over decision sites one by one
   (7 files with the exact singular import, production holds ZERO -
   re-verified by grep 2026-10-22; all 7 survivors on excluded
   battle/micro/interrupt/terran-info tracks, dying with the
   manager-tree migration, never through the board); the class dies
   at zero, not before. Landed: worker Core-veto, enemy-race naming,
   Stargate branch absorptions, wall-in/cannon/Forge demand matrices
   (pins: `WorkerCoreVetoBoardTest`, `StargateEnemyRaceBoardTest`,
   `AbsorbedBranchesBoardTest`, `WallInGoalTest`, `CannonGoalTest`,
   `ForgeGoalTest`). REFUSED for now, recorded not dropped:
   robotics/observer demand and the gateway threshold thicket migrate
   only with a game proof each.
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

Unit 1432/1432 green (2026-10-26f clean `out/` rebuild: KITE refusal-state pins + cutoff escape-capability pin + constructing-elsewhere shield pin `6a71f18e0` + wall-funnel losing-side pin included; a stale `EscapeAngleProbeTest.class` from the long-deleted #173 throwaway probe executed once in a non-clean tree (1 fail by design) - `run-tests.sh` does `mkdir -p` without cleaning, so the WO-13 stale-class trap applies to the test runner too, not just architecture runs); architecture advisory
5/7 (same 2 pre-existing edges, empty store diff). History rots -
re-measure before quoting, never copy old counts here.

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
  reference, not a reader. No switch; the item still waits on E/H. Re-verified 2026-10-26: DoAvoidEnemies:27 still FoggedUnit-live (battle track), Jfap.isValidUnit:66 same predicate (line drift 72->66), BuilderAvoidEnemies fallback confirms out-of-scope.

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
  warps/min; V2 built no Core (hence no Dragoons), no Nexus, and two Forges - tech coverage is the gap, exactly per the ordering. Re-verify 2026-10-26: ProductionRacePolicy + 3 impls present (Protoss/Zerg/Terran), ProductionRacePolicyTest 5/5 green - seam stands, thin gap unchanged.

## Stage H — god-class split

- **#9** `A` is down to 43 public static methods (from 163). Left:
  resource/supply facades (~1400 call sites), clock arithmetic, race
  predicates (Stage G), wall-clock formatters. The facade destination
  (`AGame` vs `EconomyContext`) belongs with Stage G/I - do not start
  it here. (Wall-clock extraction tried and reverted 2026-10-09: trivial
  split, unreliable edit path, zero behaviour change.) Re-verified 2026-10-26: A.java 57 public-static methods (grep `public static .*(`) vs 43 on record - drift from new guarded readers (canCmd/canMove twins); destination still G/I, do not start.
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
  needing designed splits, not verbatim moves. Re-verified 2026-10-26: dispatcher lives in SelectionMembership (combatBuildingsAnti x3 + inShootRangeOf x2, Selection.java:767-772 delegates), slice files present (Radius/Membership/Sorting/Accessors/Views), SelectionViewsTest 8/8 green.
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
  in the seam with the reason recorded). Re-verified 2026-10-26: AUnitHealth/Shield/NearCounts seams present, AUnitHealthTest 4/4 green - recipe holds, rest is per-condition design.

## Stage I — physical migration

- **#12** Package move to bounded contexts per
  `DOCS/ARCHITECTURE-CONTEXT-MAP.md`, mechanical and separately
  revertable per context, only after F–H. Re-verified 2026-10-26: context map present (DOCS/ARCHITECTURE-CONTEXT-MAP.md), still gated on F-H, no movement.

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
  moving classes (`javac -d` leaves stale `.class` files behind). Caught live 2026-10-26c: the same trap fires in the test runner, and 2026-10-26d proved the worse half: the wipe took out/wine_runs with it (logs lived under the build tree). Never assume out/ holds only build outputs - `ls out/` first, always (`mkdir -p` without clean) - a deleted probe's `.class` executed and failed a full-suite run; clean rebuild is mandatory there too.
  Detail: `_AI/work-orders/WO-13-mechanical-archunit-sweep.md`. Re-verified 2026-10-26: ArchitectureBoundaryTest 5/7 (2 advisory failures = same pre-existing edges, build green) - no new violations, store diff empty. Authoritative gate scripts/run-architecture-tests.sh 2026-10-26b: same 5/7, store git-clean (only NEXT.md modified) - 9/11 in Test health was rot, corrected per in-file norm. Streak: git status every turn shows only NEXT.md - store never grew.

## Needs a game (quiet OpenBW run or flagged Wine run)

- **#150** Opening Gateway statuses lie (owner report 2026-10-17, Wine
  game 22:07, single base [112,10]): (a) Pylon PREPARING with no
  object shown - correct but invisible, painted orange since
  7c458cca, visual proof pending owner; (b) GW#2 At-10
  funding-blocked names itself live via `BUILD_REFUSED`
  (twin works on Wine); (c) GW#3 same-frame race vs warping GW#2 -
  pick-time guards see the warping unit only after it lists.
  Landed (pins: `OverlapsOurUnfinishedBuilding` spiral guard,
  adopt-time resite revalidation, adopt-exit twins,
  proximity-cover CANCEL twin, `ConstructionStatusAdvanceTest`,
  opening-status E2E gate, chain twins with cid/oid, log truncation
  per JVM, write-dir clearing pre-game, `WarpStartedTwinTest`,
  archive-aware `dropBuilderlessNotStarted`). Remainder, in order:
  owner visual proof of orange PREPARING (a); a live overlap game;
  the builder-dead warp twin gap (orphan-drop twin covers only
  not-started; progress-based demote is the WO-sized design, no
  twin until the HP-static signal exists - a blocked-demote twin
  without it would mislabel healthy builderless warps).
  Siteless-mint CANCELs read off the CANCEL reason (no REQUESTED
  row by construction - mint links before siting).
  WO-150b written (`_AI/work-orders/WO-150b-builder-dead-warp-twin.md`): twin + progress-signal contract for the builder-dead warp gap, slice 1 landed (trail + note + hpStatic pin-tested); live freeze proof still open. Distinct shape 2026-10-24 (B134, builder-ALIVE stall, not WO-150b): Pylon oid40/cid60 STARTED f7761 [15,48], IN_PROGRESS to game end (f28200, ~20k frames) with zero warp/cancel/demote rows and ~68 blocker-construction twin rows; builder #117 never died (no death row, still attached) so neither the builder-gone demote nor the FROZEN twin (dead-builder-gated by construction) could fire; no warping entity ever near the site (ledger clean, blocker-unit rows name only the healthy Pylon #150 at [16,46], completed f6600). Mechanism (code-read, `ConstructionStatusChanger.demoteStalledInProgress`): the stoodStill branch needs the builder still within 3.5 tiles doing nothing - a builder that walked away to a far job shields the entry forever, and no age cap covers IN_PROGRESS-without-entity for a live builder (the 90f cap lives only in the builder-gone branch). Fix direction LANDED 2026-10-24 (sim-pinned): age-cap IN_PROGRESS-without-building regardless of builder state (2400f > longest legal 1800f warp; Terran constructing stays shielded; young travellers unaffected - `aTravellingBuilderKeepsInProgress` green at 120f, 20k-frame entries re-arm with `stalledAge` twin); WO-150b stays parked (different shape - the HP signal is correctly silent here, nothing to sample). Stall-class audit 2026-10-26d (hunter: STARTED oid not in ledger, no CANCEL/DEMOTE, age>2500f): 4 hits - 182050 oid36 Pylon (2 rows, dead to maxframe), 021259 oid24 Gateway [18,47] f4406 dead 9500f (army routed around it), 082330 oid9 Cyber (DATUMS-filed silent stall), B134 oid40 (hunter-rediscovered blind, validates hunter + 'ledger clean'). oid36 is a CLASS (4+), not a singleton. Coverage: builder-gone branch cap 90f (STALLED_IN_PROGRESS_DEMOTE_FRAMES=90, ConstructionStatusChanger.java:307) + stalledAge 2400f (:382-389) would re-arm all three silent shapes (ages 3617/4338/~9500f, no warping entity, no recent attempts) via branch 4/5 by builder state; games predate the 2026-10-26 fix. Live proof rides any ancient-stall game. Correction 2026-10-26e (self): blockers are evidence, not decision inputs - the landed pins already covered live/dead-builder no-blocker variants, so 'no-blocker-shape pin open' was imprecise. Precise hole found by reading the branches: attached-builder-constructing-elsewhere is shielded (branch 5 returns false) - pinned as-shielded, zero behavior change (StalledInProgressDemoteTest 12/12, commit 6a71f18e0; oid36's builder #172 alive, 1 row total, never tracked again - fits this hole exactly). Stall-class audit 2026-10-26d (hunter: STARTED oid not in ledger, no CANCEL/DEMOTE, age>2500f, 153 ledger games): 4 hits - 182050 oid36 Pylon (2 rows, dead to maxframe), 021259 oid24 Gateway [18,47] f4406 dead 9500f (army routed around it), 082330 oid9 Cyber (DATUMS-filed silent stall), B134 oid40 (hunter-rediscovered blind, validates hunter + 'ledger clean'). oid36 is a CLASS (4+), not a singleton. Coverage: builder-gone branch cap 90f (STALLED_IN_PROGRESS_DEMOTE_FRAMES=90, ConstructionStatusChanger.java:307) + stalledAge 2400f (:382-389) would re-arm all three silent shapes (ages 3617/4338/~9500f, no warping entity, no recent attempts) via branch 4/5 by builder state; games predate the 2026-10-26 fix. Live proof rides any ancient-stall game; no-blocker-shape sim pin still open. Live proof rides the next ancient-stall game (B134 shape has not recurred since). 170306: FROZEN_WARP 0; DEMOTE 1 (Gateway oid25 [24,44] stoodStillFor=90f, recovered in place f4919 - builder present, not ancient, not WO-150b); live freeze proof still open, still no ancient-stall game Re-verify 2026-10-26 script: DEMOTE 0/2, FROZEN 0, CANCEL 9/0/63 (726D7D69/9415845A/C39F086C) - no 20k-frame entries, shape still absent. Re-verify 2026-10-26: StalledInProgressDemoteTest 11/11 green (2400f cap vs 1800f legal max, young travellers shielded) - pin holds, live proof still rides ancient-stall game. Sweep 2026-10-26b: FROZEN 0 in 9 FFA + wine_runs, DEMOTE chain 101 unique (102 raw) - unchanged. Code audit: cap FROZEN_WARP_OBSERVED_FRAMES=2400 (:323) vs 1800 Nexus (:320), Terran shield (:382), 3.5-tile branch (:395/:622), stalledAge twin (:387-388) - matches the file. Slice-1 pins 2026-10-26b: WarpHpSignalTest 5/5 + WarpHpTrailWiringTest 2/2 green - trail+note+hpStatic wired. Freeze watch: FROZEN_WARP 0 in every retained game (WO-150b negative sweep, 296 out/wine_runs logs + 4 FFA games incl. 726D7D69, 0 everywhere); per-game candidate verdicts ride the DATUMS §3 rows (latest B142: 0 demote-gone, 2 non-builder combat deaths, nothing qualifies); demote chain integrity unchanged (0 DEMOTE_STALLED in 726D7D69).

  → full chain: _AI/WINE-MACRO-DATUMS.md §3

- **#151** Dynamic demand re-requests a proximity-covered tile forever
  (Wine TauCross: oid=118 "At 32 Gateway*" 255 chain rows, ~20
  constructions on tile [105,110] cancelled same-frame against the
  standing Gateway grazing the footprint; second instance fully
  attributed: oid 72/78, 39/35 BUILD rows, fresh cid each time, both
  eventually warped elsewhere). Landed 2026-10-18: spiral checklist
  refuses standing-covered sites (stub-pinned 5/5); the twin
  (`prepare-site-overlap`) decides the next occurrence (silent unlinks
  vs flush-buffer loss indistinguishable otherwise). No merge of the
  two overlap checks (disjoint worlds by construction: live units vs
  reservations). Adopt side re-sites since the resite seam. Proof
  shape: a covered-tile game where the loop is gone. 2026-10-21 Wine macro IS the game, at unprecedented scale: 463 proximity-finish CANCELs against the STANDING Pylon cover. Mint path traced: the legacy ConstructionFinder chain, not the spiral - no local patch; the fix rides #145, this storm class is its acceptance test. Storm log rides DATUMS §3 (B134 x222, B135 x62, B136 x22, B137/B138 absent, B139 x582); cover rule verified code-read (`shouldProximityFinish`: standing same-type building within 2.5 tiles); proposal half unattributed (spiral inputs untwinned) - knife-edge cover config, not a stable attractor; the cutover still kills the class, but it can also die by itself. Re-verify 2026-10-24 (726D7D69): 8x same-tile [119,12] proximity-finish CANCELs + 2 STARTED at same tile - storm recurs via the finder path, spiral guard correctly silent (other funnel); acceptance-test status unchanged. Sweep 2026-10-26b: 726D7D69 prox 8x stable, other 8 FFA prox 0 - storm class static. Ledger audit 2026-10-26c: 182958 exact (45/45 [119,12] f8118-9320 single oid44, lone STARTED f8881 cid118 same tile cancelled same-frame, zero ledger warp) - never resited elsewhere, order dead at exit; 141134 biggest storm = 346 rows (1x f5510 oid11 + 345x f8775-f9119 1/frame oids 57/58), Cyber demote f4531 92f + cover STARTED f4321 as filed - dense window is 345, range reads f5510 + dense window. Ledger audit 2026-10-26d: all 45 same oid44; lone STARTED f8881 cid118 cancelled same-frame (cid118 rows: PREPARING-STCANCEL all f8875-8881); zero ledger [119,12] warp - order dead at exit; 141134 cover STARTED f4321 vs filed f4406 (+85 = ledger-lag consistent); 'B141134' = macro 141134, not B141 - cross-ref resolved. Ledger audit 2026-10-26d: all 45 same oid44; lone STARTED f8881 cid118 cancelled same-frame (cid118 rows: PREPARING-STCANCEL all f8875-8881); zero ledger [119,12] warp - order dead at exit; 141134 cover STARTED f4321 vs filed f4406 (+85 = ledger-lag consistent).

- **#152** Dragoon kiting (Wine 1Drag_v_1Zeal, owner 2026-10-19).
  Landed (pins in-test, all mutation-proofed): HIT/SHOT twins +
  per-cycle timeline + per-game digest; margin rule (targeted+facing);
  no-reissue + ride discipline; fromBest escape; glue-shoot;
  frozen-STOP reset; no-respam cadence; micro-step gain gate;
  Stardust desired-distance hold; SHOT/HIT evaluator fixed to the
  flushed file; TunerObjective/HallOfFame/Promotion/gradient/sim-batch
  + 32-param registry (`ParamTweakerFactory` by-value apply).
  Latency ladder (owner doctrine): V0 15.7 -> V1 finished-release 12.0
  -> V2 fire-frame window 9.6-10.3 over 9 duels; overnight n=6 at 9.2
  (combined n=15 in the V2 band, win kept every game).
  Step-amplitude grid answered: 2.0/2.5/3.0 no gradient (physics +
  pocket geometry bind, not amplitude). Open: 0-2 hits (via #186,
  not more open-duel grid); (a) wall-aware steps deferred
  (production first); (b) macro-game kite proof needs a Wine macro
  with real enemies; (c) tuner efficacy needs K-of-N replicates. Re-verify 2026-10-24: Wine TauCross KITE 0-156 (latest 156) but foe-attribution absent - macro proof still open (FFA 370 KITE with real foes is a different vehicle); wall-aware still deferred production-first.
  Twins landed alongside: DEMOTE_STALLED (cid/type/tile/reason),
  V2_GOAL_SOURCE (provider-goal authorship for WO-144). Duel 2026-10-24 (014201, default jar, full stack): VICTORY 0:20, 15 shot-fired / 11 HIT taken, KITE 34 stepped-aside + 2 duel-yield + 1 frozen-reset + 8 stuck-no-step, foe-visible CLUSTER throughout (fx/fy live), zero FLEE rows - win kept, technique looks like the V2 band, not a new regime.

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
  debugging can say why. 2026-10-24: two more named ends (Wine 1Drag duel GAME_END won:true + VICTORY banner; scbw Steamhammer game winner line) - silent shape absent again. Re-verify 2026-10-26: 6 newest FFA all named-or-crashed (4 winners + 2 crashed), 0 silent.
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
 2026-10-26 macro 091041 (367s VICTORY 1/0, 1 base): right shape, still far too short of the 700 s+ lottery. 170306: same bucket (VICTORY 2/0, 388s, 1 base, horizon-50 exit f9240 sup51, GAME_END won:true + VICTORY banner on a horizon exit = nominal victory) - horizon ends real-opponent 1-base games too, not just enemyless macros; lottery still needs horizon-90+cap+1-base B135-B142 lottery verdicts ride DATUMS §4 (1-base B135 still short; rest multi-base, horizon-90 exits; B142 multi-base too, cap never engaged). Lottery attempt #1 done (per-run horizon-90 + raised-cap env, verified live): multi-base shape, vehicle works, lottery not won.
  Vehicle finding 2026-10-24 (code-read, decisive): Wine macros cannot win this lottery as configured - `ForceExitAtSupply` (horizon 50 in ENV-WINE-FAST) ends every macro game at supply ~51 (340-502s observed), and nothing else ever ends them (foe unknown all game, 0/0 most games). The 944s game was OpenBW (their gates own the end). One long macro now running with a per-run env (horizon 90, shared ENV untouched) - first attempt at the shape, not at the verdict. - DONE 2026-10-24: B134 ran 1209s to exactly f28800 = 20:00 game time, which is the bot's own `inGameSecondsLimit` default, not a map cap (base intact, army wiped, DEFEAT on exit); multi-base in any case, so still not the lottery shape. A real attempt needs horizon 90 AND the ingame cap raised/disabled AND a 1-base game - still no 1-base 700s+ game anywhere (best remains the 944s OpenBW run). Lottery attempt #1 done 2026-10-24 (per-run horizon-90 + raised-cap env): supply-90 exit, multi-base - vehicle works, shape did not materialize; cap never engaged. Ledger audit 2026-10-26d: GAME_END supplyUsed=90 exact, Nexus#2 f8366 [105,38] exact. Ledger audit 2026-10-26c: GAME_END supplyUsed=90 exact, Nexus#2 f8366 [105,38] exact. Re-verify 2026-10-24: ORDER_SAVING 0 in 726D7D69/9415845A + latest B-games (14700/14550f, Nexus STARTED 1x each, multi-base) - 1-base 700s+ lottery continues. Re-verify 2026-10-26: nearest miss on record C2BF7CF7 (1 base, 0 Nexus, 16384f = 683s, short 17s of 700s; bank peak 782 > 550 floor, correctly silent) - lottery continues. Config audit 2026-10-26b: FORCE_END_AT_SUPPLY=50 + 750s wall (bwapi-data/AI/ENV-WINE-FAST:31/:18) - vehicle finding measured at config level. Code defaults 2026-10-26b: inGameSecondsLimit=60*20 (ForceExitLocallyAfterSomeTime.java:18, explains B134 exact f28800), ForceExitAtSupply off-when-absent (:20), Wine sets 50 - full chain config+code.
  → full chain: _AI/WINE-MACRO-DATUMS.md §4. Ledger audit 2026-10-26d: 004000 Nexus#2 f9390 [105,38] + Forge STARTED f9391/ledger f9594 (sighting-lag consistent) exact - 1 base + natural attempt, right shape too short as filed; Nexus completes ~f10764 (8964+1800 < maxframe), no CANCEL/DEMOTE after - 021259 accounting: 24 STARTED = 23 ledger + oid24 stuck, 'warping at exit' reads stale, 'zero-waste' needs the oid24 asterisk.. Ledger audit 2026-10-26d: 004000 Nexus#2 f9390 [105,38] + Forge STARTED f9391/ledger f9594 (sighting-lag consistent) exact - 1 base + natural attempt, right shape too short as filed.. Ledger audit 2026-10-26c: 004000 Nexus#2 f9390 [105,38] + Forge STARTED f9391/ledger f9594 exact (lag consistent with sighting model).

- **#81** Corner-stall Pylon search (top-right base [119,10]): needs the corner-stall game to decide doctrine vs leash-relax (owner call). Game happened (corner [119,10], 46 fails, Pylons still landed, supply unblocked): ranked refusals are physical 91k, PTY-modulo 33k, PTX-modulo 7k, boundaries ~274 - leash never bound (doctrine wins on evidence, 99.5% tile rules). Instruments landed (refusal-breakdown accessor + `PylonSearchBreakdownTest` + `StreetGridDoctrineTest`); total failure removed by a last-resort ladder (doctrine untouched, `PYLON_FIND_FAILED` only when all passes fail, pinned 2/2). `PylonNeedsRoomAround` in place (pinned 4/4): two no-harm proofs, zero misfires, zero firing shapes yet - the firing path still rests on the stub pin until a real corner shape appears. B134 (20-min, crowded base): 0 PYLON_FIND_FAILED - last-resort ladder never needed, doctrine untouched. Re-verify 2026-10-24: 0 PYLON_FIND_FAILED in 726D7D69/9415845A - ladder still never needed.

- **#84** Wine Pylon ordered but unbuilt (worker stall, placement refusal,
  null builder and cancels all ruled out from flag-off logs): needs a
  `BUILDER_LOG=1` Wine game (builder? site? no attempt?). Opened
  instances resolved as stall-with-recovery (builder moved and
  delivered), NOT new #84 stalls. Twin work landed: `WarpStartedTwinTest`
  2/2 (one STARTED row per construction - the cid=96 builder-path gap
  closed); siteless CANCELs carry cid+oid+reason (read the reason, no
  twin added per decision). Open: the STARTED-twin gap on the
  re-request door (cid=98/oid=67 "Null position" with zero REQUESTED
  rows); REVERSE-hunter audit 2026-10-26d (ledger warps with no STARTED row, 144 ledger games): twin present in every full log (0 zero-STARTED games) - the re-request-door gap is per-warp, not per-game. 5 mismatched pairs classified: 3x boot-only EVENTS (32 rows, maxframe 3) + full ledger = filed abnormal exits (suite-overlap corruption, process rule obeyed: pgrep clean every turn); 2x truncated tails with live log past EVENTS end (102716 ledger f4915 > EVENTS f4533; 104444 ledger f10697 + WINE_LOG live f10954 > EVENTS f10325) - 102716 filed-as-abnormal in chain note, 104444 UNFILED (new #96 instance; contributes 2 FED rows from truncated EVENTS, possible undercount in lost tail, tally unaffected - non-tally game, no DATUMS §1 entry). Live proof of the warp-observer naming rides the next
  legacy-minted warp game. Re-verify 2026-10-26 script (726D7D69): 14 STARTED, 12 warped + 2 in flight at wipe, 8 same-tile [119,12] proximity cancels + 1 under-attack cancel, 0 PLACEMENT_FAILED - pipeline healthy, zero breakdowns. Sweep 2026-10-26b FFA-wide: STARTED 7-17/game, CANCEL known storm volumes (63 C39F086C, 35 C2BF7CF7), FROZEN 0 - pipelines healthy. Pipelines healthy on all flagged games since. Ledger audit 2026-10-26e (170306, first post-wipe game): 12 STARTED = 12 ledger buildings, CANCEL 0, DEMOTE 1 (oid25, recovered in place same site), PREPARING twin absent for GW#2/#3 (instant starts continue; mechanism grounded 2026-10-26f: emitter is site-pick + builder-reserve, PrepareConstructionSite.java:100-111; absent = instant-adopt path skips prepare, warp-observe fills STARTED with the unit-center view instead - oid2/oid3/oid25 show center tiles, TL-agreeing rows come from adopt; Cyber oid9 scare resolved: PREPARING f3863 < STARTED f3908, lead 45f, order normal, no inversion - my earlier f3810 was the 726D7D69 tile leaking into analysis), PLACEMENT_FAILED 6x named Pylon "Can't physically build here" f9169-9230 (deletion gate still far); archive triple+ complete (EVENTS/BUILDINGS/WINE_LOG/_DEBUG/_DECISIONS/chaoslauncher/CVIS/rep) - pipeline healthy, file-outputs vehicle proven on a real post-wipe game (per-game STARTED/warped ledgers ride DATUMS §5; order-side attribution via STARTED twins. Ledger audit 2026-10-26c: 010853 exact (FED 0, STARTED 12, CANCEL 0, FLEE-CLUSTER 0, deaths 0, ledger 12 buildings = 12/12 warped); 005935 exact (FED 1x f7890, STARTED 12, deaths 0, 7 FLEE rows = one dest-closing episode f8246-8432, zero receding under center-dest reading); exact-PREPARING twin 0 rows in B142 (absence holds). Twin-key audit 2026-10-26e SUPERSEDED 2026-10-26f: the twin IS defined - CONSTRUCTION_PREPARING row for the same oid preceding BUILDING_STARTED (lead = frame gap; absent = instant start). Recovered from the ledger instruments (/tmp/opencode/append_b100.py S5: 'GW#2 PREPARING twin f1707 oid2, 4f before STARTED'). Verified live on FFA (new vehicle, twin never had it): BO gateways oid2/oid3 instant-start 6/6 across 3 FFA games; dynamic gateways show leads 68/1/51/25/37/1f (incl. three 1-frame leads - shorter than any B-chain record, different vehicle so no contradiction). Chain bookkeeping ('Nth', 'after 33rd') stays DATUMS-side; the twin itself is now checkable anywhere.; 182050 oid36 = exactly 2 rows (PREPARING f6792 + STARTED f6793 [112,4]), nothing to maxframe 10410, no CANCEL, no re-fire - started-but-never-warped stuck-order, first on record (distinct from WO-150b/B134 shapes). B142: 28 STARTED, 28 warped (zero-waste), twin absent 3rd after 33rd. Hunter audit 2026-10-26d: 021259 STARTED 24 vs ledger 23 - the unconverted row is oid24 Gateway (above); Nexus resited STARTED f8964 completes ~f10764, so 'warping at exit' reads stale and 'zero-waste' needs the oid24 asterisk. Hunter covers 153/306 logs (ledger games); duel/sim logs lack BUILDINGS.json - blind there. Ledger convention audit 2026-10-26c: 'STARTED f' = EVENTS order-side row; 'warped f' = BUILDINGS.json first-sighting (+95..+403f lag over matched warps); ledger tile = BWAPI top-left, EVENTS tile = adopt site or warp-observe center - cross-twin tile deltas need the path in mind (see #155). FFA side (726D7D69, no log flag so STUCK twins invisible by construction): 14 STARTED (9P/3GW/1C/1A), 12 warped + 2 in flight at wipe, 8 same-tile [119,12] proximity cancels (storm class recurs - fix rides #145, no local patch) + 1 under-attack cancel; pipeline healthy, zero breakdowns.
  -> full chain: _AI/WINE-MACRO-DATUMS.md section 5

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
  (would be blind). The dual-builder same-tile stall is the alternating-handoff shape to watch for - named with recovery proven live (DATUMS §3/§6: B136 dual stall, self-recovered, site warped); watch verdicts ride DATUMS §6 (B137-B141 breakdowns recovered except the geyser-blocked [96,36] loiterer, unrecovered at exit - NOT_STARTED-side sister of cid60; STUCK thin late spread; zero builder-dead shapes). B141/B142 (no log flag): zero STUCK/BUILDER rows by construction (twins gated on BuilderLog, code-verified) - builder variance invisible without the flag. G1 short flagged macro: clean. Re-verify 2026-10-26: twins BuilderLog-gated in code (BuilderManager.java:44), latest B-games flag-off (0 MOVE_BUILD/STUCK by construction) - variance invisible without the flag, no handoff evidence either way.
 2026-10-22 Wine TauCross macro (VICTORY 6:49, BUILDER_LOG=1): transient stalls only - 13 MOVE_BUILD (max 133f) + 4 BUILD, all self-recovered, 17/17 warped; no alternating-handoff shape.
 2026-10-26 macro 084359: same schema (0 MOVE_BUILD rows); 3 twin breakdowns (Gateway [14,51] builder-on-site f2040 - same frame as B49's, deterministic opening-tile signature; Pylon [24,44] x2 unexplored 2/4 attempt-phase; +7 untwinned transcript rows - twin fires per construction early only); 15 warps from 16 STARTED rows (attempt-only oid44 never STARTED; every started order warped incl. resites); zero alternating-handoff evidence either way.
  → full chain: _AI/WINE-MACRO-DATUMS.md §6

- **#85** Armor unchained from weapons-L3 to L1 (stub pin); flat rich
  gates (~2x above L2/L3 engine costs) read coherent (Forge -> weapons,
  rich checks, anti-spam, TooWeakToTech) - tuning, not defect, owner
  side of D5. Untouched. `TECH_CHECK` twin carries `refusedBy`
  (Weapons + Armor, pinned). Gates verified coherent live: research
  correctly silent without Forge at any bank, without 700-bank/3rd
  base, without 8 combat units; the post-Forge window EXISTS and
  completes through L2 (Singularity 7:30, Legs 13:37, L1 14:33, L2
  17:43 in one macro). Post-Forge window exists but thin; Singularity completed live repeatedly (ledger audit 2026-10-26d: C2BF7CF7 Singularity_Charge f9821 with no Forge yet - no-Forge Cyber->Singularity path live in FFA, extends live-fire beyond Wine macros) (ledger audit 2026-10-26d: C2BF7CF7 UPGRADE_COMPLETED Singularity_Charge f9821 with no Forge yet - no-Forge Cyber->Singularity path live in FFA, extends live-fire beyond Wine macros) (ledger audit 2026-10-26d: C2BF7CF7 UPGRADE_COMPLETED Singularity_Charge f9821 with no Forge yet - no-Forge Cyber->Singularity path live in FFA, extends live-fire beyond Wine macros) (B136/B138/B140-B142); L1/L2/legs never reached before the cap ends the game. Open: a post-Forge L3 window (richer still),
  and a longer window after research starts (one 743 s run STARTED
  at 11:16 but the wall clock ended the game first). 726D7D69: no Forge all game (TECH_CHECK refused no-forge x296, Singularity never researched) - correctly silent, nothing new. 170306: Forge correctly silent all game (TECH_CHECK refused no-forge x188, weapons+armor); Singularity research STARTED live ~f6893 (beingRes, bank spent 177/162->51/28), in flight at horizon exit - completion unwitnessed (no UPGRADE_COMPLETED twin rows exist anywhere); no-Forge Cyber->Singularity path live Re-verify script: TECH_CHECK 296 rows, Singularity STARTED 0 (9415845A: 270/0) - same silence. Re-verify 2026-10-26: only Forge in 9 FFA is C2BF7CF7 f12851 (3533f window to f16384, 1 UPGRADE_COMPLETED) - post-Forge L3 + long-window still open.

- **#82** Terran off-site completion may leak (unobserved, both Terran
  runs built on-site). 2026-10-18 datum (`143800`, Terran vs Zerg, 212
  game-s): the bot plays Terran with the Protoss build order (`Zealot
  into Goon`) and builds nothing at all (CommandC=1 SCV=4, idle) - D8
  Protoss-only, so the leak is unobservable until Terran play exists.
  The adhoc tier also has no Terran expectations (Protoss gates fail
  it trivially). Needs a game showing it. No blind fix. Re-verify 2026-10-24: D8 Protoss-only still in code (NeedDynamicSupply.java:22, SiteScope.java:10), no Terran FFA replays - leak still unobservable.
- **#92** Second Gateway ~3 min late on Wine (BO `At 10` #3 -> re-queued
  4:39 sup35, accepted 4:40+ six times same tile; first Gateway accepted
  1:09). OpenBW control warps both on time - OpenBW side clean, Wine
  proof still owner-side. Instrumented, not patched: `BUILDINGS.json`
  ledger (warp time + supply vs order expectation); the late/missing
  Gateway row names the layer when it recurs. Verdict across 8+ clean
  runs vs 3 late runs in the archive: intermittent, still no blind
  patch. Standing shape is systematic +1 supply lag on #2/#3 (not
  lateness - warps land on time, audit tolerance confirmed; first fully on-plan openings B140-B142 (ledger audit 2026-10-26d: 101217 GW sup 10/10 + 12/12, Cyber 21/22, Assimilator 23/23 - all four pairs exact incl. the Cyber off-by-one) (ledger audit 2026-10-26d: 101217 GW sup 10/10 + 12/12, Cyber 21/22, Assimilator 23/23 - all four pairs exact incl. the Cyber off-by-one) (DATUMS §7). Late shape intermittent, still no blind patch. 726D7D69: GW#2 warps sup 9 (At 9), GW#3 warps sup 11 (At 10, +1 systematic) - no lateness. 170306: GW#2 At10->sup11, GW#3 At12->sup13 (same +1 systematic, no 3-min lateness); Pylon#5 At15->sup16, #8 At21->sup23, #11 At27->sup29 (audit flags, systematic lag shape); BO audit names 5 mismatches, all lag not lateness. Re-verify script: all 3 GW STARTED+warped (f1770/f2414/f5350), 9415845A 5 GW STARTED - no missing/late shape. Re-verify 2026-10-26 FFA-wide: GW#1 f1711-2084, GW#2 f2414-4379, GW#3 f5148-5580, all warped - systematic +1 lag only, intermittent 3-min late absent.
  -> full chain: _AI/WINE-MACRO-DATUMS.md section 7

- **#94** Enemy never spawns on OpenBW (host auto-menu path): 6+
  enemyless games across TauCross/Python, Zerg/Terran - plumbing
  verified correct, slot never reaches occupied/computer_game.
  Blocks all fighting proof (cluster FIGHT/FLEE, kiting, rush twins);
  the Wine vehicle stands for fighting proof (sim zombie
  `tests.e2e.ZombieAttacksNearestUnit` is not engine proof).
  Settled 2026-10-20/21 (full archaeology in git log +
  `docs/openbw-run-regression.md`): handshake pump landed, MatchStart
  arrives, `ForceExitWhenInvalidState` own-bug fixed
  (`ForceExitNeedsPublishedSupplyTest` 2/2), hosting passes
  ENEMY_COUNT=7, exit path halt()s, WIN/LOSS/DRAW verdicts pinned.
  Open remainder, engine-side (other model's territory, not fixed
  blind): on 1Drag the enemy zealot deals damage at 0.5 tiles (HIT
  rows) while `enemy: ` stays empty - spawned and hostile, never
  visible to the client. Re-verify 2026-10-24: ENEMY_COUNT default 1 (run-openbw-e2e.sh:191), ForceExitNeedsPublishedSupplyTest 2/2 green - plumbing holds, remainder still engine-side. Audit 2026-10-26b: MatchStart handling (Atlantis.java:84/:181, UMS never sends it), exit halt()s (OnGameEnd.java:137, Atlantis.java:324). Host-smoke pump landed (test-openbw-host.sh: MatchStart published :119, PASS :130).

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
  wall (or a sim siege), not just another FE macro.) Sim-siege leg LANDED 2026-10-24 (`wallFunnelHoldsRaidWithoutHelp` + capacity pin `wallHoldsEvenHeavyRaidUntouched`: 8-raid AND 32-raid both die in the funnel, guns at FULL hp - tick 969 for 32; no ling-break exists for this funnel, the pack serializes and the cannons out-trade every wave. A losing fixture needs a different attacker shape (ranged pressers outranging the guns), noted for Stage B, not built here.) BUILT 2026-10-26f: `wallFallsToRangedPressers` (4 Guardians hold-at-max-range, guns bleed while all 4 pressers stay untouched - DragoonsVsZerglingsTest 24/24 with the strengthened no-return-fire assertion); losing side of the sim-siege leg now pinned, funnel holds raids but not pressers. First Stage B discrimination scenario for the hold-early wiring (live 1Drag proof still needs the duel). [ ] M5 module extraction (assessed
  2026-10-17: 7 files, engine-import-free except own map types +
  `AUnitType`; single engine-coupled adapter (`EngineGroundLink`) already
  isolated behind `GroundLink` - physical extraction is build/repo
  mechanics gated on owner timing). JBWEB fork
  (separate repo, walls/JPS only) plugs in at M4 if own anchors prove
  weak. Unblocks nothing above; nothing
  above blocks M0. Re-verified 2026-10-26: BaseRoles + EngineGroundLink present (adapter isolated, imports only APosition/ReachableTiles/AUnitType), WallInGoalTest 6/6 + DragoonsVsZerglingsTest 23/23 green - siting + sim-siege hold, HOLD-vs-real-raid still needs attack onto wall. Import audit 2026-10-26b: 7 files, zero non-java non-atlantis imports - extraction-ready, mechanics still owner-timed.


- **#95** Reserved-250 ghost, unresolved watch (2026-10-17): a deadlocked
  game showed global reserved=250 at supply 4 with bank 74 and nothing
  READY - no author found (READY/IN_PROGRESS paths audited, equality is
  value-based, feeder silent). Healthy games show a clean trajectory
  (0 early, single-row earmarks late). If it reappears, the `reserved`
  column plus DECISIONS.log name it; until then no blind fix. Standing
  caveat: the ORDER_NOT_READY twin only fires above price-32 bank, so a
  sub-floor deadlock would leave no twin rows - the sweep instruments
  the visible band only. Re-sweeps (12 -> 266 logs): per-row earmarks
  >= 250 always at supply 13+ in flowing games, except frame-3
  BO-load transients at sup 10 (test-boot family, four artifact
  instances total) - the supply-4/bank-74 deadlock shape absent in
  every sweep, latest 2026-10-24 across 296 unique logs (4 frame-3 boot transients, no deadlock shape). Re-verify 2026-10-24 script: 0 rows reserved>=250 at supply<=12 frame>100 - ghost still absent. Sweep 2026-10-26b: 0 rows again; ORDER_NOT_READY twin band (price-32 floor) unchanged.
  -> full chain: _AI/WINE-MACRO-DATUMS.md section 8

- **#96** Wine verification batch (headless, model or owner): painter warping labels
  (owner-visual, eyes only), file-outputs triple (WINE_LOG/_DEBUG/_DECISIONS + BUILDINGS.json, re-verified across macros incl. triple + CVIS artifacts; UMS duels collect EVENTS.jsonl + WINE_LOG only - by design, not a gap), Missions unenaction sanity (MISSION twin, change-only, pinned `MissionTwinTest`; painter labels + a missions game stay owner-side), replay naming (engine template stays proven %MAP% + strftime; script bakes MAP_PvsZ_wine_DATE at archive - replay *saving* itself stays flaky on the macro path, engine-side, teardown grace guard kept as cheap insurance. Log-path scoping 2026-10-26d: per-run logs live at LOG_DIR/WINE_RUNS_DIR/RUN_DIR (run-wine-full.sh:81/:86/:688, latest re-pointed :711) - structure self-heals on the next game run (mkdir -p + ln -sfn), only historical data is lost; the move-out-of-build-tree stays an owner call, not implemented). Re-verify 2026-10-24: latest triple complete (BUILDINGS.json + WINE_DEBUG + WINE_DECISIONS + WINE_LOG + EVENTS.jsonl + CVIS + rep), 1Drag UMS EVENTS+WINE_LOG only confirmed - triple holds. Guard audit 2026-10-26b: teardown-race guard at run-wine-full.sh:622 (replay wait, measured B64-B71). Naming chain: REPLAY_TEMPLATE=%MAP%_strftime (:537), engine strftime (:665/:894), script bakes MAP_PvsZ_wine_DATE (:527-532). Attribution rule (learned the hard way): a live console belongs to an archived game only if Pylon#1 tile + maxframe + STARTED rows match - never quote console details into a datum without the cross-check. Open tuning question: Pylon prepared at 15 warps ~2:24, past the 17/17 block (adoption+travel+warp ~25 s outruns the 15->17 window) - lever if still late is `IsReadyToProduceOrder` pull-forward amounts.

- **#34** Stub twins hold (4pool/9pool baselines pinned); a real
  opponent needs a scripted UMS map (one client per OpenBW host) -
  owner-supplied, tier stays owner-run. Sim zombie covers reproducible
  defense in-process only, never engine proof. Probed 2026-10-19 with
  `USE_MAP_SETTINGS` in the runner: `ZealDrag_v_Lings` runs the full
  1209 s but hands us a melee start (Nexus+probes, no army) and spawns
  no enemy (0/0) - no fight to assert; `8Drag_v_8Drag` ends at frame 0
  (human slots) with the exit-path backstop firing. Neither map yields
  a battle; the tier still waits on an owner-authored computer-slot map. Re-verified 2026-10-26: USE_MAP_SETTINGS plumbing present (run-openbw-e2e.sh:694), ZombieAttacksNearestUnit exists test-side (sim, never engine proof) - still owner-blocked. Audit 2026-10-26b: zero prod refs to it (grep) - sim-only confirmed.
- **#65 / #68** CherryVis code fixes are in (types, legend, seed,
  writer path, `--cvis` plumbing); what remains is the owner opening a
  real trace in the viewer (plus the `[BW<->CP Matching]` console line
  if `???` persists). Re-verify 2026-10-26: CherryVis fixes in code (UnitFirstSeen seed + TraceJson legend) - remainder still owner-visual.
- **#78 / #79** Replay desync is verified upstream (OpenBW #27, 2019,
  second Linux confirmation 2021, open/unassigned). Live log is truth,
  replays are position-viewing only. Engine surgery out of scope;
  owner runs a separate model on fully-working OpenBW. Re-verify 2026-10-26: regression doc present (docs/openbw-run-regression.md), Atlantis C++ clean (no recent .cpp touches) - doctrine holds, still upstream-blocked.

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
  `DynamicGoals.addArmyGoal` + tests, live proof `203655` (8 Dragoons, first 5:25, was 0 pre-rule). Re-verify 2026-10-26: addArmyGoal + Dragoons-ASAP rule present (DynamicGoals.java:307-313), QueueLiveGateTest 1/1 green - ordering stands, LIVE default still gated on parity. Env sweep 2026-10-26b: no static ENV file sets PRODUCTION_V2 (bwapi-data/AI/ENV*), OFF default (Env.java:362) holds everywhere - DRY_RUN only per-run by design.
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
    to the scheduler, not on a schedule. Re-verify 2026-10-24: V2_GOAL_SOURCE 0 rows in 726D7D69/9415845A (V2-off silent by construction, collectGoals never runs) - authorship evidence still rides DRY_RUN only.
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
    2026-10-24 live Gateway storm (user report, real game: 4 dynamic
    Gateway "Null position" cancels in ~1 min, all "Can't find place
    (reason: OK)", maxDistance 36, near null): prime suspect was the
    literal-30 site guard shipped 2026-10-23 - a hard 30 fights the
    spiral's own radius-36 contract, and in a crowded base the only
    free tiles sit at ring edge 30-36 ("OK" = a tile passed, then the
    guard nulled it - the exact signature). Corrected the same day:
    cap is max(30, order radius) (absurd-only, never ring-edge) and
    the guard names itself (`SITE_TOO_FAR` HIGH row with site/dist/cap)
    so the next "reason: OK" is attributable in console alone.
    Pins: `BuildSiteDistanceTest` cap + ring-edge survival (5/5). B134 measured negative with the guard live (DATUMS §3) - pure finder failure at maxDistance 36, outside the guard's ring-edge scope; the catalogue cutover remains the fix. Re-verify 2026-10-24: guard present (`FindPosition.java:137`, `capFor`/`isTooFar`), SITE_TOO_FAR 0 rows in 726D7D69/9415845A, BuildSiteDistanceTest 5/5 green. Code audit: capFor = max(MAX_SITE_GROUND_TILES, maxDistance) (:32-36), isTooFar null-safe (:43+) - absurd-only, never ring-edge, as claimed. Funnel audit 2026-10-26b: standing-cover refusal lives ONLY in RequestBuildingNear (tooManyNear :72 + isSiteBlockedByKnownBuilding seam :181+); sited door (DefineExactPositionForNewConstruction, 108 lines) has zero overlap check - separation as filed (an initial grep misattribution was caught by re-read before claiming).
  - **#146** `APositionFinder` legacy path → deleted after the catalogue
    default + parity (D1 pointer; ~25 files still route through it).
    Exit: zero `Can't find place` across retained runs, legacy path gone.
    2026-10-20: 33 PLACEMENT_FAILED in one Wine macro game - nowhere
    near zero, no deletion. Failure surfaces as CANCEL, not PLACEMENT_FAILED (B134: 0 rows vs x223 siteless); the deletion gate needs both counters, per-game breakdowns ride DATUMS §3 (variants: burst / cap-cut / scatter / cover-attributed; latest B140/B141 zero CANCELs; B142 breaks the streak: 6 CANCELs, first named PLACEMENT_FAILED volume (At 41 Pylon "Can't physically build here" x1475) - deletion gate needs named failures). Re-verify 2026-10-24: C39F086C 63 CANCEL + 59 PLACEMENT_FAILED Gateway "Can't physically build here x34 (of 34)", C2BF7CF7 35 + 42 - still far from zero, no deletion.
  - **#147** `Queue/**` + order types → stateless demand/schedule
    (01_PRODUCTION.md scheduler + dispatcher; single policy already
    gated). LAST: a bot that can't queue can't play. Exit: V2 LIVE by
    default with parity numbers, queue in src-old/. B136: legacy queue still the only queue (QUEUE_BASE 75x, duplicate-refusals 13x, V2 off) - no movement. Re-verify 2026-10-24: Queue/** still present, QueueLiveGateTest 1/1 green, V2 off in routine macros - still the only queue, no movement.
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
 2026-10-26 macro 091041 (13 warps): re-fire only - min-supply 4x (Gateway x2/Pylon x2), funding 2x (Gateway/CyberneticsC singles), earlier-reserve 1x (Pylon), core-priority 1x (Pylon), pylon-cap 1x (Pylon), unstarted-pylon 1x (Pylon); zero live fires of the stub guards (same four stub-pinned); zero core-single-instance. B135-B142 re-fire identically, per-game counts ride DATUMS §9 (no new gate; Stargate resite proofs stand, warping-link sighted 4x; prepare-site-overlap 7x Gateway (5th series, 1st Gateway instance - proof CLOSED from the B142 chain: 7 correct refusals oid25 at covered [16,44] (CyberneticsC warping there f4690) -> REQUESTED [17,41] f4821 -> 100th demote recovery f5133 -> STARTED [19,42] f5296 -> warped ledger [17,41]; refusal + DEMOTE rows now name oid. 170306 repeats the chain fully oid-linked (2x prepare-site-overlap refusals [17,47] f4351/4366 oid25 -> REQUESTED [24,44] f4367 -> DEMOTE stoodStillFor=90f f4730 cid54 -> STARTED [26,45]=center view f4919 -> ledger [24,44] warp; frame-shift (+2,+1) confirmed on fresh live data (BWAPI-TL ledger vs center-view STARTED); instant starts (GW#2/#3, no PREPARING rows) show the same center view, so the trigger is adopt-path-missed not re-request-only dedicately, series join mechanically). Ledger audit 2026-10-26d: 7x prepare-site-overlap exact, REQUESTED [17,41] f4821 + STARTED [19,42] f5296 + 100th demote f5133 stoodStillFor=90f + Cyber STARTED [16,44] f4690 all exact; same-event tiles differ (+2,+1) = half of a 4-wide footprint in 3 cases (oid25 GW, Nexus x2 games) - adopt path rows bot-TL (ProduceBuilding.java:402), warp-observe path rows unit-center (ProtossWarping.java:91), startedCids dedup picks the winner; attribution joins safe (ids/frames exact), but the [17,41]->[19,42] hop is a frame step, not a second move; the proof stands on refusal [16,44]->repick [17,41] (genuine, beyond-offset vector). Tile deltas at/below footprint-half across twins must not be read as resites. 141134 Cyber demote verified (STARTED f4349 [112,13], ledger warp f4536 = 5f re-arm); 'first Cyber demote' is B-chain-scoped per DATUMS numbering (6 sibling rows exist in other vehicles).. Ledger audit 2026-10-26d: 7x prepare-site-overlap exact, REQUESTED [17,41] f4821 + STARTED [19,42] f5296 + 100th demote f5133 stoodStillFor=90f + Cyber STARTED [16,44] f4690 all exact; same-event tiles differ (+2,+1) = half of a 4-wide footprint in 3 cases (oid25 GW, Nexus x2 games) - adopt path rows bot-TL (ProduceBuilding.java:402), warp-observe path rows unit-center (ProtossWarping.java:91), startedCids dedup picks the winner; attribution joins safe (ids/frames exact), but the [17,41]->[19,42] hop is a frame step, not a second move; the proof stands on refusal [16,44]->repick [17,41] (genuine, beyond-offset vector). Tile deltas at/below footprint-half across twins must not be read as resites., Stargate resite proofs stand, warping-link sighted 4x, proof pending). 726D7D69 (FFA): all previously-proven gates re-fire (pylon-overstock 10, below-min-supply-and-poor 8+7, pylon-cap 3, core-priority 2, unstarted-pylon 2, funding 1, earlier-reserve 1), no warping-link, no prepare-site-overlap, 0 PLACEMENT_FAILED rows - deletion gate still far. Re-verify 2026-10-24 script: same re-fire shape (overstock 10, below-min 16, cap 3, core-priority 2, unstarted 2, funding 1, earlier-reserve 2; 9415845A smaller same set) - no new gate, no warping-link/prepare-site-overlap. Sweep 2026-10-26b FFA-wide: all 7 guards re-fire in every game, no new gate. Reference-frame audit 2026-10-26c: same-event tiles differ by exactly (+2,+1) = half of a 4-wide footprint in 3 cases (oid25 GW [17,41]vs[19,42] f5296, Nexus [103,37]vs[105,38] f9390/f8366); adopt path rows bot-TL (ProduceBuilding.java:402), warp-observe path rows unit-center (ProtossWarping.java:91), startedCids dedup picks the winner; counts/frames/ids exact everywhere, so attribution joins are safe - but the [17,41]->[19,42] hop is a frame step, not a second move; the proof stands on refusal [16,44]->repick [17,41] (genuine, beyond-offset vector). Tile deltas at/below footprint-half across twins must not be read as resites.
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
  paid = has building unit" as the single exclusion. Live specimen: B136 [119,12] storm (DATUMS §3: 4 oids re-mint one tile, 22 CANCELs) - multi-author single-tile demand for the WO-144 duplicate-author question.
  Unify behind one ordered reserve ("already paid = has building
  unit") with twin-pinned duplicate rows as the proof; WO-145/147
  territory, never a blind merge. Re-verify 2026-10-24: four models still present in src (grep), EarlierReserveDivergenceTest 2/2 + QueueLiveGateTest 1/1 green - no merge. Code audit: B stop-at-self confirmed (ShouldNotTravelToConstructYet.java:161-166, queue order = priority order) - divergence trigger stands as documented. Model A exclusions in code (ProduceBuilding.java:225-228: skip orderless/self/id>=self/null-type/already-paid). C: funded() serves ranked ladder top-down from virtual bank (GoalBoardWallet.java:8-11 doc, :26-36); D: caps 500/300 (ReservedResources.java:9-10), no already-paid concept - 4-model table complete in code.
- **#157** Semantic `ConstructionOrderStatus` queries (SOLID review,
  agreed): new statuses force edits in ~6 reader classes each. Add
  enum-level answers first (`isWaiting`, `countsAsDemand`,
  `isTerminal` - additive, stub-pinned, zero behavior change), then
  migrate readers one by one with a game proof each (construction
  is the most Wine-proven area; no batch migration). LANDED
  2026-10-20: all nine PREPARING readers read isWaiting() instead of
  re-spelling the disjunction - same predicate, zero behavior change
  (`ConstructionOrderStatusTest`, `CoreSingleInstanceGateTest`
  carry the semantics). Re-sweeps stay zero (definition + one
  isWaiting-based radius reader + single-state checks only);
  per-reader game proofs ride normal macros, no dedicated games. Re-verify 2026-10-26: sole PREPARING writer is setStatus (PrepareConstructionSite.java:72), zero re-spell. Sweep 2026-10-26b: count still 1 (setStatus only).

- **#160** Automated tuner winner selection (owner 2026-10-21:
  selection is eyeballed today, make it the best long-term loop).
  All six steps LANDED and pinned: (1) objective in code (win, then
  hits asc, then seconds asc - `TunerObjective` 7/7); (2) elitist
  hall of fame with radius mutation (`TunerHallOfFame` 5/5);
  (3) K-of-N promotion bar (`TunerPromotion` 4/4); (4)
  freeze-then-expand (`mutateAroundUnfrozen`, 6/6); (5) batch
  archives (`manifest.txt`, 1-game proven); (6) gradient loop
  (`TunerFitness` TIMES_HIT x minutes, scenarios, least-squares
  slopes, proposer/driver, run-wine-tune.sh) plus the in-JVM sim
  batch runner (`SimTunerBatch` 6/6, `--engine=sim` works).
  Verdict: the machinery runs clean end to end, but efficacy is
  unproven - duel noise dominates the gradient at K=3 (97 vs 163
  first-blood frames identical code; 30-game run best 2.93, round
  means 15-19 with spikes to 24). Open: K-of-N replicates + more
  rounds before any promotion counts. Lesson kept: all schedule
  math under LC_ALL=C (awk emitted comma decimals otherwise). Re-verify 2026-10-24: TunerObjectiveTest 7/7 + SimTunerBatchTest 7/7 green - machinery still clean, no new K-of-N batch, efficacy still unproven.

- **#164** OpenBW takeover scope (owner 2026-10-20: this model leads
  logic/changes, other model gets independent file-scoped tasks).
  Single source of truth: `/sc-ai/OpenBW/openbw` clean fork
  @`801e238` (`feature/support-ums-maps`) is the canonical engine;
  the harness copy `StardustDevEnvironment/3rdparty/openbw` is a build
  artifact, never edited directly (install flow via
  `scripts/build-openbw.sh --install` with backups, never blind rsync
  or plain `cp`). Re-verify 2026-10-26: fork clean (empty status), 801e238 is ancestor of HEAD 4a7fa93 (support-ums-maps history kept, other-model branch active), --release 8 pinned (run-tests.sh:93) - takeover scope holds. Atlantis owns bot logic + sim + twins; engine C++
  changes land only via the install flow + `tests/run.sh` green.
  Baselines 2026-10-20: host smoke `test-openbw-host.sh` PASS
  (BYTE_2_OK + MATCH_TICKING + MatchStart=YES + 12/12). Re-verify 2026-10-26c: smoke PASS again (byte 2 in 95 ms, 0->11 ticks, MatchStart=YES, 12/12); engine
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
  Optional `IDEAS.md` only if the ideas list outgrows NEXT. Triage 2026-10-26 (first, was never triaged): engine branch clean, last 15 commits all Steamhammer/zombie work (slot-selection blocker per 113aaa0) - zero done-signals on A3.3/B2/E3/A3.1/B3/C1/C3/A1.4b; no Atlantis-Java overlap (our tree shows only M _AI/NEXT.md). Still owner/other-model-blocked, no handover to verify. C1/C3 readiness 2026-10-26b: single SHM cleanup (run-openbw-e2e.sh:636 rm socket+shm) - split keeps one cleanup.
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
 2026-10-22 wiring LANDED (high side preferred via HighGround.preferFoeSide + ChokeStandoff.chooseHold + foe-side candidate, #170-guarded: never march through the enemy to reach it). Pins: preferFoeSide 4/4 + chooseHold (mutation-proofed); the holdPointFor wiring itself is live-only by construction (test heights always read unknown, so the fallback path is behavior-identical in-JVM) - the TauCross macro never engaged it (Hydralisks are ranged, hold correctly silent). 726D7D69 (Zerg + hydra flood): 0 MainChoke rows - same correct silence vs ranged. Live proof still rides a melee-choke game. 2026-10-26 no-contact hold LANDED (live 7th_rav vs Protoss: 2-unit line marched to the far MainChoke standoff with no contact, uncovered the base while Zealots ran by): FIGHT + home anchor + no ongoing fight now holds halfway main->main choke (ClusterRally.noContactHoldPoint seam, owned by Regroup.march before formation/ghost/gather; never on FLEE, attack anchor, or into an ongoing fight). Pin: NoContactHoldTest 3/3; unit scope 1435/1435. Re-verify 2026-10-24 script: choke-key rows 0, HighGround.preferFoeSide + chooseHold present (ChokeStandoff.java:92-93), groundHeightOf guarded -1 off-map.
- **#172** Region-aware escape destinations (owner 2026-10-20, PurpleWave
  match review: losing won games to corner traps, Dragoons most of all).
  Stardust read, no copy (boids + walkable rays + collision vectors +
  NoGoAreas + unstick-via-STOP; no destination scoring there - this is
  our own work). Architecture (`battle.escape`, one reason per class):
  `RegionShape` probe, 8-dir walkable candidates, PurpleWave-detailed
  scoring (corner/peninsula, static obstacles, all enemies within 20 of
  the dest, size factor), minimize-hits-plus-time objective, tuner
  record with per-map overrides. Slices #174-182 all closed
  (sim-proven each: probe, candidates, static + corner scoring,
  enemy-20 avoidance, size-aware clearance, tuner wiring, pinch gate,
  second consumer, high-ground scorer). Open: live superiority proof
  (needs games) + tuner efficacy. Center-pull deliberately not added
  (corner rays measure edge risk directly; a center prior would bias
  away from measured data). Re-verify 2026-10-24: EscapeArcTest 4/4 + DragoonsVsZerglingsTest 23/23 (wall funnel + capacity) green - sim pins hold, live superiority still needs games.

- **#173** Owner escape doctrine: long-vector-first (owner 2026-10-21,
  1Drag_v_1Zeal edge-freeze; live Wine datum: the dragoon flees short
  and takes hits). Mechanism verified in code: short steps die in
  validation, not in scoring - `MoveAway.bestDestination` vetoes
  clamped micro-steps (half-distance gain floor) and
  `IsReasonablePositionToRunTo` gates (walkable(2), ground-distance,
  minerals), so every direction returns null at a wall and the caller
  fights in place; the scored pick never rejects, it returns the
  least-bad short step. Staged: (a) NOW detector-first long escape
  (away-ray <5 tiles = straight-5 blocked, deflect at full length;
  open ground bit-identical, kite footwork stays short); (b) NEXT
  angle-stepping if the long pick still traps; (c) LATER precomputed
  peninsula map (cornerPenalty is local-only); (d) LATER hard-map
  tuning vs chasers. NOT primary: shorten-and-retry (shortening
  re-traps) or a long leg inside pure pick() (walkability is
  prefix-closed - verified no-fire). Verdict (duel batches + grids):
  the per-cycle standstill lever (finishedShooting release, #152)
  fixed it (V0 15.7 -> V1 12.0 -> V2 9.6-10.3, all VICTORY);
  step amplitude has no gradient (2.0/2.5/3.0 grid flat), margin
  2.0->2.5 adds nothing (plateau, t~1.4); residual contact is
  standstill per shot cycle, not a speed cap (dragoon 5.0 vs zealot
  4.0 px/f CAN open distance); bistable opening decides the
  attractor, knobs only shift odds. Long-deflect branch verified
  present but never engaged on open maps by design (fires only when
  a wall eats the ray) - engagement proof needs a walled pocket
  map, not more open duels. Main kite path deliberately still on
  MoveAway (rollout: DuelKite yield first, ARunningManager second); long-deflect still unengaged (needs a walled pocket firing it, not more open macros - B136 retreats resolve home/mission-side on open ground, DATUMS §6). Live engagement 2026-10-26 (GAME_726D7D69 Steamhammer loss): 13 long-deflect rows, two bursts - probe #141 at (120,15) foe (111,20) ~10 tiles f8361-67, probe #11 at (121,5) foe (120,20) ~14 tiles f10903-08; map-corner geometry, real foes, zero stuck rows for either unit after (#141 lived ~2800f more, died f11146 to hydra elsewhere). Branch engages live as designed (wall eats the ray, deflects at full length); corner, not interior pocket - pocket-shape engagement plus the (b) trap question (did any deflectee then glue?) ride the next walled-pocket game, proof NOT claimed from this alone. Sim probe 2026-10-26 (throwaway, not committed): U-pocket dead-end marches 5 out the mouth even through the threat tile (no short-shuffle trap - long pick works, nothing for angles to fix); ring-with-diagonal-slit is the only trapping shape found, and only at sub-15° angles no compass dir covers (true box for wide units either way) - contrived geometry with zero live instances, so (b) stays parked: no angle-stepping without a live trapping instance or post-split stuck-validation cluster naming pockets. Code audit: LONG_STEP_TILES=5 straight-then-full-length-deflect (EscapeDestination.java:77-89); EscapeLongVectorTest 9/9 green.

- **#175** Arc retreat toward the squad (owner 2026-10-21, LOW priority,
  future): a Dragoon costs ~two Zealots, so lone kiting weakens the
  army and straight-line fleeing splits it - flee along an arc back
  toward the squad instead (stay in range, keep focus fire). Prove in
  sim first (arc vs straight: win rate, time-to-win, split distance),
  then live. Sim-first slice LANDED, default blend 0 (pinned
  `EscapeArcTest`, `ArcRetreatSimTest` A/B): split 4.21 -> 3.10,
  deaths equal, shots up. Efficacy matrix: blend sweep collapses the
  scatter ~9x/~5x and doubles the guns on both geometries (pinned);
  default stays 0, tuner picks the value (`EscapeArcBlend` 0..1
  registered). Forced-blend duel: no regression, no miracle (one
  game is noise). Open: contact-aware bending (bend away from
  contact, not just toward the squad - the chew-kites doctrine costs
  the arc arm one dragoon in contact-start surrounds); the +1 deaths
  bound stands with the reason. Re-verify 2026-10-24: ArcRetreatSimTest 3/3 green - blend 0 default stands, bending still open.

- **#178** Zealot kiting (owner 2026-10-21/22): zealots never yield,
  so vs faster-attacking enemies they stand through the cooldown and
  bleed. Rule: after attacking with cooldown remaining, withdraw a
  step when (a) wounded, (b) locally outnumbered, or (c) the foe
  attacks faster - skip while ~unhurt (~10 damage is no wound).
  Mirror of the dragoon DuelKite footwork, zealot-flavored: save HP
  that would otherwise be donated, never break the line on open
  ground. Landed sim-first (margin branch takes a full footwork step
  for withdrawing zealots; pinned `ZealotWithdrawSimTest`,
  mutation-proofed) and live on headless ling maps: the
  sim-predicted pathology (130/130 stuck, zero moves) is gone live
  (272 stepped-aside, zero standing deaths - zealots withdraw instead of donating contact; second live instance B136 Zealot#137, withdraw-then-die, not donation - DATUMS §3).
  Single-game noise per §14; the sim pin is the isolation proof. Re-verify 2026-10-24: ZealotWithdrawTest 5/5 green; 726D7D69 11 zealot deaths all by lings/hydras (9/2) with BREACH 255/VETO 46 breaks - withdraw-not-donate shape, no freeze.

- **#179** ZealDrag_v_Zeal coordination + zealot hold-fire discipline
  (owner 2026-10-21/22). Doctrine: zealots hold formation until a
  ranged weapon fires or a zealot is attacked; formation hold owns
  only the 2.5-9 tile approach window; kite outranks holding and
  trading (chewed voids every stand; swamped-4+ holds fire while the
  step rides); shoot-now targeting (weakest valid in range first,
  re-arm disciplined); post-swing settle; frozen detection + error
  row (FrozenWatch, passive by design). Landed, all pinned
  (`FormationHoldTest`, `AcquireTargetTest`, `AttackSettleTest`,
  `FrozenWatchTest`, `PhalanxVsPackSimTest`, arc re-geometrized to
  in-contact starts, fogged-snapshot own-copies). Live band (9 games):
  one 12/5 win vs eight losses; march-from-frame-0 proven, holds fire
  then veto breaks correctly, zero FROZEN, all deaths mobile
  (ClusterKite/Unstick) - coordination and freezing closed as causes.
  Open: winnability (early-brawl DPS), not coordination - B136 weak positive the other way (won 5/3 with dragoon mass, DATUMS §3/§6); Steamhammer FD18F82F (loss, 21 deaths: 17-probe run-by f5145-5517 + 4 zealots): no freeze anywhere; the failure is defense coverage (run-by unanswered), not coordination. 726D7D69 (Steamhammer Zerg loss, 40 deaths all by lings/hydras: 24 probes in-base): same coverage shape - army fights throughout (BREACH 255 / VETO 46 / SHOT 20k rows), zero freeze; loss by mass, not coordination. Confirms the breach-doctrine fix holds vs Zerg too (user hold-while-breached report was the pre-b62e78aed jar). Re-verify script: SHOT 20622, HOLD BREACH 255/VETO 46, 9415845A 43 deaths FROZEN 0 - same coverage shape. Ledger audit 2026-10-26c: 184652 death exact (Zealot#165 f9554 (112,39), 4 CANCELs as filed) - combat death, not straggler, taxonomy holds. Re-verify 2026-10-26: bot won 0/9 FFA (7 losses to Steamhammer/Zealot Hell + 2 crashed) - winnability vs real opponents still open, DPS not coordination.

- **#180** Per-second unit telemetry (owner 2026-10-21/22, extends
 #176): the death log gains meaning only with the fight around
 it. Every second log per unit: zealot-dragoon distances, HP,
 and enemy count within 2 tiles of the unit. Store compactly
 (throttled twin, one row per unit per second, not per frame);
 the dead-unit log then shows the class that decided (#176) plus
 the 5-second approach that killed it. Delete or thin when the
 coordination items close - diagnostics, not a permanent feed. Re-verify 2026-10-24: UNIT_TELEMETRY 3336/2790 + ORDER_NOT_READY 585/553 MED by design (#189) - keep until #184 ladder lands, thin after. Throttle in code 2026-10-26b: TRACE_EVERY_FRAMES=24, sampled not pushed (UnitTelemetry.java:26/:32/:48) - MED volume is bounded by construction.

- **#181** Split-cluster reinforcement (owner 2026-10-22, HIGH priority):
  ZealDrag_v_Zeal live: one half shredded while the other waits - detect the shape (per-part damage + no-order windows, logged loudly), merge toward the fight or forbid waiting. Suspects: engageable caps, squad assignment, anchor/formation fallback.
  Landed, all pinned: UMS-only reinforce to freshest contact (240f brake); ghost-recall fix (FormationHold declines ghost contacts unless really threatened - new GHOST verdict); ongoing-fight reinforce ahead of slots (ArmyContact.isOngoingFight 72f, UMS+FIGHT only). Timeouts were the dead Regions path, not the reinforce (regions repaired, ghost=sweep from frame 0). Weak live positive (Defeat 3/9: no idle half, all engaged). Open: a repeat of the park-while-fighting shape with the reinforce firing on it (B136 second weak positive, no idle half - DATUMS §6; still no park shape to fire on).

Priority note (owner 2026-10-22): the three items below slot BELOW
the production ratchet (#45, #144-147 per-gate deletions) - small
sim-pinned slices, game-verified one at a time, never preempting
the deletion order. Combat work stays off the commanders
(CONVENTIONS §23): new logic lives in FormationHold / FightTarget /
AcquireTarget seams, BattleCommander only keeps delegating.

- **#182** FormHold stands into chews; frozen holders have no remedy (owner 2026-10-22, HIGH priority).
  Mechanism (verified in code): proximity gates read `enemiesNear()`
  while shooting/telemetry read `Select.enemy().combatUnits()` - when
  they disagree the hold is blind; damage gate structurally dead for
  steady holders (see #183); holder unstick missing. Landed,
  sim-first, no behavior change without the trip (pins:
  `FormationHold` veto 2.0->3.0 + HoldVerdict + throttled HOLD twin,
  holder unstick 48f-still + off-slot -> STOP once).
  Live proof: no 200f stands since; the new shape found instead is a
  fight-loop stall (target churn + re-arm spam while holdSwing/settle starve kite), owned by the #184 ladder, not this item. B136: longest HOLD run 14 rows, veto breaks holds on contact - no stand-and-trade shape. Re-verify 726D7D69: longest HOLD chain 11 (<=5f gap), HOLD verdict HOLD 14/341 vs BREACH 255/VETO 46 - no 200f stands. Code audit: WEDGE_STILL_FRAMES=48 (:49), STOP-once discipline (:122/:279); FormationHoldTest 18/18 green.
  Sim isolation (PocketVsOpenFieldKiteTest): geometry alone explains
  stand-and-trade (open field displaces 15.2 tiles, pocket 1.0).
  Decision #182(4) (documented, not relaxed): the FrozenWatch DEPLOYED
  gate stays - pre-gate all 9 units flagged at f40 deploying, sim
  cannot clear the relaxation (actuates frame 1), and the remedy side
  never needed banking (holder/kite unstick trip on stillness, row it).

- **#183** `isUnderAttack()` is dead for steady holders (owner 2026-10-22, HIGH priority):
  Pipeline order: UnitStateManager (step 2) appends fresh `hp()` every frame it runs, so a holder that stopped re-issuing reads `hp() < last` as false by construction - the damage gate can only fire on gate-skipped frames. (Side correction on record: `hp()` = hitPoints+shields; shields were never blind, freshness was.)
  Direction: battle-hook hp snapshot (`DuelMemory.noteBattleFrame` in observe, latch `tookDamageSinceLastBattleFrame`) instead of manager history; rewire hold + zealot gates; audit-then-migrate Terran/repair/interrupt consumers one by one, no batch (moving UnitStateManager after battle rejected: pipeline-global, same coupling class). Prove: unit tests + sim (holder breaks on first damage) + live (~5 frames on ZealDrag_v_Zeal).
  Landed, pinned (`damageBreaksAnOtherwiseCalmHold`, FormationHold 11/11): latch is a backstop - VETO and the guns-in-range gate shadow it on every live board so far (DAMAGE verdict 0 rows as designed, not a latch failure); the sim pin carries the proof. Audit outcome: #179 zealot gate never read this signal (compares against MAX) - no migration there; `PreventAttacksInMissionDefend` (the one live Protoss consumer) MIGRATED to `DuelMemory.damagedLessThanAgo(45)`, pinned 2/2. The rest stay on history for stated reasons (Terran-only tracks the battle hook never observes; D8 Protoss-only keeps them dormant anyway). Second live instance (B136, 0 DAMAGE rows): latch still backstop-only. Re-verify 726D7D69/9415845A: DAMAGE verdict 0 both, hook only in PreventAttacksInMissionDefend - latch still backstop. Code audit: noteBattleFrame snapshots hp/battle-frame (:56), damagedLessThanAgo window (:77), tookDamageSinceLast (:90); DuelMemoryTest 2/2 green - freshness argument holds.

- **#184** Targeting: ladder refactor of ATargeting (ordered rules, coordination dropped) (owner 2026-10-22):
  Complaint, mechanism verified (no guessing): dragoons keep a far target while a low-HP zealot chews them - focusInRange stickiness never scans the chewer, and isValidTarget bans sub-15hp/zerglings/workers outright. The user's one-shot rule is the exact inverse of the ban.
  Agreed shape: full audit first (~1150 live lines, 13 files - the "mostly commented out" estimate was wrong and is withdrawn). Sequencing: the ladder lands only AFTER the production v1 cut (#45, #144-147). 2026-10-24 ladder SKELETON landed early (owner overrule for the Steamhammer loss: chewer + keep-current + weakest-in-range extracted as behavior classes, order lives once in TargetLadder with order-sensitivity pins - the rest still calls existing implementations in place). Rules stay hand-written; learning the gates stays sequenced post-cut. The skeleton IS the #185 action space from today.
  Coordination review (owner: "do wyrzucenia" = think hard, not a deletion order): squad-last-target, leader-picked target and current-target stickiness are removal CANDIDATES, never pre-decided (old coordination code has zero callers and does not ship, but the concepts live on in StickyTarget/FocusFire/ASquadTargeting - each removal needs a sim no-regression proof). Rule list: (0) wounded->closest when hurt; (1) melee-proximity no-target gate; (2) one-shot killable in range, non-swarm; (3) mostWoundedOrNearest; (4) C5a chase; (5) fallback nearest engageable. Dropped: coordination prefs, Terran/air/transports/buildings/disabled code/Hydra special (reasons on record). Non-goals: buildings/air/transports/Terran (D8; LingsUpg stays the proof vehicle).
  Landed slices (all pinned, suite green): fight-mirror gate (fight's own threat into shouldKite); chewed-voids-every-stand doctrine (348->74 chewed frames sim, 9-game live band: coordination holds, brawl lost on DPS not freezing); rearm never cancels a riding step (swamped-4+ line calibrated: guns win to three, four donate); meat-screen withdrawn; nearly-dead finished first (hp<=15 ban deleted); ling/worker bans audited (focus-only, anti-overkill by design - engage paths skip validity); fallback-lands-lings ANSWERED (engage paths, code-read). Keepable-vs-sub35hp verdict MEASURED 2026-10-24 (`KeepableFinishEfficacyTest`: hold-primary clears tick 140 with 75hp kept and 13 switches; forcing the target onto the 10hp chewer donates the dragoon - 0hp, pack never cleared, 70 switches, target starved to null by the validity gate). Forcing churns against `isValidTarget`, it never tests finishing: a faithful finish rule must live INSIDE the ladder next to MeleeChewerTarget, which is ladder surgery sequenced post-cut - no behavior change, question parked with proof. B136 dragoon deaths (#169/#172, DATUMS §3): firing to the end, no freeze, no march-through; keep-vs-weakest undecidable from twins (no target ids), no code. Steamhammer datapoint: zealot #148 fired 473x while holding to death - guns not stuck on invalid targets. Re-verify 2026-10-24: TargetLadder skeleton present (TargetLadder.java:32), TargetLadderTest 3/3 green - order lives once, sequencing post-cut stands. Code audit: order() (:37-38) + pick chain MeleeChewer->KeepCurrent->WeakestInRange (:56-65) - order lives once, verified. Slots audit 2026-10-26b: order() lists 9, pick() chains all 9 (3 extracted classes + 6 legacy-backed) - action space exists as slots, class migration pending. Ghost-creep follow-up CLOSED 2026-10-24 (pin existed and is green: `GhostCreepWalkableTest` 2/2 - `slideOffWall` seam plus `move()` issuing walkable-only dests over 120 ticks with the ghost inside a wall; the `move()` walkability check plus slide it asked for had already landed, only the NEXT line was stale).

- **#185** Learned behavior gating over a faithful Java sim (owner 2026-10-22):
 Direction (owner): keep the behaviors, learn WHEN - the tuner tunes global scalars, the pain is state-conditional gates (2.5 tiles etc. hand-set, each new Behavior multiplies hand-gating complexity). A policy over the fixed Behavior set, from features (distance, hp, shields, cooldown, counts, terrain), decides the behavior; thresholds stop being hand-written. No live snapshots (owner decision: synthetic scripted maps instead - scenario control plus mandatory enemy-policy variety so gates cannot overfit one script; deterministic fitness is traded away knowingly). Sequencing: AFTER the production v1 cut (#45, #144-147); design now, implementation later, other-model implementable with sim-pinned slices + review.
 Stage A - fidelity loop (gates everything else): enumerate the sim-vs-engine gaps and close them in Java, gap by gap, each sim-pinned: order issue/cancel/replace latency + stickiness (the known DPS divergence), accel/turn/top-speed per type, weapon cooldown/damage/animation timing, collision/pushing, fog/visibility for targeting inputs. Validation protocol: twin-logged live trajectories (UNIT_TRACE ux,uy + cooldown + hits are already logged) vs sim replay of the same initial conditions; error metric per unit per frame (positional drift + outcome delta). Falsification trigger, written down in advance: if after closing the listed gaps the sim-vs-live outcome delta on frozen scenarios stays above bar (win-rate delta >20pp or mean positional drift >2 tiles at 600 ticks), THEN revisit C++ (Stardust shared-memory model) - until then Java stands (speed was never the measured bottleneck; fidelity is the work). Abandoning the project over speed alone is off the table without this measurement.
 Stage B - scripted minimap scenarios (64x64 presets: formation circle vs static enemy with aggro-6, pocket/wall variants, outnumbered variants) as sim fixtures with deterministic seeds; enemy POLICY variety mandatory (static, chase-nearest, focus-weakest, kite-back); winning shapes become fast-suite tripwires.
 Stage C - learner, interpretable first: contextual bandit / decision trees over the feature set → Behavior, deployed as readable rules (journal tags keep logging chosen behavior + top features, so post-hoc analysis survives and the owner's automation goal is met without a black box). Neural net only on plateau, behind the same seam (features in, Behavior out) - and never trained on a sim with known dynamics gaps (Stage A bar first). The hand ladder (#184 TargetLadder) becomes the learner's action space; hand-written gate conditions are what the learner replaces.
 Architecture shape (owner 2026-10-26, agreed after review with a second model): modular Behaviors, NOT nested ifs, NOT the old Manager framework. Each behavior is a fresh small class with applies() (gate) + act() (order) - no usedManager bookkeeping, no interrupt chains, no per-unit manager field; that baggage stays buried with the old tree. The order lives ONCE, as an explicit ordered list (precedent already in-house: Commander.subcommanders() documents "Order matters: it is the execution order") - visible, re-orderable, testable, which is exactly what nested ifs hide. The second model's objection (reordering managers broke everything) is answered by construction, not by file layout: disjoint-or-explicitly-prioritized guards, ONE order, and order-sensitivity pins in sim (swap two adjacent branches → the test names the intended change or fails). Ifs vs classes was never the real axis; implicit vs explicit order was.
 Gating seam per behavior (the NN plug point, mirrors ProductionV2Mode OFF/DRY_RUN/LIVE incl. typo-safe default OFF): each gate answers from the hand rule OR from the NN score behind a flag - per-behavior flags like V2's per-gate flips, so rollout is gradual, plus a global kill-switch. Fallback is mandatory, not optional: NN uncertain or output missing → hand rule decides, so the net is never a single point of failure in a live game. DRY_RUN mode is the trust builder: the net logs what it WOULD pick next to what deterministic picked, per frame - disagreement rows plus outcomes become the evidence base, no blindness, no faith required. Deterministic mode stays available permanently (owner requirement): same binary, flag-selected, doubles as the A/B baseline that proves the net (frozen sim scenarios, NN vs deterministic, efficacy bar before any live takeover).
 Migration is slices, never big-bang (production cut stays the priority): extract current branches (retreat, leave-breached, hold, kite/glide/re-arm pieces, regroup...) into Behavior classes one by one, order kept 1:1 at first (zero behavior change, suite green each slice), only then does the order become data. BattleCommander.act() iterates the chain - pure delegation, CONVENTIONS §23 intact. Guard design lives in `_AI/redesign/05_BEHAVIOR_GUARDS.md` (spec only until the production cut lands): sequential levels, disjoint guards, continuity by narrowing.
 Non-goals: C++ rewrite (revisit only on the falsification trigger); end-to-end pixel policies; tuner scalar loop untouched (it keeps tuning globals); #184 coordination review stays separate (action space, not gating).
 Readiness measured 2026-10-24 (owner: examine NN training in StarEngine): the sim runs ~400 us/tick with full decision code (1 dragoon vs 5 lings, 600 ticks = 241 ms) - ~1000x cheaper than a Wine game, overnight bandit-scale training fits. Features exist per tick (hp, shields, cooldown, distances, counts, foe positions, walkability); reward exists (TunerFitness win-then-hits-then-seconds + SimTunerBatch manifests); the ACTION SPACE now exists (TargetLadder 9 rules + movement behaviors, this session). Twin side: CLUSTER rows carry leader dest (dtx/dty) since 2026-10-23 and nearest-foe tile (fx/fy) since 2026-10-24 - the #188b blocking-geometry proof is finally measurable live. First game on the extended twin (B132): fx/fy present on 154/154 rows - but the only FLEE was foe-unknown, so still no blocker geometry to reconstruct. Gaps before any learner: (1) no unified zombie-policy library (chase-nearest + stand-trade are copy-pasted per test - focus-weakest/kite-back/static missing, overfit risk per the mandate above); (2) no per-tick feature snapshot export; (3) no NN lib in the project (Java 1.8) - the in-house path is contextual bandit/linear first (zero deps, interpretable per Stage C), weights-out/CSV-train-outside second (ParamTweaker precedent). Update 2026-10-24: gaps (1) and (2) closed in test scope - ZombiePolicies (chase-nearest, stand-and-trade, focus-weakest, kite-back, holdStill; deterministic, pinned) + ZombieSimLoopTest (library drives the real loop) + BattleFeatures snapshot (7-column CSV rows, pinned). Remaining: (3) no NN lib (unchanged) + feature export is test-side only (production gating needs the seam). Update 2026-10-26: FakePositionFinder now spirals for real (was identity - the blind spot that forced GhostAdvance to re-implement sliding), and EpisodeManifest renders per-tick CSV rows for offline analysis. No behavior change anywhere (walkable-everywhere stubs answer identically). Sequencing stands: production cut first, learner slices after - but every NEW behavior lands sim-pinned from today (CONVENTIONS section 29). Owner-confirmed 2026-10-26: StarEngine fidelity (Stage A) before any network (Stage C) - no training on a sim with unclosed dynamics gaps. Re-verify 2026-10-24: ZombiePoliciesTest 5/5 green, seam HoldEarlyDisabledForAB present (DuelKite.java:71) - gaps (1)(2) closed test-side, (3) NN lib still open, sequencing stands. Code audit: Behavior.java is a frozen-tag vocabulary enum (tags = old literals), prod wiring only returns values (BattleStrategy:57-66, GhostAdvance:304) - no gating, no drift. Learner audit 2026-10-26b: no gated Behavior classes in prod (pre-existing applies() in detectors/siting is old framework, not the seam), no NN - spec-only holds. Dependency audit 2026-10-26b: zero NN libs in src/lib (no deeplearning/nd4j/neuralnet/tensorflow/onnx; vendored jars are archunit/jbwapi/junit/junixsocket) - gap (3) confirmed at dep level, in-house bandit/linear path stands.

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
 Overnight replication 2026-10-23 (n=6, same jar: full stack): 6/6 VICTORY, hits 8/7/8/12/6/14 (mean 9.2) - win kept, V2 band holds at n=9 combined; still no wiring-attributable gain (discrimination limit stands). Replication 2026-10-24 (n=4, same jar): 4/4 VICTORY, hits 11/12/12/14 (mean 12.25) - combined n=13 at mean ~10.5, V2 band holds Duel 014201: VICTORY, 11 hits taken - n=14, band holds; duel 020017: VICTORY 23s, 18 shot-fired / 8 hits taken, 40 stepped-aside + 5 duel-yield + zero stuck/frozen - n=15, band holds; A/B verdict (code-read): single call site (DuelKite.shouldYield -> shouldHoldEarly, DuelKite.java:72); a live n=1 A/B cannot discriminate (established noise band), so the A/B rides the K-of-N batch, not a one-off duel. Seam LANDED 2026-10-26 (volatile `holdEarlyDisabledForAB`, default false; `SimTunerBatch HoldEarlyDisabled=0/1` passthrough outside the knob registry + audit line; pin `wiringToggleRestoresPreWiringAnswer`; suite green). Sim A/B measured 2026-10-26 (mid-vector, K=9/arm, paired vector + deterministic spawns): OFF 9/9 WIN hits=0 17s vs ON 9/9 WIN hits=0 17s - identical. The sim cannot discriminate the wiring at this scenario difficulty (spawn 13-15 tiles, flawless wins both arms - same verdict as the T-duel proxy, now with the real toggle); blocker is scenario difficulty (Stage B harder scenarios), not the toggle. Vehicle works. Re-verify 2026-10-24: StopPointTimingPinsTest 3/3 + ProtossAttackHoldToShootTest 11/11 + ChaserCommitmentTest 3/3 green, wiring single call site DuelKite.java:72 - sim still cannot discriminate, live 1Drag proof remains. Re-verify 2026-10-26: wiring intact (DuelKite.java:47 seam default false, :71 call site), n=15 band stands, no new duel batch - proof still open.


- **#187** Scout persists in the enemy main (owner report 2026-10-23, critical): a scout that finds the enemy main with no battle units yet leaves and wanders to far expos instead of watching. Required: stay in/around the enemy main gathering intel until battle units chase it out; then keep a circling mission nearby and retreat (natural, third base) only when hurt. No unprompted wander to unscouted expos while the main still has unseen area. Sim pin first (scout-vs-chaser: intel frames in main dominate the pre-contact window), live proof after on a game with a real enemy main (Wine with a real opponent; the UMS vehicle once #34 lands). Update 2026-10-26: clock-cutoff slice LANDED (sim-pinned): `ScoutNearEnemyBase` no longer declines past 340 s - the ancient gate evicted unchased scouts into far-expo wander; leaving is the chase machinery's call only (5/5 `ScoutCirclesEnemyMainTest`, suite 1335/1335). Fallback slice LANDED 2026-10-24 (sim-first, red-green): Separate's fallback tried scored 8-tile pick first (`separateFallback`, `ScoutSeparateScored`), old first-success stays last resort; scored blockers now include enemy buildings (boxed scout hugged their edges unseen - repro dest (36,20) edge 0.5, now clears 1.5; 2/2 `ScoutSeparateFallbackTest`). Live proof still pending. Steamhammer FFA (real opponent): only separation micro fired (ScoutSeparateFromCloseEnemies 5x, HoldCourse 3x) - no enemy-main watch rows anywhere, and combat-unit-only telemetry carries no scout trail, so the watch is unproven and unwitnessed there too. B134: no SCOUT twin rows at all in 97k rows; early ling rush killed 3 probes at f4330-f4402, likely including the scout before any main-watch. B136: 0 SCOUT twin rows too (macro vehicle blind again - live proof still rides a real-enemy game). 3rd real-opponent game (Steamhammer FD18F82F, loss): 0 SCOUT rows again - the scout never produces a watch trail; live proof still pending. 4th real-opponent game (Steamhammer 726D7D69, Zerg loss): 0 SCOUT rows again. 170306 (Steamhammer Zerg, VICTORY 2/0): 0 SCOUT rows again - 5th real-opponent game, watch still unproven and unwitnessed; live proof still rides a real-enemy main Re-verify 2026-10-24 sweep: SCOUT 0 in 306 files (out + REPLAYS), FROZEN_WARP 0 - watch still unproven, vehicle blind. Re-verify 2026-10-26: sweep still 0 (no new games since 12:20), 5th real-opponent game still missing.
- **#189** Log priority adoption (owner 2026-10-26, remainder after the
  central slice): every console line carries `[LOW]/[MED]/[HIGH]/[CRIT]`
  (untagged reads `[MED]`), event rows carry short `"lvl"`, `[TEMP]`
  marks removable lines (CONVENTIONS section 27). Slices 1-3 LANDED
  (HIGH-by-construction error channel, remaining banners + load/race
  failures, stable-slug throttle keys - suite green). Slice 4 verdict
  (measured, no code): console side DONE (macros carry only one-shot
  startup/audit/exit MED lines); event-side MED volume (UNIT_TELEMETRY,
  ORDER_NOT_READY, CLUSTER, TECH/SINGULARITY_CHECK) is the diagnostic
  dataset and stays MED by design (reclassifying fights #180 and the
  #95 watch). #189 console work closed; `[TEMP]` lines die per session. Re-verify 2026-10-26: 0 live TEMP prints (only doc + helper), 0 live @@@@ - sweep clean. Sweep 2026-10-26b: 0 live TEMP + 0 live @@@@ again. TEMP sweep 2026-10-26: 2 live TEMP prints removed (Citadel errPrintln + 2 Archives @@@@ printlns), suite 1419/1419 green; remaining @@@@ are commented-out or the error channel (DT alerts) - kept.

- **#188** ClusterFlee overhaul live proof (owner report 2026-10-26: fleeing into death, fleeing with no visible enemy, scattered advance/retreat). Landed, sim-pinned: stragglers rally home on FLEE (never onto a forward-kiting leader); chased keeps ClusterFlee/RUN_ENEMY, unchased walks home as ClusterOrganizedRetreat/MOVE_FORMATION; blocking army between cluster and home forces FIGHT (Stardust rule, latch bypassed). Proven: new unit/chain tests green, full suite green, no-regression across clean Wine macros (homeward FLEEs, 0 scatter deaths). Pending: a real fight showing (a) organized tag in a death log, (b) a blocking-geometry fight-through, (c) cohesion through a lost fight. Update 2026-10-23: (a)+(c) partially closed by B58 - chased-FLEE + home-rally verified live (Zealot#145 died chased, both FLEE dests home; straggler-to-leader not reproduced); organized branch taken on every foe-unknown FLEE frame by construction, no death ever carried its tag; density rule deferred with measurement (max combat spread 2.7 tiles across 53 buckets. Metric audit 2026-10-26e RESOLVED 2026-10-26f: definition found in the analyst instruments (audit_fight.py:26-40, same formula in audit_one.py:44 + audit_two.py:52): per-240f-bucket max-from-centroid over Zealot/Dragoon UNIT_TELEMETRY, buckets<3 skipped, take max. Unit audit: twin logs TILES (unit.x()/32 at UnitTelemetry.java), script divides by 32 again (px assumption) - filed numbers are tiles/32, ordering intact. Verified on surviving FFA with the exact formula: 726D7D69 0.7 filed / 22.0 true, 9415845A 2.1/67.5, 30B7DE5F 2.6/82.7 - same band structure as filed B-games (tight <1, scattered >2). Consequence: absolute comparisons to tile-scale quantities are 32x off (true 86 vs 2-tile leash, not 2.7 vs 2); the tripwire works identically either way as long as the formula stays fixed. Fix direction: drop the /32 going forward and re-baseline the rule's threshold, or keep the formula and stop comparing to tile thresholds (owner call; measurement fix needs no game, threshold change does)., existing 2-tile leash + formation slots suffice unless scatter recurs). (b) still open live - no reconstructable blocker geometry in any twin (no enemy positions); sim proof done instead (BlockingForceSimTest: same wounded force both arms, blocked attacks through, lateral walks home untouched). Steamhammer FFA loss (real opponent, 61 deaths audited): ZERO march-through deaths - every death either fought (attack orders + in-range targets to the end, e.g. #177/#178/#199 kiting while chewing) or retreated-caught-at-rally (e.g. #239 marched with foe 50+ away, died to the pursuer at the standoff); the chewer rule fires live (in-range ling/hydra attacks in all zealot death logs, tgt- only in the final frames); dtx/dty live on every CLUSTER row (FLEE dests split standoff (98,96) vs mission anchor, no donation into the enemy observed - centers recede or hold). Blocking-geometry fight-through itself still unproven live. (b) still open live - no reconstructable blocker geometry in any twin (no enemy positions); sim proof done instead (BlockingForceSimTest). Proof vehicle complete since 2026-10-24 (leader dest dtx/dty + nearest-foe tile fx/fy, verified on every CLUSTER row of B132/B133/B134) - the next FLEE with a visible foe is decidable per frame, no new code needed; Steamhammer + B134 show the other half still missing (all FLEEs foe-unknown); sim proof done instead (BlockingForceSimTest). Wine 1Drag duel 014201: foe-visible CLUSTER (fx/fy live, real foe) but zero FLEE rows - vehicle works, shape still not produced. Second Steamhammer game 2026-10-24: winner Steamhammer (loss, engine-side only, no twins - vehicle health only). A real-opponent game (Steamhammer vehicle) is the live vehicle for all three. Advance cohesion reviewed against Stardust (UnitCluster::move + moveAsBall boids + narrow-choke exclusions): our formation slots + 2-tile gather leash + GhostAdvance line-march cover the same intent, measured spreads stay ≤2.7, no change without scatter evidence. Wine macro B136 (015114): first foe-known FLEE with contact ghost (14 twin rows: centers + foe tiles + dest (15,24)->(17,22) vs anchor [9,45)) - geometry reconstructable for the first time; 0 FIGHT rows in the contact window (blocking rule silent), 3 deaths all shooting + kiting (fighting rearguard, zero march-through); home-relative adjudication DONE 2026-10-26 (twin semantics code-read + HP trajectories): no blocking geometry - foe tiles (13-23,14-26) sit west by the mission anchor, main body (13-14 units, posture ATTACK, breached False) centers (47,29)->(98,28) walk east toward home unimpeded; the 3 deaths are a cut-off vanguard AT the leader-dest tile at the leader-dest tile, ~40 tiles from center, fighting to the end (trajectories in the archived twin rows). 170306 shows the shape live with twin split: post-FLEE rearguard (#167/#169 dragoons + #158/#159 zealots) fights in contact f8921+, SHOTs fired, #169 chewed (4 HITs) while kite aims fail validation with engine commandable - survives (0 deaths, win 231f later). Same cut-off shape, benign outcome; sim must reproduce the lethal variant. Ledger audit 2026-10-26d: 184652 death geometry consistent (nearest center 16.3 vs filed 18, within method) - taxonomy holds across vehicles. Ledger audit 2026-10-26d: 184652 death geometry consistent (nearest center 16.3 vs filed 18, within method) - taxonomy holds across vehicles. Ledger audit 2026-10-26d: 184652 death geometry consistent (nearest center 16.3 vs filed 18, within method) - taxonomy holds across vehicles. New shape named (not march-through, not rally-caught): cut-off rearguard at the attack dest. Twin vehicle proven complete (centers + foe tiles + leader dest + HP trajectories reconstruct all of it); the blocking instance itself still unseen. B141: 14-episode FLEE chain, 1 contact episode, foe off-path - no blocking (DATUMS §6). Ledger audit 2026-10-26c: 106 rows + 14 FIGHT-split episodes exact - the count DATUMS §1 misattributes to B142. Contact geometry 2026-10-26d: center (38,55)->dest (33,53), foe (40,59) anti-parallel to retreat vector - off-path holds; closest center-foe 4.5 (f10943/10974) vs filed 3.9 (likely unit-level metric) - definitional wobble, verdict unaffected. Contact geometry 2026-10-26d: center (38,55)->dest (33,53), foe (40,59) anti-parallel to retreat vector - off-path holds; closest center-foe 4.5 (f10943/10974) vs filed 3.9 (likely unit-level metric) - definitional wobble, verdict unaffected. B142: 12-episode FLEE chain, no contact ghost - no blocking (DATUMS §6). Ledger audit 2026-10-26c: 94 rows exact; 12 episodes under 3 splitting rules vs DATUMS §1 '14' - §1 count is a B141 copy slip (B141 verifies 14: 106 rows FIGHT-split exact); NEXT.md 12 stands. First foe-known FLEE vs a real opponent (Steamhammer FD18F82F, 1 row: center (112,28), foe (116,28) 4.2 tiles, dest (118,13), anchor Nexus@[119,10] - foe marginally on the retreat path in x; thin but reconstructable). 9415845A (Zealot Hell flood loss): pursuer-behind FLEE f9486-9579 (center (112,36)->(114,32) dest (117,27), foe (113,37)->(114,34) contact south of center, gdist grows 36->97, 0 deaths) + 1-frame flicker f9641 (foe (117,31) 3.0 on the FLEE-dest line, back to FIGHT f9672, 0 deaths) - no blocking instance (foe never between cluster and home; nearest miss on record). 726D7D69 (Steamhammer Zerg loss): 12 FLEE episodes, 37 foe-known rows; contact-bearing candidates adjudicated - f7068 (foe (118,29) 0.5 adj south of center (118,28), dest (117,28) west, pursuer-behind, dest opposite) and f8897-9114 standoff (center static (113,21) base edge, dest (113,25), foe 1.7 receding to 18.6, 0 deaths) - no blocking instance, no fight-through needed. 170306 reconstructs fully (182/182 foe-known CLUSTER, FLEE 8-row single episode f8649-8866, centers (57,101)->(46,80) toward dest (33,53), foe tiles east/southeast throughout, dest opposite) - non-blocking (13th reconstructed non-instance); the old 'no reconstructable geometry' clause is superseded as of this game (geometry reconstructs; instances still unseen). Re-verify 2026-10-26: HoldTwinTest 5/5 green, still no post-split game - unstick decision still pending live rows. Sweep 2026-10-26b confirms no new game. Code audit: Stardust rule + latch bypass (ClusterTactic.java:57-64), BlockingArmy MIN_BLOCKING_ENEMIES count (:38-49) - mechanism as filed. (b) stays open live. Same game, MOVE_FAILED shape: zealots #156/#157 died in place after 373/197 refused slot moves (HOLD 376/202, little shooting); #148 died shooting (473 fired, HOLD 492 + MOVE_FAILED 492) - last-stand gun, not a freeze. Root-caused 2026-10-26 (code-read `AUnitOrders.move` + `BwapiOrderSink.issue`): all LOUD refusal paths (null/unwalkable, ordersink exception) log with stack traces and fired zero times in that game - the 373x refusals are quiet BWAPI-level `u().move()` false. Follow-up, no behavior: name the BWAPI refusal reason in-twin first (canCommand/canMove at refuse time), then decide on unstick; sim-pin the twin, never blind. Twin half LANDED 2026-10-24 (HOLD rows carry `canCmd`/`canMove` on every verdict - refusers plus the commandable-holder control group; `AUnit.canCommand()/canMove()` guarded readers + stub overrides, pinned 2/2 `HoldTwinTest`; zero behavior change, FormationHold untouched) - verified against 14,603 archived HOLD rows (zero carried the state before). Unstick decision still pending live rows with the state. 726D7D69 (Steamhammer Zerg loss, post-breach jar, canCmd twin live): 0 MOVE_FAILED verdicts - 3rd straight post-breach game without the refused-slot-move shape (262B8A6A had 32255 pre-breach, pre-twin. Ledger audit 2026-10-26d: 32255 MOVE_FAILED, 0 with canCmd - exact.. Ledger audit 2026-10-26d: 32255 MOVE_FAILED, 0 with canCmd - exact.. Ledger audit 2026-10-26c: 32255 MOVE_FAILED, 0 with canCmd - exact.); nothing new to decide on, stays parked. Re-verify script: CLUSTER 298 all foe-known, FLEE 52 rows, f7068 pursuer-behind + f8897-9114 static standoff (center (113,21) static, foe 1.7-18.6 receding, 0 deaths) - no blocking.

- **#190** Cutoff swamped dragoon glued in place (owner report 2026-10-24: Dragoon #193, GAME_9415845A Zealot Hell flood - detached ~30 tiles from a FLEEing cluster with 4 zealots on it, fought in place (Attack/ClusterKite churn + stuck-no-step, 4.2 tiles over the last 400 frames) and died without ever running; 10 of the 15 dragoon deaths in that game show the same glued shape, 112 of 135 KITE rows stuck for #197). Mechanism verified in code + twins: the cutoff rule (`CutoffRetreat.threatIfCutOff` + fight-if-in-range in `RetreatFromLostFight`) has no odds check - a swamped unit that can hit back always fights, and loses the DPS race standing. The sim does NOT reproduce it (`CutoffSwampedRetreatTest`: same shape escapes cleanly, 0 stuck rows - maneuver physics fine), so this is a live-validation gap, not a decision bug: either every kite aim fails validation in the live surround, or the engine quietly refuses the moves. Twin half LANDED (zero behavior change): the KITE stuck row now names which half failed - `stuck-no-step` (no valid dest) vs new `stuck-order-refused` (+ `canCmd`/`canMove`, same readers the HOLD twin uses), pinned 2/2 `KiteRefusedOrderTest` (red-green: pre-fix rows misfiled refusals as nowhere-to-step). Next: read the next live loss - if `stuck-no-step` dominates, the fix rides a validation-gate relaxation or a stuck-while-chewed run-escalation (sim-pinned against a sim that reproduces the glue first, never blind); if `stuck-order-refused` dominates, the fix rides the #188 unstick decision with the refusal state attached. No behavior change until the twin speaks. Jar-vintage check 2026-10-26 (GAME_726D7D69, finished 12:20, twin commit 12:41): 370 KITE rows, 0 with canCmd - pre-split jar, 149 stuck-no-step rows undifferentiated as before (HOLD twin WAS live: 341/341 canCmd, still 0 MOVE_FAILED). First post-split game RAN 2026-10-26 (170306, VICTORY 2/0, horizon-50 f9240): twin discriminates as designed - 78/78 KITE stuck-no-step, canCmd+canMove all true, dragoons #167/#169, f8940-9009 window, 0 deaths. Stuck-no-step dominates 78-0: validation side, not engine refusal. Instance benign (70f burst, no glue, no death) - NO behavior change; the sim must reproduce stuck-while-chewed->death first (HIT rows confirm chewing during the stuck window: #169 taking hits f8971-9011 while aims fail validation and engine stays commandable); fix stays parked. Re-verify script 726D7D69: KITE 370 (stepped 191 / stuck-no-step 149 / frozen-reset 17 / long-deflect 13), canCmd 0 rows (pre-split jar); HOLD 341/341 canCmd, 0 MOVE_FAILED - sim-vs-live gap stands. Re-verify 2026-10-26: still no post-split game (newest REPLAYS 12:20 < twin 12:41, newest out/wine_runs 10:40); KiteRefusedOrderTest 2/2 green - twin waits, no behavior. Sweep 2026-10-26b: still no post-split game (REPLAYS 12:20, wine_runs 10:40). Code audit: threatIfCutOff no-odds (CutoffRetreat.java:33-45), fight-if-can-hit-back falls through (RetreatFromLostFight.java:62+) - glue is live-validation gap, as filed.

## Current game state (updated 2026-10-21)

   Snapshot 2026-10-21 → _AI/WINE-MACRO-DATUMS.md §10 (frozen reference;
   newer macro evidence lives in the §1–§9 chains).
