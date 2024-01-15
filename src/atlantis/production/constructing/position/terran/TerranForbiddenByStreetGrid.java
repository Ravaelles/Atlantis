package atlantis.production.constructing.position.terran;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class TerranForbiddenByStreetGrid {
    private static final int GRID_VALUE_X = 9;
    private static final int GRID_VALUE_Y = 6;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (!We.terran()) return false;

        if (ForbiddenByStreetGridForSupplyDepotAndAcademy.isForbidden(builder, building, position)) return true;
        if (ForbiddenByStreetGridForBarracks.isForbidden(builder, building, position)) return true;

        if (
            building.isBase()
                || building.isGasBuilding()
                || building.isSupplyDepot()
                || building.isCombatBuilding() // @Check
        ) return false;

        if (We.protoss() && A.supplyTotal() <= 10) return false;

//        if (
//            building.isMissileTurret() && Select.ourBuildingsWithUnfinished().inRadius(3, position).empty()
//        ) return false;

        // =========================================================

//        if (position.tx() % GRID_VALUE_X <= 0) return fail("TX modulo");
//        if (position.ty() % GRID_VALUE_Y <= 0) return fail("TY modulo");

        if ((position.tx() + building.dimensionRightPixels() / 32) % GRID_VALUE_X <= 0) return fail("TX modulo");
        if ((position.ty() + building.dimensionDownPixels() / 32) % GRID_VALUE_Y <= 0) return fail("TY modulo");

//        if ((position.tx() + building.dimensionRightPx() * 32) % GRID_VALUE_X <= 0) return fail("TX modulo");
//        if ((position.ty() + building.dimensionDownPx() * 32) % GRID_VALUE_Y <= 0) return fail("TY modulo");

//        if (building.getTileWidth() >= 4) {
//            if (position.tx() % 4 >= 2) return fail("TX big modulo");
//        }

//        if (building.isBarracks() || building.isFactory()) {
//            if (position.tx() % 3 <= 1) return fail("TX Barracks & Factory modulo");
//            if (position.ty() % 3 <= 1) return fail("TX Barracks & Factory modulo");
//        }

        // =========================================================

//        // Leave entire vertical (same tileX) corridor free for units
//        if (
//            buildingLeftTx(building, position) % GRID_VALUE <= 1
//                || buildingRightTx(building, position) % GRID_VALUE <= 1
//        ) {
//            if (
//                !position.translateByTiles(-1 - building.dimensionLeftTx(), 0).isWalkable()
//                    && !position.translateByTiles(+1 + building.dimensionRightTx(), 0).isWalkable()
//            ) {
//                return fail("LEAVE_PLACE_VERTICALLY");
//            }
//        }
//
//        // Leave entire horizontal (same tileY) corridor free for units
//        if (
//            position.ty() % GRID_VALUE <= 1
//                || (position.ty() + building.dimensionDownPx() / 32) % GRID_VALUE <= 0
//        ) {
//            if (
//                !position.translateByTiles(0, -1 - building.dimensionUpTx()).isWalkable()
//                    && !position.translateByTiles(0, +1 + building.dimensionDownTx()).isWalkable()
//            ) {
//                return fail("LEAVE_PLACE_HORIZONTALLY");
//            }
//        }

        return false;
    }

    private static boolean fail(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }

    private static int buildingLeftTx(AUnitType building, APosition position) {
        return position.tx() - building.dimensionLeftPixels() / 32;
    }

    private static int buildingRightTx(AUnitType building, APosition position) {
        int addonBonus = building.addonWidthInPx();

        return (int) (position.tx() + (building.dimensionRightPixels() + addonBonus) / 32);
    }
}
