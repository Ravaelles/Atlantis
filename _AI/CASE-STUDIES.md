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
     2. **A Pylon is placed in a real run.** NO, and the cause is measured
     2026-10-09 - but **not** what the first reading suggested. The engine is
     telling the truth: on the probed tiles `engBuild=true` and only the occupancy
     variant `engBuildInc=false` for the one tile (7,44) that is genuinely
     occupied. So `MapTiles.isBuildable` needs no fallback, and the earlier
     "engine lies" note in this item was wrong.

     Verified from the engine jar: a Pylon is **2x2** (Gateway is 4x3).
     `CanPhysicallyBuildHere` correctly requires each footprint tile to be
     buildable-including-buildings; the sampled candidate included occupied tile
     (7,44). The owner confirms a manual SC run builds Pylon and Gateway; this is
     owner evidence for the legacy path, not an OpenBW verdict. The next bounded
     OpenBW run must decide whether the legacy-path failure persists; do not
     patch `APositionFinder` without a fresh reproducer.

     **Fresh LIVE OpenBW evidence (2026-10-09):** the selected catalogue tile's
     `Unit.build(Pylon, tile)` returned false; the 2x2 footprint's four tiles were
     individually buildable in both the map grid and `Game.isBuildable(..., true)`,
     no other unit overlapped the footprint, and minerals were sufficient. Yet
     `Game.canBuildHere(tile, Pylon, builder)` was false. This is a fresh command
     rejection, but its cause is still unknown; do not add a map fallback or alter
     `APositionFinder` based on the map-only values.

     **Cause now MEASURED (2026-10-10, three live runs + two temporary probes; probes
     removed, fast suite 372/372).** The full numbers are in `_AI/CHALLENGES/OpenBW.md`
     ("The real cause, measured: the search area is the base itself"). In short:

     - The engine is not at fault. A probe of the search square found **2422
       buildable tiles** around `nearTo` with the nearest at distance 0.
     - **The first-Pylon `nearTo` is the base coordinate, 7 tiles from the left map
       edge** (`nearTo=[7,43]` on TauCross), and a Pylon at `supplyUsed <= 30`
       reserves exactly a **7-tile** boundary margin. So the search starts in a
       corner of its own base and its first rings are consumed by
       `Too close to left map boundary` (944 log lines), `Too close to main base`,
       and the base/mineral occupancy tiles. The open ground exists but sits behind
       those rings. **This is the blocker: a `nearTo`/margin-geometry problem, not
       terrain, not fog, not regions.**
     - Two earlier readings in my own notes were wrong and are withdrawn:
       invisibility (`checkExplored=false` by default; the probe found 1149
       *invisible* tiles answering `buildInc=true`) and "the search area is
       unbuildable terrain" (an artefact of a diagnostics bug - see below).
     - **Our own failure print lied about coordinates**: `PositionFulfillsAllConditions`
       set `_currentTX = position.ty()` as well as `_currentTY`, so every logged
       `(x,y)` read `x == y`. Fixed; the fields are print-only, so no behaviour changed.
       Any analysis based on that print before 2026-10-10 is invalid.

     **PLACEMENT NOW SUCCEEDS - two roots fixed, one new blocker exposed (2026-10-10).**
     Measured, live run: the log now prints **`SUCCESS FOR Pylon at 113,15`** and the
     placement failures went to **zero** (`Can't find place` was 2208+; `Region mismatch`
     549 -> 0). Nothing was patched blind - both fixes came from probes.

     **Root 1 - the search origin was the Nexus (fixed).**
     `PylonPosition.nearToPositionForFirstPylon()` computed
     `geyser.translateTilesTowards(mineralsCenter, 50)`, which on TauCross landed
     **36 tiles outside the map** (`raw=[-36,88]`); `ensureWithinBounds()` then returned
     **null**, the `if (centerOfResources != null)` guard skipped the step meant to move
     the origin off the base, and `nearTo` became the Nexus itself
     (`distBase = 0.84`). The method was rewritten to the stated intent: the base moved
     `FIRST_PYLON_TILES_FROM_BASE` (7) tiles **towards the resource line when that has
     room**, otherwise **away from the nearest edge** (a corner base points its mineral
     line at the edge its own 7-tile Pylon margin guards).

     **Root 2 - `Region mismatch` fired on every candidate (fixed).**
     `IsProbablyInAnotherRegion` used bare `position.hasPathTo(...)` = `Game.hasPath`,
     which on OpenBW is region-based and answers **false for every pair** (the server
     publishes no regions). A probe showed **all 549 rejections** were
     `directHasPathTo=false / MapTilesHasPathBetween=true` with the candidate 1-3 tiles
     from `nearTo`. It now uses `MapTiles.hasPathBetween`, which already carried the
     engine-then-map fallback `MapTiles` needed for exactly this reason and which this
     condition never used.

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

    **Why this is not a one-line fix:** the two existing answers are each wrong in a
    different direction, so neither can be adjusted into the correct one.
    - `isBuildableIncludeBuildings` counts **all** units, so it also refuses a tile
      occupied only by our own builder - a placement the game allows.
    - `BuildingTilesAreOccupied.check` counts **buildings only**, so it accepts a
      tile with a mining worker on it - a placement the game refuses.
    `_AI/POSITION-FINDER.md` point 2 explains why the guard was narrowed to
    buildings (a worker or a patch two tiles over "overlapped" a Pylon's footprint),
    and that narrowing traded a false negative for a false positive instead of
    encoding the real rule. The contract to change is `BuildingTilesAreOccupied.check
    (position, buildingType)` -> it needs to know the builder (excludable) and to
    count units, not only buildings; 6 call sites.

    **Second half, and it is why a strict rule alone is not enough:** a mineral line
    is saturated with workers and a Pylon is 2x2, so "no unit may be here" refuses
    the whole area. The answer is to **ask the blocking workers to move** -
    `MoveUnitsFromConstructionPlace` already tries (`moveAwayFrom(buildPosition, 2)`
    at `TravelToConstruct:51`), but it (a) only runs while the builder is still
    travelling (`distanceToConstruction >= 3.8` returns early, i.e. not when the
    builder is already at the site) and (b) shoves every nearby unit with no notion of
    "except the builder". Extending that to run at the site and to exclude the builder
    is the other half of this item.

    **Test to write with the fix:** a footprint with the assigned builder on it is
    placeable; the same footprint with a *mining* worker on it is not, until the
    worker is sent away. The existing test to reconcile is
    `OccupiedTileIsNeverBuildableTest.aUnitStandingOnTheTileOccupiesIt`: its NAME says
    a standing unit occupies the tile, but its BODY asserts the opposite for a worker
    (``"a worker on the tile is not an existing building - counting it refused every
    mineral-line position"``), reasoning that a worker "is handled by the engine's
    own answer and by the builder stepping aside". This measurement shows that
    reasoning is not true today: the engine refuses, and nothing steps aside. Name and
    body have to be reconciled with the rule above.


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

  **It is not a Gateway bug and not a build-order bug.** The build order does list them
  (`8 - Pylon`, `9 - Gateway`, `10 - Gateway`), and in a shorter run they ARE built -
  measured: `### Protoss buildings: Pylons=2 Gateways=2 ###` with
  `construction=Gateway isConstructing=true lastCommand=Build action=BUILD`. The long run
  differs in one respect:

  ```
  issued: OK Probe  = 0        <- the bot never trains another worker
  canAfford=false 2587 vs true 195
  construction=Gateway status=NOT_STARTED  canAfford=false  action=GATHER_MINERALS
  ```

  **CORRECTED 2026-10-11 (measurement replaced the first reading). The worker gate is a
  symptom, not the cause.** A probe on `AutoProduceWorkersCommander.dont(...)` printed the
  refusing gate and the state every second:

  ```
  WORKER_GATE @24 reason=CantAfford workers=5 minerals=0 reserved=0 supply=5/9
  ... identical for 180+ frames
  ```

  `minerals=0` with 5 workers means the workers are not MINING, and the manager census says
  why:

  ```
  manager=BuilderManager  2631     <- the workers are 'building'
  manager=GatherResources    3     <- and almost never mining
  ```

  **So the real defect is a cyclic deadlock, not a mineral threshold:** constructions wait
  for
  minerals (`canAfford=false`), `BuilderManager` holds every worker at those sites, the
  workers therefore do not mine, no minerals arrive, and the constructions keep waiting.
  `CantAfford` on the worker goal is the next link in the chain, not its start - a first
  reading of this item blamed the `hasMinerals(250)` gate below, and that gate is real but
  secondary: with workers not mining, no threshold would have been reached either.

  **CORRECTED AGAIN 2026-10-11, and this time it is the real cause - `buildUnit()` is
  null on OpenBW, so a construction that HAS started is never marked started.**

  The probe that settled it (`BUILDER_LOG=1`):

  ```
  unit=#123 Probe @[13,58] construction=Pylon status=NOT_STARTED at=[13,58]
  distToBuild=0.3 buildable=true canAfford=true action=BUILD
  isConstructing=TRUE lastCommand=Build
  ```

  Everything is fine - the Probe is ON the site, the tile is buildable, the minerals are
  there, the order was issued, **and the engine reports the unit as constructing**. Yet our
  `Construction` still says `NOT_STARTED`, so `BuilderManager` keeps the worker assigned to
  it, and the worker does not return to mining. That is the deadlock: not a mineral
  threshold and not a worker gate, but a status that never advances.

  **Why it never advances, traced through the layers:**

  - `ConstructionStatusChanger` advances `NOT_STARTED -> IN_PROGRESS` from
    `builder.buildUnit() != null` (the Zerg metamorphosis branch at line 143 is a
    different, correctly `We.zerg()`-gated path);
  - `Unit.getBuildUnit()` reads `unitData.getBuildUnit()` - a shared-memory field;
  - the harness fills it at `UnitUpdate.cpp:333`:
    `self->buildUnit = o.currentBuildUnit() ? server.getUnitID(getUnitFromBWUnit(o.currentBuildUnit())) : -1`;
  - OpenBW **does** set `u->current_build_unit` (`bwgame.h:8203`), so the field exists -
    what fails is the conversion: `getUnitFromBWUnit(...)` yields a unit the BWAPI id table
    does not know, `getUnitID` returns -1, and the client reads `null`.

  **This is the fourth instance of one pattern in this engine**: a shared-memory field that
  StarCraft fills and OpenBW does not fill the way BWAPI expects.
  `regionCount = 0` broke pathing, `isOccupied` broke the occupancy grid, `hasPath` broke
  placement, and now `buildUnit` breaks construction status. The first three each cost a
  session; recognising the shape is the point of writing it down.

  **The good news, and the reason to fix it in Atlantis rather than in the library:**
  `isConstructing` IS published correctly (`UnitUpdate.cpp:359` computes it in the client)
  and reads `true` at the moment above. Atlantis does not need `buildUnit` to know that a
  construction started - it can use the field that works, or the plain fact that the
  builder is standing on the reserved tile with the building absent. That is a change on
  our side of the seam and does not require another fork.

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

  ### The last thing, and it is NOT in the status logic

  Fix 3's probe printed the whole picture:

  ```
  FINISH_WHY Pylon site=[16,46] status=NOT_STARTED ourOfTypeWithUnfinished=0 within12=
  FINISH_WHY_ALL ourBuildings=3 ourOfType(Pylon)=1 all=27 ourUnits=14
  ```

  **`ourOfType(Pylon) = 1` and `ourOfTypeWithUnfinished=0` in the same breath** - a Pylon
  exists, and the construction waiting at `[16,46]` has no Pylon within 12 tiles of its
  site. So the construction's recorded site is not where its building was put, the finder
  keeps re-offering the same tile, and the worker stays claimed by a site that will never
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

  **Superseded, kept as the record of the hunt (the duplicate-construction theory):**

  That is the same disagreement the owner's log shows from the other side:

  ```
  FOUND PLACE FOR Gateway at 118,37
  FOUND PLACE FOR Pylon at 101,30
  FOUND PLACE FOR Gateway at 118,37     <- same tile again
  FOUND PLACE FOR Pylon at 101,30
  ```

  The finder keeps returning the same tile, so the tile is not the problem - but the
  construction that ends up recorded is not the one the builder went to.

  **`RefreshConstructionPosition.refreshPosition` was ruled OUT as the cause** by a probe:
  it does move the site (`REFRESH_MOVE Gateway from=[16,48] to=[17,41]`, seven tiles of
  drift), but that is now guarded - it refuses to move a site the builder is already moving
  to or standing on - and the `ourOfTypeWithUnfinished=0` measurement above comes from AFTER
  that guard.

  **MEASURED AND IDENTIFIED: there are TWO constructions for one building.**

  `Construction.setPositionToBuild` was instrumented to print every site assignment with its
  caller chain. It printed **nothing** - no code ever moves a construction's site after
  creation, so "the site was mutated" is ruled out. The status census then showed what is
  actually there:

  ```
  construction=Gateway status=IN_PROGRESS at=[94,111]  1460   <- being built
  construction=Gateway status=NOT_STARTED at=[98,111]  1410   <- a SECOND one, 4 tiles away
  construction=Pylon   status=IN_PROGRESS at=[79,114]   498
  construction=Pylon   status=IN_PROGRESS at=[90,112]   535
  construction=Pylon   status=NOT_STARTED at=[79,114]   122   <- duplicates here too
  construction=Pylon   status=NOT_STARTED at=[90,112]   372
  ```

  Two constructions of the same type, alive at the same time, four tiles apart. One is
  being built; the other waits forever for a building that is already going up elsewhere,
  holding its worker while it waits. **That is the deadlock, and it is the same thing the
  owner saw in the IDE** - `FOUND PLACE FOR Gateway at 118,37` alternating with
  `Pylon at 101,30`: two entries for one intent, each re-finding its own tile.

  **Why `alreadyExists` does not catch it:** `ConstructionRequests.alreadyExists` requires
  `sameAs(...)` AND `positionToBuildCenter().distTo(...) <= 6` - the two Gateways are 4
  tiles apart, so that part passes - but it is consulted on the path that creates a new
  construction (`NewConstructionRequest.requestConstructionOf`), and the second one is
  evidently created through a path that does not reach that check, or reached it before the
  first was registered. **The next measurement is the creation side**, not the site side:
  instrument `ConstructionRequests.constructions.add` with the caller chain and the
  building type, and the two creation paths will be named.

  **Note the severity:** this affects every building, not just the opening. A duplicated
  construction is a permanently employed worker and one permanently unsatisfiable task, so
  the base loses a miner for the rest of the game each time it happens.

  **The builder-ownership rule from `NEXT.md` #48 made this reachable.** That change gave
  `BuilderManager` first priority in `WorkerManager` and made it hold an assigned builder
  even when it cannot issue an order that frame - correct for "do not steal the builder",
  but it did not bound how many workers can be claimed at once, so a construction whose
  status never advances claims a worker forever.

  **What it should do instead (owner's call, it is a strategy question):** a worker held at
  a site that cannot start should return to mining until the site is affordable. The
  existing `ShouldNotTravelToConstructYet` is exactly that idea for the *travel* half
  (it refuses to set off when the building is not yet affordable) - the gap is the worker
  that has already arrived, or that is claimed before the check runs.

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
