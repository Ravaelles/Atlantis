# Formations: how Stardust keeps the cluster together (read 2026-10-20)

Owner question: why does Stardust's army move as one body while ours
scatters into 1v1s that die surrounded? Answer from the source, not
from memory. Primary: `/sc-ai/Stardust/src/General/UnitCluster/`
(`Formations/Ball.cpp`, `Formations/Arc.cpp`, `Tactics/Flee.cpp`,
`Tactics/Regroup.cpp`). Secondary: PurpleWave
`src/Micro/Formation/FormationStandard.scala`,
`src/Micro/Coordination/Pathing/MicroPathing.scala`.

## Stardust model: vanguard + center + shared destination

A cluster owns a **vanguard** (front unit), a **center** (centroid) and
an **area**. Every formation routine moves each unit toward a position
derived from SHARED state (pivot/center/waypoint), never from a
per-unit threat read. That is the whole difference: our kite picks a
step per unit per threat; theirs picks one shape per cluster and slots
units into it.

## Ball (movement formation, `Formations/Ball.cpp:35-176`)

Four boids per unit, summed into one move order:

- **Goal (128)**: third navigation-grid node ahead toward the target
  (`NextGridOrChokeWaypoint(..., 3)`). Pathfinding first, vectors
  second - collisions with terrain are avoided by routing, not by
  stepping around walls.
- **Collision (64)**: away from unwalkable tiles (`Map::collisionVector`),
  only when the pushed position itself is collision-free.
- **Cohesion (64/sqrt(area/pi))**: toward the cluster center, scaled
  DOWN as the squad grows (big armies hold looser). Skipped across
  narrow chokes (no pulling through a choke the unit can't share).
- **Separation (2.0/96; 160 with AoE radius+16)**: from nearby friends,
  widened against splash. Same boid the kite uses to not block friends.

Fallbacks, in order: boids fail → waypoint → direct `moveTo`. A unit
never stands still for lack of a geometric idea. Units with no grid
still move (degraded, not stuck).

## Arc (fighting formation, `Formations/Arc.cpp:16-130`)

Pivot + desired distance (usually the vanguard pushed 64px past the
nearest enemy, `Flee.cpp:54`). Per unit, in order: unstick first, skip
if mid-attack (`isReady`), flyers go to the vanguard, stragglers
(>128px behind the vanguard's pivot distance) return to the vanguard,
everyone else holds `desiredDistance` from the pivot with goal weight
at least 2x the separation weight (formation beats spacing, never the
reverse). Invalid boid → move to the vanguard (regroup, not idle).
No arc in narrow ground (`walkableWidth < 7` at the vanguard).

## Flee and Regroup (`Tactics/Flee.cpp`, `Tactics/Regroup.cpp`)

Flee today is one line: move to the main (`Flee.cpp:12` - the arc
fallback is present but DISABLED as unsafe). Regroup picks the
strategy by combat sim: contain static, hold choke, stand ground,
flee to main, or attack the blocking army. The decision is
cluster-level (sim-gated with stability buffering); the movement is
always a formation (Ball out, Arc when safe), never per-unit runs.

## PurpleWave: slots + styles + threat-aware paths

`FormationStandard`: per-class slots around an apex facing a threat
(`threatOrigin`) or goal, vanguard = closest 50% toward it, styles
March/Engage/Disengage/Guard with per-style step cadences, zone/edge
geometry for chokes, auto-switch March→Engage when targets close.
Retreats walk `getThreatAwarePath` (repulsors + threat cost 6 +
occupancy cost, threat maximum 0, length 7-15 tiles toward safety),
unstick by re-issuing on `seeminglyStuck`. Same principle as Stardust:
the PATH is threat-aware, the SHAPE is shared, the unit only executes.

## What Atlantis has today (2026-10-20)

Cluster with anchor + Alpha center + gatherPoint, but NO shared
movement shape: kiting is per-unit (`BattleCommander` threat pick +
`MoveAway` compass), retreats rally per-unit, stuck = STOP reset,
unwalkable = null (now silent, KITE twin carries it). Consequences
measured on Wine: 92-1500% stuck-no-step in brawls, full wipes after
the line breaks, wins only when zealots accidentally hold long enough.

## Adoption order (cheapest first, each game-proven)

1. **Shared escape direction** (no pathing): the leader picks one
   threat-gradient direction per ~10 frames; followers step relative
   to it instead of each picking their own compass point. Kills the
   line-break without any map data.
2. **Threat-aware step sampling**: score compass candidates by
   threat-at-destination + walkability (PurpleWave profile, cheap
   local version of the repulsor grid) instead of pure distance.
3. **Vanguard + cohesion**: name the front unit, pull stragglers to it
   (Stardust Arc rules), hold the ball on the march (Stardust Ball
   boids minus the nav grid).
4. **Full path integration**: threat-aware escape paths once the map
   library owns reachability (M4/M5) - the advanced fleeing mechanism
   specced in `redesign/02_COMBAT.md`, not before.
