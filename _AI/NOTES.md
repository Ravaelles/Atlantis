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

## Mockito static-mock leak (fixed)

- World-based tests used to leave `Mockito.mockStatic(BaseSelect.class)`
  registered: `tearDown → cleanUp` only *resets* `MockedStatic` fields, it
  does not *close* them. Both world entry points (`createWorld`,
  `usingFakeOursEnemiesAndNeutral`) now close the mock on exit.
- There are **two** `baseSelect` static fields
  (`AbstractTestWithWorld` and `AbstractWorldCreatingTest`); treat both as
  suspect when debugging mock issues.
- `BaseSelectTest.neutralUnits` freeloaded on the leak and now owns its mock
  via try-with-resources.
- `MockEverything` statics (`aGame` etc.) are the remaining unclosed suspects
  if an order-dependent failure ever returns.

## ArchUnit store mechanics (observed, vendored version)

- Each test run **auto-removes stale entries** (violations that no longer
  exist) from `_AI/architecture/archunit-store/` but **never adds** new ones.
  It also does **not** recreate a deleted store file.
- Violation strings embed `Class[]`-vs-constructor-call distinctions AND
  source line numbers:
  - `X.class` literals in arrays are (mostly) invisible to the rules;
    `X::new` constructor references are flagged as dependencies. Converting
    reflection to factories therefore *surfaces* previously frozen edges —
    re-freeze explicitly and prove 1:1 mapping, do not silently absorb.
  - Touching a method can shift its line numbers and churn its entries.
- Re-freeze procedure used: capture failing entries per rule → verify each
  maps to a moved (not new) edge → append exact lines → rerun to green →
  review `git diff` of the store.

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
