# Known bugs and behaviour debt

Not a TODO list — this file records things that are **wrong or misleading
today**, as opposed to work that is merely not started (that lives in
`_AI/NEXT.md`). Every entry states what was measured, how it was measured, and
what the consequence is.

An entry leaves this file when the behaviour is either fixed or deliberately
documented in code with a comment. The closure goes into the commit message.

## B-1 — `AUnit.eval()` has no documented scale, and 228 call sites assume one

- **Where:** `atlantis/units/AUnit.java:2396` (javadoc) vs
  `atlantis/combat/eval/AtlantisJfap.java:90`.
- **What the code does:** `eval()` returns
  `enemyScore / (ourScore + 0.001)` — an **unbounded ratio**. For our unit a
  lower value means we are stronger; for an enemy unit the same formula runs
  from its side, so a higher value means we are stronger. When no enemy is in
  reach it returns **9874.0**, and `-1.0` absolute.
- **What the javadoc promises:** "1.0 means our army is as strong, 1.3 means our
  army is 30% stronger than enemy".
- **Measured (one world each, `CombatEvaluatorTest` scenarios):**
  | scenario | our eval | their eval |
  |---|---|---|
  | 1 marine + 1 wraith vs 2 hydras + zealot | 0.0182 | 55.03 |
  | 4 marines vs 1 hydra | 7.5557 | 0.1324 |
  | 3 marines vs 2 hydras | 3.3694 | 0.2968 |
  | 4 marines + medic vs 1 hydra | 21.2513 | 0.0500 |
  | 1 marine vs 1 sunken colony (far) | 0.7210 | 1.0010 |
  | wraith vs 2 free dragoons | 0.0197 | — |
- **Why it matters:** 228 production call sites compare against thresholds in
  `[0.6, 3]` (`ShouldStopRunning`, `TooFarFromFocusPoint`,
  `TerranMarineLongNotAttacked`, `ProtossLowEval`, …). Whether those thresholds
  still mean "we are slightly weaker" after the Jfap retune is **unverified**,
  and the "no enemy nearby" value of 9874 makes every `eval >= 2` trivially
  true in a quiet part of the map.
- **How to settle it:** not a test change. Needs a decision (ADR) whether the
  evaluator should be re-normalised to the documented scale, or the thresholds
  re-derived from a scenario sweep over the real evaluator. Do not "fix" it by
  editing one threshold. Evidence first, decision second: the measurement
  procedure is `_AI/work-orders/WO-B1-eval-evidence-sweep.md`, the draft to
  fill is `DOCS/adr/0006-combat-eval-scale.md`.

## B-2 — additive Protoss tweaks can push `eval()` below zero

- **Where:** `AUnit.freshCombatEvalRelative` ->
  `ProtossJfapTweaksConsiderChokesEtc.apply` (adds up to -0.4: -0.1 for enemy
  buildings near, up to -0.3 for combat buildings, choke/cohesion/retreat
  terms on top), applied to the `enemyScore / (ourScore + 0.001)` ratio.
- **Measured** (scratch probe, since deleted): lone Wraith against two
  (fogged, i.e. full-health) Photon Cannons. As Terran the ratio is **0.0066**,
  absolute **-760** - the wraith is utterly doomed and the number says so. As
  Protoss the same fight scores **-0.3934** = 0.0066 - 0.1 - 0.3. The enemy
  side is unaffected (cannon eval **152.03** both ways; the pair is even
  reciprocal as Terran: 0.0066 x 152 ~= 1), so the penalties break both the
  sign and the reciprocity, exactly when the raw ratio is near zero.
- **Why it matters:** every production guard of the form `unit.eval() >= 1.2`
  or `<= 2.5` silently changes meaning for a negative score - and in the
  dangerous direction: `eval() <= 2.5` ("we are fine") is *true* for -0.39,
  for a unit that loses 150-to-1. It is also why the old
  `CombatEvaluatorTest.takesIntoAccountFoggedUnits` could never pass: it
  asserted `ourEval > 0`.
- **History:** this entry used to blame "two Jfap side scores with opposite
  signs". The scores are both negative (cost-like) and their ratio is fine;
  the sign break happens one layer up, in the additive tweaks. The old
  -0.3961/169.06 numbers were measured with a fiction table; the mechanism
  above reproduces with engine data.
- **How to settle it:** make the tweaks sign-safe - floor the tweaked eval at
  0, apply them multiplicatively, or skip them when the raw ratio is already
  near zero. Any of those changes live fight/avoid behaviour, so it needs a
  game run, not just the suite.

## B-8 — `APositionFinder` can still terminate the JVM

- **Where:** `atlantis/production/constructions/position/APositionFinder.java:113`
  (`System.exit(-1)` on an "Invalid race"), next to similar exits in
  `AtlantisRaceConfig`, `Atlantis` and `AKeyboard` (swept in NEXT.md #17).
- **Why it matters:** a leaf position finder deciding to kill the process is the
  same violation already fixed in `AFile.loadFile`.
- **Update:** the leaf now throws `IllegalStateException` instead of exiting.
  The branch is unreachable in any working game (the race is always one of the
  three above it), so no reachable behaviour changes; callers already handle
  the race finders' nulls. Keep this entry until a game run confirms no exit
  path regressed. The remaining three exits are triaged in #17: `Atlantis`
  is the shutdown path (exiting is its job), the other two are fail-fast at
  startup and need a game run to touch safely.

## B-9 — the queue never detects unit/building progress on its own

- **Where:** `IsOrderInProgress.isInProgress` and `IsOrderCompleted.isCompleted`
  (both have their **unit branch commented out**, with the notes "this will
  happen in OnOurUnitCreated / OnOurNewUnitCompleted").
- **Measured:** after firing `OnOurNewUnitCompleted` for a completed depot the
  order does become FINISHED (and `IsReadyToProduceOrder` refuses to move a
  FINISHED order back), but for an **unfinished** building the order flips from
  IN_PROGRESS back to READY_TO_PRODUCE on the next `Queue.refresh()` - the only
  thing that keeps it IN_PROGRESS in a real game is the linked `Construction`
  with `buildingUnit().hp() > 0`.
- **Consequences:**
  1. Correctness of the queue silently depends on the invariant "every
     in-progress building has a construction with hp > 0". Nothing checks it,
     and a missed construction silently turns into duplicate production
     (order goes back to ready while the building is still rising).
  2. `Queue2Test`, `CountInQueueTest`, `Queue3Test` could not observe queue
     states by adding units to the mocked list; they now fire the engine
     listeners, which is the faithful simulation.
- **How to settle it:** decide whether the queue should verify unit progress
  itself (re-enable the commented branch) or whether the construction invariant
  should be asserted somewhere. The first is safer; the second documents the
  coupling. Either way the invariant deserves a name and a test.
- **Test (done):** `tests/acceptance/QueueInProgressInvariantTest` states the
  invariant as `isHeldByALivingConstruction()` and checks it after a real
  `Queue.refresh()`, then removes the one link and shows the order falling back
  to READY_TO_PRODUCE. So the second half of the question - the name and the test -
  is answered; what is still open is the first half, the behaviour change, which
  needs a game run to confirm no building is produced twice.

## B-18 — the combat evaluator only sees the opening of a long fight

- **Where:** `AUnit.eval()` -> `AtlantisJfap` (a ~60-frame simulation),
  reached from
  `CombatEvaluatorTest.fourMarinesAgainstOneSunkenColonyScoresAboutEven`.
- **Measured:** four Marines (40 hit points, 6 damage, 4 tiles) starting
  inside their own reach of a lone Sunken Colony (300 hit points, 40 damage a
  shot, 7 tiles) score `eval() = 0.98` - about even, a hair our way.
- **Over a full fight the colony wins that damage race.** Four 40-point
  Marines put ~20 a volley into 300 hit points (about fifteen volleys); the
  colony one-shots a Marine a shot and needs four shots. The marines are all
  dead around frame 130 having dealt about half of what the colony needs -
  and that is the *kind* variant, with no approach under fire. The 60-frame
  window only ever sees the opening exchange, where the marines are still at
  full strength, so it reports even.
- **Why it matters:** `eval()` is what `weAreStronger()`, the fight/retreat
  decisions and `CombatEvaluator` all read, and every one of them treats
  ~1.0 as "safe to engage". A doctrine that reads a truncated window as a
  won fight is expensive, and it is invisible: nothing crashes, the bot just
  walks in.
- **How to settle it:** either score the projected outcome (extend the window
  past the longest relevant kill time, or extrapolate), or teach the callers
  that `eval ~ 1` against defensive buildings means "undecided, not safe".
  Until then the number is pinned in the test, so touching the horizon fails
  the test and asks for the claim back.
- **History:** this entry replaces B-17, whose premise ("the evaluator rates
  marines below a sunken they beat") was measured with a fiction table that
  had the colony at 150 hit points with a 6-damage, 2.5-tile tentacle. A
  weaker colony scoring *worse* for us should have smelled; with engine data
  the same scenario gives 0.98. The defect was in the data, not the model.

## B-19 — workers flee a packed rush they should help kill

- **Where:** `WorkerDefenceManager` ordering (`WorkerDefenceRun` 3rd,
  `WorkerDefenceFight` 5th) + the 300-frame run/fight lockout
  (`WorkerDefenceFightCombatUnits:23`, `WorkerHelpCombatUnitsFight:30`).
- **Measured** (`FourPoolDefenseTest`, Protoss base vs 6 lings, 900 frames):
  the cannon fights and dies ~150, the zealot ~250, both trading two lings;
  the four probes never engage the packed lings and the nexus falls ~880.
  `WorkerDefenceRun` fires on 3 lings within 3 tiles, the fled probes are
  then locked out of fight *and* help for 300 frames, and the help path
  additionally skips every fifth probe (`id % 5 <= 1`) - in a base defense
  that combination means nobody ever supports the cannon while it dies three
  tiles away.
  Reproduced by the 9pool twin (`NinePoolDefenseTest`, 8 lings from x=32,
  2026-10-03): cannon ~163, zealot ~214, nexus ~631, three of four probes
  killed off at the very end, one escapes - the mechanism is scenario
  independent, and more attackers still cost the base earlier despite the
  later arrival.
- **Why it matters:** fleeing is right in the field and fatal at home: once
  the army is dead there is nothing left between the lings and the nexus,
  and the probes that could have turned the cannon fight (4 x 5 damage into
  35-hp lings) spent it running. A base under attack needs "help the static
  defense", not "run to another region".
- **How to settle it:** teach the run/fight arbitration about base defense
  (enemy inside the base, or a cannon fighting nearby, suppresses Run and
  the lockout) - then watch the scenario flip. Until then the numbers above
  are pinned in the test, so touching the arbitration fails the test and
  asks for the claim back. Needs a game run before any production change
  counts as verified.
- **Update (2026-10-03, scenario flip measured; game run still owed):** three
  things, all in `WorkerDefence*`:
  1. `WorkerDefenceHelpCannon.applies()` AND-ed
     `laterInGameAgainstRangedEnemiesJustIgnore()` **positively** - the predicate
     that says "ignore", un-negated, so the manager only ever fired after 9.5 game
     minutes or against 5-6+ ranged enemies, i.e. never during the melee rush it
     exists for. Now negated, as the name says.
  2. `atlantis.units.BaseUnderAttack` is where "the attack is at home" is decided,
     and `WorkerDefenceRun`, `WorkerDefenceFightCombatUnits` and
     `WorkerHelpCombatUnitsFight` consult it: Run suppresses itself for a worker
     that can help (artillery attackers - Reaver/Tank/Lurker - still outrule
     holding ground), and the two fight managers lift the 300-frame run lockout
     and the `id % 5` / `id % 3` skips while the base is being hit.
  3. The scenarios' baselines were wrong, not just stale: they had been measured
     with diagnostics inside the frame loop, which changes the outcome
     (NOTES.md). Re-measured from a clean run, the 4pool **holds** (lings dead
     between frames 51 and 249, cannon on 10 hp, nexus untouched, one strike per
     probe where there used to be none) and the 9pool still loses the base but
     trades three lings instead of two, with six probe strikes instead of zero.
     Fixing the world harness was part of it: the stub world never removed dead
     units from its unit lists, so corpses stayed selectable and the bot spent
     frames attacking them.

  What is left is the same as when this entry was written: a game run. The
  stub world's physics is documented harness rules, not the game, so "the
  scenario flips" is evidence that the arbitration changed, not that the bot
  survives a real 4pool.

## How to add an entry

```
## B-<n> — <one-line symptom>

- **Where:** file:line
- **Measured:** the numbers, and the command or test that produced them
- **Why it matters:** the concrete consequence in the game or in the suite
- **How to settle it:** what a fix would have to decide (not "fix the test")
```
