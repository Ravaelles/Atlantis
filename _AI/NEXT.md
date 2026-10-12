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
  = 64 (65 on 2026-10-04; -1 on 2026-10-13: dead `notEnoughZealots()` branch in
  `DragoonInsteadZealotVsZerg`, WO-13 cycle 1), `architecture -> ...` = 24,
  `util -> ...` = 0 (was 73).

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

- **#42 CLOSED 2026-10-11 (by execution, without a dedicated fix) - the first Assimilator is
  built.** Measured on a 251-second OpenBW run:
  `### Protoss: Nexus=1 Probe=21 Pylon=4 Gateway=2 Zealot=6 CyberneticsC=1 Assimilator=1
  (1 lost) ###`. The item had tracked "0 gas mined all game, dragoons impossible from frame
  one" and suspected the `Strategy.isExpansion() && supply <= 44` gate or the buildings
  commander's `applies()`. **Neither was the cause**: the bot simply never got that far,
  because the construction pipeline could not match a finished building to its construction
  and deadlocked its workers before the tech tree was reached. Once that was fixed (see the
  `DefineConstructionForNewUnit` note under #49) the assimilator appeared on its own, with no
  change to any gate. The diagnostics added for this item (reason labels on producer
  rejections) stay - they are what a future "production stopped" report will be read from.

  **Superseded description, kept as the record of the hunt:** First assimilator never ordered
  despite a finished Cybernetics Core
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

- **#47 CLOSED** - the OpenBW E2E loop works end to end: the runner attaches, is bounded, self-terminates
  and asserts a scenario. The long history of the attach/placement blockers is in `_AI/CASE-STUDIES.md` (#47),
  and the tooling that replaced it (two tiers, explicit OK/FAIL report, durable logs) is `scripts/run-openbw-e2e.sh`.

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

- **#53 CLOSED 2026-10-11** - the construction finish path (`ProtossWarping`), verified by a bounded run
  (`Pylon=2 Gateway=1`; `CANNOT define construction` 0). Full mechanism and the withdrawn readings:
  `_AI/CASE-STUDIES.md` (#53). Still open: the duplicate-construction leak, now #57.

- **#54 - `RefreshConstructionPosition` can cancel a construction on a tile it calls
  `buildable:true`, in a `FOUND PLACE` -> `WTF?!? Cancel and request again` loop.**
  Owner's IDE game, 2026-10-11:

  ```
  FOUND PLACE FOR Gateway at 118,37
  FOUND PLACE FOR Pylon at 101,30
  FOUND PLACE FOR Gateway at 118,37      <- same two tiles, alternating
  3:21 Refresh CyberneticsC position
  FOUND PLACE FOR CyberneticsC at 118,37
  3:21 WTF?!? Cancel and request again.
  Cancel construction: CyberneticsC / position still not good after refresh
    / at:[118,37] / buildable:true        <- the tile is called GOOD and refused anyway
  ```

  **What the log proves:** the finder returns the SAME tile, the refresh asks again, the
  second `shouldRefreshConstructionPosition` call still answers "refresh needed", and the
  construction is cancelled and re-requested - landing on the same tile, forever. The
  tile's own `buildable` reads true while this happens, so the deciding predicate is not
  the engine's buildability.

  **Why it is two calls to the same predicate, not one:** `handleRefreshingPositionIfNeeded`
  checks `shouldRefreshConstructionPosition`, refreshes, then checks it AGAIN and cancels
  when it still says yes. Anything the refresh cannot fix - a predicate that depends on
  something other than the tile - therefore cancels by construction. The board says the
  position is fine; the guard disagrees; nothing between them can reconcile it.

  **REPRODUCED ON OpenBW 2026-10-11, and it is now THE blocker for the first Dragoon.** The
  earlier "`FOUND PLACE` = 0, needs the IDE path" reading was wrong - it is fully reproducible
  on the headless engine, measured in `/sc-ai/BOTS/AtlantisOpenBW/E2E_LOGS/*/bot.log`:

  ```
  5:58 Refresh CyberneticsC position
  5:58 WTF?!? Cancel and request again.
  This got re-requested: null
  Re-re-requesting: At 12 CyberneticsC* (#526) @ [17,40]
  ```

  **Quantified:** 46 cancel/re-request cycles in one 30s run (50 in the next), **every single
  one at the same tile `[17,40]`**. The Cybernetics Core therefore never finishes:
  `construction=CyberneticsC status=IN_PROGRESS` with a Probe standing on it for the whole
  game, `CyberneticsCore=0` in the summary, no Dragoon, and the `--short` tier's success gate
  can never fire. This is the concrete production defect between the bot and its first tech
  unit - not the placement search, not the builder, not supply.

  **The two-call shape is the mechanism, and it is now measured rather than inferred:**
  `handleRefreshingPositionIfNeeded` asks `shouldRefreshConstructionPosition`, refreshes, then
  asks the SAME predicate again and cancels when it still says yes. `refreshPosition` cannot
  fix this case because the predicate depends on something other than the tile (the docs say
  the tile reads `buildable:true` in the same log line), so the second call is guaranteed to
  fail and the cancel is structural. A predicate used to ACCEPT must be the one used to
  REJECT, or the gap is invisible until a game trips it.

  **Next measurement (cheap, and it decides the fix):** instrument
  `shouldRefreshConstructionPosition` to print WHICH of its two branches said yes on the
  second call - `isBuildableIncludeBuildings()` false, or the `everyNthGameFrame(23) &&
  isPositionVisible() && BuildingTilesAreOccupied.check(...)` branch. The two branches need
  opposite fixes (the first is a tile/engine disagreement, the second an occupancy false
  positive), and the log line does not distinguish them yet. Do not patch either branch
  before that probe.

  **PARTIALLY FIXED 2026-10-11 - `WTF?!?` is now 0, but the tile is still re-requested.**
  `TravelToConstruct` was comparing the distance to `positionToBuildCenter()` (offset by the
  building's tile dimensions, ~1.8 tiles for a 4x3 Gateway) against the 1.4-tile "close
  enough to build" threshold, so `shouldMoveToConstruct` stayed true with the worker parked on
  its own tile and `IssueBuildOrder` was never reached. Measured 0 `WTF?!?` cycles afterwards
  (was 46-50) and `GatewayBuildOrderTest` passes. **The re-request itself still happens:** the
  next run logs `Gateway took too long (43s) / buildable:true` nine times, and the cause is the
  duplicate constructions below, not the refresh loop.

- **#59 (2026-10-11) - THE REQUEUE GATE (fixed) + the builder that never fires (open, top
  blocker). Answering the owner's "it worked hours ago, now it does not": there was NO code
  regression - see the table below - and the fixable half is now fixed.**

  **FIXED AND VERIFIED IN A GAME: a Gateway cancelled for "taking too long" was never
  re-requested.** `CancelTooLongConstructions.constructionCancelledRequestAgainBecauseItsImportant`
  asked "do we already have the maximum not-started ones?" **before** the call that frees the
  slot:

  ```java
  if (CountInQueue.countNotStarted(type) >= maxAllowedAtOnce(type)) return false;
  ...
  CancelNotStarted.cancel(type, "Taking too long, re-try");   // frees the slot, too late
  ```

  `maxAllowedAtOnce(Gateway)` is 2, so a plan holding two not-started Gateways vetoed its own
  replacement. Measured in a full game before the fix: `### FAILED ### Requesting` = **0** and
  `Taking too long, re-try` = **0** - the requeue path never executed at all. After the fix, on
  the same command: **2 and 2**. Test `GatewayRequeueAfterCancelTest` (source-pinned on the
  ORDER, which is the whole defect; proven to fail on the old order and pass on the new one).

  **A probe kept, deliberately:** `NewConstructionRequest` now logs `REQUEST_ACCEPTED` /
  `REQUEST_REFUSED_DUPLICATE` under `BUILDER_LOG=1`, because "no new construction appeared" had
  two indistinguishable causes (producer gated out vs request rejected as duplicate) and this
  item could not be diagnosed without it.

  ---

  **FIXED 2026-10-11 (commit `7278d55d`) - and the cause was NOT the engine, it was our own
  distance comparison. `travelWhenReady` compared `groundDist(positionToBuildCenter())` against
  the 1.4-tile "close enough to build" threshold, and both halves of that expression were wrong
  for the question being asked:**

  - **centre, not tile**: `positionToBuildCenter()` is offset by the building's tile dimensions
    (~1.8 tiles for a 4x3 Gateway), so it can never be within 1.4 of a worker standing ON the
    tile;
  - **`groundDist`, not `distTo`**: `groundDistanceTo` routes through JBWEB
    (`PositionUtil`), which `_AI/POSITION-FINDER.md` point 3 already records as unusable on
    OpenBW. Measured side by side on the same worker and tile: **`distTo=1.3`,
    `groundDist=1.8`** - the JBWEB-mediated number is inflated, and THAT was the one compared
    against the 1.4 threshold.

  So `shouldMoveToConstruct` stayed true while the worker stood on its site, `moveToConstruct`
  ran every frame, and `IssueBuildOrder` - the only path to `unit.build` - was never reached:
  the measured 35-43s cancels. **Verified in a game: the run that produced `Gateway=0` now
  produces `Gateway=2`, and in one run `CyberneticsC=1 Dragoon=3` - the full opening.**

  **A second, independent bug was found by the same trace and fixed:** `IssueBuildOrder` gated
  the build attempt on `unit.lastActionMoreThanAgo(20)`, an action age that `AUnitOrders.move`
  resets on every call (`setLastActionReceivedNow().setAction(...)`) - so for a worker whose
  last command is `MOVE_BUILD` it was false on nearly every frame (measured: `lastActionAgo`
  sawtoothing 2,5,8,11,14,17,20, one build attempt in the whole run). It now uses
  `lastCommandIssuedAgo`, which is only updated when a command is actually issued. **This is the
  third instance of that shape** (a debounce reading a value the path itself refreshes); the
  other two are already documented in `TravelToConstruct`.

  **Honest limit: reproducible improvement, not determinism.** Two verification runs after the
  fix: `Gateway=2 + Dragoon=3` (passes) and `Gateway=0` (fails). Before the fix, vs-Zerg runs
  gave `Gateway=0` consistently. Strictly better, not solved - the remaining variance is the
  next item's territory (`#54`'s Cybernetics-Core refresh loop, which is what stops the
  Dragoon).

  ---

  **Superseded diagnosis, kept because it is the record of the hunt** (it was wrong about the
  cause being the engine's refusal - the refusal was a *symptom* of never getting there, and
  the one `BUILD_ATTEMPT` that did happen was an `engineAccepted=false` on a worker whose last
  command was still `Move`):

  ```
  Pylon    engineAccepted=true   distToBuild=0.0  isMoving=true  lastCommand=Build
  Pylon    engineAccepted=true   distToBuild=0.9  isMoving=true  lastCommand=Build
  Pylon    engineAccepted=true   distToBuild=0.6  isMoving=true  lastCommand=Build
  Gateway  engineAccepted=false  distToBuild=0.4  isMoving=true  lastCommand=Move   <- refused
  ```

  **`distToBuild`, `isMoving` and the action are identical** for the Pylons that work and the
  Gateway that does not. **The only difference is `lastCommand`**: `Build` vs `Move` - and
  `lastCommandName()` reads the ENGINE's own `getLastCommand()`, so it is the engine telling us
  our last accepted order was a move. The Gateway therefore sits `NOT_STARTED` until
  `CancelTooLongConstructions` kills it at 35-43s, which is the churn the owner sees.

  **Why Pylons are immune:** a Pylon's site is chosen in open ground and the worker arrives and
  its move settles (`getOrder=PlaceBuilding` in the working trace); the Gateway site shares its
  area with other builders/units, so `moveToConstruct` keeps re-issuing moves into the frames
  where `IssueBuildOrder` then tries the build.

  **A `stop`-then-build attempt was added and did NOT fix it** (`IssueBuildOrder`: on a refused
  build, if `unit.isMoving()` stop and return). Measured: `stop` was never even reached in the
  following run, and `gateways=0` again - so the assumption behind it (that the worker is moving
  at that moment) is not what holds. **Do not repeat that patch without a trace showing the
  stop being issued.**

  **Superseded 2026-10-12 by the trace that DID show it:** later `BUILD_ATTEMPT` lines read
  `distToBuild=1.3 isMoving=true lastCommand=Move` refused every frame - the worker IS moving
  at refusal, it just never settles, so the old `lastCommand==Move && !isMoving` gate (added
  for the stale-move twin of this defect) never fires. The condition is now bare
  `lastCommand==Move`: stopping inside the 1.4-tile threshold is always safe (travel
  issues no moves there; the >= 3 age gate retries once settled). Different trigger,
  same sequence - the "do not repeat" above meant the moving-worker variant, which is
  exactly what the new trace demanded.

  **Standstill probe 2026-10-12 (owner's ask):** `BUILD_BREAKDOWN` in
  `BuilderDiagnostics`, fired from `BuilderManager` when a builder stands still
  60+ frames on a NOT_STARTED site (`BUILDER_LOG=1`, every 30th frame). It calls
  every `canBuild` level separately - worker gates, unit/game/our-port
  reachability, no-builder/with-builder tile rule, full gate, explored,
  affordability - so the refusing term is read, not inferred. A short
  building-only run is the existing `--short` tier with this flag; no new
  harness was needed for it.

  **BISECTION RESULT 2026-10-12 - it is poverty, exonerating regions, power,
  occupancy and placement in one line.** Gateway at [96,111]: `unitHasPath`,
  `gameHasPath`, `ourPort` all true; `noBuilder/withBuilder/withBuilderExplored`
  all true; `hasPower=true`, `unitsInRect=0`, terrain ok; but `canBuild=false`.
  Flag bisection (`000=true`, `100=false`, `010/001=true`) isolated the third
  sub-flag: `Game.canMake` -> `minerals(28) < 150`. The engine is right - the bot
  is broke when it attempts. Regions, power, occupancy, placement and the patched
  fallback are all cleared by the same line. The defect is upstream: ~10 workers
  hold ~30 minerals (spent as fast as mined on the worker pump?), so the Gateway
  is attempted broke, refused, and cancelled at 35s. Different site, different
  mechanism than [14,51] (`noBuilder=false` there - power or occupancy, still
  needs its own split run).

  **Next measurement, exact and cheap:** in `IssueBuildOrder`, the moment `unit.build(...)`
  returns false, print `unit.u().canBuild(type, tile)`, `unit.u().canIssueCommand(canBuild(
  type, tile))` and `unit.u().getOrder()` - i.e. WHICH engine gate says no, rather than assuming
  it is the order type. The one thing already known is `canCommand=true` (printed) and
  `canBuildHere=true` (printed), so the remaining terms are `canIssueCommandType` and the
  `isBuilding || isInterruptible` branch - and `isInterruptible` was measured true earlier in
  `PLAN-STAGE4-COMMAND-PATH.md`, which makes `canIssueCommandType` the last candidate standing.

  **Superseded framing, kept for the record: "it worked hours ago, now it does not".**

  **There is no commit between the working run and the broken one that changed any behaviour.**
  Verified with `git log --name-only` over the window: the only commit in between (`e657b83f`
  "Tweaks", 20:40) **deleted one `_AI/e2e/*.md` file and touched no source**. The jar was
  freshness-checked fresh against the working tree in both runs.

  **What actually differs between "worked" and "did not", measured with the same jar:**

  | run | budget | opponent | result |
  |---|---|---|---|
  | 20:03 | 120 s | Protoss | `Gateway=2` + CyberneticsC + Dragoon (worked) |
  | 20:39 (owner) | 1200 s in-game | Zerg | 0 Gateway, 0 army |
  | mine now | 120 s | Protoss | 0 Gateway |
  | mine now | 120 s | Zerg | `Pylon=3 Gateway=0 Zealot=0 Dragoon=0`, ingame=1209 s |

  **The discriminator is not the race and not the code - it is whether the construction
  pipeline happens to get a Gateway started before the run ends.** The failure signature is
  identical in every failing run: a Gateway construction exists, its builder walks to it,
  and 43 game-seconds later `Cancel construction: Gateway / Gateway took too long (43s) /
  at:[14,51] / buildable:true`, after which it is **never re-requested** (the log has no
  second `ADDED TO QUEUE` for Gateway). Two Gateway constructions, two cancels, then silence -
  at `min=834`, `sup=20`, with 25 Probes. So the bot has the economy and the intent and still
  ends at zero Gateways.

  **This is why the earlier "it works" result was real but not reproducible:** the fix to
  `TravelToConstruct` (the 1.4-tile center-vs-tile distance) removed the `WTF?!?` refresh loop
  (46-50 -> 0, measured), which was enough for ONE run to get a Gateway through. It did not
  remove the cancel-and-never-requeue, which is the same construction-lifecycle defect as #57.
  **Do not re-open the distance fix - it is verified. The remaining blocker is the requeue.**

  **Next measurement (names the bug, ~20 lines):** instrument `Construction.cancel` to print
  the caller chain, and `AddToQueue`/`NewConstructionRequest` to print when a Gateway request
  is refused. The question to answer is narrow: after `CancelTooLongConstructions` cancels the
  Gateway, why is there no new request - is `ProduceGateway` gated out (it has ~20 gates, all
  logged with `log("...")`), or is the request made and rejected as a duplicate
  (`alreadyExists`, #57)?

- **#61 (2026-10-11, NEW) - WORKERS THAT STAND STILL AND NEVER MINE (`manager=GatherResources`,
  `lastCommand=NO_COMMAND`, `lastCommandIssuedAgo=1`, `isGathering=true`, `isIdle=true`).**
  Owner's report: "many workers just stand there doing nothing after being produced; in the
  earlier game it happened after 10-12 of them, here even the first few (5, 6) stand still -
  and there is NOTHING in the log".

  **Measured** with a purpose-built probe (`WorkerHandlerCommander.reportStuckWorker`, one line
  per second per worker whose position has not changed for 10 frames, `BUILDER_LOG=1` - written
  because this class of bug is invisible everywhere else: nothing throws and no manager reports
  a refusal):

  ```
  STUCK_WORKER @90 unit=#116 @[95,119] stoodStillFor=25f manager=GatherResources
    lastCommand=NO_COMMAND lastCommandIssuedAgo=1 action=BUILD isIdle=true isStopped=true
    isGathering=true distToMineral=4.4 mineralsNear=9
  ```

  **1003 such lines in one run, the same 5 workers from frame 30 to the end.** `lastCommand=NO_COMMAND`
  with `lastCommandIssuedAgo=1` means something calls `lastCommandIssuedNow()` every frame while
  the engine records no command - and the worker is 4.4 tiles from 9 minerals, idle and stopped.

  **Three hypotheses were checked and are RULED OUT** (recorded so they are not re-tried):
  1. **stale `isGathering*` flags** - a `AMineralGathering`/`GatherResources` relaxation
     (`stalledWhileClaimingToGather`) was written on the strength of the reading above and **did
     not change the behaviour**; reverted, not shipped;
  2. **`TreeSet` identity in `Select.ourWorkers()`** - it collects into a `TreeSet` ordered by
     `AUnit.compareTo`, which compares `hashCode()`, so colliding workers would be silently
     dropped. Ruled out: `hashCode()` IS `id()` and ids are unique per game;
  3. **the worker is not iterated at all** - ruled out: the probe sits inside
     `WorkerHandlerCommander`'s iteration and reports it every frame.

  **This is also an economy explanation, not just a curiosity:** a worker that never mines is a
  lost miner for the whole game, which is how #60's `CantAfford -> NoSupply` oscillation starts.

  **Next measurement (the one that ends the guessing):** print, on a stuck worker,
  `new GatherResources(worker).applies()`, `new IdleWorker(worker).applies()` and
  `new WorkerManager(worker).applies()` - the managers' OWN answers, not the state they read.
  **Careful with placement:** these must be called from a class in a package that may depend on
  `atlantis.production`; putting them in `WorkerHandlerCommander` (core, `atlantis.units..`)
  trips `ArchitectureBoundaryTest` - it did, twice in one session.

- **#60 (2026-10-11, top economy blocker) - THE WORKER GATE OSCILLATES BETWEEN TWO
  OPPOSITE FAILURES, and it explains "the test takes forever", the 8000-mineral bank and the
  stuck Pylon at once.**

  **Measured** with a new `WORKER_GATE` probe (one line per second, `BUILDER_LOG=1`;
  `AutoProduceWorkersCommander.REASON` was written and read nowhere, so the refusing gate was
  literally unobservable):

  ```
  WORKER_GATE @30     reason=CantAfford workers=5   minerals=0      supply=5/9
  WORKER_GATE @17910  reason=NoSupply   workers=23  minerals=8554   supply=33/33
  ```

  1. **Early: `CantAfford`** - 5 workers, **0 minerals** - the bot cannot afford the 50-mineral
     Probe that would fix that. `reserved=0`, so nothing else is holding the money.
  2. **Late: `NoSupply`** - 23 workers, **8554 minerals**, `supply=33/33` - supply-capped with a
     bank it cannot spend, which is the owner's "never more than 600 minerals" at its worst and
     the reason the run crawls.

  **The link between them, measured the same run:** the bot reaches `supply=33/33` because its
  **Pylon construction sits `NOT_STARTED`** while the worker stands ON the site
  (`distToSite=0.2`, `lastActionAgo=193`, `isMoving=false`). A Pylon that never completes means
  supply never rises, so workers stop, so mining stops, so the next Pylon is unaffordable - the
  cycle closes. (The one Pylon that DID complete shows the healthy path working: at `@1127`
  `engineAccepted=true getOrder=PlaceBuilding isConstructing=true` - so the build path itself is
  fine; it is the construction lifecycle around it that stalls.)

  **Also from the owner's adhoc run:** `Assimilator took too long (33s) / buildable:false` - the
  first genuinely-not-buildable tile seen (the Gateway case was `buildable:true`), so the
  Assimilator tile selection is a separate, real defect.

  **Next measurement (narrow):** print, on the Pylon whose `canAfford=false`, the value
  `AGame.minerals()` **in the same line** - the two readings above come from different frames,
  and `minerals >= 100` with a large bank is impossible, so one of the two numbers is being read
  at the wrong moment. Until that is settled, do not change any affordability gate.

- **#57 (2026-10-11) - the duplicate-construction leak is the single root cause behind
  both symptoms the owner reported (Gateway churn AND minerals above 600).**

  **Measured:** three constructions of a Gateway at the SAME tile `[92,113]` plus one at
  `[92,111]`, four probes assigned to them, **zero build attempts**, and the bank at 974
  minerals. The producers are not the bug - `ProduceGateway` fires correctly on
  `A.hasMinerals(150) && freeGateways <= 1`, and the build order lists Gateways - and the
  mineral hoard is a CONSEQUENCE, not a separate gate: minerals accumulate because nothing that
  would spend them ever completes.

  **What it is not:** not position drift (three at one identical tile), not `alreadyExists`
  working as written - a stub-world test that hammers
  `NewConstructionRequest.requestConstructionOf(Gateway)` with a rich bank passes, producing
  exactly one construction. So the duplicate is created through a path that BYPASSES that
  guard, or before the first one was registered.

  **Next measurement, and it is the one that names the bug (do not patch blind):** instrument
  `ConstructionRequests.constructions.add(...)` with `AConsole.printStackTrace(...)` and the
  building type - every add site is then named with its caller chain. That is cheap (~20 lines,
  one run) and it is the same technique that identified the build-issuing chain in
  `PLAN-STAGE4-COMMAND-PATH.md`. The number is new so nobody mistakes this for a duplicate of
  #54: #54 was the refresh loop, this is the construction leak.

  **Related, and it is the same shape as #52's lesson:** a predicate used to ACCEPT must be
  the same one used to REJECT, or the gap between them is invisible until a game trips it.
  Worth checking at the same time whether `buildable:true` should short-circuit the second
  call outright.

- **#55 - the map analysis runs on the IDE path too (`MAP_ANALYSIS areas=38 chokes=54`),
  which the owner does not expect outside OpenBW.** Reported 2026-10-11: "we normally did
  not do this, it was not needed". It is not obviously a defect - `AMap.initMapAnalysis()`
  is unconditional in the pipeline and `verifyMapAnalysisIsUsable()` was added as a
  diagnosis for OpenBW - but it costs time at startup on every backend and nobody has
  measured whether that cost is wanted. **Not investigated yet**, and recorded because the
  owner flagged it as a change in behaviour: either it was always there and went unnoticed
  (most likely, since the log line is old), or something started calling it. Check with
  `git log` on `AMap.initMapAnalysis` callers before assuming either.

- **#58 - Expansion is DECIDED but the Nexus is re-queued instead of finished.** (Was
  mis-numbered #54, colliding with the `RefreshConstructionPosition` item; numbers are never
  reused, so the duplicate takes the next free one - CONVENTIONS §7.) Owner's log
  (2026-10-11), two lines a few seconds apart:

  ```
  2:57: Nexus ADDED TO QUEUE, min=792/ sup=17 / LimitedBases
  3:00: Nexus ADDED TO QUEUE, min=832/ sup=17 / LimitedBases
  ```

  **`LimitedBases` is not a reason to expand, it is the label ON a yes** -
  `ProtossShouldExpand:38` is `if (A.hasMinerals(777) && Count.basesWithPlanned() <= 3) return
  yes("LimitedBases")`. So the decision is correct: 792 minerals, one base, expand. The
  interesting part is the SECOND line - three seconds later, still `<= 3` bases planned, the
  Nexus added to the queue AGAIN. Either the first Nexus was never built, or it was built and
  `basesWithPlanned()` does not count it, and both read the same in the log.

  **REPRODUCED AND THE CAUSE FOUND: `isOverdue` was cancelling the whole plan.** A 1209-second
  run reproduced the re-queue (7× `Nexus ADDED TO QUEUE`, three seconds apart), and the log
  around it named the mechanism:

  ```
  Cancel construction: Pylon    / Pylon is overdue                / buildable:true
  Cancel construction: Gateway  / Last resort - cancelling ALL not started
  Cancel construction: Nexus    / Last resort - cancelling ALL not started
  ```

  `Construction.isOverdue()`:

  ```java
  if (hasStarted()) return false;                    // status != NOT_STARTED || buildingUnit() != null
  ...
  return A.ago(timeOrdered) > 30 * 22;               // 22 seconds
  ```

  `buildingUnit()` is always null on OpenBW (the `buildUnit()` defect, this item), so the
  status carried the entire decision - and a construction whose builder is still WALKING is
  `NOT_STARTED` by definition, because the status advances when the worker starts building,
  not when it is dispatched. A Nexus site 26 tiles away therefore looked exactly like a worker
  that had given up.

  The reaction is what makes it severe: `OverdueConstructions.whenDoesNotRequirePower` cancels
  the construction, cancels every other not-started building, re-queues, and if the re-queue
  returns null runs `Last resort - cancelling ALL not started`. **One slow walk flushed the
  entire plan**, which is why the same Nexus was ordered seven times and almost nothing was
  ever finished.

  **Fixed in `isOverdue`: an assigned, living builder means it is not overdue.** That is what
  "overdue" means - nobody is working on it. Measured after: `Last resort - cancelling ALL`
  goes 0, `is overdue` 0, `Nexus ADDED TO QUEUE` 2 (a normal re-plan), on the same 1209-second
  run.

  **Still open, and it is the next layer:** with the cancellations gone the bot stops at
  `Probe=25 Pylon=4` and builders sit on `Pylon status=NOT_STARTED` 701 frames against 38
  frames of `IN_PROGRESS` - while four Pylons did get built, so `isConstructing` is not a
  reliable "has this worker started" signal either. The completion path from #53 uses it, so
  that is where the next measurement goes: either the status should also advance when the
  building EXISTS at the site (which `buildingFinishedAtSite` already detects for the other
  half), or the engine's `isConstructing` needs a second condition. Do not guess which - the
  probe is cheap and the two have different consequences.

- **#56 - `TerranNewBuilding` has the same two finish-path defects `ProtossWarping` had,**
  and they were not touched because they need their own measurement (found 2026-10-11 while
  fixing #53).

  `TerranNewBuilding.updateNewBuilding` is the Terran twin, and it differs from the fixed
  Protoss path in exactly two ways, both visible in the source:

  1. **No fallback when the positional lookup misses** - `defineConstruction` returns null and
     the method simply `return`s, so a Terran building that went up away from its reserved
     tile leaves its construction in the queue.
  2. **`IN_PROGRESS`, never `FINISHED`** - and `setStatus(FINISHED)` is the only status that
     removes a construction, so a Terran construction that produced its building is still
     counted by `countPendingOfType` for the rest of the game.

  **Why this is a separate item and not a copy of the Protoss fix:** the Terran worker does
  not walk away when the building starts - it is walked INTO the building (the SCV is consumed
  by the construction and `ConstructionStatusChanger` reads `builder.buildType()` for the
  "builder became the building" case) - so the deadlock shape and the correct release point
  are different, and the Protoss fix's `GatherResources(builder)` would be wrong here. Measure
  first: a Terran OpenBW run, `BUILDER_LOG=1`, and check whether `No construction for <unit>`
  and a growing `countPendingOfType` appear. Tracked rather than fixed blind, per
  `CONVENTIONS.md` §3a.

- **#62 (2026-10-12) - the Assimilator is never built on OpenBW; the request tile is right,
  the engine still refuses.** Owner's log has only `GAS_FIX in=[86,118] geysersIn6=1
  nearest=[88,119] d=0.0` and `out(ok)=[86,118]`; the construction then dies
  `took too long (33-39s) / buildable:false`.

  **Checked in the source, and both easy readings are wrong:**
  - `GasBuildingPositionFinder:36` returns `geyser.translateByPixels(-64, -32)` - for a
    4x2 geyser that is exactly the centre-to-top-left conversion, so the request tile
    EQUALS the engine's geyser tile by construction. The offset is not the bug.
  - `d=0.0` is correct edge-distance (BWAPI `getDistance` is edge-to-edge; the site is
    adjacent to the geyser), not a broken `distTo`.
  - Exonerated too: the JBWAPI `hasPathOverTerrain` fallback (`Game.java:2491-2507`)
    errs towards reachable, so the builder-path branch passes next to a geyser; and
    the `TooCloseToGeyser` exemption (`TooCloseToMineralsOrGeyser:28`) is in place.

  **Next measurement (one bounded run, no patch before it):** at the refusal, print
  requestTile vs the engine geyser tile equality plus the Java client's `canBuildHere`
  components (refinery branch, builder `hasPath`, explored, power) with `lastCommand`
  and `distToBuild`. The tile math says the refinery term should pass, so the refusal
  is elsewhere - the probe names where instead of a third static theory.

  **FIXED 2026-10-12 (Stardust pattern) WITHOUT waiting for the run.** Reading  Stardust's placement settled the code side: a refinery site is its geyser's OWN
  tile (`Block::Location(geyser->tile)`), never center-math. Our finder recomputed
  it as `center-(2,1)` and both `GasBuildingFix` snaps used the center itself -
  right on some maps, 2-off wherever center and tile disagree about rounding, and
  wrong-by-construction for the snap. New `AUnit.tilePosition()` (engine value,
  footprint-derived fallback), finder and both snaps answer the geyser tile.
  Test `GasTileIsGeyserTileTest` (fix case fails pre-fix). The engine-equality
  half still belongs to the GAS_REFUSED probe above - if it ever reads
  `noBuilder=false` on this tile, the term after the tile is next.

  **EXPERIMENT 2026-10-12 (owner's ask, TEMPORARY):** the attempt tile cycles
  exact/center/right-adjacent/below-adjacent across 15-frame windows
  (`GasBuildingFix.experimentalVariantTile`, `GAS_VARIANT` log on change,
  `GasVariantTileTest` pins the rotation). The log then shows which tile an
  accepted attempt used. REMOVE with the experiment once measured - the answer
  becomes the single request tile.

- **#64 CLOSED 2026-10-12 - StackOverflowError in the Pylon relaxed-margin fallback.**
  Owner's log tail: `findStandardPositionFor:189` <-> `findWithRelaxedBoundaryMargin:206`
  alternating until the stack overflowed, bot dead instead of "no tile". Mechanism:
  the fallback branch fired on every failed Pylon sweep with no re-entry guard, so
  the relaxed pass re-entered the same branch forever. One-corner case that triggers
  it: a sweep that fails even relaxed (dense base / corner geometry).
  Fix: `&& !relaxedBoundaryMargin` on the fallback branch. Proof: new
  `PylonFallbackNoRecursionTest` (zero search radius fails both passes by
  construction) throws `StackOverflowError` on the old code and passes on the new;
  flag reset asserted. Unit scope green.

- **#69 (2026-10-12) - exit path hangs after the trace write (9 s game, 110 s run).**
  Owner's adhoc log: ForceExit at real 9 s, GameSummary printed, trace files
  written, then no "Killing"/"Exit" prints, one more silent ForceExit (latch),
  frames stopped, timeout killed the JVM at 110 s. Location unproven - candidates
  are `SaveGameResultsToFile` and the kill branch, all silent/fast by inspection.
  Mitigated, not diagnosed: `OnGameEnd` arms a 20 s daemon backstop (`halt(1)`,
  hooks skipped) on the self-exit path, so a hung teardown fails in ~30 s instead
  of burning the cap. Skipped for `END_GAME_VIA_ENGINE` and in tests. Still open:
  catch one hanging run with `jstack` and name the frame.

- **#67 (2026-10-12) - sleep gate overwrote the owner in history (CLOSED).**
  Owner's question: `UnitStateManager`/`WorkerManager` lines in a builder's
  history, alternating with the leaf - dispatchers that only forward. Mechanism:
  the latency gate returned `usedManager(this, "SleepUntilFrame")`, stamping
  whichever dispatcher ran (UnitState one frame, Worker the next) over the true
  owner, with the leaf's stale tooltip attached. That alternation read as hijack
  and cost two withdrawn readings (#48/#61). Fix: the gate returns `this`
  (chain still stops) without touching owner or history
  (`SleepGateOwnershipTest`, fails pre-fix). `unit.manager()` semantics kept -
  dozens of `isActiveManager` gates depend on it.

- **#68 (2026-10-12) - per-unit data never attaches ("i???" on click). ROOT CAUSE
  FOUND, fix in.** The viewer fuzzy-matches cvis units against replay units by
  (frame, type, x, y); our `units_first_seen[].type` carried `AUnitType.id()`
  (ours, auto-assigned from 1000 - the trace showed `"type": 1210` for a Probe)
  while everything else speaks BWAPI ids (Probe 64). Zero units matched, every
  click resolved to a negative id. Fix: record `ut().id` at all three write
  sites (discovery, creation hook, seed); the `types_names` table already uses
  engine ids (verified against the JBWAPI enum). Test pins literal 64/176,
  fails pre-fix. Game verification (fresh trace + viewer match) pending.
  Follow-up in this item: per-frame manager+order lines for non-building units
  (viewer renders `units_logs`, not the `manager` update field - wire order
  issue points behind a flag). DONE 2026-10-12: `CV.unitLog` at `BwapiOrderSink`
  (every order, OK/REFUSED) + on manager change, behind const-false
  `CHERRYVIS_UNIT_ORDERS`; proven in game (16 units, 30 manager + 1272 order
  lines), flag reverted after. A killed run once "collected" the previous
  run's trace and passed - source dir is now purged pre-run.
  Reference analysis 2026-10-12 (`cherryvisdumper.cpp/.h`): shapes confirmed -
  first_seen `{id,type,x,y}` per frame arrays, updates `{task|type}` on change,
  logs `{frame,message,sev,file,line,attachments}`, board `{frame:{k:v}}`
  changed-only, tasks list (absent for us - no UPC system, panel shows None by
  design). Our `units_logs {frame,message}` subset renders (renderer needs only
  those two). Known-minor, not fixed: `board_updates` emits duplicate frame
  keys when two states change on one frame (last wins).
  Viewer-side check 2026-10-12: traces ≤19:43 carry pre-fix 1000+ type ids
  (match nothing, all ???); traces ≥19:59 carry engine ids. If ??? persists on
  a NEW trace, the needed datum is the browser's `[BW<->CP Matching] Unable to
  match N units` console line (names the failing axis: type/position/frame).
  FIXED 2026-10-12 part 2: frame-0 seeds never match (replay has no visible
  units on frame 0; Stardust bumps 0->1 for the same reason) - our `record()`
  clamps to 1, fresh traces seed 107 units at frame "1", no "0" key.
  MATCHER mechanics (read from `openbw/ui/unit_matcher.cpp`, not guessed):
  per-frame, nearest same-type within 25px, ambiguity guards (18-25px needs
  uniqueness in 30px, 10-18px in 20px), one-to-one consumption, NO retry of a
  missed frame. Fragile cases: dense mineral lines, same-spot warps; a WASM
  throw mid-run freezes all later units while early ones keep working.

- **#66 (2026-10-12) - game starts paused, keypress "2" unfreezes it (CLOSED).**
  Owner's IDE/Wine report: 40 s frozen from the start, first "2" starts the game.
  Two real traps, both fixed without behavior change: (1) speed changes paused
  first and unpaused last with no `finally` - anything throwing in between left
  `GameSpeed` paused forever while `OnEveryFrame` spins (`KeyboardPauseReleaseTest`
  pins it with a throwing game mock, fails pre-fix); (2) a keys file left by a
  killed session replayed Pause into the next game - `KeyRelay.purge()` at both
  launcher starts drops it unexecuted. Engine-pause (`resumeGame`) path kept as
  is - it already covers its case with a test.

- **#65 (2026-10-12) - CherryVis per-unit reporting: first-seen fixed, two format
  questions open.** Owner's report: whole-bot reporting worked, per-unit never did
  ("format was wrong or something"). Measured in source, no frontend needed:
  - FIXED: `ACherryVis_UnitFirstSeen.get()` indexed a frame-keyed map by loop
    counter (`unitsSeenAt.get(i)`) - NPE on any real game, killing the whole
    `trace.json` write at game end - and emitted one object per frame instead of an
    array (two discoveries on one frame lost one). Rewritten as frame-to-list with
    id dedup (the shape Stardust writes and the viewer parses); dead predecessor
    `ACherryVis_UnitFirstSeenOld` deleted. Test `CherryVisUnitFirstSeenTest` pins
    the exact JSON without running CherryVis.
  - FIXED: `units_updates` legend. Manager ids are runtime-assigned from 10000 in
    first-logged order, but `custom_names.manager` hardcoded one exact order - any
    other order mislabeled every manager in the viewer, and managers past the table
    had no name. The legend is now built from the runtime table
    (`CherryVisCustomNamesTest` pins it, no frontend needed).
  - FIXED: empty `units_first_seen` in real games. Our listener only recorded
    `UNIT_DISCOVERED`, which BWAPI never fires for our own units - and on OpenBW
    (late attach) that is every unit that matters. Record at creation
    (`OnUnitCreated`, Stardust does the same) plus a start-of-game seed sweep of
    ours and minerals (`OnGameStarted`, Stardust marks static neutrals the same
    way); id dedup makes the overlap harmless. `CherryVisSeedCurrentUnitsTest`.
  - FIXED (owner's install, 2026-10-12): the viewer returned HTTP 500 on every
    page - `replays/views.py` does `import zstd` but the README (and our
    `install_cherryvis.sh`) installed only `zstandard`. The script installs both
    now, and gained `--run-only` (server without reinstall, prints "Starting
    CherryVis server without installation..."). Verified: HTTP 200.
  - FIXED (empty side panel, owner report): the panel loads cvis data through
    `/get/cvis/` while the game canvas does not - so the game played and the
    panel stayed empty with no console error. Root cause in two layers: the
    shipped `zstd` lost the streaming API (first compat patch), and its one-shot
    replacement cannot decode size-less streams (measured `length mismatch` on
    the sample). The endpoint now uses `zstandard`'s streaming reader with a
    plain-file fallback. Verified: sample serves 6.4 MB valid JSON, ours 23
    first-seen frames.
  - PROVEN (2026-10-12): `--short --cvis --save-replay` leaves the viewable pair
    `/sc-ai/REPLAYS/<name>.rep` + `<name>.rep.cvis/` (valid trace.json, 23
    first-seen frames, 16 updated units).
  - FIXED: `units_logs` is no longer `{}`. New `CV.unitLog(message, unit)` API
    (Stardust's `CherryVis::log(unitId)`), stored per unit, rendered inline in the
    legacy single-file shape; `CherryVisUnitLogsTest` pins the exact JSON. First
    gated call sites: build issued/refused in `IssueBuildOrder`, behind the new
    const-false `DebugFlags.CHERRYVIS_BUILDER_ACTIONS` (compiled away when off -
    the per-line flag pattern the owner asked for).
  - FIXED (owner-found 2026-10-12): `zstd-jni` was missing from the bot jar, so
    `onGameEnd` died with `NoClassDefFoundError` and no trace was ever written.
    The lib is packaged now (build asserts `Zstd.class`), the writer path is
    guarded so a debug artifact can never kill the game-end report again, and
    `trace.json` is plain JSON with the zstd copy next to it as `trace.json.zst`.
  - E2E: `--cvis` flag in `run-openbw-e2e.sh` (ENV `GENERATE_CHERRYVIS_REPLAY`),
    artifacts collected into the run dir and next to the `.rep` as `.rep.cvis/`,
    missing trace fails a clean run; `--self-test-cvis` covers the checks.
  Next: decide per-unit logs shape (files like Stardust vs inline), then pin
  `units_updates`+`custom_names` agreement with the same no-frontend string test.

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
