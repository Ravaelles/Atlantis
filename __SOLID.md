# SOLID / SRP review of the 50 largest Java files in `src/`

OWNER'S NOTE: Out of these, I'd say it's not worth to modify SelectionMembership, AAdvancedPainter, Select, Count and 
MapSpecificCommander. bwem/BWMap is vendor's dir, so I'd leave it as well. Also don't spend time on old Production 
V1 code that will eventually be removed. 

Scope note (per the last instruction): `AUnit.java`, `AUnitType.java` and all concrete test classes
(`*Test.java`, e.g. `DragoonsVsZerglingsTest`, `AUnitTest`, `ProductionSchedulerTest`, `DragoonKiteTest`,
`GoalSourcesTest`, `WorkerDefenceTest`, `WorkerSwarmTest`, `CombatEvaluatorTest`, `GoalBoardFeederTest`)
are **excluded**. Abstract test infrastructure (`AbstractTestWithUnits`) and test fakes/harnesses are kept,
because they are shared infrastructure rather than the tests of one specific feature.

The exclusion list is applied first, then the remaining files are sorted by byte size; the table below is
the result (the 50 largest remaining `.java` files).

| # | File | Bytes | Lines |
|---|---|---:|---:|
| 1 | `src/atlantis/debug/painter/AAdvancedPainter.java` | 74 937 | 1915 |
| 2 | `src/atlantis/units/AUnitOrders.java` | 40 867 | 922 |
| 3 | `src/atlantis/units/select/SelectionMembership.java` | 36 177 | 1064 |
| 4 | `src/atlantis/units/select/Selection.java` | 31 178 | 1095 |
| 5 | `src/atlantis/production/constructions/commanders/ConstructionStatusChanger.java` | 29 271 | 579 |
| 6 | `src/atlantis/placement/engine/CataloguePlacementPlanner.java` | 26 628 | 622 |
| 7 | `src/atlantis/debug/tweaker/ParamTweakerFactory.java` | 25 226 | 471 |
| 8 | `src/main/Main.java` | 23 828 | 437 |
| 9 | `src/atlantis/production/requests/produce/ProduceBuilding.java` | 20 664 | 438 |
| 10 | `src/atlantis/units/select/Select.java` | 20 404 | 624 |
| 11 | `src/tests/unit/AbstractTestWithUnits.java` | 19 732 | 479 |
| 12 | `src/atlantis/production/v2/goals/DynamicGoals.java` | 18 962 | 446 |
| 13 | `src/atlantis/config/env/Env.java` | 18 864 | 504 |
| 14 | `src/atlantis/production/constructions/NewConstructionRequest.java` | 17 830 | 392 |
| 15 | `src/bwem/BWMap.java` | 17 489 | 487 |
| 16 | `src/atlantis/production/orders/production/queue/order/ProductionOrder.java` | 17 456 | 580 |
| 17 | `src/atlantis/production/constructions/Construction.java` | 17 386 | 482 |
| 18 | `src/atlantis/battle/kite/CombatTuning.java` | 17 326 | 492 |
| 20 | `src/tests/fakes/FakeUnit.java` | 16 937 | 667 |
| 21 | `src/atlantis/Atlantis.java` | 16 601 | 492 |
| 22 | `src/atlantis/battle/FightTarget.java` | 16 272 | 310 |
| 23 | `src/atlantis/units/select/Count.java` | 16 253 | 506 |
| 24 | `src/atlantis/production/orders/production/queue/add/AddToQueue.java` | 15 981 | 381 |
| 25 | `src/atlantis/production/v2/LegacyPlacementPlanner.java` | 15 923 | 359 |
| 26 | `src/atlantis/production/dynamic/workers/AutoProduceWorkersCommander.java` | 15 848 | 365 |
| 27 | `src/atlantis/util/ProcessHelper.java` | 15 489 | 386 |
| 28 | `src/atlantis/production/constructions/builders/RefreshConstructionPosition.java` | 15 315 | 313 |
| 29 | `src/atlantis/map/AMap.java` | 15 114 | 395 |
| 30 | `src/atlantis/map/base/BaseLocations.java` | 15 061 | 418 |
| 31 | `src/tests/unit/sim/SimWorld.java` | 15 046 | 381 |
| 32 | `src/atlantis/units/workers/defence/run/WorkerDefenceRun.java` | 14 999 | 390 |
| 33 | `src/atlantis/production/v2/ProductionScheduler.java` | 14 694 | 317 |
| 34 | `src/atlantis/production/constructions/position/protoss/ProtossPositionFinder.java` | 14 593 | 307 |
| 35 | `src/atlantis/production/v2/engine/GameStateSnapshot.java` | 14 460 | 328 |
| 36 | `src/atlantis/production/constructions/builders/BuilderDiagnostics.java` | 14 100 | 268 |
| 37 | `src/atlantis/production/constructions/commanders/CancelTooLongConstructions.java` | 14 015 | 315 |
| 38 | `src/atlantis/map/position/HasPosition.java` | 13 954 | 431 |
| 39 | `src/atlantis/production/constructions/ConstructionRequests.java` | 13 888 | 388 |
| 40 | `src/atlantis/units/Units.java` | 13 681 | 501 |
| 41 | `src/atlantis/production/requests/produce/BuildGates.java` | 13 343 | 303 |
| 42 | `src/atlantis/map/position/APosition.java` | 13 293 | 418 |
| 43 | `src/atlantis/game/A.java` | 13 175 | 434 |
| 44 | `src/atlantis/production/constructions/builders/IssueBuildOrder.java` | 13 147 | 226 |
| 45 | `src/jfap/JFAP.java` | 12 803 | 340 |
| 46 | `src/atlantis/production/dynamic/protoss/units/ProduceZealot.java` | 12 680 | 305 |
| 47 | `src/atlantis/game/listeners/OnGameStarted.java` | 12 643 | 257 |
| 48 | `src/atlantis/production/orders/production/queue/add/PreventDuplicateOrders.java` | 12 461 | 327 |
| 49 | `src/atlantis/production/dynamic/terran/turrets/TurretsForMain.java` | 12 162 | 347 |
| 50 | `src/atlantis/config/MapSpecificCommander.java` | 11 843 | 326 |

## How to read the entries

Each entry gives: what the file actually does (from its code), where its single responsibility breaks (SRP),
and — where it is genuinely worth changing — concrete, minimal changes with the SOLID principle they serve.
Files that are large but legitimately single-purpose are marked **keep the shape** with a short reason;
I do not recommend splitting everything just because it is big.

Priority legend: **P1** = do it, cheap and clearly beneficial; **P2** = worth doing during the next
touch of that area; **P3** = optional / cosmetic.

A recurring theme, named once here so it is not repeated 20 times:
the codebase has two *shapes* of SRP debt.
1. **Wiring debt** — composition roots (`Main`, `Atlantis`, `OnGameStarted`, 
   `AbstractTestWithUnits`, `ParamTweakerFactory`) doing many independent jobs in one class.
2. **Rule-debt** — a behavioural policy living as a long `if`-chain inside a data object
   (`AUnit` order helpers, `Count`, `Construction`, `ProduceZealot`, `Count`-style counting, `BWMap`).
Wiring debt is safe to carve out mechanically; rule-debt needs each rule named before it is moved.

---

## 2. `src/atlantis/units/AUnitOrders.java` — 922 linie, 46 poleceń

**What it does.** An interface of `default` methods (one per BWAPI command: `attackUnit`, `move`, `train`,
`build`, `useTech`, cancel-*, load/unload, burrow, siege...) that all end in `unit().orderSink().xxx(...)`.

**SRP break.** The interface already partially solved SRP by taking orders out of `AUnit` — good. What is left
is mis-grouped: validation (null checks, `ErrorLog` spam throttling), "should I even issue this" policy
(`executeOrder`, `lastCommandIssuedAgo()` cooldowns), and the actual command translation to the sink, all mixed
in one default method (see `move(...)`, lines 228–311). It is also an *fat* interface: a test fake that wants
only `move` must inherit 40 unrelated defaults (ISP).

**Proposed changes.**
- (P1) Extract a small `OrderIssuance` helper: `boolean shouldIssue(AUnit, HasPosition target)` — the
  `!target.equals(currentTarget) || lastPositionChangedAgo() >= 10` heuristic and `commandMinDelayWhenMoving`
  live there, unit-testable without an engine. (SRP.)
- (P2) Split the interface by intent into `MovementOrders`, `CombatOrders`, `ProductionOrders`,
  `StructureOrders`, `AbilityOrders`, and let `AUnitOrders extend` them only for `AUnit`. Callers that need
  only movement stop depending on cancels and tech casts. (ISP.)
- (P3) The repeated `ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Null ... " + unit.typeWithHash())`
  pattern (≈8 occurrences) becomes one `requireOrderArgs(...)` guard; the tooltip/logging block at the end of
  every command becomes one `recordDecision(unit, action, target, issued)`. (DRY + SRP.)

**Keep** the one-command-per-default-method granularity — it is the right grain for translating to `OrderSink`.

---
## 3. `src/atlantis/units/select/SelectionMembership.java` — 1 064 linie, 154 predykaty

**What it does.** ~150 `static Selection xxx(Selection s, ...)` filter functions used by `Selection`'s fluent API.
They are grouped by nothing in particular: type filters (`dragoons`, `marines`, `televators`), state filters
(`wounded`, `burrowed`, `notStuck`), combat predicates (`canAttack`, `inShootRangeOf`), economy, roles
(`builders`/`protectors`), timestamps (`lastSeenRecently`).

**SRP break.** One class = one *category* of filter, but here the category is "all filters". It is a stateless
utility, so splitting is low risk but it is also currently the only seam that keeps `Selection` small — which
is valuable. The real SRP smell is the `ofType`-style filters that duplicate `AUnit`'s own `is*` methods and
the `typeMatches` helper.

**Proposed changes (P2–P3).**
- Group by meaning into `MembershipByType`, `MembershipByState` (hp/energy/cooldown/burrowed/loaded),
  `MembershipByCombat` (canAttack, inShootRange, facing), `MembershipByRole`, `MembershipByTime` — each is
  then a cohesive unit with a clear reason to change. (SRP, ISP.)
- (P3) `canAttack(...)` and `inShootRangeOf(...)` are *combat rules*, not membership; they should depend on a
  combat policy object (`Targetability`) rather than reaching into `AUnit`/`WeaponType` directly, so the
  evaluator and the filter share one definition. (DRY, OCP.)

---
## 4. `src/atlantis/units/select/Selection.java` — 1 095 linii, 228 metod

**What it does.** A fluent wrapper over a `Collection<AUnit>`. Most methods are one-liners delegating into
collaborators (`SelectionMembership`, `SelectionViews`, `SelectionRadius`, `SelectionCount`) — that delegation
already fixed the once-huge class. What remains *inside* `Selection` itself: termination/accessors
(`count`, `first`, `second`, `random`, `list`, `units`), sorting (`sortDataByDistanceTo` with an inner
comparator), aggregation (`totalHp`, `totalHits`, `averageShields`), predicates on the whole collection
(`onlyMelee`, `onlyRanged`, `mostlyOfType`), printing and `estimate()`.

**SRP break.** `Selection` is simultaneously: a collection view, a sorter, an aggregator, a collection-wide
predicate holder, and a debug printer. The 200 delegating methods are harmless, but they hide the fact that the
*small* class that is left still has five responsibilities.

**Proposed changes.**
- (P1) Move aggregation/printer methods (`totalHp`, `totalHits`, `averageShields`, `estimate`, `print`,
  `unitIds`, `toString` formatting) to `SelectionStats` / `SelectionDebug`. The fluent API keeps one-line
  forwards. (SRP.)
- (P2) Move `onlyMelee`/`onlyRanged`/`mostlyRanged`/`onlyAir`/`mostlyOfType` into a `Composition` value
  object; they answer "what is this group made of", which is a different question from "which units are in it".
- (P3) `Sorting` (the three `sortDataBy*` plus comparators) becomes its own class so `Selection` no longer
  needs to know that distance sorting exists.

---
## 5. `src/atlantis/production/constructions/commanders/ConstructionStatusChanger.java` — 579 linii

**What it does.** A `Commander` that iterates `ConstructionRequests.constructions` and, per construction, runs a
long sequential decision: detect "building destroyed", promote builder→building (Extractor), Zerg special case,
"proximity finish", promote to IN_PROGRESS, demote stalled, twin-frozen warp detection, log. Almost the whole
class is one method, `checkForConstructionStatusChange` (lines 35–247), plus static helpers
(`refineToStartingIfTravelling`, `advanceToInProgressIfStarted`, `demoteStalledInProgress`,
`shouldProximityFinish`, `adoption`, `coveringBuildingTile`).

**SRP break.** The class mixes (a) *traversal* (loop), (b) *per-construction state machine transitions*
(PREPARING→STARTING→IN_PROGRESS→FINISHED and demotions), (c) *engine quirks* (OpenBW not filling `buildUnit()`,
Zerg morphing), and (d) *diagnostics/logging*. Each transition is an independent rule; the long `if` chain
means adding a transition touches the same method as all others (OCP) and the rules cannot be unit-tested
separately.

**Proposed changes.**
- (P1) Turn each transition into a named rule object implementing `ConstructionTransition { boolean apply(Construction); }`
  (`DestroyedRule`, `BuilderBecameBuildingRule`, `ZergMorphRule`, `ProximityFinishRule`,
  `PromoteToInProgressRule`, `DemoteStalledRule`, `FrozenWarpTwinRule`) collected in one ordered list.
  `handle()` becomes: for each construction, for each rule until one returns true. The rules are pure enough to
  test with a stub `Construction`. (SRP + OCP.)
- (P2) The static `logger` block (every cancel emits an `EventLog` with 6 params, and the same shape repeats)
  becomes `ConstructionEvents.cancelled(construction, reason, detail)` — one place that decides the row shape.
  (SRP; keeps logging out of decisions.)
- (P2) `handleZergConstructionsWhichBecameBuildings()` belongs to a Zerg policy object (`ZergConstructionQuirks`),
  not to the generic changer. (SRP — race-specific knowledge in a race-specific place.)
- (P3) `coveringBuildingTile` / `finishedBuildingOnSameTiles` are geometry queries — move to a
  `BuildingFootprint` helper reused by placement. (DRY.)

---
## 6. `src/atlantis/placement/engine/CataloguePlacementPlanner.java` — 623 linie

**What it does.** Implements `PlacementPlanner.reservePlacement` on top of `TileAvailabilityGrid`,
`BuildLocationCatalogue`, `RacePlacementStrategy`, `NeighbourhoodRegistry`. It also owns: six constructors and
their defaults (`GameContext`, `LIVE_GAME`/`NO_GAME` via a nested interface), pass-lifetime state
(`reservedThisPass`, `supportThisPass`, `passBuilder`), support-request draining, candidate refinement
(`refine`, `availabilityFrames`, `builderTravelEstimate`, `exitDistanceFor`), ranking (`rankerFor`,
`coverageRankerFor`, `nearestTo` + an inner `Comparator`), and search-centre resolution (`searchCentre`,
`areaCentre`, `baseCentre`).

**SRP break.** Three responsibilities: (a) planner lifecycle (`startPass`/`endPass`/drain), (b) *candidate
generation & scoring* (pure geometry + strategy), (c) *adapter from `Producible`/`TargetPlacement` to
`AUnitType`/positions*. The six overloaded constructors exist mostly to make (b) testable — a sign the object
graph is too big for one class.

**Proposed changes.**
- (P1) Extract `BuildSiteRanking` (rankerFor/coverageRankerFor/nearestTo/squaredDistance) and
  `BuilderTravel` (travel estimate + exit distance). They are pure and are the parts most likely to be tuned;
  each becomes an independently testable unit. (SRP.)
- (P2) Extract `PlacementPassState` (reserved set, support list, pass builder) and inject it; the planner keeps
  no mutable pass fields → no hidden temporal coupling between `startPass` and `reservePlacement`. (SRP +
  explicit lifecycle.)
- (P2) Replace the constructor zoo with a factory (`CataloguePlacementPlanner.forTests(grid, strategy)` /
  `.forLiveGame(...)`), so production wiring (`EngineTerrainSource`, `ProtossPlacementStrategy`,
  `EnginePowerSource`, `EngineNeighbourhoodSource`) is created in the composition root instead of a domain class.
  (DIP — the planner should not know how the engine is built.)
- (P3) `GameContext` nested interface is fine as a seam, but it sits in the planner; move it next to
  `PlacementPlanner` so both default and legacy planners share it. (ISP.)

---
## 7. `src/atlantis/debug/tweaker/ParamTweakerFactory.java` — 471 linii

**What it does.** `init()` (lines 12–232) registers ~40 parameters one by one: name string, getter lambda, setter
lambda that calls `applyOrRandomize`/`applyOrRandomizeInt` with a min/max/divisor. `apply(name, value)` then
re-dispatches by **string name** to the right static field/setter (lines 233–368). `bounds()` and
`integerParams()` are a *third* parallel table (names → ranges) and there is a *fourth* place where the same
names appear in the setter lambdas.

**SRP break.** The class holds four representations of the same thing (registration, apply-dispatch, bounds,
integrality) that must be kept in sync by hand — a classic change-amplification (OCP: a new parameter edits
4 places; SRP: "describe a parameter" and "apply a parameter" are different jobs).

**Proposed changes.**
- (P1) Introduce a `TweakParam` value object: `{ name, getter, setter, min, max, divisor, isInteger }`. Keep one
  `List<TweakParam>`; derive `bounds()` and `integerParams()` from it, and make `apply(name, value)` look the
  parameter up and call `param.set(value)`. Deleting the string switch removes an entire class of typo bugs.
  (SRP + OCP + DRY.)
- (P2) Split registration into per-domain registrars (`CombatParams`, `EscapeParams`, `MacroParams`) that each
  return their list; the factory only concatenates. The factory then has one reason to change: the registry
  contract. (SRP.)
- (P3) The randomisation range belongs to the parameter, not to the call site — with `TweakParam` it moves
  naturally. Removes ~40 repeated `applyOrRandomize(...)` invocations.

---
## 8. `src/main/Main.java` — 437 linii

**What it does.** `main()` reads env, optionally starts local setup, registers a shutdown hook, runs `Atlantis`.
`defineMapToUse()` picks a map from CLI, then tries five hard-coded per-race/per-testing map helpers
(`mapGeneric`, `mapAsProtoss` 150 lines, `mapAsTerran` 70, `mapAsZerg` 13), each returning a hard-coded path.
`runOpenBWGame` and `localAtlantisSetup` handle launcher details; `ourRace()`/`enemyRace()` read env.

**SRP break.** Four jobs in one entry point: (a) bootstrap/lifecycle, (b) **test-map catalogue** (≈230 lines of
`if (buildOrderSettings.equals("...")) return "maps/...";`), (c) launcher selection (Wine/OpenBW/local),
(d) process teardown hook.

**Proposed changes.**
- (P1) Extract the map catalogue into `TestMapCatalogue` (a name → path map, or `MapFor(buildOrderSettings,
  race)`), leaving `defineMapToUse` as: CLI override → catalogue → default glob. This alone removes >200 lines
  from the composition root and makes the test map list data instead of code. (SRP + OCP.)
- (P2) Extract `ShutdownSummaryHook` (the `Runtime` hook) and `LocalLauncherSetup` into their own classes;
  `main()` should read as: *env → optional local launch → run → teardown*. (SRP.)
- (P3) `localAtlantisSetup` mixes "start process", "modify bwapi.ini", "sleep" — split per launcher type using
  the already existing launcher strategy (Windows vs Wine), so the branch lives in one place. (OCP.)

---
## 9. `src/atlantis/production/requests/produce/ProduceBuilding.java` — 438 linii

**What it does.** The entry point for "start building X from an order": refusal guard + throttle (`refuse`,
`clearRefusedThrottleForTests`), pylon dedup/cost accounting (`tooManyPylonsInFlight`, `anotherUnstartedPylonExists`,
`countUnstartedPylonsForOtherOrders`, `earlierUnfinishedCost`), "is this already paid" (`isAlreadyPaidFor`),
demand diagnostics (`demandReason`, `demandContext`), dispatch (`produceBuilding`, `requestBuilding`), addon path,
site adoption (`adoptPreparedSite`), and `noteStarted` status update + `validatePosition`.

**SRP break.** It is a facade *and* a set of five separate policies: pylon throttling, funding/reservation
accounting, demand explanation (debug), adoption of a prepared site, and position validation. The static mutable
`_lastConstruction` also makes it stateful.

**Proposed changes.**
- (P1) Name the policies and extract them: `PylonInFlightPolicy` (tooMany/anotherUnstarted/countUnstarted),
  `ConstructionFunding` (earlierUnfinished*, isAlreadyPaidFor), `PreparedSiteAdoption` (adoptPreparedSite +
  validatePosition). `produceBuilding` stays a short orchestration: *refuse? → request → adopt → note*. (SRP.)
- (P2) The `refuse(...)` throttle map and `_lastConstruction` static → an injected `RefusalThrottle` instance with
  `resetForTests()`; today tests must call a production class to clear state. (SRP + testability/DIP.)
- (P3) `demandReason`/`demandContext` belong to a diagnostics class (they exist to explain refusals to a human),
  not to the decision path. (SRP.)

---
## 10. `src/atlantis/units/select/Select.java` — 624 linie

**What it does.** Static entry point for queries: four frame-cached `UnitIndex`es (`all`, `ours`, `enemy`,
`neutral`) with duplicated `xxxIndex`/`xxxIndexFrame` pairs, a `Cache` for legacy keys, race-specific helpers
(`ourOfType`, `countOurOfType*`, `ownBuildings`, `minerals`, `geysers`), "main base" resolution
(`main`, `mainFromCache`, `mainOrAnyBuilding`, `naturalOrMain`), and worker selection
(`ourWorkersThatGather`, `ourWorkersMiningMinerals`, `ourFree`).

**SRP break.** Three responsibilities: (a) **index/cache lifecycle**, (b) **query facade** (many of the
methods are compositions of `Selection` calls), (c) **semantic definitions** of "main base", "free worker",
"workers that gather" — which are domain policies, duplicated with `BaseLocations` (`main()`, `natural()`) and
`Economy`.

**Proposed changes.**
- (P1) Extract `UnitIndexCache` with a single generic `index(Source)`/invalidate mechanism; the four
  index/frame field pairs collapse to one map. (SRP + removes 4× duplicated invalidation code.)
- (P2) Extract `MainBase` (main/natural/orAny answers) and delegate `BaseLocations.main()` to it — one
  definition of "where is our main", instead of two. (DRY + SRP.)
- (P2) Worker-role queries (`ourWorkersThatGather`, `ourWorkersMiningMinerals`, `ourFree`) move next to
  `Economy`/`FreeWorkers`, and `Select` merely forwards. (SRP.)

**Keep** the static facade itself — the entire codebase calls `Select.our()...`, and that is a deliberate,
cheap-to-read API. The fix is *what* it delegates to, not removing the facade.

---
## 11. `src/tests/unit/AbstractTestWithUnits.java` — 479 linii (abstrakcyjna infrastruktura testów)

**What it does.** One `@BeforeEach` that boots the whole fake world: registers ~10 seam factories (UnitStats,
FakeBullets, FakeRegion, FakeFoggedUnit, FakeUnitOrigin, FakeOrderFallback, FakeMapTiles, FakePositionFinder,
FakeResearch), clears caches, runs `MockEverything`, resets the clock. Then helpers: races, build order/strategy
setup, `useFakeTime`, `fake(...)` constructors, `fogged(...)`, assertions. `@AfterEach cleanUp()` closes
`MockedStatic` fields **by reflection over public fields** and resets ~6 production statics by name.

**SRP break.** (a) world bootstrapping, (b) test-fixture factory methods, (c) teardown/isolation bookkeeping.
Teardown is the worst part: it lists production statics one by one (`ProduceBuilding.clearRefusedThrottleForTests`,
`GoalBoardFeeder.clearForTests`, `AddToQueue.clearForTests`, `ReservedResources.reset`, ...), so every new static
in production adds a line here (OCP) and the reflection trick requires fields to be `public` (test leakage into
production visibility).

**Proposed changes.**
- (P1) Introduce `TestWorldBootstrap` (install seams), `TestIsolation.reset()` (one entry point that production
  statics themselves register into — e.g. a `ResetRegistry` or `ClearAllCaches.register(...)`), and
  `FakeUnits` (the `fake/fakeEnemy/fakeOurs/fogged` builders). `AbstractTestWithUnits` then only composes them.
  (SRP; removes the "new static → edit test base" coupling.)
- (P2) Replace the reflective `MockedStatic` closing with an explicit `List<AutoCloseable> registeredMocks`
  declared in the base class — reflection over `public static` fields constrains production visibility for a test
  convenience. (SRP; better encapsulation.)
- (P3) The MI/`enemyRaceInWorld` mutable fields stay, but are documented as a fixture; no change needed.

---
## 12. `src/atlantis/production/v2/goals/DynamicGoals.java` — 446 linii

**What it does.** `contribute(GameSnapshot, ProductionRacePolicy)` collects goals from independent generators:
`addFortificationGoals`, `addWorkerGoal`, `addSupplyGoal` (+`supplyGoal` public), `addArmyGoal`, `addTechGoal`,
`addGasGoal`, `addExpansionGoal`. Nested `GameSnapshot` holds **21 constructor arguments** and five telescoping
constructors (lines 89–172), plus a public mutable `supplyProvidersComing`.

**SRP break.** The static class is fine as a *collector*, but it also holds the race-independent numbers for
every generator: it is the only place where worker saturation, supply urgency, army sizing, tech gating and
expansion timing rules meet, so each rule's constants (`SUPPLY_WORRY_LEVEL`, `WORKERS_PER_BASE`) share a class
with unrelated rules. The nested `GameSnapshot` has SRP problems of its own: it is a DTO *and* an assembler
(5 constructors filling zeros) — any new input requires touching 5 constructors and every test.

**Proposed changes.**
- (P1) Replace the telescoping constructors with a builder (`GameSnapshot.builder().workers(..).gas(..).build()`)
  or a single canonical constructor plus named factories (`forArmyOnly(...)`). Adding a field then touches one
  place. (SRP + OCP; also makes tests readable.)
- (P1) One class per generator (`WorkerGoalSource`, `SupplyGoalSource`, `ArmyGoalSource`, `TechGoalSource`,
  `GasGoalSource`, `ExpansionGoalSource`, `FortificationGoalSource`) with a shared `GoalSource { void contribute(List, GameSnapshot, Policy) }`.
  `DynamicGoals.contribute` becomes a loop. Each source then has one reason to change and a matching test.
  (SRP + OCP — the codebase already has `GoalSourcesTest` and "the supply rule is declared in exactly one place",
  which is exactly this direction.)
- (P2) Group the 21 snapshot fields into value objects (`Economy {minerals, gas, workers, bases}`,
  `Supply`, `Army`, `Tech`, `EnemyFacts`, `Anchor`). Both the constructors and the generators then take what
  they actually read, instead of the whole snapshot. (ISP.)

---
## 13. `src/atlantis/config/env/Env.java` — 504 linie

**What it does.** Parses the ENV file into ~18 static flags, keeps `rawEnv`, exposes typed getters
(`isLocal`, `isWine`, `isOpenBW`, `productionV2`, `builderLog`, ...) **and** ~9 `setXxxForTest(...)` mutators,
plus path resolution, CLI-arg scanning (`mainArgsContains/Equals`), value coercion (`toInt`, `trueFalse`).

**SRP break.** Four responsibilities: (a) file discovery/IO, (b) parsing/coercion, (c) the **flag registry**
(spread across a 100-line `switch` in `applyKeyAndValueToFlag`), (d) a **test backdoor** (setters) that exists
only because the flags are static. Any new ENV key edits parsing + switch + getter (+ setter + test).

**Proposed changes.**
- (P1) Make the flags a `Map<EnvKey, Supplier<Object>>`/typed record with declared defaults, and let the switch
  become a declaration table (`EnvKey.BUILDER_LOG -> value -> builderLog = trueFalse(value)`). Adding a key
  becomes one line. (OCP + SRP.)
- (P1) Move the `setXxxForTest` setters into a single `EnvTestOverrides` object (or make `Env` an instance that
  tests create) — production config then has no write path after `readEnvFile`. (SRP; removes mutable statics
  from production code.)
- (P2) Split IO (`EnvFile.read(path) → Map<String,String>`) from interpretation (`EnvFlags.from(map, args)`).
  The parser becomes testable without touching the filesystem. (SRP + DIP.)

---
## 14. `src/atlantis/production/constructions/NewConstructionRequest.java` — 392 linie

**What it does.** Four `requestConstructionOf(...)` overloads that funnel into one method doing: validation,
special-building delegation (`SpecificConstructionRequests`), requirement check, construction creation,
builder assignment (random then optimal), position finding (`DefineExactPositionForNewConstruction`), duplicate
detection + re-find, cancel-with-log, and **diagnostics** (`requestingCaller()` walking the stack,
`dominantRefusal`, `shouldPullPylon` which reads a refusal breakdown map).

**SRP break.** The class is both the *use case* ("create a construction for building X") and the *diagnostic
narrator* (stack-trace caller detection, refusal aggregation, pylon pull logic). The decision sequence is also
a single ~150-line method where each guard changes state.

**Proposed changes.**
- (P1) Extract `ConstructionDiagnostics` (`requestingCaller`, `dominantRefusal`, refusal breakdown formatting).
  It exists to explain *why nothing was built* and has no place in the request path. (SRP.)
- (P1) Extract the flow into steps: `ConstructionValidator`, `BuilderSelection` (random + optimal), `SiteSelection`
  (position + duplicate re-find), `ConstructionRegistrar`. `requestConstructionOf` becomes a readable pipeline,
  and each step is testable alone. (SRP.)
- (P2) `shouldPullPylon` is strategy for the **Pylon-first** build; move it next to `BuildPylonFirst` so the
  fallback rule has one owner. (SRP.)

---
## 15. `src/bwem/BWMap.java` — 487 linii (+ nested `NeighboringAreaChooser`)

**What it does.** A port of BWEM's `Map`: area queries (`getArea`/`getNearestArea`/`getMainArea`), graph access,
BFS searches (two overloads each for `TilePosition`/`WalkPosition`), path length/path lookup, starting-location
assignment (`assignStartingLocationsToSuitableBases`), unit-destruction callbacks (`onUnitDestroyed`,
`onMineralDestroyed`, `onStaticBuildingDestroyed`), initialisation helpers (`setAreaIdInTile`,
`findNeighboringAreas`, `chooseNeighboringArea`).

**SRP break.** It is a *reference port*, so some breadth is justified. But it currently mixes (a) map model/queries,
(b) **analysis algorithms** (BFS twice, area-id assignment, neighbouring-area choice), (c) **event handling** for
destroyed units (this is the live-game side leaking into a static map model), and (d) starting-location policy
using `Select.geysers()` from the bot (`assignStartingLocationsToSuitableBases`, lines 81–119) — a heavy
dependency from a low-level library on a high-level service.

**Proposed changes.**
- (P1) Invert the dependency: `assignStartingLocationsToSuitableBases` should receive a `GeyserQuery` (or just the
  already-computed base list) instead of calling `Select`. The bot adapts `Select` to that interface in its
  composition root. (DIP — the map library must not know the bot.)
- (P2) Extract `MapSearch` (the two BFS families) and `AreaInitializer` (`setAreaIdInTile`, `findNeighboringAreas`,
  `chooseNeighboringArea`, `NeighboringAreaChooser`). They are pure algorithms over the model. (SRP.)
- (P2) Move `onUnitDestroyed`/`onMineralDestroyed`/`onStaticBuildingDestroyed` into a `MapUnitEvents` listener
  registered by `BWEM`; a map model should not subscribe itself to the game. (SRP + observer.)
- **Keep** the public query surface (`getArea`, `getPath`, `getBases`) as one facade.

---
## 16. `src/atlantis/production/orders/production/queue/order/ProductionOrder.java` — 580 linii

**What it does.** One `ProductionOrder` can represent four different things — a unit, a building, an upgrade, a
tech, and also a mission (five nullable payload fields: `upgrade`, `tech`, `unitOrBuilding`, `mission`,
`construction`), each of which gets `if (x != null)` branches in `toString`, `whatToString`, `name`,
`mineralPrice`, `gasPrice`. It also carries status/lifecycle (`status`, `isInProgress`, `isFinished`,
`isReadyToProduce`, `consume`, `resetRuntimeState`), reservations (`orderReservations`), placement info
(`position`, `maximumDistance`, `usingExactPosition`, `modifier`), supply gating
(`supplyRequirementFulfilled`, `penaltyReservedMinerals`, `supplyEarlierBonusToConsiderReady`), and equality
based on the raw first column.

**SRP break.** Two: (a) **sum type as a record with nullable fields** → every method re-implements a 4-way
switch (violates OCP: adding a fifth kind edits ~6 methods), and (b) the order mixes *data* with *policy*
("am I ready given supply/bonus/reservations"), and with *runtime links* (`construction`, `consumed`).
This is the canonical "anemic-ish entity that grew behaviour".

**Proposed changes.**
- (P1) Replace the nullable payloads with a sealed `OrderTarget` hierarchy (`UnitTarget`, `BuildingTarget`,
  `UpgradeTarget`, `TechTarget`, `MissionTarget`) exposing `name()`, `mineralPrice()`, `gasPrice()`,
  `isUnit()/isBuilding()`. Every `if (upgrade != null)` collapses into a polymorphic call. (OCP — the strongest
  win in this list.)
- (P1) Extract `OrderReadiness` (supply check + earlier bonus + reserved-mineral penalty) as a policy object
  taking the order state; the order becomes a data holder with a `reservations()` getter. (SRP.)
- (P2) Split `status` transitions into an `OrderLifecycle` (`notReady → ready → inProgress → finished`,
  `consumed`, `cancel`) so invariants (e.g. "consumed orders are finished") have one home. (SRP.)
- (P3) `rawFirstColumnInFile` is parsing metadata — belongs to the build-order parser, stored via a
  `SourceLine` field only for diagnostics.

---
## 17. `src/atlantis/production/constructions/Construction.java` — 482 linie

**What it does.** The construction record: building type, site (`positionToBuild`, `near`, `maxDistance`),
crew (`builder`, `reservedBuilder`, `buildingUnit`), status + timings (`status`, `timeOrdered`,
`timeBecameInProgress`, `lastBuildAttemptFrame`, `frozenWarpTwinned`), link to `ProductionOrder`, plus
**behaviour**: `findPositionForNewBuilding()` (calls `APositionFinder`, cached by a string key built from ids),
`assignRandomBuilderForNow()` (calls `FreeWorkers.get()`), `assignOptimalBuilder()` (≈50 lines of builder
policy), `isOverdue()`, `progressPercent()`, `releaseReservedResources()`.

**SRP break.** A data/aggregate object that also performs **worker selection** and **site finding** — both are
domain decisions that depend on `FreeWorkers`, `APositionFinder`, supply state. That is why
`NewConstructionRequest` and `ConstructionStatusChanger` both reach back into `Construction` to make decisions:
the aggregate is the decision-maker and its callers are orchestrators with no rules.

**Proposed changes.**
- (P1) Move `assignRandomBuilderForNow` / `assignOptimalBuilder` to a `BuilderAssignment` policy
  (it already reads supply, stuckness, distance, previous builder). `Construction` keeps `setBuilder`/`builder()`.
  (SRP — the aggregate stores, the policy decides.)
- (P1) Move `findPositionForNewBuilding` + `genericCacheKey` to the site-finder (`ConstructionSite`) so the cache
  key policy lives with the thing it caches. (SRP.)
- (P2) `isOverdue`, `progressPercent`, `startedAgo` are *queries over the lifecycle* → `ConstructionTimeline`
  value object; also gives `CancelTooLongConstructions` a single collaborator instead of six private helpers.
  (SRP + DRY.)
- (P3) `frozenWarpTwinned` is an OpenBW-engine workaround flag; document and keep, but name it
  `engineWarpTwin` to make the reason visible.

---
## 18. `src/atlantis/battle/kite/CombatTuning.java` — 492 linie

**What it does.** An immutable (copy-on-write) snapshot of **37 tuning knobs** with one getter and one `withXxx`
per knob (~74 accessors), a copy constructor (37 assignments), `current()`/`install()`/`resetToDefaults()` statics.
Everything is a flat `double`/`int` field in one class.

**SRP break.** The copy-on-write mechanism is good, but the class holds knobs for **at least four unrelated
policies**: kite geometry (`kite*`), Dragoon-specific thresholds (`dragoon*`), breach/retreat (`breach*`), and
unit-vs-unit-memory (`ums*`, `regroup*`). Any new knob touches: field, default, getter, `withXxx`, copy ctor,
`ParamTweakerFactory` (4–5 files). That is the definition of a low-cohesion class.

**Proposed changes.**
- (P1) Split into `KiteTuning`, `DragoonTuning`, `BreachTuning`, `RegroupTuning`, each an immutable value with its own
  `with` methods, and let `CombatTuning` be a *composite* holding the four (`CombatTuning.current().kite().enemyTiles()`).
  (SRP + ISP; `Kite` then depends only on `KiteTuning`.)
- (P2) Replace the 37 hand-written `withXxx`/copy pairs with one generic `Tuning.apply(Map<String,Double>)`
  or a builder; the getters stay for readability. Removes ~150 lines of mechanical code. (SRP.)
- (P2) The static `current`/`install` global is a service locator; prefer injecting `CombatTuning` into the
  managers that use it (as a parameter of the `Kite`/`DuelKite` calls), with the static only at the composition
  root. (DIP.)

---
## 20. `src/tests/fakes/FakeUnit.java` — 667 linii, ~38 public fields

**What it does.** An `AUnit` subclass that doubles as the test world's unit: public mutable fields for nearly
every engine fact (`hp`, `shields`, `energy`, `cooldown`, `completed`, `burrowed`, `cloaked`, `lockedDown`,
`stasised`, `target`, `researching`, `upgrading`, `lastCommand`...), 89 methods (mostly trivial getters),
fluent `setXxx` mutators, `attackUnit`/`move`/`holdPosition` recording into `lastCommand`, and an id counter.

**SRP break.** (a) It is a *stub engine unit*, a *scenario builder*, and a *test oracle* (`lastCommand()`,
`lastTechUsed`) at once. (b) Every field is `public`, so any test writes any state — there is no invariant and no
single place to update when `AUnit`'s contract changes. (c) `getAngle`, `effVisible`, `isPatrolling`, etc., are
hand-written per-method overrides duplicating what a default stub could do.

**Proposed changes.**
- (P1) Make the state a `FakeUnitState` value object (fields private with getters/setters usedby the override
  methods); keep the fluent `with...` builders on `FakeUnit`. Tests then express scenarios through one API
  (`unit.withHp(20).withTarget(...)`). (SRP + encapsulation.)
- (P2) Separate the *recording* role into `OrderRecorder` (`lastCommand`, `lastTechUsed`, attack/move records) —
  assertions then read the recorder, not the stub. (SRP — this is why tests currently check `lastCommand`
  strings.)
- (P3) Consider *one* `FakeUnit` for everything (current approach) vs `FakeCombatUnit`/`FakeBuilding`; given
  the breadth of engine flags, a single configurable stub is acceptable if state is encapsulated.

---
## 21. `src/atlantis/Atlantis.java` — 492 linie

**What it does.** The BWAPI `DefaultBWListener`/`Atlantis` singleton: every engine callback
(`onStart`, `onFrame`, `onUnitCreate/Complete/Destroy/Discover/Evade/Hide/Morph/Show/Renegade`, `onEnd`,
`onSendText`, ...), game bootstrap (`setBwapiFlags`, `unpauseIfPaused`), process teardown (`killProcesses`,
`exitGame`), plus **global scoreboard counters** (`KILLED`, `LOST`, `KILLED_RESOURCES`, ...) as public statics.

**SRP break.** (a) Listener adapter, (b) lifecycle manager, (c) OS process killer, (d) global scoreboard state.
The counter statics are the worst: any unit-death code mutates `Atlantis.KILLED`, so "who updates the score" is
spread everywhere and the class becomes a global mutable holder (breaks DIP and makes tests order-dependent).

**Proposed changes.**
- (P1) Extract `GameScoreboard` (killed/lost counters + resources) as an object; `Atlantis` forwards
  `onUnitDestroy` to it. Callers that today do `Atlantis.KILLED++` call `GameScoreboard.recordKill(unit)`.
  (SRP; removes mutable globals.)
- (P1) Extract `GameProcessTeardown` (`killProcesses`, `exitGame`) — OS concerns have nothing to do with the
  BWAPI adapter. (SRP.)
- (P2) Delegate the unit callbacks: `onUnitDestroy` etc. contain logic (fog/archive updates). Move each to the
  subsystem that owns it (`UnitsArchive`, `FoggedUnits`, `GameCommander`) and keep `Atlantis` as a thin adapter
  that forwards. (SRP + OCP — new engine events don't edit the class that also owns the score.)
- **Keep** the singleton/`getInstance` entry point: the engine requires a stable listener instance.

---
## 22. `src/atlantis/battle/FightTarget.java` — 310 linii

**What it does.** A static "one engaged frame" pipeline: `fight()` (contact snapshot, ghost logging, holdSwing,
makeWay, kite, reengage, rearm) with **six static `Map<unitId, ...>`** memory tables (`lastStepFrame`,
`lastMovedFrame`, `lastMovedPos`, `lastRepositionFrame`, `lastContactPos`, `lastGhostFrame`), and helper methods
per phase.

**SRP break.** (a) Combat decision pipeline, (b) per-unit combat memory, (c) `clearForTests()` lifecycle of that
memory. Because the memory is static and keyed by unit id, tests must call `clearForTests()` and two bot
instances could never coexist. The pipeline methods are already nicely separated — the problem is the state.

**Proposed changes.**
- (P1) Extract `DuelMemory` (the six maps, `reset()`, `lastContact(id)`, `markRepositioned(id)`, ...) as an
  instance; `FightTarget` receives it (constructor or per-call argument) and keeps only the frame pipeline.
  (SRP — separate "what we do this frame" from "what we remember about a unit".)
- (P2) Name the pipeline steps as a small list of `FightStep` objects (`HoldSwing`, `MakeWay`, `KiteThreat`,
  `Reengage`, `RearmAttack`), each returning `handled`; `fight()` iterates. New tactics then don't edit `fight()`.
  (OCP.)
- (P2) `GhostTargetLogger` for the "fighting an invisible target" block (currently inline with `EventLog`).
  (SRP.)

---
## 23. `src/atlantis/units/select/Count.java` — 506 linii, 98 metod

**What it does.** "How many do we have / are coming": `ofType*`, `existingOrInProduction(OrInQueue)`,
`inQueue(...)`, `inProduction(...)`, `existing(...)`, plus 60+ unit-specific shortcuts
(`dragoons`, `zealots`, `pylons`, `bunkers`, `turrets`, `sunkens`, `freeGateways`, `gasBuildings`...).

**SRP break.** (a) The **counting semantics** (what counts as existing vs in production — including race-specific
Zerg morph rules in `inProduction`, lines 114–147, and Protoss primary-base rules), (b) a **catalogue** of type
shortcuts, (c) a cache (`clearCache`). The type shortcuts are pure noise (one line each) but 60 of them hide the
three real rules. The Zerg/Protoss branches inside `inProduction`/`existing` are race policy inside a counting
utility (OCP: a new race rule edits this file).

**Proposed changes.**
- (P1) Extract `TypePresence` (existing/inProduction/inQueue per type) with a **race hook**
  (`RacePresencePolicy` implemented by `ProtossPresence`, `ZergPresence`, `TerranPresence`), so the Zerg
  Creep-Colony/Sunken/Spore and Lair/Lair/Hive rules live in the Zerg policy, not in an `if (We.zerg())`.
  (OCP + SRP.)
- (P1) Generate the one-line shortcuts (or move them to a `Have`-style facade) — `Count` keeps only the four
  counting primitives. (SRP: "count" vs "name of a common type".)
- (P2) `countUnitsBeingProduced`, `notBeingProduced` etc. are the same question asked differently; unify behind
  the primitives to remove duplicated `Select...count()` compositions.

---
## 24. `src/atlantis/production/orders/production/queue/add/AddToQueue.java` — 381 linii

**What it does.** The queue's write API: ~14 `withXxxPriority(...)` overloads, `tech()`, `upgrade()`,
`toHave(type[, count[, priority]])`, `maxAtATime(...)`, `addToQueueIfNotAlreadyThere`,
`addToQueueIfHaveFreeBuilding`, and the private `addToQueue(...)` (≈130 lines) that computes min supply,
prevents excessive orders, throttles Nexus queueing (`lastNexusQueueFrame`), and coordinates with duplicates.

**SRP break.** (a) **Priority API surface** (14 nearly identical overloads that just build a `ProductionOrder`),
(b) **insertion policy** (min supply, excessive-order prevention, Nexus cooldown), (c) **duplicate prevention**
(delegated to `PreventDuplicateOrders`, partly), (d) a static throttle counter. Adding a new "priority + position"
combination means another overload (OCP).

**Proposed changes.**
- (P1) One builder/parameter object: `AddToQueue.of(type).near(pos).priority(HIGH).atMost(n).toHave()`. This
  removes ~10 overloads and makes the call sites read as intent. (SRP + OCP.)
- (P1) Extract `MinSupplyPolicy` (`defineMinSupplyForNewOrder`) and `NexusQueueThrottle`
  (`lastNexusQueueFrame`) — both are independent rules with independent state, and the throttle is a static that
  tests must clear. (SRP.)
- (P2) Merge `canPreventExcessiveOrders` with `PreventDuplicateOrders`; today "excessive" and "duplicate" are split
  across two classes that both decide whether an order is admitted. (SRP — one admission policy.)

---
## 25. `src/atlantis/production/v2/LegacyPlacementPlanner.java` — 359 linii

**What it does.** The old `PlacementPlanner` implementation: `choose()` among candidates (via a swappable
`CandidateResolver`), `reservePlacement` (≈80 lines finding a `bestPoweredSite`, marking tiles, returning a
`PlacementReservation`), `carriedOver`, `twinRefusal`/`topRefusal` diagnostics, and helpers
(`sameTile`, `containsTile`, `isPoweredBy`, `resolveNear`, `areaCentre`).

**SRP break.** (a) Planner protocol (`startPass`, `reservePlacement`, `carriedOver`), (b) **power-site geometry**
(`POWER_RING`, `isPoweredBy`, `bestPoweredSite`), (c) **tile-set utilities** (`sameTile`, `containsTile`) that
duplicate grid/catalogue logic used by the new planner, (d) refusal diagnostics.

**Proposed changes.**
- (P1) Extract `PylonPowerSites` (POWER_RING + isPoweredBy + bestPoweredSite) so both planners use one definition
  of "powered site" instead of two divergent ones (the catalogue planner has `PsiGating`/`EnginePowerSource`).
  (DRY + SRP.)
- (P2) Move `sameTile`/`containsTile`/`areaCentre`/`resolveNear` into `TargetPlacement`/`PlacementGeometry`
  shared by both planners; then `LegacyPlacementPlanner` is only the legacy *selection order*. (SRP.)
- (P2) Since this is explicitly the legacy path, mark it `@Deprecated` and keep it behind the `PLACEMENT=` flag —
  the SOLID goal here is *containment*, not polish.

---
## 26. `src/atlantis/production/dynamic/workers/AutoProduceWorkersCommander.java` — 365 linii

**What it does.** A `Commander` whose `handle()` asks `shouldProduceWorkers()` (≈110 lines of gates: early flow,
supply-9 mandate, saturation, minerals, `REASON` bookkeeping) and then `produceWorker(base)` /
`queueWorker()`/`canQueueWorkerAt` with `shouldQueueAnotherWorker(remainingFrames, alreadyQueued)`.

**SRP break.** (a) Commander wiring, (b) **worker-production policy** (a long list of independent gates), (c) base
selection/queueing mechanics, (d) a static `REASON` string as an out-of-band result channel. The gates are
sequential `if (x) return dont("...")` — the readable version of the same debt seen in `ProduceZealot`.

**Proposed changes.**
- (P1) Extract `WorkerProductionPolicy` returning a **decision object** (`produce | don't(reason) | queue(count)`),
  with each gate a named private method or `Gate` (the codebase already has `BuildGates` — reuse that shape).
  The commander becomes 10 lines. (SRP.)
- (P2) Run the policy **before** the commander and feed it a small `EconomySnapshot` (workers, bases, supply,
  minerals, train frames), so the policy is testable without `Select`/`A`. (DIP.)
- (P3) `public static String REASON` → field on the decision object. (SRP.)

---
## 27. `src/atlantis/util/ProcessHelper.java` — 386 linii

**What it does.** Kills and starts StarCraft/ChaosLauncher/OpenBW on Windows, Wine and wine-host; writes `wmode.ini`,
positions the Wine window (`wmctrl`), reads process output, executes detached commands
(`executeDetached`, `executeInCommandLine`, `executeInCommandLineQuietly`, `executeInCommandLineDetached`),
and has an inner `Runnable` for `run()`.

**SRP break.** (a) **Platform strategy** (Windows `taskkill` / Linux `pkill` / OpenBW), (b) **command execution
primitives** (5 variants), (c) **Wine-specific configuration** (`writeWModeIni`, `positionWineWindowWhenReady`,
`displayEnv`, `wineGameRoot`). Every new launcher mode edits several methods with the same `if (Env.isWine()) /
if (Env.isOpenBW())` prefix — an OCP violation repeated 4×.

**Proposed changes.**
- (P1) Introduce `ProcessControl` with implementations `WindowsProcessControl`, `WineProcessControl`,
  `OpenBWProcessControl` (the codebase already has `ChaosGameLauncher`/`UnixChaosGameLauncher` performing
  exactly this selection). Choose once in a factory, then `killGame()`/`killLauncher()`/`startLauncher()`
  lose all the platform branches. (OCP + SRP.)
- (P1) Extract `Shell` (`execute`, `executeDetached`, `executeQuietly`, `readAll`) — one responsibility used by
  everything. (SRP.)
- (P2) Wine window management (`wmctrl` polling, `wmode.ini`) → `WineWindowConfig` (the class already exists and is
  imported!). (SRP — console/OS windowing is unrelated to game processes.)

---
## 28. `src/atlantis/production/constructions/builders/RefreshConstructionPosition.java` — 313 linii

**What it does.** Decides whether the site of an existing `Construction` should be re-found:
`refreshIfNeeded` (skips gas/base/waiting), `refreshPosition` (never moves a committed builder; clears caches;
re-finds; syncs the order's exact position), `finalRefreshBeforeIssuingOrderFailed` (cancels + re-queues when the
builder stands still), `shouldRefreshConstructionPosition`, `completedBuildingOverlaps`,
`builderHasCommittedToTheSite`, `doNotRefreshPosition`.

**SRP break.** (a) **Refresh policy** ("may the site move now?"), (b) **site mutation** (re-find + cache clears +
order sync), (c) **cancel/re-request policy** (the `everyNthGameFrame(97)` + cancel + top-priority re-add), (d)
geometry (`completedBuildingOverlaps`). The class name promises one thing; it does four.

**Proposed changes.**
- (P1) Split into `SiteRefreshPolicy` (pure booleans: builder committed, status waiting, type exempt) and
  `SiteRefresh` (the mutation + order sync). The policy is easy to unit-test with a mock construction; today the
  tests must pin the contract through a live-ish object. (SRP.)
- (P1) Move the "can't build here → cancel → re-request" block into `CancelTooLongConstructions` (or a
  `BuildFailureRecovery`), which already owns cancel+re-request. (SRP — one owner for recovery.)
- (P3) `completedBuildingOverlaps` → `BuildingFootprint` shared with `ConstructionStatusChanger` (see item 5).

---
## 29. `src/atlantis/map/AMap.java` — 395 linii

**What it does.** Static map facade: `initMapAnalysis()` (BWEM bootstrap + failure handling + verification,
`handleBwemFailure`, `verifyMapAnalysisIsUsable`), size/name accessors, random position pickers
(`randomInvisiblePosition`, `randomUnexploredPosition`), walkability search
(`getMostWalkablePositionNear`, `tileWalkabilityScore`), and `allChokeCenters()`.

**SRP break.** (a) **Analysis bootstrap & health checks** (exit codes, BWEM missing/empty), (b) **map queries**
(size, name, file name), (c) **search/sampling algorithms** (random invisible position, most walkable tile,
choke centres). The bootstrap half is startup-only and error handling; the query half is used every frame. They
share only a `bwem` static.

**Proposed changes.**
- (P1) Extract `MapAnalysisBootstrap` (init, BWEM failure handling, verification, failure exit codes) — it runs
  once and should not be reachable from gameplay code. (SRP.)
- (P2) Extract `MapSampling` (`randomInvisiblePosition`, `randomUnexploredPosition`, `getMostWalkablePositionNear`,
  `tileWalkabilityScore`, `allChokeCenters`) — these are algorithms over the map, currently bloating the facade.
  (SRP.)
- (P2) `project/main` should own the decision to `System.exit(42/43)`; a map class returning failure codes couples
  it to the process lifecycle. (SRP — failure policy belongs to the entry point.)

---
## 30. `src/atlantis/map/base/BaseLocations.java` — 418 linii

**What it does.** Queries over base locations: starting/expansion discovery (`nearestUnexploredStartingLocation`,
`expansionFreeBaseLocationNearestTo`, `baseLocationLooksFree`), natural/third/main helpers (`natural()`, `main()`,
`enemyMain()`, `enemyNatural()`, `enemyThird()`, `hasBaseAtNatural`, `bunkerAtNatural`), plus
`mainNaturalAndExistingBases`/`mainAndNatural`.

**SRP break.** (a) **Base-location catalogue** (all/non-starting/starting), (b) **free/occupancy policy**
(`baseLocationLooksFree` — a real rule mixing visible units, enemy units and fog), (c) **"our base identity"**
(`main()`, `natural()` — duplicated in `Select.main()`), (d) **enemy-base inference** (`enemyMain`, `enemyNatural`,
`enemyThird`) which is scouting knowledge, not map geometry.

**Proposed changes.**
- (P1) Extract `OurBaseIdentity` (`main`, `natural`, `third`, `mainOrNaturalPositions`) and have both
  `Select.main()` and this class delegate — one definition of "our main". (DRY + SRP.)
- (P2) Extract `EnemyBaseKnowledge` (`enemyMain`, `enemyNatural`, `enemyThird`) into `information.enemy`; it is
  inferred from what we have seen, not from the map. (SRP.)
- (P2) `baseLocationLooksFree` is a policy used by expansion decisions → `ExpansionSites.isFree(base)`, testable
  with a stub selection. (SRP.)

---
## 31. `src/tests/unit/sim/SimWorld.java` — 381 linii

**What it does.** A miniature physics/combat simulator for tests: holds ours/enemies/arena, installs a fake
`Selection`, ticks the world (`tick(Runnable decisions)` → `moveAll`, `combatAll`), movement with acceleration,
braking and turn limiting (`desiredVelocity`, `toward`, `topSpeed`, `limitTurn`, `step`, `publish`), firing and
damage (`fire`, `reachTiles`), and health checks (`aliveOurs`, `stuckFrames`, `maxStuckFrames`).

**SRP break.** (a) **World assembly/setup** (`installSelection`, `close`), (b) **kinematics** (`step`, `limitTurn`,
`publish`), (c) **combat model** (`fire`, `isAlive`), (d) **metrics** (`shotsFired`, `aliveOurs`, `stuckFrames`).
Reasonable for a test harness, but it is the shared base of all simulation tests, so any change to movement math
touches the same class as combat and metrics.

**Proposed changes (P3, test-only).**
- Extract `SimKinematics` and `SimCombat` with a `SimState` (units + arena + frame). `SimWorld` then orchestrates:
  `tick` → move → combat → metrics. Cleaner, and lets a future test reuse only the combat model.
- Keep the single public entry (`tick`) — it is a good seam.

---
## 32. `src/atlantis/units/workers/defence/run/WorkerDefenceRun.java` — 390 linii

**What it does.** A worker `Manager`: `applies()` (hold-ground, early-game and hp gates), `handle()` dispatching to
`helpWoundedFriend()` or a per-enemy-type branch (`runFromZealots`, `runFromDragoons`, `runFromReaver`,
`runFromCarrier`; `runFromMassLings`, `runFromMutas`), plus `isEligibleHelper`, `isTheChosenHelper`, `attackerOf`,
`victimToHelp`, `runFromEnemyToAnotherRegion`.

**SRP break.** (a) **When to flee** (policy with many thresholds), (b) **whom to help** (helper election),
(c) **how to flee** (movement), (d) **per-enemy-type tactics** — a growing `if (Enemy.protoss()) / if (Enemy.zerg())`
block (OCP: every new harassment unit edits this class). The long javadoc at `helpWoundedFriend` explains why it
lives here (architecture test freezes manager constructor references) — that is an external constraint, not a
design argument.

**Proposed changes.**
- (P1) Turn the per-enemy branches into `ThreatResponse` objects (`ZealotThreat`, `DragoonThreat`, `MassLingsThreat`,
  `MutaliskThreat`, `ReaverThreat`, `CarrierThreat`) registered in a list, each with `boolean applies(unit)` and
  `handle(unit)`. Adding a threat stops editing the manager's `handle()`. (OCP + SRP; the architecture rule is kept
  because the manager list stays frozen — only *inside* it changes.)
- (P2) Extract `WoundedFriendHelp` (`helpWoundedFriend`, `victimToHelp`, `attackerOf`, `isEligibleHelper`,
  `isTheChosenHelper`) into a collaborator passed to the manager; the manager then composes policy objects.
  (SRP.)
- (P2) Constants (`EARLY_GAME_HP_ENOUGH`, `HOLD_GROUND_MIN_HP`, ...) are context-dependent tuning — move next to
  `defence` tuning so all defence numbers are in one place. (SRP.)

---
## 33. `src/atlantis/production/v2/ProductionScheduler.java` — 317 linii

**What it does.** Schedules goals onto a resource timeline: sorts goals, `ensurePrerequisites` (recursive, cycle-cut),
`alreadyAvailableOrComing`, `scheduleItem` (affordability + producer choice), `commit`,
`chooseProducer`, `freeFrom`, `facilities(typeId)`, plus **eight mutable per-pass maps** reset in `schedule()`
(`busyUntil`, `facilitiesByType`, `plannedAvailable`, `consumed`, `plannedBuildingGoals`, `nextPlannedFacilityId`).

**SRP break.** The class is mostly well-factored (pure, no BWAPI). The remaining problem is that the per-pass state
makes "scheduler" mean both *algorithm* and *mutable pass workspace*; `schedule()` must reset all eight maps in the
right order, and concurrency/reentrancy is impossible. Prerequisite recursion also carries its own `HashSet` pass
parameter through four methods (tramp data).

**Proposed changes (P2).**
- Extract `SchedulingPass` holding the 8 maps + `ProductionPlan` + `timeline`; constructed fresh in `schedule()`.
  Methods become `pass.scheduleGoal(goal)` — no manual reset, no hidden ordering. (SRP.)
- Make the prerequisite recursion a small `PrerequisiteResolver` with its visiting set as a field of the pass,
  so the `new HashSet<>()` parameter disappears from the call chain. (SRP + cleaner interface.)
- `buildingGoalKey` (string concatenation key) → a `BuildingGoalKey` record with `equals/hashCode`; string keys are
  a bug source. (SRP.)

**Keep** the purity — this is the cleanest production class on the list; changes here are cosmetic.

---
## 34. `src/atlantis/production/constructions/position/protoss/ProtossPositionFinder.java` — 307 linii

**What it does.** Protoss building placement: `findStandardPositionFor(builder, building, nearTo, maxDistance)`
(≈150 lines of search with conditions), `needsPylonPower`, `firstEngineBuildableTile`,
`findWithRelaxedBoundaryMargin`, `lastFoundPosition`, `lastRefusalBreakdown`.

**SRP break.** (a) **Search strategy** (candidate generation, distance relaxation), (b) **race rule** (pylon power),
(c) **static result state** (`relaxedBoundaryMargin`, `lastFound`, refusal breakdown) used as an out-of-band
return channel. The class already documents the deliberate decision to keep pylon power in the Protoss layer —
that is correct SRP; the problem is the static mutable cache/results.

**Proposed changes.**
- (P1) Replace the statics with a returned `PlacementSearchResult { position, refusalBreakdown, usedRelaxedMargin }`;
  callers (`DefineExactPositionForNewConstruction`) consume the object. Removes cross-call state leaking between
  orders and tests. (SRP.)
- (P2) Extract `PylonPowerRule` (needsPylonPower + the two long javadoc blocks) as a named rule object; the finder
  then takes a `PowerRequirement` predicate. (SRP — the race rule is then reusable by the catalogue planner's
  `PsiGating`.)
- (P2) The search itself → `BoundaryRelaxingSearch` shared with other race finders. (DRY.)

---
## 35. `src/atlantis/production/v2/engine/GameStateSnapshot.java` — 328 linii

**What it does.** The BWAPI bridge for the v2 production layer: builds a `ResourceTimeline`
(`buildTimeline`, `applyCommittedWork`), a `ProducerFacilityRegistry`, `ExistingItems`, `dynamicGoals()`,
and helpers `supplyProvidersComing`/`unstartedProvidersComing`/`unmaterializedInProgressComing`,
`supplyProviderType`, `workerType`, `armyType`, `cannonsAtMain`, `pullForwardGoals`, `buildOrderGoals`.

**SRP break.** It is deliberately the one bridge (documented) — that is acceptable SRP-wise for *engine access*.
But it also contains **domain policy**: `supplyProvidersComing(provider)` and friends encode "what counts as
coming" (unstarted providers vs unmaterialized in-progress), and `armyType()`/`supplyProviderType()` encode race
mapping. Those are rules, not engine reads; today they are static and untestable without a game.

**Proposed changes.**
- (P1) Move `supplyProvidersComing` / `unstartedProvidersComing` / `unmaterializedInProgressComing` into
  `ExistingItems`/`InFlight` (pure, testable) and let the snapshot call them. The "guarantee" can then be tested
  with the same harness as `ProductionSchedulerTest`. (SRP + DIP.)
- (P2) Race mapping (`supplyProviderType`, `workerType`, `armyType`) belongs to `ProductionRacePolicy` — the class
  already receives it; use it instead of local `We.protoss()`-style branches. (OCP.)
- **Keep** `buildTimeline`/`facilityRegistry` as the engine boundary; it is the class's stated single job.

---
## 36. `src/atlantis/production/constructions/builders/BuilderDiagnostics.java` — 268 linii

**What it does.** Pure diagnostics for build attempts: `logBuildAttempt` (why the order failed),
`logStandstillBreakdown` (why the builder stands still: units in footprint, terrain, explored footprint).

**SRP break.** None serious — it is one responsibility (explain a failed build) and it is already pure/static.
The only smell: `unitsInFootprint` takes `bwapi.Unit` (lowest level) while the rest work on `Construction`/`AUnit`,
so the class mixes two abstraction levels.

**Proposed changes (P3).**
- Accept `AUnit`/`HasPosition` instead of raw `bwapi.Unit` so the file has one abstraction level; `builder.u()`
  stays in the caller. (DIP-lite: don't depend on the bridge where unnecessary.)
- **Keep the shape** otherwise.

---
## 37. `src/atlantis/production/constructions/commanders/CancelTooLongConstructions.java` — 315 linii

**What it does.** Cancels stalled constructions: `cancelCauseTakingTooLongIfNeeded`, timeout computation
(`timeoutInSeconds`, `tookFrames`, `tookSeconds`, `tookTooLong`), replacement logic
(`tryReplaceWithFreshSite`, `shouldReplace`, `isDifferentSite`), re-queue veto
(`constructionCancelledRequestAgainBecauseItsImportant`, `techAlreadyCovered`, `shouldVetoRequeue`,
`countTriggerableNotStarted`, `maxAllowedAtOnce`), and `forceRescueOrder`.

**SRP break.** (a) **Timeout policy** (how long is too long per building), (b) **replacement policy** (new site vs
same site), (c) **re-request policy** (importance, tech already covered, max at once), (d) order creation.
Four policies in one utility. The class is static, so none of them is injectable/testable in isolation.

**Proposed changes.**
- (P1) Extract `BuildTimeout(buildingType)` returning the limit, and `RequeuePolicy` (importance/covered/limit).
  Both are pure given `Construction` + counts. (SRP.)
- (P2) `tryReplaceWithFreshSite` + `forceRescueOrder` + `AddToQueue.withTopPriority(...)` = one `ConstructionRecovery`
  use case, which is also the natural owner of the "can't build here" recovery currently in
  `RefreshConstructionPosition` (item 28). (SRP — one recovery path.)
- **Keep** it static if the whole production layer is static; priority P2 after the split.

---
## 38. `src/atlantis/map/position/HasPosition.java` — 431 linii, 62 default methods

**What it does.** Geometry/position interface: translation (`translate*Towards`, `translateBy*`), validity
(`makeBuildable`, `makeWalkable`, `makeLandableFor`, `ensureWithinBounds`), distance (`distTo*`, `groundDist*`),
map queries (`isWalkable`, `isExplored`, `isCloseToMapBounds`, `distToMapBorders`, `isInMapCorner`), regions
(`regionsMatch*`), **painting** (`paintCircle`, `paintLine`, `paintRectangle`, `paintTextCentered`), comparison
(`equals`, `compareTo`), and `nearestPositionFreeFromUnits`.

**SRP break.** The interface has five responsibilities. The most acute: **debug painting** (10 methods) lives on a
geometry abstraction, pulling `AAdvancedPainter`/`bwapi.Color` into every implementor's compile classpath; and
`isWalkable/isExplored/region()` make position depend on `AMap`/`Select` (static globals).

**Proposed changes.**
- (P1) Move painting to `Painter` extension methods (`Painter.circle(HasPosition, ...)`) or a
  `PaintablePosition` mixin used only in debug code. Geometry stops knowing about drawing. (SRP — the biggest
  and cheapest win here.)
- (P2) Split map-dependent predicates (`isWalkable`, `isExplored`, `region`, `nearestChoke`,
  `regionsMatch`) into a `MapPositionQueries` service taking a map handle; the interface keeps only pure
  arithmetic (`translate*`, `distTo`, `tx/ty`, `compareTo`). (DIP — no hidden static map lookup.)
- (P2) `equals(HasPosition)`/`compareTo(HasPosition)` as default methods on an interface are a trap (not the
  `Object.equals` contract). Make them named (`samePointAs`, `orderByPoint`) or implement them in `APosition`.
  (SRP/LSP — an interface should not silently provide equality semantics.)
- **Keep** the pure translation/distance defaults — they are the useful part.

---
## 39. `src/atlantis/production/constructions/ConstructionRequests.java` — 388 linii

**What it does.** The global construction registry (`ConcurrentLinkedQueue<Construction> constructions`) plus 29
query/mutation methods: counts by status/type/radius (`countNotStartedOfType`, `countNotFinishedOfTypeInRadius`,
`countPendingOfType`, `countExistingAndNotFinished`...), lookups (`getNotStartedOfType`, `constructionFor`),
listing (`notStarted`, `notFinished`, `all`), removal (`removeOrder`, `removeAllNotStarted`), duplicate check
(`alreadyExists`), resource sum (`resourcesNeededForNotStarted`).

**SRP break.** (a) **Storage** (public mutable queue), (b) **query model** with 12 near-duplicate counting methods
(status × type × radius × existing-planned), (c) **mutation policy** (`alreadyExists`, `removeAllNotStarted` with
reason), (d) start-up/resource accounting. The public field lets anyone mutate the registry (encapsulation broken);
the 12 counters are one parametrised query expressed 12 times (OCP).

**Proposed changes.**
- (P1) Make the collection private, expose `snapshot()`, `add`, `remove`, `clear`; callers (many) get a read-only
  view. (SRP + encapsulation.)
- (P1) Replace the count matrix with a `ConstructionQuery` object:
  `query().ofType(t).status(NOT_STARTED).inRadius(r, pos).count()`; delete the 12 methods (and `isNotStarted`).
  (OCP + DRY — a new combination no longer needs a new method.)
- (P2) `alreadyExists` (duplicate policy) → `DuplicateConstructions` used by `NewConstructionRequest` and
  `AddToQueue`, one owner for "is this construction a duplicate". (SRP.)

---
## 40. `src/atlantis/units/Units.java` — 501 linii

**What it does.** A `List<AUnit>` + per-unit `Double` values (a light multimap used for clustering/weights):
mutation (`addUnit`, `addUnitWithValue`, `removeUnit(s)`, `replaceUnitsWith`, `incrementValue`, `changeValueBy`,
`setValueFor`), extremes (`unitWithLowestValue`, `unitWithHighestValue`, `lowestValue`), sorting
(`sortByDistanceTo`, `sortByGroundDistTo`), aggregation (`median`, `medianUnit`, `average`,
`averageDistanceWeightedTo`), collection predicates (`onlyRanged`, `onlyMelee`, `onlyAir`, `onlyGround`,
`onlyWorkers`), and printing.

**SRP break.** It is simultaneously a **collection**, a **weighted map**, a **sorter**, an **aggregator** and a
**printer**. The weighted-map API (`valueFor*`, `incrementValue`) exists for clustering; the rest is generic
collection behaviour duplicating `Selection`.

**Proposed changes.**
- (P1) Split the weighted aspect into `WeightedUnits` (unit → value with extremes); `Units` keeps rich list
  behaviour (`median`, `average`, sorting, predicates). (SRP.)
- (P2) `onlyRanged/onlyMelee/...` duplicate `Selection`'s; keep one implementation and let `Units` delegate.
  (DRY.)
- (P3) `print()/toString()` → `UnitsDebug`. (SRP.)

---
## 41. `src/atlantis/production/requests/produce/BuildGates.java` — 303 linie, 12 nested classes

**What it does.** A **good example to copy**: the refusal rules for spawning a building are declared as 11 separate
`BuildGate` implementations (`BelowMinSupplyGate`, `LiveConstructionGate`, `CoreSingleInstanceGate`, `PylonCapGate`,
`PylonOverstockGate`, `UnstartedPylonGate`, `WarpingLinkGate`, `EarlierReserveGate`, `FundingGate`,
`CorePriorityGate`, `PowerNotFundedGate`), each with `name()` + `check(type, order) → refusal reason or null`,
and one `all()` list.

**SRP status.** Each gate has one responsibility and one reason to change — this is the pattern that
`ProduceZealot`, `AutoProduceWorkersCommander` and `ConstructionStatusChanger` should adopt. The remaining smells
are: all 12 classes live in one file, and `all()` is a hard-coded list (adding a gate edits the list — acceptable,
but a registry per race/stage would be more OCP).

**Proposed changes (P3).**
- One file per gate (or a `gates` package) so each class is independently named and reviewed; `all()` becomes a
  static import of `BuildGateRegistry.ALL`.
- Expose the gate list as a dependency of the producer (`new BuildingRefusalChain(BuildGates.all())`) rather than
  a static, so V2 could plug its own chain. (DIP.)
- **Keep the shape**: this is the reference design for splitting the long `if`-chains elsewhere in this report.

---
## 42. `src/atlantis/map/position/APosition.java` — 418 linii

**What it does.** The pixel-position value object: constructors and `create(Object)` (accepts `Position`,
`HasPosition`, `WalkPosition`, `TilePosition`, `ChokePoint`), distances (`distTo`, `groundDistanceTo`,
`distTo(AChoke)`, `distTo(AUnit)`), normalisation (`makeValidGroundPosition`,
`makeBuildableGroundPositionFarFromBounds`), vector arithmetic (`subtract/add/divide/multiply`), equality,
`hasPathTo`, `region()`, `isHighGround`, neighbours (`left/right/top/bottom`), `isOutOfBounds`.

**SRP break.** Mostly a clean value object. Two leaks: (a) **`create(Object)` + instanceof chain** — a factory that
knows every other position representation (should be overloads or a `PositionFactory`, otherwise the value type
grows with each new representation — OCP); (b) **map-aware queries** (`groundDistanceTo`, `hasPathTo`, `region`,
`isHighGround`, `makeValidGroundPosition`) make a *value* depend on the live map (`AMap`/`Paths`), so it cannot be
used as a plain value in tests or in the pathfinding layer. (c) `left/right/top/bottom` semantics are ambiguous
(tile vs pixel?).

**Proposed changes.**
- (P1) Replace `create(Object)` with explicit typed factories (`APosition.of(Position)`, `of(TilePosition)`,
  `of(WalkPosition)`); the `instanceof` chain disappears. (OCP.)
- (P2) Move map-dependent methods to `MapPosition`/`PathQueries` (same direction as item 38), leaving `APosition`
  a pure immutable value with `x`, `y`, arithmetic, `dist` (Euclidean only). (SRP + DIP.)
- (P3) Make `p` final (it is assigned in `readObject`; a final-holder pattern or a `Serializable` proxy keeps
  immutability). (SRP — value object invariants.)

---
## 43. `src/atlantis/game/A.java` — 434 linie, 62 static methods

**What it does.** The global "A" god-utility of the bot: date/time formatting (`getCurrentDateInFormatYMDHHmm`,
`dateYear`...), game clock (`now`, `seconds`, `ago`, `secondsAgo`, `everyNthGameFrame`, `setNow`), resources
(`minerals`, `gas`, `supply*`, `hasMinerals`, `hasGas`, `canAfford`, `reservedMinerals`), map/umu flags
(`isUms`, `notUms`), formatting (`digit`, `dist`, `trueFalse`, `ucfirst`, `substring`), collection helpers
(`getOccurenceMap`, `sortByValue`, `countSubstrings`), process (`sleep`, `quit`).

**SRP break.** At least **six** unrelated responsibilities. It is the most-depended-on class in the codebase, so
any change ripples everywhere; it also wraps `AGame` (which tests mock), making the seam fragile.

---
## 44. `src/atlantis/production/constructions/builders/IssueBuildOrder.java` — 226 linii, 3 methods

**What it does.** `considerIssuingNow(construction)` (≈195 lines): a debug `EventLog` block, a block of
`System.err.println` calls, affordability check, position validation, the actual `build(...)` call, and
`moveOtherUnitsOutOfConstructionPlace`.

**SRP break.** Nearly the whole class is one method that mixes **logging/diagnostics**, **validation**,
**command issuing** and **unit displacement** (moving our own units off the footprint). The `System.err.println`
block (lines 43–51) is unconditional debug output in a production path.

**Proposed changes.**
- (P1) Remove the unconditional `System.err` block (or gate it with `Env.builderLog()` and route it through
  `EventLog` like the first block). (SRP — diagnostics should be one channel, not two.)
- (P1) Extract `FootprintClearing` (`moveOtherUnitsOutOfConstructionPlace`) — a distinct behaviour with its own
  policy. (SRP.)
- (P2) Extract `BuildPreconditions` (affordability, position null/valid, builder on site) returning a reason; the
  method becomes *log → preconditions → build → clear footprint*. (SRP.)

---
## 45. `src/jfap/JFAP.java` — 340 linii

**What it does.** A combat simulator: `simulate(nFrames)` loop, `unitSim` (targeting, kiting step, moving,
shooting, elevation penalty), `medicsim` (heal), `suicideSim` (Scourge/Interceptors), `suicideSim`/`dealDamage`
(damage types, shields), `unitDeath`, scoring (`score`, `playerScores*`), `isSuicideUnit`, `updateUnit`.

**SRP break.** (a) Simulation **loop/turn order**, (b) **per-behaviour rules** (`unitSim`, `medicsim`, `suicideSim`
duplicate the "find closest enemy → step → shoot" skeleton three times), (c) **damage model**, (d) **scoring**.
The triplication of the targeting/step/shoot skeleton is the main maintenance risk (a fix in `unitSim` must be
replicated in `suicideSim`).

**Proposed changes.**
- (P1) Extract `ClosestEnemy` (target selection per damage type) and `CombatStep` (advance / hold / shoot), used by
  all three behaviours; `unitSim`, `medicsim`, `suicideSim` become short behaviour methods. (DRY + SRP.)
- (P2) `DamageModel` (shield/armor/damage-type table) as a pure object — it is the part most worth unit-testing
  against BWAPI values, and it is currently entangled with the movement code.
- (P3) `playerScores*` (three variants over the same loop) → `score(PlayerFilter)`.

---
## 46. `src/atlantis/production/dynamic/protoss/units/ProduceZealot.java` — 305 linii

**What it does.** `zealot()` (≈70 lines): a long sequence of `if (...) return produceZealot("Reason")` /
`return false` decisions, each with a threshold (minerals 600/550/560/530/350/300/250, gas 60/50/17/200,
supply 25/30/50/180/185, counts 5/7/10/12/13), interleaved with delegation to helpers (`ZealotInsteadDragoon`,
`PreventExcessiveZealots`, `Prioritize...`, `earlyGameZealots`, `Decisions`, `ProduceDragoonInsteadZealot`), plus a
static `reason`.

**SRP break.** **One method implementing ~25 independent rules** in order. Order matters implicitly (each rule sees
the state after the previous rules did not fire), so the rules are not composable, not individually testable and
not documented as a set. Every balance change edits this method (OCP).

**Proposed changes (P1 — template: `BuildGates`).**
- Turn each rule into a `ZealotRule { Decision decide(ZealotState) }` returning
  `Produce | Wait(reason) | Neutral`. Keep them in one ordered list. `zealot()` = first non-neutral decision.
  The behaviour is identical (same order, same thresholds) but each rule is named and testable. (SRP + OCP.)
- Introduce `ZealotState` (free gateways, minerals, gas, supply, zealots, dragoons, core, race) built once at the
  top — the current method calls `Count.*` and `A.*` repeatedly with different values, which is hard to reason
  about. (SRP.)
- Replace `public static String reason` with the returned decision. (SRP.)

**The same pattern, same remedy:** `AutoProduceWorkersCommander` (item 26), `AutoProduceGas`
(and the other `ProduceXxx` classes) — once `ProduceZealot` is the exemplar, the rest follow mechanically.

---
## 47. `src/atlantis/game/listeners/OnGameStarted.java` — 257 linii

**What it does.** `execute()` performs the one-time game boot: painter binding, **per-game resets** (early-scout
tracker, end-game latch, scout state, battle commander, missions, unit-keyed deltas/throttles), strategy choice,
build-order setup, initial actions, map init, race detection, `DetectInitialTechs`, cherry replay init, logging,
`BenchmarkMode`/`Main` hooks.

**SRP break.** It is a *composition root* and a *reset list* and *strategy selection* in one method. The reset
list is explicitly documented as "per-game state that must not leak" — a list that grows with every new static
(int the same OCP trap as `AbstractTestWithUnits.cleanUp`).

**Proposed changes.**
- (P1) Introduce `PerGameReset.all()` — each subsystem registers its `reset()` (or exposes one), and
  `OnGameStarted` calls one line. New static state no longer edits this class. (OCP + SRP.)
- (P2) Split the boot phases: `PainterBoot`, `StrategyBoot` (race/strategy/build order), `MapBoot`,
  `InitialActionsBoot`, `DiagnosticsBoot`. `execute()` then reads as a list of phases. (SRP.)
- (P2) The `Main` import (line 31) from a game listener is backwards: the listener should not know the entry point.
  Move the `BenchmarkMode`/`Main` hook to `main`/`Atlantis` orchestration. (DIP.)

---
## 48. `src/atlantis/production/orders/production/queue/add/PreventDuplicateOrders.java` — 327 linii

**What it does.** `preventExcessiveOrInvalidOrders(type, position)` runs an ordered chain of admission rules:
`OnlyOneAllowedOfType`, `justRequestedThisType` (with a static `lastRequestedAgo`), `preventSpamPylons`,
`tooManyRecently`, `tooManyOrdersOfThisType` (+ `MAX_NONCOMPLETED_ORDERS_AT_ONCE`), each reporting a refusal via
`QueueLastStatus.updateStatusFailed(...)`. Also a global `tempDisabled` kill-switch.

**SRP break.** This is the **same chain shape as `BuildGates`**, but the rules are private methods in one class
instead of objects, and each rule has a side effect (status update) interleaved with the decision. The static
`lastRequestedAgo` + `tempDisabled` are mutable globals.

**Proposed changes (P2).**
- Adopt the `BuildGates` shape: `OrderAdmissionRule { String reject(type, position); }` objects in a list; the
  caller maps the returned reason to `QueueLastStatus`. Decisions become pure, the status update happens once.
  (SRP + OCP.)
- `tempDisabled` / `lastRequestedAgo` into the admission state object injected by the queue (and resettable in one
  place). (SRP.)
- Merge with `AddToQueue.canPreventExcessiveOrders` (item 24) — one admission policy, two entry points today.

---
## 49. `src/atlantis/production/dynamic/terran/turrets/TurretsForMain.java` — 347 linii

**What it does.** Terran missile-turret placement around the main: `buildIfNeeded()` (disabled via
`if (true) return false;`), three independent heuristics — `turretsForMainChoke()`, `turretsForMainRegionBorders()`,
`turretsForMainBase()` — with per-instance tuning fields (`BORDER_TURRETS_*`), caches
(`Cache<ArrayList<APosition>>`, `Cache<APosition>`), and a `static AUnit main`.

**SRP break.** (a) **Three placement strategies** in one class with shared instance state and two caches;
(b) a `static AUnit main` snapshot that goes stale; (c) tunables as non-static `private final` fields (so they are
per-instance yet never vary); (d) a dead entry point (`if (true) return false`).

**Proposed changes (P2, low risk because it is disabled).**
- Make each strategy a `TurretSiteStrategy` (`ChokeTurretStrategy`, `BorderTurretStrategy`, `MainTurretStrategy`)
  returning `APosition or null`; `buildIfNeeded` iterates. Then enabling/disabling one strategy is one line.
  (SRP + OCP.)
- `BORDER_TURRETS_*` → `static final` constants (or a tuning object, cf. item 18). (SRP.)
- Remove the `static main` field; pass the main base through the strategy call. (SRP — no stale global.)
- Either delete the dead `buildIfNeeded` or wire it behind a config flag; dead-but-compiled code is misdirection.

---
## 50. `src/atlantis/config/MapSpecificCommander.java` — 326 linii

**What it does.** A `Commander` that, in local (watched) runs, applies **per-map presentation tweaks**: painting
mode, `GameSpeed.changeSpeedTo`, `CameraCommander.focusCameraOnInterestingCombatUnit`, based on
`ActiveMap.isMap("Bullet" / "M_v_" / "Tanks_v_" / "inCorner" / ...)`, plus `initialSpeed`.

**SRP break.** (a) Map-specific **policy** written as an `if/else` chain over map names (OCP: every new demo map
edits this class), (b) **presentation/labelling** concerns (`AAdvancedPainter.paintingMode`), (c) **game speed**
concerns, (d) cancelling itself under several env flags (`isLocal`, `isBenchmark`, `isParamTweaker`,
`isWineSuperFast`) — four environment conditions at the top of a per-map rule set.

**Proposed changes.**
- (P1) Model a map profile as data: `MapProfile { namePattern, paintingMode, speed, focusCamera }` in a list;
  the commander applies the first matching profile. New maps are data, not code. (OCP + SRP.)
- (P2) The "should I run at all" gate (`isLocal && !benchmark && !paramTweaker && !superFast`) belongs to the
  commander chain composition (e.g. `AtlantisGameCommander` decides whether to install it), not inside `handle()`.
  (SRP.)
- (P3) Camera and painting actions are presentation — route through the debug/painter subsystem (item 1) rather
  than poking `AAdvancedPainter.paintingMode` directly.

---

## Cross-cutting summary

| Priority | Change | Files affected |
|---|---|---|
| P1 | Delete / relocate dead debug code | 19, 44, 49 |
| P1 | Split god-utilities into named collaborators | 43 (`A`), 2 (`AUnitOrders`), 16 (`ProductionOrder`) |
| P1 | Replace long `if`-chains with ordered rule objects (template: `BuildGates`, item 41) | 5, 26, 43 (`ProduceZealot` item 46), 48, 22 |
| P1 | Make per-pass/static hidden state explicit (`*PassState`, `DuelMemory`, caches) | 6, 7, 12, 22, 33, 34 |
| P1 | Data-driven registries instead of hand-maintained parallel tables | 7 (`ParamTweakerFactory`), 13 (`Env`), 8 (`Main` map list), 50 |
| P2 | Move race-specific rules into race policies | 5, 23, 32, 35 |
| P2 | Encapsulate public mutable collections/globals | 21 (`Atlantis` counters), 39 (`constructions`), 20 (`FakeUnit`) |
| P2 | Invert dependencies so the map/geo layer does not know the bot | 15 (`BWMap`), 38, 42, 29 |
| P3 | Split test harness concerns | 11, 31, 20 |

**Files where the right answer is "keep the shape"**: the individual gates/branches in `BuildGates` (41), the
painting primitives of `AAdvancedPainter` (1), the drawing/distance defaults of `HasPosition` (38, after painting
is removed), and `BuilderDiagnostics` (36). Size alone is not a defect — the entries above only change things that
have more than one reason to change.
