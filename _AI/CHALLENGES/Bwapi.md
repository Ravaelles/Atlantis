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

## `bi=0` means a UNIT is standing there - the engine is right (measured 2026-10-10)

- **Symptom:** every Pylon placement is refused with
  `Can't find place for \`Pylon\``, `(reason: Can't physically build here)`, and
  `MapTiles.canBuildHere` is false while the tile's own queries look fine.
- **What the engine answers**, printed per footprint tile of a refused position,
  together with the tile's own state:

  ```
  BI0_TILES Pylon at=[7,43]  (7,44) explored=1 visible=1 unitOnTile=2
                             (8,44) explored=1 visible=1 unitOnTile=1
  ```

  `isBuildable(...,true)` - `bi` - counts **units**, not just buildings. The refused
  tiles are walked by our own workers around the mineral line, so the engine refuses
  them correctly and the refusal is **our candidate search's fault, not the
  engine's**.
- **An earlier reading of this was wrong and is withdrawn.** A first probe sampled
  only the origin tile and units within ~4 tiles of the *origin*, concluded the
  tiles were empty, and recorded "the engine reports occupied where nothing stands".
  That was an artefact of where the probe looked: per-tile counting shows the units
  immediately. **Do not repeat it - `bi=0` is not evidence of a bug.**
- **The real defect this exposes:** the two occupancy answers disagree by design.
  `bi` (`isBuildableIncludeBuildings`) counts **all units**; our
  `BuildingTilesAreOccupied.check` counts **buildings only**. `POSITION-FINDER.md`
  point 2 records the other half of this history: the guard once counted every unit,
  was narrowed to buildings because "a worker or a patch two tiles over overlapped a
  Pylon's footprint", and that narrowing is what left the engine and our oracle
  answering different questions.
- **Rule:** when `canBuildHere` is false on a tile that reads walkable and
  buildable, count units **per refused footprint tile** (`Select.all().inRadius(0.5,
  tile)`) before concluding anything about the engine. Then decide the policy: a
  worker on a tile is transient and the builder can be sent to shoo it away, but a
  search that never accounts for it re-picks the same tile forever.
- **Still open:** why the search does not move past a tile a worker occupies. The
  builder picks a candidate, a worker is standing on it, and nothing makes either
  the worker move or the search advance - which is the shape of the 36 s
  `took too long` cancel.
