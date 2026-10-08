# OpenBW: how to run it, and what it is for

Status 2026-10-08: **the client attaches and the bot plays.** One command owns the
whole lifecycle:

```bash
bash scripts/run-openbw-e2e.sh "maps/cog/(3)TauCross1.1.scx" Protoss Zerg
```

Optional env: `GAME_SECONDS` (default 240), `PLACEMENT=catalogue`,
`PRODUCTION_V2=LIVE`, `ENEMY_COUNT=0|1`. All but `GAME_SECONDS` are written into
the bot's ENV by the script.

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
2. **The bot ends its own game.** `FORCE_END_GAME_AFTER_REAL_SECONDS` must be
   below the host's timeout, or the host dies first, the client is orphaned and
   the run reports failure after playing. The script sets three nested timeouts
   (game < bot kill < host kill) and refuses a config over the 360 s cap
   (CONVENTIONS §13).
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
command returns), so two terminals:

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
