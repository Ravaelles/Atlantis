# Track 4: own map-analysis library (owner direction 2026-10-18)

Status: **requirements, not started.** The diet (M0) may start immediately;
M1+ needs no new engine work. This file is the charter; day-to-day truth
lives in `NEXT.md` #97. It is a big separate project by design: the library
must be usable without Atlantis, and Atlantis must be usable while the
library grows.

## Why not JBWEB, why not a fork (for these uses)

`src/jbweb/` is a pre-1.15 partial Java port (pure Java, no natives - the
old "JNI missing" story is withdrawn): Blocks layouts nobody reads,
Walls that never worked (NPE `Path.createUnitPath:129`, Ravaelles'
MrTate/JBWEB#1, still open), JPS used only by wall scoring, plus a dead
`src/bweb/` twin. Live uses are four narrow fallbacks (ground distance,
main-choke, base list, `isPlaceable`), each replaceable with own code in
dozens of lines. Carrying the port for those four is the wrong trade, and
forking it for them is a bigger wrong trade: our catalogue already does
the Blocks job (Stardust geometry), our reachability does the regions job.
The fork project (walls/JPS, separate repo) stays valid for exactly one
thing - a proven wall solver - and nothing else. Everything below is our
own code, BWEB ideas with attribution, MIT-clean.

## Charter

- The library answers questions **about the map**, never issues orders.
  Analysis is a pure function of (terrain + BWEM + static bases): no game
  state, no statics, no frame loop inside. Placement and execution stay in
  Atlantis by design - extracting them too would drag game state across
  the module boundary, which is the same spaghetti in a new package.
- Deterministic: same map file, same model. Golden JSON per reference map
  (`(3)TauCross1.1`, `Python 1.1`) pinned in tests; a model change that
  moves a golden file says so in its commit.
- Java 8, depends only on JBWAPI + `bwem` types. Starts as
  `atlantis.map.analysis` with an ArchUnit boundary (nothing outside the
  package touches sources except through `api`); extracts to its own
  module/repo when the model is stable (M5).
- Naming: nouns for model (`ChokeModel`, `BaseRole`, `DefenseSpot`),
  verbs for queries. No `Util`, `Manager`, `Finder` nouns.

## Needs inventory (measured 2026-10-18)

| Need | State | Home |
|---|---|---|
| tile walkability/buildability grids, BWEM wrappers | have (`map/`, 79 files) | stays, thins |
| main/natural choke, base roles, reachability | have (own) | stays, moves under model |
| Stardust block catalogue | have (`placement/`) | consumer, not library |
| defensive spot per choke | **missing** (only legacy cannon finder + dying Stations) | M2 |
| drop spots / islands / backdoors | **missing** (zero code) | M3 |
| wall anchors per choke | partial (wall geometry, no gap measurement) | M4 |
| expansion ranking | partial (scattered heuristics) | M2 stretch |
| ramps, proxy spots | missing | later, not chartered |

## Modules

- **M0 — diet (slice 0).** Delete dead: `src/bweb/` + `InitBWEB.java`
  (called only from commented code), `jbweb/Result.java` (zero refs),
  dead debug painters; replace 3 fallbacks with own code (ground-distance
  BFS over BWEM areas, main-choke primary, base list over `AMap.bwem`).
  Each deletion green suite + init still healthy. Does not touch live
  behavior.
- **M1 — model.** Immutable `MapModel { areas, chokes with geometry
  (line, width, ends), bases with roles (main/natural/expansion/island),
  mineral lines }` built once per game from terrain + BWEM. Golden tests.
  `AMap` thins to engine queries; everything structural moves under the
  model. No behavior change: consumers read the same answers through the
  model.
- **M2 — defense spots.** `defenseSpotsFor(choke)`: ranked tiles for
  cannons/tanks covering the choke line + the mineral line behind it,
  from choke geometry + engine weapon ranges (CONVENTIONS §9, never
  memory). First consumer: fortification policy (counts become spots).
  Status 2026-10-18: model (`DefenseSpots`) + `CoverageCannonRanker`
  wired in the catalogue, unit-pinned (coverage beats nearness).
  Live proof PARTIAL: long catalogue run `20261017_120347` (819 game-s,
  full tech incl. Forge/Robotics/Stargate/Observatory, E2E OK) warped
  its cannon through the sited path, which bypasses the adapter - zero
  cannon twin rows, ranker unexercised. Forcing it via V2 LIVE failed
  differently (147 s wall for 254 game-s, thin play, killed by the cap).
  The gap is architectural, not numerical: `RequestBuildingNear`
  searches with legacy `FindPosition` directly, so sited demand never
  reaches the catalogue. Next slice: route it through the funnel, then
  re-run long. Short-tier safety proven meanwhile (E2E OK, 6 s).
- **M3 — drop spots / islands.** Area-graph analysis over BWEM areas:
  enemy-base areas with no ground path from our army area (islands,
  backdoors) ranked by flight distance. Query:
  `dropCandidates(enemyBase)`. Proof: golden areas on Python + scouting
  usage later; no behavior change on landing (observer/shuttle logic is
  a separate track).
- **M4 — wall anchors + the natural wall-in.** Per-choke anchor sets for
  wall pieces; then the concrete case below. The JBWEB fork plugs in
  here IF own anchors prove weak (measured gap, not hope): `WallSource`
  interface, two implementations (own, jbweb-jar).
- **M5 — extraction.** Move `map.analysis` to its own module with the
  golden suite; Atlantis keeps a versioned dependency. Only when M1-M3
  are stable and consumers read through `api`.

## The concrete case: wall-in at the natural (Pylon + Gateway + cannons)

The shape Stardust and PurpleWave both play, specified so it is
implementable without re-deciding it mid-slice:

- **Where:** the natural choke (`ChokeModel` for the natural area exit;
  falls back to main choke when the natural has none).
- **What:** Pylon (power + tight) + Gateway (plug) + 1-2 cannons behind,
  leaving a one-tile `door` our units pass (door recorded in the model,
  holders keep it clear). Zealot holds the door; Dragoons shoot over.
- **When:** a `wallIn` goal fires after the core is up against Zerg
  (4pool/9pool window), later vs others; never before the opening
  releases (BO ownership holds). Race-gated: Zerg first, others later.
- **Failure modes, decided up front:** choke wider than pieces -> no wall,
  cannons-as-spots instead (M2 covers); wall pieces killed mid-build ->
  release claims, demand re-fires (ledger semantics from the placement
  rewrite); own units trapped -> door holder is a bug, fixed as a bug.
- **Proof:** a game holding a scripted early pressure with the wall
  standing (rush twins are dark until the cluster fights - the wall
  proof is survival + standing pieces, not kills).

## Proof gates (every module)

Golden model tests for M1 (map in, JSON out); game A/B for every
consumer swap (M2/M4): same tier, roster + twins compared, no new
`Can't find place` class, no sim slowdown past the measured budget.
A module with no consumer stays out of production (`api` grows on use,
never on speculation).

## Relations

- Phase 4 placement rewrite consumes the model (spots as constraints,
  anchors as exact tiles) but does not wait for it: catalogue first,
  model plugs in per query.
- Terran/Zerg assemblies stay stubs (D8); the model itself is
  race-neutral (geometry has no race).
- JBWEB fork (separate repo): walls/JPS only, integrates at M4 through
  `WallSource` when it demonstrates a working wall-in on OpenBW.
