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

## Third solution: owner's Protoss Moon (src-old, read 2026-10-20)

Location: `src-old/atlantis/combat/squad/positioning/formations/moon/`
(6 files, 667 lines) + actuator
`src-old/.../positioning/protoss/formations/ProtossMoon.java`
(per-unit `move(slot, MOVE_FORMATION)`).

- **Shape**: crescent around the ENEMY (`MoonCenter` = nearest enemy
  combat unit, re-anchored past 1.4 tiles; units' midpoint nudged 33%
  toward the region center so the arc avoids walls). Arc span from
  unit widths + separation (arc length = r·θ), slots assigned per
  unit (cached 4-6f), crowded units take the nearest free slot,
  invalid slots fall back to the leader. Pure geometry - no map lib,
  no pathfinding, works on any map.
- **Radius**: leader's distance to the enemy, adjusted per unit -
  melee +2.5 (wider arc), leader -0.4 (tighter), weak eval +1/eval,
  recent-formation bonus +2/+3, capped 11/12/13.8, refused under 3.3.
  **Overtime shortening**: the arc closes 0.012/frame (capped -10,
  slower when strong) - commitment over time, fights never stall at
  max range forever.
- **Separation**: 20/(10+n) clamped, min = dragoon width + matchup +
  army/100 (tighter when weak).
- **Gate** (`ProtossMoonFormationApplies`, 262 lines): the formation
  applies when NOT fighting - no enemies in shoot range, no recent
  shots/under-attack/cooldowns, no choking/DTs, eval/strength-gated
  per matchup (PvP/PvZ branches), UMS forced at init. It is an
  APPROACH shape: contact hands off to micro. Clean separation of
  concerns - formation walks there, micro fights there.
- Weaknesses: straight-line walks to slots (walls!), no cohesion on
  the move (slots are static points), no unstick beyond
  leader-fallback, no AoE awareness, 262 lines of matchup
  special-casing (PvP/PvZ only, brittle), no focus fire.

## Best of each (owner asked for all three)

- **Stardust wins movement**: nav-grid waypoints + collision boid +
  fallbacks mean a unit NEVER stands still for lack of geometry;
  cohesion scaled by army size; vanguard-anchored regroup; unstick
  discipline (never re-issue into a wedge); decisions by sim, not
  thresholds. Weakest at: no pre-computed slots (shape is emergent),
  per-unit micro with no cross-unit fire sync.
- **PurpleWave wins structure**: per-class slots (ranged back, melee
  front - the actual front/back line), styles per situation with
  cadences, threat-aware escape paths, DPS-aware volley decision.
  Weakest at: heavy machinery (zones/edges/profiles, needs map
  analysis first).
- **Moon wins cheap arrival**: crescent geometry with zero map
  dependency, overtime shrink (commitment), region-nudged center
  (fewer wall pins), approach-only gating (micro takes over in
  contact), UMS init forcing. Weakest at: straight walks, no
  cohesion, no unstick, brittle matchup gate.

Steal in this order: Moon's crescent + shrink + region nudge for
ARRIVAL (cheapest body-keeping, proven in-house, no map lib) →
Stardust's cohesion/separation boids + unstick + fallbacks for
MOVEMENT → PurpleWave's class slots + threat-aware escape for
ESCAPE/FOCUS. That sequence is exactly the adoption order above:
Moon covers arrival, Stardust covers movement, PurpleWave covers
escape - and the 1v1 kite rules stay frozen through all of it.
