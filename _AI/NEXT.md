# TODO — Atlantis (open issues)

Living backlog for the architecture migration defined in `_AI/REVIEW.md` §16
and for defects found along the way. This file is the single source of truth
for "what is left"; `_AI/REVIEW.md` keeps the *stage* narrative and
`_AI/NOTES.md` keeps operational learnings.

## Current owner priorities (2026-10-09)

1. **OpenBW simulations must be fast and self-terminating** - CONVENTIONS §17:
   `TIMEOUT_SECONDS=120` wall-clock and `INGAME_TIME=20` game minutes, both set
   once at the top of `scripts/run-openbw-e2e.sh` (done, commit `d90a4ea2`).
   Every future run mode gets the same pair.
2. **Finish Production V2** - `_AI/STATUS.md` and `_AI/redesign/01_PRODUCTION.md`.
3. **Placement** - the rewrite landed (`_AI/redesign/03_PLACEMENT.md`); the
   cut-over and the rest of Protoss are what remain.
4. **OpenBW tests** - `_AI/IDEA-E2E-TESTS.md` §3.5 has the ordered steps.

The index of which document answers which question is `_AI/README.md`.

## Process (normative)

- Every item has a **stable number** (`#1`, `#2`, ...). Numbers are never
  reused and never renumbered, so an old commit or chat answer can always be
  mapped back to the item it referred to.
- **When an item is closed, delete its line from this file** (do not move it
  to a "done" section — the git history is the archive) and say in the commit
  message `Closes #<n>` with the reason it is actually done (test result,
  game-run id, ArchUnit output).
- New items are appended with the next free number, grouped by area.
- Work proceeds in any order; prefer items that unblock the most downstream
  work, not the easiest ones.
- One logical change per commit, per `_AI/CONVENTIONS.md` §6.
- An item is only closed when it is **verified by execution** (build, test
  run, or game run), not when the code merely compiles.

## Test health

The suite is green (440 passing, 1 skipped, plus 10/11 architecture with the one
known structural failure, as of 2026-10-13 autonomous session;
re-run `bash scripts/run-tests.sh` for the current numbers - do not trust a count
written here). What is open is *quality* of the data behind it, not a red test.

- **#34** Real opponents for the stub-tier scenarios. The two twins hold in
  `tests.e2e` (`FourPoolDefenseTest` 6 lings, `NinePoolDefenseTest` 8 lings) with
their baselines pinned; what is missing is a **real opponent**: OpenBW hosts one
client, so a second bot cannot attach, and a scripted rusher needs a UMS map
that does not exist here. The scbw tier has real verdicts in `_AI/e2e/` and is
owner-run. Resume when a scripted UMS map is supplied or the opponent runner is
agreed. Detail: `_AI/IDEA-E2E-TESTS.md` §3.4-§3.5.

## Stage E — read model (remaining)

- **#3** Migrate production readers of `FoggedUnit` to `UnitSnapshot`.
  Candidate sites inventoried (24 files reference fogged types; most are
  lifecycle, not reading). Verdicts:
  - MIGRATE (need the last-known-vs-current distinction, real payoff):
    `ProcessAttackUnit:56` (attack a last-known position without fog
    awareness), `BuilderAvoidEnemies:27` (avoid a position), `DeadMan:25`
    (a fogged unit cannot be a dead man - exactly what `hpKnown=false`
    says), `AFocusPoint:111` (focus on a last-known position),
    `DoAvoidEnemies:30` (filter on position-known, selection-level),
    `AtlantisJfap.isValidUnit:71` (validity gate could be "has a position
    snapshot" - borderline, decide at migration time).
  - REJECTED (same object, same values, worse GC): `AUnitOrders:233,261`
    (order issuance needs the target entity itself), `Selection:223,296`
    and `Units.addFoggedUnits` (live-collection plumbing),
    `PositionUtil:50` (pure position delegation),
    `MissionAttackFocusPoint:125` (focus needs the entity identity),
    `AAdvancedPainter` (debug reads everything anyway),
    `OffensiveTurrets:41` (commented out).
  - NOT #3 (lifecycle - dies with the World registry, Stage E core):
    `AUnit:173-197` constructors, `UnitsArchive:141`, `EnemyUnits*`,
    `NeutralUnits`, `OnUnitMorph:31`.
  Migrate the first group when the World registry owns lifecycle; until
  then a delegation switch has no value. This item closes with those
  migrations, not before.

## Stage F — cache purge

The full inventory is `DOCS/SELECT-CACHES.md` (46 entries, key → TTL → readers),
generated rather than remembered. Three concrete starting points came out of it;
the first is already done. The two behaviour-neutral ones have an executable
procedure: `_AI/work-orders/WO-F-neutral-cache-cleanup.md` (the query service
itself, #5, is design work and out of that order's scope).
- Both behaviour-neutral items are done (WO-F): `microCacheForFrames` is gone -
  the 24 call sites say `0`, which is what the enemy-side queries have always
  used - and `Select.clearCache()` clears `cacheObject`, so
  `mainOrAnyBuildingPosition` no longer survives a unit-created event on a
  73-frame TTL.
- Six TTLs above one frame (30, 31, 53, 73, 91, 293) have no stated reason
  anywhere. `Select.main()` - 90 call sites, the most-read method in the tree -
  is cached for 2.4 s.

- **#5** Introduce a per-frame query service in `core.world` owning the
  caches, with explicit invalidation per frame instead of TTL guesses. Move
  one cache at a time, starting with the one with the fewest readers.
- **#6** Delete the migrated static caches from `Select` (no compat shims —
  every call site rewired in the same commit) and re-freeze the ArchUnit
  store only if violations genuinely disappeared rather than moved.

## Stage G — race strategies

- **#8** Implement the seam and migrate one subsystem end-to-end as proof
  (candidate: production), measuring that the branching-site count drops
  without behaviour change.

## Stage H — god-class split

- **#9** Split `A` (163 public static methods) into cohesive collaborators.
  Progress: 163 → 43 public static methods, 1688 → 414 lines. Extracted so
  far: `AFile` (file/path I/O), `AGui` (4 popups, A lost Swing entirely),
  `AMath` (ranges/statistics), `ARandom` (stays in `atlantis.game`: it shares
  the mutable `A.random` stream), `AConsole` (console writers). Deleted: 56
  methods with zero callers, including `formatDecimalPlaces` which formatted
  the wrong argument. Each extraction also shrank the frozen ArchUnit store
  instead of growing it.
  Remaining in `A`: resource/supply facades (`supplyUsed`, `hasMinerals`,
  `canAfford*`, ~1400 call sites), clock arithmetic (`seconds`, `ago`,
  `everyNthGameFrame`, `minSec`), race predicates (`whenEnemyProtoss*` — do
  these in Stage G, not here), and the wall-clock formatters
  (`getCurrentDateInFormatYMDHHmm`, `getCurrentTimeAsString`). Decide whether
  the resource facades move to `AGame` (which already exposes minerals/gas)
  or become an `EconomyContext` port — that decision belongs with Stage G/I,
  so do not start it here.

  The wall-clock formatters were attempted as an extraction and **reverted**
  (2026-10-09): the split is trivial but the tooling spent many edits on a text
  substitution and no behaviour changed, so it is not worth another attempt
  until the edit path is reliable. `A` still owns both methods.
- **#10** Split `Selection` (235 public methods) along its existing internal
  seams; verify with a test that pins selection semantics before and after.
- **#11** Split `AUnit` (~623 methods) — start by extracting the order-emission
  surface that `AUnitOrders` already half owns, then the per-frame manager
  orchestration, then per-condition logic.

## Stage I — physical migration

- **#12** Once F–H land, define the physical package move to the bounded
  contexts (`Economy`, `Production`, `Combat`, `Intelligence`, `Map`,
  `Scouting`) per `DOCS/ARCHITECTURE-CONTEXT-MAP.md`, as a mechanical,
  separately-revertable step per context.

## Stage J — fortress

- **#13** Reach a **zero-violation** ArchUnit baseline and flip
  `ArchitectureBoundaryTest` from frozen store to hard rules (keeping the store
  only as a history of how far the ratchet got).

  **State (2026-10-04): 356 violations, down from 499.** Three rules are at 0
  (`map.scout -> combat/production`, map geometry, and one more). The rest
  (2026-10-13 recount: core file holds 255 entries, was 267):
  `core -> combat/production/information/...` = 255, `information -> combat/production`
  = 60 (65 on 2026-10-04; -1 on 2026-10-13: dead `notEnoughZealots()` branch in
  `DragoonInsteadZealotVsZerg`, WO-13 cycle 1; -1 more: dead `enoughZealots()`
  branch + unused import, WO-13 cycle 2; -3: moved `AllOfOurBasePositions.allBases`
  to its only caller `TerranReinforceBasesWithBunkers`, WO-13 cycle 3),
  `architecture -> ...` = 24,
  `util -> ...` = 0 (was 73).

  **The 255 and the 24 are structural**, not a sweep: the Commander/Manager
  framework is unit-centric by design, so they belong to Stages E/H. The
  information rule swept 65 -> 60 on 2026-10-13 (WO-13 cycles 1-3: two dead
  `ProduceZealot` branches, one single-caller helper folded into its caller);
  every remaining entry was triaged and is live strategy-response logic
  (race responses ordering production, forcing missions, reading queue/tech
  state) - the seams Stage G exists to redesign, not to delete line by line.

  Two traps, both paid for:
  - A change must *remove* violations from one rule, not move them into another.
    Moving `ScoutManager` into `atlantis.units.special` compiled and passed every
    test, and only grew the core rule (267 -> 280).
  - `rm -rf out` before `scripts/run-architecture-tests.sh` after moving a class
    between packages: `javac -d` leaves the old `.class` behind, ArchUnit reads
    both, and violations "come back" for a package that no longer exists. ArchUnit
    also only prunes stale entries for the rule it re-writes, so re-freezing needs
    the store emptied by hand and the diff reviewed (a re-freeze can absorb
    violations whose text merely changed).

  The mechanical-sweep details and the per-class triage are in
  `_AI/work-orders/WO-13-mechanical-archunit-sweep.md` and in git history.
## Review rounds

Each review of the last window gets its findings and their dispositions here, so a
later reader does not have to re-open the review to find out what was done about it.
Reviews: `_AI/REVIEW.md` (top-down, §16 stages), `_AI/REVIEW-GLM.md`
(SOLID + architecture, 2026-10-04, window `e8962bda`…`e4a2579c` plus the GameClock WIP).

### GLM 2026-10-04

| finding | disposition |
|---|---|
| F-1 two clocks, three writers, one of them bypassing the publish protocol | **done** - `A.setNow(framesNow, secondsNow)` is the only writer (game + both harnesses); `GameClockAgreementTest` (3) pins both views moving together. Measured, and worth knowing: the predicted drift does not reproduce, because the same frame is published in `onFrameEnd` before the frame body runs - it was ordering luck, not design |
| F-2 race gates written per leaf | **done** - the rule ("a commander whose subcommanders are race-scoped gates itself in `applies()`") is now in `DOCS/SOLID-CHECKLIST.md` OCP, with the two commanders that had to remember and the crash it prevents |
| F-3 `BaseUnderAttack` is all-static while everything around it becomes a `Manager` | **#36, not urgent** - it works, the scenario tier pins it, and the per-frame cost is bounded by the `enemiesNear` 5-frame cache. What it does lack is a seam: no test can force or stub the answer, and `check()` recomputes the same `Select.mainOrAnyBuilding()` query three times per frame per worker |
| F-4 `ProtossJfapTweaksConsiderChokesEtc.rawEval` static mutable state | **done, differently than suggested** - the field was written and read nowhere, so it was deleted instead of threaded through the penalty helpers. The review's direction was right and its evidence was wrong (a grep for readers is two commands) |
| F-5 "pressure" now has a second class (`BaseUnderAttack`, `ExpansionUnderPressure`) | **named, no merge** - thresholds, radius types and semantics differ, so they stay. If a third "is pressure real here" class appears, extract a shared threat assessment then, not now |
| F-6 `CancelNotStartedBases` name drift after B-23's narrowing | **dropped 2026-10-04 (owner's ruling)** - cosmetic, and the drift is contained: the class may deliberately keep the oldest pending base, the docstrings carry the nuance, and callers say "not started ones" anyway |
| F-7 `scripts/run-e2e.sh` write policy | **checked, no violation** - §8 allows read-only `~/.scbw`, the script writes into `_AI/e2e/`, and `--parse-only` matches its claim |
| F-8 `Commander.applies()` is load-bearing for correctness, not filtering | **no action, recorded** - the OR-accumulated `handle()` and the non-null-stops `Manager.handle()` are documented where declared; the next contract addition should follow the same rule |

- **#36** `BaseUnderAttack`: give it the same treatment `ExpansionUnderPressure`'s
  callers got - a per-frame cache of `check()`, or a tiny injectable seam so a test can
  force the answer. Cheapest honest version first (the cached `check()`), because the
  static-to-`Manager` conversion is a bigger change than the three duplicated queries
  it would remove. Not urgent: nothing is wrong with the behaviour today.
- **#39** The Protoss cannon producers are commented out in the buildings commander
  (`// || ProduceCannon.produce()`, `// || ProduceCannonAtNatural.produce()`), disabled
  2024-10-02 (`49a4536a`) and 2025-01-09 (`9a34add6`, `75d7650f`) with no recorded
  reason, and `ProduceCannon` no longer exists in the tree. The two live cannon paths are
  `ProtossSecureBasesCommander` (needs `Have.forge()` + two bases) and
  `ProtossResponseEnemyHiddenUnits` -> `ProduceCannonAtNaturalOrMain`. **Decision, not a
  chore:** re-enable (which mechanism should own "the base is being attacked", given two
  exist), or delete the dead classes, or leave it and own the consequence in a comment at
  the call site. `BUGS.md` B-19 carries the measurements - four games, zero cannons - and
  the chain that leads there.
- **#40** `ConstructionThatLooksBugged.handleConstructionThatLooksBugged()` has an
  unreachable branch: the method returns unless `status() == NOT_STARTED`, and the inner
  guard is `if (constr.status() != NOT_STARTED) constr.assignOptimalBuilder();`, so the
  assignment never runs. The effect is that a planned construction with no builder is
  *cancelled* ("Weird case, ... has no builder. Cancel.") rather than given one.
  Builders are assigned at creation (`NewConstructionRequest` /
  `DefineConstructionForNewUnit`), which is why it is invisible - and why it is recorded
  rather than changed. Worth a test that says which of the two behaviours is intended
  before anyone touches it: a construction whose builder is assigned a frame later would
  be cancelled by the safety net.

- **#15 PARTIAL — deployed Protoss jar freshness.** Rebuilt
  `/sc-ai/BOTS/AtlantisP/AI/Atlantis.jar` with the canonical builder; freshness
  fingerprint matches the source tree (`94e518…`) and bytecode major is 52
  (Java 8). OpenBW run `GAME_SECONDS=90 PLACEMENT=catalogue`, map
  `(3)TauCross1.1.scx`, Protoss vs Zerg: client attached, bot exited 0,
  `Total time: 843 seconds`, no exception; it still logged
  `Can't find place for Pylon` at 0:39, so this verifies the jar was played, not
  that placement works. **Terran remains blocked:**
  `/sc-ai/BOTS/AtlantisT/AI/Atlantis.jar` does not exist and the scbw opponent
  cache is owned by scbw; no approved Terran deployed target was found. Do not
  close #15 until the intended Terran artifact path is supplied or the owner
  verifies that no deployed Terran jar is required. Run artefacts:
  `out/openbw/bot.log`, `out/openbw/server.log`.

## Production v2 and the OpenBW E2E engine (2026-10-08)

- **#51 - Protoss buildings are allowed 2.8 tiles from minerals, which reads as "on
  top of the mineral line".** Owner's report, same IDE game: the position was
  otherwise fine but sat about two tiles from the minerals. The number is
  `TooCloseToMineralsOrGeyser.minDistToMineral`: for Protoss it returns **2.8** by
  default, and only rises with supply (5.2 at `supplyTotal >= 40` under some
  conditions, 6 at `>= 60`). A building 2.8 tiles from a mineral patch crowds the
  mining workers' path.
  Decision needed (owner's call, it is a gameplay constant): raise the Protoss
  default, e.g. to ~3.5-4, or key it off the building's own footprint plus a worker
  gap rather than a flat radius. `minDistToGeyser` is the parallel value (5.1 for
  Pylon/Gateway/Cannon, 4.1 otherwise) and should be re-checked in the same pass.

- **#52 - Production V2: a planned building's `startFrame` slides forward every frame,
  so the item is never dispatched. Split out of #48 (2026-10-10), which fixed the
  duplicate/re-dispatch half.**

  Measured under `PRODUCTION_V2=LIVE PLACEMENT=catalogue`: the plan is correct (one
  Pylon, no duplicate), the game ends itself (exit 0), every worker order is issued -
  and the building never is, because its window keeps moving:

  ```
  @@937 plan=2 [Probe@940-1240 by#70 Pylon@1129-1579 ] issued: OK Probe@940 (trained)
  @@938 plan=2 [Probe@940-1240 by#70 Pylon@1130-1580 ] issued: OK Probe@940 (trained)
  @@940 plan=2 [Probe@940-1240 by#70 Pylon@1132-1582 ] issued: OK Probe@940 (trained)
  @@941 plan=2 [Probe@941-1241 by#70 Pylon@1125-1575 ] issued: OK Probe@941 (trained)
  ```

  `startFrame` moves +1 per frame while the frame under it also advances, so the item is
  due "16 frames from now" forever. This is the same relative-frame slide
  `_AI/STATUS.md` recorded ("planned Pylon start moved 449, 450, 451, 452"), and it is an
  **arbitration** problem: the Probe (50 minerals, higher priority, `PRIORITY_WORKERS`)
  is scheduled first every pass and keeps the bank below the Pylon's 100, so the
  Pylon's earliest-affordable frame is always just out of reach.

  **Why this is now the top V2 blocker:** with #48's duplicate defect fixed, this is what
  stands between V2 LIVE and a built Pylon - and it is also why the stage-4 command-path
  question is currently moot under V2 (nothing is ever dispatched to be refused).

  **Mechanism now MEASURED and confirmed in the production code (2026-10-10).** It is an
  ordering problem, and it reproduces deterministically without a game
  (`tests.unit.TimelineSlideTest`, 5 cases, mutation-checked):

  - `DynamicGoals:221` gives the worker goal **`PRIORITY_WORKERS = 20`**;
  - `BuildOrderGoals:91` gives a build-order Pylon line **`PRIORITY_DEPOTS = 30`**;
  - the scheduler sorts by priority, so the Probe is planned FIRST every pass, and
    `ResourceTimeline.allocate` deducts its 50 minerals from the chosen frame **and the
    whole tail** (deliberately - reserved money is gone for the rest of the plan);
  - `findEarliestAffordableFrame` requires the cost covered across that tail, so the
    Pylon's earliest affordable frame moves later with each pass. Rebuilt from scratch
    every frame, it moves every frame: the slide.

  **The measurement that had to be made first, and it corrected the framing:** the slide
  needs **income**. With a flat bank the expensive item is not "late", it is unplannable
  (`-1`) - a different defect. The first draft of the test used a flat bank and proved
  nothing; the corrected one gives each pass the same bank *plus mining income*, so the
  pass-to-pass movement is the scheduler's doing and not the fixture's. Both cases are
  pinned now so a future fix is not aimed at the wrong one.

  **Not patched, deliberately.** `_AI/STATUS.md` records two attempts that were already
  reverted (subtracting `ExistingItems.availableFrom` suppressed worker production;
  raising only the dynamic supply goal did not address a build-order Pylon). The
  mechanism above suggests why both failed: neither changes the **priority relationship**
  between the worker goal and the build-order Pylon, which is what decides the order of
  allocation. A fix has to answer "who gets the reserves first when both are wanted",
  which is a policy decision and is the owner's call - not a timeline tweak.

  **FIXED and verified in a live run (2026-10-10).** The owner's ruling: the default
  opening is "Pylon at supply 8", so a build-order supply provider at low supply is the
  precondition for everything and is planned before the workers.

  - New band `ProductionGoal.PRIORITY_OPENING_SUPPLY` (15, ahead of
    `PRIORITY_WORKERS` = 20). It is deliberately narrow: only a **supply provider whose
    row gate is at or below `OPENING_SUPPLY_THRESHOLD` (12)** gets it. Checked against
    `bwapi-data/AI/build_orders/Protoss/*.txt`: the first Pylon is at supply **8** in every
    file, the second at **15-16** - so 12 covers the real opening and excludes the first
    top-up. (A threshold of 16 was tried first; the test caught that it silently promoted
    the supply-16 depot too, which is exactly what the rule must not do.)
  - `BuildOrderGoals.priorityFor(row, line, supplyUsed)` decides from **what the row is**
    (a supply provider, at a gate that means "opening") rather than from its line number
    alone. The old `priorityForLine` gave "line < 4" a depot band, so the same Pylon on
    line 5 landed in a lower band - a rule nobody chose. Line order survives only as the
    fallback for everything else.

  **Live evidence:** `@@914 plan=2 [Pylon@914-1364 Probe@...] issued: OK Pylon@914
  (builder committed)` - the Pylon now **leaves the plan as a dispatched order**, and its
  start is stable at 914 across frames 915-920 instead of sliding +1 per frame. 2087
  `issued: OK Pylon` lines in the run, against 7 for the Probe.

  **What is left is NOT #52** - it is the stage-4 library refusal, now reached for the
  first time: `unit=#72 Probe @[120,14] construction=Pylon status=NOT_STARTED
  pos=[120,14]` - the builder is standing exactly on the site and the construction stays
  `NOT_STARTED` because `Unit.build(...)` returns false (the region-based `hasPath` inside
  the library's `canBuild`, `_AI/PLAN-STAGE4-COMMAND-PATH.md`). So `Pylons=0` in the end
  state is stage 4, not a priority or scheduling problem.

  **Three defects have now been peeled off this one symptom, in order:** the duplicate
  goal/dispatch (#48, fixed), the sliding start frame (#52, fixed above), and the library's
  command refusal (stage 4, open). None of them was the one the previous note suspected.

  **REGRESSION WATCH 2026-10-13 (autonomous LIVE run, `20261013_184412`): the
  slide is BACK under V2 LIVE - `Pylon@557` @2, `@777` @360, `@979` @660
  (never dispatched, only the Probe trains, exit 124 on the cap). The
  PRIORITY_OPENING_SUPPLY band did not hold: either the Pylon is misclassified
  or the band does not decide allocation order as assumed. #48's half stays
  fixed (465 plans, zero duplicate buildings). Next: print the goal's band in
  the plan line, then re-read the scheduler's sort-vs-allocate order.**

- **#57 (2026-10-11) - the duplicate-construction leak is the single root cause behind
  both symptoms the owner reported (Gateway churn AND minerals above 600).**

  **Measured:** three constructions of a Gateway at the SAME tile `[92,113]` plus one at
  `[92,111]`, four probes assigned to them, **zero build attempts**, and the bank at 974
  minerals. The producers are not the bug - `ProduceGateway` fires correctly on
  `A.hasMinerals(150) && freeGateways <= 1`, and the build order lists Gateways - and the
  mineral hoard is a CONSEQUENCE, not a separate gate: minerals accumulate because nothing that
  would spend them ever completes.

  **What it is not:** not position drift (three at one identical tile), not `alreadyExists`
  working as written - a stub-world test that hammers
  `NewConstructionRequest.requestConstructionOf(Gateway)` with a rich bank passes, producing
  exactly one construction. So the duplicate is created through a path that BYPASSES that
  guard, or before the first one was registered.

  **Next measurement, and it is the one that names the bug (do not patch blind):** instrument
  `ConstructionRequests.constructions.add(...)` with `AConsole.printStackTrace(...)` and the
  building type - every add site is then named with its caller chain. That is cheap (~20 lines,
  one run) and it is the same technique that identified the build-issuing chain in
  `PLAN-STAGE4-COMMAND-PATH.md`. The number is new so nobody mistakes this for a duplicate of
  #54: #54 was the refresh loop, this is the construction leak.

  **Related, and it is the same shape as #52's lesson:** a predicate used to ACCEPT must be
  the same one used to REJECT, or the gap between them is invisible until a game trips it.
  Worth checking at the same time whether `buildable:true` should short-circuit the second
  call outright.

- **#58 - Expansion is DECIDED but the Nexus is re-queued instead of finished.** (Was
  mis-numbered #54, colliding with the `RefreshConstructionPosition` item; numbers are never
  reused, so the duplicate takes the next free one - CONVENTIONS §7.) Owner's log
  (2026-10-11), two lines a few seconds apart:

  ```
  2:57: Nexus ADDED TO QUEUE, min=792/ sup=17 / LimitedBases
  3:00: Nexus ADDED TO QUEUE, min=832/ sup=17 / LimitedBases
  ```

  **`LimitedBases` is not a reason to expand, it is the label ON a yes** -
  `ProtossShouldExpand:38` is `if (A.hasMinerals(777) && Count.basesWithPlanned() <= 3) return
  yes("LimitedBases")`. So the decision is correct: 792 minerals, one base, expand. The
  interesting part is the SECOND line - three seconds later, still `<= 3` bases planned, the
  Nexus added to the queue AGAIN. Either the first Nexus was never built, or it was built and
  `basesWithPlanned()` does not count it, and both read the same in the log.

  **REPRODUCED AND THE CAUSE FOUND: `isOverdue` was cancelling the whole plan.** A 1209-second
  run reproduced the re-queue (7× `Nexus ADDED TO QUEUE`, three seconds apart), and the log
  around it named the mechanism:

  ```
  Cancel construction: Pylon    / Pylon is overdue                / buildable:true
  Cancel construction: Gateway  / Last resort - cancelling ALL not started
  Cancel construction: Nexus    / Last resort - cancelling ALL not started
  ```

  `Construction.isOverdue()`:

  ```java
  if (hasStarted()) return false;                    // status != NOT_STARTED || buildingUnit() != null
  ...
  return A.ago(timeOrdered) > 30 * 22;               // 22 seconds
  ```

  `buildingUnit()` is always null on OpenBW (the `buildUnit()` defect, this item), so the
  status carried the entire decision - and a construction whose builder is still WALKING is
  `NOT_STARTED` by definition, because the status advances when the worker starts building,
  not when it is dispatched. A Nexus site 26 tiles away therefore looked exactly like a worker
  that had given up.

  The reaction is what makes it severe: `OverdueConstructions.whenDoesNotRequirePower` cancels
  the construction, cancels every other not-started building, re-queues, and if the re-queue
  returns null runs `Last resort - cancelling ALL not started`. **One slow walk flushed the
  entire plan**, which is why the same Nexus was ordered seven times and almost nothing was
  ever finished.

  **Fixed in `isOverdue`: an assigned, living builder means it is not overdue.** That is what
  "overdue" means - nobody is working on it. Measured after: `Last resort - cancelling ALL`
  goes 0, `is overdue` 0, `Nexus ADDED TO QUEUE` 2 (a normal re-plan), on the same 1209-second
  run.

  **Still open, and it is the next layer:** with the cancellations gone the bot stops at
  `Probe=25 Pylon=4` and builders sit on `Pylon status=NOT_STARTED` 701 frames against 38
  frames of `IN_PROGRESS` - while four Pylons did get built, so `isConstructing` is not a
  reliable "has this worker started" signal either. The completion path from #53 uses it, so
  that is where the next measurement goes: either the status should also advance when the
  building EXISTS at the site (which `buildingFinishedAtSite` already detects for the other
  half), or the engine's `isConstructing` needs a second condition. Do not guess which - the
  probe is cheap and the two have different consequences.

  **2026-10-13 --long game (`20261013_193330`, 409 s, exit 0, SCENARIO FAILED
  only on Assimilators=1/Nexus=1 vs ≥2/≥3): still no Nexus decision -
  `Nexus ADDED TO QUEUE` 0, `QUEUE_BASE` 0, 13 `REQUEST_ACCEPTED` all distinct
  type+tile, zero cancels, DEMOTE fired once (Pylon [79,114], completed anyway).
  Two findings: (a) the legacy path emits no queue markers at all, so part of
  the "silence" is an instrumentation gap - QUEUE_BASE can only fire where
  refresh sees the order, and nothing logs whether it does; (b) 409 s
  undisturbed with no expansion attempt is the D5 threshold shape (3 gateways,
  <630 s), not a queue bug. Next: legacy-path queue logging, or a V2-path run
  where the markers exist.**

  **Queue-birth markers added 2026-10-13:** `AddToQueue` now logs
  `<Type> ADDED TO QUEUE` for buildings (the Nexus twin of the line is what
  named the re-queue) - a construction that never starts now leaves a birth
  trace. Still needs the Nexus-queuing game for the actual hunt.**

- **#56 - `TerranNewBuilding` has the same two finish-path defects `ProtossWarping` had,**
  and they were not touched because they need their own measurement (found 2026-10-11 while
  fixing #53). Measured 2026-10-13 (temporary Terran race flip, reverted same
  turn): 258 s game builds `SCV=21 Marine=5 Academy=1 Refinery=1 CommandC=1
  SupplyD=2 Barracks=1` with zero `No construction for`, zero `CANNOT
  define`, all attempts accepted - the predicted defects did not trigger
  (positional miss needs site drift that did not occur). Open until a game
  shows the symptoms; do not fix blind.

  `TerranNewBuilding.updateNewBuilding` is the Terran twin, and it differs from the fixed
  Protoss path in exactly two ways, both visible in the source:

  1. **No fallback when the positional lookup misses** - `defineConstruction` returns null and
     the method simply `return`s, so a Terran building that went up away from its reserved
     tile leaves its construction in the queue.
  2. **`IN_PROGRESS`, never `FINISHED`** - and `setStatus(FINISHED)` is the only status that
     removes a construction, so a Terran construction that produced its building is still
     counted by `countPendingOfType` for the rest of the game.

  **Why this is a separate item and not a copy of the Protoss fix:** the Terran worker does
  not walk away when the building starts - it is walked INTO the building (the SCV is consumed
  by the construction and `ConstructionStatusChanger` reads `builder.buildType()` for the
  "builder became the building" case) - so the deadlock shape and the correct release point
  are different, and the Protoss fix's `GatherResources(builder)` would be wrong here. Measure
  first: a Terran OpenBW run, `BUILDER_LOG=1`, and check whether `No construction for <unit>`
  and a growing `countPendingOfType` appear. Tracked rather than fixed blind, per
  `CONVENTIONS.md` §3a.

  **Measured 2026-10-13, second Terran game (temporary flip, reverted same
  turn, Protoss re-verified playing): symptoms PARTIALLY PRESENT,
  behaviorally.** 157 s: all literal markers absent (`No construction for` 0,
  `CANNOT define` 0, `engineAccepted=false` 0, one BUILD_ATTEMPT - Depot,
  accepted). But no Barracks was ever attempted: two SCVs Move-loop at
  successive sites ([100,111] 88f→358f, [78,116] 73f→343f, `distToBuild=1.4
  isConstructing=false`), `REQUEST_REFUSED_DUPLICATE Barracks/SupplyD`
  present, roster ends `CommandC=1 SCV=16 SupplyD=1` (0 Barracks/Marines,
  ForceExit on hoarded minerals). So the STALL half shows in behavior while
  neither predicted log-marker fires - next is reading `TerranNewBuilding`
  against the fixed Protoss path, not re-measuring. Item stays open.

- **#69 (2026-10-12) - exit path hangs after the trace write (9 s game, 110 s run).**
  Owner's adhoc log: ForceExit at real 9 s, GameSummary printed, trace files
  written, then no "Killing"/"Exit" prints, one more silent ForceExit (latch),
  frames stopped, timeout killed the JVM at 110 s. Location unproven - candidates
  are `SaveGameResultsToFile` and the kill branch, all silent/fast by inspection.
  Mitigated, not diagnosed: `OnGameEnd` arms a 20 s daemon backstop (`halt(1)`,
  hooks skipped) on the self-exit path, so a hung teardown fails in ~30 s instead
  of burning the cap. Skipped for `END_GAME_VIA_ENGINE` and in tests. Still open:
  catch one hanging run with `jstack` and name the frame.

  **Instrumented 2026-10-13:** `run-openbw-e2e.sh` runs a background catcher -
  while the bot runs, every 10 s it checks log growth; java alive + log stalled
  30 s → one `jstack -l` dump to `$RUN_DIR/jstack-hang.txt`, then it stands
  down (killed after `wait`). Skipped when jstack is missing, silent unless it
  fires, `bash -n` clean. No live verification possible without a hang (12+
  runs all exit 0) - the next hanging run now names its own frame.**

- **#68 (2026-10-12) - per-unit data never attaches ("i???" on click). ROOT CAUSE
  FOUND, fix in.** The viewer fuzzy-matches cvis units against replay units by
  (frame, type, x, y); our `units_first_seen[].type` carried `AUnitType.id()`
  (ours, auto-assigned from 1000 - the trace showed `"type": 1210` for a Probe)
  while everything else speaks BWAPI ids (Probe 64). Zero units matched, every
  click resolved to a negative id. Fix: record `ut().id` at all three write
  sites (discovery, creation hook, seed); the `types_names` table already uses
  engine ids (verified against the JBWAPI enum). Test pins literal 64/176,
  fails pre-fix. Game verification (fresh trace + viewer match) pending.
  Follow-up in this item: per-frame manager+order lines for non-building units
  (viewer renders `units_logs`, not the `manager` update field - wire order
  issue points behind a flag). DONE 2026-10-12: `CV.unitLog` at `BwapiOrderSink`
  (every order, OK/REFUSED) + on manager change; since 2026-10-13 the manager
  mirror is unconditional (owner ask - the flag stays only for per-order
  chatter), verified 22 units with logs in game. A killed run once "collected" the previous
  run's trace and passed - source dir is now purged pre-run.
  Reference analysis 2026-10-12 (`cherryvisdumper.cpp/.h`): shapes confirmed -
  first_seen `{id,type,x,y}` per frame arrays, updates `{task|type}` on change,
  logs `{frame,message,sev,file,line,attachments}`, board `{frame:{k:v}}`
  changed-only, tasks list (absent for us - no UPC system, panel shows None by
  design). Our `units_logs {frame,message}` subset renders (renderer needs only
  those two). Known-minor, not fixed: `board_updates` emits duplicate frame
  keys when two states change on one frame (last wins).
  Viewer-side check 2026-10-12: traces ≤19:43 carry pre-fix 1000+ type ids
  (match nothing, all ???); traces ≥19:59 carry engine ids. If ??? persists on
  a NEW trace, the needed datum is the browser's `[BW<->CP Matching] Unable to
  match N units` console line (names the failing axis: type/position/frame).
  FIXED 2026-10-12 part 2: frame-0 seeds never match (replay has no visible
  units on frame 0; Stardust bumps 0->1 for the same reason) - our `record()`
  clamps to 1, fresh traces seed 107 units at frame "1", no "0" key.
  MATCHER mechanics (read from `openbw/ui/unit_matcher.cpp`, not guessed):
  per-frame, nearest same-type within 25px, ambiguity guards (18-25px needs
  uniqueness in 30px, 10-18px in 20px), one-to-one consumption, NO retry of a
  missed frame. Fragile cases: dense mineral lines, same-spot warps; a WASM
  throw mid-run freezes all later units while early ones keep working.

- **#65 (2026-10-12) - CherryVis per-unit reporting: first-seen fixed, two format
  questions open.** Owner's report: whole-bot reporting worked, per-unit never did
  ("format was wrong or something"). Measured in source, no frontend needed:
  - FIXED: `ACherryVis_UnitFirstSeen.get()` indexed a frame-keyed map by loop
    counter (`unitsSeenAt.get(i)`) - NPE on any real game, killing the whole
    `trace.json` write at game end - and emitted one object per frame instead of an
    array (two discoveries on one frame lost one). Rewritten as frame-to-list with
    id dedup (the shape Stardust writes and the viewer parses); dead predecessor
    `ACherryVis_UnitFirstSeenOld` deleted. Test `CherryVisUnitFirstSeenTest` pins
    the exact JSON without running CherryVis.
  - FIXED: `units_updates` legend. Manager ids are runtime-assigned from 10000 in
    first-logged order, but `custom_names.manager` hardcoded one exact order - any
    other order mislabeled every manager in the viewer, and managers past the table
    had no name. The legend is now built from the runtime table
    (`CherryVisCustomNamesTest` pins it, no frontend needed).
  - FIXED: empty `units_first_seen` in real games. Our listener only recorded
    `UNIT_DISCOVERED`, which BWAPI never fires for our own units - and on OpenBW
    (late attach) that is every unit that matters. Record at creation
    (`OnUnitCreated`, Stardust does the same) plus a start-of-game seed sweep of
    ours and minerals (`OnGameStarted`, Stardust marks static neutrals the same
    way); id dedup makes the overlap harmless. `CherryVisSeedCurrentUnitsTest`.
  - FIXED (owner's install, 2026-10-12): the viewer returned HTTP 500 on every
    page - `replays/views.py` does `import zstd` but the README (and our
    `install_cherryvis.sh`) installed only `zstandard`. The script installs both
    now, and gained `--run-only` (server without reinstall, prints "Starting
    CherryVis server without installation..."). Verified: HTTP 200.
  - FIXED (empty side panel, owner report): the panel loads cvis data through
    `/get/cvis/` while the game canvas does not - so the game played and the
    panel stayed empty with no console error. Root cause in two layers: the
    shipped `zstd` lost the streaming API (first compat patch), and its one-shot
    replacement cannot decode size-less streams (measured `length mismatch` on
    the sample). The endpoint now uses `zstandard`'s streaming reader with a
    plain-file fallback. Verified: sample serves 6.4 MB valid JSON, ours 23
    first-seen frames.
  - PROVEN (2026-10-12): `--short --cvis --save-replay` leaves the viewable pair
    `/sc-ai/REPLAYS/<name>.rep` + `<name>.rep.cvis/` (valid trace.json, 23
    first-seen frames, 16 updated units).
  - FIXED: `units_logs` is no longer `{}`. New `CV.unitLog(message, unit)` API
    (Stardust's `CherryVis::log(unitId)`), stored per unit, rendered inline in the
    legacy single-file shape; `CherryVisUnitLogsTest` pins the exact JSON. First
    gated call sites: build issued/refused in `IssueBuildOrder`, behind the new
    const-false `DebugFlags.CHERRYVIS_BUILDER_ACTIONS` (compiled away when off -
    the per-line flag pattern the owner asked for).
  - FIXED (owner-found 2026-10-12): `zstd-jni` was missing from the bot jar, so
    `onGameEnd` died with `NoClassDefFoundError` and no trace was ever written.
    The lib is packaged now (build asserts `Zstd.class`), the writer path is
    guarded so a debug artifact can never kill the game-end report again, and
    `trace.json` is plain JSON with the zstd copy next to it as `trace.json.zst`.
  - E2E: `--cvis` flag in `run-openbw-e2e.sh` (ENV `GENERATE_CHERRYVIS_REPLAY`),
    artifacts collected into the run dir and next to the `.rep` as `.rep.cvis/`,
    missing trace fails a clean run; `--self-test-cvis` covers the checks.
  Next: decide per-unit logs shape (files like Stardust vs inline), then pin
  `units_updates`+`custom_names` agreement with the same no-frontend string test.

- **#45** Production v2: **`Construction/**` cannot be deleted yet, only `Queue/**`
  is a candidate.** Measured dependency count: **149 production files** reference
  `Queue.get()`, `CurrentQueue`, `CountInQueue`, `AddToQueue` or
  `QueueInitializer` ("who uses this" grep, 2026-10-08). They are the whole
  `production/dynamic/**` tree (`DynamicProductionCommander`,
  `AutoProduceWorkersCommander`, `SupplyCommander`, every
  `ProduceXxx`/`ResearchXxx`), `production/requests/**`
  (`DynamicBuildingCommander`, `RemoveExcessiveOrders`, ...), `ConstructionsCommander`
  and the game listeners. In `PRODUCTION_V2=LIVE` the commander tree drops
  `DynamicProductionCommander` + `SupplyCommander`, but **`ConstructionsCommander`
  stays by design** (v2 delegates builder execution to it), so the `Construction`
  subsystem is live, not dead.

  Ordering that is actually safe (each step ends green, fast suite + ArchUnit):
  1. keep LIVE as the default policy and verify it in a real game (#43);
  2. delete the **dynamic commanders** (`production/dynamic/**`,
     `production/requests/DynamicBuildingCommander`, `RemoveExcessiveOrders`)
     only after v2 covers their behaviour (tech, race-specific army - still
     open, see `_AI/STATUS.md` "what is still open");
  3. delete `Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`,
     `ReservedResources`/`OrderReservations` last, when (2) is done and
     `Construction` no longer needs a `ProductionOrder` back-reference;
  4. keep `Construction/**` until v2 has its own builder (`GameOrderDirector`
     currently *constructs* `Construction` objects to hand execution over).

  **Do not** delete from the bottom of this list upward: step 3 before step 2 is
  a bot that cannot research or build race-specific units.

- **#71 (2026-10-13) - `cantAffordAndDidntExpandYet` reads inverted, unpatched.**
  `IsReadyToProduceOrder:172` blocks every order when the bank is ABOVE 550
  late (700 s+) on one base - the name says "cant afford". Probably missing a
  `!`, but it fires only where no bounded run reaches, so no blind one-char
  fix: verify in a 700 s+ game (owner Wine) that production halts rich, then
  flip with that log as evidence.

- **#72 (2026-10-13) - `ATechManager.researchDynamically` has no callers: Singularity
  Charge is never researched.** The only dynamic upgrade path (Core, supply-gated,
  affordability-checked) is referenced nowhere - Dragoons fight without range
  all game. Wire it (free, big combat value, needs a long-game verification
  that research completes) or delete it (dead code). Owner call either way.

- **#74 (2026-10-13) - Wine builder variance + issue-distance threshold boundary (remainder of #59).**
  #59's fixed halves are verified and its line is deleted: requeue cancel-before-count
  (`GatewayRequeueAfterCancelTest` 2/2, order in `CancelTooLongConstructions:192-194`),
  distTo-vs-center distance (`GatewayRequeueAfterCancelTest` travel case),
  `lastCommandIssuedAgo` debounce (`BuildOrderStarvedByMoveDebounceTest` 1/1, no
  `lastActionMoreThanAgo` left under `production/constructions`), bare-`Move` stop
  (`IssueBuildOrder:184-185`) and the `BUILD_BREAKDOWN` probe
  (`BuilderDiagnostics:105`, via `BuilderManager:85`) - plus the OpenBW stability
  (6/8 recent --short PASS, hold + builderless-demote game-verified). What remains
  needs a Wine `BUILDER_LOG=1` game: builders alternating Builder/Gather on a site
  (the steal-gone confirmation for the hold fix) and builders parked exactly at
  the 1.4-tile issue distance. No bounded OpenBW run reaches either; do not patch
  blind.

- **#75 (2026-10-13) - three Protoss strategies name build orders that do not exist (remainder of #46).**
  `3 Gate`, `12 Nexus`, `Carrier Push` - choosing one starts a game with no build
  order at all (the bot mines and does nothing, no error). The init-order half is
  verified and #46's line is deleted: `ProtossStrategies.initialize()` runs before
  `StrategyChooser` (`OnGameStarted.java:115` before `:118`), and
  `StrategyBuildOrderTest` 5/5 pins the missing set as exactly these three (any
  fourth missing file, or any of the three landing on disk, fails it). What remains
  is owner policy (D5): rename the entries, add the `.txt` files, or delete the
  entries.


- **#77 (2026-10-13) - acceptance tier: 7 stale pins (remainder of #73).**
  The 4 FakeUnit NPEs are fixed (null-guard in `AUnit.isTrainingAnyUnit`, the
  same shape as its neighbors; `ProtossDragoonProductionTest` 2/2 and
  `protoss.ProtossDynamicUnitProductionTest` 3/3 fully green). What remains is
  judgment, not crashes: 3 numeric combat-eval pins (`CombatEvaluatorTest` -
  engine data moved on, touching them touches ADR 0006) and 4 boolean
  behavioral pins (`TerranDynamicUnitsCommanderTest.letsSee`,
  `TerranInfantryWeaponsTest`, `ZealotSelfDefenseTest`,
  `OccupiedTileIsNeverBuildableTest` - fix code or update pins, owner's call
  either way).


- **#78 (2026-10-13) - roster-vs-replay divergence: counting exonerated, pairing suspect.**
  The phantom theory is DEAD for the counting pipeline: `RAW_ENGINE_UNITS`
  dump (`20261013_214302`, temporary probe, removed after reading) shows the
  raw `getUnits()` list matching the roster 1:1 - 28/28 entries exists=true
  completed=true (2 Gateways, 4 Zealots, Core all REALLY there). Our counts do
  not invent units. What remains unexplained is the OWNER's replay showing
  zero gateways for a logged game: the prime suspect is a replay-pairing
  mixup (a stale `.rep` opened for the logged game - the engine mangles
  configured names, REPLAYS accumulates). Needs the owner: which `.rep`
  timestamp was opened, and does it match the run dir? If the timestamps
  match and the replay still shows nothing, the suspect moves to OpenBW
  replay-writing itself. Completed-only roster (D9 executed) stands
  regardless - gates are honest now.


### OpenBW headless run (2026-10-08)

The client attaches and the bot plays - see `_AI/PLAN-OPENBW.md` §9 for the
command, the log and the one setting that made it work
(`BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON`).

Three silent startup defects had to be fixed to get the bot playing; each looked
like "the bot mines and does nothing", with no error:

1. **Startup order**: `ProtossStrategies.initialize()` renames strategies to their
   build-order file names, but `StrategyChooser` picked one BEFORE that, so the
   chosen strategy looked for `PROTOSS_Zealot_into_Goon.txt`. Fixed by
   initialising the strategies first. Test: `StrategyBuildOrderTest`.
2. **`BWAPI_DATA_PATH` was never set for the OpenBW bot directory** (the Wine bot
   directory carries one in its `bwapi-data/AI/ENV`; ours did not). The E2E script
   now writes it explicitly.
3. **`AFile.loadFile` ate the last character of any value ending in the delimiter**
   (`.../bwapi-data/` arrived as `.../bwapi-data`). Test: `AFileLoadFileTest`.
   `AtlantisRaceConfig.validate()` also called `System.exit(-1)` at game start when
   the race was unknown; it now reports and skips.

**Placement is the remaining blocker** for a real game: see #46 and
`_AI/POSITION-FINDER.md`.

**Superseded, and to be solved by a rewrite (2026-10-08):** the owner's decision
is that this subsystem is deleted and written from scratch, so the findings from
one more session of debugging it live on OpenBW are recorded in
**`_AI/POSITION-FINDER.md`** instead of as patches. That file has the measured
facts (the engine calls the refused tiles valid and empty; the refusal came from
our own occupancy predicate, which disagreed with the engine; JBWEB is unusable
on OpenBW in two independent ways; the standard finder was never even reached)
and the traps the rewrite must avoid. Read it before touching any of this.



