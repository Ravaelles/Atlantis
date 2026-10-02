# Cycle learnings (operational notes, not architecture)

Architecture lives in `_AI/REVIEW.md` §16 and `DOCS/`. This file records
hard-won operational facts that do not belong anywhere else.

## Test runner hygiene

- `scripts/run-tests.sh` does **not** clean `out/` before compiling.
  Deleted/renamed test classes keep running as stale `.class` files, which
  corrupts test counts and can flip order-dependent tests. For any
  definitive run: `rm -rf out` first.
- JUnit class execution order is not source order. New test classes can
  shift it.

## Mockito static-mock leak (fully fixed)

- World-based tests used to leave `Mockito.mockStatic(BaseSelect.class)`
  registered. Root cause was in `AbstractTestWithUnits.cleanUp()`, reached from
  `@AfterEach`: it called `MockedStatic.reset()`, which clears stubs but keeps
  the mock **registered in the thread**. Any test that failed inside
  `createWorld` (assertion error before the closing line) therefore leaked the
  mock into whatever ran next - which is why failures looked order-dependent.
- `cleanUp()` now `close()`s and nulls the field (reflection loop over public
  `MockedStatic` fields). Both world entry points (`createWorld`,
  `usingFakeOursEnemiesAndNeutral`) also close on the happy path, and
  `BaseSelectTest.neutralUnits` owns its mock via try-with-resources.
- Consequence: the suite is order-independent again - 11 failures are the
  documented pre-existing ones, with or without a failing world test in front
  of `TestWithUnits`.
- Rule of thumb: `@AfterEach` owns mock lifecycle. A `finally`/close at the end
  of a happy path is not enough, because the unhappy path is exactly when the
  next test needs the thread back.
- `MockEverything` statics (`aGame` etc.) are still unclosed suspects if an
  order-dependent failure ever returns.

## ArchUnit store mechanics (observed, vendored version)

- `scripts/run-architecture-tests.sh` reads the **compiled** classes from
  `out/`. Running it after `rm -rf out` makes all 7 rules fail with
  "failed to check any classes" — that is a missing build, not a regression.
  Run the unit suite (or any compile) first.
- Each test run **auto-removes stale entries** (violations that no longer
  exist) from `_AI/architecture/archunit-store/` but **never adds** new ones.
  It also does **not** recreate a deleted store file.
- Violation strings distinguish `Class[]` literals from constructor calls:
  - `X.class` literals in arrays are (mostly) invisible to the rules;
    `X::new` constructor references are flagged as dependencies. Converting
    reflection to factories therefore *surfaces* previously frozen edges —
    re-freeze explicitly and prove 1:1 mapping, do not silently absorb.
- **Line numbers do NOT affect store matching** (verified: `ErrorLog` entries
  in the store still carry `:18/:41/:49/:51/:59` while the code sits at
  `:19/:42/:50/:52/:60`, and the rule is green). What matters is
  origin-class → target-class/method. So editing a method body, adding
  imports or shifting lines is free; only *changing which class a call
  targets* (e.g. `A.saveToFile` → `AFile.saveToFile`) creates a new entry.
- Re-freeze procedure used: capture failing entries per rule → verify each
  maps to a moved (not new) edge → append exact lines → rerun to green →
  review `git diff` of the store.
- Prefer *deleting* the dependency over re-freezing it. The `AFile` extraction
  could have been a rename of 6 stored violations; putting the new class in
  `atlantis.util` instead deleted those 6 baseline entries for real.

## Java 8 target applies to tests too

- The game jar is built with a single `javac --release 8` over the **whole**
  tree, tests included. A test using a Java 9+ API therefore breaks the game
  build, not just itself.
- This bit us with `FramePipelineTest`: `List.of(...)` (Java 9+) had to become
  `Arrays.asList(...)`. Prefer Java 8 APIs in tests — `Arrays.asList`,
  `Collections.unmodifiableList`, anonymous classes over lambdas where the
  target matters.
- Only `ATargetingTest` is excluded from the jar build (needs Nashorn, removed
  in JDK 15).

## Fat-jar recipe (game runs)

- Canonical: `scripts/build-bot-jar.sh <base-jar> <out-jar>`.
- Production bytecode must be major 52 (container runs Corretto 8):
  compile with `--release 8`; only `ATargetingTest` (Nashorn) and
  `FramePipelineTest` (`List.of`) are excluded.
- Layering that bit us twice: freshly compiled classes win; JBWAPI-Rav's
  `bwapi`/`bwem` win over the stale ones frozen in old jars (compile-time
  vs runtime classpath shadowing caused `IllegalAccessError`/`NoSuchMethodError`
  in game). Never append to a zip (duplicates shadow); always rebuild fresh.
- `bots/AtlantisP` / `bots/AtlantisT` jars are build artifacts refreshed
  manually after verified cycles.

## Benchmarks (Stage J)

- Harness: `src/tests/benchmark/TreeConstructionBenchmark.java` (not a JUnit
  test) + `scripts/benchmark-trees.sh`. Measures full combat-tree frame work
  (construction + traversal) for 12 marines in the stub world; Mockito
  inflates absolutes, so only relative A/B comparisons count.
- Stage C payoff (same harness, worktree A/B): pre-reflection ~6.1ms vs
  post-factories ~4.6ms per frame per unit (**-25%**).
- Manager constructors are NOT pure: some read ambient statics (`Strategy`,
  game handle). Standalone (world-free) tree construction throws; the harness
  must run inside `createWorld`, which needs the JUnit `@BeforeEach`
  scaffolding replicated (`setUp()` call) when driven from `main()`.
- Fogged hp sentinel: `AbstractFoggedUnit.hp()` returns `-69` for unknown;
  snapshot projections stay faithful (no guessing) — representation of
  unknown state is open E-core design work.
