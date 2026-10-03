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
  editing one threshold.

## B-2 — `eval()` can be negative, which makes comparisons meaningless

- **Where:** `AtlantisJfap.calculateToRelativeScoreIfNeeded`.
- **Measured:** wraith vs two fogged photon cannons gives our eval **-0.3961**
  and enemy eval **169.06**; the product is -67 instead of 1. A negative value
  means the two Jfap side scores have opposite signs, i.e. the evaluator models
  one side as *losing outright*.
- **Why it matters:** every production guard of the form `unit.eval() >= 1.2`
  or `<= 2.5` silently changes meaning for a negative score. It is also why the
  old `CombatEvaluatorTest.takesIntoAccountFoggedUnits` could never pass: it
  asserted `ourEval > 0`.
- **How to settle it:** find which tweak in `AtlantisJfapModifier` produces a
  negative side score for fogged buildings, then decide whether a negative
  score is meaningful or a modelling artefact.

## B-4 — `AUnit.isOtherUnitShowingBackToUs()` reads like its opposite

- **Where:** `AUnit.isOtherUnitFacingThisUnit` / `isOtherUnitShowingBackToUs`.
- **What they do:** both ask about the **other** unit's angle, but against
  different reference directions — "is it facing us" within 1.1 rad of
  other→this, versus "is it showing its back" within 0.95 rad of this→other.
- **Why it matters:** reading the names as one question with one answer is how
  the old `AUnitTest.facingLogic` ended up asserting angles that the
  tolerances never produced. 46 call sites in production inherit the ambiguity.
- **Pinned by:** `AUnitTest.facingUsesTheTolerancesOfTheEngine` and
  `facingHelperAgreesWithTheRawVector`, which assert the real windows.

## B-8 — `APositionFinder` can still terminate the JVM

- **Where:** `atlantis/production/constructions/position/APositionFinder.java:113`
  (`System.exit(-1)` on an "Invalid race"), next to similar exits in
  `AtlantisRaceConfig`, `Atlantis` and `AKeyboard` (swept in NEXT.md #17).
- **Why it matters:** a leaf position finder deciding to kill the process is the
  same violation already fixed in `AFile.loadFile`.

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

> Fixed entries (B-3, B-5, B-7, B-10, B-11–B-16) were removed per the file's own
> rule — the git history is the archive. Numbers are never reused, so the next
> entry after B-16 is B-17.

## How to add an entry

```
## B-17 — the combat evaluator rates four Marines below one Sunken Colony

- **Where:** `AUnit.eval()` -> `AtlantisJfap` (a 60-frame simulation), reached
  from `CombatEvaluatorTest.fourMarinesLoseToOneSunkenColonyInThisEvaluator`.
- **Measured:** four Marines (45 hit points each, 6 damage, 4 tiles) against a
  lone Sunken Colony (150 hit points, one armour, 6 damage, 2.5 tiles) gives
  `eval() = 2.1705` - the evaluator says the Marines lose by more than two to
  one.
- **In the game they win.** Four Marines put about 20 damage per volley into a
  150-point building with one armour: roughly eight volleys, during which the
  Sunken returns 6 damage a shot at one Marine at a time. Losing two Marines to
  kill one defensive building is a trade Terran takes.
- **What is *not* established:** which part of the model is wrong. `eval()` is a
  simulation, not the old `Evaluate` heuristic, so the flat "defensive building
  present" bonus and the 1.3 military-building multiplier are not the cause. The
  candidates are the simulation's damage model, `AtlantisJfapModifier`'s
  building handling, or the tentacle's numbers - and the tentacle is one of the
  entries this project transcribed by hand (`NEXT.md` #29), so it cannot be
  ruled out from here either.
- **Why it matters:** `eval()` is what `weAreStronger()`, the fight/retreat
  decisions and `CombatEvaluator` all read. A doctrine that turns down a won
  fight is expensive, and it is invisible: nothing crashes, the bot just declines
  to attack.
- **How to settle it:** print the simulation's per-unit contribution for this one
  matchup and compare it with the arithmetic above; then either correct the model
  or, if the model is right and the doctrine is wrong, say so in a test name.
  Until then the number is pinned in the test, so fixing the evaluator fails the
  test and asks for the claim back.

## B-<n> — <one-line symptom>

- **Where:** file:line
- **Measured:** the numbers, and the command or test that produced them
- **Why it matters:** the concrete consequence in the game or in the suite
- **How to settle it:** what a fix would have to decide (not "fix the test")
```
