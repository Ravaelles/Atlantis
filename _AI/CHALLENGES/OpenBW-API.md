# OpenBW: which BWAPI queries can you trust? (measured 2026-10-10)

The reference table from the capability survey. Read this **before** trusting any
BWAPI query on the headless engine. Every row below is a measurement, not a
reading of the source; the run id is in the last section.

The survey is reproducible: put `OPENBW_PROBE=1` in ENV (or pass it to
`scripts/run-openbw-e2e.sh`) and the bot runs `atlantis.debug.OpenBwCapabilityProbe`
on a few early frames, printing `OPENBW_PROBE`-prefixed lines. It only *reads*
engine answers, never issues an order, so it does not disturb the game it measures
(`_AI/NOTES.md`, "scenario E2E probes are actuators, not sensors").

## The table

| Query | OpenBW result | Trustworthy? |
|---|---|---|
| `Game.isWalkable(walkPos)` | correct on every sampled tile | **yes** |
| `Game.isBuildable(tx, ty)` | correct on every sampled tile | **yes** |
| `Game.isBuildable(tx, ty, true)` (with buildings) | correct on every sampled tile | **yes** |
| `Game.isExplored(tx, ty)` | correct | **yes** |
| `Game.isVisible(tx, ty)` | correct | **yes** |
| `APosition.isWalkable()` / `.isBuildable...()` (our wrappers) | agree with the raw engine | **yes** |
| **`Game.hasPath(from, to)`** | **`false` for EVERY pair, at 32/64/128/256 px** | **NO** |
| **`Unit.hasPath(to)`** | **`false`, always** | **NO** |
| **`Game.hasPath(p, p)` (a point to itself)** | **`false`** | **NO** |
| `Game.canBuildHere(tile, type, unit, true)` | `false` everywhere (it calls `hasPath`) | **NO** - broken by the row above |
| `Unit.canBuild(type, tile)` | `false` everywhere (same reason) | **NO** |
| `Unit.build(type, tile)` | `false` everywhere (same reason) | **NO** |

## WHY: the engine has no regions at all (found in the OpenBW sources, 2026-10-10)

The headless run has **zero regions**. Measured by scanning the whole map, not by
sampling one point:

```
OPENBW_PROBE REGIONS probes=256 nonNull=0 distinctGroups=0 groups=[]
OPENBW_PROBE REGION p=280,1510 region=null || p=312,1510 region=null
```

256 samples across the map, **every one returns no region**. The chain that turns
that into `false` is short and fully documented in the sources, and it is worth
knowing because it means `hasPath` is not buggy - it is *honest about an engine that
was never initialised for pathing*:

1. `region` has `size_t index = ~(size_t)0;` as its default
   (`openbw/game_types.h:259-300`), and BWAPI's `Region::operator bool()` is
   `index != (size_t)-1` (`bwapi/Shared/RegionShared.cpp`). So a region that was
   never assigned answers **false**.
2. `Regions::tile_region_index` is `a_vector<size_t>(256 * 256)`, zero-filled
   (`game_types.h`). A tile that was never claimed by a region reads back as
   index `0`.
3. With `regions.regions` **empty**, index `0` has no backing region, so
   `getRegionAt` returns the default (`index = -1`) - which is `null` to BWAPI.
4. `Game::hasPath` is `if (rgnA && rgnB && rgnA->getRegionGroupID() ==
   rgnB->getRegionGroupID()) return ok; return Unreachable_Location;`
   (`bwapi/BWAPILIB/Source/Game.cpp`). Both regions are null, so it **always**
   returns `Unreachable_Location` - including for a point against itself, which is
   exactly what the survey measured.
5. `Region::getRegionGroupID()` is `self->islandID`, set from OpenBW's
   `Regions::group_index`, which is only assigned to regions with `tile_count > 0`
   during the neighbour BFS in `create_regions()` (`openbw/bwgame.h:20425-20445`).
   No regions means nothing to group.

**So this is not "pathing is broken" - it is "the region graph was never built".**
`hasPath` is a region-group comparison, not a path search, and on a run with no
regions it can only answer no.

## There is no Java-side workaround (tried and measured, 2026-10-10)

Do not spend time looking for one. A `BwapiOrderSink.build` branch was written that
decided placement from **our own** oracle (`MapTiles.canBuildHere`, which the survey
proved is right about terrain) and then sent a `bwapi.UnitCommand` directly via
`Unit.issueCommand` - i.e. the command, with the broken check bypassed. It did
**not** work: the Pylon was still cancelled at 36 s. The reason is in the bytecode:
`Unit.issueCommand(command)` calls `canIssueCommand(command, true)` with
`checkCanBuildHere` **hardcoded `true`**, so the internal re-check runs the same
`canBuildHere` -> `hasPath` that fails. Reverted; the branch is documented at its
old site in `BwapiOrderSink.build` so the next person does not retry it.

The only unchecked enqueue, `Game.addUnitCommand(int, ...)`, is **package-private**
and unreachable from `atlantis.*`. So the options are: build the region graph in the
harness/engine, or patch the vendored `JBWAPI-Rav.jar`.

**One more trap found on the way, worth knowing (it is easy to misread as the
bug):** `create_regions()` writes the index table with a **hardcoded stride of 256**
while reading the tile array with `map_tile_width`, in two adjacent lines
(`openbw/bwgame.h:20797-20798`):

```cpp
auto& index = game_st.regions.tile_region_index[y * 256 + x];   // fixed stride
auto& t     = st.tiles[y * game_st.map_tile_width + x];         // map stride
```

That looks like an off-by-a-map-width bug and it was my first theory, but it is
**not** the cause here: `tile_region_index` is allocated `256 * 256`
(`game_types.h`) and `get_region_at` reads it back with the *same* 256 stride, so
write and read agree. The mismatch is only a wasted-memory convention for maps
narrower than 256 tiles. The measured fact - **zero regions** - is what breaks
`hasPath`, not the stride.

Whether `create_regions()` is skipped or its result is not published to the client
in this harness is a separate question. What is settled: **the query is unusable,
deterministically, for the whole run**, and no Java-side trick avoids it.

## Read next: `_AI/CHALLENGES/TerrainAnalysis.md`

Before writing any workaround, read that file. Short version: **our own terrain
analysis works on OpenBW** (`bwem.BWEM` from tiles; the main choke is found), and so
does PurpleWave's (BWTA). The engine's region table is a *different* thing and is
empty. Every bot that computes terrain itself is unaffected by this - which is why
the fix is not "ask the engine differently" but "keep using our own model", and
for anything path-shaped, a tile BFS like PurpleWave's `GridGroundDistance`.

## The decision this leaves (owner's call)

1. **Make the harness build the region graph.** The real fix, and the only one that
   makes the E2E engine whole: after it, `hasPath` and `canBuildHere` work and the
   ordinary `Unit.build` path is usable by any bot, not just ours. It is in
   `StardustDevEnvironment`'s OpenBW (`create_regions()` in `openbw/bwgame.h`,
   called from map load) - not in this repo.
2. **Patch the vendored `lib/JBWAPI-Rav.jar`** so the OpenBW path skips the
   `canBuildHere` precondition. Cheap, unblocks a verdict now, but it edits a
   vendored library and leaves the engine still unable to answer `hasPath` for
   anything else that needs it.
3. **Accept that OpenBW cannot place buildings and test building on Wine only.**
   Honest, but it gives up the headless E2E goal.

Recommendation: **(1)**, with (2) as a stopgap if a game verdict is needed before
it lands. (3) is a fallback, not a plan.

## The one fact that matters

**`hasPath` is not "sometimes wrong" - it is always false, including from a point
to itself.**

```
OPENBW_PROBE PATH from=3022,3877 d32px:game=0,unit=0 d64px:game=0,unit=0 d128px:game=0,unit=0 d256px:game=0,unit=0
OPENBW_PROBE PATH self: game=0 unit=0
```

The self-case is what makes the diagnosis certain: no map, region, occupancy or
distance explanation survives a graph that cannot reach the node it is standing on.
The engine's path query is simply not implemented/initialised in this headless
harness.

And the map layer is **fine** - which is why every earlier theory (terrain,
exploration, occupancy) was wrong and cost time:

```
OPENBW_PROBE MAP tiles=49 notWalkable=0 notBuildable=0 notExplored=0
OPENBW_PROBE MAP sample (91,118)w=1b=1bi=1e=1v=1apiW=1apiB=1 ...
```

## Why this broke *building* specifically

`Templates::canBuildHere` ends with
`if (!builder->hasPath(Position(lt) + Position(type.tileSize())/2)) return false;`
(`3rdparty/openbw/bwapi/bwapi/Shared/Templates.h:196-201`). Since `hasPath` is
always false, `canBuildHere` is always false, so `Unit.build` can never succeed -
**on this engine, no building of any kind can be placed through the normal BWAPI
call**, on any map, not just TauCross. That is why pinning a different map would
not have helped.

The check is **client-side only**: `UnitImpl::issueCommand` runs `canIssueCommand`
and the server (`GameCommands.cpp`) queues `MakeBuilding` without re-checking. So
the command is refused before it is ever sent, and a path that sends it without the
pre-check would work.

## Rules that come out of this

1. **Never rely on `hasPath` on OpenBW.** Anything built on it - `canBuildHere`,
   `Unit.canBuild`, `Unit.build` - is unusable there.
2. **Map queries are safe.** `isWalkable`/`isBuildable`/`isExplored`/`isVisible`
   answered correctly on every sample, and our `APosition` wrappers agreed. Keep
   using them.
3. **`isWalkable` takes a WALK position (8x8 px), not a tile.** Passing tiles reads
   the wrong cell - this produced one false lead in the #48 investigation before
   the survey's explicit `toWalkPosition()` conversion.
4. **A survey beats one-at-a-time discovery.** Every one of these answers was found
   separately, live, in a game, over days. The probe that produced this table runs
   in seconds.
5. **ENV booleans are strict**: `trueFalse()` accepts only the literal `"true"`. A
   hand-written `=1` is silently ignored (measured here - the first probe run
   looked like it did nothing). `OPENBW_PROBE` accepts both because it is a
   diagnostic switch, but be aware of the rule for every other flag.

## Run

`OPENBW_PROBE=1 bash scripts/run-openbw-e2e.sh "maps/cog/(3)TauCross1.1.scx" Protoss Zerg`,
`48` `OPENBW_PROBE` lines in `out/openbw/bot.log`, samples at frames 20/40/60/80/100/120.

## Frame-rate note (useful for budgeting)

The same run showed the bot playing frames 2 -> ~333 in its play window, i.e.
**~33 game frames per wall-second** on this headless engine. That is what sizes a
scenario: a Pylon due at frame 557 needs ~17 s of bot runtime, which is why a
20-second single-test budget cannot reach it (CONVENTIONS §17, and the reason the
`BOT_KILL_SECONDS` split needs care at small budgets).
