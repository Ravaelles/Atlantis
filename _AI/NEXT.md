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

The suite is green: 237 passing, 0 failing, 4 skipped (`ObjectToFileTest` needs
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
- **#28** Production code imports the test harness. **4 files left** (was 15),
  each needing an ADR-0001 port with the fakes as one adapter among several:
  `AtlantisJfap`, `PositionUtil` and `AUnit` (all `instanceof FakeUnit`) and
  `AUnitOrders` (a fake order sink while `u()` is null). All four ask the same
  kind of question - "is this a real game unit?" - so one predicate port may
  serve all of them; the order of work is theirs to pick.

  The port shape is now written down twice - `UnitStats.Source` for unit and
  weapon data, and `Bullets.Source` for the bullets in flight:

  ```java
  // production: a question, not a branch on the environment
  public interface Source { Set<ABullet> currentBullets(); boolean bulletStillExists(ABullet b); }
  private static Source source;            // null in a game
  private static Source source() { return source != null ? source : engineSource; }
  // tests/fakes/FakeBullets.Source is the other adapter; setUp() installs it
  ```

  Three ports are done, all of the same shape and all with the engine as the
  default: `Bullets.Source` (which bullets exist, and whether the engine object
  behind one is still there - the two `Env.isTesting()` branches it used to have),
  `Regions.Source` (which region a tile is in, where the stub world has no BWEM
  areas and answers one region per base), and
  `AbstractFoggedUnit.FoggedUnitFactory` (how a unit that went behind the fog is
  wrapped). No call site changed in any of them.

  The last one also moved `FakeFoggedUnit` out of `src/atlantis/units/fogged`
  into `tests/fakes`: a test double no longer sits in the production tree, and the
  `instanceof FakeUnit` branch that reached for it is now in the harness's own
  adapter, where it belongs. `PositionUtil` was reading the fake type to get a
  position; it now reads `AbstractFoggedUnit`, which is what the fake extends and
  what the game's own fogged unit extends too.

  Gone earlier, and both were worse than an import:
  - `ClearCountCache` imported `tests.unit.helpers.ClearAllCaches` and **never
    used it** - the dependency existed only in the import list, and the jar had to
    ship a test package for nothing;
  - `BaseLocationsTest` and `ChokesTest` were JUnit classes inside
    `src/atlantis/map/...`, so they were outside every `--select-package tests`
    selector and **no command in `scripts/` ever executed them**. They live in
    `tests/acceptance` now and run (3 tests).

  Consequence while any of the 9 remains: the game jar **must ship
  `tests/fakes/**` and `tests/unit/helpers/**`**, otherwise
  `NoClassDefFoundError: tests/fakes/FakeUnit` (`GAME_08792F08`). The seam added
  with `UnitStats` (a `Source` the harness installs, production delegating to
  the engine) is the shape to copy. Verify the end of it with a game run: the
  jar size and `scripts/build-bot-jar.sh`'s assertions are the check.


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
  store only as a history of how far the ratchet got). **430 violations left**
  (was 499). Two rules are already at 0 (`map.scout -> combat/production`, map
  geometry), so what is left is:
  `core(units, units.., map.position.., decisions..) -> combat/production/
  information/protoss/terran/map.scout/map.base/units.workers` = 267,
  `util -> units/game/map/production/information/combat/debug` = 60 (was 73),
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
  - `CacheKey.toKey` (13 entries) - a polymorphic key builder that knows about
    units, positions, chokes, bases and constructions. Removing it means either
    moving the knowledge to the callers or giving the cache a key type.
  - `Cache` (5) - a generic cache that reads `A.now()` and clones `Selection`s.
    Both are the clock and a domain type reaching into the kernel; the fix is a
    clock port and a per-type copy hook.
  - `Log`, `ErrorLog`, `ConsoleLog`, `TimeMoment` (11) - the logging kernel
    reads the game clock, and `Log` also stores `debug.tools.LogMessage`
    (tried: moving that class next to `Log` deletes 9 entries but the class
    reads `A.now()` itself and arrives with 4 new ones, so it needs a
    clock-free `LogMessage` first).
  - `Vectors` (8) - a geometry helper whose signatures mention `AUnit`; it is
    the one remaining class where "move it next to what it serves" is a real
    option, once its three callers are counted as one change.
  - `We.haveBase`, `BwapiAccessibility` and the `util/object` serialisation
    cluster (5) - facades and a subsystem only a `@Disabled` test uses.

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
  mock for world-free tests too. No `ClockPort`, no new abstraction; the field
  stays as a write-only leftover until a follow-up deletes it with the two
  test setup writes. `GameQuery` and `MapPort` are still open.

## Housekeeping

- **#15** Rebuild the deployed bot jars. Done for the `A`-split round
  (`GAME_5AC1C438`, `GAME_5C6F3544`) and for the race/harness round
  (`GAME_7D1C5E57`, `is_crashed: false`, zero exceptions, 51 units built);
  repeat after the next backlog round so `bots/AtlantisP` and `bots/AtlantisT`
  never lag behind the source.
