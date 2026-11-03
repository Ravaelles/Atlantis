# Production V3: one door for placing buildings

Owner-ordered migration doc (2026-11-04, supersedes the V2 funnel /
legacy / V2-direct split). No code here - slices land as separate
commits, each pinned red-first, suite green, game proof where named.
Work proceeds piece by piece on owner call; nothing lands without it.

## 1. Why V3: the three doors

Three live issuance paths mint constructions with three different
gate sets. Every recent production defect is the same shape: a row
enters through the path whose gate set is missing the relevant check.

- **Legacy spiral** (`FindPosition.findForBuilding`,
  `APositionFinder.findStandardPosition`): power via the footprint
  center (correct), 7-tile early-Pylon map edge
  (`ProtossTooCloseToMapBoundaries`), geyser/minerals, occupancy.
  Still called directly by `RequestBuildingNear` (sited cannons,
  pylons, CyberneticsC), `ProduceBuilding.validatePosition`
  (re-validation), and the three base siters
  (`NextBasePosition`, `FindPositionForBase` x2). The A4h fallback
  removal was attempted and REVERTED as load-bearing, so the spiral
  is a live door, not a dead one.
- **Catalogue funnel** (`CatalogueSiteAdapter.siteFor`): proposes via
  the catalogue, validates per tile (the file's own words: "the
  catalogue proposes, the legacy validator disposes"). Has power
  (footprint, since the 2026-11-03 fix), geyser, refused-tile and
  standing-cover gates - but map edge is a 1-tile adjacency margin
  only (`TileAvailabilityGrid`), not the 7-tile early-Pylon rule.
- **V2-direct scheduler** (`ProductionScheduler.reservePlacement` +
  `GameOrderDirector.createConstruction`): mints the reservation
  tile verbatim with NO funnel validation. Its power verdict read
  the tile origin instead of the footprint center until the
  2026-11-03 fix; its edge rule is the same 1-tile margin.
- **Exact-position verbatim** (`DefineExactPositionForNewConstruction`):
  an `isUsingExactPosition` order hands the tile back without ANY
  validation (power, geyser, refused, edge) - only an occupancy check
  can force a fresh search.

Measured consequences on live games (no speculation):

- 2026-11-03 vs Steamhammer, loss: a 4x3 Gateway row sat at
  [123,26] with a warping Pylon at [121,22]. Tile-point power reads
  dy=144 (in the psi mask); the engine enforces the footprint center
  (dy=176, out). 82 seconds of refused Builds, timeout cancel,
  requeue ("Can't find place", "Too close to geyser" from the
  re-search near the cursed tile), "looks bugged" churn, 25 probes
  lost to lings, defeat with 1060 minerals banked and zero Gateways.
- Refused-tile loop: retained logs show re-notes on exact 2400-frame
  (TTL) boundaries across 32 tiles and 212 games - attempts never
  stop while a live row sits on a refused tile, because only NEW
  rows consult the refusal memory.
- Post-warp "builder gone" demotes: 19 of 25 retained demotes fired
  on healthy warps (measured site-vs-warp drift 3.16-5.0 tiles vs a
  2.5-tile self-match radius).

Why fixes did not stick: each fix closed one door's hole while the
other two stayed open, and the suite cannot tell the difference -
the sim has no real siting map behind `findPositionForNewBuilding`
(AMap statics are null headless) and `FakeOrderSink.build` never
refuses, so siting defects are green by construction. Games discover,
tests cannot. The HealthyMinimumBuildTest pins Pylon/Gateway/Core/
Assimilator warps on a flat stub without geyser, edge, or power
pressure - the right contract, the wrong map.

Communication failure, owned: the last weeks' fixes (including mine)
patched one path each and thereby cemented the three-door split
instead of challenging it, and my temp harnesses used flat arenas
while the TauCross snapshot vehicle already existed
(`src/atlantis/map/snapshot/`, `src/tests/resources/taucross-snapshot.json`,
`SimMapSnapshotTest`, NEXT #191). Per CONVENTIONS §3 that challenge
should have been raised at the first multi-path touch, not now.

## 2. What good looks like (Stardust, read from source)

`Stardust/src/Builder/BuildingPlacement.h` + `Blocks/` (no
interpretation - this is their header, verbatim in structure):

- Start block anchored RELATIVE TO the Nexus, fixed offsets, typed
  slots (2 Gateways, 2-4 tech, 3-4 cannons), a power Pylon that is
  PART OF the layout, and a reserved border-tile ring (walkable
  corridors, not decoration).
- Per-block edge rules (`allowTopEdge`/`allowLeftEdge`/
  `allowRightEdge`/`allowCorner`): every start-block variant returns
  false for all of them - start blocks never sit on an edge or corner.
- Pylon doctrine: the FIRST pylon is always in the start block; the
  next powers the choke cannon (PvP); the rest are ordered by distance
  to the mineral line. The producer takes the first pylon that either
  provides required build locations or keeps a minimum number of
  build locations available (slot preservation, not travel time).

What we already have of this: block templates
(`BlockTemplates.startBlocks()`), the USED+BORDER ring
(`TileAvailabilityGrid`), the travel+exit+power ranker vocabulary,
and per-type start-block anchoring in the catalogue. What diverged:
nothing forces the first Pylon into the start block on the V2 path,
nothing enforces edge rules on the catalogue/V2 path, and the ranker
optimizes travel instead of slot preservation.

## 3. Target: one entry, one exit, one verdict

- ONE entry: `ProductionScheduler.reservePlacement` is the only
  minter. No direct `FindPosition` / `APositionFinder` /
  `CatalogueSiteAdapter.siteFor` calls outside the planner; exact
  orders go through the same verdict (occupied-check carve-out
  preserved).
- ONE verdict: a single gate function every path consults -
  power-at-footprint-center, geyser/minerals, refused tiles,
  map edge, standing cover, builder-walkable/reachable. No two
  implementations that can disagree.
- ONE exit: `GameOrderDirector.createConstruction` trusts the
  verdict and only twins. No silent mints (closes NEXT #238's
  authorship question by construction: every row carries the
  verdict's reason).

## 4. Step sequence (each: pin red-first, implement, fast gate green, game proof where named)

- **S0. Snapshot siting harness.** Prove `reservePlacement` runs
  headless on `taucross-snapshot.json` (tiles, regions, chokes,
  resources, starts). Vehicle for every pin below; no flat stubs
  for siting ever again.
- **S1. Single verdict.** Merge funnel + V2-direct power/geyser/
  refused/standing checks into one function; both callers delegate.
  Deletes the disagree class, not one disagreement.
- **S2. Edge parity: 4 tiles everywhere (owner decision
  2026-11-04).** Replaces the legacy 7-tile early-Pylon rule and
  the catalogue 1-tile margin. Updates the pinned tests that name
  7 (`ProtossTooCloseToMapBoundariesTest`, `PylonPosition`
  references). Per-block edge flags (Stardust `allow*Edge`) ride
  here if the shared margin proves too coarse in review.
- **S3. Start-block-first Pylon (+ slot preservation).** Plumb the
  existing `startBlockAnchors` into `reservePlacement`; the first
  Pylon demand takes the block's power-Pylon slot. Ranker keeps
  travel+exit+power, adds Stardust's producer rule as the
  tiebreak/decider - A/B measured on the snapshot sim before it
  sticks. Asserts: first Pylon within N tiles of the Nexus on the
  snapshot; no Pylon inside 4 tiles of the edge.
- **S4. Kill switches, one per commit.** Route
  `RequestBuildingNear` sited calls, `ProduceBuilding.validatePosition`,
  and the three base siters through the single door; delete the
  orphaned legacy entry points to `src-old/`. Each deletion lands
  only with the suite green.
- **S5. Exact orders through the verdict.** `isUsingExactPosition`
  keeps its occupied-tile fast path; everything else about the tile
  (power, geyser, refused, edge) goes through the single verdict
  instead of verbatim trust.
- **S6. Corridor proof, not new machinery.** The BORDER ring
  exists; pin on the snapshot sim that a stamped block never traps
  the army (pathing assertion). New corridor machinery only if the
  pin fails.

## 5. Test strategy (the "100% build" answer)

- Planner pins on the TauCross SNAPSHOT (never flat stubs) for
  every gate x every building type: power-footprint (done,
  `GatewayPsiEdgeTest` model), geyser, edge-4, refused, standing.
- Milestone sim on the snapshot through the REAL siting stack:
  Pylon -> Gateway -> Core -> Assimilator warp in order, first
  Pylon near the Nexus, no edge placements. This is the test that
  would have caught the Steamhammer shape (unpowered row + churn).
- Honest split, stated up front: the sim still cannot run the
  ENGINE's refusal, so engine-truth stays a live-game proof (one
  quiet headless game per slice; games confirm, never discover).
- Default `FakeOrderSink.build` stays permissive; per-scenario
  `canBuildHere` stubs keep owning scenario-specific refusals.

## 6. Open decisions (do not implement past them)

- Edge number: DECIDED 4 tiles (owner 2026-11-04).
- Ranker objective weights (travel vs slot preservation): measure
  on the snapshot sim in S3, then decide - no blind tuning.
- UMS / no-BWEM maps (no regions): the spiral radius cap question
  (#236 left the reachability check; distance caps are untouched).
  Decided per slice, not globally.
- Legacy deletion order past S4: owner call per family, as in #147.
