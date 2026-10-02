# Testing

There is no build system (no Maven/Gradle). The project is an IntelliJ module
with vendored jars in `lib/`. Everything can still be compiled and tested
headlessly:

```bash
bash scripts/run-tests.sh                    # unit tests (default scope!)
bash scripts/run-acceptance-tests.sh         # acceptance tests
bash scripts/run-tests.sh --select-package tests    # everything at once
bash scripts/run-architecture-tests.sh       # architecture bounds only
```

`scripts/run-tests.sh` compiles all sources to `out/production/Atlantis` and
runs JUnit via `lib/junit-platform-console-standalone-1.10.0.jar`.

## Two scopes, two baselines — read this before trusting a green run

**The default run only executes `tests.unit`.** The whole
`tests.acceptance` package (world/squad/commander behaviour, ~119 tests) is
**not** run by `bash scripts/run-tests.sh`. That was true for the whole
architecture effort, and it hid a lot: acceptance tests were written against a
broken harness and were never executed, so nobody saw 44 failures sitting in
the tree. There is now `scripts/run-acceptance-tests.sh` for that scope, so it
is one command rather than a flag somebody has to remember.

| Scope | Command | Result (2026-10-03) |
|---|---|---|
| Unit (default) | `bash scripts/run-tests.sh` | **93 passing / 6 failing** of 99 |
| Acceptance | `bash scripts/run-acceptance-tests.sh` | **119 passing / 0 failing** |
| Everything | `bash scripts/run-tests.sh --select-package tests` | **222 passing / 6 failing** of 229 |
| Architecture | `bash scripts/run-architecture-tests.sh` | **7 passing / 0 failing** |

The 6 remaining failures are all in `tests.unit` and are listed below. Random
order (seeds 7, 42, 99) gives the identical failure set.

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

## Known-failing baseline (unit tests)

The suite is **not fully green yet**. Six `ATargetingTest` cases fail
(`targetsSunken`, `targetsMarinesOverBunkerYup`, `targetsCreepOverBaseOrDrones`,
`targetsUnfinishedSunken`, `targetsUnfinishedSunkenOverBaseOrDrones`,
`nearHydrasOverWounded`): targeting picks a different enemy than expected.

This is not a wrong assertion and not a wrong bot — the harness has no unit-type
data at all. `bwapi.UnitType.isFlyer()` answers `false` for every type, hit
points are placeholders (marine 40 vs 45, sunken colony 300 vs 150) and ranges
are off (dragoon 128 px vs 96). Injecting a unit-type table flipped these 6
into 10 failures, because the test expectations and the placeholders were
calibrated against each other. Fixing it properly needs real engine data.
Tracked as `_AI/NEXT.md` #29.

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
