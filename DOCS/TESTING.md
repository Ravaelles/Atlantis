# Testing

There is no build system (no Maven/Gradle). The project is an IntelliJ module
with vendored jars in `lib/`. Everything can still be compiled and tested
headlessly:

```bash
bash scripts/run-tests.sh                              # unit tests
bash scripts/run-tests.sh --select-package tests       # all tests
bash scripts/run-architecture-tests.sh                 # architecture bounds only
```

`scripts/run-tests.sh` compiles all sources to `out/production/Atlantis` and
runs JUnit via `lib/junit-platform-console-standalone-1.10.0.jar`.

## Known-failing baseline (unit tests)

The suite is **not fully green yet**. As of the architecture work, running
`tests.unit` gives ~62 passing / ~11 failing. These failures are **pre-existing
assertion mismatches**, unrelated to the architecture stages; they are kept
visible on purpose rather than hidden:

| Test | Symptom |
|---|---|
| `ATargetingTest` (7 cases) | targeting picks a different enemy than expected |
| `ProtossRetreatTest` (`goonsVsCannons`, `retreatGoonsVsHydras`) | expected retreat. `true`, was `false` |
| `ProtossSmallRetreatTest` (`noRetreatWhenMeleeAdvantage`, `retreatWhenNoMeleeAdvantage`) | expected `true`, was `false` |
| `ChokeTest.distToChokes` | expected `2.0`, was `14.29` (likely needs real map data) |

> Do not "fix" these by weakening assertions. Either make the behaviour match
> the expectation, or update the expectation deliberately and explain why.

## Architecture boundary tests

`tests.architecture.*` is separate from the above and **must stay green**:

- `ArchitectureBoundaryTest` — frozen baseline (Stage B); see
  `_AI/architecture/archunit-store/README.md`.
- `FramePipelineTest` — pins the top-level frame order (Stage C).

## Why this matters

Being able to run the suite headlessly is what lets every later stage be
verified instead of guessed. Keep it working.
