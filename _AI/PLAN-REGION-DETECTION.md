# Plan: our own reachability, so OpenBW placement stops depending on engine regions

> **Done 2026-10-11:** stages 1-3 shipped (`ReachableTiles` port with the
> terrain fallback, all callers routed) and stage 4 resolved in
> `_AI/PLAN-STAGE4-COMMAND-PATH.md` (library patch). The "not started" status
> underneath is stale. Kept as the design record.

Status 2026-10-10: **plan, not started.** Written because the last blocker of the
first Pylon is now fully identified and it is not ours to fix in application code.

## Why this exists (measured, not assumed)

`Unit.build(...)` returns `false` for every attempt on OpenBW, while every
observable precondition is true:

```
BUILD_ATTEMPT @1341 unit=#119 Probe building=Pylon engineAccepted=false
  || canBuildHere=true explored=4/4 canAfford=true distToBuild=0.2
     canCommand=true isPowered=true isLockedDown=false isCompleted=true
```

104 attempts in one run, 104 refusals, with `Can't find place` at **zero** - i.e.
placement now works and this single engine term is all that is left.

The chain: `Unit.build` -> `canIssueCommand` -> `canBuild` ->
`Game.canBuildHere(tile, type, builder, true)`, and inside it
(`Shared/Templates.h:196`):

```cpp
if ( builder ) {
    if ( !builder->getType().isBuilding() ) {
      if ( !builder->hasPath( Position(lt) + Position(type.tileSize())/2 ) )
        return false;                       // the refusal
    }
}
```

`Unit::hasPath` -> `Game::hasPath` -> **BWAPI regions**, and the OpenBW server
publishes `regionCount = 0` (`Server.cpp:362/368`), so `hasPath` is false for every
pair of positions on the map. The builder "cannot reach" a tile it is standing on.

This is the third appearance of the same defect: `MapTiles.hasPathBetween` works
around it, `IsProbablyInAnotherRegion` was switched to that workaround, and the
library's own `canBuild` is where it now bites - inside `jbwapi-2.2.0.jar`, so not
reachable from our application code.

## What the reference projects do (checked, this is the answer to "how do they cope")

**Neither of them asks the engine.** That is the whole trick - they never depended
on regions, so the bug cannot reach them.

- **Stardust** runs its **own A\*** over tiles: `PathFinding::Search(start, end,
  tileValidator, closeEnoughToEnd)` (`src/Map/PathFinding/PathFinding.h:108`), with a
  caller-supplied `tileValidator` and, at the placement site,
  `hasPathWithBuilding = PathFinding::Search(startTile, endTile,
  validPathfindingTile)` (`src/Builder/BuildingPlacement_Walls.cpp:295-308`). Walkability
  comes from its own grids (`PathFinding_Grids.cpp`, `NavigationGrid`), and ground
  distance from `PathFinding::GetGroundDistance`.
- **PurpleWave** uses raw terrain flags: `game.isWalkable(WalkTile(...))` at the four
  corners of a footprint (`src/Placement/Generation/CannonRushFinder.scala:89-92`).

So the claim "they patched the broken regions" is **wrong** - they bypassed them. What
we need is the same thing at our level, which is what this plan is.

## What we can and cannot do about it

Options, cheapest first, with the actual blocker each one leaves:

| # | Option | Fixes the refusal? | Leaves |
|---|---|---|---|
| 1 | Swap the jar | **No** - measured: `jbwapi-2.2.0.jar` still calls `Unit.hasPath`, and `canBuildHere` has the same builder branch | nothing |
| 2 | Don't pass the builder | **No (worse)** - the builder branch is skipped, but the *command path* still runs its own validation; and the reason the branch exists (route planning) is lost | unverified |
| 3 | Patch the harness (`Shared/Templates.h`) | **Yes** - skip the reachability check when regions are absent | a change to shared platform code |
| 4 | Stop issuing `build` through BWAPI | **Yes**, and it is the direction the architecture already chose | rewrite of the command path |

**Option 4 is the plan of record** (`_AI/PLACEMENT-CUTOVER-PLAN.md`): the rewritten
planner replaces the legacy finder, and the placement decision moves into our code -
which is exactly what makes Stardust immune. The work below is the piece of that
cut-over that this investigation proved is needed.

> **Stages 1-3 are done and the plan has moved to
> `_AI/PLAN-STAGE4-COMMAND-PATH.md`**, which restates the four options with their real
> costs and names the first task (a stack trace at the LIVE build call site, not an
> edit). Read that one for what to do next; this file explains why the work exists.

## Target design (SOLID, mirroring Stardust without copying its shape)

Stardust's A\* is the *algorithm*; its `tileValidator` is the *seam*. We take the seam
and keep our own architecture (CONVENTIONS §5: adapters -> application -> contexts ->
core; no new statics).

```
core (no bwapi, no statics)
  Reachability                 interface: boolean canReach(from, to)
  WalkabilityGrid              interface: boolean isWalkable(tx, ty), isWalkable(tx, ty, size)
  TilePath                     pure A* over WalkabilityGrid, own open/closed sets
  ConnectivityRegions          UNION-FIND over WalkabilityGrid -> region id per tile
                               (this is the piece that replaces "engine regions")

adapters
  BwapiWalkability             implements WalkabilityGrid from isWalkable/isBuildable
  EngineReachability           implements Reachability via Game.hasPath   (legacy truth)
  MapDataReachability          implements Reachability via ConnectivityRegions
  ReachabilityFactory          picks MapData when the engine reports no regions
```

Why union-find and not "copy Stardust's A\*": connectivity ("are these two tiles in
the same walkable area") is a **different question** from "what is the shortest path",
and it is the question the engine's `hasPath` was being asked. A one-off flood
fill / union-find answers it in O(tiles) once per map state, needs no path search at
all, and cannot disagree with itself the way the three existing answers do. The A\*
(`TilePath`) is then only needed where a real path is wanted (travel time, choke
routing), and can come later.

**Escape hatch check - the point of the design:** `ReachabilityFactory` must *detect*
the broken engine, not assume it. The detection is: ask the engine whether a tile
can reach its own neighbour; a healthy engine says yes, a region-less one says no.
That is one query at startup, needs no version sniffing, and is the guard the plan
asks for.

## Staged delivery, each step shippable

**Status 2026-10-10: steps 1-3 are DONE and committed; step 4 has its own plan
(`_AI/PLAN-STAGE4-COMMAND-PATH.md`); step 5 is not started.**

1. **`WalkabilityGrid` + `ConnectivityRegions` (core, unit-tested).** DONE - pure, no
   bwapi, 10 tests, mutation-checked. `ConnectivityRegionsTest` pins the property the
   engine violates: a tile must reach itself.
2. **`Reachability` port + `RegionReachability` + the engine-incapability probe.**
   DONE - `EngineReachabilityProbe` asks whether a walkable tile reaches itself, which is
   a capability measurement, not a version check; 9 tests over both outcomes.
3. **Route every caller through the port.** DONE - `ReachableTiles` resolves the
   implementation once per game and `MapTiles.hasPathBetween`,
   `IsProbablyInAnotherRegion` and `APosition.hasPathTo` all go through it. Verified in a
   live run: the probe logged `engine publishes no regions - answered from terrain` on its
   own, `Region mismatch` fell from 549 to **0**, `Can't find place` stayed at 0.
4. **The command path.** NOT STARTED - this is the step that actually unblocks the
   Pylon. Stage 3 changed what *we* believe; the library still runs its own
   builder-reachability check inside `canBuild`, and a live run confirms it:
   `engineAccepted=false` on 112 of 112 attempts with every precondition true. Four
   options with their costs are laid out in `_AI/PLAN-STAGE4-COMMAND-PATH.md`; the plan
   of record is finishing the catalogue cut-over, and its first task is `NEXT.md` #48.
5. **`TilePath` (A\*) only if something still needs a real path** - travel time and
   wall/choke routing. Deliberately last: it is the expensive part and nothing in
   this investigation needed it.

## What this does NOT do, stated plainly

- It does **not** make the engine's own `canBuildHere` work. Any code path that still
  calls `Unit.build(...)` will still be refused, however good our regions are. Steps
  1-3 give us a correct answer *for our own decisions*; step 4 is what changes what
  the engine is asked to do.
- It does **not** replace BWEM. BWEM initialises fine on OpenBW (`MAP_ANALYSIS
  areas=21 chokes=42`) and answers areas/chokes; this plan is about *reachability*,
  which BWEM does not provide either.
- Option 3 (patching the harness) is not proposed here. It is the honest fix for every
  Java bot on this engine, but it is shared platform code and needs the owner's
  decision; this plan is the version that lives entirely in our tree.

## Evidence to keep

- The `BUILD_ATTEMPT` trace with all preconditions true and `engineAccepted=false`.
- The self-neighbour probe result, before and after any change.
- A bounded OpenBW run in which a Pylon is actually constructed.
