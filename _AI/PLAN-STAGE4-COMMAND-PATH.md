# Stage 4: get the build command past the library's broken reachability check

Status 2026-10-10: **plan, not started.** Stage 3 of `_AI/PLAN-REGION-DETECTION.md` is
done; this is the step it deliberately stops short of, and the only one that can make a
Pylon appear.

## Where we are (measured this session)

Stages 1-3 gave us a correct, self-detecting answer to "can a ground unit get from A to
B", and the live run shows the whole effect:

```
ReachableTiles: engine publishes no regions - answered from terrain
Region mismatch:  549 -> 0
Can't find place:    0   (was 2208+)
SUCCESS FOR Pylon:   present
engineAccepted=false: 112 of 112 build attempts
```

So the search now finds a valid tile and the builder walks to it - and the engine still
refuses the order. That refusal is inside the library, not in our code:

`Unit.build(...)` -> `canIssueCommand` -> `canBuild` ->
`Game.canBuildHere(tile, type, builder, true)` -> `builder->hasPath(site)` ->
region-based `Game.hasPath` -> **false**, because OpenBW publishes no regions.

`jbwapi-2.2.0.jar` behaves identically (checked with `javap`: its `canBuild` still calls
`Unit.hasPath`), so a library swap is not a fix.

## The four honest options

| # | Option | Makes `Unit.build` work? | Cost | Verdict |
|---|---|---|---|---|
| A | Finish the catalogue cut-over: stop issuing placement through the legacy path | **Yes** - the new planner does not route the command through that validation | the cut-over work itself (`PLACEMENT-CUTOVER-PLAN.md`) | the plan of record |
| B | Patch `jbwapi-2.2.0.jar` so `canBuild` does not consult `hasPath` when regions are absent | Yes | we carry a fork of the library again - the thing we just removed | rejected unless A stalls |
| C | Patch the harness (`Shared/Templates.h`) to skip the reachability check when `regionCount == 0` | Yes, for every Java bot on this engine | shared platform code; owner's decision | worth proposing separately |
| D | Do not pass the builder, so the check never runs | **Unverified, and probably no** - the command path still validates, and this is what the owner already judged "worse, not better" | unknown | do not start here |

**A is the plan.** B and C are recorded because A is not small, and a future session
deserves to know what the alternatives cost rather than rediscovering them.

## MEASURED (2026-10-10): the order is issued by the LEGACY path, not by V2

First task done - a stack trace at `BwapiOrderSink.build` on a live run. All **46** build
attempts have **one identical chain**:

```
BUILD_ISSUER engineAccepted=false building=Protoss_Pylon tile=[16,46]
  atlantis.units.BwapiOrderSink.build
  < atlantis.units.AUnitOrders.build                    <-- the debounce guard
  < atlantis.production.constructions.builders.IssueBuildOrder.considerIssuingNow
  < atlantis.production.constructions.builders.TravelToConstruct.travelWhenReady
  < atlantis.production.constructions.builders.BuilderManager.handle
  < atlantis.architecture.Manager.invokeFrom ...
  < atlantis.units.workers.WorkerHandlerCommander.handle
  < atlantis.architecture.Commander.invokedCommander
```

**`GameOrderDirector` does not appear at all.** So in this configuration the V2
`OrderDirector` is not what calls `Unit.build` - the legacy `BuilderManager ->
TravelToConstruct -> IssueBuildOrder` chain is, and V2 (if it is engaged at all here)
hands execution to `Construction`, which is exactly what `NEXT.md` #45 says it does
("V2 delegates builder execution to it").

**What that means for the edit, and why it is good news:** the single place to change is
`IssueBuildOrder.considerIssuingNow` / `TravelToConstruct` - one method chain, reached
from one `BuilderManager` - and not a scattered set of directors. It is also the chain
`PLACEMENT-CUTOVER-PLAN.md` already scheduled for deletion, so the change is on the
path of the cut-over rather than beside it.

## What A actually requires (the concrete steps)

1. **DONE - the issuing path is identified** (above): the legacy builder chain, one
   call site. The remaining question is not *where* but *what to replace it with*, and
   that is the cut-over, not a patch.
2. **Prove V2+LIVE issues a build at all.** Still gated on `NEXT.md` #48 (the re-dispatch
   loop) - a LIVE run currently never reaches a stable "issue once" state. #48 is
   therefore the real first task of stage 4, not a side quest.
3. **Then the gate from `PLACEMENT-CUTOVER-PLAN.md` step 1**: a Pylon actually built in a
   bounded run without special flags.
4. Only then make the pair the default and work through the deletion order.

## What stage 4 must not do

- **Do not patch the jar** (option B) while A is still plausible: the whole point of
  committing `jbwapi-2.2.0` was to stop carrying a fork nobody else can rebuild.
- **Do not add a second placement path.** Two answers to "where does this building go" is
  the disease; stages 1-3 were spent curing a milder form of it.
- **Do not treat `engineAccepted=false` as a placement bug.** It is not: placement is
  fixed and measured. The remaining refusal is the command path.

## Order of work, and the first thing to do

1. `NEXT.md` #48 (LIVE re-dispatch loop) - without it the V2 path never issues once.
2. Stack-trace the LIVE build attempt to find which director issues the order (step 1
   above). This is cheap and decides the shape of the actual edit.
3. Make that director the only issuer, then run the cut-over gate.

**The first concrete action is a measurement, not an edit:** a LIVE run with a stack trace
at the build call site. Everything else in this plan depends on which path that names.
