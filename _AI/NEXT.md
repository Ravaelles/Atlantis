# TODO — Atlantis (open issues)

Living backlog for the architecture migration defined in `_AI/REVIEW.md` §16
and for defects found along the way. This file is the single source of truth
for "what is left"; `_AI/REVIEW.md` keeps the *stage* narrative and
`_AI/NOTES.md` keeps operational learnings.

## Current owner priorities (2026-10-09)

1. **OpenBW simulations must be fast and self-terminating** - CONVENTIONS §17:
   `TIMEOUT_SECONDS=120` wall-clock and `INGAME_TIME=20` game minutes, both set
   once at the top of `scripts/run-openbw-e2e.sh` (done, commit `d90a4ea2`).
   Every future run mode gets the same pair.
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

1. **Pylon closure:** Wine re-run with the current jar (convertible
   waiters + walking-builder shield are in it) for the first-Pylon
   proof (#84); corner-stall game for doctrine-vs-leash (#81).
2. **Per-gate legacy deletions**, one game of proof each (ratchet):
   no candidate yet - feeder fired once ever (`143903`), legacy still
   does ~everything, and board-invisible production (Forge, cannons)
   is capability, not duplication. Deletions wait for routine feeder
   coverage, never batch rewrites.
3. **Game-proof backlog** (all need a quiet run, OpenBW or flagged
   Wine): Core-cancel retry (#88), 700 s+ rich run (#71), research
   completion (#72), L2/L3 (#85), builder-variance leftovers (#74).
4. **Second goal vocabulary** (Terran or midgame): triggers the
   `goals/` split by vocabulary - until then no new packages.
5. **Decision absorption:** board takes over decision sites one by one
   (51 files importing Decision, 49 in prod, measured 2026-10-15, counting
   down, no new ones per §21); the class dies at zero, not before.
6. Unchanged long-term ordering: #45 deletion order, #8 V2 LIVE runs,
   stages E–J below.

## Test health

Suite green 2026-10-15: 575 passing, 1 skipped (+2
`UnworkedConstructionSurvivesNearbyBuildingTest`);
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
  core/assimilator; workers/army/expansion/detection stay with their
  owners). No new arbitration gate in legacy producers, ever (ratchet).
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
  crash, no exceptions. Two watches: V2 rows carry minSupply 0 (no BO
  expectation reaches the ledger - the deferred timeline scheduler);
  Pylon #1 warped at sup5 (pull-forward or waste - undecided, needs a
  side-by-side). Forge divergence still tracked, not regressed.

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

## Review leftovers

## Needs a game (quiet OpenBW run or flagged Wine run)

- **#71** Readiness gate flipped (was blocking rich, name said poor):
  stub inversion pin 2/2. Needs a 700 s+ game; the named
  `ORDER_SAVING_FOR_EXPANSION` line is the signature to look for
  (20261014_172954 reached only 324 s; `20261015_185046` reached 575 s
  in 141 s wall - heavy economies sim ~4x, so 700 s needs a lighter
  game or more wall).
- **#72** Singularity Charge fires in stub (2 Dragoons + 250 gas), idle
  live for lack of a second living Dragoon (not a divergence, not a
  stuck flag - measured). Needs a gas-healthy long game proving research
  completes (150/150 held to issue). Live precondition since seen:
  `SINGULARITY_APPLIES` firing with 2 Dragoons and gas 180-196
  (`20261015_150026`) - completion still unproven, run ended 8:43.
- **#81** Corner-stall Pylon search (top-right base [119,10]): 1953
  refusals, grid-modulo + doctrine kills (a mislabel fix already
  landed, zero behaviour). Needs the corner-stall game to decide
  doctrine vs leash-relax (owner call, same class as the zero-supply
  yield).
- **#84** Wine Pylon ordered but unbuilt: late-game instance (5:01 +
  38 s, bank hoarded) and early-game instance (READY from ~100 s, 9/9
  + 400 bank at log end). Worker stall, placement refusal, null
  builder and cancels all ruled out from flag-off logs. Needs a
  `BUILDER_LOG=1` Wine game (builder? site? no attempt?).
- **#88** Core never re-requested after a cancel (152437): stub
  contract holds (`CoreRequeueAfterCancelTest`), live path unmeasured.
  Needs a `BUILDER_LOG=1` game catching a Core cancel. No blind patch.
- **#74** Wine builder variance remainder (requeue/distances/debounce
  halves verified): needs a flagged Wine game for alternating
  Builder/Gather on a site and the exact 1.4-tile parking case.
- **#85** Armor unchained from weapons-L3 to L1 (stub pin); flat rich
  gates (~2x above L2/L3 engine costs) untouched (D5-adjacent tuning).
  Needs a gas-healthy game proving L2/L3.
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
  8:28-8:51 game-time (`20261015_132452`).
- Shutdown-hook summary proven `20261015_181553`: a wall-killed short
  run now ends with `### Stopped (no game end reached) after 302
  seconds. ###` + roster instead of mid-frame silence.
- Wine first Pylon still unbuilt in both instances (see #84); the
  convertible-waiters fix plus walking-builder shield are in the jar,
  final proof is the owner's re-run.
