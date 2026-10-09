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
`ENEMY_COUNT=0|1`. All of them (including both limits) are written into the bot's
ENV by the script. `TIMEOUT_SECONDS > 120` is an error, by design; a longer run
needs the owner's explicit permission for that specific command (CONVENTIONS §13).

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
