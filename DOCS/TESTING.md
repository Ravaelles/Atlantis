# Testing

There is no build system (no Maven/Gradle). The project is an IntelliJ module
with vendored jars in `lib/`. Everything can still be compiled and tested
headlessly:

```bash
bash scripts/run-tests.sh                    # unit tests (default scope!)
bash scripts/run-acceptance-tests.sh         # acceptance tests
bash scripts/run-tests.sh --select-package tests    # everything at once
bash scripts/run-architecture-tests.sh       # architecture bounds only
bash scripts/run-scenario-tests.sh           # the scenario tier (tests.e2e)
bash scripts/run-e2e.sh --self-test      # scbw verdict parser, no game needed
```

`scripts/run-tests.sh` compiles all sources to `out/production/Atlantis` and
runs JUnit via `lib/junit-platform-console-standalone-1.10.0.jar`.

## Two scopes, two baselines — read this before trusting a green run

**The default run only executes `tests.unit`.** The whole
`tests.acceptance` package (world/squad/commander behaviour, 126 tests) is
**not** run by `bash scripts/run-tests.sh`. That was true for the whole
architecture effort, and it hid a lot: acceptance tests were written against a
broken harness and were never executed, so nobody saw 44 failures sitting in
the tree. There is now `scripts/run-acceptance-tests.sh` for that scope, so it
is one command rather than a flag somebody has to remember.

| Scope | Command | Result (2026-10-04) |
|---|---|---|
| Unit (default) | `bash scripts/run-tests.sh` | **111 passing / 0 failing** of 111 (+4 skipped) |
| Acceptance | `bash scripts/run-acceptance-tests.sh` | **130 passing / 0 failing** |
| Scenario (stub-world E2E) | `bash scripts/run-scenario-tests.sh` | **6 passing / 0 failing** (all of `tests.e2e`) |
| Everything, one compile | `bash scripts/run-full-tests.sh` | **257 passing / 0 failing** of 257 (+4 skipped) |
| Architecture | `bash scripts/run-architecture-tests.sh` | **7 passing / 0 failing** |

Two of these counts are scope questions, not regressions. `--select-package
tests.architecture` finds 10 tests while `run-architecture-tests.sh` runs 7 - the
script names the rule classes it wants, the package selector takes everything under
`tests.architecture`. And the scenario scope is `tests.e2e` alone (6); the "10"
this table used to quote predates the two `loneNexusFallsFast` controls being
replaced by `StubWorldDamageTest`.

A third scope no longer exists: `tests.starengine` was 3 assertion-free smoke runs,
and the whole simulator is gone as of 2026-10-04 (OpenBW replaces it -
`_AI/IDEA-E2E-TESTS.md` Stage 5). `scripts/run-full-tests.sh` and
`scripts/run-scenario-tests.sh` no longer have a starengine row.

### Where the time goes (measured 2026-10-04)

| scope | test time | note |
|---|---|---|
| compile (1534 sources, every script) | **3.7 s** | 29 fewer sources since StarEngine went; it was never the bottleneck |
| `tests.unit` | **3 s** | what `run-tests.sh` runs by default |
| `tests.architecture` | **1-2 s** | ten boundary rules in the package, seven of them in `run-architecture-tests.sh` |
| `tests.acceptance` | **4-5 s** | world/squad/commander behaviour |
| `tests.e2e` | **84-87 s** | six stub games; four 900-frame ones, two of which stop early |

So the fast loop is ~6 s end to end and never touches the slow tiers; the whole
suite is ~95 s, and the scenario tier is ~85% of it.

The e2e number is the one to be careful with, twice over. It is **not** the 39 s an
earlier version of this table claimed - that figure predates the scenarios driving real
decisions instead of stubs (they run the actual combat micro and can therefore pay for
the 60-frame Jfap simulation per unit per frame), which is the price of a scenario that
can fail. And it is **noisy**: four consecutive runs of the same code and the same
class files read 84, 85, 85 and 87 s, while two readings minutes apart during the same
session read 148 s (one class, standalone) and 201 s (the whole package), both taken
while other JVMs were still running. So: compare a change against 84-87 s, and treat
anything inside that band as no result at all. `_AI/BUGS.md` carried this as an entry
until 2026-10-04, when the owner closed it as stale - the honest reason is not that the
question was answered but that 86 s for six stub games is not worth more of anyone's
attention.

`run-full-tests.sh` compiles once and prints this table per run, with
`--skip-scenarios` for "everything but the slow tier" (~30 s).

Four tests are skipped on purpose (`ObjectToFileTest`: it needs a serialized
fixture and a `--add-opens` JVM flag - see its javadoc). **The suite is green**:
nothing is failing, and random order (seeds 7, 42, 99, 1234, three rounds each)
gives the identical result.

Run the seeds loop more than once. A single pass per seed is not enough evidence:
an order-dependent failure that depends on a global being installed by an earlier
class showed up in 2 of 6 runs and in none of the next 12, at the same seed either
way. `--select-class` on the offending class reproduces that kind of thing
deterministically, and is the first thing to try when a seed run is red.

The vendored console launcher (1.10.0) has no `--order` flag, so order
sensitivity must be checked with JVM properties:

```bash
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
java -Djunit.jupiter.testclass.order.default=org.junit.jupiter.api.ClassOrderer\$Random \
     -Djunit.jupiter.testmethod.order.default=org.junit.jupiter.api.MethodOrderer\$Random \
     -Djunit.jupiter.testexecution.order.random.seed=1 \
     -cp "out/production/Atlantis:.:$CP" \
     org.junit.platform.console.ConsoleLauncher --select-package tests --details=summary
```

Run that whenever you touch test infrastructure. Order dependence used to be
everywhere; the cause was not the tests but the harness (a `Cache.nukeAllCaches()`
that only worked once per JVM, a race that was hard-coded to Protoss, a global
mission that leaked). See `_AI/BUGS.md` B-12, B-15 and B-16 before adding a
`@TestMethodOrder` or "flaky" label to anything.

### Isolation: one class per JVM

Random order finds *order dependence* - two classes that fight over a global.
This finds *leftovers*: a class that passes only because an earlier class left
something installed. Compile the suite first, then:

```bash
CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
find src/tests -name "*Test.java" | sed 's|src/||; s|\.java$||; s|/|.|g' | sort > /tmp/testclasses.txt
while read -r cls; do
  java -cp "out/production/Atlantis:.:$CP" org.junit.platform.console.ConsoleLauncher \
      --select-class "$cls" --details=summary --disable-ansi-colors --disable-banner 2>/dev/null \
    | grep -E "^\[ +[0-9]+ tests (successful|failed)"
done < /tmp/testclasses.txt
```

Baseline (2026-10-04): **79 classes, 0 failures** - every class passes with
nothing but its own `setUp()` behind it. Five classes run zero tests on purpose:
`AbstractWorldCreatingTest` (abstract base), `RetreatScenarioTest` and
`UnitsForRetreatTest` (helpers with no `@Test`), `UnitTest` (helper), and
`ObjectToFileTest` (the four skipped ones).

This is the check that catches what the seeds only catch sometimes. It found two
real defects on 2026-10-04: `UnitRegistryTest`, which installs nothing and failed
4 of 4 alone while the full suite was green most of the time, and - the harder one
- a queue shared between tests through the static `Strategy`/`Queue` pair, which
only ever showed up as "3 of 8 random-order runs red, always in the scenarios,
never in isolation". A class that only passes in company is a class that documents
nothing.

## A test must state its race

`AbstractTestWithUnits.initRace()` (our race) and `initEnemyRace()` (theirs) are
the override points; the defaults are Terran and Protoss, which is what
`Main.ourRace()` returns and what nearly every test builds. Override them when
the test is about another race:

```java
@Override
public Race initRace() {
    return Race.Protoss;          // pylon/cannon logic
}
```

This is not cosmetic: `AtlantisRaceConfig.BASE`, `WORKER`, `BARRACKS`,
`DEFENSIVE_BUILDING_*` and every `We.terran()` branch follow the race, so a test
that lies about its race silently exercises the wrong branches (that is exactly
how B-13 and B-14 survived).

## Where the numbers come from (unit tests)

The vendored `bwapi` jar carries the real Brood War dataset: hit points,
shields, weapon range, weapon damage. A Marine has 40 hit points, a Sunken
Colony 300, a Tentacle 224 px of reach for 40 damage, a Dragoon 100 + 80 -
every one of them cross-checked against BWAPI's own reference tests. An
earlier `UnitStatsTable` overwrote some twenty of those with hand-transcribed
fictions and the suite went green against them; the table is now a correction
list (currently empty) and `UnitStatsTableTest` pins the engine values instead,
so a jar swap fails loudly rather than drifting.

`atlantis.units.UnitStats` is the seam: production asks it, and outside a game
it is a plain delegation to the engine, so a real game reads the engine as
before.

```
UnitStats.hitPoints(AUnitType)      shields(...)      weaponRange(...)      weaponDamage(...)
    -> UnitStats.Source installed by the harness, -1 for "engine is right"
        -> falls back to bwapi.UnitType / bwapi.WeaponType
```

`UnitStatsTableTest` guards it: engine sanity pins for every number the suite
depends on, a ban on unjustified corrections, a name-resolution check (a typo
in the table is invisible otherwise), and the installation check.
`_AI/NEXT.md` #29 has the rest.

Base values versus upgrades: the engine reports *unupgraded* stats. A Dragoon
shoots 4 tiles until Singularity Charge (then 6) and a Hydralisk 4 tiles until
Grooved Spines (then 5). Upgrade-aware callers (`OurDragoonRange`,
`EnemyDragoonWeaponRange`) live separately; the stub world researches nothing,
so tests measure base stats.

Two harness lies were removed rather than papered over, and both were needed
before any of the above could be measured:

- the enemy race was a value read once in `setUp()`, so a test full of drones
  and creep colonies was silently a Protoss opponent and every `Enemy.zerg()`
  branch answered for the wrong race. It is now `enemyRaceInWorld`, read on every
  call like the supply, and `ATargetingTest` declares the race in each of its 26
  scenarios;
- `AUnit.leader()` returned `Select.ourCombatUnits().first()` when
  `Env.isTesting()`, so a lone Dragoon had a leader in tests and none in a game,
  and the targeting fallback fired where the bot's real doctrines would.

Three expectations that used to be here described behaviour the bot does not
have, and were corrected rather than made to pass:

- `ATargetingTest.targetsMarinesOverBunkerYup` expected a Marine at 13.2 to be
  chosen over a Bunker at 13.1. `ATargetingImportant` lists both in one bucket
  and breaks the tie by distance, so the Bunker is right; the test is now
  `targetsTheBunkerWhenTheBunkerIsNearer`. Its sibling
  `targetsMarinesOverBunker` only ever passed because there the Marine was the
  nearer of the two.
- `ATargetingTest.targetsUnfinishedSunkenOverBaseOrDrones` expected an unfinished
  Sunken Colony at 14.9 over a Creep Colony at 11.4. "Most wounded" compares hit
  points to maximum hit points and an unfinished building still reports full hit
  points, so the tie goes to the nearer building. The bot does have a
  finish-defensive-buildings-first rule, but only for Creep Colonies; there is
  none for Sunken or Spore. The test is now
  `targetsTheNearestBuildingWhenNothingIsWounded`.
- `CombatEvaluatorTest.fourMarinesBeatOneSunkenColony` expected the evaluator to
  rate four Marines above a lone Sunken Colony. With engine data it scores
  about even (0.98, inside marine range) - and over a full fight the colony
  wins that damage race, which the simulation's ~60-frame window cannot see.
  The test is now `fourMarinesAgainstOneSunkenColonyScoresAboutEven` and pins
  that number; the horizon limitation is `_AI/BUGS.md` B-18. (An intermediate
  version pinned 2.17 the other way, measured with a fiction table that had
  the colony at 150 hit points with a 6-damage tentacle.)

Two lists that used to be here are now green:

- `ProtossRetreatTest` and `ProtossSmallRetreatTest` — they were failing on a
  harness that declared the wrong race and on unit positions the world put
  where the test did not expect. `goonsVsCannons` now also pins the
  `DontEnemyCB` doctrine (no retreat from an anti-ground cannon within 10
  tiles).
- `ChokeTest.distToChokes` (`2.0` vs `14.29`) — the same harness defect as
  `_AI/BUGS.md` B-15, a stale choke list from the previous test.

> Do not "fix" these by weakening assertions. Either make the behaviour match
> the expectation, or update the expectation deliberately and explain why.

## Known-failing baseline (acceptance tests)

None. The package went 45 failures → 0 without weakening a single assertion to
hide a production bug: what came out was four real defects (B-10, B-11, B-13,
B-14), two harness defects (B-15, B-16) and a set of tests that were asserting
against the 22-unit sample world instead of their own generators.

## Architecture boundary tests

`tests.architecture.*` is separate from the above and **must stay green**:

- `ArchitectureBoundaryTest` — frozen baseline (Stage B); see
  `_AI/architecture/archunit-store/README.md`.
- `FramePipelineTest` — pins the top-level frame order (Stage C).

## Which world helper to use in a new test

There are exactly **two** ways to declare a world, and no more:

- `world(frames, eachFrame)` — the sample world: the 22 units of
  `mockOurUnitsArray()` against `mockEnemyUnitsArray()`. Use it when the code
  under test only needs *some* units and enemy units to be visible.
- `world(frames, ours, enemies, eachFrame)` — an explicit world: exactly these
  are our units, exactly those are the enemy's. Use it whenever the test is
  about particular units. `units(one)` wraps a single unit for the array
  argument.

Everything else about a world is a field or an override on the test class:
`options` (supply, anything else `Options` carries), `neutralInWorld`,
`initRace()`, `initEnemyRace()`. Set them before calling `world(...)`.

Both entry points land in `buildWorld(...)` (`AbstractWorldCreatingTest`),
which is the only code that actually builds and steps a world — tests never
call it.

The old six-overload `createWorld(...)` and the six `usingFake*()` wrappers
were removed in one sweep across ~120 call sites. They were one operation with
the arguments in six different orders, so nobody could remember which one to
reach for; `usingFakeOurs*` in particular was a second way to say "no frames",
which is what `world(1, ...)` means now.

Prefer world-free construction (plain `new FakeUnit(...)`) for assertions that
read only a unit's own type or fields — it is the cheapest and least
order-sensitive option.

Every helper that opens a static mock closes it before returning, so tests
stay order-independent. If a suite failure mentions "static mocking is
already registered", a helper leaks again — see `_AI/NOTES.md`.

## Why this matters

Being able to run the suite headlessly is what lets every later stage be
verified instead of guessed. Keep it working.
