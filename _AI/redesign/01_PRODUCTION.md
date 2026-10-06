# Track 1: Production and Construction — Porting Stardust's Architecture to Atlantis

> Priority: **FIRST TRACK** (higher return on investment, fewer real-time combat micro dependencies).
> Sources: `/sc-ai/Stardust/src/Producer/**`, `/sc-ai/Stardust/src/Strategist/**`,
> `/sc-ai/Stardust/src/Builder/**`; Atlantis: `src/atlantis/production/**`.

## Current State Diagnosis (Atlantis)

### Three Sources of Truth for Production (Root Cause)

1. **Static Build Order Files** — `production/orders/build/*` (`CurrentBuildOrder`, `ProtossBuildOrder`...), supply-gated text rows parsed into `ProductionOrder`.
2. **Dynamic Requests** — `production/dynamic/*` (`DynamicProductionCommander` → `AutoProduceWorkersCommander`, `DynamicUnitAndTechProducerCommander`, `DynamicBuildingsCommander`, `ExpansionCommander`, supply, tech) — command layers inserting ad-hoc `ProductionOrder`s into the queue at runtime.
3. **The Mutable `Queue`** — `production/orders/production/queue/**`: stateful list of `ProductionOrder` entities with lifecycle flags (`NOT_READY`, `READY_TO_PRODUCE`, `IN_PROGRESS`, `FINISHED`). Synchronized via `QueueRefresher`, deduped via `PreventDuplicateOrders`, while `ReservedResources` attempts manual mineral tracking.

**Emergent Defect Mitigations**:
`RemoveExcessiveOrders`, `PreventDuplicateOrders`, `ProductionOrderHandler` wrapping every order in broad catch blocks ("Problem with ... EXCEPTION", ~5,000 caught stack traces per game per source comments), `isAlreadyConsumed` workarounds, `Construction.isOverdue`, `ConstructionThatLooksBugged`.

### Construction Subsystem Isolation

Flow: `ProductionOrder` → `ProduceBuilding` → creates a domain `Construction` object (builder, tile, status, back-reference to `ProductionOrder`).
Lifecycle managed by `ConstructionsCommander` with 5 healing sub-commanders: `ConstructionStatusChanger`, `ConstructionUnderAttack`, `ConstructionThatLooksBugged`, `IdleBuildersFix`, `TerranKilledBuilderCommander` + 12 builder selection classes.
Tile search: `APositionFinder.findPositionForNew` (57-frame cache), reactive cancellation if enemies approach.

### Symptoms and Failure Modes

- Orders duplicate or vanish across state transitions (necessitating `PreventDuplicateOrders`).
- Fragmented budgeting: `OrderReservations` + `ReservedResources` + scattered `A.canAfford` calls → execution order reflects iteration loop sequence, not strategic priority.
- No temporal dimension: An order is either strictly affordable now or skipped. There is no concept of *"this Dragoon is delayed by 12 frames because we must build a Pylon first"*.

---

## Target Model (Stardust)

### Core Structure

- **`ProductionGoal`** — Plain value object (immutable frame snapshot): `requester`, type (unit/building/upgrade/tech), `count` (-1 for continuous production), `producerLimit`, `location` (neighbourhood or specific tile), `reservedBuilder`, `desiredStartFrame`.
- **`Strategist`**: Aggregates prioritized goals from active **Plays** each frame (`Map<Integer, List<ProductionGoal>>` keyed by priority band) and flattens them into a deterministic sequence.
- **`Producer::update()` — Single Planning Engine**:
  1. `initializeResources()`: Projects `minerals[]`, `gas[]`, `supply[]` across `PREDICT_FRAMES` (2,000–4,500 frames) starting from current BWAPI values and gathering rates.
  2. For each goal sequentially (priority determines scheduling precedence): `handleGoal`:
     - Buildings: If an identical `(type, tile)` is already committed → skip.
     - `addMissingPrerequisites`: Recursively inserts prerequisite buildings (e.g., Dragoon without Cybernetics Core inserts Cybernetics Core with appropriate backward time shift).
     - `resolveDuplicates`: Resolves overlaps, preserving the earliest item.
     - `reserveBuildPositions(items, commit=false)`: Tentative building slot reservation.
     - Selects producers: existing completed + planned buildings.
     - `resolveResourceBlocks(item, prerequisites, commit)`: Scans timeline for funds; shifts items forward if resources are temporarily exhausted (`shiftOne`/`shiftAll`). Atomically deducts resources on `commit=true`.
  3. Pull-forward adjustments: `pullRefineries`, `pullSupplyProviders` move gas/pylons earlier if mineral banks permit.
  4. Emergency rules: Auto-queues Pylons/Gateways when supply is maxed.
  5. **Deferred Execution**: Real BWAPI commands are dispatched **only** when `startFrame <= latencyFrames`.
- **Buildings are Simply Scheduled Items**: A building is just a `ProductionItem` with a planned tile and builder assignment. Builder failures are resolved naturally on the subsequent frame's recalculation.

### Architectural Invariants

- **Stateless Recomputation**: The production plan is recreated from scratch every frame. No order state persists across frames.
- **Precedence by Sequence, Fairness by Time-Shifting**: High-priority items claim resources first; lower-priority items are pushed forward in time rather than discarded.
- **Atomic Timeline Deductions**: Resources are reserved once per pass across the forward timeline.

---

## Implementation Plan (Atlantis Java 1.8)

1. **Domain Types** (Pure logic, zero BWAPI dependencies): `ProductionGoal`, `ProductionItem`, `ProducerFacility`, `TargetPlacement`, `ResourceTimeline`.
2. **Scheduling Engine**: `ProductionScheduler` + `ResourceTimeline` with forward shifting (`findEarliestAffordableFrame`, `allocate`). Unit-tested with mock game states.
3. **Placement Planning**: `PlacementPlanner` leveraging existing `APositionFinder` / BWEM initially; Block templates later.
4. **Execution Layer**: Replace `ProduceOrdersFromQueue` with `ProductionDispatcher` (issuing `train` and `Builder.build` in latency window).
5. **Phase-Out**: Deprecate and remove `Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`, `Construction/**` recovery commanders.
6. **Build Order Compatibility**: Existing `.txt` build orders act purely as Goal generators (`BuildOrderGoals`) without porting the legacy queue mechanism.

---

## Technical Blueprint: Classes and Interfaces (Java 1.8)

Derived from comprehensive analysis of `Producer.cpp` (2,400+ lines), `Strategist`/`Play`/`StrategyEngine`, and `Builder`.
Guiding Principle: **Production state is computed statelessly from scratch every single frame** — zero mutable order states persist across frames.

### Target Package: `atlantis.production.v2`

**Data Model (Immutable Value Objects):**

- `ProductionGoal`: Strategy input containing `requester:String`, `item:Producible`, `count:int` (-1 = continuous production), `producerLimit:int`, `placement:TargetPlacement`, `reservedBuilder:AUnit`, `desiredStartFrame:int`, `priority:int`.
- `TargetPlacement`: Spatial constraint abstraction (`Anywhere`, `Neighbourhood`, `BaseLocation`, `ExactTile`).
- `ProductionItem`: Internal planning node holding `item:Producible`, `startFrame:int`, `completionFrame:int`, `producer:ProducerFacility`, `isPrerequisite:boolean`, `placementReservation:PlacementReservation`, `estimatedWorkerMovementTime:int`. Cost methods: `mineralPrice()` (incorporates worker opportunity cost: `2.0 * workerMovementTime * MINERALS_PER_WORKER_FRAME`), `gasPrice()`, `supplyProvided()`, `supplyRequired()`.
- `ProducerFacility`: Represents production capacity (`existingUnit:AUnit` or `plannedBuilding:ProductionItem`); tracks `availableFromFrame:int` and assigned `scheduledItems`.
- `PredictedResources` (Core Engine Timeline): Projects `minerals[]`, `gas[]`, `supply[]`, `totalSupply[]` across `PREDICT_FRAMES` (2,000–4,500 frames depending on game stage) + `framesWithReassignableMineralWorker:TreeMultiset<Integer>`.
  Methods: `spend(amount, fromFrame)`, `addProvidedSupply(amount, frame)`, `updateIncome(fromFrame, workerDelta, rate)`, `frameWhenAffordable(cost, startFrame, extraStops)`.

**Engine Components:**

- `ProducerEngine.update(goals:List<ProductionGoal>)` — Core pipeline execution:
  1. `initializeResources()`: Seeds timeline with current BWAPI stocks; extrapolates income (`rate * workers`); accounts for completing workers; **assimilates pending buildings from Builder as pre-committed timeline deductions**.
  2. Evaluates each goal in strict priority order via `handleGoal(goal)`.
  3. Executes pull-forward heuristics (`pullRefineries`, `pullSupplyProviders`) to advance gas and supply structures if mineral margins allow.
  4. (Protoss) At max supply: Dynamically schedules emergency Pylons and Gateways.
  5. `issueOrders()`: Dispatches concrete game commands only at execution horizons.
- `ProducerEngine.handleGoal(goal)`:
  - Deduplication: If an identical `(type, tile)` is already committed, skip; unlocated Pylons can claim specific requested coordinates.
  - `collectPrerequisites(type)`: Recursively builds `PrerequisiteSet` from unit requirements, inserting placeholders for structures under active construction.
  - `normalizeStartFrames(prerequisites)`: Converts relative dependency offsets to absolute timeline coordinates.
  - `resolveDuplicates(prerequisites)`: Retains earliest candidates, cross-checks committed items, returns `prerequisitesAvailableFrame`.
  - `reserveBuildPositions(prerequisites, commit=false)`: Tentative spatial validation.
  - `collectProducers(producerType, location)`: Aggregates ready and upcoming facilities.
  - Commitment loop: Pairs `chooseProducer()` with `resolveResourceBlocks(item, prerequisites, commit)` until count is met or timeline runs out of funds.
  - Fallback safety: If an item cannot be produced in the horizon but has unbuilt prerequisites, commit the first prerequisite to advance the tech tree.
- `ResourceSolver`: Solves temporal resource blockages (`shiftForMinerals`, `shiftForGas`, `shiftForSupply`) using backward horizon scans with multi-step cost checkpoints ("frame stops"). Special handling: Self-paying worker amortization; rolling gas cost into minerals when tech requires Cybernetics Core before Assimilator.
- `BuildPositionResolver`: Assigns building locations from neighbourhood tiles by size (`[neighbourhood][tileWidth]`), scores Pylon placements by powered tiles unlocked, and handles building unpowered-delay shifts.
- `OrderIssuer`: Dedicated gateway to BWAPI commands. Issues `train` commands when `item.startFrame <= getRemainingLatencyFrames()`; issues builder construction when `arrivalFrame - currentFrame >= startFrame`; detects recently sent commands via `unit.getLastCommand() == Train` to avoid duplicate dispatches.

**Goal Generation Sources:**

- `BuildOrderGoals`: Translates existing text build orders into sorted `ProductionGoal` lists (priority mapped from line order) without requiring the legacy queue.
- `DynamicGoals`: Replaces old Dynamic Commanders with pure goal generators: `WorkerGoals`, `SupplyGoals`, `ExpansionGoals`, `TechGoals`, `ArmyGoals`. Each implements `contribute(goals:SortedMap<Integer,List<ProductionGoal>>)`.
- `Play.contributeProductionGoals(...)`: Integrated with strategic Plays using standardized priority bands (`PRIORITY_EMERGENCY`, `PRIORITY_WORKERS`, `PRIORITY_DEPOTS`, etc.).

**Implementation Milestones:**

1. `PredictedResources` + unit tests (pure deterministic logic, zero BWAPI dependencies).
2. `ProductionItem` / `ProductionGoal` / `ProducerFacility` + `ResourceSolver`.
3. `ProducerEngine` for units and upgrades in dry-run mode (running in parallel with the legacy system, emitting comparison logs).
4. `BuildPositionResolver` integrating building placement.
5. `OrderIssuer` activation and live feature flag cutover.
6. Full cleanup: Delete `Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`, `Construction/**`.

### Architectural Invariants (Enforced via ArchUnit and Unit Tests)

- **Solvency**: Projected balances in `PredictedResources` must never drop below zero at any frame across the timeline.
- **Single-Frame Uniqueness**: A single goal cannot produce duplicate `ProductionItem` dispatches within the same frame.
- **Monotonicity**: Committed items in the active plan must maintain monotonic start frame ordering.
- **Execution Timeliness**: Every committed item with `startFrame <= latencyFrames` must trigger an order dispatch unless blocked by builder unavailability.

## Production Architecture v2 (Code Review & Clean Design)

### 1. Architectural Code Review (Senior Engineer Perspective)

An architectural critique of the naive port reveals 4 distinct code smells that would replicate Atlantis's legacy pitfalls:

1. **SRP Violation in `ProducerEngine` (God Class Trap):**
   In Stardust's C++, `Producer.cpp` is a 2,400-line monolith mutating anonymous namespace globals. In Java, packing wallet initialization, mining rate projections, technology tree recursion, building placement, constraint solving, and BWAPI order dispatch into `ProducerEngine` would create an unmaintainable God object.
   *Resolution:* Decouple into 4 autonomous layers: **Timeline Simulation** (`ResourceTimeline`), **Solver/Scheduler** (`ProductionScheduler`), **Spatial Planner** (`PlacementPlanner`), and **Execution Dispatcher** (`ProductionDispatcher`).

2. **OCP Violation in Goal Handling & Producible Types:**
   Using raw unions or type checking (`if (type.isBuilding())`) in the planning loop prevents clean extensions. Zerg produces from Larvae (no dedicated facility per unit), Terran attaches Addons, Protoss requires Psi coverage.
   *Resolution:* **Command / Recipe Pattern (`Producible`)**. Every producible item implements a clean contract encapsulating its own cost, prerequisites, facility requirements, and racial mechanics.

3. **BWAPI Leakage into Computational Core (DIP Violation):**
   `ResourceTimeline` and the solver must not depend directly on `BWAPI::Broodwar` or concrete `AUnit` objects. The scheduling algorithm must be a pure mathematical function of time, slots, and resources, 100% deterministically testable in JUnit without launching Brood War.

4. **Temporal Ambiguity (Lack of Reified Game Time):**
   Conflating relative frame offsets ("in 45 frames") with absolute game frames ("at frame 1420") causes persistent off-by-one errors.
   *Resolution:* All timeline operations use absolute game frames (`GameFrame` integers).

---

### 2. Class and Flow Diagram (Mermaid)

```mermaid
classDiagram
    direction TB

    package "1. Declarative Goal Layer (What We Want)" {
        class ProductionGoal {
            -Producible item
            -int priority
            -int count
            -int maxConcurrentProducers
            -TargetPlacement placementConstraint
            +isSatisfied(GameStateSnapshot state) boolean
        }
        class TargetPlacement {
            <<abstract>>
            +anywhere()$ TargetPlacement
            +inNeighbourhood(Neighbourhood)$ TargetPlacement
            +exactTile(TilePosition)$ TargetPlacement
        }
        class Producible {
            <<interface>>
            +ResourceCost cost()
            +Duration buildDuration()
            +List~Producible~ prerequisites()
            +boolean canBeProducedBy(ProducerFacility facility)
            +boolean requiresPlacement()
        }
        class UnitProducible {
            -AUnitType unitType
        }
        class TechProducible {
            -TechType techType
        }
        class UpgradeProducible {
            -UpgradeType upgradeType
        }
        Producible <|.. UnitProducible
        Producible <|.. TechProducible
        Producible <|.. UpgradeProducible
        ProductionGoal --> Producible
        ProductionGoal --> TargetPlacement
    }

    package "2. Deterministic Wallet Layer (What We Can Afford)" {
        class ResourceTimeline {
            -int[] minerals
            -int[] gas
            -int[] availableSupply
            -int[] totalSupply
            +boolean canAfford(ResourceCost cost, int frame)
            +int findEarliestAffordableFrame(ResourceCost cost, int afterFrame)
            +void allocate(ResourceCost cost, int atFrame)
            +void addMiningIncome(int fromFrame, double mineralRate, double gasRate)
        }
        class ResourceCost {
            +int minerals
            +int gas
            +int supply
            +int builderOpportunityCost
        }
        ResourceTimeline ..> ResourceCost
    }

    package "3. Solver & Scheduler Layer (When and Who)" {
        class ProductionScheduler {
            -PlacementPlanner placementPlanner
            -ProducerFacilityRegistry facilityRegistry
            +ProductionPlan schedule(List~ProductionGoal~ sortedGoals, ResourceTimeline timeline)
        }
        class PlacementPlanner {
            <<interface>>
            +PlacementReservation reservePlacement(Producible building, TargetPlacement placement, int targetFrame)
        }
        class ProductionPlan {
            -List~ScheduledScheduleItem~ scheduledItems
            +List~ScheduledScheduleItem~ itemsDueBefore(int frameThreshold)
        }
        class ScheduledScheduleItem {
            -Producible item
            -ProducerFacility assignedProducer
            -PlacementReservation placement
            -int startFrame
            -int completionFrame
        }
        ProductionScheduler ..> ResourceTimeline
        ProductionScheduler --> PlacementPlanner
        ProductionScheduler --> ProductionPlan
        ProductionPlan *-- ScheduledScheduleItem
    }

    package "4. Execution Layer (BWAPI Dispatch)" {
        class ProductionDispatcher {
            -BuildingOrderDirector buildingDirector
            +void dispatch(ProductionPlan plan, int currentFrame, int latencyFrames)
        }
        class BuildingOrderDirector {
            +void dispatchBuilder(PlacementReservation placement, Producible building)
        }
        ProductionDispatcher --> BuildingOrderDirector
    }

    ProductionGoal ..> ProductionScheduler : Input
    ProductionScheduler ..> ProductionDispatcher : Output Plan
```

---

### 3. Layer Contracts and Skeleton Design (Java 1.8)

#### Layer 1: Declarative Goal & Recipe Contracts

```java
public final class ResourceCost {
    private final int minerals;
    private final int gas;
    private final int supply;

    public ResourceCost(int minerals, int gas, int supply) {
        this.minerals = minerals;
        this.gas = gas;
        this.supply = supply;
    }
    public int minerals() { return minerals; }
    public int gas() { return gas; }
    public int supply() { return supply; }
}

public interface Producible {
    String id();
    ResourceCost cost();
    int buildDurationFrames();
    List<Producible> immediatePrerequisites();
    boolean canBeProducedBy(ProducerFacility facility);
    boolean requiresPlacement();
}

public final class ProductionGoal implements Comparable<ProductionGoal> {
    private final Producible item;
    private final int priority;              // Lower integer = higher priority
    private final int count;                 // -1 = infinite / continuous
    private final int targetStartFrame;
    private final TargetPlacement placement;

    public ProductionGoal(Producible item, int priority, int count, int targetStartFrame, TargetPlacement placement) {
        this.item = item;
        this.priority = priority;
        this.count = count;
        this.targetStartFrame = targetStartFrame;
        this.placement = placement;
    }
    public Producible item() { return item; }
    public int priority() { return priority; }
    public int count() { return count; }
    public int targetStartFrame() { return targetStartFrame; }
    public TargetPlacement placement() { return placement; }

    @Override
    public int compareTo(ProductionGoal other) {
        return Integer.compare(this.priority, other.priority);
    }
}
```

#### Layer 2: Timeline Simulator

```java
public final class ResourceTimeline {
    private final int[] minerals;
    private final int[] gas;
    private final int[] supplyAvailable;
    private final int horizonFrames;

    public ResourceTimeline(int horizonFrames, int startMinerals, int startGas, int startSupplyAvail) {
        this.horizonFrames = horizonFrames;
        this.minerals = new int[horizonFrames];
        this.gas = new int[horizonFrames];
        this.supplyAvailable = new int[horizonFrames];
        Arrays.fill(this.minerals, startMinerals);
        Arrays.fill(this.gas, startGas);
        Arrays.fill(this.supplyAvailable, startSupplyAvail);
    }

    public void addMiningIncome(int fromFrame, double mineralRatePerFrame, double gasRatePerFrame) {
        double currentM = 0;
        double currentG = 0;
        for (int f = fromFrame; f < horizonFrames; f++) {
            currentM += mineralRatePerFrame;
            currentG += gasRatePerFrame;
            minerals[f] += (int) currentM;
            gas[f] += (int) currentG;
        }
    }

    public int findEarliestAffordableFrame(ResourceCost cost, int afterFrame) {
        for (int f = Math.max(0, afterFrame); f < horizonFrames; f++) {
            if (minerals[f] >= cost.minerals() && gas[f] >= cost.gas() && supplyAvailable[f] >= cost.supply()) {
                return f;
            }
        }
        return -1; // Unaffordable within horizon
    }

    public void allocate(ResourceCost cost, int atFrame) {
        for (int f = atFrame; f < horizonFrames; f++) {
            minerals[f] -= cost.minerals();
            gas[f] -= cost.gas();
            supplyAvailable[f] -= cost.supply();
        }
    }
}
```

#### Layer 3: Production Scheduler

```java
public final class ProductionScheduler {
    private final PlacementPlanner placementPlanner;
    private final ProducerFacilityRegistry facilityRegistry;

    public ProductionScheduler(PlacementPlanner placementPlanner, ProducerFacilityRegistry facilityRegistry) {
        this.placementPlanner = placementPlanner;
        this.facilityRegistry = facilityRegistry;
    }

    public ProductionPlan schedule(List<ProductionGoal> goals, ResourceTimeline timeline) {
        ProductionPlan plan = new ProductionPlan();
        Collections.sort(goals);

        for (ProductionGoal goal : goals) {
            scheduleGoal(goal, timeline, plan);
        }
        return plan;
    }

    private void scheduleGoal(ProductionGoal goal, ResourceTimeline timeline, ProductionPlan plan) {
        Producible item = goal.item();
        List<Producible> missingPre = resolveMissingPrerequisites(item);
        for (Producible pre : missingPre) {
            scheduleItem(pre, timeline, plan, TargetPlacement.anywhere());
        }
        scheduleItem(item, timeline, plan, goal.placement());
    }

    private void scheduleItem(Producible item, ResourceTimeline timeline, ProductionPlan plan, TargetPlacement placementConstraint) {
        ProducerFacility facility = facilityRegistry.findEarliestAvailable(item);
        int earliestProducerReady = (facility != null) ? facility.availableAtFrame() : 0;

        ResourceCost cost = item.cost();
        int readyFrame = timeline.findEarliestAffordableFrame(cost, earliestProducerReady);
        if (readyFrame < 0) return;

        PlacementReservation placement = null;
        if (item.requiresPlacement()) {
            placement = placementPlanner.reservePlacement(item, placementConstraint, readyFrame);
            if (!placement.isSuccessful()) return;
            readyFrame = Math.max(readyFrame, placement.readyFrame());
        }

        timeline.allocate(cost, readyFrame);
        if (facility != null) facility.occupyUntil(readyFrame + item.buildDurationFrames());

        plan.add(new ScheduledScheduleItem(item, facility, placement, readyFrame));
    }
}
```

#### Layer 4: Execution Dispatcher

```java
public final class ProductionDispatcher {
    private final BuildingOrderDirector buildingDirector;

    public ProductionDispatcher(BuildingOrderDirector buildingDirector) {
        this.buildingDirector = buildingDirector;
    }

    public void dispatch(ProductionPlan plan, int currentFrame, int latencyFrames) {
        List<ScheduledScheduleItem> dueItems = plan.itemsDueBefore(currentFrame + latencyFrames);

        for (ScheduledScheduleItem scheduled : dueItems) {
            if (scheduled.isDispatched()) continue;

            if (scheduled.item().requiresPlacement()) {
                buildingDirector.dispatchBuilder(scheduled.placement(), scheduled.item());
            } else {
                AUnit producerUnit = scheduled.assignedFacility().unit();
                producerUnit.train(scheduled.item().asUnitType());
            }
            scheduled.markDispatched();
        }
    }
}
```

---

### 4. Why This Architecture Resolves 10 Years of Atlantis Bugs

1. **Zero State Mutation Across Frames:** In legacy Atlantis, an order lingered inside `Queue` in an `IN_PROGRESS` state even if the builder died en route. Here, the plan is entirely stateless: calculated and discarded every frame. No `RemoveExcessiveOrders`, `PreventDuplicateOrders`, or `isAlreadyConsumed` workarounds.
2. **Elimination of Economic Starvation:** If a high-priority tech goal (e.g. Carrier) is requested, lower-priority unit goals **cannot starve it by hoarding minerals**. The timeline solver reserves funds for the Carrier at its target frame and shifts the Zealot forward into the post-reservation surplus.
3. **Strict SRP Adherence:**
   - Modifying building placement logic alters only `PlacementPlanner`.
   - Modifying worker mining rates alters only `ResourceTimeline`.
   - Neither change impacts `ProductionScheduler` or `ProductionDispatcher`.
4. **OCP and Multi-Race Extensibility:** Adding Zerg does not require 50 `if (We.zerg())` branches. Zerg simply injects a `LarvaFacilityRegistry`, leaving `ResourceTimeline` and scheduling algorithms untouched.

---

### 5. Edge Case Resolutions

#### A. Strict Placement Constraints (`exactTile`) and Worker Mortality
`TargetPlacement` provides 3 constraint modes:
- `TargetPlacement.anywhere()`
- `TargetPlacement.inNeighbourhood(Neighbourhood)`
- `TargetPlacement.exactTile(TilePosition tile)` (for Choke Walls, ramp Pylons, Cannon creeps, geysers).

**Worker Death Scenario:**
1. Frame 1,000: Goal `Goal(Forge, exactTile(42, 18), priority=10)`. Worker A assigned (travel time: 120 frames). Minerals allocated for frame 1,120.
2. Frame 1,050: Worker A is intercepted and dies.
3. Frame 1,051: The previous frame's plan no longer exists. The Forge goal remains unsatisfied. The scheduler re-evaluates, selects Worker B (nearest living builder), and directs it to the **exact same tile (42, 18)**. No orphaned states or stuck construction locks.

#### B. Priority Bands: Rush Defense vs. Capital Ships
The scheduler does not make policy decisions — active strategic **Plays** determine priority bands:

| Priority Band | Priority Value | Purpose |
|---|---|---|
| `PRIORITY_EMERGENCY` | **10** | Immediate rush defense (Bunker repair, emergency Zealots/Cannons) |
| `PRIORITY_WORKERS` | **20** | Continuous economic expansion |
| `PRIORITY_DEPOTS` | **30** | Supply-block prevention |
| `PRIORITY_BASEDEFENSE`| **40** | Early-game choke security |
| `PRIORITY_MAINARMYBASE`| **60** | Core baseline military strength |
| `PRIORITY_NORMAL` | **80** | Tech progression, core upgrades |
| `PRIORITY_MAINARMY` | **90** | Macro army scaling (mass Gateways, late-game Carriers) |
| `PRIORITY_LOWEST` | **100** | Speculative tech, third-tier upgrades |

- **Peace State:** Carrier at priority 60, macro Zealots at priority 90. The scheduler reserves Carrier funds first; Zealots are scheduled only from the remaining economic surplus.
- **Under Zergling Rush:** Strategy detects aggression and dynamically raises Zealots to `EMERGENCY (10)`. Carrier goals are deprioritized or removed. The scheduler immediately claims all available minerals for Zealots, pushing expensive tech far beyond the horizon.
- **Head-of-Line Blocking Elimination:** Because Gateway and Stargate are separate facilities and reservations are projected over time, an expensive future goal will never idle an available Gateway if income replenishes before the Stargate becomes ready.

---

## Success Metrics & Validation

- **Deterministic Unit Tests:** Verify `ProductionScheduler` using mocked timelines ("Pylon before Gateway", "Cybernetics before Dragoon", "competing gas goals shift rather than drop").
- **A/B Performance Benchmarks:** Zero idle production facility frames, zero mineral banking > 500 without active capacity expansion, zero "overdue constructions".
- **Integrity Validation:** Zero duplicate orders issued within latency windows.

## Implementation Risks & Mitigations

- **Economic Accuracy:** BWAPI does not expose native forward gathering simulation. Stardust's constant worker mining rate model (`MINERALS_PER_WORKER_FRAME`) provides sufficient fidelity.
- **Latency Margins:** Dispatches must respect `BWAPI::Broodwar->getRemainingLatencyFrames()` to prevent command drops.
- **Worker Movement Estimation:** Worker travel times to target tiles must be factored into placement readiness to avoid builders idling at build sites.
