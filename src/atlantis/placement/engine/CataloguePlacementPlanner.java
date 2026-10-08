package atlantis.placement.engine;

import atlantis.map.position.APosition;
import atlantis.placement.core.BuildLocation;
import atlantis.placement.core.BuildLocationCatalogue;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * S1 of the placement rewrite (`_AI/redesign/03_PLACEMENT.md` §5.4): the
 * catalogue-backed {@link PlacementPlanner}.
 *
 * <p>
 * It answers the same seam the scheduler already consumes, so nothing outside
 * this class changes when the Block templates and Psi gating arrive (S2/S3).
 * What
 * it adds over {@link LegacyPlacementPlanner} - which returned one tile from
 * {@code APositionFinder} - is a <b>ranked list of validated candidates</b>:
 * the
 * catalogue knows every tile on the map a footprint fits on, and the planner
 * picks the one nearest the requested neighbourhood.
 * </p>
 *
 * <p>
 * Deliberately not here yet, and each is a later stage rather than a TODO in
 * this
 * class: Block prefab layouts (S2), Protoss Pylon power gating and pull-forward
 * (S3), choke/wall placement (S4). The catalogue is the seam they plug into.
 * </p>
 */
public final class CataloguePlacementPlanner implements PlacementPlanner {

    private final TileAvailabilityGrid grid;
    private final BuildLocationCatalogue catalogue;

    /**
     * Tiles claimed in the current pass. The grid is rebuilt between passes (the
     * plan is stateless per frame), so a reservation is applied to the grid as
     * {@link TileAvailabilityGrid#markUsed} and disappears with it.
     */
    private final Set<String> reservedThisPass = new HashSet<>();

    public CataloguePlacementPlanner() {
        this(new TileAvailabilityGrid(new EngineTerrainSource()));
    }

    /** Test seam: a grid over a synthetic terrain. */
    public CataloguePlacementPlanner(TileAvailabilityGrid grid) {
        this.grid = grid;
        this.catalogue = new BuildLocationCatalogue(grid);
    }

    @Override
    public void startPass() {
        reservedThisPass.clear();
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

        List<BuildLocation> candidates = catalogue.candidates(
                unitType.getTilesWidth(), unitType.getTilesHeights(), nearestTo(centre));

        for (BuildLocation candidate : candidates) {
            String tile = candidate.tileX() + ":" + candidate.tileY();
            if (reservedThisPass.contains(tile))
                continue;

            reservedThisPass.add(tile);
            grid.markUsed(candidate.tileX(), candidate.tileY(),
                    candidate.tileWidth(), candidate.tileHeight());

            return PlacementReservation.success(candidate.tileX(), candidate.tileY(), targetFrame);
        }

        return PlacementReservation.failure();
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
