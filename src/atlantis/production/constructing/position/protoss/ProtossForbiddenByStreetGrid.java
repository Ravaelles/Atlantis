package atlantis.production.constructing.position.protoss;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
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

        // === CB ==============================================

        if (building.isCombatBuilding() && position.distToNearestChokeLessThan(4)) {
            if (moduloX <= 0) return failed("TX modulo CB_X1 = " + moduloX);
            if (moduloY <= 0) return failed("TY modulo CB_Y1 = " + moduloY);
        }

        // === Pylon ===========================================

        if (building.isPylon()) {
            if (
//                moduloX != 2
                moduloX != 2
            ) {
                if (A.supplyUsed() >= 30 && !A.hasFreeSupply(1) && A.minerals() >= 300) {
                    if (moduloX != 4 && moduloX != 6) return failed("TX modulo PP_X_Special_1 = " + moduloX);
                    if (A.supplyTotal() >= 90) return failed("TX modulo PP_X_Special_2 = " + moduloX);
                }
                else {
                    if (A.supplyTotal() >= 90 && !A.hasFreeSupply(1)) return false;

                    return failed("TX modulo PP_X2 = " + moduloX);
                }
            }

            if (
                moduloY != 2 && moduloY != 4
            ) {
//                if (moduloY != 6 && A.hasMinerals(800) && A.supplyFree() <= 0) return false;

                return failed("TY modulo PP_Y2 = " + moduloY);
            }

//            System.err.println("Pylon: " + moduloX + " / " + moduloY);
        }

        // === Non-pylon ======================================

        else {
//            System.err.println(moduloX + " / " + moduloY);

            // P:2-3  G:4-7  G:8-11
            boolean moduloXIsInvalid = moduloX != 2 && moduloX != 4 && moduloX != 6 && moduloX != 8;

            // G:2-4  G:5-7
            boolean moduloYIsInvalid = moduloY != 2 && moduloY != 5;

            if (
                (moduloXIsInvalid || moduloYIsInvalid)
                    && A.supplyUsed() <= 60
//                    && building.isGateway()
                    && !building.isPylon()
                    && !building.isGasBuilding()
            ) {
                moduloXIsInvalid = moduloX <= 1;
                moduloYIsInvalid = moduloY <= 1;

                if (building.isForge() && Count.gateways() == 0) return false;
            }

            if (A.hasMinerals(700) && (moduloXIsInvalid || moduloYIsInvalid)) return false;

            if (moduloXIsInvalid) {
                return failed("TX modulo PG_X2 = " + moduloX + " / tx:" + position.tx() + " / grid:" + GRID_SIZE_X);
            }

//            if (moduloXIsInvalid && building.getTileWidth() <= 3) {
//                moduloXIsInvalid = moduloX != 6;
//            }

//            if (moduloYIsInvalid && building.getTileHeight() <= 2) {
//                moduloYIsInvalid = moduloY != 5 && moduloY != 8;
//            }

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
