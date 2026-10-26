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

- **#47 - OpenBW E2E "fully working" (open umbrella, tracked in `NEXT.md`,
  not a resolved case).** Only the lasting trap is kept here, because it
  re-appears in every runner investigation: **`Pylons=0` is never a placement
  bug by itself.** The placement hunt was re-opened three times on that
  symptom, each killed by a probe; the causes sat one layer below:
  - the builder was assigned but never started (`BuilderManager` history, 1799
    frames, the order never issued);
  - `DefineConstructionForNewUnit` matched by NOT_STARTED status, so a building
    whose construction was already IN_PROGRESS logged `CANNOT define
    construction` and held its worker forever (#49);
  - `Game.hasPath` is region-based and OpenBW publishes no regions, so it
    answered `false` for every pair (fixed by the vendored
    `lib/jbwapi-2.2.0-atlantis.jar`).

  Two engine-level facts measured along the way: the engine never "lies"
  (`isBuildable(tx,ty,true)` refuses a tile only when a *unit* stands there,
  including our own mining worker - the builder itself is exempt, see
  `CHALLENGES/Bwapi.md`), and Pylon is 2x2 (4x3 is Nexus/Gateway) - sizes come
  from `bwapi.UnitType`, never from memory (§9). Current status and the open
  remainder live in `NEXT.md` (#94: no enemy spawns; `IDEA-E2E-TESTS.md` §3.5
  orders the E2E steps). Measure the builder and the status chain before
  re-opening the placement hunt on `Pylons=0` alone - see `NOTES.md`,
  "Tracing one construction's life from logs".

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

  - **Superseded intermediate (kept as one line):** before the library
    patch, the builder-exempt occupancy rule (`BuildingTilesAreOccupied`,
    pinned in `PlacementSearchMatchesCommandTest`) landed behind the
    stage-4 wall: the order WAS issued and the library refused it, so
    `Pylons=0` was unchanged by design, not by failure of the rule.

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
  cycle (10 283 log lines -> 64, `BUILDER @` = 0). What stays open is the
  strategy half, now tracked as `NEXT.md` #212 (a worker held at a site
  that cannot start should return to mining; the 250-mineral worker gate
  is worth re-examining).

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

  Four fixes landed, each verified by a run, not by reading: status
  advances on `isConstructing` (not the null `buildUnit()`); the "was
  destroyed" guard no longer cancels a building it never saw (5 false
  cancels before, none after); a finished construction is removed; and
  the Protoss "do not switch while moving" guard no longer blocks an
  ARRIVED builder (`lastActionLessThanAgo(20, MOVE_BUILD)` was refreshed
  every 13 frames, so `IssueBuildOrder` was never reached - a builder one
  tile from its Gateway climbed `lastActionAgo` 0..44, never `Build`).
  The last two are what made the bot build reliably - but `FINISHED`
  still stayed 0, because the construction's recorded site is not where
  its building was put (`ourOfType(Pylon) = 1` with no Pylon within 12
  tiles of the waiting `[16,46]`).

  **The finish path was the missing half - `ProtossWarping` (closed same day):**

  `ProtossWarping.updateNewBuildingJustWarped` runs from `OnOurUnitCreated`
  for a building that now exists - exactly when a construction is over -
  and did two things wrong, both measured and both fixed: it marked the
  construction `IN_PROGRESS`, never `FINISHED` (the only status that removes
  it from `ConstructionRequests`, so it lasted the game and held its worker),
  and a building warped away from its reserved tile matched nothing
  (positional lookup, 2.5 tiles). Now `FINISHED` on warp, and on a positional
  miss the oldest unfinished construction of the same type is adopted.

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

  Traps to keep: an order-issuing manager must never set its "I am doing X"
  flag before the engine accepts the order - the flag becomes the evidence
  that blocks the re-try; TWO constructions for one building (measured:
  IN_PROGRESS at `[94,111]` + NOT_STARTED twin 4 tiles away - one builds,
  the other waits forever holding its worker, because the second order takes
  a path that never reaches the `alreadyExists` check) reads as one miner
  lost per duplication and rides `NEXT.md` #144's duplicate-Pylon
  attribution; and the livelock shape itself - the output of waiting is
  fewer minerals, so the condition that stops the worker also prevents
  the state that would release it.

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
