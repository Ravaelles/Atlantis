# PLAN: OpenBW as the primary game engine for every test and experiment

Status: **the client attaches and the bot plays** (2026-10-08). One blocker is left
for a real game: **placement** (`_AI/POSITION-FINDER.md`). Details of what works
and what does not are in `_AI/IDEA-E2E-TESTS.md` §2-3.

Created 2026-10-07. Owner's intent: *"make OpenBW usable nearly 1:1 the way
StarCraft under Wine is - this is your setup for all tests and experiments."*

**Read `_AI/CHALLENGES/OpenBW.md` first** - it holds the measurements this plan is
built on, and CONVENTIONS §11a points at it.

---

## 1. Why OpenBW (verified on this machine, not assumed)

```
$ ldd /sc-ai/StardustDevEnvironment/build/bin/BWAPILauncher
    libOpenBWData.so => ...   # it IS OpenBW
```

OpenBW's BWAPI exposes everything the bot reads: start locations
(`Game::getStartLocations`), minerals/geysers (`getMinerals`, `getGeysers`,
`getStaticMinerals`), neutral units, terrain (`isBuildable`, `isWalkable`,
`getGroundHeight`), and regions/chokes/areas through BWEM (linked into the
harness). So **no StarCraft, no Wine and no hand-made map dump is needed** - Wine
stays for the owner's own verification and for occasional parity checks.

The harness already hosts a real SSCAIT map
(`StardustDevEnvironment/build/test/maps/sscai/`, and the owner's
`maps/cog/(3)TauCross1.1.scx`).

## 2. Non-goals

- Replacing the fast JUnit suite (it stays the inner loop; OpenBW runs are
  seconds-to-minutes and owner-tier).
- Rebuilding the harness. `StardustDevEnvironment/` is used as it is; we add the
  Atlantis client side, not a second harness.
- Wine in the loop.

## 3. What works now

One command owns the whole lifecycle (CONVENTIONS §15):

```
bash scripts/run-openbw-e2e.sh "maps/cog/(3)TauCross1.1.scx" Protoss Zerg
```

Measured 2026-10-08:

```
Connection successful
### mapFileName = (3)TauCross1.1.scx
Analyzing map... Use build order: `Zealot into Goon`
MISSION @0:15 TO Sparta: TooFewZealots - Focus{name='MainChoke', ...}
HELLO_ATLANTIS - BWAPI attached, Atlantis is playing!
...
Killing OpenBW game processes... Exit...     (bot exit code: 0)
```

Three things had to be true, all now in the script (and in `OpenBWHost` for the
programmatic path):

1. **`BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON` on the host.** The harness's
   `Server.cpp` creates the shared-memory game registry - the table the Java
   client reads the server PID from - only when `serverEnabled`, i.e. when
   `LoadConfigStringUCase("config", "shared_memory", "ON") == "ON"`. That resolves
   from `BWAPI_CONFIG_<SECTION>__<KEY>` before any `bwapi.ini` is read, and the
   harness ships no `bwapi.ini`, so it has to come from the environment. Without
   it the host serves a socket but publishes no registry, and the client loops on
   "No server proc ID". Pinned by `OpenBWLauncherTest`.
2. **The bot ends its own game.** `FORCE_END_GAME_AFTER_REAL_SECONDS` must sit
   below the host's timeout, or the host dies first, the client is orphaned and
   the run reports failure after playing. The script sets three nested timeouts
   (game < bot kill < host kill) and refuses a config that would exceed the 360 s
   cap (CONVENTIONS §13).
3. **`BWAPI_DATA_PATH` + `AI/build_orders` resolve from the directory the jar runs
   in**, or the bot plays with no production and says nothing.

## 4. What is blocked, and on what

**Placement.** The bot cannot place its first Pylon: `Can't find place for
'Pylon' (Can't physically build here)`, every run at 0:39. A scenario that cannot
raise a Pylon exercises no production, tech or expansion.

This is being solved by a **rewrite**, not a patch: `_AI/redesign/03_PLACEMENT.md`
is the design, `_AI/POSITION-FINDER.md` the measured facts, and the old
`APositionFinder` is on the deletion list. Do not patch the legacy finder.

Secondary, both noted in `IDEA-E2E-TESTS.md` §3: there is no assertion layer on
top of the run yet (only a verdict), and the opponent is an open question (a
scripted rusher first).

## 5. How to run the host by hand (owner's path)

The blocker for a *model-run* command is that the host must outlive the client; a
model-run shell kills its background processes when the command returns. Two
terminals the owner controls:

Terminal A, started first and left running:

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

Notes that make the difference between working and mysterious:

- **Clear the transports first, in A:**
  `pkill -9 -x BWAPILauncher; rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*`.
  A leftover segment makes `Server` set `localOnly` and create **no socket at
  all**, with nothing in any log.
- **Do not use `timeout` around the host in A** - it must outlive the client.
- **Never start the client from `bots/AtlantisP/AI`:** that `ENV` says
  `GAME_LAUNCHER=WINE` and the bot starts a real game (CONVENTIONS §14).

## 6. Related records

- `_AI/CHALLENGES/OpenBW.md` - the attach investigation and its root cause.
- `_AI/IDEA-E2E-TESTS.md` - what E2E means here, and the mega-test's blockers.
- `_AI/POSITION-FINDER.md`, `_AI/redesign/03_PLACEMENT.md` - the placement rewrite.
- `DOCS/HOW-TESTS-WORK.md` - the Stardust runner's own map.
