# Testing

There is no build system (no Maven/Gradle). The project is an IntelliJ module
with vendored jars in `lib/`. Everything can still be compiled and tested
headlessly:

```bash
bash scripts/run-tests.sh                              # unit tests (default scope!)
bash scripts/run-tests.sh --select-package tests       # unit + acceptance
bash scripts/run-architecture-tests.sh                 # architecture bounds only
```

`scripts/run-tests.sh` compiles all sources to `out/production/Atlantis` and
runs JUnit via `lib/junit-platform-console-standalone-1.10.0.jar`.

## Two scopes, two baselines — read this before trusting a green run

**The default run only executes `tests.unit`.** The whole
`tests.acceptance` package (world/squad/commander behaviour, ~120 tests) is
**not** run by `bash scripts/run-tests.sh` unless you pass
`--select-package tests`. That was true for the whole architecture effort, and
it hid a lot: acceptance tests were written against a broken harness and were
never executed, so nobody saw 44 failures sitting in the tree.

| Scope | Command | Result (2026-10-02) |
|---|---|---|
| Unit (default) | `bash scripts/run-tests.sh` | **87 passing / 11 failing** of 98 |
| Everything | `bash scripts/run-tests.sh --select-package tests` | **212 passing / 11 failing** of 223 |
| Architecture | `bash scripts/run-architecture-tests.sh` | **7 passing / 0 failing** |

The acceptance package is **fully green** (115/115) after the harness fixes
recorded in `_AI/BUGS.md` B-13…B-16. The 11 remaining failures are all in
`tests.unit` and are listed below.

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

The suite is **not fully green yet**. These failures are **pre-existing
assertion mismatches**, unrelated to the architecture stages; they are kept
visible on purpose rather than hidden:

| Test | Symptom |
|---|---|
| `ATargetingTest` (6 cases) | targeting picks a different enemy than expected |
| `ProtossRetreatTest` (`goonsVsCannons`, `goonsVsGoons_3v4`, `retreatGoonsVsHydras`) | expected retreat. `true`, was `false` |
| `ProtossSmallRetreatTest` (`noRetreatWhenMeleeAdvantage`, `retreatWhenNoMeleeAdvantage`) | expected `true`, was `false` |

`ChokeTest.distToChokes` used to be on this list (`2.0` vs `14.29`); it was
the same harness defect as everything else in `_AI/BUGS.md` B-15 - a stale
choke list from the previous test - and passes now.

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

Two families, pick by need — do not add new ones:

- `createWorld(...)` (`AbstractTestWithWorld` / `AbstractWorldCreatingTest`) —
  full stub world with advancing frames (`A.now` moves). Use when the code
  under test depends on frame progression (command throttles, TTLs, multi-
  frame behaviour). Overloads differ only in how units are supplied
  (single/array/Callable, default enemies); they all funnel into one
  implementation.
- `usingFakeOurs*` / `usingFakeEnemy` / `usingFakeNeutral`
  (`AbstractTestWithUnits`) — mocks only, no frames advance. Use for pure
  unit-level assertions. Prefer world-free construction (plain
  `new FakeUnit(...)`) when even mocks are unnecessary.

Every helper that opens a static mock closes it before returning, so tests
stay order-independent. If a suite failure mentions "static mocking is
already registered", a helper leaks again — see `_AI/NOTES.md`.

## Why this matters

Being able to run the suite headlessly is what lets every later stage be
verified instead of guessed. Keep it working.
