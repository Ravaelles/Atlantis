package atlantis.placement.strategy;

import atlantis.map.position.APosition;
import atlantis.placement.strategy.BuildingPlacementStrategy.PlacementQuery;
import atlantis.units.AUnitType;

/**
 * Pylon sites (owner 2026-11-09, {@code forPylon} as ranked tiers):
 * strict Pylon cells first, relaxed even-MX cells when the strict
 * ones are taken, plain non-corridor ground last. Pylons own range,
 * so they never ask for power - and they never answer null while any
 * street-legal ground exists, so a supply block degrades instead of
 * dying on a veto.
 */
public final class PylonPlacementStrategy implements BuildingPlacementStrategy {

    /**
     * Candidates evaluated per tier before the walk stops.
     */
    static final int TIER_BUDGET = 1500;

    /**
     * Rings walked per tier around the anchor.
     */
    static final int TIER_RADIUS = 40;

    @Override
    public APosition find(PlacementQuery query) {
        if (query == null || query.centre() == null) return null;

        AUnitType type = query.building();
        APosition hit = walk(query, type, true, false);
        if (hit != null) return hit;

        hit = walk(query, type, false, true);
        if (hit != null) return hit;

        return walkAny(query, type);
    }

    private static APosition walk(PlacementQuery query, AUnitType type, boolean strict, boolean relaxed) {
        final APosition[] hit = new APosition[1];
        StreetCells.spiral(query.centre(), TIER_RADIUS, TIER_BUDGET, (tx, ty) -> {
            boolean cell = strict
                ? StreetCells.isPylonCell(tx, ty)
                : StreetCells.isRelaxedPylonCell(tx, ty);
            if (!cell) return true;

            APosition tile = APosition.create(tx, ty);
            if (query.isExcluded(tx, ty)) return true;
            if (query.isOccupied(tile, type)) return true;

            hit[0] = tile;

            return false;
        });

        return hit[0];
    }

    private static APosition walkAny(PlacementQuery query, AUnitType type) {
        final APosition[] hit = new APosition[1];
        StreetCells.spiral(query.centre(), TIER_RADIUS, TIER_BUDGET, (tx, ty) -> {
            if (StreetCells.isCorridor(tx, ty)) return true;

            APosition tile = APosition.create(tx, ty);
            if (query.isExcluded(tx, ty)) return true;
            if (query.isOccupied(tile, type)) return true;

            hit[0] = tile;

            return false;
        });

        return hit[0];
    }
}
