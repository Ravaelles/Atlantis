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
| Everything | `bash scripts/run-tests.sh --select-package tests` | **177 passing / 45 failing** of 222 |
| Architecture | `bash scripts/run-architecture-tests.sh` | **7 passing / 0 failing** |

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

Run that whenever you touch test infrastructure. **4 classes are still
order-dependent** (`CombatEvaluatorTest`, `RequestBuildingNearTest`,
`ManagerTest`, `starengine.DragoonsVsDragoonsTest`): the total stays 45 across
seeds, but which tests fail moves. Everything else fails deterministically.

## Known-failing baseline (unit tests)

The suite is **not fully green yet**. These failures are **pre-existing
assertion mismatches**, unrelated to the architecture stages; they are kept
visible on purpose rather than hidden:

| Test | Symptom |
|---|---|
| `ATargetingTest` (6 cases) | targeting picks a different enemy than expected |
| `ProtossRetreatTest` (`goonsVsCannons`, `retreatGoonsVsHydras`) | expected retreat. `true`, was `false` |
| `ProtossSmallRetreatTest` (`noRetreatWhenMeleeAdvantage`, `retreatWhenNoMeleeAdvantage`) | expected `true`, was `false` |
| `ChokeTest.distToChokes` | expected `2.0`, was `14.29` (likely needs real map data) |

> Do not "fix" these by weakening assertions. Either make the behaviour match
> the expectation, or update the expectation deliberately and explain why.

## Known-failing baseline (acceptance tests)

45 failures in the never-run package, listed per class in `_AI/NEXT.md` (#22).
Two harness bugs found while fixing `AUnitTest` accounted for 10 of them; the
rest still need per-class triage. Do not add to this list without saying which
commit added the failure.

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
