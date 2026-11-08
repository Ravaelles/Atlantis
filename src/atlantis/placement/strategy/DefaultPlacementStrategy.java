package atlantis.placement.strategy;

import atlantis.map.position.APosition;
import atlantis.placement.strategy.BuildingPlacementStrategy.PlacementQuery;
import atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon;
import atlantis.units.AUnitType;

/**
 * Sites for everything that is neither Pylon nor Gateway (owner
 * 2026-11-09, {@code forStandardBuilding} + {@code forCombatBuilding}
 * as ranked tiers): strict standard cells first, plain non-corridor
 * ground when those are taken. Combat buildings keep off the choke
 * (legacy 4.1 tiles). Power-needing buildings only ever see powered
 * cells - the mint-time predicate ({@code IsPoweredByAPylon}) itself,
 * so the strategy can never name an unpowered tile for them.
 */
public final class DefaultPlacementStrategy implements BuildingPlacementStrategy {

    static final int TIER_BUDGET = 1500;

    static final int TIER_RADIUS = 40;

    @Override
    public APosition find(PlacementQuery query) {
        if (query == null || query.centre() == null) return null;

        AUnitType type = query.building();
        boolean combat = type != null && type.isCombatBuilding();

        APosition hit = walk(query, type, combat, true);
        if (hit != null) return hit;

        return walk(query, type, combat, false);
    }

    private static APosition walk(PlacementQuery query, AUnitType type, boolean combat, boolean strict) {
        final APosition[] hit = new APosition[1];
        StreetCells.spiral(query.centre(), TIER_RADIUS, TIER_BUDGET, (tx, ty) -> {
            boolean cell = strict
                ? StreetCells.isStandardCell(tx, ty)
                : !StreetCells.isCorridor(tx, ty);
            if (!cell) return true;

            APosition tile = APosition.create(tx, ty);
            if (query.isExcluded(tx, ty)) return true;
            if (query.isOccupied(tile, type)) return true;
            if (combat && StreetCells.tooCloseToChoke(tile)) return true;
            if (type != null && type.needsPower()
                && !IsPoweredByAPylon.check(tile, type)) return true;

            hit[0] = tile;

            return false;
        });

        return hit[0];
    }
}
