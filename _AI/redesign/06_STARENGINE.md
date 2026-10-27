# StarEngine: own test engine (maximum priority)

Status: **plan, not started.** Owner order 2026-10-27: maximum priority on
StarEngine, full test engine before further legacy deletion. This file is
the owner's working plan. English throughout per CONVENTIONS (no Polish
files, ever).

## 0. Hard constraints (non-negotiable)

- **No verbatim restore of the old StarEngine** (revert `b60a7029`
  stands, `_AI/OPENBW-HANDOFF.md`: "No verbatim StarEngine restore").
  This is a new engine, not a resurrection.
- **OpenBW is frozen** (vehicle: Wine only; #216 hands-off). StarEngine
  does not compete with OpenBW — it replaces what OpenBW was supposed
  to be as a test vehicle, and what Wine is today (slow).
- **Wine is ground truth.** The only real game we have. StarEngine is
  calibrated against Wine logs (`BUILDINGS.json`: warp frames, banks,
  supply) — never the reverse, never from memory.
- **Snapshot = map, never gameplay.** Queues, warps, banks, HP, orders
  are engine runtime state. Storing them would test old replays, not the
  bot. The sim starts from base coordinates + mineral fields and creates
  everything else by playing.

## 1. Goal (measurable)

1. **Opening gate in-suite**: "Zealot into Goon" played from frame 1 to
   50 supply in the sim, asserting: building order == BO, bank <= 550
   before 50 supply (today `scripts/assert-wine-opening.sh` does this on
   real-game logs in ~15 minutes; the sim must do it in seconds).
2. **Unlock legacy deletion**: every WO-144–147 item gets a sim gate
   before the file moves to `src-old/` (today deletion rides on Wine
   games, one game per evening).
3. **Speed over fidelity** (owner decision): a fast engine, not a perfect
   one. Two modes (§5).

## 2. What exists today (inventory)

| Element | Where | State |
|---|---|---|
| Static copy of TauCross | `src/tests/resources/taucross-snapshot.json` | HAVE: 128x128 tiles (`x`/`.`/`w`), 21 regions with neighbours, 21 chokes (center+width+ends+regions), 93 minerals + 9 geysers **with amounts**, 3 starts, 1 preplaced |
| Map loader | `src/tests/unit/sim/SimMap.java` | HAVE: loads snapshot, installs `Regions` + `MapTiles` (walkable/buildable/explored), `minerals()`/`geysers()`/`preplaced()` spawn helpers |
| Dumper (ground truth) | `src/atlantis/map/snapshot/MapSnapshot*.java` | HAVE: `MAP_SNAPSHOT=1` dumps full statics from a live game; same class reads and writes (no schema drift) |
| Fight stepper | `src/tests/unit/sim/SimWorld.java` + `SimArena.java` | HAVE, **combat only**: movement with accel/braking/turn, attack cycles from engine data, damage. Zero economy |
| Fake units | `src/tests/fakes/FakeUnit.java` + `FakeOrderSink.java` | HAVE: positions, HP, orders land in a sink (assertable `build`), **never consumed or validated** |
| World harness | `tests/acceptance/AbstractTestWithWorld.world()` | HAVE: frames, hand-set `currentMinerals`, `options` for supply — **no income exists** |
| Bot seams | `ProductionEngine.updateFrame()`, `ConstructionRequests`, `Construction` (`PREPARING/NOT_STARTED/STARTING/IN_PROGRESS`) | HAVE: V2 plans from bare numbers; `Construction` owns a state machine nothing ticks in stub |
| Batch runner | `scripts/run-sim-batch.sh` | HAVE: Wine-batch contract, but duels only (`1Drag_v_1Zeal`) |

### Snapshot gaps (static only — dynamics are NOT gaps, they are runtime)

- **Base objects**: starts + resources exist, but no "base = start tile +
  its mineral line + gas + natural". Derivable, but derived three
  different ways in three places today — must be stored once.
- **Reachability values**: `SimMap.install()` answers `hasPathBetween`
  true always; stub `distTo` is Euclidean. The region neighbour graph is
  already stored (dumped from `Area.getAccessibleNeighbors()`, i.e.
  walkability connectivity) but never used for queries.
- Starting workers/Nexus: scenario setup (bot side), not map data. The
  snapshot's `units` holds map-preplaced units only.

## 3. How StarCraft validates orders (read-only consult of `/sc-ai/OpenBW`)

Reference: `openbw/bwgame.h`, `state::can_place_building` (mirrors the
original engine) and `state::is_reachable`. Read 2026-10-27, not copied.
Polarity of the unexplored-mask bit to verify against BWAPI docs before
coding (noted below, not assumed).

**Placement (`can_place_building`, per footprint tile + globals):**

1. Footprint in-bounds (tile_pos + tile_size from `placement_size`).
2. Builder reachability: `is_reachable(builder, site)` REQUIRED —
   no path, no building. Exceptions: flying builders, the building
   itself.
3. Refinery special case: footprint must sit on an unexplored geyser
   (`get_building_at_center_position(...) == geyser`).
4. Psi (Protoss, `ut_requires_psionic_matrix`): site inside a friendly
   psi field (`is_in_psionic_matrix`, 256x160px mask around pylons).
5. Creep (Zerg): footprint tiles must have creep (non-Zerg: must NOT).
6. Tile flags: reject `partially_walkable | unbuildable` (+ creep for
   non-Zerg, `unk4`); unexplored-black tiles rejected (mask polarity:
   OpenBW stores the unexplored bit, BWAPI inverts it in `isExplored` —
   verify, do not guess).
7. Occupancy: `flag_occupied` tiles + unit collision inside the inner
   bounding box (flying, own builder and Dark Swarm excluded; addons
   have their own rule).
8. Resources: `canAfford` is a separate order requirement, checked
   before the placement walk, not inside it.

**Reachability (`is_reachable`): region group equality**
(`get_region_at(from)->group_index == get_region_at(to)->group_index`).
Groups are exactly walkability connectivity — our stored neighbour graph
(BWEM accessible neighbours) already encodes them. So `hasPathBetween`
= BFS over the stored region graph, and the all-pairs matrix (§4) makes
it O(1).

**Movement targets** (`find_unit_placement`): move orders do NOT require
reachability at issue time — units walk, slide or stop. Only BUILD
requires the path upfront. The sim must reproduce this split: validate
`build` strictly, accept `move` leniently, resolve the walk on the
ground (BFS on tiles in FAITHFUL, straight segment in FAST).

## 4. Snapshot v2 (static only — the one extension allowed)

Additive fields, loader ignores unknown fields (forward compatible):

- `bases`: `[{x, y, startIndex, mineralIds:[], gasIds:[]}]` — base tile
  + its mineral line + gas, dumped from the live game (the dumper sees
  all starts and all resources; assignment = nearest line to the start,
  ties broken by region). Rationale: one stored answer instead of three
  derived ones.
- `regionReach`: all-pairs region reachability matrix (21x21 booleans
  on TauCross — tiny), dumped from live BWEM connectivity, verified
  equal to BFS-over-neighbours at load. This is the stored
  "hasPathBetween-type value": O(1) queries, no per-test BFS.
- Nothing else. No queues, no warps, no banks, no HP, no orders, no
  worker positions — all of that is created by playing (§0).

Dumper work: bases + reach matrix from the live world; loader work:
parse + verify (matrix == BFS); golden test extended (3 starts, minerals
per base with amounts, start↔start connectivity through chokes).

## 5. Two modes (owner decision)

| | FAST (approximate) | FAITHFUL (1:1) |
|---|---|---|
| Builder travel | straight segment, constant speed | SimWorld mover (accel/braking/turn) |
| Placement check | flag: accept-all OR check | full check (§3 rules 1–8) |
| Mining | flat income per miner per frame | trips: travel to patch + 8 minerals (calibrated §7) |
| Combat | none (build tests) | SimWorld as today |
| Use | opening gate, deletions, suite | placement/travel disputes, "why warp f3063 not f2307" debug |

One `SimConfig { mode, seed, scenario }`, one codebase, divergence in
exactly three points (movement/mining/placement-check). Two separate
engines = two corpses to maintain.

## 6. Architecture rework (the step back — SimWorld was scaffolding)

Findings on the current code (SRP/OCP read 2026-10-27):

- `SimWorld` is a god class: tick loop + movement physics + combat
  resolution + `BaseSelect` mocking + cache clearing + assertion
  readouts. Adding economy means editing it — open for modification,
  closed for nothing.
- Order dispatch is stringly-typed (`"Move".equals(lastCommand)`,
  `"AttackUnit"`): every new verb (gather/build/train) is another
  branch in `desiredVelocity`. This is where validation must live and
  does not.
- Two walkability authorities that can disagree: `SimArena` rectangles
  (geometric) vs `MapTiles` tile grid. Terrain added in one is missing
  in the other.
- `SimMap` mixes loader + global-source installer + spawners; install/
  uninstall mutate globals, order-dependent with the harness mocks.
- `FakeUnit` fields are public mutable; bot and sim poke the same
  fields with no contract. The sink records orders nobody consumes.

Target layers (new concepts; production bot code untouched, sim is
another "world" of the harness, never a fork):

```
Ground  (static, from snapshot v2: tiles, regions, bases, reach matrix)
State   (dynamic: units, bank, constructions — owned, encapsulated)
Rules   (pure: OrderValidator per verb from §3, movement, mining, combat)
Driver  (tick loop calling real bot code through existing seams only)
Scenarios (setup + scripted enemy + assertions; the 50-supply test first)
```

Key new concept: **`SimOrder` as a value with a validation verdict**
(verb + args + ACCEPTED / REJECTED + reason), replacing
stringly-typed `lastCommand`. New verb = new `OrderValidator`, driver
untouched (OCP). Rejection reasons are first-class test output
("build refused: footprint tile [x,y] not buildable" reads like the
engine, not like a null).

Bot entry stays through existing seams only (`ProductionEngine`,
`ConstructionRequests`, `Select/Count/A`) — zero `if (isSim)` in
production code.

## 7. Calibration: numbers and their sources (never from memory)

| Number | Source |
|---|---|
| Prices, build/train times, supply, prerequisites, placement sizes | `bwapi.UnitType` via `AUnitType` (already in the bot, CONVENTIONS §9) |
| Income per miner, bank-vs-supply curve | `BUILDINGS.json` from Wine runs (bank at every warp) — 3 runs banked |
| Reference warp frames | same + `assert-wine-opening.sh` PASS as the golden result |
| Speeds, ranges, damage | binding, as SimWorld does today (`UnitStatsTable`) |
| Windup/accel/braking, mining trip overhead | model params as today (WINDUP in SimWorld), pinned by tests |

Parity: scenario "Zealot into Goon to 50 supply" replayed in the sim
must reproduce Wine warp frames within tolerance (proposal: ±10% or
±150 frames — owner decision in §10). Over-tolerance drift = sim bug OR
bot bug; both answers are valuable.

## 8. Phases with gates (each lands green or does not land)

- **P0 Frozen map (1–2 days)**: snapshot v2 (bases, reach matrix),
  golden test extended (3 starts, minerals per base with amounts,
  start↔start connectivity through chokes, matrix == BFS). Gate:
  golden unchanged by refactors.
- **P1 FAST economy (one week)**: clock+bank+orders+constructions in
  FAST, no enemy, placement check on. Scenario BO-to-50: order + bank
  assertions (in-suite twin of `assert-wine-opening`). Gate: green in
  `run-full-tests.sh` under 30 s; Wine parity inside §7 tolerance.
- **P2 FAITHFUL movement+placement (one week)**: mover, builder travel,
  cancels, placement check default. Gate: scenario reproduces the
  proximity-cancel storm shape + Gateway warp inside parity tolerance.
- **P3 Scripted enemy**: scout at frame N, 4-ling runby at frame M
  (the 115321 death shape). Gate: army answers, base survives the
  f4000 ling-flood scenario.
- **P4 Deletion hookup**: every WO-144–147 file gets a sim gate BEFORE
  `git mv src-old/`. Gate: no deletion lands on a red sim.
- **P5 Upkeep**: golden warp frames in repo; over-tolerance drift
  blocks commits (like arch tests).

## 9. What the sim unlocks (mapping)

- WO-144 (supply): "zero blocks 17/25 + banks" gate in sim, not Wine.
- WO-145 (placement): catalogue-vs-legacy A/B in sim in seconds
  (placement check in both modes).
- WO-146 (`APositionFinder`): "zero `Can't find place`" gate in sim.
- WO-147 (`Queue/**`): sim plays BO on V2 only — proof the queue is dead.
- NEXT: Forge/Cannon authors (port of `ForgeGoal`+`CannonGoal`), the
  orphan-prerequisite in the scheduler, stance-from-focus — each with a
  sim pin.

## 10. Risks (named so they do not return)

- **Second OpenBW**: an engine bigger than needed. Cure: §4-equivalent
  scope ban in this file + review of every "just one more X" against
  phases.
- **Fidelity trap**: chasing 1:1 forever. Cure: tolerances in §7, FAST
  is the suite default.
- **Wine drift**: a sim without parity lies. Cure: P5 golden frames.
- **Tests that test nothing**: a passing scenario because
  placement=accept-all and enemy=none. Cure: accept-all behind a flag
  only, check is default; P3 mandatory before P4.
- **Global statics**: `Regions.useSource` etc. are globals — the sim
  MUST clean up (`uninstall`), or it poisons neighbouring tests.

## 11. Owner decisions needed before P1

1. Warp-frame parity tolerance: ±10% / ±150 frames / other?
2. Enemy in P1: strictly none, or a scripted scout (affects the BO)?
3. Suite budget for the opening gate: hard <30 s, or may it cost more?
4. May the sim EVER replace Wine for WO-145 A/B, or only precede it
   (sim fast, Wine last)?
5. (Answered 2026-10-27: docs in English always — recorded, this file
   rewritten accordingly.)
