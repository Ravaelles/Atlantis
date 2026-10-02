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

## B-3 — `AUnit.shieldPercent()` is `NaN` for units without shields

- **Where:** `AUnit.shieldPercent()` = `100 * shields() / maxShields()` with no
  zero guard.
- **Measured:** a Terran marine returns `NaN`; a Protoss zealot returns 100.0.
- **Why it matters:** harmless today only because every production caller checks
  `maxShields()` first. A naive use silently poisons every comparison
  (`NaN >= 0` is false, `NaN == NaN` is false), which is the worst failure mode
  for a "percentage" getter.
- **Pinned by:** `AUnitTest.shieldsOnAUnitThatHasNone` (deliberately asserts the
  current behaviour so a fix is a conscious change).

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

## B-5 — `AUnit.cooldownRemaining()` had a guard that made tests unfalsifiable

- **Fixed** (`da2b3f45`), listed here because the pattern is worth naming: a
  redundant `u == null` check in a read path is not a no-op for the test
  suite, it turns "measured" into "constant". When a getter looks suspiciously
  constant in tests, look for a short-circuit in the getter.

## B-6 — the Protoss position finder cannot place anything in the stub world

- **Where:** `atlantis/production/constructions/position/PositionFulfillsAllConditions`
  (14 Protoss conditions), reached from `ProtossPositionFinder.findStandardPositionFor`.
- **Measured** (`RequestBuildingNearTest` scenario, main nexus at 9,46, natural
  at 16,14, one probe worker, no pylon nearby): a direct
  `CanPhysicallyBuildHere.check(worker, Protoss_Pylon, position)` **accepts 90**
  candidate tiles around the natural, yet
  `RequestBuildingNear.constructionOf(Protoss_Pylon).near(natural).request()`
  returns `null` with `_STATUS = "[Testing] Can't physically build here"`.
  So one of the other thirteen conditions rejects every candidate.
- **Consequence:** "protect a base that has no pylon" cannot be tested at all,
  which is exactly the behaviour the Protoss opening depends on. Both affected
  tests are pinned at the weaker contract that does hold (see
  `RequestBuildingNearTest`), with this entry as the reason.
- **How to settle it:** binary-search the conditions by calling
  `PositionFulfillsAllConditions.doesPositionFulfillAllConditions` for one of
  the 90 accepted tiles and printing which check flips it. Prime suspects:
  `ProtossForbiddenByStreetGrid` (needs `moduloX == 2` lattice positions),
  `IsProbablyInAnotherRegion` and `ProtossTooCloseToRegionBoundaries` (both
  need `ARegion`, which is a stub without a BWEM area).

## B-7 — `ProtossTooCloseToRegionBoundaries` can never fire

- **Where:** `.../position/protoss/ProtossTooCloseToRegionBoundaries.java:14`.
- **The code:** `if (!building.isPylon()) return false;` followed by
  `if (!building.isCannon()) return false;`. A building that is both a pylon
  and a cannon does not exist, so the whole condition is dead code.
- **Why it matters:** it looks like a safety rule ("do not build pylons or
  cannons near region borders") and is neither enforced nor documented as
  disabled. Almost certainly one `||` was intended instead of two `&&` guards -
  but which of the two was meant is a design question, not a typo fix, because
  the rule would then apply to *every* Protoss building.
- **How to settle it:** decide the intent, then either fix the condition or
  delete it with a comment. Deleting is defensible: it currently has zero
  effect and nobody noticed in a decade.

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

## B-10 — army strength saturates at 999 when the enemy is only buildings

- **Where:** `Army.calculate()` and `EnemyArmyStrength.calculateFrom()`
  (`totalHp + melee*10 + ranged*30 - combatBuildings*50 - bases*100`, then
  `Math.max(1, total)`), consumed by
  `ArmyStrength.ourArmyRelativeStrength()` = `min(999, ours*100/theirs)`.
- **Measured** (`EnemyUnitsTest` frame 4, one marine against a Zerg base made of
  creep colony, sunken colonies, a lurker den, extractor and a hydralisk):
  `ourArmyRelativeStrength() = 999`, i.e. the clamp.
- **Why it matters:** an enemy army made mostly of buildings scores *negative*
  and is clamped to 1, so "the enemy has only structures" reads as "our army is
  999% as strong". `weAreStronger()` (>= 108), `weAreMuchStronger()` and every
  fight/avoid decision keyed on those thresholds then see a certainty that the
  data does not support - and it is exactly the situation in which the bot must
  be most careful (a defended base).
- **How to settle it:** decide whether the building penalties may drive the
  score to its floor (then the ratio needs a guard, e.g. treat a floored
  strength as "unknown" rather than "infinitely strong"), or whether the
  penalties should be clamped separately. Either way `ourArmyRelativeStrength`
  should not return 999 for a base.

## B-11 — Terran units kept no margin against combat buildings *(fixed as a side effect of B-13)*

- **Where:** `TerranDontEngageWhenCombatBuildings.handle()`: inside 9 tiles it
  calls `moveToSafety`, between 9 and 11 it holds position **only if not already
  attacking**.
- **Measured** (`AvoidCombatBuildingsTest.neverRunsIntoCombatBuildings`, 50
  frames, marine vs two sunken colonies at 19 and 29): the marine walked up to
  **2.875 tiles** - well inside the colony's 7 tile kill range - and attacked.
  The test had been "fixed" earlier by pinning that number.
- **Why it mattered:** the class-level javadoc of that test says a unit 0.1 tiles
  outside a sunken colony's range already sees a much worse evaluation, and the
  Protoss side (`ProtossCombatBuildingClose`) has an explicit
  `moveAwayFrom(..., moveAwayDist())` branch.
- **Resolution:** the margin was missing because the safety margin of a Terran
  marine against *any* ranged attacker was garbage (B-13) - the colony was not
  counted as a threat at all. With B-13 fixed, the marine retreats: measured
  9.0 → 9.6 tiles over the first frames, and the test asserts its original
  intent (`distToSunken > 7.05`) again.

## B-12 — `ProtossCombatBuildingClose.applies()` was not reproducible across runs *(fixed)*

- **Where:** `atlantis/combat/micro/avoid/buildings/protoss/ProtossCombatBuildingClose.applies()`.
- **Measured:** the *same* scenario (lone dragoon at 10, missile turrets at 17/22,
  spore colonies at 17/22, photon cannon at 21.1) gave `applies() == false`
  three times in a row when the class ran alone and `applies() == true` after a
  world-based test from the same class. Clearing `Select`, `ArmyStrength` and
  `EnemyUnits` inside the test changed nothing.
- **Root cause:** `Cache.nukeAllCaches()` cleared its own instance registry after
  nuking (B-15), so from the second test in a JVM onwards *nothing* was cleared.
  The deciding value was simply the previous test's, still inside its cache.
- **What the test does now:** asserts the part that is reproducible (the finder
  picks the anti-air building and never a turret or a spore colony against air)
  and no longer asserts the fight decision.

## B-13 — Terran infantry treated every ranged attacker as harmless *(fixed)*

- **Where:** `atlantis/combat/micro/avoid/margin/SafetyMarginAgainstRanged.marginAgainst()`,
  Terran branch: `return (new MarineSafetyMarginAgainstRanged(defender)).marginAgainst(attacker);`
- **The bug:** `MarineSafetyMarginAgainstRanged` answers **-1 = "no opinion"** for
  everything except mutalisks, and the caller returned that sentinel as if it
  were a critical distance. `SafetyMargin.marginAgainst()` then computed
  `base + distance - (-1)`.
- **Measured** (`AvoidEnemiesTest.zergUnits`, marine vs hydras, before the fix):
  a hydralisk 4 tiles away scored **+5.0** tiles of safety margin and a zergling
  0.1 tiles away scored **-2.69**, i.e. inverted. Consequences: the hydra never
  appeared in `EnemyUnitsToAvoid.enemiesDangerouslyClose()`, so nothing told the
  marine to move away from it.
- **Why it was invisible:** the test suite ran as Protoss, so `We.terran()` was
  false and this branch never executed (see B-16).
- **Fix:** check the sentinel (`if (marineMargin > -1) return marineMargin;`),
  exactly like the melee path already does.

## B-14 — two Terran paths dereferenced `Chokes.mainChoke()` unguarded *(fixed)*

- **Where:** `TerranResponseEnemyHiddenUnits.buildAnywhere()` (runs for **every**
  discovered enemy unit, inside `EnemyUnitsUpdater.weDiscoveredEnemyUnit`) and
  `HaveBunkerAtMainChoke.applies()` (runs every frame of the main loop).
- **Measured:** `java.lang.NullPointerException: Cannot invoke
  "AChoke.translateTilesTowards(...)" because the return value of
  "Chokes.mainChoke()" is null` - `DynamicProductionCommanderTest` and
  `AtlantisGameCommanderTest` in an acceptance-package run.
- **Why it matters:** `Chokes.mainChoke()` is null until the map analysis has run
  and stays null on maps without a main choke. A frame that dies is a frame the
  bot does not play.
- **Fix:** `TerranResponseEnemyHiddenUnits` returns "no decision" without a main
  choke; `HaveBunkerAtMainChoke.applies()` returns false without one.

## B-15 — `Cache.nukeAllCaches()` only worked for the first test in a JVM *(fixed)*

- **Where:** `atlantis/util/cache/Cache.nukeAllCaches()`.
- **The bug:** cache instances register themselves in their constructor (in
  testing mode) and `nukeAllCaches()` emptied the registry afterwards. Cache
  objects are static fields, constructed once, so from the second call onwards
  the registry was empty and **nothing at all was cleared**.
- **Measured:** tests that pass alone failed inside a package run - e.g. the
  Protoss cannon tests died with `FakeUnit.position() is null` on a unit they
  never created, because `EnemyTooCloseToUnstartedConstruction` still held the
  previous test's enemy selection.
- **Fix:** do not empty the registry.
- **Note:** no game impact - registration is guarded by `Env.isTesting()`, so in
  a real game the registry is empty anyway.

## B-16 — the whole test suite ran as Protoss while building Terran units *(fixed)*

- **Where:** `AbstractTestWithUnits.setUpTestLogic()` read
  `MockEverything.defaultRaceForTests()` directly instead of the overridable
  `initRace()`, and `MockEverything.mockAtlantisConfig()` always called
  `useConfigForProtoss()`. On top of that, `EnemyRace` could be told a race while
  the `Enemy` mock next to it was hard-coded to "enemy is Protoss".
- **What it hid:** `AtlantisRaceConfig` said BASE = Protoss_Nexus, WORKER =
  Protoss_Probe, BARRACKS = Protoss_Gateway, DEFENSIVE_BUILDING_* =
  Photon_Cannon while tests created Terran_Marine and Terran_Barracks; every
  `We.terran()` branch was dead. Switching the default to Terran immediately
  exposed B-13 and B-14 and invalidated several pinned numbers.
- **Fix:** `initRace()` is honoured (default Terran, matching `Main.ourRace()`),
  a new `initEnemyRace()` hook drives both enemy mocks, and the Protoss tests
  say so.

## How to add an entry

```
## B-<n> — <one-line symptom>

- **Where:** file:line
- **Measured:** the numbers, and the command or test that produced them
- **Why it matters:** the concrete consequence in the game or in the suite
- **How to settle it:** what a fix would have to decide (not "fix the test")
```
