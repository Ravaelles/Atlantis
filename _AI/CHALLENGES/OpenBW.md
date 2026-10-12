# OpenBW: making the Atlantis Java client attach to the headless engine

Read this before touching `scripts/run-openbw-e2e.sh`, `build-bot-jar.sh`'s
library list, or anything that starts a game. Every failure below presented as a
different problem than it was, and each one cost a research cycle.

**Status 2026-10-09: solved and bounded.** The client attaches, the bot plays,
and every run self-terminates (CONVENTIONS §17). This file is the reference for
the traps, not a task list.

## The five packaging rules (`build-bot-jar.sh` asserts all of them)

A jar that cannot attach is a build failure, and the failure is silent - it
surfaces as "the bot never joins the game", so the assertions exist.

1. **junixsocket 1.0.x cannot run on Java 9+.** JBWAPI-Rav bundles the old fork
   (`no.fiken.oss.junixsocket`), whose `AFUNIXSocket` calls
   `java.net.Socket.setCreated()` - removed after Java 8. On Java 17 it throws
   `IllegalStateException: Cannot find method "setCreated"`, JBWAPI swallows it,
   and the client loops on "Unable to open communications socket" against a
   healthy server.
2. **A mixed junixsocket package is worse than an old one.** Replacing only the
   classes present in both versions leaves the new `AFUNIXSocket` (2.10.1,
   extends `AFSocket`) next to the old `AFUNIXSocketImpl` (1.0.2, extends
   `Socket`). The whole `org/newsclub/` tree must be dropped and re-laid.
3. **The native payload is versioned, compiler-tagged and descriptor-driven.**
   - 2.10.1 ships `lib/amd64-Linux-clang/jni/libjunixsocket-native-2.10.1.so`,
     but the loader asks for `lib/amd64-Linux/jni/...` - so the .so must also be
     aliased to the plain architecture name.
   - The `META-INF/native-image/**/resource-config.json` descriptors decide
     *which* native is chosen; dropping them makes the loader report
     "architecture not supported" with the .so present.
   - The jar must declare **`Multi-Release: true`**: the packaged junixsocket is
     a multi-release jar and the JVM ignores `META-INF/versions/**` without it.
   - The old fork's `META-INF/nar/.../nar.properties` and
     `META-INF/maven/no.fiken.oss.junixsocket/` metadata must be removed: the
     loader reads the version from them and looks for the wrong artefact.

## The decisive test that separated the layers

Connect to the socket by hand, with the same junixsocket, from a 20-line Java
program:

```java
AFUNIXSocket s = AFUNIXSocket.newInstance();
s.connect(new AFUNIXSocketAddress(new File(socketPath)));
```

- A clean classpath (`junixsocket-common` + `-native-common`) -> `CONNECTED OK`.
- The same program with `Atlantis.jar` -> `UnsatisfiedLinkError` / `setCreated`.

That is how the problem was localized to the **packed jar**, not the server and
not the client code. Without the split the investigation circles: the log says
"socket", the socket exists, and everything looks correct.

## The host registry: one setting, and the stale-state trap

- **The registry is created only when `shared_memory` resolves to ON.**
  `Server.cpp` gates it on
  `LoadConfigStringUCase("config", "shared_memory", "ON") == "ON"`, and
  `Config.cpp` resolves that from `BWAPI_CONFIG_<SECTION>__<KEY>` *before* any
  `bwapi.ini`. Without the flag the host serves a socket but publishes no game
  registry, and the client loops on "No server proc ID" while the socket it
  blames is fine. `scripts/run-openbw-e2e.sh` and `OpenBWHost` set it now.
  Pinned by `OpenBWLauncherTest`.
- *Two earlier readings of this were wrong and are removed from this file:* the
  registry slot is written with `isConnected = 0` by a live host (which is what
  the client's free-slot search wants); the `isConnected = 1` reading came from a
  **dead** host's stale entry. And `Server.cpp` does call
  `initializeSharedMemory()` from its constructor - nothing server-side needed to
  change.
- **A stale `/dev/shm` entry looks exactly like a live-host bug.** Clear
  `/dev/shm/bwapi_shared_memory_*` and `/tmp/bwapi_socket_*` **before** hosting,
  never after, and never decode a registry slot without checking
  `pgrep -x BWAPILauncher` first.
- **A stale segment makes the next launch a silent no-op.** If the segment
  already exists, `Server` sets `localOnly = true` and creates no socket at all -
  nothing in any log. This is why the clearing is mandatory, not cosmetic.

## Process-ID traps (cost the most time)

- `BWAPILauncher` **forks**: the PID `$!` returns is not always the one that owns
  the transport. Compare `pgrep -x BWAPILauncher` with the PID in
  `/dev/shm/bwapi_shared_memory_game_list` before concluding anything.
- A wrapper script that starts the host and then exits kills it with its process
  group. Use `setsid` + `nohup`, and start the client while the host is alive -
  in one command, not two terminal calls.
- **`pkill -x`, never `pkill -f`.** A `-f` pattern matches the command line of
  the shell running it, so the shell SIGKILLs itself before it can print
  anything, which reads as a mysterious hang.
- The host accepts **one** client, polls for 5 s, then unlinks the socket and
  `shm_unlink()`s the segment. The socket file is a rendezvous that disappears on
  success, so a late client sees "no socket" on a healthy host.

## What the client needs (from the vendored jar)

`bwapi.Client` picks the transport from `os.name`: Windows ->
`ClientConnectionW32`, everything else -> `ClientConnectionPosix`. There is no
third option. On Linux, `ClientConnectionPosix` opens, in order:

1. `/dev/shm/bwapi_shared_memory_game_list` - the registry, one line per game:
   `index | serverPid | state | size`;
2. `/dev/shm/bwapi_shared_memory_<serverPid>` - the game's shared segment;
3. `/tmp/bwapi_socket_<serverPid>` - the sync channel.

Slot layout: int `serverProcessID` at +0, byte `isConnected` at +4, int
`lastKeepAliveTime` at +8 (12 bytes per slot, 8 slots). The client takes the
first slot with `serverProcessID != 0 && !isConnected`. So on Linux the shared
segment is what matters first; the "socket" in an error message is usually a
*consequence* of the segment being unreachable.

## Rules this produced

- **Read the harness source before recording a blocker against it.** Two full
  sessions went into a "needs a server-side change" conclusion that one
  `grep serverEnabled` in `Server.cpp` overturned.
- **Clear stale state before hosting** (see above) - always.
- `build-bot-jar.sh` asserts the packaged junixsocket is 2.10.1: no `setCreated`,
  `AFSocket` present, no `junixsocket-native-1.*`, descriptors and the
  plain-architecture alias present.
- CONVENTIONS §14 keeps Wine/StarCraft off limits for models; §15 holds the
  lifecycle rules; §17 bounds every simulation.

## The two real bugs found on our side (fixed, tested)

These were ours, not the harness, and both looked like "the client cannot
attach":

- the launcher killed the host it was about to join;
- `ENV`/`build_orders` were written to the wrong directory, so the bot played
  with no production and said nothing.

## Map knowledge on OpenBW: measured 2026-10-10

- **BWEM works.** `MAP_ANALYSIS areas=21 chokes=42` on TauCross, no exception, and
  real coordinates reach the mission line (`Choke{[117,35], width=2}`). This
  contradicts `_AI/LOCAL-STARCRAFT.md`'s 2026-10-05 claim that BWEM never
  initialises here - that section now carries a SUPERSEDED note. Do not spend a
  session on "the map model is missing": it is not, on this map.
- **Check the model, do not assume it.** `AMap.verifyMapAnalysisIsUsable()` counts
  areas and chokes and prints `MAP_ANALYSIS areas=.. chokes=..`; on OpenBW an empty
  model exits 43 and a missing BWEM exits 42 (`AMap.handleBwemFailure`). Both are
  non-zero so the runner reports a failed run; neither is silent.
- **`src/bwem/BWMap` shadows the jar's `bwem.BWMap`, and they differ.** The local
  one has `getAreas()` and `getPath()` but **no `getChokePoints()`** - chokes are
  read per `Area` (`area.getChokePoints()`). A compile error naming a method that
  `javap` shows on the jar is almost always this shadowing, not a missing library.
- **The remaining OpenBW blocker is not map knowledge.** A bounded run ends with
  `Can't find place for \`Pylon\`, At 8 Pylon (READY_TO_PRODUCE)(#1)`,
  `(reason: Can't physically build here)`, `(Max search distance was: 36) near
  null`, then `ForceExitLocallyAfterRealSeconds`. So the placement search runs, the
  map is fine, and the refusal is inside the placement conditions - `near null`
  says `order.aroundPosition()` was never set. Start there, and note that the
  message has changed shape over time, so grep for `Can't find place` rather than
  an exact older wording.

## `canBuildHere` is implemented in the JAVA CLIENT, not the server (measured 2026-10-10)

This is the mechanism behind the Pylon refusal, traced by disassembling the jar we
actually play with (`lib/JBWAPI-Rav.jar`, `javap -p -c bwapi/Game.class`). The
result corrects two of our own comments.

- **`bwapi.Game.canBuildHere(TilePosition, UnitType, Unit)` is a Java method in
  the client.** It is not a call into OpenBW. The server exposes only
  `isWalkable` and `isBuildable` (terrain flags, synced per frame in
  `Server.cpp`); the composite "can *this* building stand *here*" question is
  answered by the client out of data it already holds. So there is no
  "the engine lies" bug to find — the refusal is computed **in our own JVM**.
- **There are two independent "is something on this tile" answers in the client,
  and they disagreed in opposite directions — which is exactly the trap #49 and
  `Bwapi.md` describe, now with the code to prove it:**
  - `canBuildHere`'s own check is `getUnitsInRectangle(rect, filter)`, which is
    `getAllUnits().stream().filter(inside rect)` over the units **the client has
    synchronised**. It excludes flying, loaded, and the builder itself (the lambda
    `lambda$canBuildHere$23` names those cases; the Nydus is the fourth exclusion).
    It is *unit*-aware and therefore strict — the tile-level `isBuildable` says
    yes, `canBuildHere` says no, and the reason is a real unit on the footprint.
  - `isBuildable(x, y, true)` — the `bi` in our probe — is
    `buildable[x][y] && !gameData.isOccupied(x,y)`, read **from the shared-memory
    occupancy grid**, not from the unit list. And `Map::copyToSharedMemory()`
    writes that grid as
    `(data->isVisible[x][y] || completeMapInfo) && tileData.bCurrentlyOccupied`
    (`BWAPI/Source/BWAPI/Map.cpp:92`). **Occupation is only published for tiles
    that are currently visible to us.** For a tile we are not watching, the grid
    says "free" whatever is standing there — which is why `bi=true` on tiles that
    a mining worker occupies, and why our probe and `canBuildHere` disagreed.
- **So the refusal is the client-side unit-rectangle test, and it is
  information-correct: a unit really is on the footprint.** It is *not* the engine
  lying, and it is not the terrain.
- **What this corrects in our own comments:**
  - `MapTiles.canBuildHere`'s "OpenBW's `Game.canBuildHere` is unreliable
    headless: it rejects tiles the map data says are fine" is **a wrong diagnosis**.
    The two answers are about different questions (units vs terrain, and visible
    tiles only), so their disagreement is not evidence of a defect.
  - Relying on `tilesCoveredAreBuildable(...)` (which uses `bi`) as the OpenBW
    fallback is therefore **unsound**: it answers a weaker question than the one
    the engine will enforce, but it can also answer "free" for an unwatched tile.
  - `_AI/NEXT.md` #47.2's "a fresh command rejection, cause unknown" is resolved:
    the client-side unit-rectangle test on a visible, worker-occupied footprint.
- **Why the IDE/manual game works and OpenBW does not** (the owner's observation):
  the *rule* is identical, so the difference is what the client can see. In a
  manual game the human's client has full visibility of the main base from frame
  0; the bot attaching a few frames into an OpenBW game has a synchronised unit
  list, and the footprint check runs against it. The next measurement that would
  settle the remaining doubt is to log `getUnitsInRectangle(footprint)` **and**
  `gameData.isOccupied` per tile at the moment of refusal — the first names the
  blocking unit, the second says whether the tile was visible.
- **Consequence for the fix.** This is the same contract `_AI/NEXT.md` #49 already
  identifies (`BuildingTilesAreOccupied.check(position, buildingType)` must know
  the builder and must count units), approached from the engine's own answer: the
  client's `canBuildHere` already implements "no unit except the builder", so the
  placement path either has to honour it by clearing the footprint ("ask the
  blocking workers to move") or the search has to treat a worker-blocked tile as a
  position to reject and move past — not as a reason to distrust the engine.

## MEASURED 2026-10-10: why no tile is ever accepted (a live run, with a probe)

This section **corrects the two readings above it**. Both were written from the
source; neither survived the measurement. The probe printed, per refused candidate,
the building type, the engine's composite answer, every footprint tile's flags, and
the units the client believes are on the footprint. Live run: TauCross, Protoss vs
Zerg, bot exit 0, ~62 s of game time, 1887 refused Pylon candidates.

**Every single refused candidate had at least one footprint tile with
`buildInc=false`.** There is not one case of "all tiles fine, still refused". So
the question is entirely about `isBuildableIncludeBuildings` — and the answer is
**terrain, overwhelmingly**. Breaking the offending tiles out by their flags:

| footprint tile flags | count | what it is |
|---|---|---|
| `walk=false, build=false` | 3854 | **not walkable** — cliff, mountain, doodad |
| `walk=true, build=false` | 951 | walkable but **unbuildable** terrain |
| `walk=true, build=true, buildInc=false` | **296** | terrain fine, **something on it** |
| `walk=true, build=true, buildInc=true` | 2447 | a normal free tile |

**Read this table with the caveat below it: the coordinates it was collected from
were printed by a buggy diagnostic (both fields got `ty`), so the counts are real but
the *positions* are not. The table describes the tiles that were refused; it does
NOT say the search area is unbuildable — run 3 shows 2422 buildable tiles in the
same square.**

- **`build=false` (terrain) accounts for 4805 of the 4949 refused tiles.** `build`
  is `Game.isBuildable(x,y)`, synced from the server. On the specific rings the
  search walks first in the first-Pylon case those are cliffs/edges — but the square
  as a whole is mostly buildable, so this is about *which tiles the search reaches
  first*, not about the region being barren.
- **Withdrawn: the earlier version of this section blamed invisibility, and the
  project's own probe data refutes it.** **1149 tiles with `expl=false, vis=false`
  answered `buildInc=true`.** Invisible tiles are *not* refused as a rule, so "the
  bot cannot see the tile, so the engine says no" is false. The offending tiles were
  mostly invisible (4949 vs 152) — correlation, not causation.
- **`checkExplored=false` is the default, so exploration is not consulted at all.**
  `Templates::canBuildHere` gates the explored check on `checkExplored && ...`, and
  the 3-argument Java form passes `false`. (`actions.h`'s engine-side
  `can_place_building` even *skips* the occupancy check on invisible tiles; the
  BWAPI client path is the one that matters here, and it does not consult visibility
  either.) So a not-yet-seen tile is not refused *for that reason*.
- **The 296 `walk=true, build=true, buildInc=false` tiles are the genuine occupancy
  case**, and the probe's own unit list on the footprint was **empty** for them — the
  blocker is not visible to `Select.all()`. The types the probe ever did see on a
  footprint are `MineralField`, `VespeneGeyser`, `Probe`, `Nexus`; a mineral field
  gives exactly `walk=true, build=true, buildInc=false`. Occupancy is therefore real
  and is a resource line rather than a worker we could shoo away.
- **`Region mismatch` is real but secondary** — 549 log lines against 3528
  `Can't physically build here` in run 1. `IsProbablyInAnotherRegion` asks
  `hasPathTo`, which is region-based and always false on OpenBW (the server publishes
  no regions), but it is reached *after* `CanBuildHere`, so it is not what stops the
  first Pylon.

### FIXED 2026-10-10: placement succeeds; the remaining blocker is builder execution

Two roots were found by probe and fixed; a live run then printed
**`SUCCESS FOR Pylon at 113,15`** and `Can't find place` fell from 2208+ to **0**
(`Region mismatch`: 549 -> 0). What each root was:

1. **`nearTo` was the Nexus itself.** `PylonPosition.nearToPositionForFirstPylon()`
   computed `geyser.translateTilesTowards(mineralsCenter, 50)`, which on TauCross gave
   `raw=[-36,88]` - **36 tiles off the map**; `ensureWithinBounds()` returned `null`,
   the `if (centerOfResources != null)` guard skipped the shift-7 step, and the origin
   collapsed onto the base (`distBase = 0.84`). Rewritten to the stated intent with the
   7-tile constant named; the direction now prefers the resource line only when it has
   room and otherwise goes away from the nearest edge (a corner base points its
   minerals at the edge its own 7-tile Pylon margin guards - measured on TauCross,
   Nexus at (117,7)-(120,9)).
2. **`IsProbablyInAnotherRegion` used the engine's region-based `hasPath`**, which on
   OpenBW is false for **every** pair. A probe showed all 549 rejections were
   `directHasPathTo=false / MapTilesHasPathBetween=true`, candidates 1-3 tiles from
   `nearTo`. It now uses `MapTiles.hasPathBetween` - the fallback that existed next to
   it all along.

**The next blocker, and it is not placement:** the construction is created and a Probe
is assigned, `BuilderManager` handles it for ~1800 frames, and then
`Cancel construction: Pylon took too long (44s) / at:[…] / buildable:true`. That is
`NEXT.md` #48's territory - and it is now **measured to the frame**, with the
`BUILDER_LOG=1` trace (`BuilderDiagnostics`):

```
@1321 unit=#119 Probe @[15,45] construction at=[16,46] distToBuild=0.0
      canAfford=true isMoving=true isConstructing=false action=BUILD
      lastCommand=Move lastCommandIssuedAgo=0
```

- **The builder reaches the site and the build command is attempted** (`action=BUILD`,
  `lastActionAgo=0` - it was issued this frame). Nothing about the search, the tile or
  affordability is wrong: `distToBuild=0.0`, `buildable=true`, `canAfford=true`.
- **`isConstructing` stays `false` and `lastCommand` stays `Move`**, so
  `orderSink().build(...)` is **returning false** - the engine refuses the command at
  the point of issue. This is the `ErRoR:b` line `_AI/STATUS.md` recorded as
  "cause unknown"; the trace above is the missing context (it was previously seen
  without `distToBuild=0.0` and `isConstructing=false` side by side).
- **Second, independent finding on the way there: `canAfford=false` for hundreds of
  frames** (`@969 … canAfford=false distToBuild=6.6`). The worker sits on
  `GATHER_MINERALS` while the Pylon is unaffordable, because continuous Probe
  production keeps minerals under 100 - the livelock `_AI/STATUS.md` describes. It is
  real, but it is **not** why the final attempt fails: by `@1321` the bot *can* afford
  the Pylon and still cannot build it.

**So there are two blockers left, and they are separate:** (a) the mineral livelock
that delays the attempt, and (b) the engine refusing `build()` on a tile that passes
`canBuildHere`. Do not merge them - the trace shows one after the other.

## MEASURED: `build()` returns false with EVERY engine precondition true

The decisive trace (`BUILDER_LOG=1`, `BUILD_ATTEMPT` line printed at the exact call
site in `IssueBuildOrder`), 92 attempts in one run, **all of them false**:

```
BUILD_ATTEMPT @1341 unit=#119 Probe building=Pylon tile=[16,46] engineAccepted=false
  || canBuildHere=true explored=4/4 canAfford=true distToBuild=0.2
     isConstructing=false isMoving=false action=BUILD lastCommand=Move
     canCommand=true isPowered=true isLockedDown=false getOrder=PlayerGuard isCompleted=true
```

Every precondition the engine's own `canIssueCommand` checks is **true**:
`canCommand=true`, `isPowered=true`, `isLockedDown=false`, `isCompleted=true`, the tile
is buildable and explored, the worker is standing on it, and the building is affordable.
**And `unit.build(...)` still answers `false`.**

- **So the refusal is not coming from the engine.** A condition cannot be both true and
the cause. The remaining candidates are all on our side of the call:
  `AUnitOrders.build`'s own guard (`lastCommandIssuedAgo() <= 1`, which returns false
  *without* calling the sink - see the two-step rhythm in the trace, attempts at @1321
  and @1341), and `BwapiOrderSink.issue`'s catch block (which would log a stack trace),
  which did **not** fire.
- **`lastCommand=Move` and `getOrder=Move/PlayerGuard` never becoming `Build`** is
  consistent with the guard: the flag is set by `lastCommandIssuedNow(Build)` *before*
  the sink call, so a refused call still leaves the previous command visible.
- **Verified side finding, no longer a suspect:** `isPowered()` is `true` for a worker
  even outside any Pylon field - `UnitUpdate.cpp:448` computes it as
  `!(Protoss && isBuilding && isUnpowered)`, i.e. it can only be false for a *building*.
  A first hypothesis that `canCommand()` failed because the Probe was unpowered was wrong.

### Narrowed further: the refusal is inside JBWAPI's `Unit.build`, and nowhere else

Both exits of `AUnitOrders.build` are now instrumented (`BUILD_CALLSITE` before the
debounce guard, `BUILD_SINK_RESULT` after the sink), one run:

```
BUILD_CALLSITE    @1321 unit=#119 building=Pylon tile=[90,112] skippedByDebounce=false lastCommandIssuedAgo=21
BUILD_SINK_RESULT @1321 unit=#119 building=Pylon engineAccepted=false
BUILD_CALLSITE    @1321 unit=#119 building=Pylon tile=[90,112] skippedByDebounce=true  lastCommandIssuedAgo=0
BUILD_CALLSITE    @1341 ... skippedByDebounce=false lastCommandIssuedAgo=20
BUILD_SINK_RESULT @1341 ... engineAccepted=false
```

- **52 calls reached the engine; 52 returned `false`.** So the sink is called, no
  exception is thrown (the `catch` in `BwapiOrderSink.issue` would have logged), and
  `bwapi.Unit.build(...)` simply answers `false`.
- **The other 52 lines are the debounce** (`lastCommandIssuedAgo() <= 1`), which makes
  the bot try every second frame. It halves the attempts; it does not cause the refusal.
- **Therefore the refusal is entirely inside JBWAPI's `Unit.build` -> `canIssueCommand`
  -> `canBuild` chain**, with every input we can observe reading correct. This is the
  boundary of what can be concluded from outside the library: the next step is either a
  patched jar that prints *which* term of `canIssueCommand` returns false, or moving to
  `PLACEMENT=catalogue` (the rewritten planner) which does not share this code path.

### SOLVED: the refusal is `builder->hasPath(...)` inside `canBuildHere` - the region bug, again

Found by following the owner's pointer to `TestAIModule/Source/BuildingPlacer.cpp`
([OpenBW/bwapi, 48124ba](https://github.com/OpenBW/bwapi/blob/48124ba8ed1b4d52b3dfd52acbaf34afb9a37fe2/bwapi/TestAIModule/Source/BuildingPlacer.cpp#L5)),
which calls **`canBuildHere(position, type)` - no builder** - and therefore takes a
different branch from ours. Confirmed afterwards on a live run: **104 of 104 build
attempts returned false**, with `canBuildHere=true explored=4/4 canAfford=true
distToBuild=0.2 action=BUILD canCommand=true isPowered=true isLockedDown=false
isCompleted=true`, and the placement search logged **zero** refusals
(`Can't find place` = 0, `SUCCESS FOR Pylon` present) - i.e. placement now works and
this single engine term is the only thing left. The chain, end to end:

1. `Unit.build(type, tile)` -> `canIssueCommand` -> `canBuild` ->
   **`Game.canBuildHere(tile, type, builder, true)`** - with our Probe as the builder.
2. Inside `canBuildHere` (`Shared/Templates.h:196-205`):

   ```cpp
   // Check if builder is capable of reaching the building site
   if ( builder ) {
       if ( !builder->getType().isBuilding() ) {
         if ( !builder->hasPath( Position(lt) + Position(type.tileSize())/2 ) )
           return false;                                  // <-- we die here
       }
   }
   ```

3. `Unit::hasPath` (`BWAPILIB/Source/Unit.cpp:165`) -> `Game::hasPath`
   (`BWAPILIB/Source/Game.cpp:731`), which is **region-based**:

   ```cpp
   Region rgnA = getRegionAt(source);
   Region rgnB = getRegionAt(destination);
   if (rgnA && rgnB && rgnA->getRegionGroupID() == rgnB->getRegionGroupID())
     return setLastError();          // true
   return setLastError(Errors::Unreachable_Location);   // false
   ```

4. The OpenBW server publishes **`regionCount = 0`** (`Server.cpp:362/368` - the real
   values are commented out, `mapTileRegionId[x][y] = 0` is written instead), so
   `getRegionAt` yields an invalid region and **`hasPath` is false for every pair**. The
   builder therefore *cannot reach the site*, and placement is refused.

**Why this explains everything the earlier probes could not:** every precondition we
could print was true (`canBuildHere` at TILE level reads buildable and explored; the
units on the rectangle are clear; `canAfford`; `distToBuild=0.9`), because the failing
term is the builder's *path to the site* - a question the tile-level query never asks
and our diagnostics never printed.

**It is the same defect already worked around twice, now in a third place:**
- `MapTiles.hasPathBetween` carries an engine-then-map fallback for it (added earlier);
- `IsProbablyInAnotherRegion` was switched to that fallback (fixed this session);
- `canBuildHere`'s builder-reachability check **still calls the raw engine** - and that
  one is inside the vendored jar's own `canBuild` chain, i.e. **not ours to fix in Java
  code we control**, except by not passing a builder or by patching the jar.

**Practical options (owner's call, none taken yet):**
1. **Do not let the command path see a builder** - the `TestAIModule` shape works on this
   engine precisely because it omits it. In practice that means calling
   `canBuildHere(tile, type)` ourselves and issuing the build through a path that does
   not re-validate with the builder - i.e. the cut-over to the catalogue planner, or a
   targeted patch.
2. **Patch the vendored `BWAPI-Rav.jar`** so `hasPath` is not consulted (or is answered
   from map data, the way `MapTiles` already does). Effective today, but it is a fork we
   then carry.
3. **Fix it in the harness** (`Shared/Templates.h` - skip the reachability check when
   regions are absent), which is the honest fix and helps every Java bot on this engine,
   but it is a change to shared platform code (`StardustDevEnvironment/3rdparty`) and
   needs the owner's agreement.

**Do not keep hypothesising about the builder loop, the guard, the debounce or the
engine's other preconditions.** Those are all measured and cleared.

#### How the reference bots avoid this entirely (measured 2026-07-11, sources read)

Neither reference project ever asks `Game.hasPath`. That is the whole answer to "why does
this work for them".

- **Stardust runs its own A\*** over tiles: `PathFinding::Search(startTile, endTile,
  validPathfindingTile).size()`, with its own `validPathfindingTile` predicate
  (`src/Builder/BuildingPlacement_Walls.cpp:295-308`, `hasPathWithBuilding`). Ground
  distance elsewhere comes from `PathFinding::GetGroundDistance(...)`, also its own.
  It never consults the engine's region graph.
- **PurpleWave uses terrain flags**: `game.isWalkable(WalkTile(...))` at the four corners
  of a building footprint (`src/Placement/Generation/CannonRushFinder.scala:89-92`).
  No regions, no `hasPath`.

So "they patched the broken regions" is **not** what they did - they never depended on
them. `isWalkable` is raw terrain and works on OpenBW; `hasPath` is the region query and
cannot. The lesson for us is the same one `MapTiles.hasPathBetween` already encodes:
**around here, reachability is answered from terrain/map data, never from
`Game.hasPath`.**

#### What the newer library changes (measured): nothing about this

`jbwapi-2.2.0.jar` was inspected for exactly this question:

- `Unit.canBuild(...)` **still calls `Unit.hasPath`** (`javap` of `bwapi/Unit.class`,
  offset 268), i.e. the same region-based refusal as the vendored jar. **Swapping the
  library does not fix placement.**
- Its bundled `org/newsclub` junixsocket is the **same 1.0.x build as `JBWAPI-Rav`'s**:
  identical class sizes, and `setCreated` appears in the same three classes in both. So
  the newer jar is not newer in the one place that matters for attaching to OpenBW.
- Class-file major is **52 (Java 8)**, so it can be dropped in without moving off Java 8.

What the library *does* change is API surface: `Pair.first`/`second` are **private** there
(public in `JBWAPI-Rav`), which is a compile error we hit (`ARegion.chokeBetween`). Using
`getFirst()`/`getSecond()` compiles against **both**, which is why the code now does that
- it makes the jar swappable from the build script alone.

## REFUTED (2026-10-10, measured): `isExplored` is NOT the reason `build()` fails

**The section below is wrong and is kept only as a record.** The owner objected on
solid grounds - `TravelToConstruct` only issues `build` once the builder has arrived
(`travelWhenReady` line 53: the `else` branch calling `IssueBuildOrder` is reached only
when `distance <= minDistanceToIssueBuildOrder`, 1.4 tiles for a Pylon), so the worker
is standing on the tile when the command is sent, and a tile you are standing on is
necessarily explored. A probe added to `BuilderDiagnostics` confirmed it:

```
@1133 unit=#118 Probe @[113,15] construction=Pylon at=[113,15] distToBuild=0.2
      canAfford=true isMoving=false isConstructing=false action=BUILD
      lastCommand=Move lastCommandIssuedAgo=0 exploredOnFootprint=4/4
... 1320 lines, exploredOnFootprint=4/4 in EVERY one
```

- **`exploredOnFootprint=4/4` on all 1320 sampled frames** - the footprint is fully
explored, so the `checkExplored` term cannot be the refusal.
- **What the same sample does show:** `action=BUILD` and `lastCommandIssuedAgo=0` (the
  command was issued this frame), while `isConstructing=false` and `lastCommand` stays
  **`Move`** - i.e. `orderSink().build(...)` returns false and the unit's last command
  is never updated to `Build`. So the refusal is real, but its cause is **not**
  exploration and **not** "no order was sent".

**Lesson, and it is the second time this exact shape occurred:** a hypothesis built by
reading an API ("the 4th argument is `checkExplored`, therefore exploration is
checked, therefore that is the cause") is not a measurement. The probe cost one run;
the reasoning cost three sessions across this file. See CONVENTIONS §3a.

## (REFUTED, kept as record) Why `build()` returns false: the COMMAND path checks `isExplored`

Measured by disassembling `lib/JBWAPI-Rav.jar` and reading the harness C++ - this is
the answer to "the builder stands on a valid tile, `canAfford=true`, `buildable=true`,
`action=BUILD` and the engine still refuses".

**The two questions are not the same question.**

| | asked by | checks |
|---|---|---|
| `Game.canBuildHere(tile, type, builder)` (3-arg, used by `MapTiles`, the placement search) | our finder | terrain + **units on the footprint** |
| `Unit.build(...)` -> `canIssueCommand` -> `canBuild` -> `Game.canBuildHere(tile, type, builder, **true**)` | the command path | the same, **plus `isExplored` on every footprint tile** |

The 4th argument exists and is **`true` on the command path**:

```
// bwapi/Unit.class, canIssueCommand case 3 (UnitCommandTypes.Build)
271: iload 4        // the checkExplored parameter
273: iconst_0
274: iconst_0
275: invokestatic  canBuild(UnitType, TilePosition, Z, Z, Z)
...
// -> canBuild ends with:
65: iconst_1
66: invokestatic  Game.canBuildHere(TilePosition, UnitType, Unit, TRUE)
```

and inside `canBuildHere`, the `true` reaches exactly one extra term
(`javap` of `bwapi/Game.class`, lines 204-209):

```
204: iload 4                 // checkExplored
206: ifeq 222                // skipped only when false
209: ... Game.isExplored(x, y)   // <- the extra condition
```

- **`isExplored` is per-player and comes from shared memory**, not from the unit list:
  `Map::copyToSharedMemory()` writes
  `data->isExplored[x][y] = !(tileData.bExploredFlags & playerFlag)`
  (`BWAPI/Source/BWAPI/Map.cpp:90`). So it is a real answer, and it is a *different*
  answer from `isBuildable`/`isVisible`.
- **Our placement code asks about exploration only in three narrow, unrelated places**
  (`CanPhysicallyBuildHere` lines 42/119/133 - the Terran fog allowance, the early
  Forge/Gateway case, and the Terran unexplored allowance). The generic path that
  accepted the Pylon tile **never required `isExplored`**, which is why the finder was
  happy and the command was not.
- **This is the missing half of the earlier `ErRoR:b` observation** in `_AI/STATUS.md`
  ("`Unit.build(Pylon, tile)` returned false even though … the origin tile is
  buildable including buildings"): `ErRoR:b` only reports `isBuildableIncludeBuildings`,
  which is a *different* predicate. A tile can be `buildInc=true` and `explored=false`,
  and the command is refused for the second one while the error string names the first,
  which is exactly how this looked like an engine contradiction for two sessions.

**Consequence - the fix belongs in the finder, and it is a filter, not a fallback:**
a candidate tile must satisfy the *same* contract the command will enforce, i.e. every
footprint tile `isExplored` (in addition to what is already checked). Anything else
means the finder and the issuer disagree by construction, which is the recurring shape
of this whole investigation. It also finally explains why the manual game works: a
human's client has the base area explored from frame 0, the bot's early search does not
necessarily.

**Not yet done (deliberately):** adding the `isExplored` filter, and verifying on a run
that the Pylon is then built. Measure first, patch second - the last four attempts to
explain this from a plausible mechanism were all wrong.

### SUPERSEDED: the root-cause analysis that led here (kept as the record)

### THE ROOT CAUSE (6 probe runs, 2026-10-10): `centerOfResources` is null, so `nearTo` IS the Nexus

This is the answer, and it is a plain null-handling bug in our own code. One probe
line, printed inside `PylonPosition.nearToPositionForFirstPylon()`:

```
PROBE_NEARTO base=[119,10] geyser=[119,5] mineralsCenter=[125,11]
             distGeyserToMinCenter=6.25  centerOfResources=null
             distBaseToCenterOfRes=-
             RESULT=[117,8]  distBaseToResult=0.84375
```

- **`centerOfResources` is `null`.** Therefore the line that was supposed to move the
  search origin off the base — `position = base.translateTilesTowards(-7, centerOfResources)`
  — **never runs**: it is inside `if (centerOfResources != null)`. The method falls
  through to `makeBuildableFarFromBounds(5)`, which returns the position unchanged
  when it is already buildable-including-buildings.
- **The result is the base itself: `distBaseToResult = 0.84` tiles.** The intended
  distance was 7 tiles. So `nearTo` for the first Pylon is the Nexus, and the search
  starts on top of it.
- **That is why every candidate is occupied** (`buildInc=0`, and `buildInc=4` never
  occurs), why the rings are dominated by `Can't physically build here` (2208) and
  the edge rules (`Too close to top map boundary` 954, `right` 95), and why the
  previously-seen `occupiedTiles=[117,9][118,9][117,10][118,10]` was the Nexus
  footprint. Every measurement now agrees.

**Why `centerOfResources` is null is not yet established** — the probe shows it null
while its two inputs (`geyser`, `mineralsCenter`) are both non-null and 6.25 tiles
apart, so the null is produced by `geyser.translateTilesTowards(mineralsCenter, 50)`
or by `ensureWithinBounds()` inside it. **That is the next measurement, not a guess:**
one probe inside `translateTilesTowards`/`ensureWithinBounds` on this map settles it.
Do not "fix" it by reordering the null checks before that is measured.

### Why this does not happen in the owner's manual game

Still **not measured**, and this gets its own line because four different stories were
invented here already. What is now known: on OpenBW the search origin collapses onto
the base because of the null above. Whether the manual game takes the same code path
and gets a non-null `centerOfResources` (i.e. the map/base geometry differs), or the
manual path is a different method entirely, is exactly what the next probe answers.

### SUPERSEDED: the search walks into a corner and every candidate is occupied

This is the final measurement and it supersedes everything below it in this section.
The probe printed, per rejected candidate, the full footprint breakdown:
`walk / terrain(build) / visible / buildInc(occupancy)` plus the exact offending tiles.

```
PROBE_TV Pylon at=[117,8] footprint=4 walk=4 terrain=4 vis=4 buildInc=0 occupiedTiles=[117,9][118,9]
PROBE_TV Pylon at=[117,7] ...                              occupiedTiles=[117,9][118,9][117,10][118,10]
```

Three facts, each measured:

1. **Terrain is never the problem.** On the candidates that reach the check, `walk`,
   `terrain` and `vis` are at **full footprint** almost always. The tiles are fine.
2. **Occupancy is the reason, and the occupier is our own Nexus.**
   `occupiedTiles=[117,9][118,9][117,10][118,10]` — that is the Nexus footprint (4x3).
   In the same run the `Too close to main base` ring spans `x 113..121, y 7..16`,
   i.e. the Nexus sits at ~**(117,12)**. The search is testing tiles **on and around
   its own base**, and `buildInc` correctly says "occupied".
3. **Not one candidate was ever fully free: `buildInc=4` occurs zero times in the
   whole run.** Every one of the rejected positions had something on its footprint.

**Where the candidates are is the smoking gun.** The scanned positions are:

```
y = 0..2   (the TOP map edge — 206 candidates)
x = 87..127 (the RIGHT map edge)
```

So with the base at `(117,12)` the search is not exploring the open ground beside
or below the base — it is marching **up and to the corner**. That is why every
candidate is occupied: the corner region is where the Nexus, its mineral line and
the map edge all sit together, and the ring order reaches those tiles first.

**Conclusion: `nearTo` / the search origin is wrong, not the engine and not the
occupancy guard.** The engine answered truthfully on every tile (terrain fine,
occupied where the Nexus is). `CanPhysicallyBuildHere`'s rejection is correct — and
the root cause of the wrong origin is the null measured above.

### What to fix

1. **The first-Pylon search must look at open ground, not at the base and the
   corner.** `PylonPosition.nearToPositionForFirstPylon()` computes
   `base.translateTilesTowards(-7, centerOfResources)` and then
   `makeBuildableFarFromBounds(5)`; on TauCross that lands the origin so the search
   walks into the corner. The probe's `build=2422` shows plenty of buildable ground
   existed in the square — the search simply reaches occupied tiles first at small
   radii and never gets a free one accepted.
2. **Do NOT change `canBuildHere`, `BuildingTilesAreOccupied`, the terrain flags or
   anything about visibility on account of this.** All four were measured and none of
   them is the cause.
3. **The margin/edge rules are secondary.** `Too close to left/top map boundary`
   (~944 + 954 lines) confirms the origin is near an edge, but the decisive fact is
   the occupancy of the reached tiles, not the margin arithmetic.

### Superseded readings below (kept only as a record of what was believed)

> Everything under this line was written before the run above. The "search area is
> the base, margin walls it in" reading is close but not the mechanism: the decisive
> fact is that **every reached candidate is occupied by the Nexus**, and the search
> drifts to the map corner. The "unbuildable terrain" and "invisibility" readings
> were artefacts of a diagnostics bug (below) and of a wrong inference.

### The real cause, measured: the search area is the base itself, and the margin rule walls it in

**Run 3 (2026-10-10, second probe, after fixing a diagnostics bug — see below).**
This supersedes every "terrain / occupancy / visibility" reading above. The numbers:

```
PROBE_SEARCH Pylon nearTo=[7,43] builder@[4,47] maxR=36
    sq=3212 build=2422 walk=2601 vis=481 buildInc=2350 nearestBuildableDxDy=0/0
```

The search square around `nearTo` has **2422 buildable tiles** and the nearest one is
**at distance 0** (the tile under `nearTo`). Terrain is abundant. Yet the first
candidates, in the order the search walks them, are:

```
(7,43): Can't physically build here      <- the nearTo tile itself
(6,42): Too close to left map boundary
(6,43): Can't physically build here
(6,44): Can't physically build here
(7,42): Too close to main base (2)
(7,44): Can't physically build here
(5,41..45): Too close to left map boundary   (five rings in a row)
```

- **`nearTo` is the base coordinate, and it is 7 tiles from the left map edge.**
- **`ProtossTooCloseToMapBoundaries` reserves a 7-tile margin for a Pylon while
  `supplyUsed <= 30`** (`marginPx = 7 * PX_PER_TILE`). So the search is centred 7
  tiles from the edge and its first several rings are eaten by the boundary rule,
  then by `Too close to main base` (the Nexus), then by the mineral/occupancy tiles.
  The buildable open ground the probe counted is mostly *further out*, behind those
  rings — the search does reach it eventually at larger radii, but by then other
  conditions (region, occupancy) also apply, and the run logged no success.
- **The status histogram of the run is the shape of that:** `Can't physically build
  here` (dominant), `Too close to left map boundary` (944), `Too close to main base`,
  `Not powered by a Pylon`, `Region mismatch`. No single one of them is "the" cause;
  they are the rings of a search that starts in a corner of its own base.

### Two of our own diagnostics lied, and that is why this took three runs

- **`PositionFulfillsAllConditions` printed wrong coordinates.** It assigned
  `_currentTX = position.ty()` and then `_currentTY = position.ty()` — **both fields
  got `ty`** — so every logged `(x,y)` pair read `x == y`. A whole run's worth of
  "all candidates lie on the diagonal" was this bug, not the search. **Fixed**
  (`position.tx()` in the first line); the fields are used only by the failure print,
  so the fix changes no behaviour, only the log.
- **The first probe's terrain histogram measured the wrong thing.** It counted flags
  of the tiles the search *rejected* and I read those as "the search area is
  unbuildable terrain". With the coordinates fixed, the honest statement is the one
  above: the area is buildable, the **order** of the search hits walls first.

### Why the owner's manual game builds a Pylon and this does not

**Still not measured, and the earlier answer ("the human sees the whole base, the
bot has a small vision disc") was invented — do not repeat it.** What is now
measured is where the difference is *likely* to live: the candidate **order** and
where `nearTo` points, not the engine's answers. A manual player clicks a spot a few
tiles out from the Nexus in open ground; the search starts *at* the base coordinate
and walks outward through its own margin and base rules. Comparing the first N
candidates of both would settle it.

### What to do about it

1. **`nearTo` for the first Pylon is too close to the map edge and to the base for
   the margin rule that applies at that supply.** `PylonPosition.nearToPositionForFirstPylon()`
   derives it as "base translated -7 tiles towards the resources"; on TauCross that
   lands 7 tiles from the left edge, which is exactly the Pylon margin. Either the
   first-Pylon `nearTo` must be searched with a bigger budget/toward open ground, or
   the margin must be a constraint on the *candidate*, not a wall across the rings.
2. **Do not "fix" this by distrusting `canBuildHere` or by relaxing terrain.** The
   engine answered correctly on every tile the probe could check; the search simply
   asks in a place where the answers are all "no" for legitimate reasons.
3. **Never reason about fog/vision here.** Both the fog story and the
   "unbuildable terrain" story were wrong, and both were written down before being
   measured. This section is the third attempt and the only one with numbers.

**Probe hygiene:** the probe was temporary, printed only refused candidates, and was
**removed** after the run (verified: `git diff` shows no production file changed).
The run did not commit anything, so the tree is clean apart from the `_AI/` edits.

### The fourth "field OpenBW fills differently": `buildUnit` and the warp finish (2026-10-11)

- **`Unit.getBuildUnit()` is null on OpenBW** even while the worker is on the site with
  `order=Build` and `isConstructing=true`. Cause traced to the harness:
  `UnitUpdate.cpp:333` converts through `getUnitFromBWUnit(...)`, OpenBW's `current_build_unit`
  does not resolve in the BWAPI id table, `getUnitID` returns -1, the client reads null.
  Same shape as `regionCount`, `isOccupied` and `hasPath` - a shared-memory field that
  StarCraft fills and OpenBW does not fill the way BWAPI expects.
- **Consequence that cost the session:** `Construction` status advanced only on
  `buildUnit() != null`, so it sat on `NOT_STARTED` forever, `BuilderManager` held the worker,
  the worker never mined, and the base deadlocked (measured: `Pylons=5 gateways=0` at
  ingame=1209 s, `BuilderManager` 2631 frames against `GatherResources` 3).
- **The fix belongs on our side of the seam, and there is a signal that works:**
  `isConstructing` (published correctly, `UnitUpdate.cpp:359`) plus "a building of this type
  now stands within 2.5 tiles of the reserved tile" (read from the live unit list). Do not
  fork the library for this one.
- **Second, independent defect found in the same path:** `ProtossWarping` used
  `DefineConstructionForNewUnit.defineConstruction`, which matches the construction
  **positionally** (2.5 tiles), and marked the match `IN_PROGRESS` - never `FINISHED`.
  `setStatus(FINISHED)` is the only status that removes a construction, so an `IN_PROGRESS`
  one lasted the rest of the game: `countPendingOfType` counted a standing building,
  `BuilderManager.isBuilder` kept refusing the worker. A building that appears outside the
  radius (site moved by `RefreshConstructionPosition`, or a snapped footprint) matched
  nothing at all and leaked the same way. Fixed: `FINISHED` on warp, and a fallback that
  adopts the oldest waiting construction of the same type when the positional lookup misses.
- **Measured effect of the fix** (bounded OpenBW run, `BUILDER_LOG=1`, TauCross, Protoss vs
  Protoss, `Total time: 125 seconds`, exit 0): `### Protoss: Nexus=1 Probe=13 Pylon=2
  Gateway=1 ###` - two Pylons and a Gateway build. `BUILDER @` diagnostic lines 10 283 -> 654,
  stuck `Gateway NOT_STARTED` frames 3534 -> 86, `CANNOT define construction` -> 0, and build
  attempts reaching the engine 0 accepted -> 3/3 `engineAccepted=true` with
  `getOrder=PlaceBuilding`.
- **Parsing trap, cost me a wrong reading mid-session:** the run summary line is
  `### Protoss: Nexus=1 Probe=13 Pylon=2 Gateway=1 ###`, **not** `Pylons=N Gateways=N`. A grep
  for `Pylons=` / `SUCCESS FOR` returns zero on a run that built two Pylons, which reads as
  "nothing was built" if you trust the grep. Grep the exact summary format, or `tail` the log.
- **`BUILDER_LOG` throttles `BUILD_ATTEMPT`** (it is `printMaxOncePerMinute`-ish through the
  diagnostic path), so its count is a lower bound, not the number of build attempts. Use the
  `BUILDER @` status census for frequency questions, not the attempt lines.

### The fifth "field OpenBW fills differently": `isCompleted` at game start (2026-10-12)

- **Starting workers read `isCompleted=false`** (hence `canCommand=false`,
  JBWAPI `Unit.java:3221`) into the early game, so every gather/rightClick/move is
  refused and `lastCommand` stays `NO_COMMAND` from frame 30. Same shape as the four
  above - a shared-memory field StarCraft fills and OpenBW does not fill the way
  BWAPI expects, at least not at attach time.
- **Our-side fix, no harness change:** never assign or order an uncompleted unit -
  `GatherResources`/`IdleWorker` decline it, `FreeWorkers` offers `onlyCompleted()`,
  `BuilderManager` replaces it. The residual early-minerals window is waited out
  with `DoNothing`, not churned through (a churning worker re-marks Gather every
  2nd frame and poisons our own gathering flag, locking the re-try path).
