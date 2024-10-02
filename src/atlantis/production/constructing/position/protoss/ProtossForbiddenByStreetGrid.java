package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossForbiddenByStreetGrid {
    public static final int GRID_SIZE_X = 11;
    public static final int GRID_SIZE_Y = 7;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (building.isBase() || building.isAssimilator()) return false;

        int moduloX = (position.tx() % GRID_SIZE_X);
        int moduloY = (position.ty() % GRID_SIZE_Y);

        // === Pylon ===========================================

        if (building.isPylon()) {
            if (
//                moduloX != 2
                moduloX != 2
            ) return failed("TX modulo PP_X2 = " + moduloX);

            if (
                moduloY != 2 && moduloY != 4 && moduloY != 6
            ) return failed("TY modulo PP_Y2 = " + moduloY);

//            System.err.println("Pylon: " + moduloX + " / " + moduloY);
        }

        // === Non-pylon ======================================

        else {
//            System.err.println(moduloX + " / " + moduloY);

            // P:2-3  G:4-7  G:8-11
            boolean moduloXIsInvalid = moduloX != 4 && moduloX != 8;

//            if (moduloXIsInvalid && building.getTileWidth() <= 3) {
//                moduloXIsInvalid = moduloX != 6;
//            }

            // G:2-4  G:5-7
            boolean moduloYIsInvalid = moduloY != 2 && moduloY != 5;

//            if (moduloYIsInvalid && building.getTileHeight() <= 2) {
//                moduloYIsInvalid = moduloY != 5 && moduloY != 8;
//            }

            if (moduloXIsInvalid) {
                return failed("TX modulo PG_X2 = " + moduloX + " / tx:" + position.tx() + " / grid:" + GRID_SIZE_X);
            }

            if (moduloYIsInvalid) {
                return failed("TY modulo PG_Y2 = " + moduloY + " / ty:" + position.ty() + " / grid:" + GRID_SIZE_Y);
            }

//            if (moduloX == 5 && moduloY == 4) return failed("TY/TX modulo mix");
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }

//    private static int buildingLeftTx(AUnitType building, APosition position) {
//        return position.tx() - building.dimensionLeftPx() / 32;
//    }
//
//    private static int buildingRightTx(AUnitType building, APosition position) {
//        int addonBonus = building.addonWidthInPx();
//
//        return (int) (position.tx() + (building.dimensionRightPx() + addonBonus) / 32);
//    }
}
