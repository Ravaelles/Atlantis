# Architecture Comparison: Stardust vs. Atlantis — Why Stardust Avoids Chronic State Issues

Comparative architectural study based on code analysis of both projects (October 2026).
Objective: Define which components of Atlantis should be pruned and replaced with Stardust patterns.

## TL;DR

Stardust does not have "fewer decision layers" — it uses a fundamentally **different decision geometry**:

- **Atlantis**: Decisions are made **per individual unit** (a chain of Managers sequentially evaluating each unit, where any Manager can intercept execution), and units belong to **static squads** (`Alpha`, `Bravo`, `Omega`...). Production relies on a **`ProductionOrder` Queue** (interleaving static build orders and dynamic requests) queried frame-by-frame, while buildings are treated as an isolated `Construction` subsystem with its own volatile lifecycle.
- **Stardust**: Decisions are made **per tactical group** (`Play` → `Squad` → `UnitCluster`), while units are **dynamically assigned** to spatial clusters each frame based on proximity thresholds. Production is a **unified forward-looking schedule**: `Producer` projects economic resources (`minerals`, `gas`, `supply` across an upcoming frame window) and atomically commits prioritized goals. There is no persistent "order queue" and no separate lifecycle manager for buildings.

---

## 1. Combat Unit Management

### Atlantis (`combat/`, ~60 classes)

- **Top-Down Execution per Frame**: `FramePipeline` → `CombatCommander` → `MissionCommander` + `SquadsCommander` → `ActWithSquadsCommander` → for **every unit**: `(new CombatUnitManager(unit)).invokeFrom(this)` → chain of Managers (`ProtossCombatManagerTopPriority` contains **over 30 Managers** in a single sequence).
- **Static Squad Topology**: Units are assigned once upon creation (`NewUnitsToAssigner`: air → `Delta`, DT → `X`, ground army → `Alpha`) and rarely transition across squads outside manual transfer routines.
- **Per-Unit Responsibility Chain**: `Manager.handle()` stops at the first non-null/handled action. **Array ordering acts as implicit business logic**. Conflicts between competing behaviors are resolved via list priority, leading over time to brittle emergent bugs and "priority spaghetti".
- **Targeting & Retreating**: Scattered across disparate subsystems (`SquadTargeting`, individual micro classes, `combat/retreating`, `combat/running`, focus points).

### Stardust (`General/`, ~20 classes)

- **Play → Squad → UnitCluster**: `Strategist` maintains active strategic **Plays** (`MainArmyPlay`, `DefensivePlay`, `Scouting`, `SpecialTeams`). Plays allocate Squads; Squads form dynamic **UnitClusters**.
- **Dynamic Spatial Clustering**: `Squad::updateClusters()` recomputes memberships every frame:
  - Units join the nearest cluster vanguard (`ADD_THRESHOLD = 480`).
  - Nearby clusters merge (`COMBINE_THRESHOLD = 480`, adjusted for ball/line radii).
  - Outliers drop off (`REMOVE_THRESHOLD = 600`).
  - Spatial geometry self-heals continuously without manual transfers.
- **Decisions at Cluster Level, Not Unit Level**: A cluster holds an `Activity` (`Moving`, `Attacking`, `Regrouping`) and a `SubActivity` (`ContainStaticDefense`, `ContainChoke`, `StandGround`, `Flee`, `AttackBlockingArmy`). Units receive orders from their cluster context. Per-unit classes (`MyDragoon`, `MyCorsair`) only handle **kinematic execution** (when to fire, spacing, unsticking), never strategic **intent**.
- **Combat Simulation as Ground-Truth Arbiter**: `runCombatSim` (FAP) gates `Attack` vs. `Regroup`. Simulation outputs are buffered (`recentSimResults`, `consecutiveSimResults`) requiring multiple stable frames to transition, eliminating 1-frame decision oscillations.
- **Integrated Game Engine**: Stardust embeds a fork of `bwgame` (complete SC:BW physics and logic simulation in C++), enabling precise forward projections of movement and cooldowns.

**Conclusion 1**: What Atlantis attempted to solve with extensive per-unit decision spaces is solved in Stardust by **group-level consensus and a single unified state per cluster**.
Recommended migration: Remove per-unit Manager chains. Introduce dynamic spatial clusters with explicit `Activity` and `SubActivity` states.

---

## 2. Production and Construction

### Atlantis

- **Three Disconnected Sources of Truth**: Static build order files (`CurrentBuildOrder`), dynamic requests (`DynamicProductionCommander`, `ProtossRequests`), and the mutable `Queue` storing `ProductionOrder` objects with volatile state flags (`NOT_READY`, `READY_TO_PRODUCE`, `IN_PROGRESS`, `FINISHED`).
- **Reactive Patchwork**: Required auxiliary patch classes: `QueueRefresher`, `PreventDuplicateOrders`, `RemoveExcessiveOrders`, and try-catch safety nets in `ProductionOrderHandler`.
- **Buildings as an Autonomous Subsystem**: `ProductionOrder` → `ProduceBuilding` → creates a `Construction` domain object managed by `ConstructionsCommander` with 5 recovery sub-commanders (`ConstructionStatusChanger`, `ConstructionThatLooksBugged`, `IdleBuildersFix`...).
- **Uncoordinated Budgeting**: Multiple components independently check `A.canAfford()`. Resource reservations are split across `ReservedResources` and `OrderReservations`.

### Stardust

- **Single Source of Truth**: `Strategist` aggregates prioritized `ProductionGoal` records directly from active Plays (immutable tuple: requester, type, count, producer limit, location constraint, earliest frame). No persistent order queue exists.
- **Forward Resource Timeline (`Producer::update()`)**:
  1. Initializes timeline projection arrays: `minerals[]`, `gas[]`, `supply[]` over `PREDICT_FRAMES` (2,000–4,500 frames) using real worker gathering rates.
  2. For each goal in priority order:
     - Recursively injects missing technology prerequisites (`addMissingPrerequisites`).
     - Normalizes timeline offsets and dedupes items (`resolveDuplicates`).
     - Gathers available producers (completed buildings + planned items).
     - Solves resource constraints (`resolveResourceBlocks`): shifts items forward along the timeline if funds are insufficient, without discarding them.
     - Performs tentative placement reservations (`reserveBuildPositions`).
  3. Atomically commits resources on the timeline when constraints are satisfied.
  4. Dispatches BWAPI commands (`train`, `build`) **only when `startFrame <= latencyFrames`**.
- **Buildings are Simply Scheduled Items**: A building is just a `ProductionItem` with a `buildLocation` and a builder assignment. If a builder dies, the next frame's recalculation naturally reassigns a new worker. No multi-class construction lifecycle manager is required.

**Conclusion 2**: Production queue corruption, double orders, and stalled constructions stem from **multiple unsynchronized state managers**. Stardust avoids this through **ephemeral, stateless timeline simulation recalculated every frame**. This core algorithm is pure deterministic scheduling and can be ported directly to Java 1.8.

---

## 3. High-Level Comparison Matrix

| Architectural Area | Atlantis (Java 1.8) | Stardust (C++) |
|---|---|---|
| **Unit Representation** | Heavy `AUnit` wrapper + mutable `UnitState` + distributed caches | Lightweight `UnitImpl` / `MyUnitImpl` + explicit simulation fields |
| **Enemy Intelligence** | Global collections (`EnemyUnits`) | Per-unit fog tracking, `simPosition`, predicted attack impacts |
| **Game Interface** | Pure BWAPI client wrapper | Hybrid: BWAPI compatibility layer running on OpenBW / bwgame |
| **Testing Strategy** | Unit testing with JUnit + ArchUnit architecture rules | Unit tests + high-speed headless in-process OpenBW runs |
| **Visualization & Debug** | CherryVis integration | Native CherryVis instrumentation |

---

## 4. Pragmatic Action Plan

1. **Prune**:
   - Per-unit manager chains (`combat/micro/**`, `combat/managers/**`).
   - Static squad registries (`combat/squad/squads/**`).
   - Three-valued logic `Decision` (`TRUE`/`ALLOWED`/`INDIFFERENT`/`FORBIDDEN`/`FALSE`).
   - Production queue subsystem (`production/orders/production/queue/**`, `production/constructions/**`).
2. **Implement (Production First)**:
   - Port `ResourceTimeline`, `ProductionScheduler`, `ProductionGoal`, and `PlacementPlanner` to clean, testable Java 1.8 classes.
3. **Implement (Combat Second)**:
   - Port `Play` → `Squad` → `UnitCluster` with dynamic spatial clustering.
   - Separate tactical intent (`ClusterCommander`) from kinematic micro execution (`UnitOrderExecutors` + profile behaviors).
   - Gate transitions with FAP / buffered combat simulation.
4. **Retain**:
   - Sound foundational components: `information/**`, `map/**` (BWEM/JBWeb integration), worker mining management, ArchUnit architecture assertions, and CherryVis debug tooling.
