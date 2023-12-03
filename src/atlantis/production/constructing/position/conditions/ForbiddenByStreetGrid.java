package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ForbiddenByStreetGrid {
    private static final int GRID_VALUE = 8;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (We.protoss() && A.supplyTotal() <= 10) return false;
        if (building.isBase() || building.isGasBuilding()) return false;

        if (
            building.isMissileTurret() && Select.ourBuildingsWithUnfinished().inRadius(3, position).empty()
        ) return false;

        // =========================================================

//        if (building.isCombatBuilding()) {
//            if (position.tx() % 6 <= 1) return fail("TX CB modulo");
//            if (position.ty() % 6 <= 1) return fail("TY CB modulo");
//        }
//        else {
        if (!building.isSupplyDepot()) {
            if (position.tx() % GRID_VALUE <= 0) return fail("TX modulo");
            if (position.ty() % GRID_VALUE <= 0) return fail("TY modulo");
        }

//        if (building.isBarracks() || building.isFactory()) {
//            if (position.tx() % 3 <= 1) return fail("TX Barracks & Factory modulo");
//            if (position.ty() % 3 <= 1) return fail("TX Barracks & Factory modulo");
//        }

        // =========================================================

        // Leave entire vertical (same tileX) corridor free for units
        if (
            buildingLeftTx(building, position) % GRID_VALUE <= 1
                || buildingRightTx(building, position) % GRID_VALUE <= 1
        ) {
            if (
                !position.translateByTiles(-1 - building.dimensionLeftTx(), 0).isWalkable()
                    && !position.translateByTiles(+1 + building.dimensionRightTx(), 0).isWalkable()
            ) {
                return fail("LEAVE_PLACE_VERTICALLY");
            }
        }

        // Leave entire horizontal (same tileY) corridor free for units
        if (
            position.ty() % GRID_VALUE <= 1
                || (position.ty() + building.dimensionDownPx() / 32) % GRID_VALUE <= 0
        ) {
            if (
                !position.translateByTiles(0, -1 - building.dimensionUpTx()).isWalkable()
                    && !position.translateByTiles(0, +1 + building.dimensionDownTx()).isWalkable()
            ) {
                return fail("LEAVE_PLACE_HORIZONTALLY");
            }
        }

        return false;
    }

    private static boolean fail(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }

    private static int buildingLeftTx(AUnitType building, APosition position) {
        return position.tx() - building.dimensionLeftPx() / 32;
    }

    private static int buildingRightTx(AUnitType building, APosition position) {
        int addonBonus = building.addonWidthInPx();

        return (int) (position.tx() + (building.dimensionRightPx() + addonBonus) / 32);
    }
}
