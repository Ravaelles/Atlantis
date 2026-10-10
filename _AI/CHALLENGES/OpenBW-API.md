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
