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
- **#29** The test harness has **no unit-type data at all**, so every test that
  depends on unit attributes measures a fiction. Measured: with
  `bwapi.UnitType` read straight from the jar, `isFlyer()` is **false for every
  single type** (a Protoss Dragoon is a ground unit), hit points are wrong
  (marine 40, sunken colony 300, marine should be 45/150), and weapon ranges are
  placeholders (Dragoon 128 px = 8 tiles instead of 6). `UnitType` reads those
  from static arrays that a BWAPI client fills from the running game, and there
  is no client - and no data file to load them from - in a test. That is why
  `ATargetingTest` cannot pass: its subject is air-vs-ground targeting.
  Attempts and measurements, so nobody repeats them:
  - Filling the arrays from a hand-written table of real stats *and* the
    Flyer/Building/Worker flags: **10 failures instead of 6** - giving Cocoons
    real hit points turned them into valid targets, and marking buildings
    changed the building-vs-worker priority. The jar's placeholder data and the
    suite's expectations were calibrated together.
  - Flags only, no hit points: also 10 failures (a Nexus started winning over a
    Photon Cannon).
  So the choice is: (a) get real unit-type data - run a game with a patched
  client that dumps `UnitTypeContainer`, or point the harness at a data file
  from scbw/OpenBW; or (b) keep a maintained table **and** recalibrate every
  expectation that depends on a unit attribute (ranges, hit points, air) - which
  is most of `tests.unit`. Until then `ATargetingTest` stays red on purpose:
  its expectations are right about the game and unreachable in this harness.
  Do **not** "fix" them by rewriting the expectations to match the fake world.
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

- **#24** `AUnit.shieldPercent()` is `100 * shields / maxShields` with no zero
  guard, so it returns `NaN` for every unit without shields (Terran, buildings
  without upgrades). Production callers check `maxShields()` first, so it is
  harmless today, but a naive use silently poisons comparisons. Decide the
  contract (100%? 0%? throw?) and fix it; `AUnitTest.shieldsOnAUnitThatHasNone`
  pins the current behaviour on purpose.

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

- **#4** Inventory the static caches in `Select` (4 caches, dozens of string
  keys, magic TTLs: 0, 1, 30, 31, 53, 73, 91, 293 frames) into one document
  table: key → TTL → why the TTL → who reads it. No code change yet; the
  inventory is what makes the migration safe.
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
  store only as a history of how far the ratchet got).
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
