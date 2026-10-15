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
2. **Finish Production V2** - `_AI/NEXT.md` #45 and `_AI/redesign/01_PRODUCTION.md`.
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

The suite is green (494 passing, 1 skipped, plus 10/11 architecture with the one
known structural failure, as of 2026-10-14 session;
re-run `bash scripts/run-tests.sh` for the current numbers - do not trust a count
written here). What is open is *quality* of the data behind it, not a red test.

- **#34** Real opponents for the stub-tier scenarios. The two twins hold in
  `tests.e2e` (`FourPoolDefenseTest` 6 lings, `NinePoolDefenseTest` 8 lings) with
their baselines pinned; what is missing is a **real opponent**: OpenBW hosts one
client, so a second bot cannot attach, and a scripted rusher needs a UMS map
that does not exist here. No verdict table is kept in `_AI/e2e/` today
(the last one was deleted by the owner in `e657b83f`); the tier is owner-run. Resume when a scripted UMS map is supplied or the opponent runner is
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

  **Pilot implemented 2026-10-14 (ADR 0007 accepted; game proof pending):**
  `ProductionRacePolicy` port + 3 immutable impls; goal/snapshot selectors
  migrated (8 sites → single `ambient()` selector + kept fortification gate;
  21 We.* lines → 5); engine owns policy, commander resolves once (instance
  field); GoalSourcesTest unchanged-green (equivalence) + per-race identity
  pins (previously unreachable). Selector reads `Main.ourRace()` (We-truth),
  deviating from the ADR's MY_RACE line - documented on the port. DRY_RUN
  log exists (4772 lines, zero V2 errors); first divergence datum: V2 wants
  Forge ×1712 frames, legacy never queues it. Pilot-code DRY_RUN long game
  (20261014_160155, exit 0): zero V2 errors/exceptions, legacy roster
  healthy (Core+Assimilator+Dragoons) - the pilot disturbs nothing. Forge
  divergence persists under the pilot (V2 ×2500+ lines) - tracked parity
  gap, not a regression. V2 LIVE runs still pending.

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

  **2026-10-14 watch datum (`20261013_233256`, not a verdict):** `REQUEST_ACCEPTED
  Pylon [11,51]` x3 at 4:45/4:47/4:50, `totalOfType=1` every time, zero
  `Cancel construction: Pylon`, zero `REQUEST_REFUSED_DUPLICATE`, zero
  REPLACED/DEMOTE for the tile. Three same-tile requests with no cancel, no
  refusal, and a counter blind to siblings (tot1 x3) - the duplicate shape
  without the builder/bank proof. The add-site stack trace prescribed below
  still names the path; do not patch the guard blind.

  **2026-10-14, door instrumented:** `addConstruction` (the single door: legacy
  `NewConstructionRequest:144` with `alreadyExists` re-find-then-cancel,
  V2 `GameOrderDirector:91` with its own exact-tile dedup) prints the caller
  chain per add behind `BUILDER_LOG=1` (`CONSTRUCTION_ADD` + type). Zero
  behavior change (log lines get no pinning test, CONVENTIONS §10). Next: a
  `BUILDER_LOG=1` game showing the duplicate shape - the trace names legacy
  vs V2 path per add.

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

  **2026-10-14 Wine game:** order flapping `READY⇄IN_PROGRESS` after 5:48
  with bank 927+ and no build issue - needs builder/order data, which the
  ENV-source startup line (now unconditional) unblocks; the birth markers
  above plus a `BUILDER_LOG=1` game name the next measurement.**

  **Double order ROOT-CAUSED and fixed 2026-10-14 (`ExpansionNoSecondBaseWhileOnePendingTest`,
  mutation-proven: 2 queued pre-fix, 1 after):** `AddToQueue.maxAtATime`
  counts `nextOrders()`, which skips IN_PROGRESS (`OrdersFilters.next`
  filters it out) - so with the first Nexus IN_PROGRESS the cap read 0 < 1
  and the producer added a sibling (game 20261014_121406: ADDED 6:36 +
  6:45, first IN_PROGRESS throughout, zero cancels). `ProtossExpandNow`
  now refuses while any unfinished base order exists (units keep the old
  counting - a training unit plus a queued next one is normal; Terran's
  twin call carries a same-shape-unmeasured note, not a fix). Still open:
  why neither order converts - the bank sawtooths 24-558 against the 400
  price with zero `REQUEST_ACCEPTED` (army pump is NOT gated by the
  workers-only `SaveForNexus`), which is arbitration (NEXT #45), not queue.

  **Priority mechanism implemented 2026-10-14 (D3, game proof pending):**
  `PriorityWaiters` totals unfinished HIGH+ waiter prices; Nexus queues HIGH;
  worker pump, Dragoon/Zealot producers (first line, exact prices) and the
  `IsReadyToProduceOrder` term yield unless the bank covers both. Rich bank
  passes. Rush-defense twins hold parity (4pool green; 9pool pin updated
  to the measured split in #86/#87).

  **Doctrine refinement (owner, same item):** STANDARD < HIGH (tech incl.
  Core/Assimilator, default at creation) < HIGHEST (detection: Forge/Cannon)
  < MAX (reserved for retry escalation). Supply providers never block
  (152437 froze a Core 310 s behind TOP Pylons); detection-necessity alone
  outranks tech. Convergence holds - paused spenders let the bank grow to
  the waiter. Stargate-class extensions follow the same one-line pattern
  when measured.
  Game proof 20261014_160101 (long, exit 0, zero errors): Core=1,
  Assimilator=1 built; waiters named live - CyberneticsC(HIGH,200/0) ×172,
  Assimilator(HIGH,100/0) ×6 (central tech-HIGH default working);
  Pylon(TOP) pressure no longer freezes tech (carve-out). Nexus expansion
  itself still needs an undisturbed long game (thresholds, D5).

- **#82 (2026-10-13) - off-site Terran completion may leak (unobserved).**
  Found while fixing #56: `buildingFinishedAtSite` fires by site radius, so a
  Terran building completing >2.5 tiles from its reservation would stay
  IN_PROGRESS with its SCV held forever. Never observed in a game (both
  Terran runs built on-site); needs a game showing it. Do not fix blind.

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
  Update: per-unit logs stay inline (legacy single-file shape) and the
  `units_updates`+`custom_names` agreement is pinned (`CherryVisUpdatesLegendAgreementTest`);
  what remains is the owner opening a real trace in the viewer.

- **#45** Production v2: **`Construction/**` cannot be deleted yet, only `Queue/**`
  is a candidate.** Measured dependency count: **124 production files** reference
  (recounted 2026-10-14, same pattern; was 149 on 2026-10-08)
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
     open);
  3. delete `Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`,
     `ReservedResources`/`OrderReservations` last, when (2) is done and
     `Construction` no longer needs a `ProductionOrder` back-reference;
  4. keep `Construction/**` until v2 has its own builder (`GameOrderDirector`
     currently *constructs* `Construction` objects to hand execution over).

  **Do not** delete from the bottom of this list upward: step 3 before step 2 is
  a bot that cannot research or build race-specific units.

- **#91 (2026-10-15) - GoalBoard: ranked arbitration without OpenBW.**
  Owner direction 1A/2A/3A: pure priority board (`atlantis.decisions.board`,
  zero engine imports) replacing scattered vetoes. v1: `BoardState`
  snapshot + 8 phase-1 goals (supply, detection, workers, gateway, core,
  assimilator, army, expansion) on a fixed ladder + in-flight commitment
  with emergency break. Ranking proves in milliseconds
  (`GoalBoardTest` 7/7); wallet/commitment execution and the worker-pump
  driver come next, then Pylon (2A order). Relation to #45: the board is
  the Strategist half without the timeline scheduler (deferred - every
  measured starvation was a priority inversion, never timing). Ratchet
  from today: no new arbitration gate in legacy producers, ever.

  **Advisor live 2026-10-15 (game 110748):** `GoalBoardAdvisor` (last in
  the legacy commander list, read-only) logs top+active every ~60 s:
  supply-blocked -> `supply-pylon`, economy -> `workers`, late game ->
  `expansion`, then `workers` again re-saturating new bases. Zero
  behavior change; divergence lines are the worker-driver data.

  **Driver v1 live 2026-10-15 (owner order 2A): emergency preemption
  only.** Analysis showed a top-only driver would also kill Nexus saving
  (rank 80 by design), so `GoalBoardDriver.pauseWorkers` fires solely on
  emergency-ranked tops with bank short of both. Hooked after engine
  facts in `shouldProduceWorkers` (`BoardEmergency` reason).
  `BoardEmergencyDriverTest` 2/2 full-path (pre-fix pauses via
  `PylonAt8`, new reason wins). Suite 546/546.

  **Driver live 2026-10-15 (game 111441, bot-cap):** BOARD lines flow,
  `BoardEmergency` never fires (no hidden enemies - driver correctly
  dormant), economy healthy throughout (workers 8->22 across lines).
  No behavior change observed, as designed.

  **Feeder (board -> queue, owner order 2A):** `GoalBoardFeeder.ensure`
  queues one order per active unowned goal (supply-pylon TOP, gateway
  STANDARD, core/assimilator HIGH), silent when covered; workers, army,
  expansion and detection stay with their owners (pump, strategy, site
  approval, response system). Queue rows don't spend, so feeding is safe
  by construction. `GoalBoardFeederTest` 3/3 (dedup interplay measured:
  Gateway needs a standing Pylon - `EnforcePylonFirst`). Suite 549/549,
  twins green, arch store unchanged.

  **Feeder live 2x silent by design (games 112418, 112749):** build-order
  rows exist from frame 0, so in-flight never empties for the four
  covered types - the feeder is a rescue net (fires after cancels, like
  the 200018 deadlock would have), not a routine orderer. First
  BOARD_FED line will be its positive proof; stub logic + no-harm games
  (healthy scaling to 35 workers / 122 supply) stand.

- **#71 (2026-10-13) - `cantAffordAndDidntExpandYet` reads inverted, unpatched.**
  `IsReadyToProduceOrder:146` blocks every order when the bank is ABOVE 550
  late (700 s+) on one base - the name says "cant afford". Probably missing a
  `!`, but it fires only where no bounded run reaches, so no blind one-char
  fix: verify in a 700 s+ game (owner Wine) that production halts rich, then
  flip with that log as evidence.

  **2026-10-14, flipped with a stub inversion pin (`LateOneBaseReadinessTest`
  2/2, both red pre-fix):** rich+late+one-base blocked the Probe pre-fix and
  poor+late+one-base flowed - the exact inversion. The negation converges
  (poor blocks saving for the expansion, rich releases); the old shape was a
  livelock (rich blocks, bank grows, never releases). Added the named
  `ORDER_SAVING_FOR_EXPANSION` line (buildings only) - the silent gate is the
  signature the live proof looks for. Live proof still pending: game
  20261014_172954 reached 324 s (trigger unreached, zero ORDER_SAVING) -
  needs a 700 s+ game.

- **#72 (2026-10-13) - Singularity Charge: existing path kept, HIGH waiter wired.**
  Owner's direction 2026-10-14 (and correction: +2 range, not +1):
  `ATechManager.researchDynamically` stays unused - the live path is
  `ResearchSingularityCharge` (rich gating, wired in
  `ProtossDynamicTechResearch`). The dead method is deleted (zero callers,
  suite green). The research order now queues HIGH, so the Dragoon pump
  saves gas via the D3 mechanism instead of out-spending it. Still open:
  long-game proof that research completes (gas 150/150 held to issue).

  **2026-10-14, decision exonerated in stub:** 2 Dragoons + 250 gas + Core
  fires (`SingularityChargeFiresWhenGasHealthyTest` green) - yet zero
  research lines in three gas-healthy games (274-296). Added one throttled
  `SINGULARITY_APPLIES` line at the action point (zero behavior): the next
  game says whether the decision fires live (downstream/engine hunt) or
  not (stub/live divergence hunt).

  **2026-10-14, vector decides (game 182156):** `beingRes=false` throughout
  (stuck-upgrade hypothesis DEAD), `engineRes=false`, `critGas=true` - the
  decision is CORRECTLY idle: `dragoons=1` all late game (needs 2+ via the
  gas-180 rule, or hydras). No divergence, no stuck flag - research waits
  for a second living Dragoon. Refusal-vector wrapper committed (zero
  behavior, `appliesInner` extraction).

- **#85 (2026-10-14) - upgrades never pass level 1.** Owner report:
  multi-level upgrades (weapons/armor) are researched once at most, never to
  levels 2-3. The per-type researchers DO carry level logic
  (`ResearchProtossGroundWeapons.getCurrentUpgradeLevel`), so this is not a
  missing path - name which gate (level check, gas ladder, queue dedup)
  refuses L2/L3 in a game that reached them. Do not patch blind.

  **2026-10-14, narrowed by stub replication (`ProtossUpgradesLevelGatingTest`
  2/2, mutation-proven):** weapons L2 DOES re-fire when rich (800/400 - the
  level logic works, not the blocker); armor was chained behind weapons L3
  (`!Weapons.isResearched()`, bulk commit, no reason recorded - vanilla and
  the Terran researchers carry no such cross requirement) - relaxed to
  weapons L1, keeping the "weapons first" intent. Still open: flat rich
  gates (290/500/550 min) sit ~2x above L2/L3 engine costs (jar: weapons
  150/150 then 200/200, armor 175/175 then 250/250), so the middle bank band
  refuses, and live games sit at gas 0 anyway (Currently broken) - needs a
  gas-healthy game for the L2/L3 completion proof. Rich gates untouched
  (D5-adjacent tuning).

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

- **#77 (2026-10-13) - acceptance tier: 6 stale pins (remainder of #73).**
  The 4 FakeUnit NPEs are fixed (null-guard in `AUnit.isTrainingAnyUnit`, the
  same shape as its neighbors; `ProtossDragoonProductionTest` 2/2 and
  `protoss.ProtossDynamicUnitProductionTest` 3/3 fully green). OccupiedTile is
  done (test aligned to the #49 ruling: worker blocks without a builder, the
  assigned builder is excluded). What remains is judgment, not crashes: 3
  numeric combat-eval pins (`CombatEvaluatorTest` - engine data moved on,
  touching them touches ADR 0006), ZealotSelfDefense (a defend-mission guard
  was tried and falsified by measurement - the claim persists through a
  flapping stub-world proximity selector, open question), and 2
  Terran-race-harness pins (`letsSee`, `InfantryWeapons` - both vetoed by
  `We.terran()` reading hardcoded `Main.ourRace()`, needs a harness strategy
  decision). Owner's call on all.


- **#78 (2026-10-13) - replay-vs-live divergence: OpenBW #27, live is truth.**
  Counting exonerated by raw dump (28/28 exists+completed, 1:1). The pairing
  mixup is DEAD (owner cleared REPLAYS, single file, timestamps match) - and
  the replacement suspect is confirmed REAL: OpenBW issue #27 ("openbw
  creates desynced replay", open since 2019, confirmed by a second user on
  Linux) - bot log correct, generated replays desync shortly into the game.
  Standing consequences: (1) roster/E2E gates read LIVE truth - valid;
  (2) replays are position-viewing only, never unit-existence truth;
  (3) viewer fuzzy-matching compares live-true traces against desynced
  replays, so residual "unmatched" on --save-replay pairs is expected -
  direct unit_ids pairs bypass it for per-unit panels. Fixing the engine's
  replay writer is out of scope (upstream C++, open since 2019, unassigned);
  our OpenBW version has no fix to update to. Reinstall changes nothing.


- **#79 (2026-10-13) - OpenBW replay desync (upstream issue #27): cheap questions first.**
  Confirmed real class (log-true, replay desyncs early; open 2019, second
  Linux confirmation 2021, unassigned, no fix). Our engine is the vendored
  `3rdparty/openbw` (frozen at vendoring + local late-attach patch), NOT
  upstream HEAD; `/sc-ai/OpenBW` is a pristine reference, not what runs.
  Before any engine surgery, in order: (1) minimal repro WITHOUT our bot
  (does a no-bot/scripted game desync?); (2) first-divergent-frame (live
  trace counts vs replay parse - which action type goes missing: morph,
  cancel, warp?); (3) diff vendored replay_saver.h/bwgame.h action paths vs
  upstream (a fix to pull?). Only then patch; every engine change
  re-verifies placement physics + full E2E. Owner priority: fully working
  OpenBW, separate model on it.

- **#81 (2026-10-13) - late Pylon search refuses the whole neighborhood (OpenBW).**
  `20261013_223821_short` (LatCom-OFF A/B): 3rd Pylon "Can't find place" at
  3:02 with bank; `PYLON_FIND_FAILED` counts: `Can't physically build here`
  =1953 plus hundreds of street-grid modulo rejections near the top-right
  corner base [119,10]. The zero-supply grid yield (owner ask) doesn't cover
  21/25 - supply capped to the 4:13 ForceExit, nothing trained (Dragoon=0).
  Same family as #59/#50 (a different refusing predicate each time). Next:
  attribute the 1953 in code (terrain vs grid footprint), then extend the
  yield below a free-supply threshold (owner call - same class as the
  zero-supply yield) or relax the grid near the corner. (Pylon-1 breakdown in
  the same run shows poverty-held 40-88 min, not placement - consistent with
  PylonAt8.)

  **Attribution update 2026-10-14:** part of the 1953 was certainly mislabeled
  doctrine kills - `PylonTooFarFromBaseEarly` never set `_STATUS`, so its
  victims merged whatever stale label the previous tile died on. Fixed
  (zero behavior change): the rule now reports `Pylon too far from main, too
  early`, pinned in `RequestBuildingNearTest`. Long game `20261014_101806`
  (368 s): placement healthy 6/6 Pylons, ZERO FIND_FAILED/Can't-find-place,
  so no TRUE-split data yet - the corner-stall game still decides between
  doctrine and leash-relax. NoSupply down to 21 polls (17/17, 25/25, 33/33,
  41/41, reserved>bank = earmarked money, not starvation).

- **#88 (2026-10-14) - Core never re-requested after cancel (game 152437).**
  One CyberneticsC construction ([112,13], builder stolen to minerals) cancelled
  "took too long" at 34s - then zero Core lines (ADDED/NOT_READY/QUEUE_BASE)
  for 200+ s while the bank swung 100-440. No REPLACED/DEMOTE fired. The
  producer fires in a replicated stub state (`CoreRequeueAfterCancelTest`:
  commander applies + produce() true at 48 supply / 300 bank / 10 Zealots),
  so the live silence is commander-level gating (`MineralsOrQueueSize`:
  min<220 with 5+ not-started orders?) or a silent `AddToQueue` refusal -
  indistinguishable without per-frame Core data (BUILDER_LOG was off).
  Needs a BUILDER_LOG=1 game catching a Core cancel. Do not patch blind:
  the stub contract holds, the live path is unmeasured.

- **#84 (2026-10-14) - Wine late-game Pylon ordered but unbuilt, unattributed.**
  Owner's Wine game: Pylon conversion ordered at 5:01, still unbuilt 38 s
  later while the bank hoarded. Needs a `BUILDER_LOG=1` game to attribute
  (builder? placement? no attempt?) - owner runs without the flag, so this
  waits for the next flagged Wine log. Do not patch blind and do not assume
  #81: that item is an OpenBW corner-search refusal, this one has no
  placement reading at all yet.

  **Wine first-Pylon root-caused 2026-10-14 (`out/wine/client.log` 968
  lines, killed at 103 s, 9/9 + ~400 bank):** PREPARING picked [113,15]
  at 0:27 and the builder was adopted; walking 10 tiles he was cancelled
  for age at 52 s (`took too long`, timeout prices distance but not a
  slow walk) - yet the engine ACCEPTED his order 5 s later
  (`BUILD_ATTEMPT engineAccepted=true`). Two fixes: cancel-too-long
  skips an alive, moving builder (progress, not stall; fleeing reads
  moving too - accepted gap), and the post-cancel rescue bypasses the
  dedup wall (`forceRescueOrder` - `### FAILED ### null` while rows
  stood and zero Pylons stood). `WalkingBuilderNotStalledTest` 2/2.
  Suite 536/536. Wine re-run pending (owner).

  **Second instance 2026-10-14 (early game, `out/wine/client.log` 692 lines,
  map `sscai/(3)TauCross.scx`):** first Pylon (build-order row min-supply 8)
  READY from ~100 s, still unbuilt at 9/9 + ~400 bank when the log ends at
  130 s. Ruled out from the flag-off log: worker stall (supply walked
  4->9, all four rows are build-order min-supplies 8/13/20/27, not a
  minSupply bug), placement refusal (zero `Can't find place`), null builder
  (zero `Builder is null`), any cancel (zero). Remaining, all silent
  without the flag: builder walking far (slow, benign), IN_PROGRESS +
  stuck builder, stale `order.construction()` link skipping the handler,
  consumed-flag stall, silent TrimQueue eat. Every one of them prints
  under `BUILDER_LOG=1` (REQUEST_ACCEPTED/REFUSED, BUILDER_STATE,
  CONSTRUCTION_ADD). Still no blind patch.

- **#89 (2026-10-14) - unit pumps out-spend queued STANDARD buildings.**
  Game 180053: Gateway-2 (ADDED 3:44) never builds with zero
  overdue-cancels - effective bank sits under 150 (samples 266-396 against
  reserved=300) while the Zealot pump trains on a flat `minerals >= 275`
  ignoring reservations. The D3 first lines (worker 50, Zealot 100,
  Dragoon 125) yield only to HIGH+ waiters; one-time STANDARD buildings
  lose the every-frame race against the 100-mineral pump. Fix direction:
  unit spenders (pumps and unit orders) also wait for same-level
  buildings/upgrades/tech; buildings keep waiting only above their level.
  One-time waiters converge, continuous pumps resume (B-22 mirror watched:
  unbuildable STANDARD rows would halt the pump - narrow then, measured).

  **2026-10-14, root cause one level deeper (no pump brake needed):**
  reservations attach on READY but are never released leaving it - every
  READY/NOT_READY flap (bank sawtooth vs reserved pool) inflates
  requirements permanently (game 180053: reserved=300 stuck, effective
  bank under 150, zero cancels). `OrderStatusWasChanged` now releases on
  every exit from earmarked except IN_PROGRESS buildings (builder en
  route, money about to warp); FINISHED-unit forget preserved
  (`ReservationsReleaseWhenNotReadyTest`, red pre-fix). Suite 526/526.

  **2026-10-14, verified live (game 182156, 399 s):** reserved=0 in 941
  samples (pool drains, was stuck 300) - the leak is closed. Roster
  Nexus=2 Assimilator=1 Gateway=1 Dragoon=1 (verdict reads finished
  units: Gateway<3, Dragoon<5, Assimilator<2, Nexus<3 all fail). A second
  Assimilator was walking to the natural ([86,118], @5760+) when the
  tripwire fired. Combined single-game proof still open.

  **2026-10-14, brake design corrected before coding:** the pump ALREADY
  respects reservations at `ProduceZealot:100`
  (`bank >= reserved+100`) - the leak is the early plenty-rules above it
  (`NotEnoughZealots`, `EarlyGameZealots`, `NoAssimilator`) firing first.
  Added throttled `ZEALOT_TRAIN reason=` / `DRAGOON_TRAIN` lines at both
  choke points (zero behavior): the next game names which rules fire in
  the starvation window, then the brake moves precisely.
- **3+ Dragoons PROVEN LIVE (2026-10-14, game 192653_long, 581 s, killed
  by bot-cap mid-late-game):** `DRAGOON_TRAIN dragoons=7` (`Count.dragoons`
  = completed only) with Nexus IN_PROGRESS (3rd), supply 94/106, Photon
  Cannon dedup refusing correctly (`existing=1`). The 191650 single
  Dragoon was throughput + short game (pump trains proven there too);
  longer games compound. Combined 3+2 in one finished game still open
  (this one was killed, no roster).
- **FULL --long GATE PASSED 2026-10-14 (game 210002_long, 912 s, exit 0):**
  Nexus=6 Probe=81 Pylon=11 Gateway=4 CyberneticsC=5 Zealot=5 Dragoon=7
  Forge=1 (first Forge ever). Combined proof exceeded (7 + 6).
  Discipline next, measured: Nexus ADDED 12 (~6 converting, expansion
  loop healthy); Core ADDED 20 for 5 standing (2x churn per Core -
  duplicate-leak territory, #57); Forge 1/1.
  **#57 side datum (same games):** every Gateway add in 4 games comes
  through the single legacy path (`NewConstructionRequest` via
  `ProduceBuilding`, zero `GameOrderDirector` - V2 DRY_RUN builds nothing);
  the 233256 triple-accept shape did not reproduce - still needs its game.

## Currently broken (2026-10-14, game 20261014_162906_adhoc, 335 s, exit 0)

Roster: `CyberneticsC=1 Probe=22 Pylon=5 Zealot=4 Dragoon=1 Assimilator=1
Nexus=1 Gateway=1`. Tech chain EXISTS (Core+Assimilator built, waiters named
live: `CyberneticsC(HIGH,200/0)`, `Assimilator(HIGH,100/0)`), but the game
is thin and hoards (603 minerals at the tripwire end). What does not work:

- **Gas never accumulates: 0 in all 177 bank samples.** Exactly one Dragoon
  proves gas flowed transiently, then stopped - no sustained mining all game.
  Undecided between late Assimilator completion (too little time left to
  matter) and gas miners never sticking (assigned, then pulled). No
  gas-worker evidence lines exist in this build. Needs a run with gas-miner
  visibility, not a blind fix.
- **One Gateway all game - producer FIXED and firing in game, order stuck
  behind phantom waiters (game 20261014_172954_long, 324 s, exit 0):**
  `Gateway ADDED TO QUEUE` at 3:44, min=356 - the old code refused there
  (free Gateway + bank <=360), so the 500->300 floor + deleted early lines
  fire live. But the order then waits 177x (`SecondGatewayQueuedTest` still
  green): `for=CyberneticsC(HIGH,200/0)+Assimilator(HIGH,100/0)` while BOTH
  buildings stand (roster Core=1 Assimilator=1). The waiters are the
  build-order rows (`Zealot into Goon.txt:28,30`) - legacy built the
  structures without linking a Construction, and `IsOrderCompleted:23`
  answers false forever on `construction()==null`. Same non-convergence
  shape as #71 pre-fix. **Fixed 2026-10-14
  (`UnlinkedBuildingOrderCompletionTest` 2/2, both red pre-fix):** an
  unlinked order completes when more completed buildings of its type stand
  than same-type rows already finished (fungible rows keep count - two
  Gateway rows + one standing Gateway finish exactly one). Suite 517/517.
  **Extended 2026-10-14 (game 174710: Assimilator waiters survive the
  unlinked fix):** that order is LINKED with a null unit (queue-built 4:04,
  engine blind) - the fallback now also covers linked-but-orphaned
  constructions, gated on NO active work (a warping Nexus-2 keeps waiting
  against its standing main, else the reserve drops early and #58 re-queues
  a third). 4/4 green, orphan case red pre-fix, Nexus-2 guard green both.
  Game proof pending (Gateway-2 must leave WAITING in the next game).
  Aside: Core's waiter
  clears in the last 5 lines (Assimilator-only) - unexplained, recorded.
- **Gas: assignment confirmed, accumulation still absent (same game):**
  `GasWorker assigned` x3 at ~271 s, yet gas=0 in all 177 bank samples with
  Dragoon=1 (transient mining, then dry). Late-Assimilator branch gains:
  no Assimilator ADDED/FINISHED/construction lines at all (legacy path,
  known marker gap) - miners top up at the very end with no time to matter.
  The steal branch still needs a gas-healthy mid game, not a guess.
- **Assimilator duplicate (#58 shape) - guarded 2026-10-14 (game 174710:**
  Assimilator+Assimilator waiters for one geyser): the dynamic add only
  checks the next-2 cap, so a progressing (IN_PROGRESS-skipped) build-order
  row is invisible and a second is added (4:04). Progressing/ready rows now
  block the dynamic add; stuck notStarted rows are still replaced by it
  (`FirstAssimilatorNoDuplicateTest`, red pre-fix). With the completion
  fallback the surviving row clears once the refinery stands.
- **Gas: formula cleared, assignment needs game telemetry:** the demand side
  is pinned (`GasMinersAssignedTest`: 22 workers demand 3); engine data
  exonerated (player gas synced, refinery is a container). Added one
  throttled `GasWorker assigned` line - the next game says assign-vs-steal.
  Harness fields probed, not memory.
- **Late spending stall:** 603 bank, free supply, idle Gateway, zero
  production-refusal lines for units in this build - the pump's reasons are
  silent when they decline. Nothing names which gate held the last 60 s.
- **STANDARD-vs-STANDARD arbitration (2026-10-14, game 180053):** Gateway-2
  (ADDED 3:44) never builds with zero overdue-cancels - correctly waiting,
  not stuck: effective bank sits under 150 (samples 266-396 against
  reserved=300) while the Zealot pump trains on a flat `minerals >= 275`
  ignoring reservations. One-time buildings lose the every-frame race
  against the 100-mineral pump. Same family as #45 (scheduler), not a gate
  to flip blind (B-22: the opposite failure killed all unit production).
  Game proofs so far: Nexus=2 (175707, 180053), Dragoons=3 (175329),
  Gateway=2 (175329) - each in a different game; combined single-game
  proof still open.
- **Second Nexus absent - decision FIXED, game proof pending (2026-10-14):**
  vs Zerg the old gates needed 11+ units (`DelayNaturalVsZerg`) and 550+
  bank / 90+ strength before the affordable rule was even reachable - a
  5-unit game never got there. New `SafeAfford` rule first in
  `forSecondBase`: 400+ bank + 4+ combat units + one base + nothing in
  production + no pressure at natural/choke/base (observed, not proxies).
  Rush safety: a rush never coexists with a 400 bank, pressure blocks.
  `ExpandSecondBaseWhenSafeTest` pins it (red pre-fix). Suite 518/518.
  Still needs the long game: decision -> HIGH order -> pump yields (D3) ->
  400 held -> REQUEST_ACCEPTED -> builder walk.
- **Hoarding tripwire cut a healthy game (2026-10-14, game 175329):**
  `ForceExitLocallyInOpenBW` ends the run at 600+ minerals, but a Nexus-2
  reserve legitimately sits at 400-600 while warping - the game died at
  385 s with Nexus-2 accepted at 5:51. Saving is not hoarding: an
  unfinished HIGH+ waiter now suppresses the exit
  (`HoardingTripwireRespectsSavingTest` 2/2, saving case red pre-fix);
  with nothing waiting, 600+ unspent still exits. Suite 523/523.
- **Stolen builder stalls construction forever (2026-10-14, game 175707):**
  Gateway-2's builder (#117) mined 4000+ frames while its site sat
  NOT_STARTED - no cancel, no finish. The #58 shield (living builder =
  not overdue) could not tell walking from stolen. `isOverdue` now
  excludes builders back on minerals/gas while affordable; walking ones
  still shield (`StolenBuilderOverdueTest`, red pre-fix). Suite 524/524.
- **Built rows must leave next-orders (owner 2026-10-14):** a row whose
  builder is dead (or missing) tracks nothing - `isConstructionActive`
  now returns false there, so the standing building satisfies it by
  existence. A warping Nexus-2 with a living builder still waits (reserve
  kept, no #58 triple). `linkedDeadBuilderCompletesByExistence` pins it
  (red pre-fix, Nexus guard green both). Suite 527/527.
- **Continuous early probe stream (owner 2026-10-14, critically):** the
  worker pump waited for NOT_READY tech rows from frame 30 (Core+Assim
  300 -> 350 bank with 4 probes). Only READY/converting HIGH+ waiters
  pause workers now; bases still pause from queued (saving TO 400 takes
  minutes - `nexusQueuedPausesWorkersBelow450` holds). Army pumps
  unchanged (saving for tech is strategy). Rewrote
  `coreQueuedPausesWorkersLikeNexus` into notReady-flows/ready-pauses
  (old expectation was the strangler). Suite 527/527.
- **Dragoon=0 + Nexus=1 in 344 s (2026-10-14, game 183050_adhoc): tempo,
  not gates.** Cascade: Core ~228 s (waiter span) -> Assimilator ordered
  4:04 -> first gas @279 s (peak 232) -> Dragoons earliest ~5:30; min-6
  Zealot forcing ate the bank to Zealot=5 (4 trains, all NotEnoughZealots)
  and the game ended at 344 s just as gas arrived. Nexus ordered 5:12 +
  5:15 (two real orders - Nexus logs TWICE per add, resolved in
  `AddToQueue:146+162`; second despite the #58 guard, trim-eat suspect),
  READY with 472-536 bank but never accepted in 29 s; zero placement
  errors, zero builder-null, zero cancels - conversion unattributed
  (BUILDER_LOG off). End-cause unattributed too (no banner, p1_win, clean
  onEnd). Needs: earlier gas (Assimilator order ~3:00) + a BUILDER_LOG
  window over the Nexus READY stall + a game past ~7:30.
- **Owner directives verified live (2026-10-14, game 191124_long, 532 s):**
  Probe=26 (stream flows, supply-9 pace doubled vs Wine: 53 s vs 119 s),
  ORDER_WAITING 2 lines total all game (was 177-291 - built rows leave,
  queue drains), Nexus=2 Assimilator=2 Dragoon=2. Pendulum swung: bank
  1000+, Gateway ADDED x8 / constructions x14 for 6 standing (rich-bank
  spam, needs discipline), Pylon=3 underbuilt. Combined 3+2 proof still
  open; Dragoon throughput next (capacity now exists).
- Aside (healthy): scout terminals fire in game
  (`ScoutEnemyBasesWithoutBase` ×5); `Scout idle` ×3, rare null-chain,
  telemetry only.
- **#90 (2026-10-14) - PREPARING sites, picked one supply early (owner).**
  New `ConstructionOrderStatus.PREPARING`: site picked (Pylon-8 at supply
  7), no builder, no issue; `ProduceBuilding` adopts it (builder +
  NOT_STARTED) once ready instead of searching anew. Overdue/builders/
  status-changer ignore it; completion falls back to existence; orphans
  swept with the refresh. v1: ordinary buildings (bases/gas stay old
  path), one site in flight per type, hook in `QueueRefresher`.
  `PreparingConstructionSiteTest` 4/4 (hook-stash: 3 red).
  Game proof pending (PREPARING lines + earlier first-Pylon conversion).

  **2026-10-14, first game killed it in 87 s (game 200018, 1209 s, zero
  buildings):** the cancel-too-long path has no PREPARING guard, so it
  cancelled the 48 s old site; the requeue vetoed on 4 untriggerable rows
  (`REQUEUE_VETO notStartedInQueue=4 maxAllowedAtOnce=2`) and the dangling
  order link blocked both re-prepare and handler - 9/9 deadlock to the
  tripwire. Fixed: cancel path skips PREPARING; the refresh sweep also
  nulls links to listed-nowhere constructions. 6/6 green (cancel-guard
  red pre-fix). Suite 533/533.
- **Requeue veto counts only triggerable rows (same game):** rows at
  13/20/27 can never convert at frozen 9/9 supply, so they must not veto
  the rescue rebuild (`RequeueVetoCountsTriggerableTest`: old count 4 vs
  triggerable 1; structural guard test updated, cancel-before-count
  intent preserved). Suite 534/534.
- **Deadlock broken live (2026-10-14, game 202326_long, bot-cap 110 s):**
  first Pylon cancelled at 54 s -> `### FAILED ### Requesting Pylon`
  FIRED (was vetoed) -> game flows: supply 65/89, 27 workers, Dragoons
  0->1->2->4 compounding (gas 94->234), Zealots to 10. PREPARING picks
  sites early throughout (8/13/20 rows). First-Pylon exact frame still
  unattributed; Pylon IN_PROGRESS seen @8640.
- **Worker re-queue at 15 frames left (owner 2026-10-14):** `queueWorker`
  threshold 10 -> 15 (`NEXT_WORKER_WHEN_TRAINING_REMAINS`, pinned with
  the 300-frame engine number). The stub always direct-trains, so the
  path itself is game-proven only - watch early probe cadence.
- **First Pylon converts at 8 (owner 2026-10-14, Wine):** unconvertible
  HIGH rows (Core/Assim, NOT_READY for minutes) blocked every building
  order until ~350 bank. `IsReady` waiters are convertible-only now
  (READY; IN_PROGRESS money is sunk); pumps keep the status-blind
  variant (army saving is strategy). `FirstPylonConvertsAtEightTest`
  pins the whole pipeline (READY + conversion + builder, red pre-fix).
  Suite 537/537.
- **Verified live 2026-10-14 (game 211152_long, bot-cap):** PREPARING
  Pylon at 0:28, builder on site at frame 1020 (42 s), standing Pylon
  later powering checks; supply-9 at 53 s; Dragoons 3->5 compounding,
  Nexus decisions firing (ManyWorkers + SafeAfford). First Pylon ~1.5
  min vs Wine never-at-130s. Final proof = your Wine re-run with the
  current jar.
