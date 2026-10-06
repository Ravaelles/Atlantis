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

### Map data in OpenBW: BWEM never initialises (measured 2026-10-05)

All base/choke/region data comes from BWEM (`AMap.initMapAnalysis()` calls
`new BWEM(game).initialize()` + `assignStartingLocationsToSuitableBases()`, and
`AllBaseLocations` -> `jbweb.Stations.allBases()`). Measured with `Probe12`
against a live Python 1.1 game:

    bwem.BWEM.initialize -> java.lang.IllegalStateException:
      At least one starting location was not assigned to a base.
      at bwem.BWMap.assignStartingLocationsToSuitableBases(BWMap.java:94)

`AMap.initMapAnalysis()` catches it and continues with an **uninitialised
`bwem`**, so: `Stations.allBases()` is empty -> `BaseLocations` empty ->
`natural()` null ("Natural base can not be determined"), `Chokes.chokes()`
empty (mission focus falls back to a fake choke). One cause, every symptom.

Not a code bug: OpenBW's map data does not satisfy BWEM. Same run shows the
engine *does* expose the resources - `g.getMinerals()=9`, `g.getGeysers()=1`,
`neutral=10` - so the data exists one layer below BWEM.

Consequence: "fake natural / fake choke / fake base locations" is not a hack
to avoid, it is the only working path until BWEM can see the map. The shape
the project already has for this is the **Source port** (`MapTiles.Source`,
`Regions.Source`, `PositionFinder.Source`, NEXT #18): an OpenBW adapter that
answers base/choke questions from the engine's own units (`g.getMinerals()`,
`g.getGeysers()`, `g.getStartLocations()`) instead of from BWEM. Hardcoding
Tau Cross coordinates into production would be the wrong shape - it breaks
the Map context and only ever answers for one map.

**Open question for the owner (asked 2026-10-05, unanswered):** when we move
from Python (for a working process) to Tau Cross (3 players, the real map),
is a correct defeat/loss on Tau Cross acceptable as "success", or must the
next session first make the map data path work on Tau Cross (so the loss is a
true loss, not a null-data artefact)? The answer changes the plan: with "loss
is fine" the session is: Python-process -> Tau Cross wiring -> play -> map
fix. With "true loss required" the map-data port comes first and Python is
only the smoke test.

## The frozen economy (fixed 2026-10-05)

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

- **There is a fixed ~30 s delay between server start and the first client
  frame** in this setup (measured 2026-10-05: a client that attaches at t=3 s
  saw frame 0 at t~33 s). A client that shows nothing for the first half
  minute is not hung; wait it out before re-running. Two `Unable to open
  communications socket` retries before the connection is normal.
- **`bwapi.Unit.getDistance(Unit)` crashes on mineral fields in OpenBW.**
  Probe11 died with an NPE inside `getDistance` while picking the nearest
  mineral. Use `getDistance(Position)` or compute the difference by hand.
- **`OpenBW/bwapi` is a fork of BWAPI 4.2.0** with its own IPC and no real-
  StarCraft support; the version check is `GameData.client_version`, and
  JBWAPI-Rav hard-codes `10003`. Upstream BWAPI **4.4.0** also defines
  `CLIENT_VERSION = 10003`; that is why bumping the fork's constant to 10003
  was enough and why "we are on 4.2.0, we need 4.4.0" is a number, not a
  merge. Do not start a 4.4.0 port on the version string alone.
- `bwapi.JBWEB` uses native `JNI` libraries that do not exist on Linux. On
  OpenBW, `InitJBWEB.init()` fails quietly and `AMap` falls back to no ground
  distance. This is a *second, independent* gap next to BWEM - fixing BWEM
  will not make JBWEB work, so base/choke lookups must not depend on it.
- **Exactly one BWAPILauncher at a time.** Two servers = the newer one
  overwrites the game-table row with its PID while the older keeps the
  listening socket; the client connects to the stale PID and loops on
  `Unable to open communications socket: /tmp/bwapi_socket_<dead-pid>`
  (measured 2026-10-05: 1122835 listening, 1124737 stale, client chose
  1124737). `run-openbw-server.sh` kills leftovers before hosting now; the
  tell is `ss -xlp | grep bwapi` naming a different PID than the game table.
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
## The Wine desktop (real StarCraft), measured 2026-10-06

This is the `GAME_LAUNCHER=WINE` backend, the Linux twin of the Windows F5
workflow: `java -jar bots/AtlantisP/AI/Atlantis.jar` brings StarCraft up under
Wine itself. It is separate from the headless OpenBW recipe above.

- **BWAPI needs a map's exact path, not its file name.** `--map=<name>` where
  the file lives in a subfolder made the game never start, and the bot printed
  `Game table mapping not found` forever while StarCraft sat in its map list.
  `ActiveMap.activeMapPath()` now resolves a bare name under `maps/ums` and
  `maps/sscai` (recursively, name-exact) and only then falls back to
  `maps/<name>`. Measured against the install: `M&M_v_Zealots.scx` lives at
  `ums/rav/minimaps/M&M_v_Zealots.scx` - not at `ums/rav/terran/`, where the
  owner expected it. Two files of the same name in different folders are left
  unresolved on purpose (a loud BWAPI failure beats silently playing the wrong
  map).
- **Wine cannot position its virtual-desktop window.** There is no option for
  it, so the position is set with `wmctrl` on the window titled
  `Default - Wine desktop` (name is `WineWindowConfig.DESKTOP_NAME` + the
  suffix Wine uses). The window appears a few seconds after ChaosLauncher
  starts, so the positioner polls `wmctrl -x -l` for it (up to 8 s) instead of
  setting and hoping. `WINE_WINDOW_X`/`Y` place it explicitly; empty means
  centered on the primary X screen (`xdotool getdisplaygeometry`).
- **The Wine desktop size and StarCraft's `windowed` flag are two settings,
  owned by two places.** The size is the Wine registry
  (`HKCU\Software\Wine\Explorer\Desktops`, written by
  `WineWindowConfig.enableVirtualDesktopCommands()`, read by Wine at
  `wineserver` start). `windowed = OFF` in `bwapi.ini` is what makes StarCraft
  *fill* that desktop instead of drawing its own smaller window inside it. A
  stock `bwapi.ini` ships `windowed = ON`, and a run that only patched races
  and the map produced two windows (measured 2026-10-05); on Wine the flag is
  now forced OFF from `AtlantisIgniter.updateWineWindowModeIfNeeded()`, so the
  `java -jar` path and `scripts/run-wine-game.sh` end up in the same state.
- **`taskkill` does not exist on Linux, and the exit path used to shell out to
  it anyway**, so Escape printed two `java.io.IOException: Cannot run program
  "taskkill"` stack traces before anything was actually killed. Both
  `killStarcraftProcess()` and `killChaosLauncherProcess()` now answer the Wine
  backend with `pkill -9 -x` (the `-x` matters, see above). The Windows path is
  untouched.
- `wmctrl` and `xdotool` are both installed on this machine (wmctrl 1.36,
  `xdotool getdisplaygeometry` -> `3840 2160` on the primary screen), so the
  centering path has its tools; without `wmctrl` the positioner exits quietly
  and the window manager places the window as before.
### W-MODE doublesize is the working window solution (measured 2026-10-06)

**This section corrects the claim below it, which was wrong.** The earlier
version of this note said W-MODE was the cause of an invisible StarCraft and
that it must be disabled (`WineWindowConfig.disableWModeCommands()`). The owner
then tested both states by hand and disproved it:

- W-MODE **enabled**: the game window is visible and, with doublesize, large.
- W-MODE **disabled**: the game renders at StarCraft's native 640x480 inside a
  big Wine desktop - "unusably small", the owner's words.
- `bwapi.ini [window] windowed = ON` (not W-MODE) was the other test the owner
  ran; that produced a game window whose position was far off-screen ("a few
  percent of the game visible, top left").

So the actual window solution on Wine is: **W-MODE enabled, doublesize on**
(`wmode.ini DblSizeMode=1`), **no Wine virtual desktop** (the registry
`Desktop` value deleted). W-MODE is also where the only real scaling available
on Wine lives - ALT+F9 toggles doublesize at runtime, documented in
`Plugins/WModeReadme.txt`. The Wine-side DPI knob (`LogPixels`) was also
checked and does not apply: Wine 9.0's binaries only implement non-client DPI
scaling (`NtUserEnableNonClientDpiScaling`), which an application must opt into
itself, and StarCraft 1.16.1 renders a fixed 640x480 through DirectDraw
(`SetDisplayMode`, `vidblit.cpp`) and does not opt in. Wine is an API layer,
not an image scaler - there is no way to force it to zoom a game.

Verified live in the same session: with W-MODE on and the desktop off,
`wmctrl -lGx` showed the game window `starcraft.exe` at `(2550,1056)` sized
`1290x997` - on screen, doubled. Position/size are set in `wmode.ini`
(`WindowClientX/Y`, `WindowClientXDblSized/YDblSized`); those keys are the
right lever for "StarCraft fills the screen", not the Wine desktop registry.

What remains true from the old note: the registry write pattern (`wine reg add
... /f`, query with `wine reg query`, values reach `user.reg` only when
wineserver flushes) and that the **BWAPI injector must stay enabled** -
without it there is no bot.

Two more facts from the same session:

- **`Runtime.exec(String)` word-splits, so a shell fragment cannot be passed to
  it.** The window positioner builds `eval "set -- 'Default - Wine desktop'" ...`
  and the first version handed that whole string to `exec`, which tried to
  execute `eval` as a program: `IOException: Cannot run program "eval"`. The
  method now returns `{"sh", "-c", script}` and the caller runs the vector.
  Rule: if a command contains shell builtins (`eval`, `command`) or redirections,
  it must be exec'd as a vector with the shell named.
- **The window title Wine uses is `<desktop> - Wine desktop`**, confirmed live:
  both `wine explorer /desktop=TestDesktop,800x600` and the registry desktop
  produce `wmctrl -x -l` lines titled `Default - Wine desktop` /
  `TestDesktop - Wine desktop`, class `explorer.exe.explorer.exe`. That is the
  string the positioner matches.
- **`GameSpeed.changeSpeedTo` needed the null guard itself.** `changeSpeed()`
  guards `game() != null` before delegating, but the keyboard shortcut path
  (`AKeyboard.changeSpeedAndFrameSkip` -> `changeSpeedTo`) skipped it, so
  pressing `3` before BWAPI attached threw an NPE on the JNativeHook dispatch
  thread. Guarding the method every caller reaches is what fixed it.### Java bot attaching to Wine StarCraft — the real mechanism (2026-10-06)

Three JVM variants were tested against a live Wine StarCraft with BWAPI
injected: native Linux JVM, Linux JVM run under Wine, and a Windows JRE run
under Wine (`drive_c/Java`, Temurin 8 Windows x64 — the same shape sc-docker
uses, verified in `sc-docker/docker/scripts/win_java32`). All three looped on
`Game table mapping not found` — none ever saw the server's table.

Root cause found in the vendored `JBWAPI-Rav.jar`: it ships **two connection
backends**, `ClientConnectionPosix` (shm_open, `/dev/shm`) and
`ClientConnectionW32` (Kernel32 `OpenFileMapping`), and `Client` picks between
them from `os.name`. On Linux the POSIX backend is chosen — and BWAPI under
Wine creates Windows named sections via wineserver, so `/dev/shm` stays empty
(measured during a live game). Linux JVMs therefore search the wrong namespace
forever; even a Windows JRE run under Wine is still a Linux JVM as far as
`os.name` goes and gets the same POSIX backend.

**The working override: launch the client with `-Dos.name=Windows 10`.** The
W32 backend is then selected, the game table becomes visible
(`0 | 280 | 0 | <pid>` rows print instead of the not-found loop), and one run
reached `Connected`. This is the bridge between the two namespaces: Wine
implements Kernel32, so both sides talk to the same server.

Remaining blocker at the end of the session: the client then loops on
`Unable to open communications pipe: \\.\pipe\bwapi_pipe_<serverpid>`. The
most likely cause is that the server only exposes the pipe once a **match is
running** — the earlier `Connected` happened with the game in its menu, and
the failing runs could not confirm a started match. Next step: start a game
(auto-menu), confirm the match is actually playing, then run the client; if
the pipe still refuses, check Wine's named-pipe handling for
`CreateNamedPipe`/server-side creation timing.

Practical recipe so far (client side only, game must be up first):
```
wine "C:\Java\bin\java.exe" "-Dos.name=Windows 10" -jar "Z:\sc-ai\Atlantis\bots\AtlantisP\AI\Atlantis.jar"
```
with `GAME_LAUNCHER=WINECLIENT` in `bw-data/AI/ENV` (the WineClientGameLauncher
does not start or kill anything).
