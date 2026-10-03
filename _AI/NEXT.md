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

- **#1** Triage the remaining unit-test failures - now **6, all
  `ATargetingTest`**, and **blocked on missing engine data** (see #29). The five
  retreat failures are gone; `ChokeTest.distToChokes` turned out to be the
  harness defect of `_AI/BUGS.md` B-15, not a choke bug.
- **#29** The test harness has **no unit-type and no weapon-type data**, so every
  test that depends on an attribute measures a fiction. Measured on 2026-10-03,
  after `AUnitType.isAirUnit()` was introduced: the suite is
  **7 failing**, and every one of them is explained by the remaining
  placeholders. What is wrong, by probe of `bwapi.UnitType` and
  `bwapi.WeaponType` inside a test:

  | field | harness value | StarCraft |
  |---|---|---|
  | `UnitType.isFlyer()` | false for everything (Scourge/Overlord excepted) | replaced by the explicit list in `AUnitType.isAirUnit()` |
  | `UnitType.maxHitPoints()` | Marine 40, Bunker 350, Sunken Colony 300, Creep Colony 400, Overlord 200/0 shields | 45, 400, 150, 600, 200/**50** |
  | `WeaponType.maxRange()` | Dragoon 128 px (4 tiles), Hydralisk 128, Lurker 192, Drone 32, Scourge 3 | 192 (6), 160 (5), 256 (8), 64 (2), 64 (2) |
  | `WeaponType.damageAmount()` / `damageFactor()` | **0 for every weapon** (only `Psi_Blades` is special-cased to 16 in `WeaponUtil`) | real per-weapon values |
  | `UnitType.isBuilding()`, `isWorker()` | correct | correct |

  `BulletDamageAgainstTest` fails with "expected 5 but was 0" purely because
  `WeaponUtil.damageNormalized()` multiplies two zeros.

  **There is no authoritative source on this machine.** Checked three ways:
  1. the jar itself - placeholders, as above;
  2. `3rdparty/openbw/bwapi/Documentation/dox/unittypes.dox` - **identical**
     values for all 205 comparable types, so it was generated from the same
     uninitialised client;
  3. `3rdparty/openbw/bwapi/bwapi/BWAPILIBTest/unitTypesTest.cpp` - also the
     same values (`Assert_maxHitPoints(300)` for a Sunken Colony), i.e. BWAPI's
     own reference tests encode the fiction.
  The real numbers live only in `units.dat` / `weapons.dat` inside
  `starcraft/STARDAT.MPQ`, and nothing here can read them: `mpyq` fails with
  "Encryption is not supported yet" (StarCraft MPQs have encrypted tables),
  PyMS is not installable from PyPI in a usable form, and StormLib is not on
  the machine. Writing an MPQ reader with PKWARE-DCL decompression, or
  installing StormLib, is the only way to get data that is not typed by hand.

  So the remaining choice: (a) hand-write the table into the **harness**
  (`tests/fakes`), not into production - roughly 8 hit-point values, 10 ranges
  and 6 weapons - with a guard test that fails when a unit type used by a test
  has no entry, and then re-check each of the 15 expectations against real
  StarCraft behaviour; or (b) get StormLib and generate the table from
  `units.dat`, which needs no judgement at all.
  **Do not** "fix" a failure by rewriting the expectation to match the fake
  world - `ATargetingTest`'s expectations are right about the game.
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

- **#2** Decide the representation of unknown hit points in the read model
  (`AbstractFoggedUnit.hp()` returns the `-69` sentinel). Options: explicit
  `OptionalInt`/nullable in `UnitSnapshot`, a dedicated `Hp` value object, or
  a documented sentinel accessor on the snapshot. Needs a written rationale —
  the current magic number leaks into every comparison site.
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

## Review 2026-10-03 — air/ground audit leftovers

Sceptical pass over `feature/2025-12-t...feature/2026-10-ii` after the flyer
fiction (Dragoon/Vulture/Broodling/Infested Terran treated as air). Fixed in
this round: `isAirUnit()` now unions `ut.isFlyer()` (covers hero flyers in a
real game), `hasBiggerWeaponRangeThan(Units)` compared ground-vs-air ranges,
`isPurelyAntiAir()` listed the ground Goliath instead of the Devourer,
`hasCloseRepairer()` had inverted air/ground thresholds,
`CombatEvaluatorTest` names said "Beat" while asserting `eval > 1` (lose),
`AvoidCombatBuildingsTest` comments said turrets/spores "cannot shoot at air"
about a ground Dragoon (and copy-pasted "Dragoon" into the Wraith test).
Verified: full suite 222/6 (same 6 `ATargetingTest` placeholders as #29),
ArchUnit 7/7.

- **#30** Re-check the marine-vs-hydra evaluations once #29 lands. Renamed to
  `fourMarinesLoseToOneHydralisk` / `threeMarinesLoseToTwoHydralisks` /
  `marinesAndMedicLoseToOneHydralisk` (`eval` 7.56 / 3.37 / 21.25 = "we lose"),
  but 4 marines vs 1 hydra losing 7:1 smells like placeholder damage (0 for
  every weapon), not StarCraft. Do not re-pin numbers before the harness has
  real damage; then decide whether the evaluator or the scenario is wrong.
- **#31** `ProtossRetreatTest.goonsVsCannons` pins no-retreat for 1v1..10v1
  Dragoon-vs-Cannon while its own javadoc admits `eval` 0.3 (3x worse) and the
  cannon outranges the dragoon. Retreat doctrine vs evaluator disagreement —
  needs a design decision, not a threshold tweak.
- **#32** `UnitTest`/`SelectTest` counts pin the `isInvincible()` placeholder:
  the 6th "ground unit" is a Vulture spider mine, real only because the fake
  `isInvincible()` is wrong. When #29 fixes the harness, the guard test must
  fail loudly here instead of silently shifting `GROUND_UNITS`/`AIR_UNITS`/
  `REAL_UNITS`.
- **#33** Hero flyers are still ground in the harness. Production is fixed
  (`ut.isFlyer() ||` hand list), but the hand list has no `Hero_*` entries, so
  e.g. a hero Scout/Mutri/Guardian counts as ground in tests. Add hero entries
  (or engine-data-driven flags) with the #29 harness work.
