package atlantis.placement.strategy;

import atlantis.map.position.APosition;
import atlantis.placement.strategy.BuildingPlacementStrategy.PlacementQuery;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 * Pylon sites (owner 2026-11-09, {@code forPylon} as ranked tiers):
 * strict Pylon cells first, relaxed even-MX cells when the strict
 * ones are taken, plain non-corridor ground last. Pylons own range,
 * so they never ask for power - and they never answer null while any
 * street-legal ground exists, so a supply block degrades instead of
 * dying on a veto.
 *
 * <p>Spacing (owner 2026-11-09): while we hold 5 or fewer Pylons, every
 * new one lands at least 8 tiles from the standing ones - the clump
 * around the main wasted the range the Pylons exist for. The second
 * Pylon is searched from halfway between the main base and the main
 * choke instead of the demand anchor, so it lands mid-way out rather
 * than on top of the first.</p>
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

    /**
     * While this many (or fewer) Pylons stand, the new one keeps its
     * distance - past it the map is covered and density is fine.
     */
    static final int SPACED_PYLONS = 5;

    /**
     * Minimum tile distance between Pylon origins while spacing holds.
     */
    static final double SPACING_TILES = 8;

    @Override
    public APosition find(PlacementQuery query) {
        if (query == null || query.centre() == null) return null;

        AUnitType type = query.building();
        APosition anchor = anchorFor(query);

        APosition hit = walk(query, type, anchor, true, false);
        if (hit != null) return hit;

        hit = walk(query, type, anchor, false, true);
        if (hit != null) return hit;

        return walkAny(query, type, anchor);
    }

    /**
     * The second Pylon is searched from halfway between the main base
     * and the main choke; everything else searches from the demand
     * anchor. Missing main or choke falls back to the anchor.
     */
    private static APosition anchorFor(PlacementQuery query) {
        try {
            if (atlantis.units.select.Count.pylonsWithUnfinished() != 1) return query.centre();

            AUnit main = atlantis.units.select.Select.mainOrAnyBuilding();
            atlantis.map.choke.AChoke choke = atlantis.map.choke.Chokes.mainChoke();
            if (main == null || main.position() == null || choke == null || choke.center() == null) {
                return query.centre();
            }

            return APosition.create(
                (main.position().tx() + choke.center().tx()) / 2,
                (main.position().ty() + choke.center().ty()) / 2);
        } catch (Exception | Error ignored) {
            return query.centre();
        }
    }

    /**
     * Live Pylons the spacing holds from (standing and warping alike -
     * both will own ground when the new one lands).
     */
    private static java.util.List<AUnit> livePylons() {
        java.util.List<AUnit> out = new java.util.ArrayList<>();
        try {
            for (AUnit pylon : atlantis.units.select.Select.ourWithUnfinished(AUnitType.Protoss_Pylon).list()) {
                if (pylon == null || !pylon.isAlive() || pylon.position() == null) continue;
                out.add(pylon);
            }
        } catch (Exception | Error ignored) {
        }

        return out;
    }

    private static boolean tooCloseToPylons(APosition tile, java.util.List<AUnit> pylons) {
        if (pylons.size() > SPACED_PYLONS) return false;
        try {
            for (AUnit pylon : pylons) {
                if (pylon != null && tile.distTo(pylon) < SPACING_TILES) return true;
            }
        } catch (Exception | Error ignored) {
            return false;
        }

        return false;
    }

    private static APosition walk(PlacementQuery query, AUnitType type, APosition anchor, boolean strict, boolean relaxed) {
        java.util.List<AUnit> pylons = livePylons();
        final APosition[] hit = new APosition[1];
        StreetCells.spiral(anchor, TIER_RADIUS, TIER_BUDGET, (tx, ty) -> {
            boolean cell = strict
                ? StreetCells.isPylonCell(tx, ty)
                : StreetCells.isRelaxedPylonCell(tx, ty);
            if (!cell) return true;

            APosition tile = APosition.create(tx, ty);
            if (query.isExcluded(tx, ty)) return true;
            if (query.isOccupied(tile, type)) return true;
            if (tooCloseToPylons(tile, pylons)) return true;

            hit[0] = tile;

            return false;
        });

        return hit[0];
    }

    private static APosition walkAny(PlacementQuery query, AUnitType type, APosition anchor) {
        java.util.List<AUnit> pylons = livePylons();
        final APosition[] hit = new APosition[1];
        StreetCells.spiral(anchor, TIER_RADIUS, TIER_BUDGET, (tx, ty) -> {
            if (StreetCells.isCorridor(tx, ty)) return true;

            APosition tile = APosition.create(tx, ty);
            if (query.isExcluded(tx, ty)) return true;
            if (query.isOccupied(tile, type)) return true;
            if (tooCloseToPylons(tile, pylons)) return true;

            hit[0] = tile;

            return false;
        });

        return hit[0];
    }
}
