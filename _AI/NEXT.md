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
2. **Finish Production V2** - `_AI/STATUS.md` and `_AI/redesign/01_PRODUCTION.md`.
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

## Test health

The suite is green (332 passing, 2 known-failing, 4 skipped as of 2026-10-08;
re-run `bash scripts/run-tests.sh` for the current numbers - do not trust a count
written here). What is open is *quality* of the data behind it, not a red test.

- **#29 CLOSED** - unit/weapon data: the engine (the vendored jar) is the source.
  An intermediate table had invented ~20 numbers and the suite was green against
  them. Now: `UnitStats` seam, an **empty** correction table, and
  `UnitStatsTableTest` pinning the engine's values. Full story in `NOTES.md`.
- **#28 CLOSED** - production code no longer imports the test harness (five
  `Source` ports, `src/starengine` deleted, the jar drops `tests/**` and fails the
  build if a packaged class names it). Detail in `NOTES.md` and git history.

## Scenario E2E (stub tier - runs today)

- **#34** Real opponents for the stub-tier scenarios. The two twins hold in
  `tests.e2e` (`FourPoolDefenseTest` 6 lings, `NinePoolDefenseTest` 8 lings) with
their baselines pinned; what is missing is a **real opponent**: OpenBW hosts one
client, so a second bot cannot attach, and a scripted rusher needs a UMS map
that does not exist here. The scbw tier has real verdicts in `_AI/e2e/` and is
owner-run. Resume when a scripted UMS map is supplied or the opponent runner is
agreed. Detail: `_AI/IDEA-E2E-TESTS.md` §3.4-§3.5.

## Stage E — read model (remaining)

- **#3** Migrate production readers of `FoggedUnit` to `UnitSnapshot`.
  Candidate sites inventoried (24 files reference fogged types; most are
  lifecycle, not reading). Verdicts:
  - MIGRATE (need the last-known-vs-current distinction, real payoff):
    `ProcessAttackUnit:56` (attack a last-known position without fog
    awareness), `BuilderAvoidEnemies:27` (avoid a position), `DeadMan:25`
    (a fogged unit cannot be a dead man - exactly what `hpKnown=false`
    says), `AFocusPoint:111` (focus on a last-known position),
    `DoAvoidEnemies:30` (filter on position-known, selection-level),
    `AtlantisJfap.isValidUnit:71` (validity gate could be "has a position
    snapshot" - borderline, decide at migration time).
  - REJECTED (same object, same values, worse GC): `AUnitOrders:233,261`
    (order issuance needs the target entity itself), `Selection:223,296`
    and `Units.addFoggedUnits` (live-collection plumbing),
    `PositionUtil:50` (pure position delegation),
    `MissionAttackFocusPoint:125` (focus needs the entity identity),
    `AAdvancedPainter` (debug reads everything anyway),
    `OffensiveTurrets:41` (commented out).
  - NOT #3 (lifecycle - dies with the World registry, Stage E core):
    `AUnit:173-197` constructors, `UnitsArchive:141`, `EnemyUnits*`,
    `NeutralUnits`, `OnUnitMorph:31`.
  Migrate the first group when the World registry owns lifecycle; until
  then a delegation switch has no value. This item closes with those
  migrations, not before.

## Stage F — cache purge

The full inventory is `DOCS/SELECT-CACHES.md` (46 entries, key → TTL → readers),
generated rather than remembered. Three concrete starting points came out of it;
the first is already done. The two behaviour-neutral ones have an executable
procedure: `_AI/work-orders/WO-F-neutral-cache-cleanup.md` (the query service
itself, #5, is design work and out of that order's scope).
- Both behaviour-neutral items are done (WO-F): `microCacheForFrames` is gone -
  the 24 call sites say `0`, which is what the enemy-side queries have always
  used - and `Select.clearCache()` clears `cacheObject`, so
  `mainOrAnyBuildingPosition` no longer survives a unit-created event on a
  73-frame TTL.
- Six TTLs above one frame (30, 31, 53, 73, 91, 293) have no stated reason
  anywhere. `Select.main()` - 90 call sites, the most-read method in the tree -
  is cached for 2.4 s.

- **#5** Introduce a per-frame query service in `core.world` owning the
  caches, with explicit invalidation per frame instead of TTL guesses. Move
  one cache at a time, starting with the one with the fewest readers.
- **#6** Delete the migrated static caches from `Select` (no compat shims —
  every call site rewired in the same commit) and re-freeze the ArchUnit
  store only if violations genuinely disappeared rather than moved.

## Stage G — race strategies

- **#8** Implement the seam and migrate one subsystem end-to-end as proof
  (candidate: production), measuring that the branching-site count drops
  without behaviour change.

## Stage H — god-class split

- **#9** Split `A` (163 public static methods) into cohesive collaborators.
  Progress: 163 → 43 public static methods, 1688 → 414 lines. Extracted so
  far: `AFile` (file/path I/O), `AGui` (4 popups, A lost Swing entirely),
  `AMath` (ranges/statistics), `ARandom` (stays in `atlantis.game`: it shares
  the mutable `A.random` stream), `AConsole` (console writers). Deleted: 56
  methods with zero callers, including `formatDecimalPlaces` which formatted
  the wrong argument. Each extraction also shrank the frozen ArchUnit store
  instead of growing it.
  Remaining in `A`: resource/supply facades (`supplyUsed`, `hasMinerals`,
  `canAfford*`, ~1400 call sites), clock arithmetic (`seconds`, `ago`,
  `everyNthGameFrame`, `minSec`), race predicates (`whenEnemyProtoss*` — do
  these in Stage G, not here), and the wall-clock formatters
  (`getCurrentDateInFormatYMDHHmm`, `getCurrentTimeAsString`). Decide whether
  the resource facades move to `AGame` (which already exposes minerals/gas)
  or become an `EconomyContext` port — that decision belongs with Stage G/I,
  so do not start it here.

  The wall-clock formatters were attempted as an extraction and **reverted**
  (2026-10-09): the split is trivial but the tooling spent many edits on a text
  substitution and no behaviour changed, so it is not worth another attempt
  until the edit path is reliable. `A` still owns both methods.
- **#10** Split `Selection` (235 public methods) along its existing internal
  seams; verify with a test that pins selection semantics before and after.
- **#11** Split `AUnit` (~623 methods) — start by extracting the order-emission
  surface that `AUnitOrders` already half owns, then the per-frame manager
  orchestration, then per-condition logic.

## Stage I — physical migration

- **#12** Once F–H land, define the physical package move to the bounded
  contexts (`Economy`, `Production`, `Combat`, `Intelligence`, `Map`,
  `Scouting`) per `DOCS/ARCHITECTURE-CONTEXT-MAP.md`, as a mechanical,
  separately-revertable step per context.

## Stage J — fortress

- **#13** Reach a **zero-violation** ArchUnit baseline and flip
  `ArchitectureBoundaryTest` from frozen store to hard rules (keeping the store
  only as a history of how far the ratchet got).

  **State (2026-10-04): 356 violations, down from 499.** Three rules are at 0
  (`map.scout -> combat/production`, map geometry, and one more). The rest:
  `core -> combat/production/information/...` = 267, `information -> combat/production`
  = 65, `architecture -> ...` = 24, `util -> ...` = 0 (was 73).

  **The 267 and the 24 are structural**, not a sweep: the Commander/Manager
  framework is unit-centric by design, so they belong to Stages E/H. What is left
  that is mechanical has already been taken: the util rule (73 -> 0) and most of
  information. The remaining entries in `information` are `private` methods their
  own class calls - nothing to rewire - so this item is **blocked on the Stage E/H
  design work, not on effort**.

  Two traps, both paid for:
  - A change must *remove* violations from one rule, not move them into another.
    Moving `ScoutManager` into `atlantis.units.special` compiled and passed every
    test, and only grew the core rule (267 -> 280).
  - `rm -rf out` before `scripts/run-architecture-tests.sh` after moving a class
    between packages: `javac -d` leaves the old `.class` behind, ArchUnit reads
    both, and violations "come back" for a package that no longer exists. ArchUnit
    also only prunes stale entries for the rule it re-writes, so re-freezing needs
    the store emptied by hand and the diff reviewed (a re-freeze can absorb
    violations whose text merely changed).

  The mechanical-sweep details and the per-class triage are in
  `_AI/work-orders/WO-13-mechanical-archunit-sweep.md` and in git history.
- **#35 CLOSED** (2026-10-04, by decision) - a mirror fight scored 0.88 instead of
  1.0; the asymmetry is JFAP's own per-player deltas, so it is tie handling in a
  vendored library, not our bug. Our `eval()` only matters from our own side; the
  doctrine that came out of it is the B-18 penalty. The mirror test now asserts
  reciprocity instead of comparing our side with itself.

## Review rounds

Each review of the last window gets its findings and their dispositions here, so a
later reader does not have to re-open the review to find out what was done about it.
Reviews: `_AI/REVIEW.md` (top-down, §16 stages), `_AI/REVIEW-GLM.md`
(SOLID + architecture, 2026-10-04, window `e8962bda`…`e4a2579c` plus the GameClock WIP).

### GLM 2026-10-04

| finding | disposition |
|---|---|
| F-1 two clocks, three writers, one of them bypassing the publish protocol | **done** - `A.setNow(framesNow, secondsNow)` is the only writer (game + both harnesses); `GameClockAgreementTest` (3) pins both views moving together. Measured, and worth knowing: the predicted drift does not reproduce, because the same frame is published in `onFrameEnd` before the frame body runs - it was ordering luck, not design |
| F-2 race gates written per leaf | **done** - the rule ("a commander whose subcommanders are race-scoped gates itself in `applies()`") is now in `DOCS/SOLID-CHECKLIST.md` OCP, with the two commanders that had to remember and the crash it prevents |
| F-3 `BaseUnderAttack` is all-static while everything around it becomes a `Manager` | **#36, not urgent** - it works, the scenario tier pins it, and the per-frame cost is bounded by the `enemiesNear` 5-frame cache. What it does lack is a seam: no test can force or stub the answer, and `check()` recomputes the same `Select.mainOrAnyBuilding()` query three times per frame per worker |
| F-4 `ProtossJfapTweaksConsiderChokesEtc.rawEval` static mutable state | **done, differently than suggested** - the field was written and read nowhere, so it was deleted instead of threaded through the penalty helpers. The review's direction was right and its evidence was wrong (a grep for readers is two commands) |
| F-5 "pressure" now has a second class (`BaseUnderAttack`, `ExpansionUnderPressure`) | **named, no merge** - thresholds, radius types and semantics differ, so they stay. If a third "is pressure real here" class appears, extract a shared threat assessment then, not now |
| F-6 `CancelNotStartedBases` name drift after B-23's narrowing | **dropped 2026-10-04 (owner's ruling)** - cosmetic, and the drift is contained: the class may deliberately keep the oldest pending base, the docstrings carry the nuance, and callers say "not started ones" anyway |
| F-7 `scripts/run-e2e.sh` write policy | **checked, no violation** - §8 allows read-only `~/.scbw`, the script writes into `_AI/e2e/`, and `--parse-only` matches its claim |
| F-8 `Commander.applies()` is load-bearing for correctness, not filtering | **no action, recorded** - the OR-accumulated `handle()` and the non-null-stops `Manager.handle()` are documented where declared; the next contract addition should follow the same rule |

- **#36** `BaseUnderAttack`: give it the same treatment `ExpansionUnderPressure`'s
  callers got - a per-frame cache of `check()`, or a tiny injectable seam so a test can
  force the answer. Cheapest honest version first (the cached `check()`), because the
  static-to-`Manager` conversion is a bigger change than the three duplicated queries
  it would remove. Not urgent: nothing is wrong with the behaviour today.
- **#38 CLOSED 2026-10-11: `atlantis.debug.object` removed, with three of its four
  libraries.** The owner's ruling was "if nothing plans to use it it is probably a dead
  idea", and the audit confirmed it twice over - `OnEveryFrameHelper.handle()` opened
  with `if (true) return;` **and** the single call into the subsystem,
  `serializeMapDataLikeRegionsToAFile()`, opened with the same. Both had to be true for
  the code to be reachable, so neither could run.

  Deleted: `src/atlantis/debug/object/**` (9 classes, 510 lines),
  `tests/unit/object/ObjectToFileTest` (the suite's only `@Disabled` tests),
  `serializeMapDataLikeRegionsToAFile`, and `kryo`, `reflectasm`, `minlog` from `lib/`
  and `RUNTIME_LIBS`. **Measured jar effect: 10.83 MB -> 10.34 MB.**

  **`lib/objenesis-3.2.jar` deliberately stays**: Mockito needs it reflectively for
  tests (verified - one reference in its jar, and the suite is green with it present). It
  is only no longer *packaged into the bot jar*; removing it from the project would break
  the test toolchain, which is a different question from shipping dead code to a game.

  A grep for the four packages found no other user anywhere in `src/`, and the fast suite
  is green with the subsystem gone (405/405). `BUGS.md` B-25 can be closed with this -
  the measurement there (6.1 MB fat, 3610 entries, four libraries shipped for unreachable
  code) is what this resolves.
- **#39** The Protoss cannon producers are commented out in the buildings commander
  (`// || ProduceCannon.produce()`, `// || ProduceCannonAtNatural.produce()`), disabled
  2024-10-02 (`49a4536a`) and 2025-01-09 (`9a34add6`, `75d7650f`) with no recorded
  reason, and `ProduceCannon` no longer exists in the tree. The two live cannon paths are
  `ProtossSecureBasesCommander` (needs `Have.forge()` + two bases) and
  `ProtossResponseEnemyHiddenUnits` -> `ProduceCannonAtNaturalOrMain`. **Decision, not a
  chore:** re-enable (which mechanism should own "the base is being attacked", given two
  exist), or delete the dead classes, or leave it and own the consequence in a comment at
  the call site. `BUGS.md` B-19 carries the measurements - four games, zero cannons - and
  the chain that leads there.
- **#40** `ConstructionThatLooksBugged.handleConstructionThatLooksBugged()` has an
  unreachable branch: the method returns unless `status() == NOT_STARTED`, and the inner
  guard is `if (constr.status() != NOT_STARTED) constr.assignOptimalBuilder();`, so the
  assignment never runs. The effect is that a planned construction with no builder is
  *cancelled* ("Weird case, ... has no builder. Cancel.") rather than given one.
  Builders are assigned at creation (`NewConstructionRequest` /
  `DefineConstructionForNewUnit`), which is why it is invisible - and why it is recorded
  rather than changed. Worth a test that says which of the two behaviours is intended
  before anyone touches it: a construction whose builder is assigned a frame later would
  be cancelled by the safety net.

- **#42** First assimilator never ordered despite a finished Cybernetics Core
  (`GAME_29A29B04`: 0 gas mined all game, dragoons impossible from frame one, book #9
  at supply 22 never fired and neither did the dynamic path). Suspects, in order:
  `Strategy.isExpansion() && supply <= 44` (supply peaked at 40), the buildings
  commander's `applies()` (minerals 220...), the core-timing gate. **Diagnostics
  first:** name the blocking gate in the log with the same reason-pattern the producers
  got in B-22, then decide. No gating change blind - the last three "production
  stopped" reports were three different root causes (idle-gateway reading, stale jar,
  no gas), and each looked like the others from the score line.

## Housekeeping

- **#15 PARTIAL — deployed Protoss jar freshness.** Rebuilt
  `/sc-ai/BOTS/AtlantisP/AI/Atlantis.jar` with the canonical builder; freshness
  fingerprint matches the source tree (`94e518…`) and bytecode major is 52
  (Java 8). OpenBW run `GAME_SECONDS=90 PLACEMENT=catalogue`, map
  `(3)TauCross1.1.scx`, Protoss vs Zerg: client attached, bot exited 0,
  `Total time: 843 seconds`, no exception; it still logged
  `Can't find place for Pylon` at 0:39, so this verifies the jar was played, not
  that placement works. **Terran remains blocked:**
  `/sc-ai/BOTS/AtlantisT/AI/Atlantis.jar` does not exist and the scbw opponent
  cache is owned by scbw; no approved Terran deployed target was found. Do not
  close #15 until the intended Terran artifact path is supplied or the owner
  verifies that no deployed Terran jar is required. Run artefacts:
  `out/openbw/bot.log`, `out/openbw/server.log`.

## Production v2 and the OpenBW E2E engine (2026-10-08)

- **#48 - SPLIT AND RE-MEASURED 2026-10-10: the duplicate/re-dispatch defect is FIXED;
  what remains is the sliding `startFrame` (a different bug, now #52).** Live evidence
  below; the original description is kept after it as the record.

  **Measured 2026-10-10** (`PRODUCTION_V2=LIVE PLACEMENT=catalogue`, bounded run, fresh
  jar, exit **0** not 124):
  - **No duplicate Pylon in any single plan** - the old signature
    (`plan=3 [.. Pylon@2234 Pylon@2234]`) does not occur. Plan size is 2, and the
    duplicate-goal fix in `BuildOrderGoals`/`ProductionScheduler` holds in a live game,
    not only in its unit test.
  - **The game ends itself**: `ForceExitLocallyAfterRealSeconds`, `Total time: 100
    seconds`, `Defeat`, exit 0. The "never ends, outer timeout kills it" consequence is
    gone.
  - **`engineAccepted=false` does not appear at all under V2 LIVE** - because V2 issues
    only `trained` orders here, never a build, so the library's broken
    builder-reachability check is never reached.

  **What remains, and it is a DIFFERENT defect (#52):** the Pylon's `startFrame` slides
  forward frame by frame and the item is therefore never dispatched:

  ```
  @@633 Pylon@978-1428   @@637 Pylon@992-1442
  @@937 Pylon@1129-1579  @@938 Pylon@1130-1580  @@940 Pylon@1132-1582
  ```

  Every line reads `issued: OK Probe@NNN (trained)` - the worker orders are issued, the
  Pylon is planned and re-planned with a moving start. This is the same relative-frame
  slide `_AI/STATUS.md` recorded as "planned Pylon start moved 449, 450, 451, 452", and it
  is an arbitration/livelock problem (a cheaper higher-priority item keeps the bank below
  the Pylon's cost), **not** a duplicate-dispatch problem. It is tracked separately so the
  two are not confused a third time.

  ---

  **Original report (kept as the record of what was fixed):** under
  `PRODUCTION_V2=LIVE` the plan contained DUPLICATE buildings re-dispatched every frame,
  so the game never ended and the run died on the outer timeout. Measured 2026-10-09,
  three times.

  **What the log shows** (`TIMEOUT_SECONDS=90`, in-game 3 min):
  ```
  @@3214 plan=3 [Probe@3664-3964 by#70 Pylon@2234-2684 Pylon@2234-2684 ]
       issued: OK Pylon@2234 (builder committed) OK Pylon@2234 (builder committed)
  @@3215 ... identical ...
  ```
  Two facts, both wrong:
  1. **The same Pylon tile (2234) appears TWICE in one plan** and is dispatched
     twice per frame. The goal layer asks for one Pylon, so the duplicate is
     created inside the scheduler/dispatcher path.
  2. **`startFrame` is ~1000 frames in the past** (2234 while the frame is 3214),
     so `isDue` is permanently true and the item is re-issued forever - it never
     completes and never leaves the plan.

  **Consequence:** the game never reaches `Total time`/`Defeat`, the
  `ForceExitLocallyAfterRealSeconds` line never appears, and the outer `timeout`
  kills the process (exit 124, or exit 1 with no verdict). Without LIVE the same
  command ends itself cleanly (`Total time: 188 seconds`, exit 0), so the defect is
  in the LIVE path.

  **Root cause of the duplicate, measured in the source 2026-10-09:** the active
  build order (`Zealot into Goon`) declares Pylons on **four separate rows**
  (supply 8, 13, 20, 27). `BuildOrderGoals.from` emits one goal per row whose
  supply gate is open, and its `supplyGateOpen` is `supplyUsed + lookahead >=
  minSupply` - so once supply passes 27 **all four gates are open at the same
  time** and four Pylon goals enter one pass. The scheduler then honours each as a
  separate building, and the plan logs them at the same tile. This is the
  "queue-like" behaviour the v2 design intended to remove: a build order row is a
  *sequence*, not a set of independent wants, and the goal layer lost the ordering
  between rows when it flattened them.

  **Implemented and unit-tested, live status still open:**
  - `BuildOrderGoals` emits only the earliest unsatisfied occurrence per item type;
    `ProductionScheduler` deduplicates same building type+placement across sources.
    Regression test `openingSchedulesOnePylonBeforeGatewayDespiteDuplicateSupplyGoal`.
  - Assigned builders are owned by construction: `BuilderManager` is first in
    `WorkerManager`; `BuilderManager.handle()` claims a pending assignment even
    when `TravelToConstruct` cannot issue an order that frame; worker defense and
    `WorkerHelpCombatUnitsFight` decline assigned builders. Regression forces the
    WorkerManager chain and asserts `unit.managerLogs()` contains BuilderManager,
    not WorkerHelpCombatUnitsFight/GatherResources (`WorkerDefenceTest`: 12/12).
  - `GameSummary` reports Pylon/Gateway counts, and runner assertions cover them;
    offline assertion self-test rejects a 0/0 building fixture.

  **Manual owner confirmation:** owner reports that a real StarCraft game built
  both Pylon and Gateway. This confirms the default/legacy gameplay on that run,
  but does not verify the Production V2 LIVE path. The most recent LIVE OpenBW run
  before the builder ownership change dispatched Pylon repeatedly and timed out.
  The next bounded LIVE run is blocked while the runner detects an active owner
  `StarCraft.exe`/ChaosLauncher; never terminate it. Resume after it closes and
  inspect the specific assigned worker's `managerLogs()` if construction stalls.

  **Corrected twice mid-investigation:** an earlier reading blamed a caught init
  exception (disproved - `HELLO_ATLANTIS` present, no exception), and a later one
  blamed only builder recognition (disproved by the still-looping run above).

- **#47 - OpenBW E2E must be fully working (TOP priority).** The runner attaches
  and is bounded, but an E2E test is not "fully working" until all of these hold:

  1. **Attach is now reliable - fixed 2026-10-09.** The host was killed a few
     seconds after starting and left its PID in the registry, so the client
     looped on `No server proc ID` while the run still reported exit 0. The bot
     JVM is now killed 10 s before the host, so the host outlives the client, and
     a log with no `HELLO_ATLANTIS` is an assertion failure. Verified: three
     consecutive runs attached and passed. Remaining: a run that fails for some
        other reason must still be distinguishable from a pass. **Non-zero bot exits
        now fail before scenario assertions** (a timed-out run previously printed
        `SCENARIO PASSED` with no expectations); this is pinned by
        `LauncherBuildsTheJarTest` and fast suite 346/346. A real non-zero process-exit
        integration run is still unverified.
     2. **A Pylon is placed in a real run.** NO, and the cause is measured
     2026-10-09 - but **not** what the first reading suggested. The engine is
     telling the truth: on the probed tiles `engBuild=true` and only the occupancy
     variant `engBuildInc=false` for the one tile (7,44) that is genuinely
     occupied. So `MapTiles.isBuildable` needs no fallback, and the earlier
     "engine lies" note in this item was wrong.

     Verified from the engine jar: a Pylon is **2x2** (Gateway is 4x3).
     `CanPhysicallyBuildHere` correctly requires each footprint tile to be
     buildable-including-buildings; the sampled candidate included occupied tile
     (7,44). The owner confirms a manual SC run builds Pylon and Gateway; this is
     owner evidence for the legacy path, not an OpenBW verdict. The next bounded
     OpenBW run must decide whether the legacy-path failure persists; do not
     patch `APositionFinder` without a fresh reproducer.

     **Fresh LIVE OpenBW evidence (2026-10-09):** the selected catalogue tile's
     `Unit.build(Pylon, tile)` returned false; the 2x2 footprint's four tiles were
     individually buildable in both the map grid and `Game.isBuildable(..., true)`,
     no other unit overlapped the footprint, and minerals were sufficient. Yet
     `Game.canBuildHere(tile, Pylon, builder)` was false. This is a fresh command
     rejection, but its cause is still unknown; do not add a map fallback or alter
     `APositionFinder` based on the map-only values.

     **Cause now MEASURED (2026-10-10, three live runs + two temporary probes; probes
     removed, fast suite 372/372).** The full numbers are in `_AI/CHALLENGES/OpenBW.md`
     ("The real cause, measured: the search area is the base itself"). In short:

     - The engine is not at fault. A probe of the search square found **2422
       buildable tiles** around `nearTo` with the nearest at distance 0.
     - **The first-Pylon `nearTo` is the base coordinate, 7 tiles from the left map
       edge** (`nearTo=[7,43]` on TauCross), and a Pylon at `supplyUsed <= 30`
       reserves exactly a **7-tile** boundary margin. So the search starts in a
       corner of its own base and its first rings are consumed by
       `Too close to left map boundary` (944 log lines), `Too close to main base`,
       and the base/mineral occupancy tiles. The open ground exists but sits behind
       those rings. **This is the blocker: a `nearTo`/margin-geometry problem, not
       terrain, not fog, not regions.**
     - Two earlier readings in my own notes were wrong and are withdrawn:
       invisibility (`checkExplored=false` by default; the probe found 1149
       *invisible* tiles answering `buildInc=true`) and "the search area is
       unbuildable terrain" (an artefact of a diagnostics bug - see below).
     - **Our own failure print lied about coordinates**: `PositionFulfillsAllConditions`
       set `_currentTX = position.ty()` as well as `_currentTY`, so every logged
       `(x,y)` read `x == y`. Fixed; the fields are print-only, so no behaviour changed.
       Any analysis based on that print before 2026-10-10 is invalid.

     **PLACEMENT NOW SUCCEEDS - two roots fixed, one new blocker exposed (2026-10-10).**
     Measured, live run: the log now prints **`SUCCESS FOR Pylon at 113,15`** and the
     placement failures went to **zero** (`Can't find place` was 2208+; `Region mismatch`
     549 -> 0). Nothing was patched blind - both fixes came from probes.

     **Root 1 - the search origin was the Nexus (fixed).**
     `PylonPosition.nearToPositionForFirstPylon()` computed
     `geyser.translateTilesTowards(mineralsCenter, 50)`, which on TauCross landed
     **36 tiles outside the map** (`raw=[-36,88]`); `ensureWithinBounds()` then returned
     **null**, the `if (centerOfResources != null)` guard skipped the step meant to move
     the origin off the base, and `nearTo` became the Nexus itself
     (`distBase = 0.84`). The method was rewritten to the stated intent: the base moved
     `FIRST_PYLON_TILES_FROM_BASE` (7) tiles **towards the resource line when that has
     room**, otherwise **away from the nearest edge** (a corner base points its mineral
     line at the edge its own 7-tile Pylon margin guards).

     **Root 2 - `Region mismatch` fired on every candidate (fixed).**
     `IsProbablyInAnotherRegion` used bare `position.hasPathTo(...)` = `Game.hasPath`,
     which on OpenBW is region-based and answers **false for every pair** (the server
     publishes no regions). A probe showed **all 549 rejections** were
     `directHasPathTo=false / MapTilesHasPathBetween=true` with the candidate 1-3 tiles
     from `nearTo`. It now uses `MapTiles.hasPathBetween`, which already carried the
     engine-then-map fallback `MapTiles` needed for exactly this reason and which this
     condition never used.

     **NEW BLOCKER (open, and it is #48's territory): the builder never starts.**
     With placement working, the log shows the construction is created and assigned -
     `Builder diagnostic: unit=#118 Probe @[113,15] (BuilderManager) construction=Pylon
     status=NOT_STARTED`, 1799 frames of `BuilderManager` in its history - and then
     `Cancel construction: Pylon took too long (44s) / at:[113,15] / buildable:true`.
     So the tile is good, the builder is assigned and in `BuilderManager`, and the order
     is never issued. **`Pylons=0` is now this, not placement.**

     **Do not re-open the placement hunt on account of `Pylons=0`:** placement benefits
     from `canBuildHere`, `BuildingTilesAreOccupied`, the terrain flags and visibility
     were all measured and none of them was the cause; the remaining failure is
     builder execution.

     A second, independent defect is confirmed but is not the blocker: 549 log lines
     of `Region mismatch` (`IsProbablyInAnotherRegion` asks `hasPathTo`, which is
     region-based and always false on OpenBW because the server publishes no
     regions - `OpenBW.md`). It is reached after `CanBuildHere`, so it is a
     follow-up once placement accepts tiles at all.

     If the default legacy path still refuses after the owner's latest changes,
     diagnose its candidate search from a fresh bounded run. The planned long-term
     option remains the Production V2 cut-over in `_AI/PLACEMENT-CUTOVER-PLAN.md`;
     #48 tracks the separate LIVE scheduler/dispatch blocker.
  3. A scenario file instead of loose environment variables (not implemented).
  4. A deliberately broken build fails the same scenario (not verified).
    5. The 7-minute survival scenario passes with Pylon>=1, Gateway>=1,
       `EXPECT_MIN_INGAME_SECONDS=420`, `EXPECT_MIN_KILLED=12`,
       `EXPECT_MAX_KILLED=40`, `EXPECT_MIN_RESOURCE_BALANCE=-200`, and no placement
       refusals. Still not verified on OpenBW.

  - **#49 is the single blocker that gates everything else on OpenBW.** Measured
    2026-10-10 with a fresh bounded run (bot exit 0, `ingame=251s`, and
    `MAP_ANALYSIS areas=21 chokes=42` so the map model is healthy): the only
    building the bot ever tried to place was the Pylon, and it was refused once
    (`Can't find place for \`Pylon\`, At 8 Pylon (READY_TO_PRODUCE)(#1)`) after
    which no further construction was requested at all. So nothing downstream can
    be exercised - a Cybernetics Core never gets far enough to test the power rule
    (#50), and the mineral-distance rule (#51) never gets a candidate to reject.
    **Do not read a green suite or a clean run as evidence that #50/#51 work in a
    game: they are behind this item and currently unexercised.**

  - **#49 - IMPLEMENTED 2026-10-10 (contract + wiring); it does not change the outcome
    yet, because the library still refuses the command (#49's own note, below, predicted
    this shape).** `BuildingTilesAreOccupied.check(position, type, builder)` now asks the
    rule the game enforces - the footprint may contain the assigned **builder** and
    nothing else - and the two production call sites that have a builder pass it:
    `RefreshConstructionPosition.shouldRefreshConstructionPosition` (which had
    `construction.builder()` in hand and was asking "is a unit on the tiles" without it,
    so a builder arriving at its own site looked like an obstacle) and
    `MapTiles.canBuildHere` (whose occupancy guard called a tile occupied by the builder
    that had walked there to build). `checkBuildingsOnly` keeps the old question for
    callers that genuinely ask it.

    Verified: 8 cases in `PlacementSearchMatchesCommandTest` (mutation-checked - dropping
    the builder exemption fails one), 405/405 fast suite, ArchUnit 7/7.

    **Live result, stated plainly: `Pylons=0` is unchanged**, and `construction=Pylon
    status=NOT_STARTED` is logged 2038 times. That is the expected consequence of the
    stage-4 blocker, not a failure of this fix: the order IS issued and the library
    refuses it, so the construction status cannot advance whatever occupancy says. #49 is
    correct and necessary; it is behind the same wall as everything else on this path.
    Do not read the unchanged `Pylons=0` as evidence against the rule.

  **The mechanism behind the Pylon refusal, as originally measured:**

    ```
    BI0_TILES Pylon at=[7,43]  (7,44) explored=1 visible=1 unitOnTile=2
                               (8,44) explored=1 visible=1 unitOnTile=1
    ```

    The refused footprint tiles are walked by our own mining workers. The engine is
    right to refuse them: `isBuildable(...,true)` counts **units**, not just
    buildings, and a mining worker on the footprint is a real blocker.

    **The rule (owner's ruling):** StarCraft allows a building to be placed **under
    the unit that is about to build it** - the builder is not an obstacle, because
    the engine moves it onto the site. Every **other** unit on the footprint blocks.
    When the search runs for the first time no builder is assigned yet, so then
    nothing may be on the footprint at all.

    **Why this is not a one-line fix:** the two existing answers are each wrong in a
    different direction, so neither can be adjusted into the correct one.
    - `isBuildableIncludeBuildings` counts **all** units, so it also refuses a tile
      occupied only by our own builder - a placement the game allows.
    - `BuildingTilesAreOccupied.check` counts **buildings only**, so it accepts a
      tile with a mining worker on it - a placement the game refuses.
    `_AI/POSITION-FINDER.md` point 2 explains why the guard was narrowed to
    buildings (a worker or a patch two tiles over "overlapped" a Pylon's footprint),
    and that narrowing traded a false negative for a false positive instead of
    encoding the real rule. The contract to change is `BuildingTilesAreOccupied.check
    (position, buildingType)` -> it needs to know the builder (excludable) and to
    count units, not only buildings; 6 call sites.

    **Second half, and it is why a strict rule alone is not enough:** a mineral line
    is saturated with workers and a Pylon is 2x2, so "no unit may be here" refuses
    the whole area. The answer is to **ask the blocking workers to move** -
    `MoveUnitsFromConstructionPlace` already tries (`moveAwayFrom(buildPosition, 2)`
    at `TravelToConstruct:51`), but it (a) only runs while the builder is still
    travelling (`distanceToConstruction >= 3.8` returns early, i.e. not when the
    builder is already at the site) and (b) shoves every nearby unit with no notion of
    "except the builder". Extending that to run at the site and to exclude the builder
    is the other half of this item.

    **Test to write with the fix:** a footprint with the assigned builder on it is
    placeable; the same footprint with a *mining* worker on it is not, until the
    worker is sent away. The existing test to reconcile is
    `OccupiedTileIsNeverBuildableTest.aUnitStandingOnTheTileOccupiesIt`: its NAME says
    a standing unit occupies the tile, but its BODY asserts the opposite for a worker
    (``"a worker on the tile is not an existing building - counting it refused every
    mineral-line position"``), reasoning that a worker "is handled by the engine's
    own answer and by the builder stepping aside". This measurement shows that
    reasoning is not true today: the engine refuses, and nothing steps aside. Name and
    body have to be reconciled with the rule above.

  - **#50 - The legacy planner never checks Pylon power, so a Protoss building can be
  placed outside a pylon's field.** Found from the owner's IDE game: a Cybernetics
  Core was sent to a position with **no pylon covering it**. Verified in the source -
  power is checked in exactly two places, and neither is the default path:
  - `ProtossAllowHereEarlyEvenWithoutRequirements:23` does check
    `needsPower() && !IsPoweredByAPylon.check(position)`, but it is only reached for
    buildings `AllowToProduceEarlyWithoutRequirements.isAllowedForProtoss()` admits,
    and line 29 restricts the "early" branch to gateways;
  - `placement/core/PsiGating` gates power in the **rewritten** planner, which only
    runs with `PLACEMENT=catalogue`.
  `PositionFulfillsAllConditions` - the condition chain the default
  `APositionFinder` uses - does **not** check power at all, and `FindPosition:136`
  has the check **commented out**.
  So with the default planner a power-needing building is placed wherever the other
  conditions allow. This is the same gap `_AI/POSITION-FINDER.md` point 4 records
  from the other end ("`Game.hasPowerPrecise` is not usable... Power is now computed
  from our own Pylons at the engine's 6-tile radius") - the fix landed in the new
  planner and never in the old one.
  Fix: add the power condition to the shared chain (or to `ProtossPositionFinder`)
  so both planners agree, then a test that a Cybernetics Core is refused on a tile
  no finished pylon covers.

- **#51 - Protoss buildings are allowed 2.8 tiles from minerals, which reads as "on
  top of the mineral line".** Owner's report, same IDE game: the position was
  otherwise fine but sat about two tiles from the minerals. The number is
  `TooCloseToMineralsOrGeyser.minDistToMineral`: for Protoss it returns **2.8** by
  default, and only rises with supply (5.2 at `supplyTotal >= 40` under some
  conditions, 6 at `>= 60`). A building 2.8 tiles from a mineral patch crowds the
  mining workers' path.
  Decision needed (owner's call, it is a gameplay constant): raise the Protoss
  default, e.g. to ~3.5-4, or key it off the building's own footprint plus a worker
  gap rather than a flat radius. `minDistToGeyser` is the parallel value (5.1 for
  Pylon/Gateway/Cannon, 4.1 otherwise) and should be re-checked in the same pass.

- **#52 - Production V2: a planned building's `startFrame` slides forward every frame,
  so the item is never dispatched. Split out of #48 (2026-10-10), which fixed the
  duplicate/re-dispatch half.**

  Measured under `PRODUCTION_V2=LIVE PLACEMENT=catalogue`: the plan is correct (one
  Pylon, no duplicate), the game ends itself (exit 0), every worker order is issued -
  and the building never is, because its window keeps moving:

  ```
  @@937 plan=2 [Probe@940-1240 by#70 Pylon@1129-1579 ] issued: OK Probe@940 (trained)
  @@938 plan=2 [Probe@940-1240 by#70 Pylon@1130-1580 ] issued: OK Probe@940 (trained)
  @@940 plan=2 [Probe@940-1240 by#70 Pylon@1132-1582 ] issued: OK Probe@940 (trained)
  @@941 plan=2 [Probe@941-1241 by#70 Pylon@1125-1575 ] issued: OK Probe@941 (trained)
  ```

  `startFrame` moves +1 per frame while the frame under it also advances, so the item is
  due "16 frames from now" forever. This is the same relative-frame slide
  `_AI/STATUS.md` recorded ("planned Pylon start moved 449, 450, 451, 452"), and it is an
  **arbitration** problem: the Probe (50 minerals, higher priority, `PRIORITY_WORKERS`)
  is scheduled first every pass and keeps the bank below the Pylon's 100, so the
  Pylon's earliest-affordable frame is always just out of reach.

  **Why this is now the top V2 blocker:** with #48's duplicate defect fixed, this is what
  stands between V2 LIVE and a built Pylon - and it is also why the stage-4 command-path
  question is currently moot under V2 (nothing is ever dispatched to be refused).

  **Mechanism now MEASURED and confirmed in the production code (2026-10-10).** It is an
  ordering problem, and it reproduces deterministically without a game
  (`tests.unit.TimelineSlideTest`, 5 cases, mutation-checked):

  - `DynamicGoals:221` gives the worker goal **`PRIORITY_WORKERS = 20`**;
  - `BuildOrderGoals:91` gives a build-order Pylon line **`PRIORITY_DEPOTS = 30`**;
  - the scheduler sorts by priority, so the Probe is planned FIRST every pass, and
    `ResourceTimeline.allocate` deducts its 50 minerals from the chosen frame **and the
    whole tail** (deliberately - reserved money is gone for the rest of the plan);
  - `findEarliestAffordableFrame` requires the cost covered across that tail, so the
    Pylon's earliest affordable frame moves later with each pass. Rebuilt from scratch
    every frame, it moves every frame: the slide.

  **The measurement that had to be made first, and it corrected the framing:** the slide
  needs **income**. With a flat bank the expensive item is not "late", it is unplannable
  (`-1`) - a different defect. The first draft of the test used a flat bank and proved
  nothing; the corrected one gives each pass the same bank *plus mining income*, so the
  pass-to-pass movement is the scheduler's doing and not the fixture's. Both cases are
  pinned now so a future fix is not aimed at the wrong one.

  **Not patched, deliberately.** `_AI/STATUS.md` records two attempts that were already
  reverted (subtracting `ExistingItems.availableFrom` suppressed worker production;
  raising only the dynamic supply goal did not address a build-order Pylon). The
  mechanism above suggests why both failed: neither changes the **priority relationship**
  between the worker goal and the build-order Pylon, which is what decides the order of
  allocation. A fix has to answer "who gets the reserves first when both are wanted",
  which is a policy decision and is the owner's call - not a timeline tweak.

  **FIXED and verified in a live run (2026-10-10).** The owner's ruling: the default
  opening is "Pylon at supply 8", so a build-order supply provider at low supply is the
  precondition for everything and is planned before the workers.

  - New band `ProductionGoal.PRIORITY_OPENING_SUPPLY` (15, ahead of
    `PRIORITY_WORKERS` = 20). It is deliberately narrow: only a **supply provider whose
    row gate is at or below `OPENING_SUPPLY_THRESHOLD` (12)** gets it. Checked against
    `bwapi-data/AI/build_orders/Protoss/*.txt`: the first Pylon is at supply **8** in every
    file, the second at **15-16** - so 12 covers the real opening and excludes the first
    top-up. (A threshold of 16 was tried first; the test caught that it silently promoted
    the supply-16 depot too, which is exactly what the rule must not do.)
  - `BuildOrderGoals.priorityFor(row, line, supplyUsed)` decides from **what the row is**
    (a supply provider, at a gate that means "opening") rather than from its line number
    alone. The old `priorityForLine` gave "line < 4" a depot band, so the same Pylon on
    line 5 landed in a lower band - a rule nobody chose. Line order survives only as the
    fallback for everything else.

  **Live evidence:** `@@914 plan=2 [Pylon@914-1364 Probe@...] issued: OK Pylon@914
  (builder committed)` - the Pylon now **leaves the plan as a dispatched order**, and its
  start is stable at 914 across frames 915-920 instead of sliding +1 per frame. 2087
  `issued: OK Pylon` lines in the run, against 7 for the Probe.

  **What is left is NOT #52** - it is the stage-4 library refusal, now reached for the
  first time: `unit=#72 Probe @[120,14] construction=Pylon status=NOT_STARTED
  pos=[120,14]` - the builder is standing exactly on the site and the construction stays
  `NOT_STARTED` because `Unit.build(...)` returns false (the region-based `hasPath` inside
  the library's `canBuild`, `_AI/PLAN-STAGE4-COMMAND-PATH.md`). So `Pylons=0` in the end
  state is stage 4, not a priority or scheduling problem.

  **Three defects have now been peeled off this one symptom, in order:** the duplicate
  goal/dispatch (#48, fixed), the sliding start frame (#52, fixed above), and the library's
  command refusal (stage 4, open). None of them was the one the previous note suspected.

- **#43** OpenBW headless run - **resolved 2026-10-08.** The client attaches and
the bot plays: `HELLO_ATLANTIS`, map analysed, build order loaded, missions
running. The one missing setting was `BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON` on
the host; the three bot-side startup defects this item listed (unreachable build
order, strategy file-name mismatch, `AtlantisRaceConfig.validate()` exiting at
startup) are fixed. Status and the remaining blockers live in
`_AI/PLAN-OPENBW.md` §9 and `_AI/IDEA-E2E-TESTS.md` §3 - placement is the one
that still stops a real game. Do not re-investigate the attach.

- **#44** ~~Production v2: cutover and legacy deletion are ready to do.~~
  **Superseded by #45** (measured afterwards): only `Queue/**` is a candidate, and
  only after #45's ordering. Do not act on the older wording.

- **#45** Production v2: **`Construction/**` cannot be deleted yet, only `Queue/**`
  is a candidate.** Measured dependency count: **149 production files** reference
  `Queue.get()`, `CurrentQueue`, `CountInQueue`, `AddToQueue` or
  `QueueInitializer` ("who uses this" grep, 2026-10-08). They are the whole
  `production/dynamic/**` tree (`DynamicProductionCommander`,
  `AutoProduceWorkersCommander`, `SupplyCommander`, every
  `ProduceXxx`/`ResearchXxx`), `production/requests/**`
  (`DynamicBuildingCommander`, `RemoveExcessiveOrders`, ...), `ConstructionsCommander`
  and the game listeners. In `PRODUCTION_V2=LIVE` the commander tree drops
  `DynamicProductionCommander` + `SupplyCommander`, but **`ConstructionsCommander`
  stays by design** (v2 delegates builder execution to it), so the `Construction`
  subsystem is live, not dead.

  Ordering that is actually safe (each step ends green, fast suite + ArchUnit):
  1. keep LIVE as the default policy and verify it in a real game (#43);
  2. delete the **dynamic commanders** (`production/dynamic/**`,
     `production/requests/DynamicBuildingCommander`, `RemoveExcessiveOrders`)
     only after v2 covers their behaviour (tech, race-specific army - still
     open, see `_AI/STATUS.md` "what is still open");
  3. delete `Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`,
     `ReservedResources`/`OrderReservations` last, when (2) is done and
     `Construction` no longer needs a `ProductionOrder` back-reference;
  4. keep `Construction/**` until v2 has its own builder (`GameOrderDirector`
     currently *constructs* `Construction` objects to hand execution over).

  **Do not** delete from the bottom of this list upward: step 3 before step 2 is
  a bot that cannot research or build race-specific units.

- **#46** Three Protoss strategies declare a build order that does not exist:
  `3 Gate`, `12 Nexus`, `Carrier Push` (found by `tests/unit/StrategyBuildOrderTest`,
  which compares the names set in `ProtossStrategies.initialize()` against the files
  in `bwapi-data/AI/build_orders/Protoss/`). Missing in both the repo and
  `/sc-ai/BOTS/AtlantisP/AI/build_orders`, so not a version skew. Choosing one of
  them starts a game with **no build order at all**: the bot mines and does nothing,
  no error - the failure the OpenBW run showed.
  Fix options, cheapest first: rename the entries to an existing file, add the
  missing `.txt`, or delete the unreachable entries. **Needs the owner - game
  policy, not a mechanical fix.** The design flaw behind it is fixed:
  `ProtossStrategies.initialize()` now runs before `StrategyChooser` selects, since
  a chosen strategy loads its order while being selected.

### OpenBW headless run (2026-10-08)

The client attaches and the bot plays - see `_AI/PLAN-OPENBW.md` §9 for the
command, the log and the one setting that made it work
(`BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON`).

Three silent startup defects had to be fixed to get the bot playing; each looked
like "the bot mines and does nothing", with no error:

1. **Startup order**: `ProtossStrategies.initialize()` renames strategies to their
   build-order file names, but `StrategyChooser` picked one BEFORE that, so the
   chosen strategy looked for `PROTOSS_Zealot_into_Goon.txt`. Fixed by
   initialising the strategies first. Test: `StrategyBuildOrderTest`.
2. **`BWAPI_DATA_PATH` was never set for the OpenBW bot directory** (the Wine bot
   directory carries one in its `bwapi-data/AI/ENV`; ours did not). The E2E script
   now writes it explicitly.
3. **`AFile.loadFile` ate the last character of any value ending in the delimiter**
   (`.../bwapi-data/` arrived as `.../bwapi-data`). Test: `AFileLoadFileTest`.
   `AtlantisRaceConfig.validate()` also called `System.exit(-1)` at game start when
   the race was unknown; it now reports and skips.

**Placement is the remaining blocker** for a real game: see #46 and
`_AI/POSITION-FINDER.md`.

**Superseded, and to be solved by a rewrite (2026-10-08):** the owner's decision
is that this subsystem is deleted and written from scratch, so the findings from
one more session of debugging it live on OpenBW are recorded in
**`_AI/POSITION-FINDER.md`** instead of as patches. That file has the measured
facts (the engine calls the refused tiles valid and empty; the refusal came from
our own occupancy predicate, which disagreed with the engine; JBWEB is unusable
on OpenBW in two independent ways; the standard finder was never even reached)
and the traps the rewrite must avoid. Read it before touching any of this.
