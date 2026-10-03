# WO-B1 — evaluator evidence sweep (for BUGS.md B-1 / ADR 0006)

## Goal

A measurement table of what `AUnit.eval()` actually returns across a fixed
scenario matrix, written into `DOCS/adr/0006-combat-eval-scale.md` as evidence.
The ADR decision itself is human work; this order produces only numbers.

## Non-goals

- No production changes. No test changes (except the deleted scratch file).
  No threshold changes. No interpretation beyond arithmetic (a product is
  ~= 1 or it is not). If a number looks impossible, record it - do not
  "fix" anything.

## Prerequisites

- Read `DOCS/adr/0006-combat-eval-scale.md`, `_AI/BUGS.md` B-1/B-2/B-18, and
  the contract javadoc of `src/tests/acceptance/CombatEvaluatorTest.java`
  (reciprocal pairs, 9874 quiet value).
- Baseline: full suite green (your change adds no code, so it must stay so).

## Steps

1. Create the scratch probe `src/tests/unit/EvalSweepProbeTest.java` exactly
   like this (world scaffolding is the only thing that varies per scenario):
   ```java
   package tests.unit;

   import atlantis.units.AUnitType;
   import org.junit.jupiter.api.Test;
   import tests.acceptance.WorldStubForTests;
   import tests.fakes.FakeUnit;

   /** SCRATCH - evidence for ADR 0006. Delete after measuring. */
   public class EvalSweepProbeTest extends WorldStubForTests {
       @Test
       public void sweep() {
           // one world(...) per scenario below; print inside each lambda:
           //   System.err.println("TAG ourEval=" + ours.eval()
           //       + " theirEval=" + foe.eval()
           //       + " ourAbs=" + ours.combatEvalAbsolute()
           //       + " product=" + (ours.eval() * foe.eval()));
       }
   }
   ```
   `fake(...)` builds our unit, `fakeEnemy(...)` theirs; `fakeOurs(...)` /
   `fakeEnemies(...)` group them; `world(1, fakeOurs(...), fakeEnemies(...),
   () -> { ... })` runs one frame. Positions are tile-x with y implied
   (see any test in `CombatEvaluatorTest` for the shape).
2. Measure these scenarios, one `world(...)` each (distances in tiles matter -
   copy them from `CombatEvaluatorTest`, do not invent new ones):
   - mirror: our Marine 10 vs enemy Marine 11;
   - 4 of ours (10, 11, 11.5, 12) vs 1 Hydralisk 13.3 (read `marine` at 11.5);
   - 3 of ours (11.5, 11.6, 12) vs 2 Hydralisks (13.2, 13.3);
   - 3 Marines + Medic (11.5, 11.6, 11.7, 12) vs 1 Hydralisk 13.3;
   - 4 of ours (10, 11, 11.5, 12) vs 1 Sunken Colony 13 (in range);
   - 1 of ours (10) vs 1 Sunken Colony 23.5 (far, out of range);
   - Wraith 90 vs 2 Dragoons (92, 93);
   - Wraith 90 vs 2 fogged Photon Cannons (92, 93) discovered via
     `EnemyUnitsUpdater.weDiscoveredEnemyUnit`, **twice**: default race and
     with `initRace()` overridden to Protoss (copy the override from the
     B-2 entry - the Protoss additive tweaks change the sign);
   - solo Marine 10 vs no enemies (the 9874 quiet value).
3. Run: `bash scripts/run-tests.sh --select-class tests.unit.EvalSweepProbeTest`.
   Collect every printed line.
4. Delete the probe file.
5. Append the table to `DOCS/adr/0006-combat-eval-scale.md` under a new
   `## Evidence (measured <today's date>)` section at the end: one row per
   scenario with ourEval / theirEval / product / both absolutes. No prose
   beyond one line per anomaly (e.g. "product != 1", "negative").
6. Full suite green (nothing changed, so it must be).

## Verification

- The ADR diff adds only the evidence section; no production or test file
  changed (`git status` shows the ADR plus nothing else).
- Full suite counts match baseline.
- Commit message lists the scenario count and points at the ADR section.

## Stop rules

- A scenario from step 2 does not compile or throws: copy the shape from
  the nearest `CombatEvaluatorTest` case again, carefully. If it still
  fails, drop that row, note the drop in the commit, continue with the rest.
- Any urge to change a threshold, a tweak, or an expectation: stop. That is
  the ADR decision, not this order.
