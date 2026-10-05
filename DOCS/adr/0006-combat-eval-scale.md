# ADR 0006 — Combat evaluation scale (PROPOSED, not accepted)

- **Status:** Proposed. This ADR frames the decision `_AI/BUGS.md` B-1 asks
  for; it does not take it. Accepting needs a scenario sweep over the real
  evaluator plus game runs.
- **Context:** `AUnit.eval()` returns `enemyScore / (ourScore + 0.001)` from a
  ~60-frame JFAP simulation - an unbounded ratio over cost-like negative scores,
  where a side's score is what it lost, so a **higher** number means our side is
  stronger (see `CombatEvaluatorTest.higherEvalMeansWeAreBetter`). 228 production
  call sites compare it against thresholds in
  `[0.6, 3]`, while the javadoc promises "1.0 = even, 1.3 = 30% stronger".
  Three known distortions interact with any scale decision:
  - B-2: additive Protoss tweaks push near-zero ratios below zero (measured
    -0.3789 for a 0.0211 fight), which no `eval >= y` or `eval <= y` guard can
    read - floored since (2026-10-04);
  - B-18: the 60-frame window scores long fights as even (0.98) that the
    colony wins outright;
  - the no-enemy reading is 9874.0 (and -1.0 absolute), which every
    `eval >= 2` guard reads as safe.
  Verified invariants that constrain the design: mirror pairs are reciprocal
  (`ourEval * theirEval ~= 1`), and both JFAP side scores are cost-like
  negative, so the raw ratio is never negative - only the tweaks break it.
- **Decision (recommended):** do NOT re-normalise the ratio. Any monotonic
  re-map preserves order, so all 228 thresholds would need re-mapping anyway -
  same churn as re-deriving, with none of the three distortions fixed. Instead:
  1. floor the tweaked eval at 0 (fixes B-2 at the source; **done** 2026-10-04,
     `CombatEvalScale.FLOOR` = 0.01);
  2. document 9874.0 explicitly as "no threat in reach", not "infinitely
     strong", and audit the guards that must distinguish "safe" from "no
     data" (a dozen, not 228 - most high-eval reads are harmless when quiet);
  3. leave the horizon (B-18) to a simulation change, not a scale change.
- **Consequences:** thresholds keep their tuned meanings; the ratio stays
  unbounded above (fine - it is a ratio); the sign and no-data cases become
  total. All three steps change fight behaviour and need game runs.
- **Follow-up regardless of the decision:** freeze the WO-B1 evidence table
  as a golden test (like the ArchUnit store): any evaluator change that moves
  a number fails the build until the table is deliberately re-frozen. The
  fiction-table episode proved a green suite without that ratchet is not
  evidence.
- **Alternatives rejected:** re-normalising to a bounded scale (churn without
  fixing B-2/B-18/9874); re-deriving all thresholds from a sweep (re-tuning
  the bot by hand - years of game knowledge, no oracle).

## Evidence (measured 2026-10-03)

Procedure: `_AI/work-orders/WO-B1-eval-evidence-sweep.md`. One frame per
scenario, positions in tiles, engine data from `lib/JBWAPI-Rav.jar`, values
rounded to four decimals. `ourAbs` / `theirAbs` are the two JFAP side scores
(cost-like, negative).

| scenario | ourEval | theirEval | product | ourAbs | theirAbs |
|---|---|---|---|---|---|
| mirror: 1 marine vs 1 marine | 1.0000 | 1.0000 | 1.0000 | -88 | -88 |
| 4 marines vs 1 hydralisk | 7.5557 | 0.1324 | 1.0000 | -45 | -340 |
| 3 marines vs 2 hydralisks | 3.3694 | 0.2968 | 1.0000 | -111 | -374 |
| 3 marines + medic vs 1 hydralisk | 21.2513 | 0.0500 | **1.0626** | -16 | -340 |
| 4 marines vs 1 sunken colony, in range | 0.9796 | 1.0208 | 1.0000 | -98 | -96 |
| 1 marine vs 1 sunken colony, out of range | 1.0010 | 1.0010 | **1.0020** | -1 | -1 |
| wraith vs 2 free dragoons | 0.0217 | 46.0951 | 1.0001 | -507 | -11 |
| wraith vs 2 fogged photon cannons (Terran) | 0.0211 | 47.5030 | 1.0001 | -760 | -16 |
| wraith vs 2 fogged photon cannons (Protoss) | **-0.3789** | 47.5030 | **-18.0011** | -760 | -16 |
| 1 marine, no enemies in reach | 9874.0000 | - | - | -1 | - |

Anomalies, one line each, arithmetic only:

- `3 marines + medic vs 1 hydralisk`: product 1.0626, not 1. The absolute
  scores are -16 and -340, a ratio of 21.25 - and the reverse direction reads
  0.0500, whose product with 21.2513 is 1.0626. A medic in the group is the
  only difference from the 4-marines row above it (product 1.0000).
- `1 marine vs 1 sunken colony, out of range`: both sides read ~1.0 and both
  absolutes are -1, i.e. nothing happened in the window; the product is 1.0020
  because -1 / -0.999 is not exactly its own inverse.
- `wraith vs 2 fogged photon cannons (Protoss)`: our eval is negative and the
  pair is not reciprocal (product -18.0011). Same fight as the Terran row,
  whose product is 1.0001 - the Protoss additive tweaks move our side only.
- B-2's first draft quoted 0.0066 / 152.03 for the cannon side of this
  fight. Those were measurements from before the engine-data correction
  (a 150-hp cannon era); with engine data the Terran ratio reads 0.0211 and
  the cannon 47.5030 at the same test coordinates. Old numbers, superseded -
  and a reminder that every number in this table needs coordinates to be
  re-measured (positions in tiles: wraith 90, cannons/dragoons 92-93,
  marines/hydras/sunken as in `CombatEvaluatorTest`).

## Evidence (measured 2026-10-04)

Procedure: `_AI/work-orders/WO-B1-eval-evidence-sweep.md`, same scenarios and
coordinates as above, but one `world()` per test method (two scratch classes,
`EvalSweepProbeTest` at the default race plus `EvalSweepProbeProtossTest` with
`initRace()` overridden - the stub-world rules in `_AI/NOTES.md` forbid sharing
state between measured runs), both deleted after measuring. `eval()` now
carries the B-18 hedge (-0.3 our side) and the B-2 floor (0.01); `theirEval`
is untouched by either. Values rounded to four decimals.

| scenario | ourEval | theirEval | product | ourAbs | theirAbs |
|---|---|---|---|---|---|
| mirror: 1 marine vs 1 marine | 0.7000 | 1.0000 | 0.7000 | -88 | -88 |
| 4 marines vs 1 hydralisk | 7.2557 | 0.1324 | 0.9603 | -45 | -340 |
| 3 marines vs 2 hydralisks | 3.0694 | 0.2968 | 0.9110 | -111 | -374 |
| 3 marines + medic vs 1 hydralisk | 20.9513 | 0.0500 | 1.0476 | -16 | -340 |
| 4 marines vs 1 sunken colony, in range | 0.6796 | 1.0208 | 0.6938 | -98 | -96 |
| 1 marine vs 1 sunken colony, out of range | 0.7010 | 1.0010 | 0.7017 | -1 | -1 |
| wraith vs 2 free dragoons | 0.0100 | 46.0951 | 0.4610 | -507 | -11 |
| wraith vs 2 fogged photon cannons (Terran) | 0.0100 | 47.5030 | 0.4750 | -760 | -16 |
| wraith vs 2 fogged photon cannons (Protoss) | 0.0100 | 47.5030 | 0.4750 | -760 | -16 |
| 1 marine, no enemies in reach | 9873.7000 | - | - | -1 | - |

Anomalies, one line each, arithmetic only:

- Every `ourEval` is exactly 0.3 below the 2026-10-03 value, except the three
  rows where that would go at or below zero: `wraith vs 2 free dragoons`
  (0.0217 - 0.3), `wraith vs 2 fogged cannons (Terran)` (0.0211 - 0.3) and
  `(Protoss)` (-0.3789) all read the 0.01 floor instead.
- The quiet value reads 9873.7, i.e. the hedge also applies to the no-threat
  reading (9874.0 - 0.3); every `eval >= 2` guard still reads it as safe.
- No product is ~1 anymore (mirror 0.7000, medic 1.0476); reciprocity now lives
  in `ownCombatEvalRelative()`, which the tests assert, not in `eval()`.
- All absolutes are identical to the 2026-10-03 table: the engine data did not
  move, only the doctrine around it did.

## Audit step 2 - "safe" versus "no reading" (started 2026-10-05)

Step 2 of the decision above: document 9874.0 as "no threat in reach" and audit the
guards that must be able to tell it apart from a real reading. The *machinery* is in
place; the audit itself is not finished, and this section says exactly how far it got.

**Why it can be told apart at all.** `AUnit.hasEnemyForEval()` asks the evaluator's own
question ("is an enemy in reach with a weapon that is not immobilized"), and
`CombatEvalScale.NO_ENEMY_IN_REACH` (9874.0) is the quiet reading as a named value -
`eval()` answers 9873.7 for our own units because the hedge applies to it like any other.
`AtlantisJfap.noEnemiesNear()` now reads the unit's predicate rather than repeating it,
so there is one definition. `EvalHasReadingTest` (3 tests) pins both directions,
including that an enemy without a weapon is not a reading.

**The size of the audit.** 195 production comparisons of `eval()` against a numeric
threshold: **105 are "our side is at least X"** (higher is safer for us, so the quiet
reading passes them all) and 90 are "at most X" (the quiet reading fails them, which is
the harmless direction - a unit that is not retreating because nothing is in reach is
right). So the audit set is the 105, and within it the risky subset is the guards whose
subject is a *chosen* target rather than what happens to be nearby: there the reading can
be about an entirely different fight, or about none.

**Row 1, measured** (`tests.acceptance` scratch probe, deleted after measuring):
`ProtossMissionDefendAllowsToAttack.allowsToAttackEnemyUnit(enemy)` -
`if (unit.eval() >= 1.3) return Decision.TRUE;`. A zealot on a defend mission whose
assigned target is **30 tiles away** (`NEAR_DIST` is 15, so it is outside
`enemiesNear()`) reads `eval() = 9873.7` with `hasEnemyForEval() == false` and gets
`Decision.TRUE`: it will leave the spot it is defending for a target it has no reading
on. The guards above it (`PreventChasingEnemyWorkerInBase`,
`PreventChasingEnemyLingInBase`) cover two specific target kinds, which is why this is a
per-site question and not a blanket change - and why changing it is a strategy decision
rather than a bug fix. Recorded, not changed; tracked as NEXT #41.

**What is left:** the rest of the 105, one class at a time, with the same three-line
measurement (reading, hasReading, decision) per row. Nothing in the audit so far says a
threshold is wrong - it says which ones cannot see the difference between "strong" and
"nothing to fight".
