# Formations: how Stardust keeps the cluster together (read 2026-10-20)

> **STATUS: DEFERRED (owner 2026-10-20).** Design complete, implementation
> NOT started - starts on the owner's call. Natural slot: migration
> Phase 3 (combat cluster behavior, after the Phase 0 entry gate holds:
> average battles won). Until then this file is the memory; the 1v1
> kite rules stay frozen and no formation code lands.

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

## Target architecture: PurpleWave-class synthesis (owner decision 2026-10-20)

Owner: no half-measures - the target is the best of all three, with
PurpleWave as the skeleton (only it answers "where each unit stands
in every situation"), Stardust movement underneath it, Moon geometry
inside it. Honest costing: the full system needs map reachability
(zones/edges - migration M4/M5), so the architecture lands phased;
the phase order is dependency order, not scope cutting.

### Components (all inside the Combat context)

- **BattleDetector** (PurpleWave MatchupGraph / Stardust 480/480/600):
  proximity clustering assigns every combat unit to exactly one
  battle per frame. No unit micro-manages itself outside a battle.
- **SimGate** (Stardust combat sim + stability buffering): per battle
  WIN/LOSE/TRADE verdict with hysteresis (no flip-flop under N
  frames of consensus). The verdict picks the style, never a unit.
- **Formation** (PurpleWave FormationStandard skeleton):
  style ∈ {March, Engage, Disengage, Guard}; face = threat
  (Engage/Disengage) or goal; apex = vanguard centroid (closest 50%
  toward face); slots per class on a Moon crescent around the
  pivot (melee arc inside, ranged arc at weapon range, span from
  unit widths, overtime shrink for commitment); separation widened
  vs AoE. Zone/edge geometry plugs in at M4 - until then apex/face
  from centroid + threat pixel + known chokes.
- **Mover** (Stardust Ball boids): goal = path waypoint (straight
  line until M4, grid path after) + collision (away from
  unwalkable) + cohesion (toward center, scaled down with army
  size, skipped across narrow chokes) + separation. Fallbacks:
  waypoint → direct move. Never null, never idle.
- **Micro handoff** (Moon gate, current kite frozen): units with a
  target in range fight per-unit (kite margin, sticky target,
  wound/low-HP/surround rules - proven on the 1v1, untouched);
  units without a target take formation slots. Formation walks,
  micro fights - never both at once.
- **Escape** (PurpleWave retreat): sim says LOSE → shared escape
  direction (leader, ~10f cadence) + threat-aware sampled steps
  (threat + walkability score) → regroup rally → re-form. Three-deep
  fallback per step, cornered trades (never idle).
- **Focus** (the missing multiplier): shared target = weakest in
  range of the most guns, sticky until dead; uncommitted units join
  it, committed units finish theirs (sticky-first ordering).

### Per-frame algorithm

1. Cluster units into battles (proximity); orphans rally to the
   nearest cluster, never fight alone.
2. Sim-gate each battle → style (winning/tradable = Engage,
   losing = Disengage, travelling = March, holding = Guard).
3. Compute face/apex/slots (Moon crescent per class, shrink with
   fight time, widen vs AoE).
4. Units with a target in range → per-unit micro (frozen rules).
   Units without → move to slot (boids + fallbacks).
5. Sim says LOSE and no winning trade → shared escape direction,
   threat-aware steps, regroup, re-form.
6. Shared target re-picked only when dead/out of range (sticky);
   style changes only on sim consensus (hysteresis).

### What PurpleWave does when the arc does not fit (owner asked 2026-10-20)

The slot tracer (`FormationStandard.arcSlots`, alternating
left/right from center, row by row outward) never emits a bad
pixel - each candidate is tested and only surviving pixels become
slots:

- walkable + wrong altitude / under enemy fire
  (`enemyRangeGround > margin`) / occupancy-grid taken → skip,
  keep tracing;
- unwalkable + different zone → HALT that direction (the arc does
  not cross into bad terrain), keep tracing the other side;
- unwalkable + same zone → skip, keep tracing;
- 10k evaluations → stop (safety valve, broken maps).

Occupancy is pre-blocked on a grid: mineral lines, reserved tiles,
push traffic (Shove+), scarab paths. Every anchor snaps to walkable
(`walkableTile`/`walkablePixel`); air units just follow the ground
exemplar. Whole-arc failure has a named fallback: Guard that cannot
fill its slots assumes the choke is breached, backs up one choke
and rebuilds there. A unit with no slot (`Formation.apply` = None)
falls back to non-formation behavior instead of standing still.

Mapped to our Moon problem (unwalkable slots, stepping into walls):
validate the DESTINATION (Moon does this) AND the direction
(Moon does not - straight `move(slot)` walks into walls); sample
candidates like the tracer (walkable + low-threat + free, halt the
direction that leaves terrain); keep a fallback chain (try next
slot → regroup point → trade, never null); retreat one choke and
rebuild when nothing fits. Steps 1-2 of the adoption order.

### Q&A distilled (owner asked 2026-10-20, answered from the code)

- **"Stardust has no slots (emergent shape)"** means no unit owns a
  place. Every frame each unit sums local vectors (waypoint + away
  from friends + away from walls) and walks the sum. The ball you see
  is nowhere drawn - it emerges like a flock (no bird has a seat, yet
  the flock holds). Consequence: nothing guarantees the shape. Ten
  dragoons retreating through a gap squeeze, string out, two stick
  to a wall - and nobody is "out of position", because positions do
  not exist. The only global rule is weak: Arc pulls units 128+ px
  behind the vanguard back to it. Contrast PurpleWave/Moon: a unit
  off its computed pixel walks back to it.
- **Cohesion is NOT spacing.** Separation = gaps between units (don't
  stack; Moon has this via slot spacing). Cohesion = group integrity:
  don't split in two, don't leave stragglers, arrive together
  (Stardust: pull toward center, weaker in big armies; PurpleWave:
  vanguard + march cadences that wait for the tail). Moon has
  separation without march cohesion: fast units arrive first and
  fight alone while slow ones walk, nobody waits for anybody (the
  gate even requires friends within 3 tiles to apply at all - a lone
  unit simply does something else).
- **Moon "marches straight"** means `ProtossMoon.handle` issues
  `move(slot)` in a straight line: only the DESTINATION is
  validated (walkable + same-or-close region, else leader position),
  never the path. Wall between unit and slot = walk into the wall.

### Prerequisites checklist (what must exist before each phase)

- Phase A needs: leader selection (exists: Alpha/leader), per-class
  reach (exists: dragoon/zealot filters), compass stepping (exists:
  `MoveAway`), walkability probe (exists, stub-pinned), KITE twin
  (exists: stuck/stepped rows = the acceptance instrument).
- Phase B needs: M4 map anchors (choke records, region graph) -
  NOT started, see `redesign/04_MAPLIB.md`.
- Phase C needs: combat sim usable per-battle (FAP exists, cluster
  wiring does not) + the acceptance battery (ZealDrag losses +
  1v1 hits band).

### Phases (dependency order)

- **A (now, no map lib)**: Moon crescent slots + shared escape
  direction + sticky/focus-first ordering. Kills the scatter with
  pure geometry.
- **B (needs M4 anchors)**: zone/edge-aware slots, choke holds
  (Arc at chokes), threat-aware escape paths.
- **C**: full styles + sim-gated transitions. The 1v1 duel stays
  the regression gate throughout; ZealDrag losses are the
  acceptance metric.
