# Case studies — resolved investigations, kept because they explain the code

> These are the **long narratives of resolved defects**. They were living inside
> `NEXT.md`, which is meant to be a scannable backlog, and they made it 1300 lines of
> history with the open items buried in the middle. A closed item's line is deleted
> from `NEXT.md` (CONVENTIONS §7) and the git log is the archive; what is kept HERE is
> the part a future reader needs and git history hides - the mechanism, the withdrawn
> readings, and the traps.

The rule for what belongs here: a defect whose *cause* was non-obvious and whose
explanation would save a session. One section per defect, named by symptom. This is not
a changelog - `git log` is - and it is not a backlog - that is `NEXT.md`.

---

- **#47 - OpenBW E2E must be fully working (TOP priority).** The runner attaches
  and is bounded, but an E2E test is not "fully working" until all of these hold:

  1. **Attach is now reliable - fixed 2026-10-09.** The host was killed a few
     seconds after starting and left its PID in the registry, so the client
     looped on `No server proc ID` while the run still reported exit 0. The bot
     JVM is now killed 10 s before the host, so the host outlives the client, and
     a log with no `HELLO_ATLANTIS` is an assertion failure. Verified: three
     consecutive runs attached and passed. Remaining: a run that fails for some
        other reason must still be distinguishable from a pass. **Non-zero bot exits
        now fail before scenario assertions** (a timed-out run previously printed
        `SCENARIO PASSED` with no expectations); this is pinned by
        `LauncherBuildsTheJarTest` and fast suite 346/346. A real non-zero process-exit
        integration run is still unverified.
     2. **A Pylon is placed in a real run.** NO at first, and the cause chain
     ran through three wrong readings, each killed by a probe (transcripts in
     git): "the engine lies" (wrong: `engBuild=true`, only genuinely occupied
     tile (7,44) refuses); invisibility (wrong: 1149 invisible tiles answer
     `buildInc=true`); "unbuildable search area" (artefact of a diagnostics
     bug - plus our own failure print logged `x == y`, fixed, print-only).
     Measured roots: the first-Pylon `nearTo` was the base coordinate itself
     (a `translateTilesTowards(mineralsCenter, 50)` landed 36 tiles off-map,
     `ensureWithinBounds` nulled, guard skipped - rewritten to push 7 tiles
     off the base), and `Region mismatch` fired on every candidate
     (`Game.hasPath` is region-based, always false on OpenBW - routed through
     `MapTiles.hasPathBetween`). Result: `SUCCESS FOR Pylon at 113,15`,
     failures 2208+ to 0. Engine jar ground truth used throughout: Pylon 2x2,
     Gateway 4x3.

     **NEW BLOCKER (open, and it is #48's territory): the builder never starts.**
     With placement working, the log shows the construction is created and assigned -
     `Builder diagnostic: unit=#118 Probe @[113,15] (BuilderManager) construction=Pylon
     status=NOT_STARTED`, 1799 frames of `BuilderManager` in its history - and then
     `Cancel construction: Pylon took too long (44s) / at:[113,15] / buildable:true`.
     So the tile is good, the builder is assigned and in `BuilderManager`, and the order
     is never issued. **`Pylons=0` is now this, not placement.**

     **Do not re-open the placement hunt on account of `Pylons=0`:** placement benefits
     from `canBuildHere`, `BuildingTilesAreOccupied`, the terrain flags and visibility
     were all measured and none of them was the cause; the remaining failure is
     builder execution.

     A second, independent defect is confirmed but is not the blocker: 549 log lines
     of `Region mismatch` (`IsProbablyInAnotherRegion` asks `hasPathTo`, which is
     region-based and always false on OpenBW because the server publishes no
     regions - `OpenBW.md`). It is reached after `CanBuildHere`, so it is a
     follow-up once placement accepts tiles at all.

     If the default legacy path still refuses after the owner's latest changes,
     diagnose its candidate search from a fresh bounded run. The planned long-term
     option remains the Production V2 cut-over in `_AI/PLACEMENT-CUTOVER-PLAN.md`;
     #48 tracks the separate LIVE scheduler/dispatch blocker.
  3. A scenario file instead of loose environment variables (not implemented).
  4. A deliberately broken build fails the same scenario (not verified).
    5. The 7-minute survival scenario passes with Pylon>=1, Gateway>=1,
       `EXPECT_MIN_INGAME_SECONDS=420`, `EXPECT_MIN_KILLED=12`,
       `EXPECT_MAX_KILLED=40`, `EXPECT_MIN_RESOURCE_BALANCE=-200`, and no placement
       refusals. Still not verified on OpenBW.

  - **#49 CLOSED 2026-10-11 - A PYLON IS BUILT, AND THE CHAIN RUNS.** The fix that finished
    it is written up in the commit that follows the library patch: `DefineConstructionForNewUnit`
    matched a new building to its construction by NOT_STARTED status, while the building's own
    construction was already IN_PROGRESS by then - so every building logged
    `CANNOT define construction` and its worker stayed claimed forever. Matching is positional
    now. With that, `CANNOT define construction`, `No construction for`, the
    `Refresh .. position` / `WTF?!? Cancel and request again` loop and `Can't find place` are
    all ZERO in a run, and the bot completes a tech chain:
    `Pylon=4 Gateway=2 CyberneticsC=1 Assimilator=1`. `### Protoss buildings: Pylons=1 ###`,
    with `engineAccepted=true`, `getOrder=PlaceBuilding`, and the bot continuing to the
    next build-order item (a Forge). The remaining blocker turned out to be one line in
    JBWAPI (`Game.hasPath` is region-based; OpenBW publishes no regions, so it answered
    false for every pair); it is fixed in a local source checkout and shipped as
    `lib/jbwapi-2.2.0-atlantis.jar`, with the reasoning in
    `_AI/PLAN-STAGE4-COMMAND-PATH.md`. **Do not look for a placement bug here any more -
    the three that existed are all closed** (#48 duplicates, #52 slide, occupancy rule),
    and the search was already finding valid tiles (`Region mismatch` 549 -> 0).

  - **Superseded note (kept as the record of the investigation): #49 is the single
    blocker that gates everything else on OpenBW.** Measured
    2026-10-10 with a fresh bounded run (bot exit 0, `ingame=251s`, and
    `MAP_ANALYSIS areas=21 chokes=42` so the map model is healthy): the only
    building the bot ever tried to place was the Pylon, and it was refused once
    (`Can't find place for \`Pylon\`, At 8 Pylon (READY_TO_PRODUCE)(#1)`) after
    which no further construction was requested at all. So nothing downstream can
    be exercised - a Cybernetics Core never gets far enough to test the power rule
    (#50), and the mineral-distance rule (#51) never gets a candidate to reject.
    **Do not read a green suite or a clean run as evidence that #50/#51 work in a
    game: they are behind this item and currently unexercised.**

  - **#49 - IMPLEMENTED 2026-10-10 (contract + wiring); it does not change the outcome
    yet, because the library still refuses the command (#49's own note, below, predicted
    this shape).** `BuildingTilesAreOccupied.check(position, type, builder)` now asks the
    rule the game enforces - the footprint may contain the assigned **builder** and
    nothing else - and the two production call sites that have a builder pass it:
    `RefreshConstructionPosition.shouldRefreshConstructionPosition` (which had
    `construction.builder()` in hand and was asking "is a unit on the tiles" without it,
    so a builder arriving at its own site looked like an obstacle) and
    `MapTiles.canBuildHere` (whose occupancy guard called a tile occupied by the builder
    that had walked there to build). `checkBuildingsOnly` keeps the old question for
    callers that genuinely ask it.

    Verified: 8 cases in `PlacementSearchMatchesCommandTest` (mutation-checked - dropping
    the builder exemption fails one), 405/405 fast suite, ArchUnit 7/7.

    **Live result, stated plainly: `Pylons=0` is unchanged**, and `construction=Pylon
    status=NOT_STARTED` is logged 2038 times. That is the expected consequence of the
    stage-4 blocker, not a failure of this fix: the order IS issued and the library
    refuses it, so the construction status cannot advance whatever occupancy says. #49 is
    correct and necessary; it is behind the same wall as everything else on this path.
    Do not read the unchanged `Pylons=0` as evidence against the rule.

  **The mechanism behind the Pylon refusal, as originally measured:**

    ```
    BI0_TILES Pylon at=[7,43]  (7,44) explored=1 visible=1 unitOnTile=2
                               (8,44) explored=1 visible=1 unitOnTile=1
    ```

    The refused footprint tiles are walked by our own mining workers. The engine is
    right to refuse them: `isBuildable(...,true)` counts **units**, not just
    buildings, and a mining worker on the footprint is a real blocker.

    **The rule (owner's ruling):** StarCraft allows a building to be placed **under
    the unit that is about to build it** - the builder is not an obstacle, because
    the engine moves it onto the site. Every **other** unit on the footprint blocks.
    When the search runs for the first time no builder is assigned yet, so then
    nothing may be on the footprint at all.

    Both halves landed via #49 (builder-exempt occupancy + `PlacementSearchMatchesCommandTest`); the full rule discussion lives in `_AI/CHALLENGES/Bwapi.md` (`bi=0`).

---

- **#53 CLOSED 2026-10-11: the finish path was the missing half - `ProtossWarping`.**
  Verified by execution: the same bounded OpenBW run that used to end at `Pylons=0/5
  gateways=0` now finishes `### Protoss: Nexus=1 Probe=13 Pylon=2 Gateway=1 ###`
  (`Total time: 125 seconds`, exit 0), with `CANNOT define construction` 0 (was a 1799-frame
  wall), stuck `Gateway NOT_STARTED` frames 3534 -> 86, and build attempts reaching the
  engine 0 accepted -> 3/3 `engineAccepted=true`. Tests: `OrphanedConstructionTest` (2),
  fast suite 418/418, ArchUnit 7/7. The per-frame `BUILDER` trace was removed in the same
  cycle (10 283 log lines -> 64, `BUILDER @` = 0). What stays open under the item below is
  the strategy half (should a worker held at a site that cannot start return to mining).

  ---

  **Original report (kept as the record of the hunt):**

  **ECONOMIC LIVELOCK: the bot stops making workers, and then cannot afford anything else.**
  Found 2026-10-11 by the owner asking why no Gateways were built (`pylons=5 gateways=0`,
  ingame=1209 s).

  Three wrong readings in order, each killed by a probe (transcripts in git):
  worker `hasMinerals(250)` gate (real but secondary - unfunded workers earn
  nothing either way); worker gate as cause (`WORKER_GATE CantAfford`, 180
  identical frames - a symptom: `minerals=0` with 5 workers means nobody
  mines, and the census read `BuilderManager 2631` vs `GatherResources 3`);
  cyclic deadlock as cause (constructions wait for minerals while holding
  every worker - true but not the root).
  The root, settled by `BUILDER_LOG=1`: a Probe ON its site, tile buildable,
  minerals present, order issued, engine reporting `isConstructing=TRUE` -
  while our `Construction` stays `NOT_STARTED`, so the worker never returns.
  Layer trace: `ConstructionStatusChanger` advances on `builder.buildUnit()`,
  which reads shared-memory `unitData.getBuildUnit()`; OpenBW sets
  `u->current_build_unit` (`bwgame.h:8203`) but the id-table conversion
  (`UnitUpdate.cpp:333`) yields -1, so the client reads `null`.
  Fourth instance of one engine pattern (after `regionCount = 0`,
  `isOccupied`, `hasPath`): a field StarCraft fills and OpenBW does not
  fill the way BWAPI expects. Fix on our side of the seam (`isConstructing`
  IS published correctly, `UnitUpdate.cpp:359`), no library fork.

  ### Three fixes landed, and the last probe showed the real shape

  What was implemented and measured, in order:

  1. **Status advances on `isConstructing` + builder at the site** (not on `buildUnit()`).
     Verified: `status=IN_PROGRESS` appears 150 times in a run where it was zero before, and
     the run reached `Pylons=3 Gateways=2` with minerals peaking at 306 (was ~46).
  2. **The "was destroyed" guard no longer fires when the building was simply never seen.**
     It read `(building == null || !building.isAlive())`, and `buildingUnit()` is null on
     OpenBW for the whole construction, so it cancelled live Pylons 30 frames after they
     started - measured 5 such cancels. Now it requires a building that WAS seen and has
     since died.
  3. **A finished construction is now removed** (`FINISHED` + `removeOrder`), which is what
     gives the worker back to mining.

  **Fix 3 does not fire yet, and the probe says why - this is the actual remaining
  defect, and it is not in the status logic at all:**

  ```
  FINISH_CHECK Pylon at=[16,46] found=null isCompleted=false
               status=NOT_STARTED builderAtSite=false
  ```

  On the tile the construction reserved there is **no building**, and the builder is not
  there either - `Pylons=3` in the same run proves Pylons were built, just **somewhere
  else**. So the construction and the building it produces have lost track of each other,
  and the worker is held by a construction whose site is empty forever.

  **FOUR FIXES LANDED, and the measurement that closes the loop (2026-10-11).** Each was
  verified by a run, not by reading:

  1. status advances on `isConstructing` (not on the null `buildUnit()`) - `IN_PROGRESS`
     went from zero to 2631 occurrences;
  2. the "was destroyed" guard no longer cancels a building it never saw - 5 false cancels
     measured before, none after;
  3. a finished construction is removed - **still does not fire, and now we know why**;
  4. **the Protoss "do not switch while moving" guard no longer blocks an ARRIVED builder**
     (`TravelToConstruct.asProtossMultiBuilderDoNotSwitchConstructions`): its check is
     `lastActionLessThanAgo(20, MOVE_BUILD)` and `moveToConstruct` refreshes that action
     every 13 frames, so it was true forever and `IssueBuildOrder` was never reached.
     Measured before: a builder one tile from its Gateway with `action=MOVE_BUILD`,
     `lastActionAgo` climbing 0..44, never `Build`.

  **Fix 4 is the one that made the bot build both types reliably:** `Pylons=2 Gateways=2`,
  `Gateway status=IN_PROGRESS` 1344 frames, exit 0.

  The finish half was NOT status logic: `ourOfType(Pylon) = 1` with
  `ourOfTypeWithUnfinished=0` in the same breath - a Pylon stands, and
  the construction waiting at `[16,46]` has no Pylon within 12 tiles.
  The construction's recorded site is not where its building was put,
  have a building. **`FINISHED` stays 0 for that reason alone.**

  ### CLOSED 2026-10-11: the finish path was the missing half - `ProtossWarping`

  **This was it.** `ProtossWarping.updateNewBuildingJustWarped` runs from `OnOurUnitCreated`
  for a building that now exists - i.e. exactly when a construction is over - and it did two
  things wrong, both measured and both fixed:

  1. **It marked the construction `IN_PROGRESS`, never `FINISHED`.** `setStatus(FINISHED)` is
     the only status that removes it from `ConstructionRequests`, so an `IN_PROGRESS`
     construction lasted the rest of the game: `countPendingOfType` counted a building that
     already stood, and `BuilderManager.isBuilder` kept treating it as a reason to hold its
     worker. Now `FINISHED` - which is also what the method's own javadoc claimed all along
     ("remove them from construction queue").
  2. **A building that appeared away from its reserved tile matched no construction at all.**
     The lookup is positional (`DefineConstructionForNewUnit`, 2.5 tiles), and the site moves
     (`RefreshConstructionPosition`) or the engine snaps a footprint. Now the positional hit
     still wins, and on a miss the oldest unfinished construction of the same type is adopted
     - the one whose worker has been claimed longest, which is the point.

  **Evidence, unit tier:** `tests/unit/OrphanedConstructionTest` (2) - a Pylon on its reserved
  tile and a Pylon 8 tiles away both leave `constructions` empty with `builder=null`; the first
  was green before the fix for the wrong reason, which is why both cases are pinned. Fast suite
  418/418 (was 405 + these 2 and the suite's own count), ArchUnit 7/7, store unchanged.

  **Evidence, game tier (bounded OpenBW run, `BUILDER_LOG=1`, TauCross, Protoss vs Protoss,
  `Total time: 125 seconds`, exit 0):** the run finishes with
  `### Protoss: Nexus=1 Probe=13 Pylon=2 Gateway=1 ###` - **two Pylons and a Gateway actually
  built**, which is the thing the item was about. Before/after on the same command:

  | measure | before | after |
  |---|---|---|
  | `BUILDER @` diagnostic lines | 10 283 | 654 |
  | stuck `Gateway status=NOT_STARTED` frames | 3 534 | 86 |
  | stuck `CyberneticsC status=NOT_STARTED` frames | 1 084 | 139 |
  | `CANNOT define construction` | a 1799-frame wall | **0** |
  | buildings built | `Pylons=0/5 gateways=0` | **`Pylon=2 Gateway=1`** |
  | build attempts reaching the engine | 0 accepted | 3/3 `engineAccepted=true` |

  The status now advances in a live game (`NOT_STARTED -> IN_PROGRESS` on a Pylon at `@1137`,
  immediately after `engineAccepted=true` and `getOrder=PlaceBuilding`), which is exactly what
  never happened before. **This is the measurement that closes the finish half of #53.**

  **What is still open under #53:** the *engine-side* half. `buildUnit()` is null on OpenBW
  (the harness's `getUnitFromBWAPI` id lookup fails - see `CHALLENGES/OpenBW.md`), so
  `isConstructing` and "the building stands at the site" are the only working signals. The
  status logic and the finish path now use those; whether an ORPHANED worker also needs the
  `ShouldNotTravelToConstructYet` release (a worker held at a site that cannot start should
  return to mining) is a strategy decision the owner must make, and the 250-mineral worker
  gate is still worth re-examining - a base short of minerals refuses to buy the thing that
  would earn it more.

  Ruled out along the way: the finder re-offering one tile (tile is fine,
  the recorded construction is not the one the builder went to);
  `RefreshConstructionPosition` mutating the site (guarded since; the
  mismatch reproduces after the guard). Measured: TWO constructions for
  one building (IN_PROGRESS at `[94,111]` + NOT_STARTED twin 4 tiles
  away; same for Pylons) - one builds, the other waits forever holding
  its worker. `alreadyExists` misses it: same-type + within 6 tiles
  passes, but the second order takes a path that never reaches the
  check (creation side, not site side). Severity: every building, one
  miner lost per duplication. Reachable since #48 gave BuilderManager
  first priority with no claim bound. Open strategy half (owner call):
  a worker held at an unstartable site should return to mining
  (`ShouldNotTravelToConstructYet` covers only the travel half).

  **Superseded detail, kept because the gate is still worth fixing:**

  ```java
  if (!A.hasMinerals(250) && !ProtossCriticalStuffInQueue.hasEnoughMinerals())
      return dont("CriticalStuff");
  ```

  Below 250 minerals - and with nothing "critical" queued - the bot refuses to train a
  worker. So a base that is short of minerals does not buy the thing that would earn it
  more: it waits. Every other gate above it (`NoSupply`, `CantAfford` at 50, `RushPriority`,
  `FollowBO`, the Protoss `MineralsMargin` at 175) is a variation on the same shape -
  "not rich enough yet" - so the whole worker path is gated on being rich, which is the
  one state a young base is not in.

  **Why this only surfaced now:** before the placement fix, nothing could be built at all,
  so the economy never got far enough for the difference between "a worker early" and "a
  worker 250 minerals later" to matter. The library patch made building work and exposed
  the layer underneath it.

  **Not a regression from this session's work:** none of the commits here touch
  `production/dynamic/**` (checked with `git log` over those paths).

  **Why it is a livelock rather than a slow start:** the output of waitng is fewer
  minerals, so the condition that stops the worker also prevents the state that would
  release it. Nothing in the tree breaks the cycle, because each gate is individually
  reasonable.

  **Decision needed (owner's call - it is a strategy question, not a bug fix):** which
  gate should yield. Candidates: exempt a base below N workers from the mineral-margin
  gates (a worker is only worth refusing if there is something better to buy), or make
  the worker goal a V2 `DynamicGoals` contributor at `PRIORITY_WORKERS` and let the
  scheduler's arbitration decide instead of the commanders' vetoes - which is what
  Production V2 was built to do.

## #43 — the OpenBW client never attached (resolved 2026-10-08)

Four settings, in the order they were found; the last one was the actual fix and the
first three each looked like "the bot mines and does nothing":

1. `ProtossStrategies.initialize()` ran AFTER `StrategyChooser` picked, so the chosen
   strategy looked for a build-order file that did not exist yet.
2. `BWAPI_DATA_PATH` was never set for the OpenBW bot directory (the Wine one carries
   it in `bwapi-data/AI/ENV`; ours did not).
3. `AFile.loadFile` ate the last character of any value ending in the delimiter
   (`.../bwapi-data/` arrived as `.../bwapi-data`).
4. **`BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON` on the host** — the one that made the
   client attach. Without it the host serves a socket but publishes no game registry
   and the client loops on `No server proc ID`.

**Do not re-investigate the attach.** It works; the detail is in `CHALLENGES/OpenBW.md`
and `PLAN-OPENBW.md` §9.

## #44 — superseded before it was acted on

"Production v2 cutover and legacy deletion are ready to do" was the original wording.
Measured afterwards it was wrong: only `Queue/**` is a candidate, after the ordering in
`#45`. The line was removed from `NEXT.md`; `#45` carries what is true. Recorded here
only so a reader who finds `#44` quoted in an old commit knows it was withdrawn.

## #63 — workers that stood still and never mined (resolved 2026-10-12)

Symptom: newly produced workers stood by the base forever (`manager=GatherResources`,
`lastCommand=NO_COMMAND`, `lastCommandIssuedAgo=1`, `isGathering=true`,
`isIdle=true`), and the economy froze at `minerals=0` with 5 "workers".

Mechanism, measured with a purpose-built probe in two bounded runs: the stuck Probe
read `workerExists=true workerCompleted=false workerCanCommand=false`,
`canGather=false canRightClick=false`, `order=Nothing` on a walkable tile - JBWAPI
`canCommand` is false until completion, so NO order can land, while our own
`GATHER_MINERALS` action flag (set before the engine call) claimed it was gathering
and locked the `!isGatheringResources` re-try path. The throttle made it retry every
2nd frame, re-marking each time, which is why `ago` read 1 on every 30-frame sample.

Withdrawn readings: stale `isGathering` flags (a relaxation was written on that
reading and changed nothing - reverted); `TreeSet` identity (hashCode IS id, ruled
out); "worker never iterated" (the probe sits inside the iteration); the request
tile for gas (refuted separately under #62, kept there). A second population on the
same lines - accepted Gather, `order=MiningMinerals`, on the patch - is healthy
mining, and the 10-frame standstill threshold false-positives on it; that is a probe
threshold issue, not a bot defect.

Fix (our side): decline `!isCompleted()` in both mining managers, `onlyCompleted()`
in `FreeWorkers` (all 25 consumers pick units to command something with), replace
`isStuck-or-uncompleted` builders via the existing `assignOptimalBuilder` path.
Verified: mutation-proven unit/acceptance tests, unit scope 391/391, and a bounded
run with zero churning stalls whose managers read `DoNothing` on the uncompleted
(they wait instead of churning) and mining recovers (`Pylon=3`, 22 Probes from 0
minerals). Trap to keep: an order-issuing manager must never set its "I am doing
X" flag before the engine accepts the order - the flag becomes the evidence that
blocks the re-try.

## Power refusals that were not about power (resolved 2026-10-13, NEXT #50/#59/#70)

Symptom, identical every time: a construction exists, its builder walks to
the site, the engine refuses for the life of the game (`took too long`,
cancel). Three different mechanisms wore this one shape, in order:

1. **No power rule at all.** The legacy finder never checked it (the new
   planner did), so unpowered tiles were requested and correctly refused.
   Fixed by gating both paths - but on a guessed rule.
2. **The guessed rule was wrong.** "6 tiles Chebyshev from the footprint"
   allowed 6 vertically where the engine allows under 5 (Gateway [14,51]
   with a Pylon at [16,46] passed ours, died on the engine's 315 times) and
   cut 8 horizontally down to 6. Read `/sc-ai/OpenBW/openbw/bwgame.h`
   (`is_in_psionic_matrix_range` + `psi_field_mask`): |dx|<256px by
   |dy|<160px plus an 8x5 tapered mask.
3. **The mask applied to the wrong point.** The client enforces power at the
   footprint CENTER (`bwapi/Game: canBuildHere -> hasPower(lt, type)`), so an
   origin-based mask repeats the old mistake one tile over (Gateway [14,50]:
   origin inside, center 176px outside, refused). The pylon anchor is its
   tile origin. Proven by a probe run that printed our answer next to the
   engine's at decision time (`POWER_AT_REQUEST`): a [112,20]/[114,16] split
   decided it.

Withdrawn along the way: fog/visibility (explored 12/12 on refused
footprints), regions (all reachability true), occupancy (footprint empty),
poverty (bank 150+), builder movement (idle at 1.0 tiles). Every one of them
was true-and-irrelevant; only the bisection plus the engine source named the
term.

Traps to keep: a tile rule must be read from the enforcer (server mask for
placement validity, client center-rule at issue time - they can disagree at
razor boundaries, and the finder must follow the stricter observable one);
a refusal that looks identical from the score line is never one defect (this
file's whole reason to exist).

---

- **Expansion re-queued instead of finished (NEXT #58, closed 2026-10-15).**
  Symptom: one Nexus ordered, then ordered again every few seconds, almost
  nothing ever finished. Mechanism, in layers: (1) `isOverdue` fired on a
  builder still WALKING - status advances when the worker starts building,
  not when dispatched, so a 26-tile Nexus walk read exactly like a worker
  that gave up; the reaction (`Last resort - cancelling ALL not started`)
  flushed the whole plan on one slow walk. Fixed: an assigned, living
  builder means not overdue. (2) Refined twice: a builder back on minerals
  while affordable is stolen, not working (gathering exclusion); a dead or
  missing builder tracks nothing (existence fallback). A warping Nexus-2
  with a living builder still waits - the reserve must survive, else the
  expansion guard re-queues a third. (3) Double order: `maxAtATime` counted
  `nextOrders()`, which skips IN_PROGRESS, so a second Nexus queued 9 s
  after the first; `ProtossExpandNow` refuses while any unfinished base
  order exists. (4) Arbitration: Nexus queues HIGH, pumps yield unless the
  bank covers both (PriorityWaiters); supply providers never block tech.
  Proven: mutation-proven double-order test, `Last resort` 0 on the same
  1209 s run, Nexus=2 in four-plus games.

  Traps: an ACCEPT predicate must be the same one used to REJECT, or the
  gap stays invisible until a game trips it; a silent queue refusal and a
  gated-out producer read identically from the log - instrument before
  attributing.

- **CherryVis per-unit panels never attach (NEXT #65/#68, code fixed
  2026-10-12; owner-side viewer check still open).** Symptom: every click
  resolved to `i???`. Mechanism: the viewer fuzzy-matches trace units
  against replay units by (frame, type, x, y), but our
  `units_first_seen[].type` carried `AUnitType.id()` (auto-assigned from
  1000 - a Probe read `1210`) while everything else speaks BWAPI ids
  (Probe 64). Zero matched. Fixed: record `ut().id` at all three write
  sites (discovery, creation hook, seed). Second half: frame-0 seeds never
  match (replays show no visible units on frame 0; Stardust bumps 0->1
  for the same reason) - `record()` clamps to 1. Matcher mechanics, read
  from `openbw/ui/unit_matcher.cpp`, not guessed: nearest same-type
  within 25px, ambiguity guards, one-to-one, NO retry of a missed frame;
  dense mineral lines and same-spot warps are fragile by construction;
  a WASM throw mid-run freezes later units only.

  Traps: an id from the wrong numbering scheme matches nothing, loudly
  nowhere; when `???` persists on a fresh trace, the browser's
  `[BW<->CP Matching] Unable to match N units` line names the failing
  axis (type/position/frame).
