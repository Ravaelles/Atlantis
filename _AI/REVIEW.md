# Atlantis — Architectural Review & Target Plan

> Prepared at the request of the project owner. Scope: whole solution, viewed
> top-down. Audience: the owner (senior-level honesty, not reassurance).
> Status: proposal for discussion, not yet executed.

---

## 0. How to read this document

This is a **review and a plan**, deliberately written before any code is
touched. It is opinionated and, by request, **skeptical**. Where an idea
(Vertical Slice Architecture, DDD, "rewrite the layers") is a poor fit for this
project, the document says so and explains why. The recommendations in the
final section are ordered so that each step is independently shippable and can
be abandoned without harm.

The project has been built for ~10 years and is genuinely large. **A big-bang
rewrite would be the single worst decision available.** Almost everything below
is designed around incremental, strangler-style change that keeps the bot
playable at every commit.

> **Canonical plan:** §16 ("Action Plan — named stages") is the single source of
> truth for execution. Sections §8 and §15 explain *why*; where their lettering
> conflicts with §16, §16 wins.

---

## 1. Executive summary

Atlantis is a ~128,000-line Java framework for a StarCraft: Brood War bot,
1,514 Java files (1,360 under `src/atlantis`). It compiles, it runs, it has real
tests, and it actually wins games. That is the honest headline: **this is not a
broken codebase, it is a successful codebase that has outgrown its
architecture.**

The problems are not "bad code" in the small. They are **structural**:

1. **No enforced layering.** Packages imply layers (game → combat/production →
   map/information → units → bwapi) but nothing enforces the direction.
   Anything can call anything; cycles are the norm.
2. **A small number of God classes** concentrate a decade of decisions:
   `AUnit` (4,080 lines), `A` (1,688 lines, 163 public static methods),
   `Selection` (1,811 lines), `AUnitType`, `AAdvancedPainter`.
3. **Global state as the integration mechanism.** Static caches
   (`Select.cache`), a static unit registry (`AUnit.instances`), a God utility
   (`A`) used by 625 of 1,360 classes, and hand-tuned cache TTLs are how
   subsystems cooperate.
4. **A home-grown reflective behavior tree** (Commander/Manager) that is
   simultaneously the best idea in the codebase and its biggest liability:
   430 `Manager` subclasses, instantiated **per unit, per frame, reflectively**.
5. **Entity management is duplicated and leaky.** AUnit is a data bag *and* a
   623-method behavior surface *and* has ~24 `public` mutable `_`-fields
   mutated from outside; there is a parallel `FoggedUnit` entity model; identity
   lives in a static `HashMap`.

**Verdict on the two named ideas:**

- **Vertical Slice Architecture (VSA): do not adopt as the primary model.**
  VSA is a shape for request/response business applications (a user action maps
  to one vertical slice that owns UI→handler→domain→persistence). A real-time
  bot has no discrete "requests" — every frame, *all* subsystems run against a
  shared, mutating world. Forcing VSA here produces "slices" that all need the
  same game loop and the same entities, i.e. fake verticals. However, *one*
  flavor of vertical thinking is genuinely useful: **self-contained per-unit
  behavior packs** (a Marine pack, a Dragoon pack). That is a local
  organization principle, not the system architecture.
- **Domain-Driven Design: adopt the *strategic* half, reject the *tactical*
  half.** Bounded contexts, a ubiquitous language, and explicit context
  boundaries fit this domain well (Economy, Production, Combat, Intelligence,
  Map, Infrastructure). Aggregates, repositories, and transactional consistency
  do **not** map onto a game memory projection that the engine mutates
  underneath you. Full tactical DDD would add ceremony with no invariant to
  protect.

**What to do instead (recommended primary architecture):** a **modular
monolith / hexagonal core with explicit bounded contexts**, driven by a
**frame pipeline**, with a **read-model + stateless-systems** treatment of
units. See §6.

---

## 2. Measured facts (evidence)

All counts below were taken directly from the tree.

| Signal | Value |
|---|---|
| Java files (whole repo / `src/atlantis`) | 1,514 / 1,360 |
| Lines of Java under `src` | ~128,654 |
| Largest classes | `AUnit` 4,080 / `AAdvancedPainter` 1,891 / `Selection` 1,811 / `AUnitType` 1,800 / `A` 1,688 |
| `AUnit` public/private methods | ~623 |
| `AUnit` `public` mutable state fields (`_last*` etc.) | 24+ |
| `A` public static methods | 163 |
| `Selection` public methods | 235 |
| Classes importing `AUnit` | 913 |
| Classes using the `A.*` God utility | 625 |
| Classes using `Select.*` static selection | 366 |
| Classes using `AGame.*` | 98 |
| `Commander` subclasses | 104 |
| `Manager` subclasses | 430 |
| Files branching on race (`We.protoss/terran/zerg`) | 241 |
| Production-code sites branching on `Env.isTesting()/isStarEngine()` | 58 |
| Static caches keyed by string literals in `Select` alone | 4 caches, dozens of keys, magic TTLs (0, 1, 30, 31, 53, 73, 91, 293 frames) |

These numbers alone do not prove a problem. Their *pattern* does: a single God
utility reachable from nearly half the codebase, and per-unit reflective object
graphs rebuilt 25 times per second, are structural facts that no amount of
local tidying will fix.

---

## 3. What the architecture actually is today

Two parallel, reflective trees, plus a global blackboard of static caches.

### 3.1 Commander tree (game-level, 104 classes)

`AtlantisGameCommander` lists 16 top-level `Commander`s in an array
(`topLevelSubcommanders()`). `BaseCommander` reflectively instantiates the whole
tree **once at startup**. Each frame, `Commander.invokedCommander()` runs
`handle()`, guarded by `if (A.now == lastFrameInvoked) return false;`.

This is a reasonable **ordered pipeline of systems** — the good part. Its
weaknesses:
- Ordering is encoded implicitly by array position across dozens of files; no
  single place documents why `BulletsCommander` must precede
  `UnitStateCommander` etc.
- The `A.now == lastFrameInvoked` dedup makes a commander run at most once per
  frame, but who may re-invoke it, and with what effect, is invisible.
- `handle()` returns a boolean whose meaning is inconsistent between
  `Commander` (`result = result || ...`, "did anything happen") and `Manager`
  ("non-null = stop the chain"). Two different contracts share the name.

### 3.2 Manager tree (unit-level, 430 classes) — the biggest liability

`UnitStateCommander.handle()` does:

```java
for (AUnit unit : Select.ourUnitsWithUnfinishedList())
    (new UnitStateManager(unit)).invokeFrom(this);
```

`BaseManager`'s constructor calls `initializeManagerInstances()`, which
reflectively instantiates **every declared submanager**, whose constructors do
the same recursively. So **constructing one root manager builds an entire
object tree, per unit, per frame.** `Manager.invokedFor(Class, unit)` and
`InstantiateManager.byClass(...)` then create additional managers on demand
throughout the code.

This is a home-grown behavior tree / chain-of-responsibility with:
- **Reflection in the hot loop** (25 fps × N units × whole subtree).
- **Ordering via `managers()` arrays** — same invisibility as commanders.
- **Exception swallowing**: `invokedFor` catches everything and returns `null`,
  silently changing behavior. A malformed manager becomes a no-op.
- **Broken `equals`/`hashCode` contract** in `BaseManager`:
  `equals` compares only `getClass()`, while `hashCode` uses `unit.id()` +
  class. Two managers of the same class for different units are `equals` but
  have different hash codes. This will misbehave in any hash-based collection.
- **`applies()` + `handle()` + submanager recursion** make single behaviors hard
  to unit-test in isolation; the test must reconstruct the tree.

None of this is a reason to throw the tree away. It is a reason to make the
tree **explicit, ordered, and non-reflective**.

### 3.3 The global blackboard

- `AUnit.instances` — a `static HashMap<Integer, AUnit>` identity registry with
  manual lifecycle (`createFrom`, `forgetUnitEntirely`).
- `Select.cache`, `cacheInt`, `cacheUnit`, `cacheObject` — four static caches in
  one class, keyed by string literals, with hard-coded frame TTLs. Correctness
  depends on someone calling `clearCache()` at the right time. `Cache.getIfValid`
  has a latent bug: it performs a validity check for `AFocusPoint`/`AUnit` but
  then `return value;` on the fallthrough **regardless of validity**, so the
  check is ineffective.
- `A` — a static utility used by 625 classes, spanning logging, formatting,
  file I/O, Swing/JFrame construction, date math, random helpers, resource
  accounting, and camera control. It is at least six responsibilities wearing
  one name, and it reaches *back* into production (`ReservedResources`) and
  *up* into units (`AUnit`), so "utility" is a fiction.

---

## 4. SOLID / SRP assessment (honest version)

**SRP is violated at the class level almost everywhere it matters — but the
project has over-corrected in one direction while ignoring another.**

- `AUnit`, `A`, `Selection` are the textbook violations: multiple reasons to
  change in one class; a decade of additions with no cohesion boundary.
- Yet the codebase also has **430 `Manager` micro-classes**, many of which are
  one- or two-method wrappers. That is SRP taken to the point of fragmentation:
  the cost of navigating/understanding the behavior is spread across hundreds
  of files, and the "single reason to change" is often a single `if`.
- So the real diagnosis is **uneven granularity**: a few hundred classes carry
  everything, hundreds carry almost nothing, and the connective tissue
  (ordering + side effects + caching) lives in reflection and globals.

**Open/Closed**: the reflective trees are *accidentally* open — you add a
behavior by adding a class to an array — but the arrays are scattered. Adding a
race is not OCP; it means touching 241 files with race branches. Race should be
a strategy/context boundary, not an `if` sprinkled across the code.

**Liskov / Interface Segregation**: `Manager` has one fat contract
(`applies/handle/managers/invokedFor/usedManager/parentsStack/...`) that every
one of the 430 subclasses inherits whether it needs it or not. `Selection`'s
235 public methods are effectively an unsegmented interface.

**Dependency Inversion**: almost absent. High-level policy (`production`,
`strategy`) depends on low-level detail (`AUnit` internals, static caches,
`bwapi`) directly. `A` is a dependency on a concrete global, not an abstraction.
Selection even depends on the production layer (`BuilderManager`), confirming the
absence of a stable dependency direction.

Additional concrete findings worth fixing regardless of the grand plan:

- **`BaseManager.equals`/`hashCode` contract violation** (§3.2).
- **`Cache.getIfValid` returns invalid values** (§3.3).
- **58 test-only branches in production code** (`Env.isTesting()`). Testability
  leaked into the design instead of being provided by it.
- **`System.exit`/`A.quit`/`AGame.exit` in 26 places** deep inside logic. A bad
  configuration or missing manager terminates the JVM from arbitrary depth.

---

## 5. Entity / unit management — root cause of the recurring pain

The owner's instinct is correct: unit management is the replicated problem. The
cause is that **four different responsibilities are fused into `AUnit` and its
static registry:**

1. **Engine binding** — wrapping `bwapi.Unit`.
2. **Identity & lifecycle** — static `instances` map, `createFrom`,
   `forgetUnitEntirely`, `isValid`.
3. **Domain state** — hp, position, orders, cooldowns, targets…
4. **Behavior** — 623 methods that decide what the unit should do, plus a
   parallel state machine living in `public _last*` fields written by
   `UnitStateManager` and read all over the codebase.

Because all four are one class, every feature that needs "a unit" reaches into
`AUnit`; because state is `public` and mutated externally, the *rules* about that
state are nowhere. And because there are two entity representations (`AUnit`
and `AbstractFoggedUnit`/`FoggedUnit`), fakes, tests, and production each
re-derive "what a unit is" differently (`src/tests/fakes/FakeUnit.java`,
`test/...`, `starengine`). That is the "repeated in many places" feeling.

**Recommendation (core insight of this review):** split the fused
responsibilities into a **read model** and **stateless systems**.

- `UnitSnapshot` — an immutable per-frame projection (id, type, position, hp,
  cooldown, order, target, flags). Read-only. No behavior.
- `UnitEntity` / `World` — owns identity and the current snapshot map;
  the *only* place lifecycle happens.
- `UnitState` — the accumulated derived state currently scattered across
  `public _last*` fields, with an explicit small API; no external mutation.
- **Systems** — stateless operations over snapshots (`MovementSystem`,
  `TargetingSystem`, `ProductionSystem`, …). They read snapshots, emit
  intentions; a single applier issues `bwapi` orders.
- **Behavior packs** — per-unit-type decision components (replacing much of the
  Manager tree) that operate on `(UnitSnapshot, World)` and return an intention.

This directly kills the static registry, the public mutable fields, the
duplicate fogged model, and the need to rebuild behavior trees per frame.

---

## 6. Target architecture (top-down)

Recommended shape: **Modular monolith with a hexagonal domain core, driven by a
frame pipeline, with data-oriented units.** Concretely, five rings:

```
┌──────────────────────────────────────────────────────────────┐
│ 6. Infrastructure / Adapters                                  │
│    bwapi, jbweb, BWEM/JPS, files, Swing debug UI, launcher     │
│    (Chaos / OpenBW behind GameLauncher — already correct)      │
├──────────────────────────────────────────────────────────────┤
│ 5. Application / Orchestration                                 │
│    FramePipeline: ordered, explicit list of Systems            │
│    Commands / intents, one applier to bwapi                   │
├──────────────────────────────────────────────────────────────┤
│ 4. Bounded Contexts (modules)                                  │
│    Economy │ Production │ Combat │ Intelligence │ Map │ Scouting│
│    each with public API + internal implementation              │
├──────────────────────────────────────────────────────────────┤
│ 3. Domain Core                                                 │
│    World, UnitEntity, UnitSnapshot, UnitState, value objects   │
│    (position, resources, supply, type) — no bwapi, no statics  │
├──────────────────────────────────────────────────────────────┤
│ 2. Ports                                                       │
│    Interfaces the core needs (GameQuery, OrderSink, Clock, Map)│
├──────────────────────────────────────────────────────────────┤
│ 1. Adapters implement ports (bwapi adapter, fake adapter,      │
│    starengine adapter)                                         │
└──────────────────────────────────────────────────────────────┘
```

Key rules to make this real (and enforceable):

1. **Dependencies point inward only.** Adapters depend on ports; ports depend
   on the core. Contexts may depend on the core and on *published* APIs of other
   contexts, never on their internals.
2. **No `static` mutable state in the core.** Caches become per-frame query
   services with explicit invalidation owned by the pipeline.
3. **One `World` per frame.** Snapshots are immutable; systems cannot mutate
   each other's inputs.
4. **Enforce it mechanically.** A build-time check (ArchUnit or an
   `AbstractTestWithWorld`-style boundary test) fails the build on forbidden
   imports. Without enforcement, the old habits win within a month.

### 6.1 Is this "better layers"? Yes — and why

The current packages *look* layered but aren't, because the dependency arrows
allow up-calls (`Selection` → production, `A` → units, `AUnit` → combat). The
proposal makes the intended direction real and testable. You do **not** need to
reorganize every file to get 80% of the benefit; you need (a) the four core
types above, (b) the pipeline, (c) a boundary test, and (d) migration of the
worst offenders.

---

## 7. VSA and DDD — direct answers

### 7.1 Vertical Slice Architecture — **No, not as the system architecture**

- VSA's unit is a *feature/use case that a user triggers*, owning its full stack.
  A bot has no user requests; it has a 25 Hz tick where all subsystems coexist.
- Slices would duplicate the game loop, entity access, and map information, or
  they would share a core — at which point you have a modular monolith, not VSA.
- The one worthwhile borrow: **feature/enemy/unit-type slices as self-contained
  behavior modules** with a narrow contract, so adding "how a Dragoon fights"
  touches one folder. Name it "behavior packs", not VSA, and apply it inside
  `Combat`, not across the whole system.

### 7.2 Domain-Driven Design — **Strategic yes, tactical no**

- **Adopt:** bounded contexts (Economy/Production/Combat/Intelligence/Map),
  a ubiquitous language (the code already has great nouns: supply, build order,
  squad, mission, focus point, tech), and an explicit context map with
  published APIs.
- **Skip:** Aggregates, Repositories, Domain Events as a persistence/transaction
  ...mechanism. There is no database and no transactional invariant to protect;
  the "domain" is a read/write projection of engine memory. `AUnit` is not an
  aggregate root.
- **Net:** DDD's strategic design improves boundaries; its tactical patterns
  would add ceremony without invariants.

### 7.3 The pattern that actually fits: read-model + systems (data-oriented)

StarCraft bots are simulations. The proven shape is: **immutable per-frame
snapshots + stateless systems + an explicit pipeline**, i.e. an ECS-flavored
approach (not necessarily a full ECS library). It is the natural cure for the
entity-management pain, the global caches, and the reflective per-frame trees —
all at once.

---

## 8. Migration plan (incremental, strangler)

> Superseded for naming by §16. Kept for the rationale.

Each phase is independently shippable and keeps the bot playable. Do not start a
phase until the previous one is merged and verified by running the bot.

### Phase 0 — Ratchet (1–2 weeks, low risk)

Goal: stop the bleeding and make progress measurable.

1. Add **ArchUnit** boundary tests (or plain test) encoding today's layering as
   a *frozen baseline*: fail on **new** violations only.
2. Fix the two confirmed defects: `BaseManager.equals/hashCode` and
   `Cache.getIfValid` returning invalid values.
3. Replace `System.exit`-from-arbitrary-depth with an exception + one top-level
   handler.
4. Add a benchmark/profiler baseline for a full game tick; you will need it to
   prove the Entity phase helps.

### Phase 1 — Explicit pipeline, no reflection (3–6 weeks, medium)

Goal: make the good idea (ordered systems) explicit and cheap.

1. Introduce `FramePipeline` with an **explicit, documented command list**;
   delete `topLevelSubcommanders()` ordering surprises. Commanders become
   pipeline steps with one documented contract.
2. Replace reflective `Commander`/`Manager` instantiation with a registry of
   **factories** (or plain constructors). Remove `InstantiateManager`,
   `invokedFor`, and reflection from the hot path.
3. Keep behavior identical; prove it with the existing acceptance tests and a
   game run. **No behavior changes in this phase.**

### Phase 2 — Entity rebuild behind an interface (4–8 weeks, medium-high)

Goal: kill the fused `AUnit` without a big-bang rewrite.

1. Introduce `UnitSnapshot` (immutable) and a `World` that produces snapshots
   each frame. Back it initially by the existing `AUnit` (adapter) so nothing
   else changes.
2. Migrate `UnitStateManager`'s `public _last*` writes into `UnitState` behind a
   small API. Delete the public fields one at a time.
3. Make `FoggedUnit` a `UnitSnapshot`-based projection; delete the duplicate
   model.
4. Replace `AUnit.instances` with the `World` registry; delete
   `forgetUnitEntirely` call sites.
5. Delete the static `Select` caches in favor of per-frame query services with
   explicit invalidation.

### Phase 3 — Split the God classes (ongoing, 4–8 weeks)

1. Dismantle `A`: `AString`, `AFile`, `ALog`, `ASwing`, `ATime`, `ANumbers`,
   `AResources`. Keep a thin deprecated `A` facade during migration, then delete.
2. Split `Selection` into a narrow core + composed capability interfaces
   (filtering, geometry, ordering) so no caller sees 235 methods.
3. Move AUnit's 623 behavior methods into behavior packs/systems (Phase 4 order).

### Phase 4 — Bounded contexts + behavior packs (ongoing)

1. Define the six context APIs; enforce with ArchUnit.
2. Collapse race `if`-branches (241 files) into race **strategies** registered
   per context. Adding a race must not touch 241 files.
3. Convert per-unit-type Managers into behavior packs with the
   `(UnitSnapshot, World) → Intention` contract.
4. Route all `bwapi` order issuing through a single `OrderSink` adapter.

### Phase 5 — Enforce & document

1. Flip the ArchUnit baseline from "no new violations" to "zero violations".
2. Write an ADR set (architecture decision records) in `DOCS/`.
3. Delete the compatibility façades.

---

## 9. Anti-goals (explicitly out of scope / not recommended)

- **Full rewrite** of the bot. Not worth the risk; the strangler path reaches
  the same target.
- **VSA as the primary architecture.** See §7.1.
- **Full tactical DDD** (aggregates/repositories). See §7.2.
- **Microservices / event bus.** This is a single-process, 25 Hz simulation;
  in-process contexts + pipeline are correct.
- **A third-party ECS framework** unless Phase 2 proves the read-model is
  insufficient. Start data-oriented by hand; adopt a library only with evidence.

---

## 10. Risks & how this could fail

1. **The reflective trees are load-bearing and undocumented.** Phase 1 must not
   change behavior; rely on acceptance tests + a full game run, and keep the
   phase purely mechanical.
2. **Hidden coupling via caches.** Phase 2 removes a correctness crutch. Expect
   latent bugs to surface; this is the point — they are currently masked by
   lucky TTL numbers.
3. **Boundary enforcement without buy-in decays.** The ArchUnit baseline is the
   single most important artifact; if it is not enforced in CI, nothing changes.
4. **Over-decomposition.** The codebase already shows SRP taken too far (430
   managers). New modules must be *cohesive*, not merely small.

---

## 11. First concrete backlog (next 10 changes)

1. ArchUnit frozen-baseline test.
2. Fix `BaseManager.equals`/`hashCode`.
3. Fix `Cache.getIfValid` invalid-value fallthrough.
4. Centralize JVM exit (one top-level handler).
5. Introduce `FramePipeline` with an explicit ordered step list (no behavior
   change).
6. Remove reflection from `Commander` construction.
7. Remove reflection from `Manager` construction; keep behavior identical.
8. Introduce `UnitSnapshot` (backed by `AUnit`).
9. Introduce `UnitState`; migrate the first `public _last*` field.
10. Add a tick benchmark to CI and record before/after.

---

## 12. One-paragraph answer to the original question

You do not have a "bad code" problem, you have a **boundary** problem: a
successful simulation whose integration mechanism is global static state, whose
behavior tree is rebuilt by reflection every frame, and whose entity is four
responsibilities fused into one 4,000-line class. Fix the boundaries with a
modular monolith (bounded contexts + ports/adapters), make the pipeline
explicit, and rebuild units as immutable snapshots with stateless systems.
Borrow VSA only as per-unit behavior packs. Borrow DDD only for bounded contexts
and ubiquitous language. Do it incrementally, behind tests, one phase at a
time.

---

## 13. Is the current modularity real? (measured)

**No. The package structure is nominal, not architectural.**

The packages group code by *subject* (what it is about), not by *dependency
direction* (who may depend on whom). Measured on `src/atlantis`, `import`
counts between top-level packages show that **every major package is in a cycle
with several others**. There is no acyclic ordering you could impose without
changing code.

Representative cycles (counts = number of importing files):

| Cycle | A → B | B → A |
|---|---|---|
| production ↔ game | 200 | 13 |
| combat ↔ game | 243 | 11 |
| combat ↔ units | 518 | 15 |
| production ↔ units | 275 | 8 |
| map ↔ units | 54 | 17 |
| map ↔ information | 13 | 13 |
| information ↔ production | 17 | 100 |
| util ↔ units | 28 | 11 |
| util ↔ game | ? | 18 |
| combat ↔ terran | 13 | 8 |
| combat ↔ protoss | 27 | 5 |
| combat ↔ architecture | 321 | 1 |

Two consequences follow directly:

1. **`units` is the de-facto core, but it is not isolated.** It is the most
   depended-on package (`combat` 518, `production` 275, `protoss` 67, `map` 54,
   `information` 49, `terran` 42…), yet `units` itself depends on `combat`,
   `production`, `terran`, `protoss`, `information`, `map`. A core that depends
   on its own consumers is not a core.
2. **`architecture` is not a stable layer.** `combat` (321) and `production`
   (64) depend on it, but it depends back on `game`, `units`, `combat`, `util`,
   `debug`. The base of the dependency graph is itself entangled.

Also visible: race packages (`protoss`, `terran`) are orthogonal to domains and
cut across them (`combat ↔ protoss`, `combat ↔ terran`). Race is a *dimension*
(strategy), not a folder. This is the measured root of the 241 files that
branch on race.

**Conclusion.** Rigorous physical separation of all 1,360 classes would be a
huge change with little payoff and high regression risk. What is missing is not
*more folders* — it is **enforceable boundaries**: a small number of cohesive
contexts, each exposing a narrow published API, with a mechanically checked
allowed-dependency direction. Achieve that, and the cycles die ; do it by moving
folders in one sweep, and you will just recreate the cycles inside the new tree.

---

## 14. The hexagonal core, explained (from scratch)

**Purpose.** Keep the rules of the game (the part worth testing and reasoning
about) independent of the engine, the framework, and the infrastructure.

**The idea in one line:** the application sits inside a hexagon; nothing
outside may be referenced by name; the application talks to the outside only
through interfaces it defines itself (the **ports**), and concrete technologies
plug in at the edge (the **adapters**).

```
        driven/driving side                                   infrastructure

  BWAPI events ──▶┌──────────────────────────────────────┐◀── BwapiAdapter
  Keyboard     ──▶│            APPLICATION               │◀── FakeAdapter (tests)
  Tests        ──▶│   ports in ──▶ domain logic ──▶ out  │◀── StarEngineAdapter
                  └───────────────┬──────────────────────┘
                                  │
             defines ─────────────┘
             interfaces (ports):
               GameQuery, OrderSink, MapPort, ClockPort, LogPort
```

Two kinds of ports:

- **Driving (primary) ports** — how the outside *calls in*: the frame tick,
  unit-discovered events, keyboard commands. In Atlantis these already exist as
  `Atlantis` / `AtlantisGameCommander`; they just need to be explicit and thin.
- **Driven (secondary) ports** — what the core *calls out to*: query game state,
  issue orders, read the map, read the clock, write logs. **The core defines
  these interfaces itself**; it never imports `bwapi`.

**The one rule that makes it hexagonal:** *dependencies point inward.* The core
knows nothing about `bwapi`, `jbweb`, Swing, or files. Adapters know about the
core's ports. This is the **Dependency Inversion Principle applied at the system
level**, not just at class level.

**How it maps onto Atlantis:**

| Layer | Atlantis today | Target |
|---|---|---|
| Domain core | scattered across `units`, `combat`, globals | `World`, `UnitSnapshot`, `UnitState`, value objects, game rules |
| Ports | implicit, call `bwapi` directly | `GameQuery`, `OrderSink`, `MapPort`, `ClockPort`, `LogPort` |
| Adapters | `bwapi` everywhere; `Env.isTesting()` branches | `BwapiAdapter`, `FakeAdapter`, `StarEngineAdapter`, `OpenBW`/`Chaos` launchers |
| Application | `Commander`/`Manager` trees | explicit `FramePipeline` of stateless systems |

**Why this is worth it here (concretely):**

- The 58 `Env.isTesting()` branches in production code disappear: tests plug in
  `FakeAdapter`, production plugs in `BwapiAdapter`. No `if (testing)` anywhere.
- A second engine backend stops being a special case: the `GameLauncher`
  strategy you already have is exactly a driven adapter — hexagonal just applies
  the same idea consistently.
- The core becomes unit-testable without a running StarCraft.

**Honest cost / when *not* to use it.** Hexagonal adds indirection. A port is
only worth creating where you (a) will have more than one adapter, or (b) need
isolation for tests. Do **not** wrap all of `bwapi` in fifty interfaces — that is
"hexagonal theater". For a game bot the domain core is thin; the win is in the
five ports above plus the read model, not in abstracting everything.

---

## 15. Proposed architectural changes to the current approach

> Superseded for naming by §16 — the letters below are the origin of the stage
> list. Read §16 for the canonical, execution-ready plan.

These are concrete, ordered, and each keeps the bot playable. They build on the
phases in §8 but are expressed as changes *to what exists*, not to a blank
canvas.

**A. Declare six bounded contexts and a context map (no code moves).**
`Economy`, `Production`, `Combat`, `Intelligence`, `Map`, `Scouting`.
For each, decide the **published API** (the only types other contexts may
import) and the **forbidden imports**. Publish it in `DOCS/`. This is a design
artifact, not a refactor — cost is days, not weeks.

**B. Freeze the boundary with ArchUnit (the enforcement ratchet).**
Encode today's violations as a baseline and fail the build on *new*
violations. Without this, steps C–H decay within weeks. This is the single
highest-leverage change in the whole document.

**C. Introduce a read model: `World` + `UnitSnapshot`.**
One immutable projection per frame. Backed initially by the existing `AUnit`, so
no behavior changes. This kills the need for static `instances` and gives systems
a stable input.

**D. Make the frame pipeline explicit and non-reflective.**
Replace `topLevelSubcommanders()` arrays + reflective construction with an
ordered, documented `FramePipeline`. Behavior identical; ordering finally
visible in one place.

**E. One `OrderSink` for all engine commands.**
Every `unit.train(...)`, `unit.move(...)`, `unit.attack(...)` goes through one
adapter. This is the seam that makes `FakeAdapter`/`StarEngineAdapter` possible
and removes `bwapi` from the core.

**F. Replace static `Select` caches with per-frame query services.**
Explicit invalidation owned by the pipeline; no magic TTLs, no `clearCache()`
discipline.

**G. Make race a strategy dimension, not a package dimension.**
Register race-specific strategies behind one interface per context. Adding a
race must not touch 241 files; `protoss`/`terran` folders disappear into the
contexts they belong to.

**H. Then, and only then, move files.**
Target layout (illustrative, reached incrementally):

```
src/atlantis/
  core/          # no bwapi imports
    world/       # World, UnitEntity, UnitSnapshot, UnitState
    model/       # types, positions, resources, money
    rules/       # pure game rules
  ports/         # GameQuery, OrderSink, MapPort, ClockPort, LogPort
  application/   # FramePipeline + stateless systems
  contexts/
    economy/ production/ combat/ intelligence/ map/ scouting/
  adapters/
    bwapi/ starengine/ fake/ debug/
  bootstrap/     # Main, launcher, config, env
```

**What NOT to do at this stage (skeptical):**

- Do not physically split all packages into `core/ports/adapters` in one move.
- Do not create a port for everything; only the five above.
- Do not create a `common`/`shared`/`utils` context — that is how God utilities are
  reborn. Shared value objects live in `core/model`.
- Do not chase a perfect dependency graph before the ArchUnit ratchet exists; you
  will regress.

---

## 16. Action Plan — named stages (canonical)

This is the execution plan. Stages are labeled with a letter **and a name** so
that work, branches, and issues can reference them unambiguously (e.g. a branch
`stage-b/boundary-ratchet`). Each stage is independently shippable and must
leave the bot playable. Pooling: **A** and **B** can start immediately.

### 16.0 Summary

| Stage | Name | Horizon | Behavior change | Depends on |
|---|---|---|---|---|
| **A** | Boundary Contract | Now | none | — |
| **B** | Boundary Ratchet | Now | none | A |
| **C** | Explicit Pipeline | Next | none (mechanical) | B |
| **D** | Order Sink | Next | none | B |
| **E** | Read Model | Core | internal, observable | C, D |
| **F** | Cache Purge | Core | correctness raised | E |
| **G** | Race Strategy | Later | internal | E |
| **H** | God-Class Split | Later | internal | E, F |
| **I** | Physical Migration | Later | none | A–H |
| **J** | Fortress | Continuous | none | all |

Horizons: **Now** = start immediately; **Next** = once B is merged; **Core** =
the real re-architecture; **Later**; **Continuous** = never "done".

---

### Stage A — Boundary Contract (Now)

**Goal:** write down the boundaries before enforcing them. No code moves.

**Tasks**
1. Declare six bounded contexts: `Economy`, `Production`, `Combat`,
   `Intelligence`, `Map`, `Scouting`.
2. For each, write the **Published API** (the only types other contexts may
   import) and the **Forbidden Imports**.
3. Draw the **Context Map**: allowed dependency directions between contexts and
   the single direction toward `core`.
4. Put it in `DOCS/ARCHITECTURE-CONTEXT-MAP.md`.

**Definition of Done:** a document a new contributor can follow to place any
class into exactly one context and know what it may import.
**Effort:** days. **Risk:** low. **Enables:** B.

---

### Stage B — Boundary Ratchet (Now)

**Goal:** make the boundaries mechanically enforced so progress cannot silently
rot.

**Tasks**
1. Add **ArchUnit** (`com.tngtech.archunit`) as a test dependency
   (*this is a separate third-party library; unrelated to `AUnit`*).
2. Encode the current violations as a **frozen baseline** file; the test fails
   only on **new** violations.
3. Wire it into the build so it runs with the normal test suite.

**Definition of Done:** a green build that turns red the moment someone adds a
forbidden cross-context import. **Effort:** days. **Risk:** low.
**Note:** this is the single highest-leverage change in the whole plan.

---

### Stage C — Explicit Pipeline (Next)

**Goal:** make the good idea (ordered systems) explicit, documented, and cheap.

**Tasks**
1. Introduce `FramePipeline` with one ordered, documented step list;
   retire the scattered `topLevelSubcommanders()` arrays as the ordering source.
2. Replace reflective `Commander` construction with plain constructors/factories.
3. Do the same for `Manager` construction; remove reflection from the per-frame
   hot path.
4. Give `Commander` and `Manager` **one** documented contract (current boolean
   meanings differ).

**Definition of Done:** **zero behavior change**, proven by the existing
acceptance tests plus a full game run; ordering visible in one file.
**Effort:** weeks. **Risk:** medium (mechanical; must not change behavior).

---

### Stage D — Order Sink (Next)

**Goal:** one door for all commands to the engine; the seam for adapters and for
removing `bwapi` from the core later.

**Tasks**
1. Introduce `OrderSink` (a port) and route every `train/move/attack/...` call
   through it.
2. Implement it with `BwapiOrderSink` (production) and a recording
   `FakeOrderSink` (tests).
3. Make exceptions/rogue `System.exit` in the command path impossible (return a
   result instead).

**Definition of Done:** `bwapi` order calls exist only inside the sink.
**Effort:** weeks. **Risk:** medium.

---

### Stage E — Read Model (Core)

**Goal:** stop fusing identity, state, and behavior in `AUnit`.

**Tasks**
1. Introduce `World` + immutable `UnitSnapshot` (one projection per frame),
   backed initially by the existing `AUnit` so nothing changes.
2. Introduce `UnitState`; migrate the `public _last*` fields out of `AUnit` a few
   at a time, deleting each public field as it moves.
3. Replace `AUnit.instances` (static `HashMap`) with the `World` registry;
   remove `forgetUnitEntirely` call sites.
4. Make `FoggedUnit` a `UnitSnapshot` projection; delete the duplicate model.

**Definition of Done:** no `public` mutable state on units; no static unit
registry; tests can construct a `World` without StarCraft.
**Effort:** 4–8 weeks. **Risk:** high (latent bugs surface). **Depends on:** C, D.

---

### Stage F — Cache Purge (Core)

**Goal:** kill correctness-by-lucky-TTL.

**Tasks**
1. Replace the four static `Select` caches with per-frame query services whose
   invalidation is owned by the pipeline.
2. Remove magic TTLs and the `clearCache()` discipline.
3. Fix `Cache.getIfValid` (it currently returns invalid values — see §3.3).

**Definition of Done:** no `static` mutable cache in the core; invalidation is
explicit and testable. **Effort:** 2–4 weeks. **Risk:** medium.
**Depends on:** E.

---

### Stage G — Race Strategy (Later)

**Goal:** a race must not cost 241 edited files.

**Tasks**
1. Define one strategy interface per context for race differences.
2. Register Protoss/Terran/Zerg implementations (Strategy pattern, mirroring
   `GameLauncher`).
3. Dissolve `protoss`/`terran` top-level packages into the contexts they belong
   to; delete race `if`-branches.

**Definition of Done:** adding a race touches only its strategy classes.
**Effort:** 3–6 weeks. **Risk:** medium. **Depends on:** E.

---

### Stage H — God-Class Split (Later)

**Goal:** break up the concentration without a big-bang rewrite.

**Tasks**
1. Split `A` into `AString`, `AFile`, `ALog`, `ASwing`, `ATime`, `ANumbers`,
   `AResources`; keep a deprecated `A` façade temporarily, then delete it.
2. Split `Selection` into a narrow core plus composed capability interfaces
   so no caller sees 235 methods.
3. Move `AUnit`'s ~623 behavior methods into the systems / behavior packs
   produced by Stage E.

**Definition of Done:** no class in `core` exceeds a healthy size; the `A` façade
is deleted. **Effort:** 4–8 weeks. **Risk:** medium. **Depends on:** E, F.

---

### Stage I — Physical Migration (Later)

**Goal:** make the folder tree match the architecture — *last*, once boundaries
are real.

**Tasks:** move classes to the target layout from §15-H
(`core/`, `ports/`, `application/`, `contexts/`, `adapters/`, `bootstrap/`),
package by package, keeping Stage B green.

**Definition of Done:** tree matches the context map; ArchUnit passes.
**Effort:** ongoing. **Risk:** low (mechanical). **Depends on:** A–H.

---

### Stage J — Fortress (Continuous)

**Goal:** keep it. Architecture is a habit, not a one-off.

**Tasks**
1. Flip the ArchUnit baseline from "no new violations" to **zero violations**.
2. Add a **tick benchmark gate** in CI (the reflection removal in C should show
   up here; guard against regressions).
3. Keep architecture decision records in `DOCS/adr/`.
4. Delete the compatibility façades created in C–H.

**Definition of Done:** never — it is a standing practice.

---

### 16.1 Start here (immediate)

Begin with **Stage A** and **Stage B**. They are days of work, change no
behavior, and unlock every later stage by making the boundaries enforceable.
Suggested first branches: `stage-a/boundary-contract`,
`stage-b/boundary-ratchet`.
