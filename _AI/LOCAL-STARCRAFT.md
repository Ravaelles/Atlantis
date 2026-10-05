# LOCAL-STARCRAFT.md — OpenBW on Linux, steps since `7aa5a413`

Status: **uncommitted**, all work is in the working tree of
`/sc-ai/StardustDevEnvironment` (6 files, +312/-12) plus one local-only file
in Atlantis (`bwapi-data/AI/ENV`, git-ignored). Nothing is committed yet.

Goal of this stretch: get a **working local OpenBW game** so that the economy
defects (B-22 Protoss production, B-19 defense) can finally be measured on
real game data instead of on `scbw`, which cannot host a Computer AI and
therefore never grows an economy to inspect.

Everything below was verified by execution. The throwaway probes live in
`/tmp/opencode/openbw-probe/` (`Probe*.java`, `probe*.log`, `server*.log`) and
are not part of any repo.

## Steps

1. **Reproduced and localised the block.** `run-openbw-server.sh` printed
   `Waiting for AI client to attach...` forever; the Java client selected the
   game-table row, mapped the segment, then died silently. Probing
   (`strace` on the server, `javap` on `JBWAPI-Rav.jar`) showed the whole
   chain works up to `connectSharedLock()`.

2. **Version handshake** — `Error: Client and Server are not compatible!
   Client version: 10003 / Server version: 10002`. `CLIENT_VERSION` in
   `bwapi/include/BWAPI.h` bumped to `10003` (the value JBWAPI demands; the
   shared `GameData`/`GameTable` layouts were already byte-identical).

3. **Connection checks ran only on frame 1.** `checkForConnections()` was
   gated on `!startedClient`, but the JVM needs seconds to attach, so the one
   window had long passed. Now it runs every frame while nobody is attached
   (and only then — an attached C++ module sets `externalModuleConnected` on
   frame 1, so the in-process test suite keeps its exact pace).

4. **`Game::countdownTimer()` throws** `countdownTimer?` in this engine —
   `updateSharedMemory()` called it. Now reports `0`, which is what the
   reference does deliberately.

5. **Late attach had no `MatchStart`.** `Game.init()` (player/unit lists) and
   `onStart()` are dispatched *only* from that event, and the client only
   looks at events once `isInGame` is true — the not-in-game loop dispatches
   nothing. So `MatchStart` must be serialized in the very same frame as the
   `isInGame` flip, one frame earlier is unread garbage. Added
   `Server::everSynced` + `hasSynced()`, gated `isInGame` on it, and made the
   launcher wait for `isConnected() && hasSynced()` before adopting.

6. **Unit discovery was spent on nobody.** `computePrimaryUnitSets()` emits
   `UnitCreate`/`UnitDiscover`/`UnitShow` exactly once per unit — on frame 1,
   before any client exists. The adopted client then rebuilt `allUnits` from an
   empty `visibleUnits` set every frame, i.e. an empty map. Added
   `GameImpl::rearmUnitDiscovery()` (clears the four `was*` flags) and called
   it at adoption. Probe went `myUnits=0` → `myUnits=5` (Nexus + 4 Probes).

7. **`PlayerData::isParticipating` was never written** by `Server.cpp`. The
   client's `Player.isObserver()` is `!isParticipating`, and `isEnemy()`/
   `isAlly()` return false whenever either side is an observer — so every
   player looked like an observer, `game.enemies()` stayed empty and Atlantis
   died with `NoSuchElementException` in `AGame.enemyName()`. Written now.
   Invisible to the in-process C++ tests: they call `PlayerImpl` methods,
   never read `PlayerData`.

8. **Auto-menu env var name.** `BWAPI_CONFIG_AUTO_MENU__ENEMY_RACE_0` is
   ignored — index 0 reads key `enemy_race`, only `enemy_race_1..7` are
   indexed. Correct spelling: `BWAPI_CONFIG_AUTO_MENU__ENEMY_RACE=ZERG`.

9. **Verified end to end.** Probe: `onStart map=Python 1.1 self=Protoss`,
   `players=[0:Zerg/true, 1:Protoss, 2:Unused]`, `enemies=1`, 1200 frames, no
   exceptions. Atlantis (script-built jar, Corretto 8): attaches, analyses the
   map, prints `### Atlantis is working! ###` and a mission line, 100 s and
   45 s runs with zero exceptions.

## The economy: solved 2026-10-05 (was the open item)

**SOLVED 2026-10-05 (verified by execution, see below).** The frozen economy
was the probe, not the engine: `Probe6` used `UnitCommand.gather(...)`, which
this JBWAPI refuses at the client before it ever reaches the engine. The
working order is `UnitCommand.rightClick(worker, mineral)`: it is accepted,
the worker enters `MoveToMinerals`, and `gatheredMinerals` climbs
50 -> 226 over 1200 frames (Probe11).

### The recipe was broken (fixed in `StardustDevEnvironment`)

`run-openbw-server.sh` and this document both omitted
`BWAPI_CONFIG_AUTO_MENU__AUTO_MENU=SINGLE_PLAYER`. Without it
`AutoMenuManager::startGame()` returns without creating a game (default
`OFF`), so the server sits idle, the Java client connects and then **hangs at
`Connected` forever** - exactly the silent failure this file used to describe.
The script now sets the key and clears stale `/dev/shm/bwapi_shared_memory_*`
and `/tmp/bwapi_socket_*` before hosting (a stale row makes the client map a
dead PID). Verified: with the patched script, Probe11 runs and mines.

### Why `gather` fails and `rightClick` works (measured)

`issueCommand` -> `canIssueCommand` -> for a Gather command ->
`canGather(worker, mineral)`. Measured terms at frame 30 (Probe9/Probe10):

| term | value |
|---|---|
| `canCommand()` (exists, owner == self) | **true** |
| `canIssueCommandType(Gather)` | **true** |
| `canRightClick(mineral)` | **true** |
| `canGather(mineral)` | **false** <- the failing term |
| `isResourceContainer()` of the mineral | true (not the culprit) |

The remaining gate in `canGather` is `hasPath(self.getPosition())` (bytecode
offset 76-88). So a Gather command is refused by a client-side pathfinding
check, while `rightClick` - what a player actually does to send a worker to a
patch - passes and produces `MoveToMinerals`.

### This matters for Atlantis itself

Atlantis' whole mining path is the refused call:
`AUnitOrders.gather(...)` -> `BwapiOrderSink.gather(...)` ->
`actor.u().gather(target.u())` (`src/atlantis/units/BwapiOrderSink.java:91`).

**FIXED 2026-10-05 (verified): `BwapiOrderSink.gather` now falls back to
`rightClick` when the engine refuses `gather` and the target is a mineral field
or gas building.** In a real game `gather` succeeds, so the fallback never
runs - no behaviour change on Windows/Chaos. In OpenBW it is the difference
between a frozen economy and a playing bot.

Verified end to end: `bash scripts/build-bot-jar.sh bots/AtlantisP/AI/Atlantis.jar`
(6.1 MB, 3617 entries), then the jar attached to the patched server and ran:
`Connection successful` -> `### Atlantis is working! ###` -> mission
`Zealot into Goon`, buildings queued and **minerals climbing 50 -> 784 -> 1064**
(they sat at 50 before the fix). Zero `OrderSink`/`gather` errors. Suite 293/0,
ArchUnit 7/7, endurance store unchanged.

Run log kept at `/tmp/opencode/openbw-probe/atlantis-run.log`.

## Operational notes (cost me time, keep them)

- **The server needs `BWAPI_CONFIG_AUTO_MENU__AUTO_MENU=SINGLE_PLAYER`.**
  Without it `startGame()` does nothing (default `OFF`), the server sits idle
  and the client hangs at `Connected` with no game. Cost: the whole 2026-10-05
  morning was spent on "why does Probe5 no longer attach" when only this key
  was missing. `run-openbw-server.sh` sets it now; any hand-launched server
  must too.
- **Env config keys are `BWAPI_CONFIG_<SECTION>__<ITEM>`, UPPERCASE**
  (`Config.cpp`: `"BWAPI_CONFIG_" + upper(key) + "__" + upper(item)`). So
  `auto_menu` / `enemy_race` are one thing and the section key `auto_menu`
  repeated is the mode: `BWAPI_CONFIG_AUTO_MENU__AUTO_MENU`.
- The server prints nothing until the first `startGame()` returns, so an
  **empty server log means it is stuck inside the auto-menu**, not that it is
  buffering. `stdbuf -oL` does not help there.
- `pkill -f BWAPILauncher` **kills the calling shell too** (the pattern matches
  its own cmdline) and silently truncates the rest of the command chain. Use
  `pkill -9 -x BWAPILauncher`, and never chain it into a longer `&&` line.
- Always `rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*` between
  runs. A stale row makes the client pick a dead PID and print
  `Unable to open shared memory mapping` forever. `run-openbw-server.sh` does
  it before hosting now.
- The server holds ~120s of terminal lines hostage if started without
  `setsid ... < /dev/null &`: the wrapper times the command out and SIGTERMs
  the server. Detach with `setsid` and redirect all three fds; see
  `/tmp/opencode/openbw-probe/start-server.sh`.
- Java **must** be `/home/rav/.jdks/corretto-1.8.0_492/bin/java`; the junixsocket
  bundled in `JBWAPI-Rav.jar` breaks on the PATH Java 17
  (`Cannot find method "setCreated" in java.net.Socket`).
- Order API in this JBWAPI: `unit.issueCommand(UnitCommand...)`. There is no
  `bwapi.UnitOrder` and no `UnitCommand.mine()`. For mining use
  `UnitCommand.rightClick(worker, mineral)` - `gather(...)` is refused (above).
- `listen(syncSocket, 8)` instead of the reference's `0`: with frame pacing a
  backlog of 0 makes the client's `connect()` fail with EAGAIN.
- Reading live shared memory without a client file: copy the snapshot through
  `/proc/<pid>/fd/<fd>` — the segment name is `shm_unlink`ed while the server
  runs, so `/dev/shm/bwapi_shared_memory_<pid>` does not exist.
- Probe inventory in `/tmp/opencode/openbw-probe/` (all compiled against
  `lib/JBWAPI-Rav.jar` with Corretto 8): `Probe5` attach/players, `Probe6`
  command path via `gather` (frozen - the wrong call), `Probe7`/`Probe9`
  `canIssueCommand` terms, `Probe8` player identity, `Probe10` gather vs
  rightClick, `Probe11` **the one that mines** (`rightClick`, economy climbs).
  Compile: `/home/rav/.jdks/corretto-1.8.0_492/bin/javac -cp
  /sc-ai/Atlantis/lib/JBWAPI-Rav.jar -d . ProbeN.java`.