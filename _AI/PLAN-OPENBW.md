# PLAN: OpenBW as the primary game engine for every test and experiment

Status: **Step 1 diagnosed end to end, blocked on one server-side decision.**
Created 2026-10-07. Owner's intent: *"make OpenBW usable nearly 1:1 the way
StarCraft under Wine is - this is your setup for all tests and experiments."*

**Read `_AI/CHALLENGES/OpenBW.md` first** - it holds the measurements this plan
is built on, and CONVENTIONS §11a points at it.

---

## 0. Answers to the owner's two questions (asked before the plan)

### "Shouldn't OpenBW handle all of this? It is the whole game engine."

**Yes - and it does.** Verified on this machine, not assumed:

```
$ ldd /sc-ai/StardustDevEnvironment/build/bin/BWAPILauncher
    libOpenBWData.so => ...   # it IS OpenBW
$ nm -D --defined-only build/lib/libBWAPILauncher.so | grep -iE 'start|map|game'
$ nm -D --defined-only build/lib/libBWAPI.so \
    | grep -iE 'getStartLocations|getMinerals|getGeysers|getStaticMinerals|getGroundHeight'
```

OpenBW's BWAPI exposes exactly the map data the owner wants dumped:

| what we need | OpenBW/BWAPI API | present |
|---|---|---|
| base locations | `Game::getStartLocations()` | yes |
| mineral fields + amounts | `Game::getMinerals()`, `getStaticMinerals()`, `Unit::getResources()` | yes |
| geysers | `Game::getGeysers()`, `getStaticGeysers()` | yes |
| neutral units (includes resources) | `Game::getNeutralUnits()` | yes |
| terrain / buildability | `Game::getGroundHeight()`, `isBuildable`, `isWalkable` | yes |
| regions, chokes, areas | **BWEM**, linked into the harness (`libBWAPILIB.so` / `libbwem`) | yes |

So: **no StarCraft, no Wine, no one-off Wine dump is required for the map
data.** The `sscai-analyzed/` corpus the owner offered to produce by hand can
be produced by OpenBW itself, reproducibly, and committed. Wine remains an
option for *parity checks* (does OpenBW agree with the real game), not for
producing the data.

### "Can we run OpenBW on a real map?"

**Yes.** The harness already hosts a real SSCAIT map:
`run-openbw-server.sh` sets `BWAPI_CONFIG_AUTO_MENU__MAP` and launches the
engine; the maps live in `StardustDevEnvironment/build/test/maps/sscai/`
(`(3)TauCross.scx` present - the owner duplicated it so both spellings exist).

---

## 1. Where we actually stand (measured 2026-10-07)

**Working:**
- OpenBW engine present and linked: `build/bin/BWAPILauncher` + `libOpenBWData.so`.
- A host script exists and is documented: `StardustDevEnvironment/scripts/run-openbw-server.sh`.
- Atlantis already has an OpenBW launcher: `src/atlantis/config/launcher/OpenBWGameLauncher.java`
  (`GAME_LAUNCHER=OPENBW`), which does **not** start/kill Windows processes, does
  **not** hook the keyboard on a headless box, and does **not** patch `bwapi.ini`.
- Atlantis's own E2E entry script exists: `scripts/run-openbw-e2e.sh`
  (isolated bot dir `bots/AtlantisOpenBW/AI`, `GAME_LAUNCHER=OPENBW`, refuses to
  run if StarCraft/ChaosLauncher are up, 360 s bound).

**Broken (the one blocker):**
- The Java client never attaches. `out/openbw/bot.log` shows the client reading
  the server PID correctly and then failing to find its transport:
  `Unable to open communications socket: /tmp/bwapi_socket_<pid>` /
  `Unable to open shared memory mapping`.
- Cause (measured): the OpenBW server and the JBWAPI client disagree on the
  **transport**. The server offers a POSIX **socket** named after its PID; the
  JBWAPI client defaults to the **shared-memory** backend unless told otherwise.
  `run-openbw-server.sh` already documents both namespaces and clears stale
  entries of both before hosting, so the plumbing is understood - the client
  end is what is not yet configured.

**Unknown until step 1:** the exact switch JBWAPI needs (`-Dos.name=Windows`
selects the Win32 backend; the POSIX side needs its own flag/ENV), and whether
the harness must be told to use shared memory instead of a socket. This is a
research step with a measurement, not a guess.

### Step 1 RESULT (measured 2026-10-07) - the transport is solved

JBWAPI's transport choice is now known exactly, decompiled from the vendored
jar (`javap -p -c bwapi/Client.class` and `bwapi/ClientConnectionPosix.class`):

- `bwapi.Client` picks `ClientConnectionW32` when `os.name` contains "win",
  else `ClientConnectionPosix`. There is no third option and no property to
  force one - so on Linux the POSIX path is what runs, always.
- `ClientConnectionPosix` opens, in this order:
  1. `/bwapi_shared_memory_game_list` via `shm_open` -> `/dev/shm/bwapi_shared_memory_game_list`
     (the registry of running games: `0 | <serverPid> | <state> | <size>`),
  2. `/bwapi_shared_memory_<serverPid>` -> the game's shared segment,
  3. `/tmp/bwapi_socket_<serverPid>` via `AFUNIXSocket` (the sync channel).
- OpenBW publishes **all three**; `run-openbw-server.sh` already clears both
  namespaces before hosting.

**So the client's default already matches the server.** The failing line in
`bot.log` (`Unable to open communications socket: /tmp/bwapi_socket_<pid>`) was
a *consequence*, not the cause: the shared segment was missing because the
server process had already exited, and the sync socket then failed too.

**The real bug is the server's lifetime in our wrapper**, and it is ours, not
the harness's: `run-openbw-server.sh` ends with `exec BWAPILauncher`, so the
server's PID is the script's PID. My first wrapper started it as a child of the
script and killed the process group on teardown, which left `game_list`
pointing at a dead PID - exactly the stale entry the harness script warns
about. Fix: `setsid` + `nohup` (survive the wrapper) and wait for the socket to
appear instead of sleeping, then let the client attach while the host is alive.

---

## 2. Goal and non-goals

**Goal.** One command starts a headless OpenBW game on a real SSCAIT map, an
unchanged Atlantis fat jar attaches and plays it, and the same setup is the
default home for every E2E test. No Wine, no ChaosLauncher, no StarCraft, no
window.

**Non-goals.**
- Replacing the fast JUnit suite (it stays the inner loop; OpenBW runs are
  seconds-to-minutes and owner-tier).
- Rebuilding the harness. `StardustDevEnvironment/` and its scripts are used as
  they are; we add the Atlantis client side, not a second harness.
- Wine. It stays for the owner's own verification and for parity checks only.

---

## 3. Steps, in order

Each step ends with a **verification** (a command whose output proves it), per
CONVENTIONS §7 (an item is closed only when verified by execution).

### Step 1 - Make the Java client attach to the OpenBW server

The blocker. Read JBWAPI's connection code (the vendored `lib/JBWAPI-Rav.jar`
is the authoritative source per CONVENTIONS §9) and find the flag/ENV that
selects the transport matching what `BWAPILauncher` exposes.

- Probe: `java -cp lib/JBWAPI-Rav.jar ...` on the client classes, and
  `strings`/`javap` on the jar, to find the transport selection (Windows
  shared memory vs POSIX socket vs POSIX shared memory).
- Choose the one the server actually offers; prefer the direction that needs
  the *fewest* changes to the harness, because the harness is shared.
- If the server must be told to use shared memory (the client's default), that
  is a one-line config on the server side - still preferred over a client patch.

**Verification:** `out/openbw/bot.log` reaches the Atlantis "HELLO_WORLD"
equivalent - i.e. the client prints that it connected and the game reached
frame 1 - with no `Unable to open...` line.

**Deliverable:** the working invocation recorded in `DOCS/HOW-ATLANTIS-OPENBW.md`
(created in step 5) and in `scripts/run-openbw-e2e.sh` comments.

### Step 2 - Atlantis plays a full game on OpenBW, on a real map

- Map: `maps/sscai/(3)TauCross.scx` (the owner's map).
- Seed fixed, frame limit bounded, `setLocalSpeed(0)` (as fast as the CPU).
- Run the unchanged fat jar (no test-only branches; CONVENTIONS §2).

**Verification:** the bot's own log shows a played game (workers mined,
buildings ordered, frames advanced) and the process exits 0 at the frame limit.
**A crash and a hang are both failures**, and the 360 s bound (CONVENTIONS §13)
is what tells them apart.

**Deliverable:** `scripts/run-openbw-e2e.sh` runs this scenario green.

### Step 3 - Map analysis dump -> `_AI/sscai-analyzed/`

The owner's "I can run Wine once and you dump everything" idea, done the better
way: OpenBW writes the same data itself, reproducibly, for every map - and it
can be re-run when a map is added.

Content per map (one file per map, deterministic field order):
- start locations (base locations), x/y in tiles,
- mineral patches: position, amount, (and whether rich), sorted,
- geysers: position, amount,
- chokes: position and width (via BWEM),
- regions/areas: id, bounding box, chokepoints between them.
- the map's basic geometry: width/height in tiles.

Source: OpenBW/BWAPI at `onStart` (see §0 table). Written by a small Java
`tools/` main (not a test) so it can run for any map in one command, e.g.
`bash scripts/dump-map.sh "maps/sscai/(3)TauCross.scx"`.

**Verification:** the dump for TauCross contains 3 start locations (it is a
3-player map - the count is checkable by eye against the map), non-empty
minerals/geysers, and at least one choke. Cross-check a couple of coordinates
against BWEM values already used by Atlantis's `map/**` code.

**Deliverable:** `_AI/sscai-analyzed/<MapName>.md` (committed), the dump tool,
and a guard test that the tool's *parsing/serialisation* is stable (the map
data itself is engine-provided; the hand-written part is the format).

### Step 4 - The real acceptance test: no hoarding in the opening

The criterion the owner stated, now possible because the loop is real:

- "When the first `Protoss_Pylon` starts (unfinished count first becomes 1),
  at most 16 minerals remain."
- "Likewise for `Protoss_Gateway`."

Played on OpenBW, observed from the game, not from a stub.

**Verification:** the scenario logs the bank at both moments and asserts the
threshold; it fails against the pre-fix scheduler and passes against the fixed
one (a deliberate broken-bot check, which is what makes it a test rather than a
demo).

**Deliverable:** the scenario in the owner-tier suite + its recorded numbers in
`_AI/e2e/`.

### Step 5 - Make it usable: one script, one document

- `scripts/run-openbw-e2e.sh` becomes the single entry point (start server, play
  scenario, tear down, report) - already shaped this way; step 1 fills in the
  transport.
- `DOCS/HOW-ATLANTIS-OPENBW.md`: the recipe (env vars, ports/socket path, how to
  add a map, how to read the logs), replacing the dangling reference in
  `OpenBWGameLauncher`'s javadoc.
- `_AI/e2e/README.md`: where verdicts and baselines live, retention rule.

**Verification:** a fresh shell can run the documented command and get the same
result twice (the "reproduces twice" bar from IDEA-E2E-TESTS.md Stage 1).

---

## 4. Order, cost, and what blocks what

| Step | Depends on | Blocked by |
|---|---|---|
| 1 client attaches | nothing | the transport question (research) |
| 2 full game | 1 | - |
| 3 map dump | 2 (needs a live game to read the map from) | - |
| 4 no-hoarding test | 2 | - |
| 5 docs/entry point | 1-4 | - |

Step 3 could be done before step 2 in principle (the map exists at `onStart`),
but a dump written from a game that never runs is unverifiable, so it follows.

### Step 1 RESULT #2 (measured 2026-10-07): transport works, the registry does not

After fixing the jar (see the four blockers in `_AI/CHALLENGES/OpenBW.md`) the
socket connects from a bare classpath AND from the packed Atlantis jar:

```
SRV=1168435 SOCK=/tmp/bwapi_socket_1168435
socket srwxrwxr-x
--- connect test (server alive: YES) ---
CONNECTED OK
```

So `ClientConnectionPosix`'s transport is not the remaining problem. The
remaining problem is the **game registry**:

- JBWAPI's `ClientConnectionPosix` first reads
  `/dev/shm/bwapi_shared_memory_game_list` to discover the server PID, and
  fails with **"No server proc ID"** when it is empty.
- This harness's `libBWAPI.so` **does not write that file**: `strings` finds no
  `game_list` literal in it, and the file stays 0 bytes while the host runs and
  serves a working socket.
- The two are a protocol-version mismatch: the client (JBWAPI-Rav, built
  against upstream BWAPI 4.4) expects the shared-memory game registry; this
  OpenBW fork publishes only the socket.

**Options, in preference order** (none implemented yet - this is where step 1
stands):

1. **Make the client skip the registry.** JBWAPI takes the PID from
   `game_list`; if a supported property or a small client-side shim can supply
   the PID directly (the host prints it, and it is also the socket suffix), the
   client attaches without the registry. Cleanest: no change to the shared
   harness, no change to the engine.
2. **Point the client at the socket explicitly.** `AFUNIXSocket` connects
   directly (proven by the test above); if JBWAPI exposes a transport or a
   socket-path override, use it.
3. **Build the harness's BWAPI with the registry enabled** (upstream BWAPI
   writes it; this fork may have it behind a flag). Touches
   `StardustDevEnvironment/`, which is the shared platform - last resort, and
   it needs the owner's agreement because other bots link against it.

**Do not** patch JBWAPI's `bwapi/Client*.class` in the jar as a first move: it
is a vendored dependency and CONVENTIONS §9 makes it the source of truth for
the protocol, so a local fork of it would have to be maintained and documented.
Option 1 is the same result without forking a library.

**Where this leaves the plan:** steps 2-5 are blocked on one of the three
options above. Everything else - the jar, the host script, the lifecycle rules,
the challenge log - is done and verified.

### Step 1 RESULT #3 (measured 2026-10-07): the server is built for late attach

Reading the launcher's own source settles what the server does and does not do.
`3rdparty/openbw/bwapi/bwapi/BWAPILauncher/Source/Main.cpp` is explicit:

- A Java client is expected to attach **after** the game is already running
  ("JVM start alone" takes seconds), so the first frames run module-less and
  the client is adopted later, with `MatchStart` re-issued so the client calls
  `Game.init()` and `onStart()`.
- Two gates are required before the server advances frames for a client:
  `server.isConnected()` (socket handshake done) and `server.hasSynced()` (one
  full frame exchange done). Until then it prints
  `Waiting for AI client to attach...` every 15 s, and exits after 300 s.
- `BWAPI::Server::initializeSharedMemory()` **is** implemented (it creates
  `/dev/shm/bwapi_shared_memory_<pid>` and the `game_list` registry), and
  `GameImpl` holds a `Server` member - the capability is present and wired.

So the pieces all exist. What is missing is the **order**:
`initializeSharedMemory()` runs from the server's game-start path, which is only
reached once the game is actually starting, while the client needs the registry
**before** it will even attempt the socket. In our runs the launcher sits in
`startGame()` (20 s, no output at all - measured) and the registry stays empty,
so the client reads PID 0 and reports "No server proc ID".

**This is now a server-side ordering question, not a client or packaging one.**
Options 1-3 above still stand, and option 3 (a small change on the harness side,
with the owner's agreement) is the most direct: the registry must be published
before the client is expected to look, exactly as upstream BWAPI does it.

**Where the plan stands:** step 1 is diagnosed end to end and blocked on that
one decision. Steps 2-5 depend on it. The jar, the host script, the lifecycle
rules and the challenge log are done and verified.

## 5. Risks, stated up front

- **Transport mismatch is the real one.** If the POSIX client cannot be made to
  speak to the launched server, the fallback is to run the *server* in the
  shared-memory mode the client already uses - a server-side config change, not
  a patch to Atlantis or JBWAPI. Both are checked in step 1 before any code is
  written.
- **Determinism vs. ladder bots.** OpenBW games against another bot are not
  bit-reproducible across versions. Scenario assertions must be about the bot's
  own behaviour (survival frames, build timing, no crash), not about winning.
- **"Nearly 1:1 with Wine" is a claim to test, not to assume.** OpenBW is a
  reimplementation. Step 3's number cross-check and an occasional parity game
  are the only things entitled to call it "1:1".
- **Cost.** A real game is minutes. This tier is owner-run (CONVENTIONS §12),
  never the model's inner loop, and every command is bounded to 360 s (§13).

## 6. What is explicitly NOT in this plan

- Any further attempt to start StarCraft/ChaosLauncher/Wine from a model run
  (CONVENTIONS §14). The owner runs those.
- Touching `StardustDevEnvironment/`'s engine or its linked bots. It is the
  platform; we add a client and a dump tool, we do not modify the harness.
- Rebuilding the production redesign. This plan makes the engine available to
  *test* it; the redesign itself is tracked in `01_PRODUCTION.md` and is
  otherwise done (M1-M6 landed).

## 7. First move

Step 1, the transport probe. Concretely: read the vendored JBWAPI's connection
classes, find how it chooses its transport, and compare with what
`BWAPILauncher` publishes (socket in `/tmp/bwapi_socket_<pid>` vs a segment in
`/dev/shm`). Everything else in this plan is downstream of that one answer.

## 8. How to run the host by hand (the "you start the server" path)

The blocker is that the host must stay alive while the client attaches, and a
model-run terminal kills its background processes when the command returns. The
host's own accept window is 5 s, and everything then runs on the connected
socket.

**Run these two commands in two terminals you own** (not in one model-run
command). Terminal A, started first and left running:

```bash
cd /sc-ai/StardustDevEnvironment/build/test
export LD_LIBRARY_PATH=/sc-ai/StardustDevEnvironment/build/lib
export BWAPI_CONFIG_AUTO_MENU__AUTO_MENU=SINGLE_PLAYER
export BWAPI_CONFIG_AUTO_MENU__MAP="maps/cog/(3)TauCross1.1.scx"
export BWAPI_CONFIG_AUTO_MENU__RACE=Protoss
export BWAPI_CONFIG_AUTO_MENU__ENEMY_RACE=Zerg
/sc-ai/StardustDevEnvironment/build/bin/BWAPILauncher
```

Terminal B, once A prints (or after ~3 s) - never before, because the host
creates the socket only when a client knocks:

```bash
cd /sc-ai/Atlantis/bots/AtlantisOpenBW
java -jar AI/Atlantis.jar
```

Notes that make the difference between working and mysterious:

- **Clear the transports first, in A:**
  `pkill -9 -x BWAPILauncher; rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*`.
  A leftover segment makes `Server` set `localOnly` and create **no socket at
  all**, with nothing in any log.
- **Do not use `timeout` around the host in A** - it must outlive the client.
- **Never start the host with `bots/AtlantisP/AI` as the client's directory:**
  that `ENV` says `GAME_LAUNCHER=WINE` and the bot starts a real game.
- Atlantis's `ENV` must sit in the directory the jar runs from (Terminal B's
  `cwd`), and `AI/build_orders` must resolve from there too; otherwise the bot
  picks the Chaos backend (`taskkill` error on Linux) or dies with
  `BUILD ORDER is NULL` **after** a successful attach.

An alternative, when the model must own both sides: add a longer accept window
on the harness side (`Server::checkForConnections`, currently 5 s in
`StardustDevEnvironment/3rdparty/openbw/bwapi`). That is a change to our fork
and is the option recorded for the owner's decision.
