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

## WITHDRAWN: patching the harness (option C) would change nothing

Option C above proposed patching `Shared/Templates.h` to skip the reachability check
when the engine has no regions. **It cannot work, and the reason is a correction to
this plan's own earlier claim.**

`Shared/Templates.h` is compiled into `libBWAPIClient.a` and the **C++** client
(`BWAPIClient/Source/GameImpl.cpp` and `UnitImpl.cpp` include it, as do
`BWAPI/Source/BWAPI/*`). **Our bot is the Java client.** It has its own, purely-Java
implementation of the same methods:

- `bwapi/Game.class` contains 7 `canBuildHere` references - Java logic, no native call;
- `bwapi/Client.class` picks `bwapi/ClientConnectionPosix` / `ClientConnectionW32` for its
  transport - Java and junixsocket, no C++ client library;
- nothing in the Java path loads `libBWAPIClient.a` or `libBWAPILIB.so`.

So the refusal happens entirely inside the JVM, in `Unit.issueCommand` ->
`canIssueCommand` (**offset 2**, confirmed by disassembly: `canIssueCommand(cmd)` /
`ireturn`), and the *server* never sees the command at all - `Server.cpp` has no
`canBuildHere` handling, and `GameCommands.cpp` queues `MakeBuilding` unconditionally once
a command arrives.

**Consequence:** a C++ patch would cost a harness rebuild and change not one line that
executes in our runs. That option is removed, not deferred. The four options are now
effectively three, and the library's Java validation is the wall.

## The refusal is now traced to the exact line (2026-10-10)

Every gate on the way to it was checked, by disassembly and by a probe that printed the
client's own state at the moment of refusal. The result rules out four suspects that each
looked plausible, and it is worth recording because three of them cost real time:

**What the client reports at the refusal** (live run, `BUILD_STATE` probe):

```
BUILD_STATE tile=[120,14] tileIsValid=true canCommand=true
             isBuilding=false isInterruptible=true isConstructing=false
             isCompleted=true isIdle=true isHallucination=false getOrder=PlayerGuard
```

Every one of those is what a healthy builder about to start a building looks like.

**The chain, from `Unit.build` down** (`javap` of `bwapi/Unit.class` and
`bwapi/Game.class` in `jbwapi-2.2.0.jar`):

1. `build(type, tile)` -> `issueCommand(cmd)` -> `canIssueCommand(cmd)` -> ... -> the
   7-argument form with **all six flags `true`** (each overload fills the next one with
   `iconst_1`; there is no relaxed entry point, so there is no bypass to call).
2. Gate 1, `canCommand()`: **true** - confirmed by probe.
3. Gate 2, `canIssueCommandType(type)`: passes.
4. `tableswitch` case 3 (`Build`) -> `canBuild(type, tile, flag4, false, false)`.
5. Inside `canBuild`, in order: `flag5 && !canCommand()` (**passes**),
   `flag4 && !canBuild(boolean)` (**passes** - and inside that: `isBuilding ||
   isInterruptible`, where `isInterruptible=true` was confirmed by probe),
   `flag3 && !canBuild(type, Z, Z)` (**skipped**, flag is false),
   `!tile.isValid(game)` (**passes** - `tileIsValid=true`), and finally
6. **`return Game.canBuildHere(tile, type, this, TRUE)`** - the last line, and the one
   that returns false.

**So the refusal is `canBuildHere` with `checkExplored = true`**, i.e. exactly the call
identified earlier: inside it, `if (builder)` takes the builder-reachability branch and
calls `builder->hasPath(site)`, and on OpenBW `hasPath` is region-based with
`regionCount = 0`, so it answers false for **every** pair - including a tile against
itself.

**Suspects ruled out, each with the measurement that killed it** (these are the ones to
not re-investigate):

| suspect | how it was killed |
|---|---|
| map dimensions / `TilePosition.isValid` returning zero-sized | probe: `tileIsValid=true`, map 128x128 |
| `canCommand()` (power, lock-down, order state) | probe: `canCommand=true` |
| `isInterruptible` / `isBuilding` (the `canBuild(boolean)` branch) | probe: `isBuilding=false` **but** `isInterruptible=true` |
| the exploration term of `canBuildHere` | the terrain probe: `explored=4/4` on every refused footprint, and `checkExplored` is not what `hasPath` consults |

**There is no client-side flag to relax it and no second entry point.** The four options
above are therefore the complete set - in particular, "issue the command without
validation" is not available through the public API of this library.

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

## RESOLVED 2026-10-11: option B, in source form - and the cut-over was withdrawn

**A Pylon is built.** `### Protoss buildings: Pylons=1 ###` with `engineAccepted=true`,
`getOrder=PlaceBuilding`, and the bot goes on to the next build-order item (a Forge) -
which only happens because the Pylon exists and supplied the base.

**What resolved it is option B, done properly.** Not a byte-patched jar: a local source
checkout of JBWAPI at `/sc-ai/JBWAPI`, one fix in `bwapi/Game.java`, built with Maven and
committed as `lib/jbwapi-2.2.0-atlantis.jar`. `Game.hasPath` now answers from terrain when
the host publishes no regions (`regions.length == 0`), detected from the DATA rather than
from a version or a flag - so a host that publishes regions is unaffected and the patch
becomes inert. The fallback is the same rule Atlantis' own `ReachableTiles` port uses, so
the library and the application cannot disagree. Rebuild instructions live next to the jar
name in `build-bot-jar.sh`.

**Option A (the catalogue cut-over) was started, measured, and withdrawn - deliberately.**
A `V2BuildExecutor` was written so the V2 dispatcher would issue build orders itself and
skip the legacy chain, behind a `V2_ISSUES_BUILD_ORDERS` flag. It never built a Pylon in
three live runs, and along the way it produced two instructive failures of my own: a third
occupancy predicate that refused every tile the builder was standing on, and a
per-frame guard that was never cleared, so after the first issue it returned "success"
while doing nothing. With the library patch in place, the LEGACY path also builds the
Pylon (`Pylons=1`), so the cut-over was no longer needed to unblock anything.

**Withdrawn, not deferred**, on the same principle this whole sequence has been applying:
two ways to issue a build order is two sources of truth, and keeping an unproven second
path alive because it was expensive to write is how a codebase acquires the problem this
one has been curing. The cut-over remains a desirable cleanup for its own reasons
(**#45** ordering, deleting the legacy tree) and is planned there - not as a bug fix.

**The `AUnitOrders.build` debounce is worth a look when that cleanup happens:** the live
trace shows attempts on alternating frames, which is `lastCommandIssuedAgo() <= 1`
refusing every second attempt. Harmless for correctness, but it doubles the time to issue.

## Order of work, and the first thing to do

**Owner's decision 2026-10-11: option A is the road.** With C withdrawn (it cannot work)
and B rejected (it re-creates the fork we just removed), A is the only path that reaches a
built Pylon without carrying a patched library - and it is what the architecture already
committed to.

Steps 1 and 2 are DONE:

1. ~~`NEXT.md` #48 (LIVE re-dispatch loop)~~ - **done**: the duplicate/re-dispatch half is
   fixed and the sliding start (#52) with it.
2. ~~Stack-trace the LIVE build attempt~~ - **done**: all 46 attempts go through the
   legacy chain `BuilderManager -> TravelToConstruct -> IssueBuildOrder -> Unit.build`.
   `GameOrderDirector` never appears, i.e. V2 delegates builder execution to
   `Construction`, exactly as `NEXT.md` #45 says.

**What is left is step 3, and it is the real work of A:** make the V2 dispatcher issue the
build itself instead of handing a `Construction` to the legacy pipeline, so the command no
longer passes through the library's `canBuild` at all.

The sequence, smallest safe increment first:

1. **Give `GameOrderDirector.buildAt` its own execution path** - it already receives a
   validated `PlacementReservation` (a tile the V2 planner chose), so it does not need the
   legacy `TravelToConstruct -> IssueBuildOrder` chain to re-validate it. SHIPPING NOTE:
   this is where the library refusal is bypassed, and it is deliberate - the reservation
   exists precisely because the tile was already decided.
2. **A test that pins the new path**: a build order from the V2 director reaches an order
   issue without going through `AUnitOrders.build`, asserted structurally rather than by a
   game run (the existing `BUILDER_LOG` stack already shows the chain).
3. **The cut-over gate** from `PLACEMENT-CUTOVER-PLAN.md`: a bounded LIVE run that builds
   a Pylon without special flags.
4. Then the parity gap and the deletion order (#45), unchanged.

**Risk to watch, and it is the reason this is staged:** the legacy chain does more than
validate - it also re-finds the position when the builder is blocked and it shoves units
out of the way. Step 1 must either keep those behaviours or consciously drop them; the
reservation covers the first (the tile is fixed) and `MoveUnitsFromConstructionPlace`
covers the second (it is already called from `IssueBuildOrder`).

**What must NOT happen:** no patched jar, and no second placement path. Two answers to
"where does this building go" is the disease this whole sequence has been curing.
