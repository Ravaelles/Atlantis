# BWAPI — key challenges

## Two bot types; the message differs
- Module bots (in-process C++) need `gameInit`/`newAimodule` exports from the
  dll in `[ai]`. Client bots (Java, shared memory) do not — the
  `is now live using "<Nothing>"` message is noise for them, not an error.
- `bwapi.ini` lives in the GAME's bwapi-data (`~/.wine/drive_c/sc/bwapi-data/`),
  not in the distribution (`/sc-ai/BWAPI/...` is only the template copied once).

## Shared memory under Wine (client bots)
- The server creates Windows named sections; the client must use the W32
  connection backend (`-Dos.name=Windows 10`) or it searches `/dev/shm`
  forever ("Game table mapping not found").
- The client connects by PID from the game table. A dead PID in the table
  (from a killed server) = "Unable to open shared memory mapping" loop until
  the table is cleaned. Clean `/dev/shm/bwapi*` + `/tmp/bwapi_socket_*` only
  when no server is alive.

## OpenBW (the headless engine)
- BWEM on OpenBW: `assignStartingLocationsToSuitableBases` can leave start
  locations unassigned (its rule needs a geyser near the base); the fix is in
  our `src/bwem/BWMap.java` — nearest-base fallback.
- The OpenBW server dies when the game ends (`while !gameOver()`); a game with
  no opponent ends instantly, so the enemy race must be set
  (`BWAPI_CONFIG_AUTO__ENEMY_RACE`) or the client is always too late.
- ~30s between server start and first client frame is normal; 2
  "Unable to open communications socket" retries are normal; more = the socket
  is gone (server died).

## `canBuildHere` refuses a valid tile: `checkExplored` (cost: two sessions)

- **Symptom:** on OpenBW, `Unit.build(Pylon, tile)` returns `false` while every
  footprint tile probes as walkable, buildable, buildable-including-buildings and
  empty, and minerals suffice. Looks like the engine lying.
- **Cause:** the command path turns on an exploration check the probes did not.
  `Unit.build` -> `issueCommand` -> `canIssueCommand` -> `canBuild(type, tile,
  /*checkCanBuildHere=*/true, ...)` -> `Game.canBuildHere(tile, type, unit,
  checkExplored=TRUE)` -> OpenBW `Templates::canBuildHere`:
  `if (!isBuildable(x,y) || (checkExplored && !isExplored(x,y))) return false;`.
  One unexplored tile of the footprint is enough.
- **The trap:** the two-argument `Game.canBuildHere(tile, type)` defaults
  `checkExplored` to **false**, so a probe built on it reports a tile the actual
  command refuses. Always probe the same overload `Unit.build` reaches
  (4-arg, `checkExplored=true`), or probe `isExplored` per footprint tile.
- **Structure:** the Java overloads all bottom out in the C++ template; read
  `3rdparty/openbw/bwapi/bwapi/Shared/Templates.h` (`canBuildHere`) and the
  tile query pair `isBuildable` (terrain+occupancy) vs `isExplored` (fog) - they
  are independent answers and the command requires both.
