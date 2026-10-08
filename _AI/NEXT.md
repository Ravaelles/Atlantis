# TODO — Atlantis (open issues)

Living backlog for the architecture migration defined in `_AI/REVIEW.md` §16
and for defects found along the way. This file is the single source of truth
for "what is left"; `_AI/REVIEW.md` keeps the *stage* narrative and
`_AI/NOTES.md` keeps operational learnings.

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

The suite is green: 258 passing, 0 failing, 4 skipped (`ObjectToFileTest` needs
a serialized fixture and a `--add-opens` flag). What is still open is *quality*
of the data behind it, not a red test.

- **#29** The test harness had **no unit-type and no weapon-type data** - or so
  the suite believed. The truth, established by probing the vendored
  `JBWAPI-Rav.jar` field by field and cross-checking against BWAPI's own
  reference tests (`bwapi/BWAPILIBTest/unitTypesTest.cpp` on github.com/bwapi:
  Marine 40 hit points, Ghost 45, Vulture 80, Goliath 125, Siege Tank 150,
  Dragoon 100 + 80 shields, Photon Cannon 100 + 100, Sunken Colony 300, Spider
  Mine 20 hit points), is that **the engine was the source all along** and an
  intermediate `UnitStatsTable` overwrote it with some twenty hand-transcribed
  fictions (Marine 45, Sunken Colony 150, Phase Disruptor 8 damage, Siege Tank
  at 5 and 6 tiles, Mutalisk at 6, Ghost 125 hit points, a Terran *Banshee*).
  The suite was green against that fiction; the episode is kept in git history
  as a warning and in `UnitStatsTable`'s javadoc.

  What stands now:
  - `atlantis/units/UnitStats.java` - the seam. `hitPoints`, `shields`,
    `weaponRange`, `weaponDamage`, `weaponDamageFactor`, each answering a
    `UnitStats.Source` if one is installed and `bwapi` otherwise. Production
    installs nothing, so in a game this is delegation and nothing else changes.
  - `tests/fakes/UnitStatsTable.java` - a **correction table, currently
    empty**. It holds one entry per engine field proven wrong, each with the
    evidence; everything else falls back to the engine. The near-misses were
    base-vs-upgraded confusion (a Dragoon shoots 4 tiles until Singularity
    Charge, a Hydralisk 4 until Grooved Spines - the engine's 128 px is
    correct, and upgrade-aware callers like `OurDragoonRange` live separately).
  - `tests/unit/UnitStatsTableTest.java` - the guard: engine sanity pins for
    every number the suite depends on (so a jar swap fails loudly instead of
    drifting), a ban on unjustified corrections, a name-resolution check, and
    the installation check.
  - 14 call sites rewired (all of `maxRange`/`damageAmount`/`damageFactor` in
    production). `WeaponUtil` moved `atlantis.util` -> `atlantis.units` because
    it reads unit data and `util` must not point upward; the ArchUnit store
    shrank by 8 lines.

  How to verify the engine from a checkout, without a game: compile a small
  `Probe` against `lib/` and print `maxHitPoints/maxShields/isFlyer` for
  `UnitType`, `maxRange/damageAmount/damageFactor/damageType` for
  `WeaponType`. Full procedure in `UnitStatsTable`'s javadoc.

  Still open:
  - `UnitType.isFlyer()` names 22 types and no heroes - but production covers
    heroes through `ut.isFlyer()` itself (see #33, closed);
  - the MPQ archives (`STARDAT.MPQ`/`BROODAT.MPQ` sit one directory above the
    repo) remain unreadable from here: standard header geometry, encrypted
    tables, no local tool for Blizzard's filename crypto plus PKWARE-implode
    sectors. They would be the generator if they ever open; until then the
    engine is the source and the table is the patch list.

  **Do not** "fix" a failure by rewriting the expectation to match the fake
  world. When a test failed for real, the reasoning is in the test's comment.
- **#28** ~~Production code imports the test harness.~~ **Closed 2026-10-04.**
  `src/atlantis`, `src/main` and `src/jfap` were cleaned one port at a time - the
  last five behind `UnitStats.Source`, `Bullets.Source`, `Regions.Source`,
  `AbstractFoggedUnit.FoggedUnitFactory` and `UnitOrigin`, no call site changed in
  any of them. What was left was one finding rather than twelve:
  `src/starengine`, a fake-driven simulator in the production tree, which the owner
  decided to delete rather than give ports ("StarEngine is/will be unnecessary since
  we can use OpenBW for tests or other approaches ... we should remove the StarEngine
  code and replace it with OpenBW").

  Removed in that commit: 26 files / 1209 lines of simulator, 3 assertion-free smoke
  tests, the `isUsingEngine` branches in `AbstractWorldCreatingTest` /
  `FakeOnFrameEnd` / `FakeUnit`, and `Env.markUsingStarEngine` / `isStarEngine`
  (written by the launcher, read by a commented-out line in `Select`). `FakeUnit`'s
  two StarEngine enums became plain flags - nothing but the simulator set them.

  The payoff was the jar, and it is measured rather than assumed: production
  bytecode has no reference to `tests/` or `starengine/` (0 of 3610 entries, checked
  by scanning constant pools), so `scripts/build-bot-jar.sh` now drops both trees
  from the payload and *fails the build* if a packaged class ever names `tests/`
  again. Same machine, same script: 3755 entries -> 3610, i.e. 119 harness classes
  and 26 simulator classes the game was shipping for nothing.

  What is lost, and what replaces it: nothing can assert on a simulated fight until
  the OpenBW tiers in `_AI/IDEA-E2E-TESTS.md` Stage 3 land. The stub world plays
  scripted scenarios with harness physics, which is enough for defence mechanisms
  (B-19's cannon and probes) and not enough for "does this micro win the fight".
  That is the open part, and it is not a port problem - it is the OpenBW stepper.

  The port shape is now written down twice - `UnitStats.Source` for unit and
  weapon data, and `Bullets.Source` for the bullets in flight:

  ```java
  // production: a question, not a branch on the environment
  public interface Source { Set<ABullet> currentBullets(); boolean bulletStillExists(ABullet b); }
  private static Source source;            // null in a game
  private static Source source() { return source != null ? source : engineSource; }
  // tests/fakes/FakeBullets.Source is the other adapter; setUp() installs it
  ```

  Five ports are done, all of the same shape and all with the engine as the
  default: `UnitStats.Source` (unit and weapon data),
  `Bullets.Source` (which bullets exist, and whether the engine object behind one
  is still there - the two `Env.isTesting()` branches it used to have),
  `Regions.Source` (which region a tile is in, where the stub world has no BWEM
  areas and answers one region per base),
  `AbstractFoggedUnit.FoggedUnitFactory` (how a unit that went behind the fog is
  wrapped), and
  `UnitOrigin` ("is this unit simulated?" - the `instanceof FakeUnit` term in
  `AUnit.init()`, `AUnit.cacheType()`, `AtlantisJfap.isValidUnit()` and
  `JfapCombatEvaluator.addFriends()`).
  No call site changed in any of them.

  The fogged-unit port also moved `FakeFoggedUnit` out of
  `src/atlantis/units/fogged` into `tests/fakes`: a test double no longer sits in
  the production tree, and the branch that reached for it is now in the harness's
  own adapter, where it belongs.

  Two sites needed no port at all, which is the useful result of reading them
  first:
  - `PositionUtil` asked *which class* when the real question was *is there an
    engine object*. Its `FakeUnit` branch existed only because the `AUnit` branch
    assumed every `AUnit` has a `bwapi.Unit`. It now measures from the unit when it
    has one and takes the position the unit reports when it does not, so a double
    takes the path its branch used to take and a game unit is untouched.
  - `AUnitOrders` was recording orders into `tests.fakes.FakeUnitData`. That is a
    sink, not a predicate, so it became `OrderFallback`. Its default answers
    `false`, because in a game every ordered unit has an engine object and the
    branch is unreachable - "the game never received this order" is the honest
    answer where the old `List.add` returned `true`.


  Gone earlier, and both were worse than an import:
  - `ClearCountCache` imported `tests.unit.helpers.ClearAllCaches` and **never
    used it** - the dependency existed only in the import list, and the jar had to
    ship a test package for nothing;
  - `BaseLocationsTest` and `ChokesTest` were JUnit classes inside
    `src/atlantis/map/...`, so they were outside every `--select-package tests`
    selector and **no command in `scripts/` ever executed them**. They live in
    `tests/acceptance` now and run (3 tests).

  Consequence while `src/starengine` remains: the game jar **must ship
  `tests/fakes/**`**, otherwise `NoClassDefFoundError: tests/fakes/FakeUnit`
  (`GAME_08792F08`) - the simulator is compiled into it. `scripts/build-bot-jar.sh`
  *asserts* that three harness classes are present, and those assertions are still
  true: **do not flip them to "must be absent"** before `src/starengine` is out.
  When it is, the jar can stop shipping the harness and those assertions invert -
  the end is verified with a game run (jar size plus the assertions), which is the
  user's half of #15/#28.

  `tests/unit/helpers/**` no longer has to ship: `ClearCountCache` was its last
  importer.


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
  `ArchitectureBoundaryTest` from frozen store to hard rules (keeping the
  store only as a history of how far the ratchet got). **356 violations left**
  (was 499; re-counted from the store files on 2026-10-04, not from this note).
  Three rules are already at 0 (`map.scout -> combat/production`, map geometry,
  and the fifth one), so what is left is:
  `core(units, units.., map.position.., decisions..) -> combat/production/
  information/protoss/terran/map.scout/map.base/units.workers` = 267,
  `util -> units/game/map/production/information/combat/debug` = **0** (was 73),
  `information -> combat/production` = 65 (was 66),
  `architecture -> combat/production/units/game/util/debug` = 24.
  The 267 and the 24 are structural - the Commander/Manager framework is
  unit-centric by design, so they are Stage E/H work, not a mechanical sweep.
  The 73 and the 66 are the mechanical ones; keep taking them smallest-first.
  The mechanical sweep under
  `_AI/work-orders/WO-13-mechanical-archunit-sweep.md` took the two rules to
  **60 and 65** (from 73 and 66) and then hit the design wall: what is left in
  them is not mechanical. A scripted triage of every remaining entry for "does
  anything call this" came back empty - all four util candidates and all
  thirteen information candidates are `private` methods that their own class
  calls. What remains, by class:
  - ~~`CacheKey`~~ **done 2026-10-04, -18 - the biggest single win left.** It was
    18 entries, not 13: `toKey(Object)` `instanceof`-checks thirteen domain types
    and `create(AUnitType[])` formats them. A shared kernel that knows thirteen
    domain types is not shared, so the class moved from `atlantis.util.cache` to
    `atlantis.units.select` beside `Selection` (five of the thirteen call sites are
    in that class) - **and that alone traded 18 violations for 4 new ones**, which
    is the trap above in its purest form: `atlantis.units.select` is core, and
    `Construction` and `ABaseLocation` are consumers. Two branches had to go for
    the move to be honest rather than a shuffle:
    - the two call sites that passed a `Construction` now pass `construction.id()`,
      which is all the key ever read ("Constr#" was cosmetic);
    - the `ABaseLocation` branch was a wrapper around `toString()`, so deleting it
      changed no key anywhere.
    The store diff is 18 deletions and zero additions - the first number that is
    worth checking twice, because a placement change is exactly the kind that
    hides a swap.
  - `Cache` (5) - a generic cache that reads `A.now()` and clones `Selection`s.
    Both are the clock and a domain type reaching into the kernel; the fix is a
    clock port and a per-type copy hook.
  - ~~`Log`, `ErrorLog`, `ConsoleLog`, `TimeMoment`~~ **done 2026-10-04.**
    `LogMessage` moved to `atlantis.util.log` and stopped reading the clock: it is
    a value now - the message plus its two creation timestamps - and `Log` passes
    "now" in. The predicted **-8** came out as **-16 +3**: the ten
    `Log -> LogMessage` edges are gone, and the three additions are the two the
    prediction named (`Log.addMessage -> A.now`, `Log.messages -> A.now`) plus
    `LogMessage.color -> A.now` from the one caller `AAdvancedPainter:301,1503`
    still gets it from. Those three were reviewed in the store diff rather than
    absorbed, which is why the util rule is 39 and not 47.
  - ~~`Vectors`~~ **done 2026-10-04, -5.** It was 5 entries, not 8. The class moved
    from `atlantis.util` to `atlantis.map.position` - next to `APosition`, the only
    thing it actually read - and the `directionTowards(AUnit, AUnit)` overload is
    gone, so the three call sites in `AUnit` pass `unit.position()` themselves. A
    shared kernel that takes domain types as parameters is not a kernel. The store
    diff is five deletions and nothing else: no rule picked up a new edge, because
    `units -> map.position` was already an edge and `map.position -> util` is not
    banned by any of the seven.
  - ~~`BwapiAccessibility` and the `util/object` cluster~~ **done 2026-10-04,
    -4.** The whole serialisation subsystem (9 files, 510 lines) moved from
    `atlantis.util.object` to `atlantis.debug.object`: it names `AGame`, `AMap`,
    `AUnit` and `AUnitType`, which a shared kernel may not, and it is a debug/IO
    helper rather than a utility. Measured while looking for its callers, which is
    the interesting part: the only production call site is
    `OnEveryFrameHelper.serializeMapDataLikeRegionsToAFile()`, whose first line is
    `if (true) return;`, and the only test is `@Disabled` - which is where the four
    skipped tests in the suite come from. So the jar currently ships **kryo +
    minlog + reflectasm + objenesis** for a capability no reachable code can call;
    that is recorded as a question in `BUGS.md` rather than acted on, because
    dropping runtime libraries from the bot jar is the owner's call.
  - **The util rule is empty (2026-10-04, 73 -> 0).** What was left, and where it
    went:
    - three clusters: `util/object` (a debug helper, moved to `atlantis.debug.object`),
      the clock (`GameClock`, the game publishes the counters), and two one-method
      facades (`We.haveBase` inlined into its single caller, which already imported
      `Count`; `Log.addMessage(AUnit)`, the flag and call moved to `AUnit`, which has
      the unit);
    - `Cache.get`'s `Selection` copy hook, which became `atlantis.util.cache.Copyable`
      - the same shape as the `ValidityCheck` already sitting next to it: the kernel
      asks a question through a kernel interface and the value answers, instead of the
      cache naming a domain type. `BaseSelection.copy()` is the clone it always was.
  - ~~three left~~ (superseded by the list above). For the record:
    - ~~`Log.addMessage` / `replaceLastWith` taking an `AUnit` parameter~~ **done,
      -2.** `Log` stores strings and frames and never kept the unit; the parameter
      existed only to feed a debug dump that is switched off
      (`SAVE_UNIT_LOGS_TO_FILES = 0`). The flag and the call moved to
      `AUnit.addManagerLogMessage`, the side that has the unit.
    - `Cache.get`'s copy hook for `Selection` (2: `instanceof` + `clone()`) - one
      decision: who owns "copy this value". A per-type copy hook on the cache is the
      ADR 0001 shape; asking `Selection` for a `copyOf()` is the smaller one.
    - `We.haveBase` (1) - a one-method facade in `atlantis.util` that asks
      `Count.bases()`.
  - ~~the clock~~ **done 2026-10-04, -7.** Seven of the twelve were one question
    asked upward (`A.now()`, `A.seconds()`, `A.everyNthGameFrame()` from the cache,
    the log kernel and the error throttle). `atlantis.util.GameClock` now holds two
    published ints; `AGame.calcSeconds` publishes them next to the `A.now`/`A.s`
    writes it already did, and the harness publishes them in `useFakeTime`. A side
    measurement worth keeping: `A.seconds()` returned **0 in every stub world**, so
    `ErrorLog`'s once-a-minute throttle was comparing 0 against 0 and suppressing
    everything after the first print - the throttle only works now.

  Every one of those is a design decision, not a mechanical edit, so the sweep
  stops here; the 267 and the 24 stay as Stage E/H work.
  Two traps, both paid for already:
  - A change must *remove* violations from one rule, not move them into
    another: moving `ScoutManager` into `atlantis.units.special` compiled,
    passed every test, and only grew the core rule from 267 to 280.
  - `rm -rf out` before `scripts/run-architecture-tests.sh` after moving a class
    between packages. `javac -d` leaves the old `.class` behind, ArchUnit reads
    both, and violations "come back" for a package that no longer exists in the
    sources. ArchUnit also only prunes stale entries for the rule it re-writes,
    so the store file has to be emptied and re-frozen by hand - and that diff
    reviewed, because a re-freeze can absorb violations whose text merely
    changed (the last one rewrote ten Log -> LogMessage entries because
    LogMessage moved package).
- **#16** Give `AUnit` and `Selection` consumer-shaped interfaces. ISP cannot
  start before the split: 587 and 231 methods cannot be "just injected". Start
  with the two or three narrowest consumers (e.g. "something that can be
  attacked", "something that has a position") and move call sites one by one.
- **#17** Remove `System.exit` from domain code: `AtlantisRaceConfig`,
  `APositionFinder`, `Atlantis`, `AKeyboard`. Same rule as the `AFile` fix —
  report and let the composition root decide. Verify each with a game run,
  because a wrong exit path is invisible in unit tests. Triaged: `APositionFinder`
  (the only mid-game leaf) now throws - see BUGS.md B-8; `Atlantis.killProcesses`
  is the shutdown path, where exiting is the job, not a violation; `AtlantisRaceConfig`
  and `AKeyboard` are fail-fast at startup, equivalent outcome either way, so they
  wait for a game run rather than a blind edit.
- **#18** Add the remaining ADR 0001 ports (`GameQuery`, `MapPort`,
  `ClockPort`) and migrate one subsystem each. `LogPort` is the precedent:
  port + adapter + a test double, no call-site churn. Clock turned out not to
  need a port at all: `A.now()` already delegates to the statically mocked
  `AGame.now()`, and the only thing defeating it was 25-odd direct reads of
  the public `A.now` field (plus a mock that stubbed `now()` for world tests
  only). Those reads now go through the method - production-neutral, because
  `AGame` syncs the field every frame - and the base `useFakeTime` stubs the
  mock for world-free tests too. **Clock is closed, in two steps:** the seven
  kernel sites now read `atlantis.util.GameClock`, whose two ints the game layer
  publishes once per frame, and `A.setNow` is the single writer of both that and
  `A.s` (F-1 of `_AI/REVIEW-GLM.md`). The public `A.now` field itself was
  deleted 2026-10-04 once `A.setNow` existed - it had no production reader left,
  and the harness's three clock stubs now read `GameClock.frames()`. `A.s` stays
  a field: four production classes still read it
  (`NeedChokeBlockers`, `DontAttackOverlords`, `ProtossForceFight`,
  `LeaderProgressFlagToNextFocusChoke`), which is a separate change and not a
  dead one.

  **`MapPort` is in, for the tile questions.** `atlantis.map.MapTiles.Source`
  answers four questions the map answers in a game (`isWalkable`, `isExplored`,
  `isVisible`, `isBuildable`), the engine is the default and
  `tests.fakes.FakeMapTiles` is the other adapter. `HasPosition` used to write
  each of them as its own `if (Env.isTesting()) return ...` branch - the stub
  world's rules were spelled out in the core - and the five branches are now one
  file, with no call site changing behaviour. Two things fell out: the
  core no longer reaches for `Select` to decide whether a tile is free (one fewer
  frozen violation), and `APosition.TESTING_EXPLORED`, a public static the core
  carried for one test, moved to the adapter that returns it - where setUp resets
  it, which also fixed a leak in which `BaseLocationsTest` left the map "explored"
  for every test that ran after it.

  Two of the same question are still asked outside the port, both without a
  harness answer written down, which is why they were left rather than folded in:
  `HasPosition.isConnected()` (`Atlantis.game().isVisible`) and
  `map/wall/LocationValidator` (`Atlantis.game().isBuildable`). Deciding what the
  stub world should say for those is a decision, not a mechanical move.

  **`GameQuery` has its first slice too, and it came from a static mock.**
  `ATech` - "what has our player researched and upgraded" - was mocked as a whole
  class in every test (`Mockito.mockStatic(ATech.class)`, three stubbed methods),
  so Mockito answered everything else with null or 0: `getCurrentlyResearching()`
  returned null, `costOf(...)` returned null, `isResearchedWithOrder(...)` said
  "not researched" for every tech including the one `isResearched(Lockdown)` said
  was researched. Two questions are now `ATech.Source` (engine default,
  `tests.fakes.FakeResearch` as the other adapter), the `Env.isTesting()` branch in
  `getUpgradeLevel` is gone, and the rest of the facade runs for real. One test
  (`TerranGhostTest`) had been relying on the mock's hidden "Lockdown is
  researched" and now says so itself.

  The payoff is a capability, not just a smaller dependency:
  `TerranInfantryWeaponsTest` runs one world twice and changes only the researched
  level (0 vs 3), which flips the doctrine's answer. Before, no test could get past
  level 0.

  **What is left in this item** is the biggest remaining `Env` leak - the other
  side of the same coin. Counting `src/atlantis` by grep, code lines only:
  **21 `Env.isTesting()` call sites in 16 files** (29 lines mention it, the other 8
  being comments or javadoc - the earlier counts in this paragraph, 34/28, 41/29 and
  44/24, were raw grep hits and included those),
  one or two per file, across `combat`, `units`, `map`, `production` and
  `information`. Three of them are already answered by ports added since: `AUnit`'s three facing helpers
  through `UnitOrigin` (as `weKnowNothingAboutIt()`), `AbstractFoggedUnit`'s last
  known position through `UnitOrigin`, and `DefineNaturalBase.isConnected()`
  through `MapTiles.hasPathBetween`. A port for the flag itself would be indirection, not
  inversion (`Env` is a static flag holder, so the port would be a static flag
  holder). What is worth doing is the ATech shape: find a subsystem that asks the
  game something real - "is this position walkable", "did we research this" - and
  put the environment branch behind that question. The first one is done:
  - ~~the seven `HasPosition.makeX()` methods~~ **done 2026-10-04.**
    `atlantis.map.position.PositionFinder.Source` asks the seven questions,
    the spiral searches moved verbatim into the engine default, and
    `tests.fakes.FakePositionFinder` answers "this one will do" - the old
    branch answers, byte for byte. Pinned by
    `HasPositionTest.positionFinderReturnsOriginInTests`; suite 258/0/4,
    ArchUnit 7/7, store unchanged. Candidates still open, in
  rough order of how real the question is:
  - the seven `HasPosition.makeX()` methods, which return the position unchanged in
    a test: a *position finder* port, where the harness would answer "this one will
    do" and the port could later answer "here is a free spot". The algorithms behind
    them are the code that would move into the adapter, so this is Stage E work with
    tests (`HasPositionTest`, 6 tests);
  - `CanPhysicallyBuildHere` and `IsProbablyInAnotherRegion` (production position
    logic with a test shortcut);
  - **The 25 that are left, classified (2026-10-04).** Four kinds, and they want
    different things - which is why this item never got shorter by being counted:
    | kind | what the branch does | sites | what it takes |
    |---|---|---|---|
    | A - a different *rule* | the test path takes another decision (never "only attack bases"; every ground combat unit is in Alpha; a retreat is 15 tiles right; comsat station acts every frame) | 10: `ATargeting.shouldOnlyAttackBases`, `AUnit.hasWeaponRangeByGame`/`squad`/`squadSize`/`squadCenter`/`squadCenterUnit`, `ProtossStartRetreat`, `Missions`, `UmsSpecialBehaviorCommander`, `TerranComsatStation` | the harness should get to answer the question, one subsystem at a time - the `WalkableAround` round is the template: find a question the harness can answer, put it behind the port that already exists, delete the branch |
    | B - a different *data source* | the test path is handed a stub answer: a random build position, an approximation, a fresh enemy list | 4: `APositionFinder.findPositionForNew`, `IsProbablyInAnotherRegion`, `EnemyUnits.discovered`, `RebaseToNewMineralPatches` | same shape as A, but note `APositionFinder` is *non-deterministic* in tests today (random coordinates for every building), which is worth knowing before anything pins a position |
    | C - a diagnostic the branch suppresses | production logs an error/warning that tests skip | **4 done**: `DefineNaturalBase`, `RebaseToNewMineralPatches`, `MissionAttackFocusPoint`, `WhenCBDiscovered` | mechanical, and the precedent is set: `CanPhysicallyBuildHere` stopped branching and writes the diagnostic in tests too. Un-gating these costs **1 line** across the acceptance tier (measured) because `ErrorLog`'s once-a-minute throttle now works in stub worlds - which is the GameClock fix paying for itself |
    | C- - a per-event *banner*, not a diagnostic | printed once per game start rather than once per problem | 1, deliberately left: `OnGameStarted`'s "Use build order" - 99 lines in the acceptance tier (measured), because the stub world starts a game in every test. The line is useful in a local game and noise in CI; the comment at the site says so |
    | B/D - not a diagnostic at all | `OnGameEnd` (2) is the test *exit path* plus a summary file, `ProtossShouldFullRetreat` (1) is a rule difference | 3: reclassified after reading them - this is why the table needed reading rather than counting |
    | D - environment plumbing | the flag *is* the question: a test-only cache registry, a default race, a default bwapi path | 3: `Cache.allInstances`, `We.race()`, `AtlantisIgniter` | each needs its own argument; `Cache`'s registry is the cheapest (make it unconditional - a few hundred ints in a game that never reads it) |
    C is done (4 sites), and the order after it is A/B: the harness has to be able to
    answer the question, which is the `WalkableAround` template - one subsystem, one
    port, one test.
  - ~~"is this position walkable"~~ **done 2026-10-04**: `WalkableAround` was the
    last hand-written `if (Env.isTesting()) return true;` in front of a tile question
    - it asked `position.isWalkable()`, which is itself a delegation to `MapTiles`, so
    the branch was a second answer to a question the port had already answered. All
    five tile checks go through `MapTiles.isWalkable` now, one fewer site and one fewer
    place that decides what a stub world means.
  - `AUnit`'s `hasNoU()/noPosition()` guards (`if (...) && !Env.isTesting()`), which
    are really "do we know anything about this unit" - the fog question, and
    therefore the same port `#3` wants;
  - `EnemyUnits.discovered()`'s branch, which is not a game question at all but a
    cache question (the test world has no staleness), so it belongs in the cache,
    not in a port.


## Combat evaluation

- **#35** ~~A symmetric fight stops scoring even when the Jfap horizon is raised.~~
  **Closed 2026-10-04 by decision, not by a fix.** Measured: raw pair `[-88, -88]`
  at the shipped 60-frame horizon, `[-100, -88]` from 120 frames on, with the
  tweak deltas at 0.0 and the clamp inert - so the asymmetry is JFAP's own
  per-player score deltas, i.e. tie handling in a vendored library.
  The owner's ruling: `eval()` only matters from our own side, so a mirror fight
  scoring 0.88 instead of 1.0 is not a defect worth chasing - and the doctrine
  that came out of it ("1.01 means we should win, so that fight is too risky
  unless we recognise 1:1 explicitly") is being implemented as a combat-eval
  penalty, see B-18. What was worth taking from the hunt did get taken: the
  mirror test's "same absolute score" assertion compared our side's score with
  our side's score and could not fail, and it now asserts reciprocity instead.

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

- **#15** Rebuild the deployed bot jars. Done for the `A`-split round
  (`GAME_5AC1C438`, `GAME_5C6F3544`) and for the race/harness round
  (`GAME_7D1C5E57`, `is_crashed: false`, zero exceptions, 51 units built);
  repeat after the next backlog round so `bots/AtlantisP` and `bots/AtlantisT`
  never lag behind the source. **Due again after the 2026-10-04 round, and this
  one is not only a version bump** - three of the changes in it are behaviour or
  payload, and one game run would confirm all three:

  | what to watch in the game | why it matters | how it was verified here |
  |---|---|---|
  | the bot starts at all | the jar no longer ships `tests/**` (119 classes) or `starengine/**` (26) | 0 of 3610 packaged classes reference either, and the build now fails if one ever does |
  | no building is produced twice | B-9: production progress now comes from the units, not from a `Construction` link | unit + acceptance green; `QueueInProgressInvariantTest` pins the whole lifecycle |
  | fewer suicide missions, more caution around even fights | B-18: our own combat eval reads 0.3 lower than the raw ratio | unit + acceptance + all six scenarios green; the doctrine itself is a game-run question |

  The jar assertions that used to *require* the harness are inverted, so this
  round also removes a class of deploy mistakes: a jar that shipped without them
  used to be a build failure, and now it is the correct thing to build.

  Rebuilt 2026-10-04 for the Zerg-rush verification games (owner's call: rush
  first): `bots/AtlantisP/AI/Atlantis.jar` + `bots/AtlantisT/AI/Atlantis.jar`
  from HEAD, 6.1 MB / 3610 entries each, 0 `tests/**` classes (re-verified by
  hand on the artifact, not just by the build log). The table above is what
  those games are for.

  Rebuilt twice more the same day, and the watch table got its games:
  `GAME_9FC7B9A3` (AtlantisP vs Marine Hell) and `GAME_02EC7DF2` (AtlantisP
  vs Steamhammer), both 5000 frames on `sscai/(3)TauCross.scx`, both
  `is_crashed: false` - and both logging ~5000 `NullPointerException`s from
  `DefineMainChoke` (fixed as B-20: a null region skipped the JBWEB
  fallback, plus an unthrottled `printStackTrace` in the production catch).
  `GAME_367376E7` re-ran the Marine Hell pairing with the fix: zero NPEs,
  zero "Problem with", both replays present. `GAME_08C2DF7E` (Bereaver vs
  Marine Hell, no Atlantis) is the control that proved the runner itself.
  First baseline table: `_AI/e2e/scbw-2026-10-04_154203.md`. Of the three
  behaviour watches, "the bot starts at all" is confirmed (four games, no
  nostart); duplicate buildings (B-9) and hedge caution/micro (B-18) are
  still game-run questions these capped games cannot answer.

  Two more full losses 2026-10-04 (same jars): `GAME_DD6EAB8E` (vs
  Steamhammer: 1 ling killed for 21 probes + 2 zealots + nexus, no Forge, no
  cannons) and `GAME_366E9D6C` (vs Marine Hell: kill_score 0 for 14 probes
  + 7 nexuses + gateway, 8 nexuses built while killing nothing). Both 0
  exceptions, both clean production sequences (7 + 16 starts, no
  duplicates). Third baseline table `_AI/e2e/scbw-2026-10-04_161236.md`
  compares 8 games.
- **Update 2026-10-04 (B-21 verification):** `GAME_060E6A25` (full loss vs
  Marine Hell, 0 exceptions, 0 frame-deaths, 0 `HaveBunker` frames) confirms
  both fixes: zero "Cancelling expansion due to enemy pressure" (was five in
  a row in `GAME_366E9D6C`), the two naturals warped this game completed
  (9063, 11766) and lived thousands of frames, and the two remaining
  "Cancelling pending base" lines are the intended re-placement path (a new
  warp elsewhere retires the old morph), not the self-cancel bonfire.
  Fourth baseline table `_AI/e2e/scbw-2026-10-04_165556.md` compares 9.

  Full games 2026-10-04 (same jars, no frame cap): `GAME_B978D4B7`
  (AtlantisP vs Marine Hell - loss, kill_score 500 vs 3000, 0 exceptions)
  and `GAME_2AD8C998` (AtlantisP vs Steamhammer - loss, 150 vs 2400, 0
  exceptions). Second baseline table `_AI/e2e/scbw-2026-10-04_154837.md`
  records all six games. Watch outcomes: B-9 closed (28 building starts
  over three games, zero duplicates), B-8 closed (five games, no exit path,
  no `IllegalStateException`; the three remaining exits stay in #17), B-18
  still open (losses consistent with caution and with being out-macroed -
  unscorable from ladder lines), B-19 still open (no static defense existed
  in either game, so the repaired help-path was never engaged).

## Production v2 and the OpenBW E2E engine (2026-10-08)

- **#43** OpenBW headless run: **the client attaches, the bot then does nothing.**
  Measured (six runs, one command each, `scripts/run-openbw-e2e.sh`):
  `scripts/run-openbw-e2e.sh` → `Connection successful` in
  `out/openbw/bot.log` on every attempt, so the whole attach chain
  (`_AI/CHALLENGES/OpenBW.md`) is **no longer the blocker**. What blocks a
  playable game is one layer above, in the bot:

  1. **No build order is reachable.** With `LOCAL=true` the loader resolves
     `BWAPI_DATA_PATH` + `AI/build_orders/`, and the OpenBW bot directory has no
     `bwapi-data/`, so `bwapi-data/AI/build_orders/` is never found. Symptom is
     silence: `CurrentBuildOrder` stays null, every order is skipped with
     "condition not met", nothing is logged.
  2. **Strategy file-name mismatch.** `ABuildOrderLoader` uses the strategy
     **name**, which `ProtossStrategies.initialize()` sets to the file name
     (`"Zealot into Goon"`), while the *declaration-time* load happens before
     that rename. So the file must exist under **both** names, or the load must
     be deferred to `initialize()`. The repo actually ships
     `Zealot into Goon.txt`; the constant-derived name
     (`PROTOSS_Zealot_into_Goon`) does not exist.
  3. **`AtlantisRaceConfig.validate()` can `System.exit(-1)` during game start**
     when the config is incomplete - the process dies with a clean log.

  **Deferred on purpose** (owner's call): stop chasing this loop. The fixes above
  are understood but each attempt costs a ~5-minute game run and the diagnosis is
  not yet complete end to end (the last run's genuine failure point was not
  confirmed from the log). Revisit with a **stub-world test first** - a unit test
  that drives the strategy/build-order startup path and asserts a non-null
  `CurrentBuildOrder` for every strategy - so the fix is proven without a game.
  Only then spend another OpenBW run.

- **#44** Production v2: **cutover and legacy deletion are ready to do**
  (`_AI/__01_PRODUCTION_TODO.md` P1 + most of P2 closed, `PRODUCTION_V2=LIVE`
  already drops the legacy dynamic/supply policy). Deleting `Queue/**`,
  `ProductionOrder`, `PreventDuplicateOrders`, `ReservedResources` and the
  `Construction/**` healing commanders is the deliberate next step; it is
  separate from #43 and does not need a game run, only the fast suite + ArchUnit.

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

- **#46** Three Protoss strategies declare a build order that does not exist
  anywhere: `3 Gate`, `12 Nexus`, `Carrier Push` (found by
  `tests/unit/StrategyBuildOrderTest`, which compares the names set in
  `ProtossStrategies.initialize()` against the files shipped in
  `bwapi-data/AI/build_orders/Protoss/`). Verified against both the repo tree and
  `/sc-ai/BOTS/AtlantisP/AI/build_orders/Protoss` - the file is missing in both,
  so this is not a version skew. Choosing one of those strategies (`3 Gate` is
  the cheese entry, `12 Nexus` the expansion entry, both reachable through
  `StrategyChooser`) starts a game with no build order at all: the bot mines and
  does nothing, no error, which is the failure the OpenBW run showed.
  Fix options, cheapest first: rename the entries to an existing file, add the
  missing `.txt`, or delete the unreachable ones. Decide with the owner - this is
  game policy, not a mechanical fix. `enemyStrategy()` no longer needs a premature
  load: it reads `AStrategy.canProduceUnit`, which is race logic only.
  Also fixed the design flaw behind it: `ProtossStrategies.initialize()` (naming
  strategies after their build-order files) now runs **before**
  `StrategyChooser.initializeStrategy()`, because a chosen strategy loads its
  order while being selected.

### OpenBW headless run — FIRST PLAYABLE GAME (2026-10-08)

Command (one command, whole lifecycle, per CONVENTIONS §15):
`timeout 300 bash scripts/run-openbw-e2e.sh "maps/cog/(3)TauCross1.1.scx" Protoss Zerg`
Log: `out/openbw/bot.log`. Result, quoted from the log:

```
Connection successful
Analyzing map... Use build order: `Zealot into Goon`
HELLO_ATLANTIS - BWAPI attached, Atlantis is playing!
MISSION @0:15 TO Sparta: TooFewZealots - Focus{name='MainChoke', choke=Choke{[117,35], width=2}}
0:39: Can't find place for `Pylon`, At 8 Pylon (READY_TO_PRODUCE)(#1)
```

So the whole chain works: engine hosts, client attaches, map analysis runs, the
**build order loads**, the bot enters its first mission, and its first real
production order (a Pylon at 8 supply) reaches placement.

Four root causes were found and fixed to get here, all of them silent (no error,
just a bot that mines and does nothing):

1. **Startup order**: `ProtossStrategies.initialize()` renames strategies to their
   build-order file names, but `StrategyChooser` picked one BEFORE that, so the
   chosen strategy looked for `PROTOSS_Zealot_into_Goon.txt`. Fixed by
   initialising the strategies first. Test: `StrategyBuildOrderTest`.
2. **`BWAPI_DATA_PATH` was never set for the OpenBW bot directory** (the wine bot
   directory carries one in its `bwapi-data/AI/ENV`; ours did not). The E2E script
   now writes it explicitly.
3. **`AFile.loadFile` ate the last character of any value ending in the delimiter**
   (`BWAPI_DATA_PATH=.../bwapi-data/` arrived as `.../bwapi-data`), producing
   `.../bwapi-dataread/build_orders/`. Fixed in `AFile` and belt-and-braces in
   `AtlantisIgniter.setBwapiDataPath`. Test: `AFileLoadFileTest`.
4. **`AtlantisRaceConfig.validate()` called `System.exit(-1)` during game start**
   whenever the race was unknown. It now reports and skips.

Remaining, next (not touched this round, recorded as #46): placement.
`Can't find place for 'Pylon' (Can't physically build here)` - `FindPosition`
fails on this map in the OpenBW harness, and `DefineNaturalBase` cannot resolve a
natural base.
