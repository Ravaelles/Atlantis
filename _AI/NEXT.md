# TODO — Atlantis (open issues)

Living backlog for the architecture migration defined in `_AI/REVIEW.md` §16
and for defects found along the way. This file is the single source of truth
for "what is left"; `_AI/REVIEW.md` keeps the *stage* narrative and
`_AI/NOTES.md` keeps operational learnings.

## Current owner priorities (2026-10-08)

1. **Finish Production V2** - `_AI/STATUS.md`, `_AI/__01_PRODUCTION_TODO.md`,
   `_AI/redesign/01_PRODUCTION.md`.
2. **Rewrite `PositionFinder`** - a blocker for (1) and for E2E;
   `_AI/POSITION-FINDER.md` has the facts. Do not patch it in the meantime.

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

- **#29** Unit/weapon data: **the engine is the source.** An intermediate
  `UnitStatsTable` had overwritten it with ~20 hand-transcribed fictions (Marine
  45 hp, Sunken Colony 150, Siege Tank at 5 and 6 tiles, a Terran *Banshee*) and
  the suite was green against them. Settled by probing the vendored
  `JBWAPI-Rav.jar` field by field against BWAPI's own reference tests
  (`bwapi/BWAPILIBTest/unitTypesTest.cpp`). What stands:
  - `atlantis/units/UnitStats.java` - the seam; production installs no `Source`,
    so in a game it is delegation to `bwapi` and nothing else changes;
  - `tests/fakes/UnitStatsTable.java` - a **correction table, currently empty**:
    one entry per engine field proven wrong, with its evidence;
  - `tests/unit/UnitStatsTableTest.java` - the guard: sanity pins for every
    number the suite depends on (a jar swap fails loudly), a ban on unjustified
    corrections, name resolution, installation;
  - 14 production call sites rewired; `WeaponUtil` moved `util` -> `units`
    (`util` must not point upward); the store shrank by 8 lines.

  To verify the engine without a game: compile a small `Probe` against `lib/`
  printing `maxHitPoints/maxShields/isFlyer` for `UnitType` and
  `maxRange/damageAmount/damageFactor/damageType` for `WeaponType` (procedure in
  `UnitStatsTable`'s javadoc). **Do not** "fix" a failure by rewriting the
  expectation to match the fake world - when a test failed for real, the reason is
  in its comment. The MPQ archives stay unreadable from here (encrypted tables, no
  local tool), so the engine remains the source.
- **#28** ~~Production code imports the test harness.~~ **Closed 2026-10-04.**
  Five ports of the same shape, each with the engine as the default:
  `UnitStats.Source`, `Bullets.Source`, `Regions.Source`,
  `AbstractFoggedUnit.FoggedUnitFactory`, `UnitOrigin`; `OrderFallback` replaced an
  order *sink*; `PositionUtil` and `AUnitOrders` needed no port. No call site
  changed. `src/starengine` (a fake-driven simulator in the production tree) was
  deleted rather than given ports - 26 files / 1209 lines and 3 assertion-free
  smoke tests - which also let `scripts/build-bot-jar.sh` drop `tests/**` and
  `starengine/**` from the payload and **fail the build** if a packaged class ever
  names `tests/` again (3755 entries -> 3610, measured). The cost: nothing can
  assert on a simulated fight until OpenBW scenarios land, which is
  `IDEA-E2E-TESTS.md`.

## Scenario E2E (stub tier - runs today)

- **#34** Real-opponent follow-ups for the stub-tier scenarios, now living in
  `tests.e2e` (moved out of `tests.acceptance`, where they were invisible to any
  tier selector). Both twins hold: `FourPoolDefenseTest` (6 lings from x=26) -
  lings dead frames 51-249, cannon on 10 hp after 11 strike rounds, nexus
  untouched, one strike per probe where there used to be none - and
  `NinePoolDefenseTest` (8 lings from x=32, six probes) since the combat units
  are driven directly (see below): nexus holds at 700, cannon and all six
  probes live, all eight lings die, the zealot tanks two strikes and falls at
  218. The full commander never gets past its orchestration in the stub world
  (DoNothing for 200 frames, enemies 2 tiles away), so both scenarios invoke
  `CombatUnitManager` per combat unit per frame - decisions are real Atlantis
  code, only the dispatch is harness, and `ScenarioObserver` (every 15 frames:
  pos, hp, manager, target, order) records what happened. Remaining:
  real opponents (Steamhammer / UAlbertaBot / scripted rusher) once the runner
  from `_AI/IDEA-E2E-TESTS.md` can host Atlantis (its Stages 1-2). The scenarios
  keep their forces, timing and assertions across that move; only the
  driver and the physics get swapped for the engine.

  Real games are playable now (2026-10-04): the two-day outage where no
  match ever started was a map path without the `sscai/` prefix
  (`/app/sc/maps/(3)TauCross.scx` does not exist in the container), fixed in
  every `~/.scbw/bots/*.sh` shortcut. `scripts/run-e2e.sh --parse-only`
  reads the real schema and the first baseline table is
  `_AI/e2e/scbw-2026-10-04_154203.md` (control + three AtlantisP games, two
  pre-B-20 with the NPE stacks honestly flagged, one post-fix clean).
  Remaining: the fixed scenario pairs from `_AI/IDEA-E2E-TESTS.md` §4 (4pool,
  9pool scripted) and the runner from Stages 1-2. Update 2026-10-04: two
  full games to a verdict are in (`GAME_B978D4B7` lost to Marine Hell,
  `GAME_2AD8C998` lost to Steamhammer, both `is_crashed: false`, 0
  exceptions, replays kept) and the second baseline table
  `_AI/e2e/scbw-2026-10-04_154837.md` compares 6 games against the first.
  Real-opponent signal flows; scripted-rush pairs still need the runner.
  Update 2026-10-04: four full games to a verdict, all losses
  (`GAME_B978D4B7`, `GAME_2AD8C998`, `GAME_DD6EAB8E`, `GAME_366E9D6C` -
  the last one kill_score 0 with 8 nexuses built), all `is_crashed: false`,
  0 exceptions; third baseline table `_AI/e2e/scbw-2026-10-04_161236.md`
  compares 8.

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

- **#7** Design the race-strategy seam (which decisions move out of the
  `protoss`/`terran`/`zerg` packages into a `RaceStrategy` port) and record
  it as an ADR before touching the 241 branching sites.
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
- **#35** ~~A symmetric fight stops scoring even when the Jfap horizon is raised.~~
  **Closed 2026-10-04 by decision.** Measured: raw pair `[-88, -88]` at the shipped
  60-frame horizon, `[-100, -88]` from 120 frames on - the asymmetry is JFAP's own
  per-player deltas, i.e. tie handling in a vendored library. The owner's ruling:
  `eval()` only matters from our own side, so a mirror fight scoring 0.88 instead of
  1.0 is not worth chasing; the doctrine that came out of it is a combat-eval
  penalty (B-18). The mirror test now asserts reciprocity instead of comparing our
  side's score with itself.

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
- **#38** Removal candidate (not a decision): `atlantis.debug.object` (9 files, 510
  lines) plus the four libraries its one dead caller needs in the payload - kryo,
  minlog, reflectasm, objenesis. `BUGS.md` B-25 has the measurements; the owner's
  answer there was "note it as a candidate for removal, and if nothing plans to use it
  it is probably a dead idea", and a search of `_AI/IDEA-E2E-TESTS.md`,
  `_AI/REVIEW.md` and every stage found no plan that uses it. So the work is: decide
  once, then either delete the subsystem + `serializeMapDataLikeRegionsToAFile()` +
  `SAVE_UNIT_LOGS_TO_FILES`'s file dump and drop the four jars from
  `scripts/build-bot-jar.sh`, or revive the routine behind a flag and keep them.
  Nothing else in the tree reaches any of it.
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
- **#41** ADR 0006 audit step 2, continued: the 105 production guards of the form
  `eval() >= X`, where the quiet reading (9873.7, "nothing in reach") passes them all.
  Row 1 is measured and recorded in the ADR:
  `ProtossMissionDefendAllowsToAttack` lets a defend-mission unit chase a target 30 tiles
  away on a reading that is not about that target. The method is in place -
  `AUnit.hasEnemyForEval()` and `CombatEvalScale.NO_ENEMY_IN_REACH`, pinned by
  `EvalHasReadingTest` - so the remaining work is one class at a time, three lines per row
  (reading / hasReading / decision). Changing a guard is a strategy decision, not a bug
  fix: the two guards above row 1 already handle the two target kinds that matter.
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

- **#15** Rebuild the deployed bot jars (`bots/AtlantisP/AI`, `bots/AtlantisT/AI`) so
they never lag the source. Done repeatedly; the last rebuild followed the
production-v2 and placement work. Verify with one scbw or OpenBW game run
(`_AI/e2e/*.md` carries the verdict tables; the run journal is in git history).

## Production v2 and the OpenBW E2E engine (2026-10-08)

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
     open in `_AI/__01_PRODUCTION_TODO.md` P2);
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
