# Production V3: one door for placing buildings

Owner-ordered migration doc (2026-11-04, written on owner order to
replace the V2 funnel / legacy / V2-direct split; supersedes the
2026-11-04 research note it grew from). This file is the single
source of truth for the migration: NEXT.md carries exactly one
delegation line to it and nothing else. A slice is done only when
every checkbox under it has its evidence filled in - the file makes
it impossible to claim completion while work from the list remains.

Status legend per slice: TODO (not started), DOING (branch work in
flight), DONE (evidence linked: test run, game id, ArchUnit output).
No slice closes on "it compiles".

## 0. Session-start protocol (binding on every worker, human or AI)

Before any code in this area:

1. `git log --oneline -5` + `git status --short` (where are we).
2. Read NEXT.md Active section (the one-line delegation lives there).
3. Read THIS file in full, including the status boxes.
4. Mirror back in one paragraph: where we are, what is next, what
   "done" means for the next slice. Only then touch code.

Skipping step 4 is how the three-door split survived "we cut
everything": summaries replaced verification. The mirror makes
misunderstanding visible before it costs a commit.

## 1. Why V3 (measured, not asserted)

Three live issuance paths mint constructions with three different
gate sets. Every recent production defect is the same shape: a row
enters through the path whose gate set is missing the relevant check.

- **Legacy spiral** (`FindPosition.findForBuilding`,
  `APositionFinder.findStandardPosition`): power via the footprint
  center (correct), 7-tile early-Pylon map edge
  (`ProtossTooCloseToMapBoundaries`), geyser/minerals, occupancy.
  Live callers: `RequestBuildingNear` (sited cannons/pylons/CC),
  `ProduceBuilding.validatePosition` (re-check), the three base
  siters (`FindPositionForBase` x3, `NextBasePosition`), plus
  `FindPositionForCannon` / Terran-Zerg remnants below.
- **Catalogue funnel** (`CatalogueSiteAdapter.siteFor`): proposes
  via the catalogue, validates per tile. Has power (footprint,
  fixed 2026-11-03), geyser, refused-tile and standing-cover gates -
  but map edge is a 1-tile adjacency margin only, and declined rows
  fall back to the legacy spiral (callers do).
- **V2-direct scheduler** (`ProductionScheduler.reservePlacement` +
  `GameOrderDirector.createConstruction`): mints the reservation
  tile verbatim with NO funnel validation. Its power verdict read
  the tile origin instead of the footprint center until the
  2026-11-03 fix; its edge rule is the same 1-tile margin.
- **Exact-position verbatim**
  (`DefineExactPositionForNewConstruction`): an
  `isUsingExactPosition` order hands the tile back without ANY
  validation (power, geyser, refused, edge) - only an occupancy
  check can force a fresh search.

Measured consequences on live games:

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
refuses, so siting defects are green by construction. Games
discover, tests cannot. The HealthyMinimumBuildTest pins
Pylon/Gateway/Core/Assimilator warps on a flat stub without geyser,
edge, or power pressure - the right contract, the wrong map.

Communication failure, owned (2026-11-04): the last weeks' fixes
(including the reviewer's own) patched one path each and thereby
cemented the three-door split instead of challenging it, and temp
harnesses used flat arenas while the TauCross snapshot vehicle
already existed (`src/atlantis/map/snapshot/`,
`src/tests/resources/taucross-snapshot.json`,
`SimMapSnapshotTest`, NEXT #191). Per CONVENTIONS §3 that challenge
was owed at the first multi-path touch, not now.

## 2. What good looks like (Stardust, read from source, no tuning)

`Stardust/src/Builder/BuildingPlacement.h` + `Blocks/`:

- Start block anchored RELATIVE TO the Nexus, fixed offsets, typed
  slots (2 Gateways, 2-4 tech, 3-4 cannons), a power Pylon that is
  PART OF the layout, and a reserved border-tile ring (walkable
  corridors, not decoration).
- Per-block edge rules (`allowTopEdge` / `allowLeftEdge` /
  `allowRightEdge` / `allowCorner`): every start-block variant
  returns false for all of them - start blocks never sit on an edge
  or corner.
- Pylon doctrine: the FIRST pylon is always in the start block; the
  next powers the choke cannon (PvP); the rest are ordered by
  distance to the mineral line. The producer takes the first pylon
  that either provides required build locations or keeps a minimum
  number of build locations available (slot preservation, not travel
  time).

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
  implementations that can disagree. New file, not a patch:
  `src/atlantis/placement/core/SitingVerdict.java`
  (ACCEPT / NEEDS_SUPPORT_PYLON / REFUSE + reason).
- ONE exit: `GameOrderDirector.createConstruction` trusts the
  verdict and only twins. No silent mints (closes NEXT #238's
  authorship question by construction: every row carries the
  verdict's reason).

## 4. Steps (each: pin red-first, implement, fast gate green, game proof where named)

### S0. Snapshot siting harness [Status: DONE 2026-11-04]
Prove `reservePlacement` runs headless on
`src/tests/resources/taucross-snapshot.json` (tiles, regions,
chokes, resources, starts). Vehicle for every pin below; no flat
stubs for siting ever again.
- ADD: `src/tests/fakes/SnapshotTerrain.java` +
  `src/tests/unit/SnapshotSitingTest.java` (vehicle + Pylon/Gateway
  reserve near a real main, deterministically).
- Verify: placement domain green (vehicle 2/2).
- Exit: a green test that fails if the planner cannot run without
  a live game. Evidence: commit 19124de29; SnapshotSitingTest green.

### S1. Single verdict [Status: DONE 2026-11-04]
Merge funnel + V2-direct + legacy per-tile power/geyser/refused/
standing checks into `SitingVerdict.check` (decision stays pure and
pinned; the two paths' twins already name their reasons).
- ADD: `src/atlantis/production/constructions/position/SitingVerdict.java`
  (lives next to the predicates it consults - the placement engine
  already imports that package, so no new arch edges - not in
  `placement/core` as first sketched) + `SitingVerdictTest` decision
  table per gate x building type, incl. the live [123,26] shape, +
  funnel geyser twin.
- MODIFY: `CatalogueSiteAdapter` (delegates; gains the geyser twin it
  never named), `PositionFulfillsAllConditions` (delegates; gains
  worker-cover). `ProtossPlacementStrategy` NOT delegated: NEEDS_SUPPORT
  stays in the V2-direct availability path by decision (support flow
  vs finished-tile verdict). `EnginePowerSource` stays
  (already footprint-correct).
- DELETE: nothing yet.
- Verify: new pin green (red-first: funnel served the crowding tile);
  `GatewayPsiEdgeTest` + placement domain green; `grep -rn
  "PsiField.covers" src/atlantis --include=*.java` shows exactly two
  files (`EnginePowerSource` + the funnel's `IsPoweredByAPylon`).
- Exit: one function owns every power/geyser/refused/standing/worker
  answer for funnel + legacy. V2-direct worker-cover DELIBERATELY
  OUT: the sim assigns builders on covered tiles fine and
  HealthyMinimumBuildTest stalls with no Gateway when the loop skips
  them - the live builderless death is not modeled in the sim.
  Unify at the S4 single door with live proof (comment in
  `reservePlacement`). Evidence: commit 8dd2c81c7; fast gate 1099/0.
- Live proof pending (owner runs games only on call).

### S2. Edge parity: 4 tiles everywhere (owner decision 2026-11-04)
[Status: DONE 2026-11-04]
- MODIFY: the single verdict enforces the 4-tile map-edge rule
  (`EDGE_MARGIN_TILES`, footprint-aware, Protoss non-base non-gas,
  null-safe; shared `isTooCloseToEdge` so the V2-direct loop consults
  the same numbers without taking the full verdict - unpowered must
  read NEEDS_SUPPORT, not REFUSE); `ProtossTooCloseToMapBoundaries`
  (legacy 7-tile early-Pylon rule) is deleted and its callers use the
  verdict (chain; finder duplicate removed, fallback kept).
- MODIFY: `ProtossTooCloseToMapBoundariesTest` retires into
  `SitingVerdictTest` edge pins (boundary 3-refused/4-accepted, right
  edge 122-accepted/126-refused, base/gas/Terran exempt, twin,
  supply-blocked emergency exempt, TauCross-grounded verdict pin);
  `PylonPosition` (`FIRST_PYLON_TILES_FROM_BASE` stays 7 - that is
  a distance-from-base, not an edge rule; only the edge numbers move
  7->4 / 5->4).
- Verify: updated pins green (11/11); placement green.
- Exit: `grep -rn "marginPx"` finds nothing; `grep -rn "7 * tile"`
  finds only history comments + distance-from-base; the only edge
  numbers left are the two 4s. Evidence: commit f875c0d26.
- Follow-up (same day): the V2-direct loop consult was reverted -
  the frozen-warp recovery needs an edge tile in sim while sim
  supply reads are stubbed (StalledWarpRecoverySimTest bisected it),
  same sim-gap class as the S1 worker-cover deferral; the loop keeps
  serving, the verdict still owns the rule. The supply-blocked
  Gateway/Pylon emergency exemption from the deleted rule is kept
  (live parity) and pinned. Evidence: commit 5e29d257a.
- Live proof pending (owner runs games only on call).

### S3. Start-block-first Pylon [Status: REVERTED 2026-11-04]
The slot served (pin 4/4 green, SimOpeningToFifty green) but shifts
the whole opening layout, and the shifted trajectory stalls the
pinned StalledWarpRecoverySimTest at f18600 (931 banked, no warp):
with the slot active the recovery fails even with both edge
mechanisms disabled, while without it the suite is green (bisected
across S1/S2/S3 worktrees). The blocker in the shifted world is a
non-edge gate - a layout-density vs crowding-rule doctrine
interaction that needs owner input and live proof, not a silent
sitter swap. S3 returns after that. Revert removes the helper and
its pin together; S0-S2 stand.

### S4. Kill switches, one per commit [Status: BLOCKED on live proof]
Route each legacy caller through the single door, then delete the
orphaned file to `src-old/`. Order: `RequestBuildingNear` sited
path, `ProduceBuilding.validatePosition`,
`FindPositionForBase` trio + `NextBasePosition`,
`FindPositionForCannon` (+ Terran/Zerg callers, dead under
Protoss-only - verify, don't assume).
- DELETE (each its own commit, suite green): `FindPosition.java`,
  `APositionFinder.java` (keep `clearCache` callers working -
  they call a static that moves with the cache, not the finder),
  `ProtossPositionFinder.java`, `FindPositionForBase.java`,
  `NextBasePosition.java`, `FindPositionForCannon.java`,
  `terran/` + `zerg/` position files (audit 2026-11-04: NOT dead -
  `TerranPositionFinder` has a live combat caller
  (`NewBunkerPositionFinder`) and chain guards - verify, don't assume).
  NOTE 2026-11-04: each deletion removes a live fallback path whose
  behavior the sim cannot model (exact-position sited cannons, base
  siters, last-resort engine-buildable). Per CONVENTIONS §3 they need
  one live Wine confirmation each - owner runs games only on call,
  so this slice waits for the owner. Suite-green alone is not the
  gate here.
  `PylonPosition.java` (superseded by S3),
  `ProtossDefineNearTo.java` (+ `DefineNearTo.java`,
  `DefineCannonNearTo.java` if orphaned),
  `GasBuildingPositionFinder.java` (only after the gas
...[truncated 2085 chars]

### S5. Live-hardening batch + single minter [Status: DONE 2026-11-04]
Wine baselines 20261104_101505 (TauCross vs Grendel, 23 warps, zero
duplicates) and scbw GAME_9B5AB3C1 (vs Steamhammer, 4 warps, zero
Gateways ever - the reported failure reproduced) showed in the twins:
81 no-op POSITION_REFRESH + stderr lines for one Gateway on a refused
tile, builders walking to cursed tiles, a refused row holding its
tile through dead builders, no per-building unit-log key.
- Unit log names the building on the command half:
  `#Build-Pylon/Pylon/BuilderManager` (OrderWatcher; BuilderOrderLogTest).
- Refresh spam killed: twin + stderr only fire when the site actually
  moves; same-tile set skipped; null-position println removed
  (RefusedTileResitesRowTest pins silence + one twin per real move).
- Refused travel gate in ShouldNotTravelToConstructYet (pinned in
  TravelForecastTest): no walks to engine-refused tiles; builder stays
  claimed and mines while the refresh moves the site.
- Cancel+re-request reuses the live queue row instead of minting a
  twin next to it (pinned: still exactly one row, same minSupply).
- V2 mint door refuses a second concurrent single-demand tech
  building (Core/Citadel/Forge/...; pinned red-first in
  TechDuplicateMintTest; Pylon pairs still mint twice).
- Single minting door enforced: ConstructionRequests.addConstruction
  refuses same-type+same-tile doubles (pinned red-first in
  SingleDoorMintTest; neighbour tiles still mint, null sites bypass).
- V2 mint door refuses what no builder can start (owner order
  2026-11-04): overrun with no free builders minted-and-cancelled
  the same Pylon tile 170 times in 7 s live (GAME_D1AA02FE, fresh
  cid+oid every frame, "looks bugged" cancelling the order while
  de-dup guards saw nothing live). The demand survives the BUILD_REFUSED
  twin and re-dispatches when builders exist (pinned red-first in
  MintWithoutBuilderIsRefusedTest, both directions).
- Travel-anchor experiment (base instead of free worker): REVERTED -
  moved the natural answer out of its neighbourhood
  (BaseSitingCatalogueTest), ranker tuned around worker travel.
- Resite without a live builder (proxy validation, shared
  findPositionForNewBuilding overload): the funnel's no-builder decline
  froze rows whose builder died on a refused tile. Pinned (guard
  contract); live proof pending.
- V2 pylon cap ported to the mint door (same legacy function, one
  implementation); Gateway pairs unaffected (pinned).
- Verify: placement 87/0, fast 1100/0, combat 746/0, e2e 41/0
  (incl. stall recovery), acceptance only the 6 pre-existing #147
  queue failures.
- Verification loop (owner ask, Java-only 2026-11-04 -
  scripts/analyze-game.sh deleted, digest takes the verdict):
  `PostGameDigest` writes `bwapi-data/write/POSTGAME_SUMMARY.json`
  {errors, byKey, samples (max 10/key), construction} and prints one
  bounded console section on both exit paths. Bot side:
  BuildOrderAudit mismatch lines read "[MED] Construction issue
  detected: Gateway#11 built at 10 supply instead 11"; clean games
  print "[LOW] No issues in Build Order execution detected."
  Twin HIGH/CRIT rows are errors; twin-MEDIUM stays out of the
  counts (telemetry chatter by construction).
- Remaining V3 (needs one live game of proof each, owner-gated):
  route sited requests (RequestBuildingNear fallback) and the cannon
  direct siter (ProtossPhotonCannonAntiLand) through reservePlacement;
  base siters (FindPositionForBase/NextBasePosition) after V2 proves
  natural/expansion siting alone; dead dispatcher BUILD branch +
  requestBuilding/validatePosition/adoptPreparedSite + BuildGates
  (pinned doctrine - port each gate to V2 with its test before
  deleting; Terran/Zerg files stay - live combat caller).
- Game proof: scbw GAME_1077E111 (this tree minus audit strings)
  warped Gateway 1:23 + Core with zero spam twins and zero cancels;
  final-tree scbw GAME_921C9B8D (jar fingerprint matches 8ea97aaac,
  vs Steamhammer, 12:15 harness timeout, no winner): 23 warps
  (Pylon 12, Gateway 3, Core, Assimilator, Forge, Citadel, TemplarA,
  Stargate 2, Nexus attempt), `HIGH=12 MED=1 CRIT=1 LOW=0 warps=23
  offPlan=0 sameTileDups=0 status=issues` - zero same-tile dups,
  zero off-plan; remaining HIGHs are the S4 base-siter remainder
  (3x Nexus expansion cancel at [94,112] "Can't build here"),
  2x early STALL, 1x CLOSE_WARPS Pylon-pair overlap signal
  (benign: pairs allowed), 1x late SUPPLY_OVERSTOCK. No audit lines
  (LOW=0): the timeout never reached OnGameEnd, same shape as the
  BuildingLedger javadoc notes.
- Java postgame digest (owner order 2026-11-04, overruling the
  script-side aggregation: analysis is project code, not a sidecar):
  `atlantis.util.log.PostGameDigest` aggregates in memory (twin feed
  from `EventLog.append`, console feed from `AConsole`), counts per
  [LEVEL] KEY with at most 10 samples per key, writes
  `bwapi-data/write/POSTGAME_SUMMARY.json` and prints one bounded
  console section from `GameSummary` on both exit paths (OnGameEnd +
  shutdown hook). Routine-guard stack trace (`AddToQueue` Nexus
  no-expand) replaced by a BUILD_REFUSED twin; untagged construction
  `System.err` lines routed through tagged `AConsole` (Refresh moves
  MEDIUM + capped at 10/type per owner 2026-11-04 - an unplaceable
  building disrupts everything downstream; cancels/errors HIGH/CRIT).
  A crashed JVM still yields no digest - the periodic twins/ledger
  stay the record for those games. Halt lifted 2026-11-04 (owner):
  one quiet game at a time, batched verification.
- Live verification batch (owner order): GAME_DE15E51B (timeout,
  23 warps, digest works end-to-end, NPE storm gone), GAME_D935C37C
  (2:26 loss), GAME_1923B7E0 (3:48 loss, Gateway#1+#2 both off-plan
  at 16 - placement drought, B1 gate correctly silent: bank never
  hit 300 with the BO open... UNPROVEN, twin pending),
  GAME_68F87352 (realtime-out 16:43, 19 warps). Recurring shape all
  four games: first Gateway unplaceable until the second Pylon opens
  a powered pocket (SCHEDULE_REFUSED no-placement with 600f
  heartbeat now names the drought length). Open: support-Pylon flow
  never fires despite NEEDS_SUPPORT verdict existing (no V2 support
  request twinned in any game); B1 live trigger unobserved (Core
  warps while the BO is open in 68F87352 - bank at goal time not
  decidable from twins, the new block twin adjudicates next game);
  Nexus anchor twin deployed, awaiting an expansion game.
- Mint-door builder check (owner order 2026-11-04): overrun with no
  free builders minted-and-cancelled the same Pylon tile 170 times in
  7 s live (GAME_D1AA02FE, fresh cid+oid every frame, "looks bugged"
  cancelling the order while de-dup guards saw nothing). The V2 door
  now refuses the mint when no builder was assigned (mirrors the
  legacy mint door + adopt path's no-free-builder guard) and twins
  BUILD_REFUSED/no-builder change-gated; the demand survives and
  re-dispatches. Pinned red-first both directions.
- Ledger tracks warp completion (owner order 2026-11-04): the
  same-tile-dup verdict counted a cancelled warp re-served as a
  double-mint (GAME_BAF8D14E: Pylon [121,22] warped 2:20 and 4:15, no
  death or cancel twin anywhere). Rows now carry completed, set on
  unit completion; the dup counts only repeats after a completion.
  Completion matches by origin tile (unit.tilePosition(), the same
  source the recorder uses) - the first version read center-derived
  tx()/ty(), which differ for every footprint, so zero warps ever
  matched live; pinned end-to-end through the completion hook.
- B1 reads the BO from the same rows the scheduler does
  (BuildOrderGoals.isFinished over buildOrderRows + live progress):
  the gate used to consult the parallel legacy queue, which can
  disagree with the door it gates - silent live passes with an
  unfinished BO and a poor bank while two games showed early Cores.
- Parked STARTING rows name themselves + stolen builders re-arm
  (owner order 2026-11-04, NEXT #150): a Nexus sat 9 minutes with
  its builder assigned and no twin anywhere - demoteParkedStarting
  shields on motion, and miners trip every few seconds so the 2400f
  clock never fills. twinParkedStarting fires once per ancient
  (2400f+) STARTING row regardless of motion, naming builder state;
  the demote gains the stolen-builder doctrine (gathering + ancient
  re-arms even while moving, young rows keep their walk window).
  Pinned red-first (ParkedStartingTwinTest 3/3 green, new demote
  pins 9/9 green, verified red on stashed old code) + main() repro
  under src/tests-aux.
- Console hygiene (owner order: no MED/HIGH in output): end-of-game
  and startup rollups read LOW, never MED - POSTGAME DIGEST +
  BUILD_ORDER audit header + Reserved slots announce + GameSummary
  blocks + LongFrames + Game-ended + config valid. Real anomaly
  lines (off-plan warps, refresh moves, cancels, stalls) keep
  their MEDIUM/HIGH and must be fixed to zero, not demoted.
- S4 cuts 2-3 (dead code, zero live callers, suite green 1127/0):
  cut 2 removes the anti-land/air commander subtree
  (DynamicBuildingCommander + AntiLand/AntiAir + both Protoss
  photon-cannon siters + TerranBunker); cut 3 removes the
  bunker-siting family (NewBunker + PositionFinder +
  ApproximatePosition + ShouldPlaceBunker + both Estimates).
  Remaining S4 items (sited fallback, base siters, BuildGates,
  ProduceBuilding helpers) have live callers and wait for one
  Wine game of proof each.
- Live verification GAME_4382BF70 (this tree, vs Steamhammer Zerg,
  11:14 loss, no crash): console hygiene holds - digest/audit/slots/
  summaries all [LOW], no spam twins; new PARKED twin silent (0 rows,
  no ancient parks), stolen-builder demote unfired (no theft shape
  this game). Real anomalies named, not hidden: 5 off-plan BO warps
  (placement/timing drift), 1 same-tile Core dup (Critical emergency
  re-fire), 6 Critical cancels + 5:40/6:17 phantom cannons from a
  2:37 hidden-units call vs Zerg (Lurkers impossible before 5:00) -
  the response now gates on earliest timing (see below), 1 PhotonC
  refresh move (sited cannon routing, S4 remainder), 12 STALL rows
  (army hold, combat workstream), 3 marginal SUPPLY_OVERSTOCK
  (free=15). Console [MED]/[HIGH] left: HELLO marker (protocol),
  5 off-plan lines, 1 refresh move, 2 transient builder lines
  (now LOW next game), CherryVis/cannonsNeeded (now LOW next game).
- Hidden-units earliest-timing gate (owner order 2026-11-04): the
  emergency (cannons + Critical cancels gutting live Core/Gateways)
  cannot fire before 4:00 (DTs 4:30+, Lurkers 5:00+). Pinned
  red-first (HiddenUnitsTimingGateTest 2/2); live proof next game.

### S6. Pre-planned slots, display first (owner order 2026-11-04)
Per-frame siting hunts make every game pay the search at its most
contended moment (measured live: first Gateway unplaceable until
the second Pylon; Nexus parked at y=0). Stardust answers this with
blocks: the layout is picked once, slots are typed and numbered,
issuance takes a slot instead of searching. Slice 1 (this commit)
is display-only and behavior-free by construction: at game start
`ReservedSlots` picks pylons + 10 gateways (G1..G10) near the main
from the same catalogue the planner ranks, and full-mode painting
draws the labels - a weird layout cannot break a game, only draw.
- ADD: `placement/core/ReservedSlots.java` (plan once at game start
  via OnGameStarted, paint in full mode) + `ReservedSlotsTest`
  decision table (10 G + P slots, distinct buildable tiles, G1
  nearest, deterministic across runs) on the TauCross snapshot.
- MODIFY: `OnGameStarted` (plan once, guarded), `AAdvancedPainter`
  (draw labels in full mode only).
- Verify: placement domain green; a live full-mode game shows
  G1..G10/P1.. on the ground (owner visual check).
- Next slice (needs its own pin + live proof): wire issuance to
  take reserved slots (funnel first), then delete the per-frame
  fallback search for covered types. S3 (start-block-first Pylon,
  reverted for shifting the whole opening) returns in this form:
  slots, not sitters.
