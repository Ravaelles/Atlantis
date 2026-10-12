# OpenBW: how to run it, and what it is for

Status 2026-10-09: **the client attaches and the bot plays**, and every run is
bounded by two hard limits (CONVENTIONS §17). One command owns the whole
lifecycle:

```bash
TIMEOUT_SECONDS=120 INGAME_TIME=$((60*20)) \
  timeout 120 bash scripts/run-openbw-e2e.sh "maps/cog/(3)TauCross1.1.scx" Protoss Zerg
```

Limits, set once at the top of the script and refused when widened:

| knob | default | meaning |
|---|---|---|
| `TIMEOUT_SECONDS` | `120` | wall-clock cap for the whole simulation |
| `INGAME_TIME` | `60*20` (20 game minutes) | the bot ends the game itself at this in-game time |

Other optional env: `PLACEMENT=catalogue`, `PRODUCTION_V2=LIVE`,
`ENEMY_COUNT=0|1`, `BUILDER_LOG=1`. All of them (including both limits) are written
into the bot's ENV by the script. `TIMEOUT_SECONDS > 120` is an error, by design; a
longer run needs the owner's explicit permission for that specific command
(CONVENTIONS §13).

### `--save-replay` - record the game to `/sc-ai/REPLAYS` (added 2026-10-10)

```bash
TIMEOUT_SECONDS=120 INGAME_TIME=120 \
  timeout 120 bash scripts/run-openbw-e2e.sh --save-replay "maps/cog/(3)TauCross1.1.scx" Protoss Zerg
ls -lat /sc-ai/REPLAYS/ | head
```

**WORKING (2026-10-10).** A bounded run now writes a real replay, verified:
`/sc-ai/REPLAYS/atlantis_20261010_223617_Protoss_vs_Zerg.rep`, 34 KB, Brood War header
(`a77e 7e2b` + `reRS`). Two things were needed, and they are both in the runner now:

1. **The path reaches the host** via `BWAPI_CONFIG_AUTO_MENU__SAVE_REPLAY`
   (`AutoMenuManager.cpp:141`), written as a plain ASCII name - the engine runs the
   configured path through `strftime` and strips characters outside `[A-Za-z0-9-_. /]`
   (`GameEvents.cpp:375-390`), so a map name like `(3)TauCross1.1` mangles the output.
2. **The bot must let the engine end the match** (`END_GAME_VIA_ENGINE=true`, set only
   for a `--save-replay` run). The engine writes the `.rep` in `onGameEnd()`, which the
   launcher reaches **after** its game loop exits (`BWAPILauncher/Source/Main.cpp:76`);
   a bot that `System.exit`s first (`FORCE_END_GAME_AFTER_REAL_SECONDS` -> `LOCAL=true`
   -> `exitGame`) takes the JVM down before that, and no file appears. With the flag,
   `OnGameEnd` calls `leaveGame()` instead.

`cleanup()` also stops the host gently (then `-9`s it) when recording, so the flush is
not lost - the `-9` fallback stays, because a leftover host poisons the next run
(`_AI/CHALLENGES/GameExecution.md` #4).

Notes: the run exits `124` (the outer `timeout`) because ending via the engine means the
bot no longer returns promptly - the game ends and the replay is written, but the
process lingers until the cap. **Inspect the `.rep`, not only the exit code, for a
recording run.** `END_GAME_VIA_ENGINE` stays off for ordinary runs: the self-exit is the
shape CONVENTIONS §17 relies on.

### `BUILDER_LOG=1` - per-frame builder trace (added 2026-10-10)
```bash
BUILDER_LOG=1 TIMEOUT_SECONDS=120 INGAME_TIME=120 \
  timeout 120 bash scripts/run-openbw-e2e.sh "maps/cog/(3)TauCross1.1.scx" Protoss Zerg
```

Prints one `BUILDER @<frame> …` line per frame for every worker that owns a
construction (`BuilderDiagnostics`): its active manager, the last command it issued,
the command's target, and the construction's tile, status, `buildable` and
`distToBuild`, plus `canAfford` / `isMoving` / `isConstructing` / `action` /
`lastActionAgo`. Off by default - it is a diagnostic switch, and it costs frame time.

It exists because a construction that silently never starts leaves **no** trace in a
normal log (nothing throws): the builder simply keeps being handled by a manager that
never issues the order, which looks identical to one that does. The two facts that
separate the cases - *which manager is running the unit* and *what the last command
was and where it pointed* - are what this prints. Read it as: `action=BUILD` with
`isConstructing=false` means the command was issued and the engine refused it;
`action=GATHER_MINERALS` with `canAfford=false` means the bot is simply waiting for
minerals.

### `--cvis` - CherryVis trace for the run (added 2026-10-12)

```bash
timeout 120 bash scripts/run-openbw-e2e.sh --short --cvis "maps/cog/(3)TauCross1.1.scx" Protoss Zerg
```

Sets `GENERATE_CHERRYVIS_REPLAY=true` in the bot's ENV. The bot writes
`bwapi-data/write/cherryvis_dir.rep.cvis/` (`trace.json` plain, `trace.json.zst`
compressed copy, `trace_raw.json`, `game_summary.json`); the script copies it
into the run dir and, with `--save-replay`, next to the replay as
`/sc-ai/REPLAYS/<name>.rep.cvis/` - the layout the viewer opens. A clean run
with no trace fails (a killed/crashed bot cannot have one - `onGameEnd` never
ran - so that case reports its own reason first). `--self-test-cvis` pins the
artifact checks with no game. Verified 2026-10-12: clean 5 s run leaves a valid
trace (22 first-seen frames, 107 units seeded at frame 0, 21 units with manager
updates). Per-unit free-text lines additionally need
`DebugFlags.CHERRYVIS_BUILDER_ACTIONS=true` (recompile + rebuild the jar).

### Viewing a trace: CherryVis in Docker (added 2026-10-12)

Native install works (`scripts/install_cherryvis.sh`, plus `--run-only` for
server-only starts), but the reproducible path is bmnielsen's image, fixed up
locally in `/sc-ai/CherryVis/cherryvis-docker`: `python:3.10-alpine` kept (3.14
breaks the old pins), `zstandard==0.21.0` (0.11.0 cannot compile on 3.10+ -
`Py_TYPE` assignment, measured), the missing `zstd` added (the HTTP 500 we fixed
natively), and `cvis-views-zstd-compat.diff` (the shipped `zstd` dropped the
streaming API `views.py` was written against; the endpoint now serves both
compressed partitions and plain files).

```bash
docker build -t cvis /sc-ai/CherryVis/cherryvis-docker
docker run -d -p 8770:8770 -v /sc-ai/REPLAYS:/my-replays --name cvis cvis
```

Then `http://127.0.0.1:8770/` shows the samples; our games need the glob
(`/list/?pattern=/my-replays/*.rep`) and pair via `<name>.rep` +
`<name>.rep.cvis/` (`has_cvis_data: true`). Pairing is by the file the engine
really wrote - it strips colons from the configured name, and the runner now
pairs with the newest on-disk `.rep` for exactly that reason. Verified: our
trace serves as valid JSON through `/get/cvis/` both plain and via `.zst`.

To list host replays by default instead of the samples (the UI also has a
search box that takes the same glob and persists it in the URL):

```bash
docker run -d -p 8770:8770 -v /sc-ai/REPLAYS:/my-replays --name cvis \
  cvis ./server.py --host 0.0.0.0:8770 --replays_pattern '/my-replays/*.rep'
```

### Opening the trace locally (CherryVis viewer install)

Upstream repo is archived (2022), but the viewer still builds. From
`TorchCraft/TorchCraftAI/scripts/cherryvis/README.md`:

```bash
git clone https://github.com/TorchCraft/TorchCraftAI.git && cd TorchCraftAI/scripts/cherryvis
virtualenv -p python3 venv_django && source venv_django/bin/activate
pip install django zstandard
# emscripten from source (kripken docs), then:
./build_js.sh
./server.py   # http://127.0.0.1:8770/
```

Caveats measured against our output: the viewer was built for Stardust's
partitioned layout (trace.json with file references); ours is the legacy
single-file layout (everything inline, from `cherryvisdumper.cpp`'s
`writeGameSummary` shape), which is what the blog's "a zstd compressed JSON
trace file can be provided" paragraph describes - point the server at a
directory containing our `trace.json` + `game_summary.json` (e.g. a run's
`cvis/` copy or `/sc-ai/REPLAYS/<name>.rep.cvis/`). `server.py` only accepts
local connections by default (it is a window into the filesystem).

### Scenario assertions (optional)

The script can also check *what happened*, by reading the facts `GameSummary`
already prints into `bot.log`. With none of these set the script only reports the
verdict, exactly as before:

| env | fails when |
|---|---|
| `EXPECT_MIN_INGAME_SECONDS` | the game ended earlier than this in-game time |
| `EXPECT_MIN_KILLED` | we killed fewer units |
| `EXPECT_MAX_KILLED` | we killed more units (e.g. the rush should be survivable, not farmed) |
| `EXPECT_MIN_PYLONS` | fewer Protoss Pylons exist at game end (completed or unfinished) |
| `EXPECT_MIN_GATEWAYS` | fewer Protoss Gateways exist at game end (completed or unfinished) |
| `EXPECT_MIN_RESOURCE_BALANCE` | `Resource killed/lost` is below this (use `-200` for "roughly even") |

Example - survive the opening for 7 game minutes with a near-even trade:

```bash
TIMEOUT_SECONDS=120 INGAME_TIME=$((60*7)) \
  EXPECT_MIN_INGAME_SECONDS=420 EXPECT_MIN_KILLED=12 EXPECT_MAX_KILLED=40 \
  EXPECT_MIN_PYLONS=1 EXPECT_MIN_GATEWAYS=1 EXPECT_NO_PLACEMENT_FAILURES=1 \
  EXPECT_MIN_RESOURCE_BALANCE=-200 \
  timeout 120 bash scripts/run-openbw-e2e.sh "maps/cog/(3)TauCross1.1.scx" Protoss Zerg
```

The script prints `verdict: ingame=..s killed=.. resourceBalance=..` and exits
non-zero (with `SCENARIO FAILED`) when an expectation is not met.

**Measured 2026-10-09:** the run above finishes in ~5 s of wall clock for 428 s
of game time (OpenBW is that fast), and the assertions work - the first run
correctly reported `killed=0`, Pylons=0, Gateways=0 and resourceBalance=-300 -
the bot made no opening structures and killed nothing because it never got a Pylon
(`Can't find place for Pylon` at 0:39, CONVENTIONS §17 does
not hide that). `GameSummary` prints `Defeat` even on a deliberate limit stop,
so read the verdict line, never `Defeat`.

For the *why* and the investigation that got here, read
`_AI/CHALLENGES/OpenBW.md` first (CONVENTIONS §11a points at it). This file is the
recipe and the caveats.

## What works (measured 2026-10-08)

```
Connection successful
### mapFileName = (3)TauCross1.1.scx
Analyzing map... Use build order: `Zealot into Goon`
HELLO_ATLANTIS - BWAPI attached, Atlantis is playing!
...
Killing OpenBW game processes... Exit...     (bot exit code: 0)
```

Three settings make that true, all already in the script and in `OpenBWHost`:

1. **`BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON` on the host.** The harness's
   `Server.cpp` creates the shared-memory game registry (the table the Java client
   reads the server PID from) only when `serverEnabled`, i.e.
   `LoadConfigStringUCase("config", "shared_memory", "ON") == "ON"`. That resolves
   from `BWAPI_CONFIG_<SECTION>__<KEY>` *before* any `bwapi.ini`, and the harness
   ships no `bwapi.ini` - so it must come from the environment. Without it the host
   serves a socket but publishes no registry and the client loops on "No server
   proc ID". Pinned by `OpenBWLauncherTest`.
2. **The bot ends its own game, on either limit.**
   `FORCE_END_GAME_AFTER_REAL_SECONDS` (wall clock) and
   `FORCE_END_GAME_AFTER_INGAME_SECONDS` (game clock, 20 minutes) both call
   `ForceExitLocallyAfterRealSeconds`, which ends the game while the host is still
   alive. If the host dies first the client is orphaned and the run reports failure
   after playing (measured 2026-10-08). `Env` re-caps the wall-clock value at 120,
   so a hand-edited ENV cannot widen it.
3. **`BWAPI_DATA_PATH` + `AI/build_orders` resolve from the directory the jar runs
   in**, or the bot plays with no production and says nothing.

## Why OpenBW, and what it cannot do

OpenBW is the headless engine the harness links (`ldd BWAPILauncher` shows
`libOpenBWData.so`). No StarCraft, no Wine, no window. Wine stays for the owner's
own verification and occasional parity checks.

**It cannot host a custom opponent.** Measured, in
`_AI/CHALLENGES/StationaryEnemy.md`:

- the harness accepts **one** BWAPI client (`Server.cpp` `checkForConnections`),
  so a second Java bot cannot attach;
- the enemy is spawned by the engine's auto-menu **by race only** - no custom
  enemy module on this path;
- `ENEMY_COUNT=0` (no opponent at all) **crashes JBWAPI** at `Game.init`.

So a scripted or passive enemy has to be a **UMS map** with its own trigger, not a
bot. `auto_menu.enemy_race` (the default) gives a normal growing AI opponent.

## Hand-run recipe (owner's path)

A model-run command cannot own both sides (its background processes die when the
command returns), so two terminals. **The owner runs this path, not a model:**
with two terminals the assistant cannot enforce the 120-second cap, and CONVENTIONS
§14 keeps Wine/StarCraft off limits for models anyway. The bounded, one-command
model path is `scripts/run-openbw-e2e.sh` above.

Terminal A, first and left running:

```bash
cd /sc-ai/StardustDevEnvironment/build/test
export LD_LIBRARY_PATH=/sc-ai/StardustDevEnvironment/build/lib
export BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON
export BWAPI_CONFIG_AUTO_MENU__AUTO_MENU=SINGLE_PLAYER
export BWAPI_CONFIG_AUTO_MENU__MAP="maps/cog/(3)TauCross1.1.scx"
export BWAPI_CONFIG_AUTO_MENU__RACE=Protoss
export BWAPI_CONFIG_AUTO_MENU__ENEMY_RACE=Zerg
/sc-ai/StardustDevEnvironment/build/bin/BWAPILauncher
```

Terminal B, once A is up:

```bash
cd /sc-ai/Atlantis/bots/AtlantisOpenBW
java -jar AI/Atlantis.jar --map="maps/cog/(3)TauCross1.1.scx"
```

Notes that separate working from mysterious:

- **Clear the transports first, in A:**
  `pkill -9 -x BWAPILauncher; rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*`.
  A leftover segment makes `Server` set `localOnly` and create **no socket at
  all**, with nothing in any log.
- **Do not `timeout` the host in A** - it must outlive the client.
- **Never start the client from `bots/AtlantisP/AI`:** that ENV says
  `GAME_LAUNCHER=WINE` and the bot starts a real game (CONVENTIONS §14).
