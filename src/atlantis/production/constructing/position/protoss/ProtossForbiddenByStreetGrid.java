package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossForbiddenByStreetGrid {
    public static final int GRID_SIZE_X = 14;
    public static final int GRID_SIZE_Y = 12;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (!We.protoss()) return false;
        if (building.isBase() || building.isGasBuilding()) return false;

        if (building.isPylon()) {
            int moduloX = (position.tx() % GRID_SIZE_X);
            int moduloY = (position.ty() % GRID_SIZE_Y);

            if (moduloX != 5 && moduloX != 7) return failed("TX modulo PP_X2 = " + moduloX);
            if (moduloY != 4 && moduloY != 6) return failed("TY modulo PP_Y2 = " + moduloY);
        }
        else {
            int moduloX = (position.tx() % GRID_SIZE_X);
            int moduloY = (position.ty() % GRID_SIZE_Y);

            // G:1  P:5 P:7  G:9
            if (moduloX != 1 && moduloX != 9 && moduloX != 10) return failed("TX modulo PG_X2 = " + moduloX
                + " / tx:" + position.tx() + " / grid:" + GRID_SIZE_X);

            // G:1  P:4 P:6  G:8
            if (moduloY != 1 && moduloY != 4 && moduloY != 8) return failed("TY modulo PG_Y2 = " + moduloY
                + " / ty:" + position.ty() + " / grid:" + GRID_SIZE_Y);
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
