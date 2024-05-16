package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossForbiddenByStreetGrid {
    public static final int GRID_SIZE_X = 18;
    public static final int GRID_SIZE_Y = 10;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (building.isBase() || building.isGasBuilding()) return false;

        if (building.isPylon()) {
            int moduloX = (position.tx() % GRID_SIZE_X);
            int moduloY = (position.ty() % GRID_SIZE_Y);

            if (moduloX != 9 && moduloX != 11 && moduloX != 13 && moduloX != 15)
                return failed("TX modulo PP_X2 = " + moduloX);
            if (
                moduloY != 0 && moduloY != 2 && moduloY != 6
            ) return failed("TY modulo PP_Y2 = " + moduloY);

//            System.err.println("Pylon: " + moduloX + " / " + moduloY);
        }
        else {
            int moduloX = (position.tx() % GRID_SIZE_X);
            int moduloY = (position.ty() % GRID_SIZE_Y);
//            System.err.println(moduloX + " / " + moduloY);

            // G:1 G:5  P:9  G:11 G:14
            boolean moduloXIsInvalid = moduloX != 0
                && moduloX != 4
                && moduloX != 7
                && moduloX != 11;

            // G:1  G:4  P:1 P:3 P:5
//            boolean moduloYIsInvalid = moduloY != 1 && moduloY != 4;
            boolean moduloYIsInvalid = moduloY != 0
                && moduloY != 3
                && moduloY != 6;

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
