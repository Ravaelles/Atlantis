package atlantis.placement.engine;
import atlantis.production.placement.PlacementPlanner;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.producibles.Producible;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.placement.core.*;
import atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding;
import atlantis.production.constructions.position.SiteTwin;
import atlantis.production.placement.*;
import atlantis.production.producibles.*;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.UnitStats;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The catalogue-backed {@link PlacementPlanner} - S1-S3 of
 * `_AI/redesign/03_PLACEMENT.md` §5.4.
 *
 * <p>
 * It answers the same seam the scheduler already consumes, so nothing outside
 * this class changes as the stages land. What it adds over
 * the legacy spiral planner - which returned one tile from
 * {@code APositionFinder} - is a <b>ranked list of validated candidates</b>: the
 * catalogue knows every tile on the map a footprint fits on (Blocks first, then a
 * per-tile scan), and the planner picks from it.
 * </p>
 *
 * <p>
 * S3 adds Protoss Psi gating: a building that {@code needsPower} only takes a
 * tile that is powered now (preferred) or will be powered by a Pylon already
 * under construction. A tile nothing can ever power is refused rather than
 * returned and discovered broken later - which is the failure mode
 * `_AI/POSITION-FINDER.md` describes.
 * </p>
 *
 * <p>
 * Deliberately not here yet: choke/wall placement (S4) and the base-fortification
 * <i>policy</i> (S5, which is goal generation, not placement).
 * </p>
 */
public final class CataloguePlacementPlanner implements PlacementPlanner {

    private static final GameContext LIVE_GAME = new GameContext() {
        @Override
        public boolean inGame() {
            return atlantis.Atlantis.game() != null;
        }
    };
    private static final GameContext NO_GAME = new GameContext() {        @Override
        public boolean inGame() {
            return false;
        }
    };
    private final TileAvailabilityGrid.TerrainSource terrain;
    private final RacePlacementStrategy strategy;
    private final NeighbourhoodRegistry neighbourhoods;
    private final boolean withBlocks;
    /**
     * Passes since the last terrain rescan. The terrain (buildability,
     * resources, depots) barely moves between passes, while the rescan
     * costs a full map walk (measured ~370 ms in sim) - so passes reuse
     * the grid via {@code clearReservations()} and rescan every 30th.
     * A stale window of at most 30 frames only ever blocks conservatively
     * (a depleted patch stays blocked a moment longer, never the reverse).
     */
    private int passesSinceTerrainRefresh = 0;
    private static final int TERRAIN_REFRESH_EVERY = 30;
    private final GameContext gameContext;
    /**
     * Tiles claimed in the current pass. The grid is rebuilt between passes (the
     * plan is stateless per frame), so a reservation is applied to the grid as
     * {@link TileAvailabilityGrid#markUsed} and disappears with it.
     */
    private final Set<String> reservedThisPass = new HashSet<>();
    private final List<SupportRequest> supportThisPass = new ArrayList<>();

    /**
     * Last natural-anchor centre twinned by this instance: the centre
     * barely moves within a game, so one row per anchor answers the
     * "which ground did V2 aim at" question without a per-frame twin.
     * Instance state (fresh planner per test, one planner per game).
     */
    private String lastCentredBase;
    private TileAvailabilityGrid grid;
    /**
     * Rebuilt per pass, so reservations do not survive a frame.
     */
    private BuildLocationCatalogue catalogue;
    /**
     * The worker this pass measures travel from; chosen once, reset by startPass.
     */
    private AUnit passBuilder;

    /**
     * The pass builder's position, fetched once per pass. The ranker asks
     * travel for every candidate (~10k per building goal) - re-reading the
     * live unit object there costs a full stub round-trip each time in the
     * sim, for an answer that cannot change mid-pass (the game is frozen
     * while the plan computes).
     */
    private APosition passBuilderPosition;

    public CataloguePlacementPlanner() {
        this(
            new EngineTerrainSource(),
            new atlantis.placement.race.ProtossPlacementStrategy(
                new PsiGating(new EnginePowerSource())
            ),
            new NeighbourhoodRegistry(new EngineNeighbourhoodSource())
        );
    }

    /**
     * Test seam: terrain over a synthetic map, no strategy, no neighbourhood ranking.
     */
    public CataloguePlacementPlanner(TileAvailabilityGrid grid) {
        this(grid, null, null, true, NO_GAME);
    }

    /**
     * Test seam with a strategy: a null strategy disables the availability check.
     */
    public CataloguePlacementPlanner(TileAvailabilityGrid grid, RacePlacementStrategy strategy) {
        this(grid, strategy, null, true, NO_GAME);
    }

    public CataloguePlacementPlanner(
        TileAvailabilityGrid grid, RacePlacementStrategy strategy, NeighbourhoodRegistry neighbourhoods
    ) {
        this(grid, strategy, neighbourhoods, true, NO_GAME);
    }

    /**
     * Production constructor: the terrain source is enough; pass 1 builds the grid.
     */
    public CataloguePlacementPlanner(
        TileAvailabilityGrid.TerrainSource terrain,
        RacePlacementStrategy strategy,
        NeighbourhoodRegistry neighbourhoods
    ) {
        this.terrain = terrain;
        this.strategy = strategy;
        this.neighbourhoods = neighbourhoods;
        this.withBlocks = true;
        this.gameContext = LIVE_GAME;
        // No game, no grid: constructing the planner must never touch the
        // live map (unit tests build the whole commander tree with no
        // game running). The first real startPass() builds it instead;
        // until then every reserve fails closed. The try covers stub games
        // that are present but incomplete (map dims without units) - a real
        // game is always complete, so this never triggers live.
        try {
            if (LIVE_GAME.inGame()) {
                this.grid = new TileAvailabilityGrid(terrain);
                this.catalogue = buildCatalogue();
            }
            else {
                this.grid = null;
                this.catalogue = null;
            }
        }
        catch (RuntimeException ignored) {
            this.grid = null;
            this.catalogue = null;
        }
    }

    /**
     * The full constructor. {@code withBlocks == false} builds the catalogue from the
     * per-tile scan alone - the seam a test uses when it wants the planner's own
     * rules (exact tiles, footprints, reservations) without whatever a Block happened
     * to reserve.
     */
    public CataloguePlacementPlanner(
        TileAvailabilityGrid grid, RacePlacementStrategy strategy,
        NeighbourhoodRegistry neighbourhoods, boolean withBlocks
    ) {
        this(grid, strategy, neighbourhoods, withBlocks, LIVE_GAME);
    }

    /**
     * The full constructor with an explicit game context (the last test seam).
     */
    public CataloguePlacementPlanner(
        TileAvailabilityGrid grid, RacePlacementStrategy strategy,
        NeighbourhoodRegistry neighbourhoods, boolean withBlocks, GameContext gameContext
    ) {
        this.terrain = null;   // a caller-supplied grid is reused as-is across passes
        this.grid = grid;
        this.strategy = strategy;
        this.neighbourhoods = neighbourhoods;
        this.withBlocks = withBlocks;
        this.gameContext = gameContext != null ? gameContext : LIVE_GAME;
        this.catalogue = buildCatalogue();
    }

    /**
     * The block factories a strategy contributes. With no strategy (a test) the
     * default Protoss-shaped table is used, so a bare-grid test still gets a
     * populated catalogue.
     */
    private static List<BuildLocationCatalogue.BlockFactory> factoriesFor(RacePlacementStrategy strategy) {
        final List<BuildBlock.Spec> specs = strategy != null
            ? strategy.blockTemplates()
            : atlantis.placement.blocks.BlockTemplates.normal();

        List<BuildLocationCatalogue.BlockFactory> factories = new ArrayList<>();
        for (final BuildBlock.Spec spec : specs) {
            factories.add(new BuildLocationCatalogue.BlockFactory() {
                @Override
                public BuildBlock at(int left, int top) {
                    return new BuildBlock(spec, left, top);
                }
            });
        }
        return factories;
    }

    /**
     * The start-block finder (C4), or null when the strategy offers no start
     * blocks - a race without an anchor layout, or a test.
     */
    private static StartBlockFinder startBlockFinderFor(
        TileAvailabilityGrid grid, RacePlacementStrategy strategy
    ) {
        if (strategy == null) return null;

        List<BuildBlock.Spec> starts = new ArrayList<>();
        for (BuildBlock.Spec spec : strategy.blockTemplates()) {
            if (spec.name.startsWith("Start")) starts.add(spec);
        }
        return starts.isEmpty() ? null : new StartBlockFinder(grid, starts);
    }

    private static AUnit nearestBaseUnit() {
        AUnit base = Select.main();

        return base != null ? base : Select.ourBases().first();
    }

    private static double squaredDistance(BuildLocation location, APosition centre) {
        double dx = location.tileX() - centre.tx();
        double dy = location.tileY() - centre.ty();

        return dx * dx + dy * dy;
    }

    private static AUnitType resolveUnitType(Producible producible) {
        if (producible instanceof UnitProducible) {
            return ((UnitProducible) producible).type();
        }
        return null;
    }

    /**
     * Support declared by this pass; drained by the composition root, cleared by startPass.
     */
    @Override
    public List<SupportRequest> drainSupportRequests() {
        List<SupportRequest> drained = new ArrayList<>(supportThisPass);
        supportThisPass.clear();

        return drained;
    }

    /**
     * Builds the catalogue for the current pass. Called once at construction and
     * again at every {@link #startPass()}, which is what makes the planner
     * stateless per frame: reservations made in one pass are gone in the next, so a
     * building that was skipped can be offered the same tile again tomorrow.
     */
    private BuildLocationCatalogue buildCatalogue() {
        return withBlocks
            ? new BuildLocationCatalogue(grid, factoriesFor(strategy),
            startBlockFinderFor(grid, strategy), ourBasePositions(), allBasePositions())
            : new BuildLocationCatalogue(grid, java.util.Collections
            .emptyList());
    }

    /**
     * Our bases as tile centres, so each gets a start-block anchor. Answers an
     * empty list when there is no game attached (a pure planner test) - anchoring
     * is a refinement, not a precondition of placing a building.
     *
     * <p>
     * The game check is explicit rather than a try/catch: {@code Select} answers
     * from a lazy cache, so a failure inside it surfaces on a later cache call, not
     * at the call site - which is exactly what a try/catch here would miss.
     * </p>
     */
    private List<int[]> ourBasePositions() {
        List<int[]> bases = new ArrayList<>();
        if (!gameContext.inGame()) return bases;

        for (AUnit base : Select.ourBasesWithUnfinished().list()) {
            if (base.position() != null) bases.add(new int[]{base.tx(), base.ty()});
        }
        return bases;
    }

    /**
     * Every known base location (own and future expansions), so normal
     * blocks never cover an expansion's Nexus spot. Empty without a
     * game (pure planner tests keep the old behavior); failures fail
     * open to no protection, never to no candidates.
     */
    private List<int[]> allBasePositions() {
        List<int[]> bases = new ArrayList<>();
        if (!gameContext.inGame()) return bases;

        try {
            for (atlantis.map.base.ABaseLocation base :
                atlantis.map.base.BaseLocations.baseLocations()) {
                if (base == null || base.position() == null) continue;
                bases.add(new int[]{base.position().tx(), base.position().ty()});
            }
        } catch (Exception | Error ignored) {
        }
        return bases;
    }

    @Override
    public void startPass() {
        reservedThisPass.clear();
        supportThisPass.clear();
        passBuilder = null;
        passBuilderPosition = null;

        // A caller-supplied grid is reused as-is across passes (terrain is
        // null, so there is nothing to rebuild from): drop the reservation
        // marks the previous pass left, or the second pass finds a map the
        // first one already consumed. Terrain marks stay untouched.

        // The plan is recomputed every frame, so the pass starts from a clean map:
        // a tile a building took in the previous pass is free again, and a proxy that
        // just finished changes the terrain answer. Where a terrain source is known
        // (production) the grid is rebuilt; a caller-supplied grid (a test that wants
        // to control the map) is kept as it was handed over.
        //
        // NOTE: the grid must be rebuilt on EVERY pass regardless of withBlocks. The
        // withBlocks flag only decides whether the block templates contribute, while
        // the fresh TileAvailabilityGrid is what clears the previous pass's
        // markUsed reservations - dropping it was measured to let a footprint
        // overlap a tile a building already took (PlacementPlannerTest).
        try {
            if (terrain != null && (gameContext == null || gameContext.inGame())) {
                if (grid == null || passesSinceTerrainRefresh >= TERRAIN_REFRESH_EVERY) {
                    this.grid = new TileAvailabilityGrid(terrain);
                    this.catalogue = buildCatalogue();
                    passesSinceTerrainRefresh = 0;
                }
                else {
                    passesSinceTerrainRefresh++;
                    grid.clearReservations();
                }
            }
            else if (grid != null) {
                grid.clearUsed();
            }
        }
        catch (RuntimeException ignored) {
            this.grid = null;
        }
        if (grid == null) this.catalogue = null;
    }

    /**
     * The worker every candidate in this pass is measured from: the nearest free
     * worker to our base. One per pass (not per building) so two candidates in one
     * frame cannot disagree about travel time. The gas branch reuses the same
     * worker to hand to the exact-geyser rule.
     */
    private AUnit builderForThisPass() {
        if (passBuilder == null || !passBuilder.isAlive()) {
            if (!gameContext.inGame()) return null;

            // Owner: always the free pool, never a hand-rolled chain -
            // a pass builder with a real job starves that job for the
            // whole pass while every frame agrees on travel time.
            AUnit anchor = nearestBaseUnit();
            passBuilder = anchor != null
                ? FreeWorkers.get().nearestTo(anchor)
                : FreeWorkers.get().first();
        }
        return passBuilder;
    }

    @Override
    public PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame) {
        return reservePlacement(building, constraint, targetFrame, java.util.Collections.<String>emptySet());
    }

    @Override
    public PlacementReservation reservePlacementExcluding(Producible building, TargetPlacement constraint,
                                                          int targetFrame, java.util.Set<String> excludedTiles) {
        return reservePlacement(building, constraint, targetFrame,
            excludedTiles == null ? java.util.Collections.<String>emptySet() : excludedTiles);
    }

    private PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame,
                                                  java.util.Set<String> excluded) {
        AUnitType unitType = resolveUnitType(building);
        if (unitType == null)
            return PlacementReservation.failure();

        if (unitType.getTilesWidth() <= 0 || unitType.getTilesHeights() <= 0) {
            return PlacementReservation.failure();
        }

        // No map, no siting: the planner was constructed without a game
        // (unit tests own the commander tree game-less) and no pass has
        // built the grid yet. Fail closed instead of dereferencing nulls.
        if (grid == null || catalogue == null)
            return PlacementReservation.failure();

        if (constraint.mode() == TargetPlacement.Mode.EXACT_TILE) {
            return reserveExact(constraint, unitType, targetFrame);
        }

        // A building of this type already ordered and not yet started keeps its
        // tile: the plan is recomputed every frame, so without this the planner
        // would hand out a FRESH tile each frame and the dispatcher would re-issue
        // the order endlessly (measured 2026-10-08: 1600+ "issued Pylon" lines in
        // one game, the tile creeping one step per frame). The legacy planner had
        // the same idea in carriedOver().
        PlacementReservation alreadyOrdered = carriedOver(unitType, targetFrame);
        if (alreadyOrdered != null) return alreadyOrdered;

        // Gas branch (exact-geyser, never a search): a refinery is legal only
        // on its geyser's own tile, which the terrain grid marks blocked (it
        // is a resource). The generic rank would never serve it, so gas goes
        // straight to the legacy exact-geyser rule and reserves that tile.
        if (unitType.isGasBuilding()) {
            return reserveGas(unitType, targetFrame);
        }

        // V3 S6: the once-planned layout owns issuance on the single
        // door too - the first free slot serves before the ranked hunt,
        // through the same legality ladder a hunted tile faces, so the
        // V2-direct path (which never funnels) issues from the layout.
        // A refused slot falls through to the hunt below, never declines.
        // A slot outside a bounded neighbourhood falls through too: the
        // sited door asked near its anchor, the layout lives at the main.
        APosition centre = searchCentre(constraint);
        if (centre == null)
            return PlacementReservation.failure();

        double slotRadius = constraint.radius();
        boolean slotBounded = constraint.mode() == TargetPlacement.Mode.NEIGHBOURHOOD && slotRadius >= 0;

        BuildLocation planned = firstFreePlannedSlot(unitType, excluded);
        if (planned != null
            && (!slotBounded
            || APosition.create(planned.tileX(), planned.tileY()).distTo(centre) <= slotRadius)) {
            int supportMark = supportThisPass.size();
            PlacementReservation slotAnswer = serveIfLegal(planned, unitType, targetFrame, null);
            if (slotAnswer != null) {
                // The single door proves it issues from the layout: hunted
                // tiles stay untwinned (as before), a served slot names
                // itself, so a game log tells slot issues apart from hunts.
                if (slotAnswer.isSuccessful()) {
                    SiteTwin.answered(unitType,
                        APosition.create(planned.tileX(), planned.tileY()),
                        "slot-planner", slotAnswer, null);

                    return slotAnswer;
                }

                // An unpowered slot declares its support (inside
                // serveIfLegal) but no longer ends the pass: the hunt below
                // may know a powered tile near standing power, and serving
                // it now beats waiting 600f for the support chain (measured
                // live: Gateway slot dark 3500f while Pylon power stood a
                // few tiles away). The speculative support is retracted when
                // the hunt serves - power extends only while nothing
                // powered exists. Refused slots still fall through as before
                // (serveIfLegal null), and an empty hunt keeps the support.
                PlacementReservation hunted = hunt(unitType, constraint, centre,
                    targetFrame, excluded);
                if (hunted != null && hunted.isSuccessful())
                    supportThisPass.subList(supportMark, supportThisPass.size()).clear();

                return hunted;
            }
        }

        return hunt(unitType, constraint, centre, targetFrame, excluded);
    }

    /**
     * The ranked hunt over catalogue candidates, shared by the plain pass
     * and the unpowered-slot fall-through (a dark slot declares support
     * but no longer parks the pass behind it). Serves the first legal
     * tile - powered ones only, an unpowered candidate declares support
     * and ends the hunt - or fails with the support kept for forwarding.
     */
    private PlacementReservation hunt(AUnitType unitType, TargetPlacement constraint, APosition centre,
                                      int targetFrame, java.util.Set<String> excluded) {
        // Named-area base anchors are adjudication-critical and otherwise
        // invisible on the V2 path (no funnel twin): a far-away warp
        // names whether the anchor or the serving was wrong. Change-gated
        // per planner instance, so one row per anchor, never per frame.
        if (unitType.isBase() && constraint != null
            && constraint.mode() == atlantis.production.placement.TargetPlacement.Mode.NAMED_AREA) {
            String centreKey = centre.tx() + "," + centre.ty();
            if (!centreKey.equals(lastCentredBase)) {
                lastCentredBase = centreKey;
                SiteTwin.centre(unitType, centre, "v2-direct");
            }
        }

        // Refine before ranking: the catalogue emits geometry it can know without
        // workers, and the ranker needs builder travel, the exit distance and when
        // the tile becomes usable at all. All three are estimates (§4.6) - they
        // decide ORDER, never correctness.
        List<BuildLocation> candidates = refine(
            unitType,
            catalogue.candidates(unitType.getTilesWidth(), unitType.getTilesHeights(), null),
            centre
        );

        BuildLocationCatalogue.LocationScorer ranker = rankerFor(unitType, centre);
        java.util.Collections.sort(candidates, ranker);

        // A bounded neighbourhood (the sited door passes its maxDistance)
        // serves only inside the radius: the legacy ring search never
        // left it either. Unbounded (funnel, V2-direct) serves anywhere
        // as before - the ranker alone decides order there.
        double radius = constraint.radius();
        boolean bounded = constraint.mode() == atlantis.production.placement.TargetPlacement.Mode.NEIGHBOURHOOD
            && radius >= 0;

        for (BuildLocation candidate : candidates) {
            String tile = candidate.tileX() + ":" + candidate.tileY();
            if (reservedThisPass.contains(tile) || excluded.contains(tile))
                continue;
            if (bounded && APosition.create(candidate.tileX(), candidate.tileY()).distTo(centre) > radius)
                continue;

            PlacementReservation answer = serveIfLegal(candidate, unitType, targetFrame, ranker);
            if (answer != null) return answer;
        }

        return PlacementReservation.failure();
    }

    /**
     * The first free planned slot of this type as a catalogue-shaped
     * candidate, or null. Occupancy is footprint overlap, standing and
     * unfinished alike: a warping Pylon and a parked row both hold
     * their slot, and a cancelled row frees it by ceasing to cover it
     * (no bookkeeping that could desync). Pure order, no mutation.
     */
    private BuildLocation firstFreePlannedSlot(AUnitType unitType, final java.util.Set<String> excluded) {
        if (unitType == null) return null;

        try {
            APosition tile = atlantis.placement.core.ReservedSlots.firstFreeSlotOfType(
                unitType,
                (slotTile, building) ->
                    (excluded != null && excluded.contains(slotTile.tx() + ":" + slotTile.ty()))
                        || atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding
                        .check(slotTile, building)
                        || atlantis.production.constructions.position.conditions.OverlapsOurUnfinishedBuilding
                        .check(slotTile, building));
            if (tile == null) return null;

            return new atlantis.placement.core.BuildLocation(
                tile.tx(), tile.ty(),
                unitType.getTilesWidth(), unitType.getTilesHeights(),
                0, 0, 0, false);
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    /**
     * One candidate through the whole legality ladder, hunt and slot
     * alike: refused tiles never serve, unpowered ones declare support
     * (and end the pass with failure, like the hunt), standing-covered
     * tiles never host a warp. Returns the reservation, or null when
     * this candidate is skipped and the hunt moves on.
     */
    private PlacementReservation serveIfLegal(BuildLocation candidate, AUnitType unitType, int targetFrame,
            BuildLocationCatalogue.LocationScorer ranker) {
        RacePlacementStrategy.AvailabilityVerdict availability = availabilityVerdict(unitType, candidate);
        if (availability == RacePlacementStrategy.AvailabilityVerdict.REFUSE) return null;

            // A tile a standing building already covers never hosts a warp:
            // serving it burns a builder walk plus a proximity-finish cancel
            // every frame while the need persists (NEXT #145: 17 same-tile
            // Pylon re-fires in the opening sim, 2065x on live [105,115]).
            // Completed buildings only - warping ones still finish and free
            // the tile (same rule as the sim's coveredByStanding and the
            // refresh path's blockingCompletedBuilding).
            if (standsOnCompletedBuilding(candidate, unitType)) return null;

            // A mineral-crowding tile never hosts a warp either: same shape as
            // the standing-cover check above, evaluated through the shared
            // legacy predicate so the two paths cannot disagree on it.
            // (Owner 2026-11-01: no Pylon within 5 tiles of minerals.)
            if (atlantis.production.constructions.position.conditions.TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(
                unitType, APosition.create(candidate.tileX(), candidate.tileY())
            )) return null;

            // An engine-refused tile never hosts a warp while the refusal
            // stands (NEXT #151): the engine said no there, so serving it
            // again burns a builder walk plus a refresh-cancel every pass
            // (measured live: Nexus [105,115] refused 1031 times, natural
            // never online). TTL-forgiven, like the funnel decline.
            if (atlantis.production.constructions.position.RefusedTiles.isRefused(
                APosition.create(candidate.tileX(), candidate.tileY()), unitType)) return null;

            // Expansion discipline (owner order 2026-11-04): a base warps
            // only next to a real base location (within 2 tiles) and away
            // from our standing or planned bases (15+ tiles). Measured
            // live Tomas Cere: a [120,0] edge tile with no base on it
            // parked a Nexus 105 s ("looks bugged" cancels, re-mint on
            // another edge tile). No map data (tests, sims without BWTA)
            // fails open to the old behavior.
            if (unitType.isBase() && !isLegalBaseSite(candidate.tileX(), candidate.tileY())) return null;

            // No worker-cover skip here (V3 S1, deferred): the funnel and
            // the legacy spiral decline worker-covered tiles through the
            // shared verdict, but this loop serves the sim opening on tiles
            // miners cover - HealthyMinimumBuildTest stalls with no Gateway
            // when the skip is added (bank 310, production stalled). The
            // sim assigns builders on covered tiles fine; the live
            // builderless death the funnel guards against is not modeled
            // here. Unify at the S4 single door with live proof, not by
            // breaking the sim-green opening now.

            // Map edge (owner order, S2 numbers, live-proven 2026-11-06:
            // the planner served x=0 Pylon tiles on TauCross while the
            // funnel declined them - the SUPPORT flow mints exact tiles
            // with no downstream verdict, so the loop owns the same rule.
            // Single source: the shared verdict predicate, emergency
            // exemption included (a supply-blocked Pylon/Gateway may still
            // take an edge tile, so frozen-warp recovery is not parked
            // behind the doctrine).
            if (atlantis.production.constructions.position.SitingVerdict.isTooCloseToEdge(
                unitType, APosition.create(candidate.tileX(), candidate.tileY()))) return null;

            // Reachability (owner 2026-11-01): the main base must walk
            // there by ground. Same rule as the funnel's sited path, so
            // the loop never serves a tile the funnel would decline.
            // Fail-open with no main yet (first frames, game-less tests).
            if (unreachableFromMain(APosition.create(candidate.tileX(), candidate.tileY()))) return null;

            if (availability == RacePlacementStrategy.AvailabilityVerdict.NEEDS_SUPPORT) {
                // Race-specific support (e.g. a Pylon) is declared by the strategy;
                // the composition root that ran this pass forwards it to
                // production - the generic planner never learns the support
                // unit's identity, and geometry never writes the queue.
                SupportRequest support =
                    strategy.requestAvailabilitySupport(candidate.tileX(), candidate.tileY(), unitType.name());
                if (support != null) supportThisPass.add(support);

                return PlacementReservation.failure();
            }

            reservedThisPass.add(candidate.tileX() + ":" + candidate.tileY());
            grid.markUsed(candidate.tileX(), candidate.tileY(),
                candidate.tileWidth(), candidate.tileHeight());

            PlacementReservation reservation =
                PlacementReservation.success(candidate.tileX(), candidate.tileY(), targetFrame);
            CoverageVerdict coverage = ranker == null ? null : ranker.coverageOf(candidate);
            if (coverage != null) {
                reservation = reservation.withCoverage(coverage.choke(), coverage.anchor());
            }
            return reservation;
    }

    /**
     * Fills in the two facts the catalogue cannot know: the builder travel estimate
     * and the distance to the neighbourhood's exit. One nearest worker per pass (the
     * same choice the legacy planner made), so every candidate is measured from the
     * same place and the ordering is stable frame to frame.
     *
     * <p>Deliberately the free worker, not the base: the ranker and its pins
     * (notably base siting near the natural) are tuned around worker-measured
     * travel. A base anchor was tried for cross-frame stability and moved the
     * natural answer out of its neighbourhood - same tie, other side.
     * Cross-frame flips are absorbed downstream instead (carriedOver
     * re-offers the owned tile, buildAt dedups it).</p>
     */
    private List<BuildLocation> refine(AUnitType unitType, List<BuildLocation> candidates,
                                       APosition centre) {
        AUnit builder = builderForThisPass();

        // The search centre's base area, once per pass: every candidate is
        // answered against it, so the ranker can keep buildings in the
        // area they were asked for (Stardust: blocks per area, never a
        // cross-map grab while home ground stands free).
        AUnit centreBase = nearestBaseFor(centre);

        List<BuildLocation> refined = new java.util.ArrayList<>(candidates.size());
        for (BuildLocation candidate : candidates) {
            BuildLocation withTravel = candidate.refined(
                builderTravelEstimate(builder, candidate),
                exitDistanceFor(candidate),
                sameBaseAs(candidate, centreBase)
            );
            refined.add(withTravel.withFramesUntilAvailable(availabilityFrames(unitType, candidate)));
        }
        return refined;
    }

    /**
     * Nearest owned base to a tile (Chebyshev, same rule as the
     * neighbourhood source), main as the pre-base fallback. Null when
     * nothing is known - the area answer fails open above it.
     */
    private static AUnit nearestBaseFor(APosition at) {
        if (at == null) return null;

        try {
            AUnit nearest = null;
            int best = Integer.MAX_VALUE;
            for (AUnit base : Select.ourBasesWithUnfinished().list()) {
                if (base == null) continue;
                int d = Math.max(Math.abs(base.tx() - at.tx()), Math.abs(base.ty() - at.ty()));
                if (d < best) {
                    best = d;
                    nearest = base;
                }
            }
            if (nearest == null) nearest = Select.main();

            return nearest;
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    /**
     * Same base area as the search centre: both tiles answer to the same
     * owned base. Unknown on either side reads same (fail-open: with no
     * bases listed there is no area to disagree about).
     */
    private static boolean sameBaseAs(BuildLocation candidate, AUnit centreBase) {
        if (centreBase == null) return true;

        AUnit candidateBase = nearestBaseFor(APosition.create(candidate.tileX(), candidate.tileY()));
        if (candidateBase == null) return true;

        return candidateBase.id() == centreBase.id();
    }

    /**
     * When the tile becomes usable. For a power-needing building this is the
     * Pylon's completion frame - the number the ranker sorts on first, so a tile
     * that is powered now always beats one that must wait.
     */
    private int availabilityFrames(AUnitType unitType, BuildLocation candidate) {
        if (strategy == null || !strategy.requiresAvailability(unitType.name())) return 0;

        return strategy.framesUntilAvailable(candidate.tileX(), candidate.tileY(), unitType.name());
    }

    /**
     * How long the builder needs to get there, in frames. Ground distance in tiles
     * at the slowest worker speed, plus a small floor - an estimate, deliberately
     * cheap: the ranker only needs the candidates ORDERED, and a path query per
     * candidate is the cost the catalogue exists to avoid (§4.6).
     */
    private int builderTravelEstimate(AUnit builder, BuildLocation candidate) {
        if (builder == null) return 0;

        APosition dest = APosition.create(candidate.tileX(), candidate.tileY());
        double tiles;
        if (atlantis.map.AMap.bwem() == null) {
            if (passBuilderPosition == null) passBuilderPosition = builder.position();
            APosition from = passBuilderPosition;
            tiles = (from == null || from.position() == null || dest == null)
                ? 999
                : atlantis.map.position.PositionUtil.distanceTo(from.position().p(), dest.position().p());
        }
        else {
            tiles = builder.groundDist(dest);
        }

        // A Probe moves ~0.7 tiles/s at the default speed; 30 frames/s, so about
        // 43 frames per tile. Rounded up, with a 20-frame floor for "already there".
        int framesPerTile = 43;

        return Math.max(20, (int) Math.ceil(tiles * framesPerTile));
    }

    private int exitDistanceFor(BuildLocation candidate) {
        if (neighbourhoods == null) return 0;

        NeighbourhoodRegistry.Neighbourhood area =
            neighbourhoods.forTile(candidate.tileX(), candidate.tileY());

        return NeighbourhoodRegistry.distanceToExit(area, candidate.tileX(), candidate.tileY());
    }

    /**
     * A construction of this type already ordered and not yet started, as a
     * reservation on the tile it already owns.
     *
     * <p>
     * This is what makes the planner idempotent across frames: the plan is
     * discarded and rebuilt every frame, so "start allocating a tile" must not mean
     * "allocate a different tile each time". The tile is marked used in this pass's
     * grid too, so a second building in the same frame cannot take it.
     * </p>
     */
    private PlacementReservation carriedOver(AUnitType unitType, int targetFrame) {
        for (atlantis.production.constructions.Construction construction
            : atlantis.production.constructions.ConstructionRequests.constructions) {
            if (construction.buildingType() != unitType) continue;
            if (construction.hasStarted()) continue;
            if (construction.buildPosition() == null) continue;

            int x = construction.buildPosition().tx();
            int y = construction.buildPosition().ty();

            // A refused tile is never carried over (NEXT #151): the
            // engine said no there, so the plan must name a different
            // tile - re-serving the cursed one loops the refresh-cancel
            // every pass until the TTL forgives it.
            if (atlantis.production.constructions.position.RefusedTiles.isRefused(
                APosition.create(x, y), unitType)) {
                return null;
            }

            reservedThisPass.add(x + ":" + y);
            grid.markUsed(x, y, unitType.getTilesWidth(), unitType.getTilesHeights());

            return PlacementReservation.success(x, y, targetFrame).committedAt(targetFrame);
        }

        return null;
    }

    /**
     * Race-neutral availability seam; each strategy owns its own support policy.
     */
    private RacePlacementStrategy.AvailabilityVerdict availabilityVerdict(
        AUnitType unitType, BuildLocation candidate) {
        if (strategy == null || !strategy.requiresAvailability(unitType.name())) {
            return RacePlacementStrategy.AvailabilityVerdict.AVAILABLE;
        }
        return strategy.availabilityVerdict(candidate.tileX(), candidate.tileY(), unitType.name());
    }

    /**
     * True when a standing completed building already covers this tile.
     * One shared predicate with the resite/adopt guards and the legacy
     * physical check (no second idea of "covered"). Null-safe by
     * construction: with no game running there are no standing buildings
     * to collide with.
     */
    private boolean standsOnCompletedBuilding(BuildLocation candidate, AUnitType unitType) {
        try {
            return OverlapsOurStandingBuilding.check(APosition.create(candidate.tileX(), candidate.tileY()), unitType);
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * An explicit tile is honoured only if it is free and fits - the fix for the
     * exact-position bug in `_AI/POSITION-FINDER.md` §2.2, where a stored tile was
     * handed back verbatim long after something had been built on it.
     */
    private PlacementReservation reserveGas(AUnitType unitType, int targetFrame) {
        APosition tile = atlantis.production.constructions.position.GasBuildingPositionFinder
            .findPositionForGasBuilding(unitType, passBuilder);
        if (tile == null) return PlacementReservation.failure();

        String key = tile.tx() + ":" + tile.ty();
        if (reservedThisPass.contains(key)) return PlacementReservation.failure();
        reservedThisPass.add(key);

        return PlacementReservation.success(tile.tx(), tile.ty(), targetFrame);
    }

    private PlacementReservation reserveExact(TargetPlacement constraint, AUnitType type, int targetFrame) {
        int x = constraint.exactTileX();
        int y = constraint.exactTileY();

        if (!grid.isFreeFor(x, y, type.getTilesWidth(), type.getTilesHeights())) {
            return PlacementReservation.failure();
        }

        APosition tile = APosition.create(x, y);

        // Exact tiles answer the same single verdict as every other tile:
        // an exact hint is a wish, not a proof (the SUPPORT flow mints
        // exact Pylon tiles with no downstream check, and sited orders
        // carry stale tiles). Refused, unpowered, mineral-crowded,
        // standing-covered, worker-covered or edge-exact all fail here
        // instead of minting an illegal warp. Power stays a verdict
        // answer (ACCEPT/REFUSE, never NEEDS_SUPPORT): an unpowered
        // exact tile is refused outright - the caller re-hints or waits.
        atlantis.production.constructions.position.SitingVerdict verdict =
            atlantis.production.constructions.position.SitingVerdict.check(type, tile);
        if (!verdict.accepted()) {
            return PlacementReservation.failure();
        }

        // Reachability stays with the caller (the verdict never reads
        // live state): the main base must walk there, same rule as the
        // funnel's sited path, so the two cannot disagree.
        if (unreachableFromMain(tile)) {
            return PlacementReservation.failure();
        }

        reservedThisPass.add(x + ":" + y);
        grid.markUsed(x, y, type.getTilesWidth(), type.getTilesHeights());

        return PlacementReservation.success(x, y, targetFrame);
    }

    /**
     * The main base must reach the tile by ground (owner 2026-11-01).
     * Same rule as the funnel's sited path - the two paths cannot
     * disagree on reachability. Fail-open with no main yet.
     */
    private static boolean unreachableFromMain(APosition site) {
        try {
            AUnit main = Select.mainOrAnyBuilding();
            if (main == null || main.position() == null) return false;

            return !atlantis.map.ReachableTiles.between(main.position(), site);
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    /**
     * The preference order, from {@link BuildLocationRanker}: availability first,
     * then the weighted distance score. The neighbourhood ranker is used when a
     * registry is installed; without one (a test) it falls back to pure distance
     * from the requested centre, which keeps tests independent of map geometry.
     */
    private BuildLocationCatalogue.LocationScorer rankerFor(AUnitType unitType, final APosition centre) {
        if (neighbourhoods == null) return nearestTo(centre);

        // A Photon Cannon exists to shoot whatever comes through the choke, so
        // proximity to it is the placement's whole point (S4; the DECISION to build
        // one is policy and lives in S5). Expressed as a scorer, not as its own
        // class, so the catalogue and the seam do not change for it.
        if (unitType.isCannon()) {
            BuildLocationCatalogue.LocationScorer coverage = coverageRankerFor(unitType, centre);
            if (coverage != null) return coverage;

            return new ChokeAffinityRanker(neighbourhoods);
        }

        return new BuildLocationRanker(
            false,
            BuildLocationRanker.isTechBuilding(unitType)
        );
    }

    /**
     * Coverage scoring needs the choke segment; anything unresolvable keeps the distance ranker.
     */
    private BuildLocationCatalogue.LocationScorer coverageRankerFor(AUnitType unitType, final APosition centre) {
        try {
            if (centre == null) return null;

            AChoke choke = Chokes.nearestChoke(centre);
            if (choke == null || choke.firstPoint() == null || choke.lastPoint() == null) return null;

            NeighbourhoodRegistry.Neighbourhood area = neighbourhoods.forTile(centre.tx(), centre.ty());
            APosition anchor = area == null ? null : APosition.create(area.originX(), area.originY());
            int rangeTiles = UnitStats.weaponRangeInTiles(unitType.groundWeapon());
            if (rangeTiles <= 0) return null;

            return new CoverageCannonRanker(choke.firstPoint(), choke.lastPoint(), anchor, rangeTiles);
        } catch (Exception e) {
            atlantis.util.log.ErrorLog.printMaxOncePerMinute("coverage-cannon-ranker",
                "CoverageCannonRanker resolution failed, distance ranker keeps the tile: " + e);

            return null;
        }
    }

    /**
     * Ordering: closer to the requested neighbourhood first.
     */
    private BuildLocationCatalogue.LocationScorer nearestTo(final APosition centre) {
        return new BuildLocationCatalogue.LocationScorer() {
            @Override
            public int compare(BuildLocation a, BuildLocation b) {
                double da = squaredDistance(a, centre);
                double db = squaredDistance(b, centre);

                return Double.compare(da, db);
            }
        };
    }

    /**
     * Where to search. {@code ANYWHERE} means "around our base", the same
     * convention the legacy pipeline uses; a named area resolves to its own
     * centre.
     */
    /**
     * Expansion anchor discipline (owner order 2026-11-04): a base tile
     * is legal only next to a real base location (within 2 tiles) and
     * away from our standing or planned bases (15+ tiles). Pure over
     * positions, so the boundary pins without a game; unknown map data
     * fails open to serving (tests and sims without BWTA keep the old
     * behavior). Public as a seam: the legacy siter takes the same
     * numbers when S4 routes it through the single door.
     */
    public static boolean isLegalBaseSite(int tx, int ty) {
        try {
            java.util.List<atlantis.map.position.HasPosition> bases =
                new java.util.ArrayList<atlantis.map.position.HasPosition>();
            for (atlantis.units.AUnit base : Select.ourBasesWithUnfinished().list()) {
                if (base != null && base.position() != null) bases.add(base.position());
            }

            return isLegalBaseSite(tx, ty,
                atlantis.map.base.BaseLocations.baseLocations(), bases);
        } catch (Exception | Error ignored) {
            return true;
        }
    }

    public static boolean isLegalBaseSite(int tx, int ty, java.util.List<? extends atlantis.map.position.HasPosition> baseLocations,
                                   java.util.List<? extends atlantis.map.position.HasPosition> ownBases) {
        // No map truth (tests, sims without BWEM): fail open to serving,
        // the old behavior. A real map always lists its bases.
        if (baseLocations == null || baseLocations.isEmpty()) return true;

        APosition tile = APosition.create(tx, ty);

        boolean nearBase = false;
        for (atlantis.map.position.HasPosition base : baseLocations) {
            if (base == null || base.position() == null) continue;
            if (tile.distTo(base.position()) <= 2.0) {
                nearBase = true;
                break;
            }
        }
        if (!nearBase) return false;

        if (ownBases != null) {
            for (atlantis.map.position.HasPosition own : ownBases) {
                if (own == null || own.position() == null) continue;
                if (tile.distTo(own.position()) < 15.0) return false;
            }
        }

        return true;
    }

    private APosition searchCentre(TargetPlacement constraint) {
        switch (constraint.mode()) {
            case NEIGHBOURHOOD:
                return APosition.create(constraint.tileX(), constraint.tileY());

            case NAMED_AREA:
                return areaCentre(constraint.areaName());

            case ANYWHERE:
            default:
                return baseCentre();
        }
    }

    private APosition areaCentre(String areaName) {
        if ("NATURAL".equals(areaName)) {
            APosition natural = atlantis.map.base.BaseLocations.natural();
            if (natural != null)

                return natural;
        }
        return baseCentre();
    }

    private APosition baseCentre() {
        // No game attached (a pure planner test): "anywhere" still has to mean
        // something, and with no base to prefer, the grid centre is the honest
        // answer - candidates are ranked by distance from whatever centre comes
        // back, and on a flat map every one is equally valid.
        if (!gameContext.inGame()) {
            return APosition.create(grid.width() / 2, grid.height() / 2);
        }

        AUnit base = Select.main();
        if (base == null)
            base = Select.ourBases().first();
        if (base == null)
            base = Select.ourBuildings().first();
        if (base != null)
            return base.position();

        // In game, but nothing stands yet (first frame of a game): fall back to
        // the map centre, which the legacy finder's "near" argument does not
        // tolerate being null.

        return APosition.create(grid.width() / 2, grid.height() / 2);
    }

    /**
     * Whether this planner is running against a live game. A seam so the planner's
     * own rules (blocks, tiles, reservations) can be exercised with no game at all,
     * instead of the core asking {@code atlantis.Atlantis} statically (DIP; the same
     * shape as every other port in {@code placement.core}).
     */
    public interface GameContext {
        boolean inGame();
    }
}
