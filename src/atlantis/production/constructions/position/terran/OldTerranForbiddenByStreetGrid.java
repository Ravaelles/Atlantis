package atlantis.production.constructions.position.terran;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class OldTerranForbiddenByStreetGrid {
    //    private static final int GRID_VALUE_X = 9;
//    private static final int GRID_VALUE_Y = 6;
    public static final int GRID_VALUE_X = 13;
    public static final int GRID_VALUE_Y = 13;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (!We.terran()) return false;

        if (building.isSupplyDepot() || building.isAcademy()) {
            return ForbiddenByStreetGridForSupplyDepotAndAcademy.isForbidden(builder, building, position);
//            if (ForbiddenByStreetGridForSupplyDepotAndAcademy.isForbidden(builder, building, position)) return true;
        }

        if (
            building.isBase()
                || building.isGasBuilding()
                || building.isCombatBuilding() // @Check
        ) return false;

        int modulo;
        if ((modulo = (position.tx()) % 3) != 0) return failed("TX modulo K = " + modulo);
        if ((modulo = (position.ty()) % 3) != 0) return failed("TY modulo L = " + modulo);

        if (true) return false;

//        if (ForbiddenByStreetGridForBarracks.isForbidden(builder, building, position)) return true;

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
        AbstractPositionFinder._STATUS = reason;
        return true;
    }

    private static int buildingLeftTx(AUnitType building, APosition position) {
        return position.tx() - building.dimensionLeftPixels() / 32;
    }

    private static int buildingRightTx(AUnitType building, APosition position) {
        int addonBonus = building.addonWidthInPx();

        return (int) (position.tx() + (building.dimensionRightPixels() + addonBonus) / 32);
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}
