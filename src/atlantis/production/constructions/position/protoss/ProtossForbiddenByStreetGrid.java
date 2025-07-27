package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import java.util.HashMap;
import java.util.Map;

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
        if (ignore(building)) return false;

        int moduloX = (position.tx() % GRID_SIZE_X);
        int moduloY = (position.ty() % GRID_SIZE_Y);

        if (moduloX == 0) return failed("PTX modulo is 0");
        if (moduloY == 0) return failed("PTY modulo is 0");

        if (specialExclusionPermission(building)) return false;

        // === CB ==============================================

        if (building.isCombatBuilding()) {
//            if (position.distToNearestChokeLessThan(4)) {
//                if (moduloX != 0) return failed("TX modulo CB_X1 = " + moduloX);
//                if (moduloY != 0) return failed("TY modulo CB_Y1 = " + moduloY);
//            }

            if (position.distToNearestChokeLessThan(3.8)) {
                return failed("CB too close to choke");
            }

            if (
                (
                    position.tx() % GRID_SIZE_X == 0 || position.ty() % GRID_SIZE_Y == 0
                        || position.tx() % GRID_SIZE_X == 5 || position.ty() % GRID_SIZE_Y == 5
                )
                    && Count.withPlanned(AUnitType.Protoss_Photon_Cannon) >= 2
                    && Select.mainOrAnyBuilding().groundDist(position) <= 45
            ) {
                return failed("TX/TY modulo CB_XY = " + moduloX + " / " + moduloY);
            }

            return false;
//            if (position.distTo(Select.mainOrAnyBuilding()) >= 30) return false;
        }

        // === Pylon ===========================================

        if (building.isPylon()) {
            if (asPylon(moduloX, moduloY, position)) return true;
        }

        // === Non-pylon ======================================

        else {
            if (asNonPylon(moduloX, moduloY, position, building)) return true;
        }

        return false;
    }

    private static boolean asNonPylon(int moduloX, int moduloY, APosition position, AUnitType building) {
        //            System.err.println(moduloX + " / " + moduloY);

        // P:2-3  G:4-7  G:8-11
        boolean moduloXIsInvalid = moduloX != 2 && moduloX != 4 && moduloX != 6 && moduloX != 8;

        // G:2-4  G:5-7
        boolean moduloYIsInvalid = moduloY != 2 && moduloY != 5;

        // =========================================================

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
            if (building.isCannon() && A.supplyUsed() >= 100) return false;
        }

        // =========================================================

        if (A.hasMinerals(700) && (moduloXIsInvalid || moduloYIsInvalid)) {
            return buildingsNear(4, position) >= 2 && failed("Too many buildings near already");
        }

        // =========================================================

        if (moduloXIsInvalid) {
            if (
                building.isGateway()
                    && moduloX == 0
                    && buildingsNear(4, position) <= 1
                    && (A.supplyUsed() <= 80 || A.minerals() >= 650 || A.supplyFree() <= 2)
            ) return false;

            return failed("TX modulo NonP_X = " + moduloX + " / tx:" + position.tx() + " / grid:" + GRID_SIZE_X);
        }

//            if (moduloXIsInvalid && building.getTileWidth() <= 3) {
//                moduloXIsInvalid = moduloX != 6;
//            }

//            if (moduloYIsInvalid && building.getTileHeight() <= 2) {
//                moduloYIsInvalid = moduloY != 5 && moduloY != 8;
//            }

        if (moduloYIsInvalid) {
            return failed("TY modulo NonP_Y = " + moduloY + " / ty:" + position.ty() + " / grid:" + GRID_SIZE_Y);
        }

//            if (moduloX == 5 && moduloY == 4) return failed("TY/TX modulo mix");

        return false;
    }

    private static boolean asPylon(int moduloX, int moduloY, APosition position) {
        if (moduloX != 2) {
            if (A.supplyUsed() >= 30 && !A.hasFreeSupply(1) && A.minerals() >= 300) {
                if (moduloX != 4 && moduloX != 6) return failed("TX modulo Pyl_X_Special_1 = " + moduloX);
                if (A.supplyTotal() >= 90) return failed("TX modulo Pyl_X_Special_2 = " + moduloX);
            }
            else {
                if (
                    moduloX % 2 == 0
                        && A.supplyTotal() >= 30
                        && !A.hasFreeSupply(1)
                        && buildingsNear(4, position) <= 1
                ) return false;

                return failed("TX modulo Pyl_X = " + moduloX);
            }
        }

        if (moduloY != 2 && moduloY != 4) {
            if (moduloY != 3 && A.supplyFree() <= 2 && Count.pylons() >= 2) return false;

            return failed("TY modulo Pyl_Y = " + moduloY);
        }

//            System.err.println("Pylon: " + moduloX + " / " + moduloY);
        return false;
    }

    private static int buildingsNear(double radius, APosition position) {
        return Select.ourBuildingsWithUnfinished().inRadius(radius, position).count();
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
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

    private static boolean ignore(AUnitType building) {
        if (building.isBase() || building.isAssimilator()) return true;
        if (A.supplyTotal() <= 15) {
            if (building.isPylon()) return true;
            if (building.isForge()) return true;
        }

        return false;
    }

    private static Map<AUnitType, Integer> specialExclusions = new HashMap<>();

    private static boolean specialExclusionPermission(AUnitType building) {
        if (specialExclusions.isEmpty()) return false;

        return specialExclusions.containsKey(building) && specialExclusions.get(building) == A.now();
    }

    public static void addSpecialGridExclusionPermission(AUnitType building) {
        specialExclusions.put(building, A.now());
    }
}
