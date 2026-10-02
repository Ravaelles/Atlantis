# Atlantis — Architecture Context Map (Stage A)

> Status: **normative direction** for all future work. Companion to
> `_AI/REVIEW.md` §16 (canonical plan) and enforced by
> `src/tests/architecture/ArchitectureBoundaryTest.java` (Stage B).
> This document defines the *target* boundaries. It does **not** require moving
> code yet (that is Stage I); until then the mapping below is how existing
> packages are classified, and the Stage B test freezes today's violations.

---

## 1. Purpose

Atlantis currently groups code by **subject**, not by **dependency direction**.
The measured result (`_AI/REVIEW.md` §13) is that every major package is in a
cycle with several others and there is no stable core. This document fixes the
boundaries so that dependency direction can be enforced mechanically.

The architecture is a **modular monolith with a hexagonal core**:

```
adapters  ──▶  application  ──▶  contexts  ──▶  core
   (bwapi, starengine, fake)      (6 domains)      (no bwapi, no statics)
```

Dependencies point **inward only**. A context may depend on `core` and on the
**published API** of another context, never on its internals.

---

## 2. The six contexts

| Context | Responsibility (one reason to change) |
|---|---|
| **Economy** | workers, mining/gas, resource allocation, base/expansion economy |
| **Production** | build orders, construction placement, production queue, supply |
| **Combat** | missions, squads, targeting, micro, retreating |
| **Intelligence** | what we know about the enemy: units, strategy, tech, timings |
| **Map** | static geometry: regions, chokes, paths, walls, high ground, bases |
| **Scouting** | exploration and information gathering across the map |

Plus, outside the domains:

- **core** — the domain model and rules; no `bwapi`, no static mutable state.
- **application** — the frame pipeline and stateless systems.
- **adapters** — engine/IO integrations (`bwapi`, `starengine`, fakes, debug UI).
- **bootstrap** — entry point, config, launcher selection.

**Rule:** every class belongs to exactly one context (or to core /
application / adapters / bootstrap). If a class fits two, it is two classes.

---

## 3. Published APIs

These are the **only** types other contexts may import. Everything else in a
context is internal and may change at will. (Names marked *planned* do not exist
yet; they are the target of Stages C–H.)

### core
`AUnit`, `AUnitType`, `Units`, `APosition`, `HasPosition`, `PositionUtil`,
`Decision`, `Vector`.
*(Target: `UnitSnapshot`, `UnitState`, `World` — see REVIEW §16 Stage E.)*
**Forbidden:** any context package, `bwapi`, `atlantis.game` globals.

### Economy
`FreeWorkers`, `WorkerRepository`, `IdleWorker`, `BaseLocations`, `Bases`,
`ABaseLocation`, `ExpansionCommander`.
**Forbidden:** `atlantis.combat`, `atlantis.production.orders`,
`atlantis.information`, `atlantis.protoss`, `atlantis.terran`.

### Production
`Queue`, `ProductionOrder`, `CurrentQueue`, `ReservedResources`,
`ConstructionRequests`, `Construction`, `ProductionCommander`,
`BuildingsCommander`.
**Forbidden:** `atlantis.combat` internals, `atlantis.map.wall`,
`atlantis.protoss`, `atlantis.terran`.

### Combat
`CombatCommander`, `Squad`, `AllSquads`, `Mission`, `SquadTargeting`.
**Forbidden:** `atlantis.production.orders` internals,
`atlantis.units.workers` internals, `atlantis.map.scout` internals.

### Intelligence
`EnemyUnits`, `AStrategy`, `EnemyStrategy`, `StrategyChooser`, `OurInfo`,
`ATech`, `ATechManager`.
**Forbidden:** `atlantis.combat` internals, `atlantis.production` internals.

### Map
`Chokes`, `ChokeToBlock`, `ARegion`, `Regions`, `MainRegion`,
`PathToEnemyBase`, `GetWallIn`, `Structure`.
**Forbidden:** every other context. Map is pure geometry and must not know what
is being built or fought.

### Scouting
`ScoutCommander`, `ScoutState`.
**Forbidden:** `atlantis.combat` internals, `atlantis.production` internals.
May depend on **Intelligence** (it feeds it) and **Map**.

---

## 4. Context map (allowed dependency directions)

```
                      ┌──────────────┐
                      │    core      │◀── depended on by everyone
                      └──────────────┘
             ▲   ▲   ▲   ▲   ▲   ▲
             │   │   │   │   │   │
        ┌────┴─┐ │ ┌─┴───┐ │ ┌─┴────┐
        │ Map  │ │ │Econ.│ │ │Intel.│
        └──────┘ │ └─────┘ │ └───┬──┘
                 │         │     ▲
        ┌────────┴──┐ ┌────┴─────┴─┐
        │ Production│ │  Combat    │
        └───────────┘ └────────────┘
                 ▲            ▲
             Scouting ────────┘   (Scouting → Intelligence, Map)
```

Allowed (arrow = "may depend on"):
- all contexts → `core`
- `Combat` → `core`
- `Production` → `core`
- `Economy` → `core`
- `Scouting` → `Intelligence`, `Map`, `core`
- `Intelligence` → `Map`, `core`
- `Combat` → `Production` **only** via its published API (already a measured
  cycle; to be broken, not widened)

Disallowed (examples enforced by Stage B):
- `core`/`units` → any context (the core must not depend on consumers)
- `Map` → `Combat`, `Production`, `Economy`
- any context → `adapters.bwapi` (only adapters/bootstrap touch the engine)

**Crossing a context boundary is allowed only through a Published API.** Import
of an internal package is a boundary violation.

---

## 5. Application & infrastructure rules

- **application**: the `FramePipeline` and stateless systems. May read core and
  call context APIs; issues engine orders **only** through the `OrderSink` port
  (Stage D). No static mutable caches.
- **adapters**: the only place allowed to import `bwapi`, `jbweb`, `bweb`,
  `jfap`, `jps`, `jnativehook`, or files/Swing. `starengine` and fakes are
  adapters too.
- **bootstrap**: `main.Main`, `atlantis.Atlantis`, `atlantis.config.*`,
  `atlantis.keyboard.*`.
- **debug/cherryvis**: presentation only; may read any context, may be read by
  none. Never a dependency of production logic.

---

## 6. Current packages → context (mapping used by Stage B today)

Until Stage I physically moves files, this is how existing packages are judged.

| Current package | Context |
|---|---|
| `atlantis.units` (root), `atlantis.map.position`, `atlantis.decisions` | **core** |
| `atlantis.units.workers`, `atlantis.map.base`, `atlantis.production.dynamic.expansion`, `atlantis.production.dynamic.workers` | **Economy** |
| `atlantis.production.**` (incl. `orders`, `constructions`, `requests`) | **Production** |
| `atlantis.combat.**`, `atlantis.units.interrupt`, `atlantis.units.attacked_by` | **Combat** |
| `atlantis.information.**`, `atlantis.units.fogged` | **Intelligence** |
| `atlantis.map.choke`, `.region`, `.path`, `.wall`, `.high`, `.bullets` | **Map** |
| `atlantis.map.scout` | **Scouting** |
| `atlantis.game`, `atlantis.config`, `atlantis.keyboard`, `atlantis.architecture`, `atlantis.util` | **infrastructure** (to be split in Stage H) |
| `atlantis.debug`, `atlantis.cherryvis` | **adapters/presentation** |
| `atlantis.protoss`, `atlantis.terran` | target: **Combat** strategies (Stage G) |
| `bwapi`, `jbweb`, `bweb`, `bwem`, `jfap`, `jps`, `starengine` | **adapters** (external libs) |

Notes:
- `atlantis.util` is a God-utility area (`A`, caches). It is **not** a context.
  Its value objects belong to `core`; its I/O and logging belong to adapters.
  Stage H dismantles it.
- `atlantis.game.A` / `AGame` are infrastructure globals. New production code
  must not add `A.*` calls; prefer explicit dependencies.

---

## 7. How this is enforced

1. `src/tests/architecture/ArchitectureBoundaryTest.java` (Stage B) asserts the
   directions above.
2. Today's violations are **frozen** in `_AI/architecture/archunit-store/`; the
   test fails only on **new** violations.
3. Every new violation must be either fixed or explicitly justified; the goal
   (Stage J) is to shrink the store to zero and then delete it.
