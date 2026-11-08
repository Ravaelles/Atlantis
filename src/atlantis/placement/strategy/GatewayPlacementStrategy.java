package atlantis.placement.strategy;

import atlantis.map.position.APosition;
import atlantis.map.position.PsiField;
import atlantis.placement.strategy.BuildingPlacementStrategy.PlacementQuery;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gateway sites (owner 2026-11-09): a Gateway always sits on existing
 * power, nearest Pylon first. For every own Pylon, nearest first, the
 * strategy walks the Gateway street cells it powers and serves the
 * first free one; when no Pylon powers a free cell it moves to the
 * next Pylon. Standing power first, warping power second - and null
 * when nothing anywhere is powered, never an unpowered tile.
 *
 * <p>The power read is the mint-time predicate itself
 * ({@code IsPoweredByAPylon} for standing, {@code PsiField} over
 * warping Pylons for incoming), so the strategy, the planner and the
 * mint door can never disagree on what "powered" means.</p>
 */
public final class GatewayPlacementStrategy implements BuildingPlacementStrategy {

    static final int GATEWAY_TILES_W = 4;
    static final int GATEWAY_TILES_H = 3;

    /**
     * Rings walked around each Pylon (psi reaches ~8 tiles).
     */
    static final int PYLON_RADIUS = 9;

    /**
     * Candidates evaluated per Pylon before moving on.
     */
    static final int PYLON_BUDGET = 400;

    @Override
    public APosition find(PlacementQuery query) {
        if (query == null || query.centre() == null) return null;

        AUnitType type = query.building();

        APosition hit = walkPylons(query, type, true);
        if (hit != null) return hit;

        return walkPylons(query, type, false);
    }

    private static APosition walkPylons(PlacementQuery query, AUnitType type, boolean standingOnly) {
        for (AUnit pylon : pylonsNearestFirst(query.centre(), standingOnly)) {
            APosition tile = walkPylon(query, type, pylon);
            if (tile != null) return tile;
        }

        return null;
    }

    private static APosition walkPylon(PlacementQuery query, AUnitType type, AUnit pylon) {
        int[] origin = pylonOrigin(pylon);
        final APosition[] hit = new APosition[1];

        StreetCells.spiral(
            APosition.create(origin[0], origin[1]), PYLON_RADIUS, PYLON_BUDGET, (tx, ty) -> {
                if (!StreetCells.isGatewayCell(tx, ty)) return true;
                if (!PsiField.covers(
                    origin[0], origin[1], tx, ty, GATEWAY_TILES_W, GATEWAY_TILES_H
                )) return true;

                APosition tile = APosition.create(tx, ty);
                if (query.isExcluded(tx, ty)) return true;
                if (query.isOccupied(tile, type)) return true;

                hit[0] = tile;

                return false;
            });

        return hit[0];
    }

    private static List<AUnit> pylonsNearestFirst(APosition anchor, boolean standingOnly) {
        List<AUnit> pylons = new ArrayList<>();
        try {
            for (AUnit pylon : Select.ourOfType(AUnitType.Protoss_Pylon).list()) {
                if (pylon == null || !pylon.isAlive()) continue;
                if (standingOnly && !pylon.isCompleted()) continue;
                if (pylon.position() == null) continue;
                pylons.add(pylon);
            }
        } catch (Exception | Error ignored) {
            return pylons;
        }

        final APosition home = anchor;
        pylons.sort(Comparator.comparingDouble(p -> {
            try {
                return p.distTo(home);
            } catch (Exception | Error ignored) {
                return Double.MAX_VALUE;
            }
        }));

        return pylons;
    }

    /**
     * Same origin-tile contract as {@code IsPoweredByAPylon}: tile
     * position when the engine knows it, tile address otherwise.
     */
    static int[] pylonOrigin(AUnit pylon) {
        try {
            bwapi.TilePosition tile = pylon.tilePosition();
            if (tile != null) return new int[]{tile.getX(), tile.getY()};
        } catch (Exception | Error ignored) {
        }

        return new int[]{pylon.tx(), pylon.ty()};
    }
}
