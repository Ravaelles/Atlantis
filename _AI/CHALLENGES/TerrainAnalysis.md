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

## Done: our own reachability replaced the engine query (2026-10-10)

`MapTiles.hasPathBetween` no longer falls back to `JBWEB.isWalkable(to)` (a
walkability check on the destination tile only - it said "yes" for a tile across an
impassable wall). It now asks `jbweb.Pathfinding.reachable(from, to)`, a flood fill
over our own `walkGrid`, the same shape PurpleWave's `GridGroundDistance` and this
repo's own unused `Path.bfsPath` use.

Measured, our answer next to the engine's on the same pair:

```
OPENBW_PROBE OURPATH from=[120,12] to=[126,18] ours=1 engine=0
OPENBW_PROBE OURPATH from=[123,12] to=[129,18] ours=0 engine=0
```

The first line is the fix: **we answer yes where the engine cannot answer at all**.
The second is not a regression - a destination outside the walkable area is
genuinely unreachable, and saying so is the point of asking a real question
instead of a walkability flag. The engine says `no` for both, which is why its
answer carried no information.

Tests: `tests.unit.PathfindingReachableTest` (7) - isolation, symmetry, a wall with
a gap, a wall without one, self-reachability, cache reuse. They drive the grid
directly, so they run with the fast suite (355/355) instead of needing a game.

### The whole class of the bug, closed in three places

`hasPathBetween` was only the first site. The same wrong shape - asking the engine
for a path - was in two more places, both used widely in production:

- `APosition.hasPathTo` called `Atlantis.game().hasPath(...)` directly;
- `AUnit.hasPathTo` (both overloads) called `u.hasPath(...)`.

Twelve production call sites reach them: start-location choice, expansion, attack
targeting, worker retreat and combat missions. On OpenBW all of them were silently
fed `false`. Both now go through `MapTiles.hasPathBetween`, so there is **one** place
that answers the question, and it is ours.

### Area graph first, tile flood fill second

`MapTiles.hasPathBetween` now asks our BWEM area graph first, because that is the
same shape the engine was supposed to provide and it already exists:
`bwem.Area.isAccessibleFrom` (used by `ARegion` and `PathToEnemyBase`). It answers
only for two points in **different** areas; same-area and out-of-area pairs fall
through to the tile flood fill, because the area graph knows nothing about a local
wall or a building inside one area. Measured live: every probed pair now answers
`ours=1 engine=0`, where before the tile-only fallback returned `0` for some.

### Measured effect on the E2E run

With the reachability fix plus a corrected teardown margin (the bot used to get
only half of a 20 s budget), a bounded run now **ends the game itself**:

```
verdict: ingame=1209s killed=0 resourceBalance=0 pylons=0 gateways=0
Total time / Defeat, bot exit 0, zero exceptions, runtime 16s of 20s budget
```

So the engine, the bot and the map analysis are all healthy - the bot plays 20
game-minutes and finishes cleanly. `pylons=0` is the **remaining** half: the
command gate, which no Java code can bypass (`OpenBW-API.md`).

## The consequence for building on OpenBW

Since our model is healthy, the `hasPath` problem stops being an engine-repair
problem and becomes a **use-our-own-model** problem:

1. **DONE** - `MapTiles.hasPathBetween` asks `jbweb.Pathfinding.reachable` (a flood
   fill over `walkGrid`) instead of the destination-tile `JBWEB.isWalkable` check it
   used to do. See the section above for the measurement.
2. **Open** - `Unit.build` still goes through the engine's client-side
   `canBuildHere`, which is refused because the engine's region table is empty.
   That is the piece no Java code can bypass (`OpenBW-API.md`: `issueCommand`
   hardcodes the check, and the only unchecked enqueue is package-private), so it
   needs either the region graph built in the harness or a patch to the vendored
   jar.

So the honest split is: **the terrain half is fixed here** (the model is ours and
now actually used), while the **command half** remains the owner's decision.

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
