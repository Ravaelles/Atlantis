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
- **#28** Production code imports the test harness: 15 files under
  `src/atlantis`/`src/main` import `tests.fakes.*` (`AUnit` -> `FakeUnit`,
  `Bullets` -> `FakeBullets`, `AbstractFoggedUnit`, `AUnitOrders` ->
  `FakeUnitData`, ...) and `ClearCountCache` imports
  `tests.unit.helpers.ClearAllCaches`. (The two JUnit classes that lived in the
  production tree have moved to `src/tests/acceptance/production/`, and the one
  whose only test was commented out now asserts something.) The
  consequence is concrete: the game jar **must ship `tests/fakes/**` and
  `tests/unit/helpers/**`**, otherwise
  `NoClassDefFoundError: tests/fakes/FakeUnit` (`GAME_08792F08`). Fix it the
  ADR 0001 way: give each of those call sites a port (a unit sink, a bullet
  sink, a cache-clearing hook) with the fakes as one adapter among several,
  and then the jar can stop shipping the harness. Verify with a game run: the
  jar size and `scripts/build-bot-jar.sh`'s assertions are the check.


## Stage E — read model (remaining)

- **#3** Migrate production readers of `FoggedUnit` to `UnitSnapshot`.
  Deliberately skipped before because a pure delegation switch has no value
  (same object, same values, worse GC). Do it only where it changes a
  decision, e.g. code that needs the *last known* vs *current* distinction or
  that can now be expressed without fog-awareness. List candidate sites
  first, migrate the ones with a real payoff, and record the rest as rejected.

## Stage F — cache purge

The full inventory is `DOCS/SELECT-CACHES.md` (46 entries, key → TTL → readers),
generated rather than remembered. Three concrete starting points came out of it;
the first is already done.
- `Select.clearCache()` never clears `cacheObject`, so
  `mainOrAnyBuildingPosition` lives purely on its 73-frame TTL. Decide whether
  that is an oversight before migrating anything.
- 24 of the 46 keys use `microCacheForFrames`, which is literally the constant
  `1` - the same one-frame intent as TTL `0`, spelled twice. Replacing it is
  behaviour-neutral and needs no game run.
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
  `util -> units/game/map/production/information/combat/debug` = 73,
  `information -> combat/production` = 66,
  `architecture -> combat/production/units/game/util/debug` = 24.
  The 267 and the 24 are structural - the Commander/Manager framework is
  unit-centric by design, so they are Stage E/H work, not a mechanical sweep.
  The 73 and the 66 are the mechanical ones; keep taking them smallest-first.
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
- **#14** Add a benchmark guard for the frame pipeline
  (`scripts/benchmark-trees.sh` result checked in CI-style thresholds) so a
  Stage E/F/H refactor that slows the per-frame work fails visibly instead of
  silently.

## SOLID follow-ups (see `DOCS/SOLID-CHECKLIST.md`)

- **#16** Give `AUnit` and `Selection` consumer-shaped interfaces. ISP cannot
  start before the split: 587 and 231 methods cannot be "just injected". Start
  with the two or three narrowest consumers (e.g. "something that can be
  attacked", "something that has a position") and move call sites one by one.
- **#17** Remove `System.exit` from domain code: `AtlantisRaceConfig`,
  `APositionFinder`, `Atlantis`, `AKeyboard`. Same rule as the `AFile` fix —
  report and let the composition root decide. Verify each with a game run,
  because a wrong exit path is invisible in unit tests.
- **#18** Add the remaining ADR 0001 ports (`GameQuery`, `MapPort`,
  `ClockPort`) and migrate one subsystem each. `LogPort` is the precedent:
  port + adapter + a test double, no call-site churn. Clock first — it is the
  most-read global (`A.now`, `A.seconds()`, `A.minSec()`), and a port makes
  TTL/frame logic testable without a game.

## Housekeeping

- **#15** Rebuild the deployed bot jars. Done for the `A`-split round
  (`GAME_5AC1C438`, `GAME_5C6F3544`) and for the race/harness round
  (`GAME_7D1C5E57`, `is_crashed: false`, zero exceptions, 51 units built);
  repeat after the next backlog round so `bots/AtlantisP` and `bots/AtlantisT`
  never lag behind the source.
