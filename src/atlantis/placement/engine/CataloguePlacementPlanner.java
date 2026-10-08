package atlantis.placement.engine;

import atlantis.map.position.APosition;
import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.BuildLocation;
import atlantis.placement.core.BuildLocationCatalogue;
import atlantis.placement.core.BuildLocationRanker;
import atlantis.placement.core.NeighbourhoodRegistry;
import atlantis.placement.core.PsiGating;
import atlantis.placement.core.RacePlacementStrategy;
import atlantis.placement.core.StartBlockFinder;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.production.v2.LegacyPlacementPlanner;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.PlacementPlanner;
import atlantis.production.v2.Producible;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

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
 * {@link LegacyPlacementPlanner} - which returned one tile from
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

    private final TileAvailabilityGrid grid;
    private final BuildLocationCatalogue catalogue;
    private final RacePlacementStrategy strategy;
    private final NeighbourhoodRegistry neighbourhoods;

    /**
     * Tiles claimed in the current pass. The grid is rebuilt between passes (the
     * plan is stateless per frame), so a reservation is applied to the grid as
     * {@link TileAvailabilityGrid#markUsed} and disappears with it.
     */
    private final Set<String> reservedThisPass = new HashSet<>();

    /** The worker this pass measures travel from; chosen once, reset by startPass. */
    private AUnit passBuilder;

    public CataloguePlacementPlanner() {
        this(
            new TileAvailabilityGrid(new EngineTerrainSource()),
            new atlantis.placement.race.ProtossPlacementStrategy(
                new PsiGating(new EnginePowerSource())
            ),
            new NeighbourhoodRegistry(new EngineNeighbourhoodSource())
        );
    }

    /** Test seam: a grid over a synthetic terrain, no strategy, no neighbourhood ranking. */
    public CataloguePlacementPlanner(TileAvailabilityGrid grid) {
        this(grid, (RacePlacementStrategy) null, null);
    }

    /** Test seam with a strategy: a null strategy disables the availability check. */
    public CataloguePlacementPlanner(TileAvailabilityGrid grid, RacePlacementStrategy strategy) {
        this(grid, strategy, null);
    }

    public CataloguePlacementPlanner(
        TileAvailabilityGrid grid, RacePlacementStrategy strategy, NeighbourhoodRegistry neighbourhoods
    ) {
        this.grid = grid;
        this.strategy = strategy;
        this.neighbourhoods = neighbourhoods;
        this.catalogue = new BuildLocationCatalogue(
            grid,
            factoriesFor(strategy),
            startBlockFinderFor(grid, strategy),
            ourBasePositions()
        );
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

    /** Our bases as tile centres, so each gets a start-block anchor. */
    private static List<int[]> ourBasePositions() {
        List<int[]> bases = new ArrayList<>();
        for (AUnit base : Select.ourBasesWithUnfinished().list()) {
            if (base.position() != null) bases.add(new int[]{base.tx(), base.ty()});
        }
        return bases;
    }

    @Override
    public void startPass() {
        reservedThisPass.clear();
        passBuilder = null;
    }

    /**
     * The worker every candidate in this pass is measured from: the nearest free
     * worker to our base. One per pass (not per building) so two candidates in one
     * frame cannot disagree about travel time.
     */
    private AUnit builderForThisPass() {
        if (passBuilder == null || !passBuilder.isAlive()) {
            AUnit anchor = nearestBaseUnit();
            passBuilder = anchor != null
                ? Select.ourWorkers().nearestTo(anchor)
                : Select.ourWorkers().first();
            if (passBuilder == null) passBuilder = Select.ourWorkers().first();
        }
        return passBuilder;
    }

    private static AUnit nearestBaseUnit() {
        AUnit base = Select.main();
        return base != null ? base : Select.ourBases().first();
    }

    @Override
    public PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame) {
        AUnitType unitType = resolveUnitType(building);
        if (unitType == null)
            return PlacementReservation.failure();
        if (unitType.getTilesWidth() <= 0 || unitType.getTilesHeights() <= 0) {
            return PlacementReservation.failure();
        }

        if (constraint.mode() == TargetPlacement.Mode.EXACT_TILE) {
            return reserveExact(constraint, unitType, targetFrame);
        }

        APosition centre = searchCentre(constraint);
        if (centre == null)
            return PlacementReservation.failure();

        // Refine before ranking: the catalogue emits geometry it can know without
        // workers, and the ranker needs builder travel, the exit distance and when
        // the tile becomes usable at all. All three are estimates (§4.6) - they
        // decide ORDER, never correctness.
        List<BuildLocation> candidates = refine(
            unitType,
            catalogue.candidates(unitType.getTilesWidth(), unitType.getTilesHeights(), null)
        );

        java.util.Collections.sort(candidates, rankerFor(unitType, centre));

        for (BuildLocation candidate : candidates) {
            String tile = candidate.tileX() + ":" + candidate.tileY();
            if (reservedThisPass.contains(tile))
                continue;

            PsiGating.PowerVerdict power = powerVerdict(unitType, candidate);
            if (power == PsiGating.PowerVerdict.REFUSE) continue;

            if (power == PsiGating.PowerVerdict.NEEDS_NEW_PYLON) {
                // The spot is good, it just needs power: ask for a Pylon near it and
                // let the NEXT pass place this building on the now-powered tile. That
                // is Stardust's pull-forward in its simplest form - nothing is
                // reserved here, so a later frame re-decides with the Pylon coming.
                requestPylonNear(candidate);
                return PlacementReservation.failure();
            }

            reservedThisPass.add(tile);
            grid.markUsed(candidate.tileX(), candidate.tileY(),
                    candidate.tileWidth(), candidate.tileHeight());

            return PlacementReservation.success(candidate.tileX(), candidate.tileY(), targetFrame);
        }

        return PlacementReservation.failure();
    }

    /**
     * Fills in the two facts the catalogue cannot know: the builder travel estimate
     * and the distance to the neighbourhood's exit. One nearest worker per pass (the
     * same choice the legacy planner made), so every candidate is measured from the
     * same place and the ordering is stable frame to frame.
     */
    private List<BuildLocation> refine(AUnitType unitType, List<BuildLocation> candidates) {
        AUnit builder = builderForThisPass();

        List<BuildLocation> refined = new java.util.ArrayList<>(candidates.size());
        for (BuildLocation candidate : candidates) {
            BuildLocation withTravel = candidate.refined(
                builderTravelEstimate(builder, candidate),
                exitDistanceFor(candidate)
            );
            refined.add(withTravel.withFramesUntilAvailable(availabilityFrames(unitType, candidate)));
        }
        return refined;
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

        double tiles = builder.groundDist(
            APosition.create(candidate.tileX(), candidate.tileY())
        );

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
     * Psi verdict for a candidate. A building that needs no power is always
     * accepted; with gating disabled (a test seam, or a non-Protoss game) so is
     * every tile.
     */
    /**
     * Availability verdict for a candidate. The race strategy owns what "available"
     * means (Protoss: Pylon power); a race that needs no availability concept, or a
     * test without a strategy, accepts every tile.
     */
    private PsiGating.PowerVerdict powerVerdict(AUnitType unitType, BuildLocation candidate) {
        if (strategy == null) return PsiGating.PowerVerdict.ACCEPT;
        if (!strategy.requiresAvailability(unitType.name())) return PsiGating.PowerVerdict.ACCEPT;

        // The Protoss strategy is the one that answers NEEDS_NEW_PYLON; ask it
        // through the gating seam when it has one, otherwise treat "available" as
        // a boolean and refuse only what is never available.
        if (strategy instanceof atlantis.placement.race.ProtossPlacementStrategy) {
            return ((atlantis.placement.race.ProtossPlacementStrategy) strategy).gating()
                .verdictFor(candidate.tileX(), candidate.tileY());
        }

        return strategy.framesUntilAvailable(candidate.tileX(), candidate.tileY(), unitType.name()) >= 0
            ? PsiGating.PowerVerdict.ACCEPT
            : PsiGating.PowerVerdict.REFUSE;
    }
    /**
     * Asks for a Pylon near a good-but-unpowered spot (S3 pull-forward).
     *
     * <p>
     * The request goes through the ordinary production queue rather than a private
     * channel: the Pylon is a normal goal the production engine will place and
     * build, and this planner will then find the tile powered on a later pass. The
     * queue call is guarded, so a build without the legacy queue (a unit test, a
     * future v2-only build) simply records nothing.
     * </p>
     */
    private void requestPylonNear(BuildLocation candidate) {
        try {
            atlantis.production.orders.production.queue.add.AddToQueue.withHighPriority(
                AUnitType.Protoss_Pylon,
                APosition.create(candidate.tileX(), candidate.tileY())
            );
        } catch (Throwable t) {
            // A build without the legacy queue: the next pass re-decides anyway.
        }
    }

    /**
     * An explicit tile is honoured only if it is free and fits - the fix for the
     * exact-position bug in `_AI/POSITION-FINDER.md` §2.2, where a stored tile was
     * handed back verbatim long after something had been built on it.
     */
    private PlacementReservation reserveExact(TargetPlacement constraint, AUnitType type, int targetFrame) {
        int x = constraint.exactTileX();
        int y = constraint.exactTileY();

        if (!grid.isFreeFor(x, y, type.getTilesWidth(), type.getTilesHeights())) {
            return PlacementReservation.failure();
        }

        reservedThisPass.add(x + ":" + y);
        grid.markUsed(x, y, type.getTilesWidth(), type.getTilesHeights());
        return PlacementReservation.success(x, y, targetFrame);
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
            return new ChokeAffinityRanker(neighbourhoods);
        }

        return new BuildLocationRanker(
            neighbourhoods,
            false,
            BuildLocationRanker.isTechBuilding(unitType)
        );
    }

    /** Ordering: closer to the requested neighbourhood first. */
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

    private static double squaredDistance(BuildLocation location, APosition centre) {
        double dx = location.tileX() - centre.tx();
        double dy = location.tileY() - centre.ty();
        return dx * dx + dy * dy;
    }

    /**
     * Where to search. {@code ANYWHERE} means "around our base", the same
     * convention the legacy pipeline uses; a named area resolves to its own
     * centre.
     */
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

    private static APosition areaCentre(String areaName) {
        if ("NATURAL".equals(areaName)) {
            APosition natural = atlantis.map.base.BaseLocations.natural();
            if (natural != null)
                return natural;
        }
        return baseCentre();
    }

    private static APosition baseCentre() {
        AUnit base = Select.main();
        if (base == null)
            base = Select.ourBases().first();
        if (base == null)
            base = Select.ourBuildings().first();
        return base != null ? base.position() : null;
    }

    private static AUnitType resolveUnitType(Producible producible) {
        if (producible instanceof UnitProducible) {
            return ((UnitProducible) producible).type();
        }
        return null;
    }
}
