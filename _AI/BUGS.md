# Known bugs and behaviour debt

Not a TODO list — this file records things that are **wrong or misleading
today**, as opposed to work that is merely not started (that lives in
`_AI/NEXT.md`). Every entry states what was measured, how it was measured, and
what the consequence is.

An entry leaves this file when the behaviour is either fixed or deliberately
documented in code with a comment. The closure goes into the commit message.

## B-25 — the bot jar ships four serialisation libraries for unreachable code (question, not a plan)

- **Measured 2026-10-04** while moving the subsystem out of the kernel:
  `atlantis.debug.object` (9 files, 510 lines, kryo-based) has exactly one production
  call site, `OnEveryFrameHelper.serializeMapDataLikeRegionsToAFile()`, whose first
  line is `if (true) return;`, and one test, which is `@Disabled` (that is where all
  four skipped tests in the suite come from).
- **Consequence:** `scripts/build-bot-jar.sh` ships `kryo`, `minlog`, `reflectasm` and
  `objenesis` in every bot jar "for `ObjectToFile`" - i.e. four libraries, and their
  size in the payload, for code nothing reachable can call. Measured jar: 6.1 MB fat,
  3610 entries.
- **The owner's answer (2026-10-04), verbatim:** *"Nie mam pojęcia, to zostało
  dodane ostatnio; zanotuj to jako kandydata do potencjalnego usunięcia; jeśli nie ma
  w planie tego używać, to zapewne jakiś martwy pomysł"* - I do not know, it was added
  recently, note it as a candidate for removal, and if nothing plans to use it then it
  is probably a dead idea. **Checked against the plans and found nowhere:** neither
  `_AI/IDEA-E2E-TESTS.md`, `_AI/REVIEW.md` nor any stage mentions serialising maps or
  unit lists, and the one routine that wanted it has been switched off in place. So it
  is tracked as **NEXT #38** (a removal candidate, not a decision) rather than acted on
  here: deleting 510 lines of debug code and four libraries from the payload is the
  owner's call, and the entry stays until that call is made.

## B-1 — `AUnit.eval()` has no documented scale, and 228 call sites assume one

- **Where:** `atlantis/units/AUnit.java:2396` (javadoc) vs
  `atlantis/combat/eval/AtlantisJfap.java:90`.
- **What the code does:** `eval()` returns
  `enemyScore / (ourScore + 0.001)` — an **unbounded ratio** over cost-like
  negative side scores, where a side's score is what it **lost** in the
  simulated window. So the ratio reads "how much did the enemy lose, divided by
  how much did we lose": for our unit a **higher** value means we are stronger;
  for an enemy unit the same formula runs from its side, so a **lower** value
  means we are stronger. When no enemy is in reach it returns **9874.0**, and
  `-1.0` absolute. Pinned by `CombatEvaluatorTest.higherEvalMeansWeAreBetter`
  (three Marines next to one Zergling — a fight we win — read 1.6667 with an
  absolute of -30; one Marine next to one Zealot — a fight we lose — read 0.1300
  with -100). This entry, and the test class it pointed at, had the direction
  backwards until 2026-10-04; every threshold table below is about *where* the
  numbers sit, not about which way they run, so the inversion did not corrupt
  them.
- **What the javadoc promises:** "1.0 means our army is as strong, 1.3 means our
  army is 30% stronger than enemy".
- **Measured (one world each, `CombatEvaluatorTest` scenarios):**
  | scenario | our eval | their eval |
  |---|---|---|
  | 1 marine + 1 wraith vs 2 hydras + zealot | 0.0182 | 55.03 |
  | 4 marines vs 1 hydra | 7.5557 | 0.1324 |
  | 3 marines vs 2 hydras | 3.3694 | 0.2968 |
  | 4 marines + medic vs 1 hydra | 21.2513 | 0.0500 |
  | 1 marine vs 1 sunken colony (far) | 1.0010 | 1.0010 |
  | wraith vs 2 free dragoons | 0.0217 | — |
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

## B-2 — additive Protoss tweaks can push `eval()` below zero (fixed 2026-10-04)

- **Where:** `AUnit.freshCombatEvalRelative` ->
  `ProtossJfapTweaksConsiderChokesEtc.apply` (adds up to -0.4: -0.1 for enemy
  buildings near, up to -0.3 for combat buildings, choke/cohesion/retreat
  terms on top), applied to the `enemyScore / (ourScore + 0.001)` ratio.
- **Measured** (re-measured 2026-10-04, stub world, `ProtossCombatEvalScaleTest`):
  lone Wraith against two (fogged, i.e. full-health) Photon Cannons. As Terran the
  ratio is **0.0211**, absolute **-760** in both readings. As Protoss the same fight
  scores **-0.3789** = 0.0211 - 0.1 - 0.3 - (choke/cohesion terms). The enemy side
  is unaffected by the tweaks' sign (cannon eval **47.50** on both readings; the
  pair is even reciprocal as Terran), so the sign break happens in the additive
  tweaks, exactly when the raw ratio is near zero.
- **Fixed 2026-10-04 (the owner's ruling: `Math.max(0.01, eval)`).** The floor
  lives in `CombatEvalScale.signSafe` and is applied at the two places where the
  sign can still break: the ratio in `AtlantisJfap.calculateToRelativeScoreIfNeeded`
  (our side scoring nothing divides by ~0) and the sum in
  `ProtossJfapTweaksConsiderChokesEtc.apply` (measured -0.3789 becomes 0.01). It is
  in the evaluator rather than in `AUnit.eval()` on purpose: `atlantis.units` must
  not depend on `atlantis.combat` (REVIEW §16 Stage E, and AUnit already leans on
  one frozen edge), so a floor applied there would have cost a new core→consumer
  violation.
- **What the floor does and does not buy (measured):** the smallest number any
  production guard compares `eval()` against is **0.3** - over all 236 `eval()` call
  sites in `src/atlantis`, 34 distinct literals from 0.3 to 10 - so flooring at 0.01
  is inert for every comparison and only removes the sign break. The smallest
  measured Terran-side raw is 0.0211 (this fight), which passes through untouched -
  the floor engages only the Protoss-tweaked negatives. It does **not** make
  `eval() < 0.5` stricter - 0.01 is still below 0.5 -
  so the dangerous direction B-2 opened (`eval() <= 2.5` is true for a fight lost
  150:1) is unchanged and remains B-1's problem. The B-18 combat-eval hedge is what
  actually tightens the guards.
- **Why it matters:** a ratio whose two sides had opposite signs is not a
  strength comparison, so no threshold can read it: `eval() >= 1.2` (12 call
  sites) answers "not a good fight" for a number that is not a fight, and
  `eval() <= 2.5` answers "no better than even" — the two answers contradict each
  other, and which one a given doctrine gets depends on the direction it happens
  to compare in. It is also why the old
  `CombatEvaluatorTest.takesIntoAccountFoggedUnits` could never pass: it
  asserted `ourEval > 0` - which is now
  `ProtossCombatEvalScaleTest.aWraithAgainstTwoDiscoveredCannonsScoresAtTheFloorNotBelowIt`,
  the same world with the bot playing Protoss, and it passes.
- **History:** this entry used to blame "two Jfap side scores with opposite
  signs". The scores are both negative (cost-like) and their ratio is fine;
  the sign break happens one layer up, in the additive tweaks. The old
  -0.3961/169.06 numbers were measured with a fiction table; the mechanism
  above reproduces with engine data.
- **How to settle it:** settled - floored at both places the sign can break.
  The other two options from the original question (apply the tweaks
  multiplicatively, or skip them when the raw ratio is near zero) are still
  available and would change the *magnitude* of the tweaks rather than only
  their sign, but they are not needed for the defect this entry names.

## B-23 — the "new base created" pass cancels the natural it is supposed to keep

- **Reported from a game (2026-10-04), verbatim:** a Nexus NATURAL queued at 4:13
  (`Nexus ADDED TO QUEUE, min=593/ sup=26 / CanAfford`) is cancelled minutes later:
  `7215 Cancelling pending base At 30 Nexus NATURAL* (IN_PROGRESS)(#176), Reason: New
  base created, remove not started ones` -> `Cancel order ... at 5:03` ->
  `Cancel construction: Nexus / ... / at:[103,37] / buildable:false`, and the bot
  queues the same Nexus again at 5:15 (`min=799/ sup=31 / LimitedBases`). The
  reported shape: "the algorithm sees that a new base was built and cancels the
  others, but it cancels our own building".
- **Where:** `OnOurUnitCreated` -> `CancelNotStartedBases.cancelNotStartedOrEarlyBases`,
  condition `!construction.hasStarted() || construction.progressPercent() <= 49`.
  The second half cancels anything **up to half built**, which is a different policy
  under the same name, and it is the half that eats a natural with a builder on it.
- **Fixed 2026-10-04, in the owner's words:** *"see all unfinished bases / buildings
  / units of that type and if there is more than one unfinished, cancel it. But only
  if >= 2"*. So `cancelNotStartedBases` now counts everything unfinished of the base
  type - rising buildings and units, constructions ordered but not started, and
  constructions still only a request - and returns early when that count is below two.
  Which one goes: only bases nobody has started on are prunable (the construction with
  a builder keeps its minerals and its worker); if something is already going up the
  pending ones all go, and if nothing has started the **oldest** pending base is kept
  and the rest dropped, because cancelling every pending expansion only makes the bot
  queue them again a minute later - which is the churn this pass used to cause.
  `cancelNotStartedOrEarlyBases` keeps the aggressive half for the four callers whose
  reason is "give up on this expansion for minerals or survival" - "Cancel base -
  much weaker", "Critical base cancel", "HiddenEnemiesPressure", and the expansion
  veto in `ProtossShouldExpand.no(...)`.
- **Test (done):** `CancelNotStartedBasesTest`, four halves of that sentence - a
  single pending base survives; of two pending bases only the newer is dropped; a
  pending base is dropped when another one is already being built, and that one is
  untouched; the aggressive pass still drops a half-built base.
- **Measured along the way, worth knowing:** the pass looks at
  `Queue.get().statusNotReady()`, which is the double negative - orders whose status
  is *not* `NOT_READY`. So "not started" here means *ready to produce with no unit on
  the construction yet*; an order already marked NOT_READY was never in scope. The
  trigger unit's own construction is excluded, which is why the log's natural was a
  *different* base than the one that had just finished.
- **The half this entry could not decide alone, fixed 2026-10-04.** It needed one line
  of the owner's log: which base had completed. If it was a **rebuild** of a base we
  already had (main lost and re-taken), then "we have enough bases" is false in the
  first place and the pass should not run at all. Rather than wait for the line, the
  distinction is now made where it can be made: `UnitsArchive` stamps the frame our
  last base died (the same shape as the combat-unit stamp next to it), and
  `worthPruningBases()` refuses to prune when the base that just finished is a rebuild
  - four-minute window, deliberately wider than a base takes to build, because the
  false-positive cost is that one pass does not run, and leaving a pending base alone
  has cost this bot a queued Nexus while the opposite has cost it a mineral field.
  The aggressive half (`cancelNotStartedOrEarlyBases`) deliberately still runs: its
  callers are giving up on an expansion to free minerals or to survive, which never
  depended on "we gained a base". Two tests: a rebuild prunes nothing, and a base that
  finishes with nothing lost still prunes (the precondition is not a switch-off).
  Checked that the first one fails without the fix: "expected 2 but was 1".
- **Side effect worth having:** `UnitsArchive` had no reset, so "when did our last base
  die" (and the combat-unit stamp, and every per-type counter) leaked between tests in
  the same JVM - test-order dependence in the one question whose answer is per-game.
  `UnitsArchive.reset()` now runs from `ClearAllCaches.clearAll()`, the shape
  `ReservedResources.reset()` and `Missions.reset()` already had.

## B-22 — "we build a zealot and a dragoon and then nothing"

- **Reported from a game (2026-10-04):** the Protoss dynamic unit production
  produces one zealot and one dragoon and then stops, with resources in the bank.
  `ProtossDynamicUnitProductionCommander.reason` is the field to read in a log.
- **Why nobody could tell from the suite:** the path was unreachable.
  `ProtossDynamicUnitProductionCommander.handle()` opens with
  `if (!AGame.everyNthGameFrame(7)) return false;`, and `AGame` is statically mocked
  in every test with only `AGame::now` stubbed - so that throttle answered false
  forever. **77 call sites in 65 files** share it, i.e. every frame-throttled path in
  production was dead in tests. Measured: a 120-frame world where `A.now()` walked
  1..120, the commander's reason string never left "-", and `ProduceDragoon.dragoon()`
  called directly returned true every single frame.
- **Two harness gaps, both fixed (2026-10-04):** `useFakeTime` now answers
  `everyNthGameFrame`/`notNthGameFrame` from the same frame number as everything else,
  and `AUnit.trainingQueue()` returns an empty list when there is no engine object
  (it dereferenced `u` unguarded, so the first question the commander asked -
  `Count.zealotsWithUnfinished()` -> `Selection.producing()` - threw an NPE from
  inside a doctrine).
- **Measured with both fixed** (`ProtossDynamicCombatProductionTest`, the reported
  world: 800 minerals, 300 gas, supply 40/60, one zealot, one dragoon, gateway +
  cybernetics core, 2 Marines + 1 SCV): **42 Dragoons and 0 zealots in 300
  frames**, one order every 7 frames. Dragoon answers first at this economy
  (`dragoons <= 4`, then `hasMinerals(125) && hasGas(150) && dragoons <= 17`) and
  zealots are last in the chain, so these gates do not stop production.
- **Then the owner sent the log (2026-10-04), and the answer is "no exceptions":**
  the excerpt has mission lines, base queueing lines (`4:10`, `4:13`, `5:15`,
  `6:33 Nexus ADDED TO QUEUE, min=2025/ sup=34`) and dying-unit logs, and nothing that
  throws. So the "something throws before the commander" branch is out - which is the
  same shape B-20 had (a per-frame NPE that killed the rest of each frame), so it was
  worth ruling out.
- **Re-measured with the log's own numbers** (2025 minerals, 300 gas, supply 34/40, one
  gateway, a cybernetics core, 2 Marines + 1 SCV): **still produces** - 42 orders in
  300 frames. And with the reported ending included (half of every produced unit dies,
  which is what happened to zealot #132 and dragoon #153 at 5:21): **59 orders in 400
  frames**, `freeGateways` never dropping to 0. So neither the resources, nor the
  supply, nor a single gateway, nor losing the produced units stops this code.
- **The state only the game has, measured from the games themselves (2026-10-05).**
  `~/.scbw/games/GAME_*/logs_*/unit_events.csv` records every unit that finished, with
  its frame, so the claim can be checked instead of inferred. All five games the owner
  ran on 2026-10-04:

  | game | opponent | our combat units built | last one | game ended | gas gathered |
  |---|---|---|---|---|---|
  | `5B9FABC8` | Ecgberht (Terran) | 1 zealot + 1 dragoon | frame 5433 | 17136 | 1368 |
  | `631F4FE6` | Steamhammer (Zerg) | 2 zealots | frame 4069 | 7896 | 72 |
  | `A84A0F2C` | Tomas Cere (Protoss) | 1 zealot + 1 dragoon | frame 5511 | 13944 | 712 |
  | `BD9184B0` | Zealot Hell (Protoss) | 1 zealot + 1 dragoon | frame 5424 | 11136 | 704 |
  | `FCCD84AE` | Ecgberht (Terran) | 1 zealot + 1 dragoon | frame 5483 | 12576 | 640 |

  So the report is not one unlucky game: it is **every game**, against every race, for 4
  to 8 minutes after the last combat unit, with 640-1368 gas banked in four of the five.
  "Not enough resources" is dead: in `631F4FE6` the whole game gathered 2810 minerals and
  **72 gas, and spent 2100 minerals and 0 gas**, while queueing exactly one thing after
  its 10-item book (a Nexus at 4:30 with 778 minerals, reason `LimitedBases` - that bot
  never took a natural, never built a second assimilator, and so could never afford a
  dragoon; that is a separate finding, not this entry). For the other four, gas was
  there and the units still stopped.
- **Root cause, by elimination through the code (2026-10-05):** with minerals ≥ 600 and
  supply 40+ of 60, `freeToSpendResources()` returns true (`Minerals++`),
  `AllowProduceZealot.allowed()` and `AllowProduceDragoon.allowed()` both return true
  (minerals ≥ 500), the cybernetics core exists, and `ProduceZealot` has two gates that
  fire on `freeGateways >= 1` or `>= 2` with the minerals the bot was sitting on. The
  only gate both producers share, and the only one that can be true in all five games,
  is **"no free gateway"** - `Count.freeGateways()` → `Select.free()` → `!isBusy()` →
  `!AUnit.isIdle()` → the engine's `u.isIdle()`. The bot read a *movement* notion
  ("this building has no orders") as a *production* one ("this building cannot take a
  train order"), gave up silently, and had no recovery: the state is re-read every 7
  frames and was still false at the end of every game.
- **Fixed 2026-10-05, three parts:**
  1. `GatewayClosestToEnemy` falls back to *any* of our gateways when none is free.
     Whether a producer can take an order is the engine's question, so the bot asks
     instead of guessing: if the engine refuses, the cost is one refused call every 7
     frames and no state change.
  2. `ProduceZealot` and `ProduceDragoon` measure capacity as "gateways we could ask"
     (`Count.gateways()`) when none is free, and each carries a `reason` string naming
     the gate that answered - `Minerals`, `Gas`, `NoGatewaysOrCore`, `NotAllowed`,
     `NoRule` - so the next report is a log line rather than an investigation.
  3. `ProtossProductionDiagnostics.reportRichButIdle(...)` logs one line a minute when
     the bot can afford units, has a gateway and a core, and produced nothing: minerals,
     gas, supply, gateways/free, the commander reason and both producer reasons. It goes
     through `ErrorLog`, so it is rate-limited in games *and* stub worlds and lands in
     `bot.log`.
- **Test:** `ProtossBusyGatewayProductionTest` - every gateway `busy`, 800 minerals,
  300 gas, 40/60 supply, 200 frames - production continues. Verified it fails with the
  three parts reverted ("ordered: " is empty, i.e. nothing at all). Getting there needed
  two harness/robustness fixes that the test exposed, both real:
  - `FakeUnit.isIdle()` answered a separate `idle` field that nothing ever set, so a stub
    unit claimed to be busy *and* idle at once and `free()` (which filters on
    `isBusy()`) disagreed with it. It is now `!busy`, like production's, and the dead
    field and the test assertion that set it are gone.
  - `AUnit.hasNothingInQueue()` dereferenced `u()` unguarded
    (`isFree() && u().getTrainingQueueCount() == 0`). It never fired only because
    `isFree()` was false for every stub unit - a guard that held because a lie preceded
    it. It now goes through `trainingQueue()`, which is the same question answered
    without an engine object, exactly as the earlier B-22 harness fix did for
    `Selection.producing()`. This is the B-20 shape (an exception from inside a
    doctrine) in a spot where a game can never show it.
- **Still open, and now cheap to answer:** whether the engine's `isIdle()` was false for
  a reason the fallback can also clear (a finished order it still counts, a queued
  action) or for one it cannot (an order the bot keeps re-issuing - if so, the report
  above will now say "free=0 gateways=2" every minute and the re-issuing doctrine is the
  next thing to look for).

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
- **Update (2026-10-04, the "extend the window" option measured and refuted):**
  `AtlantisJfap.NUM_OF_FRAMES_TO_SIMULATE` is one constant, so the horizon can be
  varied by editing it and re-running `CombatEvaluatorTest`. Measured on the
  scenario above (4 Marines at x=11.5-12 vs 1 Sunken Colony at x=13):

  | horizon | our eval | reading |
  |---|---|---|
  | 60 (current) | 0.98 | "about even" - the opening exchange |
  | 120 | 1.31 | **moves in our favour, which is the wrong way** |
  | 180 | 1.21 | converges, still our way |
  | 240 | 1.21 | unchanged - the fight has resolved inside 180 frames |

  (The readings in this table were written before the direction of the scale was
  settled; a higher number means the enemy lost more than we did, so "against us"
  is 1.31 reading the wrong way for a fight the colony wins outright.)

  Two things follow. First, a longer window moves the number *away* from the truth
  here - it plateaus at **1.21 for a fight we lose roughly 150-to-1**, which reads
  as a comfortable win and is nowhere near the `eval >= 2` that the retreat and
  focus-point thresholds read as dangerous. The score is a ratio of accumulated
  costs, not a win prediction, so no horizon makes it one. Second, the change is not safe as a constant bump:
  `oneMarineAgainstOneEnemyMarine`, an exactly symmetric fight, scores **1.0 at 60
  frames and 0.88 from 120 frames on**, while its absolute scores stay equal (that
  assertion passes) - so the horizon change breaks the mirror invariant somewhere
  in the tweak layer, not in the simulation. That asymmetry is unexplained and is
  tracked as NEXT #35.

  So the horizon is out, and the remaining option is the second one: teach the
  callers that `eval ~ 1` against a defensive building means "undecided", not
  "safe".
- **Update (2026-10-04, the owner's ruling, implemented):** instead of teaching
  callers one building at a time, `CombatEvalScale.OUR_SIDE_HEDGE = 0.3` takes 0.3
  off **our own** reading in `AUnit.eval()` - the raw 1.0 reads 0.7, so only a raw
  1.3 is "even", and a fight that reads 1.01 stops being a fight we walk into. It
  is subtractive, so it bites hardest where the decisions are: a 0.6 gate now needs
  a raw 0.9 (50% stricter), a 4.0 gate needs 4.3 (7.5% stricter). Measured over the
  195 literal comparisons in production (of 236 `eval()` call sites): 11 below 0.7,
  39 in 0.7-0.99, 1 at exactly 1.0, 37 in 1.0-1.3 and 107 above 1.3. An enemy's
  number is not hedged - that would understate the enemy, which is the failure the
  owner reports from games. **Needs a game run**: this moves live fight and retreat
  decisions, and the scenario tier passing says the defended behaviours still hold,
  not that micro is unchanged.
- **Update (2026-10-04, two full games, still open):** `GAME_B978D4B7`
  (AtlantisP vs Marine Hell, loss, 0 exceptions) ended kill_score 500 vs
  3000 - we killed 4 marines + 1 SCV and lost 23 probes, 4 nexuses, a
  gateway, a zealot and a dragoon; `GAME_2AD8C998` (AtlantisP vs
  Steamhammer, loss, 0 exceptions) ended 150 vs 2400. Both bots played
  clean (no crash, no NPE), both lost every fight that mattered - which is
  consistent with "too cautious" and with "out-macroed", and these score
  lines cannot tell the two apart. The hedge question needs a fight where
  the pre-hedge reading is known, i.e. a scenario, not a ladder score.
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
- **Update (2026-10-04, full games, still open):** `GAME_2AD8C998`
  (AtlantisP vs Steamhammer Zerg, loss, 0 exceptions) is the closest thing
  to the rush this entry is about, and it does not test the fix: first
  zergling at 4478, our zealot died at 5628, then 23 probes died in the
  mineral line for 1 ling killed - but we built no Forge and no cannons at
  all, so the repaired path ("help the static defense") never had a cannon
  to help. `GAME_B978D4B7` (vs Marine Hell, first marine 3504) never brought
  pressure either. Game-run confirmation still owed, and now it has a
  precondition: a game where static defense exists.
- **Update (2026-10-04, four full games, pattern):** `GAME_DD6EAB8E` repeats
  the Steamhammer shape (1 ling for 21 probes + 2 zealots, no Forge, no
  cannons) and `GAME_366E9D6C` (Marine Hell, kill_score 0) never saw
  pressure at all. Four real games, zero static defense built in any of
  them - the help-path's precondition never occurs, so the question moves
  one level up: it is no longer "do workers help the cannon" but "why does
  no cannon ever exist to help". That is a strategy/production question,
  not an arbitration one, and score lines cannot answer it - it needs a
  replay read, which is the owner's half.

## How to add an entry

```
## B-<n> — <one-line symptom>

- **Where:** file:line
- **Measured:** the numbers, and the command or test that produced them
- **Why it matters:** the concrete consequence in the game or in the suite
- **How to settle it:** what a fix would have to decide (not "fix the test")
```
