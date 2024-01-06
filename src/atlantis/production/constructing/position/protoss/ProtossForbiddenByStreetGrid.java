package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossForbiddenByStreetGrid {
    private static final int GRID_VALUE_X = 9;
    private static final int GRID_VALUE_Y = 6;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        if (!We.protoss()) return false;
        if (building.isBase() || building.isGasBuilding()) return false;

        // =========================================================

        if ((position.tx() + building.dimensionRightPx() / 32) % GRID_VALUE_X <= 0) return fail("TX modulo");
        if ((position.ty() + building.dimensionDownPx() / 32) % GRID_VALUE_Y <= 0) return fail("TY modulo");

        return false;
    }

    private static boolean fail(String reason) {
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
