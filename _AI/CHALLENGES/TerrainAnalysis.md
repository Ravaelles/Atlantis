# Terrain analysis: what the bot uses, and why engine queries are not it

The reference for "where does the map knowledge come from". Read this before
adding anything that asks the engine about terrain, paths or regions, and before
concluding that terrain analysis is missing on OpenBW.

## The one-line answer

**All three bots (Atlantis, Stardust, PurpleWave) compute terrain themselves, from
tiles. None of them asks the engine for a path or a region.** The engine query
that broke building (`hasPath`) is not part of any of their designs - which is
exactly why they work and why ours did not.

## The evidence, per project

### Atlantis (ours) - measured healthy on OpenBW

Live survey (`OPENBW_PROBE=1`), from a Probe's point of view, on `(3)TauCross1.1`:

```
OPENBW_PROBE OURS bwemAreas=21 ourRegions=21 jbwebInit=true mainChoke=Choke{[12,25], width=1}
```

**Everything we depend on is built and working:** 21 BWEM areas, 21 of our regions,
and `jbwebInit=true`. This is the fact that reframes the whole OpenBW problem:
our terrain analysis was never the thing that was broken.

- `AMap.initMapAnalysis()` builds our **own** map model in Java:
  `new BWEM(Atlantis.game())` + `bwem.initialize()` + `assignStartingLocationsToSuitableBases()`
  (`src/atlantis/map/AMap.java:50-54`).
- `bwem.BWEM` here is the **Java library in `lib/JBWAPI-Rav.jar`**, not OpenBW's
  internal `bwem`. It initialises from **tiles**:
  `bwem.AreaInitializer` uses `Game.getTile`, `WalkPosition` and `TilePosition` -
  it never calls `getRegionAt` (verified against the jar's bytecode).
- So our regions, areas and chokes come from our own analysis.
  `src/atlantis/map/region/Regions.java:133` reads them via
  `AMap.getMap().getAreas()`.
- **This works on OpenBW.** Measured in `out/openbw/bot.log`:
  `Analyzing map...` and a correct `MainChoke, choke=Choke{[67,114], width=2}`.
- **`jbweb.JBWEB` is pure Java** (`src/jbweb/JBWEB.java`), not JNI, and it is
  **initialised on OpenBW** (`jbwebInit=true`, measured). It builds its own
  `walkGrid[256][256]` from `Game.isWalkable(walkPosition)` - the query the survey
  found correct on this engine - and `JBWEB.isWalkable(TilePosition)` is just
  `walkGrid[x][y]`. So we have a working tile-walkability model of our own.
  **Correction to `_AI/POSITION-FINDER.md` point 3:** that note says JBWEB is
  unusable on Linux because its natives do not exist. That is true of the
  **`bweb.*`/`jbweb` JNI helpers if any call into natives**, but the map model
  above is plain Java and measured working; do not cite the old note to conclude
  that everything named JBWEB is dead on OpenBW. Measure `jbwebInit` instead.
- `InitBWEB` / `bweb.*` are the fragile JNI-shaped helpers; they are used for
  wall/block/station logic, not for region knowledge.

### PurpleWave (Scala - the clearest case)

- Regions come from **BWTA**, not from the engine:
  `GeographyBuilder` uses `BWTA.getRegion(tile.bwapi)`.
- It **never calls `hasPath` or `getGroundDistance`**. Grep for both across its
  source returns nothing outside its own classes.
- It ships its **own pathfinding**: `Information/Geography/Pathfinding/`
  (`TilePathfinder`, `ZonePathfinder`, `TilePath`, `ZonePath`, `GroundDistance`).
- `GridGroundDistance` is a hand-written **BFS flood fill over tiles** - `open`/
  `openSize` buffers, neighbours at `i±1` and `i±width`, starting from a set of
  origin tiles. No engine query anywhere.

### Stardust (C++)

- Its own placement and wall logic work from a **tile availability bitmap** it
  builds itself (`Builder/BuildingPlacement.cpp`, `Blocks/**`), and it measures
  distances itself (`Geo::EdgeToEdgeDistance`).
- It calls `builder->build(...)` and reacts to the result; against real BWAPI the
  engine's path query is fine, which is why it never needed a workaround.
  (Not detailed further here - PurpleWave makes the point with less noise.)

## Why this matters for the OpenBW problem

The build failure was diagnosed down to `hasPath`, which is a **region-group
comparison in the engine** (`Game::hasPath` -> `Region::getRegionGroupID` ->
`Regions::group_index`), and in this headless run the engine's own region table is
**empty** (`OPENBW_PROBE REGIONS probes=256 nonNull=0` - see `OpenBW-API.md`).

Two separate region concepts, and conflating them is the trap:

| | our regions | engine regions |
|---|---|---|
| built by | `bwem.BWEM` (Java, from tiles) | `create_regions()` in OpenBW |
| reachable as | `AMap.getMap().getAreas()`, `Regions` | `Game.getRegionAt` |
| state on OpenBW | **works** (choke found) | **empty** |

So "terrain analysis is missing on OpenBW" is **not** true - ours works, and that
is why the bot can analyse the map, find chokes and plan expansion. What is missing
is the **engine's** region table, which only the engine's own `hasPath` depends on.
No bot that computes terrain itself notices it.

## The consequence for building on OpenBW

Since our model is healthy, the `hasPath` problem stops being an engine-repair
problem and becomes a **use-our-own-model** problem. Concretely:

1. `MapTiles.hasPathBetween` currently falls back to `JBWEB.isWalkable(
   to.toTilePosition())` - that is a *walkability* check masquerading as a
   *reachability* answer, and it only ever tests the destination tile. It should
   ask a real question over our grid.
2. A real answer is a **tile BFS/flood fill** over `walkGrid` (or
   `Game.isWalkable(walkPosition)`), starting from the builder and testing whether
   the site's tiles are reached - the exact shape PurpleWave's
   `GridGroundDistance` uses. Our `walkGrid` is already built and correct.
3. `MapTiles.canBuildHere` must then stop depending on the engine's
   `canBuildHere` for the OpenBW case (it already has the `tilesCoveredAreBuildable`
   fallback; the missing piece is that `Unit.build` itself still goes through the
   broken client-side check - see `OpenBW-API.md` for why that needs a jar patch or
   a harness fix, and why a Java-only bypass does not exist).

So the honest split is: **terrain we can fix ourselves** (and should - the model is
ours and works), while the **engine call that refuses the command** is the piece
that needs the jar patch or the harness's region table.

## Rules that come out of this

1. **Never call `Game.hasPath` / `Unit.hasPath` / `getGroundDistance` /
   `Game.canBuildHere` to decide terrain on OpenBW.** Use our own model
   (`MapTiles`, `AMap.getMap()`, `Regions`, `Chokes`) - it is correct there and
   measured to be.
2. **A path question is a tile BFS, not an engine query** - the shape PurpleWave
   uses (`GridGroundDistance`). If a path is genuinely needed (wall gaps, blocking
   spots, "can the builder reach the site"), implement it over our tile grid.
3. **Do not treat our working analysis as broken because an engine query fails.**
   The two are independent, and the failure of one says nothing about the other.
4. **When the engine and we disagree about terrain, we are the authority** - this
   is already the doctrine in `_AI/POSITION-FINDER.md` ("one question, one source of
   truth"), and the survey confirmed our map answers match the engine's tile
   queries while only the path/region query is empty.
