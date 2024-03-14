package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossForbiddenByStreetGrid {
    public static final int GRID_SIZE_X = 13;
    public static final int GRID_SIZE_Y = 11;
//    public static final int GRID_VALUE_X = 14;
//    public static final int GRID_VALUE_Y = 14;
//    public static final int GRID_VALUE_X = 13;
//    public static final int GRID_VALUE_Y = 13;

    /**
     * Returns true if game says it's possible to build given building at this position.
     * <p>
     * Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
     * So disallow building in e.g. 0,1, 6,7, 12,13, horizontally and vertically
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
//        if (true) return false;

        if (!We.protoss()) return false;
        if (building.isBase() || building.isGasBuilding()) return false;

//        if ((modulo = (position.tx() % 4)) != 0) return failed("TX modulo P_XG = " + modulo);
//        if ((modulo = (position.ty() % 3)) != 0) return failed("TY modulo P_YGs = " + modulo);

        if (building.isPylon()) {
            int moduloX = (position.tx() % GRID_SIZE_X);
            int moduloY = (position.ty() % GRID_SIZE_Y);

//            return ForbiddenByStreetGridForPylon.isForbidden(builder, building, position);

//            if ((modulo = (position.tx()) % 2) != 0) return failed("TX modulo PP_X1 = " + modulo);
//            if ((modulo = (position.ty()) % 2) != 0) return failed("TY modulo PP_Y1 = " + modulo);

//            if (modulo <= 7 || modulo >= 12) return failed("TX modulo PP_X2 = " + modulo);
            if (moduloX != 8 && moduloX != 10) return failed("TX modulo PP_X2 = " + moduloX);
            if (moduloY != 5 && moduloY != 7) return failed("TY modulo PP_Y2 = " + moduloY);
        }
        else {
            int moduloX = (position.tx() % GRID_SIZE_X);
            int moduloY = (position.ty() % GRID_SIZE_Y);

//            if (moduloX % 4 != 0) return failed("TX modulo PG_X1 = " + moduloX
//                + " / tx:" + position.tx() + " / grid:" + GRID_SIZE_X);
//            if (moduloY % 3 != 0) return failed("TY modulo PG_Y1 = " + moduloY
//                + " / ty:" + position.ty() + " / grid:" + GRID_SIZE_Y);

//            if ((modulo = (position.tx() % 4)) != 0) return failed("TX modulo PG_X1 = " + modulo);
//            if ((modulo = (position.ty() % 3)) != 0) return failed("TY modulo PG_Y1 = " + modulo);

            if (moduloX != 0 && moduloX != 4) return failed("TX modulo PG_X2 = " + moduloX
                + " / tx:" + position.tx() + " / grid:" + GRID_SIZE_X);

            if (moduloY != 0 && moduloY != 3) return failed("TY modulo PG_Y2 = " + moduloY
                + " / ty:" + position.ty() + " / grid:" + GRID_SIZE_Y);
        }

        if (true) return false;

        if (isProducingBuilding(building)) {
            if (ProtossForbiddenForProducerBuildings.isForbidden(builder, building, position)) return true;
            return false;
        }

        // =========================================================

//        System.err.println("------------------");
//        System.err.println("position = " + position);
//        System.err.println("position.x() = " + position.x());
//        System.err.println("building.dimensionLeftPixels() = " + building.dimensionLeftPixels());
//        System.err.println(position + " MODULO = "
//            + (((position.x() - building.dimensionLeftPixels()) / 32) % GRID_VALUE_X) + " / "
//            + (((position.y() - building.dimensionUpPixels()) / 32) % GRID_VALUE_Y)
//        );

//        if (position.tx() % 2 == 0 || position.ty() % 2 == 0) return failed("TX modulo even");

//        int modulo;
//        if ((modulo = (position.tx()) % GRID_SIZE_X) <= 1) return failed("TX modulo A = " + modulo);
//        if ((modulo = (position.ty()) % GRID_SIZE_Y) <= 1) return failed("TY modulo C = " + modulo);

//        if (((position.x() + building.dimensionRightPixels()) / 32) % GRID_VALUE_X == 1) return failed("TX modulo B");
//        if (((position.y() + building.dimensionDownPixels()) / 32) % GRID_VALUE_Y == 1) return failed("TY modulo D");

        // =========================================================

//        if (((position.x() - building.dimensionLeftPixels()) / 32) % GRID_VALUE_X == 1) return failed("TX modulo A");
//        if (((position.x() + building.dimensionRightPixels()) / 32) % GRID_VALUE_X == 1) return failed("TX modulo B");
//
//        if (((position.y() - building.dimensionUpPixels()) / 32) % GRID_VALUE_Y == 1) return failed("TY modulo C");
//        if (((position.y() + building.dimensionDownPixels()) / 32) % GRID_VALUE_Y == 1) return failed("TY modulo D");

        // =========================================================

        return false;
    }

    private static boolean isProducingBuilding(AUnitType building) {
        return building.isGateway() || building.isRoboticsFacility();
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
