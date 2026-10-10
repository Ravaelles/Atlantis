# Operational notes

Hard-won operational facts that do not belong anywhere else. Architecture lives in
`_AI/REVIEW.md` §16 and `DOCS/`.

## Waiting for a run: poll with a condition, never `sleep` (measured 2026-10-10)

- **Never `sleep <seconds>` to wait for a run to finish.** A blind sleep wastes the
  entire interval even when the run ends early, and it *hides* a stuck run by
  making "slow" look the same as "hung". The cost is real and was paid: most of
  one session went into `sleep 118` calls against OpenBW runs that never produced
  a verdict.
- Poll with a condition and a stop:
  ```bash
  for i in $(seq 1 10); do pgrep -x BWAPILauncher >/dev/null || break; sleep 2; done
  ```
  or start the run in the background and inspect it once its own budget elapsed.
- **A run that reaches its budget is stuck, not slow** (CONVENTIONS §17): 20 s is
  the normal budget for a single OpenBW test and 120 s is the mega-test ceiling.
  `scripts/run-openbw-e2e.sh` now prints `runtime: <n>s of <budget>s` and says
  `BUDGET EXHAUSTED` when a non-mega run spends its whole budget, so the failure is
  loud instead of quiet.

## Test runner hygiene

- `scripts/run-tests.sh` does **not** clean `out/` before compiling, so a deleted
  or renamed class keeps running as a stale `.class` file and corrupts counts (and
  can flip order-dependent tests). For any definitive run: `rm -rf out` first.
  Measured 2026-10-08: a deleted `Block8x8` class kept failing a test that no
  longer had a source.
- JUnit class execution order is not source order; a new test class can shift it.

## Scenario E2E probes are actuators, not sensors

- Directly invoking a manager inside a scenario loop, or running selection queries
  in the frame body, **changes the outcome it claims to observe**. Measured
  2026-10-03: a diagnostic block in `FourPoolDefenseTest` made a nexus survive 900
  frames the green test pins as lost; the same test without the block is green.
- Rule: instrument a scenario with pure unit-field reads only (`hp()`,
  `shields()`, `isAlive()`, positions) - never manager invocation, `attackUnit` or
  selection builders. Measure, delete the probes, then pin the numbers from the
  clean run.

## A test that has never been run is not a test

- `scripts/run-tests.sh` defaults to `tests.unit`, so `tests.acceptance` was never
  executed for a whole architecture effort and held 44 failures - including tests
  whose assertions could not fail.
- When adding infrastructure, run the widest scope at least once; when a class is
  "fixed", run it alone, in the full package, and with random order.
- **Order dependence hides in the strategy and the queue, not the unit list.**
  `Strategy.setTo()` returns early when the strategy is already the one asked for,
  and the test strategies are static singletons - so a previous test's queue and
  order flags survived. The reset lives once in `setUpTestLogic()`.

## Mockito static-mock leak (fixed)

- `@AfterEach` owns mock lifecycle. `cleanUp()` now `close()`s and nulls its
  `MockedStatic` fields; a `finally` on the happy path is not enough, because the
  unhappy path is exactly when the next test needs the thread back.
- A test that stops re-stubbing `everyNthGameFrame` silently answers `false`
  everywhere - 77 call sites depend on it, and a whole template stayed dead until
  it was stubbed.

## ArchUnit store mechanics

- `scripts/run-architecture-tests.sh` reads the **compiled** classes from `out/`.
  Running it after `rm -rf out` fails all rules with "failed to check any classes"
  - that is a missing build, not a regression.
- **A failed compile rewrites the store.** Fewer classes are seen, the
  auto-remove-stale-entries behaviour deletes the baseline, and the next
  *successful* run reports those missing entries as new violations. Recovery is
  `git checkout _AI/architecture/archunit-store/`. **Never run it while the
  compile is red, and check `git status` after any red architecture run.**
- Each run auto-removes stale entries but **never adds** new ones, and does not
  recreate a deleted store file.
- **Line numbers do not affect store matching** - origin-class to target-class
  does. Editing a method body or shifting lines is free; changing *which class a
  call targets* creates a new entry.
- Prefer deleting a dependency over re-freezing it: moving a class to the right
  package can delete baseline entries for real, where a rename just moves them.

## Java 8 applies to tests too

- The game jar is one `javac --release 8` over the **whole** tree, tests included,
  so a Java 9+ API in a test breaks the game build. Use `Arrays.asList`, not
  `List.of`. Only `ATargetingTest` (Nashorn) is excluded.

## Fat jar

- Canonical: `scripts/build-bot-jar.sh <output-jar> [--thin]`. Production bytecode
  must be major 52. Freshly compiled classes win over what a jar froze in; never
  append to a zip (duplicates shadow) - always rebuild fresh. Full detail is in
  the script's own header and `_AI/CHALLENGES/BuildAndLogging.md`.

## Reading Brood War's own unit data (a hunt that ended in "don't")

The real hit points and damage live in `units.dat` / `weapons.dat` inside the
installed archives; the hunt to read them instead of using the engine was the
wrong road, and this is why the note exists:

- the archives hide their file names behind Blizzard's decryption table, no local
  tool implements it, and the sectors are PKWARE-implode compressed;
- **the vendored jar was never a placeholder.** `unitTypesTest.cpp` asserts Marine
  40 hp, Ghost 45, Vulture 80, Goliath 125, Siege Tank 150, and the jar answers
  exactly that. The engine was the source all along; the first hand-written table
  invented twenty numbers that contradicted it.
- `UnitStatsTable` is now a correction list (currently empty) and
  `UnitStatsTableTest` pins the engine's values, so a jar swap fails loudly.
- If an independent source is ever needed, `3rdparty/openbw/openbw/data_loading.h`
  documents the exact layout of both files - the parser is easy, the archive is
  the whole problem.
