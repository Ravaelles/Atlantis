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

## `isBuildableIncludeBuildings` says occupied where nothing stands (measured 2026-10-10)

- **Symptom:** every Pylon placement is refused with
  `Can't find place for \`Pylon\``, `(reason: Can't physically build here)`;
  `MapTiles.canBuildHere` is false while the tile looks fine.
- **What the engine actually answers**, printed per footprint tile of a refused
  2x2 position at `[117,8]` on TauCross:

  ```
  (117,8) w1 b1 bi1    (118,8) w1 b1 bi1
  (117,9) w1 b1 bi0    (118,9) w1 b1 bi0
  ```

  `w` = isWalkable, `b` = isBuildable, `bi` = isBuildable(...,true). Two tiles read
  walkable AND buildable but **not** buildable-with-buildings.
- **The trap:** `bi=0` reads as "a building stands here", and that is how it was
  interpreted - which sent the investigation into occupancy code. It is not true.
  The refused tiles are `[117,9]` and `[118,9]`, and the only units nearby were
  `Nexus#77@[119,10]` (4x3, covering x 119..122, y 10..12) and
  `VespeneG#7@[119,5]` (4x2, x 119..122, y 5..6). **Neither covers x=117 or x=118.**
  The tiles are empty; the engine still reports them occupied-with-buildings.
- **So a building can be refused a tile that is empty, walkable and buildable.**
  `BuildingTilesAreOccupied.check` (our own live-unit-list guard) correctly says
  `false` for the same tile, which is the contradiction that identifies this.
- **Rule:** when `MapTiles.canBuildHere` is false on a tile that is walkable,
  buildable and empty, print `bi` **per footprint tile plus the units within ~4
  tiles** before touching occupancy logic. `bi=0` alone is not evidence of a
  building, and believing it costs a session.
- **Unresolved:** whether `bi` is wrong everywhere or only near units/structure
  edges, and whether it is the same defect as the region/`hasPath` emptiness. The
  next measurement is a scan of `bi` across a known-empty area with no units
  nearby: if `bi=0` appears there too, the query is unusable on OpenBW the way
  `hasPath` is.
